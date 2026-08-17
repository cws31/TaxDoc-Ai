package cs.sonu.TaxDoc.document.workflow;

import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.entity.DocumentType;

public interface DocumentWorkflowHandler {
    DocumentType getSupportedDocumentType();

    void handle(Document document);
}