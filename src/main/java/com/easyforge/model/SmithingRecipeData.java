package com.easyforge.model;

public class SmithingRecipeData {
    private String id;
    private String template;
    private String base;
    private String addition;
    private String result;
    private int count = 1;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }
    public String getBase() { return base; }
    public void setBase(String base) { this.base = base; }
    public String getAddition() { return addition; }
    public void setAddition(String addition) { this.addition = addition; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}