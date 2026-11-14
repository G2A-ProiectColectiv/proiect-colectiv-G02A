package ro.rentalplatform.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.security.CurrentUserProvider;
import ro.rentalplatform.storage.ImageStorageService;
import ro.rentalplatform.storage.InvalidImageException;
import ro.rentalplatform.storage.StoredImage;
import ro.rentalplatform.user.domain.Role;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.domain.UserStatus;
import ro.rentalplatform.user.dto.UpdateProfileRequest;
import ro.rentalplatform.user.dto.UserDto;
import ro.rentalplatform.user.mapper.UserMapper;
import ro.rentalplatform.user.repository.UserRepository;
import ro.rentalplatform.user.service.UserServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final Long USER_ID = 7L;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("ana@test.ro")
                .passwordHash("hashed")
                .firstName("Ana")
                .lastName("Pop")
                .role(Role.TENANT)
                .status(UserStatus.ACTIVE)
                .build();
        // BaseEntity has no setter for id; the database normally assigns it
        ReflectionTestUtils.setField(user, "id", USER_ID);
    }

    private void loggedInAsUser() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    private static MockMultipartFile image() {
        return new MockMultipartFile("file", "me.png", "image/png", new byte[] {1, 2, 3});
    }

    // ---- getCurrentUser ----

    @Test
    void getCurrentUserReturnsMappedLoggedInUser() {
        loggedInAsUser();
        UserDto dto = new UserDto(USER_ID, "ana@test.ro", "Ana", "Pop", null, null, null, "TENANT", "ACTIVE");
        when(userMapper.toDto(user)).thenReturn(dto);

        assertSame(dto, userService.getCurrentUser());
    }

    @Test
    void getCurrentUserFailsWhenNobodyIsAuthenticated() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getCurrentUser());
    }

    @Test
    void getCurrentUserFailsWhenUserNoLongerExists() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getCurrentUser());
    }

    // ---- updateProfile ----

    @Test
    void updateProfileTrimsAndSavesEditableFieldsOnly() {
        loggedInAsUser();
        when(userRepository.save(user)).thenReturn(user);

        userService.updateProfile(new UpdateProfileRequest("  Maria ", " Ionescu ", " 0712345678 ", "  Salut! "));

        assertEquals("Maria", user.getFirstName());
        assertEquals("Ionescu", user.getLastName());
        assertEquals("0712345678", user.getPhone());
        assertEquals("Salut!", user.getBio());
        // email and role cannot be changed through the profile
        assertEquals("ana@test.ro", user.getEmail());
        assertEquals(Role.TENANT, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void updateProfileClearsOptionalFieldsWhenNull() {
        user.setPhone("0712345678");
        user.setBio("old bio");
        loggedInAsUser();
        when(userRepository.save(user)).thenReturn(user);

        userService.updateProfile(new UpdateProfileRequest("Ana", "Pop", null, null));

        assertNull(user.getPhone());
        assertNull(user.getBio());
    }

    // ---- updateAvatar ----

    @Test
    void updateAvatarStoresNewImageSavesPathThenDeletesOldOne() {
        user.setAvatarUrl("avatars/old.png");
        loggedInAsUser();
        MockMultipartFile file = image();
        when(imageStorageService.store(file, "avatars")).thenReturn(new StoredImage("avatars/new.png"));

        userService.updateAvatar(file);

        assertEquals("avatars/new.png", user.getAvatarUrl());
        // the old picture is deleted only after the new one is stored and saved
        InOrder order = inOrder(imageStorageService, userRepository);
        order.verify(imageStorageService).store(file, "avatars");
        order.verify(userRepository).save(user);
        order.verify(imageStorageService).delete("avatars/old.png");
    }

    @Test
    void updateAvatarWithoutPreviousAvatarDeletesNothing() {
        loggedInAsUser();
        MockMultipartFile file = image();
        when(imageStorageService.store(file, "avatars")).thenReturn(new StoredImage("avatars/new.png"));

        userService.updateAvatar(file);

        assertEquals("avatars/new.png", user.getAvatarUrl());
        verify(imageStorageService, never()).delete(anyString());
    }

    @Test
    void updateAvatarKeepsOldAvatarWhenNewImageIsInvalid() {
        user.setAvatarUrl("avatars/old.png");
        loggedInAsUser();
        MockMultipartFile file = image();
        when(imageStorageService.store(file, "avatars")).thenThrow(new InvalidImageException("invalid"));

        assertThrows(InvalidImageException.class, () -> userService.updateAvatar(file));

        assertEquals("avatars/old.png", user.getAvatarUrl());
        verify(userRepository, never()).save(any());
        verify(imageStorageService, never()).delete(anyString());
    }

    // ---- removeAvatar ----

    @Test
    void removeAvatarClearsPathAndDeletesFile() {
        user.setAvatarUrl("avatars/old.png");
        loggedInAsUser();

        userService.removeAvatar();

        assertNull(user.getAvatarUrl());
        verify(userRepository).save(user);
        verify(imageStorageService).delete("avatars/old.png");
    }

    @Test
    void removeAvatarWithoutAvatarDeletesNothing() {
        loggedInAsUser();

        userService.removeAvatar();

        verify(imageStorageService, never()).delete(anyString());
    }
}
