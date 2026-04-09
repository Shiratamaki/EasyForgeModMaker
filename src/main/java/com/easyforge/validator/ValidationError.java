package com.easyforge.validator;

public class ValidationError {
    public enum Severity { ERROR, WARNING }
    private Severity severity;
    private String category;
    private String message;
    private String suggestedFix;

    public ValidationError(Severity severity, String category, String message, String suggestedFix) {
        this.severity = severity;
        this.category = category;
        this.message = message;
        this.suggestedFix = suggestedFix;
    }

    // Getters
    public Severity getSeverity() { return severity; }
    public String getCategory() { return category; }
    public String getMessage() { return message; }
    public String getSuggestedFix() { return suggestedFix; }
}