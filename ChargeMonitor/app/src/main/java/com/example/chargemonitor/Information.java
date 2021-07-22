package com.example.chargemonitor;

import java.io.Serializable;

public class Information implements Serializable {
    private String description;
    private String unit;
    private String value;

    public Information(String description, String unit, String value) {
        this.description = description;
        this.unit = unit;
        this.value = value;
    }

    public String getDescription() { return description; }
    public String getUnit() { return unit; }
    public String getValue() { return value; }

    public void setDescription(String description) { this.description = description; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setValue(String value) { this.value = value; }

}
