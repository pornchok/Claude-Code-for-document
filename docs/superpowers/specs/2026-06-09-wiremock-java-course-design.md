# WireMock Java Course — Design Spec
**Date:** 2026-06-09  
**Status:** Approved

---

## 1. ภาพรวม

สร้างสื่อการเรียนรู้ WireMock ภาษาไทย สำหรับมือใหม่ที่รู้ Java พื้นฐาน  
เน้น hand-holding, accuracy สูง, และ animation สำหรับ concept ที่เห็นภาพแล้วเข้าใจทันที

**Stack:** Java + Maven + Spring Boot + WireMock 3.x  
**Format:** Markdown (เนื้อหาหลัก) + HTML animations (4 ไฟล์)  
**Running Example:** Order Service เรียก Payment API ภายนอก (Omise mock)

---

## 2. Prerequisites ของผู้เรียน

- Java พื้นฐาน (class, method, import, loops)
- IntelliJ IDEA หรือ VS Code ติดตั้งแล้ว
- Maven ติดตั้งแล้ว (หรือใช้ Maven Wrapper ได้)
- ไม่ต้องมีประสบการณ์ testing หรือ WireMock มาก่อน

---

## 3. โครงสร้าง 11 บท

| # | ชื่อบท | จุดประสงค์หลัก |
|---|--------|--------------|
| 1 | ทำไมต้อง Mock API? | ปูปัญหา external dependency — ทำไม test ที่เรียก API จริงถึงแย่ |
| 2 | Setup: Maven + JUnit 5 + WireMock + Lifecycle | จับมือ setup + อธิบาย lifecycle (start/stop/reset) + base URL injection |
| 3 | Stub พื้นฐาน: GET + Response Body | สร้าง stub แรก ตอบ GET request แบบ hardcode |
| 4 | Verify: ตรวจว่า client เรียก API จริง | สอน verify() คู่ stubFor — ป้องกัน silent pass |
| 5 | Request Matching: POST + Body | match request body ด้วย equalToJson + ignoreArrayOrder |
| 6 | Request Matching: Headers + Query Params | match headers, query params, urlPathEqualTo vs urlEqualTo |
| 7 | Response Templating: Dynamic Response | ใช้ Handlebars template ใน response body |
| 8 | Scenarios: Stateful API + Retry Logic | state machine mock — first call fails, second succeeds |
| 9 | Spring Boot Integration: @WireMockTest | @SpringBootTest + @EnableWireMock + auto port injection |
| 10 | Error Simulation: 5xx, 4xx, Timeout | ทดสอบ error handling path ของ client ใน Spring context |
| 11 | Best Practices + Common Mistakes | gotchas รวม, anti-patterns, real-world checklist |

---

## 4. มาตรฐานเนื้อหาต่อบท (7 ส่วน)

ทุกบทมีครบ 7 ส่วนนี้ (บทที่ 1 ยกเว้น Pre-chapter Retrieval):

1. **Pre-chapter Retrieval** (บท 2+) — คำถามจากบทก่อน, ให้ตอบก่อน scroll ดูเฉลย
2. **วัตถุประสงค์** — bullet list ด้วย verb วัดได้ (เขียน/อธิบาย/แก้/สร้าง)
3. **ทำไมต้องรู้?** — ปัญหาที่ concept นี้แก้ ก่อนอธิบายว่ามันคืออะไร
4. **เนื้อหาหลัก** — verify กับ WireMock official docs ก่อนเขียน
5. **ตัวอย่าง 3 ระดับ** — Beginner / Intermediate (สถานการณ์ใหม่) / Advanced (production-grade)
6. **Common Mistakes** — ❌ → ✅ + เหตุผล + source กำกับ
7. **สรุปบท** — Retrieval questions 2-3 ข้อ ให้ตอบก่อนดูเฉลย

**Code standards:**
- รันได้ทันที ไม่ใช่ partial snippet
- แสดง output จริงที่ได้จากการรัน
- ระบุ version: WireMock 3.x, Spring Boot 3.x, Java 17+
- Running example ใช้ Order Service → Payment API ตลอด — ห้ามดริฟไปเป็น `/api/foo` generic
- Test ด้วย Bash ก่อนใส่เอกสาร

---

## 5. Animation Spec (4 ไฟล์)

วางใน `docs/wiremock-java/animations/`  
Tech: HTML + CSS animations + Vanilla JS (ไม่ใช้ external framework)

| ไฟล์ | concept | ใช้ใน |
|------|---------|-------|
| `01-how-wiremock-works.html` | Request flow: client → WireMock → response | บท 1 |
| `02-stub-lifecycle.html` | start → stubFor → call → verify → reset → stop (step-by-step highlight) | บท 2 |
| `03-url-matching.html` | urlEqualTo vs urlPathEqualTo vs urlMatching (interactive: type URL, see match result) | บท 6 |
| `04-scenario-state.html` | State machine: STARTED → RETRY → SUCCESS (animated flow) | บท 8 |

---

## 6. File Structure

```
docs/wiremock-java/
├── 00-overview.md
├── 01-why-mock-api.md
├── 02-setup.md
├── 03-basic-stub-get.md
├── 04-verify.md
├── 05-request-matching-post-body.md
├── 06-request-matching-headers-query.md
├── 07-response-templating.md
├── 08-scenarios-stateful.md
├── 09-spring-boot-integration.md
├── 10-error-simulation.md
├── 11-best-practices.md
├── exercises.md
├── glossary.md
└── animations/
    ├── 01-how-wiremock-works.html
    ├── 02-stub-lifecycle.html
    ├── 03-url-matching.html
    └── 04-scenario-state.html
```

---

## 7. Dependencies ที่ใช้ (verified)

```xml
<!-- WireMock core — WireMock 3.x -->
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock</artifactId>
    <version>3.13.2</version>
    <scope>test</scope>
</dependency>

<!-- Spring Boot integration -->
<dependency>
    <groupId>org.wiremock.integrations</groupId>
    <artifactId>wiremock-spring-boot</artifactId>
    <version>4.0.9</version>
    <scope>test</scope>
</dependency>
```

⚠️ หมายเหตุ: WireMock 3.x เปลี่ยน artifact จาก `com.github.tomakehurst:wiremock` → `org.wiremock:wiremock` ต้องระบุให้ชัดในเอกสาร

---

## 8. Accuracy Protocol

ก่อนเขียนทุก concept:
1. WebSearch → หา official docs URL
2. WebFetch → ดึงเนื้อหาที่ต้องการ
3. บันทึก QUOTE ใน source-notes ก่อนเขียน
4. ถ้า verify ไม่ได้ → label "ยังไม่ได้ verify" ในเอกสาร

Source หลัก: https://wiremock.org/docs/

---

## 9. Gotchas ที่ต้องครอบคลุม (จาก expert review)

- **Lifecycle:** ลืม start/stop → "connection refused" ที่หาไม่เจอ
- **Base URL injection:** client ต้องชี้ไปที่ `localhost:{port}` ของ WireMock — ถ้าลืม test เรียก API จริง
- **urlEqualTo vs urlPathEqualTo:** `urlEqualTo` match รวม query string, `urlPathEqualTo` match แค่ path
- **Stub leak:** `WireMockServer` ไม่ reset อัตโนมัติ ต้อง `resetAll()` ใน `@AfterEach`
- **`@WireMockTest` reset:** reset stubs ระหว่าง test อัตโนมัติ — ต่างจาก programmatic server
- **Jetty conflict:** Spring Boot + Jetty embedded อาจชนกับ WireMock → ใช้ `wiremock-jetty12`
- **Port collision:** ห้าม hardcode port → ใช้ port 0 (random)

---

## 10. ลำดับการสร้าง (Implementation Order)

1. `00-overview.md` — ภาพรวม, prerequisites, TOC
2. Animation `01-how-wiremock-works.html` — ก่อนบท 1 เพื่อมีของ link
3. บท 1-4 (core concepts) ทีละบท
4. Animation `02-stub-lifecycle.html`
5. บท 5-8
6. Animation `03-url-matching.html` + `04-scenario-state.html`
7. บท 9-11
8. `exercises.md`
9. `glossary.md`
