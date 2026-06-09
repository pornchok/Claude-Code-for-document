# บทที่ 10 — Error Simulation

## ⏸ Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบคำถามนี้ก่อน:

> **`@DynamicPropertySource` ใช้ทำอะไรใน WireMock + Spring Boot test?**
> ทำไมต้องใช้ method reference (`wireMock::baseUrl`) แทนการเรียกค่าโดยตรง (`wireMock.baseUrl()`)?

ลองตอบด้วยตัวเองก่อน แล้วค่อย scroll ดูเฉลย

---

**เฉลย:** `@DynamicPropertySource` ใช้ override Spring properties หลังจาก WireMock เริ่ม server แล้วแต่ก่อน Spring context สร้าง beans — ทำให้ `PaymentClient` ชี้ไปที่ WireMock แทน URL จริง ต้องใช้ method reference `wireMock::baseUrl` เพราะ Spring จะเรียก supplier นี้ทีหลังเมื่อต้องการค่าจริง (lazy) ถ้าเรียก `wireMock.baseUrl()` ทันทีค่าอาจยังไม่พร้อม

---

## 1. วัตถุประสงค์

อ่านจบแล้วทำได้:

- จำลอง HTTP error codes (404, 500) ด้วย `withStatus()`
- เพิ่ม delay ให้ response ด้วย `withFixedDelay()` และยืนยันผลด้วย timing assertion
- จำลอง network fault ด้วย `withFault(Fault.CONNECTION_RESET_BY_PEER)`
- อธิบายความแตกต่างระหว่าง HTTP error กับ network-level fault

---

## 2. ทำไมต้องรู้?

เวลา test ระบบจริง เราไม่ได้ test แค่ "happy path" — payment API ล่มได้ทุกเมื่อ เครือข่ายช้าได้, connection ถูกตัดได้, server คืน 500 ได้

ถ้าระบบเรารับมือกับสถานการณ์เหล่านี้ไม่ได้ ผู้ใช้จะเห็น error ที่ไม่ได้ตั้งใจ หรือแอปอาจ hang ค้างรอ response ที่ไม่มาทั้งวัน

WireMock ช่วยจำลองสถานการณ์เหล่านี้ได้อย่างแม่นยำ โดยไม่ต้องรอให้ API จริงล่มเอง

---

## 3. เนื้อหาหลัก

### HTTP Status Errors — withStatus()

วิธีง่ายที่สุดคือกำหนด status code ที่ต้องการ:

```java
aResponse()
    .withStatus(404)
    .withHeader("Content-Type", "application/json")
    .withBody("{\"error\":\"Payment not found\"}")
```

เมื่อ RestTemplate ได้รับ 4xx หรือ 5xx Spring จะ throw exception อัตโนมัติ:

| Status Range | Exception ที่ RestTemplate throw |
|---|---|
| 4xx (Client Error) | `HttpClientErrorException` |
| 5xx (Server Error) | `HttpServerErrorException` |
| Network failure | `ResourceAccessException` |

### Delay — withFixedDelay()

```java
aResponse()
    .withFixedDelay(2000)  // หน่วยเป็น milliseconds
    .withBody("...")
```

ใช้ทดสอบว่าระบบของเรารับมือกับ slow API ได้อย่างไร เช่น ควรมี timeout setting หรือไม่

นอกจาก fixed delay ยังมี:
- `withLogNormalRandomDelay(median, sigma)` — จำลอง latency แบบ distribution จริงตาม lognormal
- `withChunkedDribbleDelay(chunks, totalMs)` — ส่ง response เป็นชิ้นๆ ช้าๆ

### Network Faults — withFault()

Fault ต่างจาก HTTP error ตรงที่เกิดที่ **network level** ไม่ใช่ HTTP protocol — ไม่มี response กลับมาเลย:

```java
aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)
```

| Fault | พฤติกรรม |
|---|---|
| `CONNECTION_RESET_BY_PEER` | ตัด connection ทันที ไม่ส่ง response ("Close the connection, setting SO_LINGER to 0") |
| `EMPTY_RESPONSE` | ส่ง response ว่างเปล่า ไม่มีอะไรเลย |
| `MALFORMED_RESPONSE_CHUNK` | ส่ง status header ก่อน แล้วส่ง garbage แล้วตัด connection |
| `RANDOM_DATA_THEN_CLOSE` | ส่ง garbage data แล้วปิด connection |

*(source: wiremock.org/docs/simulating-faults/)*

เมื่อ fault เกิดขึ้น RestTemplate จะ throw `ResourceAccessException` (ซึ่ง wraps IOException)

### ความแตกต่างระหว่าง withStatus(500) กับ withFault()

```
withStatus(500):
  Client → [request] → WireMock
  Client ← [HTTP/1.1 500 Internal Server Error] ← WireMock
  RestTemplate throw: HttpServerErrorException

withFault(CONNECTION_RESET_BY_PEER):
  Client → [request] → WireMock
  Client ← [TCP RST] ← WireMock  (ไม่มี HTTP response เลย)
  RestTemplate throw: ResourceAccessException
```

---

## 4. ตัวอย่าง 3 ระดับ

### Beginner — 404 Not Found

สถานการณ์: Order Service พยายามดึง payment ที่ไม่มีอยู่

```java
// tested: Java 17, Spring Boot 3.4.1, WireMock 3.13.2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentErrorTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.service.url", wireMock::baseUrl);
    }

    @Autowired
    private PaymentClient paymentClient;

    @Test
    void getPayment_404_shouldThrowHttpClientErrorException() {
        // Arrange — Omise API คืน 404 สำหรับ payment ที่ไม่มีอยู่
        wireMock.stubFor(get(urlEqualTo("/payments/not-found"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Payment not found\"}")));

        // Act & Assert
        assertThatThrownBy(() -> paymentClient.getPayment("not-found"))
            .isInstanceOf(HttpClientErrorException.class)
            .hasMessageContaining("404");

        wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/not-found")));
    }
}
```

Output จากการรัน:
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

### Intermediate — 500 Server Error

สถานการณ์: Omise payment gateway มีปัญหา server-side ระหว่าง Black Friday

```java
@Test
void createPayment_500_shouldThrowHttpServerErrorException() {
    // Arrange — payment gateway internal error
    wireMock.stubFor(post(urlEqualTo("/payments"))
        .willReturn(aResponse()
            .withStatus(500)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"error\":\"Internal Server Error\"}")));

    PaymentRequest request = new PaymentRequest(5000, "THB", "Flash Sale Order");

    // Act & Assert
    assertThatThrownBy(() -> paymentClient.createPayment(request))
        .isInstanceOf(HttpServerErrorException.class)
        .hasMessageContaining("500");

    wireMock.verify(1, postRequestedFor(urlEqualTo("/payments")));
}
```

### Advanced — Delay + Fault รวมกัน

สถานการณ์: ทดสอบทั้ง slow response และ connection failure เพื่อให้มั่นใจว่าระบบรองรับทั้งสองกรณี

```java
@Test
void getPayment_withFixedDelay_shouldStillReturnResponse() {
    // WireMock delay 1000ms — ยืนยันว่า response ยังถูกต้อง และใช้เวลา >= 1000ms
    wireMock.stubFor(get(urlEqualTo("/payments/slow-pay"))
        .willReturn(okJson(
            "{\"id\":\"slow-pay\",\"status\":\"successful\",\"amount\":200,\"currency\":\"THB\"}")
            .withFixedDelay(1000)));

    long start = System.currentTimeMillis();
    PaymentResponse response = paymentClient.getPayment("slow-pay");
    long elapsed = System.currentTimeMillis() - start;

    // Response ต้องถูกต้อง
    assertThat(response.getId()).isEqualTo("slow-pay");

    // ต้องใช้เวลาอย่างน้อย 1000ms จริง
    assertThat(elapsed).isGreaterThanOrEqualTo(1000L);

    wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/slow-pay")));
}

@Test
void getPayment_connectionReset_shouldThrowResourceAccessException() {
    // Fault.CONNECTION_RESET_BY_PEER — ตัด TCP connection ทันที
    wireMock.stubFor(get(urlEqualTo("/payments/fault-test"))
        .willReturn(aResponse()
            .withFault(Fault.CONNECTION_RESET_BY_PEER)));

    // ResourceAccessException wraps IOException จาก network layer
    assertThatThrownBy(() -> paymentClient.getPayment("fault-test"))
        .isInstanceOf(ResourceAccessException.class);
}
```

Output จากการรัน Ch10 ทั้งหมด:
```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.298 s
[INFO] BUILD SUCCESS
```

---

## 5. Common Mistakes

**❌ แบบผิด: คาดว่า fault จะ throw HttpServerErrorException**

```java
// WRONG — Fault.CONNECTION_RESET_BY_PEER ไม่ส่ง HTTP response
assertThatThrownBy(() -> paymentClient.getPayment("fault"))
    .isInstanceOf(HttpServerErrorException.class);  // test จะ fail!
```

**✅ แบบถูก:**

```java
assertThatThrownBy(() -> paymentClient.getPayment("fault"))
    .isInstanceOf(ResourceAccessException.class);  // network-level error
```

เหตุผล: `Fault.CONNECTION_RESET_BY_PEER` ตัด TCP connection ก่อนส่ง HTTP response ดังนั้น RestTemplate ไม่ได้รับ HTTP status code เลย *(source: wiremock.org/docs/simulating-faults/ — "CONNECTION_RESET_BY_PEER: Close the connection, setting SO_LINGER to 0")*

---

**❌ แบบผิด: ทดสอบ timing ใน CI โดยไม่มี tolerance**

```java
// WRONG — ใน CI อาจช้ากว่า local ทำให้ flaky
assertThat(elapsed).isEqualTo(2000L);  // exact match — flaky!
```

**✅ แบบถูก:**

```java
// ยืนยันแค่ว่าใช้เวลาอย่างน้อย delay ที่ตั้งไว้
assertThat(elapsed).isGreaterThanOrEqualTo(1000L);
```

เหตุผล: Thread scheduling และ JVM overhead ทำให้ elapsed time ไม่ตรงกับ delay ที่ตั้งพอดี *(source: general testing best practice)*

---

**❌ แบบผิด: ลืม import Fault class**

```java
// WRONG — import ผิด package
import com.github.tomakehurst.wiremock.Fault;  // ไม่มี class นี้
```

**✅ แบบถูก:**

```java
import com.github.tomakehurst.wiremock.http.Fault;  // อยู่ใน .http package
```

เหตุผล: `Fault` enum อยู่ใน package `com.github.tomakehurst.wiremock.http` *(source: WireMock 3.13.2 javadoc)*

---

## 6. สรุปบท

WireMock ให้เราจำลอง error scenarios ได้ 3 ระดับ:
1. **HTTP errors** (`withStatus(4xx/5xx)`) — API ตอบกลับมาแต่บอกว่ามีปัญหา → RestTemplate throw `HttpClientErrorException` / `HttpServerErrorException`
2. **Slow response** (`withFixedDelay(ms)`) — API ช้า แต่ยังตอบกลับ → timeout testing
3. **Network faults** (`withFault(Fault.X)`) — connection ระดับ TCP ล้มเหลว → RestTemplate throw `ResourceAccessException`

การทดสอบ error path เป็น **สิ่งจำเป็น** ไม่ใช่ optional — ระบบที่ดีต้อง fail gracefully

**คำถาม Retrieval — ตอบก่อนดูเฉลย:**

1. ความแตกต่างหลักระหว่าง `withStatus(500)` กับ `withFault(Fault.CONNECTION_RESET_BY_PEER)` คืออะไร?
2. RestTemplate throw exception ชนิดใดเมื่อได้รับ 404? และชนิดใดเมื่อเกิด connection reset?
3. ทำไมการ test timing assertion เช่น `assertThat(elapsed).isEqualTo(2000)` ถึงเป็น bad practice?

---

**เฉลย:**

1. `withStatus(500)` ยังส่ง HTTP response กลับมา (มี status code), `withFault(CONNECTION_RESET_BY_PEER)` ตัด TCP connection ก่อนส่ง HTTP response ใดๆ
2. 404 → `HttpClientErrorException`, connection reset → `ResourceAccessException`
3. เพราะ JVM warm-up, thread scheduling, CI machine speed ทำให้ elapsed time ไม่ตรงพอดีกับ delay ที่ตั้ง — ควรใช้ `isGreaterThanOrEqualTo()` แทน
