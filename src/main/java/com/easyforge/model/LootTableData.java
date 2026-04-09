package com.easyforge.model;

import java.util.ArrayList;
import java.util.List;

public class LootTableData {
    private String id;
    private String type = "minecraft:block"; // block, entity, chest, fishing, gift, archaeology
    private List<LootPool> pools = new ArrayList<>();

    public static class LootPool {
        private int rolls = 1;
        private int bonusRolls = 0;
        private List<LootEntry> entries = new ArrayList<>();
        private List<LootCondition> conditions = new ArrayList<>();

        public int getRolls() { return rolls; }
        public void setRolls(int rolls) { this.rolls = rolls; }
        public int getBonusRolls() { return bonusRolls; }
        public void setBonusRolls(int bonusRolls) { this.bonusRolls = bonusRolls; }
        public List<LootEntry> getEntries() { return entries; }
        public void setEntries(List<LootEntry> entries) { this.entries = entries; }
        public List<LootCondition> getConditions() { return conditions; }
        public void setConditions(List<LootCondition> conditions) { this.conditions = conditions; }
    }

    public static class LootEntry {
        private String type = "minecraft:item";
        private String name;
        private int weight = 1;
        private int count = 1;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getWeight() { return weight; }
        public void setWeight(int weight) { this.weight = weight; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public static class LootCondition {
        private String condition = "minecraft:survives_explosion";

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<LootPool> getPools() { return pools; }
    public void setPools(List<LootPool> pools) { this.pools = pools; }
}