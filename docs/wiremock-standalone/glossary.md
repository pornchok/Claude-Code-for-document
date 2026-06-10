# Glossary — WireMock สำหรับ QA

---

## Admin API
REST API ของ WireMock เองสำหรับจัดการ stub แบบ real-time เช่น เพิ่ม/ลบ stub, reset scenario, ดู request history — เข้าถึงผ่าน `/__admin/...`
*(source: https://wiremock.org/docs/standalone/administration/)*

---

## bodyFileName
Field ใน response ของ stub ที่ชี้ไปยังไฟล์ใน `__files/` แทนที่จะใส่ response body ตรงๆ ใน JSON — ใช้เมื่อ response body ยาวมาก
*(source: https://wiremock.org/docs/stubbing/)*

---

## CONNECTION_RESET_BY_PEER
Network fault type ใน WireMock ที่จำลองการที่ server ตัด TCP connection กะทันหัน — client จะได้รับ `Connection reset` error แทน HTTP response
*(source: https://wiremock.org/docs/simulating-faults/)*

---

## EMPTY_RESPONSE
Network fault type ที่จำลองการที่ server รับ connection แต่ไม่ส่งข้อมูลใดๆ กลับมาเลย — ใช้ทดสอบ timeout handling ของ client
*(source: https://wiremock.org/docs/simulating-faults/)*

---

## equalToJson
Body matcher ที่ match request body กับ JSON ที่กำหนดไว้ — สามารถเพิ่ม `ignoreExtraElements: true` เพื่อ match แม้ body มี field เพิ่มเติม
*(source: https://wiremock.org/docs/request-matching/)*

---

## fault
Field ใน response section ของ stub ที่ใช้จำลอง network-level failure แทนที่จะ return HTTP response ปกติ
*(source: https://wiremock.org/docs/simulating-faults/)*

---

## fixedDelayMilliseconds
Field ใน response section ของ stub สำหรับกำหนด delay คงที่ (หน่วย millisecond) ก่อน return response — ใช้จำลอง slow API
*(source: https://wiremock.org/docs/simulating-faults/)*

---

## jsonBody
Field ใน response section ของ stub ที่รับ JSON object โดยตรง ไม่ต้อง escape — แนะนำให้ใช้แทน `body` เมื่อ response เป็น JSON
*(source: https://wiremock.org/docs/stubbing/)*

---

## mappings/
Folder ที่ WireMock อ่าน JSON stub files จากเมื่อ startup — WireMock สร้างให้อัตโนมัติเมื่อรันครั้งแรก
*(source: https://wiremock.org/docs/standalone/java-jar/)*

---

## Mock API
API ปลอมที่สร้างขึ้นมาเพื่อแทน API จริง return response ที่กำหนดได้เองโดยไม่มี business logic จริง — ใช้ในการทดสอบเพื่อ isolate component ที่ต้องการ test
*(source: https://wiremock.org/docs/overview/)*

---

## Mock Server
Server ที่รัน mock API — WireMock Standalone เป็นตัวอย่างของ mock server ที่รันเป็น process แยกต่างหาก
*(source: https://wiremock.org/docs/standalone/)*

---

## newScenarioState
Field ใน stub mapping ที่กำหนดว่า scenario จะเปลี่ยนไปอยู่ที่ state อะไรหลังจาก stub นี้ match request
*(source: https://wiremock.org/docs/stateful-behaviour/)*

---

## requiredScenarioState
Field ใน stub mapping ที่กำหนดว่า stub นี้จะ match request ก็ต่อเมื่อ scenario อยู่ใน state ที่ระบุ
*(source: https://wiremock.org/docs/stateful-behaviour/)*

---

## Response Templating
Feature ของ WireMock ที่ทำให้ response body สามารถมีค่า dynamic ได้ เช่น `{{request.url}}`, `{{now}}`, `{{randomValue type='UUID'}}` — เปิดใช้ด้วย `--global-response-templating`
*(source: https://wiremock.org/docs/response-templating/)*

---

## Scenario
State machine ที่มีชื่อ ใช้จำลอง stateful behavior — stub หลายตัวสามารถ share scenario เดียวกันและ respond ต่างกันตาม state ปัจจุบัน
*(source: https://wiremock.org/docs/stateful-behaviour/)*

---

## scenarioName
Field ใน stub mapping ที่ระบุว่า stub นี้เป็นส่วนหนึ่งของ scenario ชื่ออะไร
*(source: https://wiremock.org/docs/stateful-behaviour/)*

---

## Standalone Mode
โหมดรัน WireMock เป็น server แยกต่างหาก ไม่ต้องเขียน Java code — ใช้ `java -jar wiremock-standalone-X.Y.Z.jar` เพื่อ start
*(source: https://wiremock.org/docs/standalone/java-jar/)*

---

## Stub
Response สำเร็จรูปที่กำหนดไว้ล่วงหน้า — ใน WireMock หมายถึง stub mapping ที่ประกอบด้วย request conditions และ response definition
*(source: https://wiremock.org/docs/stubbing/)*

---

## __files/
Folder สำหรับเก็บ response body files (JSON, HTML, text ฯลฯ) ที่ stub อ้างอิงผ่าน `bodyFileName` — WireMock serve ไฟล์เหล่านี้โดยตรงได้ด้วย
*(source: https://wiremock.org/docs/standalone/java-jar/)*

---

## urlPath
Field ใน request section ของ stub ที่ match เฉพาะ URL path ไม่รวม query string — แนะนำให้ใช้แทน `url` เมื่อ request อาจมี query parameter
*(source: https://wiremock.org/docs/request-matching/)*

---

## urlPattern
Field ใน request section ของ stub ที่ใช้ regex match URL — ใช้เมื่อต้องการ match URL หลาย pattern เช่น `/api/products/[0-9]+`
*(source: https://wiremock.org/docs/request-matching/)*

---

## Verify (WireMock)
Feature ที่ตรวจสอบว่า request ถูกส่งมาจริงหรือเปล่า และกี่ครั้ง — ใช้ผ่าน Admin API `GET /__admin/requests` หรือ `POST /__admin/requests/count`
*(source: https://wiremock.org/docs/standalone/administration/)*
