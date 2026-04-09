package com.easyforge.model;

public enum ArmorMaterial {
    LEATHER("leather", 5, new int[]{1, 2, 3, 1}, 15, 0.0f, 0.0f),
    CHAIN("chainmail", 15, new int[]{1, 4, 5, 2}, 12, 0.0f, 0.0f),
    IRON("iron", 15, new int[]{2, 5, 6, 2}, 9, 0.0f, 0.0f),
    GOLD("gold", 7, new int[]{1, 3, 5, 2}, 25, 0.0f, 0.0f),
    DIAMOND("diamond", 33, new int[]{3, 6, 8, 3}, 10, 2.0f, 0.0f),
    NETHERITE("netherite", 37, new int[]{3, 6, 8, 3}, 15, 3.0f, 0.1f);

    private final String name;
    private final int durabilityMultiplier;
    private final int[] slotProtections;
    private final int enchantability;
    private final float toughness;
    private final float knockbackResistance;

    ArmorMaterial(String name, int durabilityMultiplier, int[] slotProtections, int enchantability, float toughness, float knockbackResistance) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.slotProtections = slotProtections;
        this.enchantability = enchantability;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
    }

    public String getName() { return name; }
    public int getDurabilityMultiplier() { return durabilityMultiplier; }
    public int getProtectionForSlot(int slot) { return slotProtections[slot]; }
    public int getEnchantability() { return enchantability; }
    public float getToughness() { return toughness; }
    public float getKnockbackResistance() { return knockbackResistance; }
}