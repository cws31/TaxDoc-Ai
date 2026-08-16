package cs.sonu.TaxDoc.classification.ai;

import cs.sonu.TaxDoc.classification.entity.ClassificationResult;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;

import java.nio.file.Path;

public interface AiClient {

    ClassificationResult classify(Path documentPath);

    W2ExtractionResult extractW2(Path documentPath);
}