package cs.sonu.TaxDoc.document.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.service.DocumentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("file") MultipartFile file) {

        Document document = documentService.uploadDocument(file);

        return ResponseEntity.ok(document);
    }

    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        return ResponseEntity.ok(
                documentService.getAllDocuments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocument(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                documentService.getDocument(id));
    }

    @PostMapping("/{id}/classify")
    public Document classifyDocument(@PathVariable UUID id) {

        return documentService.classifyDocument(id);
    }
}