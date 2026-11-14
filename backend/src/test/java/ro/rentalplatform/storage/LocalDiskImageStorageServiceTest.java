package ro.rentalplatform.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalDiskImageStorageServiceTest {

    @TempDir
    Path tempDir;

    private Path root;
    private LocalDiskImageStorageService storage;

    @BeforeEach
    void setUp() {
        root = tempDir.resolve("uploads");
        StorageProperties props = new StorageProperties();
        props.setRoot(root.toString());
        storage = new LocalDiskImageStorageService(props);
    }

    private static byte[] realImage(String format, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format, out);
        return out.toByteArray();
    }

    private static MockMultipartFile png(String filename) throws IOException {
        return new MockMultipartFile("file", filename, "image/png", realImage("png", 10, 10));
    }

    @Test
    void storeSavesFileInFolderAndReturnsRelativePath() throws IOException {
        StoredImage stored = storage.store(png("me.png"), "avatars");

        assertTrue(stored.relativePath().startsWith("avatars/"));
        assertTrue(stored.relativePath().endsWith(".png"));
        assertTrue(Files.exists(root.resolve(stored.relativePath())));
    }

    @Test
    void storeGeneratesFileNameInsteadOfUsingTheUploadedOne() throws IOException {
        StoredImage first = storage.store(png("me.png"), "avatars");
        StoredImage second = storage.store(png("me.png"), "avatars");

        assertFalse(first.relativePath().contains("me"));
        assertNotEquals(first.relativePath(), second.relativePath());
    }

    @Test
    void storeResizesLargeImagesToMaxSide() throws IOException {
        MockMultipartFile large = new MockMultipartFile("file", "big.png", "image/png",
                realImage("png", 2000, 1000));

        StoredImage stored = storage.store(large, "avatars");

        BufferedImage saved = ImageIO.read(root.resolve(stored.relativePath()).toFile());
        assertEquals(1600, saved.getWidth());
        assertEquals(800, saved.getHeight());
    }

    @Test
    void storeRejectsInvalidImageAndWritesNothing() {
        MockMultipartFile text = new MockMultipartFile("file", "fake.jpg", "image/jpeg", "hello".getBytes());

        assertThrows(InvalidImageException.class, () -> storage.store(text, "avatars"));
        assertFalse(Files.exists(root.resolve("avatars")));
    }

    @Test
    void storeRejectsFolderOutsideRoot() throws IOException {
        MockMultipartFile file = png("me.png");

        assertThrows(InvalidImageException.class, () -> storage.store(file, "../evil"));
        assertFalse(Files.exists(tempDir.resolve("evil")));
    }

    @Test
    void deleteRemovesStoredFile() throws IOException {
        StoredImage stored = storage.store(png("me.png"), "avatars");
        Path savedFile = root.resolve(stored.relativePath());

        storage.delete(stored.relativePath());

        assertFalse(Files.exists(savedFile));
    }

    @Test
    void deleteRejectsPathOutsideRootAndKeepsTheFile() throws IOException {
        Path secret = Files.writeString(tempDir.resolve("secret.txt"), "secret");

        assertThrows(InvalidImageException.class, () -> storage.delete("../secret.txt"));
        assertTrue(Files.exists(secret));
    }

    @Test
    void deleteIgnoresNullAndBlankPaths() {
        assertDoesNotThrow(() -> storage.delete(null));
        assertDoesNotThrow(() -> storage.delete("  "));
    }

    @Test
    void deleteFolderRemovesFolderWithAllFiles() throws IOException {
        storage.store(png("a.png"), "listings/5");
        storage.store(png("b.png"), "listings/5");

        storage.deleteFolder("listings/5");

        assertFalse(Files.exists(root.resolve("listings/5")));
    }

    @Test
    void deleteFolderRejectsFolderOutsideRoot() throws IOException {
        Path outside = Files.createDirectory(tempDir.resolve("outside"));

        assertThrows(InvalidImageException.class, () -> storage.deleteFolder("../outside"));
        assertTrue(Files.exists(outside));
    }
}
