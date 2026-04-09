package com.easyforge.model;

public class BrewingRecipeData {
    private String id;
    private String input;      // 输入物品（如粗药水）
    private String ingredient; // 酿造材料（如地狱疣）
    private String result;     // 输出物品（如粗制药水）
    private int count = 1;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    public String getIngredient() { return ingredient; }
    public void setIngredient(String ingredient) { this.ingredient = ingredient; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}