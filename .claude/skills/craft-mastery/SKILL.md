---
name: craft-mastery
description: สร้างเอกสารการเรียนรู้ระดับ world-class สำหรับหัวข้อใดก็ได้ — research จาก official docs, verification โดยรัน code จริง, quality review loop จนได้ 100 ทุก dimension, และ learning science ครบถ้วน (retrieval practice, spaced repetition, Generation effect). ใช้เมื่อต้องการเรียนรู้หัวข้อใหม่อย่างลึกซึ้งและได้เอกสารที่จะช่วย retention ระยะยาว. Use when asked to create, write, or make a learning document about any topic.
argument-hint: "[topic] | [level: beginner/intermediate/advanced] | [scope: brief description]"
disable-model-invocation: true
# v2.0 — checkpoint/resume system, adaptive review cap, Agent 2 revision efficiency
---

# สร้างเอกสารการเรียนรู้: $ARGUMENTS

ทำตามทุกขั้นตอนโดยไม่ข้ามแม้แต่ขั้นเดียว

---

## CHECKPOINT SYSTEM — ตรวจก่อนเริ่มทุกครั้ง

**ถ้ามีไฟล์ `docs/[topic-slug]-checkpoint.md` อยู่แล้ว (session ก่อนถูกตัด):**
1. Read ไฟล์นั้น
2. Parse ค่า NEXT: ถ้า NEXT ระบุ STEP ที่รู้จัก (STEP 0–8 หรือชื่อไฟล์บทใน COMPLETED list) → แจ้ง user: "พบ checkpoint จาก session ก่อน — กำลังดำเนินต่อจาก [NEXT]" แล้วข้ามไปทันที
   ถ้า NEXT parse ไม่ได้หรือ STEP ไม่มีอยู่จริง → แจ้ง user: "checkpoint มี NEXT ที่ไม่สามารถ parse ได้: [NEXT value] — จะเริ่มใหม่จาก STEP 0 หรือดำเนินต่อจาก COMPLETED list?" แล้วรอ user เลือก — ถ้า user ไม่ respond → autonomous default: ใช้ COMPLETED list infer step ถัดไป; ถ้า COMPLETED ก็ไม่ชัดเจน → เริ่ม STEP 0 ใหม่
3. ข้ามไปยัง STEP ที่ NEXT ระบุได้เลย — ไม่ต้องทำ STEP ที่ COMPLETED ซ้ำ

**ถ้าไม่มี checkpoint → เริ่ม STEP 0 ตามปกติ**

**Checkpoint format** (บันทึกหลัง STEP 1, STEP 2, แต่ละบทใน STEP 3, STEP 4, STEP 5 เสร็จ):
```
SKILL: craft-mastery v2
TOPIC: [topic]
SLUG: [topic-slug]
LEVEL: [level]
COMPLETED: [STEP list เช่น "STEP 0, STEP 1, STEP 2, 01-intro.md, 02-basics.md"]
CURRENT_STATUS: [สิ่งที่ทำล่าสุด]
NEXT: STEP [N] — [สิ่งที่ต้องทำต่อ เช่น "เขียน 03-advanced.md"]
FILES_WRITTEN: [list ไฟล์ที่สร้างแล้ว]
```
บันทึกด้วย Write tool ไปที่ `docs/[topic-slug]-checkpoint.md` (overwrite ได้)
ลบไฟล์นี้ใน STEP 7 พร้อมกับ source-notes

---

## STEP 0 — กำหนด Scope และ Audience

**ขั้นที่ 1 — Parse $ARGUMENTS ก่อน:**
⚠️ Input validation: ถ้า $ARGUMENTS ว่างเปล่า หรือ parse แล้วไม่พบ topic → หยุดและถาม user: "กรุณาระบุ: [topic] | [level: beginner/intermediate/advanced] | [scope: สิ่งที่ต้องการสอน]" แล้วรอ input ก่อน proceed
กำหนด scope draft จาก $ARGUMENTS:
- หัวข้อ (topic)
- ระดับ: beginner / intermediate / advanced (default ถ้าไม่ระบุ: beginner | synonym mapping: "basic"/"easy"/"เบื้องต้น" → beginner, "mid"/"medium" → intermediate, "pro"/"expert"/"สูง"/"ขั้นสูง" → advanced — map แล้ว confirm กับ user ใน scope gate โดยไม่ต้องถามก่อน | ถ้า map ไม่ได้: ถาม user เลือกจาก beginner / intermediate / advanced)
- ขอบเขต draft: สิ่งที่จะสอน 5-8 อย่าง + สิ่งที่ไม่ครอบคลุม
- กลุ่มเป้าหมาย: รู้อะไรอยู่แล้ว / ยังไม่รู้อะไร
- แนวทางเนื้อหา: เน้น approach ที่ industry ใช้จริง (best practice / popular pattern) — ถ้ามีหลาย approach ที่ทำสิ่งเดียวกันได้ ต้องระบุในเอกสารว่า (a) approach ไหนเป็น best practice และทำไม (b) approach ไหนควรเลี่ยงและเหตุผล — ห้ามสอนหลาย approach โดยไม่ระบุ preference ถ้าไม่มีข้อมูลพอให้ผู้เรียนตัดสินใจเอง
- จำกัด concept ใหม่ไม่เกิน 3-5 รายการต่อบท (concept ที่ซับซ้อนสูงควรจำกัดที่ 2-3) — สำหรับ beginner ให้ prefer 3 ก่อน เพราะ novice learner มี smaller working memory chunks กว่า expert และ 5 items อาจ overload ได้ง่าย
  นับ "concept ใหม่" = named technique, API method, หรือ principle ที่ผู้เรียนต้องสร้าง mental model ใหม่ — ไม่ใช่แค่ term หรือ parameter name ที่ follow จาก concept หลักอยู่แล้ว (เช่น options ของ function ที่สอนไปแล้ว = ไม่นับ)
  "concept ที่ซับซ้อนสูง" = concept ที่ต้องการ prerequisite knowledge > 1 ระดับขึ้นไป หรือมี interacting sub-concepts ≥ 3 อย่างที่ต้องเข้าใจพร้อมกัน

**ขั้นที่ 2 — WebSearch เพื่อ verify scope draft ที่กำหนดแล้ว:**
WebSearch: "[topic] overview key concepts"
WebSearch: "[topic] beginner guide what to learn"

⚠️ Scope anchor: WebSearch ใช้เพื่อ verify ว่า concept ที่วางไว้มีจริงไหม — ไม่ใช่เพื่อขยาย scope ใหม่ ถ้าพบ concept เพิ่มเติมให้ note ว่า "out of scope" ห้ามเพิ่มเข้า outline โดยไม่มาจาก $ARGUMENTS — rule นี้ใช้กับ WebSearch ใน STEP 0 เท่านั้น (ถ้าพบ concept สำคัญที่ขาดหายในขั้น STEP 1 fetch ภายหลัง → ดู STEP 1.3 ซึ่งมี autonomous default สำหรับกรณีนี้ (ไม่เพิ่มเข้า scope หรือเพิ่มเป็น prerequisite note เท่านั้น))
ถ้า WebSearch พบว่า concept ใน $ARGUMENTS deprecated หรือ removed จาก version ปัจจุบัน → ถ้า $ARGUMENTS ระบุ version เก่าชัดเจน: สอน version นั้นตามที่ขอ + เพิ่ม callout "⚠️ concept นี้ deprecated ใน version [Y] — replacement คือ [Z]" ไว้ต้นเอกสาร — ถ้า $ARGUMENTS ไม่ระบุ version: สอน replacement ล่าสุด + เพิ่ม note ในเอกสารว่า "[X] deprecated ใน version [Y] แทนด้วย [Z]" — ไม่ต้องถาม user ทั้งสองกรณี

⚠️ **Domain adaptation** — ปรับ approach ตาม domain type โดยอัตโนมัติ ไม่ต้องถาม user:
- **Visual/GUI-heavy topics** (Figma, CSS layout, chart libraries): code examples ต้องมี text-based output description เช่น "# output: กล่องสีน้ำเงิน 200×100px ตรงกลางหน้าจอ" แทน screenshot — ระบุไว้ใน 00-overview ว่า "เอกสารนี้ใช้ text description แทน visual output"
- **Math-heavy topics** (statistics, ML math, cryptography): ถ้า target environment รองรับ LaTeX (เช่น GitHub, Obsidian, Docusaurus) ใช้ LaTeX notation ได้ตามปกติ — ถ้าไม่แน่ใจ environment: fallback เป็น plain text notation เช่น `E = mc^2` หรือ code block แสดง calculation แทน (universal compatibility)
- **Rapidly-changing APIs** (cloud services, 3rd-party APIs): เพิ่ม `# API version tested: [version], [date]` ใน code examples ทุกชิ้น + เพิ่ม callout ใต้ code: "⚠️ API นี้อาจเปลี่ยนแปลง — verify กับ official docs ก่อนใช้งานจริง"
- **Niche/proprietary tools** ที่ไม่มี official public docs: community source (GitHub, StackOverflow upvote > 100) เป็น primary source ได้ + ระบุใน 00-overview ว่า "เอกสารนี้ใช้ community sources เพราะไม่มี official docs สาธารณะ"
- **Conceptual/non-code topics** (soft skills, methodology, design patterns): ไม่มี code ก็ได้ — ใช้ scenario/case study แทน code examples ใน STEP 3 ส่วนที่ 4

ถ้า level = beginner และ topic เป็น advanced domain → แจ้ง user ว่า "กำลังสมมติ prerequisites ดังนี้: [...] ถูกต้องไหม?" ก่อนดำเนิน STEP 1 — "advanced domain" = domain ที่ต้องการ formal background หรือ industry experience ก่อน เช่น cryptography, distributed systems, ML/AI model internals, financial derivatives, compiler design, operating system internals (เทียบ: "basic topic" ที่นิยามไว้ในบรรทัดถัดไป)
ถ้า level = advanced และ topic ดู basic มาก (เช่น "Python variables | advanced") → interpret ว่า user ต้องการสำรวจ topic นั้นในเชิง advanced (เช่น internals, edge cases, production patterns) — ไม่ต้อง block แต่ระบุใน scope draft ว่า "สอน X ในแง่ [ชื่อ aspect] สำหรับผู้ที่รู้ basics แล้ว" พร้อมแจ้ง user เพื่อยืนยันทิศทาง — หลักการ: "topic ที่ดู basic มาก" = topic ที่ปรากฏในหลักสูตร beginner ทั่วไป เช่น variables, loops, basic syntax, basic data types
ถ้า user confirm prerequisites: ประเมินว่า prerequisites ที่ระบุ non-trivial ไหม (ต้องใช้เวลาเรียน > 2 ชั่วโมง) — ถ้าใช่ → **autonomous default**: เพิ่ม prerequisite recap สั้นๆ ใน 00-overview.md แล้ว proceed STEP 1 ได้เลย — ไม่ต้องถาม user

ประมาณ reading time: 15-20 นาทีต่อบท + 5 นาทีต่อ code example + 20-30 นาทีสำหรับ exercises — ใช้ตัวเลขนี้คำนวณ estimated total hours
⚠️ Complexity multiplier: ถ้า topic ต้องการ prior abstract knowledge สูง (เช่น cryptography, distributed systems, type theory) หรือ concept มี many interdependencies → ใช้ 1.5x-2x ของ estimate ปกติ — ระบุในคำเตือนให้ user ทราบ
ถ้า scope ที่กำหนดจะมีมากกว่า 8 บท หรือ estimated reading time > 6 ชั่วโมง → autonomous default: limit scope ให้ไม่เกิน 8 บทสำหรับ Part 1 + ระบุใน scope summary ว่า "Part 1 of series — ครอบคลุม [N บทแรก]" + proceed STEP 1 ได้เลยโดยไม่ต้องรอ user

⚠️ Scope confirmation gate: ก่อนเดิน STEP 1 ต้องแสดง scope summary ให้ user เห็นและรอ confirm ก่อนเสมอ:
```
Topic: [topic]
Level: [level]
บทที่จะสอน:
  1. [บทที่ 1]
  2. [บทที่ 2]
  ...
Out of scope: [สิ่งที่ไม่ครอบคลุม]
Estimated reading time: ~[X] ชั่วโมง
---
ยืนยัน scope นี้ไหม? (ใช่ / ไม่ใช่ + comment)
```
รอ user confirm ก่อน proceed STEP 1 — ห้ามข้าม gate นี้ — เมื่อ confirm แล้ว → บันทึก checkpoint (NEXT: STEP 1) → ดำเนิน STEP 1 ทันที
ถือว่า confirm เมื่อ user ตอบ "ใช่" / "yes" / "ok" หรือ phrase ที่แสดง intent confirm ชัดเจน เช่น "ลุยเลย" / "เอาเลย" / "ก็ได้" / "เริ่มได้" / "sure" / "โอเค" โดยไม่มีข้อความอื่นตามมา — หลักการ: ถ้า intent ชัดเจนว่า confirm โดยไม่มี modification content → treat เป็น confirm — ถ้า response มีทั้ง confirmation word และ modification content (เช่น "โอเคแต่เปลี่ยน X ด้วย") → treat เป็น modification ทั้งหมด ไม่ถือว่า confirm — ถ้า user ระบุ modification ใดๆ → revise scope แล้ว re-show scope format เดิมพร้อม confirm gate ใหม่ก่อน proceed STEP 1 เสมอ — ถ้า response ไม่ชัดเจน ให้ถามซ้ำด้วย: "ยืนยัน scope นี้ไหม? (ตอบ ใช่ หรือ ไม่ใช่ + comment)"
ถ้า user ตอบ "ไม่ใช่" โดยไม่มี comment → ถาม: "ต้องการเปลี่ยนอะไรใน scope นี้?" ก่อน revise และ re-show scope ใหม่
ถ้าถามซ้ำเกิน 2 ครั้งแล้ว user ยังตอบไม่ชัดเจน → หยุดและแจ้ง: "ไม่สามารถ proceed ได้ — กรุณาตอบ ใช่ หรือ ไม่ใช่ + comment แล้ว invoke skill ใหม่"
ถ้า user ไม่ respond เลยภายใน session (async/offline) → proceed ด้วย scope ที่ propose ไว้ได้เลย (treat เป็น implicit confirm จาก $ARGUMENTS)

---

## STEP 1 — FETCH OFFICIAL SOURCES

⚠️ ทุกข้อมูลต้อง verify กับ official source — ความรู้ของ Claude ใช้เป็น starting point ได้ แต่ต้องยืนยันก่อนเขียนเสมอ เพราะอาจ outdated หรือผิดโดยไม่รู้ตัว

### 1.1 ค้นหาและ Fetch Official Docs
⚠️ Search results จาก STEP 1 supersede ผลจาก STEP 0 — ถ้า user เปลี่ยน scope ระหว่าง confirm gate: STEP 0 search results อาจ stale สำหรับ concepts ที่เพิ่ม/ลด ไม่ต้องใช้ผลเก่าจาก STEP 0 สำหรับ concepts ที่เปลี่ยน — fetch ใหม่ใน STEP 1 นี้เท่านั้น
WebSearch: "[topic] official documentation"
WebSearch: "[topic] docs site:docs.* OR site:*.dev OR site:*.io"
WebSearch: "[topic] best practices production"
WebSearch: "[topic] anti-patterns common mistakes"
(2 queries สุดท้ายใช้เพื่อ verify ว่า approach ที่จะสอนเป็น best practice จริง — บันทึกผลใน source-notes พร้อม CONCEPT: "Best Practice: [ชื่อ approach]" หรือ CONCEPT: "Anti-pattern: [ชื่อ approach]")
⚠️ Best practice search fallback: ถ้า search results ส่วนใหญ่อยู่ใน banned list (Medium, Dev.to, personal blogs) → อย่า fetch — ให้ WebSearch ใหม่ด้วย `"[topic] recommendations site:[official-docs-domain]"` แทน หรือ ค้นหา section ที่ชื่อว่า "Best Practices", "Recommendations", "Guidelines", "Do and Don't" ใน official docs ที่ fetch ไปแล้วในรอบนี้ — ถ้ายังไม่พบ QUOTE รองรับ best practice ใด: proceed ด้วย no-consensus format ใน STEP 3 (ระบุ use case ของแต่ละ approach แทนการ force preference) — ห้ามใช้ banned source เพื่อหา best practice แม้จะเป็น query สุดท้าย
จากนั้นใช้ WebFetch ดึงเนื้อหาจาก URL ที่หาได้ — Fetch สูงสุด 5 URL ต่อ concept (prioritize tier 1 ก่อน) ถ้ามี tier-1 sources หลายแหล่ง: ใช้แหล่งที่ authoritative ที่สุดสำหรับ concept นั้น
ถ้า WebFetch ส่งคืน error, redirect ไปยัง login/paywall, 404, timeout/network error, หรือเนื้อหาไม่ใช่ documentation จริง → บันทึก FETCH_FAILED สำหรับ URL นั้นไว้ก่อน อย่า proceed ต่อโดยสมมติว่า fetch สำเร็จ — ถ้าเป็น timeout/network error (ต่างจาก 404): ลอง retry 1 ครั้งทันที ถ้ายัง timeout → treat เหมือน FETCH_FAILED และดำเนินการตาม retry cap ปกติ — ดำเนิน fetch ต่อสำหรับ URL ที่เหลือก่อน แล้วรวบรวม FETCH_FAILED ทั้งหมดและ handle ใน STEP 1.4 ครั้งเดียวหลังจาก fetch ครบ URL ที่ต้องการในรอบนี้
ถ้า docs ยาวมาก: WebSearch เฉพาะ concept ทีละตัว (เช่น "[topic] [concept-name]") แทนการ fetch ทั้งหมด — prioritize sections: Getting Started, Core Concepts, API Reference

⚠️ Source hierarchy (trusted มากไปน้อย):
1. Official documentation site ของ project (รวมถึง MDN สำหรับ Web APIs ซึ่งถือเป็น quasi-official สำหรับ web technologies)
2. Official GitHub repo ของ project (README/Wiki)
3. Official package registry (PyPI, npm, crates.io)
4. Official blog/announcement ของ project

ห้าม fetch จาก: Medium, Dev.to, Hashnode, personal blogs, tutorial aggregators เป็น primary source — ถ้าเจอใน search results ให้ข้ามและ search ต่อ
ถ้า search results ทุกรายการอยู่ใน banned list → search ใหม่ด้วย query ที่เฉพาะเจาะจงกว่า เช่น เพิ่ม site: filter หรือค้น "[topic] specification" / "[topic] reference manual" ก่อนไป STEP 1.4
ถ้าหา source tier 1-4 ไม่ได้ → ดำเนินการตาม STEP 1.4 และแจ้ง user ว่าจะใช้ source ประเภทใดก่อน proceed

### 1.2 บันทึก Source Notes
สร้างไฟล์ชั่วคราว docs/[topic]-source-notes.md บันทึกทุก concept:
⚠️ ห้ามลบ source-notes ก่อน STEP 7 — ไฟล์นี้ใช้ใน review loop ทุกรอบ (Agent 2 ต้องอ่าน QUOTE จากไฟล์นี้)

SOURCE: [URL]
VERSION: [เช่น v3.2] หรือ fetched [วันที่ที่ fetch URL นี้] ถ้าหา version ไม่ได้ — ใช้วันที่ fetch ไม่ใช่วันที่เขียนเอกสาร
CONCEPT: [ชื่อ concept]
QUOTE: "[ข้อความจริงจาก docs]"

⚠️ QUOTE integrity rule: QUOTE ต้องเป็น exact copy จาก source ภาษาเดิม (ส่วนใหญ่ภาษาอังกฤษ) — ห้ามแปลหรือ paraphrase
ทดสอบด้วย WebFetch: fetch URL นั้นอีกครั้ง แล้ว search หาข้อความใน QUOTE ใน response — ถ้าเจอเป็น exact substring = valid ถ้าไม่เจอ หรือเจอแค่ near-match (คำเปลี่ยนนิดหน่อย) = QUOTE invalid — ถ้า WebFetch response ถูก truncate ก่อนถึง QUOTE text → fetch URL นั้นซ้ำโดยใช้ URL fragment ที่ชี้ไปส่วนนั้น หรือค้นด้วย "[exact phrase from QUOTE]" site:[docs-domain] เพื่อยืนยันก่อน label invalid — ถ้า WebFetch ครั้งที่ 2 ก็ fail (network error, timeout) → proceed ด้วย fallback methods ที่เหลือตาม retry cap ก่อน (ดูกฎ retry cap ด้านล่าง) — label [UNVERIFIED] เมื่อครบ retry cap แล้วยังหาไม่ได้เท่านั้น และ proceed ตาม STEP 1.4 community source rules
⚠️ JS-rendered docs exception: ถ้า docs ใช้ JS framework (Next.js, Docusaurus, VitePress ฯลฯ) WebFetch อาจคืน HTML ว่างเปล่าหรือ loading placeholder — ไม่ถือว่า QUOTE invalid ทันที ให้ลอง: (1) fetch raw GitHub source ของ docs repo แทน หรือ (2) search ด้วย "[exact phrase from QUOTE] site:[docs-domain]" — ถ้ายังไม่เจอจาก 2 วิธีนี้จึง label [UNVERIFIED]
ถ้า QUOTE invalid หรือ JS-rendered และ fallback ทั้ง 2 ไม่สำเร็จ: label [UNVERIFIED] และ proceed ตาม STEP 1.4 (community source rules)
⚠️ Retry cap per QUOTE: สูงสุด 1 WebFetch ซ้ำ + ไม่เกิน 2 fallback methods (GitHub raw หรือ site: search) → รวมสูงสุด 3 operations ต่อ QUOTE — ถ้าครบแล้วยังหาไม่ได้ → label [UNVERIFIED] ทันที ห้าม retry เพิ่ม
ถ้า QUOTE มาจาก community source → ใส่ [COMMUNITY] ไว้ต้น QUOTE เพื่อ track ใน STEP 3

ทุก concept ที่จะสอนต้องมี QUOTE อ้างอิงจาก source

### 1.3 Fetch เพิ่มจนครบ + Gate Check
Fetch เพิ่มจนครบทุก Concept ใน STEP 0

⚠️ Gate: สร้าง checklist table ก่อน proceed:
| Concept (จาก STEP 0 scope) | มี QUOTE ใน source-notes? |
|---|---|
| [concept 1] | Yes / No |
| [concept 2] | Yes / No |

ห้ามเดิน STEP 2 ถ้ามีแถว "No" อยู่ — fetch เพิ่มก่อน — ถ้า concept ใดไม่มี QUOTE หลัง retry ครบ cap แล้ว (ตาม retry cap ใน STEP 1.2: 1 WebFetch ซ้ำ + 2 fallback methods รวม 3 operations) → ใช้ community source rules (STEP 1.4) สำหรับ concept นั้น แทนการ loop ต่อ
ถ้า docs เปิดเผยว่า scope ใน STEP 0 ไม่ถูกต้อง:
- concept ที่วางไว้ไม่มีจริง / deprecated → ตัดออกจาก scope ได้ทันที
- prerequisites ที่ประเมินผิด → ปรับ prerequisites ได้ทันที
- พบ concept สำคัญที่ไม่ได้วางแผน → autonomous default: ไม่เพิ่ม concept นั้น + proceed ด้วย scope เดิม — ถ้า concept นั้นสำคัญมากจริงๆ (จำเป็นต้องเข้าใจก่อน concept อื่นในบท) → เพิ่มเป็น prerequisite note ใน 00-overview.md แทนที่จะเพิ่มเป็นบทใหม่
⚠️ scope revision ใน STEP 1.3 ≠ ขยาย scope — เป็นแค่การ trim หรือ adjust prerequisites เท่านั้น — ถ้า trim > 1 concept หรือ prerequisites เปลี่ยนมีนัยสำคัญ → แจ้ง user ใน scope summary ว่า trim อะไรและเหตุผล + proceed STEP 2 ได้เลย (สอดคล้องกับ STEP 0 scope anchor) — ไม่ต้องรอ confirm

### 1.4 ถ้าหา Official Docs ไม่ได้
- **Autonomous default**: ลอง community source ก่อนตัดทิ้ง — แหล่งที่ยอมรับตามลำดับ: GitHub official repo → official blog → MDN → StackOverflow (upvote > 100) ไม่ต้องถาม user ก่อน
- แหล่งที่ยอมรับ: GitHub official repo, official blog, MDN, StackOverflow (upvote > 100)
- Fetch อ่าน community source แล้วตรวจว่า content ตอบ concept ที่ต้องการจริง — ไม่ใช่แค่ mention topic แบบผ่านๆ (เกณฑ์ qualify: source ต้อง explain หรือ demonstrate concept โดยตรง — ถ้าพูดถึงแบบ peripheral โดยไม่อธิบาย = ไม่ qualify)
- ถ้า qualify: บันทึก QUOTE พร้อม [COMMUNITY] prefix + ระบุใน callout ในเอกสารว่า "> ข้อมูลส่วนนี้มาจาก community source — verify ก่อนใช้งานจริง"
- ถ้า community source ก็หาไม่ได้หลัง retry ครบ cap (ตาม STEP 1.2) หรือ content ที่ fetch มาไม่ qualify: autonomous default คือตัด concept ออก + ระบุใน scope summary ว่าตัด concept ใดออกและเหตุผล + proceed ด้วย concept ที่มี verified source เท่านั้น — ห้าม loop หรือใช้ placeholder concept ที่ไม่มี source รองรับ

---

## STEP 2 — สร้าง Outline

จากข้อมูลใน source-notes:

docs/[topic-kebab]/
├── 00-overview.md        (~5 นาที)
├── 01-[concept-1].md     (~X นาที)
├── 02-[concept-2].md     (~X นาที)
├── ...
├── exercises.md          (~20-30 นาที)
└── glossary.md

แต่ละบทต้อง map ไปยัง QUOTE/concept ใน source-notes — ถ้า concept ในบทใดไม่มี QUOTE รองรับ ต้อง fetch ก่อนหรือตัดออก
ตรวจ: ลำดับสมเหตุสมผล? ไม่กว้างเกินไปสำหรับ 1-2 ชั่วโมง? แต่ละบทมีไม่เกิน 3-5 concept?

⚠️ Formative checkpoint ก่อน STEP 3 — ทำเป็น internal self-check (ไม่ต้องแสดงให้ user เห็น) ตอบ 3 คำถามนี้ก่อนเริ่มเขียน:
1. ทุก concept ในบทใดๆ มี QUOTE จาก source-notes รองรับไหม? ถ้าไม่ → กลับ STEP 1
2. บทแรกสุดสามารถเรียนได้โดยใช้แค่ prerequisites ที่ระบุใน STEP 0 ไหม? ถ้าไม่ → เพิ่ม prerequisite recap สั้นๆ ใน overview ได้เลย + แจ้ง user ใน scope summary ว่าปรับ prerequisites อะไร — ไม่ต้องรอ confirm (ตาม STEP 1.3)
3. ลำดับบท A→B→C สมเหตุสมผลไหม — concept ในบท B build on บท A จริงไหม? ถ้าไม่ → เรียงลำดับใหม่ก่อน
ถ้าตอบ "ไม่" ข้อใดข้อหนึ่ง → แก้ก่อนแล้วค่อยเดิน STEP 3

---

## ⚓ CORE INVARIANTS — อ่านทุกครั้งก่อนเริ่ม STEP 3

กฎด้านล่างนี้เป็นสรุปของ rule ที่มีอยู่แล้วในสกิลนี้ ใส่ไว้เป็น anchor ป้องกัน instruction drift ในการเขียน series ยาว — ถ้า rule ใดขัดแย้งกับ STEP อื่น ให้อ่าน STEP นั้นโดยตรงเสมอ:

1. **Source first** — ทุก claim ต้องมี QUOTE ใน source-notes ก่อนเขียน ห้าม rely ความจำโดยไม่มีหลักฐาน
2. **Threshold = 100** — review loop จบได้เมื่อทุก agent ได้ 100 เท่านั้น ไม่มีข้อยกเว้น
3. **Exercise domain** — Intermediate exercises ต้องใช้ domain ที่ต่างจากตัวอย่างในบทอย่างชัดเจน ห้าม copy context หรือตัวเลขจากตัวอย่าง (ทุก level)
4. **Self-check ⏸ = 3 elements ของ Generation Effect** — (1) pause + retrieve, (2) เขียนคำตอบก่อนดูเฉลย, (3) เขียนเหตุผลที่ตอบแบบนั้น — ขาด element ใด = fail
5. **Code = tested** — code ที่ไม่มี label "ยังไม่ได้ทดสอบ" ต้องรันผ่านจริงด้วย Bash ก่อน insert เท่านั้น

---

## STEP 3 — WRITE

⚠️ **Mid-process scope change**: ถ้า user ส่งข้อความขอ modify scope ระหว่าง STEP 3 (หลัง confirm ใน STEP 0 แล้ว):
- เสร็จบทที่กำลังเขียนอยู่ก่อน ห้าม abort กลางคัน
- แจ้ง user ว่า "จะ apply scope change นี้หลังเสร็จบทปัจจุบัน — scope ที่เปลี่ยนคือ: [สรุป] — ยืนยันก่อนดำเนินต่อไหม?"
- ถ้า user confirm → update outline ใน STEP 2 format แล้ว proceed; ถ้า user ไม่ respond → ใช้ scope เดิมต่อ

เขียนแต่ละ section โดยอิงจาก QUOTE ใน source-notes เป็นหลัก — ถ้าใช้ความรู้ที่มี ต้อง cross-check กับ source-notes ก่อนทุกครั้ง

⚠️ **Pre-chapter checkpoint**: ก่อนเริ่มเขียนแต่ละบท (ไม่ใช่หลังเขียนเสร็จ) — บันทึก checkpoint ทันที:
```
NEXT: กำลังเขียน [filename] — ยังไม่เสร็จ
```
เหตุผล: ถ้า context หมดระหว่างเขียนบท checkpoint จะระบุว่า session ใหม่ต้องเขียนบทนี้ใหม่ตั้งแต่ต้น (ดีกว่าไม่รู้ว่าค้างอยู่ที่ไหน) — เมื่อเขียนบทเสร็จแล้ว update NEXT เป็นบทถัดไป

ทุกบทต้องมี sections บังคับครบ:
- **บทที่ 1**: sections 1,2,3,4,5,6 บังคับ (6 mandatory — ไม่มี Pre-chapter Retrieval)
- **บทที่ 2+**: sections 1,2,3,4,5,6,7 บังคับ (7 mandatory — ต้องมี Pre-chapter Retrieval)

1. วัตถุประสงค์ — bullet list "อ่านจบแล้วทำอะไรได้บ้าง"
   ทุก objective ต้องใช้ action verb ที่วัดได้ตาม Bloom's level:
   Beginner → อธิบาย, ระบุ, ยกตัวอย่าง | Intermediate → ประยุกต์, เปรียบเทียบ, แก้ปัญหา | Advanced → ออกแบบ, วิเคราะห์, ประเมิน
   ห้ามใช้ "เข้าใจ" หรือ "รู้จัก" — วัดไม่ได้

2. ทำไมต้องรู้? (Why) — concept นี้แก้ปัญหาอะไร ก่อนอธิบายว่ามันคืออะไร
   ⚠️ ปัญหาที่ใช้ใน Why section ต้องอธิบายได้ด้วยภาษาของ prerequisites ที่ระบุไว้ใน STEP 0 — ห้ามใช้ vocabulary จาก concept ที่กำลังจะสอน (circular dependency) ตรวจด้วยคำถาม: "ถ้า target audience อ่าน Why section โดยไม่รู้ concept นี้เลย จะเข้าใจปัญหาที่อธิบายได้ไหม?"

3. เนื้อหาหลัก — จาก QUOTE ใน source-notes ภาษาไทย ศัพท์เทคนิค/code เป็น English
   เพิ่ม self-check ระหว่าง sub-section (อย่างน้อย 1 ⏸ ต่อ major sub-section หรือต่อ concept ใหม่ที่สำคัญ):
   "⏸ ก่อนอ่านต่อ: ลองอธิบาย [micro-concept นี้] ด้วยคำตัวเองใน 1 ประโยค — เขียนลงกระดาษหรือ editor (ห้ามดูเฉลย) — แล้วเขียนเหตุผลสั้นๆ 1 ประโยคว่าทำไมถึงตอบแบบนั้น ก่อนดูเฉลย
   ---
   > เฉลย: [คำตอบที่ถูกต้อง 1-2 ประโยค] — ถ้าตอบไม่ได้: อ่าน [ชื่อ sub-section] อีกครั้ง — ถ้าระบุจุดที่เข้าใจผิดไม่ได้: อ่าน section ทั้งหมดของบทนั้นใหม่ โดยเน้นที่ตัวอย่าง Beginner และ Intermediate ก่อน"
   (คั่นด้วย `---` เพื่อให้ผู้เรียนต้องเลื่อนลงดูเฉลย — เหมือนกับ Pre-chapter Retrieval)
   ทุก self-check ต้องมีเฉลย + (a) remediation direction ("ถ้าตอบไม่ได้: อ่าน [section]") + (b) novice fallback ("ถ้าระบุจุดที่เข้าใจผิดไม่ได้: อ่าน section ทั้งหมดของบทนั้นใหม่ โดยเน้นที่ตัวอย่าง Beginner และ Intermediate ก่อน") — ทั้งสองต้องสั้น ไม่ใช่ paragraph ยาว
   ⚠️ Backward retrieval: อย่างน้อย 1 ⏸ ต่อบท ต้องเป็น backward retrieval — ถามเกี่ยวกับ concept จาก sub-section ก่อนหน้าในบทเดียวกัน โดยต้องข้ามอย่างน้อย 1 sub-section (ไม่ใช่ sub-section ที่เพิ่งอ่านจบทันที — เช่น ถ้ามี sub-sections A→B→C และ self-check อยู่หลัง C ให้ถาม concept จาก A ไม่ใช่ B) เพื่อสร้าง spacing effect ภายในบท
   ⚠️ คำถาม self-check ควรทำให้ผู้เรียนรู้สึก "ต้องคิดก่อนตอบ" — ไม่ใช่ถามคำถามที่เพิ่งอ่านผ่านตามาและยังอยู่ใน short-term memory ทันที (ให้ถามในแง่ application หรือ explain-in-own-words แทน recall ตรง)
   ⚠️ Bloom's alignment: ระดับ cognitive ของ self-check ต้องไม่ต่ำกว่า Bloom's level ของ objectives บทนั้น (section 1) — ใช้ ordinal position เป็น floor (minimum level) เมื่อ objectives ไม่ได้ระบุ Bloom's level ชัดเจน (ไม่ใช่ target ของทุก self-check — objectives ของบทนั้นเป็นตัวกำหนด level ที่แท้จริง): บท 1 = L1-L2, บท 2-3 = L3, บท 4+ = L4-L5 — "บท" = ไฟล์ prefix ≥ 01 (ไม่นับ 00-overview, exercises.md, glossary.md) — ถ้า objectives ของบทนั้นระบุ level ชัดเจนกว่า → override ตาม objectives (ห้าม override ลง) — ถ้า series = intermediate: floor ขั้นต่ำสุด = L2, advanced: floor ขั้นต่ำสุด = L3 — ถ้า series < 4 บท → บทสุดท้ายใช้ L3 ได้ — concept ใหม่ที่ยากในบทหลังๆ ยังอาจต้องการ self-check แบบ L1 ก่อน แต่ต้องมีอย่างน้อย 1 self-check ที่ถึง floor ด้วย
   ⚠️ Best practice priority: เมื่อมีหลาย approach ที่ทำสิ่งเดียวกันได้ — ต้องระบุในเนื้อหาว่า (a) approach ไหนเป็น best practice / popular pattern ที่ industry ใช้กว้างขวาง + เหตุผลสั้นๆ และ (b) approach ไหนที่ควรเลี่ยง + เหตุผล (เช่น outdated, performance issue, security risk) — format แนะนำ: "✅ วิธีที่แนะนำ: [approach] — เพราะ [เหตุผล]" + "⚠️ ควรเลี่ยง: [approach] — เพราะ [เหตุผล]"
   ⚠️ verify หมายถึง: ต้องมี QUOTE entry ใน source-notes (จาก STEP 1.1 best practice queries หรือจาก section "Best Practices"/"Recommendations"/"Guidelines" ในเอกสาร official ที่ fetch ไปแล้ว) ที่สนับสนุน preference นั้นโดยตรง — ห้ามระบุ best practice จากความรู้ตัวเองโดยไม่มี QUOTE รองรับ
   กรณีพิเศษ: ถ้ามีเพียง approach เดียวที่ถูกต้อง → ระบุว่า "นี่คือ standard approach — ไม่มีทางเลือกอื่นที่แนะนำ" | ถ้าไม่มี QUOTE รองรับ preference ใด (official source ไม่ได้ระบุ preference ชัดเจน หรือ community ยังไม่มี consensus) → ระบุว่า "ไม่มี consensus ที่ชัดเจนในอุตสาหกรรม — เลือกตาม use case: [approach A] เหมาะกับ [กรณี X] เพราะ [เหตุผล], [approach B] เหมาะกับ [กรณี Y] เพราะ [เหตุผล]" — ห้ามแต่ง preference ขึ้นเองถ้าไม่มีหลักฐานจาก source-notes

4. ตัวอย่าง 3 ระดับ:
   - Beginner: มี inline comments อธิบายทุกบรรทัดที่ non-obvious (high scaffolding) — "non-obvious" = บรรทัดที่ผู้เรียนระดับนั้น อาจถามว่า "ทำไมต้องทำแบบนี้?" หรือ "นี่มาจากไหน?" ถ้าไม่มี comment อธิบาย — comments ต้องอธิบาย "ทำไมต้องทำแบบนี้" (reasoning/why) ไม่ใช่แค่ "ทำอะไร" (what) เช่น # ใช้ list comprehension เพราะ Pythonic กว่าและอ่านง่ายกว่า for loop ปกติ (ไม่ใช่แค่ # สร้าง list ใหม่)
   - Intermediate: ลด comments เหลือเฉพาะ idiom หรือ pattern เฉพาะของ language/framework ที่ผู้เรียน intermediate อาจยังไม่เคยเห็น (medium scaffolding — ทดสอบ: ถ้าผู้เรียนที่รู้ basics แล้วจะถาม "pattern/idiom นี้คืออะไร?" = ใส่ comment)
   - Advanced (production-grade): ไม่มี step-by-step comments มีเฉพาะ trade-off discussion (faded) — ต้องใช้ approach ที่เป็น best practice จริง (verify จาก source-notes) — ถ้าเลือกใช้ approach ที่ไม่ใช่ best practice เพื่อ demonstrate trade-off ต้องระบุด้วย comment ว่า "# หมายเหตุ: approach นี้ใช้เพื่อ demo trade-off — production จริงใช้ [approach ที่ดีกว่า]"
   ⚠️ สำหรับ concept ที่ complex สูง (ตามนิยามใน STEP 0): พิจารณาเพิ่ม "completion problem" ระหว่าง Intermediate กับ Advanced examples — code ที่มี `# TODO: [task]` ให้ผู้เรียนทำ partially-guided task ก่อนลุยทำ Advanced exercise เต็มรูปแบบ (ช่วยลด cognitive load jump)

5. Common Mistakes — ก่อนเขียน mistake แต่ละข้อ: ตรวจว่ามี QUOTE ใน source-notes รองรับไหม
   - Mistake ต้องเกี่ยวกับ misconception ที่เกิดจาก concept ในบทนั้นโดยตรง ไม่ใช่ general coding mistake (เช่น "ลืม semicolon") ที่เกิดกับทุก concept
   - ถ้ามี QUOTE → เขียนได้ format: ❌ แบบผิด → ✅ แบบถูก + เหตุผล + 🔍 สัญญาณที่จะรู้ว่าทำ mistake นี้: [อาการที่สังเกตได้จากผลลัพธ์หรือ error] + 🤔 "ลองอธิบายด้วยตัวเองก่อนอ่านเหตุผล: ทำไม ✅ แบบถูกถึงทำงานได้ถูกต้อง — principle อะไรที่อยู่เบื้องหลัง?" + *(source: [ชื่อ/URL])*
     (🔍 สัญญาณ = อาการที่ผู้เรียนจะสังเกตเห็นจริงใน output/error เช่น "ถ้าเห็น TypeError: X is not a function" หรือ "ถ้า response ได้ 200 แต่ data ว่าง" — ไม่ใช่แค่บอกว่า "ถ้าทำผิด")
   - source ที่ยอมรับ: QUOTE ใน source-notes, official GitHub Issues/PR, official blog posts, StackOverflow (upvote > 100)
   - ถ้าหา source ไม่ได้ → label inline: *(unverified — common pattern, verify before applying)* และจะถูกหัก 5 คะแนนใน review
   - ห้ามใส่ *(source: official docs)* แบบ generic โดยไม่มี URL จริง — URL ต้องเป็น URL จาก source-notes ของ QUOTE ที่รองรับ mistake นั้น (copy จาก source-notes โดยตรง)

6. สรุปบท — คำถาม Retrieval 2-3 ข้อ (ผู้เรียนตอบก่อน แล้วค่อยดูเฉลยด้านล่าง — ห้ามเป็นแค่ bullet list)
   ต้องมีอย่างน้อย 1 ข้อที่เป็น application-level ขึ้นไป และอย่างน้อย 1 ข้อที่เป็น elaborative interrogation เช่น "ทำไม X จึงออกแบบมาแบบนี้?" หรือ "จะเกิดอะไรถ้า Y ไม่มี Z?" — ไม่ใช่แค่ "X คืออะไร?"
   ⚠️ Transfer-Appropriate Processing: สำหรับบทที่สอน code skills หรือ technical procedures — อย่างน้อย 1 ข้อต้องเป็น code-based task (เช่น "เขียน code ที่ทำ X" หรือ "ระบุ bug ใน code นี้") เพราะ encoding ที่ตรงกับ retrieval mode ช่วย transfer สู่ real-world usage ได้ดีกว่า verbal-only questions
   ⚠️ desirable difficulty: คำถามที่ทำให้ผู้เรียนรู้สึก "ไม่แน่ใจ" นั้นถูกต้องแล้ว — ห้าม simplify จนตอบได้ทันที
   ⚠️ Bloom's progression: ordinal position เป็น floor (minimum level) เมื่อ objectives ไม่ระบุ Bloom's level ชัดเจน — "บท" = ไฟล์ prefix ≥ 01 เท่านั้น (ไม่นับ 00-overview, exercises.md, glossary.md) | floor: บท 1 = L1-L2, บท 2-3 = L3, บท 4+ = L4-L5 | ถ้า chapter objectives ระบุ level ชัดเจนกว่า → override ตาม objectives (ห้าม override ลง เพราะ retrieval ห้ามง่ายกว่า objectives) | floor สำหรับ intermediate series = L2, advanced = L3 | ถ้า series < 4 บท → บทสุดท้ายใช้ L3 ได้
   ⚠️ Generation effect: ก่อนแสดงเฉลย ให้เพิ่มบรรทัด: "หยุดคิดอย่างน้อย 30 วินาที (ปรับได้ตามตัวเอง) พยายาม retrieve ก่อนเขียน — จากนั้นเขียนคำตอบลงกระดาษหรือ editor (ห้ามดูเฉลย) — แล้วเขียนเหตุผลสั้นๆ 1-2 ประโยคว่าทำไมคุณถึงตอบแบบนั้น — แล้วค่อยดูเฉลย" — การ pause + retrieve ก่อน generate เหตุผล ให้ผล retention ที่แน่นกว่า passive reading
   เฉลยทุกข้อต้องมี remediation path: "ถ้าตอบผิด หรือไม่แน่ใจ: ลองอธิบายก่อนว่าคุณเข้าใจผิดตรงไหน → กลับอ่าน [ระบุชื่อ section/ส่วนที่ X] ในบทนี้อีกครั้ง แล้วลองตอบคำถามนี้ใหม่โดยไม่ดูเนื้อหา — ถ้าระบุจุดที่เข้าใจผิดไม่ได้ (ไม่รู้ว่าตัวเองไม่รู้อะไร): อ่าน section ทั้งหมดของบทนั้นใหม่ โดยเน้นที่ตัวอย่าง Beginner และ Intermediate ก่อน แล้วลองตอบใหม่" — การระบุจุดที่เข้าใจผิดก่อนกลับอ่านช่วย activate metacognition ได้ดีกว่าการ re-read แบบ passive

7. Pre-chapter Retrieval (บทที่ 2+ เท่านั้น) — วางไว้เป็น **section แรกสุดของไฟล์ ก่อนส่วนที่ 1** ไม่ใช่ท้ายบท
   ต้องมีอย่างน้อย 1 ข้อที่เชื่อม concept บทก่อนกับบทปัจจุบัน เช่น "จาก [concept บทก่อน] ลองทายว่า [concept บทนี้] จะช่วยแก้ปัญหาอะไรได้บ้าง?" — ไม่ใช่แค่ถาม definition ซ้ำ
   format: "⏰ แนะนำ: อ่านบทนี้หลังจากผ่านไปอย่างน้อย 1 วันจากบทก่อน — ถ้าอ่านต่อเนื่อง Pre-chapter Retrieval นี้จะได้ผลน้อยลงมาก
   ก่อนอ่านบทนี้ ลองตอบ: [คำถาม 1-2 ข้อ] — เขียนคำตอบลงกระดาษหรือ editor ก่อน (ห้ามดูเฉลย) แล้วค่อย scroll ดูเฉลย"
   ⚠️ Generation effect: ก่อนแสดงเฉลย ให้เพิ่มบรรทัด: "หยุดคิดอย่างน้อย 30 วินาที (ปรับได้ตามตัวเอง) พยายาม retrieve ก่อนเขียน — จากนั้นเขียนคำตอบลงกระดาษหรือ editor (ห้ามดูเฉลย) — แล้วเขียนเหตุผลสั้นๆ 1-2 ประโยคว่าทำไมคุณถึงตอบแบบนั้น — แล้วค่อยดูเฉลย" — pause + retrieve ก่อน generate เหตุผล ให้ retention แน่นกว่า passive reading
   คั่นด้วย `---` แล้วค่อยแสดงเฉลย
   เฉลยต้องมี remediation path เช่นกัน: "ถ้าตอบผิดหรือไม่แน่ใจ: ลองอธิบายก่อนว่าคุณเข้าใจผิดตรงไหน → กลับทบทวนบทที่ X section Y ก่อน — ถ้าระบุจุดที่เข้าใจผิดไม่ได้: อ่านบทที่ X ใหม่ทั้งบทโดยเน้นที่ตัวอย่าง Beginner และ Intermediate ก่อน — การอ่านต่อโดยไม่แน่ใจ prerequisite จะทำให้เข้าใจบทนี้ยากขึ้นมาก"

---

00-overview.md ต้องมี:
- Prior Knowledge Activation: ออกแบบ prompt ที่ activate schema ก่อนอ่าน ให้เหมาะกับ topic และ audience — ถ้า audience มี prior knowledge: "นึกถึงครั้งที่คุณต้องแก้ปัญหา [specific situation ที่ topic นี้แก้ได้] — คุณแก้มันยังไง?" / ถ้า audience ไม่มี prior knowledge: "นึกถึงปัญหาที่คุณเคยเจอที่ [topic] น่าจะช่วยได้" — ตามด้วยประโยค bridging 1-2 บรรทัดที่เชื่อม prior knowledge กับสิ่งที่จะเรียน
  - ถ้า audience มี prior approach: bridging ต้องระบุชัดว่า topic นี้จะ extend หรือแก้ปัญหาที่ approach เดิมทำไม่ได้ รูปแบบที่ดี: "[X ที่คุณรู้อยู่แล้ว] ใช้ได้ในกรณี A แต่ [topic นี้] ช่วยให้ทำ B ได้ด้วย เพราะ [เหตุผล]"
  - ถ้า audience เป็น absolute beginner ที่ไม่มี prior approach (เช่น ไม่เคยเรียน topic นั้นเลย): bridging ให้เชื่อมกับ pain point จาก activation prompt ว่า series นี้จะแก้ได้อย่างไร แทน เช่น "series นี้จะพาคุณจาก [สถานการณ์ที่รู้สึกอึดอัด] ไปสู่ [สิ่งที่จะทำได้] โดยไม่ต้องรู้อะไรมาก่อน"
  - ห้าม bridging แบบ surface เช่น "[topic นี้] เชื่อมกับ [prior knowledge]" โดยไม่ระบุว่า "อย่างไร"
  ⚠️ ห้าม activation prompt แบบ "คุณรู้อะไรเกี่ยวกับ [topic] บ้าง?" (กว้างเกินไป ไม่ activate specific schema) — ต้องเจาะจงถึง situation หรือ problem จริง
- Prerequisites ชัดเจน (รู้อะไรมาก่อนถึงจะอ่านได้) + Self-assessment: เพิ่ม 2-3 คำถามที่ผู้อ่านควรตอบได้ก่อนอ่าน เช่น "ลองตอบก่อนอ่าน: [คำถาม] — ถ้าตอบไม่ได้ แนะนำให้อ่าน [prerequisite resource/link] ก่อน" ห้ามเป็นแค่ bullet list รายชื่อ prerequisites
- Table of contents พร้อม markdown links ไปแต่ละไฟล์
- Recommended reading schedule: "แนะนำให้อ่าน 1-2 บทต่อวัน แล้วทำ Pre-chapter Retrieval ของวันถัดไป — การเว้นระยะ (spacing) ช่วยให้จำได้นานกว่าอ่านทีเดียวหมด เพราะสมองต้องดึงความจำขึ้นมาใหม่แทนที่จะแค่อ่านต่อเนื่อง | หลังอ่านแต่ละบทเสร็จ: ทำ Retrieval Questions ของบทนั้นอีกครั้งหลังจาก 2 วัน → อีกครั้งหลัง 1 สัปดาห์ → อีกครั้งหลัง 1 เดือน (โดยไม่ดูเนื้อหาทุกครั้ง) — เพื่อ reinforce chapter-level content ก่อน Mixed Review | ⚠️ ตัวเลขเหล่านี้เป็น starting point ปรับได้ตามตัวเอง — ถ้า recall ยาก = ลด interval; ถ้า recall ง่าย = เพิ่ม interval | หลังอ่านครบ series แล้ว: ทำ Mixed Review อีกครั้งหลังผ่านไป 3 วัน และอีกครั้งหลัง 1 สัปดาห์ — เพื่อ consolidate long-term retention | ⚠️ ตัวเลขเหล่านี้เป็น starting point — ถ้า recall ยาก = ลด interval ลง; ถ้า recall ง่ายมาก = เพิ่ม interval ให้นานขึ้น ปรับตามตัวเองได้"
- Holistic outcome: "เมื่ออ่านครบ series นี้ คุณจะสามารถ [observable action ระดับ series] เช่น สร้าง / วิเคราะห์ / แก้ปัญหา / อธิบายให้คนอื่นเข้าใจได้" — ไม่ใช่ list objectives ของแต่ละบทซ้ำ ใช้ verb ที่ measurable เหมือน STEP 3 ส่วนที่ 1
  ⚠️ Backward alignment check: ก่อน publish outcome นี้ ให้ตรวจว่า — ผู้เรียนที่อ่านครบทุกบทและทำ exercises ครบ จะสามารถทำสิ่งที่ outcome ระบุได้จริงไหม? ถ้า outcome บอกว่า "สร้าง X" แต่ไม่มีบทใดสอน component สำคัญของ X → ต้องแก้ outcome ให้สอดคล้องกับ chapters จริง หรือเพิ่มบทที่ missing
- วิธีอ่านให้ได้ผล: "1) ลอง predict ก่อนอ่านทุก section, 2) หยุดที่ ⏸ ทุกครั้งและเขียนคำตอบจริงก่อนดูเฉลย, 3) อธิบาย concept ด้วยปากตัวเองก่อนไปบทถัดไป, 4) ทำ exercise ก่อนดูเฉลย — ถ้ายังไม่แน่ใจ pattern ไหน กลับอ่านซ้ำก่อน proceed, 5) ก่อนปิดหนังสือแต่ละวัน จดคำถาม 1 ข้อที่ยังค้างคาอยู่ — เพื่อ prime spaced recall เมื่อกลับมาอ่านวันถัดไป"

exercises.md ต้องมี cognitive task ที่ถูกต้องต่อระดับ:
- Beginner: Recall + Recognition — อธิบายด้วยคำตัวเอง, ยกตัวอย่างจากชีวิตจริง
- Intermediate: Application in novel context — สถานการณ์ที่ไม่มีใน section ตัวอย่าง (ส่วนที่ 4) ของบทนั้น → novel context = near transfer — เปลี่ยน use-case หรือ problem scenario โดยยังอยู่ใน application domain ที่ผู้เรียนคุ้นเคย ไม่ใช่แค่เปลี่ยนชื่อตัวแปรหรือตัวเลข (เช่น ถ้าตัวอย่างในบทใช้ e-commerce → exercise ใช้ healthcare หรือ logistics ได้ เพราะทั้งหมดเป็น business application domain) — ไม่ใช่ far transfer ข้ามไปชีวิตประจำวันหรืออธิบายให้คนทั่วไปเข้าใจ (นั่นคือ Advanced level)
- Advanced: Synthesis / Diagnosis — หา bug, ออกแบบระบบ, เปรียบเทียบ tradeoff
  ต้องมีอย่างน้อย 1 ข้อ semi-blind far transfer: ให้ problem scenario ใน industry/domain ที่ต่างออกไป โดยไม่บอกว่าต้องใช้ concept ไหน — ผู้เรียนต้องตัดสินใจเองจาก series ที่เรียนมา (เช่น "ปัญหาต่อไปนี้เกิดขึ้นใน [domain ที่ต่าง]: [description] — วิเคราะห์และแก้ไข")
  ต้องมีอย่างน้อย 1 ข้อ explanation/metacognitive task (ไม่ใช่ far transfer — เป็น teaching task ที่ใช้ Feynman Technique ครบ 4 ขั้น): "อธิบาย concept นี้ให้คนที่ไม่รู้ [topic domain] เข้าใจ โดย: (1) อธิบายราวกับสอนเด็ก ใช้ภาษาง่ายที่สุด, (2) ระบุจุดที่คุณอธิบายไม่ได้หรือสะดุด — ส่วนนั้นคือ gap ของความเข้าใจ, (3) กลับทบทวนส่วนนั้นในเอกสารก่อนอธิบายใหม่, (4) simplify ด้วยภาษาและตัวอย่างที่เข้าใจง่าย"
  ⚠️ (Claude note — ห้ามนำไปใส่ใน exercises.md): ขั้นที่ 2 และ 3 สำคัญมาก เป็น mechanism ที่ทำให้ Feynman Technique work จริง
  ต้องมีอย่างน้อย 1 ข้อ elaborative interrogation: "ทำไม [concept] ถึงออกแบบมาแบบนี้แทนที่จะเป็น [alternative]?" หรือ "จะเกิดอะไรถ้า [condition X] ไม่มีอยู่?" — บังคับ generate เหตุผล ไม่ใช่แค่ describe
- Mixed Review (ทำหลังจากอ่านครบทุกบท): exercises ชุดที่สลับ concept จากอย่างน้อย 3 บทขึ้นไปโดยไม่บอกว่าต้องใช้อะไร — ผู้เรียนต้องตัดสินใจเอง เลือก concept ที่ผู้เรียนมักสับสนกัน ไม่ใช่สุ่มสลับ (interleaving practice เพื่อ long-term retention)
  ต้องเพิ่ม note นี้ไว้ต้น Mixed Review: "⚠️ Mixed Review ตั้งใจให้รู้สึกยากกว่าตอนที่เพิ่งอ่านจบแต่ละบท — นั่นคือสัญญาณว่า interleaving กำลัง work ไม่ใช่สัญญาณว่าเรียนไม่ดี performance ระหว่างฝึกต่ำกว่า แต่ long-term retention สูงกว่า blocked practice มาก"
  จำนวน Mixed Review: อย่างน้อย 1 ข้อต่อ concept หลักที่ครอบคลุมใน series — heuristic เลือก concept สำหรับ interleave: เลือกคู่ที่ (a) ดูคล้ายกันแต่ใช้ต่างกัน หรือ (b) มักถูก apply ในสถานการณ์เดียวกันแต่ผลต่างกัน
  ต้องมีอย่างน้อย 1 ข้อ discrimination task โดยชัดเจน: สถานการณ์ที่ concept หลายตัวอาจดูใช้ได้ — ผู้เรียนต้องเลือกว่าจะใช้อะไร พร้อมอธิบายเหตุผลที่ไม่เลือกตัวอื่น (ทดสอบ recognition + decision-making ไม่ใช่แค่ application)
- Timing: ระบุที่แต่ละกลุ่ม exercises ว่า "ทำหลังอ่านบทที่ X" หรือ "ทำได้ระหว่างอ่าน" — ช่วยให้ผู้เรียน distribute practice ได้ถูกต้อง
- ห้ามใช้คำถาม fill-in-the-blank (ทุก level)
- ห้ามใช้คำถามที่ copy โดยตรงจากตัวอย่างในบท (context หรือตัวเลขเดิม) (ทุก level ไม่ใช่แค่ Intermediate)
- อย่างน้อย 3 ข้อต่อ concept หลัก: 1 Beginner + 1 Intermediate + 1 Advanced อย่างน้อย — ห้ามมี concept ที่มีแต่ exercises ระดับเดียวทั้งหมด + เฉลยครบทุกข้อ

⚠️ ตรวจสอบ novel context สำหรับ exercises.md: list domain ของตัวอย่างในบท (ส่วนที่ 4) แล้ว verify ว่า Intermediate exercise ใช้ domain ที่ต่างกันอย่างชัดเจน (ทำก่อน STEP 5 สำหรับ exercises) — ถ้า domain ยังเหมือนกัน → ต้องแก้ exercise ให้ต่าง domain ก่อน ห้าม proceed STEP 5 โดยไม่แก้

glossary.md:
- ทุกคำศัพท์เทคนิคที่ถูก introduce ครั้งแรกในเอกสารนี้ หรือที่ผู้อ่านระดับ target audience อาจไม่รู้ (คำที่ target audience ควรรู้แล้วไม่ต้องใส่)
- fetch คำจำกัดความจาก official source โดยตรง ระบุ SOURCE URL กำกับแต่ละคำ
- คำอธิบายภาษาไทยในรูปแบบที่จำง่าย: "[คำ] คือ [ฟังก์ชัน/บทบาทหลัก] เพราะ [เหตุผลที่ถูกออกแบบมาแบบนี้] — อย่าสับสนกับ [คำที่คล้ายกัน]" — ห้ามแปลตรงจาก source เพราะจำยาก — ถ้าไม่มีคำที่คล้ายกันใน series นี้ ให้ระบุ "ไม่มีคำที่คล้ายใน series นี้" แทน (ห้ามละทิ้ง field นี้)
- เรียงลำดับ alphabetical ตาม English term เพื่อ navigability
- แต่ละคำต้องมี "First seen in: [ชื่อไฟล์ + section]" เพื่อให้ผู้เรียน navigate กลับไปดู context ได้

⚠️ **STEP 3 → STEP 4 transition**: เมื่อเขียนครบทุกไฟล์ (ทุกบท + exercises.md + glossary.md) แล้ว → update checkpoint (NEXT: STEP 4) → ดำเนิน STEP 4 ทันที ห้ามข้ามไป STEP 5 หรือ STEP 6 โดยไม่ผ่าน STEP 4

---

## STEP 4 — CODE VERIFICATION

⚠️ Code ทุกชิ้นต้องรันผ่านจริง — ไม่มีข้อยกเว้นสำหรับ syntax หรือ logic ที่ "น่าจะถูก"

ก่อนใส่ code ทุกชิ้น (รวมถึง code ใน exercises.md ด้วย):
1. เขียนลงไฟล์ temp แล้วใช้ Bash รันจริง
2. ถ้าผ่าน → insert code (พร้อมระบุภาษา + output จริง + version ที่ทดสอบ) เข้าเอกสารก่อน → แล้ว Read ไฟล์เอกสารใหม่เพื่อยืนยันว่า code ที่ insert ตรงกับที่ทดสอบจริง (ตรวจ copy-paste error)
3. ถ้าไม่ผ่าน → แก้จนผ่านก่อน ห้าม insert code ที่ fail
4. ถ้าทดสอบไม่ได้ → ใช้ label "ยังไม่ได้ทดสอบ" ได้เฉพาะ code ที่ต้องการ external service จริง (database, API key, cloud resource, หรือ resource ที่ไม่พร้อมใช้ใน sandbox เช่น GUI display, filesystem path เฉพาะ, localhost service) — ห้ามใช้กับ self-contained code ที่รันได้ใน sandbox
   ("sandbox" = bash environment ของ Claude agent เอง — รัน Python, Node.js, shell commands ได้โดยไม่ต้องการ external service — self-contained code คือ code ที่ import เฉพาะ standard library หรือ package ที่ติดตั้งไว้แล้ว และไม่ต้องการ network, GUI, หรือ filesystem path พิเศษ)
   ⚠️ **ภาษาที่อาจไม่มี runtime ใน sandbox** (เช่น Go, Rust, Java, Swift, Kotlin): ถ้า `go build`/`javac`/ฯลฯ หาไม่เจอ → treat เหมือน "ทดสอบไม่ได้เพราะ runtime ไม่มี" → ใช้ label "ยังไม่ได้ทดสอบ (runtime ไม่มีใน environment)" ได้ — ไม่ถือว่า "external service" แต่ยอมรับว่าทดสอบไม่ได้

⚠️ Code ที่ใช้ real website เป็น target (เช่น Playwright, HTTP client, web scraping):
- ต้องรันกับ website นั้นจริงๆ และ verify ว่า output ที่ได้ตรงกับที่ claim ในเอกสาร — ห้าม assume ว่าจะ work
- ถ้า website อาจ block automation หรือเปลี่ยน structure: เลือกใช้ target ที่ stable กว่า เช่น httpbin.org, jsonplaceholder.typicode.com, example.com สำหรับ demo — หรือใช้ mock server แทน
- ถ้าบังคับต้องใช้ real website: ระบุ `# tested: [วันที่] — เว็บอาจเปลี่ยน structure` และทดสอบจริงก่อน insert
- ห้ามสร้าง code example ที่ใช้ website จริงโดยไม่ได้ทดสอบจริง แม้จะ "น่าจะทำงานได้"

⚠️ **STEP 4 → STEP 5 transition**: เมื่อ code ทุกชิ้นในทุกไฟล์ผ่านหรือได้รับ label ถูกต้องครบแล้ว → ดำเนิน STEP 5 ทันที

---

## STEP 5 — VERIFY CHECKLIST

⚠️ ทำ checklist โดยการ Read ไฟล์และ quote ทันทีก่อน tick แต่ละข้อ — ห้าม Read ไฟล์ทั้งหมดพร้อมกันก่อนแล้วค่อย tick จากความจำ (pattern ที่ห้าม: Read all → tick 12 ข้อ = invalid)
สำหรับทุกข้อ: Read → quote snippet (อย่างน้อย 1-2 บรรทัดที่เกี่ยวข้อง) + filename → tick — ถ้าไม่มี output snippet = ข้อนั้น invalid = treat as fail ต้อง Read ไฟล์และ quote ใหม่ก่อน tick
⚠️ Quote ที่ valid ต้องแสดงให้เห็นชัดว่า checklist item นั้นครบจริง — ตัวอย่าง: checklist item "Self-check มี remediation direction" → quote ต้องเป็นบรรทัด "ถ้าตอบไม่ได้: อ่าน [section]..." จากไฟล์นั้น ไม่ใช่แค่ quote ชื่อ section หรือ heading อย่างเดียว — quote header เพียงอย่างเดียวโดยไม่มีเนื้อหาที่ตรวจจริง = invalid

⚠️ **Blocking gate**: ถ้า checklist item ใด fail → ต้องแก้ไขให้ผ่านก่อน proceed STEP 6 ห้าม proceed โดยมี fail อยู่ (เช่น ถ้า Intermediate domain เหมือนตัวอย่างในบท → กลับแก้ก่อน) — หลังแก้แล้ว ต้อง **re-check checklist ทั้งหมดอีกครั้ง** ไม่ใช่แค่ item ที่แก้ (เพราะการแก้บางอย่างอาจสร้าง fail ใหม่ใน item อื่น) — proceed STEP 6 ได้เมื่อ checklist ทั้งหมด pass ในรอบเดียวกัน

- [ ] ทุกบทมี mandatory sections ครบ (บทที่ 1: sections 1,2,3,4,5,6 — 6 mandatory; บทที่ 2+: sections 1,2,3,4,5,6,7 — 7 mandatory) → verified in: [list]
- [ ] เนื้อหาหลัก (section 3) ระบุ best practice / popular approach ชัดเจนเมื่อมีหลาย approach — ไม่ทิ้งให้ผู้เรียนเลือกเองโดยไม่มีข้อมูล (ยกเว้น: ถ้า official source ไม่มี consensus → ใช้ no-consensus format ระบุ use case ของแต่ละ approach ชัดเจน = ผ่าน) → verified in: [list]
- [ ] Pre-chapter Retrieval (บทที่ 2+) มีอย่างน้อย 1 ข้อที่เชื่อม concept บทก่อนกับบทปัจจุบัน — ไม่ใช่แค่ถาม definition ซ้ำ → verified in: [list]
- [ ] วัตถุประสงค์ทุกข้อใช้ action verb ที่วัดได้ (ไม่มี "เข้าใจ"/"รู้จัก") และ verb เหมาะสมกับ level ของ series (beginner=อธิบาย/ระบุ/ยกตัวอย่าง, intermediate=ประยุกต์/เปรียบเทียบ/แก้ปัญหา, advanced=ออกแบบ/วิเคราะห์/ประเมิน — verb ต่ำกว่าหรือสูงกว่า level อย่างไม่สมเหตุ = -3) → verified in: [list]
- [ ] Why section (section 2) ทุกบทอธิบายปัญหาด้วย vocabulary จาก prerequisites เท่านั้น — ไม่ใช้ vocabulary จาก concept ที่กำลังจะสอน (ทดสอบ: อ่าน Why section โดยสมมติไม่รู้ concept ในบทนั้น → เข้าใจปัญหาได้ไหม?) → verified in: [list]
- [ ] Self-check ⏸ ทุกจุดมีเฉลยสั้น + remediation direction ("ถ้าตอบไม่ได้: อ่าน [section]") + novice fallback ("ถ้าระบุจุดที่เข้าใจผิดไม่ได้: อ่าน section ทั้งหมดใหม่") + instruction ให้ผู้เรียนเขียนเหตุผลสั้นๆ ว่าทำไมถึงตอบแบบนั้น (Generation effect element 3) → verified in: [list]
- [ ] Retrieval Questions ท้ายบทมีเฉลย + remediation path + Generation effect instruction ("หยุดคิดอย่างน้อย 30 วินาที ... เขียนเหตุผลสั้นๆ 1-2 ประโยคว่าทำไมถึงตอบแบบนั้น ... แล้วค่อยดูเฉลย") + อย่างน้อย 1 ข้อ application-level + อย่างน้อย 1 ข้อ elaborative interrogation ("ทำไม" หรือ "จะเกิดอะไรถ้า") → verified in: [list]
- [ ] overview มี Prior Knowledge Activation + prerequisites + clickable TOC + reading schedule ที่มี multi-session (chapter-level: 2วัน→1สัปดาห์→1เดือน; series-level: 3วัน→1สัปดาห์) + note ว่าเป็น guideline ปรับได้ → verified in: 00-overview.md
- [ ] Holistic outcome ใน overview สอดคล้องกับ chapters จริง — ผู้เรียนที่อ่านครบทุกบทและทำ exercises ครบจะทำสิ่งที่ outcome ระบุได้จริง (backward alignment) → verified in: 00-overview.md
- [ ] exercises มี 3 ข้อ/concept ถูก cognitive type (Recall/Application/Synthesis) + Mixed Review + เฉลยครบ → verified in: exercises.md
- [ ] Mixed Review มีอย่างน้อย 1 ข้อ discrimination task ชัดเจน (สถานการณ์ที่ concept หลายตัวอาจดูใช้ได้ — ผู้เรียนต้องเลือกและอธิบายเหตุผลที่ไม่เลือกตัวอื่น) → verified in: exercises.md
- [ ] exercises แต่ละกลุ่มมี Timing instructions ระบุว่า "ทำหลังอ่านบทที่ X" หรือ "ทำได้ระหว่างอ่าน" ไหม → verified in: exercises.md
- [ ] Mixed Review มี note อธิบาย interleaving cost ("รู้สึกยากกว่าคือสัญญาณที่ดี") ไหม → verified in: exercises.md
- [ ] exercises ไม่มี fill-in-the-blank หรือ copy context/ตัวเลขจากตัวอย่างในบท (ทุก level) + Intermediate ใช้ต่าง domain จริง → verified in: exercises.md
- [ ] glossary ครอบคลุมทุกคำที่ introduce ครั้งแรก + มี SOURCE URL กำกับทุกคำ + มี "First seen in: [ชื่อไฟล์ + section]" ทุกคำ + มี "อย่าสับสนกับ [คำที่คล้าย] หรือ 'ไม่มีคำที่คล้ายใน series นี้'" ทุกคำ (ห้ามละทิ้ง field นี้) → verified in: glossary.md
- [ ] ทุก concept ที่มาจาก community source ([COMMUNITY] prefix ใน source-notes) มี callout "> ข้อมูลส่วนนี้มาจาก community source — verify ก่อนใช้งานจริง" ในบทที่เกี่ยวข้อง → verified in: [list]
- [ ] code รัน test ผ่านแล้ว หรือ label ถูกต้อง (รวม exercises.md) → verified in: [list]
- [ ] Beginner code examples มี inline comments อธิบาย "ทำไม" (reasoning/why) ไม่ใช่แค่ "ทำอะไร" (what) → verified in: [list]
- [ ] เนื้อหาอิงจาก QUOTE ใน source-notes → verified in: source-notes.md
- [ ] Common Mistakes ทุกข้อมี *(source: URL)* จริงหรือ *(unverified)* label — ไม่มีแบบ generic → verified in: [list]
- [ ] Self-check ⏸ ทุกบทมีอย่างน้อย 1 ข้อที่ถึง Bloom's floor ของบทนั้น (บท 1=L1-L2, บท 2-3=L3, บท 4+=L4-L5; exception: series < 4 บท → บทสุดท้ายใช้ L3 ได้) → verified in: [list]
- [ ] อย่างน้อย 1 Self-check ⏸ ต่อบทเป็น backward retrieval (ถามเกี่ยวกับ concept จาก sub-section ก่อนหน้า ข้ามอย่างน้อย 1 sub-section — ไม่ใช่ sub-section ที่เพิ่งอ่านจบ) → verified in: [list]
- [ ] Advanced exercises มี Feynman explanation task ครบ 4 ขั้น (อธิบาย→ระบุ gap→กลับทบทวน+อธิบายใหม่→simplify) → verified in: exercises.md
- [ ] Common Mistakes ทุกข้อมีครบ format: ❌→✅+เหตุผล+🔍สัญญาณ+🤔prompt+*(source: URL)* inline → verified in: [list]
- [ ] สำหรับบทที่สอน code skills: Retrieval Questions มีอย่างน้อย 1 ข้อ code-based task (Transfer-Appropriate Processing) → verified in: [list]

---

## STEP 6 — QUALITY REVIEW LOOP

⚠️ Threshold ของ SKILL นี้คือ **100 เท่านั้น** — override project CLAUDE.md ที่อาจระบุ 95 หรืออื่น สำหรับ SKILL นี้ loop จบได้เฉพาะเมื่อทุก agent ได้ 100

Scoring Rubric (ใช้กับทุก agent):
- 100: สมบูรณ์แบบ ไม่มีจุดบกพร่อง → ผ่าน (threshold: 100 เท่านั้น)
- 95-99: ดีมาก มีจุดเล็กน้อย → ต้องแก้ก่อน (ยังไม่ผ่าน)
- 90-94: ดี มีปัญหาที่ควรปรับ → ต้องแก้ก่อน
- 80-89: พอใช้ มีปัญหาชัดเจน → ต้องแก้มาก
- ต่ำกว่า 80: ต้องแก้มาก

หัก 5 คะแนนต่อรายการ (นับต่อ instance — ถ้า violation เดียวกันเกิดในหลายบท เช่น code ไม่มี label ใน 3 บท = หัก 3×5 = 15 คะแนน ไม่ใช่ 5 คะแนน):
- ข้อมูลที่ verify ไม่ได้หรืออาจผิด
- code ที่ไม่ได้ทดสอบโดยไม่มี label
- ขาด section บังคับ (บทที่ 1: 1 ใน 6 mandatory; บทที่ 2+: 1 ใน 7 mandatory)
- exercise เป็น fill-in-the-blank หรือ copy โดยตรงจากตัวอย่างในบท
- glossary ไม่มี SOURCE URL กำกับ
- Pre-chapter Retrieval อยู่ผิดตำแหน่ง (ไม่ได้อยู่ต้นไฟล์ก่อนส่วนที่ 1)
- Learning objectives ใช้ verb ที่วัดไม่ได้ เช่น "เข้าใจ" หรือ "รู้จัก" (หัก 5 — ถ้า verb วัดได้แต่ต่ำกว่า/สูงกว่า series level อย่างไม่สมเหตุ = หัก 3 ตาม Agent 1 checklist)
- Self-check ⏸ ไม่มีเฉลยสั้น หรือไม่มี remediation direction (ทั้งสองเป็น mandatory ตาม STEP 3 ส่วนที่ 3)
ทุก violation จาก STEP 5 checklist ที่ไม่ได้ระบุใน list ด้านบน = หัก 3 คะแนนต่อ item

⚠️ Anti-inflation protocol — ทุก agent ต้องทำตามลำดับนี้ก่อนให้คะแนน:
1. Read ไฟล์เนื้อหาจริงทีละ section
2. สำหรับทุก checklist item: quote ข้อความจากไฟล์ที่ยืนยัน + ระบุ filename
3. ระบุ deductions ทั้งหมดพร้อมหลักฐาน quote ก่อน คำนวณคะแนน
4. ถ้าให้ 100: แนบ verification log แสดงว่าตรวจ checklist item ใดจากไฟล์ใด
ห้ามให้คะแนนก่อนทำ step 1-3 ครบ — ห้ามหักคะแนนโดยไม่มีหลักฐาน

⚠️ **Anti-double-penalty rule**: ถ้า Agent 1 หักคะแนนสำหรับ item X แล้ว → Agent 3 ห้ามหักซ้ำในมิติเดิม — Agent 1 = presence/completeness; Agent 3 = quality/learning effectiveness — ถ้าไม่แน่ใจว่า overlap หรือไม่ → ระบุ "may overlap with Agent 1 deduction" ห้ามหัก -5 ซ้ำ ใช้ -3 เท่านั้น

⚠️ เมื่อ launch Task agents: ต้อง include actual resolved path ของ docs folder (ไม่ใช่ raw '$ARGUMENTS') ใน prompt ของแต่ละ agent โดยตรง เพราะ sub-agents เป็น independent context ที่ไม่มี access ต่อ variable นี้

Launch 3 agents พร้อมกันผ่าน Task tool:
(Task tool = Claude Code's built-in parallel agent capability — ใช้ Task tool ใน Claude Code environment เหมือนกับที่ใช้ใน SKILL นี้เอง)
ถ้า Task tool ไม่พร้อมใช้งาน (Task tool unavailable) → แจ้ง user ว่า "กำลังใช้ sequential review mode แทน parallel agents" แล้วทำ sequential fallback: main agent review ทั้ง 3 dimension ด้วยตัวเองตามลำดับ โดยต้องแสดง output ของแต่ละ dimension ออกมาก่อนก่อนเดินต่อ — ห้ามทำรวม 3 dimension ในครั้งเดียว:
1. Completeness review → แสดง score + issue list → แก้ไข
2. Accuracy review → แสดง score + issue list → แก้ไข
3. Learning Quality review → แสดง score + issue list → แก้ไข
ถ้า dimension ใดได้คะแนนต่ำกว่า 100 ใน sequential mode → แก้ไขและทำ dimension นั้นซ้ำก่อน จากนั้นค่อย proceed dimension ถัดไป — ไม่ต้องทำทั้ง 3 dimension ซ้ำ — แต่ละ dimension ทำซ้ำได้ไม่เกิน 3 รอบ (initial + 2 revision) ถ้า fail หลังรอบที่ 3 → แจ้ง user พร้อม feedback ทั้งหมดและ options เหมือน per-agent escalation
⚠️ Global cap ใน sequential mode: ใช้ adaptive_cap เดียวกับ parallel mode (max(6, ceil(N_chapters × 0.75)) — ถ้ายังไม่ได้คำนวณ N_chapters: นับจากจำนวนไฟล์ 01-*.md ใน output folder) — ถ้า total dimension-runs เกิน adaptive_cap → escalate ทันที
⚠️ Double-penalty reconciliation ใน sequential mode: logic เดียวกับ parallel mode (Anti-double-penalty rule ด้านบน) — ก่อน report คะแนน main agent ต้อง cross-check ว่ามี item ใดถูกหักทั้งใน Completeness และ Learning Quality — ถ้ามี: ใช้ deduction สูงสุดเพียงครั้งเดียว ไม่นับซ้ำ
⚠️ Agent Discussion Protocol ยังคง apply ใน sequential mode — ถ้า main agent disagree กับ feedback ข้อใดใน dimension ใด → ต้องทำ Dispute Round ก่อนแก้ (ดู "Agent Discussion Protocol" ด้านล่าง) — ห้ามแก้หรือ ignore โดยไม่ผ่าน protocol นี้แม้จะ run sequential

⚠️ Loop cap (2 independent triggers — whichever hits first escalates):
- Per-agent cap: นับต่อ agent — initial run = รอบ 1 ถ้า agent นั้น fail แล้วแก้และ re-run อีก 2 รอบ (รวมเป็นรอบที่ 3) ยังไม่ผ่าน → escalate agent นั้นทันที
- Global cap (adaptive): นับรวมทุก agent — initial parallel run ของ 3 agents = 3 รอบ
  **adaptive_cap = max(6, ceil(N_chapters × 0.75))** โดยที่ N_chapters = จำนวนไฟล์ 01-*.md ถึง 0X-*.md ใน output folder (ไม่นับ 00-overview, exercises, glossary)
  ตัวอย่าง: series 3 บท → cap = max(6, 3) = 6 | series 8 บท → cap = max(6, 6) = 6 | series 10 บท → cap = max(6, 8) = 8 | series 14 บท → cap = max(6, 11) = 11
  ถ้ารวมทุก agent เกิน adaptive_cap แล้วยังไม่ผ่านครบ → หยุดและ escalate ทันที แม้ไม่ครบ per-agent cap
  ตัวอย่างการนับ (series 5 บท, cap=6): initial 3 agents (รวม=3) → Agent 2 fail → re-run Agent 2 (รวม=4) → Agent 3 fail → re-run Agent 3 (รวม=5) → Agent 3 fail อีก → re-run Agent 3 รอบ 3 (รวม=6, ไม่ trigger เพราะ 6 ไม่ > 6) → Agent 3 fail อีก → รอบที่ 4 ของ Agent 3 (รวม=7, trigger global cap: > 6) → escalate ทันที
แจ้ง user พร้อม options เมื่อ escalate — ถ้า user ไม่ respond ภายใน session (async/offline) → autonomous default: accept score ล่าสุด + บันทึก MEMORY.md + หยุด process (ใช้กับทุก escalation path รวมถึง per-agent cap และ global cap)
⚠️ **Post-escalation counter behavior**: ถ้า user อนุมัติให้ทำต่อหลัง escalate (เช่น "ลองอีกรอบ") — per-agent counter **ไม่ reset** ยังคงนับต่อจากรอบที่ escalate; global cap ก็นับต่อ — ถ้า counter ถึง cap อีกครั้งทันที → escalate ทันทีโดยไม่ต้อง run

Agent 1 — Completeness Reviewer
⚠️ ต้อง quote หลักฐานจากไฟล์จริงก่อนตรวจทุกข้อ — ห้าม tick จากความจำ
อ่านเอกสารทั้งหมดใน docs/[topic]/ ตรวจ:
- ทุกบทมีครบ mandatory sections ไหม: บทที่ 1 = 6 mandatory (วัตถุประสงค์, Why, เนื้อหาหลัก, ตัวอย่าง 3 ระดับ, Common Mistakes, Retrieval Questions — ไม่มี Pre-chapter Retrieval), บทที่ 2+ = 7 mandatory (เดิม + Pre-chapter Retrieval)
- Pre-chapter Retrieval อยู่ต้นไฟล์ก่อนส่วนที่ 1 ไหม + เชื่อม concept บทก่อนกับบทปัจจุบัน (ไม่ใช่แค่ถาม definition ซ้ำ)
- วัตถุประสงค์ทุกข้อใช้ action verb ที่วัดได้ (ไม่มี "เข้าใจ"/"รู้จัก") ไหม + verb เหมาะสมกับ level ของ series ไหม: beginner = อธิบาย/ระบุ/ยกตัวอย่าง, intermediate = ประยุกต์/เปรียบเทียบ/แก้ปัญหา, advanced = ออกแบบ/วิเคราะห์/ประเมิน — verb วัดได้แต่ต่ำกว่า/สูงกว่า series level อย่างไม่สมเหตุ → หัก 3
- overview มี Prior Knowledge Activation + prerequisites + TOC + reading schedule (chapter-level: 2 วัน → 1 สัปดาห์ → 1 เดือน; series-level: 3 วัน → 1 สัปดาห์) + holistic outcome ที่ backward-aligned กับ chapters จริง ไหม
- แต่ละบทมีไม่เกิน 3-5 concept ไหม (concept ซับซ้อน = ≤ 3)
- Self-check ⏸ ทุกจุดมีเฉลยสั้น + remediation direction + novice fallback ไหม (ทั้งสามเป็น mandatory) + instruction ให้ผู้เรียนเขียนเหตุผลสั้นๆ ว่าทำไมถึงตอบแบบนั้น (Generation effect element 3) + อย่างน้อย 1 ⏸ ต่อบทเป็น backward retrieval (ถามเกี่ยวกับ concept จาก sub-section ก่อนหน้า ข้ามอย่างน้อย 1 sub-section) + อย่างน้อย 1 ⏸ ต่อบทถึง Bloom's floor (บท 1=L1-L2, บท 2-3=L3, บท 4+=L4-L5; exception: series < 4 บท → บทสุดท้ายใช้ L3 ได้)
- Retrieval Questions มีเฉลย + remediation path + Generation effect instruction (หยุดคิดอย่างน้อย 30 วินาที + เขียนคำตอบก่อนดูเฉลย + เขียนเหตุผลว่าทำไมถึงตอบแบบนั้น) + อย่างน้อย 1 ข้อ elaborative interrogation ("ทำไม" หรือ "จะเกิดอะไรถ้า") + สำหรับบทที่สอน code skills: อย่างน้อย 1 ข้อ code-based task ไหม
- Common Mistakes ทุกข้อมีครบ format: ❌→✅+เหตุผล+🔍สัญญาณ+🤔metacognitive prompt+*(source: URL)* inline ไหม
- exercises มีครบ 3 ระดับ cognitive + Mixed Review + เฉลยไหม — Intermediate ใช้ต่าง domain (ไม่ใช่แค่เปลี่ยนชื่อตัวแปร) + Mixed Review มีอย่างน้อย 1 ข้อ discrimination task + Mixed Review มี note อธิบาย interleaving cost ("รู้สึกยากกว่าคือสัญญาณที่ดี") + ทุกกลุ่ม exercises มี Timing instructions ("ทำหลังอ่านบทที่ X") + ไม่มี fill-in-the-blank ทุก level + ไม่ copy context/ตัวเลขจากตัวอย่างในบท (ทุก level) + Advanced มี Feynman task ครบ 4 ขั้น (อธิบาย→ระบุ gap→กลับทบทวน+อธิบายใหม่→simplify)
- glossary ครอบคลุมคำที่ introduce ครั้งแรก + มี SOURCE URL กำกับทุกคำ + มี "First seen in: [ชื่อไฟล์ + section]" ทุกคำ + มี "อย่าสับสนกับ [คำที่คล้าย] หรือ 'ไม่มีคำที่คล้ายใน series นี้'" ทุกคำ ไหม
- Beginner examples มี inline comments อธิบาย "ทำไม" (reasoning/why) ไม่ใช่แค่ "ทำอะไร" (what) ไหม (Worked Example Effect)
- Why section (section 2) ใช้แค่ vocabulary จาก prerequisites ไหม — ไม่ใช้ vocabulary ใหม่จาก concept ที่บทนี้กำลังจะสอน (circular dependency) — ทดสอบ: อ่าน Why section โดยสมมติว่าไม่รู้ concept ในบทนั้นเลย → เข้าใจปัญหาที่อธิบายได้ไหม? ถ้าไม่ได้ → หัก 3 คะแนน
- เนื้อหาหลัก (section 3) ระบุ best practice / popular pattern ชัดเจน **และ** เหตุผลสั้นๆ เมื่อมีหลาย approach ไหม — ไม่ทิ้งให้ผู้เรียนเลือกเองโดยไม่มีข้อมูล (หัก 3 ต่อบทที่พบ — Agent 3 ห้ามหักซ้ำในมิตินี้) ยกเว้น: ถ้า official source ไม่มี consensus → ใช้ no-consensus format (ระบุ use case ของแต่ละ approach ชัดเจน) = ผ่าน ไม่หัก
ให้คะแนน 0-100 ตาม rubric + bullet list จุดแก้ไขพร้อมระบุไฟล์

Agent 2 — Accuracy Reviewer
⚠️ **Revision mode efficiency**: ถ้านี่คือ revision round (ไม่ใช่ initial run) — ให้ re-fetch เฉพาะ URL ที่มี verdict mismatch/outdated/unverifiable จาก round ก่อน เท่านั้น (ข้าม URL ที่มี verdict match/[STALE-URL] แล้ว) — re-run เฉพาะ code ที่ fail จาก round ก่อนเท่านั้น (ข้าม code ที่ pass แล้ว) — ประหยัด token สูงสุด 80% สำหรับ series ขนาดใหญ่

**Initial run:**
1. อ่าน source-notes ดู URL ทั้งหมด
2. Fetch URL เหล่านั้นใหม่จริงๆ
3. สำหรับแต่ละ URL ที่ fetch: แสดง (a) URL ที่ fetch, (b) QUOTE จาก source-notes, (c) ข้อความที่พบใน URL ปัจจุบัน, (d) verdict: match / mismatch / [OUTDATED-UPDATE] / [STALE-URL] / [UNVERIFIABLE]
   ⚠️ Retry cap per URL: สูงสุด 1 WebFetch ซ้ำ + ไม่เกิน 2 fallback methods (รวม 3 operations ต่อ URL) — ถ้ายัง verify ไม่ได้หลัง 3 operations → verdict: [UNVERIFIABLE]
   ⚠️ ถ้า URL ส่งคืน 404: WebSearch หา URL ใหม่ก่อน — ถ้าพบ URL ใหม่ที่มีเนื้อหาเดิม → ใช้ URL ใหม่ confirm QUOTE และ verdict ตามปกติ — ถ้าหาไม่เจอ → verdict: [STALE-URL] (URL เปลี่ยนเพราะ docs restructure) — หมายเหตุ: [STALE-URL] ไม่หักคะแนน (ไม่ใช่ความผิดของ author เอกสาร) ต่างจาก QUOTE mismatch ที่หัก 5 คะแนน
   ⚠️ ถ้า QUOTE valid ตอน source-notes สร้าง แต่ source update ระหว่างกระบวนการ → verdict: [OUTDATED-UPDATE] + หัก 3 คะแนน (ไม่ใช่ 5 เพราะ QUOTE valid ตอนสร้าง docs ต่างจาก "ข้อมูลที่ verify ไม่ได้ตั้งแต่แรก") + ระบุว่าเนื้อหาส่วนใดในเอกสารต้องแก้
   ⚠️ JS-rendered docs exception: ถ้า WebFetch คืน HTML ว่างเปล่าหรือ loading placeholder → ลอง (1) fetch raw GitHub source ของ docs repo แทน หรือ (2) search ด้วย "[exact phrase from QUOTE] site:[docs-domain]" — ถ้ายังไม่เจอ → verdict: [UNVERIFIABLE]
   ⚠️ Session-level cap: ถ้า UNVERIFIABLE ≥ 50% ของ URL ที่ตรวจ → บันทึกใน report ว่า "⚠️ Accuracy re-verification ไม่สมบูรณ์" + proceed ต่อโดยอัตโนมัติ
4. รัน code examples ทั้งหมด (รวม exercises.md) ด้วย Bash
ถ้าพบ discrepancy: ระบุว่าเกิดจาก docs update หรือ QUOTE ผิดตั้งแต่ต้น
ถ้าเอกสารมี explicit descriptions ของ learning science mechanisms (เช่น อธิบาย mechanism ของ Feynman Technique, spacing effect, retrieval practice ฯลฯ) → ตรวจว่า description ถูกต้องตาม evidence เหมือน content claims อื่นๆ — ถ้าอธิบาย mechanism ผิด → หัก 5 คะแนน
ให้คะแนน 0-100 ตาม rubric + bullet list จุดแก้ไขพร้อมระบุไฟล์และข้อผิดพลาด

Agent 3 — Learning Quality + Practitioner Reviewer
อ่านเอกสารทั้งหมดในฐานะ practitioner ที่ใช้ topic นี้ใน production จริง (infer topic จากเนื้อหาเอกสาร) ตรวจ:

⚠️ Practitioner lens (ใช้กับทุกข้อด้านล่าง — ตั้งคำถามผ่านมุมมองคนที่ใช้งานจริง):
- ตัวอย่างและ Common Mistakes สะท้อน real-world usage ไหม (ไม่ใช่แค่ toy examples)
- มี gotcha ที่คนทำงานจริงรู้แต่ docs ไม่บอกไหม
- Advanced exercise ท้าทายพอสำหรับ production use ไหม
- เมื่อ topic มีหลาย approach: เนื้อหาหลัก (section 3) ระบุ best practice / popular pattern และเหตุผลไหม — ถ้าไม่ระบุ preference ปล่อยให้ผู้เรียนเลือกเองโดยไม่มีข้อมูล → หัก 3 คะแนน (ถ้า Agent 1 หักแล้วในมิตินี้ ห้ามหักซ้ำ ระบุ "may overlap with Agent 1") ยกเว้น: ถ้า official source ไม่มี consensus → ใช้ no-consensus format (ระบุ use case ของแต่ละ approach ชัดเจนว่าเหมาะกับกรณีใด) = ผ่าน ไม่หัก
- เข้าใจง่ายไหม ไหลลื่นไหม ลำดับ Why→What→How→Apply ชัดไหม
- Pre-chapter Retrieval อยู่ต้นไฟล์ก่อนเนื้อหาไหม + มี `---` separator + instruction ให้เขียนก่อน scroll ไหม
- Retrieval Questions มีอย่างน้อย 1 ข้อ application-level + 1 ข้อ elaborative interrogation ("ทำไม" หรือ "จะเกิดอะไรถ้า") ไหม
- Exercises: Intermediate ใช้ต่าง domain จริงไหม, Advanced มี Feynman task ครบ 4 ขั้น (อธิบาย→ระบุ gap→กลับทบทวน+อธิบายใหม่→simplify) ไหม, มี Mixed Review ไหม
- exercises ไม่มีข้อที่เป็น fill-in-the-blank ไหม (ทุก level)
- exercises ไม่มีข้อที่ copy context หรือตัวเลขโดยตรงจาก section ตัวอย่าง (ส่วนที่ 4) ไหม (ทุก level)
- แต่ละบทมีไม่เกิน 3-5 concept ไหม
- ภาษาไทยเป็นธรรมชาติไหม ย่อหน้าสั้นพอไหม (ไม่เกิน 4-5 บรรทัด)
- Learner journey simulation: อ่าน series ทั้งหมดในฐานะผู้เรียนที่มี prerequisites ตาม STEP 0 พยายามระบุจุดที่จะรู้สึก "งง" หรือ "ข้ามขั้น" (target: 3 จุด — ถ้าหาได้น้อยกว่า 3 แสดงว่าเอกสารดีมาก ไม่ต้องบังคับ) และยืนยันว่าทุกจุดที่พบได้รับการจัดการในเอกสารแล้ว — ถ้าไม่ได้จัดการ → หัก 3 คะแนนต่อจุด
ให้คะแนน 0-100 ตาม rubric + bullet list จุดแก้ไขพร้อมระบุไฟล์

เงื่อนไข:
- ทุก agent ได้ 100 → เดินหน้า STEP 7
- ถ้า agent ใดได้ต่ำกว่า 100:
  1. Main agent ทบทวน feedback ของ agent นั้น — ระบุ "แก้อะไร ที่ไหน ด้วยเหตุผลอะไร" ก่อน re-run

  ⚠️ **Re-research path**: ถ้า feedback ระบุว่า "เนื้อหาขาดข้อมูลสำคัญ" หรือ "ไม่มี source รองรับ" (ไม่ใช่แค่ format/structure issue) → กลับ STEP 1 เพื่อ fetch ข้อมูลเพิ่ม → update source-notes → แก้เนื้อหา → re-run review agent เดิม — **ไม่นับ STEP 1 loop นี้เป็น review round** เพราะเป็น research ไม่ใช่ revision
  ⚠️ **Re-research cap**: ทำได้ไม่เกิน 2 ครั้งต่อ agent ที่ fail (นับแยกต่อ agent — Agent 1, 2, 3 มีโควต้า 2 ครั้งของตัวเองแต่ละตัว) — ถ้าหลัง 2 ครั้งยัง fail เพราะ content gap เดิม → escalate user ทันที พร้อม (a) concept ที่ไม่มี source รองรับ (b) options: ตัด concept นั้น / ยอมรับ unverified label / manual research — ถ้า user ไม่ respond ภายใน session (async/offline) → autonomous default: ตัด concept ที่ไม่มี source ออก + proceed ด้วย concept ที่มี verified source เท่านั้น

  2. Run review agent นั้นใหม่ (ไม่ต้อง run ทั้ง 3 ใหม่ถ้าบางตัวผ่านแล้ว)
  3. นับรอบแยกต่างหากต่อ agent — ถ้า agent นั้น fail หลังรอบที่ 3 (initial run + 2 revision rounds) ยังไม่ผ่าน → แจ้ง user ว่าติดปัญหาอะไร พร้อม (a) feedback ทั้งหมดจาก agent นั้น, (b) options ให้ user เลือก: ลด scope / ยอมรับ score ที่ได้ / manual fix / re-research (ถ้า feedback เป็น content gap: กลับ STEP 1 เพื่อ fetch ข้อมูลเพิ่ม — ไม่นับเป็น review round)

⚠️ **Agent Discussion Protocol** — ก่อนแก้ไขทุกครั้ง main agent ต้องประเมิน feedback ก่อน:

**กรณีปกติ (feedback มีหลักฐาน quote จากไฟล์จริง + อ้างอิงได้กับ SKILL goals ที่ระบุไว้ชัดเจน):**
→ แก้ไขตาม feedback ได้เลย ไม่ต้อง arbitrate

**กรณี main agent disagree กับ feedback ข้อใดข้อหนึ่ง:**
→ ห้ามแก้หรือ ignore ทันที — ต้องทำ Dispute Round ก่อน:
Launch **Dispute Arbiter Agent** (ผ่าน Task tool) ด้วย (a) feedback ของ review agent, (b) ข้อโต้แย้ง + เหตุผล, (c) quote ส่วนที่พิพาทจากเอกสาร — **ต้อง pass Scoring Rubric และ deduction rules ทั้งหมดของ SKILL นี้ไปใน prompt ของ arbiter โดยตรง** (เพราะ arbiter เป็น independent context ที่ไม่มี access ต่อ SKILL file) → arbiter ให้ verdict → ถ้า sides with reviewer: แก้ไข; ถ้า sides with main: waive; ถ้า "unclear" หรือ main agent ยังไม่เห็นด้วยหลัง verdict → main agent ตัดสินใจเอง: ใช้ review agent's feedback เป็น default (conservative) + บันทึก "[Autonomously resolved: deferred to reviewer feedback]" ใน report ห้าม launch Arbiter ซ้ำ

**กรณี feedback จาก 2+ agents ขัดแย้งกัน** (score ต่างกัน > 10 คะแนนสำหรับ item เดียวกัน หรือ explicit contradiction):
→ Launch **Synthesis Agent** (ผ่าน Task tool) ให้อ่าน feedback ทั้งหมด + เอกสารจริง แล้วหา consensus — ถ้า Synthesis Agent conclude ไม่ได้ → escalate user — ถ้า user ไม่ respond ภายใน session (async/offline) → autonomous default: accept score ล่าสุด + บันทึก MEMORY.md + หยุด process

**ถ้า 3 agents ขัดแย้งกันหมด:** แสดง 3 positions + tradeoff แล้ว escalate user ทันที ห้ามตัดสินใจเอง — ถ้า user ไม่ respond → autonomous default: accept score ล่าสุด + บันทึก MEMORY.md + หยุด process

⚠️ Dispute cap: เปิด Dispute Arbiter ได้ไม่เกิน 3 ข้อต่อรอบ review — ถ้าต้อง dispute เกิน 3 ข้อ → escalate user แทน เพราะแปลว่ามี systematic disagreement ที่ต้องการ human judgment — ถ้า user ไม่ respond → autonomous default: accept score ล่าสุด + บันทึก MEMORY.md + หยุด process

⚠️ **Anti-false-positive check**: ก่อน main agent แก้ไขตาม deduction ที่อ้างว่า "ขาด X" หรือ "ไม่มี X" — main agent ต้อง Read ไฟล์เองและ verify ว่า X ไม่มีจริงในเอกสาร — ถ้าพบว่า X มีอยู่แล้ว → deduction นั้นเป็น false positive → waive + บันทึก "waived: false positive [issue]" → ไม่ต้องแก้ไข — ถ้า review agent ไม่ได้ quote หลักฐานที่ยืนยันว่า X ขาด (ตาม anti-inflation protocol) → main agent ต้อง verify ด้วยตัวเองก่อนเสมอ

⚠️ **Convergence check**: ถ้า agent score ยังไม่ถึง 100 **และ** เพิ่มขึ้น < 3 คะแนนระหว่าง 2 รอบติดกัน ทั้งที่แก้ไขตาม feedback แล้ว → escalate user ทันที พร้อมแสดง score history + remaining issues (อาจ converge ช้าหรือมี false positive loop) — rule นี้ trigger ก่อน per-agent cap หากพบ slow convergence — ห้าม re-run อีกรอบโดยไม่ผ่าน user approval ก่อน — ถ้า user ไม่ respond ภายใน session (async/offline) → autonomous default: accept score ล่าสุด + บันทึก MEMORY.md + หยุด process

---

## STEP 7 — CLEAN UP

(ทำหลังจากทุก agent ผ่าน 100 แล้วเท่านั้น)
ลบไฟล์ชั่วคราว:
- `docs/[topic]-source-notes.md`
- `docs/[topic-slug]-checkpoint.md`

เมื่อลบครบแล้ว → ดำเนิน STEP 8 ทันที

---

## STEP 8 — UPDATE MEMORY

Append ใน MEMORY.md ของ project ส่วน "เอกสารที่สร้างแล้ว" (ดู path จาก project CLAUDE.md หรือ memory pointer — โดยทั่วไปอยู่ที่ [project root]/MEMORY.md หรือตาม path ที่ระบุใน CLAUDE.md):
| วันที่ | หัวข้อ | Path | Completeness | Accuracy | Learning | รอบที่ผ่าน |
|--------|--------|------|--------------|----------|----------|------------|
| [วันที่] | [topic] | docs/[path]/ | [X]/100 | [X]/100 | [X]/100 | รอบที่ [N] |

(บันทึก score จาก round สุดท้ายของแต่ละ agent ไม่ใช่ average)

⚠️ ถ้า user เลือก "manual fix" หลัง escalate และไม่ invoke skill ใหม่ → MEMORY.md จะไม่ถูก update โดยอัตโนมัติ — ให้ main agent บันทึก MEMORY.md ด���วย score ล่าสุดพร้อม note "resolved manually" ทันทีก่อนหยุด process เพื่อให้มี record ว่าเอกสารสร้างแล้วแม้ยังไม่ได้ verify รอบสุดท้าย
