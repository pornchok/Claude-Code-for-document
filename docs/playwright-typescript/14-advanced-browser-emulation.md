## ก่อนอ่านบทนี้ ลองตอบ:

1. คุณมี test suite ที่ต้องรัน 150 tests ซึ่ง 120 tests ต้อง login ก่อน — คุณจะใช้ storageState อย่างไรเพื่อไม่ให้ login ซ้ำทุก test? และถ้า parallel tests บางตัว modify ข้อมูลของ user เดียวกัน จะมีปัญหาอะไร?

2. เพื่อนคุณ save storageState แล้ว commit ไฟล์ `playwright/.auth/user.json` ขึ้น GitHub repository — มีความเสี่ยงอะไรบ้าง และควรแก้อย่างไร?

---

เฉลย:

1. สร้าง `auth.setup.ts` ที่ login ครั้งเดียวแล้ว save state — จากนั้น config ให้ test projects ใช้ `storageState` จากไฟล์นั้น ทุก test อ่าน state แทนการ login ใหม่ สำหรับ parallel tests ที่ modify ข้อมูล user เดียวกัน จะ conflict กันได้ ต้องใช้ per-worker auth (account แยกต่อ worker) เพื่อ isolation

2. ความเสี่ยง: ไฟล์ JSON เก็บ cookies และ tokens จริง ใครที่ clone repo ได้ก็ impersonate account นั้นได้ทันที — แก้โดยเพิ่ม `playwright/.auth` ใน `.gitignore` และแจ้ง team ทันที

---

# บทที่ 14: Advanced Browser Features & Emulation

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- จัดการ popup windows และ new tabs ด้วย `page.waitForEvent('popup')` อย่างถูกต้อง
- interact กับ content ภายใน iframe ผ่าน `frameLocator()` และ `locator.contentFrame()`
- handle dialogs (alert/confirm/prompt) โดย register handler ก่อน trigger action เสมอ
- upload และ download files ใน test โดยไม่ต้องเปิด OS dialog
- interact กับ Shadow DOM โดยที่ Playwright handle ให้อัตโนมัติ
- emulate mobile devices ด้วย `devices['iPhone 15 Pro']` และ configure locale/timezone/geolocation
- ใช้ `page.emulateMedia()` สำหรับ print stylesheet และ dark mode testing
- ใช้ `page.mouse` และ `page.keyboard` สำหรับ low-level interactions ที่ locator ธรรมดาทำไม่ได้

---

## 2. ทำไมต้องรู้? (Why)

web app จริงในชีวิตจริงไม่ได้มีแค่ "click button แล้ว verify text" — มันซับซ้อนกว่านั้นมาก:

- **Popup windows**: เปิด OAuth login, payment gateway, หรือ preview ในหน้าต่างใหม่
- **iFrames**: embed แผนที่ Google Maps, video player, หรือ third-party widget
- **Dialogs**: browser-native `alert()`, `confirm()`, `prompt()` ที่ JS code call โดยตรง
- **File operations**: upload รูปโปรไฟล์, download report PDF
- **Mobile testing**: ทดสอบว่า responsive design พังหรือเปล่าใน iPhone

ใน Robot Framework + Selenium การทำสิ่งเหล่านี้ต้องการ keyword เฉพาะ (`Select Window`, `Select Frame`), library เพิ่มเติม (`AlertKeywords`), หรือ JavaScript workaround สำหรับ Shadow DOM Playwright built-in ทุกอย่างนี้ไว้ในภาษาเดียว

บทนี้จะให้คุณ control browser ได้เต็ม spectrum — ตั้งแต่ popup จนถึง mobile emulation

---

## 3. Analogy

**Advanced browser features คือ special effects บนเวที performance** — actor หลักคือ `page` แต่บางฉากต้องใช้ technique พิเศษ:

- **Popup** = actor เปิดประตูไปห้องข้างๆ (new window) — คุณต้องวางคนไว้ฟังเสียงประตูเปิดก่อนที่ actor จะเดินไปเปิด ไม่งั้นคุณจะพลาดช่วงเปิดประตู
- **iframe** = actor อีกคนซ้อมอยู่บนเวทีที่ 2 ภายในเวทีหลัก — คุณต้องเดินเข้าไปในเวทีที่ 2 ก่อนจะสั่งให้ actor คนนั้นทำอะไร (แต่ Playwright ทำให้ง่ายกว่า Selenium มาก — ไม่ต้อง "switch context" แค่ระบุ `frameLocator`)
- **Dialog** = stage manager วิ่งเข้ามาขัดจังหวะการแสดง — คุณต้องบอก stage manager ล่วงหน้าว่าให้ตอบว่าอะไร ไม่งั้นเขาจะยืนค้างอยู่กลางเวทีไม่ยอมออก
- **Mobile emulation** = เปลี่ยนชุดฉากจาก desktop studio เป็น mobile studio — ขนาดเวที, camera angle, และ touch controls เปลี่ยนทั้งหมด แต่ script การทดสอบยังเหมือนเดิม

⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:

- **iframe content เข้าถึงได้เหมือน page ธรรมดาเสมอ** — จริงๆ cross-origin iframe (iframe จาก domain อื่น เช่น embed Google Maps ใน site ของคุณ) Playwright ไม่สามารถ inspect หรือ interact กับ DOM ภายในได้ เพราะ browser same-origin policy ป้องกันอยู่ — ทำได้เฉพาะ same-origin iframe เท่านั้น
- **`devices['iPhone 15 Pro']` emulate hardware จริง** — จริงๆ emulate แค่ viewport size, user-agent string, touch events, device pixel ratio — ไม่ใช่ CPU speed หรือ GPU ของ iPhone จริง benchmark performance จะยังเป็นค่าของเครื่อง host ที่รัน test
- **dialog handler ลงทะเบียนได้ตอนไหนก็ได้** — ต้อง register ก่อน action ที่ trigger dialog เสมอ เพราะ dialog เกิดขึ้นพร้อมกับ (หรือหลัง) action ทันที ถ้า register ช้าจะพลาด

---

## 4. เนื้อหาหลัก

### 4.1 Popup Windows และ New Tabs

**Popup** คือ window หรือ tab ใหม่ที่เปิดจาก JavaScript (`window.open()`) หรือ link ที่มี `target="_blank"`

Pattern สำคัญ: **ต้องเริ่ม listen ก่อน action ที่เปิด popup เสมอ** ถ้า listen หลัง action อาจพลาด popup ที่เปิดเร็วมาก

```typescript
// tested: Playwright v1.50+, Node.js 20+

// Pattern ที่ถูกต้อง — start listening before click
const popupPromise = page.waitForEvent('popup');
await page.getByText('open the popup').click();
const popup = await popupPromise;

// รอให้ popup load เสร็จก่อน interact
await popup.waitForLoadState();
await expect(popup).toHaveTitle(/Todo/);
await popup.close();
```

ถ้าต้องการ listen ทุก popup ที่เปิดตลอด test:

```typescript
// tested: Playwright v1.50+, Node.js 20+

// Continuous listener สำหรับทุก popup
page.on('popup', async popup => {
  await popup.waitForLoadState();
  console.log(await popup.title());
});
```

**สำหรับ Demo App** — popup ที่ `/advanced` เปิดจาก button `btn-open-popup`:

```typescript
// tested: Playwright v1.50+, Node.js 20+

test('popup opens and has correct title', async ({ page }) => {
  await page.goto('/advanced');

  const popupPromise = page.waitForEvent('popup');
  await page.click('[data-testid="btn-open-popup"]');
  const popup = await popupPromise;

  await popup.waitForLoadState();
  await expect(popup).toHaveTitle(/Todo/);
  await popup.close();
});
```

---

### 4.2 iFrames กับ frameLocator

**frameLocator** คือวิธีที่ Playwright แนะนำในการ interact กับ iframe — แทนที่จะต้อง "switch context" แบบ Selenium, `frameLocator()` return object ที่ scoped ภายใน iframe นั้นเลย

```typescript
// tested: Playwright v1.50+, Node.js 20+

// ใช้ attribute selector เพื่อ target iframe ที่ต้องการ
const frame = page.frameLocator('iframe[data-testid="embedded-iframe"]');

// ทุก locator ที่ได้จาก frameLocator จะ scope ภายใน iframe นั้น
await frame.getByLabel('Username').fill('John');
await frame.getByRole('button', { name: 'Submit' }).click();
```

**`locator.contentFrame()`** (เพิ่มใน v1.43) — ใช้เมื่อคุณมี `Locator` ของ iframe element อยู่แล้ว แล้วต้องการ interact กับ content ข้างใน:

```typescript
// tested: Playwright v1.50+, Node.js 20+

// มี locator ของ iframe element
const iframeLocator = page.locator('iframe[data-testid="embedded-iframe"]');

// แปลงจาก Locator → FrameLocator
const frameLocator = iframeLocator.contentFrame();

// ตอนนี้ interact กับ content ภายใน iframe ได้
await frameLocator.getByRole('button').click();
```

Operation ย้อนกลับ — จาก FrameLocator กลับเป็น Locator (element ของ iframe เอง):

```typescript
// tested: Playwright v1.50+, Node.js 20+

const frameLocator = page.frameLocator('iframe[name="embedded"]');
const iframeElement = frameLocator.owner(); // กลับเป็น Locator ของ <iframe> element
```

⚠️ **Cross-origin iframe**: ถ้า iframe load content จาก domain อื่น (เช่น `<iframe src="https://maps.google.com">`) Playwright ไม่สามารถ inspect หรือ click element ข้างในได้ เพราะ browser same-origin policy — ทำได้แค่กับ same-origin iframe

---

### 4.3 Dialogs (alert/confirm/prompt)

Browser-native dialogs คือ `window.alert()`, `window.confirm()`, `window.prompt()` — เป็น blocking UI ที่หยุด JavaScript execution จนกว่าจะ dismiss

**Rule #1**: ลงทะเบียน handler **ก่อน** action ที่ trigger dialog เสมอ

```typescript
// tested: Playwright v1.50+, Node.js 20+

// Alert — แค่ dismiss
page.once('dialog', async dialog => {
  console.log(dialog.message()); // ข้อความใน dialog
  await dialog.accept();
});
await page.click('[data-testid="btn-alert"]');

// Confirm — accept หรือ dismiss
page.once('dialog', async dialog => {
  expect(dialog.type()).toBe('confirm');
  await dialog.accept();    // คลิก OK
  // await dialog.dismiss(); // คลิก Cancel
});
await page.click('[data-testid="btn-confirm"]');

// Prompt — ส่งค่าไปด้วย
page.once('dialog', async dialog => {
  expect(dialog.type()).toBe('prompt');
  await dialog.accept('my answer'); // type แล้วกด OK
});
await page.click('[data-testid="btn-prompt"]');
```

ความแตกต่างระหว่าง `page.on()` และ `page.once()`:
- `page.on('dialog', handler)` — handle ทุก dialog ที่เกิดขึ้นตลอด lifecycle ของ page
- `page.once('dialog', handler)` — handle แค่ dialog ครั้งถัดไปครั้งเดียว (แนะนำสำหรับ test ที่รู้ว่า trigger ครั้งเดียว)

ถ้าไม่ register handler: Playwright จะ auto-dismiss dialogs ทั้งหมด — แต่ถ้า test รอผล dialog (เช่น confirm ส่งผล false → รอ delete ไม่เกิด) test จะพัง

---

### 4.4 File Upload

Playwright ใช้ `setInputFiles()` กับ `<input type="file">` โดยตรง — ไม่ต้องเปิด OS file picker

```typescript
// tested: Playwright v1.50+, Node.js 20+

// อัปโหลดไฟล์เดี่ยว
await page.locator('[data-testid="input-file"]').setInputFiles('path/to/file.txt');

// อัปโหลดหลายไฟล์พร้อมกัน
await page.locator('input[type="file"]').setInputFiles([
  'tests/fixtures/avatar.png',
  'tests/fixtures/resume.pdf',
]);

// Clear files ที่เลือกอยู่
await page.locator('input[type="file"]').setInputFiles([]);
```

สร้าง buffer โดยไม่ต้องมีไฟล์จริง:

```typescript
// tested: Playwright v1.50+, Node.js 20+

await page.locator('input[type="file"]').setInputFiles({
  name: 'test.txt',
  mimeType: 'text/plain',
  buffer: Buffer.from('hello world'),
});
```

---

### 4.5 File Download

Pattern เดียวกับ popup — **start listening before click**:

```typescript
// tested: Playwright v1.50+, Node.js 20+

const downloadPromise = page.waitForEvent('download');
await page.click('[data-testid="btn-download"]');
const download = await downloadPromise;

// ชื่อไฟล์ที่ browser แนะนำ (จาก Content-Disposition header)
const filename = download.suggestedFilename();
console.log(filename); // เช่น 'report-2024.pdf'

// path ชั่วคราวที่ Playwright save ไว้
const tempPath = await download.path();

// save ไปยัง path ที่ต้องการ
await download.saveAs(`/tmp/downloads/${filename}`);
```

---

### 4.6 Shadow DOM

Shadow DOM เป็น web component ที่มี DOM tree แยกออกมา — เข้าถึงผ่าน JavaScript ทั่วไปยาก แต่ **Playwright pierce through open shadow roots อัตโนมัติ** ไม่ต้อง workaround ใดๆ

```typescript
// tested: Playwright v1.50+, Node.js 20+

// เข้าถึง element ภายใน shadow root โดยตรง
// Playwright ค้นหาข้าม shadow boundary อัตโนมัติ
await page.locator('[data-testid="shadow-counter"]').locator('#inc').click();

// หรือใช้ getByRole/getByText ก็ได้ — Playwright pierce shadow root ให้
await page.getByRole('button', { name: 'Increment' }).click();
```

⚠️ closed shadow root (`attachShadow({ mode: 'closed' })`) Playwright ไม่สามารถ pierce ผ่านได้ — แต่ในทางปฏิบัติ app ที่ทดสอบได้จริงมักใช้ open shadow root เพราะ closed mode ไม่ได้ให้ security จริงๆ

---

### 4.7 Device Emulation

**`devices`** คือ registry ของ device configurations ที่ Playwright ship มาให้ — เมื่อ spread เข้า context จะ set viewport, userAgent, deviceScaleFactor, isMobile, hasTouch ให้อัตโนมัติ

```typescript
// tested: Playwright v1.50+, Node.js 20+
// playwright.config.ts

import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  projects: [
    {
      name: 'Desktop Chrome',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'Mobile iPhone 15 Pro',
      use: {
        ...devices['iPhone 15 Pro'], // exact device name ที่ Playwright รองรับ
        locale: 'th-TH',
        timezoneId: 'Asia/Bangkok',
      },
    },
  ],
});
```

device names ที่ Playwright รองรับสำหรับ iPhone 15:

| Device Name | หมายเหตุ |
|-------------|----------|
| `'iPhone 15'` | Portrait |
| `'iPhone 15 landscape'` | Landscape |
| `'iPhone 15 Plus'` | |
| `'iPhone 15 Pro'` | |
| `'iPhone 15 Pro Max'` | |

สร้าง context สำหรับ mobile ใน test โดยตรง:

```typescript
// tested: Playwright v1.50+, Node.js 20+

test('mobile geolocation', async ({ browser }) => {
  const mobileContext = await browser.newContext({
    ...devices['iPhone 15 Pro'],
    geolocation: { latitude: 13.7563, longitude: 100.5018 }, // กรุงเทพ
    permissions: ['geolocation'],
  });

  const page = await mobileContext.newPage();
  await page.goto('/location-based-feature');

  // ทดสอบ geolocation-based feature
  await expect(page.getByText('กรุงเทพมหานคร')).toBeVisible();

  await mobileContext.close();
});
```

---

### 4.8 `page.emulateMedia()`

ใช้เปลี่ยน media type หรือ color scheme ระหว่าง test — มีประโยชน์สำหรับ:

1. **Print stylesheet testing** — verify ว่า `@media print` CSS ทำงานถูกต้อง
2. **Dark mode testing** — verify ว่า `@media (prefers-color-scheme: dark)` แสดงสีถูกต้อง

```typescript
// tested: Playwright v1.50+, Node.js 20+

test('print stylesheet hides navigation', async ({ page }) => {
  await page.goto('/dashboard');

  // switch ไป print mode
  await page.emulateMedia({ media: 'print' });

  // navigation ควร hidden ใน print
  await expect(page.locator('nav')).not.toBeVisible();
});

test('dark mode shows correct colors', async ({ page }) => {
  await page.goto('/settings');

  // เปลี่ยน color scheme ระหว่าง test
  await page.emulateMedia({ colorScheme: 'dark' });

  // verify dark mode styles
  const body = page.locator('body');
  await expect(body).toHaveCSS('background-color', 'rgb(18, 18, 18)');
});
```

---

### 4.9 Low-Level Mouse และ Keyboard

ส่วนใหญ่ใช้ `locator.click()`, `locator.type()` ก็พอ แต่บางกรณีต้องการ low-level control:

**Mouse:**

```typescript
// tested: Playwright v1.50+, Node.js 20+

// drag จาก point A ไป point B
await page.mouse.move(100, 200);
await page.mouse.down();    // กด mouse button ค้างไว้
await page.mouse.move(300, 400);
await page.mouse.up();      // ปล่อย

// double click ที่พิกัดเฉพาะ
await page.mouse.dblclick(250, 300);
```

**Keyboard:**

```typescript
// tested: Playwright v1.50+, Node.js 20+

// keyboard shortcuts
await page.keyboard.press('Control+A');     // Select All
await page.keyboard.press('Control+C');     // Copy
await page.keyboard.press('Escape');

// type text
await page.keyboard.type('Hello World');

// hold key ค้างไว้ระหว่าง action
await page.keyboard.down('Shift');
await page.click('[data-testid="last-item"]'); // Shift+Click เพื่อ select range
await page.keyboard.up('Shift');
```

⚠️ สำหรับ drag-and-drop ทั่วไป แนะนำใช้ `locator.dragTo()` แทน `page.mouse` เพราะ readable กว่าและ reliable กว่า:

```typescript
await page.locator('#source').dragTo(page.locator('#target'));
```

---

### 4.10 RF/Selenium vs Playwright: เปรียบเทียบ

| Feature | Robot Framework + Selenium | Playwright |
|---------|---------------------------|------------|
| Popup / New Tab | `Select Window    title=...` keyword | `page.waitForEvent('popup')` |
| iframe | `Select Frame    id=myframe` + switch context | `frameLocator('iframe[name=...]')` — ไม่ต้อง switch |
| Dialog (alert) | `AlertKeywords` library | `page.on('dialog', ...)` built-in |
| Mobile emulation | `DesiredCapabilities` + `ChromeOptions` | `devices['iPhone 15 Pro']` built-in |
| Shadow DOM | XPath `>>` หรือ JS `shadowRoot.querySelector()` | Automatic — pierce open shadow root |
| File upload | `Choose File` keyword | `setInputFiles()` |
| File download | `Wait For Download` keyword หรือ custom | `page.waitForEvent('download')` |
| Print media | ไม่มี built-in | `page.emulateMedia({ media: 'print' })` |

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner — Popup + Dialog จาก `/advanced`

```typescript
// tested: Playwright v1.50+, Node.js 20+
// tests/beginner-popup-dialog.spec.ts

import { test, expect } from '@playwright/test';

test.describe('Popup and Dialog basics', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/advanced');
  });

  test('popup opens and has correct title', async ({ page }) => {
    // เริ่ม listen ก่อน trigger
    const popupPromise = page.waitForEvent('popup');
    await page.click('[data-testid="btn-open-popup"]');
    const popup = await popupPromise;

    await popup.waitForLoadState();
    await expect(popup).toHaveTitle(/Todo/);

    // popup ควรมี content
    await expect(popup.getByRole('heading')).toBeVisible();
    await popup.close();
  });

  test('alert dialog is handled', async ({ page }) => {
    // register handler ก่อน trigger
    let alertMessage = '';
    page.once('dialog', async dialog => {
      alertMessage = dialog.message();
      await dialog.accept();
    });

    await page.click('[data-testid="btn-alert"]');

    // หลัง dialog ปิด page ควร continue ได้
    await expect(page.locator('[data-testid="btn-alert"]')).toBeVisible();
    console.log('Alert message:', alertMessage);
  });

  test('confirm dialog — accept shows success', async ({ page }) => {
    page.once('dialog', async dialog => {
      expect(dialog.type()).toBe('confirm');
      await dialog.accept();
    });

    await page.click('[data-testid="btn-confirm"]');
    // หลัง accept ควรมี feedback บน page
    await expect(page.locator('[data-testid="confirm-result"]')).toHaveText(/confirmed/i);
  });

  test('prompt dialog — submit answer', async ({ page }) => {
    page.once('dialog', async dialog => {
      expect(dialog.type()).toBe('prompt');
      await dialog.accept('Playwright Test Answer');
    });

    await page.click('[data-testid="btn-prompt"]');
    await expect(page.locator('[data-testid="prompt-result"]')).toHaveText('Playwright Test Answer');
  });
});
```

---

### Intermediate — Mobile Emulation สำหรับ Responsive Design

สถานการณ์: ทีมพัฒนา e-commerce site แจ้งว่า checkout button หายไปใน mobile view หลัง deploy ล่าสุด — เขียน test ยืนยัน:

```typescript
// tested: Playwright v1.50+, Node.js 20+
// tests/mobile-responsive.spec.ts

import { test, expect, devices } from '@playwright/test';

// ทดสอบใน multiple viewports
const mobileDevices = [
  { name: 'iPhone 15 Pro', device: devices['iPhone 15 Pro'] },
  { name: 'iPhone 15 Pro Max', device: devices['iPhone 15 Pro Max'] },
];

for (const { name, device } of mobileDevices) {
  test.describe(`Responsive checkout — ${name}`, () => {
    test.use({ ...device });

    test('checkout button is visible and tappable', async ({ page }) => {
      await page.goto('/products/laptop-pro');

      // mobile viewport ควรเห็น add-to-cart button
      const addToCart = page.getByRole('button', { name: /add to cart/i });
      await expect(addToCart).toBeVisible();

      // ต้องอยู่ใน viewport จริง (ไม่ใช่แค่ exist ใน DOM)
      await expect(addToCart).toBeInViewport();
      await addToCart.tap(); // ใช้ tap() สำหรับ touch device

      // cart badge อัปเดต
      await expect(page.locator('[data-testid="cart-count"]')).toHaveText('1');
    });

    test('navigation hamburger menu works on mobile', async ({ page }) => {
      await page.goto('/');

      // desktop nav ควร hidden
      await expect(page.locator('nav.desktop-nav')).not.toBeVisible();

      // hamburger ควรเห็น
      const hamburger = page.getByRole('button', { name: /menu/i });
      await expect(hamburger).toBeVisible();
      await hamburger.tap();

      // mobile menu เปิด
      await expect(page.locator('nav.mobile-nav')).toBeVisible();
    });

    test('product grid shows correct columns in portrait vs landscape', async ({ browser }) => {
      // --- PORTRAIT MODE ---
      const portraitCtx = await browser.newContext({
        viewport: { width: 390, height: 844 },
      });
      const portraitPage = await portraitCtx.newPage();
      await portraitPage.goto('/shop');

      // portrait ควรแสดง 1 column layout (stacked)
      const portraitCards = portraitPage.locator('[data-testid="product-card"]');
      const portraitFirstCard = portraitCards.nth(0);
      const portraitSecondCard = portraitCards.nth(1);

      const portraitFirstBox = await portraitFirstCard.boundingBox();
      const portraitSecondBox = await portraitSecondCard.boundingBox();

      // ส่วน Y ต่างกัน (stacked vertically)
      expect(portraitSecondBox!.y).toBeGreaterThan(portraitFirstBox!.y + portraitFirstBox!.height);
      // ส่วน X เหมือนกัน (same column)
      expect(portraitSecondBox!.x).toBeCloseTo(portraitFirstBox!.x, 10);

      await portraitCtx.close();

      // --- LANDSCAPE MODE ---
      const landscapeCtx = await browser.newContext({
        viewport: { width: 844, height: 390 },
      });
      const landscapePage = await landscapeCtx.newPage();
      await landscapePage.goto('/shop');

      const landscapeCards = landscapePage.locator('[data-testid="product-card"]');
      const landscapeFirstCard = landscapeCards.nth(0);
      const landscapeSecondCard = landscapeCards.nth(1);

      const landscapeFirstBox = await landscapeFirstCard.boundingBox();
      const landscapeSecondBox = await landscapeSecondCard.boundingBox();

      // landscape ควรแสดง 2 columns (side-by-side)
      // ส่วน Y เหมือนกัน (same row)
      expect(landscapeSecondBox!.y).toBeCloseTo(landscapeFirstBox!.y, 10);
      // ส่วน X ต่างกัน (different columns)
      expect(landscapeSecondBox!.x).toBeGreaterThan(landscapeFirstBox!.x + landscapeFirstBox!.width);

      await landscapeCtx.close();
    });
  });
}
```

---

### Advanced — Synthesis: iframe + Shadow DOM + Download ใน Flow เดียว

สถานการณ์: ระบบ HR portal ที่มี document viewer (iframe) แสดงเอกสาร, shadow DOM widget สำหรับ digital signature, และ download button ที่ generate PDF หลังเซ็น — เขียน end-to-end test:

```typescript
// tested: Playwright v1.50+, Node.js 20+
// tests/hr-document-signing.spec.ts

import { test, expect } from '@playwright/test';
import * as path from 'path';
import * as fs from 'fs';

test('HR document signing flow — iframe + shadow DOM + download', async ({ page }) => {
  await page.goto('/hr/documents/offer-letter-2024');

  // --- STEP 1: ตรวจสอบ document content ใน iframe ---
  const docViewer = page.frameLocator('iframe[data-testid="document-viewer"]');

  // verify document loaded correctly
  await expect(docViewer.getByRole('heading', { name: /Offer Letter/i })).toBeVisible();

  const candidateName = await docViewer.locator('[data-field="candidate-name"]').textContent();
  expect(candidateName).toMatch(/John Doe/);

  // scroll ไปท้าย document ใน iframe ก่อน sign
  await docViewer.locator('[data-testid="document-end"]').scrollIntoViewIfNeeded();

  // --- STEP 2: Digital Signature Widget (Shadow DOM) ---
  // shadow-signature-pad คือ web component ที่มี shadow root
  const signatureWidget = page.locator('shadow-signature-pad');

  // Playwright auto-pierces open shadow root
  const clearBtn = signatureWidget.locator('[data-action="clear"]');
  const canvas = signatureWidget.locator('canvas.signature-canvas');

  // clear ก่อนวาด
  await clearBtn.click();

  // วาด signature ด้วย mouse (simulate drag)
  const canvasBound = await canvas.boundingBox();
  if (!canvasBound) throw new Error('Signature canvas not found');

  const startX = canvasBound.x + 50;
  const startY = canvasBound.y + canvasBound.height / 2;

  await page.mouse.move(startX, startY);
  await page.mouse.down();
  await page.mouse.move(startX + 100, startY - 20);
  await page.mouse.move(startX + 200, startY + 10);
  await page.mouse.up();

  // verify signature มีข้อมูล (canvas ไม่ว่าง)
  const signatureData = await signatureWidget.locator('[data-testid="signature-data"]').inputValue();
  expect(signatureData.length).toBeGreaterThan(0);

  // --- STEP 3: Submit + Download PDF ---
  // dialog confirm ก่อน submit
  page.once('dialog', async dialog => {
    expect(dialog.type()).toBe('confirm');
    expect(dialog.message()).toMatch(/ยืนยันการลงนาม/);
    await dialog.accept();
  });

  // start listening for download ก่อน click submit
  const downloadPromise = page.waitForEvent('download');
  await page.getByRole('button', { name: /ลงนามและดาวน์โหลด/i }).click();
  const download = await downloadPromise;

  // verify download
  const filename = download.suggestedFilename();
  expect(filename).toMatch(/offer-letter.*\.pdf$/i);

  // save และตรวจสอบไฟล์
  const savePath = path.join('test-results', 'downloads', filename);
  // ✅ สร้าง directory ก่อนบันทึกไฟล์
  fs.mkdirSync(path.dirname(savePath), { recursive: true });
  await download.saveAs(savePath);
  expect(fs.existsSync(savePath)).toBe(true);

  const fileSize = fs.statSync(savePath).size;
  expect(fileSize).toBeGreaterThan(1000); // PDF ต้องมีเนื้อหาจริง

  // --- STEP 4: Verify success state ---
  await expect(page.getByText(/เอกสารลงนามสำเร็จ/)).toBeVisible();
  await expect(page.locator('[data-testid="signature-status"]')).toHaveText('Signed');
});
```

---

## 6. Common Mistakes

❌ **ไม่ register dialog handler ก่อน trigger action ที่เปิด dialog**

```typescript
// ผิด — handler ลงทะเบียนหลัง click, อาจพลาด dialog
await page.click('[data-testid="btn-confirm"]');
page.once('dialog', async dialog => await dialog.accept()); // สาย
```

✅ **Register handler ก่อน action เสมอ**

```typescript
// ถูก — handler พร้อมรับก่อน action trigger
page.once('dialog', async dialog => await dialog.accept());
await page.click('[data-testid="btn-confirm"]');
```

หากไม่ handle: test อาจ hang หรือ Playwright auto-dismiss แล้ว test ล้มเหลวจาก state ที่ไม่ถูกต้อง *(source: https://playwright.dev/docs/dialogs)*

---

❌ **ใช้ `page.frames()` loop ด้วย index เพื่อหา iframe**

```typescript
// ผิด — brittle มาก, ถ้า iframe เพิ่ม/ลดจะ index เปลี่ยน
const frame = page.frames()[1];
await frame.fill('#username', 'John');
```

✅ **ใช้ `frameLocator()` ด้วย attribute selector**

```typescript
// ถูก — stable ใช้ attribute ที่ meaningful
const frame = page.frameLocator('iframe[data-testid="login-frame"]');
await frame.getByLabel('Username').fill('John');
```

การใช้ index แบบ hardcode พังทันทีที่ order ของ iframe ใน page เปลี่ยน *(source: https://playwright.dev/docs/frames)*

---

❌ **ใช้ `page.mouse.click()` พิกัด hardcode แทน locator**

```typescript
// ผิด — พิกัด hardcode พังเมื่อ layout เปลี่ยนหรือ viewport ต่างกัน
await page.mouse.click(234, 567);
```

✅ **ใช้ `locator.click()` เป็น default, ใช้ `page.mouse` เฉพาะเมื่อจำเป็น**

```typescript
// ถูก — stable ตาม semantic หรือ test id
await page.getByRole('button', { name: 'Submit' }).click();

// ถ้าต้องการ mouse interaction จริง (drag, hover precision) ใช้ boundingBox()
const box = await page.locator('#target').boundingBox();
await page.mouse.move(box!.x + box!.width / 2, box!.y + box!.height / 2);
```

*(source: Playwright best practices — https://playwright.dev/docs/best-practices)*

---

❌ **คาดหวังว่า `devices['...']` emulate performance จริงของ hardware**

```typescript
// เข้าใจผิด — ใช้ measure performance แล้วคาดว่าจะเป็น iPhone จริง
const startTime = Date.now();
await page.goto('/heavy-page');
const loadTime = Date.now() - startTime;
expect(loadTime).toBeLessThan(1000); // อาจ pass บน desktop แต่ fail บน iPhone จริง
```

✅ **`devices[...]` emulate viewport, userAgent, touch เท่านั้น — ไม่ใช่ CPU/GPU**

ใช้สำหรับ: layout testing, CSS breakpoint, touch interaction, user-agent sniffing — ไม่ใช่ performance benchmark *(source: https://playwright.dev/docs/emulation)*

---

❌ **interact กับ cross-origin iframe content**

```typescript
// ผิด — จะ throw error หรือ timeout สำหรับ cross-origin iframe
const frame = page.frameLocator('iframe[src="https://maps.google.com"]');
await frame.getByRole('button').click(); // Error: cross-origin frame
```

✅ **รับรู้ข้อจำกัด — cross-origin iframe ไม่สามารถ inspect ได้**

ถ้าต้องการทดสอบ integration กับ third-party widget ใน cross-origin iframe ให้ mock API response แทน หรือ test widget แยกใน environment ที่ควบคุม origin ได้ *(source: https://playwright.dev/docs/frames)*

---

## 7. สรุปบท

ก่อนดูเฉลย ลองตอบ 3 คำถามนี้ด้วยตัวเองก่อน:

**คำถาม 1**: คุณต้องการทดสอบว่าหลังจาก click ปุ่ม "Delete" จะมี confirm dialog ขึ้นมา ถ้า user กด OK จะลบสำเร็จ ถ้ากด Cancel จะไม่ลบ — คุณจะเขียน 2 test cases นี้อย่างไร? อะไรคือสิ่งที่ต้องทำก่อน `await page.click('[data-testid="btn-delete"]')` เสมอ?

**คำถาม 2**: `frameLocator()` ต่างจาก `page.frame()` อย่างไร? เมื่อไหรควรใช้ `locator.contentFrame()` แทน `page.frameLocator()`?

**คำถาม 3**: สถาปนิกในทีมบอกให้คุณเขียน test ที่รัน automated ทุกคืนเพื่อตรวจสอบว่า print stylesheet ซ่อน navigation bar และ sidebar อย่างถูกต้อง — คุณจะ set up อย่างไร?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย:**

**คำถาม 1**: ต้อง `page.once('dialog', handler)` ก่อน click เสมอ สำหรับ 2 cases:
- Case "กด OK": `page.once('dialog', async d => await d.accept())` ก่อน click, แล้ว assert ว่า item ถูกลบ
- Case "กด Cancel": `page.once('dialog', async d => await d.dismiss())` ก่อน click, แล้ว assert ว่า item ยังอยู่
ถ้าไม่ register handler ก่อน Playwright อาจ auto-dismiss แล้ว test ไม่ได้ทดสอบ behavior จริง

**คำถาม 2**: `frameLocator()` return FrameLocator ที่ scope ภายใน iframe — ทุก locator ที่ chain ต่อจะหาใน iframe นั้น ไม่ต้อง "switch context" — `page.frame()` return Frame object แบบ Selenium-style ที่ older API ส่วน `locator.contentFrame()` ใช้เมื่อคุณมี Locator ของ iframe element อยู่แล้ว (เพิ่มใน v1.43) แต่ต้องการ interact กับ content ข้างใน — ทั้งสองให้ผลเหมือนกัน แค่ starting point ต่างกัน

**คำถาม 3**: สร้าง test ที่เรียก `await page.emulateMedia({ media: 'print' })` ก่อน assert, ตรวจสอบด้วย `expect(locator).not.toBeVisible()` สำหรับ nav และ sidebar — ใส่ใน project แยกหรือ tag ว่า `@print` แล้วรันใน CI cron job ทุกคืน

</details>

---

## 8. Pre-chapter Retrieval สำหรับบทถัดไป

บทที่ 15 จะพูดถึง CI/CD Integration และ Reporting — ก่อนอ่าน ลองนึกดูว่า:

- Playwright test suite ที่ดีควร integrate กับ CI pipeline อย่างไร? sharding ช่วยอะไรใน CI?
- `playwright.config.ts` มี option อะไรบ้างที่ควร set แตกต่างกันระหว่าง local และ CI environment?
