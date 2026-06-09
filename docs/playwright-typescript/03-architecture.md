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
  await expect(page.locator('[data-testid="session-badge"]')).toContainText('Not logged in');
});
```

คำถามคือ: **test ที่ 2 จะผ่านหรือเปล่า?** ถ้ามาจาก Selenium คุณอาจกลัวว่า session จาก test แรก (login admin) จะ "รั่ว" มา test ที่สอง แล้วทำให้ session badge แสดง "admin"

คำตอบคือ: ผ่านเสมอ เพราะ Playwright สร้าง BrowserContext ใหม่ก่อนทุก test โดยอัตโนมัติ — cookies, localStorage, sessionStorage ทุกอย่างเริ่มใหม่จากศูนย์ badge จึงแสดง "Not logged in" แทนที่จะเป็น "admin"

แต่ถ้าคุณไม่เข้าใจว่า BrowserContext คืออะไร คุณจะไม่รู้ว่า isolation นี้เกิดขึ้นที่ระดับไหน หรือจะทำลายมันได้อย่างไร (โดยไม่ตั้งใจ) และเมื่อไหร่ต้องจัดการ context เอง

นี่คือเหตุผลที่ต้องเข้าใจ architecture 3 layer นี้ก่อนเขียน test จริง

---

## 3. เนื้อหาหลัก

### 3.1 Browser — เปิดครั้งเดียว ใช้ได้ทั้ง suite

Browser ใน Playwright คือโปรแกรม Chromium, Firefox, หรือ WebKit ที่รันอยู่จริงบนเครื่อง การเปิด browser ขึ้นมาแต่ละครั้งใช้เวลาหลายร้อย millisecond เพราะ OS ต้องโหลดโปรแกรมขนาดใหญ่และจัดสรร memory

Playwright แก้ปัญหานี้ด้วยการ **เปิด browser ครั้งเดียว แล้วใช้ร่วมกันทุก test** — test 100 ตัวใช้ browser เดิม ไม่ได้เปิด/ปิด 100 รอบแบบ Selenium

> "Browsers are shared across tests to optimize resources."
> — playwright.dev/docs/test-fixtures

### 3.2 BrowserContext — Isolation Engine

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

### 3.3 Page — หนึ่ง Tab ใน Context

> "A Page refers to a single tab or a popup window within a browser context."
> — playwright.dev/docs/pages

Page คือสิ่งที่คุณ interact ด้วยมากที่สุด — นี่คือ object ที่คุณใช้ `page.goto()`, `page.click()`, `page.fill()` ทุกอย่าง

Page ผูกอยู่กับ BrowserContext ที่สร้างมัน และ share state กับ page อื่นใน context เดียวกัน:

> "Pages inside a context respect context-level emulation, like viewport sizes, custom network routes or browser locale."
> — playwright.dev/docs/pages

### 3.4 Fixture คืออะไร และ Default Fixtures ที่ Playwright เตรียมให้

**Fixture คืออะไร?**

Fixture คือ resource ที่ Playwright **เตรียมให้ก่อน test เริ่ม และเก็บกวาดให้เองหลัง test จบ** โดยที่คุณไม่ต้องเขียน setup/teardown เอง

ลองดูโค้ด test ทั่วไป:

```typescript
test('my test', async ({ page }) => {
  //                    ↑
  //            ตรงนี้แหละคือ fixture
  //            Playwright inject page มาให้อัตโนมัติ
  await page.goto('http://localhost:3000');
});
```

`{ page }` ในวงเล็บหลัง `async` คือการบอก Playwright ว่า "ฉันต้องการ `page` — ช่วยเตรียมมาให้หน่อย" Playwright จะสร้าง BrowserContext ใหม่ เปิด Page ใน context นั้น ส่งให้คุณใช้ใน test แล้วปิดทุกอย่างให้หลัง test จบ

เทียบกับ Robot Framework ที่ต้องเขียน:
```robot
*** Test Cases ***
My Test
    [Setup]    Open Browser    http://localhost:3000    Chrome
    ...
    [Teardown]    Close Browser
```

Playwright ทำส่วน Setup และ Teardown ให้ผ่าน fixture อัตโนมัติ

---

**Default Fixtures ที่ใช้บ่อย:**

**`page` fixture** — ใช้ 90% ของเวลา

```typescript
test('example', async ({ page }) => {
  // page คือ 1 browser tab ที่สะอาดใหม่
  // Playwright สร้างมาให้ก่อน test เริ่ม
  // และ close ให้หลัง test จบ — ไม่ต้องเขียนอะไรเพิ่ม
  await page.goto('http://localhost:3000');
});
```

> "Playwright creates a context for each test, and provides a default Page in that context."
> — playwright.dev/docs/browser-contexts

**`context` fixture** — ใช้เมื่อต้องการเปิดหลาย tab ที่ share session กัน

```typescript
test('multi-tab', async ({ page, context }) => {
  // page ตัวแรกมีอยู่แล้ว
  const secondTab = await context.newPage(); // เปิด tab ที่ 2 ใน session เดียวกัน
  // ทั้ง page และ secondTab เห็น cookies เดียวกัน
});
```

> "Isolated context for this test run. The `page` fixture belongs to this context as well."
> — playwright.dev/docs/test-fixtures

**`browser` fixture** — ใช้เมื่อต้องการสร้าง session แยกกันหลายอัน (multi-user scenario)

```typescript
test('multi-user', async ({ browser }) => {
  // สร้าง 2 sessions แยกกันสมบูรณ์
  const adminCtx = await browser.newContext();  // session ของ admin
  const userCtx  = await browser.newContext();  // session ของ user ทั่วไป
  // ทั้งสองไม่ share cookies กัน
});
```

> "Browsers are shared across tests to optimize resources."
> — playwright.dev/docs/test-fixtures

**สรุปการเลือก fixture:**

| ต้องการ | ใช้ fixture |
|---------|------------|
| test ทั่วไป 1 user | `page` |
| เปิดหลาย tab ที่ login ด้วย user เดียวกัน | `context` |
| ทดสอบหลาย user พร้อมกัน | `browser` |

### 3.5 Multi-User Scenario

Playwright รองรับ multi-user testing โดยธรรมชาติ:

> "Playwright can create multiple browser contexts within a single scenario. This is useful when you want to test for multi-user functionality, like a chat."
> — playwright.dev/docs/browser-contexts

แต่ละ context = session ของ user คนละคน บน browser เดียวกัน — ทำงานพร้อมกันได้โดยไม่ต้อง Selenium Grid

### 3.6 เปรียบเทียบ RF/Selenium vs Playwright

| | Robot Framework + Selenium | Playwright |
|-|--------------------------|------------|
| Browser startup | เปิด process ใหม่ทุก test case (ช้า) | Reuse browser ข้าม tests (เร็ว) |
| Session isolation | ต้องเขียน setup/teardown เอง | Built-in ผ่าน BrowserContext อัตโนมัติ |
| Multi-user parallel | ต้องใช้ Selenium Grid (infrastructure แยก) | ใช้หลาย context บน browser เดียวกัน |
| Auth state sharing | copy cookie/sessionStorage ด้วย script เอง | `storageState` export/import (Ch13) |
| State cleanup | ต้องเขียน teardown keyword | context ถูก close อัตโนมัติหลังแต่ละ test |

---

## 4. ตัวอย่าง

### Beginner: Default Page Fixture Isolation

สถานการณ์: ต้องการยืนยันว่า test แต่ละตัวไม่ได้ share session กัน

**อ่าน syntax นี้ก่อน:**

```typescript
test('ชื่อ test', async ({ page }) => {
//                          ↑
//                   นี่คือ fixture — Playwright inject page มาให้
//                   ไม่ต้องสร้างเอง ไม่ต้องปิดเอง

  await page.goto('...');
//  ↑
//  await = รอให้เสร็จก่อนบรรทัดถัดไป
//  ทุก action ใน Playwright ต้องมี await เสมอ
});
```

```typescript
// tested: Playwright v1.50+, Node.js 20+ (requires demo app at localhost:3000)
import { test, expect } from '@playwright/test';

// Test 1: login เป็น admin แล้วปล่อยให้ Playwright จัดการ cleanup
test('test 1 — login as admin', async ({ page }) => {
  // Playwright สร้าง context ใหม่สะอาดให้ก่อน test นี้เสมอ
  await page.goto('http://localhost:3000');
  await expect(page.locator('[data-testid="session-badge"]')).toContainText('Not logged in');

  // login เป็น admin
  await page.goto('http://localhost:3000/login');
  await page.fill('[data-testid="input-username"]', 'admin');
  await page.fill('[data-testid="input-password"]', 'admin123');
  await page.click('[data-testid="btn-login"]');
  await expect(page.locator('[data-testid="session-badge"]')).toContainText('admin');

  // หลัง test นี้จบ Playwright จะ close context (และ session ที่ login ไว้) ทิ้งอัตโนมัติ
  // ไม่ต้องเขียน logout หรือ cleanup ใดๆ
});

// Test 2: ได้ context ใหม่สะอาด — ไม่มีร่องรอยจาก Test 1 เลย
test('test 2 — fresh context, no session from test 1', async ({ page }) => {
  await page.goto('http://localhost:3000');
  // ถ้า Playwright ไม่มี isolation จริง ตรงนี้จะเห็น "admin" จาก test 1
  // แต่เพราะ context ใหม่ทุกครั้ง — badge แสดง "Not logged in" เสมอ
  await expect(page.locator('[data-testid="session-badge"]')).toContainText('Not logged in');
});
```

สังเกตว่า: ไม่มี `beforeEach`, ไม่มี `afterEach`, ไม่มี cleanup code เลย — Playwright จัดการให้ทั้งหมดผ่าน fixture

---

### Intermediate: ทดสอบ Role-Based Access — admin กับ testuser เห็น Admin Dashboard ต่างกัน

สถานการณ์: ระบบมีหน้า Admin Dashboard ที่จำกัดสิทธิ์ — admin ต้องเห็น welcome message แต่ testuser (role ปกติ) ต้องเห็น "Access denied" ต้องการทดสอบทั้งสองพร้อมกันบน browser เดียว เพื่อยืนยันว่า context isolation ทำให้ role ของแต่ละ session แยกกันสมบูรณ์

```typescript
// tested: Playwright v1.50+, Node.js 20+ (requires demo app at localhost:3000)
import { test, expect } from '@playwright/test';

test('admin sees dashboard, testuser sees access denied — same browser, different contexts', async ({ browser }) => {
  // สร้าง 2 contexts — แต่ละตัวมี session และ role ของตัวเอง
  const adminCtx = await browser.newContext();
  const userCtx = await browser.newContext();

  const adminPage = await adminCtx.newPage();
  const userPage = await userCtx.newPage();

  try {
    // Login เป็น admin ใน context แรก
    await adminPage.goto('http://localhost:3000/login');
    await adminPage.fill('[data-testid="input-username"]', 'admin');
    await adminPage.fill('[data-testid="input-password"]', 'admin123');
    await adminPage.click('[data-testid="btn-login"]');

    // Login เป็น testuser ใน context ที่สอง
    await userPage.goto('http://localhost:3000/login');
    await userPage.fill('[data-testid="input-username"]', 'testuser');
    await userPage.fill('[data-testid="input-password"]', 'test123');
    await userPage.click('[data-testid="btn-login"]');

    // ทั้งสอง navigate ไปหน้าเดียวกัน — /admin
    await adminPage.goto('http://localhost:3000/admin');
    await userPage.goto('http://localhost:3000/admin');

    // Admin เห็น welcome message (มีสิทธิ์)
    await expect(adminPage.locator('[data-testid="admin-welcome"]')).toBeVisible();

    // testuser เห็น access denied (ไม่มีสิทธิ์) — หน้าเดียวกัน แต่ต่างกันเพราะ session ต่างกัน
    await expect(userPage.locator('[data-testid="access-denied"]')).toBeVisible();

  } finally {
    // ต้อง close contexts ที่สร้างเองด้วยมือเสมอ
    await adminCtx.close();
    await userCtx.close();
  }
});
```

สังเกตสิ่งสำคัญ:
- ใช้ `browser` fixture (ไม่ใช่ `page`) เพราะต้องสร้าง context เอง
- ใช้ `try/finally` เพื่อให้ contexts ถูก close แม้ test fail
- URL เดียวกัน (`/admin`) แต่ผลต่างกันเพราะ session แยกกัน — นี่คือพลังของ context isolation

---

### Advanced: Diagnosis — วิเคราะห์ Flaky Test จาก State Leak

สถานการณ์: Junior test engineer ส่ง test มาให้ดู บอกว่า "test 2 บางครั้งผ่าน บางครั้งพัง ไม่รู้ทำไม"

```typescript
// ⚠️ ตัวอย่างนี้จงใจเขียนผิด เพื่อแสดง anti-pattern — ห้าม copy
// tested: Playwright v1.50+, Node.js 20+ (requires demo app at localhost:3000)
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
  await sharedPage.click('[data-testid="nav-logout"]');
  await expect(sharedPage.locator('[data-testid="session-badge"]')).toContainText('Not logged in');
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
// tested: Playwright v1.50+, Node.js 20+ (requires demo app at localhost:3000)
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
  await page.click('[data-testid="nav-logout"]');
  await expect(page.locator('[data-testid="session-badge"]')).toContainText('Not logged in');
});
```

**Key Insight:** ถ้า test ของคุณ sensitive ต่อ order การรัน — นั่นคือสัญญาณว่า state leak มักเกิดจาก shared object ที่ไม่ควร share

---

## 5. Common Mistakes

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

## 6. สรุปบท

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

<details>
<summary>ดูเฉลย</summary>

1. ข้อโต้แย้ง: BrowserContext สร้างเร็วและเบา ("fast and cheap to create") เพราะ Playwright ไม่ต้องสร้าง browser process ใหม่ — แค่สร้าง isolated profile บน browser เดิม ส่วน browser process ที่แพงนั้น Playwright reuse ข้าม tests ทั้งหมดอยู่แล้ว ดังนั้น Playwright มักเร็วกว่า Selenium ที่ต้องเปิด browser process ใหม่ทุก test

2. คำตอบ: ไม่ต้องเขียนอะไรเลย — Playwright close context หลังแต่ละ test อัตโนมัติ test B จะได้ context ใหม่ที่สะอาดโดยไม่มีร่องรอย session จาก test A นี่คือ design ที่ทำให้ไม่ต้องเขียน cleanup

3. แนวทาง: ใช้ `browser` fixture แล้วสร้าง context หลายตัว — แต่ละ context คือ active session ของ user คนละคน ตัวอย่าง: สร้าง 3 contexts พร้อมกัน ให้แต่ละตัว navigate ไปที่ app แล้ว assert ว่า counter แสดง "3 online users" เสร็จแล้ว close contexts ทีละตัวแล้ว verify counter ลดลง ใช้ `try/finally` เพื่อให้ contexts ถูก close เสมอ

</details>

---

*ต่อไป: Ch04 — Locators & Selectors (วิธีหา element บนหน้าเว็บที่ robust และไม่ brittle)*
