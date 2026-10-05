package com.Expenzo_Backend.Expenzo.service;

import com.Expenzo_Backend.Expenzo.model.Transaction;
import com.Expenzo_Backend.Expenzo.repository.TransactionRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Service
public class ExportService {

    private final TransactionRepository transactionRepository;

    public ExportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // ═══════════════════════════════════════════════════════════
    // CSV Export — null-safe
    // ═══════════════════════════════════════════════════════════
    public String generateTransactionCsv(Long userId) {

        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("ID,Amount,Currency,Description,Date,Category");

        for (Transaction transaction : transactions) {

            String category = (transaction.getCategory() != null
                    && transaction.getCategory().getName() != null)
                    ? transaction.getCategory().getName()
                    : "";   // ✅ null-safe

            pw.println(
                    safe(transaction.getId()) + "," +
                            safe(transaction.getAmount()) + "," +
                            safe(transaction.getCurrency()) + "," +
                            csvEscape(transaction.getDescription()) + "," +
                            safe(transaction.getDate()) + "," +
                            csvEscape(category)
            );
        }

        return sw.toString();
    }

    // ═══════════════════════════════════════════════════════════
    // PDF Export — already null-safe
    // ═══════════════════════════════════════════════════════════
    public byte[] generateTransactionPdf(Long userId) {

        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter pdfWriter = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument);

        document.add(new Paragraph("Transaction Report"));

        Table table = new Table(6);
        table.addHeaderCell("ID");
        table.addHeaderCell("Amount");
        table.addHeaderCell("Currency");
        table.addHeaderCell("Description");
        table.addHeaderCell("Date");
        table.addHeaderCell("Category");

        for (Transaction t : transactions) {

            table.addCell(t.getId() != null ? t.getId().toString() : "");
            table.addCell(t.getAmount() != null ? t.getAmount().toString() : "");
            table.addCell(t.getCurrency() != null ? t.getCurrency() : "");
            table.addCell(t.getDescription() != null ? t.getDescription() : "");
            table.addCell(t.getDate() != null ? t.getDate().toString() : "");

            if (t.getCategory() != null && t.getCategory().getName() != null) {
                table.addCell(t.getCategory().getName());
            } else {
                table.addCell("");
            }
        }

        document.add(table);
        document.close();

        return byteArrayOutputStream.toByteArray();
    }

    // ═══════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════
    /** Convert any object to string, empty if null. */
    private String safe(Object o) {
        return o != null ? o.toString() : "";
    }

    /** Escape CSV value if it contains comma, quote, or newline. */
    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}