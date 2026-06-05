## ก่อนอ่านบทนี้ ลองตอบ:

1. ทำไม hybrid test ที่ setup ข้อมูลผ่าน API แล้ว verify ผ่าน UI ถึงดีกว่า full UI test ในแง่ความเร็วและความเปราะบาง? และ `toBeOK()` ต่างจาก `expect(res.status()).toBe(200)` อย่างไร?

2. คุณต้องทดสอบว่า "หลังจาก user login สำเร็จแล้ว profile page แสดงชื่อถูกต้อง" — ถ้าใช้ hybrid approach คุณจะ setup auth state อย่างไร และทำไมถึงไม่ควรใช้ `fetch()` ธรรมดาแทน `request` fixture?

---

เฉลย:

1. Hybrid approach เร็วกว่าเพราะ API calls ใช้เวลา ~50ms ต่อ call เทียบกับ UI form fill ที่ใช้ 2-3 วินาทีต่อ field — สำหรับ 20 items ต่างกันระหว่าง ~500ms กับ ~60 วินาที นอกจากนั้นยัง robust กว่าเพราะ API contract เปลี่ยนน้อยกว่า UI ส่วน `toBeOK()` accept status 200-299 ทั้งหมด (รวม 201, 204) ในขณะที่ `toBe(200)` เป็น exact match — ใช้ `toBeOK()` สำหรับ general success check และ `toBe(201)` เมื่อต้องการยืนยัน create response โดยเฉพาะ

2. Setup ผ่าน `request` fixture — POST ไป `/api/auth/login` เพื่อรับ token หรือใช้ storageState ที่บันทึกไว้ล่วงหน้า `request` fixture ดีกว่า `fetch()` ธรรมดาเพราะมี baseURL, TLS handling, context lifecycle ที่ integrate กับ Playwright runner — cookies และ auth state ที่ได้จาก `request` fixture สามารถ share กับ page context ได้ผ่าน storageState

---

# บทที่ 16: Visual Testing & Accessibility Testing

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- ใช้ `toHaveScreenshot()` เพื่อทำ visual regression testing แบบ built-in ไม่ต้องติดตั้ง library เพิ่ม
- เข้าใจว่าทำไม screenshot ถึงต่างกันระหว่าง OS และจะจัดการ CI อย่างไร
- ติดตั้งและใช้ `@axe-core/playwright` เพื่อ scan accessibility issues ตาม WCAG standards
- ใช้ `toMatchAriaSnapshot()` เพื่อ verify accessibility tree ของ component โดยไม่ขึ้นกับ OS
- รู้จัก WCAG violations ที่พบบ่อยและวิธี scope การ scan ให้ตรงจุด
- เปรียบเทียบ approach นี้กับ Robot Framework + Selenium ที่ไม่มี visual testing built-in

---

## 2. ทำไมต้องรู้? (Why)

ลองนึกถึงสถานการณ์นี้: designer push code ที่ทำให้สีปุ่ม "Add to Cart" เปลี่ยนจาก `#2563EB` เป็น `#2564EB` (ต่างกัน 1 hex digit) — test ทุก test ผ่าน เพราะ test ของคุณตรวจแค่ว่า "ปุ่มมีข้อความ 'Add to Cart' อยู่" ไม่ได้ตรวจว่าสีถูกต้อง

ทั้งสอง approach นี้ไม่สามารถตรวจได้:

- **Functional test**: ตรวจ behavior — "เมื่อกดปุ่มแล้วสินค้าเข้า cart" ✅ ตรวจ logic ได้ ❌ ไม่รู้ว่าหน้าตาเปลี่ยนไปไหม
- **Manual QA**: ตรวจสายตา — แต่ทำ regression test ทุก deploy ไม่ไหว และคนเหนื่อยก็พลาดได้

**Visual regression testing** แก้ปัญหานี้: ถ่าย screenshot baseline ไว้ แล้วทุก run เปรียบเทียบ pixel-by-pixel — ถ้า 1 pixel ผิดก็ fail ทันที

แต่ "หน้าตาสวย" ไม่ได้แปลว่า "ทุกคนใช้ได้" — ถ้า button ไม่มี `aria-label` คนที่ใช้ screen reader จะไม่รู้ว่าปุ่มนี้ทำอะไร

**Accessibility testing** แก้ปัญหานี้: ตรวจ WCAG compliance อัตโนมัติ ว่า app รองรับ screen readers, keyboard navigation, และ contrast ratio ที่เพียงพอ

ใน Playwright ทั้งสองอย่างนี้ทำได้ใน test suite เดียวกัน:
- `toHaveScreenshot()` — visual regression, built-in
- `@axe-core/playwright` — WCAG compliance scan, ติดตั้งง่าย 1 command
- `toMatchAriaSnapshot()` — ตรวจ accessibility tree structure, built-in ตั้งแต่ v1.49

ส่วน Robot Framework + Selenium ไม่มี visual testing built-in เลย — ต้องใช้ third-party service เช่น Applitools หรือ Percy ซึ่งมีค่าใช้จ่าย

---

## 3. Analogy

**Visual testing คือช่างภาพที่ถ่าย reference photo แล้วเปรียบเทียบกับรูปจริงทีละ pixel — Aria snapshot คือผู้อ่าน screen reader ที่ตรวจว่า app เข้าถึงได้จริง**

จินตนาการว่าคุณเป็นช่างภาพประกันภัยที่ถ่ายภาพรถลูกค้าก่อนเช่า แล้วตรวจเทียบตอนคืน — คุณวางรูปสองรูปซ้อนกันแล้วดูว่ามี pixel ไหนที่เปลี่ยนไปบ้าง ถ้ารอยขีดข่วนใหม่ปรากฏแม้แต่ 1 จุด คุณจะจับได้ทันที นั่นคือ `toHaveScreenshot()` — เปรียบเทียบ baseline กับ actual ทีละ pixel

ส่วนการตรวจ accessibility คล้ายกับ inspector ที่ตรวจว่าอาคารมี ramp สำหรับ wheelchair ไหม มี Braille sign ไหม มี emergency exit ที่ทุกคนเข้าถึงได้ไหม — inspector ไม่สนใจว่าอาคารสวยแค่ไหน แต่สนใจว่าทุกคนใช้ได้จริงไหม นั่นคือ `@axe-core` + `toMatchAriaSnapshot()`

⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:
- **Visual snapshot ตรวจ logic ได้** — ตรวจเฉพาะ visual appearance ไม่ใช่ behavior ถ้าปุ่มมีสีถูกแต่ไม่ทำงาน visual test จะผ่านแต่ functional test จะ fail
- **Accessibility test รัน OS ไหนก็ได้เหมือนกัน** — ครึ่งจริง: `toMatchAriaSnapshot()` ไม่ขึ้นกับ OS แต่ `toHaveScreenshot()` ต่างกันระหว่าง macOS/Linux/Windows เพราะ font rendering และ anti-aliasing ต่างกัน

---

## 4. เนื้อหาหลัก

### 4.1 Visual Testing ด้วย `toHaveScreenshot()`

Playwright มี visual regression testing built-in ผ่าน `toHaveScreenshot()` ซึ่งเป็นส่วนหนึ่งของ `expect` API ไม่ต้องติดตั้งอะไรเพิ่ม

**พื้นฐาน:**

```typescript
// Full page screenshot
await expect(page).toHaveScreenshot('dashboard.png');

// Element เฉพาะส่วน
await expect(page.locator('[data-testid^="product-card-"]').first()).toHaveScreenshot('product-card.png');
```

ครั้งแรกที่รัน Playwright จะสร้าง baseline screenshot ไว้ใน folder ชื่อ `[testfile]-snapshots/` การรันครั้งถัดไปจะเปรียบเทียบกับ baseline นั้น ถ้า pixel ต่างกันเกิน threshold ที่กำหนด test จะ fail พร้อม diff image ให้ดู

**Options ที่ใช้บ่อย:**

```typescript
await expect(page).toHaveScreenshot('dashboard.png', {
  maxDiffPixels: 100,       // ยอมรับความต่างได้ไม่เกิน 100 pixels
  animations: 'disabled',  // หยุด CSS animations ระหว่าง screenshot
  mask: [page.locator('.timestamp')],  // mask element ที่เปลี่ยนบ่อย
  stylePath: './screenshot.css',       // inject CSS เพื่อซ่อน dynamic content
});
```

ตั้งค่า global ใน `playwright.config.ts` เพื่อไม่ต้องระบุทุก test:

```typescript
// playwright.config.ts
export default defineConfig({
  expect: {
    toHaveScreenshot: {
      maxDiffPixels: 100,
      animations: 'disabled',
    },
  },
});
```

**อัปเดต baseline:**

```bash
npx playwright test --update-snapshots
```

### 4.2 Platform Gotcha — เรื่องสำคัญที่สุดใน Visual Testing

นี่คือเรื่องที่คนเพิ่งเริ่มใช้ Visual Testing พลาดบ่อยที่สุด:

> "Browser rendering can vary based on the host OS, version, settings, hardware, power source (battery vs. power adapter), headless mode, and other factors. For consistent screenshots, run tests in the same environment where the baseline screenshots were generated."
> *(source: https://playwright.dev/docs/test-snapshots)*

ชื่อไฟล์ baseline จะมี platform ติดมาด้วย เช่น:

```
dashboard-chromium-darwin.png    ← macOS
dashboard-chromium-linux.png     ← Linux
dashboard-chromium-win32.png     ← Windows
```

ถ้าสร้าง baseline บน macOS แล้วรัน test ใน CI (Linux) — test จะ fail ทันที เพราะ font rendering และ anti-aliasing ต่างกัน

**วิธีแก้สำหรับ CI:**

```yaml
# .github/workflows/playwright.yml
jobs:
  test:
    runs-on: ubuntu-latest
    container:
      image: mcr.microsoft.com/playwright:v1.50.0-jammy
```

หรือสร้าง baseline ใน Docker environment เดียวกับ CI:

```bash
# สร้าง baseline บน Linux (ใช้ Docker locally)
docker run --rm -v $(pwd):/work -w /work \
  mcr.microsoft.com/playwright:v1.50.0-jammy \
  npx playwright test --update-snapshots --project=chromium
```

Baseline สำหรับ CI ควร commit เข้า git ร่วมกับ code เพื่อให้ทุก environment ใช้ตัวเดียวกัน

### 4.3 Accessibility Testing ด้วย `@axe-core/playwright`

Axe เป็น accessibility testing engine ที่ใช้กันแพร่หลายที่สุด — มี integration กับ Playwright โดยตรง

**ติดตั้ง:**

```bash
npm install @axe-core/playwright
```

**Import:**

```typescript
import AxeBuilder from '@axe-core/playwright';
```

**Scan พื้นฐาน:**

```typescript
import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

test('should not have any automatically detectable accessibility issues', async ({ page }) => {
  await page.goto('http://localhost:3000');

  const results = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa'])
    .analyze();

  expect(results.violations).toEqual([]);
});
```

`results.violations` เป็น array ของ issue ที่พบ ถ้า empty คือผ่านทั้งหมด

**WCAG Tags ที่ใช้ได้:**
- `wcag2a` — WCAG 2.0 Level A (พื้นฐาน)
- `wcag2aa` — WCAG 2.0 Level AA (มาตรฐานทั่วไป)
- `wcag21a` — WCAG 2.1 Level A
- `wcag21aa` — WCAG 2.1 Level AA

**Scan เฉพาะ component:**

```typescript
// scan เฉพาะ login form — ไม่สนใจ nav หรือ footer
const results = await new AxeBuilder({ page })
  .include('[data-testid="login-form"]')
  .withTags(['wcag2a', 'wcag2aa'])
  .analyze();
```

**Exclude content ที่รู้ว่ามีปัญหา (แต่ยังไม่ fix):**

```typescript
// exclude third-party widget ที่ควบคุมไม่ได้
const results = await new AxeBuilder({ page })
  .exclude('#third-party-chat-widget')
  .withTags(['wcag2a', 'wcag2aa'])
  .analyze();
```

> "you can use `AxeBuilder.exclude()` to exclude them from being scanned until you're able to fix the issues"
> *(source: https://playwright.dev/docs/accessibility-testing)*

**Common WCAG violations ที่พบบ่อย:**

| Violation | สาเหตุ | วิธีแก้ |
|-----------|--------|--------|
| Image missing alt | `<img>` ไม่มี `alt` attribute | เพิ่ม `alt="description"` หรือ `alt=""` ถ้าเป็น decorative |
| Color contrast | text/background contrast < 4.5:1 | ปรับสีให้ contrast ≥ 4.5:1 (WCAG AA) |
| Form input missing label | `<input>` ไม่มี `<label>` หรือ `aria-label` | เพิ่ม `<label for="...">` หรือ `aria-label` |
| Interactive element no accessible name | `<button>` ไม่มี text หรือ `aria-label` | เพิ่ม text content หรือ `aria-label` |
| Missing focus indicator | กด Tab แล้วไม่เห็น focus outline | อย่า remove CSS `:focus` outline |

### 4.4 `toMatchAriaSnapshot()` — Deep Dive

`toMatchAriaSnapshot()` เป็น Playwright built-in API (v1.49+) ที่ตรวจ accessibility tree ของ page แทนที่จะเปรียบเทียบ pixel — ข้อดีคือไม่ขึ้นกับ OS เลย

**Accessibility tree คืออะไร?** เป็น tree structure ที่ browser สร้างขึ้นจาก HTML เพื่อให้ screen readers และ assistive technologies ใช้งาน — แต่ละ node มี role, name, และ state ตาม ARIA specification

**Syntax:**

```typescript
// ตรวจ navigation bar ว่ามี links ครบถ้วน
await expect(page.getByRole('navigation')).toMatchAriaSnapshot(`
  - navigation:
    - link "Home"
    - link "Shop"
    - link "Todos"
`);

// ตรวจ todo list
await expect(page.locator('[data-testid="todo-list"]')).toMatchAriaSnapshot(`
  - list:
    - listitem: Buy groceries
    - listitem: Walk the dog
`);

// ตรวจ form พร้อม state
await expect(page.getByRole('form')).toMatchAriaSnapshot(`
  - form:
    - textbox "Email"
    - textbox "Password"
    - button "Login"
`);
```

Format ของ snapshot เป็น YAML-like — แต่ละบรรทัดคือ `- role "name"` หรือ `- role: text` ตามโครงสร้างจริงของ accessibility tree

**Inline vs File snapshots:**

```typescript
// Inline (บันทึกใน code โดยตรง)
await expect(locator).toMatchAriaSnapshot(`
  - navigation:
    - link "Home"
`);

// File (บันทึกเป็นไฟล์ .aria.yml แยก)
await expect(locator).toMatchAriaSnapshot({ name: 'navigation.aria.yml' });
```

**อัปเดต aria snapshot:**

```bash
npx playwright test --update-snapshots
# หรือ shorthand
npx playwright test -u
```

> "Snapshots that did not match. Matching snapshots will not be updated."
> *(source: https://playwright.dev/docs/aria-snapshots)*

**ARIA roles ที่ใช้บ่อย:**

| Role | HTML element | หมายเหตุ |
|------|-------------|----------|
| `navigation` | `<nav>` | — |
| `heading` | `<h1>`-`<h6>` | มี `level` attribute |
| `list` | `<ul>`, `<ol>` | — |
| `listitem` | `<li>` | — |
| `button` | `<button>` | มี `disabled`, `pressed` state |
| `link` | `<a>` | — |
| `textbox` | `<input type="text">` | มี `disabled`, `readonly` state |
| `checkbox` | `<input type="checkbox">` | มี `checked` state |

**ARIA Attributes ที่ใช้บ่อย:**

```typescript
await expect(page.getByRole('button')).toMatchAriaSnapshot(`
  - button "Submit" [disabled]
`);

await expect(page.getByRole('checkbox')).toMatchAriaSnapshot(`
  - checkbox "Subscribe to newsletter" [checked]
`);
```

### 4.5 เปรียบเทียบ Visual Screenshot vs Aria Snapshot

| | `toHaveScreenshot()` | `toMatchAriaSnapshot()` |
|-|---------------------|------------------------|
| ตรวจ | Visual pixels | Accessibility tree |
| Platform-specific | ใช่ — OS ต่างกัน = baseline ต่างกัน | ไม่ใช่ — ทำงานเหมือนกันทุก OS |
| ตรวจ hidden content | ไม่ได้ | ได้ (บางส่วน ขึ้นกับ role) |
| Flaky เพราะ animation | ได้ถ้าไม่ใช้ `animations: 'disabled'` | ไม่มีปัญหา |
| Version required | ไม่ระบุ (built-in มาตั้งแต่ต้น) | v1.49+ |
| อัปเดต snapshot | `--update-snapshots` | `--update-snapshots` |
| ใช้ทดสอบอะไร | Visual design, layout, style | Screen reader compat, ARIA structure |

ทั้งสองอย่างเสริมกัน — ใช้ `toHaveScreenshot()` เพื่อตรวจ visual regression และ `toMatchAriaSnapshot()` เพื่อตรวจ accessibility structure

### 4.6 เปรียบเทียบกับ Robot Framework + Selenium

| | Robot Framework + Selenium | Playwright |
|-|--------------------------|------------|
| Visual regression | ไม่มี built-in — ต้องใช้ Applitools, Percy (มีค่าใช้จ่าย) | `toHaveScreenshot()` built-in ฟรี |
| Accessibility scan | ต้องติดตั้ง axe-selenium แยก และ integrate เอง | `@axe-core/playwright` ติดตั้ง 1 command, API ออกแบบมา integrate ใน test |
| Aria tree testing | ไม่มี native support | `toMatchAriaSnapshot()` built-in v1.49+ |
| Update baseline | อัปเดต manual ทุกไฟล์ | `--update-snapshots` flag จัดการให้ |
| CI integration | ต้องเซ็ตค่า env แยกต่างหาก | ใช้ Docker image `mcr.microsoft.com/playwright:vX.X-jammy` |

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner — Visual Screenshot + Aria Snapshot พื้นฐาน

สถานการณ์: ทดสอบว่า homepage ดูถูกต้อง และ navigation accessible

```typescript
// tested: Playwright v1.50+, Node.js 20+
// tests/visual-basic.spec.ts

import { test, expect } from '@playwright/test';

test.describe('Visual + Aria: Homepage', () => {
  test('homepage matches visual snapshot', async ({ page }) => {
    await page.goto('http://localhost:3000');

    // หยุด animation เพื่อให้ screenshot stable
    await page.emulateMedia({ reducedMotion: 'reduce' });

    await expect(page).toHaveScreenshot('homepage.png', {
      animations: 'disabled',
      maxDiffPixels: 50,
    });
  });

  test('product card matches visual snapshot', async ({ page }) => {
    await page.goto('http://localhost:3000/shop');

    // รอให้ products โหลดก่อน
    await page.waitForSelector('[data-testid^="product-card-"]');

    // screenshot เฉพาะ product card แรก
    const firstCard = page.locator('[data-testid^="product-card-"]').first();
    await expect(firstCard).toHaveScreenshot('product-card.png', {
      animations: 'disabled',
    });
  });

  test('navigation has correct aria structure', async ({ page }) => {
    await page.goto('http://localhost:3000');

    await expect(page.getByRole('navigation')).toMatchAriaSnapshot(`
      - navigation:
        - link "Home"
        - link "Shop"
        - link "Todos"
    `);
  });
});
```

**Expected output (first run):** ไม่มี baseline → Playwright สร้าง snapshot ให้อัตโนมัติ, test pass

**Expected output (subsequent runs):** เปรียบเทียบกับ baseline ที่บันทึกไว้ — pass ถ้าไม่มีการเปลี่ยนแปลง

---

### Intermediate — Dark Mode Visual Test + Accessible Form Verification

สถานการณ์: ทีม design เพิ่ม dark mode feature — ต้องตรวจว่า shop page ดูถูกต้องใน dark mode และ login form ยัง accessible อยู่

```typescript
// tested: Playwright v1.50+, Node.js 20+
// tests/visual-dark-mode.spec.ts

import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

test.describe('Dark Mode Visual + Form Accessibility', () => {
  test('shop page matches snapshot in dark mode', async ({ page }) => {
    // เปิด dark mode ก่อน navigate
    await page.emulateMedia({ colorScheme: 'dark' });
    await page.goto('http://localhost:3000/shop');

    // รอ products load
    await page.waitForSelector('[data-testid^="product-card-"]');

    await expect(page).toHaveScreenshot('shop-dark.png', {
      animations: 'disabled',
      maxDiffPixels: 100,
    });
  });

  test('shop page matches snapshot in light mode', async ({ page }) => {
    await page.emulateMedia({ colorScheme: 'light' });
    await page.goto('http://localhost:3000/shop');
    await page.waitForSelector('[data-testid^="product-card-"]');

    await expect(page).toHaveScreenshot('shop-light.png', {
      animations: 'disabled',
      maxDiffPixels: 100,
    });
  });

  test('login form is accessible and has correct aria structure', async ({ page }) => {
    await page.goto('http://localhost:3000');

    // ─── Accessibility scan เฉพาะ login form ───
    const results = await new AxeBuilder({ page })
      .include('[data-testid="login-form"]')
      .withTags(['wcag2a', 'wcag2aa'])
      .analyze();

    expect(results.violations).toEqual([]);

    // ─── ตรวจ Aria structure ───
    await expect(page.locator('[data-testid="login-form"]')).toMatchAriaSnapshot(`
      - form:
        - textbox "Username"
        - textbox "Password"
        - button "Login"
    `);
  });
});
```

**Expected output:**
- `shop-dark.png` และ `shop-light.png` สร้าง baseline แยกกัน
- Accessibility scan บน login form ผ่านทุก WCAG 2.0 AA rule
- Aria snapshot verify ว่า form มี textbox สำหรับ username/password และปุ่ม login

---

### Advanced — Full Accessibility Audit + Selective Snapshot Strategy

สถานการณ์: ก่อน deploy version ใหม่ต้องทำ accessibility audit เต็มรูปแบบ — scan ทุก page ด้วย axe, handle known violations ที่ยังแก้ไม่ได้, ตรวจ aria structure ของ dynamic content ที่เปลี่ยนตาม state, และตรวจ visual regression เฉพาะ critical components

```typescript
// tested: Playwright v1.50+, Node.js 20+
// tests/accessibility-audit.spec.ts

import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

// Helper: format violations เพื่อ error message ที่อ่านง่าย
function summarizeViolations(violations: { id: string; description: string; nodes: unknown[] }[]) {
  return violations.map(v => ({
    rule: v.id,
    description: v.description,
    affectedNodes: v.nodes.length,
  }));
}

test.describe('Accessibility Audit: Full Coverage', () => {
  test('homepage has no WCAG 2.1 AA violations', async ({ page }) => {
    await page.goto('http://localhost:3000');

    const results = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'])
      // exclude third-party analytics widget ที่ควบคุมไม่ได้
      .exclude('[data-testid="analytics-widget"]')
      .analyze();

    expect(results.violations, `Found ${results.violations.length} violations:\n${
      JSON.stringify(summarizeViolations(results.violations), null, 2)
    }`).toHaveLength(0);
  });

  test('shop page: scan with known violations excluded, aria verify product list', async ({ page }) => {
    await page.goto('http://localhost:3000/shop');
    await page.waitForSelector('[data-testid^="product-card-"]');

    // ─── Accessibility scan ───
    const results = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa'])
      // product image alt text — filed as bug, not fixed yet
      .disableRules(['image-alt'])
      .analyze();

    expect(results.violations, `Unexpected violations:\n${
      JSON.stringify(summarizeViolations(results.violations), null, 2)
    }`).toHaveLength(0);

    // ─── Aria snapshot: ตรวจโครงสร้าง product list ───
    // ใช้ first() เพื่อ snapshot แค่ card แรก ไม่ขึ้นกับจำนวน products
    const firstCard = page.locator('[data-testid^="product-card-"]').first();
    await expect(firstCard).toMatchAriaSnapshot(`
      - article:
        - img "Product image"
        - heading "T-Shirt"
        - text: /\$[\d.]+/
        - button "Add to Cart"
    `);
  });

  test('todo list: aria snapshot reflects completed state correctly', async ({ page, request }) => {
    // ─── Setup ผ่าน API ─── (hybrid approach จากบทที่ 15)
    await request.post('http://localhost:3000/api/reset');

    const res1 = await request.post('http://localhost:3000/api/todos', {
      data: { text: 'Write tests' }
    });
    const todo1 = await res1.json();

    const res2 = await request.post('http://localhost:3000/api/todos', {
      data: { text: 'Deploy to staging' }
    });
    const todo2 = await res2.json();

    // mark todo1 เป็น completed
    await request.patch(`http://localhost:3000/api/todos/${todo1.id}`, {
      data: { completed: true }
    });

    // ─── Verify ผ่าน Aria snapshot ───
    await page.goto('http://localhost:3000/todos');
    await page.waitForSelector('[data-testid^="todo-item-"]');

    // verify todo list structure — completed item ควรมี checked state
    await expect(page.locator('[data-testid="todo-list"]')).toMatchAriaSnapshot(`
      - list:
        - listitem:
          - checkbox "Write tests" [checked]
        - listitem:
          - checkbox "Deploy to staging"
    `);

    // ─── Visual snapshot: ตรวจ visual state ของ completed todo ───
    // completed todo ควรมี strikethrough style
    const completedItem = page.locator('[data-testid^="todo-item-"]').first();
    await expect(completedItem).toHaveScreenshot('todo-completed.png', {
      animations: 'disabled',
    });

    // ─── Accessibility scan หลัง state เปลี่ยน ───
    const results = await new AxeBuilder({ page })
      .include('[data-testid="todo-list"]')
      .withTags(['wcag2a', 'wcag2aa'])
      .analyze();

    expect(results.violations).toEqual([]);
  });

  test('compare: visual diff catches style regression that functional test misses', async ({ page }) => {
    await page.goto('http://localhost:3000/shop');
    await page.waitForSelector('[data-testid^="btn-add-cart-"]');

    // ─── Visual snapshot: จับการเปลี่ยนแปลงสี/style ───
    const addBtn = page.locator('[data-testid^="btn-add-cart-"]').first();
    await expect(addBtn).toHaveScreenshot('btn-add-cart.png', {
      animations: 'disabled',
      maxDiffPixels: 10,  // strict มากขึ้นสำหรับ critical UI element
    });

    // ─── Functional test ยังทำงานต่อได้แม้ visual test fail ───
    await addBtn.click();
    await expect(page.locator('[data-testid="cart-count"]')).toHaveText('1');
  });
});
```

**Key design decisions:**
- `summarizeViolations()` helper ทำให้ error message อ่านง่ายเมื่อ test fail
- `.disableRules(['image-alt'])` ระบุ rule ที่รู้ว่ามีปัญหาแต่ยังไม่ fix ให้ชัดเจน
- ใช้ hybrid approach setup ผ่าน API ก่อน verify aria snapshot — เร็วกว่า fill form ทาง UI
- `maxDiffPixels: 10` สำหรับ critical element ที่ต้องการความแม่นยำสูง

---

## 6. Common Mistakes

❌ **สร้าง baseline บน macOS แล้วรัน test ใน Linux CI**

```bash
# ผิด — baseline สร้างบน macOS developer machine
npx playwright test --update-snapshots
# แล้ว commit: dashboard-chromium-darwin.png

# CI รันบน Linux → ได้ dashboard-chromium-linux.png
# เปรียบเทียบกัน → fail ทันที เพราะ font rendering ต่างกัน
```

```bash
# ✅ ถูก — สร้าง baseline ใน Docker environment เดียวกับ CI
docker run --rm -v $(pwd):/work -w /work \
  mcr.microsoft.com/playwright:v1.50.0-jammy \
  npx playwright test --update-snapshots
# commit: dashboard-chromium-linux.png
```

Screenshots ต่างกันระหว่าง OS เพราะ font rendering, anti-aliasing, และ sub-pixel rendering ต่างกัน — *(source: https://playwright.dev/docs/test-snapshots)*

---

❌ **ไม่ใช้ `animations: 'disabled'` ก่อน screenshot**

```typescript
// ผิด — ถ้า page มี CSS animation เช่น loading spinner, fade-in
// screenshot อาจได้ frame ที่ animation ยังไม่จบ → flaky test
await expect(page).toHaveScreenshot('dashboard.png');
```

```typescript
// ✅ ถูก — disable animations + รอ content load ก่อนเสมอ
await page.waitForSelector('[data-testid="main-content"]');
await expect(page).toHaveScreenshot('dashboard.png', {
  animations: 'disabled',
});
```

CSS animations เช่น loading spinner, fade-in, slide-in อาจทำให้ screenshot ได้ frame ที่ต่างกันในแต่ละ run — *(source: https://playwright.dev/docs/test-snapshots)*

---

❌ **อัปเดต baseline โดยไม่ review ความเปลี่ยนแปลงก่อน**

```bash
# ผิด — รัน update-snapshots แล้ว commit ทันทีโดยไม่ดู diff
npx playwright test --update-snapshots
git add . && git commit -m "update snapshots"
```

```bash
# ✅ ถูก — ดู diff ก่อนเสมอ แล้ว approve เฉพาะที่ตั้งใจเปลี่ยน
npx playwright test --update-snapshots
# ตรวจสอบ snapshot diff ใน HTML report ก่อน
npx playwright show-report
# approve เฉพาะ snapshot ที่เปลี่ยนตั้งใจ
git add tests/visual.spec.ts-snapshots/dashboard-chromium-linux.png
git commit -m "update: dashboard snapshot after new header design"
```

`--update-snapshots` อัปเดตทุก snapshot ที่ไม่ match — รวมถึง regression ที่เกิดจาก bug โดยไม่ตั้งใจ *(source: https://playwright.dev/docs/test-snapshots)*

---

❌ **Scan accessibility ทั้ง page โดยไม่ exclude dynamic/third-party content**

```typescript
// ผิด — scan ทั้ง page รวม third-party widgets
const results = await new AxeBuilder({ page }).analyze();
// อาจ fail เพราะ Intercom chat widget, Google Analytics, หรือ external content
// ที่คุณแก้ไขไม่ได้
expect(results.violations).toEqual([]);
```

```typescript
// ✅ ถูก — scope scan เฉพาะ content ที่ควบคุมได้
const results = await new AxeBuilder({ page })
  .include('[data-testid="app-content"]')
  .exclude('#third-party-widget')
  .withTags(['wcag2a', 'wcag2aa'])
  .analyze();
expect(results.violations).toEqual([]);
```

> "you can use `AxeBuilder.exclude()` to exclude them from being scanned until you're able to fix the issues"
> *(source: https://playwright.dev/docs/accessibility-testing)*

---

❌ **ลืม install `@axe-core/playwright` แล้ว import แบบผิด**

```typescript
// ผิด — ลืม npm install หรือ import path ผิด
import { AxeBuilder } from '@axe-core/playwright'; // named import ผิด
import AxeBuilder from 'axe-playwright'; // package ชื่อผิด
```

```typescript
// ✅ ถูก — default import จาก package ที่ถูกต้อง
// หลังจาก: npm install @axe-core/playwright
import AxeBuilder from '@axe-core/playwright';
```

> "The following examples rely on the `@axe-core/playwright` package."
> *(source: https://playwright.dev/docs/accessibility-testing)*

---

## 7. สรุปบท

ก่อนดูเฉลย ลองตอบ 3 คำถามนี้ด้วยตัวเองก่อน:

**คำถาม 1**: Developer push code ที่เปลี่ยนสี primary button จาก `#2563EB` เป็น `#2564EB` — functional test ทั้งหมดผ่าน คุณจะตรวจจับ regression นี้ได้อย่างไร? และถ้า test นั้นรันบน CI ที่เป็น Linux แต่ baseline สร้างบน macOS — จะเกิดอะไรขึ้น?

**คำถาม 2**: ทีม QA รายงานว่า screen reader ไม่อ่าน "Add to Cart" button ในหน้า shop — คุณจะเขียน test อะไรเพื่อ reproduce และ verify fix? ใช้ tool ไหนและ API อะไร?

**คำถาม 3**: `toHaveScreenshot()` และ `toMatchAriaSnapshot()` ต่างกันอย่างไรในแง่ (a) platform dependency, (b) สิ่งที่ตรวจ, (c) กรณีที่ควรใช้แต่ละอย่าง?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย:**

**คำถาม 1**: เพิ่ม `toHaveScreenshot()` เพื่อตรวจ visual regression — screenshot จะ fail เพราะ pixel สีต่างกัน เพราะ functional test ตรวจแค่ behavior ไม่ใช่ visual appearance ส่วนถ้า baseline สร้างบน macOS (`-darwin.png`) แต่ CI รันบน Linux (`-linux.png`) test จะ fail ทันทีเพราะ font rendering ต่างกัน — ต้องสร้าง baseline บน environment เดียวกับ CI หรือใช้ Docker image `mcr.microsoft.com/playwright:v1.50.0-jammy`

**คำถาม 2**: ใช้ทั้งสอง approach: (1) `toMatchAriaSnapshot()` เพื่อตรวจว่า button มี accessible name — `await expect(page.locator('[data-testid^="btn-add-cart-"]').first()).toMatchAriaSnapshot('- button "Add to Cart"')` ถ้า name หาย snapshot จะ fail และ (2) `AxeBuilder` เพื่อ scan WCAG compliance รวมถึง `button-name` rule — `new AxeBuilder({ page }).include('[data-testid^="product-card-"]').analyze()` แล้ว `expect(results.violations).toEqual([])`

**คำถาม 3**: (a) Platform dependency: `toHaveScreenshot()` ขึ้นกับ OS — snapshot ต่างกันระหว่าง macOS/Linux/Windows, `toMatchAriaSnapshot()` ไม่ขึ้นกับ OS เลย (b) สิ่งที่ตรวจ: screenshot ตรวจ visual pixels (สี, layout, font rendering), aria snapshot ตรวจ accessibility tree structure (roles, names, states) (c) ใช้ screenshot เมื่อ design/visual regression สำคัญ เช่น ตรวจว่า dark mode ถูกต้อง, ใช้ aria snapshot เมื่อต้องการ verify accessibility structure ที่ cross-platform เช่น ตรวจว่า navigation มี links ครบ หรือ form มี labels ถูกต้อง

</details>
