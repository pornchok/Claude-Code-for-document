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

ทำไมรอ UI update ก่อนถึงมั่นใจว่า DB เขียนแล้ว: demo app นี้ใช้ `writeFileSync` แบบ synchronous — server เขียน DB เสร็จก่อนส่ง HTTP response กลับ ดังนั้นเมื่อ browser ได้รับ response และ UI update แสดงว่า DB write สมบูรณ์แล้ว สำหรับ server ที่ใช้ async DB writes (เช่น background job หรือ message queue) ต้องใช้ `expect.poll()` แทน (ดู Pattern 5)

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
  // resolve() ใช้ process.cwd() เป็น base — ต้องรัน npx playwright test จาก repo root
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
  // ⚠️ Race condition: .check() ไม่รอ PATCH network request เสร็จ — Layer 2 อาจได้ stale data
  // สำหรับ robust code: ใช้ page.waitForResponse() หรือ expect.poll() (ดู Pattern 5)
  // ตัวอย่าง robust: await page.waitForResponse(r => r.url().includes('/api/todos/') && r.request().method() === 'PATCH')
  const todosRes = await request.get('http://localhost:3000/api/todos');
  const todos = await todosRes.json();
  const updatedTodo = todos.find((t: { id: number }) => t.id === id);
  expect(updatedTodo?.completed).toBe(true);

  // Layer 3: UI re-render verify — ตรวจว่า UI สะท้อน DB state ใหม่
  // ใช้ todo-text-{id} (span) ไม่ใช่ todo-item-{id} (li) — class "completed" อยู่ที่ span
  await expect(page.getByTestId(`todo-text-${id}`)).toHaveClass(/completed/);
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
| DB isolation / cleanup | ต้องตั้ง Test Setup/Teardown ใน `.robot` file — ถ้า Suite Setup fail ทุก tests ใน suite จะถูก skip และ Test Teardown ไม่รัน | Playwright fixture teardown (code หลัง `await use()`) รันเสมอแม้ test throw error (ยกเว้น timeout หมดก่อน teardown เสร็จ) |
| Cross-layer verify | ต้อง switch ระหว่าง SeleniumLibrary keyword และ RequestsLibrary keyword — state ไม่ share | `page` + `request` ใน test เดียวกัน share `baseURL` และ `extraHTTPHeaders` — แต่ไม่ share cookies โดยอัตโนมัติ (ใช้ `page.request` แทนถ้าต้องการ share cookies กับ browser) |
| Type safety | Python dict — ไม่มี type checking | TypeScript: `expect(todos[0]).toMatchObject<Partial<Todo>>({...})` |

จุดแตกต่างสำคัญที่สุดคือ Playwright รวม browser automation และ API testing ไว้ใน framework เดียว — ไม่ต้อง context switch ระหว่าง library, ไม่ต้อง manage library version แยก, และ `page` กับ `request` ใช้ `baseURL` และ `extraHTTPHeaders` ร่วมกันได้ใน test เดียวกัน

---

## 5. ตัวอย่าง

### Beginner

**กำลังทดสอบอะไร:** เมื่อสร้าง todo ผ่าน UI ทุก field ใน DB บันทึกถูกต้อง — ไม่ใช่แค่ record มีอยู่ แต่ตรวจว่า `text` ไม่ถูก trim ผิด, `completed` เริ่มต้นเป็น `false`, `id` และ `createdAt` ถูก generate ครบ

**ทำไม:** Bug แบบ "ข้อมูลมีอยู่แต่ค่าผิด" พบบ่อยกว่า "ข้อมูลหายไปเลย" — ถ้า test ตรวจแค่ว่า record exist แต่ไม่ตรวจ field values จะจับ bug นี้ไม่ได้เลย

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

test.beforeEach(async ({ request }) => {
  await request.post('http://localhost:3000/api/reset');
});

test('todo fields are saved correctly in database', async ({ page, request }) => {
  await page.goto('http://localhost:3000/todos');
  await page.getByTestId('input-new-todo').fill('Read Playwright docs');
  await page.getByTestId('btn-add-todo').click();

  // รอ UI confirm ก่อน — ป้องกัน race condition ที่ test วิ่งเร็วกว่า backend เขียน
  await expect(page.getByTestId('todo-list')).toContainText('Read Playwright docs');

  // API Read-back: verify ทุก field ไม่ใช่แค่ exist
  const res = await request.get('http://localhost:3000/api/todos');
  const todos = await res.json();

  expect(todos).toHaveLength(1);
  expect(todos[0]).toMatchObject({
    text: 'Read Playwright docs',
    completed: false,
  });
  // id และ createdAt ต้อง generate มา — ถ้าไม่มีแสดงว่า schema ผิด
  expect(todos[0].id).toBeDefined();
  expect(todos[0].createdAt).toBeDefined();
  // verify createdAt เป็น valid ISO 8601 format — ถ้า format ผิด new Date() จะ return NaN
  expect(new Date(todos[0].createdAt).toISOString()).toBe(todos[0].createdAt);
});
```

**สิ่งที่น่าสังเกต:**

1. `beforeEach` reset ด้วย `POST /api/reset` ทำให้ `toHaveLength(1)` assert ได้แน่นอน — ถ้าไม่ reset, test นี้จะ fail ครั้งที่สองเพราะมี record เก่าค้างอยู่
2. `toMatchObject` ตรวจแค่ field ที่ระบุ — `id` และ `createdAt` เป็น dynamic จึงตรวจแยกด้วย `toBeDefined()` และ ISO format check
3. การรอ UI confirm ก่อน read API ไม่ใช่แค่ UX pattern — มันเป็น synchronization barrier ที่ให้เวลา backend เขียน DB เสร็จก่อนที่ test จะ query

---

### Intermediate

**กำลังทดสอบอะไร:** เมื่อ user mark todo ว่า "complete" ใน UI — DB state ของ todo นั้นต้องเปลี่ยน, UI re-render ต้องสะท้อนความเปลี่ยนแปลง, **และ todo อื่นใน DB ต้องไม่โดนกระทบ** (negative check)

**ทำไม:** Bug "side effect" ที่ action หนึ่ง unintentionally แก้ record อื่นพร้อมกันพบได้เมื่อ update query ขาด WHERE clause — test ที่ตรวจแค่ target record จะจับ bug นี้ไม่ได้

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

test.beforeEach(async ({ request }) => {
  await request.post('http://localhost:3000/api/reset');
});

test('completing one todo updates DB without affecting others', async ({ page, request }) => {
  // Setup ผ่าน API: สร้าง 2 todos (เร็วกว่า UI 2 รอบ และ isolate ชัดเจนกว่า)
  const res1 = await request.post('http://localhost:3000/api/todos', {
    data: { text: 'Study TypeScript generics' }
  });
  const todo1 = await res1.json();

  const res2 = await request.post('http://localhost:3000/api/todos', {
    data: { text: 'Write unit tests' }
  });
  const todo2 = await res2.json();

  // Layer 1: UI action — complete todo1 เท่านั้น
  await page.goto('http://localhost:3000/todos');
  await page.getByTestId(`todo-item-${todo1.id}`).locator('input[type="checkbox"]').check();

  // Layer 2: ใช้ expect.poll() รอ PATCH เสร็จก่อน verify
  // (.check() trigger click แต่ไม่รอ PATCH network request — poll แก้ race condition นี้)
  await expect.poll(async () => {
    const res = await request.get('http://localhost:3000/api/todos');
    const ts = await res.json();
    return ts.find((t: { id: number }) => t.id === todo1.id)?.completed;
  }).toBe(true);

  // อ่าน DB อีกครั้งเพื่อ verify ทั้งสอง record ในคราวเดียว
  const todos = await (await request.get('http://localhost:3000/api/todos')).json();
  const updated2 = todos.find((t: { id: number }) => t.id === todo2.id);

  // Negative check: todo2 ต้องไม่โดนกระทบ
  expect(updated2).toBeDefined(); // guard ก่อน access property — ถ้า find() คืน undefined แสดงว่า record หาย
  expect(updated2!.completed).toBe(false);

  // Layer 3: UI re-render — สะท้อน DB state
  // class "completed" อยู่ที่ <span data-testid="todo-text-{id}"> ไม่ใช่ <li data-testid="todo-item-{id}">
  await expect(page.getByTestId(`todo-text-${todo1.id}`)).toHaveClass(/completed/);
  await expect(page.getByTestId(`todo-text-${todo2.id}`)).not.toHaveClass(/completed/);
});
```

**สิ่งที่น่าสังเกต:**

1. Setup ผ่าน API แทน UI ทั้งสองครั้ง — ทำให้ setup เร็วขึ้นและ test โฟกัสที่ verify behavior ไม่ใช่ setup behavior
2. Layer 2 ใช้ `expect.poll()` แทนการ call API โดยตรง — เพราะ `.check()` trigger click แต่ไม่รอ PATCH network request เสร็จ `expect.poll()` retry จนกว่าจะเห็น `completed: true` ใน DB ซึ่ง handle race condition ได้ถูกต้อง
3. Negative check (`updated2?.completed === false`) มีค่าเท่ากับ positive check — ถ้า backend มี bug ที่ mark todos ทั้งหมดพร้อมกัน positive check จะผ่านแต่ negative check จะ catch ได้
4. Layer 3 ใช้ `toHaveClass(/completed/)` บน `todo-text-{id}` (คือ `<span class="todo-text completed">`) — class "completed" อยู่ที่ span ไม่ใช่ที่ `<li>` ที่มีแค่ class `todo-item` เสมอ regex check ดีกว่า exact string เพราะ element อาจมีหลาย class พร้อมกัน

---

### Advanced

**กำลังทดสอบอะไร:** สร้าง reusable test fixture สำหรับ order verification ที่ต้องอ่าน db.json โดยตรง (เพราะไม่มี `GET /api/orders`) พร้อม TypeScript type definitions ที่ครบ และ automatic cleanup ทั้งก่อนและหลัง test

**ทำไม:** Test ที่ต้องการ authentication token + direct file read + cleanup ทุกครั้ง ถ้าไม่ abstract เป็น fixture จะมี boilerplate ซ้ำกันทุก test ทำให้ maintain ยากและ error-prone

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test as base, expect } from '@playwright/test';
import { readFileSync } from 'fs';
import { resolve } from 'path';

// --- Type Definitions ---

interface OrderItem {
  productId: number;
  quantity: number;
}

interface Order {
  orderId: string;
  status: string;
  items: OrderItem[];
  createdAt: string;
}

interface DbSnapshot {
  users: Array<{ id: number; username: string; password: string; role: string }>;
  products: Array<{ id: number; name: string; price: number; category: string; description: string; image: string }>;
  todos: Array<{ id: number; text: string; completed: boolean; createdAt: string }>;
  orders: Order[];
}

// --- Helper: อ่าน DB snapshot โดยตรงจากไฟล์ ---
// ใช้ sync read ได้เพราะ server.js ใช้ writeFileSync — HTTP response กลับมาแล้ว file เขียนเสร็จแน่นอน
function readDb(): DbSnapshot {
  const dbPath = resolve(
    'docs/playwright-typescript/playwright-course-app/data/db.json'
  );
  return JSON.parse(readFileSync(dbPath, 'utf-8')) as DbSnapshot;
}

// --- Custom Fixtures ---

type OrderFixtures = {
  adminToken: string;
  cleanOrders: void;
};

const test = base.extend<OrderFixtures>({
  // fixture: login อัตโนมัติ ทุก test ที่ใช้ adminToken จะได้ token พร้อมใช้ทันที
  adminToken: async ({ request }, use) => {
    const loginRes = await request.post('http://localhost:3000/api/auth/login', {
      data: { username: 'admin', password: 'admin123' }
    });
    expect(loginRes.status()).toBe(200);
    const { token } = await loginRes.json();
    await use(token);
    // ไม่มี teardown สำหรับ token — JWT stateless ไม่ต้อง revoke
  },

  // fixture: reset todos ก่อนและหลัง test เสมอ แม้ test จะ fail
  // ⚠️ ชื่อ cleanOrders อาจทำให้เข้าใจผิด — POST /api/reset ล้างแค่ todos ไม่ใช่ orders
  // (demo app ไม่มี DELETE /api/orders) ดังนั้น test ต้องใช้ orderCountBefore snapshot แทน
  cleanOrders: [async ({ request }, use) => {
    await request.post('http://localhost:3000/api/reset'); // ล้าง todos เท่านั้น
    await use();
    // Teardown รันเสมอแม้ test throw error — ป้องกัน test pollution
    await request.post('http://localhost:3000/api/reset');
  }, { auto: false }],
});

// --- Test ---

test('order created via API is persisted correctly in db.json', async ({
  request,
  adminToken,
  cleanOrders, // eslint-disable-line @typescript-eslint/no-unused-vars
}) => {
  // Snapshot ก่อน: บันทึก order count เป็น baseline
  const beforeDb = readDb();
  const orderCountBefore = beforeDb.orders.length;

  // สร้าง order ผ่าน API ที่ต้องการ authentication
  const orderRes = await request.post('http://localhost:3000/api/orders', {
    headers: { Authorization: `Bearer ${adminToken}` },
    data: { items: [{ productId: 2, quantity: 3 }] }
  });
  expect(orderRes.status()).toBe(201);
  const { orderId } = await orderRes.json();

  // Direct file read: verify order ถูก persist ลง db.json
  const afterDb = readDb();
  expect(afterDb.orders).toHaveLength(orderCountBefore + 1);

  const savedOrder = afterDb.orders.find(o => o.orderId === orderId);
  expect(savedOrder).toBeDefined();
  expect(savedOrder).toMatchObject({
    orderId,
    status: 'confirmed',
    items: [{ productId: 2, quantity: 3 }],
  });
  // createdAt ต้องเป็น valid ISO 8601 (ใช้ ! เพราะ toBeDefined() ข้างบนการันตีว่าไม่ใช่ undefined)
  expect(savedOrder!.createdAt).toBeDefined();
  expect(new Date(savedOrder!.createdAt).toISOString()).toBe(savedOrder!.createdAt);

  // Cross-verify ผ่าน Admin Stats API — orders count ต้องเพิ่มด้วย
  const statsRes = await request.get('http://localhost:3000/api/admin', {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  expect(statsRes.status()).toBe(200);
  const { stats } = await statsRes.json();
  expect(stats.orders).toBe(orderCountBefore + 1);
});
```

**สิ่งที่น่าสังเกต:**

1. **Snapshot pattern** (`orderCountBefore`) แทนที่จะ hardcode expect length เป็น 1 — ทำให้ test ทำงานถูกต้องแม้มี order เก่าค้างอยู่ใน DB จาก test run ก่อนหน้า (defensive against state leakage)
2. **Cross-verify สองชั้น** ในท้ายแบบเดียวกัน: direct file read verify ว่า record มีอยู่ใน storage จริง + Admin Stats API verify ว่า aggregate count ถูกต้อง — สองแหล่งข้อมูลอิสระที่ต้องตรงกัน
3. **`{ auto: false }`** บน `cleanOrders` fixture หมายความว่า fixture นี้รันเฉพาะเมื่อ test ประกาศใช้ชัดเจน ไม่รัน auto ทุก test — ป้องกัน reset ที่ไม่ตั้งใจสำหรับ test อื่นใน suite เดียวกัน

---

## 6. Common Mistakes

### Mistake 1: Verify UI แล้วไม่ตรวจ DB

❌ **ผิด:** Test ที่ตรวจแค่ว่า UI แสดง success message โดยไม่ verify DB เลย — นี่คือ test ที่อันตรายที่สุด เพราะผ่านได้แม้ข้อมูลไม่ถึง DB

```typescript
// ❌ ผิด
test('add todo', async ({ page }) => {
  await page.getByTestId('input-todo').fill('My task');
  await page.getByTestId('btn-add-todo').click();
  await expect(page.getByTestId('todo-list')).toContainText('My task');
  // จบโดยไม่ verify DB เลย — UI อาจ render จาก local state แทน
});
```

✅ **ถูก:** ต้อง read-back จาก API หลัง UI confirm เพื่อยืนยันว่าข้อมูลถึง DB จริง

```typescript
// ✅ ถูก
test('add todo', async ({ page, request }) => {
  await page.getByTestId('input-todo').fill('My task');
  await page.getByTestId('btn-add-todo').click();
  // รอ UI confirm ก่อน — ถ้า UI แสดงแล้ว server น่าจะตอบกลับแล้ว
  await expect(page.getByTestId('todo-list')).toContainText('My task');
  // Verify ว่า DB มีข้อมูลจริง ไม่ใช่แค่ UI state
  const todos = await request.get('http://localhost:3000/api/todos').then(r => r.json());
  expect(todos).toContainEqual(expect.objectContaining({ text: 'My task' }));
});
```

*(source: https://playwright.dev/docs/api-testing)*

---

### Mistake 2: ไม่รอ UI confirm ก่อน verify DB

❌ **ผิด:** Verify DB ทันทีหลัง click โดยไม่รอ UI — อาจ query DB ก่อน server เขียนเสร็จ ทำให้ test flaky

```typescript
// ❌ ผิด — race condition: DB อาจยังไม่มีข้อมูลตอนที่ query
test('add todo', async ({ page, request }) => {
  await page.getByTestId('btn-add-todo').click();
  // Query DB ทันทีโดยไม่รอ UI — server อาจยังประมวลผลอยู่
  const todos = await request.get('http://localhost:3000/api/todos').then(r => r.json());
  expect(todos).toContainEqual(expect.objectContaining({ text: 'My task' }));
});
```

✅ **ถูก:** รอ UI confirm ก่อนเสมอ — UI confirm หมายความว่า server response กลับมาแล้ว ซึ่งหมายความว่า server น่าจะเขียน DB เสร็จแล้ว

```typescript
// ✅ ถูก
test('add todo', async ({ page, request }) => {
  await page.getByTestId('btn-add-todo').click();
  // รอ UI confirm ก่อน — server response กลับมาแล้ว แปลว่า DB ควรมีข้อมูลแล้ว
  await expect(page.getByTestId('todo-list')).toContainText('My task');
  // ค่อย verify DB หลังจากนั้น
  const todos = await request.get('http://localhost:3000/api/todos').then(r => r.json());
  expect(todos).toContainEqual(expect.objectContaining({ text: 'My task' }));
});
```

*(source: https://playwright.dev/docs/test-assertions)*

---

### Mistake 3: ไม่ cleanup DB ระหว่าง tests

❌ **ผิด:** Tests แชร์ DB state ร่วมกัน — test ที่รันก่อนทิ้งข้อมูลไว้ให้ test ถัดไปเจอ ทำให้ผล assert ไม่น่าเชื่อถือ

```typescript
// ❌ ผิด — ไม่มี cleanup ระหว่าง tests
test('first test adds todo A', async ({ request }) => {
  await request.post('http://localhost:3000/api/todos', { data: { text: 'A' } });
  // test จบ แต่ข้อมูล 'A' ยังอยู่ใน DB
});

test('second test checks count', async ({ request }) => {
  const todos = await request.get('http://localhost:3000/api/todos').then(r => r.json());
  expect(todos).toHaveLength(1); // ❌ อาจเจอ 2 ถ้า first test ยังอยู่ใน DB
});
```

✅ **ถูก:** ใช้ `beforeEach` reset ทุกครั้งก่อน test รัน เพื่อให้แต่ละ test เริ่มจาก clean state เสมอ

```typescript
// ✅ ถูก
test.beforeEach(async ({ request }) => {
  // Reset DB ก่อนทุก test — แต่ละ test เริ่มจาก clean state
  await request.post('http://localhost:3000/api/reset');
});

test('second test checks count', async ({ request }) => {
  await request.post('http://localhost:3000/api/todos', { data: { text: 'B' } });
  const todos = await request.get('http://localhost:3000/api/todos').then(r => r.json());
  expect(todos).toHaveLength(1); // ✅ มั่นใจได้ว่า count ถูกต้อง
});
```

> ⚠️ **Parallel workers:** `beforeEach` reset ยังเพียงพอสำหรับ sequential tests แต่ถ้ารัน parallel ด้วย `workers > 1` workers หลายตัวอาจ call `/api/reset` ชนกัน ต้องใช้ fixture-based isolation พร้อม unique data prefix แทน (ดู Pattern 6 และ Ch19 L5 exercise)

*(source: https://playwright.dev/docs/best-practices)*

---

### Mistake 4: ใช้ exact JSON match กับ dynamic values

❌ **ผิด:** Assert ทั้ง object แบบ exact match รวม fields ที่ generate อัตโนมัติ — `id`, `createdAt`, `updatedAt` เปลี่ยนทุก run ทำให้ test fail เสมอ

```typescript
// ❌ ผิด — id และ createdAt เปลี่ยนทุก run
expect(todos).toEqual([
  { id: 1, text: 'My task', completed: false, createdAt: '2026-01-01T00:00:00.000Z' }
]);
```

✅ **ถูก:** ใช้ `objectContaining` เพื่อ assert เฉพาะ fields ที่เรา control และรู้ค่าล่วงหน้า ส่วน dynamic fields ให้ assert แค่ว่า "มีอยู่" หรือ "มี type ถูกต้อง"

```typescript
// ✅ ถูก — assert เฉพาะ fields ที่เรา control
expect(todos).toContainEqual(
  expect.objectContaining({ text: 'My task', completed: false })
);

// ถ้าต้องการตรวจ dynamic fields ด้วย ให้ assert แค่ type
const todo = todos.find((t: { text: string }) => t.text === 'My task');
expect(todo).toBeDefined(); // ตรวจก่อนว่า find() เจอ record จริง
expect(typeof todo!.id).toBe('number');
expect(new Date(todo!.createdAt).toISOString()).toBe(todo!.createdAt); // valid ISO string
```

*(source: https://playwright.dev/docs/test-assertions)*

---

## 7. สรุปบท

บทนี้สอน pattern สำคัญสำหรับการ verify ว่า data ถึง DB จริงหลังจาก user action:

- **3 patterns หลัก**: API Read-back (ใช้เมื่อมี GET endpoint), Direct File Read (ใช้เมื่อ demo app เก็บข้อมูลใน JSON file), Cross-layer Verification (ตรวจหลายชั้นพร้อมกัน) — แต่ละ pattern เหมาะกับ stack ที่ต่างกัน
- **ทำไม verify DB ถึงสำคัญ**: UI อาจแสดง success ได้จาก local state โดยที่ข้อมูลไม่เคยถึง DB — test ที่ตรวจแค่ UI จะไม่จับ bug ประเภทนี้ได้
- **`expect.poll()`** คือเครื่องมือสำหรับ async DB writes ที่ backend process หลังจาก response กลับแล้ว — poll ซ้ำจนครบ timeout แทนที่จะใช้ `waitForTimeout()` ที่ fragile
- **DB isolation**: `beforeEach` reset เหมาะสำหรับ sequential tests — สำหรับ parallel workers ต้องใช้ fixture-based isolation พร้อม unique data prefix เพื่อป้องกัน race condition บน DB เดียวกัน

---

**คำถาม (ตอบก่อนดูเฉลย):**

1. คุณเขียน test ที่ผู้ใช้สร้าง order ผ่าน UI และ UI แสดง "Order created!" — แต่คุณต้องการยืนยันว่า order ถูกบันทึกใน DB จริงๆ ด้วย อธิบายว่าจะใช้ pattern ไหน และทำไม

2. ระบบของคุณ process bulk import แบบ async — backend return 202 Accepted ทันที แต่ DB จะถูกเขียนหลังจากนั้น 1-3 วินาที คุณจะ verify ว่า DB มีข้อมูลครบอย่างไร โดยไม่ใช้ `waitForTimeout()`

3. Tests ของคุณรัน parallel ด้วย 4 workers และทุก test ที่ verify todo count เริ่ม fail intermittently บน CI — อธิบาย root cause และวิธีแก้

<details>
<summary>เฉลย</summary>

**ข้อ 1:** ตรวจสอบ API ก่อนว่ามี GET endpoint สำหรับ orders ไหม — ถ้ามีให้ใช้ **API Read-back pattern** เป็นตัวเลือกแรก เพราะ query ผ่าน application layer เดิมไม่ต้อง access storage โดยตรง

สำหรับ demo app นี้ **ไม่มี `GET /api/orders` endpoint** จึงต้องเลือกจาก 2 แนวทาง:
- **(a) Direct File Read** — อ่าน `db.json` ด้วย `fs.readFileSync` แล้ว parse หา order โดยตรง (ตรงที่สุด)
- **(b) Admin Stats API** — call `GET /api/admin` แล้วตรวจ `stats.orders` ว่าเพิ่มขึ้น (ตรวจ count เท่านั้น ไม่เห็น content)

approach (a) ดีกว่า (b) เมื่อต้องการ verify ว่าข้อมูลใน order ถูกต้อง เช่น `status:'confirmed'` และ items array ตรงกัน

*(source: https://playwright.dev/docs/api-testing)*

---

**ข้อ 2:** ใช้ **`expect.poll()`** แทน `waitForTimeout()` เพราะ poll ตรวจสอบ condition จริงๆ แล้วหยุดทันทีที่ผ่าน แทนที่จะรอเวลาตายตัวซึ่ง flaky

```typescript
await expect.poll(async () => {
  const res = await request.get('http://localhost:3000/api/import/items');
  const { items } = await res.json();
  return items.length;
}, {
  intervals: [500, 1000, 2000], // ลอง 500ms, 1000ms, 2000ms
  timeout: 10_000               // รอสูงสุด 10 วินาที
}).toBe(expectedCount);
```

Pattern นี้ทำงานได้เพราะ `expect.poll()` "Polls the condition until it returns a truthy value. Will keep re-evaluating the condition until it times out." — หยุดทันทีเมื่อ condition ผ่าน ไม่รอครบ timeout

*(source: https://playwright.dev/docs/test-assertions)*

---

**ข้อ 3:** **Root cause:** Tests แชร์ DB state ร่วมกัน — เมื่อ 4 workers รันพร้อมกัน workers ต่างๆ เขียนข้อมูลลง DB เดียวกันและอ่านข้อมูลทับกัน ทำให้ count ที่ assert ไม่ตรงกับที่คาดเพราะมีข้อมูลจาก workers อื่นปน

**วิธีแก้มี 2 แนวทาง:**

**แนวทาง 1 — beforeEach reset:** เพิ่ม `beforeEach` ที่ call reset endpoint ก่อนทุก test เพื่อ clear DB แต่ข้อเสียคือ tests ยังคง share DB อยู่ ถ้ามี test อื่นรันพร้อมกันจาก worker อื่นก็ยังชนกันได้

**แนวทาง 2 — Fixture-based isolation (แนะนำสำหรับ parallel):** ออกแบบ fixture ที่สร้าง isolated namespace สำหรับแต่ละ test เช่น prefix ทุก test ID ด้วย worker ID หรือ timestamp ทำให้แต่ละ test ทำงานกับชุดข้อมูลของตัวเองโดยไม่กระทบกัน

"Make tests as isolated as possible" — isolation ต้องทำระดับ test ไม่ใช่แค่ระดับ suite

*(source: https://playwright.dev/docs/best-practices)*

</details>
