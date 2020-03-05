package com.jhon.verde.sunat.cpe.validez.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SunatApiRestCpeResponse {

    private String success;
    private String message;
    private String errorCode;
    private DataCpe data;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class DataCpe {
        private String estadoCp;
        private String estadoRuc;
        private String condDomiRuc;
        private String[] observaciones;
    }

}
