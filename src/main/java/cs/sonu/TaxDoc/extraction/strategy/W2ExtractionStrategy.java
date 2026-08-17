package cs.sonu.TaxDoc.extraction.strategy;

import cs.sonu.TaxDoc.document.entity.DocumentType;
import cs.sonu.TaxDoc.extraction.ai.ExtractionAiClient;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class W2ExtractionStrategy implements DocumentExtractionStrategy {

    private final ExtractionAiClient extractionAiClient;

    public W2ExtractionStrategy(ExtractionAiClient extractionAiClient) {
        this.extractionAiClient = extractionAiClient;
    }

    @Override
    public DocumentType getSupportedDocumentType() {
        return DocumentType.W2;
    }

    @Override
    public Object extract(String storagePath) {
        Path path = Path.of(storagePath);
        return extractionAiClient.extractDocumentData(path, getW2ExtractionPrompt(), W2ExtractionResult.class);
    }

    private String getW2ExtractionPrompt() {
        return """
                You are a strict, factual tax document optical parser.
                Your job is ONLY to extract text and numbers exactly as they physically appear on the provided W-2 document.

                CRITICAL ANTI-HALLUCINATION RULES:
                1. DO NOT guess, infer, or hallucinate values. If a box, number, or identifier is missing, blank, or illegible on the document, you MUST set its 'value' and 'rawSnippet' to null, and explain the missing evidence in 'visualEvidenceReasoning'.
                2. Extract monetary amounts strictly as numbers (e.g., 85000.00). Do not include dollar signs ($) or commas (,).
                3. Return ONLY a raw JSON object strictly matching the schema below. Do not wrap in markdown or json code fences.

                Schema:
                {
                  "employeeSsn": {"value": null, "rawSnippet": null, "detectedBoxLabel": "Box a", "fieldConfidence": 0.0, "visualEvidenceReasoning": "..."},
                  "employerEin": {"value": null, "rawSnippet": null, "detectedBoxLabel": "Box b", "fieldConfidence": 0.0, "visualEvidenceReasoning": "..."},
                  "employerName": {"value": null, "rawSnippet": null, "detectedBoxLabel": "Box c", "fieldConfidence": 0.0, "visualEvidenceReasoning": "..."},
                  "box1Wages": {"value": null, "rawSnippet": null, "detectedBoxLabel": "Box 1", "fieldConfidence": 0.0, "visualEvidenceReasoning": "..."},
                  "box2FederalTaxWithheld": {"value": null, "rawSnippet": null, "detectedBoxLabel": "Box 2", "fieldConfidence": 0.0, "visualEvidenceReasoning": "..."},
                  "box3SocialSecurityWages": {"value": null, "rawSnippet": null, "detectedBoxLabel": "Box 3", "fieldConfidence": 0.0, "visualEvidenceReasoning": "..."},
                  "box4SocialSecurityTaxWithheld": {"value": null, "rawSnippet": null, "detectedBoxLabel": "Box 4", "fieldConfidence": 0.0, "visualEvidenceReasoning": "..."},
                  "box5MedicareWages": {"value": null, "rawSnippet": null, "detectedBoxLabel": "Box 5", "fieldConfidence": 0.0, "visualEvidenceReasoning": "..."},
                  "box6MedicareTaxWithheld": {"value": null, "rawSnippet": null, "detectedBoxLabel": "Box 6", "fieldConfidence": 0.0, "visualEvidenceReasoning": "..."},
                  "box16StateWages": {"value": null, "rawSnippet": null, "detectedBoxLabel": "Box 16", "fieldConfidence": 0.0, "visualEvidenceReasoning": "..."},
                  "box17StateTaxWithheld": {"value": null, "rawSnippet": null, "detectedBoxLabel": "Box 17", "fieldConfidence": 0.0, "visualEvidenceReasoning": "..."},
                  "overallExtractionConfidence": 0.0
                }
                """;
    }
}