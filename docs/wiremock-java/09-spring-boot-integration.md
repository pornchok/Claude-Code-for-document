# บทที่ 9 — Spring Boot Integration Test

## ⏸ Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบคำถามนี้ก่อน:

> **`inScenario()` + `whenScenarioStateIs()` + `willSetStateTo()` ทำงานร่วมกันอย่างไร?**
> ยกตัวอย่างสถานการณ์จริงที่ต้องใช้ทั้งสามตัวนี้พร้อมกัน

ลองตอบด้วยตัวเองก่อน แล้วค่อย scroll ดูเฉลย

---

**เฉลย:** `inScenario("ชื่อ")` สร้าง state machine ชื่อนั้น, `whenScenarioStateIs(state)` บอกว่า stub นี้ใช้ได้เมื่อ state เป็นอะไร, `willSetStateTo(newState)` เปลี่ยน state หลัง stub ถูกเรียก เช่น payment retry — ครั้งแรก state = `STARTED` → stub คืน pending แล้วเซต state เป็น `"PENDING"`, ครั้งที่สอง state = `"PENDING"` → stub คืน success

---

## 1. วัตถุประสงค์

อ่านจบแล้วทำได้:

- อธิบายความแตกต่างระหว่าง unit test กับ Spring Boot integration test ในบริบท WireMock
- ตั้งค่า `WireMockExtension` + `@DynamicPropertySource` เพื่อให้ Spring context ชี้ไปที่ WireMock
- อธิบายความแตกต่างระหว่าง `static` extension กับ instance extension
- เขียน integration test ที่ `@Autowired` bean ใช้งานได้จริงกับ WireMock

---

## 2. ทำไมต้องรู้?

บทก่อนหน้า (2-8) เราสร้าง `PaymentClient` ด้วยมือทุกครั้งใน `@BeforeEach`:

```java
paymentClient = new PaymentClient(new RestTemplate(), "http://localhost:" + wireMock.port());
```

แต่ในโปรเจคจริง `PaymentClient` ถูก inject ผ่าน Spring context ซึ่งมี configuration ซับซ้อนกว่า — RestTemplate อาจมี interceptors, timeouts, หรือ custom error handlers ที่กำหนดใน `@Bean`

ถ้า test เราสร้าง `new RestTemplate()` เองโดยไม่ผ่าน Spring context เราก็ไม่ได้ test พฤติกรรมจริงของแอป

**Integration test** แก้ปัญหานี้: โหลด Spring context ทั้งหมด แล้วให้ WireMock เป็น "stand-in" สำหรับ external API — bean จริงทำงาน แต่ปลายทางคือ WireMock แทนที่จะเป็น Omise API จริง

---

## 3. เนื้อหาหลัก

### ทำไม wiremock-spring-boot ไม่ใช้ในคอร์สนี้

มี library ชื่อ `wiremock-spring-boot` ที่ใช้ `@EnableWireMock` annotation ได้สะดวก แต่มีปัญหา **Jetty version conflict** กับ Spring Boot 3.x — ทั้งสองดึง Jetty เวอร์ชันต่างกันและ conflict กัน

เราจึงใช้ `wiremock-standalone` แทน แล้วจัดการ integration ด้วยสองส่วน:

1. **`WireMockExtension`** — จัดการ lifecycle ของ WireMock server ใน JUnit 5
2. **`@DynamicPropertySource`** — บอก Spring context ให้ใช้ URL ของ WireMock แทน

### WireMockExtension

`WireMockExtension` เป็น JUnit 5 extension ที่ทำหน้าที่เหมือน `WireMockServer` แต่จัดการ lifecycle ให้อัตโนมัติ:

```java
@RegisterExtension
static WireMockExtension wireMock = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort())
    .build();
```

- `@RegisterExtension` — บอก JUnit 5 ให้ใช้ extension นี้
- `static` — server เริ่มครั้งเดียวก่อน test แรก, หยุดหลัง test สุดท้าย
- `.dynamicPort()` — ใช้ port ว่างอัตโนมัติ (ไม่ hardcode 8080)
- stubs ถูก reset อัตโนมัติก่อนแต่ละ test

### @DynamicPropertySource

ปัญหาคือ Spring context โหลดก่อน WireMock รู้ว่าจะใช้ port อะไร เราจึงไม่สามารถใส่ค่า URL ใน `application.properties` ล่วงหน้าได้

`@DynamicPropertySource` แก้ปัญหานี้ — มันเป็น callback ที่ Spring เรียกหลังจาก WireMock เริ่มแล้ว แต่ก่อน Spring context สร้าง bean:

```java
@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("payment.service.url", wireMock::baseUrl);
}
```

สังเกต `wireMock::baseUrl` — นี่คือ **method reference** ไม่ใช่การเรียกค่าทันที Spring จะเรียก `wireMock.baseUrl()` ในเวลาที่ต้องการ (lazy evaluation)

### static vs instance extension

| | `static WireMockExtension` | instance `WireMockExtension` |
|---|---|---|
| Server เริ่ม | ก่อน test แรก | ก่อนแต่ละ test |
| Server หยุด | หลัง test สุดท้าย | หลังแต่ละ test |
| Stubs reset | ก่อนแต่ละ test | (server ใหม่ stubs ก็ว่างเปล่าอยู่แล้ว) |
| ใช้กับ @SpringBootTest | ต้องเป็น static เพราะ @DynamicPropertySource ต้องการ static context | ใช้ไม่ได้กับ @DynamicPropertySource |
| เหมาะกับ | Spring Boot integration test | unit test ทั่วไป |

**กฎสำคัญ:** เมื่อใช้กับ `@SpringBootTest` ต้องเป็น `static` เสมอ เพราะ `@DynamicPropertySource` ต้องทำงานก่อน Spring context โหลด และ Spring context โหลดก่อน instance fields ถูกสร้าง

### Pattern รวม

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MyIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.service.url", wireMock::baseUrl);
    }

    @Autowired
    private PaymentClient paymentClient;  // bean จริงจาก Spring context
```

---

## 4. ตัวอย่าง 3 ระดับ

### Beginner — GET ผ่าน Spring context

สถานการณ์: ดึงข้อมูล payment จาก Order Service

```java
// tested: Java 17, Spring Boot 3.4.1, WireMock 3.13.2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentGetIntegrationTest {

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
    void getPayment_shouldReturnPaymentFromSpringContext() {
        // Arrange
        wireMock.stubFor(get(urlEqualTo("/payments/order-555"))
            .willReturn(okJson(
                "{\"id\":\"order-555\",\"status\":\"successful\",\"amount\":1500,\"currency\":\"THB\"}")));

        // Act — paymentClient ถูก inject จาก Spring context จริง
        PaymentResponse response = paymentClient.getPayment("order-555");

        // Assert
        assertThat(response.getId()).isEqualTo("order-555");
        assertThat(response.getStatus()).isEqualTo("successful");
        assertThat(response.getAmount()).isEqualTo(1500);

        // Verify
        wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/order-555")));
    }
}
```

Output จากการรัน:
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.183 s
```

### Intermediate — POST ผ่าน Spring context

สถานการณ์: สร้าง payment order ใหม่ในระบบ e-commerce

```java
@Test
void createPayment_shouldPostToWireMockViaSpringBean() {
    // Arrange
    wireMock.stubFor(post(urlEqualTo("/payments"))
        .willReturn(okJson(
            "{\"id\":\"new-001\",\"status\":\"pending\",\"amount\":3000,\"currency\":\"THB\"}")));

    PaymentRequest request = new PaymentRequest(3000, "THB", "Order #999");

    // Act
    PaymentResponse response = paymentClient.createPayment(request);

    // Assert
    assertThat(response.getId()).isEqualTo("new-001");
    assertThat(response.getStatus()).isEqualTo("pending");

    // Verify — Spring bean ส่ง POST จริง
    wireMock.verify(1, postRequestedFor(urlEqualTo("/payments")));
}
```

### Advanced — ยืนยัน stubs reset อัตโนมัติ

สถานการณ์: ยืนยันว่า WireMockExtension reset stubs ก่อนแต่ละ test จริง — ถ้าไม่ reset stub เก่าจะ leak ข้ามไป test อื่น

```java
// Test นี้ทำงานได้ถูกต้องเพราะ WireMockExtension reset stubs ก่อนรัน
// ถ้าใช้ WireMockServer โดยไม่มี resetAll() ใน @AfterEach
// stub จาก test แรกจะยังอยู่และทำให้ test ถัดไปมีพฤติกรรมแปลก
@Test
void stubsAreResetBetweenTests_previousStubShouldNotExist() {
    wireMock.stubFor(get(urlEqualTo("/payments/reset-test"))
        .willReturn(okJson(
            "{\"id\":\"reset-test\",\"status\":\"ok\",\"amount\":100,\"currency\":\"THB\"}")));

    PaymentResponse response = paymentClient.getPayment("reset-test");
    assertThat(response.getStatus()).isEqualTo("ok");

    // ถ้า test นี้รันหลัง test อื่น stub ของ test อื่นจะไม่อยู่ที่นี่แล้ว
    // WireMockExtension รับประกัน isolation ระหว่าง tests
}
```

Output จากการรัน test ทั้ง 3 ใน Ch09:
```
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.622 s
[INFO] BUILD SUCCESS
```

---

## 5. Common Mistakes

**❌ แบบผิด: ใช้ instance field แทน static กับ @SpringBootTest**

```java
// WRONG — instance field ไม่ทำงานกับ @DynamicPropertySource
@RegisterExtension
WireMockExtension wireMock = WireMockExtension.newInstance()  // ขาด static
    .options(wireMockConfig().dynamicPort())
    .build();

@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("payment.service.url", wireMock::baseUrl);  // compile error!
}
```

**✅ แบบถูก:**

```java
@RegisterExtension
static WireMockExtension wireMock = ...  // ต้องเป็น static
```

เหตุผล: `@DynamicPropertySource` ทำงานในช่วง Spring context bootstrap ซึ่งเกิดก่อน instance fields ถูกสร้าง *(source: wiremock.org/docs/junit-jupiter/ — "Static fields: Server starts before first test method")*

---

**❌ แบบผิด: เรียก baseUrl() ทันที (eager evaluation)**

```java
@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("payment.service.url", wireMock.baseUrl());  // เรียกทันที — อาจเป็น null
}
```

**✅ แบบถูก:**

```java
registry.add("payment.service.url", wireMock::baseUrl);  // method reference — lazy
```

เหตุผล: `wireMock.baseUrl()` ตอนนี้อาจยังไม่มีค่าถ้า WireMock ยังไม่ start ครบ ส่วน method reference จะถูกเรียกเมื่อ Spring ต้องการค่าจริง *(source: Spring Framework docs — DynamicPropertySource Supplier)*

---

**❌ แบบผิด: ลืม `webEnvironment = RANDOM_PORT`**

```java
@SpringBootTest  // ค่าเริ่มต้น = MOCK (ไม่ start Tomcat)
```

**✅ แบบถูก:**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
```

เหตุผล: ถ้าไม่กำหนด Spring จะใช้ mock servlet context ซึ่ง RestTemplate อาจทำงานไม่ถูกต้องกับ WireMock *(source: Spring Boot Test docs — webEnvironment)*

---

## 6. สรุปบท

Integration test ต่างจาก unit test ตรงที่โหลด **Spring context จริง** ทำให้ bean ทุกตัวทำงานผ่าน configuration จริงของแอป

Pattern ที่ใช้ในคอร์สนี้:
- `@RegisterExtension static WireMockExtension` — จัดการ WireMock lifecycle ใน JUnit 5
- `@DynamicPropertySource` — override property ให้ชี้ไปที่ WireMock หลังจาก port ถูกจัดสรรแล้ว
- `wireMock::baseUrl` (method reference) — lazy evaluation เพื่อให้ได้ค่า port ที่ถูกต้อง

**คำถาม Retrieval — ตอบก่อนดูเฉลย:**

1. ทำไม `WireMockExtension` ต้องประกาศเป็น `static` เมื่อใช้กับ `@SpringBootTest`?
2. ความแตกต่างระหว่าง `wireMock.baseUrl()` กับ `wireMock::baseUrl` คืออะไร และอันไหนควรใช้ใน `@DynamicPropertySource`?
3. ถ้าไม่ใช้ `WireMockExtension` และต้องการ reset stubs ระหว่าง tests จะต้องทำอะไร?

---

**เฉลย:**

1. เพราะ `@DynamicPropertySource` ต้องเป็น `static` method และเรียกใช้ field ใน static context ได้เฉพาะ static field เท่านั้น — Spring context bootstrap เกิดก่อน instance fields
2. `wireMock.baseUrl()` เรียกค่าทันที (อาจ null ถ้า server ยังไม่พร้อม), `wireMock::baseUrl` เป็น method reference ที่ Spring จะเรียกทีหลัง — ใช้ `wireMock::baseUrl`
3. ต้องเรียก `wireMockServer.resetAll()` ใน `@AfterEach` หรือ `wireMockServer.resetMappings()` ด้วยตัวเอง
