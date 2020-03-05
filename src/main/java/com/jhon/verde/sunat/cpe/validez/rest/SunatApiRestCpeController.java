package com.jhon.verde.sunat.cpe.validez.rest;

import com.jhon.verde.sunat.cpe.validez.dto.CpeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/sunat/rest")
public class SunatApiRestCpeController {

    @Autowired
    private SunatApiRestCpeService sunatApiRestCpeService;

    @PostMapping(value = "/cpe/validez", produces = MediaType.APPLICATION_JSON_VALUE)
    public SunatApiRestCpeResponse procesarCpe(@Valid @RequestBody CpeRequest request) {
        return sunatApiRestCpeService.validarCpe(request);
    }

    @PostMapping(value = "/cpe/validez/lote", produces = MediaType.APPLICATION_JSON_VALUE)
    public String procesarCpeLoteParalelo(@Valid @RequestBody CpeRequest[] requests) throws IOException {
        sunatApiRestCpeService.procesarCpesEnLoteParalelo(requests);
        return "Se esta procesando su solicitud. En unos minutos le llegara un correo informandole de lo sucedido.";
    }

    @PostMapping(value = "/cpe/validez/lote-secuencial", produces = MediaType.APPLICATION_JSON_VALUE)
    public String procesarCpeLoteSecuencial(@Valid @RequestBody CpeRequest[] requests) throws Exception {
        sunatApiRestCpeService.procesarCpesEnLoteSecuencial(requests);
        return "Se esta procesando su solicitud. En unos minutos le llegara un correo informandole de lo sucedido.";
    }

}
