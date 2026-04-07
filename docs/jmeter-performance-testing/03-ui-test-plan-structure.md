# บทที่ 3: JMeter UI & Test Plan Structure

---

## ⏰ Pre-chapter Retrieval

แนะนำ: อ่านบทนี้หลังจากผ่านไปอย่างน้อย 1 วันจากบทที่ 2

ก่อนอ่านบทนี้ ลองตอบคำถาม 2 ข้อนี้ **โดยไม่เปิดดูบทที่แล้ว**:

**ข้อ 1:** จากบทที่ 2 คุณรู้ว่า official docs บอกว่า "GUI mode should only be used for creating the test script, CLI mode must be used for load testing" — ถ้าอย่างนั้น ในบทนี้ที่เราจะเรียน JMeter UI อย่างละเอียด เราเรียนไปเพื่ออะไร? GUI มีประโยชน์ตรงไหนที่ CLI ทำแทนไม่ได้?

**ข้อ 2:** JMeter ต้องการ Java เพราะเป็น pure Java application — แล้ว Test Plan ที่คุณสร้างใน GUI จะถูก save เป็นไฟล์ format อะไร? คุณเดาได้ไหมว่าทำไมถึงเป็น format นั้น?

เขียนคำตอบลงกระดาษก่อน — หยุดอย่างน้อย 30 วินาที พยายาม retrieve ก่อนเขียน และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น

---

> **เฉลยข้อ 1:** GUI มีประโยชน์ในการ **design และ debug** — คุณเห็น tree hierarchy ได้ชัดเจน, click เพื่อแก้ field ทีละ element ได้ทันที, และ run ทดสอบแบบ small scale เพื่อ verify ว่า config ถูกต้องก่อนรัน load test จริงด้วย CLI. ถ้าตอบผิดหรือคลุมเครือ: ทบทวนบทที่ 2 ส่วน "GUI mode vs CLI mode"
>
> **เฉลยข้อ 2:** Test Plan ถูก save เป็นไฟล์ **.jmx** ซึ่งเป็น XML format — เหตุผลที่เป็น XML เพราะ Java ecosystem ใช้ XML เป็น standard สำหรับ serialization configuration มาตั้งแต่ยุคแรก และ JMeter ซึ่งเป็น Java application ก็เลือก format นี้ด้วย ถ้าเดาไม่ออก: ไม่ผิด เพราะยังไม่ได้เรียน — แต่ให้จำไว้ว่า .jmx = XML

---

## ส่วนที่ 1: วัตถุประสงค์

อ่านบทนี้จบแล้วคุณจะสามารถ:

- **ระบุ** ส่วนประกอบทุกชนิดใน Test Plan tree ได้ (Thread Group, Sampler, Listener, Assertion, Config Element, Timer) และอธิบายได้ว่าแต่ละชนิดทำหน้าที่อะไรในกระบวนการ testing
- **อธิบาย** ว่า Thread Group fields แต่ละ field (Number of Threads, Ramp-Up Period, Loop Count, Duration) ส่งผลต่อพฤติกรรมของ test อย่างไร โดยยกตัวอย่างตัวเลขจริง
- **เลือก** Listener ที่เหมาะสมกับแต่ละ use case (debug vs load test จริง) และอธิบายได้ว่าทำไมถึงเลือกแบบนั้น
- **วางแผน** Test Plan structure ที่ถูกต้องสำหรับ scenario ง่ายๆ ได้ก่อนลงมือสร้างจริงในบทที่ 4

---

## ส่วนที่ 2: ทำไมต้องรู้? (Why)

ลองนึกถึงสถานการณ์นี้:

คุณเปิด JMeter GUI ครั้งแรก เห็น panel ซ้ายมีต้นไม้ (tree) อยู่ คุณ right-click สุ่มเพิ่ม element หลายอย่าง แล้วกด Run — JMeter รันบางอย่าง บางอย่างไม่รัน ผลลัพธ์แปลกๆ ออกมา คุณไม่รู้ว่าผิดตรงไหน

ปัญหานี้เกิดจากการไม่เข้าใจ **ว่าทำไม JMeter ถึงออกแบบ UI เป็น tree hierarchy** และ **element แต่ละชนิดทำงานยังไง**

JMeter ออกแบบ Test Plan เป็น tree hierarchy เพราะ:
1. **Scope inheritance** — element ลูกได้รับ config จาก element พ่อ ทำให้ไม่ต้อง copy ซ้ำ
2. **Execution order** — JMeter รัน element ตามลำดับที่เห็นใน tree จากบนลงล่าง ทำให้คาดเดาได้
3. **Modularity** — Thread Group แยกกันทำงานได้อิสระ เหมาะกับ scenario ที่มี user หลายประเภท

ถ้าคุณเข้าใจ logic ของ tree นี้ คุณจะไม่ต้องลองผิดลองถูก — คุณจะรู้ทันทีว่าต้อง add element ตรงไหนและทำไม

---

## ส่วนที่ 3: Analogy

### Test Plan เหมือน Script การแสดงละครเวที

ลองนึกถึงการแสดงละครเวทีที่มีนักแสดงหลายคน:

**Mechanism 1 — Test Plan เหมือน Script หลัก**
Script ทั้งเล่มคือ blueprint — มันไม่ "แสดง" เอง แต่เป็นที่รวมทุกอย่าง ทุกฉาก ทุกบทพูด Test Plan ก็เช่นกัน มันเป็น container หลักที่รวม Thread Groups และ elements ทั้งหมด

**Mechanism 2 — Thread Group เหมือน กลุ่มนักแสดงประเภทเดียวกัน**
ละครอาจมีกลุ่มตัวเอก, กลุ่มตัวร้าย, กลุ่มนักแสดงประกอบ — แต่ละกลุ่มทำสิ่งที่แตกต่างกัน Thread Group ก็เหมือนกัน คุณอาจมี Thread Group สำหรับ admin users และอีกอันสำหรับ regular users ที่ทำ action ต่างกัน

**Mechanism 3 — Sampler เหมือน "บทพูด" แต่ละบท**
แต่ละ sampler คือ action เดียวที่ virtual user ทำ — เหมือนบทพูดแต่ละบรรทัดที่นักแสดงต้องพูด HTTP Request Sampler คือการส่ง request ไปที่ server หนึ่งครั้ง

**Mechanism 4 — Listener เหมือน กล้องถ่ายทำที่บันทึกการแสดง**
กล้องไม่ได้มีส่วนร่วมในการแสดง แต่บันทึกทุกอย่างไว้ Listener ก็ไม่ได้ส่ง request แต่รวบรวมผลลัพธ์ไว้แสดงและ save

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:** threads ทำงานทีละตัวตามลำดับเหมือนนักแสดงที่รอฉากก่อนหน้าจบก่อน — จริงๆ threads ทำงาน **concurrent** พร้อมกัน ไม่ใช่ sequential — thread ที่ 2 ไม่ต้องรอ thread ที่ 1 ทำเสร็จก่อน ทำให้ load บน server เป็น simultaneous จริง นี่คือเหตุผลที่ 100 threads ≠ 100 requests ที่ส่งทีละอัน แต่คือ requests ที่ส่งพร้อมกัน

---

## ส่วนที่ 4: เนื้อหาหลัก

### 4.1 Test Plan คืออะไร — โครงสร้างรวม

เมื่อคุณเปิด JMeter ครั้งแรก สิ่งที่เห็นในส่วน Tree Panel (panel ซ้าย) คือ "Test Plan" อยู่บนสุด — นั่นคือ root node ของทุกอย่าง

Official docs ระบุว่า:

> "A minimal test will consist of the Test Plan, a Thread Group and one or more Samplers."
> *(source: https://jmeter.apache.org/usermanual/test_plan.html)*

แปลว่า structure ที่เล็กที่สุดที่จะรัน test ได้คือ:

```
Test Plan
└── Thread Group
    └── HTTP Request Sampler (อย่างน้อย 1 ตัว)
```

ในทางปฏิบัติ Test Plan ของคุณจะซับซ้อนกว่านี้ — แต่ให้จำ minimal structure นี้ไว้เป็น mental model

JMeter รัน request ตามลำดับที่เห็นใน tree จากบนลงล่าง:

> "JMeter sends requests in the order that they appear in the tree."
> *(source: https://jmeter.apache.org/usermanual/build-web-test-plan.html)*

---

### 4.2 Thread Group — หัวใจของ Test Plan

Thread Group เป็น element ที่สำคัญที่สุดเพราะมันกำหนดว่า "จะจำลองผู้ใช้กี่คน ทำอะไร นานแค่ไหน"

> "The thread group element controls the number of threads JMeter will use to execute your test."
> *(source: https://jmeter.apache.org/usermanual/test_plan.html)*

> "The Thread Group tells JMeter the number of users you want to simulate, how often the users should send requests, and how many requests they should send."
> *(source: https://jmeter.apache.org/usermanual/build-web-test-plan.html)*

Thread Group มี field สำคัญดังนี้:

**Number of Threads (Users)**
คือจำนวน virtual users ที่ JMeter จะจำลอง แต่ละ thread ทำงานอิสระจากกัน — ถ้าตั้ง 100 threads JMeter จะมี 100 virtual users รันพร้อมกัน

**Ramp-Up Period (seconds)**
คือเวลา (หน่วยวินาที) ที่ JMeter ใช้ในการ start thread ทั้งหมด official docs ให้ตัวอย่างที่ชัดเจนมาก:

> "If 10 threads are used, and the ramp-up period is 100 seconds, then JMeter will take 100 seconds to get all 10 threads up and running."
> *(source: https://jmeter.apache.org/usermanual/test_plan.html)*

แปลว่าถ้า Ramp-Up Period = 100 วินาทีและมี 10 threads → JMeter จะ start thread ใหม่ทุกๆ 10 วินาที (100 ÷ 10 = 10 วินาทีต่อ thread)

**Loop Count**
> "This property tells JMeter how many times to repeat your test."
> *(source: https://jmeter.apache.org/usermanual/build-web-test-plan.html)*

ถ้าตั้ง Loop Count = 5 แต่ละ thread จะวน test ครบ 5 รอบแล้วหยุด ถ้าเลือก "Infinite" thread จะวนไม่หยุดจนกว่าคุณจะกด Stop หรือหมดเวลาที่กำหนด

**Duration (seconds)**
ใช้คู่กับ Infinite loop — กำหนดว่าให้รัน test นานกี่วินาทีแล้วหยุดเอง เหมาะกับ soak test ที่ต้องการรัน 1 ชั่วโมง = Duration 3600 วินาที

---

⏸ **Self-check:** ก่อนอ่านต่อ ลองตอบ: ถ้าคุณตั้ง Ramp-Up Period = 0 จะเกิดอะไรขึ้น? (นึกถึง formula ที่คำนวณได้จากตัวอย่าง 10 threads / 100 วินาที ข้างบน) เขียนคำตอบลงกระดาษก่อน — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น — แล้วค่อยอ่านต่อ

> เฉลย: Ramp-Up Period = 0 หมายความว่า JMeter จะ start ทุก thread **พร้อมกันทันที** ไม่มีการทยอย start ตามลำดับ — ผลคือเกิด load spike ขนาดใหญ่ในวินาทีแรก ซึ่งอาจทำให้ server รับไม่ไหวและผลการทดสอบ skewed (ไม่สะท้อนการใช้งานจริงที่ user ทยอยเข้า)

---

### 4.3 Element ประเภทต่างๆ ใน Test Plan

**Samplers — คนส่ง Request**

> "Samplers tell JMeter to send requests to a server and wait for a response. They are processed in the order they appear in the tree."
> *(source: https://jmeter.apache.org/usermanual/test_plan.html)*

Sampler คือ element เดียวในบรรดาทั้งหมดที่ "ทำงาน" จริงๆ — มันส่ง request และรอ response ชนิดที่ใช้บ่อยสุดคือ HTTP Request Sampler

**Listeners — คนเก็บผล**

> "Listeners provide access to the information JMeter gathers about the test cases while JMeter runs."
> *(source: https://jmeter.apache.org/usermanual/test_plan.html)*

> "A listener is a component that shows the results of the samples. The results can be shown in a tree, tables, graphs or simply written to a log file."
> *(source: https://jmeter.apache.org/usermanual/listeners.html)*

Listener ไม่ส่ง request เอง แต่ "ฟัง" ผลลัพธ์จาก Sampler แล้วแสดงหรือบันทึกไว้

**Assertions — คนตรวจสอบ**

> "Assertions allow you to assert facts about responses received from the server being tested."
> *(source: https://jmeter.apache.org/usermanual/test_plan.html)*

Assertion เปรียบได้กับ automated checker — ถ้า server ส่ง response 200 แต่ body ไม่มีคำว่า "success" Assertion จะ mark request นั้นว่า fail

**Configuration Elements — คนตั้งค่า**

> "A configuration element works closely with a Sampler. Although it does not send requests, it can add to or modify requests."
> *(source: https://jmeter.apache.org/usermanual/test_plan.html)*

Config Element เช่น HTTP Request Defaults หรือ HTTP Header Manager ทำงานก่อน Sampler จะส่ง request — มันเพิ่มหรือแก้ไข request ตาม config ที่ตั้งไว้

**Timers — คนหน่วงเวลา**

> "A timer will cause JMeter to delay a certain amount of time before each sampler which is in its scope."
> *(source: https://jmeter.apache.org/usermanual/test_plan.html)*

Timer จำลอง "think time" ของผู้ใช้จริง — ผู้ใช้ไม่ได้คลิกปุ่มถัดไปทันทีหลัง page โหลด แต่ใช้เวลาอ่านหน้าก่อน การใส่ Timer ทำให้ผล test สะท้อนความเป็นจริงมากขึ้น

**Controllers — คนกำหนดเส้นทาง**

> "JMeter has two types of Controllers: Samplers and Logical Controllers. These drive the processing of a test."
> *(source: https://jmeter.apache.org/usermanual/test_plan.html)*

Logical Controllers เช่น If Controller, Loop Controller ใช้กำหนด logic ว่า Sampler ไหนรัน เมื่อไหร่ และกี่ครั้ง

---

### 4.3.1 ลำดับการทำงานของ Elements (Execution Order)

เมื่อ Sampler ถูก execute JMeter ประมวลผล elements ที่อยู่ใน scope เดียวกันตามลำดับนี้เสมอ:

```
Pre-Processors → Timers → Sampler → Post-Processors → Assertions → Listeners
```

**ตัวอย่างเพื่อให้เข้าใจ:** ถ้า Thread Group มี Uniform Random Timer (Pre-Processor), HTTP Request Sampler, JSON Extractor (Post-Processor), Response Assertion, และ Aggregate Report (Listener) — JMeter จะ:
1. รัน Timer ก่อน (หน่วงเวลา)
2. ส่ง HTTP Request (Sampler)
3. ดึง JSON value จาก response (Post-Processor)
4. ตรวจว่า response ผ่าน Assertion ไหม
5. บันทึกผลลัพธ์ใน Aggregate Report (Listener)

**ทำไมต้องรู้?** เพราะการวาง element ผิดที่ทำให้ logic ผิด เช่น ถ้าวาง JSON Extractor ก่อน Sampler — ยังไม่มี response ให้ดึงค่า, ถ้าวาง Assertion ก่อน Post-Processor — ยังไม่มี extracted variable ให้ assert

> *(source: https://jmeter.apache.org/usermanual/test_plan.html — "Elements of a Test Plan")*

---

### 4.4 JMeter UI Panels — รู้จักหน้าต่าง

JMeter GUI แบ่งเป็น 3 ส่วนหลัก:

**Left Pane (Tree Panel)**
แสดง Test Plan structure ทั้งหมดเป็น tree hierarchy ที่คุณ expand/collapse ได้ — นี่คือ "ภาพรวม" ของ test ทั้งหมด การ click element ใน tree จะทำให้ Right Pane แสดง config ของ element นั้น

**Right Pane (Configuration Panel)**
แสดง field ที่แก้ไขได้ของ element ที่ selected อยู่ — เมื่อ click Thread Group จะเห็น fields ของ Thread Group เมื่อ click HTTP Request จะเห็น fields ของ HTTP Request

**Log Viewer (ด้านล่าง)**
แสดง log messages real-time ระหว่างรัน test — ถ้ามี error หรือ warning จะปรากฏที่นี่ก่อน มีประโยชน์มากตอน debug

**Menu Bar (ด้านบน)**
มี menu File (save/open .jmx), Edit (add/remove elements), Run (start/stop/shut down test), Options (language, log level) ฯลฯ

---

### 4.5 Listener ที่เหมาะกับแต่ละ Use Case

ไม่ใช่ทุก Listener เหมาะกับทุกสถานการณ์ — เลือกผิดทำให้ JMeter ช้าและผลไม่น่าเชื่อถือ

**View Results Tree**
- **เหมาะกับ:** Debug phase — ดู request/response รายตัว
- **ไม่เหมาะกับ:** Load test จริง — render ทุก request ทำให้ JMeter ใช้ memory สูงมาก

> ⚠️ **ควรเลี่ยง:** ใช้ View Results Tree ตอนรัน load test จริง — เพราะ render ทุก request ทำให้ JMeter ช้ามาก และอาจทำให้ผลการทดสอบ skewed เพราะ JMeter เองกลายเป็น bottleneck

Official docs เตือนชัดเจน:

> "Don't use 'View Results Tree' or 'View Results in Table' listeners during the load test, use them only during scripting phase to debug your scripts."
> *(source: https://jmeter.apache.org/usermanual/best-practices.html)*

**Aggregate Report**
- **เหมาะกับ:** Load test จริง — แสดงสถิติสรุป (Average, 90th Percentile, Throughput, Error%)
- **ข้อดี:** ใช้ memory น้อยกว่า View Results Tree มาก เพราะ aggregate samples ที่มี elapsed time เดียวกัน

> "samples with the same elapsed time are aggregated. Less memory is now needed."
> *(source: https://jmeter.apache.org/usermanual/listeners.html)*

**Summary Report**
- **เหมาะกับ:** Load test ที่ต้องการประหยัด memory สูงสุด
- **ข้อเสีย:** ไม่มี Median และ 90th Percentile column ซึ่งสำคัญสำหรับการวิเคราะห์

> "Simpler (lower memory) version of Aggregate Report. Excludes the Median and 90% columns, which are expensive in memory terms"
> *(source: https://jmeter.apache.org/api/org/apache/jmeter/visualizers/SummaryReport.html)*

**สรุปการเลือก Listener:**

| Use Case | แนะนำ Listener | เหตุผล |
|----------|---------------|--------|
| Debug / ตรวจ request-response รายตัว | View Results Tree | เห็น detail ครบ |
| Load test — ต้องการ percentile stats | Aggregate Report | memory พอดี + มี 90th percentile |
| Load test — memory จำกัดมาก | Summary Report | เบาที่สุด แต่ขาด percentile |
| Save ผลเป็น file สำหรับวิเคราะห์ภายหลัง | Simple Data Writer (CSV) | CSV เล็กกว่า XML มาก |

> "CSV files are much smaller than XML files, so use CSV if you are generating lots of samples."
> *(source: https://jmeter.apache.org/usermanual/listeners.html)*

---

⏸ **Self-check (Bloom's L3 — Application):** คุณกำลังออกแบบ Test Plan สำหรับ API ที่ต้องทดสอบว่ารับ 500 concurrent users ได้ไหม คุณจะ add Listener ชนิดไหน และวางไว้ที่ไหนใน tree hierarchy? (นึกถึง scope inheritance ที่อธิบายไว้ใน Why section) เขียนคำตอบลงกระดาษก่อน — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น — แล้วค่อยอ่านต่อ

> เฉลย: ใช้ **Aggregate Report** (ไม่ใช่ View Results Tree) เพราะนี่คือ load test จริง ไม่ใช่ debug phase — วาง Listener ไว้ใต้ **Thread Group** เพื่อให้ scope ครอบคลุมทุก Sampler ใน Thread Group นั้น หรือใต้ **Test Plan** ถ้าต้องการรวบรวมผลจากทุก Thread Group พร้อมกัน

---

## ส่วนที่ 5: ตัวอย่าง 3 ระดับ

### ตัวอย่าง Beginner: ทำความเข้าใจ JMeter UI ผ่าน structure จริง

**คำอธิบาย UI (แทน screenshot):**

เมื่อเปิด JMeter ครั้งแรก คุณจะเห็น:

```
[Left Pane — Tree Panel]          [Right Pane — Config Panel]
┌─────────────────────┐          ┌────────────────────────────┐
│ 📋 Test Plan         │──click──→│ Name: Test Plan            │
│   └─ Thread Group   │          │ Comments: (text area)       │
│                     │          │ ☐ Functional Test Mode      │
│                     │          │ ☐ Add directory...          │
└─────────────────────┘          └────────────────────────────┘
```

เมื่อ click "Thread Group" ใน tree จะเห็น Right Pane เปลี่ยนเป็น:

```
[Right Pane เมื่อ Thread Group selected]
┌──────────────────────────────────────────────┐
│ Name: Thread Group                           │
│ Comments:                                    │
│                                              │
│ Action to be taken after a Sampler error:    │
│ ● Continue  ○ Start Next Thread Loop         │
│ ○ Stop Thread  ○ Stop Test  ○ Stop Test Now  │
│                                              │
│ Thread Properties                            │
│ Number of Threads (users): [1        ]       │
│ Ramp-Up Period (seconds):  [1        ]       │
│ Loop Count:  ☐ Infinite   [1        ]       │
│                                              │
│ Same user on each iteration ☐               │
│ Delay Thread creation until needed ☐        │
│ Specify Thread lifetime                      │
│   Duration (seconds):     [          ]      │
│   Startup delay (seconds):[          ]      │
└──────────────────────────────────────────────┘
```

**JMX XML ที่ตรงกับ structure ข้างบน:**

```xml
<!-- # label: JMX snippet — ยังไม่ได้ทดสอบแบบ standalone; ต้องใช้กับ JMeter 5.6.3 -->
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.6.3">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="Test Plan">
      <boolProp name="TestPlan.functional_mode">false</boolProp>
    </TestPlan>
    <hashTree>
      <!-- Thread Group: 1 virtual user, Ramp-Up 1 วินาที, Loop 1 ครั้ง -->
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Thread Group">
        <intProp name="ThreadGroup.num_threads">1</intProp>
        <intProp name="ThreadGroup.ramp_time">1</intProp>
        <boolProp name="ThreadGroup.same_user_on_next_iteration">true</boolProp>
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <intProp name="LoopController.loops">1</intProp>
        </elementProp>
      </ThreadGroup>
      <hashTree>
        <!-- Samplers, Listeners, etc. จะอยู่ใน hashTree นี้ -->
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

**สังเกตสิ่งสำคัญในโครงสร้าง XML:**
- `<hashTree>` ที่ซ้อนกันคือสิ่งที่สร้าง hierarchy ใน tree — ยิ่ง nest ลึก ยิ่งเป็น child element
- `<ThreadGroup>` อยู่ใน `<hashTree>` ของ `<TestPlan>`
- Samplers จะอยู่ใน `<hashTree>` ของ `<ThreadGroup>`

---

### ตัวอย่าง Intermediate: Thread Group config สำหรับ Healthcare API

**Scenario:** ทีมพัฒนา Patient Portal API ของโรงพยาบาลต้องทดสอบว่า API รับ load ได้เท่าไหร่ในช่วง morning peak hour (8:00-9:00 น. ที่แพทย์และพยาบาลเข้าระบบพร้อมกัน)

**Target:** จำลอง 100 healthcare staff เข้าระบบพร้อมกัน โดย ramp up ใน 60 วินาที (สมจริงกว่าการ start พร้อมกัน) รันนาน 5 นาที

**Thread Group configuration:**

| Field | ค่า | เหตุผล |
|-------|-----|--------|
| Number of Threads (Users) | 100 | จำนวน staff ที่คาดว่า concurrent ใน peak hour |
| Ramp-Up Period | 60 | ให้ thread ทยอย start — 100÷60 ≈ 1.67 thread/วินาที สะท้อนการ login ทีละคน |
| Loop Count | ☐ Infinite | เพราะต้องการรันตามเวลา ไม่ใช่ตามจำนวนรอบ |
| Duration | 300 | 5 นาที = 300 วินาที |

**XML snippet สำหรับ config นี้:**

```xml
<!-- # label: JMX snippet — ยังไม่ได้ทดสอบแบบ standalone; ต้องการ JMeter 5.6.3 -->
<ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup"
             testname="Healthcare Staff - Peak Hour">
  <intProp name="ThreadGroup.num_threads">100</intProp>
  <intProp name="ThreadGroup.ramp_time">60</intProp>
  <boolProp name="ThreadGroup.scheduler">true</boolProp>
  <stringProp name="ThreadGroup.duration">300</stringProp>
  <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
  <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
    <boolProp name="LoopController.continue_forever">true</boolProp>
    <intProp name="LoopController.loops">-1</intProp>
  </elementProp>
</ThreadGroup>
```

**ทำไม Ramp-Up 60 วินาทีถึงสำคัญ:** ถ้าตั้ง Ramp-Up = 0 แพทย์ 100 คนจะ login พร้อมกันในวินาทีเดียว — ไม่สะท้อนความเป็นจริง และ server อาจ spike ผิดปกติ ทำให้ผลการทดสอบ misleading

---

### ตัวอย่าง Advanced: Multiple Thread Groups สำหรับ User Types ต่างกัน

**Scenario:** ระบบ e-learning platform มี user 3 ประเภทที่ทำ action ต่างกัน:
- **Student (80%):** Browse course, watch video, submit quiz
- **Instructor (15%):** Upload content, review submissions
- **Admin (5%):** Generate reports, manage users

**Test Plan structure:**

```
Test Plan
├── HTTP Request Defaults         ← Config ส่วนกลาง (base URL)
├── HTTP Cookie Manager           ← Cookie จัดการ session (global scope)
│
├── Thread Group: Students        ← 800 threads, Ramp-Up 120s
│   ├── HTTP Request: GET /courses
│   ├── HTTP Request: GET /videos/{id}
│   ├── Constant Timer: 5000ms    ← Think time จำลองการดู video
│   └── HTTP Request: POST /quiz/submit
│
├── Thread Group: Instructors     ← 150 threads, Ramp-Up 60s
│   ├── HTTP Request: POST /content/upload
│   └── HTTP Request: GET /submissions
│
├── Thread Group: Admins          ← 50 threads, Ramp-Up 30s
│   ├── HTTP Request: GET /reports/generate
│   └── HTTP Request: GET /admin/users
│
└── Aggregate Report              ← รวบรวมผลจากทุก Thread Group
    └── (save to results.csv)
```

**ข้อดีของ Multiple Thread Groups:**
- กำหนด load profile ต่างกันสำหรับ user แต่ละประเภท
- ดูผลแยกตาม Thread Group ได้ใน Aggregate Report
- เพิ่ม/ลด thread จาก user type ไหนก็ได้โดยไม่กระทบอื่น

**Trade-off ที่ต้องรู้:**
- Thread Group ทำงาน parallel กันโดย default — ถ้าต้องการให้รัน sequential ต้องเปิด "Run Thread Groups consecutively" ใน Test Plan settings
- ยิ่ง Thread Group มาก ยิ่ง monitor ยากขึ้น — ถ้าระบบช้า ต้องระบุให้ได้ว่า Thread Group ไหนเป็น culprit

---

## ส่วนที่ 6: Common Mistakes

### ❌ Mistake 1: ใส่ Listener ผิดตำแหน่ง ทำให้ไม่เก็บผล

**แบบผิด:** เพิ่ม Aggregate Report ไว้ใน Test Plan level แต่วาง **ข้างนอก** Thread Group แทนที่จะให้ครอบคลุม sampler

```
Test Plan
├── Aggregate Report    ← ✓ อยู่ที่นี่ถูกต้อง (scope = Test Plan ทั้งหมด)
└── Thread Group
    ├── HTTP Request 1
    └── HTTP Request 2  ← ⚠️ ถ้า Listener อยู่หลัง HTTP Request 2
                            ใน tree ลำดับเดียวกัน — บางกรณี scope ไม่ครอบคลุม
```

**แบบถูก:** เข้าใจ scope rule ของ JMeter — Listener ที่อยู่ใน parent element จะเก็บผลจาก child ทั้งหมด ถ้าวาง Listener ใน Test Plan level จะครอบทุก Thread Group ถ้าวางใน Thread Group จะครอบเฉพาะ Thread Group นั้น

🔍 **เหตุผล:** Listener ทำงานตาม scope hierarchy เหมือน Config Element — วางผิดที่ = เก็บผลไม่ครบหรือเก็บซ้ำ

🤔 **วิธีตรวจสอบ:** รัน test ด้วย 1 thread ก่อน แล้วดูว่า Listener แสดงผลหรือเปล่า ถ้าไม่มีผลเลย = scope ผิด

*(source: https://jmeter.apache.org/usermanual/test_plan.html — scope hierarchy)*

---

### ❌ Mistake 2: ตั้ง Ramp-Up Period = 0 ทำให้เกิด spike ทันที

**แบบผิด:**
```
Number of Threads: 500
Ramp-Up Period: 0         ← ผิด — thread ทั้ง 500 start พร้อมกัน
Loop Count: 10
```

**แบบถูก:**
```
Number of Threads: 500
Ramp-Up Period: 120       ← 2 นาที — ทยอย start ~4 threads/วินาที
Loop Count: 10
```

🔍 **เหตุผล:** Real users ไม่ได้เข้าระบบพร้อมกันพอดีทุกคน — Ramp-Up Period = 0 สร้าง artificial spike ที่ไม่สะท้อนการใช้งานจริง ผลการทดสอบ (response time, error rate) จะดูแย่กว่าความเป็นจริง

🤔 **Guideline:** ตั้ง Ramp-Up Period ให้เท่ากับ "เวลาที่ user ทยอยเข้าระบบในชีวิตจริง" เช่น ถ้า peak hour ใช้เวลา 10 นาทีในการ login ก็ตั้ง Ramp-Up = 600 วินาที

*(source: https://jmeter.apache.org/usermanual/test_plan.html — Ramp-Up Period example)*

---

### ❌ Mistake 3: สับสนระหว่าง Loop Count vs Duration

**แบบผิด:** ต้องการรัน test 10 นาที แต่ตั้ง Loop Count = 10 โดยคิดว่า = 10 นาที

**แบบถูก:**
- ถ้าต้องการรัน **ตามเวลา** → เลือก Infinite loop + ตั้ง Duration = 600 (วินาที)
- ถ้าต้องการรัน **ตามจำนวนรอบ** → ตั้ง Loop Count ให้ตรง + ปล่อย Duration ว่าง

🔍 **เหตุผล:** Loop Count กำหนดจำนวนรอบ ไม่ใช่เวลา — ถ้า response time ของแต่ละ request เปลี่ยน เวลารัน total ก็เปลี่ยนตาม แต่ถ้าใช้ Duration จะรันนานตามที่กำหนดเสมอ

🤔 **เมื่อใช้ Duration:** เหมาะสำหรับ soak test หรือ endurance test ที่ต้องการ run นานแน่นอน เช่น "รัน 1 ชั่วโมงแล้วดูว่า memory leak ไหม"

*(source: https://jmeter.apache.org/usermanual/build-web-test-plan.html — Loop Count field)*

---

## ส่วนที่ 7: สรุปบท

### Retrieval Questions — ตอบก่อนดูเฉลย

หยุดอ่าน เขียนคำตอบลงกระดาษก่อน อย่างน้อย 30 วินาทีต่อข้อ — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น ก่อนดูเฉลย

**ข้อ 1:** JMeter official docs บอกว่า Test Plan ขั้นต่ำต้องมีอะไรบ้าง? และทำไม element ทั้งสามนั้นถึงขาดกันไม่ได้?

**ข้อ 2:** ถ้าคุณมี Thread Group ที่ตั้งค่า Number of Threads = 50, Ramp-Up = 25 วินาที — JMeter จะ start thread ใหม่ทุกกี่วินาที? และถ้าเปลี่ยน Ramp-Up เป็น 0 จะเกิดอะไรขึ้น?

**ข้อ 3:** คุณได้รับ task ให้ debug ว่า API ส่ง response body อะไรกลับมา แต่อีก task คือรัน load test จริงกับ 1,000 users — คุณจะเลือก Listener ต่างกันอย่างไรสำหรับสองงานนี้ และเพราะอะไร?

---

**เฉลย**

> **ข้อ 1:** "A minimal test will consist of the Test Plan, a Thread Group and one or more Samplers" — Test Plan คือ container ที่ขาดไม่ได้, Thread Group กำหนดว่ามี virtual users กี่คนและทำงานอย่างไร, Sampler คือ element เดียวที่ส่ง request จริง — ขาด Sampler = ไม่มีการทดสอบ, ขาด Thread Group = ไม่มี virtual user ที่จะรัน Sampler

> **ข้อ 2:** 25 ÷ 50 = 0.5 วินาทีต่อ thread (start thread ใหม่ทุกครึ่งวินาที) — ถ้า Ramp-Up = 0 thread ทั้ง 50 จะ start พร้อมกันทันที เกิด spike load ทันที

> **ข้อ 3:** Debug → **View Results Tree** (เห็น request/response detail ทุก field) | Load test 1,000 users → **Aggregate Report** (เบากว่า, มี percentile metrics ที่ต้องการ) — ห้ามใช้ View Results Tree ตอน load test เพราะ official docs เตือนว่า "don't use during load test, use only during scripting phase"

**ถ้าตอบผิด:** ทบทวน section 4.2 (Thread Group fields) และ 4.5 (Listener selection guide) แล้วลองอธิบายเองว่าเข้าใจผิดตรงไหน — การระบุจุดที่เข้าใจผิดได้แม่นยำกว่าการอ่านซ้ำตรงๆ

**ถ้าตอบข้อ 3 ได้ แต่อธิบายเหตุผลไม่ได้:** ลองทำ Feynman Technique — อธิบาย scope inheritance ของ Listener ให้เพื่อนฟังโดยไม่ดูโน้ต ถ้าติดตรงไหน = ยังไม่เข้าใจจริง
