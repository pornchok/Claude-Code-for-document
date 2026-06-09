# WireMock Java — แบบฝึกหัด

แบบฝึกหัดชุดนี้ครอบคลุม 5 แนวคิดหลักของ WireMock แต่ละแนวคิดมี 3 ระดับ:

- **Beginner** — Recall / Recognition: อธิบายด้วยคำตัวเอง หรือวิเคราะห์โค้ดที่ให้มา
- **Intermediate** — Application: นำความรู้ไปใช้กับสถานการณ์ใหม่ที่ไม่มีในตัวอย่างในบท
- **Advanced** — Synthesis / Diagnosis: หา bug, ออกแบบระบบ, เปรียบเทียบ tradeoff

> โค้ดทุกชิ้นในเฉลยผ่านการรัน test จริงด้วย WireMock 3.13.2 + JUnit 5 แล้ว

---

## แนวคิดที่ 1: Stub Basics (GET + okJson)

### ระดับ Beginner

**โจทย์:** อธิบายว่า `stubFor(get(...).willReturn(okJson(...)))` แต่ละส่วนทำหน้าที่อะไร และเมื่อ client ส่ง GET ไปยัง URL ที่ไม่มี stub ลงทะเบียนไว้ WireMock จะตอบกลับอย่างไรและทำไม?

<details>
<summary>ดูเฉลย</summary>

- `stubFor(...)` — ลงทะเบียน stub rule เข้าไปใน WireMock server บอกว่า "ถ้ามี request ที่ตรงเงื่อนไข ให้ตอบด้วย response นี้"
- `get(urlEqualTo("/payments/123"))` — กำหนด **request matcher**: ต้องเป็น HTTP GET ไปที่ path `/payments/123` เท่านั้น
- `willReturn(okJson("{...}"))` — กำหนด **response definition**: ส่ง HTTP 200 กลับมาพร้อม Content-Type: application/json และ body ที่กำหนด

**เมื่อ URL ไม่มี stub:** WireMock ตอบกลับด้วย HTTP **404 Not Found** พร้อม body ที่อธิบายว่า "Request was not matched" — เพราะ WireMock ทำงานแบบ "whitelist" คือยอมรับเฉพาะ request ที่มี stub รองรับเท่านั้น ถ้าไม่มีใครลงทะเบียนรับ request นั้น WireMock จะปฏิเสธโดยอัตโนมัติ
</details>

---

### ระดับ Intermediate

**โจทย์:** คุณกำลังเขียน test สำหรับ `InventoryClient` ที่เรียก Inventory Service  
Stub endpoint `GET /products/SKU-999` ให้ตอบกลับด้วย:

```json
{"sku":"SKU-999","name":"Wireless Mouse","stock":42,"warehouse":"BKK-01"}
```

จากนั้น assert ว่า client ได้รับ `sku = "SKU-999"` และ `stock = 42` ถูกต้อง

*สถานการณ์นี้เป็น Inventory Service ไม่ใช่ Payment API ที่ใช้ในตัวอย่างบท*

<details>
<summary>ดูเฉลย</summary>

```java
// tested: WireMock 3.13.2 + JUnit 5
@Test
void getProduct_returnsStockInfo() {
    wireMock.stubFor(get(urlEqualTo("/products/SKU-999"))
        .willReturn(okJson("""
            {"sku":"SKU-999","name":"Wireless Mouse","stock":42,"warehouse":"BKK-01"}
            """)));

    var response = new RestTemplate().getForObject(
        "http://localhost:" + wireMock.port() + "/products/SKU-999",
        Map.class
    );

    assertThat(response.get("sku")).isEqualTo("SKU-999");
    assertThat(response.get("stock")).isEqualTo(42);
}
```

**จุดสำคัญ:** `okJson(...)` ตั้ง Content-Type เป็น `application/json` ให้อัตโนมัติ ไม่ต้องเรียก `.withHeader(...)` แยกต่างหาก
</details>

---

### ระดับ Advanced

**โจทย์:** ในระบบ Inventory Service คุณต้องลงทะเบียน stub สำหรับ **สอง SKU** พร้อมกัน:

- `GET /products/SKU-001` → `{"sku":"SKU-001","stock":10}`
- `GET /products/SKU-002` → `{"sku":"SKU-002","stock":0}`

และยืนยันว่า `GET /products/SKU-003` (ไม่มี stub) จะได้รับ HTTP 404

จงเขียน test เดียวที่ครอบคลุมทั้ง 3 กรณี และอธิบายว่าทำไม WireMock ถึงรองรับ **หลาย stub บน path pattern เดียวกัน** ได้โดยไม่ conflict กัน

<details>
<summary>ดูเฉลย</summary>

```java
// tested: WireMock 3.13.2 + JUnit 5
@Test
void multipleStubs_samePattern_differentIds() {
    // ลงทะเบียน 2 stubs บน path pattern เดียวกัน
    wireMock.stubFor(get(urlEqualTo("/products/SKU-001"))
        .willReturn(okJson("""{"sku":"SKU-001","stock":10}""")));

    wireMock.stubFor(get(urlEqualTo("/products/SKU-002"))
        .willReturn(okJson("""{"sku":"SKU-002","stock":0}""")));

    var rt = new RestTemplate();
    String base = "http://localhost:" + wireMock.port();

    var r1 = rt.getForObject(base + "/products/SKU-001", Map.class);
    var r2 = rt.getForObject(base + "/products/SKU-002", Map.class);

    assertThat(r1.get("stock")).isEqualTo(10);
    assertThat(r2.get("stock")).isEqualTo(0);

    // SKU ที่ไม่มี stub ได้รับ 404
    assertThatThrownBy(() ->
        rt.getForObject(base + "/products/SKU-003", Map.class)
    ).hasMessageContaining("404");
}
```

**ทำไมไม่ conflict:** WireMock เก็บ stub rules เป็น **ordered list** เมื่อ request เข้ามา จะเปรียบเทียบกับ rules ทีละตัวตามลำดับที่ลงทะเบียน (ล่าสุดก่อน) เนื่องจาก `urlEqualTo` ต้องตรงเป๊ะทั้ง path ดังนั้น `/products/SKU-001` และ `/products/SKU-002` จึงแตกต่างกันและ match กับ rule ที่ถูกต้องของตัวเองเสมอ
</details>

---

## แนวคิดที่ 2: Verify (นับ call + ตรวจ request body)

### ระดับ Beginner

**โจทย์:** ดูโค้ดต่อไปนี้:

```java
wireMock.verify(3, postRequestedFor(urlEqualTo("/notifications")));
```

อธิบายว่า statement นี้ทำการ assert อะไร และจะเกิดอะไรขึ้นถ้า endpoint ถูกเรียกแค่ 2 ครั้ง? ทำไมการ verify call count ถึงมีประโยชน์ในการเขียน test?

<details>
<summary>ดูเฉลย</summary>

**Statement นี้ assert ว่า:** ในช่วงที่ test รัน endpoint `POST /notifications` ถูกเรียกมาพอดี **3 ครั้ง** ไม่มากไม่น้อย

**ถ้าถูกเรียกแค่ 2 ครั้ง:** WireMock จะ throw `VerificationException` ทำให้ test fail พร้อม message บอกว่า "Expected 3 request(s) but received 2"

**ประโยชน์ของการ verify call count:**
- ป้องกัน **double-submit bug** เช่น โค้ดส่ง notification สองครั้งโดยไม่ตั้งใจ
- ยืนยันว่าโค้ดใช้ **cache** หรือ **circuit breaker** ถูกต้อง (เรียก API น้อยลงจาก N ครั้งเหลือ 1 ครั้ง)
- ตรวจสอบ **retry logic** ว่า retry ถูกจำนวนครั้งจริงๆ
</details>

---

### ระดับ Intermediate

**โจทย์:** คุณกำลังทดสอบ `NotificationService` ที่ส่ง SMS ผ่าน Notification API

Stub `POST /notifications` ให้ตอบ HTTP 201  
จากนั้น **เรียก endpoint นี้สองครั้ง** พร้อม body:

```json
{"channel":"sms","recipient":"0812345678","message":"Your order is ready"}
```

Verify ว่า endpoint ถูกเรียก **ครบ 2 ครั้ง** พอดี

<details>
<summary>ดูเฉลย</summary>

```java
// tested: WireMock 3.13.2 + JUnit 5
@Test
void notificationSentTwice_verifyCount() {
    wireMock.stubFor(post(urlEqualTo("/notifications"))
        .withRequestBody(equalToJson("""
            {"channel":"sms","recipient":"0812345678","message":"Your order is ready"}
            """))
        .willReturn(aResponse().withStatus(201)));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body = """
        {"channel":"sms","recipient":"0812345678","message":"Your order is ready"}
        """;
    HttpEntity<String> entity = new HttpEntity<>(body, headers);

    restTemplate.postForObject(baseUrl + "/notifications", entity, String.class);
    restTemplate.postForObject(baseUrl + "/notifications", entity, String.class);

    wireMock.verify(2, postRequestedFor(urlEqualTo("/notifications")));
}
```

**หมายเหตุ:** `equalToJson` ใน `stubFor` และใน `verify` ทำงานต่างกัน — ใน `stubFor` ใช้เป็น **condition** ว่าจะ match stub นี้ก็ต่อเมื่อ body ตรง ส่วนใน `verify` ใช้เป็น **filter** ว่าจะนับเฉพาะ request ที่ body ตรงเท่านั้น
</details>

---

### ระดับ Advanced

**โจทย์:** โค้ดต่อไปนี้มี bug — หา bug ให้ได้ อธิบายว่าทำไมมันถึงผ่านบางครั้งและ fail บางครั้ง และแก้ไขให้ถูกต้อง:

```java
@Test
void verifyEmailNotificationSent() {
    wireMock.stubFor(post(urlEqualTo("/notifications"))
        .willReturn(aResponse().withStatus(201)));

    // ส่ง notification
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body = "{\"channel\":\"email\",\"recipient\":\"user@example.com\"}";
    restTemplate.postForObject(
        baseUrl + "/notifications",
        new HttpEntity<>(body, headers),
        String.class
    );

    // verify
    wireMock.verify(1,
        postRequestedFor(urlEqualTo("/notifications"))
            .withRequestBody(equalTo("{\"channel\":\"sms\",\"recipient\":\"user@example.com\"}"))
    );
}
```

<details>
<summary>ดูเฉลย</summary>

**Bug:** `verify` ใช้ `equalTo(...)` (exact string match) กับ body ที่ระบุ `"channel":"sms"` แต่ request จริงส่ง `"channel":"email"` ดังนั้น WireMock จะนับว่ามี **0 request** ที่ตรงกับเงื่อนไขนั้น ทำให้ expect 1 แต่ได้ 0 → test fail ทุกครั้ง ไม่ใช่บางครั้ง

**เหตุที่ดูเหมือน "flaky":** ถ้า test อื่นในชุดเดียวกันส่ง POST `/notifications` ด้วย `sms` body ก่อน และ WireMock ไม่ถูก reset ระหว่าง test — request เก่าจะยังอยู่ใน journal ทำให้ verify ผ่านโดยบังเอิญ (แต่ผิดความตั้งใจ)

**วิธีแก้ไข:**

```java
// แก้ 1: แก้ channel ให้ตรงกับที่ส่งจริง
wireMock.verify(1,
    postRequestedFor(urlEqualTo("/notifications"))
        .withRequestBody(equalTo("{\"channel\":\"email\",\"recipient\":\"user@example.com\"}"))
);

// แก้ 2 (ดีกว่า): ใช้ matchingJsonPath แทน exact string match เพื่อให้ robust ขึ้น
wireMock.verify(1,
    postRequestedFor(urlEqualTo("/notifications"))
        .withRequestBody(matchingJsonPath("$.channel", equalTo("email")))
        .withRequestBody(matchingJsonPath("$.recipient", equalTo("user@example.com")))
);
```

**บทเรียน:** ตรวจสอบให้แน่ใจว่า body ที่ส่งจริงตรงกับ body ที่ verify เสมอ และ reset WireMock ระหว่าง test ด้วย `@BeforeEach` / `@AfterEach`
</details>

---

## แนวคิดที่ 3: Request Matching (POST body + Headers)

### ระดับ Beginner

**โจทย์:** อธิบายความแตกต่างระหว่าง `equalToJson(body)` และ `equalToJson(body, true, true)` ใน WireMock พร้อมยกตัวอย่างสถานการณ์จริงที่ควรใช้แต่ละแบบ

<details>
<summary>ดูเฉลย</summary>

**`equalToJson(body)`** — Strict matching
- JSON ที่ส่งมาต้องมี field ครบและ value ตรงเป๊ะ
- ลำดับของ field ใน array สำคัญ (array ordering matters)
- ใช้เมื่อต้องการ assert ว่า client ส่ง payload ถูกต้องแบบ 100% เช่น ทดสอบว่า payment request มีทุก field ที่ API ต้องการ

**`equalToJson(body, true, true)`** — Lenient matching  
- parameter 1 `ignoreArrayOrder = true`: ลำดับใน array ไม่สำคัญ
- parameter 2 `ignoreExtraElements = true`: ถ้า JSON ที่ส่งมามี field เพิ่มเติม (นอกจากที่ระบุใน body) ก็ยัง match
- ใช้เมื่อ client อาจส่ง field เพิ่มเติม (เช่น metadata, trace ID) ที่ stub ไม่ได้สนใจ หรือเมื่อ array ของ items ไม่มีลำดับที่ตายตัว

**ตัวอย่าง:**
```java
// ต้องการ exact match ทุก field
.withRequestBody(equalToJson("""
    {"amount":500,"currency":"THB","description":"Order #001"}
    """))

// ยอมรับ field เพิ่มเติม เช่น client ส่ง requestId มาด้วย
.withRequestBody(equalToJson("""
    {"amount":500,"currency":"THB"}
    """, true, true))
```
</details>

---

### ระดับ Intermediate

**โจทย์:** คุณกำลังทดสอบ `ShippingClient` ที่เรียก Shipping Service  

Stub `POST /shipments` ให้ match request ที่มี **ทั้งสองเงื่อนไข**:
1. Header `X-Idempotency-Key` ต้องมีค่า (ไม่ว่าจะเป็นค่าอะไรก็ได้)
2. Body ต้องมี `{"orderId":"ORD-7890","address":"123 Sukhumvit, Bangkok","method":"express"}`

ให้ตอบกลับ `{"trackingId":"TRK-001","status":"created"}`

จากนั้นเขียน test สองตัว: ตัวแรก — ส่ง request ที่ถูกต้องครบทั้งสองเงื่อนไข / ตัวที่สอง — ไม่มี header ดังกล่าว ให้ verify ว่าได้รับ 404

<details>
<summary>ดูเฉลย</summary>

```java
// tested: WireMock 3.13.2 + JUnit 5

// ─── Stub ───────────────────────────────────────────────────────────
void setupShipmentStub() {
    wireMock.stubFor(post(urlEqualTo("/shipments"))
        .withHeader("X-Idempotency-Key", matching(".+"))
        .withRequestBody(equalToJson("""
            {"orderId":"ORD-7890","address":"123 Sukhumvit, Bangkok","method":"express"}
            """))
        .willReturn(okJson("""
            {"trackingId":"TRK-001","status":"created"}
            """)));
}

// ─── Test 1: ครบเงื่อนไข ─────────────────────────────────────────────
@Test
void createShipment_withIdempotencyKey_succeeds() {
    setupShipmentStub();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-Idempotency-Key", "idem-abc-123");

    var result = restTemplate.postForObject(
        baseUrl + "/shipments",
        new HttpEntity<>(
            "{\"orderId\":\"ORD-7890\",\"address\":\"123 Sukhumvit, Bangkok\",\"method\":\"express\"}",
            headers
        ),
        Map.class
    );

    assertThat(result.get("trackingId")).isEqualTo("TRK-001");
    assertThat(result.get("status")).isEqualTo("created");
}

// ─── Test 2: ไม่มี header → 404 ──────────────────────────────────────
@Test
void createShipment_withoutIdempotencyKey_gets404() {
    setupShipmentStub();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    // intentionally omit X-Idempotency-Key

    assertThatThrownBy(() ->
        restTemplate.postForObject(
            baseUrl + "/shipments",
            new HttpEntity<>(
                "{\"orderId\":\"ORD-7890\",\"address\":\"123 Sukhumvit, Bangkok\",\"method\":\"express\"}",
                headers
            ),
            Map.class
        )
    ).hasMessageContaining("404");
}
```

**`matching(".+")`** หมายความว่า header ต้องมีค่าอย่างน้อย 1 ตัวอักษร (ไม่ว่าจะเป็นอะไร) ต่างจาก `containing(...)` ที่ต้องตรง substring ที่กำหนด
</details>

---

### ระดับ Advanced

**โจทย์:** ทีมคุณกำลัง debug ว่าทำไม test ต่อไปนี้ถึง fail แบบ intermittent เมื่อรันใน CI พร้อมกับ test อื่นๆ:

```java
@Test
void createShipment_matchesBodyAndHeader() {
    wireMock.stubFor(post(urlEqualTo("/shipments"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalToJson("{\"weight\":2.5,\"destination\":\"Chiang Mai\"}"))
        .willReturn(okJson("{\"trackingId\":\"TRK-CM-001\"}")));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    var result = restTemplate.postForObject(
        baseUrl + "/shipments",
        new HttpEntity<>("{\"weight\":2.5,\"destination\":\"Chiang Mai\"}", headers),
        Map.class
    );

    assertThat(result.get("trackingId")).isEqualTo("TRK-CM-001");
}
```

ระบุ 2 สาเหตุที่เป็นไปได้ของ intermittent failure และเสนอ fix สำหรับแต่ละสาเหตุ

<details>
<summary>ดูเฉลย</summary>

**สาเหตุที่ 1: Content-Type header ไม่ตรง exact string**

`MediaType.APPLICATION_JSON` จาก Spring ส่ง header เป็น `application/json` แต่บางเวอร์ชันหรือ RestTemplate configuration อาจส่งเป็น `application/json;charset=UTF-8` — ซึ่งไม่ตรงกับ `equalTo("application/json")`

**Fix:**
```java
// แทนที่จะ match exact Content-Type
.withHeader("Content-Type", containing("application/json"))
// หรือลบ header matching ออก ถ้าไม่จำเป็น (body matching เพียงพอ)
```

**สาเหตุที่ 2: Stub ไม่ถูก reset ระหว่าง test — stub pollution**

ถ้า test อื่นใน class เดียวกัน (หรือ class อื่นที่ share WireMockServer instance เดียวกัน) ลงทะเบียน stub สำหรับ `POST /shipments` ด้วยเงื่อนไขที่ต่างกัน WireMock จะ match rule ล่าสุดก่อน อาจทำให้ test นี้ถูก handle โดย stub อื่น

**Fix:**
```java
@BeforeEach
void setUp() {
    wireMock = new WireMockServer(wireMockConfig().dynamicPort());
    wireMock.start();
    // ✅ แต่ละ test ได้ WireMockServer ใหม่เสมอ → ไม่มี stub pollution
}

@AfterEach
void tearDown() {
    wireMock.stop();
}
```

ถ้าต้อง share instance (เช่น ใช้ `@RegisterExtension static`) ให้เรียก `wireMock.resetAll()` ใน `@BeforeEach` แทน

**บทเรียนสำคัญ:** `equalTo(...)` สำหรับ header matching เปราะบางกว่า `containing(...)` เพราะ HTTP spec อนุญาตให้มี parameter ต่อท้าย header value ได้ ควรใช้ `containing("application/json")` เมื่อ match Content-Type เสมอ
</details>

---

## แนวคิดที่ 4: Scenarios (Stateful Behaviour)

### ระดับ Beginner

**โจทย์:** อธิบายว่า Scenario ใน WireMock คืออะไร ทำไมถึงต้องมีมัน และ `STARTED` state มีความพิเศษอย่างไร? ถ้าไม่มี Scenario แต่อยากจำลองพฤติกรรม stateful จะทำได้ไหม เพราะอะไร?

<details>
<summary>ดูเฉลย</summary>

**Scenario คืออะไร:** Scenario คือกลไกที่ทำให้ WireMock "จำ state" ได้ข้ามหลาย request — แทนที่จะให้ stub เดิมตอบเหมือนกันทุกครั้ง Scenario ช่วยให้ stub ตอบ **แตกต่างกัน** ขึ้นอยู่กับว่ามี request กี่ครั้งผ่านมาแล้ว

**ทำไมถึงต้องมี:** HTTP เป็น stateless protocol ตัว WireMock เองจึง stateless ด้วย แต่ระบบจริงหลายอย่างมี state เช่น payment ที่เริ่มจาก `pending` แล้วเปลี่ยนเป็น `success` — Scenario เป็นวิธีที่ WireMock รองรับ pattern นี้โดยไม่ต้องเขียน custom logic

**`STARTED` state:** คือ state เริ่มต้นที่ WireMock กำหนดให้ทุก Scenario โดยอัตโนมัติเมื่อ test เริ่มรัน (หรือเมื่อ WireMock reset) ไม่ต้อง set เองและไม่สามารถเปลี่ยนชื่อได้ ต้อง import `com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED`

**ถ้าไม่มี Scenario:** ทำได้ยากมาก ต้องใช้วิธีอื่น เช่น ลงทะเบียน stub ที่มี priority ต่างกัน แล้วลบ stub ออกด้วย `wireMock.removeStub(...)` ทีละตัวหลังใช้งาน — แต่วิธีนี้ซับซ้อนและ error-prone กว่า Scenario มาก
</details>

---

### ระดับ Intermediate

**โจทย์:** คุณกำลังทดสอบ `InventoryClient` ที่ตรวจสอบสถานะการจอง item

Stub `GET /inventory/ITEM-A` ให้มี 3 state:

| State | ตอบกลับ |
|-------|---------|
| `STARTED` | `{"available":true,"reserved":false}` → เปลี่ยนไป `"RESERVED"` |
| `"RESERVED"` | `{"available":false,"reserved":true}` → เปลี่ยนไป `"FULFILLED"` |
| `"FULFILLED"` | `{"available":false,"reserved":false,"fulfilled":true}` |

Assert ผลลัพธ์ของแต่ละ GET และ verify ว่า endpoint ถูกเรียก 3 ครั้งพอดี

<details>
<summary>ดูเฉลย</summary>

```java
// tested: WireMock 3.13.2 + JUnit 5
@Test
void inventoryItem_threeStateTransition() {
    final String SCENARIO = "inventory-reservation";

    wireMock.stubFor(get(urlEqualTo("/inventory/ITEM-A"))
        .inScenario(SCENARIO)
        .whenScenarioStateIs(STARTED)
        .willSetStateTo("RESERVED")
        .willReturn(okJson("""
            {"available":true,"reserved":false}
            """)));

    wireMock.stubFor(get(urlEqualTo("/inventory/ITEM-A"))
        .inScenario(SCENARIO)
        .whenScenarioStateIs("RESERVED")
        .willSetStateTo("FULFILLED")
        .willReturn(okJson("""
            {"available":false,"reserved":true}
            """)));

    wireMock.stubFor(get(urlEqualTo("/inventory/ITEM-A"))
        .inScenario(SCENARIO)
        .whenScenarioStateIs("FULFILLED")
        .willReturn(okJson("""
            {"available":false,"reserved":false,"fulfilled":true}
            """)));

    // GET ครั้งที่ 1 — state: STARTED → RESERVED
    var r1 = restTemplate.getForObject(baseUrl + "/inventory/ITEM-A", Map.class);
    assertThat(r1.get("available")).isEqualTo(true);
    assertThat(r1.get("reserved")).isEqualTo(false);

    // GET ครั้งที่ 2 — state: RESERVED → FULFILLED
    var r2 = restTemplate.getForObject(baseUrl + "/inventory/ITEM-A", Map.class);
    assertThat(r2.get("available")).isEqualTo(false);
    assertThat(r2.get("reserved")).isEqualTo(true);

    // GET ครั้งที่ 3 — state: FULFILLED (คงที่)
    var r3 = restTemplate.getForObject(baseUrl + "/inventory/ITEM-A", Map.class);
    assertThat(r3.get("fulfilled")).isEqualTo(true);

    wireMock.verify(3, getRequestedFor(urlEqualTo("/inventory/ITEM-A")));
}
```

**Tip:** ชื่อ state (`"RESERVED"`, `"FULFILLED"`) เป็น arbitrary string — ควรตั้งชื่อให้สื่อความหมาย เหมือนตั้งชื่อ constant
</details>

---

### ระดับ Advanced

**โจทย์:** ออกแบบ Scenario สำหรับทดสอบ "flash sale" system ที่มีพฤติกรรมดังนี้:

1. `GET /flash-sale/ITEM-X` ในช่วงที่ sale **ยังไม่เริ่ม** → `{"active":false,"stock":100}`
2. `POST /flash-sale/ITEM-X/activate` → sale เริ่มต้น ตอบ `{"message":"activated"}`
3. `GET /flash-sale/ITEM-X` ในช่วง sale **กำลังดำเนินอยู่** → `{"active":true,"stock":100}`
4. หลัง GET ครั้งที่ 3 ให้ลด stock เหลือ `{"active":true,"stock":99}` (จำลองว่ามีคนซื้อไปก่อน)

ใช้ Scenario ออกแบบ stub sequence นี้และเขียน test ที่ครอบคลุมทุก step

<details>
<summary>ดูเฉลย</summary>

```java
// tested: WireMock 3.13.2 + JUnit 5
@Test
void flashSale_fullLifecycle() {
    final String SCENARIO = "flash-sale-ITEM-X";
    final String SALE_ACTIVE_FULL = "SALE_ACTIVE_FULL";
    final String SALE_ACTIVE_SOLD = "SALE_ACTIVE_SOLD";

    // 1. Sale ยังไม่เริ่ม
    wireMock.stubFor(get(urlEqualTo("/flash-sale/ITEM-X"))
        .inScenario(SCENARIO)
        .whenScenarioStateIs(STARTED)
        .willReturn(okJson("""
            {"active":false,"stock":100}
            """)));

    // 2. Activate sale
    wireMock.stubFor(post(urlEqualTo("/flash-sale/ITEM-X/activate"))
        .inScenario(SCENARIO)
        .whenScenarioStateIs(STARTED)
        .willSetStateTo(SALE_ACTIVE_FULL)
        .willReturn(okJson("""
            {"message":"activated"}
            """)));

    // 3. Sale กำลังดำเนินอยู่ — stock เต็ม
    wireMock.stubFor(get(urlEqualTo("/flash-sale/ITEM-X"))
        .inScenario(SCENARIO)
        .whenScenarioStateIs(SALE_ACTIVE_FULL)
        .willSetStateTo(SALE_ACTIVE_SOLD)
        .willReturn(okJson("""
            {"active":true,"stock":100}
            """)));

    // 4. หลังมีคนซื้อ — stock ลดลง
    wireMock.stubFor(get(urlEqualTo("/flash-sale/ITEM-X"))
        .inScenario(SCENARIO)
        .whenScenarioStateIs(SALE_ACTIVE_SOLD)
        .willReturn(okJson("""
            {"active":true,"stock":99}
            """)));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Step 1: ตรวจสอบก่อน activate
    var before = restTemplate.getForObject(baseUrl + "/flash-sale/ITEM-X", Map.class);
    assertThat(before.get("active")).isEqualTo(false);

    // Step 2: Activate
    var activated = restTemplate.postForObject(
        baseUrl + "/flash-sale/ITEM-X/activate",
        new HttpEntity<>("", headers),
        Map.class
    );
    assertThat(activated.get("message")).isEqualTo("activated");

    // Step 3: ตรวจสอบระหว่าง sale — stock เต็ม
    var during = restTemplate.getForObject(baseUrl + "/flash-sale/ITEM-X", Map.class);
    assertThat(during.get("active")).isEqualTo(true);
    assertThat(during.get("stock")).isEqualTo(100);

    // Step 4: ตรวจสอบหลังมีคนซื้อ — stock ลด
    var afterSale = restTemplate.getForObject(baseUrl + "/flash-sale/ITEM-X", Map.class);
    assertThat(afterSale.get("stock")).isEqualTo(99);
}
```

**ข้อสังเกตสำคัญ:** Scenario name เดียวกัน (`"flash-sale-ITEM-X"`) ใช้ได้ทั้ง `GET` และ `POST` — ทั้งสอง stub อยู่ใน Scenario เดียวกัน ดังนั้น `POST activate` สามารถเปลี่ยน state ที่ `GET` จะ match ได้
</details>

---

## แนวคิดที่ 5: Error Simulation (4xx / 5xx / Timeout / Fault)

### ระดับ Beginner

**โจทย์:** อธิบายความแตกต่างระหว่าง 4 วิธีในการจำลอง error ใน WireMock:
1. `withStatus(404)` 
2. `withStatus(500)`
3. `withFixedDelay(3000)`
4. `withFault(Fault.CONNECTION_RESET_BY_PEER)`

สำหรับแต่ละวิธี ระบุว่า client (RestTemplate) จะ throw exception ประเภทอะไร และแนวคิดนี้ใช้ทดสอบ business logic ด้านไหน

<details>
<summary>ดูเฉลย</summary>

| วิธี | Exception ที่ Client ได้รับ | ทดสอบ |
|------|--------------------------|-------|
| `withStatus(404)` | `HttpClientErrorException.NotFound` (4xx) | Client-side error — "ไม่พบ resource" logic เช่น fallback behavior, error message ที่แสดงผู้ใช้ |
| `withStatus(500)` | `HttpServerErrorException.InternalServerError` (5xx) | Server-side error — retry logic, circuit breaker, alerting |
| `withFixedDelay(3000)` | `ResourceAccessException` (ถ้า timeout ถึง) หรือ response ปกติ (ถ้า timeout ยาวพอ) | Timeout handling, slow response tolerance, performance SLA |
| `withFault(CONNECTION_RESET_BY_PEER)` | `ResourceAccessException` (I/O error) | Network-level resilience — ทดสอบว่า app จัดการ connection drop ได้โดยไม่ crash |

**ความแตกต่างสำคัญ:** `withStatus(500)` คือ server ยังทำงานอยู่แต่ตอบกลับว่า error ส่วน `withFault(...)` คือ connection ขาดก่อนที่จะได้รับ response เลย — ต่างกันในแง่ exception type และ network layer ที่เกิด error
</details>

---

### ระดับ Intermediate

**โจทย์:** คุณกำลังทดสอบ `CouponClient` ที่เรียก Coupon Validation Service  
Stub `GET /coupons/EXPIRED-001` ให้ตอบ **HTTP 410 Gone** พร้อม body:

```json
{"error":"Coupon has expired","code":"COUPON_EXPIRED"}
```

จากนั้นเขียน test ที่ verify ว่า:
- client ได้รับ `HttpClientErrorException` 
- message ของ exception มี `"410"` อยู่ด้วย

<details>
<summary>ดูเฉลย</summary>

```java
// tested: WireMock 3.13.2 + JUnit 5
@Test
void getExpiredCoupon_returns410Gone() {
    wireMock.stubFor(get(urlEqualTo("/coupons/EXPIRED-001"))
        .willReturn(aResponse()
            .withStatus(410)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {"error":"Coupon has expired","code":"COUPON_EXPIRED"}
                """)));

    assertThatThrownBy(() ->
        new RestTemplate().getForObject(
            "http://localhost:" + wireMock.port() + "/coupons/EXPIRED-001",
            Map.class
        )
    ).isInstanceOf(HttpClientErrorException.class)
     .hasMessageContaining("410");
}
```

**หมายเหตุ:** HTTP 410 Gone (ต่างจาก 404 Not Found) หมายความว่า resource เคยมีอยู่แต่ถูกลบออกอย่างถาวร — ทั้งสองเป็น subclass ของ `HttpClientErrorException` แต่ spring จะ throw `HttpClientErrorException.Gone` สำหรับ 410 โดยเฉพาะ ซึ่ง `isInstanceOf(HttpClientErrorException.class)` ครอบคลุมทั้งคู่
</details>

---

### ระดับ Advanced

**โจทย์:** ออกแบบและเขียน test ที่จำลองสถานการณ์ "retry after service recovery" สำหรับ `CouponValidationService`:

1. POST `/coupons/validate` ครั้งแรก → HTTP 503 Service Unavailable  
   `{"error":"Service temporarily unavailable"}`
2. POST `/coupons/validate` ครั้งที่สอง (หลัง retry) → HTTP 200  
   `{"valid":true,"discount":15}`

ใช้ Scenario เพื่อจำลอง state transition และ assert ว่า:
- ครั้งแรกได้รับ `HttpServerErrorException` ที่มี `"503"` ใน message
- ครั้งที่สองได้รับ response ที่มี `valid = true` และ `discount = 15`

นอกจากนี้ อธิบายว่าในระบบ production จริง คุณจะทดสอบ **retry logic** ของ client ด้วย test นี้ได้อย่างไร (hint: Spring Retry หรือ Resilience4j)

<details>
<summary>ดูเฉลย</summary>

```java
// tested: WireMock 3.13.2 + JUnit 5
@Test
void validateCoupon_503ThenRecovery_retryScenario() {
    final String SCENARIO = "coupon-service-recovery";

    wireMock.stubFor(post(urlEqualTo("/coupons/validate"))
        .inScenario(SCENARIO)
        .whenScenarioStateIs(STARTED)
        .willSetStateTo("RECOVERED")
        .willReturn(aResponse()
            .withStatus(503)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {"error":"Service temporarily unavailable"}
                """)));

    wireMock.stubFor(post(urlEqualTo("/coupons/validate"))
        .inScenario(SCENARIO)
        .whenScenarioStateIs("RECOVERED")
        .willReturn(okJson("""
            {"valid":true,"discount":15}
            """)));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<>(
        """{"code":"SAVE15","orderId":"ORD-555"}""",
        headers
    );

    // ครั้งแรก — 503
    assertThatThrownBy(() ->
        restTemplate.postForObject(baseUrl + "/coupons/validate", entity, Map.class)
    ).isInstanceOf(HttpServerErrorException.class)
     .hasMessageContaining("503");

    // ครั้งที่สอง — 200 หลัง retry
    var result = restTemplate.postForObject(
        baseUrl + "/coupons/validate", entity, Map.class
    );
    assertThat(result.get("valid")).isEqualTo(true);
    assertThat(result.get("discount")).isEqualTo(15);
}
```

**การทดสอบ retry logic จริงด้วย Spring Retry:**

ถ้า `CouponClient` ใช้ `@Retryable` จาก Spring Retry:

```java
@Retryable(retryFor = HttpServerErrorException.class, maxAttempts = 2)
public Map<String, Object> validate(String code, String orderId) {
    // ... เรียก /coupons/validate
}
```

test เดียวกันนี้จะทดสอบ retry ได้ เพราะ:
1. `@Retryable` จะ catch `HttpServerErrorException` จาก call แรก (503) แล้วเรียกซ้ำอัตโนมัติ
2. WireMock จะอยู่ใน state `"RECOVERED"` แล้ว → call ที่สองได้ 200
3. method คืนค่าสำเร็จโดยที่ caller ไม่รู้ว่า retry เกิดขึ้น

เพิ่ม `wireMock.verify(2, postRequestedFor(...))` เพื่อยืนยันว่า retry เกิดขึ้นจริง 2 ครั้ง
</details>
