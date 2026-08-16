package cs.sonu.TaxDoc.classification.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.*;
import cs.sonu.TaxDoc.classification.dto.AiClassificationResponse;
import cs.sonu.TaxDoc.classification.dto.ClassificationResult;
import cs.sonu.TaxDoc.common.exception.AiProcessingException;
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
                                          "holdsOmbSignature": true,
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

                        return new ClassificationResult(
                                        parsed.documentType() != null ? parsed.documentType() : "UNKNOWN",
                                        parsed.rawConfidence(),
                                        parsed.detectedFormHeaders(),
                                        parsed.detectedKeyBoxes(),
                                        parsed.holdsOmbSignature(),
                                        parsed.classificationReasoning());

                } catch (IOException e) {
                        throw new AiProcessingException("AI classification failure for file: " + documentPath, e);
                }
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