package cs.sonu.TaxDoc.classification.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import cs.sonu.TaxDoc.document.entity.DocumentType;

public record AiClassificationResponse(
        @JsonProperty("documentType") DocumentType documentType,
        @JsonProperty("confidence") BigDecimal confidence) {
}