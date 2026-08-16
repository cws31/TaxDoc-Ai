package cs.sonu.TaxDoc.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record AiW2ExtractionResponse(
        @JsonProperty("employerName") AiExtractedField<String> employerName,
        @JsonProperty("employerEin") AiExtractedField<String> employerEin,
        @JsonProperty("employeeSsn") AiExtractedField<String> employeeSsn,
        @JsonProperty("box1Wages") AiExtractedField<BigDecimal> box1Wages,
        @JsonProperty("box2FederalTaxWithheld") AiExtractedField<BigDecimal> box2FederalTaxWithheld) {
}