## ก่อนอ่านบทนี้ ลองตอบ:

1. `test.slow()` ต่างจาก `test.fixme()` อย่างไร — ทั้งสองตัวเมื่อรันแล้วเกิดอะไรขึ้น?
2. ถ้าต้องการรัน test เฉพาะกลุ่ม `@smoke` ด้วย `--grep` แต่ยกเว้น `@slow` ด้วย ควรเขียน CLI command อย่างไร?

---

เฉลย:

1. `test.slow()` — **รัน test ตามปกติ** แต่ขยาย timeout เป็น 3 เท่า (ใช้เมื่อ test ต้องการเวลานาน แต่ยังทำงานได้ถูกต้อง) | `test.fixme()` — **ไม่รัน test เลย** เหมือน skip แต่มีความหมายว่า "มีแผนจะแก้" ต่างจาก `test.skip()` ที่ไม่มีแผนชัดเจน
2. `npx playwright test --grep "@smoke" --grep-invert "@slow"` — `--grep` filter เฉพาะ pattern ที่ match, `--grep-invert` ยกเว้น pattern ที่ match

---

# บทที่ 10: Configuration & Projects — ปรับ Playwright ให้ทำงานตาม Environment

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- เข้าใจโครงสร้างของ `playwright.config.ts` และอธิบายทุก option หลักได้
- ใช้ **Projects** เพื่อรัน test suite เดียวกันบนหลาย browser หรือหลาย environment พร้อมกัน
- ตั้งค่า **timeout ทั้ง 7 ประเภท** ได้อย่างถูกต้อง รู้ว่าแต่ละตัวควบคุมอะไร
- ใช้ `webServer` เพื่อให้ Playwright start development server อัตโนมัติก่อนรัน test
- ใช้ **Project Dependencies** แทน `globalSetup` แบบเก่า เพื่อทำ authentication setup
- หลีกเลี่ยง common mistakes เช่น hardcode URL หรือตั้ง `trace: 'on'` ใน production CI

---

## 2. ทำไมต้องรู้? (Why)

ลองนึกถึงชีวิตก่อนมี config ที่ดี:

- ทุก test file เขียน `page.goto('http://localhost:3000/login')` แบบ hardcode — แล้ววันนึง URL เปลี่ยนเป็น `staging.company.com` ต้องไปแก้ทุกไฟล์
- CI pipeline ล้มเหลวเพราะ dev ลืม comment `test.only` ออก — แต่ไม่มี guard ป้องกัน
- test รันบน Chrome เครื่องตัวเองผ่าน แต่ Firefox ที่ CI กลับ fail — ทั้งที่ควรรันทั้งสองตั้งแต่แรก
- Dev server ต้องรันก่อนด้วยมือในอีก terminal — ถ้าลืม test เปิด URL ไม่ได้

`playwright.config.ts` แก้ปัญหาเหล่านี้ทั้งหมดในที่เดียว: centralizing configuration แทนการ scatter ตาม test files

---

## 3. Analogy

**`playwright.config.ts` เหมือน ระเบียบการสอบ** ที่ทางมหาวิทยาลัยออกให้นักศึกษาทุกคน

ระเบียบการสอบกำหนดว่า: ทุกห้องสอบต้องเริ่มพร้อมกัน (timeout เท่ากัน), ห้ามใช้ตำรา (forbidOnly), ถ้าเขียนผิดแก้ได้ 2 ครั้ง (retries: 2), กระดาษคำตอบมี format เดียวกัน (reporter) — นักศึกษาไม่ต้องถามกฎซ้ำในทุกห้อง เพราะกฎถูกบังคับใช้โดยอัตโนมัติ

**Projects** เหมือนการจัดสอบให้นักศึกษา **3 หลักสูตรที่แตกต่างกัน** ด้วย **ข้อสอบชุดเดียวกัน** — สอบปกติ, สอบภาษาอังกฤษ, สอบ international program ต่างใช้ข้อสอบเดิม แต่บริบท (browser/environment) ต่างกัน

⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:
- **Config เป็นแค่ "setting ภายนอก" ที่ไม่กระทบ logic** — จริงๆ config กำหนด behavior ของ test lifecycle ด้วย: timeout ทำให้ test หยุดกลางคัน, retries ทำให้ test รัน 3 รอบ, parallel ทำให้ tests รันพร้อมกัน ทั้งหมดนี้เปลี่ยน behavior จริง ไม่ใช่แค่ appearance
- **Projects คือ test files ที่แยกกัน** — จริงๆ Projects คือ **configurations ที่ต่างกัน** ที่รัน **test files ชุดเดียวกัน** ซ้ำ Project `chromium` กับ `firefox` รัน `tests/login.spec.ts` ไฟล์เดียวกัน แค่ browser ต่างกัน

---

## 4. เนื้อหาหลัก

### `defineConfig()` — Blueprint ของ Test Suite

```typescript
// playwright.config.ts
// tested: Playwright v1.50+, Node.js 20+
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
});
```

มาแกะทีละ option ให้ชัดขึ้น:

**`fullyParallel` vs `workers` — สองสิ่งที่สับสนบ่อยที่สุด**

`workers` คือจำนวน **parallel processes** ที่ Playwright spawns — เหมือนจำนวน "คนงาน" ที่ทำงานพร้อมกัน

`fullyParallel` คือ "ทุก test ในทุก file รัน parallel ได้หรือไม่" — ถ้า `fullyParallel: false` (default) แต่ละ file รันทีละ test ตามลำดับ (sequential ภายใน file) แต่ files ต่างกันรัน parallel กันได้ ถ้า `fullyParallel: true` ทุก test ในทุก file รัน parallel พร้อมกันได้

```
workers: 4, fullyParallel: false → 4 files รันพร้อมกัน, แต่ละ file รัน tests ทีละตัว
workers: 4, fullyParallel: true  → tests ทั้งหมดรัน parallel สูงสุดพร้อมกัน 4 test
```

**`forbidOnly` — Guard สำหรับ CI**

ถ้า dev ลืม `test.only` ทิ้งไว้ใน code แล้ว push ขึ้น CI — Playwright จะรันแค่ test นั้นเดียว ทั้ง suite ผ่านเพราะรันแค่ test เดียว! `forbidOnly: !!process.env.CI` บังคับให้ exit error ถ้าเจอ `.only` ใน CI

**`retries` — Best Practice สำหรับ CI**

```typescript
retries: process.env.CI ? 2 : 0
```

ทำไม `2` ใน CI แต่ `0` ใน local? เพราะ:
- **CI environment** มี network latency, resource contention, cold start — บางครั้ง test fail เพราะ infra ไม่ใช่ code retry 2 ครั้งช่วย catch flakiness จริงๆ
- **Local development** ถ้า test fail แล้ว retry pass ทันที = ปัญหาถูกซ่อน ดีกว่า fail ทันทีให้ dev แก้

หมายเหตุ: ตั้งแต่ Playwright v1.52+ มี `failOnFlakyTests: !!process.env.CI` ที่ทำให้ CI exit error ถ้า test pass เพราะ retry (แสดงว่า test นั้น flaky) — เป็น option เสริมที่ combine ได้กับ `retries`

---

### Projects — รัน Test Suite เดียวบนหลาย Browser

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
    // Mobile emulation
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
    {
      name: 'Mobile Safari',
      use: { ...devices['iPhone 12'] },
    },
  ],
});
```

`devices['Desktop Chrome']` เป็น preset ที่ Playwright จัดมาให้ ครอบคลุม viewport, userAgent, browser engine ที่ถูกต้องสำหรับแต่ละ device — ไม่ต้องตั้งค่าเอง

**รัน project เฉพาะ:**

```bash
npx playwright test --project=chromium
npx playwright test --project=firefox --project=webkit
```

**Project Dependencies — Modern Global Setup**

แทนที่จะใช้ `globalSetup` แบบเก่า (ที่ไม่ขึ้น HTML report, ไม่รองรับ trace) ให้ใช้ setup project แทน:

```typescript
// tested: Playwright v1.50+, Node.js 20+
export default defineConfig({
  projects: [
    {
      name: 'setup',
      testMatch: /global\.setup\.ts/,   // รัน setup ก่อน
    },
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
      dependencies: ['setup'],           // รอให้ setup ผ่านก่อน
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
      dependencies: ['setup'],
    },
  ],
});
```

```typescript
// tests/global.setup.ts
import { test as setup } from '@playwright/test';

setup('authenticate admin user', async ({ page }) => {
  await page.goto('/login');
  await page.fill('[data-testid="input-username"]', 'admin');
  await page.fill('[data-testid="input-password"]', process.env.ADMIN_PASSWORD!);
  await page.click('[data-testid="btn-login"]');
  // บันทึก session state ให้ tests ใช้
  await page.context().storageState({ path: 'playwright/.auth/admin.json' });
});
```

ข้อดีของ Project Dependencies เทียบ `globalSetup` เก่า:
- Setup ปรากฏใน HTML report แยกต่างหาก ดู debug ได้ง่าย
- Trace recording ทำงานได้ใน setup phase
- ใช้ fixtures ได้ (เช่น `page`, `request`) ไม่ต้องสร้าง browser เอง

---

### Timeout — 7 ประเภทที่ต้องรู้

Playwright แยก timeout ละเอียดกว่า testing framework อื่น ทำให้ debug ง่ายขึ้นมาก เพราะรู้ว่า timeout ไหนเกิดที่ขั้นตอนไหน:

| Timeout | Default | ตั้งค่าที่ | ควบคุมอะไร |
|---------|---------|-----------|------------|
| **test timeout** | 30,000 ms | `timeout` ใน config หรือ `test.setTimeout()` | ทุกอย่างใน test รวม fixtures และ beforeEach |
| **expect timeout** | 5,000 ms | `expect: { timeout }` ใน config หรือ per-assertion `{ timeout }` | auto-retrying assertions |
| **action timeout** | ไม่มี default | `use: { actionTimeout }` ใน config หรือ per-action `{ timeout }` | แต่ละ action เช่น `click()`, `fill()` |
| **navigation timeout** | ไม่มี default | `use: { navigationTimeout }` ใน config หรือ per-nav `{ timeout }` | `page.goto()`, `page.waitForNavigation()` |
| **global timeout** | ไม่มี default | `globalTimeout` ใน config | ทุก test รวมกัน (ทั้ง test run) |
| **fixture timeout** | ใช้ test timeout | `{ timeout }` ใน fixture definition | การ setup/teardown ของ fixture นั้นๆ |
| **beforeAll/afterAll timeout** | 30,000 ms | `test.setTimeout()` ภายใน hook | การรัน hook นั้น |

การตั้งค่าใน config:

```typescript
// tested: Playwright v1.50+, Node.js 20+
export default defineConfig({
  timeout: 60_000,          // test timeout: 60 วิ
  globalTimeout: 3_600_000, // ทั้ง run ต้องจบใน 1 ชั่วโมง
  expect: {
    timeout: 10_000,        // assertions retry ได้ถึง 10 วิ
  },
  use: {
    actionTimeout: 15_000,     // click/fill ต้องเสร็จใน 15 วิ
    navigationTimeout: 30_000, // goto() ต้องเสร็จใน 30 วิ
  },
});
```

Override เฉพาะ test:

```typescript
test('test ที่ต้องใช้เวลานาน', async ({ page }) => {
  test.setTimeout(120_000); // override เฉพาะ test นี้
  // ...
});

test('test ที่ใช้ test.slow()', async ({ page }) => {
  test.slow(); // ขยาย timeout เป็น 3 เท่าของ config timeout
  // ...
});
```

---

### `webServer` — Auto-start Development Server

```typescript
// tested: Playwright v1.50+, Node.js 20+
export default defineConfig({
  webServer: {
    command: 'cd playwright-course-app && npm start',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,   // รอ server start ได้ถึง 2 นาที (default 60 วิ)
    stdout: 'pipe',     // แสดง server logs ใน terminal
    stderr: 'pipe',
  },
});
```

`reuseExistingServer: !process.env.CI` หมายความว่า:
- **Local**: ถ้ามี server รันอยู่แล้ว ใช้ต่อได้เลย ไม่ต้อง start ใหม่ (ประหยัดเวลา)
- **CI**: ต้อง start server ใหม่ทุกครั้ง ห้าม reuse (เพื่อ clean state)

---

### `baseURL` + Environment Variables

```typescript
// playwright.config.ts
// tested: Playwright v1.50+, Node.js 20+
import 'dotenv/config';

export default defineConfig({
  use: {
    baseURL: process.env.BASE_URL ?? 'http://localhost:3000',
  },
});
```

```typescript
// test file — ใช้ relative path ได้เลย
test('login page loads', async ({ page }) => {
  await page.goto('/login');          // ไปที่ baseURL + /login
  await page.goto('/');               // ไปที่ baseURL
});
```

สร้าง `.env` สำหรับ local และ `.env.staging` สำหรับ staging:

```bash
# .env (local)
BASE_URL=http://localhost:3000

# .env.staging (staging)
BASE_URL=https://staging.company.com
```

---

### `tsconfig` — Custom TypeScript Config สำหรับ Tests

Playwright v1.49+ รองรับ `tsconfig` property ใน config เพื่อระบุ tsconfig ที่ใช้กับ test files ทั้งหมด:

```typescript
export default defineConfig({
  tsconfig: './tsconfig.test.json',  // ระบุ tsconfig เฉพาะสำหรับ tests
});
```

ถ้าไม่ระบุ Playwright จะ auto-detect `tsconfig.json` ที่ใกล้ที่สุดสำหรับแต่ละ imported file แยกกัน property นี้มีประโยชน์เมื่อต้องการ tsconfig เดียวที่ apply ทั้ง test suite

---

### เปรียบเทียบกับ Robot Framework + Selenium

| ฟีเจอร์ | Robot Framework + Selenium | Playwright |
|--------|--------------------------|------------|
| Config file | `robot.yaml`, environment variables | `playwright.config.ts` (TypeScript, type-safe) |
| Multi-browser | แยก process, รัน suite ซ้ำหลายรอบ | Projects ใน config เดียว, รันพร้อมกัน |
| Timeout | `Set Global Timeout` keyword, ระดับเดียว | 7 timeout types, granular ต่างระดับ |
| Dev server | รันแยกด้วยมือใน terminal | `webServer` auto-start, Playwright จัดการให้ |
| Retry | `--retries` CLI flag เท่านั้น | `retries` ใน config + override ต่อ test ได้ |
| Global setup | ไม่มี built-in (ใช้ Suite Setup) | Project Dependencies (modern) หรือ `globalSetup` (legacy) |
| Environment config | robot.yaml + override ด้วย CLI | `defineConfig()` + `process.env` + dotenv |

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner: Minimal Config ที่ใช้งานได้จริง

สถานการณ์: เพิ่งเริ่มต้นโปรเจค ต้องการ config ที่ทำงานได้ทันที รัน tests บน Chrome เดียว

```typescript
// playwright.config.ts
// tested: Playwright v1.50+, Node.js 20+
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: {
    command: 'npm start',
    url: 'http://localhost:3000',
    reuseExistingServer: true,
  },
});
```

```typescript
// tests/home.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

test('home page loads', async ({ page }) => {
  await page.goto('/');                    // ใช้ baseURL — ไม่ต้อง hardcode
  await expect(page).toHaveTitle(/Playwright Course App/);
});

test('login page accessible', async ({ page }) => {
  await page.goto('/login');               // relative path เสมอ
  await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible();
});
```

ผลลัพธ์เมื่อรัน `npx playwright test`:
```
Running 2 tests using 1 worker
  ✓ home page loads (543ms)
  ✓ login page accessible (312ms)

  2 passed (2.1s)
```

---

### Intermediate: Multi-Project Config ที่มี Setup Dependencies

สถานการณ์: ทีม QA ต้องรัน test บน 3 browsers พร้อมกัน แต่ทุก test ต้องการ admin session ก่อน — ใช้ setup project เพื่อทำ authentication ครั้งเดียว แล้วทุก browser project ใช้ auth state ร่วมกัน

```typescript
// playwright.config.ts
// tested: Playwright v1.50+, Node.js 20+
import { defineConfig, devices } from '@playwright/test';
import 'dotenv/config';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 2 : undefined,
  reporter: process.env.CI ? 'github' : 'html',

  use: {
    baseURL: process.env.BASE_URL ?? 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },

  projects: [
    // ── Setup project: รัน auth ก่อน ──────────────────────────────
    {
      name: 'setup',
      testMatch: /global\.setup\.ts/,
    },

    // ── Browser projects: รอ setup ผ่านก่อน ───────────────────────
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'playwright/.auth/admin.json',  // ใช้ auth state จาก setup
      },
      dependencies: ['setup'],
    },
    {
      name: 'firefox',
      use: {
        ...devices['Desktop Firefox'],
        storageState: 'playwright/.auth/admin.json',
      },
      dependencies: ['setup'],
    },
    {
      name: 'webkit',
      use: {
        ...devices['Desktop Safari'],
        storageState: 'playwright/.auth/admin.json',
      },
      dependencies: ['setup'],
    },
  ],

  webServer: {
    command: 'cd playwright-course-app && npm start',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
  },
});
```

```typescript
// tests/global.setup.ts
// tested: Playwright v1.50+, Node.js 20+
import { test as setup, expect } from '@playwright/test';
import path from 'path';

const authFile = path.join(__dirname, '../playwright/.auth/admin.json');

setup('authenticate as admin', async ({ page }) => {
  await page.goto('/login');
  await page.fill('[data-testid="input-username"]', 'admin');
  await page.fill('[data-testid="input-password"]', process.env.ADMIN_PASSWORD ?? 'admin123');
  await page.click('[data-testid="btn-login"]');

  // ยืนยันว่า login สำเร็จก่อน save state
  await expect(page.getByTestId('session-badge')).toBeVisible();

  // บันทึก cookies + localStorage ให้ tests ใช้ต่อ
  await page.context().storageState({ path: authFile });
});
```

```typescript
// tests/admin-dashboard.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

// test นี้ได้ storageState จาก project config — ไม่ต้อง login ใหม่
test('admin can see dashboard metrics', async ({ page }) => {
  await page.goto('/admin');
  await expect(page.getByRole('heading', { name: 'Admin Dashboard' })).toBeVisible();
  await expect(page.getByTestId('metric-total-users')).toBeVisible();
});
```

เมื่อรัน `npx playwright test`:
```
Running 1 test using 1 worker
  ✓ [setup] › authenticate as admin (1.2s)

Running 3 tests using 3 workers
  ✓ [chromium] › admin-dashboard.spec.ts › admin can see dashboard metrics (543ms)
  ✓ [firefox]  › admin-dashboard.spec.ts › admin can see dashboard metrics (621ms)
  ✓ [webkit]   › admin-dashboard.spec.ts › admin can see dashboard metrics (498ms)

  4 passed (3.8s)
```

---

### Advanced: Diagnose Config ที่ทำให้ Test Flaky

สถานการณ์: CI report แสดงว่า test ที่ `tests/checkout.spec.ts` fail บน Firefox เป็นบางครั้ง error message คือ `Test timeout of 30000ms exceeded` แต่ local รันผ่านทุกครั้ง ต้องหาต้นเหตุและแก้ config

```typescript
// playwright.config.ts (PROBLEMATIC — มีปัญหา 3 จุด)
// tested: Playwright v1.50+, Node.js 20+
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  timeout: 30_000,           // ❌ จุดที่ 1: สำหรับ checkout flow ที่มี payment API นี้น้อยเกินไป
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on',             // ❌ จุดที่ 2: trace: 'on' ทุก test ทำให้ CI ช้า เพิ่ม overhead
    actionTimeout: 0,        // ❌ จุดที่ 3: ไม่มี action timeout = action ค้างได้ไม่จำกัด
  },
  projects: [
    { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
  ],
  // ❌ ไม่มี retries ใน CI — ถ้า Firefox CI เจอ flakiness จะ fail ทันที
});
```

การวิเคราะห์:

1. **`timeout: 30_000` สำหรับ checkout** — checkout flow มี payment API call ที่อาจใช้เวลา 10-15 วินาที + assertion timeout 5 วินาที + navigation 5 วินาที รวมกันเกิน 30 วินาทีได้ใน CI ที่ช้ากว่า local
2. **`trace: 'on'`** — บันทึก trace ทุก test เพิ่ม I/O overhead ทำให้ทุก test ช้าขึ้น และ checkout ที่ใกล้ limit อยู่แล้วก็ timeout
3. **`actionTimeout: 0`** — ไม่มี action timeout คือรอไม่จำกัด แต่ขัดกับ test timeout 30 วินาที ถ้า action ค้างจะรอจนหมด test timeout แล้วค่อย fail — ทำให้ error message ดูเหมือน test timeout แต่จริงๆ เป็น action ค้าง

```typescript
// playwright.config.ts (FIXED)
// tested: Playwright v1.50+, Node.js 20+
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  timeout: 60_000,           // ✅ เพิ่มเป็น 60 วินาที รองรับ payment API ที่ช้า
  retries: process.env.CI ? 2 : 0,  // ✅ retry ใน CI จัดการ flakiness

  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',  // ✅ บันทึกเฉพาะตอน retry — ไม่เปลือง I/O
    actionTimeout: 15_000,    // ✅ action ต้องเสร็จใน 15 วิ ถ้าไม่เสร็จ = มีปัญหาแน่นอน
    navigationTimeout: 30_000,
  },

  projects: [
    { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
  ],
});
```

```typescript
// tests/checkout.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

// checkout flow ที่ใช้เวลานาน — override timeout เฉพาะ test นี้
test('complete checkout with payment', async ({ page }) => {
  test.setTimeout(90_000); // override เฉพาะ test นี้ถ้าจำเป็น

  await page.goto('/cart');
  await page.click('[data-testid="btn-checkout"]');

  // payment API อาจช้า — expect timeout จะ retry assertion ถึง 10 วินาที
  await expect(page.getByTestId('payment-status')).toContainText('Success', {
    timeout: 20_000,  // per-assertion override สำหรับ step นี้โดยเฉพาะ
  });
});
```

ผลลัพธ์หลังแก้:
```
Running 1 test using 1 worker
  ✓ [firefox] › checkout.spec.ts › complete checkout with payment (12.3s)

  1 passed (12.3s)
```

---

## 6. Common Mistakes

❌ **Hardcode URL ใน test files แทนใช้ `baseURL`**
```typescript
// ❌ ผิด — แก้ URL ทีต้องไปแก้ทุกไฟล์
await page.goto('http://localhost:3000/login');
await page.goto('http://localhost:3000/dashboard');
```
```typescript
// ✅ ถูก — ตั้ง baseURL ใน config ครั้งเดียว
// playwright.config.ts
use: { baseURL: 'http://localhost:3000' }

// test file
await page.goto('/login');       // relative path เสมอ
await page.goto('/dashboard');
```
*(source: https://playwright.dev/docs/test-configuration)*

---

❌ **ใช้ `trace: 'on'` ใน config สำหรับ CI**
```typescript
// ❌ ผิด — trace ทุก test เพิ่ม disk I/O และ memory ทำให้ CI ช้าลงมาก
use: { trace: 'on' }
```
```typescript
// ✅ ถูก — บันทึก trace เฉพาะตอน retry ครั้งแรก (เพียงพอสำหรับ debug)
use: { trace: 'on-first-retry' }
```
*(source: https://playwright.dev/docs/trace-viewer)*

---

❌ **ไม่ตั้ง `actionTimeout` ทำให้ debug ยาก**
```typescript
// ❌ ผิด — ถ้า action ค้าง จะรอจนหมด test timeout แล้วได้ error message กว้างๆ
use: {
  // ไม่มี actionTimeout
}
```
```typescript
// ✅ ถูก — ตั้ง actionTimeout ทำให้รู้ทันทีว่า action ไหน timeout
use: {
  actionTimeout: 15_000,     // click/fill ต้องเสร็จใน 15 วิ
  navigationTimeout: 30_000, // goto() ต้องเสร็จใน 30 วิ
}
```
*(source: https://playwright.dev/docs/test-timeouts)*

---

❌ **ใส่ generic type ใน `defineConfig<PlaywrightTestConfig>()`**
```typescript
// ❌ ผิด — ไม่จำเป็นและทำให้ code verbose
import { defineConfig } from '@playwright/test';
import type { PlaywrightTestConfig } from '@playwright/test';
export default defineConfig<PlaywrightTestConfig>({ ... });
```
```typescript
// ✅ ถูก — defineConfig() infer types ให้อัตโนมัติ
import { defineConfig } from '@playwright/test';
export default defineConfig({ ... });
```
*(source: https://playwright.dev/docs/test-configuration)*

---

❌ **ใช้ `globalSetup` แบบเก่าสำหรับ authentication**
```typescript
// ❌ เก่า — setup ไม่ขึ้น HTML report, ไม่มี trace, ต้องสร้าง browser เอง
export default defineConfig({
  globalSetup: require.resolve('./global-setup'),
});
```
```typescript
// ✅ Modern — ใช้ Project Dependencies แทน
export default defineConfig({
  projects: [
    { name: 'setup', testMatch: /global\.setup\.ts/ },
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
      dependencies: ['setup'],
    },
  ],
});
```
*(source: https://playwright.dev/docs/test-global-setup-teardown)*

---

## 7. สรุปบท

ลองตอบคำถามต่อไปนี้ก่อนดูเฉลย — เพื่อ consolidate ความเข้าใจ:

**คำถาม 1:** ถ้า config มี `timeout: 30_000` และ `actionTimeout: 15_000` แล้ว action หนึ่งใช้เวลา 20 วินาที — อะไรจะ timeout ก่อน และ error message จะบอกว่าอะไร?

**คำถาม 2:** ทีมมี tests 200 ไฟล์ และต้องการรันบน Chrome, Firefox, WebKit พร้อมกัน แต่มี setup project สำหรับ authentication ด้วย — เมื่อรัน `npx playwright test` Playwright จะรัน test กี่ครั้งโดยรวม (ไม่รวม setup)?

**คำถาม 3:** `reuseExistingServer: !process.env.CI` ทำงานอย่างไรใน local vs CI — อธิบายพฤติกรรมและเหตุผลที่แตกต่างกัน

---

เฉลย:

**คำถาม 1:** `actionTimeout: 15_000` จะ timeout ก่อน เพราะ action ใช้เวลา 20 วิ > 15 วิ Error message จะบอกว่า "action timeout of 15000ms exceeded" — ซึ่งบอกชัดกว่า "test timeout" ว่าปัญหาอยู่ที่ action นั้นโดยเฉพาะ ทำให้ debug ง่ายขึ้น

**คำถาม 2:** 200 ไฟล์ × 3 projects = **600 ครั้ง** (ไม่รวม setup project ที่รัน 1 ครั้งก่อน)

**คำถาม 3:**
- **Local** (`!process.env.CI` = `true`): ถ้ามี server รันอยู่ที่ port 3000 แล้ว Playwright ใช้ต่อได้เลย ไม่ start ใหม่ ประหยัดเวลา dev ที่ต้องรัน test บ่อยๆ
- **CI** (`!process.env.CI` = `false`): บังคับ start server ใหม่ทุกครั้ง เพื่อ guarantee clean state ป้องกัน test pass เพราะ state จาก run ก่อนหน้า

---

## 8. Pre-chapter Retrieval สำหรับบทถัดไป

บทที่ 11 จะพูดถึง **API Testing** กับ `request` fixture และ `APIRequestContext` — ลองนึกดูก่อน:

- ถ้าต้องการ test REST API endpoint โดยไม่เปิด browser เลย Playwright ทำได้ไหม อย่างไร?
- `request` fixture ที่ Playwright จัดมาให้ต่างจากการใช้ `fetch()` ปกติอย่างไร?
