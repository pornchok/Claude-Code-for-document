# บทที่ 2 — Setup WireMock Standalone

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อนนะครับ:

- Mock API คือ server ที่ทำอะไร และมันต่างจาก API จริงยังไง?
- ยกตัวอย่างหนึ่งสถานการณ์ที่ QA ควรใช้ mock แทนการรอ API จริง

---

??? note "เฉลย"

    - **Mock API** คือ server ปลอมที่เราสร้างเอง return response ที่เรากำหนดได้ ต่างจาก API จริงตรงที่ไม่มี business logic หรือ database อยู่เบื้องหลัง
    - **สถานการณ์ที่ควรใช้**: เช่น API ยังไม่เสร็จ, ต้องการทดสอบ error case, third-party API ล่มบ่อย

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:

- ติดตั้งและรัน WireMock Standalone ได้สำเร็จ
- อธิบาย folder structure ของ WireMock ได้
- เปลี่ยน port และ options พื้นฐานได้

---

## ทำไมต้องรู้? (Why)

WireMock Standalone คือโหมดที่ทำให้ QA ใช้ WireMock ได้โดยไม่ต้องเขียน Java เลย มันรันเป็น server แยกต่างหาก รับ HTTP request เหมือน API server ทั่วไป ทุกอย่างกำหนดผ่าน JSON และ command line เท่านั้น

---

## เนื้อหาหลัก

### Step 1: ตรวจสอบ Java

WireMock ต้องการ Java 11 ขึ้นไป เปิด terminal แล้วพิมพ์:

```bash
java -version
```

ผลที่ได้ควรเป็น:

```
openjdk version "11.0.x" ...
```

ถ้ายังไม่มี Java ให้ download จาก [https://adoptium.net](https://adoptium.net) แล้วติดตั้งตามปกติ

### Step 2: Download WireMock JAR

ไปที่ [https://wiremock.org/docs/download-and-installation/](https://wiremock.org/docs/download-and-installation/) แล้ว download `wiremock-standalone-3.13.2.jar`

หรือใช้คำสั่ง:

```bash
curl -o wiremock.jar \
  https://repo1.maven.org/maven2/org/wiremock/wiremock-standalone/3.13.2/wiremock-standalone-3.13.2.jar
```

### Step 3: สร้าง folder สำหรับโปรเจค

```bash
mkdir my-mocks
cd my-mocks
# วาง wiremock.jar ไว้ใน folder นี้
```

โครงสร้างที่ดีควรเป็น:

```
my-mocks/
├── wiremock.jar
├── mappings/        ← JSON stub files อยู่ที่นี่
└── __files/         ← response body files อยู่ที่นี่
```

WireMock จะสร้าง `mappings/` และ `__files/` ให้อัตโนมัติเมื่อรันครั้งแรก

### Step 4: รัน WireMock

```bash
java -jar wiremock.jar
```

ผลที่ได้:

```
 /$$      /$$ /$$                     /$$      /$$                     /$$
| $$  /$ | $$|__/                    | $$$    /$$$                    | $$
| $$ /$$$| $$ /$$  /$$$$$$   /$$$$$$ | $$$$  /$$$$  /$$$$$$   /$$$$$$$| $$   /$$
...
port:                         8080
enable-browser-proxying:      false
no-request-journal:           false
verbose:                      false
```

WireMock รันที่ **port 8080** โดย default *(source: wiremock.org/docs/standalone/java-jar/)*

### Step 5: ทดสอบว่า server ทำงาน

เปิด terminal อีกอันแล้วพิมพ์:

```bash
curl http://localhost:8080/__admin/mappings
```

ผลที่ได้:

```json
{
  "mappings": [],
  "meta": {
    "total": 0
  }
}
```

ถ้าเห็น response แบบนี้แสดงว่า WireMock ทำงานแล้ว ตอนนี้ยังไม่มี stub ใดๆ เลย

### Options ที่ใช้บ่อย

```bash
# เปลี่ยน port
java -jar wiremock.jar --port 9090

# เปิด verbose logging (เห็น request ที่เข้ามา)
java -jar wiremock.jar --verbose

# กำหนด root directory
java -jar wiremock.jar --root-dir /path/to/my-mocks

# รวมกัน
java -jar wiremock.jar --port 9090 --verbose --root-dir ./mocks
```

---

## ตัวอย่าง 3 ระดับ

### Beginner — รัน WireMock พื้นฐาน

ทดสอบว่า Admin API ทำงานได้จริง:

```bash
# Terminal 1: รัน server
java -jar wiremock.jar --port 8080 --verbose

# Terminal 2: ดู mappings ทั้งหมด
curl http://localhost:8080/__admin/mappings

# ผลที่ได้
{"mappings":[],"meta":{"total":0}}
```

### Intermediate — โปรเจคทีม QA

ทีม QA มีหลาย project ควรแยก folder ชัดเจน:

```
qa-mocks/
├── payment-service/
│   ├── wiremock.jar
│   ├── mappings/
│   └── __files/
└── user-service/
    ├── wiremock.jar
    ├── mappings/
    └── __files/
```

รัน payment service mock บน port 8081 และ user service บน port 8082:

```bash
# Terminal 1
cd qa-mocks/payment-service
java -jar wiremock.jar --port 8081

# Terminal 2
cd qa-mocks/user-service
java -jar wiremock.jar --port 8082
```

### Advanced — Script รัน WireMock อัตโนมัติ

สร้างไฟล์ `start-mocks.sh` เพื่อรัน mock server ทุกตัวพร้อมกัน:

```bash
#!/bin/bash
# start-mocks.sh

WIREMOCK_JAR="wiremock.jar"

echo "Starting Payment Service mock on port 8081..."
java -jar $WIREMOCK_JAR --port 8081 --root-dir ./payment-service &
PAYMENT_PID=$!

echo "Starting User Service mock on port 8082..."
java -jar $WIREMOCK_JAR --port 8082 --root-dir ./user-service &
USER_PID=$!

echo "All mocks started!"
echo "Payment PID: $PAYMENT_PID"
echo "User PID: $USER_PID"

# รอให้ user กด Ctrl+C แล้ว kill ทั้งหมด
trap "kill $PAYMENT_PID $USER_PID; echo 'Mocks stopped.'" EXIT
wait
```

```bash
chmod +x start-mocks.sh
./start-mocks.sh
```

---

## Common Mistakes

❌ **รัน WireMock แล้วได้ `Address already in use`**
port 8080 ถูกใช้งานอยู่แล้ว (อาจเป็น app อื่น)
→ ✅ ใช้ `--port 9090` หรือ port อื่นที่ว่างอยู่
*(source: wiremock.org/docs/standalone/java-jar/)*

❌ **ไฟล์ใน mappings/ ไม่โหลดเมื่อรัน**
ถ้าเพิ่ม JSON file เข้า mappings/ หลังจาก WireMock รันแล้ว stub จะไม่โหลดอัตโนมัติ
→ ✅ ต้อง restart WireMock หรือใช้ Admin API reload: `POST /__admin/mappings/reset`
*(source: wiremock.org/docs/standalone/)*

❌ **วาง JAR ผิด folder แล้วหา mappings/ ไม่เจอ**
WireMock สร้าง mappings/ ใน directory ที่ terminal อยู่ ณ ตอนนั้น ไม่ใช่ที่ที่ JAR อยู่เสมอไป
→ ✅ ใช้ `--root-dir` ระบุ path ชัดเจน หรือ `cd` เข้าไปใน project folder ก่อนรัน
*(source: wiremock.org/docs/standalone/java-jar/)*

---

## สรุปบท

**⏸ คำถาม Retrieval**

1. ทำไม WireMock Standalone ถึงเหมาะกับ QA ที่ไม่ได้เขียน Java?

2. หลังรัน `java -jar wiremock.jar` แล้ว จะรู้ได้อย่างไรว่า server พร้อมทำงาน?

3. ถ้า port 8080 ถูกใช้งานอยู่แล้ว จะรัน WireMock บน port 9191 ได้ยังไง?

---

??? note "เฉลย (คลิกเพื่อดู)"

    **ข้อ 1**: เพราะ Standalone mode ใช้ JSON files + command line เท่านั้น ไม่ต้องเขียน Java code เลย ทำให้ QA ทุกคนใช้ได้โดยไม่ต้องรู้ภาษา Java

    **ข้อ 2**: เรียก `curl http://localhost:8080/__admin/mappings` แล้วได้ JSON response กลับมา (แม้จะเป็น empty mappings) แสดงว่าพร้อมแล้ว

    **ข้อ 3**: `java -jar wiremock.jar --port 9191`
