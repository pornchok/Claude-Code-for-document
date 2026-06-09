package com.example.wiremock;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Ch10ErrorSimulationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.service.url", wireMock::baseUrl);
    }

    @Autowired
    private PaymentClient paymentClient;

    // ─── Test 1: 404 Not Found ──────────────────────────────────────────────

    @Test
    void getPayment_404_shouldThrowHttpClientErrorException() {
        wireMock.stubFor(get(urlEqualTo("/payments/not-found"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Payment not found\"}")));

        assertThatThrownBy(() -> paymentClient.getPayment("not-found"))
            .isInstanceOf(HttpClientErrorException.class)
            .hasMessageContaining("404");

        wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/not-found")));
    }

    // ─── Test 2: 500 Server Error ───────────────────────────────────────────

    @Test
    void createPayment_500_shouldThrowHttpServerErrorException() {
        wireMock.stubFor(post(urlEqualTo("/payments"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Internal Server Error\"}")));

        PaymentRequest request = new PaymentRequest(5000, "THB", "Test order");

        assertThatThrownBy(() -> paymentClient.createPayment(request))
            .isInstanceOf(HttpServerErrorException.class)
            .hasMessageContaining("500");

        wireMock.verify(1, postRequestedFor(urlEqualTo("/payments")));
    }

    // ─── Test 3: Fixed Delay ────────────────────────────────────────────────

    @Test
    void getPayment_withFixedDelay_shouldStillReturnResponse() {
        // Stub ที่มี delay 1000ms — ทดสอบว่า request สำเร็จแม้จะช้า
        wireMock.stubFor(get(urlEqualTo("/payments/slow-pay"))
            .willReturn(okJson("{\"id\":\"slow-pay\",\"status\":\"successful\",\"amount\":200,\"currency\":\"THB\"}")
                .withFixedDelay(1000)));

        long start = System.currentTimeMillis();
        PaymentResponse response = paymentClient.getPayment("slow-pay");
        long elapsed = System.currentTimeMillis() - start;

        // Response ต้องถูกต้อง
        assertThat(response.getId()).isEqualTo("slow-pay");
        assertThat(response.getStatus()).isEqualTo("successful");

        // ต้องใช้เวลาอย่างน้อย 1000ms
        assertThat(elapsed).isGreaterThanOrEqualTo(1000L);

        wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/slow-pay")));
    }

    // ─── Test 4: Connection Reset by Peer ──────────────────────────────────

    @Test
    void getPayment_connectionReset_shouldThrowResourceAccessException() {
        // Fault.CONNECTION_RESET_BY_PEER — ตัด connection ทันที ไม่ส่ง response
        wireMock.stubFor(get(urlEqualTo("/payments/fault-test"))
            .willReturn(aResponse()
                .withFault(Fault.CONNECTION_RESET_BY_PEER)));

        // RestTemplate จะ throw ResourceAccessException เมื่อ connection ถูกตัด
        assertThatThrownBy(() -> paymentClient.getPayment("fault-test"))
            .isInstanceOf(ResourceAccessException.class);
    }
}
