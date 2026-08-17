package cs.sonu.TaxDoc.extraction.policy;

import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.entity.DocumentStatus;
import cs.sonu.TaxDoc.extraction.compliance.DocumentComplianceEngine.ComplianceReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ThresholdBasedExtractionRoutingPolicy implements ExtractionRoutingPolicy {

    private static final Logger log = LoggerFactory.getLogger(ThresholdBasedExtractionRoutingPolicy.class);

    private static final double AUTO_APPROVAL_THRESHOLD = 0.95;
    private static final double MIN_EXTRACTION_CONFIDENCE = 0.50;

    @Override
    public void applyRouting(Document document, ComplianceReport report) {
        double score = report.complianceScore();

        if (score >= AUTO_APPROVAL_THRESHOLD) {
            document.setStatus(DocumentStatus.EXTRACTED);
            log.info("Production Routing [AUTO_ACCEPTED]: Document {} passed with score {}", document.getId(), score);
        } else if (score >= MIN_EXTRACTION_CONFIDENCE) {
            document.setStatus(DocumentStatus.PENDING_REVIEW);
            log.warn("Production Routing [REVIEW_QUEUE]: Document {} flagged for manual audit. Score: {}",
                    document.getId(), score);
        } else {
            document.setStatus(DocumentStatus.REJECTED);
            document.setErrorMessage(String
                    .format("Rejected: Extraction confidence score (%.2f) failed enterprise safety floor.", score));
            log.error("Production Routing [INSTANT_REJECT]: Document {} halted due to critical low confidence: {}",
                    document.getId(), score);
        }
    }
}