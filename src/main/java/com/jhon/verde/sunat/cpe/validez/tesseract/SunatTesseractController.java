package com.jhon.verde.sunat.cpe.validez.tesseract;

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
@RequestMapping("/api/sunat/tesseract")
public class SunatTesseractController {

    @Autowired
    private SunatTesseractCpeService sunatTesseractCpeService;


    @PostMapping(value = "/cpe/validez", produces = MediaType.APPLICATION_JSON_VALUE)
    public SunatTesseractCpeResponse procesarCpe(@Valid @RequestBody CpeRequest request) {
        return sunatTesseractCpeService.validarCpe(request);
    }

    @PostMapping(value = "/cpe/validez/lote", produces = MediaType.APPLICATION_JSON_VALUE)
    public String procesarCpeLoteParalelo(@Valid @RequestBody CpeRequest[] requests) throws IOException {
        sunatTesseractCpeService.procesarCpesEnLoteSecuencial(requests);
        return "Se esta procesando su solicitud. En unos minutos le llegara un correo informandole de lo sucedido.";
    }

}
