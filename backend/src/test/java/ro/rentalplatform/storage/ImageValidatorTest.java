package ro.rentalplatform.storage;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ImageValidatorTest {

    private static byte[] realImage(String format) throws IOException {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format, out);
        return out.toByteArray();
    }

    private static MockMultipartFile file(String filename, byte[] content) {
        return new MockMultipartFile("file", filename, "application/octet-stream", content);
    }

    @Test
    void acceptsRealPng() throws IOException {
        MockMultipartFile png = file("photo.png", realImage("png"));

        assertDoesNotThrow(() -> ImageValidator.validate(png));
    }

    @Test
    void rejectsTextFileRenamedToJpg() {
        MockMultipartFile fake = file("photo.jpg", "hello".getBytes());

        InvalidImageException ex = assertThrows(InvalidImageException.class, () -> ImageValidator.validate(fake));

        assertEquals("Fișierul nu este o imagine validă.", ex.getMessage());
    }

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile fake = file("empty.png", new byte[0]);

        InvalidImageException ex = assertThrows(InvalidImageException.class, () -> ImageValidator.validate(fake));

        assertEquals("Fișierul este gol.", ex.getMessage());
    }

    @Test
    void rejectsWrongExtensionEvenForValidImage() throws IOException {
        MockMultipartFile gif = file("photo.gif", realImage("png"));

        InvalidImageException ex = assertThrows(InvalidImageException.class, () -> ImageValidator.validate(gif));

        assertEquals("Doar fișiere JPG, PNG sau WEBP sunt acceptate.", ex.getMessage());
    }

    @Test
    void acceptsRealJpg() throws IOException {
        MockMultipartFile accepted = file("photo.jpg", realImage("jpg"));

        assertDoesNotThrow(() -> ImageValidator.validate(accepted));
    }

    @Test
    void rejectsCorruptedPng() throws IOException {
        MockMultipartFile corrupted = file("photo.png", Arrays.copyOf(realImage("png"), 20));

        InvalidImageException ex = assertThrows(InvalidImageException.class, () -> ImageValidator.validate(corrupted));

        assertEquals("Fișierul nu poate fi procesat ca imagine.", ex.getMessage());
    }
}
