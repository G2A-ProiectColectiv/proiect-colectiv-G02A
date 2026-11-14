package ro.rentalplatform.rentalrequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ro.rentalplatform.rentalrequest.dto.CreateRentalRequestDto;
import ro.rentalplatform.rentalrequest.dto.UpdateRentalRequestDto;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ro.rentalplatform.rentalrequest.RentalRequestTestData.createDto;

/**
 * CER-04. Runs the real Bean Validation engine on the DTOs, the same way @Valid does in the controller,
 * so the test also proves the annotation is wired to the validator and the error lands on budgetMax.
 */
class BudgetRangeValidatorTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    private static Set<ConstraintViolation<CreateRentalRequestDto>> validate(String min, String max) {
        return validator.validate(createDto(new BigDecimal(min), new BigDecimal(max), null, null));
    }

    @Test
    void minLowerThanMaxIsValid() {
        assertTrue(validate("250", "400").isEmpty());
    }

    @Test
    void minEqualToMaxIsValid() {
        assertTrue(validate("400", "400").isEmpty());
    }

    @Test
    void minGreaterThanMaxIsReportedOnBudgetMax() {
        Set<ConstraintViolation<CreateRentalRequestDto>> violations = validate("500", "400");

        assertEquals(1, violations.size());
        ConstraintViolation<CreateRentalRequestDto> violation = violations.iterator().next();
        assertEquals("budgetMax", violation.getPropertyPath().toString());
        assertEquals("Bugetul minim nu poate fi mai mare decât bugetul maxim.", violation.getMessage());
    }

    @Test
    void comparesNumericallyNotByScale() {
        // BigDecimal.equals would treat 400 and 400.00 as different; compareTo does not
        assertTrue(validate("400.00", "400").isEmpty());
    }

    @Test
    void updateDtoUsesTheSameRule() {
        UpdateRentalRequestDto dto = RentalRequestTestData.updateDto(null, null);
        UpdateRentalRequestDto inverted = new UpdateRentalRequestDto(dto.title(), dto.description(),
                dto.desiredType(), new BigDecimal("700"), new BigDecimal("600"), dto.currency(), dto.city(),
                dto.area(), dto.rooms(), dto.desiredFrom(), null, null);

        Set<ConstraintViolation<UpdateRentalRequestDto>> violations = validator.validate(inverted);

        assertEquals("budgetMax", violations.iterator().next().getPropertyPath().toString());
    }
}
