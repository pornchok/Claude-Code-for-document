# บทที่ 1 — ทำไม QA ต้องรู้เรื่อง Mock API?

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:

- อธิบายได้ว่า mock API คืออะไรและแก้ปัญหาอะไรของ QA
- ระบุได้ว่าสถานการณ์ไหนในงานจริงที่ควรใช้ mock
- แยกแยะความแตกต่างระหว่าง mock, stub, และ fake ได้

---

## ทำไมต้องรู้? (Why)

ลองนึกถึงสถานการณ์นี้:

Sprint นี้คุณต้องทดสอบหน้า checkout แต่ team dev แจ้งว่า Payment API จะเสร็จอีก 2 อาทิตย์ คุณจะทำอะไรได้บ้าง?

**Option A:** รอ dev ทำเสร็จ → เสีย 2 อาทิตย์เปล่าๆ

**Option B:** ใช้ mock server แทน Payment API → ทดสอบได้เลยวันนี้

QA ที่รู้เรื่อง mock เลือก Option B ได้เสมอ และนั่นทำให้พวกเขาทำงานได้เร็วกว่าและ block น้อยกว่ามาก

---

## เนื้อหาหลัก

### Mock API คืออะไร?

Mock API คือ server ปลอมที่เราสร้างขึ้นมาเองเพื่อแทน API จริง มันรับ request เหมือนกัน แต่ return response ที่เรากำหนดเองได้

```
Test → Mock Server (ปลอม) → ได้ response ตามที่กำหนด
Test → Real API (จริง) → ได้ response จาก backend จริง
```

### ปัญหาจริงที่ mock แก้ได้

**1. API ยังไม่เสร็จ**
dev กำลังเขียน API แต่ QA ต้องเริ่มเขียน test script ได้แล้ว mock ช่วยให้ทั้งสอง team ทำงานพร้อมกันได้

**2. ทดสอบ negative cases ยาก**
จะให้ API จริง return 500 Internal Server Error เพื่อทดสอบ error handling → ยากมาก แต่กับ mock แค่กำหนดใน config ไฟล์เดียว

**3. Third-party API ไม่เสถียร**
Payment gateway บางเจ้า sandbox ล่มบ่อย → test fail ทั้งที่ code ถูก mock ช่วยให้ test ของเราไม่ขึ้นอยู่กับ third-party อีกต่อไป

**4. API มี rate limit หรือเสียเงิน**
บาง API เรียกได้แค่ 100 ครั้งต่อวัน หรือเรียกแต่ละครั้งมีค่าใช้จ่าย mock ทำให้เรียกได้ไม่จำกัด

**5. ต้องการ test ใน environment ที่ไม่มี backend**
CI/CD pipeline บางทีไม่มี database หรือ backend service mock ทำให้ test รันได้ทุกที่

### Mock vs Stub vs Fake

คำเหล่านี้มักใช้แทนกัน แต่มีความหมายที่ต่างกันนิดหน่อย:

| คำ | ความหมาย | ตัวอย่าง |
|----|---------|---------|
| **Stub** | ส่ง response สำเร็จรูปที่กำหนดไว้ล่วงหน้า | ขอ GET /users → ได้ list users เสมอ |
| **Mock** | Stub ที่ verify ได้ว่า request ถูกส่งมาจริง | ตรวจว่า API ถูกเรียก 1 ครั้งหรือเปล่า |
| **Fake** | Implementation จริงแต่ simplified | In-memory database แทน PostgreSQL |

WireMock ทำได้ทั้ง stub และ mock แต่ในชีวิตจริงคนมักเรียกรวมๆ ว่า "mock" ทั้งหมด

### ทำไมเลือก WireMock?

มี mock tool หลายตัว แต่ WireMock โดดเด่นเพราะ:

- **Standalone mode**: รันเป็น server แยกได้ โดยไม่ต้องเขียน Java
- **JSON config**: กำหนด stub ด้วย JSON ไฟล์ ง่าย อ่านได้ เก็บใน Git ได้
- **Admin API**: manage stub ผ่าน REST API ได้แบบ real-time
- **Mature & stable**: ใช้กันมากกว่า 10 ปี download 5 ล้านครั้งต่อเดือน
- **ฟรี open source**: ไม่มีค่าใช้จ่าย

---

## ตัวอย่าง 3 ระดับ

### Beginner — QA ใหม่รอ API

ทีมกำลังสร้างแอปสั่งอาหาร Menu API จะเสร็จสัปดาห์หน้า แต่ QA อยากเริ่มทดสอบหน้า menu แล้ว

→ สร้าง mock server ที่ return รายการเมนู 3-4 รายการ ทดสอบ UI ได้เลยโดยไม่รอ backend

### Intermediate — ทดสอบ payment error

QA ต้องทดสอบว่าแอปแสดง error message ถูกต้องเมื่อ payment ล้มเหลว แต่การทำให้ payment จริงล้มเหลวจงใจทำได้ยากมาก

→ สร้าง mock ที่ return `{"status": "failed", "reason": "insufficient_funds"}` ทดสอบ error flow ได้ครบ

### Advanced — CI/CD pipeline ที่ไม่มี third-party

ทีมมี CI pipeline ที่รัน E2E test ทุก PR แต่ external shipping API ไม่อนุญาต access จาก IP ของ CI server

→ สร้าง mock server เป็น service หนึ่งใน Docker Compose ให้ test ทั้งหมดเรียก mock แทน ทำให้ CI รันได้โดยไม่พึ่ง external dependency

---

## Common Mistakes

❌ **ใช้ mock แทน integration test ทุกอย่าง**
Mock ทำให้ test เร็วและ stable แต่ต้องมี integration test กับ API จริงอยู่ด้วยเสมอ ไม่งั้นจะไม่รู้ว่า mock กับของจริงต่างกันตอนไหน
→ ✅ ใช้ mock สำหรับ unit/component test, ใช้ API จริงสำหรับ integration/E2E test บางส่วน
*(source: wiremock.org/docs/overview/)*

❌ **Mock ซับซ้อนเกินไปจน maintain ยาก**
บางทีพยายาม mock ทุก edge case จนไฟล์ config ยาวหลายร้อยบรรทัด
→ ✅ เริ่มจาก happy path ก่อน แล้วค่อยเพิ่ม error case ทีละอย่าง
*(source: wiremock.org/docs/stubbing/)*

❌ **ลืม update mock เมื่อ API จริงเปลี่ยน**
Mock ที่ outdated ทำให้ test ผ่านทั้งที่ของจริงพัง
→ ✅ เก็บ mock files ไว้ใน Git repository เดียวกับ test code และ review ทุกครั้งที่ API contract เปลี่ยน
*(source: wiremock.org/docs/overview/)*

---

## สรุปบท

ก่อนอ่านเฉลย ลองตอบคำถามเหล่านี้ในใจก่อนนะครับ:

**⏸ คำถาม Retrieval**

1. จงยกตัวอย่างสถานการณ์จริงในงาน QA ที่การมี mock server จะช่วยได้ (ต้องเป็นสถานการณ์ที่ไม่มีในบทนี้)

2. Mock กับ Stub ต่างกันอย่างไร? ให้อธิบายด้วยคำตัวเองสั้นๆ

3. ทำไมถึงไม่ควรใช้ mock แทน integration test ทั้งหมด?

---

??? note "เฉลย (คลิกเพื่อดู)"

    **ข้อ 1**: ตัวอย่างอื่น เช่น — ทดสอบ login flow เมื่อ auth service ล่ม, ทดสอบแอปในโหมด offline, ทดสอบ response เมื่อ database timeout

    **ข้อ 2**: Stub แค่ return response ที่กำหนดไว้ — Mock ทำเหมือนกันแต่ยัง verify ได้ด้วยว่า request ถูกส่งมากี่ครั้งและมี parameter อะไรบ้าง

    **ข้อ 3**: Mock ไม่รู้ว่า API จริงเปลี่ยน spec ไปหรือเปล่า ถ้าไม่มี integration test กับ API จริงเลย เราจะ "test กับตัวเอง" และอาจ miss bug จริงในการ integrate
