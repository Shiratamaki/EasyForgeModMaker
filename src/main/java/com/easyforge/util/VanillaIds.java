package com.easyforge.util;

import java.util.*;

/**
 * 原版 Minecraft ID 数据提供者
 */
public class VanillaIds {

    // 原版物品 ID 列表（带中文名）
    public static final List<IdEntry> ITEMS = new ArrayList<>();
    // 原版方块 ID 列表
    public static final List<IdEntry> BLOCKS = new ArrayList<>();
    // 原版实体 ID 列表
    public static final List<IdEntry> ENTITIES = new ArrayList<>();
    // 原版附魔 ID 列表
    public static final List<IdEntry> ENCHANTMENTS = new ArrayList<>();
    // 原版生物群系 ID 列表
    public static final List<IdEntry> BIOMES = new ArrayList<>();

    static {
        // 初始化物品列表
        addItem("minecraft:apple", "苹果");
        addItem("minecraft:golden_apple", "金苹果");
        addItem("minecraft:enchanted_golden_apple", "附魔金苹果");
        addItem("minecraft:bread", "面包");
        addItem("minecraft:cooked_beef", "熟牛排");
        addItem("minecraft:cooked_porkchop", "熟猪排");
        addItem("minecraft:cooked_chicken", "熟鸡肉");
        addItem("minecraft:cooked_mutton", "熟羊肉");
        addItem("minecraft:cooked_rabbit", "熟兔肉");
        addItem("minecraft:cooked_cod", "熟鳕鱼");
        addItem("minecraft:cooked_salmon", "熟鲑鱼");
        addItem("minecraft:iron_ingot", "铁锭");
        addItem("minecraft:gold_ingot", "金锭");
        addItem("minecraft:diamond", "钻石");
        addItem("minecraft:emerald", "绿宝石");
        addItem("minecraft:lapis_lazuli", "青金石");
        addItem("minecraft:redstone", "红石");
        addItem("minecraft:quartz", "石英");
        addItem("minecraft:netherite_ingot", "下界合金锭");
        addItem("minecraft:stick", "木棍");
        addItem("minecraft:feather", "羽毛");
        addItem("minecraft:leather", "皮革");
        addItem("minecraft:ender_pearl", "末影珍珠");
        addItem("minecraft:blaze_rod", "烈焰棒");
        addItem("minecraft:slime_ball", "粘液球");
        addItem("minecraft:clay_ball", "粘土");
        addItem("minecraft:brick", "红砖");
        addItem("minecraft:nether_brick", "下界砖");
        addItem("minecraft:prismarine_shard", "海晶碎片");
        addItem("minecraft:prismarine_crystals", "海晶砂粒");

        // 工具
        addItem("minecraft:wooden_sword", "木剑");
        addItem("minecraft:stone_sword", "石剑");
        addItem("minecraft:iron_sword", "铁剑");
        addItem("minecraft:golden_sword", "金剑");
        addItem("minecraft:diamond_sword", "钻石剑");
        addItem("minecraft:netherite_sword", "下界合金剑");
        addItem("minecraft:wooden_pickaxe", "木镐");
        addItem("minecraft:stone_pickaxe", "石镐");
        addItem("minecraft:iron_pickaxe", "铁镐");
        addItem("minecraft:golden_pickaxe", "金镐");
        addItem("minecraft:diamond_pickaxe", "钻石镐");
        addItem("minecraft:netherite_pickaxe", "下界合金镐");
        addItem("minecraft:wooden_axe", "木斧");
        addItem("minecraft:stone_axe", "石斧");
        addItem("minecraft:iron_axe", "铁斧");
        addItem("minecraft:golden_axe", "金斧");
        addItem("minecraft:diamond_axe", "钻石斧");
        addItem("minecraft:netherite_axe", "下界合金斧");
        addItem("minecraft:wooden_shovel", "木锹");
        addItem("minecraft:stone_shovel", "石锹");
        addItem("minecraft:iron_shovel", "铁锹");
        addItem("minecraft:golden_shovel", "金锹");
        addItem("minecraft:diamond_shovel", "钻石锹");
        addItem("minecraft:netherite_shovel", "下界合金锹");
        addItem("minecraft:wooden_hoe", "木锄");
        addItem("minecraft:stone_hoe", "石锄");
        addItem("minecraft:iron_hoe", "铁锄");
        addItem("minecraft:golden_hoe", "金锄");
        addItem("minecraft:diamond_hoe", "钻石锄");
        addItem("minecraft:netherite_hoe", "下界合金锄");

        // 盔甲
        addItem("minecraft:leather_helmet", "皮革头盔");
        addItem("minecraft:leather_chestplate", "皮革胸甲");
        addItem("minecraft:leather_leggings", "皮革护腿");
        addItem("minecraft:leather_boots", "皮革靴子");
        addItem("minecraft:iron_helmet", "铁头盔");
        addItem("minecraft:iron_chestplate", "铁胸甲");
        addItem("minecraft:iron_leggings", "铁护腿");
        addItem("minecraft:iron_boots", "铁靴子");
        addItem("minecraft:diamond_helmet", "钻石头盔");
        addItem("minecraft:diamond_chestplate", "钻石胸甲");
        addItem("minecraft:diamond_leggings", "钻石护腿");
        addItem("minecraft:diamond_boots", "钻石靴子");
        addItem("minecraft:netherite_helmet", "下界合金头盔");
        addItem("minecraft:netherite_chestplate", "下界合金胸甲");
        addItem("minecraft:netherite_leggings", "下界合金护腿");
        addItem("minecraft:netherite_boots", "下界合金靴子");

        // 初始化方块列表
        addBlock("minecraft:stone", "石头");
        addBlock("minecraft:cobblestone", "圆石");
        addBlock("minecraft:dirt", "泥土");
        addBlock("minecraft:grass_block", "草方块");
        addBlock("minecraft:sand", "沙子");
        addBlock("minecraft:gravel", "沙砾");
        addBlock("minecraft:glass", "玻璃");
        addBlock("minecraft:obsidian", "黑曜石");
        addBlock("minecraft:oak_planks", "橡木木板");
        addBlock("minecraft:spruce_planks", "云杉木板");
        addBlock("minecraft:birch_planks", "白桦木板");
        addBlock("minecraft:jungle_planks", "丛林木板");
        addBlock("minecraft:acacia_planks", "金合欢木板");
        addBlock("minecraft:dark_oak_planks", "深色橡木木板");
        addBlock("minecraft:coal_ore", "煤矿石");
        addBlock("minecraft:iron_ore", "铁矿石");
        addBlock("minecraft:gold_ore", "金矿石");
        addBlock("minecraft:diamond_ore", "钻石矿石");
        addBlock("minecraft:emerald_ore", "绿宝石矿石");
        addBlock("minecraft:lapis_ore", "青金石矿石");
        addBlock("minecraft:redstone_ore", "红石矿石");
        addBlock("minecraft:nether_quartz_ore", "下界石英矿石");
        addBlock("minecraft:nether_gold_ore", "下界金矿石");
        addBlock("minecraft:ancient_debris", "远古残骸");

        // 实体列表
        addEntity("minecraft:zombie", "僵尸");
        addEntity("minecraft:skeleton", "骷髅");
        addEntity("minecraft:creeper", "苦力怕");
        addEntity("minecraft:spider", "蜘蛛");
        addEntity("minecraft:enderman", "末影人");
        addEntity("minecraft:witch", "女巫");
        addEntity("minecraft:villager", "村民");
        addEntity("minecraft:cow", "牛");
        addEntity("minecraft:pig", "猪");
        addEntity("minecraft:chicken", "鸡");
        addEntity("minecraft:sheep", "羊");
        addEntity("minecraft:rabbit", "兔子");
        addEntity("minecraft:wolf", "狼");
        addEntity("minecraft:cat", "猫");
        addEntity("minecraft:horse", "马");
        addEntity("minecraft:iron_golem", "铁傀儡");
        addEntity("minecraft:snow_golem", "雪傀儡");

        // 附魔列表
        addEnchantment("minecraft:protection", "保护");
        addEnchantment("minecraft:fire_protection", "火焰保护");
        addEnchantment("minecraft:blast_protection", "爆炸保护");
        addEnchantment("minecraft:projectile_protection", "弹射物保护");
        addEnchantment("minecraft:feather_falling", "摔落保护");
        addEnchantment("minecraft:respiration", "水下呼吸");
        addEnchantment("minecraft:aqua_affinity", "水下速掘");
        addEnchantment("minecraft:sharpness", "锋利");
        addEnchantment("minecraft:smite", "亡灵杀手");
        addEnchantment("minecraft:bane_of_arthropods", "节肢杀手");
        addEnchantment("minecraft:knockback", "击退");
        addEnchantment("minecraft:fire_aspect", "火焰附加");
        addEnchantment("minecraft:sweeping", "横扫之刃");
        addEnchantment("minecraft:efficiency", "效率");
        addEnchantment("minecraft:silk_touch", "精准采集");
        addEnchantment("minecraft:unbreaking", "耐久");
        addEnchantment("minecraft:fortune", "时运");
        addEnchantment("minecraft:power", "力量");
        addEnchantment("minecraft:punch", "冲击");
        addEnchantment("minecraft:flame", "火矢");
        addEnchantment("minecraft:infinity", "无限");
        addEnchantment("minecraft:luck_of_the_sea", "海之眷顾");
        addEnchantment("minecraft:lure", "钓饵");
        addEnchantment("minecraft:looting", "抢夺");
        addEnchantment("minecraft:thorns", "荆棘");
        addEnchantment("minecraft:mending", "经验修补");

        // 生物群系列表
        addBiome("minecraft:plains", "平原");
        addBiome("minecraft:desert", "沙漠");
        addBiome("minecraft:forest", "森林");
        addBiome("minecraft:taiga", "针叶林");
        addBiome("minecraft:swamp", "沼泽");
        addBiome("minecraft:jungle", "丛林");
        addBiome("minecraft:badlands", "恶地");
        addBiome("minecraft:savanna", "热带草原");
        addBiome("minecraft:ice_spikes", "冰刺之地");
        addBiome("minecraft:ocean", "海洋");
        addBiome("minecraft:deep_ocean", "深海");
        addBiome("minecraft:river", "河流");
        addBiome("minecraft:beach", "沙滩");
        addBiome("minecraft:mountains", "山地");
        addBiome("minecraft:snowy_plains", "雪原");
        addBiome("minecraft:nether_wastes", "下界荒地");
        addBiome("minecraft:crimson_forest", "绯红森林");
        addBiome("minecraft:warped_forest", "诡异森林");
        addBiome("minecraft:soul_sand_valley", "灵魂沙峡谷");
        addBiome("minecraft:basalt_deltas", "玄武岩三角洲");
        addBiome("minecraft:the_end", "末地");
        addBiome("minecraft:small_end_islands", "末地小型岛屿");
        addBiome("minecraft:end_midlands", "末地内陆");
        addBiome("minecraft:end_highlands", "末地高地");
        addBiome("minecraft:end_barrens", "末地荒地");
    }

    private static void addItem(String id, String chinese) {
        ITEMS.add(new IdEntry(id, chinese));
    }

    private static void addBlock(String id, String chinese) {
        BLOCKS.add(new IdEntry(id, chinese));
    }

    private static void addEntity(String id, String chinese) {
        ENTITIES.add(new IdEntry(id, chinese));
    }

    private static void addEnchantment(String id, String chinese) {
        ENCHANTMENTS.add(new IdEntry(id, chinese));
    }

    private static void addBiome(String id, String chinese) {
        BIOMES.add(new IdEntry(id, chinese));
    }

    /**
     * ID 条目，包含完整 ID 和中文显示名
     */
    public static class IdEntry {
        private final String id;
        private final String chinese;

        public IdEntry(String id, String chinese) {
            this.id = id;
            this.chinese = chinese;
        }

        public String getId() { return id; }
        public String getChinese() { return chinese; }
        public String getDisplay() { return id + " (" + chinese + ")"; }

        @Override
        public String toString() {
            return getDisplay();
        }
    }
}