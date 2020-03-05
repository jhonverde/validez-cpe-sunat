package com.jhon.verde.sunat.cpe.validez.tesseract;

import com.jhon.verde.sunat.cpe.validez.util.Constantes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunatTesseractCpeResponse {

    private boolean estado;
    private String mensaje;
    private Constantes.RespuestaSunat respuestaSunat;

    public static SunatTesseractCpeResponse responseOk(String mensaje, Constantes.RespuestaSunat respuestaSunat){
        return new SunatTesseractCpeResponse(true, mensaje, respuestaSunat);
    }

    public static SunatTesseractCpeResponse responseError(String mensaje){
        return new SunatTesseractCpeResponse(false, mensaje);
    }

    public static SunatTesseractCpeResponse responseError(String mensaje, Constantes.RespuestaSunat respuestaSunat){
        return new SunatTesseractCpeResponse(false, mensaje, respuestaSunat);
    }

    private SunatTesseractCpeResponse(boolean estado, String mensaje){
        this.estado = estado;
        this.mensaje = mensaje;
    }

}
