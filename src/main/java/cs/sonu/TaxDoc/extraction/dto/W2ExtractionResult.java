package cs.sonu.TaxDoc.extraction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class W2ExtractionResult {
    private ExtractedValue employerName;
    private ExtractedValue employerEin;
    private ExtractedValue employeeSsn;
    private ExtractedValue box1Wages;
    private ExtractedValue box2FederalTaxWithheld;
    private ExtractedValue box3SocialSecurityWages;
    private ExtractedValue box5MedicareWages;
    private ExtractedValue state;
    private ExtractedValue box17StateIncomeTax;
}
