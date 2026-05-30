# Playwright TypeScript Course — Content Writing Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** เขียนเอกสาร 18 บท + exercises.md + glossary.md สำหรับ Playwright TypeScript Master Course ครบตาม 8-section structure และ verify ทุก concept กับ playwright.dev

**Architecture:** แต่ละบทเป็น Markdown file อิสระ เขียนทีละบทตามลำดับ (บทหลังอาจ reference บทก่อน) source notes บันทึกใน `docs/playwright-typescript-source-notes.md` ไฟล์เดียว

**Tech Stack:** Markdown, Playwright v1.50+, Node.js 20+, TypeScript 5.x

**Prerequisites:** `playwright-course-app/` ต้อง build เสร็จและรัน `npm start` ได้ก่อนเริ่ม — บางบทต้อง test code กับ app จริง

---

## Workflow มาตรฐานทุกบท (ใช้ซ้ำทุก Task)

```
Step A: WebFetch playwright.dev URLs ที่ระบุใน task
Step B: บันทึก source notes (format ด้านล่าง)
Step C: เขียน chapter ตาม 8-section structure
Step D: Test code examples ด้วย Bash
Step E: Commit
```

**Source Notes Format (บันทึกใน `docs/playwright-typescript-source-notes.md`):**
```
## Ch[N]: [ชื่อบท]
SOURCE: https://playwright.dev/docs/[page]
VERSION: Playwright vX.XX.X (ติดตั้งจริง)
CONCEPT: [ชื่อ concept]
QUOTE: "[ข้อความจริงจาก docs — copy ตรง ไม่ paraphrase]"
```

**8-Section Structure (บังคับทุกบท):**
1. วัตถุประสงค์ — bullet list "อ่านจบแล้วทำอะไรได้"
2. ทำไมต้องรู้? (Why) — อธิบาย problem ก่อน solution
3. Analogy + breakdown points ("⚠️ ถ้าเชื่อ 100% จะเข้าใจผิดว่า: ...")
4. เนื้อหาหลัก (verified, ภาษาไทย, code/terms เป็น English)
5. ตัวอย่าง 3 ระดับ: Beginner / Intermediate (สถานการณ์ใหม่) / Advanced
6. Common Mistakes ❌→✅ + `*(source: URL)*` inline
7. สรุปบท + Retrieval Questions 2-3 ข้อ (ตอบก่อนดูเฉลย)
8. Pre-chapter Retrieval (บทที่ 2+ — "ก่อนอ่านบทนี้ ลองตอบ: ..." คั่นด้วย `---`)

**Code Example Standards:**
```typescript
// tested: Playwright vX.XX.X, Node.js 20.X.X
```
- รันได้จริงทุกตัว ไม่ใช่ partial snippet
- แสดง output จริง
- Test ด้วย Bash ก่อนใส่เอกสาร

**RF/Selenium Comparison (ทุกบท):**
```markdown
| | Robot Framework + Selenium | Playwright |
|-|--------------------------|------------|
| [concept] | [RF/Selenium approach] | [Playwright approach] |
```

---

## File Map

| File | Task |
|------|------|
| `docs/playwright-typescript-source-notes.md` | สร้างใน Task 0, เพิ่มต่อทุกบท |
| `docs/playwright-typescript/00-overview.md` | Task 0 |
| `docs/playwright-typescript/01-why-playwright.md` | Task 1 |
| `docs/playwright-typescript/02-setup-typescript.md` | Task 2 |
| `docs/playwright-typescript/03-architecture.md` | Task 3 |
| `docs/playwright-typescript/04-locators.md` | Task 4 |
| `docs/playwright-typescript/05-actions-assertions.md` | Task 5 |
| `docs/playwright-typescript/06-debugging.md` | Task 6 |
| `docs/playwright-typescript/07-fixtures.md` | Task 7 |
| `docs/playwright-typescript/08-page-object-model.md` | Task 8 |
| `docs/playwright-typescript/09-test-organization.md` | Task 9 |
| `docs/playwright-typescript/10-configuration-projects.md` | Task 10 |
| `docs/playwright-typescript/11-parallelism-sharding-reporting.md` | Task 11 |
| `docs/playwright-typescript/12-mocking-network-time-browser.md` | Task 12 |
| `docs/playwright-typescript/13-authentication-storage-state.md` | Task 13 |
| `docs/playwright-typescript/14-advanced-browser-emulation.md` | Task 14 |
| `docs/playwright-typescript/15-api-testing-hybrid.md` | Task 15 |
| `docs/playwright-typescript/16-visual-accessibility-testing.md` | Task 16 |
| `docs/playwright-typescript/17-cicd.md` | Task 17 |
| `docs/playwright-typescript/18-production-patterns.md` | Task 18 |
| `docs/playwright-typescript/exercises.md` | Task 19 |
| `docs/playwright-typescript/glossary.md` | Task 20 |

---

### Task 0: Setup — Overview + Source Notes File

**Files:**
- Create: `docs/playwright-typescript-source-notes.md`
- Create: `docs/playwright-typescript/00-overview.md`
- Create dir: `docs/playwright-typescript/`

- [ ] **Step 1: สร้าง directory และ source-notes file**

```bash
mkdir -p "docs/playwright-typescript"
```

สร้าง `docs/playwright-typescript-source-notes.md`:
```markdown
# Playwright TypeScript Course — Source Notes

ไฟล์นี้บันทึก QUOTE จาก playwright.dev สำหรับ cross-check accuracy
ลบไฟล์นี้ได้หลังจาก Quality Review ผ่านทั้ง 18 บทแล้วเท่านั้น

---
```

- [ ] **Step 2: WebFetch ข้อมูลสำหรับ Overview**

```
WebFetch: https://playwright.dev/docs/intro
WebFetch: https://playwright.dev/docs/test-typescript
```

บันทึก: Playwright version ปัจจุบัน, Node.js requirement, TypeScript support statement

- [ ] **Step 3: สร้าง 00-overview.md**

ต้องมีครบ:
- **Prerequisites:**
  - Node.js 20+ (รัน `node --version` verify)
  - npm 9+ (รัน `npm --version` verify)
  - JavaScript/TypeScript เบื้องต้น (variable, function, async/await concept)
  - HTML เบื้องต้น (element, attribute, id, class)
  - Robot Framework หรือ Selenium พื้นฐาน
- **วิธีรัน Demo App:** `cd playwright-course-app && npm install && npm start`
- **วิธีสร้าง Test Project:** `npm init playwright@latest`
- **เวลาเรียนโดยประมาณ** (ตารางแต่ละบท)
- **Table of Contents** พร้อม link ทุกบท
- **Playwright version ที่ใช้:** v1.50+ (ระบุ actual version)

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/ docs/playwright-typescript-source-notes.md
git commit -m "docs: init playwright-typescript course structure and overview"
```

---

### Task 1: บทที่ 1 — Why Playwright? Mindset Shift จาก RF/Selenium

**Files:**
- Create: `docs/playwright-typescript/01-why-playwright.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch และบันทึก source notes**

```
WebFetch: https://playwright.dev/docs/intro
WebFetch: https://playwright.dev/docs/why-playwright  (ถ้ามี)
WebFetch: https://playwright.dev/docs/test-runners
```

บันทึกใน source-notes: QUOTE เกี่ยวกับ auto-waiting, browser isolation, cross-browser support

- [ ] **Step 2: เขียน 01-why-playwright.md ตาม 8-section structure**

**เนื้อหาหลักที่ต้องครอบคลุม:**

Section 4 (เนื้อหา) ต้องอธิบาย:
- **ปัญหาของ Selenium:** explicit waits (`time.sleep`, `Wait Until Element Is Visible`), flaky tests, ChromeDriver version mismatch, slow startup
- **ปัญหาของ RF:** keyword overhead, debugging ยาก, ไม่มี auto-complete ที่ดี, test logic ซ้อนใน keyword ลึกมาก
- **Playwright แก้อะไร:** auto-waiting (no more sleep), built-in browser (ไม่ต้อง driver), TypeScript = IDE support, Trace Viewer
- **เมื่อ RF/Selenium ยังดีกว่า:** legacy systems, non-technical team, keyword-driven สำหรับ business stakeholders

Section 5 (ตัวอย่าง) ต้องมี:
- Beginner: เปรียบเทียบ code ที่ wait element ใน RF/Selenium vs Playwright (no sleep ใน Playwright)
- Intermediate: เหตุการณ์ test flaky เพราะ explicit wait — แก้ด้วย Playwright auto-wait อย่างไร
- Advanced: เปรียบเทียบ test architecture decision — เลือกใช้ Playwright แทน RF เมื่อไหร่ (criteria checklist)

RF/Selenium comparison table ต้องมี:
- Browser driver management, Wait strategy, Language/IDE support, Parallel testing, Debugging tools

- [ ] **Step 3: ไม่มี code ต้อง test (บทนี้ conceptual)**

Verify ว่าไม่มี code snippet ที่ "claimed to run" แต่ไม่ได้ test จริง

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/01-why-playwright.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch01 why playwright — mindset shift from RF/Selenium"
```

---

### Task 2: บทที่ 2 — Setup + TypeScript Essentials

**Files:**
- Create: `docs/playwright-typescript/02-setup-typescript.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/intro
WebFetch: https://playwright.dev/docs/test-typescript
WebFetch: https://playwright.dev/docs/running-tests
```

บันทึก QUOTE: TypeScript support statement, npm init playwright@latest output, tsconfig requirements

- [ ] **Step 2: Install Playwright จริงและบันทึก version**

```bash
mkdir /tmp/pw-test-setup && cd /tmp/pw-test-setup
npm init playwright@latest -- --quiet 2>&1 | head -20
npx playwright --version
node --version
```

บันทึก actual version สำหรับ `// tested: Playwright vX.XX.X`

- [ ] **Step 3: เขียน 02-setup-typescript.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

Section 4 ต้องอธิบาย:
- `npm init playwright@latest` → directory structure ที่ได้ (tests/, playwright.config.ts)
- `tsconfig.json` ที่ Playwright สร้างให้ — อธิบาย key options
- TypeScript types ที่ต้องรู้: `Page`, `Locator`, `Browser`, `BrowserContext`, `APIRequestContext`
- `import { test, expect } from '@playwright/test'` — why ไม่ import จาก 'playwright' ตรงๆ
- `async/await` ใน Playwright — ทุก action เป็น async เพราะอะไร
- `defineConfig()` — standard usage (ไม่ใช้ generic โดยไม่จำเป็น)
- `tsc --noEmit` — verify TypeScript errors ก่อน run tests
- ESLint `@typescript-eslint/no-floating-promises` — ทำไม missing `await` อันตราย

Section 5 ต้องมี code ที่ test ได้จริง:
```typescript
// Beginner: first test
import { test, expect } from '@playwright/test';
test('dashboard has correct title', async ({ page }) => {
  await page.goto('http://localhost:3000');
  await expect(page).toHaveTitle('Playwright Course App — Dashboard');
});
```

- [ ] **Step 4: Test code examples**

```bash
cd /tmp/pw-test-setup
# ต้องมี playwright-course-app รัน: npm start
cat > tests/example.spec.ts << 'EOF'
import { test, expect } from '@playwright/test';
test('first test', async ({ page }) => {
  await page.goto('http://localhost:3000');
  await expect(page).toHaveTitle(/Playwright Course App/);
});
EOF
npx playwright test tests/example.spec.ts --reporter=line
```

Expected output: `1 passed`

```bash
npx tsc --noEmit
```

Expected: ไม่มี error

- [ ] **Step 5: Commit**

```bash
git add docs/playwright-typescript/02-setup-typescript.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch02 setup and TypeScript essentials"
```

---

### Task 3: บทที่ 3 — Architecture: Browser → BrowserContext → Page

**Files:**
- Create: `docs/playwright-typescript/03-architecture.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/browser-contexts
WebFetch: https://playwright.dev/docs/pages
WebFetch: https://playwright.dev/docs/multi-pages
```

บันทึก QUOTE: context isolation definition, page lifecycle, browser reuse

- [ ] **Step 2: เขียน 03-architecture.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

Section 3 (Analogy): "Browser = โรงแรม, BrowserContext = ห้องพัก (แยก isolated), Page = โต๊ะทำงานในห้อง"
Breakdown points: ⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า context ต้องมีเพียง 1 page เท่านั้น (จริงๆ 1 context มีได้หลาย page)

Section 4 ต้องอธิบาย:
- Browser: 1 process, expensive to create, reused ข้าม tests โดย Playwright automatically
- BrowserContext: isolated session (cookies, localStorage, auth state แยกกันสมบูรณ์), lightweight, 1 test = 1 context (Playwright default)
- Page: 1 tab, belongs to exactly 1 context
- ทำไม default `page` fixture จึง isolated ทุก test — เพราะ Playwright สร้าง context ใหม่ทุก test
- Demo: session badge บน navbar แสดง context isolation จริง (`http://localhost:3000`)

Section 5 code examples ต้องมี:
```typescript
// Intermediate: context isolation — 2 users logged in พร้อมกัน
test('two users can be logged in simultaneously', async ({ browser }) => {
  const adminCtx = await browser.newContext();
  const userCtx = await browser.newContext();
  const adminPage = await adminCtx.newPage();
  const userPage = await userCtx.newPage();

  // Login admin
  await adminPage.goto('http://localhost:3000/login');
  await adminPage.fill('[data-testid="input-username"]', 'admin');
  await adminPage.fill('[data-testid="input-password"]', 'admin123');
  await adminPage.click('[data-testid="btn-login"]');

  // Login testuser
  await userPage.goto('http://localhost:3000/login');
  await userPage.fill('[data-testid="input-username"]', 'testuser');
  await userPage.fill('[data-testid="input-password"]', 'test123');
  await userPage.click('[data-testid="btn-login"]');

  // Both session badges are independent
  await expect(adminPage.locator('[data-testid="session-badge"]')).toContainText('admin');
  await expect(userPage.locator('[data-testid="session-badge"]')).toContainText('testuser');

  await adminCtx.close();
  await userCtx.close();
});
```

- [ ] **Step 3: Test code examples**

```bash
npx playwright test tests/ch03-architecture.spec.ts --reporter=line
```

Expected: passed

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/03-architecture.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch03 playwright architecture browser-context-page"
```

---

### Task 4: บทที่ 4 — Locators — Accessibility-First

**Files:**
- Create: `docs/playwright-typescript/04-locators.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/locators
WebFetch: https://playwright.dev/docs/other-locators
WebFetch: https://playwright.dev/docs/best-practices  (section on locators)
```

บันทึก QUOTE: recommended locator priority order จาก official docs, strict mode definition

- [ ] **Step 2: เขียน 04-locators.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

Section 4 สอนตาม official priority order:
1. `getByRole(role, { name })` — ARIA role (preferred สุด), demo: `/components` checkboxes, buttons
2. `getByLabel('text')` — form label, demo: `/login` inputs
3. `getByPlaceholder('text')` — input placeholder
4. `getByText('text')` — visible text
5. `getByAltText('text')` — image alt, demo: `/shop` product images
6. `getByTitle('text')` — title attribute
7. `getByTestId('data-testid')` — fallback เมื่อ semantic locator ไม่พอ
8. `locator('css')` — last resort
9. Filter: `locator.filter({ hasText, has })`, `locator.filter({ visible: true })` ⚠️ verify ก่อนเขียนว่า option นี้มีจริง
10. Combinators: `locator.and(other)`, `locator.or(other)` — เมื่อไหร่ใช้
11. `locator.nth(0)`, `.first()`, `.last()` — เมื่อ strict mode throw
12. Strict mode: เมื่อ locator match หลาย element → error, วิธีแก้
13. `locator.contentFrame()` — สำหรับ iframe (preview Ch12)

Common Mistakes:
- ❌ `page.locator('#submit-btn')` → ✅ `page.getByRole('button', { name: 'Submit' })` เพราะ ID เปลี่ยนได้แต่ ARIA role ไม่เปลี่ยน
- ❌ `page.locator('text=Login')` (legacy) → ✅ `page.getByText('Login')` (current API)
- ❌ locator ที่ match หลาย element แล้วไม่ใช้ `.nth()` → strict mode error

- [ ] **Step 3: Test code examples**

```bash
# ต้อง start playwright-course-app ก่อน
npx playwright test tests/ch04-locators.spec.ts --reporter=line
```

ตัวอย่าง test ที่ต้อง pass:
```typescript
// getByRole
await expect(page.getByRole('button', { name: 'Add' })).toBeVisible();
// getByLabel
await expect(page.getByLabel('Username')).toBeEditable();
// getByAltText (shop page)
await expect(page.getByAltText('Product: iPhone 15 Pro')).toBeVisible();
// filter
const completedTodos = page.locator('[data-testid^="todo-item"]').filter({ hasText: '✓' });
```

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/04-locators.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch04 locators accessibility-first approach"
```

---

### Task 5: บทที่ 5 — Actions & Assertions

**Files:**
- Create: `docs/playwright-typescript/05-actions-assertions.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/input
WebFetch: https://playwright.dev/docs/test-assertions
WebFetch: https://playwright.dev/docs/navigations
```

บันทึก QUOTE: auto-waiting definition, actionability checks, web-first assertions

- [ ] **Step 2: เขียน 05-actions-assertions.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

**Actions:**
- `click()`, `dblclick()` — demo: double-click edit todo ใน `/todos`
- `fill()` vs `pressSequentially()` — fill() replaces all, pressSequentially() simulates keystrokes (เลือกใช้เมื่อไหร่)
- `clear()`, `selectOption()`, `check()`, `uncheck()`
- `hover()` — demo: hover menu ใน `/components`
- `dragTo()` — demo: drag-drop list ใน `/components`
- `press('Enter')`, `press('Tab')`, `press('Control+A')`
- `page.reload()`, `page.goBack()`, `page.goForward()`
- `page.evaluate()` — run JS ใน browser context
- Actionability checks: visible, stable, enabled, editable — Playwright auto-wait จนกว่าจะ pass

**Assertions (Web-first assertions — ไม่เหมือน regular Jest):**
- `toBeVisible()`, `toBeHidden()`, `toBeEnabled()`, `toBeDisabled()`, `toBeChecked()`
- `toHaveText()`, `toContainText()`, `toHaveValue()`, `toHaveAttribute()`, `toHaveClass()`
- `toHaveCount()`, `toHaveURL()`, `toHaveTitle()`
- `toHaveAccessibleName()`, `toHaveRole()` (v1.44+)
- `toMatchAriaSnapshot()` — **mention เท่านั้น** ดู deep dive ใน Ch16
- Dynamic content: `toContainText(/regex/)`, partial match, avoid exact timestamps
- **Soft assertions:** `expect.soft()` — test ไม่หยุดแม้ fail, collect ทุก error ก่อน report
- **Custom matchers:** `expect.extend()` สร้าง `toBeLoggedIn()` เป็นต้น
- `expect.objectContaining()`, `expect.any()` — สำหรับ object shape assertion

- [ ] **Step 3: Test code examples**

```bash
npx playwright test tests/ch05-actions.spec.ts --reporter=line
```

ต้อง test:
```typescript
// dblclick to edit
await page.goto('http://localhost:3000/todos');
await page.fill('[data-testid="input-new-todo"]', 'Test todo');
await page.press('[data-testid="input-new-todo"]', 'Enter');
await page.dblclick('[data-testid^="todo-text-"]');
// soft assertions
const softExpect = expect.soft;
await softExpect(page.getByRole('heading')).toBeVisible();
```

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/05-actions-assertions.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch05 actions and assertions including soft assertions"
```

---

### Task 6: บทที่ 6 — Debugging

**Files:**
- Create: `docs/playwright-typescript/06-debugging.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/debug
WebFetch: https://playwright.dev/docs/trace-viewer
WebFetch: https://playwright.dev/docs/codegen
WebFetch: https://playwright.dev/docs/test-ui-mode
```

บันทึก QUOTE: Inspector description, trace viewer features, UI mode description

- [ ] **Step 2: เขียน 06-debugging.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

Section 4 ต้องอธิบาย tools ทุกตัวพร้อม exact command:

1. **Playwright Inspector:** `PWDEBUG=1 npx playwright test` — step-through, highlight element
2. **`page.pause()`** — หยุด test ณ จุดนั้น เปิด Inspector
3. **Trace Viewer:** config `trace: 'on'` หรือ `trace: 'on-first-retry'` → `npx playwright show-trace trace.zip` — timeline, screenshots, network, console
4. **`--headed` mode:** `npx playwright test --headed` — เห็น browser ทำงาน
5. **Screenshot on failure:** `screenshot: 'only-on-failure'` ใน config
6. **Video recording:** `video: 'retain-on-failure'` ใน config
7. **VS Code Extension:** run/debug test จาก editor, pick locator
8. **Codegen:** `npx playwright codegen http://localhost:3000` — record actions → generate test
9. **UI Mode:** `npx playwright test --ui` — interactive test runner, watch mode, timeline
10. **Watch mode:** `npx playwright test --watch` — re-run ทุกครั้งที่ save file

Section 5:
- Beginner: เปิด Inspector แล้ว step-through test ใน `/login`
- Intermediate: trace CI failure — enable trace, run failing test, อ่าน trace viewer
- Advanced: ใช้ Codegen record test จาก `/checkout` แล้ว refactor เป็น proper test

- [ ] **Step 3: ทดสอบ commands จริง**

```bash
# ทดสอบ Codegen เริ่ม (Ctrl+C หลังเห็น browser เปิด)
timeout 5 npx playwright codegen http://localhost:3000 2>&1 | head -5 || true

# ทดสอบ trace recording
npx playwright test tests/ch06-trace.spec.ts --trace=on --reporter=line
npx playwright show-trace test-results/*/trace.zip --help 2>&1 | head -3
```

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/06-debugging.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch06 debugging inspector trace-viewer ui-mode codegen"
```

---

### Task 7: บทที่ 7 — Fixtures

**Files:**
- Create: `docs/playwright-typescript/07-fixtures.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/test-fixtures
WebFetch: https://playwright.dev/docs/test-fixtures#worker-scoped-fixtures
```

บันทึก QUOTE: fixture definition, test-scoped vs worker-scoped, fixture composition

- [ ] **Step 2: เขียน 07-fixtures.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

Section 3 (Analogy): "Fixture เหมือน mise en place ของเชฟ — เตรียมทุกอย่างไว้ก่อน ใช้ได้ทันที"
Breakdown: ⚠️ ถ้าเชื่อ 100% จะเข้าใจผิดว่า fixture คือแค่ beforeEach/afterEach — แท้จริง fixture มี scope, lazy init, dependency injection

Section 4 ต้องอธิบาย:
- Built-in fixtures: `page`, `browser`, `context`, `request`, `browserName`, `baseURL`
- `test.extend<T>()` — สร้าง custom fixture ด้วย TypeScript generic
- Test-scoped fixture: สร้าง/ล้างต่อ 1 test (เหมาะกับ data ที่ต้องใหม่ทุก test)
- Worker-scoped fixture `{ scope: 'worker' }`: share ข้าม tests ใน worker เดียว (เหมาะกับ expensive setup)
- Automatic fixture `{ auto: true }`: run ทุก test โดยไม่ต้อง declare
- Fixture composition: fixture เรียก fixture อื่น (dependency injection pattern)
- `mergeTests()` และ `mergeExpects()` — รวม fixtures จากหลาย module

```typescript
// test-scoped fixture ที่ต้อง test ได้:
const test = base.extend<{ todoPage: TodoPage; cleanDb: void }>({
  cleanDb: async ({ request }, use) => {
    await request.post('http://localhost:3000/api/reset');
    await use();  // run test
    await request.post('http://localhost:3000/api/reset'); // teardown
  },
  todoPage: async ({ page, cleanDb }, use) => {
    const tp = new TodoPage(page);
    await tp.goto();
    await use(tp);
  }
});
```

- [ ] **Step 3: Test code examples**

```bash
npx playwright test tests/ch07-fixtures.spec.ts --reporter=line
```

ต้อง verify: fixture teardown รัน แม้ test fail (`api/reset` ถูกเรียกทุกครั้ง)

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/07-fixtures.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch07 fixtures test-scoped worker-scoped composition"
```

---

### Task 8: บทที่ 8 — Page Object Model

**Files:**
- Create: `docs/playwright-typescript/08-page-object-model.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/pom
```

บันทึก QUOTE: POM description, when to use

- [ ] **Step 2: เขียน 08-page-object-model.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

Section 4 ต้องมี:
- ปัญหาที่ POM แก้: test code ซ้ำ, locator กระจาย, เมื่อ UI เปลี่ยนต้องแก้หลายที่
- POM pattern ใน TypeScript: class + constructor รับ `Page`, properties เป็น `Locator`, methods เป็น actions
- TypeScript interface สำหรับ page objects — เมื่อไหร่ควรใช้
- Multi-page workflow: `LoginPage` → navigate → `TodoPage`
- Component Object: ส่วนย่อยของ page (เช่น `NavBar`, `TodoItem`)
- Composition vs Inheritance: ใน Playwright ควรใช้ Composition

```typescript
// ตัวอย่างที่ต้อง test ได้:
class LoginPage {
  constructor(private page: Page) {}
  readonly usernameInput = this.page.getByLabel('Username');
  readonly passwordInput = this.page.getByLabel('Password');
  readonly loginButton = this.page.getByRole('button', { name: 'Login' });
  readonly errorMessage = this.page.getByTestId('login-error');

  async goto() { await this.page.goto('http://localhost:3000/login'); }
  async login(username: string, password: string) {
    await this.usernameInput.fill(username);
    await this.passwordInput.fill(password);
    await this.loginButton.click();
  }
}
```

- [ ] **Step 3: Test code examples**

```bash
npx playwright test tests/ch08-pom.spec.ts --reporter=line
```

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/08-page-object-model.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch08 page object model TypeScript patterns"
```

---

### Task 9: บทที่ 9 — Test Organization

**Files:**
- Create: `docs/playwright-typescript/09-test-organization.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/test-annotations
WebFetch: https://playwright.dev/docs/test-parameterize
WebFetch: https://playwright.dev/docs/api/class-test  (section on test.step)
```

บันทึก QUOTE: annotation descriptions, test.each syntax, test.step description

- [ ] **Step 2: เขียน 09-test-organization.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

Section 4 ต้องอธิบาย (แยก Annotations จาก Tags ให้ชัด):

**Annotations (built-in):**
- `test.skip(condition, reason)` — ข้าม test นี้ (ต่างจาก `.skip` ใน Tags)
- `test.fail(condition, reason)` — expect ว่า test จะ fail (ถ้าผ่านกลับเป็น fail)
- `test.fixme(condition, reason)` — known broken, mark ไว้แก้ทีหลัง
- `test.slow()` — เพิ่ม timeout 3x สำหรับ test นี้
- Runtime annotation: `testInfo.annotations.push({ type: 'issue', description: '...' })`

**Tags (ใช้กับ grep/filter):**
- `test('title @smoke @regression', ...)` หรือ `test.tag(['smoke'])` (v1.42+)
- `npx playwright test --grep @smoke`
- `npx playwright test --grep-invert @slow`

**Parameterize:**
- `test.each(data)(name, fn)` — data-driven testing
- Parameterized projects (Ch10 preview)

**`test.step()`:**
- จัดกลุ่ม actions ใน Trace Viewer
- `test.step('Login as admin', async () => { ... })`
- step timeout: `test.step('...', fn, { timeout: 5000 })`
- `testInfo` object: `testInfo.title`, `testInfo.status`, `testInfo.outputDir`

- [ ] **Step 3: Test code examples**

```bash
npx playwright test tests/ch09-organization.spec.ts --reporter=line
# test.each example:
npx playwright test tests/ch09-each.spec.ts --reporter=line
```

```typescript
// test.each ที่ต้อง test ได้:
const credentials = [
  { username: 'admin', password: 'admin123', role: 'admin' },
  { username: 'testuser', password: 'test123', role: 'user' },
];
test.each(credentials)('login as $username has role $role', async ({ page }, { username, password, role }) => {
  // ...
});
```

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/09-test-organization.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch09 test organization annotations tags parameterize"
```

---

### Task 10: บทที่ 10 — Configuration & Projects

**Files:**
- Create: `docs/playwright-typescript/10-configuration-projects.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/test-configuration
WebFetch: https://playwright.dev/docs/test-projects
WebFetch: https://playwright.dev/docs/test-timeouts
WebFetch: https://playwright.dev/docs/test-webserver
WebFetch: https://playwright.dev/docs/test-global-setup-teardown
```

บันทึก QUOTE: projects description, timeout types, webServer option, global setup via project dependencies (modern approach)

- [ ] **Step 2: เขียน 10-configuration-projects.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

Section 4:

**`defineConfig()` deep dive:**
```typescript
export default defineConfig({
  testDir: './tests',
  fullyParallel: false,    // parallel ทุก test (ดู Ch11)
  forbidOnly: !!process.env.CI,  // fail ถ้ามี test.only ใน CI
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

**Projects (multi-browser + multi-environment):**
- cross-browser: `chromium`, `firefox`, `webkit`
- project dependencies: `setup` project รัน auth ก่อน test projects
- filter by project: `npx playwright test --project=chromium`

**Timeouts — 7 ประเภท:**
| Type | Default | Set via |
|------|---------|---------|
| test | 30000ms | `timeout` in config |
| expect | 5000ms | `expect.timeout` |
| action | no timeout | `actionTimeout` |
| navigation | no timeout | `navigationTimeout` |
| global | no timeout | `globalTimeout` |
| fixture | no timeout | fixture's `timeout` |
| beforeAll/afterAll | 30000ms | `timeout` in hook |

**`webServer` option:** start dev server อัตโนมัติก่อน test
**`dotenv` integration:** `require('dotenv').config()` ต้นไฟล์ config
**Global Setup via Project Dependencies (modern):** ต่างจาก `globalSetup` แบบเก่าอย่างไร

⚠️ Verify ก่อนเขียน: `testConfig.tsconfig` option — ตรวจ exact name กับ official docs

- [ ] **Step 3: Test code examples**

```bash
# ทดสอบ multi-project config
npx playwright test --project=chromium --reporter=line
npx playwright test --project=firefox --reporter=line
```

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/10-configuration-projects.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch10 configuration projects timeouts webserver"
```

---

### Task 11: บทที่ 11 — Parallelism, Sharding & Reporting

**Files:**
- Create: `docs/playwright-typescript/11-parallelism-sharding-reporting.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/test-parallel
WebFetch: https://playwright.dev/docs/test-sharding
WebFetch: https://playwright.dev/docs/test-reporters
```

บันทึก QUOTE: parallelism model, sharding explanation, merge-reports command

- [ ] **Step 2: เขียน 11-parallelism-sharding-reporting.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

**Parallelism:**
- `workers: N` — กี่ worker processes รัน parallel
- `fullyParallel: true` — ทุก test รัน parallel (ไม่ใช่แค่ test files)
- `test.describe.parallel()` — parallel ภายใน describe block
- `test.describe.serial()` — force sequential ใน describe block (ไม่ใช่ `test.serial()`)
- `test.describe.configure({ mode: 'parallel' | 'serial' })` — alternative API

**⚠️ ข้อจำกัดสำคัญ:**
- Shared state: parallel tests ที่แชร์ `/api/todos` จะ interfere กัน — demo ด้วย todo count ใน badge
- Solution: สร้าง unique test data per test, ใช้ `/api/reset` ใน fixture teardown
- Port conflicts: ถ้า tests เปิด server เองต้องระวัง

**Sharding:**
- `npx playwright test --shard=1/4` — แบ่ง test เป็น 4 chunks
- Blob reporter สำหรับรวม results: `reporter: [['blob']]`
- Merge: `npx playwright merge-reports --reporter html ./blob-reports`

**CLI flags:**
- `--last-failed` — รันเฉพาะ test ที่ fail ครั้งล่าสุด
- `--repeat-each=3` — รัน test ซ้ำ 3 รอบ (หา flaky tests)
- `--only-changed` ⚠️ verify ว่ามีใน v1.51+ จริง
- `failOnFlakyTests` ❌ ไม่มีใน Playwright — ใช้ `retries: 0` + `--forbid-only` แทน

**Reporters:**
- `html` — interactive HTML report
- `line`, `dot` — CI-friendly
- `json` — machine readable
- `junit` — Jenkins/GitLab integration
- `allure-playwright` — Allure integration (install แยก)

- [ ] **Step 3: Test code examples**

```bash
# Test sharding
npx playwright test --shard=1/2 --reporter=blob
npx playwright test --shard=2/2 --reporter=blob
npx playwright merge-reports --reporter html ./blob-reports
```

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/11-parallelism-sharding-reporting.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch11 parallelism sharding reporting"
```

---

### Task 12: บทที่ 12 — Mocking: Network, Time & Browser APIs

**Files:**
- Create: `docs/playwright-typescript/12-mocking-network-time-browser.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/mock
WebFetch: https://playwright.dev/docs/network
WebFetch: https://playwright.dev/docs/clock
WebFetch: https://playwright.dev/docs/mock-browser-apis
```

บันทึก QUOTE: route description, clock API introduction, addInitScript description

- [ ] **Step 2: เขียน 12-mocking-network-time-browser.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

**Network Interception:**
- `page.route(url, handler)` — intercept requests
- `route.fulfill({ status, body, contentType })` — mock response
- `route.abort()` — cancel request (simulate offline)
- `route.continue({ headers, postData })` — pass through กับ modification
- `page.waitForResponse(url)`, `page.waitForRequest(url)`
- `page.on('request', ...)`, `page.on('response', ...)`
- HAR recording: `context.recordHAR({ path: 'recording.har' })`
- HAR playback: `page.routeFromHAR('recording.har')` — offline replay
- Extra HTTP headers: per-route ผ่าน `route.continue({ headers })` (context-level อยู่ใน Ch15)

**WebSocket:**
- `page.routeWebSocket(url, handler)` (v1.48) — intercept WebSocket
- Demo: intercept `ws://localhost:3000/ws`, verify ping messages

**Clock API (v1.45+):**
- `await page.clock.install({ time: new Date('2024-01-01') })` — freeze time
- `await page.clock.setFixedTime(date)` — set fixed time shortcut
- `await page.clock.fastForward(1000 * 60 * 5)` — skip 5 minutes (milliseconds)
- `await page.clock.runFor(1000)` — run timers for 1 second
- Use case: test session timeout, countdown timer, scheduled tasks

**Mock Browser APIs:**
- `page.addInitScript(script)` — inject ก่อน page load (page-scoped)
- `context.addInitScript(script)` — inject สำหรับทุก page ใน context (context-scoped)
- `page.exposeFunction(name, fn)` — expose Node.js function ให้ browser code เรียกได้
- Use case: mock `navigator.geolocation`, `window.matchMedia`, Battery API

- [ ] **Step 3: Test code examples**

```bash
npx playwright test tests/ch12-mocking.spec.ts --reporter=line
```

```typescript
// Network mock ที่ต้อง test:
await page.route('**/api/products*', route => {
  route.fulfill({ json: { data: [{ id: 999, name: 'Mock Product', price: 1, category: 'Test', description: '', image: '' }], total: 1, page: 1, limit: 5, totalPages: 1 } });
});
// Clock ที่ต้อง test:
await page.clock.install({ time: new Date('2030-12-31T23:59:00') });
await page.clock.fastForward(61 * 1000); // skip 61 seconds
```

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/12-mocking-network-time-browser.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch12 mocking network clock browser-apis"
```

---

### Task 13: บทที่ 13 — Authentication & Storage State

**Files:**
- Create: `docs/playwright-typescript/13-authentication-storage-state.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/auth
WebFetch: https://playwright.dev/docs/auth#multiple-signed-in-roles
```

บันทึก QUOTE: storageState definition, global setup via project deps, multiple roles pattern

- [ ] **Step 2: เขียน 13-authentication-storage-state.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

Section 4:
- `storageState` — save cookies + localStorage ไว้ใน JSON file
- `context.storageState({ path: 'playwright/.auth/user.json' })` — export หลัง login
- Re-use: `use: { storageState: 'playwright/.auth/user.json' }` ใน config project
- `.gitignore` ต้องมี `playwright/.auth/` ⚠️ (ไม่ commit token)
- Global auth setup via Project Dependencies (modern — ไม่ใช้ `globalSetup` แบบเก่า):

```typescript
// playwright.config.ts
projects: [
  { name: 'setup', testMatch: /.*\.setup\.ts/ },
  { name: 'chromium', use: { storageState: 'playwright/.auth/user.json' }, dependencies: ['setup'] }
]
```

- Multiple roles: `user.json` และ `admin.json` — test ที่ต้อง login เป็น admin
- Per-worker auth: `testInfo.parallelIndex` — สร้าง auth state แยกต่อ worker (ป้องกัน conflict ใน parallel)
- `context.addCookies([...])` — inject cookies โดยตรง (เมื่อ login ผ่าน API ได้ token แต่ใช้ cookie)
- `context.cookies()` — ดู cookies ปัจจุบัน
- **IndexedDB gotcha:** `storageState` ไม่ capture IndexedDB — ต้องใช้ `page.evaluate()` + `addInitScript` แทน
- **sessionStorage gotcha:** `storageState` capture sessionStorage ได้เฉพาะ domain เดียวกัน

- [ ] **Step 3: Test code examples**

```bash
# สร้าง auth state จริง
npx playwright test tests/auth.setup.ts --reporter=line
# ตรวจว่าไฟล์สร้าง
ls playwright/.auth/
# รัน test ที่ใช้ auth state
npx playwright test tests/ch13-auth.spec.ts --reporter=line
```

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/13-authentication-storage-state.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch13 authentication storage-state multi-role"
```

---

### Task 14: บทที่ 14 — Advanced Browser + Emulation

**Files:**
- Create: `docs/playwright-typescript/14-advanced-browser-emulation.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/pages  (popups, dialogs)
WebFetch: https://playwright.dev/docs/frames
WebFetch: https://playwright.dev/docs/downloads
WebFetch: https://playwright.dev/docs/emulation
```

บันทึก QUOTE: popup handling, frameLocator description, download event, devices list, emulation options

- [ ] **Step 2: เขียน 14-advanced-browser-emulation.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

**Popups / New Tabs:**
```typescript
const [popup] = await Promise.all([
  page.waitForEvent('popup'),
  page.click('[data-testid="btn-open-popup"]'),
]);
await popup.waitForLoadState();
await expect(popup).toHaveTitle(/Todo/);
```

**iFrames:**
- `page.frameLocator('iframe[data-testid="embedded-iframe"]')` — returns FrameLocator
- `frameLocator.locator('input')` — locator ภายใน iframe
- `locator.contentFrame()` — จาก Locator → FrameLocator (v1.43)
- ⚠️ cross-origin iframe มีข้อจำกัด

**File Upload:**
```typescript
await page.locator('[data-testid="input-file"]').setInputFiles('path/to/file.txt');
```

**File Download:**
```typescript
const [download] = await Promise.all([
  page.waitForEvent('download'),
  page.click('[data-testid="btn-download"]'),
]);
const path = await download.path();
```

**Dialogs:**
```typescript
page.once('dialog', dialog => dialog.accept('Playwright'));
await page.click('[data-testid="btn-prompt"]');
```

**Shadow DOM:**
```typescript
// locator สร้างได้ปกติใน open shadow root
await page.locator('my-counter').locator('#inc').click();
```

**Emulation:**
- `devices['iPhone 15 Pro']` ⚠️ verify exact string กับ `playwright.devices`
- `{ locale: 'th-TH', timezone: 'Asia/Bangkok' }`
- `{ geolocation: { latitude: 13.7563, longitude: 100.5018 }, permissions: ['geolocation'] }`
- `{ colorScheme: 'dark' }` — dark mode
- `{ offline: true }` — simulate offline

**`page.emulateMedia()`:**
- `{ media: 'print' }` — print stylesheet
- `{ colorScheme: 'dark' }` — change mid-test

**`page.mouse` (low-level):**
- `page.mouse.move(x, y)`, `.click(x, y)`, `.dblclick(x, y)`
- ใช้เมื่อ `locator.click()` ไม่พอ (custom canvas, game, pixel-precise)

- [ ] **Step 3: Test code examples**

```bash
npx playwright test tests/ch14-advanced.spec.ts --reporter=line
```

Test ต้อง cover: popup, iframe locator, file download, dialog accept, shadow DOM click

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/14-advanced-browser-emulation.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch14 advanced browser features and emulation"
```

---

### Task 15: บทที่ 15 — API Testing + Hybrid

**Files:**
- Create: `docs/playwright-typescript/15-api-testing-hybrid.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/api-testing
```

บันทึก QUOTE: request fixture description, APIRequestContext methods, response assertions

- [ ] **Step 2: เขียน 15-api-testing-hybrid.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

Section 4:
- `request` fixture: `APIRequestContext` — built-in, ไม่ต้องติดตั้งเพิ่ม
- `request.get(url)`, `.post(url, { data })`, `.put()`, `.patch()`, `.delete()`
- `response.ok()`, `response.status()`, `response.json()`, `response.text()`
- `expect(response).toBeOK()`, `expect(response).toHaveStatus(201)`
- Auth header: `request.post(url, { headers: { Authorization: 'Bearer ...' } })`
- **Context-level `extraHTTPHeaders`** (ต่างจาก per-route ใน Ch12):
  ```typescript
  const authRequest = await request.newContext({
    extraHTTPHeaders: { Authorization: `Bearer ${token}` }
  });
  ```
- Auth state transfer: login ผ่าน API แล้วใช้ cookies ใน browser context
- **Hybrid test pattern** (ทรงพลังที่สุด):
  ```typescript
  test('todo created via API appears in UI', async ({ request, page }) => {
    // Setup via API (fast)
    await request.post('http://localhost:3000/api/todos', { data: { text: 'Test via API' } });
    // Verify in UI
    await page.goto('http://localhost:3000/todos');
    await expect(page.getByText('Test via API')).toBeVisible();
  });
  ```
- `expect.objectContaining()` สำหรับ API response shape assertion
- Response schema validation pattern

RF/Selenium comparison: RF ไม่มี built-in API testing (ต้องใช้ RequestsLibrary) vs Playwright built-in

- [ ] **Step 3: Test code examples**

```bash
npx playwright test tests/ch15-api.spec.ts --reporter=line
```

Test ต้อง cover: GET todos list, POST create, PATCH update, DELETE, protected endpoint 401, admin endpoint 403 เมื่อ user ทั่วไป, hybrid test

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/15-api-testing-hybrid.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch15 api testing and hybrid ui+api patterns"
```

---

### Task 16: บทที่ 16 — Visual Testing + Accessibility Testing

**Files:**
- Create: `docs/playwright-typescript/16-visual-accessibility-testing.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/test-snapshots
WebFetch: https://playwright.dev/docs/accessibility-testing
```

บันทึก QUOTE: toHaveScreenshot description, update snapshots, axe integration, toMatchAriaSnapshot (v1.49)

- [ ] **Step 2: ติดตั้ง @axe-core/playwright**

```bash
cd /tmp/pw-test-setup
npm install @axe-core/playwright
```

บันทึก version ที่ติดตั้ง

- [ ] **Step 3: เขียน 16-visual-accessibility-testing.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

**Visual Testing:**
- `await expect(page).toHaveScreenshot('dashboard.png')` — full page screenshot baseline
- `await expect(locator).toHaveScreenshot('button.png')` — element screenshot
- options: `{ maxDiffPixels: 100 }`, `{ threshold: 0.1 }`, `{ animations: 'disabled' }`
- Update baseline: `npx playwright test --update-snapshots`
- **Platform gotcha:** screenshots ต่างกันระหว่าง macOS / Linux / Windows — CI ต้องใช้ OS เดียวกัน (Docker)
- Demo: screenshot `/visual` ทั้ง light และ dark mode

**Accessibility Testing:**
- ติดตั้ง: `npm install @axe-core/playwright`
- Basic scan:
  ```typescript
  import { checkA11y } from '@axe-core/playwright';
  await checkA11y(page, undefined, { axeOptions: { runOnly: ['wcag2a', 'wcag2aa'] } });
  ```
- Targeted scan: scan เฉพาะ component
- อ่าน violations report
- Common WCAG violations ที่พบบ่อย: missing alt text, color contrast, missing label

**`toMatchAriaSnapshot()` (v1.49) — deep dive:**
```typescript
await expect(page.getByRole('list')).toMatchAriaSnapshot(`
  - listitem: Todo 1
  - listitem: Todo 2
`);
```
- Accessibility tree assertion ต่างจาก visual screenshot อย่างไร
- `--update-snapshots` สำหรับ aria snapshots ด้วย

- [ ] **Step 4: Test code examples**

```bash
npx playwright test tests/ch16-visual.spec.ts --reporter=line
# Generate initial screenshots
npx playwright test tests/ch16-visual.spec.ts --update-snapshots --reporter=line
# Re-run to verify match
npx playwright test tests/ch16-visual.spec.ts --reporter=line
```

- [ ] **Step 5: Commit**

```bash
git add docs/playwright-typescript/16-visual-accessibility-testing.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch16 visual testing and accessibility testing"
```

---

### Task 17: บทที่ 17 — CI/CD

**Files:**
- Create: `docs/playwright-typescript/17-cicd.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/ci
WebFetch: https://playwright.dev/docs/ci-intro
WebFetch: https://playwright.dev/docs/docker
```

บันทึก QUOTE: CI configuration recommendations, Docker image name, GitHub Actions example

- [ ] **Step 2: เขียน 17-cicd.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

Section 4:

**GitHub Actions (complete YAML):**
```yaml
name: Playwright Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
      - name: Start demo app
        run: cd playwright-course-app && npm install && npm start &
      - name: Install Playwright
        run: cd my-tests && npm ci && npx playwright install --with-deps
      - name: TypeScript check
        run: cd my-tests && npx tsc --noEmit
      - name: Run tests
        run: cd my-tests && npx playwright test
        env:
          CI: true
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: playwright-report
          path: my-tests/playwright-report/
```

**`CI=true` effects:**
- `retries: process.env.CI ? 2 : 0`
- `forbidOnly: !!process.env.CI` — fail ถ้ามี `test.only` ลืม commit

**`--reporter=github`:** GitHub PR annotations ชี้ตำแหน่ง failed test

**Docker:**
```dockerfile
FROM mcr.microsoft.com/playwright:v1.XX.X-jammy
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npx playwright install
CMD ["npx", "playwright", "test"]
```

**Sharding ใน CI (matrix strategy vs `--shard`):**
- matrix: หลาย jobs รัน parallel ใน GitHub Actions
- shard: แบ่ง test files ให้แต่ละ job
- merge-reports: รวม blob reports จากทุก shard

**`--only-changed`** ⚠️ verify ก่อนเขียน: รัน test เฉพาะไฟล์ที่เปลี่ยน

- [ ] **Step 3: Validate YAML syntax**

```bash
# Validate GitHub Actions YAML (ถ้ามี actionlint)
which actionlint && actionlint docs/playwright-typescript/examples/ci.yml || echo "actionlint not available, manual review"
```

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/17-cicd.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch17 ci-cd github-actions docker sharding"
```

---

### Task 18: บทที่ 18 — Production Patterns

**Files:**
- Create: `docs/playwright-typescript/18-production-patterns.md`
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch**

```
WebFetch: https://playwright.dev/docs/best-practices
WebFetch: https://playwright.dev/docs/retries
```

บันทึก QUOTE: best practices list, retry behavior, trace on retry

- [ ] **Step 2: เขียน 18-production-patterns.md**

**เนื้อหาหลักที่ต้องครอบคลุม:**

**Flaky Test Strategies:**
- `retries: 2` global + `trace: 'on-first-retry'` — see exactly what went wrong on retry
- `test.retries(3)` per-test override
- `test.fixme()` — known broken, skip with reason, remind to fix
- `test.fail()` — expected to fail (unusual but valid)
- `--repeat-each=5` — find flaky tests locally
- **ข้อจำกัดของ retry:** non-idempotent operations (POST ซ้ำ → duplicate records) ต้องแก้ด้วย idempotency หรือ cleanup fixture

**`test.abort()` — unrecoverable errors:**
```typescript
test('critical order flow', async ({ page }) => {
  await page.goto('/');
  if (!await page.locator('[data-testid="nav-shop"]').isVisible()) {
    await test.info().attach('screenshot', { body: await page.screenshot() });
    test.abort('Navigation not available — cannot proceed');
  }
  // ... rest of test
});
```

**Test Data Factory Pattern (code จริง):**
```typescript
interface Todo { id: number; text: string; completed: boolean; createdAt: string; }

class TodoFactory {
  constructor(private request: APIRequestContext) {}

  async create(text: string = `Todo-${Date.now()}`): Promise<Todo> {
    const res = await this.request.post('http://localhost:3000/api/todos', { data: { text } });
    return res.json();
  }

  async createMany(count: number): Promise<Todo[]> {
    return Promise.all(Array.from({ length: count }, (_, i) => this.create(`Todo ${i + 1}`)));
  }

  async cleanup(): Promise<void> {
    await this.request.post('http://localhost:3000/api/reset');
  }
}
```

**Large Suite Organization:**
- กลุ่ม test โดย feature ไม่ใช่ layer (`tests/todos/` ไม่ใช่ `tests/ui/`, `tests/api/`)
- `--forbid-only` บน CI ป้องกัน `test.only` ลืม push
- Tagging: `@smoke` subset สำหรับ fast feedback, `@regression` full suite
- Environment config: `.env.staging`, `.env.production` ผ่าน dotenv

- [ ] **Step 3: Test code examples**

```bash
npx playwright test tests/ch18-production.spec.ts --reporter=line
# Test factory pattern + retry
npx playwright test tests/ch18-flaky.spec.ts --retries=2 --reporter=line
```

- [ ] **Step 4: Commit**

```bash
git add docs/playwright-typescript/18-production-patterns.md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch18 production patterns flaky tests data factory"
```

---

### Task 19: exercises.md

**Files:**
- Create: `docs/playwright-typescript/exercises.md`

- [ ] **Step 1: เขียน exercises.md ครอบคลุมทุก 18 บท**

**Structure สำหรับแต่ละบท:**
```markdown
## บทที่ N — [ชื่อบท]

### Recall (Beginner)
[คำถาม อธิบายด้วยคำตัวเอง / ยกตัวอย่าง]

### Application (Intermediate)
[สถานการณ์ใหม่ที่ไม่ซ้ำกับตัวอย่างในบท]

### Synthesis (Advanced)
[วิเคราะห์ bug / ออกแบบระบบ / เปรียบเทียบ tradeoff]

---
เฉลย (ซ่อนด้วย `<details>`)
```

**Exercise standards (ตาม CLAUDE.md):**
- ห้าม fill-in-the-blank
- Intermediate: สถานการณ์ใหม่ ห้าม copy context/ตัวเลขจากบท
- Advanced: Synthesis / Diagnosis / Design

ตัวอย่างที่ดี:
- Ch04 Advanced: "เพื่อนส่ง test มาให้ review พบว่าใช้ `page.locator('.submit-btn')` ทั่ว file ลองวิเคราะห์ว่ามีปัญหาอะไร และ refactor ด้วย locator ที่เหมาะสมกว่า"
- Ch11 Advanced: "ออกแบบ sharding strategy สำหรับ 500 tests ที่ต้องรันใน GitHub Actions ให้เสร็จภายใน 5 นาที — มีงบ runner 4 machines"

- [ ] **Step 2: Commit**

```bash
git add docs/playwright-typescript/exercises.md
git commit -m "docs: add exercises for all 18 chapters"
```

---

### Task 20: glossary.md

**Files:**
- Create: `docs/playwright-typescript/glossary.md`

- [ ] **Step 1: รวบรวม terms จากทุกบทและ WebFetch definitions**

Terms ที่ต้องมี (verify จาก playwright.dev):
`BrowserContext`, `Locator`, `Fixture`, `Trace`, `Shard`, `Storage State`, `Page Object Model`, `Auto-waiting`, `Actionability`, `Strict Mode`, `Web-first Assertions`, `Codegen`, `HAR`, `Emulation`, `Accessibility Tree`, `Shadow DOM`, `Worker`, `Test Step`, `Annotation`, `Tag`

- [ ] **Step 2: เขียน glossary.md**

Format แต่ละ term:
```markdown
## [Term]
**SOURCE:** https://playwright.dev/docs/[page]
[คำอธิบายภาษาไทย 1-3 ประโยค พร้อม context ว่าใช้ตอนไหน]
```

- [ ] **Step 3: Commit**

```bash
git add docs/playwright-typescript/glossary.md
git commit -m "docs: add glossary with 20+ terms verified from playwright.dev"
```

---

## Self-Review

**Spec coverage check:**

| Spec requirement | Task |
|-----------------|------|
| 18 chapters ทุกบทมี 8 sections | Tasks 1-18 |
| RF/Selenium comparison ทุกบท | Tasks 1-18 |
| Source notes ทุก concept | Tasks 0-18 (source-notes.md) |
| Code examples tested จริง | Steps 3/4 ในทุก Task |
| `test.describe.serial()` (ไม่ใช่ `test.serial()`) | Task 11 |
| `failOnFlakyTests` ❌ พร้อม note | Task 11 |
| `toMatchAriaSnapshot()` intro Ch05 / deep dive Ch16 | Tasks 5, 16 |
| `test.step()` อยู่ใน Ch09 (ไม่ใช่ Ch07) | Tasks 7, 9 |
| Clock API methods ถูกต้อง (ไม่มี "freeze") | Task 12 |
| JWT response `{ token }` | Task 13 |
| extraHTTPHeaders = context-level (Ch15) | Task 15 |
| exercises.md ≥3 ข้อ/concept | Task 19 |
| glossary.md + SOURCE URL | Task 20 |
| Verify list items (testConfig.tsconfig, --only-changed, filter visible) | Tasks 10, 11, 4 |

**Placeholder scan:** ไม่พบ TBD หรือ incomplete steps ✅

**Type/Method consistency:** `apiFetch`, `Auth`, `TodoFactory`, page object class patterns สม่ำเสมอตลอด ✅
