# บทที่ 5 — Response Control

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อนนะครับ:

- `url` กับ `urlPath` ต่างกันอย่างไร?
- ถ้า test ส่ง request มาพร้อม query string แต่ stub ใช้ `url` ผลจะเป็นอย่างไร?

---

??? note "เฉลย"

    - `url` match ทั้ง path + query string ต้องตรงพอดี | `urlPath` match เฉพาะ path ไม่สนใจ query string
    - WireMock จะ return 404 เพราะไม่เจอ stub ที่ match

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:

- ควบคุม HTTP status code ของ response ได้
- กำหนด response headers ได้
- จำลอง delay เพื่อทดสอบ slow API ได้

---

## ทำไมต้องรู้? (Why)

QA ไม่ได้ทดสอบแค่ happy path การควบคุม response ได้อย่างละเอียดทำให้เราจำลองสถานการณ์ต่างๆ ได้:

- API ช้า (delay 5 วินาที) → ทดสอบว่า UI แสดง loading spinner ถูกไหม?
- API return 401 → ทดสอบว่าแอป redirect ไปหน้า login ถูกไหม?
- API return data format ผิด → ทดสอบว่าแอปไม่ crash ถูกไหม?

ทั้งหมดนี้ทำได้ด้วย Response Control

---

## เนื้อหาหลัก

### Status Codes

กำหนด status code ใดก็ได้:

```json
{
  "request": { "method": "GET", "url": "/api/resource" },
  "response": {
    "status": 404,
    "jsonBody": {
      "error": "Not Found",
      "message": "Resource does not exist"
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

Status codes ที่ใช้บ่อยใน QA:

| Code | ความหมาย | เมื่อไหร่ที่ทดสอบ |
|------|---------|-----------------|
| 200 | OK | Happy path |
| 201 | Created | สร้าง resource สำเร็จ |
| 400 | Bad Request | ส่ง data ไม่ครบหรือ format ผิด |
| 401 | Unauthorized | ไม่มี token หรือ token หมดอายุ |
| 403 | Forbidden | มี token แต่ไม่มีสิทธิ์ |
| 404 | Not Found | ไม่เจอ resource ที่ขอ |
| 422 | Unprocessable | Validation error |
| 500 | Internal Server Error | Server พัง |
| 503 | Service Unavailable | Server ไม่พร้อมให้บริการ |

### Response Headers

```json
{
  "request": { "method": "GET", "urlPath": "/api/data" },
  "response": {
    "status": 200,
    "jsonBody": { "data": "value" },
    "headers": {
      "Content-Type": "application/json",
      "X-Request-Id": "mock-req-001",
      "Cache-Control": "no-cache",
      "X-Rate-Limit-Remaining": "99"
    }
  }
}
```

### Delay — จำลอง Slow API

**Fixed delay** — delay คงที่ทุก request: *(source: wiremock.org/docs/simulating-faults/)*

```json
{
  "request": { "method": "GET", "urlPath": "/api/slow-endpoint" },
  "response": {
    "status": 200,
    "jsonBody": { "data": "finally here" },
    "headers": { "Content-Type": "application/json" },
    "fixedDelayMilliseconds": 3000
  }
}
```

request นี้จะใช้เวลา 3 วินาทีก่อน return response

**Random delay** — delay แบบสุ่มเพื่อจำลอง real-world latency:

```json
{
  "request": { "method": "GET", "urlPath": "/api/variable-speed" },
  "response": {
    "status": 200,
    "jsonBody": { "result": "ok" },
    "headers": { "Content-Type": "application/json" },
    "delayDistribution": {
      "type": "lognormal",
      "median": 200,
      "sigma": 0.4
    }
  }
}
```

delay จะสุ่มตาม lognormal distribution โดยมีค่ากลางอยู่ที่ 200ms

### Response Templating — Response แบบ Dynamic

เปิด templating ได้ด้วย `--global-response-templating` flag:

```bash
java -jar wiremock.jar --global-response-templating
```

แล้วใช้ template variables ใน response:

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/echo"
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "yourRequest": "{{request.url}}",
      "timestamp": "{{now format='yyyy-MM-dd HH:mm:ss'}}",
      "requestId": "{{randomValue type='UUID'}}"
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

response จะแตกต่างกันทุกครั้ง เช่น:

```json
{
  "yourRequest": "/api/echo",
  "timestamp": "2024-01-15 10:30:45",
  "requestId": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## ตัวอย่าง 3 ระดับ

### Beginner — Mock ระบบ authentication

สร้าง stub สำหรับ token หมดอายุ:

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/profile",
    "headers": {
      "Authorization": { "equalTo": "Bearer expired-token" }
    }
  },
  "response": {
    "status": 401,
    "jsonBody": {
      "error": "Unauthorized",
      "message": "Token has expired"
    },
    "headers": {
      "Content-Type": "application/json",
      "WWW-Authenticate": "Bearer error=invalid_token"
    }
  }
}
```

### Intermediate — Mock ระบบที่มี rate limiting

จำลอง API ที่มี rate limit header และ 429 response เมื่อ exceed:

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/data"
  },
  "response": {
    "status": 429,
    "jsonBody": {
      "error": "Too Many Requests",
      "retryAfter": 60
    },
    "headers": {
      "Content-Type": "application/json",
      "Retry-After": "60",
      "X-Rate-Limit-Limit": "100",
      "X-Rate-Limit-Remaining": "0",
      "X-Rate-Limit-Reset": "1705312245"
    }
  }
}
```

### Advanced — Mock ระบบที่ช้าสำหรับ performance test

สร้าง stub ที่จำลองความหน่วงของ database query จริง:

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/reports/monthly"
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "reportId": "RPT-2024-01",
      "generatedAt": "{{now format='yyyy-MM-dd'}}",
      "totalRevenue": 1250000,
      "totalOrders": 3421
    },
    "headers": { "Content-Type": "application/json" },
    "delayDistribution": {
      "type": "lognormal",
      "median": 800,
      "sigma": 0.6
    }
  }
}
```

delay จะอยู่ระหว่าง ~400-2000ms จำลอง report generation จริง

---

## Common Mistakes

❌ **ลืมใส่ `Content-Type` header ใน response**
หลาย frontend framework จะไม่ parse response เป็น JSON ถ้าไม่มี Content-Type header ที่ถูกต้อง
→ ✅ ใส่ `"Content-Type": "application/json"` ทุกครั้งเมื่อ response เป็น JSON *(best practice)*

❌ **ใช้ delay นานเกินจนทำให้ test timeout**
delay 30 วินาที แต่ test framework มี timeout แค่ 5 วินาที → test fail ด้วยเหตุผลที่ไม่ใช่ logic ที่ต้องทดสอบ
→ ✅ ปรับ delay ให้น้อยกว่า timeout ของ test framework และตั้ง timeout ของ test ให้เหมาะสม *(best practice)*

❌ **ลืมเปิด `--global-response-templating` แล้ว template ไม่ทำงาน**
`{{request.url}}` จะ return ค่า literal string แทนที่จะ render เป็นค่าจริง
→ ✅ รัน WireMock ด้วย `--global-response-templating` หรือเพิ่ม `"transformers": ["response-template"]` ใน response *(source: wiremock.org/docs/response-templating/)*

---

## สรุปบท

**⏸ คำถาม Retrieval**

1. QA ต้องทดสอบว่าแอปแสดง "กรุณาล็อกอินใหม่" เมื่อ token หมดอายุ จะสร้าง stub ยังไง?

2. `fixedDelayMilliseconds` กับ `delayDistribution` ต่างกันยังไง และควรใช้อันไหนเมื่อไหร่?

3. ต้องทำอะไรก่อนถึงจะใช้ `{{now}}` ใน response body ได้?

---

??? note "เฉลย (คลิกเพื่อดู)"

    **ข้อ 1**: กำหนด `"status": 401` ใน response และอาจเพิ่ม body `{"error": "Token expired"}` กับ match header `Authorization` ที่มี expired token

    **ข้อ 2**: `fixedDelayMilliseconds` delay คงที่ทุกครั้ง เหมาะกับ test ที่ต้องการผลลัพธ์แน่นอน | `delayDistribution` delay สุ่มตาม distribution เหมาะกับจำลอง real-world latency ที่ไม่สม่ำเสมอ

    **ข้อ 3**: รัน WireMock ด้วย flag `--global-response-templating` หรือเพิ่ม `"transformers": ["response-template"]` เข้าไปใน response object ของ stub
