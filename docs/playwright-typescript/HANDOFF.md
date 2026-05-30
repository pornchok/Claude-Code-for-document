# Playwright Course — Session Handoff Document

> **สำหรับ Claude session ใหม่:** อ่านไฟล์นี้ก่อนเริ่มเขียนทุกครั้ง

---

## สถานะปัจจุบัน

| บท | ไฟล์ | สถานะ |
|----|------|-------|
| Overview | 00-overview.md | ✅ เสร็จ |
| Ch01 | 01-why-playwright.md | ✅ เสร็จ (440 บรรทัด) |
| Ch02 | 02-setup-typescript.md | ✅ เสร็จ (460 บรรทัด) |
| Ch03 | 03-architecture.md | ⏳ ยังไม่ได้เขียน |
| Ch04 | 04-locators.md | ⏳ ยังไม่ได้เขียน |
| Ch05 | 05-actions-assertions.md | ⏳ ยังไม่ได้เขียน |
| Ch06 | 06-debugging.md | ⏳ ยังไม่ได้เขียน |
| Ch07 | 07-fixtures.md | ⏳ ยังไม่ได้เขียน |
| Ch08 | 08-page-object-model.md | ⏳ ยังไม่ได้เขียน |
| Ch09 | 09-test-organization.md | ⏳ ยังไม่ได้เขียน |
| Ch10 | 10-configuration-projects.md | ⏳ ยังไม่ได้เขียน |
| Ch11 | 11-parallelism-sharding-reporting.md | ⏳ ยังไม่ได้เขียน |
| Ch12 | 12-mocking-network-time-browser.md | ⏳ ยังไม่ได้เขียน |
| Ch13 | 13-authentication-storage-state.md | ⏳ ยังไม่ได้เขียน |
| Ch14 | 14-advanced-browser-emulation.md | ⏳ ยังไม่ได้เขียน |
| Ch15 | 15-api-testing-hybrid.md | ⏳ ยังไม่ได้เขียน |
| Ch16 | 16-visual-accessibility-testing.md | ⏳ ยังไม่ได้เขียน |
| Ch17 | 17-cicd.md | ⏳ ยังไม่ได้เขียน |
| Ch18 | 18-production-patterns.md | ⏳ ยังไม่ได้เขียน |
| Exercises | exercises.md | ⏳ ยังไม่ได้เขียน |
| Glossary | glossary.md | ⏳ ยังไม่ได้เขียน |

**เริ่มต้นที่:** Ch03 (`03-architecture.md`)

---

## ไฟล์อ้างอิงที่ต้องอ่านก่อน

1. **Plan file** (task-by-task instructions per chapter):
   `docs/superpowers/plans/2026-05-30-playwright-typescript-course-docs.md`

2. **Spec file** (chapter topics, demo app design, quality targets):
   `docs/superpowers/specs/2026-05-30-playwright-typescript-design.md`

3. **Source notes** (APPEND เท่านั้น, บันทึก QUOTES จาก playwright.dev):
   `docs/playwright-typescript-source-notes.md`

4. **ตัวอย่างบทที่ดี** (อ่านเพื่อเข้าใจ format/tone ที่ถูกต้อง):
   `docs/playwright-typescript/01-why-playwright.md`
   `docs/playwright-typescript/02-setup-typescript.md`

---

## โครงสร้างบังคับ 8 Section ทุกบท

```
[Section 8: Pre-chapter Retrieval — อยู่ก่อนทุกอย่าง, ตั้งแต่บท 02 เป็นต้นไป]
## ก่อนอ่านบทนี้ ลองตอบ:
1. [คำถามจากบทก่อน]
---
เฉลย: [คำตอบ]

# ชื่อบท

## วัตถุประสงค์
[bullet list: "หลังอ่านบทนี้คุณจะ..." ≥5 ข้อ]

## ทำไมต้องรู้? (Why)
[อธิบาย PROBLEM ก่อน — ไม่ใช่ "X คืออะไร"]

## Analogy
[เฉพาะเจาะจง + ⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า: ...]

## เนื้อหาหลัก
[verified จาก playwright.dev + RF/Selenium comparison table]

## ตัวอย่าง
### Beginner
### Intermediate (สถานการณ์ใหม่ ห้าม copy จาก section 4)
### Advanced (Synthesis/Diagnosis)

## Common Mistakes
❌ [wrong] → ✅ [correct]
*(source: https://playwright.dev/docs/...)*

## สรุปบท
[3-4 bullet] + Retrieval Questions (ตอบก่อนดูเฉลย)
```

---

## Accuracy Protocol (สำคัญที่สุด)

**ลำดับที่ต้องทำก่อนเขียนทุกบท:**

1. **WebFetch** playwright.dev URLs ที่ระบุใน plan file สำหรับบทนั้น
2. **บันทึก QUOTE จริง** ใน `docs/playwright-typescript-source-notes.md`:
   ```
   ## Ch[N]: [ชื่อบท]
   SOURCE: https://playwright.dev/docs/[page]
   VERSION: Playwright vX.XX.X
   CONCEPT: [ชื่อ concept]
   QUOTE: "[ข้อความตรงๆ จาก docs — copy word for word]"
   ```
3. **เขียน chapter** จาก verified information
4. **ถ้า verify ไม่ได้** → เขียนว่า "ยังไม่ได้ verify" หรือ "อาจ outdated"
5. **Common Mistakes** ทุกข้อต้องมี `*(source: URL)*` — ไม่ใช่จากความจำ

---

## Style ที่ต้องรักษา

- **ภาษา:** Thai หลัก, code/technical terms เป็น English
- **โทน:** สนทนา เหมือนสอนเพื่อน ไม่เป็นทางการ
- **ความยาว:** 400-500+ บรรทัดต่อบท (ดูจาก Ch01=440, Ch02=460)
- **Code blocks:** ต้องมี `// tested: Playwright v1.50+, Node.js 20+`
- **RF/Selenium comparison:** ทุกบทต้องมี table เปรียบเทียบ

---

## Demo App สำหรับ Code Examples

**รัน:** `cd playwright-course-app && npm install && npm start` → http://localhost:3000

**Users:** admin/admin123 (admin role), testuser/test123 (user role)

**Key data-testid ที่ใช้บ่อย:**
```
Session badge: [data-testid="session-badge"] → แสดง "Logged in as: [user]"
Login form: input-username, input-password, btn-login, login-error
Todos: input-new-todo, btn-add-todo, todo-list, todo-count, todo-item-{id}
Shop: filter-category, search-input, product-grid, product-card-{id}
Advanced: btn-open-popup, embedded-iframe, shadow-counter, btn-alert, btn-confirm
```

**API สำหรับ test setup:**
```typescript
// สร้าง test data
await request.post('http://localhost:3000/api/todos', { data: { text: 'My todo' } })
// ล้าง state
await request.post('http://localhost:3000/api/reset')
// Login
const res = await request.post('http://localhost:3000/api/auth/login', {
  data: { username: 'admin', password: 'admin123' }
})
const { token } = await res.json()
```

---

## Items ต้อง Verify ก่อนเขียนบทนั้น

| Item | ใช้ใน | วิธี verify |
|------|-------|------------|
| `testConfig.tsconfig` exact option name | Ch10 | WebFetch https://playwright.dev/docs/test-configuration |
| `--only-changed` flag มีจริงใน v1.51+ | Ch11 | WebFetch playwright changelog |
| `locator.filter({ visible: true })` | Ch04 | WebFetch https://playwright.dev/docs/locators |
| `devices['iPhone 15 Pro']` exact string | Ch14 | WebFetch https://playwright.dev/docs/emulation |
| `failOnFlakyTests` ❌ ไม่มีใน Playwright | Ch11 | ระบุ pattern ที่ถูก: `retries: process.env.CI ? 2 : 0` |

---

## Chapter-Specific Notes

### Ch03 — Architecture
- Analogy: โรงแรม (Browser) → ห้องพัก (BrowserContext) → โต๊ะทำงาน (Page)
- Demo proof: session-badge บน navbar แสดง context isolation ได้
- Code: 2 contexts, admin + testuser login พร้อมกัน, badge แสดงต่างกัน

### Ch04 — Locators
- Priority order จาก official docs: getByRole > getByLabel > getByPlaceholder > getByText > getByAltText > getByTitle > getByTestId > CSS
- ⚠️ Verify `locator.filter({ visible: true })` ก่อน — อาจต้องใช้ `.and(page.locator(':visible'))` แทน
- Demo: product images ใน /shop มี alt="Product: [Name]" → getByAltText()

### Ch05 — Actions & Assertions
- `toMatchAriaSnapshot()` = **intro เท่านั้น** ใน Ch05, deep dive อยู่ใน Ch16
- `test.step()` ไม่อยู่ใน Ch05 — อยู่ใน Ch09 เท่านั้น
- Double-click demo: todos.html มี double-click to edit

### Ch06 — Debugging
- UI Mode (`--ui`) และ Watch mode (`--watch`) ต้องอยู่ในบทนี้
- Codegen: `npx playwright codegen http://localhost:3000`

### Ch07 — Fixtures
- `test.step()` ไม่อยู่ใน Ch07 — อยู่ใน Ch09
- Demo: cleanDb fixture + todoPage fixture ที่ใช้ /api/reset
- `mergeTests()` และ `mergeExpects()` อยู่ที่นี่

### Ch09 — Test Organization
- `test.step()` อยู่ที่นี่ (ไม่ใช่ Ch07)
- Annotations (skip/fail/fixme/slow) ≠ Tags — สอนแยกให้ชัด

### Ch10 — Configuration
- `failOnFlakyTests` ❌ ไม่มีใน Playwright — ใช้ `retries: process.env.CI ? 2 : 0` แทน
- `defineConfig()` ไม่ต้องใส่ generic โดยไม่จำเป็น
- ⚠️ Verify `testConfig.tsconfig` exact name ก่อนเขียน

### Ch11 — Parallelism
- `test.describe.serial()` ถูกต้อง — ไม่ใช่ `test.serial()`
- `merge-reports` command: `npx playwright merge-reports --reporter html ./blob-reports`
- ⚠️ Verify `--only-changed` flag ก่อนเขียน

### Ch12 — Mocking
- Clock methods ที่ถูกต้อง: `clock.install()`, `clock.setFixedTime()`, `clock.fastForward()`, `clock.runFor()`
- ไม่มี method ชื่อ "freeze"
- `page.addInitScript()` = page-scoped vs `context.addInitScript()` = context-scoped

### Ch13 — Auth
- JWT response format: `{ token: "<jwt>" }` (ตรวจ demo app)
- Public todos (/api/todos) ไม่ต้อง auth — สอนได้ตั้งแต่ Ch07
- Protected: /api/me, /api/admin, /api/orders

### Ch14 — Advanced Browser
- ⚠️ Verify `devices['iPhone 15 Pro']` exact string
- `page.emulateMedia()` สอน 2 use cases: `{ media: 'print' }` AND `{ colorScheme: 'dark' }`

### Ch16 — Visual + Accessibility
- `toMatchAriaSnapshot()` อยู่ที่นี่เป็น **deep dive** (Ch05 แค่ mention)
- `@axe-core/playwright` = install แยก: `npm install @axe-core/playwright`

---

## Commit Pattern

ทุกบท commit แยก:
```bash
git add docs/playwright-typescript/[NN]-[name].md docs/playwright-typescript-source-notes.md
git commit -m "docs: ch[NN] [topic name]"
```

---

## คำสั่งสำหรับ session ใหม่

เมื่อเปิด session ใหม่ให้บอก Claude ว่า:
> "ทำต่อจาก Playwright TypeScript course content — เริ่มจาก Ch03 อ่าน HANDOFF.md ก่อน แล้วดู plan ที่ docs/superpowers/plans/2026-05-30-playwright-typescript-course-docs.md ทำทีละบทโดยใช้ subagent-driven development เน้นความถูกต้อง verify ทุก concept กับ playwright.dev"
