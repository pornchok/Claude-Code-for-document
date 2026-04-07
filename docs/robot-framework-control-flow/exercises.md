# แบบฝึกหัด: Control Flow ใน Robot Framework

## วิธีใช้
- **Recall** — อธิบายด้วยคำตัวเอง ไม่ต้องเขียนโค้ด
- **Application** — เขียนโค้ดที่ run ได้จริง (test กับ `robot` command)
- **Synthesis** — วิเคราะห์ / หา bug / ออกแบบ

---

## ส่วนที่ 1: IF / ELSE

### [Recall] ข้อ 1

ถ้าเขียน IF/ELSE IF/ELSE โดยเรียง condition จากน้อยไปมาก เช่น:

```robotframework
IF    ${temp} > 10
    Log    อุ่น
ELSE IF    ${temp} > 30
    Log    ร้อน
END
```

ถ้า `${temp} = 35` จะ log อะไร? มีปัญหาอะไรกับโค้ดนี้ไหม? อธิบายด้วยคำตัวเอง

---

### [Application] ข้อ 2

เขียน keyword ชื่อ `Classify File Size` ที่รับ argument `${size_mb}` แล้ว log ว่า:
- น้อยกว่า 1 MB → "Small"
- 1–100 MB → "Medium"
- มากกว่า 100 MB → "Large"

พร้อมเขียน test case ที่ call keyword นี้ด้วย 3 ค่าที่ครอบคลุมทุก branch
(ห้ามใช้ค่า 90, 85, 75 จากตัวอย่างในบทที่ 1)

---

### [Synthesis] ข้อ 3

ดูโค้ดต่อไปนี้แล้วหา bug:

```robotframework
Check Login Status
    [Arguments]    ${username}    ${role}
    IF    ${role} == admin
        Log    Welcome Admin
    ELSE IF    ${role} == user
        Log    Welcome User
    ELSE
        Log    Unknown role
    END
```

มีปัญหากี่จุด? แต่ละจุดเกิดอะไรขึ้น? เขียนโค้ดที่แก้แล้วด้วย

---

## ส่วนที่ 2: FOR Loop

### [Recall] ข้อ 4

อธิบายให้เพื่อนที่ไม่รู้จัก RF ฟังว่า FOR IN ENUMERATE ต่างจาก FOR IN อย่างไร และควรใช้เมื่อไหร่ (ใช้ภาษาไทยธรรมดา ไม่ต้องมีโค้ด)

---

### [Application] ข้อ 5

มี `@{cities}` = `["Bangkok", "Chiang Mai", "Phuket", "Khon Kaen"]`

เขียน FOR loop ที่ log ออกมาแบบนี้:
```
เมืองที่ 1: Bangkok
เมืองที่ 2: Chiang Mai
เมืองที่ 3: Phuket
เมืองที่ 4: Khon Kaen
```
(index เริ่มที่ 1 ไม่ใช่ 0)

---

### [Synthesis] ข้อ 6

ออกแบบ keyword ชื่อ `Validate Users` ที่รับ 2 list: `${usernames}` และ `${expected_roles}` แล้ว log ว่า username แต่ละคนมี role ถูกต้องหรือไม่ เปรียบเทียบ list คู่ขนาน

ข้อ challenge: ถ้า list ทั้ง 2 มีความยาวต่างกัน ควรจัดการอย่างไร? (อย่าให้ test ผ่านโดยไม่รู้ตัว)

---

## ส่วนที่ 3: WHILE + BREAK + CONTINUE

### [Recall] ข้อ 7

อธิบายว่า `limit` parameter ของ WHILE มีไว้ทำอะไร ถ้าไม่ใส่จะเกิดอะไรขึ้นเมื่อ condition ไม่เคยเป็น False?

---

### [Application] ข้อ 8

เขียน keyword ชื่อ `Find First Even` ที่รับ `@{numbers}` แล้ว:
- วน loop ผ่านทุกตัวเลข
- ถ้าเจอเลขคู่ → log ว่า "Found even: [number]" แล้วหยุด
- ถ้าไม่มีเลขคู่เลย → log ว่า "No even number found"

ทดสอบกับ `${1}    ${3}    ${7}    ${4}    ${9}` (expect: หยุดที่ 4)

---

### [Synthesis] ข้อ 9

ดูโค้ดต่อไปนี้แล้วตอบ:

```robotframework
Retry Until Success
    [Arguments]    ${max_attempts}
    ${attempt}=    Set Variable    1
    WHILE    ${attempt} <= ${max_attempts}
        ${result}=    Run Keyword And Return Status    Flaky Keyword
        IF    ${result}    BREAK
        ${attempt}=    Evaluate    ${attempt} + 1
    END
    Should Be True    ${result}    Keyword failed after ${max_attempts} attempts
```

1. ถ้า `Flaky Keyword` สำเร็จในรอบที่ 2 จาก 5 — `${attempt}` จะเป็นค่าอะไรหลัง BREAK?
2. ถ้า `Flaky Keyword` ล้มเหลวทุกรอบ — `Should Be True` จะได้รับค่าอะไร? test จะ pass หรือ fail?
3. มีจุดไหนที่อาจเป็น bug ไหม? (hint: ลองคิดถึงกรณีที่ `max_attempts = 0`)

---

## เฉลย

<details>
<summary>เฉลยข้อ 1</summary>

log "อุ่น" — เพราะ `35 > 10` เป็น True ตั้งแต่ condition แรก RF จะข้าม ELSE IF ไป

ปัญหา: ไม่มีทางได้ "ร้อน" เลย เพราะ condition ที่กว้างกว่า (`> 10`) อยู่ก่อน ควรเรียงจาก "เข้มงวดที่สุด" ก่อน:
```robotframework
IF    ${temp} > 30
    Log    ร้อน
ELSE IF    ${temp} > 10
    Log    อุ่น
END
```

</details>

<details>
<summary>เฉลยข้อ 2</summary>

```robotframework
*** Keywords ***
Classify File Size
    [Arguments]    ${size_mb}
    IF    ${size_mb} < 1
        Log    Small
    ELSE IF    ${size_mb} <= 100
        Log    Medium
    ELSE
        Log    Large
    END

*** Test Cases ***
Test File Size Classification
    Classify File Size    0.5
    Classify File Size    50
    Classify File Size    200
```

</details>

<details>
<summary>เฉลยข้อ 3</summary>

**Bug 2 จุด:**

1. `${role} == admin` → ควรเป็น `'${role}' == 'admin'` — ขาด quote รอบ string ทั้งสองฝั่ง
2. `${role} == user` → เช่นเดียวกัน

โค้ดที่แก้แล้ว:
```robotframework
Check Login Status
    [Arguments]    ${username}    ${role}
    IF    '${role}' == 'admin'
        Log    Welcome Admin
    ELSE IF    '${role}' == 'user'
        Log    Welcome User
    ELSE
        Log    Unknown role
    END
```

</details>

<details>
<summary>เฉลยข้อ 4</summary>

FOR IN วนผ่าน list ปกติ เราได้แค่ตัว item เอง — ไม่รู้ว่า item อยู่ตำแหน่งที่เท่าไหร่

FOR IN ENUMERATE เพิ่มตัวแปร index มาให้ด้วย เหมือนมีป้ายเลขกำกับทุก item

ใช้ ENUMERATE เมื่อต้องการรู้ว่า item อยู่แถวที่เท่าไหร่ — เช่น print "ข้อที่ 1/2/3" หรือ check ข้อมูลตามลำดับแถวในตาราง

</details>

<details>
<summary>เฉลยข้อ 5</summary>

```robotframework
*** Test Cases ***
Test City List
    @{cities}=    Create List    Bangkok    Chiang Mai    Phuket    Khon Kaen
    FOR    ${index}    ${city}    IN ENUMERATE    @{cities}    start=1
        Log    เมืองที่ ${index}: ${city}
    END
```

</details>

<details>
<summary>เฉลยข้อ 6</summary>

```robotframework
*** Keywords ***
Validate Users
    [Arguments]    ${usernames}    ${expected_roles}
    ${len_users}=      Get Length    ${usernames}
    ${len_roles}=      Get Length    ${expected_roles}
    Should Be Equal As Integers    ${len_users}    ${len_roles}
    ...    List length mismatch: ${len_users} users vs ${len_roles} roles
    FOR    ${username}    ${expected}    IN ZIP    ${usernames}    ${expected_roles}
        Log    ${username}: expected=${expected}
    END
```

Bug prevention: `Should Be Equal As Integers` ก่อน ZIP เพื่อ fail fast ถ้า list ไม่เท่ากัน — แทนที่จะวนแค่ list ที่สั้นกว่าโดยไม่รู้ตัว

</details>

<details>
<summary>เฉลยข้อ 7</summary>

`limit` กำหนดจำนวนรอบสูงสุดที่ WHILE จะวน ถ้าไม่ใส่ default คือ 10,000 รอบ

ถ้า condition ไม่เคยเป็น False และไม่มี BREAK → loop จะวนจนครบ limit แล้ว **FAIL** ทันที (ไม่ใช่ค้างตลอดไป)

</details>

<details>
<summary>เฉลยข้อ 8</summary>

```robotframework
*** Keywords ***
Find First Even
    [Arguments]    @{numbers}
    ${found}=    Set Variable    False
    FOR    ${num}    IN    @{numbers}
        ${is_even}=    Evaluate    ${num} % 2 == 0
        IF    ${is_even}
            Log    Found even: ${num}
            ${found}=    Set Variable    True
            BREAK
        END
    END
    IF    '${found}' == 'False'
        Log    No even number found
    END

*** Test Cases ***
Test Find First Even
    Find First Even    ${1}    ${3}    ${7}    ${4}    ${9}
```

Output: `Found even: 4`

</details>

<details>
<summary>เฉลยข้อ 9</summary>

1. `${attempt}` = 2 — BREAK หยุด loop แต่ `${attempt}` ยังมีค่าของ iteration ที่ BREAK เกิดขึ้น

2. `${result}` = False → `Should Be True    False` → **test FAIL** พร้อมข้อความ "Keyword failed after N attempts" — ซึ่งถูกต้อง

3. Bug: ถ้า `max_attempts = 0` → `WHILE    1 <= 0` = False ตั้งแต่แรก loop ไม่ทำงานเลย แต่ `${result}` ยังไม่ถูก assign → `Should Be True    ${result}` จะ error เพราะ `${result}` ไม่มีค่า

Fix: initialize `${result}=    Set Variable    False` ก่อน WHILE

</details>
