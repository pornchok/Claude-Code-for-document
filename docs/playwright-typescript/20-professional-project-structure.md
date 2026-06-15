# บทที่ 20: Professional Project Structure — โครงสร้างโปรเจคระดับมืออาชีพ

## 0. Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบคำถามจากบทที่แล้ว:

**Q1:** ใน Ch19 เราใช้ `readDb()` เพื่ออะไร? ต่างจากการ query ผ่าน API request อย่างไร?

**Q2:** `expect.poll()` ต่างจาก `waitForTimeout()` อย่างไร? ทำไมถึงดีกว่า?

**Q3:** เหตุใดการ verify เฉพาะ UI สำหรับ "create todo" จึงอันตราย?

---

<details>
<summary>เฉลย Pre-chapter</summary>

**A1:** `readDb()` อ่าน `db.json` โดยตรงจาก filesystem — ได้ ground truth ว่าข้อมูลถึง DB จริงหรือยัง ต่างจาก API request ที่ผ่าน business logic (validation, serialization) ซึ่งอาจ filter ข้อมูลบางอย่างออก

**A2:** `expect.poll()` ตรวจ condition จริงๆ แล้วหยุดทันทีที่ผ่าน, `waitForTimeout()` รอเวลาตายตัวเสมอ — `poll` เร็วกว่า, ไม่ flaky, และ self-documenting ว่ากำลังรอ condition อะไร

**A3:** UI อาจ render จาก local state (optimistic update) โดยที่ POST ยังไม่ถึง server หรือ server มี bug ที่ไม่ save ลง DB — test ผ่านได้แม้ข้อมูลไม่ถึง DB จริง

</details>

---

## 1. วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะ:

- ออกแบบ folder structure ที่รองรับทั้ง web (UI) และ API testing ในโปรเจคเดียวกัน
- เข้าใจว่า `pages/`, `fixtures/`, `helpers/`, `types/` มีหน้าที่ต่างกันอย่างไร
- รู้ว่า over-abstraction คืออะไรและจะหลีกเลี่ยงได้อย่างไร
- เขียน Page Object ที่ถูกต้องและ fixture ที่ใช้ร่วมกันระหว่าง web กับ API test
- ตัดสินใจได้ว่า test ใดควรอยู่ใน `tests/web/` หรือ `tests/api/`

---

## 2. ทำไมต้องรู้? (Why)

สมมติว่าทีมมี test suite 80 tests — ทุก `.spec.ts` อยู่ในโฟลเดอร์เดียวกัน setup code copy-paste ซ้ำกันในทุกไฟล์ และไม่มีใครรู้ว่า "login helper" อยู่ไฟล์ไหน:

```
tests/
├── login.spec.ts         ← มี loginHelper() อยู่ข้างใน
├── cart.spec.ts          ← copy loginHelper() จาก login.spec.ts มา
├── admin-api.spec.ts     ← มี loginHelper() เวอร์ชันที่ 3
├── todos.spec.ts         ← มี loginHelper() เวอร์ชันที่ 4 แต่ bug ต่าง
└── ... (76 ไฟล์อื่น)
```

ผลที่ตามมา:
- แก้ bug ใน loginHelper ต้องแก้ 4 ที่ (แล้วยังลืมบางที่)
- Web test กับ API test ปนกัน — ไม่รู้ว่า `todos.spec.ts` test UI หรือ endpoint
- Onboard developer ใหม่ใช้เวลา 2 สัปดาห์แค่เพื่อเข้าใจว่าไฟล์แต่ละตัวทำอะไร

structure ที่ดีแก้ปัญหาเหล่านี้ได้โดยไม่ต้องเพิ่ม complexity โดยไม่จำเป็น

---

## 3. โครงสร้างที่แนะนำ

### 3.1 The Recommended Structure

```
project-root/
├── playwright.config.ts        # config ทั้งหมด — projects, baseURL, timeout
├── tests/
│   ├── web/                    # UI tests (ใช้ page, เปิด browser)
│   │   ├── auth/
│   │   │   └── login.spec.ts
│   │   ├── shop/
│   │   │   └── shop.spec.ts
│   │   └── todos/
│   │       └── todos.spec.ts
│   └── api/                    # Pure API tests (ใช้ request เท่านั้น)
│       ├── todos.api.spec.ts
│       └── products.api.spec.ts
├── pages/                      # Page Object Models (web เท่านั้น)
│   ├── LoginPage.ts
│   ├── ShopPage.ts
│   └── TodosPage.ts
├── fixtures/                   # Custom fixtures (ใช้ร่วมกัน web+api)
│   ├── index.ts                # barrel export — import จากที่เดียว
│   └── auth.fixture.ts
├── helpers/                    # Pure functions ที่ไม่ขึ้น Playwright
│   └── db.ts                  # อ่าน db.json โดยตรง
└── types/
    └── index.ts                # TypeScript interfaces ที่ใช้ร่วมกัน
```

**หลักการแบ่ง:**
| โฟลเดอร์ | ขึ้น Playwright? | ใช้กับ | ตัวอย่าง |
|-----------|-----------------|--------|----------|
| `tests/web/` | ✅ | browser | login flow, checkout |
| `tests/api/` | ✅ | request only | CRUD endpoints |
| `pages/` | ✅ | web tests | LoginPage.ts |
| `fixtures/` | ✅ | web + api | loginFixture |
| `helpers/` | ❌ | ทุกที่ | readDb(), generateId() |
| `types/` | ❌ | ทุกที่ | interface Todo, Product |

### 3.2 ทำไม Web/API แยกกัน?

ใน `tests/web/` — test เปิด browser เสมอ, ช้ากว่า (2-10 วินาที/test), ตรวจ user experience:

```typescript
// tests/web/todos/todos.spec.ts
test('เพิ่ม todo แล้วแสดงใน list', async ({ page }) => {
  await page.goto('/todos');
  await page.getByTestId('input-new-todo').fill('Buy milk');
  await page.getByTestId('btn-add-todo').click();
  await expect(page.getByText('Buy milk')).toBeVisible();
});
```

ใน `tests/api/` — ไม่เปิด browser, เร็วมาก (~50ms/test), ตรวจ API contract:

```typescript
// tests/api/todos.api.spec.ts
test('POST /api/todos returns 201 + todo object', async ({ request }) => {
  const res = await request.post('http://localhost:3000/api/todos', {
    data: { text: 'Buy milk' }
  });
  expect(res.status()).toBe(201);
  const todo = await res.json();
  expect(todo).toMatchObject({ text: 'Buy milk', completed: false });
  expect(todo.id).toBeDefined();
});
```

### 3.3 Page Objects — เฉพาะ UI Abstraction

Page Object encapsulate selectors และ actions สำหรับหนึ่งหน้า — ถ้า testid เปลี่ยน แก้ที่เดียว:

```typescript
// pages/TodosPage.ts
// tested: Playwright v1.50+, TypeScript 5+
import { type Page, type Locator } from '@playwright/test';

export class TodosPage {
  readonly page: Page;
  readonly input: Locator;
  readonly addButton: Locator;
  readonly list: Locator;

  constructor(page: Page) {
    this.page = page;
    this.input = page.getByTestId('input-new-todo');
    this.addButton = page.getByTestId('btn-add-todo');
    this.list = page.getByTestId('todo-list');
  }

  async goto() {
    await this.page.goto('/todos');
  }

  async addTodo(text: string) {
    await this.input.fill(text);
    await this.addButton.click();
  }

  todoItem(id: number) {
    return this.page.locator(`[data-testid="todo-item-${id}"]`);
  }
}
```

**กฎ Page Object:**
- Contain selectors + actions เท่านั้น — ไม่มี `expect()` ใน Page Object
- ไม่มี API calls ใน Page Object — ถ้าต้องการ setup data ผ่าน API ให้ทำใน fixture
- ไม่ต้อง Page Object สำหรับทุกหน้า — สร้างเมื่อมีการ reuse จาก 3+ tests

### 3.4 Fixtures — Shared Setup ที่ใช้ได้กับทั้ง Web และ API

Fixture คือ dependency injection ของ Playwright — ทั้ง web test และ API test ใช้ร่วมกันได้:

```typescript
// fixtures/auth.fixture.ts
// tested: Playwright v1.50+, TypeScript 5+
import { test as base, expect } from '@playwright/test';

type AuthFixtures = {
  authToken: string;
  authenticatedPage: import('@playwright/test').Page;
};

export const test = base.extend<AuthFixtures>({
  authToken: async ({ request }, use) => {
    const res = await request.post('http://localhost:3000/api/auth/login', {
      data: { username: 'testuser', password: 'test123' }
    });
    const { token } = await res.json();
    await use(token);
    // ไม่มี teardown ที่นี่ — JWT stateless
  },

  authenticatedPage: async ({ page, authToken }, use) => {
    // inject token เข้า localStorage (key = 'jwt_token' ตามที่ client.js ใช้)
    await page.goto('/');
    await page.evaluate((tok) => {
      localStorage.setItem('jwt_token', tok);
    }, authToken);
    await use(page);
  },
});

export { expect };
```

```typescript
// fixtures/index.ts — barrel export
export { test, expect } from './auth.fixture';
```

ใช้ใน web test:

```typescript
// tests/web/todos/todos.spec.ts
import { test, expect } from '../../../fixtures';

test('authenticated user สร้าง todo ได้', async ({ authenticatedPage }) => {
  const page = authenticatedPage;
  await page.goto('/todos');
  await page.getByTestId('input-new-todo').fill('Buy groceries');
  await page.getByTestId('btn-add-todo').click();
  await expect(page.locator('[data-testid^="todo-item-"]').first()).toBeVisible();
});
```

ใช้ใน API test:

```typescript
// tests/api/todos.api.spec.ts
import { test, expect } from '../../fixtures';

test('authenticated user POST /api/todos ได้', async ({ authToken, request }) => {
  const res = await request.post('http://localhost:3000/api/todos', {
    headers: { Authorization: `Bearer ${authToken}` },
    data: { text: 'API test todo' }
  });
  expect(res.status()).toBe(201);
});
```

### 3.5 Helpers — Pure Functions ที่ Test ได้

`helpers/` เก็บ logic ที่ไม่ขึ้น Playwright — test ได้ง่าย, reuse ได้ทุกที่:

```typescript
// helpers/db.ts
// tested: Node.js 20+
import { readFileSync } from 'fs';
import { join } from 'path';

interface Db {
  todos: Array<{ id: number; text: string; completed: boolean }>;
  products: Array<{ id: number; name: string; price: number; category: string }>;
  orders: Array<{ id: number; items: unknown[] }>;
  users: Array<{ id: number; username: string; role: string }>;
}

export function readDb(): Db {
  // path สัมพัทธ์จาก project root (process.cwd() คือที่ที่รัน npx playwright test)
  // ถ้ารันจาก docs/playwright-typescript/ ให้ใช้ 'playwright-course-app/data/db.json'
  // ถ้ารันจาก repo root ให้ใช้ 'docs/playwright-typescript/playwright-course-app/data/db.json'
  const dbPath = join(process.cwd(), 'playwright-course-app/data/db.json');
  return JSON.parse(readFileSync(dbPath, 'utf-8'));
}

export function generateUniqueText(prefix = 'test'): string {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
}
```

### 3.6 Playwright Config ที่ support Web + API

```typescript
// playwright.config.ts
// tested: Playwright v1.50+
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  workers: process.env.CI ? 2 : undefined,
  timeout: 30_000,
  expect: { timeout: 5_000 },

  projects: [
    // API tests — ไม่ต้องการ browser, เร็วกว่า
    {
      name: 'api',
      testMatch: '**/api/**/*.spec.ts',
      use: { baseURL: 'http://localhost:3000' },
    },
    // Web tests — รัน Chromium
    {
      name: 'web-chromium',
      testMatch: '**/web/**/*.spec.ts',
      use: {
        ...devices['Desktop Chrome'],
        baseURL: 'http://localhost:3000',
      },
    },
  ],

  webServer: {
    command: 'node playwright-course-app/server.js',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
  },
});
```

---

## 4. ตัวอย่าง 3 ระดับ

### Beginner — Refactor จาก Flat เป็น Organized

ก่อน refactor (flat, copy-paste everywhere):

```typescript
// tests/shop-test.spec.ts — ❌ login logic ซ้ำ
test('shop products show', async ({ page }) => {
  // login ซ้ำทุก test file
  const res = await page.request.post('/api/auth/login', {
    data: { username: 'testuser', password: 'test123' }
  });
  const { token } = await res.json();
  await page.evaluate(tok => localStorage.setItem('jwt_token', tok), token);

  await page.goto('/shop');
  await page.waitForSelector('[data-testid="product-grid"]');
  await expect(page.locator('[data-testid^="product-card-"]').first()).toBeVisible();
});
```

หลัง refactor (fixture-based):

```typescript
// tests/web/shop/shop.spec.ts — ✅ ใช้ authFixture
import { test, expect } from '../../../fixtures';

test('shop products show', async ({ authenticatedPage }) => {
  await authenticatedPage.goto('/shop');
  await authenticatedPage.waitForSelector('[data-testid="product-grid"]');
  await expect(
    authenticatedPage.locator('[data-testid^="product-card-"]').first()
  ).toBeVisible();
});
```

สิ่งที่ดีขึ้น: ถ้า password เปลี่ยน แก้แค่ `fixtures/auth.fixture.ts` ที่เดียว

### Intermediate — Page Object + Real Course App

สถานการณ์: ทีม QA ต้องการ test todo workflow หลาย scenario — login, add, complete, delete — โดยไม่ต้อง duplicate selector string

```typescript
// pages/LoginPage.ts
// tested: Playwright v1.50+, TypeScript 5+
import { type Page } from '@playwright/test';

export class LoginPage {
  constructor(private page: Page) {}

  async goto() {
    await this.page.goto('/login');
  }

  async login(username: string, password: string) {
    await this.page.getByTestId('input-username').fill(username);
    await this.page.getByTestId('input-password').fill(password);
    await this.page.getByTestId('btn-login').click();
  }

  async loginAsTestuser() {
    await this.goto();
    await this.login('testuser', 'test123');
  }
}
```

```typescript
// tests/web/todos/todos-workflow.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';
import { LoginPage } from '../../../pages/LoginPage';
import { TodosPage } from '../../../pages/TodosPage';

test.describe('Todo Workflow', () => {
  test.beforeEach(async ({ request }) => {
    // reset todos ก่อนทุก test
    await request.post('http://localhost:3000/api/reset');
  });

  test('add todo แล้วแสดงใน list', async ({ page }) => {
    const login = new LoginPage(page);
    const todos = new TodosPage(page);

    await login.loginAsTestuser();
    await todos.goto();
    await todos.addTodo('Buy groceries');

    await expect(page.getByText('Buy groceries')).toBeVisible();
  });

  test('complete todo แสดง strikethrough', async ({ page, request }) => {
    // setup ผ่าน API (เร็วกว่า UI ~10x)
    const loginRes = await request.post('http://localhost:3000/api/auth/login', {
      data: { username: 'testuser', password: 'test123' }
    });
    const { token } = await loginRes.json();

    const todoRes = await request.post('http://localhost:3000/api/todos', {
      headers: { Authorization: `Bearer ${token}` },
      data: { text: 'Read book' }
    });
    const todo = await todoRes.json();

    // verify UI
    const login = new LoginPage(page);
    const todos = new TodosPage(page);
    await login.loginAsTestuser();
    await todos.goto();

    // click checkbox ผ่าน UI
    await page.locator(`[data-testid="todo-checkbox-${todo.id}"]`).click();
    await expect(
      page.locator(`[data-testid="todo-text-${todo.id}"]`)
    ).toHaveClass(/completed/);
  });
});
```

### Advanced — Multi-Project Config + Shared Fixture Design

Production-grade scenario: suite ใหญ่ขึ้นจาก 50 → 200 tests, ต้องการ:
- API tests รัน parallel ไม่มี browser overhead
- Web tests รัน multi-browser บน CI
- Auth fixture ใช้ร่วมกันทั้ง 2 project types

```typescript
// playwright.config.ts — production config
// tested: Playwright v1.50+
import { defineConfig, devices } from '@playwright/test';

const BASE_URL = process.env.BASE_URL ?? 'http://localhost:3000';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 4 : undefined,
  reporter: [
    ['html', { open: 'on-failure' }],
    ...(process.env.CI ? [['github'] as ['github']] : []),
  ],

  projects: [
    // ── Smoke: API เท่านั้น (เร็วสุด, รัน pre-deploy) ──
    {
      name: 'smoke-api',
      testMatch: '**/api/**/*.spec.ts',
      grep: /@smoke/,
      use: { baseURL: BASE_URL },
    },

    // ── Full API regression ──
    {
      name: 'api',
      testMatch: '**/api/**/*.spec.ts',
      use: { baseURL: BASE_URL },
    },

    // ── Web: Chrome เท่านั้นใน local ──
    {
      name: 'web',
      testMatch: '**/web/**/*.spec.ts',
      use: { ...devices['Desktop Chrome'], baseURL: BASE_URL },
    },

    // ── Web: multi-browser บน CI เท่านั้น ──
    ...(process.env.CI ? [
      {
        name: 'web-firefox',
        testMatch: '**/web/**/*.spec.ts',
        use: { ...devices['Desktop Firefox'], baseURL: BASE_URL },
      },
    ] : []),
  ],

  webServer: {
    command: 'node playwright-course-app/server.js',
    url: BASE_URL,
    reuseExistingServer: !process.env.CI,
    stdout: 'ignore',
    stderr: 'pipe',
  },
});
```

**Design decision:** ใช้ `process.env.CI` แยก multi-browser เฉพาะ CI — local dev ใช้ Chrome เท่านั้นเพื่อให้ feedback loop เร็ว

---

## 5. Common Mistakes ❌→✅

**Mistake 1: Over-abstraction ก่อนถึงเวลา**

❌ สร้าง factory, builder, repository pattern ตั้งแต่วันแรก

```
tests/
├── factories/
│   ├── TodoFactory.ts
│   ├── UserFactory.ts
│   └── OrderFactory.ts
├── repositories/
│   └── TodoRepository.ts
├── builders/
│   └── RequestBuilder.ts
└── ... (test ยังไม่ถึง 20 ชิ้น)
```

✅ เริ่มจาก flat, refactor เมื่อมีการ duplicate จริง

*(source: https://playwright.dev/docs/best-practices)*

**Mistake 2: ใส่ expect() ใน Page Object**

❌ Page Object ทำ assertion เอง

```typescript
// ❌ Page Object ไม่ควร assert
class TodosPage {
  async addTodoAndVerify(text: string) {
    await this.input.fill(text);
    await this.addButton.click();
    await expect(this.list).toContainText(text); // ❌ assertion ใน POM
  }
}
```

✅ Page Object return locator หรือ perform action เท่านั้น — test file ทำ assertion

```typescript
// ✅ แยก action กับ assertion
class TodosPage {
  async addTodo(text: string) {
    await this.input.fill(text);
    await this.addButton.click();
    // ไม่มี expect ที่นี่
  }
}

// ใน test:
await todosPage.addTodo('Buy milk');
await expect(page.getByText('Buy milk')).toBeVisible(); // assertion อยู่ใน test
```

*(source: https://playwright.dev/docs/pom)*

**Mistake 3: ผสม API logic ใน Page Object**

❌ Page Object ทำ API call

```typescript
// ❌ Page Object ไม่ควรรู้จัก API
class TodosPage {
  async createTodoViaApi(text: string) {
    const res = await fetch('/api/todos', { method: 'POST', ... });
    // ← API logic ใน POM ทำให้ test กับ POM coupled เกินไป
  }
}
```

✅ API setup อยู่ใน fixture หรือ test โดยตรง — Page Object รู้จักแค่ UI

*(source: https://playwright.dev/docs/pom)*

**Mistake 4: ไม่ export จาก barrel — import หลายที่**

❌ import จาก path ยาวในทุก test file

```typescript
// ❌ ทุก test file ต้องรู้ path จริง
import { test } from '../../../fixtures/auth.fixture';
import { expect } from '@playwright/test';
```

✅ barrel export จาก `fixtures/index.ts` — import จากที่เดียว

```typescript
// ✅ ใน fixtures/index.ts
export { test, expect } from './auth.fixture';

// ใน test files — สั้น, ชัดเจน
import { test, expect } from '../../../fixtures';
```

*(source: https://playwright.dev/docs/test-fixtures)*

**Mistake 5: ไม่แยก web test กับ API test ออกจากกัน**

❌ ทุกอย่างใน `tests/` เดียว — ไม่รู้ว่า test ไหน test อะไร

✅ แยก `tests/web/` และ `tests/api/` — ใช้ playwright projects config กรองได้ง่าย

```bash
# รัน API tests เท่านั้น (ไม่เปิด browser)
npx playwright test --project=api

# รัน web tests เท่านั้น
npx playwright test --project=web
```

*(source: https://playwright.dev/docs/test-projects)*

---

## 6. สรุปบท

ลองตอบคำถามต่อไปนี้ก่อนดูเฉลย:

**Q1:** ทำไม `helpers/` จึงไม่ควรมี Playwright imports? ข้อดีของการแยกออกคืออะไร?

**Q2:** คุณมี test ที่ทำ API call 3 ครั้ง แล้ว navigate ไปยัง UI เพื่อ verify — ควรใส่ test ไว้ใน `tests/web/` หรือ `tests/api/`? ทำไม?

**Q3:** เพื่อนบอกว่าควรสร้าง `utils/`, `support/`, `helpers/` ทั้งสามโฟลเดอร์แยกกัน เพราะแต่ละอย่างต่างกัน คุณจะแนะนำอย่างไร?

---

<details>
<summary>เฉลย</summary>

**A1:** ถ้า helpers ไม่มี Playwright imports — สามารถ test helpers ด้วย Jest หรือ Vitest ได้โดยตรง (unit test), reuse ใน script อื่น, และไม่ผูกกับ Playwright version สิ่งนี้เรียกว่า "pure function" ที่ predictable และ test ง่าย

**A2:** ใน `tests/web/` — เหตุผล: primary verification เกิดที่ UI (navigate + visual check) แม้จะมี API setup แต่ "what you're testing" คือ UI behavior API calls เป็นแค่ test data setup ถ้า primary verification เป็น API response ให้ใส่ใน `tests/api/`

**A3:** แนะนำให้ใช้ชื่อเดียว เช่น `helpers/` — `utils/` และ `support/` เป็น alias ของ concept เดียวกัน การมีทั้งสามทำให้ developer ใหม่ไม่รู้ว่าจะวางของที่ไหน และทำให้เกิด "ห้องเปล่า" ที่ไม่มีใครรู้ว่าควรใส่อะไร เลือกชื่อเดียวและ document อย่างชัดเจนว่าเก็บอะไร

</details>
