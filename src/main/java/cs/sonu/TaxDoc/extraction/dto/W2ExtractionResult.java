package cs.sonu.TaxDoc.extraction.dto;

import java.math.BigDecimal;

public record W2ExtractionResult(
                ExtractedValue<String> employerName,
                ExtractedValue<String> employerEin,
                ExtractedValue<String> employeeSsn,
                ExtractedValue<BigDecimal> box1Wages,
                ExtractedValue<BigDecimal> box2FederalTaxWithheld) {
}