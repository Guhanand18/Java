package com.bnpparibas.model;

import java.util.Date;

public class Audit {

    private Long id;
    private String empId;
    private String modifiedBy;
    private Date modifiedAt;
    private int versionNumber;

    public Audit() {}

    public Audit(String empId, String modifiedBy, Date modifiedAt, int versionNumber) {
        this.empId = empId;
        this.modifiedBy = modifiedBy;
        this.modifiedAt = modifiedAt;
        this.versionNumber = versionNumber;
    }

    // ===== Getters & Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }

    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }

    public Date getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(Date modifiedAt) { this.modifiedAt = modifiedAt; }

    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }
}