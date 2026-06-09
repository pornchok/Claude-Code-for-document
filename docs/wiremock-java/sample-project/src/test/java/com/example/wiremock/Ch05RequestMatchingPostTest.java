package com.example.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.web.client.RestTemplate;

class Ch05RequestMatchingPostTest {
    private WireMockServer wireMock;
    private PaymentClient paymentClient;

    @BeforeEach void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        paymentClient = new PaymentClient(new RestTemplate(), "http://localhost:" + wireMock.port());
    }

    @AfterEach void tearDown() { wireMock.stop(); }

    @Test
    void createPaymentMatchesExactBody() {
        wireMock.stubFor(post(urlEqualTo("/payments"))
            .withRequestBody(equalToJson("""
                {"amount": 500, "currency": "THB", "description": "Order #001"}
                """))
            .willReturn(okJson("""
                {"id":"pay_001","status":"success","amount":500,"currency":"THB"}
                """)));

        PaymentResponse response = paymentClient.createPayment(
            new PaymentRequest(500, "THB", "Order #001"));

        assertThat(response.getId()).isEqualTo("pay_001");
        assertThat(response.getStatus()).isEqualTo("success");
        wireMock.verify(1, postRequestedFor(urlEqualTo("/payments")));
    }

    @Test
    void createPaymentWithIgnoreExtraElements() {
        wireMock.stubFor(post(urlEqualTo("/payments"))
            .withRequestBody(equalToJson("""
                {"amount": 1000, "currency": "THB"}
                """, true, true))
            .willReturn(okJson("""
                {"id":"pay_002","status":"success","amount":1000,"currency":"THB"}
                """)));

        // request has extra field "description" — still matches because ignoreExtraElements=true
        PaymentResponse response = paymentClient.createPayment(
            new PaymentRequest(1000, "THB", "Subscription renewal"));

        assertThat(response.getId()).isEqualTo("pay_002");
    }
}
