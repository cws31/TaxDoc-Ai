package cs.sonu.TaxDoc.classification.strategy;

import cs.sonu.TaxDoc.classification.ai.AiClient;
import cs.sonu.TaxDoc.classification.dto.ClassificationResult;
import cs.sonu.TaxDoc.document.entity.DocumentType;
import org.springframework.stereotype.Component;
import java.nio.file.Path;

@Component
public class W2ClassificationStrategy implements DocumentClassificationStrategy {

    private final AiClient aiClient;

    public W2ClassificationStrategy(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    @Override
    public DocumentType getSupportedDocumentType() {
        return DocumentType.W2;
    }

    @Override
    public ClassificationResult classify(Path documentPath) {
        String prompt = """
                You are an enterprise document auditor. Analyze this document specifically to check if it is a valid Form W-2.
                Look for: "Form W-2", "Wage and Tax Statement", and Box labels 1 through 20.

                Return JSON matching this schema:
                {
                  "documentType": "W2",
                  "rawConfidence": 0.98,
                  "detectedFormHeaders": ["Form W-2"],
                  "detectedKeyBoxes": ["Box 1", "Box 2"],
                  "holdsGovernmentSignature": true,
                  "classificationReasoning": "Found official W-2 markers."
                }
                """;

        return aiClient.classifyDocumentData(documentPath, prompt, ClassificationResult.class);
    }
}