package com.easyforge.util;

import java.util.Arrays;
import java.util.List;

public class VanillaItems {
    // 常用原版物品/方块 ID 列表（可按需扩展）
    public static final List<String> COMMON_IDS = Arrays.asList(
            // 基础材料
            "minecraft:stone", "minecraft:cobblestone", "minecraft:dirt", "minecraft:grass_block",
            "minecraft:sand", "minecraft:gravel", "minecraft:glass", "minecraft:obsidian",
            "minecraft:oak_planks", "minecraft:spruce_planks", "minecraft:birch_planks",
            "minecraft:jungle_planks", "minecraft:acacia_planks", "minecraft:dark_oak_planks",
            // 矿石
            "minecraft:coal_ore", "minecraft:iron_ore", "minecraft:gold_ore", "minecraft:diamond_ore",
            "minecraft:emerald_ore", "minecraft:lapis_ore", "minecraft:redstone_ore", "minecraft:nether_quartz_ore",
            "minecraft:nether_gold_ore", "minecraft:ancient_debris",
            // 锭/宝石
            "minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:diamond", "minecraft:emerald",
            "minecraft:lapis_lazuli", "minecraft:redstone", "minecraft:quartz", "minecraft:netherite_ingot",
            "minecraft:netherite_scrap",
            // 工具
            "minecraft:wooden_sword", "minecraft:stone_sword", "minecraft:iron_sword", "minecraft:golden_sword", "minecraft:diamond_sword", "minecraft:netherite_sword",
            "minecraft:wooden_pickaxe", "minecraft:stone_pickaxe", "minecraft:iron_pickaxe", "minecraft:golden_pickaxe", "minecraft:diamond_pickaxe", "minecraft:netherite_pickaxe",
            "minecraft:wooden_axe", "minecraft:stone_axe", "minecraft:iron_axe", "minecraft:golden_axe", "minecraft:diamond_axe", "minecraft:netherite_axe",
            "minecraft:wooden_shovel", "minecraft:stone_shovel", "minecraft:iron_shovel", "minecraft:golden_shovel", "minecraft:diamond_shovel", "minecraft:netherite_shovel",
            "minecraft:wooden_hoe", "minecraft:stone_hoe", "minecraft:iron_hoe", "minecraft:golden_hoe", "minecraft:diamond_hoe", "minecraft:netherite_hoe",
            // 食物
            "minecraft:apple", "minecraft:golden_apple", "minecraft:enchanted_golden_apple", "minecraft:bread", "minecraft:cooked_beef", "minecraft:cooked_porkchop",
            "minecraft:cooked_chicken", "minecraft:cooked_mutton", "minecraft:cooked_rabbit", "minecraft:cooked_cod", "minecraft:cooked_salmon",
            // 其他
            "minecraft:stick", "minecraft:feather", "minecraft:leather", "minecraft:ender_pearl", "minecraft:blaze_rod", "minecraft:ender_eye",
            "minecraft:slime_ball", "minecraft:clay_ball", "minecraft:brick", "minecraft:nether_brick", "minecraft:prismarine_shard", "minecraft:prismarine_crystals"
    );
}