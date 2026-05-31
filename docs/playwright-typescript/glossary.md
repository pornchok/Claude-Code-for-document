# Glossary — Playwright TypeScript Course

คำศัพท์ทุกคำเรียงตาม alphabetical order พร้อม SOURCE URL จาก playwright.dev ที่ verify แล้ว

---

## Accessibility Tree

**SOURCE:** https://playwright.dev/docs/api/class-page#page-get-by-role

โครงสร้างลำดับชั้นของ element บนหน้าเว็บในรูปแบบ semantic — แต่ละ element มี ARIA role, ARIA attribute, และ accessible name แทนที่จะเป็น HTML tag ดิบ Playwright ใช้ accessibility tree ผ่าน `getByRole()` เพื่อ locate element ตามวิธีที่ assistive technology (เช่น screen reader) มองเห็นหน้าเว็บ

ใช้เมื่อ: ต้องการเขียน test ที่สะท้อนประสบการณ์ผู้ใช้จริง และตรวจสอบว่า UI มี ARIA role ถูกต้อง

---

## Annotation

**SOURCE:** https://playwright.dev/docs/test-annotations

Metadata ที่แปะบน test เพื่อบอก test runner ว่าควรจัดการ test นั้นอย่างไร Playwright มี 4 built-in annotation: `skip` (ข้ามไม่รัน), `fail` (รันแต่คาดว่าต้อง fail), `fixme` (ข้ามเพราะรู้ว่า broken), `slow` (เพิ่ม timeout เป็น 3 เท่า) annotation สามารถระบุเงื่อนไขหรือใส่ข้อความอธิบายได้ และปรากฏในรายงาน

ใช้เมื่อ: ต้องการ mark test ที่ยังไม่พร้อม หรือ document สถานะของ test ในทีม

---

## Actionability

**SOURCE:** https://playwright.dev/docs/actionability

เงื่อนไขที่ element ต้องผ่านทั้งหมดก่อนที่ Playwright จะ execute action มี 5 เงื่อนไขหลัก: **Visible** (มี bounding box และไม่ hidden), **Stable** (ไม่กำลัง animate), **Enabled** (ไม่ disabled), **Editable** (ไม่ readonly สำหรับ input), **Receives Events** (ไม่มี overlay บัง) action แต่ละประเภทต้องการเงื่อนไขต่างกัน

---

## Auto-waiting

**SOURCE:** https://playwright.dev/docs/actionability

กลไกที่ Playwright หยุดรอจนกว่า element จะผ่านเงื่อนไข actionability ครบก่อนที่จะ execute action ใดๆ เช่น คลิก ไม่เกิดจนกว่า element จะ visible, stable, enabled, และรับ event ได้ ถ้า element ไม่ผ่านภายใน timeout จะ throw `TimeoutError`

ใช้เมื่อ: ทำงานโดยอัตโนมัติทุกครั้งที่ใช้ action — ไม่ต้อง config อะไรเพิ่ม

---

## baseURL

**SOURCE:** https://playwright.dev/docs/test-configuration

Config option ที่กำหนด URL พื้นฐานสำหรับ navigation ใน test เมื่อ set แล้ว สามารถใช้ `page.goto('/')` แทน `page.goto('http://localhost:3000/')` ได้ทันที กำหนดใน `playwright.config.ts` ภายใต้ `use.baseURL`

ใช้เมื่อ: ต้องการลด repetition ของ URL ใน test และทำให้ config environment ต่างๆ ง่ายขึ้น

---

## BrowserContext

**SOURCE:** https://playwright.dev/docs/browser-contexts

Session browser ที่ isolated อย่างสมบูรณ์ — มี cookies, localStorage, และข้อมูล session ของตัวเอง ไม่แชร์กับ context อื่น Playwright รัน test แต่ละอันใน BrowserContext แยกกัน เหมือน incognito window ใหม่ทุกครั้ง สร้างเร็วและมีต้นทุนต่ำ

ใช้เมื่อ: ต้องการ test หลาย user พร้อมกัน (เช่น chat) หรือ isolate auth state ระหว่าง test

---

## Codegen

**SOURCE:** https://playwright.dev/docs/codegen

Tool ของ Playwright ที่ record การกระทำของผู้ใช้ในเบราว์เซอร์แล้วสร้าง test code อัตโนมัติ เมื่อรัน `playwright codegen` จะเปิด 2 หน้าต่าง: เบราว์เซอร์สำหรับ interact และ Playwright Inspector สำหรับดู code ที่ generate ออกมา ระบบเลือก locator ที่ดีที่สุดให้อัตโนมัติ เช่น role, text, test ID

ใช้เมื่อ: ต้องการ bootstrap test ใหม่อย่างรวดเร็ว หรือสำรวจ locator ที่เหมาะสมสำหรับ element

---

## defineConfig()

**SOURCE:** https://playwright.dev/docs/test-configuration

TypeScript helper function สำหรับ wrap config object ใน `playwright.config.ts` ให้ได้รับ type checking และ IntelliSense จาก IDE เมื่อ wrap ด้วย `defineConfig()` ตัว editor จะช่วย autocomplete option และแจ้งเตือน error ใน config ก่อน runtime

ใช้เมื่อ: เขียน config เสมอ — เป็น best practice มาตรฐานสำหรับทุก Playwright project ที่ใช้ TypeScript

---

## Emulation

**SOURCE:** https://playwright.dev/docs/emulation

ความสามารถของ Playwright ในการจำลองสภาพแวดล้อมต่างๆ บน browser เดียวกัน ครอบคลุม: device (user agent, viewport, touch), locale, timezone, geolocation, permissions, color scheme, และ network offline Playwright มี device profiles สำเร็จรูปสำหรับอุปกรณ์ยอดนิยม

ใช้เมื่อ: ต้องการ test UX บน mobile, ตรวจสอบการแสดงผลตาม region/timezone หรือ test การขอ permission

---

## Fixture

**SOURCE:** https://playwright.dev/docs/test-fixtures

กลไก setup/teardown ของ Playwright Test ที่ทำงานแบบ dependency injection — แต่ละ fixture เตรียมสิ่งที่ test ต้องการ (เช่น authenticated page, database connection) แล้วทำ cleanup หลัง test จบ code ก่อน `await use()` คือ setup, หลัง `use()` คือ teardown fixture ถูก inject เข้า test โดยอัตโนมัติตามชื่อ parameter

ใช้เมื่อ: ต้องการ reuse setup logic ข้าม test หลายอัน หรือสร้าง test environment ที่ composable

---

## fullyParallel

**SOURCE:** https://playwright.dev/docs/test-parallel

Config option ที่เปิดให้ test ทุกอันในทุกไฟล์รัน parallel พร้อมกัน โดย default Playwright รัน test ในแต่ละไฟล์เป็น sequential แต่ระหว่างไฟล์ parallel เมื่อ set `fullyParallel: true` ทุก test ไม่ว่าจะอยู่ไฟล์ไหนจะรันใน worker process แยกกันทั้งหมด

ใช้เมื่อ: test suite ใหญ่และ test แต่ละอัน independent ต้องการลด total execution time สูงสุด

---

## HAR

**SOURCE:** https://playwright.dev/docs/mock#record-and-replay-requests

HTTP Archive — format ไฟล์ที่บันทึก network request และ response ทั้งหมดที่เกิดขึ้นขณะโหลดหน้า ครอบคลุม headers, cookies, response body, และ timing Playwright ใช้ `page.routeFromHAR()` เพื่อ record และ replay network traffic ทำให้ test ไม่ต้องเรียก API จริง

ใช้เมื่อ: ต้องการ mock API ที่ซับซ้อน, test offline scenarios, หรือทำให้ test reproducible โดยไม่พึ่ง external service

---

## Locator

**SOURCE:** https://playwright.dev/docs/locators

Reference ไปยัง element บนหน้าเว็บที่ "smart" — ทุกครั้งที่ใช้ Locator ทำ action, Playwright จะค้นหา element ใหม่จาก DOM ปัจจุบัน ไม่ยึดติดกับ reference เดิม จึง retry ได้อัตโนมัติเมื่อ element ยังไม่พร้อม Locator เป็นศูนย์กลางของ auto-waiting และ retry-ability ทั้งหมดใน Playwright

ใช้เมื่อ: ต้องการ interact หรือ assert กับ element บนหน้า — ควรใช้แทน ElementHandle เสมอ

---

## mergeTests()

**SOURCE:** https://playwright.dev/docs/test-fixtures#combining-custom-fixtures-from-multiple-modules

Function สำหรับรวม fixture จากหลาย module เข้าด้วยกันเป็น `test` object เดียว เช่น `mergeTests(dbTest, a11yTest)` ทำให้ test สามารถใช้ fixture จากทั้งสอง module ได้พร้อมกันโดยไม่ต้อง extend ซ้อนกัน

ใช้เมื่อ: มี fixture library หลายชุดแยกตาม domain (database, auth, accessibility) และต้องการ compose เข้าหากัน

---

## Page Object Model

**SOURCE:** https://playwright.dev/docs/pom

Design pattern สำหรับจัด test โดยสร้าง class แทนแต่ละหน้าหรือ component ของแอป — แต่ละ class รวม locator และ action ที่เกี่ยวข้องไว้ที่เดียว เมื่อ UI เปลี่ยน แก้ที่ class เดียวแทนที่จะแก้ทุก test ทำให้ test suite ขนาดใหญ่ maintain ได้ง่ายขึ้นมาก

ใช้เมื่อ: test suite มีขนาดใหญ่, มีหน้าที่ซับซ้อน หรือต้องการ reuse action ข้าม test หลายอัน

---

## Shadow DOM

**SOURCE:** https://playwright.dev/docs/locators#locate-in-shadow-dom

DOM tree ที่ encapsulate อยู่ภายใน web component — แยกจาก DOM หลักเพื่อป้องกัน style และ script ของ component รั่วออกไปข้างนอก Playwright locator ทุกตัวสามารถ pierce shadow root ได้โดยอัตโนมัติโดยไม่ต้อง config พิเศษ ยกเว้น XPath selector และ closed-mode shadow root

ใช้เมื่อ: test web component ที่ใช้ Shadow DOM — ใช้ locator ปกติได้เลย ไม่ต้องจัดการ shadow root เอง

---

## Shard

**SOURCE:** https://playwright.dev/docs/test-sharding

ส่วนแบ่งของ test suite ที่แยกออกมารันบน machine อิสระในขนาด `x/y` (เช่น `--shard=2/4` คือส่วนที่ 2 จาก 4 ส่วน) แต่ละ shard รัน subset ของ test ทั้งหมดแบบ parallel กับ shard อื่น ผลจากทุก shard merge รวมกันเป็น report เดียวได้

ใช้เมื่อ: test suite ใหญ่มากและต้องการลด CI execution time โดยกระจาย test ไปหลาย machine

---

## Storage State

**SOURCE:** https://playwright.dev/docs/auth

ข้อมูล session ของเบราว์เซอร์ที่ serialize แล้ว ครอบคลุม cookies, localStorage, และ IndexedDB ใช้สำหรับ save authenticated state หลังจาก login ครั้งเดียว แล้ว load state นั้นซ้ำใน test อื่นๆ โดยไม่ต้อง login ใหม่ทุกครั้ง

ใช้เมื่อ: test ส่วนใหญ่ต้อง login ก่อน — ช่วยลด test time อย่างมากโดยข้ามขั้นตอน authentication

---

## storageState

**SOURCE:** https://playwright.dev/docs/auth

Method และ config option คู่กัน: ใช้ `browserContext.storageState()` เพื่อ export state ออกเป็น JSON file และใช้ option `storageState: 'path/to/state.json'` ใน config หรือ `test.use()` เพื่อ load state กลับมาให้ BrowserContext ใหม่ เป็น implementation ของ Storage State pattern

ใช้เมื่อ: set up authenticated fixtures ที่ใช้ร่วมกันทั้ง test suite

---

## Strict Mode

**SOURCE:** https://playwright.dev/docs/locators#strictness

พฤติกรรมของ Locator ที่ throw exception ทันทีเมื่อ locator match element มากกว่า 1 อัน และ action นั้น assume ว่ามี target เดียว (เช่น `.click()`, `.fill()`) ป้องกันการ interact กับ element ผิดอันโดยบังเอิญ สามารถใช้ `.first()`, `.last()`, `.nth()` เพื่อเลือกระบุได้ แต่ควรหลีกเลี่ยงเพราะขึ้นกับลำดับ

ใช้เมื่อ: ทำงานโดยอัตโนมัติ — เป็น default behavior ที่ช่วยให้ test fail เร็วและชัดเจนเมื่อ locator ไม่ unique พอ

---

## Tag

**SOURCE:** https://playwright.dev/docs/test-annotations

Label ที่ขึ้นต้นด้วย `@` สำหรับจัดกลุ่ม test เพื่อให้ filter ได้จาก command line ใส่ได้ทั้งใน title ของ test โดยตรง หรือผ่าน details object ใช้ `--grep @tagname` เพื่อรันเฉพาะ test ที่มี tag นั้น และ `--grep-invert` เพื่อ exclude

ใช้เมื่อ: ต้องการรัน test subset เช่น `@smoke`, `@regression`, `@slow` แยกตาม context เช่น local vs CI

---

## Test Step

**SOURCE:** https://playwright.dev/docs/api/class-test#test-step

กลุ่มของ action ที่มีชื่อ สร้างด้วย `test.step('ชื่อ step', async () => { ... })` แต่ละ step ปรากฏเป็น entry แยกใน Trace Viewer และ HTML report ทำให้เห็นชัดว่า test ทำอะไรในแต่ละขั้น step ซ้อนกันได้ และเมื่อ fail จะระบุได้ว่า fail ใน step ไหน

ใช้เมื่อ: test มีหลาย phase ที่ซับซ้อน เช่น "Login", "Add to cart", "Checkout" ต้องการให้ report อ่านเข้าใจง่าย

---

## Trace

**SOURCE:** https://playwright.dev/docs/trace-viewer

ไฟล์ที่บันทึกทุกอย่างที่เกิดขึ้นระหว่าง test รัน ครอบคลุม DOM snapshot (ก่อน/ระหว่าง/หลัง action), screenshots, network request, console log, และ source code location เปิดดูได้ด้วย Trace Viewer ทั้งแบบ local และที่ trace.playwright.dev

ใช้เมื่อ: debug test ที่ fail บน CI โดย config `trace: 'on-first-retry'` เพื่อ record เฉพาะ test ที่ต้อง retry

---

## Web-first Assertions

**SOURCE:** https://playwright.dev/docs/test-assertions

Async matcher พิเศษของ Playwright ที่ retry การ check ซ้ำๆ จนกว่าเงื่อนไขจะผ่านหรือหมด timeout (default 5 วินาที) ต่างจาก assertion ปกติที่ check ครั้งเดียวแล้วตัดสิน เช่น `await expect(locator).toBeVisible()` จะรอจนกว่า element จะปรากฏ ไม่ fail ทันทีถ้ายังไม่เห็น

ใช้เมื่อ: assert สิ่งใดก็ตามบนหน้าเว็บ — ควรใช้แทน assertion ปกติเสมอเพื่อลด flakiness

---

## Worker

**SOURCE:** https://playwright.dev/docs/test-parallel

OS process ที่รัน test อิสระจาก process อื่น — แต่ละ worker มี browser instance ของตัวเอง ไม่สามารถ communicate กับ worker อื่นได้ Playwright reuse worker เดิมรัน test ไฟล์อื่นต่อไปเมื่อเป็นไปได้ แต่จะ terminate worker ที่ fail เพื่อให้ test ถัดไปได้สภาพแวดล้อมสะอาด

ใช้เมื่อ: เข้าใจ model การ parallel — test ใน worker เดียวกันจะ sequential, ข้าม worker จะ parallel
