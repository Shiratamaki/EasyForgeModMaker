package com.easyforge.model;

public enum ItemType {
    NORMAL("普通物品", "Item"),
    TOOL("工具", "ToolItem"),
    WEAPON("武器", "SwordItem"),
    PICKAXE("镐", "PickaxeItem"),
    AXE("斧", "AxeItem"),
    SHOVEL("锹", "ShovelItem"),
    HOE("锄", "HoeItem"),
    ARMOR_HELMET("头盔", "ArmorItem"),
    ARMOR_CHESTPLATE("胸甲", "ArmorItem"),
    ARMOR_LEGGINGS("护腿", "ArmorItem"),
    ARMOR_BOOTS("靴子", "ArmorItem");

    private final String displayName;
    private final String className;

    ItemType(String displayName, String className) {
        this.displayName = displayName;
        this.className = className;
    }

    public String getDisplayName() { return displayName; }
    public String getClassName() { return className; }

    public boolean isTool() {
        return this == TOOL || this == PICKAXE || this == AXE || this == SHOVEL || this == HOE;
    }

    public boolean isWeapon() {
        return this == WEAPON;
    }

    public boolean isArmor() {
        return this.name().startsWith("ARMOR_");
    }
}