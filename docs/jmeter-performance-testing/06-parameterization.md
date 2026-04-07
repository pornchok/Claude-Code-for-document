# บทที่ 6: Parameterization ด้วย CSV Data Set Config

---

## ⏰ Pre-chapter Retrieval

> แนะนำ: อ่านบทนี้หลังจากผ่านไปอย่างน้อย 1 วันหลังบทที่ 5

**ก่อนอ่านบทนี้ ลองตอบ:**

ใน Test Plan ที่สร้าง HTTP Request Sampler ในบทก่อนหน้า สมมติ Thread Group มี 50 threads และ HTTP Request ใช้ค่า hardcode เช่น `username=testuser` และ `password=pass1234` — จะเกิดอะไรขึ้นกับข้อมูลบน server เมื่อ test รัน? นี่เป็นปัญหาอะไรในการ simulate real users?

เขียนคำตอบก่อน อย่าเพิ่งอ่านต่อ — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น

---

> **เฉลย:** 50 threads จะ login ด้วย username เดิมพร้อมกัน — เกิดปัญหา 2 ระดับ:
>
> 1. **Server side:** ระบบอาจบล็อก session ซ้ำ, trigger rate limiting, หรือ backend logic อาจมี edge case เฉพาะเมื่อ user เดิม login หลาย session — ไม่ใช่ scenario ที่ real users ทำ
>
> 2. **Data contamination:** ถ้า test มี write operations เช่น สั่งซื้อ, อัปเดตข้อมูล — ทุก thread จะ operate บน account เดียวกัน ผลอาจขัดแย้งกัน เช่น thread A เพิ่ง update address แล้ว thread B ก็ update ซ้อน ทำให้ test data corrupted
>
> **Remediation path:** ถ้าตอบได้แค่ว่า "มัน login ซ้ำกัน" แต่ยังไม่เข้าใจว่าทำไมถึงเป็นปัญหา — ลองนึกถึง scenario จริง: ถ้าร้านค้าทดสอบระบบ checkout แต่ทุก transaction ใช้ account เดิม ผล test จะสะท้อน real users ได้ไหม?
>
> **Novice fallback:** เหมือนทดสอบคิวแคชเชียร์โดยให้คนเดิมยืนซ้ำทุกช่อง แทนที่จะเป็นลูกค้า 50 คนที่ต่างกัน — คิวอาจเร็วหรือช้าคนละแบบกับความเป็นจริง

---

## 1. วัตถุประสงค์

เมื่ออ่านบทนี้จบ คุณจะสามารถ:

- **อธิบาย** ว่า Parameterization คืออะไรและทำไมถึงจำเป็นสำหรับ realistic load test
- **สร้าง** CSV file และ config CSV Data Set Config ใน JMeter ได้ถูกต้องครบทุก field
- **ใช้** variable จาก CSV ใน HTTP Request Sampler ด้วย `${variable_name}` syntax
- **เลือก** ระหว่าง CSV Data Set Config กับ User Defined Variables ให้เหมาะกับ use case
- **ระบุ** 3 common mistakes ที่พบบ่อย (path ผิด, Sharing Mode ผิด, Recycle + Stop Thread ขัดกัน)

---

## 2. ทำไมต้องรู้?

สมมติทีมทดสอบ API login ของระบบ e-learning — test ผ่านดี แต่พอตรวจสอบผล server logs พบว่า:
- Account `testuser001` ถูก login พร้อมกัน 100 sessions
- Account ถูก lock โดย security system เพราะตรวจพบ "concurrent login ผิดปกติ"
- Response time พุ่งสูงเพราะ server lock contention บน record เดิม

test บอกว่า "ผ่าน" แต่ไม่ได้วัดสิ่งที่อยากวัดจริงๆ เพราะ real users 100 คน ไม่ได้ใช้ account เดียวกัน

Parameterization แก้ปัญหานี้โดยทำให้แต่ละ thread ใช้ข้อมูลที่แตกต่างกัน — เหมือน users จริงที่ต่างคนต่าง credentials ต่างคนต่าง product ID ต่างคนต่าง search query

---

## 3. Analogy: สคริปต์ละครที่มีนักแสดงต่างกัน

ลองนึกภาพ test plan ของ JMeter เหมือน **บทละคร (script)**

- บทละครบอกว่า "ตัวละครต้องไปที่ counter แล้วพูดชื่อตัวเอง จากนั้นยื่น ID"
- ถ้าทุกคนอ่านบทเดิมแต่มีชื่อและ ID ต่างกัน — นั่นคือ parameterization
- CSV file คือ **cast list** — รายชื่อนักแสดงพร้อม prop แต่ละคน
- CSV Data Set Config คือ **stage manager** ที่คอยบอกว่า "scene นี้ใช้คนไหน"
- `${username}` ใน script คือ **stage direction** ที่บอกให้นักแสดงแต่ละคนพูดชื่อตัวเอง

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:** CSV ทุก row จะถูกใช้อย่างเท่าเทียมกันเสมอ — ผิด ถ้า Sharing Mode = "All threads" และ threads มากกว่า CSV rows JMeter จะ Recycle กลับ row 1 ทำให้ rows แรกถูกใช้บ่อยกว่า rows หลัง และถ้า Recycle = False test จะหยุดกลางคันเมื่อ CSV หมด — behavior นี้เปลี่ยนไปทั้งหมดขึ้นอยู่กับ Sharing Mode และ Recycle setting

---

## 4. เนื้อหาหลัก

### 4.1 CSV Data Set Config: ทำไมต้องมี

ก่อนจะรู้ว่า config อะไร ต้องรู้ว่าปัญหาคืออะไร

**ปัญหาของ hardcoded values:**
- ทุก thread ใช้ข้อมูลเดิม → ไม่ simulate real diversity of users
- ถ้า server ล็อก duplicate session → test พังด้วยเหตุผลผิด (ไม่ใช่ performance แต่เป็น data issue)
- write operations บน record เดิม → test data corrupted

**CSV Data Set Config แก้ปัญหาโดย:**
- อ่าน CSV file ที่มี rows ของข้อมูลต่างๆ
- แจก row ให้ threads ตาม Sharing Mode ที่กำหนด
- แต่ละ thread ได้ข้อมูลต่างกัน → simulate real users ได้จริง

---

### 4.2 CSV Data Set Config: Fields ทีละตัว

เพิ่ม CSV Data Set Config ได้ที่: คลิกขวาที่ Thread Group → Add → Config Element → CSV Data Set Config

**Field 1: Filename**

> "Path to CSV file"
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

- ระบุ path ของ CSV file
- ✅ **Best practice:** เก็บ CSV ไว้ใน directory เดียวกับ `.jmx` file แล้วใช้ชื่อไฟล์เปล่าๆ เช่น `users.csv`
- เหตุผล: relative path ทำให้ test plan portable — ย้ายไปรันบนเครื่องอื่นหรือ CI/CD server ได้โดยไม่ต้องแก้ path

⚠️ **Gotcha สำคัญ — Working Directory ของ JMeter:**

JMeter ตีความ relative path จาก **working directory ขณะรัน** ไม่ใช่จาก directory ที่เก็บ `.jmx` file

- **GUI mode:** working directory = `apache-jmeter-x.x.x/bin/` → `users.csv` หมายถึง `/path/to/jmeter/bin/users.csv`
- **CLI mode (ตัวอย่างที่อาจ fail):** `jmeter -n -t /home/user/tests/test.jmx` → working directory ยังเป็น `bin/` ดังนั้น `users.csv` จะไม่พบ

**วิธีแก้ที่ recommended:**
```
# วิธีที่ 1 (portable): ใช้ path relative ต่อ .jmx file ด้วย built-in property
Filename: ${__P(test.basedir,/default/path)}/users.csv

# วิธีที่ 2 (ง่ายที่สุด): ใช้ absolute path เสมอ
Filename: /home/user/tests/users.csv

# วิธีที่ 3 (สำหรับ CLI): cd ไปที่ folder test plan ก่อน
cd /home/user/tests && jmeter -n -t test.jmx
```

**Field 2: Variable Names**

> "Comma-separated list of variable names"
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

- ชื่อ variable ที่จะ map กับแต่ละ column ใน CSV (คั่นด้วย comma)
- ถ้า CSV row คือ `alice,pass123` และ Variable Names คือ `username,password`
- → `${username}` = `alice`, `${password}` = `pass123`
- ถ้าปล่อยว่าง JMeter จะใช้ row แรกของ CSV เป็น header (ชื่อ column)

**Field 3: Delimiter**

> "Character separating values (default: comma)"
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

- ค่า default: `,` (comma)
- เปลี่ยนได้ถ้า data มี comma เช่น address — ใช้ `|` หรือ `\t` (tab) แทน
- ถ้าใช้ tab ให้พิมพ์ `\t` ในช่อง Delimiter

**Field 4: Allow Quoted Data**

- ถ้า `True` — ค่าที่อยู่ใน double quotes จะถูก parse เป็น 1 field แม้มี comma ข้างใน
- เช่น `"Smith, John",pass123` → field 1 = `Smith, John`, field 2 = `pass123`
- ใช้เมื่อ data มี comma ภายใน field เช่น address หรือ full name

**Field 5: Recycle on EOF**

> "Restart from beginning when file ends"
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

- `True` (default): เมื่ออ่าน CSV ครบทุก row แล้ว กลับไปเริ่มต้นที่ row แรกใหม่
- `False`: เมื่อ CSV หมด — ขึ้นอยู่กับ Stop Thread on EOF
- ใช้ `True` เมื่อ: test รันนานกว่า rows ที่มี (เช่น soak test 8 ชั่วโมงกับ CSV 100 rows)
- ใช้ `False` เมื่อ: ต้องการให้แต่ละ row ถูกใช้ครั้งเดียว เช่น test ที่ data สำคัญและห้ามซ้ำ

**Field 6: Stop Thread on EOF**

> "Terminate thread when file exhausted"
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

- `True`: เมื่อ CSV หมด thread จะหยุดทำงาน
- `False`: thread ไม่หยุด (ใช้ร่วมกับ Recycle on EOF = True หรือ False มีผลต่างกัน — ดู section 6c)
- ใช้ `True` เมื่อ: test design ให้รันตาม data — "ทดสอบ 1000 orders ครบแล้วหยุด"

**Field 7: Sharing Mode**

> "Thread/thread group scope" [controls how file data is distributed among threads]
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

Options:
- **All threads** (แนะนำสำหรับ real-world): ทุก thread ใน test plan ใช้ pointer เดียวกัน — แต่ละ request ได้ row ถัดไปตามลำดับ ไม่มีการซ้ำ
- **Current thread group**: แต่ละ Thread Group มี pointer ของตัวเอง — Thread Group A กับ B อ่าน CSV แยกกัน
- **Current thread**: แต่ละ thread มี pointer ของตัวเอง — thread A เริ่มที่ row 1, thread B เริ่มที่ row 1 เช่นกัน → ทุก thread อ่านข้อมูลเดิม!

✅ **Best practice: ใช้ Sharing Mode = "All threads" สำหรับ real-world test**

เหตุผล: "All threads" ทำให้ทุก request ได้ data ต่างกัน เหมือน users จริงที่ต่างคนต่าง credentials ถ้าใช้ "Current thread" ทุก thread จะวนซ้ำ data ชุดเดิมตั้งแต่ row 1 ซึ่งไม่ simulate real diversity

---

⏸ **self-check (Backward Retrieval):** ก่อนอ่านต่อ — อธิบาย Sharing Mode = "Current thread" vs "All threads" ว่าต่างกันอย่างไร และ scenario ไหนที่ "Current thread" จะให้ผล misleading เขียนคำตอบลงกระดาษก่อน — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น — แล้วค่อยอ่านต่อ

---

> **เฉลย:**
> - **Sharing Mode = "All threads"** — CSV pointer เป็น global ทุก thread ใน test plan แชร์ตัวนับเดียวกัน — thread 1 ได้ row 1, thread 2 ได้ row 2, thread 3 ได้ row 3 — เหมาะสำหรับ simulating unique users ที่ใช้ credentials ต่างกัน
> - **Sharing Mode = "Current thread"** — แต่ละ thread มี CSV pointer ของตัวเอง เริ่มจาก row 1 เสมอ — thread 1 ได้ row 1, thread 2 ก็ได้ row 1 เช่นกัน — เหมาะสำหรับ scenario ที่ thread ต้องทำ full workflow ซ้ำๆ ด้วย data ชุดเดิม
>
> **ถ้าตอบไม่ถูก:** กลับอ่าน Section 4.1 (Sharing Mode) อีกครั้ง โดยเน้น table เปรียบเทียบ

---

### 4.3 ใช้ Variable ใน HTTP Request

หลังตั้งค่า CSV Data Set Config แล้ว ใช้ตัวแปรใน HTTP Request Sampler ด้วย syntax `${variable_name}`

**ตัวอย่าง:**
```
HTTP Request: POST /api/auth/login

Body Data (JSON):
{
  "username": "${username}",
  "password": "${password}"
}
```

`${username}` และ `${password}` จะถูกแทนที่ด้วยค่าจาก CSV ก่อน JMeter ส่ง request

✅ **Best practice:** ใช้ variable ใน Path ได้ด้วย เช่น `/api/products/${product_id}` — ทำให้ test เรียก product ID ต่างๆ แทนที่จะเรียก product เดิมซ้ำ

---

### 4.4 User Defined Variables vs CSV: เมื่อไหรใช้อะไร

JMeter มีอีก 1 วิธีในการ define variables: **User Defined Variables**

> "Define some user-defined variables at the Test Plan level...anyplace that value is found in your recorded samples will be replaced"
> *(source: https://jmeter.apache.org/usermanual/best-practices.html)*

**เมื่อไหรใช้ User Defined Variables:**
- ค่าที่ **คงที่ตลอด test** เช่น base URL, API version, timeout
- ค่าที่ไม่เปลี่ยนตาม thread หรือ iteration
- ตัวอย่าง: `BASE_URL = https://api.example.com`, `API_VERSION = v2`

**เมื่อไหรใช้ CSV Data Set Config:**
- ข้อมูลที่ต้องการให้ **ต่างกันต่อ thread หรือ iteration** เช่น username, product ID, order ID
- ข้อมูลที่มีหลาย rows

**ข้อจำกัดสำคัญของ User Defined Variables:**
> "Configuration elements are processed by a separate thread. Therefore functions such as __threadNum do not work properly in elements such as User Defined Variables."
> *(source: https://jmeter.apache.org/usermanual/functions.html)*

ใช้ User Defined Variables สำหรับค่า static เท่านั้น — ไม่ใช่ dynamic per-thread values

**สรุปตาราง:**

| Use Case | วิธีที่แนะนำ |
|----------|------------|
| Base URL, API key ที่ใช้ทั้ง test | User Defined Variables |
| Username/password ต่างกันต่อ user | CSV Data Set Config |
| Product ID จาก list 1,000 items | CSV Data Set Config |
| Environment (staging/production) | User Defined Variables |
| Random search keywords | CSV Data Set Config หรือ `${__Random()}` |

---

### 4.5 Random Variable Controller และ Built-in Functions

**`${__Random(min,max)}`**
> "The random function returns a random number that lies between the given min and max values."
> *(source: https://jmeter.apache.org/usermanual/functions.html)*

ตัวอย่าง: `${__Random(1,1000)}` → สุ่มตัวเลขระหว่าง 1-1000
ใช้เมื่อ: ต้องการ random ID โดยไม่มี CSV เช่น `/api/products/${__Random(1,500)}`

**`${__CSVRead()}`**
> "In most cases, the newer CSV Data Set Config element is easier to use."
> *(source: https://jmeter.apache.org/usermanual/functions.html)*

แนะนำให้ใช้ CSV Data Set Config แทน `__CSVRead()` เพราะ config-based approach ดูแลง่ายกว่าและมี UI ที่ชัดเจน

---

⏸ **self-check (Bloom's L4 — Analysis):** สมมติ test plan มี Thread Group 50 threads, CSV มี 10 rows, Sharing Mode = "All threads", Recycle on EOF = True — ในแต่ละ iteration thread ที่ 11 จะได้ data จาก row ไหน? ทำไม? เขียนคำตอบลงกระดาษก่อน — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น — แล้วค่อยอ่านต่อ

---

> **เฉลย:** กับ Sharing Mode = "All threads" และ Recycle = True:
> - Thread 1 Loop 1 → Row 1, Thread 2 Loop 1 → Row 2, ..., Thread 10 Loop 1 → Row 10
> - Thread 11 Loop 1 → Row 1 (Recycle กลับต้น), ..., Thread 20 Loop 1 → Row 10
> - Thread 21-30 → Row 1-10 อีกรอบ (recycle) และ Thread 31-50 เช่นกัน
> - **ข้อสังเกต:** ด้วย 50 threads และ 10 rows — แต่ละ row ถูกใช้โดย 5 threads พร้อมกัน → ทดสอบ concurrent access บน account เดียวกัน
> - **ถ้าต้องการ unique user per thread:** ต้องมีอย่างน้อย 50 rows ใน CSV หรือปรับ Sharing Mode
>
> **ถ้าตอบไม่ถูก:** กลับอ่าน Section 4.1 (Sharing Mode + Recycle on EOF) และลอง trace ด้วยกระดาษ — ถ้าระบุจุดที่เข้าใจผิดไม่ได้: อ่าน section 4 ทั้งหมดใหม่

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner: User Credentials สำหรับ Login API

**Scenario:** ทดสอบ `POST /api/auth/login` ด้วย 5 users ที่มี credentials ต่างกัน

**ขั้นตอนที่ 1: สร้าง CSV file**

สร้างไฟล์ `users.csv` ในโฟลเดอร์เดียวกับ `.jmx` file:

```csv
username,password
alice@example.com,AlicePass123
bob@example.com,BobPass456
carol@example.com,CarolPass789
dave@example.com,DavePass012
eve@example.com,EvePass345
```

**ขั้นตอนที่ 2: Config CSV Data Set Config ใน JMeter**

```
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3

CSV Data Set Config:
  Name: User Credentials

  Filename: users.csv
  # ทำไม relative path? เพราะ .jmx และ CSV อยู่โฟลเดอร์เดียวกัน
  # portable — ย้ายไปเครื่องอื่นไม่ต้องแก้ path

  Variable Names: username,password
  # ทำไม ไม่ปล่อยว่าง? เพราะเราไม่ต้องการให้ row แรก (header) เป็น data
  # ถ้าปล่อยว่าง JMeter อ่าน row แรกเป็น variable names → ถูกต้อง แต่ header จะถูก skip
  # ระบุ explicit ดีกว่า ชัดเจนกว่า

  Delimiter: ,
  # ค่า default ใช้ได้เพราะ CSV ใช้ comma ปกติ

  Allow Quoted Data: False
  # ทำไม False? ข้อมูลง่ายๆ ไม่มี comma ใน field — ไม่ต้องการ

  Recycle on EOF: True
  # ทำไม True? ถ้า Thread Loop > 1 ต้องวน CSV ซ้ำ
  # 5 rows สำหรับ 5 threads — ถ้า loop 3 รอบ จะได้ 15 requests ต้องวนกลับ

  Stop Thread on EOF: False
  # ทำไม False? เราตั้ง Recycle = True อยู่แล้ว EOF จะไม่เกิด

  Sharing Mode: All threads
  # ทำไม All threads? ต้องการให้แต่ละ thread ได้ user ต่างกัน
  # thread 1 = alice, thread 2 = bob, ... — สะท้อน real users
```

**ขั้นตอนที่ 3: ใช้ variable ใน HTTP Request**

```
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3

HTTP Request Sampler:
  Method: POST
  Path: /api/auth/login

  Body Data:
  {
    "email": "${username}",
    "password": "${password}"
  }

  Headers (ผ่าน HTTP Header Manager):
  Content-Type: application/json
```

---

### Intermediate: Parameterize Product ID สำหรับ Inventory Management System

**Scenario:** ทีม QA ต้องทดสอบ `GET /api/inventory/items/${item_id}` ของระบบ warehouse management ด้วย product IDs จาก catalog จริง 100 items

**สร้าง CSV:**
```csv
item_id,item_name,expected_category
WH-001,Hydraulic Pump,machinery
WH-002,Safety Helmet,ppe
WH-003,Forklift Battery,power
...
# (ครบ 100 rows)
```

**Config สำหรับ Load Test:**
```
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3

Thread Group:
  Threads: 50
  Ramp-Up: 50 seconds
  Loop Count: Infinite
  Scheduler: Duration = 300 seconds (5 นาที)

CSV Data Set Config:
  Filename: warehouse_items.csv
  Variable Names: item_id,item_name,expected_category
  Delimiter: ,
  Recycle on EOF: True      # 50 threads × หลาย loop > 100 rows → ต้องวน
  Stop Thread on EOF: False
  Sharing Mode: All threads  # thread ต่างๆ ได้ item_id ต่างกัน

Uniform Random Timer:
  Random delay maximum: 1500
  Constant delay offset: 500   # delay 500-2000ms เลียนแบบ warehouse staff
                                # ที่ scan barcode แล้วรอดู result

HTTP Request Sampler:
  Method: GET
  Path: /api/inventory/items/${item_id}

Response Assertion (เพิ่มเพื่อ validate):
  Field to test: Response Body
  Contains: "${expected_category}"
  # ตรวจสอบว่า category ใน response ตรงกับที่คาดไว้
```

**ทำไม `expected_category` ถึงอยู่ใน CSV?**
เพราะ test ที่ดีต้องตรวจสอบ correctness ไม่ใช่แค่ latency — การเก็บ expected value ใน CSV ทำให้ assertion ยืดหยุ่น ไม่ต้อง hardcode ใน script

---

### Advanced: JWT Token Refresh Logic ใน Test Plan

**Scenario:** API ต้องการ JWT token ที่ expire ทุก 15 นาที — test ที่รัน 1 ชั่วโมงต้องจัดการ token refresh ระหว่างทาง

**ปัญหา:** CSV Data Set Config ใส่ static token ไม่ได้สำหรับกรณีนี้ เพราะ token expire

**แนวทาง: Login และ Extract Token เป็น Flow ใน Test Plan**

```
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3

Test Plan Structure:
├── Thread Group: API Load Test
│   ├── CSV Data Set Config
│   │   Filename: users.csv
│   │   Variable Names: username,password
│   │   Sharing Mode: All threads
│   │
│   ├── [Once Only Controller]   ← รันครั้งเดียวตอนเริ่ม thread
│   │   └── HTTP Request: POST /api/auth/login
│   │       Body: {"username": "${username}", "password": "${password}"}
│   │
│   │       + JSON Extractor (เพิ่มใต้ HTTP Request นี้)
│   │         Variable Name: access_token
│   │         JSON Path: $.data.access_token
│   │
│   │         + JSON Extractor (อีกตัว)
│   │           Variable Name: token_expires_at
│   │           JSON Path: $.data.expires_at
│   │
│   ├── [If Controller]   ← ตรวจสอบว่า token ใกล้ expire ไหม
│   │   Condition: ${__jexl3(System.currentTimeMillis() > Long.parseLong("${token_expires_at}") - 60000)}
│   │   # ถ้า current time > (expire_time - 1 นาที) → เข้า block นี้เพื่อ refresh
│   │   └── HTTP Request: POST /api/auth/refresh
│   │       Body: {"refresh_token": "${refresh_token}"}
│   │
│   │       + JSON Extractor
│   │         Variable Name: access_token  ← update ค่า token ใหม่
│   │
│   └── HTTP Request: GET /api/protected/data
│       Headers:
│         Authorization: Bearer ${access_token}
│         # ใช้ token ที่ได้จาก login หรือ refresh ล่าสุด
```

**Trade-off Discussion:**

| แนวทาง | ข้อดี | ข้อเสีย |
|--------|-------|---------|
| Once Only Controller + token extraction | ทำ login จริงเหมือน user | เพิ่ม complexity ใน test plan |
| Static token ใน CSV (ไม่ expire หรือ expire ยาวมาก) | ง่ายมาก ไม่ต้องทำ refresh logic | ไม่ realistic กับ security policy จริง |
| Pre-generate tokens ก่อน test (script ภายนอก) | แยก concerns ชัดเจน | ต้อง maintain 2 ส่วน (token generator + test plan) |

**เมื่อไหรใช้แนวทางไหน:**
- Test สั้น (< 10 นาที) และ token expire ยาว → pre-generate ใน CSV ก็พอ
- Test ยาว (> token expiry) หรือ test security flow → ใช้ Once Only + If Controller
- Production-grade test ที่ต้องการ full realistic simulation → Once Only + refresh logic

---

## 6. Common Mistakes

### ❌ (a) Path ของ CSV ผิด (Absolute vs Relative)

**แบบผิด:**
```
Filename: C:\Users\john\tests\users.csv
# ใช้ได้บนเครื่อง john เท่านั้น
# พอรันบน CI/CD server หรือเครื่อง team member อื่น → FileNotFoundException
```

🔍 **สัญญาณ:** error message ว่า `FileNotFoundException` หรือ `No such file or directory` เมื่อรันบนเครื่องอื่น
🤔 **ถามตัวเอง:** "ถ้าฉันส่ง .jmx file นี้ให้ teammate รัน เขาจะต้องแก้ Filename ไหม?"

**แบบถูก:**
```
Filename: users.csv
# วาง users.csv ในโฟลเดอร์เดียวกับ .jmx file
# ทำงานได้ทุกเครื่อง ทุก OS
```

*(source: https://jmeter.apache.org/usermanual/component_reference.html — Filename: "Path to CSV file")*

---

### ❌ (b) Sharing Mode ผิดทำให้ Threads ใช้ Data ซ้ำ

**แบบผิด:**
```
Sharing Mode: Current thread
# ทุก thread เริ่มอ่าน CSV จาก row 1
# thread 1, 2, 3 ... ทุกตัวได้ username เดิม (alice) ในทุก iteration แรก
```

🔍 **สัญญาณ:** ดู server access log แล้วเห็น username เดิมปรากฏซ้ำๆ มาจาก IP ต่างๆ แทนที่จะเห็น username หลากหลาย
🤔 **ถามตัวเอง:** "ถ้า test ของฉันมี 50 threads และ CSV มี 50 rows — ทุก thread ควรได้ username ต่างกันไหม? Sharing Mode ที่ใช้อยู่ทำแบบนั้นได้ไหม?"

**แบบถูก:**
```
Sharing Mode: All threads
# thread ทั้งหมดแชร์ pointer เดียว
# thread 1 ได้ row 1, thread 2 ได้ row 2, thread 3 ได้ row 3 ...
```

*(source: https://jmeter.apache.org/usermanual/component_reference.html — Sharing mode: "Thread/thread group scope")*

---

### ❌ (c) Recycle on EOF = False + Stop Thread on EOF = False ทำให้ Variable ว่าง

**แบบผิด:**
```
Recycle on EOF: False
Stop Thread on EOF: False
# เมื่อ CSV หมด JMeter จะพยายามอ่าน row ต่อไปแต่ไม่มี
# ค่า variable จะกลายเป็น "<EOF>" หรือ string ว่าง
# HTTP Request จะส่ง username="" หรือ username="<EOF>"
```

🔍 **สัญญาณ:** request บางตัวได้ response "invalid username" หรือ 400 Bad Request โดยที่ test data ดูเหมือนถูกต้อง — แต่จริงๆ CSV หมดแล้ว
🤔 **ถามตัวเอง:** "CSV ของฉันมีกี่ rows? threads รัน iterations กี่รอบ? rows พอไหม?"

**แบบถูก (เลือก 1 ใน 3):**
```
# Option A: ต้องการวนซ้ำ data
Recycle on EOF: True
Stop Thread on EOF: False

# Option B: ต้องการหยุดเมื่อ data หมด
Recycle on EOF: False
Stop Thread on EOF: True

# Option C: สร้าง CSV ให้มี rows มากพอกว่า threads × loops
# ไม่ต้องกังวลเรื่อง EOF เลย
```

*(source: https://jmeter.apache.org/usermanual/component_reference.html — Recycle on EOF: "Restart from beginning when file ends" / Stop Thread on EOF: "Terminate thread when file exhausted")*

---

## 7. สรุปบท

### คำถาม Retrieval — ตอบก่อนดูเฉลย

หยุดอ่าน เขียนคำตอบลงกระดาษก่อน อย่างน้อย 30 วินาทีต่อข้อ — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น ก่อนดูเฉลย

**คำถาม 1:** อธิบายด้วยคำพูดตัวเองว่า Sharing Mode = "All threads" ต่างจาก "Current thread" อย่างไร — และในการทำ login test ที่มี 100 threads กับ CSV 100 rows คุณจะใช้ mode ไหนและทำไม?

**คำถาม 2:** ถ้า CSV มี 50 rows, threads = 10, Loop Count = 10, Recycle on EOF = True, Sharing Mode = All threads — แต่ละ thread จะใช้ data จาก CSV ตาม pattern ไหน? มีโอกาสที่ rows บางอันจะถูกใช้บ่อยกว่าอื่นไหม?

**คำถาม 3 (code-based):** ดู config ด้านล่างนี้แล้วบอกว่ามีปัญหาอะไร:
```
Test Plan สำหรับ E-commerce Order API:
  CSV Data Set Config:
    Filename: C:\jmeter-tests\orders\product_ids.csv
    Variable Names: (ปล่อยว่าง)
    Recycle on EOF: False
    Stop Thread on EOF: False
    Sharing Mode: Current thread

  HTTP Request:
    GET /api/products/${product_id}
    # แต่ CSV header row คือ "product_id,product_name"
```

---

**เฉลย คำถาม 1:**
"All threads" — ทุก thread แชร์ pointer เดียว thread หนึ่งอ่าน row แล้ว pointer เลื่อน thread ถัดไปได้ row ถัดไป → ทุก thread ได้ข้อมูลต่างกัน
"Current thread" — แต่ละ thread มี pointer ของตัวเอง เริ่มที่ row 1 เหมือนกันทุก thread → ทุก thread ได้ข้อมูลเดิม

สำหรับ login test 100 threads: ใช้ "All threads" — ต้องการให้แต่ละ thread ใช้ account ต่างกัน simulate 100 unique users

**เฉลย คำถาม 2:**
100 iterations ทั้งหมด (10 threads × 10 loops) ด้วย CSV 50 rows และ Recycle = True — แต่ละ row จะถูกใช้เฉลี่ย 2 ครั้ง (100/50) แต่การแจกขึ้นอยู่กับ timing และ thread interleaving — rows ช่วงต้นอาจถูกใช้บ่อยกว่าเล็กน้อยถ้า threads เริ่มต้นพร้อมกัน ในทางปฏิบัติถือว่ากระจายได้ดีพอ

**เฉลย คำถาม 3 (ปัญหาใน config):**
ปัญหา 4 จุด:
1. **Path absolute:** `C:\jmeter-tests\...` จะพังบน OS อื่นหรือเครื่องอื่น → แก้เป็น relative path `product_ids.csv`
2. **Variable Names ว่าง:** JMeter จะอ่าน row แรก (`product_id,product_name`) เป็น variable names — ในกรณีนี้ถูกต้อง แต่ควรระบุ explicit เพื่อความชัดเจน
3. **Recycle = False + Stop Thread = False:** เมื่อ CSV หมด `${product_id}` จะเป็น `<EOF>` → request ส่ง `/api/products/<EOF>` → 404 ทุกตัว → แก้เป็น Recycle = True หรือ Stop Thread = True
4. **Sharing Mode = Current thread:** ทุก thread เริ่มที่ `product_id` ตัวแรก → ทุก thread request product เดิม → ไม่ simulate diversity → แก้เป็น All threads

---

**Generation Effect — ลองสร้างเอง:**

หยุดสักครู่ก่อนปิดบทนี้ เลือก 1 ใน 2:

**A)** คิด scenario จากงาน/โปรเจคของตัวเอง ว่าถ้าต้องทดสอบ API ของคุณ ข้อมูลอะไรที่ควรอยู่ใน CSV? แต่ละ column คืออะไร? จะตั้ง Sharing Mode และ Recycle on EOF เป็นอะไร?

**B)** เขียน CSV file 5 rows สำหรับ scenario ที่ต่างจากตัวอย่างในบทนี้ทั้งหมด แล้วระบุ CSV Data Set Config settings ที่จะใช้ พร้อมเหตุผลแต่ละ field

การสร้าง CSV ด้วยตัวเองทำให้เชื่อม concept กับ use case จริงได้ — ดีกว่าการอ่านซ้ำ 3 รอบ

---

**Remediation Path:**
- ถ้าตอบคำถาม 1 ไม่ได้ → อ่าน section 4.2 Field 7 (Sharing Mode) ใหม่
- ถ้าตอบคำถาม 2 ผิด → ลองวาด diagram บนกระดาษ: 10 threads, pointer เดียว, 50 rows — ตาม pointer ว่าจะเลื่อนอย่างไร
- ถ้าตอบคำถาม 3 ได้แค่ 1-2 ข้อ → อ่าน section 6 (Common Mistakes) ทั้ง 3 ข้ออีกครั้ง

---

*บทที่ 7: Assertions — ตรวจสอบว่า Response ถูกต้อง →*
