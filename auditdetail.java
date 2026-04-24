package com.bnpparibas.model;

public class AuditDetail {

    private Long id;
    private Long auditId;
    private String attribute; // field name changed
    private String oldValue;
    private String newValue;

    public AuditDetail() {}

    public AuditDetail(Long auditId, String attribute, String oldValue, String newValue) {
        this.auditId = auditId;
        this.attribute = attribute;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    // ===== Getters & Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAuditId() { return auditId; }
    public void setAuditId(Long auditId) { this.auditId = auditId; }

    public String getAttribute() { return attribute; }
    public void setAttribute(String attribute) { this.attribute = attribute; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
}