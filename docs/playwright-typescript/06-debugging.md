## ก่อนอ่านบทนี้ ลองตอบ:

1. Web-first assertion กับ `isVisible()` ต่างกันอย่างไรในแง่ retry behavior — และทำไมความแตกต่างนั้นถึงสำคัญมากใน async web app?
2. เมื่อไหรควรใช้ `expect.soft()` แทน `expect()` ธรรมดา — และมีสถานการณ์ใดบ้างที่ไม่ควรใช้ soft assertions?

---

เฉลย:

1. `isVisible()` คือ snapshot ณ วินาทีที่ call — ถ้า element ยังไม่ปรากฏใน millisecond นั้นได้ `false` ทันทีโดยไม่ retry เลย ส่วน web-first assertion เช่น `expect(locator).toBeVisible()` จะ retry การตรวจซ้ำจนครบ timeout default (5 วินาที) ความสำคัญคือ browser ทำงาน async เสมอ — element โผล่หลัง API call, animation เสร็จ, หรือ state update — ถ้าไม่ retry test จะ flaky อย่างไม่มีสาเหตุที่ชัดเจน
2. ควรใช้ `expect.soft()` เมื่อต้องการ collect error ทุกข้อในรอบเดียว เช่น form validation ที่ต้องการรู้ว่าทุก field แสดง error ถูกไหม — ไม่ควรใช้เมื่อ assertion ถัดไป depend on assertion ก่อนหน้า เพราะถ้า assertion แรก fail แต่ test รันต่อ assertion ถัดไปอาจ confuse มากกว่าได้ประโยชน์ เช่น assert ว่า "modal เปิดแล้ว" fail แต่ยัง assert "ปุ่มใน modal กดได้" ต่อ

# บทที่ 6: Debugging — Inspector, Trace Viewer, และเครื่องมือช่วย debug

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- เปิด Playwright Inspector ด้วย `--debug` flag เพื่อ step-through test ทีละ action ได้
- ใส่ `page.pause()` ในจุดที่ต้องการหยุดดู และเข้าใจว่าควรใช้เมื่อไหรและเมื่อไหรไม่ควร
- ตั้งค่า Trace Viewer ใน config และอ่าน trace.zip หลัง test รันเพื่อวิเคราะห์ failure ที่ reproduce ไม่ได้ใน local
- เลือก debug tool ที่เหมาะสมตามสถานการณ์ (local dev / CI failure / performance diagnosis)
- ใช้ Codegen บันทึก action แล้ว refactor code ที่ได้ให้ใช้งานได้จริง
- รัน UI Mode เพื่อ debug หลาย tests แบบ interactive พร้อม watch mode
- เปรียบเทียบ debug workflow ของ Playwright กับ Robot Framework + Selenium ได้ชัดเจน

---

## 2. ทำไมต้องรู้? (Why)

ลองนึกภาพ test นี้:

```typescript
test('checkout flow', async ({ page }) => {
  await page.goto('http://localhost:3000/cart');
  await page.getByRole('button', { name: 'Proceed to Checkout' }).click();
  await page.getByLabel('Card Number').fill('4111111111111111');
  await page.getByRole('button', { name: 'Place Order' }).click();
  await expect(page.getByTestId('order-confirmation')).toBeVisible();
});
```

test นี้รันผ่านใน local ทุกครั้ง แต่พังใน CI ทุกรอบ error message บอกว่า `order-confirmation` ไม่ปรากฏ

คุณจะทำอะไร? ถ้าเป็น Robot Framework + Selenium คุณคงต้องเพิ่ม `Log` keyword ทุกจุด, download screenshot จาก CI, แล้วไล่ดูว่าหยุดตรงไหน ใช้เวลานานและยังไม่แน่ว่าจะเจอปัญหา

Playwright มีแนวทางที่ต่างออกไปสิ้นเชิง: Trace Viewer บันทึกทุกอย่างที่เกิดขึ้นระหว่าง test — screenshot ทุก step, network request/response, console log, DOM snapshot — ทั้งหมดเป็น timeline ที่ scrub ได้ ไม่ต้องเดาแล้วเพิ่ม log ใหม่แล้วรันใหม่อีกรอบ

บทนี้คือ toolkit ที่คุณต้องใช้ทุกวันในงาน testing จริง

---

## 3. Analogy: Black Box Recorder และ Live Control Tower

นึกถึงระบบสองอย่างในการบิน

**Black Box Recorder (Trace Viewer):** เครื่องบินบันทึกทุกอย่างตลอดเวลา — ความเร็ว, ระดับความสูง, คำสั่ง control, เสียงในห้องนักบิน — เมื่อเกิดเหตุ นักสืบสามารถย้อนกลับไปดู "ช่วง 3 นาทีก่อนเกิดเหตุ" ได้ทุกมิติพร้อมกัน เปรียบเหมือน Trace Viewer ที่เก็บ screenshot, network, DOM, console ไว้ใน trace.zip และให้คุณย้อนดูเหตุการณ์ก่อน failure ได้

**Live Control Tower (Playwright Inspector):** ขณะที่เครื่องบินกำลังบิน นักบินคุย live กับ tower ที่เห็นภาพรวมทั้งหมดและสั่ง step ต่อไปได้ เปรียบเหมือน Inspector ที่เปิด browser ตรงหน้า แสดง action ที่กำลังจะทำ และให้คุณกด "Next" เองทีละ step

---

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**

- **ต้องใช้ Inspector (PWDEBUG=1 / --debug) ตลอดเวลา** — Inspector pause และ slow down test ทุก step มีไว้สำหรับ debug session เท่านั้น ถ้า run ใน CI test จะ timeout เพราะรอ interaction ที่ไม่มีใครกด
- **Trace Viewer เปิดดูได้ทันทีหลัง test รัน** — ต้องเปิด trace collection ใน config ก่อนเสมอ (`trace: 'on-first-retry'` หรือ `trace: 'on'`) ถ้าไม่ตั้งค่า trace.zip จะไม่ถูกสร้างเลย ไม่ว่า test จะพังแค่ไหน
- **Black box บันทึกเฉพาะตอนเกิดเหตุ** — ในความเป็นจริง black box บันทึกตลอดเวลา และ Trace Viewer ก็เช่นกัน `trace: 'on'` บันทึกทุก test ทุกรอบ (แต่ช้า) ส่วน `trace: 'on-first-retry'` บันทึกเฉพาะรอบ retry แรก ซึ่ง balance ระหว่าง coverage และ performance ได้ดีที่สุดสำหรับ CI

---

## 4. เนื้อหาหลัก

### 4.1 Playwright Inspector — Step-Through Debugging

Inspector คือ GUI tool ที่เปิด browser ให้เห็นและ highlight element ที่ test กำลัง interact ด้วย พร้อม log ของ action แต่ละขั้น

**วิธีเปิด:**

```bash
# debug ทั้ง test file
npx playwright test --debug

# debug เฉพาะ test ใน file นั้น บรรทัดที่ 10
npx playwright test example.spec.ts:10 --debug

# debug เฉพาะ project chromium
npx playwright test --project=chromium --debug
```

*(source: https://playwright.dev/docs/debug)*

เมื่อเปิด Inspector คุณจะเห็น:
- Browser window ที่ highlight element ที่กำลัง interact
- Inspector panel ด้านขวาที่แสดง action ถัดไปและ locator ที่ใช้
- ActionLog ที่บอกว่า Playwright รอ actionability check อะไรอยู่
- ปุ่ม "Step over" เพื่อทำ action ถัดไปทีละขั้น
- "Pick locator" เพื่อคลิก element บน browser แล้ว Inspector สร้าง locator ให้

---

### 4.2 page.pause() — Breakpoint ใน Code

แทนที่จะใช้ `--debug` ซึ่งเริ่มจาก step แรก คุณสามารถใส่ `page.pause()` ในจุดที่ต้องการหยุดโดยตรง

```typescript
// partial example — see Section 5 for runnable version
test('debug ณ จุดสำคัญ', async ({ page }) => {
  await page.goto('http://localhost:3000/checkout');
  await page.getByLabel('Card Number').fill('4111111111111111');
  await page.pause(); // ← หยุดตรงนี้ เปิด Inspector ให้ดู state ณ จุดนี้
  await page.getByRole('button', { name: 'Place Order' }).click();
});
```

เหมาะเมื่อ: รู้แน่แล้วว่า bug อยู่แถวบรรทัดใด ไม่ต้องการ step through ตั้งแต่ต้น

**ข้อควรระวัง:** อย่าลืม `page.pause()` ทิ้งไว้ใน code ก่อน commit — test จะ hang ใน CI เพราะรอ interaction

---

### 4.3 Trace Viewer — Timeline Forensics

Trace Viewer คือเครื่องมือหลักสำหรับ debug CI failure เพราะ test ในสภาพแวดล้อม CI มักจะ reproduce ไม่ได้ใน local

**ขั้นที่ 1: เปิด trace collection ใน config**

```typescript
// partial example — see Section 5 for runnable version
// playwright.config.ts
import { defineConfig } from '@playwright/test';

export default defineConfig({
  retries: 1,
  use: {
    trace: 'on-first-retry',  // บันทึกเฉพาะรอบ retry แรก
  },
});
```

Options ที่มี:

| Option | พฤติกรรม | เมื่อใช้ |
|--------|----------|---------|
| `'on-first-retry'` | บันทึกรอบ retry แรกเท่านั้น | แนะนำสำหรับ CI |
| `'on-all-retries'` | บันทึกทุกรอบ retry | ต้องการ compare หลาย retry |
| `'on'` | บันทึกทุก test ทุกรอบ | debug intensive — ช้า |
| `'retain-on-failure'` | บันทึกทุกอย่าง ลบเฉพาะที่ผ่าน | ต้องการ trace ของ failed tests เท่านั้น |
| `'off'` | ปิด | production / ไม่ต้องการ trace |

*(source: https://playwright.dev/docs/trace-viewer)*

**ขั้นที่ 2: เปิดดู trace หลัง test รัน**

```bash
npx playwright show-trace test-results/[test-name]/trace.zip
```

หรือลาก trace.zip ไปที่ [trace.playwright.dev](https://trace.playwright.dev) โดยตรง

**สิ่งที่ Trace Viewer แสดง:**

- **Timeline**: film strip ของ screenshot ทุก action — hover เพื่อดู preview
- **Actions tab**: locator ที่ใช้ใน action แต่ละ step พร้อมเวลาที่ใช้ *(source: https://playwright.dev/docs/trace-viewer — "what locator was used for every action and how long each one took")*
- **DOM Snapshots**: ภาพ DOM ก่อน/ระหว่าง/หลัง action แต่ละขั้น *(source: https://playwright.dev/docs/trace-viewer — "the exact click position")*
- **Network tab**: request/response ทุกอัน filter ได้ตาม type, status, method, duration
- **Console tab**: log จาก browser และ test file
- **Error markers**: จุดสีแดงบน timeline บอกว่า failure เกิดตรงไหน

---

### 4.4 Headed Mode — ดู Browser ทำงาน

วิธีง่ายที่สุดสำหรับ local debug คือดู browser จริงๆ ขณะ test รัน

```bash
# รัน test แบบ headed (เห็น browser window)
npx playwright test --headed

# รัน headed + slow motion 1 วินาที/action
npx playwright test --headed --slow-mo=1000
```

หรือตั้งใน config:

```typescript
// partial example — see Section 5 for runnable version
// playwright.config.ts
export default defineConfig({
  use: {
    headless: false,
    slowMo: 500,  // milliseconds ต่อ action
  },
});
```

*(source: https://playwright.dev/docs/debug — "Launch browsers visually by setting headless: false in configuration or using slowMo to slow execution by milliseconds per operation")*

เหมาะเมื่อ: ต้องการดู animation, dropdown, modal ทำงานจริงก่อนเขียน locator

---

### 4.5 Screenshot และ Video — หลักฐานอัตโนมัติ

ตั้งค่าเพื่อเก็บหลักฐานเมื่อ test fail โดยไม่ต้องเพิ่ม code ใน test:

```typescript
// partial example — see Section 5 for runnable version
// playwright.config.ts
export default defineConfig({
  use: {
    screenshot: 'only-on-failure',  // screenshot เฉพาะตอน fail
    video: 'retain-on-failure',     // เก็บ video เฉพาะ test ที่ fail
  },
});
```

ไฟล์จะอยู่ใน `test-results/[test-name]/` หลัง test รัน

---

### 4.6 VS Code Extension — Debug จาก Editor

ถ้าใช้ VS Code ติดตั้ง Playwright Test extension จาก Microsoft เพื่อ:

- กด Run บน test แต่ละตัวจาก sidebar โดยตรง
- Set breakpoint ด้วย debugger ปกติ (F9) แล้ว "Debug Test" จาก right-click menu
- "Pick locator" — hover บน browser element แล้ว extension สร้าง locator ที่ดีที่สุดให้
- ดู error message inline ใน editor

extension นี้เหมาะมากสำหรับ daily workflow เพราะไม่ต้องออกจาก editor

---

### 4.7 Codegen — บันทึก Action แล้วสร้าง Test

Codegen ช่วยสร้าง test skeleton จากการ interact กับ browser จริง

```bash
npx playwright codegen http://localhost:3000
```

*(source: https://playwright.dev/docs/codegen — "Playwright comes with the ability to generate tests for you as you perform actions in the browser.")*

เมื่อรัน คุณจะเห็นสอง window:
- **Browser window**: ที่คุณ interact (click, fill, navigate)
- **Inspector window**: แสดง code ที่ generate real-time ตามที่คุณทำ

Codegen เลือก locator โดย prioritize role, text, และ test id ก่อน CSS selector *(source: https://playwright.dev/docs/codegen — "prioritizes role, text and test id locators")*

สามารถ record assertion ได้ด้วยผ่าน toolbar icons:
- `assert visibility` — element นี้ต้องมองเห็นได้
- `assert text` — element มี text นี้
- `assert value` — input มีค่านี้

**สิ่งสำคัญ:** code ที่ Codegen สร้างคือ starting point เท่านั้น ต้อง refactor ก่อนใช้จริง เช่น แยก magic string เป็น variable, รวม repeated setup เป็น fixtures, ตรวจว่า locator ยืดหยุ่นพอในทุก state ของ app

---

### 4.8 UI Mode — Interactive Test Runner พร้อม Watch

UI Mode คือ interactive runner ที่ดีที่สุดสำหรับ development phase

```bash
npx playwright test --ui
```

*(source: https://playwright.dev/docs/test-ui-mode — "UI Mode lets you explore, run, and debug tests with a time travel experience complete with a watch mode.")*

สิ่งที่ UI Mode มีให้:
- **Sidebar**: list ของ test file ทั้งหมด กด run แต่ละ test/file/ทั้งหมด
- **Filter**: กรองด้วยชื่อ test, @tags, project, status (passed/failed/skipped)
- **Timeline view**: navigation และ action พร้อม screenshot hover preview
- **DOM snapshot**: เปิด snapshot แยก window เพื่อดู DOM ณ จุดนั้น
- **Pick locator**: hover element ใน snapshot แล้วได้ locator ที่ optimize แล้ว
- **Watch mode**: กดไอคอนตา — test re-run อัตโนมัติทุกครั้งที่ save file *(source: https://playwright.dev/docs/test-ui-mode — "Eye icons activate automatic re-runs when test code changes")*
- **Network/Console**: ดู request และ log ของแต่ละ test

เหมาะเมื่อ: เขียน test ใหม่และอยากเห็น feedback ทันทีทุกครั้งที่แก้ code

---

### 4.9 Watch Mode — Auto Re-run

ถ้าต้องการแค่ auto re-run โดยไม่ต้องการ UI Mode เต็มรูป:

```bash
npx playwright test --watch
```

test จะ re-run ทุกครั้งที่ save file ใดๆ ใน test directory

---

### 4.10 PWDEBUG=console — DevTools Integration

สำหรับ debug locator โดยตรงใน browser console:

```bash
PWDEBUG=console npx playwright test
```

เมื่อรัน จะมี `playwright` object ใน DevTools console ให้ใช้:

```javascript
playwright.$('.my-selector')         // query element เดียว
playwright.$$('button')              // query ทุก element ที่ match
playwright.inspect('.selector')      // highlight ใน Elements panel
playwright.locator('text=Submit')    // สร้าง locator แล้วดู result
```

*(source: https://playwright.dev/docs/debug)*

---

### 4.11 Decision Guide — เลือก Tool ให้ถูก

| สถานการณ์ | Tool ที่เหมาะ |
|-----------|-------------|
| ไม่รู้ว่า test ทำอะไรใน step ไหน | Inspector (`--debug`) / Headed mode |
| Test พังใน CI แต่ pass local | Trace Viewer (`trace: 'on-first-retry'`) |
| ต้องการสร้าง test จากการคลิก | Codegen |
| Debug + เขียน test ไปพร้อมกัน | UI Mode (`--ui`) |
| แก้ code แล้วอยากเห็นผลทันที | Watch mode (`--watch`) หรือ UI Mode watch |
| ไม่รู้ locator ที่ถูกต้อง | Inspector "Pick locator" หรือ Codegen |
| CI screenshot ไม่พอ | เพิ่ม `video: 'retain-on-failure'` |
| Bug อยู่ตรงบรรทัดที่รู้แล้ว | `page.pause()` หยุด ณ จุดนั้น |

---

### 4.12 RF/Selenium vs Playwright — Debug Workflow

| | Robot Framework + Selenium | Playwright |
|-|--------------------------|------------|
| Step-through debug | ไม่มี built-in — ดู log keyword ด้วยตา | Inspector step-through แบบ GUI |
| Inspect element | เปิด browser DevTools แยก ระหว่าง test pause | Inspector highlight + Pick locator live |
| CI failure analysis | Download screenshot เดียว ไม่รู้ context | Trace zip — network + DOM + console + timeline |
| Record actions | ไม่มี built-in | Codegen บันทึก → generate test |
| Interactive runner | ไม่มี | UI Mode — filter, watch, timeline |
| Slow motion | ต้อง add Sleep keyword เอง | `--slow-mo=500` หรือ config `slowMo` |
| Network debugging | ต้องใช้ external proxy | Trace Viewer มี Network tab built-in |

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner: ใช้ page.pause() หยุดดู state ตรงจุดที่สงสัย

สถานการณ์: test login flow แต่ไม่แน่ใจว่าหลังกรอก username แล้ว state ของ form เป็นอย่างไรก่อนกด submit

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

test('debug login flow', async ({ page }) => {
  await page.goto('http://localhost:3000/login');

  // กรอก username ก่อน
  await page.fill('[data-testid="input-username"]', 'admin');

  // หยุดตรงนี้ — เปิด Inspector ขึ้นมาให้ inspect form state ด้วยตา
  // ก่อน run: npx playwright test --headed (ต้องรัน headed mode)
  await page.pause();

  // ดูแล้วก็รันต่อได้ด้วย play button ใน Inspector
  await page.fill('[data-testid="input-password"]', 'admin123');
  await page.click('[data-testid="btn-login"]');

  await expect(page.locator('[data-testid="session-badge"]')).toContainText('admin');
});
```

**Output ที่คาดหวัง:**
```
Running 1 test using 1 worker
Browser window opens at http://localhost:3000/login
Inspector panel opens — paused after username fill
[กด Play ใน Inspector]
  ✓  debug login flow (2.3s)

  1 passed (2.4s)
```

**หมายเหตุ:** `page.pause()` ต้องใช้กับ headed mode เท่านั้น อย่าใช้ใน CI เพราะ test จะ hang ตลอดไป

---

### Intermediate: ตั้งค่า CI Debug Config สำหรับ test ที่ flaky

สถานการณ์: ทีมมี test suite สำหรับระบบ inventory management ที่พัง CI ไม่สม่ำเสมอ ต้องการตั้งค่าให้เก็บหลักฐานทุกครั้งที่ fail โดยไม่กระทบ performance รอบปกติ

```typescript
// playwright.config.ts
// tested: Playwright v1.50+, Node.js 20+
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  // retry 1 ครั้งก่อนถือว่า fail — ทำให้ trace เก็บได้ใน on-first-retry
  retries: process.env.CI ? 2 : 0,

  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:3000',

    // trace เฉพาะรอบ retry แรก — ไม่กิน disk ใน pass runs
    trace: 'on-first-retry',

    // screenshot เฉพาะตอน fail — ดูเร็วก่อน เปิด trace ถ้าต้องการ context เพิ่ม
    screenshot: 'only-on-failure',

    // video เฉพาะ test ที่ fail — ดู real-time behavior ที่ trace ไม่ได้เล่า
    video: 'retain-on-failure',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],

  // output path สำหรับ CI artifact upload
  outputDir: 'test-results/',
});
```

**ทำไม config นี้เหมาะกับ CI:**
- `retries: 2` ใน CI ทำให้ `on-first-retry` มีโอกาส trigger เมื่อ network หรือ timing flaky
- `trace: 'on-first-retry'` ไม่กิน performance ใน pass runs ซึ่งเป็น majority
- `screenshot + video` ให้ดูเร็วก่อนเปิด trace (เบากว่า) — เปิด trace เฉพาะเมื่อต้องการ context เพิ่ม
- `outputDir` ชี้ไปที่ folder เดียวเพื่อ upload artifact ใน CI pipeline ได้ง่าย

---

### Advanced: วิเคราะห์ Failure ใน 3 สถานการณ์ที่แตกต่างกัน

นักทดสอบ senior ต้องเลือก debug strategy ต่างกันตาม context ของปัญหา ดูกรณีต่อไปนี้:

```typescript
// tests/advanced-debug-scenarios.spec.ts
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

// ── สถานการณ์ที่ 1: Local Dev — ไม่รู้ว่า element ไหนทำให้ test hang ──
// Strategy: รัน --headed --debug เพื่อ step-through ดู actionability log
// command: npx playwright test advanced-debug-scenarios.spec.ts:15 --debug
test('product search with filter', async ({ page }) => {
  await page.goto('http://localhost:3000/products');

  // สมมติ test ค้างตรงนี้โดยไม่รู้สาเหตุ — เปิด Inspector จะเห็น actionability log:
  // "waiting for element to be visible..."
  // "waiting for element to stop moving..."
  // นี่คือ clue ว่า dropdown animation ยังไม่เสร็จ
  await page.getByRole('combobox', { name: 'Category' }).selectOption('Electronics');
  await page.getByRole('button', { name: 'Apply Filters' }).click();

  // เมื่อ step ผ่าน Inspector จะเห็น network request ออกไปหลัง click
  // และ DOM เปลี่ยนแปลงทีละ step — ช่วยระบุว่า loading state กินเวลานานแค่ไหน
  await expect(page.getByTestId('product-count')).toContainText('Electronics');
});

// ── สถานการณ์ที่ 2: CI Failure — test พังเฉพาะ CI environment ──
// Strategy: ดึง trace.zip จาก CI artifact แล้วเปิดใน Trace Viewer
// ใน trace จะเห็น:
//   - Network tab: API /api/products/search return 503 ใน CI เพราะ service ยังไม่ ready
//   - Timeline: test เริ่มหลัง 0ms แต่ API call ออกไปที่ 800ms หลัง click
//   - Console: มี error "TypeError: Cannot read property 'data' of undefined"
test('product search returns results', async ({ page }) => {
  await page.goto('http://localhost:3000/products');
  await page.getByTestId('search-input').fill('laptop');
  await page.getByTestId('search-button').click();

  // ใน CI: API ช้ากว่า local → ต้องเพิ่ม waitFor หรือ ตรวจสอบ network mock
  // Trace Viewer แสดงว่า request ออกไปแต่ response กลับมาหลัง assertion timeout
  await expect(page.getByTestId('search-results')).toBeVisible();
  await expect(page.getByTestId('result-item').first()).toBeVisible();
});

// ── สถานการณ์ที่ 3: Performance Diagnosis — หา action ที่ช้าที่สุด ──
// Strategy: เปิด trace: 'on' ชั่วคราว แล้วดู Actions tab ใน Trace Viewer
// Actions tab แสดง duration ของแต่ละ action — หา action ที่ใช้เวลานานผิดปกติ
test('complete order workflow — performance baseline', async ({ page }) => {
  await page.goto('http://localhost:3000/cart');

  // หลังเปิด Trace Viewer → Actions tab:
  // ✓ goto (245ms)
  // ✓ fill card-number (12ms)
  // ✓ click place-order (8ms)
  // ✗ waiting for order-confirmation (4,800ms ← ใกล้ timeout แล้ว!)
  // นี่คือ signal ว่า API /api/orders ช้า ต้อง investigate ที่ backend ไม่ใช่ test
  await page.getByTestId('card-number').fill('4111111111111111');
  await page.getByTestId('card-expiry').fill('12/28');
  await page.getByTestId('card-cvv').fill('123');
  await page.getByRole('button', { name: 'Place Order' }).click();

  await expect(page.getByTestId('order-confirmation')).toBeVisible({ timeout: 10000 });
});
```

**สรุป Strategy เลือกตาม context:**

| สถานการณ์ | Debug Strategy | ข้อมูลที่ได้ |
|-----------|---------------|------------|
| Local — ไม่รู้ว่า test ค้างตรงไหน | `--debug` Inspector | Actionability log real-time, DOM state ทุก step |
| CI failure — reproduce ไม่ได้ local | Trace Viewer (`on-first-retry`) | Network, DOM snapshot, console, timeline ครบ |
| Performance slow test | `trace: 'on'` + Actions tab duration | ระบุ action ที่ช้าได้แม่นยำ |

---

## 6. Common Mistakes ❌→✅

**1. ทิ้ง page.pause() ไว้ใน code ก่อน commit**

❌
```typescript
test('checkout', async ({ page }) => {
  await page.goto('http://localhost:3000/checkout');
  await page.pause(); // ← ลืมลบ
  await page.getByRole('button', { name: 'Pay' }).click();
});
```

✅
```typescript
// ตรวจก่อน commit เสมอ:
// grep -r "page.pause" tests/
// หรือตั้ง pre-commit hook
test('checkout', async ({ page }) => {
  await page.goto('http://localhost:3000/checkout');
  await page.getByRole('button', { name: 'Pay' }).click();
  await expect(page.getByTestId('payment-success')).toBeVisible();
});
```

เหตุผล: `page.pause()` ทำให้ test รอ user interaction ตลอดไป — CI job จะ hang จนกว่าจะ timeout *(source: https://playwright.dev/docs/debug)*

---

**2. ใช้ trace: 'on' ตลอดเวลาใน CI**

❌
```typescript
// playwright.config.ts — config ที่กิน performance และ disk
use: {
  trace: 'on',  // บันทึกทุก test ทุกรอบ — test suite 100 tests = 100 trace files
},
```

✅
```typescript
use: {
  trace: 'on-first-retry',  // บันทึกเฉพาะเมื่อ test fail และ retry
},
```

เหตุผล: `trace: 'on'` สร้าง trace ทุก test — pass tests ส่วนใหญ่ไม่ต้องการ trace เลย ทำให้ CI ช้าและ disk เต็ม *(source: https://playwright.dev/docs/trace-viewer)*

---

**3. ใช้ PWDEBUG=1 ใน CI pipeline**

❌
```yaml
# .github/workflows/test.yml — ผิด
- name: Run tests
  run: PWDEBUG=1 npx playwright test
```

✅
```yaml
# ถูกต้อง — ไม่ใช้ PWDEBUG ใน CI
- name: Run tests
  run: npx playwright test
- name: Upload traces
  if: failure()
  uses: actions/upload-artifact@v3
  with:
    name: playwright-traces
    path: test-results/
```

เหตุผล: `PWDEBUG=1` เปิด Inspector และรอ user interaction — ใน CI ไม่มีใครกด play ดังนั้น test จะ timeout ทุกตัว *(source: https://playwright.dev/docs/debug)*

---

**4. Copy code จาก Codegen โดยตรงโดยไม่ refactor**

❌
```typescript
// code ที่ Codegen สร้าง — ใช้ได้แต่ไม่ maintainable
test('test', async ({ page }) => {
  await page.goto('http://localhost:3000/products');
  await page.locator('#filter-category').selectOption('Electronics');
  await page.locator('#btn-apply').click();
  await expect(page.locator('#product-list')).toContainText('Laptop');
  await expect(page.locator('#product-list')).toContainText('Phone');
  await expect(page.locator('#product-list')).toContainText('Tablet');
});
```

✅
```typescript
// หลัง refactor — ใช้ proper locators และ descriptive test name
test('filter products by category shows correct items', async ({ page }) => {
  await page.goto('http://localhost:3000/products');

  await page.getByRole('combobox', { name: 'Category' }).selectOption('Electronics');
  await page.getByRole('button', { name: 'Apply Filters' }).click();

  const productList = page.getByTestId('product-list');
  await expect(productList).toContainText('Laptop');
  await expect(productList).toContainText('Phone');
  await expect(productList).toContainText('Tablet');
});
```

เหตุผล: Codegen prioritize role/text/testid แต่บางครั้งก็ใช้ CSS selector เมื่อไม่มีทางเลือกที่ดีกว่า — ต้อง review ทุก locator ว่า semantic และ resilient ต่อ DOM change *(source: https://playwright.dev/docs/codegen)*

---

**5. ไม่ตั้ง retries ใน config ทำให้ on-first-retry ไม่ทำงาน**

❌
```typescript
// playwright.config.ts — trace จะไม่ถูกสร้างเลยแม้ test fail
export default defineConfig({
  use: {
    trace: 'on-first-retry',
  },
  // ← ไม่มี retries — ไม่มี retry → ไม่มี trace
});
```

✅
```typescript
export default defineConfig({
  retries: process.env.CI ? 1 : 0,  // retry ใน CI เท่านั้น
  use: {
    trace: 'on-first-retry',
  },
});
```

เหตุผล: `on-first-retry` บันทึกเฉพาะ "รอบ retry แรก" — ถ้า `retries: 0` ไม่มีรอบ retry เลย trace จึงไม่ถูกสร้างแม้ test จะ fail *(source: https://playwright.dev/docs/trace-viewer)*

---

## 7. สรุปบท + Retrieval Questions

ในบทนี้คุณได้เรียน:

- **Playwright Inspector** — step-through debugging ด้วย `--debug` flag หรือ `page.pause()` เหมาะสำหรับ local dev เท่านั้น ไม่ใช้ใน CI
- **Trace Viewer** — เครื่องมือหลักสำหรับ CI failure analysis บันทึก network, DOM snapshot, console, timeline ต้องตั้ง `trace` ใน config ก่อน
- **Headed mode** — `--headed` / `slowMo` สำหรับดู browser ทำงานจริง
- **Screenshot / Video** — `only-on-failure` / `retain-on-failure` สำหรับเก็บหลักฐานอัตโนมัติ
- **Codegen** — บันทึก action → generate test skeleton → ต้อง refactor ก่อนใช้จริง
- **UI Mode** — `--ui` interactive runner พร้อม watch mode, filter, timeline
- **Decision guide** — เลือก tool ให้ตรงกับสถานการณ์ (local dev / CI failure / เขียน test ใหม่)

---

ก่อนอ่านบทถัดไป ลองตอบคำถามเหล่านี้ด้วยตัวเองก่อน (ไม่ต้อง scroll ขึ้นไปดูเฉลย):

**คำถามที่ 1:** คุณมี test ที่พังใน GitHub Actions ทุกรอบ แต่รัน local ผ่านตลอด error message บอกแค่ว่า `expect(locator).toBeVisible() → element not visible` โดยไม่มีข้อมูลเพิ่มเติม — คุณจะ debug อย่างไร? ระบุขั้นตอนและ tool ที่ใช้ตามลำดับ

**คำถามที่ 2:** config นี้มีปัญหาอะไร และจะแก้ยังไง?
```typescript
export default defineConfig({
  use: {
    trace: 'on-first-retry',
    screenshot: 'on',
    video: 'on',
  },
});
```

**คำถามที่ 3:** `PWDEBUG=1` กับ `PWDEBUG=console` ต่างกันอย่างไร — ใช้แต่ละอันเมื่อไหร่?

<details>
<summary>ดูเฉลย</summary>

**คำถามที่ 1:** ขั้นตอน: (1) ตั้ง `retries: 1` และ `trace: 'on-first-retry'` ใน config แล้ว push ไป CI รอบหนึ่ง (2) download `test-results/` artifact จาก GitHub Actions (3) รัน `npx playwright show-trace path/to/trace.zip` (4) ดู Network tab ว่ามี API error หรือ timeout ไหม, ดู Console tab ว่ามี JavaScript error ไหม, ดู Timeline ว่า element ไม่ปรากฏเพราะอะไร (network ช้า / race condition / environment config ต่างกัน) — วิธีนี้ให้ข้อมูลมากกว่าการเดาเพิ่ม log แล้ว re-run ใหม่

**คำถามที่ 2:** ปัญหาสองจุด: (1) `screenshot: 'on'` และ `video: 'on'` บันทึกทุก test ทุกรอบ — pass tests ที่เป็น majority ไม่ต้องการ artifact เหล่านี้เลย ทำให้ CI ช้าและ disk เต็ม (2) ไม่มี `retries` ดังนั้น `trace: 'on-first-retry'` จะไม่ทำงาน แก้เป็น: `retries: process.env.CI ? 1 : 0`, `trace: 'on-first-retry'`, `screenshot: 'only-on-failure'`, `video: 'retain-on-failure'`

**คำถามที่ 3:** `PWDEBUG=1` เปิด Playwright Inspector GUI แบบ visual step-through — เหมาะเมื่อต้องการเดิน action ทีละ step ดู element highlight และ actionability log ใช้เมื่อไม่รู้ว่า test ค้างอยู่ที่ action ไหน ส่วน `PWDEBUG=console` inject `playwright` object เข้า browser DevTools console — เหมาะเมื่อต้องการ query element และทดสอบ locator ด้วยตัวเองใน console โดยไม่ต้องแก้ code และ re-run ใช้เมื่อรู้คร่าวๆ แล้วว่าปัญหาคือ locator และต้องการ iterate เร็ว

</details>
