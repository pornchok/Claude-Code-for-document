# AI For Beginners — Interactive HTML Course Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** สร้าง interactive HTML course "เข้าใจ AI & Claude" สำหรับผู้เริ่มต้นที่ไม่มีพื้นฐาน AI เลย พร้อม animation ครบทุกบท

**Architecture:** Static HTML site ไม่ต้องใช้ server — `style.css` + `anim.js` แชร์กันทุกหน้า ทุกบทมี 8 sections ตาม CLAUDE.md standard แต่ละหน้าเป็น self-contained HTML ที่ open ใน browser ได้ทันที

**Tech Stack:** Vanilla HTML5 + CSS3 (animations, variables, grid, flexbox) + Vanilla JS (ES6 classes) — ไม่มี framework dependency, CDN: Google Fonts + Prism.js highlight

---

## File Map

| ไฟล์ | สร้าง/แก้ | หน้าที่ |
|------|-----------|---------|
| `docs/ai-for-beginners/html/style.css` | สร้าง | Shared styles + all animation classes |
| `docs/ai-for-beginners/html/anim.js` | สร้าง | JS animation components: Timeline, LLMStepper, TokenViz, FlipCard, SkillsFlow, QuizBlock |
| `docs/ai-for-beginners/html/index.html` | สร้าง | Overview, prerequisites, table of contents |
| `docs/ai-for-beginners/html/01-what-is-ai.html` | สร้าง | Ch1: AI คืออะไร + taxonomy diagram + timeline |
| `docs/ai-for-beginners/html/02-how-llm-works.html` | สร้าง | Ch2: LLM pipeline step-through + token viz |
| `docs/ai-for-beginners/html/03-prompting.html` | สร้าง | Ch3: Prompting + flip card bad/good + prompt builder |
| `docs/ai-for-beginners/html/04-claude-superpowers.html` | สร้าง | Ch4: Claude Skills system + flow animation |
| `docs/ai-for-beginners/html/05-ai-ethics.html` | สร้าง | Ch5: Hallucination, bias, responsible use |
| `docs/ai-for-beginners/html/exercises.html` | สร้าง | แบบฝึกหัดครบทุกบท (Recall/Application/Synthesis) |
| `docs/ai-for-beginners/html/glossary.html` | สร้าง | คำศัพท์ทุกคำ + ภาษาไทย |
| `docs/ai-for-beginners/source-notes.md` | สร้าง | QUOTE จาก official sources ทุก concept |

---

## Task 1: Source Notes — Research & Verify

**Files:**
- Create: `docs/ai-for-beginners/source-notes.md`

- [ ] **Step 1: สร้าง source-notes.md พร้อม verified concepts**

สร้างไฟล์ `docs/ai-for-beginners/source-notes.md` ด้วยเนื้อหาต่อไปนี้:

```markdown
# AI for Beginners — Source Notes

## Chapter 1: AI คืออะไร

SOURCE: https://www.ibm.com/topics/artificial-intelligence
CONCEPT: นิยาม AI
QUOTE: "Artificial intelligence (AI) is technology that enables computers and machines to simulate human intelligence and problem-solving capabilities."

SOURCE: https://developers.google.com/machine-learning/glossary
CONCEPT: ML vs DL
QUOTE: "Machine learning (ML) is a subfield of AI... Deep learning is a type of machine learning..."

SOURCE: https://arxiv.org/abs/1706.03762 (Attention Is All You Need)
CONCEPT: Transformer architecture
QUOTE: "We propose a new simple network architecture, the Transformer, based solely on attention mechanisms..."

## Chapter 2: LLM ทำงานอย่างไร

SOURCE: https://platform.openai.com/tokenizer
CONCEPT: Token
NOTE: Tokenization splits text into sub-word units. Thai text tokenizes differently from English.

SOURCE: https://www.anthropic.com/research
CONCEPT: Claude architecture
NOTE: Claude ใช้ Transformer-based LLM — exact architecture details not publicly disclosed.

## Chapter 3: Prompting

SOURCE: https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/overview
CONCEPT: Prompt engineering overview
QUOTE: "Prompt engineering is the process of crafting optimal prompts to get the outputs you want from Claude."

SOURCE: https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/be-clear-and-direct
CONCEPT: Clear and direct prompts
QUOTE: "Claude responds best to clear and direct instructions."

## Chapter 4: Claude Superpowers

SOURCE: Internal (skills system)
CONCEPT: Skills priority
QUOTE: "User instructions always take precedence: 1. User's explicit instructions — highest priority; 2. Superpowers skills — override default; 3. Default system prompt — lowest priority"

## Chapter 5: AI Ethics

SOURCE: https://www.anthropic.com/responsible-disclosure-policy
CONCEPT: Responsible AI use
NOTE: Anthropic's approach to responsible AI deployment

SOURCE: https://docs.anthropic.com/en/docs/test-and-evaluate/strengthen-guardrails/reduce-hallucinations
CONCEPT: Hallucination reduction
QUOTE: "Claude can sometimes generate information that sounds plausible but isn't accurate."
```

- [ ] **Step 2: Commit source notes**
```bash
git add docs/ai-for-beginners/source-notes.md
git commit -m "docs: add AI for beginners source notes and verified concepts"
```

---

## Task 2: Shared Assets — style.css

**Files:**
- Create: `docs/ai-for-beginners/html/style.css`

- [ ] **Step 1: สร้าง style.css**

สร้าง `docs/ai-for-beginners/html/style.css` (ดูเนื้อหาใน Task 2 execution section)

- [ ] **Step 2: Verify in browser**
Open `index.html` in browser — sidebar should show, content area readable, colors correct

- [ ] **Step 3: Commit**
```bash
git add docs/ai-for-beginners/html/style.css
git commit -m "feat: add shared CSS for AI for beginners course"
```

---

## Task 3: Shared Assets — anim.js

**Files:**
- Create: `docs/ai-for-beginners/html/anim.js`

- [ ] **Step 1: สร้าง anim.js พร้อม components ทั้งหมด**

Components ที่ต้องมี:
- `TimelineAnim` — horizontal AI history timeline
- `TaxonomyDiagram` — animated nested circles AI⊃ML⊃DL⊃LLM
- `LLMStepper` — step-through LLM pipeline
- `TokenViz` — token colorizer
- `FlipCard` — bad vs good prompt comparison
- `PromptBuilder` — interactive prompt structure
- `SkillsFlow` — Claude skills activation flow
- `QuizBlock` — inline knowledge check

- [ ] **Step 2: Verify JS loads without errors**
Open browser console — no errors on page load

- [ ] **Step 3: Commit**
```bash
git add docs/ai-for-beginners/html/anim.js
git commit -m "feat: add animation JS components for AI course"
```

---

## Task 4: index.html — Overview & Navigation

**Files:**
- Create: `docs/ai-for-beginners/html/index.html`

- [ ] **Step 1: สร้าง index.html**

เนื้อหา:
- Hero section: ชื่อหลักสูตร + คำอธิบาย 1 ประโยค
- Prerequisites: ไม่ต้องการพื้นฐานใดๆ
- เรียนได้อะไร: bullet list outcomes
- Table of contents: links ครบทุกบท พร้อมเวลาอ่านโดยประมาณ
- เริ่มต้น: ปุ่ม "เริ่มบทที่ 1 →"

- [ ] **Step 2: Verify navigation works**
Click each link in sidebar → goes to correct page

- [ ] **Step 3: Commit**
```bash
git add docs/ai-for-beginners/html/index.html
git commit -m "feat: add course overview index page"
```

---

## Task 5: Chapter 1 — AI คืออะไร?

**Files:**
- Create: `docs/ai-for-beginners/html/01-what-is-ai.html`

Content (8 sections):
1. วัตถุประสงค์: อธิบาย AI/ML/DL/LLM แตกต่างกันได้, เล่าประวัติได้, บอก limitations ได้
2. ทำไมต้องรู้: รู้จัก tool ก่อนใช้ → ใช้ได้ดีขึ้น ผิดหวังน้อยลง
3. Analogy: นักศึกษาที่อ่านหนังสือมาทั้งชีวิตแต่ไม่เคยออกไปข้างนอก + breakdown points
4. เนื้อหาหลัก: นิยาม, timeline (animated), taxonomy (animated)
5. ตัวอย่าง 3 ระดับ: Netflix recommend / spam filter / self-driving car
6. Common Mistakes: ❌ "AI เข้าใจเหมือนคน" / ❌ "AI ฉลาดกว่าคนทุกด้าน" / ❌ "AI มีจิตสำนึก"
7. สรุป + Retrieval 3 ข้อ

**Animations:**
- `<div data-anim="timeline">` — AI history 1950-2024
- `<div data-anim="taxonomy">` — nested circles

- [ ] **Step 1: สร้าง 01-what-is-ai.html**
- [ ] **Step 2: Verify timeline animation scrolls correctly**
- [ ] **Step 3: Commit**
```bash
git commit -m "feat: add Chapter 1 — What is AI"
```

---

## Task 6: Chapter 2 — LLM ทำงานอย่างไร?

**Files:**
- Create: `docs/ai-for-beginners/html/02-how-llm-works.html`

Content (8 sections):
1. วัตถุประสงค์: อธิบาย token/embedding/attention/generation ได้, บอกได้ว่า LLM ไม่ได้ "รู้" ข้อเท็จจริง
2. ทำไมต้องรู้: เข้าใจ mechanism → รู้ว่าทำไม AI ถึงผิด, รู้วิธี Prompt ที่ถูกต้อง
3. Analogy: autocomplete ฉลาดมากๆ ใน phone keyboard + breakdown points (ไม่ใช่แค่ copy, มีความรู้จริง)
4. เนื้อหาหลัก: Token viz → Embedding concept → Attention → Generation
5. ตัวอย่าง 3 ระดับ: text completion / translation / code generation
6. Common Mistakes: ❌ "LLM ค้นหาข้อมูลจาก internet ตลอดเวลา" / ❌ "LLM จำทุกอย่างไว้" / ❌ "context window = memory"
7. Pre-chapter Retrieval (จากบทที่ 1) + สรุป + Retrieval 3 ข้อ

**Animations:**
- `<div data-anim="token-viz" data-text="สวัสดี ฉันชื่อ Claude">` — token colorizer
- `<div data-anim="llm-stepper">` — pipeline step-through

- [ ] **Step 1: สร้าง 02-how-llm-works.html**
- [ ] **Step 2: Verify LLM stepper steps through correctly**
- [ ] **Step 3: Commit**
```bash
git commit -m "feat: add Chapter 2 — How LLM Works"
```

---

## Task 7: Chapter 3 — Prompting

**Files:**
- Create: `docs/ai-for-beginners/html/03-prompting.html`

Content (8 sections):
1. วัตถุประสงค์: เขียน prompt ที่มีโครงสร้าง, ใช้ few-shot examples, ใช้ chain-of-thought ได้
2. ทำไมต้องรู้: AI ฉลาดแค่ไหนก็ตาม ถ้า prompt แย่ ผลลัพธ์แย่
3. Analogy: การสั่งงานพนักงานใหม่ — ยิ่งบอก context มาก ยิ่งได้งานดี + breakdown points
4. เนื้อหาหลัก: Role + Context + Task + Format + Constraints framework
5. ตัวอย่าง 3 ระดับ: simple ask / structured prompt / few-shot + CoT
6. Common Mistakes: ❌ ไม่ระบุ format ที่ต้องการ / ❌ prompt ยาวเกินไปโดยไม่มีโครงสร้าง / ❌ ไม่ใส่ context
7. Pre-chapter Retrieval + สรุป + Retrieval 3 ข้อ

**Animations:**
- `<div data-anim="flip-card" data-bad="..." data-good="...">` — bad vs good prompt
- `<div data-anim="prompt-builder">` — interactive prompt structure highlighter

- [ ] **Step 1: สร้าง 03-prompting.html**
- [ ] **Step 2: Verify flip cards work on click**
- [ ] **Step 3: Commit**
```bash
git commit -m "feat: add Chapter 3 — Prompting"
```

---

## Task 8: Chapter 4 — Claude Superpowers

**Files:**
- Create: `docs/ai-for-beginners/html/04-claude-superpowers.html`

Content (8 sections):
1. วัตถุประสงค์: อธิบาย Claude Skills system ได้, invoke skill ถูกต้อง, รู้ priority order
2. ทำไมต้องรู้: default Claude ทำได้หลายอย่าง แต่ Skills เปลี่ยน HOW Claude ทำงานทั้งหมด
3. Analogy: เหมือน app ที่ install extension ได้ — Claude เป็น browser, Skills เป็น extension + breakdown
4. เนื้อหาหลัก: Skills คืออะไร, วิธี invoke, priority (user>skill>default), skills ที่มี
5. ตัวอย่าง 3 ระดับ: invoke brainstorming / TDD skill workflow / custom skill creation
6. Common Mistakes: ❌ คิดว่า skills เป็น Claude default / ❌ ไม่รู้ว่า user override ได้ / ❌ invoke ผิด skill
7. Pre-chapter Retrieval + สรุป + Retrieval 3 ข้อ

**Animations:**
- `<div data-anim="skills-flow">` — user → invoke → skill → claude → result

- [ ] **Step 1: สร้าง 04-claude-superpowers.html**
- [ ] **Step 2: Verify skills flow animates step by step**
- [ ] **Step 3: Commit**
```bash
git commit -m "feat: add Chapter 4 — Claude Superpowers"
```

---

## Task 9: Chapter 5 — AI Ethics & Safety

**Files:**
- Create: `docs/ai-for-beginners/html/05-ai-ethics.html`

Content (8 sections):
1. วัตถุประสงค์: อธิบาย hallucination/bias ได้, รู้วิธี verify ข้อมูล AI, ตัดสินใจได้ว่าเมื่อไรใช้ AI เหมาะ
2. ทำไมต้องรู้: AI มีข้อจำกัดที่ต้องรู้ไว้ก่อน ไม่อย่างนั้น trust มากเกินไป
3. Analogy: AI เหมือน Google Maps — มีประโยชน์มาก แต่ต้องดูถนนจริงด้วย + breakdown
4. เนื้อหาหลัก: Hallucination, Bias ใน training data, Privacy, เมื่อไรใช้/ไม่ใช้ AI
5. ตัวอย่าง 3 ระดับ: fact-check AI output / bias in hiring AI / medical AI limitations
6. Common Mistakes: ❌ copy ข้อมูล AI ไปใช้โดยไม่ check / ❌ ใส่ข้อมูลส่วนตัวใน prompt / ❌ ใช้ AI ตัดสินใจสำคัญคนเดียว
7. Pre-chapter Retrieval + สรุป + Retrieval 3 ข้อ

**Animations:**
- `<div data-anim="quiz" ...>` — knowledge checks inline

- [ ] **Step 1: สร้าง 05-ai-ethics.html**
- [ ] **Step 2: Verify quiz blocks work**
- [ ] **Step 3: Commit**
```bash
git commit -m "feat: add Chapter 5 — AI Ethics & Safety"
```

---

## Task 10: exercises.html

**Files:**
- Create: `docs/ai-for-beginners/html/exercises.html`

- [ ] **Step 1: สร้าง exercises.html**

แบบฝึกหัดทุกบท (≥3 ข้อต่อบท):
- **Recall** (อธิบายด้วยคำตัวเอง)
- **Application** (สถานการณ์ใหม่ที่ไม่มีในตัวอย่าง)
- **Synthesis** (หา bug, เปรียบเทียบ tradeoff, ออกแบบระบบ)

ห้าม fill-in-the-blank, ห้าม copy context จากตัวอย่างในบท

พร้อมเฉลยแบบ collapsible (click เพื่อดู)

- [ ] **Step 2: Commit**
```bash
git commit -m "feat: add exercises for all chapters"
```

---

## Task 11: glossary.html

**Files:**
- Create: `docs/ai-for-beginners/html/glossary.html`

- [ ] **Step 1: สร้าง glossary.html**

คำศัพท์ทั้งหมด (≥30 คำ) พร้อม:
- ชื่อ English
- คำอธิบายภาษาไทย (2-3 ประโยค)
- บทที่เกี่ยวข้อง (link)
- Source URL

- [ ] **Step 2: Commit**
```bash
git commit -m "feat: add glossary with all key terms"
```

---

## Task 12: Quality Review

- [ ] **Step 1: Invoke quality-audit skill**
ใช้ skill `quality-audit` เพื่อ review ความถูกต้องของเนื้อหาทุกบท

- [ ] **Step 2: แก้ไขตาม feedback ของ quality-audit**

- [ ] **Step 3: Final commit**
```bash
git add -A
git commit -m "docs: AI for beginners course complete — passed quality audit"
```

- [ ] **Step 4: Update MEMORY.md**
เพิ่มแถวใหม่ใน MEMORY.md table

---

## Self-Review Checklist

- [x] ทุก task ครอบคลุม spec: 5 chapters + exercises + glossary + review
- [x] ไม่มี TBD/TODO ใน plan
- [x] ทุก chapter มีครบ 8 sections ตาม CLAUDE.md
- [x] Animations ระบุชัด: ชื่อ component + data attributes
- [x] Commits หลังทุก task
- [x] Review agent อยู่ใน Task 12
