# บทที่ 1: โลกของ Mobile Testing — Appium คืออะไร?

## วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะ:

- อธิบายได้ว่า Appium คืออะไร และทำงานยังไง
- บอกความแตกต่างระหว่าง Appium กับ Selenium ได้
- เข้าใจว่า AppiumLibrary สัมพันธ์กับ Appium ยังไง
- รู้จักประเภทของ mobile app ที่ Appium รองรับ

---

## ทำไมต้องรู้? (Why)

ก่อนที่คุณจะเขียน test แรก คุณต้องเข้าใจก่อนว่า **สิ่งที่คุณกำลังจะเรียนมันคืออะไร และทำงานยังไง**

ถ้าคุณกระโดดเข้าไป install ทันทีโดยไม่เข้าใจ architecture — คุณจะงงว่าทำไมต้อง start Appium server ก่อน, ทำไม capabilities ถึงไม่ทำงาน, หรือทำไม keyword ใน RF ถึง connect กับ device ได้

บทนี้สร้าง mental model ที่ถูกต้องให้คุณก่อน — ทุกอย่างจะเข้าที่เข้าทางมากขึ้น

---

## Analogy: Appium เหมือนล่ามภาษา

จินตนาการว่าคุณเป็นนักท่องเที่ยวชาวไทย ไปประชุมในญี่ปุ่น แต่คุณพูดภาษาญี่ปุ่นไม่ได้ คุณต้องพูดผ่าน **ล่าม** ที่แปลสิ่งที่คุณพูดให้คนญี่ปุ่นเข้าใจ

ในโลก mobile testing:
- **คุณ** = Robot Framework + AppiumLibrary (คนที่ออกคำสั่ง)
- **ล่าม** = Appium server (รับคำสั่งแล้วแปลงเป็นภาษาที่ OS เข้าใจ)
- **คนญี่ปุ่น** = Android หรือ iOS (ระบบปฏิบัติการที่จะ execute จริง)

คุณพูด "click ปุ่ม Login" → ล่าม (Appium) แปลงเป็น UIAutomator2 command → Android execute จริง

> ⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
> - Appium server ต้อง "รู้ภาษาไทย" ก่อน → จริงๆ Appium รับ HTTP requests ที่เป็น W3C WebDriver protocol เสมอ ไม่ว่าจะมาจาก RF, Python script, หรือ WDIO
> - มีล่ามคนเดียว → จริงๆ Appium ใช้ driver แยกต่างหากสำหรับแต่ละ platform (UIAutomator2 สำหรับ Android, XCUITest สำหรับ iOS)

---

## เนื้อหาหลัก

### Appium คืออะไร?

Appium คือ open-source framework สำหรับ **UI automation ของ mobile app**

> "Appium is an open-source project and ecosystem of related software, designed to facilitate UI automation of many app platforms."
> *(appium.io)*

ง่ายๆ คือ: Appium = Selenium แต่สำหรับ mobile

### Appium ต่างจาก Selenium ยังไง?

| | Selenium | Appium |
|---|---|---|
| Target | Web browser | Mobile app (และ desktop) |
| Protocol | W3C WebDriver | W3C WebDriver (เหมือนกัน!) |
| Driver | ChromeDriver, GeckoDriver | UIAutomator2, XCUITest |
| Installation | ง่าย — driver รวมกับ browser | ต้องติดตั้ง Appium server แยก + driver แยก |
| Locators | CSS, XPath, ID | accessibility id, resource-id, XPath |

สังเกตว่า **protocol เหมือนกัน** — นี่คือเหตุผลที่ AppiumLibrary keyword หลายตัวเหมือนกับ SeleniumLibrary เลย เพราะ "ภาษาที่ใช้คุย" เป็น standard เดียวกัน

### Architecture: Appium ทำงานยังไง?

```
┌─────────────────────────┐
│   Robot Framework Test  │
│  (+ AppiumLibrary)      │
└────────────┬────────────┘
             │ HTTP (W3C WebDriver Protocol)
             ▼
┌─────────────────────────┐
│    Appium Server        │  ← ต้อง start ก่อนรัน test
│  (runs on Node.js)      │
└────────────┬────────────┘
             │
      ┌──────┴──────┐
      ▼             ▼
┌──────────┐  ┌──────────┐
│ Android  │  │   iOS    │
│UIAuto2   │  │XCUITest  │
│  Driver  │  │  Driver  │
└──────────┘  └──────────┘
      │
      ▼
┌──────────────────────────┐
│  Android Emulator /      │
│  Real Device             │
└──────────────────────────┘
```

**Flow ที่เกิดขึ้นเวลารัน test:**
1. คุณรัน `robot test.robot`
2. AppiumLibrary ส่ง HTTP request ไปที่ Appium server (localhost:4723)
3. Appium server ส่งต่อคำสั่งไปที่ UIAutomator2 driver
4. UIAutomator2 driver สั่งงาน Android emulator/device จริงๆ

### Appium 2.x — แบบใหม่ที่ต้องรู้

Appium เวอร์ชันปัจจุบัน (2.x) เปลี่ยนสถาปัตยกรรมสำคัญ:

**ก่อน (Appium 1.x):** ติดตั้ง Appium ตัวเดียว มี driver ทุกอย่างมาให้

**ตอนนี้ (Appium 2.x):** Appium = server เปล่าๆ ต้องติดตั้ง driver แยกต่างหาก

> "Simply installing Appium 2.0 will install the Appium server only, but no drivers. To install drivers, you must instead use the new Appium extension CLI."
> *(appium.io)*

นั่นคือเหตุผลที่ setup มีสองขั้นตอน:
```bash
npm i --location=global appium        # ติดตั้ง server
appium driver install uiautomator2    # ติดตั้ง Android driver แยก
```

### AppiumLibrary คืออะไร?

AppiumLibrary คือ **Robot Framework library** ที่ทำหน้าที่เป็น client ที่คุยกับ Appium server

- Package name: `robotframework-appiumlibrary`
- เขียนด้วย Python
- Version ปัจจุบัน: 3.0.0 (รองรับ Python 3.9-3.13)

ความสัมพันธ์:
```
AppiumLibrary → คุยกับ → Appium Server → คุยกับ → Device
```

### ประเภทของ Mobile App ที่ Appium รองรับ

| ประเภท | คืออะไร | ตัวอย่าง |
|--------|---------|---------|
| **Native App** | เขียนด้วย Java/Kotlin (Android) หรือ Swift/ObjC (iOS) | แอปธนาคาร, แอปกล้อง |
| **Hybrid App** | Native shell ที่ embed WebView ข้างใน | แอปบางตัวที่มีทั้ง native screen และ web หน้า |
| **Mobile Web** | Website เปิดบน mobile browser | ทดสอบ Chrome บน Android |

สำหรับ Banking app เช่น Krungsri Nimble มักเป็น **Native App** หรือ **Hybrid App**

---

## ตัวอย่าง 3 ระดับ

### Beginner: เปรียบเทียบ keyword เปิด browser vs เปิด app

**SeleniumLibrary (web — ที่คุณรู้อยู่แล้ว):**
```robotframework
*** Settings ***
Library    SeleniumLibrary

*** Test Cases ***
เปิด Browser ไป Google
    Open Browser    https://www.google.com    chrome
    Title Should Be    Google
    Close Browser
```

**AppiumLibrary (mobile):**
```robotframework
*** Settings ***
Library    AppiumLibrary

*** Test Cases ***
เปิด App บน Emulator
    Open Application    http://localhost:4723
    ...    platformName=Android
    ...    automationName=UIAutomator2
    ...    appPackage=com.example.app
    ...    appActivity=.MainActivity
    Close Application
```

สังเกตความแตกต่าง:
- `Open Browser` → `Open Application`
- ไม่ได้ใส่ URL ของ website แต่ใส่ URL ของ Appium server
- ต้องบอก platform, driver, และ app identifier

### Intermediate: อธิบาย flow ให้ทีม

สมมติคุณต้องอธิบายให้ developer ในทีมเข้าใจว่า test ทำงานยังไง:

```
เวลา RF รัน test:
1. AppiumLibrary ส่ง POST /session ไปที่ localhost:4723
2. Appium server รับ request + เช็ค capabilities
3. Appium เปิด UIAutomator2 driver
4. UIAutomator2 เปิด app บน emulator
5. RF รัน keyword ต่อไป (click, input text ฯลฯ)
6. ทุก keyword = HTTP request หนึ่งครั้ง
```

### Advanced: ทำไม test บางครั้ง timeout?

เมื่อ RF keyword หมดเวลาโดยไม่ได้รับ response มักเกิดจาก:
- Appium server ไม่ได้ start → connection refused
- Emulator ยังไม่ boot ครบ → session creation timeout
- App crash ระหว่างรัน → command timeout

การ debug: ดู Appium server log (terminal ที่ start Appium) ก่อน debug ที่ RF log เสมอ

---

## Common Mistakes

❌ **เริ่มรัน test ก่อน start Appium server**
```
[FAIL] ConnectionRefusedError: Connection refused (localhost:4723)
```
✅ **start Appium server ก่อนรัน test ทุกครั้ง** — รัน `appium` ใน terminal แล้วปล่อยไว้
*(source: appium.io/docs/en/2.0/quickstart/)*

---

❌ **ลง Appium 2.x แล้วลืมติดตั้ง driver**
```
[FAIL] No driver found for capabilities
```
✅ **ต้องรัน `appium driver install uiautomator2` แยกต่างหาก** หลังจาก install Appium
*(source: appium.io/docs/en/2.0/quickstart/install/)*

---

❌ **คิดว่า AppiumLibrary = Appium**
→ ทำให้งงเวลา error เพราะไม่รู้จะ debug ที่ไหน
✅ **AppiumLibrary = RF client / Appium = server** — error จาก connection มักมาจาก Appium server, error จาก keyword syntax มาจาก AppiumLibrary
*(source: github.com/serhatbolsu/robotframework-appiumlibrary)*

---

## สรุปบท

ก่อนอ่านเฉลย ลองตอบคำถามนี้ด้วยคำของคุณเองก่อน:

> **คำถาม 1:** ถ้ามี developer ถามคุณว่า "Appium ต่างจาก Selenium ยังไง?" คุณจะตอบยังไงใน 2-3 ประโยค?

> **คำถาม 2:** เวลา Robot Framework รัน keyword `Click Element` บน mobile app — มีกี่ "hop" ที่คำสั่งต้องผ่านก่อนถึง device จริง? และแต่ละ hop คืออะไร?

> **คำถาม 3:** ทำไม Appium 2.x ถึงต้องติดตั้ง driver แยกต่างหาก? ข้อดีของ design นี้คืออะไร?

---

<details>
<summary>ดูเฉลย (กด expand หลังจากตอบเองแล้ว)</summary>

**เฉลย 1:** Appium กับ Selenium ใช้ protocol เดียวกัน (W3C WebDriver) แต่ Appium target mobile app ในขณะที่ Selenium target web browser Appium ต้องการ server แยกต่างหาก และใช้ driver เฉพาะ platform (UIAutomator2 สำหรับ Android)

**เฉลย 2:** 3 hop — (1) AppiumLibrary ส่ง HTTP request ไปที่ Appium server, (2) Appium server ส่งคำสั่งไปที่ UIAutomator2 driver, (3) UIAutomator2 driver execute บน Android device/emulator จริง

**เฉลย 3:** Appium 2.x แยก server กับ driver เพื่อให้ update driver แต่ละ platform แยกกันได้ โดยไม่ต้อง update Appium ทั้งหมด (modular architecture) ข้อดีคือ driver สามารถ release version ใหม่ได้เร็วกว่าและมี third-party driver ได้

</details>

---

**บทต่อไป:** [บทที่ 2 — Setup สภาพแวดล้อม](02-setup-environment.md)
