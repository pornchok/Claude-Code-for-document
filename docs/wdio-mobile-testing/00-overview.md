# Mobile Testing ด้วย WebdriverIO + Appium

## โปรเจคนี้คืออะไร?

คอร์สนี้สอน **WebdriverIO (WDIO)** สำหรับ mobile testing ด้วย Appium

ออกแบบสำหรับคนที่:
- เรียน Series 1 (RF + AppiumLibrary) มาแล้ว → รู้ Appium architecture และ mobile testing concepts แล้ว
- หรือมีพื้นหลัง web testing แต่ต้องการเรียน JavaScript test framework

WebdriverIO ต่างจาก Robot Framework ตรงที่:
- เขียนด้วย **JavaScript / TypeScript**
- ใช้ **Mocha** (หรือ Jasmine/Cucumber) เป็น test runner
- มี ecosystem กว้างมากในฝั่ง JavaScript

---

## อ่านจบแล้วทำอะไรได้บ้าง?

- อธิบาย WebdriverIO คืออะไร และต่างจาก Robot Framework ยังไง
- สร้าง WDIO project ด้วย `npm init wdio` ได้
- เชื่อม WDIO กับ Appium server เพื่อทดสอบ mobile app ได้
- เขียน test case ด้วย Mocha `describe/it` pattern ได้
- ใช้ WDIO selectors สำหรับ mobile element ได้
- สร้าง Page Objects ด้วย JavaScript class ได้
- เลือกได้ว่างานไหนควรใช้ RF และงานไหนควรใช้ WDIO

---

## Prerequisites

| ต้องรู้ | ระดับ |
|---------|-------|
| Appium architecture (Session, Capabilities, Driver) | ✅ จำเป็น — เรียนจาก Series 1 บทที่ 1-3 |
| Mobile locators (accessibility_id, resource-id) | ✅ จำเป็น — เรียนจาก Series 1 บทที่ 5 |
| JavaScript พื้นฐาน (variables, functions, arrow functions) | ✅ จำเป็น |
| Node.js และ npm | ✅ จำเป็น — ติดตั้งแล้วจาก Series 1 |
| Robot Framework | ⚪ ไม่จำเป็น แต่ช่วยให้เปรียบเทียบได้ |

> ถ้ายังไม่รู้ JavaScript ให้เรียน JavaScript พื้นฐาน (MDN Web Docs — JavaScript First Steps) ก่อน

---

## สารบัญ

| บท | หัวข้อ | เวลาโดยประมาณ |
|----|--------|--------------|
| [บทที่ 1](01-what-is-wdio.md) | WebdriverIO คืออะไร — เทียบกับ Robot Framework | 25 นาที |
| [บทที่ 2](02-setup-wdio-project.md) | Setup WDIO Project | 45 นาที |
| [บทที่ 3](03-wdio-appium-config.md) | เชื่อม WDIO กับ Appium สำหรับ Mobile | 30 นาที |
| [บทที่ 4](04-writing-tests.md) | เขียน Test แรก — describe/it pattern | 40 นาที |
| [บทที่ 5](05-selectors-interactions.md) | Selectors และ Mobile Interactions | 35 นาที |
| [บทที่ 6](06-page-objects-wdio.md) | Page Objects ด้วย JavaScript Class | 40 นาที |
| [บทที่ 7](07-comparing-rf-wdio.md) | เปรียบเทียบ RF vs WDIO — ใช้อะไรเมื่อไหร่ | 30 นาที |
| [แบบฝึกหัด](exercises.md) | Exercises ทุก concept | — |
| [Glossary](glossary.md) | คำศัพท์ทั้งหมดในคอร์ส | — |

---

## Versions ที่ใช้ในคอร์สนี้

| เครื่องมือ | Version |
|-----------|---------|
| WebdriverIO | v9 |
| @wdio/appium-service | latest |
| @wdio/mocha-framework | latest |
| Appium | 2.x |
| Node.js | 18+ |
| npm | 9+ |
