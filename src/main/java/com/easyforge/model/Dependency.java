package com.easyforge.model;

public class Dependency {
    private String modId;
    private boolean mandatory;
    private String versionRange;
    private String ordering;
    private String side;

    public Dependency() {}

    public Dependency(String modId, boolean mandatory, String versionRange, String ordering, String side) {
        this.modId = modId;
        this.mandatory = mandatory;
        this.versionRange = versionRange;
        this.ordering = ordering;
        this.side = side;
    }

    // Getters and Setters
    public String getModId() { return modId; }
    public void setModId(String modId) { this.modId = modId; }
    public boolean isMandatory() { return mandatory; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }
    public String getVersionRange() { return versionRange; }
    public void setVersionRange(String versionRange) { this.versionRange = versionRange; }
    public String getOrdering() { return ordering; }
    public void setOrdering(String ordering) { this.ordering = ordering; }
    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }
}