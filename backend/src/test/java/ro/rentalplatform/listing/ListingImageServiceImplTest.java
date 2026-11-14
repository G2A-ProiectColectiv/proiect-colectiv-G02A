package ro.rentalplatform.listing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ro.rentalplatform.common.exception.BadRequestException;
import ro.rentalplatform.common.exception.ForbiddenOperationException;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.common.security.OwnershipVerifier;
import ro.rentalplatform.listing.domain.Listing;
import ro.rentalplatform.listing.domain.ListingImage;
import ro.rentalplatform.listing.domain.ListingStatus;
import ro.rentalplatform.listing.mapper.ListingMapper;
import ro.rentalplatform.listing.repository.ListingImageRepository;
import ro.rentalplatform.listing.repository.ListingRepository;
import ro.rentalplatform.listing.service.ListingImageServiceImpl;
import ro.rentalplatform.security.CurrentUserProvider;
import ro.rentalplatform.storage.ImageStorageService;
import ro.rentalplatform.storage.InvalidImageException;
import ro.rentalplatform.storage.StoredImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ro.rentalplatform.listing.ListingTestData.image;
import static ro.rentalplatform.listing.ListingTestData.landlord;
import static ro.rentalplatform.listing.ListingTestData.listing;

@ExtendWith(MockitoExtension.class)
class ListingImageServiceImplTest {

    private static final Long OWNER_ID = 1L;
    private static final Long LISTING_ID = 10L;
    private static final String FOLDER = "listings/10";

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private ListingImageRepository imageRepository;
    @Mock
    private ImageStorageService storage;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private ListingMapper listingMapper;

    private ListingImageServiceImpl service;
    private Listing listing;

    @BeforeEach
    void setUp() {
        service = new ListingImageServiceImpl(listingRepository, imageRepository, storage, currentUserProvider,
                new OwnershipVerifier(), listingMapper);
        listing = listing(LISTING_ID, landlord(OWNER_ID), ListingStatus.ACTIVE);
    }

    private void loggedInAsOwner() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(Optional.of(OWNER_ID));
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
    }

    private void existingImages(List<ListingImage> images) {
        when(imageRepository.findByListingIdOrderByPositionAscIdAsc(LISTING_ID)).thenReturn(images);
    }

    private static List<MultipartFile> files(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> (MultipartFile) new MockMultipartFile("files", "p" + i + ".jpg", "image/jpeg",
                        new byte[] {1}))
                .toList();
    }

    /** Every store() call returns a distinct path, like the real storage does. */
    private void storageReturnsDistinctPaths() {
        int[] counter = {0};
        when(storage.store(any(), eq(FOLDER))).thenAnswer(inv -> new StoredImage(FOLDER + "/new" + counter[0]++ + ".jpg"));
    }

    private List<ListingImage> savedImages(int expectedCount) {
        ArgumentCaptor<ListingImage> captor = ArgumentCaptor.forClass(ListingImage.class);
        verify(imageRepository, times(expectedCount)).save(captor.capture());
        return captor.getAllValues();
    }

    // ---- addImages ----

    @Test
    void firstUploadedImageBecomesPrimary() {
        // POZE-02
        loggedInAsOwner();
        existingImages(new ArrayList<>());
        storageReturnsDistinctPaths();

        service.addImages(LISTING_ID, files(3));

        List<ListingImage> saved = savedImages(3);
        assertTrue(saved.get(0).isPrimary());
        assertFalse(saved.get(1).isPrimary());
        assertFalse(saved.get(2).isPrimary());
        assertEquals(List.of((short) 0, (short) 1, (short) 2), saved.stream().map(ListingImage::getPosition).toList());
        assertEquals(FOLDER + "/new0.jpg", saved.get(0).getUrl());
    }

    @Test
    void newImagesAreAppendedAfterExistingOnesWithoutStealingPrimary() {
        loggedInAsOwner();
        existingImages(List.of(image(1L, listing, 0, true), image(2L, listing, 1, false)));
        storageReturnsDistinctPaths();

        service.addImages(LISTING_ID, files(2));

        List<ListingImage> saved = savedImages(2);
        assertFalse(saved.get(0).isPrimary());
        assertEquals((short) 2, saved.get(0).getPosition());
        assertEquals((short) 3, saved.get(1).getPosition());
    }

    @Test
    void moreThanTenImagesAreRejectedBeforeStoringAnything() {
        // POZE-01
        loggedInAsOwner();
        List<ListingImage> nine = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            nine.add(image((long) i, listing, i, i == 0));
        }
        existingImages(nine);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.addImages(LISTING_ID, files(2)));

        assertTrue(ex.getMessage().contains("10"));
        verify(storage, never()).store(any(), anyString());
    }

    @Test
    void emptyUploadIsRejected() {
        assertThrows(BadRequestException.class, () -> service.addImages(LISTING_ID, List.of()));
        assertThrows(BadRequestException.class, () -> service.addImages(LISTING_ID, null));
    }

    @Test
    void failedUploadRemovesFilesAlreadyStoredInTheSameRequest() {
        loggedInAsOwner();
        existingImages(new ArrayList<>());
        when(storage.store(any(), eq(FOLDER)))
                .thenReturn(new StoredImage(FOLDER + "/ok.jpg"))
                .thenThrow(new InvalidImageException("Fișierul nu este o imagine validă."));

        assertThrows(InvalidImageException.class, () -> service.addImages(LISTING_ID, files(2)));

        // no orphan files left on disk
        verify(storage).delete(FOLDER + "/ok.jpg");
    }

    @Test
    void uploadToAnotherLandlordsListingIsForbidden() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(Optional.of(2L));
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));

        assertThrows(ForbiddenOperationException.class, () -> service.addImages(LISTING_ID, files(1)));
        verify(storage, never()).store(any(), anyString());
    }

    @Test
    void uploadToDeletedListingIsNotFound() {
        listing.setStatus(ListingStatus.DELETED);
        loggedInAsOwner();

        assertThrows(NotFoundException.class, () -> service.addImages(LISTING_ID, files(1)));
    }

    // ---- removeImage ----

    @Test
    void removingPrimaryMakesNextImagePrimaryAndDeletesFile() {
        // POZE-03 + POZE-05
        loggedInAsOwner();
        ListingImage first = image(1L, listing, 0, true);
        ListingImage second = image(2L, listing, 1, false);
        ListingImage third = image(3L, listing, 2, false);
        listing.getImages().addAll(List.of(first, second, third));
        when(imageRepository.findByIdAndListingId(1L, LISTING_ID)).thenReturn(Optional.of(first));
        existingImages(List.of(second, third));

        service.removeImage(LISTING_ID, 1L);

        assertTrue(second.isPrimary());
        assertFalse(third.isPrimary());
        assertEquals((short) 0, second.getPosition());
        assertEquals((short) 1, third.getPosition());
        verify(imageRepository).delete(first);
        verify(storage).delete(first.getUrl());
    }

    @Test
    void removingNonPrimaryKeepsPrimary() {
        loggedInAsOwner();
        ListingImage first = image(1L, listing, 0, true);
        ListingImage second = image(2L, listing, 1, false);
        when(imageRepository.findByIdAndListingId(2L, LISTING_ID)).thenReturn(Optional.of(second));
        existingImages(List.of(first));

        service.removeImage(LISTING_ID, 2L);

        assertTrue(first.isPrimary());
        verify(storage).delete(second.getUrl());
    }

    @Test
    void removingUnknownImageIsNotFound() {
        loggedInAsOwner();
        when(imageRepository.findByIdAndListingId(99L, LISTING_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.removeImage(LISTING_ID, 99L));
        verify(storage, never()).delete(anyString());
    }

    // ---- setPrimary ----

    @Test
    void setPrimaryMovesPrimaryFlag() {
        loggedInAsOwner();
        ListingImage first = image(1L, listing, 0, true);
        ListingImage second = image(2L, listing, 1, false);
        existingImages(List.of(first, second));

        service.setPrimary(LISTING_ID, 2L);

        assertFalse(first.isPrimary());
        assertTrue(second.isPrimary());
    }

    @Test
    void setPrimaryOnImageOfAnotherListingIsNotFound() {
        loggedInAsOwner();
        existingImages(List.of(image(1L, listing, 0, true)));

        assertThrows(NotFoundException.class, () -> service.setPrimary(LISTING_ID, 99L));
    }

    // ---- reorder ----

    @Test
    void reorderAssignsPositionsInGivenOrder() {
        // POZE-04
        loggedInAsOwner();
        ListingImage a = image(1L, listing, 0, true);
        ListingImage b = image(2L, listing, 1, false);
        ListingImage c = image(3L, listing, 2, false);
        existingImages(List.of(a, b, c));

        service.reorder(LISTING_ID, List.of(3L, 1L, 2L));

        assertEquals((short) 0, c.getPosition());
        assertEquals((short) 1, a.getPosition());
        assertEquals((short) 2, b.getPosition());
    }

    @Test
    void reorderMustContainExactlyAllImages() {
        loggedInAsOwner();
        existingImages(List.of(image(1L, listing, 0, true), image(2L, listing, 1, false)));

        assertThrows(BadRequestException.class, () -> service.reorder(LISTING_ID, List.of(1L)));
        assertThrows(BadRequestException.class, () -> service.reorder(LISTING_ID, List.of(1L, 1L)));
        assertThrows(BadRequestException.class, () -> service.reorder(LISTING_ID, List.of(1L, 99L)));
    }
}
