package cs.sonu.TaxDoc.extraction.compliance;

import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;

public interface W2ComplianceRule {
    RuleEvaluationResult evaluate(W2ExtractionResult result);

    record RuleEvaluationResult(
            String ruleName,
            boolean passed,
            double penaltyPoints,
            String message) {
    }
}