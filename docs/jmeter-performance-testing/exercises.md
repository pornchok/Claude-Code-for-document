# แบบฝึกหัด: Performance Testing ด้วย Apache JMeter

> **วิธีใช้ไฟล์นี้**
> ทำแต่ละ exercise ด้วยตัวเองก่อนดูเฉลย เขียนคำตอบออกมาจริงๆ (ไม่ใช่แค่คิดในใจ) — การเขียนบังคับให้สมองประมวลผลลึกขึ้น
>
> **3 ระดับต่อ concept:**
> - **Beginner** (Recall/Recognition): อธิบายหรือระบุจากความจำ
> - **Intermediate** (Application): ใช้ความรู้ในสถานการณ์ใหม่ที่ไม่เคยเห็นในตัวอย่าง
> - **Advanced** (Synthesis/Diagnosis): วิเคราะห์ระบบซับซ้อน หา bug ออกแบบ strategy

---

## กลุ่มที่ 1: Performance Testing Concepts
> ทำหลังอ่านบทที่ 1

### Exercise 1.1 — Beginner (Recall)

อธิบายความแตกต่างระหว่าง **Load Test** กับ **Stress Test** ด้วยคำตัวเอง (ห้าม copy จากเอกสาร)

พร้อม: ยกตัวอย่าง use case จริงที่แต่ละประเภทเหมาะสม — ใช้บริบทของระบบ ticketing สำหรับ concert ที่กำลังจะเปิดขาย

<details>
<summary>ดูเฉลย</summary>

**Load Test:** ทดสอบระบบที่ **load ปกติหรือ load สูงสุดที่คาดไว้** เพื่อยืนยันว่าระบบทำงานได้ตาม SLA — ไม่ได้พยายาม "ทำให้ระบบพัง" แต่ validate ว่า "ระบบรับได้ไหม"

ตัวอย่าง: ระบบ ticketing ที่คาดว่าจะมี 5,000 คน login พร้อมกัน → รัน load test ที่ 5,000 concurrent users 30 นาที เพื่อยืนยันว่า p95 response time < 2 วินาที และ error < 1%

**Stress Test:** ผลักดัน load เกินกว่า capacity ที่คาดไว้ เพื่อ**หาจุดแตก** (breakpoint) และดูว่าระบบ fail อย่างไร (graceful degradation หรือ crash)

ตัวอย่าง: เพิ่ม load จาก 5,000 → 8,000 → 12,000 → 20,000 users เรื่อยๆ จนระบบ error — บันทึก breakpoint ว่าระบบพังที่ load เท่าไหร่ และ fail message อะไร (500 error, OOM, connection refused)

</details>

---

### Exercise 1.2 — Intermediate (Application in novel context)

ทีม DevOps ของบริษัท logistics ต้องการรู้ว่า API `/shipment/tracking` จะ degrade อย่างไรระหว่างพายุ (ช่วงที่ shipments เพิ่มกะทันหัน 400% ใน 10 นาที แล้วลดลงกลับปกติใน 5 นาที)

ออกแบบ **test strategy** ว่าต้องรัน test ประเภทไหน, กำหนด thread count และ duration อย่างไร, และวัด metric อะไรเพื่อตอบคำถามของทีม

<details>
<summary>ดูเฉลย</summary>

สถานการณ์นี้ตรงกับ **Spike Test** — load พุ่งสูงกะทันหันแล้วลดลง ไม่ใช่ load ปกติที่ค่อยๆ เพิ่ม

**Test Strategy:**

1. **Baseline (Smoke Test):** รัน 5-10 users 5 นาที — ยืนยันว่า API ทำงานปกติก่อน
2. **Load Test:** รัน 100% normal load (สมมติ 500 concurrent users) 20 นาที — สร้าง baseline metrics
3. **Spike Test:** ออกแบบ Thread Group ดังนี้
   - Phase 1 (Normal): 500 users, 5 นาที
   - Phase 2 (Spike): เพิ่มเป็น 2,000 users (400%) ใน 2 นาที, hold 10 นาที
   - Phase 3 (Recovery): ลดกลับ 500 users ใน 5 นาที
   - Phase 4 (Post-spike): 500 users อีก 10 นาที เพื่อดูว่า recover ได้หรือไม่

**Metrics ที่ต้องวัด:**
- Error rate ระหว่าง spike (ยอมรับได้ไหม?)
- p99 response time ระหว่าง spike vs baseline
- เวลาที่ใช้ recovery หลัง spike (กลับ error < 1% เมื่อไหร่)
- Throughput drop ระหว่าง spike (server throttle ยังไง)

</details>

---

### Exercise 1.3 — Advanced (Synthesis)

ทีม engineering ถกเถียงกันว่า "เราควรรัน Endurance Test (Soak Test) หรือเปล่า ก่อน production launch?" engineer คนหนึ่งบอกว่า "ไม่จำเป็น Load Test ผ่านแล้ว" อีกคนบอก "จำเป็นมาก"

ใครถูก? เขียน argument ที่สมบูรณ์สำหรับทั้งสองฝ่าย แล้ว synthesize ว่าเมื่อไหร่ที่ Endurance Test จำเป็นจริงๆ และเมื่อไหร่ที่ skip ได้อย่างมีเหตุผล

<details>
<summary>เฉลย</summary>

**ฝ่ายที่บอกว่าไม่จำเป็น:**
- Load Test ยืนยันว่า peak capacity ผ่านแล้ว
- ใช้ cost/benefit: Endurance Test ต้องใช้เวลาหลายชั่วโมง ค่า infrastructure สูง
- ถ้าระบบ stateless และ auto-scaling คือ instance ใหม่จะ replace instance เก่าอยู่แล้ว

**ฝ่ายที่บอกว่าจำเป็น:**
- Load Test รันแค่ 30-60 นาที ไม่พบ memory leak ที่ใช้เวลาหลายชั่วโมงจึงเห็น
- ไม่พบ connection pool exhaustion, database cursor leak, file descriptor leak
- ไม่พบ log rotation ปัญหา, disk full หลังรันนาน
- ไม่พบ thread starvation ที่สะสมช้าๆ

**Synthesis — เมื่อไหร่ที่ Endurance Test จำเป็น:**
- ระบบมี in-memory state (session, cache ที่ grow ไม่หยุด)
- ภาษา/framework ที่ทราบว่ามี GC pressure (JVM apps)
- Database connection pool หรือ external service connection
- ระบบต้อง run 24/7 โดยไม่ restart
- เคยมี memory leak report จาก production ก่อนหน้า

**เมื่อไหร่ที่ skip ได้อย่างมีเหตุผล:**
- Stateless microservices ที่ auto-scale และ restart สม่ำเสมอ (เช่น Lambda, container ที่ recycle ทุก N hours)
- ระบบ read-only ที่ไม่มี stateful component
- ระบบที่มี production monitoring ครบถ้วน (สามารถ detect และ remediate production issue ได้เร็ว)

</details>

---

## กลุ่มที่ 2: JMeter Installation & Setup
> ทำหลังอ่านบทที่ 2

### Exercise 2.1 — Beginner (Recall)

JMeter ต้องการ Java ในการรัน — อธิบายว่าทำไม และ Java version ที่ support กับ JMeter 5.x คือเท่าไหร่? บอกด้วยว่าจะ verify ว่า Java ติดตั้งถูกต้องได้อย่างไร

<details>
<summary>ดูเฉลย</summary>

JMeter เป็น Java application (เขียนด้วย Java) — รันบน JVM (Java Virtual Machine) ดังนั้นต้องมี JRE หรือ JDK ติดตั้งก่อน

JMeter 5.x รองรับ **Java 8 ขึ้นไป** — official docs แนะนำให้ติดตั้ง "latest minor version of your major version for security and performance reasons" (เช่น Java 17 LTS หรือ Java 21 LTS)

verify ด้วย:
```bash
java -version
# ควรได้: java version "17.x.x" / "21.x.x" หรือสูงกว่า (LTS)
```

ถ้า command ไม่พบ หรือ version ต่ำกว่า 8 → ต้องติดตั้งหรืออัปเกรด Java ก่อน

</details>

---

### Exercise 2.2 — Intermediate (Application)

สถานการณ์: ทีม QA ของ startup fintech กำลัง setup JMeter บน CI/CD server (Ubuntu 22.04, ไม่มี GUI) เพื่อรัน automated performance test ทุกคืน server นี้ไม่ได้ install Java ไว้ก่อน

เขียน bash script ที่ติดตั้ง Java, download JMeter, และ verify การติดตั้งครบถ้วน (พร้อม error handling พื้นฐาน)

<details>
<summary>ดูเฉลย</summary>

```bash
# ยังไม่ได้ทดสอบแบบ end-to-end — ต้องการ Ubuntu 22.04 + internet access + sudo/root privileges
#!/bin/bash
set -e

JMETER_VERSION="5.6.3"
INSTALL_DIR="/opt"

echo "=== Installing Java 11 ==="
apt-get update -q
apt-get install -y openjdk-11-jre-headless

echo "=== Verifying Java ==="
java -version 2>&1 | grep -E "11\." || { echo "ERROR: Java 11 not found"; exit 1; }

echo "=== Downloading JMeter ${JMETER_VERSION} ==="
wget -q "https://downloads.apache.org/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz" \
  -O "/tmp/jmeter.tgz"

echo "=== Extracting JMeter ==="
tar -xzf /tmp/jmeter.tgz -C "$INSTALL_DIR"
ln -sf "${INSTALL_DIR}/apache-jmeter-${JMETER_VERSION}/bin/jmeter" /usr/local/bin/jmeter

echo "=== Verifying JMeter ==="
jmeter --version 2>&1 | grep -E "${JMETER_VERSION}" || { echo "ERROR: JMeter not installed correctly"; exit 1; }

echo "=== Setup complete ==="
echo "JMeter available at: /usr/local/bin/jmeter"
```

</details>

---

### Exercise 2.3 — Advanced (Diagnosis)

QA engineer install JMeter บน Windows และพบว่า:
- `jmeter.bat` เปิดได้และเห็น GUI
- แต่พอรัน test ได้แค่ 200 requests แล้ว JMeter crash ด้วย error: `java.lang.OutOfMemoryError: Java heap space`
- Machine มี RAM 16GB แต่ Task Manager แสดงว่า JMeter ใช้ RAM แค่ 256MB

วิเคราะห์ root cause และเสนอ solution ที่ครอบคลุม (ไม่ใช่แค่ "เพิ่ม RAM")

<details>
<summary>ดูเฉลย</summary>

**Root Cause:** JVM heap size default ต่ำเกินไปสำหรับ load ที่ต้องการ JMeter ถูก start โดยไม่ได้กำหนด heap size → JVM ใช้ default ซึ่งมักเป็น 256MB หรือ fraction ของ RAM (ไม่ใช่ total RAM)

**ทำไม 16GB RAM ไม่ช่วย:** RAM ที่มีอยู่ไม่ได้ถูก allocate ให้ JVM โดยอัตโนมัติ — ต้องระบุผ่าน JVM flags

**Solution ที่ครอบคลุม:**

1. แก้ `HEAP` ใน `jmeter.bat` (สำหรับ Windows):
```batch
set HEAP=-Xms1g -Xmx4g
```

2. หรือสร้าง `setenv.bat` ใน `bin/` folder:
```batch
set HEAP=-Xms1g -Xmx4g
set JVM_ARGS=-XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

3. ปิด listener ที่กิน memory (`View Results Tree`, `View Results in Table`) ระหว่าง test

4. ถ้า load สูงมาก — switch ไปใช้ CLI mode แทน GUI (CLI ประหยัด memory มากกว่า)

5. ตรวจว่า `jmeter.save.saveservice.*` บันทึกแค่ข้อมูลที่จำเป็น (ไม่บันทึก response body ถ้าไม่ต้องการ)

**Lesson:** OutOfMemoryError ใน JMeter ส่วนใหญ่ไม่ใช่ RAM ไม่พอ แต่เป็น JVM heap ไม่ได้รับ allocation ที่เพียงพอ

</details>

---

## กลุ่มที่ 3: Test Plan Structure
> ทำหลังอ่านบทที่ 3

### Exercise 3.1 — Beginner (Recall)

ระบุว่า Thread Group field ใดที่ต้องตั้งค่าเพื่อ simulate สถานการณ์ต่อไปนี้: **50 users เข้าระบบในเวลา 2 นาที แต่ละคนทำ 3 requests แล้วออก (ไม่วน loop)** — ตั้งค่าแต่ละ field เป็นเท่าไหร่?

<details>
<summary>ดูเฉลย</summary>

| Field | ค่าที่ตั้ง | เหตุผล |
|-------|---------|-------|
| Number of Threads (users) | 50 | = จำนวน virtual users |
| Ramp-Up Period (seconds) | 120 | = 2 นาที = 120 วินาที (JMeter จะ add user ทีละ 50/120 ≈ 1 user ทุก 2.4 วินาที) |
| Loop Count | 3 | = แต่ละ user ทำ 3 requests แล้วหยุด |

หมายเหตุ: "ไม่วน loop" ≠ Loop Count = 1 — "ทำ 3 requests" คือ Loop Count = 3 (assuming มี 1 HTTP Request Sampler ใน Thread Group)

</details>

---

### Exercise 3.2 — Intermediate (Application)

สถานการณ์: แพลตฟอร์ม food delivery มี 3 workflows หลักที่เกิดขึ้นพร้อมกันในช่วง peak hour:
- เรียกดูร้านอาหารและ menu (`GET /restaurants` + `GET /restaurants/{id}/menu`) — 65% ของ traffic
- สั่งอาหาร (`POST /orders`) — 25% ของ traffic
- ติดตามสถานะ delivery (`GET /orders/{id}/status`) — 10% ของ traffic

ออกแบบ JMeter Test Plan structure (ระบุ element แต่ละตัวและ configuration หลัก) ที่สะท้อน traffic pattern นี้ได้ถูกต้อง และอธิบายว่าทำไมต้องใช้ element แต่ละชนิดนั้น

<details>
<summary>ดูเฉลย</summary>

**Test Plan structure:**

```
Test Plan
├── HTTP Request Defaults (base URL: api.fooddelivery.com, port 443, HTTPS)
└── Thread Group (100 threads, ramp-up 60s, loop Forever, Duration 300s)
    ├── Throughput Controller — 65%
    │   ├── HTTP Request: GET /restaurants
    │   └── HTTP Request: GET /restaurants/${restaurant_id}/menu
    ├── Throughput Controller — 25%
    │   └── HTTP Request: POST /orders
    └── Throughput Controller — 10%
        └── HTTP Request: GET /orders/${order_id}/status
```

**Throughput Controller configuration:**
- เลือก "Percent Executions" mode (ไม่ใช่ "Total Executions")
- ตั้งค่า: 65, 25, 10 ตามลำดับ (รวม = 100%)

**ทำไมต้องใช้ element เหล่านี้:**
- **HTTP Request Defaults:** ป้องกันการ repeat base URL ใน sampler แต่ละตัว — ถ้าเปลี่ยน environment ต้องแก้แค่จุดเดียว
- **Throughput Controller (Percent Executions):** weight traffic ตาม real proportion ที่วัดได้จาก production — ต่างจาก Random Controller ที่ weight เท่ากันทุก sampler
- **Duration-based loop:** เหมาะสำหรับ peak hour simulation ที่รู้ duration (5 นาที) แต่ไม่รู้ว่าแต่ละ user ทำกี่ loop

</details>

---

### Exercise 3.3 — Advanced (Synthesis)

ทีม QA ออกแบบ Test Plan สำหรับ e-learning platform ที่มี feature ใหม่: "live quiz" ที่ทำงานผ่าน WebSocket และ REST API ผสมกัน

Test Plan ต้องครอบคลุม:
- Authentication flow (REST: POST /auth/login)
- Join quiz session (REST: POST /quiz/join)
- Receive quiz questions (WebSocket: stream)
- Submit answer (REST: POST /quiz/answer)

ออกแบบ JMeter Test Plan ที่ครอบคลุม flow นี้ พร้อมระบุข้อจำกัดของ JMeter สำหรับ WebSocket testing และวิธีจัดการ

<details>
<summary>ดูเฉลย</summary>

**ข้อจำกัดสำคัญ:** JMeter ไม่รองรับ WebSocket natively — ต้องใช้ **WebSocket Sampler plugin** (จาก JMeter Plugins Manager: `WebSocket Samplers by Peter Doornbosch`)

**Test Plan structure:**

```
Test Plan
├── HTTP Cookie Manager (scope: Test Plan)
├── HTTP Header Manager (Content-Type, Accept)
└── Thread Group (N users)
    ├── HTTP Sampler: POST /auth/login
    │   └── JSON Extractor: extract token → variable ${auth_token}
    ├── HTTP Header Manager: Authorization: Bearer ${auth_token}
    ├── HTTP Sampler: POST /quiz/join
    │   └── JSON Extractor: extract session_id → ${session_id}
    ├── WebSocket Open Connection: wss://api.example.com/quiz/stream
    │   └── Header Manager: Sec-WebSocket-Protocol: quiz-v1
    ├── WebSocket Read Sampler (อ่าน questions จาก stream)
    │   └── Duration: 30s (รอรับ questions)
    ├── HTTP Sampler: POST /quiz/answer (รัน N ครั้งตาม number of questions)
    └── WebSocket Close Connection
```

**ข้อจำกัดที่ต้อง acknowledge:**
1. WebSocket timing ไม่ controllable เหมือน HTTP — ต้องใช้ timeout แทน fixed loop
2. ถ้า quiz question interval ไม่สม่ำเสมอ Read Sampler อาจ timeout ก่อนได้รับ question
3. JMeter วัด WebSocket latency แตกต่างจาก HTTP — ต้อง clarify metric กับ stakeholder

**Alternative:** ถ้า WebSocket testing ซับซ้อนมาก ให้แยก test: ใช้ JMeter test REST API และใช้ tool เฉพาะ WebSocket (เช่น Artillery, Gatling) test WebSocket แยก

</details>

---

## กลุ่มที่ 4: HTTP Request Sampler และ Parameterization
> ทำหลังอ่านบทที่ 4

### Exercise 4.1 — Beginner (Recall)

CSV Data Set Config ตั้งค่าดังนี้:
- Recycle on EOF = **false**
- Stop thread on EOF = **false**

มี CSV file 10 rows และ Thread Group มี 30 users แต่ละ user loop 5 ครั้ง — อธิบายว่าจะเกิดอะไรขึ้นกับแต่ละ user เมื่ออ่าน CSV data หมดแล้ว?

<details>
<summary>ดูเฉลย</summary>

**ที่เกิดขึ้น:**
- Users 30 ตัว อ่าน CSV แบบ sequential (ใคร request ก่อนได้ row แรก ฯลฯ)
- เมื่อ row ที่ 10 ถูกอ่านแล้ว rows ต่อไปจะได้ค่า **เดิมซ้ำๆ** (row สุดท้าย = row 10) เพราะ:
  - Recycle on EOF = false → ไม่วนกลับต้น
  - Stop thread on EOF = false → ไม่หยุด thread

**ผลลัพธ์ที่แท้จริง:** requests ส่วนใหญ่จะใช้ข้อมูล row ที่ 10 ซ้ำกัน ซึ่งอาจทำให้ test ไม่สะท้อน real-world ถ้าต้องการ test ด้วยข้อมูลหลากหลาย

**Configuration ที่ถูกต้องตามวัตถุประสงค์:**
- ต้องการแต่ละ user ใช้ข้อมูลเฉพาะของตัวเอง → Stop thread on EOF = true + CSV มี rows ≥ (users × loops)
- ต้องการวน data ซ้ำได้ → Recycle on EOF = true

</details>

---

### Exercise 4.2 — Intermediate (Application)

ทีม QA ของ food delivery platform กำลัง test API `/order/place` ที่ต้องรับ:
- `restaurant_id` — มี 50 restaurants ในระบบ
- `menu_item_ids` — array ของ item IDs (แต่ละ restaurant มี menu item 10-20 รายการ)
- `delivery_address` — coordinates (lat, lng)
- `payment_method` — เลือกจาก: "card", "wallet", "cod"

ออกแบบ CSV structure และ JMeter configuration สำหรับ parameterize request นี้ให้ครอบคลุม realistic combinations

<details>
<summary>ดูเฉลย</summary>

**CSV structure (`order_test_data.csv`):**
```csv
restaurant_id,menu_items,lat,lng,payment_method
101,"[{""item_id"":1001,""qty"":2}]",13.7563,100.5018,card
102,"[{""item_id"":2005,""qty"":1},{""item_id"":2010,""qty"":3}]",13.7454,100.5340,wallet
103,"[{""item_id"":3002,""qty"":1}]",13.7308,100.5694,cod
...
```

หมายเหตุ: JSON ใน CSV ต้องใส่ใน quotes และ escape double quotes เป็น `""`

**CSV Data Set Config:**
- Filename: `order_test_data.csv`
- Variable Names: `restaurant_id,menu_items,lat,lng,payment_method`
- Delimiter: `,`
- Recycle on EOF: true (ให้วน data ซ้ำถ้า users มากกว่า rows)
- Sharing mode: All threads (ไม่ให้ users ซ้ำ data กัน)

**HTTP Request Body:**
```json
{
  "restaurant_id": ${restaurant_id},
  "items": ${menu_items},
  "delivery_location": {
    "lat": ${lat},
    "lng": ${lng}
  },
  "payment_method": "${payment_method}"
}
```

**ข้อควรระวัง:** ต้องสร้าง CSV data ให้ครอบคลุม edge cases: restaurant ที่ไม่มี stock, delivery zone ที่อยู่นอก range, combination ของ payment method ที่อาจไม่รองรับทุก restaurant

</details>

---

### Exercise 4.3 — Advanced (Diagnosis)

ดู CSV snippet และ JMeter configuration ด้านล่าง แล้วระบุว่า parameterization จะ fail ยังไง พร้อมอธิบาย root cause ของแต่ละปัญหา:

**CSV file (`users.csv`):**
```
username,password,role
john@test.com,pass123,admin
jane@test.com,secret!@#,user
bob@test.com,my pass word,manager
```

**JMeter CSV Data Set Config:**
- Delimiter: `,`
- Variable Names: (ว่างเปล่า — ไม่ได้ระบุ)
- Allow quoted data: false

**HTTP Request Body:**
```
{"username": "${username}", "password": "${password}", "role": "${role}"}
```

<details>
<summary>ดูเฉลย</summary>

**ปัญหาที่ 1: Variable Names ว่างเปล่า**
- root cause: ถ้าไม่ระบุ Variable Names JMeter จะใช้ header row เป็น column names ซึ่งถูกต้อง — **แต่** ถ้า CSV ไม่มี header row จะทำให้ first row ของ data ถูกข้ามไป (ถือว่าเป็น header)
- ในกรณีนี้ CSV มี header "username,password,role" → JMeter จะใช้เป็น column names ถูกต้อง แต่ถ้าลืมใส่ header → data row แรกจะหาย

**ปัญหาที่ 2: `bob@test.com,my pass word,manager`**
- root cause: `my pass word` มี space → ไม่มีปัญหากับ comma delimiter แต่ถ้า HTTP encoding ไม่ถูกต้อง password จะกลายเป็น `my+pass+word` หรือ `my%20pass%20word`
- ต้องตั้ง HTTP Request ให้ encode correctly หรือ escape ค่าใน Body

**ปัญหาที่ 3: `jane@test.com,secret!@#,user`**
- root cause: `!@#` เป็น special characters ที่อาจถูก interpret ผิดใน JSON string — ถ้าไม่ escape ถูกต้องใน request body JSON จะ malformed
- ต้องใช้ JSR223 PreProcessor เพื่อ escape special chars หรือ URL-encode ก่อน

**ปัญหาที่ 4: Allow quoted data = false**
- root cause: ถ้า column มีค่าที่มีคอมมาอยู่ข้างใน (เช่น `"Smith, John"`) JMeter จะ split ผิดตำแหน่ง เพราะไม่ parse quoted fields
- ควรตั้ง Allow quoted data = true เสมอ เพื่อรองรับ edge cases

</details>

---

## กลุ่มที่ 5: Timers และ Assertions
> ทำหลังอ่านบทที่ 5

### Exercise 5.1 — Beginner (Recall)

อธิบายว่า Think Time คืออะไร และทำไมการไม่ใส่ Timer ใน Test Plan ถึงทำให้ผลของ load test "ไม่ตรงความจริง" — ยกตัวอย่างพฤติกรรมของ real user ที่ Timer พยายาม simulate

<details>
<summary>ดูเฉลย</summary>

**Think Time:** ระยะเวลาที่ real user ใช้ในการ "คิด" หรือ "อ่าน" ระหว่าง actions — เช่น หลัง page โหลด user ใช้เวลาอ่าน content 3-5 วินาที ก่อนคลิกปุ่มถัดไป

**ทำไมไม่ใส่ Timer ถึงไม่ตรงความจริง:**
JMeter thread จะส่ง request ถัดไปทันทีหลัง receive response — ไม่มี pause เลย แปลว่า 50 virtual users จะ hammer server ด้วย rate ที่เร็วกว่า real users หลายเท่า

ตัวอย่าง real user behavior:
- User เข้า homepage (3 วินาที) → อ่านสินค้า → คลิก product page (5 วินาที) → อ่าน description → add to cart (2 วินาที) → กรอกที่อยู่
- Think time ระหว่าง action เฉลี่ย 2-8 วินาที

ถ้าไม่ใส่ Timer: test จะ overestimate load ที่ server รับต่อ concurrent user 1 คน ทำให้ผล throughput สูงกว่าที่ real users จะสร้างได้จริง

</details>

---

### Exercise 5.2 — Intermediate (Application)

สถานการณ์: ทีม test ของ EdTech company กำลัง test API `/course/lessons` ที่ return JSON ดังนี้:

```json
{
  "status": "success",
  "data": {
    "lessons": [...],
    "total_count": 42,
    "course_id": "CS101"
  }
}
```

ออกแบบ Assertions สำหรับ request นี้ที่ครอบคลุมทั้ง HTTP layer และ Business Logic layer — ระบุ Assertion type แต่ละตัวและ configuration

<details>
<summary>ดูเฉลย</summary>

**Layer 1: HTTP Layer**
- **Response Assertion** — Response Code
  - Field to test: Response Code
  - Pattern: `200`
  - Test type: Equals

**Layer 2: Content/Format Layer**
- **Response Assertion** — Content-Type header
  - Field to test: Response Headers
  - Pattern: `application/json`
  - Test type: Contains

**Layer 3: Business Logic Layer**
- **JSON Assertion** (หรือ JSON Path Assertion)
  - JSON Path: `$.status`
  - Expected value: `success`

- **JSON Assertion** — ตรวจ course_id ถูกต้อง
  - JSON Path: `$.data.course_id`
  - Expected value: `CS101` (ต้อง match กับ input ที่ส่งไป — ต้องใช้ variable ถ้า parameterize)

- **JSON Assertion** — ตรวจ total_count เป็น number > 0
  - JSON Path: `$.data.total_count`
  - Expected value: `[1-9][0-9]*` (regex)
  - Test type: Matches

- **Duration Assertion** — response ต้องมาภายใน 3 วินาที
  - Duration in milliseconds: `3000`

**สิ่งที่ไม่ควรทำ:** ตรวจ lesson content แบบ hard-coded ใน assertion เพราะ data เปลี่ยนได้ — ตรวจแค่ structure และ business invariants

</details>

---

### Exercise 5.3 — Advanced (Synthesis)

ทีม performance engineer สังเกตว่าหลัง deploy ใหม่:
- Load test ผ่าน (error% = 0.2%)
- แต่ production monitoring แสดงว่า user complaint เรื่อง "ได้ข้อมูลเก่า" เพิ่มขึ้น

สิ่งที่ทีมทำ: ดู JMeter log พบว่า Response Code = 200 ทุก request และ Assertion ที่ตั้งไว้คือ "Response Code = 200" เท่านั้น

วิเคราะห์ว่า Assertion strategy นี้บกพร่องอย่างไร ออกแบบ Assertion strategy ใหม่ที่ catch ปัญหา "stale data" ได้ และอธิบายว่าต้อง correlate กับ API behavior อะไร

<details>
<summary>ดูเฉลย</summary>

**ปัญหาของ Assertion strategy เดิม:**
Response Code 200 = "HTTP request สำเร็จ" แต่ไม่ได้บอกว่า "ข้อมูลที่ได้ถูกต้อง" — server อาจ return 200 พร้อม cached/stale data ที่อายุเก่า หรือ return data จาก database replica ที่ replication lag สูง

**วิธีที่ Assertion จะ catch ปัญหา stale data:**

1. **Timestamp Assertion:** ถ้า API return `last_updated` field
   - JSON Path: `$.data.last_updated`
   - ตรวจว่าค่าไม่เก่ากว่า N วินาที (ต้องใช้ JSR223 Assertion เพราะ JSON Path Assertion ไม่รองรับ dynamic comparison)
   - ตัวอย่าง JSR223 Groovy:
   ```groovy
   def body = prev.getResponseDataAsString()
   def json = new groovy.json.JsonSlurper().parseText(body)
   def lastUpdated = json.data.last_updated  // Unix timestamp
   def now = System.currentTimeMillis() / 1000
   if ((now - lastUpdated) > 30) {  // stale ถ้าเก่ากว่า 30 วินาที
       AssertionResult.setFailure(true)
       AssertionResult.setFailureMessage("Data is stale: ${now - lastUpdated}s old")
   }
   ```

2. **Version/ETag Assertion:** ถ้า API ใช้ `version` หรือ `ETag` header
   - ตรวจว่า version ใน response ตรงกับ version ที่ expect หลัง update

3. **Write-then-Read Verification:**
   - POST (เขียนข้อมูลใหม่) → GET (อ่านข้อมูลเดิม) → Assertion ว่าข้อมูลที่อ่านได้ = ข้อมูลที่เพิ่งเขียน
   - ต้องใช้ Extractor เก็บค่าจาก POST response และ Assertion ใน GET step

**Lesson:** Assertion ต้องสะท้อน business correctness ไม่ใช่แค่ HTTP protocol correctness

</details>

---

## กลุ่มที่ 6: Listeners และ Reports
> ทำหลังอ่านบทที่ 6

### Exercise 6.1 — Beginner (Recall)

ระบุว่า Listener แต่ละตัวต่อไปนี้เหมาะกับสถานการณ์ไหน และอันไหนที่ห้ามใช้ระหว่าง load test จริง: View Results Tree, Aggregate Report, Summary Report, View Results in Table

<details>
<summary>ดูเฉลย</summary>

| Listener | เหมาะกับ | ข้อจำกัด |
|----------|---------|---------|
| View Results Tree | Debug / Scripting phase — ดู response body จริง | ❌ ห้ามใช้ระหว่าง load test — เก็บ response body ทุกชิ้น กิน memory มหาศาล |
| View Results in Table | Debug — ดูผลทีละ row | ❌ ห้ามใช้ระหว่าง load test — เหตุผลเดียวกัน |
| Summary Report | ✅ ระหว่าง load test รัน — real-time monitoring | ไม่มี Median และ 90th percentile |
| Aggregate Report | ✅ หลัง test เสร็จ หรือ debug — ต้องการ percentile ครบ | Memory usage สูงกว่า Summary Report |

**หลักการ:** ระหว่าง load test ใช้ Listener น้อยที่สุด ดีที่สุดคือรัน non-GUI และดูผลจาก HTML Dashboard Report แทน

</details>

---

## กลุ่มที่ 7: Running & Reports
> ทำหลังอ่านบทที่ 7

### Exercise 7.1 — Beginner (Recall)

อธิบายว่า `jmeter -n -t test.jmx -l results.jtl -e -o report/` แต่ละ flag ทำอะไร และถ้าต้องการ generate HTML Report จาก JTL file ที่มีอยู่แล้ว (ไม่ต้องรัน test ใหม่) จะใช้ command อะไร?

<details>
<summary>ดูเฉลย</summary>

| Flag | หน้าที่ |
|------|--------|
| `-n` | Non-GUI mode (CLI) — บังคับสำหรับ load test |
| `-t test.jmx` | ระบุ Test Plan file |
| `-l results.jtl` | บันทึก raw results ไปยัง JTL file |
| `-e` | Generate HTML Dashboard Report หลัง test เสร็จ |
| `-o report/` | ระบุ output folder สำหรับ HTML report |

**Generate report จาก existing JTL:**
```bash
jmeter -g results.jtl -o report/
```
ใช้ `-g` (generate) แทน `-t` (test) + `-n` — ไม่ต้องรัน test ซ้ำ

</details>

---

### Exercise 7.2 — Intermediate (Application)

สถานการณ์: คุณกำลัง test API สำหรับ payment gateway ของ fintech startup ที่ endpoint `POST /api/v1/payments` ซึ่งรับ JSON body และ return response พร้อม `transaction_id`

**ตัวอย่าง response เมื่อ success:**
```json
{
  "status": "approved",
  "transaction_id": "TXN-20260318-084521",
  "amount": 2500.00,
  "currency": "THB"
}
```

**ตัวอย่าง response เมื่อ fail:**
```json
{
  "status": "declined",
  "error_code": "INSUFFICIENT_FUNDS",
  "transaction_id": ""
}
```

SLA ของ payment API กำหนดว่า:
- Response time ต้องไม่เกิน 2,000ms
- `transaction_id` ต้องมีค่าและไม่ว่างเปล่าเมื่อ status = "approved"
- Status code ต้องเป็น 201 (Created) เสมอไม่ว่า payment จะ approved หรือ declined

**โจทย์:** ออกแบบ Assertions สำหรับ test plan นี้ที่ครอบคลุม:
1. ตรวจว่า status code เป็น 201
2. ตรวจว่า `transaction_id` มีอยู่และไม่ว่าง (สำหรับกรณี approved)
3. ตรวจว่า response time ไม่เกิน 2,000ms

ระบุ Assertion ประเภทไหนสำหรับแต่ละข้อ, fields ที่ตั้งค่า, และเหตุผลว่าทำไม

<details>
<summary>ดูเฉลย</summary>

**Assertion 1: ตรวจ Status Code 201**

```
Assertion type: Response Assertion
Apply To:       Main sample only
Field to Test:  Response Code
Pattern Matching Rules: Equals
Patterns to Test: 201
```

เหตุผล: ใช้ `Equals` กับ `Response Code` field เพราะ status code เป็นค่าที่แน่นอน — ถ้าใช้ `Response Body` แล้ว server return 500 จะ confuse กับ body content ได้

**Assertion 2: ตรวจว่า transaction_id มีอยู่และไม่ว่าง**

```
Assertion type: JSON Assertion
Assert JSON Path exists:     $.transaction_id
Additionally assert value:   ✓ (เปิด)
Expected value:              TXN-.+
Match as regular expression: ✓ (เปิด)
```

เหตุผล: ใช้ regex `TXN-.+` เพื่อตรวจว่า transaction_id ขึ้นต้นด้วย "TXN-" และมีข้อความตามหลัง (`.+` = 1 ตัวขึ้นไป ไม่ใช่ empty string) — ครอบคลุมทั้ง "มีอยู่" และ "ไม่ว่าง" ในขั้นตอนเดียว

**หมายเหตุ:** ถ้าต้องการ assert เฉพาะ approved case ให้เพิ่ม JSON Assertion อีกตัวสำหรับ `$.status` ด้วย expected value `approved` แล้ว chain assertions ทั้งสองเข้าด้วยกัน

**Assertion 3: ตรวจ Response Time ไม่เกิน 2,000ms**

```
Assertion type: Duration Assertion
Duration (ms): 2000
```

เหตุผล: Duration Assertion ทำให้ JMeter นับ request ที่ช้าเกิน SLA เป็น failure ใน error rate — ทำให้ report สะท้อน "กี่ % ของ payment transactions ช้าเกิน 2 วินาที" แบบ real-time ไม่ต้องรอดู percentile หลัง test จบ

**ทำไมต้องใช้ทั้ง 3 Assertions ร่วมกัน:**
Payment API ที่ดีต้องผ่านทั้งสามเงื่อนไขพร้อมกัน — status code ถูก + ข้อมูลครบถ้วน + เร็วพอ ถ้า fail ที่ assertion ใด assertion หนึ่ง engineering team จะรู้ทันทีว่าปัญหาอยู่ที่ layer ไหน

</details>

---

### Exercise 7.3 — Advanced (Synthesis)

ดู Aggregate Report ต่อไปนี้จาก production-like load test:

| Label | Samples | Average | Median | 90% Line | 99% Line | Error% | Throughput |
|-------|---------|---------|--------|----------|----------|--------|------------|
| POST /api/auth | 5,000 | 120ms | 105ms | 195ms | 420ms | 0.1% | 82/s |
| GET /api/products | 25,000 | 890ms | 340ms | 3,200ms | 8,900ms | 2.1% | 410/s |
| POST /api/cart | 10,000 | 180ms | 170ms | 285ms | 510ms | 0.2% | 165/s |
| POST /api/checkout | 5,000 | 2,400ms | 1,800ms | 6,500ms | 12,000ms | 8.5% | 78/s |

วิเคราะห์และบอกว่า:
1. Bottleneck หลักอยู่ที่ใด และอาจมาจากอะไร
2. Error% ของ `/api/products` และ `/api/checkout` น่าจะ error ประเภทต่างกัน — อธิบายเหตุผล
3. Recommendations สำหรับ engineering team (แยกตามลำดับความสำคัญ)

<details>
<summary>ดูเฉลย</summary>

**1. Bottleneck Analysis:**

**Bottleneck ที่ 1: `/api/checkout`** — รุนแรงที่สุด
- Average 2,400ms, p90 = 6,500ms, p99 = 12,000ms, Error 8.5%
- Pattern: average สูง + percentile สูงมาก + error สูง = endpoint มีปัญหาพื้นฐาน ไม่ใช่แค่ tail latency
- สาเหตุที่เป็นไปได้: database transaction ที่ lock row นาน (payment + inventory update พร้อมกัน), external payment gateway ช้า, หรือ N+1 query problem

**Bottleneck ที่ 2: `/api/products`** — น่ากังวล
- Average 890ms สูง แต่ Median แค่ 340ms — gap ใหญ่มาก → มี outliers ดึง average ขึ้น
- p90 = 3,200ms, p99 = 8,900ms — tail latency รุนแรง
- สาเหตุที่เป็นไปได้: database query ไม่มี index, N+1 query, หรือ cache miss สำหรับ popular products

**2. Error Type Analysis:**

`/api/products` Error 2.1%: น่าจะเป็น **timeout** — เพราะ p99 = 8,900ms และ JMeter มักตั้ง Connection Timeout ที่ 5-10 วินาที requests ที่ใช้เวลานานมากจะ timeout และกลายเป็น error

`/api/checkout` Error 8.5%: น่าจะเป็น **business logic error หรือ 5xx** — เพราะ error rate สูงมากพร้อมกับ latency สูง บ่งชี้ว่า server เกิด error จริงๆ (database deadlock, payment gateway reject) ไม่ใช่แค่ช้า

**3. Recommendations (Priority Order):**

🔴 **P0 - แก้ก่อน release:**
- `/api/checkout`: ตรวจ database transaction logs หา deadlock หรือ long-running transaction, ตรวจ payment gateway timeout configuration, เพิ่ม error logging เพื่อ classify error 8.5% ว่าคืออะไร

🟡 **P1 - แก้ภายใน sprint:**
- `/api/products`: เพิ่ม database index สำหรับ product query, implement caching layer สำหรับ popular products, ตรวจสอบ N+1 query pattern

🟢 **P2 - Monitor:**
- `/api/auth` และ `/api/cart` อยู่ในเกณฑ์ดี — แค่ monitor ไม่ต้องแก้เร่งด่วน

</details>

---

## กลุ่มที่ 8: Distributed Testing และ CI/CD
> ทำหลังอ่านบทที่ 8

### Exercise 8.1 — Beginner (Recall)

ใน JMeter Distributed Testing: Controller Node และ Worker Node ต่างกันอย่างไร และถ้าตั้ง Thread Group เป็น 200 threads แล้วมี Worker 4 ตัว — total concurrent users จะเป็นเท่าไหร่และทำไม?

<details>
<summary>ดูเฉลย</summary>

**Controller Node:** เครื่องที่รัน JMeter GUI หรือ CLI — ทำหน้าที่ควบคุม test (ส่ง Test Plan ไปยัง Workers, รวม results) แต่ไม่ส่ง HTTP requests เอง

**Worker Node:** เครื่องที่รัน `jmeter-server` — รับ Test Plan จาก Controller แล้ว generate load จริงๆ (ส่ง HTTP requests ไปยัง target server)

**Total concurrent users = 200 × 4 = 800 users**

เหตุผล: JMeter ไม่แบ่ง load ระหว่าง Workers — แต่ละ Worker รัน Test Plan เดิมทั้งหมด ดังนั้น Worker แต่ละตัวมี 200 threads → รวม 4 Workers = 800 concurrent virtual users

</details>

---

### Exercise 8.2 — Intermediate (Application)

สถานการณ์: ทีม DevOps ของบริษัท streaming video ต้องการ test API `/video/stream/start` ที่ต้องรับ 8,000 concurrent streaming sessions ในช่วง prime time เครื่องแต่ละเครื่องสามารถ run JMeter ได้ 1,200 threads อย่างน่าเชื่อถือ

ออกแบบ: จำนวน Worker Nodes ที่ต้องการ, Thread Group configuration, remote_hosts configuration, และ CLI command ที่ถูกต้อง

<details>
<summary>ดูเฉลย</summary>

**จำนวน Workers ที่ต้องการ:**
- 8,000 ÷ 1,200 = 6.67 → ต้องการ **7 Workers** (round up)
- 7 Workers × 1,143 threads = 8,001 virtual users (ปรับ threads ให้ได้ 8,000 พอดี: 6 Workers × 1,143 + 1 Worker × 1,142 = 8,000)
- วิธีง่ายกว่า: ใช้ **7 Workers × 1,143 threads** = 8,001 ≈ 8,000 (ต่างแค่ 1 ไม่มีนัยสำคัญ)

**Thread Group Configuration:**
- Number of Threads: 1,143
- Ramp-Up Period: 300 วินาที (5 นาที — ramp-up ค่อยๆ เพื่อ simulate prime time surge)
- Duration: 3,600 วินาที (1 ชั่วโมง — simulate prime time window)

**remote_hosts (jmeter.properties):**
```properties
remote_hosts=10.0.1.10,10.0.1.11,10.0.1.12,10.0.1.13,10.0.1.14,10.0.1.15,10.0.1.16
```

**CLI Command:**
```bash
jmeter -n \
  -t streaming_test.jmx \
  -R 10.0.1.10,10.0.1.11,10.0.1.12,10.0.1.13,10.0.1.14,10.0.1.15,10.0.1.16 \
  -l results/streaming_$(date +%Y%m%d).jtl \
  -e \
  -o results/report_$(date +%Y%m%d)/
```

**Checklist ก่อนรัน:**
- ✅ JMeter version ตรงกันทุก node
- ✅ ปิด antivirus ทุก Worker
- ✅ Workers อยู่ใน subnet เดียวกับ Controller
- ✅ firewall เปิด RMI port (1099)

</details>

---

### Exercise 8.3 — Advanced (Synthesis)

ทีม engineering ต้องการ integrate JMeter เข้า GitHub Actions pipeline แต่พบปัญหา:

1. Distributed Workers อยู่ใน on-premise network
2. GitHub Actions runners เป็น cloud-hosted (ไม่อยู่ใน network เดียวกับ Workers)
3. ทีมต้องการ fail pipeline ถ้า p95 > 2,000ms หรือ error > 0.5%
4. Report ต้องเก็บใน S3 สำหรับ historical comparison

ออกแบบ architecture และ pipeline strategy ที่แก้ปัญหาทุกข้อ — ไม่จำเป็นต้อง provide code ทั้งหมด แต่ต้อง describe approach อย่างละเอียด

<details>
<summary>ดูเฉลย</summary>

**ปัญหาหลัก:** GitHub Actions cloud runners ไม่สามารถ connect ถึง on-premise Workers ผ่าน RMI ได้

**Architecture Solution:**

**Option A: Self-Hosted Runner (แนะนำ)**
- ติดตั้ง GitHub Actions self-hosted runner บนเครื่องในเครือข่าย on-premise
- Runner นี้อยู่ใน subnet เดียวกับ Workers → RMI ทำงานได้
- GitHub Actions จะ trigger runner บน on-premise โดยอัตโนมัติ

```yaml
jobs:
  performance:
    runs-on: [self-hosted, performance-controller]  # ระบุ runner label
```

**Option B: VPN Tunnel**
- เปิด GitHub Actions cloud runner แล้วต่อ VPN เข้า on-premise network ก่อน
- ใช้ `tailscale` หรือ WireGuard setup ใน pipeline step

**Threshold Validation (p95 > 2000ms หรือ error > 0.5%):**

```python
# Python script สำหรับ validate (รันใน CI step)
import csv, sys

results = []
with open('results.jtl') as f:
    for row in csv.DictReader(f):
        results.append({'elapsed': int(row['elapsed']), 'success': row['success'] == 'true'})

total = len(results)
errors = sum(1 for r in results if not r['success'])
sorted_times = sorted(r['elapsed'] for r in results)
p95 = sorted_times[int(total * 0.95)]
error_pct = errors / total * 100

failed = p95 > 2000 or error_pct > 0.5
if failed:
    print(f"FAIL: p95={p95}ms, error={error_pct:.2f}%")
    sys.exit(1)
```

**S3 Upload สำหรับ Historical Comparison:**

```bash
# Upload report และ JTL ไปยัง S3 พร้อม timestamp + git SHA เป็น key
aws s3 sync test-results/report/ \
  s3://perf-reports/jmeter/${GITHUB_SHA}/ \
  --metadata "commit=${GITHUB_SHA},date=$(date -I),branch=${GITHUB_REF_NAME}"

# เก็บ raw JTL สำหรับ re-analysis
aws s3 cp test-results/results.jtl \
  s3://perf-reports/jtl/${GITHUB_SHA}_results.jtl
```

**Historical Comparison Strategy:**
- เก็บ baseline p95/p99/error% ของ main branch ล่าสุดใน S3 (หรือ Parameter Store)
- ก่อน fail pipeline ด้วย hard threshold ให้ compare กับ baseline ด้วย — เช่น fail ถ้า p95 deteriorate > 20% จาก baseline (regression detection)

</details>

---

## Mixed Review — ทำหลังอ่านครบ Series

> ⚠️ **Mixed Review ตั้งใจให้รู้สึกยากกว่าตอนที่เพิ่งอ่านจบแต่ละบท — นั่นคือสัญญาณที่ดี**
> ความรู้สึก "ยาก" หรือ "จำไม่ได้แน่ๆ" ขณะทำ Mixed Review คือสัญญาณว่า interleaving กำลังทำงาน สมองกำลัง retrieve และ consolidate — ทนต่อความรู้สึกนั้นและ struggle ให้นานก่อนดูเฉลย

---

### Mixed Review 1 — Discrimination Task

สถานการณ์ต่อไปนี้ควรใช้ **Constant Timer**, **Uniform Random Timer**, หรือ **Gaussian Random Timer**? อธิบายเหตุผลและระบุว่าทำไมจึงไม่เลือกตัวอื่น

**สถานการณ์ A:** Test API ของ batch processing system ที่รับ requests จาก scheduled job ที่รันทุก 5 วินาทีพอดี (ไม่มี human behavior)

**สถานการณ์ B:** Simulate การเข้าเว็บไซต์ e-commerce ของ real users ที่พฤติกรรมการ browse ไม่แน่นอน บางคนอ่านนาน บางคนคลิกเร็ว ส่วนใหญ่อยู่ในช่วง 2-8 วินาที

**สถานการณ์ C:** Simulate behavior ของ user กลุ่มหนึ่งที่ผ่านการ usability testing แล้ว พบว่า think time กระจายแบบ normal distribution รอบ mean 4 วินาที ± 1.5 วินาที

<details>
<summary>ดูเฉลย</summary>

**สถานการณ์ A → Constant Timer**
- เหตุผล: batch job รันสม่ำเสมอ ไม่มี variance — ต้องการ think time คงที่ที่ 5,000ms
- ทำไมไม่ใช้ Uniform/Gaussian: จะเพิ่ม random variance ที่ไม่ match behavior จริงของ system-to-system integration

**สถานการณ์ B → Uniform Random Timer**
- เหตุผล: รู้แค่ range (2-8 วินาที) ไม่รู้ distribution shape → Uniform distribution เหมาะกับ "ไม่รู้ว่า peak อยู่ที่ไหน"
- ทำไมไม่ใช้ Gaussian: Gaussian มี peak ที่ mean ซึ่งต้องการ data จริงว่า distribution shape เป็นอย่างไร ถ้าไม่มีข้อมูล Uniform ปลอดภัยกว่า
- ทำไมไม่ใช้ Constant: real user behavior ไม่สม่ำเสมอ การใช้ constant จะทำให้ test ไม่ realistic

**สถานการณ์ C → Gaussian Random Timer**
- เหตุผล: มีข้อมูลจริงที่บอกว่า distribution เป็น normal curve รอบ mean 4 วินาที → ตั้ง `Constant Delay Offset = 4000ms` และ `Deviation = 1500ms`
- ทำไมไม่ใช้ Uniform: Uniform ไม่มี "most likely value" — แต่ข้อมูลบอกชัดว่าส่วนใหญ่อยู่รอบ 4 วินาที
- ทำไมไม่ใช้ Constant: มี variance ที่วัดแล้ว ต้องสะท้อนใน test

</details>

---

### Mixed Review 2 — Mixed Concept

ออกแบบ distributed test ดังนี้:
- **3 Worker Nodes** แต่ละตัวมี **50 threads**, ramp-up 30 วินาที, loop count 10
- **CSV Data Set Config** ที่ Sharing mode = **All threads**, มี **100 rows** ของ test data
- Test มี 2 endpoints: `GET /search` (อ่าน) และ `POST /order` (เขียน) ใน sequence

**Describe ปัญหาที่จะเกิดขึ้นจริง** (อาจมีมากกว่า 1 ปัญหา) และเสนอวิธีแก้

<details>
<summary>ดูเฉลย</summary>

**ปัญหาที่ 1: Data exhaustion (Critical)**

- Total virtual users = 50 × 3 = 150 users
- Total requests per user = 10 loops × (1 search + 1 order) = 20 requests per user
- Total data reads = 150 users × 10 loops = 1,500 reads จาก CSV ที่มีแค่ 100 rows
- ผล: หลังใช้ data ไปแล้ว 100 rows users ที่เหลือจะได้ข้อมูลซ้ำ (ถ้า Recycle = true) หรือ stop (ถ้า Recycle = false)

**วิธีแก้:** เพิ่ม CSV เป็น 1,500+ rows หรือตั้ง Recycle on EOF = true (ยอมรับว่าจะใช้ data ซ้ำ)

**ปัญหาที่ 2: Sharing mode "All threads" ใน distributed context**

- Sharing mode "All threads" บน Worker แต่ละตัวหมายความว่า threads ใน Worker เดียวกันแชร์ CSV data pointer ร่วมกัน ✅
- **แต่:** Workers คนละเครื่องไม่แชร์ pointer กัน — Worker 1, 2, 3 ต่างอ่าน CSV ตั้งแต่ row 1 ใหม่คนละตัว
- ผล: data ถูกใช้ซ้ำ 3 เท่า (แต่ละ Worker ใช้ชุด data เดิม)
- ในกรณีที่ `/order` ต้องการ **unique order data** (เช่น unique order ID) จะเกิด duplicate order

**วิธีแก้:**
- แบ่ง CSV เป็น 3 ไฟล์ (Worker 1 ใช้ rows 1-50, Worker 2 ใช้ rows 51-100, ฯลฯ)
- หรือ generate unique ID ใน JSR223 PreProcessor แทนที่จะอ่านจาก CSV

**ปัญหาที่ 3: `POST /order` อาจมี side effects ใน test environment**

- Loop 10 ครั้ง × 150 users = 1,500 orders จริงๆ ใน test database
- ต้องมี data cleanup strategy หรือใช้ separate test environment ที่ reset ได้

</details>

---

### Mixed Review 3 — Elaborative Interrogation

**ทำไม JMeter ถึงออกแบบให้ each Worker runs the full test plan แทนที่จะ distribute load automatically?**

เขียน argument ที่สมบูรณ์ว่า design decision นี้มาจาก trade-off อะไร และมีข้อดีอะไรที่อาจไม่เห็นชัดตอนแรก

<details>
<summary>ดูเฉลย</summary>

**Design Decision: Each Worker runs full test plan**

**เหตุผลด้านความเรียบง่าย (Simplicity):**
- ถ้า JMeter แบ่ง load อัตโนมัติ Controller ต้อง coordinate thread distribution, data partitioning, และ result aggregation ซึ่งซับซ้อนมาก
- Error ใน coordination layer จะทำให้ผลลัพธ์ไม่น่าเชื่อถือ — ยากต่อการ debug ว่า error มาจาก test logic หรือ distribution logic
- "Simple and predictable" เป็น design principle ที่ดีสำหรับ testing tools

**ข้อดีที่ไม่เห็นชัดตอนแรก:**

1. **Identical behavior ทุก Worker:** แต่ละ Worker รัน test เดิม 100% → ถ้า test มี bug หรือ timing issue มันจะปรากฏเหมือนกันทุก Worker ทำให้ debug ง่ายขึ้น ถ้า distribute แตกต่างกัน bug อาจปรากฏแค่บาง Worker

2. **Predictable total load:** User รู้แน่ว่า total load = threads × workers ไม่ต้องเดา การควบคุมตรงไปตรงมา

3. **Independent failure:** ถ้า Worker หนึ่งล้มเหลว Workers อื่นยังรันต่อได้อิสระ ถ้า distribute centrally การล้มของ coordinator จะ kill test ทั้งหมด

4. **No data partitioning complexity:** ถ้า JMeter distribute load จะต้อง partition test data ด้วย — ซึ่งต้อง solve data dependency (เช่น user A ต้องใช้ order ของตัวเองเท่านั้น) ซึ่งซับซ้อนมาก

**Trade-off ที่ยอมรับ:**
- User ต้องเข้าใจ behavior และตั้ง threads ให้ถูกต้อง — มีโอกาส misconfiguration สูงขึ้น
- ไม่มี automatic load balancing ถ้า Worker บางตัวช้ากว่า

**สรุป:** Design นี้ optimize เพื่อ correctness และ predictability แลกกับ convenience ซึ่งเหมาะสำหรับ testing tool ที่ผลลัพธ์ต้องน่าเชื่อถือเป็นอันดับแรก

</details>

---

## Advanced Exercises — Cross-Series

### Advanced 1: Performance Report Analysis

ดู mock Aggregate Report จากระบบ API gateway:

| Label | Samples | Average | 90% Line | Error% | Throughput |
|-------|---------|---------|----------|--------|------------|
| GET /api/v1/users | 50,000 | 45ms | 89ms | 0.0% | 825/s |
| POST /api/v1/users | 2,000 | 320ms | 780ms | 0.5% | 32/s |
| GET /api/v1/orders | 30,000 | 1,200ms | 4,500ms | 3.2% | 490/s |
| GET /api/v1/orders/{id} | 20,000 | 95ms | 180ms | 0.1% | 328/s |
| DELETE /api/v1/orders/{id} | 500 | 250ms | 480ms | 12.0% | 8/s |

วิเคราะห์ว่า:
1. Bottleneck หลักคืออะไร และมีสาเหตุที่เป็นไปได้อะไรบ้าง
2. `DELETE /api/v1/orders/{id}` Error 12% น่าจะมาจากอะไร (ไม่ใช่แค่ "server error" — ต้องเจาะจงกว่านี้)
3. Recommendations เรียงตาม priority

<details>
<summary>ดูเฉลย</summary>

**1. Bottleneck Analysis:**

**Bottleneck หลัก: `GET /api/v1/orders` (list)**
- Average 1,200ms สูงมาก เปรียบกับ `GET /api/v1/orders/{id}` (individual) ที่แค่ 95ms
- p90 = 4,500ms → 10% ของ requests รอนานกว่า 4.5 วินาที
- Error 3.2% บ่งชี้ว่า request จำนวนหนึ่ง timeout

สาเหตุที่น่าจะเป็น:
- **N+1 Query:** list endpoint อาจ query order แต่ละรายการแยก แทนที่จะ batch query
- **Missing pagination/index:** ถ้า return orders ทั้งหมดของ user โดยไม่มี limit และไม่มี index บน user_id จะช้ามาก
- **No caching:** list มักถูก cache ยากกว่า individual item

**2. DELETE Error 12%:**

Error 12% บน DELETE มักไม่ใช่ server crash แต่น่าจะเป็น:
- **Business rule violation:** delete order ที่อยู่ใน "shipped" หรือ "completed" state ไม่ได้ → server return 422 Unprocessable Entity หรือ 409 Conflict
- **Concurrent modification:** สอง users พยายาม delete order เดิมพร้อมกัน → หนึ่งสำเร็จ อีกหนึ่ง 404
- **Missing Authorization:** test data ที่ใช้ทำ DELETE อาจไม่ใช่ owner ของ order → 403 Forbidden
- **Race condition กับ POST:** ถ้า test create order แล้ว delete เร็วมาก order อาจยังอยู่ใน "processing" state ที่ delete ไม่ได้

**3. Recommendations (Priority):**

🔴 P0: ตรวจ Error ใน DELETE (ดู error type จาก Error table)  — ถ้าเป็น 422/409 อาจเป็น test data problem ไม่ใช่ bug จริง

🔴 P0: แก้ `GET /api/v1/orders` — เพิ่ม pagination บังคับ (limit/offset), เพิ่ม database index บน `user_id` + `created_at`, ตรวจ query ว่ามี N+1 ไหม

🟡 P1: ตรวจ `POST /api/v1/users` Error 0.5% — อาจเป็น validation error (test data ไม่ valid) หรือ duplicate user

🟢 P2: Monitor `GET /api/v1/users` ต่อไป — ปัจจุบันดีมาก (45ms, 0% error)

</details>

---

### Advanced 2: Far Transfer — System Design

> ⚠️ Exercise นี้ไม่บอกว่าต้องใช้ tool ไหน คุณต้องตัดสินใจเองจากความรู้ที่มี

**สถานการณ์:** ระบบ logistics ของบริษัทขนส่งมี API ที่ต้องรับ **10,000 shipment status updates ใน 1 นาที** ในช่วง peak hour (เช้า 8-9 โมง) และ API ยังต้องตอบ query จาก tracking web app ที่มี 50,000 unique visitors ต่อวัน

ออกแบบ **complete performance test strategy** สำหรับระบบนี้ก่อน production launch โดยระบุ:
1. ประเภท test ที่ต้องรัน (ทั้งหมด) และลำดับ
2. Tool(s) ที่เลือกใช้และเหตุผล
3. Thread/Load configuration ที่สำคัญ
4. Metrics และ acceptance criteria
5. ปัญหาที่อาจเกิดและวิธีรับมือ

<details>
<summary>ดูเฉลย (แนวทาง — คำตอบไม่มีแบบเดียว)</summary>

**1. ประเภท Test และลำดับ:**

```
Smoke Test → Load Test → Stress Test → Spike Test → Endurance Test
```

- **Smoke Test (5 users, 5 นาที):** verify ทุก API endpoint ทำงานก่อน — ไม่ต้องรัน full test ถ้า basic function พัง
- **Load Test (100% expected load):** 10,000 updates/min = ~167 req/s update API + 50,000 visitors/day = ~580 req/s peak query (estimate) — รัน 1 ชั่วโมงเพื่อ validate SLA
- **Stress Test:** เพิ่ม load ทีละ 20% จนระบบ fail — หา breakpoint และ error behavior
- **Spike Test:** simulate peak hour surge — เพิ่ม load จาก normal เป็น 10,000 updates/min ใน 5 นาที แล้วลดกลับ
- **Endurance Test (8 ชั่วโมง):** simulate full business day — ตรวจ memory leak, connection leak

**2. Tool Selection:**

- **JMeter** สำหรับ REST API testing (shipment update + tracking query)
  - ถ้า 10,000 updates/min เกิน capacity เครื่องเดียว → Distributed Testing (3-5 Workers)
- **k6 หรือ Gatling** เป็น alternative ถ้าทีมต้องการ code-based test ที่ maintainable กว่า JMX
- **Grafana + InfluxDB** สำหรับ real-time monitoring ระหว่าง test (JMeter Backend Listener ส่งผลไป InfluxDB)

**3. Load Configuration:**

- **Update API:** 167 req/s sustained — ถ้า Distributed ใช้ 3 Workers × 56 threads = 168 threads
- **Query API:** 580 req/s peak — ramp-up 5 นาที, hold 55 นาที
- Timer: ไม่ใส่ Think Time สำหรับ update API (system-to-system) แต่ใส่ Gaussian Timer สำหรับ query API (human behavior)

**4. Metrics และ Acceptance Criteria:**

| Metric | Acceptance Criterion |
|--------|---------------------|
| Update API p99 | < 500ms |
| Query API p95 | < 2,000ms |
| Error rate (update) | < 0.1% |
| Error rate (query) | < 0.5% |
| Throughput (update) | ≥ 167 req/s sustained |
| No degradation after 8h | Endurance — p99 ไม่เพิ่ม > 10% |

**5. ปัญหาที่อาจเกิดและวิธีรับมือ:**

- **Test data:** shipment update ต้องมี valid shipment_id — ต้องสร้าง 10,000+ shipment records ใน test DB ก่อน
- **Idempotency:** ถ้า update ส่ง duplicate (เกิดจาก retry ใน test) ระบบต้องรับได้ — ต้องทดสอบ duplicate scenarios
- **Database isolation:** test ไม่ควรรันบน production DB — ต้องมี staging environment ที่ representative
- **Cleanup:** หลัง Endurance Test มี 10,000 × 60 × 8 = 4,800,000 shipment updates ใน test DB — ต้องมี cleanup plan

</details>

---

### Advanced 3: Feynman Task

**ภารกิจ:** อธิบาย performance testing workflow ทั้งหมดที่เรียนมาใน series นี้ ให้ **junior developer คนหนึ่งที่เพิ่ง join ทีมและไม่เคย test performance มาก่อน** โดยทำตาม 4 ขั้นตอน:

**ขั้นตอนที่ 1:** เขียนอธิบาย workflow ทั้งหมดด้วยภาษาง่ายๆ ราวกับสอนคนจริงๆ (เขียนออกมาจริง ไม่ใช่แค่คิด)

**ขั้นตอนที่ 2:** ระบุจุดที่คุณสะดุด — ส่วนไหนที่อธิบายได้ไม่ราบรื่น ต้องหยุดคิด หรือรู้สึกไม่แน่ใจ (ความซื่อสัตย์ต่อตัวเองสำคัญมากในขั้นนี้)

**ขั้นตอนที่ 3:** กลับทบทวนส่วนที่สะดุดใน series — ระบุว่ากลับไปอ่านไฟล์ไหน section ไหน

**ขั้นตอนที่ 4:** Simplify ด้วย Analogy พร้อม Breakdown Point — สร้าง analogy ใหม่ที่อธิบาย performance testing workflow ทั้งหมดได้ในแบบที่ junior developer จะ "เข้าใจและจำได้" แล้วระบุ **1 breakdown point ที่อันตรายที่สุด** ในรูปแบบ: "⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า: [misconception เฉพาะเจาะจง]" — เลือกเพียง 1 ข้อที่คุณคิดว่าจะทำให้ผู้เรียนเข้าใจผิดและทำ mistake ได้มากที่สุด

> ไม่มีเฉลยสำหรับ exercise นี้ — เป้าหมายคือ process ไม่ใช่ผลลัพธ์ ถ้าขั้นตอนที่ 2 บอกว่า "ไม่มีจุดสะดุดเลย" นั่นคือสัญญาณว่าคุณอาจไม่ได้ลองจริงๆ

</details>
