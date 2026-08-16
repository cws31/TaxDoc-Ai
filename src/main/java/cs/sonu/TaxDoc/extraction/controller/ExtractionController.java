package cs.sonu.TaxDoc.extraction.controller;

import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.service.DocumentService;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
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

    @PostMapping("/w2/{documentId}")
    public ResponseEntity<W2ExtractionResult> extractSingleW2(@PathVariable UUID documentId) {
        Document document = documentService.getDocument(documentId);
        W2ExtractionResult result = extractionService.extractW2Data(document);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/w2/batch")
    public ResponseEntity<List<W2ExtractionResult>> extractBatchW2(@RequestBody List<UUID> documentIds) {
        List<W2ExtractionResult> results = extractionService.extractBatchW2Data(documentIds);
        return ResponseEntity.ok(results);
    }
}