package com.example.chargemonitor;

import java.io.Serializable;

public class ChargeState implements Serializable {
    public String isCharging;
    public String chargingType;
    public String health;
    public String technology;

    public ChargeState(String isCharging, String chargingType, String health, String technology) {
        this.isCharging = isCharging;
        this.chargingType = chargingType;
        this.health = health;
        this.technology = technology;
    }

    public void setIsCharging(String isCharging) {
        this.isCharging = isCharging;
    }

    public void setChargingType(String chargingType) {
        this.chargingType = chargingType;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }
}
