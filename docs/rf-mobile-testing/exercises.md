# แบบฝึกหัด: RF + AppiumLibrary Mobile Testing

> **คำแนะนำ:** ลองทำทุกข้อด้วยตัวเองก่อน แล้วค่อยดูเฉลย — ประโยชน์ของ exercise อยู่ที่กระบวนการคิด ไม่ใช่คำตอบ

---

## บทที่ 1: Mobile Testing Landscape

### Recall (ระดับ Beginner)

**ข้อ 1.1:** อธิบายด้วยคำของคุณเองว่า Appium ทำงานยังไง ไม่ต้องใช้ศัพท์เทคนิค — อธิบายให้ designer ในทีมเข้าใจได้ภายใน 5 ประโยค

**ข้อ 1.2:** วาด architecture diagram อย่างง่าย (แม้แต่ในกระดาษ) แสดงว่า Robot Framework, AppiumLibrary, Appium Server, UIAutomator2, และ Android emulator เชื่อมกันยังไง และ HTTP request เดินทางยังไง

### Application (ระดับ Intermediate)

**ข้อ 1.3:** ทีมคุณมี web app และ mobile app ของระบบธนาคาร หัวหน้าถามว่า "เราจะ reuse test ของ web ไปใช้กับ mobile ได้ไหม?" — คุณจะตอบว่าอะไร? ระบุว่าส่วนไหนเหมือนกัน ส่วนไหนต่างกัน

### Synthesis (ระดับ Advanced)

**ข้อ 1.4:** ทีม QA ใหม่มาถามว่า "เราควรเลือก Appium หรือ native test framework ของ platform (Espresso สำหรับ Android, XCUITest สำหรับ iOS)?" — ออกแบบ decision framework ว่าจะเลือกอะไรเมื่อไหร่ โดยคำนึงถึง: team skills, cross-platform testing needs, CI/CD integration, และ test maintenance cost

---

## บทที่ 2: Setup Environment

### Recall (ระดับ Beginner)

**ข้อ 2.1:** ถ้าทำ checklist setup ใหม่ — รายการ verify command ที่คุณต้องรัน 8 อย่างมีอะไรบ้าง? (ห้ามดูเอกสาร — จำเอง)

**ข้อ 2.2:** `appium-doctor --android` report ว่า `✘ ANDROID_HOME is not set` — คุณต้องทำอะไรบ้างเพื่อแก้ปัญหานี้? (step by step)

### Application (ระดับ Intermediate)

**ข้อ 2.3:** เพื่อนที่ทำงานใช้ Windows และติดตั้งครบแล้ว แต่รัน `adb devices` ไม่เห็น emulator ทั้งที่เปิด Android Studio และ emulator ขึ้นมาแล้ว — คุณจะ troubleshoot ยังไง? ลิสต์ขั้นตอนการตรวจสอบตามลำดับความน่าจะเป็น

### Synthesis (ระดับ Advanced)

**ข้อ 2.4:** ทีมต้องการรัน mobile test บน CI/CD pipeline (เช่น GitHub Actions) — ออกแบบ infrastructure plan ว่าจะ setup emulator ใน CI environment ยังไง มีปัญหาอะไรที่ต้องระวัง และจะ handle ยังไง

---

## บทที่ 3: Appium Capabilities

### Recall (ระดับ Beginner)

**ข้อ 3.1:** หลังจากรัน `adb shell dumpsys window | grep mCurrentFocus` ได้ output นี้:
```
mCurrentFocus=Window{... com.myshop.app/com.myshop.app.screens.home.HomeFragment}
```
คุณจะเขียน `appPackage` และ `appActivity` ใน capabilities ยังไง?

**ข้อ 3.2:** ต่างกันยังไงระหว่าง `appium:noReset=True` กับ `appium:fullReset=True`? ให้ยกตัวอย่าง scenario จริงที่ควรใช้แต่ละตัว

### Application (ระดับ Intermediate)

**ข้อ 3.3:** ทีมกำลังทดสอบ app ที่มี 2 environment: staging (`com.myapp.staging`) และ production (`com.myapp`). ออกแบบ variables structure ที่ทำให้รัน test กับทั้งสอง environment ได้โดยไม่ต้องแก้ code (hint: ใช้ command-line variable override)

### Synthesis (ระดับ Advanced)

**ข้อ 3.4:** app ทดสอบอยู่ดี ๆ test เริ่ม fail ด้วย error "Session not created: An unknown server-side error occurred" — วาง troubleshooting plan โดยระบุ: สาเหตุที่เป็นไปได้, วิธีตรวจสอบแต่ละสาเหตุ, และวิธีแก้

---

## บทที่ 4: AppiumLibrary Keywords

### Recall (ระดับ Beginner)

**ข้อ 4.1:** เขียน keyword `ทำการ Logout` สำหรับ app ที่มี:
- ปุ่ม Menu ที่ `accessibility_id=menu_button`
- Menu item Logout ที่ `accessibility_id=logout_item`
- Dialog confirmation ที่ `accessibility_id=confirm_logout_btn`
- หลัง logout จะกลับมา Login screen ที่ `accessibility_id=login_button`

### Application (ระดับ Intermediate)

**ข้อ 4.2:** สร้าง test case สำหรับ "แก้ไข profile" ที่:
1. Login แล้วไปหน้า Profile
2. กด Edit
3. เปลี่ยน Display Name
4. Save
5. ตรวจสอบว่า name อัปเดตแล้ว

โดยใช้แค่ keyword ที่เรียนมา ไม่ต้องมี locator จริง (ตั้งชื่อ accessibility_id เองที่สมเหตุสมผล)

### Synthesis (ระดับ Advanced)

**ข้อ 4.3:** test ของคุณ fail intermittently (บางครั้ง pass บางครั้ง fail โดยไม่มีเหตุผลชัดเจน) ที่ step `Click Element accessibility_id=submit_button` — วิเคราะห์สาเหตุที่เป็นไปได้อย่างน้อย 3 อย่าง และเสนอวิธีแก้สำหรับแต่ละอย่าง

---

## บทที่ 5: Mobile Locators

### Recall (ระดับ Beginner)

**ข้อ 5.1:** Appium Inspector แสดง element นี้:
```
content-desc: ""
resource-id: "com.shopapp:id/btn_add_to_cart"
class: "android.widget.Button"
text: "เพิ่มลงตะกร้า"
clickable: true
```
เรียงลำดับ locator ที่คุณจะใช้จากดีที่สุดไปแย่ที่สุด พร้อมเหตุผล

### Application (ระดับ Intermediate)

**ข้อ 5.2:** คุณต้องทดสอบ "คลิก notification ที่ 3 ในรายการ" บน notification center ที่มี element แบบนี้:
```
resource-id: com.app:id/notification_item  (ซ้ำกันทุก item)
text: แตกต่างกันแต่ละ item
```
เขียน keyword ที่ click notification ตาม index ที่รับมาเป็น argument

### Synthesis (ระดับ Advanced)

**ข้อ 5.3:** ทีมมี test suite 200 cases ที่ใช้ xpath เป็นหลัก รัน test suite ทั้งหมดใช้เวลา 4 ชั่วโมง หัวหน้าขอให้ลดเวลาลงเหลือ 2 ชั่วโมง — วางแผน migration strategy โดยไม่ต้องเขียน test ใหม่ทั้งหมด ระบุว่าจะ prioritize เปลี่ยน locator ไหนก่อน และวิธีวัดผลว่าดีขึ้น

---

## บทที่ 6: Gestures

### Recall (ระดับ Beginner)

**ข้อ 6.1:** เขียน `Swipe` command สำหรับ screen ขนาด 1080x2340 pixels เพื่อ:
a) Scroll down (เนื้อหาขยับขึ้น)
b) Scroll up (เนื้อหาขยับลง)
c) Swipe left (เปลี่ยนหน้าถัดไป)

### Application (ระดับ Intermediate)

**ข้อ 6.2:** เขียน keyword `Scroll ลงจนเจอ Element พร้อม timeout` ที่:
- รับ locator และ max_scroll_count เป็น argument
- Scroll ลงทีละครั้ง ตรวจสอบว่าเห็น element หรือยัง
- ถ้าหาได้ return สำเร็จ
- ถ้าครบ max_scroll_count แล้วยังไม่เจอ ให้ Fail พร้อม message ที่อธิบายชัด

### Synthesis (ระดับ Advanced)

**ข้อ 6.3:** app มี "infinite scroll list" ที่ load data เพิ่มเมื่อ scroll ถึงด้านล่าง — คุณต้องทดสอบว่า "item ที่ต้องการมีอยู่ใน list" โดยไม่รู้ว่า item อยู่ที่ position เท่าไหร่ ออกแบบ keyword ที่ handle กรณีนี้ได้ พร้อม edge cases: item ไม่มีใน list, list ว่างเปล่า, server error ระหว่าง scroll

---

## บทที่ 7: Page Object Model

### Recall (ระดับ Beginner)

**ข้อ 7.1:** สร้าง page file สำหรับ "Settings Screen" ที่มี:
- Toggle notification ที่ `accessibility_id=notif_toggle`
- Dropdown language ที่ `accessibility_id=language_dropdown`
- ปุ่ม Save ที่ `accessibility_id=save_settings_btn`
- Back button ที่ `accessibility_id=back_btn`

ให้มี variables และ keywords ที่เหมาะสม

### Application (ระดับ Intermediate)

**ข้อ 7.2:** เขียน test case สำหรับ flow นี้โดยใช้ Page Object pattern:
1. Login
2. ไปหน้า Settings
3. เปลี่ยน language เป็น English
4. Save
5. Logout
6. Login ใหม่ — ตรวจสอบว่า language ยังเป็น English

สร้าง page files ที่จำเป็น และ test file

### Synthesis (ระดับ Advanced)

**ข้อ 7.3:** ทีมมี test suite ที่ไม่ได้ใช้ Page Object — locators กระจายอยู่ใน test files 50 ไฟล์ Developer บอกว่า app จะ redesign ใน 2 เดือน ซึ่งจะเปลี่ยน locators เกือบทุกหน้า — เสนอ migration plan ที่ realistic สำหรับ refactor ภายใน 2 เดือน โดยไม่หยุด delivery test ใหม่

---

## เฉลย

<details>
<summary>เฉลยบทที่ 1</summary>

**1.1:** ตัวอย่างคำตอบ — "Appium เป็นเหมือนล่ามที่แปลคำสั่งจากโปรแกรม test ของเรา ให้ Android หรือ iOS เข้าใจ เราเขียน test ใน Robot Framework สั่งให้ 'กดปุ่ม Login' — Appium รับคำสั่งนั้นแล้วแปลงเป็นภาษาที่ Android เข้าใจ แล้ว Android ก็ทำการกดปุ่มจริงๆ บน emulator หรือ device"

**1.3:** ส่วนที่เหมือน: test structure (keyword-driven), assertion keywords, Wait keywords, Page Object pattern — ส่วนที่ต่าง: locator strategies (CSS/XPath web vs accessibility_id/resource-id mobile), library (SeleniumLibrary vs AppiumLibrary), setup complexity (Selenium เรียบง่ายกว่า), gesture interactions (mobile only)

**1.4:** เลือก Appium เมื่อ: ทีมเดียวต้องทดสอบทั้ง Android และ iOS, ต้องการ reuse test infrastructure กับ web testing, ทีมมี QA ที่รู้ Python/RF — เลือก Espresso/XCUITest เมื่อ: ต้องการ performance สูงสุด, ทดสอบ platform เดียว, ทีมเป็น developer ที่รู้ Java/Kotlin/Swift อยู่แล้ว

</details>

<details>
<summary>เฉลยบทที่ 2</summary>

**2.1:** Verify commands ที่ต้องรัน 8 อย่าง:
1. `java -version` — ตรวจ JDK
2. `echo $JAVA_HOME` — ตรวจ environment variable
3. `adb version` — ตรวจ Android Debug Bridge
4. `echo $ANDROID_HOME` — ตรวจ Android SDK path
5. `node --version` — ตรวจ Node.js
6. `appium --version` — ตรวจ Appium server
7. `appium driver list --installed` — ตรวจ UIAutomator2 driver
8. `adb devices` (หลังเปิด emulator) — ตรวจว่า device พร้อม

**2.2:** ขั้นตอนแก้ `ANDROID_HOME is not set`:
1. หา Android SDK path: ใน Android Studio → Settings → Android SDK → ดู SDK Location
2. เพิ่มใน `~/.zshrc` หรือ `~/.bashrc` (macOS/Linux):
   ```bash
   export ANDROID_HOME=$HOME/Library/Android/sdk
   export PATH=$PATH:$ANDROID_HOME/platform-tools
   ```
   Windows: เพิ่มใน System Environment Variables → `ANDROID_HOME` = `C:\Users\YourName\AppData\Local\Android\Sdk`
3. Restart terminal (environment variables ไม่ update แบบ real-time)
4. รัน `appium-doctor --android` ใหม่เพื่อยืนยัน

**2.3:** Troubleshoot adb ไม่เห็น emulator บน Windows (เรียงตามความน่าจะเป็น):
1. **ตรวจว่า emulator boot ครบ** — emulator ต้องแสดง Android home screen ก่อน adb จะเห็น
2. **Restart ADB server:**
   ```bash
   adb kill-server
   adb start-server
   adb devices
   ```
3. **ตรวจ ANDROID_HOME** — path ต้องไม่มี space และ restart terminal หลังตั้งค่า
4. **ตรวจว่า platform-tools ใน PATH** — `adb` ต้องอยู่ใน `%ANDROID_HOME%\platform-tools`
5. **BIOS virtualization** — ถ้า emulator เปิดได้แต่ช้ามากหรือ freeze ให้เปิด Intel VT-x / AMD-V ใน BIOS
6. **USB debugging** — ถ้าใช้ physical device ต้องเปิด Developer Options + USB debugging

**2.4:** Infrastructure plan สำหรับ CI/CD (เช่น GitHub Actions):
- ใช้ `reactivecircus/android-emulator-runner@v2` action ซึ่งจัดการ emulator lifecycle ใน CI
- ระวัง: emulator ใน CI ต้องใช้ `x86` / `x86_64` image ไม่ใช่ ARM เพราะ CI servers ส่วนใหญ่เป็น x86
- ปัญหาที่พบบ่อย: (1) emulator boot timeout — เพิ่ม `emulator-boot-timeout` ถึง 300s (2) QEMU hardware acceleration — GitHub Actions รองรับ KVM บน Ubuntu (3) headless mode — ต้องตั้ง display เช่น `DISPLAY=:99`

```yaml
# .github/workflows/mobile-test.yml
- uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 33
    arch: x86_64
    script: |
      appium &
      sleep 5
      robot tests/
```

</details>

<details>
<summary>เฉลยบทที่ 3</summary>

**3.1:**
```
appPackage=com.myshop.app
appActivity=.screens.home.HomeFragment
```

**3.3:**
```robotframework
# variables/staging.robot
${APP_PACKAGE}    com.myapp.staging
${APP_ENV}        staging

# variables/production.robot
${APP_PACKAGE}    com.myapp
${APP_ENV}        production
```
```bash
# รัน staging
robot --variablefile variables/staging.robot tests/

# รัน production
robot --variablefile variables/production.robot tests/
```

</details>

<details>
<summary>เฉลยบทที่ 4</summary>

**4.1:**
```robotframework
ทำการ Logout
    Click Element    accessibility_id=menu_button
    Wait Until Element Is Visible    accessibility_id=logout_item    10s
    Click Element    accessibility_id=logout_item
    Wait Until Element Is Visible    accessibility_id=confirm_logout_btn    10s
    Click Element    accessibility_id=confirm_logout_btn
    Wait Until Element Is Visible    accessibility_id=login_button    15s
    Page Should Contain Element    accessibility_id=login_button
```

**4.2:**
```robotframework
*** Settings ***
Library    AppiumLibrary

*** Variables ***
${APPIUM}           http://localhost:4723
${PKG}              com.example.app
${ACT}              .MainActivity

*** Keywords ***
เปิดแอป
    Open Application    ${APPIUM}
    ...    platformName=Android
    ...    appium:automationName=UIAutomator2
    ...    appium:deviceName=emulator-5554
    ...    appium:appPackage=${PKG}
    ...    appium:appActivity=${ACT}
    ...    appium:noReset=True

*** Test Cases ***
แก้ไข Profile Display Name สำเร็จ
    [Setup]    เปิดแอป
    # Login ก่อน
    Wait Until Element Is Visible    accessibility_id=username_input    15s
    Input Text    accessibility_id=username_input    test_user
    Input Text    accessibility_id=password_input    test_pass
    Hide Keyboard
    Click Element    accessibility_id=login_button
    # ไปหน้า Profile
    Wait Until Element Is Visible    accessibility_id=profile_tab    15s
    Click Element    accessibility_id=profile_tab
    Wait Until Element Is Visible    accessibility_id=edit_button    10s
    Click Element    accessibility_id=edit_button
    # เปลี่ยน Display Name
    Wait Until Element Is Visible    accessibility_id=display_name_field    10s
    Clear Text    accessibility_id=display_name_field
    Input Text    accessibility_id=display_name_field    My New Name
    Hide Keyboard
    # Save
    Click Element    accessibility_id=save_button
    # ตรวจสอบ
    Wait Until Element Is Visible    accessibility_id=profile_name    10s
    ${name}=    Get Text    accessibility_id=profile_name
    Should Be Equal    ${name}    My New Name
    [Teardown]    Close Application
```

**4.3:** สาเหตุ intermittent fail ที่ `Click Element accessibility_id=submit_button`:
1. **Timing issue** — element visible แต่ยังไม่ ready (เช่น disabled ชั่วคราวระหว่าง animation)
   → แก้: ใช้ `Wait Until Element Is Enabled` ก่อน click แทน `Wait Until Element Is Visible`
2. **Keyboard บัง element** — submit button อยู่ใต้ keyboard ที่เปิดอยู่
   → แก้: เพิ่ม `Hide Keyboard` ก่อน click submit
3. **Element ยังไม่ load** (race condition กับ API response) — form validation ทำงานช้า
   → แก้: เพิ่ม wait ที่ยาวขึ้น หรือรอ loading spinner หายก่อน

</details>

<details>
<summary>เฉลยบทที่ 5</summary>

**5.1:** ลำดับ: (1) `id=com.shopapp:id/btn_add_to_cart` — resource-id unique ดีที่สุดเมื่อ content-desc ว่าง (2) `xpath=//android.widget.Button[@text='เพิ่มลงตะกร้า']` — fallback ใช้ text (3) `class=android.widget.Button` — ไม่แนะนำ เพราะอาจมีหลายปุ่มบนหน้า

**5.2:**
```robotframework
คลิก Notification ตาม Index
    [Arguments]    ${index}
    ${items}=    Get WebElements    id=com.app:id/notification_item
    ${count}=    Get Length    ${items}
    Should Be True    ${index} < ${count}    มีแค่ ${count} items ไม่มี index ${index}
    Click Element    ${items}[${index}]
```

</details>

<details>
<summary>เฉลยบทที่ 6</summary>

**6.1:** screen ขนาด 1080x2340 pixels:
```robotframework
# a) Scroll down (เนื้อหาขยับขึ้น) — swipe from bottom to top
Swipe    540    1800    540    500    800

# b) Scroll up (เนื้อหาขยับลง) — swipe from top to bottom
Swipe    540    500    540    1800    800

# c) Swipe left (เปลี่ยนหน้าถัดไป) — swipe from right to left
Swipe    900    1170    180    1170    600
```
หลักการ: center X = 540 (1080/2), center Y = 1170 (2340/2) — ใช้ ~80% ของ screen height เป็น distance

**6.2:**
```robotframework
Scroll ลงจนเจอ Element พร้อม timeout
    [Arguments]    ${locator}    ${max_scroll_count}=5
    FOR    ${i}    IN RANGE    ${max_scroll_count}
        ${found}=    Run Keyword And Return Status
        ...    Element Should Be Visible    ${locator}
        IF    ${found}    RETURN
        Swipe    540    1600    540    600    800
    END
    Fail
    ...    ไม่พบ element '${locator}' หลังจาก scroll ${max_scroll_count} ครั้ง
```

**6.3:**
```robotframework
หา Item ใน Infinite Scroll List
    [Arguments]    ${item_text}
    ${found}=    Set Variable    ${False}
    ${prev_last_item}=    Set Variable    ${EMPTY}

    FOR    ${attempt}    IN RANGE    20
        # ตรวจ empty list
        ${is_empty}=    Run Keyword And Return Status
        ...    Page Should Contain Element    accessibility_id=empty_list_message
        IF    ${is_empty}
            Fail    List ว่างเปล่า — ไม่มี item ใดเลย
        END

        # ตรวจ server error
        ${has_error}=    Run Keyword And Return Status
        ...    Page Should Contain Element    accessibility_id=error_banner
        IF    ${has_error}
            Fail    Server error ระหว่าง scroll — ไม่สามารถดึงข้อมูลเพิ่มได้
        END

        # หา item ที่ต้องการ
        ${item_found}=    Run Keyword And Return Status
        ...    Page Should Contain Text    ${item_text}
        IF    ${item_found}
            RETURN
        END

        # ตรวจว่าถึง end of list หรือยัง (last item ไม่เปลี่ยน)
        ${items}=    Get WebElements    accessibility_id=list_item
        ${last_item_text}=    Get Text    ${items}[-1]
        IF    '${last_item_text}' == '${prev_last_item}'
            Fail    ถึง end of list แล้ว — ไม่พบ '${item_text}' ใน list
        END
        ${prev_last_item}=    Set Variable    ${last_item_text}

        # scroll ลงต่อ
        Swipe    540    1600    540    600    800
        Sleep    1s    # รอ new items load
    END
    Fail    ไม่พบ '${item_text}' หลังจาก scroll 20 ครั้ง
```

</details>

<details>
<summary>เฉลยบทที่ 7</summary>

**7.1:**
```robotframework
# pages/settings_page.robot

*** Variables ***
${SETTINGS_NOTIF_TOGGLE}     accessibility_id=notif_toggle
${SETTINGS_LANGUAGE_DD}      accessibility_id=language_dropdown
${SETTINGS_SAVE_BTN}         accessibility_id=save_settings_btn
${SETTINGS_BACK_BTN}         accessibility_id=back_btn

*** Keywords ***
เปิด-ปิด Notification
    Click Element    ${SETTINGS_NOTIF_TOGGLE}

เลือก Language
    [Arguments]    ${language}
    Click Element    ${SETTINGS_LANGUAGE_DD}
    Wait Until Element Is Visible    accessibility_id=language_option_${language}    5s
    Click Element    accessibility_id=language_option_${language}

บันทึก Settings
    Click Element    ${SETTINGS_SAVE_BTN}
    Wait Until Element Is Not Visible    ${SETTINGS_SAVE_BTN}    5s

กลับหน้าก่อนหน้า
    Click Element    ${SETTINGS_BACK_BTN}
```

</details>
