package cs.sonu.TaxDoc.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.entity.DocumentStatus;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByStatus(DocumentStatus pendingReview);
}