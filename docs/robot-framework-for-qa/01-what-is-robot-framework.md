# บทที่ 1: ทำไมต้องใช้ Robot Framework?

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:
- อธิบายได้ว่า Robot Framework คืออะไร และทำงานยังไง
- เข้าใจว่า keyword-driven testing คืออะไร
- รู้ว่า RF เหมาะกับงานแบบไหน และไม่เหมาะแบบไหน
- ตัดสินใจได้ว่าควรใช้ SeleniumLibrary หรือ Browser Library

---

## ทำไมต้องรู้? (Why)

ลองนึกถึงปัญหาที่ QA manual เจอบ่อย:

> "Test case นี้เรารัน regression ทุก sprint — 200 case ใช้เวลา 2 วัน"

> "Bug ที่แก้แล้ว กลับมาอีกครั้งเพราะลืม test บาง case"

> "ทีม dev ถามว่า test ผ่านไหม แต่ยังเทสไม่เสร็จเลย"

นี่คือปัญหาที่ test automation แก้ได้ — รัน 200 case ใน 20 นาที, ทุกคืนอัตโนมัติ, ไม่มีลืม

แต่ automation tool หลายตัวใช้ยากสำหรับคนที่ไม่ได้เป็น developer Robot Framework ถูกออกแบบมาให้ **QA เขียนได้** โดยไม่ต้องเก่ง programming มาก

---

## Robot Framework คืออะไร?

Robot Framework เป็น open-source framework สำหรับ test automation ที่ใช้แนวคิดชื่อว่า **keyword-driven testing**

แทนที่จะเขียน code แบบนี้:
```python
# แบบ Selenium Python ปกติ
driver.find_element(By.ID, "username").send_keys("testuser")
driver.find_element(By.ID, "password").send_keys("pass123")
driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()
assert "Welcome" in driver.title
```

RF เขียนแบบนี้:
```robot
# แบบ Robot Framework
Input Text    id:username    testuser
Input Text    id:password    pass123
Click Button    css:button[type='submit']
Title Should Be    Welcome Page
```

อ่านแล้วเข้าใจทันที แม้ไม่รู้ภาษา Python

---

## Analogy: Robot Framework เหมือน Recipe Book ของ Chef

ลองนึกว่าคุณเป็น Chef และมี **Recipe Book** (Robot Framework):

- **Keywords** คือ step ใน recipe เช่น "ต้มน้ำ", "ใส่เกลือ", "คนให้เข้ากัน"
- **Test Case** คือ recipe 1 อาหาร ที่เรียง step ตามลำดับ
- **Library** คือหมวดเครื่องมือ เช่น "เครื่องมือผัด" หรือ "เครื่องมืออบ"
- **Resource File** คือหนังสือ recipe รวม ที่ Chef หลายคนใช้ร่วมกัน

เวลา test ก็เหมือนทำตาม recipe — ทำตาม step, ถ้า step ไหนผิดก็รู้ทันที

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
- Keywords เหมือน recipe step ที่ทำได้แค่ตามลำดับ — จริงๆ Keywords ส่งค่า (arguments) และรับค่ากลับ (return) ได้
- Library เหมือนเครื่องมือที่ใช้ได้เฉพาะในครัว — จริงๆ Library 1 ตัวใช้ได้ใน test file ทั้งหมดที่ import
- Recipe เหมือน test case ที่ทำได้ครั้งละ 1 อาหาร — จริงๆ test case 1 อันอาจเรียก test หลาย scenario พร้อมกันผ่าน Data-Driven

---

## Keyword-Driven Testing คืออะไร?

ไอเดียหลักของ RF คือ **แยก "สิ่งที่ test" ออกจาก "วิธี implement"**

```
Test Case (สิ่งที่ test):
    User Can Login With Valid Credentials
        Open Login Page
        Enter Username    john@example.com
        Enter Password    secretpass
        Click Login
        Verify Welcome Message Is Shown

Keyword (วิธี implement — ซ่อนไว้ใน resource file):
    Open Login Page
        Browser.Go To    ${BASE_URL}/login
        Browser.Wait For Elements State    id:username    visible

    Enter Username
        [Arguments]    ${email}
        Browser.Fill Text    id:username    ${email}
```

QA ที่เขียน test case อ่านแค่ส่วนบน ไม่ต้องรู้ว่า `Open Login Page` ทำยังไง
Developer ที่ดูแล library ดูแลแค่ส่วนล่าง

---

## RF เหมาะกับอะไร? ไม่เหมาะกับอะไร?

### เหมาะมาก
- **Acceptance Testing / Functional Testing** — ทดสอบตาม requirement
- **Regression Testing** — รัน test เดิมซ้ำๆ ทุก sprint
- **Web UI Testing** — ทดสอบผ่าน browser
- **API Testing** — ทดสอบ REST API
- **Database Verification** — เช็คข้อมูลในฐานข้อมูล

### ไม่เหมาะ
- **Unit Testing** — ใช้ pytest หรือ unittest ตรงกว่า
- **Performance/Load Testing** — ใช้ JMeter, k6 แทน
- **Test ที่ต้องการ logic ซับซ้อนมาก** — เขียน Python library แยกดีกว่า

---

## SeleniumLibrary vs Browser Library — ใช้อันไหน?

สำหรับ web testing มี library 2 ตัวหลัก:

| | SeleniumLibrary | Browser Library (Playwright) |
|---|---|---|
| **เบื้องหลัง** | Selenium WebDriver | Microsoft Playwright |
| **ติดตั้ง** | ง่ายกว่า | ต้อง init เพิ่ม |
| **ความเร็ว** | ช้ากว่า | เร็วกว่าชัดเจน |
| **รอ element** | ต้องเขียน wait เอง | รอให้อัตโนมัติ |
| **Modern web (React, Vue)** | อาจมีปัญหา | รองรับดีกว่า |
| **เอกสาร/tutorial** | เยอะกว่า (เก่ากว่า) | น้อยกว่าแต่ดีขึ้นเรื่อยๆ |
| **แนะนำสำหรับ** | โปรเจคเก่าที่ใช้ Selenium อยู่แล้ว | **โปรเจคใหม่ทุกโปรเจค** |

**Course นี้ใช้ Browser Library** เพราะเป็นทิศทางของ industry ปัจจุบัน (2025) และรองรับ modern web app ได้ดีกว่า

---

## สรุปบท

ก่อนไปบทต่อไป ลองตอบคำถามพวกนี้ด้วยตัวเองก่อน (อย่าเปิดดูเฉลยก่อนคิด 30 วินาที):

**คำถาม 1:** ถ้าทีมคุณต้องการรัน regression test 300 case ทุกวันหลัง deploy อัตโนมัติ — RF เหมาะไหม? เพราะอะไร?

**คำถาม 2:** "Keywords" ใน Robot Framework คืออะไร? ต่างจาก "Test Case" ยังไง?

**คำถาม 3:** ถ้าเริ่มโปรเจคใหม่วันนี้ ควรเลือก SeleniumLibrary หรือ Browser Library? เพราะอะไร?

---

<details>
<summary>เฉลย (คลิกเพื่อดู — ดูหลังจากลองตอบแล้วเท่านั้น)</summary>

**เฉลย 1:** เหมาะมาก RF ถูกออกแบบมาสำหรับ regression testing โดยตรง รันผ่าน CI/CD ได้, มี report ชัดเจน, เหมาะกับ functional/acceptance test แบบนี้

**เฉลย 2:** Keyword คือ "ชื่อการกระทำ" ที่ซ่อน implementation ไว้ข้างใน Test Case คือลำดับของ Keywords ที่เรียงกันเพื่อทดสอบ scenario หนึ่ง — Test Case อ่านง่าย, Keyword ดูแล implementation

**เฉลย 3:** Browser Library (Playwright) เพราะเร็วกว่า, รอ element อัตโนมัติ, รองรับ modern web app ดีกว่า, เป็นทิศทาง industry ปัจจุบัน

</details>
