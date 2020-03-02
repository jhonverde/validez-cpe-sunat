package com.jhon.verde.cpe.sunat.validez.exception;

public class NegocioException extends RuntimeException{

    public NegocioException(String mensaje, Throwable t){
        super(mensaje, t);
    }

}
