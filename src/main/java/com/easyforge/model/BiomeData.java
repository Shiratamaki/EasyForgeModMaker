package com.easyforge.model;

public class BiomeData {
    private String id;
    private String displayName;
    private String precipitation = "RAIN";
    private float temperature = 0.5f;
    private float downfall = 0.5f;
    private int waterColor = 0x3F76E4;
    private int waterFogColor = 0x050533;
    private int fogColor = 0xC0D8FF;
    private int grassColor = 0x91BD59;
    private int foliageColor = 0x77AB2F;

    // Getters and Setters (省略，请自行生成)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPrecipitation() { return precipitation; }
    public void setPrecipitation(String precipitation) { this.precipitation = precipitation; }
    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }
    public float getDownfall() { return downfall; }
    public void setDownfall(float downfall) { this.downfall = downfall; }
    public int getWaterColor() { return waterColor; }
    public void setWaterColor(int waterColor) { this.waterColor = waterColor; }
    public int getWaterFogColor() { return waterFogColor; }
    public void setWaterFogColor(int waterFogColor) { this.waterFogColor = waterFogColor; }
    public int getFogColor() { return fogColor; }
    public void setFogColor(int fogColor) { this.fogColor = fogColor; }
    public int getGrassColor() { return grassColor; }
    public void setGrassColor(int grassColor) { this.grassColor = grassColor; }
    public int getFoliageColor() { return foliageColor; }
    public void setFoliageColor(int foliageColor) { this.foliageColor = foliageColor; }
}