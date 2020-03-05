package com.jhon.verde.sunat.cpe.validez.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jhon.verde.sunat.cpe.validez.rest.SunatApiRestCpeResponse;
import com.jhon.verde.sunat.cpe.validez.portal.SunatCookiePortalCpeResponse;
import com.jhon.verde.sunat.cpe.validez.tesseract.SunatTesseractCpeResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CpeRequest {

    private Comprobante comprobante;
    private String fechaEmision;
    private String importe;

    private SunatCookiePortalCpeResponse sunatCookiePortalCpeResponse;
    private SunatApiRestCpeResponse sunatApiRestCpeResponse;
    private SunatTesseractCpeResponse sunatTesseractCpeResponse;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comprobante {
        private String ruc;
        private String tipoCpe;
        private String serie;
        private String correlativo;
    }

    public String getNombreCpe(){
        return String.join("-", comprobante.getRuc(), comprobante.getTipoCpe(), comprobante.getSerie(), comprobante.getCorrelativo());
    }

}
