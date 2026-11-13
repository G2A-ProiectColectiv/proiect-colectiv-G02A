package ro.rentalplatform.location.resolver;

import org.springframework.stereotype.Component;
import ro.rentalplatform.location.config.LocationProperties;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class AllowedHostsPolicy {

    private static final Pattern GOOGLE_DOMAIN_PATTERN = Pattern.compile(
            "^(?:(?:www|maps)\\.)?google\\.[a-z]{2,3}(?:\\.[a-z]{2})?$"
    );

    private final LocationProperties locationProperties;

    public AllowedHostsPolicy(LocationProperties locationProperties) {
        this.locationProperties = locationProperties;
    }

    public boolean isAllowed(URI uri) {
        if (uri == null) {
            return false;
        }
        String scheme = uri.getScheme();
        if (scheme == null || !"https".equalsIgnoreCase(scheme)) {
            return false;
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return false;
        }
        String normalizedHost = host.toLowerCase(Locale.ROOT);
        for (String allowed : locationProperties.getAllowedHosts()) {
            if (normalizedHost.equalsIgnoreCase(allowed.trim())) {
                return true;
            }
        }
        return GOOGLE_DOMAIN_PATTERN.matcher(normalizedHost).matches();
    }

    public boolean isShortLink(URI uri) {
        if (uri == null || uri.getHost() == null) {
            return false;
        }
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        return "maps.app.goo.gl".equals(host) || "goo.gl".equals(host);
    }
}
