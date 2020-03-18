package com.jhon.verde.sunat.cpe.validez.controller;

import com.jhon.verde.sunat.cpe.validez.dto.CpeRequest;
import com.jhon.verde.sunat.cpe.validez.dto.SunatApiRestCpeResponse;
import com.jhon.verde.sunat.cpe.validez.dto.SunatTesseractCpeResponse;
import com.jhon.verde.sunat.cpe.validez.facade.SunatApiRestCpeFacade;
import com.jhon.verde.sunat.cpe.validez.facade.SunatTesseractCpeFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/sunat")
@Slf4j
public class Controller {

    @Autowired
    private SunatApiRestCpeFacade sunatApiRestCpeFacade;

    @Autowired
    private SunatTesseractCpeFacade sunatTesseractCpeFacade;

    @PostMapping(value = "/cpe/validez/tesseract", produces = MediaType.APPLICATION_JSON_VALUE)
    public SunatTesseractCpeResponse procesarCpePorTesseract(@Valid @RequestBody CpeRequest request) {
        return sunatTesseractCpeFacade.validarCpe(request);
    }

    @PostMapping(value = "/cpe/validez/rest", produces = MediaType.APPLICATION_JSON_VALUE)
    public SunatApiRestCpeResponse procesarCpePorApiRest(@Valid @RequestBody CpeRequest request) {
        return sunatApiRestCpeFacade.validarCpe(request);
    }

    @PostMapping(value = "/cpe/validez/rest/lote", produces = MediaType.APPLICATION_JSON_VALUE)
    @Async
    public String procesarPorApiRestCpesEnLote(@Valid @RequestBody CpeRequest[] requests) throws IOException, ExecutionException, InterruptedException {
        procesarPorApiRestCpes(requests);
        return "Se esta procesando su solicitud. En unos minutos se creara un archivo txt con los CPEs validados.";
    }

    public void procesarPorApiRestCpes(CpeRequest[] sunatCpeRequests) throws IOException, ExecutionException, InterruptedException {
        log.info("Inicia procesar CPEs en lote.");
        List<CpeRequest> listaRequests = new ArrayList<>();
        List<CpeRequest> listaRequestProcesados = new ArrayList<>();

        listaRequests = obtenerListaRequest(sunatCpeRequests[0]);

        Long inicio = System.currentTimeMillis();

        for (int i = 0; i < 3; i++) {
            List<CpeRequest> listaFuture = procesarCpesEnLote(listaRequests).get();
            listaRequestProcesados.addAll(listaFuture);
        }

        Long fin = System.currentTimeMillis();
        log.info("Tiempo de procesar todos los comprobantes: {}", fin - inicio);

        FileWriter writer = new FileWriter("CpesProcesados.txt", false);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        AtomicInteger contadorCpes = new AtomicInteger(1);

        listaRequestProcesados.stream().forEach(cpe -> {
            try {
                bufferedWriter.write(String.join("|", String.valueOf(contadorCpes.get()), cpe.getNombreCpe(), cpe.getSunatApiRestCpeResponse().getSuccess(), cpe.getSunatApiRestCpeResponse().getMessage()));
                bufferedWriter.newLine();
                contadorCpes.incrementAndGet();
            } catch (IOException e) {
                log.error("Error al escribir en el archivo TXT. Excepcion: {}", e);
            }
        });

        bufferedWriter.close();
        writer.close();
        log.info("Termino de crear archivo txt");
    }

    private List<CpeRequest> obtenerListaRequest(CpeRequest sunatCpeRequest) {
        List<CpeRequest> listaRequest = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            listaRequest.add(sunatCpeRequest);
        }
        return listaRequest;
    }

    private CompletableFuture<List<CpeRequest>> procesarCpesEnLote(List<CpeRequest> listaCpes) {
        return CompletableFuture.supplyAsync(() -> {
            listaCpes.parallelStream().forEach(cpe -> {
                cpe.setSunatApiRestCpeResponse(sunatApiRestCpeFacade.validarCpe(cpe));
            });
            return listaCpes;
        });
    }

}
