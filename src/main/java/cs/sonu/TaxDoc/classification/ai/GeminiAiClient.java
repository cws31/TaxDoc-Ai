package cs.sonu.TaxDoc.classification.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import cs.sonu.TaxDoc.common.exception.AiProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class GeminiAiClient implements AiClient {

        private static final Logger log = LoggerFactory.getLogger(GeminiAiClient.class);

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
        @Retryable(retryFor = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2.0))
        public <T> T classifyDocumentData(Path documentPath, String prompt, Class<T> responseType) {
                try {
                        byte[] docBytes = Files.readAllBytes(documentPath);
                        String mimeType = probeMimeType(documentPath);

                        Content content = Content.fromParts(
                                        Part.fromText(prompt),
                                        Part.fromBytes(docBytes, mimeType));

                        GenerateContentConfig config = GenerateContentConfig.builder()
                                        .temperature(0.0f)
                                        .responseMimeType("application/json")
                                        .build();

                        GenerateContentResponse response = client.models.generateContent(model, content, config);
                        String rawJsonResponse = cleanJsonResponse(response.text());

                        return objectMapper.readValue(rawJsonResponse, responseType);

                } catch (IOException e) {
                        log.error("AI classification failure for file: {}", documentPath, e);
                        throw new AiProcessingException("AI classification failure for file: " + documentPath, e);
                }
        }

        private String cleanJsonResponse(String rawResponse) {
                if (rawResponse == null)
                        return "{}";
                String cleaned = rawResponse.trim();
                if (cleaned.startsWith("```json")) {
                        cleaned = cleaned.substring(7);
                }
                if (cleaned.startsWith("```")) {
                        cleaned = cleaned.substring(3);
                }
                if (cleaned.endsWith("```")) {
                        cleaned = cleaned.substring(0, cleaned.length() - 3);
                }
                return cleaned.trim();
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