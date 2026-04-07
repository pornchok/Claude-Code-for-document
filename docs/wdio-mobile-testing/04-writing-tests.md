# บทที่ 4: เขียน Test แรก — describe/it Pattern

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อน:

> **ใน `wdio.conf.js` capabilities key ที่มี `:` เช่น `appium:automationName` ต้องเขียนยังไง? และ `@wdio/appium-service` มีประโยชน์อะไร?**

---

<details>
<summary>ดูเฉลย</summary>

ต้องใส่ quotes: `'appium:automationName': 'UIAutomator2'` — เพราะ `:` มีความหมายพิเศษใน JS object literal
`@wdio/appium-service` auto-start/stop Appium server อัตโนมัติ ไม่ต้องเปิด terminal แยก

</details>

---

## วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะ:

- เขียน test file ด้วย Mocha `describe/it` pattern ได้
- ใช้ `before`, `after`, `beforeEach`, `afterEach` hooks ได้
- ใช้ `expect` สำหรับ assertions ได้
- เข้าใจ `async/await` ใน context ของ WDIO ได้

---

## ทำไมต้องรู้? (Why)

`describe/it` คือ "ภาษา" ของ test ใน JavaScript world — ต่างจาก RF ที่ใช้ `*** Test Cases ***`

เมื่อเข้าใจ pattern นี้แล้ว การอ่าน test ที่คนอื่นเขียน หรือการเขียน test ใหม่จะรู้สึกเป็นธรรมชาติ

---

## Analogy: describe/it เหมือน หัวข้อ/รายการทดสอบ

นึกถึงรายงาน QA:
- `describe('Login')` = **หัวข้อ**: "ทดสอบ Login Feature"
- `it('should login with valid credentials')` = **รายการ**: "ทดสอบ login ด้วย credentials ที่ถูกต้อง"
- `it('should show error for wrong password')` = "ทดสอบ login ด้วย password ผิด"

โครงสร้างนี้ทำให้ report อ่านง่ายเหมือนเอกสาร QA จริงๆ

> ⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
> - `describe` = test case และ `it` = step → จริงๆ `it` แต่ละอัน = 1 test case ที่ independent กัน
> - `describe` ซ้อนได้แค่ 2 ชั้น → ซ้อนได้หลายชั้นตามต้องการ

---

## เนื้อหาหลัก

### โครงสร้าง Test File

```javascript
// test/specs/login.test.js

describe('Login Feature', () => {

    before(async () => {
        // รันครั้งเดียวก่อน describe block ทั้งหมด
        // ใช้สำหรับ: setup ที่ทำครั้งเดียว
    });

    after(async () => {
        // รันครั้งเดียวหลัง describe block ทั้งหมดจบ
        // ใช้สำหรับ: cleanup
    });

    beforeEach(async () => {
        // รันก่อนทุก it block
        // ใช้สำหรับ: reset state ก่อนแต่ละ test
    });

    afterEach(async () => {
        // รันหลังทุก it block
        // ใช้สำหรับ: teardown แต่ละ test
    });

    it('should login with valid credentials', async () => {
        // Test case 1
    });

    it('should show error for wrong password', async () => {
        // Test case 2
    });
});
```

> "A basic Mocha test looks like this: describe('my awesome website', () => { it('should do some assertions', async () => { ... }) })"
> *(webdriver.io/docs/frameworks)*

### Hooks เทียบกับ RF

| RF | WDIO (Mocha) | ใช้เมื่อ |
|----|-------------|---------|
| `[Setup]` ใน Test Case | `beforeEach` | ทำก่อนแต่ละ test |
| `[Teardown]` ใน Test Case | `afterEach` | ทำหลังแต่ละ test |
| `Suite Setup` | `before` | ทำครั้งเดียวก่อน suite ทั้งหมด |
| `Suite Teardown` | `after` | ทำครั้งเดียวหลัง suite ทั้งหมดจบ |

### Assertions ด้วย `expect`

WDIO ใช้ Jasmine expect syntax:

```javascript
// ตรวจ element มีอยู่
const loginBtn = await $('~login_button');
await expect(loginBtn).toBeDisplayed();

// ตรวจข้อความ
const welcomeText = await $('~welcome_message');
await expect(welcomeText).toHaveText('ยินดีต้อนรับ');

// ตรวจ element enabled/disabled
const submitBtn = await $('~submit_btn');
await expect(submitBtn).toBeEnabled();

// ตรวจ element ไม่มี
await expect(await $('~error_msg')).not.toBeDisplayed();
```

### `$()` Selector — หา Element

```javascript
// accessibility_id  (เหมือน accessibility_id= ใน RF)
const btn = await $('~login_button');

// resource-id
const field = await $('android=new UiSelector().resourceId("com.app:id/et_username")');

// xpath
const el = await $('//android.widget.Button[@text="Login"]');

// รอให้ element พร้อม
await btn.waitForDisplayed({ timeout: 10000 });
```

> "Accessibility ID: The accessibility id strategy works across platforms—for iOS it maps to accessibility identifiers, while for Android it corresponds to content-description for the element."
> *(webdriver.io/docs/selectors)*

`~` prefix = accessibility_id strategy — shorthand ของ WDIO

---

## ตัวอย่าง 3 ระดับ

### Beginner: Test Login ครบ flow

```javascript
// test/specs/login.test.js
// tested: WDIO v9, Appium 2.x, Android API 33 emulator

describe('Login', () => {
    afterEach(async () => {
        // Reset app หลังแต่ละ test
        await driver.reset();
    });

    it('should login successfully', async () => {
        // รอ login screen โหลด
        const usernameField = await $('~username_input');
        await usernameField.waitForDisplayed({ timeout: 15000 });

        // กรอก credentials
        await usernameField.setValue('user@email.com');
        await $('~password_input').setValue('ValidPass123');
        await $('~login_button').click();

        // ตรวจว่า home screen ขึ้น
        const homeScreen = await $('~home_screen');
        await homeScreen.waitForDisplayed({ timeout: 15000 });
        await expect(homeScreen).toBeDisplayed();
    });

    it('should show error for wrong password', async () => {
        const usernameField = await $('~username_input');
        await usernameField.waitForDisplayed({ timeout: 15000 });

        await usernameField.setValue('user@email.com');
        await $('~password_input').setValue('WrongPass');
        await $('~login_button').click();

        const errorMsg = await $('~error_message');
        await errorMsg.waitForDisplayed({ timeout: 10000 });
        await expect(errorMsg).toHaveTextContaining('Invalid credentials');
    });
});
```

### Intermediate: Nested describe + shared helpers

```javascript
// test/specs/transfer.test.js
// tested: WDIO v9, Appium 2.x

describe('Transfer Feature', () => {
    before(async () => {
        // Login ครั้งเดียวก่อนทุก transfer test
        await $('~username_input').waitForDisplayed({ timeout: 15000 });
        await $('~username_input').setValue('user@email.com');
        await $('~password_input').setValue('ValidPass123');
        await $('~login_button').click();
        await $('~home_screen').waitForDisplayed({ timeout: 15000 });
    });

    describe('Transfer Amount Validation', () => {
        beforeEach(async () => {
            await $('~transfer_button').click();
            await $('~transfer_screen').waitForDisplayed({ timeout: 10000 });
        });

        afterEach(async () => {
            // กลับ home ก่อน test ถัดไป
            await $('~back_button').click();
        });

        it('should reject transfer when amount is 0', async () => {
            await $('~amount_field').setValue('0');
            await $('~confirm_button').click();
            await expect(await $('~error_amount')).toBeDisplayed();
        });

        it('should reject transfer exceeding daily limit', async () => {
            await $('~amount_field').setValue('9999999');
            await $('~confirm_button').click();
            const error = await $('~error_limit');
            await error.waitForDisplayed({ timeout: 5000 });
            await expect(error).toHaveTextContaining('exceeds daily limit');
        });
    });
});
```

### Advanced: Custom Assertion Helper

```javascript
// helpers/assertions.js
export async function waitForAndAssert(selector, options = {}) {
    const { timeout = 15000, message } = options;
    const element = await $(selector);
    await element.waitForDisplayed({ timeout, timeoutMsg: message });
    return element;
}

export async function assertTextContains(selector, expectedText) {
    const element = await waitForAndAssert(selector);
    const text = await element.getText();
    if (!text.includes(expectedText)) {
        throw new Error(
            `Expected element '${selector}' to contain '${expectedText}'\nActual: '${text}'`
        );
    }
    return element;
}
```

```javascript
// test/specs/dashboard.test.js
import { waitForAndAssert, assertTextContains } from '../../helpers/assertions.js';

describe('Dashboard', () => {
    it('should display account balance', async () => {
        const balance = await waitForAndAssert(
            '~account_balance',
            { message: 'Balance did not appear within 15s' }
        );
        // ตรวจว่า balance มี format ถูก (มี ฿ หรือ THB)
        await assertTextContains('~account_balance', '฿');
    });
});
```

---

## Common Mistakes

❌ **ลืม `await` ก่อน selector**
```javascript
const btn = $('~login_button');  // ❌ ได้ Promise ไม่ใช่ element
btn.click();                     // ❌ click บน Promise — ไม่ทำงาน
```
✅ **`await` ก่อนทุก WDIO call เสมอ**
```javascript
const btn = await $('~login_button');  // ✅
await btn.click();                     // ✅
```
*(source: webdriver.io/docs/gettingstarted)*

---

❌ **ใช้ `before` แทน `beforeEach` โดยไม่เข้าใจความต่าง**
→ state ไม่ reset ระหว่าง test ทำให้ test ขึ้นกับ order
✅ **`before` = ทำครั้งเดียว / `beforeEach` = ทำก่อนทุก `it`** เลือกให้ถูก
*(source: webdriver.io/docs/frameworks)*

---

❌ **test ที่ตั้งชื่อด้วย `it('should...')` แต่ทดสอบหลาย behavior ในอัน**
```javascript
it('should login and check balance and logout', async () => { ... })  // ❌
```
→ เมื่อ fail ไม่รู้ว่าขั้นตอนไหนผิด
✅ **1 it = 1 behavior ที่ทดสอบ** แยกออกเป็น 3 test case
*(source: testing best practice)*

---

## สรุปบท

ลองตอบก่อนดูเฉลย:

> **คำถาม 1:** `before` กับ `beforeEach` ต่างกันยังไง? ให้ยกตัวอย่าง scenario ที่ควรใช้แต่ละอัน

> **คำถาม 2:** `~` ใน `$('~login_button')` หมายความว่าอะไร? ต่างจาก `$('//android.widget.Button')` ยังไง?

> **คำถาม 3:** ถ้า test case หนึ่งทำให้ state ของ app เปลี่ยน (เช่น login แล้ว) และส่งผลกระทบต่อ test case ถัดไป — คุณจะแก้ปัญหานี้ยังไง?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย 1:** `before` รันครั้งเดียวก่อน describe block ทั้งหมด ใช้เมื่อ setup แพง เช่น login ครั้งเดียวแล้ว test ทุก case ใช้ session เดิม / `beforeEach` รันก่อนทุก `it` ใช้เมื่อต้องการ fresh state ทุก test เช่น navigate กลับ home ก่อนแต่ละ test

**เฉลย 2:** `~` = accessibility_id shorthand ใน WDIO — เทียบเท่า `accessibility_id=` ใน RF ต่างจาก xpath ที่ต้อง scan ทั้ง UI tree จึงเร็วกว่า

**เฉลย 3:** ใช้ `afterEach` reset app state หลังแต่ละ test: `await driver.reset()` หรือ navigate กลับ start screen เพื่อให้แต่ละ test เริ่มใน state เดิม

</details>

---

**บทต่อไป:** [บทที่ 5 — Selectors และ Mobile Interactions](05-selectors-interactions.md)
