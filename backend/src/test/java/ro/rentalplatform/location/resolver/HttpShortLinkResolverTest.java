package ro.rentalplatform.location.resolver;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.rentalplatform.location.config.LocationProperties;
import ro.rentalplatform.location.exception.CoordinatesNotFoundException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Uses a local MockWebServer instead of Google. The real AllowedHostsPolicy only accepts https Google hosts,
 * so here it is replaced by a mock that allows only this local server.
 */
class HttpShortLinkResolverTest {

    private MockWebServer server;
    private HttpShortLinkResolver resolver;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        AllowedHostsPolicy onlyLocalServer = mock(AllowedHostsPolicy.class);
        when(onlyLocalServer.isAllowed(any())).thenAnswer(inv -> {
            URI uri = inv.getArgument(0);
            return uri != null && uri.getPort() == server.getPort();
        });

        LocationProperties props = new LocationProperties();
        props.setMaxRedirects(5);
        props.setTimeout(Duration.ofMillis(500));
        resolver = new HttpShortLinkResolver(onlyLocalServer, props);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private static MockResponse redirectTo(String location) {
        return new MockResponse().setResponseCode(302).setHeader("Location", location);
    }

    @Test
    void followsChainOfTwoRedirectsToFinalUrl() throws InterruptedException {
        String finalUrl = server.url("/maps/place/@44.4268,26.1025,15z").toString();
        server.enqueue(redirectTo(server.url("/step2").toString()));
        server.enqueue(redirectTo(finalUrl));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("<html>map</html>"));

        String resolved = resolver.resolve(server.url("/short").toString());

        assertEquals(finalUrl, resolved);
        assertEquals(3, server.getRequestCount());
    }

    @Test
    void sendsBrowserUserAgent() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(200));

        resolver.resolve(server.url("/short").toString());

        RecordedRequest request = server.takeRequest();
        assertNotNull(request.getHeader("User-Agent"));
        assertTrue(request.getHeader("User-Agent").startsWith("Mozilla/5.0"));
    }

    @Test
    void resolvesRelativeLocationHeader() {
        server.enqueue(redirectTo("/maps/@44.4268,26.1025,15z"));
        server.enqueue(new MockResponse().setResponseCode(200));

        String resolved = resolver.resolve(server.url("/short").toString());

        assertEquals(server.url("/maps/@44.4268,26.1025,15z").toString(), resolved);
    }

    @Test
    void forbiddenHostInChainStopsWithoutCallingIt() {
        server.enqueue(redirectTo("https://evil.example/steal"));

        assertThrows(CoordinatesNotFoundException.class, () -> resolver.resolve(server.url("/short").toString()));
        assertEquals(1, server.getRequestCount());
    }

    @Test
    void forbiddenStartUrlIsNeverRequested() {
        assertThrows(CoordinatesNotFoundException.class, () -> resolver.resolve("https://evil.example/short"));
        assertEquals(0, server.getRequestCount());
    }

    @Test
    void chainLongerThanMaxRedirectsFails() {
        for (int i = 0; i < 6; i++) {
            server.enqueue(redirectTo(server.url("/hop" + i).toString()));
        }
        server.enqueue(new MockResponse().setResponseCode(200));

        assertThrows(CoordinatesNotFoundException.class, () -> resolver.resolve(server.url("/short").toString()));
    }

    @Test
    void redirectWithoutLocationHeaderFails() {
        server.enqueue(new MockResponse().setResponseCode(302));

        assertThrows(CoordinatesNotFoundException.class, () -> resolver.resolve(server.url("/short").toString()));
    }

    @Test
    void slowServerTimesOut() {
        server.enqueue(new MockResponse().setResponseCode(200).setHeadersDelay(3, TimeUnit.SECONDS));

        assertThrows(CoordinatesNotFoundException.class, () -> resolver.resolve(server.url("/short").toString()));
    }

    @Test
    void unwrapsGoogleConsentRedirect() {
        String finalUrl = server.url("/maps/@44.4268,26.1025,15z").toString();
        String consent = "https://consent.google.com/ml?continue="
                + URLEncoder.encode(finalUrl, StandardCharsets.UTF_8) + "&gl=RO";
        server.enqueue(redirectTo(consent));
        server.enqueue(new MockResponse().setResponseCode(200));

        String resolved = resolver.resolve(server.url("/short").toString());

        assertEquals(finalUrl, resolved);
    }
}
