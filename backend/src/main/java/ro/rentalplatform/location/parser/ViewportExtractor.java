package ro.rentalplatform.location.parser;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ro.rentalplatform.location.domain.Coordinates;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(4)
public class ViewportExtractor implements CoordinateExtractor {

    private static final Pattern VIEWPORT_PATTERN = Pattern.compile(
            "/@(-?\\d+(?:\\.\\d+)?),(-?\\d+(?:\\.\\d+)?)(?:,\\d+(?:\\.\\d+)?[a-zA-Z]?)?"
    );

    @Override
    public Optional<Coordinates> extract(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        if (input.contains("/dir/")) {
            return Optional.empty();
        }
        Matcher matcher = VIEWPORT_PATTERN.matcher(input);
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
