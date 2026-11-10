package ro.rentalplatform.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ro.rentalplatform.auth.dto.AuthResponse;
import ro.rentalplatform.auth.dto.LoginRequest;
import ro.rentalplatform.auth.dto.RegisterRequest;
import ro.rentalplatform.auth.service.AuthServiceImpl;
import ro.rentalplatform.common.exception.BadCredentialsAppException;
import ro.rentalplatform.common.exception.BadRequestException;
import ro.rentalplatform.common.exception.ConflictException;
import ro.rentalplatform.security.JwtService;
import ro.rentalplatform.user.domain.Role;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.domain.UserStatus;
import ro.rentalplatform.user.mapper.UserMapper;
import ro.rentalplatform.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private static RegisterRequest registerRequest(String email, String role) {
        return new RegisterRequest(
            email,
            "parola123",
            "Ana",
            "Pop",
            null,
            null,
            null,
            role
        );
    }

    private static User activeUser(String email, String passwordHash) {
        return User.builder()
            .email(email)
            .passwordHash(passwordHash)
            .role(Role.TENANT)
            .status(UserStatus.ACTIVE)
            .build();
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("ana@test.ro")).thenReturn(true);
        RegisterRequest request = registerRequest("ana@test.ro","TENANT");

        assertThrows(ConflictException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerAdminRejected() {
        RegisterRequest request = registerRequest("ana@test.ro","ADMIN");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.register(request));
        assertEquals("role", ex.getFieldErrors().getFirst().field());
    }

    @Test
    void unknownRole() {
        RegisterRequest request = registerRequest("ana@test.ro","FOO");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.register(request));
        assertEquals("role", ex.getFieldErrors().getFirst().field());
    }

    @Test
    void successfulRegister() {
        when(passwordEncoder.encode("parola123")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest registerRequest = registerRequest("   Ana@Test.RO", "tenant");
        authService.register(registerRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("ana@test.ro", saved.getEmail());
        assertEquals("hashed", saved.getPasswordHash());
        assertEquals(Role.TENANT, saved.getRole());
        assertEquals(UserStatus.ACTIVE, saved.getStatus());
    }

    @Test
    void loginRejectsWrongPassword() {
        when(userRepository.findByEmail("ana@test.ro"))
            .thenReturn(Optional.of(activeUser("ana@test.ro", "hashed")));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        LoginRequest request = new LoginRequest("ana@test.ro", "wrong");

        assertThrows(BadCredentialsAppException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void loginRejectsUnknownEmail() {
        LoginRequest request = new LoginRequest("unknown@mail.ro", "random");

        assertThrows(BadCredentialsAppException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void sameErrorMessage() {
        LoginRequest unknownMailRequest = new LoginRequest("unknown@mail.ro", "random");

        BadCredentialsAppException unknownMail = assertThrows(BadCredentialsAppException.class, () -> authService.login(unknownMailRequest));
        verify(jwtService, never()).generateToken(any());

        when(userRepository.findByEmail("ana@test.ro"))
            .thenReturn(Optional.of(activeUser("ana@test.ro", "hashed")));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        LoginRequest wrongPasswordRequest = new LoginRequest("ana@test.ro", "wrong");

        BadCredentialsAppException wrongPassword = assertThrows(BadCredentialsAppException.class, () -> authService.login(wrongPasswordRequest));
        verify(jwtService, never()).generateToken(any());

        assertEquals(wrongPassword.getMessage(), unknownMail.getMessage());
    }

    @Test
    void rejectBlockedUser() {
        User blocked = activeUser("blocked@mail.ro", "hashed");
        blocked.setStatus(UserStatus.BLOCKED);

        when(userRepository.findByEmail("blocked@mail.ro")).thenReturn(Optional.of(blocked));
        when(passwordEncoder.matches("parola123", "hashed")).thenReturn(true);

        LoginRequest loginRequest = new LoginRequest("blocked@mail.ro", "parola123");

        BadCredentialsAppException ex = assertThrows(BadCredentialsAppException.class, () -> authService.login(loginRequest));
        verify(jwtService, never()).generateToken(any());

        assertEquals("Contul nu este activ.", ex.getMessage());
    }

    @Test
    void successfulLogin() {
        when(userRepository.findByEmail("ana@test.ro")).thenReturn(Optional.of(activeUser("ana@test.ro", "hashed")));
        when(passwordEncoder.matches("parola123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        LoginRequest request = new LoginRequest("ANA@test.ro", "parola123");

        AuthResponse authResponse = authService.login(request);
        assertEquals("jwt-token", authResponse.token());
    }
}
