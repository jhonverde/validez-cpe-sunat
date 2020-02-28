package com.jhon.verde.cpe.sunat.validez;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SunatCpeResponseExternal {

    @JsonProperty("data")
    private Comprobante comprobante;
    private Integer rpta;
    private String msjError;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Comprobante {
        @JsonProperty("numRuc")
        private String ruc;
        @JsonProperty("codComp")
        private String tipoCpe;
        @JsonProperty("numeroSerie")
        private String serie;
        @JsonProperty("numero")
        private String correlativo;
        @JsonProperty("monto")
        private String importe;
        @JsonProperty("observaciones")
        private String[] observaciones;
    }

}
