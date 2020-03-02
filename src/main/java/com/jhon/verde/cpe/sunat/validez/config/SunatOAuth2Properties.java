package com.jhon.verde.cpe.sunat.validez.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sunat.oauth2")
@Data
@NoArgsConstructor
public class SunatOAuth2Properties {

    private String clientId;
    private String clientSecret;
    private String grantType;
    private String scope;
    private String obtenerTokenUrl;
    private String validarCpeUrl;

}
