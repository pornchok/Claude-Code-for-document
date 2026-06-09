## ก่อนอ่านบทนี้ ลองตอบ:

1. ทำไม Playwright ถึงสร้าง BrowserContext ใหม่ก่อนทุก test โดยอัตโนมัติ — และ isolation นี้หมายความว่าอะไรในทางปฏิบัติ?
2. ถ้าคุณต้องการ test ที่จำลอง 2 users login พร้อมกัน คุณจะใช้กลไกใดใน Playwright และทำไมไม่ใช้แค่ `page` fixture เดียว?

---

เฉลย:

1. Playwright สร้าง BrowserContext ใหม่ทุก test เพราะ BrowserContext เปรียบเหมือน "incognito profile" ที่แยก cookies, localStorage, sessionStorage จากกันสมบูรณ์ — ในทางปฏิบัติหมายความว่า test ที่ล็อกอินใน test หนึ่งจะไม่ "รั่ว" session ไปยัง test อื่น และถ้า test หนึ่งพัง state ก็ไม่กระทบ test ถัดไป
2. ต้องสร้าง BrowserContext แยกกัน 2 context โดยใช้ `browser` fixture แล้วเรียก `browser.newContext()` สองครั้ง เพราะ `page` fixture เดียวกันอยู่ใน context เดียวกัน — login user B จะ overwrite session ของ user A โดยไม่ตั้งใจ

# บทที่ 4: Locators — Accessibility-First

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- อธิบายความแตกต่างระหว่าง semantic locator (`getByRole`, `getByLabel`) กับ structural locator (`locator('#id')`, `locator('.class')`) ได้ พร้อมบอกได้ว่าแบบไหนดีกว่าและทำไม
- ใช้ locator ทั้ง 8 ประเภทได้ถูกต้อง ตามลำดับ priority ที่ Playwright แนะนำ
- filter และ chain locator เพื่อจัดการ list, table, และ element ซับซ้อนได้
- วิเคราะห์และแก้ StrictModeViolationError ที่เกิดจาก locator match หลาย element
- เปรียบเทียบ locator strategy ระหว่าง Playwright กับ Robot Framework + Selenium ได้ชัดเจน

---

## ⚙️ เตรียมก่อนเริ่ม — รัน demo app และ Playwright

บทนี้มีตัวอย่างที่รันได้จริง ควรเปิด demo app ทิ้งไว้ระหว่างอ่านเพื่อลองได้เลย

**Terminal 1 — รัน demo app:**
```bash
cd path/to/docs/playwright-typescript/playwright-course-app
npm install   # ครั้งแรกเท่านั้น
npm start
# → เว็บพร้อมที่ http://localhost:3000
```

**Terminal 2 — สร้าง Playwright project (ถ้ายังไม่มี):**
```bash
mkdir playwright-practice
cd playwright-practice
npm init playwright@latest
# → เลือก TypeScript, กด Enter ทุกข้อ
```

**ลองรัน Codegen ดู element บนหน้าเว็บ:**
```bash
npx playwright codegen http://localhost:3000
```
คลิก element ต่างๆ แล้วดู locator ที่ generate ให้ — ใช้ระหว่างอ่านบทนี้ได้เลย

---

## 2. ทำไมต้องรู้? (Why)

ลองนึกภาพสถานการณ์นี้:

ทีมคุณ refactor หน้า login ใหม่ — designer เปลี่ยน CSS class ของปุ่ม Submit จาก `.btn-primary` เป็น `.btn-contained` และ backend เปลี่ยน `id` จาก `#submit` เป็น `#login-submit` เพื่อให้ชัดขึ้น

ถ้า test เขียนด้วย CSS selector แบบนี้:

```robot
# Robot Framework + Selenium
Click Element    css:#submit
```

หรือแบบนี้:

```typescript
// Playwright — แต่ใช้ CSS selector
await page.locator('#submit').click();
```

test จะพังทันทีโดยที่ไม่มีอะไรผิดจากมุมมอง user เลย — ปุ่มยังอยู่, text ยังเขียนว่า "Login", flow ยังทำงานได้ปกติ แต่ test บอกว่า "element not found"

นี่คือปัญหาหลักของ structural locator — มัน coupling กับ implementation detail ที่เปลี่ยนบ่อย

Playwright แนะนำ accessibility-first approach: ค้นหา element ด้วย ARIA role, label, และ text ที่ user มองเห็น — สิ่งเหล่านี้เปลี่ยนน้อยกว่ามาก เพราะถ้าคุณเปลี่ยน ARIA role ของปุ่ม Login ก็แปลว่า UI จริงๆ เปลี่ยนจนส่งผลต่อ user — ซึ่งควรให้ test พังด้วย

---

## 3. เนื้อหาหลัก

### 3.0 เมื่อเห็น Element บนหน้า — ใช้ getBy อะไร?

ทุกครั้งที่เห็น element บนหน้าเว็บ ให้ถามตัวเองว่า:

> **"element นี้คืออะไร และผู้ใช้เข้าถึงมันด้วยอะไร?"**

ดู HTML ของ element แล้วเทียบกับตารางนี้:

| เห็นอะไรบนหน้า | HTML เบื้องหลัง | locator ที่ควรใช้ |
|---|---|---|
| ปุ่ม | `<button>Login</button>` | `getByRole('button', { name: 'Login' })` |
| Link | `<a href="/about">About</a>` | `getByRole('link', { name: 'About' })` |
| หัวข้อ | `<h1>Dashboard</h1>` | `getByRole('heading', { name: 'Dashboard' })` |
| Input ที่มีป้ายกำกับ | `<label>Email <input></label>` | `getByLabel('Email')` |
| Input ที่ไม่มีป้ายกำกับ | `<input placeholder="Search...">` | `getByPlaceholder('Search...')` |
| Dropdown | `<select>...</select>` + label | `getByRole('combobox', { name: 'Category' })` |
| Checkbox | `<input type="checkbox">` + label | `getByRole('checkbox', { name: 'Remember me' })` |
| Radio button | `<input type="radio">` + label | `getByRole('radio', { name: 'Male' })` |
| รูปภาพ | `<img alt="Logo">` | `getByAltText('Logo')` |
| ข้อความ error/notification | `<p>Invalid credentials</p>` | `getByText('Invalid credentials')` |
| Container ที่ไม่มี role ชัดเจน | `<div data-testid="product-grid">` | `getByTestId('product-grid')` |

---

#### 1 Parameter vs 2 Parameters — เมื่อไหนต้องใส่ options เพิ่ม?

```typescript
// 1 parameter — ใช้เมื่อ element แบบนั้นมีแค่อันเดียวในทั้งหน้า
// หรือเมื่อ chain แล้ว scope แคบพอแล้ว
page.getByRole('navigation')                    // nav มีอันเดียวในหน้า — พอแล้ว
page.getByRole('navigation').getByRole('link')  // link ใน nav — scope แคบแล้ว ไม่ต้องระบุ name

// 2 parameters — ใช้เมื่อ element แบบนั้นมีหลายอันในหน้า ต้อง narrow ลง
page.getByRole('button', { name: 'Login' })     // หน้ามีหลายปุ่ม → ระบุ name
page.getByRole('link', { name: 'About Us' })    // หน้ามีหลาย link → ระบุ name
page.getByRole('heading', { level: 1 })         // ต้องการ h1 เท่านั้น ไม่ใช่ h2-h6
```

**กฎ**: ถ้า locator match มากกว่า 1 element → เพิ่ม option จนเหลืออันเดียว

---

**ถ้ายังไม่แน่ใจ → ใช้ Codegen ช่วย:**
```bash
npx playwright codegen http://localhost:3000
```
คลิก element ใดก็ได้บนหน้า — Codegen จะบอกว่าควรใช้ locator อะไรทันที

---

### 3.1 Priority Order — ลำดับที่ควรใช้

Playwright แนะนำ locator ตามลำดับนี้ (ตรวจสอบจาก playwright.dev/docs/locators แล้ว):

---

#### 1. `page.getByRole(role, { name })` — อันดับหนึ่งที่แนะนำที่สุด

อ้างอิง ARIA role ของ element — ซึ่งมาจาก HTML tag หรือ `role` attribute

```typescript
// partial example — see Section 5 for runnable version
// ปุ่ม
page.getByRole('button', { name: 'Login' })

// Heading
page.getByRole('heading', { name: 'Welcome back' })

// Link
page.getByRole('link', { name: 'Forgot password?' })

// Textbox (input ทั่วไป)
page.getByRole('textbox', { name: 'Username' })

// Checkbox
page.getByRole('checkbox', { name: 'Remember me' })
```

ทำไมถึงดีที่สุด? เพราะ ARIA role สะท้อน semantics ของ UI ที่ user รับรู้ และยังช่วยให้ผู้พิการที่ใช้ screen reader เข้าถึง app ได้ด้วย — ถ้า role เปลี่ยน แปลว่า UI เปลี่ยนจริงๆ

**Options ที่ใช้บ่อยใน getByRole:**

```typescript
// name — accessible name ของ element (คือข้อความที่ user/screen reader เห็น)
page.getByRole('button', { name: 'Submit' })
page.getByRole('button', { name: /submit/i })  // regex — case-insensitive

// exact — default false (substring match)
// false: name: 'Log' จะ match "Login", "Logout" ด้วย
// true: match "Login" เท่านั้น ต้องตรงทั้งหมด
page.getByRole('button', { name: 'Log' })                   // match "Login", "Logout"
page.getByRole('button', { name: 'Login', exact: true })    // match "Login" เท่านั้น

// level — สำหรับ heading เท่านั้น (h1 = level 1, h2 = level 2, ...)
page.getByRole('heading', { level: 1 })                     // เฉพาะ h1
page.getByRole('heading', { name: 'Products', level: 2 })   // h2 ที่ชื่อ Products

// checked — สำหรับ checkbox หรือ radio
page.getByRole('checkbox', { name: 'Remember me', checked: true })   // ที่ tick แล้ว
page.getByRole('checkbox', { name: 'Remember me', checked: false })  // ยังไม่ได้ tick

// disabled — สำหรับ disabled elements
page.getByRole('button', { name: 'Submit', disabled: false })  // เฉพาะปุ่มที่ active อยู่
page.getByRole('button', { name: 'Submit', disabled: true })   // เฉพาะปุ่มที่ disabled อยู่
```

*(source: https://playwright.dev/docs/api/class-page#page-get-by-role)*

**Accessible Name คืออะไร — Playwright หาจากไหน?**

"Accessible name" คือข้อความที่ระบุตัว element สำหรับ screen reader Playwright ค้นหาตามลำดับนี้:

```
1. aria-label attribute
   <button aria-label="Close dialog">✕</button>
   → name = "Close dialog"

2. aria-labelledby (ชี้ไปที่ element อื่น)
   <span id="del-lbl">Delete account</span>
   <button aria-labelledby="del-lbl">🗑️</button>
   → name = "Delete account"

3. Text content ของ element (พบบ่อยที่สุด)
   <button>Login</button>
   → name = "Login"

4. value / alt / title attribute
   <input type="submit" value="Send message">
   → name = "Send message"
```

ในทางปฏิบัติ: **ข้อความที่เห็นบนปุ่ม/link/heading = accessible name** ได้เลยในกรณีส่วนใหญ่

*(source: https://www.w3.org/TR/accname-1.2/)*

---

#### 2. `page.getByText('text')` — visible text content

ค้นหา element จาก text ที่ user มองเห็น

```typescript
// partial example — see Section 5 for runnable version
// substring match (default) — ยืดหยุ่นกว่า
page.getByText('Welcome')          // match "Welcome back", "Welcome!"

// exact match — precise กว่า
page.getByText('Welcome back', { exact: true })

// regex — สำหรับ pattern
page.getByText(/welcome/i)
```

ใช้ดีกับ message, label, heading ที่ไม่ใช่ interactive control — ถ้าใช้กับปุ่มหรือ input ให้ใช้ `getByRole` หรือ `getByLabel` แทน

---

#### 3. `page.getByLabel('text')` — form inputs

ค้นหา form control ด้วย label ที่ associated อยู่ด้วย — ทั้ง `<label for="...">` และ `aria-label`

```typescript
// partial example — see Section 5 for runnable version
page.getByLabel('Username')
page.getByLabel('Email address')
page.getByLabel('Password')
```

เหมาะมากสำหรับ form เพราะ label คือสิ่งที่ user อ่านเห็นและเข้าใจว่า input นั้นคืออะไร

**HTML patterns ที่ getByLabel รองรับทั้งหมด:**

```typescript
// Pattern 1: <label> ที่ครอบ input ไว้ข้างใน
// HTML: <label>Username <input type="text"></label>
page.getByLabel('Username')

// Pattern 2: <label for="id"> แยกจาก input
// HTML: <label for="pwd">Password</label>
//        <input type="text" id="pwd">
page.getByLabel('Password')

// Pattern 3: aria-label บน input โดยตรง (ไม่มี <label> tag)
// HTML: <input type="search" aria-label="Search products">
page.getByLabel('Search products')

// Pattern 4: aria-labelledby ชี้ไปที่ element อื่น
// HTML: <span id="card-lbl">Card number</span>
//        <input type="text" aria-labelledby="card-lbl">
page.getByLabel('Card number')
```

⚠️ ข้อสำคัญ: `getByLabel` หา **form control** (input/textarea/select) ที่ associate กับ label นั้น — ไม่ได้หา element `<label>` เอง

*(source: https://playwright.dev/docs/locators#locate-by-label)*

---

#### 4. `page.getByPlaceholder('text')` — placeholder ใน input

ใช้เมื่อ input ไม่มี label แต่มี placeholder

```typescript
// partial example — see Section 5 for runnable version
page.getByPlaceholder('Search products...')
page.getByPlaceholder('Enter your email')
```

ระวัง: placeholder บางครั้งหายไปเมื่อ user พิมพ์แล้ว ถ้า UI design เปลี่ยน placeholder อาจเปลี่ยนตาม — แต่ดีกว่า CSS selector อยู่ดี

---

#### 5. `page.getByAltText('text')` — image alt text

สำหรับ `<img>` และ element อื่นที่มี `alt` attribute

```typescript
// partial example — see Section 5 for runnable version
page.getByAltText('Company logo')
page.getByAltText('Product: MacBook Pro')
```

---

#### 6. `page.getByTitle('text')` — title attribute

ค้นหาจาก `title` attribute ที่มักเป็น tooltip

```typescript
// partial example — see Section 5 for runnable version
page.getByTitle('Close dialog')
page.getByTitle('Sort ascending')
```

---

#### 7. `page.getByTestId('data-testid')` — test ID fallback

ใช้เมื่อ element ไม่มี semantic ที่ชัดเจนพอ หรือ UI ซับซ้อนมากจน locator อื่นไม่เสถียร

```typescript
page.getByTestId('product-grid')
page.getByTestId('todo-list')
page.getByTestId('btn-add-todo')
```

ทำไมถึงเป็น fallback ไม่ใช่ first choice? เพราะ `data-testid` ไม่มีความหมายต่อ user จริงๆ — มันเป็น attribute ที่ developer เพิ่มเข้าไปเพื่อ test โดยเฉพาะ ซึ่งหมายความว่า:

1. ต้องประสานงานกับ dev ทุกครั้งที่อยากเพิ่ม testid
2. ถ้า dev ลืมใส่ → test หา element ไม่เจอ
3. ไม่ได้ validate ว่า UI accessible สำหรับ user จริงๆ

แต่ในหลาย project `data-testid` เป็นแนวปฏิบัติมาตรฐาน — ใช้ได้เมื่อจำเป็น แต่ลอง semantic locator ก่อนเสมอ

---

#### 8. `page.locator('css')` — CSS selector (last resort)

ใช้เฉพาะเมื่อทุกอย่างข้างต้นทำไม่ได้

```typescript
page.locator('button.submit-btn')        // CSS class
page.locator('[data-custom="value"]')    // custom attribute
page.locator('xpath=//button[@type="submit"]')  // XPath
```

ปัญหาหลัก: CSS class และ XPath ผูกกับ implementation — เปราะแตกเมื่อ developer refactor

---

### 3.2 Chaining & Filtering

เมื่อ locator simple ไม่เพียงพอ ใช้ method เหล่านี้:

#### filter() — กรอง locator ตาม condition

```typescript
// partial example — see Section 5 for runnable version
// กรองด้วย text
page.getByRole('listitem').filter({ hasText: 'Active' })
page.getByRole('listitem').filter({ hasNotText: 'Deleted' })

// กรองด้วย child locator
page.getByRole('listitem').filter({
  has: page.getByRole('checkbox', { name: 'Done' })
})

// กรองด้วย visible (verified จาก official docs)
page.locator('button').filter({ visible: true })
```

#### nth(), first(), last() — เลือก element ใน list

ใช้เมื่อ locator match หลาย element และคุณต้องการตัวที่ n:

```typescript
page.getByRole('listitem').first()    // ตัวแรก
page.getByRole('listitem').last()     // ตัวสุดท้าย
page.getByRole('listitem').nth(2)     // ตัวที่ 3 (index เริ่มจาก 0)
```

ระวัง: `.nth()` ควรใช้เป็น last resort ด้วยเช่นกัน เพราะถ้า list เปลี่ยนลำดับ test จะพัง ทางที่ดีกว่าคือทำ locator ให้ specific กว่า เช่น `filter({ hasText: 'specific text' })`

#### and() — locator ที่ตรงทั้งสอง condition

```typescript
// partial example — see Section 5 for runnable version
// element ที่เป็นทั้ง button และมี role=submit
const submitBtn = page.getByRole('button').and(
  page.locator('[type="submit"]')
);
```

#### or() — locator ที่ตรงอย่างน้อยหนึ่ง condition

```typescript
// partial example — see Section 5 for runnable version
// element ที่เป็น button หรือ link ก็ได้
const action = page.getByRole('button', { name: 'Continue' }).or(
  page.getByRole('link', { name: 'Continue' })
);
```

---

### 3.3 Strict Mode — เมื่อ locator match หลาย element

Playwright บังคับ strict mode: ถ้า locator match มากกว่า 1 element และคุณสั่ง action หรือ assertion — จะ throw `StrictModeViolationError`

```
Error: strict mode violation: getByRole('button') resolved to 5 elements
```

**วิธีแก้ที่ดีที่สุด:** ทำ locator ให้ specific กว่า

```typescript
// partial example — see Section 5 for runnable version
// ❌ match ทุกปุ่มใน page
page.getByRole('button')

// ✅ specific ด้วย name
page.getByRole('button', { name: 'Add to cart' })
```

**วิธีแก้รอง:** ใช้ `.nth()` เมื่อจำเป็น

```typescript
// เลือกปุ่มแรกในกรณีที่ locator เฉพาะเจาะจงกว่าไม่ได้จริงๆ
page.getByRole('button', { name: 'Delete' }).nth(0)
```

**วิธีแก้สำหรับ loop ทุก element:** ใช้ `.all()`

```typescript
// ไม่ error แม้ match หลายตัว — คืน Locator[]
const items = await page.getByRole('listitem').all();
for (const item of items) {
  console.log(await item.textContent());
}
```

---

### 3.4 เปรียบเทียบกับ Robot Framework + Selenium

| หัวข้อ | Robot Framework + Selenium | Playwright |
|--------|---------------------------|------------|
| ค้นหา element | `By.ID`, `By.XPATH`, `By.CSS` — structural | `getByRole`, `getByLabel`, `getByText` — semantic |
| Flakiness จาก DOM change | สูง (ID/CSS เปลี่ยนเมื่อ UI refactor) | ต่ำ (ARIA role/label ไม่เปลี่ยนตาม implementation) |
| Strict mode | ไม่มี (match ตัวแรกเสมอ — silent bug) | บังคับ — match หลายตัว = error ทันที |
| Auto-retry locator | ไม่มี — ต้องเขียน `Wait Until Element` | มี — retry จนครบ timeout โดยอัตโนมัติ |
| Debug locator | ต้อง inspect DOM เอง | Playwright Inspector highlight element ให้ทันที |
| Accessibility validation | ไม่ได้ตรวจ | ใช้ semantic locator = ตรวจ accessibility ไปด้วยในตัว |

### 3.5 ARIA Roles Quick Reference

เมื่อไม่แน่ใจว่า HTML tag ไหนมี role อะไร ดูตารางนี้แล้วใส่ใน `getByRole(...)`:

| HTML Element | ARIA Role | หมายเหตุ |
|---|---|---|
| `<button>` | `button` | รวม `<input type="submit">` และ `<input type="button">` |
| `<a href="...">` | `link` | ต้องมี `href` — ถ้าไม่มี `href` จะเป็น generic element |
| `<h1>` ถึง `<h6>` | `heading` | ใช้ `{ level: 1-6 }` เพื่อเจาะ level ที่ต้องการ |
| `<input type="text">` | `textbox` | รวม `type="email"`, `type="search"`, `type="url"`, `type="tel"` |
| `<input type="password">` | `textbox` | |
| `<input type="checkbox">` | `checkbox` | |
| `<input type="radio">` | `radio` | |
| `<select>` | `combobox` | |
| `<textarea>` | `textbox` | |
| `<img alt="...">` | `img` | ถ้า `alt=""` = decorative ไม่มี role |
| `<nav>` | `navigation` | |
| `<main>` | `main` | |
| `<ul>`, `<ol>` | `list` | |
| `<li>` | `listitem` | |
| `<table>` | `table` | |
| `<tr>` | `row` | |
| `<td>` | `cell` | |
| `<th>` | `columnheader` | หรือ `rowheader` |
| `<dialog>` | `dialog` | |
| `<article>` | `article` | |
| `<header>` (top-level) | `banner` | เฉพาะเมื่ออยู่นอก article/section |
| `<footer>` (top-level) | `contentinfo` | เฉพาะเมื่ออยู่นอก article/section |

**เคล็ดลับ**: เปิด Chrome DevTools → แท็บ "Accessibility" → คลิก element ใดก็ได้ จะเห็น "Role" แสดงอยู่เลย — ใช้ค่านั้นใน `getByRole()` ได้ทันที

*(source: https://www.w3.org/TR/html-aria/)*

---

## 4. ตัวอย่าง

### Beginner: Priority Order ใน action จริง

```typescript
// tested: Playwright v1.50+, Node.js 20+
// requires demo app at localhost:3000
import { test, expect } from '@playwright/test';

test('shows locator priority order in practice', async ({ page }) => {
  await page.goto('http://localhost:3000/login');

  // ลำดับที่ 1: getByRole — ดีที่สุด
  // ปุ่ม Login มี implicit role="button" จาก <button> tag
  const loginBtn = page.getByRole('button', { name: 'Login' });

  // ลำดับที่ 3: getByLabel — สำหรับ form inputs
  // ค้นหาจาก <label>Username</label> ที่ associate กับ input
  const usernameInput = page.getByLabel('Username');
  const passwordInput = page.getByLabel('Password');

  // ตรวจสอบว่า elements พร้อมใช้งาน
  await expect(loginBtn).toBeVisible();
  await expect(usernameInput).toBeEditable();
  await expect(passwordInput).toBeEditable();

  // ทำ action
  await usernameInput.fill('admin');
  await passwordInput.fill('admin123');
  await loginBtn.click();

  // ลำดับที่ 2: getByText — ตรวจ message หลัง login
  await expect(page.getByText('Welcome back')).toBeVisible();
});
```

ข้อสังเกต: ไม่มี CSS selector หรือ data-testid เลย — ทั้งหมดเป็น semantic locator ที่อ่านแล้วเข้าใจทันทีว่าแต่ละบรรทัดทำอะไร

---

### Beginner+: Walkthrough — อ่าน HTML แล้วเลือก Locator

สถานการณ์: คุณได้รับ HTML ของ login form — ให้เลือก locator ที่เหมาะสมสำหรับแต่ละ element

```html
<!-- HTML ของ Login Page บน demo app (simplified) -->
<form>
  <label for="username">Username</label>
  <input id="username" type="text" data-testid="input-username" placeholder="Enter username">

  <label for="password">Password</label>
  <input id="password" type="password" data-testid="input-password">

  <button type="submit" data-testid="btn-login">Login</button>
  <p data-testid="login-error" class="error-message">Invalid credentials</p>
</form>
```

**การตัดสินใจ — เลือก locator อะไรให้แต่ละ element:**

| element | ตัวเลือกที่มี | ที่เลือก | เหตุผล |
|---|---|---|---|
| username input | `getByLabel`, `getByPlaceholder`, `getByTestId` | `getByLabel('Username')` | มี `<label>` → semantic ที่สุด |
| password input | `getByLabel`, `getByTestId` | `getByLabel('Password')` | มี `<label>` → semantic ที่สุด |
| login button | `getByRole`, `getByText`, `getByTestId` | `getByRole('button', { name: 'Login' })` | `<button>` tag + text "Login" → role ชัดเจน |
| error message | `getByText`, `getByTestId` | `getByTestId('login-error')` | `<p>` ธรรมดา ไม่มี role → testid ดีที่สุด |

```typescript
// tested: Playwright v1.50+, Node.js 20+
// requires demo app at localhost:3000/login
import { test, expect } from '@playwright/test';

test('login walkthrough — locator decision in practice', async ({ page }) => {
  await page.goto('http://localhost:3000/login');

  // username: มี <label for="username">Username</label>
  // → getByLabel ดีกว่า getByTestId แม้จะมี data-testid ด้วย
  const usernameInput = page.getByLabel('Username');

  // password: มี <label for="password">Password</label>
  const passwordInput = page.getByLabel('Password');

  // button: เป็น <button> → role='button', text "Login" = accessible name
  const loginBtn = page.getByRole('button', { name: 'Login' });

  // error message: เป็น <p> ธรรมดา ไม่มี semantic role → ใช้ testid
  const errorMsg = page.getByTestId('login-error');

  // ทดสอบ: login ด้วย credentials ผิด → ต้องเห็น error message
  await usernameInput.fill('wronguser');
  await passwordInput.fill('wrongpass');
  await loginBtn.click();
  await expect(errorMsg).toBeVisible();

  // ทดสอบ: login ด้วย credentials ถูก → ต้องออกจากหน้า /login
  await usernameInput.fill('admin');
  await passwordInput.fill('admin123');
  await loginBtn.click();
  await expect(page).not.toHaveURL('/login');
});
```

หลักจำ: priority = `getByLabel` > `getByRole` > `getByTestId` > `getByText` — เลือก semantic ที่สุดที่ทำได้เสมอ

---

### Intermediate: Filter list ที่มี state ผสมกัน

สถานการณ์: ใน shop page มี product หลายรายการในหมวดต่างกัน คุณต้องการตรวจว่า product ที่อยู่ใน category "Electronics" ทุกตัวแสดง badge "In Stock" อย่างน้อยหนึ่งตัว

```typescript
// tested: Playwright v1.50+, Node.js 20+
// requires demo app at localhost:3000/shop
import { test, expect } from '@playwright/test';

test('at least one Electronics product is in stock', async ({ page }) => {
  await page.goto('http://localhost:3000/shop');

  // เลือก category "Electronics" จาก filter dropdown
  // getByRole('combobox') สำหรับ <select> element
  await page.getByRole('combobox', { name: 'Category' }).selectOption('Electronics');

  // ดึง product cards ทั้งหมดที่มี badge "In Stock"
  // chain: getByRole('article') + filter({ hasText: 'In Stock' })
  const inStockProducts = page.getByRole('article').filter({
    has: page.getByText('In Stock')
  });

  // ตรวจว่ามีอย่างน้อย 1 รายการ
  await expect(inStockProducts).not.toHaveCount(0);

  // ดู product แรกที่ in stock — ตรวจ alt text ของรูปภาพ
  const firstProduct = inStockProducts.first();
  const productImage = firstProduct.getByRole('img');
  await expect(productImage).toHaveAttribute('alt', /^Product:/);
});
```

ทำไม intermediate ตัวอย่างนี้น่าสนใจ? เพราะมีการ chain locator (article → img) และ filter ด้วย child locator (`has:`) ซึ่งเป็น pattern ที่ใช้บ่อยมากใน e-commerce testing — แต่ไม่ได้ copy มาจากตัวอย่าง Beginner เลย

---

### Advanced: Diagnosis — StrictModeViolationError

ทีมได้รับ test ที่พังต่อเนื่อง ให้วิเคราะห์หาสาเหตุและแก้ไข:

```typescript
// tested: Playwright v1.50+, Node.js 20+
// requires demo app at localhost:3000
import { test, expect } from '@playwright/test';

// ❌ TEST พังด้วย: StrictModeViolationError
// Error: strict mode violation: getByRole('button') resolved to 5 elements
test.skip('broken — strict mode violation', async ({ page }) => {
  await page.goto('http://localhost:3000');

  // page มีปุ่ม: "Login", "Sign up", "Add to cart" x3
  await page.getByRole('button').click();  // ❌ ambiguous — ไม่รู้จะ click ตัวไหน
});

// ✅ วิเคราะห์: ปัญหาคือ locator ไม่ specific
// solution 1 — ดีที่สุด: ระบุ name
test('navigate to login', async ({ page }) => {
  await page.goto('http://localhost:3000');

  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page).toHaveURL(/\/login/);
});

// สถานการณ์ที่ซับซ้อนกว่า:
// Todo list มีปุ่ม "Delete" ทุก item — ต้องลบ item ที่ชื่อ "Buy groceries"
test('delete specific todo item', async ({ page }) => {
  await page.goto('http://localhost:3000');

  // ❌ วิธีผิด — nth(0) ลบ item แรกเสมอ ถ้า list เรียงใหม่ test พัง
  // await page.getByRole('button', { name: 'Delete' }).nth(0).click();

  // ✅ วิธีถูก — หา listitem ที่มี text "Buy groceries" ก่อน แล้วหาปุ่ม Delete ในนั้น
  const targetItem = page.getByRole('listitem').filter({
    hasText: 'Buy groceries'
  });

  // chain เข้าไปใน scope ของ item นั้น
  await targetItem.getByRole('button', { name: 'Delete' }).click();

  // ตรวจว่า item หายไปแล้ว
  await expect(page.getByText('Buy groceries')).not.toBeVisible();
});

// สถานการณ์ที่ต้องใช้ or() — ปุ่ม submit ที่ชื่อต่างกันระหว่าง staging กับ production
// staging ใช้ "Sign In", production ใช้ "Login" — test ต้องรันได้ทั้งสอง environment
test('submits login form regardless of button label', async ({ page }) => {
  await page.goto('http://localhost:3000/login');

  await page.getByLabel('Username').fill('admin');
  await page.getByLabel('Password').fill('admin123');

  // or() ให้ locator match element ใดก็ได้ที่ตรงกับ arm ใด arm หนึ่ง
  // ถ้า page มีปุ่ม "Sign In" → arm แรก match
  // ถ้า page มีปุ่ม "Login" → arm ที่สอง match
  // สองปุ่มนี้ต่างกันจริง — ไม่มีทางที่ทั้งสองชื่อจะอยู่บน element เดียวกัน
  const submitBtn = page
    .getByRole('button', { name: 'Sign In' })
    .or(page.getByRole('button', { name: 'Login' }));

  await submitBtn.click();
  await expect(page).toHaveURL(/\/(dashboard|home)/);
});
```

**การวิเคราะห์ (Diagnosis):**

| ปัญหา | สาเหตุ | วิธีแก้ |
|-------|--------|---------|
| `getByRole('button')` พัง | Strict mode — match 5 elements | เพิ่ม `{ name: '...' }` |
| `nth(0)` brittle | ขึ้นกับ DOM order ที่เปลี่ยนได้ | ใช้ `filter({ hasText: '...' })` แทน |
| ปุ่มชื่อต่างกันระหว่าง environment | staging vs production ใช้ label ต่างกัน | ใช้ `or()` เพื่อรองรับทั้งสองชื่อ |

---

## 5. Common Mistakes ❌→✅

**1. ใช้ CSS ID selector แทน semantic locator**

❌
```typescript
await page.locator('#submit-btn').click();
```

✅
```typescript
await page.getByRole('button', { name: 'Submit' }).click();
```

เหตุผล: CSS ID เปลี่ยนได้ทุกครั้ง dev refactor แต่ ARIA role และ accessible name เปลี่ยนเฉพาะเมื่อ UI จริงๆ เปลี่ยน — ทำให้ test เสถียรกว่ามาก
*(source: https://playwright.dev/docs/best-practices)*

---

**2. ใช้ legacy `text=` syntax**

❌
```typescript
await page.locator('text=Login').click();
```

✅
```typescript
await page.getByText('Login').click();
// หรือดีกว่าคือ getByRole ถ้า element นั้น interactive
await page.getByRole('button', { name: 'Login' }).click();
```

เหตุผล: `text=` เป็น deprecated syntax ใน Playwright — ยังใช้งานได้แต่ไม่รับประกันในอนาคต อีกทั้ง `getByText()` อ่านง่ายกว่าและมี `{ exact: true }` option ชัดเจน
*(source: https://playwright.dev/docs/other-locators)*

---

**3. ไม่จัดการ StrictModeViolationError อย่างถูกต้อง**

❌
```typescript
// แก้ด้วยการ "ยัด" nth(0) โดยไม่คิด
await page.getByRole('button', { name: 'Delete' }).nth(0).click();
```

✅
```typescript
// ทำ locator ให้ specific ก่อน — ระบุ context ของ element
const firstRow = page.getByRole('row').filter({ hasText: 'Item to delete' });
await firstRow.getByRole('button', { name: 'Delete' }).click();
```

เหตุผล: `.nth(0)` ผูกกับ DOM order — ถ้า list reorder test พังโดยไม่มีเหตุผลด้าน logic วิธีที่ถูกคือทำ locator ให้ specific กว่าด้วย context
*(source: https://playwright.dev/docs/locators)*

---

**4. ใช้ `getByTestId` เป็น default ทุกอย่าง**

❌
```typescript
// ทุก element ใช้ testid แม้มี semantic ที่ชัดเจนอยู่แล้ว
await page.getByTestId('btn-login').click();
await page.getByTestId('input-username').fill('admin');
```

✅
```typescript
// semantic locator ก่อน — fallback ถึง testid เมื่อจำเป็นจริงๆ
await page.getByRole('button', { name: 'Login' }).click();
await page.getByLabel('Username').fill('admin');

// testid ใช้สำหรับ container/section ที่ไม่มี semantic ชัดเจน
const productGrid = page.getByTestId('product-grid');
```

เหตุผล: `getByTestId` ไม่ช่วย validate accessibility — ถ้าทีมใช้ semantic locator จะตรวจเจอ accessibility issue ได้ตั้งแต่ตอน write test แทนที่จะรอให้ user รายงาน
*(source: https://playwright.dev/docs/best-practices)*

---

**5. สับสนระหว่าง Locator กับ ElementHandle**

❌
```typescript
// ElementHandle เป็น API เก่า — ไม่มี auto-retry
const btn = await page.$('button.submit');
await btn.click();  // ถ้า button ยังไม่ ready จะ error ทันที
```

✅
```typescript
// Locator มี auto-retry และ actionability checks อัตโนมัติ
const btn = page.getByRole('button', { name: 'Submit' });
await btn.click();  // retry จนครบ timeout ถ้า button ยังไม่ ready
```

เหตุผล: `page.$()` และ `ElementHandle` เป็น legacy API ที่ Playwright แนะนำให้เลิกใช้ — ไม่มี auto-waiting ทำให้ test flaky ใน dynamic UI
*(source: https://playwright.dev/docs/locators)*

---

## 6. สรุปบท + Retrieval Questions

ในบทนี้คุณได้เรียน:

- **Priority order** ของ locator ตาม Playwright: getByRole → getByText → getByLabel → getByPlaceholder → getByAltText → getByTitle → getByTestId → locator(css) — เรียงจาก semantic มากไปน้อย
- **Locator เป็น lazy description** — query DOM จริงเมื่อมี action/assertion เท่านั้น ทำให้ auto-retry ได้
- **Strict mode** บังคับให้ locator ชัดเจน — match หลายตัวขณะ action = error ทันที ซึ่งดีกว่า "match ตัวแรกเงียบๆ" แบบ Selenium
- **Chaining & Filtering** (`filter`, `nth`, `and`, `or`) ช่วยจัดการ element ซับซ้อนได้โดยไม่ต้องหนีไปใช้ CSS selector

---

ก่อนอ่านบทถัดไป ลองตอบคำถามเหล่านี้ด้วยตัวเองก่อน (ไม่ต้อง scroll ขึ้นไปดูเฉลย):

**คำถามที่ 1:** ถ้าคุณมี `<input placeholder="Search..." />` ที่ไม่มี label — คุณจะใช้ locator ประเภทไหน? และถ้า designer เพิ่ม label "Search products" ให้ input นั้นในภายหลัง คุณควรเปลี่ยนไปใช้ locator ประเภทไหน?

**คำถามที่ 2:** `page.getByRole('row').nth(2)` กับ `page.getByRole('row').filter({ hasText: 'John Doe' })` — แบบไหนเสถียรกว่าในระยะยาว และทำไม?

**คำถามที่ 3:** Locator นี้จะ throw error ตอนไหน และทำไม?
```typescript
const btn = page.getByRole('button');
// ... (หน้านี้มีปุ่ม 4 ตัว)
await expect(btn).toHaveCount(4);  // บรรทัดนี้จะ error ไหม?
await btn.click();                  // บรรทัดนี้จะ error ไหม?
```

<details>
<summary>ดูเฉลย</summary>

**คำถามที่ 1:** ใช้ `page.getByPlaceholder('Search...')` เพราะนั่นคือสิ่งเดียวที่ระบุตัวตนของ input ได้ตอนนี้ — เมื่อ designer เพิ่ม label ควรเปลี่ยนเป็น `page.getByLabel('Search products')` ทันที เพราะ label อยู่ในลำดับ priority สูงกว่า (ลำดับ 3 vs ลำดับ 4) และมั่นคงกว่าเพราะ label คือสิ่งที่ user อ่านเห็น

**คำถามที่ 2:** `filter({ hasText: 'John Doe' })` เสถียรกว่ามาก เพราะ `nth(2)` ขึ้นกับ DOM order — ถ้า table sort ใหม่, เพิ่ม row, หรือ server ส่งข้อมูลลำดับต่างกัน test พัง แต่ `filter({ hasText: 'John Doe' })` ยึดกับ content จริง — row ของ John Doe อยู่ที่ไหนใน table ก็ยังหาเจอ

**คำถามที่ 3:** `toHaveCount(4)` จะ **ไม่ error** — เพราะ `toHaveCount()` ออกแบบมาเพื่อตรวจจำนวน element หลายตัว strict mode ไม่บังคับที่นี่ — แต่ `btn.click()` บรรทัดถัดไปจะ **throw StrictModeViolationError** ทันที เพราะ `click()` เป็น action ที่ต้องการ element เดียวที่ชัดเจน แต่ locator match 4 element

</details>
