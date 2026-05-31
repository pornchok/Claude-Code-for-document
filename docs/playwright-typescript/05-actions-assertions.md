## ก่อนอ่านบทนี้ ลองตอบ:

1. Locator ใน Playwright "lazy" หมายความว่าอะไร — และนี่ทำให้ auto-retry ทำงานได้อย่างไร?
2. ถ้าหน้า web มีปุ่ม "Delete" 5 ตัว (ทุก todo item มีปุ่มตัวเอง) และคุณต้องการลบเฉพาะ item ที่ชื่อ "Buy groceries" — คุณจะ locate ปุ่มนั้นอย่างไรโดยไม่ใช้ `nth()`?

---

เฉลย:

1. "Lazy" หมายความว่า Locator คือ description ของ element ยังไม่ได้ query DOM เลยจนกว่าจะมี action (`click()`, `fill()`) หรือ assertion (`expect().toBeVisible()`) — เมื่อมี action Playwright จะ re-evaluate locator ซ้ำในทุก retry cycle จนครบ timeout ทำให้รองรับ element ที่ยังไม่ปรากฏได้โดยไม่ต้องรอ manual
2. ใช้ chaining + filter: `page.getByRole('listitem').filter({ hasText: 'Buy groceries' }).getByRole('button', { name: 'Delete' }).click()` — หา listitem ที่มี text นั้นก่อน แล้วหาปุ่ม Delete ใน scope ของมัน

# บทที่ 5: Actions & Assertions

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- เรียกใช้ action พื้นฐาน (`click`, `fill`, `press`, `hover`, `dragTo`) และเลือกได้ถูกต้องระหว่าง `fill()` กับ `pressSequentially()`
- อธิบาย actionability checks ที่ Playwright ทำโดยอัตโนมัติก่อนทุก action ได้ว่ามีอะไรบ้างและทำไม
- เขียน web-first assertion ด้วย `expect()` และอธิบายความต่างจาก regular assertion ที่ไม่ retry
- ใช้ soft assertions เพื่อ collect ทุก error ในรอบเดียว แทนที่จะหยุดที่ error แรก
- สร้าง custom matcher ด้วย `expect.extend()` สำหรับ assertion ที่ใช้ซ้ำในโปรเจค
- เปรียบเทียบ actions/assertions ใน Playwright กับ Robot Framework + Selenium ได้ชัดเจน

---

## 2. ทำไมต้องรู้? (Why)

ลองนึกภาพ test นี้:

```typescript
// ❌ แบบที่ developer ใหม่มักเขียน
await page.click('#submit-btn');
await page.waitForTimeout(2000);  // รอ 2 วินาทีเผื่อ
const isVisible = await page.locator('.success-msg').isVisible();
expect(isVisible).toBe(true);     // ถ้าไม่ visible จะ fail ทันที ไม่ retry
```

test นี้มีปัญหา 3 จุด:
- `waitForTimeout(2000)` เดาเวลา — machine เร็วผ่าน, CI server ช้าพัง
- `isVisible()` คือ snapshot ณ ขณะที่ call — ถ้า message โผล่หลัง 50ms test พังโดยไม่ควรพัง
- `expect(isVisible).toBe(true)` ไม่ retry เลย ถ้าค่าเป็น false ณ วินาทีนั้น = fail

แต่ถ้าเขียนแบบ Playwright-native:

```typescript
// ✅ แบบที่ถูก
await page.getByRole('button', { name: 'Submit' }).click();
await expect(page.locator('.success-msg')).toBeVisible();
```

สองบรรทัดนี้ทำงานได้ถูกต้องเพราะ Playwright เข้าใจ "ความเป็นจริงของ browser" — element ใช้เวลาปรากฏ, animation ยังไม่เสร็จ, network ยังโหลด — และ framework จัดการให้ทั้งหมดโดยไม่ต้องเดาเวลา

บทนี้คือการเข้าใจว่า Playwright จัดการความซับซ้อนเหล่านั้นอย่างไร

---

## 3. Analogy: Auto-waiting คือ Waiter ที่รอให้อาหารพร้อมก่อนเสิร์ฟ

ลองนึกภาพคุณนั่งในร้านอาหารระดับดี

**Waiter แบบเก่า (Selenium style):** ทันทีที่คุณสั่ง เขาก็เดินไปหยิบจานจากครัวในวินาทีนั้นเลย — ถ้าอาหารยังไม่พร้อม ได้จานเปล่ากลับมา หรือพ่อครัวบอกว่า "ยังไม่มี" แล้วรายงานว่า error

**Waiter แบบ Playwright:** เขา "รู้" ว่าต้องรอให้อาหารอยู่บนจาน จานวางนิ่งบนโต๊ะแล้ว และจานเย็นพอให้หยิบจับได้ — ทุก criteria ผ่านก่อน จึงค่อยเสิร์ฟ

ใน Playwright ทุกครั้งที่เรียก `click()` หรือ `fill()` Playwright จะรอจนกว่า element จะ "actionable" ตาม criteria ที่กำหนดก่อน — ไม่ใช่แค่ "อยู่ใน DOM แล้ว" แต่ต้องพร้อมจริงๆ

---

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**

- **Auto-waiting รอทุกอย่างได้จนกว่า business logic จะเสร็จ** — จริงๆ แล้ว auto-waiting รอเฉพาะ "actionability" ของ element ตาม criteria ที่กำหนด (visible, stable, enabled, editable, receives events) เท่านั้น ไม่ได้รอว่า API call จะ return, ว่า animation transition จะสมบูรณ์ 100%, หรือว่า database จะ commit — ถ้าต้องรอสิ่งเหล่านั้น ต้องใช้ assertion web-first เพิ่มเติม เช่น `await expect(page.getByText('Saved')).toBeVisible()`

- **`expect(await locator.isVisible()).toBe(true)` กับ `expect(locator).toBeVisible()` เหมือนกัน** — ต่างกันมาก: แบบแรกเรียก `isVisible()` ณ วินาทีนั้นแล้วได้ค่า boolean มา — ถ้า element ยังไม่ปรากฏก็ได้ `false` ทันที ไม่ retry เลย แบบที่สองคือ web-first assertion ที่ Playwright จะ retry การตรวจซ้ำจนครบ timeout (default 5 วินาที) — ต่างกันอย่างสิ้นเชิง

---

## 4. เนื้อหาหลัก

### 4.1 Actionability Checks — สิ่งที่ Playwright ตรวจก่อนทุก action

ก่อน Playwright จะ click, fill, หรือทำ action ใดๆ มันตรวจ criteria เหล่านี้โดยอัตโนมัติ:

| Check | ความหมาย |
|-------|-----------|
| **Visible** | element มี bounding box ที่ไม่ว่างเปล่า และไม่มี `visibility:hidden` |
| **Stable** | element ไม่ขยับ — bounding box เดิมสองรอบติดกัน (animation เสร็จแล้ว) |
| **Receives Events** | element รับ pointer event ได้ — ไม่ถูก element อื่นบัง |
| **Enabled** | element ไม่ถูก disable (`disabled` attribute) |
| **Editable** | element enabled และไม่ readonly |

**ไม่ใช่ทุก action ต้องผ่านทุก check:**

| Action | Checks ที่ต้องผ่าน |
|--------|------------------|
| `click()`, `dblclick()`, `check()`, `uncheck()` | ทั้ง 5: visible + stable + receives events + enabled + (editable ไม่นับ) |
| `hover()`, `dragTo()` | visible + stable + receives events |
| `fill()`, `clear()` | visible + enabled + editable |
| `selectOption()` | visible + enabled |
| `press()`, `pressSequentially()` | ไม่มี check — ทำงานทันที |

ข้อมูลนี้มาจาก playwright.dev/docs/actionability โดยตรง — ทำให้เข้าใจว่าทำไม `pressSequentially()` ถึงใช้กับ hidden input ได้ แต่ `fill()` ไม่ได้

---

### 4.2 Actions — การกระทำบน Element

#### click() และ dblclick()

```typescript
// partial example — see Section 5 for runnable version
// click ธรรมดา
await page.getByRole('button', { name: 'Submit' }).click();

// double-click — ใช้สำหรับ edit inline (เช่น todo list)
await page.getByTestId('todo-text-1').dblclick();

// click ที่ตำแหน่งเฉพาะ หรือพร้อม modifier key
await page.getByRole('button').click({ button: 'right' });   // right-click
await page.getByText('Item').click({ modifiers: ['Shift'] }); // Shift+click
```

`dblclick()` ใช้บ่อยใน inline-edit pattern — เช่น todo app ที่ double-click บน text เพื่อเปิด edit mode

---

#### fill() vs pressSequentially()

นี่คือความแตกต่างที่สำคัญ — เลือกผิดทำให้ test พังหรือได้ผลลัพธ์ผิด:

**`fill(value)`** — แทนที่ content ทั้งหมดทันที:
- ล้าง input เดิมออกก่อน แล้วใส่ค่าใหม่ในครั้งเดียว
- ไม่ emit keydown/keypress/keyup events ทีละตัว
- เหมาะกับ input ทั่วไปที่ไม่มี validation ขณะพิมพ์
- เร็วกว่ามาก

**`pressSequentially(text)`** — จำลอง keystroke ทีละตัว:
- emit keydown, keypress, keyup ต่อทุกตัวอักษร
- เหมาะกับ input ที่มี mask (เช่น phone number `(___) ___-____`), autocomplete dropdown, หรือ real-time validation ขณะพิมพ์
- ช้ากว่า แต่ simulate user จริงกว่า

```typescript
// partial example — see Section 5 for runnable version
// ✅ ใช้ fill() สำหรับ input ทั่วไป
await page.getByLabel('Email').fill('user@example.com');

// ✅ ใช้ pressSequentially() สำหรับ masked input หรือ autocomplete
await page.getByLabel('Phone').pressSequentially('0812345678');
// → จำลองการพิมพ์: 0, 8, 1, 2, ... ทีละตัว trigger validation ระหว่างพิมพ์

// เพิ่ม delay ระหว่าง keystroke ถ้าต้องการ
await page.getByRole('combobox', { name: 'City' }).pressSequentially('Bang', { delay: 100 });
// → พิมพ์ช้าๆ เพื่อให้ autocomplete dropdown โหลดก่อน
```

---

#### clear(), selectOption(), check(), uncheck()

```typescript
// partial example — see Section 5 for runnable version
// ล้าง input
await page.getByLabel('Search').clear();

// เลือก option ใน <select>
await page.getByLabel('Country').selectOption('TH');                    // by value
await page.getByLabel('Country').selectOption({ label: 'Thailand' });   // by label
await page.getByLabel('Size').selectOption(['S', 'M']);                 // multiple

// checkbox และ radio
await page.getByLabel('Remember me').check();
await page.getByLabel('Remember me').uncheck();
await page.getByLabel('Accept terms').setChecked(true);  // toggle ตาม boolean
```

---

#### hover() และ dragTo()

```typescript
// partial example — see Section 5 for runnable version
// hover เพื่อเปิด dropdown หรือ tooltip
await page.getByRole('button', { name: 'Options' }).hover();
await expect(page.getByRole('menu')).toBeVisible();

// drag and drop
const source = page.getByTestId('todo-item-1');
const target = page.getByTestId('todo-item-3');
await source.dragTo(target);
```

---

#### press() — keyboard shortcuts

```typescript
// partial example — see Section 5 for runnable version
// Single key
await page.getByLabel('Search').press('Enter');
await page.getByLabel('Username').press('Tab');  // focus ไปที่ field ถัดไป

// Modifier + key
await page.getByRole('textbox').press('Control+A');  // select all
await page.getByRole('textbox').press('Control+C');  // copy
await page.keyboard.press('Escape');                  // global keypress
```

`press()` ไม่มี actionability check — ทำงานทันทีบน element ที่ focused

---

### 4.3 Page-level Navigation

```typescript
// partial example — see Section 5 for runnable version
// ไปยัง URL
await page.goto('http://localhost:3000/todos');
await page.goto('http://localhost:3000/login', { waitUntil: 'networkidle' });

// navigation ย้อนหน้า
await page.goBack();
await page.goForward();
await page.reload();

// รัน JavaScript ใน browser context
const title = await page.evaluate(() => document.title);
const count = await page.evaluate(() => {
  return document.querySelectorAll('[data-testid^="todo-item-"]').length;
});
```

`page.goto()` รอ `load` event โดย default — แปลว่ารอให้ HTML, CSS, JS โหลดครบก่อน return *(source: https://playwright.dev/docs/navigations)*

---

### 4.4 Web-First Assertions — assert แบบ Playwright

Playwright มี assertion เฉพาะที่ทำงานต่างจาก Jest/Jasmine ทั่วไป — เรียกว่า "web-first assertions"

ความต่างหลัก:
- **Regular assertion** (`expect(value).toBe(...)`) — ตรวจ ณ วินาทีที่ call ทันที ถ้าไม่ผ่าน = fail
- **Web-first assertion** (`expect(locator).toBeVisible()`) — retry จนครบ timeout (default 5 วินาที) แล้วค่อย fail ถ้ายังไม่ผ่าน

#### Visibility & State

```typescript
// partial example — see Section 5 for runnable version
await expect(page.getByRole('alert')).toBeVisible();
await expect(page.getByRole('dialog')).toBeHidden();
await expect(page.getByRole('button', { name: 'Submit' })).toBeEnabled();
await expect(page.getByRole('button', { name: 'Delete' })).toBeDisabled();
await expect(page.getByLabel('Accept terms')).toBeChecked();
await expect(page.getByLabel('Newsletter')).not.toBeChecked();
await expect(page.getByLabel('Email')).toBeEditable();
await expect(page.getByLabel('Username')).toBeFocused();
```

#### Content & Attribute

```typescript
// partial example — see Section 5 for runnable version
// ตรวจ text ทั้งหมด
await expect(page.getByTestId('todo-text-1')).toHaveText('Buy groceries');

// ตรวจว่า contain (ไม่ต้อง exact match)
await expect(page.getByRole('status')).toContainText('Saved');

// regex — สำหรับ dynamic content
await expect(page.getByRole('heading')).toContainText(/\d+ items?/);

// input value
await expect(page.getByLabel('Email')).toHaveValue('user@example.com');

// attribute
await expect(page.getByRole('img', { name: 'Avatar' })).toHaveAttribute('src', /\/avatars\//);

// CSS class
await expect(page.getByRole('tab', { name: 'Active' })).toHaveClass(/active/);

// จำนวน element
await expect(page.getByRole('listitem')).toHaveCount(5);
```

#### Page-level Assertions

```typescript
// partial example — see Section 5 for runnable version
await expect(page).toHaveURL('http://localhost:3000/dashboard');
await expect(page).toHaveURL(/\/dashboard/);  // regex
await expect(page).toHaveTitle('My App - Dashboard');
```

#### Accessibility Assertions (v1.44+)

```typescript
// partial example — see Section 5 for runnable version
// ตรวจ accessible name (สิ่งที่ screen reader อ่าน)
await expect(page.getByRole('button')).toHaveAccessibleName('Close dialog');

// ตรวจ ARIA role
await expect(page.locator('.loader')).toHaveRole('progressbar');
```

#### toMatchAriaSnapshot() — mention only

`toMatchAriaSnapshot()` ใช้ capture และ compare โครงสร้าง ARIA ของ component ทั้งหมดในครั้งเดียว — เหมาะมากสำหรับ accessibility regression testing บทที่ 16 จะ deep dive เรื่องนี้โดยเฉพาะ

---

### 4.5 Soft Assertions — เก็บทุก error ก่อน report

ปกติ test หยุดที่ assertion แรกที่ fail:

```typescript
// ❌ แบบ regular — หยุดที่ error แรก ไม่รู้ว่า error ที่เหลือมีอะไรบ้าง
await expect(page.getByTestId('first-name')).toHaveValue('John');   // fail → หยุด
await expect(page.getByTestId('last-name')).toHaveValue('Doe');      // ไม่ได้รัน
await expect(page.getByTestId('email')).toHaveValue('john@x.com');   // ไม่ได้รัน
```

Soft assertion ใช้ `expect.soft()` — ถ้า fail จะ **บันทึก error ไว้แต่ไม่หยุด** test ยังรันต่อ แล้วรายงานทุก error ตอนจบ:

```typescript
// ✅ Soft assertions — รัน assertion ทุกข้อ แล้ว report ทุก error รวมกัน
await expect.soft(page.getByTestId('first-name')).toHaveValue('John');
await expect.soft(page.getByTestId('last-name')).toHaveValue('Doe');
await expect.soft(page.getByTestId('email')).toHaveValue('john@x.com');

// ถ้าอยากหยุดแบบ manual หลังจาก soft assertions
// ตรวจด้วย test.info().errors.length
```

เหมาะมากสำหรับ form validation testing ที่ต้องการ verify หลาย field พร้อมกัน — แทนที่จะต้อง debug ทีละ field

---

### 4.6 Custom Matchers — expect.extend()

ถ้า assertion เดิมซ้ำในหลายที่ สร้าง custom matcher ได้:

```typescript
// partial example — see Section 5 for runnable version
// fixtures/matchers.ts
import { expect } from '@playwright/test';

// สร้าง custom matcher
expect.extend({
  async toBeLoggedIn(page: any) {
    const userMenu = page.getByTestId('user-menu');
    const isVisible = await userMenu.isVisible();
    return {
      pass: isVisible,
      message: () => isVisible
        ? 'Expected page NOT to be logged in, but user menu is visible'
        : 'Expected page to be logged in, but user menu is not visible',
    };
  },
});

// ใช้งาน — อ่านเข้าใจได้ทันทีว่า assert อะไร
await expect(page).toBeLoggedIn();
await expect(page).not.toBeLoggedIn();
```

---

### 4.7 RF/Selenium vs Playwright — Comparison Table

| กรณีใช้งาน | Robot Framework + Selenium | Playwright |
|------------|---------------------------|------------|
| Click element | `Click Element    id:submit` + `Wait Until Element Is Enabled` | `click()` — auto-wait ในตัว |
| Fill input | `Input Text    id:email    user@x.com` | `fill('user@x.com')` |
| Simulate typing | ไม่มี built-in keystroke emulation | `pressSequentially('text')` |
| Assert visible | `Element Should Be Visible    css:.msg` (fail ทันที ถ้าไม่ visible) | `toBeVisible()` (retry จนครบ timeout) |
| Assert text | `Element Should Contain    css:.status    Saved` | `toContainText('Saved')` (retry) |
| Soft assertions | ไม่มี built-in — ต้องใช้ try/except แล้วเก็บ errors เอง | `expect.soft()` built-in |
| Wait for condition | `Wait Until Element Is Visible    css:.modal` | ไม่จำเป็น — `toBeVisible()` retry เอง |
| Custom assertion | library keyword ที่ซับซ้อน | `expect.extend()` |
| Check/Uncheck | `Select Checkbox` / `Unselect Checkbox` | `check()` / `uncheck()` / `setChecked()` |
| Keyboard shortcut | `Press Key    CTRL+A` | `press('Control+A')` |

ความต่างที่สำคัญที่สุด: ใน RF + Selenium ถ้า assertion fail หมายความว่า "ณ วินาทีนั้น element ไม่ตรงตามเงื่อนไข" แต่ใน Playwright หมายความว่า "หลังพยายาม retry ครบ timeout แล้ว element ยังไม่ตรงตามเงื่อนไข" — ทำให้ test เสถียรกว่ามากสำหรับ async UI

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner: เพิ่ม Todo item และ assert ผลลัพธ์

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

test('add a new todo item', async ({ page }) => {
  await page.goto('http://localhost:3000/todos');

  // กรอก input และ submit
  await page.getByTestId('input-new-todo').fill('Buy groceries');
  await page.getByTestId('btn-add-todo').click();

  // assert ว่า item ปรากฏใน list
  await expect(page.getByText('Buy groceries')).toBeVisible();

  // assert ว่า input ถูกล้างหลัง submit
  await expect(page.getByTestId('input-new-todo')).toHaveValue('');
});
```

สังเกตว่าไม่มี `waitForTimeout` เลย — `toBeVisible()` retry เองจนกว่า item จะปรากฏ ซึ่ง handle ทั้งกรณีที่ fast และ slow network ได้ในโค้ดเดียว

---

### Intermediate: ตรวจสอบ form validation ด้วย Soft Assertions

สถานการณ์: หน้า Register ต้องแสดง error message ทุก field ที่ไม่ถูกต้องพร้อมกัน — คุณต้องการ verify ว่า error ทุก field แสดงถูกต้องในครั้งเดียว

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

test('shows validation errors for all invalid fields simultaneously', async ({ page }) => {
  await page.goto('http://localhost:3000/register');

  // กรอกข้อมูลที่ invalid ทุก field
  await page.getByLabel('Username').fill('ab');              // ต้องการ >= 3 ตัวอักษร
  await page.getByLabel('Email').fill('not-an-email');       // รูปแบบ email ผิด
  await page.getByLabel('Password').fill('123');             // ต้องการ >= 8 ตัวอักษร
  await page.getByLabel('Confirm Password').fill('456');     // ไม่ match password

  // กด Submit เพื่อ trigger validation
  await page.getByRole('button', { name: 'Register' }).click();

  // ใช้ soft assertions — ตรวจทุก error message แม้บางตัว fail
  await expect.soft(page.getByTestId('error-username')).toContainText('at least 3 characters');
  await expect.soft(page.getByTestId('error-email')).toContainText('valid email');
  await expect.soft(page.getByTestId('error-password')).toContainText('at least 8 characters');
  await expect.soft(page.getByTestId('error-confirm-password')).toContainText('do not match');

  // ตรวจว่ายังอยู่หน้า register (ไม่ redirect ออกไป)
  await expect(page).toHaveURL(/\/register/);
});
```

ทำไมใช้ soft assertions ที่นี่? ถ้าใช้ `expect()` ธรรมดา และ `error-username` ไม่แสดง test จะหยุดทันที — คุณจะไม่รู้ว่า field อื่น error หรือไม่ ต้องรัน test ซ้ำหลายรอบ soft assertions ทำให้เห็นภาพรวมของทุก validation ในรอบเดียว

---

### Advanced: วิเคราะห์ fill() vs pressSequentially() และ Actionability Failures

สถานการณ์นี้เป็น diagnosis — ทีมรับ bug report ว่า test ที่ทำงานกับ phone number field พัง เหตุผลไม่ชัด

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

// ❌ TEST ที่พัง — ทีมรายงานว่า phone ไม่ถูก format หลัง submit
test.skip('broken — fill() with masked input', async ({ page }) => {
  await page.goto('http://localhost:3000/profile');

  // Phone field มี input mask: (___) ___-____
  // fill() แทนที่ content ทั้งหมดในครั้งเดียว
  await page.getByLabel('Phone Number').fill('0812345678');

  // ปัญหา: mask ไม่ได้ trigger เพราะ fill() ไม่ emit keystroke events
  // ผลลัพธ์ใน DOM: "0812345678" ไม่ใช่ "(081) 234-5678"
  await expect(page.getByLabel('Phone Number')).toHaveValue('(081) 234-5678');
  // → FAIL: received "0812345678"
});

// ✅ วิธีที่ถูก — ใช้ pressSequentially() กับ masked input
test('phone number formats correctly with mask', async ({ page }) => {
  await page.goto('http://localhost:3000/profile');

  const phoneInput = page.getByLabel('Phone Number');

  // pressSequentially() emit keystroke ทีละตัว → mask library รับ event แต่ละตัว
  await phoneInput.pressSequentially('0812345678');

  // mask library จัด format ให้ระหว่าง typing
  await expect(phoneInput).toHaveValue('(081) 234-5678');

  await page.getByRole('button', { name: 'Save Profile' }).click();
  await expect(page.getByRole('status')).toContainText('Profile updated');
});

// Actionability failure diagnosis
test.describe('actionability checks in practice', () => {
  test('button behind overlay fails until overlay closes', async ({ page }) => {
    await page.goto('http://localhost:3000/todos');

    // เพิ่ม todo แล้วรอ loading spinner (ถ้ามี)
    await page.getByTestId('input-new-todo').fill('Learn Playwright');
    await page.getByTestId('btn-add-todo').click();

    // Playwright auto-wait — ถ้า "btn-add-todo" ยังมี loading overlay บัง
    // (receives events check fail) → retry จนกว่า overlay จะหายไป
    // ไม่ต้อง waitForTimeout หรือ waitForSelector เพิ่มเติม

    // ตรวจ: double-click บน todo text เพื่อ edit
    const newTodo = page.getByText('Learn Playwright');
    await newTodo.dblclick();

    // edit input ควรปรากฏขึ้นมา
    const editInput = page.getByTestId(`todo-edit-input`);
    await expect(editInput).toBeVisible();
    await expect(editInput).toBeFocused();

    // เปลี่ยน text
    await editInput.fill('Master Playwright');
    await editInput.press('Enter');

    // ตรวจว่า text อัปเดต
    await expect(page.getByText('Master Playwright')).toBeVisible();
    await expect(page.getByText('Learn Playwright')).not.toBeVisible();
  });
});
```

**การวิเคราะห์ Tradeoff:**

| สถานการณ์ | ใช้ | เหตุผล |
|-----------|-----|--------|
| Input ทั่วไป (email, name) | `fill()` | เร็วกว่า, ง่ายกว่า |
| Masked input (phone, credit card) | `pressSequentially()` | Mask library ต้องการ keystroke events |
| Autocomplete/typeahead | `pressSequentially()` + delay | ต้องรอ dropdown โหลดระหว่างพิมพ์ |
| OTP / PIN field | `pressSequentially()` | แต่ละ field อาจเป็น `<input maxlength="1">` แยก |
| Search box ที่ debounce | `fill()` แล้วรอ `toBeVisible()` | fill ครั้งเดียว แล้วรอ dropdown ด้วย assertion |

---

## 6. Common Mistakes ❌→✅

**1. ใช้ isVisible() แทน web-first assertion**

❌
```typescript
const isVisible = await page.locator('.success-banner').isVisible();
expect(isVisible).toBe(true);
```

✅
```typescript
await expect(page.locator('.success-banner')).toBeVisible();
```

เหตุผล: `isVisible()` คือ snapshot ณ ขณะที่ call — ถ้า banner ยังไม่ขึ้นใน millisecond นั้น ได้ `false` ทันที ไม่มีการ retry เลย ส่วน `toBeVisible()` retry จนครบ default timeout 5 วินาที *(source: https://playwright.dev/docs/test-assertions)*

---

**2. ใช้ waitForTimeout แทน web-first assertion**

❌
```typescript
await page.click('[data-testid="btn-save"]');
await page.waitForTimeout(2000);  // รอ 2 วิ เผื่อ
await expect(page.locator('.toast-success')).toBeVisible();
```

✅
```typescript
await page.click('[data-testid="btn-save"]');
await expect(page.locator('.toast-success')).toBeVisible();
// toBeVisible() retry ให้เองจนครบ timeout
```

เหตุผล: `waitForTimeout` เดาเวลา — ถ้า server ช้ากว่า 2 วิ test พัง ถ้า server เร็ว เสีย 2 วิฟรีทุก test run *(source: https://playwright.dev/docs/best-practices)*

---

**3. ใช้ fill() กับ input ที่มี mask หรือ real-time validation**

❌
```typescript
// Phone field มี mask library — fill() ข้าม keystroke events ทั้งหมด
await page.getByLabel('Phone').fill('0812345678');
await expect(page.getByLabel('Phone')).toHaveValue('(081) 234-5678');
// FAIL: value ยังเป็น "0812345678" เพราะ mask ไม่ถูก trigger
```

✅
```typescript
await page.getByLabel('Phone').pressSequentially('0812345678');
await expect(page.getByLabel('Phone')).toHaveValue('(081) 234-5678');
```

เหตุผล: `fill()` เขียน value โดยตรง ไม่ emit keyboard events ตามลำดับ — mask library ต้องการ keydown/keypress/keyup ทีละตัวเพื่อ format ค่าระหว่างพิมพ์ *(source: https://playwright.dev/docs/input)*

---

**4. ลืมว่า dblclick() auto-wait ให้แล้ว**

❌
```typescript
// ไม่จำเป็นต้อง wait ด้วยตัวเอง
await page.waitForSelector('[data-testid="todo-text-1"]');
await page.locator('[data-testid="todo-text-1"]').dblclick();
```

✅
```typescript
// dblclick() auto-wait จนกว่า element จะ visible + stable + receives events
await page.getByTestId('todo-text-1').dblclick();
```

เหตุผล: `dblclick()` ผ่าน actionability checks ทั้ง 5 ก่อนทำงาน — visible, stable, receives events, enabled ครบ — ไม่ต้อง wait นำหน้าด้วย *(source: https://playwright.dev/docs/actionability)*

---

**5. ไม่ใช้ Soft Assertions เมื่อต้องการตรวจหลาย field พร้อมกัน**

❌
```typescript
// test หยุดที่ error แรก — ไม่รู้ว่า field อื่น fail ด้วยหรือไม่
await expect(page.getByTestId('error-name')).toBeVisible();    // fail → หยุด
await expect(page.getByTestId('error-email')).toBeVisible();   // ไม่รัน
await expect(page.getByTestId('error-phone')).toBeVisible();   // ไม่รัน
```

✅
```typescript
// รันทุก assertion แล้ว report รวมตอนจบ
await expect.soft(page.getByTestId('error-name')).toBeVisible();
await expect.soft(page.getByTestId('error-email')).toBeVisible();
await expect.soft(page.getByTestId('error-phone')).toBeVisible();
```

เหตุผล: เมื่อ test form validation การรู้ว่า error ข้อเดียว fail หรือ fail ทั้งหมดมีความหมายต่างกัน — soft assertions ให้ภาพรวมที่สมบูรณ์ในรอบเดียว *(source: https://playwright.dev/docs/test-assertions)*

---

## 7. สรุปบท + Retrieval Questions

ในบทนี้คุณได้เรียน:

- **Actionability checks** — Playwright ตรวจ visible, stable, receives events, enabled, editable อัตโนมัติก่อน action แต่ละ action มี subset ของ checks ที่ต้องผ่านต่างกัน
- **fill() vs pressSequentially()** — fill แทนที่ content ทันที, pressSequentially emit keystroke ทีละตัว — เลือกตาม behavior ของ input
- **Web-first assertions** — retry จนครบ timeout ต่างจาก regular assertion ที่ตรวจครั้งเดียว
- **Soft assertions** — `expect.soft()` collect ทุก error ก่อน report ไม่หยุดที่ error แรก
- **Custom matchers** — `expect.extend()` สำหรับ assertion ที่ใช้ซ้ำบ่อย

---

ก่อนอ่านบทถัดไป ลองตอบคำถามเหล่านี้ด้วยตัวเองก่อน (ไม่ต้อง scroll ขึ้นไปดูเฉลย):

**คำถามที่ 1:** คุณมี `<select>` dropdown สำหรับเลือก payment method และ `<input type="text">` สำหรับใส่ credit card number ที่มี mask `____ ____ ____ ____` — ทั้งสอง field ควรใช้ action อะไร? และทำไม?

**คำถามที่ 2:** test นี้จะทำงานถูกต้องไหม? ถ้าไม่ — ปัญหาคืออะไรและแก้ยังไง?
```typescript
await page.click('[data-testid="btn-delete"]');
const isGone = await page.locator('[data-testid="item-1"]').isVisible();
expect(isGone).toBe(false);
```

**คำถามที่ 3:** เมื่อไหรควรใช้ soft assertions และเมื่อไหรไม่ควรใช้? ให้ยกตัวอย่างกรณีที่ soft assertions ทำให้ test ดีขึ้น และกรณีที่ไม่ควรใช้

---

เฉลย:

**คำถามที่ 1:** `<select>` ใช้ `selectOption()` เพราะ dropdown ไม่ใช่ text input และ Playwright มี API เฉพาะสำหรับ select element ที่จัดการ option matching ให้ครบ — credit card number ที่มี mask ควรใช้ `pressSequentially()` เพราะ mask library ต้องการ keystroke events ทีละตัวเพื่อจัด format ระหว่างพิมพ์ ถ้าใช้ `fill()` mask จะไม่ทำงานและค่าใน DOM จะเป็นตัวเลขล้วนแทนที่จะเป็น `1234 5678 9012 3456`

**คำถามที่ 2:** test นี้จะ **fail อย่างไม่น่าเชื่อถือ** — ปัญหาคือ `isVisible()` คือ snapshot ณ ขณะที่ call ถ้า item ยังไม่ถูกลบออกจาก DOM ทันที (มี animation หรือ async deletion) `isVisible()` จะคืน `true` ทำให้ `expect(isGone).toBe(false)` fail ทั้งที่ item จะหายไปในอีกไม่กี่ millisecond วิธีแก้: `await expect(page.getByTestId('item-1')).not.toBeVisible()` — web-first assertion จะ retry จนกว่า item จะหายไปจริง

**คำถามที่ 3:** ควรใช้ soft assertions เมื่อต้องการตรวจหลาย field/condition พร้อมกันและต้องการเห็น error ทุกข้อในรอบเดียว — ตัวอย่างที่ดี: form validation ที่ต้องการรู้ว่า field ไหน fail บ้างทั้งหมด, หน้า dashboard ที่ต้องตรวจ widget ทุกตัว ไม่ควรใช้เมื่อ assertion ถัดไป depend on assertion ก่อนหน้า — เช่น ถ้า assert ว่า "modal เปิดแล้ว" fail แล้วยัง assert "ปุ่มใน modal clickable" ต่อไป test จะ confusing มากกว่าได้ประโยชน์
