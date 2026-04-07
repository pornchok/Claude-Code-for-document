# บทที่ 8: Running Tests & HTML Dashboard Reports

> เวลาโดยประมาณ: 45–60 นาที

---

## ⏰ Pre-chapter Retrieval

> แนะนำ: อ่านบทนี้หลังจากผ่านไปอย่างน้อย 1 วันหลังบทที่ 7

**ก่อนอ่านบทนี้ ลองตอบ:**

ตอนนี้คุณมี test plan ที่สมบูรณ์แล้ว — มี Thread Group, HTTP Requests, CSV Data Set Config สำหรับ parameterization, และ Assertions ตรวจ response ครบทุก critical request

**คำถาม 2 ข้อ:**

1. ถ้าจะรัน load test จริง (ไม่ใช่แค่ debug script) คุณควรรันจาก JMeter GUI หรือ command line? ทำไม?

2. หลังจาก test รันเสร็จ คุณจะรู้ได้อย่างไรว่าระบบ "ผ่าน" หรือ "ไม่ผ่าน"? ตัวเลขไหนที่ดูก่อน?

เขียนคำตอบลงกระดาษ — อย่า scroll ข้ามเฉลยก่อน และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น

---

> **เฉลย:**
>
> 1. ควรรันจาก **command line (CLI mode)** เสมอ เพราะ JMeter official docs ระบุชัดเจนว่า: *"GUI mode should only be used for creating the test script, CLI mode (NON GUI) must be used for load testing"* — GUI ใช้ memory และ CPU ส่วนหนึ่งในการ render UI ทำให้ JMeter engine มี resource น้อยลงสำหรับสร้าง load จริง ผลที่ได้จาก GUI mode จึงไม่น่าเชื่อถือ
>
> 2. ดู **Error%** ก่อน — ถ้า error rate สูงกว่า threshold ที่กำหนด (เช่น > 1%) ระบบยังไม่พร้อม หลังจากนั้นดู **90th percentile** เพราะบอกว่า 90% ของ users ได้รับ response ภายในกี่ ms — ตัวเลขนี้สะท้อนประสบการณ์จริงของ users ได้ดีกว่า Average
>
> **Generation effect:** ก่อนอ่านต่อ ลองเขียนด้วยคำของตัวเองว่า "ทำไม Average response time ถึงอาจทำให้เข้าใจผิด" ในหนึ่งประโยค — เขียนก่อนอ่าน section 4 เพื่อ activate prior knowledge
>
> **Remediation path:** ถ้าตอบข้อ 1 ว่า "GUI ได้เพราะสะดวกกว่า" — อ่าน section 4.1 แรก แล้วกลับมาอธิบายใหม่ว่าทำไมถึงไม่ใช่คำตอบที่ถูก

---

## 1. วัตถุประสงค์

เมื่ออ่านบทนี้จบ คุณจะสามารถ:

- **รัน** JMeter test ด้วย CLI mode และอธิบายความหมายของทุก flag ในคำสั่ง
- **Generate** HTML Dashboard Report ทั้งแบบ during test run และ from existing results file
- **อ่าน** Aggregate Report ได้ครบทุกคอลัมน์ รวมถึง 90th/95th/99th percentile, Error%, และ Throughput
- **อธิบาย** ทำไม 90th percentile ถึงสำคัญกว่า Average และ Average ซ่อนข้อมูลอะไร
- **ตีความ** APDEX score และผลจาก HTML Dashboard เพื่อระบุว่าระบบ "ผ่าน" หรือ "ไม่ผ่าน" SLA ได้
- **เปรียบเทียบ** ผลจาก 2 test runs เพื่อ detect regression และ validate optimization

---

## 2. ทำไมต้องรู้?

ลองนึกภาพว่าคุณเป็น QA engineer ที่รัน load test เสร็จแล้ว — ข้อมูลหลายพันบรรทัดอยู่ใน log file คำถามคือ: **คุณจะสรุปผลให้ทีมฟังอย่างไรใน 2 นาที?**

ถ้าดูแค่ตัวเลขส่วนตัว เช่น "average 300ms รู้สึกว่าโอเค" — คุณอาจพลาดว่า 10% ของ users รอนาน 3 วินาที ซึ่งในระบบที่มี 10,000 requests นั่นคือ 1,000 users ที่ได้ประสบการณ์ที่แย่

บทนี้จะให้คุณอ่าน report ได้อย่างมืออาชีพ — ไม่ใช่แค่ "ตัวเลขดูดี" แต่เข้าใจจริงว่าแต่ละตัวเลขบอกอะไร และ trade-off ที่ต้องตัดสินใจคืออะไร

---

## 3. Analogy: รายงานผลการแข่งขันวิ่งมาราธอน

ลองนึกภาพว่าคุณเป็น coach ดูแลนักวิ่ง 1,000 คนในการแข่งมาราธอน

**Aggregate Report** = ตารางสรุปผลทุกคน ระบุ เวลาเร็วสุด เวลาช้าสุด เวลาเฉลี่ย และที่สำคัญกว่า — **"90% ของนักวิ่งเข้าเส้นชัยก่อน 4 ชั่วโมง 30 นาที"** (90th percentile)

**Average** เฉลี่ยอาจดูดี แต่ถ้านักวิ่ง 50 คนล้มกลางทาง (DNF) แต่อีก 950 คนวิ่งเร็ว Average อาจยังอยู่ในเกณฑ์ดี — ทั้งที่จริงๆ 5% fail ไปแล้ว **Error%** จะจับตรงนี้

**HTML Dashboard** = วิดีโอ replay ทั้งการแข่ง ไม่ใช่แค่ตาราง — เห็นว่า speed ลดลงช่วงไหน (Over Time charts) มีนักวิ่งตะคริวที่กิโลเมตรที่ 30 ไหม (latency spike)

**APDEX** = คะแนนความพึงพอใจนักวิ่ง: วิ่งเสร็จได้เวลาตามเป้าหรือเปล่า — 1.0 คือทุกคนพอใจ, ต่ำกว่า 0.5 คือเกินครึ่งไม่พอใจ

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:** 90th percentile = ค่าเฉลี่ยของ 90% นักวิ่งที่เร็วที่สุด — ผิด 90th percentile คือค่าที่ **90% ของ samples มีค่าต่ำกว่า** หรือพูดง่ายๆ ว่า "ช้ากว่านี้มีแค่ 10%" — ตัวอย่าง: p90 = 500ms หมายถึง 90% ของ requests ตอบภายใน 500ms และอีก 10% ช้ากว่านั้น — ถ้าเข้าใจผิดว่าเป็นค่าเฉลี่ยจะอ่าน SLA report ผิดและตัดสินใจผิด

---

## 4. เนื้อหาหลัก

### 4.1 CLI Mode: วิธีที่ถูกต้องในการรัน Load Test

✅ **Best practice: รัน load test ด้วย CLI mode เสมอ ไม่ใช่ GUI mode**

> *"GUI mode should only be used for creating the test script, CLI mode (NON GUI) must be used for load testing"*
> *(source: https://jmeter.apache.org/usermanual/get-started.html)*

เหตุผลที่ GUI mode ไม่เหมาะสำหรับ load test:

1. **Resource overhead:** GUI ต้องใช้ CPU และ memory ในการ render charts, tables, และ real-time updates — resource เหล่านี้ถูกแย่งจาก JMeter engine ที่ต้องสร้าง virtual users
2. **Memory leak เสี่ยงกว่า:** View Results Tree และ Listeners ใน GUI mode เก็บ response data ทั้งหมดไว้ใน memory — ถ้า test รันนาน memory จะเต็มและ JMeter จะ crash
3. **ผลไม่น่าเชื่อถือ:** เวลา response อาจสูงขึ้นเพราะ JMeter machine เองช้า ไม่ใช่เพราะ server ช้า

**คำสั่ง CLI mode มาตรฐาน:**

> *"Use CLI mode: jmeter -n -t test.jmx -l test.jtl"*
> *(source: https://jmeter.apache.org/usermanual/best-practices.html)*

> *"jmeter -n -t <test JMX file> -l <test log file> -e -o <Path to output folder>"*
> *(source: https://jmeter.apache.org/usermanual/generating-dashboard.html)*

**คำสั่งเต็มพร้อมอธิบาย flag:**

```bash
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3
jmeter -n -t test.jmx -l results.jtl -e -o ./output-report
```

| Flag | ความหมาย | ตัวอย่าง |
|------|-----------|---------|
| `-n` | Non-GUI mode — รัน JMeter โดยไม่เปิด GUI | (ไม่มี argument) |
| `-t` | Test file — path ของ .jmx file ที่ต้องการรัน | `-t my-api-test.jmx` |
| `-l` | Log file — path สำหรับ save ผล (ไฟล์ .jtl) | `-l results-20260317.jtl` |
| `-e` | Generate dashboard — สร้าง HTML report หลัง test จบ | (ต้องใช้คู่กับ `-o`) |
| `-o` | Output folder — folder สำหรับ HTML report | `-o ./reports/run-001` |

> CLI flags ที่ verify ได้: `-n`, `-t`, `-l` มาจาก source:
> *"-n: This specifies JMeter is to run in cli mode" / "-t [name of JMX file that contains the Test Plan]" / "-l [name of JTL file to log sample results to]"*
> *(source: https://jmeter.apache.org/usermanual/get-started.html)*

> `-e` และ `-o` มาจาก:
> *"jmeter -n -t <test JMX file> -l <test log file> -e -o <Path to output folder>"*
> *(source: https://jmeter.apache.org/usermanual/generating-dashboard.html)*

**วิธี generate report จาก existing results file (รัน test แยกต่างหาก):**

```bash
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3
# Generate HTML report จาก .jtl ที่มีอยู่แล้ว
jmeter -g results.jtl -o ./output-report
```

> *"jmeter -g <log file> -o <Path to output folder>"*
> *(source: https://jmeter.apache.org/usermanual/generating-dashboard.html)*

---

### 4.2 Aggregate Report: อ่านทุก Column

Aggregate Report แสดงสถิติสรุปของทุก HTTP Request ใน test plan — เป็น listener ที่ประหยัด memory กว่า View Results Tree เพราะ:

> *"samples with the same elapsed time are aggregated. Less memory is now needed."*
> *(source: https://jmeter.apache.org/usermanual/listeners.html)*

**ตัวอย่าง Aggregate Report:**

| Label | Samples | Average | Median | 90th%ile | 95th%ile | 99th%ile | Min | Max | Error% | Throughput | KB/sec |
|-------|---------|---------|--------|----------|----------|----------|-----|-----|--------|------------|--------|
| GET /api/products | 5000 | 245 | 198 | 520 | 780 | 1450 | 12 | 2340 | 0.24% | 83.3/sec | 412.5 |
| POST /api/orders | 2500 | 380 | 310 | 820 | 1100 | 1980 | 45 | 3210 | 1.20% | 41.6/sec | 85.2 |

**คำอธิบายแต่ละ column:**

**Samples** — จำนวน requests ทั้งหมดที่ส่งไป
*(source: Apache JMeter GitHub — `messages.properties`: `aggregate_report_count=# Samples`; implementation: `getCount()` ใน `StatCalculator.java` — accumulates total sample count)*

**Average** — response time เฉลี่ยทุก request (ms)
*(source: Apache JMeter GitHub — `messages.properties`: `average=Average`; implementation: `getMeanAsNumber()` = `(long)(sum / count)` ใน `StatCalculator.java`)*

**Median** — response time ที่ 50th percentile

> *"Median is a number which divides the samples into two equal halves. The Median is the same as the 50th Percentile"*
> *(source: https://jmeter.apache.org/usermanual/glossary.html)*

**90th%ile / 95th%ile / 99th%ile** — response time ที่ X% ของ requests อยู่ต่ำกว่า

> *"Get the value which %percent% of the values are less than. This works just like median (where median represents the 50% point)."*
> *(source: https://jmeter.apache.org/api/org/apache/jorphan/math/StatCalculator.html)*

สำหรับ **90th%ile** โดยเฉพาะ:
> *"The value below which 90% of the samples fall. The remaining samples take at least as long as this value."*
> *(source: https://jmeter.apache.org/usermanual/glossary.html)*

**Min** — response time เร็วที่สุด (ms)
*(source: Apache JMeter GitHub — `messages.properties`: `aggregate_report_min=Min`; implementation: `getMin()` ใน `StatCalculator.java` — minimum observed value, returns 0 if negative)*

**Max** — response time ช้าที่สุด (ms)
*(source: Apache JMeter GitHub — `messages.properties`: `aggregate_report_max=Max`; implementation: `getMax()` ใน `StatCalculator.java` — maximum observed response time)*

**Error%** — สัดส่วน requests ที่ fail

> *"Returns the raw double value of the percentage of samples with errors that were recorded. (Between 0.0 and 1.0)"*
> *(source: https://jmeter.apache.org/api/org/apache/jmeter/util/Calculator.html)*

**Throughput** — จำนวน requests ต่อหน่วยเวลา

> *"Throughput is calculated as requests/unit of time. The time is calculated from the start of the first sample to the end of the last sample. The formula is: Throughput = (number of requests) / (total time)."*
> *(source: https://jmeter.apache.org/usermanual/glossary.html)*

**KB/sec** — ปริมาณข้อมูลที่รับ/ส่งต่อวินาที

> *"Sent bytes throughput in kilobytes / second"*
> *(source: https://jmeter.apache.org/api/org/apache/jmeter/util/Calculator.html)*

---

### 4.3 ทำไม 90th Percentile สำคัญกว่า Average

✅ **Best practice: ดู 90th/95th percentile แทน Average เพราะ Average ซ่อน outliers**

ลองดูตัวอย่างนี้ — test มี 10 requests:

```
Response times (ms): 100, 105, 98, 102, 110, 99, 104, 108, 95, 2000
```

- **Average** = (100+105+98+102+110+99+104+108+95+2000) / 10 = **292ms** ← ดูเหมือนช้าพอสมควร
- **Median (50th%ile)** = 103ms ← 50% ของ users ได้รับในเวลานี้
- **90th%ile** = ประมาณ 200ms ← 90% ของ users ได้รับภายใน 200ms
- **99th%ile** = 2000ms ← มี request หนึ่งที่ช้ามาก

Average "ถูกดึง" ขึ้นไปเป็น 292ms เพราะ outlier 1 ตัว (2000ms) — ทั้งที่จริงๆ 9 ใน 10 requests เสร็จใน ~100ms

ในระบบที่มี 10,000 requests ถ้า 1% (100 requests) ใช้เวลา 10 วินาที Average อาจดูยอมรับได้ แต่นั่นหมายถึง 100 users ต้องรอ 10 วินาที — ประสบการณ์ที่แย่มากสำหรับ users เหล่านั้น

**ที่ใช้กัน:** SLA มักกำหนดเป็น percentile เช่น "p90 ต้องไม่เกิน 1000ms" ไม่ใช่ "average ต้องไม่เกิน 1000ms"

---

### 4.4 HTML Dashboard Report: ส่วนประกอบหลัก

เมื่อรัน test ด้วย `-e -o` JMeter จะ generate folder ที่มีไฟล์ HTML — เปิด `index.html` ในนั้น

> *"request summary graph showing the Success and failed requests percentage" / "Statistics table providing in one table a summary of all metrics per transaction" / "error table providing a summary of all errors"*
> *(source: https://jmeter.apache.org/usermanual/generating-dashboard.html)*

**ส่วนประกอบหลัก 4 ส่วน:**

**1. Statistics Table** — ตาราง summary เหมือน Aggregate Report แต่ interactive มี sorting และ filtering ได้

**2. APDEX Table** — Application Performance Index score ต่อแต่ละ transaction

> *"APDEX (Application Performance Index) table that computes for every transaction the APDEX based on configurable values for tolerated and satisfied thresholds"*
> *(source: https://jmeter.apache.org/usermanual/generating-dashboard.html)*

APDEX score อยู่ระหว่าง 0–1.0:
- **1.0** = ทุก request อยู่ใน "Satisfied" threshold
- **0.7–0.85** = acceptable
- **< 0.5** = มากกว่าครึ่งของ users ได้รับประสบการณ์ที่ไม่ดี

Default thresholds:
> *"apdex_satisfied_threshold: Sets the satisfaction threshold for the APDEX calculation (in ms). Default: 500"*
> *(source: https://jmeter.apache.org/usermanual/generating-dashboard.html)*

> *"apdex_tolerated_threshold: Sets the tolerance threshold for the APDEX calculation (in ms). Default: 1500"*
> *(source: https://jmeter.apache.org/usermanual/generating-dashboard.html)*

หมายความว่า:
- Response < 500ms = **Satisfied** (นับเต็ม)
- 500ms–1500ms = **Tolerated** (นับครึ่ง)
- Response > 1500ms = **Frustrated** (ไม่นับ)

**3. Over Time Charts** — กราฟแสดง response time, active threads, throughput ตามเวลา — ช่วยดูว่ามี degradation หรือ spike เกิดขึ้นตอนไหนระหว่าง test

**4. Error Table** — สรุป error ทุกประเภท จำนวน และ % ของ total — ช่วยระบุว่า error มาจาก assertion fail, network timeout, หรือ server error

---

### 4.5 การอ่านผล: Error% > 1% หมายถึงอะไร?

ไม่มีเกณฑ์สากลที่ตายตัว ขึ้นอยู่กับ system requirement แต่ rule of thumb ทั่วไป:

| Error% | ความหมาย | action |
|--------|-----------|--------|
| 0% | สมบูรณ์แบบหรือ assertions ไม่ครอบคลุมพอ | ตรวจว่า assertions ครอบคลุมจริง |
| < 0.1% | ยอมรับได้สำหรับระบบ high-availability | Monitor ต่อ |
| 0.1–1% | ต้องสืบสวน — อาจเป็น intermittent issue หรือ timeout | ดู error type ใน Error Table |
| > 1% | ปัญหาชัดเจน — ไม่ควร go-live | Fix ก่อน retest |
| > 5% | Critical — ระบบไม่พร้อม | Stop deployment |

**การอ่าน Error Table:** ดูว่า error ส่วนใหญ่เป็น type อะไร
- "Non HTTP response code" = network issue, timeout
- "500 Internal Server Error" = application bug
- Assertion failure message = logic error ใน response

⏸ **หยุดคิด (backward retrieval):** ถ้า Aggregate Report แสดง Error% = 3% สำหรับ POST /checkout แต่ Error Table แสดงว่าทุก error เป็น "Duration Assertion failed" — นั่นหมายความว่าอะไร? ระบบมีปัญหาอะไรและควรทำอะไรต่อ? เขียนคำตอบลงกระดาษก่อน — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น — แล้วค่อยอ่านต่อ

> **เฉลย:** 3% ของ /checkout requests ใช้เวลาเกิน Duration threshold ที่ตั้งไว้ (เช่น 3000ms) — server ไม่ได้ return error แต่ตอบช้าเกิน SLA ขั้นตอนต่อไป: ดู 90th/99th percentile ของ /checkout ว่าช้าแค่ไหน, ดู Over Time charts ว่าเกิดช้าตลอดหรือแค่บางช่วง (spike), จากนั้นสืบสวน bottleneck ใน application (database query, external API call, etc.)

⏸ **หยุดคิด (Bloom's L4 — Analysis):** ระบบมี SLA กำหนดว่า p90 ต้องไม่เกิน 800ms และ error rate ต้องไม่เกิน 0.5% — Aggregate Report แสดง p90 = 650ms แต่ error rate = 0.8% — คุณจะ report ผลนี้ต่อ manager อย่างไร? ผ่านหรือไม่ผ่าน? และขั้นตอนถัดไปคืออะไร?

> **เฉลย:** **ไม่ผ่าน SLA** — p90 ผ่าน (650ms < 800ms) แต่ error rate ไม่ผ่าน (0.8% > 0.5%) SLA ทั้งสองเงื่อนไขต้องผ่านพร้อมกัน ขั้นตอนถัดไป: ดู error table เพื่อหา root cause ของ error 0.8%, fix แล้ว retest — อย่า go-live จนกว่าทั้งสอง criteria จะผ่านพร้อมกัน

---

## 5. ตัวอย่าง 3 ระดับ

### ระดับ Beginner: รัน JMeter CLI Mode และผลที่ควรเห็น

สถานการณ์: test plan ชื่อ `api-test.jmx` มี Thread Group 50 users, Ramp-Up 30 วินาที, Loop 10 ครั้ง

**คำสั่งรัน:**

```bash
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3

jmeter \
  -n \          # Non-GUI mode (จำเป็น)
  -t api-test.jmx \  # test plan file
  -l results-$(date +%Y%m%d-%H%M%S).jtl \  # timestamp ใน filename ช่วย version tracking
  -e \          # enable HTML report generation
  -o ./reports/run-001  # output folder (ต้องไม่มีอยู่ก่อน — ถ้ามีแล้ว JMeter จะ error)
```

หรือแบบบรรทัดเดียว:

```bash
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3
jmeter -n -t api-test.jmx -l results-20260317-103045.jtl -e -o ./reports/run-001
```

**Terminal output ที่ควรเห็น (ตัวอย่าง):**

```
Creating summariser <summary>
Created the tree successfully using api-test.jmx
Starting standalone test @ 2026-03-17 10:30:45 (1742200245)
Waiting for possible Shutdown/StopTestNow/HeapDump/ThreadDump message on port 4445
Warning: Nashorn engine is planned to be removed from a future JDK release
summary +    500 in 00:00:30 = 16.7/s Err:     0 (0.00%) Active: 50 Started: 50 Finished: 0
summary +    800 in 00:00:30 = 26.7/s Err:     2 (0.25%) Active: 50 Started: 50 Finished: 0
summary =   1300 in 00:01:00 = 21.7/s Err:     2 (0.15%) Active: 50 Started: 50 Finished: 0
summary +   2200 in 00:01:30 = 49.4/s Err:     0 (0.00%) Active: 50 Started: 50 Finished: 500
summary =   3500 in 00:02:00 = 29.2/s Err:     2 (0.06%) Active: 0 Started: 50 Finished: 50
Tidying up ...    @ 2026-03-17 10:32:45 (1742200365)
... end of run
Generating Dashboard
dashboard generated
```

**อ่าน output อย่างไร:**
- `summary +` = สถิติในช่วงเวลานั้น, `summary =` = สถิติสะสมทั้งหมด
- `16.7/s` = throughput ณ ขณะนั้น
- `Err: 0 (0.00%)` = จำนวน error และ %
- `Active: 50` = threads ที่กำลัง run อยู่

**HTML Report ที่ generate:**

```
./reports/run-001/
├── index.html          ← เปิดไฟล์นี้ใน browser
├── content/
│   ├── js/
│   └── pages/
│       ├── Overview.html
│       └── ...
└── sbadmin2-1.0.7/    ← CSS/JS framework ของ dashboard
```

---

### ระดับ Intermediate: วิเคราะห์ Aggregate Report ของ Healthcare System

สถานการณ์: ระบบ hospital management มี SLA กำหนดว่า:
- `GET /api/patient-records`: p90 ≤ 1000ms, error rate ≤ 0.1%
- `POST /api/appointments`: p90 ≤ 1500ms, error rate ≤ 0.5%

**Aggregate Report หลัง load test (100 concurrent users, 30 นาที):**

| Label | Samples | Average | Median | 90th%ile | 95th%ile | 99th%ile | Min | Max | Error% | Throughput |
|-------|---------|---------|--------|----------|----------|----------|-----|-----|--------|------------|
| GET /api/patient-records | 45,000 | 612 | 480 | **1,240** | 1,890 | 3,450 | 45 | 6,230 | **0.08%** | 25.0/sec |
| POST /api/appointments | 18,000 | 890 | 720 | **1,380** | 2,100 | 4,100 | 120 | 8,900 | **2.50%** | 10.0/sec |

**การวิเคราะห์:**

**GET /api/patient-records:**
- p90 = 1,240ms > SLA 1,000ms → ❌ **ไม่ผ่าน p90 SLA**
- Error rate = 0.08% < 0.1% → ✅ ผ่าน error SLA
- แม้ error rate ผ่าน แต่ latency ไม่ผ่าน ถือว่า **fail overall**

สังเกต: Average = 612ms ดูเหมือนยอมรับได้ แต่ p90 = 1,240ms บอกว่า 10% ของ records access ช้ากว่า 1.2 วินาที — ในระบบ healthcare ที่หมอต้องดูข้อมูลผู้ป่วยอย่างรวดเร็ว นี่คือปัญหาจริง

**POST /api/appointments:**
- p90 = 1,380ms < SLA 1,500ms → ✅ ผ่าน p90 SLA
- Error rate = 2.50% > 0.5% → ❌ **ไม่ผ่าน error SLA**
- ผล: **fail overall** — error rate สูงมาก ต้องสืบสวน error type

**ขั้นตอนถัดไป:**
1. **GET /patient-records:** ตรวจ database query plan — อาจมี missing index, N+1 query, หรือ cache miss ทำให้ slow
2. **POST /appointments:** ดู Error Table ว่า 2.5% error มาจากอะไร — ถ้าเป็น "500 Internal Server Error" อาจมี concurrency bug (race condition ใน appointment booking logic)
3. Fix + retest ก่อน go-live

---

### ระดับ Advanced: เปรียบเทียบ Baseline vs After Optimization

สถานการณ์: ทีมทำ database optimization (เพิ่ม index และ query caching) แล้วรัน test ซ้ำเพื่อ validate

**Test Run 1 (Baseline — ก่อน optimize):**

| Label | Samples | Average | 90th%ile | 99th%ile | Error% | Throughput |
|-------|---------|---------|----------|----------|--------|------------|
| GET /products | 30,000 | 480 | 1,150 | 3,200 | 0.15% | 50.0/sec |
| POST /orders | 15,000 | 620 | 1,800 | 5,400 | 0.80% | 25.0/sec |
| GET /inventory | 20,000 | 890 | 2,400 | 7,100 | 1.20% | 33.3/sec |

**Test Run 2 (After Optimization):**

| Label | Samples | Average | 90th%ile | 99th%ile | Error% | Throughput |
|-------|---------|---------|----------|----------|--------|------------|
| GET /products | 30,000 | 185 | 380 | 820 | 0.06% | 52.1/sec |
| POST /orders | 15,000 | 310 | 680 | 1,450 | 0.25% | 26.8/sec |
| GET /inventory | 20,000 | 920 | 2,600 | 7,400 | 1.35% | 33.0/sec |

**การอ่านผลเปรียบเทียบ:**

**GET /products** — **ปรับปรุงชัดเจน:**
- Average: 480ms → 185ms (-61%)
- p90: 1,150ms → 380ms (-67%)
- Error%: 0.15% → 0.06% ดีขึ้น
- Optimization ได้ผลดีมาก

**POST /orders** — **ปรับปรุงได้ผล:**
- p90: 1,800ms → 680ms (-62%)
- Error%: 0.80% → 0.25% ดีขึ้น แต่ยังอาจต้องตรวจ SLA

**GET /inventory** — **⚠️ Regression Sign:**
- Average: 890ms → 920ms (+3%)
- p90: 2,400ms → 2,600ms (+8%)
- Error%: 1.20% → 1.35% แย่ลง
- Throughput ลดเล็กน้อย: 33.3 → 33.0/sec

GET /inventory **แย่ลง** หลัง optimization — นี่คือ regression sign

**การสืบสวน regression:**
1. ตรวจว่า optimization ใดที่ทำ — อาจมี side effect กับ inventory queries
2. ดู Over Time charts เปรียบเทียบ 2 runs — inventory latency เพิ่มขึ้นตลอดหรือมี spike ช่วงใดช่วงหนึ่ง
3. ตรวจว่า database index ที่เพิ่มสำหรับ products ทำให้ inventory queries ช้าลงไหม (index overhead ต่อ write operations)
4. ตรวจ query execution plan ของ inventory queries หลัง optimization

**หลักการ:** ต้องรัน test ทั้ง 2 runs ด้วย **เงื่อนไขเดียวกัน** ทุกอย่าง — thread count, ramp-up, duration, data set, target server — ถ้า environment ต่างกันผลเปรียบเทียบไม่ valid

---

## 6. Common Mistakes

### ❌ (a) รัน Load Test ด้วย GUI Mode

**แบบผิด:**
เปิด JMeter GUI แล้วกด Run (หรือ Start) เพื่อรัน test plan ที่มี 500+ threads

**แบบถูก:**
```bash
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3
jmeter -n -t test.jmx -l results.jtl -e -o ./output
```

🔍 **สิ่งที่เกิดขึ้น:** GUI mode ใช้ CPU และ memory ส่วนหนึ่งในการ render UI — JMeter machine มี resource น้อยลงสำหรับสร้าง virtual users ผลที่ได้คือ throughput ต่ำกว่าจริง response time สูงกว่าจริง และ report ที่ได้ไม่สะท้อนความสามารถจริงของ server

🤔 **เหตุผล:** JMeter เป็น Java application ที่ต้องแย่ง resource กันระหว่าง rendering engine และ load testing engine

*(source: https://jmeter.apache.org/usermanual/get-started.html — "GUI mode should only be used for creating the test script, CLI mode (NON GUI) must be used for load testing")*

---

### ❌ (b) ดู Average แทน Percentile ในการตัดสิน SLA

**แบบผิด:**
"Average response time = 400ms, SLA กำหนด 500ms — ผ่าน!"

**แบบถูก:**
"p90 = 950ms, SLA กำหนด p90 ≤ 800ms — ไม่ผ่าน แม้ Average จะดูโอเค"

🔍 **สิ่งที่เกิดขึ้น:** Average ถูก distort โดย distribution — ถ้า 80% ของ requests เสร็จใน 200ms แต่ 20% ใช้เวลา 2000ms, Average อาจได้ 560ms ซึ่งดูเหมือนยอมรับได้ แต่ 20% คือ users 1 ใน 5 ที่ได้รับ experience ที่แย่

🤔 **เหตุผล:** SLA ที่ดีกำหนดด้วย percentile เพราะสะท้อนประสบการณ์ของ users จริงๆ ได้ดีกว่า Average ที่ถูก outlier ดึงให้เบี่ยงเบน

*(source: https://jmeter.apache.org/usermanual/glossary.html — "The value below which 90% of the samples fall.")*

---

### ❌ (c) Output Directory มีอยู่แล้ว

**แบบผิด:**
```bash
# รัน command นี้ 2 ครั้งด้วย -o folder เดิม
jmeter -n -t test.jmx -l results.jtl -e -o ./reports/run-001
jmeter -n -t test.jmx -l results.jtl -e -o ./reports/run-001  # ← ERROR
```

Error message ที่จะเห็น:
```
An error occurred: Cannot write to '/path/to/reports/run-001' as folder is not empty
```

**แบบถูก:**
```bash
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3

# Option 1: ใช้ folder ชื่อใหม่ทุกครั้ง (แนะนำ — เก็บ history ไว้เปรียบเทียบ)
jmeter -n -t test.jmx -l results.jtl -e -o ./reports/run-$(date +%Y%m%d-%H%M%S)

# Option 2: ลบ folder เก่าก่อน (ถ้าไม่ต้องการ history)
rm -rf ./reports/run-001 && jmeter -n -t test.jmx -l results.jtl -e -o ./reports/run-001
```

🔍 **สิ่งที่เกิดขึ้น:** JMeter ไม่ overwrite output folder — ถ้า folder มีอยู่แล้ว test จะรันสำเร็จ แต่ HTML report จะไม่ถูก generate และจะแสดง error ใน terminal

🤔 **เหตุผล:** การไม่ overwrite เป็น safety feature ป้องกันการสูญเสีย historical reports โดยไม่ตั้งใจ

*(source: https://jmeter.apache.org/usermanual/generating-dashboard.html)*

---

### ❌ (d) ลืม `-e -o` ทำให้ไม่ Generate HTML Report

**แบบผิด:**
```bash
jmeter -n -t test.jmx -l results.jtl
# รันได้ผ่าน แต่ไม่มี HTML report ให้ดู
```

**แบบถูก:**
```bash
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3

# Option 1: generate during test run
jmeter -n -t test.jmx -l results.jtl -e -o ./output-report

# Option 2: generate after จาก .jtl file ที่มีอยู่
jmeter -g results.jtl -o ./output-report
```

🔍 **สิ่งที่เกิดขึ้น:** ถ้าลืม `-e -o` จะมีแค่ไฟล์ .jtl ซึ่งเป็น raw data ในรูปแบบ CSV หรือ XML — อ่านด้วยมือได้ยาก และไม่มี charts หรือ APDEX score ให้ดู

🤔 **เหตุผล:** HTML report ต้องการ flag 2 ตัวทำงานร่วมกัน: `-e` บอก "generate report" และ `-o` บอก "save ไว้ที่ไหน" — ขาดตัวใดตัวหนึ่งไม่ได้

*(source: https://jmeter.apache.org/usermanual/generating-dashboard.html — "jmeter -n -t <test JMX file> -l <test log file> -e -o <Path to output folder>")*

---

## 7. สรุปบท

**คำถาม Retrieval — ตอบก่อนดูเฉลย:**

หยุดอ่าน เขียนคำตอบลงกระดาษก่อน อย่างน้อย 30 วินาทีต่อข้อ — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น ก่อนดูเฉลย

---

**คำถามที่ 1 (Code-based):** ดู command นี้:

```bash
jmeter -n -t load-test.jmx -l output.jtl -o ./results
```

มีปัญหา 2 จุด — ระบุปัญหาแต่ละจุดและเขียน command ที่ถูกต้อง

เขียนคำตอบก่อนดูเฉลย

---

> **เฉลย:**
>
> **ปัญหาที่ 1:** ขาด flag `-e` — ไม่มี `-e` ทำให้ `-o` ถูก ignore และ HTML report จะไม่ถูก generate
>
> **ปัญหาที่ 2:** `-o ./results` ถ้า folder `./results` มีอยู่แล้ว JMeter จะ error และ report ไม่ถูก generate
>
> **Command ที่ถูกต้อง:**
> ```bash
> # ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3
> jmeter -n -t load-test.jmx -l output.jtl -e -o ./results-$(date +%Y%m%d-%H%M%S)
> ```

---

**คำถามที่ 2 (Elaborative Interrogation):** ทำไม JMeter ถึงไม่ให้รัน load test ด้วย GUI mode? ถ้าจะอธิบายให้ manager ที่ไม่ technical เข้าใจ คุณจะใช้ analogy อะไร?

---

> **เฉลย (ตัวอย่าง):** GUI mode เหมือนนักวิ่งที่ต้องถือกล้องถ่ายรูปตัวเองพร้อมกับวิ่ง — กล้อง (GUI) ต้องใช้แขนและพลังงาน ทำให้วิ่งได้ช้ากว่าความเป็นจริง ผล report ก็จะบอกว่าตัวเองวิ่งช้าทั้งที่จริงๆ ปัญหาอยู่ที่การถือกล้อง — ไม่ใช่ความเร็ววิ่ง
>
> CLI mode = วิ่งโดยให้คนอื่นถ่ายรูป ผลที่ได้สะท้อนความเร็วที่แท้จริง

---

**Generation effect:** ก่อนปิดบท ลองเขียนด้วยคำของตัวเองว่า "ความแตกต่างระหว่าง Median, Average, และ 90th percentile คืออะไร และเมื่อไหรควรดูตัวเลขแต่ละตัว" — เขียนโดยไม่เปิดเอกสาร แล้วเปรียบเทียบกับสิ่งที่อ่านในบทนี้ หาจุดที่ยังไม่ครบหรือผิด

**Remediation path:**
- ยังสับสน Average vs Percentile → ลองสร้าง spreadsheet ด้วย response times ที่มี outlier แล้วคำนวณทั้งสามค่าด้วยมือ
- ยังสับสน `-e` vs `-g` → จำง่ายๆ: `-e` = "enable dashboard during test", `-g` = "generate from existing log"
- APDEX ยังไม่ชัดเจน → เปลี่ยน default threshold ใน `user.properties` แล้วรัน test ซ้ำเพื่อดูว่า score เปลี่ยนอย่างไร — การทดลองเองทำให้จำได้นานกว่าการอ่าน
