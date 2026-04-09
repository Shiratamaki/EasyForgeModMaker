package com.easyforge.model;

public class SmeltingRecipeData {
    private String id;              // 配方ID（文件名）
    private String input;           // 输入物品ID（如 minecraft:iron_ingot）
    private String output;          // 输出物品ID
    private int count = 1;          // 输出数量
    private float experience = 0.1f; // 经验值
    private int cookingTime = 200;   // 烧炼时间（刻，默认200=10秒）

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public float getExperience() { return experience; }
    public void setExperience(float experience) { this.experience = experience; }
    public int getCookingTime() { return cookingTime; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }
}