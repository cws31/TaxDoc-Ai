package cs.sonu.TaxDoc.classification.strategy;

import cs.sonu.TaxDoc.classification.dto.ClassificationResult;
import cs.sonu.TaxDoc.document.entity.DocumentType;
import java.nio.file.Path;

public interface DocumentClassificationStrategy {
    DocumentType getSupportedDocumentType();

    ClassificationResult classify(Path documentPath);
}