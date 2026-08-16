package cs.sonu.TaxDoc.extraction.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
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
    public W2ExtractionResult extractW2Data(Path documentPath) {
        try {
            byte[] fileBytes = Files.readAllBytes(documentPath);
            String mimeType = determineMimeType(documentPath);

            // CORRECTED CODE:
            Content content = Content.fromParts(
                    Part.fromBytes(fileBytes, mimeType),
                    Part.fromText(getExtractionPrompt()));

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(0.0f)
                    .responseMimeType("application/json")
                    .build();

            GenerateContentResponse response = genaiClient.models.generateContent(modelName, content, config);
            String rawJsonResponse = cleanJsonResponse(response.text());

            return objectMapper.readValue(rawJsonResponse, W2ExtractionResult.class);

        } catch (Exception e) {
            log.error("Failed to extract W-2 data from document: {}", documentPath, e);
            throw new RuntimeException("W-2 extraction failed: " + e.getMessage(), e);
        }
    }

    private String getExtractionPrompt() {
        return """
                Extract all W-2 tax form fields from the document image/PDF with full visual evidence proof.
                Return ONLY a raw JSON object strictly matching this schema:

                {
                  "employeeSsn": {"value": "XXX-XX-XXXX", "rawSnippet": "...", "detectedBoxLabel": "Box a", "fieldConfidence": 0.99, "visualEvidenceReasoning": "..."},
                  "employerEin": {"value": "XX-XXXXXXX", "rawSnippet": "...", "detectedBoxLabel": "Box b", "fieldConfidence": 0.99, "visualEvidenceReasoning": "..."},
                  "employerName": {"value": "...", "rawSnippet": "...", "detectedBoxLabel": "Box c", "fieldConfidence": 0.99, "visualEvidenceReasoning": "..."},
                  "box1Wages": {"value": 0.00, "rawSnippet": "...", "detectedBoxLabel": "Box 1", "fieldConfidence": 0.99, "visualEvidenceReasoning": "..."},
                  "box2FederalTaxWithheld": {"value": 0.00, "rawSnippet": "...", "detectedBoxLabel": "Box 2", "fieldConfidence": 0.99, "visualEvidenceReasoning": "..."},
                  "box3SocialSecurityWages": {"value": 0.00, "rawSnippet": "...", "detectedBoxLabel": "Box 3", "fieldConfidence": 0.99, "visualEvidenceReasoning": "..."},
                  "box4SocialSecurityTaxWithheld": {"value": 0.00, "rawSnippet": "...", "detectedBoxLabel": "Box 4", "fieldConfidence": 0.99, "visualEvidenceReasoning": "..."},
                  "box5MedicareWages": {"value": 0.00, "rawSnippet": "...", "detectedBoxLabel": "Box 5", "fieldConfidence": 0.99, "visualEvidenceReasoning": "..."},
                  "box6MedicareTaxWithheld": {"value": 0.00, "rawSnippet": "...", "detectedBoxLabel": "Box 6", "fieldConfidence": 0.99, "visualEvidenceReasoning": "..."},
                  "box16StateWages": {"value": 0.00, "rawSnippet": "...", "detectedBoxLabel": "Box 16", "fieldConfidence": 0.99, "visualEvidenceReasoning": "..."},
                  "box17StateTaxWithheld": {"value": 0.00, "rawSnippet": "...", "detectedBoxLabel": "Box 17", "fieldConfidence": 0.99, "visualEvidenceReasoning": "..."},
                  "overallExtractionConfidence": 0.99
                }

                Rules:
                1. Every field MUST contain: value, rawSnippet, detectedBoxLabel, fieldConfidence (0.0 to 1.0), and visualEvidenceReasoning.
                2. For monetary fields, return numeric values in 'value' and exact text in 'rawSnippet'.
                3. Do not wrap in markdown quotes or ```json fences.
                """;
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