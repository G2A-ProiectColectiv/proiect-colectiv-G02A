package ro.rentalplatform.storage;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class ImageValidator {
    private ImageValidator() {
    }

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("Fișierul este gol.");
        }

        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                : "";

        if (!List.of("jpg", "jpeg", "png", "webp").contains(extension)) {
            throw new InvalidImageException("Doar fișiere JPG, PNG sau WEBP sunt acceptate.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = new byte[12];
            int length = inputStream.read(header);
            if (length < 4) {
                throw new InvalidImageException("Fișierul nu este o imagine validă.");
            }

            boolean isJpg = header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF;
            boolean isPng = header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47;
            boolean isWebp = header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F';

            if (!isJpg && !isPng && !isWebp) {
                throw new InvalidImageException("Fișierul nu este o imagine validă.");
            }

            if (ImageIO.read(file.getInputStream()) == null) {
                throw new InvalidImageException("Fișierul nu poate fi citit ca imagine.");
            }
        } catch (IOException ex) {
            throw new InvalidImageException("Fișierul nu poate fi procesat ca imagine.");
        }
    }
}
