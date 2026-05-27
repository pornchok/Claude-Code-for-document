# Design Spec: สื่อการสอน "เข้าใจ AI & Claude" สำหรับผู้เริ่มต้น

**Date:** 2026-05-28  
**Author:** Claude (autonomous design — user approved via "เสร็จแล้วบอกนะ")  
**Output path:** `docs/ai-for-beginners/html/`

---

## 1. Goal & Audience

**เป้าหมาย:** สร้างสื่อการสอนแบบ interactive HTML ที่มี animation ครอบคลุมตั้งแต่ "AI คืออะไร" ไปจนถึง "Claude Superpowers" สำหรับผู้ที่ไม่มีพื้นฐาน AI เลย แต่ต้องการความรู้เชิงลึกจริงๆ

**ผู้เรียนเป้าหมาย:**
- ไม่รู้ว่า AI ทำงานอย่างไร
- เคยได้ยิน ChatGPT / Claude แต่ไม่เข้าใจ mechanism ข้างใน
- ต้องการใช้ AI ให้ได้ผลจริงในชีวิตประจำวัน

**ความรู้ที่จะได้รับ:**
- เข้าใจว่า LLM คืออะไร และทำงานอย่างไรในระดับ conceptual
- Prompting ที่ได้ผลจริง (not just "ask nicely")
- Claude Superpowers / Skills system
- AI safety และจริยธรรมเบื้องต้น

---

## 2. Format & Tech

- **Format:** Static HTML site (ไม่ต้องใช้ server)
- **Animation:** CSS animations + vanilla JS (no framework dependency)
- **Style:** Dark sidebar + light content (ขยาย style เดิมจาก `html-v2`)
- **Language:** ภาษาไทย ศัพท์เทคนิคเป็น English
- **Tone:** สนทนา เหมือนเพื่อนสอนเพื่อน
- **No external runtime dependencies** — CDN ใช้ได้ (fonts, icons)

---

## 3. Animation Features

### A. Concept Reveal Animation
กล่องความรู้ค่อยๆ slide in พร้อม icon ทำให้เหมือนเปิดเผยทีละส่วน

### B. Step-Through Diagram (ขยายจาก Stepper เดิม)
สำหรับ LLM pipeline: Token → Embedding → Transformer → Output
แต่ละขั้นมี highlight + explanation panel ทางขวา

### C. Side-by-Side Comparison
Prompt เปรียบเทียบ Bad vs Good แบบ flip card / split view

### D. Animated Timeline
ประวัติ AI ตั้งแต่ 1950s ถึงปัจจุบัน แบบ horizontal scroll + pop-in

### E. Skills Flow Diagram
Claude Superpowers: user → skill invocation → result flow ที่ animate ทีละขั้น

### F. Knowledge Check
Quiz แบบ inline (click เฉลย) ไม่ต้องออกจากหน้า

---

## 4. Chapter Structure

| ไฟล์ | เนื้อหา | เวลาอ่าน |
|------|---------|----------|
| `index.html` | Overview + Prerequisites + Table of contents | 5 นาที |
| `01-what-is-ai.html` | AI คืออะไร, ประวัติ, AI vs ML vs DL vs LLM | 25 นาที |
| `02-how-llm-works.html` | Token, Embedding, Transformer, Generation — ไม่ใช้ math | 30 นาที |
| `03-prompting.html` | ศิลปะการเขียน Prompt: Structure, Context, Examples, Role | 30 นาที |
| `04-claude-superpowers.html` | Skills System: คืออะไร, ใช้อย่างไร, ทำไมสำคัญ | 20 นาที |
| `05-ai-ethics.html` | Bias, Hallucination, Responsible use | 15 นาที |
| `exercises.html` | แบบฝึกหัด Recall / Application / Synthesis ครบทุกบท | — |
| `glossary.html` | คำศัพท์ทุกคำ + คำอธิบายภาษาไทย | — |

รวม: **5 บท + exercises + glossary = 8 ไฟล์หลัก**

---

## 5. Shared Assets

- `style.css` — ขยายจาก html-v2 style เพิ่ม animation classes, quiz, timeline, flip-card
- `anim.js` — ขยายจาก html-v2 anim.js เพิ่ม: TimelineAnim, FlipCard, QuizBlock, SkillsFlow

---

## 6. Per-Chapter Content Standard (จาก CLAUDE.md)

ทุกบทต้องมีครบ 8 ส่วน:
1. วัตถุประสงค์ (bullet list)
2. ทำไมต้องรู้? (Why)
3. Analogy + breakdown points
4. เนื้อหาหลัก (verified)
5. ตัวอย่าง 3 ระดับ
6. Common Mistakes (❌→✅)
7. สรุปบท + Retrieval Questions
8. Pre-chapter Retrieval (บทที่ 2+)

---

## 7. Review Agent

หลังสร้างเสร็จ → invoke `quality-audit` skill เพื่อ review accuracy ครบทุกบท

---

## 8. Source Notes

ก่อนเขียนเนื้อหา ต้อง verify ข้อมูลกับ official sources:
- Anthropic docs: https://docs.anthropic.com
- Claude documentation
- AI/ML fundamentals จาก peer-reviewed sources

บันทึกใน `docs/ai-for-beginners/source-notes.md`
