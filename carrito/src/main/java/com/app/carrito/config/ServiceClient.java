package com.app.carrito.config;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public <T> ResponseEntity<T> enviarConToken(
            String url,
            HttpMethod method,
            Object body,
            Class<T> responseType,
            String token
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> entity = (body != null) ? new HttpEntity<>(body, headers) : new HttpEntity<>(headers);

        return restTemplate.exchange(url, method, entity, responseType);
    }
}