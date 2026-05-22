package org.optipace.authmanager.DTO.RequestDTO;
import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String captcha;

}