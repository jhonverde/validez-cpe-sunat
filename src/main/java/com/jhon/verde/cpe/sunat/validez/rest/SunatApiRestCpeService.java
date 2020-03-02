package com.jhon.verde.cpe.sunat.validez.rest;

import com.jhon.verde.cpe.sunat.validez.dto.CpeRequest;
import com.jhon.verde.cpe.sunat.validez.exception.NegocioException;
import com.jhon.verde.cpe.sunat.validez.config.SunatOAuth2Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

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
    private SunatOAuth2Properties sunatOAuth2Properties;

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

}
