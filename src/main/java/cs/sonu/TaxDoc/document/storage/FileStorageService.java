package cs.sonu.TaxDoc.document.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageLocation;

    public FileStorageService(
            @Value("${app.storage.location:storage/documents}") String storageLocation) {

        this.storageLocation = Paths.get(storageLocation)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create storage directory", e);
        }
    }

    public String store(UUID documentId, MultipartFile file) {

        try {
            Path documentDirectory = storageLocation.resolve(documentId.toString());

            Files.createDirectories(documentDirectory);

            String originalFilename = file.getOriginalFilename();

            if (originalFilename == null || originalFilename.isBlank()) {
                throw new IllegalArgumentException(
                        "File must have a name");
            }

            String safeFilename = Paths.get(originalFilename)
                    .getFileName()
                    .toString();

            Path targetPath = documentDirectory.resolve(safeFilename);

            Files.copy(
                    file.getInputStream(),
                    targetPath);

            return targetPath.toString();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not store file", e);
        }
    }
}