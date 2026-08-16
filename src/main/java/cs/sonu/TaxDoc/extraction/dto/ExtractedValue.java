package cs.sonu.TaxDoc.extraction.dto;

public record ExtractedValue<T>(
        T value,
        double confidence) {
}