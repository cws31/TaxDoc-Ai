package cs.sonu.TaxDoc.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cs.sonu.TaxDoc.document.entity.Document;

import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
}