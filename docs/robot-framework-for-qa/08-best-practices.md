# บทที่ 8: Best Practices และ Project Structure

## ก่อนอ่านบทนี้ ลองตอบ:

- `log.html` กับ `report.html` ต่างกันอย่างไร? ใช้อันไหนทำอะไร?
- ถ้า test fail ด้วย "Element not visible" จะ debug อย่างไร?

---

ลองตอบก่อน แล้วค่อยอ่านต่อ

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:
- จัดโครงสร้างโปรเจค RF ที่ maintainable ในทีมได้
- ใช้ Page Object Pattern เพื่อลด duplication
- รู้ว่า code smell ของ RF มีอะไรบ้าง และแก้ยังไง
- Integrate RF เข้า CI/CD pipeline เบื้องต้นได้

---

## ทำไมต้องรู้? (Why)

test ที่ทำงานได้วันนี้อาจ "ทนทาน" แค่ไหน?

เดือนหน้า:
- หน้า web redesign → locator ทั้งหมดเปลี่ยน
- Feature ใหม่ → test case เพิ่มขึ้น 3 เท่า
- สมาชิกทีมใหม่ → ต้องเข้าใจโค้ดเร็วที่สุด

Best practices คือสิ่งที่ทำให้ test โปรเจคอยู่รอดในระยะยาว ไม่ใช่แค่วันนี้

---

## Analogy: Project Structure เหมือน บ้านที่ออกแบบดี

บ้านที่ดีมีห้องแยกชัดเจน — ห้องนอน, ครัว, ห้องน้ำ ไม่ปะปนกัน

RF project ที่ดีก็เหมือนกัน:
- `tests/` = ห้องนอน — เก็บสิ่งที่ต้อง test (test case)
- `resources/` = ครัว — เก็บเครื่องมือ (keywords)
- `data/` = ห้องเก็บของ — เก็บ test data

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
- แค่แยกโฟลเดอร์ถูกก็พอ — จริงๆ structure ภายในโฟลเดอร์ก็สำคัญ เช่น แยก resource file ตาม feature ไม่ใช่ dump ทุกอย่างใน `common.resource`

---

## โครงสร้างโปรเจคมาตรฐาน

```
my-qa-project/
├── tests/
│   ├── smoke/
│   │   ├── login_smoke.robot
│   │   └── homepage_smoke.robot
│   ├── regression/
│   │   ├── login_tests.robot
│   │   ├── checkout_tests.robot
│   │   └── profile_tests.robot
│   └── api/
│       └── user_api_tests.robot
│
├── resources/
│   ├── common.resource            ← browser setup/teardown, screenshot
│   ├── login.resource             ← login workflow keywords
│   ├── navigation.resource        ← เนวิเกต menu, pages
│   └── pages/                     ← Page Object files
│       ├── login_page.resource
│       ├── dashboard_page.resource
│       └── checkout_page.resource
│
├── data/
│   ├── users.yaml                 ← test users ทุก environment
│   ├── products.csv               ← product data
│   └── config/
│       ├── staging.yaml
│       └── production.yaml
│
├── .venv/                         ← ไม่ commit
├── requirements.txt               ← commit
├── .gitignore
└── robot.toml                     ← RF config (optional)
```

---

## Page Object Pattern

Page Object คือ resource file ที่ represent "หน้าเดียว" ของ app

ทุก action และ verification บนหน้านั้น อยู่ใน file เดียว

### ตัวอย่าง Login Page Object

```robot
# resources/pages/login_page.resource
*** Settings ***
Library    Browser

*** Variables ***
${LOGIN_URL}          /login
${USERNAME_INPUT}     id:username
${PASSWORD_INPUT}     id:password
${LOGIN_BUTTON}       css:button[type='submit']
${SUCCESS_MSG}        css:.flash.success
${ERROR_MSG}          css:.flash.error

*** Keywords ***
Open Login Page
    [Documentation]    เปิดหน้า login
    Go To    ${BASE_URL}${LOGIN_URL}
    Login Page Should Be Open

Login Page Should Be Open
    [Documentation]    ตรวจสอบว่าอยู่หน้า login
    Wait For Elements State    ${USERNAME_INPUT}    visible

Login With
    [Documentation]    Login ด้วย credentials ที่กำหนด
    [Arguments]    ${username}    ${password}
    Fill Text    ${USERNAME_INPUT}    ${username}
    Fill Text    ${PASSWORD_INPUT}    ${password}
    Click    ${LOGIN_BUTTON}

Login Should Succeed
    [Documentation]    ตรวจสอบว่า login สำเร็จ
    Wait For Elements State    ${SUCCESS_MSG}    visible

Login Should Fail With
    [Documentation]    ตรวจสอบ error message เมื่อ login ล้มเหลว
    [Arguments]    ${expected_message}
    Wait For Elements State    ${ERROR_MSG}    visible
    Get Text    ${ERROR_MSG}    contains    ${expected_message}
```

### Test file ที่ใช้ Page Object

```robot
# tests/regression/login_tests.robot
*** Settings ***
Library      Browser
Resource     resources/common.resource
Resource     resources/pages/login_page.resource

Suite Setup     Open Browser Session
Suite Teardown  Close Browser Session

*** Variables ***
&{VALID_USER}    username=tomsmith    password=SuperSecretPassword!

*** Test Cases ***
Valid Login Redirects To Secure Area
    [Tags]    login    smoke
    Open Login Page
    Login With    ${VALID_USER}[username]    ${VALID_USER}[password]
    Login Should Succeed

Invalid Password Shows Error
    [Tags]    login    negative
    Open Login Page
    Login With    ${VALID_USER}[username]    wrongpassword
    Login Should Fail With    Your password is invalid!
```

**ประโยชน์ของ Page Object:**
- ถ้า locator เปลี่ยน แก้แค่ `login_page.resource` ที่เดียว
- test file อ่านง่าย เห็นแค่ business logic
- keyword ชัดเจน reuse ได้

---

## Code Smells ที่พบบ่อย

### 1. Magic Numbers/Strings

```robot
# ❌ ไม่รู้ว่า 300 มาจากไหน
Should Be Equal As Numbers    ${total}    300

# ✅ ชัดเจน มีความหมาย
${EXPECTED_TOTAL}    300
Should Be Equal As Numbers    ${total}    ${EXPECTED_TOTAL}
```

### 2. God Keyword (ทำทุกอย่างใน keyword เดียว)

```robot
# ❌ keyword ยาวเกิน ทำหลายอย่าง
Complete Purchase
    Open Cart
    Apply Discount Code    SAVE10
    Fill Shipping Address    ...    # 5 fields
    Fill Payment Info    ...       # 4 fields
    Confirm Order
    Verify Order Confirmation
    Check Email Notification

# ✅ แบ่งเป็น keyword ย่อย
Complete Purchase
    Open Cart
    Apply Discount Code    SAVE10
    Fill Checkout Details
    Confirm And Verify Order

Fill Checkout Details
    Fill Shipping Address
    Fill Payment Info

Confirm And Verify Order
    Confirm Order
    Verify Order Confirmation
    Check Email Notification
```

### 3. Test Case ที่ขึ้นกันกัน (Test Dependencies)

```robot
# ❌ test 2 ต้องรันหลัง test 1 เท่านั้น
Test 1 - Login
    Login As    admin    pass123

Test 2 - Go To Profile
    # สมมติว่า login อยู่แล้วจาก test 1
    Go To Profile Page    # ถ้า test 1 fail → test 2 fail ด้วยทั้งที่ logic ถูก

# ✅ แต่ละ test อิสระกัน
Test 2 - Go To Profile
    [Setup]    Login As    admin    pass123    # login เองใน test
    Go To Profile Page
```

---

## การ Integrate กับ CI/CD

### GitHub Actions (ตัวอย่าง)

```yaml
# .github/workflows/robot-tests.yml
name: Robot Framework Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up Python
        uses: actions/setup-python@v4
        with:
          python-version: '3.12'

      - name: Install dependencies
        run: |
          pip install -r requirements.txt
          rfbrowser init

      - name: Run Robot Framework tests
        run: |
          robot --include smoke \
                --output results/output.xml \
                --log results/log.html \
                --report results/report.html \
                tests/

      - name: Upload test results
        uses: actions/upload-artifact@v3
        if: always()    # upload แม้ test fail
        with:
          name: robot-results
          path: results/
```

### คำสั่ง RF ที่ CI/CD ใช้บ่อย

```bash
# รัน smoke test, exit code 1 ถ้า fail
robot --include smoke tests/

# รัน ทุก test แต่ไม่ fail build ถ้า acceptance test fail
robot --include smoke tests/ && echo "Smoke passed"

# รัน แบบ parallel (ต้องติดตั้ง pabot)
pip install robotframework-pabot
pabot --processes 4 tests/
```

---

## ตัวอย่าง 3 ระดับ

### Beginner — เขียน test ที่เป็นอิสระจากกัน

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Library    Collections

*** Test Cases ***
Calculate Shipping For Domestic Order
    [Documentation]    Test shipping calculation สำหรับออเดอร์ในประเทศ
    [Tags]    shipping    smoke
    [Setup]    Initialize Shipping Rules
    ${fee}=    Calculate Shipping Fee    500    domestic
    Should Be Equal As Numbers    ${fee}    50

Calculate Shipping For Heavy Domestic Order
    [Documentation]    Test shipping calculation สำหรับออเดอร์หนักในประเทศ
    [Tags]    shipping    regression
    [Setup]    Initialize Shipping Rules
    ${fee}=    Calculate Shipping Fee    3000    domestic
    Should Be Equal As Numbers    ${fee}    100

Calculate Shipping For International Order
    [Documentation]    Test shipping calculation สำหรับออเดอร์ต่างประเทศ
    [Tags]    shipping    regression
    [Setup]    Initialize Shipping Rules
    ${fee}=    Calculate Shipping Fee    500    international
    Should Be Equal As Numbers    ${fee}    350

*** Keywords ***
Initialize Shipping Rules
    ${rules}=    Create Dictionary
    ...    domestic_standard=50
    ...    domestic_heavy=100
    ...    international_standard=350
    Set Test Variable    ${SHIPPING_RULES}    ${rules}

Calculate Shipping Fee
    [Arguments]    ${order_value}    ${destination}
    IF    '${destination}' == 'domestic'
        IF    ${order_value} >= 2000
            RETURN    ${SHIPPING_RULES}[domestic_heavy]
        ELSE
            RETURN    ${SHIPPING_RULES}[domestic_standard]
        END
    ELSE
        RETURN    ${SHIPPING_RULES}[international_standard]
    END
```

Output ที่ควรได้:
```
Calculate Shipping For Domestic Order                                 | PASS |
Calculate Shipping For Heavy Domestic Order                          | PASS |
Calculate Shipping For International Order                           | PASS |
3 tests, 3 passed, 0 failed
```

### Intermediate — Page Object + ใช้ข้อมูลจาก Dictionary

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
# จำลอง Page Object pattern โดยไม่ต้องมี browser
*** Settings ***
Library    Collections

*** Variables ***
# Simulated Page State (จำลองแทน browser)
&{PAGE_STATE}    is_logged_in=${False}    current_user=${EMPTY}    page=login

*** Test Cases ***
Complete User Journey From Login To Profile Update
    [Documentation]    ทดสอบ user journey: login → view profile → update name
    [Tags]    user-journey    regression
    Login Page Should Be Showing
    User Logs In With    alice    pass123
    Dashboard Should Be Showing    alice
    User Navigates To Profile
    User Updates Display Name    Alice Smith
    Profile Name Should Be Updated To    Alice Smith

*** Keywords ***
Login Page Should Be Showing
    Should Be Equal    ${PAGE_STATE}[page]    login

User Logs In With
    [Arguments]    ${username}    ${password}
    Should Not Be Empty    ${username}
    Should Not Be Empty    ${password}
    Set To Dictionary    ${PAGE_STATE}    is_logged_in=${True}
    Set To Dictionary    ${PAGE_STATE}    current_user=${username}
    Set To Dictionary    ${PAGE_STATE}    page=dashboard

Dashboard Should Be Showing
    [Arguments]    ${expected_user}
    Should Be True    ${PAGE_STATE}[is_logged_in]
    Should Be Equal    ${PAGE_STATE}[current_user]    ${expected_user}
    Should Be Equal    ${PAGE_STATE}[page]    dashboard

User Navigates To Profile
    Set To Dictionary    ${PAGE_STATE}    page=profile

User Updates Display Name
    [Arguments]    ${new_name}
    Set To Dictionary    ${PAGE_STATE}    display_name=${new_name}

Profile Name Should Be Updated To
    [Arguments]    ${expected_name}
    Dictionary Should Contain Key    ${PAGE_STATE}    display_name
    Should Be Equal    ${PAGE_STATE}[display_name]    ${expected_name}
```

### Advanced — Config-driven test ที่รองรับหลาย environment

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Library    Collections
Library    OperatingSystem

*** Variables ***
${ENV}             staging    # เปลี่ยนได้ด้วย --variable ENV:production
&{STAGING_CONFIG}  base_url=https://staging.example.com    api_key=stg-key-123    timeout=30
&{PROD_CONFIG}     base_url=https://example.com            api_key=prod-key-456   timeout=10

*** Test Cases ***
API Config Should Match Environment
    [Documentation]    ตรวจสอบว่า config ถูก load ตาม environment
    [Tags]    config    smoke
    ${config}=    Get Config For Environment    ${ENV}
    Should Not Be Empty    ${config}[base_url]
    Should Not Be Empty    ${config}[api_key]
    Should Be True    ${config}[timeout] > 0
    Log    Environment: ${ENV}
    Log    Base URL: ${config}[base_url]
    Log    Timeout: ${config}[timeout]s

Staging Config Should Have Longer Timeout
    [Documentation]    Staging ควรมี timeout ยาวกว่า production
    [Tags]    config    regression
    ${staging}=    Get Config For Environment    staging
    ${prod}=       Get Config For Environment    production
    Should Be True    ${staging}[timeout] > ${prod}[timeout]
    Log    Staging timeout: ${staging}[timeout]s > Prod timeout: ${prod}[timeout]s

*** Keywords ***
Get Config For Environment
    [Documentation]    คืน config dictionary ตาม environment ที่กำหนด
    [Arguments]    ${environment}
    IF    '${environment}' == 'staging'
        RETURN    ${STAGING_CONFIG}
    ELSE IF    '${environment}' == 'production'
        RETURN    ${PROD_CONFIG}
    ELSE
        Fail    Unknown environment: ${environment}
    END
```

Output ที่ควรได้:
```
API Config Should Match Environment                                   | PASS |
Staging Config Should Have Longer Timeout                            | PASS |
2 tests, 2 passed, 0 failed
```

---

## Checklist ก่อน Merge Test Code

ใช้ checklist นี้ review test code ก่อน push:

- [ ] Test case มี `[Documentation]` อธิบายว่า test อะไร
- [ ] Test case มี `[Tags]` อย่างน้อย 1 tag
- [ ] แต่ละ test case เป็นอิสระ ไม่ขึ้นกับ test อื่น
- [ ] Locator เก็บเป็น Variable ไม่ hardcode ใน keyword
- [ ] ไม่มี `Sleep` — ใช้ `Wait For Elements State` แทน
- [ ] Browser ปิดใน `[Teardown]` ไม่ใช่ท้าย test case
- [ ] Test data ที่ใช้ซ้ำอยู่ใน Variable หรือ external file
- [ ] Keyword ชื่อสื่อความหมาย อ่านแล้วรู้ว่าทำอะไร
- [ ] Resource file แยกตาม feature (login, checkout, etc.)

---

## Common Mistakes

❌ **ทุก test ขึ้นกับ state จาก test ก่อนหน้า**
```robot
# ผิด — ถ้า Test 1 fail, Test 2 อาจ pass หรือ fail ไม่แน่นอน
Test 1 - Add Item
    Add To Cart    laptop
Test 2 - Verify Cart
    Cart Should Have Item    laptop    # ขึ้นอยู่กับ Test 1 ผ่านหรือเปล่า?

# ถูก — แต่ละ test manage state ของตัวเอง
Test 2 - Verify Cart
    [Setup]    Add To Cart    laptop
    Cart Should Have Item    laptop
```
*(source: [RF Best Practices - Test Independence](https://docs.robotframework.org/docs/style_guide))*

❌ **ไม่มี CI/CD integration — รัน test แค่บน local machine**
```
# ผิด — ไม่มีหลักฐานว่า test ผ่านบน server จริง
"Test ผ่านในเครื่องฉันนะ"

# ถูก — ทุก PR trigger test อัตโนมัติ
GitHub Actions / GitLab CI / Jenkins → run tests → report
```
*(source: [Continuous Testing Best Practices](https://docs.github.com/en/actions))*

❌ **ตั้งชื่อ test case ไม่สื่อความหมาย**
```robot
# ผิด — ไม่รู้ว่า test อะไร หรือ expect อะไร
Test001
    Login As    admin    pass123

# ถูก — บอก scenario และ expected outcome
Admin Login With Valid Credentials Should Show Dashboard
    Login As    admin    pass123
    Dashboard Should Be Visible
```
*(source: [RF Style Guide - Naming](https://docs.robotframework.org/docs/style_guide#naming-conventions))*

---

## สรุป Course

คุณเรียนจบ Robot Framework สำหรับ QA แล้ว! มาสรุปสิ่งที่เรียนมา:

| บท | สิ่งที่ได้ |
|----|-----------|
| 1 | ทำไม RF, keyword-driven testing, Browser Library vs Selenium |
| 2 | ติดตั้ง Python + RF + Browser Library, first test |
| 3 | 4 Sections, Variables 3 แบบ, Spacing rules |
| 4 | Keywords, Arguments, Resource Files, Page Object |
| 5 | Locators, Browser actions, Wait vs Sleep |
| 6 | Test Template, Data-Driven, FOR loop |
| 7 | Report.html, Log.html, Debug flow |
| 8 | Project structure, Page Object, CI/CD |

**ขั้นต่อไปที่แนะนำ:**
1. ทำ exercise ใน [exercises.md](exercises.md) ให้ครบ
2. ลอง setup โปรเจคจริงกับ app ที่ทีมคุณกำลัง test อยู่
3. เรียนเพิ่มเติม: RequestsLibrary สำหรับ API testing
4. เรียนเพิ่มเติม: AppiumLibrary สำหรับ mobile testing
