## ก่อนอ่านบทนี้ ลองตอบ:

1. Flaky test คืออะไร และสาเหตุที่พบบ่อยที่สุด 3 ข้อที่ทำให้ test ไม่ stable มีอะไรบ้าง?

2. Test Data Factory pattern ทำไมถึงต้องสร้าง unique data ต่อ test แทนที่จะใช้ข้อมูลชุดเดิมซ้ำกัน? ถ้าไม่ทำแบบนี้จะเกิดปัญหาอะไร?

---

เฉลย:

1. Flaky test คือ test ที่บางครั้งผ่าน บางครั้งไม่ผ่านโดยไม่มี code เปลี่ยนแปลง ทำให้ทีมหมดความเชื่อถือ CI และ merge code โดยไม่แน่ใจ สาเหตุหลัก 3 ข้อคือ: (1) **Timing issues** — page ยังโหลดไม่เสร็จแต่ test ดำเนินการไปแล้ว (2) **Shared state** — tests รัน parallel แล้ว state ปนกัน เช่น test A สร้างข้อมูลในขณะที่ test B กำลังลบทั้งหมด (3) **External dependency** — network ช้าหรือ third-party API ไม่ stable

2. Test Data Factory สร้าง unique data ต่อ test เพราะเมื่อ tests รัน parallel บน machine เดียวกัน แต่ละ test ต้องทำงานกับ data ของตัวเองโดยไม่กระทบกัน ถ้าใช้ข้อมูลชุดเดิมซ้ำกัน — test A อาจกำลัง update record เดิมที่ test B กำลัง assert ค่าอยู่พร้อมกัน ทำให้ผลลัพธ์ไม่แน่นอนขึ้นกับ race condition ไม่ใช่ behavior จริงของ app

---

# บทที่ 19: Database State Verification — ปิด Loop ด้วยการตรวจ DB

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- ใช้ `request` fixture เพื่อ read-back ข้อมูลจาก API หลัง UI action — ยืนยันว่า backend รับข้อมูลจริงไม่ใช่แค่ UI แสดงผล
- อ่านไฟล์ DB โดยตรงด้วย Node.js `fs` เพื่อ verify สถานะ persistence ที่ชั้น storage จริง
- ทำ cross-layer verification ครบทั้ง 3 ชั้น (UI → API → DB → UI re-render) ในชุดทดสอบเดียว
- ใช้ `expect.poll()` สำหรับ async DB writes ที่ไม่เสร็จทันทีหลัง HTTP response
- ทำ negative verification — ยืนยันว่าข้อมูลถูก delete ออกจาก DB จริง ไม่ใช่แค่หายจาก UI
- จัดการ DB isolation เพื่อป้องกัน test contamination ข้ามบท และ reset state ก่อนแต่ละ test

---

## 2. ทำไมต้องรู้? (Why)

สมมติระบบ e-commerce ของคุณมี bug แบบนี้: ผู้ใช้กด "สั่งซื้อ" — UI แสดง "Order confirmed! #12345" พร้อม animation สวยงาม แต่ backend async handler มี race condition — order ถูก write ลง DB แค่ 60% ของเวลา อีก 40% silent fail ไป ไม่มี error message ไม่มี HTTP 500 ทุกอย่างดูปกติ

Test ที่ตรวจแค่ UI จะ **ผ่าน 100%** ทั้งที่ bug นี้ทำให้ลูกค้า 40% ไม่ได้รับสินค้า

หรือระบบ HR ที่ admin กด "ลบพนักงาน" แล้ว UI แสดงว่าลบแล้ว แต่ soft-delete logic มีเงื่อนไขผิด — record ยังอยู่ใน DB และยังสามารถ login ได้อยู่ ถ้า test ตรวจแค่ว่า "ชื่อหายออกจาก list" ก็จะผ่าน แต่ security bug ยังอยู่ครบ

อีกกรณี: form validation ผ่าน frontend แต่ backend ใช้ database transaction — ถ้าข้อมูล violate constraint ตัวใดตัวหนึ่ง backend จะ rollback ทั้งหมด UI ไม่รู้เรื่อง แสดง success ต่อไป แต่ DB ไม่มีข้อมูลนั้นเลย

**ปัญหาร่วมของทั้งสามกรณีคือ: test ตรวจแค่ชั้นเดียว**

ระบบจริงทำงาน 3 ชั้นเสมอ:

- **UI Layer** — สิ่งที่ผู้ใช้เห็นและโต้ตอบ
- **API Layer** — business logic, validation, transaction
- **DB Layer** — state ที่ persist จริง — ข้อมูลที่อยู่ที่นี่คือ "ความจริง" ของระบบ

Test ที่ครอบคลุมต้องตรวจทั้ง 3 ชั้น เพราะแต่ละชั้นสามารถ fail แบบ silent โดยชั้นบนยังแสดงผลปกติได้

---

## 3. Analogy

**การตรวจ DB หลัง action เหมือนระบบตรวจสอบของธนาคาร 3 ชั้น**

ลองนึกภาพคุณโอนเงิน 50,000 บาทผ่าน ATM:

- **ATM screen (UI Layer)** แสดง "โอนเงินสำเร็จ! Transaction ID: TXN-9981" — คุณเห็นข้อความนี้และเชื่อว่าเสร็จแล้ว
- **Transaction API (API Layer)** รับ request, สร้าง transaction record, return HTTP 200 พร้อม transaction ID — ดูเหมือนทุกอย่างถูกต้อง
- **Core Banking System (DB Layer)** ตัดยอดจริงจากบัญชีคุณและเพิ่มยอดในบัญชีปลายทาง — นี่คือสิ่งที่เกิดขึ้น "จริงๆ"

ธนาคารไม่ได้เชื่อแค่ ATM screen — ระบบ reconciliation รัน batch job ตรวจสอบทุก transaction ว่า ATM บอกว่าโอนแล้ว, API มี record ตรงกัน, **และ Core Banking ตัดยอดจริงหรือเปล่า** ถ้า 3 ชั้นไม่ตรงกัน → flag เป็น exception ทันที

การทำ DB verification ใน Playwright test คือการสร้าง "reconciliation layer" เดียวกันนี้ให้กับ automated test ของคุณ — ไม่ไว้ใจแค่ UI บอกว่าสำเร็จ แต่ตรวจว่า "Core Banking (DB)" บันทึกจริงหรือเปล่า

⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:

- **"ถ้า API return HTTP 200 แสดงว่า DB ถูกต้องเสมอ"** — ไม่จริง เหมือนธนาคารที่ API ตอบรับ request แล้ว แต่ส่งต่อไปให้ async job เขียน DB ซึ่งอาจ fail ทีหลัง HTTP 200 ส่งออกไปแล้ว หรือเกิด DB transaction rollback หลัง response — API บอกสำเร็จแต่ Core Banking ไม่มีข้อมูล

- **"UI แสดงข้อมูลถูกต้องแปลว่า DB มีข้อมูลนั้น"** — ไม่จริง เหมือน ATM screen แสดงยอดเงินจาก cache ที่ยังไม่ sync กับ Core Banking — UI อาจแสดงจาก optimistic update (แสดงผลล่วงหน้าก่อน DB จะ confirm) หรือจาก in-memory state ที่ยังไม่ได้ persist จริง

---
