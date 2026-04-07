# บทที่ 1: IF / ELSE IF / ELSE และ Inline IF

## วัตถุประสงค์

อ่านจบแล้วทำได้:
- เขียน IF block ที่ทำงานตามเงื่อนไข
- ต่อ ELSE IF และ ELSE สำหรับหลายกรณี
- ใช้ Inline IF เมื่อต้องการโค้ดสั้นกว่า
- หลีกเลี่ยง mistake ที่พบบ่อยที่สุดใน IF

---

## ทำไมต้องมี IF?

ใน test จริง ผลลัพธ์ที่คาดหวังมักขึ้นอยู่กับบริบท เช่น:
- ถ้า environment คือ production → check ค่าหนึ่ง
- ถ้าเป็น staging → check อีกค่าหนึ่ง

ถ้าไม่มี IF เราต้องเขียน test แยกทุก case — ซ้ำซ้อนมาก

---

## Analogy: ไฟจราจร

IF/ELSE เหมือนไฟจราจร:
- ไฟเขียว (IF true) → รถวิ่ง
- ไฟเหลือง (ELSE IF) → ชะลอ
- ไฟแดง (ELSE) → หยุด

โค้ดอ่านเงื่อนไขจากบนลงล่าง เจอ condition แรกที่จริง → ทำ แล้วข้ามอันที่เหลือทั้งหมด

⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:
- "ไฟจราจรรัน case เดียวเสมอ" — ถูกต้องสำหรับ IF/ELSE แต่ **ไม่เหมือน** กับ keyword แต่ละอัน เพราะ RF ยังคงรัน keyword ที่อยู่นอก IF block ต่อไปหลัง END เสมอ

---

## เนื้อหาหลัก

### 1. IF Block พื้นฐาน

```robotframework
# tested: RF 7.3.2
IF    ${score} >= 90
    Log    ผ่านดีมาก
END
```

**กฎ:** ต้องมี `END` ปิดทุกครั้ง ถ้าไม่มี RF จะ error

---

### 2. IF + ELSE IF + ELSE

```robotframework
# tested: RF 7.3.2
${score}=    Set Variable    75

IF    ${score} >= 90
    Log    A
ELSE IF    ${score} >= 80
    Log    B
ELSE IF    ${score} >= 70
    Log    C
ELSE
    Log    F
END
```

Output: `C`

**สำคัญ:** RF ประเมินจากบนลงล่าง เจอ condition แรกที่ `True` → รัน แล้วข้าม ELSE IF/ELSE ที่เหลือทั้งหมด

---

### 3. Condition กับตัวแปรสตริง

เมื่อ compare string ต้องใส่ single quote รอบ `${variable}`:

```robotframework
# tested: RF 7.3.2
${env}=    Set Variable    production

IF    '${env}' == 'production'
    Log    กำลังทดสอบบน Production!
ELSE
    Log    Environment: ${env}
END
```

❌ **ผิด:** `IF    ${env} == 'production'` → RF อาจ parse ผิดเพราะ `${env}` อาจถูก resolve เป็น string แล้วเปรียบเทียบกับ literal โดยตรง ซึ่งบางกรณีทำงานได้ แต่บางกรณีไม่ได้
✅ **ถูก:** ใส่ quote ทั้งสองฝั่ง: `'${env}' == 'production'` *(source: RF User Guide)*

---

### 4. Inline IF (RF 5.0+)

ใช้เมื่อต้องการรัน **keyword เดียว** ตามเงื่อนไข — ไม่ต้องมี END:

```robotframework
# tested: RF 7.3.2
${x}=    Set Variable    10

IF    ${x} > 5    Log    x มากกว่า 5
```

Inline IF มี ELSE ได้ด้วย:

```robotframework
IF    ${x} > 5    Log    big    ELSE    Log    small
```

รับค่า return ได้:

```robotframework
${label}=    IF    ${x} > 5    Set Variable    big    ELSE    Set Variable    small
Log    ${label}
```

**เมื่อไหร่ใช้ Inline IF:**
- Keyword เดียว, condition เดียว, logic ไม่ซับซ้อน
- ถ้าต้องการ ELSE IF หรือหลาย keyword → ใช้ block IF ปกติ

---

## Common Mistakes

❌ **ลืม END:**
```robotframework
IF    ${x} > 0
    Log    positive
# ไม่มี END → Robot Framework error
```
✅ ปิดด้วย `END` เสมอ *(source: RF User Guide — "The END token closes the IF/ELSE structure")*

---

❌ **ใช้ตัวเล็ก:**
```robotframework
if    ${x} > 0    # ← ผิด! ต้องเป็น IF
```
✅ ต้อง UPPERCASE ทั้งหมด: `IF`, `ELSE IF`, `ELSE`, `END` *(source: RF User Guide)*

---

❌ **Nested IF แต่ END ไม่พอ:**
```robotframework
IF    ${a} > 0
    IF    ${b} > 0
        Log    both positive
    END
# ลืม END ชั้นนอก → error
END
```
✅ Nested IF block ต้องมี END ของตัวเอง + END ของ parent *(source: RF User Guide)*

---

## สรุปบท

ก่อนดูเฉลย ลองตอบเองก่อน:

**คำถาม 1:** ถ้าเขียน `IF    ${score} >= 70` แล้วมี `ELSE IF    ${score} >= 80` ต่อมา — ถ้า score = 85 จะเข้า branch ไหน? เพราะอะไร?

**คำถาม 2:** Inline IF ต่างจาก block IF อย่างไร? ควรใช้อันไหนเมื่อไหร่?

**คำถาม 3:** ทำไมต้องใส่ `'` รอบ `${variable}` เมื่อ compare กับ string?

---

<details>
<summary>เฉลย (คลิกเพื่อดู)</summary>

**เฉลย 1:** เข้า IF (`>= 70`) ทันที เพราะ RF ประเมินจากบนลงล่าง เจอ `True` ตัวแรกก็หยุด — ELSE IF (`>= 80`) จะไม่ถูกประเมินเลย นี่คือ logic bug ที่พบบ่อย ควรเรียง condition จาก "เข้มงวดที่สุด" ลงมา

**เฉลย 2:** Inline IF เขียนในบรรทัดเดียว ไม่มี END เหมาะกับ keyword เดียว / condition เดียว Block IF ใช้เมื่อต้องการหลาย keyword ใน branch หรือมี ELSE IF

**เฉลย 3:** เพราะ `${env}` ถูก resolve เป็น string ก่อน evaluate ถ้าไม่ใส่ quote Python expression อาจ parse ผิด — การใส่ quote ทำให้มั่นใจว่า compare string กับ string เสมอ

</details>
