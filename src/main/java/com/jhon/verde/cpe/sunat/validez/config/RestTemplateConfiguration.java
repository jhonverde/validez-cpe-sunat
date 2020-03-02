package com.jhon.verde.cpe.sunat.validez.config;

import com.jhon.verde.cpe.sunat.validez.rest.SunatApiRestCpeService;
import com.jhon.verde.cpe.sunat.validez.rest.SunatApiRestTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;

@Configuration
@Slf4j
public class RestTemplateConfiguration {

    @Autowired
    private SunatApiRestCpeService sunatApiRestCpeService;

    private SunatApiRestTokenResponse sunatApiRestTokenResponse;

    @PostConstruct
    public void inicializarToken(){
        sunatApiRestTokenResponse = new SunatApiRestTokenResponse();
        sunatApiRestTokenResponse.setAccessToken("MAL");
        //sunatApiRestTokenResponse = sunatApiRestCpeService.obtenerToken();
    }

    @Bean("restTemplateSimple")
    public RestTemplate obtenerRestTemplateSimple(){
        return new RestTemplate();
    }

    @Bean("restTemplateApiRestSunat")
    public RestTemplate RestTemplate(){
        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        //RestTemplate restTemplate = new RestTemplate();
        //restTemplate.setInterceptors(Collections.singletonList(new HttpStatusUnauthorizedInterceptor()));
        restTemplate.getInterceptors().add(new HttpStatusUnauthorizedInterceptor());
        return restTemplate;
    }

    public class HttpStatusUnauthorizedInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + sunatApiRestTokenResponse.getAccessToken());
            ClientHttpResponse response = execution.execute(request, body);

            if(HttpStatus.UNAUTHORIZED.equals(response.getStatusCode())){
                log.info("Token no autorizado: {}", sunatApiRestTokenResponse.getAccessToken());
                log.info("Obteniendo token.");
                sunatApiRestTokenResponse = sunatApiRestCpeService.obtenerToken();
                log.info("Token: {}", sunatApiRestTokenResponse.getAccessToken().hashCode());
                request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + sunatApiRestTokenResponse.getAccessToken());
                return execution.execute(request, body);
            }

            return response;
        }
    }

}
