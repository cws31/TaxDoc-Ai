package cs.sonu.TaxDoc.classification.service;

import cs.sonu.TaxDoc.classification.dto.ClassificationResult;
import cs.sonu.TaxDoc.classification.strategy.DocumentClassificationStrategy;
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
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AiClassificationService implements ClassificationService {

    private static final Logger log = LoggerFactory.getLogger(AiClassificationService.class);

    private final Map<DocumentType, DocumentClassificationStrategy> strategyMap;
    private final ClassificationVerifier classificationVerifier;
    private final DocumentRepository documentRepository;

    public AiClassificationService(
            List<DocumentClassificationStrategy> strategies,
            ClassificationVerifier classificationVerifier,
            DocumentRepository documentRepository) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(DocumentClassificationStrategy::getSupportedDocumentType,
                        Function.identity()));
        this.classificationVerifier = classificationVerifier;
        this.documentRepository = documentRepository;
    }

    @Override
    @Transactional
    public Document classify(Document document) {
        Path documentPath = Path.of(document.getStoragePath());

        document.setStatus(DocumentStatus.CLASSIFYING);
        documentRepository.save(document);

        // For now, default new/unclassified documents through the W2 strategy
        // (or you can use a primary router strategy if supporting multiple unknown
        // formats at once).
        DocumentClassificationStrategy strategy = strategyMap.get(DocumentType.W2);

        if (strategy == null) {
            throw new IllegalStateException("No classification strategy found for DocumentType: W2");
        }

        ClassificationResult rawResult = strategy.classify(documentPath);
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

        return documents.stream()
                .map(this::classify)
                .toList();
    }
}