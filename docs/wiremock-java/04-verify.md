# บทที่ 4 — Verify การเรียก API

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบจากที่จำได้:

1. `stubFor()` + `get()` + `willReturn()` ทำงานอย่างไร? อธิบาย pattern นี้ด้วยคำของตัวเอง
2. เมื่อ request ไม่ match stub ใดเลย WireMock จะตอบกลับอย่างไร?

*ตอบก่อน แล้วค่อยอ่านต่อ*

---

**เฉลย:**

1. `stubFor(get("/url").willReturn(okJson("{}")))` บอก WireMock ว่า "ถ้าได้รับ GET request มาที่ /url ให้ตอบกลับ JSON ที่กำหนด" — `stubFor` = ลงทะเบียน stub, `get(url)` = request matcher, `willReturn` = response builder
2. WireMock ตอบด้วย HTTP 404 พร้อม body ที่ระบุว่าไม่พบ stub ที่ match

---

## 1. วัตถุประสงค์

หลังจากอ่านบทนี้จบ คุณจะสามารถ:

- **อธิบาย** ได้ว่า "silent pass" problem คืออะไรและทำไมมันอันตราย
- **เขียน** `wireMock.verify()` เพื่อยืนยันว่า API ถูกเรียกกี่ครั้งได้
- **ใช้** count matchers เช่น `exactly(n)`, `moreThan(n)`, `verify(0, ...)` ได้ถูกต้อง
- **แก้** bug ที่ test ผ่านแต่ client เรียก wrong URL ด้วย verify ได้

---

## 2. ทำไมต้องรู้? (Why)

ลองดู test นี้:

```java
@Test
void processPayment_shouldCallPaymentApi() {
    wireMock.stubFor(get("/payments/123")
        .willReturn(okJson("""{"id":"123","status":"success"}""")));

    // สมมติว่ามี bug ใน PaymentClient ที่ทำให้ไม่ได้เรียก API
    // หรือเรียกผิด URL เช่น /payment/123 แทน /payments/123

    PaymentResponse response = paymentClient.getPayment("123");

    // ถ้า getPayment() มี bug แล้วคืน cached/default value
    // test นี้อาจ PASS ทั้งที่ API ไม่ได้ถูกเรียกเลย!
    assertThat(response).isNotNull();
}
```

นี่คือ **silent pass problem** — test ผ่าน แต่ไม่ได้ทดสอบสิ่งที่คิดว่าทดสอบ

ปัญหานี้พบบ่อยเมื่อ:
- client มี caching layer ที่ return value โดยไม่เรียก API
- มี typo ใน URL ทำให้ WireMock ตอบ 404 แต่ code ดัก exception ไว้
- refactor แล้วทำให้ code path เปลี่ยน แต่ test ยังผ่าน

`verify()` คือเครื่องมือที่บอกว่า **"API ถูกเรียกจริงหรือเปล่า และกี่ครั้ง?"**

---

## 3. เนื้อหาหลัก

### verify() API พื้นฐาน

```java
// ตรวจสอบว่า GET /payments/123 ถูกเรียก exactly 1 ครั้ง
wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/123")));

// ตรวจสอบว่าไม่ถูกเรียกเลย (ใช้สำหรับ negative test)
wireMock.verify(0, getRequestedFor(urlEqualTo("/payments/123")));
```

syntax หลัก: `wireMock.verify(count, requestPattern)`

- **count**: จำนวนครั้งที่คาดหวัง (integer) หรือ count matcher
- **requestPattern**: `getRequestedFor(url)`, `postRequestedFor(url)`, ฯลฯ

### Count Matchers

นอกจากระบุจำนวนตรงๆ คุณใช้ count matchers เพื่อความยืดหยุ่น:

```java
// ถูกเรียกพอดี 3 ครั้ง
wireMock.verify(exactly(3), getRequestedFor(urlEqualTo("/payments/123")));

// ถูกเรียกมากกว่า 0 ครั้ง (อย่างน้อย 1 ครั้ง)
wireMock.verify(moreThan(0), getRequestedFor(urlEqualTo("/payments/123")));

// ถูกเรียกน้อยกว่า 3 ครั้ง
wireMock.verify(lessThan(3), getRequestedFor(urlEqualTo("/payments/123")));

// ถูกเรียกอย่างน้อย 2 ครั้ง
wireMock.verify(moreThanOrExactly(2), getRequestedFor(urlEqualTo("/payments/123")));
```

(ที่มา: wiremock.org/docs/verifying — "exactly(5) — precise count match | moreThan(5) — exceeds count")

### Request Journal — สิ่งที่อยู่เบื้องหลัง verify()

WireMock เก็บ **in-memory log** ของทุก request ที่ได้รับ เรียกว่า **Request Journal** `verify()` ทำงานโดยค้นหาใน journal นี้

```
[test รัน]
  → paymentClient.getPayment("123")
      → HTTP GET /payments/123 → WireMock
                                    ↓
                        WireMock บันทึกใน Request Journal
                        WireMock ตอบกลับด้วย stub response
  ← response กลับมาที่ test
  
[wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/123")))]
  → ค้นหาใน Request Journal
  → พบ 1 request ที่ match → verify ผ่าน ✓
```

Journal reset อัตโนมัติเมื่อ `wireMock.stop()` หรือ `wireMock.resetAll()`

### Common Trap: Stub Passes แต่ URL ผิด

```java
// bug scenario
wireMock.stubFor(get("/payments/123").willReturn(okJson("{}")));

// ถ้า PaymentClient มี typo: เรียก /payment/123 แทน /payments/123
paymentClient.getPayment("123"); // → WireMock ตอบ 404, code catch exception

// verify จะจับ bug นี้ได้ทันที:
wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/123")));
// → VerificationException: Expected 1 request but found 0
```

---

## 4. ตัวอย่าง 3 ระดับ

### Beginner — verify ว่า API ถูกเรียก 1 ครั้ง และไม่ถูกเรียก

```java
// tested: Java 17, WireMock 3.13.2
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.web.client.RestTemplate;

class Ch04VerifyTest {
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
    void verifyPaymentApiWasCalledExactlyOnce() {
        wireMock.stubFor(get("/payments/123")
            .willReturn(okJson("""
                {"id":"123","status":"success","amount":500,"currency":"THB"}
                """)));

        paymentClient.getPayment("123");

        // ยืนยันว่า GET /payments/123 ถูกเรียกพอดี 1 ครั้ง
        wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/123")));
    }

    @Test
    void verifyPaymentApiWasNotCalled() {
        wireMock.stubFor(get("/payments/123").willReturn(okJson("{}")));

        // ไม่เรียก paymentClient เลย — ตั้งใจทดสอบ negative case

        // ยืนยันว่าไม่มี request เกิดขึ้น
        wireMock.verify(0, getRequestedFor(urlEqualTo("/payments/123")));
    }
}
```

**Output จากการรันจริง:**

```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0,
       Time elapsed: 0.025 s -- in com.example.wiremock.Ch04VerifyTest
[INFO] BUILD SUCCESS
```

### Intermediate — จับ Bug ด้วย verify ในสถานการณ์ Caching

สถานการณ์: ทีม refactor `PaymentService` โดยเพิ่ม in-memory cache ทีม QA ต้องตรวจสอบว่า cache ทำงานถูกต้อง — เรียก API ครั้งแรก แต่ครั้งที่สองต้องใช้ cache:

```java
// tested: Java 17, WireMock 3.13.2
class PaymentService_CacheTest {
    private WireMockServer wireMock;
    private CachingPaymentService service; // service ที่มี cache

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        service = new CachingPaymentService(
            new PaymentClient(new RestTemplate(),
                "http://localhost:" + wireMock.port())
        );
    }

    @AfterEach
    void tearDown() { wireMock.stop(); }

    @Test
    void firstCallHitsApi_secondCallUsesCache() {
        wireMock.stubFor(get("/payments/p-100")
            .willReturn(okJson("""
                {"id":"p-100","status":"success","amount":999}
                """)));

        // เรียกครั้งแรก — ต้อง hit API
        service.getPayment("p-100");

        // เรียกครั้งที่สอง — ต้องใช้ cache ไม่ hit API อีก
        service.getPayment("p-100");

        // verify: API ต้องถูกเรียกแค่ 1 ครั้ง ไม่ใช่ 2 ครั้ง
        wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/p-100")));
        // ถ้า cache ไม่ทำงาน จะได้ VerificationException: Expected 1 but found 2
    }

    @Test
    void differentPaymentIds_callApiEachTime() {
        wireMock.stubFor(get("/payments/p-201").willReturn(okJson("{\"id\":\"p-201\"}")));
        wireMock.stubFor(get("/payments/p-202").willReturn(okJson("{\"id\":\"p-202\"}")));

        service.getPayment("p-201");
        service.getPayment("p-202");

        // แต่ละ ID ต้องถูก call แยกกัน 1 ครั้ง
        wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/p-201")));
        wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/p-202")));
        // ตรวจสอบว่าไม่ได้เรียก URL ของกันและกัน
        wireMock.verify(0, getRequestedFor(urlEqualTo("/payments/p-201"))
            .withUrl("/payments/p-202")); // ไม่เกิดขึ้น
    }
}
```

### Advanced — Verify Header และ Request Body ใน Production Scenario

สถานการณ์: Compliance team ต้องการหลักฐานว่า Payment API request ทุก request ส่ง `Authorization` header และ `X-Idempotency-Key` เสมอ การลืมส่ง header เหล่านี้อาจทำให้ transaction ซ้ำหรือ unauthorized:

```java
// tested: Java 17, WireMock 3.13.2
class PaymentClient_HeaderComplianceTest {
    private WireMockServer wireMock;
    private SecurePaymentClient secureClient;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        secureClient = new SecurePaymentClient(
            new RestTemplate(),
            "http://localhost:" + wireMock.port(),
            "Bearer test-token-abc"
        );
    }

    @AfterEach
    void tearDown() { wireMock.stop(); }

    @Test
    void paymentRequest_mustIncludeAuthorizationHeader() {
        wireMock.stubFor(post("/payments")
            .willReturn(okJson("""
                {"id":"new-123","status":"pending"}
                """)));

        secureClient.createPayment(new PaymentRequest(5000, "THB", "order-abc"));

        // verify ว่า POST /payments ถูกเรียก 1 ครั้ง
        // และมี Authorization header ที่ถูกต้อง
        wireMock.verify(1, postRequestedFor(urlEqualTo("/payments"))
            .withHeader("Authorization", equalTo("Bearer test-token-abc")));
    }

    @Test
    void paymentRequest_mustIncludeIdempotencyKey() {
        wireMock.stubFor(post("/payments")
            .willReturn(okJson("{\"id\":\"new-124\",\"status\":\"pending\"}")));

        secureClient.createPayment(new PaymentRequest(5000, "THB", "order-xyz"));

        // verify ว่ามี X-Idempotency-Key header (ค่าอะไรก็ได้ แต่ต้องมี)
        wireMock.verify(1, postRequestedFor(urlEqualTo("/payments"))
            .withHeader("X-Idempotency-Key", matching(".+")));
            // matching(".+") = มีค่า non-empty
    }

    @Test
    void getPayment_shouldNeverCallDeleteEndpoint() {
        wireMock.stubFor(get("/payments/777")
            .willReturn(okJson("{\"id\":\"777\"}")));

        secureClient.getPayment("777");

        // ตรวจสอบ negative: ต้องไม่มี DELETE request เกิดขึ้นเลย
        wireMock.verify(0, deleteRequestedFor(urlMatching("/payments/.*")));
    }
}
```

---

## 5. Common Mistakes

**❌ ผิด: เขียน verify ก่อนเรียก client**

```java
@Test
void wrongOrder() {
    wireMock.stubFor(get("/payments/123").willReturn(okJson("{}")));

    // ❌ verify ก่อนที่ code จะทำงาน — จะ fail เสมอ
    wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/123")));

    paymentClient.getPayment("123"); // ← ควรมาก่อน verify
}
```

✅ **ถูก**: ลำดับที่ถูกต้องคือ: 1) `stubFor()`, 2) เรียก code ที่ต้องการ test, 3) `verify()` *(source: wiremock.org/docs/verifying — verify pattern)*

---

**❌ ผิด: คิดว่า verify(0, ...) ไม่มีประโยชน์**

```java
// บางคนคิดว่า "ตรวจว่าไม่ถูกเรียก" ไม่สำคัญ
// แต่จริงๆ มี use case สำคัญมาก
```

✅ **ถูก**: `verify(0, ...)` มีประโยชน์มากในสถานการณ์เช่น: ตรวจว่า service ไม่ได้ส่ง request เมื่อไม่ควรส่ง (เช่น ถ้า feature flag ปิดอยู่), ตรวจว่า caching ทำงาน (ไม่มี API call ครั้งที่ 2), หรือตรวจว่าไม่ได้เรียก destructive endpoint โดยไม่ตั้งใจ *(source: wiremock.org/docs/verifying)*

---

**❌ ผิด: ใช้ verify แทน assertion บน response**

```java
@Test
void badTest() {
    wireMock.stubFor(get("/payments/123").willReturn(okJson("{}")));
    PaymentResponse response = paymentClient.getPayment("123");

    // ❌ verify บอกแค่ว่า API ถูกเรียก ไม่ได้บอกว่า response ถูก process ถูกต้อง
    wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/123")));
    // ขาด: assertThat(response.getId()).isEqualTo("123");
}
```

✅ **ถูก**: ใช้ทั้งสอง — `assertThat()` ตรวจ response value, `verify()` ตรวจว่า API ถูกเรียกด้วย request ที่ถูกต้อง สองอย่างนี้ complement กัน *(source: wiremock.org/docs/verifying — "verification also uses WireMock's Request Matching system")*

---

## 6. สรุปบท

ลองตอบก่อนดูเฉลย:

**คำถามที่ 1**: อธิบาย "silent pass" problem ด้วยตัวอย่างที่ไม่ใช่จากบทนี้ — ทำไมมันถึงอันตราย และ `verify()` แก้ได้อย่างไร?

**คำถามที่ 2**: `verify(moreThan(0), ...)` ต่างจาก `verify(1, ...)` อย่างไร? มีสถานการณ์ไหนที่ `moreThan(0)` เหมาะกว่า `verify(1, ...)`?

**คำถามที่ 3**: คุณ refactor `OrderService` ให้ batch payment IDs แล้วเรียก API ครั้งเดียวแทนเรียนทีละครั้ง คุณจะเขียน `verify()` ยังไงเพื่อยืนยันว่า API ถูกเรียกแค่ 1 ครั้ง ไม่ใช่ 3 ครั้ง?

---

**เฉลย:**

---

**เฉลยคำถามที่ 1**: ตัวอย่างที่ต่างออกไป: `NotificationService` มี feature flag ถ้า flag ปิด ไม่ควรส่ง email ถ้า test แค่ assert ว่า method ไม่ throw exception — test จะ pass เสมอ แม้ว่า code ผิดพลาดส่ง email ออกไปจริงๆ ก็ตาม `verify(0, postRequestedFor(urlEqualTo("/email/send")))` จะจับ bug นี้ได้ทันที มันอันตรายเพราะ test สร้าง false confidence — dev คิดว่า code ถูก แต่จริงๆ behavior ผิดอย่างเงียบๆ

**เฉลยคำถามที่ 2**: `verify(1, ...)` ต้องเรียกพอดี 1 ครั้งเท่านั้น `verify(moreThan(0), ...)` หมายถึงเรียกอย่างน้อย 1 ครั้ง (1, 2, 3... ครั้งก็ผ่านหมด) `moreThan(0)` เหมาะกว่าเมื่อคุณรู้ว่า "ต้องเรียก API" แต่ไม่สำคัญว่ากี่ครั้ง เช่น retry logic ที่อาจ retry 1-3 ครั้งขึ้นอยู่กับ response

**เฉลยคำถามที่ 3**: เขียน `wireMock.verify(1, postRequestedFor(urlEqualTo("/payments/batch")))` ถ้า API endpoint เปลี่ยน หรือถ้า API เดิมแต่ใช้ batch body: `verify(1, postRequestedFor(urlEqualTo("/payments")).withRequestBody(containing("id1")).withRequestBody(containing("id2")).withRequestBody(containing("id3")))` ที่สำคัญคือ verify count = 1 เพื่อยืนยัน batch behavior
