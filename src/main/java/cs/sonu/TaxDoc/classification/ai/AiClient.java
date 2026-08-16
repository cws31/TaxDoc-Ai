package cs.sonu.TaxDoc.classification.ai;

import cs.sonu.TaxDoc.classification.dto.ClassificationResult;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;

import java.nio.file.Path;

public interface AiClient {

    /**
     * Classifies a document at the given file path and returns visual evidence
     * proof.
     */
    ClassificationResult classify(Path documentPath);

    /**
     * Extracts structured W-2 data fields from a file path.
     */
    W2ExtractionResult extractW2(Path filePath);
}