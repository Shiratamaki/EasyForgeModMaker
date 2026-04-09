package com.easyforge.util;

import java.util.HashMap;
import java.util.Map;

public class ChineseNames {
    private static final Map<String, String> map = new HashMap<>();
    static {
        // 基础方块
        map.put("minecraft:stone", "石头");
        map.put("minecraft:cobblestone", "圆石");
        map.put("minecraft:dirt", "泥土");
        map.put("minecraft:grass_block", "草方块");
        map.put("minecraft:sand", "沙子");
        map.put("minecraft:gravel", "沙砾");
        map.put("minecraft:glass", "玻璃");
        map.put("minecraft:obsidian", "黑曜石");
        map.put("minecraft:oak_planks", "橡木木板");
        map.put("minecraft:spruce_planks", "云杉木板");
        map.put("minecraft:birch_planks", "白桦木板");
        map.put("minecraft:jungle_planks", "丛林木板");
        map.put("minecraft:acacia_planks", "金合欢木板");
        map.put("minecraft:dark_oak_planks", "深色橡木木板");
        // 矿石
        map.put("minecraft:coal_ore", "煤矿石");
        map.put("minecraft:iron_ore", "铁矿石");
        map.put("minecraft:gold_ore", "金矿石");
        map.put("minecraft:diamond_ore", "钻石矿石");
        map.put("minecraft:emerald_ore", "绿宝石矿石");
        map.put("minecraft:lapis_ore", "青金石矿石");
        map.put("minecraft:redstone_ore", "红石矿石");
        map.put("minecraft:nether_quartz_ore", "下界石英矿石");
        map.put("minecraft:nether_gold_ore", "下界金矿石");
        map.put("minecraft:ancient_debris", "远古残骸");
        // 锭/宝石
        map.put("minecraft:iron_ingot", "铁锭");
        map.put("minecraft:gold_ingot", "金锭");
        map.put("minecraft:diamond", "钻石");
        map.put("minecraft:emerald", "绿宝石");
        map.put("minecraft:lapis_lazuli", "青金石");
        map.put("minecraft:redstone", "红石");
        map.put("minecraft:quartz", "石英");
        map.put("minecraft:netherite_ingot", "下界合金锭");
        map.put("minecraft:netherite_scrap", "下界合金碎片");
        // 工具
        map.put("minecraft:wooden_sword", "木剑");
        map.put("minecraft:stone_sword", "石剑");
        map.put("minecraft:iron_sword", "铁剑");
        map.put("minecraft:golden_sword", "金剑");
        map.put("minecraft:diamond_sword", "钻石剑");
        map.put("minecraft:netherite_sword", "下界合金剑");
        map.put("minecraft:wooden_pickaxe", "木镐");
        map.put("minecraft:stone_pickaxe", "石镐");
        map.put("minecraft:iron_pickaxe", "铁镐");
        map.put("minecraft:golden_pickaxe", "金镐");
        map.put("minecraft:diamond_pickaxe", "钻石镐");
        map.put("minecraft:netherite_pickaxe", "下界合金镐");
        map.put("minecraft:wooden_axe", "木斧");
        map.put("minecraft:stone_axe", "石斧");
        map.put("minecraft:iron_axe", "铁斧");
        map.put("minecraft:golden_axe", "金斧");
        map.put("minecraft:diamond_axe", "钻石斧");
        map.put("minecraft:netherite_axe", "下界合金斧");
        map.put("minecraft:wooden_shovel", "木锹");
        map.put("minecraft:stone_shovel", "石锹");
        map.put("minecraft:iron_shovel", "铁锹");
        map.put("minecraft:golden_shovel", "金锹");
        map.put("minecraft:diamond_shovel", "钻石锹");
        map.put("minecraft:netherite_shovel", "下界合金锹");
        map.put("minecraft:wooden_hoe", "木锄");
        map.put("minecraft:stone_hoe", "石锄");
        map.put("minecraft:iron_hoe", "铁锄");
        map.put("minecraft:golden_hoe", "金锄");
        map.put("minecraft:diamond_hoe", "钻石锄");
        map.put("minecraft:netherite_hoe", "下界合金锄");
        // 食物
        map.put("minecraft:apple", "苹果");
        map.put("minecraft:golden_apple", "金苹果");
        map.put("minecraft:enchanted_golden_apple", "附魔金苹果");
        map.put("minecraft:bread", "面包");
        map.put("minecraft:cooked_beef", "熟牛排");
        map.put("minecraft:cooked_porkchop", "熟猪排");
        map.put("minecraft:cooked_chicken", "熟鸡肉");
        map.put("minecraft:cooked_mutton", "熟羊肉");
        map.put("minecraft:cooked_rabbit", "熟兔肉");
        map.put("minecraft:cooked_cod", "熟鳕鱼");
        map.put("minecraft:cooked_salmon", "熟鲑鱼");
        // 其他
        map.put("minecraft:stick", "木棍");
        map.put("minecraft:feather", "羽毛");
        map.put("minecraft:leather", "皮革");
        map.put("minecraft:ender_pearl", "末影珍珠");
        map.put("minecraft:blaze_rod", "烈焰棒");
        map.put("minecraft:ender_eye", "末影之眼");
        map.put("minecraft:slime_ball", "粘液球");
        map.put("minecraft:clay_ball", "粘土");
        map.put("minecraft:brick", "红砖");
        map.put("minecraft:nether_brick", "下界砖");
        map.put("minecraft:prismarine_shard", "海晶碎片");
        map.put("minecraft:prismarine_crystals", "海晶砂粒");
    }

    public static String getChinese(String id) {
        return map.getOrDefault(id, "");
    }
}