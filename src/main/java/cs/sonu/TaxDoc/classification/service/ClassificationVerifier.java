package cs.sonu.TaxDoc.classification.service;

import cs.sonu.TaxDoc.classification.dto.ClassificationResult;
import org.springframework.stereotype.Component;

@Component
public class ClassificationVerifier {

    /**
     * Independently evaluates whether the classification is proven or false based
     * on visual proof.
     */
    public boolean verifyClassificationProof(ClassificationResult result) {
        if (result == null || !"W2".equalsIgnoreCase(result.documentType())) {
            return false;
        }

        int score = 0;

        // Proof Check 1: Mandatory official header string (e.g., "Form W-2", "Wage and
        // Tax Statement")
        boolean hasValidHeader = result.detectedFormHeaders() != null && result.detectedFormHeaders().stream()
                .anyMatch(h -> h.toLowerCase().contains("w-2") || h.toLowerCase().contains("wage and tax"));

        if (hasValidHeader) {
            score += 40;
        }

        // Proof Check 2: Mandatory W-2 box grid labels (e.g., "Box 1", "Box 2")
        boolean hasValidBoxes = result.detectedKeyBoxes() != null && result.detectedKeyBoxes().size() >= 2;

        if (hasValidBoxes) {
            score += 40;
        }

        // Proof Check 3: Official IRS OMB Number / Signature (e.g., OMB No. 1545-0008)
        if (Boolean.TRUE.equals(result.holdsOmbSignature())) {
            score += 20;
        }

        // Core Rule: Must have BOTH a valid header AND standard box grid structure to
        // pass proof verification
        return hasValidHeader && hasValidBoxes && (score >= 80);
    }
}