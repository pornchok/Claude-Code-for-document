# บทที่ 3 — Stub แรกของคุณ

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อนนะครับ:

- WireMock สร้าง folder อะไรให้อัตโนมัติเมื่อรันครั้งแรก?
- ถ้า port 8080 ถูกใช้งาน จะรัน WireMock บน port 9090 ด้วยคำสั่งอะไร?

---

??? note "เฉลย"

    - WireMock สร้าง `mappings/` และ `__files/` ให้อัตโนมัติ
    - `java -jar wiremock.jar --port 9090`

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:

- สร้าง JSON stub mapping file ได้ด้วยตัวเอง
- เขียน stub สำหรับ GET และ POST request ได้
- ทดสอบ stub ด้วย curl และ Postman ได้

---

## ทำไมต้องรู้? (Why)

Stub mapping file คือหัวใจของ WireMock ทุกครั้งที่ request เข้ามา WireMock จะเปิดไฟล์ใน `mappings/` ดูว่า request นี้ตรงกับ stub ไหน แล้ว return response ตามที่กำหนดไว้

เข้าใจ structure ของไฟล์นี้ = เข้าใจ WireMock 80%

---

## เนื้อหาหลัก

### โครงสร้าง JSON Stub

ทุก stub มีแค่ 2 ส่วนหลักเสมอ: *(source: wiremock.org/docs/stubbing/)*

```json
{
  "request": {
    "method": "GET",
    "url": "/api/products"
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "products": []
    },
    "headers": {
      "Content-Type": "application/json"
    }
  }
}
```

| Field | ความหมาย |
|-------|---------|
| `request.method` | HTTP method: GET, POST, PUT, DELETE |
| `request.url` | URL ที่จะ match (exact match) |
| `response.status` | HTTP status code ที่จะ return |
| `response.jsonBody` | Response body แบบ JSON (ไม่ต้อง escape) |
| `response.body` | Response body แบบ plain text |
| `response.headers` | HTTP headers ของ response |

### สร้าง Stub แรก: GET /api/products

สร้างไฟล์ `mappings/get-products.json`:

```json
{
  "request": {
    "method": "GET",
    "url": "/api/products"
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "products": [
        { "id": 1, "name": "iPhone 15", "price": 35000 },
        { "id": 2, "name": "Samsung S24", "price": 29000 }
      ]
    },
    "headers": {
      "Content-Type": "application/json"
    }
  }
}
```

restart WireMock แล้วทดสอบ:

```bash
curl http://localhost:8080/api/products
```

ผลที่ได้:

```json
{
  "products": [
    { "id": 1, "name": "iPhone 15", "price": 35000 },
    { "id": 2, "name": "Samsung S24", "price": 29000 }
  ]
}
```

### สร้าง Stub: POST /api/orders

สร้างไฟล์ `mappings/create-order.json`:

```json
{
  "request": {
    "method": "POST",
    "url": "/api/orders"
  },
  "response": {
    "status": 201,
    "jsonBody": {
      "orderId": "ORD-001",
      "status": "created",
      "message": "Order created successfully"
    },
    "headers": {
      "Content-Type": "application/json"
    }
  }
}
```

ทดสอบ:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 2}'
```

ผลที่ได้:

```json
{
  "orderId": "ORD-001",
  "status": "created",
  "message": "Order created successfully"
}
```

### Response body จากไฟล์แยก

ถ้า response body ยาวมาก ให้เก็บไว้ใน `__files/` แทน:

สร้างไฟล์ `__files/users-response.json`:

```json
{
  "users": [
    { "id": 1, "name": "สมชาย", "email": "somchai@example.com" },
    { "id": 2, "name": "สมหญิง", "email": "somying@example.com" }
  ]
}
```

แล้วในไฟล์ mapping ใช้ `bodyFileName`:

```json
{
  "request": {
    "method": "GET",
    "url": "/api/users"
  },
  "response": {
    "status": 200,
    "bodyFileName": "users-response.json",
    "headers": {
      "Content-Type": "application/json"
    }
  }
}
```

### เพิ่ม Stub ผ่าน Admin API (ไม่ต้อง restart)

ถ้าไม่อยาก restart WireMock ทุกครั้ง ใช้ Admin API ได้เลย:

```bash
curl -X POST http://localhost:8080/__admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "url": "/api/status"
    },
    "response": {
      "status": 200,
      "jsonBody": { "status": "ok" }
    }
  }'
```

stub จะ active ทันทีโดยไม่ต้อง restart

---

## ตัวอย่าง 3 ระดับ

### Beginner — Mock ระบบ login

สร้าง stub สำหรับ `POST /api/login` ที่ return token:

```json
{
  "request": {
    "method": "POST",
    "url": "/api/login"
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "token": "eyJhbGciOiJIUzI1NiJ9.mock-token",
      "expiresIn": 3600
    },
    "headers": {
      "Content-Type": "application/json"
    }
  }
}
```

### Intermediate — Mock ระบบธนาคาร

QA กำลังทดสอบแอป mobile banking ที่ต้องเรียก 3 API:
- GET /api/accounts → ดูยอดเงิน
- GET /api/transactions → ดูประวัติธุรกรรม
- POST /api/transfer → โอนเงิน

สร้าง 3 ไฟล์แยกกัน:

`mappings/get-accounts.json`:
```json
{
  "request": { "method": "GET", "url": "/api/accounts" },
  "response": {
    "status": 200,
    "jsonBody": {
      "accountNumber": "123-456-789",
      "balance": 50000.00,
      "currency": "THB"
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

`mappings/transfer-money.json`:
```json
{
  "request": { "method": "POST", "url": "/api/transfer" },
  "response": {
    "status": 200,
    "jsonBody": {
      "transactionId": "TXN-2024-001",
      "status": "success",
      "remainingBalance": 45000.00
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

### Advanced — Mock ที่ return response แตกต่างตาม ID

ใช้ `urlPattern` แทน `url` เพื่อ match URL แบบ dynamic:

```json
{
  "request": {
    "method": "GET",
    "urlPattern": "/api/products/[0-9]+"
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "id": 1,
      "name": "Generic Product",
      "price": 999
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

URL ที่ match: `/api/products/1`, `/api/products/42`, `/api/products/999`

---

## Common Mistakes

❌ **ใช้ `body` กับ JSON แล้วต้อง escape ทุกตัว**
`"body": "{\"name\": \"test\"}"` อ่านยาก และ error ง่าย
→ ✅ ใช้ `jsonBody` แทนเมื่อ response เป็น JSON จะไม่ต้อง escape เลย *(source: wiremock.org/docs/stubbing/)*

❌ **เพิ่ม JSON file ใน mappings/ แล้ว stub ไม่ทำงาน**
WireMock อ่านไฟล์ใน mappings/ แค่ตอน startup เท่านั้น
→ ✅ restart WireMock หรือ POST ผ่าน Admin API แทน *(source: wiremock.org/docs/standalone/)*

❌ **ตั้งชื่อไฟล์ใน mappings/ ผิด format**
ชื่อไฟล์ไม่มีผลต่อ WireMock แต่ถ้าตั้งชื่อสับสนจะ maintain ยาก
→ ✅ ตั้งชื่อตาม pattern: `[method]-[resource].json` เช่น `get-users.json`, `post-orders.json` *(best practice)*

---

## สรุปบท

**⏸ คำถาม Retrieval**

1. โครงสร้าง JSON ของ WireMock stub มี 2 ส่วนหลักคืออะไร และแต่ละส่วนทำหน้าที่อะไร?

2. ต่างกันอย่างไรระหว่างการใช้ `body` กับ `jsonBody` ใน response?

3. ถ้าต้องการเพิ่ม stub ใหม่โดยไม่ restart WireMock จะทำได้วิธีไหน?

---

??? note "เฉลย (คลิกเพื่อดู)"

    **ข้อ 1**: `request` — กำหนดเงื่อนไขที่จะ match กับ incoming request (method + URL) | `response` — กำหนดสิ่งที่จะ return กลับ (status + body + headers)

    **ข้อ 2**: `body` ต้องใส่เป็น string ทำให้ต้อง escape ตัวอักษรพิเศษ เช่น `"body": "{\"name\": \"test\"}"` ส่วน `jsonBody` รับ JSON object โดยตรง ไม่ต้อง escape ทำให้อ่านและแก้ไขง่ายกว่ามาก

    **ข้อ 3**: POST request ไปที่ `/__admin/mappings` พร้อม stub JSON ใน request body — stub จะ active ทันทีโดยไม่ต้อง restart
