# บทที่ 5: Selectors และ Mobile Interactions

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อน:

> **`$('~login_button')` ใน WDIO เทียบเท่ากับ locator อะไรใน RF? และทำไม `~` ถึงแนะนำให้ใช้เป็นอันดับแรก?**

---

<details>
<summary>ดูเฉลย</summary>

`~` = accessibility_id strategy เทียบเท่ากับ `accessibility_id=login_button` ใน RF แนะนำเพราะเร็ว stable และ cross-platform (Android/iOS ใช้ได้ทั้งคู่)

</details>

---

## วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะ:

- ใช้ selector strategies ทั้งหมดของ WDIO สำหรับ mobile ได้
- ทำ swipe, scroll, long press ใน WDIO ได้
- Hide keyboard ใน WDIO ได้
- เข้าใจ WDIO mobile commands API

---

## ทำไมต้องรู้? (Why)

Selectors ใน WDIO มี syntax ต่างจาก RF แต่ locator strategy เหมือนกัน เมื่อรู้แล้วจะย้ายความรู้ระหว่างสองฝั่งได้ง่าย

Gesture ใน WDIO ก็ต่างจาก RF — WDIO มี abstraction สูงกว่าสำหรับ gesture ทั่วไป

---

## Analogy: Selector เหมือน Remote Control ปุ่มต่างๆ

`$('~btn')` กับ `$('android=...')` ต่างก็ชี้ไปยัง element เดียวกัน แต่คนละวิธี:
- `~` = ปุ่มลัด (เร็ว ตรง)
- `android=` = remote พิมพ์ชื่อ function ยาวๆ (ยืดหยุ่น)
- `//xpath` = remote ที่พิมพ์ path ทั้งหมด (ช้า แต่หาได้ทุกอย่าง)

> ⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
> - ทุก selector หา element เดิมเสมอ → บางครั้งหลาย element match กับ selector เดียวกัน (โดยเฉพาะ xpath และ class)
> - `~` เร็วกว่าเสมอ → ถ้า content-desc ว่าง การใช้ `~` จะหาไม่เจอ ต้องใช้ตัวอื่น

---

## เนื้อหาหลัก

### Selector Strategies ใน WDIO

#### 1. Accessibility ID (`~`) — แนะนำอันดับ 1

```javascript
// เทียบเท่ากับ accessibility_id= ใน RF
const btn = await $('~login_button');
```

#### 2. UiSelector (Android-specific) — รองรับทุก attribute

```javascript
// ใช้ resource-id
const field = await $('android=new UiSelector().resourceId("com.app:id/et_username")');

// ใช้ text
const btn = await $('android=new UiSelector().text("Login")');

// ใช้ className + index
const item = await $('android=new UiSelector().className("android.widget.TextView").instance(2)');
```

> "Android: Uses UiAutomator selectors like android=new UiSelector().text('Cancel')"
> *(webdriver.io/docs/selectors)*

#### 3. XPath — fallback

```javascript
const btn = await $('//android.widget.Button[@text="Login"]');
const field = await $('//*[@resource-id="com.app:id/et_username"]');
```

#### 4. iOS Predicate String (iOS เท่านั้น)

```javascript
// iOS only
const btn = await $('ios predicate string:type == "XCUIElementTypeButton" AND label == "Login"');
```

> "iOS: Supports UIAutomation, XCUITest predicate strings, and class chains"
> *(webdriver.io/docs/selectors)*

### ตารางเปรียบเทียบ RF vs WDIO Selectors

| RF locator | WDIO selector | หมายเหตุ |
|-----------|--------------|---------|
| `accessibility_id=login_btn` | `$('~login_btn')` | แนะนำ |
| `id=com.app:id/btn_login` | `$('android=new UiSelector().resourceId("com.app:id/btn_login")')` | resource-id |
| `xpath=//android.widget.Button` | `$('//android.widget.Button')` | เหมือนกันเลย |

### Interactions

#### คลิกและพิมพ์

```javascript
// คลิก
await $('~login_button').click();

// พิมพ์ข้อความ (clear แล้วพิมพ์)
await $('~username_field').setValue('user@email.com');

// เพิ่มข้อความโดยไม่ clear
await $('~username_field').addValue(' extra text');

// อ่านข้อความ
const text = await $('~welcome_msg').getText();
```

#### รอ Element

```javascript
// รอให้ display
await $('~home_screen').waitForDisplayed({ timeout: 15000 });

// รอให้หายไป
await $('~loading_spinner').waitForDisplayed({
    timeout: 15000,
    reverse: true  // รอให้ไม่ display
});

// รอให้ enabled
await $('~submit_btn').waitForEnabled({ timeout: 10000 });
```

### Mobile Gestures ใน WDIO

> "WebdriverIO abstracts away complex Appium APIs to enable concise, intuitive, and platform-agnostic test scripts. For example, instead of manually constructing action chains for a long press, you can simply call .longPress()."
> *(webdriver.io/docs/api/mobile)*

#### Swipe

```javascript
// Swipe ทั้งหน้าจอ
await driver.touchAction([
    { action: 'press', x: 540, y: 1400 },
    { action: 'moveTo', x: 540, y: 600 },
    { action: 'release' }
]);

// WDIO v9 — ใช้ mobile commands
await driver.execute('mobile: swipe', {
    direction: 'up',      // 'up', 'down', 'left', 'right'
    element: elementRef,  // optional: swipe บน element เฉพาะ
});
```

#### Scroll หา Element

```javascript
// Scroll จนเจอ element
await driver.execute('mobile: scroll', {
    direction: 'down',
    selector: '~target_element',
    strategy: 'accessibility id',
});
```

#### Long Press

```javascript
// Long press ที่ element
await $('~message_item').longPress();

// หรือระบุ duration (ms)
await $('~message_item').longPress({ duration: 2000 });
```

#### Hide Keyboard

```javascript
await driver.hideKeyboard();
```

> "Commands work on both Android and iOS without conditional logic."
> *(webdriver.io/docs/api/mobile)*

---

## ตัวอย่าง 3 ระดับ

### Beginner: Login Form ครบ

```javascript
// test/specs/login.test.js
// tested: WDIO v9, Appium 2.x, Android API 33

describe('Login Form', () => {
    it('should fill and submit login form', async () => {
        // รอ form โหลด
        await $('~username_input').waitForDisplayed({ timeout: 15000 });

        // กรอก username
        await $('~username_input').setValue('john@email.com');

        // กรอก password
        await $('~password_input').setValue('secret123');

        // ซ่อน keyboard
        await driver.hideKeyboard();

        // กด Login
        await $('~login_button').click();

        // ตรวจผล
        await $('~home_screen').waitForDisplayed({ timeout: 15000 });
        await expect(await $('~home_screen')).toBeDisplayed();
    });
});
```

### Intermediate: Scroll หา item ใน list

```javascript
// test/specs/transaction.test.js
// tested: WDIO v9

describe('Transaction List', () => {
    it('should find transaction by scrolling', async () => {
        // เข้าหน้า transactions
        await $('~history_tab').click();
        await $('~transaction_list').waitForDisplayed({ timeout: 10000 });

        // Scroll ลงหา transaction เฉพาะ
        let found = false;
        for (let i = 0; i < 5; i++) {
            const elements = await $$('~transaction_item');
            for (const el of elements) {
                const text = await el.getText();
                if (text.includes('Transfer to ABC')) {
                    found = true;
                    await el.click();
                    break;
                }
            }
            if (found) break;

            // scroll down
            await driver.execute('mobile: scroll', { direction: 'down' });
        }

        expect(found).toBe(true);
    });
});
```

### Advanced: Swipe to Delete + Verify

```javascript
// test/specs/recipients.test.js
// tested: WDIO v9

describe('Saved Recipients', () => {
    it('should delete recipient by swiping left', async () => {
        await $('~recipients_tab').click();
        await $('~recipient_john').waitForDisplayed({ timeout: 10000 });

        // หา element location
        const recipient = await $('~recipient_john');
        const location = await recipient.getLocation();
        const size = await recipient.getSize();

        // Swipe left บน element
        const startX = location.x + size.width - 10;
        const startY = location.y + size.height / 2;
        const endX = location.x + 10;

        await driver.touchAction([
            { action: 'press', x: startX, y: startY },
            { action: 'wait', ms: 200 },
            { action: 'moveTo', x: endX, y: startY },
            { action: 'release' }
        ]);

        // กด Delete ที่ปรากฏ
        await $('~delete_button').waitForDisplayed({ timeout: 5000 });
        await $('~delete_button').click();

        // ตรวจว่า recipient หายไป
        await expect(await $('~recipient_john')).not.toBeDisplayed();
    });
});
```

---

## Common Mistakes

❌ **ใช้ `$$('~item')` แล้วลืม `await`**
```javascript
const items = $$('~transaction_item');  // ❌ ได้ Promise ไม่ใช่ array
items.length                            // ❌ undefined
```
✅ **ใช้ `await $$()` เสมอ**
```javascript
const items = await $$('~transaction_item');  // ✅ array of elements
```
*(source: webdriver.io/docs/selectors)*

---

❌ **ใช้ `setValue` กับ field ที่มี text เดิม โดยหวังว่ามันจะ append**
→ `setValue` จะ clear ก่อนแล้วพิมพ์ใหม่
✅ **ถ้าต้องการ append ใช้ `addValue`** ถ้าต้องการ replace ใช้ `setValue`
*(source: webdriver.io/docs/api/element/setValue)*

---

❌ **ใช้ UiSelector syntax ผิด**
```javascript
$('android=resourceId("com.app:id/btn")')  // ❌ ผิด syntax
```
✅ **ต้องใช้ `new UiSelector()` เสมอ**
```javascript
$('android=new UiSelector().resourceId("com.app:id/btn")')  // ✅
```
*(source: webdriver.io/docs/selectors)*

---

## สรุปบท

ลองตอบก่อนดูเฉลย:

> **คำถาม 1:** จะหา element จาก resource-id `com.nimble.bank:id/tv_balance` ด้วย WDIO syntax อย่างไร? เขียน selector มาให้ครบ

> **คำถาม 2:** ต่างกันยังไงระหว่าง `setValue` กับ `addValue`? และ `waitForDisplayed({ reverse: true })` หมายความว่าอะไร?

> **คำถาม 3:** `$$('~item')` กับ `$('~item')` ต่างกันยังไง? และใช้แต่ละอันเมื่อไหร่?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย 1:**
```javascript
await $('android=new UiSelector().resourceId("com.nimble.bank:id/tv_balance")')
// หรือถ้ามี accessibility_id:
await $('~account_balance')
```

**เฉลย 2:** `setValue` = clear แล้วพิมพ์ใหม่ / `addValue` = append ต่อท้าย text ที่มีอยู่ / `waitForDisplayed({ reverse: true })` = รอให้ element **หายไป** จากหน้าจอ (ตรงข้ามกับ waitForDisplayed ปกติ)

**เฉลย 3:** `$` = คืน element แรกที่ match / `$$` = คืน array ของ elements ทั้งหมดที่ match ใช้ `$` เมื่อรู้ว่า element unique, ใช้ `$$` เมื่อต้องการหลาย elements เช่น list items

</details>

---

**บทต่อไป:** [บทที่ 6 — Page Objects ด้วย JavaScript Class](06-page-objects-wdio.md)
