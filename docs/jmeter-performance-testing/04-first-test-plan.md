# บทที่ 4: สร้าง Test Plan แรกสำหรับ HTTP API

---

## ⏰ Pre-chapter Retrieval

แนะนำ: อ่านบทนี้หลังจากผ่านไปอย่างน้อย 1 วันจากบทที่ 3

ก่อนอ่านบทนี้ ลองตอบ 2 คำถามนี้ **โดยไม่เปิดดูบทที่แล้ว**:

**ข้อ 1:** จากบทที่ 3 คุณรู้ว่า Thread Group มี 4 fields หลัก — Number of Threads, Ramp-Up Period, Loop Count, Duration — ถ้าคุณต้องการรัน test นาน 3 นาทีด้วย 200 virtual users โดย start ทีละคนใน 2 นาทีแรก คุณจะตั้งค่า field แต่ละ field อย่างไร?

**ข้อ 2:** Official docs บอกว่า "A minimal test will consist of the Test Plan, a Thread Group and one or more Samplers" — แล้วในบทนี้เราจะเพิ่ม element อีกหลายชนิด (Header Manager, Cookie Manager, HTTP Request Defaults) เพื่ออะไร? ทำไม minimal structure ถึงยังไม่พอสำหรับการทดสอบจริง?

เขียนคำตอบลงกระดาษก่อน — หยุดอย่างน้อย 30 วินาที พยายาม retrieve ก่อนเขียน และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น

---

> **เฉลยข้อ 1:** Number of Threads = 200, Ramp-Up Period = 120 (2 นาที = 120 วินาที), Loop Count = ☐ Infinite, Duration = 180 (3 นาที = 180 วินาที) — 120÷200 = 0.6 วินาทีต่อ thread ใหม่ ถ้าตอบผิด: ทบทวน section 4.2 ของบทที่ 3 โดยเฉพาะ formula Ramp-Up ÷ Threads = เวลาต่อ thread
>
> **เฉลยข้อ 2:** Minimal structure ทำงานได้ แต่ไม่สะท้อน real user — Real users ส่ง headers (Content-Type, Authorization), มี cookies (session tracking), และ API มักมี base URL เดียวกันทุก endpoint การไม่มี Config Elements ทำให้ต้อง duplicate ค่าในทุก Sampler และ test ไม่สะท้อนการใช้งานจริง ถ้าตอบไม่ออก: ไม่ผิด บทนี้จะอธิบายเรื่องนี้โดยละเอียด

---

## ส่วนที่ 1: วัตถุประสงค์

อ่านบทนี้จบแล้วคุณจะสามารถ:

- **สร้าง** Test Plan สำหรับ HTTP GET และ POST request ได้ตั้งแต่ต้นโดยกรอก field ถูกทุก field
- **อธิบาย** ว่า HTTP Request Sampler แต่ละ field (Server, Port, Protocol, Path, Method, Body Data) หมายถึงอะไรและส่งผลอย่างไร
- **เลือก** ว่าสถานการณ์ไหนต้องใช้ HTTP Header Manager และ HTTP Cookie Manager
- **ออกแบบ** Test Plan ที่ใช้ HTTP Request Defaults เพื่อลด duplication และง่ายต่อการบำรุงรักษา
- **อ่าน** ผลลัพธ์จาก View Results Tree และบอกได้ว่า request นั้นสำเร็จหรือล้มเหลวและเพราะอะไร
- **ประเมิน** tradeoff ระหว่าง Test Plan แบบง่ายกับแบบที่มี setup/teardown สำหรับ production-grade testing

---

## ส่วนที่ 2: ทำไมต้องรู้? (Why)

ลองนึกถึงสถานการณ์จริง:

คุณเพิ่ง deploy API endpoint ใหม่ที่ให้ผู้ใช้ login และดู profile — ก่อน deploy คุณทดสอบด้วย Postman แล้วผ่าน แต่พอเปิด production จริง มีผู้ใช้ 500 คน login พร้อมกัน response time พุ่งขึ้นจาก 200ms เป็น 8 วินาที

ปัญหาคือคุณ "ทดสอบถูกต้อง" ด้วย Postman แต่ทดสอบ "กับ 1 คน" เท่านั้น

HTTP Request Sampler ใน JMeter แก้ปัญหานี้ — มันเป็นสิ่งเดียวกับที่ Postman ทำ แต่ทำพร้อมกันได้หลาย thread มันส่ง request จริงไปที่ server จริง รอ response จริง แล้ว JMeter รวบรวมผลจากทุก virtual user ไว้ให้วิเคราะห์

บทนี้สำคัญเพราะ HTTP Request Sampler คือ "กล้ามเนื้อหลัก" ของ JMeter — element อื่นๆ ทั้งหมดที่เรียนมา (Thread Group, Listener, Config Elements) ล้วนทำงานเพื่อสนับสนุน Sampler ตัวนี้

---

## ส่วนที่ 3: Analogy

### HTTP Request Sampler เหมือน นักสืบที่ถูกส่งไปทำภารกิจ

ลองนึกถึง Thread Group เป็น "หัวหน้าฝ่ายสืบสวน" ที่มีทีมนักสืบ 100 คน:

**Mechanism 1 — HTTP Request Sampler เหมือน "คำสั่งภารกิจ" ที่ส่งให้นักสืบแต่ละคน**
คำสั่งระบุว่า "ไปที่บ้านเลขที่ X (Server + Path), เคาะประตูด้วยวิธีนี้ (Method: GET/POST), นำเอกสารนี้ไปด้วย (Body Data), แล้วรอรับคำตอบ (wait for response)" — นักสืบทุกคนทำตาม spec เดียวกัน แต่ทำพร้อมกัน

**Mechanism 2 — HTTP Header Manager เหมือน "บัตรประจำตัวและเอกสารแนะนำตัว"**
ก่อนนักสืบเข้าไปพูดคุย ต้องแสดงตัวตนว่าเป็นใคร (Authorization header) และประกาศว่าจะพูดภาษาอะไร (Content-Type: application/json) — ถ้าไม่แสดง เจ้าของบ้านอาจปฏิเสธไม่รับ

**Mechanism 3 — HTTP Cookie Manager เหมือน "ป้ายสมาชิกที่ติดไว้"**
หลัง login ครั้งแรก server ออก "ป้ายสมาชิก" (cookie) ให้ — นักสืบต้องพกป้ายนี้ไปทุกครั้งที่กลับมา ไม่งั้นจะถูกมองว่าเป็นคนแปลกหน้าและต้อง login ใหม่ทุกครั้ง

**Mechanism 4 — HTTP Request Defaults เหมือน "ที่อยู่ base" ที่นักสืบทุกคนรู้**
ถ้าทีมสืบสวนต้องไปหลายบ้านในตึกเดียวกัน แทนที่จะบอกชื่อตึกในทุกคำสั่ง หัวหน้าประกาศครั้งเดียวว่า "ตึกทั้งหมดอยู่ที่ถนน api.example.com" — นักสืบแค่รู้ "ห้องที่ต้องไป" (/users, /products) โดยไม่ต้องจำ address เต็ม

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:** Cookie Manager เหมือน login ให้เสร็จแล้วถ่าย cookie อัตโนมัติ — ผิด Cookie Manager แค่ **จัดเก็บและส่ง cookies อัตโนมัติ** ตาม HTTP standard เท่านั้น มันไม่ได้ login ให้คุณ คุณต้องมี Login Sampler ก่อน แล้ว Cookie Manager จะเก็บ session cookie ที่ได้จาก login response นั้นไว้ใช้ต่อ — ถ้าลืมใส่ Login Sampler ผู้ใช้ทุกคนจะ call API ในฐานะ anonymous user โดยไม่รู้ตัว

---

## ส่วนที่ 4: เนื้อหาหลัก

### 4.1 HTTP Request Sampler — Field ทีละ Field

HTTP Request Sampler คือ element ที่ส่ง HTTP request จริงไปยัง server official docs อธิบายว่า:

> "lets you send an HTTP/HTTPS request to a web server. It also lets you control whether or not JMeter parses HTML files for images and other embedded resources and sends HTTP requests to retrieve them."
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

**Server Name or IP**
> "Domain name or IP address of the web server"
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

ใส่แค่ domain ไม่รวม protocol — เช่น `api.example.com` หรือ `192.168.1.100` ไม่ใช่ `https://api.example.com`

**Port Number**
> "Port the web server is listening to. Default: 80"
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

ถ้าใช้ HTTPS มักเป็น 443 ถ้าใช้ HTTP เป็น 80 ถ้า server รันบน port อื่น (เช่น dev server ที่ 8080) ต้องใส่ให้ตรง

**Protocol**
ใส่ `https` หรือ `http` — ถ้าปล่อยว่างไว้ JMeter ใช้ http ซึ่งไม่ encrypted ควรใช้ https เสมอสำหรับ endpoint จริง

**Path**
> "The path to resource (for example, /servlets/myServlet)"
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

คือส่วน URL หลัง domain — เช่น `/api/v1/users` หรือ `/posts/123` ต้องขึ้นต้นด้วย `/`

**HTTP Method**
> "GET, POST, HEAD, TRACE, OPTIONS, PUT, DELETE, PATCH"
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

เลือกตาม API specification — GET สำหรับดูข้อมูล, POST สำหรับสร้าง, PUT/PATCH สำหรับแก้ไข, DELETE สำหรับลบ

**Body Data**
ใช้สำหรับ POST/PUT/PATCH request ที่ต้องส่ง data ไปด้วย — ถ้า API ใช้ JSON body ให้ใส่ JSON ที่นี่ เช่น:
```json
{
  "username": "testuser",
  "password": "testpass123"
}
```

**Parameters (แทน Body Data)**
สำหรับ query string หรือ form data ใช้ tab "Parameters" แทน — JMeter จะ encode ให้อัตโนมัติ

---

### 4.2 HTTP Header Manager — ทำไมต้องใส่

Header Manager เพิ่ม HTTP headers เข้าไปใน request ก่อนส่ง:

> "Enables configuration of HTTP headers to send with requests"
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

**ทำไมต้องมี:** API ส่วนใหญ่ต้องการ headers เพื่อ:
1. บอก server ว่า body เป็น format อะไร → `Content-Type: application/json`
2. ยืนยัน identity → `Authorization: Bearer <token>` หรือ `Authorization: Basic <encoded>`
3. บอก format ที่ client ต้องการรับ → `Accept: application/json`

ถ้าไม่ส่ง Content-Type สำหรับ POST request ที่มี JSON body server อาจ reject ด้วย 400 Bad Request หรือ parse body ผิด

**วางไว้ที่ไหน:** ถ้า headers เหมือนกันทุก request → วางใต้ Thread Group (scope ครอบทุก Sampler ใน group) ถ้า header เฉพาะ request นั้น → วางใต้ Sampler นั้นโดยตรง

---

### 4.3 HTTP Cookie Manager — ทำไมต้องใส่สำหรับ Session-based App

Cookie Manager จัดการ cookies ให้อัตโนมัติเหมือน browser จริง:

ถ้าไม่มี Cookie Manager เกิดสิ่งนี้:
1. Virtual user ส่ง login request → server ตอบกลับพร้อม Set-Cookie header
2. Virtual user ส่ง request ถัดไป → **ไม่มี cookie ติดไปด้วย**
3. Server เห็นว่าไม่มี session → ตอบ 401 Unauthorized หรือ redirect ไป login

ผลคือ test ของคุณไม่สะท้อน real user เพราะ real browser เก็บและส่ง cookie อัตโนมัติ

**การใส่ Cookie Manager:** Add ที่ระดับ Thread Group หรือ Test Plan — Cookie Manager จะทำงาน per-thread หมายความว่า virtual user แต่ละคนมี cookie jar ของตัวเอง แยกจากกัน สะท้อน independent users จริงๆ

---

### 4.4 HTTP Request Defaults — ลด Duplication

ถ้า Test Plan มี 10 Sampler ที่เรียก API เดียวกัน คุณจะต้องพิมพ์ `api.example.com` และ port 443 ซ้ำกัน 10 ครั้ง — ถ้าต้องเปลี่ยน domain ทีหลัง (เช่น deploy ไป staging) ต้องแก้ทีละ field

HTTP Request Defaults แก้ปัญหานี้:

> "A configuration element works closely with a Sampler. Although it does not send requests, it can add to or modify requests."
> *(source: https://jmeter.apache.org/usermanual/test_plan.html)*

ตั้งค่า Server, Port, Protocol ครั้งเดียวใน HTTP Request Defaults → Sampler ทุกตัวใน scope จะใช้ค่านั้นเป็น default โดยอัตโนมัติ

**วิธีใช้:** Add → Config Element → HTTP Request Defaults วางไว้ใต้ Thread Group หรือ Test Plan ก็ได้ แล้วใส่แค่ field ที่ต้องการให้เป็น default — ไม่ต้องกรอก Path เพราะแต่ละ Sampler มี path ต่างกัน

⚠️ **Best practice:** ใช้ HTTPS ไม่ใช่ HTTP สำหรับ test real endpoints — Protocol ใน HTTP Request Defaults ควรตั้งเป็น `https` เสมอถ้า endpoint ของคุณใช้ HTTPS

---

⏸ **Self-check (Backward Retrieval):** ก่อนอ่านต่อ — จากบทที่ 3 คุณเรียนเรื่อง Listener ไป เมื่อกี้เราเพิ่ง configure HTTP Request Sampler เสร็จ ถ้าคุณกด Run ทันทีโดยไม่มี Listener เลย จะเกิดอะไร? Test Plan ยังรันได้ไหม? เขียนคำตอบลงกระดาษก่อน — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น — แล้วค่อยอ่านต่อ

> เฉลย: Test Plan **รันได้** — Sampler ยังส่ง request ตามปกติ แต่คุณ **ไม่เห็นผลลัพธ์** เพราะไม่มี Listener รวบรวมผล JMeter จะ save ผลลงไฟล์ .jtl ถ้าคุณเพิ่ม `-l log.jtl` ใน CLI mode แต่ใน GUI mode ถ้าไม่มี Listener = ไม่เห็นข้อมูลอะไรเลย

---

### 4.5 อ่านผลจาก View Results Tree

View Results Tree แสดงผล request ทุกตัวเป็นรายการ:

**โครงสร้างที่เห็น:**
```
View Results Tree
├── ✅ HTTP Request: GET /posts (สีเขียว = success)
│   ├── Request Tab:   แสดง headers ที่ส่งออกไป + body
│   ├── Response Tab:  แสดง response body ที่ได้กลับมา
│   └── Sampler Result: status code, latency, bytes
└── ❌ HTTP Request: POST /login (สีแดง = failure)
    ├── Request Tab
    ├── Response Tab:  แสดง error message จาก server
    └── Sampler Result: Response code: 401
```

**สิ่งที่ต้องดูก่อนเสมอ:**
1. **Response Code** — 200 = OK, 201 = Created, 4xx = client error, 5xx = server error
2. **Response Body** — ดูว่า server ส่ง response ที่ถูกต้องหรือเปล่า
3. **Latency** — เวลาตั้งแต่ JMeter ส่ง request จนได้รับ byte แรกของ response (ms)

**การใช้ Response data tab:** เปลี่ยน dropdown จาก "Text" เป็น "JSON" เพื่อดู JSON response แบบ formatted หรือ "HTML" ถ้า response เป็น web page

---

⏸ **Self-check (Bloom's L4 — Analysis):** คุณรัน Test Plan แล้วเห็นใน View Results Tree ว่า POST /login ได้ response code 200 แต่ response body เป็น `{"error": "Invalid credentials"}` — JMeter จะ mark request นี้ว่า "สำเร็จ" หรือ "ล้มเหลว"? และนี่บอกอะไรเกี่ยวกับ Test Plan ของคุณ? เขียนคำตอบลงกระดาษก่อน — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น — แล้วค่อยอ่านต่อ

> เฉลย: JMeter จะ mark ว่า **"สำเร็จ"** (สีเขียว) เพราะมองแค่ HTTP status code 200 — แต่จริงๆ business logic ล้มเหลว เพราะ response บอก "Invalid credentials" นี่คือเหตุผลที่ต้องใช้ **Response Assertion** เพื่อ verify ว่า response body มีเนื้อหาที่ถูกต้อง ไม่ใช่แค่ status code ที่ถูก (เรื่องนี้จะเรียนในบทที่ 7)

---

## ส่วนที่ 5: ตัวอย่าง 3 ระดับ

### ตัวอย่าง Beginner: GET Request ไปที่ Public API

**เป้าหมาย:** สร้าง Test Plan ที่ส่ง GET request ไปที่ `https://jsonplaceholder.typicode.com/posts` และดูผลใน View Results Tree

**ทำไม API นี้:** JSONPlaceholder เป็น free fake REST API ที่ใช้ทดสอบได้โดยไม่ต้อง setup server เอง ไม่มี authentication ทำให้เหมาะสำหรับฝึก

---

**Step 1: สร้าง Test Plan ใหม่**

เปิด JMeter → ไม่ต้องทำอะไร เพราะมี Test Plan ว่างๆ ให้อยู่แล้ว
- เปลี่ยน Name จาก "Test Plan" เป็น "JSONPlaceholder GET Test"

**Step 2: เพิ่ม Thread Group**

Right-click ที่ Test Plan → Add → Threads (Users) → Thread Group

ตั้งค่า:
- Name: `Single User Test`
- Number of Threads: `1` ← ทดสอบก่อนด้วย 1 user
- Ramp-Up Period: `1`
- Loop Count: `1` ← ส่งครั้งเดียว

**Step 3: เพิ่ม HTTP Request Defaults (Config Element)**

Right-click ที่ Thread Group → Add → Config Element → HTTP Request Defaults

ตั้งค่า:
- Server Name or IP: `jsonplaceholder.typicode.com`
  ← ทำไม: base domain เหมือนกันทุก request ตั้งครั้งเดียว
- Port Number: `443`
  ← ทำไม: HTTPS ใช้ port 443
- Protocol: `https`
  ← ทำไม: endpoint นี้ใช้ HTTPS ไม่ใช่ HTTP

**Step 4: เพิ่ม HTTP Request Sampler**

Right-click ที่ Thread Group → Add → Sampler → HTTP Request

ตั้งค่า:
- Name: `GET All Posts`
- Server Name or IP: *(ว่างไว้ — ใช้ค่าจาก Defaults)*
  ← ทำไม: ถ้าว่างไว้ JMeter ดึงค่าจาก HTTP Request Defaults อัตโนมัติ
- Path: `/posts`
  ← ทำไม: endpoint ที่ต้องการ ใส่เฉพาะ path ไม่ใส่ domain
- Method: `GET`
  ← ทำไม: เราแค่ดึงข้อมูล ไม่ได้ส่งอะไรไป

**Step 5: เพิ่ม HTTP Header Manager**

Right-click ที่ HTTP Request (GET All Posts) → Add → Config Element → HTTP Header Manager

คลิก "Add" แล้วเพิ่ม:
- Name: `Accept`  Value: `application/json`
  ← ทำไม: บอก server ว่าต้องการรับ JSON response

**Step 6: เพิ่ม View Results Tree**

Right-click ที่ Thread Group → Add → Listener → View Results Tree

(ไม่ต้องตั้งค่าอะไรเพิ่ม — default พอ)

**Step 7: Save และ Run**

File → Save As → บันทึกเป็น `jsonplaceholder-get-test.jmx`

กด Run → Start (Ctrl+R)

---

**ผลลัพธ์ที่ควรเห็นใน View Results Tree:**

```
View Results Tree
└── ✅ GET All Posts (สีเขียว)
    Sampler Result tab:
      Thread Name: Thread Group 1-1
      Sample Start: [timestamp]
      Load time: [xxx]ms
      Connect Time: [xxx]ms
      Latency: [xxx]ms
      Size in bytes: [ประมาณ 26,xxx bytes]
      Sent bytes: [xxx]
      Response code: 200
      Response message: OK

    Response data tab (JSON):
      [
        {
          "userId": 1,
          "id": 1,
          "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
          "body": "quia et suscipit\n..."
        },
        ...
      ]
```

**JMX XML ของ Test Plan นี้:**

```xml
<!-- # label: JMX snippet — ยังไม่ได้ทดสอบแบบ standalone; ต้องการ JMeter 5.6.3 -->
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.6.3">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan"
              testname="JSONPlaceholder GET Test">
      <boolProp name="TestPlan.functional_mode">false</boolProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup"
                   testname="Single User Test">
        <intProp name="ThreadGroup.num_threads">1</intProp>
        <intProp name="ThreadGroup.ramp_time">1</intProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <intProp name="LoopController.loops">1</intProp>
        </elementProp>
      </ThreadGroup>
      <hashTree>
        <!-- HTTP Request Defaults: ตั้ง base URL ครั้งเดียว -->
        <ConfigTestElement guiclass="HttpDefaultsGui" testclass="ConfigTestElement"
                           testname="HTTP Request Defaults">
          <stringProp name="HTTPSampler.domain">jsonplaceholder.typicode.com</stringProp>
          <stringProp name="HTTPSampler.port">443</stringProp>
          <stringProp name="HTTPSampler.protocol">https</stringProp>
        </ConfigTestElement>
        <hashTree/>
        <!-- HTTP Header Manager: ส่ง Accept header -->
        <HeaderManager guiclass="HeaderPanel" testclass="HeaderManager"
                       testname="HTTP Header Manager">
          <collectionProp name="HeaderManager.headers">
            <elementProp name="" elementType="Header">
              <stringProp name="Header.name">Accept</stringProp>
              <stringProp name="Header.value">application/json</stringProp>
            </elementProp>
          </collectionProp>
        </HeaderManager>
        <hashTree/>
        <!-- HTTP Request Sampler: GET /posts -->
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy"
                          testname="GET All Posts">
          <stringProp name="HTTPSampler.path">/posts</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
        </HTTPSamplerProxy>
        <hashTree/>
        <!-- View Results Tree: ดูผลรายตัว (ใช้ตอน debug เท่านั้น) -->
        <ResultCollector guiclass="ViewResultsFullVisualizer" testclass="ResultCollector"
                         testname="View Results Tree">
          <boolProp name="ResultCollector.error_logging">false</boolProp>
        </ResultCollector>
        <hashTree/>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

---

### ตัวอย่าง Intermediate: POST Request สำหรับ E-Learning API

**Scenario:** ระบบ e-learning มี API endpoint สำหรับ instructor submit บทเรียนใหม่ ต้องการทดสอบว่า endpoint รับ POST request พร้อม JSON body และ Authorization header ถูกต้อง

**Test Plan structure:**

```
Test Plan: E-Learning Instructor API Test
└── Thread Group: Instructor Users (50 threads, Ramp-Up 30s)
    ├── HTTP Request Defaults
    │   └── Server: api.elearning-platform.example
    │       Port: 443, Protocol: https
    ├── HTTP Header Manager (สำหรับทุก request)
    │   ├── Content-Type: application/json
    │   ├── Authorization: Bearer ${__P(auth_token,test-token-placeholder)}
    │   └── Accept: application/json
    ├── HTTP Cookie Manager
    ├── HTTP Request: POST /api/v1/courses
    │   └── Body Data: JSON payload
    └── Aggregate Report
```

**HTTP Header Manager configuration:**

| Name | Value | ทำไม |
|------|-------|------|
| Content-Type | application/json | บอก server ว่า body เป็น JSON |
| Authorization | Bearer ${__P(auth_token,placeholder)} | ส่ง token — ใช้ JMeter property แทน hardcode |
| Accept | application/json | บอก server ว่าต้องการรับ JSON กลับมา |

**Body Data ของ POST request:**

```json
{
  "title": "Introduction to Python Programming",
  "description": "Beginner course covering Python fundamentals",
  "category": "programming",
  "duration_hours": 20,
  "price": 1500,
  "published": false
}
```

**ทำไม Cookie Manager จำเป็นที่นี่:** ถ้า API ใช้ session-based auth (ไม่ใช่ stateless JWT) Cookie Manager จะเก็บ session cookie หลัง authenticate และส่งในทุก request ถัดไป ทำให้ virtual user ทำงานเหมือน real instructor ที่ login แล้ว

**ผลที่ควรได้จาก Aggregate Report:**

| Label | # Samples | Average | 90th % | Error% |
|-------|-----------|---------|--------|--------|
| POST /api/v1/courses | 500 | <500ms | <1000ms | <1% |

ถ้า Error% > 1% → ดู View Results Tree (ใน debug run) เพื่อหาว่า server ตอบ error อะไร

---

### ตัวอย่าง Advanced: Test Plan ที่มี Setup, Main Test, Teardown

**Scenario:** Production-grade Test Plan สำหรับ e-commerce checkout API ที่ต้องมี:
1. **Setup phase:** ลงทะเบียน test data (สร้าง test users, สร้าง test products)
2. **Main test:** Load test checkout flow จริง
3. **Teardown phase:** ลบ test data ออกจาก system

**Test Plan structure:**

```
Test Plan: E-Commerce Checkout Load Test
├── HTTP Request Defaults (global)
├── setUp Thread Group: Test Data Setup      ← รันก่อน
│   ├── HTTP Request: POST /api/users (สร้าง test users)
│   └── HTTP Request: POST /api/products (สร้าง test products)
│
├── Thread Group: Main Load Test             ← รันหลัก
│   ├── HTTP Cookie Manager
│   ├── HTTP Header Manager
│   ├── HTTP Request: POST /api/auth/login
│   ├── HTTP Request: GET /api/products
│   ├── HTTP Request: POST /api/cart/add
│   ├── HTTP Request: POST /api/orders/checkout
│   ├── Constant Timer: 2000ms (think time)
│   └── Aggregate Report
│
└── tearDown Thread Group: Cleanup           ← รันหลังสุด
    ├── HTTP Request: DELETE /api/test/users
    └── HTTP Request: DELETE /api/test/products
```

**ทำไมต้องมี setUp และ tearDown Thread Group:**

JMeter มี Thread Group ชนิดพิเศษ 2 ชนิด:
- `setUp Thread Group` — รันก่อน Thread Group อื่นทั้งหมด เหมาะสำหรับ prepare test data
- `tearDown Thread Group` — รันหลัง Thread Group อื่นทั้งหมดเสร็จ เหมาะสำหรับ cleanup

**Trade-off ที่ต้องพิจารณา:**

| ด้าน | Test Plan ง่าย (ไม่มี setup/teardown) | Test Plan ครบ (มี setup/teardown) |
|------|--------------------------------------|----------------------------------|
| ความซับซ้อน | ต่ำ — เริ่มได้เร็ว | สูง — ใช้เวลา design มากกว่า |
| ความน่าเชื่อถือ | ต่ำ — ต้องเตรียม data มือทุกครั้ง | สูง — data สดใหม่ทุก run |
| ผลกระทบต่อระบบ | อาจทิ้ง dirty data ไว้ | Clean หลัง test เสร็จ |
| เหมาะกับ | Development + quick smoke test | Staging + CI/CD pipeline |

**คำแนะนำ:** เริ่มด้วย Test Plan ง่ายก่อน (เหมือน Beginner example) แล้วค่อยเพิ่ม setUp/tearDown เมื่อ test stable แล้วและต้องการ automate ใน pipeline

---

## ส่วนที่ 6: Common Mistakes

### ❌ Mistake 1: ลืม HTTP Cookie Manager ทำให้ test ไม่สะท้อน real user

**แบบผิด:** Test Plan ที่ test login flow แต่ไม่มี Cookie Manager

```
Thread Group
├── HTTP Request: POST /login        ← ได้ Set-Cookie header กลับมา
├── HTTP Request: GET /dashboard     ← ❌ ไม่มี cookie → server เห็นว่าไม่ได้ login
└── HTTP Request: GET /profile       ← ❌ 401 Unauthorized ทุกครั้ง
```

**แบบถูก:** เพิ่ม HTTP Cookie Manager ก่อน Sampler แรก

```
Thread Group
├── HTTP Cookie Manager              ← ✅ เก็บ cookie จาก login response อัตโนมัติ
├── HTTP Request: POST /login
├── HTTP Request: GET /dashboard     ← ✅ cookie ติดไปอัตโนมัติ
└── HTTP Request: GET /profile       ← ✅ server รู้ว่า logged in
```

🔍 **เหตุผล:** ถ้าไม่มี Cookie Manager ผล test จะบอกว่า dashboard และ profile endpoint ช้าและ error rate สูง — แต่จริงๆ ปัญหาคือ test ไม่ได้ส่ง session cookie ไปด้วย ไม่ใช่ปัญหาของ endpoint เหล่านั้น ผลการทดสอบ misleading มาก

🤔 **วิธีตรวจสอบ:** ดูใน View Results Tree → tab Response Headers ของ POST /login ว่ามี `Set-Cookie` header ไหม ถ้ามีแต่ request ถัดไป error 401 → Cookie Manager หายไปหรือวางผิดที่

*(source: https://jmeter.apache.org/usermanual/component_reference.html — HTTP Cookie Manager)*

---

### ❌ Mistake 2: Hardcode ค่า Sensitive ใน Body Data

**แบบผิด:** ใส่ password และ API key โดยตรงใน Body Data

```json
{
  "username": "admin",
  "password": "MySecretPassword123",
  "api_key": "sk-live-abc123xyz"
}
```

**แบบถูก:** ใช้ JMeter Properties หรือ User Defined Variables

```json
{
  "username": "${test_username}",
  "password": "${test_password}",
  "api_key": "${api_key}"
}
```

แล้วกำหนดค่าผ่าน:
- JMeter Properties file (`user.properties`)
- CLI argument: `jmeter -n -t test.jmx -Jtest_username=testuser -Jtest_password=testpass`
- User Defined Variables ใน Test Plan (สำหรับ non-sensitive values)

🔍 **เหตุผล:** ถ้า hardcode ค่า sensitive ใน .jmx file และ commit เข้า version control → credentials จะรั่วไปในประวัติ git ที่ใครก็ clone ได้ นอกจากนี้ถ้าต้องเปลี่ยน environment (dev → staging → prod) ต้องแก้ file ทุกครั้ง

🤔 **Best practice เพิ่มเติม:** ตั้งค่า `.gitignore` เพื่อกันไม่ให้ properties file ที่มี sensitive data ถูก commit ด้วย

*(source: OWASP CI/CD Security Risks CICD-SEC-06 — "Code containing credentials being pushed to one of the branches of an SCM repository... the credentials are exposed to anyone with read access to the repository" — https://owasp.org/www-project-top-10-ci-cd-security-risks/CICD-SEC-06-Insufficient-Credential-Hygiene | JMeter official mailing list — JMeter committer sebb confirmed: "Which stores its passwords in plain text in the JMX file" — https://jmeter.apache.org/usermanual/best-practices.html — properties management)*

---

### ❌ Mistake 3: ไม่ใช้ HTTP Request Defaults ทำให้ Duplicate Config ทุก Sampler

**แบบผิด:** กรอก Server, Port, Protocol ซ้ำในทุก Sampler

```
Thread Group
├── HTTP Request: GET /users       (Server: api.example.com, Port: 443, Protocol: https)
├── HTTP Request: GET /products    (Server: api.example.com, Port: 443, Protocol: https)
├── HTTP Request: POST /orders     (Server: api.example.com, Port: 443, Protocol: https)
└── HTTP Request: GET /payments    (Server: api.example.com, Port: 443, Protocol: https)
```

**แบบถูก:** ตั้งค่า base config ครั้งเดียวใน HTTP Request Defaults

```
Thread Group
├── HTTP Request Defaults          (Server: api.example.com, Port: 443, Protocol: https)
├── HTTP Request: GET /users       (ว่างไว้ — ดึงจาก Defaults)
├── HTTP Request: GET /products    (ว่างไว้ — ดึงจาก Defaults)
├── HTTP Request: POST /orders     (ว่างไว้ — ดึงจาก Defaults)
└── HTTP Request: GET /payments    (ว่างไว้ — ดึงจาก Defaults)
```

🔍 **เหตุผล:** ถ้าต้อง deploy test ไป staging environment ที่ใช้ domain ต่างกัน — แบบผิดต้องแก้ทุก Sampler (อาจหลายสิบตัว) แบบถูกแก้ที่เดียวใน HTTP Request Defaults ทุก Sampler update อัตโนมัติ

🤔 **กฎ:** ถ้าค่า field เดียวกันซ้ำกันมากกว่า 2 ตัว → ย้ายไปใส่ใน HTTP Request Defaults

*(source: https://jmeter.apache.org/usermanual/test_plan.html — Configuration Elements)*

---

## ส่วนที่ 7: สรุปบท

### Code-Based Task (ทำก่อนดูเฉลย)

**Task:** ดู JMX XML snippet ด้านล่าง แล้วตอบ 3 ข้อ

```xml
<!-- # label: JMX snippet — ยังไม่ได้ทดสอบแบบ standalone; ต้องการ JMeter 5.6.3 -->
<ThreadGroup testname="API Test">
  <intProp name="ThreadGroup.num_threads">200</intProp>
  <intProp name="ThreadGroup.ramp_time">0</intProp>
  <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
    <boolProp name="LoopController.continue_forever">false</boolProp>
    <intProp name="LoopController.loops">5</intProp>
  </elementProp>
</ThreadGroup>
<hashTree>
  <HTTPSamplerProxy testname="Create Order">
    <stringProp name="HTTPSampler.domain">api.shop.example.com</stringProp>
    <stringProp name="HTTPSampler.port">80</stringProp>
    <stringProp name="HTTPSampler.protocol">http</stringProp>
    <stringProp name="HTTPSampler.path">/api/orders</stringProp>
    <stringProp name="HTTPSampler.method">POST</stringProp>
    <stringProp name="HTTPSampler.postBodyRaw">
      {"user_id": "123", "product_id": "456", "payment_key": "sk-live-secret999"}
    </stringProp>
  </HTTPSamplerProxy>
  <hashTree/>
  <ResultCollector guiclass="ViewResultsFullVisualizer" testname="View Results Tree"/>
  <hashTree/>
</hashTree>
```

**คำถาม:**
1. มีปัญหาอะไรใน configuration นี้บ้าง? (ระบุอย่างน้อย 3 จุด)
2. ถ้า Thread Group นี้รันไป — request ทั้งหมดจะส่งไปกี่ครั้ง?
3. ถ้าต้องการ run test นี้ใน production environment จริง ต้องแก้อะไรบ้างก่อน?

---

**เฉลย**

> **ข้อ 1 — ปัญหาที่พบ:**
> - `ramp_time = 0` → thread ทั้ง 200 start พร้อมกัน เกิด spike ทันที ไม่สะท้อน real traffic
> - `protocol: http, port: 80` → ถ้า endpoint นี้เป็น production API ควรใช้ HTTPS (port 443) ไม่ใช่ HTTP ที่ไม่ encrypted
> - `payment_key: "sk-live-secret999"` hardcode อยู่ใน Body Data → ถ้า save เป็น .jmx และ commit ไป git credentials รั่ว
> - ไม่มี HTTP Cookie Manager → ถ้า order API ต้องการ login session จะ error ทันที
> - ไม่มี HTTP Header Manager → ไม่มี Content-Type: application/json ทั้งๆ ที่ส่ง JSON body server อาจ parse ผิด
> - View Results Tree ถูก add ไว้ → ถ้ารัน load test จริงกับ 200 threads × 5 loops = 1,000 requests จะใช้ memory สูงมาก

> **ข้อ 2:** 200 threads × 5 loops = **1,000 requests** ทั้งหมด

> **ข้อ 3:** ก่อน run production:
> - เปลี่ยน `ramp_time` เป็นค่าที่สมเหตุสมผล เช่น 60-120 วินาที
> - เปลี่ยน protocol เป็น `https` และ port เป็น `443`
> - ย้าย `payment_key` ออกไปใส่ใน properties file แล้วใช้ `${__P(payment_key)}` แทน
> - เพิ่ม HTTP Cookie Manager และ HTTP Header Manager
> - แทน View Results Tree ด้วย Aggregate Report สำหรับ load test

---

### Elaborative Interrogation

**ทำไม HTTP Request Defaults ถึงสำคัญกว่าที่คิด?**

ลองนึกว่าคุณมี Test Plan ที่มี 30 Sampler และต้องย้ายไปทดสอบ staging environment ซึ่งใช้ domain ต่างกัน — ถ้าไม่ใช้ HTTP Request Defaults คุณต้องแก้ 30 ตัว ถ้าใช้แก้ตัวเดียว

แต่มีเหตุผลที่ลึกกว่านั้น: เมื่อ Test Plan ซับซ้อนขึ้น (หลาย Thread Group, หลาย Sampler) การที่แต่ละ Sampler มี domain hardcode ของตัวเองทำให้ยากมากที่จะ maintain และเพิ่ม risk ของ human error (พิมพ์ผิด 1 Sampler แต่อีก 29 ตัวถูก → ผลการทดสอบ inconsistent)

HTTP Request Defaults บังคับ single source of truth ซึ่งเป็น engineering best practice ที่ใช้ได้กับโค้ดและ config ทุกชนิด

---

### Generation Effect — Retrieval Questions สุดท้าย

หยุดอ่าน เขียนคำตอบลงกระดาษก่อน ห้ามดูเฉลยจนกว่าจะเขียนเสร็จ

**ข้อ 1:** อธิบายด้วยคำพูดตัวเองว่า HTTP Cookie Manager ทำงานอย่างไรและทำไมถึงจำเป็นสำหรับ session-based application (ห้าม copy จากบทความ)

**ข้อ 2:** ถ้า Test Plan มี HTTP Request Defaults ตั้ง Protocol = `https` และ Sampler ตัวหนึ่งตั้ง Protocol = `http` ด้วย — อะไรจะชนะ? และนี่อาจเป็น bug ได้อย่างไร?

---

> **เฉลยข้อ 1:** Cookie Manager ทำงานเหมือน browser — เมื่อ server ส่ง Set-Cookie header กลับมา Cookie Manager เก็บ cookie ไว้ใน "jar" ของ thread นั้น และส่ง cookie กลับไปในทุก request ถัดไปอัตโนมัติ ถ้าไม่มี Cookie Manager ทุก request จะเหมือน "ผู้ใช้ใหม่" ที่ไม่เคย login ซึ่งไม่สะท้อนความเป็นจริง

> **เฉลยข้อ 2:** ค่าใน **Sampler ชนะ** เพราะ Sampler-level config override Defaults — ถ้า developer ตั้ง Protocol = `http` ผิดพลาดใน Sampler หนึ่งตัว JMeter จะส่ง request นั้นผ่าน HTTP แทน HTTPS โดยที่ไม่มี warning ตัวอย่างที่เป็น bug จริง: Sampler ที่ส่ง Authorization token ผ่าน HTTP แทน HTTPS ทำให้ token ไม่ encrypted ระหว่างทาง

**ถ้าตอบไม่ออก:** กลับไปอ่าน section 4.4 (HTTP Request Defaults) แล้วลองอธิบายด้วยการวาด diagram ว่า Defaults กับ Sampler interact กันอย่างไร — visualization ช่วยให้เข้าใจ scope rules ได้ดีกว่าการอ่านซ้ำ
