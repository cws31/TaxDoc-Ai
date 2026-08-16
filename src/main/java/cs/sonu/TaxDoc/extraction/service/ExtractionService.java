package cs.sonu.TaxDoc.extraction.service;

import cs.sonu.TaxDoc.classification.ai.AiClient;
import cs.sonu.TaxDoc.common.exception.AiProcessingException;
import cs.sonu.TaxDoc.common.exception.ResourceNotFoundException;
import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.entity.DocumentStatus;
import cs.sonu.TaxDoc.document.entity.DocumentType;
import cs.sonu.TaxDoc.document.repository.DocumentRepository;
import cs.sonu.TaxDoc.extraction.dto.ExtractedFieldResponse;
import cs.sonu.TaxDoc.extraction.dto.ExtractedValue;
import cs.sonu.TaxDoc.extraction.dto.W2ExtractionResult;
import cs.sonu.TaxDoc.extraction.entity.ExtractedField;
import cs.sonu.TaxDoc.extraction.entity.ExtractedFieldStatus;
import cs.sonu.TaxDoc.extraction.repository.ExtractedFieldRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ExtractionService {

        private static final Logger log = LoggerFactory.getLogger(ExtractionService.class);
        private static final double AUTO_ACCEPT_CONFIDENCE_THRESHOLD = 0.90;

        private final DocumentRepository documentRepository;
        private final ExtractedFieldRepository extractedFieldRepository;
        private final AiClient aiClient;

        public ExtractionService(
                        DocumentRepository documentRepository,
                        ExtractedFieldRepository extractedFieldRepository,
                        AiClient aiClient) {

                this.documentRepository = documentRepository;
                this.extractedFieldRepository = extractedFieldRepository;
                this.aiClient = aiClient;
        }

        /**
         * Synchronously validates the document and flags it as EXTRACTING
         * before delegating to the background thread pool.
         */
        @Transactional
        public void prepareForExtraction(UUID documentId) {
                Document document = documentRepository.findById(documentId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Document not found with ID: " + documentId));

                if (document.getDocType() != DocumentType.W2) {
                        throw new IllegalArgumentException(
                                        "Only classified W2 documents can be extracted. Current type: "
                                                        + document.getDocType());
                }

                // Clean up previous extractions to maintain idempotency
                List<ExtractedField> existingFields = extractedFieldRepository.findByDocumentId(documentId);
                if (!existingFields.isEmpty()) {
                        extractedFieldRepository.deleteAll(existingFields);
                }

                document.setStatus(DocumentStatus.EXTRACTING);
                document.setErrorMessage(null);
                documentRepository.save(document);
        }

        // Executes AI extraction asynchronously on background threads.

        @Async("documentTaskExecutor")
        public void processExtractionAsync(UUID documentId) {
                log.info("Starting background extraction for document ID: {} on thread {}",
                                documentId, Thread.currentThread().getName());

                Document document = documentRepository.findById(documentId).orElse(null);
                if (document == null) {
                        log.error("Document ID {} vanished before async processing", documentId);
                        return;
                }

                try {
                        Path path = Path.of(document.getStoragePath());
                        W2ExtractionResult result = aiClient.extractW2(path);

                        // Persist fields in a batch save
                        saveExtractedResults(document, result);

                        // Update status to IN_REVIEW
                        document.setStatus(DocumentStatus.IN_REVIEW);
                        documentRepository.save(document);

                        log.info("Completed async extraction for document ID: {}", documentId);

                } catch (Exception e) {
                        log.error("Async extraction failed for document ID: {}", documentId, e);

                        document.setStatus(DocumentStatus.ERROR);
                        document.setErrorMessage("Extraction failed: " + e.getMessage());
                        documentRepository.save(document);
                }
        }

        @Transactional
        protected void saveExtractedResults(Document document, W2ExtractionResult result) {
                List<ExtractedField> fields = new ArrayList<>();

                addFieldToList(fields, document, "employer_name", result.employerName());
                addFieldToList(fields, document, "employer_ein", result.employerEin());
                addFieldToList(fields, document, "employee_ssn", result.employeeSsn());
                addFieldToList(fields, document, "box1_wages", result.box1Wages());
                addFieldToList(fields, document, "box2_federal_tax_withheld", result.box2FederalTaxWithheld());

                extractedFieldRepository.saveAll(fields);
        }

        private void addFieldToList(List<ExtractedField> list, Document document, String fieldName,
                        ExtractedValue<?> extractedValue) {
                if (extractedValue == null)
                        return;

                ExtractedField field = new ExtractedField();
                field.setDocument(document);
                field.setFieldName(fieldName);

                if (extractedValue.value() != null) {
                        field.setFieldValue(extractedValue.value().toString());
                }

                BigDecimal confidence = BigDecimal.valueOf(extractedValue.confidence())
                                .setScale(3, RoundingMode.HALF_UP);
                field.setConfidence(confidence);

                if (extractedValue.confidence() >= AUTO_ACCEPT_CONFIDENCE_THRESHOLD) {
                        field.setStatus(ExtractedFieldStatus.AUTO_ACCEPTED);
                } else {
                        field.setStatus(ExtractedFieldStatus.PENDING_REVIEW);
                }

                list.add(field);
        }

        @Transactional(readOnly = true)
        public List<ExtractedFieldResponse> getFields(UUID documentId) {
                if (!documentRepository.existsById(documentId)) {
                        throw new ResourceNotFoundException("Document not found with ID: " + documentId);
                }

                return extractedFieldRepository.findByDocumentId(documentId)
                                .stream()
                                .map(ExtractedFieldResponse::from)
                                .toList();
        }
}