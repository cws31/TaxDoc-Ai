package cs.sonu.TaxDoc.document.service;

import cs.sonu.TaxDoc.classification.service.ClassificationService;
import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.entity.DocumentStatus;
import cs.sonu.TaxDoc.document.entity.DocumentType;
import cs.sonu.TaxDoc.document.repository.DocumentRepository;
import cs.sonu.TaxDoc.extraction.service.ExtractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class DocumentWorkflowOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(DocumentWorkflowOrchestrator.class);

    private final DocumentService documentService;
    private final ClassificationService classificationService;
    private final ExtractionService extractionService;
    private final DocumentRepository documentRepository;

    public DocumentWorkflowOrchestrator(
            DocumentService documentService,
            ClassificationService classificationService,
            ExtractionService extractionService,
            DocumentRepository documentRepository) {
        this.documentService = documentService;
        this.classificationService = classificationService;
        this.extractionService = extractionService;
        this.documentRepository = documentRepository;
    }

    public List<Document> processBatchEndToEnd(MultipartFile[] files) {

        List<Document> uploadedDocuments = documentService.uploadBatch(files);

        return uploadedDocuments.parallelStream()
                .map(this::executePipeline)
                .toList();
    }

    private Document executePipeline(Document document) {
        try {
            log.info("Starting pipeline execution for Document ID: {}", document.getId());

            document = classificationService.classify(document);
            log.info("Document {} classified as type: {} with confidence: {}",
                    document.getId(), document.getDocType(), document.getDocTypeConfidence());

            if (document.getDocType() == DocumentType.W2) {
                log.info("Triggering automated W2 Extraction and Compliance Engine for ID: {}", document.getId());
                extractionService.extractW2Data(document);
            } else {
                log.warn("Document ID {} is not a W2. Flagging for review.", document.getId());
                document.setStatus(DocumentStatus.PENDING_REVIEW);
                documentRepository.save(document);
            }
        } catch (Exception e) {
            log.error("Pipeline failure for Document ID {}: {}", document.getId(), e.getMessage());
            document.setStatus(DocumentStatus.REJECTED);
            documentRepository.save(document);
        }

        return documentService.getDocument(document.getId());
    }
}