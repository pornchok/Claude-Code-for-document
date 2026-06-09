# บทที่ 5 — Request Matching: POST Body

---

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบคำถามนี้จากบทที่ 4:

> **`verify()` ต่างจากแค่ assert ผลลัพธ์อย่างไร?**

ลองตอบในใจก่อน แล้วเลื่อนลงมาดูเฉลย

---

**เฉลย:** `verify()` ตรวจสอบจาก **request journal** ของ WireMock ว่า HTTP request เกิดขึ้นจริงหรือไม่ — กี่ครั้ง, ตรงกับ URL/header/body ที่กำหนดหรือเปล่า ต่างจาก assert ที่ตรวจแค่ **ผลลัพธ์** ที่ response ส่งกลับมา ถ้า production code เรียก API ผิด URL แต่โชคดีที่ stub ตอบ response ถูก — `verify()` จะจับได้ แต่ assert ธรรมดาจะไม่จับ

---

## ส่วนที่ 1: วัตถุประสงค์

เมื่ออ่านบทนี้จบ คุณจะสามารถ:

- เขียน stub สำหรับ POST request ด้วย `post()` และ `withRequestBody()`
- อธิบายความแตกต่างระหว่าง `equalToJson()` แบบ strict และแบบใช้ options
- แก้ปัญหาเมื่อ stub ไม่ match เพราะ body ไม่ตรง
- สร้าง test ที่ verify ว่า Payment API รับ body ถูกต้อง

---

## ส่วนที่ 2: ทำไมต้องรู้? (Why)

บทที่ 3-4 เราจัดการแค่ GET requests ซึ่งง่ายเพราะข้อมูลอยู่ใน URL แต่ในระบบจริง payment system, booking system, หรือ order service มักส่งข้อมูลสำคัญผ่าน **request body** แบบ JSON

ลองนึกถึงสถานการณ์นี้: Order Service ส่ง POST ไปหา Omise Payment API พร้อม body `{"amount": 500, "currency": "THB"}` แต่ production code ดันส่ง `{"amount": "500", "currency": "THB"}` (amount เป็น string แทน number) — การ test แบบเก่าที่ไม่ตรวจ body จะไม่จับ bug นี้

**ปัญหาที่แก้:** ถ้า WireMock ไม่ตรวจ body, test ผ่านแต่ production fail เพราะ Payment API จริงปฏิเสธ request ที่ส่ง body ผิด format

---

## ส่วนที่ 3: เนื้อหาหลัก

### POST stub พื้นฐาน

สำหรับ POST request, เปลี่ยนจาก `get()` เป็น `post()`:

```java
wireMock.stubFor(post(urlEqualTo("/payments"))
    .willReturn(okJson("{\"id\":\"pay_001\",\"status\":\"success\"}")));
```

แต่นี่ยัง match **ทุก POST** ไปที่ `/payments` โดยไม่สนใจ body เลย ถ้าต้องการตรวจว่า body ถูกต้องด้วย ต้องเพิ่ม `withRequestBody()`

### equalToJson — ตรวจ body แบบ JSON semantic

```java
wireMock.stubFor(post(urlEqualTo("/payments"))
    .withRequestBody(equalToJson("""
        {"amount": 500, "currency": "THB", "description": "Order #001"}
        """))
    .willReturn(okJson("{\"id\":\"pay_001\",\"status\":\"success\"}")));
```

`equalToJson()` ทำ **semantic comparison** — ไม่ใช่ string comparison ดังนั้น whitespace หรือ key order ต่างกันก็ยังถือว่า match:

```
{"amount":500,"currency":"THB","description":"Order #001"}   ✅ match
{ "currency": "THB", "amount": 500, "description": "Order #001" }   ✅ match (key order ต่าง)
{"amount":"500","currency":"THB","description":"Order #001"}  ❌ ไม่ match (amount เป็น string)
```

### เมื่อ body ไม่ match — WireMock ตอบ 404

นี่คือพฤติกรรมสำคัญที่ต้องจำ: ถ้า request เข้ามาแต่ไม่ match stub ไหนเลย WireMock ตอบ HTTP 404 พร้อม body อธิบายว่า match ล้มเหลวตรงไหน

```
HTTP/1.1 404 Not Found
...
{
  "status": 404,
  "message": "No response could be served as there are no stub mappings in this WireMock instance."
}
```

ซึ่งทำให้ RestTemplate던지น exception `HttpClientErrorException: 404 Not Found` — สัญญาณชัดว่า body ที่ส่งไม่ตรงกับ stub

### equalToJson options: ignoreArrayOrder และ ignoreExtraElements

signature เต็มของ `equalToJson()`:

```java
equalToJson(String json, boolean ignoreArrayOrder, boolean ignoreExtraElements)
```

**ignoreArrayOrder = true** — array ที่มี element เหมือนกันแต่ลำดับต่างถือว่า match:

```java
// stub กำหนด
{"tags": ["payment", "thb"]}

// request ส่งมา — ยัง match
{"tags": ["thb", "payment"]}
```

**ignoreExtraElements = true** — request ส่ง field เพิ่มมาก็ยังถือว่า match:

```java
// stub กำหนดแค่
{"amount": 1000, "currency": "THB"}

// request ส่ง description มาด้วย — ยัง match
{"amount": 1000, "currency": "THB", "description": "Subscription renewal"}
```

ใช้ option ทั้งสองพร้อมกัน:

```java
.withRequestBody(equalToJson("""
    {"amount": 1000, "currency": "THB"}
    """, true, true))
```

**เมื่อไหร่ควรใช้ options?**

| สถานการณ์ | ignoreArrayOrder | ignoreExtraElements |
|-----------|-----------------|---------------------|
| API ที่ client อาจส่ง field optional เพิ่ม | ไม่จำเป็น | ✅ true |
| Test เฉพาะ field ที่สำคัญ ไม่สนส่วนที่เหลือ | ไม่จำเป็น | ✅ true |
| API รับ array ที่ลำดับไม่สำคัญ (เช่น tag list) | ✅ true | ไม่จำเป็น |
| ต้องการ strict match ทุก field ทุก element | false | false |

---

## ส่วนที่ 4: ตัวอย่าง 3 ระดับ

### Beginner — stub POST พื้นฐาน

```java
// tested: WireMock 3.13.2, Java 17
@Test
void createPaymentMatchesExactBody() {
    wireMock.stubFor(post(urlEqualTo("/payments"))
        .withRequestBody(equalToJson("""
            {"amount": 500, "currency": "THB", "description": "Order #001"}
            """))
        .willReturn(okJson("""
            {"id":"pay_001","status":"success","amount":500,"currency":"THB"}
            """)));

    PaymentResponse response = paymentClient.createPayment(
        new PaymentRequest(500, "THB", "Order #001"));

    assertThat(response.getId()).isEqualTo("pay_001");
    assertThat(response.getStatus()).isEqualTo("success");
    wireMock.verify(1, postRequestedFor(urlEqualTo("/payments")));
}
```

Output เมื่อรัน `mvn test -Dtest=Ch05RequestMatchingPostTest`:
```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.871 s
[INFO] BUILD SUCCESS
```

### Intermediate — test ว่า body ไม่ตรงก็จับได้

สถานการณ์: ต้องการพิสูจน์ว่าถ้า amount เป็น string แทน number, stub ไม่ match และ test ล้มเหลว

```java
// tested: WireMock 3.13.2, Java 17
@Test
void bodyMismatchCausesHttpError() {
    wireMock.stubFor(post(urlEqualTo("/payments"))
        .withRequestBody(equalToJson("""
            {"amount": 500, "currency": "THB"}
            """))
        .willReturn(okJson("{\"id\":\"pay_001\",\"status\":\"success\",\"amount\":500,\"currency\":\"THB\"}")));

    // ส่ง body ผิด — amount เป็น string "500" แทน number 500
    // ใช้ RestTemplate โดยตรงเพื่อส่ง raw string
    RestTemplate rt = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<>("{\"amount\":\"500\",\"currency\":\"THB\"}", headers);

    // WireMock จะตอบ 404 เพราะ body ไม่ match
    assertThatThrownBy(() ->
        rt.postForObject(
            "http://localhost:" + wireMock.port() + "/payments",
            entity,
            PaymentResponse.class))
        .isInstanceOf(org.springframework.web.client.HttpClientErrorException.class)
        .hasMessageContaining("404");
}
```

### Advanced — ใช้ ignoreExtraElements เพื่อ test เฉพาะ field สำคัญ

```java
// tested: WireMock 3.13.2, Java 17
@Test
void createPaymentWithIgnoreExtraElements() {
    // stub กำหนดแค่ field บังคับ — ไม่สน description
    wireMock.stubFor(post(urlEqualTo("/payments"))
        .withRequestBody(equalToJson("""
            {"amount": 1000, "currency": "THB"}
            """, true, true))
        .willReturn(okJson("""
            {"id":"pay_002","status":"success","amount":1000,"currency":"THB"}
            """)));

    // request ส่ง description มาด้วย — ยังได้ response ถูกต้อง
    PaymentResponse response = paymentClient.createPayment(
        new PaymentRequest(1000, "THB", "Subscription renewal"));

    assertThat(response.getId()).isEqualTo("pay_002");
    assertThat(response.getStatus()).isEqualTo("success");
}
```

---

## ส่วนที่ 5: Common Mistakes

**❌ ใช้ `equalTo()` แทน `equalToJson()` สำหรับ JSON body**

```java
// ❌ ผิด — เป็น string comparison ธรรมดา
.withRequestBody(equalTo("{\"amount\":500,\"currency\":\"THB\"}"))
```

```java
// ✅ ถูก — เป็น JSON semantic comparison
.withRequestBody(equalToJson("{\"amount\":500,\"currency\":\"THB\"}"))
```

เหตุผล: `equalTo()` ต้องตรง character-by-character รวม whitespace และลำดับ key ถ้า client ส่ง JSON โดยให้ library serialize อาจได้ key order ต่างจาก expected ทำให้ stub ไม่ match *(source: https://wiremock.org/docs/request-matching/)*

---

**❌ ลืม `withRequestBody()` แล้วสงสัยว่าทำไม test pass ทั้งที่ส่ง body ผิด**

```java
// ❌ ผิด — stub นี้ match ทุก POST ที่ /payments โดยไม่สนใจ body
wireMock.stubFor(post(urlEqualTo("/payments"))
    .willReturn(okJson("{\"id\":\"pay_001\",\"status\":\"success\",\"amount\":0,\"currency\":\"\"}")));
```

```java
// ✅ ถูก — เพิ่ม withRequestBody() เพื่อตรวจ body ด้วย
wireMock.stubFor(post(urlEqualTo("/payments"))
    .withRequestBody(equalToJson("{\"amount\":500,\"currency\":\"THB\"}"))
    .willReturn(okJson("{\"id\":\"pay_001\",\"status\":\"success\",\"amount\":0,\"currency\":\"\"}")));
```

เหตุผล: ถ้าไม่ระบุ `withRequestBody()`, WireMock จะ match request นั้นโดยไม่สนใจ body เลย ทำให้ test ผ่านทั้งที่ production code ส่ง body ผิด *(source: https://wiremock.org/docs/request-matching/)*

---

**❌ คาดว่า WireMock throw exception อธิบายชัดเจนเมื่อ body ไม่ match**

```java
// ❌ ความเข้าใจผิด — คิดว่า WireMock จะ throw exception บอกว่า body ไม่ match
// จริงๆ แล้ว WireMock ตอบ HTTP 404 ธรรมดา
```

```java
// ✅ ถูก — ต้องรู้ว่า 404 = ไม่พบ stub ที่ match
// ดู log ของ WireMock เพื่อดูว่า match ล้มเหลวตรงไหน
```

เหตุผล: WireMock ตอบ 404 เมื่อไม่มี stub match แทนที่จะ throw exception ดู WireMock server log เพื่อ debug ว่า body ต่างกันตรงไหน *(source: https://wiremock.org/docs/request-matching/)*

---

## ส่วนที่ 6: สรุปบท

บทนี้เราเรียนรู้ว่า POST request matching ต้องตรวจทั้ง URL และ request body ด้วย `withRequestBody(equalToJson())` ซึ่งทำ semantic JSON comparison ต่างจาก `equalTo()` ที่ตรง string เป๊ะ option `ignoreArrayOrder` และ `ignoreExtraElements` ช่วยให้ test ยืดหยุ่นขึ้นเมื่อ client อาจส่ง field เพิ่มหรือ array ลำดับต่าง เมื่อ body ไม่ match WireMock ตอบ 404

**คำถาม Retrieval — ลองตอบก่อนดูเฉลย:**

1. ถ้า stub กำหนด `equalToJson({"amount":500})` แต่ client ส่ง `{"amount":500,"note":"urgent"}` — จะเกิดอะไรขึ้น?
2. `ignoreExtraElements` ต่างจาก `ignoreArrayOrder` อย่างไร?
3. ทำไมต้องใช้ `equalToJson()` แทน `equalTo()` สำหรับ JSON body?

---

**เฉลย:**

1. stub ไม่ match — WireMock ตอบ 404 เพราะ default ของ `equalToJson()` ต้องการ body ตรงกันพอดี ถ้าต้องการให้ pass ต้องเพิ่ม `ignoreExtraElements=true`
2. `ignoreExtraElements` อนุญาตให้ request มี JSON field เพิ่มเติมที่ stub ไม่ได้ระบุ ส่วน `ignoreArrayOrder` อนุญาตให้ element ใน array อยู่ต่างลำดับ
3. `equalToJson()` ทำ semantic JSON comparison — key order และ whitespace ไม่สำคัญ ส่วน `equalTo()` ต้องตรง character-by-character ทำให้ fail ง่ายเมื่อ JSON serialization ให้ลำดับ key ต่าง
