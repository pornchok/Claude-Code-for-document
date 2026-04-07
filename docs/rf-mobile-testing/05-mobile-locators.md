# บทที่ 5: Mobile Locators — หา Element บน Mobile Screen

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อน:

> **ทำไม CSS selector ที่ใช้ได้บน web browser ถึงใช้ไม่ได้กับ native mobile app?**

---

<details>
<summary>ดูเฉลย</summary>

CSS selector ทำงานกับ HTML DOM ซึ่งมีอยู่แค่ใน web browser เท่านั้น Native mobile app ไม่ได้ render HTML — มันใช้ native UI components ของ Android/iOS แทน ดังนั้น Appium ต้องใช้ locator strategies ที่เฉพาะกับ mobile OS

</details>

---

## วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะ:

- รู้จัก locator strategies ที่ใช้บน Android native app ได้ทั้ง 4 แบบ
- ใช้ Appium Inspector เพื่อหา locator ได้จริง
- เลือก locator strategy ที่เหมาะสมกับสถานการณ์ได้
- เข้าใจว่า locator ไหน fast และ locator ไหน fragile

---

## ทำไมต้องรู้? (Why)

Locator คือจุดที่ test fail บ่อยที่สุดในทุก automation project

บน web คุณรู้ CSS, XPath, ID อยู่แล้ว แต่บน mobile มี strategies ที่ต้องเรียนใหม่ เพราะ:
- ไม่มี HTML → ไม่มี CSS
- ชื่อ attribute ต่างออกไป (ไม่มี `id` attribute แบบ HTML)
- มี shortcut syntax พิเศษ เช่น `accessibility_id=`

การเลือก locator ที่ผิดทำให้ test เปราะบาง หรือช้าโดยไม่จำเป็น

---

## Analogy: Locator เหมือนที่อยู่บ้าน

ในการส่งพัสดุ คุณมีหลายวิธีระบุที่อยู่:
- **บ้านเลขที่** = `accessibility_id` → ชัดเจน, unique, เร็ว
- **GPS coordinates** = `resource-id` → แม่นยำมาก, แต่บางบ้านไม่มี
- **คำบรรยายทาง** = `xpath` → ยืดหยุ่นมาก แต่ยาวและเปลี่ยนง่าย
- **ชื่อคน** = `text` → ง่าย แต่ถ้าชื่อซ้ำกันหลายคนจะงง

> ⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
> - `accessibility_id` ทุกตัวมีค่าเสมอ → จริงๆ developer ต้องใส่ `contentDescription` ใน Android code ถึงจะมี ถ้าไม่ใส่จะว่างเปล่า
> - `resource-id` ต้อง unique → ใน list view อาจมีหลาย element ที่ใช้ resource-id เดียวกัน

---

## เนื้อหาหลัก

### Locator Strategies สำหรับ Android

#### 1. accessibility id (แนะนำอันดับ 1)

```robotframework
Click Element    accessibility_id=login_button
```

- ใช้ค่า `content-desc` ของ Android element
- ทำงานได้ทั้ง Android และ iOS (portable)
- เร็ว และ stable

> "Accessibility ID maps to content-desc in Android or accessibility-id in iOS, is recommended when IDs are not available."
> *(BrowserStack Appium Locators Guide)*

ข้อจำกัด: developer ต้องตั้ง `contentDescription` ในโค้ด ถ้าไม่ตั้งไว้จะหาไม่เจอ

#### 2. id (resource-id) — แนะนำอันดับ 2

```robotframework
Click Element    id=com.example.app:id/btn_login
```

- ใช้ `resource-id` ของ Android element
- Format: `[package_name]:id/[view_id]`

> "The ID Locator uses the element's resource-id in Android, and is fast, unique, and preferred wherever available."
> *(BrowserStack Appium Locators Guide)*

ข้อจำกัด: บาง element ไม่มี resource-id, ใน list อาจซ้ำกัน

#### 3. xpath — ใช้เป็น fallback

```robotframework
Click Element    xpath=//android.widget.Button[@text='Login']
Click Element    xpath=//*[@content-desc='login_button']
```

- ยืดหยุ่นมากที่สุด — หา element ได้แม้ไม่มี id
- ช้ากว่า strategies อื่น

> "XPath scans the whole XML source tree of the application screen, has performance issues and is the slowest performing locator strategy."
> *(BrowserStack Appium Locators Guide)*

ใช้เมื่อ accessibility_id และ id ไม่มี

#### 4. class name

```robotframework
Click Element    class=android.widget.Button
```

- ใช้ UI component class
- ไม่แนะนำ เพราะ class เดียวกันมีหลาย element ในหน้าเดียว

### วิธีใช้ Appium Inspector หา Locator

1. **เปิด emulator** และ start Appium server
2. **เปิด Appium Inspector** (app ที่ดาวน์โหลดไว้)
3. ใส่ capabilities (เหมือนที่ใช้ใน RF)
4. กด **Start Session**
5. คลิก element บนหน้าจอที่ Inspector แสดง
6. Inspector จะแสดง attributes ของ element ในแถบขวา:
   - `content-desc` → ใช้เป็น `accessibility_id=`
   - `resource-id` → ใช้เป็น `id=`
   - `class` → ใช้เป็น `class=`

**ตัวอย่าง Inspector output:**
```
content-desc: login_button
resource-id: com.example.app:id/btn_login
class: android.widget.Button
text: เข้าสู่ระบบ
```

→ locator ที่ดีที่สุดคือ `accessibility_id=login_button`

### ลำดับการเลือก Locator

```
1. accessibility_id   → เร็ว, stable, cross-platform
2. id (resource-id)   → เร็ว, specific to app
3. xpath              → ช้า แต่ flexible, ใช้เป็น fallback
4. class name         → หลีกเลี่ยง เพราะไม่ specific
```

> "Generally, you should mostly likely use Accessibility ID and ID automation strategies; XPath is flexible as a fallback when no ID exists."
> *(LambdaTest Appium Locators Blog)*

### Prefix Syntax ใน AppiumLibrary

AppiumLibrary รับ locator ในรูปแบบ `strategy=value`:

| Strategy | Syntax ใน RF | ตัวอย่าง |
|----------|-------------|---------|
| accessibility id | `accessibility_id=value` | `accessibility_id=login_btn` |
| resource-id | `id=value` | `id=com.app:id/btn_login` |
| xpath | `xpath=expression` | `xpath=//android.widget.Button` |
| class | `class=class_name` | `class=android.widget.EditText` |

---

## ตัวอย่าง 3 ระดับ

### Beginner: อ่าน locator จาก Appium Inspector

สมมติ Inspector แสดง element นี้:
```
content-desc: username_field
resource-id: com.nimble.bank:id/et_username
class: android.widget.EditText
text: (empty)
```

คุณจะเลือก locator ไหน?

```robotframework
# ตัวเลือกที่ดี (เรียงตามความ prefer)
Input Text    accessibility_id=username_field    john@email.com   # ✅ แนะนำที่สุด
Input Text    id=com.nimble.bank:id/et_username    john@email.com # ✅ ดีเช่นกัน
Input Text    xpath=//android.widget.EditText[1]    john@email.com # ⚠️ เปราะบาง
```

### Intermediate: หา locator เมื่อ element ไม่มี content-desc

Developer ลืมใส่ `contentDescription` → `content-desc` ว่าง

ขั้นตอน:
1. ดู `resource-id` ใน Inspector → ถ้ามีใช้ `id=` ได้เลย
2. ถ้าไม่มี resource-id ด้วย → ใช้ xpath ตาม attribute อื่น

```robotframework
# element ที่ content-desc ว่าง, resource-id ก็ว่าง
# แต่มี text "ยืนยัน"
Click Element    xpath=//android.widget.Button[@text='ยืนยัน']

# หรือใช้ sibling/parent relationship
Click Element    xpath=//android.widget.TextView[@text='ยอดโอน']/../following-sibling::android.widget.Button
```

ระยะยาว: แจ้ง developer ให้เพิ่ม `contentDescription` — ทำให้ test stable ขึ้นมาก

### Advanced: Locator กับ dynamic list (เช่น transaction list)

Transaction list มีหลาย item ที่ใช้ resource-id เดียวกัน:

```
resource-id: com.nimble.bank:id/tv_transaction_amount
text: ฿500.00
---
resource-id: com.nimble.bank:id/tv_transaction_amount
text: ฿1,000.00
```

ถ้าใช้ `id=` ธรรมดา → จะ click item แรกเสมอ

```robotframework
# วิธีที่ 1: ใช้ xpath ระบุ text
Click Element    xpath=//*[@resource-id='com.nimble.bank:id/tv_transaction_amount'][@text='฿1,000.00']

# วิธีที่ 2: ใช้ index (ระวัง — fragile เพราะ order เปลี่ยนได้)
${items}=    Get WebElements    id=com.nimble.bank:id/tv_transaction_amount
Click Element    ${items}[1]    # index เริ่มที่ 0

# วิธีที่ 3 (ดีที่สุด): scroll หา element ตาม text
Scroll Element Into View    xpath=//*[@text='฿1,000.00']
Click Element               xpath=//*[@text='฿1,000.00']
```

---

## Common Mistakes

❌ **ใช้ xpath ที่ขึ้นอยู่กับ position ใน tree**
```
xpath=//android.view.ViewGroup[3]/android.widget.Button[1]
```
→ เมื่อ UI เปลี่ยนเล็กน้อย test fail ทันที
✅ **ใช้ attribute เช่น text หรือ content-desc ใน xpath**
```
xpath=//android.widget.Button[@text='Login']
```
*(source: BrowserStack Appium Locators Guide)*

---

❌ **ใช้ CSS selector กับ native app**
```
Click Element    css=#login-btn    # ❌ จะ throw error
```
✅ **Native app ไม่มี CSS — ใช้ accessibility_id หรือ resource-id แทน**
*(source: appium.io)*

---

❌ **hardcode resource-id โดยไม่ใส่ package name**
```
id=btn_login    # ❌ อาจหาไม่เจอ
```
✅ **ใส่ package name ครบ**
```
id=com.example.app:id/btn_login    # ✅ ถูกต้อง
```
*(source: AppiumLibrary documentation)*

---

## สรุปบท

ลองตอบก่อนดูเฉลย:

> **คำถาม 1:** Inspector แสดง element นี้: `content-desc=""`, `resource-id="com.bank:id/tv_balance"`, `text="฿50,000"` — คุณจะเลือก locator ไหน และทำไม?

> **คำถาม 2:** เพื่อนบอกว่า xpath ยืดหยุ่นที่สุด จึงใช้ xpath ทุกอย่าง — คุณจะบอกเพื่อนว่าอะไร?

> **คำถาม 3:** test ที่เคย pass เริ่ม fail ด้วย "element not found" หลัง app release version ใหม่ — ถ้าคุณใช้ `accessibility_id` กับ `xpath` คนละอันคุณจะ debug อันไหนก่อน? ทำไม?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย 1:** ใช้ `id=com.bank:id/tv_balance` เพราะ content-desc ว่างเปล่า ส่วน resource-id มีค่าและ stable กว่า text ที่อาจเปลี่ยนตามยอดเงิน

**เฉลย 2:** xpath มีข้อเสียสำคัญ: ช้ากว่า accessibility_id และ id มาก (ต้อง scan ทั้ง UI tree), และ xpath ที่ใช้ position ใน tree เปราะบางมาก — ควรใช้ accessibility_id เป็นหลัก xpath เป็น fallback เท่านั้น

**เฉลย 3:** ตรวจ xpath ก่อน เพราะ xpath ที่ขึ้นอยู่กับ UI structure เปลี่ยนได้เมื่อ layout เปลี่ยน ส่วน accessibility_id เปลี่ยนได้ก็ต่อเมื่อ developer เปลี่ยน contentDescription ซึ่งเกิดน้อยกว่า

</details>

---

**บทต่อไป:** [บทที่ 6 — Gestures & Interactions](06-gestures-interactions.md)
