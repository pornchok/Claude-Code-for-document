package com.example.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

class Ch06RequestMatchingHeadersTest {
    private WireMockServer wireMock;
    private RestTemplate restTemplate;

    @BeforeEach void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        restTemplate = new RestTemplate();
    }

    @AfterEach void tearDown() { wireMock.stop(); }

    @Test
    void matchByContentTypeHeader() {
        wireMock.stubFor(post(urlEqualTo("/payments"))
            .withHeader("Content-Type", equalTo("application/json"))
            .willReturn(okJson("{\"id\":\"pay_h01\",\"status\":\"success\",\"amount\":0,\"currency\":\"THB\"}")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{\"amount\":100}", headers);
        ResponseEntity<PaymentResponse> resp = restTemplate.postForEntity(
            "http://localhost:" + wireMock.port() + "/payments", entity, PaymentResponse.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody().getId()).isEqualTo("pay_h01");
    }

    @Test
    void matchByQueryParam() {
        wireMock.stubFor(get(urlPathEqualTo("/payments"))
            .withQueryParam("currency", equalTo("THB"))
            .willReturn(okJson("{\"id\":\"pay_q01\",\"status\":\"success\",\"amount\":0,\"currency\":\"THB\"}")));

        PaymentResponse resp = restTemplate.getForObject(
            "http://localhost:" + wireMock.port() + "/payments?currency=THB",
            PaymentResponse.class);
        assertThat(resp.getId()).isEqualTo("pay_q01");
    }
}
