# บทที่ 3: โครงสร้าง Test File และ Syntax

## ก่อนอ่านบทนี้ ลองตอบ:

- Virtual environment คืออะไร? ทำไมต้องใช้?
- หลัง `robot` รัน test แล้ว ได้ไฟล์อะไรบ้าง?

---

ลองตอบก่อน แล้วค่อยอ่านต่อ

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:
- อธิบาย 4 sections ในไฟล์ `.robot` ได้ และรู้ว่าแต่ละ section ใส่อะไร
- เขียน variable ทั้ง 3 แบบ (`${}`, `@{}`, `&{}`) และรู้ว่าใช้ตอนไหน
- เขียน test case ที่อ่านเข้าใจง่าย พร้อม tags และ documentation
- รู้ว่า spacing ใน RF สำคัญอย่างไร และ format ที่ถูกต้องเป็นแบบไหน

---

## ทำไมต้องรู้? (Why)

ก่อนจะเขียน test อะไรได้เลย ต้องเข้าใจ "ภาษา" ของ RF ก่อน

RF ไม่ใช่ภาษา programming ทั่วไป มันมีกฎเฉพาะเรื่อง spacing ที่ถ้าผิดพลาด test จะ fail แบบงงมาก เช่น:

```robot
# ผิด — ต้องการ 4 spaces ระหว่าง token
Input Text  id:username  testuser   ← มีแค่ 2 spaces

# ถูก
Input Text    id:username    testuser   ← 4 spaces
```

Error ที่ได้: `No keyword with name 'Input Text  id:username  testuser' found`

งงไหม? ถ้าเข้าใจ syntax ดี ก็จะหลีกเลี่ยงได้

---

## Analogy: .robot ไฟล์เหมือนสารบัญหนังสือ

ไฟล์ `.robot` มี 4 section เหมือนหนังสือที่แบ่งเป็นส่วนๆ:

| Section | เหมือน | บรรจุ |
|---------|--------|-------|
| `*** Settings ***` | คำนำ/บรรณานุกรม | import library, resource file |
| `*** Variables ***` | glossary | ค่าที่ใช้ซ้ำทั้งไฟล์ |
| `*** Test Cases ***` | เนื้อหาหลัก | สิ่งที่จะ test |
| `*** Keywords ***` | ภาคผนวก | custom keyword ที่เขียนเอง |

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
- ต้องมีทุก section ในทุกไฟล์ — จริงๆ ไฟล์ `.robot` ที่มี test case ต้องมีแค่ `*** Test Cases ***` section เดียวก็พอ ที่เหลือ optional
- Section ต้องเรียงลำดับตายตัว — จริงๆ ลำดับ section ยืดหยุ่นได้ แต่ convention คือเรียงตามตารางด้านบน

---

## 4 Sections ใน .robot ไฟล์

### 1. `*** Settings ***`

บอกว่า file นี้ใช้อะไรบ้าง — import library, resource, และกำหนด setup/teardown

```robot
*** Settings ***
Documentation    Test suite สำหรับทดสอบระบบ login
Library          Browser
Resource         resources/common.resource
Suite Setup      Open Browser Session
Suite Teardown   Close Browser Session
Test Setup       Go To Login Page
Test Teardown    Take Screenshot On Failure
```

| Setting | ความหมาย |
|---------|----------|
| `Library` | import test library |
| `Resource` | import resource file (keyword + variable) |
| `Suite Setup` | รันก่อน test ทุก case ใน file นี้ |
| `Suite Teardown` | รันหลัง test ทุก case ใน file นี้ |
| `Test Setup` | รันก่อน test case แต่ละอัน |
| `Test Teardown` | รันหลัง test case แต่ละอัน |

### 2. `*** Variables ***`

เก็บค่าที่ใช้ซ้ำหลายครั้ง ป้องกันการ hardcode

```robot
*** Variables ***
${BASE_URL}       https://example.com
${BROWSER}        chromium
${TIMEOUT}        10 seconds
@{SUPPORTED_ROLES}    admin    user    guest
&{ADMIN_USER}     username=admin    password=admin123
```

### 3. `*** Test Cases ***`

ส่วนที่บอกว่าจะ test อะไร — เป็น "what" ไม่ใช่ "how"

```robot
*** Test Cases ***
User Can Login With Valid Credentials
    [Documentation]    ทดสอบว่า user login ได้ด้วย credential ที่ถูกต้อง
    [Tags]    login    smoke
    Open Login Page
    Enter Credentials    admin    admin123
    Verify User Is Logged In

User Cannot Login With Wrong Password
    [Documentation]    ทดสอบว่าระบบแสดง error เมื่อ password ผิด
    [Tags]    login    negative
    Open Login Page
    Enter Credentials    admin    wrongpassword
    Error Message Should Be Visible    Invalid password
```

### 4. `*** Keywords ***`

Custom keyword ที่เราเขียนเอง — เป็น "how"

```robot
*** Keywords ***
Enter Credentials
    [Documentation]    กรอก username และ password
    [Arguments]    ${username}    ${password}
    Fill Text    id:username    ${username}
    Fill Text    id:password    ${password}
    Click    id:login-btn

Verify User Is Logged In
    Wait For Elements State    id:dashboard    visible
    Get Text    id:welcome-message    ==    Welcome, ${username}
```

---

## Variables — 3 แบบ, 3 ใช้

### `${scalar}` — ค่าเดี่ยว (ใช้บ่อยที่สุด)

```robot
*** Variables ***
${USERNAME}    testuser
${MAX_RETRY}   ${3}

*** Test Cases ***
Example
    Log    Username คือ: ${USERNAME}
    ${result}=    Get Login Status
    Should Be True    ${result}
```

### `@{list}` — รายการ

```robot
*** Variables ***
@{BROWSERS}    chromium    firefox    webkit

*** Test Cases ***
Example
    FOR    ${browser}    IN    @{BROWSERS}
        Log    กำลังทดสอบบน: ${browser}
    END
    ${first}=    Get From List    ${BROWSERS}    0
    Log    Browser แรก: ${first}    # chromium
```

### `&{dict}` — key-value pairs

```robot
*** Variables ***
&{VALID_USER}    username=john    password=secret123    role=admin

*** Test Cases ***
Example
    Log    username คือ: ${VALID_USER}[username]
    Log    role คือ: ${VALID_USER}[role]
```

---

## กฎ Spacing ที่ต้องรู้

RF ใช้ **4 spaces (หรือมากกว่า) หรือ tab** เพื่อแยก token

```robot
*** Test Cases ***
Login Test
    Input Text    id:username    testuser      ← keyword    locator    value
    Click Button    id:submit                  ← keyword    locator
    ${status}=    Get Text    id:result        ← ${var}=    keyword    locator
```

ห้ามใช้แค่ 1-2 spaces — RF จะมองว่าเป็น keyword เดียวกัน

**เครื่องมือช่วย format อัตโนมัติ:**
```bash
# ใช้ robocop / robotidy
pip install robotframework-tidy
robotidy tests/
```

---

## ตัวอย่าง 3 ระดับ

### Beginner — test ระบบ login อย่างง่าย

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Documentation    ตัวอย่าง test ระบบ login พื้นฐาน

*** Variables ***
${USERNAME}    alice
${PASSWORD}    pass@123

*** Test Cases ***
Verify Login Variables Are Set
    [Documentation]    ตรวจสอบว่า variable ถูกกำหนดค่าแล้ว
    [Tags]    setup    smoke
    Should Not Be Empty    ${USERNAME}
    Should Not Be Empty    ${PASSWORD}
    Length Should Be Greater Than Or Equal To    ${PASSWORD}    8
    Log    ✓ Variables พร้อมใช้งาน

Verify Credentials Format
    [Documentation]    ตรวจสอบว่า credentials อยู่ในรูปแบบที่ถูกต้อง
    [Tags]    setup    validation
    Should Contain    ${USERNAME}    alice
    Should Match Regexp    ${PASSWORD}    .*[@#$%].*
    Log    ✓ Credentials format ถูกต้อง
```

### Intermediate — test user roles ด้วย list

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Documentation    ทดสอบ role ต่างๆ ของ user
Library          Collections

*** Variables ***
@{VALID_ROLES}        admin    editor    viewer
@{INVALID_ROLES}      superadmin    root    unknown_role
&{ROLE_PERMISSIONS}   admin=all    editor=edit,view    viewer=view

*** Test Cases ***
All Valid Roles Should Be Recognizable
    [Documentation]    ตรวจสอบว่า role ที่กำหนดไว้ถูกต้องครบ 3 อัน
    [Tags]    roles    smoke
    Length Should Be    ${VALID_ROLES}    3
    Should Contain    ${VALID_ROLES}    admin
    Should Contain    ${VALID_ROLES}    editor
    Should Contain    ${VALID_ROLES}    viewer

Invalid Roles Should Not Be In Valid List
    [Documentation]    ตรวจสอบว่า invalid role ไม่อยู่ใน valid list
    [Tags]    roles    negative
    FOR    ${role}    IN    @{INVALID_ROLES}
        Should Not Contain    ${VALID_ROLES}    ${role}
        Log    ✓ ยืนยัน: '${role}' ไม่ใช่ valid role
    END

Admin Should Have All Permissions
    [Documentation]    ตรวจสอบ permission ของ admin
    [Tags]    roles    permissions
    ${admin_perms}=    Get From Dictionary    ${ROLE_PERMISSIONS}    admin
    Should Be Equal    ${admin_perms}    all
```

Output ที่ควรได้:
```
All Valid Roles Should Be Recognizable                                | PASS |
Invalid Roles Should Not Be In Valid List                            | PASS |
Admin Should Have All Permissions                                     | PASS |
3 tests, 3 passed, 0 failed
```

### Advanced — โครงสร้าง test suite พร้อม setup/teardown

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Documentation    Test suite สำหรับ User Management
...              version: 2.0
...              มีการทดสอบ CRUD operations
Suite Setup      Initialize Test Data
Suite Teardown   Cleanup Test Data
Test Setup       Reset User State
Test Tags        user-management

*** Variables ***
${BASE_URL}           https://api.example.com
${API_VERSION}        v2
&{DEFAULT_USER}       name=Test User    email=test@example.com    role=viewer
@{CLEANUP_IDS}        # จะเพิ่มระหว่าง test

*** Test Cases ***
Create User Returns Valid ID
    [Documentation]    สร้าง user ใหม่และตรวจสอบว่าได้ ID กลับมา
    [Tags]    create    critical
    ${user_id}=    Create Test User    ${DEFAULT_USER}
    Should Not Be Empty    ${user_id}
    Should Match Regexp    ${user_id}    ^[a-f0-9]{8}-
    Append To List    ${CLEANUP_IDS}    ${user_id}
    Log    สร้าง user สำเร็จ ID: ${user_id}

*** Keywords ***
Initialize Test Data
    Log    กำลัง initialize test data...
    ${ids}=    Create List
    Set Suite Variable    ${CLEANUP_IDS}    ${ids}

Cleanup Test Data
    Log    กำลัง cleanup: ${CLEANUP_IDS}
    FOR    ${id}    IN    @{CLEANUP_IDS}
        Log    ลบ user ID: ${id}
    END

Reset User State
    Log    Reset state ก่อน test ใหม่

Create Test User
    [Arguments]    ${user_data}
    Log    กำลังสร้าง user: ${user_data}[name]
    ${fake_id}=    Evaluate    'a1b2c3d4-e5f6-7890-abcd-ef1234567890'
    RETURN    ${fake_id}
```

---

## Common Mistakes

❌ **ใช้ spaces น้อยกว่า 4 ระหว่าง keyword กับ argument**
```robot
# ผิด
Input Text  id:username  testuser
# RF มองว่า 'Input Text  id:username  testuser' เป็น keyword เดียว

# ถูก
Input Text    id:username    testuser
```
*(source: [RF User Guide - Spacing](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#space-and-escape))*

❌ **ลืม `=` เมื่อรับค่าจาก keyword**
```robot
# ผิด — ไม่มี =
${result}    Get Text    id:message

# ถูก
${result}=    Get Text    id:message
# หรือ (RF 7+ ยอมรับทั้งสองแบบ)
${result} =    Get Text    id:message
```
*(source: [RF User Guide - Return Values](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#return-values))*

❌ **เขียน test logic ใน test case แทนที่จะใช้ keyword**
```robot
# ผิด — test case ยาวเกิน ไม่อ่านออก
Login Test
    Browser.Go To    https://example.com/login
    Browser.Fill Text    id:username    testuser
    Browser.Fill Text    id:password    pass123
    Browser.Click    id:login-btn
    Browser.Wait For Elements State    id:dashboard    visible
    ${title}=    Browser.Get Title
    Should Contain    ${title}    Dashboard

# ถูก — ซ่อน implementation ใน keyword
Login Test
    Go To Login Page
    Login As    testuser    pass123
    Dashboard Page Should Be Open
```
*(source: [RF Best Practices - Keyword Design](https://docs.robotframework.org/docs/style_guide))*

---

## สรุปบท

ก่อนไปบทต่อไป ลองตอบ (คิด 30 วินาทีก่อนดูเฉลย):

**คำถาม 1:** ไฟล์ `.robot` มี 4 sections อะไรบ้าง? แต่ละ section ใส่อะไร?

**คำถาม 2:** ถ้าต้องเก็บรายชื่อ browser ที่จะทดสอบ (`chromium`, `firefox`, `webkit`) ควรใช้ variable แบบไหน? เขียนออกมาด้วย

**คำถาม 3:** เหตุผลที่ควรซ่อน implementation ไว้ใน keyword แทนที่จะเขียนใน test case ตรงๆ คืออะไร?

---

<details>
<summary>เฉลย (คลิกเพื่อดู — ดูหลังจากลองตอบแล้วเท่านั้น)</summary>

**เฉลย 1:** `*** Settings ***` (import library/resource, setup/teardown), `*** Variables ***` (ค่าที่ใช้ซ้ำ), `*** Test Cases ***` (scenario ที่จะทดสอบ), `*** Keywords ***` (custom keyword ที่เขียนเอง)

**เฉลย 2:** ใช้ `@{list}` เพราะเก็บหลายค่า:
```robot
@{BROWSERS}    chromium    firefox    webkit
```

**เฉลย 3:** 3 เหตุผล: (1) อ่านง่ายขึ้น — test case บอกว่า "test อะไร" ไม่ใช่ "ทำยังไง", (2) reuse ได้ — keyword เดียวใช้ได้หลาย test case, (3) แก้ง่ายขึ้น — ถ้า element locator เปลี่ยน แก้แค่ที่เดียวใน keyword

</details>
