package cs.sonu.TaxDoc.classification.service;

import cs.sonu.TaxDoc.classification.dto.ClassificationResult;
import cs.sonu.TaxDoc.classification.verifier.DocumentVerifierStrategy;
import cs.sonu.TaxDoc.document.entity.DocumentType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ClassificationVerifier {

    private final Map<DocumentType, DocumentVerifierStrategy> registry = new EnumMap<>(DocumentType.class);

    public ClassificationVerifier(List<DocumentVerifierStrategy> strategies) {
        for (DocumentVerifierStrategy strategy : strategies) {
            registry.put(strategy.getSupportedDocumentType(), strategy);
        }
    }

    public boolean verifyClassificationProof(ClassificationResult result) {
        if (result == null || result.documentType() == null || result.documentType().isBlank()) {
            return false;
        }

        String normalizedType = result.documentType().toUpperCase().replace("-", "").replace(" ", "_");

        DocumentType docType;
        try {
            docType = DocumentType.valueOf(normalizedType);
        } catch (IllegalArgumentException e) {
            return false;
        }

        DocumentVerifierStrategy strategy = registry.get(docType);
        if (strategy == null) {
            return false;
        }

        return strategy.verify(result);
    }
}