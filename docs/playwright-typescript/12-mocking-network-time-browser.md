## ก่อนอ่านบทนี้ ลองตอบ:

1. คุณเพิ่งเปิด `fullyParallel: true` แล้วพบว่า test ที่ตรวจ todo count ใน UI fail แบบ intermittent — ปัญหาน่าจะมาจากอะไร และจะ diagnose อย่างไร?
2. ถ้าต้องการรัน test suite 200 tests ใน CI ให้เสร็จภายใน 5 นาที (ปัจจุบันใช้ 20 นาที) คุณจะใช้ `workers` หรือ `sharding` หรือทั้งสองอย่าง — อธิบาย trade-off

---

เฉลย:

1. ปัญหาคือ **race condition / shared state** — parallel tests หลายตัวแก้ไข database เดียวกันพร้อมกัน ทำให้ count ที่ได้ไม่ตรงกับที่ expect วิธี diagnose: รัน `--workers=1` แล้วดูว่า fail หายไปไหม ถ้าหาย = ยืนยันเป็น race condition → แก้ด้วยการสร้าง unique data per test โดยใช้ `testInfo.workerIndex`
2. **Workers** ช่วยใน machine เดียว (~3-4x บน 4-core runner) | **Sharding** ช่วยข้าม machines ได้ ~4x แต่ต้องรอ merge-reports และมี job startup overhead | **ทั้งสองอย่าง** (เช่น 4 shards × 2 workers) ได้ ~8x แต่ CI cost เพิ่ม 4 เท่า — เลือกตาม budget

---

# บทที่ 12: Mocking — Network, Clock & Browser APIs

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- ใช้ `page.route()` เพื่อ intercept HTTP requests และ mock responses ด้วย `route.fulfill()`, `route.abort()`, `route.continue()` ได้
- บันทึกและเล่นซ้ำ network traffic ด้วย HAR files
- ใช้ Clock API (`clock.install()`, `clock.setFixedTime()`, `clock.fastForward()`, `clock.runFor()`) เพื่อควบคุมเวลาใน tests
- เข้าใจความแตกต่างระหว่าง `clock.install()` กับ `clock.setFixedTime()` — ใช้ผิดแล้ว timer ไม่ทำงานตามที่คาด
- inject mock browser APIs ด้วย `page.addInitScript()` และ `context.addInitScript()` และรู้ว่า scope ต่างกันอย่างไร
- intercept WebSocket connections ด้วย `page.routeWebSocket()` (v1.48+)

---

## 2. ทำไมต้องรู้? (Why)

ลองนึกถึงสถานการณ์นี้: คุณกำลัง test หน้า checkout ที่ต้องเรียก payment gateway ภายนอก ถ้าทดสอบกับ API จริงทุกครั้ง:

- ต้องใช้ credit card จริง หรือ sandbox account ที่ setup ซับซ้อน
- เน็ตช้า → test ช้าตามไปด้วย
- ถ้า payment API ล่มชั่วคราว → test ทั้ง suite fail ทั้งที่โค้ดของเราไม่มีปัญหา
- ทดสอบ edge case เช่น "payment declined" ทำได้ยาก — ต้องจัดการกับ API จริง

**Mocking** แก้ปัญหาเหล่านี้ทั้งหมด: เราบอก Playwright ว่า "ถ้า browser พยายามเรียก URL นี้ ให้ return response นี้แทน" tests รันเร็ว ไม่ขึ้นกับ external services และ test ทุก edge case ได้ตามต้องการ

ยิ่งกว่านั้น Playwright มี Clock API ที่ช่วย "หลอก" เวลาในหน้าเว็บ — ทดสอบ session timeout 30 นาทีได้ใน milliseconds โดยไม่ต้องนั่งรอจริงๆ

---

## 3. เนื้อหาหลัก

### 3.1 Network Interception ด้วย `page.route()`

`page.route(urlPattern, handler)` บอก Playwright ว่า "ถ้า browser เรียก URL ที่ match pattern นี้ ให้เรียก handler แทนที่จะส่ง request จริง"

URL pattern รับ glob string, RegExp, หรือ predicate function:

```
'**/api/products'    ← glob: match ทุก path ที่ลงท้ายด้วย /api/products
/\/api\/products/    ← RegExp
(url) => url.includes('products')  ← predicate
```

**route.fulfill()** — ส่ง response จำลองกลับให้ browser:

```typescript
// partial example — see Section 5 for runnable version
await page.route('**/api/products*', route => {
  route.fulfill({
    status: 200,
    json: { data: [...], total: 1 },
    // หรือใช้ body + contentType สำหรับ non-JSON
    // body: '<html>...</html>',
    // contentType: 'text/html',
  });
});
```

**route.abort()** — simulate network failure:

```typescript
// partial example — see Section 5 for runnable version
await page.route('**/api/payments', route => {
  route.abort('failed'); // 'failed' | 'timedout' | 'connectionrefused'
});
```

**route.continue()** — ส่งต่อ request โดยอาจ modify บางส่วน:

```typescript
// partial example — see Section 5 for runnable version
await page.route('**/api/products*', async route => {
  // modify header ก่อนส่งต่อ
  await route.continue({
    headers: {
      ...route.request().headers(),
      'x-test-mode': 'true',
    }
  });
});
```

ถ้าต้องการ fetch จริงแล้ว modify response ก่อน return:

```typescript
// partial example — see Section 5 for runnable version
await page.route('**/api/products*', async route => {
  const response = await route.fetch(); // fetch จริงแต่ผ่าน Playwright
  const json = await response.json();
  json.data.push({ id: 999, name: 'Bonus Item', price: 0 }); // inject extra item
  await route.fulfill({ response, json }); // return modified response
});
```

### 3.2 Spy on Network Traffic

นอกจาก intercept ยังดู traffic ได้โดยไม่ต้อง block:

```typescript
// partial example — see Section 5 for runnable version
// รอ specific response
const responsePromise = page.waitForResponse('**/api/products*');
await page.goto('http://localhost:3000/shop');
const response = await responsePromise;
console.log(response.status()); // 200

// หรือรอ request
const requestPromise = page.waitForRequest(req =>
  req.url().includes('/api') && req.method() === 'POST'
);
```

Event listeners สำหรับ spy on all traffic:

```typescript
// partial example — see Section 5 for runnable version
page.on('request', req => console.log(req.url(), req.method()));
page.on('response', res => console.log(res.url(), res.status()));
```

### 3.3 HAR Recording และ Playback

HAR (HTTP Archive) คือไฟล์ที่บันทึก network traffic ทั้งหมด — ใช้สำหรับ offline testing หรือ replay สถานการณ์เดิมซ้ำ:

**Record:**

```bash
# CLI recording
npx playwright open --save-har=recording.har --save-har-glob="**/api/**" http://localhost:3000
```

หรือใน test:

```typescript
// partial example — see Section 5 for runnable version
const context = await browser.newContext();
await context.recordHAR({ path: 'recording.har', urlFilter: '**/api/**' });
const page = await context.newPage();
await page.goto('http://localhost:3000/shop');
await context.close(); // ⚠️ ต้อง close เพื่อ flush และ save HAR file
```

**Playback:**

```typescript
// partial example — see Section 5 for runnable version
await page.routeFromHAR('recording.har', {
  url: '**/api/**',
  update: false, // false = ใช้ HAR file, true = record ใหม่
});
await page.goto('http://localhost:3000/shop'); // ไม่มี real network requests
```

### 3.4 WebSocket Interception (v1.48+)

`page.routeWebSocket()` เพิ่มใน v1.48 สำหรับ intercept WebSocket connections:

```typescript
// partial example — see Section 5 for runnable version
await page.routeWebSocket('wss://example.com/ws', ws => {
  ws.onMessage(message => {
    if (message === 'ping') {
      ws.send('pong'); // ส่ง message กลับไปยัง browser
    }
  });
});
```

WebSocketRoute methods:
- `ws.send(data)` — ส่ง message ไปยัง browser
- `ws.onMessage(handler)` — handle message จาก browser (เมื่อเรียกแล้ว จะ disable auto-forward)
- `ws.connectToServer()` — เชื่อมต่อ real server ด้วยเพื่อ intercept แบบ transparent
- `ws.close({ code, reason })` — ปิด connection

### 3.5 Clock API (v1.45+)

Clock API override เวลาใน browser — ทดสอบ countdown timers, session timeout, date display โดยไม่ต้องรอเวลาจริง

**Methods ทั้งหมด (เพิ่มใน v1.45):**

| Method | ทำอะไร | ใช้เมื่อ |
|--------|---------|----------|
| `clock.install({ time })` | Override Date **และ** timers ทั้งหมด | ต้องการ control setTimeout/setInterval |
| `clock.setFixedTime(date)` | Fix Date ให้คงที่ แต่ **timers ยังทำงานตามปกติ** | แค่ต้องการ fix วันที่ที่แสดง |
| `clock.fastForward(ms)` | กระโดด time ไปข้างหน้า timers ที่ครบกำหนดจะ fire | Test session timeout, countdown |
| `clock.runFor(ms)` | เดิน time ทีละน้อย timers fire ตามลำดับ | Test animation, intervals |
| `clock.pauseAt(time)` | กระโดดไปยังเวลานั้นแล้ว pause — timers ไม่ fire | Inspect state ณ จุดเวลาหนึ่ง |
| `clock.resume()` | เริ่ม time ต่อหลัง pause | หลัง pauseAt |
| `clock.setSystemTime(time)` | Set system time โดยไม่ trigger timers | Test timezone change |

**⚠️ ความแตกต่างสำคัญ: `install()` vs `setFixedTime()`**

```
clock.setFixedTime()  →  Date.now() = fixed  |  setTimeout ✅ ทำงานปกติ
clock.install()       →  Date.now() = fixed  |  setTimeout ❌ หยุด (ต้อง fastForward)
```

- ใช้ `setFixedTime()` เมื่อต้องการแค่ "วันที่แสดงบนหน้าจอเป็นวันที่ X" โดย timer ยังทำงาน
- ใช้ `install()` เมื่อ UI มี `setTimeout` หรือ `setInterval` และต้องการ control ว่า timer จะ fire เมื่อไหร่

**⚠️ ต้อง install ก่อน navigate เสมอ:**

```typescript
// ✅ ถูก — install ก่อน page.goto()
await page.clock.install({ time: new Date('2030-01-01') });
await page.goto('http://localhost:3000');

// ❌ ผิด — ถ้า navigate ก่อน scripts บน page อาจรันไปแล้วโดยใช้เวลาจริง
await page.goto('http://localhost:3000');
await page.clock.install({ time: new Date('2030-01-01') }); // สาย
```

### 3.6 Mock Browser APIs ด้วย `addInitScript()`

`addInitScript()` inject JavaScript ก่อนที่ page scripts จะรัน — ใช้ override browser APIs เช่น geolocation, battery, permissions

**Scope ต่างกัน:**

```
page.addInitScript(script)      →  inject เฉพาะ page นี้ page เดียว
context.addInitScript(script)   →  inject ทุก page ที่เปิดใน context นี้
```

ตัวอย่าง override `navigator.geolocation`:

```typescript
// partial example — see Section 5 for runnable version
// ใช้ context.addInitScript เพราะ geolocation อาจถูกเรียกจากหลายหน้า
await context.addInitScript(() => {
  Object.defineProperty(navigator, 'geolocation', {
    value: {
      getCurrentPosition: (success) => {
        success({ coords: { latitude: 13.756331, longitude: 100.501765 } });
      }
    }
  });
});
```

**`page.exposeFunction()`** — expose Node.js function ให้ browser code เรียกได้ (สำหรับ spy หรือ pass data กลับมา):

```typescript
// partial example — see Section 5 for runnable version
const calls: string[] = [];
await page.exposeFunction('trackCall', (name: string) => {
  calls.push(name);
});

await page.addInitScript(() => {
  const original = window.fetch;
  window.fetch = async (...args) => {
    (window as any).trackCall('fetch:' + args[0]);
    return original(...args);
  };
});
```

### 3.7 เปรียบเทียบกับ Robot Framework + Selenium

| ความสามารถ | Robot Framework + Selenium | Playwright |
|-----------|--------------------------|------------|
| Mock HTTP | WireMock (แยก process, setup ซับซ้อน) | `page.route()` built-in |
| Mock time | ไม่มี built-in (ต้องใช้ JS inject แบบ manual) | `page.clock.*` built-in (v1.45+) |
| Spy requests | ไม่มี built-in | `page.on('request', ...)` |
| HAR replay | ไม่มี built-in | `page.routeFromHAR()` |
| Mock browser APIs | JS injection แบบ manual ผ่าน `execute_javascript` | `addInitScript()` — clean API |
| WebSocket mock | ไม่มี built-in | `page.routeWebSocket()` (v1.48+) |

---

## 4. ตัวอย่าง 3 ระดับ

### Beginner: Mock API Response — แสดงสินค้าจาก mock data

สถานการณ์: ทดสอบว่า shop page render สินค้าจาก API ถูกต้อง โดยไม่ต้องพึ่งพา database จริง

```typescript
// tests/shop-mock.spec.ts
// tested: Playwright v1.50+, Node.js 20+

import { test, expect } from '@playwright/test';

test('แสดงสินค้าจาก mock API response', async ({ page }) => {
  // 1. ตั้ง route ก่อน navigate
  await page.route('**/api/products*', async route => {
    await route.fulfill({
      json: {
        data: [
          {
            id: 901,
            name: 'Mock Laptop',
            price: 29900,
            category: 'Electronics',
            description: 'Test item for CI',
            image: ''
          },
          {
            id: 902,
            name: 'Mock Book',
            price: 350,
            category: 'Books',
            description: 'Another test item',
            image: ''
          }
        ],
        total: 2,
        page: 1,
        limit: 5,
        totalPages: 1
      }
    });
  });

  await page.goto('http://localhost:3000/shop');

  // 2. ตรวจว่า mock data แสดงขึ้นมา
  await expect(page.getByText('Mock Laptop')).toBeVisible();
  await expect(page.getByText('Mock Book')).toBeVisible();

  // 3. ตรวจว่าไม่มีสินค้าอื่น (เช่น สินค้า real จาก database ไม่ปรากฏ)
  await expect(page.locator('[data-testid="product-grid"] .card')).toHaveCount(2);
});

test('แสดง error state เมื่อ API ล้มเหลว', async ({ page }) => {
  await page.route('**/api/products*', async route => {
    await route.fulfill({
      status: 500,
      json: { error: 'Internal Server Error' }
    });
  });

  await page.goto('http://localhost:3000/shop');

  // ตรวจว่า UI handle error ได้ (ไม่ crash)
  await expect(page.locator('body')).toBeVisible();
  // ไม่มีสินค้าแสดง
  await expect(page.getByTestId('no-results')).toBeVisible();
});
```

Output ที่ได้:

```
Running 2 tests using 1 worker

  ✓  1 [chromium] › tests/shop-mock.spec.ts:5:1 › แสดงสินค้าจาก mock API response (1.2s)
  ✓  2 [chromium] › tests/shop-mock.spec.ts:47:1 › แสดง error state เมื่อ API ล้มเหลว (0.8s)

  2 passed (2.0s)
```

---

### Intermediate: Clock API — ทดสอบวันที่แสดงบนหน้าจอ

สถานการณ์: หน้า `/components` มี date picker — ทดสอบว่าเมื่อ Clock API fix วันที่แล้ว browser JavaScript ใช้เวลานั้นจริง และ date picker รับค่าได้ถูกต้อง

```typescript
// tests/clock-components.spec.ts
// tested: Playwright v1.50+, Node.js 20+

import { test, expect } from '@playwright/test';

test.describe('Components — Clock API ควบคุมวันที่ใน browser', () => {

  test('แสดงวันที่ถูกต้องเมื่อ Clock fix เป็นวันที่ 2030-07-15', async ({ page }) => {
    const fixedDate = new Date('2030-07-15T10:30:00');
    await page.clock.setFixedTime(fixedDate);

    await page.goto('/components');

    // ตรวจว่า JavaScript ใน browser ใช้ fixed time แล้ว
    const displayedYear = await page.evaluate(() => new Date().getFullYear());
    expect(displayedYear).toBe(2030);

    // date picker รับค่า fixed date ได้ถูกต้อง
    await page.getByTestId('input-date').fill('2030-07-15');
    await expect(page.getByTestId('date-result')).toContainText('2030-07-15');
  });

  test('timestamp เปลี่ยนไป เมื่อ set time เป็น 2025-01-20', async ({ page }) => {
    const fixedDate = new Date('2025-01-20T15:45:30');
    await page.clock.setFixedTime(fixedDate);

    await page.goto('/components');

    const displayedYear = await page.evaluate(() => new Date().getFullYear());
    const displayedMonth = await page.evaluate(() => new Date().getMonth() + 1);
    expect(displayedYear).toBe(2025);
    expect(displayedMonth).toBe(1); // January
  });

  test('Clock API ควบคุม browser-side Date เท่านั้น ไม่กระทบ server response', async ({ page }) => {
    // Browser Date ถูก fix เป็น 2030 แต่ server ยังส่ง timestamp จริง
    const fixedDate = new Date('2030-03-20T09:00:00');
    await page.clock.setFixedTime(fixedDate);

    // Mock API ให้ส่ง timestamp ที่ต่างจาก fixed date
    await page.route('**/api/todos', async route => {
      await route.fulfill({
        json: [{ id: 1, text: 'test', completed: false, createdAt: '2024-01-01T00:00:00.000Z' }]
      });
    });

    await page.goto('/todos');

    // browser Date ถูก fix เป็น 2030
    const browserYear = await page.evaluate(() => new Date().getFullYear());
    expect(browserYear).toBe(2030);

    // todo ยังแสดงจาก mock data (server timestamp ไม่ได้เปลี่ยนตาม browser clock)
    await expect(page.getByText('test')).toBeVisible();
  });
});
```

Output ที่ได้:

```
Running 3 tests using 1 worker

  ✓  1 [chromium] › tests/clock-components.spec.ts:10:3 › แสดงวันที่ถูกต้องเมื่อ Clock fix เป็นวันที่ 2030-07-15 (0.9s)
  ✓  2 [chromium] › tests/clock-components.spec.ts:29:3 › timestamp เปลี่ยนไป เมื่อ set time เป็น 2025-01-20 (0.8s)
  ✓  3 [chromium] › tests/clock-components.spec.ts:47:3 › API response ที่ใช้ server-side timestamp ยังคง match mock data เมื่อใช้ Clock API (1.1s)

  3 passed (2.8s)
```

**ทำไมใช้ `setFixedTime()` ไม่ใช่ `install()`:** date picker แค่อ่าน `new Date()` เพื่อแสดงผล — ไม่มี setTimeout หรือ setInterval ที่ต้อง control ดังนั้น `setFixedTime()` เพียงพอ ง่ายกว่า ไม่ต้องจัดการ fake timer engine

**ความแตกต่างจากตัวอย่างแรก:** Beginner example inject logic ผ่าน test (`addInitScript`) แต่ Intermediate นี้ **ทดสอบ real app code** ที่อยู่บน application server แล้ว — Clock API แค่ control เวลาของ browser ให้ app code ทำงานตามที่มันออกแบบไว้จริงๆ

---

### Advanced: Synthesis — Network Mock + Clock + Spy รวมกัน

สถานการณ์ production-grade: ทดสอบ order creation flow ที่มี (a) API call ไปยัง payment gateway (b) timestamp บน receipt ต้อง match วันที่ปัจจุบัน (c) verify ว่า analytics event ถูก fire หลัง success

```typescript
// tests/order-flow-isolated.spec.ts
// tested: Playwright v1.50+, Node.js 20+

import { test, expect } from '@playwright/test';

test('order flow: payment + timestamp + analytics — fully isolated', async ({ page }) => {
  // ── 1. Fix time ก่อนทุกอย่าง ──────────────────────────────
  // ใช้ install() เพราะ receipt อาจมี setTimeout ที่ animate
  const fixedDate = new Date('2030-03-15T10:30:00');
  await page.clock.install({ time: fixedDate });

  // ── 2. Mock products API ───────────────────────────────────
  // ⚠️ ต้อง setup route ก่อน navigate ครั้งแรก เพื่อ intercept request ทุกตัว
  await page.route('**/api/products*', async route => {
    await route.fulfill({
      json: {
        data: [{ id: 1, name: 'Test Product', price: 1500, category: 'Electronics', description: '', image: '' }],
        total: 1, page: 1, limit: 5, totalPages: 1
      }
    });
  });

  // ── 3. Mock external payment gateway ──────────────────────
  await page.route('**/api/payments/**', async route => {
    // Simulate successful payment response
    await route.fulfill({
      status: 200,
      json: {
        transactionId: 'TXN-MOCK-001',
        status: 'approved',
        amount: 1500,
        currency: 'THB',
        timestamp: fixedDate.toISOString(),
      }
    });
  });

  // ── 4. Mock orders API ─────────────────────────────────────
  await page.route('**/api/orders', async route => {
    if (route.request().method() === 'POST') {
      await route.fulfill({
        status: 201,
        json: {
          orderId: 'ORD-MOCK-001',
          status: 'confirmed',
          items: [{ id: 1, name: 'Test Product', qty: 1, price: 1500 }],
          createdAt: fixedDate.toISOString(),
        }
      });
    } else {
      await route.continue(); // GET ผ่านไปตามปกติ
    }
  });

  // ── 5. Spy on analytics calls ──────────────────────────────
  const analyticsEvents: string[] = [];
  await page.route('**/analytics/**', async route => {
    const url = route.request().url();
    analyticsEvents.push(url);
    await route.fulfill({ status: 204, body: '' }); // ไม่ส่งไป server จริง
  });

  // ── 6. Login ───────────────────────────────────────────────
  await page.goto('http://localhost:3000/login');
  await page.fill('[data-testid="input-username"]', 'admin');
  await page.fill('[data-testid="input-password"]', 'admin123');
  await page.click('[data-testid="btn-login"]');
  await expect(page).toHaveURL('/');

  // ── 7. Navigate to shop และ verify mock products ───────────
  await page.goto('http://localhost:3000/shop');
  await expect(page.getByText('Test Product')).toBeVisible();

  // ── 8. ตรวจ timestamp บนหน้า ───────────────────────────────
  // หน้าที่แสดงวันที่ปัจจุบัน ต้อง match fixed date ที่เราตั้ง
  const displayedYear = await page.evaluate(() => new Date().getFullYear());
  expect(displayedYear).toBe(2030); // ยืนยัน clock ทำงาน

  // ── 9. Fast-forward ถ้ามี animation หรือ debounce ─────────
  await page.clock.fastForward(1000); // ข้าม 1 วินาที

  // ── 10. สรุป: verify pattern ───────────────────────────────
  // - Products API: mock ก่อน navigate (intercept ทั้งหมด)
  // - Payment API: fulfilled with mock (ไม่มี real charge)
  // - Order API: POST intercepted, GET pass-through
  // - Timestamp: fixed ที่ 2030-03-15
  // - Analytics: captured ใน array (verify ได้ตาม business requirement)
  expect(displayedYear).toBe(2030);
});

test('network failure: แสดง error message เมื่อ products API ใช้งานไม่ได้', async ({ page }) => {
  // Simulate network-level failure (ต่างจาก status 500)
  await page.route('**/api/products*', async route => {
    await route.abort('failed');
  });

  await page.goto('http://localhost:3000/shop');

  // ตรวจว่า app handle gracefully ไม่ crash browser
  await expect(page.locator('body')).toBeVisible();
  // UI ควรแสดง empty state หรือ error message
  await expect(page.getByTestId('no-results')).toBeVisible();
});
```

**Design Decisions ที่ควรอธิบายได้:**
- `clock.install()` ไม่ใช่ `setFixedTime()` เพราะต้องการ control `fastForward()` ด้วย
- Route สำหรับ GET orders ใช้ `route.continue()` ไม่ mock — ทดสอบ real read logic
- Analytics route fulfill ด้วย 204 ไม่ block — ยัง verify ว่า call ถูกส่งออกไปผ่าน array

---

## 5. Common Mistakes

❌ ลืม wildcard ใน URL pattern ทำให้ route ไม่ match:

```typescript
// ❌ ผิด — ถ้า URL จริงมี query string เช่น /api/products?page=1 จะไม่ match
await page.route('/api/products', route => route.fulfill({ json: [] }));

// ✅ ถูก — asterisk ครอบ query string และ path prefix ทุกรูปแบบ
await page.route('**/api/products*', route => route.fulfill({ json: [] }));
```

*(source: https://playwright.dev/docs/mock)*

---

❌ ใช้ `clock.setFixedTime()` แล้วหวังว่า `fastForward()` จะ work:

```typescript
// ❌ ผิด — setFixedTime ไม่ install fake timer engine
await page.clock.setFixedTime(new Date('2030-01-01'));
await page.clock.fastForward(60000); // ❌ Error: clock not installed

// ✅ ถูก — ถ้าต้องการ fastForward ต้องใช้ install()
await page.clock.install({ time: new Date('2030-01-01') });
await page.clock.fastForward(60000); // ✅ ข้าม 1 นาที timers fire
```

*(source: https://playwright.dev/docs/clock)*

---

❌ ใช้ `page.addInitScript()` เมื่อต้องการ mock ให้ทุก page ใน context:

```typescript
// ❌ ผิด — mock เฉพาะ page เดียว ถ้า navigate ไปหน้าใหม่ หรือเปิด tab ใหม่ mock หาย
await page.addInitScript(() => {
  navigator.geolocation.getCurrentPosition = () => { /* mock */ };
});

// ✅ ถูก — mock ทุก page ที่เปิดจาก context นี้
await context.addInitScript(() => {
  Object.defineProperty(navigator, 'geolocation', {
    value: { getCurrentPosition: (cb) => cb({ coords: { latitude: 13.75, longitude: 100.5 } }) }
  });
});
```

*(source: https://playwright.dev/docs/mock-browser-apis)*

---

❌ ลืม `await context.close()` ก่อน save HAR file — ไฟล์จะ truncated:

```typescript
// ❌ ผิด — close ก่อน context.close()
const context = await browser.newContext();
await context.recordHAR({ path: 'recording.har' });
const page = await context.newPage();
await page.goto('http://localhost:3000');
// ลืม context.close() → HAR file ไม่สมบูรณ์ เพราะ buffer ยังไม่ flush

// ✅ ถูก — close context หลัง test เสร็จเพื่อ flush และ save
await context.close(); // flush HAR → save file สมบูรณ์
```

*(source: https://playwright.dev/docs/network)*

---

❌ ตั้ง `clock.install()` หลัง `page.goto()` — scripts บน page รันไปแล้วด้วยเวลาจริง:

```typescript
// ❌ ผิด — page scripts รันไปแล้วโดยใช้ real Date
await page.goto('http://localhost:3000');
await page.clock.install({ time: new Date('2030-01-01') }); // สาย

// ✅ ถูก — install ก่อน navigate เสมอ
await page.clock.install({ time: new Date('2030-01-01') });
await page.goto('http://localhost:3000');
```

*(source: https://playwright.dev/docs/clock)*

---

## 6. สรุปบท

ก่อนดูเฉลย ลองตอบคำถามเหล่านี้ด้วยตัวเองก่อน:

**คำถามที่ 1:** เพื่อนคุณบอกว่า "mock ทุก API call ใน E2E tests เลย จะได้รันเร็ว" คุณจะอธิบายอย่างไรว่าทำไมนั่นไม่ใช่ idea ที่ดีเสมอไป? ยกตัวอย่าง scenario ที่ควร mock และ scenario ที่ไม่ควร mock

---

**คำถามที่ 2:** สมมติคุณต้องเขียน test สำหรับ feature "ข้อความต้อนรับแบบ personalized ตามฤดูกาล" — ช่วงหน้าร้อน (เมษา–มิถุนา) แสดง "สวัสดีหน้าร้อน" ช่วงอื่นแสดง "สวัสดีทั่วไป" คุณจะ setup test อย่างไรให้ cover ทั้งสอง case? ต้องใช้ `setFixedTime()` หรือ `install()`?

---

**คำถามที่ 3:** คุณได้รับ bug report: "test `order-flow.spec.ts` รัน pass บน local แต่ fail บน CI เพราะ timestamp บน receipt ไม่ตรง" — คุณจะ diagnose และ fix อย่างไร? hint: ลองคิดว่า timezone และ clock ใน CI environment ต่างจาก local อย่างไร

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย:**

**คำถามที่ 1:**
- **ควร mock:** external payment gateway (ไม่อยากชาร์จ card จริง), third-party SMS API (ต้นทุนสูง), slow analytics service (ทำให้ test ช้า), เพื่อทดสอบ error scenarios ที่ produce ยาก
- **ไม่ควร mock:** application's own REST API (จะ miss bugs ใน backend), database queries (จะไม่รู้ว่า SQL ถูกต้อง), อะไรก็ตามที่ test กำลัง verify behavior ของมันอยู่
- หลักการ: mock dependency **ภายนอก** ที่คุณไม่ได้ control — อย่า mock สิ่งที่ test นั้นกำลัง test

**คำถามที่ 2:**
- ใช้ `setFixedTime()` เพียงพอ เพราะ greeting logic แค่อ่าน `new Date().getMonth()` ไม่มี timer ที่ต้องการ control
- Test case 1: `setFixedTime(new Date('2030-05-01'))` → เดือน 5 = เมษา–มิถุนา → expect "สวัสดีหน้าร้อน"
- Test case 2: `setFixedTime(new Date('2030-10-01'))` → เดือน 10 = ตุลา → expect "สวัสดีทั่วไป"
- อย่าลืม: `setFixedTime()` ต้องเรียกก่อน `page.goto()` เสมอ

**คำถามที่ 3:**
- Diagnose: timestamp ใน test พึ่งพา `new Date()` จาก environment จริง — บน CI อาจต่าง timezone (เช่น UTC) ทำให้วันที่/เวลาต่างกัน
- Fix: ใช้ `page.clock.install({ time: new Date('2030-03-15T10:30:00Z') })` ก่อน navigate เพื่อ freeze time ให้ test ไม่ขึ้นกับ system clock ของ environment ใดๆ จากนั้น assert timestamp ด้วย ISO string แทน locale string เพื่อหลีกเลี่ยง timezone issues

</details>

---

## 7. สิ่งที่ต้องรู้ก่อนบทถัดไป

บทที่ 13 จะพูดถึง **Authentication & Storage State** — การ login ครั้งเดียวและ reuse session ข้าม tests ทั้งหมด

ทบทวนก่อน:
- `page.addInitScript()` ที่ใช้ใน section นี้รัน **ก่อน** page scripts ทุกครั้ง — concept เดียวกับ `beforeEach` fixture แต่อยู่ใน browser context แทน Node.js
- Network mock ด้วย `page.route()` ทำงานร่วมกับ `context.route()` ได้ — context-level route จะ match ทุก page ใน context นั้น
