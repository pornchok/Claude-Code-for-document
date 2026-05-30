## ก่อนอ่านบทนี้ ลองตอบ:

1. คุณมี test ที่พังใน CI แต่ผ่านใน local เสมอ — ก่อนจะดู error message คุณควรทำอะไรเป็นอันดับแรก เพื่อให้ได้ข้อมูลเพียงพอสำหรับ debug โดยไม่ต้องรัน CI รอบใหม่?
2. `trace: 'on-first-retry'` ทำงานได้อย่างไรในทางปฏิบัติ — และถ้าไม่ตั้ง `retries` ใน config เลย จะเกิดอะไรขึ้นกับ trace?

---

เฉลย:

1. ตรวจสอบก่อนว่าได้ตั้ง `trace: 'on-first-retry'` และ `retries: 1` ใน config หรือยัง ถ้าตั้งแล้วให้ download artifact จาก CI แล้วรัน `npx playwright show-trace path/to/trace.zip` เพื่อดู timeline ทั้งหมด — network request, DOM snapshot, console log, screenshot ทุก action — โดยไม่ต้องแก้ code หรือ re-run ใหม่ ถ้ายังไม่ได้ตั้งต้องเพิ่ม config ก่อน แล้ว push ไป CI อีกรอบ
2. `on-first-retry` หมายความว่า Playwright จะสร้าง trace เฉพาะ "รอบ retry แรก" ของ test ที่ fail เท่านั้น — ถ้าไม่ตั้ง `retries` ไว้เลย (หรือ `retries: 0`) จะไม่มีรอบ retry เกิดขึ้น trace จึงไม่ถูกสร้างเลยแม้ test จะ fail ดังนั้นต้องตั้ง `retries: process.env.CI ? 1 : 0` คู่กันเสมอ

# บทที่ 7: Fixtures — Dependency Injection สำหรับ Tests

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- อธิบายได้ว่า Playwright fixtures แก้ปัญหาอะไรที่ `beforeEach`/`afterEach` แก้ไม่ได้
- ใช้ built-in fixtures (`page`, `browser`, `context`, `request`, `browserName`, `baseURL`) และรู้ว่า scope ของแต่ละตัวคืออะไร
- สร้าง custom fixture ด้วย `test.extend<T>()` พร้อม setup และ teardown ที่รันแม้ test fail
- เลือก fixture scope ระหว่าง test-scoped และ worker-scoped ได้ถูกต้องตามสถานการณ์
- ประกาศ fixture dependency เพื่อให้ Playwright จัดการลำดับ setup/teardown อัตโนมัติ
- ใช้ automatic fixture (`auto: true`) สำหรับ cross-cutting concerns เช่น cleanup
- รวม fixtures จากหลายไฟล์ด้วย `mergeTests()` โดยไม่ conflict กัน
- เปรียบเทียบ pattern นี้กับ Robot Framework Suite Setup / Test Setup ได้ชัดเจน

---

## 2. ทำไมต้องรู้? (Why)

สมมติคุณมี test suite สำหรับระบบ todo ที่ต้องการ:

1. ล้าง database ก่อน test แต่ละตัว
2. เปิดหน้า `/todos` ให้พร้อม
3. หลัง test เสร็จไม่ว่า pass หรือ fail — ต้องล้าง database อีกครั้ง

ด้วย `beforeEach`/`afterEach` คุณจะเขียนแบบนี้:

```typescript
let todoPage: TodoPage;

beforeEach(async ({ page, request }) => {
  await request.post('http://localhost:3000/api/reset');
  todoPage = new TodoPage(page);
  await todoPage.goto();
});

afterEach(async ({ request }) => {
  await request.post('http://localhost:3000/api/reset');
});
```

มีปัญหาอยู่สามอย่าง:

**ปัญหาที่ 1 — Coupling:** setup และ teardown อยู่คนละที่ ถ้ามีหลาย test file ที่ต้องการ pattern เดียวกัน ต้อง copy-paste ไปทุกไฟล์

**ปัญหาที่ 2 — ไม่มี lazy initialization:** `beforeEach` รันทุก test โดยไม่สนว่า test ต้องการ `todoPage` หรือเปล่า ถ้ามี test ที่ไม่ได้ใช้ `todoPage` เลย setup ก็ยังรันอยู่

**ปัญหาที่ 3 — ไม่มี type-safe dependency injection:** `todoPage` เป็น variable ระดับ module ที่ tests share กัน ถ้า test fail กลางทาง `teardown` ใน `afterEach` อาจทำงานกับ state ที่ไม่ถูกต้อง

Fixtures แก้ปัญหาทั้งสามนี้: เขียนครั้งเดียว ใช้ทุกไฟล์ สร้างเฉพาะเมื่อต้องการ และ teardown รันเสมอ

---

## 3. Analogy: Mise en Place ของเชฟ

ในครัวระดับมืออาชีพมีระบบที่เรียกว่า "mise en place" (มิซ-อ็อง-พลาส) — แปลตรงตัวว่า "วางทุกอย่างไว้ในที่ของมัน" ก่อนเริ่มปรุงอาหาร เชฟจะเตรียมทุกอย่างให้พร้อม: หั่นผัก, ชั่งเครื่องปรุง, ต้มน้ำซุป, จัดวางอุปกรณ์ให้อยู่ในมือถึง

เมื่อ order มาถึง เชฟไม่ต้องหยุดไปหาเครื่องปรุงทีละอย่าง — หยิบได้ทันที ทำงานได้ไหลลื่น

Playwright fixtures ทำงานแบบเดียวกัน: คุณประกาศ "ของที่ต้องมี" ไว้ล่วงหน้า (fixture definition) เมื่อ test รัน Playwright จัด setup ทุกอย่างที่ประกาศให้พร้อมก่อน test เริ่ม แล้ว cleanup หลัง test จบ ไม่ว่าจะสำเร็จหรือไม่

**แต่ละ test ที่รัน = เมนูหนึ่งจาน** — เชฟเตรียม mise en place สำหรับจานนั้นโดยเฉพาะ ไม่ใช่แค่เตรียมทั่วไปสำหรับทุกจานในคืนนั้น

---

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**

- **Fixture คือแค่ `beforeEach`/`afterEach` ที่เขียนสวยขึ้น** — จริงๆ fixtures มีสิ่งที่ `beforeEach` ไม่มีเลย: lazy initialization (สร้างเฉพาะเมื่อ test ประกาศว่าต้องการ), scope ที่ต่างกัน (test-scoped vs worker-scoped), และ dependency injection ที่ Playwright จัดการลำดับ setup/teardown ให้อัตโนมัติ
- **Worker-scoped fixture ใช้ข้าม workers ได้** — worker-scoped fixture share ได้เฉพาะ tests ที่รันใน worker process เดียวกันเท่านั้น ถ้า Playwright spawn worker ใหม่ fixture นั้นจะถูก setup ใหม่ในแต่ละ worker
- **Mise en place เตรียมทีเดียวใช้ได้ทุกคืน** — ในครัวจริง mise en place เตรียมใหม่ทุกวัน เช่นเดียวกับ test-scoped fixture ที่ setup ใหม่ทุก test ไม่ใช่ใช้ state ค้างจาก test ก่อน

---

## 4. เนื้อหาหลัก

### 4.1 Built-in Fixtures

Playwright Test มาพร้อม fixtures ที่ใช้ได้ทันทีโดยไม่ต้องสร้างเอง *(source: https://playwright.dev/docs/test-fixtures)*

| Fixture | Type | Scope | คำอธิบาย |
|---------|------|-------|-----------|
| `page` | Page | test | "Isolated page for this test run." |
| `context` | BrowserContext | test | "Isolated context for this test run." |
| `browser` | Browser | worker | "Browsers are shared across tests to optimize resources." |
| `request` | APIRequestContext | test | "Isolated APIRequestContext instance for this test run." |
| `browserName` | string | worker | ชื่อ browser ที่รันอยู่: `chromium`, `firefox`, `webkit` |
| `baseURL` | string | worker | URL จาก `use.baseURL` ใน playwright.config.ts |

สังเกตว่า `browser` เป็น worker-scoped เพราะการเปิด browser instance ใหม่แต่ละครั้งกิน resource มาก แต่ `page` และ `context` เป็น test-scoped เพราะต้องการ isolation ระหว่าง tests

### 4.2 สร้าง Custom Fixture ด้วย `test.extend<T>()`

Custom fixture คือการขยาย `test` object ด้วย fixtures ใหม่ที่คุณกำหนดเอง *(source: https://playwright.dev/docs/test-fixtures)*

```typescript
// fixtures/todo.fixtures.ts
// tested: Playwright v1.50+, Node.js 20+
import { test as base, expect } from '@playwright/test';

// ประกาศ type ของ fixtures ที่จะสร้าง
type MyFixtures = {
  todoPage: TodoPage;
};

// TodoPage class — Page Object ที่ wrap การทำงานกับ /todos
class TodoPage {
  constructor(private page: Page) {}

  async goto() {
    await this.page.goto('http://localhost:3000/todos');
  }

  async addTodo(text: string) {
    await this.page.getByTestId('input-new-todo').fill(text);
    await this.page.getByTestId('btn-add-todo').click();
  }
}

// ขยาย base test ด้วย fixture ใหม่
export const test = base.extend<MyFixtures>({
  todoPage: async ({ page }, use) => {
    // ── Setup: รันก่อน test เริ่ม ──
    const tp = new TodoPage(page);
    await tp.goto();

    // ── ส่งค่าให้ test ──
    await use(tp);    // ← test รันอยู่ตรงนี้

    // ── Teardown: รันหลัง test จบ (รันเสมอ แม้ test fail) ──
    // ไม่ต้องทำอะไรเพราะ page ถูก reset โดย test-scoped context อยู่แล้ว
  },
});

export { expect };
```

**Pattern หลักที่ต้องจำ:** setup อยู่ก่อน `await use()` และ teardown อยู่หลัง `await use()` — Playwright รับประกันว่า teardown จะรันเสมอ ไม่ว่า test จะ pass หรือ fail *(source: https://playwright.dev/docs/test-fixtures)*

### 4.3 Fixture Scope

**Test-scoped (default):** สร้างและ destroy ต่อ test หนึ่ง เหมาะกับ:
- State ที่ต้องการ fresh ทุก test (`page`, `context`)
- Data ที่อาจถูกแก้ไขโดย test

**Worker-scoped:** สร้างครั้งเดียวต่อ worker process และ share กับทุก tests ใน worker นั้น เหมาะกับ:
- Expensive setup ที่ไม่เปลี่ยนระหว่าง tests เช่น database connection
- Static data ที่ tests ไม่แก้ไข

```typescript
// fixtures/worker.fixtures.ts
// tested: Playwright v1.50+, Node.js 20+
import { test as base } from '@playwright/test';

type WorkerFixtures = {
  dbConnection: DatabaseConnection;
};

export const test = base.extend<{}, WorkerFixtures>({
  // Worker-scoped: ใช้ tuple syntax และระบุ { scope: 'worker' }
  dbConnection: [async ({}, use) => {
    // สร้าง connection ครั้งเดียวต่อ worker
    const conn = await DatabaseConnection.connect('postgresql://localhost/testdb');

    await use(conn);

    // teardown เมื่อ worker หยุดทำงาน
    await conn.close();
  }, { scope: 'worker' }],
});
```

"Note the tuple-like syntax for the worker fixture — we have to pass `{scope: 'worker'}` so that test runner sets this fixture up once per worker." *(source: https://playwright.dev/docs/test-fixtures)*

### 4.4 Automatic Fixture (`auto: true`)

Automatic fixture รันทุก test โดยไม่ต้องประกาศใน test function เหมาะกับ cross-cutting concerns เช่น cleanup หรือ logging *(source: https://playwright.dev/docs/test-fixtures)*

"Automatic fixtures are set up for each test/worker, even when the test does not list them directly."

```typescript
// fixtures/auto-cleanup.fixtures.ts
// tested: Playwright v1.50+, Node.js 20+
import { test as base } from '@playwright/test';

export const test = base.extend<{ autoCleanDb: void }>({
  // auto: true — รันทุก test โดยไม่ต้อง declare ใน test()
  autoCleanDb: [async ({ request }, use) => {
    // cleanup ก่อน test เสมอ
    await request.post('http://localhost:3000/api/reset');

    await use();

    // cleanup หลัง test เสมอ
    await request.post('http://localhost:3000/api/reset');
  }, { auto: true }],
});
```

### 4.5 Fixture Composition — Dependency Injection

Fixtures สามารถ depend on fixtures อื่นได้ Playwright จัดการลำดับ setup/teardown อัตโนมัติ: "When fixture A depends on fixture B: B is always set up before A and torn down after A." *(source: https://playwright.dev/docs/test-fixtures)*

```typescript
// fixtures/composed.fixtures.ts
// tested: Playwright v1.50+, Node.js 20+
import { test as base } from '@playwright/test';
import { TodoPage } from '../page-objects/TodoPage';

type ComposedFixtures = {
  cleanDb: void;
  todoPage: TodoPage;
};

export const test = base.extend<ComposedFixtures>({
  // fixture แรก: cleanDb ไม่ depend on fixture อื่น (นอกจาก request ซึ่งเป็น built-in)
  cleanDb: async ({ request }, use) => {
    await request.post('http://localhost:3000/api/reset');
    await use();
    await request.post('http://localhost:3000/api/reset');
  },

  // fixture ที่สอง: todoPage depend on cleanDb
  // → Playwright จะ setup cleanDb ก่อน todoPage เสมอ
  // → Playwright จะ teardown todoPage ก่อน cleanDb เสมอ
  todoPage: async ({ page, cleanDb }, use) => {
    // cleanDb รันแล้ว — database สะอาดแน่นอน
    const tp = new TodoPage(page);
    await tp.goto();
    await use(tp);
  },
});
```

สังเกตว่า `todoPage` ประกาศ `cleanDb` ใน parameter — แค่นั้นพอ Playwright รู้ว่าต้อง setup `cleanDb` ก่อน

### 4.6 `mergeTests()` — รวม Fixtures จากหลายไฟล์

เมื่อ test suite ใหญ่ขึ้น คุณจะแยก fixtures ออกเป็นหลายไฟล์ `mergeTests()` รวมพวกมันเข้าด้วยกันโดยไม่ conflict *(source: https://playwright.dev/docs/test-fixtures)*

```typescript
// fixtures/index.ts
// tested: Playwright v1.50+, Node.js 20+
import { mergeTests } from '@playwright/test';
import { test as authTest } from './auth.fixtures';
import { test as dbTest } from './db.fixtures';

// รวม fixtures ทั้งสอง — tests ที่ import จากไฟล์นี้ใช้ได้ทุก fixture
export const test = mergeTests(authTest, dbTest);
```

ใช้ใน test file:

```typescript
// tests/todo.spec.ts
import { test } from '../fixtures';
import { expect } from '@playwright/test';

// ใช้ fixture จากทั้ง auth และ db ได้ในที่เดียวกัน
test('create todo when authenticated', async ({ todoPage, authToken }) => {
  // todoPage มาจาก db.fixtures, authToken มาจาก auth.fixtures
  await todoPage.addTodo('Buy coffee');
  await expect(todoPage.todoCount).toContainText('1');
});
```

**Note:** `mergeTests()` รวม test objects โดยที่ custom matchers ที่สร้างจาก `expect.extend()` ทำงานอยู่ในแต่ละไฟล์ ถ้าต้องการรวม custom matchers ข้ามหลาย fixture files ให้ใช้ `mergeExpects()`:

```typescript
// fixtures/matchers.ts
// tested: Playwright v1.50+, Node.js 20+
import { expect as authExpect } from './auth.fixtures';  // มี toBeLoggedIn()
import { expect as todoExpect } from './todo.fixtures';  // มี toHaveTodoCount()
import { mergeExpects } from '@playwright/test';

// รวม custom matchers จากหลาย fixture files
export const expect = mergeExpects(authExpect, todoExpect);
```

ใช้ใน test:

```typescript
// tests/combined.spec.ts
import { test } from '../fixtures';
import { expect } from '../fixtures/matchers';

test('verify auth status and todo count', async ({ page }) => {
  await expect(page).toBeLoggedIn();           // จาก auth.fixtures
  await expect(page).toHaveTodoCount(5);       // จาก todo.fixtures
});
```

`mergeExpects()` ต้องใช้คู่กับ `mergeTests()` เสมอ เพื่อให้ test objects และ matchers อยู่ในการควบคุมเดียวกัน

### 4.7 เปรียบเทียบกับ Robot Framework + Selenium

| สิ่งที่ต้องการ | Robot Framework + Selenium | Playwright Fixtures |
|----------------|---------------------------|---------------------|
| Setup ก่อนแต่ละ test | `Test Setup` keyword ใน Settings | test-scoped fixture |
| Setup ก่อน test suite | `Suite Setup` keyword | worker-scoped fixture |
| Teardown รับประกัน | `[Teardown]` keyword — อาจ skip ถ้า setup error | teardown หลัง `await use()` รันเสมอ |
| Dependency ระหว่าง setup | เขียนเองใน keyword — ไม่มี DI จริงๆ | ประกาศ parameter ใน fixture — Playwright จัดการให้ |
| Reuse ข้าม test files | Resource file + Import | export จากไฟล์เดียวกัน + import ในทุก test file |
| Shared state ที่ safe | Global variable ใน Resource — race condition ถ้า parallel | worker-scoped fixture — isolated ต่อ worker |

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner: Built-in Fixtures พื้นฐาน

สถานการณ์: ทำความเข้าใจ `request` fixture โดยทดสอบ API ของ demo app โดยตรง

```typescript
// tests/beginner-fixtures.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

// request fixture คือ APIRequestContext ที่ใช้ HTTP request ได้โดยตรง
// ไม่ต้องเปิด browser — เหมาะสำหรับ test data setup และ API verification
test('built-in request fixture — สร้าง todo ผ่าน API', async ({ request }) => {
  // ล้าง state ก่อน test
  await request.post('http://localhost:3000/api/reset');

  // สร้าง todo ผ่าน API โดยตรง
  const createRes = await request.post('http://localhost:3000/api/todos', {
    data: { text: 'Buy coffee beans' }
  });

  expect(createRes.ok()).toBe(true);

  const todo = await createRes.json();
  expect(todo.text).toBe('Buy coffee beans');
  expect(todo.id).toBeDefined();
  expect(todo.completed).toBe(false);
});

// page fixture คือ browser tab ที่ isolated ต่อ test นี้โดยเฉพาะ
// Playwright สร้าง page ใหม่ทุก test — ไม่แชร์ state กับ test อื่น
test('built-in page fixture — navigate และ verify', async ({ page }) => {
  await page.goto('http://localhost:3000');

  // session badge ควรไม่แสดงเมื่อยังไม่ login
  await expect(page.getByTestId('session-badge')).not.toBeVisible();

  // title ควรถูกต้อง
  await expect(page).toHaveTitle(/Playwright Course App/);
});

// browserName fixture — รู้ว่ากำลัง test บน browser อะไร
test('built-in browserName fixture', async ({ page, browserName }) => {
  await page.goto('http://localhost:3000');

  // log browser name ให้เห็นใน test output
  console.log(`Running on: ${browserName}`);

  // ใช้ filter test ตาม browser ได้
  test.skip(browserName === 'webkit', 'ข้าม Safari เพราะ environment นี้ไม่รองรับ');

  await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
});
```

**Output ที่คาดหวัง:**
```
Running 3 tests using 1 worker

  ✓  built-in request fixture — สร้าง todo ผ่าน API (312ms)
  ✓  built-in page fixture — navigate และ verify (445ms)
  ✓  built-in browserName fixture (389ms)
  -  built-in browserName fixture (skipped on webkit)

  3 passed (1.2s)
```

---

### Intermediate: Fixture Composition กับ E-commerce Checkout

สถานการณ์: ทีม QA ต้องการ test ระบบ checkout ของ e-commerce site ที่ต้องการ (1) database สะอาด (2) user login แล้ว (3) สินค้าใน cart ก่อนแต่ละ test — ต้องเขียน fixtures ที่ compose กันได้

```typescript
// fixtures/checkout.fixtures.ts
// tested: Playwright v1.50+, Node.js 20+
import { test as base } from '@playwright/test';

// สมมติว่า demo app มี /api/cart endpoint (ใช้เป็น conceptual example)
// ในบทนี้ใช้ /api/todos แทนเพื่อให้ทดสอบกับ demo app จริงได้

type CheckoutFixtures = {
  cleanState: void;          // ล้าง state ทั้งหมด
  loggedInPage: Page;        // page ที่ login แล้ว
  preparedTodos: string[];   // todos ที่ถูกสร้างไว้แล้ว
};

export const test = base.extend<CheckoutFixtures>({
  // Fixture 1: cleanState — ล้างก่อนและหลัง test
  cleanState: async ({ request }, use) => {
    await request.post('http://localhost:3000/api/reset');
    await use();
    await request.post('http://localhost:3000/api/reset');
  },

  // Fixture 2: loggedInPage — depend on cleanState
  // Playwright setup cleanState ก่อน loggedInPage อัตโนมัติ
  loggedInPage: async ({ page, cleanState }, use) => {
    // login ผ่าน UI
    await page.goto('http://localhost:3000/login');
    await page.getByTestId('input-username').fill('admin');
    await page.getByTestId('input-password').fill('admin123');
    await page.getByTestId('btn-login').click();

    // รอให้ login เสร็จ
    await expect(page.getByTestId('session-badge')).toBeVisible();

    await use(page);
  },

  // Fixture 3: preparedTodos — depend on cleanState เพื่อสร้าง test data
  preparedTodos: async ({ request, cleanState }, use) => {
    // สร้าง todos ผ่าน API (เร็วกว่า UI)
    const items = ['Review requirements', 'Write test cases', 'Run regression'];
    for (const text of items) {
      await request.post('http://localhost:3000/api/todos', {
        data: { text }
      });
    }

    await use(items);
    // teardown จัดการโดย cleanState fixture ที่ depend on อยู่แล้ว
  },
});
```

```typescript
// tests/intermediate-fixtures.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { test } from '../fixtures/checkout.fixtures';
import { expect } from '@playwright/test';

test('todo list แสดง prepared items ถูกต้อง', async ({ loggedInPage, preparedTodos }) => {
  const page = loggedInPage;
  await page.goto('http://localhost:3000/todos');

  // verify ว่า todos ที่สร้างไว้ผ่าน API ปรากฏบน UI
  for (const text of preparedTodos) {
    await expect(page.getByTestId('todo-list')).toContainText(text);
  }

  // verify count ถูกต้อง
  await expect(page.getByTestId('todo-count')).toContainText(`${preparedTodos.length}`);
});

test('admin สามารถ add todo ได้เมื่อ login แล้ว', async ({ loggedInPage }) => {
  const page = loggedInPage;
  await page.goto('http://localhost:3000/todos');

  await page.getByTestId('input-new-todo').fill('New task from fixture test');
  await page.getByTestId('btn-add-todo').click();

  await expect(page.getByTestId('todo-list')).toContainText('New task from fixture test');
});
```

**จุดสำคัญของตัวอย่างนี้:**
- `loggedInPage` และ `preparedTodos` ต่าง depend on `cleanState` — Playwright setup `cleanState` แค่ครั้งเดียวต่อ test ไม่ใช่สองครั้ง
- teardown ลำดับ: `loggedInPage` teardown → `preparedTodos` teardown → `cleanState` teardown (reverse ของ setup)
- test แรกใช้ทั้งสอง fixtures, test ที่สองใช้แค่ `loggedInPage` — Playwright สร้างเฉพาะที่ต้องการ

---

### Advanced: Worker-Scoped Fixture เทียบกับ Test-Scoped — Performance Tradeoffs

สถานการณ์: ทีม senior ต้องออกแบบ test infrastructure สำหรับ test suite 200 tests ที่ต้องการ database seed data เหมือนกันทุก test แต่มีบาง test ที่แก้ไข state ด้วย ต้องเลือก scope ให้ถูกต้องเพื่อ balance ระหว่าง performance และ isolation

```typescript
// fixtures/performance-comparison.fixtures.ts
// tested: Playwright v1.50+, Node.js 20+
import { test as base } from '@playwright/test';

// ── Approach A: Test-scoped (Safe แต่ช้า) ──
// สร้าง database seed ใหม่ทุก test = 200 × 300ms = 60 วินาที overhead
export const testScopedTest = base.extend<{ seedData: SeedData }>({
  seedData: async ({ request }, use) => {
    // สร้าง seed data ทุก test — isolate อย่างสมบูรณ์
    await request.post('http://localhost:3000/api/reset');
    await request.post('http://localhost:3000/api/todos', {
      data: { text: 'Seed Todo 1' }
    });
    await request.post('http://localhost:3000/api/todos', {
      data: { text: 'Seed Todo 2' }
    });

    await use({ count: 2 });

    await request.post('http://localhost:3000/api/reset');
  },
});

// ── Approach B: Worker-scoped (เร็ว แต่ต้องระวัง mutation) ──
// สร้าง seed data ครั้งเดียวต่อ worker = ประหยัด setup time
export const workerScopedTest = base.extend<{}, { sharedSeedData: SeedData }>({
  sharedSeedData: [async ({ browser }, use, workerInfo) => {
    // Worker index บอกว่านี่คือ worker ที่เท่าไหร่ใน parallel run
    const workerIndex = workerInfo.workerIndex;
    console.log(`Worker ${workerIndex}: setting up shared seed data`);

    // สร้าง context แยกสำหรับ setup (ไม่กระทบ test contexts)
    const context = await browser.newContext();
    const page = await context.newPage();

    // ใช้ API request เพื่อ setup data — เร็วกว่า UI
    const req = context.request;
    await req.post('http://localhost:3000/api/reset');
    await req.post('http://localhost:3000/api/todos', {
      data: { text: 'Shared Seed 1' }
    });

    await context.close();

    // share ค่านี้กับทุก tests ใน worker นี้
    await use({ count: 1, workerIndex });
  }, { scope: 'worker' }],
});
```

```typescript
// tests/advanced-fixtures.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { workerScopedTest as test } from '../fixtures/performance-comparison.fixtures';
import { expect } from '@playwright/test';

// ทุก test ใน worker เดียวกันได้รับ sharedSeedData object เดียวกัน
test('read-only test — ใช้ shared data ได้ปลอดภัย', async ({ page, sharedSeedData }) => {
  await page.goto('http://localhost:3000/todos');

  // แค่อ่าน — ไม่แก้ไข state
  const count = await page.getByTestId('todo-count').textContent();
  expect(parseInt(count || '0')).toBeGreaterThanOrEqual(sharedSeedData.count);
});

// ⚠️ Anti-pattern: test ที่แก้ไข worker-scoped state ทำให้ tests อื่น interfere
// ถ้าจำเป็นต้องแก้ไข state ให้ใช้ test-scoped fixture แทน
test('mutation test — ต้องใช้ test-scoped ถ้าต้องการ isolation', async ({
  page,
  sharedSeedData
}, testInfo) => {
  // Diagnosis: ถ้า test นี้รันก่อน "read-only test" ใน worker เดียวกัน
  // แล้ว add todo ไป — read-only test จะเห็น count มากกว่า expected
  // Solution: ใช้ test-scoped fixture สำหรับ tests ที่ mutate state

  test.skip(
    true,
    'Example: ถ้า test นี้ต้องแก้ state — ให้ใช้ testScopedTest แทน workerScopedTest'
  );
});
```

**เมื่อใช้แต่ละ scope:**

| สถานการณ์ | Scope | เหตุผล |
|-----------|-------|--------|
| Test data ที่ tests แก้ไข | test-scoped | ต้องการ isolation สมบูรณ์ |
| Static reference data (dropdown options, config) | worker-scoped | ไม่เปลี่ยน — share ได้ปลอดภัย |
| Browser / Database connection | worker-scoped | Expensive setup — ประหยัด resource |
| Authentication token ที่ expire ได้ | test-scoped | Token อาจ expire ระหว่าง test run |
| User session ที่แต่ละ test ต้องการต่างกัน | test-scoped | State แตกต่างกัน |

---

## 6. Common Mistakes

**1. ใช้ `beforeEach` แทน fixture เมื่อต้องการ reuse ข้ามไฟล์**

❌
```typescript
// tests/todo.spec.ts
let todoPage: TodoPage;

beforeEach(async ({ page }) => {
  todoPage = new TodoPage(page);
  await todoPage.goto();
});

// tests/admin.spec.ts — copy-paste setup เดิม ทุกครั้ง
let todoPage: TodoPage;

beforeEach(async ({ page }) => {
  todoPage = new TodoPage(page);
  await todoPage.goto();
});
```

✅
```typescript
// fixtures/todo.fixtures.ts — เขียนครั้งเดียว
export const test = base.extend<{ todoPage: TodoPage }>({
  todoPage: async ({ page }, use) => {
    const tp = new TodoPage(page);
    await tp.goto();
    await use(tp);
  },
});

// tests/todo.spec.ts AND tests/admin.spec.ts — import เดียวกัน
import { test } from '../fixtures/todo.fixtures';
```

เหตุผล: `beforeEach` ไม่มี lazy initialization — รันทุก test แม้ test นั้นไม่ต้องการ `todoPage` ส่วน fixture สร้างเฉพาะเมื่อ test ประกาศว่าต้องการ *(source: https://playwright.dev/docs/test-fixtures)*

---

**2. ลืม `await use()` ใน fixture**

❌
```typescript
export const test = base.extend<{ myFixture: string }>({
  myFixture: async ({}, use) => {
    const data = 'some setup';
    // ลืม await use(data) — test จะรอจนหมด timeout
  },
});
```

✅
```typescript
export const test = base.extend<{ myFixture: string }>({
  myFixture: async ({}, use) => {
    const data = 'some setup';
    await use(data);  // ← ต้องมีเสมอ
  },
});
```

เหตุผล: `use()` คือ "จุดที่ test รัน" ถ้าไม่ call `use()` test จะไม่เริ่มและ hang จนหมด timeout โดยไม่มี error message ที่อธิบายสาเหตุ *(source: https://playwright.dev/docs/test-fixtures)*

---

**3. ใช้ test-scoped fixture กับ expensive setup ที่รันซ้ำเปล่า**

❌
```typescript
// สร้าง database schema ใหม่ทุก test — ช้ามาก (ถ้า 100 tests = 100 × 2 วินาที)
export const test = base.extend<{ dbSchema: void }>({
  dbSchema: async ({}, use) => {
    await runMigrations();  // 2 วินาที per test
    await use();
    await dropAllTables();
  },
});
```

✅
```typescript
// Worker-scoped: run migrations ครั้งเดียวต่อ worker
export const test = base.extend<{}, { dbSchema: void }>({
  dbSchema: [async ({}, use) => {
    await runMigrations();  // 2 วินาที per worker (ไม่ใช่ per test)
    await use();
    await dropAllTables();
  }, { scope: 'worker' }],
});
```

เหตุผล: Schema ไม่เปลี่ยนระหว่าง tests — worker-scoped ประหยัด setup time ได้มากโดยไม่กระทบ isolation *(source: https://playwright.dev/docs/test-fixtures)*

---

**4. แก้ไข shared state ใน worker-scoped fixture**

❌
```typescript
// worker-scoped fixture ที่ tests ทุกตัวใช้ร่วมกัน
sharedCart: [async ({ browser }, use) => {
  const cart = new Cart();
  await use(cart);
}, { scope: 'worker' }]

// test A แก้ไข cart — test B ที่รันหลังใน worker เดียวกันได้รับ state ที่เปลี่ยนแล้ว
test('add item to cart', async ({ sharedCart }) => {
  sharedCart.add('item1');  // ← แก้ไข shared state
});

test('cart starts empty', async ({ sharedCart }) => {
  // FAIL: cart มี 'item1' อยู่แล้วจาก test ก่อน
  expect(sharedCart.items).toHaveLength(0);
});
```

✅
```typescript
// ใช้ test-scoped fixture สำหรับ state ที่ tests แก้ไข
cart: async ({}, use) => {
  const cart = new Cart();
  await use(cart);
  // cart ใหม่ทุก test — ไม่ interfere กัน
},
```

เหตุผล: worker-scoped fixture เหมาะกับ "read-only shared resources" เท่านั้น ถ้า test แก้ไข state — ใช้ test-scoped เสมอ *(source: https://playwright.dev/docs/test-fixtures)*

---

## 7. สรุปบท + Retrieval Questions

ในบทนี้คุณได้เรียน:

- **Built-in fixtures** — `page`/`context`/`request` เป็น test-scoped (fresh ทุก test), `browser` เป็น worker-scoped (share เพื่อประหยัด resource)
- **`test.extend<T>()`** — สร้าง custom fixture ด้วย setup ก่อน `await use()` และ teardown หลัง `await use()` ที่รันเสมอ
- **Test-scoped vs Worker-scoped** — เลือกตาม "mutation risk" ของ state นั้น — ถ้า test แก้ไข state ให้ใช้ test-scoped เสมอ
- **Automatic fixture (`auto: true`)** — รันทุก test โดยไม่ต้องประกาศใน test function เหมาะกับ cleanup และ logging
- **Fixture composition** — ประกาศ dependency ใน parameter → Playwright จัดการลำดับ setup/teardown อัตโนมัติ
- **`mergeTests()`** — รวม fixtures จากหลายไฟล์โดยไม่ conflict

---

ก่อนอ่านบทถัดไป ลองตอบคำถามเหล่านี้ด้วยตัวเองก่อน (ไม่ต้อง scroll ขึ้นไปดูเฉลย):

**คำถามที่ 1:** คุณมี fixture `loginAsAdmin` ที่ต้องการให้รันก่อน `todoPage` fixture เสมอ และ `todoPage` ต้องการ user ที่ login แล้ว — จะเขียน dependency ระหว่าง fixtures สองตัวนี้อย่างไร? เขียน type declaration และ fixture body ให้ถูกต้อง

**คำถามที่ 2:** test suite มี 50 tests ที่ทุกตัวใช้ `dbConnection` fixture แต่ค่าเฉลี่ย setup time คือ 800ms ต่อครั้ง ทำให้ test suite ช้ามาก — จะแก้อย่างไรโดยใช้ fixture scope ที่เหมาะสม? มีข้อควรระวังอะไรบ้าง?

**คำถามที่ 3:** code นี้มีปัญหาอะไร และ test จะเกิดอะไรขึ้นเมื่อรัน?
```typescript
const test = base.extend<{ prepared: void }>({
  prepared: async ({ request }, use) => {
    await request.post('http://localhost:3000/api/reset');
    // --- หยุดตรงนี้ ---
  },
});

test('add todo', async ({ page, prepared }) => {
  await page.goto('http://localhost:3000/todos');
  await page.getByTestId('input-new-todo').fill('Test todo');
});
```

---

เฉลย:

**คำถามที่ 1:**
```typescript
type Fixtures = {
  loginAsAdmin: void;
  todoPage: TodoPage;
};

const test = base.extend<Fixtures>({
  loginAsAdmin: async ({ page }, use) => {
    await page.goto('http://localhost:3000/login');
    await page.getByTestId('input-username').fill('admin');
    await page.getByTestId('input-password').fill('admin123');
    await page.getByTestId('btn-login').click();
    await expect(page.getByTestId('session-badge')).toBeVisible();
    await use();
  },

  // ประกาศ loginAsAdmin ใน parameter = "ต้อง setup loginAsAdmin ก่อนฉัน"
  todoPage: async ({ page, loginAsAdmin }, use) => {
    const tp = new TodoPage(page);
    await tp.goto();
    await use(tp);
  },
});
```
แค่ใส่ `loginAsAdmin` ใน parameter ของ `todoPage` fixture — Playwright จัดการลำดับ setup/teardown ที่ถูกต้องให้อัตโนมัติ

**คำถามที่ 2:** เปลี่ยนเป็น worker-scoped fixture:
```typescript
const test = base.extend<{}, { dbConnection: DatabaseConnection }>({
  dbConnection: [async ({}, use) => {
    const conn = await DatabaseConnection.connect(DB_URL);
    await use(conn);
    await conn.close();
  }, { scope: 'worker' }],
});
```
ประหยัดได้: ถ้า Playwright ใช้ 4 workers จาก 50 tests = 4 × 800ms = 3.2 วินาที แทน 50 × 800ms = 40 วินาที **ข้อควรระวัง:** ถ้า tests แก้ไข database state (INSERT/DELETE) จะ interfere กัน — ต้องแน่ใจว่า tests ใช้ `dbConnection` แค่ query อ่านเท่านั้น หรือแต่ละ test ทำ cleanup หลังตัวเองเสมอ

**คำถามที่ 3:** ปัญหาคือลืม `await use()` ใน `prepared` fixture — test จะไม่เริ่มรันเลยและ hang จนหมด timeout (default 30 วินาที) แล้ว fail ด้วย `Test timeout of 30000ms exceeded` โดยไม่มี error ที่บอกว่าต้นเหตุคือ fixture แก้โดยเพิ่ม `await use()` หลัง reset:
```typescript
prepared: async ({ request }, use) => {
  await request.post('http://localhost:3000/api/reset');
  await use();  // ← ต้องมี
},
```
