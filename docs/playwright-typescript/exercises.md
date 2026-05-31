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
storageState เก็บ: cookies ทั้งหมด, localStorage, sessionStorage ของทุก origin ที่ browser เข้าถึง

storageState ไม่เก็บ: IndexedDB, Web Crypto keys, in-memory JavaScript state

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

Solution: Per-worker accounts — สร้าง test accounts หลาย accounts แต่ละ worker ใช้ account ของตัวเอง:

```typescript
// playwright.config.ts
export default defineConfig({
  workers: 4,
  projects: [{
    name: 'tests',
    use: {
      // ใช้ worker index เลือก storageState
      storageState: ({ workerIndex }) => 
        `playwright/.auth/user-${workerIndex}.json`,
    },
  }],
});

// auth.setup.ts — รันสำหรับแต่ละ worker
test('authenticate', async ({ page, workerIndex }) => {
  const email = `testuser-${workerIndex}@company.com`;
  await page.goto('/login');
  await page.fill('#email', email);
  await page.fill('#password', 'password123');
  await page.click('#submit');
  await page.context().storageState({
    path: `playwright/.auth/user-${workerIndex}.json`,
  });
});
```

ต้องสร้าง test accounts จริงใน test database สำหรับแต่ละ workerIndex ด้วย

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
<summary>เฉلى</summary>

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

*แบบฝึกหัดนี้ครอบคลุม 18 บท × 3 ระดับ = 54 exercises*
*สร้างโดยอิงเนื้อหาจากแต่ละบท — exercises ทุกข้อใช้สถานการณ์ใหม่ที่ไม่ซ้ำตัวอย่างในบท*
