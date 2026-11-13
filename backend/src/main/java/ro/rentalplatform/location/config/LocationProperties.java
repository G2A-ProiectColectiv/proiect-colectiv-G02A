package ro.rentalplatform.location.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.location")
public class LocationProperties {
    private List<String> allowedHosts = List.of(
            "maps.app.goo.gl",
            "goo.gl",
            "google.com",
            "www.google.com",
            "maps.google.com",
            "google.ro",
            "www.google.ro",
            "maps.google.ro"
    );
    private int maxRedirects = 5;
    private Duration timeout = Duration.ofSeconds(5);
    private int rateLimitPerMinute = 20;
}
