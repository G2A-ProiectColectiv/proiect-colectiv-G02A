package ro.rentalplatform.user.service;

import org.springframework.web.multipart.MultipartFile;
import ro.rentalplatform.user.dto.UpdateProfileRequest;
import ro.rentalplatform.user.dto.UserDto;

public interface UserService {
    UserDto getCurrentUser();
    UserDto updateProfile(UpdateProfileRequest request);
    UserDto updateAvatar(MultipartFile file);
    void removeAvatar();
}
