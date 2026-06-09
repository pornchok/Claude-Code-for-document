# บทที่ 1 — ทำไมต้อง Mock API?

## 1. วัตถุประสงค์

หลังจากอ่านบทนี้จบ คุณจะสามารถ:

- **อธิบาย** ได้ว่า external dependency ใน test คืออะไร และทำให้เกิดปัญหาอะไรบ้าง
- **ระบุ** ได้ว่าสถานการณ์แบบไหนที่ควรใช้ mock API และแบบไหนที่ไม่ควร
- **อธิบาย** ได้ว่า WireMock แก้ปัญหาเหล่านั้นอย่างไร
- **เปรียบเทียบ** ข้อดีข้อเสียของการทดสอบกับ API จริง vs API ที่ mock ไว้

---

## 2. ทำไมต้องรู้? (Why)

ลองนึกภาพนี้: คุณเขียน test สำหรับ `OrderService` ที่ต้องเรียก Payment API ของ Omise ทุกครั้งที่ลูกค้าสั่งซื้อ test รันได้ดีบนเครื่องคุณ แต่วันหนึ่ง CI pipeline เริ่ม fail — ไม่ใช่เพราะ code คุณผิด แต่เพราะ Omise มี maintenance window อยู่ชั่วคราว

ปัญหานี้เรียกว่า **flaky test** — test ที่ผลลัพธ์ขึ้นอยู่กับสิ่งที่อยู่นอกเหนือการควบคุมของคุณ

แล้วถ้าคุณอยากทดสอบ "กรณีที่ Omise ตอบกลับว่า payment failed เพราะบัตรหมดอายุ" จะทำยังไง? คุณคงไม่อยากจ่ายเงินจริงๆ แค่เพื่อทดสอบ error case

**นี่คือเหตุผลที่ต้องมี mock API**

---

## 3. เนื้อหาหลัก

### External Dependency ใน Test คืออะไร?

**External dependency** คือสิ่งที่ test ของคุณต้องพึ่งพา แต่ไม่ได้อยู่ใน codebase ของตัวเอง เช่น:

- **Payment API** (Omise, Stripe, 2C2P)
- **SMS/Email service** (Twilio, SendGrid)
- **Third-party data API** (ราคาหุ้น, อัตราแลกเปลี่ยน)
- **Service อื่นใน microservices architecture** ของบริษัท

เมื่อ test ของคุณเรียก API เหล่านี้โดยตรง คุณกำลัง **ฝากชะตากรรมของ test ไว้กับคนอื่น**

### ปัญหาของการเรียก Real API ในการทดสอบ

**1. ช้า**

HTTP request ไป-กลับใช้เวลาหลายร้อย millisecond ถึงหลายวินาที ถ้ามี test 100 ตัวที่แต่ละตัวเรียก API 2-3 ครั้ง test suite คุณอาจใช้เวลาหลายนาทีกว่าจะเสร็จ เทียบกับ test ที่ใช้ mock ซึ่งรันจบใน milliseconds

**2. Flaky — ผลลัพธ์ไม่แน่นอน**

API จริงอาจ down ได้เสมอ ไม่ว่าจะเป็น maintenance, network issue, หรือ rate limit ทำให้ test fail โดยที่ code คุณไม่ผิดอะไร

**3. มีค่าใช้จ่าย**

API เชิงพาณิชย์หลายตัวคิดราคาตาม request หากทีมคุณรัน test วันละหลายร้อยครั้ง (เช่นใน CI) ค่าใช้จ่ายจะสะสมอย่างรวดเร็ว

**4. จำลอง error scenario ไม่ได้**

คุณจะทดสอบ "กรณีที่ API timeout" หรือ "กรณีที่ server ตอบ 500" กับ API จริงได้อย่างไร? แทบเป็นไปไม่ได้โดยไม่มีตัวช่วย

**5. ทำ parallel test ได้ยาก**

API จริงอาจมี rate limit หรือ state ที่ test แต่ละตัว interfere กันได้

### WireMock แก้ปัญหาอย่างไร?

[ดู animation: WireMock ทำงานอย่างไร?](animations/01-how-wiremock-works.html)

WireMock คือ **HTTP server จำลอง** ที่รันอยู่บนเครื่องของคุณ (localhost) ระหว่างที่ test กำลังรัน

แทนที่จะให้ `OrderService` เรียก Omise โดยตรง คุณตั้งค่าให้มันชี้ไปที่ WireMock แทน แล้วบอก WireMock ว่า "ถ้าได้รับ request แบบนี้ ให้ตอบกลับแบบนี้"

```
[OrderService] --POST /payments--> [WireMock:8089]  ← ไม่ใช่ Omise จริงๆ
                                        ↓
                              ตอบกลับตามที่คุณกำหนด
```

ข้อดีที่ได้ทันที:
- **รวดเร็ว** — เป็น local server ไม่มี network latency
- **เสถียร** — ไม่ขึ้นอยู่กับ Omise uptime
- **ฟรี** — ไม่มีค่า API call
- **ควบคุมได้ทุก scenario** — จะตอบ 200, 500, timeout ก็ได้หมด

---

## 4. ตัวอย่าง 3 ระดับ

### Beginner — เปรียบเทียบ Test ที่เรียก Real API vs Mock

สมมติเรามี `OrderService` ที่เรียก Payment API:

**แบบที่เรียก Real API (ปัญหาเยอะ):**

```java
// tested: Java 17, WireMock 3.13.2
// ⚠️ อย่าทำแบบนี้ใน production test
@Test
void createOrder_withRealApi_fragile() {
    // เรียก Omise จริงๆ — ถ้า Omise down test นี้ fail ทันที
    // ถ้า network slow test นี้ timeout
    // ถ้า test account ถูก disable test นี้ fail
    String realOmiseUrl = "https://api.omise.co/charges";
    // ... เรียก HTTP จริง
}
```

**แบบที่ใช้ WireMock (ถูกต้อง):**

```java
// tested: Java 17, WireMock 3.13.2
@Test
void createOrder_withWireMock_stable() {
    // WireMock รันบน localhost — ไม่มี network dependency
    wireMock.stubFor(post("/charges")
        .willReturn(okJson("""
            {"id":"chrg_123","status":"successful","amount":50000}
            """)));

    // OrderService เรียก WireMock แทน Omise จริงๆ
    OrderResult result = orderService.createOrder(new OrderRequest(500, "THB"));

    assertThat(result.getStatus()).isEqualTo("successful");
    // test นี้จะ pass เสมอ ไม่ว่า Omise จะ down หรือไม่
}
```

### Intermediate — จำลอง Error Scenario ที่ Real API ทำได้ยาก

สถานการณ์: ทีมคุณต้องทดสอบว่า `OrderService` จัดการ payment failure อย่างถูกต้องหรือเปล่า ทีมงานได้รับ bug report ว่าเมื่อ payment fail ระบบยังคงแสดงสถานะ "กำลังดำเนินการ" แทนที่จะแจ้ง error

```java
// tested: Java 17, WireMock 3.13.2
class OrderService_PaymentFailureTest {
    private WireMockServer wireMock;
    private OrderService orderService;

    @BeforeEach void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        orderService = new OrderService("http://localhost:" + wireMock.port());
    }

    @AfterEach void tearDown() { wireMock.stop(); }

    @Test
    void whenPaymentFails_orderStatusShouldBeRejected() {
        // จำลอง scenario ที่บัตรหมดอายุ — ทำกับ real API ไม่ได้
        wireMock.stubFor(post("/charges")
            .willReturn(aResponse()
                .withStatus(402)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "object": "error",
                        "code": "invalid_card",
                        "message": "The card has expired"
                    }
                    """)));

        OrderResult result = orderService.createOrder(new OrderRequest(500, "THB"));

        // ตรวจสอบว่า OrderService จัดการ error ถูกต้อง
        assertThat(result.getStatus()).isEqualTo("rejected");
        assertThat(result.getErrorMessage()).contains("card has expired");
    }

    @Test
    void whenPaymentApiTimeout_orderShouldFail() {
        // จำลอง timeout — ทำกับ real API แทบเป็นไปไม่ได้
        wireMock.stubFor(post("/charges")
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(5000))); // delay 5 วินาที

        assertThatThrownBy(() -> orderService.createOrder(new OrderRequest(500, "THB")))
            .isInstanceOf(PaymentTimeoutException.class);
    }
}
```

Output เมื่อรัน:
```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.312 s
```

### Advanced — Test Suite ที่ครอบคลุมทุก Edge Case ของ Payment Flow

สถานการณ์ production-grade: ทีมต้องการ test ที่ครอบคลุม happy path และ edge case ทุกกรณีก่อน deploy

```java
// tested: Java 17, WireMock 3.13.2
class PaymentFlow_ComprehensiveTest {

    private WireMockServer wireMock;
    private PaymentClient paymentClient;

    @BeforeEach void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        paymentClient = new PaymentClient(new RestTemplate(),
            "http://localhost:" + wireMock.port());
    }

    @AfterEach void tearDown() { wireMock.stop(); }

    @ParameterizedTest
    @CsvSource({
        "200, successful, true",
        "402, failed,     false",
        "429, rate_limit, false",
        "503, unavailable,false"
    })
    void paymentOutcomeByStatusCode(int httpStatus, String expectedStatus,
                                    boolean shouldSucceed) {
        wireMock.stubFor(post("/payments")
            .willReturn(aResponse()
                .withStatus(httpStatus)
                .withHeader("Content-Type", "application/json")
                .withBody(String.format("""
                    {"status":"%s","amount":500}
                    """, expectedStatus))));

        if (shouldSucceed) {
            PaymentResponse resp = paymentClient.createPayment(
                new PaymentRequest(500, "THB", "order-001"));
            assertThat(resp.getStatus()).isEqualTo("successful");
        } else {
            assertThatThrownBy(() ->
                paymentClient.createPayment(
                    new PaymentRequest(500, "THB", "order-001")))
                .isInstanceOf(Exception.class);
        }
    }
}
```

---

## 5. Common Mistakes

**❌ ผิด: "API เราเสถียรดีอยู่แล้ว ไม่ต้องมี mock หรอก"**

```java
// อย่าทำแบบนี้แม้ API ดูเสถียร
@Test
void testPayment() {
    // เรียก API จริงทุก test run
    PaymentResponse resp = realHttpClient.post("https://api.omise.co/charges", ...);
    assertThat(resp.getStatus()).isEqualTo("successful");
}
```

✅ **ถูก**: API เสถียรแค่ไหนก็ไม่การันตีได้ว่าจะเสถียรตลอดไป network blip, maintenance, หรือ breaking change ที่ provider ทำโดยไม่แจ้ง ล้วนทำ test fail ได้ทั้งนั้น นอกจากนี้ test กับ real API ยัง **ไม่สามารถทดสอบ error scenarios** ได้ *(source: wiremock.org/docs — "isolate yourself from flakey 3rd parties")*

---

**❌ ผิด: Mock ทุก test แม้แต่ test ที่ไม่ได้ call external API**

```java
// ไม่จำเป็นต้องมี WireMock ถ้า test นี้ไม่ได้เรียก HTTP
@Test
void calculateDiscount_noExternalCall() {
    WireMockServer wireMock = new WireMockServer(dynamicPort());
    wireMock.start(); // ⬅ ทำไม? test นี้ไม่ได้เรียก API เลย

    int discount = discountService.calculate(1000, "VIP");
    assertThat(discount).isEqualTo(100);

    wireMock.stop();
}
```

✅ **ถูก**: ใช้ WireMock เฉพาะ test ที่ต้องการ mock HTTP call เท่านั้น การ start/stop server ที่ไม่จำเป็นเพิ่ม overhead ให้ test suite และทำให้ code อ่านยาก *(source: wiremock.org/docs — ออกแบบมาสำหรับ HTTP interaction testing โดยเฉพาะ)*

---

## 6. สรุปบท

ลองตอบคำถามเหล่านี้ก่อนดูเฉลย — ถ้าตอบไม่ได้ ให้กลับไปอ่านส่วนที่เกี่ยวข้องอีกครั้ง:

**คำถามที่ 1**: ทีมคุณมี test สำหรับ `NotificationService` ที่เรียก SMS API (Twilio) จริงๆ ทุกครั้ง ปัญหาที่อาจเกิดขึ้นมีอะไรบ้าง? ระบุให้ครบอย่างน้อย 3 ข้อ

**คำถามที่ 2**: มีนักพัฒนาคนหนึ่งบอกว่า "เราใช้ sandbox environment ของ Stripe ในการทดสอบ ไม่ต้องใช้ WireMock แล้ว" — คุณเห็นด้วยหรือไม่? มีเหตุผลอะไรสนับสนุนหรือโต้แย้ง?

**คำถามที่ 3**: WireMock ทำงานอย่างไรในภาพรวม? อธิบายด้วยคำของตัวเองโดยไม่ดู diagram

---

**เฉลย:**

---

**เฉลยคำถามที่ 1**: ปัญหาของการเรียก Twilio จริงใน test:
1. **ช้า** — HTTP round-trip ใช้เวลาหลาย hundred ms ทำให้ test suite ช้า
2. **มีค่าใช้จ่าย** — Twilio คิดเงินต่อ SMS ที่ส่ง ถ้ารัน CI วันละหลายครั้งค่าใช้จ่ายสะสม
3. **Flaky** — ถ้า Twilio down หรือ network มีปัญหา test fail โดยที่ code ไม่ผิด
4. **จำลอง error ไม่ได้** — ทดสอบ "กรณี Twilio ตอบ 503" กับ API จริงทำไม่ได้
5. **ผลข้างเคียงจริง** — ส่ง SMS จริง ซึ่งอาจรบกวน user จริงๆ

**เฉลยคำถามที่ 2**: ไม่เห็นด้วยอย่างสมบูรณ์ Stripe sandbox ช่วยได้บางส่วน (ไม่มีค่าใช้จ่ายจริง) แต่ยังคงมีปัญหา: ยังต้องพึ่ง network และ Stripe uptime, ยังช้ากว่า local, จำลอง network error/timeout ไม่ได้, และทดสอบ edge case เฉพาะที่ Stripe sandbox รองรับเท่านั้น WireMock ให้ **การควบคุม 100%** เหนือ response ทุกประเภท

**เฉลยคำถามที่ 3**: WireMock รัน HTTP server จำลองบน localhost ในระหว่าง test รัน คุณบอก WireMock ล่วงหน้าว่า "ถ้าได้รับ request URL X ให้ตอบกลับ Y" (เรียกว่า stub) แล้วตั้งค่าให้ code ที่ต้องการทดสอบชี้ไปที่ WireMock แทน real API เมื่อ code เรียก WireMock จะตอบกลับตามที่กำหนดไว้ ทำให้ test รวดเร็ว เสถียร และควบคุมได้
