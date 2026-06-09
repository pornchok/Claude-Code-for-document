package com.example.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.web.client.RestTemplate;

class Ch02SetupTest {
    private WireMockServer wireMock;
    private RestTemplate restTemplate;

    @BeforeEach void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        restTemplate = new RestTemplate();
    }

    @AfterEach void tearDown() { wireMock.stop(); }

    @Test
    void wireMockServerStartsSuccessfully() {
        wireMock.stubFor(get("/health").willReturn(ok("OK")));
        String result = restTemplate.getForObject(
            "http://localhost:" + wireMock.port() + "/health", String.class);
        assertThat(result).isEqualTo("OK");
    }
}
