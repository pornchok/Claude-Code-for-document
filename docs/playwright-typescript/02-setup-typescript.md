# บทที่ 2: ติดตั้ง Playwright และ TypeScript Essentials

## ก่อนอ่านบทนี้ ลองตอบ:

1. Playwright ต่างจาก Selenium ในเรื่อง wait strategy อย่างไร?
2. Protocol ที่ Playwright ใช้สื่อสารกับ browser คืออะไร?

---

เฉลย:

1. Playwright มี auto-waiting — ตรวจ actionability อัตโนมัติก่อนทุก action (visible, enabled, stable) โดยไม่ต้องเขียน `sleep` หรือ explicit wait
2. CDP (Chrome DevTools Protocol) / WebSocket — ต่อตรงกับ browser ไม่ผ่าน WebDriver HTTP layer ทำให้เร็วกว่าและ reliable กว่า

---

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- ติดตั้ง Playwright และเลือก TypeScript ได้ถูกต้องตั้งแต่ขั้นตอนแรก
- เข้าใจโครงสร้างไฟล์ทุกตัวที่ได้จาก `npm init playwright@latest` และรู้ว่าแต่ละไฟล์ทำอะไร
- รู้จัก TypeScript types หลัก 4 ตัว: `Page`, `Locator`, `Browser`, `BrowserContext`
- เข้าใจว่าทำไม `async/await` ถึงจำเป็นใน Playwright ทุก action (และเกิดอะไรถ้าลืม)
- ใช้ `tsc --noEmit` เพื่อตรวจ TypeScript errors ก่อนรัน test ได้
- เขียน first test และรันผ่านได้จริง

---

## 2. ทำไมต้องรู้? (Why TypeScript ไม่ใช่ Optional)

ถ้าคุณมาจาก Robot Framework หรือ Selenium Python คุณอาจสงสัยว่า "TypeScript จำเป็นแค่ไหน? ใช้ JavaScript ธรรมดาไม่ได้เหรอ?"

คำตอบคือ: ได้ แต่คุณจะเสียเครื่องมือที่ดีที่สุดของ Playwright ไป

Playwright ถูกออกแบบมาโดยมี TypeScript เป็น first-class language ตั้งแต่ต้น ทีม Playwright เขียน type definitions ครบทุก API ไว้ใน `@playwright/test` package เอง หมายความว่า IDE ของคุณรู้ว่า `page.goto()` รับ argument อะไร, `expect(locator)` มี method อะไรบ้าง, และเมื่อคุณพิมพ์ผิดจะบอกทันทีก่อนรัน test

เทียบกับ Robot Framework ที่ keyword arguments ไม่มี type safety — คุณรู้ว่า argument ผิดก็ต่อเมื่อ test fail ตอน runtime

TypeScript ช่วยให้:
- **autocomplete** ทำงานได้ 100% ใน VS Code
- **type errors** โชว์ใน editor ก่อนรัน test
- **refactoring** ปลอดภัยกว่า เพราะ IDE ตามแก้ให้ได้ทุกที่

---

## 3. Analogy: แผนที่กับ GPS

TypeScript กับ Playwright เหมือนแผนที่กับ GPS

TypeScript คือแผนที่ — บอกว่าทุกถนน (API) มีทิศทางอะไรบ้าง ไปได้ที่ไหน มีข้อจำกัดอะไร Playwright คือ GPS — ใช้แผนที่นั้นนำทาง (autocomplete, type checking) บอกได้ล่วงหน้าว่าเส้นทางนั้นถูกหรือไม่ก่อนที่คุณจะขับออกไปจริง

ถ้าขับโดยไม่มีแผนที่ (JavaScript) ยังไปถึงปลายทางได้ แต่หลงทางง่ายกว่า และรู้ว่าหลงก็ต่อเมื่อขับไปแล้ว

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
- TypeScript ทำให้ code รันช้าลง — จริงๆ TypeScript compile เป็น JavaScript ก่อนรัน runtime performance เหมือนกันทุกประการ Playwright จัดการ compilation ให้โดยอัตโนมัติ *(source: "You just write tests in TypeScript, and Playwright will read them, transform to JavaScript and run.")*
- TypeScript เป็น layer แยกต่างหาก — จริงๆ `@playwright/test` รวม TypeScript types ไว้ใน package เดียวกัน ไม่ต้องติดตั้งแยก

---

## 4. เนื้อหาหลัก

### 4.1 ติดตั้ง Playwright

ก่อนเริ่ม ตรวจ Node.js version:

```bash
node --version
# ต้องได้ 20.x, 22.x หรือ 24.x
```

จากนั้นสร้าง project ใหม่:

```bash
# สร้าง folder ใหม่และเข้าไป
mkdir my-playwright-tests
cd my-playwright-tests

# ติดตั้ง Playwright พร้อม scaffold
npm init playwright@latest
```

คำสั่งนี้จะถามคำถาม interactive ตามลำดับ:

```
✔ Do you want to use TypeScript or JavaScript? · TypeScript   ← เลือก TypeScript เสมอ
✔ Where to put your end-to-end tests? · tests                ← กด Enter ใช้ default
✔ Add a GitHub Actions workflow? (y/N) · false               ← กด Enter ก่อน (แก้ทีหลัง)
✔ Install Playwright browsers (can be done manually via 'npx playwright install')? (Y/n) · true  ← กด Enter
```

Playwright จะดาวน์โหลด browser binaries (Chromium, Firefox, WebKit) และสร้างไฟล์ scaffold ให้ครบ

### 4.2 โครงสร้างไฟล์ที่ได้

```
my-playwright-tests/
├── playwright.config.ts    ← ตั้งค่าทุกอย่าง: browsers, baseURL, timeouts, reporters
├── package.json            ← dependencies และ scripts
├── package-lock.json       ← lock file (หรือ yarn.lock / pnpm-lock.yaml)
├── tsconfig.json           ← TypeScript config (Playwright สร้างให้อัตโนมัติ)
└── tests/
    └── example.spec.ts     ← ตัวอย่าง test ที่ Playwright สร้างให้
```

อธิบายแต่ละไฟล์:

**`playwright.config.ts`** — ศูนย์กลางควบคุมทั้งหมด ตั้ง browser ที่จะรัน, baseURL, timeout, reporter ที่ใช้ และ project configurations

**`tsconfig.json`** — Playwright สร้างให้อัตโนมัติพร้อมค่าที่เหมาะสม ไม่ต้องแก้ตอนเริ่ม Playwright รองรับเฉพาะ options: `allowJs`, `baseUrl`, `paths`, และ `references`

**`tests/example.spec.ts`** — `.spec.ts` คือ naming convention ของ test files Playwright จะค้นหาไฟล์ที่ลงท้ายด้วย `.spec.ts` หรือ `.test.ts` โดยอัตโนมัติ

### 4.3 TypeScript Types ที่ต้องรู้ 4 ตัว

Playwright มี types หลักที่ใช้บ่อยใน test ดังนี้:

```typescript
import { test, expect, type Page, type Locator, type Browser, type BrowserContext } from '@playwright/test';

// Page — แทน 1 browser tab
// ใช้ควบคุม navigation, กรอก form, click, screenshot ฯลฯ
async function example(page: Page) {
  await page.goto('http://localhost:3000');
  await page.getByLabel('Email').fill('user@example.com');
}

// Locator — pointer ไปยัง element บนหน้า
// สำคัญ: Locator เป็น lazy query — ยังไม่ query DOM จนกว่าจะ await action
const button: Locator = page.getByRole('button', { name: 'Login' });
await button.click(); // query DOM ตอนนี้ และรอจนกว่าจะ actionable

// Browser — browser process ทั้งก้อน (Chromium / Firefox / WebKit)
// ใช้สร้าง BrowserContext ใหม่เมื่อต้องการ session แยก

// BrowserContext — isolated browser session
// cookies, localStorage, sessionStorage แยกกันระหว่าง context
// เหมาะสำหรับ test multi-user scenarios
```

ในการใช้งานปกติ `page` จะถูก inject เข้ามาผ่าน test fixture อัตโนมัติ ไม่ต้องสร้างเอง:

```typescript
test('my test', async ({ page }) => {
  // page พร้อมใช้งานทันที — Playwright จัดการ lifecycle ให้
  await page.goto('http://localhost:3000');
});
```

### 4.4 ทำไม async/await ต้องมีทุก action

นี่คือจุดสำคัญที่สุดในบทนี้ และเป็นแหล่งที่มาของ bug ที่ตามหาได้ยากที่สุด

Playwright actions ทุกตัวเป็น `Promise` เพราะต้องส่ง command ผ่าน WebSocket ไปยัง browser และรอ response กลับมา เหมือนการโทรหาคนที่อยู่คนละห้อง — ต้องรอให้เขาตอบก่อนถึงจะทำขั้นตอนต่อไปได้

```typescript
// ❌ ผิด: ลืม await — test อาจผ่าน แต่ action ไม่ได้รันจริง (silent bug)
test('dangerous example', async ({ page }) => {
  page.goto('http://localhost:3000');   // สร้าง Promise แต่ไม่รอ
  page.click('[data-testid="submit"]'); // race condition — หน้าอาจยังโหลดไม่เสร็จ
  expect(await page.title()).toBe('Dashboard'); // อาจผ่านหรือไม่ผ่านแบบ random
});

// ✅ ถูกต้อง: await ทุก action
test('correct example', async ({ page }) => {
  await page.goto('http://localhost:3000');        // รอจนหน้าโหลดเสร็จ
  await page.click('[data-testid="submit"]');      // รอจนคลิกได้ และคลิกแล้ว
  await expect(page).toHaveTitle('Dashboard');     // รอจนเงื่อนไขผ่าน
});
```

กฎง่ายๆ: **ทุกบรรทัดที่เรียก `page.*()` หรือ `locator.*()` ต้องมี `await` นำหน้าเสมอ**

### 4.5 playwright.config.ts — โครงสร้างพื้นฐาน

```typescript
// playwright.config.ts
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',           // folder ที่เก็บ test files
  fullyParallel: true,          // รัน tests พร้อมกันทุกไฟล์
  forbidOnly: !!process.env.CI, // ป้องกัน .only ขึ้น CI โดยไม่ตั้งใจ
  retries: process.env.CI ? 2 : 0, // retry อัตโนมัติใน CI
  use: {
    baseURL: 'http://localhost:3000', // ใช้ await page.goto('/login') แทน URL เต็ม
    trace: 'on-first-retry',          // record trace เมื่อ retry
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    // เพิ่ม firefox / webkit ได้ทีหลัง
  ],
});
```

`defineConfig()` ไม่ต้องใส่ generic `<T>` สำหรับ config ทั่วไป TypeScript จะ infer types ให้อัตโนมัติ

### 4.6 tsc --noEmit — ตรวจ TypeScript ก่อน Run

Playwright จัดการ compilation ให้อัตโนมัติตอนรัน test แต่ถ้าต้องการตรวจ TypeScript errors แยกต่างหาก (เช่น ใน CI pipeline ก่อนรัน test จริง) ใช้:

```bash
# ตรวจ TypeScript errors โดยไม่สร้างไฟล์ output
npx tsc -p tsconfig.json --noEmit

# ถ้าไม่มี output = ไม่มี TypeScript error
# ถ้ามี error = แก้ก่อนที่ test จะรัน

# สำหรับ development: watch mode
npx tsc -p tsconfig.json --noEmit -w
```

`--noEmit` หมายความว่า "compile เพื่อตรวจ แต่ไม่สร้างไฟล์ `.js`" — เป็น type-checking step ที่ไม่ทำให้เกิด output ไฟล์

### 4.7 no-floating-promises — ESLint Rule ที่ควรเปิด

`await` ที่หายไปเป็น silent bug ที่อันตรายมาก เพราะ test อาจผ่านโดยที่ action ไม่ได้รันจริง ESLint rule `@typescript-eslint/no-floating-promises` จะตรวจจับ pattern นี้:

```typescript
// ESLint จะ flag บรรทัดนี้ว่าเป็น error:
page.click('button');
// Error: Promises must be awaited, end with a call to .catch,
//        end with a call to .then with a rejection handler or be explicitly marked
//        as ignored with the `void` operator

// แก้โดยใส่ await:
await page.click('button');
```

ถ้าโปรเจคยังไม่มี ESLint ไม่ต้องตั้งตอนนี้ แต่ให้จำกฎไว้: **ทุก async call ต้อง await**

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner — First Test: ตรวจ title และ heading

```typescript
// tests/first-test.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

test('dashboard loads correctly', async ({ page }) => {
  await page.goto('http://localhost:3000');
  await expect(page).toHaveTitle('Playwright Course App — Dashboard');
  await expect(page.getByTestId('dashboard-heading')).toBeVisible();
});
```

วิธีรัน:

```bash
npx playwright test tests/first-test.spec.ts --reporter=line
```

Expected output:

```
Running 1 test using 1 worker
  ✓  tests/first-test.spec.ts:4:1 › dashboard loads correctly (1.2s)

  1 passed (2.1s)
```

ถ้า test fail จะเห็น error พร้อม screenshot อัตโนมัติใน `test-results/` folder

### Intermediate — ตรวจ form elements ครบถ้วนในหน้า registration

สถานการณ์: QA team ต้องการ smoke test ว่าหน้า registration มี field ครบก่อนส่ง sprint review

```typescript
// tests/registration-form.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

test('registration form has all required fields', async ({ page }) => {
  await page.goto('/register'); // ใช้ relative path เพราะมี baseURL ใน config

  // ตรวจ title ถูก
  await expect(page).toHaveTitle(/Register/);

  // ตรวจ input fields ครบ
  await expect(page.getByLabel('Full Name')).toBeVisible();
  await expect(page.getByLabel('Email')).toBeVisible();
  await expect(page.getByLabel('Password')).toBeVisible();
  await expect(page.getByLabel('Confirm Password')).toBeVisible();

  // ตรวจ submit button ใช้งานได้
  await expect(page.getByRole('button', { name: 'Create Account' })).toBeEnabled();

  // ตรวจ error messages ซ่อนอยู่ตอนที่ยังไม่ submit
  await expect(page.getByTestId('form-errors')).toBeHidden();
});
```

สังเกตว่า test นี้ใช้ `page.getByLabel()` แทน CSS selector — เพราะ label เป็น user-visible text ที่เสถียรกว่า class name ที่อาจเปลี่ยนตาม design system

### Advanced — TypeScript helper function พร้อม type safety

```typescript
// tests/navigation.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { test, expect, type Page, type Locator } from '@playwright/test';

// typed helper function — TypeScript จะ enforce ว่าต้องส่ง Page มา
async function getNavLinks(page: Page): Promise<Locator> {
  return page.getByRole('navigation').getByRole('link');
}

// typed helper สำหรับ login workflow ที่ใช้หลาย test
async function loginAs(page: Page, username: string, password: string): Promise<void> {
  await page.goto('/login');
  await page.getByLabel('Username').fill(username);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page).toHaveURL('/dashboard');
}

test('authenticated user sees all nav links', async ({ page }) => {
  await loginAs(page, 'testuser', 'password123');

  const links = await getNavLinks(page);
  await expect(links).toHaveCount(7);

  // ตรวจว่า link texts ถูกต้อง
  await expect(links).toContainText([
    'Dashboard', 'Todos', 'Shop', 'Cart', 'Components', 'Advanced', 'Visual'
  ]);
});

test('unauthenticated user sees limited nav links', async ({ page }) => {
  await page.goto('/');

  const links = await getNavLinks(page);
  // ไม่ได้ login ควรเห็นแค่ Login link
  await expect(links).toHaveCount(1);
  await expect(links.first()).toHaveText('Login');
});
```

ประโยชน์ของ `type Page` และ `type Locator` ใน import: TypeScript จะ tree-shake types ออกตอน compile ทำให้ชัดเจนว่า import มาเป็น type ไม่ใช่ value

---

## 6. Common Mistakes

❌ **เลือก JavaScript แทน TypeScript ตอน `npm init playwright@latest`**
→ ไม่มี type checking, autocomplete ไม่สมบูรณ์, ต้องแปลง project ทีหลัง
✅ เลือก TypeScript เสมอเมื่อถูกถาม — เป็น default อยู่แล้ว กด Enter ก็ได้
*(source: https://playwright.dev/docs/intro — "TypeScript or JavaScript (default: TypeScript)")*

---

❌ **ลืม `await` บน action**
```typescript
// อันตราย: test อาจผ่านโดยที่ click ไม่ได้เกิดขึ้นจริง
page.click('[data-testid="submit"]');
```
→ สร้าง Promise แต่ไม่รอ — action ถูก schedule แต่ test ดำเนินต่อทันที ทำให้ผล test ไม่น่าเชื่อถือ
✅ ทุก `page.*()` และ `locator.*()` ต้องมี `await` นำหน้าเสมอ
*(source: https://playwright.dev/docs/test-typescript)*

---

❌ **แก้ `tsconfig.json` เองโดยเพิ่ม compiler options ที่ Playwright ไม่รองรับ**
```json
// ❌ เพิ่ม options เองเช่น "strict": true, "experimentalDecorators": true
{
  "compilerOptions": {
    "strict": true,
    "experimentalDecorators": true
  }
}
```
→ Playwright รองรับเฉพาะ `allowJs`, `baseUrl`, `paths`, `references` — options อื่นอาจทำให้ behavior เปลี่ยน
✅ ใช้ `tsconfig.json` ที่ `npm init playwright@latest` สร้างให้โดยไม่แก้ในช่วงเริ่มต้น
*(source: https://playwright.dev/docs/test-typescript — "Playwright only supports the following tsconfig options: allowJs, baseUrl, paths and references")*

---

❌ **รัน `npx playwright test` โดยไม่เช็ก TypeScript errors ก่อน**
→ ถ้ามี type error Playwright จะรันต่อโดย ignore TypeScript และอาจพบ runtime error แทน
✅ รัน `npx tsc -p tsconfig.json --noEmit` ใน CI pipeline ก่อนรัน test เพื่อ catch type errors ก่อน
*(source: https://playwright.dev/docs/test-typescript — "npx tsc -p tsconfig.json --noEmit")*

---

## 7. สรุปบท

บทนี้ครอบคลุม 4 จุดสำคัญ:

1. `npm init playwright@latest` สร้าง scaffold พร้อมใช้งาน — ตอบ TypeScript เสมอ
2. TypeScript เป็น first-class ใน Playwright — types ครบ, autocomplete ทำงาน, compile เป็น JS อัตโนมัติ
3. `async/await` คือกฎหลัก — ทุก action เป็น Promise ที่ต้อง await ไม่งั้น silent bug
4. `tsc --noEmit` คือ safety net — ใช้ catch TypeScript errors ก่อนรัน test จริง

**Retrieval Questions — ลองตอบก่อนดูเฉลย:**

1. TypeScript ถึงเป็น first-class language ใน Playwright เพราะอะไร? (ตอบให้ครอบคลุมอย่างน้อย 2 เหตุผล)
2. `tsc --noEmit` ทำอะไร และควรรันเมื่อไหร่?
3. ถ้า test ผ่านทั้งที่ยังไม่ได้ click ปุ่ม สาเหตุที่น่าเป็นไปได้มากที่สุดคืออะไร?

<details>
<summary>ดูเฉลย</summary>

**เฉลยข้อ 1:** Playwright team เขียน TypeScript type definitions ครบทุก API ไว้ใน `@playwright/test` เอง ทำให้ IDE autocomplete ทำงานเต็มรูปแบบ และ TypeScript ตรวจจับ type errors ก่อนรัน test ลด bug ที่หาสาเหตุยากลงได้มาก

**เฉลยข้อ 2:** `tsc --noEmit` compile TypeScript เพื่อตรวจ errors โดยไม่สร้างไฟล์ `.js` output ควรรันใน CI pipeline ก่อนขั้นตอนรัน test จริง เพื่อ fail fast เมื่อมี type error

**เฉลยข้อ 3:** ลืม `await` ก่อน action — `page.click()` สร้าง Promise แต่ไม่ถูก await ทำให้ action ถูก schedule แต่ test ดำเนินต่อไปก่อน ผล test จึงไม่สะท้อนสิ่งที่เกิดขึ้นจริง

</details>

---

**บทถัดไป:** บทที่ 3 — Locator Strategies: `getByRole`, `getByTestId`, `getByLabel` และทำไมถึงดีกว่า CSS selector
