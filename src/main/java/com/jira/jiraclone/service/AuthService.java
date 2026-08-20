package com.jira.jiraclone.service;

import com.jira.jiraclone.dto.*;
import com.jira.jiraclone.entity.*;
import com.jira.jiraclone.exception.EmailAlreadyExistsException;
import com.jira.jiraclone.repository.PasswordResetTokenRepository;
import com.jira.jiraclone.repository.UserRepository;
import com.jira.jiraclone.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import com.jira.jiraclone.entity.AuthProvider;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    private final EmailService emailService;
    private final PasswordResetTokenRepository tokenRepository;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .status(UserStatus.OFFLINE)
                .enabled(true)
                .deleted(false)
                .build();

        userRepository.save(user);

        String token =
                jwtUtil.generateToken(
                        user.getEmail()
                );

        return AuthResponse.builder()
                .token(token)
                .build();
    }

//    public AuthResponse login(LoginRequest request) {
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            request.getEmail(),
//                            request.getPassword()
//                    )
//            );
//
//            User user = userRepository.findByEmailAndDeletedFalse(
//                    request.getEmail()
//            ).orElseThrow(() ->
//                    new RuntimeException("User not found"));
//
//            user.setStatus(UserStatus.ONLINE);
//
//            user.setLastLoginAt(
//                    LocalDateTime.now()
//            );
//
//            user.setLastActivityAt(
//                    LocalDateTime.now()
//            );
//
//            userRepository.save(user);
//
//            String token =
//                    jwtUtil.generateToken(
//                            request.getEmail()
//                    );
//
//            return AuthResponse.builder()
//                    .token(token)
//                    .build();
//
//    }
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmailAndDeletedFalse(
                request.getEmail()
        ).orElseThrow(() ->
                new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account disabled");
        }

        user.setStatus(UserStatus.ONLINE);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastActivityAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(request.getEmail());

        return AuthResponse.builder()
                .token(token)
                .build();
    }

    public void changePassword(
            ChangePasswordRequest request,
            String email
    ) {

        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        boolean matches =
                passwordEncoder.matches(
                        request.getOldPassword(),
                        user.getPassword()
                );

        if (!matches) {
            throw new RuntimeException(
                    "Old password is incorrect"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);
    }

    public void forgotPassword(
            ForgotPasswordRequest request
    ) {

        Optional<User> userOptional =
                userRepository.findByEmailAndDeletedFalse(
                        request.getEmail()
                );

        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();


        tokenRepository.findByUser(user)
                .ifPresent(
                        tokenRepository::delete
                );
        SecureRandom random =
                new SecureRandom();

        String otp =
                String.format(
                        "%06d",
                        random.nextInt(1000000)
                );

        // Step 6 — Save OTP
        PasswordResetToken resetOtp =
                PasswordResetToken.builder()
                        .otp(otp)
                        .user(user)
                        .used(false)
                        .expiryDate(
                                LocalDateTime.now()
                                        .plusMinutes(5)
                        )
                        .build();

        tokenRepository.save(resetOtp);

        emailService.sendPasswordResetOtp(
                user.getEmail(),
                otp
        );
    }

    public void resetPassword(
            ResetPasswordRequest request
    ) {

        PasswordResetToken token =
                tokenRepository
                        .findByOtp(
                                request.getOtp()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid OTP"
                                ));

        // Verify email matches
        if (
                !token.getUser()
                        .getEmail()
                        .equals(
                                request.getEmail()
                        )
        ) {
            throw new RuntimeException(
                    "Invalid OTP"
            );
        }

        if (token.isUsed()) {
            throw new RuntimeException(
                    "OTP already used"
            );
        }

        // Check expiry
        if (
                token.getExpiryDate()
                        .isBefore(
                                LocalDateTime.now()
                        )
        ) {
            throw new RuntimeException(
                    "OTP expired"
            );
        }

        User user = token.getUser();

        // Check password reuse
        if (
                passwordEncoder.matches(
                        request.getNewPassword(),
                        user.getPassword()
                )
        ) {
            throw new RuntimeException(
                    "New password cannot be same as old password"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        // Delete OTP after success
        tokenRepository.delete(token);
    }
}