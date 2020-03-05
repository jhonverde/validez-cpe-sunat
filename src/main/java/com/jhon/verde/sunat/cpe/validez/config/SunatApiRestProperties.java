package com.jhon.verde.sunat.cpe.validez.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sunat.oauth2")
@Data
@NoArgsConstructor
public class SunatApiRestProperties {

    private String clientId;
    private String clientSecret;
    private String grantType;
    private String scope;
    private String obtenerTokenUrl;
    private String validarCpeUrl;

}
