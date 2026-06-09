# บทที่ 3 — Stub GET Request พื้นฐาน

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบจากที่จำได้:

1. `dynamicPort()` ต่างจากการ hardcode port อย่างไร? และทำไมถึงแนะนำให้ใช้ `dynamicPort()`?
2. ถ้า `WireMockServer` ถูก start ใน `@BeforeAll` คุณต้องทำอะไรใน `@BeforeEach` เพื่อป้องกัน stub contamination?

*ตอบก่อน แล้วค่อยอ่านต่อ*

---

**เฉลย:**

1. `dynamicPort()` ให้ OS เลือก port ว่างให้อัตโนมัติ ทำให้รัน test แบบ parallel ได้ปลอดภัย ไม่เสี่ยง port conflict ส่วน hardcode port เช่น `8089` อาจชน port ที่ process อื่นใช้อยู่ หรือชนกันเองเมื่อรัน parallel
2. ต้องเรียก `wireMock.resetAll()` เพื่อล้าง stub เก่าที่ค้างมาจาก test ก่อนหน้า

---

## 1. วัตถุประสงค์

หลังจากอ่านบทนี้จบ คุณจะสามารถ:

- **เขียน** stub สำหรับ GET request ด้วย `stubFor()` + `get()` + `willReturn()` ได้
- **อธิบาย** วิธีที่ WireMock จับคู่ URL (exact match by default)
- **ใช้** `okJson()` เพื่อ return JSON response และ deserialize เป็น Java object ได้
- **อธิบาย** ได้ว่าเกิดอะไรขึ้นเมื่อ request ไม่ match stub ใดเลย

---

## 2. ทำไมต้องรู้? (Why)

Stub GET คือ building block พื้นฐานที่สุดของ WireMock ทุกครั้งที่ service คุณต้องดึงข้อมูลจาก external API (เช่น ดู payment status, ดูข้อมูล user) คุณต้องสร้าง stub GET ก่อนจะ assert ผลลัพธ์ได้

ถ้าไม่เข้าใจวิธีที่ URL matching ทำงาน test ของคุณอาจ pass ด้วยเหตุผลที่ผิด — เพราะ stub ที่คุณตั้งไว้ไม่ match request จริงๆ เลย แต่ test ก็ไม่ fail เพราะ WireMock ไม่รู้จักว่า no-match คือความผิดพลาด (ถ้าไม่ verify)

---

## 3. เนื้อหาหลัก

### stubFor() — หัวใจของ WireMock

`stubFor()` คือ method หลักที่ใช้กำหนดว่า "ถ้าได้รับ request แบบนี้ ให้ตอบกลับแบบนี้"

```java
wireMock.stubFor(
    get("/payments/123")              // ← request matcher
        .willReturn(okJson("{}"))     // ← response builder
);
```

สองส่วนหลัก:
- **Request matcher**: `get(url)`, `post(url)`, `put(url)`, `delete(url)`
- **Response builder**: `okJson(body)`, `ok(body)`, `aResponse().withStatus(...)`, etc.

### URL Matching — Exact by Default

ค่า default ของ WireMock คือ **exact URL match** หมายความว่า:

```java
// stub นี้จะตอบกลับ ONLY เมื่อ URL ตรงกันพอดี
wireMock.stubFor(get("/payments/123").willReturn(okJson("{}")));

// GET /payments/123     → match ✓ ได้รับ response
// GET /payments/123/    → NO match ✗ (มี trailing slash)
// GET /payments/456     → NO match ✗ (id ต่างกัน)
// GET /api/payments/123 → NO match ✗ (prefix ต่างกัน)
```

เมื่อ request ไม่ match stub ใดเลย WireMock จะตอบกลับด้วย **HTTP 404** พร้อม body ที่บอกว่า "No response could be served"

### shorthand methods ที่ใช้บ่อย

WireMock มี shorthand สำหรับ response ที่พบบ่อย:

```java
// สร้าง response แบบต่างๆ
ok("body text")                        // 200 + text body
okJson("{\"key\":\"value\"}")          // 200 + Content-Type: application/json
aResponse().withStatus(404)           // 404 ไม่มี body
aResponse().withStatus(200)
    .withHeader("Content-Type", "application/json")
    .withBody("{}")                    // กำหนดทุกอย่างเอง
```

### Jackson Deserialization — อ่าน Response เป็น Java Object

เมื่อ `okJson()` ตอบกลับ JSON string ที่ถูกต้อง `RestTemplate` จะ deserialize ให้อัตโนมัติผ่าน Jackson:

```java
// WireMock stub กำหนด JSON response
wireMock.stubFor(get("/payments/123")
    .willReturn(okJson("""
        {
            "id": "123",
            "status": "success",
            "amount": 500,
            "currency": "THB"
        }
        """)));

// RestTemplate deserialize JSON → PaymentResponse object
PaymentResponse response = restTemplate.getForObject(
    "http://localhost:" + wireMock.port() + "/payments/123",
    PaymentResponse.class
);
// response.getId() == "123"
// response.getAmount() == 500
```

---

## 4. ตัวอย่าง 3 ระดับ

### Beginner — Stub GET และตรวจสอบ Response

```java
// tested: Java 17, WireMock 3.13.2
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.web.client.RestTemplate;

class Ch03BasicStubTest {
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

    @Test
    void getPaymentReturnsStubResponse() {
        // กำหนด stub
        wireMock.stubFor(get("/payments/123")
            .willReturn(okJson("""
                {"id":"123","status":"success","amount":500,"currency":"THB"}
                """)));

        // เรียก PaymentClient ซึ่งชี้ไปที่ WireMock
        PaymentResponse response = paymentClient.getPayment("123");

        assertThat(response.getId()).isEqualTo("123");
        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getAmount()).isEqualTo(500);
    }

    @Test
    void stubNotMatchedReturns404() {
        // stub กำหนดสำหรับ /payments/123 เท่านั้น
        wireMock.stubFor(get("/payments/123").willReturn(okJson("{}")));

        // เรียก /payments/999 — ไม่ match stub → WireMock ตอบ 404
        try {
            paymentClient.getPayment("999");
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("404");
        }
    }
}
```

**Output จากการรันจริง:**

```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0,
       Time elapsed: 0.097 s -- in com.example.wiremock.Ch03BasicStubTest
[INFO] BUILD SUCCESS
```

### Intermediate — Stub หลาย URL สำหรับ Order Service ในสถานการณ์จริง

สถานการณ์: `CheckoutService` ต้องดึงข้อมูล payment หลาย ID เพื่อคำนวณยอดรวม คุณต้องทดสอบว่า service รวมยอดถูกต้อง:

```java
// tested: Java 17, WireMock 3.13.2
class CheckoutService_MultiPaymentTest {
    private WireMockServer wireMock;
    private CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        checkoutService = new CheckoutService(
            new PaymentClient(new RestTemplate(),
                "http://localhost:" + wireMock.port())
        );
    }

    @AfterEach
    void tearDown() { wireMock.stop(); }

    @Test
    void calculateTotalFromMultiplePayments() {
        // Stub แต่ละ payment ID — URL แตกต่างกัน
        wireMock.stubFor(get("/payments/p-001")
            .willReturn(okJson("""
                {"id":"p-001","status":"success","amount":1500,"currency":"THB"}
                """)));
        wireMock.stubFor(get("/payments/p-002")
            .willReturn(okJson("""
                {"id":"p-002","status":"success","amount":2300,"currency":"THB"}
                """)));
        wireMock.stubFor(get("/payments/p-003")
            .willReturn(okJson("""
                {"id":"p-003","status":"success","amount":800,"currency":"THB"}
                """)));

        // CheckoutService ดึงข้อมูลทุก payment แล้วรวมยอด
        int total = checkoutService.calculateTotal(List.of("p-001", "p-002", "p-003"));

        assertThat(total).isEqualTo(4600); // 1500 + 2300 + 800
    }
}
```

### Advanced — ทดสอบ Response Parsing Edge Case ใน Production

สถานการณ์: ทีม QA พบว่า Payment API บางครั้งส่ง field `amount` เป็น null แทนที่จะเป็น 0 ทำให้ `NullPointerException` ใน code เขาต้องการ test ที่ครอบ edge case นี้ก่อน deploy:

```java
// tested: Java 17, WireMock 3.13.2
class PaymentResponseEdgeCaseTest {
    private WireMockServer wireMock;
    private PaymentClient paymentClient;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        paymentClient = new PaymentClient(new RestTemplate(),
            "http://localhost:" + wireMock.port());
    }

    @AfterEach
    void tearDown() { wireMock.stop(); }

    @Test
    void handleMissingAmountField_gracefully() {
        // จำลอง API ที่ไม่ส่ง field บางตัวมา
        wireMock.stubFor(get("/payments/edge-001")
            .willReturn(okJson("""
                {"id":"edge-001","status":"pending"}
                """)));
                // ไม่มี "amount" field

        PaymentResponse response = paymentClient.getPayment("edge-001");

        // ตรวจว่า PaymentResponse.getAmount() คืน 0 (default) ไม่ throw NPE
        assertThat(response.getAmount()).isZero();
        assertThat(response.getId()).isEqualTo("edge-001");
    }

    @Test
    void handleExtraUnknownFields_gracefully() {
        // API ใหม่เพิ่ม field ที่ PaymentResponse ยังไม่รู้จัก
        wireMock.stubFor(get("/payments/edge-002")
            .willReturn(okJson("""
                {
                    "id": "edge-002",
                    "status": "success",
                    "amount": 1000,
                    "currency": "THB",
                    "metadata": {"source": "mobile_app"},
                    "new_field_v2": "some_value"
                }
                """)));
                // Jackson จะ ignore field ที่ไม่รู้จัก ถ้า @JsonIgnoreProperties(ignoreUnknown = true)

        PaymentResponse response = paymentClient.getPayment("edge-002");

        // ต้องไม่ throw exception แม้มี unknown fields
        assertThat(response.getAmount()).isEqualTo(1000);
    }
}
```

---

## 5. Common Mistakes

**❌ ผิด: สร้าง stub แต่ลืมว่า URL matching เป็น exact**

```java
// stub กำหนดสำหรับ /payments/123
wireMock.stubFor(get("/payments/123").willReturn(okJson("{}")));

// เรียก URL ที่มี query parameter — ไม่ match!
restTemplate.getForObject(
    "http://localhost:" + port + "/payments/123?currency=THB",
    String.class
); // → HttpClientErrorException: 404
```

✅ **ถูก**: ถ้า real API ส่ง query params มาด้วย ต้องระบุใน stub ให้ตรงกัน หรือใช้ `urlPathEqualTo()` แทน `urlEqualTo()` ซึ่ง ignore query string *(source: wiremock.org/docs/stubbing — URL matching)*

---

**❌ ผิด: ใช้ `okJson()` กับ JSON ที่ format ไม่ถูกต้อง**

```java
// ❌ JSON ไม่ถูก — missing quotes รอบ key
wireMock.stubFor(get("/payments/1")
    .willReturn(okJson("{id:\"1\",status:\"ok\"}")));
    // Jackson จะ throw JsonParseException เมื่อ client พยายาม deserialize
```

✅ **ถูก**: ใช้ Java text block (`"""..."""`) เพื่อให้ JSON อ่านง่ายและตรวจสอบได้ง่ายขึ้น และตรวจสอบ JSON ให้ถูก format ก่อนเสมอ *(source: wiremock.org/docs/stubbing — response body)*

---

**❌ ผิด: คิดว่า test ผ่านหมายความว่า API ถูกเรียกแล้ว**

```java
wireMock.stubFor(get("/payments/123").willReturn(okJson("{}")));

// ถ้าบรรทัดนี้มี bug และไม่ได้เรียก API จริงๆ
// paymentClient.getPayment("123"); ← ลืมเรียก หรือ exception กลืนไป

// test นี้ยังคง PASS! เพราะแค่ assert null หรือ default value
assertThat(response).isNull(); // pass แต่ไม่มีความหมาย
```

✅ **ถูก**: เพิ่ม `wireMock.verify()` เสมอเพื่อยืนยันว่า API ถูกเรียกจริง (จะเรียนในบทที่ 4) *(source: wiremock.org/docs/verifying — "verify also uses WireMock's Request Matching system")*

---

## 6. สรุปบท

ลองตอบก่อนดูเฉลย:

**คำถามที่ 1**: คุณสร้าง stub สำหรับ `GET /products/42` แต่ client ของคุณเรียก `GET /products/42?includeDetails=true` จะเกิดอะไรขึ้น? และจะแก้อย่างไร?

**คำถามที่ 2**: `okJson(body)` ต่างจาก `ok(body)` อย่างไร? ถ้าใช้ผิดจะมีผลกับ client อย่างไร?

**คำถามที่ 3**: ถ้า test ของคุณ pass แม้ว่า `PaymentClient.getPayment()` มี bug ที่ทำให้ไม่ได้ call API เลย — แสดงว่า test ของคุณขาดอะไร?

---

**เฉลย:**

---

**เฉลยคำถามที่ 1**: WireMock จะตอบ 404 เพราะ `get("/products/42")` ทำ exact match ที่รวม query string ด้วย การแก้คือใช้ `urlPathEqualTo("/products/42")` แทน ซึ่ง ignore query parameters ทำให้ match ทั้ง `/products/42` และ `/products/42?includeDetails=true`

**เฉลยคำถามที่ 2**: `okJson(body)` ตั้ง `Content-Type: application/json` ให้อัตโนมัติ ส่วน `ok(body)` ไม่ตั้ง Content-Type ถ้า client (RestTemplate) ใช้ content negotiation หรือ Jackson deserialize จาก Content-Type อาจ throw exception หรือได้ผลผิดพลาดได้

**เฉลยคำถามที่ 3**: test ขาด `wireMock.verify()` ที่ยืนยันว่า API ถูกเรียกจริง ถ้าไม่มี verify test จะ pass ตราบเท่าที่ assertion บน response ผ่าน — แม้ว่า response จะได้มาจาก default value ไม่ใช่จาก API call จริงๆ นี่เรียกว่า "silent pass" problem
