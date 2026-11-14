package ro.rentalplatform.common.web;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import ro.rentalplatform.common.exception.BadRequestException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SortParserTest {

    private static final Set<String> ALLOWED = Set.of("price", "createdAt");
    private static final Sort DEFAULT = Sort.by(Sort.Direction.DESC, "createdAt");

    private static List<String> describe(Sort sort) {
        return sort.stream().map(order -> order.getProperty() + "," + order.getDirection()).toList();
    }

    @Test
    void parsesAllowedFieldWithDirection() {
        Sort sort = SortParser.parse("price,desc", ALLOWED, DEFAULT);

        // id is always added as a tie-breaker so pages are stable
        assertEquals(List.of("price,DESC", "id,DESC"), describe(sort));
    }

    @Test
    void directionDefaultsToAscendingAndIsCaseInsensitive() {
        assertEquals(List.of("price,ASC", "id,ASC"), describe(SortParser.parse("price", ALLOWED, DEFAULT)));
        assertEquals(List.of("price,ASC", "id,ASC"), describe(SortParser.parse(" price , ASC ", ALLOWED, DEFAULT)));
    }

    @Test
    void missingValueUsesDefault() {
        assertEquals(List.of("createdAt,DESC", "id,DESC"), describe(SortParser.parse(null, ALLOWED, DEFAULT)));
        assertEquals(List.of("createdAt,DESC", "id,DESC"), describe(SortParser.parse("  ", ALLOWED, DEFAULT)));
    }

    @Test
    void fieldOutsideTheAllowedListIsRejected() {
        // internal columns must not be sortable
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> SortParser.parse("passwordHash,asc", ALLOWED, DEFAULT));

        assertEquals("sort", ex.getFieldErrors().get(0).field());
    }

    @Test
    void invalidDirectionIsRejected() {
        assertThrows(BadRequestException.class, () -> SortParser.parse("price,up", ALLOWED, DEFAULT));
    }

    @Test
    void tooManyPartsAreRejected() {
        assertThrows(BadRequestException.class, () -> SortParser.parse("price,asc,extra", ALLOWED, DEFAULT));
    }
}
