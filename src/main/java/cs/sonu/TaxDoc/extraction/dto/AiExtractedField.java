package cs.sonu.TaxDoc.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record AiExtractedField<T>(
        @JsonProperty("value") T value,
        @JsonProperty("confidence") double confidence) {
}