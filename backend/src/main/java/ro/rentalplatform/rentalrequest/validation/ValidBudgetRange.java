package ro.rentalplatform.rentalrequest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Verifică că budgetMin ≤ budgetMax pe un DTO de tip clasă.
 * Eroarea se atașează câmpului {@code budgetMax}.
 */
@Documented
@Constraint(validatedBy = BudgetRangeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBudgetRange {

    String message() default "Bugetul minim nu poate fi mai mare decât bugetul maxim.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
