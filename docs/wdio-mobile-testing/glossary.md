# Glossary: WebdriverIO + Appium Mobile Testing

คำศัพท์ทั้งหมดที่ใช้ในคอร์สนี้ เรียงตามตัวอักษร

---

## A

**async/await**
JavaScript syntax สำหรับจัดการ asynchronous operations — ทุก WDIO call ต้องใช้ `await` เพราะ operation เป็น async
SOURCE: https://developer.mozilla.org/en-US/docs/Learn/JavaScript/Asynchronous/Promises

**Appium Service (`@wdio/appium-service`)**
WDIO plugin ที่ auto-start และ auto-stop Appium server ก่อน/หลัง test run — ไม่ต้องเปิด terminal แยก
SOURCE: https://webdriver.io/docs/appium-service/

## B

**BasePage**
Class หลักที่ Page Object classes อื่นๆ extends จาก — มี shared methods เช่น `waitForLoading()`, `hideKeyboard()`
SOURCE: https://webdriver.io/docs/pageobjects/

**BDD (Behavior-Driven Development)**
Test writing style ที่ใช้ภาษาใกล้เคียง natural language เช่น `describe('Login', () => { it('should...') })` — Mocha ใช้ BDD style เป็น default
SOURCE: https://webdriver.io/docs/frameworks/

**`before` / `after`**
Mocha hooks ที่รันครั้งเดียวก่อน/หลัง `describe` block ทั้งหมด — ใช้สำหรับ setup/teardown ที่แพงและทำแค่ครั้งเดียว
SOURCE: https://webdriver.io/docs/frameworks/

**`beforeEach` / `afterEach`**
Mocha hooks ที่รันก่อน/หลังทุก `it` block — ใช้สำหรับ reset state ระหว่าง test cases
SOURCE: https://webdriver.io/docs/frameworks/

## C

**capabilities** (ใน WDIO)
Object ใน `wdio.conf.js` ที่บอก Appium ว่าจะทดสอบ platform อะไร app ไหน บน device ไหน — เนื้อหาเหมือน RF แต่ syntax เป็น JavaScript object
SOURCE: https://webdriver.io/docs/configurationfile/

**ChainablePromiseElement**
TypeScript type ของ element ที่ `$()` คืนมา — รองรับทั้ง chaining และ await
SOURCE: https://webdriver.io/docs/typescript/

**CommonJS vs ESM**
สองรูปแบบ module system ใน Node.js — CommonJS ใช้ `require()`, ESM ใช้ `import/export` — WDIO v9 รองรับทั้งสอง แต่ต้องเลือกและใช้ให้ consistent
SOURCE: https://nodejs.org/api/esm.html

## D

**`describe()`**
Mocha function สำหรับจัด group test cases ที่เกี่ยวข้องกัน — เทียบกับ Test Suite ใน RF
SOURCE: https://webdriver.io/docs/frameworks/

**`driver`**
Global object ใน WDIO ที่ represent Appium/WebDriver session — ใช้สำหรับ session-level commands เช่น `driver.hideKeyboard()`, `driver.reset()`
SOURCE: https://webdriver.io/docs/api/webdriver/

## E

**`expect()`**
WDIO assertion function ที่ใช้ตรวจสอบผลลัพธ์ — `expect(element).toBeDisplayed()`, `expect(text).toContain('...')`
SOURCE: https://webdriver.io/docs/api/expect-webdriverio/

## G

**getter function**
JavaScript `get` keyword ที่ทำให้ property ถูก evaluate เมื่อเรียกใช้ (lazy) — ใช้ใน Page Objects เพื่อให้ selector resolve เมื่อ page พร้อมแล้ว ไม่ใช่ตอน init
SOURCE: https://webdriver.io/docs/pageobjects/

## I

**`it()`**
Mocha function สำหรับ define test case หนึ่งๆ — เทียบกับ Test Case ใน RF
SOURCE: https://webdriver.io/docs/frameworks/

## M

**Mocha**
JavaScript test framework ที่ WDIO ใช้เป็น default — มี BDD interface (`describe/it`) และ hooks (`before/after/beforeEach/afterEach`)
SOURCE: https://webdriver.io/docs/frameworks/

**`mochaOpts`**
Configuration สำหรับ Mocha ใน `wdio.conf.js` — ตั้ง `timeout`, `ui`, `retries` ฯลฯ
SOURCE: https://webdriver.io/docs/frameworks/

## N

**npm (Node Package Manager)**
Package manager สำหรับ JavaScript/Node.js — ใช้ install WDIO และ dependencies ทั้งหมด
SOURCE: https://docs.npmjs.com/

## P

**Page Object Pattern**
Design pattern ที่แยก locators และ interactions ของแต่ละ screen ออกเป็น class แยก — ลด duplication และเพิ่ม maintainability
SOURCE: https://webdriver.io/docs/pageobjects/

**Promise**
JavaScript object ที่แทนค่า async operation — ทุก WDIO call คืน Promise ต้องใช้ `await` เพื่อรอค่า
SOURCE: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise

## R

**reporter**
Plugin สำหรับ output test results ใน format ต่างๆ เช่น `spec` (terminal), `allure` (HTML report)
SOURCE: https://webdriver.io/docs/allure-reporter/

## S

**`$()` selector**
WDIO function สำหรับหา element หนึ่งตัว — `await $('~login_btn')`, `await $('//xpath')`
SOURCE: https://webdriver.io/docs/selectors/

**`$$()` selector**
WDIO function สำหรับหา elements ทุกตัวที่ match — คืน array
SOURCE: https://webdriver.io/docs/selectors/

**service**
WDIO plugin ที่ทำงานก่อน/ระหว่าง/หลัง test run เช่น `@wdio/appium-service` ที่ manage Appium server
SOURCE: https://webdriver.io/docs/appium-service/

**singleton**
Design pattern ที่ class มีแค่ instance เดียว — WDIO Page Objects ทำโดย `export default new ClassName()`
SOURCE: https://webdriver.io/docs/pageobjects/

**`spec` reporter**
Built-in WDIO reporter ที่แสดง test results ใน terminal แบบ human-readable
SOURCE: https://webdriver.io/docs/spec-reporter/

**`specs`**
Key ใน `wdio.conf.js` ที่ระบุ pattern ของ test files เช่น `['./test/specs/**/*.js']`
SOURCE: https://webdriver.io/docs/configurationfile/

## T

**TypeScript**
Superset ของ JavaScript ที่เพิ่ม static type checking — ทำให้ IDE แสดง autocomplete และจับ error ก่อน runtime
SOURCE: https://www.typescriptlang.org/

## U

**UiSelector**
Android UIAutomator syntax สำหรับหา element — ใช้ใน WDIO เป็น `$('android=new UiSelector().resourceId("...")')`
SOURCE: https://webdriver.io/docs/selectors/

## V

**`setValue()`**
WDIO method สำหรับ clear แล้วพิมพ์ text ใน input field — เทียบกับ `Input Text` ใน RF
SOURCE: https://webdriver.io/docs/api/element/setValue/

## W

**`waitForDisplayed()`**
WDIO method สำหรับรอให้ element ปรากฏบนหน้าจอ — `{ timeout: 15000 }` ระบุ max wait time, `{ reverse: true }` รอให้หายไป
SOURCE: https://webdriver.io/docs/api/element/waitForDisplayed/

**`waitForEnabled()`**
WDIO method สำหรับรอให้ element ใช้งานได้ (enabled)
SOURCE: https://webdriver.io/docs/api/element/waitForEnabled/

**WebdriverIO (WDIO)**
JavaScript test automation framework สำหรับ web และ mobile ที่ใช้ W3C WebDriver protocol — รันบน Node.js
SOURCE: https://webdriver.io/

**`wdio.conf.js`**
Config file หลักของ WDIO project — รวม framework, reporter, capabilities, services, spec paths ทุกอย่าง
SOURCE: https://webdriver.io/docs/configurationfile/

**W3C WebDriver Protocol**
Standard HTTP protocol ที่ทั้ง WDIO และ RF ใช้คุยกับ Appium server — เหตุผลที่ทั้งสองใช้ Appium server ตัวเดียวกันได้
SOURCE: https://w3c.github.io/webdriver/

---

## ข้ามไปยัง Series 1 (RF)

คำศัพท์ด้านล่างนี้มีใน Series 1 แต่ไม่ได้ใช้ใน Series 2 นี้ — ดูรายละเอียดใน [RF Glossary](../rf-mobile-testing/glossary.md)

| คำศัพท์ WDIO (Series 2) | คำศัพท์ RF เทียบเท่า (Series 1) |
|------------------------|----------------------------------|
| `$('~btn')` | `accessibility_id=btn` |
| `$('android=new UiSelector()...')` | `id=com.app:id/btn` |
| capabilities ใน `wdio.conf.js` | `Open Application` keyword |
| `$('~el').waitForDisplayed()` | `Wait Until Element Is Visible` |
| `$('~el').click()` | `Click Element` |
| `$('~el').setValue()` | `Input Text` |
| `$('~el').getText()` | `Get Text` |
| `driver.execute('mobile: swipe', {...})` | `Swipe` |
| `driver.hideKeyboard()` | `Hide Keyboard` |
| `describe / it` | `*** Test Cases ***` |
| `before / after` | `Suite Setup / Suite Teardown` |
| `beforeEach / afterEach` | `Test Setup / Test Teardown` |
