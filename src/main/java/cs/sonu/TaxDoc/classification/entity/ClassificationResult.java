package cs.sonu.TaxDoc.classification.entity;

import java.math.BigDecimal;

import cs.sonu.TaxDoc.document.entity.DocumentType;

public record ClassificationResult(
                DocumentType documentType,
                BigDecimal confidence) {
}