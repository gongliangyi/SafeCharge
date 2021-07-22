package com.example.chargemonitor;

public class LogInfo {
    public String isCharging;
    public String chargeType;
    public BatteryLog batteryLog;

    public static class BatteryLog {
        public String level;
        public String capacity;
        public String temperature;
        public String voltage;
        public String current;
        public String remainTime;

        public BatteryLog(String level, String capacity, String temperature, String voltage, String current, String remainTime) {
            this.level = level;
            this.capacity = capacity;
            this.temperature = temperature;
            this.voltage = voltage;
            this.current = current;
            this.remainTime = remainTime;
        }
    }

    public LogInfo(String isCharging, String chargeType, BatteryLog batteryLog) {
        this.isCharging = isCharging;
        this.chargeType = chargeType;
        this.batteryLog = batteryLog;
    }
}
