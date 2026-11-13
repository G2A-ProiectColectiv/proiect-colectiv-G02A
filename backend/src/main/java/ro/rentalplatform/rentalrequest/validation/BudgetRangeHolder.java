package ro.rentalplatform.rentalrequest.validation;

import java.math.BigDecimal;

/**
 * Interfață marker pentru obiectele care pot fi validate cu {@link ValidBudgetRange}.
 * Atât {@code CreateRentalRequestDto} cât și {@code UpdateRentalRequestDto} o implementează.
 */
public interface BudgetRangeHolder {
    BigDecimal getBudgetMin();
    BigDecimal getBudgetMax();
}
