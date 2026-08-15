package cs.sonu.TaxDoc.extraction.entity;

import cs.sonu.TaxDoc.document.entity.Document;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "extracted_fields")
public class ExtractedField {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "field_value", columnDefinition = "TEXT")
    private String fieldValue;

    @Column(precision = 4, scale = 3)
    private BigDecimal confidence;

    @Column(name = "source_snippet", columnDefinition = "TEXT")
    private String sourceSnippet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FieldStatus status;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

}