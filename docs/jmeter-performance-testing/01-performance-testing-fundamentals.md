# บทที่ 1: Performance Testing Fundamentals

**เวลาที่ใช้โดยประมาณ:** 45–60 นาที

---

## 1. วัตถุประสงค์

อ่านบทนี้จบแล้ว คุณจะสามารถ:

- **ระบุ** ความแตกต่างระหว่าง performance testing 6 ประเภท (Smoke, Load, Stress, Spike, Soak, Breakpoint) และบอกได้ว่าแต่ละประเภทตอบคำถามอะไร
- **อธิบาย** ว่า performance testing แตกต่างจาก functional testing อย่างไร และทำไมถึงจำเป็นต้องทำทั้งสองอย่าง
- **เปรียบเทียบ** ว่า Average response time กับ 90th percentile ต่างกันอย่างไร และควรใช้ metric ไหนเป็น primary KPI
- **ยกตัวอย่าง** สถานการณ์จริงที่ควรเลือก test type แต่ละแบบ พร้อมเหตุผล
- **ตัดสินใจ** ลำดับการรัน performance test ที่เหมาะสมสำหรับ scenario ที่กำหนดให้

---

## 2. ทำไมต้องรู้? (Why)

### ปัญหาที่เกิดขึ้นโดยไม่มี performance testing

ลองนึกถึง scenario นี้ —

ทีมคุณสร้าง API สำหรับระบบลงทะเบียนสอบออนไลน์ ทดสอบด้วยการส่ง HTTP request ผ่าน Postman ทุก endpoint ได้รับ response ถูกต้อง HTTP status 200, JSON ถูก format, ข้อมูลในฐานข้อมูลบันทึกถูก — functional test ผ่านทั้งหมด

วันที่เปิดลงทะเบียน มีนักศึกษา 8,000 คนส่ง request พร้อมกันทันทีที่ระบบเปิด server ไม่ตอบสนอง บางคนส่ง request ซ้ำหลายครั้งเพราะคิดว่าระบบค้าง ระบบลงทะเบียนซ้ำ บางคนเสียสิทธิ์เพราะ timeout ก่อนการลงทะเบียนสำเร็จ

**ทำไมถึงเกิดปัญหา?** เพราะ functional test ตรวจสอบว่า "ระบบทำถูกต้องไหม?" แต่ไม่ได้ตรวจว่า "ระบบรับ request พร้อมกัน 8,000 requests ได้ไหม?" คือคำถามคนละข้อกันทั้งหมด

### Performance testing แก้ปัญหาอะไร?

Performance testing จำลองสถานการณ์ที่มีผู้ใช้จำนวนมากส่ง HTTP request มายัง server พร้อมกัน ก่อนที่จะ deploy จริง เพื่อ:

- ค้นหาว่า server รับ concurrent request ได้มากสุดเท่าไหร่ก่อน response time เกินที่ยอมรับได้
- ระบุ bottleneck ว่าปัญหาอยู่ที่ database query, API server, network, หรือ external service
- ยืนยันว่าระบบรันได้ต่อเนื่องหลายชั่วโมงโดยไม่มี resource leak
- กำหนด capacity limit และ SLA ได้จากข้อมูลจริง ไม่ใช่การเดา

**กล่าวง่ายๆ**: อยากให้ระบบแสดงปัญหาในสภาพแวดล้อมทดสอบ — ไม่ใช่ตอนที่ผู้ใช้จริงกำลังส่ง request อยู่

---

## 3. Analogy

### Performance Test เหมือน "การซ้อมอพยพอาคารสำนักงาน"

ลองนึกถึงอาคารสำนักงาน 20 ชั้น ที่มีพนักงาน 2,000 คน ก่อน certificate รับรองความปลอดภัยจะออกให้ ทีม safety ต้องทำ fire drill

**Mechanism 1 — จำลอง load จริงในสภาพแวดล้อมควบคุม**
Fire drill เกิดขึ้นโดยที่ทุกคนรู้ตัว (controlled environment) แต่ใช้คนจำนวนจริงและเส้นทางจริง — เหมือนกับที่ performance test จำลองจำนวน user จริงบน staging environment ไม่ใช่ production

**Mechanism 2 — ค้นหา bottleneck ก่อนเกิดเหตุจริง**
ระหว่างซ้อม ทีม safety อาจพบว่าบันไดฝั่งตะวันออกแออัดเกินไป ประตูทางออก B เปิดช้า ชั้น 15 ใช้เวลานานผิดปกติ — เหมือนกับที่ performance test เปิดเผยว่า database query ช้า หรือ connection pool หมดเมื่อมี users มาก

**Mechanism 3 — ผล test ต่างประเภทให้ข้อมูลต่างมุม**
ซ้อมแบบ "ประกาศล่วงหน้า" (Load Test) ต่างจาก "ประกาศกะทันหัน" (Spike Test) ต่างจาก "ซ้อมต่อเนื่อง 8 ชั่วโมงทุกวัน" (Soak Test) — แต่ละแบบให้ข้อมูลคนละมิติเกี่ยวกับระบบ

---

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:** ถ้า pass load test แล้วระบบปลอดภัย 100% — เหมือนซ้อมอพยพผ่านแต่ไม่ได้หมายความว่าป้องกันไฟไหม้ได้ performance test เป็นเพียงหลักฐานว่าระบบรับ load นั้นได้ **ภายใต้เงื่อนไขที่ทดสอบ** ณ เวลานั้น — load pattern ต่างออกไป, code เปลี่ยน, หรือ data volume เพิ่ม ผล test เดิมก็ไม่ valid อีก

---

## 4. เนื้อหาหลัก

### 4.1 Performance Testing คืออะไร?

Performance testing คือกระบวนการทดสอบว่า software ทำงานได้ดีแค่ไหนภายใต้สภาวะต่างๆ โดยเน้นที่ความเร็ว ความเสถียร และความสามารถรับ load ต่างจาก functional testing ที่ถามว่า "ทำงานถูกต้องไหม?" performance testing ถามว่า "ทำงานได้ดีแค่ไหนภายใต้ load?"

Apache JMeter เป็นเครื่องมือหนึ่งที่ใช้ทำ performance testing:

> "The Apache JMeter™ application is open source software, a 100% pure Java application designed to load test functional behavior and measure performance." — jmeter.apache.org

> "It can be used to simulate a heavy load on a server, group of servers, network or object to test its strength or to analyze overall performance under different load types." — jmeter.apache.org

JMeter ทำงานที่ **protocol level** — ส่ง HTTP request โดยตรง ไม่ผ่าน browser:

> "JMeter is not a browser, it works at protocol level" — jmeter.apache.org

**ความหมายในทางปฏิบัติ:** ถ้าทดสอบ web app ที่ render ด้วย JavaScript (React, Vue, Angular) JMeter จะไม่ execute JavaScript นั้น — สิ่งที่ JMeter เหมาะมากคือ load test API/backend HTTP endpoints

---

⏸ **หยุดก่อนอ่านต่อ:** ลองนึกว่า functional testing กับ performance testing ต่างกันอย่างไร — เขียนคำตอบลงกระดาษก่อน พร้อมเหตุผล 1 ประโยค

---

> **เฉลย:** Functional testing ตรวจสอบว่าระบบทำสิ่งที่ถูกต้องไหม ("API ส่ง JSON ถูก format ไหม?") — Performance testing ตรวจสอบว่าระบบทำสิ่งนั้นได้ดีแค่ไหนภายใต้ load ("API ส่ง JSON ถูก format แม้มี 5,000 concurrent users ไหม?") เป็นมิติคนละมิติที่ไม่แทนกัน
>
> **ถ้าตอบไม่ได้:** อ่าน section 4.1 อีกครั้ง โดยเน้นที่คำถามที่แต่ละ test ตอบ

---

### 4.2 Performance Testing 6 ประเภท

> **Note:** Apache JMeter official docs ไม่ได้นิยามประเภทเหล่านี้ไว้อย่างเป็นทางการ คำนิยามด้านล่างอ้างอิงจาก Grafana k6 Load Testing docs (industry standard reference)

แต่ละประเภทตอบคำถามคนละข้อ — ควรเลือกตาม scenario ไม่ใช่รัน "ทุกอย่าง" โดยไม่มีเหตุผล:

---

**1. Smoke Test**
*คำถาม: "ระบบทำงานปกติที่ load ต่ำมากไหม?"*

> "Smoke tests verify the system functions with minimal load, and they are used to gather baseline performance values." — Grafana k6 docs

ใช้เมื่อ: ก่อนรัน test อื่นทุกครั้ง เพื่อยืนยันว่า test plan ถูกต้องและระบบยัง up อยู่

ไม่ควรใช้เมื่อ: คาดหวังผลลัพธ์เรื่อง scalability — smoke test ไม่ได้ออกแบบมาเพื่อนั้น

---

**2. Load Test**
*คำถาม: "ระบบรับ load ปกติที่คาดหวังได้ไหม?"*

> "Load testing simulates user activity to determine how well a system can handle increased traffic or load." — Grafana k6 docs

ใช้เมื่อ: ทดสอบว่าระบบรับ traffic ปกติตาม business requirement ได้ดีแค่ไหน และ response time อยู่ใน SLA ไหม

ไม่ควรใช้เมื่อ: ต้องการรู้ว่าระบบพังที่ load เท่าไหร่ — ใช้ Breakpoint test แทน

---

**3. Stress Test**
*คำถาม: "ระบบทำงานอย่างไรเมื่อ load เกิน capacity ปกติ?"*

> "Stress tests help you discover how the system functions with the load at peak traffic." — Grafana k6 docs

ใช้เมื่อ: ต้องการรู้ว่าระบบ degrade gracefully ไหม — ถ้า load เกินจุดปกติ ระบบยังคืน error ที่เข้าใจได้หรือพังแบบ catastrophic?

ไม่ควรใช้เมื่อ: ยังไม่รู้ baseline load ปกติของระบบ — ทำ Load Test ก่อนเสมอ

---

**4. Spike Test**
*คำถาม: "ระบบรอดและฟื้นตัวจาก traffic พุ่งกะทันหันได้ไหม?"*

> "A spike test verifies whether the system survives and performs under sudden and massive rushes of utilization." — Grafana k6 docs

ใช้เมื่อ: ระบบมีโอกาสเจอ traffic พุ่งแบบกะทันหัน เช่น แคมเปญโซเชียลมีเดีย, flash sale

ไม่ควรใช้เมื่อ: ต้องการทดสอบ steady-state load — ใช้ Load Test แทน

---

**5. Soak Test (Endurance Test)**
*คำถาม: "ระบบยังดีอยู่หลังรัน load ปกติต่อเนื่องหลายชั่วโมงไหม?"*

> "Soak tests are a variation of the average-load test. The main difference is the test duration." — Grafana k6 docs

Soak test วิเคราะห์ performance degradation และ resource consumption (เช่น memory leak) ที่เกิดขึ้นเมื่อระบบทำงานต่อเนื่องเป็นระยะเวลานาน

ใช้เมื่อ: ต้องการค้นหา memory leak, connection leak, หรือ performance degradation ที่เกิดขึ้นช้าๆ ตามเวลา — มักรัน 4–24 ชั่วโมงขึ้นไป

ไม่ควรใช้เมื่อ: ระบบยังไม่ผ่าน Load Test — ไม่มีประโยชน์รัน 8 ชั่วโมงถ้ายังรับ load ปกติไม่ได้

---

**6. Breakpoint Test**
*คำถาม: "จุดที่ระบบพังอยู่ที่ load เท่าไหร่?"*

> "Breakpoint tests discover your system's limits." — Grafana k6 docs

ใช้เมื่อ: ต้องการรู้ capacity ceiling จริงๆ ของระบบ เพื่อวางแผน infrastructure หรือกำหนด load shedding threshold

ไม่ควรใช้เมื่อ: ไม่มีแผนจัดการระบบหลัง test — Breakpoint test อาจทิ้งระบบในสภาพต้องการ restart

---

⏸ **หยุดก่อนอ่านต่อ (Backward Retrieval จาก 4.1):** จากที่เพิ่งอ่านมาเรื่อง JMeter ทำงานที่ protocol level — ถ้า API ของคุณใช้ HTTPS และต้องการทดสอบ load ที่ 500 concurrent users ควรใช้ test ประเภทไหนก่อน? เขียนคำตอบลงกระดาษพร้อมลำดับขั้นตอน

---

> **เฉลย:** เริ่มจาก **Smoke Test** (1–5 users) ก่อนเสมอเพื่อยืนยันว่า HTTPS connection ทำงานถูกต้องและ test plan ถูก จากนั้นจึง **Load Test** ที่ 500 concurrent users — ถ้าข้ามไป Load Test ทันทีและ error rate 100% จะไม่รู้ว่าปัญหาอยู่ที่ระบบจริงหรือ test plan configuration ผิด
>
> **ถ้าตอบไม่ได้:** อ่าน section 4.2 อีกครั้งโดยเน้น Smoke Test และ Load Test — แล้วกลับมาอ่าน 4.1 เรื่อง protocol level อีกรอบ

---

### 4.3 Key Performance Metrics (KPIs)

เมื่อรัน performance test เสร็จ จะได้ตัวเลขหลายตัว ต้องรู้ว่าตัวไหนสำคัญและอ่านอย่างไร:

**1. Response Time (ms)**
เวลาที่ใช้ตั้งแต่ client ส่ง HTTP request จนได้รับ response — วัดเป็น milliseconds

ตัวเลขที่ต้องดู — ไม่ใช่แค่ Average แต่ต้องดู percentile:

> "The value below which 90% of the samples fall. The remaining samples take at least as long as this value." — jmeter.apache.org/usermanual/glossary.html (นิยาม 90th Percentile)

- **Average:** ค่าเฉลี่ย — ถูก distort ได้ง่ายโดย outlier ที่ช้ามาก
- **90th Percentile (P90):** 90% ของ users ได้รับ response เร็วกว่าตัวเลขนี้
- **95th Percentile (P95):** 95% ของ users ได้รับ response เร็วกว่าตัวเลขนี้

✅ **วิธีที่แนะนำ: ดู P90 เป็น primary metric** — เพราะแสดงประสบการณ์ที่ users ส่วนใหญ่ได้รับ

⚠️ **ควรเลี่ยง: ดูแค่ Average** — เพราะถ้า 90% ของ request ตอบ 100ms แต่ 10% ตอบ 5,000ms, Average ≈ 590ms ซึ่งดูดีแต่จริงๆ user 1 ใน 10 รอนาน 5 วินาที

**2. Throughput (requests/second)**
จำนวน request ที่ระบบรับและตอบสนองได้ต่อวินาที

> "Throughput is calculated as requests/unit of time. The time is calculated from the start of the first sample to the end of the last sample. The formula is: Throughput = (number of requests) / (total time)." — jmeter.apache.org/usermanual/glossary.html

ยิ่งสูงยิ่งดี แต่ต้องดูควบคู่กับ Error Rate — Throughput สูงบน Error Rate 50% ไม่ใช่ความสำเร็จ

**3. Error Rate (%)**
เปอร์เซ็นต์ของ request ที่ได้รับ error (HTTP 5xx, timeout, connection refused ฯลฯ) เป้าหมายคือ < 1% สำหรับระบบที่ดี

**4. Concurrent Users / Virtual Users (VUs)**
จำนวน user ที่ tool จำลองพร้อมกัน — ใน JMeter เรียกว่า "Threads" ตัวเลขนี้ตั้งจาก business requirement เช่น "ระบบต้องรับ 500 concurrent users"

**ความสัมพันธ์ระหว่าง metrics:**
- Throughput สูง + Error Rate ต่ำ + Response Time ต่ำ → ระบบดี
- Throughput สูง + Error Rate สูง → ระบบ overwhelmed กำลังทิ้ง request
- Response Time พุ่งขึ้นเรื่อยๆ เมื่อ load เพิ่ม → มีปัญหา scaling

### 4.4 เมื่อใดควรเริ่ม Performance Test และบน Environment ไหน?

**เมื่อใดควรเริ่ม?**

✅ **วิธีที่แนะนำ: เริ่มตั้งแต่ต้น development cycle** — API แรกที่สร้างสามารถมี smoke test ได้ทันที การค้นพบ performance issue ตั้งแต่เนิ่นๆ แก้ได้ถูกกว่ามากกว่าค้นพบหลัง system integration

⚠️ **ควรเลี่ยง: รอจน pre-release** — ถ้าพบ architecture-level bottleneck ตอนนั้น การแก้แทบเป็นไปไม่ได้ก่อน deadline

**บน Environment ไหน?**

✅ **วิธีที่แนะนำ: ทดสอบบน staging environment ที่ใกล้เคียง production** — ใช้ database size ใกล้เคียง production จำนวน server ใกล้เคียง ผลที่ได้จะ predictive กว่า

⚠️ **ควรเลี่ยง: ทำ load test บน production โดยตรง** — load test ออกแบบมาเพื่อ stress ระบบ มีความเสี่ยงทำ downtime ขณะมีลูกค้าจริงใช้งาน

> "When is a good time to load-test our application (i.e. off-hours or week-ends), bearing in mind that this may very well crash one or more of our servers?" — jmeter.apache.org/usermanual/boss.html

⚠️ **ควรเลี่ยง: ทดสอบบน local machine ของ developer** — performance จะแตกต่างจาก production มาก network latency, CPU, RAM ล้วนต่างกัน ผลที่ได้ไม่มีความหมายในเชิง production readiness

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner: ตาราง 6 ประเภทและลำดับที่แนะนำ

```
Performance Test Types — Overview Table
(ยังไม่ได้ทดสอบด้วย JMeter — เป็น conceptual diagram)
```

| ประเภท Test | คำถามที่ตอบ | Load ที่ใช้ | ระยะเวลาทั่วไป | ตัวอย่าง Use Case |
|-------------|-------------|------------|----------------|-------------------|
| **Smoke** | ระบบ up อยู่ไหม? | 1–5 users | 1–5 นาที | ก่อนรัน test อื่นทุกครั้ง |
| **Load** | รับ normal load ได้ไหม? | ถึง target แล้วคงที่ | 30–60 นาที | ทดสอบ 500 users ตาม SLA |
| **Stress** | เมื่อเกิน capacity ทำอะไร? | เกิน normal load | 30–60 นาที | ทดสอบ 200% ของ normal |
| **Spike** | รอดจาก traffic พุ่งไหม? | พุ่งสูงแล้วลดทันที | 15–30 นาที | จำลอง viral post |
| **Soak** | มี resource leak ไหม? | Normal load ต่อเนื่อง | 4–24 ชั่วโมง | รัน API 8 ชั่วโมงต่อเนื่อง |
| **Breakpoint** | พังที่ load เท่าไหร่? | เพิ่มขึ้นไม่หยุด | จนระบบพัง | หา max capacity |

**ลำดับที่แนะนำ:**
```
Smoke → Load → Stress → Spike → Soak → Breakpoint
  ↑                                         ↑
ทำก่อนเสมอ              ใช้เวลานานที่สุด — ทำหลังผ่าน Load แล้ว
```

ทำไมลำดับนี้? เพราะแต่ละ test ขึ้นกับ test ก่อนหน้า ถ้า Smoke ล้มเหลว แปลว่า test plan ผิด ไม่ใช่ระบบผิด ไม่ควรเปลืองเวลารัน Load test ต่อ

---

### Intermediate: เลือก Test Type สำหรับ Streaming Platform

**Scenario:** ทีม startup สร้าง video streaming API (`GET /api/v1/stream/{videoId}`) สำหรับ platform การเรียนออนไลน์ที่มีนักเรียน 10,000 คน ผู้บริหาร predict ว่าช่วง final exam จะมีนักเรียน 800 คนใช้พร้อมกัน และอาจมีช่วงที่ครูส่ง link ให้ทั้งชั้น 200 คนเปิดพร้อมกันทันที

**การตัดสินใจเลือก test types:**

1. **Smoke Test (1–5 users, 3 นาที)**
   - ทำไม: ยืนยันว่า streaming endpoint ตอบสนองถูกต้องก่อนทำอะไรต่อ
   - KPI threshold: Error rate = 0%, Response time < 3,000ms (streaming อาจใช้เวลา buffer)

2. **Load Test (ค่อยๆ เพิ่มถึง 800 users, 45 นาที)**
   - ทำไม: ทดสอบ SLA ว่ารับ concurrent streaming sessions ได้ตามที่ business คาด
   - KPI threshold: Error rate < 1%, P90 response time < 5,000ms, Throughput ไม่ลดลงเมื่อ users เพิ่ม

3. **Spike Test (200 users → 1,200 users ใน 30 วินาที → กลับ 200)**
   - ทำไม: ครูส่ง link ทั้งชั้นพร้อมกัน = spike จริงๆ ต้องรู้ว่าระบบรับได้ไหมและ recover ได้ไหม
   - KPI threshold: ระบบไม่ crash (HTTP 500 rate < 5% ในช่วง spike), recover กลับ normal ภายใน 60 วินาที

4. **Soak Test ถ้าเวลาอนุญาต (800 users, 8 ชั่วโมง)**
   - ทำไม: Video streaming มักมี memory leak เพราะ byte buffer — ต้องรู้ว่ารัน exam period 8 ชั่วโมงได้ไหม
   - KPI threshold: Memory usage ไม่เพิ่มขึ้นเกิน 20% ตลอด 8 ชั่วโมง

**ข้อสังเกต:** Domain นี้ไม่ใช่ e-commerce แต่หลักการเหมือนกัน — ระบุ pattern การใช้งานจริง แล้วเลือก test type ที่ตรงกับ pattern นั้น

---

### Advanced: Test Strategy สำหรับ Mixed Workload System

**Scenario:** ระบบ SaaS สำหรับ HR management มี workload patterns ต่างกันชัดเจน:
- **Peak hours (9:00–10:00 น.):** พนักงาน 2,000 คน check-in พร้อมกัน
- **Off-peak hours (11:00–15:00 น.):** request ประปราย เฉลี่ย 50 concurrent users
- **Month-end (วันที่ 30–31):** payroll calculation batch job รันพร้อมกับ normal traffic

**Trade-off Analysis:**

| Strategy | ข้อดี | ข้อเสีย | เมื่อใช้ |
|----------|-------|---------|---------|
| ทดสอบแยก pattern | ระบุ bottleneck ได้ชัด | ไม่เห็น interference ระหว่าง workload | Phase แรกเสมอ |
| ทดสอบรวม mixed workload | เห็น behavior จริง | ยากมากถ้า test fail ว่าส่วนไหนที่ผิด | หลัง isolated test ผ่านแล้ว |
| ใช้ distributed JMeter | Scale ถึง 2,000+ users ได้ | ซับซ้อน setup และ debug | เมื่อ single JMeter ไม่พอ |

**Design ที่แนะนำ (Step-by-step):**

```
Phase 1: Isolated Tests (สัปดาห์ 1)
├── Check-in API — Load Test (2,000 users peak, 60 นาที)
│   └── เป้าหมาย: ยืนยันว่า check-in รับ peak ได้
├── Payroll batch — Stress Test (monitor resource usage)
│   └── เป้าหมาย: รู้ว่า batch กิน CPU/memory เท่าไหร่
└── Smoke Test ทุก endpoint ก่อน phase นี้

Phase 2: Mixed Workload Test (สัปดาห์ 2)
├── JMeter Thread Group 1: Check-in users (2,000 users, 60 นาที)
├── JMeter Thread Group 2: Normal traffic (50 users, concurrent)
└── JMeter Thread Group 3: Payroll job trigger (1 thread, month-end sim)
    └── ดู: Cross-interference — payroll job ทำให้ check-in ช้าขึ้นไหม?

Phase 3: Soak Test (สัปดาห์ 3 ถ้าผ่าน Phase 1–2)
└── Off-peak load (50 users) ต่อเนื่อง 24 ชั่วโมง
    └── เป้าหมาย: ค้นหา memory/connection leak ใน normal operation
```

**สิ่งที่ต้อง monitor นอกจาก JMeter:**
ต้องดู server-side metrics ควบคู่กัน (CPU %, Memory %, DB connection pool, disk I/O) — JMeter เห็นแค่ client perspective ไม่เห็นว่า server ข้างในเกิดอะไรขึ้น

---

## 6. Common Mistakes

**❌ Mistake 1: ข้ามไปรัน Load/Stress Test โดยไม่ผ่าน Smoke Test ก่อน**

> ❌ รัน stress test (500 users) ทันทีเพราะต้องการรู้ capacity — ผล error rate 95% แต่ไม่รู้ว่าเกิดจากระบบหรือ test plan ผิด

> ✅ รัน smoke test (1–5 users) ก่อนเสมอ เพื่อยืนยันว่า test plan ถูกต้องและระบบทำงานปกติที่ load ต่ำมาก

**เหตุผล:** ถ้า test plan มีข้อผิดพลาด (URL ผิด, header ขาด, auth ผิด) การรัน stress test จะให้ผล error 100% — ไม่ได้บอกอะไรเกี่ยวกับระบบเลย เสียเวลาเปล่า

🔍 **สัญญาณที่จะสังเกตเห็น:** Error rate 100% หรือ response code ผิดแปลก (เช่น 404 ทุก request) ตั้งแต่ใน stress test แรก

🤔 **Metacognitive Prompt:** "ถ้า error rate สูงมากทันทีที่รัน ถามตัวเองก่อนว่า — request เดียวด้วยเครื่องมืออื่น (เช่น curl) ผ่านไหม? ถ้าผ่าน แปลว่าปัญหาที่ JMeter configuration ไม่ใช่ระบบ"

*(source: https://grafana.com/load-testing/types-of-load-testing/ — Grafana k6 docs: "Smoke tests verify the system functions with minimal load, and they are used to gather baseline performance values.")*

---

**❌ Mistake 2: ดูแค่ Average Response Time โดยไม่ดู Percentile**

> ❌ "Average response time = 350ms — ผ่าน SLA ที่กำหนดไว้ว่า < 500ms" แต่ P90 = 3,800ms ซึ่งไม่เคยดูเลย

> ✅ ดู **90th percentile (P90)** เป็น primary metric เสมอ และดู P95, P99 ด้วยถ้ามี SLA เข้มงวด

**เหตุผล:** ถ้า 90% ของ request ตอบ 100ms แต่ 10% ตอบ 5,000ms, Average ≈ 590ms ซึ่งดูดีแต่จริงๆ user 1 ใน 10 รอนาน 5 วินาที สร้างประสบการณ์ที่แย่มาก

> "The value below which 90% of the samples fall. The remaining samples take at least as long as this value." — jmeter.apache.org/usermanual/glossary.html

🔍 **สัญญาณที่จะสังเกตเห็น:** Average ดูดีแต่มี user complaint ว่าระบบช้า หรือ Min กับ Max แตกต่างกันมากผิดปกติใน Aggregate Report

🤔 **Metacognitive Prompt:** "ดู column 90% Line ใน Aggregate Report — ถ้ามันสูงกว่า Average มากกว่า 3x ให้สงสัยว่ามี outlier ที่ต้องสอบสวน"

*(source: https://jmeter.apache.org/usermanual/glossary.html)*

---

**❌ Mistake 3: สับสนระหว่าง Stress Test กับ Breakpoint Test**

> ❌ "อยากรู้ว่าระบบพังที่ load เท่าไหร่ เลยรัน stress test ที่ 1,000 users แล้วเรียกว่า 'breakpoint test'"

> ✅ Stress test ทดสอบที่ load ที่กำหนดไว้ (เกิน normal) เพื่อดู behavior ขณะ overloaded ส่วน Breakpoint test เพิ่ม load ขึ้นเรื่อยๆ จนระบบพังเพื่อหา exact breaking point

**เหตุผล:** Stress test ตอบว่า "ระบบ behave อย่างไรที่ load X?" — Breakpoint test ตอบว่า "ระบบพังที่ load เท่าไหร่?" เป็นคำถามต่างกัน ใช้ผลคนละวัตถุประสงค์

🔍 **สัญญาณที่จะสังเกตเห็น:** รัน test แล้วไม่แน่ใจว่าผล "ผ่าน" หรือ "ล้มเหลว" เพราะไม่ได้กำหนด success criteria ล่วงหน้า

🤔 **Metacognitive Prompt:** "ก่อนรัน test ทุกครั้ง ถามตัวเองว่า: 'test นี้ตอบคำถามอะไร? ถ้า pass criteria คืออะไร ถ้า fail criteria คืออะไร?' ถ้าตอบไม่ได้ — ยังไม่พร้อมรัน"

*(source: https://grafana.com/load-testing/types-of-load-testing/ — คำนิยามแยกประเภทชัดเจน)*

---

## 7. สรุปบท และ Retrieval Questions

**หยุดคิดอย่างน้อย 30 วินาทีสำหรับแต่ละข้อ เขียนคำตอบก่อนเปิดเฉลย — เขียนเหตุผลสั้นๆ 1–2 ประโยคลงกระดาษด้วย เพราะการเขียนเองช่วยให้จำได้นานกว่าการอ่านเฉลยเพียงอย่างเดียว**

---

**คำถามที่ 1 (Application-level):**
ทีมคุณสร้าง API สำหรับระบบจองคิวโรงพยาบาลที่คาดว่าจะมี 400 concurrent users ในช่วง 8:00–9:00 น. ทุกวัน และอาจมีช่วง 3,000 คนพยายามจองในเวลาเดียวกันถ้ามีการประกาศฉีดวัคซีน ระบบนี้ควรทำ performance test ประเภทไหนบ้าง เรียงตามลำดับ และ KPI threshold ที่สมเหตุสมผลสำหรับ use case นี้คืออะไร?

<details>
<summary>ดูเฉลย (เปิดหลังคิดแล้วเท่านั้น)</summary>

**เฉลย:**
1. **Smoke Test** — ยืนยัน test plan ถูกและระบบ up ก่อน
2. **Load Test** (400 users, 60 นาที) — ทดสอบ normal morning load / KPI: Error rate < 0.5% (ระบบการแพทย์ต้องการ reliability สูง), P90 < 2,000ms
3. **Spike Test** (400 → 3,000 users ใน 60 วินาที → กลับ 400) — จำลอง vaccine announcement / KPI: ระบบไม่ crash หรือ data corruption, recover ภายใน 2 นาที
4. **Soak Test** ถ้าเป็น 24/7 system — รัน 8+ ชั่วโมงเพื่อหา memory leak

สังเกต: Breakpoint Test ไม่ใช่ priority หลักสำหรับ use case นี้เพราะรู้ expected spike load แล้ว — ลงทุนเวลาที่ spike test และ soak test แทน

**Remediation:** ถ้าตอบไม่ชัด กลับอ่าน section 4.2 เรื่อง "ใช้เมื่อ / ไม่ควรใช้เมื่อ" ของแต่ละ type

</details>

---

**คำถามที่ 2 (Elaborative Interrogation — ทำไม):**
ทำไมการดู Average response time เพียงอย่างเดียวถึงทำให้ตัดสินใจผิด? จะเกิดอะไรขึ้นในชีวิตจริงถ้าทีมตัดสินว่าระบบ "ผ่าน SLA" โดยอิงจาก Average แต่ P90 สูงมาก?

<details>
<summary>ดูเฉลย (เปิดหลังคิดแล้วเท่านั้น)</summary>

**เฉลย:**
Average ถูก distort โดย outlier — ถ้า 99% ของ request ตอบ 100ms แต่ 1% ตอบ 50,000ms, Average ≈ 600ms ซึ่งอาจดูผ่าน SLA ที่ 1,000ms แต่จริงๆ มี users ที่รอนาน 50 วินาที

ในชีวิตจริง: ถ้าตัดสินว่า "ผ่าน" โดยอิงจาก Average แล้ว deploy ขึ้น production จะเจอ users บน social media บ่นว่าช้า รีวิวแย่ support ticket เพิ่ม — และทีมจะสับสนว่า "เราทดสอบแล้วนะ ทำไมยังช้า?" เพราะไม่เคยดู P90 P95 เลย

P90 แสดงว่า user 9 ใน 10 ได้ประสบการณ์อย่างไร — นั่นคือ user ส่วนใหญ่ที่เราควรออกแบบระบบเพื่อพวกเขา

**Remediation:** ถ้าตอบไม่ชัด กลับอ่าน section 4.3 เรื่อง Response Time metrics

</details>

---

**คำถามที่ 3 (Code-based Task):**
ดู test strategy ด้านล่าง แล้วบอกว่า strategy นี้มีปัญหาอะไร และจะแก้อย่างไร:

```
Test Plan:
1. รัน Breakpoint Test (เพิ่ม users จาก 1 ถึง 2,000 ต่อเนื่อง)
2. รัน Soak Test (500 users ต่อเนื่อง 12 ชั่วโมง)
3. รัน Load Test (500 users, 60 นาที)
4. รัน Smoke Test (2 users, 3 นาที)

Environment: production server (เลือกทำ low-traffic ช่วงตี 2)
KPI: Average response time < 1,000ms
```

<details>
<summary>ดูเฉลย (เปิดหลังคิดแล้วเท่านั้น)</summary>

**เฉลย — ปัญหาที่พบ:**

1. **ลำดับผิด:** ต้องเริ่มจาก Smoke Test ก่อนเสมอ ไม่ใช่ Breakpoint Test — ถ้า Smoke fail แปลว่า test plan ผิด การรัน Breakpoint ต่อจะเสียเวลาเปล่าและ result ไม่มีความหมาย

2. **Soak Test ก่อน Load Test:** ไม่ควรทำ Soak Test (12 ชั่วโมง) โดยยังไม่ผ่าน Load Test ก่อน — ถ้า Load Test fail แปลว่าระบบรับ normal load ไม่ได้ การรัน Soak test ต่อไม่มีประโยชน์และเปลืองเวลา 12 ชั่วโมง

3. **Environment ผิด:** ทำบน production server แม้จะเป็น low-traffic ช่วงตี 2 ก็ยังมีความเสี่ยง โดยเฉพาะ Breakpoint Test ที่ออกแบบมาเพื่อทำให้ระบบพัง — ควรใช้ staging

4. **KPI ไม่เหมาะสม:** ใช้แค่ Average ไม่มี P90/P95 — ดู Common Mistake #2

**ลำดับที่ถูก:**
Smoke → Load → Stress (optional) → Spike (ถ้า relevant) → Soak → Breakpoint

**Remediation:** ถ้าตอบไม่ครบ กลับอ่าน section 4.2 เรื่องลำดับ และ section 4.4 เรื่อง environment

</details>
