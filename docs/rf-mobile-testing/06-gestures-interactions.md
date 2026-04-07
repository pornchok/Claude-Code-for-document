# บทที่ 6: Gestures — Tap, Swipe, Scroll, Long Press

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อน:

> **ทำไม `accessibility_id` ถึงดีกว่า `xpath` สำหรับ mobile locator? และเมื่อไหรถึงควรใช้ xpath?**

---

<details>
<summary>ดูเฉลย</summary>

accessibility_id เร็วกว่า (ไม่ต้อง scan ทั้ง UI tree), stable กว่า (ไม่ขึ้นกับ UI structure), และ portable ระหว่าง Android/iOS — ใช้ xpath เป็น fallback เมื่อไม่มี accessibility_id หรือ resource-id

</details>

---

## วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะ:

- ใช้ Swipe, Scroll เพื่อ navigate ใน app ได้
- ใช้ Long Press และ Tap ได้
- Hide keyboard หลัง input ได้
- เข้าใจว่าทำไม gesture ใน mobile ถึงต่างจาก mouse event ใน web

---

## ทำไมต้องรู้? (Why)

Mobile app ต้องการ interaction แบบ touch ที่ web ไม่มี

ใน web testing คุณแค่ click, type, scroll (ด้วย JavaScript execute) — แต่บน mobile มีอีกเยอะ:
- **Swipe** — ปัดหน้าจอ (navigation, delete item, unlock)
- **Scroll** — เลื่อนดู list หรือ page
- **Long Press** — กดค้าง (menu, copy, drag)
- **Pinch/Zoom** — ย่อ/ขยาย (maps, image)

ถ้าทำ gesture ไม่ได้ → ทดสอบ flow ที่ต้อง swipe หรือ scroll ไม่ได้เลย

---

## Analogy: Gesture เหมือน Remote Control ที่ต่างกัน

ลองนึกถึง remote TV กับ remote PS5:
- ปุ่มพื้นฐาน (ปิด/เปิด, volume) = เหมือนกัน → click, type ใน web และ mobile
- PS5 มี touchpad, L2/R2 pressure-sensitive = เฉพาะ PS5 → swipe, long press ใน mobile

Web testing = remote TV (ปุ่มมาตรฐาน)
Mobile testing = remote PS5 (มี features พิเศษที่ต้องเรียนใหม่)

> ⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
> - Gesture เป็น "optional extra" — จริงๆ app หลายตัว require gesture สำหรับ flow หลัก (เช่น swipe to delete, pull to refresh)
> - Gesture ทำงานเหมือนกันทุก app — จริงๆ แต่ละ app ใช้ gesture library ต่างกัน บาง swipe ต้องปรับ coordinates หรือ speed

---

## เนื้อหาหลัก

### Swipe

Swipe ใช้สำหรับ: เลื่อนหน้าจอ, เปลี่ยนหน้า, ลบ item

```robotframework
# Swipe(start_x, start_y, end_x, end_y, duration_ms)
Swipe    500    1500    500    500    800
# ปัดจากกลางล่างขึ้นบน — scroll down (เนื้อหาขยับขึ้น)
```

**ทิศทาง Swipe:**
```
Swipe Up (scroll down):    start_y สูง → end_y ต่ำ
Swipe Down (scroll up):    start_y ต่ำ → end_y สูง  
Swipe Left (next page):    start_x ขวา → end_x ซ้าย
Swipe Right (prev page):   start_x ซ้าย → end_x ขวา
```

ข้อควรระวัง: coordinates ขึ้นอยู่กับ resolution ของ device — ต้องปรับให้ตรงกับ emulator ที่ใช้

### Scroll

Scroll element ที่ต้องการให้เข้ามาในหน้าจอ:

```robotframework
# Scroll ลงไปหา element
Scroll Element Into View    accessibility_id=submit_button

# หรือ scroll ทั้งหน้า
Swipe    540    1600    540    400    1000    # scroll down
```

### Long Press

Long press สำหรับ context menu หรือ drag:

```robotframework
# Long Press ที่ element นาน 2 วินาที
Long Press    accessibility_id=transaction_item    2000
```

### Hide Keyboard

หลัง Input Text บน mobile มักต้องซ่อน keyboard เพื่อให้เห็น element อื่น:

```robotframework
Input Text    accessibility_id=search_field    test query
Hide Keyboard
# ตอนนี้ keyboard ซ่อนแล้ว สามารถ click element อื่นได้
Click Element    accessibility_id=search_button
```

### Tap ด้วย Coordinates

เมื่อ element หาด้วย locator ไม่ได้ ใช้ tap ตำแหน่ง:

```robotframework
# Tap ที่ coordinates (x, y)
Tap    540    960
```

---

## ตัวอย่าง 3 ระดับ

### Beginner: Scroll หา element ที่อยู่นอกหน้าจอ

```robotframework
# tested: Robot Framework 7.x, AppiumLibrary 3.0.0
# requires: emulator รันอยู่, Appium server รันอยู่

*** Settings ***
Library    AppiumLibrary

*** Keywords ***
Scroll Down Once
    Swipe    540    1400    540    600    800

*** Test Cases ***
หา Terms & Conditions ที่อยู่ด้านล่างสุด
    [Setup]    Open Application    http://localhost:4723
    ...    platformName=Android
    ...    appium:automationName=UIAutomator2
    ...    appium:deviceName=emulator-5554
    ...    appium:appPackage=com.example.app
    ...    appium:appActivity=.RegisterActivity

    Wait Until Element Is Visible    accessibility_id=register_form    10s

    # Scroll ลงหา checkbox
    Scroll Down Once
    Scroll Down Once
    Wait Until Element Is Visible    accessibility_id=terms_checkbox    5s
    Click Element    accessibility_id=terms_checkbox

    [Teardown]    Close Application
```

### Intermediate: Swipe เพื่อ delete item ใน list

Banking app มักมี pattern "swipe left to delete" สำหรับ saved recipients:

```robotframework
# tested: Robot Framework 7.x, AppiumLibrary 3.0.0
# requires: app ที่มี swipeable list item

*** Settings ***
Library    AppiumLibrary

*** Keywords ***
Swipe Left On Element
    [Arguments]    ${locator}
    ${element}=    Get WebElement    ${locator}
    ${location}=   Get Element Location    ${element}
    ${size}=       Get Element Size        ${element}
    ${start_x}=    Evaluate    ${location}[x] + ${size}[width] - 10
    ${start_y}=    Evaluate    ${location}[y] + ${size}[height] / 2
    ${end_x}=      Evaluate    ${location}[x] + 10
    Swipe    ${start_x}    ${start_y}    ${end_x}    ${start_y}    500

*** Test Cases ***
ลบ Saved Recipient ด้วย Swipe
    [Setup]    Open Application    http://localhost:4723
    ...    platformName=Android
    ...    appium:automationName=UIAutomator2
    ...    appium:deviceName=emulator-5554
    ...    appium:appPackage=com.example.app
    ...    appium:appActivity=.SavedRecipientsActivity

    Wait Until Element Is Visible    accessibility_id=recipient_john    10s
    Swipe Left On Element    accessibility_id=recipient_john
    Wait Until Element Is Visible    accessibility_id=delete_button    5s
    Click Element    accessibility_id=delete_button
    Page Should Not Contain Element    accessibility_id=recipient_john

    [Teardown]    Close Application
```

### Advanced: Pull to Refresh pattern

Banking app มักใช้ pull-to-refresh เพื่ออัปเดตยอดเงิน:

```robotframework
# tested: Robot Framework 7.x, AppiumLibrary 3.0.0
# requires: app ที่มี pull-to-refresh feature

*** Settings ***
Library    AppiumLibrary

*** Keywords ***
Pull To Refresh
    # ดึงจากบนลงล่าง เริ่มที่ y ต่ำ (ใกล้บน) ไปยัง y สูง (ลงล่าง)
    Swipe    540    300    540    900    1000
    # รอ loading เสร็จ
    Run Keyword And Ignore Error
    ...    Wait Until Element Is Not Visible    accessibility_id=loading_indicator    15s

ตรวจสอบยอดเงินอัปเดตหลัง Refresh
    [Setup]    Open Application    http://localhost:4723
    ...    platformName=Android
    ...    appium:automationName=UIAutomator2
    ...    appium:deviceName=emulator-5554
    ...    appium:appPackage=com.example.app
    ...    appium:appActivity=.DashboardActivity

    Wait Until Element Is Visible    accessibility_id=balance_display    15s
    ${balance_before}=    Get Text    accessibility_id=balance_display

    Pull To Refresh

    Wait Until Element Is Visible    accessibility_id=balance_display    15s
    ${balance_after}=    Get Text    accessibility_id=balance_display

    # ยอดเงินอาจเหมือนกัน (ไม่มี transaction ใหม่) แต่ refresh ต้องทำงาน
    Log    Balance before: ${balance_before}, after: ${balance_after}

    [Teardown]    Close Application
```

---

## Common Mistakes

❌ **ใช้ Swipe coordinates คงที่โดยไม่คำนึง device resolution**
→ Test pass บน emulator หนึ่งแต่ fail บน emulator อื่น
✅ **คำนวณ coordinates แบบ dynamic จาก screen size หรือ element location**
```robotframework
${size}=    Get Window Size
${width}=   Set Variable    ${size}[width]
${height}=  Set Variable    ${size}[height]
# ใช้ percentage ของ screen size แทน hardcode
Swipe    ${width/2}    ${height*0.8}    ${width/2}    ${height*0.2}    800
```
*(source: AppiumLibrary + Appium community best practice)*

---

❌ **ลืม Hide Keyboard หลัง Input Text**
→ Keyboard บัง element ที่ต้องการ click → Element not visible error
✅ **เพิ่ม `Hide Keyboard` หลัง input เสมอ** ก่อน interact กับ element อื่น
*(source: AppiumLibrary docs)*

---

❌ **Swipe เร็วเกินไป (duration สั้นเกินไป)**
→ App รับ gesture เป็น tap แทน swipe
✅ **ปรับ duration ให้เหมาะสม** — ลองเริ่มที่ 800-1000ms
*(source: ประสบการณ์ Appium community)*

---

## สรุปบท

ลองตอบก่อนดูเฉลย:

> **คำถาม 1:** `Swipe    540    1400    540    600    800` — ปัดทิศทางไหน? และ parameter สุดท้าย `800` หมายความว่าอะไร?

> **คำถาม 2:** ทำไมต้อง `Hide Keyboard` หลัง `Input Text`? จะเกิดอะไรถ้าไม่ทำ?

> **คำถาม 3:** ทำไม gesture coordinates ที่ hardcode ถึงเป็นปัญหาเมื่อรัน test บน device หลายรุ่น?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย 1:** ปัดจาก y=1400 (ด้านล่าง) ขึ้นไป y=600 (ด้านบน) = swipe up / scroll down เพื่อดูเนื้อหาที่อยู่ด้านล่าง — parameter สุดท้าย `800` = duration 800ms (ความเร็วในการปัด)

**เฉลย 2:** keyboard บัง element อื่นบนหน้าจอ — ถ้าไม่ซ่อน keyboard การ click element ที่ถูก keyboard บังจะ fail เพราะ element not visible หรือ not interactable

**เฉลย 3:** coordinates เป็น pixel absolute — device ที่ resolution ต่างกัน (เช่น 1080x2400 vs 1440x3040) จะมีพื้นที่หน้าจอต่างกัน coordinates เดียวกันอาจชนกับ element คนละตัวหรือ out-of-bounds

</details>

---

**บทต่อไป:** [บทที่ 7 — Page Object Model](07-page-object-model.md)
