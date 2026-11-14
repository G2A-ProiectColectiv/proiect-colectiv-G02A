package ro.rentalplatform.location.resolver;

import org.junit.jupiter.api.Test;
import ro.rentalplatform.location.config.LocationProperties;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllowedHostsPolicyTest {

    private final AllowedHostsPolicy policy = new AllowedHostsPolicy(new LocationProperties());

    @Test
    void allowsGoogleMapsHostsOverHttps() {
        assertTrue(policy.isAllowed(URI.create("https://maps.app.goo.gl/AbCdEf123")));
        assertTrue(policy.isAllowed(URI.create("https://goo.gl/maps/xyz")));
        assertTrue(policy.isAllowed(URI.create("https://www.google.com/maps/@44.4,26.1,15z")));
        assertTrue(policy.isAllowed(URI.create("https://maps.google.ro/?q=44.4,26.1")));
    }

    @Test
    void allowsOtherGoogleCountryDomains() {
        assertTrue(policy.isAllowed(URI.create("https://www.google.de/maps")));
        assertTrue(policy.isAllowed(URI.create("https://www.google.co.uk/maps")));
    }

    @Test
    void hostCheckIsCaseInsensitive() {
        assertTrue(policy.isAllowed(URI.create("https://WWW.Google.COM/maps")));
    }

    @Test
    void rejectsPlainHttp() {
        assertFalse(policy.isAllowed(URI.create("http://maps.app.goo.gl/AbCdEf123")));
        assertFalse(policy.isAllowed(URI.create("http://www.google.com/maps")));
    }

    @Test
    void rejectsNonGoogleHostsAndLookalikes() {
        // NF-09: the resolver must never be tricked into calling arbitrary servers
        assertFalse(policy.isAllowed(URI.create("https://evil.com/maps")));
        assertFalse(policy.isAllowed(URI.create("https://google.com.evil.com/maps")));
        assertFalse(policy.isAllowed(URI.create("https://notgoogle.com/maps")));
        assertFalse(policy.isAllowed(URI.create("https://maps.google.com@evil.com/maps")));
        assertFalse(policy.isAllowed(URI.create("https://localhost/maps")));
        assertFalse(policy.isAllowed(URI.create("https://127.0.0.1/maps")));
    }

    @Test
    void rejectsMissingSchemeHostOrUri() {
        assertFalse(policy.isAllowed(URI.create("maps.app.goo.gl/AbCdEf123")));
        assertFalse(policy.isAllowed(URI.create("https:///maps")));
        assertFalse(policy.isAllowed(null));
    }

    @Test
    void detectsShortLinks() {
        assertTrue(policy.isShortLink(URI.create("https://maps.app.goo.gl/AbCdEf123")));
        assertTrue(policy.isShortLink(URI.create("https://goo.gl/maps/xyz")));
        assertFalse(policy.isShortLink(URI.create("https://www.google.com/maps/@44.4,26.1,15z")));
        assertFalse(policy.isShortLink(null));
    }
}
