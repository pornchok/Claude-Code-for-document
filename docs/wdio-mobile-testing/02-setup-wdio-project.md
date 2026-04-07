# บทที่ 2: Setup WDIO Project

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อน:

> **WDIO ต่างจาก Robot Framework ที่สำคัญที่สุดคืออะไร? และทั้งสองตัวใช้ Appium server เดียวกันได้ไหม?**

---

<details>
<summary>ดูเฉลย</summary>

ต่างที่ภาษาและ paradigm — WDIO ใช้ JavaScript (describe/it), RF ใช้ keyword-driven (.robot) ทั้งสองใช้ Appium server ตัวเดียวกันได้เลย เพราะทั้งคู่คุยผ่าน W3C WebDriver protocol เหมือนกัน

</details>

---

## วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะ:

- สร้าง WDIO project ด้วย `npm init wdio` wizard ได้
- เข้าใจโครงสร้างไฟล์ที่ wizard สร้างให้
- รู้ว่า `wdio.conf.js` คืออะไรและทำหน้าที่อะไร
- รัน `npx wdio run` เพื่อ execute test ได้

---

## ทำไมต้องรู้? (Why)

WDIO มี wizard ที่ทำให้ setup ง่ายขึ้นมาก แต่ถ้าไม่เข้าใจว่า wizard ตั้งค่าอะไรให้ — เมื่อ config ผิดคุณจะไม่รู้จะแก้ที่ไหน

บทนี้ทำให้คุณเข้าใจ project structure แบบ "รู้ว่าไฟล์ไหนทำหน้าที่อะไร" ไม่ใช่แค่ copy-paste แล้วหวังว่ามันจะทำงาน

---

## Analogy: `wdio.conf.js` เหมือน settings ของแอป

ทุกแอปมีหน้า Settings ที่รวม config ทุกอย่างไว้ที่เดียว:
- `wdio.conf.js` = settings ของ WDIO ทั้งหมด
- เปลี่ยน framework, reporter, capabilities — ทำที่นี่ที่เดียว
- test files เองไม่ต้องรู้ว่า app รันบน device ไหน — config จัดการให้

> ⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
> - `wdio.conf.js` ไม่เปลี่ยนบ่อย → จริงๆ ต้องแก้ทุกครั้งที่เปลี่ยน device, environment, หรือ test framework
> - Config ไฟล์เดียวพอสำหรับทุก environment → ทีม professional มักมีหลาย config (staging, production, CI)

---

## เนื้อหาหลัก

### ขั้นตอนที่ 1: สร้าง Node.js Project

```bash
mkdir wdio-mobile-tests
cd wdio-mobile-tests
npm init -y
```

### ขั้นตอนที่ 2: รัน WDIO Configuration Wizard

```bash
npm init wdio@latest .
```

> "The fastest way to configure a new project is running: npm init wdio@latest . This command executes a configuration wizard that helps you to configure your test suite."
> *(webdriver.io)*

Wizard จะถามหลายคำถาม — ตอบตามนี้สำหรับ mobile testing:

```
? What type of testing would you like to do?
  ❯ Mobile (Android, iOS, TvOS)        ← เลือกนี้

? Where is your automation backend located?
  ❯ On my local machine                ← สำหรับ local development

? Which framework do you want to use?
  ❯ Mocha                              ← เลือก Mocha (most common)

? Do you want to use a compiler?
  ❯ No!                               ← เลือก No สำหรับ JavaScript
  (หรือ TypeScript ถ้าต้องการ type safety)

? Do you want WebdriverIO to autogenerate some test files?
  ❯ Yes                               ← ให้ generate example files

? Do you want to add a service to your test setup?
  ❯ appium                            ← เลือก Appium service

? Which reporter do you want to use?
  ❯ spec                              ← Spec reporter สำหรับ terminal output
```

### ขั้นตอนที่ 3: โครงสร้างไฟล์ที่ได้

หลัง wizard เสร็จ project จะมีโครงสร้างแบบนี้:

```
wdio-mobile-tests/
├── package.json            ← dependencies และ scripts
├── package-lock.json
├── wdio.conf.js            ← WDIO configuration (หัวใจของ project)
└── test/
    └── specs/
        └── example.e2e.js  ← ตัวอย่าง test file ที่ wizard สร้างให้
```

### ขั้นตอนที่ 4: ทำความเข้าใจ `wdio.conf.js`

เปิดไฟล์ `wdio.conf.js` จะเห็น config หลักๆ:

```javascript
export const config = {
    // Runner: local หรือ remote
    runner: 'local',

    // Appium server port
    port: 4723,

    // Services: Appium จะ start/stop อัตโนมัติ
    services: ['appium'],

    // Test Framework
    framework: 'mocha',

    // Reporter
    reporters: ['spec'],

    // Test Files pattern
    specs: ['./test/specs/**/*.js'],

    // Capabilities (ยังว่าง — จะตั้งในบทถัดไป)
    capabilities: [{}],

    // Mocha options
    mochaOpts: {
        ui: 'bdd',
        timeout: 60000
    }
};
```

### ขั้นตอนที่ 5: รัน Test ครั้งแรก

```bash
npx wdio run wdio.conf.js
```

> "Execute your test suite with: npx wdio run ./wdio.conf.js"
> *(webdriver.io)*

ตอนนี้ยัง fail เพราะ capabilities ยังไม่ได้ตั้ง — จะแก้ในบทที่ 3

---

## ตัวอย่าง 3 ระดับ

### Beginner: ดู scripts ใน package.json

Wizard เพิ่ม script ให้ใน `package.json`:

```json
{
  "scripts": {
    "test": "wdio run wdio.conf.js"
  }
}
```

ทำให้รัน test ได้ด้วย:
```bash
npm test
# เหมือนกับ npx wdio run wdio.conf.js
```

### Intermediate: หลาย Config File สำหรับหลาย Environment

สร้าง config แยกสำหรับ staging และ production:

```
wdio.conf.js          ← base config
wdio.staging.conf.js  ← override สำหรับ staging
wdio.prod.conf.js     ← override สำหรับ production
```

```javascript
// wdio.staging.conf.js
import { config as baseConfig } from './wdio.conf.js';

export const config = {
    ...baseConfig,
    capabilities: [{
        'appium:appPackage': 'com.nimble.bank.staging',
        'appium:appActivity': '.SplashActivity',
    }]
};
```

```bash
# รัน staging
npx wdio run wdio.staging.conf.js
```

### Advanced: เพิ่ม Allure Reporter สำหรับ HTML Report

```bash
npm install @wdio/allure-reporter --save-dev
npm install allure-commandline --save-dev
```

```javascript
// wdio.conf.js
reporters: [
    'spec',
    ['allure', {
        outputDir: 'allure-results',
        disableWebdriverStepsReporting: true,
    }]
],
```

```json
// package.json scripts
{
    "scripts": {
        "test": "wdio run wdio.conf.js",
        "report": "allure generate allure-results --clean && allure open"
    }
}
```

---

## Common Mistakes

❌ **รัน `npx wdio` โดยไม่ระบุ config file ในบาง environment**
→ หา config ไม่เจอหรือใช้ default ที่ผิด
✅ **ระบุ config file ทุกครั้ง: `npx wdio run wdio.conf.js`**
*(source: webdriver.io/docs/gettingstarted)*

---

❌ **แก้ capabilities ใน test file แทนที่จะแก้ใน `wdio.conf.js`**
→ capabilities กระจาย ดูแลยาก
✅ **capabilities อยู่ใน `wdio.conf.js` เท่านั้น** — test file ไม่ต้องรู้
*(source: webdriver.io/docs/configurationfile)*

---

❌ **ใช้ CommonJS `require()` ใน project ที่ตั้ง `"type": "module"` ใน package.json**
```javascript
const { config } = require('./wdio.conf.js'); // ❌ Error
```
✅ **ตรวจ package.json ก่อน — ถ้ามี `"type": "module"` ให้ใช้ `import`**
```javascript
import { config } from './wdio.conf.js'; // ✅
```
*(source: Node.js docs)*

---

## สรุปบท

ลองตอบก่อนดูเฉลย:

> **คำถาม 1:** `wdio.conf.js` ทำหน้าที่อะไร? และถ้าต้องการเพิ่ม HTML reporter คุณต้องแก้ไฟล์ไหน?

> **คำถาม 2:** ทำไมทีมที่ test ทั้ง staging และ production ถึงควรมีหลาย config files แทนที่จะใช้ไฟล์เดียว?

> **คำถาม 3:** หลังรัน wizard แล้ว test fail ด้วย "No capabilities found" — คุณจะแก้ที่ไหน?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย 1:** `wdio.conf.js` เป็น central config ของทั้ง project — กำหนด framework, reporter, capabilities, services, test file paths ทุกอย่าง ถ้าจะเพิ่ม reporter ก็แก้ที่ `reporters` array ในไฟล์นี้

**เฉลย 2:** staging กับ production ใช้ app package name ต่างกัน (เช่น `com.app.staging` vs `com.app`) และอาจมี device ต่างกัน — แยก config ทำให้ชัดเจน ไม่ต้องแก้ทุกครั้งที่สลับ environment และลดโอกาส error จากการลืมแก้ค่า

**เฉลย 3:** แก้ที่ `capabilities` array ใน `wdio.conf.js` — เพิ่ม platformName, automationName, appPackage, appActivity ที่ถูกต้อง

</details>

---

**บทต่อไป:** [บทที่ 3 — เชื่อม WDIO กับ Appium](03-wdio-appium-config.md)
