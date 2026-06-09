# คอร์ส WireMock สำหรับ Java — ภาพรวม

## WireMock คืออะไร?

WireMock คือ open-source API mocking tool ที่ช่วยให้คุณ "สร้าง stable test environment, แยกตัวเองออกจาก 3rd-party ที่ไม่เสถียร, และจำลอง API ที่ยังไม่มีอยู่จริง" (ที่มา: wiremock.org/docs)

ง่ายๆ คือ WireMock เป็น **fake server** ที่รับ HTTP request แล้วตอบกลับตามที่คุณกำหนดเอง — ใช้แทน API จริงในระหว่างทดสอบ WireMock ได้รับการดาวน์โหลดกว่า **5 ล้านครั้งต่อเดือน** และเป็น standard ของอุตสาหกรรมสำหรับการทดสอบ integration ใน Java ecosystem

---

## Prerequisites

ก่อนเริ่มคอร์สนี้ คุณควรมีความรู้พื้นฐานเหล่านี้:

- **Java 17** ขึ้นไป (คอร์สนี้ใช้ Java 17 + text blocks)
- **Maven 3.x** — หรือใช้ Maven ที่แนบมาใน `sample-project/.mvn-local/`
- **JUnit 5** — รู้จัก `@Test`, `@BeforeEach`, `@AfterEach`
- **Spring Boot 3.4.1** — รู้จัก `RestTemplate` หรือ `WebClient` เบื้องต้น
- **IntelliJ IDEA** หรือ **VS Code** (พร้อม Extension Pack for Java)

ถ้าคุณเคยเขียน unit test ใน Java มาบ้างแล้ว คอร์สนี้เหมาะกับคุณเลย

---

## เวอร์ชันที่ใช้ในคอร์สนี้

| Library | Version |
|---------|---------|
| Java | 17 |
| WireMock | 3.13.2 |
| Spring Boot | 3.4.1 |
| Maven | 3.9.6 (ใน .mvn-local) |

---

## วิธีรัน Sample Code

โปรเจคตัวอย่างอยู่ที่ `docs/wiremock-java/sample-project/`

### รัน test ด้วย Maven ที่แนบมา

```bash
# Windows — ใช้ mvn.cmd ที่แนบมาใน .mvn-local
cd docs/wiremock-java/sample-project

.mvn-local\apache-maven-3.9.6\bin\mvn.cmd test -Dtest=Ch02SetupTest -q

# รัน test ทั้งหมด
.mvn-local\apache-maven-3.9.6\bin\mvn.cmd test -q
```

### Maven dependency ที่สำคัญใน pom.xml

```xml
<!-- WireMock 3.x standalone — artifact ID ที่ถูกต้อง -->
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <version>3.13.2</version>
    <scope>test</scope>
</dependency>
```

> **สำคัญ**: ใช้ `org.wiremock` (ไม่ใช่ `com.github.tomakehurst` ซึ่งเป็น groupId เก่าของ WireMock 2.x)

---

## เวลาโดยประมาณ

แต่ละบทใช้เวลาประมาณ **30–45 นาที** รวมทั้งคอร์สประมาณ **8 ชั่วโมง**

---

## สารบัญ

| บท | หัวข้อ | เวลา |
|----|--------|------|
| บทที่ 1 | [ทำไมต้อง Mock API?](01-why-mock-api.md) | 30 นาที |
| บทที่ 2 | [ติดตั้งและตั้งค่า WireMock](02-setup.md) | 35 นาที |
| บทที่ 3 | [Stub GET Request พื้นฐาน](03-basic-stub-get.md) | 40 นาที |
| บทที่ 4 | [Verify การเรียก API](04-verify.md) | 40 นาที |
| บทที่ 5 | Stub POST + Request Body Matching | 40 นาที |
| บทที่ 6 | Response Templating | 45 นาที |
| บทที่ 7 | Simulating Errors และ Network Faults | 40 นาที |
| บทที่ 8 | Stateful Stubs (Scenarios) | 45 นาที |
| บทที่ 9 | WireMock กับ Spring Boot (@WireMockTest) | 45 นาที |
| บทที่ 10 | Record & Playback | 35 นาที |
| บทที่ 11 | WireMock ใน CI/CD Pipeline | 30 นาที |

---

## Animations ประกอบการเรียน

คอร์สนี้มี animation 4 ชิ้นช่วยอธิบาย concept ที่ซับซ้อน:

| Animation | เนื้อหา |
|-----------|---------|
| [WireMock ทำงานอย่างไร?](animations/01-how-wiremock-works.html) | ภาพรวมการ intercept HTTP request |
| [Stub Lifecycle](animations/02-stub-lifecycle.html) | วงจรชีวิตของ stub ตั้งแต่ start ถึง stop |
| [URL Matching](animations/03-url-matching.html) | วิธีที่ WireMock จับคู่ URL pattern |
| [Scenario State](animations/04-scenario-state.html) | การทำงานของ stateful stub |

---

## Running Example ตลอดคอร์ส

คอร์สนี้ใช้ **Order Service → Payment API (Omise-style)** เป็น example หลัก:

```
[Order Service] --HTTP GET/POST--> [Payment API]
                                        ↑
                              WireMock แทนที่ตรงนี้
```

`PaymentClient` (ใน `src/main/java`) คือ class ที่เราจะเขียน test ให้ตลอดคอร์ส

---

## หลังจากอ่านคอร์สนี้จบ คุณจะ...

- เขียน WireMock test สำหรับ GET/POST API ได้อย่างคล่องแคล่ว
- จำลอง error scenarios (timeout, 500, 401) ได้
- verify ว่า code เรียก API ถูก endpoint ด้วย parameter ที่ถูกต้อง
- integrate WireMock กับ Spring Boot test ได้
- ใช้ WireMock ใน CI/CD pipeline ได้
