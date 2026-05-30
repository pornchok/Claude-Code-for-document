# Playwright TypeScript Master Course — Design Spec

**Date:** 2026-05-30
**Status:** Approved
**Language:** TypeScript (primary), Thai (document language)
**Minimum Playwright Version:** v1.50+
**Node.js:** 20+ (LTS)

---

## 1. Overview

Course สอน Playwright ด้วย TypeScript ตั้งแต่ศูนย์ถึงระดับ Master
สำหรับผู้เรียนที่มีพื้นฐาน Robot Framework + Selenium มานิดหน่อย

**เป้าหมายปลายทาง:** อ่านจบแล้วสามารถ:
- เขียน Playwright test suite ระดับ production ได้ด้วยตัวเอง
- Debug test ที่ fail ใน CI ได้โดยไม่ติดขัด
- ออกแบบ test architecture (fixtures, POM, parallelism) สำหรับ large project
- ครอบคลุม ~95%+ ของ Playwright official docs

---

## 2. ระบบโดยรวม — 2 ส่วน

```
docs/playwright-typescript/        ← เอกสาร 18 บท + exercises + glossary (Markdown)
playwright-course-app/             ← Demo Web App (Node.js/Express) — pre-built
```

ไม่มี `playwright-course-tests/` — คนเรียนรัน `npm init playwright@latest`
แล้วสร้างทุกอย่างเอง เหมือนงานจริง

---

## 3. Course Structure — 18 บท

| บท | หัวข้อ | Topics หลักที่ครอบคลุม |
|----|--------|------------------------|
| 01 | Why Playwright? Mindset Shift จาก RF/Selenium | Selenium limitations, Playwright architecture overview, RF keyword-driven vs code-first, auto-waiting concept |
| 02 | Setup + TypeScript Essentials | `npm init playwright@latest`, `tsconfig.json`, `defineConfig()` (standard — ไม่ใช้ generic โดยไม่จำเป็น), TypeScript types (`Page`, `Locator`, `Browser`, `BrowserContext`), `tsc --noEmit`, ESLint `@typescript-eslint/no-floating-promises` |
| 03 | Architecture: Browser → BrowserContext → Page | 3-layer model, context isolation demo ผ่าน session badge บน navbar, multiple tabs, incognito-like isolation |
| 04 | Locators — Accessibility-First | `getByRole`, `getByLabel`, `getByPlaceholder`, `getByText`, `getByAltText`, `getByTitle`, `getByTestId`, `locator.and()`, `locator.or()`, `locator.filter({ hasText, has })`, `locator.contentFrame()`, nth/first/last, strict mode |
| 05 | Actions & Assertions | `click`, `dblclick`, `fill`, `clear`, `check`, `selectOption`, `hover`, `dragTo`, `press`, keyboard, `page.reload()`, `goBack()`, `goForward()`, `evaluate()`, `toBeVisible`, `toHaveText`, `toHaveValue`, `toHaveCount`, `toHaveURL`, `expect.soft()`, `expect.extend()`, `expect.objectContaining()`, `expect.any()`, `toHaveAccessibleName()`, `toHaveRole()`, `toMatchAriaSnapshot()` (**intro เท่านั้น** — deep dive อยู่ Ch16), dynamic content handling (regex, partial match) |
| 06 | Debugging | `PWDEBUG=1`, Trace Viewer, `page.pause()`, VS Code extension, Codegen, `--headed`, screenshot/video on failure, UI Mode (`--ui`), Watch mode (`--watch`) |
| 07 | Fixtures — Test/Worker-scoped + Composition | `test.extend<T>()`, test-scoped, worker-scoped, fixture composition, built-in fixtures (`page`, `browser`, `context`, `request`), `{ auto: true }` automatic fixtures, `mergeTests()`, `mergeExpects()` |
| 08 | Page Object Model + Design Patterns | Class-based POM, TypeScript interface สำหรับ page objects, multi-page workflow, component objects |
| 09 | Test Organization | `test.skip()`, `test.fail()`, `test.fixme()`, `test.slow()` (Annotations — ต่างจาก Tags), Tags (`@smoke`, `@regression`), `test.each()` + data-driven testing, `test.step()` (รวมถึง step timeout), custom annotations, `testInfo` object |
| 10 | Configuration & Projects | `defineConfig()` deep dive, `projects` (setup/teardown per project, dependencies, multi-environment), Timeouts ทั้ง 7 ประเภท (test / expect / action / navigation / global / beforeAll / fixture), `webServer` option, `dotenv` integration, Global Setup via **Project Dependencies** (modern approach แทน `globalSetup` แบบเก่า), `forbidOnly: true` ใน CI |
| 11 | Parallelism, Sharding & Reporting | `workers`, `fullyParallel`, `test.describe.parallel()`, **`test.describe.serial()`** (ไม่ใช่ `test.serial()`), `test.describe.configure({ mode })`, ข้อจำกัด shared state, sharding (`--shard=1/4`), `npx playwright merge-reports --reporter html ./blob-reports`, `--last-failed`, `--repeat-each`, `--only-changed` (v1.51+), Reporters (HTML, Allure, JUnit) — **หมายเหตุ: `failOnFlakyTests` ไม่มีใน Playwright** ใช้ `retries: process.env.CI ? 2 : 0` แทน |
| 12 | **Mocking: Network, Time & Browser APIs** | `page.route()`, `route.fulfill()`, `route.abort()`, `route.continue()`, `waitForResponse/Request`, HAR record + playback (`routeFromHAR()`), `routeWebSocket()` (v1.48), **Clock API**: `clock.install()`, `clock.setFixedTime()`, `clock.fastForward()`, `clock.runFor()` (ไม่มี method ชื่อ "freeze"), **Mock Browser APIs**: `page.addInitScript()` (page-scoped) vs `context.addInitScript()` (context-scoped — ทำงานกับทุก page ใน context), `page.exposeFunction()`, per-route headers ผ่าน `route.continue({ headers })` |
| 13 | Authentication & Storage State | `storageState`, Global Setup auth, multiple roles, `context.addCookies()`, `context.cookies()`, **per-worker auth** (`testInfo.parallelIndex`), `playwright/.auth` convention + `.gitignore`, IndexedDB auth gotcha, sessionStorage gotcha (ต้อง combine กับ `addInitScript`) |
| 14 | Advanced Browser + Emulation | Popups (`waitForEvent('popup')`), iFrames (`frameLocator()`), File Upload (`setInputFiles()`), File Download (`waitForEvent('download')`), Dialog (alert/confirm/prompt), Shadow DOM, **Emulation**: `devices['iPhone 15']`, locale, timezone, geolocation, colorScheme, permissions, offline, **`page.emulateMedia()`** (2 use cases: `{ media: 'print' }` และ `{ colorScheme: 'dark' }`), `page.mouse` low-level API |
| 15 | API Testing + Hybrid | `request` fixture, `APIRequestContext`, CRUD assertions, `toBeOK()`, auth header, auth state transfer API ↔ browser context, **context-level `extraHTTPHeaders`** (ต่างจาก per-route headers ใน Ch12), Hybrid test (API setup → UI verify) |
| 16 | Visual Testing + Accessibility Testing | `toHaveScreenshot()`, `toMatchSnapshot()`, `--update-snapshots`, platform diff gotcha ใน CI, **`@axe-core/playwright`** (install แยก), WCAG compliance, **`toMatchAriaSnapshot()` (v1.49) — deep dive** (Ch05 แค่ intro) |
| 17 | CI/CD | GitHub Actions YAML, `CI=true` env, `--reporter=github`, `tsc --noEmit` ใน CI pipeline, Docker (`mcr.microsoft.com/playwright`), parallel matrix vs sharding strategy |
| 18 | Production Patterns | Flaky test strategies, `retries`, `trace: 'on-first-retry'`, `test.abort()`, **Test data factory/builder pattern** (code จริง พร้อม TypeScript interface), `--forbid-only`, large suite organization, environment config |

**Coverage:** ~95%+ ของ Playwright official docs (verified จาก playwright.dev)

**⚠️ ต้อง verify ก่อนเขียนบทนั้น:**
- `testConfig.tsconfig` — ตรวจสอบ exact option name กับ playwright.dev
- `--only-changed` flag — ตรวจสอบว่ามีใน Playwright v1.51+ จริง
- `locator.filter()` options ที่รองรับ — ตรวจสอบว่า `{ visible: true }` มีจริงหรือต้องใช้ `.and(page.locator(':visible'))` แทน
- `devices['iPhone 15']` exact string — ตรวจสอบชื่อ device จาก `playwright.devices` ที่ export จาก `@playwright/test` ก่อนใช้ใน code example (อาจเป็น `'iPhone 15 Pro'` หรือชื่ออื่น)

---

## 4. เนื้อหาแต่ละบท — โครงสร้าง 8 ส่วน

ทุกบทมีครบตาม CLAUDE.md:
1. วัตถุประสงค์ (bullet list สิ่งที่ทำได้หลังอ่านจบ)
2. ทำไมต้องรู้? (Why — อธิบาย problem ก่อน solution)
3. Analogy + **breakdown points** ("⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า: ...")
4. เนื้อหาหลัก (verified จาก playwright.dev — มี source quote)
5. ตัวอย่าง 3 ระดับ (Beginner / Intermediate ในสถานการณ์ใหม่ / Advanced production-grade)
6. Common Mistakes ❌→✅ (พร้อม source inline)
7. สรุปบท + Retrieval Questions (2-3 ข้อ ให้ตอบก่อนดูเฉลย)
8. Pre-chapter Retrieval (บทที่ 2+ เท่านั้น)

**RF/Selenium comparison:** ทุกบทมี side-by-side เปรียบ RF/Selenium ↔ Playwright

**Exercise standards (ตาม CLAUDE.md):**
- ห้าม fill-in-the-blank ทุก level
- Intermediate: สถานการณ์ใหม่ — ห้าม copy context/ตัวเลขจากตัวอย่างในบท
- Advanced: Synthesis, diagnosis, design tradeoffs
- `exercises.md` ครอบคลุมทุก 18 บท (≥3 ข้อ/concept)

---

## 5. Demo App — `playwright-course-app/`

### Tech Stack
- **Runtime:** Node.js 20+ (LTS)
- **Framework:** Express.js
- **Frontend:** Vanilla HTML/CSS/JS (ไม่มี framework)
- **Database:** JSON file (`data/db.json`) — ไม่ต้อง setup DB
- **Start:** `npm install && npm start` → `http://localhost:3000`
- **Dependencies:** `express`, `jsonwebtoken`, `cors`, `ws`

### Pages (UI)

| หน้า | URL | Feature สำคัญ | ใช้สอนบท |
|------|-----|--------------|----------|
| Dashboard | `/` | Navbar: **"Logged in as: [username]"** (session badge — สำหรับ context isolation demo) + "Not logged in" เมื่อไม่มี session, links ทุกหน้า | 02, 03 |
| Login | `/login` | Form พร้อม label/placeholder/validation error message, JWT auth | 02, 08, 13 |
| Todo List | `/todos` | CRUD, **double-click to edit** inline, **todo count badge** `<span data-testid="todo-count">N items</span>`, drag-drop sort | 04, 05, 07, 09, 11 |
| Product Shop | `/shop` | Product cards (img + alt text เช่น `alt="Product: iPhone 15 Pro"`), filter by category, search, pagination (5 items/page) | 04, 10 |
| Shopping Cart | `/cart` | Add/remove items, quantity, total price | 08 |
| Checkout | `/checkout` | **Multi-step wizard 3 ขั้น** (Shipping → Payment → Confirm) มีปุ่ม Back/Next, multi-field form | 05, 08 |
| Components | `/components` | Checkbox, radio button, select dropdown, date picker, range slider, hover menu + tooltip, sortable table (sort + pagination), drag-drop list, accordion, tabs | 04, 05 |
| Advanced | `/advanced` | ปุ่ม "Open in new tab" (popup), iframe embed (`<iframe src="/todos">`), file upload form, ปุ่ม trigger alert/confirm/prompt, Shadow DOM web component (`<my-counter>`), ปุ่ม Download CSV | 14 |
| Visual | `/visual` | Theme switcher (light/dark) — ใช้ CSS class `data-theme`, stable layout สำหรับ screenshot baseline | 16 |
| Admin | `/admin` | Admin-only (redirect → `/login?unauthorized=1` ถ้า role ≠ admin), แสดง admin dashboard พร้อม stats | 03, 13 |

### REST API Endpoints

**Public (ไม่ต้อง auth) — เข้าถึงได้ตั้งแต่บทแรก:**

| Method | Endpoint | Description | Response | บท |
|--------|----------|-------------|----------|----|
| GET | `/api/todos` | List todos | `[{ id, text, completed, createdAt }]` | 07, 09, 11 |
| POST | `/api/todos` | Create todo | `{ id, text, completed, createdAt }` | 07, 15 |
| PATCH | `/api/todos/:id` | Update todo | `{ id, text, completed, createdAt }` | 15 |
| DELETE | `/api/todos/:id` | Delete todo | `{ success: true }` | 15 |
| POST | `/api/reset` | Reset todos กลับ clean state (ลบทุก todo) | `{ success: true }` | 07, 18 |
| GET | `/api/products` | Products list (`?search=&category=&page=&limit=`) | `{ data: [...], total, page, limit }` | 04, 15 |
| GET | `/api/products/:id` | Single product | `{ id, name, price, category, description, image }` | 15 |
| GET | `/api/slow?delay=ms` | Simulated delay (default 2000ms, max 10000ms) | `{ message: "slow response" }` | 06, 12 |
| GET | `/api/error` | Returns 500 | `{ error: "Internal Server Error" }` | 06, 12 |
| GET | `/api/flaky` | Random 50% fail: 200 หรือ 500 | `{ message: "ok" }` หรือ 500 | 18 |
| GET | `/api/export` | Download CSV (Content-Disposition: attachment) | CSV file | 14 |

**Protected (require `Authorization: Bearer <token>`) — สอนตั้งแต่บท 13:**

| Method | Endpoint | Description | Response | บท |
|--------|----------|-------------|----------|----|
| POST | `/api/auth/login` | Login | `{ token: "<jwt>" }` | 13 |
| GET | `/api/me` | Current user info | `{ id, username, role }` | 13, 15 |
| GET | `/api/admin` | Admin-only data | `{ stats: { ... } }` — 403 ถ้าไม่ใช่ admin | 13 |
| POST | `/api/orders` | Create order | `{ orderId, status, items }` | 15 |

**WebSocket:**

| Protocol | Endpoint | Behavior | บท |
|----------|----------|----------|----|
| WS | `ws://localhost:3000/ws` | Server broadcast ทุก 3 วินาที: `{ type: "ping", timestamp: "..." }` และ broadcast เมื่อ todo เปลี่ยน: `{ type: "todo_updated", data: { ... } }` | 12 |

### Users

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | admin |
| `testuser` | `test123` | user |

### `db.json` Initial Data

**โครงสร้างข้อมูลเริ่มต้น:**

- **todos:** `[]` (เริ่มว่าง — คนเรียนสร้างเองระหว่างทำ exercise)
- **products:** 10 รายการ ใน 3 categories:
  - `"Electronics"` — 4 items (เช่น iPhone 15 Pro, MacBook Air, AirPods Pro, iPad mini)
  - `"Books"` — 3 items (เช่น Clean Code, The Pragmatic Programmer, Design Patterns)
  - `"Clothing"` — 3 items (เช่น T-Shirt, Hoodie, Sneakers)
  - แต่ละ product มี fields: `id`, `name`, `price`, `category`, `description`, `image` (URL placeholder เช่น `https://placehold.co/300x200`)
  - ต้องมี ≥6 items เพื่อทดสอบ pagination (5 items/page — หน้า 1 มี 5, หน้า 2 มี 5)
- **orders:** `[]` (เริ่มว่าง)
- **users:** 2 records — `admin` (role: admin) และ `testuser` (role: user) — password เป็น plaintext ตั้งใจเพื่อความง่ายใน course (ไม่ใช่ production pattern)

### HTML Requirements (critical สำหรับบทที่ 4)

ทุก key element ต้องมี:
- ARIA roles ถูกต้อง (`button`, `heading`, `checkbox`, `listitem`, `navigation`, `main`, etc.)
- `<label for="inputId">` ผูกกับ `<input id="inputId">` ทุก form field
- `placeholder` attribute บน input ที่เหมาะสม
- `alt` text ที่สื่อความหมายบน `<img>` ทุกรูป (เช่น `alt="Product: iPhone 15 Pro"`)
- `data-testid` attribute บน element หลักทุกตัว (nav, forms, buttons, cards, badges)
- `title` attribute บน icon-only buttons

---

## 6. Output File Structure

```
docs/playwright-typescript/
├── 00-overview.md          ← ดูรายละเอียดด้านล่าง
├── 01-why-playwright.md
├── 02-setup-typescript.md
├── 03-architecture.md
├── 04-locators.md
├── 05-actions-assertions.md
├── 06-debugging.md
├── 07-fixtures.md
├── 08-page-object-model.md
├── 09-test-organization.md
├── 10-configuration-projects.md
├── 11-parallelism-sharding-reporting.md
├── 12-mocking-network-time-browser.md
├── 13-authentication-storage-state.md
├── 14-advanced-browser-emulation.md
├── 15-api-testing-hybrid.md
├── 16-visual-accessibility-testing.md
├── 17-cicd.md
├── 18-production-patterns.md
├── exercises.md            ← ≥3 ข้อ/concept ครอบคลุมทุก 18 บท + เฉลย
└── glossary.md             ← ทุกคำศัพท์ + SOURCE URL + คำอธิบายภาษาไทย

playwright-course-app/
├── package.json            ← deps: express, jsonwebtoken, cors, ws
├── server.js               ← Express server + all routes + WS server
├── middleware/
│   └── auth.js             ← JWT verification middleware
├── data/
│   └── db.json             ← JSON database (initial data ตามที่ระบุข้างบน)
├── public/
│   ├── index.html          ← Dashboard (session badge)
│   ├── login.html
│   ├── todos.html          ← double-click edit, count badge
│   ├── shop.html           ← filter, search, pagination
│   ├── cart.html
│   ├── checkout.html       ← Multi-step wizard 3 ขั้น
│   ├── components.html     ← Kitchen Sink
│   ├── advanced.html       ← popup, iframe, upload, dialog, shadow DOM, download
│   ├── visual.html         ← theme switcher
│   ├── admin.html          ← admin-only
│   ├── style.css
│   └── client.js           ← Shared frontend JS (fetch API calls, JWT storage)
└── README.md               ← วิธีรัน, users, pages list, endpoints list
```

### `00-overview.md` ต้องมี:
- **Prerequisites:**
  - Node.js 20+ และ npm ใช้เป็น (รัน `npm install`, `npm start` ได้)
  - JavaScript/TypeScript เบื้องต้น (รู้จัก variable, function, async/await concept)
  - HTML เบื้องต้น (รู้จัก element, attribute, id, class)
  - Robot Framework หรือ Selenium พื้นฐาน (เป็นที่มาของ analogy ใน course)
- **เวลาเรียนโดยประมาณ** (แต่ละบทและรวม)
- **Table of Contents** พร้อม link ทุกบท
- **วิธีรัน Demo App** (quick start)
- **วิธีสร้าง test project** (quick start: `npm init playwright@latest`)

---

## 7. Source Notes Requirements

ก่อนเขียนแต่ละบท ต้อง verify กับ playwright.dev และบันทึกใน `docs/playwright-typescript-source-notes.md`:

```
SOURCE: https://playwright.dev/docs/[page]
VERSION: Playwright vX.XX.X (actual version ที่ติดตั้งและทดสอบจริง — ต้องเป็น v1.50.0+)
CONCEPT: [ชื่อ concept]
QUOTE: "[ข้อความจริงจาก docs — copy โดยตรง ไม่ paraphrase]"
```

Code examples ทุกตัวต้อง:
1. รันได้จริง (test ด้วย Bash ก่อนใส่เอกสาร)
2. ระบุ version จริงที่ทดสอบ: `// tested: Playwright v1.XX.X, Node.js 20.X.X`
3. แสดง output จริงที่ได้จากการรัน

---

## 8. Key Design Decisions

1. **TypeScript only** — ไม่มี Python/JS เปรียบเทียบ เพื่อให้เนื้อหา focused
2. **คนเรียนสร้าง test project เอง** — `npm init playwright@latest` ตั้งแต่บทที่ 2
3. **Markdown format** — เหมาะกับ code-heavy content, syntax highlighting ดี
4. **RF/Selenium comparison ทุกบท** — ช่วย mindset shift สำหรับผู้เรียนที่มี RF/Selenium background
5. **Demo App public endpoints ไม่ต้อง auth** — ไม่ block บทเรียนก่อนบทที่ 13 (auth)
6. **Auth เฉพาะ `/api/me`, `/api/admin`, `/api/orders`** — sequencing ถูกต้องตาม learning path
7. **Playwright v1.50+ minimum** — รองรับ Clock API (v1.45), routeWebSocket (v1.48), toMatchAriaSnapshot (v1.49), --only-changed (v1.51)
8. **`defineConfig()` ไม่ใช้ generic โดยไม่จำเป็น** — standard TypeScript pattern ถูกต้อง
9. **ไม่มี `failOnFlakyTests` config ใน Playwright** — ใช้ `retries: process.env.CI ? 2 : 0` + `forbidOnly: true` แทน
10. **`test.describe.serial()` ไม่ใช่ `test.serial()`** — API ถูกต้อง
11. **Ch12 ชื่อ "Mocking: Network, Time & Browser APIs"** — สะท้อนเนื้อหาจริงที่ไม่ใช่แค่ network
12. **JWT response format: `{ token: "<jwt>" }`** — consistent ตลอด course
13. **`page.addInitScript()` vs `context.addInitScript()`** — ต่างกันใน scope สอนทั้งคู่ใน Ch12
14. **`extraHTTPHeaders` = context-level (Ch15)** — ต่างจาก per-route headers ผ่าน `route.continue()` (Ch12)

---

## 9. Quality Targets (ตาม CLAUDE.md Rubric)

- Completeness: 95+ / 100
- Accuracy: 98+ / 100 (verify ทุก concept กับ playwright.dev ก่อนเขียน)
- Learning: 95+ / 100
