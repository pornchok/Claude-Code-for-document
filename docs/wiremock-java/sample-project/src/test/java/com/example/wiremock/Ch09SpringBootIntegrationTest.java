package com.example.wiremock;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Ch09SpringBootIntegrationTest {

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

    @Test
    void getPayment_shouldReturnPaymentFromSpringContext() {
        // Arrange — stub WireMock
        wireMock.stubFor(get(urlEqualTo("/payments/order-555"))
            .willReturn(okJson("{\"id\":\"order-555\",\"status\":\"successful\",\"amount\":1500,\"currency\":\"THB\"}")));

        // Act — PaymentClient ถูก inject จาก Spring context
        PaymentResponse response = paymentClient.getPayment("order-555");

        // Assert
        assertThat(response.getId()).isEqualTo("order-555");
        assertThat(response.getStatus()).isEqualTo("successful");
        assertThat(response.getAmount()).isEqualTo(1500);

        // Verify — Spring context ส่ง request ไปที่ WireMock จริง
        wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/order-555")));
    }

    @Test
    void createPayment_shouldPostToWireMockViaSpringBean() {
        // Arrange
        wireMock.stubFor(post(urlEqualTo("/payments"))
            .willReturn(okJson("{\"id\":\"new-001\",\"status\":\"pending\",\"amount\":3000,\"currency\":\"THB\"}")));

        PaymentRequest request = new PaymentRequest(3000, "THB", "Order #999");

        // Act
        PaymentResponse response = paymentClient.createPayment(request);

        // Assert
        assertThat(response.getId()).isEqualTo("new-001");
        assertThat(response.getStatus()).isEqualTo("pending");

        // Verify
        wireMock.verify(1, postRequestedFor(urlEqualTo("/payments")));
    }

    @Test
    void stubsAreResetBetweenTests_previousStubShouldNotExist() {
        // ถ้า stub จาก test แรกยังอยู่ test นี้จะ fail เพราะ /payments/order-555 ไม่มี stub
        // WireMockExtension จะ reset stubs อัตโนมัติก่อนแต่ละ test
        wireMock.stubFor(get(urlEqualTo("/payments/reset-test"))
            .willReturn(okJson("{\"id\":\"reset-test\",\"status\":\"ok\",\"amount\":100,\"currency\":\"THB\"}")));

        PaymentResponse response = paymentClient.getPayment("reset-test");
        assertThat(response.getStatus()).isEqualTo("ok");
    }
}
