package cs.sonu.TaxDoc.extraction.dto;

import cs.sonu.TaxDoc.extraction.entity.ExtractedField;

import java.math.BigDecimal;
import java.util.UUID;

public record ExtractedFieldResponse(

        UUID id,

        String fieldName,

        String value,

        BigDecimal confidence,

        String status

) {

    public static ExtractedFieldResponse from(
            ExtractedField field) {

        return new ExtractedFieldResponse(
                field.getId(),
                field.getFieldName(),
                field.getFieldValue(),
                field.getConfidence(),
                field.getStatus().name());
    }
}