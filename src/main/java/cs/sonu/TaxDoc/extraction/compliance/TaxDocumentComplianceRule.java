package cs.sonu.TaxDoc.extraction.compliance;

public interface TaxDocumentComplianceRule<T> {
    RuleEvaluationResult evaluate(T extractionResult);

    record RuleEvaluationResult(
            String ruleName,
            boolean passed,
            double penaltyPoints,
            String message) {
    }
}