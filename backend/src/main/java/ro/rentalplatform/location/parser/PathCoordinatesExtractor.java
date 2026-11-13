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
@Order(3)
public class PathCoordinatesExtractor implements CoordinateExtractor {

    private static final Pattern PATH_PATTERN = Pattern.compile(
            "/(?:place|search)/\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*\\+?(-?\\d+(?:\\.\\d+)?)(?:[/\\?#]|$)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public Optional<Coordinates> extract(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        if (input.contains("/dir/")) {
            return Optional.empty();
        }
        String decoded;
        try {
            decoded = URLDecoder.decode(input, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            decoded = input;
        }
        Matcher matcher = PATH_PATTERN.matcher(decoded);
        if (matcher.find()) {
            try {
                double lat = Double.parseDouble(matcher.group(1));
                double lng = Double.parseDouble(matcher.group(2));
                if (Coordinates.isValid(lat, lng)) {
                    return Optional.of(new Coordinates(lat, lng));
                }
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
