package com.example.wiremock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PaymentClient(RestTemplate restTemplate,
                         @Value("${payment.service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public PaymentResponse getPayment(String paymentId) {
        return restTemplate.getForObject(
            baseUrl + "/payments/" + paymentId,
            PaymentResponse.class
        );
    }

    public PaymentResponse createPayment(PaymentRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.postForObject(
            baseUrl + "/payments",
            entity,
            PaymentResponse.class
        );
    }
}
