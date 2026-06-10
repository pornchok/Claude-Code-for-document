# บทที่ 6 — Error & Fault Simulation

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อนนะครับ:

- จะสร้าง stub ที่ delay 2 วินาทีก่อน return response ได้ยังไง?
- ต้องทำอะไรก่อนถึงจะใช้ `{{now}}` ใน response body ได้?

---

??? note "เฉลย"

    - เพิ่ม `"fixedDelayMilliseconds": 2000` ใน response object
    - รัน WireMock ด้วย `--global-response-templating` หรือเพิ่ม `"transformers": ["response-template"]` ใน response

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:

- จำลอง HTTP error response ได้ (4xx, 5xx)
- จำลอง network fault ได้ (connection reset, empty response)
- ออกแบบ test scenario สำหรับ negative testing ได้

---

## ทำไมต้องรู้? (Why)

Negative testing คือหัวใจของงาน QA — เราต้องทดสอบว่าแอปรับมือกับสถานการณ์เลวร้ายได้ดีแค่ไหน

ปัญหาคือในสภาพแวดล้อมทดสอบปกติ เราทำให้ API พังหรือ network ขาดได้ยากมาก WireMock แก้ปัญหานี้ได้ทันที — เราควบคุม "ความเลวร้าย" ได้เองทุกอย่าง

---

## เนื้อหาหลัก

### HTTP Error Responses

**400 Bad Request:**

```json
{
  "request": {
    "method": "POST",
    "urlPath": "/api/register"
  },
  "response": {
    "status": 400,
    "jsonBody": {
      "error": "Bad Request",
      "details": [
        { "field": "email", "message": "Invalid email format" },
        { "field": "password", "message": "Password must be at least 8 characters" }
      ]
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

**500 Internal Server Error:**

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/orders/crash"
  },
  "response": {
    "status": 500,
    "jsonBody": {
      "error": "Internal Server Error",
      "message": "An unexpected error occurred",
      "traceId": "abc-123-xyz"
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

**503 Service Unavailable** (จำลองเมื่อ server ไม่พร้อม):

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/payments/status"
  },
  "response": {
    "status": 503,
    "jsonBody": {
      "error": "Service Unavailable",
      "message": "Payment service is temporarily unavailable"
    },
    "headers": {
      "Content-Type": "application/json",
      "Retry-After": "30"
    }
  }
}
```

### Network Faults

นอกจาก HTTP errors ยังจำลอง network-level fault ได้ด้วย: *(source: wiremock.org/docs/simulating-faults/)*

**Connection Reset** — จำลองว่า server ตัด connection กะทันหัน:

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/unstable"
  },
  "response": {
    "fault": "CONNECTION_RESET_BY_PEER"
  }
}
```

**Empty Response** — server รับ connection แต่ไม่ส่งอะไรกลับมาเลย:

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/silent"
  },
  "response": {
    "fault": "EMPTY_RESPONSE"
  }
}
```

Fault types ทั้งหมด:

| Fault | จำลองอะไร | ใช้ทดสอบอะไร |
|-------|---------|-------------|
| `CONNECTION_RESET_BY_PEER` | server ตัด connection กะทันหัน | retry logic, error handling |
| `EMPTY_RESPONSE` | server รับ connection แต่ไม่ส่งอะไร | timeout handling |
| `MALFORMED_RESPONSE_CHUNK` | ส่ง response บางส่วนแล้วส่ง garbage | partial response handling |
| `RANDOM_DATA_THEN_CLOSE` | ส่ง random data แล้วปิด connection | data parsing error handling |

### Timeout Simulation

จำลอง API ที่ใช้เวลานานจนเกิน timeout:

```json
{
  "request": {
    "method": "POST",
    "urlPath": "/api/process"
  },
  "response": {
    "status": 200,
    "jsonBody": { "result": "done" },
    "headers": { "Content-Type": "application/json" },
    "fixedDelayMilliseconds": 30000
  }
}
```

delay 30 วินาที — ถ้า client มี timeout แค่ 10 วินาทีจะเกิด timeout error

---

## ตัวอย่าง 3 ระดับ

### Beginner — ทดสอบ validation error message

QA ต้องทดสอบว่าหน้า registration แสดง error message ถูกต้องเมื่อ email ซ้ำ:

```json
{
  "request": {
    "method": "POST",
    "urlPath": "/api/register",
    "bodyPatterns": [
      { "matchesJsonPath": "$[?(@.email == 'existing@email.com')]" }
    ]
  },
  "response": {
    "status": 409,
    "jsonBody": {
      "error": "Conflict",
      "message": "Email already registered"
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

### Intermediate — ทดสอบ retry mechanism

แอปต้องลอง retry 3 ครั้งเมื่อ API return 503 ใน Scenario นี้ stub ตัวแรก return 503 เพื่อให้ทดสอบ retry:

`mappings/flaky-api.json`:
```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/flaky"
  },
  "response": {
    "status": 503,
    "jsonBody": {
      "error": "Service temporarily unavailable"
    },
    "headers": {
      "Content-Type": "application/json",
      "Retry-After": "1"
    }
  }
}
```

ทดสอบว่า client code retry และแสดง error message ถูกต้องหลัง retry ครบ 3 ครั้ง

### Advanced — จำลอง Cascading Failure

ระบบ microservice ที่ payment service ล่ม แล้วทำให้ order service ล้มตาม:

`mappings/payment-down.json`:
```json
{
  "request": {
    "method": "POST",
    "urlPath": "/api/payments/charge"
  },
  "response": {
    "fault": "CONNECTION_RESET_BY_PEER"
  }
}
```

`mappings/order-payment-failed.json`:
```json
{
  "request": {
    "method": "POST",
    "urlPath": "/api/orders",
    "bodyPatterns": [
      { "matchesJsonPath": "$[?(@.paymentMethod == 'online')]" }
    ]
  },
  "response": {
    "status": 503,
    "jsonBody": {
      "error": "Service Unavailable",
      "message": "Payment service is unavailable. Please try again later.",
      "fallbackOptions": ["cash_on_delivery", "bank_transfer"]
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

ทดสอบว่า UI แสดงตัวเลือก fallback ให้ user ได้ถูกต้อง

---

## Common Mistakes

❌ **ใช้ 500 error สำหรับทุก error case**
500 หมายถึง "server bug" ไม่ใช่ "user ทำผิด" — ถ้าใช้ 500 ทุกที่ developer อาจแก้ code ผิดทาง
→ ✅ เลือก status code ให้ถูกต้อง: validation error = 400/422, not found = 404, duplicate = 409, no permission = 403 *(best practice)*

❌ **Test fault แล้ว hang ไม่สิ้นสุด**
`EMPTY_RESPONSE` ทำให้ client รอ response ตลอดถ้าไม่มี timeout
→ ✅ ตั้ง timeout ที่ client side เสมอก่อน test fault scenarios *(best practice)*

❌ **ลืมทดสอบ error message ที่แสดงต่อ user**
test แค่ว่า status code ถูกต้อง แต่ไม่ check ว่า UI แสดงข้อความที่ user เข้าใจได้
→ ✅ เช็คทั้ง status code และ UI error message ทุกครั้ง *(best practice)*

---

## สรุปบท

**⏸ คำถาม Retrieval**

1. Network fault ต่างจาก HTTP error response อย่างไร? ยกตัวอย่างว่าแต่ละแบบทดสอบอะไรได้บ้าง

2. ทดสอบ retry mechanism ของแอปได้อย่างไรโดยใช้ WireMock?

3. ถ้าต้องการทดสอบว่าแอป handle connection timeout ได้ถูกต้อง จะออกแบบ stub ยังไง?

---

??? note "เฉลย (คลิกเพื่อดู)"

    **ข้อ 1**: HTTP error (4xx, 5xx) คือ response ปกติที่ server ส่งกลับมาพร้อม error code — client รู้ว่าได้รับ response แล้ว | Network fault คือ connection level ปัญหา — client ไม่ได้รับ HTTP response เลย ใช้ทดสอบ error handling / retry ที่ transport layer

    **ข้อ 2**: สร้าง stub ที่ return 503 แล้วดูว่า client retry กี่ครั้งและแสดงผลอย่างไร (อาจจับคู่กับ Scenario/stateful mock ถ้าต้องการให้ retry ครั้งที่ N สำเร็จ — จะเรียนในบท 9)

    **ข้อ 3**: ใช้ `"fixedDelayMilliseconds"` ที่มากกว่า timeout ของ client เช่น client timeout 5 วินาที → ตั้ง delay 10 วินาที แล้วดูว่า client จัดการ timeout ถูกต้องหรือเปล่า
