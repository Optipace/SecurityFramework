package com.optipace.DTO.RequestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequestDto {

    private Long id;
  @NotBlank(message = "Status cannot be blank")
  private String status;

  @NotBlank(message = "Username cannot be blank")
  private String username;

  @NotBlank(message = "Email cannot be blank")
  private String email;

  @NotBlank(message = "Phone number cannot be blank")
  private String phoneNumber;

  @NotBlank(message = "First name cannot be blank")
  private String firstName;

  @NotBlank(message = "Last name cannot be blank")
  private String lastName;

  @NotBlank(message = "Password cannot be blank")
  private String password;

  @NotNull(message = "RoleId cannot be null")
  private Long roleId;
}