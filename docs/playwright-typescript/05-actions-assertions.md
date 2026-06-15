## ก่อนอ่านบทนี้ ลองตอบ:

1. `page.getByRole('button', { name: 'Submit' })` กับ `locator.click()` — อะไรที่ทำให้เกิดการ query DOM จริงๆ และทำไมความแตกต่างนี้ถึงสำคัญ?
2. ถ้าหน้า web มีปุ่ม "Delete" 5 ตัว (ทุก todo item มีปุ่มตัวเอง) และคุณต้องการลบเฉพาะ item ที่ชื่อ "Buy groceries" — คุณจะ locate ปุ่มนั้นอย่างไรโดยไม่ใช้ `nth()`?

---

เฉลย:

1. `getByRole()` แค่สร้าง Locator object ไว้ ยังไม่แตะ DOM เลย — การ query DOM จริงเกิดตอนที่ใช้ `.click()` หรือ `expect().toBeVisible()` ความสำคัญคือ Playwright จะ re-evaluate locator ซ้ำทุกครั้งที่ retry ทำให้รองรับ element ที่ยังโหลดไม่เสร็จได้
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

## 3. เนื้อหาหลัก

### 3.1 Actionability Checks — สิ่งที่ Playwright ตรวจก่อนทุก action

ก่อน Playwright จะ click หรือ fill ทุกครั้ง มันจะตรวจก่อนว่า element "พร้อม" จริงๆ — เพราะถ้าคลิกปุ่มที่ยังโหลดอยู่ หรือกรอก input ที่ disabled อยู่ มันก็ไม่มีประโยชน์

Playwright เรียกการตรวจนี้ว่า **actionability checks** *(source: https://playwright.dev/docs/actionability)*

| Check | ความหมาย | ตัวอย่างที่ "ยังไม่ผ่าน" |
|-------|-----------|------------------------|
| **Visible** | element มองเห็นได้จริง ไม่ซ่อนอยู่ | ปุ่มที่ `display: none` หรือ `opacity: 0` |
| **Stable** | element ไม่กำลังขยับหรือ animate | ปุ่มที่กำลัง slide เข้ามา ยัง animate อยู่ |
| **Receives Events** | ไม่มีอะไรทับอยู่ข้างบน ทำให้ click ผ่านไปไม่ได้ | loading overlay หรือ modal ที่ขึ้นทับ |
| **Enabled** | ไม่ถูก disable — ใช้งานได้ | ปุ่ม "Submit" ที่สีเทา กดไม่ได้ |
| **Editable** | เป็น input ที่แก้ไขได้ ไม่ใช่ read-only | input ที่ `readonly` อ่านได้แต่พิมพ์ไม่ได้ |

**ไม่ใช่ทุก action ต้องผ่านทุก check — ขึ้นอยู่กับว่าทำอะไร:**

| Action | ต้องผ่าน check อะไรบ้าง |
|--------|------------------------|
| `click()`, `dblclick()`, `check()`, `uncheck()` | visible + stable + receives events + enabled |
| `hover()`, `dragTo()` | visible + stable + receives events |
| `fill()`, `clear()` | visible + enabled + editable |
| `selectOption()` | visible + enabled |
| `press()`, `pressSequentially()` | ไม่มี — ทำงานทันที |

**ทำไมต้องรู้เรื่องนี้?**

เวลา test ค้างอยู่นานผิดปกติ ส่วนใหญ่มาจาก actionability check ที่รอ element "พร้อม" เช่น รอ loading overlay หายไปก่อน click — Playwright จัดการให้อัตโนมัติ ไม่ต้องเพิ่ม `waitForTimeout` เอง

---

### 3.2 Actions — การกระทำบน Element

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

**`fill(value)`** — เหมือน copy-paste ใส่ค่าทั้งหมดทีเดียว:
- ล้าง input เดิมออก แล้วใส่ค่าใหม่ในครั้งเดียวเลย
- เหมาะกับ input ทั่วไป เช่น email, ชื่อ, ที่อยู่
- เร็วมาก

**`pressSequentially(text)`** — เหมือนพิมพ์จริงๆ ทีละตัวอักษร:
- จำลองการกดแป้นพิมพ์ทีละตัว เหมือนผู้ใช้นั่งพิมพ์จริงๆ
- เหมาะกับ input ที่ "ฟัง" การพิมพ์ระหว่างทาง เช่น:
  - **masked input** — phone number ที่พอพิมพ์ `0812345678` แล้วจัดรูปแบบเป็น `(081) 234-5678` ให้เองอัตโนมัติ — ถ้าใช้ `fill()` ค่าจะเป็น `0812345678` เฉยๆ เพราะ library ไม่รู้ว่าผู้ใช้พิมพ์
  - **autocomplete** — search box ที่ต้องพิมพ์ก่อนถึงจะโชว์ dropdown
  - **real-time validation** — form ที่แสดง error ขณะพิมพ์
- ช้ากว่า `fill()` แต่ simulate พฤติกรรมผู้ใช้จริงได้

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

### 3.3 Page-level Navigation

```typescript
// partial example — see Section 5 for runnable version
// ไปยัง URL — default รอ load event (HTML + CSS + JS โหลดครบ)
await page.goto('http://localhost:3000/todos');

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

`page.goto()` รอ `load` event โดย default — แปลว่ารอให้ HTML, CSS, JS โหลดครบก่อน return หลังจากนั้นถ้าต้องการรอ element เฉพาะให้ใช้ `expect(locator).toBeVisible()` แทนการระบุ `waitUntil` *(source: https://playwright.dev/docs/navigations)*

**`waitUntil` option** มีไว้สำหรับกรณีพิเศษเท่านั้น:

| option | ใช้เมื่อ |
|--------|---------|
| `'domcontentloaded'` | ต้องการ start interact เร็วที่สุด ก่อน JS โหลดเสร็จ |
| `'networkidle'` | SPA ที่ fetch API data หลัง load event และไม่มี WebSocket |

```typescript
// ❌ อย่าใช้บน demo app — มี WebSocket เปิดอยู่ตลอด ทำให้ networkidle ไม่มีวันครบ
await page.goto('/login', { waitUntil: 'networkidle' });

// ✅ วิธีที่ถูกต้อง — รอ element ที่ต้องการโดยตรง
await page.goto('/login');
await expect(page.getByTestId('btn-login')).toBeVisible();
```

---

### 3.4 Web-First Assertions — assert แบบ Playwright

ใน Playwright มี assertion สองแบบ — ความแตกต่างคือ "รอไหม":

**แบบที่ 1 — Regular assertion** (ไม่รอ ตรวจทันที):
```typescript
expect(isVisible).toBe(true)
// ตรวจค่า ณ วินาทีนั้น — ถ้าไม่ผ่านก็ fail ทันที
```

**แบบที่ 2 — Web-first assertion** (รอและ retry อัตโนมัติ):
```typescript
expect(page.locator('.success-msg')).toBeVisible()
// ตรวจซ้ำทุก 100ms จนกว่าจะผ่าน หรือจนครบ 5 วินาที แล้วค่อย fail
```

ทำไม Playwright ต้องมีแบบที่ 2 — เพราะ web ทำงาน async เสมอ element ไม่ได้โผล่ทันทีหลัง click มักมี network call, animation, หรือ JavaScript ที่ต้องรันก่อน ถ้าตรวจทันทีแบบที่ 1 test จะ fail บ่อยทั้งที่ไม่ควรพัง

**กฎง่ายๆ: ถ้าส่ง Locator เข้า `expect()` → retry อัตโนมัติ**

*(source: https://playwright.dev/docs/test-assertions)*

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

### 3.5 Soft Assertions — เก็บทุก error ก่อน report

ปกติถ้า assertion แรก fail — test หยุดทันที ไม่รู้ว่า assertion ที่เหลือจะผ่านหรือเปล่า:

```typescript
// ❌ แบบปกติ — หยุดที่ error แรก
await expect(page.getByTestId('first-name')).toHaveValue('John');  // fail → หยุดเลย
await expect(page.getByTestId('last-name')).toHaveValue('Doe');    // ไม่ได้รัน
await expect(page.getByTestId('email')).toHaveValue('john@x.com'); // ไม่ได้รัน
// รู้แค่ว่า first-name ผิด ไม่รู้ field อื่น
```

**Soft assertion** แก้ปัญหานี้ — ถ้า fail จะ **จดไว้แต่ไม่หยุด** รัน assertion ต่อจนครบ แล้วรายงานทุก error รวมกันตอนจบ:

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

### 3.6 Custom Matchers — expect.extend()

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

### 3.7 RF/Selenium vs Playwright — Comparison Table

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

## 4. ตัวอย่าง 3 ระดับ

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

### Intermediate: ตรวจสอบ error state หลาย condition พร้อมกันด้วย Soft Assertions

สถานการณ์: Login ล้มเหลว — ต้องการ verify ว่า error message แสดงถูกต้อง, form ยังอยู่บนหน้า, และ URL ไม่ redirect ออกไป ทั้งหมดในครั้งเดียว

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

test('แสดง error state ครบทุก condition เมื่อ login ไม่สำเร็จ', async ({ page }) => {
  await page.goto('/login');

  await page.getByLabel('Username').fill('wronguser');
  await page.getByLabel('Password').fill('wrongpass');
  await page.getByRole('button', { name: 'Login' }).click();

  // ใช้ soft assertions — ตรวจทุก condition พร้อมกัน แม้บางตัว fail จะไม่หยุด
  await expect.soft(page.getByTestId('login-error')).toBeVisible();
  await expect.soft(page.getByTestId('login-error')).toContainText('Invalid credentials');
  await expect.soft(page.getByLabel('Username')).toBeVisible();   // form ยังแสดงอยู่
  await expect.soft(page.getByLabel('Password')).toBeVisible();   // password field ยังอยู่

  // ตรวจว่ายังอยู่หน้า login (ไม่ redirect ออกไป)
  await expect(page).toHaveURL('/login');
});
```

ทำไมใช้ soft assertions ที่นี่? ถ้าใช้ `expect()` ธรรมดา และ `login-error` ไม่แสดง test จะหยุดทันที — คุณจะไม่รู้ว่า form ยังอยู่หรือไม่ และ URL ยังถูกต้องไหม soft assertions ทำให้เห็นภาพรวมของทุก condition ในรอบเดียว

---

### Advanced: วิเคราะห์ fill() vs pressSequentially() และ Actionability Failures

สถานการณ์นี้เป็น diagnosis — ทีมรับ bug report ว่า test ที่ทำงานกับ phone number field พัง เหตุผลไม่ชัด

```typescript
// partial example — สาธิต concept สำหรับ app ที่มี masked input
// (demo app ไม่มี masked phone field — ปรับ route/selector ให้ตรงกับ app จริงก่อนรัน)
import { test, expect } from '@playwright/test';

// ❌ TEST ที่พัง — ทีมรายงานว่า phone ไม่ถูก format หลัง submit
test.skip('broken — fill() with masked input', async ({ page }) => {
  await page.goto('/profile');

  // Phone field มี input mask: (___) ___-____
  // fill() แทนที่ content ทั้งหมดในครั้งเดียว
  await page.getByLabel('Phone Number').fill('0812345678');

  // ปัญหา: mask ไม่ได้ trigger เพราะ fill() ไม่ emit keystroke events
  // ผลลัพธ์ใน DOM: "0812345678" ไม่ใช่ "(081) 234-5678"
  await expect(page.getByLabel('Phone Number')).toHaveValue('(081) 234-5678');
  // → FAIL: received "0812345678"
});

// ✅ วิธีที่ถูก — ใช้ pressSequentially() กับ masked input
test.skip('phone number formats correctly with mask', async ({ page }) => {
  await page.goto('/profile');

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

## 5. Common Mistakes ❌→✅

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

## 6. สรุปบท + Retrieval Questions

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

<details>
<summary>ดูเฉลย</summary>

**คำถามที่ 1:** `<select>` ใช้ `selectOption()` เพราะ dropdown ไม่ใช่ text input และ Playwright มี API เฉพาะสำหรับ select element ที่จัดการ option matching ให้ครบ — credit card number ที่มี mask ควรใช้ `pressSequentially()` เพราะ mask library ต้องการ keystroke events ทีละตัวเพื่อจัด format ระหว่างพิมพ์ ถ้าใช้ `fill()` mask จะไม่ทำงานและค่าใน DOM จะเป็นตัวเลขล้วนแทนที่จะเป็น `1234 5678 9012 3456`

**คำถามที่ 2:** test นี้จะ **fail อย่างไม่น่าเชื่อถือ** — ปัญหาคือ `isVisible()` คือ snapshot ณ ขณะที่ call ถ้า item ยังไม่ถูกลบออกจาก DOM ทันที (มี animation หรือ async deletion) `isVisible()` จะคืน `true` ทำให้ `expect(isGone).toBe(false)` fail ทั้งที่ item จะหายไปในอีกไม่กี่ millisecond วิธีแก้: `await expect(page.getByTestId('item-1')).not.toBeVisible()` — web-first assertion จะ retry จนกว่า item จะหายไปจริง

**คำถามที่ 3:** ควรใช้ soft assertions เมื่อต้องการตรวจหลาย field/condition พร้อมกันและต้องการเห็น error ทุกข้อในรอบเดียว — ตัวอย่างที่ดี: form validation ที่ต้องการรู้ว่า field ไหน fail บ้างทั้งหมด, หน้า dashboard ที่ต้องตรวจ widget ทุกตัว ไม่ควรใช้เมื่อ assertion ถัดไป depend on assertion ก่อนหน้า — เช่น ถ้า assert ว่า "modal เปิดแล้ว" fail แล้วยัง assert "ปุ่มใน modal clickable" ต่อไป test จะ confusing มากกว่าได้ประโยชน์

</details>
