# บทที่ 8 — Scenarios: Stateful Behaviour

---

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบคำถามนี้จากบทที่ 7:

> **`withTransformers("response-template")` ใช้ทำอะไร?**

ลองตอบในใจก่อน แล้วเลื่อนลงมาดูเฉลย

---

**เฉลย:** `withTransformers("response-template")` บอก WireMock ให้ process **Handlebars template** ใน response body และ header ของ stub นั้น ทำให้ response เป็น dynamic โดยดึงข้อมูลจาก request มาแทรก เช่น `{{request.pathSegments.[1]}}` แทรก URL segment หรือ `{{request.headers.X-Request-Id}}` แทรก header value ถ้าไม่ใส่ `withTransformers()` WireMock จะ return `{{ }}` เป็น literal text

---

## ส่วนที่ 1: วัตถุประสงค์

เมื่ออ่านบทนี้จบ คุณจะสามารถ:

- อธิบายว่า WireMock Scenarios คืออะไรและแก้ปัญหาอะไร
- สร้าง stateful stub ด้วย `inScenario()`, `whenScenarioStateIs()`, `willSetStateTo()`
- เขียน test จำลอง payment retry flow (pending → success)
- อธิบาย `Scenario.STARTED` และลำดับการเปลี่ยน state

---

## ส่วนที่ 2: ทำไมต้องรู้? (Why)

API ในโลกจริงมักมี **state** ที่เปลี่ยนไปตามเวลา:

- เรียก `GET /payments/123` ครั้งแรก → `"status": "pending"` (payment กำลังประมวลผล)
- เรียกซ้ำหลัง 2 วินาที → `"status": "success"` (payment เสร็จแล้ว)

ปัญหา: stub ปกติตอบ response เดิมทุกครั้ง ไม่ว่าจะเรียกกี่ครั้ง ถ้าต้องการ test ว่า Order Service มี retry logic ถูกต้อง — เรียกซ้ำแล้วรอจน success — เราต้องการ stub ที่ **จำ state** ระหว่าง request

**Scenarios คือคำตอบ:** WireMock จัดการ state machine ให้ เราแค่กำหนดว่า "เมื่อ state เป็น X ให้ตอบแบบนี้ แล้วเปลี่ยนไป state Y"

---

## ส่วนที่ 3: เนื้อหาหลัก

### WireMock Scenarios ทำงานอย่างไร

Scenario เป็น **state machine** ที่มีชื่อ (string) แต่ละ scenario เริ่มต้นที่ state `"Started"` (ค่าคงที่ `Scenario.STARTED`) และ transition ไป state อื่นได้ตามที่กำหนด

```
[STARTED] ──── request ครั้งที่ 1 ────► [PENDING]
[PENDING] ──── request ครั้งที่ 2 ────► [SUCCESS]
```

แต่ละ state สามารถ map กับ stub ต่างกัน ทำให้ response เปลี่ยนตาม state

### 3 methods หลัก

**`inScenario(String name)`** — ผูก stub เข้ากับ scenario ที่มีชื่อนั้น stubs ใน scenario เดียวกันแชร์ state ร่วมกัน:

```java
wireMock.stubFor(get(urlEqualTo("/payments/123"))
    .inScenario("payment-retry")    // ← ชื่อ scenario
    ...
```

**`whenScenarioStateIs(String state)`** — กำหนดว่า stub นี้ทำงานเมื่อ state เป็นอะไร:

```java
    .whenScenarioStateIs(STARTED)       // ทำงานตอนเริ่มต้น
    // หรือ
    .whenScenarioStateIs("PENDING")     // ทำงานเมื่อ state = "PENDING"
```

**`willSetStateTo(String newState)`** — หลัง request match stub นี้ ให้เปลี่ยน state ไป:

```java
    .willSetStateTo("PENDING")      // เปลี่ยน state เป็น "PENDING" หลัง request นี้
```

### Imports ที่ต้องใช้

```java
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
```

`STARTED` คือ string constant `"Started"` — state เริ่มต้นของทุก scenario

### Pattern เต็มสำหรับ 2-step scenario

```java
// Step 1: state STARTED → ตอบ pending แล้วเปลี่ยนไป PENDING
wireMock.stubFor(get(urlEqualTo("/payments/123"))
    .inScenario("payment-retry")
    .whenScenarioStateIs(STARTED)
    .willSetStateTo("PENDING")
    .willReturn(aResponse()
        .withStatus(202)
        .withBody("{\"status\":\"pending\"}")));

// Step 2: state PENDING → ตอบ success แล้วเปลี่ยนไป SUCCESS
wireMock.stubFor(get(urlEqualTo("/payments/123"))
    .inScenario("payment-retry")
    .whenScenarioStateIs("PENDING")
    .willSetStateTo("SUCCESS")
    .willReturn(okJson("{\"status\":\"success\"}")));
```

### Reset scenario

ถ้าต้องการ reset scenario กลับไป `STARTED` ระหว่าง test:

```java
wireMock.resetScenarios();      // reset ทุก scenario กลับไป STARTED
```

---

## ส่วนที่ 4: ตัวอย่าง 3 ระดับ

### Beginner — payment pending แล้ว success เมื่อ retry

```java
// tested: WireMock 3.13.2, Java 17
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
```

Output เมื่อรัน `mvn test -Dtest=Ch08ScenariosTest`:
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.804 s
[INFO] BUILD SUCCESS
```

### Intermediate — 3-state scenario: pending → processing → success

สถานการณ์: payment gateway มี 3 ขั้น (pending → processing → settled)

```java
// tested: WireMock 3.13.2, Java 17
@Test
void threeStatePaymentFlow() {
    String PAYMENT_ID = "/payments/three-state-001";

    wireMock.stubFor(get(urlEqualTo(PAYMENT_ID))
        .inScenario("three-state-payment")
        .whenScenarioStateIs(STARTED)
        .willSetStateTo("PROCESSING")
        .willReturn(aResponse().withStatus(202)
            .withHeader("Content-Type","application/json")
            .withBody("{\"id\":\"three-state-001\",\"status\":\"pending\",\"amount\":300,\"currency\":\"THB\"}")));

    wireMock.stubFor(get(urlEqualTo(PAYMENT_ID))
        .inScenario("three-state-payment")
        .whenScenarioStateIs("PROCESSING")
        .willSetStateTo("SETTLED")
        .willReturn(aResponse().withStatus(202)
            .withHeader("Content-Type","application/json")
            .withBody("{\"id\":\"three-state-001\",\"status\":\"processing\",\"amount\":300,\"currency\":\"THB\"}")));

    wireMock.stubFor(get(urlEqualTo(PAYMENT_ID))
        .inScenario("three-state-payment")
        .whenScenarioStateIs("SETTLED")
        .willReturn(okJson("{\"id\":\"three-state-001\",\"status\":\"success\",\"amount\":300,\"currency\":\"THB\"}")));

    PaymentResponse r1 = paymentClient.getPayment("three-state-001");
    assertThat(r1.getStatus()).isEqualTo("pending");

    PaymentResponse r2 = paymentClient.getPayment("three-state-001");
    assertThat(r2.getStatus()).isEqualTo("processing");

    PaymentResponse r3 = paymentClient.getPayment("three-state-001");
    assertThat(r3.getStatus()).isEqualTo("success");

    wireMock.verify(3, getRequestedFor(urlEqualTo(PAYMENT_ID)));
}
```

[ดู animation: Scenario State Machine](animations/04-scenario-state.html)

### Advanced — หลาย scenario อิสระกัน

สถานการณ์: test ที่มี payment หลายรายการพร้อมกัน แต่ละ scenario ต้องไม่รบกวนกัน

```java
// tested: WireMock 3.13.2, Java 17
@Test
void twoIndependentScenarios() {
    // Scenario A: payment-001
    wireMock.stubFor(get(urlEqualTo("/payments/pay-A"))
        .inScenario("scenario-A")
        .whenScenarioStateIs(STARTED)
        .willSetStateTo("DONE_A")
        .willReturn(aResponse().withStatus(202)
            .withHeader("Content-Type","application/json")
            .withBody("{\"id\":\"pay-A\",\"status\":\"pending\",\"amount\":100,\"currency\":\"THB\"}")));

    wireMock.stubFor(get(urlEqualTo("/payments/pay-A"))
        .inScenario("scenario-A")
        .whenScenarioStateIs("DONE_A")
        .willReturn(okJson("{\"id\":\"pay-A\",\"status\":\"success\",\"amount\":100,\"currency\":\"THB\"}")));

    // Scenario B: payment-002 (อิสระจาก A)
    wireMock.stubFor(get(urlEqualTo("/payments/pay-B"))
        .inScenario("scenario-B")
        .whenScenarioStateIs(STARTED)
        .willSetStateTo("DONE_B")
        .willReturn(aResponse().withStatus(202)
            .withHeader("Content-Type","application/json")
            .withBody("{\"id\":\"pay-B\",\"status\":\"pending\",\"amount\":200,\"currency\":\"THB\"}")));

    wireMock.stubFor(get(urlEqualTo("/payments/pay-B"))
        .inScenario("scenario-B")
        .whenScenarioStateIs("DONE_B")
        .willReturn(okJson("{\"id\":\"pay-B\",\"status\":\"success\",\"amount\":200,\"currency\":\"THB\"}")));

    // ทั้งสอง scenario ทำงานอิสระ
    PaymentResponse a1 = paymentClient.getPayment("pay-A");
    PaymentResponse b1 = paymentClient.getPayment("pay-B");
    assertThat(a1.getStatus()).isEqualTo("pending");  // A ยัง pending
    assertThat(b1.getStatus()).isEqualTo("pending");  // B ยัง pending

    PaymentResponse a2 = paymentClient.getPayment("pay-A");
    PaymentResponse b2 = paymentClient.getPayment("pay-B");
    assertThat(a2.getStatus()).isEqualTo("success");  // A เสร็จแล้ว
    assertThat(b2.getStatus()).isEqualTo("success");  // B เสร็จแล้ว
}
```

---

## ส่วนที่ 5: Common Mistakes

**❌ ใช้ชื่อ scenario ต่างกันในแต่ละ stub แล้วสงสัยว่าทำไม state ไม่เปลี่ยน**

```java
// ❌ ผิด — ชื่อ scenario ต่างกัน = คนละ state machine
wireMock.stubFor(get(urlEqualTo("/payments/123"))
    .inScenario("payment-retry")      // scenario A
    .whenScenarioStateIs(STARTED)
    ...

wireMock.stubFor(get(urlEqualTo("/payments/123"))
    .inScenario("payment-retry-flow") // scenario B (คนละตัว!)
    .whenScenarioStateIs("PENDING")
    ...
```

```java
// ✅ ถูก — ใช้ชื่อ scenario เดียวกันทุก stub ที่ต้องการแชร์ state
wireMock.stubFor(get(urlEqualTo("/payments/123"))
    .inScenario("payment-retry")
    ...
wireMock.stubFor(get(urlEqualTo("/payments/123"))
    .inScenario("payment-retry")    // ชื่อเดียวกัน
    ...
```

เหตุผล: แต่ละ scenario name คือ state machine แยกกัน ถ้าชื่อต่างกันจะ track state แยกกัน *(source: https://wiremock.org/docs/stateful-behaviour/)*

---

**❌ ลืม `willSetStateTo()` ทำให้ state ไม่เปลี่ยน**

```java
// ❌ ผิด — ไม่มี willSetStateTo → state ค้างอยู่ที่ STARTED ตลอด
wireMock.stubFor(get(urlEqualTo("/payments/123"))
    .inScenario("payment-retry")
    .whenScenarioStateIs(STARTED)
    // ลืม .willSetStateTo("PENDING")
    .willReturn(aResponse().withStatus(202).withBody("{\"status\":\"pending\"}")));
```

```java
// ✅ ถูก — ต้องระบุ willSetStateTo เพื่อ transition state
wireMock.stubFor(get(urlEqualTo("/payments/123"))
    .inScenario("payment-retry")
    .whenScenarioStateIs(STARTED)
    .willSetStateTo("PENDING")
    .willReturn(aResponse().withStatus(202).withBody("{\"status\":\"pending\"}")));
```

เหตุผล: ถ้าไม่มี `willSetStateTo()` state จะไม่เปลี่ยนหลัง request ทำให้ทุก request เจอ stub เดิมตลอด *(source: https://wiremock.org/docs/stateful-behaviour/)*

---

**❌ คาดว่า scenario state ถูก reset อัตโนมัติระหว่าง test**

```java
// ❌ ผิด — ถ้าใช้ WireMockServer เดียวกันหลาย test
// test แรก: ทำให้ state เป็น "SUCCESS"
// test ที่สอง: state ยังเป็น "SUCCESS" ไม่ได้กลับไป STARTED
@Test
void testOne() { /* runs scenario to SUCCESS */ }

@Test
void testTwo() {
    // ❌ state ยังเป็น SUCCESS จากเมื่อกี้ ไม่ใช่ STARTED
    PaymentResponse r = paymentClient.getPayment("scenario-123");
}
```

```java
// ✅ ถูก — reset scenarios ใน setUp หรือใช้ WireMockServer ใหม่ต่อ test
@BeforeEach void setUp() {
    wireMock = new WireMockServer(wireMockConfig().dynamicPort());
    wireMock.start();
    // WireMockServer ใหม่ = scenarios เริ่มต้นใหม่ทุกครั้ง
}
```

เหตุผล: scenario state คงอยู่ตลอดอายุ WireMockServer ถ้าใช้ server เดิมหลาย test ต้อง call `wireMock.resetScenarios()` ใน `@BeforeEach` หรือสร้าง server ใหม่ต่อ test *(source: https://wiremock.org/docs/stateful-behaviour/)*

---

## ส่วนที่ 6: สรุปบท

WireMock Scenarios เป็น state machine ที่ช่วยให้ stub ตอบ response ต่างกันตามจำนวนครั้งที่เรียก เหมาะสำหรับ test retry logic, polling, หรือ multi-step flow สามคำสั่งหลักคือ `inScenario()` ตั้งชื่อ scenario, `whenScenarioStateIs()` กำหนดเงื่อนไข, `willSetStateTo()` เปลี่ยน state แต่ละ scenario ชื่อต่างกัน = state machine คนละตัว state เริ่มต้นคือ `Scenario.STARTED` เสมอ

**คำถาม Retrieval — ลองตอบก่อนดูเฉลย:**

1. ถ้า stub มี `whenScenarioStateIs(STARTED)` แต่ไม่มี `willSetStateTo()` จะเกิดอะไรเมื่อเรียกครั้งที่ 2?
2. `Scenario.STARTED` คือ string อะไร และ import มาจากที่ไหน?
3. ถ้าต้องการ test retry logic ที่ลอง 3 ครั้งกว่าจะ success ต้องสร้าง stub กี่ตัวและต้องการ state กี่ state?

---

**เฉลย:**

1. state ไม่เปลี่ยน ค้างอยู่ที่ `STARTED` ตลอด ดังนั้น request ครั้งที่ 2 จะยังเจอ stub เดิม (state = STARTED) ได้ response เดิมซ้ำ
2. `Scenario.STARTED` คือ string `"Started"` (S ตัวใหญ่) import จาก `import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;`
3. ต้องการ 3 stubs และ 3 states: `STARTED→RETRY_1→RETRY_2→SUCCESS` — stub 1 (state STARTED → RETRY_1, ตอบ fail), stub 2 (state RETRY_1 → RETRY_2, ตอบ fail), stub 3 (state RETRY_2 → SUCCESS, ตอบ success)
