package com.easyforge.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AIMessage {
    private final StringProperty role = new SimpleStringProperty();
    private final StringProperty content = new SimpleStringProperty();
    private final StringProperty timestamp = new SimpleStringProperty();

    public AIMessage(String role, String content) {
        this.role.set(role);
        this.content.set(content);
        this.timestamp.set(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    public String getRole() { return role.get(); }
    public StringProperty roleProperty() { return role; }
    public String getContent() { return content.get(); }
    public StringProperty contentProperty() { return content; }
    public String getTimestamp() { return timestamp.get(); }
}