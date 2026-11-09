package ro.rentalplatform.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.rentalplatform.auth.dto.AuthResponse;
import ro.rentalplatform.auth.dto.LoginRequest;
import ro.rentalplatform.auth.dto.RegisterRequest;
import ro.rentalplatform.common.exception.BadCredentialsAppException;
import ro.rentalplatform.common.exception.BadRequestException;
import ro.rentalplatform.common.exception.ConflictException;
import ro.rentalplatform.common.exception.FieldErrorDto;
import ro.rentalplatform.security.JwtService;
import ro.rentalplatform.security.UserPrincipal;
import ro.rentalplatform.user.domain.Role;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.domain.UserStatus;
import ro.rentalplatform.user.dto.UserDto;
import ro.rentalplatform.user.mapper.UserMapper;
import ro.rentalplatform.user.repository.UserRepository;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthServiceImpl(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserDto register(RegisterRequest request) {
        Role role = parseSelfRegisterRole(request.role());
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Există deja un cont cu acest email.");
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .phone(request.phone())
                .avatarUrl(request.avatarUrl())
                .bio(request.bio())
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        return userMapper.toDto(userRepository.save(user));
    }

    /** La înregistrare se pot alege doar TENANT și LANDLORD; ADMIN și rolurile necunoscute dau 400 (AUTH-04). */
    private Role parseSelfRegisterRole(String value) {
        Role role;
        try {
            role = Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw invalidRole("Rolul nu este valid.");
        }

        if (role == Role.ADMIN) {
            throw invalidRole("Rolul ADMIN nu poate fi ales la înregistrare.");
        }
        return role;
    }

    private static BadRequestException invalidRole(String message) {
        return new BadRequestException(
                "Datele trimise nu sunt valide.",
                List.of(new FieldErrorDto("role", message)));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsAppException("Email-ul sau parola sunt incorecte."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsAppException("Email-ul sau parola sunt incorecte.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsAppException("Contul nu este activ.");
        }

        UserDto dto = userMapper.toDto(user);
        String token = jwtService.generateToken(new UserPrincipal(user.getId(), user.getEmail(), user.getRole().name()));

        return new AuthResponse(token, dto);
    }
}
