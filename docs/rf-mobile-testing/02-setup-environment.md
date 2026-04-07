# บทที่ 2: Setup สภาพแวดล้อม

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อน:

> **Appium server กับ AppiumLibrary ต่างกันยังไง? และทำไม Appium 2.x ถึงต้องติดตั้ง driver แยก?**

---

<details>
<summary>ดูเฉลย (กด expand หลังจากตอบเองแล้ว)</summary>

Appium server คือ Node.js application ที่รับ HTTP requests แล้วส่งคำสั่งไปที่ device
AppiumLibrary คือ Robot Framework library (Python) ที่เป็น client ส่ง requests ไปหา Appium server

Appium 2.x แยก driver เป็นโมดูลอิสระ เพื่อให้แต่ละ platform driver (Android, iOS) release เวอร์ชันใหม่แยกกันได้

</details>

---

## วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะ:

- ติดตั้ง Android Studio และสร้าง Android emulator ได้
- ติดตั้ง Appium server และ UIAutomator2 driver ได้
- ดาวน์โหลดและ setup Appium Inspector ได้
- ติดตั้ง robotframework-appiumlibrary ได้
- Verify ว่า environment พร้อมใช้งานโดยรัน `appium-doctor`

---

## ทำไมต้องรู้? (Why)

Setup คือ "ประตูด่านแรก" ของ mobile testing — ถ้าข้ามไม่ผ่าน ทำอะไรต่อไม่ได้เลย

ต่างจาก web testing ที่แค่ `pip install selenium` แล้วรันได้เลย mobile testing ต้องการ:
- Java (สำหรับ Android)
- Android SDK (เครื่องมือของ Google)
- Appium server (Node.js)
- Emulator หรือ real device

ขั้นตอนเยอะ แต่ทำครั้งเดียวแล้วใช้ได้นาน

---

## Analogy: เตรียมห้องทดสอบ

Setup mobile testing เหมือนเตรียม "ห้องทดสอบยา" ก่อนจะทดสอบได้:
- ห้องทดสอบ = สภาพแวดล้อมของคุณ
- เครื่องมือวัด = Android Studio + Appium
- ตัวอย่างยา = app ที่จะทดสอบ
- เทคนิเชียน = Robot Framework + AppiumLibrary

ต้องเตรียมทุกอย่างพร้อมก่อน ถึงจะเริ่มทดลองได้

> ⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:**
> - ทำครั้งเดียวแล้วเสร็จตลอดไป → จริงๆ ต้องดูแล environment เมื่อ update Android Studio, Appium, หรือ driver version
> - Setup ซับซ้อนแสดงว่า tool ไม่ดี → ความซับซ้อนมาจากการที่ mobile OS มี security ระดับสูง ไม่ใช่ข้อบกพร่อง

---

## เนื้อหาหลัก

### ภาพรวม Component ที่ต้องติดตั้ง

```
┌─────────────────────────────────────────────────┐
│  Java JDK                (JAVA_HOME)            │
│  Android Studio          (ANDROID_HOME)         │
│  Node.js                                        │
│  Appium Server           (npm install)          │
│  UIAutomator2 Driver     (appium driver)        │
│  Appium Inspector        (desktop app)          │
│  Python 3.9+                                    │
│  robotframework-appiumlibrary  (pip install)    │
└─────────────────────────────────────────────────┘
```

---

### ขั้นตอนที่ 1: Java JDK

Android tools ต้องการ Java JDK (ไม่ใช่ JRE)

> "Set JAVA_HOME pointing to your JDK home directory. The JDK—not the JRE—is required."
> *(appium.io)*

**ติดตั้ง:**

macOS:
```bash
brew install openjdk@17
# ดู path ที่ได้หลัง install แล้ว set JAVA_HOME
```

Windows: ดาวน์โหลด JDK 17+ จาก adoptium.net แล้วติดตั้ง

**Verify:**
```bash
java -version
# ควรเห็น: openjdk version "17.x.x" หรือใหม่กว่า
```

---

### ขั้นตอนที่ 2: Android Studio

Android Studio คือ IDE ของ Android แต่เราต้องการมันเพื่อ:
1. Android SDK (เครื่องมือสำหรับ build/run Android)
2. AVD Manager (สร้าง emulator)

**ดาวน์โหลด:** developer.android.com/studio

**Setup หลัง install:**
1. เปิด Android Studio
2. ไปที่ **SDK Manager** (Tools → SDK Manager)
3. ติดตั้ง **Android SDK Platform** (เลือก API level 30+ แนะนำ API 33)
4. ติดตั้ง **Android SDK Build-Tools**
5. ติดตั้ง **Android Emulator**

**ตั้งค่า Environment Variable:**

macOS/Linux — เพิ่มใน `~/.zshrc` หรือ `~/.bashrc`:
```bash
export ANDROID_HOME=$HOME/Library/Android/sdk        # macOS
export ANDROID_HOME=$HOME/Android/Sdk               # Linux
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/emulator
```

Windows — เพิ่ม System Environment Variables:
```
ANDROID_HOME = C:\Users\YourName\AppData\Local\Android\Sdk
PATH += %ANDROID_HOME%\platform-tools
PATH += %ANDROID_HOME%\emulator
```

**Verify:**
```bash
adb version
# ควรเห็น: Android Debug Bridge version X.X.X
```

---

### ขั้นตอนที่ 3: สร้าง Android Emulator

1. ใน Android Studio ไปที่ **Device Manager** (Tools → Device Manager)
2. คลิก **Create Device**
3. เลือก device model เช่น **Pixel 6**
4. เลือก System Image เช่น **Android 13 (API 33)** — กด Download ถ้ายังไม่มี
5. กด Finish

**เปิด emulator:**
```bash
# วิธีที่ 1: คลิก play button ใน Device Manager
# วิธีที่ 2: command line
emulator -avd Pixel_6_API_33
```

**Verify:**
```bash
adb devices
# ควรเห็น emulator-5554  device
```

---

### ขั้นตอนที่ 4: Node.js

Appium server รันบน Node.js

**ติดตั้ง:** nodejs.org (เลือก LTS version)

**Verify:**
```bash
node --version   # ควรเป็น v18.x.x หรือใหม่กว่า
npm --version
```

---

### ขั้นตอนที่ 5: Appium Server

```bash
npm i --location=global appium
```

> "To set up Appium system-wide, run: npm i --location=global appium"
> *(appium.io)*

**Verify:**
```bash
appium --version
# ควรเห็น: 2.x.x
```

---

### ขั้นตอนที่ 6: UIAutomator2 Driver

Appium 2.x ต้องติดตั้ง driver แยก:

```bash
appium driver install uiautomator2
```

> "Execute this command: appium driver install uiautomator2"
> *(appium.io)*

**Verify:**
```bash
appium driver list --installed
# ควรเห็น uiautomator2 อยู่ในรายการ
```

---

### ขั้นตอนที่ 7: Appium Inspector

Appium Inspector คือ GUI tool สำหรับ "inspect" elements บน mobile screen — คล้ายกับ Chrome DevTools แต่สำหรับ mobile

> "Appium Inspector is a tool for inspecting and interacting with mobile applications during testing."
> *(github.com/appium/appium-inspector)*

**ดาวน์โหลด:** github.com/appium/appium-inspector/releases

- macOS Intel: `mac-x64.dmg`
- macOS Apple Silicon (M1/M2/M3): `mac-arm64.dmg`
- Windows: `win-x64.exe` หรือ `win-arm64.exe`

---

### ขั้นตอนที่ 8: robotframework-appiumlibrary

```bash
pip install --upgrade robotframework-appiumlibrary
```

> "pip install --upgrade robotframework-appiumlibrary"
> *(github.com/serhatbolsu/robotframework-appiumlibrary)*

**Verify:**
```bash
python -c "import AppiumLibrary; print(AppiumLibrary.__version__)"
# ควรเห็น version number
```

---

### ขั้นตอนที่ 9: appium-doctor (ตรวจสุขภาพ environment)

appium-doctor คือ tool ที่ตรวจว่า dependencies ครบไหม

```bash
npm install -g appium-doctor
appium-doctor --android
```

Output ที่ดี:
```
✔ ANDROID_HOME is set to...
✔ JAVA_HOME is set to...
✔ adb exists at...
✔ android exists at...
✔ emulator exists at...
```

ถ้ามี ✘ ให้แก้ทีละข้อตาม output ที่บอก

---

## ตัวอย่าง 3 ระดับ

### Beginner: Start Appium server และ verify

หลัง setup ครบ ลองทำขั้นตอนนี้:

```bash
# Terminal 1: Start Appium server
appium

# ควรเห็น:
# [Appium] Welcome to Appium v2.x.x
# [Appium] Non-default server args: {}
# [Appium] Appium REST http interface listener started on 0.0.0.0:4723
```

```bash
# Terminal 2: ตรวจว่า Appium ตอบสนอง
curl http://localhost:4723/status
# ควรได้ JSON response กลับมา
```

### Intermediate: Setup บน Windows + ข้อควรระวัง

บน Windows environment variables ต้องตั้งผ่าน System Properties:

1. กด `Win + X` → System → Advanced System Settings
2. Environment Variables → System variables
3. เพิ่ม `ANDROID_HOME` = `C:\Users\YourName\AppData\Local\Android\Sdk`
4. แก้ไข `Path` → เพิ่ม `%ANDROID_HOME%\platform-tools` และ `%ANDROID_HOME%\emulator`
5. **Restart terminal** หลังแก้ — environment variables ไม่ update แบบ real-time

ข้อผิดพลาดที่พบบ่อยบน Windows: ใช้ path ที่มี space เช่น `C:\Program Files\...` → ใส่ quotes ให้ถูกต้อง

### Advanced: Troubleshoot Emulator ไม่ขึ้น

ถ้า `adb devices` ไม่เห็น emulator ให้ตรวจตามลำดับ:

```bash
# 1. ดูว่า emulator process รันอยู่ไหม
ps aux | grep emulator    # macOS/Linux
tasklist | findstr emulator  # Windows

# 2. Kill ADB server แล้ว restart
adb kill-server
adb start-server
adb devices

# 3. ถ้า emulator ค้าง — force stop แล้วเปิดใหม่
# ใน Android Studio: Device Manager → คลิก Stop แล้ว Start ใหม่

# 4. ตรวจว่า BIOS เปิด virtualization (Intel VT-x / AMD-V)
# ถ้าไม่เปิด emulator จะช้ามากหรือไม่ทำงาน
```

---

## Checklist ก่อนไปบทถัดไป

ทำ checklist นี้ให้ครบก่อน:

- [ ] `java -version` แสดงผล ✅
- [ ] `adb version` แสดงผล ✅
- [ ] `node --version` แสดงผล v18+ ✅
- [ ] `appium --version` แสดงผล 2.x ✅
- [ ] `appium driver list --installed` เห็น uiautomator2 ✅
- [ ] `adb devices` เห็น emulator เมื่อเปิด Android Studio ✅
- [ ] Appium Inspector ดาวน์โหลดและเปิดได้ ✅
- [ ] `pip install robotframework-appiumlibrary` สำเร็จ ✅

---

## Common Mistakes

❌ **ติดตั้ง JRE แทน JDK**
→ บาง tool รายงาน error แปลกๆ เกี่ยวกับ Java
✅ **ต้องใช้ JDK (Java Development Kit)** ไม่ใช่ JRE
*(source: appium.io/docs/en/2.0/quickstart/uiauto2-driver/)*

---

❌ **ไม่ได้ตั้ง ANDROID_HOME**
```bash
adb: command not found
```
✅ **ต้องตั้ง ANDROID_HOME และเพิ่ม platform-tools ใน PATH** และ restart terminal หลังแก้
*(source: appium.io/docs/en/2.0/quickstart/uiauto2-driver/)*

---

❌ **เปิด emulator แล้วรัน test เลยโดยไม่รอให้ boot ครบ**
→ Session creation fail เพราะ device ยังไม่พร้อม
✅ **รอให้ emulator แสดง home screen ก่อนเสมอ** ก่อนรัน test
*(source: ประสบการณ์จาก Appium community)*

---

❌ **ลืม start Appium server ก่อนรัน test**
→ `ConnectionRefusedError` ทุกครั้ง
✅ **Appium server ต้องรันอยู่ใน terminal แยก** ตลอดเวลาที่รัน test
*(source: appium.io)*

---

## สรุปบท

ลองตอบก่อนดูเฉลย:

> **คำถาม 1:** `ANDROID_HOME` ต้อง point ไปที่ folder อะไร? และทำไมถึงจำเป็น?

> **คำถาม 2:** ถ้า `appium-doctor --android` แสดง ✘ ที่ JAVA_HOME — สิ่งแรกที่คุณควรทำคืออะไร?

> **คำถาม 3:** ทำไม Appium 2.x ถึงต้องรัน `appium driver install uiautomator2` แยกต่างหาก? ถ้าไม่รันจะเกิดอะไรขึ้น?

---

<details>
<summary>ดูเฉลย</summary>

**เฉลย 1:** ANDROID_HOME ต้อง point ไปที่ Android SDK directory (ที่ Android Studio ดาวน์โหลด tools ไว้) เพราะ Appium ต้องการ `adb` และ emulator tools ที่อยู่ใน SDK นั้น

**เฉลย 2:** ตรวจว่ามี JDK ติดตั้งอยู่จริงไหม (`java -version`) ถ้ามีแล้วให้ตั้งค่า JAVA_HOME ให้ชี้ไปที่ JDK directory แล้ว restart terminal

**เฉลย 3:** Appium 2.x ใช้ modular architecture — server และ driver แยกกัน ถ้าไม่ install driver จะได้ error "No driver found for capabilities" เมื่อพยายาม start session

</details>

---

**บทต่อไป:** [บทที่ 3 — Appium Capabilities](03-appium-capabilities.md)
