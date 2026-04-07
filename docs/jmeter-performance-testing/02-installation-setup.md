# บทที่ 2: JMeter Installation & Setup

**เวลาที่ใช้โดยประมาณ:** 30–45 นาที

---

## ⏰ Pre-chapter Retrieval

> **แนะนำ: อ่านบทนี้หลังจากผ่านไปอย่างน้อย 1 วันหลังอ่านบทที่ 1** — ช่วงเวลาที่ห่างออกไปทำให้ retrieval ยากขึ้น และการพยายาม recall ที่ยากขึ้นนั้นเองที่ทำให้จำได้นานกว่า (Desirable Difficulty)

**ก่อนอ่านบทนี้เรื่อง JMeter installation ลองตอบคำถามนี้ก่อน:**

บทที่ 1 อธิบายว่า JMeter ทำงานที่ "protocol level" และไม่ใช่ browser — บทนี้จะพูดถึงการติดตั้งซึ่งต้องใช้ Java เป็น dependency หลัก ลองคิดก่อนอ่านว่า: **ถ้า JMeter ต้องทดสอบระบบที่รับ 500 concurrent users แต่เครื่อง JMeter มี RAM เพียง 512MB — จะเกิดปัญหาอะไร และสิ่งนี้เกี่ยวข้องกับการ setup อย่างไร?** เขียนคำตอบลงกระดาษ แล้วเขียนเหตุผลสั้นๆ 1–2 ประโยค

หยุดคิดอย่างน้อย 30 วินาทีก่อนอ่านเฉลย

---

**เฉลย:** JMeter ใช้ RAM ในการ simulate virtual users แต่ละ thread กิน memory เพราะ JMeter เป็น pure Java ทำงานบน JVM ที่ต้องการ heap memory ถ้า RAM น้อยเกินไป JMeter เองจะ run out of memory ก่อนที่จะสามารถ simulate 500 concurrent users ได้ครบ ทำให้ผล test ไม่น่าเชื่อถือ — นี่คือสาเหตุที่ setup ที่ถูกต้อง รวมถึงการตั้งค่า Java heap size เป็นส่วนสำคัญของ installation ไม่ใช่แค่ "unzip แล้วรัน"

ถ้าตอบไม่ได้: ไม่เป็นไร — กลับมาอ่านเฉลยนี้อีกครั้งหลังอ่าน section 4.3 ของบทนี้ จะเข้าใจมากขึ้น

**Remediation:** ถ้าไม่แน่ใจเรื่อง JMeter ทำงานที่ protocol level กลับอ่าน section 4.1 ของบทที่ 1 ก่อน

---

## 1. วัตถุประสงค์

อ่านบทนี้จบแล้ว คุณจะสามารถ:

- **ระบุ** Java version ที่ JMeter ต้องการ และอธิบายว่าทำไม JDK ดีกว่า JRE สำหรับ JMeter
- **ติดตั้ง** Apache JMeter 5.6.3 บนเครื่องตัวเองได้ตามขั้นตอน
- **เปิดใช้งาน** JMeter ทั้ง GUI mode (สำหรับสร้าง script) และ CLI mode (สำหรับรัน load test จริง)
- **อธิบาย** ว่าทำไม GUI mode ถึงไม่ควรใช้สำหรับ load test จริง พร้อมเหตุผล
- **ตั้งค่า** Java heap size สำหรับ load test ขนาดใหญ่ได้

---

## 2. ทำไมต้องรู้? (Why)

### Setup ที่ผิดทำให้ผล test ไม่น่าเชื่อถือ

ลองนึก scenario นี้: ทีมรัน load test แล้วได้ average response time = 800ms ซึ่งเกิน SLA ที่กำหนดไว้ว่า < 500ms ทีมสรุปว่า "ระบบช้า ต้องปรับ backend" แต่จริงๆ แล้ว JMeter กำลังรันใน GUI mode บน laptop ที่มี RAM 8GB และกำลัง run Chrome, VS Code, และ Slack พร้อมกัน JMeter เองกิน resource หนัก ทำให้ timestamp ที่บันทึกไม่แม่นยำ ผล 800ms นั้นรวม overhead จาก JMeter เอง ไม่ใช่แค่ response จาก server จริงๆ

**Setup ที่ผิดทำให้ตัดสินใจผิด** — นี่คือเหตุผลที่บทนี้ไม่ใช่แค่ "วิธีติดตั้ง" แต่รวมถึง best practice ที่ทำให้ผล test น่าเชื่อถือ

### ทำไม JMeter ถึงต้องการ Java?

> "JMeter is a 100% Java application and should run correctly on any system that has a compliant Java implementation." — jmeter.apache.org/usermanual/get-started.html

เพราะ JMeter เขียนด้วย Java ทั้งหมด ทำงานบน JVM (Java Virtual Machine) — ข้อดีคือทำงานได้บน Windows, macOS, Linux โดยใช้ไฟล์ JMeter ชุดเดียวกัน ไม่ต้องมีไฟล์ติดตั้งแยก OS

---

## 3. Analogy

### JMeter Installation เหมือน "การตั้งสตูดิโอบันทึกเสียงมือถือ"

ลองนึกถึงนักดนตรีที่ต้องการบันทึกเสียงแบบ professional บนโทรศัพท์มือถือ แค่ดาวน์โหลด app บันทึกเสียงยังไม่พอ ต้องมี:
- **ไมค์ที่ดี** (เหมือน Java JDK ที่ถูก version — ถ้าไม่มี ทำงานไม่ได้เลย)
- **พื้นที่จัดเก็บเพียงพอ** (เหมือน RAM และ heap size — ถ้าน้อยเกินไป บันทึกได้แต่คุณภาพแย่หรือ crash)
- **รันใน quiet mode ปิดแอปอื่น** (เหมือนรัน CLI mode แทน GUI mode — เพื่อให้บันทึกได้แม่นยำโดยไม่มี noise จากแอปอื่น)

**ใช้ app แต่ไม่ได้ตั้งค่า = บันทึกได้แต่ผลไม่น่าเชื่อถือ**
**ติดตั้ง JMeter แต่ไม่ได้ตั้งค่า = รัน test ได้แต่ผลไม่น่าเชื่อถือ**

---

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:** GUI mode กับ CLI mode ต่างกันแค่หน้าตา — ผิดอย่างมาก GUI mode กิน CPU/RAM มากกว่า CLI mode อย่างมีนัยสำคัญ ซึ่งส่งผลโดยตรงต่อความน่าเชื่อถือของผลลัพธ์ (JMeter เองกลายเป็น bottleneck) ไม่ใช่แค่ความสวยงาม — official docs ระบุชัดว่า "GUI mode should only be used for creating the test script, CLI mode must be used for load testing"

---

## 4. เนื้อหาหลัก

### 4.1 System Requirements — Java

> "JMeter is compatible with Java 8 or higher. We highly advise you to install latest minor version of your major version for security and performance reasons." — jmeter.apache.org/usermanual/get-started.html

JMeter 5.6.3 ต้องการ **Java 8 ขึ้นไป** แนะนำให้ใช้ Java 11 หรือ Java 17 (LTS versions) เพราะ stability ดีกว่า

**JDK vs JRE — ต้องใช้ตัวไหน?**

> "Although you can use a JRE, it is better to install a JDK as for recording of HTTPS, JMeter needs keytool utility from JDK." — jmeter.apache.org/usermanual/get-started.html

✅ **วิธีที่แนะนำ: ติดตั้ง JDK (Java Development Kit)** — เพราะ JMeter ต้องการ `keytool` utility (อยู่ใน JDK เท่านั้น) สำหรับ record HTTPS traffic ถ้าติดตั้งแค่ JRE จะ record HTTPS ไม่ได้และอาจเจอ error ในบางฟีเจอร์

⚠️ **ควรเลี่ยง: ติดตั้งแค่ JRE** — เพราะขาด `keytool` ที่จำเป็น

---

### 4.2 Download และ Install JMeter

> "Apache JMeter 5.6.3 (Requires Java 8+)" — jmeter.apache.org/download_jmeter.cgi (ณ วันที่ 2026-03-17)

> "The easiest way to begin using JMeter is to first download the latest production release and install it." — jmeter.apache.org/usermanual/get-started.html

> "To install a release build, simply unzip the zip/tar file into the directory where you want JMeter to be installed." — jmeter.apache.org/usermanual/get-started.html

**JMeter ไม่มี installer** — เป็นแค่ zip/tar file ที่ unzip แล้วใช้ได้เลย ไม่ต้อง "install" แบบ traditional software

**โครงสร้าง directory หลังติดตั้ง:**

> "The installation directory structure should look something like this (where X.Y is version number): apache-jmeter-X.Y/bin, apache-jmeter-X.Y/docs, apache-jmeter-X.Y/lib/" — jmeter.apache.org/usermanual/get-started.html

```
apache-jmeter-5.6.3/
├── bin/          ← script สำหรับเปิด JMeter (jmeter, jmeter.bat, jmeter-server)
├── docs/         ← local documentation
├── lib/          ← JAR dependencies
│   └── ext/      ← ใส่ plugin JARs ที่นี่
└── extras/       ← เครื่องมือเสริม
```

---

⏸ **หยุดก่อนอ่านต่อ (Backward Retrieval จาก 4.1):** จากที่อ่านมาเรื่อง JDK vs JRE — ถ้าเพื่อนบอกว่า "ฉันติดตั้ง JRE ไว้อยู่แล้ว ติดตั้ง JMeter ได้เลยไหม?" คุณจะตอบว่าอะไร? เขียนคำตอบลงกระดาษพร้อมเหตุผล

---

> **เฉลย:** ตอบว่า "ติดตั้งได้ แต่มีข้อจำกัด" — JMeter จะ run ได้แต่จะขาด `keytool` utility ซึ่งจำเป็นสำหรับ recording HTTPS traffic ถ้าแผนคือ test เฉพาะ HTTP (ไม่ใช่ HTTPS) ก็ใช้ JRE ได้ แต่แนะนำให้ใช้ JDK เพื่อความสะดวกในระยะยาว
>
> **ถ้าตอบไม่ได้:** อ่าน section 4.1 อีกครั้งโดยเน้น QUOTE เรื่อง keytool

---

### 4.3 วิธีเปิด JMeter

> "To run JMeter, run the jmeter.bat (for Windows) or jmeter (for Unix) file." — jmeter.apache.org/usermanual/get-started.html

**สองโหมดสำคัญ:**

**GUI Mode (สำหรับสร้าง test script เท่านั้น)**
```
# macOS/Linux
cd /path/to/apache-jmeter-5.6.3/bin
./jmeter

# Windows
cd C:\path\to\apache-jmeter-5.6.3\bin
jmeter.bat
```

**CLI Mode / Non-GUI Mode (สำหรับรัน load test จริง)**
```
# คำสั่งพื้นฐาน
jmeter -n -t my_test.jmx -l results.jtl

# คำสั่งพร้อม proxy (ถ้าต้องการ)
# source: https://jmeter.apache.org/usermanual/get-started.html
jmeter -n -t my_test.jmx -l log.jtl -H my.proxy.server -P 8000
```

ความหมายของ flags:
> "-n: This specifies JMeter is to run in cli mode" / "-t [name of JMX file that contains the Test Plan]" / "-l [name of JTL file to log sample results to]" — jmeter.apache.org/usermanual/get-started.html

---

### 4.4 GUI Mode vs CLI Mode — ความแตกต่างที่สำคัญมาก

นี่เป็น best practice ที่ official JMeter docs ระบุไว้ชัดเจน:

> "GUI mode should only be used for creating the test script, CLI mode (NON GUI) must be used for load testing" — jmeter.apache.org/usermanual/get-started.html

✅ **วิธีที่แนะนำ: ใช้ CLI mode สำหรับ actual load test เสมอ** — เพราะ GUI mode กิน CPU และ memory มากขึ้นเพื่อ render interface ทำให้ JMeter เองเป็น bottleneck และ timestamp ที่บันทึกไม่แม่นยำ ผลลัพธ์ที่ได้จะ skewed ไปทาง "ช้ากว่าความเป็นจริง"

⚠️ **ควรเลี่ยง: รัน load test ใน GUI mode** — เพราะ JMeter GUI เองกิน CPU/memory มาก ทำให้ผล test ไม่น่าเชื่อถือ แม้คุณจะ "เห็น" progress แบบ real-time ก็ตาม

**กฎที่ง่ายจำ:**
- GUI mode = สร้าง test plan, debug, ดู request/response
- CLI mode = รัน actual load test ทุกครั้ง

---

⏸ **หยุดก่อนอ่านต่อ — Bloom's Level 3 (Application):** สมมติว่าคุณเพิ่งสร้าง test plan เสร็จแล้วใน JMeter GUI และต้องการรัน load test จริงกับ 300 concurrent users วาง step-by-step ว่าจะทำอะไรต่อไป (ระบุให้ชัดว่าใช้ GUI หรือ CLI และทำไม) เขียนคำตอบลงกระดาษก่อน — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น — แล้วค่อยอ่านต่อ

---

> **เฉลย:**
> 1. **บันทึก test plan** เป็น `.jmx` file ใน JMeter GUI (File → Save)
> 2. **ปิด JMeter GUI** — ไม่รัน test ใน GUI
> 3. **เปิด Terminal**
> 4. **รัน CLI mode:** `jmeter -n -t my_test.jmx -l results.jtl`
> 5. รอจนเสร็จ แล้วดูผลจาก `results.jtl` หรือ generate HTML report แยกต่างหาก
>
> เหตุผลที่ปิด GUI ก่อน: เพื่อคืน RAM และ CPU ให้ JMeter ใช้สำหรับ load test จริงๆ ไม่ใช่เสียไปกับการ render interface
>
> **ถ้าตอบไม่ได้:** อ่าน section 4.4 อีกครั้ง

---

### 4.5 Java Heap Size — ตั้งค่าให้พอสำหรับ Load Test

> "Increase the Java Heap size. By default JMeter runs with a heap of 1 GB." / Default HEAP: "-Xms1g -Xmx1g -XX:MaxMetaspaceSize=256m" — jmeter.apache.org/usermanual/get-started.html

**ค่า default คือ 1 GB** — เพียงพอสำหรับ test ที่มี thread ไม่มาก แต่ถ้าต้องการจำลอง concurrent users จำนวนมาก (เช่น 1,000+ threads) อาจต้องเพิ่ม

วิธีตั้งค่า heap size:
- แก้ไขใน file `bin/jmeter` (macOS/Linux) หรือ `bin/jmeter.bat` (Windows)
- หรือ set environment variable `JVM_ARGS` ก่อนรัน

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner: ขั้นตอนติดตั้ง Java + JMeter บน macOS

```bash
# Step 1: ตรวจสอบว่ามี Java ติดตั้งไว้แล้วไหม
java -version
# ถ้าได้ "openjdk version 11.x.x" หรือสูงกว่า → ข้ามไป Step 3

# Step 2: ติดตั้ง Java JDK (ถ้ายังไม่มี)
# วิธีที่ 1: ใช้ Homebrew (แนะนำ — จัดการ version ง่าย)
brew install openjdk@17

# หลัง install เสร็จ Homebrew จะบอกให้ทำ symlink:
sudo ln -sfn $(brew --prefix openjdk@17)/libexec/openjdk.jdk \
    /Library/Java/JavaVirtualMachines/openjdk-17.jdk
# ทำไมต้อง symlink: เพราะ macOS ค้นหา JDK จาก /Library/Java/JavaVirtualMachines/

# ยืนยันว่า install สำเร็จ:
java -version
# ต้องเห็น: openjdk version "17.x.x" หรือสูงกว่า

# Step 3: ดาวน์โหลด JMeter
# ไปที่ https://jmeter.apache.org/download_jmeter.cgi
# คลิก "apache-jmeter-5.6.3.tgz" (macOS/Linux ใช้ .tgz)
# หรือใช้ curl:
curl -O https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.tgz
# ทำไมใช้ .tgz ไม่ใช่ .zip: .tgz เล็กกว่าและ preserve file permissions บน Unix ได้ดีกว่า

# Step 4: Extract
tar -xzf apache-jmeter-5.6.3.tgz
# ทำไมใช้ tar ไม่ใช่แค่ double-click: สะดวกกว่าถ้าใช้ผ่าน terminal และ script

# Step 5: ตรวจสอบ directory structure
ls apache-jmeter-5.6.3/
# ต้องเห็น: bin/ docs/ lib/ extras/

# Step 6: เปิด JMeter GUI (ครั้งแรกเพื่อดูว่า install ถูกต้อง)
cd apache-jmeter-5.6.3/bin
./jmeter
# JMeter GUI window จะเปิดขึ้นมา — ถ้าเห็น JMeter window แปลว่า install สำเร็จ

# ยังไม่ได้ทดสอบ command ทั้งหมดจาก end-to-end — ต้องการ JMeter environment จริง
```

---

### Intermediate: ตรวจสอบ Installation และ Fix Common Errors

**Scenario:** Developer ติดตั้ง JMeter แล้วพยายามรันผ่าน terminal แต่เจอ error หลายอย่าง domain: developer environment troubleshooting

```bash
# === ตรวจสอบ installation อย่างละเอียด ===

# ตรวจสอบ Java ที่ JMeter จะใช้
java -version 2>&1
# ผลที่ต้องการ: เห็น version 8 ขึ้นไป
# ถ้าเห็น "command not found": Java ไม่ได้ติดตั้ง หรือ PATH ไม่ถูกตั้ง

# ตรวจสอบว่า JAVA_HOME ถูกตั้งไหม (บางระบบต้องการ)
echo $JAVA_HOME
# ถ้า output ว่าง: ตั้ง JAVA_HOME ก่อน

# ตั้ง JAVA_HOME สำหรับ macOS + Homebrew OpenJDK:
export JAVA_HOME=$(brew --prefix openjdk@17)
# เพิ่มบรรทัดนี้ใน ~/.zshrc หรือ ~/.bash_profile ด้วยถ้าต้องการให้ persistent

# ตรวจสอบ JMeter binary permissions (macOS/Linux)
ls -la /path/to/apache-jmeter-5.6.3/bin/jmeter
# ต้องเห็น -rwxr-xr-x (มี execute permission)
# ถ้าไม่มี x: chmod +x /path/to/apache-jmeter-5.6.3/bin/jmeter

# Common Error 1: "Unable to access jarfile ApacheJMeter.jar"
# สาเหตุ: ไม่ได้อยู่ใน directory ที่ถูก หรือ extract ไม่สมบูรณ์
ls /path/to/apache-jmeter-5.6.3/bin/ApacheJMeter.jar
# ถ้าไม่เห็นไฟล์: extract ใหม่อีกครั้ง

# Common Error 2: "Error: Could not create the Java Virtual Machine"
# สาเหตุมักเป็น: heap size ที่ตั้งไว้เกิน RAM ที่มี
# ดูว่าตั้ง heap ไว้เท่าไหร่ใน bin/jmeter:
grep -n "Xmx" /path/to/apache-jmeter-5.6.3/bin/jmeter
# ถ้าเห็น -Xmx4g แต่เครื่องมี RAM 4GB: ลด heap ลงเป็น -Xmx2g

# ยังไม่ได้ทดสอบ — ต้องการ JMeter environment จริง
```

**Error ที่พบบ่อยและวิธีแก้:**

| Error message | สาเหตุที่น่าจะเป็น | วิธีแก้เบื้องต้น |
|---------------|---------------------|-----------------|
| `command not found: jmeter` | ไม่ได้อยู่ใน `bin/` directory | `cd apache-jmeter-5.6.3/bin` ก่อนแล้วค่อยรัน |
| `Unable to access jarfile` | Extract ไม่สมบูรณ์ หรือไม่ได้อยู่ใน bin/ | ตรวจสอบว่า `ApacheJMeter.jar` อยู่ใน `bin/` |
| `Java Virtual Machine error` | Heap size ใหญ่เกินไป | ลด `-Xmx` ให้น้อยกว่า RAM ที่มี |
| `JAVA_HOME is not set` | ตัวแปร environment ไม่ถูกตั้ง | Export `JAVA_HOME` ใน shell profile |

---

### Advanced: ตั้งค่า JAVA_OPTS สำหรับ Large-scale Load Test

**Scenario:** ต้องการรัน load test ที่มี 2,000 concurrent users จาก single JMeter instance บน server ที่มี 16GB RAM — production-grade configuration

```bash
# ตั้งค่า JVM_ARGS ก่อนรัน JMeter
# ทำไมตั้งผ่าน environment variable แทนแก้ bin/jmeter:
# เพราะ environment variable ไม่ถูก commit เข้า git และ override ง่ายต่อ environment ต่างๆ

# สำหรับ load test ขนาดใหญ่บน 16GB RAM server:
export JVM_ARGS="-Xms4g -Xmx4g -XX:MaxMetaspaceSize=512m -XX:+UseG1GC"
# -Xms4g: เริ่มต้น heap ที่ 4GB (ไม่ต้อง resize เพิ่ม = เร็วกว่า)
# -Xmx4g: จำกัด heap ไม่เกิน 4GB (เหลือ RAM ให้ OS และ process อื่น)
# -XX:MaxMetaspaceSize=512m: เพิ่ม Metaspace สำหรับ JMeter classes จำนวนมาก
# -XX:+UseG1GC: ใช้ G1 Garbage Collector ซึ่งมี pause time สั้นกว่า default

# รัน load test
cd /path/to/apache-jmeter-5.6.3/bin
./jmeter -n -t /path/to/load_test.jmx -l /path/to/results.jtl

# ทำไม -Xms กับ -Xmx ต้องเท่ากัน:
# เพราะถ้า Xms < Xmx JVM จะ resize heap ระหว่าง test ซึ่งทำให้มี GC pause
# และ timestamp บันทึกไม่แม่นยำ ส่งผลต่อ response time measurement

# ตรวจสอบ GC behavior ระหว่าง test (optional แต่แนะนำสำหรับ production):
export JVM_ARGS="-Xms4g -Xmx4g -XX:MaxMetaspaceSize=512m -XX:+UseG1GC \
  -verbose:gc -Xloggc:/tmp/jmeter-gc.log"
# ดู gc.log หลัง test เสร็จเพื่อดูว่า GC pause เกิดบ่อยแค่ไหน

# ยังไม่ได้ทดสอบ — ต้องการ JMeter environment และ server จริง
# ค่า heap size ที่เหมาะสมต้องปรับตาม: จำนวน threads, ชนิด test, OS ที่ใช้
```

**การคิด heap size ที่เหมาะสม:**

กฎ rough estimate: thread 1 ตัวกิน RAM ประมาณ 1–2 MB (ขึ้นอยู่กับความซับซ้อนของ test plan)
- 500 threads → 1–2 GB heap (ค่า default 1 GB อาจพอ)
- 1,000 threads → 2–4 GB heap
- 2,000 threads → 4–6 GB heap

แต่นี่เป็นแค่ starting point — ต้อง monitor ระหว่างรัน test จริง

> Note: "A single JMeter client running on a 2-3 GHz CPU can handle 1000-2000 threads depending on test type." — jmeter.apache.org/usermanual/jmeter_distributed_testing_step_by_step.html

---

## 6. Common Mistakes

**❌ Mistake 1: ใช้ GUI Mode รัน Load Test จริง**

> ❌ เปิด JMeter GUI แล้วกด Run เพราะ "เห็น progress แบบ real-time สะดวกดี"

> ✅ สร้าง test plan ใน GUI แล้วบันทึกเป็น .jmx จากนั้น **ปิด GUI** และรันด้วย CLI: `jmeter -n -t test.jmx -l results.jtl`

**เหตุผล:** GUI mode กิน CPU และ memory สำหรับ rendering interface ทำให้ JMeter เองเป็น bottleneck ผล response time จะสูงกว่าความเป็นจริง ทำให้ตัดสินใจผิดเรื่องประสิทธิภาพของระบบที่ทดสอบ

> "GUI mode should only be used for creating the test script, CLI mode (NON GUI) must be used for load testing" — jmeter.apache.org/usermanual/get-started.html

🔍 **สัญญาณที่จะสังเกตเห็น:** CPU ของเครื่องที่รัน JMeter พุ่งสูง (> 80%) ระหว่าง test, ผล response time สูงกว่าที่คาดในขณะที่ server ด้านรับดูปกติ

🤔 **Metacognitive Prompt:** "ถ้า server-side metrics ดูปกติ (CPU ต่ำ, memory ปกติ) แต่ JMeter รายงาน response time สูง — ลองถามตัวเองว่า 'รันใน GUI mode อยู่ไหม?' และ 'เครื่องที่รัน JMeter มี resource พอไหม?'"

*(source: https://jmeter.apache.org/usermanual/get-started.html)*

---

**❌ Mistake 2: Heap Memory ไม่พอสำหรับจำนวน Thread ที่ต้องการ**

> ❌ รัน load test 1,000 threads ด้วย default heap 1 GB แล้ว JMeter crash กลาง test หรือ GC pause บ่อยมาก ทำให้ผล test ไม่สมบูรณ์

> ✅ ประเมิน heap ที่ต้องการก่อน (rough rule: 1,000 threads ≈ 2–4 GB) แล้วตั้งค่า JVM_ARGS: `export JVM_ARGS="-Xms2g -Xmx2g"` ก่อนรัน

**เหตุผล:** แต่ละ JMeter thread กิน memory เพราะต้องเก็บ request/response data, variables, และ state ถ้า heap ไม่พอ JVM จะเพิ่ม GC activity หรือ throw `OutOfMemoryError` ทำให้ test ล้มเหลวกลาง test plan

> "Increase the Java Heap size. By default JMeter runs with a heap of 1 GB." — jmeter.apache.org/usermanual/get-started.html

🔍 **สัญญาณที่จะสังเกตเห็น:** JMeter แสดง `java.lang.OutOfMemoryError` ใน log, test จบก่อนกำหนด, หรือ test ช้าลงมากช่วง midpoint (เป็น sign ของ GC pressure)

🤔 **Metacognitive Prompt:** "ถ้า test plan ผ่าน 200 threads แต่ fail ที่ 500 threads ทุกครั้ง และ error บอก memory-related — ให้สงสัย heap size ก่อน ไม่ใช่ระบบที่ทดสอบ"

*(source: https://jmeter.apache.org/usermanual/get-started.html)*

---

**❌ Mistake 3: ใช้ Java Version ที่ไม่ Compatible**

> ❌ ใช้ Java 7 ที่ติดตั้งไว้นานแล้ว (หรือ Java เก่ากว่า 8) กับ JMeter 5.6.3 แล้วได้ error ตอนเปิด

> ✅ ตรวจสอบ Java version ก่อน: `java -version` ต้องได้ version 8 ขึ้นไป ถ้าไม่ใช่ให้ upgrade

**เหตุผล:** JMeter 5.6.3 ต้องการ Java 8+ อย่างชัดเจน Java รุ่นเก่ากว่านั้นไม่รองรับ Java language features ที่ JMeter ใช้ในโค้ด

> "JMeter is compatible with Java 8 or higher." — jmeter.apache.org/usermanual/get-started.html

🔍 **สัญญาณที่จะสังเกตเห็น:** JMeter ไม่เปิดหรือ crash ทันทีพร้อม error เกี่ยวกับ `UnsupportedClassVersionError` หรือ Java version incompatibility

🤔 **Metacognitive Prompt:** "ก่อน debug ปัญหา JMeter ที่ซับซ้อน ให้ตรวจสอบสิ่งง่ายที่สุดก่อนเสมอ: `java -version` — ถ้า version ไม่ถูก ทุกอย่างอื่นไม่มีความหมาย"

*(source: https://jmeter.apache.org/usermanual/get-started.html)*

---

## 7. สรุปบท และ Retrieval Questions

**หยุดคิดอย่างน้อย 30 วินาทีสำหรับแต่ละข้อ เขียนคำตอบก่อนเปิดเฉลย — เขียนเหตุผลของคำตอบลงกระดาษด้วย การเขียนเองช่วยให้จำได้นานกว่าการแค่อ่านเฉลย (Generation Effect)**

---

**คำถามที่ 1 (Application-level):**
เพื่อนนักพัฒนาบอกว่า: "ฉันรัน load test ด้วย JMeter GUI เมื่อคืน ระบบช้ามาก average response time = 1,200ms เกิน SLA ที่ 800ms แน่ๆ ต้องปรับ backend แล้ว" คุณจะถามหรือตรวจสอบอะไรก่อนที่จะเห็นด้วยว่าต้อง optimize backend? ระบุให้ได้อย่างน้อย 2 จุดที่ต้องตรวจสอบก่อน

<details>
<summary>ดูเฉลย (เปิดหลังคิดแล้วเท่านั้น)</summary>

**เฉลย — สิ่งที่ต้องตรวจสอบก่อน:**

1. **รัน test ใน GUI mode หรือ CLI mode?** ถ้า GUI mode — ผลอาจไม่น่าเชื่อถือเพราะ JMeter GUI กิน resource และทำให้ response time measurement skewed ให้รัน test เดิมใน CLI mode ก่อนแล้วเปรียบเทียบ

2. **เครื่องที่รัน JMeter มี resource พอไหม?** ถ้า CPU ของ JMeter machine พุ่ง หรือ RAM ใกล้เต็ม ตัวเลข response time จะสูงกว่าความเป็นจริง ให้ดู CPU/RAM ของ machine ที่รัน JMeter ระหว่าง test ด้วย

3. **(Bonus) ดู server-side metrics ด้วยไหม?** ถ้า server CPU/RAM ปกติแต่ JMeter รายงานว่าช้า นั่นเป็น signal ว่าปัญหาอาจอยู่ที่ JMeter setup ไม่ใช่ backend

**Remediation:** ถ้าตอบไม่ชัด กลับอ่าน section 4.4 และ Common Mistake #1

</details>

---

**คำถามที่ 2 (Elaborative Interrogation — ทำไม):**
ทำไม JMeter ถึงต้องการ JDK แทนที่จะใช้ JRE ก็พอ? จะเกิดอะไรขึ้นถ้าเราติดตั้งแค่ JRE แล้วพยายามใช้ JMeter ทดสอบ HTTPS website?

<details>
<summary>ดูเฉลย (เปิดหลังคิดแล้วเท่านั้น)</summary>

**เฉลย:**
JMeter ต้องการ `keytool` utility ในการสร้าง SSL certificate สำหรับ record HTTPS traffic — และ `keytool` อยู่ใน JDK เท่านั้น ไม่ได้อยู่ใน JRE

ถ้าใช้แค่ JRE แล้วพยายาม record HTTPS traffic: JMeter จะ throw error เกี่ยวกับ missing certificate หรือไม่สามารถสร้าง proxy certificate ได้ ทำให้ recording ใช้งานไม่ได้ ถ้าเขียน test plan เองโดยไม่ใช้ recording feature และ server ไม่ต้องการ custom certificate — JRE อาจพอ แต่เป็นการเสี่ยงโดยไม่จำเป็น

**กฎง่าย:** ใช้ JDK เสมอเพื่อหลีกเลี่ยงปัญหาที่ไม่จำเป็น

**Remediation:** ถ้าตอบไม่ชัด กลับอ่าน section 4.1

</details>

---

**คำถามที่ 3 (Code-based Task):**
ดู command ด้านล่าง ระบุว่ามีปัญหาอะไร และจะแก้อย่างไร:

```bash
# ทีมต้องการรัน load test 800 concurrent users
# บน MacBook ที่มี RAM 8GB (ใช้อยู่แล้ว 4GB จาก apps อื่น)

# สิ่งที่ทีมทำ:
export JVM_ARGS="-Xms6g -Xmx6g"
cd apache-jmeter-5.6.3/bin
./jmeter  # เปิด GUI แล้วกด Run จาก GUI
```

<details>
<summary>ดูเฉลย (เปิดหลังคิดแล้วเท่านั้น)</summary>

**เฉลย — ปัญหาที่พบ:**

**ปัญหา 1: Heap size ใหญ่เกินไปสำหรับ available RAM**
ตั้ง `-Xmx6g` บนเครื่องที่มี RAM 8GB และใช้ไปแล้ว 4GB แปลว่า free RAM มีแค่ ~4GB แต่กำลังบอกให้ JVM ใช้ heap ถึง 6GB — JVM จะไม่สามารถ allocate ได้และ throw error ตั้งแต่ก่อน test เริ่ม

แก้: ลด heap ให้อยู่ใน available RAM ที่เหลือ เช่น `-Xms2g -Xmx2g` (เหลือ buffer ให้ OS และ JMeter process เอง)

**ปัญหา 2: รัน test ใน GUI mode**
บรรทัดสุดท้าย `./jmeter` เปิด GUI แล้วกด Run จาก GUI — ควรรัน CLI mode แทน:
```bash
./jmeter -n -t my_test.jmx -l results.jtl
```

**ผลถ้าไม่แก้:** ในกรณีที่ดีที่สุด JMeter จะ crash ทันทีเพราะ heap ใหญ่เกินไป ในกรณีที่แย่กว่า (ถ้า OS swap ได้) จะรันได้แต่ช้ามากจน result ไม่น่าเชื่อถือ

**Remediation:** ถ้าตอบไม่ครบ กลับอ่าน section 4.4 และ 4.5

</details>
