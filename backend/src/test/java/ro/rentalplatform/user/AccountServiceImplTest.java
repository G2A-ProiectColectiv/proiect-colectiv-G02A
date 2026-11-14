package ro.rentalplatform.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import ro.rentalplatform.common.exception.ForbiddenOperationException;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.security.CurrentUserProvider;
import ro.rentalplatform.user.domain.Role;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.domain.UserStatus;
import ro.rentalplatform.user.event.UserDeletedEvent;
import ro.rentalplatform.user.repository.UserRepository;
import ro.rentalplatform.user.service.AccountServiceImpl;
import ro.rentalplatform.user.service.UserDataCleaner;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    private static final Long USER_ID = 7L;

    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserDataCleaner tokenCleaner;
    @Mock
    private UserDataCleaner listingCleaner;

    private AccountServiceImpl accountService;
    private User user;

    @BeforeEach
    void setUp() {
        // built by hand: @InjectMocks cannot fill a List<UserDataCleaner>
        accountService = new AccountServiceImpl(userRepository, currentUserProvider, passwordEncoder,
                List.of(tokenCleaner, listingCleaner), eventPublisher);

        user = User.builder()
                .email("ana@test.ro")
                .passwordHash("hashed")
                .firstName("Ana")
                .lastName("Pop")
                .phone("0712345678")
                .avatarUrl("avatars/ana.png")
                .bio("Salut!")
                .role(Role.TENANT)
                .status(UserStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
    }

    private void loggedInWithPassword(String rawPassword, boolean correct) {
        when(currentUserProvider.getCurrentUserId()).thenReturn(Optional.of(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, "hashed")).thenReturn(correct);
        if (correct) {
            when(passwordEncoder.encode(anyString())).thenReturn("new-hash");
        }
    }

    @Test
    void wrongPasswordIsForbiddenAndChangesNothing() {
        loggedInWithPassword("wrong", false);

        assertThrows(ForbiddenOperationException.class, () -> accountService.deleteOwnAccount("wrong"));

        // nothing is cleaned, anonymised or announced
        verify(tokenCleaner, never()).cleanUp(anyLong());
        verify(listingCleaner, never()).cleanUp(anyLong());
        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
        assertEquals("ana@test.ro", user.getEmail());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void deleteCallsEveryCleanerBeforeSavingTheUser() {
        loggedInWithPassword("parola123", true);

        accountService.deleteOwnAccount("parola123");

        InOrder order = inOrder(tokenCleaner, listingCleaner, userRepository);
        order.verify(tokenCleaner).cleanUp(USER_ID);
        order.verify(listingCleaner).cleanUp(USER_ID);
        order.verify(userRepository).save(user);
    }

    @Test
    void deleteAnonymisesTheAccount() {
        loggedInWithPassword("parola123", true);

        accountService.deleteOwnAccount("parola123");

        assertEquals(UserStatus.DELETED, user.getStatus());
        assertEquals("deleted_7@removed.local", user.getEmail());
        assertEquals("Utilizator", user.getFirstName());
        assertEquals("șters", user.getLastName());
        assertNull(user.getPhone());
        assertNull(user.getAvatarUrl());
        assertNull(user.getBio());
        assertEquals("new-hash", user.getPasswordHash());
    }

    @Test
    void deletePublishesEventWithOldAvatarPath() {
        loggedInWithPassword("parola123", true);

        accountService.deleteOwnAccount("parola123");

        // the listener deletes the file after commit, so the event must carry the old path
        ArgumentCaptor<UserDeletedEvent> captor = ArgumentCaptor.forClass(UserDeletedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(USER_ID, captor.getValue().userId());
        assertEquals("avatars/ana.png", captor.getValue().avatarPath());
    }

    @Test
    void deleteFailsWhenNobodyIsAuthenticated() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> accountService.deleteOwnAccount("parola123"));
    }
}
