package cs.sonu.TaxDoc.document.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @Column(columnDefinition = "TEXT")
    private String extractedDataJson;

    // --- Grounded Proof & Evidence Fields ---
    @Column(columnDefinition = "TEXT")
    private String classificationEvidenceJson; // Serialized list/JSON of detected headers, boxes, OMB signatures

    @Column(columnDefinition = "TEXT")
    private String classificationReasoning; // Explicit explanation from vision model

    private Boolean isProofVerified; // Flag indicating if deterministic verification passed

    private Instant uploadedAt;
    private String errorMessage;

}