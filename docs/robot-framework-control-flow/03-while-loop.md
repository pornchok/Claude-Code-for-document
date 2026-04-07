# บทที่ 3: WHILE Loop, BREAK และ CONTINUE

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อน:
- `FOR    ${i}    IN RANGE    3    9    2` — ค่า `${i}` มีอะไรบ้าง?
- FOR IN ZIP หยุดเมื่อไหร่ถ้า list 2 อันมีความยาวไม่เท่ากัน?

---

เฉลย: 3, 5, 7 | หยุดเมื่อ list ที่สั้นกว่าหมด

---

## วัตถุประสงค์

อ่านจบแล้วทำได้:
- เขียน WHILE loop ที่วนซ้ำตามเงื่อนไข
- ตั้ง `limit` เพื่อป้องกัน infinite loop
- ใช้ BREAK ออกจาก loop กลางคัน
- ใช้ CONTINUE ข้าม iteration ปัจจุบัน

---

## ทำไม WHILE ถึงต้องมีเพิ่มมาจาก FOR?

FOR loop เหมาะกับ "รู้จำนวนรอบล่วงหน้า" แต่บางสถานการณ์ไม่รู้ว่าต้องวนกี่รอบ เช่น:
- รอ element โหลดบนหน้าเว็บ
- Poll API จนกว่า status จะเปลี่ยน
- วนซ้ำจนกว่าผู้ใช้จะ login สำเร็จ

สถานการณ์เหล่านี้ต้องการ WHILE — "ทำต่อไปตราบที่เงื่อนไขยังจริง"

---

## Analogy: พนักงานเฝ้าด่าน

WHILE loop เหมือนพนักงานเฝ้าด่านที่ตรวจรถทุกคัน:
- ก่อนให้รถผ่าน → เช็คเงื่อนไข (`WHILE condition`)
- ถ้าเงื่อนไขจริง → ทำงาน (รับ keyword ข้างใน)
- วนกลับมาเช็คใหม่ทุกรอบ
- ถ้าเงื่อนไขเป็น False → หยุด ออกจาก loop

BREAK เหมือนโทรศัพท์ฉุกเฉินที่บังคับให้หยุดทันที
CONTINUE เหมือนโบกให้รถผ่านไปก่อนโดยไม่ตรวจ แล้วรอรถคันถัดไป

⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า: "WHILE จะรัน loop body ก่อนแล้วค่อยเช็ค" — จริงๆ RF เช็ค condition ก่อนเสมอ ถ้า condition เป็น False ตั้งแต่แรก loop body จะไม่ทำงานเลยแม้แต่รอบเดียว

---

## เนื้อหาหลัก

### 1. WHILE พื้นฐาน

```robotframework
# tested: RF 7.3.2
${counter}=    Set Variable    0
WHILE    ${counter} < 3
    Log    Counter: ${counter}
    ${counter}=    Evaluate    ${counter} + 1
END
```

Output:
```
Counter: 0
Counter: 1
Counter: 2
```

**สำคัญ:** ต้องอัปเดต variable ใน loop body ด้วย ไม่งั้น condition จะเป็น True ตลอดกาล

---

### 2. Limit — ป้องกัน infinite loop

WHILE มี iteration limit default = **10,000** รอบ เมื่อถึงจะ **FAIL** ทันที:

```robotframework
# tested: RF 7.3.2
# กำหนด limit เอง
${counter}=    Set Variable    0
WHILE    True    limit=5
    Log    run ${counter}
    ${counter}=    Evaluate    ${counter} + 1
    IF    ${counter} >= 3    BREAK
END
```

รูปแบบ limit ที่ใช้ได้:

| รูปแบบ | ความหมาย |
|--------|---------|
| `limit=100` | สูงสุด 100 รอบ |
| `limit=10 seconds` | สูงสุด 10 วินาที |
| `limit=NONE` | ไม่มี limit (ระวัง!) |

---

### 3. BREAK — ออกจาก loop ทันที

```robotframework
# tested: RF 7.3.2
FOR    ${i}    IN RANGE    10
    IF    ${i} == 4
        Log    เจอ 4 แล้ว หยุดเลย
        BREAK
    END
    Log    ${i}
END
```

Output:
```
0
1
2
3
เจอ 4 แล้ว หยุดเลย
```

BREAK ทำงานได้ทั้งใน FOR และ WHILE

---

### 4. CONTINUE — ข้าม iteration ปัจจุบัน

```robotframework
# tested: RF 7.3.2
FOR    ${i}    IN RANGE    6
    IF    ${i} % 2 == 0    CONTINUE
    Log    เลขคี่: ${i}
END
```

Output:
```
เลขคี่: 1
เลขคี่: 3
เลขคี่: 5
```

CONTINUE ข้าม keyword ที่เหลือใน iteration นั้น แล้วไปเริ่ม iteration ถัดไปทันที

---

### 5. ตัวอย่าง Production-grade: รอ element โหลด

```robotframework
# tested: RF 7.3.2 (ใช้ built-in keywords เท่านั้น)
Wait Until Page Ready
    [Documentation]    Poll จนกว่า element จะพร้อม (สูงสุด 30 วินาที)
    ${found}=    Set Variable    False
    WHILE    '${found}' == 'False'    limit=30 seconds
        ${status}=    Run Keyword And Return Status
        ...    Element Should Be Visible    id=main-content
        IF    ${status}
            ${found}=    Set Variable    True
        ELSE
            Sleep    1s
        END
    END
```

---

## Common Mistakes

❌ **ลืมอัปเดต condition variable:**
```robotframework
${x}=    Set Variable    0
WHILE    ${x} < 10
    Log    วนตลอดกาล!
    # ลืม update ${x} → infinite loop → FAIL ที่ 10,000
END
```
✅ อัปเดตตัวแปรใน loop body เสมอ *(source: RF User Guide)*

---

❌ **คิดว่า `limit=NONE` ปลอดภัย:**
`WHILE    True    limit=NONE` → loop ไม่มีจุดหยุด ถ้าเงื่อนไขไม่เปลี่ยน test จะค้างไปเรื่อยๆ
✅ ใช้ `limit=NONE` เฉพาะเมื่อมั่นใจว่า BREAK จะทำงาน *(source: RF User Guide)*

---

❌ **ใช้ BREAK/CONTINUE นอก loop:**
```robotframework
BREAK    # ← error! ต้องอยู่ภายใน FOR หรือ WHILE
```
✅ BREAK และ CONTINUE ใช้ได้เฉพาะภายใน loop body เท่านั้น *(source: RF User Guide)*

---

## สรุปบท

ก่อนดูเฉลย ลองตอบก่อน:

**คำถาม 1:** ถ้า WHILE condition เป็น `False` ตั้งแต่เริ่มต้น loop body จะทำงานกี่รอบ?

**คำถาม 2:** ต่างกันอย่างไรระหว่าง BREAK กับ CONTINUE?

**คำถาม 3:** เมื่อไหร่ควรใช้ WHILE แทน FOR? ยกตัวอย่างสถานการณ์จริง 1 อย่าง

---

<details>
<summary>เฉลย (คลิกเพื่อดู)</summary>

**เฉลย 1:** 0 รอบ — RF เช็ค condition ก่อนเสมอ ถ้า False ตั้งแต่แรก ข้ามทั้ง loop ไปเลย

**เฉลย 2:** BREAK หยุด loop ทั้งหมด ออกจาก loop ทันที | CONTINUE หยุดเฉพาะ iteration ปัจจุบัน แล้ววนไปเริ่ม iteration ถัดไป

**เฉลย 3:** ใช้ WHILE เมื่อไม่รู้จำนวนรอบล่วงหน้า เช่น: รอ background job เสร็จ, poll API จนได้ status = "completed", รอ element หายจากหน้าเว็บ

</details>
