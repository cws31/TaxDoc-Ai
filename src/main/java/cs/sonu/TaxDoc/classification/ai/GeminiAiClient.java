package cs.sonu.TaxDoc.classification.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.*;
import cs.sonu.TaxDoc.classification.dto.AiClassificationResponse;
import cs.sonu.TaxDoc.classification.dto.ClassificationResult;
import cs.sonu.TaxDoc.common.exception.AiProcessingException;
import cs.sonu.TaxDoc.extraction.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class GeminiAiClient implements AiClient {

        private final Client client;
        private final String model;
        private final ObjectMapper objectMapper;

        public GeminiAiClient(Client client,
                        @Value("${gemini.model}") String model,
                        ObjectMapper objectMapper) {
                this.client = client;
                this.model = model;
                this.objectMapper = objectMapper;
        }

        @Override
        public ClassificationResult classify(Path documentPath) {
                try {
                        byte[] docBytes = Files.readAllBytes(documentPath);
                        String mimeType = probeMimeType(documentPath);

                        String prompt = """
                                        You are an enterprise document auditor. Analyze the visual and textual layout of this document.

                                        Classify this document as "W2" or "UNKNOWN".

                                        To classify as "W2", you MUST locate and extract exact visual evidence from the document:
                                        1. Form header markers (e.g., "Form W-2", "Wage and Tax Statement", "Department of the Treasury").
                                        2. Standard box labels (e.g., "Box 1", "Wages, tips, other compensation", "Social security wages").
                                        3. Official government identifiers (e.g., "OMB No. 1545-0008").

                                        Return standard JSON strictly matching this schema:
                                        {
                                          "documentType": "W2",
                                          "rawConfidence": 0.98,
                                          "detectedFormHeaders": ["Form W-2", "Wage and Tax Statement"],
                                          "detectedKeyBoxes": ["Box 1 Wages, tips, other compensation", "Box 2 Federal income tax withheld"],
                                          "holdsOidSignature": true,
                                          "classificationReasoning": "Found official IRS OMB No. 1545-0008, standard Form W-2 title, and numbered boxes 1-20."
                                        }

                                        If these key evidence items are missing or unreadable, set documentType to "UNKNOWN".
                                        """;

                        Content content = Content.fromParts(
                                        Part.fromText(prompt),
                                        Part.fromBytes(docBytes, mimeType));

                        GenerateContentConfig config = GenerateContentConfig.builder()
                                        .temperature(0.0f)
                                        .responseMimeType("application/json")
                                        .build();

                        GenerateContentResponse response = client.models.generateContent(model, content, config);

                        AiClassificationResponse parsed = objectMapper.readValue(
                                        response.text(),
                                        AiClassificationResponse.class);

                        // Construct full 6-parameter ClassificationResult DTO with evidence payload
                        return new ClassificationResult(
                                        parsed.documentType() != null ? parsed.documentType() : "UNKNOWN",
                                        parsed.rawConfidence(),
                                        parsed.detectedFormHeaders(),
                                        parsed.detectedKeyBoxes(),
                                        parsed.holdsOmbSignature(), // Fixed method call
                                        parsed.classificationReasoning());

                } catch (IOException e) {
                        throw new AiProcessingException("AI classification failure for file: " + documentPath, e);
                }
        }

        @Override
        public W2ExtractionResult extractW2(Path filePath) {
                try {
                        byte[] pdfBytes = Files.readAllBytes(filePath);
                        String mimeType = probeMimeType(filePath);

                        String prompt = """
                                        You are an expert tax extraction system.
                                        Extract fields from this W-2 form.
                                        Rules:
                                        - Do not invent values; set value to null if absent.
                                        - Numbers must be numeric representations (e.g. 12500.50).
                                        - Confidence must be between 0.0 and 1.0.
                                        Return JSON in this format:
                                        {
                                          "employerName": { "value": "ACME Corp", "confidence": 0.99 },
                                          "employerEin": { "value": "12-3456789", "confidence": 0.95 },
                                          "employeeSsn": { "value": "XXX-XX-1234", "confidence": 0.90 },
                                          "box1Wages": { "value": 50000.00, "confidence": 0.98 },
                                          "box2FederalTaxWithheld": { "value": 8500.00, "confidence": 0.98 }
                                        }
                                        """;

                        Content content = Content.fromParts(
                                        Part.fromText(prompt),
                                        Part.fromBytes(pdfBytes, mimeType));

                        GenerateContentConfig config = GenerateContentConfig.builder()
                                        .temperature(0.0f)
                                        .responseMimeType("application/json")
                                        .build();

                        GenerateContentResponse response = client.models.generateContent(model, content, config);

                        AiW2ExtractionResponse raw = objectMapper.readValue(
                                        response.text(),
                                        AiW2ExtractionResponse.class);

                        return mapToW2ExtractionResult(raw);

                } catch (IOException e) {
                        throw new AiProcessingException("Failed to extract W2 data for path: " + filePath, e);
                }
        }

        private W2ExtractionResult mapToW2ExtractionResult(AiW2ExtractionResponse raw) {
                return new W2ExtractionResult(
                                new ExtractedValue<>(raw.employerName() != null ? raw.employerName().value() : null,
                                                raw.employerName() != null ? raw.employerName().confidence() : 0.0),
                                new ExtractedValue<>(raw.employerEin() != null ? raw.employerEin().value() : null,
                                                raw.employerEin() != null ? raw.employerEin().confidence() : 0.0),
                                new ExtractedValue<>(raw.employeeSsn() != null ? raw.employeeSsn().value() : null,
                                                raw.employeeSsn() != null ? raw.employeeSsn().confidence() : 0.0),
                                new ExtractedValue<>(raw.box1Wages() != null ? raw.box1Wages().value() : null,
                                                raw.box1Wages() != null ? raw.box1Wages().confidence() : 0.0),
                                new ExtractedValue<>(
                                                raw.box2FederalTaxWithheld() != null
                                                                ? raw.box2FederalTaxWithheld().value()
                                                                : null,
                                                raw.box2FederalTaxWithheld() != null
                                                                ? raw.box2FederalTaxWithheld().confidence()
                                                                : 0.0));
        }

        private String probeMimeType(Path path) {
                try {
                        String contentType = Files.probeContentType(path);
                        return contentType != null ? contentType : "application/pdf";
                } catch (IOException e) {
                        return "application/pdf";
                }
        }
}