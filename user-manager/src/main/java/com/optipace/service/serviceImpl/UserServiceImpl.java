package com.optipace.service.serviceImpl;

import com.optipace.DTO.RequestDTO.UserRequestDto;
import com.optipace.DTO.ResponseDTO.BaseResponse;
import com.optipace.DTO.ResponseDTO.StatusDescription;
import com.optipace.ExceptionHandler.AlreadyExistException;
import com.optipace.ExceptionHandler.BadRequestException;
import com.optipace.ExceptionHandler.NotFoundException;
import com.optipace.entity.Role;
import com.optipace.entity.User;
import com.optipace.repository.RoleRepository;
import com.optipace.repository.UserRepository;
import com.optipace.security.AuthPrincipal;
import com.optipace.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl  implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;



    private final UserRepository userRepository;
    private  final RoleRepository roleRepository;


    @Override
    public BaseResponse createUser(
            UserRequestDto dto,
            AuthPrincipal authPrincipal) {

        log.info("Create user request received for username: {}", dto.getUsername());

        if (userRepository.existsByUsername(dto.getUsername())) {
            log.warn("User creation failed. Username already exists: {}", dto.getUsername());
            throw new AlreadyExistException("Username already exists");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("User creation failed. Email already exists: {}", dto.getEmail());
            throw new AlreadyExistException("Email already exists");
        }

        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            log.warn("User creation failed. Phone number already exists: {}", dto.getPhoneNumber());
            throw new AlreadyExistException("Phone number already exists");
        }

        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> {
                    log.warn("Role not found. RoleId: {}", dto.getRoleId());
                    return new NotFoundException("Role not found");
                });

        User user = new User();

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());

        user.setPassword(
                passwordEncoder.encode(dto.getPassword()));

        user.setRole(role);
        user.setStatus("ACTIVE");
        user.setCreatedBy(authPrincipal.getUsername());
        user.setCreatedDatetime(LocalDateTime.now());

        userRepository.save(user);

        log.info(
                "User created successfully. UserId: {}, Username: {}, CreatedBy: {}",
                user.getId(),
                user.getUsername(),
                authPrincipal.getUsername());

        return BaseResponse.builder()
                .statusDescription(
                        new StatusDescription(
                                "User created successfully",
                                200L))
                .build();
    }

    @Override
    public BaseResponse updateUser(
            UserRequestDto dto,
            AuthPrincipal authPrincipal)
            throws AccessDeniedException {

        log.info(
                "Update user request received. UserId: {}, RequestedBy: {}",
                dto.getId(),
                authPrincipal.getUsername());

        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> {
                    log.warn("User not found. UserId: {}", dto.getId());
                    return new NotFoundException("User not found");
                });

        boolean isAdmin =
                "ADMIN".equals(authPrincipal.getRole());

        boolean isSelf =
                authPrincipal.getUserId()
                        .equals(user.getId());

        if (!isAdmin && !isSelf) {
            log.warn(
                    "Unauthorized update attempt. TargetUserId: {}, RequestedBy: {}",
                    user.getId(),
                    authPrincipal.getUsername());

            throw new AccessDeniedException(
                    "You can update only your own profile");
        }

        if (dto.getFirstName() != null)
            user.setFirstName(dto.getFirstName());

        if (dto.getLastName() != null)
            user.setLastName(dto.getLastName());

        if (dto.getEmail() != null) {

            Optional<User> existingUser =
                    userRepository.findByEmail(dto.getEmail());
            

            if (existingUser.isPresent()
                    && !existingUser.get().getId().equals(user.getId())) {

                log.warn(
                        "Email update failed. Email already exists: {}",
                        dto.getEmail());

                throw new BadRequestException(
                        "Email already exists");
            }

            user.setEmail(dto.getEmail());
        }

        if (dto.getPhoneNumber() != null)
            user.setPhoneNumber(dto.getPhoneNumber());

        if (dto.getPassword() != null) {
            log.info("Password update requested for UserId: {}", user.getId());

            user.setPassword(
                    passwordEncoder.encode(
                            dto.getPassword()));
        }

        if (isAdmin) {

            if (dto.getRoleId() != null) {

                Role role = roleRepository.findById(dto.getRoleId())
                        .orElseThrow(() -> {
                            log.warn(
                                    "Role not found while updating user. RoleId: {}",
                                    dto.getRoleId());

                            return new NotFoundException(
                                    "Role not found");
                        });

                user.setRole(role);
            }

            if (dto.getStatus() != null)
                user.setStatus(dto.getStatus());
        }

        user.setUpdatedBy(authPrincipal.getUsername());
        user.setUpdatedDatetime(LocalDateTime.now());

        userRepository.save(user);

        log.info(
                "User updated successfully. UserId: {}, UpdatedBy: {}",
                user.getId(),
                authPrincipal.getUsername());

        return BaseResponse.builder()
                .statusDescription(
                        new StatusDescription(
                                "User updated successfully",
                                200L))
                .build();
    }
}




