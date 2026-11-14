package ro.rentalplatform.common.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextSearchTest {

    @Test
    void ignoresCaseAndSeparators() {
        assertEquals("cluj napoca", TextSearch.normalize("Cluj-Napoca"));
        assertEquals("cluj napoca", TextSearch.normalize("  CLUJ   napoca "));
    }

    @Test
    void removesRomanianDiacritics() {
        assertEquals("marasti", TextSearch.normalize("Mărăști"));
        assertEquals("bucuresti", TextSearch.normalize("București"));
        // both the comma-below and the older cedilla forms of ș and ț
        assertEquals("timisoara", TextSearch.normalize("Timişoara"));
        assertEquals("constanta", TextSearch.normalize("Constanţa"));
    }

    @Test
    void emptyForMissingOrMeaninglessInput() {
        assertEquals("", TextSearch.normalize(null));
        assertEquals("", TextSearch.normalize("  - , "));
    }
}
