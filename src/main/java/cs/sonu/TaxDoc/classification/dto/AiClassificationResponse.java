package cs.sonu.TaxDoc.classification.dto;

import java.util.List;

public record AiClassificationResponse(
                String documentType,
                double rawConfidence,
                List<String> detectedFormHeaders,
                List<String> detectedKeyBoxes,
                boolean holdsOmbSignature,
                String classificationReasoning) {
}