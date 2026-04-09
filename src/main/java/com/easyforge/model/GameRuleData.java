package com.easyforge.model;

public class GameRuleData {
    private String id;
    private String type = "boolean"; // boolean, int, float
    private boolean defaultValue = true;
    private int defaultInt = 0;
    private float defaultFloat = 0.0f;
    private String description = "";

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isDefaultValue() { return defaultValue; }
    public void setDefaultValue(boolean defaultValue) { this.defaultValue = defaultValue; }
    public int getDefaultInt() { return defaultInt; }
    public void setDefaultInt(int defaultInt) { this.defaultInt = defaultInt; }
    public float getDefaultFloat() { return defaultFloat; }
    public void setDefaultFloat(float defaultFloat) { this.defaultFloat = defaultFloat; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}