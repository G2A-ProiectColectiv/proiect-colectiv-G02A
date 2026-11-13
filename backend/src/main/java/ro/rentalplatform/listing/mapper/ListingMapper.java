package ro.rentalplatform.listing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ro.rentalplatform.listing.domain.Amenity;
import ro.rentalplatform.listing.domain.Listing;
import ro.rentalplatform.listing.domain.ListingImage;
import ro.rentalplatform.listing.dto.ListingDto;
import ro.rentalplatform.listing.dto.ListingImageDto;
import ro.rentalplatform.listing.dto.ListingMapMarkerDto;
import ro.rentalplatform.listing.dto.ListingSummaryDto;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ListingMapper {

    @Mapping(target = "amenities", expression = "java(mapAmenities(listing.getAmenities()))")
    @Mapping(target = "images", expression = "java(mapImages(listing.getImages()))")
    ListingDto toDto(Listing listing);

    @Mapping(target = "primaryImage", expression = "java(resolvePrimaryImage(listing.getImages()))")
    ListingSummaryDto toSummaryDto(Listing listing);

    @Mapping(target = "primaryImage", expression = "java(resolvePrimaryImage(listing.getImages()))")
    ListingMapMarkerDto toMarkerDto(Listing listing);

    ListingImageDto toImageDto(ListingImage image);

    default List<Amenity> mapAmenities(Set<Amenity> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return List.of();
        }
        return amenities.stream().toList();
    }

    default List<ListingImageDto> mapImages(List<ListingImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .sorted(Comparator.comparing(ListingImage::getPosition, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ListingImage::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toImageDto)
                .toList();
    }

    default String resolvePrimaryImage(List<ListingImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .filter(ListingImage::isPrimary)
                .map(ListingImage::getUrl)
                .findFirst()
                .orElseGet(() -> images.stream()
                        .sorted(Comparator.comparing(ListingImage::getPosition, Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(ListingImage::getUrl)
                        .findFirst()
                        .orElse(null));
    }
}
