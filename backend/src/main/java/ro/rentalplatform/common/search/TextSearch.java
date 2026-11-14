package ro.rentalplatform.common.search;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Compară texte fără să conteze literele mari și mici, diacriticile sau separatorii:
 * „Cluj-Napoca”, „cluj napoca” și „CLUJ NAPOCA” devin toate „cluj napoca”,
 * iar „Mărăști” devine „marasti”.
 * <p>
 * Aceeași normalizare se aplică în Java (textul căutat) și în SQL (coloana), ca rezultatele să
 * se potrivească. Varianta SQL folosește doar {@code lower}, {@code translate} și
 * {@code regexp_replace} din PostgreSQL, deci nu cere extensia {@code unaccent}.
 */
public final class TextSearch {

    /** Literele cu diacritice acoperite și echivalentul lor simplu; au aceeași lungime. */
    private static final String ACCENTED = "ăâîșşțţàáäãåçèéêëìíïñòóôöõùúûüýÿ";
    private static final String PLAIN = "aaisstt" + "aaaaa" + "c" + "eeee" + "iii" + "n" + "ooooo" + "uuuu" + "yy";

    private static final String SEPARATORS = "[^a-z0-9]+";

    static {
        if (ACCENTED.length() != PLAIN.length()) {
            throw new IllegalStateException("ACCENTED și PLAIN trebuie să aibă aceeași lungime.");
        }
    }

    private TextSearch() {
    }

    /** Textul căutat, normalizat; gol dacă nu are nicio literă sau cifră. */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return decomposed
                .replaceAll("\\p{M}+", "")
                .replaceAll(SEPARATORS, " ")
                .trim();
    }

    /** Aceeași normalizare, aplicată pe o coloană din baza de date. */
    public static Expression<String> normalized(CriteriaBuilder cb, Expression<String> column) {
        Expression<String> lowered = cb.lower(column);
        Expression<String> plain = cb.function(
                "translate", String.class, lowered, cb.literal(ACCENTED), cb.literal(PLAIN));
        Expression<String> separated = cb.function(
                "regexp_replace", String.class, plain, cb.literal(SEPARATORS), cb.literal(" "), cb.literal("g"));
        return cb.trim(separated);
    }
}
