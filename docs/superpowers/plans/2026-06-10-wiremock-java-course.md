# WireMock Java Course Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** สร้างสื่อการเรียนรู้ WireMock ภาษาไทย 11 บท สำหรับมือใหม่ที่รู้ Java พื้นฐาน + 4 HTML animations + exercises + glossary

**Architecture:** Markdown files สำหรับเนื้อหาหลัก + HTML/CSS/JS animations สำหรับ 4 visual concepts + sample Maven project สำหรับ verify code ทุก example ก่อนใส่เอกสาร

**Tech Stack:** Java 17, Maven, Spring Boot 3.x, WireMock 3.13.2, wiremock-spring-boot 4.0.9, JUnit 5, AssertJ

**Spec:** `docs/superpowers/specs/2026-06-09-wiremock-java-course-design.md`

---

## File Map

### New Files
```
docs/wiremock-java/
├── 00-overview.md
├── 01-why-mock-api.md
├── 02-setup.md
├── 03-basic-stub-get.md
├── 04-verify.md
├── 05-request-matching-post-body.md
├── 06-request-matching-headers-query.md
├── 07-response-templating.md
├── 08-scenarios-stateful.md
├── 09-spring-boot-integration.md
├── 10-error-simulation.md
├── 11-best-practices.md
├── exercises.md
├── glossary.md
├── wiremock-source-notes.md
└── animations/
    ├── 01-how-wiremock-works.html
    ├── 02-stub-lifecycle.html
    ├── 03-url-matching.html
    └── 04-scenario-state.html

docs/wiremock-java/sample-project/
├── pom.xml
└── src/test/java/com/example/wiremock/
    ├── Ch03BasicStubTest.java
    ├── Ch04VerifyTest.java
    ├── Ch05RequestMatchingPostTest.java
    ├── Ch06RequestMatchingHeadersTest.java
    ├── Ch07ResponseTemplatingTest.java
    ├── Ch08ScenariosTest.java
    ├── Ch09SpringBootTest.java  (Spring Boot integration tests)
    └── Ch10ErrorSimulationTest.java
```

---

## Task 1: สร้าง Directory Structure + Sample Maven Project

**Files:**
- Create: `docs/wiremock-java/` (directory)
- Create: `docs/wiremock-java/sample-project/pom.xml`
- Create: `docs/wiremock-java/wiremock-source-notes.md`

- [ ] **Step 1: สร้าง directories**

```powershell
New-Item -ItemType Directory -Force "docs/wiremock-java/animations"
New-Item -ItemType Directory -Force "docs/wiremock-java/sample-project/src/test/java/com/example/wiremock"
New-Item -ItemType Directory -Force "docs/wiremock-java/sample-project/src/main/java/com/example/wiremock"
New-Item -ItemType Directory -Force "docs/wiremock-java/sample-project/src/main/resources"
```

- [ ] **Step 2: สร้าง pom.xml**

สร้าง `docs/wiremock-java/sample-project/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.1</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>wiremock-course</artifactId>
    <version>1.0.0</version>
    <name>WireMock Course Sample</name>

    <properties>
        <java.version>17</java.version>
        <wiremock.version>3.13.2</wiremock.version>
        <wiremock-spring-boot.version>4.0.9</wiremock-spring-boot.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- WireMock 3.x -->
        <dependency>
            <groupId>org.wiremock</groupId>
            <artifactId>wiremock</artifactId>
            <version>${wiremock.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- WireMock Spring Boot Integration -->
        <dependency>
            <groupId>org.wiremock.integrations</groupId>
            <artifactId>wiremock-spring-boot</artifactId>
            <version>${wiremock-spring-boot.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: สร้าง Spring Boot main class (จำเป็นสำหรับ @SpringBootTest)**

สร้าง `docs/wiremock-java/sample-project/src/main/java/com/example/wiremock/WireMockCourseApplication.java`:

```java
package com.example.wiremock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WireMockCourseApplication {
    public static void main(String[] args) {
        SpringApplication.run(WireMockCourseApplication.class, args);
    }
}
```

- [ ] **Step 4: สร้าง application.properties**

สร้าง `docs/wiremock-java/sample-project/src/main/resources/application.properties`:

```properties
payment.service.url=http://localhost:8080
```

- [ ] **Step 5: สร้าง PaymentClient (running example — ใช้ตลอด course)**

สร้าง `docs/wiremock-java/sample-project/src/main/java/com/example/wiremock/PaymentClient.java`:

```java
package com.example.wiremock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Component
public class PaymentClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PaymentClient(RestTemplate restTemplate,
                         @Value("${payment.service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public PaymentResponse getPayment(String paymentId) {
        return restTemplate.getForObject(
            baseUrl + "/payments/" + paymentId,
            PaymentResponse.class
        );
    }

    public PaymentResponse createPayment(PaymentRequest request) {
        HttpEntity<PaymentRequest> entity = new HttpEntity<>(request,
            new HttpHeaders() {{ setContentType(MediaType.APPLICATION_JSON); }});
        return restTemplate.postForObject(
            baseUrl + "/payments",
            entity,
            PaymentResponse.class
        );
    }
}
```

- [ ] **Step 6: สร้าง model classes**

สร้าง `docs/wiremock-java/sample-project/src/main/java/com/example/wiremock/PaymentRequest.java`:

```java
package com.example.wiremock;

public class PaymentRequest {
    private int amount;
    private String currency;
    private String description;

    public PaymentRequest() {}
    public PaymentRequest(int amount, String currency, String description) {
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }
    // getters & setters
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
```

สร้าง `docs/wiremock-java/sample-project/src/main/java/com/example/wiremock/PaymentResponse.java`:

```java
package com.example.wiremock;

public class PaymentResponse {
    private String id;
    private String status;
    private int amount;
    private String currency;

    public PaymentResponse() {}
    // getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
```

- [ ] **Step 7: สร้าง RestTemplate bean config**

สร้าง `docs/wiremock-java/sample-project/src/main/java/com/example/wiremock/AppConfig.java`:

```java
package com.example.wiremock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

- [ ] **Step 8: สร้าง source-notes file**

สร้าง `docs/wiremock-java/wiremock-source-notes.md`:

```markdown
# WireMock Course — Source Notes

ใช้ไฟล์นี้บันทึก QUOTE จาก official docs ก่อนเขียนแต่ละ concept

Format:
SOURCE: [URL]
VERSION: [version / date]
CONCEPT: [ชื่อ concept]
QUOTE: "[ข้อความตรงจาก docs]"

---
(เพิ่ม quotes ระหว่างเขียนแต่ละบท)
```

- [ ] **Step 9: Compile เพื่อตรวจ pom.xml ถูกต้อง**

```powershell
cd docs/wiremock-java/sample-project
mvn compile -q
```

Expected: `BUILD SUCCESS` (ไม่มี error)

- [ ] **Step 10: Commit**

```bash
git add docs/wiremock-java/
git commit -m "feat: scaffold WireMock course directory + sample Maven project"
```

---

## Task 2: Animation 01 — How WireMock Works

**Files:**
- Create: `docs/wiremock-java/animations/01-how-wiremock-works.html`

- [ ] **Step 1: สร้าง animation HTML**

สร้าง `docs/wiremock-java/animations/01-how-wiremock-works.html` — visualize request flow:
Client → (ถ้าไม่มี WireMock: จะชน real API) vs (ถ้ามี WireMock: intercepted → fake response)

Animation มี 3 scenes:
- Scene 1: Production — Client เรียก Real Payment API
- Scene 2: ปัญหา — Test เรียก Real API (❌ 느린, เปลือง, flaky)
- Scene 3: Solution — Test เรียก WireMock (✅ เร็ว, controllable, offline)

```html
<!DOCTYPE html>
<html lang="th">
<head>
<meta charset="UTF-8">
<title>WireMock ทำงานอย่างไร?</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: 'Segoe UI', sans-serif; background: #0f172a; color: #e2e8f0; min-height: 100vh; display: flex; flex-direction: column; align-items: center; padding: 2rem; }
  h1 { font-size: 1.5rem; margin-bottom: 0.5rem; color: #7dd3fc; }
  .subtitle { color: #94a3b8; margin-bottom: 2rem; font-size: 0.9rem; }

  .scene { display: none; width: 100%; max-width: 800px; }
  .scene.active { display: block; }

  .scene-title { font-size: 1.1rem; font-weight: 600; margin-bottom: 1.5rem; color: #f1f5f9; }
  .diagram { display: flex; align-items: center; justify-content: center; gap: 0; flex-wrap: nowrap; margin: 2rem 0; }

  .box { padding: 0.8rem 1.2rem; border-radius: 8px; font-size: 0.85rem; font-weight: 600; text-align: center; min-width: 120px; }
  .box.client { background: #1e40af; border: 2px solid #3b82f6; }
  .box.real-api { background: #065f46; border: 2px solid #10b981; }
  .box.wiremock { background: #7c3aed; border: 2px solid #a78bfa; }
  .box.danger { background: #7f1d1d; border: 2px dashed #ef4444; }

  .arrow { display: flex; flex-direction: column; align-items: center; margin: 0 0.5rem; }
  .arrow-line { width: 80px; height: 2px; background: #64748b; position: relative; }
  .arrow-line.animated { background: #3b82f6; animation: flow 1.2s ease-in-out infinite; }
  .arrow-line.blocked { background: #ef4444; }
  .arrow-label { font-size: 0.7rem; color: #94a3b8; margin-top: 4px; white-space: nowrap; }
  .arrow-head { width: 0; height: 0; border-left: 8px solid #64748b; border-top: 5px solid transparent; border-bottom: 5px solid transparent; }
  .arrow-head.blue { border-left-color: #3b82f6; }
  .arrow-head.red { border-left-color: #ef4444; }
  .arrow-head.purple { border-left-color: #a78bfa; }

  @keyframes flow {
    0% { opacity: 0.3; } 50% { opacity: 1; } 100% { opacity: 0.3; }
  }

  .badge { display: inline-block; padding: 0.2rem 0.6rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 700; margin-left: 0.5rem; }
  .badge.bad { background: #7f1d1d; color: #fca5a5; }
  .badge.good { background: #14532d; color: #86efac; }

  .explanation { background: #1e293b; border-radius: 8px; padding: 1.2rem; margin-top: 1rem; font-size: 0.9rem; line-height: 1.6; }
  .explanation ul { padding-left: 1.2rem; margin-top: 0.5rem; }
  .explanation li { margin-bottom: 0.3rem; color: #cbd5e1; }
  .explanation .red { color: #f87171; }
  .explanation .green { color: #4ade80; }

  .nav { display: flex; gap: 1rem; margin-top: 2rem; }
  .btn { padding: 0.6rem 1.5rem; border-radius: 6px; border: none; cursor: pointer; font-size: 0.9rem; font-weight: 600; transition: all 0.2s; }
  .btn-primary { background: #3b82f6; color: white; }
  .btn-primary:hover { background: #2563eb; }
  .btn-secondary { background: #334155; color: #e2e8f0; }
  .btn-secondary:hover { background: #475569; }
  .btn:disabled { opacity: 0.4; cursor: not-allowed; }

  .progress { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
  .dot { width: 10px; height: 10px; border-radius: 50%; background: #334155; }
  .dot.active { background: #3b82f6; }
  .dot.done { background: #4ade80; }

  .x-mark { font-size: 1.5rem; color: #ef4444; margin: 0 0.5rem; }
</style>
</head>
<body>
<h1>WireMock ทำงานอย่างไร?</h1>
<p class="subtitle">คลิก "ถัดไป" เพื่อดูแต่ละขั้นตอน</p>

<div class="progress">
  <div class="dot active" id="dot0"></div>
  <div class="dot" id="dot1"></div>
  <div class="dot" id="dot2"></div>
</div>

<!-- Scene 1: Production -->
<div class="scene active" id="scene0">
  <div class="scene-title">🏭 Scenario: Production — ระบบจริง</div>
  <div class="diagram">
    <div class="box client">Order<br>Service</div>
    <div class="arrow">
      <div style="display:flex;align-items:center">
        <div class="arrow-line animated"></div>
        <div class="arrow-head blue"></div>
      </div>
      <div class="arrow-label">HTTP POST /payments</div>
    </div>
    <div class="box real-api">Omise<br>Payment API</div>
  </div>
  <div class="explanation">
    ใน production — <span class="green">Order Service</span> เรียก <span class="green">Omise Payment API จริง</span> ปกติ ✅<br><br>
    แต่ถ้าเราจะ <strong>test</strong> code ของ Order Service ล่ะ?
  </div>
</div>

<!-- Scene 2: Problem -->
<div class="scene" id="scene1">
  <div class="scene-title">⚠️ ปัญหา: Test เรียก API จริง <span class="badge bad">BAD</span></div>
  <div class="diagram">
    <div class="box client">JUnit<br>Test</div>
    <div class="arrow">
      <div style="display:flex;align-items:center">
        <div class="arrow-line" style="background:#ef4444"></div>
        <div class="arrow-head red"></div>
      </div>
      <div class="arrow-label" style="color:#f87171">เรียก API จริง??</div>
    </div>
    <div class="box danger">Omise API<br>(real money!)</div>
  </div>
  <div class="explanation">
    ถ้า test เรียก Payment API จริง:
    <ul>
      <li><span class="red">❌</span> ต้องมี internet connection ตลอดเวลา</li>
      <li><span class="red">❌</span> API ล่ม = test พัง ทั้งที่ code เราถูก</li>
      <li><span class="red">❌</span> Test ช้า (network latency)</li>
      <li><span class="red">❌</span> ถ้าเป็น payment จริง อาจโดนเรียกเงินได้</li>
      <li><span class="red">❌</span> จำลอง error (500, timeout) ทำได้ยาก</li>
    </ul>
  </div>
</div>

<!-- Scene 3: Solution -->
<div class="scene" id="scene2">
  <div class="scene-title">✅ Solution: ใช้ WireMock <span class="badge good">GOOD</span></div>
  <div class="diagram">
    <div class="box client">JUnit<br>Test</div>
    <div class="arrow">
      <div style="display:flex;align-items:center">
        <div class="arrow-line animated" style="background:#a78bfa"></div>
        <div class="arrow-head purple"></div>
      </div>
      <div class="arrow-label" style="color:#c4b5fd">เรียก WireMock</div>
    </div>
    <div class="box wiremock">WireMock<br>Server<br><small style="font-weight:normal;color:#c4b5fd">localhost:8080</small></div>
    <div style="margin:0 1rem;color:#64748b;font-size:1.5rem">→</div>
    <div style="font-size: 0.8rem; color: #94a3b8; text-align:center">
      <div style="background:#1e293b;padding:0.5rem 0.8rem;border-radius:6px;border:1px solid #334155">
        Fake Response<br><code style="color:#4ade80">{"status":"success"}</code>
      </div>
    </div>
  </div>
  <div class="explanation">
    WireMock เป็น <strong>fake HTTP server</strong> ที่รันใน test:
    <ul>
      <li><span class="green">✅</span> ไม่ต้องใช้ internet — รันได้ offline</li>
      <li><span class="green">✅</span> เร็วมาก — ไม่มี network latency จริง</li>
      <li><span class="green">✅</span> ควบคุม response ได้ทุกอย่าง (200, 500, timeout)</li>
      <li><span class="green">✅</span> Test ผ่านหรือไม่ขึ้นกับ code เรา ไม่ใช่ API ภายนอก</li>
    </ul>
  </div>
</div>

<div class="nav">
  <button class="btn btn-secondary" id="prevBtn" onclick="navigate(-1)" disabled>← ก่อนหน้า</button>
  <button class="btn btn-primary" id="nextBtn" onclick="navigate(1)">ถัดไป →</button>
</div>

<script>
let current = 0;
const total = 3;

function navigate(dir) {
  document.getElementById('scene' + current).classList.remove('active');
  document.getElementById('dot' + current).classList.remove('active');
  document.getElementById('dot' + current).classList.add('done');
  current += dir;
  document.getElementById('scene' + current).classList.add('active');
  document.getElementById('dot' + current).classList.add('active');
  document.getElementById('prevBtn').disabled = current === 0;
  document.getElementById('nextBtn').disabled = current === total - 1;
  if (current === total - 1) {
    document.getElementById('nextBtn').textContent = '✓ เสร็จแล้ว';
  } else {
    document.getElementById('nextBtn').textContent = 'ถัดไป →';
  }
}
</script>
</body>
</html>
```

- [ ] **Step 2: เปิด browser ตรวจ animation ทำงานถูกต้อง**

เปิด `docs/wiremock-java/animations/01-how-wiremock-works.html` ใน browser  
ตรวจ: 3 scenes navigate ได้, animation ไหลสวย, ปุ่ม prev/next ทำงาน

- [ ] **Step 3: Commit**

```bash
git add docs/wiremock-java/animations/01-how-wiremock-works.html
git commit -m "feat: add WireMock animation 01 - how wiremock works"
```

---

## Task 3: Animation 02 — Stub Lifecycle

**Files:**
- Create: `docs/wiremock-java/animations/02-stub-lifecycle.html`

- [ ] **Step 1: สร้าง animation**

สร้าง `docs/wiremock-java/animations/02-stub-lifecycle.html` — step-by-step highlight แต่ละขั้นของ lifecycle:

```html
<!DOCTYPE html>
<html lang="th">
<head>
<meta charset="UTF-8">
<title>WireMock Stub Lifecycle</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: 'Segoe UI', sans-serif; background: #0f172a; color: #e2e8f0; padding: 2rem; display: flex; flex-direction: column; align-items: center; }
  h1 { font-size: 1.5rem; color: #7dd3fc; margin-bottom: 0.3rem; }
  .subtitle { color: #94a3b8; font-size: 0.9rem; margin-bottom: 2rem; }

  .timeline { display: flex; flex-direction: column; gap: 0; width: 100%; max-width: 700px; }
  .step { display: flex; gap: 1rem; align-items: flex-start; padding: 1rem; border-radius: 8px; border: 1px solid #1e293b; transition: all 0.4s ease; opacity: 0.4; }
  .step.active { opacity: 1; border-color: #3b82f6; background: #1e293b; transform: scale(1.01); }
  .step.done { opacity: 0.7; border-color: #22c55e; background: #0f2818; }

  .step-num { width: 32px; height: 32px; border-radius: 50%; background: #334155; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 0.85rem; flex-shrink: 0; margin-top: 2px; }
  .step.active .step-num { background: #3b82f6; }
  .step.done .step-num { background: #22c55e; }

  .step-content { flex: 1; }
  .step-title { font-weight: 600; margin-bottom: 0.3rem; }
  .step.active .step-title { color: #7dd3fc; }
  .step.done .step-title { color: #4ade80; }
  .step-desc { font-size: 0.85rem; color: #94a3b8; }
  .step.active .step-desc { color: #cbd5e1; }

  .code-block { background: #0a0f1e; border: 1px solid #1e3a5f; border-radius: 6px; padding: 0.7rem 1rem; margin-top: 0.5rem; font-family: 'Consolas', monospace; font-size: 0.8rem; color: #93c5fd; display: none; }
  .step.active .code-block { display: block; }

  .connector { width: 2px; height: 20px; background: #1e293b; margin-left: 2rem; }
  .connector.done { background: #22c55e; }

  .nav { display: flex; gap: 1rem; margin-top: 2rem; }
  .btn { padding: 0.6rem 1.5rem; border-radius: 6px; border: none; cursor: pointer; font-weight: 600; font-size: 0.9rem; transition: all 0.2s; }
  .btn-primary { background: #3b82f6; color: white; }
  .btn-primary:hover { background: #2563eb; }
  .btn-secondary { background: #334155; color: #e2e8f0; }
  .btn-secondary:hover { background: #475569; }
  .btn:disabled { opacity: 0.4; cursor: not-allowed; }

  .progress-text { color: #64748b; font-size: 0.85rem; margin-top: 1rem; }
</style>
</head>
<body>
<h1>WireMock Stub Lifecycle</h1>
<p class="subtitle">ลำดับขั้นตอนที่ถูกต้องในการใช้ WireMock</p>

<div class="timeline" id="timeline"></div>
<div class="progress-text" id="progressText">ขั้นตอนที่ 1 จาก 6</div>

<div class="nav">
  <button class="btn btn-secondary" id="prevBtn" onclick="navigate(-1)" disabled>← ก่อนหน้า</button>
  <button class="btn btn-primary" id="nextBtn" onclick="navigate(1)">ถัดไป →</button>
</div>

<script>
const steps = [
  {
    title: "① start() — เปิด Server",
    desc: "สร้าง WireMockServer และ start() ก่อนเสมอ ทำใน @BeforeEach",
    code: "WireMockServer wireMock = new WireMockServer(wireMockConfig().port(8080));\nwireMock.start(); // เปิด fake HTTP server ที่ port 8080"
  },
  {
    title: "② stubFor() — กำหนด Response",
    desc: "บอก WireMock ว่า 'ถ้าได้รับ request แบบนี้ ให้ตอบกลับแบบนี้'",
    code: "wireMock.stubFor(get(\"/payments/123\")\n  .willReturn(okJson(\"{\\\"id\\\":\\\"123\\\",\\\"status\\\":\\\"success\\\"}\")));"
  },
  {
    title: "③ HTTP Call — Client เรียก WireMock",
    desc: "Code จริงที่กำลัง test เรียก localhost:8080 — WireMock รับแล้วตอบตาม stub",
    code: "// PaymentClient ชี้ไปที่ localhost:8080\nPaymentResponse response = paymentClient.getPayment(\"123\");\n// WireMock ตอบกลับ fake response ทันที"
  },
  {
    title: "④ verify() — ตรวจการเรียก",
    desc: "ยืนยันว่า client เรียก API ถูก URL, method, และ payload จริงๆ",
    code: "wireMock.verify(getRequestedFor(urlEqualTo(\"/payments/123\")));\n// ถ้า client ไม่ได้เรียก → test fails ทันที"
  },
  {
    title: "⑤ reset() — ล้าง Stubs",
    desc: "ล้าง stubs ทั้งหมดหลังแต่ละ test เพื่อป้องกัน stubs รั่วไปยัง test ถัดไป",
    code: "wireMock.resetAll(); // ล้างทั้ง stubs และ request history\n// ทำใน @AfterEach เสมอ (ถ้าใช้ WireMockServer โดยตรง)"
  },
  {
    title: "⑥ stop() — ปิด Server",
    desc: "หยุด WireMock server หลัง test suite จบ เพื่อ free port",
    code: "wireMock.stop(); // ทำใน @AfterAll หรือ @AfterEach\n// ถ้าลืม → ครั้งต่อไปจะ fail ด้วย 'port already in use'"
  }
];

let current = 0;

function render() {
  const timeline = document.getElementById('timeline');
  timeline.innerHTML = '';
  steps.forEach((s, i) => {
    const stepDiv = document.createElement('div');
    stepDiv.className = 'step' + (i === current ? ' active' : i < current ? ' done' : '');
    stepDiv.innerHTML = `
      <div class="step-num">${i < current ? '✓' : i + 1}</div>
      <div class="step-content">
        <div class="step-title">${s.title}</div>
        <div class="step-desc">${s.desc}</div>
        <div class="code-block">${s.code}</div>
      </div>`;
    timeline.appendChild(stepDiv);
    if (i < steps.length - 1) {
      const conn = document.createElement('div');
      conn.className = 'connector' + (i < current ? ' done' : '');
      timeline.appendChild(conn);
    }
  });
  document.getElementById('progressText').textContent = `ขั้นตอนที่ ${current + 1} จาก ${steps.length}`;
  document.getElementById('prevBtn').disabled = current === 0;
  document.getElementById('nextBtn').disabled = current === steps.length - 1;
  document.getElementById('nextBtn').textContent = current === steps.length - 1 ? '✓ เสร็จ' : 'ถัดไป →';
}

function navigate(dir) {
  current = Math.max(0, Math.min(steps.length - 1, current + dir));
  render();
}

render();
</script>
</body>
</html>
```

- [ ] **Step 2: เปิด browser ตรวจ animation**

เปิด `docs/wiremock-java/animations/02-stub-lifecycle.html` — ตรวจ 6 steps highlight ได้, code แสดงถูกต้อง

- [ ] **Step 3: Commit**

```bash
git add docs/wiremock-java/animations/02-stub-lifecycle.html
git commit -m "feat: add WireMock animation 02 - stub lifecycle"
```

---

## Task 4: Animation 03 — URL Matching Interactive

**Files:**
- Create: `docs/wiremock-java/animations/03-url-matching.html`

- [ ] **Step 1: สร้าง interactive URL matching demo**

สร้าง `docs/wiremock-java/animations/03-url-matching.html` — user พิมพ์ URL แล้วเห็นว่า matcher แต่ละตัว match หรือไม่:

```html
<!DOCTYPE html>
<html lang="th">
<head>
<meta charset="UTF-8">
<title>WireMock URL Matching</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: 'Segoe UI', sans-serif; background: #0f172a; color: #e2e8f0; padding: 2rem; display: flex; flex-direction: column; align-items: center; }
  h1 { font-size: 1.5rem; color: #7dd3fc; margin-bottom: 0.3rem; }
  .subtitle { color: #94a3b8; font-size: 0.9rem; margin-bottom: 2rem; }

  .container { width: 100%; max-width: 750px; }

  .input-section { background: #1e293b; border-radius: 8px; padding: 1.2rem; margin-bottom: 1.5rem; }
  .input-label { font-size: 0.85rem; color: #94a3b8; margin-bottom: 0.5rem; }
  .url-input { width: 100%; background: #0f172a; border: 1px solid #334155; border-radius: 6px; padding: 0.6rem 0.8rem; color: #e2e8f0; font-family: 'Consolas', monospace; font-size: 0.9rem; outline: none; }
  .url-input:focus { border-color: #3b82f6; }

  .presets { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-top: 0.7rem; }
  .preset-btn { padding: 0.3rem 0.7rem; background: #334155; border: none; border-radius: 4px; color: #94a3b8; font-size: 0.75rem; cursor: pointer; font-family: 'Consolas', monospace; }
  .preset-btn:hover { background: #475569; color: #e2e8f0; }

  .matchers { display: flex; flex-direction: column; gap: 0.8rem; }
  .matcher { background: #1e293b; border-radius: 8px; padding: 1rem; border-left: 4px solid #334155; transition: border-color 0.2s; }
  .matcher.match { border-left-color: #22c55e; }
  .matcher.no-match { border-left-color: #ef4444; }

  .matcher-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
  .matcher-name { font-family: 'Consolas', monospace; font-size: 0.9rem; color: #93c5fd; font-weight: 600; }
  .match-badge { padding: 0.2rem 0.6rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 700; }
  .match-badge.yes { background: #14532d; color: #4ade80; }
  .match-badge.no { background: #7f1d1d; color: #f87171; }

  .matcher-pattern { font-family: 'Consolas', monospace; font-size: 0.8rem; color: #fbbf24; background: #0a0f1e; padding: 0.4rem 0.7rem; border-radius: 4px; margin-bottom: 0.5rem; display: inline-block; }
  .matcher-desc { font-size: 0.82rem; color: #94a3b8; }
  .matcher.match .matcher-desc { color: #cbd5e1; }
</style>
</head>
<body>
<h1>URL Matching</h1>
<p class="subtitle">พิมพ์ URL แล้วดูว่า matcher แต่ละตัว match หรือไม่</p>

<div class="container">
  <div class="input-section">
    <div class="input-label">Request URL ที่ Client ส่งมา:</div>
    <input class="url-input" id="urlInput" type="text" value="/payments/123?currency=THB" oninput="updateMatchers()" />
    <div class="presets">
      <span style="font-size:0.75rem;color:#64748b;margin-right:0.3rem">ลอง:</span>
      <button class="preset-btn" onclick="setUrl('/payments/123')">/payments/123</button>
      <button class="preset-btn" onclick="setUrl('/payments/123?currency=THB')">/payments/123?currency=THB</button>
      <button class="preset-btn" onclick="setUrl('/payments/456?currency=USD')">/payments/456?currency=USD</button>
      <button class="preset-btn" onclick="setUrl('/payments')">/payments</button>
      <button class="preset-btn" onclick="setUrl('/orders/789')">/orders/789</button>
    </div>
  </div>

  <div class="matchers" id="matchers"></div>
</div>

<script>
const matchers = [
  {
    name: 'urlEqualTo("/payments/123")',
    pattern: '/payments/123',
    desc: 'Match URL แบบ exact รวม query string — ถ้ามี ?currency=THB จะไม่ match',
    test: (url) => url === '/payments/123'
  },
  {
    name: 'urlEqualTo("/payments/123?currency=THB")',
    pattern: '/payments/123?currency=THB',
    desc: 'Match URL + query string แบบ exact ทั้งหมด',
    test: (url) => url === '/payments/123?currency=THB'
  },
  {
    name: 'urlPathEqualTo("/payments/123")',
    pattern: '/payments/123 (path only)',
    desc: 'Match เฉพาะ path — ไม่สนใจ query string ตามหลัง ? ใช้คู่กับ withQueryParam()',
    test: (url) => url.split('?')[0] === '/payments/123'
  },
  {
    name: 'urlMatching("/payments/[0-9]+")',
    pattern: '/payments/[0-9]+ (regex)',
    desc: 'Match ด้วย regex — ยืดหยุ่นสูงสุด แต่ใช้เมื่อจำเป็นเท่านั้น',
    test: (url) => /^\/payments\/[0-9]+(\?.*)?$/.test(url)
  },
  {
    name: 'urlPathMatching("/payments/.*")',
    pattern: '/payments/.* (regex path)',
    desc: 'Match path ด้วย regex — ไม่สนใจ query string',
    test: (url) => /^\/payments\/.*/.test(url.split('?')[0])
  }
];

function setUrl(url) {
  document.getElementById('urlInput').value = url;
  updateMatchers();
}

function updateMatchers() {
  const url = document.getElementById('urlInput').value;
  const container = document.getElementById('matchers');
  container.innerHTML = matchers.map(m => {
    const isMatch = m.test(url);
    return `<div class="matcher ${isMatch ? 'match' : 'no-match'}">
      <div class="matcher-header">
        <span class="matcher-name">${m.name}</span>
        <span class="match-badge ${isMatch ? 'yes' : 'no'}">${isMatch ? '✓ MATCH' : '✗ NO MATCH'}</span>
      </div>
      <div class="matcher-pattern">${m.pattern}</div>
      <div class="matcher-desc">${m.desc}</div>
    </div>`;
  }).join('');
}

updateMatchers();
</script>
</body>
</html>
```

- [ ] **Step 2: เปิด browser ตรวจ interactive matching**

พิมพ์ URL ต่างๆ แล้วตรวจว่า match/no-match ถูกต้อง

- [ ] **Step 3: Commit**

```bash
git add docs/wiremock-java/animations/03-url-matching.html
git commit -m "feat: add WireMock animation 03 - interactive URL matching"
```

---

## Task 5: Animation 04 — Scenario State Machine

**Files:**
- Create: `docs/wiremock-java/animations/04-scenario-state.html`

- [ ] **Step 1: สร้าง state machine animation**

สร้าง `docs/wiremock-java/animations/04-scenario-state.html` — animated state diagram แสดง retry flow:

```html
<!DOCTYPE html>
<html lang="th">
<head>
<meta charset="UTF-8">
<title>WireMock Scenarios</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: 'Segoe UI', sans-serif; background: #0f172a; color: #e2e8f0; padding: 2rem; display: flex; flex-direction: column; align-items: center; }
  h1 { font-size: 1.5rem; color: #7dd3fc; margin-bottom: 0.3rem; }
  .subtitle { color: #94a3b8; font-size: 0.9rem; margin-bottom: 2rem; }

  .diagram { display: flex; align-items: center; justify-content: center; gap: 1.5rem; margin: 2rem 0; flex-wrap: wrap; }
  .state { width: 130px; height: 80px; border-radius: 12px; display: flex; flex-direction: column; align-items: center; justify-content: center; border: 2px solid #334155; background: #1e293b; transition: all 0.4s; }
  .state.active { border-color: #3b82f6; background: #1e3a5f; box-shadow: 0 0 20px #3b82f640; transform: scale(1.05); }
  .state.done { border-color: #22c55e; background: #0f2818; }
  .state-name { font-weight: 700; font-size: 0.9rem; }
  .state-label { font-size: 0.72rem; color: #94a3b8; margin-top: 0.2rem; }
  .state.active .state-label { color: #93c5fd; }

  .transition { display: flex; flex-direction: column; align-items: center; gap: 0.3rem; }
  .arrow-h { display: flex; align-items: center; }
  .line-h { width: 60px; height: 2px; background: #334155; transition: background 0.4s; }
  .line-h.active { background: #3b82f6; }
  .line-h.done { background: #22c55e; }
  .arrowhead { border-left: 8px solid #334155; border-top: 5px solid transparent; border-bottom: 5px solid transparent; transition: border-color 0.4s; }
  .arrowhead.active { border-left-color: #3b82f6; }
  .arrowhead.done { border-left-color: #22c55e; }
  .t-label { font-size: 0.7rem; color: #94a3b8; white-space: nowrap; text-align: center; }
  .t-label.active { color: #93c5fd; }

  .log { background: #0a0f1e; border: 1px solid #1e293b; border-radius: 8px; padding: 1rem; width: 100%; max-width: 700px; min-height: 120px; font-family: 'Consolas', monospace; font-size: 0.82rem; }
  .log-line { margin-bottom: 0.3rem; }
  .log-line.req { color: #93c5fd; }
  .log-line.res-ok { color: #4ade80; }
  .log-line.res-err { color: #f87171; }
  .log-line.state-change { color: #fbbf24; }

  .nav { display: flex; gap: 1rem; margin-top: 1.5rem; }
  .btn { padding: 0.6rem 1.5rem; border-radius: 6px; border: none; cursor: pointer; font-weight: 600; font-size: 0.9rem; }
  .btn-primary { background: #3b82f6; color: white; }
  .btn-primary:hover { background: #2563eb; }
  .btn-secondary { background: #334155; color: #e2e8f0; }
  .btn-secondary:hover { background: #475569; }
  .btn:disabled { opacity: 0.4; cursor: not-allowed; }
  .reset-btn { background: #7c3aed; color: white; }
  .reset-btn:hover { background: #6d28d9; }
</style>
</head>
<body>
<h1>WireMock Scenarios — Stateful Mock</h1>
<p class="subtitle">จำลอง "Payment API pending ก่อน แล้วค่อย success" แบบ retry pattern</p>

<div class="diagram">
  <div class="state" id="s0">
    <div class="state-name">STARTED</div>
    <div class="state-label">State เริ่มต้น</div>
  </div>
  <div class="transition">
    <div class="arrow-h">
      <div class="line-h" id="line1"></div>
      <div class="arrowhead" id="head1"></div>
    </div>
    <div class="t-label" id="label1">Call #1<br>GET /payments/123</div>
  </div>
  <div class="state" id="s1">
    <div class="state-name">PENDING</div>
    <div class="state-label">ยังรอ confirm</div>
  </div>
  <div class="transition">
    <div class="arrow-h">
      <div class="line-h" id="line2"></div>
      <div class="arrowhead" id="head2"></div>
    </div>
    <div class="t-label" id="label2">Call #2<br>GET /payments/123</div>
  </div>
  <div class="state" id="s2">
    <div class="state-name">SUCCESS</div>
    <div class="state-label">Payment ผ่านแล้ว</div>
  </div>
</div>

<div class="log" id="log">
  <div style="color:#64748b">// กด "ส่ง Request" เพื่อจำลอง client เรียก API...</div>
</div>

<div class="nav">
  <button class="btn btn-primary" id="callBtn" onclick="sendRequest()">📤 ส่ง Request</button>
  <button class="btn reset-btn" onclick="resetSim()">↺ Reset</button>
</div>

<script>
let callCount = 0;
const logs = [];

const responses = [
  { status: 202, body: '{"id":"123","status":"pending","message":"Payment is being processed"}', stateChange: 'STARTED → PENDING' },
  { status: 200, body: '{"id":"123","status":"success","amount":500,"currency":"THB"}', stateChange: 'PENDING → SUCCESS' }
];

function addLog(text, cls) {
  logs.push(`<div class="log-line ${cls}">${text}</div>`);
  document.getElementById('log').innerHTML = logs.join('');
}

function sendRequest() {
  if (callCount >= 2) return;
  const r = responses[callCount];
  addLog(`→ GET /payments/123 (Call #${callCount + 1})`, 'req');
  if (r.status === 202) {
    addLog(`← 202 Accepted: ${r.body}`, 'res-err');
  } else {
    addLog(`← 200 OK: ${r.body}`, 'res-ok');
  }
  addLog(`⚡ State: ${r.stateChange}`, 'state-change');

  // Update diagram
  document.getElementById('s' + callCount).classList.add('done');
  document.getElementById('line' + (callCount + 1)).classList.add(callCount === 0 ? 'active' : 'done');
  document.getElementById('head' + (callCount + 1)).classList.add(callCount === 0 ? 'active' : 'done');
  document.getElementById('label' + (callCount + 1)).classList.add('active');
  callCount++;
  document.getElementById('s' + callCount).classList.add(callCount === 2 ? 'done' : 'active');
  if (callCount >= 2) {
    document.getElementById('callBtn').disabled = true;
    document.getElementById('callBtn').textContent = '✓ ครบ 2 Calls แล้ว';
    addLog('', '');
    addLog('// Scenario เสร็จสิ้น — test ผ่าน retry logic ได้', 'state-change');
  }
}

function resetSim() {
  callCount = 0;
  logs.length = 0;
  document.getElementById('log').innerHTML = '<div style="color:#64748b">// กด "ส่ง Request" เพื่อจำลอง client เรียก API...</div>';
  ['s0','s1','s2'].forEach(id => { document.getElementById(id).className = 'state'; });
  ['line1','line2'].forEach(id => { document.getElementById(id).className = 'line-h'; });
  ['head1','head2'].forEach(id => { document.getElementById(id).className = 'arrowhead'; });
  ['label1','label2'].forEach(id => { document.getElementById(id).className = 't-label'; });
  document.getElementById('s0').classList.add('active');
  document.getElementById('callBtn').disabled = false;
  document.getElementById('callBtn').textContent = '📤 ส่ง Request';
}

document.getElementById('s0').classList.add('active');
</script>
</body>
</html>
```

- [ ] **Step 2: เปิด browser ตรวจ**

กด "ส่ง Request" 2 ครั้ง ตรวจว่า state เปลี่ยน STARTED → PENDING → SUCCESS พร้อม log ถูกต้อง

- [ ] **Step 3: Commit**

```bash
git add docs/wiremock-java/animations/04-scenario-state.html
git commit -m "feat: add WireMock animation 04 - scenario state machine"
```

---

## Task 6: 00-overview.md

**Files:**
- Create: `docs/wiremock-java/00-overview.md`

- [ ] **Step 1: WebSearch ข้อมูล WireMock ล่าสุด**

```
WebSearch: "WireMock 3.x official documentation site:wiremock.org"
WebSearch: "WireMock Java Spring Boot 2025 getting started"
```

- [ ] **Step 2: บันทึก source notes**

เพิ่มใน `docs/wiremock-java/wiremock-source-notes.md`:

```
SOURCE: https://wiremock.org/docs/
VERSION: WireMock 3.x (2025)
CONCEPT: WireMock overview
QUOTE: [copy quote จาก official docs]
```

- [ ] **Step 3: เขียน 00-overview.md**

สร้าง `docs/wiremock-java/00-overview.md` ประกอบด้วย:
- คำอธิบายสั้น: WireMock คืออะไร ทำอะไรได้
- Prerequisites รายละเอียด (Java 17, Maven 3.x, IntelliJ/VS Code)
- Table of contents พร้อม links ทุกบท
- Link ไปยัง animations ทั้ง 4 ไฟล์
- เวลาเรียนโดยประมาณ

- [ ] **Step 4: Commit**

```bash
git add docs/wiremock-java/00-overview.md docs/wiremock-java/wiremock-source-notes.md
git commit -m "feat: add WireMock course overview"
```

---

## Task 7: บทที่ 1 — ทำไมต้อง Mock API?

**Files:**
- Create: `docs/wiremock-java/01-why-mock-api.md`

- [ ] **Step 1: เขียนเนื้อหา 6 ส่วน (บทที่ 1 ไม่มี Pre-chapter Retrieval)**

เขียน `docs/wiremock-java/01-why-mock-api.md` ครบ 6 ส่วน:

1. **วัตถุประสงค์** — อธิบายได้ว่า external dependency คืออะไรและทำไม test ที่เรียก API จริงถึงเป็น anti-pattern
2. **ทำไมต้องรู้?** — scenario: test suite ช้า 3 นาทีเพราะรอ Payment API / test พังเพราะ API ล่มไม่เกี่ยวกับ code เรา
3. **เนื้อหาหลัก** — concepts: test isolation, external dependency, mock vs stub
4. **ตัวอย่าง 3 ระดับ** — Beginner: อธิบาย problem แบบ analogy / Intermediate: code ที่มี dependency จริง + ปัญหาที่เกิด / Advanced: cost of slow/flaky tests ใน CI/CD
5. **Common Mistakes** — อย่า skip mock เพราะคิดว่า "API เสถียรอยู่แล้ว"
6. **สรุปบท** — 2-3 retrieval questions

Link ไปยัง animation: `[ดู animation: WireMock ทำงานอย่างไร](animations/01-how-wiremock-works.html)`

- [ ] **Step 2: Self-review ต้องได้คะแนน ≥95**

ตรวจตาม CLAUDE.md rubric — หักคะแนนถ้าขาด section, learning objectives ใช้ verb วัดไม่ได้, ไม่มี retrieval questions

- [ ] **Step 3: Commit**

```bash
git add docs/wiremock-java/01-why-mock-api.md
git commit -m "feat: add WireMock ch01 - why mock API"
```

---

## Task 8: บทที่ 2 — Setup + Lifecycle

**Files:**
- Create: `docs/wiremock-java/02-setup.md`
- Modify: `docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch03BasicStubTest.java` (เพื่อ test setup ด้วย)

- [ ] **Step 1: WebFetch dependencies ล่าสุด**

```
WebFetch: https://wiremock.org/docs/download-and-installation/
```

บันทึก quote ของ dependency ที่ถูกต้องใน source-notes

- [ ] **Step 2: สร้าง test สำหรับ verify setup ทำงาน**

สร้าง `docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch02SetupTest.java`:

```java
package com.example.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.web.client.RestTemplate;

class Ch02SetupTest {

    private WireMockServer wireMock;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        restTemplate = new RestTemplate();
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void wireMockServerStartsSuccessfully() {
        wireMock.stubFor(get("/health")
            .willReturn(ok("OK")));

        String result = restTemplate.getForObject(
            "http://localhost:" + wireMock.port() + "/health",
            String.class
        );

        assertThat(result).isEqualTo("OK");
    }
}
```

- [ ] **Step 3: รัน test เพื่อ verify setup**

```powershell
cd docs/wiremock-java/sample-project
mvn test -Dtest=Ch02SetupTest -q
```

Expected output: `Tests run: 1, Failures: 0, Errors: 0`

- [ ] **Step 4: เขียน 02-setup.md ครบ 7 ส่วน**

รวมถึง:
- pom.xml dependencies ที่ verified แล้ว
- อธิบาย `dynamicPort()` vs hardcoded port (พร้อมเหตุผล)
- อธิบาย lifecycle: `@BeforeEach` start + `@AfterEach` stop
- Base URL injection: ทำไมต้องใช้ `wireMock.port()` แทน hardcode 8080
- Link ไปยัง animation: `[ดู animation: Stub Lifecycle](animations/02-stub-lifecycle.html)`
- Code จาก Ch02SetupTest.java แสดงพร้อม output จริง

- [ ] **Step 5: Commit**

```bash
git add docs/wiremock-java/02-setup.md docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch02SetupTest.java
git commit -m "feat: add WireMock ch02 - setup and lifecycle"
```

---

## Task 9: บทที่ 3 — Stub พื้นฐาน: GET

**Files:**
- Create: `docs/wiremock-java/03-basic-stub-get.md`
- Create: `docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch03BasicStubTest.java`

- [ ] **Step 1: WebFetch stubbing docs**

```
WebFetch: https://wiremock.org/docs/stubbing/
```

บันทึก quote สำหรับ `stubFor`, `get`, `willReturn`, `okJson`

- [ ] **Step 2: สร้าง verified test**

สร้าง `Ch03BasicStubTest.java`:

```java
package com.example.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.web.client.RestTemplate;

class Ch03BasicStubTest {

    private WireMockServer wireMock;
    private PaymentClient paymentClient;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        RestTemplate restTemplate = new RestTemplate();
        paymentClient = new PaymentClient(restTemplate,
            "http://localhost:" + wireMock.port());
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void getPaymentReturnsStubResponse() {
        // Arrange
        wireMock.stubFor(get("/payments/123")
            .willReturn(okJson("""
                {"id":"123","status":"success","amount":500,"currency":"THB"}
                """)));

        // Act
        PaymentResponse response = paymentClient.getPayment("123");

        // Assert
        assertThat(response.getId()).isEqualTo("123");
        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getAmount()).isEqualTo(500);
    }
}
```

- [ ] **Step 3: รัน test**

```powershell
mvn test -Dtest=Ch03BasicStubTest -q
```

Expected: `Tests run: 1, Failures: 0, Errors: 0`

- [ ] **Step 4: เขียน 03-basic-stub-get.md ครบ 7 ส่วน**

ใช้ running example: `GET /payments/123` จาก Order Service → Payment API  
แสดง output จริงจาก test run ข้างบน

- [ ] **Step 5: Commit**

```bash
git add docs/wiremock-java/03-basic-stub-get.md docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch03BasicStubTest.java
git commit -m "feat: add WireMock ch03 - basic GET stub"
```

---

## Task 10: บทที่ 4 — Verify

**Files:**
- Create: `docs/wiremock-java/04-verify.md`
- Create: `docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch04VerifyTest.java`

- [ ] **Step 1: WebFetch verifying docs**

```
WebFetch: https://wiremock.org/docs/verifying/
```

- [ ] **Step 2: สร้าง verified test**

สร้าง `Ch04VerifyTest.java` ครอบคลุม:
- `verify(1, getRequestedFor(urlEqualTo(...)))` — exactly 1 call
- `verify(0, getRequestedFor(...))` — ไม่ได้เรียก
- `verify(moreThan(0), ...)` — เรียกอย่างน้อย 1 ครั้ง

- [ ] **Step 3: รัน test**

```powershell
mvn test -Dtest=Ch04VerifyTest -q
```

Expected: ผ่านทุก test case

- [ ] **Step 4: เขียน 04-verify.md ครบ 7 ส่วน**

เน้น: ทำไม verify สำคัญ (silent pass problem), ตัวอย่าง test ที่ผ่านแต่ผิด (ไม่มี verify), และวิธีแก้

- [ ] **Step 5: Commit**

```bash
git add docs/wiremock-java/04-verify.md docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch04VerifyTest.java
git commit -m "feat: add WireMock ch04 - verify"
```

---

## Task 11: บทที่ 5 — Request Matching: POST + Body

**Files:**
- Create: `docs/wiremock-java/05-request-matching-post-body.md`
- Create: `docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch05RequestMatchingPostTest.java`

- [ ] **Step 1: WebFetch request matching docs**

```
WebFetch: https://wiremock.org/docs/request-matching/
```

บันทึก quote สำหรับ `equalToJson`, `ignoreArrayOrder`, `ignoreExtraElements`

- [ ] **Step 2: สร้าง verified test**

สร้าง `Ch05RequestMatchingPostTest.java` ครอบคลุม:
- `post(urlEqualTo("/payments"))` + `withRequestBody(equalToJson(...))`
- `equalToJson` กับ `ignoreArrayOrder(true).ignoreExtraElements(true)`
- ตัวอย่าง test ที่ fail เพราะ body ไม่ตรง (เพื่อแสดงว่า matching ทำงาน)

- [ ] **Step 3: รัน test + บันทึก output**

```powershell
mvn test -Dtest=Ch05RequestMatchingPostTest -q
```

- [ ] **Step 4: เขียน 05-request-matching-post-body.md ครบ 7 ส่วน**

- [ ] **Step 5: Commit**

```bash
git add docs/wiremock-java/05-request-matching-post-body.md docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch05RequestMatchingPostTest.java
git commit -m "feat: add WireMock ch05 - request matching POST body"
```

---

## Task 12: บทที่ 6 — Request Matching: Headers + Query Params

**Files:**
- Create: `docs/wiremock-java/06-request-matching-headers-query.md`
- Create: `docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch06RequestMatchingHeadersTest.java`

- [ ] **Step 1: ใช้ source จาก Task 11 (ไม่ต้อง fetch ซ้ำ)**

- [ ] **Step 2: สร้าง verified test**

ครอบคลุม:
- `withHeader("Content-Type", equalTo("application/json"))`
- `withQueryParam("currency", equalTo("THB"))`
- `urlPathEqualTo` + `withQueryParam` (ต่างจาก `urlEqualTo`)

Link ไปยัง animation: `[ดู animation: URL Matching](animations/03-url-matching.html)`

- [ ] **Step 3: รัน test**

```powershell
mvn test -Dtest=Ch06RequestMatchingHeadersTest -q
```

- [ ] **Step 4: เขียน 06-request-matching-headers-query.md ครบ 7 ส่วน**

- [ ] **Step 5: Commit**

```bash
git add docs/wiremock-java/06-request-matching-headers-query.md docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch06RequestMatchingHeadersTest.java
git commit -m "feat: add WireMock ch06 - request matching headers and query params"
```

---

## Task 13: บทที่ 7 — Response Templating

**Files:**
- Create: `docs/wiremock-java/07-response-templating.md`
- Create: `docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch07ResponseTemplatingTest.java`

- [ ] **Step 1: WebFetch response templating docs**

```
WebFetch: https://wiremock.org/docs/response-templating/
```

บันทึก quote สำหรับ `withTransformers("response-template")`, Handlebars syntax `{{request.body}}`, `{{request.pathSegments.[1]}}`

- [ ] **Step 2: สร้าง verified test**

ครอบคลุม:
- Echo payment ID จาก URL path กลับมาใน response
- ต้อง enable `withTransformers("response-template")` หรือ global config

- [ ] **Step 3: รัน test**

```powershell
mvn test -Dtest=Ch07ResponseTemplatingTest -q
```

- [ ] **Step 4: เขียน 07-response-templating.md ครบ 7 ส่วน**

- [ ] **Step 5: Commit**

```bash
git add docs/wiremock-java/07-response-templating.md docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch07ResponseTemplatingTest.java
git commit -m "feat: add WireMock ch07 - response templating"
```

---

## Task 14: บทที่ 8 — Scenarios: Stateful API

**Files:**
- Create: `docs/wiremock-java/08-scenarios-stateful.md`
- Create: `docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch08ScenariosTest.java`

- [ ] **Step 1: WebFetch stateful scenarios docs**

```
WebFetch: https://wiremock.org/docs/stateful-behaviour/
```

- [ ] **Step 2: สร้าง verified test**

ครอบคลุม payment retry scenario:
- Call #1: 202 Accepted (pending) → scenario goes to "PENDING" state
- Call #2: 200 OK (success) → scenario goes to "SUCCESS" state

Link ไปยัง animation: `[ดู animation: Scenario State Machine](animations/04-scenario-state.html)`

- [ ] **Step 3: รัน test**

```powershell
mvn test -Dtest=Ch08ScenariosTest -q
```

- [ ] **Step 4: เขียน 08-scenarios-stateful.md ครบ 7 ส่วน**

- [ ] **Step 5: Commit**

```bash
git add docs/wiremock-java/08-scenarios-stateful.md docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch08ScenariosTest.java
git commit -m "feat: add WireMock ch08 - scenarios stateful API"
```

---

## Task 15: บทที่ 9 — Spring Boot Integration

**Files:**
- Create: `docs/wiremock-java/09-spring-boot-integration.md`
- Create: `docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch09SpringBootIntegrationTest.java`

- [ ] **Step 1: WebFetch Spring Boot integration docs**

```
WebFetch: https://wiremock.org/docs/spring-boot/
```

บันทึก quote สำหรับ `@EnableWireMock`, `@ConfigureWireMock`, `@InjectWireMock`, `@WireMockTest`

- [ ] **Step 2: สร้าง verified test**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(name = "payment-service",
    property = "payment.service.url"))
class Ch09SpringBootIntegrationTest {

    @InjectWireMock("payment-service")
    private WireMockServer wireMock;

    @Autowired
    private PaymentClient paymentClient;

    @Test
    void getPaymentWithSpringContext() {
        wireMock.stubFor(get("/payments/456")
            .willReturn(okJson("""
                {"id":"456","status":"success","amount":1000,"currency":"THB"}
                """)));

        PaymentResponse response = paymentClient.getPayment("456");

        assertThat(response.getId()).isEqualTo("456");
        wireMock.verify(1, getRequestedFor(urlEqualTo("/payments/456")));
    }
}
```

- [ ] **Step 3: รัน test**

```powershell
mvn test -Dtest=Ch09SpringBootIntegrationTest -q
```

Expected: `Tests run: 1, Failures: 0, Errors: 0`

- [ ] **Step 4: เขียน 09-spring-boot-integration.md ครบ 7 ส่วน**

รวม gotchas:
- `@WireMockTest` auto-resets stubs vs WireMockServer ไม่ auto-reset
- property injection ผ่าน `@ConfigureWireMock(property = "...")`
- Jetty conflict (และวิธีแก้)

- [ ] **Step 5: Commit**

```bash
git add docs/wiremock-java/09-spring-boot-integration.md docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch09SpringBootIntegrationTest.java
git commit -m "feat: add WireMock ch09 - Spring Boot integration"
```

---

## Task 16: บทที่ 10 — Error Simulation

**Files:**
- Create: `docs/wiremock-java/10-error-simulation.md`
- Create: `docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch10ErrorSimulationTest.java`

- [ ] **Step 1: WebFetch fault simulation docs**

```
WebFetch: https://wiremock.org/docs/simulating-faults/
```

บันทึก quote สำหรับ `withStatus(500)`, `withFixedDelay(millis)`, `withFault(Fault.CONNECTION_RESET_BY_PEER)`

- [ ] **Step 2: สร้าง verified test (ใช้ Spring Boot context จาก ch09)**

ครอบคลุม:
- 404 Not Found → client ต้อง handle gracefully
- 500 Internal Server Error → client retry/throw
- Network delay `withFixedDelay(3000)` → test timeout handling

- [ ] **Step 3: รัน test**

```powershell
mvn test -Dtest=Ch10ErrorSimulationTest -q
```

- [ ] **Step 4: เขียน 10-error-simulation.md ครบ 7 ส่วน**

- [ ] **Step 5: Commit**

```bash
git add docs/wiremock-java/10-error-simulation.md docs/wiremock-java/sample-project/src/test/java/com/example/wiremock/Ch10ErrorSimulationTest.java
git commit -m "feat: add WireMock ch10 - error simulation"
```

---

## Task 17: บทที่ 11 — Best Practices + Common Mistakes

**Files:**
- Create: `docs/wiremock-java/11-best-practices.md`

- [ ] **Step 1: เขียน 11-best-practices.md ครบ 7 ส่วน**

รวมทุก gotchas จาก expert review:
- ❌ Hardcode port → ✅ dynamicPort()
- ❌ ลืม resetAll() → ✅ @AfterEach
- ❌ stubFor ไม่มี verify → ✅ ทำคู่กันเสมอ
- ❌ urlEqualTo กับ query string → ✅ urlPathEqualTo + withQueryParam
- ❌ WireMock 2.x artifact (com.github.tomakehurst) → ✅ org.wiremock artifact
- ❌ Client URL ชี้ผิด → ✅ inject port จาก wireMock.port()
- Production checklist: test ทุก error path, ใช้ dynamicPort เสมอ, reset ทุก test

- [ ] **Step 2: Commit**

```bash
git add docs/wiremock-java/11-best-practices.md
git commit -m "feat: add WireMock ch11 - best practices and common mistakes"
```

---

## Task 18: exercises.md

**Files:**
- Create: `docs/wiremock-java/exercises.md`

- [ ] **Step 1: เขียน exercises.md**

≥3 ข้อต่อ concept หลัก (5 concepts = 15+ exercises) ครบ 3 ระดับ:
- **Beginner (Recall):** อธิบายด้วยคำตัวเอง / ยกตัวอย่าง
- **Intermediate (Application):** สถานการณ์ใหม่ที่ไม่มีใน examples — ห้าม copy context เดิม
- **Advanced (Synthesis):** ออกแบบ test suite, หา bug ใน code ที่ให้มา, เปรียบเทียบ approach

ทุก exercise มีเฉลย (แสดงหลัง `<details>` tag)

- [ ] **Step 2: Verify exercise code รันได้**

ทุก code ใน exercises.md ที่เป็น Java ต้อง verify ด้วย `mvn test` ก่อนใส่

- [ ] **Step 3: Commit**

```bash
git add docs/wiremock-java/exercises.md
git commit -m "feat: add WireMock course exercises"
```

---

## Task 19: glossary.md

**Files:**
- Create: `docs/wiremock-java/glossary.md`

- [ ] **Step 1: เขียน glossary.md**

ทุกคำศัพท์ต้องมี:
- ชื่อ term (English)
- คำอธิบายภาษาไทย (1-3 ประโยค)
- SOURCE URL จาก official docs

คำที่ต้องมี: WireMock, Stub, Mock, Spy, Verify, Request Matching, Response Templating, Scenario, Fault Simulation, WireMockServer, @WireMockTest, @EnableWireMock, dynamicPort, urlEqualTo, urlPathEqualTo, urlMatching, equalToJson, withFixedDelay

- [ ] **Step 2: Commit**

```bash
git add docs/wiremock-java/glossary.md
git commit -m "feat: add WireMock course glossary"
```

---

## Task 20: Final Review + Run All Tests

- [ ] **Step 1: รัน test suite ทั้งหมด**

```powershell
cd docs/wiremock-java/sample-project
mvn test
```

Expected: `Tests run: X, Failures: 0, Errors: 0`

- [ ] **Step 2: ตรวจ overview links ทั้งหมด**

ทุก link ใน `00-overview.md` ต้อง resolve ได้

- [ ] **Step 3: ตรวจ animations ทั้ง 4 ไฟล์**

เปิดแต่ละไฟล์ใน browser และตรวจว่า animate ได้ถูกต้อง

- [ ] **Step 4: Update MEMORY.md**

เพิ่มรายการ WireMock course ลง MEMORY.md ตาม format ที่กำหนด

- [ ] **Step 5: Final commit**

```bash
git add .
git commit -m "feat: complete WireMock Java course - 11 chapters + 4 animations"
```
