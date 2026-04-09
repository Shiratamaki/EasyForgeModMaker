package com.easyforge.model;

public class EnchantmentData {
    private String id;
    private String displayName;
    private String category = "BREAKABLE"; // 适用物品类型: BREAKABLE, WEAPON, ARMOR, ARMOR_HEAD, ARMOR_CHEST, ARMOR_LEGS, ARMOR_FEET, DIGGER, FISHING_ROD, TRIDENT, BOW, CROSSBOW, VANISHABLE
    private int maxLevel = 5;
    private int minLevel = 1;
    private int rarity = 1; // 0-3: COMMON=0, UNCOMMON=1, RARE=2, VERY_RARE=3
    private boolean isTreasure = false;
    private boolean isCurse = false;
    private boolean isTradeable = true;
    private boolean isDiscoverable = true;
    private int minCostBase = 1;
    private int minCostPerLevel = 10;
    private int maxCostBase = 5;
    private int maxCostPerLevel = 10;
    private String description = "";

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getMaxLevel() { return maxLevel; }
    public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }
    public int getMinLevel() { return minLevel; }
    public void setMinLevel(int minLevel) { this.minLevel = minLevel; }
    public int getRarity() { return rarity; }
    public void setRarity(int rarity) { this.rarity = rarity; }
    public boolean isTreasure() { return isTreasure; }
    public void setTreasure(boolean treasure) { isTreasure = treasure; }
    public boolean isCurse() { return isCurse; }
    public void setCurse(boolean curse) { isCurse = curse; }
    public boolean isTradeable() { return isTradeable; }
    public void setTradeable(boolean tradeable) { isTradeable = tradeable; }
    public boolean isDiscoverable() { return isDiscoverable; }
    public void setDiscoverable(boolean discoverable) { isDiscoverable = discoverable; }
    public int getMinCostBase() { return minCostBase; }
    public void setMinCostBase(int minCostBase) { this.minCostBase = minCostBase; }
    public int getMinCostPerLevel() { return minCostPerLevel; }
    public void setMinCostPerLevel(int minCostPerLevel) { this.minCostPerLevel = minCostPerLevel; }
    public int getMaxCostBase() { return maxCostBase; }
    public void setMaxCostBase(int maxCostBase) { this.maxCostBase = maxCostBase; }
    public int getMaxCostPerLevel() { return maxCostPerLevel; }
    public void setMaxCostPerLevel(int maxCostPerLevel) { this.maxCostPerLevel = maxCostPerLevel; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}