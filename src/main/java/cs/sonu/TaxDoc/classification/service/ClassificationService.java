package cs.sonu.TaxDoc.classification.service;

import cs.sonu.TaxDoc.classification.entity.ClassificationResult;
import cs.sonu.TaxDoc.document.entity.Document;

public interface ClassificationService {

    ClassificationResult classify(Document document);
}