package com.jhon.verde.cpe.sunat.validez;

public class NegocioException extends RuntimeException{

    public NegocioException(String mensaje, Throwable t){
        super(mensaje, t);
    }

}
