package cs.sonu.TaxDoc.extraction.service;

import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExtractionValidator {

    private static final Logger log = LoggerFactory.getLogger(ExtractionValidator.class);

    /**
     * Cross-verifies extracted numerical values against statutory tax rules.
     */
    public boolean validateW2MathRules(W2ExtractionResult result) {
        if (result == null)
            return false;

        boolean isSocialSecurityValid = true;
        boolean isMedicareValid = true;

        // Rule 1: Social Security Tax = ~6.2% of SS Wages (Box 4 ≈ Box 3 * 0.062)
        if (result.box3SocialSecurityWages() != null && result.box3SocialSecurityWages().value() != null
                && result.box4SocialSecurityTaxWithheld() != null
                && result.box4SocialSecurityTaxWithheld().value() != null) {

            double ssWages = result.box3SocialSecurityWages().value();
            double ssTax = result.box4SocialSecurityTaxWithheld().value();
            double expectedSsTax = ssWages * 0.062;

            if (ssWages > 0 && Math.abs(ssTax - expectedSsTax) > 50.0) { // $50 tolerance threshold
                log.warn("Math Discrepancy: Box 4 SS Tax ({}) does not equal 6.2% of Box 3 SS Wages ({})", ssTax,
                        ssWages);
                isSocialSecurityValid = false;
            }
        }

        // Rule 2: Medicare Tax = ~1.45% of Medicare Wages (Box 6 ≈ Box 5 * 0.0145)
        if (result.box5MedicareWages() != null && result.box5MedicareWages().value() != null
                && result.box6MedicareTaxWithheld() != null && result.box6MedicareTaxWithheld().value() != null) {

            double medWages = result.box5MedicareWages().value();
            double medTax = result.box6MedicareTaxWithheld().value();
            double expectedMedTax = medWages * 0.0145;

            if (medWages > 0 && Math.abs(medTax - expectedMedTax) > 50.0) { // $50 tolerance threshold
                log.warn("Math Discrepancy: Box 6 Med Tax ({}) does not equal 1.45% of Box 5 Med Wages ({})", medTax,
                        medWages);
                isMedicareValid = false;
            }
        }

        return isSocialSecurityValid && isMedicareValid;
    }
}