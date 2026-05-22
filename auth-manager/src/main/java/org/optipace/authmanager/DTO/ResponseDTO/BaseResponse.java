package org.optipace.authmanager.DTO.ResponseDTO;


import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse {

    public StatusDescription  statusDescription;
}
