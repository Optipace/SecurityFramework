package com.optipace.repository;

import com.optipace.entity.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    boolean existsByPhoneNumber(@NotBlank(message = "Phone number cannot be blank") String phoneNumber);
}
