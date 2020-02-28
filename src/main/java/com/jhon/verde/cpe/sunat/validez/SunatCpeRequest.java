package com.jhon.verde.cpe.sunat.validez;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SunatCpeRequest {

    private Comprobante comprobante;
    private String fechaEmision;
    private String importe;

    private SunatCpeResponseExternal sunatCpeResponseExternal;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Comprobante {
        private String ruc;
        private String tipoCpe;
        private String serie;
        private String correlativo;
    }

    public String getNombreCpe(){
        return String.join("-", comprobante.getRuc(), comprobante.getTipoCpe(), comprobante.getSerie(), comprobante.getCorrelativo());
    }

}
