package cs.sonu.TaxDoc.classification.service;

import cs.sonu.TaxDoc.classification.ai.AiClient;
import cs.sonu.TaxDoc.classification.entity.ClassificationResult;
import cs.sonu.TaxDoc.document.entity.Document;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class AiClassificationService
        implements ClassificationService {

    private final AiClient aiClient;

    public AiClassificationService(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    @Override
    public ClassificationResult classify(Document document) {

        Path documentPath = Path.of(document.getStoragePath());

        return aiClient.classify(documentPath);
    }
}