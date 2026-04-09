package com.easyforge.model;

public class StructureData {
    private String id;
    private String displayName;
    private String type = "FEATURE"; // FEATURE, CONFIGURED_FEATURE, PLACEMENT
    private String placementType = "RANDOM_SPREAD"; // RANDOM_SPREAD, RANDOM_PATCH, SURFACE
    private int rarity = 10;          // 稀有度（1-100）
    private int count = 1;            // 每个区块尝试次数
    private int spread = 7;           // 扩散范围
    private int minY = 0;             // 最小Y高度
    private int maxY = 255;           // 最大Y高度
    private String biomes = "all";    // 可生成的生物群系（all 或指定群系ID列表）
    private String structureFile;     // 结构文件路径（.nbt）

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPlacementType() { return placementType; }
    public void setPlacementType(String placementType) { this.placementType = placementType; }
    public int getRarity() { return rarity; }
    public void setRarity(int rarity) { this.rarity = rarity; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public int getSpread() { return spread; }
    public void setSpread(int spread) { this.spread = spread; }
    public int getMinY() { return minY; }
    public void setMinY(int minY) { this.minY = minY; }
    public int getMaxY() { return maxY; }
    public void setMaxY(int maxY) { this.maxY = maxY; }
    public String getBiomes() { return biomes; }
    public void setBiomes(String biomes) { this.biomes = biomes; }
    public String getStructureFile() { return structureFile; }
    public void setStructureFile(String structureFile) { this.structureFile = structureFile; }
}