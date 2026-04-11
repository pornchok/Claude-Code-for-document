# Claude Code Complete Training Guide
## Basic AI to Expert - For QA Automation Engineers

---

> **หมายเหตุสำคัญ:**
> - เอกสารนี้เขียนขึ้นในเดือน มกราคม 2568 ข้อมูลอาจมีการเปลี่ยนแปลง
> - URLs และคำสั่งต่างๆ อาจเปลี่ยนแปลงได้ กรุณาตรวจสอบจาก [เอกสารอย่างเป็นทางการของ Claude](https://docs.anthropic.com/en/docs/claude-code)
> - ราคาและ model ต่างๆ อาจมีการปรับเปลี่ยน ตรวจสอบราคาล่าสุดที่ [Anthropic Pricing](https://www.anthropic.com/pricing)

> **📖 ไม่เข้าใจศัพท์?** ดู [Glossary คำศัพท์สำหรับมือใหม่](#186-glossary-คำศัพท์สำหรับมือใหม่) ได้เลย (Token, Context, CLI, MCP คืออะไร? ดูที่นี่!)
>
> **📋 แนะนำลำดับการอ่าน:**
> | ระดับ | อ่านอะไร | เวลา |
> |-------|---------|------|
> | 🟢 มือใหม่สุด | Quick Start → Cheat Sheet → Safety → Scenarios → Glossary | 30 นาที |
> | 🟡 เริ่มใช้แล้ว | + Section 1-4, 7, 12-13 (CLAUDE.md, Skills, Workflow) | 1-2 ชั่วโมง |
> | 🔵 ใช้คล่อง | + Section 5-6, 8, 11, 17 (MCP, Subagents, Context, QA Workflows) | 2-3 ชั่วโมง |
> | 🔴 Pro | ทั้งหมด รวม Hooks, CI/CD, Vibe Coding | อ่านทั้งหมด |

---

# อ่านตรงนี้ก่อน! (Quick Start 5 นาที)

## Claude Code คืออะไร? (อธิบายแบบง่ายๆ)

**ลองนึกภาพว่าคุณมีผู้ช่วยที่:**
- พิมพ์โค้ดได้เร็วมาก
- ค้นหาไฟล์เก่งมาก
- รัน test ให้ได้
- อธิบายโค้ดเป็นภาษาคนได้

**Claude Code = ผู้ช่วยคนนั้น** อยู่ใน Terminal ของคุณ

## เริ่มใช้งานใน 3 ขั้นตอน

> 💡 **ยังไม่เคยใช้ Terminal?** ต้องเปิด Terminal ก่อน:
> - **Mac:** กด `Cmd+Space` พิมพ์ "Terminal" แล้วกด Enter
> - **Windows:** กด `Windows key` พิมพ์ "PowerShell" แล้วกด Enter
> - **Linux:** กด `Ctrl+Alt+T`

### ขั้นตอนที่ 0: สมัคร Account (ถ้ายังไม่มี)

> **ต้องมี account ก่อนใช้งาน!** Claude Code ต้องใช้ API key จาก Anthropic

1. เข้า [https://console.anthropic.com/](https://console.anthropic.com/)
2. กด **Sign Up** สมัครด้วย email (หรือ Google account)
3. เข้าไปที่ **Settings → API Keys → Create Key**
4. **Copy API key เก็บไว้** (จะขึ้นต้นด้วย `sk-ant-...`)

> ⚠️ **API key จะแสดงแค่ครั้งเดียว!** Copy เก็บไว้ที่ปลอดภัย
>
> 💰 **ค่าใช้จ่าย:** Claude Code ใช้ระบบ pay-per-use (จ่ายตามที่ใช้) ต้องเติมเงินใน account ก่อน
> เข้า Settings → Billing → Add Credit เพื่อเติมเงิน (เริ่มต้น $5 ก็พอ)

### ขั้นตอนที่ 1: ติดตั้ง

> **คำสั่งนี้ทำอะไร?** download ตัวติดตั้ง Claude Code จาก Anthropic แล้วติดตั้งให้อัตโนมัติ (ปลอดภัย — จาก Anthropic โดยตรง)

```bash
# Mac / Linux / WSL
curl -fsSL https://claude.ai/install.sh | bash

# Windows PowerShell
irm https://claude.ai/install.ps1 | iex
```

**ตัวอย่าง output ที่จะเห็น (ถ้าสำเร็จ):**
```
Downloading Claude Code...
Installing to ~/.claude/bin/claude...
Adding to PATH...
✓ Claude Code installed successfully!

Run 'claude --version' to verify.
```

```bash
# ตรวจสอบว่าติดตั้งสำเร็จ
claude --version
# Output: claude-code version 1.0.x
```

> ⚠️ **ถ้าเจอ error:** ดู Section 16 (Troubleshooting) หรือลอง restart Terminal

### ขั้นตอนที่ 2: เปิดใช้งาน

```bash
# เข้าไปในโฟลเดอร์โปรเจคของคุณ (เลือกตัวอย่างที่ตรงกับสถานการณ์)

# ตัวอย่าง 1: โปรเจคบน Desktop
cd ~/Desktop/my-project

# ตัวอย่าง 2: โปรเจคใน Documents
cd ~/Documents/work/my-app

# ตัวอย่าง 3: ยังไม่มีโปรเจค? สร้างโฟลเดอร์ทดลองก่อน
mkdir ~/Desktop/claude-test && cd ~/Desktop/claude-test
```

> 💡 **`cd` คืออะไร?** = Change Directory (เปลี่ยนโฟลเดอร์) เหมือนดับเบิลคลิกเปิดโฟลเดอร์ แต่ทำผ่าน Terminal
>
> **ไม่รู้ path โปรเจค?** ลาก folder จาก Finder/Explorer วางใน Terminal จะได้ path อัตโนมัติ

```bash
# แล้วเริ่ม Claude Code
claude

# ครั้งแรกจะถาม API key — วาง key ที่ copy ไว้จากขั้นตอนที่ 0
```

### ขั้นตอนที่ 3: ลองคุยกับ Claude
```
> สวัสดี
> มีไฟล์อะไรบ้างในโปรเจคนี้?
> ช่วยสร้างไฟล์ hello.txt ที่เขียนว่า "Hello World"
```

> 💡 **หมายเหตุ:** คำสั่งที่ใช้ได้ขึ้นอยู่กับโปรเจคของคุณ
> เช่น `รัน npm test` ใช้ได้เฉพาะโปรเจคที่มี package.json (Node.js project)
> ถ้าเพิ่งสร้างโฟลเดอร์เปล่า ลองสั่ง `สร้างไฟล์ hello.py ที่ print Hello World` แทน

**แค่นี้ก็ใช้งานได้แล้ว!**

## ไม่รู้จะถามอะไร? เริ่มจากตรงนี้เลย!

> **ปัญหาที่ QA มือใหม่เจอบ่อย:** เปิด Claude แล้ว... นั่งมองหน้าจอ ไม่รู้จะพิมพ์อะไร
>
> **วิธีแก้:** Copy ประโยคด้านล่างไปวางได้เลย!

### ประโยคแรกที่ควรพิมพ์ (เลือก 1 อัน)

```
ถ้าเพิ่งเปิดโปรเจค:
> สรุปให้หน่อยว่าโปรเจคนี้คืออะไร มีไฟล์อะไรบ้าง

ถ้าอยากรู้ว่า test มีอะไร:
> ดู test ทั้งหมดในโปรเจค แล้วสรุปว่ามี test อะไรบ้าง

ถ้าอยากรัน test:
> รัน test ให้หน่อย แล้วบอกว่า test ไหน pass / fail

ถ้าไม่รู้เลยว่า Claude ทำอะไรได้:
> บอกหน่อยว่าคุณช่วยอะไร QA ได้บ้าง? ยกตัวอย่าง 5 อย่าง
```

### 10 ประโยคที่ QA ใช้ได้ทุกวัน (Copy-Paste เลย!)

| # | พิมพ์อันนี้ | Claude จะทำอะไร |
|---|-----------|----------------|
| 1 | `รัน test ให้หน่อย` | รัน test แล้วสรุปผล |
| 2 | `test ไหน fail บ้าง?` | แสดงรายการ test ที่ fail |
| 3 | `test X fail เพราะอะไร ช่วยวิเคราะห์` | หาสาเหตุที่ fail |
| 4 | `เขียน test สำหรับ [feature]` | สร้าง test ใหม่ให้ |
| 5 | `อธิบายไฟล์ @ชื่อไฟล์ ให้หน่อย` | อธิบาย code แบบง่ายๆ |
| 6 | `หาไฟล์ที่เกี่ยวกับ [keyword]` | ค้นหาไฟล์ให้ |
| 7 | `review code ที่เปลี่ยนล่าสุด` | review git changes |
| 8 | `แก้ test ที่ fail ให้หน่อย` | แก้ test ให้ |
| 9 | `สรุปว่าวันนี้ทำอะไรไปบ้าง` | สรุปงานที่ทำ |
| 10 | `ช่วยเขียน bug report จาก error นี้: [paste error]` | เขียน bug report ให้ |

### ถ้ายังไม่รู้จะถามอะไร → ถาม Claude!

```
> ฉันเป็น QA มือใหม่ ไม่รู้จะเริ่มยังไง
> โปรเจคนี้ใช้ [framework] ช่วยแนะนำว่าควรทำอะไรก่อน

# Claude จะแนะนำขั้นตอนที่เหมาะกับโปรเจคของคุณ!
```

> **💡 เคล็ดลับ:** ยิ่งบอกรายละเอียดมาก ยิ่งได้คำตอบดี
> - ❌ "ช่วยหน่อย" (Claude ไม่รู้ว่าจะช่วยอะไร)
> - ✅ "ช่วยดู test ใน tests/login.robot ว่า fail เพราะอะไร" (Claude รู้เลย!)

## 5 คำสั่งที่ต้องรู้

| พิมพ์ | ทำอะไร | เมื่อไหร่ใช้ |
|------|--------|-------------|
| `/help` | ดูวิธีใช้ | งง ไม่รู้จะทำไง |
| `/clear` | เริ่มใหม่ | เปลี่ยนไปทำงานอื่น |
| `/context` | ดูว่า Claude จำอะไรอยู่ | อยากรู้ว่าใช้ memory ไปเท่าไหร่ |
| `/compact` | บีบอัด memory | memory เกือบเต็ม |
| `/cost` | ดูค่าใช้จ่าย | อยากรู้ว่าใช้เงินไปเท่าไหร่ |

## 3 ปุ่มที่ต้องจำ

| กดปุ่ม | ทำอะไร |
|-------|--------|
| `Ctrl+C` | หยุด/ยกเลิก |
| `Ctrl+D` | ออกจาก Claude |
| `Esc` | หยุดกลางคัน |

## เคล็ดลับสำคัญ: บอกให้ชัด!

```
❌ ไม่ดี: "แก้ bug"
✅ ดี:    "แก้ bug ใน function login ไฟล์ src/auth.ts error คือ undefined"
```

ยิ่งบอกชัด = ยิ่งได้ผลลัพธ์ดี

## ค่าใช้จ่าย (ต้องรู้ก่อนใช้!)

| วิธีใช้งาน | ค่าใช้จ่าย | เหมาะกับ |
|-----------|-----------|---------|
| **Claude Pro/Max subscription** | $20-100/เดือน (รวมอยู่แล้ว) | ใช้ส่วนตัว/ทีมเล็ก |
| **API Key** | จ่ายตาม token ที่ใช้ | องค์กร/ใช้เยอะ |

**Tips ประหยัด:**
- ใช้ `/model` เลือก **Haiku** สำหรับงานง่ายๆ (ถูกกว่า Sonnet/Opus)
- ใช้ `/compact` เมื่อ context เยอะ (ลด token)
- ใช้ `/cost` ดูค่าใช้จ่ายระหว่างทำงาน

---

# วันแรกที่ใช้ Claude Code (Day 1 Checklist)

> **สำหรับ QA มือใหม่:** ทำตาม checklist นี้ทีละข้อ ใช้เวลาประมาณ 30 นาที

### Phase 1: ติดตั้ง (5 นาที)
- [ ] เปิด Terminal (ดูวิธีเปิดด้านบน)
- [ ] Copy คำสั่งติดตั้งไปวาง แล้วกด Enter
- [ ] รอจนติดตั้งเสร็จ (ประมาณ 1-2 นาที)
- [ ] พิมพ์ `claude --version` ตรวจสอบว่าติดตั้งสำเร็จ

### Phase 2: ลองใช้ครั้งแรก (10 นาที)
- [ ] `cd` ไปยังโฟลเดอร์โปรเจคของคุณ
- [ ] พิมพ์ `claude` แล้วกด Enter
- [ ] Login ด้วย Anthropic account (ถ้าขึ้นมา)
- [ ] ลองพิมพ์: `สวัสดี` (Claude จะตอบกลับ!)
- [ ] ลองพิมพ์: `มีไฟล์อะไรบ้างในโปรเจคนี้?`
- [ ] ลองพิมพ์: `/help` ดูคำสั่งทั้งหมด

### Phase 3: ลอง Task จริงง่ายๆ (10 นาที)
- [ ] ลองพิมพ์: `อธิบาย function หลักในโปรเจคนี้ให้หน่อย`
- [ ] ลองพิมพ์: `รัน test ให้หน่อย` (ถ้ามี test)
- [ ] ลองพิมพ์: `/context` ดูว่าใช้ memory ไปเท่าไหร่
- [ ] ลองพิมพ์: `/cost` ดูค่าใช้จ่าย

### Phase 4: ปิดใช้งาน (1 นาที)
- [ ] กด `Ctrl+D` เพื่อออกจาก Claude
- [ ] หรือพิมพ์ `/clear` แล้ว `Ctrl+D`

### ผ่านทุกข้อแล้ว = พร้อมใช้งานจริง!

> **ถ้าติดปัญหา:** ดู Section 16 (Troubleshooting) หรือถาม Claude ตรงๆ ว่า "ติดปัญหา ... ช่วยแก้หน่อย"

---

# แผ่นโกง 1 หน้า (1-Page Cheat Sheet)

> **สำหรับ QA มือใหม่:** ปริ้นหน้านี้แปะข้างจอ ใช้ได้ทุกวัน!

```
╔═══════════════════════════════════════════════════════════════════╗
║              CLAUDE CODE: แผ่นโกงสำหรับ QA                        ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  🟢 เริ่มใช้งาน                                                    ║
║  ─────────────────                                                ║
║  claude                  เปิด Claude Code                        ║
║  claude -c               ทำงานต่อจากเมื่อกี้                      ║
║  Ctrl+D                  ออก                                      ║
║                                                                   ║
║  🔵 5 คำสั่งที่ใช้ทุกวัน                                            ║
║  ─────────────────────────                                        ║
║  /help                   ดูวิธีใช้ (งงเมื่อไหร่พิมพ์ตัวนี้)         ║
║  /context                ดูว่าใช้ memory ไปเท่าไหร่                ║
║  /compact                บีบอัด memory (ใช้เมื่อ > 50%)           ║
║  /clear                  เริ่มใหม่ (⚠️ ลบทุกอย่าง!)               ║
║  /cost                   ดูค่าใช้จ่าย                              ║
║                                                                   ║
║  🟡 สั่งงาน Claude (พิมพ์เป็นภาษาไทยได้!)                         ║
║  ──────────────────────────────────────────                       ║
║  > รัน test ให้หน่อย                                               ║
║  > อธิบายไฟล์ @src/login.ts ให้หน่อย                               ║
║  > เขียน test สำหรับ login page                                   ║
║  > test นี้ fail ช่วยดูหน่อย [paste error]                         ║
║                                                                   ║
║  🔴 ปุ่มฉุกเฉิน                                                    ║
║  ─────────────                                                    ║
║  Esc                     หยุด Claude ทันที                        ║
║  Ctrl+C                  ยกเลิกคำสั่ง                              ║
║                                                                   ║
║  ⚠️ ข้อควรระวัง                                                    ║
║  ──────────────                                                   ║
║  • Claude จะถาม "approve?" ก่อนแก้ไฟล์ → อ่านก่อนกด Y            ║
║  • ถ้าไม่แน่ใจ กด N (ปฏิเสธ) ได้เสมอ                              ║
║  • /clear ลบทุกอย่าง ไม่มี undo                                   ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

---

# สิ่งที่ต้องระวัง! (Safety Warnings)

> **อ่านก่อนใช้งาน:** เพื่อความปลอดภัยของโปรเจคคุณ

### ✅ สิ่งที่ทำได้สบายใจ (ปลอดภัย)

| สิ่งที่ทำ | ทำไมปลอดภัย |
|----------|-------------|
| ถาม Claude อะไรก็ได้ | แค่ถามไม่แก้ไฟล์ |
| ให้ Claude อ่านไฟล์ | อ่านอย่างเดียว ไม่แก้ |
| รัน `/help`, `/context`, `/cost` | แค่ดูข้อมูล |
| กด Esc หรือ Ctrl+C | หยุดทุกอย่างได้ทันที |
| กด N (No) เมื่อ Claude ถาม approve | ปฏิเสธได้เสมอ |

### ⚠️ สิ่งที่ต้องระวัง (อ่านก่อนกด)

| สิ่งที่ต้องระวัง | ทำไม | วิธีป้องกัน |
|----------------|------|-----------|
| Claude ถาม "approve edit?" | Claude จะแก้ไฟล์ของคุณ | **อ่าน diff ก่อนกด Y** ถ้าไม่แน่ใจกด N |
| `/clear` | ลบ memory ทั้งหมด ไม่มี undo | สั่ง Claude สรุปก่อน clear |
| `--permission-mode bypassPermissions` | Claude แก้ไฟล์ได้เลยไม่ถาม | **อย่าใช้!** ยกเว้นมั่นใจ 100% |
| ใส่ API Key | เป็นรหัสผ่านของคุณ | **ห้ามใส่ในไฟล์ที่ git commit** |
| รัน script ที่ Claude เขียน | อาจมี bug | **อ่าน code ก่อนรัน** หรือรันใน test env |

### ❌ สิ่งที่ห้ามทำ (อันตราย!)

```
⛔ ห้ามใส่ API Key ในไฟล์ .js, .ts, .robot แล้ว commit
   → ใช้ environment variable แทน: export ANTHROPIC_API_KEY=xxx

⛔ ห้ามใช้ bypassPermissions ในโปรเจคจริง (production)
   → ใช้แค่ในโปรเจคทดสอบที่ลบได้

⛔ ห้ามให้ Claude แก้ไฟล์ config สำคัญโดยไม่อ่านก่อน
   → อ่าน diff ทุกครั้ง กด N ถ้าไม่แน่ใจ
```

### 🆘 ถ้าพลาดแล้ว แก้ยังไง?

```bash
# Claude แก้ไฟล์ผิด → ใช้ git กลับไป
git checkout -- ชื่อไฟล์         # กู้คืนไฟล์เดียว
git checkout -- .                # กู้คืนทุกไฟล์ (⚠️ ลบงานที่ยังไม่ save)

# ไม่ได้ใช้ git? → ดูไฟล์ backup
# Claude จะสร้าง backup อัตโนมัติก่อนแก้ไฟล์

# ไม่แน่ใจ? → ถาม Claude เลย
> เมื่อกี้แก้ไฟล์อะไรไปบ้าง ช่วย list ให้หน่อย
```

---

# สมมติว่า... (QA Scenarios สำหรับมือใหม่)

> **อ่านส่วนนี้ก่อนไปเรียนบทอื่น!** จะได้เห็นภาพว่า Claude ช่วยงาน QA ยังไง

### สมมติว่า 1: "เช้านี้ต้องรัน test"

```bash
# 1. เปิด Terminal แล้วไปที่โปรเจค
cd /path/to/my-project

# 2. เปิด Claude
claude

# 3. สั่งรัน test
> รัน test ทั้งหมดให้หน่อย แล้วบอกว่า test ไหน fail

# 4. Claude จะ:
#    - รัน npm test (หรือ robot tests/ ขึ้นอยู่กับโปรเจค)
#    - บอกว่า test ไหน pass / fail
#    - ถ้า fail บอกสาเหตุด้วย

# 5. ถ้า test fail ถาม Claude ต่อได้เลย:
> test "Login with valid credentials" fail ช่วยดูว่าเป็นเพราะอะไร

# 6. เสร็จแล้วออก
Ctrl+D
```

### สมมติว่า 2: "เจอ bug แล้วต้องรายงาน"

```bash
# 1. เปิด Claude ในโปรเจค
claude

# 2. บอก Claude ว่าเจอ bug อะไร
> พบ bug: หน้า login กด submit แล้วไม่มีอะไรเกิดขึ้น
> Error ใน console: "TypeError: Cannot read property 'email' of undefined"
> ช่วย:
> 1. หาว่า bug อยู่ในไฟล์ไหน
> 2. อธิบายสาเหตุแบบง่ายๆ
> 3. เขียน test ที่จำลอง bug นี้

# 3. Claude จะ:
#    - ค้นหาไฟล์ที่เกี่ยวข้อง
#    - อธิบายสาเหตุ
#    - เขียน test ให้
#    (Claude จะถาม approve ก่อนสร้างไฟล์ → อ่านแล้วกด Y)

# 4. Copy ผลลัพธ์ไปใส่ bug report ได้เลย
```

### สมมติว่า 3: "ต้องเขียน test ใหม่แต่ไม่รู้จะเริ่มยังไง"

```bash
# 1. เปิด Claude ในโปรเจค
claude

# 2. บอก Claude ว่าจะ test อะไร (ยิ่งชัดยิ่งดี)
> เขียน Robot Framework test สำหรับหน้า register:
> - URL: https://myapp.com/register
> - ต้อง test: สมัครสำเร็จ, email ซ้ำ, password ไม่ตรง criteria
> - ดูตัวอย่าง test เดิมใน @tests/login.robot

# 3. Claude เขียน test ให้ → ถาม approve → กด Y

# 4. ลองรัน test ที่ Claude เขียน
> รัน test ที่เพิ่งสร้างให้หน่อย

# 5. ถ้า test fail → บอก Claude แก้
> test ที่เขียน fail ด้วย error: [paste error]
> ช่วยแก้ให้หน่อย

# 6. ทำซ้ำจนกว่า test จะ pass!
```

### สมมติว่า 4: "ต้อง review PR ของเพื่อนร่วมทีม"

```bash
# 1. เปิด Claude ในโปรเจค
claude

# 2. ให้ Claude review
> review code ที่เปลี่ยนแปลงล่าสุดให้หน่อย (git diff)
> ดูเรื่อง:
> - มี bug ไหม
> - มี security issue ไหม
> - test ครอบคลุมไหม
> สรุปเป็น bullet points

# 3. Copy ผลลัพธ์ไปเป็น PR comment ได้เลย!
```

---

# Learning Path สำหรับ QA (เรียนตามลำดับนี้!)

> **สำคัญ:** อย่าข้ามขั้นตอน! เรียนทีละระดับจะทำให้ไม่ overwhelm

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    🎯 QA Learning Path: 4 สัปดาห์                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Week 1: พื้นฐาน                                                            │
│  ─────────────────                                                          │
│  ✅ Day 1 Checklist (30 นาที)                                               │
│  ✅ ใช้คำสั่งพื้นฐาน: ถาม, ค้นหา, รัน test                                    │
│  ✅ เรียนรู้ /context, /compact, /cost                                      │
│  ✅ สร้าง CLAUDE.md (copy template)                                         │
│                                                                             │
│                           ⬇️                                                │
│                                                                             │
│  Week 2: Subagent (ง่ายที่สุด!)                                             │
│  ──────────────────────────────                                             │
│  ✅ ลองใช้: "ใช้ subagent ค้นหา..."                                         │
│  ✅ ใช้สำหรับ: ค้นหาไฟล์, วิเคราะห์ coverage, หา bug                          │
│  ✅ ไม่ต้องติดตั้งอะไร ไม่ต้องเขียน config!                                   │
│                                                                             │
│                           ⬇️                                                │
│                                                                             │
│  Week 3: Skills + PRP                                                       │
│  ────────────────────                                                       │
│  ✅ สร้าง Skill แรก: /run-tests (ให้ Claude สร้างให้!)                       │
│  ✅ ลองเขียน PRP สำหรับงานซับซ้อน                                            │
│  ✅ ใช้ /plan สำหรับงานใหญ่                                                  │
│                                                                             │
│                           ⬇️                                                │
│                                                                             │
│  Week 4: MCP (Optional - เมื่อคล่องแล้ว)                                     │
│  ─────────────────────────────────────                                      │
│  ⬜ ติดตั้ง Playwright MCP (สำหรับ Web Testing)                              │
│  ⬜ ติดตั้ง GitHub MCP (ดู PR/Issues)                                        │
│  ⬜ ลองใช้งานจริง                                                            │
│                                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│  💡 Key: ถ้ายังไม่มั่นใจ อยู่ที่ Week 2-3 ไปก่อนได้ MCP เป็น optional!        │
└─────────────────────────────────────────────────────────────────────────────┘
```

## สรุปสั้นๆ: เรียนอะไรก่อน?

| ลำดับ | หัวข้อ | ความยาก | ต้องทำอะไร |
|-------|--------|---------|-----------|
| 1️⃣ | **พื้นฐาน** | ง่าย | พิมพ์คุยกับ Claude |
| 2️⃣ | **Subagent** | ง่ายมาก | พิมพ์ "ใช้ subagent..." |
| 3️⃣ | **Skills** | ปานกลาง | สร้างไฟล์ (ให้ Claude ช่วย!) |
| 4️⃣ | **PRP** | ปานกลาง | เขียนเอกสาร |
| 5️⃣ | **MCP** | ยาก | ติดตั้ง + config (optional) |

> **🚀 เคล็ดลับ:** ถ้ากลัว command line ให้ถาม Claude ว่า "ช่วยสร้าง Skill /run-tests ให้หน่อย" - Claude จะสร้างให้เอง!

---

# วิธีอ่านเอกสารนี้

| ถ้าคุณคือ... | อ่านบทที่... |
|-------------|-------------|
| มือใหม่สุดๆ | บท 1-3 ก่อน |
| อยากใช้งานพื้นฐาน | บท 1-5 |
| อยากเป็น Pro | อ่านทั้งหมด |
| เจอปัญหา | บท 16 (Troubleshooting) |
| ต้องการ Reference | บท 18 (Quick Reference) |

---

# สารบัญ (Table of Contents)

**เริ่มต้นที่นี่! (Quick Start):**
- [Quick Start 5 นาที](#อ่านตรงนี้ก่อน-quick-start-5-นาที) ⭐⭐⭐
- [Day 1 Checklist](#วันแรกที่ใช้-claude-code-day-1-checklist) ⭐⭐⭐
- [Learning Path สำหรับ QA](#learning-path-สำหรับ-qa-เรียนตามลำดับนี้) ⭐⭐⭐

**พื้นฐาน:**
1. [บทนำ: Claude Code คืออะไร?](#1-บทนำ-claude-code-คืออะไร) 🟢
2. [การติดตั้งและเริ่มต้นใช้งาน](#2-การติดตั้งและเริ่มต้นใช้งาน) 🟢
3. [CLI Commands, Flags, Prompt/Context Engineering และ PRP](#3-cli-commands-และ-options) 🟢🟡

**ระดับกลาง:**
4. [CLAUDE.md - Memory ของ Claude](#4-claudemd---memory-ของ-claude) 🟡
5. [Settings และ Permissions](#5-settings-และ-permissions) 🔵
6. [MCP (เชื่อมต่อ GitHub, DB, etc.)](#6-mcp-model-context-protocol) 🔵
7. [Skills - คำสั่งลัดที่สร้างเอง](#7-skills-คำสั่งลัดที่สร้างเอง) 🟡
8. [Subagents - ผู้ช่วยที่มีความจำแยก](#8-subagents-ผู้ช่วยที่มีความจำแยก) 🔵

**หัวข้อขั้นสูง (ข้ามได้):**
9. [Hooks และ Plugins](#9-hooks-และ-plugins-หัวข้อขั้นสูง) 🔴
10. [IDE Integrations](#10-ide-integrations) 🟡
11. [Context Management](#11-context-management-real-world-guide) 🔵

**Reference:**
12. [Best Practice Workflow: ตัวอย่างครบจบ](#12-best-practice-workflow-ตัวอย่างครบจบ) 🟡
13. [Best Practices สำหรับ QA](#13-best-practices-สำหรับ-qa-automation) 🟡
14. [Security Best Practices](#14-security-best-practices) 🟡
15. [Keyboard Shortcuts](#15-keyboard-shortcuts-reference) 🟢
16. [Troubleshooting](#16-troubleshooting-guide) 🟢
17. [QA Workflows](#17-qa-automation-workflows) 🟡🔵
    - [17.6 Efficient Test Writing](#176-efficient-test-writing-workflow-ประหยัดเวลา--มีประสิทธิภาพ) ⭐⭐
    - [17.7 Auto-Loop Workflow](#177-auto-loop-workflow-เขียน--รัน--แก้--review-อัตโนมัติ) ⭐⭐⭐
    - [17.8 Smart Questioning](#178-smart-questioning-ให้-claude-ถามก่อนเริ่มงาน) ⭐⭐⭐
    - [17.9 Robot Framework Efficient](#179-robot-framework-efficient-test-writing)
    - [17.10 Real-World Examples](#1710-real-world-examples-ตัวอย่างใช้งานจริง-copy--paste-ได้เลย) ⭐⭐⭐
    - [17.11 Vibe Coding](#1711-vibe-coding-เขียนโค้ดด้วยภาษาพูด-สำคัญมาก) ⭐⭐⭐
18. [Quick Reference Cards](#18-quick-reference-cards)
    - [18.6 Glossary คำศัพท์](#186-glossary-คำศัพท์สำหรับมือใหม่) ⭐

> ⭐ = หัวข้อสำคัญที่ปรับปรุงใหม่

---

# 1. บทนำ: Claude Code คืออะไร?
> 🟢 **ระดับ: มือใหม่** | อ่าน: 10 นาที

## 1.1 อธิบายแบบง่ายๆ

### เปรียบเทียบให้เห็นภาพ

**Claude Code เหมือนกับ:**

| เหมือนกับ... | ในแง่ที่ว่า... |
|-------------|---------------|
| **Google** | ค้นหาข้อมูลในโค้ดของคุณได้ |
| **นักพัฒนามือใหม่** | เขียนโค้ดตามที่บอกได้ |
| **ผู้ช่วยส่วนตัว** | ทำงานซ้ำๆ ให้คุณได้ |
| **เพื่อนร่วมงาน** | ช่วยดูโค้ดและให้ความเห็นได้ |

### สิ่งที่ Claude Code ทำได้

```
✅ อ่านไฟล์ทุกไฟล์ในโปรเจค
✅ แก้ไขโค้ด
✅ รัน commands (npm test, git, etc.)
✅ อธิบายโค้ดเป็นภาษาคน
✅ หา bug
✅ เขียน test
```

### สิ่งที่ Claude Code ทำไม่ได้

```
❌ เข้าถึง internet (ยกเว้นตั้งค่าเพิ่ม)
❌ จำข้ามวัน (ต้องบอกใหม่ทุก session)
❌ ทำงานเองโดยไม่ถาม (ต้องขออนุญาต)
```

## 1.2 ทำไมต้องใช้ Claude Code?

### สำหรับ QA Automation โดยเฉพาะ:

| งาน | ก่อนใช้ Claude | หลังใช้ Claude |
|-----|---------------|----------------|
| เขียน test ใหม่ | นั่งพิมพ์เอง 30 นาที | บอก Claude 5 นาที |
| หา bug | ไล่ดูโค้ดทีละไฟล์ | ถาม Claude ตรงๆ |
| อ่านโค้ดคนอื่น | งง นาน | ให้ Claude อธิบาย |
| แก้ test ที่ fail | debug เอง | ให้ Claude ช่วยวิเคราะห์ |

## 1.3 ส่วนประกอบหลัก (ภาพรวม)

**อ่านแค่ให้รู้ว่ามีอะไรบ้าง - ยังไม่ต้องเข้าใจละเอียด**

```
คุณ (พิมพ์คำสั่ง)
    │
    ▼
┌─────────────────────────────────────┐
│         Claude Code                 │
│  ┌────────────────────────────┐     │
│  │ สมอง (AI)                  │     │  ← Claude ตัวหลัก
│  └────────────────────────────┘     │
│  ┌────────────────────────────┐     │
│  │ ความจำ (Context)           │     │  ← จำสิ่งที่คุยกัน
│  └────────────────────────────┘     │
│  ┌────────────────────────────┐     │
│  │ เครื่องมือ (Tools)          │     │  ← อ่าน/แก้ไฟล์, รัน command
│  └────────────────────────────┘     │
└─────────────────────────────────────┘
    │
    ▼
โปรเจคของคุณ (ไฟล์, โค้ด, tests)
```

## 1.4 รุ่นของ AI (เลือกได้)

**Claude มี 3 รุ่น - เลือกตามงาน:**

| รุ่น | เหมาะกับ | ราคา | ความเร็ว |
|-----|---------|------|---------|
| **Haiku** | งานง่ายๆ, ค้นหาไฟล์ | ถูกสุด | เร็วสุด |
| **Sonnet** | งานทั่วไป (แนะนำ) | กลาง | กลาง |
| **Opus** | งานซับซ้อน, ต้องคิดเยอะ | แพงสุด | ช้าสุด |

**วิธีเปลี่ยนรุ่น:**
```bash
/model haiku    # เปลี่ยนเป็น Haiku
/model sonnet   # เปลี่ยนเป็น Sonnet
/model opus     # เปลี่ยนเป็น Opus
```

**เคล็ดลับ:** เริ่มด้วย Sonnet ก่อน ค่อยปรับตามความต้องการ

### เลือก Model ได้จาก:
```bash
# ตอนเริ่ม session
claude --model opus
claude --model sonnet
claude --model haiku

# ระหว่าง session
/model opus
/model sonnet

# หรือ keyboard shortcut (ถ้าตั้งค่า terminal แล้ว)
Option+P (Mac) / Alt+P (Windows)
```

> **⚠️ สำหรับ macOS:** ถ้า Option key ไม่ทำงาน ต้องตั้งค่า Terminal ก่อน:
> - **Terminal.app:** Preferences → Profiles → Keyboard → ติ๊ก "Use Option as Meta key"
> - **iTerm2:** Preferences → Profiles → Keys → Left/Right Option key → เลือก "Esc+"

---

# 2. การติดตั้งและเริ่มต้นใช้งาน
> 🟢 **ระดับ: มือใหม่** | อ่าน: 10 นาที

## 2.1 System Requirements

| Platform | Requirement |
|----------|-------------|
| **macOS** | 10.15+ (Catalina or later) |
| **Linux** | Ubuntu 20.04+, Debian 10+ |
| **Windows** | Windows 10+ with WSL2 |
| **Node.js** | v18.0.0 or later |

## 2.2 การติดตั้ง

### วิธีที่แนะนำ: Native Installer (แนะนำ)

> **📌 หมายเหตุ:** วิธีนี้จะอัพเดทอัตโนมัติเมื่อมีเวอร์ชันใหม่

**macOS / Linux / WSL:**
```bash
curl -fsSL https://claude.ai/install.sh | bash
```

**Windows PowerShell:**
```powershell
irm https://claude.ai/install.ps1 | iex
```

**Windows CMD:**
```batch
curl -fsSL https://claude.ai/install.cmd -o install.cmd && install.cmd && del install.cmd
```

### วิธีอื่น (ไม่อัพเดทอัตโนมัติ)

**Homebrew (macOS):**
```bash
brew install --cask claude-code
```

**WinGet (Windows):**
```powershell
winget install Anthropic.ClaudeCode
```

### ติดตั้ง Stable Version (สำหรับ Production)
```bash
# Stable version - ผ่านการทดสอบแล้ว
curl -fsSL https://claude.ai/install.sh | bash -s stable
```

### ตรวจสอบว่าติดตั้งสำเร็จ
```bash
claude --version
# ควรเห็น: claude-code version x.x.x

claude doctor
# ตรวจสอบการติดตั้งอย่างละเอียด

claude --help
# ดูรายการคำสั่งทั้งหมด
```

> **⚠️ หมายเหตุ:** NPM installation (`npm install -g @anthropic-ai/claude-code`) เป็นวิธีเก่าที่ไม่แนะนำแล้ว ใช้ Native Installer แทน

## 2.3 Authentication

```bash
# First time setup
claude

# Follow prompts to authenticate:
# 1. Opens browser for Anthropic account login
# 2. Creates API key automatically
# 3. Stores securely in system keychain

# Or use API key directly
export ANTHROPIC_API_KEY="your-api-key"
claude
```

## 2.4 Initial Project Setup

```bash
# Navigate to your project (ใส่ path จริงของคุณ)
cd ~/Desktop/my-project          # ← เปลี่ยนเป็น path โปรเจคจริงของคุณ

# Start Claude Code
claude

# Initialize CLAUDE.md (memory file)
/init

# This creates .claude/CLAUDE.md with:
# - Project structure overview
# - Detected frameworks
# - Common commands
```

## 2.5 First Session Walkthrough

```bash
# Start Claude Code
claude

# Try these commands:
> What files are in this project?
> Explain the project structure
> How do I run the tests?

# Useful slash commands:
/help          # Show available commands
/clear         # Clear conversation
/context       # View context usage
/cost          # View token costs
```

### ✅ Best Practices: การติดตั้งและเริ่มต้น

| ✅ ทำ | ❌ ไม่ทำ |
|------|----------|
| ใช้ Native Installer (อัพเดทอัตโนมัติ) | ติดตั้งผ่าน npm (วิธีเก่า) |
| รัน `claude doctor` หลังติดตั้ง | ข้ามการตรวจสอบไปเลย |
| เริ่มจากโปรเจคจริงของตัวเอง | ลองในโฟลเดอร์ว่างๆ (ไม่เห็นประโยชน์) |
| รัน `/init` ให้ Claude เรียนรู้โปรเจค | พิมพ์คำสั่งเองทั้งหมดตั้งแต่แรก |
| ตั้ง `ANTHROPIC_API_KEY` ใน environment | ใส่ API key ตรงๆ ในโค้ด |
| ใช้ Stable version สำหรับงาน Production | ใช้ latest กับงานสำคัญ |

---

# 3. CLI Commands และ Options
> 🟢 **ระดับ: มือใหม่** (3.1-3.6) | 🟡 **กลาง** (3.7-3.8) | อ่าน: 15-30 นาที

## 3.1 Basic Commands

### Interactive Mode (REPL)
```bash
# Start new session
claude

# Start with initial prompt
claude "analyze this codebase"

# Continue last session
claude --continue
claude -c

# Resume specific session
claude --resume "session-name"
claude -r "session-name"
```

### Non-Interactive Mode (Print Mode)
```bash
# Execute and exit
claude -p "list all test files"

# With specific output format
claude -p "check coverage" --output-format json

# Pipe input
cat test-results.log | claude -p "analyze failures"
```

## 3.2 Essential Flags Reference

### Flags คืออะไร?

**Flags** = ตัวเลือกพิเศษที่เพิ่มหลังคำสั่ง `claude` เพื่อปรับการทำงาน

```
claude --flag-name value
       ↑           ↑
     ชื่อ flag   ค่าที่ต้องการ
```

> **💡 สำหรับมือใหม่:** ไม่จำเป็นต้องจำ flags ทั้งหมด เริ่มจาก 5 ตัวที่ใช้บ่อยก่อน

---

### 5 Flags ที่ QA ใช้บ่อยที่สุด

#### 1. `-c` หรือ `--continue` (ทำงานต่อจากเมื่อวาน)

**สถานการณ์:** เมื่อวานคุณให้ Claude ช่วยแก้ bug อยู่ แต่ต้องกลับบ้านก่อน วันนี้อยากทำต่อ

```bash
# ❌ ถ้าพิมพ์แค่ claude - Claude จะลืมทุกอย่างที่คุยกันเมื่อวาน
claude

# ✅ ใช้ -c เพื่อทำงานต่อจาก session ล่าสุด
claude -c
```

**ผลลัพธ์:**
```
> (Claude จำได้ว่าเมื่อวานกำลังแก้ bug อะไรอยู่)
> "สวัสดีครับ เมื่อวานเรากำลังแก้ bug ใน login function ค้างอยู่..."
```

---

#### 2. `-p` หรือ `--print` (ถามแล้วจบเลย)

**สถานการณ์:** อยากถามคำถามสั้นๆ แล้วได้คำตอบกลับมาเลย ไม่ต้องเปิด interactive mode

```bash
# ถามว่ามี test files กี่ไฟล์
claude -p "นับจำนวนไฟล์ test ในโปรเจคนี้"
```

**ผลลัพธ์:**
```
พบ 15 ไฟล์ test:
- tests/unit/: 8 ไฟล์
- tests/e2e/: 7 ไฟล์
```

**ตัวอย่างเพิ่มเติมสำหรับ QA:**
```bash
# ดูว่า test ล่าสุดผ่านไหม
claude -p "รัน npm test แล้วสรุปผลให้หน่อย"

# เช็คว่ามี TODO อะไรค้างบ้าง
claude -p "หา TODO comments ในโค้ดทั้งหมด"
```

---

#### 3. `--model` (เลือกความฉลาดของ AI)

**สถานการณ์:** งานง่ายๆ ใช้รุ่นถูก / งานยากๆ ใช้รุ่นฉลาด

```bash
# งานง่าย (หาไฟล์, นับจำนวน) - ใช้ haiku (ถูก + เร็ว)
claude --model haiku
> "หาไฟล์ที่ชื่อ login"

# งานยาก (วิเคราะห์ bug ซับซ้อน) - ใช้ opus (ฉลาดสุด)
claude --model opus
> "วิเคราะห์ว่าทำไม test นี้ถึง flaky"
```

> 💡 **ดูตาราง Model เต็มรูปแบบ:** Section 1.4 (Models และราคา)

---

#### 4. `-r` หรือ `--resume` (กลับไปทำงานเก่าที่ตั้งชื่อไว้)

**สถานการณ์:** คุณทำงานหลายเรื่องพร้อมกัน อยากสลับไปมาระหว่าง session

```bash
# ขั้นตอนที่ 1: ตั้งชื่อ session ปัจจุบัน (ใน Claude Code)
/rename bug-login

# ขั้นตอนที่ 2: วันหลังกลับมาทำต่อ
claude -r "bug-login"
```

**ตัวอย่างการใช้งานจริง:**
```bash
# Session 1: แก้ bug login
claude
/rename bug-login
> "ช่วยแก้ bug ใน login..."

# Session 2: เขียน test ใหม่
claude
/rename new-tests
> "ช่วยเขียน test สำหรับ..."

# สลับกลับไป session แก้ bug
claude -r "bug-login"
```

---

#### 5. `--max-turns` (จำกัดจำนวนรอบการทำงาน)

**สถานการณ์:** ไม่อยากให้ Claude ทำงานนานเกินไป หรือใช้เงินเยอะเกินไป

```bash
# ให้ Claude ทำงานได้สูงสุด 5 รอบ แล้วหยุด
claude --max-turns 5
> "รัน test แล้วแก้ไข error ทั้งหมด"
```

**ทำไมต้องจำกัด?**
- ป้องกันค่าใช้จ่ายบานปลาย
- บังคับให้ Claude สรุปผลเร็วขึ้น

---

### Flags อื่นๆ (สำหรับคนที่อยากรู้เพิ่ม)

<details>
<summary>คลิกเพื่อดู Flags เพิ่มเติม</summary>

#### ควบคุมค่าใช้จ่าย

```bash
# จำกัดงบไม่เกิน $2.50
claude --max-budget-usd 2.50
```

#### ควบคุม Output Format

```bash
# ได้ผลลัพธ์เป็น JSON (สำหรับใช้ใน script)
claude -p "นับ test files" --output-format json
```

#### ควบคุม Permission

```bash
# อ่านอย่างเดียว ไม่ให้แก้ไขไฟล์
claude --permission-mode plan

# อนุญาตแค่บาง tools
claude --allowedTools "Read,Bash(npm test:*)"
```

#### เข้าถึงโฟลเดอร์อื่น

```bash
# ให้ Claude เข้าถึงโฟลเดอร์ test-data ด้วย
claude --add-dir ../test-data
```

</details>

---

### สรุป Flags ยอดนิยม (Quick Reference)

| อยากทำอะไร | ใช้ Flag | ตัวอย่าง |
|-----------|---------|---------|
| ทำงานต่อจาก session ล่าสุด | `-c` | `claude -c` |
| ถามคำถามเดียวแล้วจบ | `-p` | `claude -p "คำถาม"` |
| ใช้ AI รุ่นถูก/แพง | `--model` | `claude --model haiku` |
| กลับไป session ที่ตั้งชื่อไว้ | `-r` | `claude -r "ชื่อ"` |
| จำกัดรอบการทำงาน | `--max-turns` | `claude --max-turns 5` |

## 3.3 Output Formats

> **📌 สำหรับมือใหม่:** ข้ามส่วนนี้ได้เลย! ใช้ default (text format) ก็เพียงพอ ส่วนนี้สำหรับคนที่ต้องเขียน script อัตโนมัติ

### Text (Default)
```bash
claude -p "summarize tests" --output-format text
# Returns: Plain text response
```

### JSON (For Scripting)
```bash
claude -p "check coverage" --output-format json
```

**JSON Output Structure:**
```json
{
  "result": "Claude's response text",
  "session_id": "unique-session-id",
  "usage": {
    "input_tokens": 500,
    "output_tokens": 250
  },
  "cost": 0.0045,
  "duration_ms": 2341,
  "messages": [...]
}
```

### Stream JSON (Real-time)
```bash
claude -p "run tests" --output-format stream-json
```

**Returns newline-delimited JSON:**
```json
{"type":"status","status":"starting"}
{"type":"message","role":"assistant","content":"Running..."}
{"type":"tool_use","tool":"Bash","input":{"command":"npm test"}}
{"type":"complete","result":"Tests passed"}
```

### Structured Output with Schema
```bash
SCHEMA='{
  "type":"object",
  "properties":{
    "passed":{"type":"integer"},
    "failed":{"type":"integer"},
    "coverage":{"type":"number"}
  }
}'

claude -p "analyze test results" \
  --output-format json \
  --json-schema "$SCHEMA"
```

## 3.4 Permission Modes

| Mode | Behavior | Best For |
|------|----------|----------|
| `default` | Prompts for permission | Interactive development |
| `acceptEdits` | Auto-approves file edits | Automated workflows |
| `plan` | Read-only, no edits | Code review, analysis |
| `dontAsk` | Denies unless pre-approved | Strict automation |
| `bypassPermissions` | Skips all prompts | Controlled environments |

```bash
# Examples
claude --permission-mode plan          # Read-only analysis
claude --permission-mode acceptEdits   # Auto-approve edits
claude --allowedTools "Read,Bash(npm test:*)" --permission-mode dontAsk
```

## 3.5 Interactive Commands (Slash Commands)

### Session Commands
| Command | Purpose |
|---------|---------|
| `/help` | Show help |
| `/clear` | Clear conversation |
| `/compact` | Compress context |
| `/context` | View context usage |
| `/cost` | View token costs |
| `/rename <name>` | Name current session |
| `/resume` | Resume previous session |

### Configuration Commands
| Command | Purpose |
|---------|---------|
| `/config` | Open settings |
| `/permissions` | Manage permissions |
| `/model` | Change model |
| `/memory` | Edit CLAUDE.md |
| `/init` | Initialize CLAUDE.md |

### Feature Commands
| Command | Purpose |
|---------|---------|
| `/plan` | Toggle plan mode |
| `/vim` | Toggle vim mode |
| `/agents` | Manage subagents |
| `/hooks` | Configure hooks |
| `/mcp` | Manage MCP servers |
| `/plugin` | Manage plugins |

### Diagnostic Commands
| Command | Purpose |
|---------|---------|
| `/doctor` | Run diagnostics |
| `/status` | Show version/account |
| `/bug` | Report a bug |

## 3.6 Prompt Engineering สำหรับ QA (สำคัญมาก!)

### ทำไม Prompt ถึงสำคัญ?

```
Prompt ดี = ผลลัพธ์ดี
Prompt แย่ = เสียเวลา + เสียเงิน
```

**เปรียบเทียบ:**

| Prompt แย่ | ผลลัพธ์ |
|-----------|---------|
| "แก้ bug" | Claude งง ไม่รู้ว่า bug อะไร ที่ไหน |
| "เขียน test" | Claude ไม่รู้ว่า test อะไร แบบไหน |

| Prompt ดี | ผลลัพธ์ |
|----------|---------|
| "แก้ bug ใน login.ts บรรทัด 45 error: undefined" | Claude แก้ได้ตรงจุด |
| "เขียน unit test สำหรับ validateEmail ใน utils.ts" | Claude เขียน test ได้ถูก |

---

### 5 หลักการเขียน Prompt ที่ดี (จำให้ขึ้นใจ!)

#### 1. บอก "อะไร" ให้ชัด (What)

```bash
# ❌ ไม่ชัด
> ดูโค้ด

# ✅ ชัดเจน
> ดูไฟล์ src/auth/login.ts แล้วอธิบายว่า function validateToken ทำงานยังไง
```

#### 2. บอก "ที่ไหน" (Where)

```bash
# ❌ ไม่บอกที่
> หา bug

# ✅ บอกที่ชัด
> หา bug ในไฟล์ tests/auth.test.ts ที่ทำให้ test "should reject expired token" fail
```

#### 3. บอก "ทำไม" หรือ Context (Why)

```bash
# ❌ ไม่มี context
> เขียน test

# ✅ มี context
> เขียน test สำหรับ checkout flow เพราะเมื่อวานมี bug ที่ user ไม่สามารถชำระเงินได้
```

#### 4. บอก "ผลลัพธ์ที่ต้องการ" (Expected Output)

```bash
# ❌ ไม่บอกผลลัพธ์
> ช่วยดู test

# ✅ บอกผลลัพธ์ชัด
> ช่วยดู test ใน auth.test.ts แล้วบอกว่า:
> 1. test ไหนซ้ำซ้อน
> 2. edge case ไหนที่ขาด
> 3. แนะนำ test ใหม่ที่ควรเพิ่ม
```

#### 5. แบ่งงานใหญ่เป็นงานย่อย (Break Down)

```bash
# ❌ งานใหญ่เกินไป
> refactor ระบบ authentication ทั้งหมด

# ✅ แบ่งเป็นขั้นตอน
> ขั้นตอนที่ 1: อ่านไฟล์ใน src/auth/ แล้วสรุปว่ามีอะไรบ้าง
> ขั้นตอนที่ 2: วิเคราะห์ว่าจุดไหนควร refactor
> ขั้นตอนที่ 3: เริ่ม refactor ทีละไฟล์
```

---

### Prompt Patterns สำหรับ QA (ใช้ได้เลย!)

#### Pattern 1: Bug Reproduction (จำลอง Bug)

```
> พบ bug: [อธิบาย bug]
> Error message: [paste error]
> ช่วย:
> 1. หาสาเหตุใน [ไฟล์/โฟลเดอร์]
> 2. เขียน test ที่จำลอง bug นี้
> 3. แก้ไข bug
> 4. รัน test ยืนยัน
```

**ตัวอย่างจริง:**
```
> พบ bug: user login แล้ว session หายไปหลัง 5 นาที
> Error message: "Session expired" ใน console
> ช่วย:
> 1. หาสาเหตุใน src/auth/session.ts
> 2. เขียน test ที่จำลอง bug นี้
> 3. แก้ไข bug
> 4. รัน test ยืนยัน
```

#### Pattern 2: Test Writing (เขียน Test)

```
> เขียน [ประเภท test] สำหรับ [function/feature]
> ครอบคลุม:
> - Happy path: [กรณีปกติ]
> - Edge cases: [กรณีพิเศษ]
> - Error cases: [กรณี error]
> ใช้ pattern เดียวกับ @[ไฟล์ test ที่มีอยู่]
```

**ตัวอย่างจริง:**
```
> เขียน unit test สำหรับ function calculateDiscount ใน src/pricing.ts
> ครอบคลุม:
> - Happy path: ส่วนลด 10%, 20%, 50%
> - Edge cases: ส่วนลด 0%, 100%, ราคา 0 บาท
> - Error cases: ส่วนลดติดลบ, ราคาติดลบ
> ใช้ pattern เดียวกับ @tests/pricing.test.ts
```

#### Pattern 3: Code Review (Review โค้ด)

```
> Review โค้ดใน [ไฟล์/PR]
> เน้นดู:
> - [สิ่งที่อยากให้เน้น]
> รายงานเป็น:
> - Critical: ต้องแก้
> - Warning: ควรแก้
> - Suggestion: ถ้ามีเวลา
```

**ตัวอย่างจริง:**
```
> Review โค้ดใน src/api/payment.ts
> เน้นดู:
> - Security: SQL injection, XSS
> - Error handling: try-catch ครบไหม
> - Edge cases: null, undefined
> รายงานเป็น:
> - Critical: ต้องแก้
> - Warning: ควรแก้
> - Suggestion: ถ้ามีเวลา
```

#### Pattern 4: Flaky Test Analysis (วิเคราะห์ Test ไม่เสถียร)

```
> Test [ชื่อ test] ไม่เสถียร (บางทีผ่าน บางทีไม่ผ่าน)
> ไฟล์: [path]
> Error ที่เห็น: [paste error]
> ช่วย:
> 1. วิเคราะห์สาเหตุที่ทำให้ flaky
> 2. แนะนำวิธีแก้
> 3. แก้ไขให้เสถียร
```

#### Pattern 5: Test Coverage Gap (หา Test ที่ขาด)

```
> วิเคราะห์ test coverage สำหรับ [feature/folder]
> บอก:
> 1. ไฟล์ไหนยังไม่มี test
> 2. function ไหนยังไม่ถูก test
> 3. edge case ไหนที่ขาด
> เรียงตามความสำคัญ
```

---

### ❌ Prompt ที่ควรหลีกเลี่ยง

| Prompt แย่ | ปัญหา | แก้เป็น |
|-----------|-------|--------|
| "ช่วยหน่อย" | ไม่บอกว่าช่วยอะไร | "ช่วยเขียน test สำหรับ X" |
| "ทำให้ดี" | "ดี" แปลว่าอะไร? | "ทำให้ test รันเร็วขึ้น โดย..." |
| "ดูทั้งหมด" | กิน context มาก | "ดูแค่ไฟล์ X และ Y" |
| "แก้ error" | ไม่บอก error อะไร | "แก้ error: [paste error]" |

---

### เคล็ดลับพิเศษ

#### ใช้ @ เพื่อชี้ไฟล์ตรงๆ
```bash
> ดู @src/auth/login.ts แล้วเขียน test
> เทียบ @tests/old.test.ts กับ @tests/new.test.ts
```

#### ใช้ /plan สำหรับงานซับซ้อน
```bash
/plan
> วิเคราะห์ระบบ authentication แล้ววางแผน refactor
# [Claude วิเคราะห์และเสนอแผน]
# [คุณ review แผน]
# กด Shift+Tab เพื่อเปลี่ยน permission mode (เช่น plan → normal)
# หรือพิมพ์คำสั่งถัดไปได้เลย — Claude จะเริ่มทำงานเมื่อได้รับคำสั่ง
> เริ่มทำตามแผนเลย
```

#### Copy-paste Error ตรงๆ
```bash
> Test fail ด้วย error นี้:
> """
> TypeError: Cannot read property 'id' of undefined
>     at UserService.getUser (src/user.ts:45:12)
> """
> ช่วยหาสาเหตุและแก้ไข
```

---

## 3.7 Context Engineering (ศาสตร์การให้ข้อมูล AI)

### Context Engineering คืออะไร?

**Prompt Engineering** = "ถามอย่างไร"
**Context Engineering** = "ให้ข้อมูลอะไร และอย่างไร"

```
┌─────────────────────────────────────────────────────────┐
│                   Context ที่ดี                          │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐    │
│  │ข้อมูลพื้น │  │ข้อมูลที่  │  │ตัวอย่าง  │  │ข้อจำกัด  │    │
│  │หลัง      │  │เกี่ยวข้อง │  │ที่ต้องการ│  │และกฎ    │    │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘    │
│                       ↓                                 │
│              [ AI เข้าใจงานชัด ]                         │
│                       ↓                                 │
│              [ ผลลัพธ์ตรงใจ ]                            │
└─────────────────────────────────────────────────────────┘
```

---

### 5 หลักการ Context Engineering

#### 1. ให้ Context ที่ "จำเป็น" ไม่ใช่ "ทั้งหมด"

```bash
# ❌ BAD: ให้ทุกอย่าง
> อ่านทุกไฟล์ใน src/ แล้วช่วยเขียน test
# Result: Claude อ่าน 100 ไฟล์, context เต็ม, งง

# ✅ GOOD: ให้เฉพาะที่จำเป็น
> อ่าน @src/auth/login.ts แล้วช่วยเขียน test
# Result: Claude โฟกัส, ได้คำตอบตรงประเด็น
```

#### 2. ให้ Context แบบ "Layered" (ชั้นๆ)

```bash
# เริ่มจากภาพรวม → ลงรายละเอียด

# Layer 1: ภาพรวม
> โปรเจคนี้เป็น E-commerce API ใช้ NestJS + PostgreSQL

# Layer 2: เฉพาะเจาะจง
> ระบบ payment อยู่ใน src/payment/ มี 5 ไฟล์

# Layer 3: จุดที่ต้องการ
> ต้องการเขียน test สำหรับ payment.service.ts
```

#### 3. ใช้ CLAUDE.md เป็น "Persistent Context"

```markdown
# CLAUDE.md - Context ที่ Claude จำตลอด

## Project Context
- Framework: NestJS
- Test: Jest + Supertest
- DB: PostgreSQL

## QA Rules
- ทุก test ต้องมี cleanup
- ใช้ test fixtures จาก tests/fixtures/
```

#### 4. "Priming" - ให้ตัวอย่างก่อน

```bash
# ❌ ไม่มี priming
> เขียน test สำหรับ user service

# ✅ มี priming (ให้ตัวอย่าง style ที่ต้องการ)
> ดูตัวอย่าง test ที่ @tests/auth.test.ts ก่อน
> แล้วเขียน test สำหรับ user service ในสไตล์เดียวกัน
```

#### 5. ใช้ Subagent สำหรับ "Retrieval"

```bash
# ให้ Subagent ดึงข้อมูลที่เกี่ยวข้องมาให้
> ใช้ subagent ค้นหาไฟล์ทั้งหมดที่เกี่ยวกับ payment
> แล้วสรุปมาให้ฉันว่ามีอะไรบ้าง

# จากนั้นค่อยถามคำถามหลัก
> จาก context ที่ได้ ช่วยเขียน integration test
```

---

### Context Engineering สำหรับ QA (ตัวอย่างจริง)

#### Scenario 1: เขียน Test ใหม่

```bash
# Step 1: ให้ context เกี่ยวกับ style
> ดูตัวอย่าง test pattern ที่ใช้ใน @tests/user.test.ts

# Step 2: ให้ context เกี่ยวกับ code ที่จะ test
> ดู @src/services/order.service.ts

# Step 3: สั่งงานพร้อม constraints
> เขียน test สำหรับ OrderService
> ตาม pattern เดียวกับ user.test.ts
> ครอบคลุม: create, update, delete
> ต้องมี test สำหรับ error cases ด้วย
```

#### Scenario 2: Debug Test ที่ Fail

```bash
# ให้ context ครบ: error + code + test
> Test fail ด้วย error นี้:
> """
> [paste error message]
> """
>
> Test อยู่ที่ @tests/payment.test.ts:45
> Code ที่ test อยู่ที่ @src/payment/service.ts
>
> วิเคราะห์และแก้ไขให้หน่อย
```

#### Scenario 3: Review Code

```bash
# ให้ context เกี่ยวกับ standard ที่ใช้
> Review PR นี้ โดยดู:
> 1. @src/new-feature.ts (code ใหม่)
> 2. @CLAUDE.md (standard ของเรา)
> 3. @tests/ (ดูว่ามี test ครบไหม)
>
> สรุปปัญหาเป็น checklist
```

---

### ❌ Context Mistakes ที่พบบ่อย

| Mistake | ปัญหา | วิธีแก้ |
|---------|-------|--------|
| ให้ข้อมูลมากเกินไป | Context เต็ม, Claude งง | ให้เฉพาะที่ relevant |
| ไม่ให้ตัวอย่าง | ได้ผลลัพธ์ไม่ตรง style | ใช้ Priming |
| ถามทุกอย่างใน 1 prompt | คำตอบไม่ลึก | แบ่งเป็นหลาย prompt |
| ไม่ใช้ CLAUDE.md | ต้องบอกซ้ำทุกครั้ง | เขียนสิ่งที่ใช้บ่อยใน CLAUDE.md |
| อ่านไฟล์ใน main context | กิน context | ใช้ Subagent retrieval |

---

### Context Budget Management

```bash
# ดู context ที่ใช้อยู่
/context

# ถ้าใช้เกิน 50% → พิจารณา compact
/compact

# ถ้าเริ่มงานใหม่ → clear แล้วให้ context ใหม่
/clear
```

**กฎง่ายๆ:**
- **< 50%**: ทำงานได้ปกติ
- **50-70%**: ใช้ `/compact` เพื่อบีบอัด
- **> 70%**: พิจารณา `/clear` และเริ่มใหม่ หรือใช้ Subagent

---

## 3.8 PRP - Product Requirements Prompt (Pro Technique)

> **📌 สำหรับมือใหม่:** PRP เป็นเทคนิคขั้นสูง ข้ามไปอ่าน Section 4 ก่อนได้เลย!

> **ที่มา:** แนวคิดนี้มาจาก [Context Engineering Intro](https://github.com/coleam00/context-engineering-intro) โดย Cole Medin
>
> **หลักการ:** "Context Engineering is 10x better than Prompt Engineering"

### สับสนใช่ไหม? ดูตารางนี้ก่อน

| ถ้าอยากได้... | ใช้... | เมื่อไหร่ |
|--------------|--------|----------|
| ถามให้ได้คำตอบดี | **Prompt Engineering** (Section 3.6) | ทุกครั้งที่พิมพ์ |
| Claude จำข้อมูลทุก session | **CLAUDE.md** (Section 4) | ตั้งครั้งเดียว ใช้ตลอด |
| คำสั่งลัดสำหรับงานซ้ำๆ | **Skill** (Section 7) | งานที่ทำบ่อย > 3 ครั้ง/สัปดาห์ |
| เอกสาร spec ให้ Claude ทำงานซับซ้อน | **PRP** (ส่วนนี้) | งานใหญ่ที่ต้อง plan ก่อนทำ |

### PRP คืออะไร?

**PRD** (Product Requirements Document) = เอกสาร requirements สำหรับคน
**PRP** (Product Requirements Prompt) = เอกสาร requirements สำหรับ AI

```
┌─────────────────────────────────────────────────────────────┐
│                      PRP Structure                          │
├─────────────────────────────────────────────────────────────┤
│  1. Goal/Why/What      → ต้องการอะไร ทำไม                    │
│  2. All Needed Context → ข้อมูลทั้งหมดที่จำเป็น               │
│  3. Implementation     → ขั้นตอนการทำ                        │
│  4. Validation         → วิธีตรวจสอบว่าถูกต้อง                │
│  5. Anti-Patterns      → สิ่งที่ห้ามทำ                        │
└─────────────────────────────────────────────────────────────┘
```

---

### ทำไมต้องใช้ PRP?

| วิธีปกติ | ใช้ PRP |
|---------|---------|
| บอกทีละนิด → AI ถามกลับ → เสียเวลา | บอกครบตั้งแต่แรก → ได้ผลลัพธ์เลย |
| AI เดา context → อาจผิด | AI มี context ครบ → ถูกต้อง |
| ลืมบอก edge cases → bug | เขียน gotchas ไว้ → ไม่พลาด |
| ไม่มี validation → ไม่รู้ว่าถูก | มี validation loop → รันทดสอบได้ |

---

### PRP Template สำหรับ QA

```markdown
# PRP: [ชื่องาน]

## 1. Goal (เป้าหมาย)
- **What**: [ต้องการทำอะไร]
- **Why**: [ทำไปทำไม - business value]
- **Success Criteria**: [วัดผลสำเร็จอย่างไร]

## 2. Context (ข้อมูลที่จำเป็น)

### 2.1 Codebase Structure
```
[tree output หรือ โครงสร้างไฟล์ที่เกี่ยวข้อง]
```

### 2.2 Relevant Files
- `@path/to/file1.ts` - [อธิบายหน้าที่]
- `@path/to/file2.ts` - [อธิบายหน้าที่]

### 2.3 Documentation References
- [Link to API docs]
- [Link to testing guidelines]

### 2.4 Critical Gotchas ⚠️
- [สิ่งที่ต้องระวัง #1]
- [สิ่งที่ต้องระวัง #2]
- [Edge cases ที่ต้องครอบคลุม]

## 3. Implementation Blueprint

### Task 1: [ชื่อ task]
- Files to modify: [list]
- What to do: [รายละเอียด]

### Task 2: [ชื่อ task]
...

## 4. Validation Loop

### Level 1: Syntax & Style
- [ ] Linting passes
- [ ] Type check passes

### Level 2: Unit Tests
- [ ] Test covers happy path
- [ ] Test covers error cases
- [ ] Test covers edge cases

### Level 3: Integration
- [ ] E2E test passes
- [ ] Manual testing OK

## 5. Anti-Patterns (ห้ามทำ!)
- ❌ [สิ่งที่ห้ามทำ #1]
- ❌ [สิ่งที่ห้ามทำ #2]
```

---

### ตัวอย่าง PRP สำหรับ QA: เขียน API Test

```markdown
# PRP: เขียน Integration Test สำหรับ Payment API

## 1. Goal
- **What**: เขียน integration test สำหรับ POST /api/payments
- **Why**: เพื่อให้ CI/CD สามารถตรวจจับ regression ได้
- **Success Criteria**:
  - Coverage > 80% สำหรับ payment module
  - Test ครอบคลุมทุก status codes (200, 400, 401, 500)

## 2. Context

### 2.1 Codebase Structure
```
src/
├── payment/
│   ├── payment.controller.ts   # API endpoints
│   ├── payment.service.ts      # Business logic
│   └── payment.dto.ts          # Request/Response types
tests/
├── fixtures/
│   └── payment.fixtures.ts     # Test data
└── payment/
    └── payment.test.ts         # <<<< เขียนที่นี่
```

### 2.2 Relevant Files
- `@src/payment/payment.controller.ts` - API ที่จะ test
- `@src/payment/payment.dto.ts` - ดู request/response format
- `@tests/auth/auth.test.ts` - ดู pattern การเขียน test

### 2.3 Documentation
- Payment API spec: @docs/api/payment.md
- Testing guide: @CLAUDE.md (section: Testing)

### 2.4 Critical Gotchas ⚠️
- ต้อง mock payment gateway (อย่าเรียก production!)
- ต้อง cleanup test data หลังจบแต่ละ test
- Authentication ใช้ Bearer token จาก test fixtures
- Database ใช้ test container, ไม่ใช่ production

## 3. Implementation Blueprint

### Task 1: Setup test file
- Create `tests/payment/payment.test.ts`
- Import: supertest, app, fixtures

### Task 2: Write test cases
1. POST /api/payments - success (201)
2. POST /api/payments - invalid amount (400)
3. POST /api/payments - unauthorized (401)
4. POST /api/payments - gateway error (500)

### Task 3: Add fixtures
- Valid payment request
- Invalid payment request
- Mock gateway responses

## 4. Validation Loop

### Level 1: Syntax
- [ ] `npm run lint` passes
- [ ] `npm run typecheck` passes

### Level 2: Unit Tests
- [ ] `npm test payment` passes
- [ ] Coverage > 80%

### Level 3: Integration
- [ ] `npm run test:e2e` passes
- [ ] Manual test with Postman

## 5. Anti-Patterns
- ❌ อย่าเรียก production payment gateway
- ❌ อย่า hardcode test credentials ใน code
- ❌ อย่าใช้ setTimeout สำหรับ async waits
- ❌ อย่าลืม cleanup test data
```

---

### วิธีใช้ PRP กับ Claude Code

**Step 1: สร้างไฟล์ PRP**
```bash
# สร้างไฟล์ใน project
touch PRPs/payment-test.md
# เขียนเนื้อหาตาม template
```

**Step 2: ให้ Claude อ่าน PRP**
```bash
> อ่าน @PRPs/payment-test.md แล้วทำตามทั้งหมด
```

**Step 3: Claude จะ**
1. อ่าน PRP ทั้งหมด
2. อ่านไฟล์ที่ระบุใน Context
3. ทำตาม Implementation Blueprint
4. รัน Validation Loop
5. หลีกเลี่ยง Anti-Patterns

---

### เมื่อไหร่ควรใช้ PRP?

| ✅ ควรใช้ | ❌ ไม่จำเป็น |
|----------|-------------|
| งานซับซ้อน หลายขั้นตอน | งานง่ายๆ 1-2 ขั้นตอน |
| ต้องการ consistency | one-off tasks |
| งานที่ทำซ้ำหลายครั้ง | exploratory/research |
| Critical features | quick fixes |
| Team collaboration | solo debugging |

---

### Tips สำหรับ PRP

1. **เก็บ PRP ไว้ใน repo** → ใช้ซ้ำได้, version control
2. **อัพเดท Gotchas** → ทุกครั้งที่เจอปัญหาใหม่
3. **ใช้ร่วมกับ CLAUDE.md** → PRP = specific task, CLAUDE.md = global rules
4. **Start simple** → ไม่ต้องเขียนครบทุก section ตั้งแต่แรก

### ✅ Best Practices: CLI Commands

| ✅ ทำ | ❌ ไม่ทำ |
|------|----------|
| ใช้ `/compact` เมื่อ context > 50% | ปล่อยให้ context เต็มเอง |
| ใช้ `/plan` ก่อนงานใหญ่ | สั่งงานยาวทีเดียวโดยไม่วางแผน |
| บอก context ให้ชัด: framework, ภาษา, ไฟล์ | พิมพ์สั้นๆ "แก้ bug" โดยไม่บอกรายละเอียด |
| ใช้ `--model haiku` สำหรับงานง่ายๆ | ใช้ Opus ทุกงาน (เปลืองเงิน) |
| ใช้ `-p` สำหรับ quick question ที่ไม่ต้อง interactive | เปิด session ทุกครั้งแม้แค่ถามสั้นๆ |
| รัน `/cost` เป็นระยะเพื่อติดตามค่าใช้จ่าย | ไม่เคยดูว่าใช้ token ไปเท่าไหร่ |

---

# 4. CLAUDE.md - Memory ของ Claude
> 🟡 **ระดับ: เริ่มใช้แล้ว** | อ่าน: 10 นาที

## 4.1 CLAUDE.md คืออะไร? (อธิบายง่ายๆ)

### เปรียบเทียบให้เห็นภาพ

**ลองนึกว่าคุณมีผู้ช่วยใหม่:**
- วันแรก คุณบอกเขาว่า "เราใช้ Jest สำหรับ test, รัน test ด้วย npm test"
- วันที่สอง คุณต้องบอกใหม่... เพราะเขาลืม
- วันที่สาม คุณต้องบอกใหม่อีก...

**น่าเบื่อใช่ไหม?**

**CLAUDE.md = กระดาษโน้ตที่ผู้ช่วยอ่านทุกวัน**

```
สิ่งที่คุณเขียนใน CLAUDE.md → Claude จะจำตลอด (ทุก session)
```

### ตัวอย่างง่ายๆ

```markdown
# บันทึกสำหรับ Claude

## วิธีรัน Test
- ใช้คำสั่ง: npm test

## กฎที่ต้องทำตาม
- ต้องรัน test ก่อน commit

## ข้อควรระวัง
- ห้ามแก้ไฟล์ใน src/core/
```

**แค่นี้! Claude จะอ่านทุกครั้งที่เริ่ม session**

## 4.2 ไฟล์ CLAUDE.md เก็บที่ไหน?

### สำหรับมือใหม่: ใช้แค่ 2 ที่นี้พอ

| ที่เก็บ | ใช้เมื่อ | ตัวอย่าง |
|--------|---------|---------|
| `CLAUDE.md` (root โปรเจค) | บันทึกสำหรับโปรเจคนี้ | วิธีรัน test, coding style |
| `~/.claude/CLAUDE.md` (home) | บันทึกส่วนตัวทุกโปรเจค | สไตล์การทำงานของคุณ |

### สำหรับคนที่อยากรู้มากกว่านี้ (ข้ามได้):

```
ลำดับความสำคัญ (Claude อ่านจากบนลงล่าง):

1. ระดับองค์กร     → IT กำหนด (คุณแก้ไม่ได้)
2. ระดับโปรเจค     → ./CLAUDE.md (แชร์กับทีม)
3. ระดับส่วนตัว    → ~/.claude/CLAUDE.md (ของคุณคนเดียว)
```

## 4.3 สร้าง CLAUDE.md

### วิธีที่ 1: ใช้ /init
```bash
claude
/init
# Claude จะวิเคราะห์ codebase และสร้าง CLAUDE.md อัตโนมัติ
```

### วิธีที่ 2: สร้างเอง
```bash
# Project-level (shared with team)
touch ./CLAUDE.md
# หรือ
touch ./.claude/CLAUDE.md

# Personal overrides (gitignored)
touch ./.claude/CLAUDE.local.md
```

## 4.4 เนื้อหาที่ควรใส่

### ✅ สิ่งที่ควรใส่:

```markdown
# Project Name

## Commands
- `npm test` - Run all tests
- `npm run test:unit` - Run unit tests only
- `npm run test:e2e` - Run E2E tests
- `npm run lint` - Run ESLint
- `npm run build` - Build for production

## Code Style
- Use TypeScript strict mode
- 2-space indentation
- Single quotes for strings
- Trailing commas in multiline

## Testing Conventions
- Test files: `*.test.ts` or `*.spec.ts`
- Location: `tests/` directory mirrors `src/`
- Pattern: describe() → it() → expect()
- Mock external dependencies always

## Architecture
- `/src` - Application source code
- `/tests` - Test files
- `/config` - Configuration files
- `/scripts` - Build and utility scripts

## Important Notes
- NEVER commit .env files
- Always run tests before committing
- Database migrations must be backwards compatible

## Common Gotchas
- The `auth` service requires Redis running
- E2E tests need Docker for database
- Use `npm run db:seed` to setup test data
```

### ❌ สิ่งที่ไม่ควรใส่:

- Information Claude can figure out from reading code
- Standard language conventions
- Detailed API documentation (link instead)
- Information that changes frequently
- Long tutorials or explanations

### 📋 ตัวอย่าง CLAUDE.md สำหรับ QA Team (Copy-Paste ได้เลย!)

```markdown
# CLAUDE.md สำหรับ QA Automation

## โปรเจคนี้คืออะไร
- ชื่อ: [ชื่อโปรเจค]
- ประเภท: [Web App / API / Mobile]
- Tech Stack: [เช่น React + Node.js + PostgreSQL]

## Test Framework
- **API Test:** Robot Framework + RequestsLibrary
- **Web Test:** Robot Framework + Browser Library (Playwright)
- **Unit Test:** Jest (สำหรับ JS/TS)

## คำสั่งที่ใช้บ่อย
- รัน test ทั้งหมด: `robot tests/`
- รัน smoke test: `robot --include smoke tests/`
- รัน API test: `robot tests/api/`
- รัน Web test: `robot tests/web/`
- ดู report: `open results/report.html`

## โครงสร้างโฟลเดอร์ Test
```
tests/
├── api/              # API tests
├── web/              # Web UI tests
├── data/             # Test data
├── resources/        # Shared keywords
└── pages/            # Page Objects
```

## กฎของทีม
- ทุก test ต้องมี [Tags] (api/web, smoke/regression, positive/negative)
- ใช้ data-testid สำหรับ Web locators
- Test ต้อง independent (รันแยกได้)
- Commit message: `[TICKET-123] description`

## Environment
- DEV: https://dev.example.com
- STAGING: https://staging.example.com
- API Base: ${BASE_URL}/api/v1

## ข้อควรระวัง
- ห้ามรัน test กับ Production!
- ต้อง VPN ก่อนรัน test กับ Staging
- Test data จะ reset ทุกวันจันทร์ 6:00 AM
```

> **วิธีใช้:** Copy template นี้ไปวางในไฟล์ `CLAUDE.md` ที่ root ของโปรเจค แล้วแก้ไขให้ตรงกับโปรเจคของคุณ

## 4.5 Modular Rules (.claude/rules/)

สำหรับ project ใหญ่ แยก rules ตาม topic:

```
.claude/
└── rules/
    ├── code-style.md
    ├── testing.md
    ├── security.md
    └── api/
        └── endpoints.md
```

### Path-specific Rules (ใช้ frontmatter)

```markdown
---
paths:
  - "src/**/*.ts"
  - "tests/**/*.test.ts"
---

# TypeScript Testing Rules

- All test files must use Jest
- Use describe() blocks to organize
- One assertion per test when possible
```

## 4.6 File Imports ใน CLAUDE.md

```markdown
# Import other files
See @README.md for overview
See @package.json for npm commands

# Import specific files
@docs/api-guide.md

# Import personal instructions
@~/.claude/my-conventions.md
```

## 4.7 CLAUDE.md สำหรับ QA Automation

```markdown
# QA Automation Project

## Test Framework
- Framework: Playwright / Cypress / Jest
- Test files: `tests/**/*.spec.ts`
- Run all: `npm test`
- Run specific: `npm test -- --grep "login"`

## Test Conventions
- Use Page Object Model for E2E
- AAA Pattern: Arrange → Act → Assert
- Descriptive test names: `should_behavior_when_condition`
- Keep tests independent and isolated

## Test Data
- Fixtures location: `tests/fixtures/`
- Use factory functions for dynamic data
- Clean up test data in afterEach()

## CI/CD Requirements
- All tests must pass before merge
- Minimum coverage: 80%
- No flaky tests allowed

## Common Commands
```bash
npm test                    # All tests
npm test -- --coverage      # With coverage
npm run test:e2e           # E2E only
npm run test:unit          # Unit only
npm run test:watch         # Watch mode
```

## Debugging
- Use `test.only()` to run single test
- Debug with: `DEBUG=pw:api npm test`
- Screenshots saved to: `test-results/`

## Known Issues
- E2E tests need `docker-compose up -d` first
- Some tests flaky on CI - retry 2 times
- Auth tests require valid JWT secret in .env.test
```

### ✅ Best Practices: CLAUDE.md

| ✅ ทำ | ❌ ไม่ทำ |
|------|----------|
| เริ่มด้วย `/init` แล้วเพิ่มเองทีหลัง | เขียน CLAUDE.md เองทั้งหมดตั้งแต่แรก |
| ใส่ test commands ที่ใช้จริง (`npm test`) | ใส่ข้อมูลที่ไม่เกี่ยวกับโปรเจค |
| อัพเดท Known Issues เมื่อเจอปัญหาใหม่ | เขียนครั้งเดียวแล้วไม่แตะอีก |
| ใช้ Modular Rules แยกตาม concern | ยัดทุกอย่างในไฟล์เดียวจนยาวมาก |
| Commit CLAUDE.md เข้า git ร่วมกับทีม | เก็บไว้ local คนเดียว |
| ใส่ Project Structure overview | ใส่ข้อมูลส่วนตัวหรือ credentials |

---

# 5. Settings และ Permissions
> 🔵 **ระดับ: ใช้คล่องแล้ว** | อ่าน: 10 นาที | มือใหม่: ใช้ `/config` ผ่าน CLI แทนได้

## 5.1 Configuration Hierarchy

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Managed Settings (Enterprise - Highest Priority)        │
│    macOS: /Library/Application Support/ClaudeCode/         │
│    Linux: /etc/claude-code/                                 │
│    Windows: C:\Program Files\ClaudeCode\                    │
├─────────────────────────────────────────────────────────────┤
│ 2. Command Line Arguments (Per-session)                     │
│    Example: claude --model opus                             │
├─────────────────────────────────────────────────────────────┤
│ 3. Local Settings (Personal project)                        │
│    File: .claude/settings.local.json                        │
│    Note: Gitignored, not shared                             │
├─────────────────────────────────────────────────────────────┤
│ 4. Project Settings (Team-shared)                           │
│    File: .claude/settings.json                              │
│    Note: Checked into Git                                   │
├─────────────────────────────────────────────────────────────┤
│ 5. User Settings (Personal defaults - Lowest)               │
│    File: ~/.claude/settings.json                            │
└─────────────────────────────────────────────────────────────┘
```

## 5.2 Settings File Structure

> **📌 สำหรับมือใหม่:** ไม่ต้องแก้ JSON เอง! ใช้คำสั่ง `/config` หรือ `/permissions` ใน Claude Code แทนได้ ส่วนนี้สำหรับคนที่ต้องการ fine-tune ด้วยมือ

```json
// .claude/settings.json
{
  // Permission Rules
  "permissions": {
    "allow": [
      "Read",
      "Bash(npm:*)",
      "Bash(git:*)",
      "Edit(/tests/**)"
    ],
    "ask": [
      "Bash(docker:*)",
      "Edit(/src/**)"
    ],
    "deny": [
      "Bash(rm -rf:*)",
      "Read(.env*)",
      "Read(./secrets/**)"
    ],
    "additionalDirectories": [
      "../shared-lib",
      "/path/to/fixtures"
    ],
    "defaultMode": "default"
  },

  // Model Preferences
  "model": "sonnet",

  // Environment Variables
  "env": {
    "NODE_ENV": "test",
    "DEBUG": "true"
  },

  // MCP Server Restrictions
  "allowedMcpServers": [
    { "serverName": "github" },
    { "serverUrl": "https://mcp.company.com/*" }
  ],

  // Plugin Settings
  "enabledPlugins": {
    "test-runner@qa-marketplace": true
  },

  // Session Settings
  "cleanupPeriodDays": 30,
  "respectGitignore": true
}
```

## 5.3 Permission Patterns

### Allow Patterns

```json
{
  "permissions": {
    "allow": [
      // Exact match
      "Bash(npm test)",

      // Prefix match (word boundary)
      "Bash(npm run:*)",      // npm run lint, npm run test

      // Glob pattern
      "Bash(git*)",           // git, gitk, git-lfs

      // File paths
      "Read(/tests/**)",
      "Edit(/tests/**/*.ts)",

      // All uses of tool
      "Read",

      // Domain restriction
      "WebFetch(domain:github.com)"
    ]
  }
}
```

### Pattern Examples

| Pattern | Matches | Does NOT Match |
|---------|---------|----------------|
| `Bash(npm run:*)` | `npm run test`, `npm run lint` | `npm install` |
| `Bash(npm*)` | `npm`, `npmrc`, `npm-check` | `npx` |
| `Read(/tests/**)` | `/tests/unit/foo.ts` | `/src/foo.ts` |
| `Edit(*.test.ts)` | `auth.test.ts` | `auth.ts` |
| `WebFetch(domain:api.com)` | `https://api.com/data` | `https://other.com` |

## 5.4 QA-Specific Settings

```json
// .claude/settings.json - สำหรับ QA Team
{
  "permissions": {
    "defaultMode": "acceptEdits",
    "additionalDirectories": [
      "./tests",
      "./fixtures",
      "./test-data"
    ],
    "allow": [
      "Read",
      "Grep",
      "Glob",
      "Bash(npm run test:*)",
      "Bash(npm run lint:*)",
      "Bash(npx playwright:*)",
      "Bash(npx jest:*)",
      "Bash(git status)",
      "Bash(git diff:*)",
      "Edit(/tests/**)",
      "Write(/tests/**)"
    ],
    "ask": [
      "Edit(/src/**)",
      "Bash(docker:*)"
    ],
    "deny": [
      "Bash(rm -rf:*)",
      "Bash(curl:*)",
      "Bash(wget:*)",
      "Read(.env*)",
      "Read(./secrets/**)",
      "Write(/src/**)"
    ]
  },
  "model": "sonnet",
  "env": {
    "NODE_ENV": "test",
    "CI": "true"
  }
}
```

## 5.5 Configure via CLI

```bash
# Open settings UI
/config

# Manage permissions interactively
/permissions

# Change model
/model opus
/model sonnet
/model haiku
```

---

# 6. MCP (Model Context Protocol)
> 🔵 **ระดับ: ใช้คล่องแล้ว** | อ่าน: 5 นาที (6.1-6.3) หรือ 30 นาที (ทั้งหมด)
>
> 🚀 **สำหรับมือใหม่:** อ่านแค่ 6.1-6.3 (MCP คืออะไร + Playwright MCP) ก็พอ!
> Section 6.4 เป็นต้นไปเป็นขั้นสูง ข้ามได้เลย กลับมาอ่านเมื่อคล่องแล้ว
> Claude Code ทำงานได้ดีโดยไม่ต้องติดตั้ง MCP ใดๆ

## 6.1 MCP คืออะไร? (อธิบายง่ายๆ)

### ปกติ Claude ทำอะไรได้บ้าง?

```
✅ อ่านไฟล์ในโปรเจค
✅ แก้ไขโค้ด
✅ รัน commands
❌ เข้า GitHub ไม่ได้
❌ ดู Database ไม่ได้
❌ ส่ง Slack ไม่ได้
```

### MCP = ปลั๊กอินที่ช่วยให้ Claude เข้าถึงสิ่งเหล่านี้ได้

**เปรียบเทียบง่ายๆ:**

| เหมือนกับ... | ในแง่ที่... |
|-------------|-------------|
| USB ต่อกับคอม | เชื่อมต่อ Claude กับ tools ภายนอก |
| App Store | มีหลาย MCP ให้เลือกติดตั้ง |
| Extension ของ Browser | เพิ่มความสามารถให้ Claude |

### ตัวอย่างที่เห็นภาพ

**ก่อนติดตั้ง MCP:**
```
คุณ: "ดู PR #123 ใน GitHub ให้หน่อย"
Claude: "ขอโทษ ผมเข้า GitHub ไม่ได้"
```

**หลังติดตั้ง GitHub MCP:**
```
คุณ: "ดู PR #123 ใน GitHub ให้หน่อย"
Claude: "PR #123 มีการแก้ไขไฟล์ 5 ไฟล์... [แสดงรายละเอียด]"
```

> **⚠️ หมายเหตุสำคัญเกี่ยวกับ MCP:**
> - MCP เป็นเทคโนโลยีใหม่ที่กำลังพัฒนาอย่างต่อเนื่อง
> - URL และวิธีการติดตั้งอาจเปลี่ยนแปลงได้
> - ควรตรวจสอบเอกสารล่าสุดที่ https://modelcontextprotocol.io
> - MCP บางตัว (เช่น Sentry, Slack) อาจต้องมี account และ API key

## 6.2 MCP ยอดนิยมสำหรับ QA

| MCP | ใช้ทำอะไร | ตัวอย่างคำสั่ง |
|-----|---------|---------------|
| **GitHub** | ดู PR, Issues, CI status | "ดู PR ล่าสุด", "สร้าง issue" |
| **Playwright** | ควบคุม browser สำหรับ Web Testing | "เปิด browser ไป google.com" |
| **Sentry** | ดู errors ใน production | "มี error อะไรบ้างวันนี้" |
| **Database** | query ข้อมูล test | "ดู user ที่ email = x" |
| **Slack** | ส่งแจ้งเตือน | "ส่งผล test ไป channel QA" |

> **💡 Tip สำหรับ QA:** เริ่มจาก **Playwright MCP** ก่อน (สำหรับ Web Testing) แล้วค่อยเพิ่ม GitHub MCP ทีหลัง

---

### 🎭 Playwright MCP สำหรับ Web Testing (สำคัญสำหรับ QA!)

**Playwright MCP คืออะไร?**
- MCP ที่ให้ Claude ควบคุม browser ได้โดยตรง
- เหมาะสำหรับ exploratory testing, debug UI issues
- Claude สามารถ click, type, screenshot, ดู console errors

**วิธีติดตั้ง Playwright MCP:**

```bash
# Step 1: ติดตั้ง Playwright MCP server
npm install -g @anthropic/mcp-server-playwright

# Step 2: เพิ่มเข้า Claude Code
claude mcp add playwright -- npx @anthropic/mcp-server-playwright
```

**วิธีใช้งาน (หลังติดตั้ง):**

```bash
# ใน Claude Code - ลองพิมพ์:
> เปิด browser ไปที่ https://example.com แล้ว screenshot มาให้ดู

# Claude จะ:
# 1. เปิด browser
# 2. ไปที่ URL
# 3. ถ่าย screenshot
# 4. แสดงให้คุณดู

# ตัวอย่างอื่นๆ:
> เปิด https://myapp.com/login แล้วลอง login ด้วย user test@test.com
> ตรวจสอบว่าปุ่ม Submit มี text ว่าอะไร
> หา element ที่มี data-testid="error-message"
> ดู console errors ในหน้านี้
```

**Use Cases สำหรับ QA:**

| Task | ตัวอย่างคำสั่ง |
|------|---------------|
| **Exploratory Testing** | "เปิด staging แล้วลอง flow checkout ให้หน่อย" |
| **Debug UI Bug** | "เปิดหน้า X แล้วดูว่า element Y แสดงถูกต้องไหม" |
| **Screenshot for Bug Report** | "screenshot หน้านี้ให้หน่อย ใช้ทำ bug report" |
| **Check Responsive** | "เปิดหน้านี้ใน mobile viewport แล้ว screenshot" |
| **Find Locators** | "หา data-testid ของปุ่ม Submit ให้หน่อย" |

**ข้อควรระวัง:**
- Playwright MCP จะเปิด browser จริง (ไม่ใช่ headless)
- ใช้สำหรับ exploratory/debug ไม่ใช่รัน test suite
- สำหรับ automated test ให้เขียน Robot Framework/Playwright test แทน

---

## 6.3 วิธีติดตั้ง MCP (สำหรับมือใหม่)

### ติดตั้ง GitHub MCP (ตัวอย่าง)

```bash
claude mcp add --transport http github https://api.githubcopilot.com/mcp/
```

**แค่นี้!** ครั้งหน้าที่เปิด Claude จะสามารถเข้า GitHub ได้

### ดู MCP ที่ติดตั้งแล้ว

```bash
claude mcp list
```

### ลบ MCP

```bash
claude mcp remove github
```

---

> **🚧 ตั้งแต่ Section 6.4 เป็นต้นไปเป็นเนื้อหาขั้นสูง (Advanced)**
> สำหรับมือใหม่: ข้ามไปยัง [Section 7. Skills](#7-skills-คำสั่งลัดที่สร้างเอง) ได้เลย!

## 6.4 ภาพรวม Architecture (ข้ามได้ถ้าไม่สนใจ)

```
คุณ (พิมพ์คำสั่ง)
    │
    ▼
Claude Code ────────► MCP Server ────────► External Service
             (ส่งคำขอ)              (เชื่อมต่อ)

ตัวอย่าง:
"ดู PR #123" ──► GitHub MCP ──► GitHub.com ──► ข้อมูล PR กลับมา
```

## 6.5 การติดตั้ง MCP Servers (Advanced)

### ผ่าน CLI
```bash
# HTTP Server
claude mcp add --transport http github https://api.githubcopilot.com/mcp/

# Stdio Server (local)
claude mcp add --transport stdio db \
  -- npx -y @bytebase/dbhub \
  --dsn "postgresql://user:pass@localhost/testdb"

# With headers
claude mcp add --transport http sentry \
  --header "Authorization: Bearer ${SENTRY_TOKEN}" \
  https://mcp.sentry.dev/mcp
```

### Scopes

| Scope | Location | Shared? | Command |
|-------|----------|---------|---------|
| **Local** | `~/.claude.json` | No | `--scope local` (default) |
| **Project** | `.mcp.json` | Yes (Git) | `--scope project` |
| **User** | `~/.claude.json` | No | `--scope user` |

```bash
# Project scope (shared with team)
claude mcp add --transport http github --scope project https://api.githubcopilot.com/mcp/

# User scope (all projects)
claude mcp add --transport http personal-tool --scope user https://my-tool.com/mcp
```

## 6.6 Configuration Files

### .mcp.json (Project-level)

```json
{
  "mcpServers": {
    "github": {
      "type": "http",
      "url": "https://api.githubcopilot.com/mcp/"
    },
    "sentry": {
      "type": "http",
      "url": "https://mcp.sentry.dev/mcp",
      "headers": {
        "Authorization": "Bearer ${SENTRY_TOKEN}"
      }
    },
    "test-database": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "@bytebase/dbhub"],
      "env": {
        "DATABASE_URL": "${TEST_DB_URL:-postgresql://localhost/testdb}"
      }
    },
    "local-tool": {
      "type": "stdio",
      "command": "node",
      "args": ["./tools/my-mcp-server.js"],
      "cwd": "/path/to/directory"
    }
  }
}
```

### Configuration Elements

| Element | Type | Required | Description |
|---------|------|----------|-------------|
| `type` | string | Yes | `http`, `stdio`, or `sse` |
| `url` | string | HTTP/SSE | Server endpoint URL |
| `command` | string | Stdio | Executable to run |
| `args` | array | Stdio | Command arguments |
| `env` | object | No | Environment variables |
| `headers` | object | HTTP | HTTP headers |
| `cwd` | string | Stdio | Working directory |

### Environment Variable Expansion

```json
{
  "mcpServers": {
    "database": {
      "type": "stdio",
      "command": "${CLAUDE_PLUGIN_ROOT}/server",
      "env": {
        "DB_URL": "${DB_URL:-postgresql://localhost/test}",
        "API_KEY": "${API_KEY}"
      }
    }
  }
}
```

**Syntax:**
- `${VAR}` - Use environment variable
- `${VAR:-default}` - Use VAR or default if not set
- `${CLAUDE_PLUGIN_ROOT}` - Plugin installation directory

## 6.7 Popular MCP Servers สำหรับ QA

### GitHub (Code & PR Management)
```bash
claude mcp add --transport http github https://api.githubcopilot.com/mcp/
```
**Use Cases:**
- Review pull requests
- Create/manage issues
- Check CI status
- Analyze code changes

### Sentry (Error Monitoring)
> **⚠️ ตรวจสอบ URL ล่าสุดที่:** https://docs.sentry.io หรือ https://modelcontextprotocol.io

```bash
# ตัวอย่าง - URL อาจเปลี่ยนแปลง ตรวจสอบเอกสาร Sentry ก่อนใช้งาน
claude mcp add --transport http sentry \
  --header "Authorization: Bearer ${SENTRY_TOKEN}" \
  https://mcp.sentry.dev/mcp
```
**Use Cases:**
- Monitor error patterns
- Analyze production issues
- Track error trends
- Debug test failures

### Database (Test Data Management)
```bash
claude mcp add --transport stdio test-db \
  -- npx -y @bytebase/dbhub \
  --dsn "postgresql://qa_user:pass@localhost:5432/test_db"
```
**Use Cases:**
- Query test data
- Verify data integrity
- Generate test datasets
- Validate database state

### Slack (Notifications)
> **⚠️ ตรวจสอบ URL ล่าสุดที่:** https://api.slack.com หรือ https://modelcontextprotocol.io

```bash
# ตัวอย่าง - URL อาจเปลี่ยนแปลง ตรวจสอบเอกสาร Slack ก่อนใช้งาน
claude mcp add --transport http slack \
  --header "Authorization: Bearer ${SLACK_BOT_TOKEN}" \
  https://mcp.slack.com/mcp
```
**Use Cases:**
- Send test notifications
- Report failures to team
- Log test results
- Alert on critical issues

## 6.8 MCP Management Commands

```bash
# List all configured servers
claude mcp list

# Get server details
claude mcp get github

# Remove a server
claude mcp remove github

# Check server status (in Claude Code)
/mcp

# Reset project approval choices
claude mcp reset-project-choices

# Import from Claude Desktop
claude mcp add-from-claude-desktop
```

## 6.9 Authentication

### OAuth 2.0 Flow
```bash
# Add server
claude mcp add --transport http sentry https://mcp.sentry.dev/mcp

# Authenticate (in Claude Code)
/mcp
# Follow browser prompts for OAuth
```

### Bearer Token
```bash
claude mcp add --transport http api \
  --header "Authorization: Bearer your-token" \
  https://api.example.com/mcp
```

### API Key
```bash
claude mcp add --transport http api \
  --header "X-API-Key: your-api-key" \
  https://api.example.com/mcp
```

## 6.10 Creating Custom MCP Server

### Basic Stdio Server (Node.js)

```javascript
// my-mcp-server.js
const { Server } = require("@modelcontextprotocol/sdk/server/stdio");
const { StdioServerTransport } = require("@modelcontextprotocol/sdk/server/stdio");

const server = new Server({
  name: "test-runner",
  version: "1.0.0",
  tools: [
    {
      name: "run_tests",
      description: "Run test suite",
      inputSchema: {
        type: "object",
        properties: {
          suite: { type: "string", description: "Test suite name" },
          browser: { type: "string", enum: ["chrome", "firefox"] }
        },
        required: ["suite"]
      }
    },
    {
      name: "get_coverage",
      description: "Get test coverage report",
      inputSchema: { type: "object", properties: {} }
    }
  ]
});

server.setRequestHandler("callTool", async (request) => {
  const { name, arguments: args } = request;

  if (name === "run_tests") {
    // Execute tests
    return { content: [{ type: "text", text: `Running ${args.suite}...` }] };
  }

  if (name === "get_coverage") {
    return { content: [{ type: "text", text: "Coverage: 85%" }] };
  }
});

const transport = new StdioServerTransport();
server.connect(transport);
```

### Register Custom Server
```bash
claude mcp add --transport stdio test-runner \
  -- node /path/to/my-mcp-server.js
```

## 6.11 Environment Variables

```bash
# MCP Timeouts
export MCP_TIMEOUT=10000                    # Startup timeout (ms)
export MCP_TOOL_TIMEOUT=5000               # Tool execution timeout (ms)

# MCP Output
export MAX_MCP_OUTPUT_TOKENS=50000          # Max response tokens (default: 25000)

# Tool Search
export ENABLE_TOOL_SEARCH=auto              # Enable at 10% context
export ENABLE_TOOL_SEARCH=auto:5            # Enable at 5% context
export ENABLE_TOOL_SEARCH=true              # Always enable
```

## 6.12 Troubleshooting MCP

| Issue | Cause | Solution |
|-------|-------|----------|
| "Connection closed" on Windows | Missing `cmd /c` | Use `cmd /c npx server` |
| Server not starting | Path or timeout | Check `MCP_TIMEOUT` |
| Tools not appearing | Server not loaded | Check `claude mcp list` |
| Permission denied | Script not executable | `chmod +x script.sh` |
| Auth fails | Wrong token | Use `/mcp` for OAuth |
| Timeout errors | Slow server | Increase `MCP_TIMEOUT` |

### ✅ Best Practices: MCP

| ✅ ทำ | ❌ ไม่ทำ |
|------|----------|
| เริ่มจาก Playwright MCP ก่อน (มีประโยชน์สุด) | ติดตั้ง MCP หลายตัวพร้อมกัน |
| ตั้ง scope เป็น `project` สำหรับ MCP เฉพาะโปรเจค | ตั้งทุกอย่างเป็น `user` (อาจชนกัน) |
| ใช้ `claude mcp list` ตรวจสอบสถานะ | ติดตั้งแล้วไม่ตรวจว่าทำงานไหม |
| เก็บ environment variables ใน `.env` | ใส่ token/key ตรงๆ ใน config |
| ใช้ OAuth (`/mcp`) สำหรับ MCP ที่รองรับ | Hardcode credentials ใน settings |
| อ่าน error log เมื่อ MCP ไม่ทำงาน | ลบแล้วติดตั้งใหม่โดยไม่ดู log |

---

# 7. Skills (คำสั่งลัดที่สร้างเอง)
> 🟡 **ระดับ: เริ่มใช้แล้ว** | อ่าน: 10 นาที

## 7.1 Skills คืออะไร?

### ปัญหา: พิมพ์ซ้ำๆ ทุกวัน

```
วันจันทร์: > รัน npm test แล้วบอกว่า test ไหน fail พร้อม error message
วันอังคาร: > รัน npm test แล้วบอกว่า test ไหน fail พร้อม error message
วันพุธ: > รัน npm test แล้วบอกว่า test ไหน fail พร้อม error message
(เหนื่อย!)
```

### วิธีแก้: สร้าง Skill

**Skill = คำสั่งลัดที่คุณสร้างเอง**

```
สร้าง skill "/run-tests" ครั้งเดียว
    ↓
ครั้งหน้าพิมพ์แค่: /run-tests
    ↓
Claude ทำทุกอย่างตามที่กำหนดไว้!
```

---

## 7.2 วิธีง่ายที่สุด: ให้ Claude สร้าง Skill ให้! (แนะนำสำหรับมือใหม่)

> **🚀 ไม่ต้องใช้ command line เลย!** แค่บอก Claude ว่าอยากได้ Skill อะไร

### ตัวอย่างที่ 1: สร้าง /run-tests

```bash
# พิมพ์ใน Claude Code:
> ช่วยสร้าง Skill ชื่อ /run-tests ให้หน่อย
> ให้รัน robot tests/ แล้วสรุปผล:
> - จำนวน test ทั้งหมด
> - ผ่าน/ไม่ผ่าน
> - ถ้า fail ให้บอกสาเหตุและวิธีแก้

# Claude จะ:
# 1. สร้างโฟลเดอร์ .claude/skills/run-tests/
# 2. สร้างไฟล์ SKILL.md พร้อมเนื้อหา
# 3. บอกว่าสร้างเสร็จแล้ว พร้อมใช้งาน!
```

### ตัวอย่างที่ 2: สร้าง /review-pr

```bash
> ช่วยสร้าง Skill ชื่อ /review-pr ให้หน่อย
> ให้ review code ที่เปลี่ยนแปลง ดูเรื่อง:
> - Security issues
> - Test coverage
> - Code style
> สรุปเป็น checklist
```

### ตัวอย่างที่ 3: สร้าง /analyze-fail

```bash
> ช่วยสร้าง Skill ชื่อ /analyze-fail ให้หน่อย
> ให้วิเคราะห์ test ที่ fail:
> - หาสาเหตุ
> - เสนอวิธีแก้
> - แก้ไขให้เลยถ้าทำได้
```

### 📋 Template: สร้าง Skill ด้วย Claude

```bash
# Copy ไปใช้ได้เลย แค่เปลี่ยน [ส่วนที่ต้องกรอก]

> ช่วยสร้าง Skill ชื่อ /[ชื่อ-skill] ให้หน่อย
> ให้ทำ: [อธิบายว่าต้องทำอะไร]
> Output ที่ต้องการ: [อธิบาย format ที่อยากได้]
```

> **💡 Tip:** ถ้าอยากแก้ไข Skill ก็บอก Claude ได้เลย: "แก้ไข Skill /run-tests ให้เพิ่มการแสดง test coverage ด้วย"

---

## 7.3 สร้าง Skill ด้วยตัวเอง (สำหรับคนอยากเรียนรู้)

> **หมายเหตุ:** ถ้าใช้วิธีให้ Claude สร้างให้ (Section 7.2) ข้ามส่วนนี้ได้เลย!

### ขั้นตอนที่ 1: สร้างโฟลเดอร์

```bash
# สร้างโฟลเดอร์สำหรับ skill
mkdir -p .claude/skills/run-tests
```

### ขั้นตอนที่ 2: สร้างไฟล์ SKILL.md

```bash
# สร้างไฟล์
touch .claude/skills/run-tests/SKILL.md
```

### ขั้นตอนที่ 3: เขียนเนื้อหา Skill

```markdown
---
name: run-tests
description: รัน test แล้วสรุปผล
---

# คำสั่ง

1. รัน: `npm test`
2. สรุปผล:
   - จำนวน test ทั้งหมด
   - ผ่าน / ไม่ผ่าน
3. ถ้ามี test fail:
   - บอกชื่อ test
   - บอก error message
   - แนะนำวิธีแก้
```

### ขั้นตอนที่ 4: ใช้งาน!

```bash
claude
> /run-tests
```

**แค่นี้เอง!**

---

## 7.4 โครงสร้าง Skill

### ไฟล์ SKILL.md

```markdown
---
name: ชื่อ-skill          # ต้องมี (ใช้ - แทนช่องว่าง)
description: อธิบายว่าทำอะไร  # ต้องมี
---

# คำสั่งสำหรับ Claude
เขียนสิ่งที่ต้องการให้ Claude ทำ...
```

### ที่เก็บ Skill

| ที่เก็บ | ใช้ได้กับ | เมื่อไหร่ใช้ |
|--------|----------|-------------|
| `.claude/skills/` | โปรเจคนี้ | skill สำหรับทีม |
| `~/.claude/skills/` | ทุกโปรเจค | skill ส่วนตัว |

---

## 7.5 Skill สำคัญสำหรับ QA (พร้อมใช้!)

### Skill 1: รัน Test และสรุปผล

**สร้างที่:** `.claude/skills/run-tests/SKILL.md`

```markdown
---
name: run-tests
description: รัน test แล้วสรุปผลให้เข้าใจง่าย
---

# ขั้นตอน

1. รัน `npm test`
2. สรุปผล:
   - ✅ จำนวน test ที่ผ่าน
   - ❌ จำนวน test ที่ fail
   - ⏭️ จำนวน test ที่ skip
3. สำหรับ test ที่ fail:
   - ชื่อ test
   - Error message
   - ไฟล์และบรรทัด
   - แนะนำวิธีแก้
```

**ใช้งาน:** `/run-tests`

---

### Skill 2: Review โค้ดก่อน Commit

**สร้างที่:** `.claude/skills/review/SKILL.md`

```markdown
---
name: review
description: Review โค้ดที่แก้ไขก่อน commit
---

# ขั้นตอน

1. ดูไฟล์ที่แก้ไข (git diff)
2. ตรวจสอบ:
   - **Bug**: มี bug ที่เห็นชัดไหม?
   - **Security**: มีช่องโหว่ไหม?
   - **Test**: มี test ครอบคลุมไหม?
3. รายงาน:
   - 🔴 Critical: ต้องแก้ก่อน commit
   - 🟡 Warning: ควรแก้
   - 💡 Suggestion: ถ้ามีเวลา
```

**ใช้งาน:** `/review`

---

### Skill 3: วิเคราะห์ Test ที่ Fail

**สร้างที่:** `.claude/skills/analyze-fail/SKILL.md`

```markdown
---
name: analyze-fail
description: วิเคราะห์สาเหตุที่ test fail
argument-hint: "[ชื่อ test หรือ error message]"
---

# วิเคราะห์ Test ที่ Fail

Test/Error: $ARGUMENTS

# ขั้นตอน

1. หาไฟล์ test ที่เกี่ยวข้อง
2. อ่าน test code
3. วิเคราะห์สาเหตุ:
   - Logic ผิด?
   - Data ผิด?
   - Async/timing issue?
   - Environment issue?
4. แนะนำวิธีแก้พร้อมตัวอย่าง
```

**ใช้งาน:** `/analyze-fail "should validate email format"`

---

### Skill 4: หา Test Coverage Gap

**สร้างที่:** `.claude/skills/coverage-gap/SKILL.md`

```markdown
---
name: coverage-gap
description: หา code ที่ยังไม่มี test ครอบคลุม
argument-hint: "[ไฟล์หรือโฟลเดอร์]"
---

# หา Coverage Gap

เป้าหมาย: $ARGUMENTS

# ขั้นตอน

1. อ่าน source code ใน $ARGUMENTS
2. อ่าน test files ที่เกี่ยวข้อง
3. วิเคราะห์:
   - Function ไหนยังไม่มี test?
   - Edge case ไหนที่ขาด?
   - Error case ที่ยังไม่ได้ test?
4. แนะนำ test ที่ควรเพิ่ม (เรียงตามความสำคัญ)
```

**ใช้งาน:** `/coverage-gap src/auth/`

---

## 7.6 Tips การใช้ Skill

### รับ Argument

```bash
# Skill ที่รับ argument
/analyze-fail "login test"
/coverage-gap src/api/
```

**ใน SKILL.md ใช้ `$ARGUMENTS`:**
```markdown
วิเคราะห์: $ARGUMENTS
```

### ดู Skills ที่มี

```bash
/help   # ดู skills ทั้งหมด
```

### Claude เรียก Skill เองอัตโนมัติ

```bash
> ช่วย review โค้ดที่แก้ไขก่อน commit
# Claude อาจเรียก /review ให้อัตโนมัติ (ถ้ามี)
```

### ✅ Best Practices: Skills

| ✅ ทำ | ❌ ไม่ทำ |
|------|----------|
| ให้ Claude สร้าง Skill ให้ (ง่ายสุด) | เขียน SKILL.md เองตั้งแต่แรก |
| ตั้งชื่อ Skill ให้สื่อความหมาย (`/run-tests`) | ตั้งชื่อย่อที่จำยาก (`/rt`) |
| ทดสอบ Skill ก่อนแชร์กับทีม | แชร์โดยไม่เคยลองรัน |
| สร้าง Skill สำหรับงานที่ทำบ่อย (> 3 ครั้ง/สัปดาห์) | สร้าง Skill สำหรับงานที่ทำแค่ครั้งเดียว |
| ใส่ `$ARGUMENTS` ให้ Skill รับ input ได้ | Hardcode ค่าตายตัวในทุก Skill |
| Commit `.claude/skills/` เข้า git | เก็บ Skill ไว้ local คนเดียว |

---

# 8. Subagents (ผู้ช่วยที่มีความจำแยก)
> 🔵 **ระดับ: ใช้คล่องแล้ว** | อ่าน: 10 นาที

## 8.1 Subagent คืออะไร?

### ปัญหา: ค้นหาไฟล์แล้ว Claude จำเต็ม

```
คุณ: "ค้นหาไฟล์ทั้งหมดที่เกี่ยวกับ authentication"
Claude: [อ่านไฟล์ 50 ไฟล์...]
Claude: [ความจำเต็ม 80%!]
คุณ: (ยังไม่ได้ทำงานจริงเลย...)
```

### วิธีแก้: ใช้ Subagent

**Subagent = ผู้ช่วยที่มีความจำแยกต่างหาก**

```
คุณ: "ใช้ subagent ค้นหาไฟล์ authentication"
    ↓
Subagent: [ค้นหา, อ่านไฟล์ 50 ไฟล์...]
    ↓
Subagent: "พบ 5 ไฟล์ที่เกี่ยวข้อง: [รายชื่อ]"  ← ส่งแค่สรุปกลับมา!
    ↓
Claude หลัก: ความจำยังว่าง 95%!
```

### เปรียบเทียบ

| ไม่ใช้ Subagent | ใช้ Subagent |
|----------------|--------------|
| Claude อ่านเอง 50 ไฟล์ | Subagent อ่าน 50 ไฟล์ |
| ความจำเต็ม 80% | ความจำใช้แค่ 5% (แค่สรุป) |
| ทำงานต่อยาก | พร้อมทำงานต่อ |

---

## 8.2 วิธีใช้ Subagent (ง่ายมาก!)

### แค่บอก Claude ว่า "ใช้ subagent"

```bash
# ภาษาไทย
> ใช้ subagent ค้นหาไฟล์ test ทั้งหมด
> ให้ subagent วิเคราะห์ว่ามี test coverage กี่ %
> ใช้ agent สำรวจโค้ดใน src/auth/

# English
> Use a subagent to find all files related to authentication
> Have an agent analyze the test coverage
```

**Claude จะเลือก agent ที่เหมาะสมให้อัตโนมัติ!**

---

## 8.3 Agents ที่มีให้ใช้

| Agent | ใช้เมื่อ | ตัวอย่าง |
|-------|---------|---------|
| **Explore** | ค้นหา, สำรวจโค้ด (เร็ว) | "ใช้ subagent หาไฟล์ที่ import X" |
| **general-purpose** | ทำงานทั่วไป | "ใช้ subagent วิเคราะห์ error" |

> **💡 Tip:** ไม่ต้องระบุชื่อ agent ก็ได้ Claude จะเลือกให้เอง

---

## 8.4 ใช้ Subagent เมื่อไหร่? (สำคัญสำหรับ QA!)

### ✅ ใช้ Subagent เมื่อ:

| สถานการณ์ | ตัวอย่างคำสั่ง |
|-----------|---------------|
| **ค้นหาไฟล์เยอะๆ** | "ใช้ subagent หาไฟล์ test ทั้งหมดในโปรเจค" |
| **วิเคราะห์โค้ดหลายไฟล์** | "ใช้ subagent วิเคราะห์ว่า function X ถูกใช้ที่ไหนบ้าง" |
| **สำรวจ codebase ใหม่** | "ใช้ subagent อธิบายโครงสร้างโปรเจคนี้" |
| **หา pattern ในโค้ด** | "ใช้ subagent หาทุกที่ที่มี SQL query" |

### ❌ ไม่ต้องใช้ Subagent เมื่อ:

| สถานการณ์ | ทำปกติได้เลย |
|-----------|-------------|
| ดูไฟล์เดียว | > "อธิบาย @src/auth/login.ts" |
| แก้ไขโค้ด | > "แก้ bug ใน @src/auth/login.ts" |
| รัน test | > "รัน npm test" |

---

## 8.5 ตัวอย่างการใช้งานจริงสำหรับ QA

### ตัวอย่าง 1: หา Test Coverage Gap

```bash
> ใช้ subagent วิเคราะห์:
> 1. ไฟล์ใน src/ ที่ยังไม่มี test
> 2. Function สำคัญที่ยังไม่ถูก test
> สรุปเป็นรายการ
```

**ผลลัพธ์ที่ได้:** รายการไฟล์/function ที่ต้องเขียน test เพิ่ม

---

### ตัวอย่าง 2: หา Code ที่อาจมี Bug

```bash
> ใช้ subagent ค้นหาในโค้ดทั้งหมด:
> - ที่ที่ไม่มี try-catch รอบ async
> - ที่ที่ไม่ validate input
> บอกไฟล์และบรรทัด
```

**ผลลัพธ์ที่ได้:** รายการจุดที่อาจมีปัญหา

---

### ตัวอย่าง 3: วิเคราะห์ Test ที่ Fail บ่อย

```bash
> ใช้ subagent อ่านไฟล์ test ทั้งหมดใน tests/
> หา test ที่:
> - มี retry logic
> - มี waitFor หรือ sleep
> - มี comment ว่า "flaky"
> น่าจะเป็น flaky tests
```

**ผลลัพธ์ที่ได้:** รายการ test ที่น่าจะไม่เสถียร

---

### ตัวอย่าง 4: เตรียมข้อมูลก่อน Review

```bash
> ใช้ subagent สำรวจ PR นี้:
> 1. ไฟล์ที่แก้ไข
> 2. function ที่เปลี่ยน
> 3. test ที่เกี่ยวข้อง
> สรุปให้ก่อนผมจะ review
```

**ผลลัพธ์ที่ได้:** สรุปภาพรวมของ PR

---

## 8.6 Subagent vs Skill (ต่างกันยังไง?)

| | Subagent | Skill |
|-|----------|-------|
| **ความจำ** | แยกต่างหาก | ใช้ร่วมกับ Claude หลัก |
| **ผลลัพธ์** | ได้แค่สรุป | ได้ทุกรายละเอียด |
| **เหมาะกับ** | งานค้นหา, สำรวจเยอะๆ | งานที่ทำบ่อย, คำสั่งลัด |
| **วิธีใช้** | พิมพ์บอก | `/skill-name` |

### ใช้ด้วยกันได้!

```bash
# สร้าง Skill ที่ใช้ Subagent
# ไฟล์: .claude/skills/find-gaps/SKILL.md

---
name: find-gaps
description: หา test coverage gaps
context: fork          # ← ทำให้รันใน subagent
---

ค้นหาทุกไฟล์ใน src/ ที่ยังไม่มี test
สรุปเป็นรายการเรียงตามความสำคัญ
```

**ใช้งาน:** `/find-gaps`

### ✅ Best Practices: Subagents

| ✅ ทำ | ❌ ไม่ทำ |
|------|----------|
| ใช้ Subagent สำหรับงาน "อ่านเยอะ แต่ตอบสั้น" | ใช้ Subagent สำหรับงานง่ายๆ 2-3 บรรทัด |
| ระบุขอบเขตงานชัดเจนให้ Subagent | สั่งกว้างๆ "ช่วยดูโปรเจคหน่อย" |
| ใช้ Subagent วิเคราะห์ไฟล์ใหญ่ (ประหยัด main context) | อ่านไฟล์ใหญ่ใน main session |
| รวม Subagent + Skill สำหรับงาน recurring | สร้าง Subagent ซ้ำๆ ทุกครั้งด้วยมือ |
| ใช้ Subagent เปรียบเทียบหลายไฟล์พร้อมกัน | เปิดหลาย session แยกกันเอง |

---

# 9. Hooks และ Plugins (หัวข้อขั้นสูง)
> 🔴 **ระดับ: Pro** | อ่าน: 5 นาที
>
> **📌 สำหรับมือใหม่:** ข้ามทั้ง section นี้ได้เลย! ถ้าสนใจทีหลัง แค่ถาม Claude ว่า "ช่วยตั้ง hook ให้ format code อัตโนมัติ" Claude จะสร้างให้เอง

## 9.1 Hooks คืออะไร? (สรุปสั้นๆ)

**Hooks** = script ที่รันอัตโนมัติเมื่อเกิด event ใน Claude Code

| ใช้เมื่อ | ตัวอย่าง |
|---------|---------|
| อยากให้ format code อัตโนมัติหลังแก้ไข | รัน prettier หลัง edit |
| อยากบล็อกคำสั่งอันตราย | บล็อก rm -rf |
| อยากรัน lint อัตโนมัติ | รัน eslint หลังแก้ไข |

### ตัวอย่างง่ายๆ: Format อัตโนมัติหลังแก้ไข

**ไฟล์:** `.claude/settings.json`

```json
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "prettier --write"
          }
        ]
      }
    ]
  }
}
```

### จัดการ Hooks

```bash
/hooks    # เปิด UI จัดการ hooks
```

---

## 9.2 Plugins คืออะไร? (สรุปสั้นๆ)

**Plugins** = แพ็กเกจที่รวม skills, agents, hooks ไว้ด้วยกัน สำหรับแชร์ให้ทีมใช้

| ใช้เมื่อ | ตัวอย่าง |
|---------|---------|
| อยากใช้ skills/agents ที่คนอื่นสร้างไว้ | ติดตั้ง qa-toolkit plugin |
| อยากแชร์ skills ให้ทีม | สร้าง plugin แจกทีม |

### ติดตั้ง Plugin

```bash
/plugin                # เปิด plugin manager
claude plugin install plugin-name@marketplace
```

### ใช้งาน Skill จาก Plugin

```bash
/plugin-name:skill-name
```

> **💡 สำหรับ QA:** ส่วนใหญ่ไม่จำเป็นต้องสร้าง plugin เอง ใช้ Skills และ Agents แยกก็เพียงพอ

---

# 10. IDE Integrations
> 🟡 **ระดับ: เริ่มใช้แล้ว** | อ่าน: 5 นาที

## 10.1 VS Code Integration

### Installation
```bash
# Via VS Code Extensions
# Cmd+Shift+X → Search "Claude Code" → Install

# Or via command line
code --install-extension anthropic.claude-code
```

### Features
- Side-by-side diff viewer
- Inline file references with `@`
- Multiple conversations (tabs/windows)
- Permission management UI
- Session resume from CLI

### Keyboard Shortcuts

> **หมายเหตุ:** Shortcuts อาจเปลี่ยนตามเวอร์ชัน extension
> กด `?` ใน Claude Code panel เพื่อดู shortcuts ที่ใช้ได้จริง

| Action | Mac | Windows/Linux |
|--------|-----|---------------|
| เปิด Claude Panel | ดูใน Command Palette | ดูใน Command Palette |
| Insert @-mention | `Cmd+Shift+A` | `Ctrl+Shift+A` |
| Focus toggle | `Cmd+Esc` | `Ctrl+Esc` |

**วิธีหา Shortcuts ที่ถูกต้อง:**
1. เปิด VS Code
2. กด `Cmd+Shift+P` (Mac) หรือ `Ctrl+Shift+P` (Windows)
3. พิมพ์ "Claude" จะเห็นคำสั่งทั้งหมด

### VS Code Settings

```json
// settings.json
{
  "claudeCode.selectedModel": "default",
  "claudeCode.useTerminal": false,
  "claudeCode.initialPermissionMode": "default",
  "claudeCode.preferredLocation": "panel",
  "claudeCode.autosave": true,
  "claudeCode.respectGitIgnore": true
}
```

### Usage Tips
- Use side panel for main session, tabs for side tasks
- Select code → Claude sees selection automatically
- Type `@file#5-10` for specific line ranges
- Drag conversations to different panels
- Paste images for visual analysis

## 10.2 JetBrains IDEs

### Supported IDEs
- IntelliJ IDEA
- PyCharm
- WebStorm
- PhpStorm
- GoLand
- Android Studio

### Installation
```
Settings → Plugins → Marketplace → Search "Claude Code" → Install
Restart IDE
```

### Keyboard Shortcuts

| Action | Mac | Windows/Linux |
|--------|-----|---------------|
| Open Claude | Cmd+Esc | Ctrl+Esc |
| Insert @-mention | Cmd+Option+K | Ctrl+Alt+K |

### Plugin Settings
```
Settings → Tools → Claude Code [Beta]
- Claude command: "claude"
- Enable Option+Enter for multi-line: true
- Enable automatic updates: true
```

### ESC Key Fix
If ESC doesn't work:
1. Settings → Tools → Terminal
2. Uncheck "Move focus to the editor with Escape"
3. Restart terminal

---

# 11. Context Management (Real-World Guide)
> 🔵 **ระดับ: ใช้คล่องแล้ว** | อ่าน: 15 นาที

> **📌 สำหรับมือใหม่:** สิ่งที่ต้องรู้จริงๆ มีแค่ 3 อย่าง:
> 1. พิมพ์ `/context` เพื่อดูว่าใช้ memory ไปเท่าไหร่
> 2. พิมพ์ `/compact` เมื่อ memory เกิน 50%
> 3. พิมพ์ `/clear` เมื่อเปลี่ยนไปทำงานอื่น
>
> **แค่ 3 คำสั่งนี้ก็ใช้ได้แล้ว!** รายละเอียดด้านล่างสำหรับคนที่อยากเข้าใจลึกขึ้น

## 11.1 ทำความเข้าใจ Context Window

### Token คืออะไร?

**Token** = หน่วยย่อยของข้อความ (ประมาณ 4 ตัวอักษร = 1 token)

```
"Hello, World!" ≈ 4 tokens
"function validateEmail(email)" ≈ 6 tokens
ไฟล์ 100 บรรทัด ≈ 500-1000 tokens
```

### Context Window Limits

| Model | Input Tokens | Output Tokens | ค่าใช้จ่าย |
|-------|-------------|---------------|-----------|
| **Opus 4.5** | 200K (~150K words) | 32K | สูงสุด |
| **Sonnet 4.5** | 200K | 32K | กลาง |
| **Haiku** | 200K | 8K | ต่ำสุด |

### อะไรกินพื้นที่ Context?

```
┌─────────────────────────────────────────────────────────────┐
│                    CONTEXT WINDOW (200K)                    │
├─────────────────────────────────────────────────────────────┤
│ System Prompt           │ ~5,000 tokens (fixed)             │
│ CLAUDE.md               │ 100-2,000 tokens                  │
│ Skills (loaded)         │ 500-2,000 tokens per skill        │
│ MCP Server Tools        │ 200-2,000 tokens per server       │
│ Conversation History    │ GROWS with each message           │
│ File Contents (Read)    │ 500-10,000 tokens per file        │
│ Command Outputs (Bash)  │ 100-50,000 tokens per command     │
│ Extended Thinking       │ Up to 32,000 tokens               │
└─────────────────────────────────────────────────────────────┘
```

## 11.2 Context Degradation (ปัญหาเมื่อ Context เต็ม)

### อาการที่พบ:

| ระดับ Context | อาการ |
|--------------|-------|
| **0-50%** | ทำงานปกติ, จำได้ทุกอย่าง |
| **50-80%** | เริ่มลืมรายละเอียดเก่า |
| **80-95%** | ลืม instructions, ทำผิดบ่อย |
| **95%+** | Auto-compaction, อาจเสีย context สำคัญ |

### Real-World ปัญหาที่พบบ่อย:

```
❌ Claude ลืม coding style ที่บอกไว้ตอนแรก
❌ Claude ทำซ้ำสิ่งที่ทำไปแล้ว
❌ Claude ลืมว่าต้องรัน tests หลังแก้โค้ด
❌ Claude ตอบไม่ตรงคำถาม (เพราะลืม context)
❌ Claude ใช้เวลานานขึ้นในการตอบ
```

## 11.3 Commands สำหรับจัดการ Context

### ดู Context ปัจจุบัน

```bash
/context
```

**Output:**
```
Context Usage: [████████████░░░░░░░░] 62% (124,000 / 200,000 tokens)

Breakdown:
├── System: 5,000 (2.5%)
├── CLAUDE.md: 800 (0.4%)
├── Skills: 3,200 (1.6%)
├── MCP Tools: 2,000 (1.0%)
├── Conversation: 98,000 (49.0%)
└── File Contents: 15,000 (7.5%)
```

### ดูค่าใช้จ่าย

```bash
/cost
```

**Output:**
```
Session Cost: $0.45
├── Input Tokens: 124,000 ($0.30)
└── Output Tokens: 35,000 ($0.15)

Total Turns: 12
Average per Turn: $0.0375
```

### Compact (บีบอัด Context)

```bash
# Basic compact
/compact

# Compact พร้อมระบุ focus
/compact Focus on test coverage changes and database migrations

# Compact สำหรับ QA work
/compact Keep test failures, error messages, and recent code changes
```

**การทำงานของ /compact:**
- สรุป conversation history
- เก็บ: recent messages, code changes, decisions
- ลบ: verbose outputs, old debugging attempts, redundant context

### Clear (เริ่มใหม่)

```bash
# Full reset - เริ่ม session ใหม่เลย
/clear
```

**⚠️ ข้อควรระวัง:** `/clear` จะลบทุกอย่าง รวมถึง context ที่มีประโยชน์

---

> **🟢 มือใหม่:** อ่านแค่ 11.1-11.3 ก็พอ! (3 คำสั่ง: `/context`, `/compact`, `/clear`)
> Section 11.4 เป็นต้นไปสำหรับคนที่ใช้ Claude Code คล่องแล้ว ข้ามไปยัง [Section 12](#12-best-practice-workflow-ตัวอย่างครบจบ) ได้เลย

---

## 11.4 Auto-Compaction (ระบบอัตโนมัติ)

### เมื่อไหร่จะทำงาน?

```
Context reaches 95% → Auto-compaction triggers
```

### อะไรถูกเก็บไว้?

| เก็บ (Preserved) | ลบ (Removed) |
|-----------------|--------------|
| Recent 5-10 messages | Old debugging attempts |
| Code changes made | Verbose command outputs |
| Key decisions | Repetitive explanations |
| Error messages | Exploratory file reads |
| Test results summary | Full file contents (summarized) |

### ปัญหาของ Auto-Compaction

```
⚠️ อาจลบ context สำคัญโดยไม่ตั้งใจ
⚠️ Claude อาจลืมว่าทำอะไรไปบ้าง
⚠️ อาจต้องอธิบายซ้ำ
```

**Best Practice:** `/compact` เองก่อนถึง 90%

## 11.5 Strategies แบบ Real-World

### Strategy 1: Task Separation (สำคัญมาก!)

```bash
# ❌ BAD: ทำหลาย task ใน session เดียว
> Fix authentication bug
> ... 20 messages later ...
> Now add new feature X
> ... 30 messages later ...
> Now review the API

# ✅ GOOD: แยก session ตาม task
# Session 1: Fix auth bug
/rename auth-bug-fix
> Fix the authentication timeout issue
> [Complete and commit]

# Session 2: New feature
/clear
/rename feature-x
> Add new feature X

# Session 3: API review
/clear
/rename api-review
> Review the API for security
```

### Strategy 2: Use Subagents for Heavy Work

```bash
# ❌ BAD: อ่านไฟล์เยอะๆ ใน main context
> Read all files in src/
> Now analyze them
# Context: 80% full just from file reads!

# ✅ GOOD: ใช้ subagent
> Use a subagent to explore src/ and find authentication-related files
# Returns: Summary only, main context stays clean
```

**Real-World Example:**

```bash
# QA: วิเคราะห์ test coverage
# ❌ BAD
> Read all test files
> Read all source files
> Compare coverage
# = Context explodes

# ✅ GOOD
> Use the explore agent to analyze test coverage.
> Return only: files without tests, and coverage gaps.
# = Summary returns, context stays manageable
```

### Strategy 3: Specific Prompts (ลด Token)

```bash
# ❌ BAD: Vague prompt = Claude reads everything
> Help me understand the codebase

# ✅ GOOD: Specific = reads only what's needed
> Explain how user authentication works.
> Focus on src/auth/login.ts and the JWT flow.
```

### Strategy 4: Reference Files with @

```bash
# ❌ BAD: Let Claude search everything
> Find where errors are handled

# ✅ GOOD: Point directly
> Look at @src/utils/errorHandler.ts and explain the error flow
```

### Strategy 5: Batch Related Questions

```bash
# ❌ BAD: Many small queries
> What does function A do?
> What does function B do?
> What does function C do?
# = 3 rounds of file reading

# ✅ GOOD: One comprehensive query
> Explain functions A, B, and C in src/utils.ts.
> How do they work together?
# = 1 file read, all answers
```

### Strategy 6: Use Haiku for Exploration

```bash
# ❌ BAD: ใช้ Opus ค้นหาไฟล์
> Find all test files  # Expensive!

# ✅ GOOD: ใช้ Haiku แล้วค่อยมา Opus
/model haiku
> List all test files
# Switch back for complex work
/model sonnet
> Now fix the authentication test
```

### Strategy 7: Regular Compaction Points

```bash
# สร้าง checkpoint หลังจบ task ย่อย
> Fix bug in login flow
> [Claude fixes]
> Tests pass!

/compact Keep the login fix approach and test results

> Now fix the similar bug in registration
# Fresh context with relevant summary
```

## 11.6 Token Cost โดยประมาณ

### File Reading Costs

| File Size | Token Cost | Example |
|-----------|------------|---------|
| Small (<50 lines) | ~200 tokens | config.json |
| Medium (50-200 lines) | ~800 tokens | component.tsx |
| Large (200-500 lines) | ~2,000 tokens | service.ts |
| Very Large (500+ lines) | ~5,000+ tokens | big-module.ts |

### Command Output Costs

| Command | Typical Output | Token Cost |
|---------|---------------|------------|
| `ls -la` | Short | ~100 tokens |
| `npm test` (pass) | Medium | ~500 tokens |
| `npm test` (failures) | Long | ~2,000+ tokens |
| `git diff` (small) | Medium | ~500 tokens |
| `git diff` (big PR) | Very Long | ~10,000+ tokens |

### Feature Costs

| Feature | Fixed Cost | Notes |
|---------|-----------|-------|
| System Prompt | ~5,000 | Every message |
| CLAUDE.md | 100-2,000 | Loaded at start |
| Each Skill loaded | 500-2,000 | On demand |
| Each MCP Server | 200-2,000 | Tool definitions |
| Extended Thinking | Up to 32,000 | When enabled |

## 11.7 Real-World QA Scenarios

### Scenario 1: Test Suite Analysis

```bash
# ❌ Context Explosion
> Read all 50 test files and analyze coverage
# = ~100,000 tokens just for files!

# ✅ Smart Approach
> Use a subagent to:
> 1. Find all test files
> 2. Check which source files have matching tests
> 3. Return ONLY: list of files without tests
# = ~2,000 tokens for summary
```

### Scenario 2: Debugging Flaky Test

```bash
# ❌ Full History
> Run the test 10 times and show all output
# = 10 x 2,000 = 20,000 tokens of output

# ✅ Smart Approach
> Run the test 5 times.
> Report ONLY: pass/fail status and any error messages.
> Don't show full output unless it fails.
# = ~1,000 tokens total
```

### Scenario 3: Long QA Session

```bash
# Session: 2 hours of QA work

# ⏱️ Hour 1: Test bug fixes
/rename test-fixes
> Fix flaky test in auth.spec.ts
> ... work ...
/compact Keep auth test fix approach

# ⏱️ Hour 1.5: Context check
/context
# If > 70%, compact or clear

# ⏱️ Hour 2: Different area
/clear  # Start fresh for unrelated work
/rename api-testing
> Review API integration tests
```

### Scenario 4: CI/CD Pipeline

```bash
#!/bin/bash
# ci-claude-check.sh

# Use minimal context for CI
claude -p "Run npm test and report ONLY failures" \
  --model haiku \
  --max-turns 3 \
  --output-format json \
  --no-session-persistence \
  | jq '.result'

# No session = no context buildup
```

## 11.8 Environment Variables สำหรับ Context

```bash
# Limit thinking tokens (saves context)
export MAX_THINKING_TOKENS=10000  # Default: 32000

# Increase MCP output limit
export MAX_MCP_OUTPUT_TOKENS=50000  # Default: 25000

# Enable tool search (ประหยัด context สำหรับ MCP tools)
export ENABLE_TOOL_SEARCH=auto  # At 10% context
```

## 11.9 Quick Decision Guide

```
┌─────────────────────────────────────────────────────────────┐
│                  CONTEXT MANAGEMENT FLOWCHART               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Starting new task?                                         │
│      │                                                      │
│      ├── Related to current work? → Continue session        │
│      │                                                      │
│      └── Unrelated? → /clear and start fresh               │
│                                                             │
│  Context > 60%?                                             │
│      │                                                      │
│      ├── Important context? → /compact with focus           │
│      │                                                      │
│      └── Can restart? → /clear                             │
│                                                             │
│  Need to explore codebase?                                  │
│      │                                                      │
│      ├── Large exploration? → Use subagent                  │
│      │                                                      │
│      └── Small lookup? → Direct query                       │
│                                                             │
│  Running commands with big output?                          │
│      │                                                      │
│      ├── Need full output? → Accept cost                    │
│      │                                                      │
│      └── Need summary? → "Report only errors/summary"       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 11.10 Summary: Golden Rules

| Rule | Why |
|------|-----|
| **1. Check /context regularly** | Know when to compact |
| **2. /clear between unrelated tasks** | Fresh start, no pollution |
| **3. Use subagents for exploration** | Isolate verbose work |
| **4. Be specific in prompts** | Avoid unnecessary reads |
| **5. /compact before 90%** | Control what's preserved |
| **6. Use Haiku for simple tasks** | Cheaper, faster |
| **7. Name sessions** | Easy to resume later |
| **8. Batch related questions** | Fewer round trips |

---

# 12. Best Practice Workflow: ตัวอย่างครบจบ
> 🟡 **ระดับ: เริ่มใช้แล้ว** | อ่าน: 10 นาที

> **สำคัญมาก!** Section นี้แสดงวิธีใช้ Claude Code แบบมืออาชีพ ตั้งแต่เริ่มจนจบ

## 12.1 The Complete Workflow (7 ขั้นตอน)

```
┌─────────────────────────────────────────────────────────────────┐
│                  Claude Code Best Practice Flow                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   1. Setup     →  2. Context   →  3. Plan    →  4. Execute      │
│   (ครั้งเดียว)     (ทุกครั้ง)       (งานใหญ่)     (ทำจริง)         │
│                                                                  │
│                  5. Validate   →  6. Iterate  →  7. Cleanup     │
│                  (ตรวจสอบ)        (แก้ไข)        (จัดการ)         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 12.2 ตัวอย่างจริง: แก้ Bug และเขียน Test

**สถานการณ์:** มี bug ที่ user login แล้ว session หมดอายุเร็วเกินไป

### Step 1: Setup (ทำครั้งเดียวต่อ project)

```bash
# สร้าง CLAUDE.md สำหรับ project
cat > CLAUDE.md << 'EOF'
# Project: E-Commerce API

## Tech Stack
- Framework: NestJS
- Test: Jest + Supertest
- DB: PostgreSQL

## Commands
- Run tests: `npm test`
- Run specific: `npm test -- --grep "auth"`
- Lint: `npm run lint`

## Rules
- ทุก PR ต้องมี test
- ต้องรัน lint ก่อน commit
- ห้ามแก้ไฟล์ใน src/core/ โดยไม่ถาม

## File Structure
- src/auth/ = authentication logic
- src/session/ = session management
- tests/ = all test files
EOF
```

### Step 2: Context (ให้ข้อมูลที่จำเป็น)

```bash
# เริ่ม Claude Code
claude

# ให้ context แบบ layered
> ฉันเจอ bug: user login แล้ว session หมดอายุใน 5 นาที
> แต่ควรจะอยู่ได้ 24 ชั่วโมง
>
> Error จาก production log:
> """
> [ERROR] Session expired for user_id=12345
> session_created_at: 2024-01-15T10:00:00Z
> session_expired_at: 2024-01-15T10:05:00Z
> expected_expiry: 2024-01-16T10:00:00Z
> """
>
> ไฟล์ที่น่าจะเกี่ยวข้อง:
> - @src/auth/session.service.ts
> - @src/config/session.config.ts
```

### Step 3: Plan (วางแผนก่อนทำ)

```bash
# ใช้ /plan mode สำหรับงานซับซ้อน
/plan

> วิเคราะห์ปัญหา session หมดอายุเร็ว
> และวางแผนการแก้ไข พร้อมเขียน test

# Claude จะ:
# 1. อ่านไฟล์ที่เกี่ยวข้อง
# 2. หาสาเหตุ
# 3. เสนอแผนการแก้ไข

# ตัวอย่าง output:
# ============================================
# Analysis:
# - พบว่า session.config.ts ตั้ง TTL เป็น 300 (5 นาที)
# - แต่ comment บอกว่าควรเป็น 86400 (24 ชม.)
# - ดูเหมือน typo จาก commit ล่าสุด
#
# Plan:
# 1. แก้ค่า TTL ใน session.config.ts
# 2. เขียน unit test ตรวจสอบค่า TTL
# 3. เขียน integration test สำหรับ session expiry
# 4. รัน test suite ทั้งหมด
# ============================================

# Review แผน แล้วอนุมัติ
> แผนนี้ OK ทำได้เลย

# กด Shift+Tab เพื่อเปลี่ยน permission mode หรือพิมพ์คำสั่งถัดไปเพื่อเริ่ม execute
```

### Step 4: Execute (ให้ Claude ทำงาน)

```bash
> ทำตามแผนที่วางไว้เลย
> แก้ไข session TTL และเขียน test

# Claude จะ:
# 1. แก้ไฟล์ session.config.ts
# 2. สร้าง test file ใหม่
# 3. ขออนุญาตก่อนแก้ไฟล์ (ถ้าตั้ง permission ไว้)
```

### Step 5: Validate (ตรวจสอบผลลัพธ์)

```bash
# ให้ Claude รัน tests
> รัน test ที่เขียนใหม่ และ test อื่นที่เกี่ยวข้องกับ session

# ตัวอย่าง output:
# ============================================
# Running: npm test -- --grep "session"
#
# PASS tests/session/session.config.test.ts
#   ✓ TTL should be 86400 seconds (24 hours)
#   ✓ TTL should not be less than 1 hour
#
# PASS tests/session/session.service.test.ts
#   ✓ session should expire after TTL
#   ✓ session should be renewable
#
# Test Suites: 2 passed, 2 total
# Tests:       4 passed, 4 total
# ============================================

# ตรวจสอบด้วยตัวเอง
> แสดง diff ของไฟล์ที่แก้ไขทั้งหมด
```

### Step 6: Iterate (แก้ไขถ้าจำเป็น)

```bash
# ถ้า test fail หรือต้องการแก้ไขเพิ่ม
> test case "session should be renewable" fail
> ช่วยดูและแก้ไขให้หน่อย

# Claude จะ:
# 1. วิเคราะห์ error
# 2. แก้ไข code หรือ test
# 3. รัน test อีกครั้ง

# ถ้าต้องการเพิ่ม test
> เพิ่ม edge case test สำหรับ:
> - session ที่ถูก revoke
> - session ที่ user logout
```

### Step 7: Cleanup (จัดการหลังเสร็จงาน)

```bash
# ตรวจสอบ context usage
/context
# Output: Context Usage: 45% (90,000 / 200,000 tokens)

# ถ้าจะทำงานต่อ - compact ก่อน
/compact
> เก็บเฉพาะ: การแก้ไข session TTL และ test ที่เขียน

# ตั้งชื่อ session ให้จำง่าย
/rename session-ttl-bugfix

# สร้าง commit (ถ้าพร้อม)
> สร้าง commit สำหรับการแก้ไขนี้
> commit message ควรอธิบายว่าแก้อะไร ทำไม

# ตัวอย่าง commit message ที่ดี:
# fix(session): correct TTL from 5min to 24hrs
#
# - Fixed typo in session.config.ts (300 -> 86400)
# - Added unit tests for TTL validation
# - Added integration tests for session expiry
#
# Fixes #123
```

---

## 12.3 Quick Reference: Best Practice Checklist

```
□ Step 1: CLAUDE.md มีข้อมูลครบ?
  - Tech stack
  - Commands (test, lint, build)
  - Rules และ constraints
  - File structure

□ Step 2: ให้ Context ครบ?
  - อธิบายปัญหา/งานชัดเจน
  - แปะ error message (ถ้ามี)
  - ระบุไฟล์ที่เกี่ยวข้อง (@file)
  - บอก expected outcome

□ Step 3: ใช้ /plan สำหรับงานซับซ้อน?
  - Review แผนก่อน execute
  - ถามถ้าไม่แน่ใจ

□ Step 4: ตรวจสอบก่อน approve?
  - อ่าน code ที่ Claude เขียน
  - เข้าใจ changes ทั้งหมด

□ Step 5: รัน Validation?
  - Tests pass
  - Lint pass
  - Manual test (ถ้าจำเป็น)

□ Step 6: จัดการ Context?
  - /compact ถ้าเกิน 50%
  - /clear ถ้าเปลี่ยนงาน

□ Step 7: บันทึกงาน?
  - /rename session ให้มีความหมาย
  - Commit ด้วย message ที่ดี
```

---

## 12.4 Pro Tips

### Tip 1: ใช้ Subagent สำหรับ Research

```bash
# แทนที่จะให้ main context อ่านไฟล์เยอะๆ
> ใช้ subagent ค้นหาไฟล์ทั้งหมดที่เกี่ยวกับ session
> แล้วสรุปมาให้ว่ามี logic อะไรบ้าง

# Main context ยังคงว่าง พร้อมทำงานต่อ
```

### Tip 2: แยก Session ตามงาน

```bash
# Session 1: Bug fix
claude
/rename bugfix-session-ttl

# Session 2: New feature (terminal ใหม่)
claude
/rename feature-oauth2
```

### Tip 3: ใช้ PRP สำหรับงานซ้ำๆ

```bash
# สร้าง PRP template
# แล้วใช้ซ้ำทุกครั้งที่ทำงานประเภทเดียวกัน
> อ่าน @PRPs/api-test-template.md
> แล้วทำตาม โดยเปลี่ยน endpoint เป็น /api/orders
```

### Tip 4: Checkpoint ก่อนเปลี่ยนแปลงใหญ่

```bash
# Claude Code สร้าง checkpoint อัตโนมัติ
# แต่ควร commit งานที่ดีก่อนทำต่อ

> commit งานปัจจุบันก่อน
> แล้วค่อยเริ่ม refactor
```

---

# 13. Best Practices สำหรับ QA Automation
> 🟡 **ระดับ: เริ่มใช้แล้ว** | อ่าน: 10 นาที

## 13.1 Project Setup

### Directory Structure
```
project/
├── .claude/
│   ├── CLAUDE.md              # Project memory
│   ├── CLAUDE.local.md        # Personal (gitignored)
│   ├── settings.json          # Permissions
│   ├── settings.local.json    # Personal (gitignored)
│   ├── rules/
│   │   ├── testing.md
│   │   └── security.md
│   ├── skills/
│   │   ├── run-tests/
│   │   └── code-review/
│   ├── agents/
│   │   ├── test-validator.md
│   │   └── test-fixer.md
│   └── hooks/
│       └── validate-command.sh
├── .mcp.json                  # MCP servers
├── CLAUDE.md                  # Alt location
└── .gitignore
```

### .gitignore
```gitignore
# Claude Code
.claude/CLAUDE.local.md
.claude/settings.local.json
.claude/settings.local.*.json
.claude/.mcp.local.json
.claude/sessions/
.claude/projects/
.claude/tasks/
```

## 13.2 Effective Prompting

### Good Prompts
```
> Fix the checkout flow for users with expired cards.
> Check src/payments/ for the issue, especially token refresh.
> Write a failing test that reproduces the issue, then fix it.
> Run tests to verify.
```

### Bad Prompts
```
> Fix the payment bug
```

### Prompt Patterns

**Provide Success Criteria:**
```
> Add validateEmail function.
> Test cases:
> - 'user@example.com' → true
> - 'invalid' → false
> Run tests after.
```

**Reference Specific Files:**
```
> Look at src/auth/ and explain the session flow
```

**Point to Patterns:**
```
> Follow the same pattern as ConfirmModal.tsx
```

**Separate Research from Implementation:**
```
/plan
> Read src/auth/ and create a plan for OAuth2

[Review plan]

# Press Shift+Tab to cycle permission mode, or just type next instruction
> Now implement the plan
```

## 13.3 Using @ Mentions

```
@file.ts                    # Full file
@src/components/            # Directory listing
@file.ts#5-10              # Specific lines
@file1.ts @file2.ts        # Multiple files
```

## 13.4 Session Management

```bash
# Name sessions meaningfully
/rename auth-refactor
/rename test-coverage-fix

# Use parallel sessions for parallel work
cd ../project-feature-a && claude
cd ../project-bugfix && claude

# Fork sessions for experimentation
claude --resume "main" --fork-session
```

## 13.5 Workflow Patterns

### Daily Test Health Check
```
> Use agents to:
> 1. test-validator: Review recent changes
> 2. test-fixer: Fix obvious issues
> 3. perf-analyzer: Check regressions
> Provide summary for standup.
```

### Bug Reproduction Flow
```
> I'm seeing this error: [paste error]
> Help me reproduce locally
> Write a failing test
> Fix the code
> Run full test suite
> Create PR
```

### Pre-Release Validation
```
> Use these agents in sequence:
> 1. test-validator: Full suite review
> 2. security-tester: Security coverage
> 3. perf-analyzer: Performance check
> Provide go/no-go recommendation.
```

---

# 14. Security Best Practices
> 🟡 **ระดับ: เริ่มใช้แล้ว** | อ่าน: 5 นาที

## 14.1 Permission System

- Read-only by default
- Explicit approval for edits, commands, API calls
- Checkpoints for reverting changes

## 14.2 Protecting Sensitive Files

```json
{
  "permissions": {
    "deny": [
      "Read(./.env)",
      "Read(./.env.*)",
      "Read(./secrets/**)",
      "Read(./.aws/**)",
      "Bash(curl:*)",
      "Bash(wget:*)"
    ]
  }
}
```

## 14.3 Sandboxing

```bash
/sandbox  # Enable OS-level isolation
```

Benefits:
- Filesystem isolation
- Network control
- No manual permission prompts

## 14.4 Security Practices

1. **Review suggested commands** before approval
2. **Avoid piping untrusted content**
3. **Use plan mode** for review
4. **Use VMs/containers** for untrusted scripts
5. **Keep secrets out** of conversations
6. **Configure restrictive permissions**
7. **Report suspicious behavior** with `/bug`

## 14.5 MCP Server Security

- Use only trusted MCP servers
- Anthropic doesn't audit third-party servers
- Review server permissions
- Use environment variables for credentials

---

# 15. Keyboard Shortcuts Reference
> 🟢 **ระดับ: มือใหม่** | อ่าน: 3 นาที

> **⚠️ คำเตือนสำคัญ:**
> - Keyboard shortcuts อาจแตกต่างกันตาม Terminal, IDE, และเวอร์ชันของ Claude Code
> - กด `?` ใน Claude Code เพื่อดู shortcuts ที่ใช้ได้จริงในระบบของคุณ
> - สำหรับ macOS: ถ้า Option key ไม่ทำงาน ดูวิธีตั้งค่าในบท 1.4

## 15.1 General Controls

| Shortcut | Action |
|----------|--------|
| `Ctrl+C` | Cancel/interrupt |
| `Ctrl+D` | Exit session |
| `Ctrl+L` | Clear screen |
| `Ctrl+O` | Toggle verbose |
| `Ctrl+R` | Search history |
| `Ctrl+V` / `Cmd+V` | Paste image |
| `Ctrl+G` | Open in editor |
| `Ctrl+B` | Background task |
| `Esc` | Stop mid-response |
| `Esc` + `Esc` | Rewind |
| `Shift+Tab` | Cycle permission modes |
| `Option+P` / `Alt+P` | Switch model |
| `Option+T` / `Alt+T` | Toggle thinking |

## 15.2 Text Editing

| Shortcut | Action |
|----------|--------|
| `Ctrl+K` | Delete to end of line |
| `Ctrl+U` | Delete entire line |
| `Ctrl+Y` | Paste deleted |
| `Alt+B` | Move back word |
| `Alt+F` | Move forward word |

## 15.3 Multiline Input

| Method | Shortcut |
|--------|----------|
| Escape | `\ + Enter` |
| macOS | `Option+Enter` |
| iTerm2/WezTerm | `Shift+Enter` |
| Control sequence | `Ctrl+J` |

## 15.4 Vim Mode

Enable: `/vim`

| Command | Action |
|---------|--------|
| `i`, `I`, `a`, `A`, `o`, `O` | Insert mode |
| `h/j/k/l` | Move cursor |
| `w/e/b` | Word navigation |
| `0/$` | Line start/end |
| `dd`/`D` | Delete line/to end |
| `yy` | Copy line |
| `p`/`P` | Paste |

---

# 16. Troubleshooting Guide
> 🟢 **ระดับ: มือใหม่** | อ่าน: 5 นาที (ดูเมื่อเจอปัญหา)

## 16.0 Error Resolution Flowchart (ดูอันนี้ก่อน!)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    🔧 Claude Code Error Resolution                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  เจอ Error!                                                                 │
│      │                                                                      │
│      ▼                                                                      │
│  ┌─────────────────┐                                                        │
│  │ Error ประเภทไหน? │                                                        │
│  └────────┬────────┘                                                        │
│           │                                                                 │
│     ┌─────┼─────┬─────────┬──────────┐                                      │
│     ▼     ▼     ▼         ▼          ▼                                      │
│  ┌─────┐ ┌───┐ ┌────┐ ┌──────┐ ┌─────────┐                                  │
│  │ไม่เจอ│ │Auth│ │MCP │ │Context│ │Response│                                  │
│  │claude│ │fail│ │fail│ │ full │ │ ช้า/ผิด │                                  │
│  └──┬──┘ └─┬─┘ └─┬──┘ └──┬───┘ └────┬────┘                                  │
│     │      │     │       │          │                                       │
│     ▼      ▼     ▼       ▼          ▼                                       │
│  Restart  claude claude  /compact   /model                                  │
│  Terminal auth   mcp     หรือ       เปลี่ยน                                   │
│  + PATH   login  list    /clear     model                                   │
│                                                                             │
│     │      │     │       │          │                                       │
│     └──────┴─────┴───────┴──────────┘                                       │
│                    │                                                        │
│                    ▼                                                        │
│            ยังไม่หาย?                                                        │
│                    │                                                        │
│          ┌────────┴────────┐                                                │
│          ▼                 ▼                                                │
│     ดู Section 16      ถาม Claude:                                          │
│     ด้านล่าง            "ติด error X                                        │
│                         ช่วยแก้หน่อย"                                       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Quick Fix Table

| Error | สาเหตุที่พบบ่อย | แก้ไขเร็ว |
|-------|----------------|----------|
| `command not found: claude` | ไม่ได้ติดตั้ง หรือ PATH ไม่ถูก | Restart Terminal, reinstall |
| `Authentication failed` | API key หมดอายุ/ผิด | `claude auth login` |
| `MCP server not starting` | Config ผิด, server ไม่ทำงาน | `claude mcp list`, check logs |
| `Context limit exceeded` | ใช้ memory เต็ม | `/compact` หรือ `/clear` |
| `Response too slow` | Model ใหญ่เกินไป, network | `/model haiku`, check internet |
| `Permission denied` | ไม่มีสิทธิ์รัน command | อนุญาตใน prompt หรือ settings |
| `Rate limit exceeded` | เรียก API ถี่เกินไป | รอ 1-2 นาทีแล้วลองใหม่ |
| `Invalid response` | Prompt ไม่ชัด, context สับสน | `/clear` แล้วเริ่มใหม่ |

### ยังแก้ไม่ได้?

```bash
# 1. ตรวจสอบระบบ
claude doctor

# 2. ดู logs (ถ้ามี)
cat ~/.claude/logs/latest.log

# 3. ถาม Claude ตรงๆ
> ติด error "[paste error message]" ช่วยแก้หน่อย

# 4. เริ่มใหม่ทั้งหมด
/clear
claude --version
```

---

## 16.1 Common Issues (อ่านง่าย - พร้อม error messages จริง)

### ปัญหาที่ 1: "command not found: claude"

**สิ่งที่เห็นใน Terminal:**
```
$ claude
zsh: command not found: claude
```

**สาเหตุ:** ยังไม่ได้ติดตั้ง หรือ Terminal ไม่เจอโปรแกรม

**วิธีแก้ (ทำทีละขั้น):**
```bash
# ขั้นที่ 1: ลอง restart Terminal ก่อน (ปิดแล้วเปิดใหม่)
# แล้วลองพิมพ์อีกครั้ง:
claude --version

# ขั้นที่ 2: ถ้ายังไม่ได้ ติดตั้งใหม่:
# macOS:
brew install --cask claude-code
# Linux/WSL:
curl -fsSL https://claude.ai/install.sh | bash

# ขั้นที่ 3: ตรวจสอบว่าติดตั้งอยู่ที่ไหน:
which claude
# ถ้าไม่แสดงอะไร = ยังไม่ได้ติดตั้ง
```

---

### ปัญหาที่ 2: "Authentication failed" / Login ไม่ได้

**สิ่งที่เห็น:**
```
Error: Authentication failed. Please check your credentials.
```

**วิธีแก้:**
```bash
# ขั้นที่ 1: Login ใหม่
claude auth login
# จะเปิดหน้าเว็บให้ login → login แล้วกลับมา Terminal

# ขั้นที่ 2: ถ้าใช้ API Key ตรวจสอบว่ามีค่า:
echo $ANTHROPIC_API_KEY
# ถ้าไม่แสดงอะไร = ยังไม่ได้ตั้ง API Key

# ขั้นที่ 3: ตั้ง API Key (ถ้ามี):
export ANTHROPIC_API_KEY=sk-ant-xxxxx
```

---

### ปัญหาที่ 3: MCP Server ไม่ทำงาน

**สิ่งที่เห็น:**
```
Error: MCP server 'playwright' failed to start
Connection closed
```

**วิธีแก้:**
```bash
# ขั้นที่ 1: ดูว่า MCP ตั้งค่าไว้ถูกไหม
claude mcp list
claude mcp get playwright

# ขั้นที่ 2: ลบแล้วเพิ่มใหม่
claude mcp remove playwright
claude mcp add playwright -- npx @anthropic/mcp-server-playwright

# ขั้นที่ 3: เพิ่ม timeout (ถ้า server ช้า)
export MCP_TIMEOUT=15000
```

---

### ปัญหาที่ 4: Skills ไม่โหลด / ใช้ /skill-name แล้วไม่ทำงาน

**สิ่งที่เห็น:**
```
Unknown command: /run-tests
```

**วิธีแก้:**
```bash
# ขั้นที่ 1: ตรวจสอบว่าไฟล์อยู่ถูกที่
ls .claude/skills/
# ต้องเห็นโฟลเดอร์ชื่อ skill ที่สร้าง

# ขั้นที่ 2: ตรวจสอบว่ามี SKILL.md
ls .claude/skills/run-tests/SKILL.md
# ถ้าไม่มี = ยังไม่ได้สร้าง

# ขั้นที่ 3: ให้ Claude สร้างให้
> ช่วยสร้าง Skill /run-tests ที่รัน npm test แล้วสรุปผล
```

### Context Issues
```bash
# Check context usage
/context

# Compact manually
/compact

# Clear completely
/clear
```

### Permission Errors
```bash
# Check settings
cat .claude/settings.json

# Reset permissions
/permissions
```

## 16.2 Diagnostic Commands

```bash
/doctor    # Run diagnostics
/status    # Version and account info
/debug     # Debug mode
/bug       # Report issue
```

## 16.3 Debug Mode

```bash
# Enable debug output
claude --verbose
claude --debug "api,mcp"

# View logs
tail -f ~/.claude/logs/latest.log
```

---

# 17. QA Automation Workflows
> 🟡 **ระดับ: เริ่มใช้แล้ว** (17.1-17.6) | 🔵 **ใช้คล่อง** (17.7-17.11) | อ่าน: 20-40 นาที

> **📋 ในส่วนนี้:** (เลือกอ่านเฉพาะที่ต้องการได้)
>
> | Section | เนื้อหา | ระดับ |
> |---------|---------|-------|
> | 17.1-17.4 | Test Coverage, Bug Fix, Refactor, Integration Test | 🟡 เริ่มใช้ |
> | 17.5 | CI/CD Integration (GitHub Actions, GitLab CI) | 🟡 เริ่มใช้ |
> | 17.6 | Efficient Test Writing (5 Steps) | 🟡 เริ่มใช้ |
> | 17.7 | Auto-Loop Workflow (เขียน→รัน→แก้ อัตโนมัติ) | 🔵 ใช้คล่อง |
> | 17.8 | Smart Questioning (ให้ Claude ถามก่อนเริ่มงาน) | 🔵 ใช้คล่อง |
> | 17.9 | Robot Framework | 🔵 ใช้คล่อง |
> | 17.10 | Real-World Examples (Copy-Paste ได้เลย!) | 🟡 ทุกระดับ |
> | 17.11 | Vibe Coding (เขียนโค้ดด้วยภาษาพูด) | 🔵 ใช้คล่อง |

## 17.1 Test Coverage Analysis

```bash
claude

> Find all test files
> Analyze coverage gaps
> Use test-validator agent to review
> Generate test stubs for uncovered code
> Run tests: npm test
```

## 17.2 Bug Reproduction & Fix

```bash
> I'm seeing: [error]
> Reproduce it locally
> Write failing test
> Fix the code
> Verify with full test suite
> Create PR
```

## 17.3 Test Refactoring

```bash
> Review test files for quality issues
> Suggest refactoring to reduce duplication
> Show good patterns from existing code
> Refactor tests/user.test.js
> Ensure all tests still pass
```

## 17.4 Integration Test Development

```bash
> Read API docs at @docs/api.md
> Create integration tests for /api/checkout
> Cover: success, validation errors, timeouts
> Mock the payment service
> Test happy path and errors
```

## 17.5 CI/CD Integration

```bash
#!/bin/bash
# ci-analyze.sh

claude -p "Run tests and analyze coverage" \
  --permission-mode dontAsk \
  --allowedTools "Read,Bash(npm run test:*)" \
  --max-turns 5 \
  --max-budget-usd 1.00 \
  --output-format json \
  > coverage.json

COVERAGE=$(jq '.structured_output.coverage' coverage.json)
if (( $(echo "$COVERAGE < 80" | bc -l) )); then
  echo "Coverage below 80%"
  exit 1
fi
```

### GitHub Actions Example (Copy & Paste ได้เลย!)

สร้างไฟล์ `.github/workflows/claude-qa.yml`:

```yaml
# .github/workflows/claude-qa.yml
name: Claude QA Analysis

on:
  pull_request:
    branches: [main, develop]
  push:
    branches: [main]

env:
  ANTHROPIC_API_KEY: ${{ secrets.ANTHROPIC_API_KEY }}

jobs:
  test-analysis:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Install Claude Code
        run: |
          curl -fsSL https://claude.ai/install.sh | bash
          echo "$HOME/.claude/bin" >> $GITHUB_PATH

      - name: Install dependencies
        run: npm ci

      - name: Run tests with Claude analysis
        run: |
          # รัน test และให้ Claude วิเคราะห์
          claude -p "รัน npm test แล้ววิเคราะห์ผลลัพธ์:
            1. Test ไหน fail และทำไม
            2. Coverage เท่าไหร่
            3. แนะนำ test ที่ควรเพิ่ม" \
            --permission-mode dontAsk \
            --allowedTools "Read,Bash(npm:*)" \
            --max-turns 10 \
            --max-budget-usd 2.00 \
            --output-format json \
            > analysis.json

      - name: Check coverage threshold
        run: |
          COVERAGE=$(jq -r '.structured_output.coverage // 0' analysis.json)
          echo "Coverage: $COVERAGE%"
          if (( $(echo "$COVERAGE < 80" | bc -l) )); then
            echo "::error::Coverage is below 80%"
            exit 1
          fi

      - name: Upload analysis report
        uses: actions/upload-artifact@v4
        with:
          name: claude-analysis
          path: analysis.json

  code-review:
    runs-on: ubuntu-latest
    if: github.event_name == 'pull_request'
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Install Claude Code
        run: |
          curl -fsSL https://claude.ai/install.sh | bash
          echo "$HOME/.claude/bin" >> $GITHUB_PATH

      - name: Review PR changes
        env:
          ANTHROPIC_API_KEY: ${{ secrets.ANTHROPIC_API_KEY }}
        run: |
          # ดู diff และให้ Claude review
          git diff origin/main...HEAD > changes.diff

          claude -p "Review code changes ใน changes.diff:
            1. Security issues
            2. Performance concerns
            3. Test coverage gaps
            4. Best practice violations
            สรุปเป็น markdown" \
            --permission-mode plan \
            --allowedTools "Read" \
            --max-turns 5 \
            --output-format stream \
            > review.md

      - name: Comment on PR
        uses: actions/github-script@v7
        with:
          script: |
            const fs = require('fs');
            const review = fs.readFileSync('review.md', 'utf8');
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: `## 🤖 Claude Code Review\n\n${review}`
            });
```

### GitLab CI Example

```yaml
# .gitlab-ci.yml
stages:
  - test
  - analyze

variables:
  ANTHROPIC_API_KEY: $ANTHROPIC_API_KEY

test-with-claude:
  stage: test
  image: node:20
  before_script:
    - curl -fsSL https://claude.ai/install.sh | bash
    - export PATH="$HOME/.claude/bin:$PATH"
  script:
    - npm ci
    - claude -p "รัน npm test และสรุปผล" \
        --permission-mode dontAsk \
        --allowedTools "Read,Bash(npm:*)" \
        --max-turns 5 \
        --output-format json \
        > test-results.json
  artifacts:
    paths:
      - test-results.json
    expire_in: 1 week
```

### ⚠️ ข้อควรระวังสำหรับ CI/CD

| หัวข้อ | คำแนะนำ |
|--------|---------|
| **API Key** | เก็บใน secrets เท่านั้น ห้าม hardcode |
| **Budget** | ตั้ง `--max-budget-usd` ป้องกันค่าใช้จ่ายบาน |
| **Permissions** | ใช้ `--permission-mode plan` สำหรับ review-only, `dontAsk` + `--allowedTools` สำหรับ CI ที่ต้องรัน test |
| **Timeout** | ตั้ง timeout ใน CI job (แนะนำ 10-15 นาที) |
| **Parallel jobs** | ระวัง rate limit ถ้ารัน parallel หลาย jobs |

---

## 17.6 Efficient Test Writing Workflow (ประหยัดเวลา & มีประสิทธิภาพ)

### หลักการ: "Smart Test Development"

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Smart Test Development Flow                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   1. 📋 UNDERSTAND         ให้ Claude อ่านและสรุป code ก่อน              │
│         ↓                                                                │
│   2. 🎯 PLAN               ใช้ /plan วางแผน test cases                  │
│         ↓                                                                │
│   3. 📝 GENERATE           ให้ Claude สร้าง test จาก template            │
│         ↓                                                                │
│   4. ✅ VERIFY             รัน test และแก้ไขทันที                        │
│         ↓                                                                │
│   5. 🔄 ITERATE            เพิ่ม edge cases ตามที่ต้องการ                │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

### Step 1: UNDERSTAND - ให้ Claude เข้าใจ Code ก่อน

```bash
# ❌ BAD: สั่งเขียน test โดยไม่ให้ context
> เขียน test สำหรับ payment service

# ✅ GOOD: ให้ Claude อ่านและเข้าใจก่อน
> อ่าน @src/services/payment.service.ts
> สรุปให้หน่อยว่า:
> 1. มี functions อะไรบ้าง
> 2. แต่ละ function ทำอะไร
> 3. มี dependencies อะไร
> 4. มี edge cases อะไรที่ควร test
```

**ทำไมต้องทำ?**
- Claude เข้าใจ context → เขียน test ได้ถูกต้องตั้งแต่แรก
- ไม่ต้องแก้ไขซ้ำหลายรอบ
- ได้ test ที่ครอบคลุม edge cases

---

### Step 2: PLAN - วางแผน Test Cases ก่อนเขียน

```bash
# เข้า Plan Mode
/plan

> จากที่อ่าน payment.service.ts
> วางแผน test cases โดยครอบคลุม:
>
> 1. Happy Path (ทำงานปกติ)
>    - Create payment สำเร็จ
>    - Refund สำเร็จ
>
> 2. Validation Errors (400)
>    - Invalid amount (0, negative)
>    - Missing required fields
>
> 3. Business Logic Errors
>    - Payment already processed
>    - Insufficient balance
>
> 4. Edge Cases
>    - Concurrent payments
>    - Network timeout
>
> สรุปเป็น checklist ให้หน่อย

# Review แผน แล้วพิมพ์คำสั่งถัดไปเพื่อให้ Claude เริ่มทำงาน
```

**Output ที่ได้:**
```
📋 Test Plan for PaymentService
================================
□ Happy Path
  □ createPayment - valid input → returns payment ID
  □ refundPayment - valid payment → returns refund ID

□ Validation (400)
  □ createPayment - amount = 0 → throws ValidationError
  □ createPayment - amount < 0 → throws ValidationError
  □ createPayment - missing userId → throws ValidationError

□ Business Logic
  □ createPayment - already processed → throws DuplicateError
  □ refundPayment - insufficient balance → throws InsufficientError

□ Edge Cases
  □ createPayment - concurrent same user → only one succeeds
  □ createPayment - gateway timeout → returns pending status
```

---

### Step 3: GENERATE - สร้าง Test จาก Template

**วิธีที่ 1: ให้ Claude ดูตัวอย่าง Test ที่มีอยู่**

```bash
# ให้ Claude ดู pattern จาก test ที่มีอยู่แล้ว
> ดู @tests/services/user.service.test.ts เป็นตัวอย่าง
> แล้วเขียน test สำหรับ payment.service.ts ในสไตล์เดียวกัน
> ทำตาม test plan ที่วางไว้
```

**วิธีที่ 2: ใช้ Test Template**

```bash
> เขียน test โดยใช้ template นี้:
>
> describe('[ServiceName]', () => {
>   // Setup
>   beforeEach(() => { /* reset mocks */ });
>   afterEach(() => { /* cleanup */ });
>
>   describe('[methodName]', () => {
>     describe('Happy Path', () => {
>       it('should [expected behavior] when [condition]', async () => {
>         // Arrange
>         // Act
>         // Assert
>       });
>     });
>
>     describe('Error Cases', () => {
>       it('should throw [ErrorType] when [condition]', async () => {
>         // Arrange
>         // Act & Assert
>         await expect(action).rejects.toThrow(ErrorType);
>       });
>     });
>   });
> });
```

**วิธีที่ 3: สร้างทีละกลุ่ม (Incremental)** — เขียน Happy Path → รัน → เพิ่ม Error Cases → รัน (วนจนครบ)

> 💡 **ดูรายละเอียด Write-Run-Fix Loop เต็มรูปแบบได้ที่ Section 17.7 Auto-Loop Workflow**

---

### Step 4-5: VERIFY & ITERATE

```bash
# เมื่อ Happy Path + Error Cases ผ่านแล้ว
> ดู test coverage สำหรับ payment.service.ts
> หา lines ที่ยังไม่ถูก test
> เพิ่ม test cases ให้ครอบคลุม

# หรือ
> เพิ่ม edge cases ตามนี้:
> - concurrent payments จาก user เดียวกัน
> - payment gateway timeout
> - database connection failure
```

---

### Speed Tips: เทคนิคเร่งความเร็ว

#### Tip 1: ใช้ Subagent สำหรับ Research

```bash
# ให้ Subagent หา test patterns จาก codebase
> ใช้ subagent ค้นหา test patterns ที่ใช้ใน tests/
> สรุปมาว่า:
> - ใช้ mocking library อะไร
> - setup/teardown ทำยังไง
> - assertions ใช้ pattern ไหน

# ได้ context แล้วค่อยเขียน test ใน main session
```

#### Tip 2: Batch Generate แล้ว Review

```bash
# สร้าง tests หลาย functions พร้อมกัน
> สร้าง test stubs สำหรับทุก functions ใน payment.service.ts
> แต่ละ function ให้มี:
> - 1 happy path test
> - 1 error case test
> ใส่ TODO สำหรับ implementation

# Review แล้ว implement ทีละตัว
> implement test สำหรับ createPayment (ตัวแรก)
```

#### Tip 3: ใช้ Test Data Factories

```bash
# ให้ Claude สร้าง factory functions ก่อน
> สร้าง test factory สำหรับ Payment entity
> ที่มี function: createMockPayment(overrides)
> default values ที่ valid
> เก็บใน tests/factories/payment.factory.ts

# แล้วใช้ใน tests
> เขียน test โดยใช้ createMockPayment() จาก factory
```

#### Tip 4: Copy-Paste Friendly Prompts

```bash
# Prompt Template สำหรับ Unit Test
> เขียน unit test สำหรับ @src/[path].ts
> ใช้ pattern จาก @tests/[existing].test.ts
> ครอบคลุม: happy path, validation errors, edge cases
> รันแล้วแก้ให้ผ่าน

# Prompt Template สำหรับ Integration Test
> เขียน integration test สำหรับ API endpoint [METHOD] [PATH]
> test cases:
> - success (200/201)
> - validation error (400)
> - unauthorized (401)
> - not found (404)
> mock external services, ใช้ test database
```

#### Tip 5: ใช้ Skill สำหรับงานที่ทำบ่อย

```bash
# สร้าง Skill: /gen-test
# File: .claude/skills/gen-test/SKILL.md

# Skill: Generate Test
## Instructions
1. อ่านไฟล์ source ที่ระบุ
2. หา test file ที่มีอยู่แล้วเป็นตัวอย่าง
3. วิเคราะห์ functions และ edge cases
4. สร้าง test file ใหม่
5. รัน test แล้วแก้ให้ผ่าน

## Usage
/gen-test src/services/payment.service.ts
```

---

### Common Mistakes & Solutions

| ❌ Mistake | ⚠️ Problem | ✅ Solution |
|-----------|-----------|------------|
| เขียน test โดยไม่อ่าน code | Test ไม่ตรง logic | ให้ Claude อ่าน code ก่อน |
| สั่ง "เขียน test ทั้งหมด" | ได้ test ไม่ครอบคลุม | แบ่งเป็น phases: happy → error → edge |
| ไม่ให้ตัวอย่าง | style ไม่ consistent | ชี้ไปที่ @existing.test.ts |
| เขียนเสร็จไม่รัน | ไม่รู้ว่า test ใช้ได้ | สั่ง "รันแล้วแก้ให้ผ่าน" |
| Mock ทุกอย่าง | Test ไม่จับ real bugs | Mock แค่ external deps |

---

> 💡 **ดูตัวอย่างเต็มรูปแบบ** (Understand → Plan → Generate → Verify → Iterate) ได้ใน **Section 17.7 Auto-Loop Workflow** ด้านล่าง

---

## 17.7 Auto-Loop Workflow: เขียน → รัน → แก้ → Review อัตโนมัติ

### หลักการ: "Write-Run-Fix-Review Loop"

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     Auto-Loop Workflow                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐         │
│   │  WRITE   │───▶│   RUN    │───▶│   FIX    │───▶│  REVIEW  │         │
│   │  Code    │    │   Test   │    │  if fail │    │  if pass │         │
│   └──────────┘    └──────────┘    └────┬─────┘    └──────────┘         │
│                         ▲              │                                 │
│                         └──────────────┘                                 │
│                         (loop until pass)                                │
│                                                                          │
│   💡 สั่ง Claude ครั้งเดียว ได้ผลลัพธ์ที่ใช้งานได้จริง                     │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

### Prompt Template: Complete Auto-Loop

```bash
> สร้าง [feature/function] พร้อม test
>
> Requirements:
> - [requirement 1]
> - [requirement 2]
> - [requirement 3]
>
> Workflow:
> 1. เขียน code ใน [path/to/file.ts]
> 2. เขียน test ใน [path/to/file.test.ts]
> 3. รัน test: npm test [pattern]
> 4. ถ้า fail → วิเคราะห์และแก้ไข → รันใหม่ (loop จนกว่าจะผ่าน)
> 5. เมื่อ test ผ่านทั้งหมด → review code ตัวเองโดยดู:
>    - Security issues
>    - Performance concerns
>    - Code quality
>    - Edge cases ที่อาจขาด
> 6. แก้ไขตาม review findings (ถ้ามี)
> 7. รัน test อีกครั้งเพื่อยืนยัน
> 8. สรุปผลให้ฉัน
```

---

### ตัวอย่างจริง: สร้าง API Endpoint

```bash
> สร้าง API endpoint สำหรับ user registration พร้อม test
>
> Spec:
> - POST /api/auth/register
> - Body: { email, password, name }
> - Success: 201 + user object (ไม่มี password)
> - Errors: 400 (validation), 409 (email exists)
>
> Workflow:
> 1. เขียน controller, service, dto ใน src/auth/
> 2. เขียน integration test ใน tests/auth/register.test.ts
> 3. รัน test: npm test register
> 4. Loop แก้ไขจนผ่าน
> 5. Review:
>    - Password hashing ใช้ bcrypt ถูกต้อง?
>    - SQL injection safe?
>    - Rate limiting ควรมีไหม?
>    - Test ครอบคลุม edge cases?
> 6. แก้ไขตาม review
> 7. รัน test ยืนยัน
> 8. สรุปผล + แสดง API documentation

# Claude จะสร้างทั้งหมดและรันจนผ่าน
# รวมถึง self-review และแก้ไขปัญหาที่พบ
```

---

### ตัวอย่างจริง 3: Robot Framework Test

```bash
> สร้าง Robot test สำหรับ Login flow พร้อม Page Object
>
> Requirements:
> - URL: /login
> - Test: valid login, invalid credentials, empty fields
> - ใช้ Browser Library (Playwright)
> - Page Object pattern
>
> Workflow:
> 1. สร้าง pages/login_page.resource (locators + keywords)
> 2. สร้าง tests/web/login_web.robot (test cases)
> 3. รัน: robot tests/web/login_web.robot
> 4. Loop แก้ไขจนผ่าน
> 5. Review:
>    - Locators maintainable? (ใช้ data-testid?)
>    - Wait strategies เหมาะสม?
>    - Test independent from each other?
>    - Error messages descriptive?
> 6. แก้ไขตาม review
> 7. รัน test ยืนยัน
> 8. สรุปผล
```

---

### Advanced: Conditional Review Criteria

```bash
> สร้าง payment processing function พร้อม test
>
> Requirements: [...]
>
> Auto-Loop Workflow + Review Criteria:
>
> เมื่อ test ผ่านแล้ว ให้ review ตาม criteria นี้:
>
> 🔴 Critical (ต้องแก้):
>    - Security vulnerabilities (injection, XSS, etc.)
>    - Data loss potential
>    - Memory leaks
>
> 🟡 Important (ควรแก้):
>    - Error handling ไม่ครบ
>    - Missing input validation
>    - Performance issues ที่เห็นชัด
>
> 🟢 Nice to have (แก้ถ้ามีเวลา):
>    - Code style improvements
>    - Additional test cases
>    - Documentation
>
> แก้ 🔴 และ 🟡 ก่อนสรุป
> Report 🟢 ให้ฉันพิจารณา
```

### ✅ Best Practices: Auto-Loop Workflow

| ✅ ทำ | ❌ ไม่ทำ |
|------|----------|
| ระบุ path ไฟล์และ test command ชัดเจน | ให้ Claude เดาเอาเองว่าจะเก็บที่ไหน |
| ใส่ `--max-turns 20` ป้องกัน loop ไม่สิ้นสุด | ปล่อยให้ loop ไม่จำกัด (เปลือง token) |
| กำหนด review criteria (🔴🟡🟢) | ให้ Claude review แบบกว้างๆ |
| Review ผลลัพธ์ก่อน commit เสมอ | Trust Claude 100% แล้ว commit เลย |
| ใช้กับงาน 1 feature ต่อ 1 loop | ยัดหลาย feature ใน loop เดียว |

---

## 17.8 Smart Questioning: ให้ Claude ถามก่อนเริ่มงาน

### หลักการ: "Ask First, Code Later"

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     Smart Questioning Flow                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐         │
│   │ RECEIVE  │───▶│  ANALYZE │───▶│   ASK    │───▶│  EXECUTE │         │
│   │  Task    │    │   Gaps   │    │ Questions│    │   Task   │         │
│   └──────────┘    └──────────┘    └──────────┘    └──────────┘         │
│                                                                          │
│   "ก่อนเริ่มงาน ให้ถามคำถามที่จำเป็นก่อน"                                 │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

**ทำไมต้องให้ Claude ถามก่อน?**
- ได้ output ตรงความต้องการตั้งแต่รอบแรก
- ไม่ต้องแก้ไขหลายรอบ
- ประหยัดเวลาและ tokens
- Claude เข้าใจ context ที่คุณอาจลืมบอก

---

### Prompt Template: Force Questions First

```bash
> [บอก task ที่ต้องการ]
>
> ก่อนเริ่มทำ:
> 1. วิเคราะห์ว่ามีข้อมูลอะไรที่ยังไม่ชัดเจน
> 2. ถามคำถามที่จำเป็นทั้งหมด (ถามครั้งเดียวรวมกัน)
> 3. รอฉันตอบก่อน แล้วค่อยเริ่มทำงาน
>
> หมวดคำถามที่ควรพิจารณา:
> - Technical: framework, library, patterns ที่ใช้?
> - Business: requirements, edge cases, priorities?
> - Integration: dependencies, APIs, databases?
> - Testing: test scope, coverage expectations?
> - Output: file locations, naming conventions?
```

---

### ตัวอย่างจริง 1: สร้าง Feature ใหม่

```bash
> สร้าง notification system สำหรับ app
>
> ก่อนเริ่ม ให้ถามคำถามที่จำเป็นก่อน
> รอฉันตอบ แล้วค่อยเริ่มทำ

# Claude จะถาม:
#
# 📋 ก่อนเริ่ม ขอถามเพื่อให้เข้าใจ requirements:
#
# **Technical:**
# 1. ใช้ framework อะไร? (NestJS, Express, etc.)
# 2. Notification channels ที่ต้องการ? (email, push, in-app, SMS)
# 3. ต้องการ real-time ไหม? (WebSocket, SSE)
#
# **Business:**
# 4. มี notification types อะไรบ้าง? (order update, promotion, etc.)
# 5. User สามารถ customize preferences ได้ไหม?
# 6. ต้องเก็บ history ไหม? เก็บนานแค่ไหน?
#
# **Integration:**
# 7. Email provider? (SendGrid, AWS SES, etc.)
# 8. Push notification service? (Firebase, OneSignal)
# 9. Database ที่ใช้? (PostgreSQL, MongoDB)
#
# **Output:**
# 10. เก็บ code ไว้ที่ไหน? (src/notifications/)
# 11. ต้องการ test ด้วยไหม?
#
# กรุณาตอบคำถามเหล่านี้ก่อน แล้วผมจะเริ่มทำให้ครับ
```

---

### ตัวอย่างจริง 2: แก้ Bug

```bash
> แก้ bug: user login แล้ว session หาย
>
> ก่อนเริ่มแก้ ให้ถามคำถามที่จะช่วยหา root cause

# Claude จะถาม:
#
# 🔍 ก่อนเริ่มแก้ ขอข้อมูลเพิ่มเติม:
#
# **Symptom:**
# 1. หายทันทีหลัง login หรือหลังจากทำ action บางอย่าง?
# 2. เกิดกับ user ทุกคน หรือบาง user?
# 3. เกิดทุก browser หรือบาง browser?
#
# **Environment:**
# 4. เพิ่งเกิดขึ้นเมื่อไหร่? มี deploy ล่าสุดไหม?
# 5. Production only หรือ dev ด้วย?
#
# **Technical:**
# 6. Session เก็บที่ไหน? (cookie, localStorage, Redis)
# 7. มี load balancer ไหม? (sticky session?)
# 8. Token expiry ตั้งไว้เท่าไหร่?
#
# **Logs:**
# 9. มี error logs ที่เกี่ยวข้องไหม?
# 10. มี browser console errors ไหม?
```

---

### ตัวอย่างจริง 3: เขียน Test

```bash
> เขียน test สำหรับ payment module
>
> ก่อนเริ่ม ถามคำถามที่จำเป็น

# Claude จะถาม:
#
# 📋 ก่อนเขียน test ขอถาม:
#
# **Scope:**
# 1. Test ประเภทไหน? (unit, integration, e2e, ทั้งหมด?)
# 2. Functions/endpoints ไหนที่ต้อง test? หรือทั้ง module?
# 3. Coverage target? (80%? 90%?)
#
# **Test Setup:**
# 4. ใช้ test framework อะไร? (Jest, Mocha, Vitest)
# 5. มี test ที่มีอยู่แล้วให้ดูเป็นตัวอย่างไหม?
# 6. Database test ใช้ mock หรือ test container?
#
# **External Services:**
# 7. Payment gateway (Stripe, etc.) → mock ทั้งหมด หรือใช้ sandbox?
# 8. มี third-party services อื่นที่ต้อง mock?
#
# **Priorities:**
# 9. Scenarios ไหนสำคัญที่สุด? (critical paths?)
# 10. มี known edge cases ที่ต้องครอบคลุม?
```

---

### Shortcut: Question Categories Template

```bash
> [task description]
>
> ก่อนเริ่ม ถามคำถามในหมวดต่อไปนี้:
> - 🎯 Scope & Requirements
> - 🔧 Technical Details
> - 🔗 Dependencies & Integrations
> - ✅ Success Criteria
> - 📁 Output & Delivery
```

---

### Combining Both: Question + Auto-Loop

```bash
> สร้าง user authentication system
>
> PHASE 1 - Questions: ก่อนเริ่ม ให้ถามคำถามที่จำเป็นทั้งหมด รอฉันตอบก่อน
> PHASE 2 - Execute: ใช้ Auto-Loop Workflow จาก Section 17.7 (เขียน → รัน → แก้ → review → สรุป)

# Flow: Claude ถาม → คุณตอบ → Claude ทำงานจนเสร็จ → ได้ผลลัพธ์ที่ใช้งานได้
```

---

### Pro Tip: Save as Skill

สร้าง Skill สำหรับ workflow นี้:

```bash
# .claude/skills/smart-create/SKILL.md
```

```markdown
# Skill: Smart Create

## Description
สร้าง feature/function พร้อม test โดยถามคำถามก่อนและ auto-loop จนผ่าน

## Instructions
1. **รับ task** จากผู้ใช้
2. **วิเคราะห์และถาม** คำถามที่จำเป็น (ถามครั้งเดียว)
3. **รอคำตอบ** จากผู้ใช้
4. **Execute**:
   - เขียน code
   - เขียน test
   - รัน test
   - ถ้า fail → แก้ไข → รันใหม่ (loop)
5. **Self-Review**:
   - Security issues
   - Performance concerns
   - Edge cases
   - Code quality
6. **แก้ไข** ตาม review findings
7. **รัน test** ยืนยัน
8. **สรุปผล** ให้ผู้ใช้

## Usage
/smart-create [description]
```

**ใช้งาน:**
```bash
> /smart-create payment refund feature
```

---

### Quick Reference: Test Writing Prompts

```bash
# 📊 วิเคราะห์ก่อนเขียน
> อ่าน @[file] แล้วสรุป functions และ edge cases ที่ควร test

# 📋 วางแผน Test Cases
> วางแผน test cases สำหรับ [function] ครอบคลุม happy/error/edge

# 📝 สร้าง Test (มีตัวอย่าง)
> ดู @[existing.test.ts] แล้วเขียน test สำหรับ @[source.ts] ในสไตล์เดียวกัน

# 🔧 สร้าง Test (ไม่มีตัวอย่าง)
> เขียน Jest test สำหรับ @[source.ts] ใช้ AAA pattern (Arrange-Act-Assert)

# ✅ รันและแก้ไข
> รัน npm test [pattern] ถ้า fail ให้วิเคราะห์และแก้ไข

# 📈 เพิ่ม Coverage
> ดู coverage แล้วเพิ่ม test ให้ครอบคลุม lines ที่ขาด

# 🏭 สร้าง Test Factory
> สร้าง factory function สำหรับ mock [Entity] เก็บใน tests/factories/
```

---

## 17.9 Robot Framework: Efficient Test Writing

### Quick Setup

```bash
# CLAUDE.md สำหรับ Robot Framework Project
```

```markdown
# Robot Framework Project

## Commands
- `robot tests/` - Run all tests
- `robot --include smoke tests/` - Run smoke tests
- `robot --outputdir results tests/` - With output directory

## Conventions
- Test files: `*_test.robot` or `test_*.robot`
- Resource files: `*.resource`
- Page Objects: `pages/*.resource`
- Locators: Use data-testid first, then CSS

## Libraries
- Browser (Playwright) for Web
- RequestsLibrary for API
- DatabaseLibrary for DB
```

---

### Efficient Prompts for Robot Framework

#### API Test

```bash
> สร้าง Robot API test สำหรับ:
> - Endpoint: POST /api/users
> - Request body: { "email": "...", "password": "...", "name": "..." }
> - Expected: 201 with user object
>
> ครอบคลุม:
> - Success (201)
> - Validation error (400) - invalid email, short password
> - Conflict (409) - email exists
>
> ใช้ RequestsLibrary, เก็บใน tests/api/users_api.robot
```

#### Web Test

```bash
> สร้าง Robot Web test สำหรับ Login Page:
> - URL: /login
> - Elements: email input, password input, submit button
> - Locators: ใช้ data-testid
>
> Test cases:
> - Login success → redirect to dashboard
> - Login fail → show error message
> - Empty fields → show validation
>
> ใช้ Browser Library, สร้าง Page Object ใน pages/login_page.resource
```

#### Batch Generate

```bash
> สร้าง Robot test structure สำหรับ E-commerce:
>
> tests/
> ├── api/
> │   ├── auth_api.robot      # login, register, logout
> │   ├── products_api.robot  # CRUD products
> │   └── orders_api.robot    # create, cancel, get orders
> ├── web/
> │   ├── login_web.robot
> │   ├── product_web.robot
> │   └── checkout_web.robot
> ├── pages/
> │   ├── login_page.resource
> │   ├── product_page.resource
> │   └── checkout_page.resource
> └── resources/
>     ├── common.resource
>     └── api_keywords.resource
>
> สร้างไฟล์พร้อม test stubs และ TODO comments
> ให้ฉัน review ก่อน implement
```

---

### Time-Saving Templates

#### API Test Template

```robot
*** Settings ***
Library     RequestsLibrary
Library     Collections
Resource    ../resources/common.resource
Suite Setup    Create API Session

*** Variables ***
${ENDPOINT}    /api/[resource]

*** Test Cases ***
Test [Resource] Create Success
    [Tags]    api    smoke    positive
    [Documentation]    Verify successful creation
    ${body}=    Create Dictionary    key=value
    ${response}=    POST    ${ENDPOINT}    json=${body}
    Status Should Be    201
    # Add assertions

Test [Resource] Create Invalid Data
    [Tags]    api    negative
    [Documentation]    Verify validation error
    ${body}=    Create Dictionary    key=invalid
    ${response}=    POST    ${ENDPOINT}    json=${body}    expected_status=400
    # Add assertions

*** Keywords ***
Create API Session
    Create Session    api    ${BASE_URL}    verify=${False}
```

#### Web Test Template

```robot
*** Settings ***
Library     Browser
Resource    ../resources/common.resource
Resource    ../pages/[page]_page.resource
Suite Setup       Setup Browser
Suite Teardown    Close Browser
Test Teardown     Take Screenshot On Failure

*** Test Cases ***
Test User Can [Action]
    [Tags]    web    smoke    positive
    [Documentation]    Verify user can do something
    Go To [Page]
    [Do Action]
    [Verify Result]

Test User Cannot [Action] When [Condition]
    [Tags]    web    negative
    [Documentation]    Verify error handling
    Go To [Page]
    [Create Condition]
    [Do Action]
    [Verify Error]

*** Keywords ***
Setup Browser
    New Browser    chromium    headless=${HEADLESS}
    New Page    ${BASE_URL}

Take Screenshot On Failure
    Run Keyword If Test Failed    Take Screenshot
```

---

### Workflow Summary

```
┌─────────────────────────────────────────────────────────────────────────┐
│               Efficient Test Writing Checklist                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  □ 1. อ่าน code/requirements ก่อนเขียน test                              │
│  □ 2. ใช้ /plan วางแผน test cases                                       │
│  □ 3. ให้ตัวอย่าง test ที่มีอยู่แล้วเป็น reference                        │
│  □ 4. เขียนทีละ phase: happy → error → edge                             │
│  □ 5. รัน test หลังเขียนแต่ละ phase                                      │
│  □ 6. ใช้ subagent สำหรับ research (ไม่กิน main context)                 │
│  □ 7. สร้าง Skills สำหรับงานที่ทำบ่อย                                     │
│  □ 8. ใช้ templates และ factories                                       │
│                                                                          │
│  💡 Key: ให้ context ดี + วางแผนก่อน = ประหยัดเวลา 50%+                   │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 17.10 Real-World Examples: ตัวอย่างใช้งานจริง (Copy & Paste ได้เลย!)

> **หมายเหตุ:** ตัวอย่างด้านล่างนี้ใช้ได้จริง แค่ copy prompt ไปวางใน Claude Code แล้วปรับชื่อไฟล์/path ให้ตรงกับโปรเจคของคุณ

---

### 📋 Example 1: Daily QA Tasks (งานประจำวัน)

**สถานการณ์:** เช้ามาทำงาน ต้องตรวจสอบว่า test ทั้งหมดยังผ่านอยู่ไหม

```bash
# วิธีที่ 1: รัน test แล้วสรุป
> รัน robot tests/ แล้วสรุปผลให้หน่อย
> ถ้ามี test fail ให้บอกว่า fail ตรงไหน และสาเหตุที่เป็นไปได้

# Claude จะ:
# 1. รัน robot tests/
# 2. อ่าน output.xml
# 3. สรุปผล: จำนวน pass/fail
# 4. ถ้า fail: บอกชื่อ test, error message, และวิธีแก้ที่น่าจะเป็น
```

```bash
# วิธีที่ 2: รันเฉพาะ smoke test (เร็วกว่า)
> รัน robot --include smoke tests/ สรุปผลสั้นๆ

# ผลลัพธ์ตัวอย่าง:
# ✅ Smoke Tests: 15/15 passed (2.3s)
# พร้อม deploy!
```

```bash
# วิธีที่ 3: ตรวจสอบ coverage gap
> ใช้ subagent เปรียบเทียบ:
> - API endpoints ใน src/routes/
> - Test files ใน tests/api/
> สรุปว่ามี endpoint ไหนยังไม่มี test

# Claude จะ:
# 1. สร้าง subagent (ไม่กิน main context)
# 2. Subagent อ่าน routes ทั้งหมด
# 3. เปรียบเทียบกับ test files
# 4. ส่ง summary กลับมา
```

---

### 🐛 Example 2: Bug Analysis & Fix (วิเคราะห์และแก้ Bug)

**สถานการณ์:** พบว่า Login test fail หลังจาก deploy code ใหม่

```bash
# Step 1: วิเคราะห์ปัญหา
> ดู @tests/web/login.robot
> test "User Can Login Successfully" fail ด้วย error:
> "TimeoutError: Element not found: data-testid=login-submit"
>
> ช่วยวิเคราะห์ว่าเกิดจากอะไร และวิธีแก้

# Claude จะวิเคราะห์:
# - Locator เปลี่ยนไหม?
# - มี wait ก่อน click ไหม?
# - Element render ทันไหม?
```

```bash
# Step 2: ให้ Claude เปรียบเทียบกับ source code
> ดู @tests/web/login.robot และ @src/pages/Login.tsx
> ตรวจสอบว่า data-testid ใน test ตรงกับ component จริงไหม
> ถ้าไม่ตรงให้แก้ไข test ให้ถูกต้อง

# Claude จะ:
# 1. อ่าน test file (ดู locator ที่ใช้)
# 2. อ่าน component file (ดู data-testid จริง)
# 3. เปรียบเทียบ
# 4. แก้ไข test file ถ้าไม่ตรง
```

```bash
# Step 3: ตรวจสอบว่าแก้แล้วผ่าน
> รัน robot --include login tests/web/login.robot
> ถ้ายัง fail ให้วิเคราะห์ต่อ

# Claude จะ loop จนกว่าจะผ่าน (Auto-Loop Workflow)
```

---

### ✍️ Example 3: Write New Test Suite (เขียน Test ใหม่)

**สถานการณ์:** ต้องเขียน API test สำหรับ endpoint ใหม่

```bash
# วิธี A: บอก spec ตรงๆ
> เขียน Robot API test สำหรับ:
>
> Endpoint: POST /api/users
> Request: { "name": "string", "email": "string", "role": "admin|user" }
> Response 201: { "id": "uuid", "name": "string", "email": "string" }
> Response 400: { "error": "validation error message" }
> Response 409: { "error": "Email already exists" }
>
> ต้องการ test cases:
> 1. Success - สร้าง user ได้
> 2. Invalid email format - ต้อง error 400
> 3. Missing required field - ต้อง error 400
> 4. Duplicate email - ต้อง error 409
>
> ใช้ style เดียวกับ @tests/api/auth_api.robot

# Claude จะ:
# 1. อ่าน style จาก auth_api.robot
# 2. สร้าง test file ใหม่ตาม spec
# 3. ใช้ pattern เดียวกัน (naming, structure)
```

```bash
# วิธี B: ให้ Claude อ่าน source code แล้วเขียนเอง
> ดู @src/controllers/user.controller.ts
> เขียน Robot API test ให้ครบทุก case (happy path + error cases)
> ใช้ style เดียวกับ @tests/api/auth_api.robot
> บันทึกเป็น tests/api/user_api.robot

# Claude จะ:
# 1. อ่าน controller (เข้าใจ logic)
# 2. อ่าน style จาก existing test
# 3. สร้าง test cases ครบทุก scenario
# 4. บันทึกไฟล์
```

```bash
# วิธี C: ใช้ Plan Mode สำหรับ test ที่ซับซ้อน
> /plan
>
> วางแผนเขียน test suite สำหรับ Payment API:
> - POST /api/payments (create payment)
> - GET /api/payments/:id (get payment)
> - POST /api/payments/:id/refund (refund)
>
> ต้องมี:
> - Happy path ทุก endpoint
> - Error cases (400, 401, 404, 500)
> - Edge cases (refund มากกว่าที่จ่าย, double refund)
>
> อ่าน @src/controllers/payment.controller.ts เพื่อเข้าใจ logic ก่อน

# Claude จะ:
# 1. เข้า Plan Mode
# 2. อ่าน source code
# 3. วางแผน test cases ทั้งหมด
# 4. แสดงแผนให้ review
# 5. หลังอนุมัติ พิมพ์ "ทำตามแผนเลย" แล้ว Claude จะเขียน test
```

---

### 🔍 Example 4: Code Review (รีวิวโค้ด)

**สถานการณ์:** ต้อง review PR ก่อน merge

```bash
# Review เฉพาะไฟล์ที่เปลี่ยน
> ใช้ subagent review code ที่เปลี่ยนใน PR นี้:
>
> Changed files:
> - src/services/auth.service.ts
> - src/controllers/auth.controller.ts
> - tests/api/auth_api.robot
>
> ตรวจสอบ:
> 1. Security issues (SQL injection, XSS, etc.)
> 2. Error handling ครบไหม
> 3. Test coverage ครบไหม
>
> สรุปเป็น checklist

# ผลลัพธ์ตัวอย่าง:
# ✅ Security: ไม่พบปัญหา
# ⚠️ Error handling: auth.service.ts line 45 - ไม่มี try-catch สำหรับ DB query
# ❌ Test coverage: ไม่มี test สำหรับ case "token expired"
```

```bash
# Review test quality
> ดู @tests/web/checkout.robot
> ตรวจสอบ:
> 1. Naming convention ถูกต้องไหม
> 2. Locators maintainable ไหม (ใช้ data-testid?)
> 3. มี hardcoded values ไหม
> 4. Test independent จากกันไหม
> 5. มี documentation ครบไหม
>
> แก้ไขปัญหาที่พบ

# Claude จะ:
# 1. อ่าน test file
# 2. วิเคราะห์ตาม criteria
# 3. แสดง issues ที่พบ
# 4. แก้ไขอัตโนมัติ
```

---

### 🔧 Example 5: Refactor Existing Tests (ปรับปรุง Test)

**สถานการณ์:** Test เก่าใช้ SeleniumLibrary ต้องเปลี่ยนเป็น Browser Library (Playwright)

```bash
# Migrate ทีละไฟล์
> ดู @tests/web/login_selenium.robot
>
> Migrate จาก SeleniumLibrary เป็น Browser Library:
> - Open Browser → New Browser + New Page
> - Input Text → Fill Text
> - Click Element → Click
> - Wait Until Element Is Visible → Wait For Elements State
>
> บันทึกเป็น tests/web/login.robot
> รัน test เพื่อ verify

# Claude จะ:
# 1. อ่าน test เดิม
# 2. แปลง syntax ตาม mapping
# 3. บันทึกไฟล์ใหม่
# 4. รัน test
# 5. แก้ไขถ้า fail
```

```bash
# Refactor ให้ใช้ Page Object Pattern
> ดู @tests/web/checkout.robot
>
> Refactor ให้ใช้ Page Object Pattern:
> 1. สร้าง pages/checkout_page.resource (locators + keywords)
> 2. แก้ test ให้ใช้ keywords จาก page object
> 3. ลบ hardcoded locators ออกจาก test
>
> ตัวอย่าง page object ดู @pages/login_page.resource

# Claude จะ:
# 1. อ่าน test ปัจจุบัน (ดู locators ที่ใช้)
# 2. อ่าน example page object
# 3. สร้าง checkout_page.resource
# 4. แก้ test ให้เรียก keywords แทน
```

---

### 🎲 Example 6: Debug Flaky Tests (แก้ Test ไม่เสถียร)

**สถานการณ์:** Test บางครั้ง pass บางครั้ง fail (flaky)

```bash
# วิเคราะห์จาก CI logs
> ใช้ subagent อ่าน CI results จาก 5 runs ล่าสุด:
> @ci-results/run-101.xml
> @ci-results/run-102.xml
> @ci-results/run-103.xml
> @ci-results/run-104.xml
> @ci-results/run-105.xml
>
> หา test ที่:
> - บางครั้ง pass บางครั้ง fail
> - ใช้เวลานานผิดปกติ
>
> สรุปพร้อมสาเหตุที่น่าจะเป็น

# ผลลัพธ์ตัวอย่าง:
# 🎲 Flaky Tests Found:
#
# 1. Test Search Results Load (3/5 pass)
#    Cause: Timeout - API response ช้ากว่าปกติบางครั้ง
#    Fix: เพิ่ม Wait Until keyword หรือเพิ่ม timeout
#
# 2. Test Add To Cart (4/5 pass)
#    Cause: Race condition - click ก่อน element ready
#    Fix: เพิ่ม Wait For Elements State ก่อน Click
```

```bash
# แก้ไข flaky test
> ดู @tests/web/search.robot
>
> Test "Search Results Load" flaky เพราะ timeout
> แก้ไขโดย:
> 1. เพิ่ม explicit wait ก่อน assert
> 2. เพิ่ม retry mechanism ถ้าจำเป็น
> 3. รัน test 3 ครั้งเพื่อ verify ว่าไม่ flaky แล้ว

# Claude จะ:
# 1. อ่าน test
# 2. หาจุดที่น่าจะ flaky
# 3. เพิ่ม wait/retry
# 4. รัน test 3 ครั้ง
# 5. Report ผลลัพธ์
```

---

### 🚀 Example 7: Quick Wins (งานเร็วๆ ที่ทำบ่อย)

```bash
# สร้าง test data
> สร้าง test data สำหรับ user registration:
> - 5 valid users (หลากหลาย role)
> - 3 invalid emails
> - 2 weak passwords
> บันทึกเป็น tests/data/user_data.py

# เพิ่ม tags ให้ test
> ดู tests/api/*.robot ทั้งหมด
> เพิ่ม tags ที่ขาด:
> - [Tags] api ให้ทุก test
> - [Tags] smoke ให้ test ที่สำคัญที่สุด (happy path)
> - [Tags] regression ให้ test อื่นๆ

# สร้าง test report summary
> รัน robot tests/
> สรุปผลเป็น markdown format:
> - Pass/Fail count
> - Duration
> - Top 5 slowest tests
> - Failed tests พร้อม error message
```

---

### 💡 Pro Tips: เทคนิคที่ทำให้เร็วขึ้น

```bash
# Tip 1: ใช้ @ mention แทนการพิมพ์ path ยาวๆ
> ดู @tests/api/auth_api.robot              # ✅ สั้น ชัด
> ดู tests/api/auth_api.robot               # ❌ ก็ได้ แต่ยาว

# Tip 2: ให้ตัวอย่าง style ที่ต้องการ
> เขียน test ใหม่ ใช้ style เดียวกับ @existing_test.robot

# Tip 3: ใช้ subagent สำหรับงาน research
> ใช้ subagent หา... (ไม่กิน main context)

# Tip 4: รัน test หลังแก้ทุกครั้ง
> แก้ไข test แล้วรันเลย ถ้า fail ให้แก้ต่อจนกว่าจะผ่าน

# Tip 5: ใช้ plan mode สำหรับงานซับซ้อน
> /plan
> [อธิบายงานที่ต้องทำ]
# review แผน แล้วพิมพ์คำสั่งถัดไปเพื่อเริ่มทำ (หรือ Shift+Tab เพื่อเปลี่ยน permission)

# Tip 6: Compact เมื่อ context เกิน 50%
> /context                    # เช็ค usage
> /compact                    # บีบอัด (ถ้าเกิน 50%)
```

---

### 📊 Summary: เลือก Approach ที่เหมาะกับงาน

| งาน | Approach | ตัวอย่าง Prompt |
|-----|----------|-----------------|
| รัน test ปกติ | ถามตรงๆ | `รัน robot tests/` |
| วิเคราะห์ fail | ให้ context + ถาม | `ดู @file error คือ X ช่วยวิเคราะห์` |
| เขียน test ใหม่ | ให้ spec + ตัวอย่าง | `เขียน test ตาม spec นี้ style เหมือน @example` |
| งานซับซ้อน | Plan Mode | `/plan` แล้วอธิบาย |
| Research/ค้นหา | Subagent | `ใช้ subagent หา...` |
| งานซ้ำๆ | Skill | สร้าง Skill แล้วใช้ `/skill-name` |

---

## 17.11 Vibe Coding: เขียนโค้ดด้วยภาษาพูด (สำคัญมาก!)

### Vibe Coding คืออะไร?

**Vibe Coding** = การเขียนโค้ดโดยใช้ภาษาพูด (Natural Language) บอก AI ว่าต้องการอะไร แล้ว AI เขียนโค้ดให้

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         🎯 Vibe Coding Concept                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  แบบเดิม (Traditional):                                                     │
│  ────────────────────────                                                   │
│  คุณ → เขียนโค้ดทุกบรรทัดเอง → โค้ด                                          │
│                                                                             │
│  แบบ Vibe Coding:                                                           │
│  ────────────────                                                           │
│  คุณ → บอก AI ว่าต้องการอะไร → AI เขียนโค้ด → คุณ review → โค้ด              │
│                                                                             │
│  💡 คุณเป็น "ผู้กำกับ" ไม่ใช่ "ช่างพิมพ์"                                     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### ทำไม QA ต้องรู้ Vibe Coding?

| ประโยชน์ | ตัวอย่าง |
|---------|---------|
| **เขียน Test เร็วขึ้น 5-10 เท่า** | บอก "เขียน test login" แทนพิมพ์ 100+ บรรทัด |
| **ไม่ต้องจำ syntax** | ไม่ต้องจำว่า Playwright/Robot ใช้ keyword อะไร |
| **Focus ที่ test design** | คิดว่าจะ test อะไร ไม่ใช่ test ยังไง |
| **Debug เร็วขึ้น** | "test นี้ fail ช่วยดูหน่อย" |

### วิธี Vibe Coding กับ Claude Code

#### Step 1: บอก Context ให้ชัด

```bash
# ❌ ไม่ดี - ไม่มี context
> เขียน test

# ✅ ดี - มี context ครบ
> เขียน Robot Framework test สำหรับ login page
> - URL: https://myapp.com/login
> - ต้อง test: valid login, invalid password, empty fields
> - ใช้ Browser Library
> - locators ใช้ data-testid
```

#### Step 2: ให้ตัวอย่าง (ถ้ามี)

```bash
# ให้ตัวอย่าง test ที่มีอยู่แล้ว
> ดู @tests/login.robot เป็นตัวอย่าง
> แล้วเขียน test สำหรับ register page ใน style เดียวกัน
```

#### Step 3: Iterate จนพอใจ

```bash
# Claude เขียน test มาแล้ว → ขอแก้ไข
> เพิ่ม test case สำหรับ password ที่ไม่ตรง criteria
> แก้ locator จาก css=.submit เป็น data-testid=submit-btn
> เพิ่ม screenshot เมื่อ test fail
```

### ตัวอย่าง Vibe Coding สำหรับ QA

#### ตัวอย่าง 1: เขียน Test จาก User Story

```bash
> User Story: "As a user, I want to reset my password so that I can access my account"
>
> Acceptance Criteria:
> 1. ผู้ใช้กดลิงก์ "Forgot Password"
> 2. กรอก email แล้วกด Submit
> 3. ได้รับ email มี link reset
> 4. กด link → หน้า set new password
> 5. กรอก password ใหม่ 2 ครั้ง → success
>
> เขียน Robot Framework test ให้หน่อย
> ใช้ Browser Library, locators ใช้ data-testid
```

#### ตัวอย่าง 2: เขียน API Test จาก Swagger

```bash
> ดู API spec นี้:
> POST /api/users
> Body: { "email": string, "password": string, "name": string }
> Response 201: { "id": number, "email": string }
> Response 400: { "error": string }
>
> เขียน Robot Framework API test ครอบคลุม:
> - Happy path (201)
> - Validation errors (400): email format, password too short, missing fields
> - Duplicate email (409)
```

#### ตัวอย่าง 3: Debug Test ที่ Fail

```bash
> test นี้ fail:
> @tests/checkout.robot
>
> Error: "Element not found: data-testid=checkout-btn"
>
> ช่วยดูว่าน่าจะเป็นเพราะอะไร และแก้ยังไง
```

#### ตัวอย่าง 4: Refactor Test เก่า

```bash
> test ใน @tests/old_login.robot เขียนมานานแล้ว
> - locators ใช้ XPath ยาวๆ
> - ไม่มี Page Object
> - hardcode test data
>
> ช่วย refactor ให้:
> - ใช้ data-testid แทน XPath
> - แยก Page Object ไปไฟล์ใหม่
> - ใช้ variables file สำหรับ test data
```

### Vibe Coding + Playwright MCP (Combo ทรงพลัง!)

**Scenario:** ต้องเขียน test สำหรับหน้าใหม่ แต่ไม่รู้ locators

```bash
# Step 1: ใช้ Playwright MCP ดูหน้าจริง
> เปิด browser ไปที่ https://myapp.com/checkout
> หา element ทั้งหมดที่มี data-testid

# Claude จะ:
# - เปิด browser จริง
# - หา elements
# - ส่งรายการ locators กลับมา

# Step 2: ให้ Claude เขียน test จาก locators ที่เจอ
> จาก locators ที่เจอ เขียน Robot test สำหรับ checkout flow:
> 1. เพิ่มสินค้าลงตะกร้า
> 2. กรอก shipping info
> 3. เลือก payment method
> 4. confirm order
> 5. verify success page

# Claude เขียน test ให้ โดยใช้ locators จริงที่เจอ!
```

**อีกตัวอย่าง: Debug UI Bug**

```bash
# Step 1: ให้ Claude ดูหน้าจริง
> เปิด https://myapp.com/profile
> screenshot มาให้ดู
> ตรวจสอบว่า "Save" button visible ไหม

# Claude: "ปุ่ม Save ไม่ visible เพราะถูก element อื่นบัง"

# Step 2: ให้ Claude เขียน test ที่จะจับ bug นี้
> เขียน test ที่ verify ว่า Save button ต้อง visible และ clickable
> ให้ fail ถ้า button ถูกบังหรือ disabled
```

### Quick Reference: Vibe Coding Prompts สำหรับ QA

```bash
# ===== เขียน Test ใหม่ =====
> เขียน [Robot/Playwright/Jest] test สำหรับ [feature]
> ครอบคลุม: [test cases]
> ใช้ style เหมือน @[existing test file]

# ===== Debug Test =====
> test @[file] fail ด้วย error: [error message]
> ช่วยวิเคราะห์และแก้ไข

# ===== Refactor Test =====
> refactor @[file] ให้:
> - [improvement 1]
> - [improvement 2]

# ===== Generate Test Data =====
> สร้าง test data สำหรับ [scenario]
> format: [JSON/YAML/Robot Variables]

# ===== Review Test =====
> review @[test file] ว่า:
> - ครอบคลุม edge cases ไหม
> - locators ดีไหม
> - มี race condition ไหม

# ===== Playwright MCP Combo =====
> เปิด [URL] แล้ว [action]
> จากนั้นเขียน test ที่ verify [expected result]
```

### Tips สำหรับ Vibe Coding ที่ดี

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         💡 Vibe Coding Tips                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1. ✅ ให้ Context มากพอ                                                     │
│     - บอก framework ที่ใช้                                                   │
│     - บอก style/convention ของทีม                                           │
│     - ให้ตัวอย่างที่มีอยู่                                                    │
│                                                                             │
│  2. ✅ Iterate ทีละขั้น                                                      │
│     - ให้ Claude เขียนก่อน → review → ขอแก้ไข                                │
│     - ไม่ต้องได้ perfect ครั้งแรก                                            │
│                                                                             │
│  3. ✅ ใช้ @mention                                                          │
│     - @existing-test.robot เป็นตัวอย่าง                                      │
│     - @page-object.resource เป็น reference                                  │
│                                                                             │
│  4. ✅ รัน test จริงทุกครั้ง                                                  │
│     - Claude เขียน → คุณรัน → แจ้ง error → Claude แก้                        │
│                                                                             │
│  5. ❌ อย่าเชื่อ 100%                                                        │
│     - Review โค้ดก่อน commit เสมอ                                            │
│     - ตรวจสอบ logic ว่าถูกต้อง                                               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### ✅ Best Practices: Vibe Coding

| ✅ ทำ | ❌ ไม่ทำ |
|------|----------|
| เริ่มจากงานเล็กๆ ก่อน (1 function, 1 test) | เริ่มเลยกับงานใหญ่ทั้ง module |
| ให้ context ชัด: framework, style, path | พิมพ์สั้นๆ "เขียน test ให้" |
| ใช้ @mention ชี้ไฟล์ตัวอย่าง | อธิบายยาวๆ แทนที่จะให้ดูตัวอย่าง |
| รัน test จริงทุกครั้งหลัง Claude เขียน | เชื่อว่า Claude เขียนถูก ไม่รัน |
| ใช้ Plan Mode (`/plan`) ก่อนงานใหญ่ | สั่งเขียนเลยไม่วางแผน |
| Iterate: เขียน → review → ขอแก้ → repeat | คาดหวัง perfect ครั้งแรก |

---

# 18. Quick Reference Cards
> 🟢 **ระดับ: มือใหม่** | ใช้เป็น reference เปิดดูเมื่อต้องการ

## 18.1 Essential Commands

```bash
# Start session
claude                    # New
claude -c                 # Continue
claude -r "name"          # Resume

# Configuration
/config                   # Settings
/permissions              # Permissions
/memory                   # CLAUDE.md
/init                     # Initialize

# Context
/context                  # View usage
/compact                  # Compress
/clear                    # Reset
/cost                     # Costs

# Features
/model                    # Change model
/plan                     # Plan mode
/vim                      # Vim mode
/agents                   # Manage agents
/mcp                      # MCP servers
/plugin                   # Plugins

# Help
/help                     # Help
/doctor                   # Diagnostics
```

## 18.2 CLI Flags

```bash
# Session
--continue, -c            # Continue last
--resume, -r              # Resume by name
--fork-session            # Branch session

# Output
--print, -p               # Non-interactive
--output-format           # json, text, stream-json

# Permissions
--permission-mode         # plan, acceptEdits, etc
--allowedTools            # Pre-approve tools
--disallowedTools         # Block tools

# Model
--model                   # opus, sonnet, haiku

# Budget
--max-turns               # Limit turns
--max-budget-usd          # Cost limit
```

## 18.3 File Locations

```
~/.claude/settings.json           # User settings
~/.claude/CLAUDE.md               # User memory
~/.claude/skills/                 # User skills
~/.claude/agents/                 # User agents

.claude/settings.json             # Project settings
.claude/CLAUDE.md                 # Project memory
.claude/CLAUDE.local.md           # Personal (gitignored)
.claude/skills/                   # Project skills
.claude/agents/                   # Project agents
.claude/rules/                    # Modular rules

.mcp.json                         # MCP servers
CLAUDE.md                         # Alt memory location
```

## 18.4 Environment Variables

```bash
ANTHROPIC_API_KEY                 # API key
MAX_THINKING_TOKENS               # Thinking limit
MCP_TIMEOUT                       # MCP startup timeout
MAX_MCP_OUTPUT_TOKENS             # MCP response limit
CLAUDE_CODE_PROFILE               # Config profile
```

## 18.5 Keyboard Shortcuts

```
Ctrl+C          Cancel
Ctrl+D          Exit
Ctrl+L          Clear screen
Ctrl+R          Search history
Ctrl+B          Background task
Esc             Stop response
Esc+Esc         Rewind
Shift+Tab       Cycle permissions
Option+P/Alt+P  Switch model
Option+T/Alt+T  Toggle thinking
```

## 18.6 Glossary (คำศัพท์สำหรับมือใหม่)

| คำศัพท์ | ความหมาย | อธิบายง่ายๆ |
|---------|----------|-------------|
| **Token** | หน่วยนับข้อความ | เหมือน "คำ" ที่ Claude อ่าน/เขียน ยิ่งใช้เยอะ = ยิ่งเสียเงินเยอะ |
| **Context Window** | พื้นที่ความจำ | เหมือน "โต๊ะทำงาน" ของ Claude - มีพื้นที่จำกัด ถ้าเต็มจะลืมของเก่า |
| **Context** | บริบท/สิ่งที่จำ | ทุกอย่างที่ Claude รู้ในตอนนี้ (ไฟล์ที่อ่าน, บทสนทนา, คำสั่ง) |
| **Prompt** | คำสั่ง/คำถาม | สิ่งที่คุณพิมพ์บอก Claude |
| **CLI** | Command Line Interface | หน้าจอสีดำที่พิมพ์คำสั่ง (Terminal) |
| **Terminal** | โปรแกรมพิมพ์คำสั่ง | เหมือนกับ CLI - หน้าจอที่พิมพ์ `claude` เพื่อเริ่มใช้งาน |
| **Shell** | ตัวแปลคำสั่ง | โปรแกรมที่อ่านคำสั่งของคุณ (bash, zsh, PowerShell) |
| **MCP** | Model Context Protocol | "ปลั๊กอิน" ที่ช่วยให้ Claude เชื่อมต่อ tools ภายนอก (GitHub, DB) |
| **Subagent** | ผู้ช่วยตัวย่อย | Claude สร้างผู้ช่วยแยกเพื่อทำงานเฉพาะ ไม่กินความจำหลัก |
| **Hooks** | ตะขอ/trigger อัตโนมัติ | คำสั่งที่รันอัตโนมัติเมื่อเกิดเหตุการณ์บางอย่าง |
| **Session** | ช่วงการทำงาน | ตั้งแต่เปิด Claude จนปิด = 1 session |
| **Compact** | บีบอัด | ย่อความจำให้เล็กลง เพื่อมีที่ว่างทำงานต่อ |
| **Model** | โมเดล AI | "สมอง" ของ Claude (Haiku=เร็ว/ถูก, Sonnet=สมดุล, Opus=ฉลาด/แพง) |
| **API Key** | รหัสเข้าใช้งาน | เหมือน password สำหรับเรียกใช้ Claude ผ่าน code |
| **Locator** | ตัวระบุ element | สิ่งที่ใช้หา element บนหน้าเว็บ (data-testid, CSS selector) |
| **Page Object** | รูปแบบการเขียน test | แยก locators และ actions ออกจาก test cases |
| **PRP** | Product Requirements Prompt | เอกสาร spec สำหรับให้ AI ทำงาน (เหมือน PRD แต่สำหรับ Claude) |
| **Flag** | ตัวเลือกคำสั่ง | สิ่งที่เพิ่มหลังคำสั่ง เช่น `--model haiku` → `--model` คือ flag |
| **Diff** | ส่วนต่าง | แสดงว่าไฟล์มีอะไรเปลี่ยนแปลง (บรรทัดไหนเพิ่ม/ลบ/แก้) |

### คำศัพท์ที่ใช้แทนกันได้:
- **CLI = Terminal = Command Line** → หน้าจอสีดำที่พิมพ์คำสั่ง
- **Prompt = Query = คำถาม** → สิ่งที่คุณถาม Claude
- **Agent = Subagent = ผู้ช่วย** → Claude ที่ทำงานแยก

---

# Summary: Key Takeaways

1. **CLAUDE.md** = ความจำระยะยาวของ Claude
2. **Settings** = ควบคุม permissions และ behaviors
3. **MCP** = เชื่อมต่อ external tools (GitHub, DB, etc.)
4. **Skills** = สร้าง custom commands ที่ใช้บ่อย
5. **Agents** = delegate งานไปยัง isolated context
6. **Hooks** = automation ที่ต้องเกิดทุกครั้ง
7. **Plugins** = package รวมทุกอย่างเพื่อแจกจ่าย
8. **Context** = จัดการ memory ให้ efficient
9. **Security** = ใช้ permissions และ sandbox

---

# Resources

## Official Documentation
- Claude Code Docs: https://docs.anthropic.com/en/docs/claude-code
- MCP Protocol: https://modelcontextprotocol.io/
- Agent Skills Standard: https://agentskills.io/

## Support
- GitHub Issues: https://github.com/anthropics/claude-code/issues
- `/help` command in Claude Code
- `/bug` command to report issues

---

*Document Version: 1.0.0*
*Last Updated: January 2026*
*For Claude Code Training Course: Basic AI to Expert*
