package ro.rentalplatform.user.event;

public record UserDeletedEvent(Long userId, String avatarPath) {
}
