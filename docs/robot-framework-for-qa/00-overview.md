# Robot Framework สำหรับ QA Manual

## โปรเจคนี้คืออะไร?

Course นี้สอน Robot Framework ตั้งแต่ศูนย์ สำหรับ QA ที่เคย test แบบ manual มาแล้ว
และมีความรู้การเขียน code บ้าง แต่ยังไม่เคยทำ test automation จริงจัง

เรียนจบแล้วคุณจะ:
- ติดตั้งและ setup Robot Framework ได้ด้วยตัวเอง
- เขียน automated test สำหรับ web application ได้
- จัดโครงสร้างโปรเจคให้ maintainable
- อ่าน report และ debug test ที่ fail ได้
- ใช้ best practice ที่ทีม professional ใช้จริง

---

## Prerequisites

| ต้องรู้ | ระดับที่พอเพียง |
|---------|----------------|
| การเขียน code เบื้องต้น | รู้จัก variable, function, loop |
| การใช้ command line / terminal | รัน command ได้ เช่น `cd`, `ls` |
| ความรู้ HTML เบื้องต้น | รู้จัก element, attribute, id, class |
| Python (optional) | **ไม่จำเป็น** — RF มี syntax เป็นของตัวเอง |

ไม่จำเป็นต้องรู้ Python มาก่อน Robot Framework ใช้ภาษาที่อ่านเข้าใจง่ายกว่ามาก

---

## Table of Contents

| บท | หัวข้อ | สิ่งที่จะได้ |
|----|--------|-------------|
| [01](01-what-is-robot-framework.md) | ทำไมต้องใช้ Robot Framework? | เข้าใจ concept และ keyword-driven testing |
| [02](02-installation-setup.md) | ติดตั้งและ Setup | Python, RF, Browser Library, VS Code |
| [03](03-test-structure-syntax.md) | โครงสร้าง Test File | Sections, Variables, Settings syntax |
| [04](04-keywords-and-resource-files.md) | Keywords และ Resource Files | เขียน keyword เอง, แยก resource |
| [05](05-web-testing.md) | Web Testing | Locator, คลิก, พิมพ์, verify |
| [06](06-data-driven-testing.md) | Data-Driven Testing | Test Template, หลาย test data ใน test เดียว |
| [07](07-reports-and-debugging.md) | Reports และ Debugging | อ่าน log.html, debug test ที่ fail |
| [08](08-best-practices.md) | Best Practices | Project structure, Page Object, CI basics |

---

## วิธีใช้ Course นี้ให้ได้ประโยชน์สูงสุด

**อย่า copy-paste code** — พิมพ์เองทีละบรรทัด มือจำได้ดีกว่าตา

**ทำ exercise ทุกบท** — ส่วนสรุปมีคำถาม retrieval ลองตอบก่อนดูเฉลยเสมอ

**รันทุก code ที่เห็น** — ถ้า error ให้หยุดแก้ก่อน อย่าข้ามไป

**Version ที่ใช้ใน Course นี้:**
- Robot Framework 7.4.2
- Python 3.14.2
- Browser Library (Playwright-based) สำหรับ web testing

---

## เวลาที่ใช้โดยประมาณ

| บท | เวลา (อ่าน + ทำ exercise) |
|----|--------------------------|
| บทที่ 1-2 | 1.5 ชั่วโมง |
| บทที่ 3-4 | 2 ชั่วโมง |
| บทที่ 5-6 | 2.5 ชั่วโมง |
| บทที่ 7-8 | 2 ชั่วโมง |
| **รวม** | **~8 ชั่วโมง** |
