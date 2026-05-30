# Playwright TypeScript Master Course

ยินดีต้อนรับเข้าสู่คอร์สออนไลน์ที่สอนการเขียน **Automation Test ด้วย Playwright และ TypeScript** ระดับมาสเตอร์ (Master)

คอร์สนี้ออกแบบมาให้ผู้ที่มี **Robot Framework หรือ Selenium พื้นฐาน** สามารถเข้าใจและใช้ Playwright ได้อย่างลึกซึ้ง — ไม่ใช่แค่พื้นฐาน แต่ถึงระดับการออกแบบระบบทดสอบในการผลิต (production-grade)

---

## Prerequisites (ต้องมีครบก่อนเริ่ม)

เพื่อให้เรียนได้ผล ต้องตรวจสอบให้แน่ใจว่าคุณมีครบดังนี้:

### 1. Environment
- **Node.js**: เวอร์ชัน 20.x, 22.x หรือ 24.x ขึ้นไป
  ```bash
  node --version
  # ควร output v20.x.x ขึ้นไป
  ```
- **npm**: 9.0 ขึ้นไป (มาพร้อม Node.js โดยปกติ)
  ```bash
  npm --version
  ```

### 2. Programming Knowledge (พื้นฐานจำเป็น)
- **JavaScript/TypeScript เบื้องต้น**:
  - ตัวแปร (variable), function, async/await
  - Arrow function `() => {}`
  - Promise และ async/await คำนวณ
  - Object destructuring (รู้พอเพียง)
- **HTML เบื้องต้น**:
  - HTML elements: `<div>`, `<input>`, `<button>` เป็นต้น
  - Attributes: `id`, `class`, `data-*`
  - CSS selectors สำคัญบางตัว (id selector, class selector)
- **Test Automation พื้นฐาน**:
  - มี Robot Framework หรือ Selenium experience นิดหน่อยจะช่วย
  - คิดเป็นนอก "Mindset shift" มี Ch01 เก็บไว้

### 3. Text Editor/IDE
- **Visual Studio Code** (แนะนำ) หรือ IDE ที่เสมือ
- **TypeScript extension** (ถ้าใช้ VS Code)
  ```
  ext install ms-vscode.vscode-typescript-next
  ```

---

## วิธี Setup ก่อนเริ่มเรียน

### ขั้นตอน 1: Install Playwright
```bash
# สร้าง folder สำหรับ course
mkdir playwright-course
cd playwright-course

# Install Playwright + TypeScript
npm init playwright@latest -- --typescript
```

หลังจาก `npm init` เสร็จ คุณจะได้ folder structure แบบนี้:
```
playwright-course/
├── tests/
│   ├── example.spec.ts
│   └── ...
├── playwright.config.ts
├── package.json
└── tsconfig.json
```

### ขั้นตอน 2: ติดตั้ง Browsers
```bash
# ดาวน์โหลด browser binaries (Chromium, Firefox, WebKit)
npx playwright install
```

### ขั้นตอน 3: สร้าง Demo App สำหรับ Practice
สำหรับบทที่ 2 เป็นต้นไป เราจะเขียน test ไปกับ Demo App นี้:

```bash
# Clone demo app (ก่อนสร้างไฟล์ example.spec.ts)
git clone https://github.com/playwright-demo-app/todo-app.git
cd todo-app
npm install
npm start
# → ใช้งาน http://localhost:3000
# (เปิด browser อื่นหรือ terminal อื่นต่อเพื่อรันการ test)
```

**หมายเหตุ**: ในแต่ละบท คุณจะเขียน `.spec.ts` ใหม่ที่ชี้ไปที่ http://localhost:3000 — ให้ทำ `npm start` ตรงนี้ทิ้งไว้ตลอดเวลา

### ขั้นตอน 4: ยืนยันการติดตั้งสำเร็จ
```bash
# กลับไปที่ folder playwright-course
cd ..

# รัน test ตัวอย่าง
npx playwright test

# ควรเห็น output บอกว่า test passed หมด
```

---

## Table of Contents — 18 บท

ทั้งคอร์สสำเร็จใน **~22 ชั่วโมง** (ไม่นับเวลา optional deep-dive)

| บท | หัวข้อ | ระยะเวลา |
|------|--------|---------|
| [01](./01-why-playwright.md) | Why Playwright? Mindset Shift จาก RF/Selenium | 45 นาที |
| [02](./02-setup-typescript.md) | Setup + TypeScript Essentials | 60 นาที |
| [03](./03-architecture.md) | Architecture: Browser → BrowserContext → Page | 45 นาที |
| [04](./04-locators.md) | Locators — Accessibility-First | 90 นาที |
| [05](./05-actions-assertions.md) | Actions & Assertions | 90 นาที |
| [06](./06-debugging.md) | Debugging | 60 นาที |
| [07](./07-fixtures.md) | Fixtures | 75 นาที |
| [08](./08-page-object-model.md) | Page Object Model | 60 นาที |
| [09](./09-test-organization.md) | Test Organization | 60 นาที |
| [10](./10-configuration-projects.md) | Configuration & Projects | 75 นาที |
| [11](./11-parallelism-sharding-reporting.md) | Parallelism, Sharding & Reporting | 75 นาที |
| [12](./12-mocking-network-time-browser.md) | Mocking: Network, Time & Browser APIs | 90 นาที |
| [13](./13-authentication-storage-state.md) | Authentication & Storage State | 75 นาที |
| [14](./14-advanced-browser-emulation.md) | Advanced Browser + Emulation | 90 นาที |
| [15](./15-api-testing-hybrid.md) | API Testing + Hybrid | 75 นาที |
| [16](./16-visual-accessibility-testing.md) | Visual Testing + Accessibility Testing | 60 นาที |
| [17](./17-cicd.md) | CI/CD | 60 นาที |
| [18](./18-production-patterns.md) | Production Patterns | 75 นาที |

---

## 🎯 หลักการเรียนให้ได้ผล

ถ้าต้องการเรียนให้จำจริง และใช้ Playwright ได้จริงในงาน ให้ถือหลักการนี้:

### 1. **อย่า Copy-Paste Code**
แม้ว่า example code ในหลายบท คุณสามารถ copy ได้ แต่การพิมพ์เองจะช่วยให้:
- ทำความเข้าใจระดับลึก (จำไว้นานกว่า)
- เจอ syntax error เอง → รู้ว่าต้อง debug อย่างไร
- ถ้า copy-paste → ใช้ได้แต่ตอนต่อมาปัญหาจะเยอะ

### 2. **ทำ Exercise ทุกบท**
แต่ละบท (Ch02–Ch18) มี **exercises.md** ที่มี:
- **Recall Questions**: ตอบด้วยคำตัวเอง ไม่ใช่มัลติเพิ่ล
- **Application Exercises**: ใช้สิ่งที่เรียนกับสถานการณ์ใหม่
- **Synthesis Tasks**: ออกแบบหรือหา bug อย่างมืออาชีพ

**บังคับ**: ลองตอบก่อนดูเฉลย เสมอ — ถ้า peek เฉลยไปเร็ว จำไม่ได้

### 3. **รัน Demo App ทิ้งไว้ตลอด**
- ทำให้เห็นผลจริง ไม่ใช่ abstract
- ตอนเขียน test ใหม่ ให้เปิด browser tab สำหรับ demo app ไว้

### 4. **ถ้า Code Error ให้แก้ก่อน**
- อย่าข้ามบทเพื่อดูว่าเกิดอะไรขึ้น (Cognitive Load จะมากเกินไป)
- ทำให้เสียเวลาพอยึดมั่น เรียน error handling อย่างจริงจัง

---

## 📋 Playwright Version ที่ใช้ในคอร์สนี้

- **Playwright**: ใช้วิธี `npm init playwright@latest` เพื่อให้ได้เวอร์ชันล่าสุด (คอร์สนี้อัปเดตตามเวอร์ชันล่าสุดเสมอ)
- **Node.js**: 20.x, 22.x หรือ 24.x ขึ้นไป (ตามที่ Playwright สนับสนุน)
- **TypeScript**: 5.0 ขึ้นไป (มาพร้อม Playwright โดยอัตโนมัติ)

**ความสำคัญ**: ถ้าคุณใช้ Node.js 18.x หรือเก่ากว่า test อาจจะ error เพราะเวอร์ชันเก่ากว่า Playwright สนับสนุน — อัปเดต Node.js ก่อน

---

## ❓ FAQ

### Q: ฉันต้องเรียนบทเรียงลำดับหรือสามารถข้ามบทได้?
**A**: เรียนตามลำดับ บท 01–10 แนวตั้งสำคัญ (ฟาวเดชัน) — ข้ามจะเข้าใจไม่ลึก บท 11–18 ขึ้นอยู่กับหน้าที่ (สามารถเลือกได้บ้าง)

### Q: ฉันใช้ Selenium มา 5 ปี คอร์สนี้เหมาะไหม?
**A**: ใช่ บท 01 เก็บไว้สำหรับเปลี่ยน mindset จาก Selenium → Playwright (Locator ต่างกันเยอะ) — อ่านจะได้ประโยชน์

### Q: ต้อง TypeScript หรือใช้ JavaScript ได้?
**A**: คอร์สนี้ **TypeScript เท่านั้น** เพราะ TypeScript แนะนำใน production และ IDE support ดีกว่า JavaScript มาก (autocomplete, type checking)

### Q: ทำแบบฝึกหัด syntax error ไม่ผ่าน ทำไง?
**A**: อ่าน error message อย่างตั้งใจ — เขียนว่า "line X: ..." นั่นคือ hint ส่วนใหญ่ fix ได้ด้วยตัวเอง (ฟีเจอร์ของ TypeScript ช่วยเยอะ) ถ้าจริงๆ ติด ข้ามไป แล้ว comeback หลัง exercise นั้นเสร็จ

---

## 🚀 เริ่มต้นกันเลย

ไปที่ [บท 01: Why Playwright? Mindset Shift](./01-why-playwright.md)
