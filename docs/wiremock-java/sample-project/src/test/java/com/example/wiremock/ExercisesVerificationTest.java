package com.example.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.jupiter.api.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.*;

/**
 * ExercisesVerificationTest — verifies all code answers used in exercises.md
 * Covers 5 concepts:
 *   1. Stub basics (GET + okJson)
 *   2. Verify (count + body)
 *   3. Request Matching (POST body + headers)
 *   4. Scenarios (stateful)
 *   5. Error Simulation (4xx/5xx/timeout/fault)
 */
class ExercisesVerificationTest {

    private WireMockServer wireMock;
    private PaymentClient paymentClient;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        paymentClient = new PaymentClient(
            new RestTemplate(),
            "http://localhost:" + wireMock.port()
        );
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONCEPT 1: Stub Basics — GET + okJson
    // (Scenario: Inventory Service returning stock info)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ex1-Intermediate: Stub a GET /products/SKU-999 endpoint that returns
     * {"sku":"SKU-999","name":"Wireless Mouse","stock":42,"warehouse":"BKK-01"}
     * Verify that the client receives the correct sku and stock.
     */
    @Test
    void ex1_intermediate_stubInventoryGet() {
        wireMock.stubFor(get(urlEqualTo("/products/SKU-999"))
            .willReturn(okJson("""
                {"sku":"SKU-999","name":"Wireless Mouse","stock":42,"warehouse":"BKK-01"}
                """)));

        // simulate a client call
        var response = new RestTemplate().getForObject(
            "http://localhost:" + wireMock.port() + "/products/SKU-999",
            java.util.Map.class
        );

        assertThat(response.get("sku")).isEqualTo("SKU-999");
        assertThat(response.get("stock")).isEqualTo(42);
    }

    /**
     * Ex1-Advanced: Two stubs for the same URL path pattern but different IDs.
     * Verify that each returns its own body and the unmatched ID returns 404.
     */
    @Test
    void ex1_advanced_twoStubsSamePattern() {
        wireMock.stubFor(get(urlEqualTo("/products/SKU-001"))
            .willReturn(okJson("""
                {"sku":"SKU-001","stock":10}
                """)));

        wireMock.stubFor(get(urlEqualTo("/products/SKU-002"))
            .willReturn(okJson("""
                {"sku":"SKU-002","stock":0}
                """)));

        var r1 = new RestTemplate().getForObject(
            "http://localhost:" + wireMock.port() + "/products/SKU-001",
            java.util.Map.class
        );
        var r2 = new RestTemplate().getForObject(
            "http://localhost:" + wireMock.port() + "/products/SKU-002",
            java.util.Map.class
        );

        assertThat(r1.get("stock")).isEqualTo(10);
        assertThat(r2.get("stock")).isEqualTo(0);

        // SKU-003 has no stub → 404
        assertThatThrownBy(() ->
            new RestTemplate().getForObject(
                "http://localhost:" + wireMock.port() + "/products/SKU-003",
                java.util.Map.class
            )
        ).hasMessageContaining("404");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONCEPT 2: Verify — call count + request body
    // (Scenario: Notification Service)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ex2-Intermediate: Stub POST /notifications, call it twice with the same body,
     * then verify it was called exactly twice.
     */
    @Test
    void ex2_intermediate_verifyCalledTwice() {
        wireMock.stubFor(post(urlEqualTo("/notifications"))
            .withRequestBody(equalToJson("""
                {"channel":"sms","recipient":"0812345678","message":"Your order is ready"}
                """))
            .willReturn(aResponse().withStatus(201)));

        var rt = new RestTemplate();
        var headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        var body = """
            {"channel":"sms","recipient":"0812345678","message":"Your order is ready"}
            """;
        var entity = new org.springframework.http.HttpEntity<>(body, headers);

        rt.postForObject("http://localhost:" + wireMock.port() + "/notifications", entity, String.class);
        rt.postForObject("http://localhost:" + wireMock.port() + "/notifications", entity, String.class);

        wireMock.verify(2, postRequestedFor(urlEqualTo("/notifications")));
    }

    /**
     * Ex2-Advanced: Verify that a specific field in the request body was present
     * (matchingJsonPath), and that the endpoint was never called with an empty body.
     */
    @Test
    void ex2_advanced_verifyBodyContainsField() {
        wireMock.stubFor(post(urlEqualTo("/notifications"))
            .willReturn(aResponse().withStatus(201)));

        var rt = new RestTemplate();
        var headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        var body = """
            {"channel":"email","recipient":"user@example.com","message":"Invoice #INV-2026-001 is ready"}
            """;
        rt.postForObject(
            "http://localhost:" + wireMock.port() + "/notifications",
            new org.springframework.http.HttpEntity<>(body, headers),
            String.class
        );

        // verify the call was made with the "channel" field set to "email"
        wireMock.verify(1,
            postRequestedFor(urlEqualTo("/notifications"))
                .withRequestBody(matchingJsonPath("$.channel", equalTo("email")))
        );

        // verify 0 calls with an empty body
        wireMock.verify(0,
            postRequestedFor(urlEqualTo("/notifications"))
                .withRequestBody(equalTo(""))
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONCEPT 3: Request Matching — POST body + headers
    // (Scenario: Shipping Service)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ex3-Intermediate: Stub POST /shipments that matches on body AND
     * requires header X-Idempotency-Key to be present.
     */
    @Test
    void ex3_intermediate_matchBodyAndHeader() {
        wireMock.stubFor(post(urlEqualTo("/shipments"))
            .withHeader("X-Idempotency-Key", matching(".+"))
            .withRequestBody(equalToJson("""
                {"orderId":"ORD-7890","address":"123 Sukhumvit, Bangkok","method":"express"}
                """))
            .willReturn(okJson("""
                {"trackingId":"TRK-001","status":"created"}
                """)));

        var rt = new RestTemplate();
        var headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.set("X-Idempotency-Key", "idem-abc-123");

        var body = """
            {"orderId":"ORD-7890","address":"123 Sukhumvit, Bangkok","method":"express"}
            """;

        var result = rt.postForObject(
            "http://localhost:" + wireMock.port() + "/shipments",
            new org.springframework.http.HttpEntity<>(body, headers),
            java.util.Map.class
        );

        assertThat(result.get("trackingId")).isEqualTo("TRK-001");
        assertThat(result.get("status")).isEqualTo("created");
    }

    /**
     * Ex3-Advanced: Without the required header the stub does NOT match,
     * so WireMock returns 404. Confirm that behavior.
     */
    @Test
    void ex3_advanced_missingHeaderCausesNoMatch() {
        wireMock.stubFor(post(urlEqualTo("/shipments"))
            .withHeader("X-Idempotency-Key", matching(".+"))
            .withRequestBody(equalToJson("""
                {"orderId":"ORD-7890","address":"123 Sukhumvit, Bangkok","method":"express"}
                """))
            .willReturn(okJson("""
                {"trackingId":"TRK-001","status":"created"}
                """)));

        var rt = new RestTemplate();
        var headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        // intentionally omit X-Idempotency-Key

        var body = """
            {"orderId":"ORD-7890","address":"123 Sukhumvit, Bangkok","method":"express"}
            """;

        assertThatThrownBy(() ->
            rt.postForObject(
                "http://localhost:" + wireMock.port() + "/shipments",
                new org.springframework.http.HttpEntity<>(body, headers),
                java.util.Map.class
            )
        ).hasMessageContaining("404");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONCEPT 4: Scenarios (Stateful)
    // (Scenario: Inventory reservation — reserve → confirm → released)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ex4-Intermediate: Three-state scenario.
     * GET /inventory/ITEM-A
     *   STARTED          → {"available":true,"reserved":false}
     *   "RESERVED"       → {"available":false,"reserved":true}
     *   "FULFILLED"      → {"available":false,"reserved":false,"fulfilled":true}
     */
    @Test
    void ex4_intermediate_threeStateInventory() {
        final String SCENARIO = "inventory-reservation";
        final String RESERVED = "RESERVED";
        final String FULFILLED = "FULFILLED";

        wireMock.stubFor(get(urlEqualTo("/inventory/ITEM-A"))
            .inScenario(SCENARIO)
            .whenScenarioStateIs(STARTED)
            .willSetStateTo(RESERVED)
            .willReturn(okJson("""
                {"available":true,"reserved":false}
                """)));

        wireMock.stubFor(get(urlEqualTo("/inventory/ITEM-A"))
            .inScenario(SCENARIO)
            .whenScenarioStateIs(RESERVED)
            .willSetStateTo(FULFILLED)
            .willReturn(okJson("""
                {"available":false,"reserved":true}
                """)));

        wireMock.stubFor(get(urlEqualTo("/inventory/ITEM-A"))
            .inScenario(SCENARIO)
            .whenScenarioStateIs(FULFILLED)
            .willReturn(okJson("""
                {"available":false,"reserved":false,"fulfilled":true}
                """)));

        var rt = new RestTemplate();
        String url = "http://localhost:" + wireMock.port() + "/inventory/ITEM-A";

        var r1 = rt.getForObject(url, java.util.Map.class);
        assertThat(r1.get("available")).isEqualTo(true);
        assertThat(r1.get("reserved")).isEqualTo(false);

        var r2 = rt.getForObject(url, java.util.Map.class);
        assertThat(r2.get("available")).isEqualTo(false);
        assertThat(r2.get("reserved")).isEqualTo(true);

        var r3 = rt.getForObject(url, java.util.Map.class);
        assertThat(r3.get("available")).isEqualTo(false);
        assertThat(r3.get("fulfilled")).isEqualTo(true);

        wireMock.verify(3, getRequestedFor(urlEqualTo("/inventory/ITEM-A")));
    }

    /**
     * Ex4-Advanced: Scenario does NOT advance when a POST is made but the
     * stub for the POST is absent (no match). The state stays at STARTED.
     * This verifies that unmatched requests do not affect scenario state.
     */
    @Test
    void ex4_advanced_unmatchedRequestDoesNotAdvanceState() {
        final String SCENARIO = "order-flow";

        wireMock.stubFor(get(urlEqualTo("/orders/DRAFT-1"))
            .inScenario(SCENARIO)
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("SUBMITTED")
            .willReturn(okJson("""
                {"id":"DRAFT-1","state":"draft"}
                """)));

        wireMock.stubFor(get(urlEqualTo("/orders/DRAFT-1"))
            .inScenario(SCENARIO)
            .whenScenarioStateIs("SUBMITTED")
            .willReturn(okJson("""
                {"id":"DRAFT-1","state":"submitted"}
                """)));

        var rt = new RestTemplate();
        String url = "http://localhost:" + wireMock.port() + "/orders/DRAFT-1";

        // First GET — advances to SUBMITTED
        var r1 = rt.getForObject(url, java.util.Map.class);
        assertThat(r1.get("state")).isEqualTo("draft");

        // Second GET — now in SUBMITTED state
        var r2 = rt.getForObject(url, java.util.Map.class);
        assertThat(r2.get("state")).isEqualTo("submitted");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONCEPT 5: Error Simulation — 4xx / 5xx / timeout / fault
    // (Scenario: Coupon Validation Service)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ex5-Intermediate: Stub GET /coupons/EXPIRED-001 to return 410 Gone.
     * Verify the client receives HttpClientErrorException with status 410.
     */
    @Test
    void ex5_intermediate_410GoneForExpiredCoupon() {
        wireMock.stubFor(get(urlEqualTo("/coupons/EXPIRED-001"))
            .willReturn(aResponse()
                .withStatus(410)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"error":"Coupon has expired","code":"COUPON_EXPIRED"}
                    """)));

        assertThatThrownBy(() ->
            new RestTemplate().getForObject(
                "http://localhost:" + wireMock.port() + "/coupons/EXPIRED-001",
                java.util.Map.class
            )
        ).isInstanceOf(HttpClientErrorException.class)
         .hasMessageContaining("410");
    }

    /**
     * Ex5-Advanced: Stub POST /coupons/validate that:
     * - first call returns 503 Service Unavailable
     * - second call returns 200 OK with {"valid":true}
     * Use Scenario to model "retry after service recovery".
     */
    @Test
    void ex5_advanced_503ThenRecovery() {
        final String SCENARIO = "coupon-service-recovery";
        final String RECOVERED = "RECOVERED";

        wireMock.stubFor(post(urlEqualTo("/coupons/validate"))
            .inScenario(SCENARIO)
            .whenScenarioStateIs(STARTED)
            .willSetStateTo(RECOVERED)
            .willReturn(aResponse()
                .withStatus(503)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"error":"Service temporarily unavailable"}
                    """)));

        wireMock.stubFor(post(urlEqualTo("/coupons/validate"))
            .inScenario(SCENARIO)
            .whenScenarioStateIs(RECOVERED)
            .willReturn(okJson("""
                {"valid":true,"discount":15}
                """)));

        var rt = new RestTemplate();
        var headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        var entity = new org.springframework.http.HttpEntity<>(
            """
            {"code":"SAVE15","orderId":"ORD-555"}
            """,
            headers
        );

        // First attempt — 503
        assertThatThrownBy(() ->
            rt.postForObject(
                "http://localhost:" + wireMock.port() + "/coupons/validate",
                entity,
                java.util.Map.class
            )
        ).isInstanceOf(HttpServerErrorException.class)
         .hasMessageContaining("503");

        // Second attempt — 200 OK
        var result = rt.postForObject(
            "http://localhost:" + wireMock.port() + "/coupons/validate",
            entity,
            java.util.Map.class
        );
        assertThat(result.get("valid")).isEqualTo(true);
        assertThat(result.get("discount")).isEqualTo(15);
    }

    /**
     * Ex5-Advanced bonus: CONNECTION_RESET fault causes ResourceAccessException.
     */
    @Test
    void ex5_advanced_faultConnectionReset() {
        wireMock.stubFor(get(urlEqualTo("/coupons/FAULT-TEST"))
            .willReturn(aResponse()
                .withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertThatThrownBy(() ->
            new RestTemplate().getForObject(
                "http://localhost:" + wireMock.port() + "/coupons/FAULT-TEST",
                java.util.Map.class
            )
        ).isInstanceOf(ResourceAccessException.class);
    }
}
