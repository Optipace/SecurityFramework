package com.optipace.service;

import com.optipace.DTO.RequestDTO.UserRequestDto;
import com.optipace.DTO.ResponseDTO.BaseResponse;
import com.optipace.security.AuthPrincipal;

import java.nio.file.AccessDeniedException;

public interface UserService {

    BaseResponse createUser(UserRequestDto requestDto, AuthPrincipal authPrincipal);

    BaseResponse updateUser(UserRequestDto requestDto, AuthPrincipal authPrincipal) throws AccessDeniedException;
}
