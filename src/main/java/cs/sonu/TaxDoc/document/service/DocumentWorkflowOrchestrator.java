package cs.sonu.TaxDoc.document.service;

import cs.sonu.TaxDoc.classification.service.ClassificationService;
import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.entity.DocumentStatus;
import cs.sonu.TaxDoc.document.repository.DocumentRepository;
import cs.sonu.TaxDoc.document.workflow.DocumentWorkflowHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DocumentWorkflowOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(DocumentWorkflowOrchestrator.class);

    private final DocumentService documentService;
    private final ClassificationService classificationService;
    private final DocumentRepository documentRepository;
    private final Map<cs.sonu.TaxDoc.document.entity.DocumentType, DocumentWorkflowHandler> handlerMap;

    public DocumentWorkflowOrchestrator(
            DocumentService documentService,
            ClassificationService classificationService,
            DocumentRepository documentRepository,
            List<DocumentWorkflowHandler> handlers) {
        this.documentService = documentService;
        this.classificationService = classificationService;
        this.documentRepository = documentRepository;

        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(DocumentWorkflowHandler::getSupportedDocumentType, Function.identity()));
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

            if (document.getStatus() == DocumentStatus.REJECTED) {
                log.warn("Document ID {} was rejected during classification. Halting workflow.", document.getId());
                return document;
            }
            DocumentWorkflowHandler handler = handlerMap.get(document.getDocType());
            if (handler != null) {
                handler.handle(document);
            } else {
                log.info("No automated handler found for type {}. Routing ID {} to manual review queue.",
                        document.getDocType(), document.getId());
                document.setStatus(DocumentStatus.PENDING_REVIEW);
                documentRepository.save(document);
            }

            if (document.getStatus() == DocumentStatus.REJECTED) {
                log.warn("Document ID {} failed extraction safety thresholds and was deleted from DB.",
                        document.getId());
                return document;
            }

        } catch (Exception e) {
            log.error("Pipeline orchestration failure for Document ID {}: {}", document.getId(), e.getMessage());
            document.setStatus(DocumentStatus.REJECTED);
            document.setErrorMessage("Pipeline orchestration error: " + e.getMessage());

            try {
                documentRepository.save(document);
            } catch (Exception ex) {
            }
            return document;
        }

        return documentService.getDocument(document.getId());
    }
}