package ro.rentalplatform.rentalrequest;

import org.springframework.test.util.ReflectionTestUtils;
import ro.rentalplatform.listing.domain.Currency;
import ro.rentalplatform.listing.domain.PropertyType;
import ro.rentalplatform.rentalrequest.domain.RentalRequest;
import ro.rentalplatform.rentalrequest.domain.RequestStatus;
import ro.rentalplatform.rentalrequest.dto.CreateRentalRequestDto;
import ro.rentalplatform.rentalrequest.dto.RentalRequestDto;
import ro.rentalplatform.rentalrequest.dto.UpdateRentalRequestDto;
import ro.rentalplatform.user.domain.Role;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.domain.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Builders shared by the rental request tests. */
final class RentalRequestTestData {

    static final String MAPS_URL = "https://maps.app.goo.gl/AbCdEf123";

    private RentalRequestTestData() {
    }

    static LocalDate nextMonth() {
        return LocalDate.now().plusMonths(1);
    }

    static User tenant(Long id) {
        User user = User.builder()
                .email("tenant" + id + "@test.ro")
                .passwordHash("hashed")
                .firstName("Ana")
                .lastName("Pop")
                .role(Role.TENANT)
                .status(UserStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    static RentalRequest request(Long id, User owner, RequestStatus status) {
        RentalRequest request = RentalRequest.builder()
                .tenant(owner)
                .title("Caut garsonieră")
                .description("Aproape de facultate")
                .desiredType(PropertyType.STUDIO)
                .budgetMin(new BigDecimal("250"))
                .budgetMax(new BigDecimal("400"))
                .currency(Currency.EUR)
                .city("București")
                .desiredFrom(nextMonth())
                .status(status)
                .build();
        ReflectionTestUtils.setField(request, "id", id);
        return request;
    }

    static CreateRentalRequestDto createDto(BigDecimal min, BigDecimal max, String mapsUrl, BigDecimal radiusKm) {
        return new CreateRentalRequestDto(" Caut garsonieră ", " Aproape de facultate ", PropertyType.STUDIO,
                min, max, Currency.EUR, " București ", " Militari ", (short) 1, nextMonth(), mapsUrl, radiusKm);
    }

    static CreateRentalRequestDto createDto(String mapsUrl, BigDecimal radiusKm) {
        return createDto(new BigDecimal("250"), new BigDecimal("400"), mapsUrl, radiusKm);
    }

    static UpdateRentalRequestDto updateDto(String mapsUrl, BigDecimal radiusKm) {
        return new UpdateRentalRequestDto("Caut apartament", "Cu 2 camere", PropertyType.APARTMENT,
                new BigDecimal("400"), new BigDecimal("600"), Currency.EUR, "Cluj-Napoca", null, (short) 2,
                nextMonth(), mapsUrl, radiusKm);
    }

    static RentalRequestDto dto(Long id, RequestStatus status) {
        return new RentalRequestDto(id, "Caut garsonieră", "Aproape de facultate", PropertyType.STUDIO,
                new BigDecimal("250"), new BigDecimal("400"), Currency.EUR, "București", null, (short) 1,
                nextMonth(), status, null, null, null, null, null, null);
    }
}
