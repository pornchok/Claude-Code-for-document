# บทที่ 7: Page Object Model บน Mobile

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อน:

> **ถ้า locator ของปุ่ม Login เปลี่ยนจาก `accessibility_id=login_btn` เป็น `id=com.app:id/btn_login` — ถ้าคุณไม่ได้ใช้ Page Object คุณต้องแก้ไขกี่จุดในโปรเจค?**

---

<details>
<summary>ดูเฉลย</summary>

ต้องแก้ทุก test case ที่ใช้ locator นั้น — ถ้ามี 10 test case ก็ 10 จุด ถ้ามี 50 ก็ 50 จุด นี่คือปัญหาหลักที่ Page Object แก้ไข

</details>

---

## วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะ:

- อธิบาย Page Object Model pattern ได้และเหตุผลที่ใช้
- แยกไฟล์ locators และ keywords ออกจาก test cases ได้
- เขียน resource file สำหรับแต่ละ screen ของ app ได้
- ปรับโครงสร้างโปรเจคให้ maintain ได้ง่าย

---

## ทำไมต้องรู้? (Why)

โปรเจค automation ที่ไม่มี structure มักพัง 2-3 เดือนหลัง app เปลี่ยน

ปัญหาที่เกิดขึ้นบ่อยใน production:
1. Developer เปลี่ยน UI → locator ทุกตัวใน test ต้องแก้
2. Test case เต็มไปด้วย locators → อ่านยาก maintain ยาก
3. Keyword ซ้ำกันในหลาย test file → แก้หนึ่งที่ ลืมอีกที่

Page Object Model แก้ปัญหาเหล่านี้ทั้งหมด

---

## Analogy: Page Object เหมือน Manual ประจำเครื่อง

ลองนึกถึงการซ่อม ATM:
- **โดยไม่มี manual:** ช่างทุกคนต้องไปเปิดเครื่องเองหา part ที่ต้องการ ถ้า model เปลี่ยนทุกคนต้องเรียนรู้ใหม่
- **มี manual (Page Object):** manual ระบุว่า "ปุ่ม X อยู่ที่ตำแหน่ง A, ทำหน้าที่ B" — ถ้า ATM รุ่นใหม่ย้ายปุ่ม แค่แก้ manual ที่เดียว ช่างทุกคนก็ตามทันทันที

> ⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
> - Page Object = ไฟล์เดียวสำหรับทั้ง app → จริงๆ แต่ละ screen ควรมีไฟล์แยก (Login Page, Home Page, Transfer Page ฯลฯ)
> - Page Object แก้ทุกปัญหา → มันแก้เรื่อง maintainability แต่ไม่ได้ช่วยเรื่อง test flakiness จาก timing issues

---

## เนื้อหาหลัก

### โครงสร้างโปรเจค แบบ Page Object

```
project/
├── tests/
│   ├── login_test.robot
│   ├── transfer_test.robot
│   └── profile_test.robot
├── pages/
│   ├── login_page.robot       ← locators + keywords ของ Login screen
│   ├── home_page.robot        ← locators + keywords ของ Home screen
│   └── transfer_page.robot    ← locators + keywords ของ Transfer screen
├── resources/
│   ├── common_keywords.robot  ← keywords ที่ใช้ร่วมกัน (open app, close app)
│   └── variables.robot        ← app config, capabilities
└── results/
    └── (test results)
```

### ตัวอย่าง: Login Page Resource File

```robotframework
# pages/login_page.robot

*** Settings ***
Library    AppiumLibrary

*** Variables ***
# Locators
${LOGIN_USERNAME_FIELD}    accessibility_id=username_input
${LOGIN_PASSWORD_FIELD}    accessibility_id=password_input
${LOGIN_BUTTON}            accessibility_id=login_button
${LOGIN_ERROR_MESSAGE}     accessibility_id=error_message
${LOGIN_FORGOT_PASSWORD}   accessibility_id=forgot_password_link

*** Keywords ***
กรอก Username
    [Arguments]    ${username}
    Wait Until Element Is Visible    ${LOGIN_USERNAME_FIELD}    10s
    Input Text    ${LOGIN_USERNAME_FIELD}    ${username}

กรอก Password
    [Arguments]    ${password}
    Input Text    ${LOGIN_PASSWORD_FIELD}    ${password}
    Hide Keyboard

กด Login
    Click Element    ${LOGIN_BUTTON}

ตรวจสอบ Error Message ว่าคือ
    [Arguments]    ${expected_message}
    Wait Until Element Is Visible    ${LOGIN_ERROR_MESSAGE}    10s
    ${actual}=    Get Text    ${LOGIN_ERROR_MESSAGE}
    Should Contain    ${actual}    ${expected_message}

ทำการ Login ด้วย
    [Arguments]    ${username}    ${password}
    กรอก Username    ${username}
    กรอก Password    ${password}
    กด Login
```

### ตัวอย่าง: Home Page Resource File

```robotframework
# pages/home_page.robot

*** Settings ***
Library    AppiumLibrary

*** Variables ***
${HOME_BALANCE}           accessibility_id=account_balance
${HOME_TRANSFER_BTN}      accessibility_id=transfer_button
${HOME_HISTORY_BTN}       accessibility_id=history_button
${HOME_LOADING_SPINNER}   accessibility_id=loading_indicator

*** Keywords ***
รอให้ Home Screen โหลดครบ
    Run Keyword And Ignore Error
    ...    Wait Until Element Is Not Visible    ${HOME_LOADING_SPINNER}    15s
    Wait Until Element Is Visible    ${HOME_BALANCE}    15s

ดูยอดเงิน
    ${balance}=    Get Text    ${HOME_BALANCE}
    [Return]    ${balance}

ไปหน้า Transfer
    Click Element    ${HOME_TRANSFER_BTN}
```

### ตัวอย่าง: Test Case ที่ใช้ Page Objects

```robotframework
# tests/login_test.robot

*** Settings ***
Library      AppiumLibrary
Resource     ../pages/login_page.robot
Resource     ../pages/home_page.robot
Resource     ../resources/common_keywords.robot

*** Test Cases ***
Login สำเร็จด้วย credentials ที่ถูกต้อง
    [Setup]    เปิดแอปในสถานะ fresh
    ทำการ Login ด้วย    valid_user@email.com    ValidPass123
    รอให้ Home Screen โหลดครบ
    Page Should Contain Element    ${HOME_BALANCE}
    [Teardown]    Close Application

Login ล้มเหลวด้วย password ผิด
    [Setup]    เปิดแอปในสถานะ fresh
    ทำการ Login ด้วย    valid_user@email.com    WrongPass999
    ตรวจสอบ Error Message ว่าคือ    Invalid credentials
    [Teardown]    Close Application

Login ล้มเหลวเมื่อ username ว่าง
    [Setup]    เปิดแอปในสถานะ fresh
    กรอก Password    SomePass123
    กด Login
    ตรวจสอบ Error Message ว่าคือ    Username is required
    [Teardown]    Close Application
```

### Common Keywords Resource

```robotframework
# resources/common_keywords.robot

*** Settings ***
Library      AppiumLibrary
Variables    variables.robot

*** Keywords ***
เปิดแอปในสถานะ fresh
    Open Application    ${APPIUM_URL}
    ...    platformName=${PLATFORM_NAME}
    ...    appium:automationName=${AUTOMATION_NAME}
    ...    appium:deviceName=${DEVICE_NAME}
    ...    appium:appPackage=${APP_PACKAGE}
    ...    appium:appActivity=${APP_ACTIVITY}
    ...    appium:noReset=${False}

เปิดแอปพร้อม session เดิม
    Open Application    ${APPIUM_URL}
    ...    platformName=${PLATFORM_NAME}
    ...    appium:automationName=${AUTOMATION_NAME}
    ...    appium:deviceName=${DEVICE_NAME}
    ...    appium:appPackage=${APP_PACKAGE}
    ...    appium:appActivity=${APP_ACTIVITY}
    ...    appium:noReset=${True}
```

---

## ตัวอย่าง 3 ระดับ

### Beginner: แยก locators ออกจาก test case

ก่อน (ไม่มี Page Object — locator กระจาย):
```robotframework
*** Test Cases ***
Test Login
    Click Element    accessibility_id=username_input
    Input Text    accessibility_id=username_input    user@email.com
    Input Text    accessibility_id=password_input    pass123
    Click Element    accessibility_id=login_button
```

หลัง (มี Page Object — อ่านง่าย maintain ง่าย):
```robotframework
*** Test Cases ***
Test Login
    ทำการ Login ด้วย    user@email.com    pass123
```

เวลาปุ่ม Login เปลี่ยน locator → แก้แค่ที่ `login_page.robot` ที่เดียว

### Intermediate: Transfer flow ที่ต้องผ่านหลาย screen

```robotframework
# pages/transfer_page.robot

*** Settings ***
Library    AppiumLibrary

*** Variables ***
${TRANSFER_RECIPIENT_FIELD}    accessibility_id=recipient_phone
${TRANSFER_AMOUNT_FIELD}       accessibility_id=transfer_amount
${TRANSFER_CONFIRM_BTN}        accessibility_id=confirm_transfer
${TRANSFER_SUCCESS_SCREEN}     accessibility_id=transfer_success
${TRANSFER_BACK_BTN}           accessibility_id=back_button

*** Keywords ***
กรอก Recipient
    [Arguments]    ${phone}
    Input Text    ${TRANSFER_RECIPIENT_FIELD}    ${phone}
    Hide Keyboard

กรอก Amount
    [Arguments]    ${amount}
    Input Text    ${TRANSFER_AMOUNT_FIELD}    ${amount}
    Hide Keyboard

ยืนยันการโอน
    Click Element    ${TRANSFER_CONFIRM_BTN}

ตรวจสอบโอนสำเร็จ
    Wait Until Element Is Visible    ${TRANSFER_SUCCESS_SCREEN}    15s
    Page Should Contain Element    ${TRANSFER_SUCCESS_SCREEN}
```

```robotframework
# tests/transfer_test.robot

*** Settings ***
Library      AppiumLibrary
Resource     ../pages/login_page.robot
Resource     ../pages/home_page.robot
Resource     ../pages/transfer_page.robot
Resource     ../resources/common_keywords.robot

*** Test Cases ***
โอนเงินสำเร็จ
    [Setup]    เปิดแอปพร้อม session เดิม
    รอให้ Home Screen โหลดครบ
    ไปหน้า Transfer
    กรอก Recipient    0812345678
    กรอก Amount       500
    ยืนยันการโอน
    ตรวจสอบโอนสำเร็จ
    [Teardown]    Close Application
```

### Advanced: Base Keyword สำหรับ scroll หา element

```robotframework
# resources/common_keywords.robot (เพิ่มเติม)

*** Keywords ***
Scroll จนเจอ Element
    [Arguments]    ${locator}    ${max_swipes}=5
    FOR    ${i}    IN RANGE    ${max_swipes}
        ${found}=    Run Keyword And Return Status
        ...    Element Should Be Visible    ${locator}
        IF    ${found}    RETURN
        Swipe    540    1400    540    600    800
    END
    Fail    ไม่พบ element '${locator}' หลัง scroll ${max_swipes} ครั้ง

รัน Test Suite ทั้งหมด ด้วย setup ครั้งเดียว
    # Pattern: Suite Setup เปิด app ครั้งเดียว, ทุก test case ใช้ session เดิม
    # ประหยัดเวลา setup มาก สำหรับ test suite ขนาดใหญ่
    เปิดแอปพร้อม session เดิม
```

---

## Common Mistakes

❌ **ใส่ locator ใน test case โดยตรง**
```robotframework
# ❌ เมื่อ locator เปลี่ยนต้องตามแก้ทุกที่
Click Element    accessibility_id=login_button
```
✅ **ใส่ locator เป็น variable ใน page file แยก**
```robotframework
# ✅ แก้ที่เดียวใน page file
Click Element    ${LOGIN_BUTTON}
```
*(source: Robot Framework best practice)*

---

❌ **ใส่ capabilities ใน test case โดยตรง**
→ ถ้า package name เปลี่ยนต้องตามแก้ทุก test file
✅ **แยก capabilities ไปไว้ใน variables.robot**
*(source: Robot Framework best practice)*

---

❌ **Page Object file มี logic ของ test อยู่ด้วย**
→ ทำให้ keyword ไม่ reusable
✅ **Page Object file มีแค่ locators และ low-level actions** — test logic อยู่ใน test file
*(source: Page Object pattern best practice)*

---

## สรุปบท

ลองตอบก่อนดูเฉลย:

> **คำถาม 1:** ถ้า developer เปลี่ยน `accessibility_id=login_button` เป็น `id=com.app:id/btn_login` ในโปรเจคที่ใช้ Page Object — คุณต้องแก้ไขกี่ไฟล์?

> **คำถาม 2:** `pages/login_page.robot` กับ `tests/login_test.robot` ต่างกันยังไง? แต่ละไฟล์ควรมีอะไรบ้าง?

> **คำถาม 3:** ทำไม keyword `ทำการ Login ด้วย` ใน page file ถึง reusable ได้ข้ามหลาย test case? ข้อดีนี้มาจากอะไร?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย 1:** แก้แค่ 1 ไฟล์ คือ `pages/login_page.robot` — เปลี่ยน variable `${LOGIN_BUTTON}` ที่เดียว test ทุก case ที่ใช้ keyword นั้นก็ work ทันที

**เฉลย 2:** `login_page.robot` = locators (variables) + low-level actions (keywords ที่ทำสิ่งเดียว เช่น กรอก field, คลิกปุ่ม) / `login_test.robot` = test cases ที่รวม keywords จาก page เป็น flow ที่ทดสอบ scenario จริง

**เฉลย 3:** เพราะ keyword รับ `[Arguments]` ทำให้ใช้ข้อมูลต่างกันได้ในแต่ละ test case และ locator อยู่ใน page file ไม่ใช่ใน test — ทำให้ test case อ่านง่ายและ decoupled จาก UI details

</details>

---

**ยินดีด้วย! คุณอ่านครบทุกบทของ Series 1 แล้ว**

ขั้นตอนต่อไป:
- ทำ [Exercises](exercises.md) เพื่อทบทวนและฝึกทักษะ
- อ่าน [Glossary](glossary.md) เพื่อทบทวนคำศัพท์
- ถ้าพร้อม ต่อที่ Series 2: [WebdriverIO + Appium Mobile Testing](../wdio-mobile-testing/00-overview.md)
