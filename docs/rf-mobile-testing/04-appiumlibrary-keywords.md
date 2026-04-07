# บทที่ 4: AppiumLibrary Keywords — เทียบกับ SeleniumLibrary

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อน:

> **ใน capabilities คุณต้องบอก Appium อะไรบ้างเพื่อให้ session เริ่มได้? และ `appium:` prefix ใช้เมื่อไหร่?**

---

<details>
<summary>ดูเฉลย</summary>

ต้องบอก platformName, automationName, deviceName, appPackage, appActivity (สำหรับ Android native app) — capabilities ที่เฉพาะ Appium ต้องมี `appium:` prefix ตาม W3C standard ยกเว้น `platformName` ที่เป็น W3C standard อยู่แล้ว

</details>

---

## วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะ:

- เทียบ keyword ของ SeleniumLibrary กับ AppiumLibrary ได้แบบ 1:1
- ใช้ keyword หลักของ AppiumLibrary ได้: Open Application, Click Element, Input Text, Page Should Contain Element
- เข้าใจว่า keyword ไหนเหมือน ไหนต่าง ไหนมีเฉพาะ mobile
- เขียน test case พื้นฐานสำหรับ mobile app ได้

---

## ทำไมต้องรู้? (Why)

นี่คือ "good news" สำหรับคนที่รู้ RF + Selenium มาแล้ว

AppiumLibrary ออกแบบมาให้ keyword คล้ายกับ SeleniumLibrary มากที่สุด — เพราะทั้งสองใช้ W3C WebDriver protocol เดียวกัน

ดังนั้นคุณไม่ได้เริ่มจากศูนย์ — คุณเริ่มจาก ~70% ที่รู้อยู่แล้ว

---

## Analogy: เปลี่ยนรถแต่ใช้ใบขับขี่เดิม

เรียน AppiumLibrary เหมือนคนที่ขับรถเก๋ง (Selenium/web) มาตลอด แล้วต้องขับ SUV (Appium/mobile)

- กฎจราจร = เหมือนกัน (W3C WebDriver protocol)
- วิธีขับเบื้องต้น = เหมือนกัน (click, input text, assertions)
- พวงมาลัย/เบรค = ต่างกันนิดหน่อย (locator strategies)
- ฟีเจอร์พิเศษ = SUV มี 4WD ที่รถเก๋งไม่มี (gesture, touch actions)

> ⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
> - ทุก keyword เหมือนกันหมด → มี keyword บางตัวที่ AppiumLibrary ไม่มี (เช่น Select Frame ซึ่งเป็น web concept)
> - Locator เหมือนกัน → mobile ใช้ locator strategies ที่ต่างออกไป (เรียนละเอียดในบทที่ 5)

---

## เนื้อหาหลัก

### ตารางเปรียบเทียบ: SeleniumLibrary vs AppiumLibrary

| หน้าที่ | SeleniumLibrary | AppiumLibrary |
|--------|----------------|--------------|
| เปิด session | `Open Browser` | `Open Application` |
| ปิด session | `Close Browser` | `Close Application` |
| คลิก element | `Click Element` | `Click Element` ✅ เหมือนกัน |
| พิมพ์ข้อความ | `Input Text` | `Input Text` ✅ เหมือนกัน |
| อ่านข้อความ | `Get Text` | `Get Text` ✅ เหมือนกัน |
| ตรวจ element มีอยู่ | `Page Should Contain Element` | `Page Should Contain Element` ✅ เหมือนกัน |
| รอ element | `Wait Until Element Is Visible` | `Wait Until Element Is Visible` ✅ เหมือนกัน |
| รอ element หายไป | `Wait Until Element Is Not Visible` | `Wait Until Element Is Not Visible` ✅ เหมือนกัน |
| เปิด URL | `Go To` | `Go To Url` (mobile web เท่านั้น) |
| Swipe | ❌ ไม่มี | `Swipe` ✅ mobile only |
| Scroll | ❌ ไม่มี | `Scroll` ✅ mobile only |
| Long Press | ❌ ไม่มี | `Long Press` ✅ mobile only |
| Lock device | ❌ ไม่มี | `Lock` ✅ mobile only |
| เปิด/ปิด keyboard | ❌ ไม่มี | `Hide Keyboard` ✅ mobile only |

### Keyword ที่ใช้บ่อยที่สุด

#### Open Application

```robotframework
Open Application    http://localhost:4723
...    platformName=Android
...    appium:automationName=UIAutomator2
...    appium:deviceName=emulator-5554
...    appium:appPackage=com.example.app
...    appium:appActivity=.MainActivity
```

> "Opens a new application to the given Appium server"
> *(AppiumLibrary docs)*

#### Click Element

```robotframework
Click Element    accessibility_id=login_button
Click Element    id=com.example.app:id/btn_submit
Click Element    xpath=//android.widget.Button[@text='Login']
```

> "Clicks the element identified by locator."
> *(AppiumLibrary docs)*

#### Input Text

```robotframework
Input Text    accessibility_id=username_field    john@email.com
Input Text    id=com.example.app:id/et_password    secret123
```

> "Types the given text into the text field identified by locator."
> *(AppiumLibrary docs)*

#### Page Should Contain Element / Element Should Be Visible

```robotframework
Page Should Contain Element    accessibility_id=home_screen
Element Should Be Visible      id=com.example.app:id/tv_welcome
```

#### Wait Until Element Is Visible

```robotframework
# รอสูงสุด 10 วินาที
Wait Until Element Is Visible    accessibility_id=dashboard    10s
```

#### Get Text

```robotframework
${balance}=    Get Text    accessibility_id=account_balance
Should Be Equal As Strings    ${balance}    ฿10,000.00
```

---

## ตัวอย่าง 3 ระดับ

### Beginner: Test case ง่ายๆ — เปิดแอปและตรวจ element

```robotframework
# tested: Robot Framework 7.x, AppiumLibrary 3.0.0, Android API 33 emulator
# requires: emulator รันอยู่, Appium server รันอยู่

*** Settings ***
Library    AppiumLibrary

*** Variables ***
${APPIUM}     http://localhost:4723
${PKG}        com.example.app
${ACT}        .MainActivity

*** Test Cases ***
เปิดแอปและตรวจว่า Login screen ขึ้น
    [Setup]    Open Application    ${APPIUM}
    ...    platformName=Android
    ...    appium:automationName=UIAutomator2
    ...    appium:deviceName=emulator-5554
    ...    appium:appPackage=${PKG}
    ...    appium:appActivity=${ACT}

    Page Should Contain Element    accessibility_id=username_input
    Page Should Contain Element    accessibility_id=password_input
    Page Should Contain Element    accessibility_id=login_button

    [Teardown]    Close Application
```

### Intermediate: Login flow ครบ + error case

```robotframework
# tested: Robot Framework 7.x, AppiumLibrary 3.0.0
# requires: app ที่มี login screen พร้อม username/password field

*** Settings ***
Library    AppiumLibrary

*** Variables ***
${APPIUM}     http://localhost:4723
${PKG}        com.example.app
${ACT}        .MainActivity

*** Keywords ***
เปิดแอป
    Open Application    ${APPIUM}
    ...    platformName=Android
    ...    appium:automationName=UIAutomator2
    ...    appium:deviceName=emulator-5554
    ...    appium:appPackage=${PKG}
    ...    appium:appActivity=${ACT}

ทำการ Login ด้วย
    [Arguments]    ${username}    ${password}
    Wait Until Element Is Visible    accessibility_id=username_input    10s
    Input Text    accessibility_id=username_input    ${username}
    Input Text    accessibility_id=password_input    ${password}
    Click Element    accessibility_id=login_button

*** Test Cases ***
Login สำเร็จ
    [Setup]    เปิดแอป
    ทำการ Login ด้วย    valid_user    valid_pass
    Wait Until Element Is Visible    accessibility_id=home_screen    15s
    Page Should Contain Element    accessibility_id=home_screen
    [Teardown]    Close Application

Login ล้มเหลว — password ผิด
    [Setup]    เปิดแอป
    ทำการ Login ด้วย    valid_user    wrong_pass
    Wait Until Element Is Visible    accessibility_id=error_message    10s
    ${error}=    Get Text    accessibility_id=error_message
    Should Contain    ${error}    Invalid credentials
    [Teardown]    Close Application
```

### Advanced: Keyword ที่ handle loading state อย่างมืออาชีพ

```robotframework
# tested: Robot Framework 7.x, AppiumLibrary 3.0.0
# pattern นี้ใช้กับ app ที่มี loading spinner

*** Settings ***
Library    AppiumLibrary

*** Keywords ***
รอให้ Loading หายไปและ Element ขึ้น
    [Arguments]    ${element_locator}    ${timeout}=30s
    # รอ loading spinner หายก่อน (ถ้ามี)
    Run Keyword And Ignore Error
    ...    Wait Until Element Is Not Visible    accessibility_id=loading_spinner    ${timeout}
    # แล้วค่อยรอ element ที่ต้องการ
    Wait Until Element Is Visible    ${element_locator}    ${timeout}
    Element Should Be Enabled    ${element_locator}

ทดสอบ Transfer Money Flow
    [Setup]    เปิดแอป
    ทำการ Login ด้วย    test_user    test_pass
    รอให้ Loading หายไปและ Element ขึ้น    accessibility_id=transfer_button
    Click Element    accessibility_id=transfer_button
    รอให้ Loading หายไปและ Element ขึ้น    accessibility_id=recipient_field
    Input Text    accessibility_id=recipient_field    0812345678
    [Teardown]    Close Application
```

---

## Common Mistakes

❌ **ใช้ `Open Browser` แทน `Open Application`**
→ Browser เปิดขึ้น แต่ไม่ใช่ mobile app ที่ต้องการทดสอบ
✅ **AppiumLibrary ใช้ `Open Application`** และต้องระบุ Appium server URL เป็น argument แรก
*(source: serhatbolsu.github.io/robotframework-appiumlibrary)*

---

❌ **ไม่มี Wait — click ทันทีที่ app เปิด**
```
[FAIL] Element 'accessibility_id=login_button' not found
```
→ App ยังโหลดไม่เสร็จ
✅ **ใช้ `Wait Until Element Is Visible` ก่อน interact เสมอ** — app ใช้เวลา load
*(source: AppiumLibrary best practice)*

---

❌ **ไม่ใส่ Close Application ใน Teardown**
→ Session ค้างอยู่ → test ถัดไป session conflict
✅ **ใส่ `Close Application` ใน `[Teardown]` ทุก test case เสมอ**
*(source: serhatbolsu.github.io/robotframework-appiumlibrary)*

---

❌ **copy locator จาก web มาใช้กับ mobile**
```
Click Element    css=#login-btn    # ❌ CSS ไม่ work บน mobile native app
```
✅ **Mobile native app ใช้ accessibility_id, resource-id, หรือ xpath** — CSS selector ใช้ได้กับ mobile web เท่านั้น
*(source: บทที่ 5 เรื่อง locators)*

---

## สรุปบท

ลองตอบก่อนดูเฉลย:

> **คำถาม 1:** keyword `Input Text` ของ AppiumLibrary กับ SeleniumLibrary เหมือนหรือต่างกัน? และถ้าใช้ SeleniumLibrary อยู่แล้วจะต้องเรียนใหม่ไหม?

> **คำถาม 2:** ทำไมต้องใช้ `Wait Until Element Is Visible` ก่อน `Click Element` บน mobile app? บน web ก็ต้องทำแบบนี้ไหม?

> **คำถาม 3:** ถ้า test case ของคุณ fail เพราะ element not found — สิ่งแรกที่ควร debug คืออะไร?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย 1:** `Input Text` เหมือนกันเลย (signature เดียวกัน, ชื่อเดียวกัน) ความต่างหลักอยู่ที่ locator strategy ที่ใช้ — mobile ไม่รับ CSS selector แต่รับ accessibility_id และ resource-id แทน ดังนั้นต้องเรียนเรื่อง locators ใหม่ แต่ไม่ต้องเรียน keyword ใหม่

**เฉลย 2:** Mobile app มี loading time มากกว่า web — transition ระหว่าง screen, API call, animation ทำให้ element ยังไม่พร้อมเมื่อ click ถึงแม้ web ก็ควรทำ แต่ mobile เป็นปัญหาบ่อยกว่ามาก

**เฉลย 3:** ดู screenshot ของ app ณ ตอนที่ fail (Appium สามารถถ่าย screenshot ได้) เพื่อดูว่า UI ตอนนั้นแสดงอะไร จากนั้นตรวจว่า locator ถูกต้องโดยใช้ Appium Inspector

</details>

---

**บทต่อไป:** [บทที่ 5 — Mobile Locators](05-mobile-locators.md)
