package cs.sonu.TaxDoc.audit.repository;

import cs.sonu.TaxDoc.audit.entity.FieldAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FieldAuditLogRepository extends JpaRepository<FieldAuditLog, UUID> {
    List<FieldAuditLog> findByDocumentId(UUID documentId);
}