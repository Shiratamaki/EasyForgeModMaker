package com.easyforge.model;

import java.util.ArrayList;
import java.util.List;

public class TagData {
    private String id;           // 标签ID，如 "minecraft:planks"
    private String type;         // 标签类型: blocks, items, fluids, entity_types
    private List<String> values = new ArrayList<>(); // 物品/方块列表
    private boolean replace = false;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<String> getValues() { return values; }
    public void setValues(List<String> values) { this.values = values; }
    public boolean isReplace() { return replace; }
    public void setReplace(boolean replace) { this.replace = replace; }
}