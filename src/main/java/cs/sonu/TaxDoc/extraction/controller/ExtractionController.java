package cs.sonu.TaxDoc.extraction.controller;

import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.service.DocumentService;
import cs.sonu.TaxDoc.extraction.service.ExtractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/extractions")
public class ExtractionController {

    private final ExtractionService extractionService;
    private final DocumentService documentService;

    public ExtractionController(ExtractionService extractionService, DocumentService documentService) {
        this.extractionService = extractionService;
        this.documentService = documentService;
    }

    @PostMapping("/{documentId}")
    public ResponseEntity<Object> extractSingleDocument(@PathVariable UUID documentId) {
        Document document = documentService.getDocument(documentId);
        Object result = extractionService.extractDocument(document);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Object>> extractBatchDocuments(@RequestBody List<UUID> documentIds) {
        List<Object> results = extractionService.extractBatchDocuments(documentIds);
        return ResponseEntity.ok(results);
    }
}