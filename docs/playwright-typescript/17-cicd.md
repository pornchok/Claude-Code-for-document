## ก่อนอ่านบทนี้ ลองตอบ:

1. ทำไม `toHaveScreenshot()` ถึงต้องสร้าง baseline บน environment เดียวกับ CI และถ้าสร้าง baseline บน macOS แต่ CI รันบน Linux จะเกิดอะไรขึ้น?

2. `toMatchAriaSnapshot()` และ `toHaveScreenshot()` ต่างกันอย่างไรในแง่ platform dependency — อันไหนรัน CI ได้โดยไม่ต้องกังวลเรื่อง OS และเพราะอะไร?

---

เฉลย:

1. `toHaveScreenshot()` เปรียบเทียบ pixel-by-pixel — font rendering, anti-aliasing, และ sub-pixel rendering ต่างกันระหว่าง macOS/Linux/Windows ทำให้ได้ไฟล์ `-darwin.png` บน macOS แต่ CI สร้าง `-linux.png` เมื่อนำมาเปรียบเทียบกันจะ fail ทันทีแม้ UI จะดูเหมือนกันด้วยตาเปล่า — วิธีแก้คือสร้าง baseline ใน Docker environment เดียวกับ CI หรือใช้ official image `mcr.microsoft.com/playwright:v1.50.0-jammy`

2. `toMatchAriaSnapshot()` ตรวจ accessibility tree structure (roles, names, states) ซึ่งไม่ขึ้นกับ OS เลย — ทำงานเหมือนกันบน macOS/Linux/Windows เพราะตรวจ semantic structure ไม่ใช่ visual pixels ส่วน `toHaveScreenshot()` ขึ้นกับ OS เพราะ screenshot เป็น pixel-level comparison

---

# บทที่ 17: CI/CD Integration

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- เขียน GitHub Actions workflow สำหรับ Playwright ได้ครบถ้วน — checkout, install, run, upload artifact
- เข้าใจว่า `CI=true` เปลี่ยน behavior อะไรบ้างใน `playwright.config.ts` (retries, forbidOnly, workers)
- ใช้ `--reporter=github` เพื่อให้ failed test ขึ้น annotation ตรงใน GitHub Pull Request
- รัน Playwright ใน Docker container ด้วย official image ของ Microsoft
- แบ่ง test suite ออกเป็น shards ด้วย matrix strategy เพื่อรัน parallel ใน CI
- merge blob reports จาก shards ทั้งหมดเป็น HTML report เดียว
- เปรียบเทียบ CI setup ระหว่าง Playwright กับ Robot Framework + Selenium

---

## 2. ทำไมต้องรู้? (Why)

สถานการณ์นี้เกิดบ่อย: developer merge code วันศุกร์บ่าย, test ผ่านบน local machine, แต่เช้าวันจันทร์ลูกค้าโทรมาบอกว่าปุ่ม checkout ไม่ทำงาน

ปัญหาคือ "ผ่านบน local แต่ fail บน production" เกิดจาก:
- environment ต่างกัน (OS, Node version, library version)
- developer ลืมรัน test ก่อน push
- ไม่มีกลไก prevent merge เมื่อ test fail

**CI/CD แก้ปัญหานี้**: ทุกครั้งที่มี push หรือ pull request, pipeline รัน test อัตโนมัติบน clean environment — ถ้า fail, merge ไม่ได้

สำหรับ Playwright โดยเฉพาะ CI มีความสำคัญเพิ่มเติม:
- **Artifact**: Playwright generate trace, screenshot, video เมื่อ test fail — ดู root cause ได้โดยไม่ต้อง reproduce local
- **Cross-browser**: รัน Chromium + Firefox + WebKit พร้อมกันได้ใน CI โดยไม่ต้องให้ developer ติดตั้ง browser ทุก browser
- **Sharding**: test 1,000 cases รันใน 15 นาทีแทน 1 ชั่วโมง โดยแบ่งออกเป็น shards รันพร้อมกัน

ถ้าเคยใช้ Robot Framework + Selenium มาก่อน — setup CI ยากกว่ามาก: ต้องติดตั้ง ChromeDriver ให้ตรง version, จัดการ display server บน Linux, ไม่มี official Docker image พร้อมใช้ Playwright แก้ปัญหาเหล่านี้ด้วยคำสั่งเดียว

---

## 3. เนื้อหาหลัก

### 3.1 GitHub Actions — Complete Workflow

GitHub Actions คือ CI/CD platform ที่ built-in อยู่ใน GitHub ไม่ต้องติดตั้ง service เพิ่ม สร้าง file ที่ `.github/workflows/playwright.yml` แล้ว GitHub จะรัน workflow อัตโนมัติ

นี่คือ complete workflow สำหรับ course project ที่มี demo app แยกต่างหาก:

```yaml
# .github/workflows/playwright.yml
# tested: GitHub Actions, ubuntu-latest, Node.js LTS

name: Playwright Tests
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    timeout-minutes: 60
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: lts/*

      - name: Install demo app dependencies
        run: cd docs/playwright-typescript/playwright-course-app && npm ci

      - name: Start demo app
        run: cd docs/playwright-typescript/playwright-course-app && npm start &

      - name: Install Playwright dependencies
        run: npm ci

      - name: Install Playwright browsers
        run: npx playwright install --with-deps

      - name: TypeScript check
        run: npx tsc --noEmit

      - name: Run Playwright tests
        run: npx playwright test
        env:
          CI: true

      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: playwright-report
          path: playwright-report/
          retention-days: 30
```

**ทำไมแต่ละ step สำคัญ:**

- `timeout-minutes: 60` — ป้องกัน job ค้างตลอดไปถ้า test หยุดตอบสนอง (stuck)
- `actions/checkout@v4` — ดึง code มาใน runner
- `node-version: lts/*` — ใช้ Node.js LTS version ล่าสุดเสมอ ไม่ hardcode version
- `npm ci` — เหมือน `npm install` แต่ strict: ใช้ `package-lock.json` ทุกครั้ง, ไม่แก้ lockfile — reproducible build
- `--with-deps` — ติดตั้ง browser binary + system dependencies (libnss, libgbm ฯลฯ) บน Linux ขาดสิ่งนี้แล้ว browser จะ launch ไม่ได้
- `if: always()` — upload artifact แม้ test จะ fail — ถ้าไม่มีบรรทัดนี้ artifact จะไม่ถูก upload เมื่อ job fail ทำให้ไม่มี trace ดู

### 3.2 ผลของ `CI=true` ใน playwright.config.ts

`CI=true` คือ environment variable ธรรมดา — มีผลก็ต่อเมื่อ config ของคุณอ่านค่านี้:

```typescript
// playwright.config.ts
// tested: Playwright v1.50+

import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  // retry 2 ครั้งใน CI, ไม่ retry บน local
  retries: process.env.CI ? 2 : 0,

  // fail ทันทีถ้ามี test.only() ลืม commit ขึ้น CI
  // ป้องกัน developer ลืม remove test.only() ทำให้ test อื่นไม่รัน
  forbidOnly: !!process.env.CI,

  // ใช้ 1 worker ใน CI เพื่อ stability, ใช้ทุก core บน local
  workers: process.env.CI ? 1 : undefined,

  reporter: [
    // HTML report เสมอ
    ['html'],
    // GitHub PR annotations เมื่ออยู่ใน CI
    ...(process.env.CI ? [['github'] as const] : []),
  ],

  use: {
    // เก็บ trace เมื่อ retry ครั้งแรก (มีประสิทธิภาพ: ไม่เก็บถ้าผ่านทุกครั้ง)
    trace: 'on-first-retry',
  },
});
```

**`forbidOnly`** — นี่สำคัญมาก: ถ้า developer เขียน `test.only('ทดสอบ login')` แล้วลืม remove ก่อน push, Playwright จะรันแค่ test นั้น test อื่นทั้งหมดถูก skip โดยไม่มี error — `forbidOnly: true` ทำให้ CI fail ทันทีเมื่อพบ `.only`

**`workers: 1`** — ใน CI environment บางครั้ง shared resource มีจำกัด การรัน parallel หลาย workers อาจทำให้ test แย่งกันใช้ port, database, หรือ memory ทำให้ flaky ใช้ 1 worker เพื่อ stability แม้จะช้ากว่า (ถ้าต้องการเร็วกว่านี้ ใช้ sharding แทน)

### 3.3 GitHub Reporter (`--reporter=github`)

เมื่อ test fail ใน GitHub Actions, reporter นี้ทำให้ failed test ขึ้นเป็น annotation ตรงใน Pull Request interface:

```
❌ tests/checkout.spec.ts:45:5 › Checkout › should process payment
   Expected: "Order confirmed"
   Received: "Payment failed"
```

Developer เห็น annotation นี้ใน "Files changed" tab โดยตรง ไม่ต้องเปิด CI log แยก ใช้งานผ่าน `playwright.config.ts` (ดู section 4.2 ด้านบน) หรือ flag `--reporter=github` ตอน run:

```bash
npx playwright test --reporter=github
```

### 3.4 Docker — Official Playwright Image

Microsoft publish official Docker image ที่มี browser binaries และ system dependencies ครบพร้อมใช้:

```bash
# Pull image ล่าสุด
# tested: Docker, Playwright v1.50+
docker pull mcr.microsoft.com/playwright:v1.50.0-jammy
```

**Image naming convention:**
- `mcr.microsoft.com/playwright:v1.50.0-noble` — Ubuntu 24.04 LTS (Noble Numbat) — แนะนำสำหรับ project ใหม่
- `mcr.microsoft.com/playwright:v1.50.0-jammy` — Ubuntu 22.04 LTS (Jammy Jellyfish) — compatibility สูงกว่า

**หมายเหตุสำคัญ**: image ไม่มี Playwright package มาให้ — มีแค่ browser binaries และ system dependencies ต้องติดตั้ง Playwright เองผ่าน `npm ci`

**Dockerfile สำหรับ course project:**

```dockerfile
# tested: Docker, Playwright v1.50+
FROM mcr.microsoft.com/playwright:v1.50.0-jammy
# image นี้มี browser binaries อยู่แล้วที่ /ms-playwright
# ไม่ต้องรัน npx playwright install อีกครั้ง

WORKDIR /app

# Install Node dependencies ก่อน (cache layer)
COPY package*.json ./
RUN npm ci
# ไม่ต้องรัน npx playwright install -- browser อยู่ใน image แล้ว

# Copy source
COPY . .

# Run tests
CMD ["npx", "playwright", "test"]
```

**หมายเหตุสำคัญเกี่ยวกับ browser path:**
- Official Playwright image `mcr.microsoft.com/playwright:vX.X.X-jammy` มี browser binaries ติดตั้งไว้แล้วที่ `/ms-playwright`
- ไม่ต้องรัน `npx playwright install` ใน Dockerfile เพราะ browser มีอยู่แล้ว
- ถ้าคุณใช้ `node:20` image ปกติ แทนที่จะใช้ official Playwright image ต้องรัน `npx playwright install --with-deps` เพื่อติดตั้ง browser binary + system dependencies ด้วย

**รันด้วย Docker locally:**

```bash
docker build -t playwright-tests .
docker run --rm --ipc=host playwright-tests
```

`--ipc=host` สำคัญสำหรับ Chromium — Chrome ใช้ shared memory สำหรับ renderer processes และต้องการ `/dev/shm` ที่มีขนาดเพียงพอ

**ประโยชน์หลักของ Docker สำหรับ visual testing**: สร้าง baseline screenshot บน Docker environment เดียวกับ CI ทำให้ `toHaveScreenshot()` ไม่ fail เพราะ OS ต่างกัน

### 3.5 Sharding — รัน Tests Parallel ใน CI

Sharding คือการแบ่ง test suite ออกเป็นส่วนย่อย (shards) แล้วรัน parallel บน multiple runners พร้อมกัน:

```bash
# แบ่งเป็น 4 shards — รัน 4 คำสั่งนี้พร้อมกัน
npx playwright test --shard=1/4
npx playwright test --shard=2/4
npx playwright test --shard=3/4
npx playwright test --shard=4/4
```

**GitHub Actions Matrix Strategy:**

```yaml
# .github/workflows/playwright-sharded.yml
# tested: GitHub Actions, ubuntu-latest

name: Playwright Tests (Sharded)
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  playwright-tests:
    timeout-minutes: 60
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false          # สำคัญ: ให้ shard อื่นรันต่อแม้ shard นึงจะ fail
      matrix:
        shardIndex: [1, 2, 3, 4]
        shardTotal: [4]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: lts/*
      - name: Install demo app dependencies
        run: cd docs/playwright-typescript/playwright-course-app && npm ci
      - name: Start demo app
        run: cd docs/playwright-typescript/playwright-course-app && npm start &
      - name: Install dependencies
        run: npm ci
      - name: Install Playwright browsers
        run: npx playwright install --with-deps
      - name: Run Playwright tests
        run: npx playwright test --shard=${{ matrix.shardIndex }}/${{ matrix.shardTotal }}
        env:
          CI: true
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: blob-report-${{ matrix.shardIndex }}
          path: blob-report
          retention-days: 1

  merge-reports:
    if: always()
    needs: [playwright-tests]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: lts/*
      - name: Install dependencies
        run: npm ci
      - name: Download blob reports from GitHub Actions Artifacts
        uses: actions/download-artifact@v4
        with:
          path: all-blob-reports
          pattern: blob-report-*
          merge-multiple: true
      - name: Merge into HTML Report
        run: npx playwright merge-reports --reporter html ./all-blob-reports
      - name: Upload HTML report
        uses: actions/upload-artifact@v4
        with:
          name: html-report--attempt-${{ github.run_attempt }}
          path: playwright-report
          retention-days: 14
```

**ทำไม blob reporter?** แต่ละ shard generate `blob-report/` ที่เก็บ raw test data — ไม่ใช่ HTML report ที่ human readable blob report ออกแบบมาเพื่อ merge กันได้ง่าย จากนั้น `merge-reports` job รวม blob ทั้งหมดเป็น HTML report เดียว

**ต้องเพิ่มใน `playwright.config.ts`:**

```typescript
// playwright.config.ts
reporter: process.env.CI ? 'blob' : 'html',
```

**ประสิทธิภาพ**: test suite 200 cases ที่รัน sequential ใช้เวลา ~20 นาที — แบ่งเป็น 4 shards รัน ~5 นาที

### 3.6 `--only-changed` — รัน Selective Tests

Flag นี้วิเคราะห์ dependency graph ของ test files เพื่อรันเฉพาะ tests ที่อาจได้รับผลกระทบจาก code ที่เปลี่ยน:

```bash
# รัน test ที่เกี่ยวข้องกับ file ที่เปลี่ยนใน PR นี้เท่านั้น
npx playwright test --only-changed=main
```

เหมาะสำหรับ early-feedback ใน CI — รัน quick pass ก่อน แล้วค่อยรัน full suite:

```yaml
- uses: actions/checkout@v4
  with:
    fetch-depth: 0  # ต้องการ full git history สำหรับ --only-changed

- name: Run changed tests (fast feedback)
  run: npx playwright test --only-changed=origin/main
  env:
    CI: true
```

**เหตุที่ต้อง `fetch-depth: 0`:** `--only-changed` ต้องการ git history เพื่อเปรียบเทียบ diff ระหว่าง branch ปัจจุบันกับ base branch (เช่น `origin/main`) — ถ้า `fetch-depth` เป็น default (1) จะมีแค่ latest commit และ git history ไม่พอสำหรับ comparison ทำให้ flag อาจทำงานผิดพลาดหรือ fallback ไปรัน tests ทั้งหมด (ซึ่งหมายความว่า early-feedback ไม่ได้เร็วขึ้น)

### 3.7 เปรียบเทียบ: Robot Framework + Selenium vs Playwright

| | Robot Framework + Selenium | Playwright |
|---|---|---|
| **Browser install** | ติดตั้ง ChromeDriver ให้ตรง version browser | `npx playwright install --with-deps` คำสั่งเดียว |
| **Docker image** | ต้อง custom image เอง หรือใช้ `selenium/standalone-chrome` | official `mcr.microsoft.com/playwright:vX.X.X-jammy` |
| **GitHub reporter** | ไม่มี built-in, ต้อง parse XML ด้วย plugin | `--reporter=github` built-in |
| **Sharding** | ไม่มี built-in, ต้องเขียน custom script | `--shard=N/M` + `merge-reports` built-in |
| **Trace on CI** | ไม่มี built-in | `trace: 'on-first-retry'` built-in |
| **Artifact** | JUnit XML เป็นหลัก | HTML report + trace + screenshot + video |
| **`forbidOnly`** | ไม่มี concept นี้ | ป้องกัน `.only` ลืม commit ขึ้น CI |

Playwright ออกแบบมาสำหรับ CI ตั้งแต่แรก — ทุก feature ที่ need-to-have สำหรับ CI pipeline มีอยู่ built-in ไม่ต้องประกอบเอง

---

## 4. ตัวอย่าง 3 ระดับ

### Beginner — GitHub Actions สำหรับ Course Project

สถานการณ์: สร้าง CI pipeline สำหรับ playwright-typescript course project ที่มี demo app (Express server) และ test suite

```yaml
# .github/workflows/playwright.yml
# tested: GitHub Actions, ubuntu-latest, Node.js LTS

name: Playwright Tests — Course Project
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    timeout-minutes: 60
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: lts/*

      # ── Start demo app ──────────────────────────────────────
      - name: Install demo app dependencies
        run: cd docs/playwright-typescript/playwright-course-app && npm ci

      - name: Start demo app
        run: cd docs/playwright-typescript/playwright-course-app && npm start &

      - name: Wait for demo app to be ready
        run: npx wait-on http://localhost:3000 --timeout 30000

      # ── Install & Run Playwright ─────────────────────────────
      - name: Install Playwright dependencies
        run: npm ci

      - name: Install Playwright browsers
        run: npx playwright install --with-deps

      - name: TypeScript check
        run: npx tsc --noEmit

      - name: Run Playwright tests
        run: npx playwright test
        env:
          CI: true

      # ── Upload Results ───────────────────────────────────────
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: playwright-report
          path: playwright-report/
          retention-days: 30
```

```typescript
// playwright.config.ts — สำหรับใช้กับ workflow ด้านบน
// tested: Playwright v1.50+

import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  retries: process.env.CI ? 2 : 0,
  forbidOnly: !!process.env.CI,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html'],
    ...(process.env.CI ? [['github'] as const] : []),
  ],
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
```

**Expected output:** เมื่อ push ไป GitHub → Actions tab แสดง workflow กำลังรัน → ถ้า test fail ขึ้น annotation ใน PR → download `playwright-report` artifact เพื่อดู trace

---

### Intermediate — Visual Regression ใน CI Pipeline ที่มี Sharding

สถานการณ์: ทีมมี visual regression tests จาก Ch16 และต้องการรันใน CI พร้อม sharding — visual tests ต้องรันบน Linux เพื่อให้ baseline match, shards ต้องรัน parallel แต่ merge report ให้รวมกัน

```yaml
# .github/workflows/playwright-visual-sharded.yml
# tested: GitHub Actions, ubuntu-latest

name: Playwright Visual Tests (4 Shards)
on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  playwright-tests:
    timeout-minutes: 30
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        shardIndex: [1, 2, 3, 4]
        shardTotal: [4]
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: lts/*

      - name: Install demo app dependencies
        run: cd docs/playwright-typescript/playwright-course-app && npm ci

      - name: Start demo app
        run: cd docs/playwright-typescript/playwright-course-app && npm start &

      - name: Install Playwright dependencies
        run: npm ci

      - name: Install Playwright browsers
        run: npx playwright install --with-deps

      - name: Run Playwright tests (shard ${{ matrix.shardIndex }}/${{ matrix.shardTotal }})
        run: npx playwright test --shard=${{ matrix.shardIndex }}/${{ matrix.shardTotal }}
        env:
          CI: true

      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: blob-report-${{ matrix.shardIndex }}
          path: blob-report
          retention-days: 1

  merge-and-report:
    if: always()
    needs: [playwright-tests]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: lts/*

      - name: Install dependencies
        run: npm ci

      - name: Download all blob reports
        uses: actions/download-artifact@v4
        with:
          path: all-blob-reports
          pattern: blob-report-*
          merge-multiple: true

      - name: Merge reports into HTML
        run: npx playwright merge-reports --reporter html ./all-blob-reports

      - name: Upload merged HTML report
        uses: actions/upload-artifact@v4
        with:
          name: html-report--attempt-${{ github.run_attempt }}
          path: playwright-report
          retention-days: 14
```

```typescript
// playwright.config.ts — เพิ่ม blob reporter สำหรับ sharding
// tested: Playwright v1.50+

import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  retries: process.env.CI ? 2 : 0,
  forbidOnly: !!process.env.CI,
  workers: process.env.CI ? 1 : undefined,

  // blob reporter สำหรับ shard merge, html สำหรับ local
  reporter: process.env.CI ? 'blob' : 'html',

  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
});
```

**Key consideration**: visual tests สร้าง baseline บน Linux (ubuntu-latest runner) โดยอัตโนมัติ — developer ที่ใช้ macOS ต้องรัน test ผ่าน Docker เพื่อ update baseline ไม่ใช่ machine โดยตรง

---

### Advanced — Debug Pipeline ที่ Fail ด้วย Trace + Matrix Sharding

สถานการณ์: CI pipeline fail ด้วย intermittent error "Element not found" ที่ shard 3 แต่ไม่ fail บน local — ต้องใช้ trace artifact และ matrix sharding เพื่อ isolate และ diagnose

**ขั้นตอนที่ 1**: ตั้งค่า config ให้เก็บ diagnostic data ครบ

```typescript
// playwright.config.ts — diagnostic config สำหรับ debug CI failures
// tested: Playwright v1.50+

import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  retries: process.env.CI ? 3 : 0,   // retry 3 ครั้งเพื่อ capture flaky pattern
  forbidOnly: !!process.env.CI,
  workers: process.env.CI ? 1 : undefined,

  reporter: process.env.CI ? [
    ['blob'],
    ['github'],
    // เพิ่ม json report เพื่อ programmatic analysis
    ['json', { outputFile: 'test-results/results.json' }],
  ] : [['html']],

  use: {
    baseURL: 'http://localhost:3000',

    // เก็บ trace ทุก retry — ดู state ที่แต่ละ retry
    trace: 'on-first-retry',

    // เก็บ screenshot เมื่อ fail
    screenshot: 'only-on-failure',

    // เก็บ video เมื่อ fail — ดู sequence ของ actions
    video: 'retain-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
});
```

**ขั้นตอนที่ 2**: workflow ที่ upload diagnostic artifacts แยกตาม shard

```yaml
# .github/workflows/playwright-debug.yml
# tested: GitHub Actions, ubuntu-latest

name: Playwright Debug Pipeline
on:
  workflow_dispatch:    # รันได้ manual เพื่อ debug
    inputs:
      shard_count:
        description: 'Number of shards'
        default: '4'

jobs:
  playwright-tests:
    timeout-minutes: 60
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        shardIndex: [1, 2, 3, 4]
        shardTotal: [4]
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: lts/*

      - name: Install demo app dependencies
        run: cd docs/playwright-typescript/playwright-course-app && npm ci

      - name: Start demo app
        run: cd docs/playwright-typescript/playwright-course-app && npm start &

      - name: Install dependencies
        run: npm ci

      - name: Install Playwright browsers
        run: npx playwright install --with-deps

      - name: Run tests — shard ${{ matrix.shardIndex }}/${{ matrix.shardTotal }}
        run: npx playwright test --shard=${{ matrix.shardIndex }}/${{ matrix.shardTotal }}
        env:
          CI: true
        # ต่อให้ fail ก็ไม่หยุด — ต้องการ artifact จากทุก shard

      # Upload blob report สำหรับ merge
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: blob-report-${{ matrix.shardIndex }}
          path: blob-report
          retention-days: 3

      # Upload test-results (trace, screenshot, video) แยกตาม shard
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results-shard-${{ matrix.shardIndex }}
          path: test-results/
          retention-days: 3

  merge-and-analyze:
    if: always()
    needs: [playwright-tests]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: lts/*
      - name: Install dependencies
        run: npm ci

      - name: Download blob reports
        uses: actions/download-artifact@v4
        with:
          path: all-blob-reports
          pattern: blob-report-*
          merge-multiple: true

      - name: Merge into HTML Report
        run: npx playwright merge-reports --reporter html ./all-blob-reports

      - name: Upload merged HTML report
        uses: actions/upload-artifact@v4
        with:
          name: html-report--attempt-${{ github.run_attempt }}
          path: playwright-report
          retention-days: 14
```

**ขั้นตอนที่ 3**: วิธี debug เมื่อ shard 3 fail

```bash
# 1. Download artifacts จาก GitHub Actions
#    ไปที่ Actions → เลือก failed run → download "test-results-shard-3"

# 2. ดู trace บน local
npx playwright show-trace test-results/checkout-spec-ts-Checkout-should-process-payment/trace.zip

# 3. Trace viewer แสดง:
#    - Timeline ของ actions ทั้งหมด
#    - Screenshot ก่อน/หลังแต่ละ action
#    - Network requests ที่เกิดขึ้น
#    - Console errors ที่เกิดขึ้น

# 4. ดู HTML report ที่ merged
npx playwright show-report
```

**การวิเคราะห์ root cause จาก trace:**
- ถ้า trace แสดงว่า element ปรากฏหลังจาก timeout — เป็น timing issue: เพิ่ม `waitFor` หรือ increase timeout
- ถ้า network request fail — อาจเป็น race condition กับ demo app startup: เพิ่ม `wait-on` step
- ถ้า fail เฉพาะ shard 3 แต่ไม่ใช่ shard อื่น — อาจเป็น test order dependency: ตรวจ `beforeEach`/`afterEach` cleanup

---

## 5. Common Mistakes

❌ **ลืม `--with-deps` ใน `npx playwright install`**

```bash
# ผิด — install browser binary แต่ไม่ install system dependencies
- name: Install Playwright browsers
  run: npx playwright install
# Error: "error while loading shared libraries: libnss3.so"
# หรือ "Host system is missing dependencies to run browsers"
```

```bash
# ✅ ถูก — ติดตั้ง browser + system dependencies ครบ
- name: Install Playwright browsers
  run: npx playwright install --with-deps
```

บน Linux CI runner (ubuntu-latest) ไม่มี system libraries ที่ browser ต้องการ เช่น `libnss3`, `libgbm`, `libatk` — `--with-deps` ติดตั้งให้ครบ *(source: https://playwright.dev/docs/ci)*

---

❌ **`on: push` โดยไม่กำหนด branches → รัน CI ทุก branch**

```yaml
# ผิด — รันทุกครั้งที่มี push ไม่ว่า branch ไหน
on:
  push:
# ผลคือ feature branch ทุก branch รัน CI ซึ่งสิ้นเปลือง minutes
```

```yaml
# ✅ ถูก — รันเฉพาะ branch สำคัญ + ทุก PR ที่ target main
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]
```

*(source: https://playwright.dev/docs/ci)*

---

❌ **ไม่ใส่ `if: always()` บน upload artifact → ไม่มี trace เมื่อ test fail**

```yaml
# ผิด — upload เฉพาะเมื่อ job ผ่าน (default behavior)
- uses: actions/upload-artifact@v4
  with:
    name: playwright-report
    path: playwright-report/
# ถ้า test fail → job fail → artifact ไม่ถูก upload → ไม่มี trace ดู
```

```yaml
# ✅ ถูก — upload เสมอ ไม่ว่า test จะผ่านหรือไม่
- uses: actions/upload-artifact@v4
  if: always()
  with:
    name: playwright-report
    path: playwright-report/
    retention-days: 30
```

`if: always()` เป็น condition ที่บอก GitHub Actions ว่า "รัน step นี้แม้ step ก่อนหน้าจะ fail" — ขาดบรรทัดนี้แล้ว debug CI failure ไม่ได้เพราะไม่มี artifact *(source: https://playwright.dev/docs/ci-intro)*

---

❌ **`fail-fast: true` (default) ใน matrix sharding → shard อื่น cancel ก่อน merge report**

```yaml
# ผิด — fail-fast: true คือ default, ไม่ต้องระบุก็ active
strategy:
  matrix:
    shardIndex: [1, 2, 3, 4]
    shardTotal: [4]
# ถ้า shard 1 fail → GitHub Actions cancel shard 2, 3, 4 ทันที
# merge-reports job จะได้ blob จาก shard 1 เท่านั้น
# HTML report ไม่สมบูรณ์ — ไม่รู้ว่า shard อื่นผ่านหรือไม่
```

```yaml
# ✅ ถูก — ต้อง explicit set fail-fast: false เสมอเมื่อใช้ sharding
strategy:
  fail-fast: false
  matrix:
    shardIndex: [1, 2, 3, 4]
    shardTotal: [4]
```

"With `fail-fast: false`: remaining shards complete even if one fails" — ทำให้ `merge-reports` ได้ข้อมูลครบทุก shard และ HTML report แสดง failed tests จากทุก shard *(source: https://playwright.dev/docs/test-sharding)*

---

❌ **ใช้ Docker image version ไม่ตรงกับ Playwright version ใน package.json**

```dockerfile
# ผิด — image ใช้ v1.40 แต่ package.json ใช้ @playwright/test@1.50
FROM mcr.microsoft.com/playwright:v1.40.0-jammy
WORKDIR /app
COPY package*.json ./
RUN npm ci   # ติดตั้ง playwright 1.50 แต่ browser ใน image เป็น v1.40
```

```dockerfile
# ✅ ถูก — version ตรงกันเสมอ
FROM mcr.microsoft.com/playwright:v1.50.0-jammy
WORKDIR /app
COPY package*.json ./
RUN npm ci   # playwright version ใน package.json ตรงกับ image
```

Playwright browser version ต้องตรงกับ package version เสมอ — ไม่ตรงอาจเกิด browser API mismatch *(source: https://playwright.dev/docs/docker)*

---

## 6. สรุปบท

ก่อนดูเฉลย ลองตอบ 3 คำถามนี้ด้วยตัวเองก่อน:

**คำถาม 1**: Developer เขียน `test.only('login')` แล้ว push ขึ้น CI โดยไม่ได้ตั้งใจ — โดยไม่มี `forbidOnly` ผลคืออะไร? และ `forbidOnly: !!process.env.CI` แก้ปัญหานี้ได้อย่างไร?

**คำถาม 2**: ทีมมี test suite 400 cases ที่รัน sequential ใช้เวลา 40 นาที — คุณจะใช้ sharding อย่างไรให้ลดเวลาเหลือ ~10 นาที และทำไมต้องตั้ง `fail-fast: false` + ใช้ blob reporter?

**คำถาม 3**: CI fail ด้วย error "Element 'Add to Cart' not found" ที่ shard 2 เท่านั้น แต่ test รัน local ผ่านเสมอ — คุณจะใช้ Playwright artifact อะไรเพื่อ diagnose และขั้นตอนการ debug คืออะไร?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย:**

**คำถาม 1**: โดยไม่มี `forbidOnly` — `test.only()` ทำให้ Playwright รันแค่ test นั้น test อื่นทั้งหมดถูก skip แต่ **ไม่มี error** — CI ผ่านทั้งๆ ที่ test suite ส่วนใหญ่ไม่ได้รัน เป็น false positive ที่อันตราย `forbidOnly: !!process.env.CI` ทำให้ Playwright fail ทันทีเมื่อพบ `.only` ใน CI environment — developer จะเห็น error และต้องลบ `.only` ก่อน merge ได้

**คำถาม 2**: แบ่งเป็น 4 shards ด้วย `strategy.matrix.shardIndex: [1,2,3,4]` และ `npx playwright test --shard=${{ matrix.shardIndex }}/4` — 4 shards รัน parallel แต่ละ shard ใช้เวลา ~10 นาที ต้อง `fail-fast: false` เพราะถ้า shard 1 fail แล้ว GitHub cancel shards อื่น, `merge-reports` job จะได้ blob report ไม่ครบทำให้ HTML report ไม่สมบูรณ์ และต้อง blob reporter เพราะ blob format ออกแบบมาเพื่อ merge ได้ต่างจาก HTML ที่เป็น standalone report

**คำถาม 3**: Download artifact `test-results-shard-2` จาก GitHub Actions → รัน `npx playwright show-trace test-results/[test-name]/trace.zip` → trace viewer แสดง timeline ของ actions ทั้งหมด screenshot ก่อน/หลัง action และ network requests — ถ้า trace แสดงว่า element ยังไม่ปรากฏตอน action เกิด อาจเป็น race condition กับ demo app startup (เพิ่ม `wait-on` step) หรือ timing issue (เพิ่ม `waitFor` assertion) ถ้า fail เฉพาะ shard 2 ให้ตรวจว่า tests ใน shard นั้น depend on state จาก test อื่นหรือไม่ (test isolation issue)

</details>
