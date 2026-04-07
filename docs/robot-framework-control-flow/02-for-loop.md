# บทที่ 2: FOR Loop

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อน:
- ถ้า condition เป็น `False` ตั้งแต่แรก IF block จะทำอะไร?
- Inline IF ใช้ได้เมื่อไหร่?

---

เฉลย: IF block ข้ามไปไม่ทำอะไรเลย | Inline IF ใช้ได้เมื่อต้องการ keyword เดียว condition เดียว ถ้าซับซ้อนกว่านั้นใช้ block IF

---

## วัตถุประสงค์

อ่านจบแล้วทำได้:
- วน loop ผ่าน list ด้วย FOR IN
- วนตามช่วงตัวเลขด้วย FOR IN RANGE (พร้อม start/step)
- ดึง index พร้อมค่าด้วย FOR IN ENUMERATE
- วน 2 list คู่ขนานด้วย FOR IN ZIP
- เลือก variant ที่เหมาะกับสถานการณ์จริง

---

## ทำไมต้องมี FOR Loop?

ถ้าต้องทดสอบ user 50 คน หรือ validate ข้อมูล 100 แถว — การเขียน keyword ซ้ำ 50 บรรทัดคือฝันร้าย FOR loop แก้ปัญหานี้ด้วยการ "สั่งครั้งเดียว ทำหลายรอบ"

---

## Analogy: สายพานการผลิต

FOR loop เหมือนสายพานในโรงงาน:
- **FOR IN** — สายพานส่งชิ้นงานมาทีละชิ้น ไม่รู้ว่ามีกี่ชิ้น แค่ทำจนหมด
- **FOR IN RANGE** — สายพานที่ตั้งค่าได้ว่า "รอบที่เท่าไหร่ถึงเท่าไหร่"
- **FOR IN ENUMERATE** — สายพานที่มีป้ายเลขบอกลำดับชิ้นงานด้วย
- **FOR IN ZIP** — สายพาน 2 เส้นคู่ขนาน ส่งของมาพร้อมกันทีละคู่

⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า: "สายพาน 2 เส้นจะรอกันเสมอ" — ความจริงคือ FOR IN ZIP จะหยุดเมื่อ list ที่สั้นกว่าหมดก่อน ไม่ใช่รอให้ list ที่ยาวกว่าหมดด้วย

---

## เนื้อหาหลัก

### 1. FOR IN — วน list ทีละรายการ

```robotframework
# tested: RF 7.3.2
FOR    ${fruit}    IN    apple    banana    cherry
    Log    กำลัง process: ${fruit}
END
```

Output:
```
กำลัง process: apple
กำลัง process: banana
กำลัง process: cherry
```

ใช้กับตัวแปร list:

```robotframework
# tested: RF 7.3.2
@{browsers}=    Create List    Chrome    Firefox    Edge
FOR    ${browser}    IN    @{browsers}
    Log    ทดสอบบน ${browser}
END
```

---

### 2. FOR IN RANGE — วนตามตัวเลข

```robotframework
# tested: RF 7.3.2
# แบบง่าย: 0 ถึง 4 (ไม่รวม 5)
FOR    ${i}    IN RANGE    5
    Log    รอบที่ ${i}
END
```

```robotframework
# tested: RF 7.3.2
# กำหนด start และ end: 1 ถึง 5 (ไม่รวม 6)
FOR    ${i}    IN RANGE    1    6
    Log    ${i}
END
```

```robotframework
# tested: RF 7.3.2
# กำหนด step: 0, 2, 4, 6, 8
FOR    ${i}    IN RANGE    0    10    2
    Log    ${i}
END
```

**Pattern:** `IN RANGE    [start=0]    end    [step=1]`
- ค่า `end` ไม่ถูกรวม (เหมือน Python `range()`)
- step เป็นลบได้: `IN RANGE    10    0    -1` → 10, 9, 8 ... 1

---

### 3. FOR IN ENUMERATE — วน list พร้อม index

```robotframework
# tested: RF 7.3.2
@{items}=    Create List    alpha    beta    gamma
FOR    ${index}    ${item}    IN ENUMERATE    @{items}
    Log    [${index}] ${item}
END
```

Output:
```
[0] alpha
[1] beta
[2] gamma
```

เริ่ม index จาก 1 (RF 6.0+):

```robotframework
# tested: RF 7.3.2
FOR    ${index}    ${item}    IN ENUMERATE    @{items}    start=1
    Log    [${index}] ${item}
END
```

Output:
```
[1] alpha
[2] beta
[3] gamma
```

**ใช้เมื่อไหร่:** เมื่อต้องรู้ตำแหน่งของ item — เช่น validate row ที่ N ของตาราง, log ลำดับขั้นตอน

---

### 4. FOR IN ZIP — วน 2 list คู่ขนาน

ต้องสร้าง list ด้วย `Create List` ก่อน (ไม่ใช่ inline string):

```robotframework
# tested: RF 7.3.2
${names}=    Create List    Alice    Bob    Carol
${scores}=   Create List    90       75     88
FOR    ${name}    ${score}    IN ZIP    ${names}    ${scores}
    Log    ${name} ได้คะแนน ${score}
END
```

Output:
```
Alice ได้คะแนน 90
Bob ได้คะแนน 75
Carol ได้คะแนน 88
```

❌ **ไม่ได้:** `FOR    ${a}    ${b}    IN ZIP    Alice    Bob    1    2`
✅ **ได้:** ต้องใช้ตัวแปรที่เป็น list เท่านั้น *(source: RF 7.3.2 error message — "FOR IN ZIP items must be list-like")*

---

## Common Mistakes

❌ **ลืม `@{}` เมื่อ pass list:**
```robotframework
${items}=    Create List    a    b    c
FOR    ${x}    IN    ${items}    # ← วนแค่รอบเดียว! ได้ list ทั้งอันเป็น ${x}
    Log    ${x}
END
```
✅ ใช้ `@{items}` เพื่อ unpack list: `FOR    ${x}    IN    @{items}` *(source: RF User Guide — "@ prefix unpacks the list")*

---

❌ **IN RANGE ไม่ใส่ชื่อตัวแปร:**
```robotframework
FOR    IN RANGE    5    # ← ผิด! ขาด loop variable
    Log    something
END
```
✅ ต้องมี loop variable: `FOR    ${i}    IN RANGE    5` *(source: RF User Guide)*

---

❌ **คิดว่า FOR IN ZIP รอ list ที่ยาวกว่า:**
ถ้า list 1 มี 3 items, list 2 มี 5 items → loop จะวนแค่ 3 รอบ แล้วหยุด
*(source: RF User Guide — "stops when the shortest iterable runs out")*

---

## สรุปบท

ก่อนดูเฉลย ลองตอบก่อน:

**คำถาม 1:** `FOR    ${i}    IN RANGE    2    8    3` — ${i} จะมีค่าอะไรบ้าง?

**คำถาม 2:** ต่างกันอย่างไรระหว่าง `FOR    ${x}    IN    ${myList}` กับ `FOR    ${x}    IN    @{myList}`?

**คำถาม 3:** ถ้า list A มี 4 items, list B มี 2 items และใช้ FOR IN ZIP — loop จะวนกี่รอบ? เพราะอะไร?

---

<details>
<summary>เฉลย (คลิกเพื่อดู)</summary>

**เฉลย 1:** 2, 5 — เริ่มที่ 2, บวก step 3 ทุกรอบ, หยุดก่อนถึง 8 (ไม่รวม end)

**เฉลย 2:** `${myList}` — ส่ง list ทั้งอันเป็น item เดียว วนแค่รอบเดียว | `@{myList}` — unpack list ออกมา วนทีละ item ตามจำนวน item ใน list

**เฉลย 3:** วน 2 รอบ เพราะ FOR IN ZIP หยุดเมื่อ list ที่สั้นที่สุด (B = 2 items) หมดก่อน items ที่เหลือใน A จะถูกข้ามไป

</details>
