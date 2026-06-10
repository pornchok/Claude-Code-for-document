# บทที่ 4 — Request Matching

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อนนะครับ:

- โครงสร้าง JSON ของ WireMock stub มีกี่ส่วนหลัก และแต่ละส่วนคืออะไร?
- จะเพิ่ม stub ใหม่โดยไม่ restart WireMock ได้วิธีไหน?

---

??? note "เฉลย"

    - มี 2 ส่วน: `request` (กำหนดเงื่อนไข match) และ `response` (กำหนดสิ่งที่ return กลับ)
    - POST ไปที่ `/__admin/mappings` ด้วย stub JSON ใน body

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:

- ใช้ URL matching แบบต่างๆ ได้ (exact, pattern, path)
- Match request ด้วย query parameters และ headers ได้
- Match request ด้วย request body ได้

---

## ทำไมต้องรู้? (Why)

ในงานจริง request ไม่ได้มา exact URL เสมอไป บางทีมี query string ต่อท้าย บางทีต้องดู header เพื่อแยกแยะ user บางทีต้องดู body เพื่อรู้ว่าสั่งซื้ออะไร

Request Matching คือการควบคุมว่า "stub นี้จะตอบสนองเมื่อ request มีลักษณะแบบไหน" — ยิ่ง match ละเอียด ยิ่งควบคุม test scenario ได้แม่นยำ

---

## เนื้อหาหลัก

### URL Matching

**1. Exact URL** — match ตรงๆ รวม query string

```json
{
  "request": {
    "method": "GET",
    "url": "/api/users?role=admin"
  }
}
```

URL ที่ match: `/api/users?role=admin` เท่านั้น

**2. URL Path** — match เฉพาะ path ไม่สนใจ query string *(source: wiremock.org/docs/request-matching/)*

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/users"
  }
}
```

URL ที่ match: `/api/users`, `/api/users?role=admin`, `/api/users?page=2&limit=10`

**3. URL Pattern** — ใช้ regex

```json
{
  "request": {
    "method": "GET",
    "urlPattern": "/api/users/[0-9]+"
  }
}
```

URL ที่ match: `/api/users/1`, `/api/users/42`, `/api/users/999`

### Query Parameter Matching

ใช้คู่กับ `urlPath` เมื่อต้องการ match query string แบบเจาะจง:

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/products",
    "queryParameters": {
      "category": {
        "equalTo": "electronics"
      },
      "inStock": {
        "equalTo": "true"
      }
    }
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "products": [
        { "id": 1, "name": "Laptop", "category": "electronics" }
      ]
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

URL ที่ match: `/api/products?category=electronics&inStock=true`

Query parameter matchers อื่นๆ:

| Matcher | ความหมาย | ตัวอย่าง |
|---------|---------|---------|
| `equalTo` | ค่าตรงกันพอดี | `"equalTo": "admin"` |
| `contains` | มีค่านี้อยู่ใน string | `"contains": "john"` |
| `matches` | match regex | `"matches": "[0-9]+"` |
| `doesNotMatch` | ไม่ match regex | `"doesNotMatch": "test"` |

### Header Matching

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/secure-data",
    "headers": {
      "Authorization": {
        "contains": "Bearer"
      },
      "Accept": {
        "equalTo": "application/json"
      }
    }
  },
  "response": {
    "status": 200,
    "jsonBody": { "data": "secret stuff" },
    "headers": { "Content-Type": "application/json" }
  }
}
```

### Body Matching

สำหรับ POST/PUT request ที่มี request body:

**Match ทั้ง JSON body:**

```json
{
  "request": {
    "method": "POST",
    "urlPath": "/api/login",
    "bodyPatterns": [
      {
        "equalToJson": {
          "username": "admin",
          "password": "secret"
        }
      }
    ]
  },
  "response": {
    "status": 200,
    "jsonBody": { "token": "mock-token-123" },
    "headers": { "Content-Type": "application/json" }
  }
}
```

**Match เฉพาะ field ที่สนใจ (ใช้ JSON path):**

```json
{
  "request": {
    "method": "POST",
    "urlPath": "/api/orders",
    "bodyPatterns": [
      {
        "matchesJsonPath": "$.items[?(@.quantity > 0)]"
      }
    ]
  }
}
```

---

## ตัวอย่าง 3 ระดับ

### Beginner — Mock search API

API ค้นหาสินค้าตาม keyword:

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/search",
    "queryParameters": {
      "q": { "equalTo": "laptop" }
    }
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "results": [
        { "id": 1, "name": "Laptop Pro", "price": 45000 }
      ],
      "total": 1
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

ทดสอบ:
```bash
curl "http://localhost:8080/api/search?q=laptop"
```

### Intermediate — Mock API ที่ต้องการ Authorization header

ระบบ QA ต้องทดสอบว่า frontend ส่ง token ถูกต้อง:

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/profile",
    "headers": {
      "Authorization": {
        "matches": "Bearer .+"
      }
    }
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "userId": "U001",
      "name": "Test User",
      "email": "test@example.com"
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

ถ้า request ไม่มี Authorization header หรือ format ผิด WireMock จะ return 404 (ไม่เจอ stub ที่ match)

### Advanced — Mock ที่แยก response ตาม body content

สร้าง 2 stubs สำหรับ payment API — แยกตาม payment method:

`mappings/payment-credit-card.json`:
```json
{
  "request": {
    "method": "POST",
    "urlPath": "/api/payments",
    "bodyPatterns": [
      { "matchesJsonPath": "$[?(@.method == 'credit_card')]" }
    ]
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "transactionId": "CC-001",
      "method": "credit_card",
      "status": "approved"
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

`mappings/payment-promptpay.json`:
```json
{
  "request": {
    "method": "POST",
    "urlPath": "/api/payments",
    "bodyPatterns": [
      { "matchesJsonPath": "$[?(@.method == 'promptpay')]" }
    ]
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "transactionId": "PP-001",
      "method": "promptpay",
      "qrCode": "00020101021153..."
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

---

## Common Mistakes

❌ **ใช้ `url` แต่ test ส่ง query string เพิ่มมาทำให้ 404**
`"url": "/api/users"` จะไม่ match `/api/users?page=1`
→ ✅ ใช้ `urlPath` แทนเมื่อ URL มี query parameter *(source: wiremock.org/docs/request-matching/)*

❌ **ลืม match header แล้ว stub ตอบทุก request โดยไม่แยก user**
stub ที่ไม่ check header จะตอบสนองทุก request ไม่ว่าจะมี token หรือไม่
→ ✅ เพิ่ม `headers` matching เมื่อต้องการ test authentication flow *(source: wiremock.org/docs/request-matching/)*

❌ **ใช้ `equalToJson` แต่ body มี field เพิ่มเติมทำให้ไม่ match**
`equalToJson` ต้องการให้ JSON ตรงกันทุก field โดย default
→ ✅ เพิ่ม `"ignoreExtraElements": true` เพื่อให้ match แม้มี field เพิ่มมา เช่น `{ "equalToJson": {...}, "ignoreExtraElements": true }` *(source: wiremock.org/docs/request-matching/)*

---

## สรุปบท

**⏸ คำถาม Retrieval**

1. `url` กับ `urlPath` ต่างกันอย่างไร? และควรใช้อันไหนเมื่อไหร่?

2. ถ้าต้องการ stub ที่ match เฉพาะ request ที่มี header `X-API-Key` อยู่ จะเขียน JSON ยังไง?

3. มี 2 stubs ที่ match URL เดียวกัน WireMock จะเลือก stub ไหนตอบ?

---

??? note "เฉลย (คลิกเพื่อดู)"

    **ข้อ 1**: `url` match ทั้ง path + query string (ต้องตรงทุกตัวอักษร) | `urlPath` match เฉพาะ path ไม่สนใจ query string — ควรใช้ `urlPath` เมื่อ URL มีหรืออาจมี query parameter

    **ข้อ 2**:
    ```json
    "headers": {
      "X-API-Key": { "equalTo": "ค่า key ที่ต้องการ" }
    }
    ```
    หรือถ้าแค่ต้องการให้ header นั้นมีอยู่ (ไม่สน value): `"X-API-Key": { "matches": ".+" }`

    **ข้อ 3**: WireMock ใช้ **priority** ตัดสิน (ค่าต่ำ = priority สูง) ถ้าไม่ได้กำหนด priority ไว้ WireMock จะใช้ stub ที่ match แบบ "specific" กว่า (มีเงื่อนไขมากกว่า) ก่อน
