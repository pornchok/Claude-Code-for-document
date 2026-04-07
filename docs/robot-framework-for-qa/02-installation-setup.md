# บทที่ 2: ติดตั้งและ Setup

## ก่อนอ่านบทนี้ ลองตอบ:

จากบทที่แล้ว คุณบอกได้ไหมว่า:
- "keyword-driven testing" คืออะไร?
- Browser Library ต่างจาก SeleniumLibrary ยังไง?

---

ลองตอบก่อน แล้วค่อยอ่านต่อ

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:
- ติดตั้ง Python, Robot Framework, และ Browser Library ได้
- สร้าง virtual environment ที่ถูกต้อง
- รัน test แรกของคุณ และเห็น output
- Setup VS Code สำหรับเขียน RF

---

## ทำไมต้องรู้? (Why)

Setup ที่ผิดพลาดคือสาเหตุอันดับหนึ่งที่ทำให้ผู้เริ่มต้นหยุดเรียน ก่อนจะเขียน test ได้ต้องมั่นใจว่า environment พร้อม

สิ่งที่ต้องติดตั้งมีแค่ 3 อย่าง:
1. **Python 3.8+** — ภาษาที่ RF ทำงานอยู่บน
2. **Robot Framework** — ตัว framework
3. **Browser Library** — library สำหรับ web testing

---

## Analogy: Setup เหมือนเตรียม Kitchen ก่อนทำอาหาร

ก่อนทำอาหารได้ต้องมี:
- **เตาแก๊ส** (Python) — แหล่งพลังงานหลัก
- **กระทะ** (Robot Framework) — เครื่องมือหลัก
- **ส่วนผสม** (Library) — สิ่งที่จะทดสอบ

Virtual environment เหมือน **เคาน์เตอร์ครัวแยก** — ทำอาหารแต่ละเมนูในพื้นที่ของตัวเอง ไม่ปนกัน

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
- Python เหมือนเตา ถ้ามีแค่ตัวเดียวก็พอ — จริงๆ ควรมี virtual env แยกกันทุก project เพราะ library version อาจชนกัน
- ส่วนผสม (Library) ต้องซื้อทุกครั้ง — จริงๆ Library บาง package ต้อง init หรือ download browser เพิ่มเติม (เช่น Browser Library)

---

## ขั้นตอนติดตั้ง

### ขั้นที่ 1: ตรวจสอบ Python

```bash
python3 --version
```

ต้องได้ `Python 3.8.x` ขึ้นไป ถ้ายังไม่มีให้ดาวน์โหลดที่ [python.org](https://python.org)

### ขั้นที่ 2: สร้างโปรเจคและ Virtual Environment

```bash
# สร้างโฟลเดอร์โปรเจค
mkdir my-rf-project
cd my-rf-project

# สร้าง virtual environment
python3 -m venv .venv

# เปิดใช้งาน (macOS/Linux)
source .venv/bin/activate

# เปิดใช้งาน (Windows)
.venv\Scripts\activate
```

สังเกตว่า prompt จะเปลี่ยนเป็น `(.venv) $` — แสดงว่า venv เปิดอยู่

### ขั้นที่ 3: ติดตั้ง Robot Framework

```bash
pip install robotframework
robot --version
```

Output ที่ควรได้:
```
Robot Framework 7.4.2 (Python 3.14.2 on darwin)
```

### ขั้นที่ 4: ติดตั้ง Browser Library

```bash
pip install robotframework-browser
rfbrowser init
```

`rfbrowser init` จะดาวน์โหลด browser binaries (Chromium, Firefox, WebKit) ใช้เวลา 2-5 นาที

### ขั้นที่ 5: บันทึก dependencies

```bash
pip freeze > requirements.txt
```

ใครมาทำงานต่อในทีมจะ install ได้ด้วย:
```bash
pip install -r requirements.txt
rfbrowser init
```

---

## Test แรกของคุณ

สร้างไฟล์ `hello_test.robot` ในโฟลเดอร์โปรเจค:

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Test Cases ***
My First Robot Framework Test
    Log    สวัสดี Robot Framework!
    ${message}=    Set Variable    test ผ่านแล้ว
    Should Be Equal    ${message}    test ผ่านแล้ว
    Log    ${message}
```

รันด้วย:
```bash
robot hello_test.robot
```

Output ที่ควรได้:
```
==============================================================================
Hello Test
==============================================================================
My First Robot Framework Test                                         | PASS |
------------------------------------------------------------------------------
Hello Test                                                            | PASS |
1 test, 1 passed, 0 failed
==============================================================================
Output:  /path/to/output.xml
Log:     /path/to/log.html
Report:  /path/to/report.html
```

RF สร้างไฟล์ 3 อย่างอัตโนมัติ:
- `output.xml` — ข้อมูล raw สำหรับ CI/CD
- `log.html` — รายละเอียดทุก step (เปิด browser ดูได้)
- `report.html` — สรุปผล (เปิด browser ดูได้)

---

## ตัวอย่าง 3 ระดับ

### Beginner — เช็คว่า RF ทำงานได้

```robot
# tested: Robot Framework 7.4.2, Python 3.14.2
*** Test Cases ***
Check Variables Work
    ${name}=    Set Variable    จอห์น
    ${age}=     Set Variable    ${30}
    Log    ชื่อ: ${name}, อายุ: ${age}
    Should Be Equal    ${name}    จอห์น
    Should Be Equal As Numbers    ${age}    30

Check List Works
    @{fruits}=    Create List    แอปเปิ้ล    กล้วย    ส้ม
    Length Should Be    ${fruits}    3
    Should Contain    ${fruits}    กล้วย
```

รัน: `robot check_variables.robot`

Output ที่ควรได้:
```
Check Variables Work                                                  | PASS |
Check List Works                                                      | PASS |
2 tests, 2 passed, 0 failed
```

### Intermediate — ใช้กับโปรเจคจริง (requirements.txt)

สร้างไฟล์ `requirements.txt` สำหรับทีม:

```
robotframework==7.4.2
robotframework-browser==19.2.0
```

แล้วทดสอบว่า install ได้จริง:

```bash
# สร้าง environment ใหม่เหมือนสมาชิกทีมคนใหม่
python3 -m venv .venv_test
source .venv_test/bin/activate
pip install -r requirements.txt
rfbrowser init
robot --version
```

Output ที่ควรได้:
```
Robot Framework 7.4.2 (Python 3.14.2 on darwin)
```

### Advanced — โครงสร้างโปรเจคมาตรฐาน

```bash
my-rf-project/
├── tests/
│   ├── smoke/
│   │   └── login_smoke.robot
│   └── regression/
│       └── checkout_tests.robot
├── resources/
│   ├── common.resource
│   └── pages/
│       ├── login_page.resource
│       └── checkout_page.resource
├── data/
│   └── test_users.csv
├── .venv/           ← ไม่ commit (เพิ่มใน .gitignore)
├── requirements.txt ← commit
└── .gitignore
```

`.gitignore` ควรมี:
```
.venv/
output.xml
log.html
report.html
```

---

## Setup VS Code (แนะนำ)

ติดตั้ง extension ชื่อ **Robot Framework Language Server** (publisher: Robocorp)

Extension นี้ให้:
- Syntax highlighting
- Auto-complete keyword
- Error highlighting
- Go-to-definition สำหรับ keyword

---

## Common Mistakes

❌ **ติดตั้ง package โดยไม่เปิด virtual environment**
```bash
# ผิด — ติดตั้ง global ทำให้ project อื่นพัง
pip install robotframework-browser

# ถูก — เปิด venv ก่อนเสมอ
source .venv/bin/activate
pip install robotframework-browser
```
*(source: [Python Virtual Environments docs](https://docs.python.org/3/library/venv.html))*

❌ **ลืมรัน `rfbrowser init` หลัง install**
```
ModuleNotFoundError: No module named 'Browser'
# หรือ
Browser.New Browser ล้มเหลว — browser binary ไม่พบ
```
ต้องรัน `rfbrowser init` ทุกครั้งที่ install ใน environment ใหม่
*(source: [robotframework-browser GitHub](https://github.com/MarketSquare/robotframework-browser))*

❌ **ใช้ Python version เก่า**
Robot Framework 7.x ต้องการ Python 3.8+ ถ้าใช้ Python 2.x จะ error ทันที
*(source: [RF User Guide - Prerequisites](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#prerequisites))*

---

## สรุปบท

ก่อนไปบทต่อไป ลองตอบ (คิด 30 วินาทีก่อนดูเฉลย):

**คำถาม 1:** ทำไมต้องใช้ virtual environment? ถ้าไม่ใช้จะเกิดอะไรขึ้น?

**คำถาม 2:** หลังจากรัน test แล้ว RF สร้างไฟล์อะไรบ้าง และแต่ละไฟล์ใช้ทำอะไร?

**คำถาม 3:** ทำไม Browser Library ถึงต้องรัน `rfbrowser init` เพิ่ม ทั้งที่ pip install ไปแล้ว?

---

<details>
<summary>เฉลย (คลิกเพื่อดู — ดูหลังจากลองตอบแล้วเท่านั้น)</summary>

**เฉลย 1:** Virtual environment แยก package ของแต่ละ project ออกจากกัน ถ้าไม่ใช้ project A ที่ต้องการ RF version 6 กับ project B ที่ต้องการ RF version 7 จะชนกัน อัพเดท package ใน project หนึ่งทำให้อีก project พังได้

**เฉลย 2:** RF สร้าง 3 ไฟล์: `output.xml` (raw data สำหรับ CI), `log.html` (รายละเอียดทุก step เปิดใน browser), `report.html` (สรุปผล pass/fail เปิดใน browser)

**เฉลย 3:** Browser Library ใช้ Playwright เบื้องหลัง `pip install` แค่ติดตั้ง Python package แต่ `rfbrowser init` ต้องดาวน์โหลด browser binary (Chromium, Firefox, WebKit) แยกต่างหาก เพราะ binary เหล่านี้ไม่ได้อยู่ใน pip package

</details>
