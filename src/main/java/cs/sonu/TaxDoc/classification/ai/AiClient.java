package cs.sonu.TaxDoc.classification.ai;

import java.nio.file.Path;

public interface AiClient {
    <T> T classifyDocumentData(Path documentPath, String prompt, Class<T> responseType);
}