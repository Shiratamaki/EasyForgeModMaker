package com.easyforge.model;

public class FluidData {
    private String id;
    private String displayName;
    private FluidType fluidType = FluidType.WATER;
    private String textureStill;   // 静态纹理路径
    private String textureFlowing; // 流动纹理路径
    private int density = 1000;    // 密度（水=1000，岩浆=3000）
    private int viscosity = 1000;  // 粘度（水=1000，岩浆=3000）
    private int temperature = 300; // 温度（开尔文）
    private boolean luminant = false; // 是否发光
    private int lightLevel = 0;    // 发光等级（0-15）
    private boolean gaseous = false; // 是否气体
    private String blockId;         // 对应的流体方块ID（自动生成）
    private String bucketId;        // 对应的桶物品ID（自动生成）

    public FluidData() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public FluidType getFluidType() { return fluidType; }
    public void setFluidType(FluidType fluidType) { this.fluidType = fluidType; }
    public String getTextureStill() { return textureStill; }
    public void setTextureStill(String textureStill) { this.textureStill = textureStill; }
    public String getTextureFlowing() { return textureFlowing; }
    public void setTextureFlowing(String textureFlowing) { this.textureFlowing = textureFlowing; }
    public int getDensity() { return density; }
    public void setDensity(int density) { this.density = density; }
    public int getViscosity() { return viscosity; }
    public void setViscosity(int viscosity) { this.viscosity = viscosity; }
    public int getTemperature() { return temperature; }
    public void setTemperature(int temperature) { this.temperature = temperature; }
    public boolean isLuminant() { return luminant; }
    public void setLuminant(boolean luminant) { this.luminant = luminant; }
    public int getLightLevel() { return lightLevel; }
    public void setLightLevel(int lightLevel) { this.lightLevel = lightLevel; }
    public boolean isGaseous() { return gaseous; }
    public void setGaseous(boolean gaseous) { this.gaseous = gaseous; }
    public String getBlockId() { return blockId; }
    public void setBlockId(String blockId) { this.blockId = blockId; }
    public String getBucketId() { return bucketId; }
    public void setBucketId(String bucketId) { this.bucketId = bucketId; }
}