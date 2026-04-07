# บทที่ 7: Reports และ Debugging

## ก่อนอ่านบทนี้ ลองตอบ:

- Test Template ใช้ตอนไหน? ต่างจาก test case ปกติอย่างไร?
- `Run Keyword And Continue On Failure` ต่างจากการเรียก keyword ตรงๆ อย่างไร?

---

ลองตอบก่อน แล้วค่อยอ่านต่อ

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:
- รัน test ด้วย options ต่างๆ ที่ใช้บ่อย (tags, output path, parallel)
- อ่านและใช้ประโยชน์จาก `log.html` และ `report.html`
- Debug test ที่ fail ได้อย่างเป็นระบบ
- ใช้ `Log` และ `Take Screenshot` เพื่อช่วย debug

---

## ทำไมต้องรู้? (Why)

Test automation ที่ดีไม่ใช่แค่เขียน test ผ่าน — มันคือความสามารถใน **อ่านผลและ debug ได้เร็ว** เมื่อ test fail

เมื่อ test fail มีสองสาเหตุหลัก:
1. **Bug จริงๆ** — application มีปัญหา → report ให้ developer แก้
2. **Test เสีย (flaky test)** — test ผิด locator หมดอายุ หรือ timing issue → QA ต้องแก้

ถ้าอ่าน report ไม่เป็น จะแยกไม่ออก และเสียเวลามาก

---

## Analogy: Report ของ RF เหมือน Flight Black Box

เครื่องบินทุกลำมี Black Box บันทึกทุกอย่างที่เกิดขึ้น — RF ทำเหมือนกัน:
- `log.html` = Black Box รายละเอียด บันทึกทุก step, ทุก keyword call, ทุก screenshot
- `report.html` = สรุปเที่ยวบิน — pass/fail, เวลา, tag breakdown

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
- Log บันทึกทุกอย่างอัตโนมัติเหมือน Black Box ไม่ต้องทำอะไร — จริงๆ Log เบื้องต้นดี แต่ถ้าอยากดู state เพิ่มเติม ต้องเพิ่ม `Log` keyword หรือ `Take Screenshot` เองด้วย

---

## คำสั่ง `robot` ที่ใช้บ่อย

### Run ทุก test ใน directory

```bash
robot tests/
```

### Run เฉพาะ tag

```bash
# Run เฉพาะ smoke test
robot --include smoke tests/

# Run ทุกอย่างยกเว้น slow
robot --exclude slow tests/

# Run เฉพาะ smoke AND login (ต้องมีทั้งสอง tag)
robot --include smokeANDlogin tests/

# Run smoke OR login (มีอย่างน้อยหนึ่ง tag)
robot --include smokeORlogin tests/
```

### กำหนด output path

```bash
robot --output results/output.xml \
      --log results/log.html \
      --report results/report.html \
      tests/
```

### Run แบบระบุ test ด้วยชื่อ

```bash
robot --test "Valid Login Shows Success Message" tests/
```

### Dry run (ตรวจสอบ syntax โดยไม่รันจริง)

```bash
robot --dryrun tests/
```

---

## อ่าน Report.html

`report.html` เปิดใน browser ได้ มีส่วนสำคัญ:

```
Report
├── Statistics
│   ├── Total: 25 tests, 23 passed, 2 failed
│   ├── Tags breakdown: smoke(10/10), login(5/5), checkout(8/6)
│   └── Suites breakdown: tests/login(5/5), tests/checkout(8/6)
│
└── All Tests
    ├── ✓ Valid Login Shows Success Message
    ├── ✓ Invalid Login Shows Error
    ├── ✗ Checkout With Expired Card      ← click เพื่อดูรายละเอียด
    └── ...
```

**สิ่งที่ดูใน report:**
- กี่ test pass/fail
- fail test อยู่ใน suite/tag ไหน (ช่วย identify pattern)
- เวลารวมทั้งหมด

---

## อ่าน Log.html

`log.html` คือ tool หลักสำหรับ debug — มีทุก step ที่รัน

```
▼ Valid Login Shows Success Message                                  PASS
  ▼ Open Login Page
    ► New Browser chromium headless=True                             OK
    ► New Page https://example.com/login                             OK
    ► Wait For Elements State id:username visible                    OK
  ▼ Enter Credentials testuser pass123
    ► Fill Text id:username testuser                                 OK
    ► Fill Text id:password pass123                                  OK
    ► Click id:login-btn                                             OK
  ▼ Login Success Should Be Shown                                   FAIL ←
    ► Wait For Elements State css:.flash.success visible timeout=10s FAIL
      Error: Element 'css:.flash.success' was not visible after 10s
```

**วิธีอ่าน log เมื่อ fail:**
1. หา test case ที่ fail (สีแดง)
2. เปิด step สุดท้ายที่ OK และ step แรกที่ FAIL
3. อ่าน error message ให้ครบ
4. ดู screenshot (ถ้ามี)

---

## Debug อย่างเป็นระบบ

### Step 1: อ่าน error message ใน log.html

```
Error: Element 'css:.flash.success' was not visible after 10s
```

→ เป็นเรื่อง locator หรือ timing?

### Step 2: ตรวจสอบ locator

```bash
# เปิด browser แบบ headful (เห็นหน้าจอ)
robot --variable HEADLESS:False tests/

# หรือแก้ใน resource file ชั่วคราว
${HEADLESS}    ${False}
```

เปิด DevTools กด `Ctrl+F` หรือ `F12` → Console → ทดสอบ CSS selector:
```javascript
document.querySelectorAll('css:.flash.success')
```

### Step 3: เพิ่ม Log ระหว่าง step

```robot
Login Should Succeed
    Go To Login Page
    Log    === กำลัง enter credentials ===
    Enter Credentials    ${username}    ${password}
    Log    === click แล้ว รอ response ===
    Take Screenshot    filename=after_login.png    # ดูว่า UI เป็นยังไง
    Wait For Elements State    css:.flash.success    visible    timeout=15s
```

### Step 4: ใช้ RF Debugger (เมื่อยังหาไม่เจอ)

```bash
pip install robotframework-debuglibrary
```

```robot
*** Settings ***
Library    DebugLibrary

*** Test Cases ***
Debug My Test
    Go To Login Page
    Debug    # ← หยุดตรงนี้ เปิด interactive shell
```

---

## ตัวอย่าง 3 ระดับ

### Beginner — เพิ่ม Log ให้ test อ่านง่ายขึ้น

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Library    Collections

*** Test Cases ***
Shopping Cart Calculation With Logs
    [Documentation]    คำนวณ cart พร้อม log แต่ละขั้นตอน
    [Tags]    cart    smoke
    Log    === เริ่มต้น test: Shopping Cart ===
    ${items}=    Create List    299    599    149
    Log    Items ใน cart: ${items}
    ${subtotal}=    Calculate Total    ${items}
    Log    Subtotal: ${subtotal} บาท
    ${tax}=    Evaluate    ${subtotal} * 0.07
    Log    Tax (7%): ${tax} บาท
    ${total}=    Evaluate    ${subtotal} + ${tax}
    Log    Total: ${total} บาท
    Should Be Equal As Numbers    ${subtotal}    1047
    Should Be Equal As Numbers    ${total}    1120.29
    Log    === test ผ่านแล้ว ===

*** Keywords ***
Calculate Total
    [Arguments]    ${prices}
    ${total}=    Evaluate    sum(int(x) for x in ${prices})
    RETURN    ${total}
```

Output ที่ควรได้:
```
Shopping Cart Calculation With Logs                                   | PASS |
1 test, 1 passed, 0 failed
```

### Intermediate — Tags และ conditional execution

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Library    Collections

*** Variables ***
${RUN_SLOW_TESTS}    ${False}    # เปลี่ยนเป็น True เพื่อรัน slow tests

*** Test Cases ***
Quick Validation Test
    [Documentation]    Test เร็ว รันทุกครั้ง
    [Tags]    smoke    fast
    ${result}=    Validate Input    test@example.com    email
    Should Be True    ${result}

Comprehensive Validation Test
    [Documentation]    Test ละเอียด รัน slow tests เท่านั้น
    [Tags]    regression    slow
    Skip If    not ${RUN_SLOW_TESTS}    Skipping slow test (set RUN_SLOW_TESTS=True to run)
    FOR    ${email}    IN
    ...    valid@example.com
    ...    another@test.co.th
    ...    third.user+tag@domain.org
        ${result}=    Validate Input    ${email}    email
        Should Be True    ${result}
    END

*** Keywords ***
Validate Input
    [Arguments]    ${value}    ${type}
    IF    '${type}' == 'email'
        Should Match Regexp    ${value}    ^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$
        RETURN    ${True}
    ELSE
        Fail    Unknown validation type: ${type}
    END
```

รันเฉพาะ smoke:
```bash
robot --include smoke tests/
# → Quick Validation Test รัน
# → Comprehensive Validation Test ไม่รัน
```

### Advanced — Custom Report Keyword

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Settings ***
Library    Collections
Library    DateTime

Suite Setup    Start Test Report
Suite Teardown    Print Test Report Summary

*** Variables ***
@{TEST_RESULTS}    # เก็บผล test แต่ละ case

*** Test Cases ***
Order Processing Smoke Test
    [Tags]    smoke    order
    ${passed}=    Run Keyword And Return Status
    ...    Verify Order Processing    ORD-001    1500.00    pending
    Record Test Result    Order Processing    ${passed}

Payment Validation Smoke Test
    [Tags]    smoke    payment
    ${passed}=    Run Keyword And Return Status
    ...    Verify Payment    PAY-001    1500.00    credit_card
    Record Test Result    Payment Validation    ${passed}

*** Keywords ***
Start Test Report
    ${timestamp}=    Get Current Date    result_format=%Y-%m-%d %H:%M:%S
    Log    \n=== TEST REPORT ===\nเริ่มต้น: ${timestamp}\n    console=True

Record Test Result
    [Arguments]    ${test_name}    ${passed}
    ${status}=    Set Variable If    ${passed}    PASS    FAIL
    Append To List    ${TEST_RESULTS}    ${test_name}: ${status}
    Log    ${test_name}: ${status}

Print Test Report Summary
    Log    \n=== SUMMARY ===    console=True
    FOR    ${result}    IN    @{TEST_RESULTS}
        Log    ${result}    console=True
    END

Verify Order Processing
    [Arguments]    ${order_id}    ${amount}    ${status}
    Should Not Be Empty    ${order_id}
    Should Be True    ${amount} > 0
    Should Be Equal    ${status}    pending

Verify Payment
    [Arguments]    ${payment_id}    ${amount}    ${method}
    Should Not Be Empty    ${payment_id}
    Should Be True    ${amount} > 0
    Should Contain    credit_card,debit_card,promptpay    ${method}
```

---

## Common Mistakes

❌ **ไม่ใช้ Tags ทำให้ run เฉพาะ test ไม่ได้**
```robot
# ผิด — ไม่มี tags
Login Test
    Login As    admin    admin123

# ถูก — มี tags ที่มีความหมาย
Login Test
    [Tags]    login    smoke    critical
    Login As    admin    admin123
```
*(source: [RF User Guide - Tagging Tests](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#tagging-test-cases))*

❌ **ไม่เพิ่ม Screenshot เมื่อ web test fail**
```robot
# ผิด — fail แล้วไม่รู้ UI เป็นยังไง
My Web Test
    Click    id:checkout-btn
    # ถ้า fail ไม่มีหลักฐาน

# ถูก — เพิ่ม Take Screenshot ใน Teardown
My Web Test
    Click    id:checkout-btn
    [Teardown]    Run Keywords
    ...    Run Keyword If Test Failed    Take Screenshot    fullPage=True
    ...    AND    Close Browser
```
*(source: [Browser Library - Take Screenshot](https://marketsquare.github.io/robotframework-browser/Browser.html#Take%20Screenshot))*

❌ **ดู report.html แล้วสรุปว่า test pass = ไม่มี bug**
```
# ผิด — mindset
"test pass หมด แปลว่า ไม่มี bug"

# ถูก — mindset
"test pass แปลว่า test case ที่เขียนไว้ pass
แต่ test coverage ครอบคลุมแค่ไหน?
มี edge case ที่ยังไม่ได้เขียนไหม?"
```
*(source: [Test Automation Best Practices - Testing Mindset](https://martinfowler.com/articles/practical-test-pyramid.html))*

---

## สรุปบท

ก่อนไปบทต่อไป ลองตอบ (คิด 30 วินาทีก่อนดูเฉลย):

**คำถาม 1:** ความแตกต่างระหว่าง `report.html` และ `log.html` คืออะไร? ใช้อันไหนทำอะไร?

**คำถาม 2:** ถ้า web test fail ด้วย error "Element not visible after 10s" จะมีขั้นตอน debug อย่างไร?

**คำถาม 3:** ถ้าอยากรัน test เฉพาะที่ tag ว่า `smoke` แต่ไม่อยากรัน tag `slow` ใช้ command อะไร?

---

<details>
<summary>เฉลย (คลิกเพื่อดู — ดูหลังจากลองตอบแล้วเท่านั้น)</summary>

**เฉลย 1:** `report.html` คือสรุปภาพรวม — ดูว่ากี่ test pass/fail, pattern ของความล้มเหลว ใช้ share กับทีมหรือ stakeholder `log.html` คือรายละเอียดทุก step — ใช้ debug ว่า fail ที่ step ไหน ด้วย error อะไร มี screenshot อะไร

**เฉลย 2:** (1) อ่าน error ใน log.html ดู step สุดท้ายที่ fail, (2) รัน test แบบ headful (`--variable HEADLESS:False`) เพื่อเห็นหน้าจอ, (3) เพิ่ม `Take Screenshot` หลัง action ที่น่าสงสัย, (4) ตรวจสอบ locator ใน DevTools ว่ายัง match อยู่ไหม, (5) เพิ่ม timeout หรือตรวจสอบว่า app ช้าผิดปกติไหม

**เฉลย 3:** `robot --include smoke --exclude slow tests/`

</details>
