package ro.rentalplatform.storage;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ImageProcessor {
    private static final int MAX_SIDE = 2400;
    /** Calitatea JPEG (0–1); implicitul ImageIO e 0,75, care se vede ca neclaritate pe poze de interior. */
    private static final float JPEG_QUALITY = 0.92f;

    private ImageProcessor() {
    }

    public static void resizeAndWrite(MultipartFile file, Path targetPath) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = Files.newOutputStream(targetPath)) {
            BufferedImage source = ImageIO.read(inputStream);
            if (source == null) {
                throw new IOException("Imaginea nu poate fi citită.");
            }

            int width = source.getWidth();
            int height = source.getHeight();
            double scale = Math.min(1.0d, MAX_SIDE / (double) Math.max(width, height));

            int targetWidth = Math.max(1, (int) Math.round(width * scale));
            int targetHeight = Math.max(1, (int) Math.round(height * scale));

            BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = resized.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
            } finally {
                graphics.dispose();
            }

            String extension = getExtension(targetPath.getFileName().toString());
            String formatName = switch (extension) {
                case "png" -> "png";
                case "webp" -> "jpg";
                default -> "jpg";
            };

            ImageIO.write(resized, formatName, outputStream);
        }
    }

    private static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "jpg";
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }
}
