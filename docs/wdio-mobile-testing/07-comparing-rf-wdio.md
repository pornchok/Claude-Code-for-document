# บทที่ 7: เปรียบเทียบ RF vs WDIO — ใช้อะไรเมื่อไหร่

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อน:

> **ทำไม WDIO Page Object getter ถึงต้องใช้ `get` keyword แทนการ assign ใน constructor? และ `export default new LoginPage()` ทำไมถึงดีกว่า `export default LoginPage`?**

---

<details>
<summary>ดูเฉลย</summary>

`get` = lazy evaluation — selector resolve เมื่อเรียกใช้ ไม่ใช่ตอน init ป้องกัน element not found ก่อน page load / `new LoginPage()` = export singleton instance ใช้ร่วมกันได้ทันทีโดยไม่ต้อง new ใหม่ทุกที่

</details>

---

## วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะ:

- เปรียบเทียบ RF และ WDIO แบบ objective ได้ทั้งข้อดีและข้อเสีย
- มี decision framework ว่าจะใช้อะไรใน scenario ไหน
- รู้ว่าทีมที่ใช้ทั้งสองตัวพร้อมกันทำยังไง
- เตรียมตัวสำหรับทำงานที่ Krungsri Nimble ได้ดีขึ้น

---

## ทำไมต้องรู้? (Why)

ที่ Krungsri Nimble job description ระบุทั้ง Robot Framework และ WebdriverIO — แปลว่าทีมใช้ทั้งสอง

เมื่อเจอ task ใหม่คุณต้องตัดสินใจได้ว่าจะเขียนใน RF หรือ WDIO บทนี้ช่วยให้ตัดสินใจได้อย่างมีหลักการ

---

## Analogy: RF vs WDIO เหมือน Excel vs Python Pandas

- **Excel (RF):** เปิดมาทำได้เลย ไม่ต้องรู้ programming ลึก เหมาะ business user หรือ QA ที่ไม่ได้เป็น programmer
- **Python Pandas (WDIO):** ยืดหยุ่นกว่ามาก ทำอะไรก็ได้ แต่ต้องรู้ programming และ library ecosystem

ไม่มีอันไหน "ดีกว่า" มีแต่ "เหมาะกับงานไหน"

> ⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
> - RF ไม่ต้องการ programming skills เลย → RF ที่ scale ใหญ่ต้องการ Python custom library เขียนเองบ้าง
> - WDIO ดีกว่า RF เสมอเพราะซับซ้อนกว่า → complexity ไม่ใช่ตัวตัดสิน — usefulness ต่างหาก

---

## เนื้อหาหลัก

### เปรียบเทียบแบบ Head-to-Head

| มิติ | Robot Framework | WebdriverIO | ชนะ |
|------|----------------|-------------|-----|
| **ภาษา** | keyword-driven | JavaScript/TypeScript | ขึ้นอยู่กับทีม |
| **Learning curve** | ต่ำ — อ่านเหมือน plain text | ปานกลาง — ต้องรู้ JS | RF |
| **Programmer-friendliness** | ปานกลาง | สูงมาก | WDIO |
| **npm ecosystem** | ❌ ไม่รองรับ | ✅ ใช้ npm packages ทั้งหมด | WDIO |
| **Type safety** | ❌ ไม่มี | ✅ TypeScript | WDIO |
| **IDE support** | ปานกลาง | ดีมาก (VSCode + TS) | WDIO |
| **Readability สำหรับ non-tech** | ✅ อ่านง่ายมาก | ❌ ต้องรู้ JS | RF |
| **Cross-platform (web+mobile)** | ✅ library หลากหลาย | ✅ รองรับ | เท่ากัน |
| **Report** | RF built-in report + Allure | Allure + ตัวอื่นจาก npm | WDIO ยืดหยุ่นกว่า |
| **Community** | ใหญ่ในฝั่ง Python/QA | ใหญ่มากในฝั่ง JS dev | ขึ้นอยู่กับ context |
| **Debug** | ยากกว่าเล็กน้อย | ง่ายกว่า (browser dev tools, VS Code debugger) | WDIO |

### เดียวกัน ทำต่างกัน

**Login Test — RF:**
```robotframework
*** Test Cases ***
ทดสอบ Login สำเร็จ
    [Setup]    เปิดแอป
    ทำการ Login ด้วย    user@email.com    ValidPass123
    รอให้ Home Screen โหลดครบ
    Page Should Contain Element    ${HOME_BALANCE}
    [Teardown]    Close Application
```

**Login Test — WDIO:**
```javascript
it('should login successfully', async () => {
    await LoginPage.login('user@email.com', 'ValidPass123');
    await HomePage.waitForReady();
    await expect(await HomePage.balanceText).toBeDisplayed();
});
```

RF: อ่านง่าย ใกล้เคียง natural language
WDIO: กระชับกว่า มี autocomplete ดีกว่า

### Decision Framework — ใช้อะไรเมื่อไหร่

```
ทีมส่วนใหญ่รู้ JavaScript ดี?
    ├── YES → WDIO (ใช้ทักษะที่มีอยู่)
    └── NO  →
        ทีมต้องเขียน complex logic?
            ├── YES → พิจารณา WDIO + เรียน JS ก่อน
            └── NO  → RF (learning curve ต่ำกว่า)

Test performation สำคัญมาก?
    ├── YES → WDIO (parallel testing ง่ายกว่า)
    └── NO  → ทั้งสองใช้ได้

Stakeholder อ่าน test report เองได้?
    ├── YES → RF (readable natural language)
    └── NO  → ทั้งสองใช้ได้

ต้องการ integrate กับ npm tools (Allure, Slack notify, DB query)?
    ├── YES → WDIO (npm ecosystem)
    └── NO  → ทั้งสองใช้ได้
```

### ทำงานร่วมกันได้ไหม? (Hybrid approach)

ได้ — หลายทีมใช้ทั้งสองพร้อมกัน:

```
ทีม QA (ไม่ใช่ programmer):
→ เขียน test ด้วย Robot Framework
→ mobile test ใช้ AppiumLibrary
→ web test ใช้ SeleniumLibrary

ทีม Developer-QA (รู้ JS):
→ เขียน test ด้วย WebdriverIO
→ รัน parallel บน multiple devices
→ integrate กับ CI/CD pipeline ซับซ้อน
```

สิ่งสำคัญ: ทั้งสองคุยกับ **Appium server ตัวเดียวกัน** — ไม่ conflict กัน

---

## ตัวอย่าง 3 ระดับ

### Beginner: Cheat Sheet เปรียบเทียบ Syntax

| Feature | RF | WDIO |
|---------|-----|------|
| เปิดแอป | `Open Application` | capabilities ใน `wdio.conf.js` |
| click | `Click Element    loc` | `await $('loc').click()` |
| input | `Input Text    loc    text` | `await $('loc').setValue('text')` |
| read text | `Get Text    loc` | `await $('loc').getText()` |
| รอ element | `Wait Until Element Is Visible    loc` | `await $('loc').waitForDisplayed()` |
| assert text | `Should Contain    ${text}    value` | `expect(text).toContain('value')` |
| accessibility id | `accessibility_id=btn` | `$('~btn')` |
| xpath | `xpath=//android.widget.Button` | `$('//android.widget.Button')` |

### Intermediate: เมื่อต้องทำงานกับ codebase ที่มีทั้งสอง

Krungsri Nimble ใช้ทั้งสอง — strategy ที่แนะนำ:

1. **เข้าใจว่า test ไหนเป็น RF และไหนเป็น WDIO** — ดูจาก file extension (`.robot` vs `.js`/`.ts`)
2. **RF test:** ดูที่ `*** Settings ***`, `*** Variables ***`, `*** Keywords ***`, `*** Test Cases ***`
3. **WDIO test:** ดูที่ `wdio.conf.js`, `describe/it`, `pageObjects/`
4. **Appium server เดียวกัน** — ทั้งสองใช้ port 4723 เหมือนกัน อาจต้องระวัง port conflict ถ้ารันพร้อมกัน

### Advanced: เลือก tool สำหรับ Banking App Testing

สำหรับ Krungsri Nimble — recommendation:

| Test Type | แนะนำ | เหตุผล |
|-----------|-------|--------|
| Happy path E2E | RF | อ่านง่าย PM/BA เข้าใจได้ |
| Edge cases ซับซ้อน | WDIO | logic ยืดหยุ่นกว่า |
| Regression suite ขนาดใหญ่ | WDIO | parallel testing ง่ายกว่า |
| Security/Compliance test | RF | readable สำหรับ audit |
| Performance testing | RF + JMeter | ใช้ JMeter แยก |
| Visual regression | WDIO | มี `@wdio/visual-service` |

---

## Common Mistakes

❌ **เลือก tool ตาม "coolness" ไม่ใช่ fit กับทีม**
→ WDIO ดูเท่กว่า แต่ถ้าทีมไม่รู้ JS → maintenance nightmare
✅ **เลือกตาม team skills และ project requirements** ไม่ใช่ trending technology

---

❌ **พยายาม migrate ทุกอย่างไปเป็น tool เดียว**
→ waste time, break existing tests
✅ **Hybrid approach ทำได้ดี** — ใช้ RF สำหรับสิ่งที่ทำอยู่แล้ว เพิ่ม WDIO สำหรับ use case ใหม่ที่เหมาะกว่า

---

❌ **คิดว่า WDIO ช้ากว่า RF เพราะ JavaScript**
→ misconception — ทั้งคู่ส่ง HTTP request ไปยัง Appium เหมือนกัน ความเร็วขึ้นกับ network และ device ไม่ใช่ language
✅ **ความเร็วของ test ขึ้นกับ Appium + device** ไม่ใช่ language ที่ใช้

---

## สรุปบท

ลองตอบก่อนดูเฉลย:

> **คำถาม 1:** ทีม QA ใหม่มี 4 คน — 2 คนรู้ Python/RF อยู่แล้ว, 2 คนมาจาก frontend dev ที่รู้ JS ดี — คุณจะแนะนำให้ทีมใช้ tool อะไร? อธิบายเหตุผล

> **คำถาม 2:** ถ้าทั้ง RF test และ WDIO test รันบน machine เดียวกัน จะเกิดปัญหาอะไรได้บ้าง? และแก้ยังไง?

> **คำถาม 3:** นาย A บอกว่า "ควรเขียน test ทั้งหมดเป็น WDIO เพราะ TypeScript ทำให้ bug น้อยลง" — คุณเห็นด้วยไหม? อธิบาย

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย 1:** Hybrid — 2 คนที่รู้ RF ดูแล RF test suite ที่มีอยู่ต่อ, 2 คนที่รู้ JS เริ่มสร้าง WDIO suite สำหรับ feature ใหม่ที่ต้องการ parallel testing หรือ complex logic — แทนที่จะบังคับทุกคนเรียน tool ใหม่ ควรใช้ทักษะที่แต่ละคนมีอยู่แล้ว

**เฉลย 2:** ทั้งคู่ใช้ port 4723 → ถ้ารันพร้อมกัน port conflict / แก้โดยรัน sequential ไม่ใช่ parallel, หรือตั้ง port ต่างกันสำหรับแต่ละ session, หรือใช้ appium-service ใน WDIO แทนการรัน Appium เอง

**เฉลย 3:** เห็นด้วยบางส่วน — TypeScript ช่วยเรื่อง type safety จริง แต่ไม่ควร migrate ทุกอย่างมาเป็น WDIO ถ้า RF ทำงานได้ดีอยู่แล้ว เพราะ migration มี cost สูง และ RF ยังมีข้อดีที่ WDIO ไม่มี (readability, Python ecosystem, lower learning curve)

</details>

---

**ยินดีด้วย! คุณจบ Series 2 แล้ว**

ขั้นตอนต่อไป:
- ทำ [Exercises](exercises.md) เพื่อฝึกทักษะ
- อ่าน [Glossary](glossary.md)
- กลับไปทบทวน [Series 1: RF + AppiumLibrary](../rf-mobile-testing/00-overview.md) ถ้ายังไม่แน่ใจ
