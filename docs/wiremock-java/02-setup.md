# บทที่ 2 — ติดตั้งและตั้งค่า WireMock

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบคำถามต่อไปนี้จากที่คุณจำได้:

1. การเรียก Real API ในการทดสอบมีปัญหาอะไรบ้าง? ระบุอย่างน้อย 3 ข้อ
2. WireMock แก้ปัญหาเหล่านั้นอย่างไร?

*ตอบในใจก่อน แล้วค่อยอ่านต่อ*

---

**เฉลย:**

1. ปัญหาของ real API: ช้า (network latency), flaky (API อาจ down), มีค่าใช้จ่าย, จำลอง error scenario ไม่ได้, ทำ parallel test ได้ยาก
2. WireMock รัน HTTP server จำลองบน localhost ตอบกลับตามที่คุณกำหนด — เร็ว, เสถียร, ฟรี, ควบคุมได้ทุก scenario

---

## 1. วัตถุประสงค์

หลังจากอ่านบทนี้จบ คุณจะสามารถ:

- **เพิ่ม** WireMock dependency ที่ถูกต้องใน `pom.xml` ได้
- **สร้าง** `WireMockServer` และเรียก `start()` / `stop()` ใน lifecycle ที่ถูกต้อง
- **อธิบาย** ได้ว่าทำไม `dynamicPort()` ดีกว่าการ hardcode port
- **เชื่อม** `PaymentClient` ให้ใช้ WireMock URL แทน real API ใน test ได้

---

## 2. ทำไมต้องรู้? (Why)

ก่อนจะเขียน test ได้ คุณต้องเข้าใจ **lifecycle** ของ WireMock server ก่อน ถ้า setup ผิด test จะ fail แบบแปลกๆ เช่น:

- `ConnectionRefused` — ลืม `start()` server
- port conflict — หลาย test run พร้อมกันแล้วชน port
- stub ค้างข้ามไป test อื่น — ลืม `stop()` หรือ `reset()`

[ดู animation: Stub Lifecycle](animations/02-stub-lifecycle.html)

---

## 3. เนื้อหาหลัก

### Maven Dependency

WireMock 3.x เปลี่ยน groupId จาก `com.github.tomakehurst` (เวอร์ชัน 2.x เก่า) มาเป็น `org.wiremock` ในเวอร์ชัน 3.x

```xml
<!-- pom.xml -->
<properties>
    <java.version>17</java.version>
    <wiremock.version>3.13.2</wiremock.version>
</properties>

<dependencies>
    <!-- Spring Boot Test (JUnit 5 + AssertJ) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- WireMock 3.x — ใช้ standalone เพื่อหลีกเลี่ยง Jetty version conflict -->
    <dependency>
        <groupId>org.wiremock</groupId>
        <artifactId>wiremock-standalone</artifactId>
        <version>${wiremock.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

> **สำคัญมาก**: ใช้ `wiremock-standalone` ไม่ใช่ `wiremock` plain artifact เพราะ Spring Boot 3.x มา bundled กับ Jetty บางเวอร์ชันที่ conflict กับ WireMock ได้ `wiremock-standalone` แก้ปัญหานี้โดย shading dependencies ทั้งหมดเข้าไปใน jar เดียว

> **ข้อผิดพลาดที่เจอบ่อย**: `com.github.tomakehurst:wiremock` คือ groupId ของ **WireMock 2.x** (legacy) สำหรับ WireMock 3.x ใช้ `org.wiremock:wiremock-standalone`

### WireMockServer Lifecycle

```
@BeforeEach              @AfterEach
    │                        │
    ▼                        ▼
wireMock.start()     wireMock.stop()
    │
    ▼
 [test body]
 stubFor(...)
 assertThat(...)
```

rule หลัก: **start ก่อน test, stop หลัง test เสมอ**

### dynamicPort() vs Hardcoded Port

**แบบ hardcoded (หลีกเลี่ยง):**

```java
// ❌ อย่าทำแบบนี้
wireMock = new WireMockServer(8089);
```

ปัญหา: ถ้า port 8089 ถูก process อื่นใช้อยู่ (หรือ test อื่นรันพร้อมกัน) จะได้ `BindException: Address already in use`

**แบบ dynamicPort (แนะนำ):**

```java
// ✅ ทำแบบนี้
wireMock = new WireMockServer(wireMockConfig().dynamicPort());
wireMock.start();
// จากนั้น wireMock.port() จะบอก port ที่ OS จัดสรรให้
```

`dynamicPort()` ให้ OS เลือก port ว่างให้อัตโนมัติ ทำให้รัน test แบบ parallel ได้อย่างปลอดภัย

### การ Inject Port ให้ Client รู้จัก WireMock

เมื่อได้ port แบบ dynamic คุณต้องบอก client ว่า WireMock อยู่ที่ไหน pattern ที่ใช้บ่อยคือ:

```java
@BeforeEach
void setUp() {
    wireMock = new WireMockServer(wireMockConfig().dynamicPort());
    wireMock.start();

    // สร้าง client โดยส่ง base URL ของ WireMock
    String wireMockBaseUrl = "http://localhost:" + wireMock.port();
    paymentClient = new PaymentClient(new RestTemplate(), wireMockBaseUrl);
}
```

---

## 4. ตัวอย่าง 3 ระดับ

### Beginner — ตั้งค่า WireMockServer เปล่าๆ และทดสอบว่า start ได้

```java
// tested: Java 17, WireMock 3.13.2
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.web.client.RestTemplate;

class Ch02SetupTest {
    private WireMockServer wireMock;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        restTemplate = new RestTemplate();
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void wireMockServerStartsSuccessfully() {
        // กำหนด stub: GET /health → ตอบกลับ "OK"
        wireMock.stubFor(get("/health").willReturn(ok("OK")));

        // เรียก WireMock ด้วย RestTemplate
        String result = restTemplate.getForObject(
            "http://localhost:" + wireMock.port() + "/health",
            String.class
        );

        assertThat(result).isEqualTo("OK");
    }
}
```

**Output จากการรันจริง:**

```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0,
       Time elapsed: 0.749 s -- in com.example.wiremock.Ch02SetupTest
[INFO] BUILD SUCCESS
```

### Intermediate — เทียบ lifecycle pattern สองแบบในสถานการณ์จริง

สถานการณ์: ทีมมี test หลายตัว ต้องตัดสินใจว่าจะ start/stop WireMock แบบไหน — per-test หรือ per-class?

```java
// tested: Java 17, WireMock 3.13.2
// Pattern A: start/stop per test (@BeforeEach/@AfterEach) — แนะนำ
class PerTestLifecycleExample {
    private WireMockServer wireMock;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
    }

    @AfterEach
    void tearDown() {
        wireMock.stop(); // ทุก test ได้ server สะอาดใหม่
    }

    @Test void test1() { /* stub ใน test1 ไม่รั่วไปถึง test2 */ }
    @Test void test2() { /* stub ใน test2 ไม่รั่วมาจาก test1 */ }
}

// Pattern B: start/stop per class (@BeforeAll/@AfterAll) — ต้อง reset ด้วยตัวเอง
class PerClassLifecycleExample {
    private static WireMockServer wireMock;

    @BeforeAll
    static void startServer() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
    }

    @AfterAll
    static void stopServer() {
        wireMock.stop();
    }

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll(); // ⬅ จำเป็นมาก! ไม่งั้น stub ค้างข้าม test
    }

    @Test void test1() { /* ... */ }
    @Test void test2() { /* ... */ }
}
```

Pattern A ง่ายกว่า ใช้กับงานทั่วไป Pattern B เร็วกว่าเล็กน้อย (ไม่ต้อง start/stop server ซ้ำ) แต่ต้องระวังเรื่อง stub contamination

### Advanced — ใช้ Rule-based Pattern สำหรับ Test Suite ขนาดใหญ่

สถานการณ์: ทีมมี test class 30+ ตัว ทุกตัวต้องใช้ WireMock ต้องการ base class เพื่อลด boilerplate:

```java
// tested: Java 17, WireMock 3.13.2
// Base class ที่ทุก WireMock test extend ได้
abstract class WireMockIntegrationTest {

    protected WireMockServer wireMock;
    protected String wireMockBaseUrl;

    @BeforeEach
    void startWireMock() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        wireMockBaseUrl = "http://localhost:" + wireMock.port();
        configureFor("localhost", wireMock.port()); // global static configure
    }

    @AfterEach
    void stopWireMock() {
        wireMock.stop();
    }

    // Helper method สำหรับ stub ที่ใช้บ่อย
    protected void stubPaymentSuccess(String paymentId) {
        wireMock.stubFor(get("/payments/" + paymentId)
            .willReturn(okJson("""
                {"id":"%s","status":"success","amount":500}
                """.formatted(paymentId))));
    }

    protected void stubPaymentFailure(int statusCode, String errorCode) {
        wireMock.stubFor(post("/payments")
            .willReturn(aResponse()
                .withStatus(statusCode)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"error":"%s"}
                    """.formatted(errorCode))));
    }
}

// ใช้งาน — ไม่ต้องเขียน setup boilerplate ซ้ำ
class OrderServiceTest extends WireMockIntegrationTest {
    private OrderService orderService;

    @BeforeEach
    void setUpService() {
        orderService = new OrderService(wireMockBaseUrl);
    }

    @Test
    void createOrder_success() {
        stubPaymentSuccess("pay-001"); // ใช้ helper จาก base class
        OrderResult result = orderService.createOrder("pay-001");
        assertThat(result.isSuccess()).isTrue();
    }
}
```

---

## 5. Common Mistakes

**❌ ผิด: ลืม stop() WireMock server**

```java
@AfterEach
void tearDown() {
    // wireMock.stop(); ← ลืมบรรทัดนี้!
}
```

✅ **ถูก**: เรียก `wireMock.stop()` ใน `@AfterEach` เสมอ ถ้าลืม server ยังรันอยู่หลัง test จบ อาจทำให้ test suite ถัดไป fail เพราะ port ไม่ถูก release หรือสิ้นเปลือง resource *(source: wiremock.org/docs/getting-started — lifecycle management)*

---

**❌ ผิด: ใช้ Hardcoded port**

```java
// ❌ port ชนกันได้ง่ายมาก
wireMock = new WireMockServer(8089);
```

✅ **ถูก**: ใช้ `wireMockConfig().dynamicPort()` เสมอ แล้วดึง port จริงด้วย `wireMock.port()` หลัง start ทำให้ test parallel-safe *(source: wiremock.org/docs — dynamicPort() documentation)*

---

**❌ ผิด: ใช้ groupId เก่า (WireMock 2.x)**

```xml
<!-- ❌ นี่คือ WireMock 2.x legacy -->
<dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock-jre8</artifactId>
    <version>2.35.0</version>
</dependency>
```

✅ **ถูก**: WireMock 3.x ใช้ `org.wiremock` เป็น groupId *(source: wiremock.org/docs/download-and-installation — "Group ID: org.wiremock")*

---

## 6. สรุปบท

ลองตอบก่อนดูเฉลย:

**คำถามที่ 1**: ถ้าคุณเปลี่ยนจาก `dynamicPort()` เป็น `new WireMockServer(9000)` แล้วรัน test พร้อมกัน 2 threads จะเกิดอะไรขึ้น?

**คำถามที่ 2**: ทำไม `wireMock-standalone` จึงแก้ปัญหา Jetty conflict ได้ ในขณะที่ `wiremock` plain artifact ไม่แก้?

**คำถามที่ 3**: ถ้าคุณสร้าง `WireMockServer` ใน `@BeforeAll` แทน `@BeforeEach` มีอะไรที่ต้องทำเพิ่มใน `@BeforeEach` เพื่อให้ test แต่ละตัวไม่ contaminate กัน?

---

**เฉลย:**

---

**เฉลยคำถามที่ 1**: Thread แรกจะ bind port 9000 ได้สำเร็จ Thread ที่สองจะได้ `java.net.BindException: Address already in use: bind` เพราะ OS ไม่อนุญาตให้สอง process bind port เดิมพร้อมกัน test จะ fail ด้วย error ที่ดูแปลกๆ ไม่เกี่ยวกับ logic ที่ test

**เฉลยคำถามที่ 2**: `wiremock-standalone` ใช้ Maven Shade Plugin เพื่อ "shade" (relocate + bundle) dependencies ทั้งหมด รวม Jetty เข้าไปใน jar เดียว โดย rename package เป็น `wiremock.shaded.xxx` ดังนั้น Jetty ของ WireMock จึงไม่ clash กับ Jetty ที่ Spring Boot ใช้ ส่วน `wiremock` plain artifact depend on Jetty แบบปกติ ทำให้เกิด version conflict ได้

**เฉลยคำถามที่ 3**: ต้องเรียก `wireMock.resetAll()` (หรือ `wireMock.resetMappings()`) ใน `@BeforeEach` เพื่อลบ stub ทั้งหมดที่ test ก่อนหน้าสร้างไว้ ถ้าไม่ reset stub จาก test แรกจะยังคงอยู่ใน test ถัดไป ทำให้ผลลัพธ์ test เปลี่ยนไปตาม order ซึ่งเป็น antipattern ที่อันตรายมาก
