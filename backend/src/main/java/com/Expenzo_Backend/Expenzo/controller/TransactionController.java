package com.Expenzo_Backend.Expenzo.controller;

import com.Expenzo_Backend.Expenzo.dto.ImportResult;
import com.Expenzo_Backend.Expenzo.dto.TransactionRequest;
import com.Expenzo_Backend.Expenzo.dto.TransactionResponse;
import com.Expenzo_Backend.Expenzo.dto.TransactionUpdateRequest;
import com.Expenzo_Backend.Expenzo.service.ImportService;
import com.Expenzo_Backend.Expenzo.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final ImportService importService;

    public TransactionController(TransactionService transactionService, ImportService importService) {
        this.transactionService = transactionService;
        this.importService = importService;
    }


    @PostMapping("/add-transaction")
    public TransactionResponse addTransaction(
            @RequestBody TransactionRequest request,
            Principal principal) {
        return transactionService.addTransaction(request, principal.getName());
    }

    @PutMapping("/update-transaction/{id}")
    public TransactionResponse updateTransaction(
            @PathVariable Long id,
            @RequestBody TransactionUpdateRequest request,
            Principal principal) {
        return transactionService.updateTransaction(id, request, principal.getName());
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransactionById(
            @PathVariable Long id,
            Principal principal) {
        return transactionService.getTransactionById(id, principal.getName());
    }

    @GetMapping("/all-transactions")
    public List<TransactionResponse> getAllTransactions(Principal principal) {
        return transactionService.getAllTransactions(principal.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(
            @PathVariable Long id,
            Principal principal) {
        transactionService.deleteTransaction(id, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ResponseEntity<ImportResult> importTransactions(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        ImportResult result = importService.importFile(file, principal.getName());
        return ResponseEntity.ok(result);
    }
}
