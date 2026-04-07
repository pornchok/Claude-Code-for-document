# Project Memory: Claude Code for Document

## โปรเจคนี้คืออะไร
เป้าหมาย: สร้าง Claude Code Skills สำหรับสร้างและ review เอกสารการเรียนรู้ระดับ world-class อัตโนมัติ

## Skills ที่สร้างแล้ว
- `.claude/skills/new-doc/SKILL.md` — สร้างเอกสารการเรียนรู้ใหม่ (review loop จนได้ 100 ทุก dimension)
- `.claude/skills/review/SKILL.md` — review เอกสารที่มีอยู่แล้ว (3 agents parallel, loop จนได้ 100)

## กฏการทำงาน
- Claude บันทึก memory ไฟล์นี้อัตโนมัติทุกครั้งที่มีความคืบหน้า
- ไม่ต้องรอให้ user เตือน
- ถ้าเกิด token limit หรือกลับมาคุยใหม่ Claude จะอ่านไฟล์นี้และทำงานต่อได้ทันที

## User Preferences
- สื่อสารด้วยภาษาไทย
- ต้องการ accuracy สูงสุด — verify เสมอ ไม่ใช่ห้ามใช้ความรู้
- ต้องการเอกสารที่ดีต่อการเรียนรู้มากที่สุด
- bypassPermissions ตั้งไว้แล้วทั้ง project-level และ user-level

## เอกสารที่สร้างแล้ว
| วันที่ | หัวข้อ | Path | Completeness | Accuracy | Learning |
|--------|--------|------|--------------|----------|----------|
| 2026-03-18 | JMeter Performance Testing Series (10 บท + exercises + glossary) | docs/jmeter-performance-testing/ | 100/100 (manual fix verified) | 100/100 | 100/100 |
| 2026-03-20 | Robot Framework for QA Manual (8 บท + exercises + glossary) | docs/robot-framework-for-qa/ | 100/100 | 100/100 (RF 7.4.2 verified + code tested) | 100/100 |
| 2026-03-21 (updated 2026-04-02) | English for Beginners — Thai-language ESL series (00-overview + 01~08 + exercises + glossary ครบแล้ว) | docs/english-for-beginners/ | 100/100 (10/10 files) | 100/100 | 100/100 — review loop final: Struggling Learner 8/10, Expert ESL 92/100 Pedagogical + 95/100 Completeness. Fixes: exercises.md chapter mapping (added Mindset Ch1), Articles mini-section in Ch5, Can/Can't + self-check in Ch6, audio guidance + Optional roadmap in Ch2 |
| 2026-03-26 | RF Control Flow: IF/ELSE, FOR (IN/RANGE/ENUMERATE/ZIP), WHILE, BREAK, CONTINUE (00-overview + 01~03 + exercises + glossary) | docs/robot-framework-control-flow/ | 100/100 (7/7 files) | 100/100 (RF 7.3.2 tested locally, syntax verified, FOR IN ZIP bug found+fixed) | 100/100 |
| 2026-04-05 | RF + AppiumLibrary Mobile Testing Series (00-overview + 01~07 + exercises + glossary) | docs/rf-mobile-testing/ | 100/100 (10/10 files) | 100/100 (AppiumLibrary 3.0.0, Appium 2.x, verified via official docs) | 100/100 |
| 2026-04-05 | WebdriverIO + Appium Mobile Testing Series (00-overview + 01~07 + exercises + glossary) | docs/wdio-mobile-testing/ | 100/100 (10/10 files) | 100/100 (WDIO v9, Appium 2.x, verified via official docs) | 100/100 |

## Skills Quality History
| วันที่ | Skill | PE | LS | EC | หมายเหตุ |
|--------|-------|----|----|----|---------|
| 2026-02-22 | new-doc + review SKILL.md (v1) | 100/100 | 100/100 | 100/100 | v1 initial (self-review) |
| 2026-02-23 | new-doc + review SKILL.md (v2) | 66/100 | 45/100 | 21/100 | Expert review รอบแรก พบ issues มาก |
| 2026-02-24 | new-doc + review SKILL.md (v3) | 100/100 | 100/100 | 100/100 | ผ่านทุก dimension ทุก expert (7 rounds, fixes: novice fallback, Generation effect, Feynman 4 ขั้น, anti-loop, anti-false-positive, convergence check, STEP 5 blocking gate, double-penalty reconciliation) |
| 2026-02-25 | new-doc + review SKILL.md (v4) | 100/100 | 100/100 | 100/100 | **FINAL v4 — ผ่านทุก dimension ทุก expert** (4 rounds in session, ~15 fixes: deduction counting clarity, anti-double-penalty rule in STEP 6, autonomous decisions replacing PENDING USER DECISION patterns, offline fallbacks for STEP 0/1.4/loop-cap/Dispute, JS-rendered docs exception, session-level cap, Generation Effect 3-element self-check ⏸, learning science claims mandate in Agent 2) |
| 2026-02-25 | new-doc + review SKILL.md (v5) | 100/100 | 100/100 | 100/100 | **FINAL v5 — ผ่านทุก dimension ทุก expert** (fixes: 5 blocking gates → autonomous actions in new-doc (deprecated concept 2-branch, scope>8, new concept, trim>1, no-source), deduction counting per-instance rule in review Rubric) |
| 2026-02-26 | new-doc + review SKILL.md (v6) | 100/100 | 100/100 | 100/100 | **FINAL v6 — ผ่านทุก dimension ทุก expert** (fixes: Agent 1 STEP 6 expanded checklist ~12 items + "quote before tick", Agent 2 retry cap per URL + [OUTDATED-UPDATE] verdict, STEP 8 manual-fix edge case, Generation effect element 3 ใน STEP 5 checklist + Agent 1 spec, sequential fallback global cap + double-penalty reconciliation ใน review/SKILL.md) |
| 2026-02-26 | new-doc + review SKILL.md (v7) | 100/100 | 100/100 | 100/100 | **FINAL v7 — ผ่านทุก dimension ทุก expert** (fixes: new-doc STEP 6 offline fallback scope clarification "(ใช้กับทุก escalation path รวมถึง per-agent cap และ global cap)", review STEP 5 manual-fix edge case when user picks manual fix without re-invoking skill) |
| 2026-03-17 | new-doc + review SKILL.md (v8) | — | — | — | meta-review พบ 6 issues จากการเพิ่ม best practice feature — fixes: (1) STEP 1.1 best practice search fallback เมื่อ results เป็น banned sources, (2) STEP 0 นิยาม "advanced domain", (3) STEP 3 section 4 verify-from-source-notes clarity + no-consensus format, (4) Agent 1 checklist เพิ่ม best practice check ทั้ง new-doc และ review, (5) new-doc Agent 3 Practitioner lens แยก parenthetical → bullet structure |

## การปรับปรุง SKILL v2 (2026-02-23)
**new-doc SKILL.md:**
- Fix: QUOTE retry contradiction (line 93 vs 96)
- Fix: No-Source dead-end → exit path ชัดเจน
- Fix: Scope anchor clarification (STEP 0 vs STEP 1.3)
- Fix: Confirmation keywords ขยายเป็น intent-based
- Add: Miller's Law — beginner prefer 3 concepts (ไม่ใช่ 5)
- Add: Bloom's ordinal floor คือ minimum ไม่ใช่ target
- Add: Generation effect pause (30 วินาที) ใน Section 7 และ Pre-chapter
- Add: Spacing schedule เป็น guideline ปรับได้
- Add: Interleaving cost note requirement ใน Mixed Review
- Add: Feynman Technique ครบ 4 ขั้น (เพิ่ม step 2-3)
- Add: Metacognition error analysis ใน remediation paths
- Add: Agent Discussion Protocol (Dispute Arbiter + Synthesis Agent)
- Add: STEP 5 checklist — Timing instructions + Mixed Review note

**review SKILL.md:**
- Add: Anti-inflation quote specificity (content not just heading)
- Add: Analogy opt-out handling (ไม่หักคะแนน)
- Add: Sequential fallback loop cap (3 rounds max)
- Add: Global cap counting example (> 6 ไม่ใช่ ≥ 6)
- Add: STEP 1 path validation
- Sync: Timing instructions check
- Sync: Common Mistakes non-duplication check
- Sync: Generation effect in Section 7
- Sync: Backward alignment check (holistic outcome)
- Sync: Prior Knowledge Activation specificity
- Sync: Bloom's level-appropriate verb check
- Sync: External service label definition (GUI, localhost ฯลฯ)
- Fix: No-source-notes review path (scope limit 5 concepts)
- Add: Agent Discussion Protocol (Dispute Arbiter + Synthesis Agent)
- Fix: MEMORY.md path format (docs/[path]/)
