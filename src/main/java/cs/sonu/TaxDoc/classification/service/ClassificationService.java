package cs.sonu.TaxDoc.classification.service;

import cs.sonu.TaxDoc.document.entity.Document;

public interface ClassificationService {
    Document classify(Document document);
}