package cs.sonu.TaxDoc.classification.entity;

import cs.sonu.TaxDoc.document.entity.DocumentType;

public record ClassificationResult(
        DocumentType documentType,
        double confidence) {
}