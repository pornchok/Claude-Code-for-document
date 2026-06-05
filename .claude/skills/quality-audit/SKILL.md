---
name: quality-audit
description: ตรวจสอบและยกระดับคุณภาพเอกสารการเรียนรู้ด้วย 3 review agents ขนาน — Completeness, Accuracy, และ Learning Quality — วนแก้ไขจนได้ 100 ทุก dimension. ใช้เมื่อต้องการ audit เอกสารที่สร้างด้วย /craft-mastery หรือเอกสารเรียนรู้ใดก็ตาม. Use when asked to review, check, audit, or improve a learning document.
argument-hint: "[path to document folder]"
disable-model-invocation: true
# v2.0 — adaptive global cap based on series size, Agent 2 revision efficiency
---

# ตรวจสอบคุณภาพเอกสาร: $ARGUMENTS

⚠️ **Threshold ของ SKILL นี้คือ 100 เท่านั้น** — override project CLAUDE.md ที่อาจระบุ 95 หรืออื่น สำหรับ SKILL นี้ loop จบได้เฉพาะเมื่อทุก agent ได้ 100

---

## Scoring Rubric (ใช้กับทุก agent)
- 100: สมบูรณ์แบบ ไม่มีจุดบกพร่อง → PASS (threshold: 100 เท่านั้น)
- 95-99: ดีมาก มีจุดเล็กน้อย → ต้องแก้ก่อน (ยังไม่ผ่าน)
- 90-94: ดี มีปัญหาที่ควรปรับ → ต้องแก้ก่อน
- 80-89: พอใช้ มีปัญหาชัดเจน → ต้องแก้มาก
- ต่ำกว่า 80: ต้องแก้มาก

หัก 5 คะแนนต่อรายการ (นับต่อ instance — ถ้า violation เดียวกันเกิดในหลายบท เช่น code ไม่มี label ใน 3 บท = หัก 3×5 = 15 คะแนน ไม่ใช่ 5 คะแนน):
- ข้อมูลที่ verify ไม่ได้หรืออาจผิด
- code ที่ไม่ได้ทดสอบโดยไม่มี label
- ขาด section บังคับ (บทที่ 1: ขาด 1 ใน 6 mandatory; บทที่ 2+: ขาด 1 ใน 7 mandatory)
- exercise เป็น fill-in-the-blank หรือ copy โดยตรงจากตัวอย่างในบท
- glossary ไม่มี SOURCE URL กำกับ
- Pre-chapter Retrieval อยู่ผิดตำแหน่ง (ไม่ได้อยู่ต้นไฟล์ก่อนส่วนที่ 1)
- Learning objectives ใช้ verb ที่วัดไม่ได้ เช่น "เข้าใจ" หรือ "รู้จัก" (หัก 5 — ถ้า verb วัดได้แต่ต่ำกว่า/สูงกว่า series level อย่างไม่สมเหตุ = หัก 3 ตาม Agent 1 checklist)
- Self-check ⏸ ไม่มีเฉลยสั้น หรือไม่มี remediation direction (ทั้งสองเป็น mandatory)

ทุก violation อื่นๆ = หัก 3 คะแนนต่อ item รวมถึง:
- เนื้อหาหลัก (section 3) มีหลาย approach แต่ไม่ระบุ best practice / popular pattern และไม่ใช้ no-consensus format (หัก 3 ต่อบทที่พบ — ยกเว้น: ถ้าไม่มี official consensus ให้ระบุ use case ของแต่ละ approach ชัดเจนแทนได้ = ผ่าน ไม่หัก)

⚠️ กรณีพิเศษ: [OUTDATED-UPDATE] = source update หลังจาก QUOTE ถูก verify แล้ว → หัก 3 (ไม่ใช่ -5 เพราะ QUOTE valid ตอนสร้าง docs update ทีหลัง — ต่างจาก "ข้อมูลที่ verify ไม่ได้ตั้งแต่แรก" ที่หัก 5) + ต้องระบุว่าเนื้อหาส่วนใดในเอกสารต้องแก้

---

## STEP 1 — อ่านเอกสารทั้งหมด

ตรวจ $ARGUMENTS ก่อน:
- ถ้า $ARGUMENTS ว่างเปล่า → หยุดและแจ้ง user: "กรุณาระบุ path ของ document folder"
- ถ้า path ไม่มีอยู่จริง หรือ path ชี้ไปยังไฟล์เดียว (ไม่ใช่ folder) หรือ folder ว่างเปล่า → หยุดและแจ้ง user: "ไม่พบไฟล์เอกสารใน [path] — กรุณาตรวจสอบ path และ verify ว่าเป็น folder ไม่ใช่ไฟล์เดียว"
- ถ้า path valid: ตรวจว่า folder มีไฟล์ที่จำเป็น ได้แก่ 00-overview.md, exercises.md, glossary.md — ถ้าขาดไฟล์ใด → แจ้ง user ก่อน proceed (reviewer ยังสามารถ review ไฟล์ที่มีได้ แต่จะหักคะแนน sections ที่ขาด)
- อ่านทุกไฟล์ใน $ARGUMENTS ด้วย Read tool (ไม่ใช่แค่ list ชื่อไฟล์)

⚠️ **Adaptive global cap** — หลังอ่านไฟล์ทั้งหมด: นับ N_chapters = จำนวนไฟล์ที่ขึ้นต้นด้วย 01- ถึง 09- (หรือมากกว่า) ในโฟลเดอร์ (ไม่นับ 00-overview.md, exercises.md, glossary.md)
**adaptive_cap = max(6, ceil(N_chapters × 0.75))**
บันทึกค่านี้ไว้ใช้ใน STEP 4 แทน hard cap 6 ตัวอย่าง: 4 chapters → cap=6 | 8 chapters → cap=6 | 10 chapters → cap=8 | 14 chapters → cap=11
⚠️ **N_chapters = 0 guard**: ถ้าโฟลเดอร์ไม่มีไฟล์ 01-*.md เลย (มีแค่ 00-overview.md หรือน้อยกว่า) → adaptive_cap = 6 ตามปกติ แต่ Agent 1 ต้องระบุใน report ว่า "ไม่มีบทเนื้อหา — ตรวจได้เฉพาะ 00-overview.md, exercises.md, glossary.md" และให้คะแนนเฉพาะสิ่งที่ตรวจได้จริง ห้าม give 100 โดย vacuous truth

---

## STEP 2 — Launch 3 Review Agents พร้อมกัน (Task tool)

ถ้า Task tool ไม่พร้อมใช้งาน → แจ้ง user ว่า "กำลังใช้ sequential review mode แทน parallel agents" แล้วทำ sequential fallback: main agent review ทั้ง 3 dimension ด้วยตัวเองตามลำดับ โดยต้องแสดง output ของแต่ละ dimension ออกมาก่อนก่อนเดินต่อ — ห้ามทำรวม 3 dimension ในครั้งเดียว:
1. Completeness review → แสดง score + issue list → แก้ไข
2. Accuracy review → แสดง score + issue list → แก้ไข
3. Learning Quality review → แสดง score + issue list → แก้ไข
แต่ละ dimension ทำซ้ำได้ไม่เกิน 3 รอบ (initial + 2 revision) ถ้า fail หลังรอบที่ 3 → แจ้ง user พร้อม (a) feedback ทั้งหมดจาก dimension นั้น, (b) options: ลด scope / ยอมรับ score ที่ได้ / manual fix
⚠️ Global cap ใน sequential mode: ใช้ adaptive_cap เดียวกับ parallel mode (คำนวณใน STEP 1) — ถ้า total dimension-runs เกิน adaptive_cap → escalate ทันที
⚠️ Double-penalty reconciliation ใน sequential mode: logic เดียวกับ parallel mode (ดู Anti-double-penalty rule ด้านล่าง) — ก่อน report คะแนนรวม → cross-check ว่ามี item ใดถูกหักทั้งใน Completeness review และ Learning Quality review — ถ้ามี: ใช้ deduction สูงสุดเพียงครั้งเดียว (ไม่นับซ้ำ)

⚠️ เมื่อ launch Task agents: ต้อง include actual resolved path จาก STEP 1 (ไม่ใช่ raw '$ARGUMENTS') ใน prompt ของแต่ละ agent โดยตรง เพราะ sub-agents เป็น independent context ที่ไม่มี access ต่อ variable นี้ — ตัวอย่าง: ใช้ "/actual/path/to/docs/" แทน "$ARGUMENTS" ใน prompt

⚠️ **Anti-inflation protocol** — ทุก agent ต้องทำตามลำดับนี้ก่อนให้คะแนน:
1. Read ไฟล์เนื้อหาจริงทีละ section ด้วย Read tool
2. สำหรับทุก checklist item: quote ข้อความจากไฟล์ที่ยืนยัน + ระบุ filename
3. ระบุ deductions ทั้งหมดพร้อมหลักฐาน quote ก่อน คำนวณคะแนน
4. ถ้าให้ 100: แนบ verification log แสดงว่าตรวจ checklist item ใดจากไฟล์ใด
ห้ามให้คะแนนก่อนทำ step 1-3 ครบ — ห้ามหักคะแนนโดยไม่มีหลักฐาน
⚠️ **Quote ที่ valid ต้องแสดงให้เห็นชัดว่า checklist item นั้นครบจริง** — quote header เพียงอย่างเดียวโดยไม่มีเนื้อหาที่ตรวจจริง = invalid ตัวอย่าง: checklist "Self-check มี remediation direction" → quote ต้องเป็นบรรทัด "ถ้าตอบไม่ได้: อ่าน [section]..." จากไฟล์นั้น ไม่ใช่แค่ quote heading "## ⏸ Self-check"

⚠️ **Anti-double-penalty rule (global)**: ถ้า Agent 1 หักคะแนนสำหรับ item X แล้ว → agent อื่นที่ตรวจ item X เดียวกัน ห้ามหักซ้ำในมิติเดิม — Agent 1 = presence/completeness; Agent 3 = quality/learning effectiveness — ถ้าไม่แน่ใจว่า item ซ้ำหรือไม่ → ระบุ "may overlap with Agent 1 deduction" ในรายงาน ห้ามหัก -5 ซ้ำ ใช้ -3 เท่านั้นสำหรับมิติที่ Agent 1 ไม่ได้หัก

---

### Agent 1 — Completeness Reviewer

อ่านเอกสารใน $ARGUMENTS ตรวจ (ต้อง quote หลักฐานจากไฟล์จริงก่อน tick ทุกข้อ):

**ส่วนครบถ้วนของบท:**
- บทที่ 1 = 6 mandatory sections: วัตถุประสงค์, Why, เนื้อหาหลัก + self-check ⏸, ตัวอย่าง 3 ระดับ, Common Mistakes + *(source: URL)* inline, Retrieval Questions + เฉลย — ไม่มี Pre-chapter Retrieval
- บทที่ 2+ = 7 mandatory sections: เดิมทั้ง 6 + Pre-chapter Retrieval ที่อยู่ต้นไฟล์
- Pre-chapter Retrieval อยู่ต้นไฟล์ก่อนส่วนที่ 1 ไหม (ไม่ใช่ท้ายบท) — quote บรรทัดแรกของไฟล์นั้นเพื่อยืนยัน

**วัตถุประสงค์:**
- ทุกข้อใช้ action verb ที่วัดได้ (ไม่มี "เข้าใจ"/"รู้จัก") ไหม
- verb เหมาะสมกับ level ของ series ไหม: beginner = อธิบาย/ระบุ/ยกตัวอย่าง, intermediate = ประยุกต์/เปรียบเทียบ/แก้ปัญหา, advanced = ออกแบบ/วิเคราะห์/ประเมิน — ถ้า objectives ใช้ verb ต่ำกว่า series level หรือสูงกว่าอย่างไม่สมเหตุ → หัก 3 คะแนน (verb วัดได้แต่ level ไม่เหมาะ = -3; verb วัดไม่ได้เลย เช่น "เข้าใจ" = -5 ตามรูบริกหลัก)

**Why section (Section 2):**
- ปัญหาที่อธิบายใน Why section ใช้แค่ vocabulary จาก prerequisites ไหม — ไม่ใช้ vocabulary ใหม่จาก concept ที่บทนี้กำลังจะสอน (circular dependency) — ทดสอบ: อ่าน Why section โดยสมมติว่าไม่รู้ concept ในบทนั้นเลย → เข้าใจปัญหาที่อธิบายได้ไหม? ถ้าไม่ได้ → หัก 3 คะแนน

**Self-check ⏸:**
- ทุกจุดมีทั้ง (a) เฉลยสั้น + (b) remediation direction ("ถ้าตอบไม่ได้: อ่าน [section]") + (c) novice fallback ("ถ้าระบุจุดที่เข้าใจผิดไม่ได้: อ่าน section ทั้งหมดของบทนั้นใหม่ โดยเน้นที่ตัวอย่าง Beginner และ Intermediate ก่อน") + (d) instruction ให้เขียนเหตุผลสั้นๆ ว่าทำไมถึงตอบแบบนั้น (Generation effect element 3 — ถ้าขาด → หัก 3) ไหม
- อย่างน้อย 1 ⏸ ต่อบทเป็น backward retrieval (ถามเกี่ยวกับ concept จาก sub-section ก่อนหน้า ต้องข้ามอย่างน้อย 1 sub-section — ไม่ใช่ sub-section ที่เพิ่งอ่านจบ) ไหม
- อย่างน้อย 1 ⏸ ต่อบทมี Bloom's level ถึง floor ของบทนั้น (บท 1=L1-L2, บท 2-3=L3, บท 4+=L4-L5; exception: series < 4 บท → บทสุดท้ายใช้ L3 ได้) ไหม

**สรุปบท / Retrieval Questions ท้ายบท (Section 6):**
- มีเฉลย + remediation path + novice fallback ("ถ้าระบุจุดที่เข้าใจผิดไม่ได้: อ่าน section ทั้งหมดของบทนั้นใหม่ โดยเน้นที่ตัวอย่าง Beginner และ Intermediate ก่อน") ทุกข้อไหม
- มี instruction ให้ผู้เรียนหยุดคิดและเขียนคำตอบก่อนดูเฉลย รวมถึง instruction ให้เขียนเหตุผลสั้นๆ 1-2 ประโยคว่าทำไมถึงตอบแบบนั้น (Generation Effect ครบ: retrieve + write answer + write rationale) ก่อนแสดงเฉลยไหม
- สำหรับบทที่สอน code skills: มีอย่างน้อย 1 ข้อ code-based task ไหม (Transfer-Appropriate Processing — ถ้าไม่มีเลย → หัก 3 คะแนน)
- มีอย่างน้อย 1 ข้อ elaborative interrogation ("ทำไม [concept] ถึงออกแบบมาแบบนี้?" หรือ "จะเกิดอะไรถ้า [condition X]?") ไหม

**00-overview.md:**
- มี Prior Knowledge Activation ที่เจาะจงถึง situation/problem จริง — ไม่ใช่ "คุณรู้อะไรเกี่ยวกับ [topic] บ้าง?" — quote prompt จริงจากไฟล์เพื่อ verify ว่าระบุ situation/problem scenario ที่ผู้เรียนต้องนึกถึง action หรือ experience เฉพาะของตัวเอง ไหม
- Prior Knowledge Activation ตามด้วย bridging sentence ที่ระบุชัดว่า topic จะ extend หรือแก้ปัญหาที่ approach เดิมทำไม่ได้ (ไม่ใช่แค่ "เชื่อมกับ [prior knowledge]" โดยไม่ระบุว่าอย่างไร) ไหม — ยกเว้น: ถ้า target audience เป็น absolute beginner ที่ไม่มี prior approach อยู่แล้ว (เช่น ไม่เคยเรียน topic นั้นเลย) → bridging sentence อาจเป็น "ทำไม series นี้ถึงเหมาะกับคุณและจะแก้ปัญหาที่คุณเคยเจอได้อย่างไร" แทน = ผ่าน
- มี prerequisites + self-assessment questions ไหม
- มี clickable TOC ไหม
- มี reading schedule พร้อม note ว่าเป็น guideline ปรับได้ + มีหลาย sessions (chapter-level: 2 วัน → 1 สัปดาห์ → 1 เดือน; series-level: 3 วัน → 1 สัปดาห์) ไหม — quote schedule จริงจากไฟล์เพื่อ verify มี multiple sessions (binary "มี schedule" ไม่เพียงพอ)
- มี holistic outcome ที่ backward-aligned กับ chapters จริง — ผู้เรียนที่อ่านครบและทำ exercises ครบจะทำสิ่งที่ outcome ระบุได้จริงไหม (ไม่ใช่แค่ "มี" outcome)

**Common Mistakes:**
- ทุกข้อมีครบ format: 🔍 สัญญาณที่จะรู้ว่าทำ mistake นี้ + 🤔 metacognitive prompt ("ลองอธิบายด้วยตัวเองก่อน...") + *(source: URL)* หรือ *(unverified)* inline — ไม่มีแบบ generic — quote 1 mistake จากไฟล์เพื่อ verify format ครบ

**exercises.md:**
- มีครบ 3 ระดับ cognitive (Recall/Application/Synthesis) ต่อ concept ไหม
- มี Mixed Review พร้อม note อธิบาย interleaving cost ไหม
- Mixed Review มีอย่างน้อย 1 ข้อ discrimination task ชัดเจนไหม (สถานการณ์ที่ concept หลายตัวอาจดูใช้ได้ — ผู้เรียนต้องเลือกและอธิบายเหตุผลที่ไม่เลือกตัวอื่น)
- แต่ละกลุ่ม exercises มี Timing instructions ระบุ "ทำหลังอ่านบทที่ X" หรือ "ทำได้ระหว่างอ่าน" ไหม
- เฉลยครบทุกข้อไหม

**glossary.md:**
- ครอบคลุมทุกคำที่ introduce ครั้งแรก + มี SOURCE URL กำกับทุกคำไหม
- แต่ละคำมี format ครบ: "[คำ] คือ [ฟังก์ชัน] เพราะ [เหตุผล] — อย่าสับสนกับ [คำที่คล้าย] หรือ 'ไม่มีคำที่คล้ายใน series นี้'" ไหม
- มี "First seen in: [ชื่อไฟล์ + section]" กำกับทุกคำไหม
- เรียง alphabetical ไหม

**ตัวอย่าง 3 ระดับ:**
- Beginner examples มี inline comments ที่อธิบาย "ทำไม" (reasoning/why) ไม่ใช่แค่ "ทำอะไร" (what) ไหม (Worked Example Effect — ถ้าขาด → หัก 3 คะแนน) — quote comment 1 บรรทัดจาก Beginner example เพื่อ verify

**แต่ละบทมีไม่เกิน 3-5 concept ไหม** (concept ซับซ้อน = ≤ 3)

**Best practice:**
- เนื้อหาหลัก (section 3) ระบุ best practice / popular pattern ชัดเจน **และ** เหตุผลสั้นๆ ว่าทำไมถึงแนะนำ เมื่อมีหลาย approach ไหม — ไม่ทิ้งให้ผู้เรียนเลือกเองโดยไม่มีข้อมูล (หัก 3 ต่อบทที่พบ — Agent 3 ห้ามหักซ้ำในมิตินี้) ยกเว้น: ถ้า official source ไม่มี consensus → ใช้ no-consensus format (ระบุ use case ของแต่ละ approach ชัดเจน) = ผ่าน ไม่หัก

ให้คะแนน 0-100 ตาม rubric + bullet list จุดแก้ไขพร้อมระบุไฟล์และ quote หลักฐาน

---

### Agent 2 — Accuracy Reviewer

⚠️ **Revision mode efficiency**: ถ้านี่คือ revision round (ไม่ใช่ initial run) — ให้ re-fetch เฉพาะ URL ที่มี verdict mismatch/outdated/unverifiable จาก round ก่อนเท่านั้น (ข้าม URL ที่มี verdict match/[STALE-URL] แล้ว) — re-run เฉพาะ code ที่ fail จาก round ก่อน (ข้าม code ที่ pass แล้ว) — ประหยัด token สูงสุด 80% สำหรับ series ขนาดใหญ่

อ่านเอกสารใน $ARGUMENTS แล้ว:

**Source verification:**
1. ถ้ามีไฟล์ source-notes.md: อ่าน URL ทั้งหมด → Fetch URL เหล่านั้นใหม่จริงๆ → สำหรับแต่ละ URL แสดง (a) URL ที่ fetch, (b) QUOTE จาก source-notes, (c) ข้อความที่พบใน URL ปัจจุบัน, (d) verdict: match/mismatch/outdated/[STALE-URL]
   ⚠️ **Community source scoring**: ถ้า QUOTE มี [COMMUNITY] prefix (มาจาก GitHub, StackOverflow upvote>100 ฯลฯ) — ตรวจว่า (a) เอกสารมี callout "> ข้อมูลส่วนนี้มาจาก community source — verify ก่อนใช้งานจริง" กำกับไว้หรือไม่ — ถ้าขาด callout → หัก 3 คะแนน — ถ้า QUOTE match แต่เป็น community source: ไม่หักเพิ่มเติม community source ที่ qualify และ match ถือว่าผ่าน accuracy check
   - ถ้า URL ส่งคืน 404: WebSearch หา URL ใหม่ก่อน — ถ้าพบ URL ใหม่ → ใช้ยืนยัน → verdict ปกติ — ถ้าหาไม่เจอ → verdict: [STALE-URL] (ไม่หักคะแนน เป็น infrastructure change ไม่ใช่ content error)
   - ถ้า QUOTE ที่ verify แล้ว valid ตอน source-notes สร้าง แต่ source update ระหว่างกระบวนการ → verdict: [OUTDATED-UPDATE] + หัก 3 คะแนน (กรณีพิเศษ: QUOTE valid ตอนสร้าง docs update ทีหลัง — จึงหัก 3 ไม่ใช่ 5 ซึ่งสงวนไว้สำหรับ "ข้อมูลที่ verify ไม่ได้ตั้งแต่แรก") + ระบุว่าเนื้อหาส่วนใดในเอกสารต้องแก้
   ⚠️ Retry cap per URL: สูงสุด 1 WebFetch ซ้ำ + ไม่เกิน 2 fallback methods (รวม 3 operations ต่อ URL) — ถ้ายัง verify ไม่ได้หลัง 3 operations → verdict: [UNVERIFIABLE] + หัก 5 คะแนน (เทียบเท่า "ข้อมูลที่ verify ไม่ได้")
   ⚠️ JS-rendered docs exception: ถ้า WebFetch คืน HTML ว่างเปล่าหรือ loading placeholder → ไม่ถือว่า QUOTE invalid ทันที ให้ลอง (1) fetch raw GitHub source ของ docs repo แทน หรือ (2) search ด้วย "[exact phrase from QUOTE] site:[docs-domain]" — ถ้ายังไม่เจอจาก 2 วิธีนี้ → label [UNVERIFIABLE]
   ⚠️ Session-level cap (path 1): ถ้า concept ที่ได้ [UNVERIFIABLE] ≥ 50% ของทั้งหมดที่ตรวจ → บันทึกใน review report ว่า "⚠️ Accuracy review ไม่สมบูรณ์ — [X]% ของ concepts verify ไม่ได้ — ผลอาจไม่ครอบคลุม" + proceed ต่อโดยอัตโนมัติ
2. ถ้าไม่มี source-notes: WebSearch + WebFetch official docs เพื่อ cross-check — ระบุ scope ของ cross-check ก่อนดำเนิน: fetch สูงสุด 5 concept หลัก (concept ที่ดู non-trivial หรือ specific ก่อน ไม่ใช่ basic facts) — เมื่อพบ discrepancy: แสดง (a) claim ในเอกสาร, (b) ข้อความจาก official source, (c) verdict: match/mismatch
   ⚠️ Retry cap ต่อ URL (เช่นเดียวกับ path 1): สูงสุด 1 WebFetch ซ้ำ + ไม่เกิน 2 fallback methods (รวม 3 operations ต่อ URL) — ถ้ายัง verify ไม่ได้หลัง 3 operations → verdict: [UNVERIFIABLE] + หัก 5 คะแนน
   ⚠️ Session-level cap (path 2): ถ้า concept ที่ได้ [UNVERIFIABLE] ≥ 50% ของทั้งหมดที่ตรวจ → บันทึกใน review report ว่า "⚠️ Accuracy review ไม่สมบูรณ์ — [X]% ของ concepts verify ไม่ได้ — ผลอาจไม่ครอบคลุม" + proceed ต่อโดยอัตโนมัติ

**Code verification:**
3. รัน code examples ทั้งหมดด้วย Bash (รวม exercises.md) — ถ้า pass: บันทึก output จริง — ถ้า fail: ระบุ error และ line ที่ผิด
   ตรวจว่า code ที่ pass แต่ละชิ้น: (a) มี output จริงแสดงไว้ในเอกสารตรงกับที่รันได้ (ไม่ใช่แค่ placeholder หรือ "# output: ...") + (b) ระบุ version ที่ทดสอบ เช่น `# tested: Python 3.11` — ถ้าขาดทั้งสองนี้ → หัก 3 คะแนนต่อ code block ที่ไม่มี
4. ตรวจว่า code ที่ทดสอบไม่ได้มี label "ยังไม่ได้ทดสอบ" ชัดเจนไหม — code ที่ทดสอบไม่ได้ = code ที่ต้องการ external service จริง ได้แก่: database, API key, cloud resource, GUI display, filesystem path เฉพาะ, localhost service — ห้ามใช้ label นี้กับ self-contained code ที่รันได้ใน bash environment ปกติ

ถ้าเอกสารมี explicit descriptions ของ learning science mechanisms (เช่น อธิบาย mechanism ของ Feynman Technique, spacing effect, retrieval practice ฯลฯ) → ตรวจว่า description ถูกต้องตาม evidence เหมือน content claims อื่นๆ — ถ้าอธิบาย mechanism ผิด (เช่น อธิบาย Feynman Technique ผิดขั้นตอน) → หัก 5 คะแนน

ให้คะแนน 0-100 ตาม rubric + bullet list จุดแก้ไขพร้อมระบุไฟล์และ quote หลักฐาน

---

### Agent 3 — Learning Quality + Practitioner Reviewer

อ่านเอกสารใน $ARGUMENTS ตรวจ (สวมบทบาทเป็น practitioner ที่ใช้ topic ที่เอกสารสอนใน production จริง — infer topic จากเนื้อหา):

**การไหลและโครงสร้าง:**
- เข้าใจง่ายไหม ไหลลื่นไหม ลำดับ Why→What→How→Apply ชัดไหม
- ภาษาไทยเป็นธรรมชาติไหม ย่อหน้าสั้นพอไหม (ไม่เกิน 4-5 บรรทัด)

**Pre-chapter Retrieval:**
- อยู่ต้นไฟล์ก่อนเนื้อหาไหม + มี `---` separator ก่อนเฉลย + instruction ครบ 3 elements ของ Generation effect ไหม: (1) หยุดคิดและ retrieve, (2) เขียนคำตอบก่อนดูเฉลย, (3) เขียนเหตุผลว่าทำไมถึงตอบแบบนั้น — ถ้าขาด element ที่ 3 "เขียนเหตุผล" → หัก 3 คะแนน
- มีอย่างน้อย 1 ข้อที่เชื่อม concept บทก่อนกับบทปัจจุบัน (ไม่ใช่แค่ถาม definition ซ้ำ)

**Retrieval Questions:**
- มีอย่างน้อย 1 ข้อ application-level + 1 ข้อ elaborative interrogation ("ทำไม" หรือ "จะเกิดอะไรถ้า") ไหม (ถ้า Agent 1 หักแล้วในมิตินี้ → ห้ามหักซ้ำ)
- Section 6 (สรุปบท/Retrieval Questions) มี instruction ครบ 3 elements ของ Generation effect ไหม: (1) หยุดคิดอย่างน้อย 30 วินาที, (2) เขียนคำตอบก่อนดูเฉลย, (3) เขียนเหตุผลสั้นๆ ว่าทำไมถึงตอบแบบนั้น — ถ้าขาด element ใดก็ตาม → หัก 3 คะแนน (ถ้า Agent 1 หักแล้ว → ห้ามหักซ้ำ)
- คำถามมี desirable difficulty — ทำให้รู้สึก "ต้องคิด" ไม่ใช่ตอบได้ทันทีจากความจำ short-term ไหม (ถ้า oversimplified → หัก 3 คะแนน)
- สำหรับบทที่สอน code skills: มีอย่างน้อย 1 ข้อ code-based task ไหม (Transfer-Appropriate Processing)

**Exercises:**
- Intermediate ใช้ต่าง domain จริงไหม — ให้ list domain ของตัวอย่างในบท (ส่วนที่ 4) ก่อน แล้วเทียบว่า Intermediate exercise ต่างกันอย่างชัดเจน (ไม่ใช่แค่เปลี่ยนชื่อตัวแปรหรือตัวเลข)
- Advanced มี: semi-blind far transfer + Feynman explanation task (ครบ 4 ขั้น: อธิบาย→ระบุ gap→กลับทบทวน+อธิบายใหม่→simplify) + elaborative interrogation ไหม
- Self-check ⏸ มีอย่างน้อย 1 ข้อต่อบทที่ถึง Bloom's floor (floor = minimum level; บท 1=L1-L2, บท 2-3=L3, บท 4+=L4-L5; exception: ถ้า series < 4 บท → บทสุดท้ายใช้ L3 ได้) ไหม — floor เป็น minimum ไม่ใช่ ceiling หรือ target ของทุกข้อ
- มี Mixed Review ไหม + มี note อธิบาย interleaving cost ไหม
- ไม่มีข้อ fill-in-the-blank (ทุก level)
- ไม่มีข้อ copy context/ตัวเลขจาก section ตัวอย่าง (ส่วนที่ 4) ไหม

**ตัวอย่าง 3 ระดับ:**
- Beginner examples มี inline comments อธิบาย "ทำไม" (reasoning/why) ไม่ใช่แค่ "ทำอะไร" (what) ไหม (Worked Example Effect — ถ้าขาด → หัก 3 คะแนน; ถ้า Agent 1 หักแล้ว → ระบุ "Agent 1 deducted" ห้ามหักซ้ำ)
- Self-check ⏸ มี instruction ครบ 3 elements ของ Generation effect ไหม: (1) pause + retrieve, (2) เขียนคำตอบก่อนดูเฉลย, (3) เขียนเหตุผลสั้นๆ ว่าทำไมถึงตอบแบบนั้น — ถ้าขาด element ที่ 3 → หัก 3 คะแนน (ถ้า Agent 1 หักแล้ว → ห้ามหักซ้ำ)

**Practitioner lens:**
- ตัวอย่างและ Common Mistakes สะท้อน real-world usage ไหม (ไม่ใช่แค่ toy examples)
- Advanced exercise ท้าทายพอสำหรับ production use ไหม
- มี gotcha ที่ practitioner รู้แต่ docs ไม่บอก และไม่ถูก mention ในเอกสารไหม → ระบุ
- **Domain adaptation check** (infer domain type จากเนื้อหา — ตรวจทุก type ที่ applicable): Visual/GUI-heavy → code examples ต้องมี text-based output description อธิบาย visual จริง เช่น "# output: กล่องสีน้ำเงิน 200×100px" ไม่ใช่แค่ placeholder ว่าง | Math-heavy → ถ้า environment ไม่แน่ใจ ต้องใช้ plain text notation หรือ code block แทน LaTeX raw (ตรวจว่า LaTeX syntax ที่อาจไม่ render ถูก fallback ไว้ไหม) | Rapidly-changing APIs → code ต้องมี `# API version tested: [version], [date]` + callout "⚠️ API นี้อาจเปลี่ยนแปลง" | Niche/proprietary → 00-overview ต้องมี note ว่าใช้ community sources — ถ้า domain ตรงกับ type เหล่านี้แต่ขาด requirements → หัก 3 คะแนน
- เมื่อ topic มีหลาย approach ที่ทำสิ่งเดียวกันได้: เนื้อหาหลัก (section 3) ระบุ best practice / popular pattern ที่ industry ใช้จริงและเหตุผลไหม — format "✅ วิธีที่แนะนำ" / "⚠️ ควรเลี่ยง" หรือเทียบเท่า — ยกเว้น: ถ้า official source ไม่มี consensus → ใช้ no-consensus format (ระบุ use case ของแต่ละ approach ชัดเจนว่าเหมาะกับกรณีใด) = ผ่าน ไม่หัก — ถ้าไม่มีทั้ง best practice และ no-consensus format → หัก 3 คะแนนต่อบทที่พบ (ถ้า Agent 1 หักแล้ว → ระบุ "overlap" ห้ามหักซ้ำ)

**Learner journey simulation:**
- อ่าน series ทั้งหมดในฐานะผู้เรียนที่มี prerequisites ตามที่ overview ระบุ
- พยายามระบุจุดที่จะรู้สึก "งง" หรือ "ข้ามขั้น" อย่างน้อย 3 จุด — ถ้าหาได้น้อยกว่านั้น แปลว่าเอกสาร design learner path ได้ดี (ไม่ต้องบังคับหา 3 จุด) — สำหรับทุกจุดที่พบ: ยืนยันว่าเอกสารจัดการไว้แล้ว หรือระบุว่าไม่ได้จัดการ → หัก 3 คะแนนต่อจุดที่ไม่ได้จัดการ

ให้คะแนน 0-100 ตาม rubric + bullet list จุดแก้ไขพร้อมระบุไฟล์และ quote หลักฐาน

---

## STEP 3 — สรุปผลและ Discuss Feedback

แสดง:
```
Completeness:  [X]/100
Accuracy:      [X]/100
Learning:      [X]/100
สถานะ: PASS / FAIL
```

จุดที่ต้องแก้ไข:
[รวม feedback จากทุก agent พร้อม quote หลักฐาน]

⚠️ **Double-penalty reconciliation**: ก่อน sum scores หรือ report คะแนน main agent ต้อง cross-check ว่ามี item ใดที่ถูกหักโดยทั้ง Agent 1 และ Agent 3 — ถ้ามี: ใช้ deduction สูงสุดเพียงครั้งเดียว (ไม่นับซ้ำ) ตาม Anti-double-penalty rule ใน STEP 2

⚠️ **Feedback Discussion Gate** — ก่อนแก้ไขทุกครั้ง main agent ต้องทำสิ่งนี้:

**กรณีปกติ (feedback มีหลักฐาน quote จากไฟล์จริง + สอดคล้องกับ SKILL goals ที่ระบุไว้ในไฟล์นี้อย่างชัดเจน — "สอดคล้อง" = สามารถ trace ไปยัง requirement ที่ระบุได้จริง ไม่ใช่ subjective opinion):**
→ แก้ไขตาม feedback ได้เลย

**กรณี main agent disagree กับ feedback ข้อใดข้อหนึ่ง:**
→ ห้ามแก้หรือ ignore ทันที — ต้องทำ **Agent Discussion Round** ก่อน:
1. สรุป: (a) feedback ของ review agent, (b) ข้อโต้แย้งของ main agent + เหตุผล, (c) quote ส่วนที่พิพาทจากเอกสารจริง
2. Launch **Dispute Arbiter Agent** (ผ่าน Task tool) ด้วย context ทั้ง 3 ข้อนั้น + **ต้อง pass Scoring Rubric และ deduction rules ด้านบนของ SKILL นี้ทั้งหมดไปใน prompt ของ arbiter โดยตรง** (เพราะ arbiter เป็น independent context ที่ไม่มี access ต่อ SKILL file) + ให้ arbiter ประเมินว่า feedback ถูกต้องตาม criteria ที่ให้ไปไหม พร้อมเหตุผล
3. ถ้า arbiter sides with reviewer → main agent แก้ไขตาม feedback
4. ถ้า arbiter sides with main agent → feedback item นั้น waived (บันทึกไว้ในสรุป)
5. ถ้า arbiter ให้ verdict "unclear" หรือ main agent ยังไม่เห็นด้วยหลัง arbiter verdict → main agent ตัดสินใจเอง: ใช้ review agent's feedback เป็น default (conservative approach) + บันทึก "[Autonomously resolved: deferred to reviewer feedback]" ใน report ห้าม launch Arbiter ซ้ำ

⚠️ Dispute cap: เปิด Dispute Arbiter ได้ไม่เกิน 3 ข้อต่อรอบ review — ถ้าต้อง dispute เกิน 3 ข้อ → escalate user แทน เพราะแปลว่ามี systematic disagreement ที่ต้องการ human judgment — ถ้า user ไม่ respond ภายใน session (async/offline) → autonomous default: accept score ล่าสุด + บันทึก MEMORY.md + หยุด process

⚠️ **Anti-false-positive check**: ก่อน main agent แก้ไขตาม deduction ที่อ้างว่า "ขาด X" หรือ "ไม่มี X" — main agent ต้อง Read ไฟล์เองและ verify ว่า X ไม่มีจริงในเอกสาร — ถ้าพบว่า X มีอยู่แล้ว → deduction นั้นเป็น false positive → waive + บันทึก "waived: false positive [issue]" → ไม่ต้องแก้ไข — ถ้า review agent ไม่ได้ quote หลักฐานที่ยืนยันว่า X ขาด (ตาม anti-inflation protocol) → main agent ต้อง verify ด้วยตัวเองก่อนเสมอ

⚠️ **Convergence check**: ถ้า agent score ยังไม่ถึง 100 **และ** เพิ่มขึ้น < 3 คะแนนระหว่าง 2 รอบติดกัน ทั้งที่แก้ไขตาม feedback แล้ว → escalate user ทันที พร้อมแสดง score history + remaining issues (อาจ converge ช้าหรือมี false positive loop) — rule นี้ trigger ก่อน per-agent cap หากพบ slow convergence — ห้าม re-run อีกรอบโดยไม่ผ่าน user approval ก่อน

**กรณี feedback จาก 2 agents ขัดแย้งกัน** (score ต่างกัน > 10 คะแนนสำหรับ item เดียวกัน หรือ explicit contradiction):
→ Launch **Synthesis Agent** ด้วย Task tool ให้อ่าน feedback ทั้งหมด + เอกสารจริง แล้วหา consensus ว่า version ไหนถูกต้องกว่าตาม SKILL goals — ถ้า Synthesis Agent ไม่สามารถ conclude ได้ → escalate user — ถ้า user ไม่ respond ภายใน session (async/offline) → autonomous default: accept score ล่าสุด + บันทึก MEMORY.md + หยุด process

**กรณี 3 agents ขัดแย้งกันทั้งหมด** (ทั้ง 3 agents ให้ verdict ต่างกันสำหรับ item เดียวกัน):
→ escalate user ทันที ห้ามตัดสินใจเองหรือ launch Synthesis — แสดง 3 positions + tradeoff ให้ user ตัดสิน — ถ้า user ไม่ respond ภายใน session → autonomous default: accept score ล่าสุด + บันทึก MEMORY.md + หยุด process

**ห้ามตัดสินใจเองในกรณีที่มี fundamental conflict** — ต้องผ่าน arbitration หรือ synthesis ก่อนเสมอ

---

## STEP 4 — แก้ไขและ Review ซ้ำ

⚠️ **Loop cap (2 independent triggers — whichever hits first escalates):**
- Per-agent cap: นับต่อ agent — initial run = รอบ 1 ถ้า agent นั้น fail แล้วแก้และ re-run อีก 2 รอบ (รวมเป็นรอบที่ 3) ยังไม่ผ่าน → escalate agent นั้นทันที
- Global cap (adaptive): ใช้ค่า adaptive_cap ที่คำนวณไว้ใน STEP 1 — นับรวมทุก agent ถ้ารวมทุก agent เกิน adaptive_cap แล้วยังไม่ผ่านครบ → หยุดและ escalate ทันที
  ถ้า STEP 1 ไม่ได้คำนวณ adaptive_cap ไว้ (เช่น skip): ใช้ค่า default = max(6, ceil(N_chapters × 0.75)) โดยนับ N_chapters จากจำนวนไฟล์ 01-*.md ในโฟลเดอร์นั้น

ตัวอย่างการนับ (adaptive_cap=6): initial 3 agents (รวม=3) → Agent 2 fail → re-run Agent 2 (รวม=4) → Agent 3 fail → re-run Agent 3 (รวม=5) → Agent 3 fail อีก → re-run Agent 3 รอบ 3 (รวม=6, ไม่ trigger เพราะ 6 ไม่ > 6) → Agent 3 fail อีก → รอบที่ 4 ของ Agent 3 (รวม=7, trigger global cap: > 6) → escalate ทันที

ถ้า agent ใดได้ต่ำกว่า 100:
1. Main agent (ไม่ใช่ review sub-agent) แก้ไขตาม feedback — ก่อนแก้ต้องผ่าน **Feedback Discussion Gate ใน STEP 3** ก่อนเสมอ (ถ้า main agent disagree กับ feedback ข้อใด → ต้อง dispute ก่อนแก้ — ดู STEP 3) — ระบุ "แก้อะไร ที่ไหน ด้วยเหตุผลอะไร" ก่อน re-run
2. Run review agent นั้นใหม่ (ไม่ต้อง run ทั้ง 3 ใหม่ถ้าบางตัวผ่านแล้ว)
3. นับรอบแยกต่างหากต่อ agent

⚠️ **Re-research path**: ถ้า feedback ระบุว่า "เนื้อหาขาดข้อมูลสำคัญ" หรือ "claim ไม่มี source รองรับ" (ไม่ใช่ format/structure issue) → options เมื่อ escalate ต้องรวม "re-research: invoke /craft-mastery เพื่อ fetch ข้อมูลเพิ่มและ update เนื้อหาก่อน review รอบใหม่" ด้วย — ไม่ใช่แค่ "ยอมรับ score" อย่างเดียว
⚠️ **Re-research counter behavior**: การ invoke /quality-audit ใหม่หลัง external re-research = new invocation ทุก counter เริ่มนับจาก 0 ใหม่ตามปกติ — นี่คือ expected behavior ไม่ใช่ bug; ถ้า content gap ยังคงอยู่หลัง re-research ซ้ำ → ให้ user พิจารณาตัด concept นั้นออกจาก scope แทน

เมื่อ escalate → แจ้ง user พร้อม (a) feedback ทั้งหมดจาก agent นั้น, (b) options ให้ user เลือก: ลด scope / ยอมรับ score ที่ได้ / manual fix / re-research (ถ้า feedback เป็น content gap) — ถ้า user ไม่ respond ภายใน session (async/offline) → autonomous default: accept score ล่าสุด + บันทึก MEMORY.md + หยุด process (ใช้กับทุก escalation path)

---

## STEP 5 — CLEAN UP & UPDATE MEMORY

หลังจากทุก agent ผ่าน 100 แล้ว:
1. ถ้ามีไฟล์ temp ชั่วคราวในโฟลเดอร์ ลบออก: source-notes.md + checkpoint.md (ถ้ามี — checkpoint อาจถูกสร้างโดย /craft-mastery ก่อนหน้า)
2. Append ใน MEMORY.md ของ project (ดู path จาก project CLAUDE.md หรือ memory pointer — โดยทั่วไปอยู่ที่ [project root]/MEMORY.md หรือตาม path ที่ระบุใน CLAUDE.md) ส่วน "เอกสารที่สร้างแล้ว":
   | วันที่ | หัวข้อ | Path | Completeness | Accuracy | Learning | รอบที่ผ่าน |
   |--------|--------|------|--------------|----------|----------|------------|
   | [วันที่] | [topic] | docs/[path]/ | [X]/100 | [X]/100 | [X]/100 | รอบที่ [N] |

⚠️ ถ้า user เลือก "manual fix" หลัง escalate และไม่ invoke skill ใหม่ → MEMORY.md จะไม่ถูก update โดยอัตโนมัติ — ให้ main agent บันทึก MEMORY.md ด้วย score ล่าสุดพร้อม note "resolved manually" ทันทีก่อนหยุด process เพื่อให้มี record ว่าเอกสารถูก review แล้วแม้ยังไม่ได้ verify รอบสุดท้าย
