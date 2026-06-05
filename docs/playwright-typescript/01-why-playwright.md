# บทที่ 1: ทำไมต้อง Playwright?

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- เข้าใจว่า Playwright แก้ปัญหาอะไรที่ Selenium/Robot Framework ทำได้ไม่ดีพอ
- อธิบาย auto-waiting ได้ว่าคืออะไร และทำไมมันทำให้ test เสถียรกว่า
- ตัดสินใจได้ว่าเมื่อไหร่ควรใช้ Playwright แทน RF/Selenium และเมื่อไหร่ไม่ควร
- เข้าใจ mindset shift จาก keyword-driven testing → code-first testing

---

## 2. ทำไมต้องรู้? (Why)

ถ้าคุณเคยใช้ Robot Framework + Selenium มาสักพัก คุณน่าจะเจอปัญหาเหล่านี้บ้าง:

**Test Flaky จาก Wait**

```robot
Wait Until Element Is Visible    id:submit-btn    timeout=10s
Click Button    id:submit-btn
Wait Until Page Contains    Success    timeout=15s
```

บางครั้งมันผ่าน บางครั้งมันพัง — โดยเฉพาะใน CI ที่ machine ช้ากว่า dev laptop ตัวเอง คุณเพิ่ม timeout จาก 10s เป็น 15s แล้วก็ยัง flaky อยู่ดี

**ChromeDriver Version Mismatch**

"SessionNotCreatedException: Chrome version must be between 114 and 118" — ข้อความนี้คุ้นไหม? Chrome auto-update ทำให้ ChromeDriver เก่าพังทันที ต้องมานั่ง update driver ทุกครั้ง

**Debug ใน CI ยากมาก**

Test พัง แต่ดู log แล้วไม่รู้ว่าเกิดอะไรขึ้น ได้แต่ screenshot ณ ขณะที่พัง แต่ไม่รู้ว่าก่อนหน้านั้น 5 steps เกิดอะไรขึ้น

**Test ช้า**

Selenium เปิด browser ใหม่ทุก test case — ถ้ามี 100 test ก็เปิด browser 100 รอบ ใช้เวลานานมาก

**IDE ช่วยได้น้อย**

ใน Robot Framework keywords ไม่มี autocomplete ที่ดี ต้องจำ keyword ชื่อยาว พิมพ์ผิดตอน runtime ถึงรู้

ปัญหาเหล่านี้ไม่ใช่ความผิดของคุณ — มันเป็น limitation ของ architecture ของ Selenium/RF ที่ออกแบบมาตั้งแต่ยุคที่ web ยังไม่ซับซ้อนแบบนี้ Playwright ถูกสร้างมาเพื่อแก้ปัญหาเหล่านี้โดยตรง

---

## 3. Analogy: GPS อัจฉริยะ vs คนขับแท็กซี่ที่ต้องสั่งทุกอย่าง

ลองนึกภาพการเดินทางจาก A ไป B สองแบบ:

**Selenium เหมือนคนขับแท็กซี่ที่รอรับคำสั่งทุกขั้นตอน** — คุณต้องบอกว่า "เลี้ยวซ้ายที่แยกถัดไป", "หยุดตรงนี้รอ traffic light ก่อน", "ตอนนี้ไฟเขียวแล้วออกตัวได้" ถ้าคุณลืมบอกขั้นตอนใดขั้นตอนหนึ่ง รถก็ไม่ขยับ หรือออกตัวก่อน traffic light เปลี่ยน

**Playwright เหมือน GPS อัจฉริยะที่รู้ว่าต้องรอ traffic light เองก่อนออกตัว** — คุณแค่บอก destination ว่า "คลิก Submit button" GPS จะรู้เองว่าต้องรอให้ปุ่มนั้น visible, ไม่กำลัง animate, และ enabled ก่อน — แล้วค่อยออกตัว

---

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**

- **Playwright ทำงานทุกอย่างอัตโนมัติโดยไม่ต้องเขียน code** — ยังต้องเขียน action ทุกอย่างเหมือนเดิม (`click()`, `fill()`, `goto()`) แค่ไม่ต้องเขียน wait ก่อน action เหล่านั้น
- **Playwright ไม่มีทางพัง** — ยังพังได้ถ้า timing logic ผิด เช่น assert ก่อน API response กลับมา หรือใช้ `waitForTimeout()` แทนที่จะใช้ condition-based wait

---

## 4. เนื้อหาหลัก

### ปัญหาของ Selenium + Robot Framework (ในเชิง Architecture)

**Layer ที่เยอะเกินไป**

Selenium ทำงานผ่าน WebDriver protocol ซึ่งเป็น HTTP — code ของคุณส่ง HTTP request ไปหา WebDriver server, WebDriver server ส่งคำสั่งต่อไปหา browser อีกที นั่นคือ 3 layer:

```
Your Code → HTTP → WebDriver Server → Browser
```

ทุก action มี network latency ระหว่าง layer และถ้า WebDriver version ไม่ตรงกับ browser version — พังทันที

**Explicit Waits ทุกที่**

เพราะ WebDriver ไม่รู้ว่า element พร้อมหรือยัง คุณต้องบอกมันทุกครั้ง:

```robot
Wait Until Element Is Visible    id:submit-btn    timeout=10s
Wait Until Element Is Enabled    id:submit-btn    timeout=10s
Click Button    id:submit-btn
```

ถ้าลืม wait ขั้นตอนใดขั้นตอนหนึ่ง test ก็ flaky ถ้า wait เยอะเกิน test ก็ช้า

**Robot Framework: Keyword Overhead**

RF keyword-driven มีข้อดีตรงที่อ่านง่ายสำหรับคนที่ไม่ได้ code — แต่มีข้อเสียคือ IDE support อ่อนมาก, debug ยาก (ต้องดู log หลายชั้น), และ reuse keyword ข้าม project ทำได้ยาก

---

### Playwright แก้ปัญหาเหล่านี้ยังไง

**Auto-waiting: หัวใจของ Playwright**

ก่อน action ทุกตัว Playwright ทำ actionability checks อัตโนมัติ ตาม official docs:

> "Playwright performs a range of actionability checks on the elements before making actions to ensure these actions behave as expected."
> *(source: https://playwright.dev/docs/actionability)*

สิ่งที่ Playwright check ก่อน action:

| Check | หมายความว่า |
|-------|------------|
| **Visible** | Element มี bounding box และไม่มี `visibility:hidden` |
| **Stable** | Element ไม่กำลัง animate (bounding box ไม่เปลี่ยนใน 2 frames ติดกัน) |
| **Enabled** | Element ไม่ได้ถูก disable ด้วย `[disabled]` หรือ `[aria-disabled=true]` |
| **Editable** | Element enabled และไม่ readonly (สำหรับ input fields) |
| **Receives Events** | Element เป็น hit target จริงๆ (ไม่มีอะไรทับอยู่) |

คุณไม่ต้องเขียน wait เลย — Playwright จัดการให้ทั้งหมด

**Auto-waiting ใน Assertions ด้วย**

Locator ของ Playwright มี auto-waiting built-in เช่นกัน:

> "Locators come with auto waiting and retry-ability. Auto waiting means that Playwright performs a range of actionability checks on the elements, such as ensuring the element is visible and enabled before it performs the click."
> *(source: https://playwright.dev/docs/best-practices)*

และสำหรับ assertions:

> "By using web first assertions Playwright will wait until the expected condition is met."
> *(source: https://playwright.dev/docs/best-practices)*

**Protocol ตรงกว่า**

Playwright ใช้ CDP (Chrome DevTools Protocol) และ WebSocket เชื่อมตรงกับ browser โดยไม่ผ่าน WebDriver HTTP layer — เร็วกว่าและ stable กว่า

**Built-in Browsers — ไม่มี Driver Mismatch อีกต่อไป**

Playwright ติดตั้ง Chromium, Firefox, และ WebKit มาให้พร้อมใช้ เพียง `npx playwright install` ครั้งเดียว ไม่ต้องยุ่งกับ ChromeDriver, GeckoDriver, หรือ Safari driver อีกเลย

> "Playwright supports Chromium, WebKit and Firefox on Windows, Linux and macOS, locally or in CI, headless or headed, with native mobile emulation for Chrome (Android) and Mobile Safari."
> *(source: https://playwright.dev/docs/intro)*

**TypeScript First-Class**

Playwright เขียนด้วย TypeScript และ type definitions ครบ IDE อย่าง VS Code จะ autocomplete ทุก method พร้อม type checking — ถ้าพิมพ์ method ชื่อผิดจะรู้ตั้งแต่ตอน write ไม่ต้องรอ runtime

---

### Comparison: RF + Selenium vs Playwright

| | Robot Framework + Selenium | Playwright |
|---|---|---|
| **Wait strategy** | Explicit: `Wait Until Element Is Visible` | Auto-waiting built-in ทุก action |
| **Protocol** | WebDriver (HTTP, 3 layers) | CDP/WebSocket (direct to browser) |
| **Browser management** | ChromeDriver ต้องตรง version | Built-in browsers, `npx playwright install` |
| **Language** | Custom DSL (keywords) | TypeScript/Python/Java/C# |
| **IDE support** | Limited, ไม่มี type checking | Full autocomplete + types |
| **Debugging** | Log files, screenshot | Trace Viewer, Inspector, Video |
| **Parallel testing** | ยาก (ต้องการ Selenium Grid) | Built-in workers, ง่ายมาก |
| **Network control** | ไม่มี | `waitForResponse()`, `route()` (mock API) |

---

### เมื่อไหร่ Robot Framework ยังดีกว่า

Playwright ไม่ใช่คำตอบสำหรับทุกสถานการณ์:

- **Team ไม่มี coding background** — keyword-driven ของ RF อ่านง่ายกว่าสำหรับ non-developer, business analyst เข้ามาช่วย maintain ได้
- **Legacy test suite ใหญ่มาก** — ถ้ามี RF test 500+ cases migration cost สูงมาก ต้องคำนวณ ROI ก่อน
- **Non-browser testing** — RF มี library สำหรับ desktop app, mobile (Appium), API, database ที่ mature มาก
- **ทีมต้องการ low-code approach** — RF เหมาะกับองค์กรที่ต้องการให้ QA ที่ไม่ใช่ developer maintain test ได้

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner — Login: RF vs Playwright เปรียบ side by side

**Robot Framework + Selenium (explicit waits ทุกที่)**

```robot
*** Test Cases ***
Login With Valid Credentials
    Open Browser    http://localhost:3000/login    chromium
    Wait Until Element Is Visible    id:username    timeout=10s
    Input Text    id:username    testuser
    Wait Until Element Is Visible    id:password    timeout=10s
    Input Text    id:password    test123
    Wait Until Element Is Enabled    xpath://button[@type='submit']    timeout=10s
    Click Button    xpath://button[@type='submit']
    Wait Until Page Contains    Dashboard    timeout=15s
    Close Browser
```

สังเกตว่า `Wait Until Element Is Visible` / `Wait Until Element Is Enabled` ปรากฏ 3 ครั้ง — นั่นคือ manual wait ที่ต้องเขียนทุก action

**Playwright TypeScript (ไม่มี sleep หรือ explicit wait เลย)**

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

test('login with valid credentials', async ({ page }) => {
  await page.goto('http://localhost:3000/login');
  await page.fill('[data-testid="input-username"]', 'testuser');
  await page.fill('[data-testid="input-password"]', 'test123');
  await page.click('[data-testid="btn-login"]');
  await expect(page).toHaveURL('http://localhost:3000/');
});
```

Playwright รู้เองว่าต้องรอให้ input field visible + enabled ก่อน `fill()` และรอให้ button visible + enabled + stable ก่อน `click()` ไม่ต้องบอก

---

### Intermediate — CI Pipeline มี Test Flaky เพราะ API ช้า

สมมติว่าคุณมี test ที่ verify ว่า product list โหลดมาแสดงถูกต้อง แต่ใน CI มัน flaky เพราะ API `/api/products` response ช้ากว่า local:

**แบบ Selenium (ใช้ sleep — flaky ใน CI)**

```python
# Python Selenium
driver.get("http://localhost:3000/shop")
time.sleep(3)  # หวังว่า 3 วินาทีพอ
product_grid = driver.find_element(By.TEST_ID, "product-grid")
assert product_grid.is_displayed()
```

ปัญหา: ถ้า CI machine ช้า 3 วินาทีอาจไม่พอ ถ้า fast 3 วินาทีเสียเวลาฟรี

**แบบ Playwright (รอ API response จริงๆ — deterministic)**

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { test, expect } from '@playwright/test';

test('products load from API', async ({ page }) => {
  await page.goto('http://localhost:3000/shop');

  // รอ API call เสร็จก่อนแบบ explicit condition — ไม่ต้อง sleep
  await page.waitForResponse(
    resp => resp.url().includes('/api/products') && resp.status() === 200
  );

  // ตอนนี้มั่นใจว่า API response มาแล้ว
  await expect(page.getByTestId('product-grid')).toBeVisible();
});
```

`waitForResponse()` รอ network event จริงๆ — ไม่ว่า CI จะช้าหรือเร็วแค่ไหน test จะผ่านก็ต่อเมื่อ API response มาแล้วจริงๆ

---

### Advanced — Decision Matrix: Migrate หรือไม่?

สมมติคุณเป็น QA Lead ที่ต้องตัดสินใจว่าจะ migrate RF test suite 80 cases ไป Playwright หรือไม่ นี่คือ framework สำหรับตัดสินใจ:

**ปัจจัยที่ต้องประเมิน:**

| ปัจจัย | เอียงไป Playwright | เอียงไป RF |
|--------|------------------|-----------|
| Team skill | Dev background | Non-developer |
| Test type | E2E web, visual | Desktop, API, mixed |
| Flakiness ปัจจุบัน | สูง (wait issues) | ต่ำ (stable อยู่แล้ว) |
| Parallel testing | ต้องการ | ไม่จำเป็น |
| Migration cost | 80 cases × 2-4h = 160-320h | (baseline) |
| ROI ระยะยาว | Faster CI, less flaky | ไม่ต้อง retrain |

**Strategy ที่แนะนำ — Gradual Migration:**

```
Phase 1: New tests → Playwright ทั้งหมด
Phase 2: Flaky RF tests → Migrate ก่อน (ROI สูงสุด)  
Phase 3: ประเมินผล 3 เดือน → ตัดสินใจ migrate ที่เหลือหรือไม่
```

ไม่ต้อง migrate ทั้งหมดพร้อมกัน RF และ Playwright สามารถอยู่ร่วมกันใน CI pipeline ได้

---

## 6. Common Mistakes

❌ **ยังใช้ `page.waitForTimeout(2000)` ทุกที่** — เหมือนกับ `time.sleep()` ใน Selenium ทำให้ test ช้าและยัง flaky อยู่ดีถ้า network ช้ากว่า timeout

```typescript
// ❌ แบบผิด
await page.click('[data-testid="btn-save"]');
await page.waitForTimeout(2000); // หวังว่า 2 วินาทีพอ
await expect(page.getByText('Saved')).toBeVisible();
```

```typescript
// ✅ แบบถูก — ใช้ web-first assertion ที่รอ condition จริงๆ
await page.click('[data-testid="btn-save"]');
await expect(page.getByText('Saved')).toBeVisible(); // รอจนกว่าจะเห็น
```

*(source: https://playwright.dev/docs/best-practices)*

---

❌ **คิดว่า Playwright ต้องการ ChromeDriver หรือ browser ที่ติดตั้งไว้ก่อน**

```bash
# ❌ แบบผิด — พยายามหา ChromeDriver มาติดตั้งเอง
npm install chromedriver
```

```bash
# ✅ แบบถูก — Playwright จัดการ browser เอง
npx playwright install
# Playwright จะ download Chromium, Firefox, WebKit ให้ครบ
```

*(source: https://playwright.dev/docs/intro)*

---

❌ **เขียน test แบบ keyword-driven ใน TypeScript — แปล RF keywords มาเป็น helper function ทุก step**

```typescript
// ❌ แบบผิด — คิดแบบ RF keyword-driven
async function waitAndClick(page: Page, selector: string) {
  await page.waitForSelector(selector, { state: 'visible' });
  await page.waitForSelector(selector, { state: 'enabled' });
  await page.click(selector);
}

await waitAndClick(page, '#submit-btn');
```

```typescript
// ✅ แบบถูก — Playwright locator จัดการ wait ให้แล้ว ใช้ตรงๆ
await page.click('#submit-btn'); // auto-waiting เกิดขึ้นอัตโนมัติ
// หรือดีกว่า: ใช้ locator
await page.locator('#submit-btn').click();
```

*(source: https://playwright.dev/docs/best-practices)*

---

❌ **ใช้ `isVisible()` แทน `toBeVisible()` ใน assertion**

```typescript
// ❌ แบบผิด — isVisible() ไม่รอ, return ทันที
const isVisible = await page.locator('.success-message').isVisible();
expect(isVisible).toBe(true); // อาจ false เพราะ message ยังไม่ขึ้น
```

```typescript
// ✅ แบบถูก — toBeVisible() รอจนกว่า element จะปรากฏ
await expect(page.locator('.success-message')).toBeVisible();
```

> "When using assertions such as `isVisible()` the test won't wait a single second, it will just check the locator is there and return immediately."
> *(source: https://playwright.dev/docs/best-practices)*

---

## 7. สรุปบท

**จุดสำคัญที่ต้องจำ:**

- Playwright ถูกสร้างมาเพื่อแก้ปัญหา flaky test, driver mismatch, และ debugging ที่ยากของ Selenium รุ่นเก่า
- **Auto-waiting** คือหัวใจของ Playwright — ก่อน action ทุกตัวจะ check visible, stable, enabled, editable อัตโนมัติ ไม่ต้องเขียน wait เอง
- Playwright ใช้ CDP/WebSocket ต่อตรงกับ browser — เร็วกว่าและ stable กว่า WebDriver HTTP protocol
- RF ยังดีกว่าในบางสถานการณ์ เช่น team ไม่มี coding background หรือ non-browser testing

---

**Retrieval Questions — ลองตอบก่อนดูเฉลย:**

1. Playwright ใช้ protocol อะไรในการสื่อสารกับ browser? มันต่างจาก Selenium อย่างไรในแง่ architecture?

2. "Auto-waiting" หมายความว่าอย่างไร? Playwright check อะไรบ้างก่อน `click()` ทุกครั้ง?

3. ถ้าทีมมี 50 test cases ใน Robot Framework และต้องการ migrate เป็น Playwright — คุณจะแนะนำให้ migrate ทั้งหมดพร้อมกันทันทีหรือไม่? ถ้าไม่ จะเริ่มจากตรงไหนก่อน?

<details>
<summary>ดูเฉลย</summary>

1. Playwright ใช้ **CDP (Chrome DevTools Protocol)** และ **WebSocket** เชื่อมตรงกับ browser โดยไม่ผ่าน WebDriver HTTP layer — Selenium ต้องส่ง HTTP request ผ่าน 3 layer (code → WebDriver server → browser) ซึ่งช้ากว่าและมี failure point เพิ่มขึ้น

2. ก่อน action ทุกตัว Playwright รอให้ element ผ่านเงื่อนไขเหล่านี้: **Visible** (มี bounding box, ไม่ hidden), **Stable** (ไม่กำลัง animate), **Enabled** (ไม่ถูก disable), **Editable** (สำหรับ input: enabled + ไม่ readonly), และ **Receives Events** (ไม่มีอะไรทับอยู่)

3. ไม่แนะนำให้ migrate ทั้งหมดพร้อมกัน — ควรเริ่มจาก **test ใหม่ทั้งหมดใช้ Playwright** และ **migrate RF test ที่ flaky มากที่สุดก่อน** (ROI สูงสุด) แล้วประเมินผลหลัง 3 เดือนก่อนตัดสินใจ migrate ที่เหลือ

</details>
