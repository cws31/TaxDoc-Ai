package cs.sonu.TaxDoc.extraction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.entity.DocumentStatus;
import cs.sonu.TaxDoc.document.repository.DocumentRepository;
import cs.sonu.TaxDoc.extraction.dto.ExtractedField;
import cs.sonu.TaxDoc.extraction.dto.W2CorrectionRequest;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditorService {

    private static final Logger log = LoggerFactory.getLogger(AuditorService.class);

    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;

    public AuditorService(DocumentRepository documentRepository, ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.objectMapper = objectMapper;
    }

    public List<Document> getPendingReviewDocuments() {
        return documentRepository.findByStatus(DocumentStatus.PENDING_REVIEW);
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    @Transactional
    public void submitReview(UUID documentId, W2CorrectionRequest request) throws JsonProcessingException {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));

        W2ExtractionResult currentData;
        if (doc.getExtractedDataJson() == null || doc.getExtractedDataJson().isBlank()) {
            currentData = createEmptyW2ExtractionResult();
        } else {
            currentData = objectMapper.readValue(doc.getExtractedDataJson(), W2ExtractionResult.class);
        }

        if (request.correctedFields() != null && !request.correctedFields().isEmpty()) {
            Map<String, String> fields = request.correctedFields();

            currentData = new W2ExtractionResult(
                    fields.containsKey("employeeSsn") ? createHumanOverrideString(fields.get("employeeSsn"), "Box a")
                            : currentData.employeeSsn(),
                    fields.containsKey("employerEin") ? createHumanOverrideString(fields.get("employerEin"), "Box b")
                            : currentData.employerEin(),
                    fields.containsKey("employerName") ? createHumanOverrideString(fields.get("employerName"), "Box c")
                            : currentData.employerName(),
                    fields.containsKey("box1Wages") ? createHumanOverrideDouble(fields.get("box1Wages"), "Box 1")
                            : currentData.box1Wages(),
                    fields.containsKey("box2FederalTaxWithheld")
                            ? createHumanOverrideDouble(fields.get("box2FederalTaxWithheld"), "Box 2")
                            : currentData.box2FederalTaxWithheld(),
                    fields.containsKey("box3SocialSecurityWages")
                            ? createHumanOverrideDouble(fields.get("box3SocialSecurityWages"), "Box 3")
                            : currentData.box3SocialSecurityWages(),
                    fields.containsKey("box4SocialSecurityTaxWithheld")
                            ? createHumanOverrideDouble(fields.get("box4SocialSecurityTaxWithheld"), "Box 4")
                            : currentData.box4SocialSecurityTaxWithheld(),
                    fields.containsKey("box5MedicareWages")
                            ? createHumanOverrideDouble(fields.get("box5MedicareWages"), "Box 5")
                            : currentData.box5MedicareWages(),
                    fields.containsKey("box6MedicareTaxWithheld")
                            ? createHumanOverrideDouble(fields.get("box6MedicareTaxWithheld"), "Box 6")
                            : currentData.box6MedicareTaxWithheld(),
                    fields.containsKey("box16StateWages")
                            ? createHumanOverrideDouble(fields.get("box16StateWages"), "Box 16")
                            : currentData.box16StateWages(),
                    fields.containsKey("box17StateTaxWithheld")
                            ? createHumanOverrideDouble(fields.get("box17StateTaxWithheld"), "Box 17")
                            : currentData.box17StateTaxWithheld(),
                    1.0);

            log.info("Applied {} human corrections for Document ID: {}", fields.size(), documentId);
        }

        doc.setExtractedDataJson(objectMapper.writeValueAsString(currentData));
        doc.setStatus(DocumentStatus.MANUAL_REVIEWED);
        doc.setDocTypeConfidence(1.0);
        documentRepository.save(doc);
        log.info("Document ID {} successfully reviewed and marked as MANUAL_REVIEWED", documentId);
    }

    @Transactional
    public void rejectDocument(UUID documentId, String reason) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));

        doc.setStatus(DocumentStatus.REJECTED);
        doc.setDocTypeConfidence(0.0);
        doc.setErrorMessage(reason);

        documentRepository.save(doc);
        log.warn("Document ID {} rejected. Reason: {}", documentId, reason);
    }

    private W2ExtractionResult createEmptyW2ExtractionResult() {
        return new W2ExtractionResult(
                null, null, null, null, null, null, null, null, null, null, null, 0.0);
    }

    private ExtractedField<String> createHumanOverrideString(String value, String boxLabel) {
        return new ExtractedField<>(
                value,
                value,
                boxLabel,
                1.0,
                "Manually corrected and verified by human auditor.");
    }

    private ExtractedField<Double> createHumanOverrideDouble(String valueStr, String boxLabel) {
        Double parsedValue = 0.0;
        if (valueStr != null && !valueStr.isBlank()) {
            try {
                parsedValue = Double.parseDouble(valueStr);
            } catch (NumberFormatException e) {
                parsedValue = 0.0;
            }
        }
        return new ExtractedField<>(
                parsedValue,
                valueStr,
                boxLabel,
                1.0,
                "Manually corrected and verified by human auditor.");
    }
}