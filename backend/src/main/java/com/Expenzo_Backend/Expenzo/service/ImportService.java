package com.Expenzo_Backend.Expenzo.service;

import com.Expenzo_Backend.Expenzo.dto.ImportResult;
import com.Expenzo_Backend.Expenzo.dto.TransactionRequest;
import com.Expenzo_Backend.Expenzo.model.BankAccount;
import com.Expenzo_Backend.Expenzo.model.TransactionType;
import com.Expenzo_Backend.Expenzo.model.User;
import com.Expenzo_Backend.Expenzo.repository.BankAccountRepository;
import com.Expenzo_Backend.Expenzo.repository.CategoryRepository;
import com.Expenzo_Backend.Expenzo.repository.TransactionRepository;
import com.Expenzo_Backend.Expenzo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionService transactionService;


    public ImportResult importFile(MultipartFile file, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String fileName = file.getOriginalFilename();
        if (fileName == null) throw new RuntimeException("Invalid File");

        String lower = fileName.toLowerCase();

        try(InputStream inputStream = file.getInputStream();) {


            if (lower.endsWith("csv")) {
                return importCsv(inputStream, user);
            } else if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
                return importExcel(inputStream, user);
            } else if (lower.endsWith(".pdf")) {
                return importPdf(inputStream, user);
            } else {
                throw new RuntimeException("Unsupported file type: " + fileName);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse file: " + e.getMessage(), e);
        }

    }

    private ImportResult importCsv(InputStream is, User user) throws Exception {
        List<ImportRow> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            CSVReader csv = new CSVReaderBuilder(reader).withSkipLines(1).build();
            String[] row;
            int rowNum = 1;
            while ((row = csv.readNext()) != null) {
                rowNum++;
                rows.add(new ImportRow(rowNum, row));
            }
        }

        return processRows(rows, user);
    }

    // ─── Excel ───────────────────────────────────────────────
    private ImportResult importExcel(InputStream is, User user) throws Exception {
        List<ImportRow> rows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String[] cells = new String[8];
                for (int j = 0; j < 8; j++) {
                    Cell c = row.getCell(j);
                    cells[j] = c == null ? "" : fmt.formatCellValue(c);
                }
                rows.add(new ImportRow(i + 1, cells));
            }
        }

        return processRows(rows, user);
    }

    private ImportResult importPdf(InputStream is, User user) throws Exception {
        List<ImportRow> rows = new ArrayList<>();

        byte[] bytes = is.readAllBytes();
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            String text = new PDFTextStripper().getText(doc);
            String[] lines = text.split("\\r?\\n");
            int rowNum = 0;
            for (String line : lines) {
                rowNum++;
                if (line.isBlank() || line.toLowerCase().contains("description")) continue;
                String[] parts = line.split("\\s{2,}|,");
                if (parts.length < 4) continue;
                rows.add(new ImportRow(rowNum, parts));
            }
        }

        return processRows(rows, user);
    }

    // ─── Process parsed rows ─────────────────────────────────
    private ImportResult processRows(List<ImportRow> rows, User user) {
        ImportResult result = new ImportResult(rows.size());

        for (ImportRow row : rows) {
            try {
                TransactionRequest req = buildRequest(row.cells, user);
                transactionService.addTransaction(req, user.getEmail());
                result.incrementImported();
            } catch (Exception e) {
                result.addError("Row " + row.rowNum + ": " + e.getMessage());
            }
        }

        return result;
    }

    // ─── Build TransactionRequest from raw cells ─────────────
    /**
     * Column order:
     *   0: amount                (required)
     *   1: description           (required)
     *   2: date                  (required)
     *   3: type CREDIT/DEBIT     (required)
     *   4: categoryName          (optional)
     *   5: bankAccountName OR ID (optional)
     *   6: currency              (optional, default INR)
     *   7: recurring             (optional, default false)
     */
    private TransactionRequest buildRequest(String[] cells, User user) {
        if (cells.length < 4) {
            throw new RuntimeException("Need at least 4 columns (amount, description, date, type)");
        }

        TransactionRequest req = new TransactionRequest();

        // ── Amount ──
        String amountStr = cells[0].replaceAll("[^0-9.-]", "").trim();
        if (amountStr.isEmpty()) throw new RuntimeException("Missing amount");
        req.setAmount(new BigDecimal(amountStr));

        // ── Description ──
        req.setDescription(cells[1].trim());

        // ── Date ──
        req.setDate(parseDate(cells[2].trim()).atStartOfDay());

        // ── Type ──
        try {
            req.setTransactionType(TransactionType.valueOf(cells[3].trim().toUpperCase()));
        } catch (Exception e) {
            throw new RuntimeException("Invalid type (expected CREDIT or DEBIT): " + cells[3]);
        }

        // ── Category (by name) ──
        if (cells.length > 4 && !cells[4].isBlank()) {
            String catName = cells[4].trim();
            categoryRepository.findByUserId(user.getId()).stream()
                    .filter(c -> c.getName().equalsIgnoreCase(catName))
                    .findFirst()
                    .ifPresentOrElse(
                            c -> req.setCategoryId(c.getId()),
                            () -> { /* category not found → leave null → uncategorized */ }
                    );
        }

        // ── Bank Account (by name, fallback to id) ──
        if (cells.length > 5 && !cells[5].isBlank()) {
            String bankRef = cells[5].trim();
            BankAccount bank = resolveBankAccount(bankRef, user);
            if (bank == null) {
                throw new RuntimeException("Bank account not found: " + bankRef);
            }
            req.setBankAccountId(bank.getId());
        } else {
            // No bank account specified → use the user's first bank account (if any)
            List<BankAccount> accounts = (List<BankAccount>) bankAccountRepository.findByUserId(user.getId());
            if (accounts.isEmpty()) {
                throw new RuntimeException("No bank accounts linked. Add a bank account first.");
            }
            req.setBankAccountId(accounts.get(0).getId());
        }

        // ── Currency ──
        req.setCurrency(cells.length > 6 && !cells[6].isBlank() ? cells[6].trim() : "INR");

        // ── Recurring ──
        req.setIsRecurring(cells.length > 7 && Boolean.parseBoolean(cells[7].trim()));

        return req;
    }

    // ─── Resolve bank account by name or numeric ID ──────────
    private BankAccount resolveBankAccount(String ref, User user) {
        // Try by name first (case-insensitive)
        for (BankAccount a : bankAccountRepository.findByUserId(user.getId())) {
            if (a.getBankName() != null && a.getBankName().equalsIgnoreCase(ref)) {
                return a;
            }
        }
        // Fallback: try as numeric ID
        try {
            Long id = Long.parseLong(ref);
            return bankAccountRepository.findById(id)
                    .filter(a -> a.getUser() != null
                            && a.getUser().getId().equals(user.getId()))
                    .orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ─── Date parser ─────────────────────────────────────────
    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return LocalDate.now();
        String[] patterns = {"yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy", "dd-MM-yyyy", "dd-MMM-yyyy"};
        for (String p : patterns) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern(p));
            } catch (Exception ignored) {}
        }
        return LocalDate.now();
    }

    // ─── Helper record ───────────────────────────────────────
    private record ImportRow(int rowNum, String[] cells) {}

}
