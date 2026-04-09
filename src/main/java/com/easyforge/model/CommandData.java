package com.easyforge.model;

public class CommandData {
    private String id;
    private String name;          // 命令名称（不含/）
    private String permission = "minecraft.command."; // 权限节点
    private String description = "";
    private String executionClass; // 自定义执行类名

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getExecutionClass() { return executionClass; }
    public void setExecutionClass(String executionClass) { this.executionClass = executionClass; }
}