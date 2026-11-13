package ro.rentalplatform.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class LocalDiskImageStorageService implements ImageStorageService {
    private final StorageProperties storageProperties;

    public LocalDiskImageStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public StoredImage store(MultipartFile file, String folder) {
        ImageValidator.validate(file);

        Path root = Path.of(storageProperties.getRoot()).toAbsolutePath().normalize();
        String safeFolder = normalizeFolder(folder);
        Path targetFolder = root.resolve(safeFolder).normalize();

        if (!targetFolder.startsWith(root)) {
            throw new InvalidImageException("Folderul de destinație este invalid.");
        }

        try {
            Files.createDirectories(targetFolder);
            String extension = extractExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + "." + extension;
            Path targetPath = targetFolder.resolve(fileName).normalize();

            if (!targetPath.startsWith(targetFolder)) {
                throw new InvalidImageException("Calea fișierului este invalidă.");
            }

            ImageProcessor.resizeAndWrite(file, targetPath);
            return new StoredImage(safeFolder + "/" + fileName);
        } catch (IOException ex) {
            throw new InvalidImageException("Imaginea nu a putut fi salvată.");
        }
    }

    @Override
    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }

        Path root = Path.of(storageProperties.getRoot()).toAbsolutePath().normalize();
        Path targetPath = root.resolve(relativePath).normalize();

        if (!targetPath.startsWith(root)) {
            throw new InvalidImageException("Calea fișierului este invalidă.");
        }

        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException ex) {
            throw new IllegalStateException("Fișierul nu a putut fi șters de pe disc.", ex);
        }
    }

    @Override
    public void deleteFolder(String folder) {
        Path root = Path.of(storageProperties.getRoot()).toAbsolutePath().normalize();
        String safeFolder = normalizeFolder(folder);
        Path targetFolder = root.resolve(safeFolder).normalize();

        if (!targetFolder.startsWith(root)) {
            throw new InvalidImageException("Folderul de șters este invalid.");
        }

        try {
            if (Files.exists(targetFolder)) {
                Files.walk(targetFolder)
                        .sorted((left, right) -> right.compareTo(left))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ex) {
                                throw new IllegalStateException("Ștergerea folderului a eșuat.", ex);
                            }
                        });
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Ștergerea folderului a eșuat.", ex);
        }
    }

    private String normalizeFolder(String folder) {
        String safeFolder = folder == null || folder.isBlank() ? "" : folder.trim().replace('\\', '/');
        if (safeFolder.isBlank()) {
            return "";
        }
        safeFolder = safeFolder.replaceAll("^/+", "").replaceAll("/+", "/");
        return safeFolder;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "jpg";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return "jpg";
        }
        String extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        return switch (extension) {
            case "jpeg" -> "jpg";
            case "png", "jpg", "webp" -> extension;
            default -> "jpg";
        };
    }
}
