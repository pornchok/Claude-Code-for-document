# บทที่ 10: Anti-Patterns & Production Best Practices

---

## ⏰ Pre-chapter Retrieval

> แนะนำ: อ่านบทนี้หลังจากผ่านไปอย่างน้อย 1 วันนับจากที่อ่านบทที่ 9

**ก่อนอ่านบทนี้ ลองตอบ (Synthesis Question):**

สมมติว่าเพื่อนคุณทำสิ่งต่อไปนี้ใน test plan — รัน load test ใน GUI mode, เปิด View Results Tree ไว้ตลอด, ไม่มี Think Time, ไม่มี Assertion เดียวในทั้ง plan, ใช้ user credential เดียว hardcode ใน HTTP Request, และรัน test ตรงบน production server

1. ระบุ anti-patterns ทั้งหมดที่คุณเห็น (อย่างน้อย 5 อย่าง)
2. สำหรับ anti-pattern ที่แย่ที่สุดในความเห็นของคุณ — บอกว่าทำไมถึงแย่ที่สุด และจะแก้อย่างไร

เขียนคำตอบลงกระดาษก่อน scroll ลงมาดูเฉลย และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น

---

> **เฉลย (Anti-patterns):**
> 1. รัน load test ใน GUI mode — JMeter เองกินทรัพยากร skew results
> 2. เปิด View Results Tree ระหว่าง load test — memory บวมจาก response body ที่เก็บไว้ทุก request
> 3. ไม่มี Think Time — requests ยิงติดๆ กันไม่เหมือน real user ทำให้ load สูงเกินจริง
> 4. ไม่มี Assertion — ถึงแม้ server ตอบ 200 พร้อม error message ก็ไม่รู้ว่า test ผ่านจริงหรือเปล่า
> 5. Hardcode credential เดียว — ไม่สะท้อน real-world scenario, อาจ rate limit จาก server
> 6. Test บน production — อันตรายมากที่สุด อาจทำ production down ส่งผลถึงผู้ใช้จริง
>
> Anti-pattern ที่แย่ที่สุดคือการ **test บน production** เพราะส่งผลกระทบ real users และธุรกิจโดยตรง ทันที — anti-pattern อื่นทำให้ผลลัพธ์ผิดเพี้ยน แต่ยังแก้ได้ด้วยการ re-run; testing บน production อาจทำให้เกิด downtime ที่เรียกคืนไม่ได้ในช่วงนั้น
>
> **Remediation path:** ถ้าระบุได้น้อยกว่า 4 อย่าง — กลับทบทวนบทที่ 2 (CLI vs GUI), บทที่ 5 (Think Time), บทที่ 7 (Assertions) ก่อนอ่านบทนี้ต่อ เพราะบทนี้เป็นการ synthesize ความรู้จากทุกบทที่ผ่านมา

---

## 1. วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะทำได้:

- **ระบุและอธิบาย** anti-patterns สำคัญทั้ง 10 บทที่ผ่านมาได้ในครั้งเดียว — รู้ว่าแต่ละ anti-pattern ส่งผลเสียต่อ accuracy หรือ reliability ของผลลัพธ์อย่างไร
- **ประเมิน** test plan ของผู้อื่น (code review) ได้ว่ามี anti-pattern อะไรบ้าง
- **วางแผน** production-ready test workflow ได้ตั้งแต่เขียน script จนถึง run และ report
- **ออกแบบ** CI/CD integration สำหรับ JMeter test ได้ — รู้ว่า step ไหนควรอยู่ใน pipeline
- **ตัดสินใจ** trade-off ระหว่าง thoroughness และ speed ในการเขียน test plan ได้อย่างมีเหตุผล

---

## 2. ทำไมต้องรู้? (Why)

คุณอาจเรียนรู้ tools ทุกอย่างในบทที่ 1-9 ได้ครบ — รู้วิธีสร้าง Thread Group, ใส่ CSV Data, เขียน Assertion, รัน CLI mode แต่ถ้ายังทำ anti-patterns พื้นฐาน ผลลัพธ์ที่ได้จะ **misleading** มากกว่า "ไม่มีข้อมูล"

ข้อมูลที่ผิดเพี้ยนนั้นอันตรายกว่าการไม่มีข้อมูลเลย เพราะมันทำให้คุณตัดสินใจผิด — เช่น สรุปว่า system รับได้ 1,000 users ทั้งที่ test ที่รันนั้นเต็มไปด้วย flaws

บทนี้รวม best practices จากทั้ง series ไว้ในที่เดียว — ใช้เป็น reference checklist ก่อนรัน test ทุกครั้ง

---

## 3. Analogy: Pre-flight Checklist ของนักบิน

นักบินมืออาชีพไม่ว่าจะบินมานานแค่ไหน ยังต้องทำ **pre-flight checklist** ทุกครั้งก่อน takeoff — ไม่ใช่เพราะลืมขั้นตอน แต่เพราะรู้ว่า checklist ป้องกัน error จาก cognitive shortcuts และ overconfidence

- **Best practices** = checklist item แต่ละข้อที่นักบินต้องทำ — มีเหตุผลอยู่เบื้องหลังทุกข้อ
- **Anti-pattern** = การข้ามขั้นตอน checklist เพราะ "เคยทำมาแล้ว รู้แล้ว" — อาจไม่มีปัญหา 99 ครั้ง แต่ครั้งที่ 100 อาจเป็น production outage
- **การ test บน production** = เหมือนทดสอบ engine ใหม่ครั้งแรกบนเครื่องบินที่กำลังบินอยู่ที่ระดับ 35,000 ฟุต

> ⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า: ทำครบ checklist แล้วผลการทดสอบต้องถูกต้องเสมอ — ผิด checklist ป้องกัน **known mistakes** ที่เคยเจอมาก่อนเท่านั้น แต่ไม่ครอบคลุม scenario ใหม่ที่ไม่เคยเจอมาก่อน production incident หลายครั้งเกิดจาก "ทุกอย่างถูก checklist แต่ยังพังอยู่" เพราะ context ของ test ไม่ตรงกับ production จริง

---

## 4. เนื้อหาหลัก

### 4.1 ✅ Always Use CLI Mode for Actual Load Tests

นี่คือ rule ที่สำคัญที่สุดและ official docs ระบุไว้อย่างชัดเจน:

> *"GUI mode should only be used for creating the test script, CLI mode (NON GUI) must be used for load testing"*
> — Apache JMeter documentation (get-started.html)

**กฎปฏิบัติ:**
- GUI mode → ใช้เฉพาะ สร้างสคริปต์ + debug
- CLI mode → ใช้สำหรับ run ทุก load test จริง

```bash
# ✅ แบบถูกต้อง
jmeter -n -t test.jmx -l results.jtl -e -o report/

# ❌ แบบผิด — อย่ากด "Start" ใน GUI เพื่อ run load test
```

---

### 4.2 ✅ Disable Unnecessary Listeners During Test Run

> *"Use as few Listeners as possible; if using the -l flag as above they can all be deleted or disabled."*
> — Apache JMeter best practices (best-practices.html)

> *"Don't use 'View Results Tree' or 'View Results in Table' listeners during the load test, use them only during scripting phase to debug your scripts."*
> — Apache JMeter best practices (best-practices.html)

**แนวปฏิบัติ:**
- ระหว่างรัน load test — disable Listeners ทุกตัวใน test plan หรือลบออก แล้วใช้ `-l results.jtl` แทน
- ระหว่าง debug — เปิด View Results Tree ได้ แต่ต้องปิดก่อน run load test

⚠️ **ควรเลี่ยง:** เปิด Listeners หลายตัวพร้อมกันระหว่าง load test — แต่ละ Listener บันทึกข้อมูลซ้ำกัน ทำให้ JMeter ใช้ memory สูงโดยไม่จำเป็น

---

### 4.3 ✅ Use Think Time (Timers) to Simulate Real User Behavior

Real users ไม่ได้ยิง request ติดกันทันที — พวกเขาอ่านหน้า, กรอก form, คิดก่อนคลิก Test plan ที่ไม่มี Think Time จะสร้าง **unrealistically high load** ที่ไม่ตรงกับ real-world usage pattern

ใช้ **Uniform Random Timer** เพื่อ simulate realistic think time:

```
Random delay maximum: 3000 ms
Constant delay offset: 1000 ms
```

ผลลัพธ์: delay สุ่มระหว่าง 1,000–4,000 ms ต่อ sampler — ใกล้เคียงกับ behavior จริงมากกว่า constant 0ms

---

### 4.4 ✅ Correlate Dynamic Values ด้วย Extractors

Production apps มักมี dynamic values ที่เปลี่ยนทุก session: session ID, CSRF token, auth token, order ID ถ้า hardcode ค่าเหล่านี้ใน test plan, requests ครั้งที่ 2 เป็นต้นไปจะ fail เพราะ session expired หรือ token ไม่ตรง

**วิธีแก้:** ใช้ **Extractors** (Regular Expression Extractor, JSON Extractor, XPath Extractor) ดึงค่าจาก response ของ step ก่อนหน้า แล้วส่งต่อเป็น variable ไปยัง step ถัดไป

ตัวอย่าง flow ที่ถูกต้อง:
```
POST /login       → extract auth_token จาก response body
GET  /dashboard   → ส่ง Authorization: Bearer ${auth_token}
POST /transaction → ส่ง X-CSRF-Token: ${csrf_token}
```

---

### 4.5 ✅ Keep Test Plan Maintainable

Test plan ที่ซับซ้อนและ maintain ยากจะทำให้ทีม skip การ update เมื่อ API เปลี่ยน — ใช้เครื่องมือเหล่านี้:

- **Test Fragments** — แยก reusable flow ออกเป็น sub-plan ที่ include ได้
- **Include Controller** — ดึง Test Fragment มาใช้ใน Test Plan หลัก
- **User Defined Variables** ที่ Test Plan level — ตั้ง base URL, port, และค่า shared ไว้ที่เดียว แก้ครั้งเดียวใช้ทุกที่

ประโยชน์: เปลี่ยน environment (dev → staging → prod) ด้วยการแก้แค่ variable เดียว

---

### 4.6 ✅ CI/CD Integration: รัน JMeter ใน Pipeline

Load test ที่รันแค่ตอน manual testing มักถูกลืมหรือ skip เมื่อ deadline กระชั้น — integrate เข้า CI/CD pipeline เพื่อให้ test รันอัตโนมัติ

**Brief overview สำหรับ GitHub Actions:**

```yaml
# .github/workflows/performance-test.yml
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3 + GitHub Actions environment
name: Performance Test

on:
  push:
    branches: [main]

jobs:
  performance-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Install JMeter
        run: |
          wget -q https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.tgz
          tar -xzf apache-jmeter-5.6.3.tgz
          echo "$(pwd)/apache-jmeter-5.6.3/bin" >> $GITHUB_PATH

      - name: Run Performance Test
        run: |
          jmeter -n -t tests/api_test.jmx \
            -l results/results.jtl \
            -e -o results/report/

      - name: Upload Report
        uses: actions/upload-artifact@v4
        with:
          name: performance-report
          path: results/report/
```

สำหรับ **Jenkins**: ใช้ JMeter plugin หรือ shell step ที่รัน jmeter CLI แล้วตรวจ exit code

---

### 4.7 ⚠️ Anti-Patterns ที่ต้องหลีกเลี่ยงเสมอ

**Anti-pattern 1: Hardcode credentials**
```
❌ Username: admin
   Password: password123  (hardcode ใน HTTP Request)

✅ ใช้ CSV Data Set Config กับไฟล์ test users แทน
```

**Anti-pattern 2: ไม่มี Assertion ใน test plan**
- Server ตอบ HTTP 200 พร้อม body `{"error": "session expired"}` → JMeter บันทึกว่า "success"
- ต้องมี Response Assertion ตรวจ content จริงของ response

**Anti-pattern 3: ไม่ warm up ก่อน measure**
- เริ่ม measure ตั้งแต่ request แรก → JVM cold start, cache miss, connection pool initialization skew ผลลัพธ์
- ✅ ควรมี ramp-up period ที่เหมาะสม และ ignore ผลช่วงแรก

**Anti-pattern 4: Test ใน production environment โดยไม่มี plan**
- อาจทำให้ real users ได้รับผลกระทบทันที
- ✅ ต้องมี staging environment ที่ใกล้เคียง production — ถ้าจำเป็นต้อง test บน production ต้องมี maintenance window และ rollback plan

> *"When is a good time to load-test our application (i.e. off-hours or week-ends), bearing in mind that this may very well crash one or more of our servers?"*
> — Apache JMeter documentation (boss.html)

**Anti-pattern 5: ใช้ JMeter version เก่ามาก**

> *"You should absolutely avoid using versions that are older than 3 versions before the last one."*
> — Apache JMeter best practices (best-practices.html)

ใช้ JMeter 5.6.3 (latest stable) และอัปเดตเป็นประจำ

> *"The performance of JMeter is being constantly improved, so users are highly encouraged to use the most up to date version."*
> — Apache JMeter best practices (best-practices.html)

⏸ **Self-check (L5 — Synthesis/Evaluate):** ทีมบอกว่า "test plan ของเรา error rate 0% ตลอด 30 นาที แสดงว่าระบบ stable" — ระบุว่า argument นี้มีช่องโหว่อะไรบ้าง และต้องดูข้อมูลอะไรเพิ่มก่อนสรุปแบบนั้น? เขียนคำตอบลงกระดาษก่อน — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น — แล้วค่อยอ่านต่อ

---

> **แนวทางตอบ:** ช่องโหว่ของ argument: (1) Error rate 0% อาจหมายความว่า test plan ไม่มี Assertion เลย ทำให้ JMeter นับทุก response ว่า success แม้ server ตอบ error messages ก็ตาม (2) ไม่รู้ว่า load ที่ test เป็นเท่าไหร่ — ถ้ารัน 10 threads แต่ production รับ 5,000 concurrent users ผล 30 นาทีนั้นไม่มีความหมาย (3) ไม่รู้ response time — system อาจ "ไม่ error" แต่ slow อย่างไม่ยอมรับได้ | ต้องดูเพิ่ม: (1) มี Assertion ครอบคลุมทุก endpoint ไหม (2) load level ตรงกับ production expectation ไหม (3) response time percentiles เป็นอย่างไร (4) server-side metrics (CPU, memory, DB) ขณะ test

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner: Checklist ก่อนรัน Test Plan

ใช้ checklist นี้ทุกครั้งก่อนรัน load test จริง:

```
PRE-FLIGHT CHECKLIST — JMeter Load Test
ยังไม่ได้ทดสอบ — ใช้เป็น reference เท่านั้น

□ 1. Thread Group settings
   - Number of Threads (users): ____
   - Ramp-Up Period: ____ วินาที
   - Duration: ____ วินาที (หรือ Loop Count)
   ทำไม: Thread count ที่ต่ำหรือ ramp-up ที่สั้นเกินไปทำให้ผลไม่สะท้อน real load

□ 2. Think Time
   - มี Timer อย่างน้อย 1 ตัวใน Thread Group: [ Yes / No ]
   - ค่า delay อยู่ในช่วงที่สมเหตุสมผล (>= 1 วินาทีสำหรับ web app): [ Yes / No ]
   ทำไม: ไม่มี Think Time = load สูงเกินจริง = ผลลัพธ์ไม่ใช่ real-world

□ 3. Assertions
   - ทุก endpoint หลักมี Assertion ตรวจ response content หรือ status code: [ Yes / No ]
   - มี Duration Assertion สำหรับ SLA: [ Yes / No ]
   ทำไม: ไม่มี Assertion = ไม่รู้ว่า test "ผ่าน" จริงหรือเปล่า

□ 4. Parameterization
   - Dynamic values (session token, CSRF, order ID) ถูก extract ด้วย Extractor: [ Yes / No ]
   - Test data (users, products) มาจาก CSV ไม่ใช่ hardcode: [ Yes / No ]
   ทำไม: Hardcode ค่าเดียว = test เดียวซ้ำๆ ไม่ใช่ realistic load

□ 5. Listeners
   - View Results Tree: [ Disabled / Deleted ]
   - เหลือแค่ Simple Data Writer หรือ Backend Listener: [ Yes / No ]
   ทำไม: Listener ที่ active ขณะ load test กินหน่วยความจำโดยไม่จำเป็น

□ 6. Run mode
   - ใช้ CLI command: jmeter -n -t ... ไม่ใช่กด Run ใน GUI: [ Yes / No ]
   ทำไม: GUI mode skews results เพราะ JMeter ใช้ resource ใน rendering

□ 7. Environment
   - กำลัง test บน: [ Dev / Staging / Production ]
   - ถ้า Production: มี maintenance window และ stakeholder approve แล้ว: [ Yes / No ]
   ทำไม: Test บน production โดยไม่มี plan อาจทำให้ real users ได้รับผลกระทบ

□ 8. Baseline
   - มีผล smoke test (5-10 users) ที่ผ่านแล้ว ก่อน run full load test: [ Yes / No ]
   ทำไม: ถ้า test plan มี bug พื้นฐาน smoke test จะจับก่อน ประหยัดเวลา full run
```

---

### Intermediate: Code Review Exercise — ระบุ Anti-Patterns ใน Supply Chain Test Plan

สถานการณ์: คุณเป็น senior QA ที่ได้รับ test plan JMX ของน้องใหม่ที่เขียน test สำหรับระบบ warehouse management (supply chain domain) ให้อ่าน configuration ด้านล่างและระบุ anti-patterns ทั้งหมดพร้อมอธิบายว่าแก้อย่างไร:

```xml
<!-- Test Plan excerpt — supply chain warehouse API -->
<!-- ยังไม่ได้ทดสอบ — เป็นตัวอย่างสำหรับ code review เท่านั้น -->
<ThreadGroup testname="Warehouse API Load Test">
  <intProp name="ThreadGroup.num_threads">500</intProp>
  <intProp name="ThreadGroup.ramp_time">5</intProp>  <!-- 5 วินาที -->
  <boolProp name="ThreadGroup.scheduler">false</boolProp>
  <!-- Loop count = Forever -->
  <stringProp name="LoopController.loops">-1</stringProp>
</ThreadGroup>

<HTTPSamplerProxy testname="GET Inventory">
  <stringProp name="HTTPSampler.domain">prod-warehouse-api.company.com</stringProp>
  <stringProp name="HTTPSampler.port">443</stringProp>
  <stringProp name="HTTPSampler.path">/api/inventory</stringProp>
  <stringProp name="HTTPSampler.method">GET</stringProp>
  <!-- ไม่มี Header Manager, ไม่มี Authentication -->
</HTTPSamplerProxy>

<HTTPSamplerProxy testname="POST Create Shipment">
  <stringProp name="HTTPSampler.domain">prod-warehouse-api.company.com</stringProp>
  <!-- hardcode product ID และ warehouse location ทุก request -->
  <stringProp name="HTTPSampler.arguments">
    product_id=SKU-001&amp;warehouse=BKK-01&amp;quantity=10
  </stringProp>
</HTTPSamplerProxy>

<!-- Listeners ที่ active -->
<ResultCollector testname="View Results Tree" enabled="true">...</ResultCollector>
<ResultCollector testname="Aggregate Report" enabled="true">...</ResultCollector>
<ResultCollector testname="Response Time Graph" enabled="true">...</ResultCollector>

<!-- ไม่มี Timer ใดๆ ในทั้ง plan -->
<!-- ไม่มี Assertion ใดๆ ในทั้ง plan -->
```

**Anti-patterns ที่ควรระบุและวิธีแก้:**

1. **Ramp-up 5 วินาทีสำหรับ 500 threads** → 100 threads/วินาที = aggressive เกินไป แก้: เพิ่ม ramp-up เป็นอย่างน้อย 100-300 วินาที เพื่อ warm up ระบบอย่างค่อยเป็นค่อยไป

2. **Loop = Forever โดยไม่มี Duration** → test จะรันไม่หยุดจนกว่าจะ interrupt เอง แก้: ตั้ง scheduler หรือ Duration เช่น 300 วินาที

3. **Test บน `prod-warehouse-api.company.com`** → กำลัง load test บน production โดยตรง — อันตรายมาก แก้: เปลี่ยนเป็น `staging-warehouse-api.company.com`

4. **Hardcode `product_id=SKU-001&warehouse=BKK-01`** → ทุก request ส่ง data เดิม ไม่สะท้อน real usage แก้: ใช้ CSV Data Set Config ที่มี product_id และ warehouse location หลายๆ รายการ

5. **ไม่มี Think Time** → 500 threads ยิง requests ต่อเนื่องทันที แก้: เพิ่ม Uniform Random Timer 1,000-3,000ms

6. **เปิด View Results Tree + Aggregate Report + Response Time Graph พร้อมกัน** → memory สูงมาก แก้: ปิด/ลบ Listeners ทั้งหมด ใช้ `-l results.jtl` ใน CLI แทน

7. **ไม่มี Assertion** → JMeter นับทุก response เป็น success แม้ business logic จะ fail แก้: เพิ่ม Response Assertion ตรวจ HTTP status code + response body

---

### Advanced: GitHub Actions Workflow ที่ Fail Build เมื่อ Error > 1%

```yaml
# .github/workflows/performance-test.yml
# ยังไม่ได้ทดสอบ — ต้องการ GitHub Actions + JMeter 5.6.3
name: Performance Test on Push to Main

on:
  push:
    branches: [main]
  workflow_dispatch:  # รันเองได้ด้วย

env:
  JMETER_VERSION: "5.6.3"
  ERROR_THRESHOLD: "1"      # fail ถ้า error% > 1%
  P95_THRESHOLD_MS: "2000"  # warn ถ้า p95 > 2000ms

jobs:
  performance-test:
    runs-on: ubuntu-latest
    timeout-minutes: 30

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up Java 11
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'

      - name: Install JMeter ${{ env.JMETER_VERSION }}
        run: |
          JMETER_URL="https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz"
          wget -q "$JMETER_URL" -O /tmp/jmeter.tgz
          tar -xzf /tmp/jmeter.tgz -C /opt
          echo "/opt/apache-jmeter-${JMETER_VERSION}/bin" >> $GITHUB_PATH

      - name: Create results directory
        run: mkdir -p results/report

      - name: Run Performance Test
        run: |
          jmeter -n \
            -t tests/api_load_test.jmx \
            -l results/results.jtl \
            -e -o results/report/ \
            -Jusers=50 \
            -Jrampup=60 \
            -Jduration=180
        # -J ส่ง JMeter properties — test plan ต้องใช้ ${__P(users,10)} syntax

      - name: Check Error Rate — Fail Build if > threshold
        run: |
          # JTL file เป็น CSV: column 8 (index 7) = success (true/false)
          TOTAL=$(tail -n +2 results/results.jtl | wc -l | tr -d ' ')
          ERRORS=$(tail -n +2 results/results.jtl | awk -F',' '{if($8=="false") count++} END {print count+0}')

          if [ "$TOTAL" -eq 0 ]; then
            echo "ERROR: No samples found in results file"
            exit 1
          fi

          ERROR_RATE=$(echo "scale=2; $ERRORS * 100 / $TOTAL" | bc)

          echo "=== Performance Test Results ==="
          echo "Total requests : $TOTAL"
          echo "Failed requests: $ERRORS"
          echo "Error rate     : ${ERROR_RATE}%"
          echo "Threshold      : ${ERROR_THRESHOLD}%"

          # ตรวจ error rate เกิน threshold ไหม
          EXCEEDS=$(echo "$ERROR_RATE > $ERROR_THRESHOLD" | bc -l)
          if [ "$EXCEEDS" -eq 1 ]; then
            echo ""
            echo "❌ FAIL: Error rate ${ERROR_RATE}% exceeds threshold ${ERROR_THRESHOLD}%"
            echo "Build failed — check HTML report for details"
            exit 1
          else
            echo ""
            echo "✅ PASS: Error rate ${ERROR_RATE}% is within threshold ${ERROR_THRESHOLD}%"
          fi

      - name: Upload HTML Report (always — even on failure)
        uses: actions/upload-artifact@v4
        if: always()  # upload แม้ job จะ fail เพื่อให้ดู report ได้
        with:
          name: performance-report-${{ github.run_number }}
          path: results/report/
          retention-days: 14

      - name: Upload Raw JTL
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: jmeter-results-jtl-${{ github.run_number }}
          path: results/results.jtl
          retention-days: 7
```

**จุดสำคัญของ workflow นี้:**
- ใช้ `if: always()` บน upload step เพื่อให้ download report ได้แม้ build จะ fail
- `-Jusers=50` ส่ง property เข้า test plan — test plan ต้องรับด้วย `${__P(users,10)}`
- `exit 1` ทำให้ GitHub Actions mark build เป็น "failed" และ block merge ถ้าตั้ง branch protection

---

## 6. Common Mistakes (Anti-Patterns ที่เจอบ่อยที่สุด)

❌ **ไม่ disable Listeners ก่อนรัน load test**
✅ ลบหรือ disable Listeners ทุกตัวในต้นบน load test จริง ใช้ `-l results.jtl` สำหรับ raw data
🔍 **วิธีสังเกต:** JMeter ใช้ RAM สูงผิดปกติ หรือ warning "OutOfMemoryError" ระหว่าง test
🤔 **เหตุผล:** Listeners เก็บ results ใน memory ขณะรัน — View Results Tree เก็บ response body ทุกชิ้น เมื่อ load สูงขึ้น memory จะล้นและ JMeter crash
*(source: https://jmeter.apache.org/usermanual/best-practices.html — "Don't use 'View Results Tree' or 'View Results in Table' listeners during the load test")*

---

❌ **ไม่มี Assertion ใน test plan**
✅ เพิ่ม Response Assertion ตรวจ status code และ/หรือ response body ในทุก critical endpoint
🔍 **วิธีสังเกต:** Error% = 0% แม้ server ตอบ JSON body ที่มี error messages หรือ empty responses
🤔 **เหตุผล:** JMeter นับว่า request "success" ถ้า HTTP response ได้มา (ไม่ timeout/network error) — ไม่ว่า business logic จะ fail ขนาดไหนก็ตาม Assertion เป็นสิ่งเดียวที่แยก functional correctness ออกจาก HTTP success
*(source: https://jmeter.apache.org/usermanual/best-practices.html — "Use as few Assertions as possible" [แต่ต้องมี] + https://jmeter.apache.org/usermanual/test_plan.html — "Assertions allow you to assert facts about responses received")*

---

❌ **ไม่มี Think Time — request ยิงติดกันทันที**
✅ เพิ่ม Uniform Random Timer หรือ Gaussian Random Timer ที่มีค่า delay อย่างน้อย 1,000ms
🔍 **วิธีสังเกต:** Throughput สูงผิดปกติ (เช่น 500 req/s จาก 10 threads) หรือ server ล่มที่ load ต่ำกว่าที่คาดไว้มาก
🤔 **เหตุผล:** Real users ใช้เวลาอ่านหน้า กรอก form คิดก่อนคลิก — การยิง request ติดกันทันทีจาก 100 threads เท่ากับ load จริงที่สูงกว่า 100 concurrent users มาก ทำให้ผล test ไม่สะท้อนความเป็นจริง
*(source: https://jmeter.apache.org/usermanual/test_plan.html — "A timer will cause JMeter to delay a certain amount of time before each sampler which is in its scope.")*

---

❌ **Hardcode test credentials หรือ test data ใน test plan**
✅ ใช้ CSV Data Set Config + User Defined Variables สำหรับข้อมูลที่ต้องเปลี่ยนแปลง
🔍 **วิธีสังเกต:** Test plan ทำงานได้กับ user เดียว แต่พอเพิ่ม threads มากขึ้น requests เริ่ม fail เพราะ server rate limit user เดียวกัน หรือ session conflict
🤔 **เหตุผล:** Production systems มักมี per-user rate limiting หรือ session management ที่ทำงานไม่ถูกต้องถ้า requests จากหลาย threads ใช้ credentials เดียวกัน
*(source: https://jmeter.apache.org/usermanual/best-practices.html — "Define some user-defined variables at the Test Plan level")*

---

❌ **Test บน production environment โดยไม่มี plan**
✅ ต้องมี staging environment, maintenance window, stakeholder approval, และ rollback plan ก่อน test production
🔍 **วิธีสังเกต:** ไม่มีอะไรให้สังเกต — เพราะถึงตอนนั้นอาจ production ล่มไปแล้ว
🤔 **เหตุผล:** Load test ออกแบบมาเพื่อค้นหา breaking point — มันอาจทำให้ระบบ fail จริงๆ ถ้า test บน production real users จะได้รับผลกระทบโดยตรง และ incident ที่เกิดจาก test นั้น costly กว่า bug ที่ค้นพบใน staging มาก
*(source: https://jmeter.apache.org/usermanual/boss.html — "this may very well crash one or more of our servers")*

---

## 7. สรุปบท — Retrieval Questions

ลองตอบคำถามต่อไปนี้ด้วยตัวเอง **ก่อน** ดูเฉลย — เขียนคำตอบลงกระดาษก่อน อย่างน้อย 30 วินาทีต่อข้อ และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น:

**คำถามที่ 1 (Synthesis — Evaluate):** test plan มี error rate 0% ตลอด 30 นาที ผู้จัดการสรุปว่า "system production-ready" — ระบุว่าสรุปนั้นอาจผิดได้เพราะอะไรบ้าง และต้องมีหลักฐานอะไรเพิ่มเติมก่อนสรุปแบบนั้นได้อย่างน่าเชื่อถือ?

**คำถามที่ 2 (Code-based Task):** นี่คือ CI/CD pipeline snippet ที่มีปัญหา — ระบุ anti-patterns:
```yaml
- name: Run JMeter
  run: |
    # open GUI JMeter and run test
    jmeter -t tests/load_test.jmx -l results.jtl

- name: Check results
  run: |
    # if file exists, declare success
    if [ -f results.jtl ]; then
      echo "Test completed"
    fi
```

**คำถามที่ 3 (Far Transfer — Synthesis):** คุณได้รับ task ให้ออกแบบ performance testing strategy สำหรับ fintech startup ที่กำลังจะ launch mobile banking app ในเดือนหน้า ทีมมีเวลา 5 วัน — ออกแบบ plan ว่าจะทำ test ประเภทอะไร ด้วยเครื่องมืออะไร ใน environment อะไร และมี success criteria อย่างไร (ใช้ความรู้จากทั้ง series)

---

<details>
<summary>ดูเฉลย (คลิกหลังจากตอบแล้ว)</summary>

**เฉลยที่ 1:** สรุปนั้นอาจผิดเพราะ: (1) ไม่รู้ว่า load level ที่ test สะท้อน production ไหม — ถ้า test ที่ 10 users แต่ production มี 10,000 concurrent ผล 30 นาทีไม่มีความหมาย (2) Error rate 0% ไม่บอกอะไรถ้าไม่มี Assertion — server อาจตอบ 200 พร้อม error body ก็ได้ (3) ไม่มี response time data — "ไม่มี error" ≠ "เร็วพอ" (4) 30 นาทีอาจสั้นเกินไปสำหรับ memory leak หรือ connection pool issue | ต้องมีเพิ่ม: (a) load level ≥ expected production traffic, (b) Assertions ครอบคลุม business logic, (c) response time percentiles ตาม SLA, (d) soak test อย่างน้อย 1-2 ชั่วโมง

**เฉลยที่ 2:** Anti-patterns ใน snippet: (1) `jmeter -t tests/load_test.jmx` ขาด `-n` flag → JMeter จะพยายามเปิด GUI ใน CI environment ซึ่งจะ fail หรือรันแบบ GUI mode (2) ไม่มี `-l` flag สำหรับ save results → ไม่มีข้อมูลจริงจาก test (3) `if [ -f results.jtl ]` ตรวจแค่ว่าไฟล์มีอยู่ ไม่ได้ตรวจ content — ถ้า test fail ทั้งหมด ไฟล์ก็ยังมีอยู่ → pipeline บอกว่า "success" ทั้งที่ fail | แก้ด้วย: เพิ่ม `-n` flag + ตรวจ error rate จาก JTL content จริงๆ

**เฉลยที่ 3:** ตัวอย่าง plan 5 วัน: วันที่ 1-2: Smoke test (5-10 users) ตรวจ critical flows ทำงานได้ + Load test ที่ expected load (100-500 concurrent) บน staging; วันที่ 3: Stress test เพื่อหา breaking point; วันที่ 4: Spike test simulate burst traffic ช่วง promotion; วันที่ 5: Soak test 2-4 ชั่วโมงตรวจ memory leak; Tools: JMeter 5.6.3 บน staging environment; Success criteria: p95 < 2,000ms, Error% < 1%, Throughput ≥ expected req/s; เครื่องมือ: JMeter + HTML report + monitoring (CPU/RAM/DB) บน staging ที่ใกล้เคียง production

</details>

> **Generation Effect Reminder:** ปิดเอกสารนี้แล้วเขียน "anti-patterns 5 อันดับแรก" ด้วยคำพูดตัวเอง พร้อมอธิบายว่าแต่ละอันส่งผลเสียอย่างไร — แล้วค่อยกลับมาเปรียบเทียบ
>
> **Remediation path:** ถ้าตอบคำถามที่ 1 ไม่ได้ → กลับอ่านบทที่ 7 (Assertions) + บทที่ 8 (HTML Report) | ถ้าตอบคำถามที่ 2 ไม่ได้ → กลับอ่านบทที่ 2 (CLI flags) + section 4.6 (CI/CD) ในบทนี้ | ถ้าตอบคำถามที่ 3 ไม่ได้ → กลับอ่านบทที่ 1 (Performance Test Types) และทบทวน overview ของ series ทั้งหมด
