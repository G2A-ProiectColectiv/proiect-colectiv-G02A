package ro.rentalplatform.location.parser;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ro.rentalplatform.location.domain.Coordinates;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(2)
public class QueryParamExtractor implements CoordinateExtractor {

    private static final Pattern PARAM_PATTERN = Pattern.compile(
            "(?:[?&])(q|query|ll)=([^&#]+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern COORDS_VALUE_PATTERN = Pattern.compile(
            "^\\s*(?:loc:)?(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public Optional<Coordinates> extract(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        Matcher paramMatcher = PARAM_PATTERN.matcher(input);
        while (paramMatcher.find()) {
            String rawValue = paramMatcher.group(2);
            String decoded;
            try {
                decoded = URLDecoder.decode(rawValue, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException ex) {
                decoded = rawValue;
            }
            Matcher coordsMatcher = COORDS_VALUE_PATTERN.matcher(decoded);
            if (coordsMatcher.matches()) {
                try {
                    double lat = Double.parseDouble(coordsMatcher.group(1));
                    double lng = Double.parseDouble(coordsMatcher.group(2));
                    if (Coordinates.isValid(lat, lng)) {
                        return Optional.of(new Coordinates(lat, lng));
                    }
                } catch (NumberFormatException ignored) {
                    // încercăm următorul parametru
                }
            }
        }
        return Optional.empty();
    }
}
