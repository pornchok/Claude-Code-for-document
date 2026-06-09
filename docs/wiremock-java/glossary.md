# WireMock Java — Glossary

คำอธิบายศัพท์เทคนิคทุกคำในคอร์สนี้ พร้อม SOURCE URL ที่ verify แล้ว

---

## WireMock

**คำอธิบาย:** WireMock คือ open-source tool สำหรับทดสอบ API โดยการจำลอง (mock) HTTP service แทนที่จะเรียก service จริง ทำให้สามารถทดสอบได้อย่างรวดเร็ว ควบคุมได้ และไม่ขึ้นกับ external dependency ปัจจุบันมีผู้ใช้งานมากกว่า 5 ล้าน download ต่อเดือน

**ตัวอย่างการใช้:**
```java
WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());
wireMock.start();
// จากนี้ไป HTTP calls ไปยัง wireMock.baseUrl() จะถูก intercept โดย WireMock
wireMock.stop();
```

*(SOURCE: https://wiremock.org/docs/overview/)*

---

## Stub / Mock / Spy (ความแตกต่าง)

**คำอธิบาย:**
- **Stub** — กำหนด response สำเร็จรูปล่วงหน้า ใช้เพื่อให้ system under test ทำงานได้โดยไม่ต้องพึ่ง service จริง WireMock เป็น stub server
- **Mock** — เหมือน stub แต่เพิ่มการ verify ได้ว่า method/request ถูกเรียกกี่ครั้งและด้วย argument อะไร WireMock รองรับทั้ง stubbing และ verifying จึงทำหน้าที่เป็น mock ได้ด้วย
- **Spy** — wrap ของจริง โดย delegate การทำงานไปยัง real object แต่สามารถ intercept และ verify ได้ WireMock ไม่ใช่ spy เพราะไม่ forward request ไปยัง real server (เว้นแต่ configure proxy mode)

**ตัวอย่างการใช้:**
```java
// Stub — กำหนด response
wireMock.stubFor(get("/payments/123").willReturn(okJson("{}")));

// Mock (stub + verify)
wireMock.stubFor(post("/payments").willReturn(aResponse().withStatus(201)));
// ... code under test ...
wireMock.verify(1, postRequestedFor(urlEqualTo("/payments"))); // verify call count
```

*(SOURCE: https://wiremock.org/docs/stubbing/ — "A core feature of WireMock API mocking is the ability to return canned HTTP responses for requests matching criteria.")*

---

## Request Matching

**คำอธิบาย:** กลไกที่ WireMock ใช้เปรียบเทียบ incoming HTTP request กับ stub rules ที่ลงทะเบียนไว้ สามารถ match ได้บน URL, HTTP method, headers, query parameters, และ request body รองรับทั้ง exact matching และ pattern-based matching

**ตัวอย่างการใช้:**
```java
wireMock.stubFor(post(urlEqualTo("/payments"))
    .withHeader("Authorization", matching("Bearer .+"))
    .withRequestBody(equalToJson("""
        {"amount": 500, "currency": "THB"}
        """, true, true))
    .willReturn(okJson("{\"id\":\"pay_001\"}")));
```

*(SOURCE: https://wiremock.org/docs/request-matching/ — "WireMock enables flexible definition of a mock API by supporting rich matching of incoming requests")*

---

## Response Templating

**คำอธิบาย:** ฟีเจอร์ที่ทำให้ response body สามารถ dynamic ได้ โดยใช้ Handlebars template syntax ดึงข้อมูลจาก request (เช่น URL segment, query parameter, request body field) มาใส่ใน response โดยตรง เหมาะสำหรับจำลอง API ที่ echo ข้อมูลกลับ

**ตัวอย่างการใช้:**
```java
wireMock.stubFor(get(urlPathMatching("/payments/([a-z0-9-]+)"))
    .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody("""
            {"id":"{{request.pathSegments.[1]}}","status":"success"}
            """)
        .withTransformers("response-template")));
```

*(SOURCE: https://wiremock.org/docs/response-templating/)*

---

## Scenario (Stateful Behaviour)

**คำอธิบาย:** Scenario คือ state machine ที่ WireMock ใช้จำลองพฤติกรรม stateful ข้ามหลาย request เช่น payment ที่เริ่มจาก `pending` แล้วเปลี่ยนเป็น `success` เมื่อ retry แต่ละ Scenario มี state เริ่มต้นเสมอคือ `Scenario.STARTED`

**ตัวอย่างการใช้:**
```java
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

wireMock.stubFor(get(urlEqualTo("/payments/retry-123"))
    .inScenario("payment-retry")
    .whenScenarioStateIs(STARTED)
    .willSetStateTo("SUCCESS")
    .willReturn(aResponse().withStatus(202)
        .withBody("{\"status\":\"pending\"}")));

wireMock.stubFor(get(urlEqualTo("/payments/retry-123"))
    .inScenario("payment-retry")
    .whenScenarioStateIs("SUCCESS")
    .willReturn(okJson("{\"status\":\"success\"}")));
```

*(SOURCE: https://wiremock.org/docs/stateful-behaviour/ — "A scenario is essentially a state machine whose states can be arbitrarily assigned. Its starting state is always Scenario.STARTED.")*

---

## Fault Simulation

**คำอธิบาย:** ความสามารถของ WireMock ในการจำลอง error conditions ที่ทดสอบได้ยากกับ API จริง ครอบคลุมทั้ง HTTP error status codes (4xx/5xx), network-level faults (connection reset, empty response), และ response delays เพื่อทดสอบ resilience ของ client code

**ตัวอย่างการใช้:**
```java
// HTTP error
wireMock.stubFor(get("/payments/x").willReturn(aResponse().withStatus(503)));

// Network fault
wireMock.stubFor(get("/payments/y")
    .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

// Fixed delay
wireMock.stubFor(get("/payments/z")
    .willReturn(okJson("{\"status\":\"ok\"}").withFixedDelay(2000)));
```

*(SOURCE: https://wiremock.org/docs/simulating-faults/ — "One of the main reasons it's beneficial to use web service fakes when testing is to inject faulty behaviour that might be difficult to get the real service to produce on demand.")*

---

## WireMockServer

**คำอธิบาย:** Java class หลักสำหรับสร้างและควบคุม WireMock server ในโค้ด ใช้ใน unit test ทั่วไปที่ไม่ต้องการ Spring context สามารถสร้างหลาย instance พร้อมกันได้ แต่ละ instance รับฟัง port ของตัวเอง

**ตัวอย่างการใช้:**
```java
WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());
wireMock.start();

wireMock.stubFor(get("/health").willReturn(okJson("{\"status\":\"UP\"}")));

// ... test code ...

wireMock.stop(); // ปิด server เมื่อจบ test
```

*(SOURCE: https://wiremock.org/docs/java-usage/)*

---

## WireMockExtension

**คำอธิบาย:** JUnit 5 Extension สำหรับรวม WireMock เข้ากับ test lifecycle โดยอัตโนมัติ เมื่อใช้ `@RegisterExtension` WireMock จะ start/stop และ reset stubs ให้อัตโนมัติก่อน/หลังแต่ละ test เหมาะสำหรับ integration test กับ Spring Boot

**ตัวอย่างการใช้:**
```java
@RegisterExtension
static WireMockExtension wireMock = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort())
    .build();

@Test
void myTest() {
    wireMock.stubFor(get("/api").willReturn(okJson("{}")));
    // wireMock จัดการ lifecycle ให้อัตโนมัติ
}
```

*(SOURCE: https://wiremock.org/docs/junit-jupiter/ — "Invoking the extension programmatically with @RegisterExtension allows you to run any number of WireMock instances")*

---

## @DynamicPropertySource

**คำอธิบาย:** Spring Test annotation ที่ใช้ inject dynamic configuration values เข้า Spring `ApplicationContext` ขณะที่ context กำลัง bootstrap เหมาะสำหรับบอก Spring Boot ให้ใช้ WireMock URL แทน URL ของ service จริง เนื่องจาก WireMock ใช้ random port ที่รู้ล่วงหน้าไม่ได้

**ตัวอย่างการใช้:**
```java
@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("payment.service.url", wireMock::baseUrl);
    // Spring จะ inject wireMock.baseUrl() เป็นค่าของ payment.service.url
}
```

*(SOURCE: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/test/context/DynamicPropertySource.html)*

---

## dynamicPort

**คำอธิบาย:** Configuration option ที่บอกให้ WireMock เลือก available TCP port โดยอัตโนมัติแทนที่จะใช้ port ที่กำหนดตายตัว ช่วยแก้ปัญหา port conflict เมื่อรัน test แบบ parallel หรือเมื่อ port ถูกใช้งานโดย process อื่น

**ตัวอย่างการใช้:**
```java
WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());
wireMock.start();

int port = wireMock.port(); // ดึง port ที่ถูกเลือก
String baseUrl = wireMock.baseUrl(); // เช่น "http://localhost:56432"
```

*(SOURCE: https://wiremock.org/docs/junit-jupiter/ — "wireMockConfig().dynamicPort().dynamicHttpsPort()")*

---

## urlEqualTo / urlPathEqualTo / urlMatching

**คำอธิบาย:** สามฟังก์ชันสำหรับ match URL ใน stub rules:
- **`urlEqualTo`** — exact match ทั้ง path และ query string เหมาะเมื่อต้องการ match แบบเฉพาะเจาะจง
- **`urlPathEqualTo`** — match เฉพาะ path ไม่สนใจ query parameters เหมาะเมื่อ query string อาจเปลี่ยนแปลง
- **`urlMatching`** — match ด้วย regex บน URL ทั้งหมด เหมาะเมื่อ path มี variable segment เช่น `/payments/[a-z0-9]+`

**ตัวอย่างการใช้:**
```java
// ต้องตรง /payments/123 เท่านั้น
wireMock.stubFor(get(urlEqualTo("/payments/123")).willReturn(okJson("{}")));

// match /payments/search?status=pending และ /payments/search?status=success
wireMock.stubFor(get(urlPathEqualTo("/payments/search")).willReturn(okJson("{}")));

// match /payments/pay_abc123, /payments/pay_xyz999 ฯลฯ
wireMock.stubFor(get(urlMatching("/payments/pay_[a-z0-9]+")).willReturn(okJson("{}")));
```

*(SOURCE: https://wiremock.org/docs/request-matching/ — "urlEqualTo: Performs exact matching on both path and query string together. urlPathEqualTo: Matches only the path portion of a URL, ignoring query parameters.")*

---

## equalToJson

**คำอธิบาย:** Body matcher ที่เปรียบเทียบ JSON แบบ semantic (ความหมาย) ไม่ใช่ string comparison ดังนั้น whitespace และลำดับของ fields ไม่มีผล รองรับ option `ignoreArrayOrder` และ `ignoreExtraElements` สำหรับ lenient matching

**ตัวอย่างการใช้:**
```java
// Strict: ต้องครบทุก field ตาม spec
.withRequestBody(equalToJson("""
    {"amount": 500, "currency": "THB", "description": "Order #1"}
    """))

// Lenient: ยอมรับ field เพิ่มเติมและ array order ต่างกันได้
.withRequestBody(equalToJson("""
    {"amount": 500, "currency": "THB"}
    """, true, true))
```

*(SOURCE: https://wiremock.org/docs/request-matching/ — "Performs semantic JSON comparison rather than string matching. Supports options like ignoreArrayOrder and ignoreExtraElements for flexible matching.")*

---

## withFixedDelay

**คำอธิบาย:** Response builder method ที่เพิ่ม delay (หน่วย millisecond) ก่อนที่ WireMock จะส่ง response กลับ ใช้จำลอง slow API หรือ timeout scenario เพื่อทดสอบว่า client จัดการ latency ได้ถูกต้อง

**ตัวอย่างการใช้:**
```java
// เพิ่ม delay 2 วินาที ก่อนส่ง response
wireMock.stubFor(get(urlEqualTo("/payments/slow"))
    .willReturn(
        okJson("{\"id\":\"slow-pay\",\"status\":\"success\"}")
            .withFixedDelay(2000)
    ));
```

*(SOURCE: https://wiremock.org/docs/simulating-faults/ — "A stub response can have a fixed delay attached to it, such that the response will not be returned until after the specified number of milliseconds")*

---

## stubFor / verify

**คำอธิบาย:**
- **`stubFor`** — method หลักสำหรับลงทะเบียน stub rule เข้าไปใน WireMock server กำหนดว่า request แบบไหนจะได้รับ response อะไร
- **`verify`** — method สำหรับ assert ว่ามี request ที่ตรงเงื่อนไขส่งเข้ามาในจำนวนที่ถูกต้อง ใช้หลังจาก code under test รันแล้ว อ่านข้อมูลจาก Request Journal

**ตัวอย่างการใช้:**
```java
// stubFor — กำหนด expected request + response
wireMock.stubFor(post(urlEqualTo("/payments"))
    .willReturn(aResponse().withStatus(201)));

// ... code under test ที่เรียก /payments ...

// verify — assert ว่าถูกเรียกครบตามที่คาด
wireMock.verify(1, postRequestedFor(urlEqualTo("/payments")));
wireMock.verify(0, getRequestedFor(urlEqualTo("/payments"))); // ไม่มี GET
```

*(SOURCE: https://wiremock.org/docs/verifying/ — "To check for a precise number of requests matching the criteria, use this form: verify(3, postRequestedFor(urlEqualTo('/three/times')));")*

---

## Request Journal

**คำอธิบาย:** บันทึกภายในของ WireMock ที่จัดเก็บทุก incoming request ที่ server ได้รับ ตั้งแต่ start จนถึง stop (หรือ reset) `verify(...)` ทำงานโดยการค้นหาใน Request Journal ถ้าไม่ reset ระหว่าง test request จาก test ก่อนหน้าจะยังอยู่ใน journal และอาจทำให้ verify ผิดพลาด

**ตัวอย่างการใช้:**
```java
// reset journal (ถ้า share WireMock instance ข้าม test)
wireMock.resetRequests(); // ล้างเฉพาะ journal
wireMock.resetAll();      // ล้างทั้ง stubs และ journal

// ดู requests ที่ผ่านมาทั้งหมด (ใช้ debug)
List<LoggedRequest> requests = wireMock.findAll(
    getRequestedFor(urlMatching("/payments/.*"))
);
```

*(SOURCE: https://wiremock.org/docs/verifying/ — "The WireMock server records all requests it receives in memory (at least until it is reset). This makes it possible to verify that a request matching a specific pattern was received.")*

---

## willReturn / aResponse

**คำอธิบาย:**
- **`willReturn`** — method ใน stub chain ที่รับ `ResponseDefinitionBuilder` และกำหนด response ที่จะส่งกลับเมื่อ request match
- **`aResponse()`** — factory method ที่สร้าง `ResponseDefinitionBuilder` ใช้เป็นจุดเริ่มต้นในการกำหนด status code, headers, body, delay, และ fault รองรับ method chaining
- **`okJson(...)`** — shortcut method เทียบเท่ากับ `aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(...)`

**ตัวอย่างการใช้:**
```java
// ใช้ aResponse() แบบ verbose
wireMock.stubFor(get("/payments/123")
    .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"id\":\"123\",\"status\":\"success\"}")));

// ใช้ okJson() แบบ shortcut
wireMock.stubFor(get("/payments/123")
    .willReturn(okJson("{\"id\":\"123\",\"status\":\"success\"}")));
```

*(SOURCE: https://wiremock.org/docs/stubbing/ — "stubFor(get('/json').willReturn(okJson('{ \"message\": \"Hello\" }')))" และ ".willReturn(aResponse() .withHeader('Content-Type', 'text/plain') .withBody('Hello world!'))")*
