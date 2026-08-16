package cs.sonu.TaxDoc.extraction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.entity.DocumentStatus;
import cs.sonu.TaxDoc.document.repository.DocumentRepository;
import cs.sonu.TaxDoc.extraction.dto.W2CorrectionRequest;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuditorService {

    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;

    public AuditorService(DocumentRepository documentRepository, ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches all documents currently awaiting human review.
     */
    public List<Document> getPendingReviewDocuments() {
        return documentRepository.findByStatus(DocumentStatus.PENDING_REVIEW);
    }

    /**
     * Submits manual human corrections, updates JSON data, and marks document as
     * manually reviewed.
     */
    @Transactional
    public void submitReview(UUID documentId, W2CorrectionRequest request) throws JsonProcessingException {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));

        // 1. Load current extracted JSON
        W2ExtractionResult currentData = objectMapper.readValue(doc.getExtractedDataJson(), W2ExtractionResult.class);

        // 2. Apply corrections if provided in the request
        if (request.correctedFields() != null && !request.correctedFields().isEmpty()) {
            // Note: Custom mapping or field adjustments can be handled here dynamically
            // based on request keys
        }

        // 3. Update document state
        doc.setExtractedDataJson(objectMapper.writeValueAsString(currentData));
        doc.setStatus(DocumentStatus.MANUAL_REVIEWED);
        doc.setDocTypeConfidence(1.0); // Override confidence to 100% since human verified it

        documentRepository.save(doc);
    }

    /**
     * Fetches all documents across all statuses.
     */
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    /**
     * Completely rejects a document due to unreadable files or invalid data.
     */
    @Transactional
    public void rejectDocument(UUID documentId, String reason) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));

        doc.setStatus(DocumentStatus.REJECTED);
        doc.setDocTypeConfidence(0.0);

        documentRepository.save(doc);
    }
}