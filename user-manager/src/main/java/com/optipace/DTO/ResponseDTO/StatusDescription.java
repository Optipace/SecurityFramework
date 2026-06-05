package com.optipace.DTO.ResponseDTO;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StatusDescription {
    private String message;
    private Long code;

    public StatusDescription(String message) {
        this.message = message;
    }
}

