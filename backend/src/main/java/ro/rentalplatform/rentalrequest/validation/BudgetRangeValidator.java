package ro.rentalplatform.rentalrequest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

/**
 * Validatorul asociat adnotării {@link ValidBudgetRange}.
 * <p>
 * Strategia: implementează {@link ConstraintValidator} la nivel de clasă
 * (obiectul validat trebuie să expună getBudgetMin() / getBudgetMax()).
 * Eroarea este atașată câmpului {@code budgetMax} pentru ca interfața
 * să o poată afișa lângă câmpul corespunzător (CER-04).
 */
public class BudgetRangeValidator implements ConstraintValidator<ValidBudgetRange, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext ctx) {
        if (!(value instanceof BudgetRangeHolder holder)) {
            return true;
        }

        BigDecimal min = holder.getBudgetMin();
        BigDecimal max = holder.getBudgetMax();

        if (min == null || max == null) {
            return true; // câmpurile individuale au deja @NotNull
        }

        boolean valid = min.compareTo(max) <= 0;

        if (!valid) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(ctx.getDefaultConstraintMessageTemplate())
               .addPropertyNode("budgetMax")
               .addConstraintViolation();
        }

        return valid;
    }
}
