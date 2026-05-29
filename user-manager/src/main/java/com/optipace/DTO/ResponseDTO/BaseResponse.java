package com.optipace.DTO.ResponseDTO;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Data
@Getter
@Setter
public class BaseResponse {

    private  StatusDescription statusDescription;
}
