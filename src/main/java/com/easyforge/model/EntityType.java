package com.easyforge.model;

public enum EntityType {
    CREATURE("生物", "CreatureEntity"),
    MONSTER("怪物", "MonsterEntity"),
    WATER_CREATURE("水生生物", "WaterAnimalEntity"),
    AMBIENT("环境生物", "AmbientCreatureEntity"),
    FLYING("飞行生物", "FlyingEntity"),
    BOSS("BOSS", "MobEntity");

    private final String displayName;
    private final String className;

    EntityType(String displayName, String className) {
        this.displayName = displayName;
        this.className = className;
    }

    public String getDisplayName() { return displayName; }
    public String getClassName() { return className; }
}