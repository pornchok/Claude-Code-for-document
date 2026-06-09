package com.example.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.web.client.RestTemplate;

class Ch03BasicStubTest {
    private WireMockServer wireMock;
    private PaymentClient paymentClient;

    @BeforeEach void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        paymentClient = new PaymentClient(new RestTemplate(),
            "http://localhost:" + wireMock.port());
    }

    @AfterEach void tearDown() { wireMock.stop(); }

    @Test
    void getPaymentReturnsStubResponse() {
        wireMock.stubFor(get("/payments/123")
            .willReturn(okJson("""
                {"id":"123","status":"success","amount":500,"currency":"THB"}
                """)));
        PaymentResponse response = paymentClient.getPayment("123");
        assertThat(response.getId()).isEqualTo("123");
        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getAmount()).isEqualTo(500);
    }

    @Test
    void stubNotMatchedReturns404() {
        wireMock.stubFor(get("/payments/123").willReturn(okJson("{}")));
        // calling different URL — stub doesn't match
        try {
            paymentClient.getPayment("999");
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("404");
        }
    }
}
