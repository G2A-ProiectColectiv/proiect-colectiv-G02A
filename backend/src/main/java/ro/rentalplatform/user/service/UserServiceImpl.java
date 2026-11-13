package ro.rentalplatform.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ro.rentalplatform.common.exception.NotFoundException;
import ro.rentalplatform.security.CurrentUserProvider;
import ro.rentalplatform.storage.ImageStorageService;
import ro.rentalplatform.storage.StoredImage;
import ro.rentalplatform.user.domain.User;
import ro.rentalplatform.user.dto.UpdateProfileRequest;
import ro.rentalplatform.user.dto.UserDto;
import ro.rentalplatform.user.mapper.UserMapper;
import ro.rentalplatform.user.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CurrentUserProvider currentUserProvider;
    private final ImageStorageService imageStorageService;

    public UserServiceImpl(UserRepository userRepository,
                          UserMapper userMapper,
                          CurrentUserProvider currentUserProvider,
                          ImageStorageService imageStorageService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.currentUserProvider = currentUserProvider;
        this.imageStorageService = imageStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        User user = getCurrentUserEntity();
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUserEntity();

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(request.phone() == null ? null : request.phone().trim());
        user.setBio(request.bio() == null ? null : request.bio().trim());

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto updateAvatar(MultipartFile file) {
        User user = getCurrentUserEntity();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fișierul este gol.");
        }

        String oldAvatar = user.getAvatarUrl();
        StoredImage storedImage = imageStorageService.store(file, "avatars");
        user.setAvatarUrl(storedImage.relativePath());
        userRepository.save(user);

        if (oldAvatar != null && !oldAvatar.isBlank()) {
            imageStorageService.delete(oldAvatar);
        }

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void removeAvatar() {
        User user = getCurrentUserEntity();
        String oldAvatar = user.getAvatarUrl();
        user.setAvatarUrl(null);
        userRepository.save(user);

        if (oldAvatar != null && !oldAvatar.isBlank()) {
            imageStorageService.delete(oldAvatar);
        }
    }

    private User getCurrentUserEntity() {
        Long userId = currentUserProvider.getCurrentUserId()
                .orElseThrow(() -> new NotFoundException("Utilizatorul nu a fost găsit."));

        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilizatorul nu a fost găsit."));
    }
}
