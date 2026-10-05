package com.Expenzo_Backend.Expenzo.controller;

import com.Expenzo_Backend.Expenzo.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/transactions/csv/{userId}")
    public ResponseEntity<String> generateTransactionCsv(@PathVariable Long userId) {

        String csv = exportService.generateTransactionCsv(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename-transactions.cv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/transactions/pdf/{userId}")
    public ResponseEntity<byte[]> exportTransactionsPdf(@PathVariable Long userId) throws Exception {

        byte[] pdfBytes = exportService.generateTransactionPdf(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachement; filename=transactions.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }



}
