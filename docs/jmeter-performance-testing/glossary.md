# Glossary: Performance Testing ด้วย Apache JMeter

> คำศัพท์ทุกคำเรียงตาม Alphabetical order (A-Z) ตาม English term
> Format: **[Term]** คือ [ฟังก์ชัน/บทบาทหลัก] เพราะ [เหตุผลที่ถูกออกแบบมาแบบนี้] — อย่าสับสนกับ [คำที่คล้าย]

---

## Aggregate Report

**Aggregate Report** คือ Listener ใน JMeter ที่แสดง statistics ครบถ้วนต่อ transaction รวมถึง Median, 90th/95th/99th percentile, Average, Min, Max, Error%, และ Throughput เพราะต้องการให้นักทดสอบวิเคราะห์ distribution ของ response time ได้ครบถ้วนในที่เดียว — อย่าสับสนกับ **Summary Report** ซึ่งใช้ memory น้อยกว่าแต่ไม่มี Median และ percentile columns

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#Aggregate_Report
*First seen in:* `08-running-reports.md` — section Aggregate Report vs Summary Report

---

## APDEX

**APDEX** (Application Performance Index) คือ metric มาตรฐานที่วัดระดับความพึงพอใจของ user ต่อ response time โดยแบ่งออกเป็น 3 โซน (Satisfied, Tolerated, Frustrated) และคำนวณเป็น score 0.0–1.0 เพราะต้องการ metric เดียวที่สื่อสารกับ stakeholder ที่ไม่ใช่ technical ได้ง่าย — อย่าสับสนกับ **Error Rate** ซึ่งวัดว่า request fail กี่ % (APDEX วัดแค่ response time ไม่นับ error)

*SOURCE:* https://jmeter.apache.org/usermanual/generating-dashboard.html
*First seen in:* `08-running-reports.md` — section APDEX Table

---

## Assertion

**Assertion** คือ element ใน JMeter ที่ validate ว่า response ที่ได้รับตรงตาม expectation ที่กำหนดไว้ (เช่น status code, response body, response time) เพราะ HTTP 200 ไม่ได้แปลว่า business logic ถูกต้องเสมอ — server อาจ return 200 พร้อม error message ใน body — อย่าสับสนกับ **Listener** ซึ่งแค่ collect และ display results โดยไม่ตัดสินว่า pass/fail

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#assertions
*First seen in:* `07-assertions.md` (บทที่ 7)

---

## Breakpoint Test

**Breakpoint Test** คือการทดสอบที่เพิ่ม load เรื่อยๆ จนระบบ fail ด้วยเป้าหมายเพื่อหาจุดที่ระบบไม่สามารถรับ load ได้อีกต่อไป (breaking point) เพราะต้องการรู้ว่าระบบมี headroom เหลือเท่าไหร่เหนือจาก expected peak load — อย่าสับสนกับ **Stress Test** ซึ่งก็เกินกว่า capacity แต่มักมี target load ที่ defined ไว้ล่วงหน้า ส่วน Breakpoint Test ไม่รู้ target ล่วงหน้า

**SOURCE:** https://grafana.com/load-testing/types-of-load-testing/ (secondary source: Grafana k6 docs — "Breakpoint tests discover your system's limits.")
*First seen in:* `01-performance-testing-fundamentals.md` (บทที่ 1)

---

## Concurrent Users

**Concurrent Users** คือจำนวน virtual users ที่กำลัง active และส่ง request ไปยัง server พร้อมกันในช่วงเวลาเดียวกัน เพราะต้องการ model สถานการณ์ที่ใกล้เคียงกับ real-world load ที่ server ต้องรับพร้อมกัน — อย่าสับสนกับ **Total Users** (จำนวน user ทั้งหมดที่รัน test) ซึ่งรวม user ที่ ramp-up ยังไม่เสร็จหรือกำลัง think time อยู่ด้วย

**SOURCE:** https://jmeter.apache.org/usermanual/glossary.html (JMeter glossary — performance testing terminology)
*First seen in:* `02-installation-setup.md` (บทที่ 2)

---

## Constant Timer

**Constant Timer** คือ Timer element ใน JMeter ที่เพิ่ม delay คงที่ระหว่าง requests เสมอ (เช่น 2,000ms ทุกครั้ง) เพราะเหมาะกับ system-to-system integration ที่มี fixed interval ไม่ใช่ human behavior — อย่าสับสนกับ **Uniform Random Timer** (delay สุ่มในช่วงที่กำหนด) หรือ **Gaussian Random Timer** (delay กระจายแบบ normal distribution)

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#Constant_Timer
*First seen in:* `05-thread-group-best-practices.md` (บทที่ 5)

---

## Controller Node

**Controller Node** คือเครื่องที่รัน JMeter GUI หรือ CLI เพื่อควบคุม distributed test โดยทำหน้าที่ส่ง Test Plan ไปยัง Worker Nodes และ aggregate results กลับมา — ตัว Controller เองไม่ส่ง HTTP requests ไปยัง target server โดยตรง เพราะ design นี้ช่วยให้ Controller มี resource เพียงพอสำหรับ orchestration — อย่าสับสนกับ **Worker Node** ซึ่งเป็นตัวที่ generate load จริงๆ

*SOURCE:* https://jmeter.apache.org/usermanual/jmeter_distributed_testing_step_by_step.html
*First seen in:* `09-distributed-testing.md` — section Controller Node

---

## Coordinated Omission

**Coordinated Omission** คือ bias ใน performance testing ที่เกิดขึ้นเมื่อ load generator หยุดส่ง request ระหว่างที่รอ response (แทนที่จะส่ง request ต่อตาม schedule) ทำให้ผลการทดสอบ underestimate latency ของระบบจริง เพราะ request ที่ "รอไม่ออก" จะไม่ถูกนับใน distribution — อย่าสับสนกับ **Error Rate** ซึ่งนับ request ที่ fail ไม่ใช่ request ที่ไม่ถูกส่ง

**SOURCE:** https://jmeter.apache.org/usermanual/best-practices.html (JMeter best practices — "if you don't correctly size the number of threads, you will face the 'Coordinated Omission' problem")
*First seen in:* `01-performance-testing-fundamentals.md` (บทที่ 1)

---

## CSV Data Set Config

**CSV Data Set Config** คือ Configuration Element ใน JMeter ที่อ่านข้อมูล test จากไฟล์ CSV และ inject ค่าเป็น JMeter variables สำหรับใช้ใน requests เพราะการ hardcode ค่าใน Test Plan ทำให้ test ใช้ข้อมูลซ้ำซึ่งไม่สะท้อน real-world ที่ users ใช้ข้อมูลต่างกัน — อย่าสับสนกับ **User Defined Variables** ซึ่ง hardcode ค่าคงที่ไว้ใน Test Plan โดยตรง

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#CSV_Data_Set_Config
*First seen in:* `06-parameterization.md` (บทที่ 6)

---

## Duration Assertion

**Duration Assertion** คือ Assertion ใน JMeter ที่ fail request ถ้า response time เกิน threshold ที่กำหนด (เช่น > 3,000ms) เพราะ HTTP 200 ที่ใช้เวลา 10 วินาทีอาจยัง "pass" HTTP layer แต่ถือว่า "fail" จาก user experience perspective — อย่าสับสนกับ **Response Assertion** ซึ่งตรวจ content หรือ status code ไม่ใช่ timing

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#Duration_Assertion
*First seen in:* `07-assertions.md` (บทที่ 7)

---

## Endurance Test (Soak Test)

**Endurance Test** (หรือ Soak Test) คือการทดสอบที่รัน load ปกติเป็นเวลานาน (ชั่วโมงถึงวัน) เพื่อหาปัญหาที่ปรากฏช้าๆ เช่น memory leak, connection leak, หรือ disk space exhaustion เพราะปัญหาบางอย่างไม่เกิดในช่วง 30 นาทีแรก แต่จะค่อยๆ สะสมและพังในชั่วโมงที่ 4 — อย่าสับสนกับ **Stress Test** ซึ่งเพิ่ม load เกิน capacity แต่มักรัน duration สั้นกว่า

**SOURCE:** https://grafana.com/load-testing/types-of-load-testing/ (secondary source: Grafana k6 docs — "Soak tests are a variation of the average-load test. The main difference is the test duration.")
*First seen in:* `01-performance-testing-fundamentals.md` (บทที่ 1)

---

## Error Rate

**Error Rate** (หรือ Error%) คือสัดส่วน requests ที่ fail เทียบกับ total requests ใน test โดย "fail" หมายรวมทั้ง HTTP error (4xx, 5xx), connection timeout, และ JMeter Assertion failure เพราะต้องการ metric เดียวที่บอกว่า "ระบบตอบ request ได้ถูกต้องกี่ %?" — อย่าสับสนกับ **APDEX** ซึ่งวัดแค่ response time satisfaction ไม่นับ error type

*SOURCE:* https://jmeter.apache.org/usermanual/glossary.html
*First seen in:* `08-running-reports.md` (บทที่ 8)

---

## Gaussian Random Timer

**Gaussian Random Timer** คือ Timer element ใน JMeter ที่ generate delay ตาม normal distribution โดยกำหนด mean และ standard deviation เพราะพฤติกรรมของ user จริงๆ มักกระจายแบบ bell curve รอบค่ากลาง ไม่ใช่ uniform distribution — อย่าสับสนกับ **Uniform Random Timer** ซึ่งมี equal probability ทุกค่าในช่วง ไม่มี "most likely" value

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#Gaussian_Random_Timer
*First seen in:* `05-thread-group-best-practices.md` (บทที่ 5)

---

## HTTP Header Manager

**HTTP Header Manager** คือ Configuration Element ใน JMeter ที่ add หรือ override HTTP headers ในทุก request ที่อยู่ใน scope เดียวกัน เพราะ API ส่วนใหญ่ต้องการ headers เช่น `Authorization`, `Content-Type`, `Accept` ที่ต้องส่งทุก request — อย่าสับสนกับ **HTTP Cookie Manager** ซึ่ง manage cookies โดยเฉพาะ ไม่ใช่ arbitrary headers

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#HTTP_Header_Manager
*First seen in:* `03-ui-test-plan-structure.md` (บทที่ 3)

---

## HTTP Request Sampler

**HTTP Request Sampler** คือ Sampler element หลักใน JMeter ที่ส่ง HTTP/HTTPS request ไปยัง server และรอรับ response เพราะ Sampler คือ element ที่ "ทำงานจริง" ใน Test Plan (Listener แค่ดูผล, Timer แค่รอ แต่ Sampler ส่ง request) — อย่าสับสนกับ **HTTP Request Defaults** ซึ่งเป็น Config Element ที่ตั้ง default values ไม่ส่ง request เอง

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#HTTP_Request
*First seen in:* `03-ui-test-plan-structure.md` (บทที่ 3)

---

## JMeter

**JMeter** คือ open-source load testing tool ที่พัฒนาโดย Apache Software Foundation รันบน JVM ใช้สำหรับ performance testing ของ web applications, APIs, databases, และ services อื่นๆ เพราะออกแบบมาให้ extensible ผ่าน plugins และรองรับ distributed testing สำหรับ high-load scenarios — อย่าสับสนกับ **Gatling**, **k6**, หรือ **Locust** ซึ่งเป็น load testing tools ทางเลือก (JMeter มี GUI ที่ click-based ซึ่งต่างจาก code-based tools)

*SOURCE:* https://jmeter.apache.org/
*First seen in:* `00-overview.md`

---

## JTL file

**JTL file** (JMeter Test Log) คือไฟล์ CSV ที่ JMeter บันทึก raw results ของทุก request ระหว่าง test run รวมถึง timestamp, response time, response code, success/fail, bytes, latency ฯลฯ เพราะต้องการ source of truth ที่สมบูรณ์ซึ่ง generate HTML report หรือ analyze ใหม่ได้ทุกเมื่อ — อย่าสับสนกับ **HTML Dashboard Report** ซึ่งเป็นแค่ visualization ที่ generate จาก JTL ไม่ใช่ข้อมูลต้นฉบับ

*SOURCE:* https://jmeter.apache.org/usermanual/generating-dashboard.html
*First seen in:* `08-running-reports.md` — section CLI Command

---

## JMX file

**JMX file** คือ XML file ที่บันทึก JMeter Test Plan ทั้งหมด รวมถึง Thread Groups, Samplers, Listeners, Timers, Assertions, และ Config Elements เพราะ JMeter ต้องการ file format ที่ save/load ได้และ version-controllable ด้วย Git — อย่าสับสนกับ **JTL file** ซึ่งบันทึก results ไม่ใช่ test definition

*SOURCE:* https://jmeter.apache.org/usermanual/get-started.html
*First seen in:* `02-installation-setup.md` (บทที่ 2)

---

## Load Test

**Load Test** คือการทดสอบระบบที่ load ปกติหรือ load สูงสุดที่คาดไว้ เพื่อยืนยันว่าระบบทำงานได้ตาม SLA เช่น response time, throughput, error rate เพราะต้องการ baseline ว่าระบบรองรับ production traffic ได้ก่อน release — อย่าสับสนกับ **Stress Test** (เกิน capacity เพื่อหาจุดแตก) หรือ **Spike Test** (load พุ่งกะทันหัน)

**SOURCE:** https://grafana.com/load-testing/types-of-load-testing/ (secondary source: Grafana k6 docs — "Load testing simulates user activity to determine how well a system can handle increased traffic or load.")
*First seen in:* `01-performance-testing-fundamentals.md` (บทที่ 1)

---

## Listener

**Listener** คือ element ใน JMeter ที่ collect, display, และ store results จาก Samplers เพราะต้องการ visibility ว่า test ทำงานอย่างไรและ results เป็นอย่างไร — อย่าสับสนกับ **Assertion** ซึ่งตัดสินว่า pass/fail (Listener แค่ดูและบันทึก ไม่ตัดสิน) และต้องระวังว่า Listener ที่เปิดมากเกินไประหว่าง load test จะกิน memory ของ JMeter เอง

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#listeners
*First seen in:* `08-running-reports.md` (บทที่ 8)

---

## Loop Count

**Loop Count** คือ field ใน Thread Group ที่กำหนดว่าแต่ละ virtual user จะรัน test scenario กี่รอบ เพราะต้องการควบคุม total number of requests และ test duration โดยไม่ขึ้นกับ time-based settings — อย่าสับสนกับ **Duration** (รัน test นานเท่าไหร่ เป็นวินาที) ซึ่งเป็น alternative กับ Loop Count ใน Thread Group

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#Thread_Group
*First seen in:* `03-ui-test-plan-structure.md` (บทที่ 3)

---

## Median

**Median** คือค่ากลางที่แบ่ง distribution ของ response time ออกเป็นสองส่วนเท่าๆ กัน (50% ของ requests เร็วกว่า, 50% ช้ากว่า) เพราะ Median ไม่ถูก skew โดย outliers เหมือน Average ทำให้ reflect ประสบการณ์ของ "typical user" ได้ดีกว่า — อย่าสับสนกับ **Average** ซึ่ง request ที่ช้ามากๆ ไม่กี่ครั้งสามารถดึงค่าขึ้นได้มาก

*SOURCE:* https://jmeter.apache.org/usermanual/glossary.html — "Median is a number which divides the samples into two equal halves. The Median is the same as the 50th Percentile"
*First seen in:* `08-running-reports.md` — section Statistics Table

---

## Percentile (90th / 95th / 99th)

**Percentile** คือค่าที่บอกว่า N% ของ requests มี response time ต่ำกว่าค่านี้ เช่น 90th percentile = 500ms หมายความว่า 90% ของ requests ตอบภายใน 500ms (และ 10% ช้ากว่า 500ms) เพราะ percentile สะท้อน "worst-case experience" ของ user กลุ่มหนึ่งได้ชัดเจนกว่า Average — อย่าสับสนกับ **Average** ซึ่ง understate tail latency

*SOURCE:* https://jmeter.apache.org/usermanual/glossary.html — "The value below which 90% of the samples fall. The remaining samples take at least as long as this value."
*First seen in:* `08-running-reports.md` — section Statistics Table

---

## Ramp-Up Period

**Ramp-Up Period** คือเวลา (เป็นวินาที) ที่ JMeter ใช้ในการ start threads ทีละน้อยจนครบ total threads เพราะการ start threads ทั้งหมดพร้อมกันไม่ realistic และอาจ overwhelm server ก่อนที่ test จะเริ่มจริงๆ — อย่าสับสนกับ **Duration** ซึ่งคือเวลาทั้งหมดที่ test รัน (รวม ramp-up ด้วย)

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#Thread_Group
*First seen in:* `03-ui-test-plan-structure.md` (บทที่ 3)

---

## Remote Hosts

**Remote Hosts** คือ property ใน `jmeter.properties` ที่ระบุ IP addresses ของ Worker Nodes ที่ Controller จะ connect ไปเพื่อรัน distributed test เพราะ JMeter ต้องการ explicit list ของ Workers แทนที่จะ auto-discover เพื่อป้องกันการรัน test บน machine ที่ไม่ต้องการโดยไม่ตั้งใจ — อย่าสับสนกับ **Server Name** ใน HTTP Request ซึ่งคือ target application ไม่ใช่ JMeter Worker

*SOURCE:* https://jmeter.apache.org/usermanual/jmeter_distributed_testing_step_by_step.html
*First seen in:* `09-distributed-testing.md` — section Setup Step-by-Step

---

## Response Assertion

**Response Assertion** คือ Assertion ใน JMeter ที่ validate fields ต่างๆ ของ response เช่น response code, response body, response headers, URL เทียบกับ pattern ที่กำหนด เพราะต้องการ verify business logic correctness ไม่ใช่แค่ HTTP layer success — อย่าสับสนกับ **Duration Assertion** (ตรวจ timing) หรือ **JSON Assertion** (ตรวจ JSON structure โดยเฉพาะ)

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#Response_Assertion
*First seen in:* `07-assertions.md` (บทที่ 7)

---

## Response Time

**Response Time** คือเวลาทั้งหมดตั้งแต่ JMeter ส่ง request จนกว่าจะรับ response ครบสมบูรณ์ (หน่วยเป็น milliseconds) เพราะมันสะท้อน end-to-end experience ที่ user รับรู้ได้จริง รวมถึง network latency, server processing time, และ response transmission time — อย่าสับสนกับ **Latency** ซึ่งวัดเฉพาะเวลาตั้งแต่ส่ง request จนได้รับ byte แรกของ response (ไม่รวมเวลา download response ทั้งหมด)

*SOURCE:* https://jmeter.apache.org/usermanual/glossary.html
*First seen in:* `08-running-reports.md` (บทที่ 8)

---

## Sampler

**Sampler** คือ element ประเภทหนึ่งใน JMeter ที่ทำการ "request" จริงๆ ไปยัง target system และรอรับ response เพราะ JMeter ต้องการ abstraction layer ที่รองรับ protocol หลายประเภท (HTTP, FTP, JDBC, SMTP ฯลฯ) ในโครงสร้าง Test Plan เดียวกัน — อย่าสับสนกับ **Controller** (ควบคุม flow ของ requests), **Timer** (เพิ่ม delay), หรือ **Listener** (เก็บผล)

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#samplers
*First seen in:* `03-ui-test-plan-structure.md` (บทที่ 3)

---

## Smoke Test

**Smoke Test** คือการทดสอบด้วย load น้อยมาก (1-5 users) เพื่อ verify ว่า test script ทำงานถูกต้องและ basic functionality ของ application ใช้งานได้ก่อนรัน load test จริง เพราะถ้า Smoke Test fail ไม่มีประโยชน์รัน load test ต่อ — ประหยัดเวลาและ resource — อย่าสับสนกับ **Load Test** (test ที่ target load จริง) หรือ **Sanity Test** (concept ใน functional testing)

**SOURCE:** https://grafana.com/load-testing/types-of-load-testing/ (secondary source: Grafana k6 docs — "Smoke tests verify the system functions with minimal load, and they are used to gather baseline performance values.")
*First seen in:* `01-performance-testing-fundamentals.md` (บทที่ 1)

---

## Spike Test

**Spike Test** คือการทดสอบที่เพิ่ม load กะทันหันในช่วงเวลาสั้นๆ แล้วลดกลับเพื่อดูว่าระบบ handle sudden burst ได้อย่างไรและ recover กลับสู่ normal ได้เร็วแค่ไหน เพราะ real-world traffic มักมี spike เช่น flash sale, breaking news, หรือ marketing campaign — อย่าสับสนกับ **Stress Test** (เพิ่ม load ค่อยๆ จนเกิน capacity) หรือ **Load Test** (load คงที่ในระดับ expected)

**SOURCE:** https://grafana.com/load-testing/types-of-load-testing/ (secondary source: Grafana k6 docs — "A spike test verifies whether the system survives and performs under sudden and massive rushes of utilization.")
*First seen in:* `01-performance-testing-fundamentals.md` (บทที่ 1)

---

## Stress Test

**Stress Test** คือการทดสอบที่ผลักดัน load เกินกว่า expected capacity เพื่อหา breaking point ของระบบและดูว่า fail อย่างไร (crash, graceful degradation, หรือ data corruption) เพราะต้องรู้ว่าระบบมี safety margin เท่าไหร่และ behavior เมื่อ overloaded คือ acceptable — อย่าสับสนกับ **Load Test** (ทดสอบที่ expected load ไม่ใช่เกิน) หรือ **Endurance Test** (load ปกติแต่นาน)

**SOURCE:** https://grafana.com/load-testing/types-of-load-testing/ (secondary source: Grafana k6 docs — "Stress tests help you discover how the system functions with the load at peak traffic.")
*First seen in:* `01-performance-testing-fundamentals.md` (บทที่ 1)

---

## Summary Report

**Summary Report** คือ Listener ใน JMeter ที่แสดง statistics แบบ simplified เช่น Average, Min, Max, Error%, Throughput แต่ไม่มี Median และ percentile columns เพราะออกแบบมาเพื่อใช้ memory น้อยกว่า Aggregate Report — เหมาะสำหรับ monitor ระหว่าง load test รัน — อย่าสับสนกับ **Aggregate Report** ซึ่งมีข้อมูลครบกว่าแต่กิน memory มากกว่า

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#Summary_Report — "Simpler (lower memory) version of Aggregate Report. Excludes the Median and 90% columns, which are expensive in memory terms"
*First seen in:* `08-running-reports.md` — section Aggregate Report vs Summary Report

---

## Think Time

**Think Time** คือเวลาที่ real user ใช้ในการ "คิด" หรือ "อ่าน" ระหว่าง actions เช่น หลังหน้าโหลดแล้ว user อ่านเนื้อหา 3 วินาทีก่อนคลิกปุ่มถัดไป เพราะ JMeter ส่ง request ถัดไปทันทีหลัง receive response ซึ่งไม่ realistic — การเพิ่ม Think Time ผ่าน Timer ทำให้ test สะท้อน real-world load pattern — อย่าสับสนกับ **Ramp-Up Period** ซึ่งเป็น delay ในการ start threads ไม่ใช่ delay ระหว่าง requests

**SOURCE:** https://jmeter.apache.org/usermanual/best-practices.html (JMeter best practices — Timer usage guidance)
*First seen in:* `05-thread-group-best-practices.md` (บทที่ 5)

---

## Thread

**Thread** ใน JMeter context คือ virtual user หนึ่งคนที่ run test scenario อิสระจาก thread อื่น มี state ของตัวเอง (cookies, variables, CSV row) เพราะ JMeter ใช้ Java threads เพื่อ simulate concurrent users บนเครื่องเดียว — อย่าสับสนกับ **Process** (JMeter ทั้งหมดรันใน JVM process เดียว) หรือ **User** ในความหมาย real person

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#Thread_Group
*First seen in:* `03-ui-test-plan-structure.md` (บทที่ 3)

---

## Thread Group

**Thread Group** คือ element หลักใน Test Plan ที่กำหนด virtual users (Number of Threads), Ramp-Up Period, และ Loop Count สำหรับ test scenario เพราะ Thread Group เป็น "entry point" ที่บอก JMeter ว่าจะรัน test ด้วย concurrent users เท่าไหร่และนานแค่ไหน — อย่าสับสนกับ **Test Plan** (root element ที่ครอบ Thread Group ทั้งหมด) หรือ **Thread** (virtual user แต่ละคน)

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#Thread_Group
*First seen in:* `03-ui-test-plan-structure.md` (บทที่ 3)

---

## Throughput

**Throughput** คือจำนวน requests ที่ server ประมวลผลได้ต่อหน่วยเวลา (requests/second หรือ requests/minute) เพราะมันบอก capacity ของระบบว่ารับ load ได้มากแค่ไหน — อย่าสับสนกับ **Response Time** ซึ่ง throughput สูงขึ้นไม่ได้แปลว่า response time ดีขึ้น ทั้งสองต้องดูประกอบกัน

*SOURCE:* https://jmeter.apache.org/usermanual/glossary.html — "Throughput is calculated as requests/unit of time. The formula is: Throughput = (number of requests) / (total time)."
*First seen in:* `08-running-reports.md` — section Statistics Table

---

## Timer

**Timer** คือ element ประเภทหนึ่งใน JMeter ที่เพิ่ม delay ก่อน Sampler ถัดไปจะทำงาน เพื่อ simulate Think Time ของ user จริงๆ เพราะถ้าไม่มี Timer JMeter จะส่ง requests เร็วกว่า real user มาก ทำให้ผล test ไม่ representative — อย่าสับสนกับ **Scheduler** (กำหนดว่าจะเริ่มและหยุด Thread Group เมื่อไหร่)

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#timers
*First seen in:* `05-thread-group-best-practices.md` (บทที่ 5)

---

## Uniform Random Timer

**Uniform Random Timer** คือ Timer element ใน JMeter ที่ generate delay สุ่มใน range ที่กำหนด (เช่น 1,000–5,000ms) โดยทุกค่าใน range มีโอกาสเท่ากัน เพราะเหมาะกับ user behavior ที่รู้แค่ range แต่ไม่รู้ว่า distribution shape เป็นอย่างไร — อย่าสับสนกับ **Gaussian Random Timer** (มี peak ที่ mean) หรือ **Constant Timer** (delay คงที่)

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#Uniform_Random_Timer
*First seen in:* `05-thread-group-best-practices.md` (บทที่ 5)

---

## User Defined Variables

**User Defined Variables** คือ Configuration Element ใน JMeter ที่ define ค่า variables คงที่สำหรับใช้ทั่ว Test Plan เช่น base URL, port, หรือ API version เพราะต้องการ single place ที่เปลี่ยนค่าได้โดยไม่ต้อง edit ทุก Sampler — อย่าสับสนกับ **CSV Data Set Config** (อ่านค่าจากไฟล์ CSV เพื่อ parameterize) หรือ **JSR223 variables** (compute ค่าแบบ dynamic)

*SOURCE:* https://jmeter.apache.org/usermanual/component_reference.html#User_Defined_Variables
*First seen in:* `03-ui-test-plan-structure.md` (บทที่ 3)

---

## View Results Tree

**View Results Tree** คือ Listener ใน JMeter ที่แสดง request และ response ของทุก sample แบบ real-time รวมถึง request headers, response headers, response body, และ assertion results เพราะออกแบบมาสำหรับ debug ระหว่าง scripting phase เท่านั้น — อย่าใช้ระหว่าง load test เพราะเก็บ response body ทุกชิ้นใน memory ทำให้ JMeter เองกิน RAM มหาศาล

*SOURCE:* https://jmeter.apache.org/usermanual/best-practices.html — "Don't use 'View Results Tree' or 'View Results in Table' listeners during the load test, use them only during scripting phase to debug your scripts."
*First seen in:* `08-running-reports.md` (บทที่ 8)

---

## Worker Node

**Worker Node** คือเครื่องที่รัน `jmeter-server` ใน distributed test setup ทำหน้าที่รับ Test Plan จาก Controller และ generate HTTP requests ไปยัง target server จริงๆ เพราะการแยก load generation ออกจาก orchestration ทำให้ scale ได้โดยเพิ่มเครื่องใหม่เข้า pool — อย่าสับสนกับ **Controller Node** ซึ่งควบคุมแต่ไม่ generate load และอย่าสับสนกับ **Target Server** ซึ่งเป็น application ที่กำลัง test

*SOURCE:* https://jmeter.apache.org/usermanual/jmeter_distributed_testing_step_by_step.html — "the system running jmeter-server, which takes commands from the GUI and send requests to the target system(s)"
*First seen in:* `09-distributed-testing.md` — section Worker Node
