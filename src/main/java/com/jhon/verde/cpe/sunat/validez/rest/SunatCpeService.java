package com.jhon.verde.cpe.sunat.validez.rest;

import com.jhon.verde.cpe.sunat.validez.dto.CpeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class SunatCpeService {

    @Autowired
    private SunatApiRestCpeService sunatApiRestCpeService;

    @Async
    public void procesarCpesEnLoteSecuencial(CpeRequest[] sunatCpeRequests) throws IOException{
        log.info("Inicia procesar CPEs en lote secuencial.");
        List<CpeRequest> listaRequestFuture1 = new ArrayList<>();
        List<CpeRequest> listaRequestFuture2 = new ArrayList<>();
        List<CpeRequest> listaRequestFuture3 = new ArrayList<>();
        List<CpeRequest> listaRequestProcesados = new ArrayList<>();

        listaRequestFuture1 = obtenerListaRequest(sunatCpeRequests[0]);
        listaRequestFuture2 = obtenerListaRequest(sunatCpeRequests[1]);
        listaRequestFuture3 = obtenerListaRequest(sunatCpeRequests[2]);

        Long inicio = System.currentTimeMillis();

        for (int i = 0; i < 3; i++) {
            List<CpeRequest> listaFuture = procesarCpesEnLote(listaRequestFuture1);
            listaRequestProcesados.addAll(listaFuture);
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
                e.printStackTrace();
            }
        });

        bufferedWriter.close();
        writer.close();
        log.info("Termino de crear archivo txt");
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
                    cpe.setSunatApiRestCpeResponse(procesarCpe(cpe));
                    if((contador.get() > 0) && (contador.get() % 500 == 0)){
                        log.info("Se procesaron 500 CPEs");
                    }
                    contador.incrementAndGet();
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

    public SunatApiRestCpeResponse procesarCpe(CpeRequest request){
        return sunatApiRestCpeService.validarCpe(request);
    }

}
