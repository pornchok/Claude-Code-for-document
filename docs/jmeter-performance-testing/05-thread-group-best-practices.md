# บทที่ 5: Thread Group Best Practices & Think Time

---

## ⏰ Pre-chapter Retrieval

> แนะนำ: อ่านบทนี้หลังจากผ่านไปอย่างน้อย 1 วันหลังบทที่ 4

**ก่อนอ่านบทนี้ ลองตอบ:**

ใน Test Plan ที่สร้างใน HTTP Request Sampler บทก่อน — ถ้า Thread Group ของคุณมี 50 threads, Ramp-Up 0 วินาที, Loop Count Forever และคุณไม่ได้ใส่ Timer ไว้เลย มีโอกาสเกิดปัญหาอะไรขึ้นกับผลการทดสอบ? เหตุผลคืออะไร?

เขียนคำตอบลงกระดาษก่อน ไม่ต้องถูกทุกข้อ แค่พยายามคิดก่อน 30 วินาที และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น

---

> **เฉลย:** เกิดปัญหา 2 อย่างหลัก:
>
> 1. **Ramp-Up 0 วินาที** = JMeter ปล่อย 50 threads พร้อมกันทันที ทำให้ server รับ spike ที่ไม่สะท้อน real user behavior — เหมือนคน 50 คนกดปุ่มลิฟต์พร้อมกันทุกคนใน 0 วินาที ไม่ใช่ทยอยเข้ามาตามปกติ
>
> 2. **ไม่มี Timer** = JMeter ยิง request ถัดไปทันทีหลัง response กลับมา — ไม่มีช่วงเวลาที่ user "อ่านหน้าจอ" ทำให้ load สูงกว่า production จริงมาก ผลที่ได้อาจบอกว่าระบบช้าทั้งที่จริงๆ แล้ว OK สำหรับ real users
>
> **Remediation path:** ถ้าตอบได้แค่ข้อเดียว — อ่าน section 4.2 (Think Time) และ section 4.1 (Ramp-Up) ในบทนี้อย่างละเอียด
>
> **Novice fallback:** ลองนึกภาพร้านกาแฟที่พนักงาน 50 คนมาทำงานพร้อมกัน 8 โมงเช้าพอดี และทุกคนยิงคำสั่งเข้า POS ทุก 0.1 วินาทีโดยไม่หยุด — นั่นไม่ใช่ traffic จริง แต่เป็น DoS attack

---

## 1. วัตถุประสงค์

เมื่ออ่านบทนี้จบ คุณจะสามารถ:

- **เลือก** Thread Group type ที่เหมาะกับแต่ละ test scenario และอธิบายได้ว่าทำไม
- **คำนวณ** Ramp-Up Period ที่เหมาะสมโดยใช้ rule of thumb "1 second per thread" และอธิบาย reasoning ได้
- **เปรียบเทียบ** Constant Timer, Uniform Random Timer, และ Gaussian Random Timer — และเลือกใช้ถูกประเภท
- **ออกแบบ** Thread Group config สำหรับ smoke test, load test, และ mixed workload scenario ได้
- **ระบุ** 3 common mistakes ที่ทำให้ผล test บิดเบือน (Ramp-Up ต่ำเกิน, Loop Count แทน Duration, ไม่มี Think Time)

---

## 2. ทำไมต้องรู้?

สมมติคุณทำ load test แล้วได้ผลดีมาก — response time 80ms, error rate 0% ทีม approve แล้ว deploy ขึ้น production แต่วันแรกที่ users จริงเข้ามา server ร้อนและ error rate พุ่งขึ้นทันที

สิ่งที่ซ่อนอยู่ใน test plan ของคุณ:
- Ramp-Up = 0 วินาที ทำให้ cache server โดน spike หนักในช่วงแรก — แต่ test ผ่านเพราะ test มันสั้นเกิน
- ไม่มี Think Time ทำให้ throughput ใน test สูงกว่า real users 10x — server ที่ดูเหมือนรับได้จริงๆ แล้วรับ load น้อยกว่าที่ test simulate
- ใช้ Loop Count = 100 แทน Duration ทำให้ soak test จบเร็วกว่าที่ตั้งใจ ไม่เจอ memory leak

Thread Group config ที่ผิดไม่ได้ทำให้ test พัง — มันทำให้ test ผ่านทั้งที่ระบบมีปัญหา นั่นอันตรายกว่ามาก

---

## 3. Analogy: คลื่นนักท่องเที่ยวในสนามบิน

ลองนึกภาพสนามบินระหว่างประเทศ ในวันหยุดยาว

**Thread count** = จำนวนผู้โดยสารที่อยู่ในขั้นตอน check-in พร้อมกัน — ถ้าเคาน์เตอร์รับได้ 200 คน แต่คุณส่ง 500 คนพร้อมกัน คิวจะยาวและช้า

**Ramp-Up Period** = เที่ยวบินไม่ได้ลงพร้อมกันทุกลำ ผู้โดยสารทยอยเข้ามา check-in ตลอดวัน — นั่นคือ Ramp-Up ที่ natural สนามบินออกแบบรองรับ "peak hourly throughput" ไม่ใช่ "ทุกคนมาพร้อมกันในนาทีเดียว"

**Think Time** = ผู้โดยสารแต่ละคนใช้เวลาหยิบ passport, เช็ค booking reference, วางกระเป๋า — ไม่ได้ทำธุรกรรมเสร็จใน 0.001 วินาที

**Scheduler / Duration** = สนามบินทดสอบระบบ check-in ตลอด 8 ชั่วโมงของกะเช้า ไม่ใช่แค่ 5 นาทีแล้วสรุปว่า "ผ่าน"

⚠️ **ถ้าเชื่อ analogy นี้ 100% จะเข้าใจผิดว่า:** Thread count = จำนวน users ทั้งหมดที่ระบบรองรับ — ผิด Thread count = จำนวน concurrent users ณ ช่วงเวลาหนึ่ง ไม่ใช่ users ทั้งหมดที่เคยเข้าระบบ ถ้าระบบมี 10,000 registered users แต่ peak concurrent คือ 500 คน test ควรใช้ 500 threads — ไม่ใช่ 10,000 threads (ซึ่งจะทำให้ JMeter ช้าและผล test distorted)

---

## 4. เนื้อหาหลัก

### 4.1 Thread Group Types ที่ใช้บ่อย

JMeter มี Thread Group หลายแบบ ที่ใช้บ่อยและ built-in ตั้งแต่ติดตั้ง:

**Standard Thread Group** (แนะนำสำหรับกรณีส่วนใหญ่)
- ควบคุม thread ด้วย 3 field หลัก: Number of Threads, Ramp-Up Period, Loop Count/Duration
- เหมาะกับ: smoke test, load test, soak test, stress test ทั่วไป
- เรียบง่าย predictable และ document ดี

**Concurrency Thread Group** (จาก JMeter Plugins)
- ควบคุม concurrent users ตลอดเวลา — ถ้า thread ตายจะ spawn ใหม่อัตโนมัติ
- เหมาะกับ: สถานการณ์ที่ต้องการ "รักษา concurrent users คงที่" ตลอด test
- ต้องติดตั้ง JMeter Plugins Manager ก่อน

**Stepping Thread Group** (จาก JMeter Plugins)
- เพิ่ม thread ทีละ step ตามที่กำหนด เช่น "เพิ่ม 10 users ทุก 30 วินาที"
- เหมาะกับ: breakpoint test ที่อยากเห็นว่าระบบเริ่มพังที่ concurrent users เท่าไหร่
- ก็ต้องติดตั้ง Plugins เช่นกัน

**คำแนะนำ:** สำหรับ series นี้และ use case ส่วนใหญ่ — ใช้ **Standard Thread Group** ก่อน มันรองรับ pattern การทดสอบหลักได้ครบ ถึงค่อยมองหา alternatives เมื่อมี requirement เฉพาะ

---

### 4.2 Ramp-Up Period: Rule of Thumb และเหตุผล

Ramp-Up Period คือเวลา (วินาที) ที่ JMeter ใช้เพื่อ start thread ทั้งหมดให้ครบ

ตัวอย่างจาก official docs:

> "If 10 threads are used, and the ramp-up period is 100 seconds, then JMeter will take 100 seconds to get all 10 threads up and running."
> *(source: https://jmeter.apache.org/usermanual/test_plan.html)*

**Rule of Thumb ที่ใช้กัน: 1 second per thread**

ถ้ามี 100 threads → ตั้ง Ramp-Up = 100 วินาที
ถ้ามี 50 threads → ตั้ง Ramp-Up = 50 วินาที

**ทำไม?** เหตุผลมี 2 ข้อ:

1. **หลีกเลี่ยง spike** — ถ้า Ramp-Up สั้นเกิน เช่น 100 threads ใน 1 วินาที server จะรับ request พร้อมกันเกือบทันที เหมือน DDoS attack ไม่ใช่ real user behavior
2. **Server warm-up** — real systems มี caching, connection pooling, JIT compilation — Ramp-Up ที่เพียงพอทำให้ระบบ warm up ตามธรรมชาติ เหมือนที่เกิดจริงใน production

**ข้อยกเว้น:** ถ้าทดสอบ spike test โดยเจตนา (เพื่อดูว่าระบบรับ spike ได้ไหม) ก็ตั้ง Ramp-Up ต่ำได้ — แต่ต้องรู้ว่าเจตนาคืออะไร

---

### 4.3 Think Time: ทำไมถึงสำคัญ

Think Time คือ delay ที่เพิ่มเข้าไประหว่าง requests เพื่อ simulate เวลาที่ user "คิด" หรือ "อ่านหน้าจอ"

> "A timer will cause JMeter to delay a certain amount of time before each sampler which is in its scope."
> *(source: https://jmeter.apache.org/usermanual/test_plan.html)*

⚠️ **ควรเลี่ยง: ไม่ใส่ Timer เลย** — เพราะ JMeter จะยิง request เร็วกว่าที่ human user ทำจริง ทำให้ load สูงเกินจริง และอาจทำให้ผล test misleading — ระบบที่ดูช้าใน test อาจจะ OK สำหรับ real users

**Timer 3 ประเภทหลัก:**

#### Constant Timer

```
Field: Thread delay (ms)
```

*(source: https://jmeter.apache.org/usermanual/component_reference.html)*

- ใส่ delay เท่ากันทุกครั้ง เช่น 1000ms ทุก request
- **ใช้เมื่อ:** ต้องการควบคุม throughput แม่นยำ เช่น "ส่งไม่เกิน 1 request ต่อวินาทีต่อ user"
- **ข้อด้อย:** real users ไม่ได้มี delay เท่ากันพอดี — ทำให้ load pattern ไม่ realistic

#### Uniform Random Timer

```
Fields: Random delay maximum (ms), Constant delay offset (ms)
```

*(source: https://jmeter.apache.org/usermanual/component_reference.html)*

- delay จะสุ่มระหว่าง `offset` ถึง `offset + random_max`
- เช่น offset=500, random=1000 → delay จะอยู่ระหว่าง 500ms–1500ms
- **ใช้เมื่อ:** ต้องการ simulate real user behavior ทั่วไป — นี่คือ **ตัวเลือกแนะนำสำหรับ load test ส่วนใหญ่**
- **เหตุผล:** user จริงมี think time แบบสุ่มในช่วงที่สมเหตุสมผล ไม่ใช่ตายตัว

#### Gaussian Random Timer

```
Fields: Constant delay offset (ms), Deviation (standard deviation in ms)
```

*(source: https://jmeter.apache.org/usermanual/component_reference.html)*

- delay สุ่มตาม normal distribution — ค่าส่วนใหญ่อยู่ใกล้ mean, บางครั้ง spike สูงหรือต่ำ
- **ใช้เมื่อ:** ต้องการ model พฤติกรรม user ที่มีความแปรปรวนตาม bell curve — เช่น user กลุ่มหนึ่งที่มี think time เฉลี่ย 3 วินาทีแต่บางคนเร็ว บางคนช้า
- **ข้อด้อย:** parameter ยากเข้าใจกว่า และต้องรู้ standard deviation ของ real behavior

**สรุปการเลือก:**
| สถานการณ์ | Timer ที่แนะนำ |
|-----------|---------------|
| Load test ทั่วไป | Uniform Random Timer |
| ต้องการ throughput ตายตัว | Constant Timer |
| มีข้อมูล real user behavior | Gaussian Random Timer |
| ไม่แน่ใจ | Uniform Random Timer |

---

⏸ **self-check (Backward Retrieval):** ก่อนอ่านต่อ — อธิบาย Ramp-Up Period ด้วยคำพูดตัวเองสั้นๆ ว่าคืออะไรและทำไมต้องตั้งให้นานพอ เขียนคำตอบลงกระดาษก่อน — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น — ถ้าอธิบายไม่ออก กลับไปอ่าน section 4.2

---

> **เฉลย:** Ramp-Up Period คือช่วงเวลา (วินาที) ที่ JMeter ใช้สร้าง threads ทั้งหมดขึ้นมา — เช่น 100 threads, Ramp-Up = 100 วินาที หมายความว่า JMeter สร้าง thread ใหม่ทุก 1 วินาที เพื่อป้องกัน spike load ที่ผิดปกติเมื่อเปรียบเทียบกับ real user behavior
>
> **ถ้าตอบไม่ได้:** กลับอ่าน Section 4.1 (Ramp-Up Period) อีกครั้ง — ถ้าระบุจุดที่เข้าใจผิดไม่ได้: อ่าน section ทั้งหมดของบทนั้นใหม่ โดยเน้นที่ตัวอย่าง Beginner ก่อน

---

### 4.4 Scheduler Options: Duration vs End Time

Thread Group มี Scheduler ที่ช่วยกำหนดว่า test รันนานแค่ไหน

**วิธีใช้ Scheduler:**
ใน Standard Thread Group ติ๊ก checkbox "Scheduler" แล้วกำหนด:

**Option 1: Duration (วินาที)**
- ระบุเวลาที่ต้องการให้ test รัน เช่น 600 = 10 นาที
- JMeter จะหยุด thread ทั้งหมดหลังจากครบเวลา
- **แนะนำสำหรับ:** load test และ soak test ที่รู้ duration ล่วงหน้า

**Option 2: End Time**
- ระบุวันเวลาที่ต้องการหยุด เช่น "2026-03-20 23:00:00"
- **ใช้เมื่อ:** ต้องการหยุด test ที่เวลาจริงที่แน่นอน เช่น "หยุดก่อนตี 1"

**สำคัญ:** เมื่อใช้ Scheduler ต้องตั้ง Loop Count = "Infinite" (Forever) ไม่เช่นนั้น test จะหยุดตาม Loop Count ก่อนถึง Duration — นี่คือ common mistake ที่จะอธิบายใน section 6

---

⏸ **self-check (Bloom's L4 — Analysis):** ถ้าคุณต้องการทดสอบ soak test 8 ชั่วโมงเพื่อหา memory leak — คุณควรตั้ง Loop Count เป็นอะไร และทำไม? ถ้าตั้ง Loop Count = 100 จะเกิดอะไรขึ้น? เขียนคำตอบลงกระดาษก่อน — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น — แล้วค่อยอ่านต่อ

---

> **เฉลย:** สำหรับ soak test 8 ชั่วโมง ควรตั้ง:
> - **Duration** ใน Scheduler = 28800 วินาที (8×60×60) หรือตั้ง Duration เป็น HH:MM:SS = 08:00:00
> - **Loop Count = Infinite** (ไม่ตั้งเป็นจำนวน loop เพราะไม่รู้ว่า 1 loop ใช้เวลากี่วินาที)
> - **เหตุผล:** Duration-based stopping ให้ control ที่แม่นยำกว่า Loop Count เพราะ response time อาจเปลี่ยนแปลงระหว่าง test ทำให้ loop-based stopping ไม่แน่นอน
>
> **ถ้าตอบไม่ถูก:** กลับอ่าน Section 4.4 (Scheduler Options) อีกครั้ง — ถ้าระบุจุดที่เข้าใจผิดไม่ได้: อ่าน section 4 ทั้งหมดใหม่ โดยเน้น Loop Count vs Duration comparison

---

### 4.5 Best Practice Summary

| สิ่งที่ต้องทำ | เหตุผล |
|--------------|--------|
| Ramp-Up = 1 วินาที × จำนวน threads | หลีกเลี่ยง spike, ให้เวลา server warm up |
| ใช้ Uniform Random Timer สำหรับ load test ทั่วไป | เลียนแบบ real user behavior |
| ใช้ Duration + Loop Count = Infinite สำหรับ soak test | test รันตามเวลา ไม่ใช่ตาม iteration count |
| อย่าตั้ง Ramp-Up = 0 นอกจากตั้งใจทำ spike test | Ramp-Up 0 ทำให้ผล test ไม่ realistic |
| ใส่ Timer ทุกครั้งใน load test | ป้องกัน JMeter ยิงเร็วกว่า real users |

---

## 5. ตัวอย่าง 3 ระดับ

### Beginner: Thread Group Config สำหรับ Smoke Test

**Scenario:** ทดสอบว่า API `/api/products` ทำงานปกติหลัง deploy ใหม่ ด้วย minimal load

**Thread Group Settings:**
```
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3

Thread Group Config:
  Name: Smoke Test - Products API

  Number of Threads (users): 5
  # ทำไม 5? smoke test ใช้ users น้อยที่สุด — เป้าหมายคือ verify ว่าระบบทำงาน ไม่ใช่ test load

  Ramp-Up Period (seconds): 5
  # ทำไม 5? rule of thumb: 1 second × 5 threads = 5 วินาที
  # ถึงแม้ users น้อย ก็ดีกว่าใส่ 0 เพื่อ establish pattern ที่ถูกต้อง

  Loop Count: 1
  # ทำไม 1? smoke test ต้องการ run ครั้งเดียวเพื่อ verify — ไม่ต้องการ sustained load
  # ไม่ใช้ Duration เพราะแค่ต้องการ 1 pass ผ่าน test script

  Scheduler: ไม่ติ๊ก (ปล่อยให้ Loop Count ควบคุม)
  # ทำไม? Loop Count = 1 ควบคุมจบได้แล้ว ไม่ต้องซ้อน Scheduler

Add Timer (ใต้ Thread Group):
  Type: Constant Timer
  Thread delay (ms): 500
  # ทำไม Constant? smoke test ต้องการ predictable — รู้แน่ว่าแต่ละ request ห่างกัน 0.5 วินาที
  # ทำไม 500ms? น้อยพอให้ test เสร็จเร็ว แต่มี delay ให้เป็น realistic
```

**Expected test duration:** ~5-10 วินาที (5 threads × 1 loop + Ramp-Up)
**ตีความผล:** ถ้า error rate = 0% และ response time < threshold → ผ่าน deploy verification

---

### Intermediate: Load Test สำหรับ Logistics API

**Scenario:** ทีม logistics ต้องการทดสอบ `POST /api/shipments/track` ที่จะรองรับ 200 concurrent users ในชั่วโมงเร่งด่วน

**Thread Group Settings:**
```
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3

Thread Group Config:
  Name: Load Test - Shipment Tracking API
  Number of Threads (users): 200
  Ramp-Up Period (seconds): 200
  # 1 second × 200 threads — เพิ่มทีละ 1 user ต่อวินาที
  # ป้องกัน spike ที่ทำให้ server cache ไม่ทัน warm up

  Loop Count: [ติ๊ก Infinite / Forever]
  # ใช้ Scheduler ควบคุม duration แทน

  Scheduler: ติ๊ก ✓
    Duration (seconds): 600
    # 10 นาที — เพียงพอสำหรับ load test ที่ต้องการดู sustained performance
    Startup delay: 0

Timer (เพิ่ม Uniform Random Timer ใต้ Thread Group):
  Random delay maximum (ms): 2000
  Constant delay offset (ms): 1000
  # delay จะสุ่มระหว่าง 1000ms–3000ms ต่อ request
  # เลียนแบบ user ที่ใช้เวลาดูผลลัพธ์การ track 1-3 วินาทีก่อนกด refresh หรือ action ถัดไป
```

**ทำไม Uniform Random แทน Constant?**
User จริงในระบบ logistics มีพฤติกรรมหลากหลาย — บางคนสแกน barcode เร็ว บางคนพิมพ์ tracking number ช้า Uniform Random สะท้อนความหลากหลายนั้นได้ดีกว่า

**Expected outcome:** concurrent users ค่อยๆ เพิ่มขึ้นถึง 200 ภายใน ~3 นาที แล้ว sustained อีก 7 นาที

---

### Advanced: Multiple Thread Group Strategy สำหรับ Mixed Workload

**Scenario:** ระบบ e-commerce มี traffic จริงที่เป็น read-heavy: 80% ของ requests คือ `GET /api/products` (browse), 20% คือ `POST /api/orders` (checkout)

**ทำไม Mixed Workload ถึงสำคัญ?**
ถ้าใส่ทุก endpoint ใน Thread Group เดียว สัดส่วนของ request จะถูกกำหนดโดย order ใน script ไม่ใช่ business reality เราต้องการ simulate สัดส่วนให้ถูกต้อง

**แนวทาง 1: Multiple Thread Groups (แนะนำ)**

```
# ยังไม่ได้ทดสอบ — ต้องการ JMeter 5.6.3

Test Plan
├── Thread Group: Browse (Read - 80%)
│   ├── Number of Threads: 160
│   ├── Ramp-Up: 160 seconds
│   ├── Loop Count: Infinite + Scheduler Duration: 600
│   ├── Uniform Random Timer: offset=1000, random=2000
│   └── HTTP Request: GET /api/products
│
└── Thread Group: Checkout (Write - 20%)
    ├── Number of Threads: 40
    ├── Ramp-Up: 40 seconds
    ├── Loop Count: Infinite + Scheduler Duration: 600
    ├── Uniform Random Timer: offset=3000, random=5000
    │   # checkout ใช้เวลานานกว่า browse — user อ่านรายการสินค้า ใส่ address ฯลฯ
    └── HTTP Request: POST /api/orders
```

**Trade-off Discussion:**

| แนวทาง | ข้อดี | ข้อเสีย |
|--------|-------|---------|
| Multiple Thread Groups | สัดส่วน request ตรงกับ production, ปรับแยกได้ง่าย | config ซับซ้อนขึ้น |
| Single Thread Group + Throughput Controller | ควบคุม % ได้แม่นยำกว่า | เรียนรู้ Throughput Controller เพิ่ม |
| Random Controller | simple แต่สัดส่วนไม่ stable | ไม่แนะนำสำหรับ production test |

**เมื่อไหรเปลี่ยนไปใช้ Throughput Controller?**
ถ้า ratio ต้องแม่นยำมากกว่า ±5% เช่น 80.0% พอดี — ใช้ Throughput Controller ซ้อนใน Single Thread Group แทน

---

## 6. Common Mistakes

### ❌ (a) Ramp-Up Period ต่ำเกินไป ทำให้ Spike

**แบบผิด:**
```
Number of Threads: 200
Ramp-Up Period: 5 seconds   ← ปล่อย 40 threads/วินาที!
```

🔍 **สัญญาณ:** Response time พุ่งสูงมากใน 5-10 วินาทีแรกของ test แล้วลดลง — ผล test ดูเหมือนมี 2 phase
🤔 **ถามตัวเอง:** "Ramp-Up ของฉันทำให้ server รับ request สม่ำเสมอหรือเปล่า หรือ spike ในตอนเริ่ม?"

**แบบถูก:**
```
Number of Threads: 200
Ramp-Up Period: 200 seconds  ← 1 second per thread
```

*(source: https://jmeter.apache.org/usermanual/best-practices.html — Coordinated Omission warning: "if you don't correctly size the number of threads, you will face the 'Coordinated Omission' problem")*

---

### ❌ (b) ใช้ Loop Count แทน Duration ใน Soak Test

**แบบผิด:**
```
Loop Count: 1000
Scheduler: ไม่ติ๊ก
# ตั้งใจทำ soak test 8 ชั่วโมง แต่ถ้า response time เร็ว อาจเสร็จใน 30 นาที
```

🔍 **สัญญาณ:** Test จบเร็วกว่าที่คาด — เช่น ตั้งใจรัน 8 ชั่วโมงแต่จบใน 45 นาที
🤔 **ถามตัวเอง:** "ฉันต้องการให้ test รันตาม 'เวลา' หรือตาม 'จำนวนรอบ'?"

**แบบถูก:**
```
Loop Count: Infinite (Forever)
Scheduler: ติ๊ก ✓
Duration: 28800   # 8 ชั่วโมง = 8 × 60 × 60 วินาที
```

*(source: https://jmeter.apache.org/usermanual/build-web-test-plan.html — Loop Count: "This property tells JMeter how many times to repeat your test.")*

---

### ❌ (c) ไม่ใส่ Think Time เลย

**แบบผิด:**
```
Thread Group: 100 threads
ไม่มี Timer ใดๆ ใน test plan
# JMeter จะยิง request ถัดไปทันทีหลัง response กลับมา
```

🔍 **สัญญาณ:** Throughput ใน test สูงกว่า production metrics จริงหลาย 10x เช่น test ได้ 5,000 req/s แต่ production จริงมีแค่ 500 req/s
🤔 **ถามตัวเอง:** "User จริงมี delay ระหว่าง actions ไหม? ฉัน simulate delay นั้นหรือเปล่า?"

**แบบถูก:**
```
เพิ่ม Uniform Random Timer:
  Random delay maximum: 2000
  Constant delay offset: 1000
  # delay 1-3 วินาทีระหว่าง requests — เหมาะสมสำหรับ web application ส่วนใหญ่
```

*(source: https://jmeter.apache.org/usermanual/test_plan.html — Timer behavior: "A timer will cause JMeter to delay a certain amount of time before each sampler which is in its scope.")*

---

### ❌ Mistake 4: JVM Heap memory ไม่พอสำหรับ thread count สูง

**แบบผิด:**
รัน test ด้วย 500 threads แต่ JMeter crash ด้วย `java.lang.OutOfMemoryError` หรือ performance ช้าผิดปกติ

**แบบถูก:** ตั้ง heap ใน `jmeter` script (หรือ `setenv.sh`/`bin/jmeter`) ก่อนรัน:

```bash
# เปิดไฟล์ bin/jmeter (macOS/Linux) หรือ bin/jmeter.bat (Windows)
# หา: HEAP="-Xms1g -Xmx1g"
# แก้เป็น: HEAP="-Xms2g -Xmx4g" สำหรับ test ขนาดใหญ่
```

🔍 **สัญญาณที่จะรู้ว่าทำ mistake นี้:** JMeter ออก error `java.lang.OutOfMemoryError: Java heap space` หรือ GC overhead warning ใน log, หรือ JMeter ทำงานช้าผิดปกติแม้ thread count ไม่สูงมาก

🤔 **ก่อนดูเหตุผล:** ลองอธิบายว่าทำไม default heap 1 GB ถึงไม่พอสำหรับ 500 threads — memory แต่ละ thread ใช้เพื่ออะไร?

*(source: https://jmeter.apache.org/usermanual/get-started.html — "By default JMeter runs with a heap of 1 GB, this might not be enough for your test")*

---

## 7. สรุปบท

### คำถาม Retrieval — ตอบก่อนดูเฉลย

หยุดอ่าน เขียนคำตอบลงกระดาษก่อน อย่างน้อย 30 วินาทีต่อข้อ — และเขียนเหตุผลสั้นๆ 1–2 ประโยคว่าทำไมถึงตอบแบบนั้น ก่อนดูเฉลย

**คำถาม 1:** Thread Group มี 80 threads คุณควรตั้ง Ramp-Up Period เป็นเท่าไหร่ตาม rule of thumb? และถ้า PM บอกว่า "ต้องการ test peak traffic ที่เกิดขึ้น 30 วินาที" คุณจะปรับอย่างไร?

**คำถาม 2:** Uniform Random Timer กับ Constant Timer ต่างกันอย่างไรในแง่ของ load pattern ที่ simulate ได้? ยกสถานการณ์ที่ควรใช้แต่ละตัว

**คำถาม 3 (code-based):** ดู config ด้านล่างนี้ — บอกได้ไหมว่ามีปัญหาอะไรและจะแก้ยังไง:
```
Thread Group: Soak Test
  Threads: 300
  Ramp-Up: 10 seconds
  Loop Count: 500
  Scheduler: ไม่ติ๊ก
  (ไม่มี Timer)
```

---

**เฉลย คำถาม 1:**
Ramp-Up ที่แนะนำ = 80 วินาที (1 second × 80 threads)

ถ้า PM ต้องการ peak 30 วินาที: ใช้ Duration Scheduler ตั้งเป็น 30 วินาที แต่ Ramp-Up ยังควร ≥ threads count หรืออย่างน้อย 30-60 วินาที — ถ้า Ramp-Up > Duration แสดงว่า test scenario นี้ไม่สมเหตุสมผล ต้องคุยกับ PM ใหม่

**เฉลย คำถาม 2:**
Constant Timer: delay เท่ากันทุก request เหมาะเมื่อต้องการ throughput แม่นยำ เช่น ทดสอบ API ที่มี rate limit 1 req/s
Uniform Random Timer: delay สุ่มในช่วงที่กำหนด — เหมาะกับ web application ทั่วไปที่ users มีพฤติกรรมหลากหลาย

**เฉลย คำถาม 3 (config ที่มีปัญหา):**
ปัญหา 3 จุด:
1. Ramp-Up 10 วินาทีสำหรับ 300 threads = 30 threads/วินาที → spike หนักมาก → แก้เป็น 300 วินาที
2. Loop Count 500 สำหรับ soak test → test จะจบตาม iteration ไม่ใช่เวลา → แก้เป็น Infinite + Scheduler Duration
3. ไม่มี Timer → JMeter ยิง request เร็วเกินจริง → เพิ่ม Uniform Random Timer

---

**Generation Effect — ลองสร้างเอง:**

ก่อนปิดบทนี้ เขียน Thread Group config สำหรับ **load test ของระบบใดก็ได้ที่คุณคุ้นเคย** (ไม่ต้องเป็นของจริง แค่นึกขึ้นมา) พร้อมระบุว่าทำไมแต่ละ field ถึงตั้งค่านั้น — การสร้างตัวอย่างด้วยตัวเองทำให้จำ concepts ได้นานกว่าการอ่านซ้ำ

---

**Remediation Path:**
- ถ้าตอบคำถาม 1 ไม่ได้ → อ่าน section 4.2 (Ramp-Up) ใหม่
- ถ้าตอบคำถาม 2 ไม่ได้ → อ่าน section 4.3 (Think Time) โดยเฉพาะตาราง "สรุปการเลือก"
- ถ้าตอบคำถาม 3 ได้แค่บางส่วน → อ่าน section 6 (Common Mistakes) ทั้งหมด

---

*บทที่ 6: Parameterization ด้วย CSV Data Set Config →*
