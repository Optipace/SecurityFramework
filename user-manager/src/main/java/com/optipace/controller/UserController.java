package com.optipace.controller;


import com.optipace.DTO.ResponseDTO.BaseResponse;
import com.optipace.DTO.RequestDTO.UserRequestDto;
import com.optipace.security.AuthPrincipal;
import com.optipace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;


import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/v1/create")
    @PreAuthorize("hasAuthority('CREATE_USER')")
    public BaseResponse createUser(
            @RequestBody UserRequestDto requestDto,
            @AuthenticationPrincipal AuthPrincipal authPrincipal) {

        return userService.createUser(
                requestDto,
                authPrincipal);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public BaseResponse updateUser(
            @RequestBody UserRequestDto requestDto,
            @AuthenticationPrincipal AuthPrincipal authPrincipal)
            throws AccessDeniedException {

        return userService.updateUser(
                requestDto,
                authPrincipal);
    }
}