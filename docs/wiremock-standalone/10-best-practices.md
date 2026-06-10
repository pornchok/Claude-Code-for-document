# บทที่ 10 — Best Practices สำหรับ QA Team

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อนนะครับ:

- Scenario ใน WireMock คืออะไร และแก้ปัญหาอะไรที่ stub ธรรมดาทำไม่ได้?
- ทำไมต้อง reset scenario state ก่อน test case แต่ละตัว?

---

??? note "เฉลย"

    - Scenario คือ state machine ทำให้ stub return response ต่างกันตาม state ปัจจุบัน — แก้ปัญหา stub ธรรมดาที่ return ค่าเดิมทุกครั้ง
    - เพราะ state ไม่ reset อัตโนมัติ — test แรก run ครบ workflow แล้ว state จะค้างให้ test ถัดไปเริ่มที่ state ผิด

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:

- จัดโครงสร้าง mock files ได้อย่างเป็นระเบียบและ maintain ได้
- เก็บ mock files ใน version control ได้ถูกต้อง
- แบ่งงาน mock ในทีมได้อย่างมีประสิทธิภาพ

---

## ทำไมต้องรู้? (Why)

Mock ที่ดีแต่จัดการไม่ดีจะกลายเป็นภาระ — ไม่มีใครรู้ว่า file ไหนทำอะไร ไม่มีใครกล้าแก้เพราะกลัวพัง ทั้งทีมทำ mock ซ้ำกัน

Best practices ในบทนี้ทำให้ WireMock กลายเป็น asset ที่ทั้งทีมใช้ร่วมกันได้อย่างยั่งยืน

---

## เนื้อหาหลัก

### 1. โครงสร้าง Folder ที่ดี

**ไม่ดี — ทุกอย่างรวมกันหมด:**

```
mappings/
├── stub1.json
├── test.json
├── new-stub.json
└── backup.json
```

**ดี — แยกตาม service และ scenario:**

```
mappings/
├── products/
│   ├── get-all-products.json
│   ├── get-product-by-id.json
│   └── post-create-product.json
├── orders/
│   ├── happy-path/
│   │   ├── get-order.json
│   │   └── post-create-order.json
│   └── error-scenarios/
│       ├── order-not-found.json
│       └── payment-failed.json
└── auth/
    ├── login-success.json
    └── login-expired-token.json
```

### 2. Naming Convention

ใช้ pattern: `[method]-[resource]-[scenario].json`

| ตัวอย่าง | ความหมาย |
|---------|---------|
| `get-products.json` | GET /api/products happy path |
| `get-products-empty.json` | GET /api/products คืน empty list |
| `post-orders-success.json` | POST /api/orders สำเร็จ |
| `post-orders-payment-failed.json` | POST /api/orders ที่ payment fail |
| `get-user-by-id-not-found.json` | GET /api/users/{id} ที่ไม่เจอ |

### 3. เก็บ Mock Files ใน Git

```
project-repo/
├── src/           ← production code
├── tests/         ← test code
└── mocks/         ← mock definitions (เก็บใน Git ด้วยกัน)
    ├── wiremock.jar
    ├── mappings/
    └── __files/
```

**สิ่งที่ควรเก็บใน Git:**
- ทุกไฟล์ใน `mappings/`
- ทุกไฟล์ใน `__files/`

**สิ่งที่ไม่ต้องเก็บ (เพิ่มใน .gitignore):**

```gitignore
# WireMock
wiremock-standalone-*.jar
```

เก็บ JAR ไว้ใน .gitignore เพราะ download ได้เอง และไฟล์ใหญ่ไม่ควรอยู่ใน Git

### 4. README สำหรับ Mock

สร้าง `mocks/README.md` อธิบาย:

```markdown
# Mock Server

## วิธีรัน

```bash
java -jar wiremock.jar --port 8080 --verbose
```

## Endpoints ที่มี

| Method | URL | Scenario | File |
|--------|-----|---------|------|
| GET | /api/products | Happy path | mappings/products/get-all-products.json |
| POST | /api/orders | Success | mappings/orders/happy-path/post-create-order.json |
| POST | /api/orders | Payment failed | mappings/orders/error-scenarios/post-orders-payment-failed.json |

## ถ้า port 8080 ถูกใช้งาน

```bash
java -jar wiremock.jar --port 9090
```

แล้วเปลี่ยน `WIREMOCK_URL` ใน test config ด้วย
```

### 5. แบ่งงานใน Team

**รูปแบบที่ดี:**

- QA แต่ละคน responsible ต่อ mock ของ feature ที่ตัวเองทดสอบ
- ทำ code review mock files เหมือน review code ปกติ
- ถ้า API เปลี่ยน spec → QA เจ้าของ feature อัปเดต mock ด้วย

**การ review mock file ควรดู:**
- URL ตรงกับ API spec จริงไหม?
- Response body มี field ครบตาม spec ไหม?
- มี error scenarios ที่จำเป็นครบไหม?

### 6. Sync Mock กับ API ที่เปลี่ยน

เมื่อ dev เปลี่ยน API spec ต้องอัปเดต mock ด้วย วิธีที่ดีที่สุดคือใช้ **Contract Testing** เป็น safety net:

```
API Spec (Swagger/OpenAPI)
        ↓
Contract Test ตรวจว่า mock ตรงกับ spec
        ↓
ถ้าไม่ตรง → fail CI → แจ้ง QA ให้อัปเดต mock
```

### 7. Mock สำหรับ Different Environments

สร้าง folder แยกตาม environment:

```
mocks/
├── local/
│   └── mappings/
├── staging/
│   └── mappings/
└── shared/        ← stubs ที่ใช้ทุก environment เหมือนกัน
    └── mappings/
```

รัน:
```bash
# Local
java -jar wiremock.jar --root-dir mocks/local

# Staging (port ต่างกัน)
java -jar wiremock.jar --port 9090 --root-dir mocks/staging
```

---

## ตัวอย่าง 3 ระดับ

### Beginner — Setup โปรเจคครั้งแรก

```
my-project/
├── tests/
├── mocks/
│   ├── README.md          ← อธิบาย endpoint ทั้งหมด
│   ├── mappings/
│   │   ├── auth/
│   │   └── products/
│   └── __files/
└── .gitignore             ← เพิ่ม wiremock-standalone-*.jar
```

### Intermediate — Team Workflow

1. Dev สร้าง API spec ใหม่ใน Swagger
2. QA อ่าน spec แล้วสร้าง stub ก่อน backend เสร็จ
3. QA เขียน test โดยใช้ stub
4. Dev ทำ backend เสร็จ → QA ทดสอบกับของจริง
5. ถ้า mock กับของจริงต่างกัน → อัปเดต mock ให้ตรง

### Advanced — Automated Mock Validation

เพิ่มขั้นตอนใน CI pipeline ตรวจว่า mock ยังตรงกับ API spec:

`validate-mocks.sh`:
```bash
#!/bin/bash
# Start WireMock
java -jar mocks/wiremock.jar --port 8099 --root-dir mocks &
sleep 2

# Run contract tests
python -m pytest tests/contract/ --base-url=http://localhost:8099

STATUS=$?

# Stop WireMock
kill $(lsof -t -i:8099)

exit $STATUS
```

ใส่ใน CI pipeline:
```yaml
# .github/workflows/test.yml
- name: Validate Mock Contracts
  run: ./validate-mocks.sh
```

---

## Common Mistakes

❌ **ไม่ update mock เมื่อ API เปลี่ยน**
test ยังผ่านอยู่ทั้งที่ integration จริงพัง เพราะ mock ยังเป็น version เก่า
→ ✅ ทำ PR process ให้ QA update mock พร้อมกับที่ dev เปลี่ยน API *(best practice)*

❌ **ทุกคนใน team มี mock คนละ version ในเครื่องตัวเอง**
test ผ่านในเครื่อง QA A แต่ fail ในเครื่อง QA B
→ ✅ เก็บ mock files ใน Git repository เดียวกัน ทุกคน pull มาใช้ version เดียวกัน *(best practice)*

❌ **Mock มีแต่ happy path ไม่มี error scenarios**
test suite ดู coverage สวยงามแต่ไม่ได้ทดสอบว่าแอปรับมือ error ได้
→ ✅ ทุก endpoint ควรมีอย่างน้อย 1 happy path + 1 error scenario *(best practice)*

---

## สรุปบท

**⏸ คำถาม Retrieval**

1. ทำไม mock files ถึงควรเก็บใน Git repository เดียวกับ test code?

2. เมื่อ dev เปลี่ยน API response format QA ควรทำอะไร?

3. ออกแบบโครงสร้าง folder สำหรับโปรเจคที่มี 3 service (auth, orders, notifications) โดยแต่ละ service มีทั้ง happy path และ error scenarios

---

??? note "เฉลย (คลิกเพื่อดู)"

    **ข้อ 1**: เพื่อให้ทุกคนใน team ใช้ mock version เดียวกัน — ป้องกัน "test ผ่านในเครื่องฉัน แต่ fail ในเครื่องคุณ" และทำให้ track การเปลี่ยนแปลงได้ผ่าน git history

    **ข้อ 2**: อัปเดต mock ให้ตรงกับ spec ใหม่ แล้ว run test ใหม่เพื่อดูว่า test ที่มีอยู่ยังผ่านหรือต้องแก้ด้วย

    **ข้อ 3**: ตัวอย่าง:
    ```
    mappings/
    ├── auth/
    │   ├── happy-path/
    │   └── error-scenarios/
    ├── orders/
    │   ├── happy-path/
    │   └── error-scenarios/
    └── notifications/
        ├── happy-path/
        └── error-scenarios/
    ```
