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

## 3. Analogy: ที่อยู่บนซอง — ชื่อคน vs เลขที่บ้าน

ลองนึกภาพคุณต้องส่งจดหมายหาเพื่อน

**แบบที่ 1 — ที่อยู่ตามเลขที่บ้าน (Structural):**
"บ้านเลขที่ 42/7 ซอยรามคำแหง 21 ห้อง 302"

ถ้าเพื่อนย้ายบ้าน หรืออาคารเปลี่ยนเลขห้อง — จดหมายหาย เพื่อนไม่ได้รับ ทั้งที่เพื่อนยังอยู่แถวเดิม

**แบบที่ 2 — ที่อยู่ตามชื่อคน (Semantic):**
"นายสมชาย ใจดี, บริษัท ABC จำกัด"

ไม่ว่าสมชายจะย้ายโต๊ะทำงาน เปลี่ยนแผนก หรือบริษัทขยับสำนักงาน — จดหมายยังตามไปถึงได้ เพราะที่อยู่อ้างอิง "ตัวตน" ของคน ไม่ใช่ "ตำแหน่ง" ในโครงสร้าง

Locator ใน Playwright ทำงานแบบเดียวกัน — `getByRole('button', { name: 'Login' })` อ้างอิง semantic ของ element ว่า "ปุ่มที่ชื่อว่า Login" ไม่ว่า CSS class หรือ DOM position จะเปลี่ยนอย่างไร

---

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**

- **Locator ถูก query DOM ทันทีที่เรียก `getByRole()`** — จริงๆ แล้ว Locator คือ "description" หรือ "recipe" เท่านั้น ยังไม่ได้แตะ DOM เลยจนกว่าจะมี action (`click()`, `fill()`) หรือ assertion (`expect().toBeVisible()`) — นั่นคือเหตุผลที่ Playwright ทำ auto-retry ได้: มัน re-evaluate locator ซ้ำจนครบ timeout

- **Locator ที่ดีต้องชี้ไปหา element เดียวเสมอ** — `strict mode` บอกว่า locator ที่ match หลาย element จะ throw error เมื่อสั่ง action แต่ `.all()` หรือ `.count()` ใช้ร่วมกับ locator ที่ match หลายตัวได้ถูกต้อง เช่น `page.getByRole('listitem').all()` — strict mode ใช้บังคับแค่เวลา action/assertion กับ element เดียว

---

## 4. เนื้อหาหลัก

### 4.1 Priority Order — ลำดับที่ควรใช้

Playwright แนะนำ locator ตามลำดับนี้ (ตรวจสอบจาก playwright.dev/docs/locators แล้ว):

---

#### 1. `page.getByRole(role, { name })` — อันดับหนึ่งที่แนะนำที่สุด

อ้างอิง ARIA role ของ element — ซึ่งมาจาก HTML tag หรือ `role` attribute

```typescript
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

---

#### 2. `page.getByText('text')` — visible text content

ค้นหา element จาก text ที่ user มองเห็น

```typescript
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
page.getByLabel('Username')
page.getByLabel('Email address')
page.getByLabel('Password')
```

เหมาะมากสำหรับ form เพราะ label คือสิ่งที่ user อ่านเห็นและเข้าใจว่า input นั้นคืออะไร

---

#### 4. `page.getByPlaceholder('text')` — placeholder ใน input

ใช้เมื่อ input ไม่มี label แต่มี placeholder

```typescript
page.getByPlaceholder('Search products...')
page.getByPlaceholder('Enter your email')
```

ระวัง: placeholder บางครั้งหายไปเมื่อ user พิมพ์แล้ว ถ้า UI design เปลี่ยน placeholder อาจเปลี่ยนตาม — แต่ดีกว่า CSS selector อยู่ดี

---

#### 5. `page.getByAltText('text')` — image alt text

สำหรับ `<img>` และ element อื่นที่มี `alt` attribute

```typescript
page.getByAltText('Company logo')
page.getByAltText('Product: MacBook Pro')
```

---

#### 6. `page.getByTitle('text')` — title attribute

ค้นหาจาก `title` attribute ที่มักเป็น tooltip

```typescript
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

### 4.2 Chaining & Filtering

เมื่อ locator simple ไม่เพียงพอ ใช้ method เหล่านี้:

#### filter() — กรอง locator ตาม condition

```typescript
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
// element ที่เป็นทั้ง button และมี role=submit
const submitBtn = page.getByRole('button').and(
  page.locator('[type="submit"]')
);
```

#### or() — locator ที่ตรงอย่างน้อยหนึ่ง condition

```typescript
// element ที่เป็น button หรือ link ก็ได้
const action = page.getByRole('button', { name: 'Continue' }).or(
  page.getByRole('link', { name: 'Continue' })
);
```

---

### 4.3 Strict Mode — เมื่อ locator match หลาย element

Playwright บังคับ strict mode: ถ้า locator match มากกว่า 1 element และคุณสั่ง action หรือ assertion — จะ throw `StrictModeViolationError`

```
Error: strict mode violation: getByRole('button') resolved to 5 elements
```

**วิธีแก้ที่ดีที่สุด:** ทำ locator ให้ specific กว่า

```typescript
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

### 4.4 เปรียบเทียบกับ Robot Framework + Selenium

| หัวข้อ | Robot Framework + Selenium | Playwright |
|--------|---------------------------|------------|
| ค้นหา element | `By.ID`, `By.XPATH`, `By.CSS` — structural | `getByRole`, `getByLabel`, `getByText` — semantic |
| Flakiness จาก DOM change | สูง (ID/CSS เปลี่ยนเมื่อ UI refactor) | ต่ำ (ARIA role/label ไม่เปลี่ยนตาม implementation) |
| Strict mode | ไม่มี (match ตัวแรกเสมอ — silent bug) | บังคับ — match หลายตัว = error ทันที |
| Auto-retry locator | ไม่มี — ต้องเขียน `Wait Until Element` | มี — retry จนครบ timeout โดยอัตโนมัติ |
| Debug locator | ต้อง inspect DOM เอง | Playwright Inspector highlight element ให้ทันที |
| Accessibility validation | ไม่ได้ตรวจ | ใช้ semantic locator = ตรวจ accessibility ไปด้วยในตัว |

---

## 5. ตัวอย่าง

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

## 6. Common Mistakes ❌→✅

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

## 7. สรุปบท + Retrieval Questions

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

---

เฉลย:

**คำถามที่ 1:** ใช้ `page.getByPlaceholder('Search...')` เพราะนั่นคือสิ่งเดียวที่ระบุตัวตนของ input ได้ตอนนี้ — เมื่อ designer เพิ่ม label ควรเปลี่ยนเป็น `page.getByLabel('Search products')` ทันที เพราะ label อยู่ในลำดับ priority สูงกว่า (ลำดับ 3 vs ลำดับ 4) และมั่นคงกว่าเพราะ label คือสิ่งที่ user อ่านเห็น

**คำถามที่ 2:** `filter({ hasText: 'John Doe' })` เสถียรกว่ามาก เพราะ `nth(2)` ขึ้นกับ DOM order — ถ้า table sort ใหม่, เพิ่ม row, หรือ server ส่งข้อมูลลำดับต่างกัน test พัง แต่ `filter({ hasText: 'John Doe' })` ยึดกับ content จริง — row ของ John Doe อยู่ที่ไหนใน table ก็ยังหาเจอ

**คำถามที่ 3:** `toHaveCount(4)` จะ **ไม่ error** — เพราะ `toHaveCount()` ออกแบบมาเพื่อตรวจจำนวน element หลายตัว strict mode ไม่บังคับที่นี่ — แต่ `btn.click()` บรรทัดถัดไปจะ **throw StrictModeViolationError** ทันที เพราะ `click()` เป็น action ที่ต้องการ element เดียวที่ชัดเจน แต่ locator match 4 element
