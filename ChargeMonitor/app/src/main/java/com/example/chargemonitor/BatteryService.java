package com.example.chargemonitor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.ClassicFlattener;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy;
import com.elvishew.xlog.printer.file.clean.NeverCleanStrategy;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class BatteryService extends Service {
    private static final String CHANNEL_ID = "BatteryService";
    private static final String CHANNEL_NAME = "BatteryService";
    private static final int FOREGROUND_ID = 2345;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    ChargeState mainCard;
    ArrayList<Information> cardInfo;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mainCard == null)
            mainCard = new ChargeState("null", "null", "null", "null");
        if (cardInfo == null) {
            cardInfo = new ArrayList<>();
            cardInfo.add(new Information("当前电量百分比", "%", "-1"));
            cardInfo.add(new Information("当前电量", "mAh", "-1"));
            cardInfo.add(new Information("电池温度", "℃", "-1"));
            cardInfo.add(new Information("电池电压", "V", "-1"));
            cardInfo.add(new Information("电池电流", "A", "-1"));
            cardInfo.add(new Information("充电剩余时间", "min", "-1"));
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broadcastReceiver, filter);

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void run() {
                BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
                int current = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
                int capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                long remainTime = batteryManager.computeChargeTimeRemaining();

                assert cardInfo != null;
                cardInfo.get(MainActivity.INDEX_CURRENT).setValue(String.valueOf((float)current / 1000));
                cardInfo.get(MainActivity.INDEX_CAPACITY).setValue(String.valueOf(capacity / 1000));
                if (remainTime == -1) cardInfo.get(MainActivity.INDEX_REMAIN_TIME).setValue(String.valueOf(-1));
                else cardInfo.get(MainActivity.INDEX_REMAIN_TIME).setValue((new DecimalFormat(".00")).format((double)remainTime / 60000));

                sendBroadcast();
            }
        };
        timer.schedule(timerTask, 0, 500);

        Printer filePrinter = new FilePrinter
                .Builder("/storage/emulated/0/ChargeMonitor/")
                .fileNameGenerator(new DateFileNameGenerator())
                .backupStrategy(new NeverBackupStrategy())
                .cleanStrategy(new NeverCleanStrategy())
                .flattener(new ClassicFlattener())
                .build();
        XLog.init(LogLevel.ALL, filePrinter);
        Timer logTimer = new Timer();
        TimerTask logTimerTask = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void run() {
                if (mainCard == null) return;
                String isCharging = mainCard.isCharging, chargeType = mainCard.chargingType;
                String level = cardInfo.get(MainActivity.INDEX_LEVEL).getValue() + "%";
                String capacity = cardInfo.get(MainActivity.INDEX_CAPACITY).getValue() + "mAh";
                String temperature = cardInfo.get(MainActivity.INDEX_TEMPERATURE).getValue() + "℃";
                String voltage = cardInfo.get(MainActivity.INDEX_VOLTAGE).getValue() + "V";
                String current = cardInfo.get(MainActivity.INDEX_CURRENT).getValue() + "A";
                String remainTime = cardInfo.get(MainActivity.INDEX_REMAIN_TIME).getValue() + "min";
                LogInfo.BatteryLog batteryLog = new LogInfo.BatteryLog(level, capacity, temperature, voltage, current, remainTime);
                LogInfo logInfo = new LogInfo(isCharging, chargeType, batteryLog);
                Gson gson = new Gson();
                String logInfoStr = gson.toJson(logInfo);
                XLog.i(logInfoStr);
            }
        };
        logTimer.schedule(logTimerTask, 0, 500);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(false);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle("后台运行中");
        builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_baseline_battery_alert_24));
        builder.setSmallIcon(R.drawable.ic_baseline_battery_alert_24);
        builder.setWhen(System.currentTimeMillis());

        Notification notification = builder.build();
        startForeground(FOREGROUND_ID, notification);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            if (!intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) return;

            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
            String technologyStr = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            level = level * 100 / scale;
            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

            String isChargingStr = isCharging ? "充电状态：正在充电" : "充电状态：未在充电";
            String chargeTypeStr = "充电类型：null";
            if (usbCharge) chargeTypeStr = "充电类型：USB充电";
            else if (acCharge) chargeTypeStr = "充电类型：交流充电";
            String healthStr = null;
            if (health == BatteryManager.BATTERY_HEALTH_UNKNOWN) healthStr = "未知";
            else if (health == BatteryManager.BATTERY_HEALTH_GOOD) healthStr = "良好";
            else if (health == BatteryManager.BATTERY_HEALTH_OVERHEAT) healthStr = "过热";
            else if (health == BatteryManager.BATTERY_HEALTH_DEAD) healthStr = "糟糕";
            else if (health == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE) healthStr = "电压过高";
            else if (health == BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE) healthStr = "未知错误";
            else if (health == BatteryManager.BATTERY_HEALTH_COLD) healthStr = "过冷";
            mainCard = new ChargeState(isChargingStr, chargeTypeStr, "健康状况： " + healthStr, "电池技术： " + technologyStr);

            cardInfo.get(MainActivity.INDEX_LEVEL).setValue(String.valueOf(level));
            cardInfo.get(MainActivity.INDEX_TEMPERATURE).setValue(String.valueOf((float)temperature / 10));
            cardInfo.get(MainActivity.INDEX_VOLTAGE).setValue(String.valueOf((float)voltage / 1000));

            sendBroadcast();
        }
    };

    void sendBroadcast() {
        Intent intent = new Intent();
        ArrayList<String> cardInfoStr = new ArrayList<>();
        for (Information card : cardInfo)
            cardInfoStr.add(card.getValue());
        intent.setAction(MainActivity.ACTION_CHARGE_BROADCAST);
        intent.putExtra(MainActivity.MAIN_CARD_EXTRA, mainCard);
        intent.putStringArrayListExtra(MainActivity.CARD_INFO_EXTRA, cardInfoStr);
        sendBroadcast(intent);
    }
}
