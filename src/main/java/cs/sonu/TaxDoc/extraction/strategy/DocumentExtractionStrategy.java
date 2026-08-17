package cs.sonu.TaxDoc.extraction.strategy;

import cs.sonu.TaxDoc.document.entity.DocumentType;

public interface DocumentExtractionStrategy {

    DocumentType getSupportedDocumentType();

    Object extract(String storagePath);
}