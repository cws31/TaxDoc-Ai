package cs.sonu.TaxDoc.classification.verifier;

import cs.sonu.TaxDoc.classification.dto.ClassificationResult;
import cs.sonu.TaxDoc.document.entity.DocumentType;

public interface DocumentVerifierStrategy {

    DocumentType getSupportedDocumentType();

    boolean verify(ClassificationResult result);
}