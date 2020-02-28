package com.jhon.verde.cpe.sunat.validez;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class SunatCpeService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${sunat.consulta.individual.url}")
    private String SUNAT_CONSULTA_INDIVIDUAL_URL;

    @Value("${sunat.consulta.individual.cookie}")
    private String SUNAT_COOKIE;

    @Async
    public void procesarCpesEnLoteSecuencial(SunatCpeRequest[] sunatCpeRequests) throws IOException, ExecutionException, InterruptedException {
        List<SunatCpeRequest> listaRequestFuture1 = new ArrayList<>();
        List<SunatCpeRequest> listaRequestFuture2 = new ArrayList<>();
        List<SunatCpeRequest> listaRequestFuture3 = new ArrayList<>();
        List<SunatCpeRequest> listaRequestProcesados = new ArrayList<>();

        listaRequestFuture1 = obtenerListaRequest(sunatCpeRequests[0]);
        listaRequestFuture2 = obtenerListaRequest(sunatCpeRequests[1]);
        listaRequestFuture3 = obtenerListaRequest(sunatCpeRequests[2]);

        Long inicio = System.currentTimeMillis();

        for (int i = 0; i < 3; i++) {
            List<SunatCpeRequest> listaFuture = procesarCpesEnLote(listaRequestFuture1).get();
            listaRequestProcesados.addAll(listaFuture);
        }

        Long fin = System.currentTimeMillis();
        log.info("Tiempo de procesar todos los comprobantes: {}", fin - inicio);

        FileWriter writer = new FileWriter("CpesProcesadosGet.txt", false);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        AtomicInteger contadorCpes = new AtomicInteger(1);

        listaRequestProcesados.stream().forEach(cpe -> {
            try {
                bufferedWriter.write(String.join("|", String.valueOf(contadorCpes.get()), cpe.getNombreCpe(), cpe.getSunatCpeResponseExternal().getRpta().toString(), cpe.getSunatCpeResponseExternal().getMsjError()));
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

    @Async
    public void procesarCpesEnLoteParalelo(SunatCpeRequest[] sunatCpeRequests) throws IOException {
        List<SunatCpeRequest> listaRequestFuture1 = new ArrayList<>();
        List<SunatCpeRequest> listaRequestFuture2 = new ArrayList<>();
        List<SunatCpeRequest> listaRequestFuture3 = new ArrayList<>();

        listaRequestFuture1 = obtenerListaRequest(sunatCpeRequests[0]);
        listaRequestFuture2 = obtenerListaRequest(sunatCpeRequests[1]);
        listaRequestFuture3 = obtenerListaRequest(sunatCpeRequests[2]);

        CompletableFuture<List<SunatCpeRequest>> future1 = procesarCpesEnLote(listaRequestFuture1);
        CompletableFuture<List<SunatCpeRequest>> future2 = procesarCpesEnLote(listaRequestFuture2);
        CompletableFuture<List<SunatCpeRequest>> future3 = procesarCpesEnLote(listaRequestFuture3);

        Long inicio = System.currentTimeMillis();
        CompletableFuture.allOf(future1, future2, future3).join();
        Long fin = System.currentTimeMillis();

        log.info("Tiempo de procesar todos los comprobantes: {}", fin - inicio);

        List<CompletableFuture> listaFutures = new ArrayList<>();
        listaFutures.add(future1);
        listaFutures.add(future2);
        listaFutures.add(future3);

        List<SunatCpeRequest> listaRequestProcesados = new ArrayList<>();

        listaFutures.stream().forEach(future -> {
            try {
                listaRequestProcesados.addAll((Collection<? extends SunatCpeRequest>) future.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });


        FileWriter writer = new FileWriter("CpesProcesadosAllOf.txt", false);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        AtomicInteger contadorCpes = new AtomicInteger(1);

        listaRequestProcesados.stream().forEach(cpe -> {
            try {
                bufferedWriter.write(String.join("|", String.valueOf(contadorCpes.get()), cpe.getNombreCpe(), cpe.getSunatCpeResponseExternal().getRpta().toString(), cpe.getSunatCpeResponseExternal().getMsjError()));
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

    private List<SunatCpeRequest> obtenerListaRequest(SunatCpeRequest sunatCpeRequest){
        List<SunatCpeRequest> listaRequest = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            listaRequest.add(sunatCpeRequest);
        }
        return listaRequest;
    }

    private CompletableFuture<List<SunatCpeRequest>> procesarCpesEnLote(List<SunatCpeRequest> listaCpes) {
        return CompletableFuture.supplyAsync(() -> {
            listaCpes.parallelStream().forEach(cpe -> {
                cpe.setSunatCpeResponseExternal(procesarCpe(cpe));
            });
            return listaCpes;
        });
    }

    public SunatCpeResponseExternal procesarCpe(SunatCpeRequest request) {
        return consultaCpeEnPaginaSunat(request);
    }

    private SunatCpeResponseExternal consultaCpeEnPaginaSunat(SunatCpeRequest request) {
        URI uri = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(HttpHeaders.COOKIE, obtenerCookieValido());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("numRuc", request.getComprobante().getRuc());
        formData.add("codComp", request.getComprobante().getTipoCpe());
        formData.add("numeroSerie", request.getComprobante().getSerie());
        formData.add("numero", request.getComprobante().getCorrelativo());
        formData.add("fechaEmision", !StringUtils.isEmpty(request.getFechaEmision()) ? request.getFechaEmision() : null);
        formData.add("monto", !StringUtils.isEmpty(request.getImporte()) ? request.getImporte() : null);

        try {
            uri = new URI(SUNAT_CONSULTA_INDIVIDUAL_URL);
        } catch (URISyntaxException e) {
            throw new NegocioException("Hubo un error al parsear la URI de SUNAT CPE", e);
        }

        RequestEntity requestEntity = new RequestEntity(formData, headers, HttpMethod.POST, uri);
        ResponseEntity<String> response = null;
        SunatCpeResponseExternal sunatCpeResponseExternal = null;
        try {
            Long inicioConsultaCpe = System.currentTimeMillis();
            response = restTemplate.postForEntity(uri, requestEntity, String.class);
            Long finConsultaCpe = System.currentTimeMillis();
            log.info("Tiempo consulta individual CPE SUNAT: {} ms", finConsultaCpe - inicioConsultaCpe);
        } catch (RestClientException re) {
            log.error("RestClientException - Error en la consulta individual: ", re);
            return null;
        }

        String jsonFormateado = StringUtils.removeEnd(StringUtils.removeStart(response.getBody(), "\""), "\"");
        String jsonUnEscapeJson = StringEscapeUtils.unescapeJson(jsonFormateado);

        try {
            sunatCpeResponseExternal = new ObjectMapper().readValue(jsonUnEscapeJson, SunatCpeResponseExternal.class);
        } catch (Exception e) {
            log.error("Error parsear JSON SUNAT Cpe response: {}", e);
            return null;
        }

        return sunatCpeResponseExternal;
    }

    private String obtenerCookieValido() {
        return SUNAT_COOKIE;
    }

}