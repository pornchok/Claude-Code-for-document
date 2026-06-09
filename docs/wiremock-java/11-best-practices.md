# บทที่ 11 — Best Practices & Common Mistakes

## ⏸ Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบคำถามนี้ก่อน:

> **`withFixedDelay()` กับ `Fault.CONNECTION_RESET_BY_PEER` ต่างกันอย่างไร?**
> ในสถานการณ์ไหนที่ระบบจะเจอแต่ละแบบในชีวิตจริง?

ลองตอบด้วยตัวเองก่อน แล้วค่อย scroll ดูเฉลย

---

**เฉลย:** `withFixedDelay()` จำลอง API ที่ช้า — response ยังมาแต่ใช้เวลานาน (เช่น database query หนัก, third-party API ช้า) ส่วน `CONNECTION_RESET_BY_PEER` จำลองการตัด TCP connection กลางคัน — ไม่มี response เลย เกิดจาก network issue, server crash, หรือ firewall ตัด session

---

## 1. วัตถุประสงค์

อ่านจบแล้วทำได้:

- ระบุ anti-pattern ที่พบบ่อยที่สุดใน WireMock test และแก้ให้ถูกต้อง
- ใช้ production checklist ก่อน merge WireMock test เข้า codebase
- วิเคราะห์ code snippet ที่มีปัญหาและระบุสาเหตุ

---

## 2. ทำไมต้องรู้?

WireMock ใช้งานง่าย แต่ "ใช้ได้" กับ "ใช้ถูก" ต่างกัน

Anti-patterns ที่พบบ่อยมักไม่ทำให้ test fail ทันที แต่จะสร้างปัญหาในภายหลัง เช่น:
- Test ผ่านใน local แต่ fail ใน CI เพราะ port conflict
- Test เขียว (green) แต่ silent pass — ไม่ได้ verify ว่า API ถูกเรียกจริง
- Stub เก่า leak ข้ามไป test อื่น ทำให้ test order-dependent

บทนี้รวบรวม gotchas จากทั้งคอร์สมาไว้ในที่เดียว พร้อม production checklist

---

## 3. เนื้อหาหลัก — 8 Anti-Patterns ที่พบบ่อย

### Anti-Pattern 1: Hardcode Port 8080

```java
// ❌ แบบผิด
new WireMockServer(8080)

// ✅ แบบถูก
new WireMockServer(wireMockConfig().dynamicPort())
```

**เหตุผล:** Port 8080 อาจถูกใช้งานอยู่แล้วใน local dev หรือ CI — `dynamicPort()` ให้ OS จัดสรร port ว่างให้อัตโนมัติ ไม่มี conflict *(ดูตัวอย่างจริงใน Ch02SetupTest.java)*

---

### Anti-Pattern 2: ลืม stop() WireMockServer

```java
// ❌ แบบผิด — WireMockServer ยังทำงานอยู่หลัง test จบ
@BeforeEach void setUp() {
    wireMock = new WireMockServer(wireMockConfig().dynamicPort());
    wireMock.start();
}
// ลืม @AfterEach!

// ✅ แบบถูก 1 — stop ใน @AfterEach
@AfterEach void tearDown() {
    wireMock.stop();
}

// ✅ แบบถูก 2 — ใช้ WireMockExtension (auto-stop)
@RegisterExtension
static WireMockExtension wireMock = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort())
    .build();
```

**เหตุผล:** Server ที่ไม่ถูก stop จะ hold port ไว้ และสะสม memory leak — ยิ่งมีหลาย test class ปัญหายิ่งชัด *(ดูตัวอย่างใน Ch02-Ch08 ทุกไฟล์มี @AfterEach tearDown)*

---

### Anti-Pattern 3: stubFor โดยไม่มี verify

```java
// ❌ แบบผิด — stub อยู่ แต่ไม่รู้ว่าถูกเรียกจริงหรือเปล่า
wireMock.stubFor(post(urlEqualTo("/payments"))
    .willReturn(okJson("{\"id\":\"123\",\"status\":\"ok\"}")));

PaymentResponse response = paymentClient.createPayment(request);
assertThat(response.getId()).isEqualTo("123");
// ถ้า paymentClient มี bug แล้วเรียก URL ผิด test ก็จะ fail จาก assertThat
// แต่ถ้า stub ถูก match โดยบังเอิญ เราไม่รู้ว่า code path จริงถูกใช้หรือเปล่า

// ✅ แบบถูก — ตรวจสอบว่าถูกเรียกจริง
wireMock.verify(1, postRequestedFor(urlEqualTo("/payments")));
```

**เหตุผล:** `verify` ยืนยันว่า code ส่ง request ไปยัง endpoint ที่ถูกต้อง ด้วย method ที่ถูกต้อง — ป้องกัน "silent pass" *(ดูตัวอย่างใน Ch04VerifyTest.java)*

---

### Anti-Pattern 4: urlEqualTo กับ Query String

```java
// ❌ แบบผิด — brittle เมื่อ query param order เปลี่ยน
wireMock.stubFor(get(urlEqualTo("/payments?currency=THB&status=pending"))
    .willReturn(...));
// ถ้า client ส่ง /payments?status=pending&currency=THB → ไม่ match!

// ✅ แบบถูก — แยก path กับ query params
wireMock.stubFor(get(urlPathEqualTo("/payments"))
    .withQueryParam("currency", equalTo("THB"))
    .withQueryParam("status", equalTo("pending"))
    .willReturn(...));
```

**เหตุผล:** `urlEqualTo` match string ตรงๆ รวม query string — ถ้า param order เปลี่ยนจะไม่ match *(source: wiremock.org/docs/request-matching/ — "urlPathEqualTo: Matches path only; ignores query parameters")*

---

### Anti-Pattern 5: ไม่ reset stubs ระหว่าง tests

```java
// ❌ แบบผิด — WireMockServer แบบ shared ไม่ reset อัตโนมัติ
class LeakyTest {
    static WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());

    @BeforeAll static void startServer() { wireMock.start(); }

    @Test void test1() {
        wireMock.stubFor(get("/payments/123").willReturn(okJson("{\"status\":\"ok\"}")));
        // ... test logic
        // stub ยังอยู่!
    }

    @Test void test2() {
        // stub จาก test1 ยังอยู่ — ทำให้ test2 อาจ pass โดยบังเอิญ
    }
}

// ✅ แบบถูก 1 — reset ใน @AfterEach
@AfterEach void resetStubs() {
    wireMock.resetAll();
}

// ✅ แบบถูก 2 — ใช้ WireMockExtension (reset อัตโนมัติก่อนแต่ละ test)
@RegisterExtension
static WireMockExtension wireMock = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort())
    .build();
```

**เหตุผล:** Stubs ที่ leak ข้ามไปยัง test อื่นทำให้ test order-dependent — ผ่านเมื่อรันตามลำดับ แต่ fail เมื่อรัน test เดี่ยว *(ดูตัวอย่างใน Ch09SpringBootIntegrationTest.java — test 3 ยืนยัน reset)*

---

### Anti-Pattern 6: ใช้ artifact เก่า (2.x)

```xml
<!-- ❌ แบบผิด — artifact เก่า conflict กับ Spring Boot 3.x -->
<dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock-jre8</artifactId>
    <version>2.35.0</version>
</dependency>

<!-- ✅ แบบถูก — artifact ใหม่ org.wiremock -->
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <version>3.13.2</version>
    <scope>test</scope>
</dependency>
```

**เหตุผล:** WireMock 2.x ใช้ Jetty 9 ซึ่ง conflict กับ Spring Boot 3.x ที่ใช้ Jetty 11+ ส่วน `wiremock-standalone` บรรจุ Jetty แบบ shaded (ซ่อนอยู่ใน jar) จึงไม่ conflict *(source: wiremock.org/docs/download-and-installation/)*

---

### Anti-Pattern 7: equalToJson() โดยไม่ใช้ ignoreExtraElements

```java
// ❌ แบบผิด — test จะ fail ถ้า client ส่ง field เพิ่มมา
wireMock.stubFor(post(urlEqualTo("/payments"))
    .withRequestBody(equalToJson("{\"amount\":5000,\"currency\":\"THB\"}")));
// ถ้า client ส่ง {"amount":5000,"currency":"THB","description":"test"} → ไม่ match!

// ✅ แบบถูก — อนุญาต extra fields
wireMock.stubFor(post(urlEqualTo("/payments"))
    .withRequestBody(equalToJson(
        "{\"amount\":5000,\"currency\":\"THB\"}",
        true,   // ignoreArrayOrder
        true    // ignoreExtraElements
    )));
```

**เหตุผล:** ใน real world request body มักมี field เพิ่มที่เราไม่ได้สนใจ — `ignoreExtraElements=true` ทำให้ matching ยืดหยุ่นขึ้นและ test ไม่ brittle *(source: wiremock.org/docs/request-matching/ — "Permits additional object properties in actual requests beyond expected fields")*

---

### Anti-Pattern 8: Spring context ไม่ชี้ไปที่ WireMock

```java
// ❌ แบบผิด — Spring context ยังใช้ application.properties (payment.service.url=http://localhost:8080)
@SpringBootTest
class WrongIntegrationTest {
    @Autowired PaymentClient paymentClient;

    @Test void test() {
        wireMock.stubFor(get("/payments/123").willReturn(...));
        // paymentClient จะส่ง request ไปที่ localhost:8080 (port จริง) ไม่ใช่ WireMock!
    }
}

// ✅ แบบถูก — @DynamicPropertySource override URL ให้ชี้ไปที่ WireMock
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CorrectIntegrationTest {
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.service.url", wireMock::baseUrl);
    }
    // ...
}
```

**เหตุผล:** ถ้าไม่ override URL ด้วย `@DynamicPropertySource` Spring จะใช้ค่าจาก `application.properties` ซึ่งชี้ไปที่ URL จริง — test จะพยายาม connect ไปที่ API จริง *(ดูตัวอย่างที่ถูกใน Ch09SpringBootIntegrationTest.java)*

---

## 4. ตัวอย่าง 3 ระดับ

### Beginner — Checklist ก่อน commit

ก่อน push WireMock test เข้า codebase ตรวจสอบทุกข้อ:

```java
// Pattern ที่ถูกต้องสมบูรณ์ — ใช้เป็น template ได้
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CompletePatternTest {

    // ✅ 1. dynamicPort() — ไม่ hardcode
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    // ✅ 2. @DynamicPropertySource — Spring ชี้ไปที่ WireMock
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.service.url", wireMock::baseUrl);
    }

    @Autowired
    private PaymentClient paymentClient;

    @Test
    void completePatternExample() {
        // ✅ 3. stubFor — กำหนด response
        wireMock.stubFor(get(urlEqualTo("/payments/test-id"))
            .willReturn(okJson("{\"id\":\"test-id\",\"status\":\"successful\",\"amount\":100,\"currency\":\"THB\"}")));

        // Act
        PaymentResponse response = paymentClient.getPayment("test-id");

        // ✅ 4. Assert response
        assertThat(response.getStatus()).isEqualTo("successful");

        // ✅ 5. verify — ยืนยันว่า request ถูกส่งจริง
        wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/test-id")));
    }
}
```

### Intermediate — ทดสอบทุก error path

สถานการณ์: Payment service ต้องรองรับทั้ง happy path และ error paths

```java
// ✅ test ครบทุก path ที่เป็นไปได้
class PaymentServiceCompleteTest {

    @Test void shouldReturnPayment_whenSuccess() { /* 200 */ }

    @Test void shouldThrow404_whenPaymentNotFound() { /* 404 */ }

    @Test void shouldThrow500_whenServerError() { /* 500 */ }

    @Test void shouldHandleTimeout_whenApiSlow() { /* withFixedDelay */ }

    @Test void shouldHandleConnectionReset_whenNetworkFails() { /* withFault */ }
}
```

เหตุผลที่ต้อง test ทุก path: แต่ละกรณีอาจมี business logic แตกต่างกัน เช่น 404 = "ไม่พบ order", 500 = "retry ภายหลัง", timeout = "แสดง spinner"

### Advanced — Stub Reuse Pattern

สถานการณ์: project ใหญ่ที่มี test หลาย class ที่ใช้ stub เดิมซ้ำ

```java
// Helper class สำหรับ common stubs — ลดการ duplicate
public class PaymentStubs {

    public static void stubSuccessfulPayment(WireMockExtension wireMock, String paymentId, int amount) {
        wireMock.stubFor(get(urlEqualTo("/payments/" + paymentId))
            .willReturn(okJson(String.format(
                "{\"id\":\"%s\",\"status\":\"successful\",\"amount\":%d,\"currency\":\"THB\"}",
                paymentId, amount))));
    }

    public static void stubPaymentNotFound(WireMockExtension wireMock, String paymentId) {
        wireMock.stubFor(get(urlEqualTo("/payments/" + paymentId))
            .willReturn(aResponse()
                .withStatus(404)
                .withBody("{\"error\":\"Payment not found\"}")));
    }
}

// การใช้งาน
@Test
void orderService_shouldHandlePaymentNotFound() {
    PaymentStubs.stubPaymentNotFound(wireMock, "missing-id");
    assertThatThrownBy(() -> paymentClient.getPayment("missing-id"))
        .isInstanceOf(HttpClientErrorException.class);
}
```

---

## 5. Exercises — Find the Bug

แต่ละ snippet ด้านล่างมีปัญหา ให้ระบุว่าผิดอะไรและแก้ยังไง

### Snippet A

```java
@SpringBootTest
class PaymentTest {

    @RegisterExtension
    WireMockExtension wireMock = WireMockExtension.newInstance()  // <-- ?
        .options(wireMockConfig().dynamicPort())
        .build();

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("payment.service.url", wireMock::baseUrl);  // <-- ?
    }
}
```

<details>
<summary>เฉลย Snippet A</summary>

**ปัญหา 2 จุด:**
1. `WireMockExtension` ต้องเป็น `static` field — `@DynamicPropertySource` อยู่ใน static context ไม่สามารถเข้าถึง instance field ได้ → compile error
2. Method `configureProperties` ต้องอ้างถึง static field เท่านั้น

**แก้:**
```java
@RegisterExtension
static WireMockExtension wireMock = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort())
    .build();
```

</details>

---

### Snippet B

```java
@Test
void createOrder_shouldChargeCorrectAmount() {
    wireMock.stubFor(post(urlEqualTo("/payments"))
        .withRequestBody(equalToJson("{\"amount\":9900,\"currency\":\"THB\"}"))
        .willReturn(okJson("{\"id\":\"pay-001\",\"status\":\"ok\"}")));

    // ... เรียก service ที่ส่ง {"amount":9900,"currency":"THB","description":"Order #1"}

    PaymentResponse response = service.processOrder(order);
    assertThat(response.getId()).isEqualTo("pay-001");
    // test fail! ทั้งที่ logic ถูก
}
```

<details>
<summary>เฉลย Snippet B</summary>

**ปัญหา:** `equalToJson()` โดยไม่ใช้ `ignoreExtraElements=true` — เมื่อ client ส่ง field `"description"` เพิ่มมา stub จะ `ไม่ match` เพราะ JSON ไม่ตรงกันทุก field

**แก้:**
```java
.withRequestBody(equalToJson(
    "{\"amount\":9900,\"currency\":\"THB\"}",
    true,   // ignoreArrayOrder
    true    // ignoreExtraElements — อนุญาต field เพิ่มเติม
))
```

</details>

---

### Snippet C

```java
class PaymentServiceTest {
    static WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());

    @BeforeAll static void start() { wireMock.start(); }
    @AfterAll static void stop() { wireMock.stop(); }

    @Test void test1() {
        wireMock.stubFor(get("/payments/aaa").willReturn(okJson("{\"id\":\"aaa\"}")));
        assertThat(paymentClient.getPayment("aaa").getId()).isEqualTo("aaa");
    }

    @Test void test2() {
        // ไม่มี stub สำหรับ /payments/aaa แต่บางครั้งก็ผ่าน
        PaymentResponse r = paymentClient.getPayment("aaa");
        assertThat(r).isNull();  // บางครั้งผ่าน บางครั้งไม่ผ่าน
    }
}
```

<details>
<summary>เฉลย Snippet C</summary>

**ปัญหา:** ไม่มี `wireMock.resetAll()` ใน `@AfterEach` — stub จาก `test1` ยังอยู่เมื่อ `test2` รัน ทำให้ `/payments/aaa` ยังมี stub และคืน response แทนที่จะเป็น null

**แก้:**
```java
@AfterEach void resetStubs() {
    wireMock.resetAll();
}
// หรือเปลี่ยนไปใช้ WireMockExtension ซึ่ง reset อัตโนมัติ
```

</details>

---

## 6. Production Checklist

ก่อน merge WireMock tests เข้า main branch ตรวจสอบทุกข้อ:

- [ ] ใช้ `dynamicPort()` — ไม่ hardcode port ใดๆ
- [ ] `stubFor` มี `verify` คู่กันทุกครั้ง
- [ ] Stubs ถูก reset ระหว่าง tests (`@AfterEach resetAll()` หรือ `WireMockExtension`)
- [ ] test ครอบคลุม error paths: 404, 500, timeout, connection fault
- [ ] Spring Boot tests ใช้ `@DynamicPropertySource` — ไม่ใช้ URL hardcode
- [ ] artifact เป็น `org.wiremock:wiremock-standalone` — ไม่ใช่ `com.github.tomakehurst:wiremock-jre8`
- [ ] `equalToJson()` ใช้ `ignoreExtraElements=true` เมื่อ match JSON body
- [ ] `urlPathEqualTo` + `withQueryParam()` แทน `urlEqualTo` เมื่อ URL มี query params

---

## 7. สรุปบท

คอร์สนี้ครอบคลุม WireMock ตั้งแต่พื้นฐานจนถึง production-grade patterns:

| บทที่ | สิ่งที่เรียน |
|---|---|
| 2-3 | Setup + Basic stubbing (GET) |
| 4 | Verify — ยืนยัน request ถูกส่งจริง |
| 5-6 | Request matching — POST body, headers, query params |
| 7 | Response templating — dynamic responses |
| 8 | Scenarios — stateful behavior |
| 9 | Spring Boot integration — WireMockExtension + @DynamicPropertySource |
| 10 | Error simulation — 404, 500, delay, fault |
| 11 | Best practices — anti-patterns + production checklist |

**กฎทอง 3 ข้อที่ควรจำ:**

1. **`dynamicPort()` เสมอ** — ป้องกัน port conflict ใน CI
2. **`stubFor` + `verify` คู่กัน** — ป้องกัน silent pass
3. **`WireMockExtension` กับ Spring Boot** — reset อัตโนมัติ + ไม่ต้องจัดการ lifecycle เอง

**คำถาม Retrieval — ตอบก่อนดูเฉลย:**

1. ในบรรดา 8 anti-patterns ที่เรียนมา anti-pattern ไหนอันตรายที่สุดสำหรับ team ใหญ่ที่มีหลาย test class? เพราะอะไร?
2. ถ้า test ผ่านใน local แต่ fail ใน CI ด้วย `Connection refused port 8080` สาเหตุน่าจะเป็นอะไร?
3. เมื่อไหรควรใช้ `WireMockServer` (manual) แทน `WireMockExtension`?

---

**เฉลย:**

1. Anti-pattern ข้อ 5 (ไม่ reset stubs) อันตรายที่สุดสำหรับ team ใหญ่ — test order-dependent ทำให้ test ผ่านเมื่อรันทั้ง suite แต่ fail เมื่อรันเดี่ยว ยากต่อการ debug
2. ลืมใช้ `dynamicPort()` หรือลืม `@DynamicPropertySource` — Spring context ชี้ไปที่ localhost:8080 แทน WireMock port จริง
3. ใช้ `WireMockServer` เมื่อต้องการ control lifecycle เองอย่างละเอียด เช่น ต้องการ start/stop ระหว่าง test หรือใช้ใน non-JUnit context เช่น Testcontainers, custom test framework
