package com.jhon.verde.sunat.cpe.validez.util;

import java.util.Arrays;
import java.util.List;

public class Constantes {

    public static final String NO_SE_PUDO_VALIDAR_CPE = "No se pudo validar el CPE en SUNAT";

    public static final String TAG_CAPTCHA_INGRESADO_ES_INCORRECTO = "El código ingresado es incorrecto";
    public static final String TAG_REQUEST_MAL_FORMADO = "Ha ocurrido al procesar la consulta";
    public static final List<String> TAGS_ERRORES_SUNAT = Arrays.asList(TAG_CAPTCHA_INGRESADO_ES_INCORRECTO, TAG_REQUEST_MAL_FORMADO);
    public static final String ERROR_CAPTCHA_ES_INCORRECTO = "Hubo un error al procesar el CAPTCHA. Vuelva a intentarlo por favor.";

    public static final List<String> TAGS_NO_ACEPTADO = Arrays.asList("No existe", "no ha sido informada a SUNAT", "no existe en los registros");
    public static final List<String> TAGS_ACEPTADO = Arrays.asList("comprobante de pago válido", "informada a SUNAT", "es válido");
    public static final List<String> TAGS_ANULADO = Arrays.asList("comunicada de BAJA");
    public static final List<String> TAGS_REVERSADO = Arrays.asList("no está vigente");

    public enum RespuestaSunat {
        REQUEST_INVALIDO(-3),
        CAPTCHA_INCORRECTO(-2),
        NO_CONTROLADO(-1),
        NO_ACEPTADO(0),
        ACEPTADO(1),
        ANULADO(2),
        REVERSADO(3);

        private final int valor;

        RespuestaSunat(int valor) {
            this.valor = valor;
        }

        public int getValor() {
            return valor;
        }


        @Override
        public String toString() {
            return "Codigo: " + valor + ", Descripcion: " + name();
        }
    }

}
