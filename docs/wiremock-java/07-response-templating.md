# บทที่ 7 — Response Templating

---

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบคำถามนี้จากบทที่ 6:

> **`urlPathEqualTo()` ต่างจาก `urlEqualTo()` อย่างไร?**

ลองตอบในใจก่อน แล้วเลื่อนลงมาดูเฉลย

---

**เฉลย:** `urlEqualTo("/path")` match URL ที่ตรงทั้งหมด รวม query string — ดังนั้น `/path?x=1` จะ **ไม่** match `urlEqualTo("/path")` ส่วน `urlPathEqualTo("/path")` match เฉพาะ **path** ไม่สนใจ query string — ดังนั้น `/path`, `/path?x=1`, `/path?a=1&b=2` ทุกอย่างจะ match กฎง่ายๆ: ถ้าจะใช้ `withQueryParam()` ให้ใช้ `urlPathEqualTo()` เสมอ

---

## ส่วนที่ 1: วัตถุประสงค์

เมื่ออ่านบทนี้จบ คุณจะสามารถ:

- อธิบายว่า Response Templating คืออะไรและแก้ปัญหาอะไร
- เปิดใช้ templating แบบ per-stub ด้วย `withTransformers("response-template")`
- เขียน Handlebars template ใน response body โดยใช้ `{{request.pathSegments.[N]}}` และ `{{request.headers.X}}`
- สร้าง test ที่พิสูจน์ว่า response body เปลี่ยนตาม request จริง

---

## ส่วนที่ 2: ทำไมต้องรู้? (Why)

ลองนึกถึงปัญหานี้: ระบบ Order Service มี payment หลายรายการ เมื่อ test เรียก `GET /payments/pay_t01`, เราต้องการ response ที่มี `"id": "pay_t01"` ตรงกับที่เรียก ไม่ใช่ค่า hardcode เสมอ

แบบเก่าต้องสร้าง stub แยกทุก payment ID:

```java
stubFor(get("/payments/pay_t01").willReturn(okJson("{\"id\":\"pay_t01\",...}")));
stubFor(get("/payments/pay_t02").willReturn(okJson("{\"id\":\"pay_t02\",...}")));
stubFor(get("/payments/pay_t03").willReturn(okJson("{\"id\":\"pay_t03\",...}")));
// ... ทำแบบนี้ทุก ID
```

**Response Templating แก้ปัญหานี้:** เขียน stub เดียวแล้วให้ template ดึงค่าจาก request มาใส่ใน response อัตโนมัติ:

```java
stubFor(get(urlPathMatching("/payments/.*"))
    .willReturn(aResponse()
        .withBody("{\"id\":\"{{request.pathSegments.[1]}}\",\"status\":\"success\"}")
        .withTransformers("response-template")));
```

stub เดียวนี้ handle ได้ทุก payment ID

---

## ส่วนที่ 3: เนื้อหาหลัก

### Response Templating คืออะไร

WireMock ใช้ **Handlebars** เป็น template engine — format เดียวกับที่ใช้ใน email template หลายๆ ระบบ Template expression อยู่ใน `{{ }}` และสามารถดึงข้อมูลจาก request ปัจจุบันมาแทรกใน response ได้

### วิธีเปิดใช้งาน: 2 วิธี

**วิธีที่ 1 (แนะนำสำหรับ beginner): Per-stub** — ใส่ `withTransformers("response-template")` ใน stub นั้นๆ:

```java
wireMock.stubFor(get(urlPathEqualTo("/payments/pay_t01"))
    .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"id\":\"{{request.pathSegments.[1]}}\",\"status\":\"success\"}")
        .withTransformers("response-template")));
```

ข้อดี: template ทำงานแค่ stub ที่ระบุ ไม่กระทบ stub อื่น

**วิธีที่ 2: Global** — เปิดทุก stub พร้อมกันตอน config server:

```java
wireMock = new WireMockServer(wireMockConfig()
    .dynamicPort()
    .globalTemplating(true));
```

ข้อดี: ไม่ต้องเพิ่ม `withTransformers()` ทุก stub ข้อเสีย: ทุก stub จะถูก process ผ่าน template engine แม้ไม่ได้ใช้ template

### Template helpers ที่ใช้บ่อย

**Path segments** — ดึง segment จาก URL path โดย index เริ่มที่ 0:

| URL | Expression | ค่าที่ได้ |
|-----|-----------|----------|
| `/payments/pay_001` | `{{request.pathSegments.[1]}}` | `pay_001` |
| `/orders/123/items` | `{{request.pathSegments.[0]}}` | `orders` |
| `/orders/123/items` | `{{request.pathSegments.[2]}}` | `items` |

```java
.withBody("{\"id\":\"{{request.pathSegments.[1]}}\",\"status\":\"success\"}")
```

**Request headers** — ดึงค่า header จาก request:

```java
// ดึง X-Request-Id header มาใส่ใน response header
.withHeader("X-Echo-Id", "{{request.headers.X-Request-Id}}")
```

**Request body** — ดึง body ทั้งหมดกลับมา:

```java
.withBody("{\"echo\":\"{{request.body}}\"}")
```

---

## ส่วนที่ 4: ตัวอย่าง 3 ระดับ

### Beginner — echo payment ID จาก path

```java
// tested: WireMock 3.13.2, Java 17
@Test
void echoPaymentIdFromPath() {
    wireMock.stubFor(get(urlPathEqualTo("/payments/pay_t01"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"id\":\"{{request.pathSegments.[1]}}\",\"status\":\"success\",\"amount\":500,\"currency\":\"THB\"}")
            .withTransformers("response-template")));

    PaymentResponse response = paymentClient.getPayment("pay_t01");

    assertThat(response.getId()).isEqualTo("pay_t01");    // template แทรก "pay_t01" แทน {{...}}
    assertThat(response.getStatus()).isEqualTo("success");
}
```

Output เมื่อรัน `mvn test -Dtest=Ch07ResponseTemplatingTest`:
```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.855 s
[INFO] BUILD SUCCESS
```

### Intermediate — echo header กลับในชื่อ header ต่าง

สถานการณ์จริง: API gateway สร้าง `X-Request-Id` เพื่อ tracing แล้วต้องการให้ server echo กลับใน `X-Echo-Id` เพื่อ verify correlation

```java
// tested: WireMock 3.13.2, Java 17
@Test
void echoRequestIdHeader() {
    wireMock.stubFor(get(urlPathEqualTo("/payments/pay_h01"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withHeader("X-Echo-Id", "{{request.headers.X-Request-Id}}")
            .withBody("{\"id\":\"pay_h01\",\"status\":\"success\",\"amount\":0,\"currency\":\"THB\"}")
            .withTransformers("response-template")));

    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Request-Id", "req-abc-123");
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    ResponseEntity<PaymentResponse> resp = new RestTemplate().exchange(
        "http://localhost:" + wireMock.port() + "/payments/pay_h01",
        HttpMethod.GET, entity, PaymentResponse.class);

    assertThat(resp.getHeaders().getFirst("X-Echo-Id")).isEqualTo("req-abc-123");
}
```

### Advanced — stub เดียว handle ทุก payment ID ด้วย urlPathMatching

```java
// tested: WireMock 3.13.2, Java 17
@Test
void singleStubHandlesMultiplePaymentIds() {
    // stub เดียวตอบทุก GET /payments/{id}
    wireMock.stubFor(get(urlPathMatching("/payments/.*"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {
                  "id": "{{request.pathSegments.[1]}}",
                  "status": "success",
                  "amount": 999,
                  "currency": "THB"
                }
                """)
            .withTransformers("response-template")));

    // เรียก 3 payment IDs ต่างกัน — response สะท้อน ID ที่เรียก
    PaymentResponse r1 = paymentClient.getPayment("pay_AAA");
    PaymentResponse r2 = paymentClient.getPayment("pay_BBB");
    PaymentResponse r3 = paymentClient.getPayment("pay_CCC");

    assertThat(r1.getId()).isEqualTo("pay_AAA");
    assertThat(r2.getId()).isEqualTo("pay_BBB");
    assertThat(r3.getId()).isEqualTo("pay_CCC");
}
```

---

## ส่วนที่ 5: Common Mistakes

**❌ ลืม `withTransformers("response-template")` แล้วสงสัยว่าทำไม template ไม่ทำงาน**

```java
// ❌ ผิด — ไม่มี withTransformers
wireMock.stubFor(get(urlPathEqualTo("/payments/pay_001"))
    .willReturn(aResponse()
        .withBody("{\"id\":\"{{request.pathSegments.[1]}}\"}")));
// response จะเป็น: {"id":"{{request.pathSegments.[1]}}"}  ← literal text ไม่ได้แปล
```

```java
// ✅ ถูก — เพิ่ม withTransformers
wireMock.stubFor(get(urlPathEqualTo("/payments/pay_001"))
    .willReturn(aResponse()
        .withBody("{\"id\":\"{{request.pathSegments.[1]}}\"}")
        .withTransformers("response-template")));
// response จะเป็น: {"id":"pay_001"}  ← ถูกต้อง
```

เหตุผล: ถ้าไม่ระบุ `withTransformers("response-template")` WireMock จะ return body แบบ literal text โดยไม่ process template *(source: https://wiremock.org/docs/response-templating/)*

---

**❌ index ผิดใน pathSegments**

```java
// URL: /payments/pay_001
// ❌ ผิด — index 0 คือ "payments" ไม่ใช่ "pay_001"
"{{request.pathSegments.[0]}}"   // ได้ "payments"
```

```java
// ✅ ถูก — index 1 คือ segment ที่ 2 (นับจาก 0)
"{{request.pathSegments.[1]}}"   // ได้ "pay_001"
```

เหตุผล: pathSegments นับจาก 0 โดย segment แรกหลัง `/` คือ index 0 *(source: https://wiremock.org/docs/response-templating/)*

---

**❌ ใช้ `withTransformers()` แต่ใส่ชื่อผิด**

```java
// ❌ ผิด — ชื่อ transformer ผิด
.withTransformers("response-templating")    // มี "ing" ต่อท้าย
.withTransformers("ResponseTemplate")       // ตัวพิมพ์ใหญ่
```

```java
// ✅ ถูก — ชื่อที่ถูกต้องคือ "response-template"
.withTransformers("response-template")
```

เหตุผล: ชื่อ transformer ต้องตรงกันเป๊ะกับที่ WireMock ลงทะเบียนไว้ ถ้าพิมพ์ผิด จะไม่ error แต่ template จะไม่ถูก process *(source: https://wiremock.org/docs/response-templating/)*

---

## ส่วนที่ 6: สรุปบท

Response Templating ช่วยให้ stub ตอบ response แบบ dynamic โดยดึงข้อมูลจาก request มาแทรกใน body หรือ header WireMock ใช้ Handlebars syntax ใน `{{ }}` เปิดใช้ per-stub ด้วย `.withTransformers("response-template")` หรือ globally ด้วย `.globalTemplating(true)` template helpers ที่ใช้บ่อยได้แก่ `{{request.pathSegments.[N]}}` สำหรับ URL segment และ `{{request.headers.X}}` สำหรับ header

**คำถาม Retrieval — ลองตอบก่อนดูเฉลย:**

1. `withTransformers("response-template")` ใช้ทำอะไร และต้องใส่ตรงไหนใน stub?
2. สำหรับ URL `/orders/456/items` ถ้าต้องการดึง `"456"` มาใช้ใน template จะเขียนยังไง?
3. ความแตกต่างระหว่าง global templating และ per-stub templating คืออะไร?

---

**เฉลย:**

1. `withTransformers("response-template")` บอก WireMock ให้ process Handlebars template ใน response body/header ของ stub นั้น ใส่ใน `.willReturn(aResponse()...withTransformers("response-template"))` ถ้าไม่ใส่ `{{ }}` expression จะ return เป็น literal text
2. เขียน `{{request.pathSegments.[1]}}` เพราะ segment นับจาก 0: `orders`=0, `456`=1, `items`=2
3. Global templating (`.globalTemplating(true)`) ทำให้ทุก stub process template โดยอัตโนมัติ ไม่ต้องเพิ่ม `withTransformers()` ทุก stub ส่วน per-stub จะ process template เฉพาะ stub ที่ระบุ `withTransformers()` เท่านั้น
