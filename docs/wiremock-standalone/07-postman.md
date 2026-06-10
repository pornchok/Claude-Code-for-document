# บทที่ 7 — ใช้ WireMock กับ Postman

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อนนะครับ:

- Network fault กับ HTTP error response ต่างกันอย่างไร?
- จะจำลอง API timeout ด้วย WireMock ได้อย่างไร?

---

??? note "เฉลย"

    - HTTP error = server ส่ง response กลับมาพร้อม error status code (client รับได้) | Network fault = connection level problem, client ไม่ได้รับ HTTP response เลย
    - ใช้ `"fixedDelayMilliseconds"` ตั้งให้มากกว่า timeout ของ client

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:

- ทดสอบ WireMock stub ผ่าน Postman ได้
- ตั้ง environment variable สำหรับ base URL ได้
- จัดกลุ่ม request เป็น collection ได้

---

## ทำไมต้องรู้? (Why)

Postman เป็น tool ที่ QA ใช้อยู่แล้วในชีวิตประจำวัน การเอา WireMock มาใช้ร่วมกับ Postman ทำให้เราได้ประโยชน์ 2 ด้านพร้อมกัน:

- **ทดสอบ stub** ได้ทันทีว่า WireMock return response ถูกต้องหรือเปล่า
- **เอกสาร** — Postman collection เป็น reference ให้ทีมรู้ว่า mock มี endpoint อะไรบ้าง

---

## เนื้อหาหลัก

### ตั้งค่า Environment ใน Postman

แทนที่จะพิมพ์ `http://localhost:8080` ทุก request ให้ตั้ง environment variable แทน:

1. Postman → **Environments** → **Add**
2. ตั้งชื่อ environment เช่น `WireMock Local`
3. เพิ่ม variable:

| Variable | Initial Value | Current Value |
|----------|--------------|---------------|
| `baseUrl` | `http://localhost:8080` | `http://localhost:8080` |

4. Save แล้ว **Select** environment นั้นที่มุมบนขวา

ตอนนี้ทุก request ใช้ `{{baseUrl}}` แทน URL เต็มได้เลย:

```
GET {{baseUrl}}/api/products
```

### สร้าง Collection สำหรับ Mock API

1. **New Collection** → ตั้งชื่อ เช่น `Payment Service Mock`
2. เพิ่ม request แรก: `GET {{baseUrl}}/api/products`
3. กด **Send** — ถ้า WireMock รันอยู่จะได้ response กลับมา

### เขียน Test Script ใน Postman

Postman มี Test tab ที่ให้เขียน JavaScript verify response ได้:

```javascript
// Tests tab ใน Postman request
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has products array", function () {
    const body = pm.response.json();
    pm.expect(body).to.have.property("products");
    pm.expect(body.products).to.be.an("array");
});

pm.test("Response time is acceptable", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});
```

### ทดสอบ Error Scenarios

สร้าง request แยกสำหรับแต่ละ error scenario แล้วจัดเป็น folder:

```
Payment Service Mock/
├── Happy Path/
│   ├── GET Products
│   ├── POST Create Order
│   └── GET Order Status
└── Error Scenarios/
    ├── 401 Unauthorized
    ├── 404 Not Found
    ├── 500 Server Error
    └── Timeout (3s delay)
```

### Collection Runner — รัน Test ทั้งหมดพร้อมกัน

1. เปิด Collection → กด **Run collection**
2. เลือก environment `WireMock Local`
3. กด **Run** → Postman จะรัน request ทุกตัวในลำดับ
4. ดู summary ว่า pass/fail กี่ test

---

## ตัวอย่าง 3 ระดับ

### Beginner — ทดสอบ stub พื้นฐาน

หลังสร้าง stub `GET /api/health` แล้วทดสอบใน Postman:

1. สร้าง request `GET {{baseUrl}}/api/health`
2. เพิ่ม Test:

```javascript
pm.test("Health check returns 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Status is ok", function () {
    const body = pm.response.json();
    pm.expect(body.status).to.equal("ok");
});
```

3. กด Send → ดูผลใน Test Results tab

### Intermediate — ทดสอบ Authentication Flow

สร้าง 2 request ใน collection โดย request แรก save token แล้ว request ถัดไปใช้ต่อ:

**Request 1: POST /api/login**

```javascript
// Tests tab
pm.test("Login returns token", function () {
    pm.response.to.have.status(200);
    const body = pm.response.json();
    pm.expect(body).to.have.property("token");

    // บันทึก token ไว้ใช้ request ถัดไป
    pm.environment.set("authToken", body.token);
});
```

**Request 2: GET /api/profile** (ใช้ token จาก request แรก)

Headers:
```
Authorization: Bearer {{authToken}}
```

```javascript
// Tests tab
pm.test("Profile returns user data", function () {
    pm.response.to.have.status(200);
    const body = pm.response.json();
    pm.expect(body).to.have.property("userId");
});
```

### Advanced — Pre-request Script จัดการ Dynamic Data

บาง stub ต้องการ header หรือ body ที่สร้าง dynamically เช่น timestamp หรือ UUID:

```javascript
// Pre-request Script tab
const timestamp = new Date().toISOString();
const requestId = pm.variables.replaceIn("{{$guid}}");

pm.environment.set("requestTimestamp", timestamp);
pm.environment.set("requestId", requestId);
```

แล้วใน Headers:
```
X-Request-Id: {{requestId}}
X-Timestamp: {{requestTimestamp}}
```

---

## Common Mistakes

❌ **พิมพ์ `http://localhost:8080` ตรงๆ ใน request URL ทุกตัว**
ถ้าต้องเปลี่ยน port หรือ deploy mock บน server อื่น ต้องแก้ทุก request
→ ✅ ใช้ environment variable `{{baseUrl}}` เสมอ เปลี่ยนแค่ที่เดียว *(best practice)*

❌ **ไม่เขียน Test Script แล้วต้อง check response ด้วยตาทุกครั้ง**
Collection Runner จะไม่รู้ว่า pass หรือ fail ถ้าไม่มี assertions
→ ✅ เขียน `pm.test()` ทุก request อย่างน้อย 1 assertion เพื่อให้ Runner บอกผลได้ *(best practice)*

❌ **ลืม select environment ก่อนรัน**
`{{baseUrl}}` จะเป็น string literal ทำให้ request fail
→ ✅ เช็คว่า environment ถูก select อยู่ที่มุมบนขวาของ Postman ก่อนรันเสมอ *(best practice)*

---

## สรุปบท

**⏸ คำถาม Retrieval**

1. ทำไมถึงควรใช้ environment variable แทนการพิมพ์ URL ตรงๆ?

2. Postman Test Script คือ code ที่รันเมื่อไหร่ และใช้ตรวจสอบอะไรได้บ้าง?

3. ถ้าต้องการทดสอบ login แล้วเอา token ไปใช้ใน request ถัดไป จะทำได้ยังไงใน Postman?

---

??? note "เฉลย (คลิกเพื่อดู)"

    **ข้อ 1**: เพื่อให้เปลี่ยน base URL ได้แค่ที่เดียว (ใน environment) แทนที่จะแก้ทุก request ในทุก collection — สะดวกเมื่อเปลี่ยน environment (local → staging → prod)

    **ข้อ 2**: Test Script รัน**หลัง**ได้รับ response จาก server ใช้ตรวจสอบได้ เช่น status code, response body, headers, response time

    **ข้อ 3**: ใน Test Script ของ request login ใช้ `pm.environment.set("authToken", body.token)` บันทึกค่า แล้วใน request ถัดไปใช้ `{{authToken}}` ใน header Authorization
