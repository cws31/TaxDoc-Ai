package cs.sonu.TaxDoc.extraction.policy;

import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.extraction.compliance.DocumentComplianceEngine;

public interface ExtractionRoutingPolicy {
    void applyRouting(Document document, DocumentComplianceEngine.ComplianceReport report);
}