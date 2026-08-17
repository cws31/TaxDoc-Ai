package cs.sonu.TaxDoc.extraction.ai;

import java.nio.file.Path;

public interface ExtractionAiClient {
    <T> T extractDocumentData(Path documentPath, String prompt, Class<T> responseType);
}