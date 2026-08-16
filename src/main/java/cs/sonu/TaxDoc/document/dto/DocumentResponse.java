package cs.sonu.TaxDoc.document.dto;

import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.entity.DocumentStatus;
import cs.sonu.TaxDoc.document.entity.DocumentType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String originalFilename,
        DocumentStatus status,
        DocumentType docType,
        BigDecimal docTypeConfidence,
        OffsetDateTime uploadedAt,
        String errorMessage) {
    public static DocumentResponse from(Document doc) {
        return new DocumentResponse(
                doc.getId(),
                doc.getOriginalFilename(),
                doc.getStatus(),
                doc.getDocType(),
                doc.getDocTypeConfidence(),
                doc.getUploadedAt(),
                doc.getErrorMessage());
    }
}