package cs.sonu.TaxDoc.extraction.compliance.rules;

import cs.sonu.TaxDoc.extraction.compliance.TaxDocumentComplianceRule;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
import org.springframework.stereotype.Component;

@Component
public class StateTaxValidationRule implements TaxDocumentComplianceRule<W2ExtractionResult> {
    @Override
    public RuleEvaluationResult evaluate(W2ExtractionResult result) {
        if (result.box16StateWages() != null && result.box16StateWages().value() != null
                && result.box17StateTaxWithheld() != null && result.box17StateTaxWithheld().value() != null) {

            if (result.box17StateTaxWithheld().value() > result.box16StateWages().value()) {
                return new RuleEvaluationResult("State Tax Logic Rule (Boxes 16 & 17)", false, 0.10,
                        "State tax withheld cannot logically exceed total state wages.");
            }
        }
        return new RuleEvaluationResult("State Tax Logic Rule (Boxes 16 & 17)", true, 0.0, "Pass");
    }
}