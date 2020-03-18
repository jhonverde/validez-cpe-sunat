package com.jhon.verde.sunat.cpe.validez.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SunatApiRestTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String token_type;

    @JsonProperty("expires_in")
    private Integer expiresIn;

}
