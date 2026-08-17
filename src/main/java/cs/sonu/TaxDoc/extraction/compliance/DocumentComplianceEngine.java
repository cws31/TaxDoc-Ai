package cs.sonu.TaxDoc.extraction.compliance;

import cs.sonu.TaxDoc.document.entity.DocumentType;

public interface DocumentComplianceEngine<T> {
    DocumentType getSupportedDocumentType();

    ComplianceReport evaluate(T extractionResult);

    record ComplianceReport(
            double complianceScore,
            cs.sonu.TaxDoc.extraction.entity.ExtractedFieldStatus assignedStatus,
            String auditTrail) {
    }
}