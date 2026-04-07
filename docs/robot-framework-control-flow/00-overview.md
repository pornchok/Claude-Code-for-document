# Control Flow ใน Robot Framework

**เวอร์ชัน:** Robot Framework 5.0+ (ทุก syntax ในเอกสารนี้ใช้ได้กับ RF 5.0 ขึ้นไป)
**ทดสอบจริงกับ:** RF 7.3.2 / Python 3.14.0

---

## Prerequisites

### 1. โครงสร้างพื้นฐานของ RF

ไฟล์ `.robot` แบ่งเป็น section ด้วย `*** ... ***`:

```robotframework
*** Settings ***
Library    Collections          # import library เพิ่มเติม

*** Variables ***
${NAME}    Alice                # ตัวแปร global

*** Keywords ***
Say Hello
    [Arguments]    ${name}      # keyword รับ argument
    Log    Hello, ${name}!      # สั่งให้ทำอะไรบางอย่าง

*** Test Cases ***
My First Test
    Say Hello    Bob            # เรียกใช้ keyword
```

- **Test Case** คือหน่วยทดสอบ 1 ชุด — RF จะรันทีละ Test Case
- **Keyword** คือ "คำสั่งที่นิยามเอง" เหมือน function — เรียกซ้ำได้
- **`*** Settings ***`** ใช้ import Library หรือตั้งค่า suite

---

### 2. ตัวแปร `${variable}` และ `@{list}`

RF มีตัวแปรสองรูปแบบหลักที่ใช้บ่อยใน control flow:

**`${variable}` — ตัวแปรทั่วไป** (string, number, boolean, หรือ object ใดๆ)

```robotframework
${score}=    Set Variable    85        # เก็บตัวเลข
${name}=     Set Variable    Alice     # เก็บ string
${flag}=     Set Variable    ${True}   # เก็บ boolean
```

**`@{list}` — ตัวแปรแบบ list** (เก็บหลายค่า)

```robotframework
@{fruits}=    Create List    apple    banana    cherry
Log    ${fruits}[0]    # → apple (เข้าถึงด้วย index)
```

**ความแตกต่างสำคัญ:** `@{fruits}` ใน FOR loop จะ **unpack** รายการออกมา แต่ `${fruits}` จะส่ง list ทั้งก้อนเป็น item เดียว — เรื่องนี้จะเจออีกครั้งในบทที่ 2

---

### 3. Built-in Keywords ที่ใช้บ่อยในเอกสารนี้

| Keyword | ทำอะไร | ตัวอย่าง |
|---------|---------|---------|
| `Log` | print ข้อความลง log | `Log    Hello World` |
| `Set Variable` | กำหนดค่าให้ตัวแปร | `${x}=    Set Variable    10` |
| `Evaluate` | คำนวณ Python expression | `${sum}=    Evaluate    ${a} + ${b}` |
| `Should Be Equal` | assert ว่าค่าสองอันเท่ากัน | `Should Be Equal    ${x}    ${y}` |
| `Create List` | สร้าง list ใหม่ | `${items}=    Create List    a    b    c` |
| `Get Length` | นับจำนวน item ใน list | `${len}=    Get Length    ${items}` |
| `Run Keyword And Return Status` | รัน keyword แล้วคืน True/False | `${ok}=    Run Keyword And Return Status    Should Be Equal    1    1` |

> `Evaluate` เป็น keyword ที่สำคัญมากใน WHILE loop เพราะใช้คำนวณค่าใหม่ทุกรอบ เช่น `${counter}=    Evaluate    ${counter} + 1`

---

## เอกสารนี้ครอบคลุมอะไร

| บท | หัวข้อ | ใช้เมื่อ |
|----|--------|---------|
| [01 — IF / ELSE](01-if-else.md) | เงื่อนไข: IF, ELSE IF, ELSE, Inline IF | ต้องการทำอะไรบางอย่าง "เฉพาะเมื่อ" |
| [02 — FOR Loop](02-for-loop.md) | IN, IN RANGE, IN ENUMERATE, IN ZIP | ต้องวนซ้ำตาม list หรือตัวเลข |
| [03 — WHILE + BREAK + CONTINUE](03-while-loop.md) | WHILE, BREAK, CONTINUE | วนซ้ำจนกว่าเงื่อนไขจะเปลี่ยน |
| [แบบฝึกหัด](exercises.md) | โจทย์ Recall → Application → Synthesis | ฝึกใช้จริง |
| [Glossary](glossary.md) | คำศัพท์สำคัญ | เปิดดูอ้างอิง |

---

## กฎสำคัญที่ต้องจำก่อนเริ่ม

**1. ทุก keyword ต้องเป็น UPPERCASE:**
```
IF  ELSE IF  ELSE  FOR  IN  IN RANGE  IN ENUMERATE  IN ZIP  WHILE  BREAK  CONTINUE  END
```

**2. ทุก block ต้องปิดด้วย `END`:**
```robotframework
IF    ${cond}
    Log    something
END    # ← ขาดไม่ได้
```

**3. Indent 4 spaces ภายใน block:**
```robotframework
FOR    ${i}    IN RANGE    3
    Log    ${i}    # ← 4 spaces
END
```

**4. Condition ใน IF/WHILE ใช้ Python expression:**
```robotframework
IF    ${x} > 10 and ${y} < 5    # Python expression ปกติ
IF    '${name}' == 'Alice'       # string ต้องใส่ quote
```
