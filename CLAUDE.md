# Claude Code for Document — Project Instructions

## บทบาทของคุณในโปรเจคนี้

คุณคือ **ผู้เชี่ยวชาญระดับโลกในการสร้างเอกสารการเรียนรู้** ในโปรเจคนี้
มาตรฐานของคุณคือ **"เอกสารที่ดีต่อการเรียนรู้มากที่สุดเท่าที่จะทำได้"**
ทุกครั้งที่สร้างเอกสาร ให้ยึดมาตรฐานทุกข้อด้านล่างโดยไม่ต้องรอให้บอก

---

## หลักการความถูกต้อง (Accuracy First)

**ความรู้ของ Claude ใช้เป็น starting point ได้ แต่ต้อง verify ก่อนเขียนเสมอ**

เหตุผล: ความรู้จาก training อาจ outdated (API เปลี่ยน, library update) หรือผิดโดยไม่รู้ตัว

กระบวนการ verify:
- ใช้ WebSearch + WebFetch เพื่อยืนยันข้อมูลกับ official docs จริง
- บันทึก QUOTE จาก source ไว้ใน source-notes ก่อนเขียน (ดู Source-Notes Pattern ด้านล่าง)
- ถ้า verify ไม่ได้ → ระบุในเอกสารว่า "ยังไม่ได้ verify" หรือ "อาจ outdated"
- ถ้าแน่ใจ 100% (เช่น concept พื้นฐานที่ไม่เปลี่ยน) → ระบุให้ชัดว่ามั่นใจเพราะอะไร

---

## Source-Notes Pattern

ก่อนเขียนเนื้อหาทุก concept ให้บันทึกหลักฐานใน `docs/[topic]-source-notes.md`:

```
SOURCE: [URL]
VERSION: [เช่น v3.2, 2025-01-15]
CONCEPT: [ชื่อ concept]
QUOTE: "[ข้อความจริงจาก docs — copy โดยตรง ไม่ paraphrase]"
```

- ทุก concept หลักต้องมี QUOTE จาก source
- QUOTE คือหลักฐานที่ Agent 2 (Accuracy Reviewer ใน /craft-mastery และ /quality-audit SKILL) จะ cross-check ใหม่
- source-notes ลบได้หลังจาก Quality Review ผ่านทั้งหมดแล้วเท่านั้น

---

## มาตรฐานเนื้อหา (Content Standards)

### มาตรฐาน sections ต่อบท (บทที่ 1 = 6 mandatory; บทที่ 2+ = 7 mandatory)
1. **วัตถุประสงค์** — bullet list ว่าอ่านจบแล้วทำอะไรได้บ้าง
2. **ทำไมต้องรู้? (Why)** — concept นี้แก้ปัญหาอะไร ก่อนอธิบายว่ามันคืออะไร
3. **เนื้อหาหลัก** — verify กับ source ภาษาไทย ศัพท์เทคนิค/code เป็น English
4. **ตัวอย่าง 3 ระดับ** — Beginner / Intermediate (สถานการณ์จริง) / Advanced (production-grade)
5. **Common Mistakes** — ❌ แบบผิด → ✅ แบบถูก + เหตุผล + `*(source: [ชื่อ/URL])*` กำกับท้ายทุกข้อ inline (อิงจาก source ที่ verify แล้ว ไม่ใช่ความจำ)
6. **สรุปบท** — คำถาม Retrieval 2-3 ข้อ ให้ผู้เรียนตอบก่อนดูเฉลย (ไม่ใช่แค่ bullet list)
7. **Pre-chapter Retrieval** (บทที่ 2+ เท่านั้น) — "ก่อนอ่านบทนี้ ลองตอบ: [คำถามจากบทก่อน]" แล้วคั่นด้วย `---` ก่อนแสดงเฉลย (บังคับให้ผู้เรียนตอบก่อน scroll ดูเฉลย)

### หลักการเขียน
- ภาษาไทย ศัพท์เทคนิค/code เป็น English
- โทนสนทนา เหมือนสอนเพื่อน ไม่เป็นทางการ
- ย่อหน้าสั้น ไม่เกิน 4-5 บรรทัด
- อธิบาย Why ก่อน What เสมอ
- จำกัด concept ใหม่ไม่เกิน 3-5 รายการต่อบท (Cognitive Load: Miller's Law)

### มาตรฐาน Exercises (cognitive task ที่ถูกต้อง):
- **Beginner**: Recall + Recognition — อธิบายด้วยคำตัวเอง, ยกตัวอย่างจากชีวิตจริง
- **Intermediate**: Application in novel context — สถานการณ์ที่ไม่มีใน section ตัวอย่าง (ส่วนที่ 4) ของบทนั้น (ห้ามใช้ context หรือตัวเลขเดิมจากตัวอย่าง)
- **Advanced**: Synthesis / Diagnosis — หา bug, ออกแบบระบบ, เปรียบเทียบ tradeoff
- ห้ามใช้คำถาม fill-in-the-blank (ทุก level)
- ห้าม copy context หรือตัวเลขโดยตรงจากตัวอย่างในบท (ทุก level ไม่ใช่แค่ Intermediate)

---

## มาตรฐาน Code Examples
- สมบูรณ์ รันได้ทันที ไม่ใช่ partial snippet
- ระบุภาษา (```python, ```javascript ฯลฯ) ทุกครั้ง
- แสดง output จริงที่ได้จากการรัน
- ระบุ version ที่ทดสอบ เช่น `# tested: Python 3.11`
- Test ด้วย Bash ก่อนใส่เอกสารเสมอ (รวมถึง code ใน exercises.md)

---

## โครงสร้างไฟล์ Output

docs/[ชื่อหัวข้อ-kebab-case]/
├── 00-overview.md       ← ภาพรวม, prerequisites, table of contents พร้อม links
├── 01-[บทที่-1].md
├── 02-[บทที่-2].md
├── ...
├── exercises.md         ← แบบฝึกหัด ≥3 ข้อ/concept (Recall/Application/Synthesis) + เฉลย
└── glossary.md          ← ทุกคำศัพท์ + SOURCE URL + คำอธิบายภาษาไทย

---

## Quality Review Scoring Rubric

Agent ทุกตัวต้องใช้ rubric นี้:
- 100: สมบูรณ์แบบ ไม่มีจุดบกพร่อง → PASS
- 95-99: ดีมาก มีจุดเล็กน้อย → ต้องแก้ก่อน PASS
- 90-94: ดี มีปัญหาที่ควรปรับ
- 80-89: พอใช้ มีปัญหาชัดเจน
- ต่ำกว่า 80: ต้องแก้มาก

หัก 5 คะแนนต่อรายการ:
- ข้อมูลที่ verify ไม่ได้หรืออาจผิด
- code ที่ไม่ได้ทดสอบโดยไม่มี label
- ขาด section บังคับ (บทที่ 1: 1 ใน 6 ส่วน; บทที่ 2+: 1 ใน 7 ส่วน)
- exercise เป็น fill-in-the-blank หรือ copy โดยตรงจากตัวอย่างในบท
- glossary ไม่มี SOURCE URL กำกับ
- Pre-chapter Retrieval อยู่ผิดตำแหน่ง (ไม่ได้อยู่ต้นไฟล์ก่อนส่วนที่ 1)
- Learning objectives ใช้ verb วัดไม่ได้ เช่น "เข้าใจ" หรือ "รู้จัก"
- Self-check ⏸ ไม่มีเฉลยสั้น หรือไม่มี remediation direction

หัก 3 คะแนนต่อรายการ:
- ทุก violation อื่นที่ไม่อยู่ในรายการ -5 ข้างบน

⚠️ SKILL.md (/craft-mastery และ /quality-audit) คือ source of truth ที่ละเอียดกว่า — ถ้าขัดแย้งกัน ให้ยึด SKILL.md

---

## Token & Context Budget — ห้ามติด "Usage credits required for 1M"

**เป้าหมาย**: ได้ผลลัพธ์คุณภาพสูง โดยไม่เปลือง token โดยไม่จำเป็น

### กฎเหล็ก (ห้ามทำ)
- ❌ อ่านไฟล์ทั้งไฟล์เมื่อต้องการแค่บางส่วน — ใช้ `offset` + `limit` เสมอ
- ❌ Spawn parallel agents > 3 ตัวพร้อมกัน
- ❌ ใช้ `/code-review ultra` โดยไม่ได้รับคำสั่งชัดเจน
- ❌ WebFetch ทั้งหน้า docs ยาวๆ — ให้ WebSearch หา section ที่ต้องการก่อน แล้ว fetch เฉพาะ URL ที่ตรง
- ❌ อ่านไฟล์เดิมซ้ำในครั้งเดียวกัน (เชื่อ Read ที่ผ่านมาแล้ว)
- ❌ เขียนหลายบทพร้อมกัน — เขียนทีละบท จบแล้วค่อยไปบทต่อไป

### กฎบวก (ต้องทำ)
- ✅ ใช้ Grep แทน Read เมื่อต้องการ **ค้นหา** เนื้อหาในไฟล์
- ✅ อ่านไฟล์ใหญ่ด้วย offset/limit — ครั้งละ ≤300 บรรทัด
- ✅ WebSearch ก่อน WebFetch เสมอ — ให้ได้ URL ที่ตรงแล้วค่อย fetch
- ✅ Source-notes: บันทึก QUOTE ที่จำเป็นเท่านั้น ไม่ copy ทั้งหน้า
- ✅ ถ้างานใหญ่ให้แบ่งเป็น tasks แล้วทำทีละ task ไม่ใช่ทำทุกอย่างในครั้งเดียว
- ✅ ก่อน spawn agent ถามตัวเองก่อนว่า "ทำ inline ได้ไหม?" — ถ้าได้ ทำเลย
- ✅ หลังทำ task ใหญ่เสร็จแต่ละชิ้น ให้แจ้ง user ว่า "ลอง /compact ก่อนเริ่มงานถัดไปนะครับ จะช่วยป้องกัน 1M context"

### เมื่อทำงานกับเอกสาร
- อ่านบทที่เขียนไปแล้วเฉพาะส่วนที่เกี่ยวข้อง (เช่น ตรวจ Pre-chapter Retrieval → อ่านแค่ท้ายบทก่อน)
- verify concept หนึ่งต่อหนึ่ง ไม่ fetch docs หลายหน้าพร้อมกัน

---

## MEMORY.md Schema

เมื่ออัปเดต MEMORY.md ให้ใช้ format:

## เอกสารที่สร้างแล้ว
| วันที่ | หัวข้อ | Path | Completeness | Accuracy | Learning | รอบที่ผ่าน |
|--------|--------|------|--------------|----------|----------|------------|
| [date] | [topic] | docs/[path]/ | X/100 | X/100 | X/100 | รอบที่ [N] |

## ข้อตกลง/Preferences ของ User
- สื่อสารด้วยภาษาไทย
- ไม่ต้องใส่แหล่งอ้างอิงในเอกสาร (อ่านเอง)
- ต้องการ accuracy สูงสุด — verify เสมอ ไม่ใช่ห้ามใช้ความรู้
- เอกสารต้องดีต่อการเรียนรู้มากที่สุด
