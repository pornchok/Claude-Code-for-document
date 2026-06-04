## ก่อนอ่านบทนี้ ลองตอบ:

1. คุณมี test suite 80 tests ที่ใช้ `page.route()` mock `/api/products` ทุก test แต่บาง test ต้องการ response 200 และบาง test ต้องการ response 404 — คุณจะ structure code อย่างไร เพื่อไม่ให้ route ของ test หนึ่งรบกวน test อื่น?

2. เพื่อนคุณเขียน `await page.clock.install({ time: new Date('2030-01-01') })` ไว้หลัง `await page.goto('http://localhost:3000')` แล้วบ่นว่า "Clock ไม่ทำงาน" — ปัญหาคืออะไร และควรแก้อย่างไร?

---

เฉลย:

1. ใช้ `page.route()` ใน test แต่ละตัวแยกกัน (route ใน `beforeEach` จะ override ไม่ได้ง่ายๆ) หรือใช้ `page.unroute()` ก่อน register route ใหม่ใน test ที่ต้องการ behavior ต่าง — route ที่ register ทีหลังจะ take precedence แต่ที่ดีกว่าคือ register เฉพาะใน test ที่ต้องการ แทนที่จะ register แบบ global แล้วมา override ทีหลัง

2. ปัญหา: `clock.install()` ต้องเรียก **ก่อน** `page.goto()` เสมอ เพราะ page scripts รัน scripts ทันทีที่ load — ถ้า install หลัง goto หมายความว่า `Date.now()` ที่ scripts ใช้ตอน initialize ยังเป็นเวลาจริง แก้โดย: เรียก `clock.install()` ก่อน `goto()` เสมอ

---

# บทที่ 13: Authentication & Storage State

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- อธิบายได้ว่า storageState คืออะไร และ capture อะไรบ้าง (cookies, localStorage, IndexedDB) — รวมถึงสิ่งที่ **ไม่** capture โดยอัตโนมัติ (sessionStorage)
- สร้าง `auth.setup.ts` ที่ login ครั้งเดียวแล้ว save state ลงไฟล์ เพื่อให้ทุก test ใช้ซ้ำได้
- ตั้งค่า project dependencies ใน `playwright.config.ts` เพื่อให้ setup รันก่อน test project อัตโนมัติ
- จัดการ multiple roles (admin, user) ใน config เดียวโดยไม่ต้อง login ซ้ำทุก test
- ใช้ API-based login แทน UI login เพื่อความเร็ว
- เข้าใจ per-worker auth isolation สำหรับ parallel tests ที่ modify server-side state
- หลีกเลี่ยง common mistakes เช่น commit auth files ไป git หรือใช้ storageState เดียวสำหรับ parallel tests ที่ conflict กัน

---

## 2. ทำไมต้องรู้? (Why)

ลองนึกดูว่า test suite ของคุณมี 120 tests และ 100 tests ในนั้นต้อง login ก่อน ถ้าแต่ละ test login ผ่าน UI (fill username, fill password, click, wait for redirect) ใช้เวลาประมาณ 3-5 วินาทีต่อ test — นั่นคือ **5-8 นาทีเปล่าๆ** แค่สำหรับ login

ยิ่งกว่านั้น ถ้ามีหลาย roles เช่น admin เห็น dashboard, user เห็นแค่ profile — การทดสอบ role-based access ใน Robot Framework แบบเดิมต้องสร้าง test suite แยก setup variables ซับซ้อน หรือ login ใหม่ทุกครั้ง

Playwright แก้ทั้งสองปัญหานี้ด้วย **storageState**:

1. Login **ครั้งเดียว** บันทึก browser state ลงไฟล์ JSON
2. Tests ทุกตัวที่ตามมา **อ่านไฟล์นั้น** แทนการ login ใหม่ — fast เพราะข้ามทั้ง UI interaction และ network roundtrip ของ login
3. Multi-role จัดการใน config เดียว แยกเป็น projects

ผลลัพธ์จริง: suite 100 tests ที่ต้อง login ลดเวลา ~400-500 วินาทีเหลือ **แค่ครั้งเดียว** ต่อ run

---

## 3. Analogy

**storageState เหมือน stamp ที่ร้านประทับให้คุณที่มือหลังจ่ายค่าเข้า** — แทนที่จะต้องแสดงบัตรประชาชนและซื้อตั๋วใหม่ทุกครั้งที่เข้า-ออกห้อง แค่โชว์ stamp ที่ข้อมือก็เข้าได้ทันที

ใน Playwright: "ซื้อตั๋วครั้งแรก" = login ผ่าน UI ปกติ, "stamp ที่มือ" = ไฟล์ `user.json` ที่เก็บ cookies + localStorage, "โชว์ stamp" = `use: { storageState: 'playwright/.auth/user.json' }` ใน config

⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:

- **storageState capture ทุก state ของ browser** — จริงๆ capture **cookies, localStorage, และ IndexedDB** (ต้องเปิดใช้ด้วย `{ indexedDB: true }`) แต่ **ไม่ capture sessionStorage** โดยอัตโนมัติ เพราะ sessionStorage ผูกกับ session และ tab เดียว ถ้า app ใช้ sessionStorage เก็บ auth token จะต้องใช้ `page.evaluate()` + `addInitScript()` inject ค่าเองแยกต่างหาก
- **storageState ใช้ได้ข้าม domains ทุกอย่าง** — sessionStorage ผูกกับ origin (domain + protocol + port) และ session เดียว ถ้า navigate ไป domain อื่นหรือเปิด tab ใหม่ sessionStorage จะว่างเปล่า — cookies ถึงจะข้าม page navigation ได้ แต่ก็ผูกกับ domain configuration ที่กำหนดไว้
- **stamp ไม่มีวันหมดอายุ** — จริงๆ cookies มี expiration, JWT tokens มี expiry — storageState ที่ save ไว้อาจ expire ได้ ต้อง re-generate เป็นระยะ

---

## 4. เนื้อหาหลัก

### 4.1 storageState คืออะไร

`storageState` คือ snapshot ของ browser state ณ เวลาที่เรียก — เก็บ:

- **Cookies** ทั้งหมดที่ browser มี ณ ขณะนั้น
- **localStorage** ของทุก origin ที่ page เยือน
- **IndexedDB** ของทุก origin (ต้องระบุ `{ indexedDB: true }` ตอนเรียก `storageState()`)

**sessionStorage ไม่ถูก capture** เพราะผูกกับ tab และ session เดียว ไม่สามารถ serialize ข้ามหน้าหรือ context ใหม่ได้อย่างมีความหมาย

การใช้งานพื้นฐาน: หลัง login สำเร็จ เรียก:

```typescript
await page.context().storageState({ path: 'playwright/.auth/user.json' });
```

ไฟล์ JSON ที่ได้หน้าตาประมาณนี้:

```json
{
  "cookies": [
    { "name": "session", "value": "abc123...", "domain": "localhost", "path": "/", ... }
  ],
  "origins": [
    {
      "origin": "http://localhost:3000",
      "localStorage": [
        { "name": "authToken", "value": "eyJhbGciOi..." }
      ]
    }
  ]
}
```

จากนั้น test project อื่นๆ สามารถใช้ state นี้ได้โดยระบุใน config:

```typescript
use: { storageState: 'playwright/.auth/user.json' }
```

Playwright จะ inject cookies และ localStorage ก่อนที่ test จะ navigate ไปหน้าใดๆ — ผลคือ browser "เป็นเหมือนว่า login แล้ว" ตั้งแต่ต้น

---

### 4.2 Setup ด้วย Project Dependencies (Modern Approach)

วิธีที่ Playwright แนะนำปัจจุบันคือใช้ **setup project** ที่รันก่อน test project ด้วย `dependencies`:

**โครงสร้างไฟล์:**

```
tests/
├── auth.setup.ts          ← รัน login และ save storageState
├── example.spec.ts        ← test ที่ใช้ authenticated state
playwright/
└── .auth/
    └── user.json          ← generated (อย่า commit!)
playwright.config.ts
.gitignore
```

**playwright.config.ts:**

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  projects: [
    // Setup project — รันก่อน project อื่น
    {
      name: 'setup',
      testMatch: /.*\.setup\.ts/,
    },
    // Test project — รัน หลัง setup เสร็จ
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'playwright/.auth/user.json',
      },
      dependencies: ['setup'],  // ← รอให้ 'setup' เสร็จก่อน
    },
  ],
});
```

**tests/auth.setup.ts:**

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test as setup, expect } from '@playwright/test';

const authFile = 'playwright/.auth/user.json';

setup('authenticate', async ({ page }) => {
  // Login ผ่าน UI ตามปกติ
  await page.goto('http://localhost:3000/login');
  await page.fill('[data-testid="input-username"]', 'testuser');
  await page.fill('[data-testid="input-password"]', 'test123');
  await page.click('[data-testid="btn-login"]');

  // ตรวจว่า login สำเร็จก่อน save
  await expect(page.getByTestId('session-badge')).toContainText('testuser');

  // Save state ลงไฟล์
  await page.context().storageState({ path: authFile });
});
```

สิ่งที่ต้องเพิ่มใน `.gitignore`:

```
playwright/.auth/
```

เหตุผล: ไฟล์ auth มี cookies และ tokens จริง — ถ้า commit ไป git ทุกคนใน repo จะมีสิทธิ์ impersonate account นั้น

---

### 4.3 Multiple Roles (Admin + User)

สถานการณ์จริงที่พบบ่อย: app มี admin ที่เห็น `/admin` dashboard และ user ทั่วไปที่เข้าไม่ได้ — ต้อง test ทั้งสอง role

**โครงสร้างไฟล์:**

```
tests/
├── admin.setup.ts
├── user.setup.ts
├── admin-dashboard.spec.ts
└── user-profile.spec.ts
playwright/
└── .auth/
    ├── admin.json
    └── user.json
```

**playwright.config.ts สำหรับ multi-role:**

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  projects: [
    { name: 'setup admin', testMatch: /admin\.setup\.ts/ },
    { name: 'setup user', testMatch: /user\.setup\.ts/ },
    {
      name: 'Admin tests',
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'playwright/.auth/admin.json',
      },
      dependencies: ['setup admin'],
    },
    {
      name: 'User tests',
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'playwright/.auth/user.json',
      },
      dependencies: ['setup user'],
    },
  ],
});
```

**tests/admin.setup.ts:**

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test as setup, expect } from '@playwright/test';

setup('authenticate as admin', async ({ page }) => {
  await page.goto('http://localhost:3000/login');
  await page.fill('[data-testid="input-username"]', 'admin');
  await page.fill('[data-testid="input-password"]', 'admin123');
  await page.click('[data-testid="btn-login"]');
  await expect(page.getByTestId('session-badge')).toContainText('admin');
  await page.context().storageState({ path: 'playwright/.auth/admin.json' });
});
```

**tests/user.setup.ts:** เหมือนกัน แต่ใช้ `testuser/test123` และ save ไป `user.json`

ทดสอบ role-based access ใน spec ใช้ `test.use()`:

```typescript
// tests/admin-dashboard.spec.ts
import { test, expect } from '@playwright/test';

// ไม่ต้องประกาศ storageState ที่นี่
// เพราะ project 'Admin tests' กำหนดไว้ใน config แล้ว
test('admin เห็น admin panel', async ({ page }) => {
  await page.goto('http://localhost:3000/admin');
  await expect(page.getByText('Admin Dashboard')).toBeVisible();
});
```

หรือถ้าต้องการ override role ใน specific test ภายใน project เดียวกัน:

```typescript
// Switch role เฉพาะ describe block นี้
test.describe('user ไม่ควรเข้า admin', () => {
  test.use({ storageState: 'playwright/.auth/user.json' });

  test('GET /admin returns 403', async ({ page }) => {
    const response = await page.goto('http://localhost:3000/admin');
    expect(response?.status()).toBe(403);
  });
});
```

---

### 4.4 Login ผ่าน API (เร็วกว่า UI)

UI login ช้าเพราะต้อง render หน้า fill form รอ animation และรอ redirect เฉลี่ย 2-4 วินาที — ถ้า login endpoint เป็น REST API เรียกตรงๆ ได้เลย ใช้เวลาแค่ ~200-400ms

Demo app ของเรามี `POST /api/auth/login` ที่ return JWT token:

```typescript
// tests/auth.setup.ts — API-based version
// tested: Playwright v1.50+, Node.js 20+
import { test as setup } from '@playwright/test';

setup('authenticate via API', async ({ request, browser }) => {
  // Step 1: เรียก login API โดยตรง — ไม่ต้องเปิด browser
  const res = await request.post('http://localhost:3000/api/auth/login', {
    data: { username: 'admin', password: 'admin123' }
  });
  expect(res.ok()).toBeTruthy();
  const { token } = await res.json();

  // Step 2: สร้าง browser context พร้อม token ใน localStorage
  const context = await browser.newContext();
  const page = await context.newPage();

  // Inject token ก่อน navigate — ใช้ addInitScript
  await context.addInitScript((authToken: string) => {
    window.localStorage.setItem('authToken', authToken);
  }, token);

  // Step 3: Navigate เพื่อยืนยันว่า app รับ token แล้ว
  await page.goto('http://localhost:3000/');
  await page.waitForLoadState('networkidle');

  // Step 4: Save state
  await context.storageState({ path: 'playwright/.auth/admin.json' });
  await context.close();
});
```

ข้อควรรู้: API login approach เหมาะเมื่อ auth ใช้ localStorage-based token (JWT) — ถ้า auth ใช้ HttpOnly cookie เท่านั้น (cookie ที่ JS เข้าถึงไม่ได้) ต้องใช้ UI login หรือ inject cookie ด้วย `context.addCookies()`:

```typescript
// Inject cookie โดยตรง — สำหรับ server-side session cookie
await context.addCookies([{
  name: 'session_id',
  value: 'abc123xyz',
  domain: 'localhost',
  path: '/',
  httpOnly: true,
  secure: false,
  sameSite: 'Lax',
}]);
```

---

### 4.5 Per-Worker Auth (สำหรับ Parallel Tests ที่ Modify State)

ปัญหาที่เกิดขึ้นเมื่อ tests ทำงาน parallel และ **modify server-side state**: สมมติมี 4 workers แชร์ admin account เดียว แต่ test A ลบ order #1 ขณะที่ test B พยายาม update order #1 เดียวกัน — race condition

แนวทางที่ Playwright แนะนำคือ **worker-scoped custom fixture** — สร้าง auth state แยกต่อ worker โดยใช้ `parallelIndex` เป็น identifier:

> ข้อสำคัญ: `storageState` ใน `playwright.config.ts` **ไม่รับ function** — ต้องใช้ custom fixture แทน

**tests/fixtures/auth.ts:**

```typescript
// tests/fixtures/auth.ts
// tested: Playwright v1.50+, Node.js 20+
import { test as baseTest, expect } from '@playwright/test';
import fs from 'fs';
import path from 'path';

export * from '@playwright/test';

export const test = baseTest.extend<{}, { workerStorageState: string }>({
  // Override storageState ให้ใช้ workerStorageState ของ worker นั้นๆ
  storageState: ({ workerStorageState }, use) => use(workerStorageState),

  // Worker-scoped fixture — รัน 1 ครั้งต่อ worker ไม่ใช่ต่อ test
  workerStorageState: [async ({ browser }, use) => {
    const id = test.info().parallelIndex;
    const fileName = path.resolve(
      test.info().project.outputDir,
      `.auth/worker-${id}.json`
    );

    // ถ้ามีไฟล์แล้ว (จาก run ก่อนหน้า) ใช้ซ้ำได้เลย
    if (fs.existsSync(fileName)) {
      await use(fileName);
      return;
    }

    // ใน production: ควรมี accounts แยกต่อ worker (worker0@company.com, worker1@company.com)
    // เพื่อให้ server-side state ไม่ conflict กันจริงๆ
    // ตัวอย่างนี้ใช้ account เดียวกันแต่ save state แยก file
    const page = await browser.newPage({ storageState: undefined });
    await page.goto('http://localhost:3000/login');
    await page.fill('[data-testid="input-username"]', 'testuser');
    await page.fill('[data-testid="input-password"]', 'test123');
    await page.click('[data-testid="btn-login"]');
    await expect(page.getByTestId('session-badge')).toContainText('testuser');

    await page.context().storageState({ path: fileName });
    await page.close();

    await use(fileName);
  }, { scope: 'worker' }],
});
```

**tests/parallel-orders.spec.ts — import จาก fixtures แทน `@playwright/test`:**

```typescript
// tests/parallel-orders.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from './fixtures/auth';  // ← import จาก fixtures ไม่ใช่ @playwright/test

test('สร้าง order และ verify (isolated per worker)', async ({ page }) => {
  // page มี storageState ของ worker นี้โดยอัตโนมัติ — ไม่ต้องทำอะไรพิเศษ
  await page.goto('http://localhost:3000/');
  await expect(page.getByTestId('session-badge')).toContainText('testuser');
  // ...rest of test
});
```

`playwright.config.ts` ไม่ต้องเปลี่ยนอะไร — fixture จัดการ per-worker storageState ทั้งหมดเอง

---

### 4.6 Gotchas ที่ต้องรู้

**IndexedDB ต้องเปิดใช้ด้วย `{ indexedDB: true }`:**

ตั้งแต่ Playwright v1.45+ storageState รองรับ IndexedDB แล้ว แต่ต้องระบุ option ชัดเจน:

```typescript
// ✅ รวม IndexedDB ด้วย — เหมาะกับ PWA หรือ app ที่ใช้ Firebase Auth
await page.context().storageState({ path: authFile, indexedDB: true });
```

ถ้าเรียก `storageState()` โดยไม่ระบุ `indexedDB: true` (default behavior) IndexedDB จะไม่ถูก capture ทำให้ app ที่ใช้ IndexedDB เก็บ auth token เช่น Firebase Authentication จะพบว่า auth state หายไปเงียบๆ

**sessionStorage ไม่ถูก capture** (ทุกกรณี — ไม่มี option เปิดใช้) ถ้า app ใช้ sessionStorage เก็บ state สำคัญ ต้อง serialize + inject เอง:

```typescript
// Export sessionStorage ก่อน save state
const sessionData = await page.evaluate(() => {
  return JSON.stringify({ ...window.sessionStorage });
});

// Inject กลับใน test ใหม่ด้วย addInitScript
await page.context().addInitScript((data) => {
  const parsed = JSON.parse(data);
  Object.entries(parsed).forEach(([k, v]) => sessionStorage.setItem(k, v as string));
}, sessionData);
```

**Disable auth สำหรับ test บางตัว:**

บาง test ต้องการ test unauthenticated state โดยเฉพาะ (เช่น test login page ตัวเอง):

```typescript
test.use({ storageState: { cookies: [], origins: [] } });

test('login page แสดงผลถูกต้องสำหรับ unauthenticated user', async ({ page }) => {
  await page.goto('http://localhost:3000/login');
  await expect(page.getByTestId('btn-login')).toBeVisible();
});
```

---

### 4.7 เปรียบเทียบ RF/Selenium vs Playwright

| สถานการณ์ | Robot Framework + Selenium | Playwright |
|-----------|---------------------------|------------|
| Re-use auth | Login ทุก test (~3-5s/test) หรือ Session variable ซับซ้อน | storageState ใน config — login 1 ครั้งต่อ run |
| Multi-role testing | แยก test suites คนละไฟล์ มี global variables แยก | Multiple projects ใน `playwright.config.ts` ไฟล์เดียว |
| Auth setup ordering | `Suite Setup` keyword — ควบคุมยากเมื่อ suites ซับซ้อน | `dependencies: ['setup']` — explicit dependency graph |
| Speed (100 tests ต้อง login) | 100 × 4s = **400s** (~7 นาที) login overhead | 1-2 setups × 4s = **4-8s** ทั้ง suite |
| Parallel safety | ใช้ shared session → race condition หรือต้อง login ใหม่ทุก worker | Per-worker storage state ด้วย `parallelIndex` |
| Token expiry handling | ต้อง handle ใน test code เอง | UI Mode ไม่รัน setup อัตโนมัติ — กด run setup ใหม่เมื่อ expire |

---

## 5. ตัวอย่างโค้ด 3 ระดับ

### Beginner: Auth Setup + Project Dependency แบบสมบูรณ์

สถานการณ์: สร้าง authentication setup สำหรับ demo app ที่ login ด้วย `testuser/test123` และ verify ว่า protected pages เข้าถึงได้หลัง login

**playwright.config.ts:**

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  use: {
    baseURL: 'http://localhost:3000',
  },
  projects: [
    // 1. Setup project: รันก่อนทุกอย่าง
    {
      name: 'setup',
      testMatch: /.*\.setup\.ts/,
    },
    // 2. Test project: รันหลัง setup พร้อม authenticated state
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'playwright/.auth/user.json',
      },
      dependencies: ['setup'],
    },
  ],
});
```

**tests/auth.setup.ts:**

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test as setup, expect } from '@playwright/test';
import path from 'path';

const authFile = 'playwright/.auth/user.json';

setup('authenticate as testuser', async ({ page }) => {
  // Navigate ไปหน้า login
  await page.goto('/login');

  // กรอกข้อมูล
  await page.fill('[data-testid="input-username"]', 'testuser');
  await page.fill('[data-testid="input-password"]', 'test123');
  await page.click('[data-testid="btn-login"]');

  // ตรวจว่า login สำเร็จ — เห็น session badge พร้อม username
  await expect(page.getByTestId('session-badge')).toContainText('testuser');

  // Save authenticated state ลงไฟล์
  await page.context().storageState({ path: authFile });

  console.log(`Auth state saved to: ${authFile}`);
});
```

**tests/protected-pages.spec.ts:**

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

// ไม่ต้อง login ที่นี่ — config inject storageState ให้แล้ว

test('เข้า /api/me ได้หลัง authenticate', async ({ request }) => {
  const res = await request.get('/api/me');
  expect(res.ok()).toBeTruthy();
  const body = await res.json();
  expect(body.username).toBe('testuser');
});

test('เห็น session badge บน dashboard', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByTestId('session-badge')).toContainText('testuser');
});

test('เข้า /api/orders ได้ (protected endpoint)', async ({ request }) => {
  const res = await request.get('/api/orders');
  expect(res.ok()).toBeTruthy();
});
```

Output เมื่อรัน:

```
Running 1 setup and 3 tests using 1 worker

  ✓  1 [setup] › tests/auth.setup.ts:7:1 › authenticate as testuser (1.8s)
  ✓  2 [chromium] › tests/protected-pages.spec.ts:6:1 › เข้า /api/me ได้หลัง authenticate (0.3s)
  ✓  3 [chromium] › tests/protected-pages.spec.ts:12:1 › เห็น session badge บน dashboard (0.6s)
  ✓  4 [chromium] › tests/protected-pages.spec.ts:18:1 › เข้า /api/orders ได้ (protected endpoint) (0.2s)

  4 passed (3.1s)
```

สังเกต: setup รัน 1 ครั้ง ส่วน test 3 ตัวไม่ต้อง login เลย — ทั้ง suite เสร็จใน 3.1 วินาที เทียบกับถ้า login ทุก test จะใช้ ~7-10 วินาที

---

### Intermediate: ทดสอบ Logout Flow — ยืนยันว่า Session หมดแล้วจริง

สถานการณ์: e-commerce app ต้องการ verify ว่าหลัง user logout แล้ว storageState เก่าใช้งานไม่ได้อีกต่อไป — ทั้ง protected pages ต้อง redirect ไป `/login` และ API endpoints ต้อง return 401

```typescript
// tests/logout-session.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

// project นี้ใช้ storageState: 'playwright/.auth/user.json' จาก config
// แต่ test เหล่านี้ทดสอบ state หลัง logout — ต้องล้าง auth state เอง

test.describe('logout invalidates session', () => {
  test('หลัง logout: GET /api/me ต้อง return 401', async ({ page, request }) => {
    // ขั้นตอน 1: ยืนยันว่า login อยู่ (storageState จาก config)
    await page.goto('http://localhost:3000/');
    await expect(page.getByTestId('session-badge')).toContainText('testuser');

    // ขั้นตอน 2: Logout ผ่าน UI
    await page.click('[data-testid="btn-logout"]');

    // ขั้นตอน 3: ตรวจว่า redirect ไป login page
    await expect(page).toHaveURL(/\/login/);
    await expect(page.getByTestId('btn-login')).toBeVisible();

    // ขั้นตอน 4: ลองเข้า protected page โดยตรง — ต้อง redirect กลับ login
    await page.goto('http://localhost:3000/profile');
    await expect(page).toHaveURL(/\/login/);

    // ขั้นตอน 5: ตรวจ API ด้วย request context (ยังมี cookies จาก logout อยู่)
    const meRes = await request.get('http://localhost:3000/api/me');
    expect(meRes.status()).toBe(401);
  });

  test('หลัง logout: storageState เก่าที่ inject ใหม่ใช้งานไม่ได้ ถ้า server revoke session', async ({ browser }) => {
    // จำลอง scenario: เอา storageState เก่ามา inject หลังจาก logout ไปแล้ว
    // ถ้า app ใช้ server-side session (revoked ทันทีที่ logout) → ต้อง fail
    // ถ้า app ใช้ JWT (stateless) → อาจยังใช้ได้จนกว่า token จะ expire

    const staleContext = await browser.newContext({
      storageState: 'playwright/.auth/user.json', // state เก่า
    });
    const stalePage = await staleContext.newPage();

    // Navigate ไป protected page ด้วย stale state
    const response = await stalePage.goto('http://localhost:3000/api/me');

    // Demo app ใช้ JWT — stale token ยังใช้ได้จนกว่าจะ expire
    // ถ้าเปลี่ยนเป็น server-side session test นี้ต้อง expect 401
    // การทดสอบนี้ document behavior ของ app ว่าใช้ stateless หรือ stateful auth
    if (response?.status() === 401) {
      // Server-side session: revoked ทันทีที่ logout
      console.log('App uses server-side session — stale token rejected correctly');
    } else {
      // JWT: stateless — token ยังใช้ได้จนกว่าจะ expire
      console.log('App uses JWT — stale token still valid until expiry');
      expect(response?.status()).toBe(200);
    }

    await staleContext.close();
  });
});
```

Output:

```
Running 2 tests using 1 worker

  ✓  1 [chromium] › tests/logout-session.spec.ts:9:3 › หลัง logout: GET /api/me ต้อง return 401 (1.9s)
  ✓  2 [chromium] › tests/logout-session.spec.ts:32:3 › หลัง logout: storageState เก่าที่ inject ใหม่ใช้งานไม่ได้ ถ้า server revoke session (0.8s)

  2 passed (2.8s)
```

สิ่งที่ทำให้ตัวอย่างนี้ต่างจาก Beginner:

- เริ่มจาก authenticated state (storageState จาก config) แล้วทดสอบ **transition ออกจาก auth** — ตรวจทั้ง UI redirect และ API response
- test ที่ 2 ทดสอบ security property ของ auth mechanism โดยตรง: stateless (JWT) vs stateful (server-side session) — เป็น pattern ที่ production teams ต้องรู้
- ใช้ `browser.newContext()` สร้าง context ด้วย stale storageState แยกจาก main test session — ไม่ contaminate state ของ test อื่น

---

### Advanced: API Auth + Cookie Injection + Per-Worker Isolation

สถานการณ์ production-grade: CI pipeline รัน tests แบบ parallel 4 workers แต่ละ worker ต้องมี isolated auth state เพื่อไม่ conflict กัน และต้องการ login เร็วที่สุดโดยใช้ API แทน UI

```typescript
// tests/parallel-auth.setup.ts
// tested: Playwright v1.50+, Node.js 20+
import { test as setup, expect } from '@playwright/test';

// Worker accounts สำหรับ parallel isolation
// ใน production: ควรมี account จริงหลายตัว (worker0@company.com, worker1@company.com)
// ตัวอย่างนี้ใช้ account เดียว แต่ save state แยก worker เพื่อ demonstrate pattern
const WORKER_ACCOUNTS = [
  { username: 'testuser', password: 'test123' },
  { username: 'testuser', password: 'test123' }, // เพิ่ม accounts จริงใน production
  { username: 'testuser', password: 'test123' },
  { username: 'testuser', password: 'test123' },
];

setup('authenticate per worker via API', async ({ request, browser }, testInfo) => {
  const workerIndex = testInfo.parallelIndex;
  const account = WORKER_ACCOUNTS[workerIndex] ?? WORKER_ACCOUNTS[0];
  const authFile = `playwright/.auth/worker-${workerIndex}.json`;

  // ── Step 1: API Login — เร็วกว่า UI ~10x ──────────────────────
  const loginRes = await request.post('http://localhost:3000/api/auth/login', {
    data: { username: account.username, password: account.password },
  });
  expect(loginRes.ok()).toBeTruthy();
  const { token } = await loginRes.json();
  expect(token).toBeTruthy();

  // ── Step 2: สร้าง browser context พร้อม token ─────────────────
  const context = await browser.newContext();

  // Inject token ก่อน page load ใดๆ
  await context.addInitScript((authToken: string) => {
    window.localStorage.setItem('authToken', authToken);
  }, token);

  const page = await context.newPage();

  // ── Step 3: Verify ว่า app รับ token แล้ว ─────────────────────
  await page.goto('http://localhost:3000/');
  await page.waitForLoadState('networkidle');

  // ตรวจว่า session badge แสดง username (ยืนยัน token ใช้งานได้)
  try {
    await expect(page.getByTestId('session-badge')).toContainText(account.username, { timeout: 5000 });
  } catch {
    // ถ้า app ต้องการ navigate หลัง inject localStorage
    await page.reload();
    await expect(page.getByTestId('session-badge')).toContainText(account.username, { timeout: 5000 });
  }

  // ── Step 4: Save per-worker state ─────────────────────────────
  await context.storageState({ path: authFile });
  await context.close();

  console.log(`Worker ${workerIndex}: auth saved to ${authFile}`);
});
```

**playwright.config.ts สำหรับ per-worker setup:**

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  workers: 4,
  projects: [
    {
      name: 'parallel-setup',
      testMatch: /parallel-auth\.setup\.ts/,
    },
    {
      name: 'parallel-tests',
      use: {
        ...devices['Desktop Chrome'],
        // ไม่ตั้ง storageState ที่นี่ — custom fixture ใน tests/fixtures/auth.ts
        // จัดการ per-worker storageState เองโดยใช้ parallelIndex
      },
      dependencies: ['parallel-setup'],
    },
  ],
});
```

**tests/fixtures/auth.ts — ให้ tests/parallel-orders.spec.ts import จากที่นี่แทน `@playwright/test`:**

```typescript
// tests/fixtures/auth.ts
// tested: Playwright v1.50+, Node.js 20+
import { test as baseTest } from '@playwright/test';
import path from 'path';

export * from '@playwright/test';

export const test = baseTest.extend<{}, { workerStorageState: string }>({
  storageState: ({ workerStorageState }, use) => use(workerStorageState),

  workerStorageState: [async ({}, use) => {
    const id = test.info().parallelIndex;
    const fileName = path.resolve(
      test.info().project.outputDir,
      `.auth/worker-${id}.json`
    );
    // ไฟล์ถูกสร้างโดย parallel-setup project ไปแล้ว — load ตรงๆ
    await use(fileName);
  }, { scope: 'worker' }],
});
```

Output บน CI:

```
Running 4 setups and 40 tests using 4 workers

  ✓  [parallel-setup] Worker 0: auth saved to playwright/.auth/worker-0.json (0.4s)
  ✓  [parallel-setup] Worker 1: auth saved to playwright/.auth/worker-1.json (0.4s)
  ✓  [parallel-setup] Worker 2: auth saved to playwright/.auth/worker-2.json (0.4s)
  ✓  [parallel-setup] Worker 3: auth saved to playwright/.auth/worker-3.json (0.4s)
  ✓  40 tests passed (8.2s)

  44 passed (9.0s)
```

**Design decisions ที่ต้องอธิบายได้:**

- **API login แทน UI**: ลดเวลา setup จาก ~4s/worker เป็น ~0.4s/worker — สำคัญมากเมื่อ setup รันบ่อย
- **`addInitScript()` แทน `page.evaluate()`**: `addInitScript` รันก่อน page scripts ทุกครั้ง — ถ้าใช้ `evaluate` อาจ race กับ app initialization code
- **`context.close()` หลัง save**: สำคัญ — flush storageState ให้สมบูรณ์ก่อนที่ test project จะอ่าน

---

## 6. Common Mistakes

❌ **Login ใน `beforeEach` ทุก test** — login 100 ครั้งสำหรับ 100 tests:

```typescript
// ❌ ผิด — รัน beforeEach ทุก test = login ซ้ำทุกครั้ง
test.beforeEach(async ({ page }) => {
  await page.goto('/login');
  await page.fill('[data-testid="input-username"]', 'admin');
  await page.fill('[data-testid="input-password"]', 'admin123');
  await page.click('[data-testid="btn-login"]');
});

// ✅ ถูก — login ครั้งเดียวใน setup project, test ใช้ storageState
// playwright.config.ts: use: { storageState: 'playwright/.auth/admin.json' }
// tests/admin.setup.ts: login แล้ว page.context().storageState(...)
```

*(source: https://playwright.dev/docs/auth)*

---

❌ **Commit `playwright/.auth/*.json` ไปยัง git** — ไฟล์เหล่านี้มี tokens และ cookies จริง:

```bash
# ❌ ผิด — ไม่มี .gitignore สำหรับ auth files
git add playwright/.auth/admin.json  # ← อันตราย

# ✅ ถูก — เพิ่มใน .gitignore
echo "playwright/.auth/" >> .gitignore
git rm --cached playwright/.auth/ -r  # ลบออกจาก tracking ถ้า commit ไปแล้ว
```

*(source: https://playwright.dev/docs/auth)*

---

❌ **ใช้ storageState เดียวสำหรับ parallel tests ที่ modify server-side state** — race condition:

```typescript
// ❌ ผิด — workers ทุกตัวใช้ account เดียวกัน
// Worker 1 ลบ order #5, Worker 2 พยายาม update order #5 → fail
use: { storageState: 'playwright/.auth/single-user.json' }

// ❌ ผิด — storageState ใน playwright.config.ts ไม่รับ function
storageState: ({ parallelIndex }) =>
  `playwright/.auth/worker-${parallelIndex}.json`

// ✅ ถูก — ใช้ worker-scoped fixture แทน (ดู section 4.5)
// tests/fixtures/auth.ts — custom fixture ที่ manage per-worker storageState
// tests/parallel-orders.spec.ts — import { test } from './fixtures/auth'
// ไฟล์ที่สร้างแล้ว: playwright/.auth/worker-0.json, worker-1.json, ...
```

*(source: https://playwright.dev/docs/auth#moderate-one-account-per-parallel-worker)*

---

❌ **ลืมระบุ `{ indexedDB: true }` เมื่อ app ใช้ IndexedDB** — ผิดพลาด silent:

```typescript
// ❌ สถานการณ์: app เก็บ auth token ใน IndexedDB (เช่น Firebase Auth)
// storageState จะ save โดยไม่รวม IndexedDB (default)
// test จะ fail ด้วย "not authenticated" โดยไม่รู้ว่าทำไม
await page.context().storageState({ path: authFile }); // ❌ ลืม indexedDB: true

// ✅ ถูก — เปิดใช้ IndexedDB capture ด้วย option ชัดเจน
await page.context().storageState({ path: authFile, indexedDB: true });
```

*(source: https://playwright.dev/docs/auth#reuse-signed-in-state)*

---

❌ **ลืม `dependencies` ใน config** — tests รันก่อน setup เสร็จ:

```typescript
// ❌ ผิด — ไม่มี dependencies
{
  name: 'chromium',
  use: { storageState: 'playwright/.auth/user.json' },
  // ลืม dependencies: ['setup'] → อาจรันก่อน setup → authFile ยังไม่มี → error
}

// ✅ ถูก
{
  name: 'chromium',
  use: { storageState: 'playwright/.auth/user.json' },
  dependencies: ['setup'],  // ← รอให้ 'setup' project เสร็จก่อน
}
```

*(source: https://playwright.dev/docs/auth#basic-shared-account-in-all-tests)*

---

## 7. สรุปบท

ก่อนดูเฉลย ลองตอบคำถามเหล่านี้ด้วยตัวเองก่อน:

**คำถามที่ 1:** เพื่อนคุณบอกว่า "storageState เก็บทุกอย่างของ browser เหมือน snapshot สมบูรณ์" — คุณจะอธิบายอะไรบ้างที่เขาเข้าใจผิด? ยกตัวอย่าง scenario จริงที่ความเข้าใจผิดนี้จะทำให้ test fail แบบ silent

---

**คำถามที่ 2:** ทีมของคุณมี test suite 200 tests ที่ต้องการทดสอบทั้ง admin workflow และ user workflow พร้อมกัน (100 tests ต่อ role) คุณจะออกแบบ `playwright.config.ts` อย่างไร? ต้องมีกี่ setup projects และกี่ test projects?

---

**คำถามที่ 3:** คุณพบ bug: test ที่ใช้ `storageState` รัน pass บน local แต่ fail บน CI ด้วย error "Authentication required — please login" ทั้งที่ setup project รัน pass และ save ไฟล์ `playwright/.auth/user.json` เสร็จแล้ว — อะไรคือสาเหตุที่น่าจะเป็นไปได้มากที่สุด และจะ diagnose อย่างไร?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย:**

**คำถามที่ 1:**
- **สิ่งที่เพื่อนเข้าใจผิด:** storageState capture **cookies, localStorage, และ IndexedDB** (ด้วย `{ indexedDB: true }`) — แต่ **ไม่รวม sessionStorage** และไม่รวม in-memory state ของ JavaScript การบอกว่า "เก็บทุกอย่าง" ผิดสองจุด: sessionStorage หายไปเสมอ และ IndexedDB ต้องเปิดใช้เองด้วย option
- **Scenario ที่ทำให้ fail แบบ silent:** ถ้า app ใช้ sessionStorage เก็บ auth token storageState จะ save และ load ได้ปกติ (ไม่ error) แต่ sessionStorage จะว่างเปล่าใน context ใหม่ ทำให้ test navigate ไป protected page แล้วได้รับ redirect ไป login page แทน — error message จะเป็น "expected URL to be /dashboard, got /login" ซึ่งไม่บอกตรงๆ ว่า sessionStorage หายไป

**คำถามที่ 2:**
- **2 setup projects:** `setup admin` (testMatch: `/admin\.setup\.ts/`) และ `setup user` (testMatch: `/user\.setup\.ts/`)
- **2 test projects:** `Admin tests` (storageState: admin.json, dependencies: ['setup admin']) และ `User tests` (storageState: user.json, dependencies: ['setup user'])
- ไม่ต้องมี dependencies ข้าม setup projects — admin setup และ user setup รันอิสระจากกัน
- Total: 4 projects ใน config เดียว

**คำถามที่ 3:**
- **สาเหตุที่น่าจะเป็น:**
  1. **Path ไม่ตรง** — บน CI directory structure อาจต่าง ลอง log `process.cwd()` และ path ของ authFile ใน setup
  2. **Token expire ก่อน test รัน** — ถ้า CI ช้าและ token หมดอายุเร็ว เช่น session timeout 1 นาที แต่ CI ใช้เวลา setup นาน
  3. **Domain/URL ไม่ตรง** — storageState ผูกกับ origin ถ้า local ใช้ `localhost:3000` แต่ CI ใช้ IP หรือ hostname อื่น cookies จะไม่ match
  4. **Setup รันคนละ worker กับ test** — storageState file save แล้วแต่ worker ที่รัน test อ่าน file path ผิด
- **วิธี diagnose:** เพิ่ม `console.log` ใน test เพื่อ print storageState path, ตรวจ file ว่ามีจริงและมี cookies/origins ด้วย `fs.readFileSync()`, และ verify baseURL ใน config ตรงกับ origin ที่ app ใช้จริง

</details>

---

## 8. สิ่งที่ต้องรู้ก่อนบทถัดไป

บทที่ 14 จะพูดถึง **Visual Testing & Screenshots** — การ capture หน้าจอ, เปรียบเทียบ pixel-by-pixel และ visual regression testing

ทบทวนก่อน:
- `storageState` inject cookies และ localStorage ก่อน test navigate ไปหน้าใดๆ — concept นี้เหมือนกับ `addInitScript` ที่รันก่อน page scripts แต่ storageState ทำงานระดับ browser state ไม่ใช่ JavaScript
- `dependencies` ใน playwright.config.ts ทำให้ project รันตามลำดับที่กำหนด — ใช้แนวคิดเดียวกับ CI/CD pipeline steps
- ถ้าต้องการ test ที่ไม่ต้องการ auth: ใช้ `test.use({ storageState: { cookies: [], origins: [] } })` เพื่อ override แบบ explicit
