package cs.sonu.TaxDoc.document.controller;

import cs.sonu.TaxDoc.document.dto.DocumentResponse;
import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.service.DocumentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        Document document = documentService.uploadDocument(file);
        DocumentResponse response = DocumentResponse.from(document);
        return ResponseEntity.created(URI.create("/api/v1/documents/" + document.getId())).body(response);
    }

    @PostMapping(value = "/upload-batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<DocumentResponse>> uploadBatchDocuments(@RequestParam("files") MultipartFile[] files) {
        List<DocumentResponse> responses = documentService.uploadBatch(files)
                .stream()
                .map(DocumentResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        List<DocumentResponse> responses = documentService.getAllDocuments()
                .stream()
                .map(DocumentResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable UUID id) {
        return ResponseEntity.ok(DocumentResponse.from(documentService.getDocument(id)));
    }

    @PostMapping("/{id}/classify")
    public ResponseEntity<DocumentResponse> classifyDocument(@PathVariable UUID id) {
        Document classified = documentService.classifyDocument(id);
        return ResponseEntity.ok(DocumentResponse.from(classified));
    }

    @PostMapping("/classify-batch")
    public ResponseEntity<List<DocumentResponse>> classifyBatchDocuments(@RequestBody List<UUID> documentIds) {
        List<DocumentResponse> responses = documentService.classifyBatch(documentIds)
                .stream()
                .map(DocumentResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}