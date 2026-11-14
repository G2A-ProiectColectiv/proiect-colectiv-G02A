package ro.rentalplatform.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.rentalplatform.location.config.LocationProperties;
import ro.rentalplatform.location.domain.Coordinates;
import ro.rentalplatform.location.exception.CoordinatesNotFoundException;
import ro.rentalplatform.location.parser.GoogleMapsLinkParser;
import ro.rentalplatform.location.parser.PathCoordinatesExtractor;
import ro.rentalplatform.location.parser.PinDataExtractor;
import ro.rentalplatform.location.parser.QueryParamExtractor;
import ro.rentalplatform.location.parser.RawCoordinatesExtractor;
import ro.rentalplatform.location.parser.ViewportExtractor;
import ro.rentalplatform.location.resolver.AllowedHostsPolicy;
import ro.rentalplatform.location.resolver.ShortLinkResolver;
import ro.rentalplatform.location.service.LocationServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Real parser and host policy; only the network part (short link resolver) is mocked. */
@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    private static final String SHORT_LINK = "https://maps.app.goo.gl/AbCdEf123";
    private static final String LONG_LINK = "https://www.google.com/maps/place/Bucuresti/data=!3d44.4267674!4d26.1025384";

    @Mock
    private ShortLinkResolver shortLinkResolver;

    private LocationServiceImpl service;

    @BeforeEach
    void setUp() {
        GoogleMapsLinkParser parser = new GoogleMapsLinkParser(List.of(
                new PinDataExtractor(),
                new QueryParamExtractor(),
                new PathCoordinatesExtractor(),
                new ViewportExtractor(),
                new RawCoordinatesExtractor()
        ));
        service = new LocationServiceImpl(parser, shortLinkResolver, new AllowedHostsPolicy(new LocationProperties()));
    }

    @Test
    void parsesLongLinkWithoutNetworkCall() {
        Coordinates result = service.parse(LONG_LINK);

        assertEquals(44.4267674, result.latitude(), 1e-7);
        verify(shortLinkResolver, never()).resolve(anyString());
    }

    @Test
    void parsesRawCoordinates() {
        Coordinates result = service.parse("  44.4268, 26.1025 ");

        assertEquals(26.1025, result.longitude(), 1e-6);
        verify(shortLinkResolver, never()).resolve(anyString());
    }

    @Test
    void resolvesShortLinkThenParsesResult() {
        when(shortLinkResolver.resolve(SHORT_LINK)).thenReturn(LONG_LINK);

        Coordinates result = service.parse(SHORT_LINK);

        assertEquals(44.4267674, result.latitude(), 1e-7);
        assertEquals(26.1025384, result.longitude(), 1e-7);
    }

    @Test
    void addsHttpsToShortLinkWithoutScheme() {
        when(shortLinkResolver.resolve(SHORT_LINK)).thenReturn(LONG_LINK);

        Coordinates result = service.parse("maps.app.goo.gl/AbCdEf123");

        assertEquals(44.4267674, result.latitude(), 1e-7);
    }

    @Test
    void shortLinkResolvingToLinkWithoutCoordinatesFails() {
        when(shortLinkResolver.resolve(SHORT_LINK))
                .thenReturn("https://www.google.com/maps/place/Palatul+Parlamentului");

        assertThrows(CoordinatesNotFoundException.class, () -> service.parse(SHORT_LINK));
    }

    @Test
    void plainHttpShortLinkIsRejectedWithoutNetworkCall() {
        assertThrows(CoordinatesNotFoundException.class, () -> service.parse("http://maps.app.goo.gl/AbCdEf123"));
        verify(shortLinkResolver, never()).resolve(anyString());
    }

    @Test
    void linkWithoutCoordinatesFailsWithExplicitMessage() {
        // LOC-09: the user is told what to do
        CoordinatesNotFoundException ex = assertThrows(CoordinatesNotFoundException.class,
                () -> service.parse("https://www.google.com/maps/place/Palatul+Parlamentului"));

        assertEquals(CoordinatesNotFoundException.DEFAULT_MESSAGE, ex.getMessage());
        verify(shortLinkResolver, never()).resolve(anyString());
    }

    @Test
    void nonGoogleLinkIsNeverResolved() {
        assertThrows(CoordinatesNotFoundException.class, () -> service.parse("https://bit.ly/abc"));
        verify(shortLinkResolver, never()).resolve(anyString());
    }

    @Test
    void blankInputFails() {
        assertThrows(CoordinatesNotFoundException.class, () -> service.parse("  "));
        assertThrows(CoordinatesNotFoundException.class, () -> service.parse(null));
    }
}
