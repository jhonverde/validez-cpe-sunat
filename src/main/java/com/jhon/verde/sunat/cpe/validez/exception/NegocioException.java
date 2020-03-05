package com.jhon.verde.sunat.cpe.validez.exception;

public class NegocioException extends RuntimeException{

    public NegocioException(String mensaje){
        super(mensaje);
    }

    public NegocioException(String mensaje, Throwable t){
        super(mensaje, t);
    }

}
