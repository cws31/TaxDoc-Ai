package cs.sonu.TaxDoc.extraction.compliance.rules;

import cs.sonu.TaxDoc.extraction.compliance.TaxDocumentComplianceRule;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
import org.springframework.stereotype.Component;

@Component
public class SsnAndEinRule implements TaxDocumentComplianceRule<W2ExtractionResult> {

    @Override
    public RuleEvaluationResult evaluate(W2ExtractionResult result) {
        boolean hasSsn = result.employeeSsn() != null && result.employeeSsn().value() != null;
        boolean hasEin = result.employerEin() != null && result.employerEin().value() != null;

        if (!hasSsn || !hasEin) {
            return new RuleEvaluationResult(
                    "Core Identifiers Rule (Box a & b)",
                    false,
                    0.35,
                    "Mandatory SSN or EIN is missing.");
        }
        return new RuleEvaluationResult("Core Identifiers Rule (Box a & b)", true, 0.0, "Pass");
    }
}