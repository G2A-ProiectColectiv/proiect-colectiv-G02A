package ro.rentalplatform.listing.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ro.rentalplatform.common.exception.BadRequestException;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.common.security.OwnershipVerifier;
import ro.rentalplatform.listing.domain.Listing;
import ro.rentalplatform.listing.domain.ListingImage;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.dto.ListingImageDto;
import ro.rentalplatform.listing.mapper.ListingMapper;
import ro.rentalplatform.listing.repository.ListingImageRepository;
import ro.rentalplatform.listing.repository.ListingRepository;
import ro.rentalplatform.security.CurrentUserProvider;
import ro.rentalplatform.storage.ImageStorageService;
import ro.rentalplatform.storage.StoredImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class ListingImageServiceImpl implements ListingImageService {

    public static final int MAX_IMAGES_PER_LISTING = 10;

    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final ImageStorageService imageStorageService;
    private final CurrentUserProvider currentUserProvider;
    private final OwnershipVerifier ownershipVerifier;
    private final ListingMapper listingMapper;

    public ListingImageServiceImpl(ListingRepository listingRepository,
                                   ListingImageRepository listingImageRepository,
                                   ImageStorageService imageStorageService,
                                   CurrentUserProvider currentUserProvider,
                                   OwnershipVerifier ownershipVerifier,
                                   ListingMapper listingMapper) {
        this.listingRepository = listingRepository;
        this.listingImageRepository = listingImageRepository;
        this.imageStorageService = imageStorageService;
        this.currentUserProvider = currentUserProvider;
        this.ownershipVerifier = ownershipVerifier;
        this.listingMapper = listingMapper;
    }

    @Override
    @Transactional
    public List<ListingImageDto> addImages(Long listingId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("Selectează cel puțin o poză.");
        }
        Listing listing = findOwnedListing(listingId);

        List<ListingImage> existing = listingImageRepository.findByListingIdOrderByPositionAscIdAsc(listingId);
        if (existing.size() + files.size() > MAX_IMAGES_PER_LISTING) {
            throw new BadRequestException(
                    "Un anunț poate avea cel mult " + MAX_IMAGES_PER_LISTING + " poze."
            );
        }

        boolean hasPrimary = existing.stream().anyMatch(ListingImage::isPrimary);
        int nextPosition = existing.stream()
                .mapToInt(img -> img.getPosition() != null ? img.getPosition() : 0)
                .max()
                .orElse(-1) + 1;

        String folder = "listings/" + listingId;
        List<String> storedPaths = new ArrayList<>();
        try {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                StoredImage stored = imageStorageService.store(file, folder);
                storedPaths.add(stored.relativePath());

                boolean primary = !hasPrimary && i == 0;
                ListingImage image = ListingImage.builder()
                        .listing(listing)
                        .url(stored.relativePath())
                        .primary(primary)
                        .position((short) (nextPosition + i))
                        .build();
                listing.getImages().add(image);
                listingImageRepository.save(image);
            }
            listingImageRepository.flush();
        } catch (RuntimeException ex) {
            for (String path : storedPaths) {
                try {
                    imageStorageService.delete(path);
                } catch (RuntimeException ignored) {
                    // curățare best-effort
                }
            }
            throw ex;
        }

        return getOrderedDtos(listingId);
    }

    @Override
    @Transactional
    public void removeImage(Long listingId, Long imageId) {
        Listing listing = findOwnedListing(listingId);
        ListingImage target = listingImageRepository.findByIdAndListingId(imageId, listingId)
                .orElseThrow(() -> new NotFoundException("Poza nu a fost găsită."));

        boolean wasPrimary = target.isPrimary();
        String pathToDelete = target.getUrl();

        listing.getImages().removeIf(img -> imageId.equals(img.getId()));
        listingImageRepository.delete(target);
        listingImageRepository.flush();

        List<ListingImage> remaining = listingImageRepository.findByListingIdOrderByPositionAscIdAsc(listingId);
        for (int i = 0; i < remaining.size(); i++) {
            ListingImage img = remaining.get(i);
            img.setPosition((short) i);
            if (wasPrimary && i == 0) {
                img.setPrimary(true);
            }
        }
        listingImageRepository.saveAll(remaining);
        listingImageRepository.flush();

        imageStorageService.delete(pathToDelete);
    }

    @Override
    @Transactional
    public List<ListingImageDto> setPrimary(Long listingId, Long imageId) {
        findOwnedListing(listingId);
        List<ListingImage> images = listingImageRepository.findByListingIdOrderByPositionAscIdAsc(listingId);

        ListingImage newPrimary = images.stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Poza nu a fost găsită."));

        if (!newPrimary.isPrimary()) {
            for (ListingImage img : images) {
                if (img.isPrimary()) {
                    img.setPrimary(false);
                    listingImageRepository.save(img);
                }
            }
            listingImageRepository.flush();

            newPrimary.setPrimary(true);
            listingImageRepository.save(newPrimary);
            listingImageRepository.flush();
        }

        return getOrderedDtos(listingId);
    }

    @Override
    @Transactional
    public List<ListingImageDto> reorder(Long listingId, List<Long> imageIds) {
        findOwnedListing(listingId);
        List<ListingImage> images = listingImageRepository.findByListingIdOrderByPositionAscIdAsc(listingId);

        if (imageIds == null
                || imageIds.size() != images.size()
                || new HashSet<>(imageIds).size() != images.size()) {
            throw new BadRequestException("Lista cu ordinea pozelor trebuie să conțină exact toate pozele anunțului.");
        }

        Map<Long, ListingImage> byId = new HashMap<>();
        for (ListingImage img : images) {
            byId.put(img.getId(), img);
        }

        for (int i = 0; i < imageIds.size(); i++) {
            ListingImage img = byId.get(imageIds.get(i));
            if (img == null) {
                throw new BadRequestException("Poza nu aparține acestui anunț.");
            }
            img.setPosition((short) i);
        }
        listingImageRepository.saveAll(images);
        listingImageRepository.flush();

        return getOrderedDtos(listingId);
    }

    private List<ListingImageDto> getOrderedDtos(Long listingId) {
        return listingImageRepository.findByListingIdOrderByPositionAscIdAsc(listingId)
                .stream()
                .map(listingMapper::toImageDto)
                .toList();
    }

    private Listing findOwnedListing(Long listingId) {
        Long currentUserId = currentUserProvider.getCurrentUserId()
                .orElseThrow(() -> new NotFoundException("Utilizatorul nu a fost găsit."));
        Listing listing = listingRepository.findById(listingId)
                .filter(candidate -> candidate.getStatus() != ListingStatus.DELETED)
                .orElseThrow(() -> new NotFoundException("Anunțul nu a fost găsit."));
        ownershipVerifier.verify(listing, currentUserId);
        return listing;
    }
}
