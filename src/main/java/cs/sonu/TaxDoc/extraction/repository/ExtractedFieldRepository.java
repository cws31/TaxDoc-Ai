package cs.sonu.TaxDoc.extraction.repository;

import cs.sonu.TaxDoc.extraction.entity.ExtractedField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExtractedFieldRepository
        extends JpaRepository<ExtractedField, UUID> {

    List<ExtractedField> findByDocumentId(UUID documentId);
}