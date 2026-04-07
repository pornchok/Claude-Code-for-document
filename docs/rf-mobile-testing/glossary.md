# Glossary: RF + AppiumLibrary Mobile Testing

คำศัพท์ทั้งหมดที่ใช้ในคอร์สนี้ เรียงตามตัวอักษร

---

## A

**accessibility id**
Locator strategy ที่ใช้ `content-desc` attribute ของ Android element หรือ `accessibilityIdentifier` ของ iOS — แนะนำให้ใช้เป็นหลักเพราะเร็ว stable และ cross-platform
SOURCE: https://www.browserstack.com/guide/locators-in-appium

**Activity (Android)**
Component หนึ่งของ Android app ที่แสดง UI หนึ่งหน้าจอ เช่น `MainActivity`, `LoginActivity` — ใช้ใน `appActivity` capability
SOURCE: https://developer.android.com/guide/components/activities

**adb (Android Debug Bridge)**
Command-line tool ที่ใช้สื่อสารกับ Android device หรือ emulator ผ่าน Terminal เช่น `adb devices`, `adb install`
SOURCE: https://developer.android.com/tools/adb

**Android SDK (Software Development Kit)**
ชุดเครื่องมือที่ Google ให้มาสำหรับ develop และทดสอบ Android app — ต้องติดตั้งผ่าน Android Studio รวมถึง `adb`, emulator, build-tools
SOURCE: https://developer.android.com/tools

**Android Studio**
IDE หลักสำหรับ Android development — ในบริบทของ testing เราใช้มันเพื่อ download Android SDK และสร้าง emulator (AVD)
SOURCE: https://developer.android.com/studio

**appActivity**
Capability ของ Appium ที่ระบุชื่อ Activity ที่ต้องการเปิดใน app เช่น `.MainActivity` หรือ `.ui.login.LoginActivity`
SOURCE: https://appium.io/docs/en/2.0/guides/caps/

**Appium**
Open-source automation framework สำหรับ mobile app — รันเป็น server รับ HTTP requests แล้วส่งคำสั่งไปยัง device ผ่าน driver
SOURCE: https://appium.io/docs/en/2.0/intro/

**Appium Inspector**
GUI tool สำหรับ inspect mobile app UI elements เพื่อหา locators — เหมือน Chrome DevTools แต่สำหรับ mobile
SOURCE: https://github.com/appium/appium-inspector

**appium: prefix**
Convention ใน Appium 2.x ที่ capabilities เฉพาะ Appium ต้องมี prefix `appium:` ตาม W3C WebDriver standard เช่น `appium:automationName`
SOURCE: https://appium.io/docs/en/2.0/guides/caps/

**AppiumLibrary**
Robot Framework library สำหรับ mobile testing — เป็น client ที่ส่ง commands ไปหา Appium server
package: `robotframework-appiumlibrary`
SOURCE: https://github.com/serhatbolsu/robotframework-appiumlibrary

**appPackage**
Capability ของ Appium ที่ระบุ Android package name ของ app เช่น `com.example.myapp`
SOURCE: https://appium.io/docs/en/2.0/guides/caps/

**automationName**
Capability ที่ระบุว่าจะใช้ driver ไหน สำหรับ Android ใช้ `UIAutomator2`, สำหรับ iOS ใช้ `XCUITest`
SOURCE: https://appium.io/docs/en/2.0/guides/caps/

**AVD (Android Virtual Device)**
Android emulator ที่สร้างด้วย Android Studio หรือ AVD Manager — ใช้สำหรับทดสอบโดยไม่ต้องใช้ device จริง
SOURCE: https://developer.android.com/studio/run/managing-avds

## C

**Capabilities**
Set of parameters ที่ใช้ start Appium session บอก Appium ว่าจะทำอะไร กับ app ไหน บน platform ไหน
SOURCE: https://appium.io/docs/en/2.0/guides/caps/

**content-desc (contentDescription)**
Android attribute ที่ developer ตั้งไว้เพื่อ accessibility — AppiumLibrary ใช้เป็น `accessibility_id` locator
SOURCE: https://developer.android.com/reference/android/view/View#setContentDescription(java.lang.CharSequence)

## D

**deviceName**
Capability ที่ระบุชื่อหรือ serial ของ device/emulator เช่น `emulator-5554`
SOURCE: https://appium.io/docs/en/2.0/guides/caps/

**Driver (Appium)**
Pluggable module ที่ให้ Appium ควบคุม platform หนึ่งๆ เช่น UIAutomator2 สำหรับ Android, XCUITest สำหรับ iOS
SOURCE: https://appium.io/docs/en/2.0/intro/

## E

**Emulator**
ดู AVD

## F

**fullReset**
Capability ที่สั่งให้ uninstall แล้ว reinstall app ก่อนรัน test — ได้ clean state สมบูรณ์
SOURCE: https://appium.io/docs/en/2.0/guides/caps/

## H

**Hybrid App**
Mobile app ที่มีทั้ง native components และ WebView (web content) ผสมกัน
SOURCE: https://appium.io/docs/en/2.0/

## J

**JAVA_HOME**
Environment variable ที่ชี้ไปยัง JDK directory — required สำหรับ Android tools
SOURCE: https://appium.io/docs/en/2.0/quickstart/uiauto2-driver/

## L

**locator**
วิธีระบุ element ใน UI เช่น `accessibility_id=login_btn`, `id=com.app:id/btn`
SOURCE: https://serhatbolsu.github.io/robotframework-appiumlibrary/AppiumLibrary.html

**Long Press**
AppiumLibrary keyword สำหรับกดค้าง element นานกว่า tap ปกติ — ใช้สำหรับ context menu หรือ drag
SOURCE: https://serhatbolsu.github.io/robotframework-appiumlibrary/AppiumLibrary.html

## M

**Mobile Web App**
Website ที่เปิดใน mobile browser (Chrome on Android, Safari on iOS) — ใช้ CSS locators ได้เหมือน web
SOURCE: https://appium.io/docs/en/2.0/

## N

**Native App**
Mobile app ที่เขียนด้วย Android (Java/Kotlin) หรือ iOS (Swift/ObjC) native code — ใช้ native UI components ไม่มี HTML
SOURCE: https://appium.io/docs/en/2.0/

**noReset**
Capability ที่สั่งให้ไม่ clear app data ก่อนรัน test — ใช้เพื่อรักษา login state หรือ app settings
SOURCE: https://appium.io/docs/en/2.0/guides/caps/

## P

**Page Object Model (POM)**
Design pattern ที่แยก locators และ UI interactions ออกจาก test cases — เพื่อให้ maintain ง่ายเมื่อ UI เปลี่ยน
SOURCE: Robot Framework Best Practices

**platformName**
Capability ที่ระบุ OS platform เช่น `Android` หรือ `iOS` — เป็น W3C standard capability ไม่ต้องมี `appium:` prefix
SOURCE: https://appium.io/docs/en/2.0/guides/caps/

**platformVersion**
Capability ที่ระบุ OS version เช่น `13` สำหรับ Android 13
SOURCE: https://appium.io/docs/en/2.0/guides/caps/

## R

**resource-id**
Android attribute ที่ developer กำหนดใน layout XML เช่น `@+id/btn_login` — ใน locator ใช้เป็น `id=com.app:id/btn_login`
SOURCE: https://developer.android.com/guide/topics/resources/providing-resources

## S

**Scroll**
การเลื่อนหน้าจอเพื่อดูเนื้อหาที่อยู่นอก viewport — ใน AppiumLibrary ทำด้วย `Swipe` keyword หรือ `Scroll Element Into View`
SOURCE: https://serhatbolsu.github.io/robotframework-appiumlibrary/AppiumLibrary.html

**Session (Appium)**
การเชื่อมต่อระหว่าง Appium client กับ device — เริ่มด้วย `Open Application` สิ้นสุดด้วย `Close Application`
SOURCE: https://appium.io/docs/en/2.0/guides/caps/

**Swipe**
AppiumLibrary keyword สำหรับปัดหน้าจอ — รับ coordinates ต้นทาง ปลายทาง และ duration
SOURCE: https://serhatbolsu.github.io/robotframework-appiumlibrary/AppiumLibrary.html

## U

**UIAutomator2**
Appium driver สำหรับ Android automation — ต้องติดตั้งแยกด้วย `appium driver install uiautomator2`
SOURCE: https://appium.io/docs/en/2.0/quickstart/uiauto2-driver/

## W

**W3C WebDriver Protocol**
Standard protocol ที่ Selenium และ Appium ใช้ร่วมกันในการส่งคำสั่ง automation ผ่าน HTTP — เหตุผลที่ keyword ใน SeleniumLibrary และ AppiumLibrary หลายตัวเหมือนกัน
SOURCE: https://w3c.github.io/webdriver/

**WebDriver**
ดู W3C WebDriver Protocol

## X

**XCUITest**
Appium driver สำหรับ iOS automation — ต้อง macOS และ Xcode, ติดตั้งด้วย `appium driver install xcuitest`
SOURCE: https://appium.io/docs/en/2.0/

**xpath**
Locator strategy ที่ใช้ XML Path expression เพื่อหา element — ยืดหยุ่นที่สุดแต่ช้าที่สุด ใช้เป็น fallback
SOURCE: https://www.browserstack.com/guide/locators-in-appium
