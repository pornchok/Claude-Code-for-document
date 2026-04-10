# บทที่ 7: Assertions — ตรวจสอบว่า Response ถูกต้อง

> เวลาโดยประมาณ: 30–45 นาที

---

## ⏰ Pre-chapter Retrieval

> แนะนำ: อ่านบทนี้หลังจากผ่านไปอย่างน้อย 1 วันหลังบทที่ 6

**ก่อนอ่านบทนี้ ลองตอบ:**

ในบทที่ 6 คุณเรียนรู้การใช้ CSV Data Set Config เพื่อ parameterize ข้อมูล เช่น user ID หรือ search term ต่างกันทุก iteration แล้ว HTTP Request ก็ส่ง request พร้อม parameter เหล่านั้นไปยัง server

**คำถาม:** สมมติ test plan ของคุณมี 100 threads วนลูป 10 ครั้ง โดยแต่ละ iteration ใช้ `userId` ต่างกันจาก CSV ไปเรียก `GET /users/${userId}` — ถ้า server return HTTP 200 ทุก request แต่ response body บาง request เป็น `{"error": "User not found"}` สิ่งที่เห็นใน report จะเป็นอย่างไร? JMeter จะนับ request เหล่านั้นว่าสำเร็จหรือล้มเหลว?

เขียนคำตอบลงกระดาษก่อน — อย่า scroll ข้ามไปดูเฉลย และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น

---

> **เฉลย:** JMeter จะนับว่า **สำเร็จทั้งหมด** (0% error rate) เพราะ JMeter ตัดสินผลจาก HTTP status code เป็นหลัก — ถ้า server return 200 JMeter ถือว่า pass แม้ body จะมี error message ก็ตาม
>
> นั่นคือเหตุผลหลักที่เราต้องมี **Assertions** — เพื่อบอก JMeter ว่า "ไม่ใช่แค่ 200 นะ ต้องตรวจ body ด้วย"
>
> **Generation effect:** ก่อนอ่านต่อ ลองเขียนด้วยคำของตัวเองว่า "Assertion คืออะไร" ในหนึ่งประโยค — เขียนก่อนที่จะรู้คำตอบจากบทนี้ การเดาล่วงหน้าช่วยให้จำได้นานกว่าการอ่านแบบ passive
>
> **Remediation path:** ถ้าตอบว่า "JMeter จะนับเป็น failure เพราะ body มี error" — อ่านส่วน 4.1 แรก แล้วกลับมาตอบคำถามนี้ใหม่ก่อนอ่านต่อ

---

## 1. วัตถุประสงค์

เมื่ออ่านบทนี้จบ คุณจะสามารถ:

- **อธิบาย** ว่าทำไมการตรวจแค่ HTTP status code ถึงไม่เพียงพอ และ Assertion แก้ปัญหานี้อย่างไร
- **ตั้งค่า** Response Assertion ได้ครบ 4 fields: Apply To, Field to Test, Pattern Matching Rules, Patterns to Test
- **ใช้** JSON Assertion เพื่อตรวจ structure และ value ใน response body ที่เป็น JSON
- **ใช้** Duration Assertion เพื่อกำหนด threshold response time ที่ยอมรับได้
- **เลือก** Pattern Matching Rule ที่ถูกต้อง (Contains vs Equals vs Substring) ตาม use case
- **รู้จัก** JSR223 Assertion (Groovy) ว่าเมื่อไหรควรพิจารณาและ trade-off คืออะไร

---

## 2. ทำไมต้องรู้?

สมมติคุณ deploy feature ใหม่และรัน load test — report บอก error rate 0%, response time ดี ทีมดีใจมาก แต่วันถัดมา customer report ว่า API คืนข้อมูลผิด user มีการปนกันของข้อมูล

เกิดอะไรขึ้น? API return HTTP 200 ทุก request แต่ logic ข้างในผิด — ดึงข้อมูลผิด record บ้าง return empty object บ้าง

ถ้ามี Assertion ตรวจ response body load test จะ detect ปัญหานี้ตั้งแต่ตอน test ไม่ใช่ตอนที่ customer เจอ

JMeter official docs นิยาม Assertions ไว้ว่า:
> *"Assertions allow you to assert facts about responses received from the server being tested."*
> *(source: https://jmeter.apache.org/usermanual/test_plan.html)*

---

## 3. Analogy: ระบบตรวจสอบในสายการผลิต

นึกภาพสายการผลิตชิ้นส่วนรถยนต์ ทุกชิ้นที่ผ่านสายพานต้องผ่านด่านตรวจ 3 ด่าน:

**ด่านที่ 1 — ตรวจรหัส (Response Assertion):** สแกน barcode ว่าตรงกับ part number ที่ order หรือเปล่า — ถ้าได้ชิ้นส่วนที่มี part number ถูกต้องจึงผ่าน

**ด่านที่ 2 — ตรวจโครงสร้างและขนาด (JSON Assertion):** ชิ้นส่วนต้องมีรูน็อต 4 รู ขนาด M6 — ไม่ใช่แค่ "มีรู" แต่ต้องถูก spec ด้วย

**ด่านที่ 3 — ตรวจความเร็วสายพาน (Duration Assertion):** ถ้าตรวจชิ้นส่วน 1 ชิ้นใช้เวลาเกิน 5 วินาที แสดงว่ากระบวนการมีปัญหา — ต้อง flag ทันที

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:** assertion pass แปลว่า response ถูกต้องสมบูรณ์ — ผิด assertion ตรวจเฉพาะสิ่งที่คุณตั้งค่าไว้เท่านั้น ถ้าตั้งผิด (เช่น ตรวจแค่ status code 200 แต่ไม่ตรวจ body) จะเกิด **false positive** — assertion pass ทั้งที่ response มี error ซ่อนอยู่ใน body เช่น `{"status": 200, "error": "payment_failed"}` จะผ่าน assertion ถ้าคุณตรวจแค่ HTTP 200

---

## 4. เนื้อหาหลัก

### 4.1 Response Assertion: ทำไมถึงต้องใช้?

HTTP status code บอกแค่ว่า "server ตอบกลับมาแล้ว" แต่ไม่ได้บอกว่า "ตอบในสิ่งที่ถูกต้อง"

ตัวอย่างที่พบบ่อย: API บาง design return HTTP 200 พร้อม body เป็น `{"success": false, "error": "Unauthorized"}` — ถ้าไม่มี Response Assertion JMeter จะนับ request นี้ว่า **สำเร็จ** และไม่นับใน error rate เลย

**วิธีเพิ่ม Response Assertion:**
คลิกขวาที่ HTTP Request Sampler → Add → Assertions → Response Assertion

**4 fields ที่ต้องตั้งค่า:**

**Apply To** — กำหนด scope ของ assertion

> *"Target scope (main sample, sub-samples, etc.)"*
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

ค่าที่ใช้บ่อย: `Main sample only` — ตรวจ response ของ request หลัก ไม่รวม sub-resources เช่น images, CSS

**Field to Test** — เลือกว่าจะตรวจอะไร:
- `Response Code` — ตรวจ HTTP status code (200, 404, 500)
- `Response Body` — ตรวจ content ใน body
- `Response Message` — ตรวจข้อความ เช่น "OK", "Not Found"
- `Response Headers` — ตรวจ header เช่น `Content-Type`

**Pattern Matching Rules (Test Type)** — กำหนดวิธีเปรียบเทียบ:

> *Options: "Contains, Equals, Matches, Substring"*
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

| Rule | ความหมาย | เมื่อไหรควรใช้ |
|------|-----------|----------------|
| `Contains` | Pattern เป็น regex ที่ต้องมีอยู่ใน response | ตรวจว่า body มี key word บางคำ |
| `Equals` | Response ทั้งหมดต้องตรงกับ pattern (regex) | ตรวจ status code หรือ response สั้นๆ ที่แน่นอน |
| `Substring` | Pattern เป็น plain text ที่ต้องมีอยู่ | ค้นหาข้อความธรรมดาโดยไม่ใช้ regex |
| `Matches` | Response ทั้งหมดต้อง match regex | ใช้น้อย เหมาะกับ format validation |

**ตัวอย่างเปรียบเทียบ Contains vs Matches:**

| Pattern | Contains | Matches |
|---------|----------|---------|
| `success` | ✅ match ถ้า response มีคำว่า "success" **ที่ใดก็ได้** | ✅ match ถ้า response **ทั้งหมด** เป็นแค่ "success" เท่านั้น |
| `^success$` | ✅ match ถ้า response มี substring ที่ match regex `^success$` | ✅ match ถ้า response ทั้งหมด match `^success$` |

**กฎง่ายๆ:** Contains = "มีอยู่ที่ไหนก็ได้ใน response" | Matches = "response ทั้งหมดต้อง match"

**Patterns to Test** — ค่าที่ต้องการตรวจ เช่น `200` สำหรับ status code, `"success":true` สำหรับ body

✅ **Best practice:** ใส่ assertion **ทุก request ที่ critical** — ถ้าไม่มี assertion HTTP 200 ที่ return error message ใน body จะไม่ถูกนับเป็น failure ทำให้ report แสดงผลดีเกินจริง

✅ **Best practice:** ใช้ Response Assertion + JSON Assertion ร่วมกัน ตรวจทั้ง status code และ body content

⚠️ **ควรเลี่ยง: ใช้แค่ status code assertion โดยไม่ตรวจ body** — เพราะ API บางตัว return 200 แม้มี error ตัวอย่างเช่น GraphQL API มักส่ง HTTP 200 เสมอไม่ว่า query จะ success หรือ fail โดยส่ง error ไว้ใน body แทน

---

### 4.2 JSON Assertion: ตรวจสอบ JSON Response

เมื่อ API return JSON response ต้องการตรวจว่า:
1. JSON มี field ที่ต้องการอยู่ไหม (structure check)
2. Field นั้นมีค่าที่ถูกต้องไหม (value check)

**วิธีเพิ่ม:** คลิกขวาที่ HTTP Request Sampler → Add → Assertions → JSON Assertion

**Fields ที่สำคัญ:**
- **Assert JSON Path exists** — JSON Path expression เช่น `$.data.id` หรือ `$.users[0].name`
- **Additionally assert value** — เปิดเพื่อตรวจค่าด้วย (ไม่ใช่แค่ตรวจว่า field มีอยู่)
- **Expected value** — ค่าที่คาดหวัง
- **Match as regular expression** — ถ้าต้องการใช้ regex กับค่าที่ตรวจ

**ตัวอย่าง JSON Path syntax ที่พบบ่อย:**

```
$.id              → field "id" ที่ root level
$.user.name       → field "name" ใน nested object "user"
$.users[0].email  → email ของ element แรกใน array "users"
$.items.length()  → จำนวน elements ใน array (extension บางตัวรองรับ)
```

⏸ **หยุดคิด (backward retrieval):** ย้อนกลับไปนึกถึงบทที่ 6 — ถ้า CSV มี column `expected_status` เก็บ expected HTTP status code ต่างกันต่อแต่ละ row จะนำมาใช้ใน Response Assertion ได้อย่างไร? เขียนคำตอบลงกระดาษก่อน — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น — แล้วค่อยอ่านต่อ

> **เฉลย:** ใช้ variable จาก CSV ใน Patterns to Test — ตั้งค่าเป็น `${expected_status}` JMeter จะ substitute ค่าจาก CSV ให้อัตโนมัติก่อน assertion ตรวจ ทำให้ test แต่ละ row สามารถมี expected outcome ต่างกันได้

---

### 4.3 Duration Assertion: ตรวจสอบ Response Time

Duration Assertion ทำให้ JMeter นับ request ว่า fail ถ้า response ใช้เวลานานเกิน threshold ที่กำหนด

> *"Checks whether sampler responses complete within a specified time duration threshold."*
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

**วิธีเพิ่ม:** คลิกขวาที่ HTTP Request Sampler → Add → Assertions → Duration Assertion

**Field หลัก:**

> *"Number of milliseconds to wait — responses exceeding this duration fail the assertion"*
> *(source: https://jmeter.apache.org/usermanual/component_reference.html)*

หน่วยเป็น **milliseconds** เสมอ — ถ้า SLA กำหนดว่า response ต้องได้ภายใน 2 วินาที ใส่ `2000`

**ใช้ร่วมกับ Aggregate Report:** Duration Assertion บันทึก failure ใน error rate แต่ตัวเลข response time ใน report แสดงเวลาจริง — ทำให้เห็นทั้ง "มี request ที่ช้าเกิน threshold" และ "ช้าแค่ไหน" ได้พร้อมกัน

---

### 4.4 Size Assertion: ตรวจสอบ Response Size

Size Assertion ตรวจขนาดของ response — มีประโยชน์เมื่อต้องการยืนยันว่า response ไม่ empty หรือขนาดอยู่ในช่วงที่คาดหวัง

**วิธีเพิ่ม:** คลิกขวาที่ HTTP Request Sampler → Add → Assertions → Size Assertion

**Fields:**
- **Apply To** — เลือก scope (Full Response / Response Body / Response Headers)
- **Size in bytes** — ขนาดที่ใช้เปรียบเทียบ
- **Type of Comparison** — `=`, `≠`, `>`, `<`, `≥`, `≤`

**Use case ทั่วไป:**
- ตรวจว่า response ไม่ empty: ตั้ง `> 0`
- ตรวจว่า paginated response มี data ครบ: ตั้ง `> 1000` (bytes ขั้นต่ำที่คาดหวัง)

⏸ **หยุดคิด (Bloom's L4 — Analysis):** ระบบ e-commerce มี endpoint `GET /cart/{cartId}` ที่ return รายการสินค้าในตะกร้า ถ้าต้องการ assert ว่า "ตะกร้าไม่ว่างเปล่า" คุณจะใช้ Assertion แบบไหน — Size Assertion หรือ JSON Assertion หรือใช้ทั้งคู่? และเพราะอะไร? คิดถึง trade-off ก่อนอ่านต่อ

> **เฉลย:** ใช้ทั้งคู่จะดีที่สุด — JSON Assertion ตรวจ `$.items.length() > 0` หรือ `$.items[0]` มีอยู่จริง (ตรวจที่ application logic level) ส่วน Size Assertion เป็น fast-fail check ระดับล่าง ถ้า response เล็กผิดปกติ (เช่น empty `{}` หรือ error message) Size Assertion จะจับได้ก่อน — ใช้ร่วมกันทำให้ detect error ได้ทั้งสองระดับ

---

## 5. ตัวอย่าง 3 ระดับ

### ระดับ Beginner: Response Assertion ตรวจ Status Code สำหรับ GET /posts

สถานการณ์: ทดสอบ public API `https://jsonplaceholder.typicode.com/posts/1`

**ขั้นตอน:**

1. เพิ่ม HTTP Request Sampler:
   - Server Name: `jsonplaceholder.typicode.com`
   - Method: `GET`
   - Path: `/posts/1`

2. คลิกขวาที่ HTTP Request → Add → Assertions → Response Assertion

3. ตั้งค่า Response Assertion:

```
Apply To:          Main sample only
                   # ตรวจเฉพาะ request หลัก ไม่รวม embedded resources

Field to Test:     Response Code
                   # ตรวจ HTTP status code ไม่ใช่ body
                   # เหตุผล: เราต้องการตรวจว่า server ตอบกลับด้วย 200 ก่อน
                   # ถ้าตรวจ Response Body โดยตรงแต่ server return 404
                   # body จะว่างและ assertion อาจ fail ด้วยข้อความสับสน

Pattern Matching Rules: Equals
                   # ใช้ Equals ไม่ใช่ Contains เพราะ status code เป็น
                   # ตัวเลขที่แน่นอน "200" ต้อง match ทั้งหมด
                   # ถ้าใช้ Contains กับ "2" จะ match ทั้ง 200, 201, 204 ด้วย

Patterns to Test:  200
                   # ค่า expected status code
                   # ไม่ต้องใส่ quotes — JMeter ใช้ plain text
```

4. เพิ่ม Response Assertion อีกตัวสำหรับตรวจ body:

```
Apply To:          Main sample only

Field to Test:     Response Body
                   # ตรวจใน body หลังจาก status code ผ่านแล้ว

Pattern Matching Rules: Substring
                   # ใช้ Substring แทน Contains เพราะ:
                   # - Substring: plain text search (เร็วกว่า, ง่ายกว่า)
                   # - Contains: treat pattern เป็น regex (กรณีนี้ไม่จำเป็น)

Patterns to Test:  "userId"
                   # ตรวจว่า body มี field "userId" อยู่
                   # บ่งบอกว่า response เป็น post object จริง ไม่ใช่ error
```

**ผลที่คาดหวังเมื่อ test ผ่าน:** View Results Tree จะแสดง สีเขียว (pass) ทั้งคู่

**ผลเมื่อ test fail:** assertion ที่ fail จะแสดงใน "Failure Message" เช่น:
```
Test failed: code expected to equal /200/
  *** Found: 404
```

---

### ระดับ Intermediate: JSON Assertion + Duration Assertion สำหรับ Payment API

สถานการณ์: ทดสอบ payment processing API ของระบบ financial — SLA กำหนดว่า payment verification ต้องตอบภายใน 3 วินาที และ response ต้องมี `transactionId` ที่ไม่ว่าง

**สมมติ response จาก payment API:**
```json
{
  "status": "success",
  "transactionId": "TXN-20260317-001234",
  "amount": 1500.00,
  "currency": "THB",
  "timestamp": "2026-03-17T10:30:00Z"
}
```

**ตั้งค่า JSON Assertion:**

```
Assert JSON Path exists:    $.transactionId
                            # ตรวจว่า field transactionId มีอยู่ใน response

Additionally assert value:  ✓ (เปิด checkbox)
                            # เปิดเพื่อตรวจค่าด้วย ไม่ใช่แค่ตรวจว่า field มีอยู่

Expected value:             TXN-\d{8}-\d{6}
                            # regex pattern: ขึ้นต้นด้วย TXN- ตามด้วยวันที่ 8 หลัก
                            # ขีด และ รหัส 6 หลัก

Match as regular expression: ✓ (เปิด)
                            # เพราะ transactionId format คงที่แต่ค่าเปลี่ยนทุก transaction
```

**ตั้งค่า JSON Assertion สำหรับ status:**

```
Assert JSON Path exists:    $.status
Additionally assert value:  ✓
Expected value:             success
Match as regular expression: ✗ (ปิด — ตรวจ exact match)
```

**ตั้งค่า Duration Assertion:**

```
Duration (ms): 3000
               # SLA กำหนด 3 วินาที = 3000ms
               # ถ้า response ช้ากว่า 3000ms จะถูกนับเป็น failure
               # แม้ body จะถูกต้อง — เพราะ payment ที่ช้าเกิน SLA ถือว่า fail จาก business perspective
```

**เหตุผลที่ใช้ทั้ง JSON Assertion และ Duration Assertion ร่วมกัน:**

Payment API ที่ดีต้องผ่าน 2 criteria พร้อมกัน:
1. **Correctness** — ให้ transactionId ที่ valid กลับมา
2. **Performance** — ตอบภายใน SLA

ถ้า fail ที่ JSON Assertion = logic ผิด (ไม่ควร go-live)
ถ้า fail ที่ Duration Assertion เท่านั้น = ต้องการ optimization ก่อน scale

---

### ระดับ Advanced: JSR223 Assertion สำหรับ Complex Validation

สำหรับ validation ที่ซับซ้อนเกินกว่า built-in assertions ทำได้ เช่น ต้องตรวจทุก item ในรายการว่า `quantity >= 0` พร้อมกัน — JMeter มี **JSR223 Assertion** ที่รัน Groovy script ได้

**เมื่อไหรควรใช้:** เฉพาะเมื่อ built-in assertions (Response/JSON/Duration) ทำไม่ได้จริงๆ เพราะ JSR223 เพิ่ม CPU overhead ต่อ request และต้องการ Groovy knowledge ในการดูแล

> *"Since JMeter 3.1, we advise switching from BeanShell to JSR223 Test Elements"*
> *(source: https://jmeter.apache.org/usermanual/best-practices.html)*

| ข้อดี | ข้อเสีย |
|-------|---------|
| Validate ได้ทุก logic ที่ซับซ้อน | เพิ่ม CPU overhead ต่อ request |
| Reuse Groovy library ได้ | Script ต้องมี test ของตัวเอง — ถ้า script bug จะ false positive |
| — | Maintenance สูงกว่า built-in assertions มาก |

**แนวทางปฏิบัติ:** ลอง Response Assertion + JSON Assertion ก่อนเสมอ ใช้ JSR223 เป็นทางเลือกสุดท้าย ถ้าต้องการรายละเอียดเพิ่มเติม ดูที่ JMeter official docs: [Best Practices — Scripting](https://jmeter.apache.org/usermanual/best-practices.html)

---

## 6. Common Mistakes

### ❌ (a) ลืมใส่ Assertion ทำให้ Failure ไม่ถูกนับ

**แบบผิด:**
```
Thread Group
└── HTTP Request: POST /login
    # ไม่มี assertion เลย
```

**แบบถูก:**
```
Thread Group
└── HTTP Request: POST /login
    ├── Response Assertion (status code = 200)
    └── JSON Assertion ($.token มีอยู่และไม่ว่าง)
```

🔍 **สิ่งที่เกิดขึ้น:** ถ้า POST /login return 200 แต่ body เป็น `{"error": "Invalid credentials"}` JMeter จะ report 0% error rate — ทำให้ทีมเชื่อว่า login ทำงานปกติทั้งที่จริงๆ ล้มเหลวทุก request

🤔 **เหตุผล:** JMeter default พิจารณา success จาก HTTP status code เท่านั้น ไม่ได้ parse หรือ validate body เอง

*(source: https://jmeter.apache.org/usermanual/test_plan.html — "Assertions allow you to assert facts about responses received")*

---

### ❌ (b) ใช้ Contains แทน Substring ทำให้เกิด False Positive

**แบบผิด:**
```
Pattern Matching Rules: Contains
Patterns to Test:       (active)
```

**แบบถูก:**
```
Pattern Matching Rules: Substring
Patterns to Test:       active
```

หรือถ้าต้องการใช้ regex จริงๆ:
```
Pattern Matching Rules: Contains  (regex)
Patterns to Test:       \bactive\b
```

🔍 **สิ่งที่เกิดขึ้น:** `Contains` treat pattern เป็น regex — ดังนั้น `(active)` คือ regex group และ `active` plain text ก็ match ได้ แต่ถ้าพิมพ์ผิดเป็น `(active` (ไม่ปิด parenthesis) จะเกิด regex error ที่ debug ยาก — ถ้าต้องการ plain text search ให้ใช้ `Substring` เสมอ

🤔 **เหตุผล:** ผู้ใช้ JMeter ใหม่มักสับสนระหว่าง "Contains" ที่ฟังดูเหมือน "plain text search" แต่จริงๆ ใช้ regex

*(source: https://jmeter.apache.org/usermanual/component_reference.html — Pattern Matching Rules: "Contains, Equals, Matches, Substring")*

---

### ❌ (c) JSON Path ผิด Syntax ทำให้ Assertion ไม่ทำงาน

**แบบผิด:**
```
Assert JSON Path exists: data.user.id
                         # ลืม $ ที่ root
```

```
Assert JSON Path exists: $[data][user][id]
                         # ใช้ bracket notation ผิด — ควรใช้กับ array index เท่านั้น
```

**แบบถูก:**
```
Assert JSON Path exists: $.data.user.id
                         # ต้องขึ้นต้นด้วย $ เสมอ

Assert JSON Path exists: $.users[0].id
                         # bracket notation ใช้กับ array index
```

🔍 **สิ่งที่เกิดขึ้น:** ถ้า JSON Path syntax ผิด JMeter อาจ report ว่า "path not found" แม้ field จะมีอยู่จริง — หรือบางกรณี assertion pass โดยไม่ตรวจอะไร (ขึ้นอยู่กับ "Invert assertion" setting)

🤔 **เหตุผล:** JSON Path มี syntax เฉพาะ — `$` คือ root object, `.` คือ child, `[]` คือ array index — ต้องตรวจสอบ path ด้วย View Results Tree → Response Body ก่อนตั้ง assertion เสมอ

*(source: https://jmeter.apache.org/usermanual/component_reference.html — JSON Assertion)*

---

## 7. สรุปบท

**คำถาม Retrieval — ตอบก่อนดูเฉลย:**

หยุดอ่าน เขียนคำตอบลงกระดาษก่อน อย่างน้อย 30 วินาทีต่อข้อ — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น ก่อนดูเฉลย

---

**คำถามที่ 1:** API ของคุณ return HTTP 200 เสมอไม่ว่าจะสำเร็จหรือ error — โดย success case return `{"result": "ok"}` และ error case return `{"result": "error", "code": "E001"}` — คุณจะตั้ง assertion อย่างไรให้ JMeter แยกออกระหว่างสองกรณีนี้ได้? บอก assertion type และค่าที่ตั้ง

เขียนคำตอบลงกระดาษก่อน

---

> **เฉลย:** ใช้ JSON Assertion ตรวจ `$.result` ด้วย Expected value `ok` (ปิด regex) — ถ้า response เป็น error case ค่าจะเป็น "error" ไม่ตรงกับ "ok" JMeter จะนับเป็น failure และแสดงใน error rate ถึงแม้ HTTP status จะเป็น 200

---

**คำถามที่ 2 (Code-based):** ดู assertion configuration นี้:

```
Field to Test:          Response Body
Pattern Matching Rules: Contains
Patterns to Test:       \d{3}
```

Assertion นี้ตรวจอะไร? จะ match กับ response ใดบ้าง? มีความเสี่ยงอะไรในการใช้งานจริง?

เขียนคำตอบก่อนดูเฉลย

---

> **เฉลย:** Assertion นี้ตรวจว่า response body มีตัวเลขติดกัน 3 หลัก (regex `\d{3}`) อยู่หรือเปล่า — จะ match ทุก response ที่มีตัวเลข 3 หลักขึ้นไปอยู่ด้วย เช่น "200", "abc123def", "THB1500.00", หรือแม้แต่ error code เช่น "E001" (ถ้ามีตัวเลข 3 หลักอยู่ในนั้น)
>
> ความเสี่ยง: false positive — assertion นี้ match ง่ายมาก เพราะ response body ทั่วไปมักมีตัวเลข 3 หลักอยู่เสมอ ทำให้ assertion แทบไม่มีประโยชน์ในการ detect error จริงๆ ควรเขียน pattern ให้ specific กว่านี้

---

**คำถามที่ 3 (Elaborative Interrogation):** ทำไมถึงต้องใช้ Duration Assertion แทนการดูแค่ตัวเลข 90th percentile จาก Aggregate Report? ทั้งสองวิธีให้ข้อมูลอะไรที่ต่างกัน?

---

> **เฉลย:** Duration Assertion **นับ slow response เป็น failure แบบ real-time** ทำให้ error rate สะท้อนว่ามีกี่ % ของ request ที่ช้าเกิน SLA — ช่วยในการ set pass/fail criteria อัตโนมัติ เช่น ถ้า error rate > 5% ให้ถือว่า test fail
>
> 90th percentile ใน Aggregate Report บอก**ค่าสถิติ** หลังจบ test แล้ว — ดูว่าภาพรวม latency เป็นอย่างไร แต่ไม่ได้ mark แต่ละ request ว่า pass/fail
>
> ใช้ทั้งคู่: Duration Assertion เพื่อ automatic pass/fail, Aggregate Report เพื่อ understand distribution

---

**Generation effect:** ก่อนปิดบทนี้ ลองเขียน "เมื่อไหรควรใช้ JSON Assertion แทน Response Assertion?" ด้วยคำของตัวเองโดยไม่เปิดเอกสาร — เขียนแล้วค้นหาจุดที่ยังไม่แน่ใจ

**Remediation path:**
- ยังสับสน Contains vs Substring → ทดลองสร้าง assertion ใหม่แล้ว test กับ response จริงใน View Results Tree
- ยังไม่เข้าใจ JSON Path syntax → ลองใช้ online JSON Path tester เช่น https://jsonpath.com ก่อนนำมาใช้ใน JMeter
- ยังไม่แน่ใจว่าควรใช้ assertion กี่ตัว → เริ่มจาก 2 ตัวเสมอ: Response Assertion (status code) + JSON Assertion (field สำคัญ 1 field) แล้วเพิ่มถ้าจำเป็น
