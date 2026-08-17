package cs.sonu.TaxDoc.classification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cs.sonu.TaxDoc.classification.ai.AiClient;
import cs.sonu.TaxDoc.classification.dto.ClassificationResult;
import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.entity.DocumentStatus;
import cs.sonu.TaxDoc.document.entity.DocumentType;
import cs.sonu.TaxDoc.document.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class AiClassificationService implements ClassificationService {

    private static final Logger log = LoggerFactory.getLogger(AiClassificationService.class);

    private final AiClient aiClient;
    private final ClassificationVerifier classificationVerifier;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;

    public AiClassificationService(
            AiClient aiClient,
            ClassificationVerifier classificationVerifier,
            DocumentRepository documentRepository,
            ObjectMapper objectMapper) {
        this.aiClient = aiClient;
        this.classificationVerifier = classificationVerifier;
        this.documentRepository = documentRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Document classify(Document document) {
        Path documentPath = Path.of(document.getStoragePath());

        document.setStatus(DocumentStatus.CLASSIFYING);
        documentRepository.save(document);

        ClassificationResult rawResult = aiClient.classify(documentPath);

        boolean isVerified = classificationVerifier.verifyClassificationProof(rawResult);

        if (isVerified) {
            document.setDocType(DocumentType.W2);
            document.setIsProofVerified(true);
            document.setDocTypeConfidence(rawResult.rawConfidence());
            document.setClassificationReasoning(rawResult.classificationReasoning());
        } else {
            log.warn("Document ID {} failed visual proof check. Overriding to UNKNOWN.", document.getId());
            document.setDocType(DocumentType.UNKNOWN);
            document.setIsProofVerified(false);
            document.setDocTypeConfidence(0.0);
            document.setClassificationReasoning(
                    "Failed visual proof check: Missing mandatory W-2 headers, boxes, or OMB signature.");
        }

        try {
            document.setClassificationEvidenceJson(objectMapper.writeValueAsString(rawResult));
        } catch (JsonProcessingException e) {
            document.setClassificationEvidenceJson("{}");
        }

        document.setStatus(DocumentStatus.CLASSIFIED);
        return documentRepository.save(document);
    }

    @Override
    public List<Document> classifyBatch(List<UUID> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            throw new IllegalArgumentException("Document ID list cannot be empty");
        }

        List<Document> documents = documentRepository.findAllById(documentIds);

        return documents.parallelStream()
                .map(this::classify)
                .toList();
    }
}