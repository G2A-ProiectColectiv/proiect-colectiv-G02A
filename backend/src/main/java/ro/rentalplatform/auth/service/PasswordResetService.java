package ro.rentalplatform.auth.service;

public interface PasswordResetService {
    void requestPasswordReset(String email);
    void confirmPasswordReset(String token, String newPassword);
}
