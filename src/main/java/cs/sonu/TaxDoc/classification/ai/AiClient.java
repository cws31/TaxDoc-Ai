package cs.sonu.TaxDoc.classification.ai;

import cs.sonu.TaxDoc.classification.entity.ClassificationResult;

import java.nio.file.Path;

public interface AiClient {

    ClassificationResult classify(Path documentPath);
}