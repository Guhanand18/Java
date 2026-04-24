package com.bnpparibas.model;

import java.util.Date;

public class Employee {

    private String empId;
    private String firstName;
    private String lastName;
    private Date dob;
    private Date hireDate;
    private String department;
    private String position;
    private String email;
    private String phone;
    private String address;
    private String status;

    private String createdBy;
    private String lastModifiedBy;
    private Integer version;

    private Long importId;
    private String filename;

    // ===== Getters & Setters =====

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Date getDob() { return dob; }
    public void setDob(Date dob) { this.dob = dob; }

    public Date getHireDate() { return hireDate; }
    public void setHireDate(Date hireDate) { this.hireDate = hireDate; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Long getImportId() { return importId; }
    public void setImportId(Long importId) { this.importId = importId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    // ===== HELPER METHODS (VERY IMPORTANT FOR YOU) =====

    public boolean isNew() {
        return "NEW".equals(status);
    }

    public boolean isPendingValidation() {
        return "TO_BE_VALIDATED".equals(status);
    }

    public boolean isValidated() {
        return "VALIDATED".equals(status);
    }
}