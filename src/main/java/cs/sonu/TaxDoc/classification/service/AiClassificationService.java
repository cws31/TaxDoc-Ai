package cs.sonu.TaxDoc.classification.service;

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

    public AiClassificationService(
            AiClient aiClient,
            ClassificationVerifier classificationVerifier,
            DocumentRepository documentRepository) {
        this.aiClient = aiClient;
        this.classificationVerifier = classificationVerifier;
        this.documentRepository = documentRepository;
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
            String parsedType = rawResult.documentType().toUpperCase().replace("-", "").replace(" ", "_");
            try {
                document.setDocType(DocumentType.valueOf(parsedType));
            } catch (IllegalArgumentException e) {
                document.setDocType(DocumentType.UNKNOWN);
            }

            document.setStatus(DocumentStatus.CLASSIFIED);
            return documentRepository.save(document);

        } else {
            log.warn("Document ID {} failed structural classification. Rejecting and removing from DB.",
                    document.getId());

            document.setDocType(DocumentType.UNKNOWN);
            document.setStatus(DocumentStatus.REJECTED);
            document.setErrorMessage("Rejected: Unsupported document format or unrecognized form structure.");

            documentRepository.delete(document);

            return document;
        }
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