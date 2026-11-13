package ro.rentalplatform.location.controller;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.rentalplatform.location.config.LocationProperties;
import ro.rentalplatform.location.domain.Coordinates;
import ro.rentalplatform.location.dto.CoordinatesDto;
import ro.rentalplatform.location.dto.ParseLocationRequest;
import ro.rentalplatform.location.exception.TooManyRequestsException;
import ro.rentalplatform.location.service.LocationService;
import ro.rentalplatform.security.CurrentUserProvider;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;
    private final CurrentUserProvider currentUserProvider;
    private final LocationProperties locationProperties;
    private final Map<Long, Bucket> buckets = new ConcurrentHashMap<>();

    public LocationController(LocationService locationService,
                              CurrentUserProvider currentUserProvider,
                              LocationProperties locationProperties) {
        this.locationService = locationService;
        this.currentUserProvider = currentUserProvider;
        this.locationProperties = locationProperties;
    }

    @PostMapping("/parse")
    public ResponseEntity<CoordinatesDto> parseLocation(@Valid @RequestBody ParseLocationRequest request) {
        Long userId = currentUserProvider.getCurrentUserId().orElse(0L);
        Bucket bucket = buckets.computeIfAbsent(userId, this::createBucket);
        if (!bucket.tryConsume(1)) {
            throw new TooManyRequestsException("Prea multe cereri de extragere a locației. Încearcă din nou într-un minut.");
        }

        Coordinates coordinates = locationService.parse(request.url());
        return ResponseEntity.ok(CoordinatesDto.from(coordinates));
    }

    private Bucket createBucket(Long userId) {
        int limit = Math.max(1, locationProperties.getRateLimitPerMinute());
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(limit)
                .refillGreedy(limit, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}
