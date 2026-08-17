package cs.sonu.TaxDoc.document.workflow;

import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.entity.DocumentType;
import cs.sonu.TaxDoc.extraction.service.ExtractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class W2DocumentWorkflowHandler implements DocumentWorkflowHandler {

    private static final Logger log = LoggerFactory.getLogger(W2DocumentWorkflowHandler.class);
    private final ExtractionService extractionService;

    public W2DocumentWorkflowHandler(ExtractionService extractionService) {
        this.extractionService = extractionService;
    }

    @Override
    public DocumentType getSupportedDocumentType() {
        return DocumentType.W2;
    }

    @Override
    public void handle(Document document) {
        log.info("Delegating workflow execution to Unified Extraction Engine for ID: {}", document.getId());
        extractionService.extractDocument(document);
    }
}