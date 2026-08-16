package cs.sonu.TaxDoc.classification.ai;

import cs.sonu.TaxDoc.classification.dto.ClassificationResult;
import java.nio.file.Path;

public interface AiClient {
    /**
     * Classifies a document at the given file path and returns visual evidence
     * proof.
     */
    ClassificationResult classify(Path documentPath);
}