# แบบฝึกหัด — WireMock สำหรับ QA

---

## Recall (Beginner)

### ข้อ 1
อธิบายด้วยคำของคุณเองว่า "Mock API" คืออะไร และต่างจาก API จริงอย่างไร จากนั้นยกตัวอย่าง 2 สถานการณ์ในชีวิตการทำงาน QA ที่การมี mock server จะช่วยให้ทำงานได้ง่ายขึ้น (ห้ามใช้ตัวอย่างที่มีในบทเรียน)

??? note "เฉลย"

    Mock API คือ server ปลอมที่เราสร้างขึ้นเองเพื่อแทน API จริง return response ที่เรากำหนดได้เองโดยไม่มี business logic หรือ database จริงอยู่เบื้องหลัง ต่างจาก API จริงตรงที่ mock ไม่ "ทำ" อะไร มันแค่ "ตอบ" ตามที่เราสั่ง

    ตัวอย่างสถานการณ์ (ตอบได้หลายแบบ):
    - ทดสอบ feature ใหม่ที่ต้องเรียก SMS API แต่ไม่อยากเสียเงินค่า SMS จริงระหว่าง dev
    - ทดสอบแอปในโหมด offline เพื่อดูว่า UI แสดง error message ถูกต้อง
    - ทดสอบ feature ที่ขึ้นอยู่กับ weather API โดยไม่ต้องรอสภาพอากาศจริง

### ข้อ 2
เขียน JSON stub mapping สำหรับ endpoint ต่อไปนี้:
- Method: `GET`
- URL: `/api/weather/bangkok`
- Response status: `200`
- Response body: JSON ที่มี field `city`, `temperature`, `condition`

??? note "เฉลย"

    ```json
    {
      "request": {
        "method": "GET",
        "url": "/api/weather/bangkok"
      },
      "response": {
        "status": 200,
        "jsonBody": {
          "city": "Bangkok",
          "temperature": 35,
          "condition": "Sunny"
        },
        "headers": {
          "Content-Type": "application/json"
        }
      }
    }
    ```

### ข้อ 3
คุณรัน `java -jar wiremock.jar` แล้วได้ error ว่า `Address already in use: 8080` จะแก้ไขอย่างไร? และจะทดสอบว่า server พร้อมทำงานด้วยคำสั่งอะไร?

??? note "เฉลย"

    แก้โดยระบุ port อื่น: `java -jar wiremock.jar --port 9090`

    ทดสอบว่าพร้อมด้วย: `curl http://localhost:9090/__admin/mappings`
    ถ้าได้ `{"mappings":[],"meta":{"total":0}}` กลับมา แสดงว่าพร้อม

---

## Application (Intermediate)

### ข้อ 4
ทีมของคุณกำลังสร้างแอปจอง ticket ภาพยนตร์ มี API ดังนี้:

- `GET /api/movies` — ดูรายการหนังทั้งหมด
- `GET /api/movies/{id}/seats` — ดูที่นั่งว่างของหนัง
- `POST /api/bookings` — จองที่นั่ง

ออกแบบ stub files สำหรับทดสอบ **กรณีที่หนังเต็ม** (ไม่มีที่นั่งว่าง) โดยเขียน JSON ให้ครบและอธิบายว่า test case นี้ทดสอบอะไร

??? note "เฉลย"

    `mappings/movies/get-movie-seats-full.json`:
    ```json
    {
      "request": {
        "method": "GET",
        "urlPattern": "/api/movies/[0-9]+/seats"
      },
      "response": {
        "status": 200,
        "jsonBody": {
          "movieId": 1,
          "title": "Movie Title",
          "availableSeats": 0,
          "totalSeats": 150
        },
        "headers": { "Content-Type": "application/json" }
      }
    }
    ```

    `mappings/bookings/post-booking-sold-out.json`:
    ```json
    {
      "request": {
        "method": "POST",
        "urlPath": "/api/bookings"
      },
      "response": {
        "status": 409,
        "jsonBody": {
          "error": "Conflict",
          "message": "No seats available for this showing"
        },
        "headers": { "Content-Type": "application/json" }
      }
    }
    ```

    Test case นี้ทดสอบว่า: UI แสดงข้อความ "ที่นั่งเต็มแล้ว" ให้ user ถูกต้อง และปุ่มจองถูก disable หรือแสดง error message ที่เหมาะสมเมื่อ API return 409

### ข้อ 5
สร้าง Postman Test Script สำหรับ request `POST /api/bookings` ที่ verify ว่า:
1. Status code เป็น 201
2. Response body มี field `bookingId`
3. `bookingId` ไม่ใช่ string ว่าง
4. Response time น้อยกว่า 1000ms

??? note "เฉลย"

    ```javascript
    pm.test("Status code is 201", function () {
        pm.response.to.have.status(201);
    });

    pm.test("Response has bookingId", function () {
        const body = pm.response.json();
        pm.expect(body).to.have.property("bookingId");
    });

    pm.test("bookingId is not empty", function () {
        const body = pm.response.json();
        pm.expect(body.bookingId).to.not.be.empty;
    });

    pm.test("Response time is acceptable", function () {
        pm.expect(pm.response.responseTime).to.be.below(1000);
    });
    ```

### ข้อ 6
เขียน Robot Framework test case สำหรับทดสอบ flow ต่อไปนี้โดยใช้ WireMock:

1. GET `/api/notifications` → ได้ 0 notification
2. POST `/api/notifications/mark-all-read` → ได้ success
3. GET `/api/notifications` → ยังคง return 0 notification

(ไม่ต้องใช้ Scenario เพราะ GET return เหมือนกันทั้งก่อนและหลัง)

??? note "เฉลย"

    ```robot
    *** Settings ***
    Library    RequestsLibrary
    Resource   resources/wiremock.resource

    Suite Setup     Start WireMock
    Suite Teardown  Stop WireMock

    *** Test Cases ***
    Mark All Notifications As Read
        # Step 1: ดู notifications ก่อน
        ${resp1}=    GET On Session    mock_api    /api/notifications
        Should Be Equal As Integers    ${resp1.status_code}    200
        Should Be Equal As Integers    ${resp1.json()}[count]    0

        # Step 2: Mark ทั้งหมดว่าอ่านแล้ว
        ${resp2}=    POST On Session    mock_api    /api/notifications/mark-all-read
        Should Be Equal As Integers    ${resp2.status_code}    200
        Should Be Equal    ${resp2.json()}[message]    All notifications marked as read

        # Step 3: ตรวจสอบอีกครั้ง
        ${resp3}=    GET On Session    mock_api    /api/notifications
        Should Be Equal As Integers    ${resp3.status_code}    200
        Should Be Equal As Integers    ${resp3.json()}[count]    0
    ```

---

## Synthesis (Advanced)

### ข้อ 7
ทีม QA กำลังทดสอบระบบ delivery tracking ที่มี workflow ดังนี้:

```
สั่งซื้อ → รอรับออเดอร์ → กำลังจัดส่ง → ส่งสำเร็จ
```

ออกแบบ WireMock Scenario สำหรับ endpoint `GET /api/delivery/{trackingId}` ที่ return status ต่างกันตาม workflow พร้อม:
- เขียน JSON stub ทุกไฟล์ให้ครบ
- อธิบาย state machine ที่ออกแบบ
- บอก trigger ที่ทำให้ state เปลี่ยน

??? note "เฉลย"

    **State Machine:**
    ```
    Started → Order Received → In Transit → Delivered
    ```

    **Trigger:** POST ไปที่ endpoint `/__admin/scenarios/{name}/state` หรือใช้ stub ที่ trigger state change

    `mappings/delivery-01-order-received.json`:
    ```json
    {
      "scenarioName": "Delivery Tracking",
      "requiredScenarioState": "Started",
      "newScenarioState": "Order Received",
      "request": { "method": "GET", "urlPattern": "/api/delivery/[A-Z0-9]+" },
      "response": {
        "status": 200,
        "jsonBody": {
          "status": "pending",
          "message": "Order received, waiting for pickup"
        },
        "headers": { "Content-Type": "application/json" }
      }
    }
    ```

    `mappings/delivery-02-in-transit.json`:
    ```json
    {
      "scenarioName": "Delivery Tracking",
      "requiredScenarioState": "Order Received",
      "newScenarioState": "In Transit",
      "request": { "method": "GET", "urlPattern": "/api/delivery/[A-Z0-9]+" },
      "response": {
        "status": 200,
        "jsonBody": {
          "status": "in_transit",
          "message": "Package is on the way",
          "estimatedDelivery": "Tomorrow 14:00-18:00"
        },
        "headers": { "Content-Type": "application/json" }
      }
    }
    ```

    `mappings/delivery-03-delivered.json`:
    ```json
    {
      "scenarioName": "Delivery Tracking",
      "requiredScenarioState": "In Transit",
      "request": { "method": "GET", "urlPattern": "/api/delivery/[A-Z0-9]+" },
      "response": {
        "status": 200,
        "jsonBody": {
          "status": "delivered",
          "message": "Package delivered successfully",
          "deliveredAt": "2024-01-15 15:30:00"
        },
        "headers": { "Content-Type": "application/json" }
      }
    }
    ```

    ใน test แต่ละ call GET จะเปลี่ยน state ไปทีละขั้น สามารถ verify ว่า UI แสดง tracking status ถูกต้องในแต่ละขั้น

### ข้อ 8
ทีมของคุณมีปัญหา: QA แต่ละคนมี mock files ต่างกันในเครื่องตัวเอง ทำให้ test ผ่านในเครื่องหนึ่งแต่ fail ในเครื่องอื่น

วิเคราะห์ว่าปัญหานี้เกิดจากอะไร และออกแบบ process + โครงสร้างไฟล์ที่จะแก้ปัญหานี้ให้ถาวร โดยต้องครอบคลุม: การเก็บไฟล์, workflow ของทีม, และการตรวจสอบว่า mock ยังตรงกับ API จริง

??? note "เฉลย"

    **สาเหตุ**: Mock files ไม่ได้เก็บใน version control ร่วมกัน ทุกคนสร้าง/แก้ local copy แยกกัน

    **โครงสร้างที่แนะนำ**:
    ```
    project-repo/
    ├── tests/
    └── mocks/
        ├── README.md
        ├── mappings/
        │   ├── auth/
        │   ├── orders/
        │   └── ...
        └── __files/
    ```

    **Process**:
    1. Mock files อยู่ใน Git repo เดียวกับ test code
    2. เมื่อ dev เปลี่ยน API spec → เปิด PR → QA review และอัปเดต mock ใน PR เดียวกัน
    3. CI pipeline รัน validation script ตรวจว่า mock ตอบสนองถูกต้อง
    4. ห้าม merge ถ้า mock validation fail

    **การตรวจสอบ**:
    - เปรียบเทียบ mock response กับ OpenAPI/Swagger spec ในทุก CI run
    - ถ้า spec เปลี่ยน → test fail → บังคับให้อัปเดต mock ก่อน merge
