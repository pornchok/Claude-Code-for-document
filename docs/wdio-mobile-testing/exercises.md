# แบบฝึกหัด: WebdriverIO + Appium Mobile Testing

> **คำแนะนำ:** ลองทำทุกข้อด้วยตัวเองก่อน แล้วค่อยดูเฉลย

---

## บทที่ 1: What is WDIO

### Recall (ระดับ Beginner)

**ข้อ 1.1:** อธิบายด้วยคำของคุณเองว่า WDIO ต่างจาก Robot Framework อย่างไร ในแง่ของ: ภาษา, test structure, และ ecosystem โดยไม่ copy จากเอกสาร

**ข้อ 1.2:** วาด diagram แสดงว่า WDIO, Appium Server, UIAutomator2, และ Android Device เชื่อมกันยังไง เปรียบเทียบกับ RF + AppiumLibrary — protocol เหมือนกันไหม?

### Application (ระดับ Intermediate)

**ข้อ 1.3:** ทีมมี test suite เดิมทั้งหมดเป็น RF คุณถูกขอให้ evaluate ว่าควร migrate บางส่วนไป WDIO ไหม — ออกแบบ evaluation criteria ที่จะใช้ตัดสินใจ ระบุ factor ที่สำคัญอย่างน้อย 5 ข้อ

### Synthesis (ระดับ Advanced)

**ข้อ 1.4:** Engineering manager ถามว่า "ทำไมบาง test เร็วขึ้นหลังจาก migrate จาก RF ไป WDIO?" — อธิบายว่าความเร็วจริงๆ มาจากอะไร และอะไรที่ไม่ได้เกิดจากการเปลี่ยน framework

---

## บทที่ 2: Setup WDIO Project

### Recall (ระดับ Beginner)

**ข้อ 2.1:** เขียน commands ตามลำดับที่ต้องรันเพื่อสร้าง WDIO project ใหม่สำหรับ mobile testing ตั้งแต่ `mkdir` จนถึงรัน test ครั้งแรก (ห้ามดูเอกสาร)

**ข้อ 2.2:** `wdio.conf.js` มีส่วนประกอบหลักอะไรบ้าง? บอกมาอย่างน้อย 5 key ที่สำคัญ และแต่ละ key ทำหน้าที่อะไร

### Application (ระดับ Intermediate)

**ข้อ 2.3:** ออกแบบ project structure สำหรับ banking app test suite ที่ต้อง:
- รัน test บน staging และ production แยกกัน
- รองรับ Android 12 และ Android 13
- มี Allure reporting
- รัน CI/CD ได้

ให้แสดง folder structure และ config files ที่ต้องมี

### Synthesis (ระดับ Advanced)

**ข้อ 2.4:** WDIO test suite รันได้ปกติบน local machine แต่ fail บน CI/CD pipeline ด้วย error "appium: command not found" — วาง troubleshooting plan ระบุสาเหตุที่เป็นไปได้และวิธีแก้แต่ละอย่าง

---

## บทที่ 3: WDIO Appium Config

### Recall (ระดับ Beginner)

**ข้อ 3.1:** เขียน capabilities ใน `wdio.conf.js` สำหรับ:
- Platform: Android
- Device: emulator-5554
- App: com.nimble.bank, activity: .SplashActivity
- Driver: UIAutomator2
- No reset

**ข้อ 3.2:** ทำไม `'appium:automationName'` ถึงต้องใส่ quotes แต่ `platformName` ไม่ต้อง? อธิบาย JavaScript rule ที่เกี่ยวข้อง

### Application (ระดับ Intermediate)

**ข้อ 3.3:** เขียน `wdio.conf.js` ที่ดึงค่า `appPackage`, `deviceName`, และ `noReset` จาก environment variables พร้อมค่า default ที่สมเหตุสมผล

### Synthesis (ระดับ Advanced)

**ข้อ 3.4:** ทีมต้องการรัน test เดียวกันบน 3 device พร้อมกัน (Android 11, 12, 13) — ออกแบบ capabilities config และ explain ว่าต้องแก้ส่วนไหนของ `wdio.conf.js` บ้าง รวมถึง consideration เรื่อง Appium server instance

---

## บทที่ 4: Writing Tests

### Recall (ระดับ Beginner)

**ข้อ 4.1:** เขียน test case สำหรับ "ตรวจสอบ profile หน้าจอหลัง login" ที่:
- Login ก่อน (ใน `before` hook)
- ไปหน้า Profile
- ตรวจสอบ display name ปรากฏ
- ตรวจสอบ email ปรากฏ
- ออก profile กลับ home

โดย assumptions ตั้ง accessibility_id เองที่สมเหตุสมผล

**ข้อ 4.2:** ต่างกันยังไงระหว่าง `before`, `after`, `beforeEach`, `afterEach`? ให้ยกตัวอย่าง banking app scenario สำหรับแต่ละ hook

### Application (ระดับ Intermediate)

**ข้อ 4.3:** สร้าง test suite สำหรับ "Notification Center" ที่:
1. ตรวจสอบ notification badge แสดงจำนวนที่ถูกต้อง
2. คลิก notification และตรวจว่านำไปหน้าที่ถูกต้อง
3. Mark all as read และตรวจว่า badge หายไป

รวมถึง `beforeEach` ที่ navigate กลับ home ก่อนทุก test

### Synthesis (ระดับ Advanced)

**ข้อ 4.4:** Test suite 50 cases รัน 40 นาที — analyze ว่าส่วนไหนที่น่าจะกิน time มากที่สุดใน mobile E2E testing และเสนอ strategy ลดเวลาลงเหลือ 20 นาทีโดยไม่ตัด test cases

---

## บทที่ 5: Selectors & Interactions

### Recall (ระดับ Beginner)

**ข้อ 5.1:** แปลง locators ต่อไปนี้จาก RF syntax เป็น WDIO syntax:
- `accessibility_id=transfer_button`
- `id=com.nimble.bank:id/tv_balance`
- `xpath=//android.widget.Button[@text='ยืนยัน']`

**ข้อ 5.2:** เขียน WDIO code ที่:
1. รอ loading หายไป
2. กรอก username
3. กรอก password
4. ซ่อน keyboard
5. คลิก login
6. รอ home screen ขึ้น

### Application (ระดับ Intermediate)

**ข้อ 5.3:** สร้าง helper function `scrollUntilFound(selector, maxScrolls)` ที่:
- Scroll ลงทีละครั้ง
- ตรวจว่า selector ปรากฏหรือยัง
- Return element เมื่อพบ
- Throw error พร้อม message ที่ชัดเจนเมื่อ scroll ครบ maxScrolls แล้วยังไม่เจอ

### Synthesis (ระดับ Advanced)

**ข้อ 5.4:** Test suite มี test ที่ fail เป็นครั้งคราว (flaky) โดยเฉพาะ step ที่ต้องกรอก text field แล้ว click confirm — วิเคราะห์สาเหตุที่เป็นไปได้ทั้งหมดและเสนอ robust solution

---

## บทที่ 6: Page Objects

### Recall (ระดับ Beginner)

**ข้อ 6.1:** สร้าง `OTPPage.js` Page Object สำหรับหน้าป้อน OTP ที่มี:
- 6 ช่อง OTP (accessibility_id: otp_digit_0 ถึง otp_digit_5)
- ปุ่ม Verify (accessibility_id: verify_otp_btn)
- ข้อความ error (accessibility_id: otp_error_msg)
- ปุ่ม Resend OTP (accessibility_id: resend_otp_btn)

ให้มี method: `enterOTP(otpString)`, `submit()`, `resend()`, `getErrorMessage()`

### Application (ระดับ Intermediate)

**ข้อ 6.2:** เขียน test flow ที่ใช้หลาย Page Objects ร่วมกัน:
1. Login → HomePage
2. ไปหน้า Transfer → TransferPage
3. กรอก recipient และ amount
4. กด Confirm → OTPPage (ขึ้นหน้า OTP verification)
5. กรอก OTP ที่ถูกต้อง → กลับ HomePage พร้อม success message

### Synthesis (ระดับ Advanced)

**ข้อ 6.3:** Page Object `LoginPage` ของคุณมี method `login(username, password)` — ทีมอื่นบอกว่า method นี้ทำ assertion ด้วย (ตรวจว่า home screen ขึ้น) ซึ่งขัดกับ Page Object pattern — อธิบายว่าทำไมถึงเป็นปัญหา และเสนอ refactor plan ที่กระทบ test file น้อยที่สุด

---

## บทที่ 7: Comparing RF vs WDIO

### Recall (ระดับ Beginner)

**ข้อ 7.1:** เติม syntax เปรียบเทียบ RF กับ WDIO สำหรับ:
- คลิก element
- กรอก text
- รอ element ขึ้น (15 วินาที)
- assert ว่า text มี substring
- hide keyboard

### Application (ระดับ Intermediate)

**ข้อ 7.2:** วันนี้ product owner ขอให้สร้าง test สำหรับ feature ใหม่: "Dark mode toggle ใน Settings" ทีมมี RF test suite อยู่แล้ว แต่ feature นี้ต้องการ screenshot comparison เพื่อตรวจว่า color theme เปลี่ยนจริงๆ — คุณจะ implement ใน RF หรือ WDIO? เหตุผล?

### Synthesis (ระดับ Advanced)

**ข้อ 7.3:** หัวหน้าขอ proposal สำหรับ "test automation strategy สำหรับ Krungsri Nimble app ในปีหน้า" — เขียน proposal สั้นๆ (bullet points) ที่ครอบคลุม: tool selection rationale, ใครรับผิดชอบ test ส่วนไหน, how to handle tool coexistence, และ success metrics

---

## เฉลย

<details>
<summary>เฉลยบทที่ 2</summary>

**2.1:**
```bash
mkdir wdio-mobile-tests
cd wdio-mobile-tests
npm init -y
npm init wdio@latest .
# ตอบคำถาม wizard: Mobile, local, Mocha, appium service
npx wdio run wdio.conf.js
```

**2.2:** key หลักใน wdio.conf.js:
- `runner` — local หรือ remote
- `port` — Appium server port (4723)
- `services` — plugins เช่น `['appium']`
- `framework` — test runner (`'mocha'`)
- `reporters` — output format
- `specs` — test file path pattern
- `capabilities` — device/app config
- `mochaOpts` — timeout, ui

</details>

<details>
<summary>เฉลยบทที่ 3</summary>

**3.1:**
```javascript
capabilities: [{
    platformName: 'Android',
    'appium:automationName': 'UIAutomator2',
    'appium:deviceName': 'emulator-5554',
    'appium:appPackage': 'com.nimble.bank',
    'appium:appActivity': '.SplashActivity',
    'appium:noReset': true,
}]
```

**3.2:** `platformName` เป็น W3C standard key ที่ไม่มี special characters → เป็น valid JS identifier ไม่ต้อง quotes / `appium:automationName` มี `:` ซึ่งเป็น special character ใน JS object literal → ต้อง quotes

**3.3:**
```javascript
capabilities: [{
    platformName: 'Android',
    'appium:automationName': 'UIAutomator2',
    'appium:deviceName': process.env.DEVICE_NAME || 'emulator-5554',
    'appium:appPackage': process.env.APP_PACKAGE || 'com.nimble.bank',
    'appium:appActivity': '.SplashActivity',
    'appium:noReset': process.env.NO_RESET === 'true',
}]
```

</details>

<details>
<summary>เฉลยบทที่ 5</summary>

**5.1:**
```javascript
// accessibility_id=transfer_button
$('~transfer_button')

// id=com.nimble.bank:id/tv_balance
$('android=new UiSelector().resourceId("com.nimble.bank:id/tv_balance")')

// xpath=//android.widget.Button[@text='ยืนยัน']
$('//android.widget.Button[@text=\'ยืนยัน\']')
```

**5.3:**
```javascript
async function scrollUntilFound(selector, maxScrolls = 5) {
    for (let i = 0; i < maxScrolls; i++) {
        const el = await $(selector);
        if (await el.isDisplayed()) {
            return el;
        }
        await driver.execute('mobile: scroll', { direction: 'down' });
    }
    throw new Error(
        `Element '${selector}' not found after scrolling ${maxScrolls} times`
    );
}
```

</details>

<details>
<summary>เฉลยบทที่ 6</summary>

**6.1:**
```javascript
// pageObjects/OTPPage.js
import BasePage from './BasePage.js';

class OTPPage extends BasePage {
    get otpDigits() { return $$('[accessibility_id^="otp_digit_"]'); }
    get verifyButton() { return $('~verify_otp_btn'); }
    get errorMessage() { return $('~otp_error_msg'); }
    get resendButton() { return $('~resend_otp_btn'); }

    async enterOTP(otpString) {
        for (let i = 0; i < otpString.length; i++) {
            await $(`~otp_digit_${i}`).setValue(otpString[i]);
        }
    }

    async submit() {
        await (await this.verifyButton).click();
    }

    async resend() {
        await (await this.resendButton).click();
    }

    async getErrorMessage() {
        await (await this.errorMessage).waitForDisplayed({ timeout: 10000 });
        return (await this.errorMessage).getText();
    }
}

export default new OTPPage();
```

</details>
