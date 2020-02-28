package com.jhon.verde.cpe.sunat.validez;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class SunatCpeController {

    @Autowired
    private SunatCpeService sunatCpeService;


    @PostMapping(value = "/cpe/validez", produces = MediaType.APPLICATION_JSON_VALUE)
    public SunatCpeResponseExternal procesarCpe(@Valid @RequestBody SunatCpeRequest request) {
        return sunatCpeService.procesarCpe(request);
    }

    @PostMapping(value = "/cpe/validez/lote", produces = MediaType.APPLICATION_JSON_VALUE)
    public String procesarCpeLoteParalelo(@Valid @RequestBody SunatCpeRequest[] requests) throws IOException {
        sunatCpeService.procesarCpesEnLoteParalelo(requests);
        return "Se esta procesando su solicitud. En unos minutos le llegara un correo informandole de lo sucedido.";
    }

    @PostMapping(value = "/cpe/validez/lote-secuencial", produces = MediaType.APPLICATION_JSON_VALUE)
    public String procesarCpeLoteSecuencial(@Valid @RequestBody SunatCpeRequest[] requests) throws Exception {
        sunatCpeService.procesarCpesEnLoteSecuencial(requests);
        return "Se esta procesando su solicitud. En unos minutos le llegara un correo informandole de lo sucedido.";
    }
}
