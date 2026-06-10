# บทที่ 8 — ใช้ WireMock กับ Robot Framework

## Pre-chapter Retrieval

ก่อนอ่านบทนี้ ลองตอบก่อนนะครับ:

- ทำไมถึงควรใช้ environment variable แทนการพิมพ์ URL ตรงๆ ใน Postman?
- Postman Test Script รันเมื่อไหร่ และใช้ทำอะไรได้บ้าง?

---

??? note "เฉลย"

    - เปลี่ยน environment ได้แค่ที่เดียว ไม่ต้องแก้ทุก request — เหมาะเมื่อ switch ระหว่าง local/staging/prod
    - Test Script รันหลังได้รับ response ใช้ verify status code, body, headers, response time

---

## วัตถุประสงค์

อ่านจบแล้วคุณจะ:

- Start/Stop WireMock ใน Robot Framework test suite ได้
- เรียก mock API ผ่าน RequestsLibrary ได้
- เขียน test case ที่ใช้ WireMock เป็น backend ได้

---

## ทำไมต้องรู้? (Why)

ถ้าคุณเขียน test ด้วย Robot Framework อยู่แล้ว การ integrate WireMock เข้าไปทำให้ test suite สมบูรณ์ขึ้นมาก เราสามารถ:

- รัน API test โดยไม่พึ่ง backend จริง
- ทดสอบ error flow ที่ทำยากบน real backend
- รัน test ใน CI/CD ได้โดยไม่ต้องมี environment

---

## เนื้อหาหลัก

### ติดตั้ง Library ที่จำเป็น

```bash
pip install robotframework
pip install robotframework-requests
```

### โครงสร้างโปรเจค

```
project/
├── mocks/
│   ├── wiremock.jar
│   └── mappings/
│       ├── get-products.json
│       └── post-orders.json
├── tests/
│   ├── resources/
│   │   └── wiremock.resource
│   └── api_tests.robot
└── run_tests.sh
```

### Resource File: wiremock.resource

สร้าง keyword สำหรับ start/stop WireMock ใน `tests/resources/wiremock.resource`:

```robot
*** Settings ***
Library    OperatingSystem
Library    Process
Library    RequestsLibrary

*** Variables ***
${WIREMOCK_PORT}    8080
${WIREMOCK_URL}     http://localhost:${WIREMOCK_PORT}
${WIREMOCK_JAR}     ${CURDIR}/../../mocks/wiremock.jar
${WIREMOCK_DIR}     ${CURDIR}/../../mocks

*** Keywords ***
Start WireMock
    ${process}=    Start Process    java    -jar    ${WIREMOCK_JAR}
    ...    --port    ${WIREMOCK_PORT}
    ...    --root-dir    ${WIREMOCK_DIR}
    ...    alias=wiremock
    Sleep    2s    reason=Wait for WireMock to start
    Create Session    mock_api    ${WIREMOCK_URL}

Stop WireMock
    Terminate Process    wiremock
    Delete All Sessions

Reset WireMock Stubs
    POST On Session    mock_api    /__admin/mappings/reset
```

### Test File: api_tests.robot

```robot
*** Settings ***
Library    RequestsLibrary
Library    Collections
Resource   resources/wiremock.resource

Suite Setup     Start WireMock
Suite Teardown  Stop WireMock

*** Test Cases ***
Get Products Should Return List
    ${response}=    GET On Session    mock_api    /api/products
    Should Be Equal As Integers    ${response.status_code}    200
    ${body}=    Set Variable    ${response.json()}
    Dictionary Should Contain Key    ${body}    products
    Length Should Be    ${body}[products]    2

Create Order Should Return 201
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${body}=       Create Dictionary    productId=${1}    quantity=${2}
    ${response}=   POST On Session    mock_api    /api/orders
    ...    json=${body}    headers=${headers}
    Should Be Equal As Integers    ${response.status_code}    201
    ${resp_body}=    Set Variable    ${response.json()}
    Dictionary Should Contain Key    ${resp_body}    orderId

API Should Return 401 When No Token
    ${response}=    GET On Session    mock_api    /api/profile
    ...    expected_status=401
    Should Be Equal As Integers    ${response.status_code}    401
```

### เพิ่ม Stub แบบ Dynamic ใน Test

บางครั้งต้องการ stub เฉพาะสำหรับ test case นั้น:

```robot
*** Keywords ***
Add Stub For Product ${product_id}
    ${stub}=    Create Dictionary
    ...    request=${{'method': 'GET', 'url': '/api/products/${product_id}'}}
    ...    response=${{'status': 200, 'jsonBody': {'id': ${product_id}, 'name': 'Test Product'}}}
    POST On Session    mock_api    /__admin/mappings    json=${stub}

Remove All Dynamic Stubs
    POST On Session    mock_api    /__admin/mappings/reset
```

ใช้ใน test case:

```robot
Get Specific Product By ID
    [Setup]    Add Stub For Product 42
    ${response}=    GET On Session    mock_api    /api/products/42
    Should Be Equal As Integers    ${response.status_code}    200
    [Teardown]    Remove All Dynamic Stubs
```

---

## ตัวอย่าง 3 ระดับ

### Beginner — Test เดียวกับ Postman แต่เป็น Robot Framework

```robot
*** Settings ***
Library    RequestsLibrary
Resource   resources/wiremock.resource

Suite Setup     Start WireMock
Suite Teardown  Stop WireMock

*** Test Cases ***
Health Check Endpoint Returns OK
    ${response}=    GET On Session    mock_api    /api/health
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal    ${response.json()}[status]    ok
```

### Intermediate — ทดสอบ Login Flow ครบ

```robot
*** Test Cases ***
Complete Login Flow
    # Step 1: Login
    ${credentials}=    Create Dictionary    username=testuser    password=pass123
    ${login_resp}=     POST On Session    mock_api    /api/login    json=${credentials}
    Should Be Equal As Integers    ${login_resp.status_code}    200
    ${token}=          Set Variable    ${login_resp.json()}[token]

    # Step 2: ใช้ token ดูข้อมูล profile
    ${headers}=     Create Dictionary    Authorization=Bearer ${token}
    ${profile_resp}=    GET On Session    mock_api    /api/profile    headers=${headers}
    Should Be Equal As Integers    ${profile_resp.status_code}    200
    Dictionary Should Contain Key    ${profile_resp.json()}    userId

Login With Wrong Password Returns 401
    ${credentials}=    Create Dictionary    username=testuser    password=wrongpass
    ${response}=    POST On Session    mock_api    /api/login
    ...    json=${credentials}    expected_status=401
    Should Be Equal As Integers    ${response.status_code}    401
```

### Advanced — Data-Driven Test กับ Mock

```robot
*** Settings ***
Library    RequestsLibrary
Library    DataDriver    file=test_data/products.csv    dialect=excel
Resource   resources/wiremock.resource

Suite Setup     Start WireMock
Suite Teardown  Stop WireMock

*** Test Cases ***
Product API Returns Correct Data For ${product_id}
    [Template]    Verify Product Response
    # data comes from products.csv

*** Keywords ***
Verify Product Response
    [Arguments]    ${product_id}    ${expected_name}    ${expected_price}
    ${response}=    GET On Session    mock_api    /api/products/${product_id}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal    ${response.json()}[name]    ${expected_name}
    Should Be Equal As Numbers    ${response.json()}[price]    ${expected_price}
```

`test_data/products.csv`:
```
product_id,expected_name,expected_price
1,iPhone 15,35000
2,Samsung S24,29000
```

---

## Common Mistakes

❌ **ไม่ sleep หลัง Start WireMock แล้ว test fail เพราะ server ยังไม่พร้อม**
WireMock ต้องใช้เวลา startup เล็กน้อย ถ้ารัน test ทันทีจะ connection refused
→ ✅ เพิ่ม `Sleep    2s` หลัง start process *(best practice)*

❌ **ใช้ Suite Teardown แต่ลืม Stop WireMock**
WireMock process จะ run ค้างอยู่หลัง test จบ ทำให้ port ถูก occupy
→ ✅ ใส่ `Stop WireMock` ใน `Suite Teardown` เสมอ *(best practice)*

❌ **Stub ใน mappings/ ล้อหลังจาก test บางตัว reset แล้วหาย**
`/__admin/mappings/reset` จะลบ dynamic stubs ทั้งหมดที่ POST ไปตอน runtime
→ ✅ Static stubs ที่ต้องการตลอด test suite ให้ไว้ใน JSON files ใน mappings/ folder แทน *(best practice)*

---

## สรุปบท

**⏸ คำถาม Retrieval**

1. ทำไมต้อง sleep หลัง start WireMock process ใน RF?

2. `Suite Setup` กับ `Test Setup` ต่างกันอย่างไร และควรใช้อันไหนสำหรับ Start WireMock?

3. Static stubs (JSON files ใน mappings/) กับ Dynamic stubs (POST ผ่าน Admin API) เหมาะกับกรณีไหนบ้าง?

---

??? note "เฉลย (คลิกเพื่อดู)"

    **ข้อ 1**: WireMock ต้องใช้เวลา startup — ถ้ารัน test ก่อนที่ server จะพร้อม จะได้ `ConnectionRefused` error แม้ว่า process จะ start แล้ว

    **ข้อ 2**: `Suite Setup` รันครั้งเดียวก่อน test ทุกตัวใน suite | `Test Setup` รันก่อน test แต่ละตัว — ควรใช้ `Suite Setup` สำหรับ Start WireMock เพื่อไม่ให้ start/stop ซ้ำๆ ทุก test case

    **ข้อ 3**: Static stubs เหมาะกับ endpoint ที่ใช้ตลอด suite (base happy path) | Dynamic stubs เหมาะกับ stub เฉพาะของ test case นั้น หรือ stub ที่ต้องปรับค่าตาม test data
