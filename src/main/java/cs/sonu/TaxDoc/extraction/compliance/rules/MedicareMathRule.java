package cs.sonu.TaxDoc.extraction.compliance.rules;

import cs.sonu.TaxDoc.extraction.compliance.W2ComplianceRule;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
import org.springframework.stereotype.Component;

@Component
public class MedicareMathRule implements W2ComplianceRule {
    @Override
    public RuleEvaluationResult evaluate(W2ExtractionResult result) {
        if (result.box5MedicareWages() != null && result.box5MedicareWages().value() != null
                && result.box6MedicareTaxWithheld() != null && result.box6MedicareTaxWithheld().value() != null) {

            double wages = result.box5MedicareWages().value();
            double tax = result.box6MedicareTaxWithheld().value();
            double expectedTax = wages * 0.0145; // Standard 1.45% Medicare rate

            if (Math.abs(tax - expectedTax) > 5.00) {
                return new RuleEvaluationResult("Medicare Tax Math Rule (Boxes 5 & 6)", false, 0.15,
                        String.format("Medicare Tax mismatch. Expected ~%.2f, Got %.2f", expectedTax, tax));
            }
        }
        return new RuleEvaluationResult("Medicare Tax Math Rule (Boxes 5 & 6)", true, 0.0, "Pass");
    }
}