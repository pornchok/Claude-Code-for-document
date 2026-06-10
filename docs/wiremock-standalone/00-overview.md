# WireMock สำหรับ QA — ภาพรวม

Mock API ให้เป็น แล้วชีวิต QA จะง่ายขึ้นมาก

---

## Course นี้สอนอะไร?

คุณจะได้เรียนรู้วิธีใช้ **WireMock Standalone** — โปรแกรมที่ทำหน้าที่เป็น API ปลอม (mock server) โดยไม่ต้องเขียน code Java สักบรรทัดเดียว ใช้แค่ไฟล์ JSON กับ command line

---

## Prerequisites

- เปิด terminal / command prompt ได้
- มี Java ติดตั้งอยู่ในเครื่อง (Java 11 ขึ้นไป)
- รู้จัก API คืออะไรในระดับพื้นฐาน (รู้ว่า GET/POST คืออะไร)
- ใช้ Postman ได้เบื้องต้น

ตรวจสอบ Java ด้วยคำสั่ง:

```bash
java -version
```

---

## ทำไม QA ถึงต้องรู้เรื่อง Mock?

> "รอ API จาก dev ไม่ได้ เพราะยังเขียนไม่เสร็จ"

> "ทดสอบ error cases ไม่ได้ เพราะ production ไม่ยอม return error"

> "Third-party API ล่มบ่อย ทำให้ test ของเรา fail ไปด้วย"

ถ้าคุณเคยเจอปัญหาแบบนี้ WireMock คือคำตอบครับ

---

## โครงสร้าง Course

| บท | หัวข้อ | สิ่งที่ได้ |
|----|--------|-----------|
| 1 | Why Mock? | เข้าใจว่า mock แก้ปัญหาอะไรของ QA |
| 2 | Setup Standalone | ติดตั้งและรัน WireMock ได้ครั้งแรก |
| 3 | Stub แรกของคุณ | สร้าง mock API แรกด้วยไฟล์ JSON |
| 4 | Request Matching | ควบคุมว่า stub จะ match request แบบไหน |
| 5 | Response Control | ควบคุม response ได้ครบ (status, body, delay) |
| 6 | Error & Fault Simulation | จำลอง error และ network fault สำหรับ negative testing |
| 7 | ใช้กับ Postman | ทดสอบ stub ผ่าน Postman แบบมืออาชีพ |
| 8 | ใช้กับ Robot Framework | integrate WireMock เข้า RF test suite |
| 9 | Stateful Scenarios | จำลอง workflow หลายขั้นตอน |
| 10 | Best Practices | จัดระเบียบและทำงานร่วมกันเป็น team |

---

## เครื่องมือที่ใช้ใน Course นี้

- **WireMock Standalone** v3.13.2 — ตัวหลัก
- **Postman** — ทดสอบ stub
- **Robot Framework + RequestsLibrary** — automate test ที่ใช้ mock
- **curl** — ทดสอบเร็วจาก terminal

---

[เริ่มบทที่ 1 →](01-why-mock.md)
