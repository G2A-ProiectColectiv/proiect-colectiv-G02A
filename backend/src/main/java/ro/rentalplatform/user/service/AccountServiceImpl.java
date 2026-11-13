package ro.rentalplatform.user.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.rentalplatform.common.exception.ForbiddenOperationException;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.security.CurrentUserProvider;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.domain.UserStatus;
import ro.rentalplatform.user.event.UserDeletedEvent;
import ro.rentalplatform.user.repository.UserRepository;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PasswordEncoder passwordEncoder;
    private final List<UserDataCleaner> userDataCleaners;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AccountServiceImpl(UserRepository userRepository,
                              CurrentUserProvider currentUserProvider,
                              PasswordEncoder passwordEncoder,
                              List<UserDataCleaner> userDataCleaners,
                              ApplicationEventPublisher applicationEventPublisher) {
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.passwordEncoder = passwordEncoder;
        this.userDataCleaners = userDataCleaners;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Transactional
    public void deleteOwnAccount(String rawPassword) {
        Long userId = currentUserProvider.getCurrentUserId()
                .orElseThrow(() -> new NotFoundException("Utilizatorul nu a fost găsit."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilizatorul nu a fost găsit."));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new ForbiddenOperationException("Parola este incorectă.");
        }

        String avatarPath = user.getAvatarUrl();
        userDataCleaners.forEach(cleaner -> cleaner.cleanUp(user.getId()));

        user.setStatus(UserStatus.DELETED);
        user.setEmail("deleted_" + user.getId() + "@removed.local");
        user.setFirstName("Utilizator");
        user.setLastName("șters");
        user.setPhone(null);
        user.setAvatarUrl(null);
        user.setBio(null);
        user.setPasswordHash(passwordEncoder.encode("deleted-user"));
        userRepository.save(user);

        applicationEventPublisher.publishEvent(new UserDeletedEvent(user.getId(), avatarPath));
    }
}
