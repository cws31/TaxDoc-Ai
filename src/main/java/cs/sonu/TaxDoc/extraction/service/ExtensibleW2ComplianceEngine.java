package cs.sonu.TaxDoc.extraction.service;

import cs.sonu.TaxDoc.extraction.compliance.W2ComplianceRule;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
import cs.sonu.TaxDoc.extraction.entity.ExtractedFieldStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExtensibleW2ComplianceEngine {

    private static final Logger log = LoggerFactory.getLogger(ExtensibleW2ComplianceEngine.class);
    private static final double AUTO_APPROVAL_THRESHOLD = 0.95;

    private final List<W2ComplianceRule> complianceRules;

    public ExtensibleW2ComplianceEngine(List<W2ComplianceRule> complianceRules) {
        this.complianceRules = complianceRules;
    }

    public ComplianceReport evaluate(W2ExtractionResult result) {
        if (result == null) {
            return new ComplianceReport(0.0, ExtractedFieldStatus.PENDING_REVIEW, "Extraction result is null.");
        }

        double finalScore = 1.0;
        StringBuilder auditLog = new StringBuilder();

        for (W2ComplianceRule rule : complianceRules) {
            W2ComplianceRule.RuleEvaluationResult ruleResult = rule.evaluate(result);
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

        log.info("Modular Compliance Audit: Score={}, Status={}, Log={}", finalScore, status, auditLog);
        return new ComplianceReport(finalScore, status, auditLog.toString().trim());
    }

    public record ComplianceReport(
            double complianceScore,
            ExtractedFieldStatus assignedStatus,
            String auditTrail) {
    }
}