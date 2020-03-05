package com.jhon.verde.sunat.cpe.validez.tesseract;

import com.jhon.verde.sunat.cpe.validez.config.SunatTesseractProperties;
import com.jhon.verde.sunat.cpe.validez.dto.CpeRequest;
import com.jhon.verde.sunat.cpe.validez.exception.NegocioException;
import com.jhon.verde.sunat.cpe.validez.util.Constantes;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Tesseract1;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class SunatTesseractCpeService {

    @Autowired
    private SunatTesseractProperties sunatTesseractProperties;

    @Value("${sunat.numero.reintentos}")
    private int SUNAT_NUMERO_REINTENTOS;

    @Autowired
    @Qualifier("restTemplateSimple")
    private RestTemplate restTemplate;

    @Async
    public void procesarCpesEnLoteSecuencial(CpeRequest[] sunatCpeRequests) throws IOException {
        log.info("Inicia procesar CPEs en lote secuencial.");
        List<CpeRequest> listaRequestFuture1 = new ArrayList<>();
        List<CpeRequest> listaRequestProcesados = new ArrayList<>();

        listaRequestFuture1 = obtenerListaRequest(sunatCpeRequests[0]);

        Long inicio = System.currentTimeMillis();

        for(int i=0 ; i<3 ; i++){
            listaRequestProcesados.addAll(procesarCpesEnLote(listaRequestFuture1));
        }

        Long fin = System.currentTimeMillis();
        log.info("Tiempo de procesar todos los comprobantes: {}", fin - inicio);

        FileWriter writer = new FileWriter("CpesSunatTesseractProcesadosGet.txt", false);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        AtomicInteger contadorCpes = new AtomicInteger(1);

        listaRequestProcesados.stream().forEach(cpe -> {
            try {
                bufferedWriter.write(String.join("|", String.valueOf(contadorCpes.get()), cpe.getNombreCpe(), cpe.getSunatApiRestCpeResponse().getSuccess(), cpe.getSunatApiRestCpeResponse().getMessage()));
                bufferedWriter.newLine();
                contadorCpes.incrementAndGet();
            } catch (IOException e) {
                log.error("IOException. ", e);
            }
        });

        bufferedWriter.close();
        writer.close();

        log.info("Termino de crear archivo TXT");
    }

    private List<CpeRequest> obtenerListaRequest(CpeRequest sunatCpeRequest){
        List<CpeRequest> listaRequest = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            listaRequest.add(sunatCpeRequest);
        }
        return listaRequest;
    }

    private List<CpeRequest> procesarCpesEnLote(List<CpeRequest> listaCpes) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(5);
        AtomicInteger contador = new AtomicInteger(0);
        try {
            return  forkJoinPool.submit(() -> {
                listaCpes.parallelStream().forEach(cpe -> {
                    cpe.setSunatTesseractCpeResponse(validarCpe(cpe));
                });
                return listaCpes;
            }).get();
        } catch (InterruptedException e) {
            log.error("Hubo un error de Interrupted Exception.", e);
        } catch (ExecutionException e) {
            log.error("Hubo un error de ExecutionException.", e);
        }

        return listaCpes;
    }

    public SunatTesseractCpeResponse validarCpe(CpeRequest request){
        ResponseEntity<byte[]> responseSunatCaptcha = null;
        String bodyHtmlResponseValidezCpe = null, textoCaptcha = null;
        SunatTesseractCpeResponse response = SunatTesseractCpeResponse.responseError(Constantes.NO_SE_PUDO_VALIDAR_CPE);

        Long inicioObtenerCaptcha = null, finObtenerCaptcha = null;
        Long inicioObtenerTextoTesseract = null, finObtenerTextoTesseract = null;
        Long inicioValidezCpe = null, finValidezCpe = null;

        int contadorReintento = 0;

        inicioValidezCpe = System.currentTimeMillis();

        while (contadorReintento < SUNAT_NUMERO_REINTENTOS) {
            try {
                inicioObtenerCaptcha = System.currentTimeMillis();
                responseSunatCaptcha = restTemplate.getForEntity(sunatTesseractProperties.getObtenerCaptchaUrl(), byte[].class);
                finObtenerCaptcha = System.currentTimeMillis();
                log.info("Tiempo de obtencion CAPTCHA:{} ms", finObtenerCaptcha - inicioObtenerCaptcha);
            } catch (RestClientException re) {
                log.error("Error al obtener captcha. RestClientException: ", re);
                contadorReintento++;
                continue;
            }

            inicioObtenerTextoTesseract = System.currentTimeMillis();
            textoCaptcha = obtenerTextoCaptcha(responseSunatCaptcha.getBody());
            finObtenerTextoTesseract = System.currentTimeMillis();

            log.info("Tiempo de obtencion texto tesseract: {} ms", finObtenerTextoTesseract - inicioObtenerTextoTesseract);

            if(!textoCaptcha.matches("[A-Z]{4}")){
               log.error("Texto captcha no es uno valido. Captcha: {}", textoCaptcha);
               contadorReintento++;
               continue;
            }

            bodyHtmlResponseValidezCpe = validarCpe(responseSunatCaptcha.getHeaders(), request, textoCaptcha);

            response = obtenerResponseRespuestaSunat(bodyHtmlResponseValidezCpe);

            if (!Constantes.RespuestaSunat.CAPTCHA_INCORRECTO.equals(response.getRespuestaSunat())) {
                finValidezCpe = System.currentTimeMillis();
                log.info("Tiempo de validacion CPE:{} ms", finValidezCpe - inicioValidezCpe);
                return response;
            }
        }

        finValidezCpe = System.currentTimeMillis();

        log.info("Tiempo de validacion CPE: {} ms", finValidezCpe - inicioValidezCpe);

        return response;

    }

    private String obtenerTextoCaptcha(byte[] bytesCaptcha) {
        try {
            String textoCaptcha = "";
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytesCaptcha));
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(sunatTesseractProperties.getTesseractCarpeta());
            textoCaptcha = tesseract.doOCR(bufferedImage);
            return textoCaptcha != null ? textoCaptcha.trim().replace("\n", "").toUpperCase() : textoCaptcha;
        } catch (Error | Exception e) {
            log.error("Error al obtener el texto de la imagen. Exception: ", e);
            throw new NegocioException("Error al obtener el texto de la imagen", e);
        }
    }

    private String validarCpe(HttpHeaders headersResponseCaptcha, CpeRequest request, String textoCaptcha) {
        URI uri = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(HttpHeaders.COOKIE, String.join("; ", headersResponseCaptcha.get(HttpHeaders.SET_COOKIE)));

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("accion", "CapturaCriterioValidez");
        formData.add("num_ruc", request.getComprobante().getRuc());
        formData.add("tipocomprobante", obtenerTipoComprobante(request.getComprobante().getTipoCpe(), request.getComprobante().getSerie()));
        formData.add("num_serie", request.getComprobante().getSerie());
        formData.add("num_comprob", request.getComprobante().getCorrelativo());
        formData.add("fec_emision", request.getFechaEmision() != null ? request.getFechaEmision() : null);
        formData.add("cantidad", request.getImporte() != null ? request.getImporte() : null);
        formData.add("codigo", textoCaptcha);

        try {
            uri = new URI(sunatTesseractProperties.getValidarCpeUrl());
        } catch (URISyntaxException e) {
            log.error("Hubo un error al parsear la URI de SUNAT CPE");
            throw new NegocioException("Hubo un error al parsear la URI de SUNAT CPE", e);
        }

        RequestEntity requestEntity = new RequestEntity(formData, headers, HttpMethod.POST, uri);
        ResponseEntity<String> response = null;
        try{
            response = restTemplate.postForEntity(uri, requestEntity, String.class);
        }catch(RestClientException re){
            log.error("No se pudo validar CPE. RestClientException. ", re);
            return Constantes.NO_SE_PUDO_VALIDAR_CPE;
        }

        return response.getBody();
    }

    private String obtenerTipoComprobante(String tipoCpe, String serie) {
        if ("01".equals(tipoCpe)) {
            return "03";
        }

        if ("03".equals(tipoCpe)) {
            return "06";
        }

        throw new NegocioException("Tipo de comprobante no soportado.");
    }

    private SunatTesseractCpeResponse obtenerResponseRespuestaSunat(String html) {
        if(Constantes.NO_SE_PUDO_VALIDAR_CPE.equals(html)){
            return SunatTesseractCpeResponse.responseError(Constantes.NO_SE_PUDO_VALIDAR_CPE);
        }

        Document doc = Jsoup.parse(html);
        Elements elementsClassError = doc.getElementsByClass("error");

        if (!ObjectUtils.isEmpty(elementsClassError)) {
            String mensajeError = elementsClassError.get(0).text();

            if (!estaMensajeSunatEnListaDeTags(Constantes.TAGS_ERRORES_SUNAT, mensajeError)) {
                return SunatTesseractCpeResponse.responseError("El mensaje de error SUNAT no es uno manejado.");
            }

            if (Constantes.TAG_CAPTCHA_INGRESADO_ES_INCORRECTO.contains(mensajeError)) {
                return SunatTesseractCpeResponse.responseError(mensajeError, Constantes.RespuestaSunat.CAPTCHA_INCORRECTO);
            }

            if (Constantes.TAG_REQUEST_MAL_FORMADO.contains(mensajeError)) {
                return SunatTesseractCpeResponse.responseError(mensajeError, Constantes.RespuestaSunat.REQUEST_INVALIDO);
            }

            return SunatTesseractCpeResponse.responseError(mensajeError);
        }

        Elements elementsClassBgn = doc.getElementsByClass("bgn");
        String mensajeOk = elementsClassBgn.get(0).text();

        System.out.println(html);

        if (!StringUtils.isEmpty(mensajeOk)) {
            if (estaMensajeSunatEnListaDeTags(Constantes.TAGS_NO_ACEPTADO, mensajeOk)) {
                return SunatTesseractCpeResponse.responseOk(mensajeOk, Constantes.RespuestaSunat.NO_ACEPTADO);
            }

            if (estaMensajeSunatEnListaDeTags(Constantes.TAGS_ACEPTADO, mensajeOk)) {
                return SunatTesseractCpeResponse.responseOk(mensajeOk, Constantes.RespuestaSunat.ACEPTADO);
            }

            if (estaMensajeSunatEnListaDeTags(Constantes.TAGS_ANULADO, mensajeOk)) {
                return SunatTesseractCpeResponse.responseOk(mensajeOk, Constantes.RespuestaSunat.ANULADO);
            }

            if (estaMensajeSunatEnListaDeTags(Constantes.TAGS_REVERSADO, mensajeOk)) {
                return SunatTesseractCpeResponse.responseOk(mensajeOk, Constantes.RespuestaSunat.REVERSADO);
            }
        }

        return SunatTesseractCpeResponse.responseError("Tag mensaje OK SUNAT no controlado : " + mensajeOk, Constantes.RespuestaSunat.NO_CONTROLADO);
    }

    private boolean estaMensajeSunatEnListaDeTags(List<String> listaTags, String mensaje) {
        return listaTags.stream().anyMatch(tag -> mensaje.contains(tag));
    }

}
