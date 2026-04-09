package com.easyforge.model;

public class WorldGenData {
    private String id;
    private String type = "overworld"; // overworld, nether, end
    private String noiseSettings = "minecraft:overworld";
    private String biomeSource = "minecraft:vanilla_layered";
    private String seed = "";

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getNoiseSettings() { return noiseSettings; }
    public void setNoiseSettings(String noiseSettings) { this.noiseSettings = noiseSettings; }
    public String getBiomeSource() { return biomeSource; }
    public void setBiomeSource(String biomeSource) { this.biomeSource = biomeSource; }
    public String getSeed() { return seed; }
    public void setSeed(String seed) { this.seed = seed; }
}