package com.easyforge.model;

public class BlockData {
    private String id;
    private String displayName;
    private String material;           // 材料类型: WOOD, STONE, METAL, SAND, GLASS, CLAY, GRASS, PLANTS, ROCK, IRON, ANVIL, etc.
    private float hardness;            // 硬度（挖掘时间）
    private float explosionResistance; // 爆炸抗性（默认与硬度相关）
    private int lightLevel;            // 发光等级 0-15
    private boolean requiresTool;      // 是否需要正确工具才能掉落
    private String soundType;          // 声音类型: WOOD, STONE, METAL, GLASS, SAND, GRAVEL, GRASS, SNOW, LADDER, ANVIL, etc.
    private boolean isTransparent;     // 是否透明（影响渲染）
    private boolean isFullCube;        // 是否完整方块（碰撞箱）
    private boolean hasItem;           // 是否掉落物品（一般 true）
    private String texturePath;        // 纹理路径（可选，用于生成）

    public BlockData() {}

    public BlockData(String id, String displayName, String material, float hardness) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.hardness = hardness;
        this.explosionResistance = hardness * 5.0f;
        this.lightLevel = 0;
        this.requiresTool = false;
        this.soundType = "STONE";
        this.isTransparent = false;
        this.isFullCube = true;
        this.hasItem = true;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public float getHardness() { return hardness; }
    public void setHardness(float hardness) { this.hardness = hardness; }

    public float getExplosionResistance() { return explosionResistance; }
    public void setExplosionResistance(float explosionResistance) { this.explosionResistance = explosionResistance; }

    public int getLightLevel() { return lightLevel; }
    public void setLightLevel(int lightLevel) { this.lightLevel = lightLevel; }

    public boolean isRequiresTool() { return requiresTool; }
    public void setRequiresTool(boolean requiresTool) { this.requiresTool = requiresTool; }

    public String getSoundType() { return soundType; }
    public void setSoundType(String soundType) { this.soundType = soundType; }

    public boolean isTransparent() { return isTransparent; }
    public void setTransparent(boolean transparent) { isTransparent = transparent; }

    public boolean isFullCube() { return isFullCube; }
    public void setFullCube(boolean fullCube) { isFullCube = fullCube; }

    public boolean isHasItem() { return hasItem; }
    public void setHasItem(boolean hasItem) { this.hasItem = hasItem; }

    public String getTexturePath() { return texturePath; }
    public void setTexturePath(String texturePath) { this.texturePath = texturePath; }
}