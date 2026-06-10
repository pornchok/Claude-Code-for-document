# บทที่ 9 — Stateful Scenarios

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อนนะครับ:

- Static stubs กับ Dynamic stubs (Admin API) เหมาะกับกรณีต่างกันอย่างไร?
- ทำไม RF test suite ต้องมี `Sleep` หลัง start WireMock?

---

??? note "เฉลย"

    - Static stubs (JSON files) เหมาะกับ endpoint ที่ใช้ตลอด suite | Dynamic stubs เหมาะกับ stub เฉพาะ test case
    - WireMock ต้องใช้เวลา startup ถ้า request ไปก่อนพร้อม จะได้ ConnectionRefused

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:

- อธิบาย concept ของ Scenario ใน WireMock ได้
- สร้าง multi-step workflow ด้วย Scenarios ได้
- Reset scenario state ได้

---

## ทำไมต้องรู้? (Why)

API จริงหลายตัวมี state — ผลลัพธ์ขึ้นอยู่กับว่าทำอะไรไปก่อนหน้า เช่น:

- สั่งซื้อสินค้า → ดู order status ได้ว่า "pending"
- ชำระเงินแล้ว → order status เปลี่ยนเป็น "paid"
- ส่งของแล้ว → order status เปลี่ยนเป็น "shipped"

Stub ธรรมดา return response เดิมทุกครั้ง จะ mock workflow แบบนี้ไม่ได้ — Scenarios แก้ปัญหานี้

---

## เนื้อหาหลัก

### Scenario คืออะไร

Scenario คือ state machine ที่มีชื่อ มี states หลายสถานะ และ stub แต่ละตัวสามารถ: *(source: wiremock.org/docs/stateful-behaviour/)*

- **กำหนดว่า** ตัวเองจะทำงานเมื่อ scenario อยู่ใน state ไหน (`requiredScenarioState`)
- **เปลี่ยน state** ของ scenario หลัง match (`newScenarioState`)

ทุก scenario เริ่มต้นที่ state ชื่อ `"Started"` เสมอ

### โครงสร้าง JSON Scenario Stub

```json
{
  "scenarioName": "ชื่อ scenario",
  "requiredScenarioState": "state ที่ต้องการก่อน",
  "newScenarioState": "state ที่จะเปลี่ยนไปหลัง match",
  "request": { ... },
  "response": { ... }
}
```

### ตัวอย่าง: Order Lifecycle

**Stub 1** — สร้าง order (เปลี่ยนจาก Started → Order Created)

`mappings/scenario-01-create-order.json`:
```json
{
  "scenarioName": "Order Lifecycle",
  "requiredScenarioState": "Started",
  "newScenarioState": "Order Created",
  "request": {
    "method": "POST",
    "urlPath": "/api/orders"
  },
  "response": {
    "status": 201,
    "jsonBody": {
      "orderId": "ORD-001",
      "status": "pending"
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

**Stub 2** — ดู order ที่ state "Order Created" → return pending

`mappings/scenario-02-get-order-pending.json`:
```json
{
  "scenarioName": "Order Lifecycle",
  "requiredScenarioState": "Order Created",
  "request": {
    "method": "GET",
    "urlPath": "/api/orders/ORD-001"
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "orderId": "ORD-001",
      "status": "pending",
      "message": "Awaiting payment"
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

**Stub 3** — ชำระเงิน (เปลี่ยนจาก Order Created → Payment Done)

`mappings/scenario-03-pay-order.json`:
```json
{
  "scenarioName": "Order Lifecycle",
  "requiredScenarioState": "Order Created",
  "newScenarioState": "Payment Done",
  "request": {
    "method": "POST",
    "urlPath": "/api/orders/ORD-001/pay"
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "orderId": "ORD-001",
      "status": "paid",
      "transactionId": "TXN-999"
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

**Stub 4** — ดู order หลังชำระเงิน

`mappings/scenario-04-get-order-paid.json`:
```json
{
  "scenarioName": "Order Lifecycle",
  "requiredScenarioState": "Payment Done",
  "request": {
    "method": "GET",
    "urlPath": "/api/orders/ORD-001"
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "orderId": "ORD-001",
      "status": "paid",
      "message": "Payment confirmed, preparing shipment"
    },
    "headers": { "Content-Type": "application/json" }
  }
}
```

### ทดสอบ Scenario ด้วย curl

```bash
# Step 1: สร้าง order (state: Started → Order Created)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{}'

# ผลลัพธ์: {"orderId": "ORD-001", "status": "pending"}

# Step 2: ดู order (state: Order Created)
curl http://localhost:8080/api/orders/ORD-001

# ผลลัพธ์: {"status": "pending", "message": "Awaiting payment"}

# Step 3: ชำระเงิน (state: Order Created → Payment Done)
curl -X POST http://localhost:8080/api/orders/ORD-001/pay

# ผลลัพธ์: {"status": "paid", "transactionId": "TXN-999"}

# Step 4: ดู order อีกครั้ง (state: Payment Done)
curl http://localhost:8080/api/orders/ORD-001

# ผลลัพธ์: {"status": "paid", "message": "Payment confirmed..."}
```

### Reset Scenario

หลัง test จบ reset ให้กลับสู่ state "Started":

```bash
# Reset ทุก scenario
POST /__admin/scenarios/reset

# ดู state ปัจจุบันของทุก scenario
GET /__admin/scenarios
```

ใน Robot Framework:

```robot
*** Keywords ***
Reset All Scenarios
    POST On Session    mock_api    /__admin/scenarios/reset
```

---

## ตัวอย่าง 3 ระดับ

### Beginner — Shopping Cart

```
State: Started → Item Added → Checkout Done
```

`mappings/cart-01-add-item.json`:
```json
{
  "scenarioName": "Shopping Cart",
  "requiredScenarioState": "Started",
  "newScenarioState": "Item Added",
  "request": { "method": "POST", "urlPath": "/api/cart/items" },
  "response": {
    "status": 201,
    "jsonBody": { "cartId": "CART-1", "items": 1, "total": 35000 },
    "headers": { "Content-Type": "application/json" }
  }
}
```

### Intermediate — OTP Verification

จำลอง flow ส่ง OTP และ verify:

```
State: Started → OTP Sent → OTP Verified
```

`mappings/otp-01-request.json`:
```json
{
  "scenarioName": "OTP Flow",
  "requiredScenarioState": "Started",
  "newScenarioState": "OTP Sent",
  "request": { "method": "POST", "urlPath": "/api/otp/request" },
  "response": {
    "status": 200,
    "jsonBody": { "message": "OTP sent to +66812345678", "expiresIn": 300 },
    "headers": { "Content-Type": "application/json" }
  }
}
```

`mappings/otp-02-verify.json`:
```json
{
  "scenarioName": "OTP Flow",
  "requiredScenarioState": "OTP Sent",
  "newScenarioState": "OTP Verified",
  "request": {
    "method": "POST",
    "urlPath": "/api/otp/verify",
    "bodyPatterns": [{ "matchesJsonPath": "$[?(@.otp == '123456')]" }]
  },
  "response": {
    "status": 200,
    "jsonBody": { "verified": true, "sessionToken": "sess-abc-123" },
    "headers": { "Content-Type": "application/json" }
  }
}
```

### Advanced — Scenario ใน Robot Framework Test

```robot
*** Settings ***
Library    RequestsLibrary
Resource   resources/wiremock.resource

Suite Setup     Start WireMock
Suite Teardown  Stop WireMock

*** Test Cases ***
Complete Order Lifecycle Flow
    [Setup]    Reset All Scenarios

    # Step 1: Create order
    ${create_resp}=    POST On Session    mock_api    /api/orders
    ...    json=${{}}
    Should Be Equal As Integers    ${create_resp.status_code}    201
    Should Be Equal    ${create_resp.json()}[status]    pending

    # Step 2: Check order before payment
    ${check_resp}=    GET On Session    mock_api    /api/orders/ORD-001
    Should Be Equal    ${check_resp.json()}[status]    pending

    # Step 3: Pay
    ${pay_resp}=    POST On Session    mock_api    /api/orders/ORD-001/pay
    Should Be Equal    ${pay_resp.json()}[status]    paid

    # Step 4: Check order after payment
    ${final_resp}=    GET On Session    mock_api    /api/orders/ORD-001
    Should Be Equal    ${final_resp.json()}[status]    paid
```

---

## Common Mistakes

❌ **ลืม Reset scenario ระหว่าง test cases**
test แรก run ผ่าน scenario ครบ — test ถัดไปเริ่มที่ state สุดท้ายทันที ทำให้ fail โดยไม่รู้สาเหตุ
→ ✅ เพิ่ม `Reset All Scenarios` ใน `[Setup]` ของทุก test case ที่ใช้ scenario *(source: wiremock.org/docs/stateful-behaviour/)*

❌ **ตั้งชื่อ state ต่างกันระหว่าง stubs ที่เกี่ยวข้องกัน**
`"newScenarioState": "order created"` กับ `"requiredScenarioState": "Order Created"` — case sensitive ทำให้ไม่ match
→ ✅ ใช้ชื่อ state ให้ consistent และ exact เหมือนกันทุกตัว *(source: wiremock.org/docs/stateful-behaviour/)*

❌ **ใช้ Scenario กับ stub ทุกตัวโดยไม่จำเป็น**
Scenario เพิ่ม complexity — ถ้า endpoint ไม่มี state ไม่ต้องใช้
→ ✅ ใช้ Scenario เฉพาะเมื่อ response ต้องเปลี่ยนตาม workflow จริงๆ *(best practice)*

---

## สรุปบท

**⏸ คำถาม Retrieval**

1. Scenario ใน WireMock คืออะไร และแก้ปัญหาอะไรที่ stub ธรรมดาทำไม่ได้?

2. ทุก scenario เริ่มต้นที่ state อะไร และจะ reset กลับไปได้อย่างไร?

3. ทำไมต้อง reset scenario ก่อน test case แต่ละตัวเมื่อใช้ใน Robot Framework?

---

??? note "เฉลย (คลิกเพื่อดู)"

    **ข้อ 1**: Scenario คือ state machine ที่ทำให้ stub return response ต่างกันตาม state ปัจจุบัน — แก้ปัญหา stub ธรรมดาที่ return ค่าเดิมทุกครั้งไม่สามารถจำลอง workflow หลายขั้นตอนได้

    **ข้อ 2**: เริ่มต้นที่ state `"Started"` — reset ได้ด้วย `POST /__admin/scenarios/reset`

    **ข้อ 3**: เพราะ state ของ scenario ไม่ reset อัตโนมัติ — ถ้า test แรก run ครบ workflow แล้ว state จะค้างที่ state สุดท้าย test ถัดไปจะเริ่มที่ state ผิดและ fail โดยไม่รู้ว่าทำไม
