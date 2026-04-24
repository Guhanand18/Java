package com.bnpparibas.model;

import java.util.Collections;
import java.util.List;

public class ImportResult {

    private final int validCount;
    private final int invalidCount;
    private final List<String> invalidRecords;

    public ImportResult(int validCount, int invalidCount) {
        this(validCount, invalidCount, Collections.emptyList());
    }

    public ImportResult(int validCount, int invalidCount, List<String> invalidRecords) {
        this.validCount = validCount;
        this.invalidCount = invalidCount;
        this.invalidRecords = invalidRecords;
    }

    public int getValidCount() { return validCount; }
    public int getInvalidCount() { return invalidCount; }
    public List<String> getInvalidRecords() { return invalidRecords; }
}