package cs.sonu.TaxDoc.classification.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.*;
import cs.sonu.TaxDoc.classification.dto.AiClassificationResponse;
import cs.sonu.TaxDoc.classification.entity.ClassificationResult;
import cs.sonu.TaxDoc.extraction.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import cs.sonu.TaxDoc.common.exception.AiProcessingException;
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

                        String prompt = """
                                        You are an enterprise tax document classifier.
                                        Classify this document into strictly one of: W2, UNKNOWN.
                                        Rules:
                                        1. Return W2 only if clearly a US W-2 form.
                                        2. Return UNKNOWN if uncertain or unsupported.
                                        3. Confidence must be between 0.0 and 1.0.
                                        Return ONLY standard JSON matching this structure:
                                        {
                                          "documentType": "W2",
                                          "confidence": 0.95
                                        }
                                        """;

                        Content content = Content.fromParts(
                                        Part.fromText(prompt),
                                        Part.fromBytes(docBytes, "application/pdf"));

                        GenerateContentConfig config = GenerateContentConfig.builder()
                                        .temperature(0.0f)
                                        .responseMimeType("application/json")
                                        .build();

                        GenerateContentResponse response = client.models.generateContent(model, content, config);

                        AiClassificationResponse parsed = objectMapper.readValue(
                                        response.text(),
                                        AiClassificationResponse.class);

                        return new ClassificationResult(parsed.documentType(), parsed.confidence());

                } catch (IOException e) {
                        throw new AiProcessingException("AI classification failure for file: " + documentPath, e);
                }
        }

        public W2ExtractionResult extractW2(Path filePath) {
                try {
                        byte[] pdfBytes = Files.readAllBytes(filePath);

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
                                        Part.fromBytes(pdfBytes, "application/pdf"));

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
                                new ExtractedValue<>(raw.employerName().value(), raw.employerName().confidence()),
                                new ExtractedValue<>(raw.employerEin().value(), raw.employerEin().confidence()),
                                new ExtractedValue<>(raw.employeeSsn().value(), raw.employeeSsn().confidence()),
                                new ExtractedValue<>(raw.box1Wages().value(), raw.box1Wages().confidence()),
                                new ExtractedValue<>(raw.box2FederalTaxWithheld().value(),
                                                raw.box2FederalTaxWithheld().confidence()));
        }
}