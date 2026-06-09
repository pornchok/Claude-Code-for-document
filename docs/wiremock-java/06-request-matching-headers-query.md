# บทที่ 6 — Request Matching: Headers & Query Parameters

---

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบคำถามนี้จากบทที่ 5:

> **`equalToJson()` ต่างจาก `equalTo()` อย่างไร?**

ลองตอบในใจก่อน แล้วเลื่อนลงมาดูเฉลย

---

**เฉลย:** `equalToJson()` ทำ **semantic JSON comparison** — key order และ whitespace ไม่สำคัญ รวมถึงมี options `ignoreArrayOrder` และ `ignoreExtraElements` สำหรับความยืดหยุ่น ส่วน `equalTo()` เป็น **string comparison ธรรมดา** ต้องตรง character-by-character ดังนั้นถ้า JSON มี key อยู่คนละลำดับหรือ whitespace ต่าง `equalTo()` จะ fail แต่ `equalToJson()` จะ pass

---

## ส่วนที่ 1: วัตถุประสงค์

เมื่ออ่านบทนี้จบ คุณจะสามารถ:

- เขียน stub ที่ match request ตาม HTTP header ด้วย `withHeader()`
- เขียน stub ที่ match request ตาม query parameter ด้วย `withQueryParam()`
- อธิบายความแตกต่างระหว่าง `urlEqualTo()` และ `urlPathEqualTo()`
- สร้าง stub ที่รับ header `Content-Type: application/json` เท่านั้น

---

## ส่วนที่ 2: ทำไมต้องรู้? (Why)

HTTP API จริงมักมี **constraints** เพิ่มเติมนอกจาก URL เช่น:

- Omise Payment API ต้องการ `Content-Type: application/json` — ถ้าส่งผิดจะ reject
- API บางตัวต้องการ `Authorization: Bearer <token>` — ถ้าไม่มีจะ 401
- Endpoint รายการ payment รับ query param เช่น `?currency=THB&status=pending`

ถ้า WireMock stub ไม่ตรวจ header หรือ query param, test จะผ่านแม้ production code ลืมใส่ header สำคัญ ซึ่งทำให้ bug รั่วไปถึง integration environment

นอกจากนี้ยังมีเรื่อง `urlEqualTo()` vs `urlPathEqualTo()` ที่หลายคนสับสน — ใช้ผิดทำให้ test fail ทั้งที่ code ถูก หรือผ่านทั้งที่ query param ผิด

---

## ส่วนที่ 3: เนื้อหาหลัก

### withHeader() — ตรวจ HTTP header

```java
wireMock.stubFor(post(urlEqualTo("/payments"))
    .withHeader("Content-Type", equalTo("application/json"))
    .willReturn(okJson("{\"id\":\"pay_h01\",\"status\":\"success\"}")));
```

stub นี้ match เฉพาะ POST request ที่มี `Content-Type: application/json` เท่านั้น ถ้า client ส่งโดยไม่มี header นี้ WireMock จะตอบ 404

นอกจาก `equalTo()` ยังมี matcher อื่น:

| Matcher | ใช้กับ |
|---------|--------|
| `equalTo("value")` | ตรงกันพอดี |
| `containing("partial")` | มี substring นี้ |
| `matching("^Bearer .+$")` | regex pattern |
| `absent()` | ต้องไม่มี header นี้ |

### withQueryParam() — ตรวจ query parameter

```java
wireMock.stubFor(get(urlPathEqualTo("/payments"))
    .withQueryParam("currency", equalTo("THB"))
    .willReturn(okJson("{\"id\":\"pay_q01\",\"status\":\"success\"}")));
```

ใช้ `withQueryParam()` ร่วมกับ `urlPathEqualTo()` เพื่อตรวจ query param แยกจาก path

สามารถกำหนดหลาย param พร้อมกัน:

```java
wireMock.stubFor(get(urlPathEqualTo("/payments"))
    .withQueryParam("currency", equalTo("THB"))
    .withQueryParam("status", equalTo("pending"))
    .willReturn(okJson("...")));
```

### urlEqualTo vs urlPathEqualTo — ความแตกต่างที่สำคัญมาก

นี่คือจุดที่หลายคนสับสน ดูตารางนี้:

| Method | Match ส่วนไหนของ URL | ตัวอย่าง URL ที่ match |
|--------|---------------------|----------------------|
| `urlEqualTo("/payments?currency=THB")` | ทั้ง path **และ** query string | `/payments?currency=THB` เท่านั้น |
| `urlPathEqualTo("/payments")` | path เท่านั้น (ไม่สน query) | `/payments`, `/payments?currency=THB`, `/payments?x=1&y=2` |

**ปัญหาที่เกิดบ่อย:**

```java
// ❌ ใช้ urlEqualTo แต่ต้องการตรวจ query param แยก
wireMock.stubFor(get(urlEqualTo("/payments"))   // match แค่ /payments ไม่มี query string
    .withQueryParam("currency", equalTo("THB"))  // ← เงื่อนไขนี้ไม่มีโอกาส check เลย
    .willReturn(okJson("...")));

// client เรียก /payments?currency=THB → WireMock ตอบ 404 เพราะ urlEqualTo ไม่ match
```

```java
// ✅ ถูกต้อง — ใช้ urlPathEqualTo แล้วตรวจ query param ด้วย withQueryParam
wireMock.stubFor(get(urlPathEqualTo("/payments"))
    .withQueryParam("currency", equalTo("THB"))
    .willReturn(okJson("...")));
```

**กฎง่ายๆ:** ถ้าต้องการตรวจ query param ด้วย `withQueryParam()` ให้ใช้ `urlPathEqualTo()` เสมอ

[ดู animation: URL Matching](animations/03-url-matching.html)

---

## ส่วนที่ 4: ตัวอย่าง 3 ระดับ

### Beginner — match ด้วย Content-Type header

```java
// tested: WireMock 3.13.2, Java 17
@Test
void matchByContentTypeHeader() {
    wireMock.stubFor(post(urlEqualTo("/payments"))
        .withHeader("Content-Type", equalTo("application/json"))
        .willReturn(okJson("{\"id\":\"pay_h01\",\"status\":\"success\",\"amount\":0,\"currency\":\"THB\"}")));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<>("{\"amount\":100}", headers);
    ResponseEntity<PaymentResponse> resp = restTemplate.postForEntity(
        "http://localhost:" + wireMock.port() + "/payments", entity, PaymentResponse.class);

    assertThat(resp.getStatusCode().value()).isEqualTo(200);
    assertThat(resp.getBody().getId()).isEqualTo("pay_h01");
}
```

### Intermediate — match ด้วย query parameter

```java
// tested: WireMock 3.13.2, Java 17
@Test
void matchByQueryParam() {
    wireMock.stubFor(get(urlPathEqualTo("/payments"))
        .withQueryParam("currency", equalTo("THB"))
        .willReturn(okJson("{\"id\":\"pay_q01\",\"status\":\"success\",\"amount\":0,\"currency\":\"THB\"}")));

    PaymentResponse resp = restTemplate.getForObject(
        "http://localhost:" + wireMock.port() + "/payments?currency=THB",
        PaymentResponse.class);

    assertThat(resp.getId()).isEqualTo("pay_q01");
}
```

Output เมื่อรัน `mvn test -Dtest=Ch06RequestMatchingHeadersTest`:
```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.033 s
[INFO] BUILD SUCCESS
```

### Advanced — ตรวจทั้ง header และ query param พร้อมกัน

สถานการณ์: API endpoint สำหรับดู payment ต้องการทั้ง Authorization header และ query param

```java
// tested: WireMock 3.13.2, Java 17
@Test
void matchByHeaderAndQueryParam() {
    wireMock.stubFor(get(urlPathEqualTo("/payments"))
        .withHeader("Authorization", equalTo("Bearer test-token-abc"))
        .withQueryParam("currency", equalTo("THB"))
        .withQueryParam("status", equalTo("pending"))
        .willReturn(okJson("""
            {"id":"pay_multi","status":"pending","amount":750,"currency":"THB"}
            """)));

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer test-token-abc");
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    ResponseEntity<PaymentResponse> resp = restTemplate.exchange(
        "http://localhost:" + wireMock.port() + "/payments?currency=THB&status=pending",
        HttpMethod.GET,
        entity,
        PaymentResponse.class);

    assertThat(resp.getBody().getId()).isEqualTo("pay_multi");
    // verify ว่าใส่ Authorization header จริง
    wireMock.verify(1, getRequestedFor(urlPathEqualTo("/payments"))
        .withHeader("Authorization", equalTo("Bearer test-token-abc")));
}
```

---

## ส่วนที่ 5: Common Mistakes

**❌ ใช้ `urlEqualTo()` กับ `withQueryParam()` พร้อมกัน**

```java
// ❌ ผิด — urlEqualTo ต้องการ URL ตรงทั้งหมด ไม่มี query string → match fail
wireMock.stubFor(get(urlEqualTo("/payments"))
    .withQueryParam("currency", equalTo("THB"))
    .willReturn(okJson("...")));
```

```java
// ✅ ถูก — urlPathEqualTo จับแค่ path แล้วให้ withQueryParam ตรวจ query
wireMock.stubFor(get(urlPathEqualTo("/payments"))
    .withQueryParam("currency", equalTo("THB"))
    .willReturn(okJson("...")));
```

เหตุผล: `urlEqualTo("/payments")` match เฉพาะ URL ที่ไม่มี query string เมื่อ client ส่ง `/payments?currency=THB` จะไม่ match *(source: https://wiremock.org/docs/request-matching/)*

---

**❌ ลืมว่า Spring RestTemplate ใส่ charset ต่อท้าย Content-Type**

```java
// ❌ อาจ fail เพราะ RestTemplate ส่ง Content-Type: application/json;charset=UTF-8
.withHeader("Content-Type", equalTo("application/json"))
```

```java
// ✅ ใช้ containing() เพื่อ partial match
.withHeader("Content-Type", containing("application/json"))
```

เหตุผล: RestTemplate บางเวอร์ชันเพิ่ม `;charset=UTF-8` ต่อท้าย Content-Type ทำให้ `equalTo("application/json")` fail ใช้ `containing()` เพื่อ match แบบ substring แทน *(source: https://wiremock.org/docs/request-matching/)*

---

**❌ สับสนว่า withQueryParam ตรวจ header หรือ query**

```java
// ❌ ผิด — withQueryParam ไม่ใช่สำหรับ header
.withQueryParam("Authorization", equalTo("Bearer token"))
```

```java
// ✅ ถูก — ใช้ withHeader สำหรับ HTTP header
.withHeader("Authorization", equalTo("Bearer token"))
```

เหตุผล: `withQueryParam()` ตรวจเฉพาะส่วน query string ใน URL (`?key=value`) ส่วน `withHeader()` ตรวจ HTTP request headers *(source: https://wiremock.org/docs/request-matching/)*

---

## ส่วนที่ 6: สรุปบท

บทนี้เรียนรู้ว่า stub ที่ดีควรตรวจทั้ง header และ query param นอกจาก URL `withHeader()` ใช้ matcher เช่น `equalTo()`, `containing()`, `matching()` ส่วน `withQueryParam()` ต้องใช้คู่กับ `urlPathEqualTo()` เสมอ ถ้าใช้ `urlEqualTo()` จะต้อง embed query string ใน URL โดยตรงแทน

**คำถาม Retrieval — ลองตอบก่อนดูเฉลย:**

1. ทำไมต้องใช้ `urlPathEqualTo()` แทน `urlEqualTo()` เมื่อใช้ `withQueryParam()`?
2. จะ match header ที่มี value เป็น regex ได้อย่างไร?
3. ถ้า client ส่ง `Content-Type: application/json;charset=UTF-8` แต่ stub กำหนด `equalTo("application/json")` จะเกิดอะไร?

---

**เฉลย:**

1. เพราะ `urlEqualTo("/payments")` match เฉพาะ URL ที่ไม่มี query string เลย ถ้า client ส่ง `/payments?currency=THB` URL จะไม่ match ทำให้ WireMock ตอบ 404 ก่อนที่ `withQueryParam()` จะมีโอกาส check
2. ใช้ `matching()` เช่น `.withHeader("Authorization", matching("^Bearer .+$"))` ซึ่งทำ regex match
3. stub จะไม่ match เพราะ `equalTo("application/json")` ต้องการ exact match แก้โดยใช้ `containing("application/json")` แทน
