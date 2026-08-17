package cs.sonu.TaxDoc.extraction.compliance;

import cs.sonu.TaxDoc.document.entity.DocumentType;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
import cs.sonu.TaxDoc.extraction.entity.ExtractedFieldStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class W2ComplianceEngine implements DocumentComplianceEngine<W2ExtractionResult> {

    private static final Logger log = LoggerFactory.getLogger(W2ComplianceEngine.class);
    private static final double AUTO_APPROVAL_THRESHOLD = 0.95;

    private final List<TaxDocumentComplianceRule<W2ExtractionResult>> complianceRules;

    public W2ComplianceEngine(List<TaxDocumentComplianceRule<W2ExtractionResult>> complianceRules) {
        this.complianceRules = complianceRules;
    }

    @Override
    public DocumentType getSupportedDocumentType() {
        return DocumentType.W2;
    }

    @Override
    public ComplianceReport evaluate(W2ExtractionResult result) {
        if (result == null) {
            return new ComplianceReport(0.0, ExtractedFieldStatus.PENDING_REVIEW, "Extraction result is null.");
        }

        double finalScore = 1.0;
        StringBuilder auditLog = new StringBuilder();

        for (TaxDocumentComplianceRule<W2ExtractionResult> rule : complianceRules) {
            TaxDocumentComplianceRule.RuleEvaluationResult ruleResult = rule.evaluate(result);
            if (!ruleResult.passed()) {
                finalScore -= ruleResult.penaltyPoints();
                auditLog.append(String.format("[FAIL: %s] %s ", ruleResult.ruleName(), ruleResult.message()));
            } else {
                auditLog.append(String.format("[PASS: %s] ", ruleResult.ruleName()));
            }
        }

        finalScore = Math.max(0.0, Math.min(1.0, finalScore));

        ExtractedFieldStatus status = finalScore >= AUTO_APPROVAL_THRESHOLD
                ? ExtractedFieldStatus.AUTO_ACCEPTED
                : ExtractedFieldStatus.PENDING_REVIEW;

        log.info("W2 Modular Compliance Audit: Score={}, Status={}, Log={}", finalScore, status, auditLog);
        return new ComplianceReport(finalScore, status, auditLog.toString().trim());
    }
}