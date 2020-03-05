package com.jhon.verde.sunat.cpe.validez.rest;

import com.jhon.verde.sunat.cpe.validez.dto.CpeRequest;
import com.jhon.verde.sunat.cpe.validez.exception.NegocioException;
import com.jhon.verde.sunat.cpe.validez.config.SunatApiRestProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class SunatApiRestCpeService {

    @Autowired
    @Qualifier("restTemplateSimple")
    private RestTemplate restTemplateObtenerToken;

    @Autowired
    @Qualifier("restTemplateApiRestSunat")
    private RestTemplate restTemplateValidarCpe;

    @Autowired
    private SunatApiRestProperties sunatOAuth2Properties;

    public SunatApiRestTokenResponse obtenerToken(){
        URI uri = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", sunatOAuth2Properties.getGrantType());
        formData.add("scope", sunatOAuth2Properties.getScope());
        formData.add("client_id", sunatOAuth2Properties.getClientId());
        formData.add("client_secret", sunatOAuth2Properties.getClientSecret());

        try {
            uri = new URI(sunatOAuth2Properties.getObtenerTokenUrl());
        } catch (URISyntaxException e) {
            log.error("Hubo un error al parsear la URI de obtener token SUNAT: {}", sunatOAuth2Properties.getObtenerTokenUrl());
            throw new NegocioException("Hubo un error al parsear la URI de SUNAT CPE", e);
        }

        RequestEntity requestEntity = new RequestEntity(formData, headers, HttpMethod.POST, uri);
        ResponseEntity<SunatApiRestTokenResponse> response = null;
        try {
            response = restTemplateObtenerToken.postForEntity(uri, requestEntity, SunatApiRestTokenResponse.class);
        } catch (RestClientException re) {
            log.error("RestClientException. Error al obtener token SUNAT. SunatOAuth2Properties: {}. Excepcion: {}", sunatOAuth2Properties, re);
            return null;
        }

        return response.getBody();
    }

    public SunatApiRestCpeResponse validarCpe(CpeRequest cpeRequest){
        SunatApiRestCpeRequest request = new SunatApiRestCpeRequest(cpeRequest);

        try{
            return restTemplateValidarCpe.postForEntity(sunatOAuth2Properties.getValidarCpeUrl(), request, SunatApiRestCpeResponse.class).getBody();
        }catch (RestClientException re){
            log.error("RestClientException. Error al validar cpe en SUNAT. Request: {}. Excepcion: {}", request, re);
            return null;
        }

    }

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

        FileWriter writer = new FileWriter("CpesApiRestProcesadosGet.txt", false);
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

    @Async
    public void procesarCpesEnLoteParalelo(CpeRequest[] sunatCpeRequests) throws IOException {
        log.info("Inicia procesar CPEs en lote secuencial.");
        List<CpeRequest> listaRequestFuture1 = new ArrayList<>();
        List<CpeRequest> listaRequestFuture2 = new ArrayList<>();
        List<CpeRequest> listaRequestFuture3 = new ArrayList<>();

        listaRequestFuture1 = obtenerListaRequest(sunatCpeRequests[0]);
        listaRequestFuture2 = obtenerListaRequest(sunatCpeRequests[1]);
        listaRequestFuture3 = obtenerListaRequest(sunatCpeRequests[2]);

        CompletableFuture<List<CpeRequest>> future1 = CompletableFuture.completedFuture(procesarCpesEnLote(listaRequestFuture1));
        CompletableFuture<List<CpeRequest>> future2 = CompletableFuture.completedFuture(procesarCpesEnLote(listaRequestFuture2));
        CompletableFuture<List<CpeRequest>> future3 = CompletableFuture.completedFuture(procesarCpesEnLote(listaRequestFuture3));

        Long inicio = System.currentTimeMillis();
        CompletableFuture.allOf(future1, future2, future3).join();
        Long fin = System.currentTimeMillis();

        log.info("Tiempo de procesar todos los comprobantes: {}", fin - inicio);

        List<CompletableFuture> listaFutures = new ArrayList<>();
        listaFutures.add(future1);
        listaFutures.add(future2);
        listaFutures.add(future3);

        List<CpeRequest> listaRequestProcesados = new ArrayList<>();

        listaFutures.stream().forEach(future -> {
            try {
                listaRequestProcesados.addAll((Collection<? extends CpeRequest>) future.get());
            } catch (InterruptedException e) {
                log.error("InterruptedException", e);
            } catch (ExecutionException e) {
                log.error("ExecutionException", e);
            }
        });


        FileWriter writer = new FileWriter("CpesApiRestProcesadosAllOf.txt", false);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        AtomicInteger contadorCpes = new AtomicInteger(1);

        listaRequestProcesados.stream().forEach(cpe -> {
            try {
                bufferedWriter.write(String.join("|", String.valueOf(contadorCpes.get()), cpe.getNombreCpe(), cpe.getSunatApiRestCpeResponse().getSuccess(), cpe.getSunatApiRestCpeResponse().getMessage()));
                bufferedWriter.newLine();
                contadorCpes.incrementAndGet();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        bufferedWriter.close();
        writer.close();
        log.info("Termino de crear archivo txt");
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
                    cpe.setSunatApiRestCpeResponse(validarCpe(cpe));
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

}
