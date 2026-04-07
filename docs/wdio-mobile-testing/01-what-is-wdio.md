# บทที่ 1: WebdriverIO คืออะไร — เทียบกับ Robot Framework

## วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะ:

- อธิบาย WebdriverIO ในบริบท mobile testing ได้
- เข้าใจว่า WDIO กับ RF ต่างกันที่ paradigm ไม่ใช่แค่ภาษา
- รู้จัก ecosystem ของ WDIO: Mocha, Jasmine, Cucumber
- ตัดสินใจเบื้องต้นได้ว่าควรเรียน WDIO เพราะอะไร

---

## ทำไมต้องรู้? (Why)

ถ้าคุณรู้ RF + AppiumLibrary แล้ว คุณอาจถามว่า "ทำไมต้องเรียน WDIO อีก?"

คำตอบคือ: ทีมต่างกันเลือก tools ต่างกัน ในบาง project คุณจะเจอทั้งสองอยู่ด้วยกัน

การรู้ทั้งสองทำให้คุณ:
1. ทำงานได้ทุก team ไม่ว่าจะใช้ tools ไหน
2. เลือก tool ที่เหมาะสมกับ context ได้
3. อ่าน test ของคนอื่นได้โดยไม่งง

---

## Analogy: RF vs WDIO เหมือน Excel vs Python script

ทั้งสองคำนวณตัวเลขได้:
- **Excel (RF)** = เขียน formula ใน cell, ไม่ต้องรู้ programming ลึก, visual, อ่านง่าย
- **Python script (WDIO/JS)** = code เต็มรูปแบบ, ยืดหยุ่นกว่า, ต้องรู้ programming

ไม่มีอันไหน "ดีกว่า" — ขึ้นอยู่กับ use case และทีม

> ⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
> - RF ง่ายกว่าเสมอ → RF ที่ scale ใหญ่ต้องการ Python programming ลึกเช่นกัน
> - WDIO ซับซ้อนกว่าเสมอ → WDIO มี tooling และ IDE support ที่ดีมาก ทำให้เขียนง่าย

---

## เนื้อหาหลัก

### WebdriverIO คืออะไร?

> "Next-gen browser and mobile automation test framework for Node.js"
> *(webdriver.io)*

WDIO คือ **JavaScript test framework** ที่รันบน Node.js สำหรับ automation testing ทั้ง web และ mobile

### WDIO ทำงานยังไงกับ Mobile?

Architecture เหมือน RF + AppiumLibrary เลย เพราะ Appium server เป็นตัวเดียวกัน:

```
┌──────────────────────────┐
│  WDIO Test               │
│  (JavaScript)            │
└───────────┬──────────────┘
            │ HTTP (W3C WebDriver)
            ▼
┌──────────────────────────┐
│  Appium Server           │  ← ตัวเดียวกับที่ใช้กับ RF
└───────────┬──────────────┘
            ▼
┌──────────────────────────┐
│  UIAutomator2 / XCUITest │
└───────────┬──────────────┘
            ▼
┌──────────────────────────┐
│  Android / iOS Device    │
└──────────────────────────┘
```

**ความเหมือน:** ทั้ง RF+AppiumLibrary และ WDIO ต่างก็คุยกับ Appium server ตัวเดียวกันผ่าน protocol เดียวกัน

### เปรียบเทียบ RF กับ WDIO

| หัวข้อ | Robot Framework | WebdriverIO |
|--------|----------------|-------------|
| **ภาษา** | keyword-driven (.robot) | JavaScript / TypeScript |
| **Test runner** | robot command | Mocha, Jasmine, Cucumber |
| **Test structure** | `*** Test Cases ***` | `describe() / it()` |
| **Assertion** | `Should Be Equal` keyword | `expect().toBe()` (Jasmine) หรือ Chai |
| **Page Object** | Resource file (.robot) | JavaScript Class |
| **Ecosystem** | Python libraries | npm packages (ล้านตัว) |
| **IDE support** | RF Language Server | VS Code + ESLint + TypeScript |
| **Learning curve** | ต่ำ สำหรับ non-programmers | ปานกลาง ต้องรู้ JS |
| **Flexibility** | ปานกลาง | สูงมาก |

### WDIO Ecosystem

WDIO ทำงานร่วมกับ:

**Test Frameworks (เลือกหนึ่ง):**
- **Mocha** — most popular, BDD style (`describe/it`)
- **Jasmine** — คล้าย Mocha
- **Cucumber** — BDD Gherkin syntax (`Given/When/Then`)

**Services (plugins):**
- `@wdio/appium-service` — auto start/stop Appium server
- `@wdio/allure-reporter` — สร้าง HTML report สวยงาม
- `@wdio/visual-service` — screenshot comparison

**Reporters:**
- `@wdio/spec-reporter` — output สวยงามใน terminal
- `@wdio/allure-reporter` — HTML report

### เมื่อไหรควรใช้ WDIO แทน RF?

| Situation | แนะนำ |
|-----------|-------|
| ทีมทั้งหมดเป็น developer ที่รู้ JS/TS | WDIO |
| ต้องการ integration กับ npm ecosystem | WDIO |
| Test pipeline ซับซ้อน ต้องการ programming logic | WDIO |
| ทีมส่วนใหญ่ไม่รู้ programming | RF |
| ต้องการ keyword ที่ readable สำหรับ non-tech stakeholders | RF |
| ต้องการเริ่มทำงานเร็วที่สุด | RF |

---

## ตัวอย่าง 3 ระดับ

### Beginner: Test เดียวกัน — เขียนใน RF vs WDIO

**Robot Framework:**
```robotframework
*** Settings ***
Library    AppiumLibrary

*** Test Cases ***
Login สำเร็จ
    Open Application    http://localhost:4723
    ...    platformName=Android
    ...    appium:automationName=UIAutomator2
    ...    appium:appPackage=com.example.app
    ...    appium:appActivity=.MainActivity
    Input Text    accessibility_id=username_field    user@email.com
    Input Text    accessibility_id=password_field    secret123
    Click Element    accessibility_id=login_button
    Wait Until Element Is Visible    accessibility_id=home_screen    15s
    Close Application
```

**WebdriverIO (Mocha):**
```javascript
// tested: WDIO v9, Appium 2.x, Node.js 20
describe('Login', () => {
    before(async () => {
        // capabilities set ใน wdio.conf.js
    });

    it('should login successfully', async () => {
        const usernameField = await $('~username_field');
        const passwordField = await $('~password_field');
        const loginButton = await $('~login_button');

        await usernameField.setValue('user@email.com');
        await passwordField.setValue('secret123');
        await loginButton.click();

        const homeScreen = await $('~home_screen');
        await homeScreen.waitForDisplayed({ timeout: 15000 });
    });

    after(async () => {
        await driver.deleteSession();
    });
});
```

สังเกต: ทั้งสองส่งคำสั่งเดียวกันไปยัง Appium server เหมือนกัน แต่ syntax ต่างกันมาก

### Intermediate: ความแตกต่างในการ handle async

RF ไม่ต้องกังวลเรื่อง async — keyword ทำงาน synchronous เสมอ

WDIO เป็น async/await เพราะ JavaScript non-blocking:
```javascript
// ✅ ถูก — ใช้ await ก่อนทุก WDIO operation
const element = await $('~login_button');
await element.click();
const text = await element.getText();

// ❌ ผิด — ลืม await
const element = $('~login_button');  // ได้ Promise ไม่ใช่ element
element.click();                     // click บน Promise ไม่ทำงาน
```

### Advanced: WDIO กับ TypeScript สำหรับ large scale

TypeScript เพิ่ม type safety ให้ WDIO:
```typescript
// pages/LoginPage.ts
import { ChainablePromiseElement } from 'webdriverio';

class LoginPage {
    get usernameField(): ChainablePromiseElement {
        return $('~username_field');
    }

    get passwordField(): ChainablePromiseElement {
        return $('~password_field');
    }

    async login(username: string, password: string): Promise<void> {
        await this.usernameField.setValue(username);
        await this.passwordField.setValue(password);
        await $('~login_button').click();
    }
}

export default new LoginPage();
```

Type checking ช่วยจับ error ก่อน runtime — ดีกว่า JavaScript ธรรมดาใน project ขนาดใหญ่

---

## Common Mistakes

❌ **ลืม `await` หน้า WDIO calls**
```javascript
const el = $('~button');  // ❌ ได้ Promise ไม่ใช่ element
el.click();               // ❌ Error หรือทำงานผิดพลาด
```
✅ **ใช้ `await` ก่อนทุก WDIO operation เสมอ**
```javascript
const el = await $('~button');  // ✅
await el.click();               // ✅
```
*(source: webdriver.io/docs/gettingstarted)*

---

❌ **คิดว่า WDIO เป็น Selenium อีกตัว**
→ WDIO เป็น framework แยก ไม่ใช่ wrapper บน Selenium Node.js
✅ **WDIO สื่อสารกับ WebDriver API โดยตรง** รองรับทั้ง WebDriver classic และ WebDriver Bidi protocol
*(source: webdriver.io)*

---

## สรุปบท

ลองตอบก่อนดูเฉลย:

> **คำถาม 1:** WDIO และ RF+AppiumLibrary ต่างกันที่ protocol ที่ใช้คุยกับ Appium ไหม? อธิบาย

> **คำถาม 2:** ถ้าทีม QA ของคุณส่วนใหญ่ไม่ได้เป็น developer และมีเวลาเรียน tool ใหม่ 2 สัปดาห์ — คุณจะแนะนำ RF หรือ WDIO และทำไม?

> **คำถาม 3:** ทำไม WDIO ถึงใช้ `async/await` ในขณะที่ RF ไม่ต้อง?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย 1:** ไม่ต่างกัน — ทั้งคู่ใช้ W3C WebDriver protocol เหมือนกันทั้งหมด Appium server ตัวเดียวกัน รับ request จาก RF หรือ WDIO ได้เหมือนกัน ความต่างอยู่ที่ client library ที่ห่อ protocol

**เฉลย 2:** RF — เพราะ learning curve ต่ำกว่า (keyword-driven ไม่ต้องรู้ programming ลึก) และ syntax เป็น plain text อ่านง่ายสำหรับ non-technical

**เฉลย 3:** JavaScript เป็น single-threaded + event-loop — ถ้าไม่ใช้ async/await operations จะส่งและรับ result พร้อมกันไม่ได้ RF ใช้ Python ที่ blocks ระหว่างรอ response ทำให้ดูเหมือน synchronous

</details>

---

**บทต่อไป:** [บทที่ 2 — Setup WDIO Project](02-setup-wdio-project.md)
