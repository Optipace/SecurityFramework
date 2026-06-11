package org.optipace.authmanager.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.optipace.authmanager.DTO.RequestDTO.ChangePasswordRequest;
import org.optipace.authmanager.DTO.RequestDTO.LoginRequest;
import org.optipace.authmanager.DTO.RequestDTO.RefreshTokenRequest;
import org.optipace.authmanager.DTO.ResponseDTO.BaseResponse;
import org.optipace.authmanager.DTO.ResponseDTO.StatusDescription;
import org.optipace.authmanager.DTO.ResponseDTO.TokenResponse;
import org.optipace.authmanager.Entity.RefreshToken;
import org.optipace.authmanager.Entity.User;
import org.optipace.authmanager.ExceptionHandler.BadRequestException;
import org.optipace.authmanager.ExceptionHandler.ForbiddenException;
import org.optipace.authmanager.ExceptionHandler.UnauthorisedException;
import org.optipace.authmanager.repository.RefreshTokenRepository;
import org.optipace.authmanager.repository.UserRepository;
import org.optipace.authmanager.security.JwtUtil;
import org.optipace.authmanager.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 15;

    @Override
    public ResponseEntity<TokenResponse> login(
            LoginRequest request,
            HttpSession session,
            HttpServletRequest servletRequest) {

        log.info("Login attempt for username: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid Username or Password"));

        if (user.getAccountLockedUntil() != null &&
                user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            String formattedTime =
                    user.getAccountLockedUntil().format(formatter);

            throw new ForbiddenException(
                    "Account locked until " + formattedTime);


        }

        String sessionCaptcha = (String) session.getAttribute("captcha");

        if (sessionCaptcha == null ||
                !sessionCaptcha.equalsIgnoreCase(request.getCaptcha())) {

            handleFailedLogin(user);
            log.warn("Invalid captcha provided for user: {}", request.getUsername());

            throw new BadRequestException("Invalid Captcha");
        }

        session.removeAttribute("captcha");

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            log.warn("Login attempt for inactive user: {}", request.getUsername());
            throw new ForbiddenException("User not active");
        }


        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            handleFailedLogin(user);
            log.warn("Invalid password for user: {}", request.getUsername());
            throw new BadRequestException("Invalid Username or Password");
        }

        user.setFailedAttempts(0);
        user.setLastFailedAttempt(null);
        user.setAccountLockedUntil(null);
        userRepository.save(user);

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().getName()
        );
        log.info("User logged in successfully: {}", request.getUsername());

        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return ResponseEntity.ok(
                new TokenResponse(
                        new StatusDescription("Login successfully", 200L),
                        token,
                        refreshToken
                )
        );
    }

    @Override
    @Transactional
    public ResponseEntity<TokenResponse> updateRefershToken(RefreshTokenRequest request) {
        log.info("Refresh token request received");
        if (request.getRefreshToken() == null ||
                request.getRefreshToken().isBlank()) {

            throw new BadRequestException("Refresh token absent");


        }

        Optional<RefreshToken> token =
                refreshTokenRepository.findByRefreshToken(
                        request.getRefreshToken());

        if (token.isEmpty()) {

            throw new UnauthorisedException("Refresh token not found");

        }

        if (token.get().getExpireDatetime().isBefore(LocalDateTime.now())) {

            refreshTokenRepository.delete(token.get());
            log.warn("Expired refresh token used");
            throw new UnauthorisedException("Refresh token expired");

        }

        User user = token.get().getUser();

        String jwtToken = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().getName()
        );

        refreshTokenRepository.delete(token.get());
        String newRefreshToken =
                jwtUtil.generateRefreshToken(user.getId());

        return ResponseEntity.status(HttpStatus.OK).body(
                new TokenResponse(
                        new StatusDescription(
                                "Token Refreshed Successfully",
                                200L
                        ),
                        jwtToken,
                        newRefreshToken
                )
        );
    }

    @Override
    public ResponseEntity<BaseResponse> changePassword(
            ChangePasswordRequest request, String userId) {

        log.info("Password change initiated for userId: {}", userId);


        Optional<User> optionalUser =
                userRepository.findById(
                        Long.parseLong(userId)
                );

        if (optionalUser.isEmpty()) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new BaseResponse(
                            new StatusDescription(
                                    "User Not Found",
                                    404L
                            )
                    )
            );
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(
                request.getOldPassword(),
                user.getPassword())) {
            log.warn("Old Password Incorrect: {}", userId);

            throw new BadRequestException("Old Password Incorrect");

        }

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {
            log.warn("New Password and confirm password do not match: {}", userId);
            throw new BadRequestException("New Password and confirm password do not match");

        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {
            log.warn("New password can't be same as old: {}", userId);

            throw new BadRequestException("New Password cannot be same as old password");

        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()));

        userRepository.save(user);
        log.info("Password changed successfully for userId: {}", userId);


        refreshTokenRepository.deleteByUser_Id
                (Long.parseLong(userId));

        return ResponseEntity.
                ok(
                new BaseResponse(
                        new StatusDescription(
                                "Password Changed Successfully",
                                200L
                        )
                )
        );
    }

    @Override
    @Transactional
    public ResponseEntity<BaseResponse> logout(String userId) {

        refreshTokenRepository.deleteByUser_Id(Long.valueOf(userId));

        BaseResponse response = new BaseResponse(
                new StatusDescription(
                        "Logout Successfully",
                        200L
                )
        );

        return ResponseEntity.ok(response);
    }

    private void handleFailedLogin(User user) {

        user.setFailedAttempts(user.getFailedAttempts() + 1);
        user.setLastFailedAttempt(LocalDateTime.now());

        if (user.getFailedAttempts() >= MAX_ATTEMPTS) {

            user.setAccountLockedUntil(
                    LocalDateTime.now().plusMinutes(LOCK_TIME_MINUTES)
            );

            user.setFailedAttempts(0);
        }

        userRepository.save(user);
    }
}