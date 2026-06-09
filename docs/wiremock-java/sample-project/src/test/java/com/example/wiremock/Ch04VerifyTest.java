package com.example.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.web.client.RestTemplate;

class Ch04VerifyTest {
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
    void verifyPaymentApiWasCalledExactlyOnce() {
        wireMock.stubFor(get("/payments/123")
            .willReturn(okJson("""
                {"id":"123","status":"success","amount":500,"currency":"THB"}
                """)));
        paymentClient.getPayment("123");
        wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/123")));
    }

    @Test
    void verifyPaymentApiWasNotCalled() {
        wireMock.stubFor(get("/payments/123").willReturn(okJson("{}")));
        // intentionally NOT calling paymentClient
        wireMock.verify(0, getRequestedFor(urlEqualTo("/payments/123")));
    }
}
