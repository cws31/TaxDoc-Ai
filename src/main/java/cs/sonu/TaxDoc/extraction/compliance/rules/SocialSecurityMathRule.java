package cs.sonu.TaxDoc.extraction.compliance.rules;

import cs.sonu.TaxDoc.extraction.compliance.TaxDocumentComplianceRule;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
import org.springframework.stereotype.Component;

@Component
public class SocialSecurityMathRule implements TaxDocumentComplianceRule<W2ExtractionResult> {

    @Override
    public RuleEvaluationResult evaluate(W2ExtractionResult result) {
        if (result.box3SocialSecurityWages() != null && result.box3SocialSecurityWages().value() != null
                && result.box4SocialSecurityTaxWithheld() != null
                && result.box4SocialSecurityTaxWithheld().value() != null) {

            double wages = result.box3SocialSecurityWages().value();
            double tax = result.box4SocialSecurityTaxWithheld().value();
            double expectedTax = wages * 0.062;

            if (Math.abs(tax - expectedTax) > 5.00) {
                return new RuleEvaluationResult(
                        "Social Security Tax Math Rule (Boxes 3 & 4)",
                        false,
                        0.15,
                        String.format("SS Tax mismatch. Expected ~%.2f, Got %.2f", expectedTax, tax));
            }
        }
        return new RuleEvaluationResult("Social Security Tax Math Rule (Boxes 3 & 4)", true, 0.0, "Pass");
    }
}