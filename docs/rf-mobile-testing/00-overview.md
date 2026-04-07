# Mobile Testing ด้วย Robot Framework + AppiumLibrary

## โปรเจคนี้คืออะไร?

คอร์สนี้สอนการทดสอบ mobile app โดยเฉพาะสำหรับคนที่ **รู้ Robot Framework + Selenium อยู่แล้ว** และอยากขยับมาทำ mobile testing

เนื้อหาเริ่มจากศูนย์ในฝั่ง mobile — ไม่ต้องรู้ Android/iOS development มาก่อน

---

## อ่านจบแล้วทำอะไรได้บ้าง?

- อธิบายได้ว่า Appium คืออะไร และต่างจาก Selenium ยังไง
- ติดตั้งและ setup Appium server + Android emulator ได้เอง
- เขียน Robot Framework test สำหรับ mobile app โดยใช้ AppiumLibrary
- ใช้ Appium Inspector หา locator บน mobile screen ได้
- เขียน gesture interactions (tap, swipe, scroll) ใน RF
- ออกแบบ Page Object Pattern สำหรับ mobile test suite

---

## Prerequisites

| ต้องรู้ | ระดับ |
|---------|-------|
| Robot Framework พื้นฐาน | ✅ จำเป็น — รู้ keyword, test case, variable |
| SeleniumLibrary | ✅ ช่วยได้มาก — keyword หลายตัวเหมือนกัน |
| Command line / Terminal | ✅ จำเป็น — ต้องรัน commands |
| Python พื้นฐาน | ⚪ ไม่จำเป็น แต่มีช่วย |
| Android/iOS development | ❌ ไม่จำเป็น |
| JavaScript | ❌ ไม่จำเป็น (นั่นคือ WDIO series อีกตัว) |

---

## สิ่งที่ต้องเตรียมก่อนเริ่ม

- เครื่อง Windows, macOS, หรือ Linux (ทดสอบ Android ได้ทุก platform)
- Python 3.9+ ติดตั้งแล้ว
- Node.js 18+ ติดตั้งแล้ว
- เชื่อมต่ออินเทอร์เน็ต (ต้องดาวน์โหลด Android Studio ~1GB+)

> ⚠️ **iOS testing** ต้องใช้ macOS เท่านั้น คอร์สนี้เน้น Android เพื่อให้ทุก platform เรียนได้

---

## สารบัญ

| บท | หัวข้อ | เวลาโดยประมาณ |
|----|--------|--------------|
| [บทที่ 1](01-mobile-testing-landscape.md) | โลกของ Mobile Testing — Appium คืออะไร | 30 นาที |
| [บทที่ 2](02-setup-environment.md) | Setup สภาพแวดล้อม — Android Studio, Appium, Inspector | 60-90 นาที |
| [บทที่ 3](03-appium-capabilities.md) | Appium Capabilities — กุญแจสู่ mobile session | 30 นาที |
| [บทที่ 4](04-appiumlibrary-keywords.md) | AppiumLibrary Keywords — เทียบกับ SeleniumLibrary | 45 นาที |
| [บทที่ 5](05-mobile-locators.md) | Mobile Locators — หา element บน mobile screen | 40 นาที |
| [บทที่ 6](06-gestures-interactions.md) | Gestures — Tap, Swipe, Scroll, Long Press | 40 นาที |
| [บทที่ 7](07-page-object-model.md) | Page Object Model บน Mobile | 45 นาที |
| [แบบฝึกหัด](exercises.md) | Exercises ทุก concept | — |
| [Glossary](glossary.md) | คำศัพท์ทั้งหมดในคอร์ส | — |

---

## วิธีใช้คอร์สนี้ให้ได้ผลสูงสุด

1. **ทำ Setup ให้ผ่านก่อน** (บทที่ 2) — ถ้า environment ไม่พร้อม ทดสอบอะไรก็ไม่ได้
2. **เปิด Appium Inspector คู่ขนาน** — เวลาเรียน locator ให้ลองใช้ Inspector จริงๆ กับ emulator
3. **พิมพ์ code เอง** — อย่า copy-paste โดยตรง การพิมพ์เองช่วยให้จำ syntax ได้เร็วขึ้น
4. **ทำ exercises ก่อนดูเฉลย** — บังคับตัวเองคิดก่อนอย่างน้อย 5 นาที

---

## Versions ที่ใช้ในคอร์สนี้

| เครื่องมือ | Version |
|-----------|---------|
| Robot Framework | 7.x |
| robotframework-appiumlibrary | 3.0.0 |
| Appium | 2.x |
| UIAutomator2 driver | latest |
| Python | 3.9+ |
| Node.js | 18+ |
