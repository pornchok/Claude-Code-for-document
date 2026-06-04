## ก่อนอ่านบทนี้ ลองตอบ:

1. Flaky test คืออะไร และสาเหตุที่พบบ่อยที่สุด 3 ข้อที่ทำให้ test ไม่ stable มีอะไรบ้าง?

2. Test Data Factory pattern ทำไมถึงต้องสร้าง unique data ต่อ test แทนที่จะใช้ข้อมูลชุดเดิมซ้ำกัน? ถ้าไม่ทำแบบนี้จะเกิดปัญหาอะไร?

---

เฉลย:

1. Flaky test คือ test ที่บางครั้งผ่าน บางครั้งไม่ผ่านโดยไม่มี code เปลี่ยนแปลง ทำให้ทีมหมดความเชื่อถือ CI และ merge code โดยไม่แน่ใจ สาเหตุหลัก 3 ข้อคือ: (1) **Timing issues** — page ยังโหลดไม่เสร็จแต่ test ดำเนินการไปแล้ว (2) **Shared state** — tests รัน parallel แล้ว state ปนกัน เช่น test A สร้างข้อมูลในขณะที่ test B กำลังลบทั้งหมด (3) **External dependency** — network ช้าหรือ third-party API ไม่ stable

2. Test Data Factory สร้าง unique data ต่อ test เพราะเมื่อ tests รัน parallel บน machine เดียวกัน แต่ละ test ต้องทำงานกับ data ของตัวเองโดยไม่กระทบกัน ถ้าใช้ข้อมูลชุดเดิมซ้ำกัน — test A อาจกำลัง update record เดิมที่ test B กำลัง assert ค่าอยู่พร้อมกัน ทำให้ผลลัพธ์ไม่แน่นอนขึ้นกับ race condition ไม่ใช่ behavior จริงของ app

---

# บทที่ 19: Database State Verification — ปิด Loop ด้วยการตรวจ DB

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- ใช้ `request` fixture เพื่อ read-back ข้อมูลจาก API หลัง UI action — ยืนยันว่า backend รับข้อมูลจริงไม่ใช่แค่ UI แสดงผล
- อ่านไฟล์ DB โดยตรงด้วย Node.js `fs` เพื่อ verify สถานะ persistence ที่ชั้น storage จริง
- ทำ cross-layer verification ครบทั้ง 3 ชั้น (UI → API → DB → UI re-render) ในชุดทดสอบเดียว
- ใช้ `expect.poll()` สำหรับ async DB writes ที่ไม่เสร็จทันทีหลัง HTTP response
- ทำ negative verification — ยืนยันว่าข้อมูลถูก delete ออกจาก DB จริง ไม่ใช่แค่หายจาก UI
- จัดการ DB isolation เพื่อป้องกัน test contamination ข้าม test และ reset state ก่อนแต่ละ test

---

## 2. ทำไมต้องรู้? (Why)

สมมติระบบ e-commerce ของคุณมี bug แบบนี้: ผู้ใช้กด "สั่งซื้อ" — UI แสดง "Order confirmed! #12345" พร้อม animation สวยงาม แต่ backend async handler มี race condition — order ถูก write ลง DB แค่ 60% ของเวลา อีก 40% silent fail ไป ไม่มี error message ไม่มี HTTP 500 ทุกอย่างดูปกติ

Test ที่ตรวจแค่ UI จะ **ผ่าน 100%** ทั้งที่ bug นี้ทำให้ลูกค้า 40% ไม่ได้รับสินค้า

หรือระบบ HR ที่ admin กด "ลบพนักงาน" แล้ว UI แสดงว่าลบแล้ว แต่ soft-delete logic มีเงื่อนไขผิด — record ยังอยู่ใน DB และยังสามารถ login ได้อยู่ ถ้า test ตรวจแค่ว่า "ชื่อหายออกจาก list" ก็จะผ่าน แต่ security bug ยังอยู่ครบ

อีกกรณี: form validation ผ่าน frontend แต่ backend ใช้ database transaction — ถ้าข้อมูล violate constraint ตัวใดตัวหนึ่ง backend จะ rollback ทั้งหมด UI ไม่รู้เรื่อง แสดง success ต่อไป แต่ DB ไม่มีข้อมูลนั้นเลย

**ปัญหาร่วมของทั้งสามกรณีคือ: test ตรวจแค่ชั้นเดียว**

ระบบจริงทำงาน 3 ชั้นเสมอ:

- **UI Layer** — สิ่งที่ผู้ใช้เห็นและโต้ตอบ
- **API Layer** — business logic, validation, transaction
- **DB Layer** — state ที่ persist จริง — ข้อมูลที่อยู่ที่นี่คือ "ความจริง" ของระบบ

Test ที่ครอบคลุมต้องตรวจทั้ง 3 ชั้น เพราะแต่ละชั้นสามารถ fail แบบ silent โดยชั้นบนยังแสดงผลปกติได้

---

## 3. Analogy

**การตรวจ DB หลัง action เหมือนระบบตรวจสอบของธนาคาร 3 ชั้น**

ลองนึกภาพคุณโอนเงิน 50,000 บาทผ่าน ATM:

- **ATM screen (UI Layer)** แสดง "โอนเงินสำเร็จ! Transaction ID: TXN-9981" — คุณเห็นข้อความนี้และเชื่อว่าเสร็จแล้ว
- **Transaction API (API Layer)** รับ request, สร้าง transaction record, return HTTP 200 พร้อม transaction ID — ดูเหมือนทุกอย่างถูกต้อง
- **Core Banking System (DB Layer)** ตัดยอดจริงจากบัญชีคุณและเพิ่มยอดในบัญชีปลายทาง — นี่คือสิ่งที่เกิดขึ้น "จริงๆ"

ธนาคารไม่ได้เชื่อแค่ ATM screen — ระบบ reconciliation รัน batch job ตรวจสอบทุก transaction ว่า ATM บอกว่าโอนแล้ว, API มี record ตรงกัน, **และ Core Banking ตัดยอดจริงหรือเปล่า** ถ้า 3 ชั้นไม่ตรงกัน → flag เป็น exception ทันที

การทำ DB verification ใน Playwright test คือการสร้าง "reconciliation layer" เดียวกันนี้ให้กับ automated test ของคุณ — ไม่ไว้ใจแค่ UI บอกว่าสำเร็จ แต่ตรวจว่า "Core Banking (DB)" บันทึกจริงหรือเปล่า

⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:

- **"ถ้า API return HTTP 200 แสดงว่า DB ถูกต้องเสมอ"** — ไม่จริง เหมือนธนาคารที่ API ตอบรับ request แล้ว แต่ส่งต่อไปให้ async job เขียน DB ซึ่งอาจ fail ทีหลัง HTTP 200 ส่งออกไปแล้ว หรือเกิด DB transaction rollback หลัง response — API บอกสำเร็จแต่ Core Banking ไม่มีข้อมูล

- **"UI แสดงข้อมูลถูกต้องแปลว่า DB มีข้อมูลนั้น"** — ไม่จริง เหมือน ATM screen แสดงยอดเงินจาก cache ที่ยังไม่ sync กับ Core Banking — UI อาจแสดงจาก optimistic update (แสดงผลล่วงหน้าก่อน DB จะ confirm) หรือจาก in-memory state ที่ยังไม่ได้ persist จริง

---

## 4. เนื้อหาหลัก

### 4.1 Pattern 1: API Read-back — ยืนยันผ่าน GET หลัง Action

ทำไมต้องมี pattern นี้? เพราะ UI action ส่ง HTTP request ไปที่ backend แต่คุณยังไม่รู้ว่า backend เขียน DB สำเร็จหรือเปล่า — UI แสดงผลได้จาก optimistic update หรือ local state ก็ได้ การ call GET API ซ้ำหลัง action คือการถามโดยตรงว่า "ตอนนี้ DB มีข้อมูลนี้อยู่หรือเปล่า?"

**API Read-back** คือ pattern ที่หลัง UI action เสร็จ คุณ call `request.get()` เพื่อดึงข้อมูลจาก API แล้ว verify ว่า record ที่ควรจะมีอยู่นั้นอยู่จริง Playwright ให้ `request` fixture มาพร้อมทุก test โดย isolate แยกกันอัตโนมัติ *(source: "Isolated [APIRequestContext] instance for each test.")*

```typescript
// partial example — full runnable version อยู่ใน Section 5
// tested: Playwright v1.50+, Node.js 20+
test('todo created via UI appears in database', async ({ page, request }) => {
  await page.goto('http://localhost:3000/todos');
  await page.getByTestId('input-new-todo').fill('Buy groceries');
  await page.getByTestId('btn-add-todo').click();

  // รอ UI update ก่อน
  await expect(page.getByTestId('todo-list')).toContainText('Buy groceries');

  // API Read-back: ยืนยันว่า DB มี record นี้จริง
  const response = await request.get('http://localhost:3000/api/todos');
  expect(response.ok()).toBeTruthy(); // status 200-299
  const todos = await response.json();
  expect(todos).toContainEqual(
    expect.objectContaining({ text: 'Buy groceries', completed: false })
  );
});
```

สังเกตว่าใช้ `toContainEqual` + `expect.objectContaining` ไม่ใช่ exact match แบบ `toEqual` กับ array ทั้งหมด เหตุผลคือ `id` และ `createdAt` เป็น dynamic values ที่เราไม่รู้ล่วงหน้า ถ้า assert ทั้ง array จะต้องรู้ค่าทุก field ทุก record รวมถึง record เก่าที่มีอยู่ก่อนแล้ว ซึ่งทำให้ test เปราะบาก `objectContaining` บอกว่า "ขอแค่ field ที่ระบุตรง ส่วน field อื่นไม่สนใจ" และ `toContainEqual` บอกว่า "ขอแค่หนึ่ง element ใน array ที่ match ก็พอ"

ใช้ pattern นี้เมื่อ: API มี GET endpoint สำหรับ resource ที่คุณต้องการ verify และ endpoint นั้น reflect state จาก DB จริง ไม่ใช่แค่ in-memory cache

---

### 4.2 Pattern 2: Direct File Read — อ่าน db.json โดยตรง

บางครั้ง API ไม่มี GET endpoint สำหรับ resource ที่คุณต้องการ verify — เช่น demo app ของเราไม่มี `GET /api/orders` แต่มีแค่ `POST /api/orders` ในกรณีแบบนี้ API Read-back ทำไม่ได้ ต้องใช้ **Direct File Read** แทน

Pattern นี้เหมาะสำหรับ file-based DB โดยเฉพาะ — อ่านไฟล์ db.json โดยตรงด้วย Node.js `fs.readFileSync` แล้ว parse JSON มา verify เอง ข้อดีคือตรงและเร็ว ข้อเสียคือ coupling กับ DB file path — ถ้าย้าย path ต้องแก้ test ด้วย

```typescript
// partial example — full runnable version อยู่ใน Section 5
// tested: Playwright v1.50+, Node.js 20+
import { readFileSync } from 'fs';
import { resolve } from 'path';

test('order appears in db.json after creation', async ({ request }) => {
  // Login เพื่อรับ token
  const loginRes = await request.post('http://localhost:3000/api/auth/login', {
    data: { username: 'admin', password: 'admin123' }
  });
  const { token } = await loginRes.json();

  // สร้าง order ผ่าน API
  const orderRes = await request.post('http://localhost:3000/api/orders', {
    headers: { Authorization: `Bearer ${token}` },
    data: { items: [{ productId: 1, quantity: 1 }] }
  });
  expect(orderRes.status()).toBe(201);
  const { orderId } = await orderRes.json();

  // Direct file read: อ่าน DB โดยตรง
  const dbPath = resolve('docs/playwright-typescript/playwright-course-app/data/db.json');
  const db = JSON.parse(readFileSync(dbPath, 'utf-8'));
  expect(db.orders).toContainEqual(
    expect.objectContaining({ orderId, status: 'confirmed' })
  );
});
```

ทำไม demo app ต้องใช้ direct file read สำหรับ orders? เพราะ app ไม่ expose `GET /api/orders` — ออกแบบมาเพื่อให้เห็น limitation ของ API-only approach และให้ฝึก pattern นี้

อีกเหตุผลที่ `readFileSync` (synchronous) ทำงานได้ถูกต้องตรงนี้: server.js ของ demo app ใช้ `writeFileSync` เพื่อเขียน DB เช่นกัน ซึ่งหมายความว่าเมื่อ HTTP response ส่งกลับมาแล้ว ไฟล์ db.json ถูก write เสร็จแน่นอน ไม่มี async write ค้างอยู่ ทำให้ sync read ได้ค่าที่ถูกต้องทันที

---

### 4.3 Pattern 3: Cross-layer Verification — ตรวจ 3 ชั้นในชุดเดียว

สองกรณีข้างต้นตรวจแค่ UI กับ DB ซึ่งดีกว่า UI-only แน่นอน แต่ยังมีช่องว่าง: UI action ส่งผลต่อ API state จริงไหม? และหลัง DB เปลี่ยน UI re-render สะท้อนค่าใหม่ถูกต้องไหม?

**Cross-layer Verification** คือ pattern ที่ตรวจครบ 4 จุดในชุดเดียว: UI action → API state → DB state → UI re-render ทำไมต้องครบ? เพราะแต่ละชั้นสามารถ fail แบบ silent โดยไม่กระทบชั้นอื่น เช่น UI toggle checkbox แต่ HTTP PATCH ไม่ถูก fire (Layer 1 pass แต่ Layer 2 fail), หรือ API อัปเดตสำเร็จแต่ UI ไม่ re-fetch ข้อมูลใหม่ (Layer 2 pass แต่ Layer 4 fail)

```typescript
// partial example — full runnable version อยู่ใน Section 5
// tested: Playwright v1.50+, Node.js 20+
test('completing todo updates all layers correctly', async ({ page, request }) => {
  // Setup: สร้าง todo ผ่าน API (เร็วกว่า UI)
  const createRes = await request.post('http://localhost:3000/api/todos', {
    data: { text: 'Layer verification test' }
  });
  const { id } = await createRes.json();

  // Layer 1: UI action — toggle complete
  await page.goto('http://localhost:3000/todos');
  await page.getByTestId(`todo-item-${id}`).locator('input[type="checkbox"]').check();

  // Layer 2: API verify — ตรวจว่า DB state เปลี่ยนผ่าน API
  const todosRes = await request.get('http://localhost:3000/api/todos');
  const todos = await todosRes.json();
  const updatedTodo = todos.find((t: { id: number }) => t.id === id);
  expect(updatedTodo?.completed).toBe(true);

  // Layer 3: UI re-render verify — ตรวจว่า UI สะท้อน DB state ใหม่
  await expect(page.getByTestId(`todo-item-${id}`)).toHaveClass(/completed/);
});
```

Pattern นี้ setup ด้วย `request.post()` แทน UI เพราะเร็วกว่าและไม่เกี่ยวข้องกับสิ่งที่กำลัง test *(source: "Sends HTTP(S) POST request and returns its response.")* — เราต้องการ test การ toggle ไม่ใช่การ create

trade-off ของ pattern นี้คือ test ยาวขึ้นและช้ากว่า unit test แต่ให้ confidence สูงสุดว่า entire stack ทำงานสอดคล้องกัน ใช้ pattern นี้เมื่อ feature สำคัญต่อ business (เช่น checkout, payment confirmation, user permission update) และ bug ในชั้นใดชั้นหนึ่งมีผลร้ายแรง

---

### 4.4 Pattern 4: Negative Verification — ยืนยันว่าลบจริง

**ปัญหาที่ pattern นี้แก้**: "soft delete" bug — UI แสดงว่าลบสำเร็จ แต่ record ยังคงอยู่ใน DB จริง เช่น developer ลืม commit transaction, ตั้ง `deleted_at` timestamp แทนที่จะลบ row จริง, หรือ cache ยังคืนค่าเก่า

ก่อนทำ negative verification ต้องรอให้ action เสร็จก่อนเสมอ — ในตัวอย่างนี้เรา delete ผ่าน API โดยตรงและตรวจสอบ `ok()` ก่อนค่อย query DB เพราะถ้า verify DB ทันทีโดยไม่รอ อาจ race กับ async delete และได้ false positive

```typescript
// partial example — full runnable version อยู่ใน Section 5
// tested: Playwright v1.50+, Node.js 20+
test('deleted todo is removed from database', async ({ page, request }) => {
  // Setup: สร้าง todo ผ่าน API
  const createRes = await request.post('http://localhost:3000/api/todos', {
    data: { text: 'Todo to delete' }
  });
  expect(createRes.status()).toBe(201);
  const { id } = await createRes.json();

  // Action: navigate ไปหน้า todos
  await page.goto('http://localhost:3000/todos');
  await expect(page.getByTestId(`todo-item-${id}`)).toBeVisible();

  // ลบผ่าน API (เพราะ demo app delete ผ่าน UI ต้องการ data-testid ที่ specific)
  const deleteRes = await request.delete(`http://localhost:3000/api/todos/${id}`);
  expect(deleteRes.ok()).toBeTruthy();

  // Negative DB Verification: ยืนยันว่าไม่อยู่ใน DB แล้ว
  const response = await request.get('http://localhost:3000/api/todos');
  const todos = await response.json();
  expect(todos).not.toContainEqual(expect.objectContaining({ id }));
});
```

`not.toContainEqual` ตรวจ negative state — ถ้า soft-delete logic ผิดและ record ยังอยู่ใน DB, test จะ fail ทันที pattern นี้ catch bug class ที่ UI testing ธรรมดาพลาดได้ง่ายที่สุด เพราะ UI แค่ซ่อน element ออกก็ผ่านแล้ว

---

### 4.5 Pattern 5: Async DB Polling — expect.poll() สำหรับ Async Writes

**ปัญหา**: บาง backend architecture return HTTP 200/201 ทันที แต่ DB write ยังไม่เสร็จ เช่น async job queue, message broker (Kafka/RabbitMQ), event sourcing, หรือ eventual consistency pattern

วิธีแก้แบบผิดคือ `await page.waitForTimeout(2000)` — เพราะเดาเวลาไม่ถูก ถ้า load สูงอาจยังไม่เสร็จ ถ้า load ต่ำก็รอโดยเปล่าประโยชน์

`expect.poll()` แก้ถูกต้องกว่า: *(source: "Polls the condition until it returns a truthy value. Will keep re-evaluating the condition until it times out.")* — retry ซ้ำอัตโนมัติจนกว่า assertion ผ่าน หรือหมด timeout

Default timeout คือ **5 วินาที** (ไม่ใช่ 30 วินาที), default intervals: `[100, 250, 500, 1000]` ms (exponential backoff) *(source: Playwright docs — expect.poll() options)*

```typescript
// partial example — full runnable version อยู่ใน Section 5
// tested: Playwright v1.50+, Node.js 20+
test('async operation eventually persists to database', async ({ request }) => {
  // Trigger operation ที่ async (simulate)
  await request.post('http://localhost:3000/api/todos', {
    data: { text: 'Async task' }
  });

  // expect.poll() — retry จนกว่า DB จะมีข้อมูล
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

ทำไม `expect.poll()` ดีกว่า `waitForTimeout()`:
- **ไม่ต้องเดาเวลา** — fail เร็วทันทีที่ state พร้อม แทนที่จะรอครบเวลา
- **Retry อัตโนมัติ** — intervals ปรับได้ตามลักษณะ async operation
- **Error message ชัด** — บอกว่า expected อะไร ได้อะไร หลัง timeout

ตัวอย่างเพิ่มเติม — ใช้ `expect.poll()` กับ admin stats เพื่อตรวจว่า todo count ถูกต้อง:

```typescript
// partial example
// tested: Playwright v1.50+, Node.js 20+
test('admin stats reflect current todo count', async ({ request }) => {
  // Login เพื่อรับ token
  const loginRes = await request.post('http://localhost:3000/api/auth/login', {
    data: { username: 'admin', password: 'admin123' }
  });
  const { token } = await loginRes.json();

  // สร้าง todo
  await request.post('http://localhost:3000/api/todos', { data: { text: 'Count test' } });

  // Poll admin stats จนกว่า count จะอัปเดต
  await expect.poll(async () => {
    const statsRes = await request.get('http://localhost:3000/api/admin', {
      headers: { Authorization: `Bearer ${token}` }
    });
    const { stats } = await statsRes.json();
    return stats.todos;
  }, { timeout: 5_000 }).toBeGreaterThan(0);
});
```

pattern นี้เหมาะเป็นพิเศษเมื่อ backend ใช้ background worker หรือ event-driven architecture ที่ persistence ไม่ synchronous กับ HTTP response

---

### 4.6 Pattern 6: DB Isolation — ป้องกัน Test Contamination

**ปัญหา**: เมื่อ tests รัน parallel (Playwright default), หลาย test แชร์ DB เดียวกัน — test A สร้าง record ที่ test B ไม่ได้คาดหวัง ทำให้เกิด intermittent failures ที่หา root cause ยากมาก

**Solution**: fixture ที่ reset DB ก่อนและหลังทุก test — สำคัญกว่า `afterEach` hook ธรรมดาตรงที่ fixture teardown (code หลัง `await use()`) **รันเสมอแม้ test throw error** ในขณะที่ `afterEach` อาจ skip ในบาง test runner scenario

```typescript
// partial example — full runnable version อยู่ใน Section 5
// tested: Playwright v1.50+, Node.js 20+
import { test as base } from '@playwright/test';

const test = base.extend({
  cleanDb: async ({ request }, use) => {
    // Setup: ล้าง DB ก่อน test
    await request.post('http://localhost:3000/api/reset');
    await use(undefined);
    // Teardown: ล้าง DB หลัง test — รันเสมอแม้ test fail
    await request.post('http://localhost:3000/api/reset');
  },
});

test('isolated test has clean database', async ({ cleanDb, request }) => {
  // ใน test นี้ DB เริ่มต้น empty todos เสมอ
  const todos = await (await request.get('http://localhost:3000/api/todos')).json();
  expect(todos).toHaveLength(0);
});
```

`POST /api/reset` คืนค่า `{ success: true }` และ reset todos array กลับเป็น empty พร้อม reset counter — ทำให้ทุก test เริ่มจาก clean state ที่คาดเดาได้

pattern นี้ถือเป็น best practice สำหรับ integration test ทุกชนิด ไม่ใช่แค่ Playwright — หลักการคือ test ต้องไม่พึ่งพา state จาก test อื่นและต้อง cleanup ตัวเองเสมอ

---

### 4.7 เปรียบเทียบกับ Robot Framework + Selenium

| ประเด็น | Robot Framework + Selenium | Playwright |
|---------|---------------------------|-----------|
| ตรวจ DB state | ต้องติดตั้ง DatabaseLibrary หรือ RequestsLibrary แยก ไม่ share session กับ browser | `request` fixture built-in, ใช้ร่วมกับ `page` ใน test เดียวกันได้ทันที |
| Async DB polling | ต้องเขียน custom keyword loop ที่ retry เอง หรือใช้ `Wait Until Keyword Succeeds` | `expect.poll()` built-in พร้อม configurable intervals และ timeout |
| Direct file read | ใช้ Python standard library `open()` ใน custom keyword | `import { readFileSync } from 'fs'` โดยตรงใน TypeScript test |
| DB isolation / cleanup | ต้องตั้ง Suite Setup/Teardown ใน `.robot` file — teardown อาจไม่รันถ้า Suite setup fail | Playwright fixture teardown (code หลัง `await use()`) รันเสมอแม้ test throw error |
| Cross-layer verify | ต้อง switch ระหว่าง SeleniumLibrary keyword และ RequestsLibrary keyword — state ไม่ share | `page` + `request` ใน test เดียวกัน share base URL, headers, และ context ได้ |
| Type safety | Python dict — ไม่มี type checking | TypeScript: `expect(todos[0]).toMatchObject<Partial<Todo>>({...})` |

จุดแตกต่างสำคัญที่สุดคือ Playwright รวม browser automation และ API testing ไว้ใน framework เดียว — ไม่ต้อง context switch ระหว่าง library, ไม่ต้อง manage library version แยก, และ `page` กับ `request` ใช้ base URL และ authentication context ร่วมกันได้ใน test เดียวกัน

---
