package cs.sonu.TaxDoc.extraction.dto;

public record ExtractedField<T>(
        T value,
        String rawSnippet,
        String detectedBoxLabel,
        double fieldConfidence,
        String visualEvidenceReasoning) {
}