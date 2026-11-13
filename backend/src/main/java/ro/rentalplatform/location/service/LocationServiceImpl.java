package ro.rentalplatform.location.service;

import org.springframework.stereotype.Service;
import ro.rentalplatform.location.domain.Coordinates;
import ro.rentalplatform.location.exception.CoordinatesNotFoundException;
import ro.rentalplatform.location.parser.GoogleMapsLinkParser;
import ro.rentalplatform.location.resolver.AllowedHostsPolicy;
import ro.rentalplatform.location.resolver.ShortLinkResolver;

import java.net.URI;
import java.util.Optional;

@Service
public class LocationServiceImpl implements LocationService {

    private final GoogleMapsLinkParser linkParser;
    private final ShortLinkResolver shortLinkResolver;
    private final AllowedHostsPolicy allowedHostsPolicy;

    public LocationServiceImpl(GoogleMapsLinkParser linkParser,
                               ShortLinkResolver shortLinkResolver,
                               AllowedHostsPolicy allowedHostsPolicy) {
        this.linkParser = linkParser;
        this.shortLinkResolver = shortLinkResolver;
        this.allowedHostsPolicy = allowedHostsPolicy;
    }

    @Override
    public Coordinates parse(String input) {
        if (input == null || input.isBlank()) {
            throw new CoordinatesNotFoundException();
        }
        String trimmed = input.trim();

        Optional<Coordinates> direct = linkParser.parse(trimmed);
        if (direct.isPresent()) {
            return direct.get();
        }

        String normalizedUrl = normalizeShortUrlScheme(trimmed);
        URI uri = tryParseUri(normalizedUrl);
        if (uri != null && allowedHostsPolicy.isShortLink(uri)) {
            if (!allowedHostsPolicy.isAllowed(uri)) {
                throw new CoordinatesNotFoundException();
            }
            String resolvedUrl = shortLinkResolver.resolve(normalizedUrl);
            return linkParser.parse(resolvedUrl)
                    .orElseThrow(CoordinatesNotFoundException::new);
        }

        throw new CoordinatesNotFoundException();
    }

    private String normalizeShortUrlScheme(String candidate) {
        String lower = candidate.toLowerCase(java.util.Locale.ROOT);
        if (lower.startsWith("maps.app.goo.gl/") || lower.startsWith("goo.gl/")) {
            return "https://" + candidate;
        }
        return candidate;
    }

    private URI tryParseUri(String candidate) {
        try {
            return URI.create(candidate);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
