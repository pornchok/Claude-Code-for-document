# บทที่ 3: Playwright Architecture — Browser, BrowserContext, Page

## ก่อนอ่านบทนี้ ลองตอบ:

1. ทำไม Playwright ถึงต้องใช้ TypeScript แทน JavaScript ธรรมดา? ข้อได้เปรียบหลักคืออะไร?
2. ใน `playwright.config.ts` ถ้าคุณต้องการให้ test รันบน Chromium เท่านั้น คุณต้องแก้ไข section ใด?

---

เฉลย:

1. TypeScript ให้ autocomplete, type checking ตอน compile-time (ไม่ใช่ runtime), และ refactoring ที่ปลอดภัยกว่า — `@playwright/test` รวม type definitions ไว้ครบในตัว ทำให้ IDE รู้ว่า `page.goto()` รับ argument อะไร และเตือนถ้าพิมพ์ผิดก่อนรัน test
2. แก้ใน `projects` array ภายใน `playwright.config.ts` — เหลือแค่ `{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }` แล้วลบ project อื่นออก

---

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- อธิบายความสัมพันธ์ระหว่าง Browser, BrowserContext, และ Page ได้ถูกต้อง
- เข้าใจว่าทำไม test ของ Playwright ถึง isolated กันโดยอัตโนมัติโดยไม่ต้องเขียน setup/teardown เอง
- รู้ว่าเมื่อไหร่ควรใช้ `page` fixture, `context` fixture, และ `browser` fixture — และความแตกต่างระหว่างกัน
- เขียน test ที่ simulate 2 users login พร้อมกันได้ โดยใช้ BrowserContext
- วิเคราะห์ได้ว่า test ที่ "บางครั้งผ่านบางครั้งพัง" เกิดจาก state leak ระหว่าง context หรือไม่
- อธิบายได้ว่าทำไม Playwright รัน test ได้เร็วกว่า Selenium ทั้งที่ test จำนวนมากขึ้น

---

## 2. ทำไมต้องรู้? (Why)

สมมติคุณเขียน test นี้:

```typescript
test('login as admin', async ({ page }) => {
  await page.goto('http://localhost:3000/login');
  await page.fill('[data-testid="input-username"]', 'admin');
  await page.fill('[data-testid="input-password"]', 'admin123');
  await page.click('[data-testid="btn-login"]');
  await expect(page.locator('[data-testid="session-badge"]')).toContainText('admin');
});

test('homepage shows guest view', async ({ page }) => {
  await page.goto('http://localhost:3000');
  await expect(page.locator('[data-testid="session-badge"]')).not.toBeVisible();
});
```

คำถามคือ: **test ที่ 2 จะผ่านหรือเปล่า?** ถ้ามาจาก Selenium คุณอาจกลัวว่า session จาก test แรก (login admin) จะ "รั่ว" มา test ที่สอง แล้วทำให้ session badge โผล่ขึ้นมา

คำตอบคือ: ผ่านเสมอ เพราะ Playwright สร้าง BrowserContext ใหม่ก่อนทุก test โดยอัตโนมัติ — cookies, localStorage, sessionStorage ทุกอย่างเริ่มใหม่จากศูนย์

แต่ถ้าคุณไม่เข้าใจว่า BrowserContext คืออะไร คุณจะไม่รู้ว่า isolation นี้เกิดขึ้นที่ระดับไหน หรือจะทำลายมันได้อย่างไร (โดยไม่ตั้งใจ) และเมื่อไหร่ต้องจัดการ context เอง

นี่คือเหตุผลที่ต้องเข้าใจ architecture 3 layer นี้ก่อนเขียน test จริง

---

## 3. Analogy: โรงแรม ห้องพัก และโต๊ะทำงาน

ลองนึกถึง **โรงแรมธุรกิจขนาดใหญ่**:

**Browser = โรงแรม**

โรงแรมเปิดอยู่ตลอด ไม่ปิดทุกครั้งที่แขกเช็คเอาท์ การสร้างโรงแรมขึ้นมาใหม่ (สร้าง Browser instance) แพงมาก ใช้เวลาและทรัพยากร Playwright เลยเลือกที่จะ "เปิดโรงแรมไว้ตลอด" แล้วแค่เปลี่ยนห้องให้แขกใหม่แทน

**BrowserContext = ห้องพัก (isolated)**

แต่ละห้องพักในโรงแรมมี keycard ของตัวเอง มินิบาร์ของตัวเอง และล็อคแยกจากกัน ถ้าแขกห้อง 301 สั่ง room service มา มันไม่ได้วางที่ห้อง 302 โดยอัตโนมัติ — นั่นคือ isolation แต่ละ BrowserContext มี cookies, localStorage, session ของตัวเองที่ไม่ปะปนกัน

**Page = โต๊ะทำงานในห้อง**

ในห้องพักหนึ่งห้อง คุณอาจวางแล็ปท็อปไว้ 2 เครื่อง เปิดงานคนละชิ้น — นั่นคือ Page หลายตัวใน BrowserContext เดียวกัน ทั้งสองแล็ปท็อปใช้ keycard เดียวกันเข้าห้อง (share session เดียวกัน) แต่แสดงเนื้อหาคนละชิ้น

---

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**

- **1 context ต้องมีแค่ 1 page** — จริงๆ BrowserContext หนึ่งตัวมี page ได้หลายตัว (หลาย tab) ที่ share cookies/localStorage กัน เช่นเดียวกับห้องพักที่มีโต๊ะทำงานหลายตัว *(source: "Each BrowserContext can have multiple pages.")*
- **ปิดห้อง (close context) แล้วโรงแรม (browser) ปิดด้วย** — Browser ยังทำงานต่อหลัง context ถูก close Playwright ยัง reuse browser instance เดิมสำหรับ test ถัดไป *(source: "Browsers are shared across tests to optimize resources.")*
- **แต่ละห้องแยกจากกันโดย hardware** — ใน analogy โรงแรมแต่ละห้องมีผนังจริง แต่ใน browser isolation เกิดจาก software — ถ้าคุณเขียน code ที่ expose state ระหว่าง context เอง (เช่นผ่าน global variable ใน test code) isolation ก็พัง

---

## 4. เนื้อหาหลัก

### 4.1 Browser — กระบวนการที่แพงและ Reuse ได้

Browser ใน Playwright คือ Chromium, Firefox, หรือ WebKit process ที่รันอยู่จริง การเปิด browser ใหม่แต่ละครั้งหมายความว่า OS ต้องโหลด process ขนาดใหญ่ขึ้นมา จัดสรร memory และเตรียม rendering engine — ใช้เวลาหลายร้อย millisecond

Playwright จัดการปัญหานี้ด้วยการ **reuse browser ข้าม tests** ภายใน worker เดียวกัน browser เปิดขึ้นมาครั้งเดียวตอนเริ่มต้น และปิดเมื่อ worker ทั้งหมดของ test suite ทำงานเสร็จ

ทำให้ test 100 ตัวใช้ browser เดียวกัน แทนที่จะเปิด/ปิด 100 รอบแบบ Selenium

### 4.2 BrowserContext — Isolation Engine

BrowserContext คือหัวใจของความ isolated ใน Playwright

> "BrowserContexts which are equivalent to incognito-like profiles. They are fast and cheap to create and are completely isolated, even when running in a single browser."
> — playwright.dev/docs/browser-contexts

แต่ละ BrowserContext มีของของตัวเองแยกกันสมบูรณ์:
- **Cookies** — login session, tracking cookies
- **localStorage** และ **sessionStorage** — state ที่ JavaScript เก็บไว้
- **Auth state** — HTTP authentication headers
- **Cache** — resources ที่ browser เก็บไว้เพื่อเร็วขึ้น

> "each test has its own local storage, session storage, cookies etc."
> — playwright.dev/docs/browser-contexts

และที่สำคัญมาก: BrowserContext สร้างเร็วและเบา เพราะ Playwright ไม่ต้องเปิด browser process ใหม่ แค่สร้าง isolated profile ใหม่บน browser เดิม

### 4.3 Page — หนึ่ง Tab ใน Context

> "A Page refers to a single tab or a popup window within a browser context."
> — playwright.dev/docs/pages

Page คือสิ่งที่คุณ interact ด้วยมากที่สุด — นี่คือ object ที่คุณใช้ `page.goto()`, `page.click()`, `page.fill()` ทุกอย่าง

Page ผูกอยู่กับ BrowserContext ที่สร้างมัน และ share state กับ page อื่นใน context เดียวกัน:

> "Pages inside a context respect context-level emulation, like viewport sizes, custom network routes or browser locale."
> — playwright.dev/docs/pages

### 4.4 Default Fixtures: page, context, browser

Playwright Test มี fixtures สามตัวที่สำคัญ:

**`page` fixture** — สิ่งที่คุณใช้ 90% ของเวลา

> "Isolated page for this test run."
> — playwright.dev/docs/test-fixtures

Playwright สร้าง context ใหม่ก่อนแต่ละ test แล้วสร้าง page ใน context นั้น หลัง test เสร็จ ทั้ง page และ context ถูก close อัตโนมัติ

> "Playwright creates a context for each test, and provides a default Page in that context."
> — playwright.dev/docs/browser-contexts

> "Running the test creates a new browser context each time."
> — playwright.dev/docs/browser-contexts

**`context` fixture** — ใช้เมื่อต้องการ page หลายตัวที่ share state กัน

> "Isolated context for this test run. The `page` fixture belongs to this context as well."
> — playwright.dev/docs/test-fixtures

ถ้าคุณต้องการ test ที่เปิด 2 tab และให้ทั้งสองเห็น session เดียวกัน ใช้ `context` fixture แล้ว `context.newPage()` เองได้เลย

**`browser` fixture** — ใช้เมื่อต้องการ control context ด้วยตัวเอง

> "Browsers are shared across tests to optimize resources."
> — playwright.dev/docs/test-fixtures

> "Test-scoped fixtures are torn down after each test, while worker-scoped fixtures are only torn down when the worker process executing tests is torn down."
> — playwright.dev/docs/test-fixtures

`browser` fixture คือ worker-scoped — browser เดิมถูก reuse ข้าม tests ทั้งหมดใน worker เดียวกัน ใช้ `browser` fixture เมื่อคุณต้องการสร้าง context เองด้วย `browser.newContext()` เช่น ต้องการ multi-user scenario ใน test เดียว

### 4.5 Multi-User Scenario

Playwright รองรับ multi-user testing โดยธรรมชาติ:

> "Playwright can create multiple browser contexts within a single scenario. This is useful when you want to test for multi-user functionality, like a chat."
> — playwright.dev/docs/browser-contexts

แต่ละ context = session ของ user คนละคน บน browser เดียวกัน — ทำงานพร้อมกันได้โดยไม่ต้อง Selenium Grid

### 4.6 เปรียบเทียบ RF/Selenium vs Playwright

| | Robot Framework + Selenium | Playwright |
|-|--------------------------|------------|
| Browser startup | เปิด process ใหม่ทุก test case (ช้า) | Reuse browser ข้าม tests (เร็ว) |
| Session isolation | ต้องเขียน setup/teardown เอง | Built-in ผ่าน BrowserContext อัตโนมัติ |
| Multi-user parallel | ต้องใช้ Selenium Grid (infrastructure แยก) | ใช้หลาย context บน browser เดียวกัน |
| Auth state sharing | copy cookie/sessionStorage ด้วย script เอง | `storageState` export/import (Ch13) |
| State cleanup | ต้องเขียน teardown keyword | context ถูก close อัตโนมัติหลังแต่ละ test |

---

## 5. ตัวอย่าง

### Beginner: Default Page Fixture Isolation

สถานการณ์: ต้องการยืนยันว่า test แต่ละตัวไม่ได้ share session กัน

```typescript
// tested: Playwright v1.50+, Node.js 20+ (requires demo app at localhost:3000)
import { test, expect } from '@playwright/test';

// Test 1: Login เป็น admin
test('test 1 — fresh session starts logged out', async ({ page }) => {
  // เริ่มต้น: ไปที่ homepage โดยไม่ login
  await page.goto('http://localhost:3000');
  // session-badge ไม่ควรมี เพราะ context ใหม่สะอาด
  await expect(page.locator('[data-testid="session-badge"]')).not.toBeVisible();

  // ทำการ login
  await page.goto('http://localhost:3000/login');
  await page.fill('[data-testid="input-username"]', 'admin');
  await page.fill('[data-testid="input-password"]', 'admin123');
  await page.click('[data-testid="btn-login"]');

  // ตรวจว่า session badge แสดง admin
  await expect(page.locator('[data-testid="session-badge"]')).toContainText('admin');
  // หลัง test นี้เสร็จ Playwright จะ close context นี้ทิ้ง
});

// Test 2: Context ใหม่ — ไม่มีร่องรอยจาก Test 1
test('test 2 — completely new context, no admin session', async ({ page }) => {
  await page.goto('http://localhost:3000');
  // ถ้า Playwright ไม่มี isolation จริง session badge จะแสดง admin จาก test 1
  // แต่เพราะ context ใหม่ทุกครั้ง — badge ไม่มี
  await expect(page.locator('[data-testid="session-badge"]')).not.toBeVisible();
});
```

สังเกตว่า: ไม่มี `beforeEach`, ไม่มี `afterEach`, ไม่มี cleanup code เลย — Playwright จัดการให้ทั้งหมด

---

### Intermediate: สองแผนกทดสอบ Feature ที่ต้อง Cross-User Verification

สถานการณ์: ระบบจัดการคำสั่งซื้อ — admin ยืนยัน order แล้ว customer ต้องเห็น status เปลี่ยนทันที ต้องการ verify ว่าทั้งสอง user เห็นสถานะที่ถูกต้องพร้อมกัน

```typescript
// tested: Playwright v1.50+, Node.js 20+ (requires demo app at localhost:3000)
import { test, expect } from '@playwright/test';

test('admin and customer see correct session independently', async ({ browser }) => {
  // สร้าง 2 contexts — แต่ละตัวคือ user คนละคน
  const adminCtx = await browser.newContext();
  const customerCtx = await browser.newContext();

  const adminPage = await adminCtx.newPage();
  const customerPage = await customerCtx.newPage();

  try {
    // Admin login ใน context แรก
    await adminPage.goto('http://localhost:3000/login');
    await adminPage.fill('[data-testid="input-username"]', 'admin');
    await adminPage.fill('[data-testid="input-password"]', 'admin123');
    await adminPage.click('[data-testid="btn-login"]');
    await expect(adminPage.locator('[data-testid="session-badge"]')).toContainText('admin');

    // Customer login ใน context ที่สอง (แยกกันสมบูรณ์)
    await customerPage.goto('http://localhost:3000/login');
    await customerPage.fill('[data-testid="input-username"]', 'testuser');
    await customerPage.fill('[data-testid="input-password"]', 'test123');
    await customerPage.click('[data-testid="btn-login"]');
    await expect(customerPage.locator('[data-testid="session-badge"]')).toContainText('testuser');

    // ตรวจว่า session ของแต่ละคนไม่ปะปนกัน
    // admin badge ยังเป็น 'admin' — ไม่ถูก overwrite โดย customer login
    await expect(adminPage.locator('[data-testid="session-badge"]')).toContainText('admin');
    // customer badge ยังเป็น 'testuser' — ไม่ถูก overwrite โดย admin
    await expect(customerPage.locator('[data-testid="session-badge"]')).toContainText('testuser');

  } finally {
    // ต้อง close contexts ที่สร้างเองด้วยมือเสมอ
    await adminCtx.close();
    await customerCtx.close();
  }
});
```

สังเกตสิ่งสำคัญ:
- ใช้ `browser` fixture (ไม่ใช่ `page`) เพราะต้องสร้าง context เอง
- ใช้ `try/finally` เพื่อให้ contexts ถูก close แม้ test fail
- บน browser เดียวกัน แต่ state ไม่ปะปนกันเลย

---

### Advanced: Diagnosis — วิเคราะห์ Flaky Test จาก State Leak

สถานการณ์: Junior test engineer ส่ง test มาให้ดู บอกว่า "test 2 บางครั้งผ่าน บางครั้งพัง ไม่รู้ทำไม"

```typescript
// ⚠️ CODE ที่มีปัญหา — ให้วิเคราะห์หาสาเหตุก่อนดูเฉลย
import { test, expect } from '@playwright/test';

let sharedPage: any;

test.beforeAll(async ({ browser }) => {
  const context = await browser.newContext();
  sharedPage = await context.newPage();
  await sharedPage.goto('http://localhost:3000/login');
  await sharedPage.fill('[data-testid="input-username"]', 'admin');
  await sharedPage.fill('[data-testid="input-password"]', 'admin123');
  await sharedPage.click('[data-testid="btn-login"]');
});

test('test A — check admin dashboard', async () => {
  await sharedPage.goto('http://localhost:3000/dashboard');
  await expect(sharedPage.locator('[data-testid="session-badge"]')).toContainText('admin');
});

test('test B — check homepage', async () => {
  await sharedPage.goto('http://localhost:3000');
  await expect(sharedPage.locator('[data-testid="session-badge"]')).toContainText('admin');
});

test('test C — logout then check guest view', async () => {
  await sharedPage.click('[data-testid="btn-logout"]');
  await expect(sharedPage.locator('[data-testid="session-badge"]')).not.toBeVisible();
});
```

**Exercise: วิเคราะห์ก่อนดูเฉลย**

ลองตอบคำถามเหล่านี้ก่อน scroll ต่อ:
1. ปัญหาหลักของ code นี้คืออะไร?
2. ทำไม test B ถึง "บางครั้งผ่านบางครั้งพัง"?
3. ถ้ารัน test แบบ parallel จะเกิดอะไรขึ้น?

---

**เฉลย:**

**ปัญหาหลัก: State Sharing ผ่าน `sharedPage`**

`sharedPage` เป็น variable ระดับ module ที่ tests ทุกตัว share กัน นี่คือการทำลาย isolation โดยตั้งใจ แต่ทำให้เกิดปัญหาหลายอย่าง:

**ปัญหาที่ 1 — Test Order Dependency**

ถ้า test C (logout) รันก่อน test A หรือ B — test A และ B จะเห็น session ว่าง (user logout ไปแล้ว) แล้วพัง Playwright ไม่รับประกัน order ของ test ถ้ารัน parallel

**ปัญหาที่ 2 — Parallel Execution Race Condition**

ถ้า test A กับ test B รันพร้อมกัน ทั้งสองใช้ `sharedPage` ตัวเดียวกัน test A อาจกำลัง navigate ไป `/dashboard` ขณะที่ test B กำลัง `goto('/') ` — page จะ navigate ไปที่ URL ล่าสุดที่สั่ง ทำให้ทั้งสอง assert บน URL ผิด

**ปัญหาที่ 3 — `any` type ซ่อน error**

`let sharedPage: any` ทำให้ TypeScript ไม่เตือนถ้าคุณใช้ method ผิด เช่น `sharedPage.fillll()` จะผ่าน compile แต่พัง runtime

**วิธีแก้ที่ถูกต้อง:**

```typescript
// ✅ แก้ไข: ให้แต่ละ test จัดการ login ของตัวเอง
// หรือใช้ storageState เพื่อ reuse auth (ดู Ch13)
import { test, expect } from '@playwright/test';

// Option 1: แยก login ทุก test (ง่ายที่สุด, isolated ที่สุด)
test('test A — check admin dashboard', async ({ page }) => {
  await page.goto('http://localhost:3000/login');
  await page.fill('[data-testid="input-username"]', 'admin');
  await page.fill('[data-testid="input-password"]', 'admin123');
  await page.click('[data-testid="btn-login"]');
  await page.goto('http://localhost:3000/dashboard');
  await expect(page.locator('[data-testid="session-badge"]')).toContainText('admin');
});

test('test C — logout then check guest view', async ({ page }) => {
  // Login ก่อน แล้วค่อย test logout
  await page.goto('http://localhost:3000/login');
  await page.fill('[data-testid="input-username"]', 'admin');
  await page.fill('[data-testid="input-password"]', 'admin123');
  await page.click('[data-testid="btn-login"]');
  // ตอนนี้ logout
  await page.click('[data-testid="btn-logout"]');
  await expect(page.locator('[data-testid="session-badge"]')).not.toBeVisible();
});
```

**Key Insight:** ถ้า test ของคุณ sensitive ต่อ order การรัน — นั่นคือสัญญาณว่า state leak มักเกิดจาก shared object ที่ไม่ควร share

---

## 6. Common Mistakes

**❌ Mistake 1: Login ใน `beforeAll` แล้วหวังว่า `page` fixture จะ share session**

```typescript
// ❌ ผิด: beforeAll login แต่ page fixture แต่ละ test ได้ context ใหม่
test.beforeAll(async ({ page }) => {
  await page.goto('http://localhost:3000/login');
  await page.fill('[data-testid="input-username"]', 'admin');
  await page.click('[data-testid="btn-login"]');
  // login นี้อยู่ใน context ที่ถูกทิ้งหลัง beforeAll
});

test('should see admin panel', async ({ page }) => {
  // page นี้เป็น context ใหม่ ไม่มี login state จาก beforeAll
  await expect(page.locator('[data-testid="session-badge"]')).toContainText('admin'); // ❌ พัง
});
```

```typescript
// ✅ ถูก: ถ้าต้องการ share auth ให้ใช้ storageState (เรียนใน Ch13)
// หรือ login ใน test เอง
test('should see admin panel', async ({ page }) => {
  await page.goto('http://localhost:3000/login');
  await page.fill('[data-testid="input-username"]', 'admin');
  await page.fill('[data-testid="input-password"]', 'admin123');
  await page.click('[data-testid="btn-login"]');
  await expect(page.locator('[data-testid="session-badge"]')).toContainText('admin'); // ✅ ผ่าน
});
```

*(source: https://playwright.dev/docs/browser-contexts — "Playwright creates a context for each test, and provides a default Page in that context.")*

---

**❌ Mistake 2: ไม่ close context ที่สร้างด้วย `browser.newContext()`**

```typescript
// ❌ ผิด: สร้าง context แต่ไม่ close
test('multi-user test', async ({ browser }) => {
  const ctx1 = await browser.newContext();
  const ctx2 = await browser.newContext();
  const page1 = await ctx1.newPage();
  const page2 = await ctx2.newPage();
  // ... test logic ...
  // ลืม close ctx1, ctx2
  // memory leak + resource ค้างอยู่
});
```

```typescript
// ✅ ถูก: ใช้ try/finally หรือ afterEach เพื่อให้ close เสมอ แม้ test fail
test('multi-user test', async ({ browser }) => {
  const ctx1 = await browser.newContext();
  const ctx2 = await browser.newContext();
  try {
    const page1 = await ctx1.newPage();
    const page2 = await ctx2.newPage();
    // ... test logic ...
  } finally {
    await ctx1.close();
    await ctx2.close();
  }
});
```

*(source: https://playwright.dev/docs/test-fixtures — "Test-scoped fixtures are torn down after each test, while worker-scoped fixtures are only torn down when the worker process executing tests is torn down." — fixtures ที่สร้างเองไม่มี auto-teardown)*

---

**❌ Mistake 3: คิดว่า `page.close()` แล้ว context ปิดด้วย**

```typescript
// ❌ ผิด: ปิดแค่ page แต่ context ยังอยู่
const ctx = await browser.newContext();
const page = await ctx.newPage();
await page.goto('http://localhost:3000/login');
// ... do stuff ...
await page.close(); // ❌ context ยังค้างอยู่ใน memory

// ✅ ถูก: ปิด context แทน (ซึ่ง close page ที่อยู่ใน context ด้วยอัตโนมัติ)
await ctx.close(); // ✅ close ทั้ง context และ pages ทั้งหมดใน context
```

*(source: https://playwright.dev/docs/pages — "A Page refers to a single tab or a popup window within a browser context." — page เป็นส่วนหนึ่งของ context, การ close page ไม่ได้ destroy context)*

---

**❌ Mistake 4: ใช้ `page` fixture แล้วพยายามสร้าง Page เพิ่มด้วย `browser.newPage()`**

```typescript
// ❌ ผิด: browser.newPage() สร้าง page ใน context default ของ browser
// ไม่ใช่ context เดียวกับ page fixture
test('wrong multi-tab', async ({ page, browser }) => {
  const secondPage = await browser.newPage(); // ❌ คนละ context กับ page fixture
  // secondPage ไม่ share cookies กับ page
});
```

```typescript
// ✅ ถูก: ใช้ context fixture เพื่อสร้าง page ที่ share context เดียวกัน
test('correct multi-tab', async ({ page, context }) => {
  const secondPage = await context.newPage(); // ✅ share context กับ page fixture
  // secondPage และ page share cookies/localStorage กัน
});
```

*(source: https://playwright.dev/docs/pages — "Each BrowserContext can have multiple pages.")*

---

## 7. สรุปบท

### Key Takeaways

- **Browser** = process ที่ Playwright reuse ข้าม tests — เปิดครั้งเดียวต่อ worker, ปิดเมื่อ worker หยุด
- **BrowserContext** = incognito-like profile ที่มี cookies/localStorage/auth state ของตัวเอง — Playwright สร้างใหม่ก่อนแต่ละ test, close หลัง test โดยอัตโนมัติ
- **Page** = 1 tab ใน 1 context — page หลายตัวใน context เดียวกันจะ share session กัน
- `page` fixture ใช้ 90% ของงาน, `context` fixture ใช้เมื่อต้องการหลาย page ที่ share state, `browser` fixture ใช้เมื่อต้องการ multi-user ใน test เดียว
- ถ้า test fail เพราะ session มาจากไหนไม่รู้ — ให้หา shared variable ที่ข้าม test boundary

---

### Retrieval Questions — ลองตอบก่อนดูเฉลย

1. นักพัฒนาคนหนึ่งบอกว่า "Playwright ช้ากว่า Selenium เพราะต้องสร้าง BrowserContext ใหม่ทุก test" — ข้อโต้แย้งของคุณคืออะไร?

2. ถ้า test A login เป็น admin และ test B คาดหวังว่าจะเห็น homepage แบบ guest — คุณต้องเขียน cleanup code อะไรใน `afterEach` บ้าง?

3. คุณกำลังเขียน test สำหรับ feature "online users counter" ที่แสดงจำนวน active sessions คุณจะใช้ fixture อะไร และ structure test อย่างไร?

---

เฉลย:

1. ข้อโต้แย้ง: BrowserContext สร้างเร็วและเบา ("fast and cheap to create") เพราะ Playwright ไม่ต้องสร้าง browser process ใหม่ — แค่สร้าง isolated profile บน browser เดิม ส่วน browser process ที่แพงนั้น Playwright reuse ข้าม tests ทั้งหมดอยู่แล้ว ดังนั้น Playwright มักเร็วกว่า Selenium ที่ต้องเปิด browser process ใหม่ทุก test

2. คำตอบ: ไม่ต้องเขียนอะไรเลย — Playwright close context หลังแต่ละ test อัตโนมัติ test B จะได้ context ใหม่ที่สะอาดโดยไม่มีร่องรอย session จาก test A นี่คือ design ที่ทำให้ไม่ต้องเขียน cleanup

3. แนวทาง: ใช้ `browser` fixture แล้วสร้าง context หลายตัว — แต่ละ context คือ active session ของ user คนละคน ตัวอย่าง: สร้าง 3 contexts พร้อมกัน ให้แต่ละตัว navigate ไปที่ app แล้ว assert ว่า counter แสดง "3 online users" เสร็จแล้ว close contexts ทีละตัวแล้ว verify counter ลดลง ใช้ `try/finally` เพื่อให้ contexts ถูก close เสมอ

---

*ต่อไป: Ch04 — Locators & Selectors (วิธีหา element บนหน้าเว็บที่ robust และไม่ brittle)*
