package com.optipace.DTO.RequestDTO;

import lombok.Data;

@Data
public class UserRequestDto {

    private Long id;
  private String status;
    private String username;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String password;
    private Long roleId;
}