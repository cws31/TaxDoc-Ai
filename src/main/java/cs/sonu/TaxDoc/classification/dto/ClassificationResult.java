package cs.sonu.TaxDoc.classification.dto;

import java.util.List;

public record ClassificationResult(
        String documentType,
        double rawConfidence,
        List<String> detectedFormHeaders,
        List<String> detectedKeyBoxes,
        boolean holdsOmbSignature, // Fixed: holdsOmbSignature instead of holdsOidSignature
        String classificationReasoning) {
}