# Glossary — Robot Framework

คำศัพท์ทั้งหมดที่ใช้ใน course นี้ เรียงตามตัวอักษร

---

| คำศัพท์ | คำอธิบายภาษาไทย | SOURCE |
|---------|-----------------|--------|
| **Acceptance Testing** | การทดสอบว่าระบบทำงานตาม business requirement ที่ตกลงกันไว้ — RF ถูกออกแบบมาสำหรับ acceptance testing โดยเฉพาะ | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **Argument** | ค่าที่ส่งให้ Keyword เมื่อเรียกใช้ เช่น `Login As    admin    pass123` → `admin` และ `pass123` คือ arguments | [RF User Guide - Keywords](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#keyword-arguments) |
| **Browser Library** | Library สำหรับ web testing ที่ใช้ Playwright เบื้องหลัง เร็วกว่าและรองรับ modern web app ดีกว่า SeleniumLibrary — แนะนำสำหรับโปรเจคใหม่ | [Browser Library GitHub](https://github.com/MarketSquare/robotframework-browser) |
| **BuiltIn Library** | Library ที่ RF import ให้อัตโนมัติทุกครั้ง มี keyword พื้นฐานเช่น `Log`, `Should Be Equal`, `Set Variable`, `Run Keyword And Return Status` | [BuiltIn Library Docs](https://robotframework.org/robotframework/latest/libraries/BuiltIn.html) |
| **Collections Library** | Library สำหรับจัดการ List, Dictionary เช่น `Get From List`, `Get From Dictionary`, `Append To List` ต้องสั่ง import เอง | [Collections Library Docs](https://robotframework.org/robotframework/latest/libraries/Collections.html) |
| **CSS Selector** | วิธีระบุ element บนหน้า web โดยใช้ CSS syntax เช่น `css:button.primary` หรือ `css:input[name='email']` | [MDN CSS Selectors](https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Selectors) |
| **Data-Driven Testing** | แนวทางเขียน test ที่แยก logic ออกจาก data — ใช้ Test Template รัน logic เดิมกับ data หลายชุด | [RF User Guide - Data-Driven](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#data-driven-style) |
| **Dictionary Variable** | Variable แบบ `&{name}` เก็บข้อมูลแบบ key-value เช่น `&{USER}    username=admin    role=admin` | [RF User Guide - Variables](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#variable-types) |
| **FOR Loop** | การทำซ้ำ keyword สำหรับแต่ละ item ใน list ใช้ syntax `FOR    ${item}    IN    @{list}` ... `END` | [RF User Guide - FOR Loops](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#for-loops) |
| **Headless Mode** | การรัน browser โดยไม่แสดง UI ให้ผู้ใช้เห็น เร็วกว่าและเหมาะสำหรับ CI/CD ตั้งด้วย `headless=True` | [Browser Library Docs](https://marketsquare.github.io/robotframework-browser/Browser.html) |
| **Keyword** | หน่วยพื้นฐานของ RF — ชื่อที่แทน action หนึ่งอย่าง เช่น `Click Login Button` หรือ `Verify Welcome Message` ซ่อน implementation ไว้ข้างใน | [RF User Guide - Keywords](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#user-keywords) |
| **Keyword-Driven Testing** | แนวทาง test ที่ใช้ Keyword เป็นหน่วยหลัก — QA เขียน test case โดยเรียง keyword โดยไม่ต้องรู้ implementation | [RF User Guide](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html) |
| **Library** | ชุด keyword สำเร็จรูปที่ import มาใช้ เช่น `Browser`, `Collections`, `OperatingSystem` บาง library ต้อง pip install ก่อน | [RF User Guide - Libraries](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#using-test-libraries) |
| **List Variable** | Variable แบบ `@{name}` เก็บข้อมูลหลายค่า เช่น `@{BROWSERS}    chromium    firefox    webkit` | [RF User Guide - Variables](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#variable-types) |
| **Locator** | วิธีบอกให้ RF หา element บนหน้า web เช่น `id:username`, `css:button.primary`, `xpath=//button[@type='submit']` | [Browser Library - Selectors](https://playwright.dev/python/docs/selectors) |
| **log.html** | ไฟล์ที่ RF สร้างหลังรัน test แสดงรายละเอียดทุก step ที่รัน ใช้สำหรับ debug เมื่อ test fail | [RF User Guide - Output](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#output-file) |
| **Page Object Pattern** | Pattern การจัด code ที่แยก keyword ของแต่ละหน้า web ออกเป็น resource file ของตัวเอง ช่วยให้แก้ locator ที่เดียว | [Page Object Best Practices](https://docs.robotframework.org/docs/style_guide) |
| **Playwright** | Browser automation library จาก Microsoft ที่ Browser Library ใช้เบื้องหลัง รองรับ Chromium, Firefox, WebKit | [Playwright Docs](https://playwright.dev) |
| **report.html** | ไฟล์สรุปผล test ที่ RF สร้าง แสดง pass/fail summary, tag statistics, suite breakdown | [RF User Guide - Report](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#report-file) |
| **Resource File** | ไฟล์ `.resource` ที่เก็บ keyword และ variable รวมกันเพื่อ share ระหว่าง test file หลายๆ อัน | [RF User Guide - Resource Files](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#resource-files) |
| **RETURN** | keyword สำหรับส่งค่าออกจาก keyword (RF 5+ ใช้ `RETURN` uppercase) เช่น `RETURN    ${result}` | [RF User Guide - Return Values](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#return-statement) |
| **Robot Framework** | Open-source framework สำหรับ test automation ที่ใช้ keyword-driven approach สร้างด้วย Python รองรับ web, API, database, mobile | [robotframework.org](https://robotframework.org) |
| **Scalar Variable** | Variable แบบ `${name}` เก็บค่าเดี่ยว เช่น `${USERNAME}    admin` หรือ `${TIMEOUT}    10 seconds` | [RF User Guide - Variables](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#variable-types) |
| **SeleniumLibrary** | Library สำหรับ web testing ที่ใช้ Selenium WebDriver เบื้องหลัง — ยังใช้งานได้ดีแต่ Browser Library แนะนำมากกว่าสำหรับโปรเจคใหม่ | [SeleniumLibrary GitHub](https://github.com/robotframework/SeleniumLibrary) |
| **Settings Section** | Section ใน `.robot` file ที่ใช้ import library, resource file, และกำหนด setup/teardown | [RF User Guide - Settings](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#setting-section) |
| **Suite Setup / Teardown** | Keyword ที่รันก่อน/หลัง test ทุก case ใน file/suite ใช้กำหนดใน Settings section | [RF User Guide - Setup and Teardown](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#suite-setup-and-teardown) |
| **Tag** | label ที่ติดกับ test case ใช้กรอง test เมื่อรัน เช่น `--include smoke` หรือ `--exclude slow` | [RF User Guide - Tags](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#tagging-test-cases) |
| **Teardown** | Keyword ที่รันหลัง test case เสร็จ (ทั้ง pass และ fail) มักใช้ปิด browser, cleanup ข้อมูล | [RF User Guide - Teardown](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#test-teardown) |
| **Test Case** | unit ของการทดสอบ 1 scenario ประกอบด้วยลำดับ keyword เช่น "User Can Login With Valid Credentials" | [RF User Guide - Test Cases](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#test-case-syntax) |
| **Test Setup** | Keyword ที่รันก่อนแต่ละ test case ใช้เตรียม state เช่น เปิด browser, login ก่อน | [RF User Guide - Setup](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#test-setup) |
| **Test Template** | กลไก Data-Driven Testing ที่กำหนด keyword 1 อัน แล้วรัน data หลายชุดแทน test case หลายอัน | [RF User Guide - Templates](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#test-templates) |
| **Variable** | ค่าที่เก็บไว้ใช้ซ้ำ มี 3 แบบหลัก: `${scalar}`, `@{list}`, `&{dict}` | [RF User Guide - Variables](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#variables) |
| **Variables Section** | Section ใน `.robot` file ที่ประกาศ variable ที่ใช้ในไฟล์นั้น | [RF User Guide - Variables](https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#variable-section) |
| **Virtual Environment** | การแยก Python environment ของแต่ละโปรเจคออกจากกัน ป้องกัน package version ชนกัน สร้างด้วย `python3 -m venv .venv` | [Python venv Docs](https://docs.python.org/3/library/venv.html) |
| **Wait For Elements State** | Keyword ของ Browser Library ที่รอจน element เป็น state ที่กำหนด (visible, hidden, enabled) ดีกว่า Sleep | [Browser Library Docs](https://marketsquare.github.io/robotframework-browser/Browser.html#Wait%20For%20Elements%20State) |
| **XPath** | ภาษา query สำหรับ navigate ใน XML/HTML document ใช้เป็น locator ได้ แต่มักเปราะบาง — แนะนำให้ใช้ id หรือ CSS แทนเมื่อทำได้ | [MDN XPath](https://developer.mozilla.org/en-US/docs/Web/XPath) |
