# บทที่ 3: Appium Capabilities — กุญแจสู่ Mobile Session

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อน:

> **เวลา RF รัน `Open Application` บน mobile — Appium server ต้องรู้อะไรบ้างเพื่อเริ่ม session ได้?**

---

<details>
<summary>ดูเฉลย</summary>

Appium ต้องรู้ platform (Android/iOS), driver ที่จะใช้ (UIAutomator2/XCUITest), และ app ที่จะเปิด (appPackage+appActivity หรือ path ของไฟล์ .apk)

</details>

---

## วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะ:

- อธิบายได้ว่า Capabilities คืออะไร และทำไมถึงจำเป็น
- รู้จัก capabilities หลักที่ต้องใส่สำหรับ Android testing
- หา appPackage และ appActivity ของ app ที่จะทดสอบได้
- เขียน `Open Application` ใน RF พร้อม capabilities ที่ถูกต้องได้
- เข้าใจความแตกต่างระหว่าง capabilities ของ Appium standard และ appium: prefix

---

## ทำไมต้องรู้? (Why)

Capabilities คือส่วนที่คนเรียน mobile testing ใหม่งงที่สุด

ใน web testing คุณแค่บอก browser: `Open Browser  https://example.com  chrome` — แค่นั้นพอ

แต่ mobile testing ต้องบอกข้อมูลเยอะกว่ามาก เพราะ Appium ต้องรู้ว่าจะ:
- ใช้ driver ตัวไหน (Android หรือ iOS?)
- เปิด app ไหน (มีหลาย app บน device)
- ใช้ emulator หรือ real device?
- Reset app state ก่อนทดสอบไหม?

ถ้า capabilities ผิดแม้แต่ตัวเดียว → session ไม่ start

---

## Analogy: Capabilities เหมือนฟอร์มจอง Lab

ก่อนเข้าทดสอบในห้องแล็บ คุณต้องกรอกฟอร์ม:
- ชื่อนักวิจัย = platformName (ระบุว่าทำงานกับ platform ไหน)
- ห้องแล็บที่ต้องการ = automationName (ระบุ driver)
- อุปกรณ์ที่จะใช้ = deviceName
- สารที่จะทดสอบ = appPackage/appActivity (ระบุ app)

ถ้ากรอกฟอร์มผิด → admin ไม่อนุมัติ → เข้าห้องแล็บไม่ได้

> ⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
> - Capabilities เป็น "ข้อมูลคงที่" ที่ใส่ครั้งเดียว → จริงๆ แต่ละ test session สามารถมี capabilities ต่างกันได้ (เช่น test บน Android 12 vs Android 13)
> - Capabilities ทุกตัวบังคับ → มีเพียงไม่กี่ตัวที่ required จริงๆ ที่เหลือเป็น optional

---

## เนื้อหาหลัก

### Capabilities คืออะไร?

> "Capabilities are the set of parameters used to start an Appium session."
> *(appium.io)*

ง่ายๆ คือ: capabilities = "config" ที่บอก Appium ว่าจะทำอะไร กับอะไร และบน device ไหน

### Capabilities หลักสำหรับ Android

| Capability | หมายความว่า | ตัวอย่าง |
|-----------|------------|---------|
| `platformName` | OS platform | `Android` |
| `automationName` | Driver ที่ใช้ | `UIAutomator2` |
| `deviceName` | ชื่อ device/emulator | `emulator-5554` |
| `platformVersion` | Android version | `13` |
| `appPackage` | Package name ของ app | `com.example.myapp` |
| `appActivity` | Activity ที่จะเปิด | `.MainActivity` |

### appium: Prefix — Appium 2.x Standard

ใน Appium 2.x capabilities ที่เฉพาะ Appium ต้องมี prefix `appium:` ตาม W3C WebDriver standard

> "Following W3C WebDriver standards, Appium uses the appium: prefix for vendor-specific capabilities."
> *(appium.io)*

```robotframework
# Appium 2.x — ใส่ appium: prefix
Open Application    http://localhost:4723
...    platformName=Android
...    appium:automationName=UIAutomator2
...    appium:deviceName=emulator-5554
...    appium:appPackage=com.example.app
...    appium:appActivity=.MainActivity
```

> **Note:** `platformName` ไม่ต้องมี prefix เพราะเป็น W3C standard capability

### วิธีหา appPackage และ appActivity

**วิธีที่ 1: ใช้ adb (ง่ายที่สุด)**

เปิด app บน emulator ก่อน แล้วรัน:
```bash
adb shell dumpsys window | grep -E 'mCurrentFocus|mFocusedApp'
```

Output ตัวอย่าง:
```
mCurrentFocus=Window{... com.example.myapp/.MainActivity}
```
→ `appPackage=com.example.myapp`, `appActivity=.MainActivity`

**วิธีที่ 2: ดูจาก apk file**

```bash
# ต้องมี aapt ใน Android SDK build-tools
aapt dump badging app.apk | grep -E "package|launchable-activity"
```

**วิธีที่ 3: ถามจาก developer**

สำหรับ banking app เช่น Krungsri Nimble — developer ของทีมจะรู้ค่านี้เสมอ

### ตัวอย่าง Capabilities ครบ

```robotframework
*** Settings ***
Library    AppiumLibrary

*** Variables ***
${APPIUM_URL}       http://localhost:4723
${PLATFORM}         Android
${AUTOMATION}       UIAutomator2
${DEVICE}           emulator-5554
${APP_PACKAGE}      com.example.myapp
${APP_ACTIVITY}     .MainActivity

*** Test Cases ***
เปิด App และตรวจสอบ
    Open Application    ${APPIUM_URL}
    ...    platformName=${PLATFORM}
    ...    appium:automationName=${AUTOMATION}
    ...    appium:deviceName=${DEVICE}
    ...    appium:appPackage=${APP_PACKAGE}
    ...    appium:appActivity=${APP_ACTIVITY}
    Page Should Contain Element    id=welcome_text
    Close Application
```

### Capabilities เสริมที่มีประโยชน์

| Capability | หน้าที่ | เมื่อใช้ |
|-----------|--------|---------|
| `appium:noReset` | ไม่ clear app data ก่อนรัน (default: false) | เมื่อต้องการ state เดิม |
| `appium:fullReset` | Uninstall + reinstall app | เมื่อต้องการ fresh start สมบูรณ์ |
| `appium:newCommandTimeout` | timeout ระหว่าง commands (วินาที) | ป้องกัน session หมดเวลาเร็วเกินไป |
| `appium:app` | path ไปที่ .apk file | เมื่อต้องการ install app ก่อนทดสอบ |

---

## ตัวอย่าง 3 ระดับ

### Beginner: เปิด Chrome บน Android Emulator

Chrome คือ mobile web app — capabilities ต่างกันนิดหน่อย (ไม่ต้อง appPackage/appActivity)

```robotframework
*** Settings ***
Library    AppiumLibrary

*** Test Cases ***
เปิด Chrome บน Android
    Open Application    http://localhost:4723
    ...    platformName=Android
    ...    appium:automationName=UIAutomator2
    ...    appium:deviceName=emulator-5554
    ...    appium:browserName=Chrome
    Go To Url    https://www.google.com
    Title Should Be    Google
    Close Application
```

> ⚠️ code นี้ทดสอบบน emulator — ต้องมี emulator รันอยู่ก่อน

### Intermediate: ใช้ Variables file แยก capabilities

ใน production จะไม่ hardcode capabilities ใน test case แต่จะแยกออกมา:

```robotframework
# variables.robot
*** Variables ***
${APPIUM_URL}           http://localhost:4723
${PLATFORM_NAME}        Android
${AUTOMATION_NAME}      UIAutomator2
${DEVICE_NAME}          emulator-5554
${APP_PACKAGE}          com.nimble.bank
${APP_ACTIVITY}         .ui.splash.SplashActivity
${APP_NO_RESET}         ${True}
```

```robotframework
# test_login.robot
*** Settings ***
Library      AppiumLibrary
Variables    variables.robot

*** Keywords ***
เปิดแอป
    Open Application    ${APPIUM_URL}
    ...    platformName=${PLATFORM_NAME}
    ...    appium:automationName=${AUTOMATION_NAME}
    ...    appium:deviceName=${DEVICE_NAME}
    ...    appium:appPackage=${APP_PACKAGE}
    ...    appium:appActivity=${APP_ACTIVITY}
    ...    appium:noReset=${APP_NO_RESET}

*** Test Cases ***
ทดสอบ Login หน้าจอแรก
    เปิดแอป
    Page Should Contain Element    accessibility_id=login_button
    Close Application
```

### Advanced: เชื่อม capabilities กับ CI/CD

ใน pipeline capabilities จะมาจาก environment variables:

```bash
# CI environment
export DEVICE_NAME="emulator-5554"
export APP_PACKAGE="com.nimble.bank.staging"
```

```robotframework
*** Variables ***
${DEVICE_NAME}      %{DEVICE_NAME}       # อ่านจาก env var
${APP_PACKAGE}      %{APP_PACKAGE}
```

```bash
# รัน test พร้อม override variable
robot --variable DEVICE_NAME:real-device-serial test_login.robot
```

---

## Common Mistakes

❌ **ใช้ `appPackage` ผิด — ใส่ชื่อ activity แทน package**
```
appPackage=com.example.app.MainActivity   # ❌ ผิด
```
✅ **appPackage = ชื่อ package (ไม่มีชื่อ class), appActivity = ชื่อ activity**
```
appPackage=com.example.app
appActivity=.MainActivity
```
*(source: appium.io/docs/en/2.0/guides/caps/)*

---

❌ **ลืม appium: prefix บน Appium 2.x**
```
[FAIL] W3C Capabilities validation error: 'automationName' is not a valid W3C capability
```
✅ **ใช้ `appium:automationName`, `appium:appPackage` ฯลฯ** เมื่อใช้ Appium 2.x
*(source: appium.io/docs/en/2.0/guides/caps/)*

---

❌ **ใส่ deviceName ผิด — ใส่ชื่อ model แทน serial**
→ Appium หา device ไม่เจอ
✅ **ใช้ `adb devices` ดู serial จริง** แล้วใส่ตามนั้น (เช่น `emulator-5554`)
*(source: appium.io)*

---

## สรุปบท

ลองตอบก่อนดูเฉลย:

> **คำถาม 1:** ถ้า developer บอกว่า app ชื่อ `com.nimble.bank` มี entry point activity ชื่อ `SplashActivity` อยู่ใน package `.ui.splash` — คุณจะเขียน `appPackage` และ `appActivity` ยังไง?

> **คำถาม 2:** `appium:noReset=True` และ `appium:fullReset=True` ต่างกันยังไง? และใช้แต่ละตัวเมื่อไหร่?

> **คำถาม 3:** ทำไม `platformName` ถึงไม่ต้องมี `appium:` prefix เหมือน capabilities ตัวอื่น?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย 1:**
```
appPackage=com.nimble.bank
appActivity=.ui.splash.SplashActivity
```

**เฉลย 2:** `noReset=True` = ไม่ clear app data (รักษา login state, preferences) ใช้เมื่อต้องการ test ต่อเนื่อง / `fullReset=True` = uninstall แล้ว reinstall ใหม่ ใช้เมื่อต้องการ clean state สมบูรณ์

**เฉลย 3:** `platformName` เป็น W3C WebDriver standard capability ที่ทุก WebDriver implementation รู้จัก ไม่ใช่ capability เฉพาะของ Appium จึงไม่ต้อง prefix

</details>

---

**บทต่อไป:** [บทที่ 4 — AppiumLibrary Keywords](04-appiumlibrary-keywords.md)
