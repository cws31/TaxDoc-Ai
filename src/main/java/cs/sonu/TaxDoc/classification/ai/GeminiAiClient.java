package cs.sonu.TaxDoc.classification.ai;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import cs.sonu.TaxDoc.classification.entity.ClassificationResult;
import cs.sonu.TaxDoc.document.entity.DocumentType;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class GeminiAiClient implements AiClient {

    private final Client client;
    private final String model;

    public GeminiAiClient(
            Client client,
            @Value("${gemini.model}") String model) {

        this.client = client;
        this.model = model;
    }

    @Override
    public ClassificationResult classify(Path documentPath) {

        try {

            byte[] pdfBytes = Files.readAllBytes(documentPath);

            String prompt = """
                    You are a tax document classification system.

                    Classify this document into exactly one of:

                    W2
                    UNKNOWN

                    Rules:
                    1. Return W2 only if the document clearly appears to be
                       a United States W-2 tax form.
                    2. If the document is unclear or is another type of
                       document, return UNKNOWN.
                    3. Never guess.
                    4. Confidence must be between 0 and 1.

                    Return ONLY valid JSON:

                    {
                      "documentType": "W2 or UNKNOWN",
                      "confidence": 0.0
                    }
                    """;

            Content content = Content.fromParts(
                    Part.fromText(prompt),
                    Part.fromBytes(pdfBytes, "application/pdf"));

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(0.0f)
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    model,
                    content,
                    config);

            String result = response.text();

            System.out.println("Gemini response: " + result);

            return parseResult(result);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not read document for AI classification",
                    e);
        }
    }

    private ClassificationResult parseResult(String json) {

        // Temporary parser.
        // We will replace this with proper structured output.

        String documentType = extractValue(json, "documentType");

        String confidence = extractValue(json, "confidence");

        DocumentType type = DocumentType.valueOf(documentType);

        double score = Double.parseDouble(confidence);

        if (score < 0 || score > 1) {
            throw new IllegalArgumentException(
                    "AI returned invalid confidence: " + score);
        }

        return new ClassificationResult(type, score);
    }

    private String extractValue(
            String json,
            String field) {

        String search = "\"" + field + "\"";

        int fieldIndex = json.indexOf(search);

        if (fieldIndex == -1) {
            throw new IllegalArgumentException(
                    "AI response missing field: " + field);
        }

        int colonIndex = json.indexOf(":", fieldIndex);

        int commaIndex = json.indexOf(",", colonIndex);

        int endIndex = commaIndex == -1
                ? json.indexOf("}", colonIndex)
                : commaIndex;

        String value = json.substring(
                colonIndex + 1,
                endIndex).trim();

        return value
                .replace("\"", "")
                .trim();
    }

    public W2ExtractionResult extractW2(Path filePath) throws IOException {

        byte[] pdfBytes = Files.readAllBytes(filePath);

        String prompt = """
                You are a tax document data extraction system.

                Extract information from this United States W-2 tax form.

                Return ONLY valid JSON.

                The JSON must have exactly these fields:

                {
                  "employerName": {
                    "value": "string or null",
                    "confidence": 0.0,
                    "sourceSnippet": "string or null"
                  },
                  "employerEin": {
                    "value": "string or null",
                    "confidence": 0.0,
                    "sourceSnippet": "string or null"
                  },
                  "employeeSsn": {
                    "value": "string or null",
                    "confidence": 0.0,
                    "sourceSnippet": "string or null"
                  },
                  "box1Wages": {
                    "value": "string or null",
                    "confidence": 0.0,
                    "sourceSnippet": "string or null"
                  },
                  "box2FederalTaxWithheld": {
                    "value": "string or null",
                    "confidence": 0.0,
                    "sourceSnippet": "string or null"
                  },
                  "box3SocialSecurityWages": {
                    "value": "string or null",
                    "confidence": 0.0,
                    "sourceSnippet": "string or null"
                  },
                  "box5MedicareWages": {
                    "value": "string or null",
                    "confidence": 0.0,
                    "sourceSnippet": "string or null"
                  },
                  "state": {
                    "value": "string or null",
                    "confidence": 0.0,
                    "sourceSnippet": "string or null"
                  },
                  "box17StateIncomeTax": {
                    "value": "string or null",
                    "confidence": 0.0,
                    "sourceSnippet": "string or null"
                  }
                }

                Rules:

                1. Do not guess values.
                2. If a field cannot be clearly identified, return null for value.
                3. Confidence must be between 0 and 1.
                4. Confidence should represent how certain you are that the
                   extracted value is correct.
                5. sourceSnippet should contain the relevant text visible on
                   the document that supports the extracted value.
                6. For box fields, make sure the value comes from the correct
                   numbered box.
                7. Do not invent information.
                8. Return JSON only. No markdown. No explanation.
                """;

        Content content = Content.fromParts(
                Part.fromText(prompt),
                Part.fromBytes(pdfBytes, "application/pdf"));

        GenerateContentConfig config = GenerateContentConfig.builder()
                .temperature(0.0f)
                .build();

        GenerateContentResponse response = client.models.generateContent(
                model,
                content,
                config);

        String result = response.text();

        System.out.println("Gemini W2 extraction response:");
        System.out.println(result);

        try {

            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readValue(
                    cleanJson(result),
                    W2ExtractionResult.class);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not parse Gemini W2 extraction response: "
                            + result,
                    e);
        }
    }

    private String cleanJson(String response) {

        String result = response.trim();

        if (result.startsWith("```json")) {
            result = result.substring(7);
        } else if (result.startsWith("```")) {
            result = result.substring(3);
        }

        if (result.endsWith("```")) {
            result = result.substring(
                    0,
                    result.length() - 3);
        }

        return result.trim();
    }
}