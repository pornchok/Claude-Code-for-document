# บทที่ 4: Keywords และ Resource Files

## ก่อนอ่านบทนี้ ลองตอบ:

- `.robot` ไฟล์มี section อะไรบ้าง?
- `${var}`, `@{list}`, `&{dict}` ต่างกันยังไง ใช้ตอนไหน?

---

ลองตอบก่อน แล้วค่อยอ่านต่อ

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:
- เขียน custom keyword พร้อม arguments และ return value ได้
- แยก keyword ออกไปใน `.resource` file และ import มาใช้
- เข้าใจ keyword scope — keyword ที่ไหน เรียกได้ที่ไหน
- ออกแบบ keyword ที่ reusable และ maintainable

---

## ทำไมต้องรู้? (Why)

ลองนึกภาพนี้: ทีมคุณมี test file 20 ไฟล์ ทุกไฟล์มี keyword `Open Login Page` ที่เขียนซ้ำกัน

วันนึง design ของหน้า login เปลี่ยน — button เปลี่ยน locator

ถ้าไม่ใช้ resource file: แก้ 20 ไฟล์
ถ้าใช้ resource file: แก้ 1 ที่

นี่คือสาเหตุว่าทำไม **Keywords + Resource Files** เป็น core concept ที่สำคัญที่สุดใน RF

---

## Analogy: Keyword เหมือน Function ใน Code ทั่วไป

ถ้าคุณเคยเขียน function มาแล้ว keyword ก็คือ function นั่นเอง:

```python
# Python function
def enter_credentials(username, password):
    fill_field("username", username)
    fill_field("password", password)
    click_button("login")
```

```robot
# RF Keyword
Enter Credentials
    [Arguments]    ${username}    ${password}
    Fill Text    id:username    ${username}
    Fill Text    id:password    ${password}
    Click         id:login-btn
```

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
- Keyword เหมือน function ที่ต้องเรียกด้วย parentheses เช่น `Enter Credentials()` — จริงๆ RF ไม่ใช้ `()` เรียก keyword ด้วยชื่อ + arguments แยกด้วย 4 spaces
- Keyword มี scope เหมือน Python function — จริงๆ Keyword ใน RF ไม่มี `local`/`global` scope แบบเดียวกัน มี keyword scope ที่แตกต่างออกไป
- Return value ใช้ `return` เหมือน Python — จริงๆ RF 7.x ใช้ `RETURN` (uppercase) หรือ `[Return]` ขึ้นกับ context

---

## การเขียน Keyword

### Structure พื้นฐาน

```robot
*** Keywords ***
Keyword Name
    [Documentation]    อธิบายว่า keyword นี้ทำอะไร
    [Arguments]    ${arg1}    ${arg2}    ${arg3}=default_value
    # steps ที่ทำ
    Log    ทำงานกับ: ${arg1}
    RETURN    result_value
```

### Keyword ที่ไม่มี argument

```robot
Open Application
    [Documentation]    เปิด browser และไปหน้าแรก
    New Browser    chromium    headless=False
    New Page       ${BASE_URL}
    Wait For Elements State    id:main-content    visible
```

### Keyword ที่มี argument

```robot
Login As
    [Documentation]    Login ด้วย username และ password ที่กำหนด
    [Arguments]    ${username}    ${password}
    Fill Text    id:username    ${username}
    Fill Text    id:password    ${password}
    Click        id:login-btn
    Wait For Elements State    id:dashboard    visible

# เรียกใช้
Login As    admin    admin123
Login As    testuser    pass@123
```

### Keyword ที่มี default argument

```robot
Login As
    [Arguments]    ${username}    ${password}    ${timeout}=10s
    Fill Text    id:username    ${username}
    Fill Text    id:password    ${password}
    Click        id:login-btn
    Wait For Elements State    id:dashboard    visible    timeout=${timeout}

# เรียกแบบไม่ส่ง timeout (ใช้ default 10s)
Login As    admin    admin123

# เรียกแบบกำหนด timeout เอง
Login As    admin    admin123    timeout=30s
```

### Keyword ที่ return ค่า

```robot
Get Current User Role
    [Documentation]    ดึง role ของ user ที่ login อยู่
    ${role_text}=    Get Text    id:user-role-badge
    RETURN    ${role_text}

# เรียกใช้
${current_role}=    Get Current User Role
Should Be Equal    ${current_role}    Admin
```

---

## Resource Files

Resource file คือไฟล์ที่เก็บ keyword และ variable รวมกัน เพื่อ share ระหว่าง test file หลายๆ อัน

### สร้าง Resource File

ชื่อไฟล์ใช้ `.resource` extension (แนะนำ) หรือ `.robot` ก็ได้

```robot
# resources/login.resource
*** Settings ***
Documentation    Keywords สำหรับ login workflow
Library          Browser

*** Variables ***
${LOGIN_URL}    https://example.com/login
${TIMEOUT}      10 seconds

*** Keywords ***
Go To Login Page
    [Documentation]    เปิด browser และไปหน้า login
    Go To    ${LOGIN_URL}
    Wait For Elements State    id:username    visible

Login As
    [Documentation]    Login ด้วย credentials ที่กำหนด
    [Arguments]    ${username}    ${password}
    Fill Text    id:username    ${username}
    Fill Text    id:password    ${password}
    Click        id:login-btn
    Wait For Elements State    id:dashboard    visible

Logout
    [Documentation]    Logout และกลับหน้า login
    Click    id:logout-btn
    Wait For Elements State    id:username    visible
```

### Import Resource File

```robot
# tests/login_tests.robot
*** Settings ***
Library      Browser
Resource     ../resources/login.resource

*** Test Cases ***
User Can Login With Valid Credentials
    Go To Login Page
    Login As    admin    admin123
    Page Should Contain    Welcome, admin

User Can Logout After Login
    Go To Login Page
    Login As    admin    admin123
    Logout
    Page Should Contain    Login
```

---

## โครงสร้าง Resource Files ที่ดี

### ใช้หลัก "1 Feature = 1 Resource File"

```
resources/
├── common.resource          ← keywords ที่ใช้ทุกที่ (เปิด/ปิด browser, screenshot)
├── login.resource           ← keywords ทุกอย่างที่เกี่ยวกับ login
├── navigation.resource      ← keywords สำหรับเนวิเกต
├── user_management.resource ← keywords สำหรับ user management
└── checkout.resource        ← keywords สำหรับ checkout flow
```

### common.resource มักมีอะไร?

```robot
# resources/common.resource
*** Settings ***
Library    Browser

*** Variables ***
${BASE_URL}    https://example.com
${BROWSER}     chromium
${HEADLESS}    ${False}

*** Keywords ***
Open Browser Session
    [Documentation]    เปิด browser ครั้งแรก (ใช้ใน Suite Setup)
    New Browser    ${BROWSER}    headless=${HEADLESS}

Close Browser Session
    [Documentation]    ปิด browser (ใช้ใน Suite Teardown)
    Close Browser

Take Screenshot On Failure
    [Documentation]    ถ่าย screenshot เมื่อ test fail
    Take Screenshot    fullPage=True
```

---

## ตัวอย่าง 3 ระดับ

### Beginner — เขียน keyword พร้อม arguments

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Library    Collections

*** Test Cases ***
Test Keyword With Arguments
    ${result}=    Calculate Discount    1000    10
    Should Be Equal As Numbers    ${result}    900

    ${result2}=    Calculate Discount    500    20
    Should Be Equal As Numbers    ${result2}    400

    ${result3}=    Calculate Discount    200
    Should Be Equal As Numbers    ${result3}    190

*** Keywords ***
Calculate Discount
    [Documentation]    คำนวณราคาหลังหักส่วนลด
    [Arguments]    ${price}    ${discount_percent}=5
    ${discount}=    Evaluate    ${price} * ${discount_percent} / 100
    ${final_price}=    Evaluate    ${price} - ${discount}
    RETURN    ${final_price}
```

Output ที่ควรได้:
```
Test Keyword With Arguments                                           | PASS |
1 test, 1 passed, 0 failed
```

### Intermediate — Resource file แยก feature

สร้าง 2 ไฟล์:

**`resources/cart.resource`:**
```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Documentation    Keywords สำหรับจัดการ cart
Library          Collections

*** Variables ***
${TAX_RATE}    0.07

*** Keywords ***
Calculate Cart Total
    [Documentation]    คำนวณยอดรวมของ item ใน cart
    [Arguments]    @{prices}
    ${total}=    Evaluate    sum(${prices})
    RETURN    ${total}

Calculate Total With Tax
    [Documentation]    คำนวณยอดรวมบวก tax
    [Arguments]    ${subtotal}
    ${tax}=      Evaluate    ${subtotal} * ${TAX_RATE}
    ${total}=    Evaluate    ${subtotal} + ${tax}
    RETURN    ${total}

Cart Should Have Correct Total
    [Documentation]    ตรวจสอบว่า cart total ถูกต้อง
    [Arguments]    ${expected}    @{prices}
    ${actual}=    Calculate Cart Total    @{prices}
    Should Be Equal As Numbers    ${actual}    ${expected}
```

**`tests/cart_tests.robot`:**
```robot
*** Settings ***
Documentation    Test ระบบ Cart
Library          Collections
Resource         resources/cart.resource

*** Test Cases ***
Cart Total Is Sum Of All Items
    Cart Should Have Correct Total    300    100    150    50

Total With Tax Should Include 7 Percent
    ${subtotal}=    Set Variable    ${1000}
    ${total_with_tax}=    Calculate Total With Tax    ${subtotal}
    Should Be Equal As Numbers    ${total_with_tax}    1070
```

### Advanced — Keyword ที่มี setup/teardown และ error handling

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Library    Collections
Library    OperatingSystem

*** Test Cases ***
Test User Registration Flow
    [Setup]    Initialize User Storage
    Register User    alice    alice@example.com
    Register User    bob      bob@example.com
    User Count Should Be    2
    User Should Exist    alice
    User Should Exist    bob
    [Teardown]    Clear User Storage

*** Keywords ***
Initialize User Storage
    ${storage}=    Create Dictionary
    Set Suite Variable    ${USER_STORAGE}    ${storage}

Register User
    [Arguments]    ${username}    ${email}
    Set To Dictionary    ${USER_STORAGE}    ${username}=${email}
    Log    ✓ Registered: ${username} (${email})

User Count Should Be
    [Arguments]    ${expected_count}
    ${count}=    Get Length    ${USER_STORAGE}
    Should Be Equal As Numbers    ${count}    ${expected_count}

User Should Exist
    [Arguments]    ${username}
    Dictionary Should Contain Key    ${USER_STORAGE}    ${username}
    Log    ✓ User '${username}' found

Clear User Storage
    ${empty}=    Create Dictionary
    Set Suite Variable    ${USER_STORAGE}    ${empty}
    Log    Storage cleared
```

Output ที่ควรได้:
```
Test User Registration Flow                                           | PASS |
1 test, 1 passed, 0 failed
```

---

## Common Mistakes

❌ **เขียน keyword ชื่อสั้นเกิน ไม่สื่อความหมาย**
```robot
# ผิด — ไม่รู้ว่า keyword นี้ click ที่ไหน
Click And Wait

# ถูก — ชัดเจน บอก context
Click Login Button And Wait For Dashboard
```
*(source: [RF Style Guide - Naming](https://docs.robotframework.org/docs/style_guide#naming-conventions))*

❌ **ใส่ implementation detail ไว้ใน test case**
```robot
# ผิด — test case รู้มากเกินไป
Checkout Test
    Click    css:button.add-to-cart
    Wait For Elements State    id:cart-count    visible
    Fill Text    id:qty    2
    Click    css:button[data-action='checkout']

# ถูก — test case อ่านออกว่า test อะไร
Checkout Test
    Add Item To Cart    product=laptop    quantity=2
    Proceed To Checkout
    Checkout Page Should Be Open
```
*(source: [RF Best Practices - Keyword Abstraction](https://docs.robotframework.org/docs/style_guide))*

❌ **วาง keyword ทั้งหมดไว้ใน test file แทน resource file**
```robot
# ผิด — keyword อยู่ใน test file
# tests/login_test.robot
*** Keywords ***
Login As    # ← keyword ที่ test file อื่นก็ต้องการ

# ถูก — keyword อยู่ใน resource file
# resources/login.resource
*** Keywords ***
Login As    # ← import จาก test file อื่นได้
```
*(source: [RF User Guide - Resource Files](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#resource-files))*

---

## สรุปบท

ก่อนไปบทต่อไป ลองตอบ (คิด 30 วินาทีก่อนดูเฉลย):

**คำถาม 1:** Keyword กับ Test Case ต่างกันอย่างไรในแง่ของ "what vs how"?

**คำถาม 2:** ทำไม Resource File ถึงช่วยลดความซ้ำซ้อนได้? ยกตัวอย่างสถานการณ์จริงประกอบ

**คำถาม 3:** ถ้า keyword `Login As` มีอยู่ทั้งใน `common.resource` และ `login.resource` และ test file import ทั้งสองอัน จะเกิดอะไรขึ้น?

---

<details>
<summary>เฉลย (คลิกเพื่อดู — ดูหลังจากลองตอบแล้วเท่านั้น)</summary>

**เฉลย 1:** Test Case บอกว่า "test อะไร" (ระดับ scenario) เช่น "User Can Login" ไม่สนใจว่า button locator คือ `id:login-btn` หรือ `css:button.login-btn` Keyword คือ "ทำยังไง" ซ่อน implementation ไว้ข้างใน ถ้า locator เปลี่ยนแก้แค่ keyword เดียว

**เฉลย 2:** Resource file เก็บ keyword รวมกัน test file หลายๆ อันใช้ร่วมกันได้ เช่น โปรเจคมี login.resource ที่มี keyword `Login As` — test file สำหรับ checkout, profile, settings ทุกอันก็ import keyword นี้ได้ ถ้า login flow เปลี่ยนแก้แค่ที่ resource file เดียว

**เฉลย 3:** RF จะ error "Multiple keywords with name 'Login As' found" ต้องใช้ full name เช่น `common.Login As` หรือ `login.Login As` เพื่อ disambiguate

</details>
