package ro.rentalplatform.location.parser;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ro.rentalplatform.location.domain.Coordinates;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(1)
public class PinDataExtractor implements CoordinateExtractor {

    private static final Pattern PIN_PATTERN = Pattern.compile(
            "!3d(-?\\d+(?:\\.\\d+)?)!4d(-?\\d+(?:\\.\\d+)?)"
    );

    @Override
    public Optional<Coordinates> extract(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = PIN_PATTERN.matcher(input);
        Optional<Coordinates> found = Optional.empty();
        while (matcher.find()) {
            try {
                double lat = Double.parseDouble(matcher.group(1));
                double lng = Double.parseDouble(matcher.group(2));
                if (Coordinates.isValid(lat, lng)) {
                    found = Optional.of(new Coordinates(lat, lng));
                }
            } catch (NumberFormatException ignored) {
                // continuăm căutarea
            }
        }
        return found;
    }
}
