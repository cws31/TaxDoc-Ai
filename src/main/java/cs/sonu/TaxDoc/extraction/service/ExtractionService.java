package cs.sonu.TaxDoc.extraction.service;

import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;

import java.util.List;
import java.util.UUID;

public interface ExtractionService {

    W2ExtractionResult extractW2Data(Document document);

    List<W2ExtractionResult> extractBatchW2Data(List<UUID> documentIds);
}