package org.optipace.authmanager.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.optipace.authmanager.DTO.RequestDTO.LoginRequest;
import org.optipace.authmanager.DTO.RequestDTO.RefreshTokenRequest;
import org.optipace.authmanager.DTO.ResponseDTO.BaseResponse;
import org.optipace.authmanager.DTO.ResponseDTO.StatusDescription;
import org.optipace.authmanager.DTO.ResponseDTO.TokenResponse;
import org.optipace.authmanager.Entity.RefreshToken;
import org.optipace.authmanager.Entity.User;
import org.optipace.authmanager.repository.RefreshTokenRepository;
import org.optipace.authmanager.repository.UserRepository;
import org.optipace.authmanager.security.JwtUtil;
import org.optipace.authmanager.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Override
    public ResponseEntity<TokenResponse> login(
            LoginRequest request,
            HttpSession session,
            HttpServletRequest servletRequest) {

        System.out.println("Session ID login: " + session.getId());
        System.out.println("Captcha in session: " + session.getAttribute("captcha"));
        System.out.println(passwordEncoder.encode(request.getPassword()));


        String sessionCaptcha = (String) session.getAttribute("captcha");



        if (sessionCaptcha == null ||
                !sessionCaptcha.equalsIgnoreCase(request.getCaptcha())) {

            return ResponseEntity.badRequest().body(
                    new TokenResponse(
                            new StatusDescription(
                                    "Invalid Captcha",
                                    400L
                            ),
                           null,null
            ));
        }

        session.removeAttribute("CAPTCHA");

        Optional<User> user = userRepository.findByUsername(request.getUsername());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new TokenResponse(
                            new StatusDescription(
                                    "Invalid Username",
                                    401L
                            ),
                            null,null
                    ));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new TokenResponse(
                            new StatusDescription(
                                    "Invalid  Password",
                                    401L
                            ),
                            null,null
                    ));

        }

        if (!"ACTIVE".equalsIgnoreCase(user.get().getStatus())) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new TokenResponse(
                            new StatusDescription(
                                    "User account is inactive",
                                    403L
                            ),
                            null,null
                    ));

        }

        String token = jwtUtil.generateToken(user.get().getId(),
                user.get().getUsername(),user.get().getRole().getName());


        String newRefreshToken =
                jwtUtil.generateRefreshToken(user.get().getId());
        return ResponseEntity.ok(
                new TokenResponse(
                        new StatusDescription(
                                "Login successfull",
                                200L
                        ),
                        token,newRefreshToken
                ));

    }

    @Override
    public ResponseEntity<TokenResponse> updateRefershToken(RefreshTokenRequest request) {

        if (request.getRefreshToken() == null ||
                request.getRefreshToken().isBlank()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new TokenResponse(
                            new StatusDescription("Invalid Request", 400L),
                            null,
                            null
                    )
            );
        }

        Optional<RefreshToken> token =
                refreshTokenRepository.findByRefreshToken(
                        request.getRefreshToken());

        if (token.isEmpty()) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new TokenResponse(
                            new StatusDescription("Invalid Refresh Token", 401L),
                            null,
                            null
                    )
            );
        }

        if (token.get().getExpireDatetime().isBefore(LocalDateTime.now())) {

            refreshTokenRepository.delete(token.get());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new TokenResponse(
                            new StatusDescription("Token Expired", 401L),
                            null,
                            null
                    )
            );
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


}