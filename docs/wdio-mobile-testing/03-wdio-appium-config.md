# บทที่ 3: เชื่อม WDIO กับ Appium สำหรับ Mobile

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อน:

> **`wdio.conf.js` ต่างจาก `variables.robot` ใน RF ยังไง? และ capabilities ใน WDIO ตั้งที่ไหน?**

---

<details>
<summary>ดูเฉลย</summary>

`wdio.conf.js` รวม config ทั้งหมดไว้ที่เดียว (framework, reporter, capabilities, services) ในขณะที่ RF แยก variables.robot ออกมาต่างหาก ใน WDIO capabilities ตั้งที่ `capabilities` array ใน `wdio.conf.js`

</details>

---

## วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะ:

- ตั้ง capabilities สำหรับ Android ใน `wdio.conf.js` ได้
- เข้าใจว่า `@wdio/appium-service` ทำอะไร
- รัน WDIO test เชื่อมกับ Appium server ได้จริง
- Debug connection error ระหว่าง WDIO กับ Appium ได้

---

## ทำไมต้องรู้? (Why)

Capabilities ใน WDIO เหมือนกับ RF ทุกตัว — เพราะทั้งคู่ส่งไปยัง Appium server เดียวกัน แต่ **format ของ syntax ต่างกัน**

ถ้าไม่รู้ format ที่ถูก → session ไม่ start แม้ value จะถูก

---

## Analogy: Capabilities เหมือนสายเชื่อมต่อ

ก่อน WDIO จะคุยกับ Appium ได้ต้องมี "สายเชื่อมต่อ" ที่ถูกต้อง:
- Appium service = ปลั๊กที่ start Appium server ให้อัตโนมัติ
- Capabilities = ข้อมูลที่ส่งไปเมื่อเสียบปลั๊ก (บอกว่าจะทำอะไร)
- Port 4723 = ช่องเสียบ

> ⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
> - WDIO ต้อง start Appium เอง → `@wdio/appium-service` ทำให้ start อัตโนมัติ แต่ Appium ต้องติดตั้งอยู่ในเครื่องก่อน
> - capabilities ตั้งครั้งเดียวแล้วใช้ตลอด → แต่ละ session (test run) ส่ง capabilities ใหม่

---

## เนื้อหาหลัก

### ติดตั้ง Appium Service

```bash
npm install @wdio/appium-service --save-dev
```

> "Install via npm: npm install @wdio/appium-service --save-dev"
> *(webdriver.io/docs/appium-service)*

### ตั้งค่า `wdio.conf.js` สำหรับ Android

```javascript
// wdio.conf.js
export const config = {
    runner: 'local',

    // Appium server port
    port: 4723,

    // @wdio/appium-service จะ start/stop Appium อัตโนมัติ
    services: ['appium'],

    // Test framework
    framework: 'mocha',
    reporters: ['spec'],

    // Test file pattern
    specs: ['./test/specs/**/*.js'],

    // Capabilities: ระบุ device และ app ที่จะทดสอบ
    capabilities: [{
        platformName: 'Android',
        'appium:automationName': 'UIAutomator2',
        'appium:deviceName': 'emulator-5554',
        'appium:appPackage': 'com.example.app',
        'appium:appActivity': '.MainActivity',
        'appium:noReset': true,
    }],

    // Mocha options
    mochaOpts: {
        ui: 'bdd',
        timeout: 60000  // 60 วินาที ต่อ test case (ดูคำอธิบายด้านล่าง)
    }
};
```

> "Add to your wdio.conf.js: export const config = { port: 4723, services: ['appium'] };"
> *(webdriver.io/docs/appium-service)*

**ทำไม `timeout: 60000` (60 วินาที)?**

Mobile app ใช้เวลานานกว่า web เพราะ: (1) app launch time — 3-10 วินาที (2) screen transition พร้อม animation — 1-3 วินาทีต่อ transition (3) API calls — banking app มี network latency (4) emulator ช้ากว่า real device

test case ที่ต้อง login → navigate → กรอก form → submit อาจใช้เวลา 30-50 วินาที — ถ้า timeout สั้นกว่านี้ test จะ fail ทั้งที่ app ยังทำงานอยู่ปกติ

### ความแตกต่างของ Capabilities Syntax: RF vs WDIO

เนื้อหาเหมือนกัน แต่ format ต่างกัน:

**Robot Framework:**
```robotframework
Open Application    http://localhost:4723
...    platformName=Android
...    appium:automationName=UIAutomator2
...    appium:deviceName=emulator-5554
...    appium:appPackage=com.example.app
...    appium:appActivity=.MainActivity
```

**WDIO (wdio.conf.js):**
```javascript
capabilities: [{
    platformName: 'Android',
    'appium:automationName': 'UIAutomator2',
    'appium:deviceName': 'emulator-5554',
    'appium:appPackage': 'com.example.app',
    'appium:appActivity': '.MainActivity',
}]
```

Key difference: ใน WDIO capabilities ที่มี `:` ต้องใส่ใน quotes เพราะ colon ไม่ valid ใน JS object key ปกติ

### Verify การเชื่อมต่อ

สร้าง test ง่ายๆ เพื่อตรวจว่า connect ได้:

```javascript
// test/specs/connection.test.js
describe('Connection Test', () => {
    it('should connect to app', async () => {
        // ถ้า session สร้างได้ แปลว่า connect สำเร็จ
        const appState = await driver.queryAppState('com.example.app');
        console.log('App state:', appState);
        // State 4 = running in foreground
    });
});
```

```bash
# เปิด emulator ก่อน แล้วรัน
npx wdio run wdio.conf.js
```

### ทำความเข้าใจ appium-service

เมื่อตั้ง `services: ['appium']` — WDIO จะ:
1. **start Appium server** ก่อน test รัน (ไม่ต้องรันเอง)
2. **stop Appium server** หลัง test ทั้งหมดจบ

ข้อดี: ไม่ต้องเปิด terminal แยกสำหรับ Appium

ข้อควรระวัง: ถ้า Appium ไม่ได้ติดตั้งใน PATH → service จะหา Appium ไม่เจอ

---

## ตัวอย่าง 3 ระดับ

### Beginner: Config สำหรับ Mobile Web (Chrome)

```javascript
capabilities: [{
    platformName: 'Android',
    'appium:automationName': 'UIAutomator2',
    'appium:deviceName': 'emulator-5554',
    'appium:browserName': 'Chrome',  // เปิด Chrome แทน native app
}]
```

### Intermediate: Parallel Testing บนหลาย Device

```javascript
capabilities: [
    // Device 1: Android 12
    {
        platformName: 'Android',
        'appium:automationName': 'UIAutomator2',
        'appium:deviceName': 'emulator-5554',
        'appium:platformVersion': '12',
        'appium:appPackage': 'com.example.app',
        'appium:appActivity': '.MainActivity',
        'appium:systemPort': 8200,  // ต้องต่างกันต่อ instance
    },
    // Device 2: Android 13
    {
        platformName: 'Android',
        'appium:automationName': 'UIAutomator2',
        'appium:deviceName': 'emulator-5556',
        'appium:platformVersion': '13',
        'appium:appPackage': 'com.example.app',
        'appium:appActivity': '.MainActivity',
        'appium:systemPort': 8201,  // port ต่างกัน ป้องกัน conflict
    }
],
maxInstances: 2,  // รัน parallel 2 instances พร้อมกัน
```

**ข้อควรระวังเรื่อง port สำหรับ parallel testing:**
- `appium:systemPort` — UIAutomator2 ใช้ port นี้คุยกับ Appium ต้องต่างกันทุก instance (default: 8200)
- emulator port — `emulator-5554` และ `emulator-5556` ต้องเปิดไว้ก่อนรัน test
- ถ้า port conflict → error "Could not find a connected Android device" หรือ "Address already in use"

**Performance:** การรัน parallel บน 2 devices ลดเวลาได้ ~40-50% แต่ต้องการ RAM เพิ่ม (emulator ละ ~2-3GB)

### Advanced: Config ที่ดึงค่าจาก Environment Variables

```javascript
// wdio.conf.js — CI/CD friendly
export const config = {
    port: 4723,
    services: ['appium'],
    framework: 'mocha',
    reporters: ['spec'],
    specs: ['./test/specs/**/*.js'],

    capabilities: [{
        platformName: 'Android',
        'appium:automationName': 'UIAutomator2',
        'appium:deviceName': process.env.DEVICE_NAME || 'emulator-5554',
        'appium:appPackage': process.env.APP_PACKAGE || 'com.example.app',
        'appium:appActivity': process.env.APP_ACTIVITY || '.MainActivity',
        'appium:noReset': process.env.NO_RESET === 'true',
    }],

    mochaOpts: {
        ui: 'bdd',
        timeout: parseInt(process.env.TEST_TIMEOUT) || 60000,
    }
};
```

```bash
# รันกับ staging
APP_PACKAGE=com.example.staging npx wdio run wdio.conf.js
```

---

## Common Mistakes

❌ **ใส่ capabilities key ที่มี `:` โดยไม่ใส่ quotes**
```javascript
capabilities: [{
    appium:automationName: 'UIAutomator2'  // ❌ Syntax error
}]
```
✅ **ใส่ quotes รอบ key ที่มี `:`**
```javascript
capabilities: [{
    'appium:automationName': 'UIAutomator2'  // ✅
}]
```
*(source: JavaScript object literal syntax)*

---

❌ **ลืมว่า `appium-service` ต้องการ Appium ติดตั้งอยู่ก่อน**
→ Error: "Appium command not found"
✅ **ตรวจว่า `appium` อยู่ใน PATH: `appium --version`** ก่อนรัน
*(source: webdriver.io/docs/appium-service)*

---

❌ **ใช้ `capabilities: {}` (object) แทน `capabilities: [{}]` (array)**
→ WDIO expect array เสมอ
✅ **`capabilities` ต้องเป็น array เสมอ** — แม้จะมีแค่ device เดียว
*(source: webdriver.io/docs/configurationfile)*

---

## สรุปบท

ลองตอบก่อนดูเฉลย:

> **คำถาม 1:** ทำไม capabilities key ที่มี `:` เช่น `appium:automationName` ถึงต้องใส่ quotes ใน JavaScript?

> **คำถาม 2:** `@wdio/appium-service` ทำอะไรให้คุณบ้าง? และถ้าไม่ใช้ service นี้ คุณต้องทำอะไรเพิ่ม?

> **คำถาม 3:** ถ้าต้องการรัน test เดียวกันบน Android 12 และ Android 13 พร้อมกัน — ต้องแก้ `wdio.conf.js` อย่างไร?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย 1:** ใน JavaScript `:` เป็น separator ระหว่าง key กับ value ใน object literal ดังนั้น `appium:automationName` จะถูก parse ผิด ต้องใส่ quotes ให้ engine รู้ว่านี่คือ string key

**เฉลย 2:** `appium-service` auto-start Appium server ก่อน test และ stop หลัง test จบ ถ้าไม่ใช้ต้องเปิด terminal แยกรัน `appium` ก่อน และ stop เอง

**เฉลย 3:** เพิ่มอีก 1 entry ใน `capabilities` array (รวมเป็น 2 objects) และตั้ง `maxInstances: 2` เพื่อให้รัน parallel พร้อมกัน

</details>

---

**บทต่อไป:** [บทที่ 4 — เขียน Test แรก](04-writing-tests.md)
