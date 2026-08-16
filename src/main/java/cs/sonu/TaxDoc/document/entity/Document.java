package cs.sonu.TaxDoc.document.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String originalFilename;
    private String storagePath;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @Enumerated(EnumType.STRING)
    private DocumentType docType;

    private Double docTypeConfidence;

    // --- Grounded Proof & Evidence Fields ---
    @Column(columnDefinition = "TEXT")
    private String classificationEvidenceJson; // Serialized list/JSON of detected headers, boxes, OMB signatures

    @Column(columnDefinition = "TEXT")
    private String classificationReasoning; // Explicit explanation from vision model

    private Boolean isProofVerified; // Flag indicating if deterministic verification passed

    private Instant uploadedAt;
    private String errorMessage;

    // Constructors, Getters, and Setters...
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public DocumentType getDocType() {
        return docType;
    }

    public void setDocType(DocumentType docType) {
        this.docType = docType;
    }

    public Double getDocTypeConfidence() {
        return docTypeConfidence;
    }

    public void setDocTypeConfidence(Double docTypeConfidence) {
        this.docTypeConfidence = docTypeConfidence;
    }

    public String getClassificationEvidenceJson() {
        return classificationEvidenceJson;
    }

    public void setClassificationEvidenceJson(String classificationEvidenceJson) {
        this.classificationEvidenceJson = classificationEvidenceJson;
    }

    public String getClassificationReasoning() {
        return classificationReasoning;
    }

    public void setClassificationReasoning(String classificationReasoning) {
        this.classificationReasoning = classificationReasoning;
    }

    public Boolean getIsProofVerified() {
        return isProofVerified;
    }

    public void setIsProofVerified(Boolean isProofVerified) {
        this.isProofVerified = isProofVerified;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}