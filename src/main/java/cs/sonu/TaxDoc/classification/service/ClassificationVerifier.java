package cs.sonu.TaxDoc.classification.service;

import cs.sonu.TaxDoc.classification.dto.ClassificationResult;
import org.springframework.stereotype.Component;

@Component
public class ClassificationVerifier {

    public boolean verifyClassificationProof(ClassificationResult result) {
        if (result == null || !"W2".equalsIgnoreCase(result.documentType())) {
            return false;
        }

        int score = 0;

        boolean hasValidHeader = result.detectedFormHeaders() != null && result.detectedFormHeaders().stream()
                .anyMatch(h -> h.toLowerCase().contains("w-2") || h.toLowerCase().contains("wage and tax"));

        if (hasValidHeader) {
            score += 40;
        }

        boolean hasValidBoxes = result.detectedKeyBoxes() != null && result.detectedKeyBoxes().size() >= 2;

        if (hasValidBoxes) {
            score += 40;
        }

        if (Boolean.TRUE.equals(result.holdsOmbSignature())) {
            score += 20;
        }

        return hasValidHeader && hasValidBoxes && (score >= 80);
    }
}