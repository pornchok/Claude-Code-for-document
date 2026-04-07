# Performance Testing ด้วย Apache JMeter — ภาพรวม Series

> Series นี้พาคุณจาก "ไม่รู้ performance testing คืออะไร" ไปถึง "สร้าง load test plan ให้ HTTP API จริงได้ ตีความผลและระบุ bottleneck ได้"

---

## ก่อนเริ่ม: Prior Knowledge Activation

นึกถึงสถานการณ์นี้ —

**ช่วง flash sale วันที่ 11.11** แพลตฟอร์ม e-commerce ดัง เปิดขาย 00:00 น. ผ่านไป 3 นาที ผู้ใช้หลายแสนคนกดซื้อพร้อมกัน เว็บค้าง, แอปช้าจนใช้ไม่ได้, บางคนกดจ่ายเงินแล้วได้หน้า error และไม่รู้ว่าสั่งซื้อสำเร็จหรือเปล่า บางคนถูกเก็บเงินสองครั้ง

ทีม dev แก้ไข 4 ชั่วโมงกว่าจะกลับมาปกติ — ยอดขายที่หายไปตีเป็นเงินได้หลายสิบล้านบาท

**Performance testing ช่วยป้องกันแบบนี้ได้อย่างไร?**

ก่อน deploy สู่ production คุณจำลองว่า "มีผู้ใช้หลายพันคนส่ง request พร้อมกัน" ในสภาพแวดล้อมทดสอบ ระบบจะพัง, ช้า, หรือมี memory leak ออกมาให้เห็นก่อน — ไม่ใช่ตอนที่ลูกค้าจริงกำลังกดซื้อ

Apache JMeter คือเครื่องมือที่ทำหน้าที่นี้ได้ — ฟรี open source และ verify ได้กับ API ทุกประเภท

---

## Prerequisites: ตรวจสอบก่อนอ่าน Series

ตอบคำถามต่อไปนี้ด้วยตัวเอง:

**1. คุณรู้ว่า HTTP request คืออะไรไหม?**
คุณควรรู้ว่า HTTP GET/POST ต่างกันอย่างไร, URL คืออะไร, response code เช่น 200, 404, 500 หมายถึงอะไร

**2. คุณเคยเรียก API ด้วยเครื่องมืออะไรสักอย่างไหม?**
เช่น Postman, curl, หรือ fetch ใน browser dev tools — ถ้าเคยเรียก API และเห็น response JSON ได้ คุณพร้อมแล้ว

**3. คุณรู้จัก concept ของ "server" ที่รับ request และส่ง response ไหม?**
ไม่ต้องรู้ว่าข้างในทำงานอย่างไร แต่ต้องเข้าใจว่า client ส่ง request → server ประมวลผล → ส่ง response กลับ

ถ้าตอบ "ไม่" ในข้อ 1 หรือ 3 → แนะนำให้อ่าน HTTP fundamentals ก่อน เช่น MDN Web Docs: https://developer.mozilla.org/en-US/docs/Web/HTTP/Overview

---

## Holistic Outcome: ทำอะไรได้เมื่อเรียนจบ Series

เมื่ออ่าน series นี้จบ คุณจะสามารถ:

- **อธิบาย** ความแตกต่างระหว่าง load test, stress test, spike test, soak test, smoke test, และ breakpoint test — และเลือกใช้ถูกประเภทตาม scenario
- **ติดตั้ง** Apache JMeter บนเครื่องตัวเองและรันได้ทั้ง GUI mode และ CLI mode
- **สร้าง** Test Plan ที่ใช้ HTTP Request Sampler ทดสอบ REST API endpoint ได้ตั้งแต่ต้น
- **รัน** load test ด้วย CLI mode (Non-GUI) และ generate HTML Dashboard Report
- **อ่าน** ผลจาก Aggregate Report และ HTML Dashboard — ระบุได้ว่า response time, throughput, และ error rate บอกว่าระบบมีปัญหาที่ไหน
- **ระบุ** common mistakes ที่ทำให้ผล test ไม่น่าเชื่อถือ และหลีกเลี่ยงได้

---

## สารบัญ (Clickable Table of Contents)

| บท | หัวข้อ | เวลาโดยประมาณ | ไฟล์ |
|----|--------|---------------|------|
| 1 | Performance Testing Fundamentals | 45–60 นาที | [01-performance-testing-fundamentals.md](./01-performance-testing-fundamentals.md) |
| 2 | JMeter Installation & Setup | 30–45 นาที | [02-installation-setup.md](./02-installation-setup.md) |
| 3 | JMeter UI & Test Plan Structure | 45–60 นาที | [03-ui-test-plan-structure.md](./03-ui-test-plan-structure.md) |
| 4 | สร้าง Test Plan แรกสำหรับ HTTP API | 60–90 นาที | [04-first-test-plan.md](./04-first-test-plan.md) |
| 5 | Thread Group Best Practices & Think Time | 45–60 นาที | [05-thread-group-best-practices.md](./05-thread-group-best-practices.md) |
| 6 | Parameterization ด้วย CSV Data Set Config | 45–60 นาที | [06-parameterization.md](./06-parameterization.md) |
| 7 | Assertions — ตรวจสอบว่า Response ถูกต้อง | 30–45 นาที | [07-assertions.md](./07-assertions.md) |
| 8 | Running Tests & HTML Dashboard Reports | 45–60 นาที | [08-running-reports.md](./08-running-reports.md) |
| 9 | Distributed Testing เบื้องต้น | 45–60 นาที | [09-distributed-testing.md](./09-distributed-testing.md) |
| 10 | Anti-Patterns & Production Best Practices | 30–45 นาที | [10-anti-patterns-best-practices.md](./10-anti-patterns-best-practices.md) |
| — | แบบฝึกหัด (ทุก concept) | — | [exercises.md](./exercises.md) |
| — | Glossary คำศัพท์ | — | [glossary.md](./glossary.md) |

---

## Reading Schedule: แนะนำการอ่าน

Series นี้ออกแบบให้อ่าน **1–2 บทต่อวัน** พร้อมลงมือทำ exercise หลังจบแต่ละบท

**ตัวอย่าง schedule 2 สัปดาห์:**

| วัน | บทที่ | กิจกรรม |
|-----|-------|----------|
| วันที่ 1 | 1 | อ่านบทที่ 1 + ทำ exercise บทที่ 1 |
| วันที่ 2 | 2 | อ่านบทที่ 2 + ติดตั้ง JMeter จริง |
| วันที่ 3 | 3 | อ่านบทที่ 3 + ลอง UI ตาม |
| วันที่ 4 | 4 | อ่านบทที่ 4 + สร้าง test plan แรก |
| วันที่ 5 | *ทบทวน* | ทำ exercise บท 1–4 ใหม่โดยไม่ดูโน้ต |
| วันที่ 6 | 5–6 | อ่านบทที่ 5–6 |
| วันที่ 7 | 7–8 | อ่านบทที่ 7–8 + รัน CLI mode จริง |
| วันที่ 8 | *ทบทวน* | ทำ exercise บท 5–8 + อ่าน HTML report |
| วันที่ 9 | 9 | อ่านบทที่ 9 |
| วันที่ 10 | 10 | อ่านบทที่ 10 |
| วันที่ 11–14 | *ทบทวน* | ทำโปรเจคจริง: สร้าง test plan สำหรับ API ของตัวเอง |

**Spacing ที่แนะนำ:** ทบทวนเนื้อหาหลังอ่าน 24 ชั่วโมง, 3 วัน, และ 1 สัปดาห์ — การ spacing ช่วยให้จำได้นานกว่าการอ่านซ้ำในวันเดียวกัน

---

## วิธีอ่านให้ได้ผลสูงสุด

ทำ 5 ข้อนี้ทุกครั้งที่อ่าน:

**1. Predict ก่อนอ่าน**
อ่านหัวข้อบทและ "วัตถุประสงค์" ก่อน แล้วลองเดาว่าบทนั้นจะอธิบายอะไร เขียนคำตอบลงกระดาษ — การ predict บังคับให้สมองเชื่อมกับสิ่งที่รู้อยู่แล้ว

**2. หยุดที่ ⏸ เสมอ**
เมื่อเจอสัญลักษณ์ ⏸ ให้หยุดและเขียนคำตอบก่อนอ่านต่อ — อย่า scroll ข้ามไป ถึงแม้จะรู้สึกว่า "รู้แล้ว" ก็ตาม เพราะความรู้สึกนั้นมักผิด

**3. Explain Aloud หลังจบ section**
หลังอ่านจบแต่ละ section ให้อธิบายด้วยคำพูดตัวเองราวกับสอนเพื่อน — ถ้าอธิบายติดๆ ขัดๆ ตรงไหน นั่นคือจุดที่ยังไม่เข้าใจจริง

**4. ทำ Exercise ก่อนดูเฉลย**
ทำแบบฝึกหัดโดยไม่เปิดดูเฉลยก่อน — ถึงแม้จะทำผิดก็ดีกว่าดูเฉลยก่อน เพราะการพยายามคิดเองทำให้จำได้นานกว่า (Generation Effect)

**5. เขียน Question ที่ยังค้างใจ**
เมื่ออ่านจบแต่ละบท เขียนคำถามที่ยังไม่มีคำตอบ 1–2 ข้อ แล้วหาคำตอบก่อนอ่านบทถัดไป — ความสงสัยที่ยังค้างอยู่ดีกว่าความรู้สึกว่า "เข้าใจหมดแล้ว" โดยไม่ได้ตรวจสอบ

---

*Series นี้อิงข้อมูลจาก Apache JMeter 5.6.3 (official source: https://jmeter.apache.org) และ Grafana k6 Load Testing docs สำหรับนิยามประเภท performance test*
