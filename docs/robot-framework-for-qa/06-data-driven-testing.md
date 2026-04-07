# บทที่ 6: Data-Driven Testing

## ก่อนอ่านบทนี้ ลองตอบ:

- `Wait For Elements State` ต่างจาก `Sleep` อย่างไร?
- ทำไม locator `id:xxx` ถึงดีกว่า `xpath=//div[3]/button`?

---

ลองตอบก่อน แล้วค่อยอ่านต่อ

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:
- เขียน Test Template เพื่อรัน test case เดียวกับ data หลายชุดได้
- แยก test data ออกไปเป็นไฟล์ `.csv` หรือ `.yaml`
- ใช้ `FOR` loop ใน keyword
- รู้ว่าควรใช้ Data-Driven Testing ตอนไหน

---

## ทำไมต้องรู้? (Why)

ลองนึกถึง test case นี้:

```robot
Login With Admin Should Work
    Login As    admin    admin123
    Dashboard Should Be Visible

Login With Editor Should Work
    Login As    editor    edit456
    Dashboard Should Be Visible

Login With Viewer Should Work
    Login As    viewer    view789
    Dashboard Should Be Visible
```

3 test case แต่ logic เดียวกันทุกอย่าง ต่างกันแค่ data

ถ้ามี 10 role ต้องเขียน 10 test case? ถ้าเพิ่ม step verification ต้องแก้ 10 ที่?

**Data-Driven Testing** แก้ปัญหานี้ — เขียน logic ครั้งเดียว รันกับ data กี่ชุดก็ได้

---

## Analogy: Test Template เหมือน แม่พิมพ์ขนม

แม่พิมพ์ = shape เดียวกันทุกครั้ง
Data = แป้งที่ใส่เข้าไป — สีต่างกัน, รสต่างกัน แต่ได้ขนมรูปเดิม

Test Template = logic เดียวกันทุกครั้ง
Test Case = data ชุดต่างๆ — credential ต่างกัน, role ต่างกัน แต่รัน workflow เดิม

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
- Test Template ต้องใช้กับ login เท่านั้น — จริงๆ ใช้ได้กับ scenario ไหนก็ได้ที่มี "data หลายชุด + logic เดิม"
- Data ต้องใส่ใน test file เท่านั้น — จริงๆ data มาจาก CSV, YAML, database, หรือ API ก็ได้

---

## Test Template

### รูปแบบพื้นฐาน

```robot
*** Settings ***
Test Template    Login Should Succeed

*** Test Cases ***    USERNAME    PASSWORD
Admin Login          admin       admin123
Editor Login         editor      edit456
Viewer Login         viewer      view789

*** Keywords ***
Login Should Succeed
    [Arguments]    ${username}    ${password}
    Login As    ${username}    ${password}
    Dashboard Should Be Visible
```

แต่ละ row ใน `*** Test Cases ***` คือ test case 1 อัน
ชื่อแรก = test case name, คอลัมน์ถัดไป = arguments

### Template เฉพาะ test case (ไม่ใช้ Suite-level)

```robot
*** Test Cases ***
Login With Multiple Roles
    [Template]    Login Should Succeed
    admin      admin123
    editor     edit456
    viewer     view789
```

---

## Variables จาก External File

### ใช้ Variables File (.py)

```python
# data/test_users.py
USERS = [
    {"username": "admin", "password": "admin123", "role": "Administrator"},
    {"username": "editor", "password": "edit456", "role": "Editor"},
    {"username": "viewer", "password": "view789", "role": "Viewer"},
]

BASE_URL = "https://example.com"
TIMEOUT = "10 seconds"
```

```robot
*** Settings ***
Variables    data/test_users.py

*** Test Cases ***
Check User Count
    Length Should Be    ${USERS}    3
```

### ใช้ Variables File (.yaml)

```yaml
# data/config.yaml
base_url: https://example.com
timeout: 10 seconds
users:
  - username: admin
    password: admin123
    role: Administrator
  - username: editor
    password: edit456
    role: Editor
```

```robot
*** Settings ***
Variables    data/config.yaml    # ต้องติดตั้ง pyyaml ก่อน

*** Test Cases ***
Check Config
    Log    ${base_url}
    Log    ${timeout}
```

---

## FOR Loop ใน Keyword

```robot
*** Keywords ***
All Users Should Be Able To Login
    [Arguments]    @{users}
    FOR    ${user}    IN    @{users}
        Login As    ${user}[username]    ${user}[password]
        Dashboard Should Be Visible
        Logout
    END
```

---

## ตัวอย่าง 3 ระดับ

### Beginner — Test Template สำหรับ validation

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Library          Collections
Test Template    Password Validation Rule Should Hold

*** Test Cases ***                  PASSWORD         IS_VALID
Too Short Password                  abc              ${False}
Minimum Length Password             abcdefgh         ${True}
Password With Numbers               abc123de         ${True}
Empty Password                      ${EMPTY}         ${False}
Long Valid Password                 MyStr0ng!Pass    ${True}

*** Keywords ***
Password Validation Rule Should Hold
    [Documentation]    ตรวจสอบ password validation rule
    [Arguments]    ${password}    ${expected_valid}
    ${is_valid}=    Is Password Valid    ${password}
    Should Be Equal    ${is_valid}    ${expected_valid}

Is Password Valid
    [Documentation]    คืนค่า True ถ้า password ผ่าน rule: ยาว >= 8 ตัว
    [Arguments]    ${password}
    ${length}=    Get Length    ${password}
    ${is_valid}=    Evaluate    ${length} >= 8
    RETURN    ${is_valid}
```

Output ที่ควรได้:
```
Too Short Password                                                    | PASS |
Minimum Length Password                                               | PASS |
Password With Numbers                                                 | PASS |
Empty Password                                                        | PASS |
Long Valid Password                                                    | PASS |
5 tests, 5 passed, 0 failed
```

### Intermediate — Data-Driven กับ business logic จริง

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Library    Collections

*** Variables ***
@{PROMO_CODES}
...    SAVE10,10,1000,900
...    SAVE20,20,1000,800
...    SAVE50,50,500,250
...    INVALID,0,1000,1000

*** Test Cases ***
All Promo Code Scenarios
    [Template]    Apply Promo Code And Verify Discount
    SAVE10    10    1000    900
    SAVE20    20    1000    800
    SAVE50    50     500    250
    INVALID    0    1000    1000

*** Keywords ***
Apply Promo Code And Verify Discount
    [Documentation]    ตรวจสอบว่า promo code ให้ส่วนลดถูกต้อง
    [Arguments]    ${code}    ${discount_pct}    ${original_price}    ${expected_final}
    ${final_price}=    Calculate Price After Promo    ${original_price}    ${discount_pct}
    Should Be Equal As Numbers    ${final_price}    ${expected_final}
    Log    ✓ ${code}: ${original_price} → ${final_price} (${discount_pct}% off)

Calculate Price After Promo
    [Arguments]    ${price}    ${discount_pct}
    ${discount}=    Evaluate    ${price} * ${discount_pct} / 100
    ${final}=       Evaluate    ${price} - ${discount}
    RETURN    ${final}
```

Output ที่ควรได้:
```
Apply Promo Code And Verify Discount: SAVE10 10 1000 900             | PASS |
Apply Promo Code And Verify Discount: SAVE20 20 1000 800             | PASS |
Apply Promo Code And Verify Discount: SAVE50 50 500 250              | PASS |
Apply Promo Code And Verify Discount: INVALID 0 1000 1000            | PASS |
4 tests, 4 passed, 0 failed
```

### Advanced — Loop + Conditional + Error Handling

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Library    Collections

*** Variables ***
@{TEST_EMAILS}
...    valid@example.com
...    also.valid+tag@example.co.th
...    invalid-no-at
...    @missing-local.com
...    missing-domain@

*** Test Cases ***
Validate Multiple Email Formats
    [Documentation]    ตรวจสอบ email validation กับหลาย format
    ${valid_count}=    Set Variable    ${0}
    ${invalid_count}=    Set Variable    ${0}
    FOR    ${email}    IN    @{TEST_EMAILS}
        ${is_valid}=    Run Keyword And Return Status    Email Should Be Valid    ${email}
        IF    ${is_valid}
            ${valid_count}=    Evaluate    ${valid_count} + 1
            Log    ✓ Valid: ${email}
        ELSE
            ${invalid_count}=    Evaluate    ${invalid_count} + 1
            Log    ✗ Invalid: ${email}
        END
    END
    Should Be Equal As Numbers    ${valid_count}      2
    Should Be Equal As Numbers    ${invalid_count}    3
    Log    สรุป: valid=${valid_count}, invalid=${invalid_count}

*** Keywords ***
Email Should Be Valid
    [Documentation]    ตรวจสอบ email format ด้วย regex
    [Arguments]    ${email}
    Should Match Regexp    ${email}    ^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$
```

Output ที่ควรได้:
```
Validate Multiple Email Formats                                       | PASS |
1 test, 1 passed, 0 failed
```

---

## Common Mistakes

❌ **เขียน test case แยกทุก data แทนที่จะใช้ Template**
```robot
# ผิด — ซ้ำซ้อน แก้ยาก
Admin Can Login
    Login As    admin    admin123
    Dashboard Should Be Visible

Editor Can Login
    Login As    editor    edit456
    Dashboard Should Be Visible

# ถูก — ใช้ Template
*** Settings ***
Test Template    Login Should Succeed

*** Test Cases ***    USER      PASS
Admin Login          admin     admin123
Editor Login         editor    edit456
```
*(source: [RF User Guide - Data-Driven Style](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#data-driven-style))*

❌ **Hardcode test data ใน test file แทน external file**
```robot
# ผิด — แก้ data ต้องแก้ code
*** Test Cases ***
Test Promo
    [Template]    Apply Promo
    SAVE10    10    1000
    SAVE20    20    500
    # ... อีก 50 rows

# ถูก — data อยู่ใน CSV/YAML ต่างหาก แก้ data ไม่ต้องแตะ code
*** Settings ***
Variables    data/promo_codes.yaml
```
*(source: [RF Best Practices - Test Data Management](https://docs.robotframework.org/docs/style_guide))*

❌ **ลืม handle failure ใน loop — ทำให้ test หยุดกลางทาง**
```robot
# ผิด — ถ้า user 1 fail, user 2 และ 3 ไม่ได้รัน
FOR    ${user}    IN    @{USERS}
    Login As    ${user}[username]    ${user}[password]    # ถ้า fail ทั้ง loop หยุด

# ถูก — ใช้ Run Keyword And Continue On Failure
FOR    ${user}    IN    @{USERS}
    Run Keyword And Continue On Failure    Login As    ${user}[username]    ${user}[password]
```
*(source: [RF User Guide - Continuing Despite Failures](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#continuing-on-failure))*

---

## สรุปบท

ก่อนไปบทต่อไป ลองตอบ (คิด 30 วินาทีก่อนดูเฉลย):

**คำถาม 1:** สถานการณ์แบบไหนที่ควรใช้ Test Template? ยกตัวอย่าง scenario ที่ไม่ได้อยู่ในบทนี้

**คำถาม 2:** ถ้า loop มี 10 iteration และ iteration ที่ 3 fail แต่อยากรัน iteration ที่ 4-10 ต่อ ต้องทำยังไง?

**คำถาม 3:** ความแตกต่างระหว่างเก็บ test data ไว้ใน test file กับใน external file คืออะไร? ข้อดีข้อเสียของแต่ละแบบ?

---

<details>
<summary>เฉลย (คลิกเพื่อดู — ดูหลังจากลองตอบแล้วเท่านั้น)</summary>

**เฉลย 1:** สถานการณ์ที่เหมาะ: ทดสอบ form validation กับ input หลายแบบ (valid/invalid), ทดสอบ API endpoint กับ request data ต่างๆ, ทดสอบ calculation formula กับตัวเลขหลายชุด, ทดสอบ permission กับ role ต่างๆ — หลักการคือ "logic เดิม data ต่าง"

**เฉลย 2:** ใช้ `Run Keyword And Continue On Failure` ครอบ keyword ใน loop หรือใช้ `Run Keyword And Return Status` แล้ว collect result ไว้

**เฉลย 3:** ใน test file: เข้าใจง่ายเห็น data ทันที แต่ผสม code กับ data ทำให้แก้ยากเมื่อ data มาก External file: แยก data ออกจาก logic, นักธุรกิจ/ทีม QA อื่นแก้ data ได้โดยไม่ต้องเปิด code editor, แต่ต้องรู้จัก format ไฟล์

</details>
