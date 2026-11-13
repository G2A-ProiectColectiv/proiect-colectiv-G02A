package ro.rentalplatform.common.web;

import org.springframework.data.domain.Sort;
import ro.rentalplatform.common.exception.BadRequestException;
import ro.rentalplatform.common.exception.FieldErrorDto;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Transformă parametrul {@code sort} din URL (de exemplu {@code "price,asc"}) într-un {@link Sort}.
 * <p>
 * Fiecare apelant trimite lista câmpurilor după care se poate sorta. Un câmp care nu e în listă
 * sau o direcție necunoscută dau {@code 400}, ca să nu se poată sorta după coloane interne.
 * Rezultatul are mereu {@code id} ca al doilea criteriu, pentru o paginare stabilă.
 */
public final class SortParser {

    private static final String SORT_PARAM = "sort";

    private SortParser() {
    }

    /**
     * @param value         valoarea parametrului, de forma {@code camp} sau {@code camp,asc|desc}; poate lipsi
     * @param allowedFields câmpurile permise pentru apelantul curent
     * @param defaultSort   sortarea folosită când parametrul lipsește
     */
    public static Sort parse(String value, Set<String> allowedFields, Sort defaultSort) {
        if (value == null || value.isBlank()) {
            return withIdTieBreaker(defaultSort);
        }

        String[] parts = value.split(",");
        if (parts.length > 2) {
            throw invalid("Formatul sortării trebuie să fie „câmp,asc” sau „câmp,desc”.");
        }

        String field = parts[0].trim();
        if (!allowedFields.contains(field)) {
            throw invalid("Nu se poate sorta după „" + field + "”. Câmpuri permise: "
                    + String.join(", ", allowedFields.stream().sorted().toList()) + ".");
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length == 2) {
            String rawDirection = parts[1].trim().toLowerCase(Locale.ROOT);
            direction = switch (rawDirection) {
                case "asc" -> Sort.Direction.ASC;
                case "desc" -> Sort.Direction.DESC;
                default -> throw invalid("Direcția sortării trebuie să fie „asc” sau „desc”.");
            };
        }

        return withIdTieBreaker(Sort.by(direction, field));
    }

    private static Sort withIdTieBreaker(Sort sort) {
        if (sort.getOrderFor("id") != null) {
            return sort;
        }
        Sort.Direction direction = sort.stream()
                .findFirst()
                .map(Sort.Order::getDirection)
                .orElse(Sort.Direction.DESC);
        return sort.and(Sort.by(direction, "id"));
    }

    private static BadRequestException invalid(String message) {
        return new BadRequestException(message, List.of(new FieldErrorDto(SORT_PARAM, message)));
    }
}
