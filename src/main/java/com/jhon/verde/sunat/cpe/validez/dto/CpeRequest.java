package com.jhon.verde.sunat.cpe.validez.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CpeRequest {

    private Comprobante comprobante;
    private String fechaEmision;
    private String importe;

    private SunatApiRestCpeResponse sunatApiRestCpeResponse;
    private SunatTesseractCpeResponse sunatTesseractCpeResponse;

    public String getNombreCpe() {
        return String.join("-", comprobante.getRuc(), comprobante.getTipoCpe(), comprobante.getSerie(), comprobante.getCorrelativo());
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comprobante {
        private String ruc;
        private String tipoCpe;
        private String serie;
        private String correlativo;
    }

}
