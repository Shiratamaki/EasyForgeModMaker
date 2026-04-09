package com.easyforge.model;

public class EntityData {
    private String id;
    private String displayName;
    private EntityType type = EntityType.CREATURE;
    private float health = 20.0f;
    private float attackDamage = 2.0f;
    private float speed = 0.25f;
    private float knockbackResistance = 0.0f;
    private int spawnWeight = 10;           // 生成权重（0=不自然生成）
    private int minGroupSize = 1;
    private int maxGroupSize = 4;
    private String spawnBiomeTypes = "PLAINS,FOREST"; // 可生成的生物群系类型
    private String texturePath;              // 纹理路径
    private String modelPath;                // 模型路径（GeoJSON或Java模型）
    private boolean isBoss = false;          // 是否为BOSS
    private boolean isTamable = false;       // 是否可驯服
    private boolean isRideable = false;      // 是否可骑乘

    public EntityData() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public EntityType getType() { return type; }
    public void setType(EntityType type) { this.type = type; }
    public float getHealth() { return health; }
    public void setHealth(float health) { this.health = health; }
    public float getAttackDamage() { return attackDamage; }
    public void setAttackDamage(float attackDamage) { this.attackDamage = attackDamage; }
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
    public float getKnockbackResistance() { return knockbackResistance; }
    public void setKnockbackResistance(float knockbackResistance) { this.knockbackResistance = knockbackResistance; }
    public int getSpawnWeight() { return spawnWeight; }
    public void setSpawnWeight(int spawnWeight) { this.spawnWeight = spawnWeight; }
    public int getMinGroupSize() { return minGroupSize; }
    public void setMinGroupSize(int minGroupSize) { this.minGroupSize = minGroupSize; }
    public int getMaxGroupSize() { return maxGroupSize; }
    public void setMaxGroupSize(int maxGroupSize) { this.maxGroupSize = maxGroupSize; }
    public String getSpawnBiomeTypes() { return spawnBiomeTypes; }
    public void setSpawnBiomeTypes(String spawnBiomeTypes) { this.spawnBiomeTypes = spawnBiomeTypes; }
    public String getTexturePath() { return texturePath; }
    public void setTexturePath(String texturePath) { this.texturePath = texturePath; }
    public String getModelPath() { return modelPath; }
    public void setModelPath(String modelPath) { this.modelPath = modelPath; }
    public boolean isBoss() { return isBoss; }
    public void setBoss(boolean boss) { isBoss = boss; }
    public boolean isTamable() { return isTamable; }
    public void setTamable(boolean tamable) { isTamable = tamable; }
    public boolean isRideable() { return isRideable; }
    public void setRideable(boolean rideable) { isRideable = rideable; }
}