package com.easyforge.model;

public class DimensionData {
    private String id;
    private String displayName;
    private String type = "OVERWORLD"; // OVERWORLD, NETHER, END
    private int seaLevel = 63;
    private boolean hasSkyLight = true;
    private boolean hasCeiling = false;
    private boolean ultrawarm = false;
    private boolean natural = true;
    private boolean piglinSafe = false;
    private boolean bedWorks = true;
    private boolean respawnAnchorWorks = false;
    private boolean hasRaids = true;
    private int monsterSpawnLightLevel = 0;
    private int monsterSpawnBlockLightLimit = 0;
    private String infiniburn = "minecraft:infiniburn_overworld";
    private String effects = "minecraft:overworld";

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getSeaLevel() { return seaLevel; }
    public void setSeaLevel(int seaLevel) { this.seaLevel = seaLevel; }
    public boolean isHasSkyLight() { return hasSkyLight; }
    public void setHasSkyLight(boolean hasSkyLight) { this.hasSkyLight = hasSkyLight; }
    public boolean isHasCeiling() { return hasCeiling; }
    public void setHasCeiling(boolean hasCeiling) { this.hasCeiling = hasCeiling; }
    public boolean isUltrawarm() { return ultrawarm; }
    public void setUltrawarm(boolean ultrawarm) { this.ultrawarm = ultrawarm; }
    public boolean isNatural() { return natural; }
    public void setNatural(boolean natural) { this.natural = natural; }
    public boolean isPiglinSafe() { return piglinSafe; }
    public void setPiglinSafe(boolean piglinSafe) { this.piglinSafe = piglinSafe; }
    public boolean isBedWorks() { return bedWorks; }
    public void setBedWorks(boolean bedWorks) { this.bedWorks = bedWorks; }
    public boolean isRespawnAnchorWorks() { return respawnAnchorWorks; }
    public void setRespawnAnchorWorks(boolean respawnAnchorWorks) { this.respawnAnchorWorks = respawnAnchorWorks; }
    public boolean isHasRaids() { return hasRaids; }
    public void setHasRaids(boolean hasRaids) { this.hasRaids = hasRaids; }
    public int getMonsterSpawnLightLevel() { return monsterSpawnLightLevel; }
    public void setMonsterSpawnLightLevel(int monsterSpawnLightLevel) { this.monsterSpawnLightLevel = monsterSpawnLightLevel; }
    public int getMonsterSpawnBlockLightLimit() { return monsterSpawnBlockLightLimit; }
    public void setMonsterSpawnBlockLightLimit(int monsterSpawnBlockLightLimit) { this.monsterSpawnBlockLightLimit = monsterSpawnBlockLightLimit; }
    public String getInfiniburn() { return infiniburn; }
    public void setInfiniburn(String infiniburn) { this.infiniburn = infiniburn; }
    public String getEffects() { return effects; }
    public void setEffects(String effects) { this.effects = effects; }
}