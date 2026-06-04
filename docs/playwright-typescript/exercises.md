# แบบฝึกหัด: Playwright TypeScript Course

แบบฝึกหัดครอบคลุมทุก 18 บท — 3 ระดับต่อบท (Recall / Application / Synthesis)

**กฎของแบบฝึกหัดนี้:**
- ตอบด้วยคำตัวเองก่อนดูเฉลยเสมอ
- ระดับ Intermediate ต้องเป็นสถานการณ์ใหม่ที่ไม่ซ้ำตัวอย่างในบท
- ระดับ Advanced ต้องวิเคราะห์หรือออกแบบ ไม่ใช่แค่จำ

---

## บทที่ 1 — Why Playwright: Mindset Shift จาก RF/Selenium

### Recall (Beginner)
อธิบายด้วยคำตัวเองว่า "auto-waiting" ใน Playwright คืออะไร และทำไมมันถึงทำให้ test เสถียรกว่าการใช้ `Sleep` หรือ `Wait Until Element Is Visible` ใน Robot Framework — ยกตัวอย่างสถานการณ์จากชีวิตจริง (ไม่ต้องเป็น IT) ที่อธิบาย concept นี้ได้

### Application (Intermediate)
ทีมของคุณกำลังทดสอบระบบจองโต๊ะร้านอาหาร ซึ่ง backend ใช้เวลา 1-3 วินาทีในการยืนยันการจองก่อนที่ confirmation badge จะโผล่ขึ้น มีสมาชิกทีมเสนอสองแนวทาง:

- **แนวทาง A**: เพิ่ม `await page.waitForTimeout(5000)` หลัง click ปุ่ม "ยืนยันการจอง"
- **แนวทาง B**: ใช้ `await expect(page.getByTestId('booking-confirmed')).toBeVisible()`

จงอธิบายว่าแนวทางไหนถูกต้องและทำไม รวมถึงอธิบายว่าจะเกิดอะไรขึ้นถ้าใช้แนวทางที่ผิดบน CI server ที่ช้ากว่า local 3 เท่า

### Synthesis (Advanced)
เพื่อนร่วมทีมโต้แย้งว่า "เราควรใช้ Robot Framework + Selenium ต่อไป เพราะทีมทั้งหมดรู้จักมันดีแล้ว การ migrate ไป Playwright เสีย effort มากแต่ได้ประโยชน์ไม่คุ้ม"

จงวิเคราะห์ข้อโต้แย้งนี้อย่างตรงไปตรงมา: ระบุสถานการณ์ที่ข้อโต้แย้งนี้ **ถูกต้อง** และสถานการณ์ที่ **ไม่ถูกต้อง** พร้อมให้เหตุผลที่ชัดเจน ไม่ใช่แค่ "Playwright ดีกว่า" แบบทั่วไป

<details>
<summary>เฉลย</summary>

**Beginner:**
Auto-waiting หมายความว่า ก่อนที่ Playwright จะทำ action ใดๆ (เช่น click, fill) มันจะตรวจสอบสภาพของ element อัตโนมัติว่า: มองเห็นได้ (visible), ไม่ถูก disable, ไม่กำลัง animate, และ stable แล้ว — ถ้ายังไม่พร้อมจะรอจนครบ timeout (default 30 วินาที) โดยไม่ต้องเขียน wait เอง

ตัวอย่างจากชีวิตจริง: เหมือนลิฟต์อัจฉริยะที่รอให้ประตูเปิดสุดก่อนค่อยให้คนเข้า ไม่ใช่ลิฟต์แบบเก่าที่ต้องกดปุ่มค้างรอเองจนแน่ใจว่าเปิดแล้ว ถ้าปล่อยปุ่มก่อนประตูเปิดสุดก็เข้าไม่ได้

**Intermediate:**
แนวทาง B ถูกต้อง เพราะ `expect(locator).toBeVisible()` เป็น web-first assertion ที่ retry ซ้ำจนครบ timeout (default 5 วินาที) รองรับ response ที่ใช้เวลาต่างกันในแต่ละรอบ

แนวทาง A มีปัญหา: `waitForTimeout(5000)` บน CI ที่ช้ากว่า 3 เท่า ถ้า backend ใช้เวลา 4 วินาที จะยังได้รับ confirmation ก่อน timeout 5 วินาทีพอดี แต่ถ้า network congestion ทำให้ใช้เวลา 5.5 วินาที test จะ fail ทั้งที่ระบบทำงานถูกต้อง นอกจากนั้นยังเสียเวลา 5 วินาทีเต็มทุก test ทั้งที่บางรอบ response กลับมาใน 1 วินาที

**Advanced:**
ข้อโต้แย้ง **ถูกต้อง** เมื่อ:
- ทีมมี RF test suite ที่ stable ขนาดใหญ่ (500+ tests) ที่ผ่าน review แล้ว — migration cost สูงมาก
- project ใกล้สิ้นสุด ไม่มี feature ใหม่ที่ต้องการ Playwright features
- ทีมไม่มี TypeScript experience — learning curve จะทำให้ velocity ลดลงระยะสั้น
- test suite ที่มีอยู่ไม่ flaky และ maintain ง่าย — ไม่มีปัญหาที่ต้องแก้

ข้อโต้แย้ง **ไม่ถูกต้อง** เมื่อ:
- test suite ปัจจุบัน flaky มาก ใช้เวลา debug มากกว่า write
- ต้องการ cross-browser testing ที่ทำงานได้จริง
- ทีมกำลังสร้าง modern web app ที่มี async UI ซับซ้อน
- CI pipeline ช้ามากจนทีมหยุดรัน test

</details>

---

## บทที่ 2 — Setup + TypeScript Essentials

### Recall (Beginner)
อธิบายว่าทำไม `async/await` ถึงจำเป็นใน Playwright ทุก action — ยกตัวอย่างด้วยโค้ดว่าจะเกิดอะไรขึ้นถ้าลืม `await` หน้า `page.click()`

### Application (Intermediate)
คุณได้รับ task ให้ setup Playwright project ใหม่สำหรับระบบ e-learning ที่ต้องการ test บน Chrome และ Firefox โปรเจคนี้ใช้ React + TypeScript frontend และ REST API backend ที่รันที่ `http://localhost:8080`

จงอธิบายขั้นตอนตั้งแต่ `npm init playwright@latest` จนถึง test แรกที่รันผ่านได้จริง รวมถึงไฟล์ไหนบ้างที่ต้องแก้ไขหลัง scaffold และต้องแก้อะไร

### Synthesis (Advanced)
ตรวจสอบ code นี้แล้วระบุปัญหาทุกจุดพร้อมอธิบายว่าเกิดอะไรขึ้นเมื่อรัน:

```typescript
import { test } from '@playwright/test';

test('search product', ({ page }) => {
  page.goto('http://localhost:8080/shop');
  page.getByPlaceholder('ค้นหาสินค้า').fill('notebook');
  page.getByRole('button', { name: 'ค้นหา' }).click();
  const results = page.locator('.search-results');
  console.log(results.textContent());
});
```

<details>
<summary>เฉลย</summary>

**Beginner:**
Playwright actions ทุกตัว (goto, click, fill) เป็น async operations — ใช้เวลาจริงบน browser และ return Promise ถ้าลืม `await` code จะรันต่อทันทีโดยไม่รอให้ action เสร็จ

ตัวอย่าง: ถ้าลืม `await` หน้า `page.click('#submit')` code บรรทัดถัดไปอาจ assert ผลลัพธ์ก่อนที่ browser จะได้รับ click เลย ทำให้ test fail แบบ intermittent โดยเฉพาะบน machine ที่ช้า

**Intermediate:**
1. รัน `npm init playwright@latest` → เลือก TypeScript, เลือก browsers ที่ต้องการ
2. แก้ `playwright.config.ts`:
   - ตั้ง `baseURL: 'http://localhost:8080'`
   - เพิ่ม `webServer` ถ้าต้องการให้ Playwright start backend อัตโนมัติ
   - ตรวจสอบ `projects` array ให้มี chromium และ firefox
3. เขียน test แรกใน `tests/` folder โดยใช้ `baseURL` แทน hardcode URL
4. รัน `npx playwright test` ครั้งแรก — Playwright จะ install browsers อัตโนมัติถ้ายังไม่มี

**Advanced:**
ปัญหา 4 จุด:

1. **ลืม `async` ใน test callback** — `({ page }) =>` ควรเป็น `async ({ page }) =>` ไม่เช่นนั้น `await` จะ syntax error
2. **ลืม `await` หน้า actions ทุกบรรทัด** — `page.goto()`, `page.fill()`, `page.click()` ล้วนเป็น async และต้อง await ทั้งหมด
3. **`results.textContent()` เป็น async** — ต้อง `await results.textContent()` ไม่เช่นนั้น `console.log` จะ print `[object Promise]`
4. **ไม่มี assertion** — test นี้ไม่ตรวจสอบอะไรจริงๆ แม้ค้นหาผิดพลาดก็ไม่ fail ควรใช้ `expect(results).toContainText('notebook')`

</details>

---

## บทที่ 3 — Architecture: Browser, BrowserContext, Page

### Recall (Beginner)
อธิบายความสัมพันธ์ระหว่าง `Browser`, `BrowserContext`, และ `Page` ด้วย analogy ของตัวเองที่ไม่ใช่โรงแรม — ระบุให้ชัดว่า "isolation" เกิดขึ้นที่ระดับไหน

### Application (Intermediate)
คุณกำลังเขียน test สำหรับระบบ online banking ที่ต้องทดสอบว่า "ผู้ใช้ A โอนเงินให้ผู้ใช้ B แล้วยอดเงินทั้งสองบัญชีอัปเดตถูกต้อง" โดยต้องให้ทั้งสองเข้า session พร้อมกัน

จงออกแบบ test ว่าจะใช้ fixtures ระดับไหน (`page`, `context`, หรือ `browser`) และทำไม — เขียน skeleton code แสดงโครงสร้าง

### Synthesis (Advanced)
เพื่อนร่วมทีมส่ง code นี้มาให้ review และบอกว่า "test ตัวที่สองมักจะ fail แบบ random บน CI":

```typescript
let loggedInPage: Page;

test.beforeAll(async ({ browser }) => {
  const context = await browser.newContext();
  loggedInPage = await context.newPage();
  await loggedInPage.goto('http://localhost:3000/login');
  await loggedInPage.fill('#username', 'admin');
  await loggedInPage.fill('#password', 'secret');
  await loggedInPage.click('#submit');
});

test('view dashboard', async () => {
  await loggedInPage.goto('http://localhost:3000/dashboard');
  await expect(loggedInPage.getByTestId('welcome-msg')).toBeVisible();
});

test('view reports', async () => {
  await loggedInPage.goto('http://localhost:3000/reports');
  await expect(loggedInPage.getByTestId('report-table')).toBeVisible();
});
```

วิเคราะห์ว่ามีปัญหากี่จุด อะไรเป็นสาเหตุที่ test fail แบบ random และเสนอวิธีแก้ที่ถูกต้อง

<details>
<summary>เฉลย</summary>

**Beginner:**
ตัวอย่าง analogy: คอมพิวเตอร์ 1 เครื่อง (Browser) สามารถเปิดได้หลาย user account (BrowserContext) และแต่ละ account เปิดได้หลาย browser window (Page)

Isolation เกิดขึ้นที่ระดับ BrowserContext — cookies, localStorage, sessionStorage แยกกันสมบูรณ์ระหว่าง context หมายความว่า login ใน context A ไม่รั่วไป context B เลย แต่ Pages ใน context เดียวกันแชร์ session กัน

**Intermediate:**
ต้องใช้ `browser` fixture แล้วสร้าง 2 contexts:

```typescript
test('transfer between users', async ({ browser }) => {
  // สร้าง 2 isolated sessions
  const userAContext = await browser.newContext();
  const userBContext = await browser.newContext();
  
  const userAPage = await userAContext.newPage();
  const userBPage = await userBContext.newPage();
  
  // login แยกกัน
  await loginAs(userAPage, 'userA', 'passA');
  await loginAs(userBPage, 'userB', 'passB');
  
  // ดำเนินการ transfer
  await userAPage.getByTestId('transfer-btn').click();
  // ...
  
  // cleanup
  await userAContext.close();
  await userBContext.close();
});
```

ถ้าใช้ `page` fixture เดียว login B จะ overwrite session A ทันที เพราะอยู่ใน context เดียวกัน

**Advanced:**
ปัญหา 3 จุด:

1. **Shared mutable state** — `loggedInPage` เป็น module-level variable ที่แชร์ระหว่าง tests ใน parallel mode worker อาจรัน tests พร้อมกันทำให้ `goto()` ของ test หนึ่งรบกวน state ของอีก test

2. **Context ไม่ถูก close** — context ที่สร้างใน `beforeAll` ไม่มี `afterAll` ที่ close มันทำให้ resource leak และ test runner อาจไม่ cleanup ถูกต้อง

3. **Fragile isolation** — ถ้า test แรก (`view dashboard`) ทำ action ที่เปลี่ยน state (เช่น logout โดยไม่ตั้งใจ) test ที่สองจะ fail โดยไม่ใช่ความผิดของตัวเอง

วิธีแก้ที่ถูกต้อง: ใช้ `storageState` — login ครั้งเดียวบันทึกเป็นไฟล์ แล้วให้แต่ละ test โหลด storageState ของตัวเองเป็น fresh context ดังนี้:

```typescript
test.use({ storageState: 'playwright/.auth/admin.json' });

test('view dashboard', async ({ page }) => {
  await page.goto('/dashboard');
  // แต่ละ test ได้ fresh page ที่ใช้ saved session
});
```

</details>

---

## บทที่ 4 — Locators: Accessibility-First

### Recall (Beginner)
อธิบายความแตกต่างระหว่าง `getByRole('button', { name: 'Submit' })` กับ `locator('#submit-btn')` ในแง่ของ "ความทนทานต่อการเปลี่ยนแปลง UI" — ยกตัวอย่างการเปลี่ยนแปลงที่จะทำให้แต่ละแบบพัง

### Application (Intermediate)
คุณกำลังเขียน test สำหรับตาราง "สินค้าคงคลัง" ที่มีหลายแถว แต่ละแถวมีชื่อสินค้า ราคา และปุ่ม "แก้ไข" กับ "ลบ" คุณต้องเขียน locator สำหรับ:

1. ปุ่ม "ลบ" ของสินค้าชื่อ "เก้าอี้สำนักงาน" โดยไม่ใช้ `nth()`
2. เซลล์ราคาของสินค้า "โต๊ะประชุม" เพื่อ assert ว่าเป็น "฿12,500"

จงเขียน locator ทั้งสองพร้อมอธิบายการ chain

### Synthesis (Advanced)
เพื่อนส่ง test file มาให้ review พบว่าใช้ locator แบบนี้ทั่วทั้งไฟล์ 200 บรรทัด:

```typescript
await page.locator('div.modal > div.modal-body form input[type="text"]:first-child').fill('John');
await page.locator('div.modal > div.modal-body form input[type="text"]:nth-child(2)').fill('Doe');
await page.locator('div.modal > div.modal-body form button.btn-primary').click();
```

วิเคราะห์ปัญหาทั้งหมดที่มี แล้ว refactor ให้ถูกต้องโดยสมมติว่า form นี้คือ "เพิ่มพนักงานใหม่" ที่มี field ชื่อและนามสกุล

<details>
<summary>เฉลย</summary>

**Beginner:**
`getByRole('button', { name: 'Submit' })` ค้นหาด้วย ARIA role และ visible text — จะพังก็ต่อเมื่อข้อความบนปุ่มเปลี่ยน หรือ role เปลี่ยน (ซึ่งหมายความว่า UI จริงเปลี่ยนแล้ว ควรให้ test พัง)

`locator('#submit-btn')` ค้นหาด้วย CSS id — จะพังทันทีถ้า developer เปลี่ยน id เป็น `#login-submit` เพื่อความชัดเจน ทั้งที่ปุ่มยังทำงานเหมือนเดิมทุกอย่าง นี่คือ "false failure" ที่ทำให้เสียเวลา debug โดยไม่จำเป็น

**Intermediate:**
```typescript
// 1. ปุ่มลบของสินค้า "เก้าอี้สำนักงาน"
await page.getByRole('row', { name: 'เก้าอี้สำนักงาน' })
  .getByRole('button', { name: 'ลบ' })
  .click();

// หรือใช้ filter:
await page.getByRole('row')
  .filter({ hasText: 'เก้าอี้สำนักงาน' })
  .getByRole('button', { name: 'ลบ' })
  .click();

// 2. ราคาของสินค้า "โต๊ะประชุม"
await expect(
  page.getByRole('row').filter({ hasText: 'โต๊ะประชุม' })
    .getByRole('cell', { name: /฿12,500/ })
).toBeVisible();
```

**Advanced:**
ปัญหา 4 จุด:

1. **Fragile CSS path** — `div.modal > div.modal-body form` จะพังทันทีถ้า designer เพิ่ม wrapper div หรือเปลี่ยน class ชื่อ
2. **Position-based selector** — `:first-child` และ `:nth-child(2)` พังถ้าลำดับ fields เปลี่ยน (เช่น เพิ่ม title field ก่อน)
3. **ไม่มี semantic meaning** — อ่าน code ไม่รู้ว่า input ไหนคือ field อะไร ต้องไปดู HTML ทุกครั้ง
4. **`button.btn-primary`** — พังถ้าเปลี่ยน CSS class

Refactored version:
```typescript
// สมมติ form มี label ที่ถูกต้อง
await page.getByLabel('ชื่อ').fill('John');
await page.getByLabel('นามสกุล').fill('Doe');
await page.getByRole('button', { name: 'เพิ่มพนักงาน' }).click();
```

</details>

---

## บทที่ 5 — Actions & Assertions

### Recall (Beginner)
อธิบายความแตกต่างระหว่าง `page.locator('.alert').isVisible()` กับ `expect(page.locator('.alert')).toBeVisible()` ในแง่ retry behavior — และบอกว่าสถานการณ์ใดที่ควรใช้แบบแรก

### Application (Intermediate)
คุณกำลังเขียน test สำหรับ multi-step form ลงทะเบียนงานสัมมนา ซึ่ง form มี 3 หน้า (ข้อมูลส่วนตัว → ตัวเลือก session → ยืนยัน) คุณต้องการตรวจสอบทุก field บนหน้าสุดท้าย "ยืนยัน" ว่าข้อมูลทั้งหมดแสดงถูกต้อง — แต่ถ้า field ใด field หนึ่งผิด ต้องการเห็น error ทุกจุดในรอบเดียว

จงเขียน test โดยใช้ `expect.soft()` อย่างถูกต้อง

### Synthesis (Advanced)
วิเคราะห์ test นี้ว่ามีปัญหาอะไร และในสถานการณ์ใดจะ fail แบบ flaky:

```typescript
test('notification disappears after 3 seconds', async ({ page }) => {
  await page.goto('/dashboard');
  await page.getByRole('button', { name: 'Save' }).click();
  
  const notification = page.getByTestId('toast-notification');
  expect(await notification.isVisible()).toBe(true);
  
  await page.waitForTimeout(3500);
  expect(await notification.isVisible()).toBe(false);
});
```

เสนอ refactor ที่ทำให้ test reliable กว่า

<details>
<summary>เฉลย</summary>

**Beginner:**
`isVisible()` คือ snapshot — ตรวจสอบ ณ วินาทีที่ call แล้ว return `true`/`false` ทันทีโดยไม่รอ ถ้า element ยังไม่ปรากฏขณะนั้น ได้ `false` เลย

`expect(locator).toBeVisible()` คือ web-first assertion — retry ซ้ำทุก 100ms จนครบ timeout (default 5 วินาที) ถ้า element โผล่ขึ้นในช่วงเวลานั้น test จะผ่าน

ควรใช้ `isVisible()` เมื่อต้องการ conditional logic จริงๆ เช่น:
```typescript
if (await page.getByTestId('cookie-banner').isVisible()) {
  await page.getByRole('button', { name: 'Accept' }).click();
}
```

**Intermediate:**
```typescript
test('confirm page shows all registration details', async ({ page }) => {
  // ... กรอก form 2 หน้าแรก ...
  await page.getByRole('button', { name: 'ถัดไป' }).click();
  
  // ใช้ soft assertions เพื่อเห็นทุก error พร้อมกัน
  await expect.soft(page.getByTestId('confirm-name')).toHaveText('สมชาย ใจดี');
  await expect.soft(page.getByTestId('confirm-email')).toHaveText('somchai@example.com');
  await expect.soft(page.getByTestId('confirm-session')).toHaveText('Session A: 09:00-12:00');
  await expect.soft(page.getByTestId('confirm-ticket-type')).toHaveText('บัตร VIP');
  
  // hard assertion สำหรับ step สุดท้าย — ถ้าข้อมูลผิดไม่ควร submit
  await expect(page.getByRole('button', { name: 'ยืนยันการลงทะเบียน' })).toBeEnabled();
});
```

**Advanced:**
ปัญหา 3 จุด:

1. **`isVisible()` ไม่ retry** — บรรทัด `expect(await notification.isVisible()).toBe(true)` จะ fail ถ้า toast โผล่ช้ากว่า 50ms หลัง click (ซึ่งเกิดได้บน machine ช้า)

2. **`waitForTimeout(3500)` เดาเวลา** — ถ้า animation หรือ timer ของ toast ขึ้นกับ device performance (เช่น CI throttled) 3500ms อาจไม่พอหรือมากเกินไป

3. **`isVisible()` ไม่ retry ตอนตรวจ disappear** — ควรรอให้หายจริงๆ ไม่ใช่ snapshot

Refactored version:
```typescript
test('notification disappears after 3 seconds', async ({ page }) => {
  await page.goto('/dashboard');
  await page.getByRole('button', { name: 'Save' }).click();
  
  const notification = page.getByTestId('toast-notification');
  // รอให้โผล่ก่อน (retry จนครบ timeout)
  await expect(notification).toBeVisible();
  // รอให้หาย (retry จนครบ timeout กว้างกว่า default)
  await expect(notification).not.toBeVisible({ timeout: 8000 });
});
```

</details>

---

## บทที่ 6 — Debugging: Inspector, Trace, UI Mode, Codegen

### Recall (Beginner)
อธิบายว่า Playwright Trace Viewer บันทึกข้อมูลอะไรบ้าง และทำไมมันถึงมีประโยชน์กว่าแค่ screenshot เดี่ยวๆ สำหรับการ debug test ที่ fail บน CI — ยกตัวอย่างสถานการณ์จริงที่ Trace Viewer ช่วยได้แต่ screenshot ธรรมดาช่วยไม่ได้

### Application (Intermediate)
คุณมี test ที่ fail บน CI ด้วย error: `Timeout 30000ms exceeded. waiting for locator('[data-testid="payment-success"]')` แต่รันผ่านบน local ทุกครั้ง และคุณไม่มีสิทธิ์ login เข้า CI machine โดยตรง

อธิบายขั้นตอนทีละขั้นว่าจะ debug ปัญหานี้อย่างไร โดยใช้ Playwright built-in tools — ไม่ต้องเพิ่ม `console.log` เพิ่มเติม

### Synthesis (Advanced)
เพื่อนร่วมทีมใช้ Codegen บันทึก test แล้วส่งมาให้ใช้งานจริง:

```typescript
test('create new product', async ({ page }) => {
  await page.goto('http://localhost:3000/admin/products');
  await page.locator('#root > div > main > div.container > div > button:nth-child(3)').click();
  await page.locator('input[name="productName"]').click();
  await page.locator('input[name="productName"]').fill('Gaming Chair Pro');
  await page.locator('input[name="price"]').click();
  await page.locator('input[name="price"]').fill('15900');
  await page.locator('#root > div > main > div.container > div > div.modal > form > div:last-child > button.btn.btn-success').click();
  await expect(page.locator('.alert-success')).toBeVisible();
});
```

วิเคราะห์ปัญหาของ code ที่ Codegen สร้างและ refactor ให้ใช้งาน production ได้จริง

<details>
<summary>เฉลย</summary>

**Beginner:**
Trace Viewer บันทึก: screenshot ทุก action, DOM snapshot (คลิกดู element ได้), network request/response ทุกอย่าง, console logs, และ call stack ของแต่ละ action — ทั้งหมดเป็น timeline ที่ scrub ได้

Screenshot เดี่ยวๆ บอกแค่ "หน้าตาตอนพังเป็นอย่างไร" แต่ไม่บอกว่า 5 steps ก่อนหน้าเกิดอะไร Trace Viewer ช่วยได้เมื่อ: test fail เพราะ API response ผิด (ดู network tab), element โผล่แล้วหายก่อน click (ดู DOM timeline), หรือ JavaScript error ใน console ที่เกิดก่อน visible failure

**Intermediate:**
1. ตรวจ config ว่ามี `trace: 'on-first-retry'` และ `retries: 1` แล้วหรือยัง ถ้ายังต้องเพิ่มก่อน push ไป CI
2. Push code ไป CI รอ pipeline รัน
3. Download artifact `playwright-report.zip` จาก CI
4. รัน `npx playwright show-trace path/to/trace.zip` ใน local
5. ใน Trace Viewer ดู: network requests ว่า payment API return อะไร, screenshots ทุก step ว่าหน้า payment แสดงอะไร, console logs ว่ามี JS error ไหม
6. ถ้าพบว่า payment API ช้ากว่า 30 วินาทีบน CI → แก้ timeout หรือ mock API

**Advanced:**
ปัญหา 5 จุด:

1. **CSS path ยาวและเปราะ** — `#root > div > main > div.container > div > button:nth-child(3)` พังทันทีถ้า layout เปลี่ยนแม้เล็กน้อย
2. **`click()` ก่อน `fill()`** — ปกติไม่จำเป็น Playwright จะ focus อัตโนมัติ ทำให้ code verbose โดยไม่มีประโยชน์
3. **hardcode URL** — ควรใช้ `baseURL` จาก config
4. **modal locator เปราะ** — `div.modal > form > div:last-child > button.btn.btn-success` อิง structure และ class
5. **`.alert-success`** เป็น CSS class ที่เปลี่ยนได้

Refactored:
```typescript
test('create new product', async ({ page }) => {
  await page.goto('/admin/products');
  await page.getByRole('button', { name: 'เพิ่มสินค้าใหม่' }).click();
  await page.getByLabel('ชื่อสินค้า').fill('Gaming Chair Pro');
  await page.getByLabel('ราคา').fill('15900');
  await page.getByRole('button', { name: 'บันทึก' }).click();
  await expect(page.getByRole('alert')).toContainText('บันทึกสินค้าสำเร็จ');
});
```

</details>

---

## บทที่ 7 — Fixtures: Dependency Injection สำหรับ Tests

### Recall (Beginner)
อธิบายว่า Playwright fixture ต่างจาก `beforeEach`/`afterEach` อย่างไรในเรื่อง "lazy initialization" และ "guaranteed teardown" — และทำไมสองคุณสมบัตินี้ถึงสำคัญในทางปฏิบัติ

### Application (Intermediate)
คุณกำลังสร้าง test suite สำหรับระบบ HR management ที่มี tests หลายกลุ่ม:

- **กลุ่มที่ 1**: tests ที่ต้องการ employee data 5 รายการใน database
- **กลุ่มที่ 2**: tests ที่ต้องการ login เป็น HR Manager ก่อน  
- **กลุ่มที่ 3**: tests ที่ต้องการทั้งสองอย่าง

จงออกแบบ fixtures สำหรับทั้งสองกลุ่มและอธิบายว่าจะประกาศ dependency อย่างไรให้ Playwright จัดการ order อัตโนมัติ

### Synthesis (Advanced)
วิเคราะห์ fixture นี้แล้วระบุปัญหาทั้งหมด:

```typescript
const test = base.extend<{
  adminPage: Page;
  testData: { userId: string };
}>({
  adminPage: async ({ browser }, use) => {
    const context = await browser.newContext();
    const page = await context.newPage();
    await page.goto('http://localhost:3000/login');
    await page.fill('#username', 'admin');
    await page.fill('#password', 'admin123');
    await page.click('#login-btn');
    await use(page);
  },
  
  testData: async ({}, use) => {
    const res = await fetch('http://localhost:3000/api/users', {
      method: 'POST',
      body: JSON.stringify({ name: 'Test User' }),
    });
    const data = await res.json();
    await use(data);
    // cleanup ถ้า test pass
    if (/* test passed */) {
      await fetch(`http://localhost:3000/api/users/${data.userId}`, { method: 'DELETE' });
    }
  },
});
```

<details>
<summary>เฉลย</summary>

**Beginner:**
Lazy initialization: fixture สร้างเฉพาะเมื่อ test ประกาศต้องการ (declare ใน parameter) ถ้า test ไม่ใช้ `adminPage` fixture ก็ไม่รัน setup เลย — ต่างจาก `beforeEach` ที่รันทุกครั้งแม้ test นั้นไม่ต้องการ

Guaranteed teardown: code หลัง `await use()` ใน fixture รันเสมอไม่ว่า test จะ pass หรือ fail — ต่างจาก `afterEach` ที่ถ้า `beforeEach` throw error อาจทำให้ `afterEach` ไม่รัน

**Intermediate:**
```typescript
const test = base.extend<{
  employeeData: Employee[];
  hrManagerPage: Page;
}>({
  employeeData: async ({ request }, use) => {
    // สร้าง test data ผ่าน API
    const employees = await createTestEmployees(request, 5);
    await use(employees);
    // cleanup เสมอ
    await deleteTestEmployees(request, employees);
  },
  
  hrManagerPage: async ({ page }, use) => {
    await loginAs(page, 'hr_manager', 'password');
    await use(page);
    // page cleanup โดย Playwright อัตโนมัติ
  },
});

// กลุ่ม 3 ที่ต้องการทั้งสอง — Playwright จัดการ order เอง
test('hire new employee', async ({ hrManagerPage, employeeData }) => {
  // hrManagerPage และ employeeData พร้อมใช้ทั้งคู่
});
```

Playwright resolve dependency ด้วย fixture name ใน parameter ลำดับ setup/teardown ถูกจัดการอัตโนมัติ

**Advanced:**
ปัญหา 4 จุด:

1. **`adminPage` ไม่ close context** — สร้าง `browser.newContext()` แต่ไม่มี `context.close()` หลัง `use()` ทำให้ resource leak ทุก test

2. **Hardcode credentials** — `admin123` ใน fixture source code ไม่ปลอดภัย ควรใช้ environment variable

3. **`testData` cleanup แบบ conditional** — `if (/* test passed */)` เขียนไม่ได้ใน fixture เพราะ fixture ไม่รู้ว่า test pass หรือ fail ดีกว่าคือ cleanup เสมอ (unconditional) เพื่อให้ database สะอาดเสมอ

4. **ใช้ `fetch()` ธรรมดาแทน `request` fixture** — `fetch()` ไม่ integrate กับ Playwright baseURL, authentication context, และ error handling ที่ดี ควรใช้ `request` fixture แทน

</details>

---

## บทที่ 8 — Page Object Model

### Recall (Beginner)
อธิบายว่า Page Object Model แก้ปัญหาอะไร และทำไม assertion ถึงไม่ควรอยู่ใน Page Object method — ยกตัวอย่างสถานการณ์ที่จะเกิดปัญหาถ้าใส่ assertion ไว้ใน POM

### Application (Intermediate)
คุณได้รับ task ให้สร้าง Page Object สำหรับระบบ "ยื่นใบลา" ที่มี:
- หน้า `LeaveRequestPage`: form กรอก วันเริ่ม, วันสิ้นสุด, ประเภทลา, เหตุผล
- หน้า `LeaveHistoryPage`: ตารางแสดงประวัติการลา
- `NavBarComponent`: ที่ปรากฏทั้งสองหน้า มีปุ่ม logout

จงออกแบบโครงสร้าง class ทั้งหมดโดยใช้ Composition และแสดง method signatures ที่จำเป็น

### Synthesis (Advanced)
Review code POM นี้แล้วระบุ anti-patterns ทั้งหมด:

```typescript
export class ProductPage extends BasePage {
  async addToCart(productName: string) {
    await this.page.getByText(productName).click();
    await this.page.locator('#add-to-cart').click();
    await expect(this.page.getByTestId('cart-count')).toContainText('1');
    return this;
  }
  
  async checkout() {
    await this.page.goto('/checkout');
    await this.page.waitForTimeout(2000);
    return new CheckoutPage(this.page);
  }
  
  get cartCount() {
    return this.page.locator('#cart-count').textContent();
  }
}
```

<details>
<summary>เฉลย</summary>

**Beginner:**
POM รวม locators และ actions ที่เกี่ยวกับหน้าหนึ่งไว้ใน class เดียว ทำให้เปลี่ยน locator ที่เดียวแก้ทุก test แทนที่จะต้อง find & replace ทุกไฟล์

ปัญหาของ assertion ใน POM: ถ้า `loginPage.login()` มี `expect(dashboard).toBeVisible()` อยู่ใน method test ที่ต้องการตรวจสอบ "login ล้มเหลว" จะ fail ที่ assertion ใน POM ก่อน ไม่มีโอกาส assert error message เลย POM ควรเป็น action layer ล้วนๆ ให้ test ที่ assert

**Intermediate:**
```typescript
// Component ที่แชร์กัน
class NavBarComponent {
  constructor(private page: Page) {}
  readonly logoutButton = this.page.getByRole('button', { name: 'ออกจากระบบ' });
  
  async logout() {
    await this.logoutButton.click();
  }
}

// Page Object สำหรับยื่นใบลา
class LeaveRequestPage {
  readonly navBar: NavBarComponent;
  readonly startDateInput = this.page.getByLabel('วันเริ่มต้น');
  readonly endDateInput = this.page.getByLabel('วันสิ้นสุด');
  readonly leaveTypeSelect = this.page.getByLabel('ประเภทการลา');
  readonly reasonInput = this.page.getByLabel('เหตุผล');
  
  constructor(private page: Page) {
    this.navBar = new NavBarComponent(page);
  }
  
  async submitRequest(data: LeaveRequestData): Promise<LeaveHistoryPage> {
    await this.startDateInput.fill(data.startDate);
    await this.endDateInput.fill(data.endDate);
    await this.leaveTypeSelect.selectOption(data.leaveType);
    await this.reasonInput.fill(data.reason);
    await this.page.getByRole('button', { name: 'ยื่นคำขอ' }).click();
    return new LeaveHistoryPage(this.page);
  }
}
```

**Advanced:**
Anti-patterns 4 จุด:

1. **Assertion ใน POM** — `expect(cart-count).toContainText('1')` ใน `addToCart()` ถ้า test อื่นต้องการ add หลาย items แล้ว count จะไม่ใช่ '1' assertion จะ fail โดยที่ action ถูกต้อง

2. **`waitForTimeout(2000)` ใน POM** — hardcode delay ทำให้ test ช้าและ flaky ควรใช้ condition-based wait เช่น รอให้ checkout form visible

3. **`cartCount` getter return Promise** — `textContent()` เป็น async แต่ getter ไม่มี `async` ทำให้ผู้ใช้ต้อง `await (await productPage.cartCount)` แปลกมาก ควรเป็น Locator ปกติหรือ async method

4. **Inheritance (`extends BasePage`)** — ถ้า BasePage มี methods ที่ไม่เกี่ยวกับ ProductPage ทุก subclass จะได้รับ method ที่ไม่จำเป็น Composition ดีกว่าในกรณีนี้

</details>

---

## บทที่ 9 — Test Organization: Annotations, Tags, test.step

### Recall (Beginner)
อธิบายความแตกต่างระหว่าง `test.skip()`, `test.fixme()`, และ `test.fail()` — แต่ละตัวใช้เมื่อไหรและบอกอะไรกับทีม

### Application (Intermediate)
คุณมี test suite สำหรับระบบ "สั่งซื้อออนไลน์" ที่มี 60 tests โดยมีโครงสร้างดังนี้:
- 10 tests สำหรับ smoke check (ฟังก์ชั่นหลัก)
- 30 tests สำหรับ checkout flow (บางตัวช้ามากเพราะมีหลาย steps)
- 20 tests สำหรับ account management

ทีมต้องการ: (1) รัน smoke tests อย่างเดียวก่อน deploy ได้เร็ว (2) ข้ามพักการรัน checkout tests ที่ช้าได้ชั่วคราว (3) ดู test report ที่บอกว่า checkout flow มี 8 steps ชัดเจน

จงอธิบายว่าจะใช้ annotations, tags, และ `test.step()` อย่างไร

### Synthesis (Advanced)
เพื่อนร่วมทีมส่ง test นี้มา และบอกว่า "test นี้ใช้เวลานานมาก อยากให้มัน skip ไปก่อน":

```typescript
test('full order lifecycle', async ({ page }) => {
  // ค้นหาสินค้า
  await page.goto('/shop');
  await page.getByPlaceholder('ค้นหา').fill('laptop');
  await page.getByRole('button', { name: 'ค้นหา' }).click();
  await page.getByRole('link', { name: 'Laptop Pro X' }).click();
  await page.getByRole('button', { name: 'Add to Cart' }).click();
  
  // checkout
  await page.goto('/checkout');
  await page.getByLabel('Address').fill('123 Main St');
  // ... อีก 20 actions ...
  await expect(page.getByTestId('order-id')).toBeVisible();
});
```

แทนที่จะแค่ skip — refactor test นี้ให้ดีขึ้น 3 ด้าน: (1) ใช้ annotation ที่บอก intent ชัดเจน (2) จัดกลุ่ม steps ให้ Trace Viewer อ่านง่าย (3) ตั้ง timeout ที่เหมาะสม

<details>
<summary>เฉลย</summary>

**Beginner:**
- `test.skip()` — ไม่รัน test เลย ใช้เมื่อ test ยังไม่ implement หรือ environment ไม่รองรับ ไม่มีแผนชัดเจนว่าจะแก้เมื่อไหร่
- `test.fixme()` — ไม่รัน test เลย (เหมือน skip) แต่มีความหมายว่า "รู้ว่าพังและมีแผนจะแก้" ทำให้ทีมรู้ว่านี่คือ known issue ไม่ใช่ตั้งใจ skip
- `test.fail()` — **รัน test** แต่ expect ว่ามันจะ fail ถ้า fail = ผ่าน, ถ้า pass กลับ = fail ใช้เมื่อ test ถูก design ถูกแล้ว แต่ feature ยังไม่ implement จึงรู้ว่าจะ fail

**Intermediate:**
```typescript
// Tag สำหรับ filtering
test('homepage loads', { tag: '@smoke' }, async ({ page }) => { ... });
test('search works', { tag: '@smoke' }, async ({ page }) => { ... });

// Checkout tests ที่ช้า
test.describe('checkout flow', () => {
  test.slow(); // ขยาย timeout เป็น 3 เท่าทั้ง describe block
  
  test('complete checkout', async ({ page }) => {
    await test.step('เลือกสินค้า', async () => { ... });
    await test.step('กรอกที่อยู่', async () => { ... });
    await test.step('ยืนยันการชำระเงิน', async () => { ... });
    // Trace Viewer จะแสดง 3 groups แทน 20+ actions ยาวเหยียด
  });
});
```

CLI: `npx playwright test --grep "@smoke"` สำหรับ smoke only
ชั่วคราวข้ามช้า: `npx playwright test --grep-invert "@checkout-slow"`

**Advanced:**
Refactored version:
```typescript
test('full order lifecycle', {
  tag: ['@regression', '@e2e'],
  annotation: { type: 'performance', description: 'Test ใช้เวลา ~45 วินาที — รันใน nightly build เท่านั้น' }
}, async ({ page }) => {
  test.slow(); // ขยาย timeout เป็น 3 เท่า แทนการ hardcode timeout

  await test.step('ค้นหาและเลือกสินค้า', async () => {
    await page.goto('/shop');
    await page.getByPlaceholder('ค้นหา').fill('laptop');
    await page.getByRole('button', { name: 'ค้นหา' }).click();
    await page.getByRole('link', { name: 'Laptop Pro X' }).click();
    await page.getByRole('button', { name: 'Add to Cart' }).click();
  });

  await test.step('ดำเนินการ checkout', async () => {
    await page.goto('/checkout');
    await page.getByLabel('Address').fill('123 Main St');
    // ... actions ...
  });

  await test.step('ยืนยันคำสั่งซื้อ', async () => {
    await expect(page.getByTestId('order-id')).toBeVisible();
  });
});
```

</details>

---

## บทที่ 10 — Configuration & Projects

### Recall (Beginner)
อธิบายว่า `projects` ใน `playwright.config.ts` คืออะไร และยกตัวอย่างสถานการณ์จริง 2 สถานการณ์ที่ต้องใช้ multiple projects (ไม่ใช่แค่ "รันหลาย browser")

### Application (Intermediate)
บริษัทคุณมี web app ที่ต้องการ test ใน 3 บริบท:
- **Dev environment** (localhost:3000) — รัน fast, ไม่ต้องการ trace
- **Staging environment** (staging.company.com) — รัน full suite พร้อม trace
- **Mobile view** (iPhone 15 Pro) บน staging — รันแค่ smoke tests

จงออกแบบ `playwright.config.ts` ที่รองรับทั้งสามบริบทด้วย projects และ environment variables

### Synthesis (Advanced)
ทีมมีปัญหา: developer มักลืมลบ `test.only()` ก่อน push ทำให้ CI รัน test แค่ตัวเดียวโดยไม่รู้ตัว และ `playwright.config.ts` ปัจจุบันนี้:

```typescript
export default defineConfig({
  timeout: 60000,
  retries: 3,
  workers: 8,
  reporter: 'html',
  use: {
    trace: 'on',
    video: 'on',
    screenshot: 'on',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
  ],
});
```

ระบุปัญหาทั้งหมดใน config นี้และเสนอ config ที่แก้ทุกปัญหา

<details>
<summary>เฉลย</summary>

**Beginner:**
Projects คือการรัน test suite เดียวกันในบริบทต่างกันพร้อมกัน

สถานการณ์จริง 2 อย่าง:
1. **Multi-role auth**: Project "admin" ใช้ storageState ของ admin, Project "viewer" ใช้ storageState ของ viewer — ทดสอบ access control ว่าแต่ละ role เห็นอะไรได้บ้าง
2. **Setup dependency**: Project "auth-setup" รัน login แล้ว save storageState ก่อน, Project "tests" depend on "auth-setup" เพื่อให้ setup รันก่อน tests เสมอ

**Intermediate:**
```typescript
export default defineConfig({
  use: { baseURL: process.env.BASE_URL || 'http://localhost:3000' },
  projects: [
    {
      name: 'dev-chrome',
      use: { ...devices['Desktop Chrome'], trace: 'off' },
      testMatch: '**/*.spec.ts',
    },
    {
      name: 'staging-full',
      use: {
        ...devices['Desktop Chrome'],
        baseURL: 'https://staging.company.com',
        trace: 'on-first-retry',
      },
      testMatch: '**/*.spec.ts',
    },
    {
      name: 'staging-mobile-smoke',
      use: {
        ...devices['iPhone 15 Pro'],
        baseURL: 'https://staging.company.com',
      },
      grep: /@smoke/,
    },
  ],
});
```

**Advanced:**
ปัญหา 5 จุด:

1. **ไม่มี `forbidOnly`** — developer ลืม `test.only()` CI รัน test เดียวโดยไม่รู้ตัว
2. **`trace: 'on'` เสมอ** — สร้าง trace ทุก test ทำให้ CI ช้าและใช้ disk space มาก ควรเป็น `'on-first-retry'`
3. **`video: 'on'` เสมอ** — สิ้นเปลืองมาก ควรเป็น `'on-first-retry'` หรือ `'retain-on-failure'`
4. **`retries: 3` เสมอ** — บน local ไม่ต้องการ retry ทำให้ debug ช้า ควรเป็น `process.env.CI ? 2 : 0`
5. **`workers: 8` hardcode** — บน machine บางตัวที่มี CPU น้อยกว่า อาจทำให้ช้ากว่าปกติ ควร `process.env.CI ? 4 : undefined`

Fixed config:
```typescript
export default defineConfig({
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 4 : undefined,
  reporter: process.env.CI ? [['html'], ['github']] : 'html',
  use: {
    trace: 'on-first-retry',
    video: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  // ...
});
```

</details>

---

## บทที่ 11 — Parallelism, Sharding & Reporting

### Recall (Beginner)
อธิบายความแตกต่างระหว่าง `workers` และ `sharding` ด้วยตัวอย่างที่เป็นรูปธรรม — และบอกว่าสถานการณ์ใดควรใช้อะไร (หรือใช้ทั้งคู่)

### Application (Intermediate)
ทีมของคุณมี test suite สำหรับ fintech app ที่มี 180 tests รันใน GitHub Actions ปัจจุบันใช้เวลา 22 นาที (single worker, no sharding) ทีมต้องการลดให้เหลือไม่เกิน 6 นาที และมีงบค่า GitHub Actions runner ได้สูงสุด 3 machines

จงคำนวณและออกแบบ strategy โดยระบุว่าจะตั้ง `workers` เป็นเท่าไหร่ และใช้ sharding กี่ shard

### Synthesis (Advanced)
ออกแบบ sharding strategy สำหรับ test suite ที่มีลักษณะนี้:
- 400 tests รวม
- 50 tests เป็น "integration tests" ที่รันช้ามาก (เฉลี่ย 45 วินาทีต่อ test) และต้อง serial (ห้าม parallel กัน)
- 350 tests เป็น "unit UI tests" ที่รันเร็ว (เฉลี่ย 3 วินาทีต่อ test) และ parallel ได้
- มีงบ GitHub Actions สูงสุด 4 runners
- ต้องเสร็จภายใน 8 นาที

<details>
<summary>เฉลย</summary>

**Beginner:**
Workers: รัน tests หลายตัวพร้อมกันบน machine เดียว โดยใช้ CPU cores ที่มี เช่น machine 4 cores → workers 4 → test รันพร้อมกัน 4 ตัว

Sharding: แบ่ง test suite ออกเป็นกลุ่มแล้วรันบน machines คนละเครื่องพร้อมกัน เช่น 4 machines แต่ละเครื่องรับ 25% ของ tests

ใช้ Workers เมื่อ: machine มี CPU เหลือ ต้องการเร็วขึ้นโดยไม่เพิ่ม cost
ใช้ Sharding เมื่อ: มีหลาย CI machines available ต้องการ speed เพิ่มอีกระดับ
ใช้ทั้งคู่เมื่อ: ต้องการ speed สูงสุดและมี budget สำหรับหลาย machines

**Intermediate:**
การคำนวณ:
- ปัจจุบัน: 22 นาที serial
- เป้าหมาย: ≤ 6 นาที → ต้องการ speedup ~3.7x
- มี 3 machines → sharding 3 shards = แต่ละ shard มี 60 tests = ~7.3 นาทีต่อ shard (ถ้า serial)
- ถ้าเพิ่ม workers=2 ต่อ shard = ~3.7 นาที ✓

Strategy:
```yaml
# GitHub Actions matrix
strategy:
  matrix:
    shard: [1, 2, 3]
steps:
  - run: npx playwright test --shard=${{ matrix.shard }}/3 --workers=2
```

Config: `workers: process.env.CI ? 2 : undefined`

ผลลัพธ์: 3 shards × 2 workers = ~6x speedup = ~3.7 นาที ✓

**Advanced:**
แบ่งเป็น 2 project types:

**Integration tests (50 tests × 45s = 37.5 นาที serial):**
- ห้าม parallel → workers=1
- แบ่งเป็น 2 shards (25 tests ต่อ shard × 45s = ~18.75 นาที)
- ยังช้าเกิน → ต้องใช้ 4 shards (13 tests × 45s = ~9.75 นาที)

**UI tests (350 tests × 3s = 17.5 นาที serial):**
- parallel ได้ → workers=4 บน 4-core runner
- 350 tests ÷ 4 shards × 3s ÷ 4 workers = ~65 วินาที ✓

ปัญหา: integration tests ยังใช้เวลา ~10 นาที เกินเป้า 8 นาที

วิธีแก้: รัน integration tests แยก pipeline (nightly) ไม่ใช่ทุก PR หรือ refactor integration tests ให้ parallel ได้ด้วย isolated test data

Final config แนะนำ:
- PR pipeline: UI tests only (4 shards × 4 workers) = ~65 วินาที
- Nightly pipeline: integration + UI (full suite)

</details>

---

## บทที่ 12 — Mocking: Network, Time, Browser APIs

### Recall (Beginner)
อธิบายว่า `page.route()` ทำงานอย่างไร และยกตัวอย่าง 3 สถานการณ์ที่ควรใช้ network mocking แทนการเรียก API จริง — พร้อมอธิบายว่าแต่ละสถานการณ์ได้ประโยชน์อะไร

### Application (Intermediate)
คุณกำลัง test หน้า "สรุปรายงานประจำเดือน" ที่แสดงข้อมูลจาก API `/api/reports/monthly?month=YYYY-MM` โดยหน้านี้มี feature "auto-refresh ทุก 5 นาที" คุณต้องทดสอบว่า:

1. ข้อมูลแสดงถูกต้องเมื่อ API return data ปกติ
2. แสดง error message เมื่อ API return 500
3. auto-refresh ทำงานหลังจาก 5 นาทีผ่านไป

จงเขียน test ทั้งสามข้อโดยใช้ `page.route()` และ Clock API

### Synthesis (Advanced)
วิเคราะห์ code นี้และระบุปัญหาทั้งหมด:

```typescript
test('payment flow with different statuses', async ({ page }) => {
  // Mock สำหรับ success
  await page.route('**/api/payment', route => {
    route.fulfill({ status: 200, body: JSON.stringify({ status: 'success' }) });
  });
  
  await page.goto('/checkout');
  await page.getByRole('button', { name: 'Pay Now' }).click();
  await expect(page.getByTestId('success-msg')).toBeVisible();
  
  // Mock สำหรับ failure (ใน test เดียวกัน)
  await page.route('**/api/payment', route => {
    route.fulfill({ status: 400, body: JSON.stringify({ error: 'Card declined' }) });
  });
  
  await page.getByRole('button', { name: 'Pay Now' }).click();
  await expect(page.getByTestId('error-msg')).toBeVisible();
});
```

<details>
<summary>เฉลย</summary>

**Beginner:**
`page.route(url_pattern, handler)` intercept HTTP request ที่ browser พยายามส่ง — แทนที่จะส่งจริงจะเรียก handler ที่เราเขียน แล้ว `route.fulfill()` คืน response ที่เราต้องการ

3 สถานการณ์:
1. **External API ที่ควบคุมไม่ได้** (payment gateway, SMS service) — ไม่ต้องใช้ card จริง ทดสอบ edge case ทุกอย่างได้ฟรี
2. **Test error scenarios ที่ reproduce ยาก** (network timeout, 503) — สั่งให้ API return error ได้ตามต้องการ
3. **ทดสอบบน environment ที่ไม่มี backend** (pure frontend test) — mock ทุก API calls ทำให้ test รันได้โดยไม่ต้องการ backend จริง

**Intermediate:**
```typescript
test('monthly report shows data correctly', async ({ page }) => {
  await page.route('**/api/reports/monthly*', route => {
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ revenue: 150000, orders: 42 }),
    });
  });
  await page.goto('/reports');
  await expect(page.getByTestId('revenue')).toHaveText('150,000');
});

test('monthly report shows error on API failure', async ({ page }) => {
  await page.route('**/api/reports/monthly*', route => {
    route.fulfill({ status: 500, body: 'Internal Server Error' });
  });
  await page.goto('/reports');
  await expect(page.getByTestId('error-msg')).toBeVisible();
});

test('auto-refresh after 5 minutes', async ({ page }) => {
  let callCount = 0;
  await page.route('**/api/reports/monthly*', route => {
    callCount++;
    route.fulfill({ status: 200, body: JSON.stringify({ revenue: callCount * 1000 }) });
  });
  
  // install clock ก่อน goto เสมอ
  await page.clock.install();
  await page.goto('/reports');
  await expect(page.getByTestId('revenue')).toHaveText('1,000');
  
  // ข้ามเวลา 5 นาที
  await page.clock.fastForward(5 * 60 * 1000);
  await expect(page.getByTestId('revenue')).toHaveText('2,000');
});
```

**Advanced:**
ปัญหา 3 จุด:

1. **Route ที่ 2 ไม่ override route ที่ 1 อย่างที่คิด** — Playwright เพิ่ม route handlers เป็น stack route ล่าสุดมี priority สูงกว่า แต่ route แรกยังอยู่ ในบางกรณีอาจ intercept ซ้ำกัน ควรใช้ `page.unroute()` ก่อนหรือแยกเป็น test คนละตัว

2. **Test รวม 2 scenarios ในตัวเดียว** — ถ้า assertion แรก fail (success message ไม่โผล่) test จะหยุดและ failure scenario ไม่ถูกทดสอบ ควรแยกเป็น 2 tests ที่ independent กัน

3. **ไม่ตรวจสอบว่า page state reset** — หลัง success scenario, page อาจแสดง success UI อยู่ การ click Pay Now อีกครั้งอาจไม่ trigger request ใหม่ถ้า button ถูก disabled หลัง payment สำเร็จ ต้องตรวจสอบ flow ก่อน assume button clickable

</details>

---

## บทที่ 13 — Authentication & Storage State

### Recall (Beginner)
อธิบายว่า `storageState` เก็บข้อมูลอะไรบ้าง และไม่เก็บอะไร — รวมถึงอธิบายว่าทำไม storageState ถึงช่วยให้ test suite เร็วขึ้นมากถ้ามี 80 tests ที่ต้อง login ก่อน

### Application (Intermediate)
คุณมีระบบ project management ที่มี 3 roles: `admin` (จัดการ users ได้), `manager` (สร้าง projects ได้), `viewer` (ดูอย่างเดียว) แต่ละ role ควรเห็น UI ต่างกัน

จงออกแบบ `playwright.config.ts` projects และ auth setup files เพื่อให้แต่ละ role login ครั้งเดียวและ tests ทุกตัวใช้ saved state ได้

### Synthesis (Advanced)
ทีม QA พบปัญหา: tests ที่รันพร้อมกัน (parallel) บางครั้ง fail แบบ intermittent โดยเฉพาะ tests ที่ create/delete data ใน user account เดียวกัน นี่คือ config ปัจจุบัน:

```typescript
// playwright.config.ts
{
  workers: 4,
  use: { storageState: 'playwright/.auth/user.json' },
  projects: [{ name: 'tests', testMatch: '**/*.spec.ts' }]
}

// auth.setup.ts
test('authenticate', async ({ page }) => {
  await page.goto('/login');
  await page.fill('#email', 'testuser@company.com');
  await page.fill('#password', 'password123');
  await page.click('#submit');
  await page.context().storageState({ path: 'playwright/.auth/user.json' });
});
```

วิเคราะห์ root cause และเสนอ solution ที่แก้ปัญหาได้จริงโดยไม่ลด workers

<details>
<summary>เฉลย</summary>

**Beginner:**
storageState เก็บ: cookies ทั้งหมด, localStorage ของทุก origin ที่ browser เข้าถึง และ IndexedDB (ต้องระบุ `{ indexedDB: true }` ตอนเรียก `storageState()` ด้วย)

storageState **ไม่เก็บ**: sessionStorage (ผูกกับ tab/session เดียว ไม่สามารถ serialize ข้าม context ได้), Web Crypto keys, in-memory JavaScript state

ประโยชน์ด้านความเร็ว: ถ้า login ผ่าน UI ใช้เวลา 3-5 วินาทีต่อครั้ง × 80 tests = 240-400 วินาที (~4-7 นาที) แค่สำหรับ login อย่างเดียว การ login ครั้งเดียวแล้ว reuse storageState ลดเหลือ < 1 วินาทีต่อ test (แค่อ่านไฟล์)

**Intermediate:**
```typescript
// playwright.config.ts
export default defineConfig({
  projects: [
    // Setup projects
    { name: 'setup-admin', testMatch: '**/admin.setup.ts' },
    { name: 'setup-manager', testMatch: '**/manager.setup.ts' },
    { name: 'setup-viewer', testMatch: '**/viewer.setup.ts' },
    
    // Test projects ที่ depend on setup
    {
      name: 'admin-tests',
      dependencies: ['setup-admin'],
      use: { storageState: 'playwright/.auth/admin.json' },
      testMatch: '**/admin/**/*.spec.ts',
    },
    {
      name: 'manager-tests',
      dependencies: ['setup-manager'],
      use: { storageState: 'playwright/.auth/manager.json' },
      testMatch: '**/manager/**/*.spec.ts',
    },
    {
      name: 'viewer-tests',
      dependencies: ['setup-viewer'],
      use: { storageState: 'playwright/.auth/viewer.json' },
      testMatch: '**/viewer/**/*.spec.ts',
    },
  ],
});
```

**Advanced:**
Root cause: 4 workers แชร์ storageState ไฟล์เดียวและ account เดียว → tests ที่รันพร้อมกันแก้ไขข้อมูลใน account เดียวกัน ทำให้ data conflict

Solution: Per-worker auth ใน Playwright ต้องใช้ **worker-scoped fixture** ไม่ใช่ function ใน config

ทำไม config function ถึงไม่ทำงาน: `storageState` ใน `playwright.config.ts` `use:` block รับค่า `string` หรือ `{ cookies, origins }` เท่านั้น — ไม่รองรับ function signature ใดๆ

วิธีที่ถูกต้อง — สร้าง worker-scoped fixture:

```typescript
// playwright/fixtures.ts
import { test as base } from '@playwright/test';

export const test = base.extend<{}, { workerStorageState: string }>({
  workerStorageState: [async ({ browser }, use, workerInfo) => {
    const id = workerInfo.parallelIndex;
    const fileName = `playwright/.auth/user-${id}.json`;

    // login ครั้งเดียวต่อ worker (ไม่ซ้ำถ้าไฟล์มีอยู่แล้ว)
    const page = await browser.newPage();
    await page.goto('http://localhost:3000/login');
    await page.fill('[data-testid="input-username"]', `testuser-${id}`);
    await page.fill('[data-testid="input-password"]', 'test123');
    await page.click('[data-testid="btn-login"]');
    await page.context().storageState({ path: fileName });
    await page.close();

    await use(fileName);
  }, { scope: 'worker' }],

  // override storageState fixture ให้ใช้ per-worker file
  storageState: async ({ workerStorageState }, use) => {
    await use(workerStorageState);
  },
});
```

import `test` จาก fixtures นี้แทน `@playwright/test` ในทุก test file ที่ต้องการ per-worker auth

ต้องสร้าง test accounts จริงใน test database สำหรับแต่ละ parallelIndex ด้วย

</details>

---

## บทที่ 14 — Advanced Browser & Emulation

### Recall (Beginner)
อธิบายว่าทำไมต้อง register `page.waitForEvent('popup')` ก่อน trigger action ที่เปิด popup — และจะเกิดอะไรถ้าลืม `await` หน้า `waitForEvent()`

### Application (Intermediate)
คุณกำลัง test ระบบ e-commerce ที่มี features เหล่านี้:
- ปุ่ม "Preview" เปิด product detail ในหน้าต่างใหม่
- หน้า checkout มี iframe ของ payment form จาก third-party
- ปุ่ม "ลบสินค้า" แสดง browser confirm dialog ก่อนลบจริง

จงเขียน test สำหรับแต่ละ feature โดยแสดง Playwright API ที่ใช้

### Synthesis (Advanced)
วิเคราะห์ test นี้แล้วระบุทุกปัญหาและ race condition ที่อาจเกิดขึ้น:

```typescript
test('download invoice and verify', async ({ page }) => {
  await page.goto('/orders');
  await page.getByRole('button', { name: 'Download Invoice' }).click();
  
  // รอ 3 วินาทีให้ไฟล์ download เสร็จ
  await page.waitForTimeout(3000);
  
  // verify ไฟล์มีอยู่
  const fs = require('fs');
  expect(fs.existsSync('./downloads/invoice.pdf')).toBe(true);
  
  // ทดสอบบน mobile
  await page.setViewportSize({ width: 375, height: 812 });
  await expect(page.getByTestId('mobile-menu')).toBeVisible();
});
```

<details>
<summary>เฉลย</summary>

**Beginner:**
Popup event เกิดขึ้น milliseconds หลัง click เริ่มต้น ถ้าเรียก `waitForEvent('popup')` หลัง click อาจ miss event ไปแล้ว ต้องเรียกก่อน click เสมอ:

```typescript
const popupPromise = page.waitForEvent('popup'); // register ก่อน
await page.getByRole('button', { name: 'Preview' }).click(); // trigger
const popup = await popupPromise; // รับ popup
```

ถ้าลืม `await` หน้า `waitForEvent()`: ได้รับ `Promise<Page>` แทน `Page` ทำให้ call methods บน popup ไม่ได้ TypeScript จะเตือน แต่ JavaScript ไม่เตือน

**Intermediate:**
```typescript
// Popup window
test('product preview opens in new window', async ({ page }) => {
  const popupPromise = page.waitForEvent('popup');
  await page.getByRole('button', { name: 'Preview' }).click();
  const popup = await popupPromise;
  await expect(popup.getByTestId('product-title')).toBeVisible();
  await popup.close();
});

// iframe payment form
test('fill payment form in iframe', async ({ page }) => {
  await page.goto('/checkout');
  const paymentFrame = page.frameLocator('[data-testid="payment-iframe"]');
  await paymentFrame.getByLabel('Card Number').fill('4111111111111111');
  await paymentFrame.getByLabel('Expiry').fill('12/28');
  await paymentFrame.getByLabel('CVV').fill('123');
});

// Confirm dialog
test('delete item shows confirmation', async ({ page }) => {
  page.on('dialog', dialog => dialog.accept()); // register ก่อน
  await page.getByRole('button', { name: 'ลบสินค้า' }).click();
  await expect(page.getByTestId('cart-empty')).toBeVisible();
});
```

**Advanced:**
ปัญหา 4 จุด:

1. **`waitForTimeout(3000)` เดาเวลา download** — download time ขึ้นกับ network speed และ file size บน CI ที่ช้าอาจใช้เวลากว่า 3 วินาที ควรใช้ `page.waitForEvent('download')` แทน

2. **Path ของ download file hardcode** — `./downloads/invoice.pdf` อาจไม่ตรงกับ download path ของ Playwright ที่ตั้งใน config ควรใช้ `download.path()` จาก download event

3. **`setViewportSize` หลังทดสอบ desktop** — เปลี่ยน viewport กลางทาง test ทำให้ state หน้า confuse ควรแยกเป็น 2 tests หรือตั้ง viewport ที่ต้องการใน `test.use()` ก่อน test

4. **`require('fs')` ใน test** — ควรใช้ built-in Node.js `import` หรือดีกว่าคือ assert ด้วย Playwright download API โดยตรง

Refactored download test:
```typescript
test('download invoice', async ({ page }) => {
  await page.goto('/orders');
  const downloadPromise = page.waitForEvent('download');
  await page.getByRole('button', { name: 'Download Invoice' }).click();
  const download = await downloadPromise;
  expect(download.suggestedFilename()).toContain('invoice');
  // ถ้าต้องการ verify content:
  const path = await download.path();
  expect(path).toBeTruthy();
});
```

</details>

---

## บทที่ 15 — API Testing + Hybrid

### Recall (Beginner)
อธิบายว่า "hybrid test" คืออะไร — และทำไมการ setup data ผ่าน API แล้ว verify ผ่าน UI ถึงดีกว่าการทำทุกอย่างผ่าน UI ทั้งหมด ในแง่ความเร็วและความ reliable

### Application (Intermediate)
คุณมี test ที่ต้องทดสอบว่า "ระบบแจ้งเตือนส่ง email เมื่อ order status เปลี่ยนเป็น 'shipped'" — system นี้มี REST API สำหรับ create orders และ update status

จงเขียน hybrid test ที่:
1. สร้าง order ผ่าน API
2. Update status เป็น 'shipped' ผ่าน API  
3. Verify ใน UI ว่า notification banner แสดงขึ้น
4. Cleanup ด้วย API หลัง test เสร็จ

### Synthesis (Advanced)
ทีมกำลัง debate ว่าจะ test endpoint `POST /api/products` อย่างไร มี 3 แนวทาง:

**แนวทาง A**: Pure API test — ใช้ `request` fixture เรียก endpoint โดยตรง assert response status และ body
**แนวทาง B**: Pure UI test — เปิดหน้า admin, กรอก form, submit, verify สินค้าปรากฏในรายการ
**แนวทาง C**: Hybrid — ใช้ API สร้าง product แล้ว verify ใน admin UI ว่าแสดงถูกต้อง

วิเคราะห์ trade-off ของแต่ละแนวทางและเสนอว่าในสถานการณ์ต่างกันควรใช้อะไร

<details>
<summary>เฉลย</summary>

**Beginner:**
Hybrid test คือ test ที่ผสม API calls กับ UI verification — setup data เร็วๆ ผ่าน API แล้ว verify ผ่าน UI ที่ user เห็นจริง

ทำไมดีกว่า pure UI:
- ความเร็ว: API call ~50ms vs UI form fill 2-3 วินาทีต่อ field — สำหรับ 10 fields ต่างกัน ~20-30 เท่า
- ความ reliable: API contract เปลี่ยนน้อยกว่า UI — ถ้า UI เปลี่ยน layout, API test ยังผ่าน

**Intermediate:**
```typescript
test('email notification sent when order shipped', async ({ page, request }) => {
  // 1. Setup: สร้าง order ผ่าน API
  const createRes = await request.post('/api/orders', {
    data: { productId: 'prod-001', quantity: 1, customerId: 'cust-test' }
  });
  expect(createRes.ok()).toBeTruthy();
  const { orderId } = await createRes.json();
  
  // 2. Update status ผ่าน API
  const updateRes = await request.patch(`/api/orders/${orderId}`, {
    data: { status: 'shipped' }
  });
  expect(updateRes.ok()).toBeTruthy();
  
  // 3. Verify notification ใน UI
  await page.goto(`/orders/${orderId}`);
  await expect(page.getByRole('alert')).toContainText('อีเมลแจ้งเตือนถูกส่งแล้ว');
  
  // 4. Cleanup
  await request.delete(`/api/orders/${orderId}`);
});
```

**Advanced:**
Trade-off analysis:

**แนวทาง A (Pure API)**
- ✅ เร็วที่สุด (~100ms)
- ✅ Test business logic โดยตรง ไม่ขึ้นกับ UI
- ❌ ไม่ตรวจว่า UI render data ถูกต้อง
- ✅ ใช้: สำหรับ validate API contract, edge cases, error handling (400/422/500)

**แนวทาง B (Pure UI)**
- ✅ Test user journey จริง end-to-end
- ❌ ช้าที่สุด (~30-60 วินาที)
- ❌ เปราะ — form UI เปลี่ยนทำให้ test พัง
- ✅ ใช้: smoke test ที่ต้องการ verify full stack ครั้งเดียว

**แนวทาง C (Hybrid)**
- ✅ Fast setup + real UI verification
- ✅ Test integration ระหว่าง API และ UI
- ❌ ไม่ test UI creation flow เอง
- ✅ ใช้: เมื่อมี scenarios จำนวนมากที่ต้องการ UI verification แต่ setup cost สูง

**คำแนะนำ**: ใช้ทั้งสามในสัดส่วนที่เหมาะสม — A สำหรับ API contract tests (bulk), B สำหรับ critical user journeys (น้อย), C สำหรับ UI display tests ที่มี data หลากหลาย

</details>

---

## บทที่ 16 — Visual & Accessibility Testing

### Recall (Beginner)
อธิบายว่าทำไม `toHaveScreenshot()` ถึงต้องสร้าง baseline บน environment เดียวกับ CI — และ `toMatchAriaSnapshot()` แก้ปัญหา cross-platform นี้ได้อย่างไร

### Application (Intermediate)
คุณกำลัง test component "Product Card" ที่ใช้ทั่ว e-commerce site ซึ่งมี: รูปสินค้า, ชื่อสินค้า, ราคา, badge "ลดราคา" (แสดงถ้ามี discount), และปุ่ม "เพิ่มลงตะกร้า"

จงออกแบบ test ที่ตรวจ visual regression และ accessibility พร้อมกัน โดยระบุว่าแต่ละ assertion ตรวจอะไร

### Synthesis (Advanced)
ทีมพบปัญหา: `toHaveScreenshot()` fail บน CI ทุกครั้งแม้ UI ไม่ได้เปลี่ยน error message บอกว่า "screenshot differs by 3.2%" developer สงสัยว่าเกิดจาก font rendering ต่างกัน

ออกแบบ investigation plan ทีละขั้นเพื่อ diagnose และแก้ปัญหา โดยไม่แก้ด้วยการเพิ่ม threshold แบบสุ่ม

<details>
<summary>เฉลย</summary>

**Beginner:**
`toHaveScreenshot()` เปรียบเทียบ pixel-by-pixel — font rendering, anti-aliasing, และ subpixel rendering แตกต่างกันระหว่าง macOS/Linux/Windows ทำให้ screenshot บน macOS และ Linux ต่างกันแม้ UI จะเหมือนกัน

`toMatchAriaSnapshot()` ตรวจ accessibility tree (roles, names, states) ซึ่งเป็น semantic structure ไม่ใช่ pixels จึงทำงานเหมือนกันทุก OS

**Intermediate:**
```typescript
test('product card visual and accessibility', async ({ page }) => {
  await page.goto('/products/sample');
  const card = page.getByTestId('product-card');
  
  // Visual regression — ต้องรัน update-snapshots ครั้งแรก
  await expect(card).toHaveScreenshot('product-card.png', {
    maxDiffPixelRatio: 0.01, // tolerance เล็กน้อยสำหรับ anti-aliasing
  });
  
  // Accessibility — ตรวจ structure สำคัญ
  await expect(card).toMatchAriaSnapshot(`
    - img "รูปสินค้า"
    - heading "ชื่อสินค้า"
    - text: /฿[\d,]+/
    - button "เพิ่มลงตะกร้า"
  `);
  
  // Axe accessibility scan
  const { violations } = await new AxeBuilder({ page })
    .include('[data-testid="product-card"]')
    .analyze();
  expect(violations).toEqual([]);
});

test('product card with discount badge', async ({ page }) => {
  await page.goto('/products/sale-item');
  const card = page.getByTestId('product-card');
  
  // ตรวจ badge มีอยู่และ accessible
  await expect(card.getByRole('status')).toHaveText('ลดราคา 20%');
  await expect(card).toHaveScreenshot('product-card-sale.png');
});
```

**Advanced:**
Investigation plan:

**ขั้นที่ 1: ยืนยัน hypothesis**
- Download artifact `test-results/` จาก CI
- ดู diff image ที่ Playwright สร้าง (ไฟล์ `-diff.png`) — ถ้า diff เป็น noise ละเอียดทั่วภาพ = font rendering, ถ้าเป็น specific area = content เปลี่ยน

**ขั้นที่ 2: ตรวจ environment**
- CI workflow ใช้ OS อะไร (Ubuntu version)
- baseline image สร้างบน OS อะไร ดูจาก filename (Playwright append `-linux.png` หรือ `-darwin.png`)
- ถ้า baseline เป็น `-darwin.png` แต่ CI เป็น Linux — นั่นคือสาเหตุ

**ขั้นที่ 3: แก้ที่ต้นเหตุ**
- Option A: สร้าง baseline ใหม่บน Linux Docker container เดียวกับ CI: `docker run mcr.microsoft.com/playwright:v1.50.0-jammy npx playwright test --update-snapshots`
- Option B: เพิ่ม `--project=chromium` และ screenshot เฉพาะ Chromium เพื่อ consistency
- Option C: ใช้ `toMatchAriaSnapshot()` แทน `toHaveScreenshot()` สำหรับ structural tests ที่ไม่ต้องการ pixel-perfect

**ไม่แนะนำ**: เพิ่ม `maxDiffPixelRatio: 0.05` แบบสุ่มเพราะจะทำให้ miss visual bugs จริงๆ ได้ในอนาคต

</details>

---

## บทที่ 17 — CI/CD Integration

### Recall (Beginner)
อธิบายว่า `forbidOnly: !!process.env.CI` ทำอะไร และทำไมถึงจำเป็นใน CI pipeline — ยกตัวอย่างสถานการณ์จริงที่จะเกิดปัญหาถ้าไม่มี option นี้

### Application (Intermediate)
คุณต้องสร้าง GitHub Actions workflow สำหรับ Playwright test suite ที่มี 240 tests โดยมีข้อกำหนด:
- รันทุก Pull Request
- ต้องเสร็จภายใน 8 นาที
- ถ้า test fail ต้องเห็น trace ได้ใน GitHub
- Upload HTML report เป็น artifact เสมอ

จงเขียน `.github/workflows/playwright.yml` ที่ตอบโจทย์ทุกข้อ

### Synthesis (Advanced)
ทีมมี CI pipeline ที่ทำงานช้า: 350 tests ใช้เวลา 28 นาทีบน GitHub Actions (1 runner, 2 workers) และ workflow นี้:

```yaml
name: Tests
on: [push]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
      - run: npm ci
      - run: npx playwright install --with-deps
      - run: npx playwright test
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: playwright-report
          path: playwright-report/
```

ออกแบบ optimization plan ให้เสร็จภายใน 7 นาที โดยแสดง workflow ที่แก้แล้ว

<details>
<summary>เฉลย</summary>

**Beginner:**
`forbidOnly: !!process.env.CI` ทำให้ Playwright throw error ถ้ามี `test.only()` หรือ `describe.only()` ใน code เมื่อ `CI=true`

สถานการณ์ปัญหา: developer debug test โดยเพิ่ม `test.only('login flow')` แล้วลืมลบก่อน push — ถ้าไม่มี `forbidOnly`, CI จะรันแค่ test เดียวนั้นและ pass ทั้ง pipeline แม้ tests อื่นทั้งหมดยังไม่ได้รัน developer merge code โดยไม่รู้ว่า test suite ยังมี bugs อยู่

**Intermediate:**
```yaml
name: Playwright Tests
on: [pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        shard: [1, 2, 3]  # 240 ÷ 3 = 80 tests ต่อ shard
    
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
      
      - run: npm ci
      
      - name: Install Playwright Browsers
        run: npx playwright install --with-deps chromium
      
      - name: Run Tests (Shard ${{ matrix.shard }}/3)
        run: npx playwright test --shard=${{ matrix.shard }}/3 --reporter=blob
        env:
          CI: true
      
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: blob-report-${{ matrix.shard }}
          path: blob-report/
          retention-days: 1
  
  merge-reports:
    if: always()
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
      - run: npm ci
      
      - uses: actions/download-artifact@v4
        with:
          path: all-blob-reports
          pattern: blob-report-*
          merge-multiple: true
      
      - name: Merge Reports
        run: npx playwright merge-reports --reporter html ./all-blob-reports
      
      - uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: playwright-report/
          retention-days: 30
```

**Advanced:**
```yaml
name: Playwright Tests (Optimized)
on: [push]

jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        shard: [1, 2, 3, 4]  # 4 shards × ~2 workers = ~8x speedup
    
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'  # cache npm แทน npm ci เปล่า
      
      - run: npm ci
      
      # Cache playwright browsers ข้าม runs
      - name: Cache Playwright Browsers
        uses: actions/cache@v4
        with:
          path: ~/.cache/ms-playwright
          key: playwright-${{ runner.os }}-${{ hashFiles('**/package-lock.json') }}
      
      - name: Install Playwright Browsers (if not cached)
        run: npx playwright install --with-deps chromium
      
      - name: Run Shard ${{ matrix.shard }}/4
        run: npx playwright test --shard=${{ matrix.shard }}/4 --reporter=blob
        env:
          CI: true
      
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: blob-${{ matrix.shard }}
          path: blob-report/
          retention-days: 1
  
  report:
    if: always()
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: npm ci
      - uses: actions/download-artifact@v4
        with:
          path: blobs
          pattern: blob-*
          merge-multiple: true
      - run: npx playwright merge-reports --reporter html ./blobs
      - uses: actions/upload-artifact@v4
        with:
          name: html-report
          path: playwright-report/
```

Optimizations หลัก:
1. 4 shards แทน 1 → ~4x speedup → 7 นาที (28÷4)
2. Browser cache → ลด install time ~2 นาทีต่อ run
3. npm cache → ลด npm ci time
4. `fail-fast: false` → ทุก shard รันต่อแม้ shard อื่น fail (เห็น failures ทั้งหมด)

</details>

---

## บทที่ 18 — Production Patterns

### Recall (Beginner)
อธิบายว่า "flaky test" คืออะไร และทำไมมันถึงเป็นปัญหาร้ายแรงกับทีม — ระบุสาเหตุที่พบบ่อยที่สุด 3 อย่างที่ทำให้ test flaky

### Application (Intermediate)
คุณกำลัง scale test suite จาก 50 tests เป็น 300 tests สำหรับ SaaS platform ที่มีหลาย modules (Users, Billing, Reports, Settings) tests ทั้งหมดรันใน CI ด้วย 4 workers

จงออกแบบ folder structure, tagging strategy, และ test data management strategy ที่ทำให้ suite maintain ได้เมื่อมี 300 tests

### Synthesis (Advanced)
ทีมมีปัญหาใหญ่: test suite 280 tests มี 35 tests ที่ flaky (fail intermittently) และ CI ไม่ reliable เลยตอนนี้ นี่คือ sample ของ flaky tests:

```typescript
// Test 1 — fail ~30% ของเวลา
test('user count updates after bulk import', async ({ page }) => {
  await request.post('/api/users/bulk-import', { data: users100 });
  await page.goto('/admin/users');
  await expect(page.getByTestId('user-count')).toHaveText('100');
});

// Test 2 — fail ~20% ของเวลา  
test('session expires after timeout', async ({ page }) => {
  await page.goto('/dashboard');
  await page.clock.fastForward(30 * 60 * 1000); // 30 นาที
  await page.reload();
  await expect(page).toHaveURL('/login');
});

// Test 3 — fail ~15% ของเวลา
test('search returns results', async ({ page }) => {
  await page.goto('/search?q=laptop');
  const results = page.getByRole('listitem');
  await expect(results).toHaveCount(10);
});
```

วิเคราะห์ root cause ของแต่ละ test และเสนอแนวทางแก้ที่ถูกต้อง

<details>
<summary>เฉลย</summary>

**Beginner:**
Flaky test คือ test ที่ result ไม่แน่นอน — บางรอบผ่าน บางรอบพัง โดยที่ code ไม่ได้เปลี่ยน

ปัญหาร้ายแรงกับทีม: ทีมหยุดเชื่อ CI ("แค่ retry ไป"), developer merge code โดยไม่แน่ใจว่า test ผ่านจริง, และ debugging ยากเพราะ reproduce ไม่ได้สม่ำเสมอ

3 สาเหตุที่พบบ่อย:
1. **Race conditions / timing** — test assume ว่า async operation เสร็จแล้วทั้งที่ยังรอ
2. **Shared state** — tests รัน parallel แชร์ database หรือ file ทำให้ข้อมูลปนกัน
3. **External dependencies** — test ขึ้นกับ third-party service, network, หรือ system clock ที่ควบคุมไม่ได้

**Intermediate:**
```
tests/
├── users/
│   ├── user-management.spec.ts    # @smoke @users
│   ├── bulk-operations.spec.ts    # @regression @users
│   └── permissions.spec.ts        # @regression @users
├── billing/
│   ├── subscription.spec.ts       # @smoke @billing
│   └── invoices.spec.ts           # @regression @billing
├── reports/
│   └── monthly-reports.spec.ts    # @regression @reports @slow
└── settings/
    └── account-settings.spec.ts   # @smoke @settings

fixtures/
├── user-fixture.ts
├── billing-fixture.ts
└── test-data-factory.ts  # สร้าง unique data ต่อ test
```

Test Data Factory pattern:
```typescript
// test-data-factory.ts
export function createUniqueUser(workerIndex: number, testId: string) {
  return {
    email: `test-${workerIndex}-${testId}-${Date.now()}@example.com`,
    // unique ต่อ worker และ test
  };
}
```

Tagging strategy: `@smoke` (critical path, ~20 tests), `@regression` (full coverage), `@slow` (>10 วินาที), `@[module]` (users/billing/reports/settings)

**Advanced:**

**Test 1 (bulk import):**
Root cause: `POST /api/bulk-import` return 200 ก่อน import เสร็จจริง (async job) แต่ test navigate ไป `/admin/users` ทันที ก่อน count อัปเดต
แก้: รอ job complete ด้วย polling หรือ webhook — หรือ mock API ให้ return count ที่ต้องการโดยตรง:
```typescript
await request.post('/api/users/bulk-import', { data: users100 });
// รอ job status เป็น complete ก่อน navigate
await expect.poll(async () => {
  const res = await request.get('/api/import-status');
  return (await res.json()).status;
}).toBe('complete');
await page.goto('/admin/users');
```

**Test 2 (session timeout):**
Root cause: `clock.fastForward()` ข้ามเวลาใน browser แต่ session token validate บน server ซึ่งใช้ real time ไม่ใช่ fake clock จาก client
แก้: ต้องทำงานที่ server level — ใช้ API เพื่อ expire session โดยตรง หรือ mock server-side time check:
```typescript
// ผ่าน API หรือ feature flag ทำให้ session expire
await request.post('/api/test/expire-session', { data: { userId } });
await page.reload();
await expect(page).toHaveURL('/login');
```

**Test 3 (search results):**
Root cause: หน้า search อาจ load results แบบ lazy (pagination หรือ infinite scroll) `haveCount(10)` check ณ จุดหนึ่งแต่ items อาจยังไม่ render ครบ หรือ test data ใน database ไม่แน่นอน (tests อื่น delete/create laptop records)
แก้: ใช้ stable data ที่ test control ได้:
```typescript
// สร้าง test data ที่รู้จำนวนชัดเจน
await request.post('/api/test/seed-search-data', {
  data: { query: 'laptop', count: 10, testId: testInfo.testId }
});
await page.goto(`/search?q=laptop&testId=${testInfo.testId}`);
// รอ results โหลดครบ (อย่า count ทันที)
await expect(page.getByRole('listitem').first()).toBeVisible();
await expect(page.getByRole('listitem')).toHaveCount(10);
```

</details>

---

---

## บทที่ 19 — Database State Verification: ปิด Loop ด้วยการตรวจ DB

### L1: Recall (ขั้นพื้นฐาน)

อธิบายด้วยคำตัวเองว่าทำไมการที่ UI แสดง "สร้างสำเร็จ" แล้วก็เพียงพอในการยืนยันว่าข้อมูลถูกบันทึกลง database หรือเปล่า — ยกตัวอย่างสถานการณ์จากชีวิตจริง (ไม่ต้องเป็น IT) ที่ "สิ่งที่เห็นบนหน้าจอ" ไม่ตรงกับ "ข้อมูลที่จริงๆ อยู่ในระบบ backend" และอธิบายว่า API Read-back pattern แก้ปัญหานี้อย่างไร

<details>
<summary>เฉลย</summary>

UI แสดง success ไม่ได้หมายความว่า DB บันทึกสำเร็จ เพราะ UI อาจแสดงจาก: (1) optimistic update — UI update ล่วงหน้าก่อน server confirm (2) in-memory state ที่ยังไม่ได้ persist (3) race condition ที่ server ตอบก่อน DB write เสร็จ

ตัวอย่างจากชีวิตจริง: เหมือนระบบจองตั๋วคอนเสิร์ต — เว็บไซต์แสดง "จองสำเร็จ! ที่นั่ง A12" แต่ถ้าระบบ backend มีปัญหา ตั๋วจริงอาจไม่ถูก issue ในระบบ E-Ticket ทำให้วันงานผ่านประตูไม่ได้ สิ่งที่เห็นบนหน้าจอ ≠ สถานะจริงในระบบ

API Read-back แก้ปัญหานี้โดย: หลัง UI แสดง success → call GET API → ถามระบบโดยตรงว่า "ข้อมูลมีอยู่ใน DB จริงไหม" ถ้า DB ไม่มี → test fail → เจอ bug ก่อน production

</details>

---

### L2: Recognition (อ่าน code + ระบุปัญหา)

ตรวจสอบ code ต่อไปนี้แล้วระบุปัญหาทุกจุดพร้อมอธิบายว่าแต่ละปัญหาจะทำให้ test fail หรือพลาด bug อย่างไร:

```typescript
test('product added to cart is saved in database', async ({ page, request }) => {
  await page.goto('http://localhost:3000/shop');
  await page.getByTestId('product-card-1').getByRole('button', { name: 'Add to Cart' }).click();
  
  // Verify immediately without waiting for UI
  const response = await request.get('http://localhost:3000/api/orders');
  expect(response.status()).toBe(200);
  const orders = await response.json();
  expect(orders.length).toBeGreaterThan(0);
});
```

**hint:** มีปัญหาอย่างน้อย 3 จุดที่ทำให้ test ผิดพลาด

<details>
<summary>เฉลย</summary>

ปัญหา 3 จุด:

1. **`GET /api/orders` ไม่มีใน demo app** — server.js ไม่ได้ define endpoint นี้ → response จะเป็น 404 ไม่ใช่ 200 ทำให้ test fail ผิดเหตุผล ต้องใช้ direct file read (`readFileSync`) หรือ `GET /api/admin` stats แทน

2. **ไม่รอ UI confirm ก่อน verify** — หลัง click "Add to Cart" ควรรอให้ UI แสดง feedback ก่อน (เช่น cart icon update หรือ toast message) เพื่อให้แน่ใจว่า server ได้รับ request แล้ว ถ้า verify ทันทีอาจ check DB ก่อนที่ request จะถูก process

3. **ไม่มี beforeEach reset / DB isolation** — ถ้า test อื่นสร้าง orders ไว้ก่อน `orders.length > 0` จะ pass แม้ว่า cart action นี้ไม่ได้ save อะไรจริงๆ เลย

</details>

---

### L3: Guided Application (เขียนตาม scenario)

เขียน Playwright test ที่ทำตาม steps เหล่านี้ครบถ้วน:

**Scenario:** ทดสอบว่า "ลบ todo แล้ว — ยืนยันทั้ง UI และ DB ว่าหายจริง"

**Steps ที่ต้องทำ (ตามลำดับ):**
1. Reset DB ก่อน test ด้วย `POST /api/reset` ใน `test.beforeEach`
2. สร้าง todo ข้อความ "Finish project report" ผ่าน API (`POST /api/todos`)
3. เปิดหน้า todos ใน browser และ verify ว่า todo ปรากฏใน UI
4. ลบ todo ผ่าน API (`DELETE /api/todos/:id`) และ verify response ok
5. **API Read-back**: ดึง `GET /api/todos` และ verify ว่า todo ไม่อยู่ใน array แล้ว (ใช้ `not.toContainEqual`)
6. Reload หน้าใน browser และ verify ว่า todo หายจาก UI ด้วย

**Constraints:**
- ใช้ `expect.objectContaining` สำหรับ partial match
- ใช้ `not.toContainEqual` สำหรับ negative verification
- ต้องมี import statements ที่ถูกต้อง

<details>
<summary>เฉลย</summary>

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

test.beforeEach(async ({ request }) => {
  await request.post('http://localhost:3000/api/reset');
});

test('deleted todo is removed from both UI and database', async ({ page, request }) => {
  // Step 2: สร้าง todo ผ่าน API
  const createRes = await request.post('http://localhost:3000/api/todos', {
    data: { text: 'Finish project report' }
  });
  expect(createRes.status()).toBe(201);
  const { id } = await createRes.json();

  // Step 3: verify ใน UI
  await page.goto('http://localhost:3000/todos');
  await expect(page.getByTestId(`todo-item-${id}`)).toBeVisible();

  // Step 4: ลบผ่าน API
  const deleteRes = await request.delete(`http://localhost:3000/api/todos/${id}`);
  expect(deleteRes.ok()).toBeTruthy();

  // Step 5: API Read-back — negative verification
  const todosRes = await request.get('http://localhost:3000/api/todos');
  const todos = await todosRes.json();
  expect(todos).not.toContainEqual(expect.objectContaining({ id }));

  // Step 6: UI verify หลัง reload
  await page.reload();
  await expect(page.getByTestId(`todo-item-${id}`)).not.toBeVisible();
});
```

</details>

---

### L4: Independent Application (ออกแบบเอง)

**Scenario:** คุณกำลัง test ระบบ "ระบบจัดการงาน (Task Manager)" ที่มีฟีเจอร์:
- `POST /api/tasks` สร้าง task ใหม่ มี field: `title`, `priority` (low/medium/high), `assignee`
- `PATCH /api/tasks/:id` อัปเดต task — รวมถึงเปลี่ยน priority ได้
- `GET /api/tasks` ดึงทุก task
- UI แสดง tasks เรียงตาม priority (high ก่อน)

ออกแบบและเขียน test suite (3 tests) ที่ verify ทั้ง:
1. **สร้าง task** — ยืนยันว่า DB มีข้อมูลครบทุก field ที่ส่งไป
2. **Update priority** — UI แสดง high priority task ก่อน AND DB เก็บ priority ถูกต้อง (cross-layer)
3. **Negative case** — task ที่ถูกลบไม่อยู่ใน DB อีกต่อไป

**ไม่มี scaffold ให้** — ออกแบบ structure เองทั้งหมด รวมถึง beforeEach/cleanup strategy

<details>
<summary>เฉลย</summary>

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

const BASE = 'http://localhost:3000';

test.beforeEach(async ({ request }) => {
  // Assume มี reset endpoint สำหรับ tasks
  await request.post(`${BASE}/api/tasks/reset`);
});

// Test 1: สร้าง task — verify ทุก field ใน DB
test('task creation saves all fields to database', async ({ request }) => {
  const res = await request.post(`${BASE}/api/tasks`, {
    data: { title: 'Deploy hotfix', priority: 'high', assignee: 'alice' }
  });
  expect(res.status()).toBe(201);
  const { id } = await res.json();

  const tasksRes = await request.get(`${BASE}/api/tasks`);
  const tasks = await tasksRes.json();
  expect(tasks).toContainEqual(
    expect.objectContaining({
      id,
      title: 'Deploy hotfix',
      priority: 'high',
      assignee: 'alice',
    })
  );
});

// Test 2: Update priority — cross-layer
test('updating priority reflects in database and UI sort order', async ({ page, request }) => {
  // Setup: สร้าง 2 tasks
  const lowRes = await request.post(`${BASE}/api/tasks`, {
    data: { title: 'Write docs', priority: 'low', assignee: 'bob' }
  });
  const lowTask = await lowRes.json();

  const highRes = await request.post(`${BASE}/api/tasks`, {
    data: { title: 'Fix prod bug', priority: 'high', assignee: 'alice' }
  });
  const highTask = await highRes.json();

  // Upgrade low task เป็น high
  await request.patch(`${BASE}/api/tasks/${lowTask.id}`, {
    data: { priority: 'high' }
  });

  // API verify
  const tasks = await (await request.get(`${BASE}/api/tasks`)).json();
  const updated = tasks.find((t: any) => t.id === lowTask.id);
  expect(updated?.priority).toBe('high');

  // UI verify: ทั้งสอง tasks เป็น high priority — ต้องแสดงก่อน low ones
  await page.goto(`${BASE}/tasks`);
  const firstTask = page.getByTestId('task-list').locator('[data-priority="high"]').first();
  await expect(firstTask).toBeVisible();
});

// Test 3: Negative — ลบ task แล้วหายจาก DB
test('deleted task is removed from database', async ({ request }) => {
  const res = await request.post(`${BASE}/api/tasks`, {
    data: { title: 'Cleanup temp files', priority: 'low', assignee: 'charlie' }
  });
  const { id } = await res.json();

  await request.delete(`${BASE}/api/tasks/${id}`);

  const tasks = await (await request.get(`${BASE}/api/tasks`)).json();
  expect(tasks).not.toContainEqual(expect.objectContaining({ id }));
});
```

Key design decisions:
- `beforeEach` reset สำหรับ isolation
- Test 2 setup 2 tasks เพื่อมี context สำหรับ sort order test
- Test 3 ใช้ API setup + API delete (ไม่ต้องการ UI สำหรับ deletion path ที่ test แค่ DB)

</details>

---

### L5: Expert / Synthesis (Production-grade)

**Context:** ทีม QA ของคุณมี test suite 400 tests รันบน GitHub Actions ด้วย 4 workers (parallel) tests เริ่ม fail บน CI ~25% โดยเฉพาะ tests ที่ verify todo counts — แต่ pass ทุกครั้งบน local (single worker)

นี่คือ test ที่เป็นปัญหา:

```typescript
test.beforeEach(async ({ request }) => {
  await request.post('http://localhost:3000/api/reset');
});

test('admin stats shows correct todo count after bulk create', async ({ request }) => {
  // สร้าง 3 todos
  await request.post('http://localhost:3000/api/todos', { data: { text: 'Sprint planning' } });
  await request.post('http://localhost:3000/api/todos', { data: { text: 'Code review' } });
  await request.post('http://localhost:3000/api/todos', { data: { text: 'Deploy checklist' } });

  // Login
  const { token } = await (await request.post('http://localhost:3000/api/auth/login', {
    data: { username: 'admin', password: 'admin123' }
  })).json();

  // Verify count
  const { stats } = await (await request.get('http://localhost:3000/api/admin', {
    headers: { Authorization: `Bearer ${token}` }
  })).json();

  expect(stats.todos).toBe(3);  // ← fail intermittently ใน parallel
});
```

**คำถาม (ต้องตอบครบทุกข้อ):**

1. **Root Cause Analysis:** อธิบายอย่างละเอียดว่าทำไม test นี้ pass บน local (single worker) แต่ fail ใน parallel CI — trace ผ่าน sequence ของ events ที่ทำให้เกิด race condition

2. **Fix with Code:** เขียน solution ที่แก้ปัญหาโดยไม่ลด workers — เลือก approach ที่เหมาะสมที่สุดระหว่าง: (A) worker-scoped fixture กับ unique data prefix (B) per-test DB snapshot restore (C) mock API endpoint แทน real DB

3. **Trade-off Analysis:** เปรียบเทียบ 3 approaches:
   - `beforeEach` reset (approach ปัจจุบัน)
   - Worker-scoped fixture + unique prefix
   - Mock `/api/admin` stats per test
   
   ระบุ: ✅ ข้อดี / ❌ ข้อเสีย / 🎯 เหมาะเมื่อไหร่

4. **Retrofit Plan:** ถ้า 400 tests ใช้ approach ปัจจุบัน (beforeEach reset) ทั้งหมด — จะ migrate ไป worker-scoped fixture อย่างไรโดยไม่ rewrite ทุก test

<details>
<summary>เฉลย</summary>

**1. Root Cause Analysis:**

Race condition เกิดเพราะ `POST /api/reset` ใช้ instance เดียวกันของ demo app ที่รัน file-based DB (`db.json`) บน shared server:

Timeline ใน parallel (4 workers):
```
Worker 1: POST /api/reset  → db.todos = []
Worker 2: POST /api/reset  → db.todos = []  ← W1 เพิ่งล้าง, W2 ล้างซ้ำ (OK ตอนนี้)
Worker 1: POST /api/todos (Sprint planning) → db.todos = [A]
Worker 2: POST /api/todos (งานอื่น)         → db.todos = [A, X]  ← ปนกัน!
Worker 1: GET /api/admin → stats.todos = 2  ← expect 3, ได้ 2 → FAIL
```

Local pass เพราะ single worker → ไม่มี concurrent requests → ไม่มี race condition

**2. Fix with Worker-Scoped Fixture + Unique Prefix:**

```typescript
// tests/fixtures.ts
import { test as base } from '@playwright/test';

type WorkerFixtures = {
  workerPrefix: string;
};

export const test = base.extend<{}, WorkerFixtures>({
  workerPrefix: [async ({}, use, workerInfo) => {
    // Unique prefix ต่อ worker: "w0", "w1", "w2", "w3"
    await use(`w${workerInfo.parallelIndex}`);
  }, { scope: 'worker' }],
});

// tests/admin-stats.spec.ts
import { test } from './fixtures';
import { expect } from '@playwright/test';

test('admin stats correct for worker-isolated todos', async ({ request, workerPrefix }) => {
  // สร้าง todos พร้อม prefix เพื่อ isolate ต่อ worker
  await request.post('http://localhost:3000/api/todos', {
    data: { text: `${workerPrefix}:Sprint planning` }
  });
  await request.post('http://localhost:3000/api/todos', {
    data: { text: `${workerPrefix}:Code review` }
  });
  await request.post('http://localhost:3000/api/todos', {
    data: { text: `${workerPrefix}:Deploy checklist` }
  });

  // ดึง todos แล้วนับเฉพาะ prefix ของ worker นี้
  const todos = await (await request.get('http://localhost:3000/api/todos')).json();
  const myTodos = todos.filter((t: any) => t.text.startsWith(`${workerPrefix}:`));
  expect(myTodos).toHaveLength(3);

  // Admin stats ยังตรวจได้ แต่ใช้ filtered count แทน total
  // (สำหรับ stats.todos ที่ count ทั้งหมด — ต้องใช้ approach อื่น)
});
```

**3. Trade-off Analysis:**

**beforeEach reset (ปัจจุบัน):**
- ✅ เข้าใจง่าย, setup น้อย
- ❌ Race condition ชัดเจนใน parallel — tests รัน server เดียวกัน
- 🎯 เหมาะสำหรับ: single worker หรือ sequential tests เท่านั้น

**Worker-scoped fixture + unique prefix:**
- ✅ Parallel-safe โดยไม่ต้องมี isolated server
- ✅ Performance ดี — ไม่ต้อง reset DB ทุก test
- ❌ Tests ต้อง filter data ด้วย prefix เพิ่มความซับซ้อน
- ❌ global count (เช่น `stats.todos`) ยังคง contaminated
- 🎯 เหมาะสำหรับ: tests ที่ filter data ได้ และไม่ต้องการ global counts

**Mock `/api/admin` stats per test:**
- ✅ 100% isolated — ไม่ขึ้นกับ DB state จริง
- ✅ Fast
- ❌ ไม่ได้ test integration จริง — mock อาจ diverge จาก real behavior
- 🎯 เหมาะสำหรับ: unit-level tests ที่ test UI rendering จาก data ที่รู้แน่ ไม่ใช่ DB verification

**4. Retrofit Plan:**

1. สร้าง `tests/fixtures.ts` พร้อม `workerPrefix` fixture และ export `test`
2. ใน tests ที่ต้องการ DB isolation → import `test` จาก `./fixtures` แทน `@playwright/test`
3. เพิ่ม `workerPrefix` parameter ใน test functions ที่ต้องการ
4. เปลี่ยน `beforeEach` reset → ใช้ cleanup ใน `afterEach` แทน (หรือไม่ reset เลยถ้าใช้ prefix)
5. Migration: เริ่มจาก tests ที่ fail บ่อยที่สุดก่อน (25% failure rate = ~100 tests) → migrate เป็น batch ทีละ spec file

ไม่ต้อง rewrite ทุก test พร้อมกัน — tests ที่ยังใช้ `beforeEach` reset แบบเก่าและรัน single worker ยังทำงานได้ปกติ

</details>

---

*แบบฝึกหัดหลัก: 18 บท × 3 ระดับ = 54 exercises*  
*Ch19: 5 levels (L1–L5) สำหรับ Database State Verification*  
*Expert Level (L5) เพิ่มเติม: Ch13, Ch15, Ch17, Ch18 (ดูด้านล่าง)*  
*สร้างโดยอิงเนื้อหาจากแต่ละบท — exercises ทุกข้อใช้สถานการณ์ใหม่ที่ไม่ซ้ำตัวอย่างในบท*

---

## Expert Level — เพิ่มเติม (L5 สำหรับบทสำคัญ)

### Ch13 Expert: Authentication + DB State Verification

**Context:** ทีมของคุณ migrate จาก cookie-based เป็น JWT + `Authorization` header โดย storageState ยังเก็บ cookies เหมือนเดิม แต่ `request` fixture กลับ return 401 เมื่อ call protected API

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

test('admin can verify stats match database', async ({ page, request }) => {
  await page.goto('http://localhost:3000/admin');
  await expect(page.getByTestId('stats-panel')).toBeVisible();

  // DB verification via API — fails with 401
  const statsRes = await request.get('http://localhost:3000/api/admin');
  expect(statsRes.status()).toBe(200);  // ← 401 จริงๆ
  const { stats } = await statsRes.json();
  // cross-verify ว่า UI แสดงตรงกับ API
  await expect(page.getByTestId('total-todos')).toHaveText(stats.todos.toString());
});
```

**คำถาม:**

1. **Diagnose:** อธิบาย root cause ว่าทำไม `request.get('/api/admin')` return 401 แม้ storageState set แล้ว — อธิบายความแตกต่างระหว่าง `page` context (browser cookies) และ `request` fixture context (isolated APIRequestContext)

2. **Fix with Code:** เขียน setup + test ที่แก้ปัญหา โดย:
   - `page` ยังใช้ storageState ได้ (สำหรับ UI)
   - `request` fixture ส่ง JWT token ที่ถูกต้องใน Authorization header
   - ไม่ต้อง login ซ้ำสำหรับทุก test (login ครั้งเดียวต่อ suite)

3. **DB Cross-verification:** เพิ่มใน test: verify ว่า user ที่ `GET /api/me` return ตรงกับ admin ที่ storageState เก็บไว้ — เขียน assertion ที่ confirm ทั้ง `username` และ `role` ตรงกัน

<details>
<summary>เฉลย</summary>

**1. Root Cause:**

`storageState` เก็บ browser cookies และ localStorage ของ `BrowserContext` — เมื่อ `page.goto()` browser จะส่ง cookies ไปกับทุก request อัตโนมัติ

แต่ `request` fixture เป็น **isolated `APIRequestContext`** ที่แยกต่างหากจาก BrowserContext — *(source: "Isolated [APIRequestContext] instance for each test.")* — ไม่ share cookies หรือ headers กับ browser session เลย

ดังนั้น ถึงแม้ storageState จะทำให้ browser authenticate แล้ว `request` fixture ก็ไม่รู้ว่ามี JWT token ที่ไหน → 401

**2. Fix:**

```typescript
// auth.setup.ts — เก็บ token ใน env หรือ file
import { test } from '@playwright/test';

test('authenticate admin', async ({ page, request }) => {
  // Login ผ่าน API เพื่อรับ token
  const loginRes = await request.post('http://localhost:3000/api/auth/login', {
    data: { username: 'admin', password: 'admin123' }
  });
  const { token } = await loginRes.json();

  // เก็บ token ใน file เพื่อ reuse
  require('fs').writeFileSync('playwright/.auth/admin-token.json', JSON.stringify({ token }));

  // Login ผ่าน UI เพื่อ storageState (สำหรับ page)
  await page.goto('http://localhost:3000/login');
  await page.getByTestId('input-username').fill('admin');
  await page.getByTestId('input-password').fill('admin123');
  await page.getByTestId('btn-login').click();
  await page.context().storageState({ path: 'playwright/.auth/admin.json' });
});

// fixtures.ts — fixture ที่โหลด token
import { test as base } from '@playwright/test';
import { readFileSync } from 'fs';

export const test = base.extend<{ adminToken: string }>({
  adminToken: async ({}, use) => {
    const { token } = JSON.parse(readFileSync('playwright/.auth/admin-token.json', 'utf-8'));
    await use(token);
  },
});

// admin.spec.ts
import { test } from './fixtures';
import { expect } from '@playwright/test';

test.use({ storageState: 'playwright/.auth/admin.json' });

test('admin stats match database', async ({ page, request, adminToken }) => {
  await page.goto('http://localhost:3000/admin');
  await expect(page.getByTestId('stats-panel')).toBeVisible();

  // request + JWT token
  const statsRes = await request.get('http://localhost:3000/api/admin', {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  expect(statsRes.status()).toBe(200);
  const { stats } = await statsRes.json();

  await expect(page.getByTestId('total-todos')).toHaveText(stats.todos.toString());
});
```

**3. DB Cross-verification:**

```typescript
test('logged-in user matches database record', async ({ request, adminToken }) => {
  // Verify via API ว่า token ตรงกับ admin user ใน DB
  const meRes = await request.get('http://localhost:3000/api/me', {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  expect(meRes.status()).toBe(200);
  const { username, role } = await meRes.json();

  expect(username).toBe('admin');
  expect(role).toBe('admin');

  // ถ้าต้องการ verify กับ DB โดยตรง:
  const { readFileSync } = require('fs');
  const db = JSON.parse(readFileSync(
    'docs/playwright-typescript/playwright-course-app/data/db.json', 'utf-8'
  ));
  const dbUser = db.users.find((u: any) => u.username === username);
  expect(dbUser?.role).toBe(role);
});
```

</details>

---

### Ch15 Expert: Hybrid API + DB Cross-verification

**Context:** ระบบ e-commerce ของคุณมี bug: `GET /api/admin` บอก `orders: 5` แต่ customer report บอกว่าสั่งไปแล้ว 6 ครั้ง ทีมสงสัยว่า `POST /api/orders` มี double-insert bug — บาง request insert 2 records แต่ response ดูปกติ

```typescript
test('order creation workflow', async ({ page, request }) => {
  const loginRes = await request.post('http://localhost:3000/api/auth/login', {
    data: { username: 'testuser', password: 'test123' }
  });
  const { token } = await loginRes.json();

  // สร้าง 3 orders ผ่าน API
  for (let i = 0; i < 3; i++) {
    const res = await request.post('http://localhost:3000/api/orders', {
      headers: { Authorization: `Bearer ${token}` },
      data: { items: [{ productId: i + 1, quantity: 1 }] }
    });
    expect(res.status()).toBe(201);
  }

  // Verify ผ่าน admin stats
  const adminLoginRes = await request.post('http://localhost:3000/api/auth/login', {
    data: { username: 'admin', password: 'admin123' }
  });
  const { token: adminToken } = await adminLoginRes.json();
  const statsRes = await request.get('http://localhost:3000/api/admin', {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  const { stats } = await statsRes.json();
  expect(stats.orders).toBe(3);  // ← pass ✓ แต่ bug ยังอยู่
});
```

**คำถาม:**

1. **Gap Analysis:** ระบุว่า test นี้ verify อะไรได้บ้างและไม่ verify อะไร — โดยเฉพาะ double-insert bug จะถูก catch โดย test นี้ได้ไหม? อธิบายว่าทำไม

2. **Design Better Test:** เขียน test ที่สามารถ detect double-insert bug โดย:
   - Verify จำนวน orders ที่แน่นอนใน DB (ใช้ direct file read เพราะไม่มี GET /api/orders)
   - Verify ว่า orderId ของแต่ละ order ไม่ซ้ำกัน
   - Verify items ของแต่ละ order ถูกต้อง

3. **Cleanup Challenge:** demo app ไม่มี DELETE /api/orders และ POST /api/reset ล้างแค่ todos — ออกแบบ cleanup strategy สำหรับ orders ที่สร้างในแต่ละ test

<details>
<summary>เฉลย</summary>

**1. Gap Analysis:**

Test ปัจจุบัน verify ได้:
- API returns 201 สำหรับทุก POST request ✓
- `stats.orders` เป็น 3 ✓

**ไม่ verify:**
- จำนวน records จริงใน DB (ถ้า double-insert → 6 records แต่ stats บอก 3 → ขัดกัน แต่ test ไม่ตรวจ)
- `orderId` ไม่ซ้ำกัน (UUID collision หรือ counter bug)
- items ของแต่ละ order ถูกต้องตามที่ส่งไป

**Double-insert bug:**
ถ้า server insert 2 records ต่อ 1 request แต่ stats aggregate ด้วย COUNT(DISTINCT orderId) หรือ logic อื่น — stats อาจยังบอก 3 แม้ DB มี 6 records → test นี้ **ไม่** catch bug นั้น

**2. Better Test:**

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';
import { readFileSync } from 'fs';
import { resolve } from 'path';

function readDb() {
  return JSON.parse(readFileSync(
    resolve('docs/playwright-typescript/playwright-course-app/data/db.json'),
    'utf-8'
  ));
}

test.beforeEach(async ({ request }) => {
  await request.post('http://localhost:3000/api/reset');
  // Note: orders ไม่มี reset — ต้อง track ด้วย snapshot
});

test('detects double-insert bug in order creation', async ({ request }) => {
  // บันทึก order count ก่อน
  const ordersBefore = readDb().orders.length;

  const loginRes = await request.post('http://localhost:3000/api/auth/login', {
    data: { username: 'testuser', password: 'test123' }
  });
  const { token } = await loginRes.json();

  const createdOrderIds: string[] = [];

  for (let i = 0; i < 3; i++) {
    const res = await request.post('http://localhost:3000/api/orders', {
      headers: { Authorization: `Bearer ${token}` },
      data: { items: [{ productId: i + 1, quantity: 1 }] }
    });
    expect(res.status()).toBe(201);
    const { orderId } = await res.json();
    createdOrderIds.push(orderId);
  }

  // Direct file read: verify exact count
  const afterDb = readDb();
  const newOrders = afterDb.orders.slice(ordersBefore);

  // ต้องมีแค่ 3 orders ใหม่ (ไม่ใช่ 6 จาก double-insert)
  expect(newOrders).toHaveLength(3);

  // Verify orderId ไม่ซ้ำกัน
  const uniqueIds = new Set(newOrders.map((o: any) => o.orderId));
  expect(uniqueIds.size).toBe(3);

  // Verify items ตรงกับที่ส่งไป
  for (let i = 0; i < 3; i++) {
    const order = newOrders.find((o: any) => o.orderId === createdOrderIds[i]);
    expect(order).toBeDefined();
    expect(order?.items).toEqual([{ productId: i + 1, quantity: 1 }]);
  }
});
```

**3. Cleanup Strategy:**

เนื่องจากไม่มี DELETE/reset สำหรับ orders มีทางเลือก:

**Option A: Snapshot-based** (ใช้ในตัวอย่างข้างต้น)
- บันทึก `ordersBefore` ก่อน test
- Assert บน `orders.slice(ordersBefore)` เฉพาะ orders ที่ test นี้สร้าง
- ไม่ต้องลบ orders เก่า — แค่ track ว่า index เริ่มต้นที่ไหน

**Option B: Unique marker**
- ใส่ timestamp หรือ test ID ใน items: `{ productId: 1, quantity: 1, testId: Date.now() }`
- Filter เฉพาะ orders ที่มี testId ของ test นี้

**Option C: Accept test pollution** (สำหรับ orders-as-append-only)
- ยอมรับว่า orders สะสมข้ามบน test
- ใช้ relative assertions ("เพิ่มขึ้น N") แทน absolute count
- Document behavior ใน test comment

</details>

---

### Ch17 Expert: CI/CD + DB State Management

**Context:** GitHub Actions pipeline รัน 300 tests ด้วย 4 shards parallel บน shared demo app instance เดียวกัน (`localhost:3000`) tests ที่ verify DB state fail ~30% บน CI

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
      - name: Start demo app
        run: cd docs/playwright-typescript/playwright-course-app && npm ci && npm start &
      - name: Wait for app
        run: npx wait-on http://localhost:3000
      - run: npx playwright test --shard=${{ matrix.shard }}/4
        env:
          CI: true
```

**คำถาม:**

1. **Root Cause:** อธิบายว่าทำไม 4 shards ที่ชี้ไป app instance เดียวกันถึงทำให้ DB tests fail — ยกตัวอย่าง scenario จริงที่ shard 1 และ shard 3 อาจ interfere กันใน DB เดียวกัน

2. **Solution Design:** เลือก approach ที่ดีที่สุดระหว่าง:
   - **A:** แต่ละ shard start demo app บน port ต่างกัน (3001, 3002, 3003, 3004)
   - **B:** ใช้ unique data prefix ต่อ shard (เหมือน Ch19 L5)
   - **C:** Sequential shards สำหรับ DB-sensitive tests + parallel สำหรับ tests อื่น
   
   เขียน complete GitHub Actions workflow yaml สำหรับ approach ที่เลือก

3. **Trade-off + Cleanup:** เปรียบเทียบ approach A vs B:
   - Setup complexity
   - CI cost (runner minutes)
   - Maintenance overhead
   
   และออกแบบ cleanup step ใน workflow ที่ทำงานเสมอ (even on failure) เพื่อ reset app state หลังแต่ละ shard

<details>
<summary>เฉลย</summary>

**1. Root Cause:**

4 shards ทำงานบน DB (`db.json`) เดียวกันผ่าน server เดียวกัน — ทำให้เกิด concurrent writes/reads ที่ไม่ synchronize:

Timeline scenario:
```
Shard 1: POST /api/reset     → db.todos = []
Shard 3: POST /api/todos (X) → db.todos = [X]   ← เขียนก่อน shard 1 เริ่ม test จริง
Shard 1: POST /api/todos (A) → db.todos = [X, A]  ← ปนกัน!
Shard 1: GET /api/todos      → ได้ [X, A] แต่ expect [A]
Shard 1: expect(todos).toHaveLength(1) → FAIL
```

บน local (single worker) ไม่มี concurrent requests → ไม่มี race condition → tests pass

**2. Approach A — Multiple App Instances:**

```yaml
name: Playwright Tests
on: [push]

jobs:
  test:
    strategy:
      fail-fast: false
      matrix:
        include:
          - shard: 1
            port: 3001
          - shard: 2
            port: 3002
          - shard: 3
            port: 3003
          - shard: 4
            port: 3004
    
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
      
      - run: npm ci
      
      - name: Install demo app dependencies
        run: cd docs/playwright-typescript/playwright-course-app && npm ci
      
      - name: Start demo app on port ${{ matrix.port }}
        run: PORT=${{ matrix.port }} cd docs/playwright-typescript/playwright-course-app && node server.js &
        # หมายเหตุ: server.js ต้องรองรับ PORT env variable
      
      - name: Wait for app on port ${{ matrix.port }}
        run: npx wait-on http://localhost:${{ matrix.port }}
      
      - name: Install Playwright browsers
        run: npx playwright install --with-deps chromium
      
      - name: Run tests (Shard ${{ matrix.shard }}/4)
        run: npx playwright test --shard=${{ matrix.shard }}/4 --reporter=blob
        env:
          CI: true
          BASE_URL: http://localhost:${{ matrix.port }}
      
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: blob-report-${{ matrix.shard }}
          path: blob-report/
          retention-days: 1
  
  merge-reports:
    if: always()
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: npm ci
      - uses: actions/download-artifact@v4
        with:
          path: all-blobs
          pattern: blob-report-*
          merge-multiple: true
      - run: npx playwright merge-reports --reporter html ./all-blobs
      - uses: actions/upload-artifact@v4
        with:
          name: html-report
          path: playwright-report/
          retention-days: 30
```

**3. Trade-off A vs B:**

| ประเด็น | A: Multiple Instances | B: Unique Prefix |
|---------|----------------------|-----------------|
| Setup complexity | ⚠️ Server ต้องรองรับ `PORT` env var + wait-on ต่อ port | ✅ แค่เพิ่ม fixture ใน test code |
| CI cost | ❌ npm ci + server startup ต่อ shard (~2 นาทีเพิ่ม) | ✅ ไม่มี overhead |
| Isolation | ✅ 100% isolated — DB แยกต่อ instance | ⚠️ Tests ต้อง filter data ด้วย prefix — global counts ยังปน |
| Maintenance | ⚠️ server.js ต้องไม่ hardcode port | ✅ ไม่ต้องแก้ infrastructure |
| เหมาะกับ | Tests ที่ต้องการ global state isolation | Tests ที่ filter data ได้และไม่ใช้ global counts |

**Cleanup step ที่รันเสมอ:**
```yaml
- name: Reset app state
  if: always()  # รันแม้ tests fail
  run: |
    curl -s -X POST http://localhost:${{ matrix.port }}/api/reset || true
    # || true เพื่อไม่ให้ fail ถ้า app ไม่ตอบสนอง
```

</details>

---

### Ch18 Expert: Production DB Verification Architecture

**Context:** บริษัทมี test suite ที่เติบโตจาก 50 เป็น 350 tests ใน 6 เดือน ทีม QA 3 คน CI รัน 4 workers ผลลัพธ์: tests fail บน CI ~20% โดยไม่มี pattern ชัดเจน

จากการวิเคราะห์เบื้องต้นพบว่า:
- 80 tests มี `beforeEach` reset
- 270 tests ไม่มี cleanup ใดๆ
- 40 tests verify todo count ใน admin stats
- 60 tests verify order records โดยตรง

ตัวอย่าง tests ที่ problematic:

```typescript
// todos.spec.ts — มี beforeEach reset
test.beforeEach(async ({ request }) => {
  await request.post('http://localhost:3000/api/reset');
});

test('todo count is correct', async ({ request }) => {
  await request.post('http://localhost:3000/api/todos', { data: { text: 'A' } });
  await request.post('http://localhost:3000/api/todos', { data: { text: 'B' } });
  
  const loginRes = await request.post('http://localhost:3000/api/auth/login', {
    data: { username: 'admin', password: 'admin123' }
  });
  const { token } = await loginRes.json();
  
  const stats = await (await request.get('http://localhost:3000/api/admin', {
    headers: { Authorization: `Bearer ${token}` }
  })).json();
  
  expect(stats.stats.todos).toBe(2);  // fail ~20%
});

// orders.spec.ts — ไม่มี cleanup
test('order creation works', async ({ request }) => {
  const loginRes = await request.post('http://localhost:3000/api/auth/login', {
    data: { username: 'testuser', password: 'test123' }
  });
  const { token } = await loginRes.json();
  
  const res = await request.post('http://localhost:3000/api/orders', {
    headers: { Authorization: `Bearer ${token}` },
    data: { items: [{ productId: 1, quantity: 1 }] }
  });
  expect(res.status()).toBe(201);
  // ไม่มี DB verification เลย
});
```

**คำถาม:**

1. **Full Root Cause Analysis:** Categorize ปัญหาทั้งหมดใน codebase นี้เป็น 3 หมวด:
   - Race conditions
   - State contamination  
   - Missing verification
   
   สำหรับแต่ละหมวดระบุ test ที่เป็นตัวอย่างและอธิบาย mechanism

2. **Production Fix Strategy:** ออกแบบ strategy ที่ fix ปัญหา โดย:
   - ทีม 3 คน implement ได้ใน 1 sprint (2 สัปดาห์)
   - ไม่ต้อง rewrite 350 tests
   - ลด flakiness อย่างน้อย 80%
   
   ระบุ: อะไรทำก่อน, อะไรทำหลัง, อะไรไม่ต้องทำ

3. **Shared Fixture Library:** เขียน `tests/db-fixtures.ts` ที่มี:
   - `cleanDb` fixture — reset ก่อน/หลัง test
   - `dbSnapshot` fixture — อ่าน DB state ปัจจุบัน
   - `verifyTodoCount` helper function — type-safe assertion สำหรับ todo count
   
   ทีมสามารถ import และใช้ได้ทันทีโดยไม่ต้อง modify test logic เดิม

<details>
<summary>เฉลย</summary>

**1. Root Cause Analysis:**

**Race Conditions:**
- `todos.spec.ts` — `beforeEach` reset ทำงาน แต่ workers อื่นกำลัง create todos ระหว่างที่ worker นี้กำลัง reset → หลัง reset อาจมี todos จาก worker อื่นอยู่แล้ว
- Timeline: W1 reset → W2 inserts → W1 creates 2 todos → W1 GET stats → count = 3 (ไม่ใช่ 2)

**State Contamination:**
- `orders.spec.ts` ไม่มี cleanup → orders สะสมข้าม test runs
- 270 tests ที่ไม่มี cleanup ทำให้ DB โตขึ้นเรื่อยๆ — tests ที่ verify count ต้องการ predictable state แต่ไม่มี
- 60 tests ที่ verify order records อาจเจอ orders เก่าจาก test อื่น

**Missing Verification:**
- `orders.spec.ts` ตรวจแค่ API response (201) ไม่ verify ว่า DB มี record จริง
- ไม่มีใคร catch double-insert หรือ silent fail ใน DB write

**2. Production Fix Strategy:**

**สัปดาห์ 1 (ลด flakiness ทันที):**
1. สร้าง `db-fixtures.ts` พร้อม `cleanDb` fixture
2. Import และใช้ `cleanDb` ใน 80 tests ที่มี `beforeEach` reset อยู่แล้ว (เปลี่ยนจาก manual reset เป็น fixture)
3. เพิ่ม `cleanDb` ให้ 40 tests ที่ verify todo count (เหล่านี้ fail บ่อยที่สุด)
4. ผลที่คาดหวัง: ลด flakiness จาก 20% เหลือ ~5%

**สัปดาห์ 2 (เพิ่ม DB verification):**
1. เพิ่ม DB verify ให้ 60 tests ที่ verify orders (ใช้ direct file read)
2. ทำ `verifyTodoCount` helper สำหรับ 40 stats tests
3. Document pattern ใน team wiki

**ไม่ต้องทำ:**
- Rewrite 270 tests ที่ไม่ใช่ count tests (ถ้าไม่ verify count ก็ไม่ affected)
- เพิ่ม cleanup ให้ทุก test (เน้นแค่ tests ที่ verify state-sensitive data)

**3. Shared Fixture Library:**

```typescript
// tests/db-fixtures.ts
// tested: Playwright v1.50+, Node.js 20+
import { test as base, expect } from '@playwright/test';
import { readFileSync } from 'fs';
import { resolve } from 'path';

// Types
interface DbState {
  todos: Array<{ id: number; text: string; completed: boolean; createdAt: string }>;
  orders: Array<{ orderId: string; status: string; items: any[]; createdAt: string }>;
  products: Array<{ id: number; name: string; price: number; category: string }>;
  users: Array<{ id: number; username: string; role: string }>;
}

// Helper
export function readDb(): DbState {
  return JSON.parse(readFileSync(
    resolve('docs/playwright-typescript/playwright-course-app/data/db.json'),
    'utf-8'
  ));
}

// Fixture type
type DbFixtures = {
  cleanDb: void;
  dbSnapshot: DbState;
};

// Extended test
export const test = base.extend<DbFixtures>({
  // cleanDb: reset before + guaranteed reset after
  cleanDb: async ({ request }, use) => {
    await request.post('http://localhost:3000/api/reset');
    await use();
    await request.post('http://localhost:3000/api/reset');
  },

  // dbSnapshot: อ่าน DB state ตอน test เริ่ม
  dbSnapshot: async ({}, use) => {
    await use(readDb());
  },
});

// Helper: type-safe todo count assertion
export async function verifyTodoCount(
  request: { get: (url: string) => Promise<{ json: () => Promise<any> }> },
  adminToken: string,
  expectedCount: number
): Promise<void> {
  const statsRes = await request.get('http://localhost:3000/api/admin');
  // Note: ต้องส่ง token แยก เพราะ helper ไม่รู้เรื่อง fixture context
  expect((await statsRes.json()).stats.todos).toBe(expectedCount);
}
```

**Usage ใน existing tests:**
```typescript
// todos.spec.ts — เปลี่ยนน้อยมาก
import { test } from './db-fixtures';  // เปลี่ยนแค่ import
import { expect } from '@playwright/test';

// ลบ beforeEach reset เดิม แล้วเพิ่ม cleanDb parameter
test('todo count is correct', async ({ request, cleanDb }) => {
  await request.post('http://localhost:3000/api/todos', { data: { text: 'A' } });
  await request.post('http://localhost:3000/api/todos', { data: { text: 'B' } });
  
  const { token } = await (await request.post('http://localhost:3000/api/auth/login', {
    data: { username: 'admin', password: 'admin123' }
  })).json();
  
  const { stats } = await (await request.get('http://localhost:3000/api/admin', {
    headers: { Authorization: `Bearer ${token}` }
  })).json();
  
  expect(stats.todos).toBe(2);  // ✅ reliable แล้ว
});
```

การ migrate แต่ละ test file ใช้เวลา 5-10 นาที — ทีม 3 คน รัน parallel migration ได้ ~30 files ต่อวัน

</details>

---

*แบบฝึกหัดหลัก: 18 บท × 3 ระดับ = 54 exercises*  
*Ch19: 5 levels (L1–L5) สำหรับ Database State Verification*  
*Expert Level (L5) เพิ่มเติม: Ch13, Ch15, Ch17, Ch18*
