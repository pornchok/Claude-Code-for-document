# บทที่ 5: Web Testing ด้วย Browser Library

## ก่อนอ่านบทนี้ ลองตอบ:

- ทำไมต้องแยก keyword ออกเป็น resource file?
- Keyword ที่ return ค่ากลับมาต้องเขียนยังไง?

---

ลองตอบก่อน แล้วค่อยอ่านต่อ

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:
- ใช้ Browser Library เปิด browser และ navigate ได้
- หา element ด้วย locator strategy ที่ถูกต้อง (id, css, xpath)
- คลิก, พิมพ์, เลือก dropdown, และ verify ค่าได้
- เขียน explicit wait แทน `Sleep` ที่ brittle

---

## ทำไมต้องรู้? (Why)

Web testing คือ use case หลักของ RF สำหรับ QA ทุกคน

การทดสอบ web ด้วยมือ manual นั้น:
- ซ้ำซากและเสียเวลา
- มีโอกาสผิดพลาดสูงเมื่อทำซ้ำ
- ไม่ scale เมื่อ feature เพิ่มขึ้น

Browser Library (Playwright) แก้ปัญหาเหล่านี้ได้ และยังรองรับ modern web app ที่ใช้ React, Vue, Angular ได้ดีกว่า Selenium มาก

---

## Analogy: Browser Library เหมือน "ผู้ช่วย QA เสมือน"

จินตนาการว่าคุณมีผู้ช่วย QA ที่:
- **เปิด browser** และไปหน้าที่กำหนดให้
- **มองหา element** บน screen ตาม locator ที่บอก
- **ทำ action** เช่น คลิก, พิมพ์, scroll
- **ตรวจสอบผล** ว่าเป็นตามที่คาดหวัง
- **รอ** จนกว่า element จะพร้อม ไม่รีบเกินไป

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
- ผู้ช่วยรู้จัก element เองเหมือนคนจริง — จริงๆ ต้องบอก locator ที่แม่นยำ ถ้า locator เปลี่ยนต้องอัปเดต keyword
- "รอ" หมายความว่ารอแบบ Sleep — จริงๆ Browser Library รอ state ของ element (visible, enabled, stable) ซึ่งฉลาดกว่าการรอเวลาตายตัว

---

## Locator Strategies

Locator คือวิธีบอกให้ RF หา element บนหน้า web

### Priority ในการเลือก Locator (จากดีที่สุดไปแย่ที่สุด)

```
1. id        ← เฉพาะที่สุด เปลี่ยนน้อยที่สุด  ✓✓✓
2. data-testid ← สร้างมาเพื่อ test โดยเฉพาะ    ✓✓✓
3. css       ← flexible ใช้ได้กว้าง            ✓✓
4. text      ← อ่านง่าย แต่เปลี่ยนตาม i18n    ✓
5. xpath     ← ใช้เมื่อไม่มีทางอื่น           ✓ (brittle)
```

### ตัวอย่าง Locator Syntax

```robot
# By id
Click    id:login-btn
Fill Text    id:username    testuser

# By CSS selector
Click    css:button.primary
Fill Text    css:input[name='email']    test@example.com
Click    css:.nav-menu > li:first-child

# By data-testid (แนะนำมากถ้า dev สร้างไว้ให้)
Click    data-testid=submit-button
Fill Text    data-testid=email-input    user@example.com

# By text content
Click    text=Login
Click    text=ยืนยัน

# By XPath (ใช้เฉพาะกรณีจำเป็น)
Click    xpath=//button[@type='submit']
Fill Text    xpath=//input[@placeholder='Email address']    test@example.com
```

---

## Keywords หลักของ Browser Library

### เปิด/ปิด Browser

```robot
New Browser    chromium    headless=False
New Page       https://example.com
Close Browser
```

### Navigate

```robot
Go To    https://example.com/login
Go Back
Reload
```

### Fill และ Click

```robot
Fill Text    id:username    testuser
Fill Text    id:password    secretpass
Click        id:login-btn
```

### Verify

```robot
# ตรวจสอบ text บนหน้า
Get Text    id:welcome-msg    ==    Welcome, testuser

# ตรวจสอบว่า element มี/ไม่มี
Wait For Elements State    id:error-msg    visible
Wait For Elements State    id:spinner      hidden

# ตรวจสอบ title
Get Title    ==    Dashboard - MyApp

# ตรวจสอบ URL
Get Url    contains    /dashboard
```

### Wait (สำคัญมาก!)

```robot
# รอจนกว่า element จะ visible
Wait For Elements State    id:result    visible

# รอจนกว่าจะ enabled (กดได้)
Wait For Elements State    id:submit-btn    enabled

# รอจนกว่าจะ hidden (loading หาย)
Wait For Elements State    id:loading    hidden

# รอจนกว่า network request จะเสร็จ
Wait For Navigation
```

---

## ตัวอย่าง 3 ระดับ

### Beginner — เปิด browser และตรวจสอบหน้าเว็บ

```robot
# tested: Robot Framework 7.4.2, Browser Library 19.x
# หมายเหตุ: ต้องติดตั้ง Browser Library และรัน rfbrowser init ก่อน
*** Settings ***
Library    Browser

*** Test Cases ***
Open Example Website And Verify Title
    [Documentation]    เปิด browser และตรวจสอบ title ของหน้า
    New Browser    chromium    headless=True
    New Page       https://example.com
    Get Title    ==    Example Domain
    Get Text     css:h1    ==    Example Domain
    [Teardown]    Close Browser
```

### Intermediate — Login flow สมบูรณ์

```robot
# tested: Robot Framework 7.4.2, Browser Library 19.x
# ใช้ demo app: https://the-internet.herokuapp.com/login
*** Settings ***
Library    Browser

*** Variables ***
${BASE_URL}       https://the-internet.herokuapp.com
${VALID_USER}     tomsmith
${VALID_PASS}     SuperSecretPassword!

*** Test Cases ***
Valid Login Shows Success Message
    [Documentation]    Login ด้วย credential ที่ถูกต้องและตรวจสอบ success message
    [Tags]    login    smoke
    Open Login Page
    Enter Credentials    ${VALID_USER}    ${VALID_PASS}
    Login Success Should Be Shown
    [Teardown]    Close Browser

Invalid Login Shows Error Message
    [Documentation]    Login ด้วย password ผิดและตรวจสอบ error message
    [Tags]    login    negative
    Open Login Page
    Enter Credentials    ${VALID_USER}    wrongpassword
    Login Error Should Be Shown    Your password is invalid!
    [Teardown]    Close Browser

*** Keywords ***
Open Login Page
    New Browser    chromium    headless=True
    New Page       ${BASE_URL}/login
    Wait For Elements State    id:username    visible

Enter Credentials
    [Arguments]    ${username}    ${password}
    Fill Text    id:username    ${username}
    Fill Text    id:password    ${password}
    Click        css:button[type='submit']

Login Success Should Be Shown
    Wait For Elements State    css:.flash.success    visible
    Get Text    css:.flash.success    contains    You logged into a secure area

Login Error Should Be Shown
    [Arguments]    ${expected_message}
    Wait For Elements State    css:.flash.error    visible
    Get Text    css:.flash.error    contains    ${expected_message}
```

### Advanced — Test ที่จัดการ dynamic content

```robot
# tested: Robot Framework 7.4.2, Browser Library 19.x
# ใช้ demo app: https://the-internet.herokuapp.com
*** Settings ***
Library    Browser
Library    Collections

*** Variables ***
${BASE_URL}    https://the-internet.herokuapp.com

*** Test Cases ***
Dynamic Content Loads Correctly
    [Documentation]    ตรวจสอบว่า dynamic content โหลดมาถูกต้อง
    [Tags]    dynamic    intermediate
    New Browser    chromium    headless=True
    New Page       ${BASE_URL}/dynamic_content
    Wait For Elements State    css:.large-4    visible
    ${items}=    Get Elements    css:.large-4
    Length Should Be    ${items}    3
    [Teardown]    Close Browser

Disappearing Elements Test
    [Documentation]    ทดสอบหน้าที่ element หายไปและกลับมา
    [Tags]    dynamic    advanced
    New Browser    chromium    headless=True
    New Page       ${BASE_URL}/disappearing_elements
    ${nav_items}=    Get Elements    css:ul li
    ${count}=    Get Length    ${nav_items}
    Should Be True    ${count} >= 4
    Log    พบ navigation items: ${count} รายการ
    [Teardown]    Close Browser

*** Keywords ***
Wait For Page To Fully Load
    Wait For Elements State    css:body    visible
    Sleep    0.5s    # minimal wait สำหรับ animation
```

---

## Sleep vs Wait — ทำไม Sleep เป็น bad practice

```robot
# ❌ แบบผิด — brittle และช้า
Login Test
    Click    id:login-btn
    Sleep    3 seconds         # รอแบบตายตัว ถ้า network ช้า = fail
    Page Should Contain    Welcome

# ✅ แบบถูก — รอแบบ smart
Login Test
    Click    id:login-btn
    Wait For Elements State    id:welcome-banner    visible    timeout=10s
    Get Text    id:welcome-banner    contains    Welcome
```

**เหตุผล:**
- `Sleep` ทำให้ test ช้า — รอ 3 วินาทีทุกครั้ง แม้ page load ใน 0.5 วินาที
- `Sleep` ทำให้ test brittle — ถ้า server ช้าวันไหน 3 วินาทีไม่พอ fail เลย
- `Wait For Elements State` รอแบบ adaptive — ได้แล้วรัน, timeout แล้วค่อย fail พร้อม error ที่ชัดเจน

*(source: [Browser Library Best Practices](https://marketsquare.github.io/robotframework-browser/Browser.html))*

---

## Common Mistakes

❌ **ใช้ XPath ที่ขึ้นกับ position เช่น `//div[3]/span[2]`**
```robot
# ผิด — เปราะบาก เปลี่ยน layout = fail
Click    xpath=//div[3]/span[2]/button

# ถูก — ใช้ attribute ที่มีความหมาย
Click    css:button[data-action='checkout']
# หรือ
Click    id:checkout-btn
```
*(source: [RF Best Practices - Locators](https://docs.robotframework.org/docs/style_guide))*

❌ **ไม่ปิด Browser ใน Teardown**
```robot
# ผิด — ถ้า test fail browser ค้างอยู่
My Test
    New Browser    chromium
    New Page    ${URL}
    # ... test ...
    Close Browser    ← ถ้า test fail บรรทัดนี้ไม่รัน

# ถูก — ใช้ Teardown รับประกันว่าจะรันเสมอ
My Test
    New Browser    chromium
    New Page    ${URL}
    # ... test ...
    [Teardown]    Close Browser
```
*(source: [RF User Guide - Teardown](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#suite-teardown))*

❌ **Hardcode locator ซ้ำหลายที่**
```robot
# ผิด — ถ้า id เปลี่ยนต้องแก้หลายที่
Test 1
    Click    id:add-to-cart-button
Test 2
    Wait For Elements State    id:add-to-cart-button    visible

# ถูก — ใช้ variable
*** Variables ***
${ADD_TO_CART}    id:add-to-cart-button

Test 1
    Click    ${ADD_TO_CART}
Test 2
    Wait For Elements State    ${ADD_TO_CART}    visible
```
*(source: [RF Style Guide - Variables](https://docs.robotframework.org/docs/style_guide))*

---

## สรุปบท

ก่อนไปบทต่อไป ลองตอบ (คิด 30 วินาทีก่อนดูเฉลย):

**คำถาม 1:** ถ้าหน้า web มีปุ่ม Login ที่มี `id="btn-login"` และ `class="btn-primary"` คุณจะใช้ locator แบบไหน? เพราะอะไร?

**คำถาม 2:** ความแตกต่างระหว่าง `Sleep    3 seconds` กับ `Wait For Elements State    id:result    visible` คืออะไร? อันไหนดีกว่าในสถานการณ์ใด?

**คำถาม 3:** ทำไม `[Teardown] Close Browser` ถึงดีกว่าการใส่ `Close Browser` ไว้ท้าย test case?

---

<details>
<summary>เฉลย (คลิกเพื่อดู — ดูหลังจากลองตอบแล้วเท่านั้น)</summary>

**เฉลย 1:** ใช้ `id:btn-login` ดีที่สุด เพราะ id เฉพาะที่สุดในหน้า (ควรมีแค่ element เดียวต่อ id), เปลี่ยนน้อยกว่า class ที่มักเปลี่ยนตาม styling

**เฉลย 2:** `Sleep` รอเวลาตายตัว — ถ้า page load ใน 0.5s ก็ยังรออีก 2.5s โดยเปล่าประโยชน์ และถ้า server ช้าจน 3s ไม่พอก็ fail `Wait For Elements State` รอจน element เป็นสถานะที่กำหนด — ได้แล้วรันต่อทันที, timeout ค่อย fail ดีกว่าเสมอ ยกเว้นกรณีที่ต้องรอ animation พิเศษที่ไม่มี state ให้ detect

**เฉลย 3:** `[Teardown]` รันเสมอแม้ test case fail ถ้าใส่ `Close Browser` ไว้ท้ายแต่ test fail กลางทาง browser จะค้างอยู่ไม่ปิด ทำให้ test run ต่อไปช้าลงหรือ crash

</details>
