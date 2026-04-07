# แบบฝึกหัด Robot Framework

แบบฝึกหัดแบ่งตาม cognitive level: **Recall → Application → Synthesis**

ทำแต่ละข้อด้วยตัวเอง รัน test จนผ่านก่อนดูเฉลย

---

## ส่วนที่ 1: Recall — เข้าใจ concept (บทที่ 1-3)

### Exercise 1.1 — อธิบายด้วยคำตัวเอง

ลอง explain ให้คนที่ไม่รู้จัก RF เข้าใจ:
1. "keyword-driven testing" คืออะไร? ทำไมมันต่างจากการเขียน script ธรรมดา?
2. ทำไม RF ถึงเหมาะสำหรับ QA manual มากกว่า pytest หรือ Selenium ตรงๆ?

---

### Exercise 1.2 — เขียน Variable ทั้ง 3 แบบ

เขียนไฟล์ `ex1_variables.robot` ที่มี:
- `${scalar}` เก็บชื่อ product
- `@{list}` เก็บ size ที่มี (S, M, L, XL)
- `&{dict}` เก็บราคาของแต่ละ size (S=299, M=349, L=399, XL=449)
- Test case ที่ตรวจสอบว่า List มี 4 items และ dict มี key `XL`

<details>
<summary>เฉลย Exercise 1.2</summary>

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Library    Collections

*** Variables ***
${PRODUCT_NAME}    T-Shirt Collection 2025
@{AVAILABLE_SIZES}    S    M    L    XL
&{SIZE_PRICES}    S=299    M=349    L=399    XL=449

*** Test Cases ***
Verify Product Variables Are Set Correctly
    [Documentation]    ตรวจสอบว่า variable ทุกแบบถูกกำหนดค่าถูกต้อง
    Should Not Be Empty    ${PRODUCT_NAME}
    Length Should Be    ${AVAILABLE_SIZES}    4
    Should Contain    ${AVAILABLE_SIZES}    XL
    Dictionary Should Contain Key    ${SIZE_PRICES}    XL
    Should Be Equal    ${SIZE_PRICES}[XL]    449
    Log    ✓ Product: ${PRODUCT_NAME}
    Log    ✓ Sizes: ${AVAILABLE_SIZES}
    Log    ✓ XL Price: ${SIZE_PRICES}[XL]
```

</details>

---

## ส่วนที่ 2: Application — ใช้ใน context ใหม่ (บทที่ 4-6)

### Exercise 2.1 — เขียน Resource File จริง

ทีม QA ของบริษัท e-commerce กำลัง test ระบบสมัครสมาชิก สร้าง 2 ไฟล์:

**`resources/registration.resource`** ที่มี keyword:
- `Fill Registration Form` — รับ name, email, password เป็น argument
- `Registration Should Succeed` — ตรวจสอบ success state
- `Registration Should Fail With` — รับ expected_error เป็น argument

**`tests/registration_test.robot`** ที่มี test case:
- "New User Can Register With Valid Data"
- "Registration Fails With Existing Email"

(ไม่ต้องใช้ browser จริง — จำลอง state ด้วย Dictionary หรือ Variable)

<details>
<summary>เฉลย Exercise 2.1</summary>

```robot
# resources/registration.resource
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Library    Collections

*** Variables ***
# จำลอง registered users database
@{REGISTERED_EMAILS}    existing@example.com    taken@example.com

*** Keywords ***
Fill Registration Form
    [Documentation]    กรอกข้อมูลสมัครสมาชิก
    [Arguments]    ${name}    ${email}    ${password}
    Should Not Be Empty    ${name}
    Should Match Regexp    ${email}    ^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$
    ${length}=    Get Length    ${password}
    Should Be True    ${length} >= 8
    ${is_taken}=    Run Keyword And Return Status
    ...    Should Contain    ${REGISTERED_EMAILS}    ${email}
    Set Test Variable    ${LAST_REGISTRATION_STATUS}
    ...    ${{ 'fail_duplicate' if ${is_taken} else 'success' }}
    Set Test Variable    ${LAST_REGISTERED_EMAIL}    ${email}

Registration Should Succeed
    [Documentation]    ตรวจสอบว่าสมัครสมาชิกสำเร็จ
    Should Be Equal    ${LAST_REGISTRATION_STATUS}    success
    Log    ✓ Registration successful for: ${LAST_REGISTERED_EMAIL}

Registration Should Fail With
    [Documentation]    ตรวจสอบ error message ที่ควรได้
    [Arguments]    ${expected_error}
    Should Be Equal    ${LAST_REGISTRATION_STATUS}    fail_${expected_error}
    Log    ✓ Registration failed as expected: ${expected_error}
```

```robot
# tests/registration_test.robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Library     Collections
Resource    resources/registration.resource

*** Test Cases ***
New User Can Register With Valid Data
    [Documentation]    User ใหม่สมัครได้ด้วยข้อมูลที่ถูกต้อง
    [Tags]    registration    smoke
    Fill Registration Form    John Doe    john.new@example.com    SecurePass123
    Registration Should Succeed

Registration Fails With Existing Email
    [Documentation]    สมัครด้วย email ที่มีอยู่แล้วต้องล้มเหลว
    [Tags]    registration    negative
    Fill Registration Form    Jane Doe    existing@example.com    SecurePass123
    Registration Should Fail With    duplicate
```

</details>

---

### Exercise 2.2 — Data-Driven Testing กับ Discount Rules

บริษัทมี discount rules:
- ซื้อ >= 500 บาท: ลด 5%
- ซื้อ >= 1000 บาท: ลด 10%
- ซื้อ >= 2000 บาท: ลด 15%
- มี loyalty card: ลดเพิ่มอีก 2%

เขียน test ที่ใช้ Test Template ทดสอบกรณีต่างๆ อย่างน้อย 6 scenarios รวมถึง:
- ซื้อ 400 บาท ไม่มี loyalty card
- ซื้อ 1500 บาท มี loyalty card
- ซื้อ 3000 บาท มี loyalty card

<details>
<summary>เฉลย Exercise 2.2</summary>

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Test Template    Verify Discount Calculation

*** Test Cases ***           AMOUNT    HAS_LOYALTY    EXPECTED_FINAL
Below Min No Loyalty         400       ${False}       400
At Min Threshold No Loyalty  500       ${False}       475
Mid Range No Loyalty         1500      ${False}       1350
At High Threshold No Loyalty 2000      ${False}       1700
Below Min With Loyalty       400       ${True}        392
Mid Range With Loyalty       1500      ${True}        1320
High Amount With Loyalty     3000      ${True}        2490

*** Keywords ***
Verify Discount Calculation
    [Documentation]    ตรวจสอบการคำนวณ discount
    [Arguments]    ${amount}    ${has_loyalty}    ${expected_final}
    ${final}=    Calculate Final Price    ${amount}    ${has_loyalty}
    Should Be Equal As Numbers    ${final}    ${expected_final}
    Log    ✓ ${amount} + loyalty=${has_loyalty} → ${final}

Calculate Final Price
    [Arguments]    ${amount}    ${has_loyalty}
    ${discount_pct}=    Set Variable    0
    IF    ${amount} >= 2000
        ${discount_pct}=    Set Variable    15
    ELSE IF    ${amount} >= 1000
        ${discount_pct}=    Set Variable    10
    ELSE IF    ${amount} >= 500
        ${discount_pct}=    Set Variable    5
    END
    IF    ${has_loyalty}
        ${discount_pct}=    Evaluate    ${discount_pct} + 2
    END
    ${discount}=    Evaluate    ${amount} * ${discount_pct} / 100
    ${final}=       Evaluate    ${amount} - ${discount}
    RETURN    ${final}
```

</details>

---

## ส่วนที่ 3: Synthesis — วิเคราะห์และออกแบบ (บทที่ 7-8)

### Exercise 3.1 — Diagnose Bug ใน Test Code

นักพัฒนาให้ test code มาต่อนี้ แต่มัน "flaky" — บางทีผ่าน บางทีล้มเหลว
จงหาว่ามีปัญหาอะไรบ้าง (อย่างน้อย 3 ปัญหา) และเขียน version ที่แก้แล้ว

```robot
# โค้ดที่มีปัญหา — หาว่าผิดอะไร
*** Settings ***
Library    Browser

*** Test Cases ***
Test Login
    New Browser    chromium
    New Page    https://the-internet.herokuapp.com/login
    Sleep    2 seconds
    Input Text    id:username    tomsmith
    Input Text    id:password    SuperSecretPassword!
    Click Button    //button[@type='submit']
    Sleep    3 seconds
    Page Should Contain    You logged into a secure area!

Test Profile
    # ใช้ browser จาก Test Login
    Go To    https://the-internet.herokuapp.com/secure
    Page Should Contain    Secure Area
```

<details>
<summary>เฉลย Exercise 3.1</summary>

**ปัญหาที่พบ:**

1. **ใช้ `Sleep` แทน explicit wait** — `Sleep 2s` รอตายตัว ถ้า network ช้า = fail, ถ้า network เร็ว = เสียเวลาเปล่า
2. **Test 2 ขึ้นกับ Test 1** — "Test Profile" สมมติว่า browser ยังเปิดอยู่จาก "Test Login" ถ้า Test 1 fail หรือรัน Test 2 เดี่ยวๆ จะ crash
3. **ไม่ปิด Browser ใน Teardown** — ถ้า test fail browser ค้างอยู่
4. **ใช้ XPath แทน CSS/id** — `//button[@type='submit']` brittle กว่า `css:button[type='submit']`
5. **ไม่มี Tags และ Documentation** — หา test ยากเมื่อโปรเจคใหญ่ขึ้น

```robot
# tested: Robot Framework 7.4.2, Browser Library 19.x
# โค้ดที่แก้แล้ว
*** Settings ***
Library    Browser

*** Variables ***
${BASE_URL}    https://the-internet.herokuapp.com
${USERNAME}    tomsmith
${PASSWORD}    SuperSecretPassword!

*** Test Cases ***
Login With Valid Credentials Shows Secure Area
    [Documentation]    ตรวจสอบว่า login สำเร็จแสดง secure area message
    [Tags]    login    smoke
    [Setup]    Open Login Page
    Fill Text    id:username    ${USERNAME}
    Fill Text    id:password    ${PASSWORD}
    Click    css:button[type='submit']
    Wait For Elements State    css:.flash.success    visible    timeout=10s
    Get Text    css:.flash.success    contains    You logged into a secure area!
    [Teardown]    Close Browser

View Secure Area After Login
    [Documentation]    ตรวจสอบว่าหน้า secure area แสดงผลได้
    [Tags]    secure-area    regression
    [Setup]    Login To App
    Wait For Elements State    css:h2    visible
    Get Text    css:h2    contains    Secure Area
    [Teardown]    Close Browser

*** Keywords ***
Open Login Page
    New Browser    chromium    headless=True
    New Page       ${BASE_URL}/login
    Wait For Elements State    id:username    visible

Login To App
    Open Login Page
    Fill Text    id:username    ${USERNAME}
    Fill Text    id:password    ${PASSWORD}
    Click    css:button[type='submit']
    Wait For Elements State    css:.flash.success    visible
```

</details>

---

### Exercise 3.2 — ออกแบบ Test Strategy

ทีม QA ได้รับ feature ใหม่: ระบบ **coupon code** สำหรับ e-commerce

**Business rules:**
- Coupon มีหลายประเภท: % discount, fixed amount, free shipping
- Coupon แต่ละอันมีวันหมดอายุ
- User 1 คนใช้ coupon เดิมได้แค่ครั้งเดียว
- บาง coupon ใช้ได้เฉพาะกับ product บางประเภท
- ยอดซื้อขั้นต่ำ 200 บาทถึงจะใช้ coupon ได้

**จงออกแบบ:**
1. Test case list (อย่างน้อย 8 cases) ครอบคลุม happy path + edge cases
2. Test data ที่ต้องการ
3. Keywords หลักที่ควรมีใน resource file

<details>
<summary>เฉลย Exercise 3.2</summary>

**Test Case List:**

| # | Test Case | Tag | Type |
|---|-----------|-----|------|
| 1 | Apply Valid Percent Coupon Reduces Price Correctly | coupon, smoke | happy path |
| 2 | Apply Valid Fixed Amount Coupon Reduces Price | coupon, smoke | happy path |
| 3 | Apply Free Shipping Coupon Removes Shipping Fee | coupon, shipping | happy path |
| 4 | Expired Coupon Cannot Be Applied | coupon, negative | edge case |
| 5 | Already Used Coupon Cannot Be Applied Again | coupon, negative | edge case |
| 6 | Coupon Below Minimum Order Cannot Be Applied | coupon, negative | edge case |
| 7 | Coupon Applied To Wrong Category Is Rejected | coupon, negative | edge case |
| 8 | Multiple Coupons Cannot Be Applied Simultaneously | coupon, negative | boundary |
| 9 | Valid Coupon With Exact Minimum Order Amount | coupon, boundary | boundary |
| 10 | Coupon Code Is Case Insensitive | coupon, usability | usability |

**Test Data ที่ต้องการ:**
```yaml
coupons:
  - code: SAVE10PCT
    type: percent
    value: 10
    min_order: 200
    expires: "2030-12-31"
    category: all

  - code: FIXED50
    type: fixed
    value: 50
    min_order: 200
    expires: "2030-12-31"
    category: all

  - code: FREESHIP
    type: free_shipping
    value: 0
    min_order: 200
    expires: "2030-12-31"
    category: all

  - code: EXPIRED
    type: percent
    value: 20
    expires: "2020-01-01"   # already expired

  - code: ELECTRONICS10
    type: percent
    value: 10
    min_order: 200
    expires: "2030-12-31"
    category: electronics    # specific category
```

**Keywords หลักใน `resources/coupon.resource`:**
```robot
Apply Coupon Code
    [Arguments]    ${code}
    # fills coupon field and clicks apply

Coupon Should Be Applied Successfully
    [Arguments]    ${coupon_type}    ${expected_discount}
    # verifies discount is shown correctly

Coupon Should Be Rejected With
    [Arguments]    ${expected_reason}
    # verifies error message matches reason

Verify Order Total After Coupon
    [Arguments]    ${original}    ${coupon_code}    ${expected_total}
    # end-to-end verification
```

</details>

---

## สรุป Exercises

| Exercise | บท | Level | Concept หลัก |
|----------|----|-------|-------------|
| 1.1 | 1-2 | Recall | Explain concept ด้วยคำตัวเอง |
| 1.2 | 3 | Recall | Variable 3 แบบ |
| 2.1 | 4 | Application | Resource file + Keywords |
| 2.2 | 6 | Application | Test Template + Data-Driven |
| 3.1 | 7-8 | Synthesis | Diagnose bug ใน test code |
| 3.2 | 8 | Synthesis | ออกแบบ Test Strategy |
