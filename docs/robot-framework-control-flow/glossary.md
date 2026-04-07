# Glossary: Control Flow ใน Robot Framework

| คำศัพท์ | คำอธิบาย | SOURCE |
|---------|---------|--------|
| **IF** | Keyword ที่เริ่ม conditional block — ต้องตามด้วย condition และปิดด้วย END | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **ELSE IF** | Branch เพิ่มเติมใน IF block ตรวจสอบเฉพาะเมื่อ condition ก่อนหน้าเป็น False | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **ELSE** | Branch สุดท้ายที่ทำงานเมื่อทุก condition ก่อนหน้าเป็น False | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **Inline IF** | IF แบบบรรทัดเดียว ไม่ต้องมี END เหมาะกับ keyword เดียว / condition เดียว (RF 5.0+) | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **FOR** | เริ่ม loop — ต้องระบุ loop variable, รูปแบบ (IN/IN RANGE/...) และปิดด้วย END | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **FOR IN** | วน loop ผ่าน list ทีละ item | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **FOR IN RANGE** | วน loop ตามช่วงตัวเลข — `IN RANGE [start] end [step]` | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **FOR IN ENUMERATE** | วน loop ได้ทั้ง index และ item — `start=N` (RF 6.0+) เพื่อเริ่ม index จากเลขอื่น | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **FOR IN ZIP** | วน loop ผ่าน 2+ list คู่ขนาน — ต้องเป็น list variable ไม่ใช่ literal | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **WHILE** | วน loop ตราบที่ condition เป็น True — เช็ค condition ก่อนทุก iteration (RF 5.0+) | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **limit** | Parameter ของ WHILE กำหนดจำนวนรอบสูงสุด (default: 10,000) — ถ้าเกิน FAIL ทันที | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **BREAK** | หยุด loop ทันที ออกจาก loop ไปรัน keyword ถัดจาก END (RF 5.0+) | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **CONTINUE** | ข้าม iteration ปัจจุบัน วนกลับไปเช็ค condition ใหม่ (RF 5.0+) | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **END** | ปิด block ของ IF, FOR, WHILE — ขาดไม่ได้ (ยกเว้น Inline IF) | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **Condition** | Python expression ที่ประเมินผลเป็น True/False — ใช้ใน IF และ WHILE | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **iteration** | การวนหนึ่งรอบของ loop | — |
| **infinite loop** | Loop ที่ไม่มีจุดหยุด — ใน RF จะ FAIL เมื่อถึง limit | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
