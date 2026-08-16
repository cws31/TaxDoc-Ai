package cs.sonu.TaxDoc.extraction.dto;

public record W2ExtractionResult(
                ExtractedField<String> employeeSsn,
                ExtractedField<String> employerEin,
                ExtractedField<String> employerName,
                ExtractedField<Double> box1Wages,
                ExtractedField<Double> box2FederalTaxWithheld,
                ExtractedField<Double> box3SocialSecurityWages,
                ExtractedField<Double> box4SocialSecurityTaxWithheld,
                ExtractedField<Double> box5MedicareWages,
                ExtractedField<Double> box6MedicareTaxWithheld,
                ExtractedField<Double> box16StateWages,
                ExtractedField<Double> box17StateTaxWithheld,
                double overallExtractionConfidence) {
}