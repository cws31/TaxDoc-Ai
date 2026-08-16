package cs.sonu.TaxDoc.extraction.service;

import cs.sonu.TaxDoc.extraction.dto.ExtractedField;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class AdvancedFieldValidator {

    private static final Pattern SSN_PATTERN = Pattern.compile("^(\\d{3}-\\d{2}-\\d{4}|XXX-XX-\\d{4})$");
    private static final Pattern EIN_PATTERN = Pattern.compile("^\\d{2}-\\d{7}$");
    private static final Pattern MONETARY_PATTERN = Pattern.compile("^\\d+(\\.\\d{2})$");

    /**
     * Grades an individual textual/identifier field with micro-parameter checks.
     */
    public FieldScore evaluateTextField(ExtractedField<String> field, Pattern strictPattern) {
        if (field == null || field.value() == null || field.value().isBlank()) {
            return new FieldScore(0.0, "Field is missing or null.");
        }

        String val = field.value().trim();
        if (strictPattern.matcher(val).matches()) {
            return new FieldScore(1.0, "Perfect match against strict formatting pattern.");
        } else {
            // Partial credit if text is present but format slightly deviates
            return new FieldScore(0.5, "Field present but failed strict pattern validation.");
        }
    }

    /**
     * Grades a monetary field based on presence and clean decimal structuring.
     */
    public FieldScore evaluateMonetaryField(ExtractedField<Double> field) {
        if (field == null || field.value() == null) {
            return new FieldScore(0.0, "Monetary field is missing.");
        }

        double val = field.value();
        if (val < 0) {
            return new FieldScore(0.0, "Monetary value cannot be negative.");
        }

        // Check if raw snippet matches two decimal places if available
        if (field.rawSnippet() != null && !field.rawSnippet().isBlank()) {
            String cleanSnippet = field.rawSnippet().replace(",", "").replace("$", "").trim();
            if (MONETARY_PATTERN.matcher(cleanSnippet).matches()) {
                return new FieldScore(1.0, "Monetary value and snippet formatting are 100% compliant.");
            }
        }

        // Valid numeric value, but snippet format was unverified
        return new FieldScore(0.9, "Valid numeric value present.");
    }

    public record FieldScore(double score, String reasoning) {
    }
}