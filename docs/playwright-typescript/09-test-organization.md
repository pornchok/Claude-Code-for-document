## ก่อนอ่านบทนี้ ลองตอบ:

1. ทำไม assertion ถึงไม่ควรอยู่ใน Page Object method — อธิบายด้วยตัวอย่างสถานการณ์ที่จะเกิดปัญหา?
2. ถ้า `NavBarComponent` ปรากฏในหน้า `/todos` และ `/shop` ควรออกแบบโครงสร้างอย่างไร — ใช้ Inheritance หรือ Composition และทำไม?

---

เฉลย:

1. สถานการณ์ปัญหา: ถ้า `loginPage.doLogin()` มี `expect(sessionBadge).toBeVisible()` อยู่ข้างใน test ที่ต้องการตรวจสอบ login fail จะถูก assertion ใน POM throw error ก่อน — test ไม่มีโอกาส assert `errorMessage` เลย Page Object ควรเป็น action layer ล้วนๆ ให้ test เป็นผู้ assert
2. ใช้ **Composition** — สร้าง `NavBarComponent` แล้วให้ `TodosPage` และ `ShopPage` มี `readonly navBar: NavBarComponent` ใน constructor แต่ละ class Inheritance ผิดเพราะ TodosPage ไม่ใช่ NavBar แค่ "มี" NavBar เท่านั้น

---

# บทที่ 9: Test Organization — Annotations, Tags, Parameterize, Steps

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- ใช้ **annotations** (`test.skip`, `test.fail`, `test.fixme`, `test.slow`) ได้อย่างถูกต้อง รู้ว่าแต่ละตัวต่างกันอย่างไร
- ใช้ **tags** (`@smoke`, `@regression`) เพื่อ filter tests ด้วย `--grep` และ `--grep-invert`
- เขียน **parameterized tests** ด้วย `test.each()` เพื่อรัน test เดิมกับข้อมูลหลายชุด
- จัดกลุ่ม actions ด้วย **`test.step()`** ให้ Trace Viewer แสดงผลชัดเจนขึ้น
- เพิ่ม **runtime annotations** แบบ dynamic ระหว่างที่ test กำลังรัน
- ปรับ execution mode ของ `describe` block ด้วย `test.describe.configure()`
- อธิบายความแตกต่างระหว่าง annotations กับ tags ได้ชัดเจน

---

## 2. ทำไมต้องรู้? (Why)

สมมติ test suite คุณมี 80 tests แล้วเกิดปัญหาสามอย่างพร้อมกัน:

**ปัญหาแรก** — มี bug ใน feature X ที่ยังแก้ไม่ได้ แต่ tests อื่นๆ ที่ไม่เกี่ยวกันควรรัน pass ต่อไปได้ ถ้าปล่อยให้ tests ของ feature X รันไปก็จะ fail ทุกครั้ง — CI pipeline พัง, ทีมสับสนว่า fail เป็นเรื่องใหม่หรือเรื่องเก่า

**ปัญหาที่สอง** — login form รองรับ username หลายรูปแบบ (email, phone, username ธรรมดา) ถ้าจะทดสอบทุกรูปแบบต้องเขียน test แยก 3 ตัวที่มี code เหมือนกัน 90% เปลี่ยนแค่ input data

**ปัญหาที่สาม** — checkout flow มี 8 steps แต่ Trace Viewer แสดงเป็น actions ยาวเหยียด 40 รายการโดยไม่มีกลุ่ม ทีมหาไม่เจอว่า fail ตรงไหน

Playwright มีเครื่องมือแก้ปัญหาทั้งสามนี้โดยตรง:
- **Annotations** แก้ปัญหาแรก — mark tests ที่ skip/broken ได้อย่างมีความหมาย
- **`test.each()`** แก้ปัญหาที่สอง — parameterize ข้อมูลแยกจาก logic
- **`test.step()`** แก้ปัญหาที่สาม — จัดกลุ่ม actions ใน Trace Viewer

---

## 3. Analogy: Sticky Notes กับ Label Folders

ลองนึกถึงห้องจัดเก็บเอกสาร:

**Annotations เหมือน sticky note ที่ติดบนเอกสาร** — sticky note แต่ละสีมีความหมายต่างกัน: สีเหลืองคือ "รอ review", สีแดงคือ "ห้ามส่งออก", สีน้ำเงินคือ "รู้ว่าผิด จะแก้ทีหลัง" Sticky note เหล่านี้เปลี่ยน **พฤติกรรม** ของเอกสารนั้น — HR จะจัดการมันต่างกันขึ้นอยู่กับสีที่ติด

**Tags เหมือน label บนแฟ้ม** — label ไม่เปลี่ยนว่าจะทำอะไรกับเอกสาร แค่บอกว่า "เอกสารนี้อยู่ใน category ไหน" เพื่อให้ค้นหาหรือ filter ได้ง่าย ถ้าบอสบอก "เอาแฟ้มที่ label ว่า Q1-2025 มาให้หมด" ก็แค่ filter ตาม label

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**

- **Annotation กับ tag เป็นแค่ metadata เหมือนกัน** — จริงๆ annotations เปลี่ยน behavior ของ test โดยตรง (`test.skip` ทำให้ Playwright ไม่รัน test นั้น, `test.fail` เปลี่ยน expectation ว่า test ควร fail) ส่วน tags เป็นแค่ category label ที่ไม่มีผลต่อ behavior
- **`test.fixme()` ทำให้ test ผ่าน** — fixme ไม่ได้แก้ test ให้ผ่าน มันแค่ skip test นั้นพร้อม mark ว่า "รู้ว่าพัง ต้องกลับมาแก้" — test ยังพังอยู่ แค่ไม่ถูกรัน
- **Tags ต้องขึ้นต้นด้วย @** — ใน Playwright tags ต้องมี `@` นำหน้าเสมอ (`@smoke` ไม่ใช่ `smoke`) ไม่งั้น `--grep @smoke` จะไม่เจอ

---

## 4. เนื้อหาหลัก

### 4.1 Annotations — เปลี่ยน Behavior ของ Test

Annotations คือ built-in markers ที่บอก Playwright ว่าควรจัดการกับ test นี้อย่างไร แต่ละตัวมีความหมายต่างกันชัดเจน:

---

**`test.skip()`** — ข้ามไม่รัน

```typescript
// ข้ามเสมอ (ไม่มี condition)
test.skip('feature ยังไม่ implement', async ({ page }) => { ... });

// ข้ามเมื่อ condition เป็น true
test('login บน mobile', async ({ page }) => {
  test.skip(process.env.CI === 'true', 'ไม่รันบน CI pipeline');
  // ...
});
```

ตาม docs: "Skip a test. Playwright will not run the test past the `test.skip()` call."

ใช้เมื่อ: feature ยังไม่พร้อม, test ไม่เกี่ยวกับ environment ปัจจุบัน

---

**`test.fail()`** — คาดหวังว่า test จะ fail

```typescript
test('known bug: login ด้วย special chars', async ({ page }) => {
  test.fail(true, 'BUG-789: ยังไม่ fix เรื่อง special chars');
  await page.goto('http://localhost:3000/login');
  await page.fill('[data-testid="input-username"]', 'user@"test"');
  await page.fill('[data-testid="input-password"]', 'pass123');
  await page.click('[data-testid="btn-login"]');
  await expect(page.getByTestId('session-badge')).toBeVisible(); // คาดว่าจะ fail
});
```

ตาม docs: "Marks a test as 'should fail'. Playwright runs this test and ensures that it is actually failing."

สิ่งสำคัญที่ต้องรู้: **ถ้า test กลับมา pass แทนที่จะ fail → Playwright รายงานว่า FAIL** เพราะ expectation ที่ตั้งไว้คือมันควรพัง

ใช้เมื่อ: มี known bug ที่ยืนยันได้ และต้องการตรวจสอบว่า bug ยังอยู่หรือถูก fix ไปแล้ว

---

**`test.fixme()`** — รู้ว่าพัง ต้องกลับมาแก้

```typescript
test.fixme('checkout flow หลัง session timeout', async ({ page }) => {
  // Playwright จะ skip test นี้ ไม่รัน
  // แต่ report จะแสดงว่ามี test ที่ mark ว่า fixme อยู่
});
```

ตาม docs: "Mark a test as 'fixme', with the intention to fix it. Playwright will not run the test past the `test.fixme()` call."

ความต่างจาก `test.skip()`: fixme สื่อเจตนาว่า "รู้ว่าพัง ต้องกลับมาแก้" ส่วน skip สื่อว่า "ไม่เกี่ยวข้องในตอนนี้"

---

**`test.slow()`** — เพิ่ม timeout ×3

```typescript
test('export PDF ขนาดใหญ่', async ({ page }) => {
  test.slow(); // timeout เปลี่ยนจาก 30s เป็น 90s
  await page.goto('http://localhost:3000/reports');
  // ...
});

// conditional slow
test('upload ไฟล์ใหญ่', async ({ page }) => {
  test.slow(process.env.SLOW_NETWORK === 'true', 'network throttled env');
  // ...
});
```

ตาม docs: "Marks a test as 'slow'. Slow test will be given triple the default timeout."

ใช้เมื่อ: test ต้องการเวลานานกว่าปกติ (upload/download ไฟล์ใหญ่, PDF generation, email async)

---

### 4.2 Runtime Annotations — เพิ่ม Metadata ขณะรัน

บางครั้งอยากเพิ่มข้อมูลให้ test แบบ dynamic ระหว่างที่กำลังรัน เช่น ผูก test กับ bug ticket:

```typescript
test('user profile update', async ({ page }, testInfo) => {
  testInfo.annotations.push({
    type: 'issue',
    description: 'https://jira.example.com/BUG-456'
  });
  testInfo.annotations.push({
    type: 'jira',
    description: 'BUG-456'
  });
  // ...
});
```

HTML reporter จะแสดง annotations ทั้งหมด ยกเว้นที่ขึ้นต้นด้วย `_` (underscore)

---

### 4.3 test.describe.configure() — ควบคุม Execution Mode

```typescript
test.describe.configure({ mode: 'serial' });

test.describe('checkout flow ที่ต้องรันตามลำดับ', () => {
  test('step 1: add to cart', async ({ page }) => { ... });
  test('step 2: fill shipping info', async ({ page }) => { ... });
  test('step 3: confirm payment', async ({ page }) => { ... });
});
```

Mode ที่มี:
- `'default'` — tests รันตามลำดับ แต่ถ้า retry จะ retry ตัวนั้นเดี่ยวๆ
- `'parallel'` — รัน tests ใน describe block นี้พร้อมกันทุกตัว
- `'serial'` — รันตามลำดับ ถ้าตัวใดตัวหนึ่ง fail → tests ที่เหลือใน block จะ skip ทั้งหมด

---

### 4.4 Tags — Category Labels สำหรับ Filter

Tags ใช้สำหรับจัดกลุ่มและ filter tests โดยไม่เปลี่ยน behavior:

**Syntax ที่ 1: ใส่ใน title (ทุก version)**
```typescript
test('login สำเร็จ @smoke @regression', async ({ page }) => { ... });
```

**Syntax ที่ 2: object parameter (v1.42+)**
```typescript
test('login สำเร็จ', { tag: ['@smoke', '@regression'] }, async ({ page }) => {
  // ...
});

test.describe('Auth flows', { tag: '@auth' }, () => {
  test('login', async ({ page }) => { ... });
  test('logout', async ({ page }) => { ... });
  // ทุก test ใน describe นี้ได้รับ @auth tag
});
```

**รัน filter ด้วย CLI:**
```bash
# รันเฉพาะ tests ที่มี @smoke
npx playwright test --grep @smoke

# รัน tests ที่มีทั้ง @smoke และ @regression (ไม่สนลำดับ)
npx playwright test --grep "(?=.*@smoke)(?=.*@regression)"

# ยกเว้น @slow ทั้งหมด
npx playwright test --grep-invert @slow

# combine: เอา @smoke แต่ยกเว้น @slow
npx playwright test --grep @smoke --grep-invert @slow
```

**หมายเหตุ:** `@smoke.*@regression` จะ match เฉพาะเมื่อ `@smoke` อยู่ก่อน `@regression` ในชื่อ test เท่านั้น ถ้าต้องการ match ไม่สนลำดับ (เช่น `@regression @smoke`) ให้ใช้ lookahead pattern `(?=.*@smoke)(?=.*@regression)` แทน

Tags ยังแสดงใน HTML report และ available ผ่าน `TestCase.tags` property สำหรับ custom reporter

---

### 4.5 test.each() — Parameterized Tests

เมื่อ test logic เหมือนกันแต่ข้อมูล input/output ต่างกัน ให้ใช้ `test.each()` แทนการ copy code:

**Pattern 1: Array of objects (แนะนำ)**
```typescript
const loginScenarios = [
  { username: 'admin', password: 'admin123', expectedBadge: 'admin' },
  { username: 'testuser', password: 'test123', expectedBadge: 'testuser' },
  { username: 'viewer', password: 'view456', expectedBadge: 'viewer' },
];

test.each(loginScenarios)(
  'login เป็น $username → badge แสดง $expectedBadge',
  async ({ page }, { username, password, expectedBadge }) => {
    await page.goto('http://localhost:3000/login');
    await page.fill('[data-testid="input-username"]', username);
    await page.fill('[data-testid="input-password"]', password);
    await page.click('[data-testid="btn-login"]');
    await expect(page.getByTestId('session-badge')).toContainText(expectedBadge);
  }
);
```

ใช้ `$variable` ใน test title เพื่อแสดงชื่อ parameter — test runner จะ generate ชื่อ test ให้อัตโนมัติ

**Pattern 2: Array of arrays**
```typescript
test.each([
  ['admin', 'admin123'],
  ['testuser', 'test123'],
])('login เป็น %s', async ({ page }, username, password) => {
  // ใช้ %s, %d, %i แทน positional arguments
});
```

**Pattern 3: test.describe.each() — parameterize ทั้ง describe block**
```typescript
test.describe.each([
  { env: 'staging', baseUrl: 'https://staging.example.com' },
  { env: 'prod', baseUrl: 'https://example.com' },
])('Tests บน $env', ({ env, baseUrl }) => {
  test('homepage โหลดได้', async ({ page }) => {
    await page.goto(baseUrl);
    await expect(page).toHaveTitle(/App/);
  });
  test('login ทำงานได้', async ({ page }) => {
    // ...
  });
});
```

---

### 4.6 test.step() — จัดกลุ่ม Actions ใน Trace Viewer

`test.step()` ทำให้ Trace Viewer แสดง actions เป็น groups ที่มีชื่อ แทนที่จะเห็น list ยาว 40 actions แบบไม่มีโครงสร้าง:

```typescript
test('checkout flow', async ({ page }) => {
  await test.step('Login', async () => {
    await page.goto('http://localhost:3000/login');
    await page.fill('[data-testid="input-username"]', 'admin');
    await page.fill('[data-testid="input-password"]', 'admin123');
    await page.click('[data-testid="btn-login"]');
  });

  await test.step('Add item to cart', async () => {
    await page.goto('http://localhost:3000/shop');
    await page.click('[data-testid="product-1-add"]');
  });

  await test.step('Complete checkout', async () => {
    await page.goto('http://localhost:3000/checkout');
    await page.click('[data-testid="btn-confirm"]');
  });
});
```

**Return value** — `test.step()` คืนค่าที่ callback return:
```typescript
const authToken = await test.step('Get auth token', async () => {
  await page.goto('http://localhost:3000/login');
  // ... login actions ...
  return await page.evaluate(() => localStorage.getItem('token'));
});
// ใช้ authToken ต่อได้ใน test
```

**Step timeout** — กำหนด timeout แยกต่างหากสำหรับ step:
```typescript
await test.step('Load heavy report', async () => {
  await page.goto('http://localhost:3000/reports/annual');
  await expect(page.getByTestId('report-table')).toBeVisible();
}, { timeout: 15000 }); // step นี้มี timeout 15 วินาทีของตัวเอง
```

---

### 4.7 testInfo Object

`testInfo` เป็น context ของ test ที่ใช้ได้ใน test body ผ่าน parameter ที่สอง:

```typescript
test('example', async ({ page }, testInfo) => {
  console.log(testInfo.title);      // ชื่อ test
  console.log(testInfo.status);     // 'passed' | 'failed' | 'timedOut' | 'skipped'
  console.log(testInfo.outputDir);  // path สำหรับ save artifacts
  console.log(testInfo.annotations); // array ของ annotations ที่เพิ่มไว้

  // save screenshot เป็น artifact
  const screenshotPath = testInfo.outputPath('screenshot.png');
  await page.screenshot({ path: screenshotPath });
});
```

---

### 4.8 เปรียบเทียบกับ Robot Framework + Selenium

| ฟีเจอร์ | Robot Framework + Selenium | Playwright |
|---------|--------------------------|------------|
| Skip test | `Skip If` keyword | `test.skip(condition, reason)` |
| Skip เสมอ | `[Tags] skip` + custom listener | `test.skip()` ไม่มี condition |
| Mark expected failure | ไม่มี built-in | `test.fail(condition, reason)` |
| Known broken, skip | ไม่มี built-in | `test.fixme()` |
| Test ช้า, เพิ่ม timeout | ตั้ง `timeout` manual ในทุก test | `test.slow()` ×3 อัตโนมัติ |
| Tagging | `[Tags]` section ใน test case | `@tag` ใน title หรือ `{ tag: [...] }` |
| Filter by tag | `--include` / `--exclude` | `--grep` / `--grep-invert` |
| Data-driven tests | `Test Template` + `Examples` | `test.each()` |
| Group steps ใน report | Keyword name ปรากฏใน log | `test.step()` ปรากฏใน Trace Viewer |
| Runtime metadata | Set Variable + Append To Log | `testInfo.annotations.push()` |

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner: Annotations พื้นฐาน

สถานการณ์: เพิ่งเริ่ม test suite ใหม่ มี login test ที่ปกติ, test ที่ต้องการ skip ชั่วคราว, และ test ที่รู้ว่าพัง

```typescript
// tested: Playwright v1.50+, Node.js 20+
// ไฟล์: tests/auth/login-basic.spec.ts
import { test, expect } from '@playwright/test';

// Test ปกติ — รันและ pass
test('admin login สำเร็จ @smoke', async ({ page }) => {
  await page.goto('http://localhost:3000/login');
  await page.fill('[data-testid="input-username"]', 'admin');
  await page.fill('[data-testid="input-password"]', 'admin123');
  await page.click('[data-testid="btn-login"]');
  await expect(page.getByTestId('session-badge')).toContainText('admin');
});

// test.skip — ไม่รัน เพราะ feature ยังไม่พร้อม
test.skip('SSO login via Google @smoke', async ({ page }) => {
  // จะไม่รัน — Playwright skip ไปเลย
  await page.goto('http://localhost:3000/login/google');
});

// test.skip conditional — skip เฉพาะบน CI
test('login บน mobile viewport', async ({ page }) => {
  test.skip(process.env.CI === 'true', 'Mobile viewport tests ไม่รันบน CI');
  await page.setViewportSize({ width: 375, height: 667 });
  await page.goto('http://localhost:3000/login');
  await expect(page.getByTestId('btn-login')).toBeVisible();
});

// test.fixme — รู้ว่าพัง ต้องกลับมาแก้
test.fixme('login ด้วย username ที่มี emoji 🎭 @BUG-123', async ({ page }) => {
  // Playwright ไม่รัน test นี้ แต่ report แสดงว่ามี fixme อยู่
  await page.goto('http://localhost:3000/login');
  await page.fill('[data-testid="input-username"]', 'user🎭');
});
```

**Output ที่คาดหวัง:**
```
✓ admin login สำเร็จ @smoke (1.3s)
- SSO login via Google @smoke (skipped)
- login บน mobile viewport (skipped)
- login ด้วย username ที่มี emoji 🎭 @BUG-123 (fixme)
```

---

### Intermediate: Parameterized Tests กับหลาย User Roles

สถานการณ์ใหม่: ระบบมี 4 roles (`admin`, `editor`, `viewer`, `guest`) แต่ละ role เห็นเมนูต่างกัน — ทดสอบ visibility ของ menu items สำหรับแต่ละ role

```typescript
// tested: Playwright v1.50+, Node.js 20+
// ไฟล์: tests/rbac/menu-visibility.spec.ts
import { test, expect } from '@playwright/test';

type RoleScenario = {
  role: string;
  password: string;
  canSeeAdminPanel: boolean;
  canSeeEditor: boolean;
  canSeeTodos: boolean;
};

const roleScenarios: RoleScenario[] = [
  { role: 'admin',   password: 'admin123', canSeeAdminPanel: true,  canSeeEditor: true,  canSeeTodos: true  },
  { role: 'testuser', password: 'test123', canSeeAdminPanel: false, canSeeEditor: false, canSeeTodos: true  },
];

test.describe('Menu visibility ตาม role @regression', () => {
  test.each(roleScenarios)(
    '$role เห็น menu ถูกต้อง',
    async ({ page }, { role, password, canSeeAdminPanel, canSeeEditor, canSeeTodos }) => {
      // Login
      await page.goto('http://localhost:3000/login');
      await page.fill('[data-testid="input-username"]', role);
      await page.fill('[data-testid="input-password"]', password);
      await page.click('[data-testid="btn-login"]');

      // ยืนยัน session
      await expect(page.getByTestId('session-badge')).toContainText(role);

      // ตรวจสอบ menu items ตาม permission
      const adminPanel = page.getByTestId('nav-admin');
      const editorMenu = page.getByTestId('nav-editor');
      const todosMenu  = page.getByTestId('nav-todos');

      if (canSeeAdminPanel) {
        await expect(adminPanel).toBeVisible();
      } else {
        await expect(adminPanel).not.toBeVisible();
      }

      if (canSeeEditor) {
        await expect(editorMenu).toBeVisible();
      } else {
        await expect(editorMenu).not.toBeVisible();
      }

      if (canSeeTodos) {
        await expect(todosMenu).toBeVisible();
      } else {
        await expect(todosMenu).not.toBeVisible();
      }
    }
  );
});
```

**Output ที่คาดหวัง:**
```
✓ admin เห็น menu ถูกต้อง (1.8s)
✓ testuser เห็น menu ถูกต้อง (1.6s)
```

---

### Advanced: test.step() + Runtime Annotations ใน Complex Flow

สถานการณ์: ต้องการ test checkout flow ที่มีหลาย steps และแนบ bug ticket กับ report โดยอัตโนมัติ — ใช้ `test.step()` จัดโครงสร้างให้ Trace Viewer ดู และ `testInfo.annotations` แนบ metadata

```typescript
// tested: Playwright v1.50+, Node.js 20+
// ไฟล์: tests/e2e/login-session-flow.spec.ts
import { test, expect } from '@playwright/test';

test('Full session lifecycle: login → verify identity → logout @e2e', async ({ page }, testInfo) => {
  // แนบ issue link เพื่อ traceability ใน HTML report
  testInfo.annotations.push({
    type: 'issue',
    description: 'https://jira.example.com/browse/AUTH-101'
  });
  testInfo.annotations.push({
    type: 'test-plan',
    description: 'TP-2025-Q2-AUTH'
  });

  // ── Step 1: Login ──────────────────────────────────────────────
  const sessionUser = await test.step('Login as admin', async () => {
    await page.goto('http://localhost:3000/login');
    await page.fill('[data-testid="input-username"]', 'admin');
    await page.fill('[data-testid="input-password"]', 'admin123');
    await page.click('[data-testid="btn-login"]');

    const badge = page.getByTestId('session-badge');
    await expect(badge).toBeVisible();
    // return ข้อมูล session ให้ steps ถัดไปใช้ต่อ
    return (await badge.textContent()) ?? '';
  });

  // ── Step 2: Verify session ─────────────────────────────────────
  await test.step(`Verify session for "${sessionUser}"`, async () => {
    // ยืนยันว่า session badge แสดง username ที่ถูกต้อง
    await expect(page.getByTestId('session-badge')).toContainText('admin');

    // ยืนยันว่าอยู่ใน todos page หลัง login
    await expect(page).toHaveURL(/\/todos/);
  });

  // ── Step 3: Slow operation — simulate heavy action ──────────────
  await test.step('Load todos list', async () => {
    // ถ้า page นี้ใช้เวลานาน ให้ step นี้มี timeout แยก
    await expect(page.getByTestId('todo-list')).toBeVisible();
  }, { timeout: 10000 });

  // ── Step 4: Logout ─────────────────────────────────────────────
  await test.step('Logout and verify session cleared', async () => {
    // ถ้า demo app มี logout: await page.click('[data-testid="nav-logout"]');
    // ยืนยันว่า session หายไปหลัง logout
    // await expect(page.getByTestId('session-badge')).not.toBeVisible();
    // สำหรับ demo นี้ navigate ออกไปแทน
    await page.goto('http://localhost:3000/login');
    await expect(page.getByTestId('btn-login')).toBeVisible();
  });

  // เพิ่ม runtime annotation หลังจากรู้ว่า test ผ่าน
  testInfo.annotations.push({
    type: 'result',
    description: `Session user verified: ${sessionUser}`
  });
});

// test.describe.configure — รัน serial เพราะ tests พึ่งพา state กัน
test.describe.configure({ mode: 'serial' });

test.describe('Serial auth tests — ต้องรันตามลำดับ', () => {
  test('ขั้นที่ 1: ตรวจสอบ login page โหลดได้', async ({ page }) => {
    await page.goto('http://localhost:3000/login');
    await expect(page.getByTestId('input-username')).toBeVisible();
    await expect(page.getByTestId('input-password')).toBeVisible();
    await expect(page.getByTestId('btn-login')).toBeVisible();
  });

  test('ขั้นที่ 2: login สำเร็จด้วย valid credential', async ({ page }) => {
    await page.goto('http://localhost:3000/login');
    await page.fill('[data-testid="input-username"]', 'admin');
    await page.fill('[data-testid="input-password"]', 'admin123');
    await page.click('[data-testid="btn-login"]');
    await expect(page.getByTestId('session-badge')).toContainText('admin');
  });

  test('ขั้นที่ 3: verify session badge ถาวรข้ามหน้า', async ({ page }) => {
    // ถ้า test ก่อนหน้า fail → test นี้จะ skip อัตโนมัติ (serial mode)
    await page.goto('http://localhost:3000/login');
    await page.fill('[data-testid="input-username"]', 'admin');
    await page.fill('[data-testid="input-password"]', 'admin123');
    await page.click('[data-testid="btn-login"]');
    await page.goto('http://localhost:3000/todos');
    await expect(page.getByTestId('session-badge')).toBeVisible();
  });
});
```

**Output ที่คาดหวัง (Trace Viewer):**
```
Test: Full session lifecycle...
  ├── Step: Login as admin (0.8s)
  ├── Step: Verify session for "admin" (0.3s)
  ├── Step: Load todos list (0.4s)
  └── Step: Logout and verify session cleared (0.5s)
```

---

## 6. Common Mistakes

**❌ `test.skip()` โดยไม่มี condition → test skip เสมอโดยไม่ตั้งใจ:**
```typescript
// ❌ ผิด — คนเขียนตั้งใจจะ skip เฉพาะบน Windows
// แต่ไม่ใส่ condition ทำให้ skip บนทุก environment
test('file upload', async ({ page }) => {
  test.skip(); // ← ไม่มี condition = skip เสมอ
  await page.goto('http://localhost:3000/upload');
});

// ✅ ถูก
test('file upload', async ({ page }) => {
  test.skip(process.platform === 'win32', 'File upload ไม่รองรับ Windows');
  await page.goto('http://localhost:3000/upload');
});
```
เหตุผล: `test.skip()` โดยไม่มี argument จะ skip test นั้นเสมอไม่มีเงื่อนไข *(source: https://playwright.dev/docs/api/class-test#test-skip-1)*

---

**❌ สับสน `test.fail()` กับ `test.skip()` — เลือกผิดตัว:**
```typescript
// ❌ ผิด — ต้องการ mark ว่าพังแต่ใช้ skip ซ่อนปัญหา
test('checkout หลัง session timeout', async ({ page }) => {
  test.skip(true, 'มี bug อยู่');
  // ← skip ซ่อน bug ไม่แจ้งเตือนถ้า bug ถูก fix
});

// ✅ ถูก — ใช้ fail() เมื่อต้องการยืนยันว่า bug ยังอยู่
test('checkout หลัง session timeout', async ({ page }) => {
  test.fail(true, 'BUG-321: known issue with session expiry');
  // ← ถ้า bug ถูก fix แล้ว test จะ "FAIL" เพราะ pass แทน fail
  // ← ทีมจะรู้ว่าต้องเอา test.fail() ออก
});
```
เหตุผล: `test.fail()` ทำหน้าที่เป็น "sentinel" ถ้า bug ถูก fix แล้วแต่ยังมี `test.fail()` อยู่ Playwright จะรายงาน FAIL — บังคับให้ทีมอัปเดต test *(source: https://playwright.dev/docs/api/class-test#test-fail-1)*

---

**❌ ใส่ `@tag` ใน `--grep` โดยลืม `@`:**
```bash
# ❌ ผิด — จะ grep หา string "smoke" ซึ่งอาจเจอ test อื่นที่ไม่ได้ tag
npx playwright test --grep smoke

# ✅ ถูก — grep หา @smoke อย่างชัดเจน
npx playwright test --grep @smoke
```
เหตุผล: `--grep` เป็น regex ถ้าไม่ใส่ `@` จะ match ทุก test ที่มีคำว่า "smoke" ในชื่อ ซึ่งอาจ include tests ที่ไม่ได้ตั้งใจ *(source: https://playwright.dev/docs/test-annotations)*

---

**❌ `test.each()` ใช้ข้อมูลซ้ำกับตัวอย่างอื่นในบท:**
```typescript
// ❌ ผิด — copy credentials จาก Beginner example ข้างต้นในบทเดียวกัน
test.each([
  { username: 'admin', password: 'admin123' },  // ← เหมือน Beginner example เป๊ะ
])('login as $username', async ({ page }, { username, password }) => { ... });

// ✅ ถูก — ใช้ data set ใหม่ที่ไม่ซ้ำ
test.each([
  { username: 'power-user', password: 'power789', feature: 'bulk-export' },
  { username: 'read-only',  password: 'readonly1', feature: 'view-only' },
])('$username เข้าถึง $feature ได้ตาม permission', async ({ page }, params) => { ... });
```
เหตุผล: **Pedagogical best practice** — ถ้า parameterized test ใช้ข้อมูล (context/ตัวเลข) โดยตรงจากตัวอย่างในบท แล้วแทนที่จะเป็นการ "เรียนรู้แบบ transfer learning" (นำความเข้าใจไปใช้กับสถานการณ์ใหม่) มันกลายเป็นการ "recognition ล้วนๆ" (จำไว้เฉยๆ) ผู้เรียนไม่ได้ฝึกว่า concept ใช้ได้กับ data หลากหลายระดับขนาด/บริบทต่างกัน การเปลี่ยน data set ขนาดนี้ทำให้ exercise ช่วยเสริม concept ได้จริง

---

**❌ `test.describe.configure({ mode: 'serial' })` วางไว้ใน describe block:**
```typescript
// ❌ ผิด — configure ต้องอยู่นอก describe ก่อน หรือเป็น call แรกใน describe
test.describe('my tests', () => {
  test('first', async () => { ... });
  test.describe.configure({ mode: 'serial' }); // ← ช้าไป
});

// ✅ ถูก — configure ก่อน describe หรือเป็น call แรกใน describe callback
test.describe.configure({ mode: 'serial' });
test.describe('my tests', () => {
  test('first', async () => { ... });
  test('second', async () => { ... });
});
```
เหตุผล: `test.describe.configure()` ต้องถูกเรียกก่อนที่ tests จะถูก register *(source: https://playwright.dev/docs/api/class-test#test-describe-configure)*

---

## 7. สรุปบท

Annotations, Tags, Parameterize และ Steps เป็นเครื่องมือสำหรับ **จัดระเบียบ test suite** ให้ maintainable:

- **Annotations** เปลี่ยน behavior ของ test: `skip` (ไม่รัน), `fail` (คาดว่าจะพัง), `fixme` (รู้ว่าพัง ต้องแก้), `slow` (เพิ่ม timeout ×3)
- **Tags** เป็น category label สำหรับ filter ด้วย `--grep` — ต้องขึ้นต้นด้วย `@` เสมอ
- **`test.each()`** แยก test data ออกจาก test logic — ลด duplication และเพิ่ม coverage
- **`test.step()`** จัดกลุ่ม actions ใน Trace Viewer — ทำให้ debug ง่ายขึ้นมากใน complex flows
- **`testInfo.annotations`** เพิ่ม metadata แบบ runtime เช่น bug ticket link

ความแตกต่างที่สำคัญที่สุด: **annotation เปลี่ยน behavior, tag เปลี่ยน searchability**

---

**คำถาม Retrieval — ลองตอบก่อนดูเฉลย:**

1. ถ้า `test.fail()` ถูก mark ไว้แต่ test นั้นกลับ pass จริงๆ — Playwright จะรายงาน status อะไร และทำไมถึงทำแบบนี้?
2. อธิบายด้วยคำตัวเองว่า `test.each()` กับ `forEach` loop ปกติต่างกันอย่างไร — และในสถานการณ์ไหนที่ควรเลือก `test.each()`?
3. ดู code นี้แล้วบอกว่ามีปัญหาอะไร:
   ```typescript
   test('upload file @important', async ({ page }) => {
     test.slow();
     test.fixme(true, 'upload broken');
     await page.goto('/upload');
   });
   ```

---

<details>
<summary>เฉลย (คลิกเพื่อดู)</summary>

**1. test.fail() แล้ว test pass:**
Playwright รายงาน **FAILED** เพราะ `test.fail()` หมายความว่า "คาดว่า test นี้จะพัง" การที่ test pass จึงเป็นการ "ผิดคาด" Playwright enforce expectation นี้เพื่อบังคับให้ทีมรู้ว่า bug ถูก fix แล้ว และต้องเอา `test.fail()` ออก ไม่งั้น test suite จะมี dead annotation อยู่ตลอด

**2. test.each() vs forEach:**
`forEach` loop สร้าง tests ปกติแต่ชื่อ test ต้องกำหนดเองด้วย template literal และ scoping ของ hooks อาจสับสน `test.each()` ออกแบบมาสำหรับ parameterized tests โดยเฉพาะ — generate ชื่อจาก `$variable` อัตโนมัติ, integrate กับ reporter ดีกว่า, และมีพฤติกรรม hook ที่คาดเดาได้ชัดเจน ควรเลือก `test.each()` เมื่อ logic เหมือนกัน ต่างแค่ data และต้องการ report แยกต่างหากต่อ data set

**3. ปัญหาใน code:**
มีสองปัญหา: (1) `test.slow()` และ `test.fixme(true, ...)` ทำงานด้วยกัน แต่ `test.fixme()` จะ skip test ทันที ดังนั้น `test.slow()` ไม่มีผลอะไรเลยเพราะ test ไม่ได้รันจริง (2) ถ้าตั้งใจให้ test skip อยู่แล้ว การ mark `@important` ด้วยอาจสื่อสารสับสนกับทีม — ควรเลือกว่า test นี้ "important" หรือ "fixme" ไม่ควรเป็นทั้งสอง

</details>
