## ก่อนอ่านบทนี้ ลองตอบ:

1. ใน GitHub Actions workflow สำหรับ Playwright — `forbidOnly: !!process.env.CI` มีไว้ทำอะไร และถ้าไม่ใส่ค่านี้จะเกิดปัญหาอะไรบน CI?

2. การรัน Playwright ด้วย sharding ใน CI และการ merge blob reports เป็น HTML report เดียวต้องทำขั้นตอนอะไรบ้าง และทำไมถึงต้องมีขั้นตอน merge แยก?

---

เฉลย:

1. `forbidOnly` ป้องกันไม่ให้ `test.only()` ทำให้ CI รันแค่ test เดียวโดยไม่ตั้งใจ — developer อาจลืมลบ `.only` ก่อน push ซึ่งทำให้ CI ผ่านทั้งที่รันแค่ 1 test ถ้าไม่ใส่ `forbidOnly` CI จะยอมรับ `.only` และ merge code ที่ test suite จริงๆ ไม่ได้รันผ่านเข้าไปได้

2. แต่ละ shard generate `blob-report/` ของตัวเอง — ต้อง download artifacts จากทุก shard มา merge ด้วย `npx playwright merge-reports --reporter html ./all-blob-reports` เหตุผลที่ต้องมี job แยกคือแต่ละ shard รันบน machine คนละเครื่อง ไม่มี machine เดียวที่รู้ผลของทุก shard ในขณะที่กำลังรัน จึงต้อง collect artifacts ก่อนแล้วค่อย merge ทีหลัง

---

# บทที่ 18: Production Patterns

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- จัดการ flaky tests ได้อย่างเป็นระบบ — แยกระหว่าง retry เพื่อ stability กับ root cause analysis ที่ต้องแก้จริง
- ใช้ `test.fixme()`, `test.fail()`, `test.info().attach()` ได้ถูกต้องตามสถานการณ์
- สร้าง Test Data Factory pattern เพื่อ isolate state ระหว่าง tests ที่รัน parallel
- organize test suite ขนาดใหญ่ด้วย feature folders และ tagging strategy
- เพิ่ม custom reporter สำหรับ notification และ monitoring
- รู้ common mistakes ที่ทำให้ production test suite พัง — และวิธีแก้ที่ถูกต้อง

---

## 2. ทำไมต้องรู้? (Why)

สถานการณ์นี้เกิดบ่อยมากในทีม: test suite รันผ่านบน local แต่ fail บน CI ด้วยสาเหตุที่ไม่แน่นอน — บางครั้งผ่าน บางครั้งไม่ผ่าน แก้แล้วก็กลับมาเป็นเหมือนเดิม

นี่คือ "flaky test" — ศัตรูตัวฉกาจของทีม เพราะ:
- ทีมหยุดเชื่อ CI ("fail อีกแล้ว ก็แค่ retry ไป")
- developer merge code โดยไม่แน่ใจว่า test ผ่านจริงหรือแค่โชค
- debugging ยากเพราะ error ไม่ reproduce ซ้ำ

แต่ปัญหา flaky test เป็นแค่ส่วนหนึ่งของโจทย์ "production test suite" ที่ใหญ่กว่า ทีมที่ scale ขึ้นถึง 200-500 tests ยังเจอ:
- state ปนกันระหว่าง tests ที่รัน parallel
- test data ที่ไม่ clean ทำให้ test fail แบบ intermittent
- report ที่ไม่มีประโยชน์ — fail แล้วไม่รู้ว่าเกิดอะไร
- organization ที่ยุ่งเหยิง — ไม่รู้ว่า test ไหนทดสอบ feature อะไร

บทนี้รวบรวม patterns ที่ทีม Playwright-scale ใช้จริงใน production เพื่อให้ test suite ของคุณ **เชื่อถือได้, maintain ได้, และ debug ได้** แม้จะ scale ขึ้นเรื่อยๆ

---

## 3. Analogy

**Production test suite เหมือนร้านอาหาร Michelin — ไม่ใช่แค่อาหารอร่อย แต่ทุก process ต้องสม่ำเสมอ, ทำซ้ำได้, และกู้คืนได้เมื่อเกิดปัญหา**

ร้านอาหาร Michelin ไม่ได้ดีเพราะ chef ฝีมือเก่งคนเดียว — แต่เพราะมี **system** ที่ทำให้ทุก service ออกมาเหมือนกัน ไม่ว่าจะเป็นคืนไหน chef คนไหนทำ:

- **Recipe ที่ precise** = test ที่เขียนชัดเจน ไม่ขึ้นกับ state จากภายนอก
- **Mise en place** (เตรียมวัตถุดิบก่อนปรุง) = Test Data Factory — เตรียม data สะอาดให้ทุก test ก่อนรัน
- **Station แยกกัน** (pastry, grill, sauce) = feature folders แยกตาม domain ไม่ใช่ layer
- **ระบบ QC ทุก plate ก่อนออก** = retry + trace เพื่อจับ error ก่อนถึง customer
- **Incident log** เมื่อมีของหาย = `test.info().attach()` บันทึก artifact เมื่อ test fail

⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:

- **retry แก้ flaky test ได้** — retry เหมือน chef ทำจาน 2 ถ้าจานแรกไม่ผ่าน QC แต่ถ้า ingredient เน่า ทำกี่จานก็เน่าเหมือนกัน — retry ช่วยให้ CI ผ่านชั่วคราว แต่ไม่ได้แก้ root cause จริง ต้องหาว่า "ingredient" (เช่น timing, shared state, external dependency) ไหนที่ทำให้ test ไม่ stable
- **code coverage = test quality** — ร้านที่ test taste ทุกจาน แต่แค่ลิ้มดูว่า "กินได้" ไม่ใช่ Michelin quality 100% coverage ด้วย bad tests (ไม่มี assertion จริง, แค่ไม่ throw error) แย่กว่า 60% coverage ด้วย tests ที่ verify behavior จริงๆ

---

## 4. เนื้อหาหลัก

### 4.1 Flaky Test Strategies

**Flaky test** คือ test ที่บางครั้งผ่าน บางครั้งไม่ผ่านโดยไม่มี code เปลี่ยน สาเหตุหลักมี 3 อย่าง:

1. **Timing issues** — page ยังโหลดไม่เสร็จแต่ test กด button แล้ว
2. **Shared state** — tests รัน parallel แล้ว state ปนกัน (เช่น test A สร้าง todo แต่ test B ลบทั้งหมด)
3. **External dependency** — network ช้า, third-party API ไม่ stable

**กลยุทธ์จัดการ flaky tests มีลำดับความสำคัญ:**

**ขั้นที่ 1 — ใช้ Web-First Assertions (แก้ที่ต้นเหตุ timing)**

Playwright documentation ระบุว่า `toBeVisible()`, `toHaveText()`, และ assertion อื่นๆ จะ wait และ retry อัตโนมัติก่อน fail — ไม่ต้องใช้ `waitForTimeout()` เด็ดขาด *(source: playwright.dev/docs/best-practices)*

```typescript
// ❌ flaky — ตรวจทันทีโดยไม่ wait
const isVisible = await page.locator('.toast').isVisible();
expect(isVisible).toBe(true);

// ✅ web-first — wait และ retry อัตโนมัติ
await expect(page.locator('.toast')).toBeVisible();
// tested: Playwright v1.50+, Node.js 20+
```

**ขั้นที่ 2 — Retry เพื่อ stability ระหว่างแก้ root cause**

เมื่อพบ flaky test ที่ยังไม่ได้แก้ ตั้ง retry ให้ทั้ง suite ใน CI:

```typescript
// playwright.config.ts
// tested: Playwright v1.50+, Node.js 20+
import { defineConfig } from '@playwright/test';

export default defineConfig({
  retries: process.env.CI ? 2 : 0,  // retry แค่บน CI ไม่ใช่ local
  use: {
    trace: 'on-first-retry',  // เก็บ trace เมื่อ retry — เห็น root cause ได้
  },
});
```

`trace: 'on-first-retry'` สำคัญมาก — เปิด trace เฉพาะเมื่อ test fail แล้ว retry ไม่เปิดตลอดเพราะ overhead สูง แต่เมื่อ retry เกิดขึ้นจะมี trace ให้ดูว่าเกิดอะไรขึ้นใน attempt แรกที่ fail

Override retry สำหรับ test เดี่ยวได้:

```typescript
test('unstable external payment', { retries: 3 }, async ({ page }) => {
  // test ที่ขึ้นกับ third-party ที่ยังแก้ไม่ได้
});
// tested: Playwright v1.50+, Node.js 20+
```

**ขั้นที่ 3 — Detect flaky tests ก่อน push ด้วย `--repeat-each`**

```bash
# รัน test ซ้ำ 5 รอบเพื่อหา flakiness บน local
npx playwright test --repeat-each=5 tests/checkout.spec.ts
```

ถ้า test ผ่านแค่ 3 ใน 5 รอบ — มัน flaky ต้องแก้ก่อน push ไม่ใช่รอให้ CI พบ

**ข้อจำกัดสำคัญของ retry:**

Retry ปลอดภัยสำหรับ read operations แต่ **อันตรายสำหรับ non-idempotent operations** เช่น POST ที่สร้าง resource:

```typescript
// ⚠️ อันตราย — retry POST อาจสร้าง duplicate
test('create order', async ({ page, request }) => {
  await request.post('/api/orders', { data: { item: 'book' } });
  // ถ้า retry 3 รอบ = สร้าง 3 orders
});

// ✅ ปลอดภัย — ใช้ cleanup fixture เพื่อ reset state ก่อน retry
test('create order', async ({ page, cleanDatabase }) => {
  // cleanDatabase fixture reset DB ทุกครั้งที่ test เริ่ม (รวม retry)
  await page.goto('/new-order');
  // ...
});
// tested: Playwright v1.50+, Node.js 20+
```

### 4.2 test.fixme() และ test.fail()

Playwright มี annotation สำหรับจัดการ tests ที่ยังมีปัญหา:

**`test.fixme()`** — test ที่รู้ว่า broken ชั่วคราว *(source: playwright.dev/docs/test-annotations)*

```typescript
test.fixme('checkout flow with discount code', async ({ page }) => {
  // known issue: discount API ยัง bug อยู่ — ticket #1234
  // Playwright จะ skip test นี้แต่ยัง track ไว้ใน report
});
// tested: Playwright v1.50+, Node.js 20+
```

ต่างจาก `test.skip()` ตรงที่ `fixme` ส่งสัญญาณว่า "นี่คือ known bug ที่ต้องกลับมาแก้" ไม่ใช่ "test นี้ irrelevant" — ทำให้ทีมไม่ลืม

**`test.fail()`** — expect ว่า test จะ fail (ใช้น้อย แต่มีประโยชน์)

```typescript
test.fail('login with expired token should redirect', async ({ page }) => {
  // Playwright จะรัน test นี้และ PASS ถ้า test fail
  // ถ้า behavior แก้แล้ว (test ผ่าน) — Playwright จะ warn ว่า
  // "test was expected to fail but it passed"
  await page.goto('/dashboard');
  await expect(page).toHaveURL('/login');
});
// tested: Playwright v1.50+, Node.js 20+
```

`test.fail()` เหมาะสำหรับ document known bug ที่ยังไม่ได้แก้ — เมื่อ bug แก้แล้ว Playwright จะแจ้งเตือนให้ลบ annotation นี้ออก

### 4.3 test.info().attach() — เพิ่ม Artifact เมื่อ Test Fail

เมื่อ test fail บน CI วิธีเดียวที่จะรู้ว่าเกิดอะไรขึ้นคือ artifact — screenshot, log, หรือ response body Playwright documentation ระบุว่า `attach()` "automatically takes care of copying attached files to a location that is accessible to reporters" *(source: playwright.dev/docs/api/class-testinfo)*

```typescript
// tested: Playwright v1.50+, Node.js 20+
test('critical checkout flow', async ({ page }) => {
  try {
    await page.goto('/cart');
    await page.getByRole('button', { name: 'Checkout' }).click();
    await expect(page.getByText('Order confirmed')).toBeVisible();
  } catch (error) {
    // attach screenshot เมื่อ fail — ดูใน HTML report หรือ trace viewer
    await test.info().attach('screenshot-on-failure', {
      body: await page.screenshot({ fullPage: true }),
      contentType: 'image/png',
    });
    // attach current URL เพื่อ debug
    await test.info().attach('url-on-failure', {
      body: Buffer.from(page.url()),
      contentType: 'text/plain',
    });
    throw error;  // re-throw เพื่อให้ test fail ตามปกติ
  }
});
```

**สำคัญ**: ต้อง re-throw error เสมอ ไม่งั้น test จะ pass ทั้งที่เกิดปัญหา

### 4.4 Test Data Factory Pattern

ปัญหาที่พบบ่อยใน parallel testing: tests แชร์ data กัน → state ปนกัน → flaky tests

**Test Data Factory** แก้ปัญหานี้ด้วยการให้แต่ละ test สร้าง data ของตัวเองผ่าน API (เร็วกว่า UI) และ cleanup หลังเสร็จ:

```typescript
// tests/factories/todo.factory.ts
// tested: Playwright v1.50+, Node.js 20+
import { APIRequestContext } from '@playwright/test';

interface Todo {
  id: number;
  text: string;
  completed: boolean;
  createdAt: string;
}

export class TodoFactory {
  private createdIds: number[] = [];

  constructor(private request: APIRequestContext) {}

  async create(text: string = `Todo-${Date.now()}`): Promise<Todo> {
    const res = await this.request.post('http://localhost:3000/api/todos', {
      data: { text },
    });
    const todo: Todo = await res.json();
    this.createdIds.push(todo.id);  // track เพื่อ cleanup
    return todo;
  }

  async createMany(count: number): Promise<Todo[]> {
    return Promise.all(
      Array.from({ length: count }, (_, i) => this.create(`Todo ${i + 1}`))
    );
  }

  // cleanup เฉพาะ todos ที่ test นี้สร้าง — ไม่กระทบ tests อื่น
  async cleanup(): Promise<void> {
    await Promise.all(
      this.createdIds.map((id) =>
        this.request.delete(`http://localhost:3000/api/todos/${id}`)
      )
    );
    this.createdIds = [];
  }
}
```

ใช้ Factory ผ่าน fixture เพื่อให้ cleanup อัตโนมัติ:

```typescript
// tests/fixtures/factory.fixtures.ts
// tested: Playwright v1.50+, Node.js 20+
import { test as base } from '@playwright/test';
import { TodoFactory } from '../factories/todo.factory';

type FactoryFixtures = {
  todoFactory: TodoFactory;
};

export const test = base.extend<FactoryFixtures>({
  todoFactory: async ({ request }, use) => {
    const factory = new TodoFactory(request);
    await use(factory);
    await factory.cleanup();  // cleanup อัตโนมัติหลังทุก test
  },
});

export { expect } from '@playwright/test';
```

```typescript
// tests/todos/filter.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '../fixtures/factory.fixtures';

test('filter completed todos', async ({ page, todoFactory }) => {
  // สร้าง data เฉพาะสำหรับ test นี้
  await todoFactory.create('Buy groceries');
  const doneTodo = await todoFactory.create('Read book');

  // mark as completed ผ่าน API
  // ...

  await page.goto('/');
  await page.getByRole('link', { name: 'Completed' }).click();

  // verify เฉพาะ data ของ test นี้ ไม่ถูก affect โดย tests อื่น
  await expect(page.getByText('Read book')).toBeVisible();
  await expect(page.getByText('Buy groceries')).not.toBeVisible();
  // cleanup เกิดขึ้นอัตโนมัติหลัง test จบ
});
```

### 4.5 Large Suite Organization

เมื่อ test suite เติบโตถึง 100+ tests วิธี organize มีผลต่อ maintainability มาก

**ใช้ Feature Folders ไม่ใช่ Layer Folders:**

```
# ❌ Layer-centric — ต้องดูหลาย folder เพื่อเข้าใจ 1 feature
tests/
  ui/
    todo-list.spec.ts
    checkout.spec.ts
  api/
    todo-api.spec.ts
    checkout-api.spec.ts

# ✅ Feature-centric — ทุกอย่างของ feature อยู่ด้วยกัน
tests/
  todos/
    todo-list.spec.ts      ← UI tests
    todo-api.spec.ts       ← API tests
    todo-factory.ts        ← test data
  checkout/
    checkout-flow.spec.ts
    checkout-api.spec.ts
    checkout-factory.ts
```

**Tagging สำหรับ selective runs:**

Playwright รองรับ tag ที่ขึ้นต้นด้วย `@` *(source: playwright.dev/docs/test-annotations)*

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

test('add new todo @smoke', async ({ page }) => {
  // @smoke = critical path ที่รันก่อน deploy
  await page.goto('/');
  await page.getByPlaceholder('What needs to be done?').fill('Buy milk');
  await page.keyboard.press('Enter');
  await expect(page.getByText('Buy milk')).toBeVisible();
});

test('filter by status @regression', async ({ page }) => {
  // @regression = full test suite รันทุกคืน
});
```

รัน tag เฉพาะจาก CLI:

```bash
# รัน smoke tests ก่อน deploy
npx playwright test --grep "@smoke"

# รัน regression tests ทั้งหมด ยกเว้น smoke
npx playwright test --grep "@regression" --grep-invert "@smoke"
```

**Environment Configuration ผ่าน .env:**

```typescript
// playwright.config.ts
// tested: Playwright v1.50+, Node.js 20+
import { defineConfig } from '@playwright/test';
import * as dotenv from 'dotenv';
import * as path from 'path';

// โหลด .env.staging หรือ .env.production ตาม NODE_ENV
dotenv.config({
  path: path.resolve(
    __dirname,
    `.env.${process.env.NODE_ENV || 'development'}`
  ),
});

export default defineConfig({
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:3000',
  },
});
```

ไฟล์ `.env.staging`:
```
BASE_URL=https://staging.example.com
API_URL=https://api.staging.example.com
```

```bash
# รันกับ staging environment
NODE_ENV=staging npx playwright test
```

**`--forbid-only` บน CI:**

```typescript
// playwright.config.ts
export default defineConfig({
  forbidOnly: !!process.env.CI,  // fail ถ้ามี test.only() หลุดมาบน CI
});
```

### 4.6 Reporting & Monitoring

**ตรวจ test result ใน afterEach fixture:**

`testInfo.status` มี 5 ค่า: `"passed"`, `"failed"`, `"timedOut"`, `"skipped"`, `"interrupted"` *(source: playwright.dev/docs/api/class-testinfo)*

```typescript
// tests/fixtures/monitoring.fixtures.ts
// tested: Playwright v1.50+, Node.js 20+
import { test as base } from '@playwright/test';

export const test = base.extend({
  page: async ({ page }, use, testInfo) => {
    await use(page);

    // หลัง test จบ — log result สำหรับ monitoring
    if (testInfo.status !== testInfo.expectedStatus) {
      console.log(`[ALERT] Test failed: ${testInfo.title}`);
      console.log(`  Duration: ${testInfo.duration}ms`);
      console.log(`  Status: ${testInfo.status}`);
      console.log(`  Retry: ${testInfo.retry}`);

      // เพิ่ม annotation ใน HTML report
      testInfo.annotations.push({
        type: 'alert',
        description: `Failed after ${testInfo.duration}ms`,
      });
    }
  },
});
```

**Custom Reporter สำหรับ Slack Notification:**

Playwright documentation แสดง interface ที่ต้อง implement *(source: playwright.dev/docs/test-reporters)*

```typescript
// reporters/slack-reporter.ts
// tested: Playwright v1.50+, Node.js 20+
import type {
  FullConfig,
  FullResult,
  Reporter,
  Suite,
  TestCase,
  TestResult,
} from '@playwright/test/reporter';

class SlackReporter implements Reporter {
  private failedTests: string[] = [];

  onTestEnd(test: TestCase, result: TestResult): void {
    if (result.status === 'failed' || result.status === 'timedOut') {
      this.failedTests.push(`❌ ${test.title}`);
    }
  }

  async onEnd(result: FullResult): Promise<void> {
    if (this.failedTests.length === 0) return;

    const message = [
      `🚨 Playwright Tests Failed (${result.status})`,
      ...this.failedTests,
    ].join('\n');

    // ส่ง Slack notification ผ่าน webhook
    await fetch(process.env.SLACK_WEBHOOK_URL!, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ text: message }),
    });
  }
}

export default SlackReporter;
```

เปิดใช้งานใน config:

```typescript
// playwright.config.ts
export default defineConfig({
  reporter: [
    ['html'],  // HTML report ปกติ
    ['./reporters/slack-reporter.ts'],  // Slack notification เมื่อ fail
  ],
});
```

### 4.7 Performance Patterns

**ใช้ API สำหรับ setup/teardown แทน UI:**

```typescript
// ❌ ช้า — setup ผ่าน UI ทุก test
test.beforeEach(async ({ page }) => {
  await page.goto('/login');
  await page.getByLabel('Email').fill('user@test.com');
  await page.getByLabel('Password').fill('password');
  await page.getByRole('button', { name: 'Login' }).click();
  // ~2-3 วินาทีต่อ test
});

// ✅ เร็ว — setup ผ่าน API + storageState
// ทำครั้งเดียวใน global setup แล้วใช้ซ้ำ
test.use({ storageState: 'playwright/.auth/user.json' });
// ~50ms ต่อ test
// tested: Playwright v1.50+, Node.js 20+
```

**`test.describe.parallel()` สำหรับ independent tests:**

```typescript
// tested: Playwright v1.50+, Node.js 20+
test.describe.parallel('independent feature tests', () => {
  // tests ใน group นี้รันพร้อมกันใน worker แยก
  test('feature A', async ({ page }) => { /* ... */ });
  test('feature B', async ({ page }) => { /* ... */ });
  test('feature C', async ({ page }) => { /* ... */ });
});
```

### 4.8 เปรียบเทียบกับ Robot Framework + Selenium

| หัวข้อ | Robot Framework + Selenium | Playwright |
|--------|---------------------------|------------|
| **Flaky strategy** | Retry + เพิ่ม `Sleep` (ไม่แก้ root cause) | Retry + trace `on-first-retry` + web-first assertions |
| **Test data isolation** | ยาก — shared global state ใน RF variables | Factory pattern + API cleanup per test |
| **Suite organization** | Resource files (keyword-centric) | Feature folders (user-journey-centric) |
| **Known failures** | Comment หรือ tag `robot:skip` | `test.fixme()` + `test.fail()` พร้อม intent ชัดเจน |
| **Artifact collection** | External library (pabot + custom) | `test.info().attach()` built-in |
| **Custom reporting** | Listener API (complex) | Custom Reporter interface (simple TypeScript class) |
| **Environment config** | `.robot` variables file | `.env.*` + dotenv native |

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner — TodoFactory + Cleanup Fixture

สถานการณ์: ทดสอบว่า Todo list แสดงผล correctly เมื่อมี 3 items — ต้องการ data ที่ clean ทุกครั้ง

```typescript
// tests/todos/todo-display.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '../fixtures/factory.fixtures';

test('displays all todos in order', async ({ page, todoFactory }) => {
  // สร้าง test data ผ่าน factory — clean และ isolated
  await todoFactory.create('First task');
  await todoFactory.create('Second task');
  await todoFactory.create('Third task');

  await page.goto('/');

  // verify ครบ 3 items
  const items = page.getByTestId('todo-item');
  await expect(items).toHaveCount(3);
  await expect(items.nth(0)).toHaveText('First task');
  await expect(items.nth(1)).toHaveText('Second task');
  await expect(items.nth(2)).toHaveText('Third task');

  // factory cleanup เกิดขึ้นอัตโนมัติหลัง test จบ
});
```

สิ่งที่ Beginner ควรสังเกต:
- `todoFactory` มาจาก fixture — ไม่ต้อง import class โดยตรง
- ไม่มี `afterEach` cleanup ในตัว test — fixture จัดการให้
- factory สร้าง data ผ่าน API ไม่ผ่าน UI — เร็วกว่ามาก

### Intermediate — Environment Config สำหรับ Staging vs Production

สถานการณ์: ทีมมี 3 environments: development, staging, production — แต่ละ environment ใช้ API URL และ credentials ต่างกัน ต้องการรันชุด smoke tests เดิมกับทุก environment โดยไม่แก้ code

```typescript
// playwright.config.ts
// tested: Playwright v1.50+, Node.js 20+
import { defineConfig, devices } from '@playwright/test';
import * as dotenv from 'dotenv';
import * as path from 'path';

const env = process.env.TEST_ENV || 'development';

dotenv.config({
  path: path.resolve(__dirname, `.env.${env}`),
  override: true,
});

console.log(`Running tests against: ${process.env.BASE_URL}`);

export default defineConfig({
  use: {
    baseURL: process.env.BASE_URL,
    extraHTTPHeaders: {
      'x-api-key': process.env.API_KEY || '',
    },
  },
  retries: env === 'production' ? 1 : 0,  // production มี retry 1 ครั้ง
  forbidOnly: !!process.env.CI,
});
```

ไฟล์ environment:

```bash
# .env.development
BASE_URL=http://localhost:3000
API_KEY=dev-key-local

# .env.staging
BASE_URL=https://staging.myapp.com
API_KEY=staging-api-key-xxx

# .env.production
BASE_URL=https://myapp.com
API_KEY=prod-api-key-yyy
```

Test ที่ใช้งานได้กับทุก environment:

```typescript
// tests/smoke/critical-path.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

// @smoke tag สำหรับ filter
test('homepage loads @smoke', async ({ page }) => {
  // ใช้ baseURL จาก config — ไม่ hardcode URL
  await page.goto('/');
  await expect(page).toHaveTitle(/My App/);
  await expect(page.getByRole('navigation')).toBeVisible();
});

test('api health check @smoke', async ({ request }) => {
  const res = await request.get('/api/health');
  expect(res.ok()).toBeTruthy();
  const body = await res.json();
  expect(body.status).toBe('healthy');
});
```

รันกับแต่ละ environment:

```bash
# staging smoke tests
TEST_ENV=staging npx playwright test --grep "@smoke"

# production smoke tests (ระวัง — production ข้อมูลจริง)
TEST_ENV=production npx playwright test --grep "@smoke"
```

### Advanced — Custom Reporter + Test Abort + Factory Pattern

สถานการณ์: production test suite ที่ต้องการ:
1. Slack notification เมื่อ critical tests fail
2. abort ทั้ง suite ถ้า smoke test fail (ไม่ต้องรอ regression ทั้งหมด)
3. factory cleanup ที่ robust แม้ test abort กลางคัน

```typescript
// reporters/critical-reporter.ts
// tested: Playwright v1.50+, Node.js 20+
import type {
  FullResult,
  Reporter,
  TestCase,
  TestResult,
} from '@playwright/test/reporter';

interface CriticalTestResult {
  title: string;
  duration: number;
  retry: number;
  error?: string;
}

class CriticalReporter implements Reporter {
  private criticalFailures: CriticalTestResult[] = [];
  private totalTests = 0;
  private passedTests = 0;

  onTestEnd(test: TestCase, result: TestResult): void {
    this.totalTests++;

    if (result.status === 'passed') {
      this.passedTests++;
      return;
    }

    // track เฉพาะ critical tests (tagged @smoke)
    const isCritical = test.tags.includes('@smoke');
    if (isCritical && result.status === 'failed') {
      this.criticalFailures.push({
        title: test.title,
        duration: result.duration,
        retry: result.retry,
        error: result.error?.message?.slice(0, 200),  // truncate
      });
    }
  }

  async onEnd(result: FullResult): Promise<void> {
    if (this.criticalFailures.length === 0) return;

    const passRate = Math.round((this.passedTests / this.totalTests) * 100);
    const failureList = this.criticalFailures
      .map((f) => `• ${f.title} (${f.duration}ms, retry: ${f.retry})`)
      .join('\n');

    const payload = {
      text: [
        `🚨 *Critical Tests Failed* — Pass rate: ${passRate}%`,
        `*Failed smoke tests:*`,
        failureList,
        `*Run status:* ${result.status}`,
      ].join('\n'),
    };

    try {
      await fetch(process.env.SLACK_WEBHOOK_URL!, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
    } catch (e) {
      // reporter ไม่ควร throw — แค่ log
      console.error('Slack notification failed:', e);
    }
  }
}

export default CriticalReporter;
```

```typescript
// playwright.config.ts (production-grade)
// tested: Playwright v1.50+, Node.js 20+
import { defineConfig } from '@playwright/test';
import * as dotenv from 'dotenv';

dotenv.config({ path: `.env.${process.env.TEST_ENV || 'development'}` });

export default defineConfig({
  retries: process.env.CI ? 2 : 0,
  forbidOnly: !!process.env.CI,
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:3000',
    trace: 'on-first-retry',
  },
  reporter: [
    ['html', { open: 'never' }],
    ['./reporters/critical-reporter.ts'],
    ...(process.env.CI ? [['github'] as ['github']] : []),
  ],
  projects: [
    {
      name: 'smoke',
      grep: /@smoke/,
      testDir: './tests',
    },
    {
      name: 'regression',
      grep: /@regression/,
      testDir: './tests',
      dependencies: ['smoke'],  // รัน smoke ก่อน regression เสมอ
    },
  ],
});
```

```typescript
// tests/fixtures/robust-factory.fixtures.ts
// tested: Playwright v1.50+, Node.js 20+
import { test as base } from '@playwright/test';
import { TodoFactory } from '../factories/todo.factory';

// Factory fixture ที่ cleanup แม้ test abort/timeout
export const test = base.extend<{ todoFactory: TodoFactory }>({
  todoFactory: [
    async ({ request }, use, testInfo) => {
      const factory = new TodoFactory(request);
      let cleanupFailed = false;

      try {
        await use(factory);
      } finally {
        // finally block รัน แม้ test fail, timeout, หรือ abort
        try {
          await factory.cleanup();
        } catch (cleanupError) {
          cleanupFailed = true;
          // log แต่ไม่ throw — ไม่ให้ cleanup error ซ่อน test error จริง
          testInfo.annotations.push({
            type: 'cleanup-warning',
            description: `Factory cleanup failed: ${cleanupError}`,
          });
        }
      }

      if (cleanupFailed) {
        console.warn(`[WARNING] Cleanup failed for test: ${testInfo.title}`);
      }
    },
    { scope: 'test' },
  ],
});

export { expect } from '@playwright/test';
```

---

## 6. Common Mistakes

❌ **ใส่หลาย action ใน test เดียว — เมื่อ fail ไม่รู้ว่าส่วนไหนพัง**

```typescript
// ❌ 1 test ทดสอบหลาย concern
test('todo app works', async ({ page }) => {
  await page.goto('/');
  await page.fill('[placeholder]', 'Task 1');
  await page.keyboard.press('Enter');
  await expect(page.getByText('Task 1')).toBeVisible();
  await page.getByText('Task 1').click();  // mark complete
  await page.click('.filter-completed');
  await expect(page.getByText('Task 1')).toBeVisible();
  await page.click('.clear-completed');
  await expect(page.getByText('Task 1')).not.toBeVisible();
  // fail ที่บรรทัดไหนก็ไม่รู้ว่า feature ไหนพัง
});
```

```typescript
// ✅ แยก test ตาม behavior
test('adds new todo', async ({ page }) => { /* ... */ });
test('marks todo as complete', async ({ page }) => { /* ... */ });
test('filters completed todos', async ({ page }) => { /* ... */ });
test('clears completed todos', async ({ page }) => { /* ... */ });
// tested: Playwright v1.50+, Node.js 20+
```

*(source: playwright.dev/docs/best-practices — "Each test should be completely isolated from another test")*

---

❌ **`waitForTimeout(3000)` แก้ flaky test**

```typescript
// ❌ hardcode wait — ช้า และยังเป็น race condition อยู่
await page.click('#submit');
await page.waitForTimeout(3000);  // หวังว่า toast จะขึ้นมาภายใน 3 วินาที
const text = await page.locator('.toast').textContent();
expect(text).toBe('Saved!');
```

```typescript
// ✅ web-first assertion — wait จนกว่า condition จะ true
await page.click('#submit');
await expect(page.locator('.toast')).toHaveText('Saved!');
// tested: Playwright v1.50+, Node.js 20+
```

*(source: playwright.dev/docs/best-practices — use web-first assertions)*

---

❌ **Hardcode URL ใน tests**

```typescript
// ❌ hardcode — เปลี่ยน environment ต้องแก้ทุก test
await page.goto('http://localhost:3000/todos');
await page.goto('http://localhost:3000/login');
```

```typescript
// ✅ baseURL จาก config — เปลี่ยน environment แค่ที่ config
await page.goto('/todos');    // ใช้ baseURL อัตโนมัติ
await page.goto('/login');
// tested: Playwright v1.50+, Node.js 20+
```

---

❌ **ไม่มี cleanup — tests รัน parallel แล้ว state ปนกัน**

```typescript
// ❌ ไม่มี cleanup — todos จาก test A ปนกับ test B
test('A: shows 1 todo', async ({ page, request }) => {
  await request.post('/api/todos', { data: { text: 'Task A' } });
  await page.goto('/');
  await expect(page.getByTestId('todo-item')).toHaveCount(1);
  // ถ้า test B รันพร้อมกันและเพิ่ม todo ด้วย count จะเป็น 2 ไม่ใช่ 1
});
```

```typescript
// ✅ ใช้ factory fixture — cleanup อัตโนมัติ + isolated
test('shows created todo', async ({ page, todoFactory }) => {
  await todoFactory.create('My Task');
  await page.goto('/');
  // ตรวจ text โดยตรง ไม่ตรวจ count ที่อาจ affected โดย other tests
  await expect(page.getByText('My Task')).toBeVisible();
});
// tested: Playwright v1.50+, Node.js 20+
```

---

❌ **ลืม re-throw error หลัง attach artifact**

```typescript
// ❌ test "ผ่าน" ทั้งที่เกิด error จริง
test('checkout', async ({ page }) => {
  try {
    await performCheckout(page);
  } catch (error) {
    await test.info().attach('screenshot', {
      body: await page.screenshot(),
      contentType: 'image/png',
    });
    // ลืม throw error — test จะ pass!
  }
});

// ✅ ต้อง re-throw เสมอ
test('checkout', async ({ page }) => {
  try {
    await performCheckout(page);
  } catch (error) {
    await test.info().attach('screenshot', {
      body: await page.screenshot(),
      contentType: 'image/png',
    });
    throw error;  // บังคับให้ test fail ตามที่ควรจะเป็น
  }
});
// tested: Playwright v1.50+, Node.js 20+
```

---

## 7. สรุปบท

ก่อนดูเฉลย ลองตอบคำถามเหล่านี้ด้วยตัวเองก่อน:

**คำถามที่ 1**: สมมติ test ของคุณ fail บน CI ด้วย error "Element not found: .confirm-dialog" แต่บน local ผ่านตลอด คุณจะวางแผน debug อย่างไร — ใช้ tools อะไรของ Playwright และขั้นตอนแรกที่ควรทำคืออะไร?

**คำถามที่ 2**: ทีมของคุณมี 500 tests รันบน CI ใช้เวลา 45 นาที ซึ่งช้าเกินไปสำหรับ PR workflow คุณจะใช้ strategies อะไรบ้างเพื่อลดเวลาลงเหลือ 15 นาที — ระบุ approach อย่างน้อย 3 อย่างพร้อมอธิบายว่าแต่ละอย่างช่วยได้อย่างไร?

**คำถามที่ 3**: คุณมี test suite ที่รัน parallel 4 workers แต่ result ไม่ stable — บางครั้ง test A ผ่าน บางครั้งไม่ผ่าน ทั้งที่ logic ไม่เปลี่ยน คุณ suspect ว่าเป็น state sharing ปัญหา — คุณจะ diagnose และแก้อย่างไร?

---

เฉลย:

**คำถามที่ 1**: เริ่มจาก config `trace: 'on-first-retry'` และตั้ง `retries: 1` บน CI — เมื่อ test fail และ retry จะมี trace file ที่ดาวน์โหลดได้จาก artifacts รัน `npx playwright show-trace trace.zip` เพื่อดู timeline แบบ step-by-step ว่า dialog ไม่เคยปรากฏ หรือปรากฏแล้วหายไป หรือ locator ผิด ถ้า dialog ปรากฏช้า (network-dependent) ให้เพิ่ม web-first assertion `await expect(page.locator('.confirm-dialog')).toBeVisible()` แทนการ select โดยตรง

**คำถามที่ 2**: (1) **Sharding** — แบ่ง 500 tests เป็น 3 shards รันบน 3 machines พร้อมกัน ลดจาก 45 นาทีเหลือ ~15 นาที (2) **API setup แทน UI** — tests ที่ใช้เวลาส่วนใหญ่ใน login/setup ให้ใช้ storageState ลด overhead 2-3 วินาทีต่อ test (3) **Tagging + selective runs** — รัน `@smoke` (30-50 tests critical path) ทุก PR รัน `@regression` (ทั้งหมด) ทุกคืน PR workflow เร็วขึ้นทันทีโดยไม่ต้องลด test coverage

**คำถามที่ 3**: Diagnose ด้วย `--repeat-each=10` เพื่อยืนยันว่า test A จริงๆ flaky ไม่ใช่แค่ unlucky จากนั้นตรวจว่า test A อ่าน/เขียน shared resource อะไร (database table, file, API state) — ถ้า test อื่น modify resource เดิม นั่นคือ root cause แก้ด้วย Factory pattern ให้แต่ละ test สร้าง data ของตัวเอง และ cleanup หลังเสร็จ พร้อม verify ด้วย text assertion แทน count assertion ที่ขึ้นกับ total state ของ database
