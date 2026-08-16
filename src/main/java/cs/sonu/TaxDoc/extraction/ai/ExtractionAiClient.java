package cs.sonu.TaxDoc.extraction.ai;

import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;

import java.nio.file.Path;

public interface ExtractionAiClient {
    W2ExtractionResult extractW2Data(Path documentPath);
}