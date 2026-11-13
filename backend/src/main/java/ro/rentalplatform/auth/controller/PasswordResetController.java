package ro.rentalplatform.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.rentalplatform.auth.dto.PasswordResetConfirmRequest;
import ro.rentalplatform.auth.dto.PasswordResetRequest;
import ro.rentalplatform.auth.service.PasswordResetService;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestPasswordReset(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.confirmPasswordReset(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
