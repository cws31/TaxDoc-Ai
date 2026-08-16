package cs.sonu.TaxDoc.extraction.controller;

import cs.sonu.TaxDoc.extraction.dto.ExtractedFieldResponse;
import cs.sonu.TaxDoc.extraction.service.ExtractionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class ExtractionController {

    private final ExtractionService extractionService;

    public ExtractionController(ExtractionService extractionService) {
        this.extractionService = extractionService;
    }

    @PostMapping("/{id}/extract")
    public ResponseEntity<Void> extract(@PathVariable UUID id) {

        extractionService.prepareForExtraction(id);

        extractionService.processExtractionAsync(id);

        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}/fields")
    public ResponseEntity<List<ExtractedFieldResponse>> getFields(@PathVariable UUID id) {
        return ResponseEntity.ok(extractionService.getFields(id));
    }
}