package cs.sonu.TaxDoc.extraction.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class GeminiExtractionAiClient implements ExtractionAiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiExtractionAiClient.class);

    private final Client genaiClient;
    private final String modelName;
    private final ObjectMapper objectMapper;

    public GeminiExtractionAiClient(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String modelName,
            ObjectMapper objectMapper) {
        this.genaiClient = Client.builder().apiKey(apiKey).build();
        this.modelName = modelName;
        this.objectMapper = objectMapper;
    }

    @Override
    @Retryable(retryFor = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2.0))
    public <T> T extractDocumentData(Path documentPath, String prompt, Class<T> responseType) {
        try {
            byte[] fileBytes = Files.readAllBytes(documentPath);
            String mimeType = determineMimeType(documentPath);

            Content content = Content.fromParts(
                    Part.fromBytes(fileBytes, mimeType),
                    Part.fromText(prompt));

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(0.0f)
                    .responseMimeType("application/json")
                    .build();

            GenerateContentResponse response = genaiClient.models.generateContent(modelName, content, config);
            String rawJsonResponse = cleanJsonResponse(response.text());

            return objectMapper.readValue(rawJsonResponse, responseType);

        } catch (Exception e) {
            log.error("Failed to extract data from document: {}", documentPath, e);
            throw new RuntimeException("Document extraction failed: " + e.getMessage(), e);
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

    private String determineMimeType(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        if (filename.endsWith(".pdf"))
            return "application/pdf";
        if (filename.endsWith(".png"))
            return "image/png";
        return "image/jpeg";
    }
}