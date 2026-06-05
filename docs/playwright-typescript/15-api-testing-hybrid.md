## ก่อนอ่านบทนี้ ลองตอบ:

1. คุณเขียน test ที่มี popup window เปิดขึ้นมาเมื่อ click ปุ่ม — ทำไมต้องเรียก `page.waitForEvent('popup')` ก่อน `await page.click(...)` แทนที่จะเรียกหลัง? และถ้าลืม `await` ก่อน `page.waitForEvent()` จะเกิดอะไรขึ้น?

2. `page.frameLocator()` ต่างจาก `page.frame()` อย่างไร? และ `locator.contentFrame()` (เพิ่มใน v1.43) ถูก design มาเพื่อแก้ปัญหาอะไร?

---

เฉลย:

1. เพราะ popup event เกิดขึ้นทันทีที่ browser เปิด window ใหม่ — ถ้าเรียก `waitForEvent` หลัง click คุณอาจพลาด event ไปแล้ว (race condition) ส่วนถ้าลืม `await` ก่อน `page.waitForEvent()` จะได้ Promise object กลับมา ไม่ใช่ค่า popup page
2. `frameLocator()` return FrameLocator ที่ scope การหา locator ไว้ภายใน iframe — ทุก `.getBy...()` หลังจากนั้นจะหาใน iframe เท่านั้น โดยไม่ต้อง switch context แบบ Selenium ส่วน `page.frame()` เป็น older API ที่ return Frame object แบบ Selenium-style และ `locator.contentFrame()` ใช้เมื่อคุณมี Locator ของ `<iframe>` element อยู่แล้ว แต่อยากเข้าถึง content ข้างใน — เป็น starting point ที่ต่างกันแต่ผลเหมือนกัน

---

# บทที่ 15: API Testing & Hybrid UI+API Patterns

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- ใช้ `request` fixture ที่ built-in ใน Playwright เพื่อ call API โดยไม่ต้องติดตั้ง library เพิ่ม
- ส่ง GET, POST, PATCH, DELETE requests พร้อม JSON body, headers, และ query params
- assert responses ด้วย `toBeOK()`, `toBe(statusCode)`, และ `toMatchObject()` แบบ type-safe
- ตั้งค่า auth header ทั้งแบบ per-request และ per-context ด้วย `extraHTTPHeaders`
- เขียน hybrid test ที่ setup ข้อมูลผ่าน API (เร็ว ~50ms) แล้ว verify ผ่าน UI (ครอบคลุม full stack)
- เปรียบเทียบ approach นี้กับ Robot Framework + Selenium ที่ต้องใช้ RequestsLibrary แยก

---

## 2. ทำไมต้องรู้? (Why)

ลองนึกถึงสถานการณ์นี้: คุณมี test ที่ต้องทดสอบว่า "หลังจากสร้าง 10 items ใน shopping cart แล้ว checkout page แสดง total ถูกต้อง" ถ้าทำทุกอย่างผ่าน UI คุณต้องคลิก 10 ครั้ง กรอก form 10 ครั้ง รอ animation 10 ครั้ง — test นึงอาจใช้เวลา 30-60 วินาที

แล้วถ้ามี 50 test cases ที่ต้องการ cart ที่มีของ? คำนวณเองได้

**วิธีที่ดีกว่า**: setup ข้อมูล cart ผ่าน API (ใช้เวลา < 500ms) แล้วเปิด checkout page ใน UI เลย — ใช้เวลารวม 2-3 วินาทีแทน 60 วินาที

นอกจากเรื่องความเร็ว:

- **API tests แยก business logic ออกจาก UI** — ถ้า API พัง คุณจะรู้ก่อนที่ UI test จะรัน
- **Hybrid tests ทดสอบ full stack จริง** — verify ว่า data ที่ create ผ่าน API นั้น render ใน UI ถูกต้อง ซึ่งทดสอบ backend + frontend integration ในคราวเดียว
- **Playwright รวม API client ไว้ใน framework** — ใน Robot Framework คุณต้องติดตั้ง RequestsLibrary แยก, manage session แยก, และ state ระหว่าง library ไม่ share กัน

---

## 3. Analogy

**Hybrid test คือเชฟที่ใช้ microwave (API) เตรียม base แล้วตกแต่งด้วยมือ (UI)**

เชฟต้องทำ roast chicken 50 จาน สำหรับงาน party ใหญ่ ถ้าทุกอย่างทำด้วยมือตั้งแต่ต้น — ล้างไก่, ปรุงรส, มัด, ใส่เตา, ตรวจอุณหภูมิ — จะใช้เวลามหาศาล แต่เชฟฉลาดจะ prepare base เนื้อไก่ด้วย sous vide machine (API) ที่ทำงานอัตโนมัติเป็น batch แล้วนำมาตกแต่ง, ปรุง sauce, จัดจาน (UI) ก่อนเสิร์ฟเท่านั้น — ผลลัพธ์คุณภาพเดิม แต่เร็วกว่ามาก

ใน Playwright: เตรียม test data ผ่าน API (sous vide), แล้ว verify presentation และ UX ผ่าน browser (จัดจาน)

⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:
- **API test ใน Playwright เหมือนกับการใช้ `fetch()` หรือ `axios` ใน code ทั่วไป** — จริงๆ `request` fixture คือ `APIRequestContext` ที่มี context management, base URL, cookies, TLS certificate handling, และ `extraHTTPHeaders` built-in มาให้ — ไม่ใช่แค่ HTTP client ธรรมดา
- **Hybrid test ที่ setup ผ่าน API แต่ verify ผ่าน UI จะ slow เสมอเพราะต้องรอ browser** — ส่วน API calls ใช้เวลา ~50ms ต่อ call ซึ่งเร็วมาก เทียบกับ UI form fill ที่ใช้ ~2-3 วินาทีต่อ field — hybrid test จึงเร็วกว่า full UI test มาก

---

## 4. เนื้อหาหลัก

### 4.1 `request` Fixture คืออะไร

Playwright Test comes with a built-in `request` fixture ที่ `respects configuration options like baseURL or extraHTTPHeaders` (docs) และพร้อม inject เข้า test เหมือนกับ `page` fixture ทุกประการ ไม่ต้องติดตั้งอะไรเพิ่ม

```typescript
// partial example — see Section 5 for runnable version
test('example', async ({ request }) => {
  // request คือ APIRequestContext instance
  // พร้อมใช้งานทันที
});
```

ข้อสำคัญ: `request` fixture เป็น isolated instance ต่อ test เหมือนกัน — state (cookies, headers) ไม่ carry over ระหว่าง tests

### 4.2 HTTP Methods

`APIRequestContext` รองรับทุก HTTP method หลัก:

```typescript
// partial example — see Section 5 for runnable version
// GET — ไม่มี body
const res = await request.get('http://localhost:3000/api/todos');

// POST — ส่ง JSON body ด้วย data option
const res = await request.post('http://localhost:3000/api/todos', {
  data: { text: 'Buy groceries' }
});

// PATCH — อัปเดตบางส่วน
const res = await request.patch(`http://localhost:3000/api/todos/${id}`, {
  data: { completed: true }
});

// DELETE — ลบ
const res = await request.delete(`http://localhost:3000/api/todos/${id}`);

// PATCH — อัปเดตบางฟิลด์
const res = await request.patch(`http://localhost:3000/api/todos/${id}`, {
  data: { text: 'Updated text', completed: false }
});
```

**Options ที่ใช้บ่อย:**
- `data: {...}` — JSON body (Playwright set `Content-Type: application/json` ให้อัตโนมัติ)
- `headers: { ... }` — custom headers เพิ่มเติม
- `params: { page: 1, limit: 20 }` — query string (`?page=1&limit=20`)

### 4.3 Response Methods

ทุก method return `APIResponse` object:

```typescript
// partial example — see Section 5 for runnable version
const res = await request.get('http://localhost:3000/api/todos');

res.ok()        // boolean — true ถ้า status 200-299
res.status()    // number — HTTP status code เช่น 200, 201, 404
await res.json()    // parse body เป็น object (ต้อง await!)
await res.text()    // raw string body
res.headers()   // object ของ response headers ทั้งหมด
res.url()       // URL จริงที่ request ไป (หลัง redirect)
```

⚠️ **`res.json()` เป็น async** — ต้อง `await` เสมอ มือใหม่ลืมบ่อยมาก

### 4.4 Assertions

```typescript
// partial example — see Section 5 for runnable version
// ตรวจว่า status 2xx (แนะนำทำก่อน assert อื่นๆ เสมอ)
expect(response).toBeOK();

// ตรวจ exact status code
expect(response.status()).toBe(201);

// ตรวจ body shape (partial match)
const body = await response.json();
expect(body).toMatchObject({
  text: 'Buy groceries',
  completed: false,
});

// ตรวจว่ามี item นี้ใน array (partial match)
expect(body).toContainEqual(
  expect.objectContaining({ text: 'Buy groceries', completed: false })
);
```

**ทำไมต้อง `toBeOK()` ก่อน?** เพราะถ้า status ผิด (เช่น 500) แล้วคุณ assert body ต่อ error message จะมาจากการ parse JSON ของ error response — ทำให้ debug ยาก ถ้า `toBeOK()` fail ก่อนจะได้รู้ทันทีว่าปัญหาคือ status code

### 4.5 Auth Headers

**Per-request** — ส่ง header ในแต่ละ call เลย:

```typescript
// partial example — see Section 5 for runnable version
const token = 'eyJhbGciOi...';

const res = await request.get('http://localhost:3000/api/me', {
  headers: { Authorization: `Bearer ${token}` }
});
```

**Per-context** — ใช้ `extraHTTPHeaders` ใน `newContext()` เมื่อต้องการ header เดิมทุก request ใน context นั้น:

```typescript
// partial example — see Section 5 for runnable version
const authRequest = await request.newContext({
  baseURL: 'http://localhost:3000',
  extraHTTPHeaders: {
    Authorization: `Bearer ${token}`
  }
});

// ทุก request จาก authRequest จะมี Authorization header ติดไปอัตโนมัติ
const me = await authRequest.get('/api/me');
const todos = await authRequest.get('/api/todos');

// cleanup เมื่อเสร็จ
await authRequest.dispose();
```

**ความแตกต่างจาก `extraHTTPHeaders` ใน `playwright.config.ts`**: ถ้าตั้งใน config `use:` block จะส่ง header ไปทุก request จากทุก context ใน project — อาจ leak auth header ไปยัง external API โดยไม่ตั้งใจ ใช้ `newContext()` สำหรับ scoped auth แทน

### 4.6 Hybrid Test Pattern (ทรงพลังที่สุด)

Playwright สามารถ inject ทั้ง `request` และ `page` ใน test เดียวกัน ทำให้เขียน hybrid test ได้ง่ายมาก:

```typescript
// partial example — see Section 5 for runnable version
test('todo created via API appears in UI', async ({ request, page }) => {
  // Step 1: Reset state ผ่าน API (เร็ว)
  await request.post('http://localhost:3000/api/reset');

  // Step 2: Create data ผ่าน API (เร็ว ~50ms)
  const res = await request.post('http://localhost:3000/api/todos', {
    data: { text: 'Buy groceries' }
  });
  expect(res.ok()).toBe(true);

  // Step 3: Verify ใน UI (validate full stack)
  await page.goto('http://localhost:3000/todos');
  await expect(page.getByText('Buy groceries')).toBeVisible();
});
```

Pattern นี้ verify ว่า backend API ทำงานถูกต้อง + UI render ข้อมูลถูกต้อง ในคราวเดียว — เป็น integration test ที่ครอบคลุมจริง

### 4.7 Response Schema Validation

ใช้ `toMatchObject()` ตรวจ shape ของ response:

```typescript
// partial example — see Section 5 for runnable version
const todo = await res.json();

// ตรวจทุก field ที่ต้องการ
expect(todo).toMatchObject({
  id: expect.any(Number),
  text: 'Buy groceries',
  completed: false,
});

// ใช้ expect.objectContaining() สำหรับ partial match ใน array
const todos = await listRes.json();
expect(todos).toContainEqual(
  expect.objectContaining({ text: 'Buy groceries', completed: false })
);
```

`expect.any(Number)` บอกว่า field `id` ต้องมี type เป็น number แต่ไม่ต้องระบุค่า exact — ใช้เมื่อ id ถูก generate โดย server

### 4.8 Demo App Endpoints

สำหรับ course นี้ใช้ demo app ที่มี endpoints ดังนี้:

| Method | Path | Body | Auth | คำอธิบาย |
|--------|------|------|------|-----------|
| GET | `/api/todos` | — | ไม่ต้อง | list todos |
| POST | `/api/todos` | `{ text }` | ไม่ต้อง | create todo |
| PATCH | `/api/todos/:id` | `{ completed }` | ไม่ต้อง | update todo |
| DELETE | `/api/todos/:id` | — | ไม่ต้อง | delete todo |
| POST | `/api/auth/login` | `{ username, password }` | ไม่ต้อง | login → `{ token }` |
| GET | `/api/me` | — | Bearer token | ข้อมูล user ปัจจุบัน |
| GET | `/api/admin` | — | Bearer token + admin role | admin-only (403 for regular user) |
| POST | `/api/reset` | — | ไม่ต้อง | reset todos ทั้งหมด |

### 4.9 RF/Selenium Comparison

| | Robot Framework + Selenium | Playwright |
|-|--------------------------|------------|
| API testing | RequestsLibrary (ต้อง install แยก) | `request` fixture built-in |
| Auth ใน API test | RequestsLibrary session object | `extraHTTPHeaders` หรือ per-request header |
| Hybrid test | ยาก — คนละ library, state ไม่ share กัน | `request` + `page` ใน test เดียว native |
| Response assertion | `Should Be Equal    ${status}    200` string-based | `toBeOK()`, `toMatchObject()` type-safe |
| JSON parse | `Evaluate    json.loads($response.text())` | `await res.json()` built-in |
| Setup test data | UI flow หรือ script แยก | API call ใน `beforeEach` เดียวกัน |

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner — CRUD Todos API

ทดสอบแต่ละ endpoint ของ todo API ทีละ operation:

```typescript
// tested: Playwright v1.50+, Node.js 20+
// tests/api-crud.spec.ts

import { test, expect } from '@playwright/test';

test.describe('Todos API — CRUD', () => {
  // reset state ก่อนทุก test เพื่อ isolation
  test.beforeEach(async ({ request }) => {
    await request.post('http://localhost:3000/api/reset');
  });

  test('GET /api/todos returns empty list after reset', async ({ request }) => {
    const res = await request.get('http://localhost:3000/api/todos');

    expect(res).toBeOK();
    const data = await res.json();
    expect(data).toHaveLength(0);
  });

  test('POST /api/todos creates todo with correct shape', async ({ request }) => {
    const res = await request.post('http://localhost:3000/api/todos', {
      data: { text: 'Buy groceries' }
    });

    expect(res.status()).toBe(201);
    const todo = await res.json();
    expect(todo).toMatchObject({
      id: expect.any(Number),
      text: 'Buy groceries',
      completed: false,
    });
  });

  test('PATCH /api/todos/:id marks todo as completed', async ({ request }) => {
    // สร้าง todo ก่อน
    const createRes = await request.post('http://localhost:3000/api/todos', {
      data: { text: 'Learn Playwright' }
    });
    const { id } = await createRes.json();

    // update เป็น completed
    const patchRes = await request.patch(`http://localhost:3000/api/todos/${id}`, {
      data: { completed: true }
    });

    expect(patchRes).toBeOK();
    const updated = await patchRes.json();
    expect(updated.completed).toBe(true);
  });

  test('DELETE /api/todos/:id removes the todo', async ({ request }) => {
    // สร้างแล้วลบ
    const createRes = await request.post('http://localhost:3000/api/todos', {
      data: { text: 'Temporary task' }
    });
    const { id } = await createRes.json();

    const deleteRes = await request.delete(`http://localhost:3000/api/todos/${id}`);
    expect(deleteRes).toBeOK();

    // verify ว่าไม่มีใน list แล้ว
    const listRes = await request.get('http://localhost:3000/api/todos');
    const todos = await listRes.json();
    expect(todos.find((t: { id: number }) => t.id === id)).toBeUndefined();
  });
});
```

---

### Intermediate — Query Parameters & Pagination

สถานการณ์: ระบบมี `/api/products` endpoint ที่รองรับ pagination และ filtering ด้วย query parameters — เขียน test ตรวจว่า API filter ทำงานถูกต้อง แล้ว verify ผลลัพธ์ที่ render ใน UI ตรงกับ API response:

```typescript
// tested: Playwright v1.50+, Node.js 20+
// tests/api-pagination.spec.ts

import { test, expect } from '@playwright/test';

test.describe('Products API — Query Parameters & Pagination', () => {
  test.beforeEach(async ({ request }) => {
    // reset todos ก่อนทุก test (products เป็น read-only ใน demo app — 10 ชิ้นคงที่)
    await request.post('http://localhost:3000/api/reset');
    // product catalog: 10 items — Electronics(4), Books(3), Clothing(3)
  });

  test('GET /api/products?category=Electronics returns filtered results', async ({ request }) => {
    const res = await request.get('http://localhost:3000/api/products', {
      params: { category: 'Electronics', limit: 10 }
    });

    expect(res).toBeOK();
    const data = await res.json();
    
    // verify API response shape
    expect(data).toHaveProperty('data');
    expect(data.data).toBeTruthy();
    expect(data.data.length).toBeGreaterThan(0);
    expect(data.data.length).toBeLessThanOrEqual(10);
    
    // verify ว่าทั้งหมดเป็น Electronics
    data.data.forEach((product: { category: string }) => {
      expect(product.category).toBe('Electronics');
    });
  });

  test('query params ?page=2&limit=5 returns correct offset', async ({ request }) => {
    // page 1 (offset 0, limit 5)
    const page1Res = await request.get('http://localhost:3000/api/products', {
      params: { page: 1, limit: 5 }
    });
    const page1Data = await page1Res.json();
    const firstPageIds = page1Data.data.map((p: { id: number }) => p.id);

    // page 2 (offset 5, limit 5)
    const page2Res = await request.get('http://localhost:3000/api/products', {
      params: { page: 2, limit: 5 }
    });
    const page2Data = await page2Res.json();
    const secondPageIds = page2Data.data.map((p: { id: number }) => p.id);

    // ตรวจว่า page 2 เป็น items ต่างจาก page 1
    expect(secondPageIds).not.toEqual(firstPageIds);
    expect(page2Data.data.length).toBeGreaterThan(0);
  });

  test('filter via API and verify results in UI (hybrid)', async ({ request, page }) => {
    // ─── VERIFY API FILTER ─── (ตรวจความถูกต้องที่ source)
    const filterRes = await request.get('http://localhost:3000/api/products', {
      params: { category: 'Clothing', limit: 5 }
    });
    
    expect(filterRes).toBeOK();
    const apiProducts = await filterRes.json();
    const filteredCount = apiProducts.data.length;

    // ─── VERIFY FILTER ใน UI ─── (ตรวจว่า UI render API response ถูกต้อง)
    await page.goto('http://localhost:3000/shop');
    
    // select filter dropdown
    await page.selectOption('[data-testid="filter-category"]', 'Clothing');
    
    // wait for results to load
    await page.waitForSelector('[data-testid^="product-card-"]');
    
    // count products displayed
    const uiProducts = await page.locator('[data-testid^="product-card-"]').count();
    
    // UI count ควรตรงกับ API หรือน้อยกว่า (อาจมี pagination)
    expect(uiProducts).toBeGreaterThan(0);
    expect(uiProducts).toBeLessThanOrEqual(filteredCount);
    
    // verify ว่า product name ทั้งหมดที่แสดงเป็นของ Clothing category
    const displayedNames = await page.locator('[data-testid^="product-name-"]').allTextContents();
    // (assuming API data มี name field ที่ UI render)
    expect(displayedNames.length).toBe(uiProducts);
  });
});
```

---

### Advanced — Hybrid Test: Setup ผ่าน API, Verify ผ่าน UI, Teardown ผ่าน API

สถานการณ์: ระบบ todo app มี feature ว่า completed todos จะแสดงใน section "Done" แยกจาก active todos — เขียน test ที่ verify full stack behavior นี้ พร้อม cleanup หลัง test:

```typescript
// tested: Playwright v1.50+, Node.js 20+
// tests/hybrid-todo-flow.spec.ts

import { test, expect } from '@playwright/test';

test.describe('Hybrid UI+API: Todo Workflow', () => {
  test.beforeEach(async ({ request }) => {
    // reset ผ่าน API ก่อนทุก test — เร็วกว่า navigate ไปลบทาง UI
    await request.post('http://localhost:3000/api/reset');
  });

  test('completed todo แสดง strikethrough ใน UI (full stack)', async ({ request, page }) => {
    // ─── SETUP ผ่าน API ─── (~100ms รวม 3 calls)
    // สร้าง 3 todos ผ่าน API — เร็วกว่า fill form ทาง UI 3 ครั้งมาก
    const [res1, res2, res3] = await Promise.all([
      request.post('http://localhost:3000/api/todos', { data: { text: 'Active task 1' } }),
      request.post('http://localhost:3000/api/todos', { data: { text: 'Active task 2' } }),
      request.post('http://localhost:3000/api/todos', { data: { text: 'Finish report' } }),
    ]);

    const todo1 = await res1.json();
    const todo3 = await res3.json();

    // mark task 3 เป็น completed ผ่าน API
    const patchRes = await request.patch(`http://localhost:3000/api/todos/${todo3.id}`, {
      data: { completed: true }
    });
    expect(patchRes).toBeOK();

    // ─── VERIFY ใน UI ─── (validate frontend render)
    await page.goto('http://localhost:3000/todos');

    // ตรวจว่า todos ทั้ง 3 ปรากฏใน list
    await expect(page.getByTestId('todo-list')).toBeVisible();
    await expect(page.getByTestId(`todo-text-${todo1.id}`)).toContainText('Active task 1');
    await expect(page.getByTestId(`todo-text-${todo3.id}`)).toContainText('Finish report');

    // completed todo ควรมี class "completed" (แสดง strikethrough ใน UI)
    await expect(page.getByTestId(`todo-text-${todo3.id}`)).toHaveClass(/completed/);
    await expect(page.getByTestId(`todo-checkbox-${todo3.id}`)).toBeChecked();

    // active todos ยังไม่มี completed class
    await expect(page.getByTestId(`todo-text-${todo1.id}`)).not.toHaveClass(/completed/);

    // ─── VERIFY UI ACTION ─── (ทดสอบว่า UI interaction ทำงานด้วย)
    // check checkbox ใน UI สำหรับ "Active task 1"
    await page.getByTestId(`todo-checkbox-${todo1.id}`).check();

    // หลัง check ควรมี completed class
    await expect(page.getByTestId(`todo-text-${todo1.id}`)).toHaveClass(/completed/);

    // ─── VERIFY ผ่าน API ด้วย ─── (confirm database state ถูกต้อง)
    const listRes = await request.get('http://localhost:3000/api/todos');
    const todos = await listRes.json();

    const task1 = todos.find((t: { id: number }) => t.id === todo1.id);
    expect(task1.completed).toBe(true); // UI action ส่งผลถึง backend จริง
  });

  test('bulk API setup — verify paginated UI (performance test)', async ({ request, page }) => {
    // สร้าง todos จำนวนมากผ่าน API อย่างรวดเร็ว
    const createRequests = Array.from({ length: 15 }, (_, i) =>
      request.post('http://localhost:3000/api/todos', {
        data: { text: `Task ${i + 1}: ${['Review PR', 'Fix bug', 'Write test', 'Update docs', 'Deploy'][i % 5]}` }
      })
    );
    const responses = await Promise.all(createRequests);
    responses.forEach(res => expect(res.status()).toBe(201));

    // UI ควรแสดง pagination หรือ scroll ถ้ามีมากกว่า 10 items
    await page.goto('http://localhost:3000/todos');

    // verify ว่า first page แสดง 10 items (ถ้า app paginate)
    const todoItems = page.locator('[data-testid^="todo-item-"]');
    const count = await todoItems.count();
    expect(count).toBeGreaterThanOrEqual(10);

    // ─── TEARDOWN ผ่าน API ─── (เร็วกว่า UI มาก)
    // request fixture จะ dispose อัตโนมัติหลัง test
    // แต่ถ้าต้องการ explicit cleanup ระหว่าง test:
    const listRes = await request.get('http://localhost:3000/api/todos');
    const todos = await listRes.json();

    // ลบทั้งหมดผ่าน API parallel
    await Promise.all(
      todos.map((t: { id: number }) =>
        request.delete(`http://localhost:3000/api/todos/${t.id}`)
      )
    );

    // verify clean state
    const afterRes = await request.get('http://localhost:3000/api/todos');
    const afterTodos = await afterRes.json();
    expect(afterTodos).toHaveLength(0);
  });
});
```

---

## 6. Common Mistakes

❌ **ลืม `await` ก่อน `res.json()`**

```typescript
// ผิด — data จะเป็น Promise<any> ไม่ใช่ object
const data = res.json();
console.log(data.text); // undefined
```

```typescript
// ✅ ถูก — await ก่อนใช้ข้อมูลเสมอ
const data = await res.json();
console.log(data.text); // "Buy groceries"
```

`res.json()` เป็น async method เพราะต้องรอ parse body stream — ถ้าลืม `await` จะได้ Promise object กลับมา *(source: https://playwright.dev/docs/api-testing#response-methods)*

---

❌ **ใช้ `fetch()` หรือ `axios` แทน `request` fixture**

```typescript
// ผิด — ไม่ได้ share cookies/auth context กับ page
test('check my profile', async ({ page }) => {
  const res = await fetch('http://localhost:3000/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username: 'admin', password: 'admin123' }),
    headers: { 'Content-Type': 'application/json' }
  });
  const { token } = await res.json();
  // token นี้ไม่ได้ถูก inject เข้า page context
});
```

```typescript
// ✅ ถูก — ใช้ request fixture ที่ integrate กับ Playwright context
test('check my profile', async ({ request, page }) => {
  const loginRes = await request.post('http://localhost:3000/api/auth/login', {
    data: { username: 'admin', password: 'admin123' }
  });
  const { token } = await loginRes.json();
  // ใช้ token ต่อได้ใน request fixture และส่งไป page ได้ถ้าต้องการ
});
```

`request` fixture มี baseURL, TLS handling, และ context lifecycle ที่ integrate กับ Playwright runner — ถ้าใช้ `fetch()` ธรรมดา feature เหล่านี้หาย *(source: https://playwright.dev/docs/api-testing#use-request-context)*

---

❌ **Assert body ก่อน assert status**

```typescript
// ผิด — ถ้า status 500 จะ parse JSON ของ error response
// แล้วได้ error message ที่สับสน เช่น "expected { error: 'Internal Server Error' } to have property text"
const data = await res.json();
expect(data.text).toBe('Buy groceries');
```

```typescript
// ✅ ถูก — ตรวจ status ก่อนเสมอ
expect(res).toBeOK(); // ถ้า fail จะเห็นทันทีว่า status ผิด
const data = await res.json();
expect(data.text).toBe('Buy groceries');
```

การ assert status ก่อนทำให้ error message ชัดเจนกว่ามาก โดยเฉพาะเมื่อ debug *(source: https://playwright.dev/docs/api-testing#assertions)*

---

❌ **ตั้ง `extraHTTPHeaders` ใน `playwright.config.ts` สำหรับ auth**

```typescript
// ผิด — ใน playwright.config.ts
export default defineConfig({
  use: {
    extraHTTPHeaders: {
      Authorization: `Bearer ${process.env.AUTH_TOKEN}`,
    }
  }
});
// ปัญหา: header นี้ถูกส่งไปทุก external request รวมถึง CDN, analytics, third-party APIs
```

```typescript
// ✅ ถูก — ใช้ newContext() เพื่อ scope auth ไว้เฉพาะ API ที่ต้องการ
const authRequest = await request.newContext({
  baseURL: 'http://localhost:3000',
  extraHTTPHeaders: { Authorization: `Bearer ${token}` }
});
```

Config-level `extraHTTPHeaders` ส่งไปทุก request จากทุก context ใน project — อาจ leak credentials ไปยัง third-party services *(source: https://playwright.dev/docs/api-testing#context-level-http-headers)*

---

## 7. สรุปบท

ก่อนดูเฉลย ลองตอบ 3 คำถามนี้ด้วยตัวเองก่อน:

**คำถาม 1**: คุณมี test ที่ต้อง setup shopping cart ที่มี 20 items ก่อน navigate ไป checkout page — เปรียบเทียบ approach "setup ผ่าน UI ทั้งหมด" กับ "setup ผ่าน API แล้ว verify ผ่าน UI" ในแง่ความเร็ว, ความเปราะบาง, และ scope ของสิ่งที่ทดสอบ

**คำถาม 2**: ทำไม `expect(res).toBeOK()` ถึงควรเรียกก่อน `const data = await res.json()` เสมอ? และ `toBeOK()` ต่างจาก `expect(res.status()).toBe(200)` อย่างไร?

**คำถาม 3**: คุณต้องเขียน test suite ที่ทดสอบ 3 scenarios: (a) unauthenticated user ได้ 401, (b) regular user ได้ 403 บน admin endpoint, (c) admin user ได้ 200 — คุณจะ structure test file นี้อย่างไรเพื่อลด code ซ้ำซ้อน?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย:**

**คำถาม 1**: Setup ผ่าน UI: ใช้เวลา ~40-60 วินาที (fill form 20 ครั้ง x 2-3s/item), เปราะบาง (พังถ้า UI เปลี่ยน), ทดสอบ UI + backend พร้อมกัน (scope กว้าง) — Setup ผ่าน API: ใช้เวลา ~200-500ms (20 API calls parallel), robust (API contract เปลี่ยนน้อยกว่า UI), แล้วค่อย verify checkout ใน UI ที่เป็น focus จริงๆ — hybrid approach ดีกว่าเพราะ fast + focused

**คำถาม 2**: เพราะถ้า status ผิด (เช่น server return 500 พร้อม JSON error body) แล้ว parse ต่อจะได้ error message ที่สับสน — `toBeOK()` fail จะบอกทันทีว่า "expected 500 to be in range 200-299" ซึ่งชัดกว่ามาก ส่วน `toBeOK()` ต่างจาก `toBe(200)` ตรงที่ accept status 200-299 ทั้งหมด (รวม 201, 204) — ใช้ `toBe(201)` เมื่อต้องการ exact code เช่น verify ว่า create ส่ง 201 ไม่ใช่ 200

**คำถาม 3**: แยก login ออกเป็น helper function หรือ fixture เพื่อไม่ duplicate logic, ใช้ `test.describe()` แยกตาม role, สร้าง `authContext` ใน `test.beforeEach` ของแต่ละ describe block — หรือใช้ Playwright fixture ที่ extends `test` เพื่อ inject token แยกต่อ role

</details>

---

## 8. Pre-chapter Retrieval สำหรับบทถัดไป

บทที่ 16 จะพูดถึง Visual Regression Testing — ก่อนอ่าน ลองนึกดูว่า:

- ถ้าจะทดสอบว่า "UI ของ page ไม่เปลี่ยนไปจากที่ออกแบบไว้" คุณจะ approach อย่างไร? assertion แบบไหนที่ Playwright มีให้สำหรับเรื่องนี้?
- `expect(page).toHaveScreenshot()` ทำงานอย่างไร? ครั้งแรกที่รัน test นี้จะเกิดอะไรขึ้น?
