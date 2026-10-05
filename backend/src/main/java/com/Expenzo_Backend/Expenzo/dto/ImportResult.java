package com.Expenzo_Backend.Expenzo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ImportResult {

    private int total;
    private int imported;
    private int failed;
    private List<String> errors = new ArrayList<>();

    public ImportResult(int total) {
        this.total = total;
    }

    public void incrementImported() {
        this.imported++;
    }

    public void addError(String error) {
        this.errors.add(error);
        this.failed++;
    }
}