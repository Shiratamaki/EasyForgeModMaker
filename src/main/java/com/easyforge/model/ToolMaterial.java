package com.easyforge.model;

public enum ToolMaterial {
    WOOD("wood", 59, 2.0f, 0.0f, 15),
    STONE("stone", 131, 4.0f, 1.0f, 5),
    IRON("iron", 250, 6.0f, 2.0f, 14),
    DIAMOND("diamond", 1561, 8.0f, 3.0f, 10),
    NETHERITE("netherite", 2031, 9.0f, 4.0f, 15),
    GOLD("gold", 32, 12.0f, 0.0f, 22);

    private final String name;
    private final int durability;
    private final float speed;
    private final float attackDamage;
    private final int enchantability;

    ToolMaterial(String name, int durability, float speed, float attackDamage, int enchantability) {
        this.name = name;
        this.durability = durability;
        this.speed = speed;
        this.attackDamage = attackDamage;
        this.enchantability = enchantability;
    }

    public String getName() { return name; }
    public int getDurability() { return durability; }
    public float getSpeed() { return speed; }
    public float getAttackDamage() { return attackDamage; }
    public int getEnchantability() { return enchantability; }
}