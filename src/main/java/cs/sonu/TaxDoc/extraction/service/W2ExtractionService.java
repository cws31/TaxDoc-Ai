package cs.sonu.TaxDoc.extraction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.entity.DocumentStatus;
import cs.sonu.TaxDoc.document.entity.DocumentType;
import cs.sonu.TaxDoc.document.repository.DocumentRepository;
import cs.sonu.TaxDoc.extraction.ai.ExtractionAiClient;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class W2ExtractionService implements ExtractionService {

    private static final Logger log = LoggerFactory.getLogger(W2ExtractionService.class);

    private final ExtractionAiClient extractionAiClient;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;
    private final ExtensibleW2ComplianceEngine extensibleW2ComplianceEngine;

    public W2ExtractionService(
            ExtractionAiClient extractionAiClient,
            ExtensibleW2ComplianceEngine extensibleW2ComplianceEngine,
            DocumentRepository documentRepository,
            ObjectMapper objectMapper) {
        this.extractionAiClient = extractionAiClient;
        this.extensibleW2ComplianceEngine = extensibleW2ComplianceEngine;
        this.documentRepository = documentRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public W2ExtractionResult extractW2Data(Document document) {
        if (document.getDocType() != DocumentType.W2) {
            throw new IllegalArgumentException(
                    "Extraction rejected: Document ID " + document.getId() + " is not classified as W2");
        }

        document.setStatus(DocumentStatus.EXTRACTING);
        documentRepository.save(document);

        Path documentPath = Path.of(document.getStoragePath());

        // 1. Extract raw structured fields via Gemini AI (Pure optical parser)
        W2ExtractionResult rawExtraction = extractionAiClient.extractW2Data(documentPath);

        // 2. Evaluate modular compliance and confidence via Extensible Compliance
        // Engine
        ExtensibleW2ComplianceEngine.ComplianceReport complianceReport = extensibleW2ComplianceEngine
                .evaluate(rawExtraction);

        // 3. Persist evidence JSON and confidence score
        try {
            document.setExtractedDataJson(objectMapper.writeValueAsString(rawExtraction));
            document.setDocTypeConfidence(complianceReport.complianceScore());
        } catch (JsonProcessingException e) {
            document.setExtractedDataJson("{}");
        }

        // 4. Automatic Routing Logic based on Confidence Threshold (e.g., 0.95)
        double confidenceThreshold = 0.95;

        if (complianceReport.complianceScore() >= confidenceThreshold) {
            document.setStatus(DocumentStatus.EXTRACTED); // Auto-Accepted
            log.info("Document {} auto-accepted with confidence score: {}", document.getId(),
                    complianceReport.complianceScore());
        } else {
            document.setStatus(DocumentStatus.PENDING_REVIEW); // Flagged for Human Auditor
            log.warn("Document {} flagged for PENDING_REVIEW due to low confidence score: {}", document.getId(),
                    complianceReport.complianceScore());
        }

        documentRepository.save(document);

        return rawExtraction;
    }

    @Override
    @Transactional
    public List<W2ExtractionResult> extractBatchW2Data(List<UUID> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            throw new IllegalArgumentException("Document ID list cannot be empty");
        }

        List<Document> documents = documentRepository.findAllById(documentIds);

        return documents.stream()
                .map(this::extractW2Data)
                .toList();
    }
}