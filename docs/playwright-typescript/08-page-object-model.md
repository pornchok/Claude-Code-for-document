## ก่อนอ่านบทนี้ ลองตอบ:

1. ทำไม Playwright fixtures ถึงดีกว่า `beforeEach`/`afterEach` ธรรมดา — ระบุข้อแตกต่างที่สำคัญที่สุด 2 ข้อ?
2. ถ้าคุณมี fixture ที่ใช้ `auto: true` กับ scope เป็น `worker` — fixture นั้นจะรัน setup กี่ครั้งถ้า test suite มี 10 tests แบ่งเป็น 2 workers?

---

เฉลย:

1. ข้อแตกต่างสำคัญ: (1) **Lazy initialization** — fixture สร้างเฉพาะเมื่อ test ประกาศว่าต้องการ ไม่รันโดยไม่จำเป็น (2) **Teardown รันเสมอ** — แม้ test จะ fail กลางทาง การเขียน setup และ teardown ไว้ใน fixture function เดียวกัน (ก่อน/หลัง `use()`) รับประกัน cleanup ทุกครั้ง
2. Setup รัน **2 ครั้ง** — worker-scoped fixture share ได้เฉพาะ tests ในกระบวนการ worker เดียวกัน ถ้ามี 2 workers แต่ละ worker จะ setup fixture ของตัวเองหนึ่งครั้ง รวมเป็น 2 ครั้ง

# บทที่ 8: Page Object Model — จัดการ Locator อย่างมีระบบ

## 1. วัตถุประสงค์

หลังอ่านบทนี้คุณจะ:

- อธิบายได้ว่า Page Object Model (POM) แก้ปัญหาอะไรที่ทำให้ test suite บำรุงรักษายาก
- สร้าง Page Object class ด้วย TypeScript ที่มี `readonly` Locator properties และ async action methods
- เข้าใจความต่างระหว่าง **Page Object** (ทั้งหน้า) กับ **Component Object** (ส่วนประกอบย่อย เช่น NavBar)
- ออกแบบ multi-page workflow โดยให้ method คืน Page Object instance แทน string URL
- ใช้ **Composition แทน Inheritance** ในการประกอบ Page Objects เข้าด้วยกัน
- รวม POM เข้ากับ Playwright fixtures เพื่อให้ tests อ่านง่ายและ reuse ได้
- ระบุ anti-patterns ที่พบบ่อยใน POM และแก้ให้ถูกต้อง

---

## 2. ทำไมต้องรู้? (Why)

สมมติคุณมี test suite ขนาดกลาง 30 tests ที่ login ก่อนทำงาน และ UI designer เพิ่งเปลี่ยน login button จาก `[data-testid="btn-submit"]` เป็น `[data-testid="btn-login"]`

ถ้าคุณเขียน locator กระจายอยู่ใน test file ทุกไฟล์แบบนี้:

```typescript
// test-1.spec.ts
await page.locator('[data-testid="btn-submit"]').click();

// test-2.spec.ts
await page.locator('[data-testid="btn-submit"]').click();

// test-15.spec.ts
await page.locator('[data-testid="btn-submit"]').click();
```

การเปลี่ยน selector ครั้งเดียวกลายเป็นงาน **Find & Replace ใน 30 ไฟล์** และถ้าพลาดไฟล์ใดไฟล์หนึ่ง test จะ fail โดยไม่บอกเหตุผลที่ชัดเจน

ปัญหายิ่งหนักขึ้นเมื่อมีการทำ action ซ้ำ เช่น ลำดับ fill username → fill password → click login ที่ปรากฏใน test ทุกตัว ถ้า login flow เปลี่ยน (เพิ่ม CAPTCHA, เพิ่ม 2FA field) ต้องแก้ทุกที่เหมือนกัน

**Page Object Model (POM)** คือ design pattern ที่รวม locators และ actions ที่เกี่ยวกับหน้าหนึ่งๆ ไว้ใน class เดียว — เปลี่ยนที่เดียว แก้ทุก test โดยอัตโนมัติ

---

## 3. เนื้อหาหลัก

### 3.1 ปัญหาที่ POM แก้ได้

ก่อนจะเขียน POM ต้องเข้าใจก่อนว่ามันแก้ปัญหาสามข้อที่เกิดใน test suite ขนาดใหญ่:

**ปัญหาที่ 1 — Locator กระจัดกระจาย:**
Selector เดียวกันปรากฏใน test หลายไฟล์ เมื่อ HTML เปลี่ยนต้องตามแก้ทุกที่

**ปัญหาที่ 2 — Action ซ้ำ:**
ลำดับ actions เดียวกัน (เช่น login flow) copy-paste อยู่ในหลาย test โดยไม่มีที่รวม

**ปัญหาที่ 3 — Test อ่านยาก:**
`page.locator('[data-testid="btn-login"]').click()` อ่านยากกว่า `loginPage.submit()` และ test code ที่ดีควรสื่อ **intent** ไม่ใช่ **implementation**

*(source: https://playwright.dev/docs/pom — "Page objects simplify authoring by creating a higher-level API which suits your application and simplify maintenance by capturing element selectors in one place")*

---

### 3.2 โครงสร้าง Page Object ใน TypeScript

Page Object ที่ดีมี 3 ส่วน: constructor รับ `Page`, Locator properties, และ action methods

```typescript
// partial example — see Section 5 for runnable version
// pages/login.page.ts
import { type Page, type Locator } from '@playwright/test';

export class LoginPage {
  // ① Constructor injection — รับ page จาก test
  constructor(private readonly page: Page) {}

  // ② Locator properties — เป็น readonly และประกาศระดับ class
  readonly usernameInput: Locator = this.page.getByLabel('Username');
  readonly passwordInput: Locator = this.page.getByLabel('Password');
  readonly loginButton: Locator  = this.page.getByRole('button', { name: 'Login' });
  readonly errorMessage: Locator = this.page.getByTestId('login-error');

  // ③ Action methods — เป็น async และสื่อ business intent
  async goto() {
    await this.page.goto('/login');
  }

  async login(username: string, password: string) {
    await this.usernameInput.fill(username);
    await this.passwordInput.fill(password);
    await this.loginButton.click();
  }
}
```

**ทำไม Locator เป็น property ไม่ใช่ method:**
Playwright Locator เป็น "lazy reference" — ไม่ได้ query DOM ทันทีที่สร้าง แต่จะ query เมื่อถูกเรียกใช้จริง (เช่น `.click()`, `.fill()`) ดังนั้นการประกาศเป็น property ระดับ class ไม่มีผลด้านประสิทธิภาพ และทำให้ใช้ใน test ได้สะดวกโดยตรง (`loginPage.loginButton`) โดยไม่ต้องเรียก method ก่อน

**ทำไมใช้ `private readonly page` แทน `public page`:**
`private` ป้องกัน test file เข้าถึง `page` โดยตรง บังคับให้ใช้ผ่าน methods ของ Page Object เท่านั้น TypeScript จะ error ทันทีถ้า test พยายาม `loginPage.page.goto(...)` โดยตรง

---

### 3.3 Multi-Page Workflow — คืน Page Object แทน URL

เมื่อ action หนึ่งนำไปสู่อีกหน้าหนึ่ง (เช่น login สำเร็จ → redirect ไป todos) ให้ method คืน Page Object ของปลายทาง

```typescript
// partial example — see Section 5 for runnable version
// pages/login.page.ts
import { type Page } from '@playwright/test';
import { TodosPage } from './todos.page';

export class LoginPage {
  constructor(private readonly page: Page) {}

  readonly usernameInput = this.page.getByLabel('Username');
  readonly passwordInput = this.page.getByLabel('Password');
  readonly loginButton   = this.page.getByRole('button', { name: 'Login' });
  readonly errorMessage  = this.page.getByTestId('login-error');

  async goto() {
    await this.page.goto('/login');
  }

  async login(username: string, password: string) {
    await this.usernameInput.fill(username);
    await this.passwordInput.fill(password);
    await this.loginButton.click();
  }

  // Method ที่รู้ว่าจะ navigate ไปหน้าไหน — คืน Page Object ปลายทาง
  async loginAsAdmin(): Promise<TodosPage> {
    await this.login('admin', 'admin123');
    return new TodosPage(this.page);
  }
}
```

```typescript
// partial example — see Section 5 for runnable version
// pages/todos.page.ts
import { type Page, type Locator } from '@playwright/test';

export class TodosPage {
  constructor(private readonly page: Page) {}

  readonly todoInput  = this.page.getByTestId('input-new-todo');
  readonly addButton  = this.page.getByTestId('btn-add-todo');
  readonly todoList   = this.page.getByTestId('todo-list');
  readonly todoCount  = this.page.getByTestId('todo-count');

  async goto() {
    await this.page.goto('/todos');
  }

  async addTodo(text: string) {
    await this.todoInput.fill(text);
    await this.addButton.click();
  }

  getTodoItem(id: number): Locator {
    return this.page.getByTestId(`todo-item-${id}`);
  }
}
```

ข้อดีของ pattern นี้: test code ไหลลื่นแบบ chaining และ TypeScript รู้ type ถูกต้องตลอด

```typescript
// ใน test — อ่านเป็นประโยคได้
const todosPage = await loginPage.loginAsAdmin();
await todosPage.addTodo('Deploy to production');
```

---

### 3.4 Component Object Pattern

บางส่วนของ UI ปรากฏในหลายหน้า เช่น Navigation Bar ที่มี session badge — ไม่ควรเขียน locator ซ้ำในทุก Page Object

```typescript
// partial example — see Section 5 for runnable version
// pages/components/navbar.component.ts
import { type Page, type Locator } from '@playwright/test';

export class NavBarComponent {
  constructor(private readonly page: Page) {}

  readonly sessionBadge: Locator = this.page.getByTestId('session-badge');
  readonly logoutLink:   Locator = this.page.getByTestId('nav-logout');
  readonly loginLink:    Locator = this.page.getByTestId('nav-login');

  async isLoggedIn(): Promise<boolean> {
    return this.sessionBadge.isVisible();
  }

  async getLoggedInUser(): Promise<string> {
    return this.sessionBadge.textContent() ?? '';
  }

  async logout() {
    await this.logoutLink.click();
  }
}
```

แล้ว Page Objects ที่ต้องการ NavBar ก็ใช้ **Composition** — มี Component เป็น property ไม่ใช่ extend

```typescript
// partial example — see Section 5 for runnable version
// pages/todos.page.ts
import { NavBarComponent } from './components/navbar.component';

export class TodosPage {
  readonly navBar: NavBarComponent;

  constructor(private readonly page: Page) {
    this.navBar = new NavBarComponent(page);  // ← Composition
  }

  // ... Locators และ methods อื่นๆ
}
```

Test จะใช้ได้แบบนี้:

```typescript
expect(await todosPage.navBar.getLoggedInUser()).toContain('admin');
```

---

### 3.5 Composition vs Inheritance

ใน POM ให้ใช้ **Composition เสมอ** ไม่ใช่ Inheritance:

| | Composition | Inheritance |
|---|---|---|
| วิธี | `todosPage.navBar.logout()` | `class TodosPage extends NavBarComponent` |
| ข้อดี | ยืดหยุ่น, สื่อความสัมพันธ์ที่ถูกต้อง | เรียกสั้นกว่า |
| ข้อเสีย | เรียกผ่าน property | TodosPage "เป็น" NavBar? ไม่สมเหตุสมผล |
| ใช้กับ POM | ✅ แนะนำ | ❌ ไม่แนะนำ |

Page Objects ควรสื่อความหมาย "**has a**" ไม่ใช่ "**is a**" — TodosPage *มี* NavBar ไม่ใช่ TodosPage *คือ* NavBar

---

### 3.6 TypeScript Interface สำหรับ Page Objects

ในบางกรณีอาจต้องการ mock Page Object ใน unit test ให้ประกาศ interface ไว้:

```typescript
// pages/interfaces.ts
export interface ILoginPage {
  goto(): Promise<void>;
  login(username: string, password: string): Promise<void>;
  loginAsAdmin(): Promise<ITodosPage>;
  readonly errorMessage: { textContent(): Promise<string | null> };
}

export interface ITodosPage {
  addTodo(text: string): Promise<void>;
  readonly navBar: { isLoggedIn(): Promise<boolean> };
}
```

ใน test ที่ต้องการ mock สามารถสร้าง object ที่ implement interface โดยไม่ต้องใช้ Page จริง ซึ่งเหมาะกับ unit testing logic ของ test helper โดยไม่ต้องเปิด browser

---

### 3.7 รวม POM กับ Fixtures

POM และ Fixtures ทำงานร่วมกันได้ดี — ใช้ fixtures เพื่อ setup Page Object และ inject เข้า test:

```typescript
// partial example — see Section 5 for runnable version
// fixtures/pom.fixtures.ts
import { test as base } from '@playwright/test';
import { LoginPage }  from '../pages/login.page';
import { TodosPage }  from '../pages/todos.page';

type PomFixtures = {
  loginPage:  LoginPage;
  todosPage:  TodosPage;
};

export const test = base.extend<PomFixtures>({
  loginPage: async ({ page }, use) => {
    const lp = new LoginPage(page);
    await lp.goto();
    await use(lp);
    // ไม่ต้องมี teardown พิเศษ — page ถูก cleanup โดย Playwright อัตโนมัติ
  },

  todosPage: async ({ page }, use) => {
    const tp = new TodosPage(page);
    await tp.goto();
    await use(tp);
  },
});

export { expect } from '@playwright/test';
```

Test file จะสะอาดมาก:

```typescript
// tests/todo-workflow.spec.ts
import { test, expect } from '../fixtures/pom.fixtures';

test('add a todo after login', async ({ loginPage, todosPage }) => {
  await loginPage.login('admin', 'admin123');
  await todosPage.addTodo('Write POM chapter');
  await expect(todosPage.todoCount).toContainText('1');
});
```

---

### 3.8 เปรียบเทียบกับ Robot Framework

| | Robot Framework + Selenium | Playwright + TypeScript POM |
|---|---|---|
| Encapsulation | Resource file + keywords | TypeScript class |
| Type safety | ไม่มี | TypeScript full type checking |
| Locator reuse | Variable ใน Resource file | `readonly` class property |
| Multi-page flow | คืน URL string | คืน Page Object instance (type-safe) |
| Component | แยก keyword file | แยก class (NavBarComponent, etc.) |
| Fixture integration | Suite/Test Setup | `test.extend<T>()` |
| IDE support | จำกัด | Full autocomplete, go-to-definition |

---

## 4. ตัวอย่าง 3 ระดับ

### Beginner: LoginPage พื้นฐาน — สร้างและใช้งานครั้งแรก

```typescript
// tested: Playwright v1.50+, Node.js 20+
// ไฟล์: tests/login-basic.spec.ts
import { test, expect, type Page } from '@playwright/test';

// ── Page Object ──────────────────────────────────────────────
class LoginPage {
  constructor(private readonly page: Page) {}

  readonly usernameInput = this.page.getByLabel('Username');
  readonly passwordInput = this.page.getByLabel('Password');
  readonly loginButton   = this.page.getByRole('button', { name: 'Login' });
  readonly errorMessage  = this.page.getByTestId('login-error');

  async goto() {
    await this.page.goto('http://localhost:3000/login');
  }

  async login(username: string, password: string) {
    await this.usernameInput.fill(username);
    await this.passwordInput.fill(password);
    await this.loginButton.click();
  }
}

// ── Tests ─────────────────────────────────────────────────────
test.describe('Login Page', () => {
  let loginPage: LoginPage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    await loginPage.goto();
  });

  test('login สำเร็จด้วย credential ถูกต้อง', async ({ page }) => {
    await loginPage.login('admin', 'admin123');

    // assert ใน test เท่านั้น ไม่ใช่ใน Page Object
    await expect(page.getByTestId('session-badge')).toContainText('admin');
  });

  test('แสดง error เมื่อ credential ผิด', async () => {
    await loginPage.login('admin', 'wrongpassword');

    await expect(loginPage.errorMessage).toBeVisible();
    await expect(loginPage.errorMessage).toContainText('Invalid credentials');
  });

  test('แสดง error เมื่อ field ว่าง', async () => {
    await loginPage.loginButton.click();

    await expect(loginPage.errorMessage).toBeVisible();
  });
});
```

**Output ที่คาดหวัง:**
```
✓ login สำเร็จด้วย credential ถูกต้อง (1.2s)
✓ แสดง error เมื่อ credential ผิด (0.9s)
✓ แสดง error เมื่อ field ว่าง (0.7s)
```

สังเกตว่า test code ไม่มี selector เลย — ทุกการ interact ผ่าน Page Object ทั้งหมด

---

### Intermediate: Multi-Page Workflow กับ Component Object

สถานการณ์ใหม่ที่ไม่มีในตัวอย่างข้างต้น: ทดสอบ **checkout flow** — user login แล้วไปที่ todos page ตรวจสอบ badge แล้ว logout และยืนยันว่า session หายไป

```typescript
// tested: Playwright v1.50+, Node.js 20+
// ไฟล์: tests/session-workflow.spec.ts
import { test, expect, type Page } from '@playwright/test';

// ── Component Object ──────────────────────────────────────────
class NavBarComponent {
  constructor(private readonly page: Page) {}

  readonly sessionBadge = this.page.getByTestId('session-badge');
  readonly logoutLink   = this.page.getByTestId('nav-logout');

  async isLoggedIn(): Promise<boolean> {
    return this.sessionBadge.isVisible();
  }

  async getLoggedInUser(): Promise<string> {
    return (await this.sessionBadge.textContent()) ?? '';
  }

  async logout() {
    await this.logoutLink.click();
  }
}

// ── Page Objects ──────────────────────────────────────────────
class LoginPage {
  constructor(private readonly page: Page) {}

  readonly usernameInput = this.page.getByLabel('Username');
  readonly passwordInput = this.page.getByLabel('Password');
  readonly loginButton   = this.page.getByRole('button', { name: 'Login' });

  async goto() { await this.page.goto('http://localhost:3000/login'); }

  async login(username: string, password: string): Promise<TodosPage> {
    await this.usernameInput.fill(username);
    await this.passwordInput.fill(password);
    await this.loginButton.click();
    return new TodosPage(this.page);
  }
}

class TodosPage {
  readonly navBar: NavBarComponent;

  constructor(private readonly page: Page) {
    this.navBar = new NavBarComponent(page);
  }

  async goto() { await this.page.goto('http://localhost:3000/todos'); }
}

// ── Test ──────────────────────────────────────────────────────
test('session lifecycle: login → verify → logout', async ({ page }) => {
  const loginPage = new LoginPage(page);
  await loginPage.goto();

  // Multi-page: login คืน TodosPage
  const todosPage = await loginPage.login('testuser', 'test123');

  // ตรวจสอบ session ผ่าน NavBar component
  expect(await todosPage.navBar.isLoggedIn()).toBe(true);
  expect(await todosPage.navBar.getLoggedInUser()).toContain('testuser');

  // Logout
  await todosPage.navBar.logout();

  // ยืนยันว่า session หายไป
  expect(await todosPage.navBar.isLoggedIn()).toBe(false);
  await expect(page.getByTestId('nav-login')).toBeVisible();
});
```

**Output ที่คาดหวัง:**
```
✓ session lifecycle: login → verify → logout (2.1s)
```

---

### Advanced: วินิจฉัย Anti-Pattern ใน POM ที่เขียนผิด

ดู Page Object นี้และระบุว่ามีปัญหาอะไร รวมถึงอธิบาย **ทำไมมันจะทำให้ test suite มีปัญหาในระยะยาว**

```typescript
// ⚠️ Anti-pattern POM — ห้ามใช้ในโปรเจคจริง
class LoginPage {
  constructor(public page: Page) {}     // (A)

  getLoginButton() {                    // (B)
    return this.page.locator('[data-testid="btn-login"]');
  }

  async doLogin(u: string, p: string) {
    await this.page.waitForSelector('#username');   // (C)
    await this.page.fill('#username', u);           // (D)
    await this.page.fill('#password', p);
    await this.getLoginButton().click();
    await expect(this.page.getByTestId('session-badge'))  // (E)
      .toBeVisible();
  }

  async verifyWeAreOnDashboard() {      // (F)
    await expect(this.page).toHaveURL('/todos');
  }
}
```

**ปัญหาที่ต้องวินิจฉัย:**

**(A) `public page`** — ทำลาย encapsulation ทันที test สามารถเรียก `loginPage.page.goto('anywhere')` ข้ามหัว Page Object ทำให้ abstraction layer ไร้ความหมาย

**(B) `getLoginButton()` เป็น method** — ทุกครั้งที่ test เรียก `loginPage.getLoginButton()` จะสร้าง Locator object ใหม่ (แม้จะไม่ใช่ปัญหาด้าน performance แต่ API ไม่สอดคล้องกับ Playwright best practices) ควรเป็น `readonly loginButton = this.page.locator(...)` เพื่อให้ใช้โดยตรงได้

**(C) `waitForSelector`** — เป็น legacy API ที่ Playwright แนะนำให้ใช้ Locator auto-waiting แทน การเรียก `waitForSelector` ซ้อนกับ `fill` ทำให้ wait สองรอบโดยไม่จำเป็น และเพิ่ม flakiness

**(D) `page.fill` แทน Locator** — `this.page.fill('#username', u)` เป็น CSS selector ที่เปราะบาง และไม่ได้ใช้ semantic locator (getByLabel, getByRole) ที่ Playwright แนะนำ ถ้า HTML เปลี่ยน `#username` หาย test fail โดยไม่มี error message ที่ชัดเจน

**(E) `expect` ใน Page Object** — Page Object ควรเป็น **action layer เท่านั้น** การใส่ assertion ใน `doLogin()` หมายความว่า test ที่อยากทดสอบกรณี login fail จะถูก POM บล็อกก่อนด้วย assertion ที่ fail ก่อนที่ test จะได้ assert เอง

**(F) `verifyWeAreOnDashboard()`** — method ชื่อนี้บอกว่า Page Object ทำ verification แทน test ซึ่งผิดหลักการ ควรให้ test เรียก `expect(page).toHaveURL(...)` เอง ถ้าจะมี helper ให้คืน URL ปัจจุบันแทน: `async getCurrentUrl(): Promise<string>`

**✅ เวอร์ชันที่ถูกต้อง:**

```typescript
// tested: Playwright v1.50+, Node.js 20+
import { type Page } from '@playwright/test';

class LoginPage {
  constructor(private readonly page: Page) {}

  // ✅ Fix (A): private ป้องกันการ access โดยตรง
  readonly usernameInput = this.page.getByLabel('Username');
  readonly passwordInput = this.page.getByLabel('Password');
  readonly loginButton   = this.page.getByRole('button', { name: 'Login' });
  readonly errorMessage  = this.page.getByTestId('login-error');

  async goto() {
    // ✅ Fix (D): ใช้ semantic locators แทน CSS selector
    await this.page.goto('/login');
  }

  // ✅ Fix (B): Locator เป็น property ไม่ใช่ method
  async login(username: string, password: string) {
    // ✅ Fix (C): Locator มี auto-waiting ไม่ต้อง waitForSelector
    await this.usernameInput.fill(username);
    await this.passwordInput.fill(password);
    await this.loginButton.click();
    // ✅ Fix (E): ไม่มี assertion ใน Page Object method
  }

  // ✅ Fix (F): ไม่มี verify method — ให้ test เป็นผู้ assert
}

// ใน test:
// await loginPage.goto();
// await loginPage.login('admin', 'admin123');
// expect(page.getByTestId('session-badge')).toBeVisible();
// expect(page).toHaveURL('/todos');
```

---

## 5. Common Mistakes

**❌ Locator เป็น method แทนที่จะเป็น property:**
```typescript
// ❌ ผิด
getLoginButton() {
  return this.page.locator('[data-testid="btn-login"]');
}

// ✅ ถูก
readonly loginButton = this.page.getByTestId('btn-login');
```
เหตุผล: Locator เป็น lazy reference อยู่แล้ว ไม่มีเหตุผลต้อง wrap ใน method — ทำให้ API ใช้ยากขึ้นโดยไม่จำเป็น *(source: https://playwright.dev/docs/pom)*

---

**❌ Assert ใน Page Object method:**
```typescript
// ❌ ผิด
async login(u: string, p: string) {
  await this.usernameInput.fill(u);
  await this.passwordInput.fill(p);
  await this.loginButton.click();
  await expect(this.page.getByTestId('session-badge')).toBeVisible(); // ← assertion ใน POM
}

// ✅ ถูก — assertion อยู่ใน test เท่านั้น
async login(u: string, p: string) {
  await this.usernameInput.fill(u);
  await this.passwordInput.fill(p);
  await this.loginButton.click();
}

// ใน test:
await loginPage.login('admin', 'admin123');
await expect(page.getByTestId('session-badge')).toBeVisible();
```
เหตุผล: Page Object ที่มี assertion จะทำให้ test ที่ต้องการ test กรณี failure ทำงานไม่ได้ — POM เป็น action layer ไม่ใช่ assertion layer *(source: https://playwright.dev/docs/pom)*

---

**❌ Hardcode URL ใน POM:**
```typescript
// ❌ ผิด
async goto() {
  await this.page.goto('http://localhost:3000/login');  // hardcode
}

// ✅ ถูก — ใช้ baseURL จาก config
async goto() {
  await this.page.goto('/login');  // relative URL → ใช้ baseURL อัตโนมัติ
}
```
เหตุผล: เมื่อ deploy ไป staging หรือ production URL เปลี่ยน ต้องแก้ใน POM ทุกตัวแทนที่จะแก้แค่ `baseURL` ใน playwright.config.ts *(source: https://playwright.dev/docs/test-configuration)*

---

**❌ POM extends POM อื่น (Inheritance):**
```typescript
// ❌ ผิด
class TodosPage extends NavBarComponent { ... }  // TodosPage "เป็น" NavBar?

// ✅ ถูก — Composition
class TodosPage {
  readonly navBar: NavBarComponent;
  constructor(private readonly page: Page) {
    this.navBar = new NavBarComponent(page);
  }
}
```
เหตุผล: Page Objects สื่อความสัมพันธ์ "has-a" ไม่ใช่ "is-a" Inheritance ทำให้ class hierarchy ซับซ้อนและทดสอบยากขึ้น *(source: https://playwright.dev/docs/pom)*

---

**❌ ใช้ legacy `waitForSelector` / `page.fill` ใน POM:**
```typescript
// ❌ ผิด
async fillForm(u: string, p: string) {
  await this.page.waitForSelector('#username');
  await this.page.fill('#username', u);
}

// ✅ ถูก — Locator auto-waiting จัดการให้
async fillForm(u: string, p: string) {
  await this.usernameInput.fill(u);
}
```
เหตุผล: Playwright Locators มี auto-waiting built-in — `fill()` จะรอให้ element พร้อมก่อนอัตโนมัติ การเรียก `waitForSelector` ซ้ำซ้อนและเพิ่ม flakiness *(source: https://playwright.dev/docs/locators)*

---

## 6. สรุปบท

POM คือ design pattern ที่รวม locators และ actions ที่เกี่ยวกับหน้า (หรือ component) ไว้ใน TypeScript class เดียว แก้ปัญหา locator กระจาย, action ซ้ำ, และ test อ่านยาก

Pattern ที่สำคัญที่สุด:
- Locator เป็น `readonly` class property — ไม่ใช่ method
- Method เป็น async action — ไม่มี assertion
- Multi-page flow คืน Page Object instance — ไม่ใช่ string
- Component Objects ใช้ Composition — ไม่ใช่ Inheritance
- Fixtures เป็นตัวกลางระหว่าง POM กับ test — inject Page Object เข้า test โดยตรง

---

**คำถาม Retrieval — ลองตอบก่อนดูเฉลย:**

1. ทำไม assertion ถึงไม่ควรอยู่ใน Page Object method — อธิบายด้วยตัวอย่างสถานการณ์ที่จะเกิดปัญหา?
2. ถ้า `NavBarComponent` ปรากฏในหน้า `/todos` และ `/shop` ควรออกแบบโครงสร้างอย่างไร — ใช้ Inheritance หรือ Composition และทำไม?
3. เมื่อไหรควรสร้าง TypeScript interface สำหรับ Page Object แทนที่จะใช้ class โดยตรง?

---

<details>
<summary>เฉลย (คลิกเพื่อดู)</summary>

**1. Assertion ใน Page Object:**
สถานการณ์ปัญหา: สมมติ `loginPage.doLogin()` มี `expect(sessionBadge).toBeVisible()` อยู่ข้างใน ถ้าต้องการ test กรณี login fail (credential ผิด) test จะถูก `expect` ใน POM throw assertion error ก่อนที่ test จะได้ตรวจสอบ `errorMessage` เลย — test ไม่สามารถทดสอบ failure path ได้ Page Object ควรเป็นเพียง action layer และให้ test เป็นผู้ตัดสินใจว่าจะ assert อะไร

**2. NavBar ใน หลายหน้า:**
ใช้ **Composition** — สร้าง `NavBarComponent` class แล้วให้ `TodosPage` และ `ShopPage` มี property `readonly navBar: NavBarComponent` ใน constructor ของแต่ละ Page Object Inheritance (`TodosPage extends NavBarComponent`) ไม่ถูกเพราะ TodosPage ไม่ใช่ NavBar — มีแค่ "มี" NavBar เท่านั้น

**3. เมื่อควรใช้ Interface:**
ควรใช้ interface เมื่อต้องการ mock Page Object ใน unit test ที่ไม่ต้องการ browser จริง เช่น ทดสอบ business logic ของ test helper function ที่รับ Page Object เป็น parameter interface ทำให้สร้าง lightweight mock ได้โดยไม่ต้อง instantiate class จริงที่ต้องการ `Page` object

</details>
