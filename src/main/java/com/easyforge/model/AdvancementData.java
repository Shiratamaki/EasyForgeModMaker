package com.easyforge.model;

import java.util.ArrayList;
import java.util.List;

public class AdvancementData {
    private String id;
    private String displayName;
    private String description;
    private String icon = "minecraft:stone";
    private String parent = "minecraft:adventure/root";
    private String frame = "task"; // task, challenge, goal
    private boolean showToast = true;
    private boolean announceToChat = true;
    private boolean hidden = false;
    private String trigger = "minecraft:impossible";
    private List<String> criteria = new ArrayList<>();
    private List<String> rewards = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getParent() { return parent; }
    public void setParent(String parent) { this.parent = parent; }
    public String getFrame() { return frame; }
    public void setFrame(String frame) { this.frame = frame; }
    public boolean isShowToast() { return showToast; }
    public void setShowToast(boolean showToast) { this.showToast = showToast; }
    public boolean isAnnounceToChat() { return announceToChat; }
    public void setAnnounceToChat(boolean announceToChat) { this.announceToChat = announceToChat; }
    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public String getTrigger() { return trigger; }
    public void setTrigger(String trigger) { this.trigger = trigger; }
    public List<String> getCriteria() { return criteria; }
    public void setCriteria(List<String> criteria) { this.criteria = criteria; }
    public List<String> getRewards() { return rewards; }
    public void setRewards(List<String> rewards) { this.rewards = rewards; }
}