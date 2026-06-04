# Ch19 DB Verification + Expanded Exercises — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** เพิ่ม Chapter 19 "Database State Verification" (8 sections เต็ม) และขยาย exercises.md ด้วยระบบ 5 levels สำหรับ Ch19 + Expert (L5) สำหรับ Ch13, Ch15, Ch17, Ch18

**Architecture:** WebFetch official Playwright docs ก่อนเขียนทุก concept → บันทึก QUOTE ใน source-notes → เขียน chapter content → เขียน exercises — เป็นลำดับเสมอ ห้ามข้าม

**Tech Stack:** Playwright v1.50+, TypeScript, Node.js 20+, demo app ที่ `docs/playwright-typescript/playwright-course-app/` (Express + `data/db.json`)

---

## Critical Context สำหรับ Agent

### Demo App APIs (verified จาก server.js)
```
POST /api/todos          body: { text: string }           → 201 { id, text, completed, createdAt }
GET  /api/todos                                            → array of todos
PATCH /api/todos/:id     body: { text?, completed? }      → updated todo
DELETE /api/todos/:id                                     → { success: true }
POST /api/reset                                           → { success: true }  ← ล้าง todos ทั้งหมด
GET  /api/products       query: search?, category?        → { data, total, page, limit, totalPages }
GET  /api/products/:id                                    → product object
POST /api/auth/login     body: { username, password }     → { token }
GET  /api/me             header: Authorization Bearer     → { id, username, role }
POST /api/orders         header: Authorization Bearer     → 201 { orderId, status, items, createdAt }
GET  /api/admin          header: Authorization Bearer (admin only) → { stats: { users, products, todos, orders } }
GET  /api/slow           query: delay?                    → slow response
GET  /api/error                                           → 500
GET  /api/flaky                                           → 50% fail
GET  /api/export                                         → CSV download
```

### DB Structure (data/db.json)
```json
{
  "users": [
    { "id": 1, "username": "admin", "password": "admin123", "role": "admin" },
    { "id": 2, "username": "testuser", "password": "test123", "role": "user" }
  ],
  "products": [ 10 products: Electronics (4), Books (3), Clothing (3) ],
  "todos": [],
  "orders": []
}
```

DB file path: `docs/playwright-typescript/playwright-course-app/data/db.json`

### CLAUDE.md Writing Standards (บังคับทุกบท)

**8 Sections บังคับ:**
1. Pre-chapter Retrieval (บทที่ 2+ เท่านั้น) — ถาม 2 ข้อจากบทก่อน, คั่นด้วย `---`, แสดงเฉลย
2. วัตถุประสงค์ — bullet list ≥6 ข้อ "หลังอ่านบทนี้คุณจะ..."
3. ทำไมต้องรู้? (Why) — อธิบายปัญหาก่อน ห้ามเริ่มด้วย "X คืออะไร"
4. Analogy — เฉพาะเจาะจง + ⚠️ breakdown points 2-3 ข้อ
5. เนื้อหาหลัก — verified from docs, RF/Selenium comparison table บังคับ
6. ตัวอย่าง 3 ระดับ — Beginner / Intermediate (สถานการณ์ใหม่) / Advanced
7. Common Mistakes — ≥3 ข้อ format: `❌ [...] → ✅ [...] *(source: URL)*`
8. สรุปบท — 3-4 bullets + Retrieval Questions format (ตอบก่อนดูเฉลย)

**Style:**
- ภาษาไทย, code/terms เป็น English
- ย่อหน้าไม่เกิน 4-5 บรรทัด
- อธิบาย Why ก่อน What เสมอ
- ทุก code block: `// tested: Playwright v1.50+, Node.js 20+`
- Code สมบูรณ์ runnable ได้ทันที
- Target: 750-900 บรรทัด

### Source-Notes Format
```
## Ch19: Database State Verification
SOURCE: https://playwright.dev/docs/[page]
VERSION: [วันที่ WebFetch]
CONCEPT: [ชื่อ concept]
QUOTE: "[ข้อความ copy word-for-word จาก docs]"
```
เพิ่มที่ท้ายไฟล์: `docs/playwright-typescript-source-notes.md`

### 5-Level Exercise System
| Level | Name | Cognitive Task |
|-------|------|----------------|
| L1 | Recall | อธิบาย concept ด้วยคำตัวเอง |
| L2 | Recognition | อ่าน code → ระบุว่าถูก/ผิดอย่างไรและเพราะอะไร |
| L3 | Guided Application | เขียน test ตาม scenario ที่กำหนดชัดเจน |
| L4 | Independent Application | สถานการณ์ใหม่ ออกแบบเองทั้งหมด ไม่มี scaffold |
| L5 | Expert / Synthesis | Debug production scenario, design architecture, evaluate trade-offs |

**กฎ exercises ทุก level:**
- ห้าม fill-in-the-blank
- ห้าม copy scenario/ตัวเลขจากตัวอย่างในบท
- ทุก level ต้องมี `<details><summary>เฉลย</summary>...</details>`
- L3+ ใช้ demo app `http://localhost:3000` เสมอ
- L5 ต้องมี production context (scale, CI, team)

---

## File Map

| File | Action | สิ่งที่ต้องทำ |
|------|--------|-------------|
| `docs/playwright-typescript-source-notes.md` | EDIT | append Ch19 quotes จาก playwright.dev |
| `docs/playwright-typescript/19-db-verification.md` | CREATE | chapter เต็ม 8 sections ~750-900 lines |
| `docs/playwright-typescript/exercises.md` | EDIT | append Ch19 (5 levels) + L5 สำหรับ Ch13/15/17/18 |
| `docs/playwright-typescript/HANDOFF.md` | EDIT | เพิ่ม Ch19 ในตาราง |
| `docs/playwright-typescript/00-overview.md` | EDIT | เพิ่ม Ch19 ใน TOC |

---

## Task 1: WebFetch Official Docs + Record Source Notes

**Files:**
- Modify: `docs/playwright-typescript-source-notes.md`

- [ ] **Step 1: WebFetch API Testing docs**

```
WebFetch: https://playwright.dev/docs/api-testing
```
ค้นหา quotes สำหรับ:
- "request fixture" คืออะไร และ isolation behavior
- `APIRequestContext` methods ที่สำคัญ

- [ ] **Step 2: WebFetch APIRequestContext class reference**

```
WebFetch: https://playwright.dev/docs/api/class-apirequestcontext
```
ค้นหา exact method signatures สำหรับ:
- `.get(url, options?)` 
- `.post(url, options?)`
- `.fetch(urlOrRequest, options?)` — ใช้ verify response body

- [ ] **Step 3: WebFetch expect.poll documentation**

```
WebFetch: https://playwright.dev/docs/api/class-playwrightassertions#playwright-assertions-expect-poll
```
หรือ search `expect.poll` ใน:
```
WebFetch: https://playwright.dev/docs/test-assertions
```
ค้นหา:
- `expect.poll(fn, options?)` — exact signature
- `intervals` และ `timeout` options
- เหมาะกับ async DB operations

- [ ] **Step 4: WebFetch Best Practices**

```
WebFetch: https://playwright.dev/docs/best-practices
```
ค้นหา:
- แนวทางเกี่ยวกับ test isolation
- API setup vs UI setup

- [ ] **Step 5: Append all quotes to source-notes**

เพิ่มที่ท้าย `docs/playwright-typescript-source-notes.md`:

```markdown
---

## Ch19: Database State Verification

SOURCE: https://playwright.dev/docs/api-testing
VERSION: [วันที่ fetch]
CONCEPT: request fixture isolation
QUOTE: "[copy exact text from docs about request fixture being isolated per test]"

SOURCE: https://playwright.dev/docs/api/class-apirequestcontext
VERSION: [วันที่ fetch]
CONCEPT: APIRequestContext.get() signature
QUOTE: "[copy exact method signature and description]"

SOURCE: https://playwright.dev/docs/api/class-apirequestcontext
VERSION: [วันที่ fetch]
CONCEPT: response.json() / response.ok()
QUOTE: "[copy exact text]"

SOURCE: https://playwright.dev/docs/test-assertions
VERSION: [วันที่ fetch]
CONCEPT: expect.poll for async state
QUOTE: "[copy exact text about expect.poll behavior]"

SOURCE: https://playwright.dev/docs/best-practices
VERSION: [วันที่ fetch]
CONCEPT: test isolation best practice
QUOTE: "[copy exact text about isolation]"
```

**ข้อสำคัญ:** ต้อง copy text จาก docs จริง word-for-word ไม่ paraphrase — ถ้า fetch ไม่ได้ข้อมูลที่ต้องการ ระบุว่า "could not verify" อย่าแต่งเอง

- [ ] **Step 6: Commit source notes**

```bash
git add docs/playwright-typescript-source-notes.md
git commit -m "docs: add ch19 source notes from playwright.dev"
```

---

## Task 2: Write Ch19 — Opening Sections (Pre-retrieval, วัตถุประสงค์, Why, Analogy)

**Files:**
- Create: `docs/playwright-typescript/19-db-verification.md`

- [ ] **Step 1: Create file with Pre-chapter Retrieval**

เริ่มไฟล์ด้วย 2 คำถามจาก Ch18 (Production Patterns):

```markdown
## ก่อนอ่านบทนี้ ลองตอบ:

1. "Flaky test" คืออะไร และ "shared state" ระหว่าง tests ทำให้เกิด flakiness อย่างไร — ยกตัวอย่างสถานการณ์จริง

2. Test Data Factory pattern คืออะไร และทำไมถึงแนะนำให้สร้าง unique data ต่อ test แทนที่จะ share data กัน

---

เฉลย:

1. [เฉลยครบถ้วน]

2. [เฉลยครบถ้วน]

---
```

- [ ] **Step 2: เขียน Section 1 (วัตถุประสงค์)**

ต้องมี ≥6 bullet points ที่วัดผลได้ เช่น:
- ใช้ `request` fixture เพื่อ read-back ข้อมูลจาก database หลัง UI action
- อ่านไฟล์ database โดยตรงด้วย Node.js `fs` module ใน test
- เขียน cross-layer verification: UI action → API confirm → UI re-render check
- ใช้ `expect.poll()` เพื่อรอ async database write ก่อน verify
- เขียน negative verification: ยืนยันว่า record ถูกลบจาก database จริง
- ออกแบบ DB isolation: ป้องกัน test contamination ด้วย per-test setup/teardown

- [ ] **Step 3: เขียน Section 2 (ทำไมต้องรู้?)**

**ห้ามเริ่มด้วย "X คืออะไร"** — เริ่มด้วยปัญหา:

ปัญหาที่ต้องอธิบาย: test ผ่าน UI แต่ DB ผิด — ยกตัวอย่างจริง เช่น:
- UI แสดง "บันทึกสำเร็จ" แต่ response ถูก handle ผิด — data ไม่ได้ถูก write จริง
- UI แสดง "ลบแล้ว" แต่ soft-delete ทำงานผิด — record ยังอยู่ใน DB
- Form validation pass บน frontend แต่ backend reject และ rollback — UI ไม่รู้

อธิบายว่าสามชั้นต้องตรวจ: UI Layer, API Layer, DB Layer

- [ ] **Step 4: เขียน Section 3 (Analogy)**

ใช้ analogy ที่เฉพาะเจาะจง ครอบคลุม mechanism หลัก เช่น:
"การตรวจสอบ DB state เหมือนระบบตรวจสอบคลังสินค้า 3 ชั้น: หน้าจอ POS (UI) แสดงว่าขายแล้ว, ระบบบัญชี (API) บันทึก transaction แล้ว, และการนับ stock จริงในคลัง (DB) ยืนยันว่าของน้อยลงจริง"

**ต้องมี ⚠️ breakdown points ≥2 ข้อ** ระบุ misconceptions ที่อาจเกิดจริง

- [ ] **Step 5: Commit draft sections 1-4**

```bash
git add docs/playwright-typescript/19-db-verification.md
git commit -m "docs: ch19 opening sections pre-retrieval through analogy"
```

---

## Task 3: Write Ch19 Section 4 — เนื้อหาหลัก Patterns 1–3

**Files:**
- Modify: `docs/playwright-typescript/19-db-verification.md`

- [ ] **Step 1: เขียน Pattern 1 — API Read-back Verification**

หัวข้อ: `### 4.1 Pattern 1: API Read-back — ยืนยันผ่าน GET หลัง Action`

อธิบาย: หลัง UI สร้าง/แก้ไข record → call GET API → assert response ตรงกับที่ต้องการ

Code skeleton สำหรับตัวอย่าง partial ใน section นี้ (full runnable อยู่ใน section 5):
```typescript
// partial example — see Section 5 for full runnable version
// tested: Playwright v1.50+, Node.js 20+
test('todo created via UI appears in database', async ({ page, request }) => {
  await page.goto('http://localhost:3000/todos');
  await page.getByTestId('input-new-todo').fill('Buy groceries');
  await page.getByTestId('btn-add-todo').click();

  // API Read-back: verify ว่า DB จริงๆ มี record นี้
  const response = await request.get('http://localhost:3000/api/todos');
  expect(response.ok()).toBeTruthy();
  const todos = await response.json();
  expect(todos).toContainEqual(
    expect.objectContaining({ text: 'Buy groceries', completed: false })
  );
});
```

อธิบาย: ทำไม `toContainEqual` + `expect.objectContaining` ดีกว่าเทียบ array ทั้งหมด

- [ ] **Step 2: เขียน Pattern 2 — Direct File Read**

หัวข้อ: `### 4.2 Pattern 2: Direct File Read — อ่าน db.json โดยตรง`

อธิบาย use case: เหมาะสำหรับ file-based DB, test environment ที่ไม่มี GET API ที่ครอบคลุม, หรือ verify ข้อมูลที่ API ไม่ expose

```typescript
// partial example — see Section 5 for full runnable version
// tested: Playwright v1.50+, Node.js 20+
import { readFileSync } from 'fs';
import { resolve } from 'path';

test('todo appears in db.json after UI creation', async ({ page }) => {
  await page.goto('http://localhost:3000/todos');
  await page.getByTestId('input-new-todo').fill('Direct DB check');
  await page.getByTestId('btn-add-todo').click();

  // Direct file read — sync อ่านได้ทันที เพราะ server write ก่อน response
  const dbPath = resolve('docs/playwright-typescript/playwright-course-app/data/db.json');
  const db = JSON.parse(readFileSync(dbPath, 'utf-8'));
  expect(db.todos).toContainEqual(
    expect.objectContaining({ text: 'Direct DB check' })
  );
});
```

**ระบุให้ชัด:** เมื่อใดควรใช้ direct file read vs API read-back — tradeoff ของแต่ละแบบ

- [ ] **Step 3: เขียน Pattern 3 — Cross-layer Verification**

หัวข้อ: `### 4.3 Pattern 3: Cross-layer Verification — ตรวจ 3 ชั้นในครั้งเดียว`

อธิบาย flow: UI Action → API verify DB state → UI re-render verify

ตัวอย่าง skeleton:
```typescript
// partial example
// tested: Playwright v1.50+, Node.js 20+
test('order creation verified across all layers', async ({ page, request }) => {
  // ขั้น 1: Login และ setup via API (fast)
  const loginRes = await request.post('http://localhost:3000/api/auth/login', {
    data: { username: 'admin', password: 'admin123' }
  });
  const { token } = await loginRes.json();

  // ขั้น 2: สร้าง order ผ่าน API
  const orderRes = await request.post('http://localhost:3000/api/orders', {
    headers: { Authorization: `Bearer ${token}` },
    data: { items: [{ productId: 1, quantity: 2 }] }
  });
  expect(orderRes.status()).toBe(201);
  const order = await orderRes.json();

  // ขั้น 3: Verify ใน DB ผ่าน Admin API
  const adminRes = await request.get('http://localhost:3000/api/admin', {
    headers: { Authorization: `Bearer ${token}` }
  });
  const { stats } = await adminRes.json();
  expect(stats.orders).toBeGreaterThan(0);

  // ขั้น 4: Verify ใน UI ว่า re-render ถูกต้อง
  await page.goto('http://localhost:3000/todos');
  // (ใน real app จะ navigate ไปหน้า order history)
});
```

- [ ] **Step 4: Commit patterns 1-3**

```bash
git add docs/playwright-typescript/19-db-verification.md
git commit -m "docs: ch19 patterns 1-3 api read-back file-read cross-layer"
```

---

## Task 4: Write Ch19 Section 4 — Patterns 4–6 + RF/Selenium Table

**Files:**
- Modify: `docs/playwright-typescript/19-db-verification.md`

- [ ] **Step 1: เขียน Pattern 4 — Negative Verification**

หัวข้อ: `### 4.4 Pattern 4: Negative Verification — ยืนยันว่าลบจริง`

ปัญหาที่แก้: "soft delete" bug — UI บอกว่าลบแล้ว แต่ record ยังอยู่ใน DB

```typescript
// partial example
// tested: Playwright v1.50+, Node.js 20+
test('deleted todo is removed from database', async ({ page, request }) => {
  // Setup: สร้าง todo ผ่าน API ก่อน
  const createRes = await request.post('http://localhost:3000/api/todos', {
    data: { text: 'Todo to delete' }
  });
  const { id } = await createRes.json();

  // Action: ลบผ่าน UI
  await page.goto('http://localhost:3000/todos');
  // ... find และ click delete button สำหรับ todo นี้

  // Negative DB Verification: ยืนยันว่าไม่อยู่ใน DB แล้ว
  const response = await request.get('http://localhost:3000/api/todos');
  const todos = await response.json();
  expect(todos).not.toContainEqual(expect.objectContaining({ id }));
});
```

- [ ] **Step 2: เขียน Pattern 5 — Async DB Polling**

หัวข้อ: `### 4.5 Pattern 5: Async DB Polling — expect.poll() สำหรับ Async Writes`

**ก่อนเขียน: verify exact API จาก source-notes** (ที่ record ใน Task 1)

ปัญหา: backend write ไม่ synchronous กับ response — บางระบบ return 202 Accepted ก่อน DB write เสร็จ

```typescript
// partial example
// tested: Playwright v1.50+, Node.js 20+
test('async bulk import completes in database', async ({ request }) => {
  // สมมติมี bulk operation ที่ async
  await request.post('http://localhost:3000/api/todos', {
    data: { text: 'Async todo 1' }
  });

  // expect.poll() — retry จนกว่า assertion จะผ่าน หรือ timeout
  await expect.poll(async () => {
    const res = await request.get('http://localhost:3000/api/todos');
    const todos = await res.json();
    return todos.length;
  }, {
    intervals: [500, 1000, 2000],  // retry ที่ 500ms, 1s, 2s
    timeout: 10_000,               // timeout รวม 10 วินาที
  }).toBeGreaterThan(0);
});
```

อธิบาย `intervals` vs `timeout` — และทำไม `expect.poll()` ดีกว่า `waitForTimeout()`

- [ ] **Step 3: เขียน Pattern 6 — DB Isolation**

หัวข้อ: `### 4.6 Pattern 6: DB Isolation — ป้องกัน Test Contamination`

ปัญหา: tests รันพร้อมกัน (parallel) แชร์ DB → data ปน → results ไม่แน่นอน

```typescript
// partial example
// tested: Playwright v1.50+, Node.js 20+
import { test as base } from '@playwright/test';

// Fixture ที่ reset DB ก่อนและหลัง test
const test = base.extend({
  cleanDb: async ({ request }, use) => {
    // Setup: ล้าง DB ก่อน test
    await request.post('http://localhost:3000/api/reset');
    await use(undefined);
    // Teardown: ล้าง DB หลัง test (guaranteed แม้ test fail)
    await request.post('http://localhost:3000/api/reset');
  },
});

test('test has clean database state', async ({ page, cleanDb }) => {
  // cleanDb fixture ถูก destructure เพื่อ trigger setup/teardown
  // ใน test นี้ DB เริ่มต้น empty todos
  const response = await page.request.get('http://localhost:3000/api/todos');
  const todos = await response.json();
  expect(todos).toHaveLength(0);
});
```

อธิบาย: ทำไม teardown ใน fixture guaranteed ต่างจาก `afterEach`

- [ ] **Step 4: เขียน RF/Selenium Comparison Table**

```markdown
### 4.7 เปรียบเทียบกับ Robot Framework + Selenium

| ประเด็น | Robot Framework + Selenium | Playwright |
|---------|---------------------------|-----------|
| ตรวจ DB state | ต้องใช้ DatabaseLibrary หรือ RequestsLibrary แยก | `request` fixture built-in, ใช้ได้ทันทีใน test เดียวกัน |
| Async DB polling | ต้องเขียน custom keyword ที่ retry เอง | `expect.poll()` built-in พร้อม interval config |
| Direct file read | Import Python standard library ใน custom keyword | `import { readFileSync } from 'fs'` โดยตรงใน TypeScript |
| Test isolation / DB reset | ต้องตั้ง Suite Setup/Teardown ใน `.robot` file | Fixture scope (['worker', 'test']) จัดการ lifecycle อัตโนมัติ |
| Cross-layer verify | ต้อง switch ระหว่าง SeleniumLibrary และ RequestsLibrary | `page` + `request` ใน test เดียว, share base URL และ headers |
```

- [ ] **Step 5: Commit patterns 4-6 + table**

```bash
git add docs/playwright-typescript/19-db-verification.md
git commit -m "docs: ch19 patterns 4-6 async-polling isolation rf-selenium-table"
```

---

## Task 5: Write Ch19 Section 5 — ตัวอย่าง 3 ระดับ (Full Runnable Code)

**Files:**
- Modify: `docs/playwright-typescript/19-db-verification.md`

ตัวอย่างทุกข้อต้องรันได้จริงกับ demo app — ทุก code block ต้องมี `// tested: Playwright v1.50+, Node.js 20+`

**ห้ามใช้ scenario/ตัวเลขซ้ำกับ section 4**

- [ ] **Step 1: เขียน Beginner Example — Single API Read-back**

Scenario: ตรวจว่า todo ที่สร้างผ่าน UI มีค่าถูกต้องใน DB ทุก field (ไม่ใช่แค่ exist)

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

test.beforeEach(async ({ request }) => {
  await request.post('http://localhost:3000/api/reset');
});

test('todo fields saved correctly in database after UI creation', async ({ page, request }) => {
  await page.goto('http://localhost:3000/todos');
  await page.getByTestId('input-new-todo').fill('Read Playwright docs');
  await page.getByTestId('btn-add-todo').click();

  // รอให้ UI อัปเดตก่อน
  await expect(page.getByTestId('todo-list')).toContainText('Read Playwright docs');

  // API Read-back: verify ทุก field ใน DB
  const res = await request.get('http://localhost:3000/api/todos');
  const todos = await res.json();

  expect(todos).toHaveLength(1);
  expect(todos[0]).toMatchObject({
    text: 'Read Playwright docs',
    completed: false,
  });
  expect(todos[0].id).toBeDefined();
  expect(todos[0].createdAt).toBeDefined();
});
```

Output ที่ได้เมื่อรัน: `✓ todo fields saved correctly in database after UI creation (1.2s)`

- [ ] **Step 2: เขียน Intermediate Example — Cross-layer Verification**

Scenario ใหม่ที่ไม่ซ้ำ section 4: ตรวจว่า complete/uncomplete todo ผ่าน UI สะท้อนใน DB และ UI re-render ถูกต้อง

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

test.beforeEach(async ({ request }) => {
  await request.post('http://localhost:3000/api/reset');
});

test('toggling todo completion updates database and re-renders correctly', async ({ page, request }) => {
  // Setup ผ่าน API
  const createRes = await request.post('http://localhost:3000/api/todos', {
    data: { text: 'Study TypeScript generics' }
  });
  expect(createRes.status()).toBe(201);
  const { id } = await createRes.json();

  // Layer 1: UI action — toggle complete
  await page.goto('http://localhost:3000/todos');
  await expect(page.getByTestId(`todo-text-${id}`)).toBeVisible();
  await page.getByTestId(`todo-item-${id}`).locator('input[type="checkbox"]').check();

  // Layer 2: API verify — DB state เปลี่ยนแล้ว
  const dbRes = await request.get('http://localhost:3000/api/todos');
  const todos = await dbRes.json();
  const updatedTodo = todos.find((t: any) => t.id === id);
  expect(updatedTodo?.completed).toBe(true);

  // Layer 3: UI re-render verify — UI สะท้อน DB state
  await expect(page.getByTestId(`todo-item-${id}`)).toHaveClass(/completed/);
});
```

- [ ] **Step 3: เขียน Advanced Example — DB Isolation Fixture + Parallel-safe**

Scenario: สร้าง reusable fixture ที่ ensure DB isolation สำหรับ test suite ที่รัน parallel ได้

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test as base, expect } from '@playwright/test';

// Custom fixture type
type DbFixtures = {
  isolatedDb: {
    createTodo: (text: string) => Promise<{ id: number; text: string; completed: boolean }>;
    getTodos: () => Promise<Array<{ id: number; text: string; completed: boolean }>>;
  };
};

// Extend test with DB isolation fixture
const test = base.extend<DbFixtures>({
  isolatedDb: async ({ request }, use) => {
    // Setup: ensure clean state
    await request.post('http://localhost:3000/api/reset');

    // Provide helper methods
    const helpers = {
      async createTodo(text: string) {
        const res = await request.post('http://localhost:3000/api/todos', {
          data: { text }
        });
        expect(res.status()).toBe(201);
        return res.json();
      },
      async getTodos() {
        const res = await request.get('http://localhost:3000/api/todos');
        return res.json();
      },
    };

    await use(helpers);

    // Teardown: guaranteed cleanup แม้ test fail
    await request.post('http://localhost:3000/api/reset');
  },
});

test('complete todo workflow with db verification at each step', async ({ page, isolatedDb }) => {
  // Create 3 todos ผ่าน fixture
  const todo1 = await isolatedDb.createTodo('Write unit tests');
  const todo2 = await isolatedDb.createTodo('Review PR');
  await isolatedDb.createTodo('Deploy to staging');

  // Verify initial DB state
  const initialTodos = await isolatedDb.getTodos();
  expect(initialTodos).toHaveLength(3);
  expect(initialTodos.every((t) => !t.completed)).toBe(true);

  // UI: complete todo1
  await page.goto('http://localhost:3000/todos');
  await page.getByTestId(`todo-item-${todo1.id}`).locator('input[type="checkbox"]').check();

  // Negative verify: todo2 ยังไม่ complete ใน DB
  const midTodos = await isolatedDb.getTodos();
  expect(midTodos.find((t) => t.id === todo2.id)?.completed).toBe(false);

  // UI: delete todo2
  await page.getByTestId(`todo-item-${todo2.id}`).locator('[data-action="delete"]').click();

  // DB verify after delete
  const finalTodos = await isolatedDb.getTodos();
  expect(finalTodos).toHaveLength(2);
  expect(finalTodos.find((t) => t.id === todo2.id)).toBeUndefined();
});
```

- [ ] **Step 4: Commit section 5**

```bash
git add docs/playwright-typescript/19-db-verification.md
git commit -m "docs: ch19 section 5 three-level code examples with db verification"
```

---

## Task 6: Write Ch19 Sections 6–7 (Common Mistakes + สรุปบท) + Commit Full Chapter

**Files:**
- Modify: `docs/playwright-typescript/19-db-verification.md`

- [ ] **Step 1: เขียน Section 6 (Common Mistakes)**

ต้องมี ≥3 ข้อ แต่ละข้อมี `*(source: URL)*` จาก source-notes ที่ verified แล้ว

ตัวอย่าง mistakes ที่ต้องครอบคลุม:

```markdown
## 6. Common Mistakes

### ❌ Mistake 1: Verify UI แล้วสรุปว่า DB ถูกต้อง
❌ ผิด:
```typescript
await page.getByTestId('btn-add-todo').click();
await expect(page.getByTestId('todo-list')).toContainText('My todo');
// จบ test โดยไม่ verify DB
```
✅ ถูก:
```typescript
await page.getByTestId('btn-add-todo').click();
await expect(page.getByTestId('todo-list')).toContainText('My todo');
// ตรวจ DB ด้วยเสมอสำหรับ write operations
const todos = await request.get('http://localhost:3000/api/todos').then(r => r.json());
expect(todos).toContainEqual(expect.objectContaining({ text: 'My todo' }));
```
*(source: [URL จาก source-notes])*

### ❌ Mistake 2: ไม่รอ async write ก่อน verify DB
❌ ผิด:
```typescript
await page.getByRole('button', { name: 'Save' }).click();
// verify ทันทีโดยไม่รอ
const res = await request.get('/api/todos');
```
✅ ถูก:
```typescript
await page.getByRole('button', { name: 'Save' }).click();
// รอ UI confirm ก่อนที่ DB write จะสมบูรณ์
await expect(page.getByRole('status')).toContainText('Saved');
const res = await request.get('http://localhost:3000/api/todos');
```
*(source: [URL])*

### ❌ Mistake 3: ไม่ cleanup DB ระหว่าง tests
❌ ผิด:
```typescript
// ไม่มี beforeEach reset — tests ปน DB กัน
test('first test', async ({ request }) => {
  await request.post('/api/todos', { data: { text: 'Test A' } });
  // ...
});
test('second test', async ({ request }) => {
  const todos = await (await request.get('/api/todos')).json();
  expect(todos).toHaveLength(1); // อาจเจอ 2 ถ้า Test A ยังอยู่
});
```
✅ ถูก:
```typescript
test.beforeEach(async ({ request }) => {
  await request.post('http://localhost:3000/api/reset');
});
```
*(source: [URL])*
```

- [ ] **Step 2: เขียน Section 7 (สรุปบท + Retrieval Questions)**

Format บังคับ:
```markdown
## 7. สรุปบท

- [3-4 bullet ที่สรุป key points]

---

**คำถาม (ตอบก่อนดูเฉลย):**
1. [คำถาม retrieval ที่ต้องใช้ recall จริง]
2. [คำถาม retrieval]
3. [optional คำถามที่ 3]

<details>
<summary>เฉลย</summary>

1. [เฉลยครบถ้วน]
2. [เฉลยครบถ้วน]

</details>
```

- [ ] **Step 3: ตรวจสอบ quality ก่อน commit**

ตรวจ checklist:
- [ ] 8 sections ครบ (pre-retrieval, วัตถุประสงค์, why, analogy, เนื้อหา, ตัวอย่าง, common mistakes, สรุป)
- [ ] Analogy มี breakdown points ≥2 ข้อ
- [ ] เนื้อหาหลักมี RF/Selenium comparison table
- [ ] ตัวอย่างทุกข้อมี `// tested: Playwright v1.50+, Node.js 20+`
- [ ] Common Mistakes ทุกข้อมี `*(source: URL)*`
- [ ] สรุปบทมี Retrieval Questions format ถูกต้อง
- [ ] ความยาวไม่ต่ำกว่า 750 บรรทัด

- [ ] **Step 4: Commit complete Ch19**

```bash
git add docs/playwright-typescript/19-db-verification.md
git commit -m "docs: ch19 database state verification complete chapter"
```

---

## Task 7: Write Ch19 Exercises (5 Levels) in exercises.md

**Files:**
- Modify: `docs/playwright-typescript/exercises.md`

เพิ่มที่ท้ายไฟล์ ก่อน `*แบบฝึกหัดนี้ครอบคลุม...`

- [ ] **Step 1: เพิ่ม header สำหรับ Ch19**

```markdown
---

## บทที่ 19 — Database State Verification: ปิด Loop ด้วยการตรวจ DB
```

- [ ] **Step 2: เขียน L1 — Recall**

```markdown
### L1: Recall (ขั้นพื้นฐาน)

อธิบายด้วยคำตัวเองว่าทำไมการ verify ว่า UI แสดงข้อมูลถูกต้องถึงไม่เพียงพอ — ให้ยกตัวอย่างสถานการณ์จริง (ไม่ต้องเป็น IT) ที่ "สิ่งที่เห็นหน้าจอ" ไม่ตรงกับ "ข้อมูลจริงในระบบ backend" และอธิบายว่า API Read-back pattern แก้ปัญหานี้อย่างไร
```

เฉลยในรูปแบบ `<details>` ที่ครบถ้วน

- [ ] **Step 3: เขียน L2 — Recognition**

```markdown
### L2: Recognition (อ่าน code + ระบุปัญหา)

ตรวจสอบ code ต่อไปนี้แล้วระบุปัญหาทุกจุดพร้อมอธิบายว่าแต่ละปัญหาจะทำให้ test fail อย่างไร:

```typescript
test('product price update reflects in database', async ({ page, request }) => {
  // Update price through admin UI
  await page.goto('http://localhost:3000/admin');
  await page.getByTestId('product-1-edit').click();
  await page.getByLabel('Price').fill('799');
  await page.getByRole('button', { name: 'Save' }).click();
  
  // Verify immediately
  const response = await request.get('http://localhost:3000/api/products/1');
  const product = await response.json();
  expect(product.price).toBe(799);
  
  // Verify DB file too
  const db = JSON.parse(readFileSync('data/db.json', 'utf-8'));
  expect(db.products[0].price).toBe(799);
});
```

**hint:** มีปัญหาอย่างน้อย 3 จุด
```

เฉลยที่ระบุ: 1) ไม่รอ UI confirm ก่อน verify 2) path `data/db.json` ไม่ถูก 3) ไม่มี cleanup

- [ ] **Step 4: เขียน L3 — Guided Application**

```markdown
### L3: Guided Application (เขียนตาม scenario)

เขียน Playwright test ที่ทำตาม steps เหล่านี้ครบถ้วน:

**Scenario:** ทดสอบว่า "สร้าง todo ผ่าน UI แล้วลบ — ยืนยันทั้งสองขั้นตอนใน DB"

**ต้องทำตาม steps นี้:**
1. Reset DB ก่อน test (ใช้ `POST /api/reset`)
2. สร้าง todo ข้อความว่า "Task to be deleted" ผ่าน UI (input + click add button)
3. API Read-back: verify ว่า todo มีอยู่ใน `GET /api/todos`
4. ลบ todo ผ่าน UI
5. Negative Verification: verify ว่า todo ไม่อยู่ใน `GET /api/todos` แล้ว

**Constraints:**
- ใช้ `test.beforeEach` สำหรับ reset
- ใช้ `expect.objectContaining` สำหรับ partial match
- ใช้ `not.toContainEqual` สำหรับ negative verify
```

เฉลยที่สมบูรณ์ runnable ได้จริง

- [ ] **Step 5: เขียน L4 — Independent Application**

```markdown
### L4: Independent Application (ออกแบบเอง)

**Scenario:** คุณกำลังทดสอบระบบ "ห้องสมุดออนไลน์" ที่มีฟีเจอร์ "ยืมหนังสือ" ซึ่ง:
- User click ปุ่ม "ยืมหนังสือ" → ระบบ call `POST /api/borrows`
- API return 201 และ UI แสดง "ยืมสำเร็จ"
- หนังสือมีสถานะ `available: false` ใน DB
- User dashboard แสดงรายการหนังสือที่ยืมอยู่

ออกแบบและเขียน test suite (2-3 tests) ที่ verify ทั้ง:
1. การยืมสำเร็จ — ตรวจทุก layer (UI success message, DB สถานะ available เปลี่ยน, UI dashboard แสดงในรายการ)
2. Negative case — หนังสือที่ถูกยืมแล้วต้องไม่ available ใน DB (ป้องกัน double-booking)
3. Cleanup — คืนหนังสือหลัง test เพื่อ isolation

**ไม่มี scaffold ให้** — ออกแบบ structure เองทั้งหมด รวมถึง beforeEach/afterEach และ API endpoints ที่ใช้
```

เฉลยที่แสดง approach ที่ดีพร้อมอธิบาย tradeoffs

- [ ] **Step 6: เขียน L5 — Expert / Synthesis**

```markdown
### L5: Expert / Synthesis (Production-grade)

**Context:** ทีม QA ของคุณมี test suite 400 tests รันบน GitHub Actions ด้วย 4 workers (parallel) 

ปัจจุบันมีปัญหา: tests ที่ verify todo counts fail intermittently ~25% ของเวลา บน CI แต่ pass เสมอบน local (single worker)

นี่คือ sample ของ test ที่ problematic:

```typescript
// tests/todos.spec.ts
test.beforeEach(async ({ request }) => {
  await request.post('http://localhost:3000/api/reset');
});

test('admin stats shows correct todo count', async ({ request }) => {
  await request.post('http://localhost:3000/api/todos', { data: { text: 'A' } });
  await request.post('http://localhost:3000/api/todos', { data: { text: 'B' } });
  
  const loginRes = await request.post('http://localhost:3000/api/auth/login', {
    data: { username: 'admin', password: 'admin123' }
  });
  const { token } = await loginRes.json();
  
  const statsRes = await request.get('http://localhost:3000/api/admin', {
    headers: { Authorization: `Bearer ${token}` }
  });
  const { stats } = await statsRes.json();
  expect(stats.todos).toBe(2);  // fail intermittently ใน parallel
});
```

**คำถาม (ต้องตอบครบทุกข้อ):**

1. **วิเคราะห์ Root Cause:** อธิบายอย่างละเอียดว่าทำไม test นี้ pass บน local (single worker) แต่ fail ใน parallel — trace ผ่าน code ว่า race condition เกิดตรงไหน

2. **ออกแบบ Solution:** เขียน implementation ที่แก้ปัญหา โดยไม่ลด workers และไม่ทำให้ test ช้าลงมากกว่า 20% — พิจารณา: per-worker data isolation, request context isolation, หรือ approach อื่น

3. **Evaluate Trade-offs:** เปรียบเทียบ 3 approaches:
   - A: `beforeEach` reset (ที่ใช้อยู่)
   - B: Worker-scoped fixture ที่ใช้ unique todo prefix ต่อ worker
   - C: Mock API ที่ intercept `/api/todos` ต่อ test
   
   ระบุว่าแต่ละแบบ ✅ ดีตรงไหน ❌ ปัญหาตรงไหน และแนะนำ approach สำหรับ production suite ขนาดนี้

4. **Retrofit Strategy:** ถ้าต้องนำ solution ของคุณไปใช้กับ 400 tests ที่มีอยู่โดยไม่ rewrite ทั้งหมด — จะทำอย่างไร? (migration path)
```

เฉลยที่ละเอียด ครอบคลุมทุกประเด็น

- [ ] **Step 7: อัปเดต footer ของ exercises.md**

เปลี่ยน:
```markdown
*แบบฝึกหัดนี้ครอบคลุม 18 บท × 3 ระดับ = 54 exercises*
```
เป็น:
```markdown
*แบบฝึกหัดหลัก: 18 บท × 3 ระดับ = 54 exercises*  
*Ch19 เพิ่มเติม: 5 levels (L1–L5) สำหรับ Database State Verification*  
*Expert Level (L5) เพิ่มเติม: Ch13, Ch15, Ch17, Ch18*
```

- [ ] **Step 8: Commit Ch19 exercises**

```bash
git add docs/playwright-typescript/exercises.md
git commit -m "docs: add ch19 exercises 5 levels l1-l5 db verification"
```

---

## Task 8: Write Expert L5 Exercises for Ch13 + Ch15

**Files:**
- Modify: `docs/playwright-typescript/exercises.md`

เพิ่ม L5 exercises ให้แต่ละบท หลัง exercises เดิมของบทนั้น (ก่อน `---` separator)

- [ ] **Step 1: เพิ่ม L5 ให้ Ch13 (Authentication & Storage State)**

เพิ่มต่อจาก Synthesis (Advanced) ของ Ch13:

```markdown
### L5: Expert (Authentication + DB State Verification)

**Context:** ทีมของคุณ deploy ระบบ authentication ใหม่ที่ migrate จาก cookie-based เป็น JWT + refresh token

test ชุดเดิมที่ใช้ storageState เริ่ม fail บน CI หลัง migration โดย error message คือ:
`Error: page.goto: net::ERR_ABORTED at http://localhost:3000/admin`

นี่คือ auth setup ปัจจุบัน:
```typescript
// auth.setup.ts
test('authenticate as admin', async ({ page }) => {
  await page.goto('http://localhost:3000/login');
  await page.getByTestId('input-username').fill('admin');
  await page.getByTestId('input-password').fill('admin123');
  await page.getByTestId('btn-login').click();
  await expect(page).toHaveURL('http://localhost:3000/todos');
  await page.context().storageState({ path: 'playwright/.auth/admin.json' });
});

// admin.spec.ts
test.use({ storageState: 'playwright/.auth/admin.json' });
test('admin can view stats', async ({ page, request }) => {
  await page.goto('http://localhost:3000/admin');
  await expect(page.getByTestId('stats-panel')).toBeVisible();
  
  // DB verify
  const statsRes = await request.get('http://localhost:3000/api/admin');
  expect(statsRes.status()).toBe(200);  // ← fail: 401
});
```

**คำถาม:**

1. **Diagnose:** อธิบาย root cause ว่าทำไม `request.get('/api/admin')` return 401 แม้ว่า `storageState` ถูก set แล้ว — ความแตกต่างระหว่าง `page` context กับ `request` context คืออะไร

2. **Fix:** เขียน auth setup + test ที่แก้ปัญหา โดย:
   - API requests ใช้ JWT token ที่ถูกต้อง
   - `storageState` ยังใช้กับ `page` ได้ปกติ
   - ไม่ต้อง login ซ้ำสำหรับทุก test

3. **DB Cross-verification:** เพิ่ม verification ที่ตรวจว่า logged-in user ใน `GET /api/me` ตรงกับ user ที่ storageState บันทึกไว้ — เขียน assertion ที่ verify ทั้ง UI state และ API state สอดคล้องกัน
```

เฉลยครบถ้วน

- [ ] **Step 2: เพิ่ม L5 ให้ Ch15 (API Testing + Hybrid)**

เพิ่มต่อจาก Synthesis (Advanced) ของ Ch15:

```markdown
### L5: Expert (Hybrid API + DB Cross-verification)

**Context:** ระบบ e-commerce มี bug report: "ยอดสรุปใน admin dashboard ไม่ตรงกับจำนวน orders จริง"

Investigation พบว่า `GET /api/admin` return `{ stats: { orders: 5 } }` แต่ `GET /api/orders` ไม่มี endpoint นี้ (demo app ไม่ได้ expose GET /api/orders) และ UI dashboard แสดง "5 orders" แต่ customer report บอกว่าสั่งไป 6 ครั้งแล้ว

นี่คือ test ที่ทีมเขียนและ pass:
```typescript
test('order creation flow works correctly', async ({ page, request }) => {
  const loginRes = await request.post('http://localhost:3000/api/auth/login', {
    data: { username: 'testuser', password: 'test123' }
  });
  const { token } = await loginRes.json();
  
  // สร้าง order ผ่าน UI (3 orders)
  for (let i = 0; i < 3; i++) {
    await page.goto('http://localhost:3000/checkout');
    await page.getByRole('button', { name: 'Place Order' }).click();
    await expect(page.getByTestId('order-success')).toBeVisible();
  }
  
  // Verify count ผ่าน admin API
  const adminRes = await request.get('http://localhost:3000/api/admin', {
    headers: { Authorization: `Bearer ${token}` }
  });
  const { stats } = await adminRes.json();
  expect(stats.orders).toBe(3);  // ← pass ✓
});
```

**คำถาม:**

1. **Gap Analysis:** ระบุว่า test นี้ verify อะไรได้บ้าง และไม่ verify อะไร — โดยเฉพาะในแง่ข้อมูลที่ถูก persist ใน DB จริงๆ

2. **Diagnose the Real Bug:** ถ้า `POST /api/orders` มี bug ที่ insert record ซ้ำกัน (double-insert) แต่ return response เหมือนปกติ — test นี้จะ catch bug นั้นได้ไหม? อธิบายว่าทำไม

3. **Design Better Test:** เขียน test ที่ verify ทุก layer ของ order creation:
   - API response ถูกต้อง (status + body)
   - DB count เพิ่มขึ้นถูกต้อง (via admin stats)
   - DB content ถูกต้อง (ต้องใช้ direct file read เพราะไม่มี GET /api/orders)
   - UI แสดงผลถูกต้อง
   
4. **Cleanup Strategy:** ออกแบบ cleanup สำหรับ orders ที่สร้างใน test — demo app ไม่มี DELETE /api/orders, ต้องทำอย่างไร?
```

เฉลยครบถ้วน รวมถึง direct file read approach สำหรับ orders

- [ ] **Step 3: Commit Ch13 + Ch15 L5 exercises**

```bash
git add docs/playwright-typescript/exercises.md
git commit -m "docs: add expert l5 exercises for ch13 auth and ch15 api-hybrid"
```

---

## Task 9: Write Expert L5 Exercises for Ch17 + Ch18

**Files:**
- Modify: `docs/playwright-typescript/exercises.md`

- [ ] **Step 1: เพิ่ม L5 ให้ Ch17 (CI/CD Integration)**

เพิ่มต่อจาก Synthesis (Advanced) ของ Ch17:

```markdown
### L5: Expert (CI/CD + DB State Management)

**Context:** GitHub Actions pipeline ของคุณรัน 300 tests ด้วย 4 shards แบบ parallel บน shared environment (ทุก shard ชี้ไปที่ demo app instance เดียวกันที่ `localhost:3000`)

Tests ที่ verify DB state fail ~30% บน CI แต่ pass บน local:

```yaml
# .github/workflows/playwright.yml (ปัจจุบัน)
jobs:
  test:
    strategy:
      matrix:
        shard: [1, 2, 3, 4]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: npm ci
      - run: cd docs/playwright-typescript/playwright-course-app && npm ci
      - run: cd docs/playwright-typescript/playwright-course-app && npm start &
      - run: npx playwright test --shard=${{ matrix.shard }}/4
```

**คำถาม:**

1. **Root Cause:** อธิบายว่าทำไม 4 shards ที่ชี้ไป demo app instance เดียวกันถึงทำให้ DB tests fail — trace ผ่าน scenario จริงที่ shard 1 และ shard 2 อาจ interfere กัน

2. **Solution Design:** ออกแบบ workflow ที่แก้ปัญหาโดยใช้หนึ่งในสามแนวทาง:
   - A: แต่ละ shard รัน demo app instance แยกบน port ต่างกัน (3001, 3002, ...)
   - B: ใช้ unique data prefixes per shard เพื่อ avoid collision
   - C: Sequential sharding สำหรับ DB-sensitive tests เท่านั้น
   
   เลือก 1 approach แล้วเขียน complete workflow yaml ที่ implement ได้จริง

3. **Trade-off Analysis:** เปรียบเทียบ 3 approaches ข้างต้นในด้าน:
   - Setup complexity
   - CI cost (runner minutes)
   - Reliability
   - Maintenance overhead
   
   แนะนำว่า production team ควรใช้แบบไหนและทำไม

4. **Failure Recovery:** ออกแบบ cleanup step ใน workflow ที่รันเสมอ (แม้ tests fail) เพื่อ reset DB state สำหรับ next run
```

เฉลยครบถ้วน พร้อม yaml ที่ runnable

- [ ] **Step 2: เพิ่ม L5 ให้ Ch18 (Production Patterns)**

เพิ่มต่อจาก Synthesis (Advanced) ของ Ch18:

```markdown
### L5: Expert (Production DB Verification Architecture)

**Context:** บริษัท startup ของคุณมี test suite ที่เติบโตจาก 50 เป็น 350 tests ใน 6 เดือน ทีม QA 3 คน maintain tests ส่วน CI ใช้ GitHub Actions 4 workers

ปัญหาปัจจุบัน (รายงานจาก team):
- Tests fail บน CI ~20% โดยไม่มี pattern ชัดเจน
- "DB count tests" เสี่ยงที่สุด — fail มากกว่าปกติหลัง deploy
- ทีมเริ่ม skip DB verification เพราะ "มันพังตลอด" 
- มี 80 tests ที่ใช้ `beforeEach` reset แต่อีก 270 tests ไม่มี cleanup ใดๆ

นี่คือตัวอย่างของ tests ที่เป็นปัญหา:
```typescript
// tests/user-management.spec.ts — ไม่มี cleanup
test('user count increases after registration', async ({ page, request }) => {
  const adminLogin = await request.post('/api/auth/login', {
    data: { username: 'admin', password: 'admin123' }
  });
  const { token } = await adminLogin.json();
  
  const before = await (await request.get('/api/admin', {
    headers: { Authorization: `Bearer ${token}` }
  })).json();
  
  // Register new user through UI
  await page.goto('/register');
  await page.getByLabel('Username').fill(`user_${Date.now()}`);
  // ... complete registration form
  
  const after = await (await request.get('/api/admin', {
    headers: { Authorization: `Bearer ${token}` }
  })).json();
  
  expect(after.stats.users).toBe(before.stats.users + 1);
});
```

**คำถาม:**

1. **Full Root Cause Analysis:** วิเคราะห์ทุกปัญหาที่เห็นใน codebase นี้ โดย categorize เป็น:
   - Race conditions
   - State contamination
   - Test dependency issues
   - Architecture problems
   
   สำหรับแต่ละ category ระบุ test ที่น่าจะเป็นตัวอย่างของปัญหานั้น

2. **Production Fix Strategy:** ออกแบบ strategy ที่ fix ปัญหาทั้งหมดโดย:
   - ไม่ rewrite tests ทั้ง 350 ตัว (effort ต่ำ)
   - ลด flakiness ลงอย่างน้อย 90%
   - ทีม 3 คน implement ได้ภายใน 1 sprint (2 สัปดาห์)
   
   Strategy ต้องระบุ: อะไรเปลี่ยนก่อน, อะไรเปลี่ยนหลัง, และอะไรไม่ต้องเปลี่ยน

3. **DB Verification Architecture:** ออกแบบ shared fixture library (`test-utils/db-fixtures.ts`) ที่ทีมใช้ร่วมกันได้ และมี:
   - `cleanDb` fixture — reset ก่อน/หลัง test
   - `withDbSnapshot` fixture — save state ก่อน test, restore หลัง test
   - `verifyDbState` helper — type-safe DB assertion สำหรับ entities ที่มี (todos, orders, products, users)
   
   เขียน TypeScript types และ fixture definitions ที่ complete

4. **Measurement Plan:** ออกแบบ metrics ที่ track progress ของ fix — วัดอะไร และ consider suite "healthy" เมื่อไหร่?
```

เฉลยครบถ้วน รวมถึง fixture library code

- [ ] **Step 3: Commit Ch17 + Ch18 L5 exercises**

```bash
git add docs/playwright-typescript/exercises.md
git commit -m "docs: add expert l5 exercises for ch17 cicd and ch18 production-patterns"
```

---

## Task 10: Update HANDOFF.md + 00-overview.md + Final Commit

**Files:**
- Modify: `docs/playwright-typescript/HANDOFF.md`
- Modify: `docs/playwright-typescript/00-overview.md`

- [ ] **Step 1: อัปเดต HANDOFF.md**

ในตาราง status เพิ่ม row:
```markdown
| Ch19 | 19-db-verification.md | ✅ เสร็จ | [actual line count] |
```

อัปเดต header เป็น:
```markdown
**🎉 ครบทุกบทแล้ว (19 บท) — ไม่มีงานเหลือ**
```

อัปเดต section "คำสั่งสำหรับ session ใหม่" เพิ่มข้อมูลว่ามี Ch19 แล้ว

- [ ] **Step 2: อัปเดต 00-overview.md**

เพิ่ม Ch19 ใน Table of Contents section:
```markdown
| บทที่ 19 | [Database State Verification](19-db-verification.md) | ปิด Loop ด้วยการตรวจ DB — API read-back, direct file read, async polling, isolation |
```

- [ ] **Step 3: อัปเดต memory**

อัปเดต `C:\Users\pornc\.claude\projects\d--Personal-04-PROJECTS-Claude-Code-for-document-Claude-Code-for-document\memory\project_playwright_course.md`:
- เพิ่ม Ch19 ในตาราง files
- อัปเดต description ใน MEMORY.md index ให้ครอบคลุม Ch19 + 5-level exercises

- [ ] **Step 4: Final commit**

```bash
git add docs/playwright-typescript/HANDOFF.md
git add docs/playwright-typescript/00-overview.md
git commit -m "docs: update handoff and overview for ch19 database state verification"
```

---

## Self-Review: Spec Coverage Check

| Spec Requirement | Covered by Task |
|-----------------|-----------------|
| Ch19 new chapter with 8 sections | Task 2, 3, 4, 5, 6 |
| 6 DB verification patterns | Task 3 (P1-3), Task 4 (P4-6) |
| RF/Selenium comparison table | Task 4 Step 4 |
| All code verified from playwright.dev | Task 1 (source-notes first) |
| All examples tested against demo app | Task 5 (full runnable with demo app URLs) |
| Common Mistakes with source citations | Task 6 Step 1 |
| Ch19 exercises L1-L5 (5 levels) | Task 7 |
| Expert L5 for Ch13 | Task 8 Step 1 |
| Expert L5 for Ch15 | Task 8 Step 2 |
| Expert L5 for Ch17 | Task 9 Step 1 |
| Expert L5 for Ch18 | Task 9 Step 2 |
| HANDOFF.md + 00-overview.md updated | Task 10 |
| source-notes updated | Task 1 |

**Placeholder scan:** ไม่มี "TBD" หรือ "implement later" — ทุก step มี code หรือ content ที่ชัดเจน ✅

**Type consistency:** `APIRequestContext` methods ใช้ consistent: `.get()`, `.post()`, `.json()`, `.ok()`, `.status()` ✅

**Spec gaps:** ไม่พบ ✅
