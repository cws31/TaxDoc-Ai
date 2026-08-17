package cs.sonu.TaxDoc.document.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import cs.sonu.TaxDoc.common.exception.StorageException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootStorageLocation;

    public FileStorageService(@Value("${app.storage.location:storage/documents}") String storageLocation) {
        this.rootStorageLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootStorageLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize root storage location", e);
        }
    }

    public String store(UUID documentId, MultipartFile file) {
        String rawFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        if (rawFilename.contains("..") || rawFilename.contains("/") || rawFilename.contains("\\")) {
            throw new StorageException("Invalid path sequence in filename: " + rawFilename);
        }

        try {
            Path targetDir = rootStorageLocation.resolve(documentId.toString()).normalize();
            if (!targetDir.startsWith(rootStorageLocation)) {
                throw new StorageException("Directory traversal attack detected");
            }
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(rawFilename).normalize();
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return targetPath.toString();
        } catch (IOException e) {
            throw new StorageException("Failed to store file for document " + documentId, e);
        }
    }
}