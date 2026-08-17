package cs.sonu.TaxDoc.extraction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.entity.DocumentStatus;
import cs.sonu.TaxDoc.document.entity.DocumentType;
import cs.sonu.TaxDoc.document.repository.DocumentRepository;
import cs.sonu.TaxDoc.extraction.compliance.DocumentComplianceEngine;
import cs.sonu.TaxDoc.extraction.policy.ExtractionRoutingPolicy;
import cs.sonu.TaxDoc.extraction.strategy.DocumentExtractionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UnifiedExtractionService implements ExtractionService {

    private static final Logger log = LoggerFactory.getLogger(UnifiedExtractionService.class);
    private static final double REJECTION_THRESHOLD = 0.40;

    private final Map<DocumentType, DocumentExtractionStrategy> extractionRegistry = new EnumMap<>(DocumentType.class);
    private final Map<DocumentType, DocumentComplianceEngine<?>> complianceRegistry = new EnumMap<>(DocumentType.class);

    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;
    private final ExtractionRoutingPolicy extractionRoutingPolicy;

    public UnifiedExtractionService(
            List<DocumentExtractionStrategy> extractionStrategies,
            List<DocumentComplianceEngine<?>> complianceEngines,
            DocumentRepository documentRepository,
            ObjectMapper objectMapper,
            ExtractionRoutingPolicy extractionRoutingPolicy) {

        for (DocumentExtractionStrategy strategy : extractionStrategies) {
            extractionRegistry.put(strategy.getSupportedDocumentType(), strategy);
        }

        for (DocumentComplianceEngine<?> engine : complianceEngines) {
            complianceRegistry.put(engine.getSupportedDocumentType(), engine);
        }

        this.documentRepository = documentRepository;
        this.objectMapper = objectMapper;
        this.extractionRoutingPolicy = extractionRoutingPolicy;
    }

    @Override
    @Transactional
    public Object extractDocument(Document document) {
        DocumentType docType = document.getDocType();
        DocumentExtractionStrategy strategy = extractionRegistry.get(docType);
        DocumentComplianceEngine<?> complianceEngine = complianceRegistry.get(docType);

        if (strategy == null || complianceEngine == null) {
            throw new IllegalArgumentException(
                    "No extraction or compliance strategy registered for document type: " + docType);
        }

        document.setStatus(DocumentStatus.EXTRACTING);
        documentRepository.save(document);
        Object rawExtraction = strategy.extract(document.getStoragePath());

        DocumentComplianceEngine.ComplianceReport complianceReport = evaluateCompliance(complianceEngine,
                rawExtraction);

        try {
            document.setExtractedDataJson(objectMapper.writeValueAsString(rawExtraction));
            document.setDocTypeConfidence(complianceReport.complianceScore());
        } catch (JsonProcessingException e) {
            document.setExtractedDataJson("{}");
        }

        if (complianceReport.complianceScore() < REJECTION_THRESHOLD) {
            log.warn(
                    "Document ID {} failed extraction confidence threshold (Score: {}). Rejecting and removing from DB.",
                    document.getId(), complianceReport.complianceScore());

            document.setStatus(DocumentStatus.REJECTED);
            document.setErrorMessage("Rejected: Extraction confidence too low ("
                    + String.format("%.1f", complianceReport.complianceScore() * 100) + "%). Unreadable form.");

            documentRepository.delete(document);
            return rawExtraction;
        }

        extractionRoutingPolicy.applyRouting(document, complianceReport);
        documentRepository.save(document);

        return rawExtraction;
    }

    @Override
    @Transactional
    public List<Object> extractBatchDocuments(List<UUID> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            throw new IllegalArgumentException("Document ID list cannot be empty");
        }

        List<Document> documents = documentRepository.findAllById(documentIds);
        return documents.stream()
                .map(this::extractDocument)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private <T> DocumentComplianceEngine.ComplianceReport evaluateCompliance(DocumentComplianceEngine<?> engine,
            Object rawExtraction) {
        DocumentComplianceEngine<T> typedEngine = (DocumentComplianceEngine<T>) engine;
        return typedEngine.evaluate((T) rawExtraction);
    }
}