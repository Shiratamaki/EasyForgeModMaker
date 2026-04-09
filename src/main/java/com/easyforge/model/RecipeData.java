package com.easyforge.model;

import java.util.Map;

public class RecipeData {
    private String outputItem;
    private int outputCount;
    private boolean shaped;
    private String[] shape;
    private Map<Character, String> keys;

    public String getOutputItem() { return outputItem; }
    public void setOutputItem(String outputItem) { this.outputItem = outputItem; }
    public int getOutputCount() { return outputCount; }
    public void setOutputCount(int outputCount) { this.outputCount = outputCount; }
    public boolean isShaped() { return shaped; }
    public void setShaped(boolean shaped) { this.shaped = shaped; }
    public String[] getShape() { return shape; }
    public void setShape(String[] shape) { this.shape = shape; }
    public Map<Character, String> getKeys() { return keys; }
    public void setKeys(Map<Character, String> keys) { this.keys = keys; }
}