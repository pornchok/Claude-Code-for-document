package com.example.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.web.client.RestTemplate;

class Ch07ResponseTemplatingTest {
    private WireMockServer wireMock;
    private PaymentClient paymentClient;

    @BeforeEach void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        paymentClient = new PaymentClient(new RestTemplate(), "http://localhost:" + wireMock.port());
    }

    @AfterEach void tearDown() { wireMock.stop(); }

    @Test
    void echoPaymentIdFromPath() {
        // withTransformers("response-template") enables templating per-stub
        wireMock.stubFor(get(urlPathEqualTo("/payments/pay_t01"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\":\"{{request.pathSegments.[1]}}\",\"status\":\"success\",\"amount\":500,\"currency\":\"THB\"}")
                .withTransformers("response-template")));

        PaymentResponse response = paymentClient.getPayment("pay_t01");

        assertThat(response.getId()).isEqualTo("pay_t01");
        assertThat(response.getStatus()).isEqualTo("success");
    }

    @Test
    void echoRequestIdHeader() {
        wireMock.stubFor(get(urlPathEqualTo("/payments/pay_h01"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withHeader("X-Echo-Id", "{{request.headers.X-Request-Id}}")
                .withBody("{\"id\":\"pay_h01\",\"status\":\"success\",\"amount\":0,\"currency\":\"THB\"}")
                .withTransformers("response-template")));

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Request-Id", "req-abc-123");
        org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
        org.springframework.http.ResponseEntity<PaymentResponse> resp =
            new RestTemplate().exchange(
                "http://localhost:" + wireMock.port() + "/payments/pay_h01",
                org.springframework.http.HttpMethod.GET,
                entity,
                PaymentResponse.class);

        assertThat(resp.getHeaders().getFirst("X-Echo-Id")).isEqualTo("req-abc-123");
        assertThat(resp.getBody().getId()).isEqualTo("pay_h01");
    }
}
