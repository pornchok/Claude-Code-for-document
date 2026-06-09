package com.example.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.web.client.RestTemplate;

class Ch08ScenariosTest {
    private WireMockServer wireMock;
    private PaymentClient paymentClient;

    @BeforeEach void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        paymentClient = new PaymentClient(new RestTemplate(), "http://localhost:" + wireMock.port());
    }

    @AfterEach void tearDown() { wireMock.stop(); }

    @Test
    void paymentPendingThenSuccessOnRetry() {
        // First call: payment is pending
        wireMock.stubFor(get(urlEqualTo("/payments/scenario-123"))
            .inScenario("payment-retry")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("PENDING")
            .willReturn(aResponse()
                .withStatus(202)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\":\"scenario-123\",\"status\":\"pending\",\"amount\":500,\"currency\":\"THB\"}")));

        // Second call: payment succeeded
        wireMock.stubFor(get(urlEqualTo("/payments/scenario-123"))
            .inScenario("payment-retry")
            .whenScenarioStateIs("PENDING")
            .willSetStateTo("SUCCESS")
            .willReturn(okJson("{\"id\":\"scenario-123\",\"status\":\"success\",\"amount\":500,\"currency\":\"THB\"}")));

        // First attempt — pending
        PaymentResponse first = paymentClient.getPayment("scenario-123");
        assertThat(first.getStatus()).isEqualTo("pending");

        // Second attempt (retry) — success
        PaymentResponse second = paymentClient.getPayment("scenario-123");
        assertThat(second.getStatus()).isEqualTo("success");

        // Verify exactly 2 calls were made
        wireMock.verify(2, getRequestedFor(urlEqualTo("/payments/scenario-123")));
    }
}
