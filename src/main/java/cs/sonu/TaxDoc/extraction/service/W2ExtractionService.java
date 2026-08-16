package cs.sonu.TaxDoc.extraction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.entity.DocumentStatus;
import cs.sonu.TaxDoc.document.entity.DocumentType;
import cs.sonu.TaxDoc.document.repository.DocumentRepository;
import cs.sonu.TaxDoc.extraction.ai.ExtractionAiClient;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class W2ExtractionService implements ExtractionService {

    private static final Logger log = LoggerFactory.getLogger(W2ExtractionService.class);

    private final ExtractionAiClient extractionAiClient;
    private final ExtractionValidator extractionValidator;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;

    public W2ExtractionService(
            ExtractionAiClient extractionAiClient,
            ExtractionValidator extractionValidator,
            DocumentRepository documentRepository,
            ObjectMapper objectMapper) {
        this.extractionAiClient = extractionAiClient;
        this.extractionValidator = extractionValidator;
        this.documentRepository = documentRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public W2ExtractionResult extractW2Data(Document document) {
        if (document.getDocType() != DocumentType.W2) {
            throw new IllegalArgumentException(
                    "Extraction rejected: Document ID " + document.getId() + " is not classified as W2");
        }

        document.setStatus(DocumentStatus.EXTRACTING);
        documentRepository.save(document);

        Path documentPath = Path.of(document.getStoragePath());

        // 1. Extract raw structured fields with visual evidence proof via Gemini
        W2ExtractionResult rawExtraction = extractionAiClient.extractW2Data(documentPath);

        // 2. Validate mathematical cross-verification tax rules
        boolean isMathValid = extractionValidator.validateW2MathRules(rawExtraction);
        if (!isMathValid) {
            log.warn("Document ID {} extracted with tax mathematical discrepancies.", document.getId());
        }

        // 3. Persist evidence JSON and update document state
        try {
            document.setExtractedDataJson(objectMapper.writeValueAsString(rawExtraction));
        } catch (JsonProcessingException e) {
            document.setExtractedDataJson("{}");
        }

        document.setStatus(DocumentStatus.EXTRACTED);
        documentRepository.save(document);

        return rawExtraction;
    }

    @Override
    @Transactional
    public List<W2ExtractionResult> extractBatchW2Data(List<UUID> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            throw new IllegalArgumentException("Document ID list cannot be empty");
        }

        List<Document> documents = documentRepository.findAllById(documentIds);

        // Safe transactional processing loop
        return documents.stream()
                .map(this::extractW2Data)
                .toList();
    }
}