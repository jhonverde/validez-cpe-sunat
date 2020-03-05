package com.jhon.verde.sunat.cpe.validez.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sunat.consulta.unificada.libre")
@Data
@NoArgsConstructor
public class SunatTesseractProperties {

    private String obtenerCaptchaUrl;
    private String validarCpeUrl;
    private String tesseractCarpeta;

}
