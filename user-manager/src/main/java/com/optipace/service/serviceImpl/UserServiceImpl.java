package com.optipace.service.serviceImpl;

import com.optipace.DTO.RequestDTO.UserRequestDto;
import com.optipace.DTO.ResponseDTO.BaseResponse;
import com.optipace.DTO.ResponseDTO.StatusDescription;
import com.optipace.ExceptionHandler.AlreadyExistException;
import com.optipace.ExceptionHandler.NotFoundException;
import com.optipace.entity.Role;
import com.optipace.entity.User;
import com.optipace.repository.RoleRepository;
import com.optipace.repository.UserRepository;
import com.optipace.security.AuthPrincipal;
import com.optipace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserServiceImpl  implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;



    private final UserRepository userRepository;
    private  final RoleRepository roleRepository;

     @Override
    public BaseResponse createUser(
            UserRequestDto dto,
            AuthPrincipal authPrincipal) {




        if (userRepository.existsByUsername(dto.getUsername())) {
            throw  new AlreadyExistException("Username already exists");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw  new AlreadyExistException("Email already exists");
        }

        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() ->
                        new NotFoundException("Role not found"));

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

        user.setCreatedBy(
                authPrincipal.getUsername());

        user.setCreatedDatetime(
                LocalDateTime.now());

        userRepository.save(user);

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
            AuthPrincipal authPrincipal) throws AccessDeniedException {

        User user = userRepository.findById(dto.getId())
                .orElseThrow(() ->
                        new NotFoundException("User not found"));

        boolean isAdmin =
                authPrincipal.getRole()
                        .equals("ADMIN");

        boolean isSelf =
                authPrincipal.getUserId()
                        .equals(user.getId());

        if (!isAdmin && !isSelf) {
            throw new AccessDeniedException(
                    "You can update only your own profile");
        }

        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }

        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }

        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }

        if (dto.getPassword() != null) {
            user.setPassword(
                    passwordEncoder.encode(
                            dto.getPassword()));
        }

        if (isAdmin) {

            if (dto.getRoleId() != null) {

                Optional<Role> role =
                        roleRepository.findById(
                                        dto.getRoleId());

                user.setRole(role.get());
            }

            if (dto.getStatus() != null) {
                user.setStatus(dto.getStatus());
            }
        }

        user.setUpdatedBy(
                authPrincipal.getUsername());

        user.setUpdatedDatetime(
                LocalDateTime.now());

        userRepository.save(user);

        return BaseResponse.builder()
                .statusDescription(
                        new StatusDescription(
                                "User updated successfully",
                                200L))
                .build();
    }
}




