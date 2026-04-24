package com.bnpparibas.model;

public class ImportStats {

    private Long id;
    private String importedBy;
    private int totalFiles;
    private int totalValidRecords;
    private int totalInvalidRecords;

    public ImportStats() {}

    public ImportStats(String importedBy, int totalFiles, int totalValidRecords, int totalInvalidRecords) {
        this.importedBy = importedBy;
        this.totalFiles = totalFiles;
        this.totalValidRecords = totalValidRecords;
        this.totalInvalidRecords = totalInvalidRecords;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImportedBy() { return importedBy; }
    public void setImportedBy(String importedBy) { this.importedBy = importedBy; }

    public int getTotalFiles() { return totalFiles; }
    public void setTotalFiles(int totalFiles) { this.totalFiles = totalFiles; }

    public int getTotalValidRecords() { return totalValidRecords; }
    public void setTotalValidRecords(int totalValidRecords) { this.totalValidRecords = totalValidRecords; }

    public int getTotalInvalidRecords() { return totalInvalidRecords; }
    public void setTotalInvalidRecords(int totalInvalidRecords) { this.totalInvalidRecords = totalInvalidRecords; }
}