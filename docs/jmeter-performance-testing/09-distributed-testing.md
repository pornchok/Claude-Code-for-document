# บทที่ 9: Distributed Testing เบื้องต้น

---

## ⏰ Pre-chapter Retrieval

> แนะนำ: อ่านบทนี้หลังจากผ่านไปอย่างน้อย 1 วันนับจากที่อ่านบทที่ 8

**ก่อนอ่านบทนี้ ลองตอบ:**

คุณเพิ่ง run load test ด้วย CLI mode บนเครื่องเดียว และ generate HTML report ได้แล้ว ตอนนี้ลูกค้าบอกว่าต้องการ simulate ผู้ใช้พร้อมกัน 5,000 คน

ลองตอบ 2 ข้อนี้ก่อน scroll ลงไปดูเฉลย:
1. เครื่อง JMeter เดียวที่ CPU 2-3 GHz รับ load ได้ประมาณกี่ threads ก่อนที่ตัว JMeter เองจะกลายเป็น bottleneck?
2. ถ้า single machine รับไม่ไหว — คุณจะแก้ปัญหานี้ด้วยวิธีใด? (เขียนก่อนอ่านต่อ)

หยุดคิดอย่างน้อย 1 นาที แล้วเขียนคำตอบลงกระดาษ และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น

---

> **เฉลย:**
> 1. ประมาณ **1,000–2,000 threads** ต่อ single JMeter client ที่ CPU 2-3 GHz ตามที่ official docs ระบุ — ถ้ามากกว่านี้ JMeter เองจะใช้ CPU สูงจน skew ผลลัพธ์
> 2. คำตอบที่ถูกต้องคือ **Distributed Testing** — รัน JMeter หลาย instance บนหลายเครื่อง แล้วให้เครื่องหลัก (Controller) บังคับบัญชาทุกเครื่องพร้อมกัน นี่คือสิ่งที่บทนี้จะสอน
>
> **Remediation path:** ถ้าตอบข้อ 1 ไม่ได้ — กลับอ่านบทที่ 5 section "Thread Sizing Best Practice" / ถ้าตอบข้อ 2 ได้แล้ว บทนี้จะเติม detail ว่า setup อย่างไรให้ถูกต้อง

---

## 1. วัตถุประสงค์

อ่านจบบทนี้แล้วคุณจะทำได้:

- **อธิบาย** architecture ของ JMeter distributed testing ได้ว่า Controller และ Worker node ทำหน้าที่อะไร และต่างกันอย่างไร
- **ระบุ** ได้ว่าเมื่อไหรควรเลือกใช้ distributed testing แทน single-machine testing (threshold จริง)
- **ออกแบบ** distributed setup เบื้องต้นได้ รวมถึงการ configure `remote_hosts` ใน `jmeter.properties`
- **รัน** distributed test ด้วย CLI mode และ collect ผลลัพธ์
- **ตัดสินใจ** ได้ว่าเมื่อไหรควรเลือก cloud-based load testing แทน on-premise distributed JMeter
- **ป้องกัน** ข้อผิดพลาดที่พบบ่อยใน distributed setup (version mismatch, firewall, CSV sync)

---

## 2. ทำไมต้องรู้? (Why)

เครื่องคอมพิวเตอร์เดียวมีข้อจำกัด — CPU, RAM, network bandwidth มีเพดาน ถ้า test plan ต้องการ 5,000 concurrent users แต่ single JMeter client รับได้แค่ 1,000–2,000 threads คุณกำลังสร้างสถานการณ์ที่ **JMeter เองกลายเป็น bottleneck** ไม่ใช่ระบบที่ทดสอบ ผลลัพธ์ที่ได้จะผิดเพี้ยน

Distributed testing แก้ปัญหานี้โดยกระจาย load ออกไปยังหลายเครื่อง — แต่ละเครื่องรัน JMeter ในฐานะ "Worker" ที่ส่ง requests ไปยัง server จริง ขณะที่ Controller จัดการภาพรวม

นอกจากนี้ยังมีเหตุผลเรื่อง **geography** — ถ้า users จริงมาจากหลาย region การมี Worker nodes ที่กระจายอยู่หลายที่ทำให้ simulate realistic latency ได้ดีกว่า

---

## 3. Analogy: ศูนย์บัญชาการกับทีมสายลับ

ลองนึกภาพ **ผู้อำนวยการฝ่ายปฏิบัติการ (Controller)** ที่นั่งอยู่ในห้อง command center พร้อม **ทีมสายลับ (Workers)** ที่กระจายอยู่ทั่วประเทศ

- **Controller** = ผู้อำนวยการ — ส่ง mission brief (Test Plan) ให้ทีมทุกคน, กำหนดเวลาเริ่ม, รวบรวม report ที่ส่งกลับมา
- **Worker nodes** = สายลับแต่ละคน — รับ mission เดียวกัน แล้วลงมือทำพร้อมกัน ส่ง result กลับ HQ
- **`remote_hosts`** = รายชื่อสายลับที่ผู้อำนวยการรู้จัก — ถ้าชื่อไม่อยู่ในรายการ สั่งงานไม่ได้
- **RMI** = วิทยุสื่อสารระหว่าง HQ กับสายลับ — ถ้าช่องสัญญาณถูกบล็อก (firewall) สั่งงานไม่ถึง
- **Test Plan ที่ส่งให้ทุกคน** = mission brief ฉบับเดียวกันทุกใบ — ทุกคนทำ **ภารกิจเดียวกัน** ไม่ใช่แบ่งงานกัน

> ⚠️ ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:
> - **"Worker แต่ละคนรับส่วนต่างกัน เหมือนแบ่งงาน"** — ผิดทั้งหมด official docs ระบุชัดว่า *"The same test plan is run by all the servers. JMeter does not distribute the load between servers, each runs the full test plan."* — ถ้ามี 3 Workers ที่ตั้ง 100 threads แต่ละตัว load จริงที่ระบบรับคือ 300 concurrent requests ไม่ใช่ 100
> - **"Controller ส่งคำสั่งแบบ real-time ทุกวินาที"** — ผิด Controller ส่ง Test Plan ครั้งเดียวตอนเริ่ม แล้ว Workers รันด้วยตัวเอง ส่ง results กลับมาเรื่อยๆ
> - **"Workers อยู่ที่ไหนก็ได้บน internet"** — มีข้อจำกัด RMI ต้องการ network connectivity ระหว่าง Controller และ Workers โดยตรง และ *"RMI cannot communicate across subnets without a proxy"*

---

## 4. เนื้อหาหลัก

### 4.1 Distributed Testing Architecture

JMeter distributed testing ใช้โครงสร้าง **Controller–Worker** (บางที่เรียกว่า Master–Slave แต่ JMeter docs ใช้คำว่า "client" และ "server nodes")

> *"Controller Node: the system running JMeter GUI, which controls the test"*
> — Apache JMeter Distributed Testing Step-by-Step (jmeter_distributed_testing_step_by_step.html)

> *"Worker Node: the system running jmeter-server, which takes commands from the GUI and send requests to the target system(s)"*
> — Apache JMeter Distributed Testing Step-by-Step (jmeter_distributed_testing_step_by_step.html)

**Controller** ทำหน้าที่:
- ส่ง Test Plan ไปยัง Workers ทุกตัว
- สั่ง start/stop test
- รวบรวม results จาก Workers ทั้งหมดมา merge ใน JTL เดียว

**Workers** ทำหน้าที่:
- รัน `jmeter-server` process (ไม่ใช่ `jmeter` ปกติ)
- รับ Test Plan จาก Controller แล้วรันพร้อมกัน
- ส่ง sample results กลับ Controller แบบ real-time

> *"control multiple, remote JMeter engines from a single JMeter client. By running JMeter remotely, you can replicate a test across many low-end computers and thus simulate a larger load on the server."*
> — Apache JMeter Remote Testing (remote-test.html)

**จุดสำคัญที่ต้องเข้าใจ:** Workers แต่ละตัวรัน Test Plan เต็มๆ ไม่ใช่แบ่งกัน

> *"The same test plan is run by all the servers. JMeter does not distribute the load between servers, each runs the full test plan."*
> — Apache JMeter Remote Testing (remote-test.html)

ดังนั้น ถ้า Test Plan กำหนด 500 threads และมี Workers 4 ตัว — load รวมที่ server รับคือ **2,000 concurrent requests** (500 × 4)

---

### 4.2 เมื่อไหรควรใช้ Distributed Testing

✅ **ควรใช้ distributed testing เมื่อ:**
- ต้องการ thread count ที่เกิน capacity ของ single machine (>= 500–1,000 threads ขึ้นอยู่กับ hardware)
- single JMeter instance เริ่มแสดงอาการ: CPU spike, memory pressure, หรือ JMeter warning ใน console

> *"A single JMeter client running on a 2-3 GHz CPU can handle 1000-2000 threads depending on test type."*
> — Apache JMeter Distributed Testing Step-by-Step (jmeter_distributed_testing_step_by_step.html)

- ต้องการ simulate load จากหลาย geographic location พร้อมกัน
- ต้องการ test network configuration ที่ใกล้เคียง production จริง

⚠️ **ควรเลี่ยง:** ใช้ distributed testing เพื่อแก้ปัญหา test plan ที่ไม่มีประสิทธิภาพ — ถ้า single-machine test ยังไม่ผ่าน tuning ขั้นพื้นฐาน (Thread Group ถูก, Think Time ถูก, Listeners ปิดแล้ว) ให้แก้จุดนั้นก่อน

---

### 4.3 Setup: remote_hosts Property

**ขั้นตอนที่ 1:** เปิด Worker nodes ทุกเครื่อง

บนเครื่อง Worker แต่ละเครื่อง ไปที่ directory `jmeter/bin/` แล้วรัน:

```bash
# บน Linux/macOS
./jmeter-server

# บน Windows
jmeter-server.bat
```

> *"On the worker nodes, go to jmeter/bin directory and execute jmeter-server.bat (jmeter-server on unix)."*
> — Apache JMeter Distributed Testing Step-by-Step (jmeter_distributed_testing_step_by_step.html)

**ขั้นตอนที่ 2:** Configure `remote_hosts` บน Controller

เปิดไฟล์ `jmeter/bin/jmeter.properties` บนเครื่อง Controller แล้วแก้ไข property นี้:

```properties
# jmeter.properties (บนเครื่อง Controller)
# format: IP address หรือ hostname ของ Workers คั่นด้วย comma
remote_hosts=192.168.1.10,192.168.1.11,192.168.1.12
```

> *"remote_hosts=192.168.0.10,192.168.0.11,192.168.0.12,192.168.0.13,192.168.0.14"*
> — Apache JMeter Distributed Testing Step-by-Step (jmeter_distributed_testing_step_by_step.html)

> *"Edit the properties file on the controlling JMeter machine...find the property named 'remote_hosts'"*
> — Apache JMeter Remote Testing (remote-test.html)

---

### 4.4 Best Practice: Version Matching

✅ **บังคับ:** Controller และ Workers ทุกตัวต้องใช้ JMeter version เดียวกัน — และ Java version เดียวกัน

> *"Make sure you use the same version of JMeter and Java on all the systems. Mixing versions will not work correctly."*
> — Apache JMeter Distributed Testing Step-by-Step (jmeter_distributed_testing_step_by_step.html)

> *"Make sure that all the nodes (client and servers) are running exactly the same version of JMeter."*
> — Apache JMeter Remote Testing (remote-test.html)

⚠️ **ควรเลี่ยง:** ใช้ version ต่างกันระหว่าง Controller และ Workers — เพราะ JMeter ส่ง test plan และ results ผ่าน Java object serialization และถ้า version ต่างกัน class definitions อาจไม่ตรงกัน ทำให้ serialize/deserialize ผิดพลาดหรือ crash แบบ silent

วิธีตรวจสอบ version บนทุกเครื่อง:
```bash
jmeter --version
```

---

### 4.5 รัน Distributed Test ด้วย CLI

เมื่อ Workers ทุกตัว start แล้ว และ `remote_hosts` configured แล้ว รัน distributed test บน Controller:

```bash
# รัน test บน Workers ทุกตัวที่ระบุใน remote_hosts
jmeter -n -t test.jmx -r -l results.jtl

# ถ้าต้องการระบุ Workers แบบ explicit (override remote_hosts)
jmeter -n -t test.jmx -R 192.168.1.10,192.168.1.11 -l results.jtl

# รัน test + generate HTML report
jmeter -n -t test.jmx -r -l results.jtl -e -o report/
```

| Flag | ความหมาย |
|------|----------|
| `-n` | Non-GUI (CLI) mode |
| `-t test.jmx` | Test Plan file |
| `-r` | รัน test บน Workers ทุกตัวใน `remote_hosts` |
| `-R ip1,ip2` | รัน test บน Workers ที่ระบุ (override `remote_hosts`) |
| `-l results.jtl` | บันทึก results (รวมจากทุก Workers) |
| `-e -o report/` | Generate HTML report หลังเสร็จ |

Results จากทุก Workers จะถูก merge โดยอัตโนมัติมาใน `results.jtl` เดียวบน Controller

> *"When using distributed mode the result file is combined on the Controller node"*
> — Apache JMeter best practices (best-practices.html)

⏸ **Self-check (Backward Retrieval):** ถ้ามี Workers 3 ตัว และ Test Plan มี 200 threads — server ที่ทดสอบจะรับ concurrent requests กี่ requests ในขณะที่ test รัน peak? เขียนคำตอบลงกระดาษก่อน — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น — แล้วค่อย scroll

---

> **เฉลย:** 600 concurrent requests (200 threads × 3 Workers) — เพราะ JMeter ส่ง Test Plan ฉบับเดียวกันให้ Workers ทุกตัว และทุกตัวรัน full test plan พร้อมกัน — ถ้าตอบผิด กลับอ่าน section 4.1 เรื่อง load distribution อีกครั้ง

---

### 4.6 Best Practice: Network Topology

✅ **Workers ไม่ควรอยู่บน network เดียวกับ SUT (System Under Test)**

เหตุผล: ถ้า Workers อยู่บน network เดียวกับ server ที่ทดสอบ network traffic จาก load test จะแย่ง bandwidth กับ traffic จริง ทำให้ผลลัพธ์ skewed — ตัวเลข response time ที่ได้จะต่ำกว่าความเป็นจริงที่ users ข้างนอกจะเจอ

**แนะนำ:** วาง Workers บน network ที่ simulate traffic path ของ users จริง เช่น external network หรือ DMZ

> *"JMeter/RMI requires a connection from the client to the server...JMeter/RMI also requires a reverse connection in order to return sample results from the server to the client."*
> — Apache JMeter Remote Testing (remote-test.html)

RMI (Java Remote Method Invocation) คือ protocol ที่ JMeter ใช้สื่อสารระหว่าง Controller กับ Workers — ต้องให้ port ผ่าน firewall ทั้งสองทิศทาง (default port: 1099 สำหรับ RMI registry, และ random port สำหรับ callback)

> *"RMI cannot communicate across subnets without a proxy; therefore neither can JMeter without a proxy."*
> — Apache JMeter Distributed Testing Step-by-Step (jmeter_distributed_testing_step_by_step.html)

---

### 4.7 Alternative: Cloud-Based Load Testing

สำหรับ use case บางอย่าง distributed JMeter อาจไม่ใช่ทางเลือกที่ดีที่สุด — ลองพิจารณา **cloud-based load testing** เช่น:

- **BlazeMeter** — รัน JMeter scripts บน cloud infrastructure ได้โดยตรง เหมาะถ้าต้องการ geo-distributed load
- **Grafana k6 Cloud** — ใช้ k6 script format (ไม่ใช่ JMX) แต่ infrastructure และ reporting พร้อมใช้ทันที
- **Azure Load Testing** — integrate กับ Azure ecosystem ได้ดี

**เลือก cloud-based เมื่อ:**
- ไม่มี on-premise infrastructure ที่จะวาง Worker nodes
- ต้องการ Workers ใน geographic regions ที่เฉพาะเจาะจง (เช่น Southeast Asia, Europe พร้อมกัน)
- ต้องการ scale Workers up/down อย่างรวดเร็วโดยไม่ต้องดูแล server
- team ไม่มี bandwidth จัดการ distributed JMeter infrastructure

**เลือก on-premise distributed JMeter เมื่อ:**
- ต้องการ control เต็มที่ และ data ไม่ออกนอก network
- มี infrastructure อยู่แล้วและต้องการประหยัดค่าใช้จ่าย cloud
- ต้องการ custom plugin หรือ configuration ที่ cloud tools ไม่รองรับ

⏸ **Self-check (L4+ Application):** ทีม fintech ต้องการทดสอบ payment API ด้วย 3,000 concurrent users แต่มีนโยบาย data residency ว่าทุก test traffic ต้องอยู่ภายใน Thailand เท่านั้น — คุณจะเลือก distributed JMeter หรือ cloud-based solution และทำไม? เขียนคำตอบลงกระดาษก่อน — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น — แล้วค่อยอ่านต่อ

---

> **แนวทางตอบ:** ควรเลือก **on-premise distributed JMeter** เพราะ data residency requirement ห้าม traffic ออกนอก Thailand — cloud-based tools ส่วนใหญ่รัน Workers บน cloud infrastructure ต่างประเทศ ซึ่งขัดกับ policy | ต้องเตรียม Worker nodes ประมาณ 3-6 เครื่อง (ขึ้นอยู่กับ spec) บน network ใน Thailand | ต้องดูแล version management, firewall rules, และ CSV sync เอง

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner: Configure jmeter.properties สำหรับ 2 Worker Nodes

สถานการณ์: คุณมีเครื่อง 3 เครื่องใน local network — เครื่องที่ 1 เป็น Controller, เครื่องที่ 2-3 เป็น Workers

**ขั้นตอน 1:** บนเครื่อง Worker ทั้งสอง (192.168.1.20 และ 192.168.1.21) ตรวจสอบ JMeter version ก่อน:

```bash
# บน Worker nodes ทั้งสอง
# ตรวจ version ให้ตรงกับ Controller
./jmeter --version
# ควรเห็น: Apache JMeter 5.6.3

# เปิด jmeter-server process
./jmeter-server
# ควรเห็น: Created remote object: UnicastServerRef2
```

**ขั้นตอน 2:** บนเครื่อง Controller — แก้ไข `jmeter.properties`:

```properties
# ไฟล์: apache-jmeter-5.6.3/bin/jmeter.properties
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3

# ระบุ IP addresses ของ Worker nodes ทุกตัว
# format: IP:port ถ้าต้องการ custom port, หรือแค่ IP ถ้าใช้ default port 1099
remote_hosts=192.168.1.20,192.168.1.21
# ทำไม: Controller จะ connect ไปหา Workers เหล่านี้เมื่อสั่ง remote start

# (optional) ตั้ง RMI port ให้ตายตัว เพื่อง่ายต่อ firewall rules
server.rmi.port=4000
# ทำไม: ถ้าไม่ตั้ง JMeter จะ pick random port ซึ่งยากต่อการ configure firewall

# (optional) ปิด SSL สำหรับ RMI — ใช้เฉพาะ internal network ที่ trust ได้
server.rmi.ssl.disable=true
# ทำไม: SSL setup ซับซ้อน — บน internal trusted network สามารถปิดได้
```

**ขั้นตอน 3:** รัน distributed test จาก Controller:

```bash
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3
cd apache-jmeter-5.6.3/bin/

# รัน test บน Workers ทั้งหมดใน remote_hosts
jmeter -n -t /path/to/my_test.jmx -r -l /path/to/results.jtl

# Output ที่ควรเห็น:
# Starting distributed test with remote engines: [192.168.1.20, 192.168.1.21]
# ...
# summary + 500 in 00:00:30 = 16.7/s Avg: 150 ...
```

---

### Intermediate: ออกแบบ Distributed Setup สำหรับ 2,000 Concurrent Users ใน Fintech Application

สถานการณ์: ธนาคารแห่งหนึ่งต้องการทดสอบระบบ internet banking ก่อน launch ครั้งใหญ่ team lead ให้โจทย์ว่า "simulate 2,000 concurrent sessions ที่ทำ fund transfer พร้อมกัน" งบประมาณมี server on-premise 5 เครื่อง

**วิเคราะห์ความต้องการ:**
- Target: 2,000 concurrent threads
- Single machine capacity: ~1,000 threads (conservative สำหรับ fund transfer ที่ response time อาจนาน)
- จำนวน Workers ที่ต้องการ: อย่างน้อย 3 เครื่อง (2,000 / ~700 threads ต่อเครื่อง เพื่อ safety margin)

**การออกแบบ setup:**

```
[Controller]          [Workers]                [SUT]
192.168.1.10    →    192.168.1.20 (700 threads)
                →    192.168.1.21 (700 threads)   →   banking-api.internal:8443
                →    192.168.1.22 (600 threads)
```

**ข้อพิจารณา:**

1. **CSV Data สำหรับ test users:** ถ้า test plan ใช้ CSV file ของ user credentials (ดูบทที่ 6) ต้องมี CSV file ครบบน **ทุก** Worker node โดยใช้ path เดียวกัน — ไม่งั้น Workers จะ error เพราะหา file ไม่เจอ
2. **Thread distribution:** ตั้ง Thread Group ที่ 700 threads (ไม่ใช่ 2,000) เพราะ 3 Workers × 700 = 2,100 concurrent ≈ target ที่ต้องการ
3. **Network placement:** Workers ไม่ควรอยู่ใน data center เดียวกับ banking-api.internal เพื่อหลีกเลี่ยง network interference

**jmeter.properties บน Controller:**

```properties
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3
remote_hosts=192.168.1.20,192.168.1.21,192.168.1.22
server.rmi.ssl.disable=true
```

**CLI command:**

```bash
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3
jmeter -n -t fund_transfer_test.jmx -r \
  -l results/banking_$(date +%Y%m%d_%H%M%S).jtl \
  -e -o results/report/
```

---

### Advanced: Containerized JMeter Workers ด้วย Docker

**Trade-off Analysis: Containers vs Bare Metal**

| ด้าน | Docker Containers | Bare Metal |
|------|-------------------|-----------|
| **Provisioning** | เร็ว — `docker run` ครั้งเดียว | ช้า — ต้องติดตั้ง Java + JMeter ทีละเครื่อง |
| **Version consistency** | ง่าย — ใช้ image เดียวกัน | ต้องตรวจด้วยตัวเอง |
| **Performance overhead** | ~5-10% overhead จาก container layer | ไม่มี overhead |
| **Network** | container networking มี complexity เพิ่ม | straightforward |
| **Scale up/down** | ง่าย — เพิ่ม container ได้ทันที | ต้อง provision server ใหม่ |
| **ใช้ resource เต็มที่** | ขึ้นอยู่กับ container limits | ใช้ resource เต็ม 100% |

**Dockerfile สำหรับ JMeter Worker:**

```dockerfile
# Dockerfile.jmeter-worker
# ยังไม่ได้ทดสอบ — ต้องการ Docker + JMeter 5.6.3
FROM eclipse-temurin:11-jre-jammy

ARG JMETER_VERSION=5.6.3
ENV JMETER_HOME=/opt/apache-jmeter-${JMETER_VERSION}
ENV PATH=${JMETER_HOME}/bin:${PATH}

# ดาวน์โหลด JMeter
RUN apt-get update && apt-get install -y wget && \
    wget -q https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz \
         -O /tmp/jmeter.tgz && \
    tar -xzf /tmp/jmeter.tgz -C /opt && \
    rm /tmp/jmeter.tgz

# เปิด port ที่ JMeter server ใช้
EXPOSE 1099 4000

# รัน jmeter-server เมื่อ container start
CMD ["jmeter-server", \
     "-Dserver.rmi.ssl.disable=true", \
     "-Dserver.rmi.port=4000", \
     "-Dserver_port=1099"]
```

**Docker Compose สำหรับ test environment:**

```yaml
# docker-compose.yml
# ยังไม่ได้ทดสอบ — ต้องการ Docker Compose v2
version: '3.8'

services:
  jmeter-worker-1:
    build:
      context: .
      dockerfile: Dockerfile.jmeter-worker
    networks:
      - jmeter-net
    # เปิด port ให้ Controller connect ได้
    ports:
      - "1099:1099"
      - "4000:4000"

  jmeter-worker-2:
    build:
      context: .
      dockerfile: Dockerfile.jmeter-worker
    networks:
      - jmeter-net
    ports:
      - "1199:1099"
      - "4001:4000"

networks:
  jmeter-net:
    driver: bridge
```

**ข้อควรระวัง:**

Container networking อาจทำให้ JMeter RMI มีปัญหา เพราะ container มี internal IP ที่ต่างจาก host IP — ต้องตั้ง `RMI_HOST` ให้ถูกต้อง:

```bash
# บน Worker container — ระบุ IP ที่ Controller จะ connect กลับมา
# ยังไม่ได้ทดสอบ — ต้องการ Docker + JMeter 5.6.3
jmeter-server -Djava.rmi.server.hostname=<HOST_IP>
```

**สรุป trade-off:** Docker เหมาะสำหรับ CI/CD environments ที่ต้องการ spin up Workers อย่างรวดเร็ว แต่ถ้าต้องการ performance สูงสุดและ load ที่ accurate บน bare metal ยังเป็นตัวเลือกที่ดีกว่าสำหรับ production load tests

---

## 6. Common Mistakes

❌ **ใช้ JMeter version ต่างกันระหว่าง Controller กับ Workers**
✅ ตรวจสอบ version ด้วย `jmeter --version` บนทุกเครื่องก่อนเริ่ม test และใช้ version เดียวกันทั้งหมด
🔍 **วิธีสังเกต:** Test อาจ start ไม่ได้เลย หรือ start แล้ว Workers หลุดออกกลางคัน หรือ results ผิดเพี้ยนแบบอธิบายไม่ได้
🤔 **เหตุผล:** JMeter ใช้ Java serialization ส่ง objects ระหว่าง Controller และ Workers — class definition ที่ต่าง version ต่างกันทำให้ deserialize ผิดพลาด
*(source: https://jmeter.apache.org/usermanual/jmeter_distributed_testing_step_by_step.html — "Make sure you use the same version of JMeter and Java on all the systems. Mixing versions will not work correctly.")*

---

❌ **Firewall บล็อก RMI ports ระหว่าง Controller กับ Workers**
✅ เปิด port 1099 (RMI registry) และ port สำหรับ callback connection บน Workers (ถ้าใช้ fixed port ให้เปิด port นั้นด้วย)
🔍 **วิธีสังเกต:** JMeter แสดง error "Connection refused" หรือ "Cannot connect to remote host" ตอน start remote test
🤔 **เหตุผล:** JMeter/RMI ต้องการ bidirectional connection — Controller connect ไปหา Workers และ Workers ต้อง connect กลับมาหา Controller เพื่อส่ง results
*(source: https://jmeter.apache.org/usermanual/remote-test.html — "JMeter/RMI requires a connection from the client to the server...JMeter/RMI also requires a reverse connection")*

---

❌ **CSV file ไม่ sync ไปยัง Worker nodes**
✅ ก่อนรัน distributed test ให้ copy CSV files (user data, test data ทุกไฟล์) ไปยัง path เดียวกันบน Worker ทุกตัว — หรือใช้ network share / shared volume
🔍 **วิธีสังเกต:** Workers บางตัว error ว่า "File not found" หรือ CSV data ซ้ำกันเพราะ Worker บางตัวใช้ data คนละชุด
🤔 **เหตุผล:** Controller ส่ง Test Plan (JMX) ไปให้ Workers แต่ไม่ได้ส่ง external files เช่น CSV — Workers ต้องหา files เหล่านั้นด้วยตัวเองบน local filesystem
*(source: https://jmeter.apache.org/usermanual/remote-test.html — ข้อจำกัดของ distributed mode ที่ต้องดูแล file sync เอง)*

---

## 7. สรุปบท — Retrieval Questions

ลองตอบคำถามต่อไปนี้ด้วยตัวเอง **ก่อน** ดูเฉลย — เขียนคำตอบลงกระดาษหรือพิมพ์ออกมา และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น:

**คำถามที่ 1 (Elaborative Interrogation):** ทำไม JMeter distributed testing ถึง "replicate" Test Plan บน Workers ทุกตัวแทนที่จะ "split" load ออกไป? ถ้า JMeter ออกแบบให้ split load แทน จะเกิดปัญหาอะไรในทางปฏิบัติ?

**คำถามที่ 2 (Code-based task):** นี่คือ `jmeter.properties` snippet ที่มีปัญหา — ระบุว่ามีข้อผิดพลาดอะไรบ้างและแก้อย่างไร:
```properties
remote_hosts=192.168.1.10, 192.168.1.11, 192.168.1.12
server.rmi.ssl.disable=false
# ใช้ JMeter 5.6.3 บน Controller แต่ Workers ยังใช้ 5.5.0
```

**คำถามที่ 3 (Synthesis):** ทีม DevOps ต้องการ run distributed JMeter test ใน Kubernetes cluster — Controller เป็น pod หนึ่ง, Workers เป็น 5 pods — ระบุ challenges หลัก 3 ข้อที่จะต้องแก้ไข และ solution approach สำหรับแต่ละข้อ

---

<details>
<summary>ดูเฉลย (คลิกหลังจากตอบแล้ว)</summary>

**เฉลยที่ 1:** JMeter replicate แทน split เพราะ (1) ง่ายกว่าในการ implement — Controller ไม่ต้อง track ว่า sampler ไหนอยู่บน Worker ไหน (2) ถ้า split แล้ว Worker หนึ่งตาย load จะหายไปส่วนหนึ่ง ทำให้ผลไม่ถูกต้อง | ถ้า split: ปัญหาที่จะเกิดคือ Test Plan ที่มี dependency ระหว่าง steps (login → browse → checkout) จะแตกออกเป็น partial flows บน Workers ต่างๆ ทำให้ flow ไม่สมบูรณ์

**เฉลยที่ 2:** ปัญหา 3 จุด: (1) space หลัง comma ใน remote_hosts — บางเวอร์ชันอาจ parse ไม่ถูก ควรเป็น `192.168.1.10,192.168.1.11,192.168.1.12` (2) `server.rmi.ssl.disable=false` หมายความว่า SSL enable — ต้องตั้งค่า SSL certificates ด้วย ถ้าไม่ต้องการ SSL ให้เปลี่ยนเป็น `true` (3) JMeter version ต่างกัน (5.6.3 vs 5.5.0) จะ fail — ต้อง update Workers ทุกตัวให้เป็น 5.6.3 ก่อน

**เฉลยที่ 3:** Challenges หลัก: (1) **RMI hostname resolution** — pods มี dynamic IP ที่เปลี่ยนได้ ต้องใช้ Kubernetes Service + static DNS names แทน IP | solution: สร้าง headless Service สำหรับ Worker pods (2) **Bidirectional connectivity** — Worker pods ต้อง connect กลับมาหา Controller pod เพื่อส่ง results | solution: Controller pod ต้องมี stable hostname/IP ผ่าน Service (3) **File sync** — CSV files ต้องอยู่บน Worker pods ทุกตัว | solution: ใช้ Kubernetes ConfigMap สำหรับ small files หรือ PersistentVolume shared สำหรับ large CSV

</details>

> **Generation Effect Reminder:** ถ้าเพิ่งอ่านจบ ลองปิดเอกสารและเขียน 3 สิ่งที่จำได้จากบทนี้ด้วยคำพูดตัวเอง — แล้วค่อยกลับมาเปรียบเทียบกับเนื้อหา การเขียนออกมาเองทำให้จำได้นานกว่าการอ่านซ้ำ
>
> **Remediation path:** ถ้าตอบ คำถามที่ 1 ไม่ได้ → กลับอ่าน section 4.1 | ถ้าตอบคำถามที่ 2 ไม่ได้ → กลับอ่าน section 4.3 และ 4.4 | ถ้าตอบคำถามที่ 3 ไม่ได้ → บทนี้ให้ foundation แล้ว แนะนำให้ลอง setup จริงบนเครื่อง 2 เครื่องก่อน แล้วค่อย scale ไปยัง container environment
