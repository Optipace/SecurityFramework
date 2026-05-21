
package org.optipace.authmanager.DTO.ResponseDTO;

import lombok.Data;

@Data
public class TokenResponse extends BaseResponse {

    private String accessToken;
    private String refreshToken;

    public TokenResponse(
            StatusDescription statusDescription,
            String accessToken,
            String refreshToken
    ) {
        super(statusDescription);
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}