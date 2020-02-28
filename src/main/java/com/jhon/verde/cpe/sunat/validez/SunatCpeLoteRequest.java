package com.jhon.verde.cpe.sunat.validez;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class SunatCpeLoteRequest {

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Integer limiteRegistrosAProcesar;

}
