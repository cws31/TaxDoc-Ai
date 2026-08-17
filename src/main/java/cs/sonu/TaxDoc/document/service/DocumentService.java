package cs.sonu.TaxDoc.document.service;

import cs.sonu.TaxDoc.classification.service.ClassificationService;
import cs.sonu.TaxDoc.document.entity.Document;
import cs.sonu.TaxDoc.document.entity.DocumentStatus;
import cs.sonu.TaxDoc.document.repository.DocumentRepository;
import cs.sonu.TaxDoc.document.storage.FileStorageService;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final ClassificationService classificationService;

    public DocumentService(
            DocumentRepository documentRepository,
            FileStorageService fileStorageService,
            ClassificationService classificationService) {

        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
        this.classificationService = classificationService;
    }

    public Document uploadDocument(MultipartFile file) {
        validateFile(file);

        UUID documentId = UUID.randomUUID();
        String storagePath = fileStorageService.store(documentId, file);

        Document document = new Document();
        document.setId(documentId);
        document.setOriginalFilename(file.getOriginalFilename());
        document.setStoragePath(storagePath);
        document.setStatus(DocumentStatus.UPLOADED);

        return documentRepository.save(document);
    }

    public List<Document> uploadBatch(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("Batch upload request cannot be empty");
        }

        List<Document> documents = new ArrayList<>();

        for (MultipartFile file : files) {
            validateFile(file);

            UUID documentId = UUID.randomUUID();
            String storagePath = fileStorageService.store(documentId, file);

            Document document = new Document();
            document.setId(documentId);
            document.setOriginalFilename(file.getOriginalFilename());
            document.setStoragePath(storagePath);
            document.setStatus(DocumentStatus.UPLOADED);

            documents.add(document);
        }

        return documentRepository.saveAll(documents);
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public Document getDocument(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found: " + id));
    }

    public Document classifyDocument(UUID documentId) {
        Document document = getDocument(documentId);
        return classificationService.classify(document);
    }

    public List<Document> classifyBatch(List<UUID> documentIds) {
        return classificationService.classifyBatch(documentIds);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("application/pdf")
                        && !contentType.equals("image/jpeg")
                        && !contentType.equals("image/png"))) {

            throw new IllegalArgumentException("Only PDF, JPEG and PNG files are supported");
        }
    }
}