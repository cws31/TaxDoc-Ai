package cs.sonu.TaxDoc.document.controller;

import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.service.DocumentWorkflowOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflow")
public class WorkflowController {

    private final DocumentWorkflowOrchestrator workflowOrchestrator;

    public WorkflowController(DocumentWorkflowOrchestrator workflowOrchestrator) {
        this.workflowOrchestrator = workflowOrchestrator;
    }

    @PostMapping("/process")
    public ResponseEntity<List<Document>> processDocuments(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("At least one file must be uploaded.");
        }

        List<Document> processedDocuments = workflowOrchestrator.processBatchEndToEnd(files);
        return ResponseEntity.ok(processedDocuments);
    }
}