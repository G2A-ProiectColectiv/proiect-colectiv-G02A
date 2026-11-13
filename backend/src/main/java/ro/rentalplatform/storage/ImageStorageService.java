package ro.rentalplatform.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    StoredImage store(MultipartFile file, String folder);
    void delete(String relativePath);
    void deleteFolder(String folder);
}
