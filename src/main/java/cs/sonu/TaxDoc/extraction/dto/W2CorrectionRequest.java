package cs.sonu.TaxDoc.extraction.dto;

import java.util.Map;

public record W2CorrectionRequest(
        String reviewerNotes,
        Map<String, String> correctedFields) {
}