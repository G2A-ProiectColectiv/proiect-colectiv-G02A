package ro.rentalplatform.location.resolver;

import org.springframework.stereotype.Component;
import ro.rentalplatform.location.config.LocationProperties;
import ro.rentalplatform.location.exception.CoordinatesNotFoundException;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
public class HttpShortLinkResolver implements ShortLinkResolver {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private final AllowedHostsPolicy allowedHostsPolicy;
    private final LocationProperties locationProperties;
    private final HttpClient httpClient;

    public HttpShortLinkResolver(AllowedHostsPolicy allowedHostsPolicy,
                                 LocationProperties locationProperties) {
        this.allowedHostsPolicy = allowedHostsPolicy;
        this.locationProperties = locationProperties;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(locationProperties.getTimeout())
                .build();
    }

    @Override
    public String resolve(String shortUrl) {
        URI currentUri = parseAndValidate(shortUrl);
        int maxRedirects = locationProperties.getMaxRedirects();

        for (int hop = 0; hop < maxRedirects; hop++) {
            HttpRequest request = HttpRequest.newBuilder(currentUri)
                    .GET()
                    .timeout(locationProperties.getTimeout())
                    .header("User-Agent", USER_AGENT)
                    .header("Cookie", "CONSENT=YES+cb.20210328-17-p0.en+FX+667; SOCS=CAI")
                    .build();

            HttpResponse<Void> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new CoordinatesNotFoundException();
            } catch (IOException ex) {
                throw new CoordinatesNotFoundException();
            }

            int status = response.statusCode();
            if (status >= 300 && status < 400) {
                String locationHeader = response.headers().firstValue("Location")
                        .orElseThrow(CoordinatesNotFoundException::new);
                URI nextUri;
                try {
                    nextUri = unwrapConsentRedirect(currentUri.resolve(locationHeader));
                } catch (IllegalArgumentException ex) {
                    throw new CoordinatesNotFoundException();
                }
                if (!allowedHostsPolicy.isAllowed(nextUri)) {
                    throw new CoordinatesNotFoundException();
                }
                currentUri = nextUri;
            } else {
                return currentUri.toString();
            }
        }
        throw new CoordinatesNotFoundException();
    }

    private URI unwrapConsentRedirect(URI uri) {
        String host = uri.getHost();
        if (host == null || !"consent.google.com".equals(host.toLowerCase(Locale.ROOT))) {
            return uri;
        }
        String query = uri.getRawQuery();
        if (query == null) {
            return uri;
        }
        for (String param : query.split("&")) {
            int eq = param.indexOf('=');
            if (eq > 0 && "continue".equals(param.substring(0, eq))) {
                String target = URLDecoder.decode(param.substring(eq + 1), StandardCharsets.UTF_8);
                return URI.create(target);
            }
        }
        return uri;
    }

    private URI parseAndValidate(String url) {
        URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (IllegalArgumentException ex) {
            throw new CoordinatesNotFoundException();
        }
        if (!allowedHostsPolicy.isAllowed(uri)) {
            throw new CoordinatesNotFoundException();
        }
        return uri;
    }
}
