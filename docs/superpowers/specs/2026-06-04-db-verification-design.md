# Design Spec: Database State Verification Chapter + Expanded Exercises

**Date:** 2026-06-04  
**Course:** Playwright TypeScript Master Course (Thai)  
**Status:** Approved

---

## 1. Problem Statement

The current course (Ch01–Ch18) teaches API testing and hybrid UI+API patterns in Ch15, but never teaches students to verify that data was actually **persisted to the database** after an action. This is a critical gap: a test can "pass" at the UI layer while the database contains wrong or missing data. Professional QA automation requires closing this loop.

Additionally, the current exercises.md uses only 3 levels per chapter (Recall / Application / Synthesis). To produce Playwright masters, key chapters need 5 difficulty levels that progress from basic recall to production-level architecture.

---

## 2. Deliverables

### 2.1 New File: `docs/playwright-typescript/19-db-verification.md`

Full chapter following the mandatory 8-section structure from CLAUDE.md.

**Chapter title:** บทที่ 19: Database State Verification — ปิด Loop ด้วยการตรวจ DB

**Six patterns taught (ordered basic → production-grade):**

| # | Pattern | Description | Demo App endpoint |
|---|---------|-------------|-------------------|
| 1 | **API Read-back** | After UI action, call GET API to confirm record exists with correct values | `GET /api/todos`, `GET /api/admin` |
| 2 | **Direct File Read** | Read `db.json` via Node.js `fs.readFileSync` for file-based DBs | `data/db.json` |
| 3 | **Cross-layer Verification** | UI creates → API confirms DB state → UI re-renders → confirm UI reflects DB | todos + orders flow |
| 4 | **Negative Verification** | After DELETE action, verify record is gone from DB (not just from UI) | `DELETE /api/todos/:id` |
| 5 | **Async DB Polling** | Use `expect.poll()` to handle async jobs that write to DB with delay | bulk operations pattern |
| 6 | **DB Isolation Pattern** | Prevent test contamination: per-test setup via API + `POST /api/reset` teardown | todos reset endpoint |

**Accuracy protocol (mandatory before writing):**
- WebFetch `https://playwright.dev/docs/api-testing`
- WebFetch `https://playwright.dev/docs/test-api-testing`
- WebFetch `https://playwright.dev/docs/api/class-apirequestcontext` for exact method signatures
- WebFetch `https://playwright.dev/docs/api/class-pollable` for `expect.poll()` exact API
- Record all QUOTES in `docs/playwright-typescript-source-notes.md` before writing

**Section structure:**
1. **Pre-chapter Retrieval** — 2 questions from Ch18 (before all other content)
2. **วัตถุประสงค์** — 6+ measurable outcomes
3. **ทำไมต้องรู้? (Why)** — Start with the problem: test passes but data is wrong in DB
4. **Analogy** — Specific + mechanism coverage + ⚠️ breakdown points (2-3 misconceptions)
5. **เนื้อหาหลัก** — All 6 patterns with RF/Selenium comparison table
6. **ตัวอย่าง 3 ระดับ** — Beginner (single API read-back), Intermediate (cross-layer verification with orders), Advanced (full DB isolation fixture with cleanup)
7. **Common Mistakes** — ≥3 items with `*(source: URL)*` per item, verified from docs
8. **สรุปบท** — 3-4 bullets + 2-3 Retrieval Questions (answer-before-see-answer format)

**Code standards:**
- All examples use `http://localhost:3000` demo app
- Every code block: `// tested: Playwright v1.50+, Node.js 20+`
- Complete, runnable — no partial snippets
- Show actual output where relevant

**Target length:** 750–900 lines (topic is denser than average chapter)

---

### 2.2 Updated File: `docs/playwright-typescript/exercises.md`

#### 2.2.1 New Exercise Block: Ch19 (5 levels)

Five levels defined as follows:

| Level | Name | Cognitive task |
|-------|------|----------------|
| L1 | **Recall** | อธิบาย concept ด้วยคำตัวเอง, ตอบ "คืออะไร" และ "ทำไม" |
| L2 | **Recognition** | อ่าน code ที่ให้มา แล้วระบุว่าถูก/ผิดอย่างไร และเพราะอะไร |
| L3 | **Guided Application** | เขียน test ตาม scenario ที่กำหนดชัดเจน (มี skeleton หรือ constraints ให้) |
| L4 | **Independent Application** | สถานการณ์ใหม่ที่ไม่มีใน chapter — ออกแบบ approach เองทั้งหมด |
| L5 | **Expert / Synthesis** | Debug complex scenario, design production-grade fixture, evaluate architecture trade-offs |

**Exercise topics for Ch19 (5 levels):**
- L1: อธิบาย API read-back pattern vs ดู UI อย่างเดียว
- L2: อ่าน code ที่ verify DB แบบผิด (ไม่รอ async, ไม่ cleanup) แล้วระบุปัญหา
- L3: เขียน test ที่ create todo ผ่าน UI แล้ว verify ผ่าน GET /api/todos
- L4: ออกแบบ test suite สำหรับ e-commerce order flow ที่ต้องการ DB verification ทุก step
- L5: วิเคราะห์ข้อ trade-off ระหว่าง API read-back vs direct file read vs UI-only verification ใน production context ที่มี 500+ tests

#### 2.2.2 Expert Level (L5) for 4 Existing Chapters

เพิ่ม L5 exercise ให้ 4 บทที่ production-grade ที่สุด:

**Ch13 (Authentication & Storage State) — L5 Expert:**
Topic: ออกแบบ multi-role authentication system สำหรับ test suite ขนาดใหญ่ที่ต้องการ DB verification ว่า session state ตรงกับ database record จริง

**Ch15 (API Testing + Hybrid) — L5 Expert:**
Topic: วิเคราะห์ failure ของ hybrid test ที่ซับซ้อน: UI สร้าง order → API verify → แต่ DB count ไม่ตรง — diagnose และแก้ไข

**Ch17 (CI/CD Integration) — L5 Expert:**
Topic: ออกแบบ CI pipeline ที่ handle DB state ระหว่าง parallel shards โดยไม่มี test contamination

**Ch18 (Production Patterns) — L5 Expert:**
Topic: บริษัทมี test suite 300 tests ที่ใช้ account เดียวกัน — วิเคราะห์ปัญหา DB contamination ทั้งหมดและออกแบบ solution ระดับ production

---

### 2.3 Updated Files (minor)

**`HANDOFF.md`:** เพิ่ม Ch19 ในตาราง status

**`00-overview.md`:** เพิ่ม Ch19 ใน table of contents

---

## 3. Accuracy Requirements

- **No content written from memory without verification** — every API method name, parameter, behavior must be quoted from `playwright.dev` in source-notes first
- **All code examples tested** against the demo app before inclusion (รัน `cd docs/playwright-typescript/playwright-course-app && npm start` แล้วรัน code จริง)
- **Common Mistakes section** — every item must have `*(source: URL)*` — not from training data
- **expect.poll() API** — verify exact signature before using (added in which version?)
- **Node.js fs in tests** — verify this is a valid Playwright pattern (not an anti-pattern)
- **Direct file read** — note clearly when this is appropriate vs. API read-back

---

## 4. Exercise Quality Rules (all levels)

- ห้าม fill-in-the-blank ทุก level
- ห้าม copy scenario/numbers จากตัวอย่างในบท
- L3+ ต้องใช้ demo app จริง (`http://localhost:3000`)
- L5 ต้องมี production context (scale, CI, team size) เสมอ
- ทุก level ต้องมีเฉลยครบถ้วนในรูปแบบ `<details><summary>เฉลย</summary>...</details>`

---

## 5. Files Affected

| File | Action | Description |
|------|--------|-------------|
| `docs/playwright-typescript/19-db-verification.md` | CREATE | New chapter, ~750-900 lines |
| `docs/playwright-typescript/exercises.md` | EDIT | Add Ch19 (5 levels) + L5 for Ch13/15/17/18 |
| `docs/playwright-typescript/HANDOFF.md` | EDIT | Add Ch19 to status table |
| `docs/playwright-typescript/00-overview.md` | EDIT | Add Ch19 to TOC |
| `docs/playwright-typescript-source-notes.md` | EDIT | Append Ch19 quotes from playwright.dev |

---

## 6. Implementation Order

1. WebFetch all 4 playwright.dev URLs → record quotes in source-notes
2. Write `19-db-verification.md` (draft complete chapter)
3. Test all code examples against running demo app
4. Write Ch19 exercises (5 levels) in exercises.md
5. Write L5 Expert exercises for Ch13, Ch15, Ch17, Ch18 in exercises.md
6. Update HANDOFF.md + 00-overview.md
7. Commit all changes

---

## 7. Success Criteria

- Ch19 passes Quality Review score ≥ 95/100
- All 6 DB verification patterns have runnable, tested code examples
- Ch19 exercises span L1 (can answer after 5 min reading) to L5 (requires 30+ min design thinking)
- Expert exercises for Ch13/15/17/18 are distinct from existing exercises (no scenario overlap)
- No content in Ch19 is written from training data without a source-notes QUOTE
