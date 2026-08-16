package cs.sonu.TaxDoc.classification.service;

import cs.sonu.TaxDoc.document.entity.Document;

import java.util.List;
import java.util.UUID;

public interface ClassificationService {

    Document classify(Document document);

    List<Document> classifyBatch(List<UUID> documentIds);
}