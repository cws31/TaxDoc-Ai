package cs.sonu.TaxDoc.extraction.controller;

import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.extraction.dto.W2CorrectionRequest;
import cs.sonu.TaxDoc.extraction.service.AuditorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auditor")
public class ReviewController {

    private final AuditorService auditorService;

    public ReviewController(AuditorService auditorService) {
        this.auditorService = auditorService;
    }

    @GetMapping("/documents")
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = auditorService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Document>> getPendingReviewDocuments() {
        List<Document> pendingDocs = auditorService.getPendingReviewDocuments();
        return ResponseEntity.ok(pendingDocs);
    }

    @PostMapping("/review/{documentId}")
    public ResponseEntity<String> submitReview(@PathVariable UUID documentId,
            @RequestBody W2CorrectionRequest request) {
        try {
            auditorService.submitReview(documentId, request);
            return ResponseEntity.ok("Review submitted and document updated.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Review failed: " + e.getMessage());
        }
    }

    @PostMapping("/reject/{documentId}")
    public ResponseEntity<String> rejectDocument(
            @PathVariable UUID documentId,
            @RequestParam(required = false, defaultValue = "Unreadable or invalid document") String reason) {
        try {
            auditorService.rejectDocument(documentId, reason);
            return ResponseEntity.ok("Document has been rejected successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Rejection failed: " + e.getMessage());
        }
    }
}