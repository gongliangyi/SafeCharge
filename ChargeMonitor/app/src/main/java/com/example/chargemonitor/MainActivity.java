package com.example.chargemonitor;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    public static final int INDEX_LEVEL = 0;
    public static final int INDEX_CAPACITY = 1;
    public static final int INDEX_TEMPERATURE = 2;
    public static final int INDEX_VOLTAGE = 3;
    public static final int INDEX_CURRENT = 4;
    public static final int INDEX_REMAIN_TIME = 5;

    public static final String ACTION_CHARGE_BROADCAST = "com.example.broadcast.ACTION_CHARGE_BROADCAST";
    public static final String MAIN_CARD_EXTRA = "mainCard";
    public static final String CARD_INFO_EXTRA = "cardInfo";

    MainViewModel model;

    void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (checkSelfPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);
            }
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getPermission();
        getPhoneInfo();

        model = new ViewModelProvider(this).get(MainViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.information_card_list);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false));
        InformationAdapter adapter = new InformationAdapter(model.getCardInfo().getValue(), this);
        recyclerView.setAdapter(adapter);

        model.getMainCard().observe(this, chargeState -> {
            TextView textView = findViewById(R.id.is_charging);
            textView.setText(chargeState.isCharging);
            textView = findViewById(R.id.charge_type);
            textView.setText(chargeState.chargingType);
            textView = findViewById(R.id.health);
            textView.setText(chargeState.health);
            textView = findViewById(R.id.technology);
            textView.setText(chargeState.technology);
        });
        model.getCardInfo().observe(this, cardInfo -> {
            adapter.notifyDataSetChanged();
        });

        ArrayList<Information> cardInfo = model.getCardInfo().getValue();
        assert cardInfo != null;
        cardInfo.add(new Information("当前电量百分比", "%", "-1"));
        cardInfo.add(new Information("当前电量", "mAh", "-1"));
        cardInfo.add(new Information("电池温度", "℃", "-1"));
        cardInfo.add(new Information("电池电压", "V", "-1"));
        cardInfo.add(new Information("充电电流", "A", "-1"));
        cardInfo.add(new Information("充电剩余时间", "min", "-1"));

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CHARGE_BROADCAST);
        registerReceiver(broadcastReceiver, filter);

        Intent intent = new Intent(this, BatteryService.class);
        startService(intent);
    }

    private void getPhoneInfo() {
        String systemVersion = "安卓版本： Android" + Build.VERSION.RELEASE;
        String brand = "制造商： " + Build.BRAND;
        String system_model = "手机型号： " + Build.MODEL;

        TextView textView = findViewById(R.id.system_version);
        textView.setText(systemVersion);
        textView = findViewById(R.id.brand);
        textView.setText(brand);
        textView = findViewById(R.id.system_model);
        textView.setText(system_model);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            if (!intent.getAction().equals(ACTION_CHARGE_BROADCAST)) return;

            ChargeState mainCard = (ChargeState) intent.getSerializableExtra(MAIN_CARD_EXTRA);
            ArrayList<String> cardInfoStr = intent.getStringArrayListExtra(CARD_INFO_EXTRA);

            if (mainCard != null) model.setMainCard(mainCard);
            if (cardInfoStr != null) {
                ArrayList<Information> cardInfo = model.getCardInfo().getValue();
                assert cardInfo != null;
                for (int i = 0; i < cardInfoStr.size(); ++i)
                    cardInfo.get(i).setValue(cardInfoStr.get(i));
                model.setCardInfo(cardInfo);
            }
        }
    };

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(this, BatteryService.class);
        stopService(intent);
        super.onDestroy();
    }
}