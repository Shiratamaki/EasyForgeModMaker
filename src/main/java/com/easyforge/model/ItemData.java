package com.easyforge.model;

public class ItemData {
    private String id;
    private String displayName;
    private int maxStackSize;
    private String texturePath;
    private ItemType type = ItemType.NORMAL;
    private int durability = 250;        // 耐久度（仅工具/武器/盔甲有效）
    private float attackDamage = 2.0f;   // 攻击伤害（武器/工具）
    private float attackSpeed = -2.4f;   // 攻击速度（武器/工具，负值表示减速）
    private int armorValue = 0;          // 盔甲值（仅盔甲）
    private float toughness = 0.0f;      // 盔甲韧性
    private float knockbackResistance = 0.0f; // 击退抗性
    private String toolMaterial = "WOOD"; // 工具材料: WOOD, STONE, IRON, GOLD, DIAMOND, NETHERITE
    private String armorMaterial = "LEATHER"; // 盔甲材料: LEATHER, CHAIN, IRON, GOLD, DIAMOND, NETHERITE

    public ItemData() {
        this.maxStackSize = 64;
    }

    public ItemData(String id, String displayName, int maxStackSize) {
        this.id = id;
        this.displayName = displayName;
        this.maxStackSize = maxStackSize;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public int getMaxStackSize() { return maxStackSize; }
    public void setMaxStackSize(int maxStackSize) { this.maxStackSize = maxStackSize; }
    public String getTexturePath() { return texturePath; }
    public void setTexturePath(String texturePath) { this.texturePath = texturePath; }
    public ItemType getType() { return type; }
    public void setType(ItemType type) { this.type = type; }
    public int getDurability() { return durability; }
    public void setDurability(int durability) { this.durability = durability; }
    public float getAttackDamage() { return attackDamage; }
    public void setAttackDamage(float attackDamage) { this.attackDamage = attackDamage; }
    public float getAttackSpeed() { return attackSpeed; }
    public void setAttackSpeed(float attackSpeed) { this.attackSpeed = attackSpeed; }
    public int getArmorValue() { return armorValue; }
    public void setArmorValue(int armorValue) { this.armorValue = armorValue; }
    public float getToughness() { return toughness; }
    public void setToughness(float toughness) { this.toughness = toughness; }
    public float getKnockbackResistance() { return knockbackResistance; }
    public void setKnockbackResistance(float knockbackResistance) { this.knockbackResistance = knockbackResistance; }
    public String getToolMaterial() { return toolMaterial; }
    public void setToolMaterial(String toolMaterial) { this.toolMaterial = toolMaterial; }
    public String getArmorMaterial() { return armorMaterial; }
    public void setArmorMaterial(String armorMaterial) { this.armorMaterial = armorMaterial; }
}