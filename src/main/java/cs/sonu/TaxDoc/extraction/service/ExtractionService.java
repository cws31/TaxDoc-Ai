package cs.sonu.TaxDoc.extraction.service;

import cs.sonu.TaxDoc.document.entity.Document;
import java.util.List;
import java.util.UUID;

public interface ExtractionService {
    Object extractDocument(Document document);

    List<Object> extractBatchDocuments(List<UUID> documentIds);
}