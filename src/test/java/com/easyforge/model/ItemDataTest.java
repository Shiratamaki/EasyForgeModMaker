package com.easyforge.model;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ItemDataTest {

    @Test
    public void testSerializationAndDeserialization() {
        // 创建原始对象
        ItemData original = new ItemData();
        original.setId("ruby");
        original.setDisplayName("Ruby");
        original.setMaxStackSize(64);
        original.setType(ItemType.NORMAL);
        original.setTexturePath("assets/example_mod/textures/item/ruby.png");
        original.setDurability(0);
        original.setAttackDamage(0);
        original.setAttackSpeed(0);
        original.setArmorValue(0);
        original.setToughness(0);
        original.setKnockbackResistance(0);
        original.setToolMaterial("WOOD");
        original.setArmorMaterial("LEATHER");

        // 序列化
        Gson gson = new Gson();
        String json = gson.toJson(original);
        assertNotNull(json);
        assertTrue(json.contains("\"id\":\"ruby\""));
        assertTrue(json.contains("\"displayName\":\"Ruby\""));

        // 反序列化
        ItemData deserialized = gson.fromJson(json, ItemData.class);
        assertNotNull(deserialized);
        assertEquals(original.getId(), deserialized.getId());
        assertEquals(original.getDisplayName(), deserialized.getDisplayName());
        assertEquals(original.getMaxStackSize(), deserialized.getMaxStackSize());
        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original.getTexturePath(), deserialized.getTexturePath());
        assertEquals(original.getDurability(), deserialized.getDurability());
        assertEquals(original.getAttackDamage(), deserialized.getAttackDamage());
        assertEquals(original.getAttackSpeed(), deserialized.getAttackSpeed());
        assertEquals(original.getArmorValue(), deserialized.getArmorValue());
        assertEquals(original.getToughness(), deserialized.getToughness());
        assertEquals(original.getKnockbackResistance(), deserialized.getKnockbackResistance());
        assertEquals(original.getToolMaterial(), deserialized.getToolMaterial());
        assertEquals(original.getArmorMaterial(), deserialized.getArmorMaterial());
    }

    @Test
    public void testDefaultValues() {
        ItemData item = new ItemData();
        assertEquals(64, item.getMaxStackSize());
        assertEquals(ItemType.NORMAL, item.getType());
        assertEquals(250, item.getDurability());
        assertEquals(2.0f, item.getAttackDamage());
        assertEquals(-2.4f, item.getAttackSpeed());
        assertEquals(0, item.getArmorValue());
        assertEquals(0.0f, item.getToughness());
        assertEquals(0.0f, item.getKnockbackResistance());
        assertEquals("WOOD", item.getToolMaterial());
        assertEquals("LEATHER", item.getArmorMaterial());
    }
}