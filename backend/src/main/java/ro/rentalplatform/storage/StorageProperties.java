package ro.rentalplatform.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {
    private String root = "../uploads";
    private long maxSize = 5L * 1024 * 1024;
    private List<String> allowedTypes = List.of("jpg", "jpeg", "png", "webp");
}
