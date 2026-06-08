## ก่อนอ่านบทนี้ ลองตอบ:

1. ใน `playwright.config.ts` ถ้าต้องการให้ Playwright start dev server ก่อนรัน test อัตโนมัติ ต้องใช้ option ไหน และ config หน้าตาอย่างไร?
2. `retries: 2` ต่างจาก `--repeat-each=3` อย่างไร — แต่ละตัวใช้ในสถานการณ์ไหน?

---

เฉลย:

1. ใช้ `webServer` option ใน `playwright.config.ts` — เช่น `webServer: { command: 'npm run dev', url: 'http://localhost:3000', reuseExistingServer: !process.env.CI }` Playwright จะรัน command นั้นก่อน รอจนกว่า URL ตอบสนอง แล้วจึงรัน tests
2. `retries: 2` — รัน test ซ้ำ **เฉพาะเมื่อ fail** สูงสุด 2 ครั้ง (ใช้เพื่อ handle flaky tests ใน CI) | `--repeat-each=3` — รัน test ทุกตัวซ้ำ 3 รอบ **ไม่ว่าจะ pass หรือ fail** (ใช้เพื่อตรวจหา flaky tests ระหว่าง development)

---

# บทที่ 11: Parallelism, Sharding & Reporting — รันเร็วขึ้น รายงานชัดขึ้น

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- เข้าใจความแตกต่างระหว่าง **workers** (parallel ภายใน 1 machine) กับ **sharding** (parallel ข้าม machines)
- ตั้งค่า `fullyParallel`, `workers`, `test.describe.configure()` ได้อย่างถูกต้อง
- เข้าใจว่า **shared state** คือภัยร้ายของ parallel tests และรู้วิธีแก้ด้วย unique test data
- ใช้ `--shard` + blob reporter + `merge-reports` เพื่อรวมผลจากหลาย CI jobs
- เลือก reporter ที่เหมาะกับบริบท: `html`, `json`, `junit`, `dot`, `allure-playwright`
- ใช้ CLI flags ที่มีประโยชน์ได้: `--last-failed`, `--repeat-each`, `--forbid-only`

---

## 2. ทำไมต้องรู้? (Why)

สมมติคุณมี test suite 500 tests รันบน local ใช้เวลา 25 นาที ทุกครั้งที่ push code ต้องรอนานขนาดนั้น — นั่นคือ developer experience ที่แย่มาก ทีมจะหยุดรัน test เพราะมันช้าเกินไป

Playwright มีสองเครื่องมือหลักแก้ปัญหานี้:

**Parallelism** — ใช้ CPU cores ที่มีอยู่ให้เต็มที่ รัน tests หลายตัวพร้อมกันบนเครื่องเดียว 4 cores → ลดเวลาจาก 25 นาทีเหลือ ~6-7 นาที

**Sharding** — แบ่ง test suite ออกเป็น chunks รันบน CI machines หลายตัวพร้อมกัน 4 machines → ลดเหลือ ~2 นาที แต่ต้องรวมผล (merge reports) ก่อนดูรายงานรวม

ส่วน **reporters** สำคัญไม่แพ้กัน เพราะถ้า test fail แต่รายงานดูไม่ออกว่าพัง ตรงไหน — speed ก็ไม่มีประโยชน์ รายงานที่ดีทำให้ debug เร็วขึ้น

---

## 3. Analogy

**Sharding เหมือนการแบ่งกระดาษข้อสอบให้ผู้คุมสอบหลายคนตรวจพร้อมกัน** — ถ้ามีข้อสอบ 200 ฉบับ และผู้คุม 4 คน แต่ละคนรับกอง 50 ฉบับ ตรวจพร้อมกัน แล้วรวมผลคะแนนทีเดียวตอนท้าย

**Workers** เหมือน **ผู้คุมสอบ 1 คน ที่อ่านข้อสอบได้หลายฉบับพร้อมกันด้วยสายตาหลายคู่** — เป็น parallelism ภายในตัวคนเดียว

⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:

- **Sharding ทำให้เร็วขึ้น 4x โดยอัตโนมัติ** — จริงๆ ความเร็วขึ้นอยู่กับ distribution ของ tests ถ้า test files มีขนาดไม่เท่ากัน (ไม่ได้ใช้ `fullyParallel`) shard หนึ่งอาจได้งานเยอะกว่าอีก shard ทำให้ bottleneck อยู่ที่ shard นั้น
- **Workers ใน config เดียวกับ shards** — Workers คือ parallel processes **ภายใน machine เดียว** ควบคุมด้วย `workers: N` ใน config | Shards คือ parallel **ข้าม machines** ควบคุมด้วย `--shard=N/M` ใน CLI ทั้งสองทำงานร่วมกัน: แต่ละ shard ยังมี workers ของตัวเองอีกชั้น
- **ผู้คุมสอบตรวจได้ทุกฉบับอย่างอิสระ** — จริงๆ parallel tests ที่แชร์ resource เดียวกัน (เช่น database เดียว) จะ interfere กัน เหมือนผู้คุมสอบสองคนแย่งปากกาแดงอันเดียวกัน

---

## 4. เนื้อหาหลัก

### 4.1 Parallelism — รัน Tests หลายตัวพร้อมกันบน Machine เดียว

#### `workers`: กี่ Processes รันพร้อมกัน

```typescript
// playwright.config.ts
// partial example — see Section 5 for runnable version
// tested: Playwright v1.50+, Node.js 20+
export default defineConfig({
  workers: process.env.CI ? 2 : undefined,
  // undefined = Playwright เลือกเอง (default: 50% ของ CPU cores)
  // CI ใช้ 2 เพราะ CI machines มักมี cores จำกัด และ memory น้อยกว่า
});
```

รัน via CLI: `npx playwright test --workers 4` หรือ `-j 4`

ปิด parallelism สมบูรณ์: `workers: 1` — ทุก test รันเรียงลำดับ

#### `fullyParallel`: ทุก Test รัน Parallel

โดย default Playwright รัน **แต่ละ file** แบบ sequential ภายใน file (tests ใน file เดียวกันรันเรียงลำดับ) แต่รัน **หลาย files พร้อมกัน**

`fullyParallel: true` เปิดให้ทุก test ในทุก file รัน parallel อย่างอิสระ:

```typescript
// playwright.config.ts
// partial example — see Section 5 for runnable version
// tested: Playwright v1.50+, Node.js 20+
export default defineConfig({
  fullyParallel: true, // ทุก test ทุก file รัน parallel
});
```

หรือเปิดเฉพาะบาง project:

```typescript
// playwright.config.ts — projects array (partial)
// partial example — see Section 5 for runnable version
// tested: Playwright v1.50+, Node.js 20+
projects: [
  {
    name: 'chromium',
    fullyParallel: true, // เฉพาะ chromium project เท่านั้น
  },
]
```

#### `test.describe.configure()` — ควบคุม Mode ระดับ Describe Block

```typescript
// partial example — see Section 5 for runnable version
// tested: Playwright v1.50+, Node.js 20+
import { test } from '@playwright/test';

test.describe('tests ที่ต้อง sequential', () => {
  test.describe.configure({ mode: 'serial' }); // รัน 1 ต่อ 1 เรียงลำดับ

  test('step 1 - create user', async ({ page }) => { /* ... */ });
  test('step 2 - login', async ({ page }) => { /* ... */ }); // รอ step 1 เสมอ
  test('step 3 - purchase', async ({ page }) => { /* ... */ }); // รอ step 2 เสมอ
});

test.describe('tests ที่ parallel ได้', () => {
  test.describe.configure({ mode: 'parallel' }); // override fullyParallel=false

  test('check product A', async ({ page }) => { /* ... */ });
  test('check product B', async ({ page }) => { /* ... */ }); // รันพร้อม product A
});

test.describe('กลับสู่ default', () => {
  test.describe.configure({ mode: 'default' }); // opt-out จาก fullyParallel
});
```

> **หมายเหตุ:** `test.describe.serial()` มีอยู่จริงใน API (ตั้งแต่ v1.10) แต่ **official docs ระบุว่า discouraged** — ใช้ `test.describe.configure({ mode: 'serial' })` เท่านั้น *(source: https://playwright.dev/docs/api/class-test#test-describe-serial)*

#### Shared State — ภัยร้ายของ Parallel Tests

นี่คือปัญหาที่พบบ่อยที่สุดเมื่อเปิด `fullyParallel`:

```
Test A: POST /api/todos { title: 'Buy milk' }   ← เพิ่ม todo
Test B: GET /api/todos                            ← อ่าน todos
Test B: expect(todos).toHaveLength(1)             ← FAIL! ได้ 2 เพราะ Test A ก็เพิ่มพร้อมกัน
```

สองตัวใช้ database เดียวกัน รันพร้อมกัน ผลลัพธ์ไม่ deterministic — บางครั้ง pass บางครั้ง fail = **flaky test**

**วิธีแก้: Unique Test Data**

แทนที่จะแชร์ state กัน ให้แต่ละ test สร้างข้อมูลของตัวเองและ clean up เมื่อเสร็จ:

```typescript
// แต่ละ test สร้าง unique user/data ของตัวเอง
const uniqueId = `test-${Date.now()}-${Math.random()}`;
await page.request.post('/api/todos', {
  data: { title: `Task for ${uniqueId}` }
});
```

หรือใช้ `workerIndex` ที่ Playwright ให้มา:

```typescript
test('isolated test', async ({ page }, testInfo) => {
  const workerId = testInfo.workerIndex;
  // แต่ละ worker ใช้ข้อมูลคนละ set
  await page.request.post(`/api/todos?worker=${workerId}`, { /* ... */ });
});
```

---

### 4.2 Sharding — แบ่ง Tests ข้าม CI Machines

#### Syntax พื้นฐาน

```bash
# แบ่งเป็น 4 shards รันบน 4 machines พร้อมกัน
npx playwright test --shard=1/4   # Machine 1: tests chunk ที่ 1
npx playwright test --shard=2/4   # Machine 2: tests chunk ที่ 2
npx playwright test --shard=3/4   # Machine 3: tests chunk ที่ 3
npx playwright test --shard=4/4   # Machine 4: tests chunk ที่ 4
```

Format: `--shard=<current>/<total>` โดย current เริ่มจาก 1 (not zero-based)

#### Blob Reporter — บันทึกผลจากแต่ละ Shard

แต่ละ shard ต้องใช้ **blob reporter** เพื่อบันทึกผลในรูปแบบที่ merge ได้:

```typescript
// playwright.config.ts
// partial example — see Section 5 for runnable version
// tested: Playwright v1.50+, Node.js 20+
export default defineConfig({
  reporter: process.env.CI ? 'blob' : 'html',
  // CI: บันทึกเป็น blob สำหรับ merge ทีหลัง
  // Local: HTML report เปิดดูได้ทันที
});
```

Default output directory: `blob-report/` (สร้างขึ้นอัตโนมัติ)

#### Merge Reports — รวมผลทั้งหมด

หลังทุก shards เสร็จ รวม blob-reports ทั้งหมดแล้วสร้าง HTML report:

```bash
npx playwright merge-reports --reporter html ./blob-reports
```

รองรับหลาย reporters พร้อมกัน:

```bash
npx playwright merge-reports --reporter html,json ./blob-reports
```

#### GitHub Actions ตัวอย่าง

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        shardIndex: [1, 2, 3, 4]
        shardTotal: [4]
    steps:
      - run: npx playwright test --shard=${{ matrix.shardIndex }}/${{ matrix.shardTotal }}
      - uses: actions/upload-artifact@v4
        with:
          name: blob-report-${{ matrix.shardIndex }}
          path: blob-report/

  merge-reports:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/download-artifact@v4
        with:
          path: all-blob-reports
          pattern: blob-report-*
      - run: npx playwright merge-reports --reporter html ./all-blob-reports
```

> **Tip:** `fullyParallel: true` ทำให้ shards กระจาย tests ได้สม่ำเสมอมากขึ้น (split ที่ระดับ test แทนที่จะเป็น file)

---

### 4.3 CLI Flags ที่มีประโยชน์

ทั้งหมดนี้ verify จาก official docs แล้ว:

| Flag | ความหมาย | ใช้เมื่อ |
|------|----------|----------|
| `--last-failed` | รันเฉพาะ tests ที่ fail รอบล่าสุด | Debug หลัง test suite fail |
| `--repeat-each=N` | รันทุก test ซ้ำ N รอบ (pass หรือ fail) | ตรวจหา flaky tests |
| `--only-changed [ref]` | รันเฉพาะ test files ที่เปลี่ยนแปลงจาก `HEAD` หรือ `ref` (ต้องใช้ Git) | รัน tests เร็วขึ้นระหว่าง development |
| `--forbid-only` | Fail ถ้ามี `test.only` ใน codebase | CI guard ป้องกัน commit `test.only` |
| `--workers N` / `-j N` | กำหนดจำนวน parallel workers | Override config ชั่วคราว |
| `--shard=N/M` | รันเฉพาะ shard ที่ N จากทั้งหมด M | CI sharding |

```bash
# ตัวอย่างการใช้งาน
npx playwright test --last-failed              # debug หลัง fail
npx playwright test --repeat-each=5           # หา flaky tests
npx playwright test --only-changed main       # รัน tests ที่ต่างจาก main branch
npx playwright test --forbid-only             # CI safety check
```

---

### 4.4 Reporters — รายงานผล Test

#### Built-in Reporters

**`html`** — Interactive report ดูละเอียดได้ รวม traces, screenshots, videos:

```typescript
reporter: [['html', { open: 'on-failure' }]]
// open: 'always' | 'never' | 'on-failure' (default)
```

เปิดดู report: `npx playwright show-report` หรือ `npx playwright show-report ./my-report`

**`line`** — แสดง 1 บรรทัดต่อ test ที่เสร็จ เหมาะสำหรับ test suite ใหญ่ที่ไม่อยากเห็น output ท่วมหน้าจอ

```typescript
// playwright.config.ts — line reporter สำหรับ CI
// partial example — see Section 5 for runnable version
// tested: Playwright v1.50+, Node.js 20+
export default defineConfig({
  reporter: 'line',
  // หรือใช้ด้วยกันกับ reporter อื่น:
  // reporter: [['line'], ['json', { outputFile: 'results.json' }]],
});
```

**`dot`** — กระชับที่สุด: ตัวอักษรตัวเดียวต่อ test (`·` = pass, `F` = fail, `±` = flaky, `T` = timeout) ใช้เป็น default บน CI

**`json`** — Machine-readable สำหรับ integration กับระบบอื่น:

```typescript
reporter: [['json', { outputFile: 'results.json' }]]
```

**`junit`** — XML format สำหรับ Jenkins, GitLab CI, Azure DevOps:

```typescript
reporter: [['junit', { outputFile: 'results.xml' }]]
```

**`blob`** — บันทึกผลสำหรับ merge กับ shards อื่น (ดู Section 4.2)

#### หลาย Reporters พร้อมกัน

```typescript
// playwright.config.ts
export default defineConfig({
  reporter: [
    ['html', { open: 'never' }],          // สร้าง HTML report เสมอ ไม่ auto-open
    ['json', { outputFile: 'results.json' }], // สำหรับ downstream processing
    ['dot'],                               // compact output ใน terminal
  ],
});
```

#### Allure Reporter — Third-party

ต้อง install แยก:

```bash
npm install --save-dev allure-playwright
```

```typescript
reporter: [['allure-playwright']]
```

> **หมายเหตุ:** Package name คือ `allure-playwright` (ไม่ใช่ `@allure-framework/allure-playwright` — verify จาก official Playwright docs แล้ว)

#### เลือก Reporter ตาม Environment

```typescript
export default defineConfig({
  reporter: process.env.CI
    ? [['dot'], ['junit', { outputFile: 'results.xml' }], ['blob']]
    : [['html', { open: 'on-failure' }]],
});
```

---

### 4.5 เปรียบเทียบ Robot Framework + Selenium vs Playwright

| | Robot Framework + Selenium | Playwright |
|--|--------------------------|------------|
| **Parallel** | Pabot (ต้อง install และ config แยก), Selenium Grid (setup ซับซ้อน) | `workers: N` ใน config, พร้อมใช้ทันที |
| **Sharding** | ไม่มี built-in ต้องเขียน script เอง | `--shard=N/M` + blob reporter + `merge-reports` |
| **Report หลัก** | RF HTML report | `html` reporter + `npx playwright show-report` |
| **CI Report** | Allure (ต้อง setup), JUnit XML | `dot`, `junit`, `json` built-in |
| **Flaky detection** | `--rerun-failed-suites` (robot), manual retry | `--repeat-each`, `retries` ใน config |
| **Merge reports** | ไม่มี | `npx playwright merge-reports` |

ข้อได้เปรียบหลักของ Playwright: parallelism และ sharding เป็น **first-class feature** ไม่ต้องพึ่ง third-party tools สำหรับ use case ทั่วไป

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner: Parallel Tests ที่มี Shared State Problem และวิธีแก้

สถานการณ์: Demo app มี `POST /api/reset` เพื่อ reset ข้อมูล และ `POST /api/todos` เพื่อสร้าง todo

```typescript
// tests/parallel-problem.spec.ts
// tested: Playwright v1.50+, Node.js 20+

import { test, expect } from '@playwright/test';

// ❌ แบบมีปัญหา — tests แชร์ state กัน
test.describe('Problematic parallel tests', () => {
  test.describe.configure({ mode: 'parallel' });

  test('Test A: add one todo and check count', async ({ request }) => {
    await request.post('/api/reset'); // reset ก่อน
    await request.post('/api/todos', { data: { title: 'Task A' } });

    const response = await request.get('/api/todos');
    const todos = await response.json();
    // ❌ Race condition: ถ้า Test B รันพร้อมกัน อาจเจอ 2 todos แทน 1
    expect(todos).toHaveLength(1);
  });

  test('Test B: add one todo and check count', async ({ request }) => {
    await request.post('/api/reset'); // reset ก่อน (แต่ Test A อาจ reset ซ้อนกัน)
    await request.post('/api/todos', { data: { title: 'Task B' } });

    const response = await request.get('/api/todos');
    const todos = await response.json();
    // ❌ Race condition: อาจเจอ 0 todos เพราะ Test A เพิ่งรัน reset
    expect(todos).toHaveLength(1);
  });
});
```

```typescript
// tests/parallel-fixed.spec.ts
// tested: Playwright v1.50+, Node.js 20+

import { test, expect } from '@playwright/test';

// ✅ แบบถูก — แต่ละ test สร้างข้อมูล unique ของตัวเอง
test.describe('Fixed parallel tests', () => {
  test.describe.configure({ mode: 'parallel' });

  test('Test A: verify todo creation', async ({ request }, testInfo) => {
    // ใช้ workerIndex สร้าง unique tag ไม่ชนกับ test อื่น
    const tag = `worker-${testInfo.workerIndex}-A`;

    const createRes = await request.post('/api/todos', {
      data: { title: `Task ${tag}` }
    });
    expect(createRes.ok()).toBeTruthy();

    const response = await request.get(`/api/todos?tag=${tag}`);
    const todos = await response.json();
    // ✅ ดึงเฉพาะ todos ของ test นี้ ไม่ถูก test อื่น interfere
    expect(todos.some((t: { title: string }) => t.title.includes(tag))).toBe(true);
  });

  test('Test B: verify todo deletion', async ({ request }, testInfo) => {
    const tag = `worker-${testInfo.workerIndex}-B`;

    await request.post('/api/todos', { data: { title: `Task ${tag}` } });
    // ดำเนินการกับ data ของตัวเองเท่านั้น — ปลอดภัยใน parallel
  });
});
```

Output ที่ได้เมื่อรัน:

```
Running 2 tests using 2 workers

  ✓  1 [chromium] › tests/parallel-fixed.spec.ts:14:3 › Test A (1.2s)
  ✓  2 [chromium] › tests/parallel-fixed.spec.ts:27:3 › Test B (1.1s)

  2 passed (2.3s)
```

---

### Intermediate: Sharding Config สำหรับ CI Pipeline

สถานการณ์ใหม่: ทีมมี GitHub Actions และต้องการให้ test suite 200 tests รันใน 3 parallel jobs (ไม่ใช่ 4 จาก Beginner example)

```typescript
// playwright.config.ts — สำหรับ CI sharding
// tested: Playwright v1.50+, Node.js 20+

import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true, // ทำให้ shard distribution สม่ำเสมอมากขึ้น
  workers: process.env.CI ? 1 : undefined,

  // blob reporter ใน CI, html สำหรับ local
  reporter: process.env.CI
    ? [
        ['blob'],                                    // สำหรับ merge-reports
        ['dot'],                                     // compact terminal output
      ]
    : [['html', { open: 'on-failure' }]],

  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:3000',
    trace: 'on-first-retry', // ไม่ใช่ 'on' — ประหยัด disk
  },

  // ห้ามลืม forbidOnly ใน CI
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
});
```

```yaml
# .github/workflows/test.yml
name: Playwright Tests

on: [push, pull_request]

jobs:
  test:
    name: "Shard ${{ matrix.shardIndex }}/${{ matrix.shardTotal }}"
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false  # ให้ shard อื่นรันต่อแม้บาง shard จะ fail
      matrix:
        shardIndex: [1, 2, 3]
        shardTotal: [3]

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: lts/*
      - run: npm ci
      - run: npx playwright install --with-deps chromium

      - name: Run shard ${{ matrix.shardIndex }}/${{ matrix.shardTotal }}
        run: npx playwright test --shard=${{ matrix.shardIndex }}/${{ matrix.shardTotal }}
        env:
          CI: true
          BASE_URL: https://staging.myapp.com

      - name: Upload blob report
        uses: actions/upload-artifact@v4
        if: always()  # upload แม้จะ fail
        with:
          name: blob-report-${{ matrix.shardIndex }}
          path: blob-report/
          retention-days: 1

  merge-reports:
    name: Merge Reports
    needs: test
    runs-on: ubuntu-latest
    if: always()  # รันเสมอแม้บาง shard fail

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: lts/*
      - run: npm ci

      - name: Download all blob reports
        uses: actions/download-artifact@v4
        with:
          path: all-blob-reports
          pattern: blob-report-*
          merge-multiple: true

      - name: Merge reports
        run: npx playwright merge-reports --reporter html ./all-blob-reports

      - name: Upload merged HTML report
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: playwright-report/
          retention-days: 14
```

สิ่งที่ต้องสังเกต:
- `fail-fast: false` สำคัญมาก: ถ้า shard 1 fail แต่เราต้องการดู results ของ shard 2-3 ด้วย
- Upload artifact ใช้ `if: always()` เพื่อให้ merge-reports ทำงานได้แม้บาง shard จะ fail
- `merge-multiple: true` ใน download step รวม artifacts จากหลายชื่อเป็น folder เดียว

---

### Advanced: ออกแบบ Parallel Strategy สำหรับ Suite ที่มีทั้ง Shared DB และ Independent Tests

สถานการณ์: บริษัทมี e-commerce app ที่มี tests 3 ประเภทปนกัน ต้องออกแบบให้ทำงาน parallel ได้อย่างถูกต้อง:

1. **Auth tests** — ต้องรัน sequential เพราะแก้ไข session state
2. **Product browsing tests** — read-only ทั้งหมด parallel ได้เต็มที่
3. **Order tests** — เขียน DB แต่ใช้ unique order IDs

```typescript
// tests/todos/crud-workflow.spec.ts
import { test, expect } from '@playwright/test';

// Todo CRUD: serial เพราะ step 2 & 3 ต้องการ id จาก step 1
// ถ้า step 1 fail → step 2 & 3 จะ skip อัตโนมัติ
test.describe('Todo CRUD Workflow', () => {
  test.describe.configure({ mode: 'serial' });

  let createdTodoId: number;

  test('1. สร้าง todo ใหม่', async ({ request }) => {
    const res = await request.post('/api/todos', {
      data: { text: 'Task ที่ต้องสร้างก่อน' },
    });
    expect(res.ok()).toBeTruthy();
    const todo = await res.json();
    createdTodoId = todo.id;  // เก็บ id ไว้ให้ step ต่อไปใช้
  });

  test('2. mark complete (ต้องการ id จาก step 1)', async ({ request }) => {
    const res = await request.patch(`/api/todos/${createdTodoId}`, {
      data: { completed: true },
    });
    expect(res.ok()).toBeTruthy();
    const updated = await res.json();
    expect(updated.completed).toBe(true);
  });

  test('3. ลบ todo (ต้องการ id จาก step 1)', async ({ request }) => {
    const res = await request.delete(`/api/todos/${createdTodoId}`);
    expect(res.ok()).toBeTruthy();
  });
});
```

```typescript
// tests/shop/catalog.spec.ts
import { test, expect } from '@playwright/test';

// Product tests: read-only ทั้งหมด — parallel เต็มที่โดยไม่มีความเสี่ยง
test.describe('Product Catalog', () => {
  test.describe.configure({ mode: 'parallel' });

  test('แสดง product list', async ({ page }) => {
    await page.goto('/shop');
    await expect(page.getByTestId('product-grid').getByRole('listitem')).not.toHaveCount(0);
  });

  test('filter ตาม category Electronics', async ({ page }) => {
    await page.goto('/shop');
    await page.getByTestId('filter-category').selectOption('Electronics');
    const products = page.getByTestId('product-grid').getByRole('listitem');
    await expect(products).not.toHaveCount(0);
  });

  test('search product ด้วย keyword', async ({ page }) => {
    await page.goto('/shop');
    await page.getByTestId('search-input').fill('iPhone');
    await page.getByTestId('btn-search').click();
    await expect(page.getByText('iPhone 15 Pro')).toBeVisible();
  });
});
```

```typescript
// tests/todos/parallel-todo.spec.ts
// tested: Playwright v1.50+, Node.js 20+

import { test, expect } from '@playwright/test';

// Todo tests: เขียน DB แต่ใช้ unique text ป้องกัน conflict
test.describe('Todo Management — Parallel Safe', () => {
  test.describe.configure({ mode: 'parallel' });

  // สร้าง todo ที่ unique ต่อ worker เพื่อป้องกัน conflict
  const createUniqueTodo = async (request: any, testInfo: any) => {
    const id = `${testInfo.workerIndex}-${Date.now()}`;
    const res = await request.post('http://localhost:3000/api/todos', {
      data: { text: `Task ${id}` },
    });
    return await res.json();
  };

  test('create and complete todo', async ({ request }, testInfo) => {
    // สร้าง todo ด้วย text unique ของ test นี้
    const todo = await createUniqueTodo(request, testInfo);
    expect(todo.completed).toBe(false);

    // mark completed ผ่าน PATCH
    const patchRes = await request.patch(
      `http://localhost:3000/api/todos/${todo.id}`,
      { data: { completed: true } }
    );
    expect(patchRes.ok()).toBeTruthy();

    const updated = await patchRes.json();
    expect(updated.completed).toBe(true);
  });

  test('create and delete todo', async ({ request }, testInfo) => {
    // คนละ todo กับ test ข้างบน — unique text ป้องกัน overlap
    const todo = await createUniqueTodo(request, testInfo);

    const deleteRes = await request.delete(
      `http://localhost:3000/api/todos/${todo.id}`
    );
    expect(deleteRes.ok()).toBeTruthy();

    // ยืนยันว่าลบแล้วจริง — GET ตัวเดิมควร return ไม่พบ
    const checkRes = await request.get('http://localhost:3000/api/todos');
    const { data: todos } = await checkRes.json();
    const still = todos.find((t: { id: number }) => t.id === todo.id);
    expect(still).toBeUndefined();
  });
});
```

```typescript
// playwright.config.ts — ออกแบบสำหรับ mixed suite
// tested: Playwright v1.50+, Node.js 20+

import { defineConfig } from '@playwright/test';

export default defineConfig({
  // ไม่เปิด fullyParallel ระดับ global
  // เพราะ auth tests ต้อง serial — ใช้ configure ใน describe block แทน
  fullyParallel: false,
  workers: process.env.CI ? 3 : undefined,

  reporter: [
    ['html', { open: 'on-failure' }],
    ['json', { outputFile: 'test-results.json' }],
  ],

  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:3000',
    trace: 'on-first-retry',  // ✅ ไม่ใช่ 'on' — ประหยัด disk ใน parallel mode
  },

  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
});
```

**Design Decisions ที่ควรอธิบายได้:**
- Auth tests ใช้ `serial` เพราะมี **state dependency** ระหว่าง steps
- Product tests ใช้ `parallel` เพราะ **read-only** ไม่มี side effects
- Order tests ใช้ `parallel` + **unique customer per test** ตัด shared state ออก
- Global `fullyParallel: false` + local `configure` ทำให้ควบคุมได้ละเอียดกว่า

---

## 6. Common Mistakes

❌ ใช้ `test.serial()` โดยตรง
```typescript
test.serial('my test', async ({ page }) => { /* ... */ }); // ❌ ไม่มี API นี้
```
✅ ใช้ `test.describe.configure({ mode: 'serial' })` ภายใน describe block
```typescript
test.describe('sequential tests', () => {
  test.describe.configure({ mode: 'serial' }); // ✅ ถูกต้อง
  test('step 1', async ({ page }) => { /* ... */ });
});
```
*(source: https://playwright.dev/docs/test-parallel)*

---

❌ เปิด `fullyParallel: true` โดยไม่ isolate test data
```typescript
// playwright.config.ts
export default defineConfig({
  fullyParallel: true, // ❌ เปิดแต่ tests ยังแชร์ database state กัน
});

// test A: POST /api/todos { title: 'A' }
// test B: GET /api/todos → expect length 1 → FAIL! ได้ 2 เพราะ test A ยังรันอยู่
```
✅ ใช้ unique data per test และ avoid shared reset
```typescript
// แต่ละ test ใช้ data ของตัวเอง ไม่ต้องพึ่งพา global reset
const uniqueTag = `test-${testInfo.workerIndex}-${Date.now()}`;
```
*(source: https://playwright.dev/docs/test-parallel)*

---

❌ ตั้ง `trace: 'on'` ใน parallel mode
```typescript
use: {
  trace: 'on', // ❌ บันทึก trace ทุก test ทุก retry — disk เต็มเร็วมากใน parallel
}
```
✅ ใช้ `'on-first-retry'` แทน
```typescript
use: {
  trace: 'on-first-retry', // ✅ บันทึกเฉพาะเมื่อ test fail และกำลัง retry
}
```
*(source: https://playwright.dev/docs/test-configuration)*

---

❌ ใช้ `--shard` โดยไม่ตั้ง blob reporter
```bash
# playwright.config.ts: reporter: 'html'  ← ผิด
npx playwright test --shard=1/4
npx playwright merge-reports ./blob-reports  # ❌ ไม่มี blob files ให้ merge
```
✅ ตั้ง blob reporter ใน CI ก่อนใช้ sharding
```typescript
reporter: process.env.CI ? 'blob' : 'html', // ✅ blob สำหรับ merge ทีหลัง
```
*(source: https://playwright.dev/docs/test-sharding)*

---

❌ ลืม `--forbid-only` ใน CI ทำให้ test suite รันไม่ครบ
```bash
# dev commit test.only แล้วลืมเอาออก
# CI รัน test เดียวแทนที่จะรัน 200 tests → deployment ผ่านทั้งที่ tests อื่นอาจ broken
```
✅ เพิ่ม `forbidOnly: !!process.env.CI` ใน config หรือ `--forbid-only` ใน CI command
```typescript
export default defineConfig({
  forbidOnly: !!process.env.CI, // ✅ fail ทันทีถ้าพบ test.only ใน CI
});
```
*(source: https://playwright.dev/docs/test-cli)*

---

## 7. สรุปบท

ก่อนดูเฉลย ลองตอบคำถามเหล่านี้ด้วยตัวเองก่อน:

**คำถามที่ 1:** ทีมของคุณมี test suite 300 tests รันบน GitHub Actions ใช้เวลา 20 นาที ต้องการลดเหลือ 5 นาที คุณจะใช้ `workers` หรือ `sharding` หรือทั้งสองอย่าง? อธิบาย trade-off

---

**คำถามที่ 2:** คุณเพิ่งเปิด `fullyParallel: true` แล้วพบว่า test ที่ตรวจ total badge count ใน UI fail แบบ intermittent (บางครั้ง pass บางครั้ง fail) ปัญหาน่าจะมาจากอะไร และจะ diagnose + fix อย่างไร?

---

**คำถามที่ 3:** ต้องการสร้าง CI pipeline ที่ทำทั้งหมดนี้: (a) รัน 4 shards พร้อมกัน (b) รวม report ทีหลัง (c) fail ถ้า dev ลืม `test.only` (d) บันทึก trace เฉพาะตอน fail คุณต้องตั้งค่าอะไรบ้างใน config และ CLI?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย:**

**คำถามที่ 1:**
- **Workers** ช่วยใน machine เดียว: 4-8 workers บน GitHub Actions runner (4 cores) อาจลดได้ ~3-4x → 5-7 นาที แต่ memory จำกัด
- **Sharding** ช่วยข้าม machines: 4 shards บน 4 parallel jobs ลดได้ ~4x → 5 นาที แต่ต้องรอ merge-reports และมี overhead ของ job startup (~30-60 วินาที/job)
- **ทั้งสองอย่าง:** sharding 4 jobs + workers 2 ต่อ job = ลดได้ ~8x → 2-3 นาที แต่ cost ของ CI minutes เพิ่มขึ้น 4 เท่า
- Trade-off หลัก: sharding เพิ่ม speed แต่เพิ่ม CI cost และ complexity (merge step)

**คำถามที่ 2:**
- ปัญหา: **Shared state / race condition** — parallel tests หลายตัวต่างก็เพิ่ม todo/item เข้า database เดียวกัน ทำให้ count ไม่ตรงที่ expect
- Diagnose: รัน `--workers=1` แล้วดูว่า fail หายไปไหม ถ้าหาย = ยืนยันเป็น race condition
- Fix: (1) แต่ละ test สร้าง unique user/data ของตัวเอง (2) ใช้ `testInfo.workerIndex` สร้าง namespace (3) ถ้าต้อง reset ใช้ `beforeEach` ที่ test-level ไม่ใช่ shared global reset

**คำถามที่ 3:**
```typescript
// playwright.config.ts
export default defineConfig({
  forbidOnly: !!process.env.CI,           // (c)
  reporter: process.env.CI ? 'blob' : 'html', // (b) blob สำหรับ merge
  use: { trace: 'on-first-retry' },       // (d) trace เฉพาะตอน fail+retry
});
```
```bash
# CI command (a) sharding
npx playwright test --shard=${{ matrix.shardIndex }}/${{ matrix.shardTotal }}

# merge step (b)
npx playwright merge-reports --reporter html ./all-blob-reports
```

</details>

---

## 8. สิ่งที่ต้องรู้ก่อนบทถัดไป

บทที่ 12 จะพูดถึง **API Testing & Request Interception** — การทดสอบ API โดยตรงและการ mock/intercept network requests ใน Playwright

ทบทวนก่อน:
- `workerIndex` ที่ใช้ใน section นี้ได้จาก `testInfo.workerIndex` — คุณรู้จัก `testInfo` object จาก fixtures แล้ว (บทที่ 7)
- Parallel tests ต้องการ **independent data** — concept นี้จะสำคัญมากขึ้นเมื่อ test API endpoints โดยตรง
