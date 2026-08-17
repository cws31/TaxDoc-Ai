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
    @Column(name = "status", length = 50, nullable = false)
    private DocumentStatus status;

    @Enumerated(EnumType.STRING)
    private DocumentType docType;

    private Double docTypeConfidence;

    @Column(columnDefinition = "TEXT")
    private String extractedDataJson;

    @Column(columnDefinition = "TEXT")
    private String classificationEvidenceJson;

    @Column(columnDefinition = "TEXT")
    private String classificationReasoning;

    private Boolean isProofVerified;

    private Instant uploadedAt;
    private String errorMessage;

}