package ro.rentalplatform.common.domain;

public interface StatusTransitionPolicy<S extends Enum<S>> {
    boolean isAllowed(S from, S to);
}
