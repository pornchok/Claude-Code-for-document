# บทที่ 6: Page Objects ด้วย JavaScript Class

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อน:

> **`$$('~item')` คืนอะไร? และต่างจาก `$('~item')` ยังไง? พร้อมตอบว่าควรใช้แต่ละอันเมื่อไหร่**

---

<details>
<summary>ดูเฉลย</summary>

`$$` คืน array ของทุก element ที่ match / `$` คืนแค่ element แรก ใช้ `$` เมื่อ element unique ใช้ `$$` เมื่อต้องการทุก items เช่นใน list

</details>

---

## วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะ:

- สร้าง Page Object ด้วย JavaScript class ได้
- ใช้ getter functions เพื่อ lazy-load selectors ได้
- จัดโครงสร้าง project ที่ scale ได้ดี
- เข้าใจว่า Page Object ใน WDIO ต่างจากใน RF อย่างไร

---

## ทำไมต้องรู้? (Why)

Test ที่ไม่มี Page Object จะ scale ไม่ได้เมื่อ app โตขึ้น

ใน RF เราแยก locators และ keywords ออกเป็น resource file — ใน WDIO เราทำเหมือนกัน แต่ใช้ JavaScript class แทน

ความต่างหลักคือ WDIO Page Object ใช้ **getter functions** สำหรับ selectors — เพื่อ lazy evaluation (หา element เมื่อใช้จริง ไม่ใช่ตอนสร้าง object)

---

## Analogy: Page Object class เหมือน Blueprint ของห้อง

ห้องแต่ละห้องมี blueprint:
- `LoginPage` class = blueprint ของ Login screen
- getter `usernameField` = ตำแหน่งของ username field ในห้อง
- method `login()` = ขั้นตอนการทำ action ในห้อง

ถ้าห้องเปลี่ยน (UI เปลี่ยน) แค่แก้ blueprint ที่เดียว ทุกคนที่ใช้ blueprint นั้นอัปเดตตามอัตโนมัติ

> ⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
> - Page Object 1 class = 1 page เสมอ → บาง app มี reusable components (header, nav bar) ที่ทำเป็น Component Object แยก
> - getter ต้อง return `$()` เสมอ → บาง getter return `$$()` สำหรับ list elements

---

## เนื้อหาหลัก

### โครงสร้าง Project แบบ Page Object

```
wdio-mobile-tests/
├── wdio.conf.js
├── test/
│   └── specs/
│       ├── login.test.js
│       └── transfer.test.js
└── pageObjects/
    ├── BasePage.js         ← shared methods ทุก page
    ├── LoginPage.js        ← Login screen
    ├── HomePage.js         ← Home screen
    └── TransferPage.js     ← Transfer screen
```

### BasePage — Shared Methods

```javascript
// pageObjects/BasePage.js
export default class BasePage {
    /**
     * รอให้ loading สิ้นสุด (ถ้ามี spinner)
     */
    async waitForLoading(timeout = 15000) {
        try {
            await $('~loading_spinner').waitForDisplayed({
                timeout: 3000,
                reverse: false,
            });
            await $('~loading_spinner').waitForDisplayed({
                timeout,
                reverse: true,
            });
        } catch {
            // spinner ไม่ปรากฏ = ไม่มี loading = ok
        }
    }

    /**
     * ซ่อน keyboard
     */
    async hideKeyboard() {
        await driver.hideKeyboard();
    }

    /**
     * กลับหน้าก่อนหน้า
     */
    async goBack() {
        await driver.back();
    }
}
```

### LoginPage

```javascript
// pageObjects/LoginPage.js
import BasePage from './BasePage.js';

class LoginPage extends BasePage {

    // Getters — lazy evaluation
    // selector ถูกสร้างเมื่อเรียกใช้ ไม่ใช่เมื่อ new LoginPage()
    get usernameField() {
        return $('~username_input');
    }

    get passwordField() {
        return $('~password_input');
    }

    get loginButton() {
        return $('~login_button');
    }

    get errorMessage() {
        return $('~error_message');
    }

    // Methods — actions บน page นี้
    async waitForReady() {
        await (await this.usernameField).waitForDisplayed({ timeout: 15000 });
    }

    async fillUsername(username) {
        await (await this.usernameField).setValue(username);
    }

    async fillPassword(password) {
        await (await this.passwordField).setValue(password);
        await this.hideKeyboard();
    }

    async submit() {
        await (await this.loginButton).click();
    }

    async login(username, password) {
        await this.waitForReady();
        await this.fillUsername(username);
        await this.fillPassword(password);
        await this.submit();
    }

    async getErrorMessage() {
        await (await this.errorMessage).waitForDisplayed({ timeout: 10000 });
        return (await this.errorMessage).getText();
    }
}

// export เป็น singleton instance
export default new LoginPage();
```

> "The goal of using page objects is to abstract any page information away from the actual tests."
> *(webdriver.io/docs/pageobjects)*

> "Selectors defined as getter functions are evaluated when accessed, ensuring elements are requested immediately before use rather than during object initialization."
> *(webdriver.io/docs/pageobjects)*

### HomePage

```javascript
// pageObjects/HomePage.js
import BasePage from './BasePage.js';

class HomePage extends BasePage {

    get balanceText() {
        return $('~account_balance');
    }

    get transferButton() {
        return $('~transfer_button');
    }

    get historyButton() {
        return $('~history_button');
    }

    async waitForReady() {
        await this.waitForLoading();
        await (await this.balanceText).waitForDisplayed({ timeout: 15000 });
    }

    async getBalance() {
        return (await this.balanceText).getText();
    }

    async goToTransfer() {
        await (await this.transferButton).click();
    }
}

export default new HomePage();
```

### Test File ที่ใช้ Page Objects

```javascript
// test/specs/login.test.js
// tested: WDIO v9, Appium 2.x

import LoginPage from '../../pageObjects/LoginPage.js';
import HomePage from '../../pageObjects/HomePage.js';

describe('Login', () => {
    it('should login successfully', async () => {
        await LoginPage.login('user@email.com', 'ValidPass123');
        await HomePage.waitForReady();
        await expect(await HomePage.balanceText).toBeDisplayed();
    });

    it('should show error for wrong password', async () => {
        await LoginPage.waitForReady();
        await LoginPage.login('user@email.com', 'WrongPass');
        const error = await LoginPage.getErrorMessage();
        expect(error).toContain('Invalid credentials');
    });
});
```

---

## ตัวอย่าง 3 ระดับ

### Beginner: TransferPage ครบ

```javascript
// pageObjects/TransferPage.js
import BasePage from './BasePage.js';

class TransferPage extends BasePage {

    get recipientField() { return $('~recipient_phone'); }
    get amountField()    { return $('~transfer_amount'); }
    get confirmButton()  { return $('~confirm_transfer'); }
    get successScreen()  { return $('~transfer_success'); }
    get errorMessage()   { return $('~transfer_error'); }

    async waitForReady() {
        await (await this.recipientField).waitForDisplayed({ timeout: 10000 });
    }

    async transfer(recipient, amount) {
        await this.waitForReady();
        await (await this.recipientField).setValue(recipient);
        await (await this.amountField).setValue(amount);
        await this.hideKeyboard();
        await (await this.confirmButton).click();
    }

    async isTransferSuccessful() {
        try {
            await (await this.successScreen).waitForDisplayed({ timeout: 15000 });
            return true;
        } catch {
            return false;
        }
    }
}

export default new TransferPage();
```

```javascript
// test/specs/transfer.test.js
import LoginPage from '../../pageObjects/LoginPage.js';
import HomePage from '../../pageObjects/HomePage.js';
import TransferPage from '../../pageObjects/TransferPage.js';

describe('Transfer', () => {
    before(async () => {
        await LoginPage.login('user@email.com', 'ValidPass123');
        await HomePage.waitForReady();
    });

    it('should complete transfer successfully', async () => {
        await HomePage.goToTransfer();
        await TransferPage.transfer('0812345678', '500');
        const success = await TransferPage.isTransferSuccessful();
        expect(success).toBe(true);
    });
});
```

### Intermediate: Component Object สำหรับ shared UI

```javascript
// pageObjects/components/BottomNavBar.js
import BasePage from '../BasePage.js';

class BottomNavBar extends BasePage {
    get homeTab()      { return $('~nav_home'); }
    get transferTab()  { return $('~nav_transfer'); }
    get historyTab()   { return $('~nav_history'); }
    get profileTab()   { return $('~nav_profile'); }

    async goToHome()     { await (await this.homeTab).click(); }
    async goToTransfer() { await (await this.transferTab).click(); }
    async goToHistory()  { await (await this.historyTab).click(); }
    async goToProfile()  { await (await this.profileTab).click(); }
}

export default new BottomNavBar();
```

```javascript
// ใน test
import BottomNavBar from '../../pageObjects/components/BottomNavBar.js';

await BottomNavBar.goToHistory();
```

### Advanced: TypeScript Page Object

```typescript
// pageObjects/LoginPage.ts
import { ChainablePromiseElement } from 'webdriverio';
import BasePage from './BasePage.js';

class LoginPage extends BasePage {

    get usernameField(): ChainablePromiseElement {
        return $('~username_input');
    }

    get passwordField(): ChainablePromiseElement {
        return $('~password_input');
    }

    async login(username: string, password: string): Promise<void> {
        await this.usernameField.setValue(username);
        await this.passwordField.setValue(password);
        await $('~login_button').click();
    }
}

export default new LoginPage();
```

TypeScript จะจับ error เช่น ส่ง `number` แทน `string` ก่อน runtime

---

## Common Mistakes

❌ **ใส่ logic ของ test ใน Page Object method**
```javascript
async testLogin() {
    await this.login('user', 'pass');
    expect(await $('~home')).toBeDisplayed(); // ❌ assertion ไม่ควรอยู่ใน Page Object
}
```
✅ **Page Object มีแค่ actions, assertion อยู่ใน test file**
```javascript
// Page Object
async login(u, p) { ... }

// Test
await LoginPage.login('user', 'pass');
await expect(await $('~home')).toBeDisplayed(); // ✅ assertion ใน test
```
*(source: webdriver.io/docs/pageobjects)*

---

❌ **ลืม `await` ตอน call getter**
```javascript
const text = LoginPage.errorMessage.getText();  // ❌
// loginPage.errorMessage คือ ChainablePromiseElement ไม่ใช่ resolved element
```
✅ **`await` ทั้ง getter และ method**
```javascript
const text = await (await LoginPage.errorMessage).getText();  // ✅
```
*(source: webdriver.io/docs/pageobjects)*

---

❌ **Export class แทน instance**
```javascript
export default LoginPage;  // ❌ ต้อง new ทุกครั้ง
```
✅ **Export instance เป็น singleton**
```javascript
export default new LoginPage();  // ✅ ใช้ร่วมกันได้ทุก test
```
*(source: webdriver.io/docs/pageobjects)*

---

## สรุปบท

ลองตอบก่อนดูเฉลย:

> **คำถาม 1:** ทำไม WDIO Page Object ถึงใช้ `get` getter แทนการ assign selector ใน constructor? ถ้า assign ใน constructor จะมีปัญหาอะไร?

> **คำถาม 2:** ทำไม `BasePage` ถึงมีประโยชน์? ถ้าไม่มี BasePage คุณต้องทำอะไรซ้ำในทุก page class?

> **คำถาม 3:** Page Object ใน WDIO (JS class) กับ RF (resource file) ต่างกันอย่างไร? อะไรที่ทำได้ดีกว่าในแต่ละแบบ?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย 1:** getter ทำ lazy evaluation — selector ถูก resolve เมื่อเรียกใช้จริง ถ้า assign ใน constructor selector จะถูก resolve ตอนสร้าง object ซึ่งอาจเกิดก่อน session start หรือก่อน page load ทำให้ element not found

**เฉลย 2:** BasePage รวม shared logic ไว้ที่เดียว เช่น `waitForLoading()`, `hideKeyboard()`, `goBack()` ถ้าไม่มีต้อง copy method เดิมในทุก page class ซึ่งทำให้แก้ยากเมื่อ logic เปลี่ยน

**เฉลย 3:** RF resource file — readable สำหรับ non-programmers, อ่านง่ายเป็น natural language / WDIO JS class — ยืดหยุ่นกว่า (TypeScript, inheritance, complex logic), IDE support ดีกว่า (autocomplete, type checking)

</details>

---

**บทต่อไป:** [บทที่ 7 — เปรียบเทียบ RF vs WDIO](07-comparing-rf-wdio.md)
