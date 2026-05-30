# Playwright Course Demo App — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build `playwright-course-app/` — a full-featured Node.js/Express web application with UI pages and REST/WebSocket API that supports all 18 chapters of the Playwright TypeScript Master Course.

**Architecture:** Single Express.js server (server.js) serves both REST API (`/api/*`) and static HTML pages from `public/`. JSON file (`data/db.json`) is the database — no DB setup needed. JWT auth applies only to protected endpoints. WebSocket server runs alongside HTTP on the same port.

**Tech Stack:** Node.js 20+, Express 4.x, jsonwebtoken 9.x, cors 2.x, ws 8.x, Vanilla HTML5/CSS3/JS

---

## File Map

| File | Responsibility |
|------|---------------|
| `package.json` | Dependencies and npm start script |
| `server.js` | Express app + all routes + WebSocket server |
| `middleware/auth.js` | JWT verify middleware (protected routes only) |
| `data/db.json` | JSON database: users, products, todos, orders |
| `public/style.css` | Shared CSS — light/dark theme, layout, components |
| `public/client.js` | Shared frontend JS — JWT storage, session badge, fetch wrapper |
| `public/index.html` | Dashboard (session badge, nav links) |
| `public/login.html` | Login form |
| `public/todos.html` | Todo CRUD, double-click edit, count badge, drag-drop |
| `public/shop.html` | Product catalog — filter, search, pagination |
| `public/cart.html` | Shopping cart |
| `public/checkout.html` | Multi-step checkout wizard (3 steps) |
| `public/components.html` | UI kitchen sink — all element types |
| `public/advanced.html` | Popup, iframe, file upload, dialogs, Shadow DOM, download |
| `public/visual.html` | Theme switcher for visual testing |
| `public/admin.html` | Admin-only dashboard |
| `README.md` | Setup instructions + endpoint reference |

---

### Task 1: Project Setup

**Files:**
- Create: `playwright-course-app/package.json`
- Create dirs: `playwright-course-app/data/`, `middleware/`, `public/`

- [ ] **Step 1: สร้าง directory และ package.json**

```bash
mkdir playwright-course-app
cd playwright-course-app
mkdir -p data middleware public
```

สร้าง `package.json`:
```json
{
  "name": "playwright-course-app",
  "version": "1.0.0",
  "description": "Demo app for Playwright TypeScript Master Course",
  "main": "server.js",
  "scripts": {
    "start": "node server.js",
    "dev": "node --watch server.js"
  },
  "dependencies": {
    "cors": "^2.8.5",
    "express": "^4.18.2",
    "jsonwebtoken": "^9.0.2",
    "ws": "^8.16.0"
  }
}
```

- [ ] **Step 2: ติดตั้ง dependencies**

```bash
npm install
```

Expected: `added N packages, found 0 vulnerabilities`

- [ ] **Step 3: Commit**

```bash
git add package.json package-lock.json
git commit -m "feat: init playwright-course-app"
```

---

### Task 2: Initial Database

**Files:**
- Create: `playwright-course-app/data/db.json`

- [ ] **Step 1: สร้าง db.json**

```json
{
  "users": [
    { "id": 1, "username": "admin", "password": "admin123", "role": "admin" },
    { "id": 2, "username": "testuser", "password": "test123", "role": "user" }
  ],
  "products": [
    { "id": 1, "name": "iPhone 15 Pro", "price": 999, "category": "Electronics", "description": "Apple flagship smartphone with titanium design", "image": "https://placehold.co/300x200?text=iPhone+15+Pro" },
    { "id": 2, "name": "MacBook Air M3", "price": 1299, "category": "Electronics", "description": "Ultra-thin laptop with Apple M3 chip", "image": "https://placehold.co/300x200?text=MacBook+Air" },
    { "id": 3, "name": "AirPods Pro", "price": 249, "category": "Electronics", "description": "Active noise cancellation earbuds", "image": "https://placehold.co/300x200?text=AirPods+Pro" },
    { "id": 4, "name": "iPad mini", "price": 499, "category": "Electronics", "description": "Compact tablet with A15 Bionic chip", "image": "https://placehold.co/300x200?text=iPad+mini" },
    { "id": 5, "name": "Clean Code", "price": 45, "category": "Books", "description": "A handbook of agile software craftsmanship by Robert C. Martin", "image": "https://placehold.co/300x200?text=Clean+Code" },
    { "id": 6, "name": "The Pragmatic Programmer", "price": 50, "category": "Books", "description": "Your journey to mastery by David Thomas and Andrew Hunt", "image": "https://placehold.co/300x200?text=Pragmatic+Programmer" },
    { "id": 7, "name": "Design Patterns", "price": 55, "category": "Books", "description": "Elements of Reusable Object-Oriented Software (GoF)", "image": "https://placehold.co/300x200?text=Design+Patterns" },
    { "id": 8, "name": "Classic T-Shirt", "price": 25, "category": "Clothing", "description": "100% cotton comfortable everyday t-shirt", "image": "https://placehold.co/300x200?text=T-Shirt" },
    { "id": 9, "name": "Tech Hoodie", "price": 65, "category": "Clothing", "description": "Warm fleece hoodie with front pocket", "image": "https://placehold.co/300x200?text=Hoodie" },
    { "id": 10, "name": "Canvas Sneakers", "price": 80, "category": "Clothing", "description": "Lightweight canvas shoes for everyday wear", "image": "https://placehold.co/300x200?text=Sneakers" }
  ],
  "todos": [],
  "orders": []
}
```

- [ ] **Step 2: Commit**

```bash
git add data/db.json
git commit -m "feat: add initial database with 10 products and 2 users"
```

---

### Task 3: Auth Middleware

**Files:**
- Create: `playwright-course-app/middleware/auth.js`

- [ ] **Step 1: สร้าง middleware/auth.js**

```javascript
const jwt = require('jsonwebtoken');

const JWT_SECRET = 'playwright-course-secret-2024';

function requireAuth(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];
  if (!token) return res.status(401).json({ error: 'Access token required' });
  try {
    req.user = jwt.verify(token, JWT_SECRET);
    next();
  } catch {
    return res.status(401).json({ error: 'Invalid or expired token' });
  }
}

function requireAdmin(req, res, next) {
  if (!req.user || req.user.role !== 'admin') {
    return res.status(403).json({ error: 'Admin access required' });
  }
  next();
}

module.exports = { requireAuth, requireAdmin, JWT_SECRET };
```

- [ ] **Step 2: Commit**

```bash
git add middleware/auth.js
git commit -m "feat: add JWT auth middleware"
```

---

### Task 4: Express Server + Auth API

**Files:**
- Create: `playwright-course-app/server.js`

- [ ] **Step 1: สร้าง server.js พร้อม auth routes**

```javascript
const express = require('express');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const fs = require('fs');
const path = require('path');
const { requireAuth, requireAdmin, JWT_SECRET } = require('./middleware/auth');

const app = express();
const PORT = 3000;

app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// ── DB helpers ────────────────────────────────────────────────
const DB_PATH = path.join(__dirname, 'data', 'db.json');
function readDb() { return JSON.parse(fs.readFileSync(DB_PATH, 'utf-8')); }
function writeDb(data) { fs.writeFileSync(DB_PATH, JSON.stringify(data, null, 2)); }

// ── Auth ──────────────────────────────────────────────────────
app.post('/api/auth/login', (req, res) => {
  const { username, password } = req.body || {};
  if (!username || !password) return res.status(400).json({ error: 'Username and password required' });
  const db = readDb();
  const user = db.users.find(u => u.username === username && u.password === password);
  if (!user) return res.status(401).json({ error: 'Invalid credentials' });
  const token = jwt.sign({ id: user.id, username: user.username, role: user.role }, JWT_SECRET, { expiresIn: '24h' });
  res.json({ token });
});

app.get('/api/me', requireAuth, (req, res) => {
  res.json({ id: req.user.id, username: req.user.username, role: req.user.role });
});

// ── Todos (public) ───────────────────────────────────────────
let nextTodoId = 1;

app.get('/api/todos', (req, res) => {
  res.json(readDb().todos);
});

app.post('/api/todos', (req, res) => {
  const { text } = req.body || {};
  if (!text || !text.trim()) return res.status(400).json({ error: 'text is required' });
  const db = readDb();
  const todo = { id: nextTodoId++, text: text.trim(), completed: false, createdAt: new Date().toISOString() };
  db.todos.push(todo);
  writeDb(db);
  // Notify WebSocket clients (wss defined later)
  if (global.wss) broadcast({ type: 'todo_updated', data: todo });
  res.status(201).json(todo);
});

app.patch('/api/todos/:id', (req, res) => {
  const db = readDb();
  const idx = db.todos.findIndex(t => t.id === Number(req.params.id));
  if (idx === -1) return res.status(404).json({ error: 'Todo not found' });
  Object.assign(db.todos[idx], req.body);
  writeDb(db);
  if (global.wss) broadcast({ type: 'todo_updated', data: db.todos[idx] });
  res.json(db.todos[idx]);
});

app.delete('/api/todos/:id', (req, res) => {
  const db = readDb();
  const idx = db.todos.findIndex(t => t.id === Number(req.params.id));
  if (idx === -1) return res.status(404).json({ error: 'Todo not found' });
  db.todos.splice(idx, 1);
  writeDb(db);
  res.json({ success: true });
});

app.post('/api/reset', (req, res) => {
  const db = readDb();
  db.todos = [];
  writeDb(db);
  nextTodoId = 1;
  res.json({ success: true });
});

// ── Products (public) ────────────────────────────────────────
app.get('/api/products', (req, res) => {
  const db = readDb();
  let products = [...db.products];
  const { search, category, page = '1', limit = '5' } = req.query;
  if (search) products = products.filter(p => p.name.toLowerCase().includes(search.toLowerCase()));
  if (category) products = products.filter(p => p.category === category);
  const pageNum = Math.max(1, parseInt(page));
  const limitNum = Math.min(20, Math.max(1, parseInt(limit)));
  const total = products.length;
  const data = products.slice((pageNum - 1) * limitNum, pageNum * limitNum);
  res.json({ data, total, page: pageNum, limit: limitNum, totalPages: Math.ceil(total / limitNum) });
});

app.get('/api/products/:id', (req, res) => {
  const db = readDb();
  const product = db.products.find(p => p.id === Number(req.params.id));
  if (!product) return res.status(404).json({ error: 'Product not found' });
  res.json(product);
});

// ── Orders (protected) ───────────────────────────────────────
app.post('/api/orders', requireAuth, (req, res) => {
  const db = readDb();
  const order = { orderId: `ORD-${Date.now()}`, status: 'confirmed', items: req.body.items || [], createdAt: new Date().toISOString() };
  db.orders.push(order);
  writeDb(db);
  res.status(201).json(order);
});

// ── Admin (protected + admin role) ───────────────────────────
app.get('/api/admin', requireAuth, requireAdmin, (req, res) => {
  const db = readDb();
  res.json({ stats: { users: db.users.length, products: db.products.length, todos: db.todos.length, orders: db.orders.length } });
});

// ── Utility endpoints (public) ───────────────────────────────
app.get('/api/slow', async (req, res) => {
  const delay = Math.min(10000, Math.max(0, parseInt(req.query.delay) || 2000));
  await new Promise(r => setTimeout(r, delay));
  res.json({ message: `slow response after ${delay}ms` });
});

app.get('/api/error', (req, res) => {
  res.status(500).json({ error: 'Internal Server Error' });
});

app.get('/api/flaky', (req, res) => {
  if (Math.random() < 0.5) return res.status(500).json({ error: 'Flaky failure' });
  res.json({ message: 'ok' });
});

app.get('/api/export', (req, res) => {
  const db = readDb();
  const csv = ['id,text,completed,createdAt', ...db.todos.map(t => `${t.id},"${t.text}",${t.completed},${t.createdAt}`)].join('\n');
  res.setHeader('Content-Type', 'text/csv');
  res.setHeader('Content-Disposition', 'attachment; filename="todos.csv"');
  res.send(csv);
});

// ── File upload ───────────────────────────────────────────────
app.post('/api/upload', (req, res) => {
  res.json({ success: true, message: 'File received' });
});

// ── SPA fallback ──────────────────────────────────────────────
app.get('*', (req, res) => {
  if (req.path.startsWith('/api')) return res.status(404).json({ error: 'Not found' });
  const page = req.path.slice(1).replace(/\/$/, '') || 'index';
  const filePath = path.join(__dirname, 'public', `${page}.html`);
  res.sendFile(fs.existsSync(filePath) ? filePath : path.join(__dirname, 'public', 'index.html'));
});

// ── WebSocket ─────────────────────────────────────────────────
const { WebSocketServer } = require('ws');
const http = require('http');
const server = http.createServer(app);
const wss = new WebSocketServer({ server });
global.wss = wss;

function broadcast(data) {
  const msg = JSON.stringify(data);
  wss.clients.forEach(client => { if (client.readyState === 1) client.send(msg); });
}

wss.on('connection', ws => {
  ws.send(JSON.stringify({ type: 'connected', message: 'Welcome to Playwright Course App WebSocket' }));
});

// Ping every 3 seconds
setInterval(() => broadcast({ type: 'ping', timestamp: new Date().toISOString() }), 3000);

server.listen(PORT, () => console.log(`Playwright Course App running at http://localhost:${PORT}`));
```

- [ ] **Step 2: ทดสอบ server**

```bash
node server.js &
SERVER_PID=$!

# Test login
curl -s -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}'
# Expected: {"token":"<jwt>"}

# Test products
curl -s "http://localhost:3000/api/products?page=1&limit=5"
# Expected: {"data":[...],"total":10,"page":1,"limit":5,"totalPages":2}

# Test slow
curl -s "http://localhost:3000/api/slow?delay=100"
# Expected: {"message":"slow response after 100ms"}

kill $SERVER_PID
```

- [ ] **Step 3: Commit**

```bash
git add server.js
git commit -m "feat: add all API routes and WebSocket server"
```

---

### Task 5: Shared CSS

**Files:**
- Create: `playwright-course-app/public/style.css`

- [ ] **Step 1: สร้าง style.css**

```css
/* ── Reset & Variables ──────────────────────────────────── */
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

:root {
  --bg: #ffffff; --bg-secondary: #f5f5f5; --text: #1a1a1a; --text-muted: #666;
  --border: #ddd; --primary: #2563eb; --primary-hover: #1d4ed8;
  --danger: #dc2626; --success: #16a34a; --card-bg: #fff; --shadow: 0 1px 3px rgba(0,0,0,.1);
}
[data-theme="dark"] {
  --bg: #1a1a1a; --bg-secondary: #2d2d2d; --text: #f0f0f0; --text-muted: #999;
  --border: #444; --card-bg: #2d2d2d; --shadow: 0 1px 3px rgba(0,0,0,.4);
}

body { font-family: system-ui, sans-serif; background: var(--bg); color: var(--text); line-height: 1.5; }

/* ── Navbar ────────────────────────────────────────────── */
nav { background: var(--primary); padding: .75rem 1.5rem; display: flex; align-items: center; gap: 1rem; flex-wrap: wrap; }
nav a { color: #fff; text-decoration: none; font-weight: 500; }
nav a:hover { text-decoration: underline; }
.nav-brand { font-weight: 700; font-size: 1.1rem; margin-right: auto; }
#session-badge { color: rgba(255,255,255,.85); font-size: .875rem; }

/* ── Layout ────────────────────────────────────────────── */
main { max-width: 1100px; margin: 0 auto; padding: 2rem 1rem; }
h1 { font-size: 1.75rem; margin-bottom: 1.5rem; }
h2 { font-size: 1.25rem; margin-bottom: 1rem; }

/* ── Buttons ───────────────────────────────────────────── */
.btn { display: inline-flex; align-items: center; gap: .4rem; padding: .5rem 1rem; border: none; border-radius: .375rem; cursor: pointer; font-size: .9rem; font-weight: 500; transition: background .15s; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-primary:hover { background: var(--primary-hover); }
.btn-danger { background: var(--danger); color: #fff; }
.btn-danger:hover { background: #b91c1c; }
.btn-secondary { background: var(--bg-secondary); color: var(--text); border: 1px solid var(--border); }
.btn-sm { padding: .25rem .6rem; font-size: .8rem; }

/* ── Forms ─────────────────────────────────────────────── */
.form-group { margin-bottom: 1rem; }
label { display: block; font-weight: 500; margin-bottom: .35rem; font-size: .9rem; }
input, select, textarea { width: 100%; padding: .5rem .75rem; border: 1px solid var(--border); border-radius: .375rem; background: var(--bg); color: var(--text); font-size: .9rem; }
input:focus, select:focus { outline: 2px solid var(--primary); outline-offset: 1px; }
.error-msg { color: var(--danger); font-size: .85rem; margin-top: .35rem; display: none; }
.error-msg.visible { display: block; }

/* ── Cards ─────────────────────────────────────────────── */
.card { background: var(--card-bg); border: 1px solid var(--border); border-radius: .5rem; padding: 1.25rem; box-shadow: var(--shadow); }
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 1rem; }
.product-card img { width: 100%; height: 160px; object-fit: cover; border-radius: .375rem; display: block; }
.product-card h3 { margin: .75rem 0 .25rem; font-size: 1rem; }
.product-card .price { font-weight: 700; color: var(--primary); }
.product-card .category { font-size: .8rem; color: var(--text-muted); background: var(--bg-secondary); padding: .15rem .5rem; border-radius: 999px; display: inline-block; margin-bottom: .5rem; }

/* ── Todo List ──────────────────────────────────────────── */
.todo-count { font-size: .85rem; color: var(--text-muted); margin-left: .5rem; }
.todo-list { list-style: none; margin-top: 1rem; }
.todo-item { display: flex; align-items: center; gap: .75rem; padding: .6rem .75rem; border-bottom: 1px solid var(--border); cursor: grab; }
.todo-item.dragging { opacity: .4; }
.todo-item.drag-over { border-top: 2px solid var(--primary); }
.todo-item input[type=checkbox] { width: 1.1rem; height: 1.1rem; cursor: pointer; }
.todo-text { flex: 1; }
.todo-text.completed { text-decoration: line-through; color: var(--text-muted); }
.todo-text[contenteditable="true"] { background: var(--bg-secondary); border-radius: .25rem; padding: .1rem .35rem; outline: 2px solid var(--primary); }

/* ── Table ──────────────────────────────────────────────── */
table { width: 100%; border-collapse: collapse; }
th, td { padding: .6rem 1rem; text-align: left; border-bottom: 1px solid var(--border); }
th { background: var(--bg-secondary); font-weight: 600; cursor: pointer; user-select: none; }
th:hover { background: var(--border); }
th.sort-asc::after { content: " ▲"; }
th.sort-desc::after { content: " ▼"; }

/* ── Pagination ─────────────────────────────────────────── */
.pagination { display: flex; gap: .5rem; margin-top: 1rem; align-items: center; }
.pagination button { min-width: 2rem; padding: .35rem .6rem; border: 1px solid var(--border); border-radius: .375rem; background: var(--bg); cursor: pointer; }
.pagination button.active { background: var(--primary); color: #fff; border-color: var(--primary); }
.pagination button:disabled { opacity: .4; cursor: not-allowed; }

/* ── Tabs & Accordion ───────────────────────────────────── */
.tabs { display: flex; gap: 0; border-bottom: 2px solid var(--border); margin-bottom: 1rem; }
.tab-btn { padding: .5rem 1.25rem; border: none; background: none; cursor: pointer; font-size: .9rem; color: var(--text-muted); border-bottom: 2px solid transparent; margin-bottom: -2px; }
.tab-btn.active { color: var(--primary); border-bottom-color: var(--primary); font-weight: 600; }
.tab-panel { display: none; } .tab-panel.active { display: block; }
.accordion-item { border: 1px solid var(--border); border-radius: .375rem; margin-bottom: .5rem; }
.accordion-header { width: 100%; padding: .75rem 1rem; background: var(--bg-secondary); border: none; cursor: pointer; text-align: left; font-weight: 500; border-radius: .375rem; }
.accordion-body { padding: 1rem; display: none; }
.accordion-body.open { display: block; }

/* ── Tooltip ────────────────────────────────────────────── */
.tooltip-wrap { position: relative; display: inline-block; }
.tooltip-wrap .tooltip { visibility: hidden; background: #333; color: #fff; font-size: .8rem; padding: .3rem .6rem; border-radius: .25rem; position: absolute; bottom: 130%; left: 50%; transform: translateX(-50%); white-space: nowrap; z-index: 10; }
.tooltip-wrap:hover .tooltip { visibility: visible; }

/* ── Hover Menu ─────────────────────────────────────────── */
.hover-menu { position: relative; display: inline-block; }
.hover-menu-trigger { cursor: pointer; padding: .5rem 1rem; background: var(--bg-secondary); border: 1px solid var(--border); border-radius: .375rem; }
.hover-menu-content { display: none; position: absolute; top: 100%; left: 0; background: var(--card-bg); border: 1px solid var(--border); border-radius: .375rem; box-shadow: var(--shadow); min-width: 150px; z-index: 20; }
.hover-menu:hover .hover-menu-content { display: block; }
.hover-menu-content a { display: block; padding: .5rem 1rem; color: var(--text); text-decoration: none; }
.hover-menu-content a:hover { background: var(--bg-secondary); }

/* ── Stepper (checkout) ─────────────────────────────────── */
.stepper { display: flex; gap: 0; margin-bottom: 2rem; }
.step { flex: 1; text-align: center; padding: .6rem; border-bottom: 3px solid var(--border); color: var(--text-muted); font-size: .875rem; }
.step.active { border-bottom-color: var(--primary); color: var(--primary); font-weight: 600; }
.step.done { border-bottom-color: var(--success); color: var(--success); }
.step-panel { display: none; } .step-panel.active { display: block; }

/* ── Alert area ─────────────────────────────────────────── */
.alert { padding: .75rem 1rem; border-radius: .375rem; margin-bottom: 1rem; }
.alert-success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
.alert-error { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }

/* ── Drag list ──────────────────────────────────────────── */
.drag-list { list-style: none; border: 1px solid var(--border); border-radius: .375rem; }
.drag-list li { padding: .6rem 1rem; border-bottom: 1px solid var(--border); cursor: grab; display: flex; align-items: center; gap: .5rem; }
.drag-list li:last-child { border-bottom: none; }
.drag-list li.dragging { opacity: .4; }
.drag-handle { color: var(--text-muted); cursor: grab; }

/* ── Misc ───────────────────────────────────────────────── */
.badge { display: inline-flex; align-items: center; justify-content: center; background: var(--primary); color: #fff; border-radius: 999px; min-width: 1.5rem; height: 1.5rem; font-size: .75rem; font-weight: 700; padding: 0 .4rem; }
.search-bar { display: flex; gap: .5rem; margin-bottom: 1rem; }
.search-bar input { flex: 1; }
.filter-row { display: flex; gap: 1rem; margin-bottom: 1rem; flex-wrap: wrap; }
.filter-row select { width: auto; min-width: 140px; }
```

- [ ] **Step 2: Commit**

```bash
git add public/style.css
git commit -m "feat: add shared CSS with light/dark theme"
```

---

### Task 6: Shared Client JS

**Files:**
- Create: `playwright-course-app/public/client.js`

- [ ] **Step 1: สร้าง client.js**

```javascript
// ── JWT Storage ───────────────────────────────────────────
const Auth = {
  getToken() { return localStorage.getItem('jwt_token'); },
  setToken(token) { localStorage.setItem('jwt_token', token); },
  removeToken() { localStorage.removeItem('jwt_token'); },
  getUser() {
    const token = this.getToken();
    if (!token) return null;
    try { return JSON.parse(atob(token.split('.')[1])); } catch { return null; }
  },
  isAdmin() { const u = this.getUser(); return u && u.role === 'admin'; }
};

// ── Session Badge ─────────────────────────────────────────
function updateSessionBadge() {
  const badge = document.getElementById('session-badge');
  if (!badge) return;
  const user = Auth.getUser();
  badge.textContent = user ? `Logged in as: ${user.username}` : 'Not logged in';
  badge.setAttribute('data-testid', 'session-badge');
}

// ── Fetch wrapper with auth ───────────────────────────────
async function apiFetch(url, options = {}) {
  const token = Auth.getToken();
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  const res = await fetch(url, { ...options, headers });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: res.statusText }));
    throw Object.assign(new Error(err.error || 'Request failed'), { status: res.status });
  }
  return res.json();
}

// ── Shared Nav ────────────────────────────────────────────
function buildNav(activePage) {
  const pages = [
    { href: '/', label: 'Dashboard' },
    { href: '/todos', label: 'Todos' },
    { href: '/shop', label: 'Shop' },
    { href: '/cart', label: 'Cart' },
    { href: '/components', label: 'Components' },
    { href: '/advanced', label: 'Advanced' },
    { href: '/visual', label: 'Visual' },
    ...(Auth.isAdmin() ? [{ href: '/admin', label: 'Admin' }] : [])
  ];
  return `<nav role="navigation" aria-label="main navigation">
    <span class="nav-brand">🎭 Playwright Course App</span>
    ${pages.map(p => `<a href="${p.href}" data-testid="nav-${p.label.toLowerCase()}">${p.label}</a>`).join('')}
    <span id="session-badge" data-testid="session-badge"></span>
    ${Auth.getToken() ? `<a href="#" onclick="Auth.removeToken();location.href='/'" data-testid="nav-logout">Logout</a>` : `<a href="/login" data-testid="nav-login">Login</a>`}
  </nav>`;
}

document.addEventListener('DOMContentLoaded', () => {
  const navEl = document.getElementById('main-nav');
  if (navEl) { navEl.innerHTML = buildNav(); updateSessionBadge(); }
  updateSessionBadge();
});
```

- [ ] **Step 2: Commit**

```bash
git add public/client.js
git commit -m "feat: add shared client JS (JWT, session badge, fetch wrapper)"
```

---

### Task 7: Dashboard + Login Pages

**Files:**
- Create: `playwright-course-app/public/index.html`
- Create: `playwright-course-app/public/login.html`

- [ ] **Step 1: สร้าง index.html (Dashboard)**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Playwright Course App — Dashboard</title>
  <link rel="stylesheet" href="/style.css">
</head>
<body>
  <div id="main-nav"></div>
  <main>
    <h1 data-testid="dashboard-heading">Welcome to Playwright Course App</h1>
    <p>This app is used as a test target for the Playwright TypeScript Master Course.</p>
    <div class="card-grid" style="margin-top:1.5rem">
      <div class="card"><h2>📝 Todos</h2><p>CRUD, double-click edit, drag-drop</p><a href="/todos" class="btn btn-primary" data-testid="link-todos" style="margin-top:1rem;text-decoration:none">Open Todos</a></div>
      <div class="card"><h2>🛒 Shop</h2><p>Filter, search, pagination</p><a href="/shop" class="btn btn-primary" data-testid="link-shop" style="margin-top:1rem;text-decoration:none">Open Shop</a></div>
      <div class="card"><h2>🧩 Components</h2><p>All UI element types (kitchen sink)</p><a href="/components" class="btn btn-primary" data-testid="link-components" style="margin-top:1rem;text-decoration:none">Open Components</a></div>
      <div class="card"><h2>⚡ Advanced</h2><p>Popup, iframe, file upload, dialogs</p><a href="/advanced" class="btn btn-primary" data-testid="link-advanced" style="margin-top:1rem;text-decoration:none">Open Advanced</a></div>
      <div class="card"><h2>🎨 Visual</h2><p>Theme switcher for screenshot tests</p><a href="/visual" class="btn btn-primary" data-testid="link-visual" style="margin-top:1rem;text-decoration:none">Open Visual</a></div>
      <div class="card"><h2>🔐 Login</h2><p>Authenticate to access protected pages</p><a href="/login" class="btn btn-secondary" data-testid="link-login" style="margin-top:1rem;text-decoration:none">Go to Login</a></div>
    </div>
  </main>
  <script src="/client.js"></script>
</body>
</html>
```

- [ ] **Step 2: สร้าง login.html**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Login — Playwright Course App</title>
  <link rel="stylesheet" href="/style.css">
</head>
<body>
  <div id="main-nav"></div>
  <main style="max-width:400px">
    <h1>Login</h1>
    <div class="card">
      <form id="login-form" data-testid="login-form" novalidate>
        <div class="form-group">
          <label for="username">Username</label>
          <input type="text" id="username" name="username" data-testid="input-username"
                 placeholder="Enter username" autocomplete="username" required>
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <input type="password" id="password" name="password" data-testid="input-password"
                 placeholder="Enter password" autocomplete="current-password" required>
        </div>
        <p class="error-msg" id="login-error" data-testid="login-error" role="alert"></p>
        <button type="submit" class="btn btn-primary" data-testid="btn-login" style="width:100%">
          Login
        </button>
      </form>
      <p style="margin-top:1rem;font-size:.85rem;color:var(--text-muted)">
        Credentials: <strong>admin/admin123</strong> or <strong>testuser/test123</strong>
      </p>
    </div>
  </main>
  <script src="/client.js"></script>
  <script>
    const form = document.getElementById('login-form');
    const errorEl = document.getElementById('login-error');

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      errorEl.classList.remove('visible');
      const username = document.getElementById('username').value.trim();
      const password = document.getElementById('password').value;
      if (!username || !password) {
        errorEl.textContent = 'Username and password are required';
        errorEl.classList.add('visible');
        return;
      }
      try {
        const data = await apiFetch('/api/auth/login', {
          method: 'POST',
          body: JSON.stringify({ username, password })
        });
        Auth.setToken(data.token);
        const params = new URLSearchParams(location.search);
        location.href = params.get('redirect') || '/';
      } catch (err) {
        errorEl.textContent = err.message || 'Invalid credentials';
        errorEl.classList.add('visible');
      }
    });
  </script>
</body>
</html>
```

- [ ] **Step 3: ทดสอบ**

```bash
node server.js &
# เปิด browser ไปที่ http://localhost:3000 — เห็น Dashboard
# เปิด http://localhost:3000/login — เห็น login form
# login ด้วย testuser/test123 → redirect ไป /
# session badge แสดง "Logged in as: testuser"
kill %1
```

- [ ] **Step 4: Commit**

```bash
git add public/index.html public/login.html
git commit -m "feat: add dashboard and login pages"
```

---

### Task 8: Todos Page

**Files:**
- Create: `playwright-course-app/public/todos.html`

- [ ] **Step 1: สร้าง todos.html**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Todos — Playwright Course App</title>
  <link rel="stylesheet" href="/style.css">
</head>
<body>
  <div id="main-nav"></div>
  <main>
    <h1>
      Todo List
      <span class="badge" id="todo-count-badge" data-testid="todo-count">0</span>
      <small class="todo-count">items</small>
    </h1>

    <form id="add-form" data-testid="add-todo-form" style="display:flex;gap:.5rem;margin-bottom:1.5rem">
      <input type="text" id="new-todo" data-testid="input-new-todo"
             placeholder="What needs to be done?" aria-label="New todo text" style="flex:1">
      <button type="submit" class="btn btn-primary" data-testid="btn-add-todo">Add</button>
    </form>

    <ul class="todo-list" id="todo-list" data-testid="todo-list" role="list" aria-label="Todo items"></ul>

    <div style="margin-top:1rem;display:flex;gap:.5rem;flex-wrap:wrap">
      <button class="btn btn-secondary btn-sm" id="btn-clear-completed" data-testid="btn-clear-completed">
        Clear Completed
      </button>
      <button class="btn btn-danger btn-sm" id="btn-reset" data-testid="btn-reset">
        Reset All
      </button>
    </div>
  </main>

  <script src="/client.js"></script>
  <script>
    let todos = [];
    let dragSrcIdx = null;

    async function loadTodos() {
      todos = await apiFetch('/api/todos');
      renderTodos();
    }

    function renderTodos() {
      const list = document.getElementById('todo-list');
      const badge = document.getElementById('todo-count-badge');
      badge.textContent = todos.length;
      badge.setAttribute('data-testid', 'todo-count');

      list.innerHTML = todos.map((t, i) => `
        <li class="todo-item" data-id="${t.id}" data-idx="${i}" draggable="true" data-testid="todo-item-${t.id}" role="listitem">
          <input type="checkbox" ${t.completed ? 'checked' : ''}
                 aria-label="Mark '${t.text}' as completed"
                 data-testid="todo-checkbox-${t.id}">
          <span class="todo-text ${t.completed ? 'completed' : ''}"
                data-testid="todo-text-${t.id}"
                title="Double-click to edit">${t.text}</span>
          <button class="btn btn-danger btn-sm" data-testid="btn-delete-${t.id}" title="Delete">✕</button>
        </li>`).join('');

      // Checkbox toggle
      list.querySelectorAll('input[type=checkbox]').forEach(cb => {
        cb.addEventListener('change', async () => {
          const id = Number(cb.closest('li').dataset.id);
          await apiFetch(`/api/todos/${id}`, { method: 'PATCH', body: JSON.stringify({ completed: cb.checked }) });
          await loadTodos();
        });
      });

      // Delete
      list.querySelectorAll('[data-testid^="btn-delete-"]').forEach(btn => {
        btn.addEventListener('click', async () => {
          const id = Number(btn.closest('li').dataset.id);
          await apiFetch(`/api/todos/${id}`, { method: 'DELETE' });
          await loadTodos();
        });
      });

      // Double-click to edit inline
      list.querySelectorAll('.todo-text').forEach(span => {
        span.addEventListener('dblclick', () => {
          span.contentEditable = 'true';
          span.focus();
          const range = document.createRange();
          range.selectNodeContents(span);
          window.getSelection().removeAllRanges();
          window.getSelection().addRange(range);
        });
        span.addEventListener('keydown', async (e) => {
          if (e.key === 'Enter') { e.preventDefault(); span.blur(); }
          if (e.key === 'Escape') { span.contentEditable = 'false'; await loadTodos(); }
        });
        span.addEventListener('blur', async () => {
          if (span.contentEditable !== 'true') return;
          span.contentEditable = 'false';
          const id = Number(span.closest('li').dataset.id);
          const newText = span.textContent.trim();
          if (newText) await apiFetch(`/api/todos/${id}`, { method: 'PATCH', body: JSON.stringify({ text: newText }) });
          await loadTodos();
        });
      });

      // Drag-drop reorder
      list.querySelectorAll('.todo-item').forEach(li => {
        li.addEventListener('dragstart', () => { dragSrcIdx = Number(li.dataset.idx); li.classList.add('dragging'); });
        li.addEventListener('dragend', () => li.classList.remove('dragging'));
        li.addEventListener('dragover', e => { e.preventDefault(); li.classList.add('drag-over'); });
        li.addEventListener('dragleave', () => li.classList.remove('drag-over'));
        li.addEventListener('drop', async () => {
          li.classList.remove('drag-over');
          const destIdx = Number(li.dataset.idx);
          if (dragSrcIdx === destIdx) return;
          const moved = todos.splice(dragSrcIdx, 1)[0];
          todos.splice(destIdx, 0, moved);
          renderTodos();
        });
      });
    }

    document.getElementById('add-form').addEventListener('submit', async (e) => {
      e.preventDefault();
      const input = document.getElementById('new-todo');
      const text = input.value.trim();
      if (!text) return;
      await apiFetch('/api/todos', { method: 'POST', body: JSON.stringify({ text }) });
      input.value = '';
      await loadTodos();
    });

    document.getElementById('btn-clear-completed').addEventListener('click', async () => {
      const completed = todos.filter(t => t.completed);
      await Promise.all(completed.map(t => apiFetch(`/api/todos/${t.id}`, { method: 'DELETE' })));
      await loadTodos();
    });

    document.getElementById('btn-reset').addEventListener('click', async () => {
      if (confirm('Delete all todos?')) {
        await apiFetch('/api/reset', { method: 'POST' });
        await loadTodos();
      }
    });

    loadTodos();
  </script>
</body>
</html>
```

- [ ] **Step 2: ทดสอบ**

```bash
node server.js &
# เปิด http://localhost:3000/todos
# - พิมพ์ todo แล้วกด Add → todo ปรากฏ, count badge เพิ่ม
# - double-click ที่ todo text → editable inline
# - drag todo ขึ้น/ลง → เรียงลำดับใหม่
# - tick checkbox → strikethrough
kill %1
```

- [ ] **Step 3: Commit**

```bash
git add public/todos.html
git commit -m "feat: add todos page with double-click edit and drag-drop"
```

---

### Task 9: Shop Page

**Files:**
- Create: `playwright-course-app/public/shop.html`

- [ ] **Step 1: สร้าง shop.html**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Shop — Playwright Course App</title>
  <link rel="stylesheet" href="/style.css">
</head>
<body>
  <div id="main-nav"></div>
  <main>
    <h1>Product Shop</h1>
    <div class="filter-row">
      <div>
        <label for="filter-category">Category</label>
        <select id="filter-category" data-testid="filter-category">
          <option value="">All Categories</option>
          <option value="Electronics">Electronics</option>
          <option value="Books">Books</option>
          <option value="Clothing">Clothing</option>
        </select>
      </div>
      <div style="flex:1">
        <label for="search-input">Search</label>
        <div class="search-bar">
          <input type="text" id="search-input" data-testid="search-input" placeholder="Search products...">
          <button class="btn btn-primary" id="btn-search" data-testid="btn-search">Search</button>
        </div>
      </div>
    </div>

    <div id="product-grid" class="card-grid" data-testid="product-grid" role="list"></div>
    <div class="pagination" id="pagination" data-testid="pagination" aria-label="Product pagination"></div>
    <p id="no-results" data-testid="no-results" style="display:none;color:var(--text-muted)">
      No products found.
    </p>
  </main>

  <script src="/client.js"></script>
  <script>
    let currentPage = 1;
    const LIMIT = 5;

    async function loadProducts() {
      const category = document.getElementById('filter-category').value;
      const search = document.getElementById('search-input').value.trim();
      const params = new URLSearchParams({ page: currentPage, limit: LIMIT });
      if (category) params.set('category', category);
      if (search) params.set('search', search);

      const data = await apiFetch(`/api/products?${params}`);
      renderProducts(data);
      renderPagination(data);
    }

    function renderProducts({ data }) {
      const grid = document.getElementById('product-grid');
      const noResults = document.getElementById('no-results');
      if (!data.length) { grid.innerHTML = ''; noResults.style.display = 'block'; return; }
      noResults.style.display = 'none';
      grid.innerHTML = data.map(p => `
        <div class="card product-card" data-testid="product-card-${p.id}" role="listitem">
          <img src="${p.image}" alt="Product: ${p.name}" data-testid="product-image-${p.id}">
          <span class="category" data-testid="product-category-${p.id}">${p.category}</span>
          <h3 data-testid="product-name-${p.id}">${p.name}</h3>
          <p style="font-size:.85rem;color:var(--text-muted);margin-bottom:.5rem">${p.description}</p>
          <p class="price" data-testid="product-price-${p.id}">$${p.price}</p>
          <button class="btn btn-primary btn-sm" style="margin-top:.75rem;width:100%"
                  data-testid="btn-add-cart-${p.id}"
                  onclick="addToCart(${p.id},'${p.name}',${p.price})">Add to Cart</button>
        </div>`).join('');
    }

    function renderPagination({ page, totalPages }) {
      const el = document.getElementById('pagination');
      let html = `<button data-testid="btn-prev-page" ${page <= 1 ? 'disabled' : ''} onclick="gotoPage(${page - 1})">‹ Prev</button>`;
      for (let i = 1; i <= totalPages; i++) {
        html += `<button class="${i === page ? 'active' : ''}" data-testid="btn-page-${i}" onclick="gotoPage(${i})">${i}</button>`;
      }
      html += `<button data-testid="btn-next-page" ${page >= totalPages ? 'disabled' : ''} onclick="gotoPage(${page + 1})">Next ›</button>`;
      el.innerHTML = html;
    }

    function gotoPage(p) { currentPage = p; loadProducts(); }

    function addToCart(id, name, price) {
      let cart = JSON.parse(localStorage.getItem('cart') || '[]');
      const existing = cart.find(i => i.id === id);
      if (existing) existing.qty++; else cart.push({ id, name, price, qty: 1 });
      localStorage.setItem('cart', JSON.stringify(cart));
      alert(`Added "${name}" to cart`);
    }

    document.getElementById('btn-search').addEventListener('click', () => { currentPage = 1; loadProducts(); });
    document.getElementById('filter-category').addEventListener('change', () => { currentPage = 1; loadProducts(); });
    document.getElementById('search-input').addEventListener('keydown', e => { if (e.key === 'Enter') { currentPage = 1; loadProducts(); } });

    loadProducts();
  </script>
</body>
</html>
```

- [ ] **Step 2: Commit**

```bash
git add public/shop.html
git commit -m "feat: add shop page with filter, search, pagination"
```

---

### Task 10: Cart + Checkout Pages

**Files:**
- Create: `playwright-course-app/public/cart.html`
- Create: `playwright-course-app/public/checkout.html`

- [ ] **Step 1: สร้าง cart.html**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"><title>Cart — Playwright Course App</title>
  <link rel="stylesheet" href="/style.css">
</head>
<body>
  <div id="main-nav"></div>
  <main>
    <h1>Shopping Cart</h1>
    <div id="cart-items" data-testid="cart-items"></div>
    <div id="cart-total" data-testid="cart-total" style="font-weight:700;font-size:1.1rem;margin:1rem 0"></div>
    <a href="/checkout" class="btn btn-primary" data-testid="btn-checkout" id="btn-checkout" style="text-decoration:none">Proceed to Checkout</a>
  </main>
  <script src="/client.js"></script>
  <script>
    function render() {
      const cart = JSON.parse(localStorage.getItem('cart') || '[]');
      const el = document.getElementById('cart-items');
      if (!cart.length) { el.innerHTML = '<p data-testid="empty-cart">Your cart is empty. <a href="/shop">Go shopping</a></p>'; document.getElementById('cart-total').textContent = ''; document.getElementById('btn-checkout').style.display = 'none'; return; }
      el.innerHTML = `<table><thead><tr><th>Product</th><th>Price</th><th>Qty</th><th>Subtotal</th><th></th></tr></thead><tbody>
        ${cart.map((item, i) => `<tr data-testid="cart-row-${item.id}">
          <td data-testid="cart-name-${item.id}">${item.name}</td>
          <td>$${item.price}</td>
          <td><input type="number" min="1" value="${item.qty}" style="width:4rem" data-testid="cart-qty-${item.id}" onchange="updateQty(${i}, this.value)"></td>
          <td data-testid="cart-subtotal-${item.id}">$${(item.price * item.qty).toFixed(2)}</td>
          <td><button class="btn btn-danger btn-sm" data-testid="btn-remove-${item.id}" onclick="remove(${i})">Remove</button></td>
        </tr>`).join('')}</tbody></table>`;
      const total = cart.reduce((s, i) => s + i.price * i.qty, 0);
      document.getElementById('cart-total').textContent = `Total: $${total.toFixed(2)}`;
    }
    function updateQty(idx, val) { const c = JSON.parse(localStorage.getItem('cart')||'[]'); c[idx].qty = Math.max(1, parseInt(val)||1); localStorage.setItem('cart', JSON.stringify(c)); render(); }
    function remove(idx) { const c = JSON.parse(localStorage.getItem('cart')||'[]'); c.splice(idx,1); localStorage.setItem('cart', JSON.stringify(c)); render(); }
    render();
  </script>
</body>
</html>
```

- [ ] **Step 2: สร้าง checkout.html (3-step wizard)**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"><title>Checkout — Playwright Course App</title>
  <link rel="stylesheet" href="/style.css">
</head>
<body>
  <div id="main-nav"></div>
  <main style="max-width:600px">
    <h1>Checkout</h1>
    <div class="stepper" role="list">
      <div class="step active" data-testid="step-shipping" role="listitem">1. Shipping</div>
      <div class="step" data-testid="step-payment" role="listitem">2. Payment</div>
      <div class="step" data-testid="step-confirm" role="listitem">3. Confirm</div>
    </div>

    <!-- Step 1: Shipping -->
    <div class="step-panel active card" id="panel-shipping" data-testid="panel-shipping">
      <h2>Shipping Information</h2>
      <div class="form-group">
        <label for="full-name">Full Name</label>
        <input id="full-name" type="text" data-testid="input-full-name" placeholder="John Doe" required>
      </div>
      <div class="form-group">
        <label for="address">Address</label>
        <input id="address" type="text" data-testid="input-address" placeholder="123 Main St" required>
      </div>
      <div class="form-group">
        <label for="city">City</label>
        <input id="city" type="text" data-testid="input-city" placeholder="Bangkok" required>
      </div>
      <div class="form-group">
        <label for="country">Country</label>
        <select id="country" data-testid="select-country">
          <option value="TH">Thailand</option>
          <option value="US">United States</option>
          <option value="GB">United Kingdom</option>
        </select>
      </div>
      <div style="display:flex;justify-content:flex-end;margin-top:1rem">
        <button class="btn btn-primary" data-testid="btn-next-shipping" onclick="goStep(2)">Next →</button>
      </div>
    </div>

    <!-- Step 2: Payment -->
    <div class="step-panel card" id="panel-payment" data-testid="panel-payment">
      <h2>Payment Details</h2>
      <div class="form-group">
        <label for="card-number">Card Number</label>
        <input id="card-number" type="text" data-testid="input-card-number" placeholder="1234 5678 9012 3456" maxlength="19">
      </div>
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem">
        <div class="form-group">
          <label for="expiry">Expiry (MM/YY)</label>
          <input id="expiry" type="text" data-testid="input-expiry" placeholder="12/26" maxlength="5">
        </div>
        <div class="form-group">
          <label for="cvv">CVV</label>
          <input id="cvv" type="text" data-testid="input-cvv" placeholder="123" maxlength="3">
        </div>
      </div>
      <div style="display:flex;justify-content:space-between;margin-top:1rem">
        <button class="btn btn-secondary" data-testid="btn-back-payment" onclick="goStep(1)">← Back</button>
        <button class="btn btn-primary" data-testid="btn-next-payment" onclick="goStep(3)">Next →</button>
      </div>
    </div>

    <!-- Step 3: Confirm -->
    <div class="step-panel card" id="panel-confirm" data-testid="panel-confirm">
      <h2>Order Confirmation</h2>
      <div id="order-summary" data-testid="order-summary"></div>
      <div style="display:flex;justify-content:space-between;margin-top:1rem">
        <button class="btn btn-secondary" data-testid="btn-back-confirm" onclick="goStep(2)">← Back</button>
        <button class="btn btn-primary" data-testid="btn-place-order" onclick="placeOrder()">Place Order</button>
      </div>
    </div>

    <div class="alert alert-success" id="order-success" data-testid="order-success" style="display:none;margin-top:1rem">
      ✅ Order placed successfully! Order ID: <span id="order-id" data-testid="order-id"></span>
    </div>
  </main>

  <script src="/client.js"></script>
  <script>
    let currentStep = 1;
    function goStep(n) {
      currentStep = n;
      document.querySelectorAll('.step-panel').forEach(p => p.classList.remove('active'));
      document.getElementById(`panel-${['shipping','payment','confirm'][n-1]}`).classList.add('active');
      document.querySelectorAll('.step').forEach((s, i) => {
        s.classList.toggle('active', i + 1 === n);
        s.classList.toggle('done', i + 1 < n);
      });
      if (n === 3) {
        const cart = JSON.parse(localStorage.getItem('cart') || '[]');
        const total = cart.reduce((s, i) => s + i.price * i.qty, 0);
        document.getElementById('order-summary').innerHTML = `<p><strong>${cart.length} items</strong></p><p>Total: <strong>$${total.toFixed(2)}</strong></p>`;
      }
    }
    async function placeOrder() {
      const cart = JSON.parse(localStorage.getItem('cart') || '[]');
      try {
        const order = await apiFetch('/api/orders', { method: 'POST', body: JSON.stringify({ items: cart }) });
        document.getElementById('order-id').textContent = order.orderId;
        document.getElementById('order-success').style.display = 'block';
        document.getElementById('panel-confirm').style.display = 'none';
        localStorage.removeItem('cart');
      } catch {
        alert('Please login to place an order');
        location.href = '/login?redirect=/checkout';
      }
    }
  </script>
</body>
</html>
```

- [ ] **Step 3: Commit**

```bash
git add public/cart.html public/checkout.html
git commit -m "feat: add cart and 3-step checkout wizard"
```

---

### Task 11: Components Page (Kitchen Sink)

**Files:**
- Create: `playwright-course-app/public/components.html`

- [ ] **Step 1: สร้าง components.html**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"><title>Components — Playwright Course App</title>
  <link rel="stylesheet" href="/style.css">
</head>
<body>
  <div id="main-nav"></div>
  <main>
    <h1>UI Components (Kitchen Sink)</h1>
    <p style="color:var(--text-muted);margin-bottom:2rem">All UI element types for Playwright locator and action practice.</p>

    <!-- Checkboxes -->
    <section class="card" style="margin-bottom:1rem" data-testid="section-checkboxes">
      <h2>Checkboxes</h2>
      <div class="form-group">
        <label><input type="checkbox" data-testid="check-option1"> Option 1 — JavaScript</label>
        <label><input type="checkbox" data-testid="check-option2" checked> Option 2 — TypeScript (checked by default)</label>
        <label><input type="checkbox" data-testid="check-option3" disabled> Option 3 — Disabled</label>
      </div>
    </section>

    <!-- Radio Buttons -->
    <section class="card" style="margin-bottom:1rem" data-testid="section-radios">
      <h2>Radio Buttons</h2>
      <fieldset style="border:none;padding:0">
        <legend style="font-weight:600;margin-bottom:.5rem">Preferred Browser</legend>
        <label><input type="radio" name="browser" value="chromium" data-testid="radio-chromium"> Chromium</label><br>
        <label><input type="radio" name="browser" value="firefox" data-testid="radio-firefox" checked> Firefox</label><br>
        <label><input type="radio" name="browser" value="webkit" data-testid="radio-webkit"> WebKit</label>
      </fieldset>
    </section>

    <!-- Select Dropdown -->
    <section class="card" style="margin-bottom:1rem" data-testid="section-select">
      <h2>Select Dropdown</h2>
      <div class="form-group">
        <label for="framework-select">Test Framework</label>
        <select id="framework-select" data-testid="select-framework">
          <option value="">-- Choose --</option>
          <option value="playwright">Playwright</option>
          <option value="cypress">Cypress</option>
          <option value="selenium">Selenium</option>
          <option value="rf">Robot Framework</option>
        </select>
      </div>
      <p id="select-result" data-testid="select-result" style="color:var(--text-muted)"></p>
      <script>document.getElementById('framework-select').addEventListener('change', e => { document.getElementById('select-result').textContent = e.target.value ? `Selected: ${e.target.value}` : ''; });</script>
    </section>

    <!-- Date Picker -->
    <section class="card" style="margin-bottom:1rem" data-testid="section-date">
      <h2>Date Picker</h2>
      <div class="form-group">
        <label for="date-input">Select Date</label>
        <input type="date" id="date-input" data-testid="input-date" style="width:auto">
      </div>
      <p id="date-result" data-testid="date-result" style="color:var(--text-muted)"></p>
      <script>document.getElementById('date-input').addEventListener('change', e => { document.getElementById('date-result').textContent = `Selected: ${e.target.value}`; });</script>
    </section>

    <!-- Range Slider -->
    <section class="card" style="margin-bottom:1rem" data-testid="section-slider">
      <h2>Range Slider</h2>
      <div class="form-group">
        <label for="range-input">Value: <span id="range-value" data-testid="range-value">50</span></label>
        <input type="range" id="range-input" data-testid="input-range" min="0" max="100" value="50">
      </div>
      <script>document.getElementById('range-input').addEventListener('input', e => { document.getElementById('range-value').textContent = e.target.value; });</script>
    </section>

    <!-- Hover Menu -->
    <section class="card" style="margin-bottom:1rem" data-testid="section-hover">
      <h2>Hover Menu</h2>
      <div class="hover-menu" data-testid="hover-menu">
        <div class="hover-menu-trigger" data-testid="hover-trigger">Hover me ▾</div>
        <div class="hover-menu-content" data-testid="hover-content">
          <a href="#" data-testid="hover-item-profile">👤 Profile</a>
          <a href="#" data-testid="hover-item-settings">⚙️ Settings</a>
          <a href="#" data-testid="hover-item-logout">🚪 Logout</a>
        </div>
      </div>
      &nbsp;&nbsp;
      <span class="tooltip-wrap" data-testid="tooltip-wrap">
        <button class="btn btn-secondary" data-testid="tooltip-trigger">Hover for tooltip</button>
        <span class="tooltip" data-testid="tooltip-content">This is a tooltip! 🎭</span>
      </span>
    </section>

    <!-- Sortable Table -->
    <section class="card" style="margin-bottom:1rem" data-testid="section-table">
      <h2>Sortable Table</h2>
      <table id="sort-table" data-testid="sortable-table">
        <thead><tr>
          <th data-col="name" data-testid="th-name">Name</th>
          <th data-col="age" data-testid="th-age">Age</th>
          <th data-col="city" data-testid="th-city">City</th>
        </tr></thead>
        <tbody id="sort-tbody"></tbody>
      </table>
      <div class="pagination" id="table-pagination" data-testid="table-pagination"></div>
    </section>

    <!-- Drag-and-Drop List -->
    <section class="card" style="margin-bottom:1rem" data-testid="section-draglist">
      <h2>Drag-and-Drop List</h2>
      <ul class="drag-list" id="drag-list" data-testid="drag-list" role="list">
        <li draggable="true" data-testid="drag-item-1" role="listitem"><span class="drag-handle">⠿</span> Item 1 — Playwright</li>
        <li draggable="true" data-testid="drag-item-2" role="listitem"><span class="drag-handle">⠿</span> Item 2 — TypeScript</li>
        <li draggable="true" data-testid="drag-item-3" role="listitem"><span class="drag-handle">⠿</span> Item 3 — Node.js</li>
        <li draggable="true" data-testid="drag-item-4" role="listitem"><span class="drag-handle">⠿</span> Item 4 — Express</li>
      </ul>
    </section>

    <!-- Accordion -->
    <section class="card" style="margin-bottom:1rem" data-testid="section-accordion">
      <h2>Accordion</h2>
      <div class="accordion-item" data-testid="accordion-item-1">
        <button class="accordion-header" data-testid="accordion-header-1">What is Playwright?</button>
        <div class="accordion-body" data-testid="accordion-body-1">Playwright is a Node.js library for browser automation, supporting Chromium, Firefox, and WebKit.</div>
      </div>
      <div class="accordion-item" data-testid="accordion-item-2">
        <button class="accordion-header" data-testid="accordion-header-2">Why use TypeScript?</button>
        <div class="accordion-body" data-testid="accordion-body-2">TypeScript adds static typing, better IDE support, and is the first-class language for Playwright.</div>
      </div>
      <div class="accordion-item" data-testid="accordion-item-3">
        <button class="accordion-header" data-testid="accordion-header-3">What is a Fixture?</button>
        <div class="accordion-body" data-testid="accordion-body-3">Fixtures are reusable setup/teardown blocks in Playwright that replace beforeEach/afterEach patterns.</div>
      </div>
    </section>

    <!-- Tabs -->
    <section class="card" data-testid="section-tabs">
      <h2>Tabs</h2>
      <div class="tabs" role="tablist" data-testid="tablist">
        <button class="tab-btn active" role="tab" data-testid="tab-overview" onclick="switchTab('overview')">Overview</button>
        <button class="tab-btn" role="tab" data-testid="tab-details" onclick="switchTab('details')">Details</button>
        <button class="tab-btn" role="tab" data-testid="tab-reviews" onclick="switchTab('reviews')">Reviews</button>
      </div>
      <div class="tab-panel active" id="tab-overview" data-testid="panel-overview" role="tabpanel">Overview content — Playwright is fast and reliable.</div>
      <div class="tab-panel" id="tab-details" data-testid="panel-details" role="tabpanel">Details content — Supports Chromium, Firefox, WebKit.</div>
      <div class="tab-panel" id="tab-reviews" data-testid="panel-reviews" role="tabpanel">Reviews content — ⭐⭐⭐⭐⭐ "Best testing tool ever!"</div>
    </section>
  </main>

  <script src="/client.js"></script>
  <script>
    // Accordion
    document.querySelectorAll('.accordion-header').forEach(btn => {
      btn.addEventListener('click', () => {
        const body = btn.nextElementSibling;
        body.classList.toggle('open');
      });
    });

    // Tabs
    function switchTab(name) {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
      document.querySelector(`[data-testid="tab-${name}"]`).classList.add('active');
      document.getElementById(`tab-${name}`).classList.add('active');
    }

    // Sortable Table
    const tableData = [
      { name: 'Alice', age: 28, city: 'Bangkok' }, { name: 'Bob', age: 34, city: 'Chiang Mai' },
      { name: 'Carol', age: 22, city: 'Phuket' }, { name: 'Dave', age: 41, city: 'Bangkok' },
      { name: 'Eve', age: 29, city: 'Pattaya' }, { name: 'Frank', age: 37, city: 'Khon Kaen' },
      { name: 'Grace', age: 25, city: 'Chiang Mai' }, { name: 'Henry', age: 31, city: 'Bangkok' }
    ];
    let sortCol = 'name', sortDir = 'asc', tablePage = 1;
    const TABLE_LIMIT = 3;

    function renderTable() {
      const sorted = [...tableData].sort((a, b) => {
        const va = a[sortCol], vb = b[sortCol];
        return (sortDir === 'asc' ? 1 : -1) * (va < vb ? -1 : va > vb ? 1 : 0);
      });
      const paged = sorted.slice((tablePage - 1) * TABLE_LIMIT, tablePage * TABLE_LIMIT);
      document.getElementById('sort-tbody').innerHTML = paged.map(r => `<tr data-testid="table-row-${r.name.toLowerCase()}"><td>${r.name}</td><td>${r.age}</td><td>${r.city}</td></tr>`).join('');
      document.querySelectorAll('#sort-table th').forEach(th => {
        th.classList.remove('sort-asc', 'sort-desc');
        if (th.dataset.col === sortCol) th.classList.add(`sort-${sortDir}`);
      });
      const totalPages = Math.ceil(tableData.length / TABLE_LIMIT);
      document.getElementById('table-pagination').innerHTML =
        `<button ${tablePage <= 1 ? 'disabled' : ''} onclick="tablePage--;renderTable()">‹</button>` +
        `<span>Page ${tablePage} / ${totalPages}</span>` +
        `<button ${tablePage >= totalPages ? 'disabled' : ''} onclick="tablePage++;renderTable()">›</button>`;
    }

    document.querySelectorAll('#sort-table th').forEach(th => {
      th.addEventListener('click', () => {
        if (sortCol === th.dataset.col) sortDir = sortDir === 'asc' ? 'desc' : 'asc';
        else { sortCol = th.dataset.col; sortDir = 'asc'; }
        tablePage = 1;
        renderTable();
      });
    });

    renderTable();

    // Drag list
    let dragSrc = null;
    document.querySelectorAll('#drag-list li').forEach(li => {
      li.addEventListener('dragstart', () => { dragSrc = li; li.classList.add('dragging'); });
      li.addEventListener('dragend', () => li.classList.remove('dragging'));
      li.addEventListener('dragover', e => e.preventDefault());
      li.addEventListener('drop', () => {
        if (dragSrc && dragSrc !== li) {
          const list = li.parentNode;
          const items = [...list.children];
          const srcIdx = items.indexOf(dragSrc), dstIdx = items.indexOf(li);
          if (srcIdx < dstIdx) list.insertBefore(dragSrc, li.nextSibling);
          else list.insertBefore(dragSrc, li);
        }
      });
    });
  </script>
</body>
</html>
```

- [ ] **Step 2: Commit**

```bash
git add public/components.html
git commit -m "feat: add kitchen sink components page"
```

---

### Task 12: Advanced Page

**Files:**
- Create: `playwright-course-app/public/advanced.html`

- [ ] **Step 1: สร้าง advanced.html**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"><title>Advanced — Playwright Course App</title>
  <link rel="stylesheet" href="/style.css">
</head>
<body>
  <div id="main-nav"></div>
  <main>
    <h1>Advanced Browser Features</h1>
    <p style="color:var(--text-muted);margin-bottom:2rem">Testing targets for popups, iframes, file handling, dialogs, and Shadow DOM.</p>

    <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;margin-bottom:1rem">

      <!-- Popup / New Tab -->
      <section class="card" data-testid="section-popup">
        <h2>Popup / New Tab</h2>
        <p style="font-size:.875rem;color:var(--text-muted);margin-bottom:.75rem">Click to open a new browser tab.</p>
        <button class="btn btn-primary" data-testid="btn-open-popup"
                onclick="window.open('/todos','_blank','width=800,height=600')">
          Open Todos in New Tab
        </button>
      </section>

      <!-- File Upload -->
      <section class="card" data-testid="section-upload">
        <h2>File Upload</h2>
        <form id="upload-form" data-testid="upload-form">
          <div class="form-group">
            <label for="file-input">Choose a file</label>
            <input type="file" id="file-input" data-testid="input-file" accept=".txt,.csv,.json">
          </div>
          <button type="submit" class="btn btn-primary btn-sm" data-testid="btn-upload">Upload</button>
          <p id="upload-result" data-testid="upload-result" style="margin-top:.5rem;color:var(--success);display:none"></p>
        </form>
      </section>

      <!-- Dialogs -->
      <section class="card" data-testid="section-dialogs">
        <h2>Browser Dialogs</h2>
        <div style="display:flex;flex-direction:column;gap:.5rem">
          <button class="btn btn-secondary" data-testid="btn-alert" onclick="alert('This is an alert dialog!')">
            Trigger alert()
          </button>
          <button class="btn btn-secondary" data-testid="btn-confirm"
                  onclick="document.getElementById('confirm-result').textContent = confirm('Do you confirm?') ? 'Confirmed ✅' : 'Cancelled ❌'">
            Trigger confirm()
          </button>
          <p id="confirm-result" data-testid="confirm-result" style="font-weight:600"></p>
          <button class="btn btn-secondary" data-testid="btn-prompt"
                  onclick="const r=prompt('Enter your name:','Playwright'); document.getElementById('prompt-result').textContent = r ? `Hello, ${r}!` : 'Cancelled'">
            Trigger prompt()
          </button>
          <p id="prompt-result" data-testid="prompt-result" style="font-weight:600"></p>
        </div>
      </section>

      <!-- Download -->
      <section class="card" data-testid="section-download">
        <h2>File Download</h2>
        <p style="font-size:.875rem;color:var(--text-muted);margin-bottom:.75rem">Download a CSV file of all todos.</p>
        <a href="/api/export" class="btn btn-primary" data-testid="btn-download" download="todos.csv">
          ⬇ Download todos.csv
        </a>
      </section>
    </div>

    <!-- iFrame -->
    <section class="card" style="margin-bottom:1rem" data-testid="section-iframe">
      <h2>iFrame Embed</h2>
      <p style="font-size:.875rem;color:var(--text-muted);margin-bottom:.75rem">The /todos page embedded inside an iframe.</p>
      <iframe src="/todos" width="100%" height="400" data-testid="embedded-iframe"
              title="Todos page embedded"
              style="border:1px solid var(--border);border-radius:.375rem"></iframe>
    </section>

    <!-- Shadow DOM -->
    <section class="card" data-testid="section-shadow-dom">
      <h2>Shadow DOM Component</h2>
      <p style="font-size:.875rem;color:var(--text-muted);margin-bottom:.75rem">
        A custom web component with shadow root. Regular CSS and querySelector cannot reach inside.
      </p>
      <my-counter data-testid="shadow-counter"></my-counter>
    </section>
  </main>

  <script src="/client.js"></script>
  <script>
    // File upload handler
    document.getElementById('upload-form').addEventListener('submit', async (e) => {
      e.preventDefault();
      const file = document.getElementById('file-input').files[0];
      if (!file) return;
      const result = document.getElementById('upload-result');
      result.textContent = `✅ Uploaded: ${file.name} (${(file.size / 1024).toFixed(1)} KB)`;
      result.style.display = 'block';
    });

    // Shadow DOM custom element
    class MyCounter extends HTMLElement {
      constructor() {
        super();
        this._count = 0;
        const shadow = this.attachShadow({ mode: 'open' });
        shadow.innerHTML = `
          <style>
            :host { display: inline-block; }
            .counter { display: flex; align-items: center; gap: 1rem; padding: 1rem; background: #f0f4ff; border-radius: .5rem; }
            .count { font-size: 2rem; font-weight: 700; min-width: 3rem; text-align: center; color: #2563eb; }
            button { padding: .4rem .9rem; font-size: 1.25rem; border: none; border-radius: .375rem; cursor: pointer; background: #2563eb; color: #fff; }
          </style>
          <div class="counter">
            <button id="dec" aria-label="Decrement">−</button>
            <span class="count" id="count">0</span>
            <button id="inc" aria-label="Increment">+</button>
          </div>`;
        shadow.getElementById('inc').addEventListener('click', () => { this._count++; shadow.getElementById('count').textContent = this._count; });
        shadow.getElementById('dec').addEventListener('click', () => { this._count--; shadow.getElementById('count').textContent = this._count; });
      }
    }
    customElements.define('my-counter', MyCounter);
  </script>
</body>
</html>
```

- [ ] **Step 2: Commit**

```bash
git add public/advanced.html
git commit -m "feat: add advanced page (popup, iframe, dialogs, shadow DOM, download)"
```

---

### Task 13: Visual + Admin Pages

**Files:**
- Create: `playwright-course-app/public/visual.html`
- Create: `playwright-course-app/public/admin.html`

- [ ] **Step 1: สร้าง visual.html**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"><title>Visual Testing — Playwright Course App</title>
  <link rel="stylesheet" href="/style.css">
</head>
<body>
  <div id="main-nav"></div>
  <main>
    <h1 data-testid="visual-heading">Visual Testing Page</h1>
    <p style="color:var(--text-muted);margin-bottom:1.5rem">Stable layout for screenshot baseline comparison.</p>

    <div style="display:flex;align-items:center;gap:1rem;margin-bottom:2rem">
      <label for="theme-toggle" style="font-weight:600">Theme:</label>
      <button id="theme-toggle" data-testid="btn-theme-toggle" class="btn btn-secondary"
              onclick="toggleTheme()" aria-label="Toggle dark/light theme">
        Switch to Dark Mode
      </button>
      <span id="current-theme" data-testid="current-theme">Current: Light</span>
    </div>

    <div class="card-grid" data-testid="visual-grid">
      <div class="card" data-testid="visual-card-1" style="text-align:center">
        <div style="font-size:3rem">🎭</div>
        <h2>Playwright</h2>
        <p>Reliable end-to-end testing</p>
        <span class="badge">v1.50+</span>
      </div>
      <div class="card" data-testid="visual-card-2" style="text-align:center">
        <div style="font-size:3rem">📘</div>
        <h2>TypeScript</h2>
        <p>Type-safe test code</p>
        <span class="badge" style="background:var(--success)">First-class</span>
      </div>
      <div class="card" data-testid="visual-card-3" style="text-align:center">
        <div style="font-size:3rem">⚡</div>
        <h2>Fast & Reliable</h2>
        <p>Auto-waiting built in</p>
        <span class="badge" style="background:#7c3aed">Auto-wait</span>
      </div>
    </div>

    <div class="card" style="margin-top:1rem" data-testid="visual-text-block">
      <h2>Stable Content Block</h2>
      <p>This section has predictable content for visual regression testing.</p>
      <p>Line 2: consistent text that should not change between runs.</p>
      <p>Line 3: no timestamps or dynamic data here.</p>
    </div>
  </main>

  <script src="/client.js"></script>
  <script>
    function toggleTheme() {
      const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
      document.documentElement.setAttribute('data-theme', isDark ? '' : 'dark');
      document.getElementById('theme-toggle').textContent = isDark ? 'Switch to Dark Mode' : 'Switch to Light Mode';
      document.getElementById('current-theme').textContent = `Current: ${isDark ? 'Light' : 'Dark'}`;
    }
  </script>
</body>
</html>
```

- [ ] **Step 2: สร้าง admin.html**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"><title>Admin — Playwright Course App</title>
  <link rel="stylesheet" href="/style.css">
</head>
<body>
  <div id="main-nav"></div>
  <main>
    <h1 data-testid="admin-heading">Admin Dashboard</h1>
    <div id="admin-content" data-testid="admin-content">
      <p style="color:var(--text-muted)">Loading...</p>
    </div>
  </main>

  <script src="/client.js"></script>
  <script>
    document.addEventListener('DOMContentLoaded', async () => {
      if (!Auth.getToken()) {
        location.href = '/login?redirect=/admin&unauthorized=1';
        return;
      }
      try {
        const data = await apiFetch('/api/admin');
        document.getElementById('admin-content').innerHTML = `
          <div class="card-grid">
            <div class="card" data-testid="stat-users"><h2>${data.stats.users}</h2><p>Users</p></div>
            <div class="card" data-testid="stat-products"><h2>${data.stats.products}</h2><p>Products</p></div>
            <div class="card" data-testid="stat-todos"><h2>${data.stats.todos}</h2><p>Todos</p></div>
            <div class="card" data-testid="stat-orders"><h2>${data.stats.orders}</h2><p>Orders</p></div>
          </div>
          <p style="margin-top:1rem;color:var(--text-muted)" data-testid="admin-welcome">
            Welcome, admin. You have full access.
          </p>`;
      } catch (err) {
        if (err.status === 403) {
          document.getElementById('admin-content').innerHTML = '<div class="alert alert-error" data-testid="access-denied">Access denied. Admin role required.</div>';
        } else {
          location.href = '/login?redirect=/admin&unauthorized=1';
        }
      }
    });
  </script>
</body>
</html>
```

- [ ] **Step 3: Commit**

```bash
git add public/visual.html public/admin.html
git commit -m "feat: add visual testing and admin pages"
```

---

### Task 14: README

**Files:**
- Create: `playwright-course-app/README.md`

- [ ] **Step 1: สร้าง README.md**

````markdown
# Playwright Course App

Demo web application for the **Playwright TypeScript Master Course**.

## Quick Start

```bash
npm install
npm start
# → http://localhost:3000
```

Requires Node.js 20+.

## Users

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | admin |
| testuser | test123 | user |

## Pages

| URL | Description |
|-----|-------------|
| `/` | Dashboard |
| `/login` | Login form |
| `/todos` | Todo CRUD (double-click to edit, drag-drop sort) |
| `/shop` | Product catalog (filter, search, pagination) |
| `/cart` | Shopping cart |
| `/checkout` | Multi-step checkout wizard |
| `/components` | UI kitchen sink (all element types) |
| `/advanced` | Popup, iframe, file upload, dialogs, Shadow DOM |
| `/visual` | Theme switcher for visual regression testing |
| `/admin` | Admin-only dashboard (requires admin JWT) |

## Public API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/todos` | List todos |
| POST | `/api/todos` | Create todo `{"text":"..."}` |
| PATCH | `/api/todos/:id` | Update todo |
| DELETE | `/api/todos/:id` | Delete todo |
| POST | `/api/reset` | Reset todos to empty |
| GET | `/api/products` | Products `?search=&category=&page=&limit=` |
| GET | `/api/products/:id` | Single product |
| GET | `/api/slow?delay=ms` | Simulated slow response |
| GET | `/api/error` | Returns 500 error |
| GET | `/api/flaky` | Random 50% failure |
| GET | `/api/export` | Download todos.csv |
| WS | `ws://localhost:3000/ws` | WebSocket (ping every 3s) |

## Protected API Endpoints (require `Authorization: Bearer <token>`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login → `{"token":"..."}` |
| GET | `/api/me` | Current user info |
| GET | `/api/admin` | Admin stats (admin role only) |
| POST | `/api/orders` | Create order |
````

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: add README with setup and endpoint reference"
```

---

### Task 15: Smoke Test — ทดสอบทุก endpoint

**Files:** ไม่มี (verification only)

- [ ] **Step 1: Start server และทดสอบ API ครบทุก endpoint**

```bash
node server.js &
PID=$!

# Auth
curl -s -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | grep -o '"token":"[^"]*"' | head -c 50
# Expected: "token":"eyJ...

# Get token
TOKEN=$(curl -s -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | node -e "let d='';process.stdin.on('data',c=>d+=c);process.stdin.on('end',()=>console.log(JSON.parse(d).token))")

# Todos CRUD
curl -s -X POST http://localhost:3000/api/todos -H "Content-Type: application/json" -d '{"text":"Test todo"}'
# Expected: {"id":1,"text":"Test todo","completed":false,...}

curl -s http://localhost:3000/api/todos
# Expected: [{"id":1,...}]

curl -s -X PATCH http://localhost:3000/api/todos/1 -H "Content-Type: application/json" -d '{"completed":true}'
curl -s -X DELETE http://localhost:3000/api/todos/1
curl -s -X POST http://localhost:3000/api/reset -H "Content-Type: application/json"
# Expected: {"success":true}

# Products
curl -s "http://localhost:3000/api/products?page=1&limit=5" | node -e "let d='';process.stdin.on('data',c=>d+=c);process.stdin.on('end',()=>console.log(JSON.parse(d).total))"
# Expected: 10

curl -s "http://localhost:3000/api/products?category=Electronics" | node -e "let d='';process.stdin.on('data',c=>d+=c);process.stdin.on('end',()=>console.log(JSON.parse(d).data.length))"
# Expected: 4

# Protected endpoints
curl -s http://localhost:3000/api/me -H "Authorization: Bearer $TOKEN"
# Expected: {"id":1,"username":"admin","role":"admin"}

curl -s http://localhost:3000/api/admin -H "Authorization: Bearer $TOKEN"
# Expected: {"stats":{"users":2,"products":10,"todos":0,"orders":0}}

# Utility
curl -s "http://localhost:3000/api/slow?delay=100"
# Expected: {"message":"slow response after 100ms"}

curl -s http://localhost:3000/api/error
# Expected: {"error":"Internal Server Error"} with status 500

# Export
curl -s -I http://localhost:3000/api/export | grep Content-Disposition
# Expected: Content-Disposition: attachment; filename="todos.csv"

kill $PID
```

- [ ] **Step 2: ทดสอบ pages ใน browser**

เปิด `node server.js` แล้วตรวจในบน browser:
- [ ] `http://localhost:3000/` — Dashboard แสดง cards ครบ
- [ ] `http://localhost:3000/login` — form login ทำงาน, session badge อัพเดท
- [ ] `http://localhost:3000/todos` — CRUD, double-click edit, count badge
- [ ] `http://localhost:3000/shop` — products 10 ชิ้น, pagination 2 หน้า, filter/search ทำงาน
- [ ] `http://localhost:3000/components` — accordion, tabs, table sort, drag list ทำงาน
- [ ] `http://localhost:3000/advanced` — shadow DOM counter +/-, popup, dialogs ทำงาน
- [ ] `http://localhost:3000/visual` — theme toggle เปลี่ยน light/dark ได้
- [ ] `http://localhost:3000/admin` — redirect ถ้าไม่ได้ login, แสดง stats เมื่อ login เป็น admin

- [ ] **Step 3: Final commit**

```bash
git add -A
git commit -m "feat: complete playwright-course-app smoke test passed"
```

---

## Self-Review

**Spec coverage check:**

| Spec requirement | Task |
|-----------------|------|
| session badge "Logged in as: [username]" | Task 7 (client.js + index.html) |
| todo count badge `data-testid="todo-count"` | Task 8 |
| double-click to edit todo | Task 8 |
| `/api/todos` public (no auth) | Task 4 |
| `/api/admin` protected + admin role | Task 4 |
| JWT response `{ token }` | Task 4 |
| WebSocket ping every 3s + todo_updated | Task 4 |
| 10 products, 3 categories, pagination 5/page | Task 2 |
| `/api/reset` | Task 4 |
| `/api/flaky` 50% fail | Task 4 |
| `/api/export` CSV download | Task 4 |
| Multi-step checkout wizard | Task 10 |
| Shadow DOM web component | Task 12 |
| iframe embed `/todos` | Task 12 |
| popup trigger | Task 12 |
| alert/confirm/prompt triggers | Task 12 |
| file upload form | Task 12 |
| download button | Task 12 |
| theme switcher light/dark | Task 13 |
| admin page redirect if not admin | Task 13 |
| ARIA roles, labels, alt text, data-testid on all elements | All HTML tasks |
| `middleware/auth.js` separate file | Task 3 |
| `client.js` (not `app.js`) | Task 6 |
| `data/db.json` initial data | Task 2 |

**Placeholder scan:** ไม่พบ TBD หรือ incomplete steps ✅

**Type consistency:** `apiFetch`, `Auth`, `readDb`, `writeDb` ใช้สม่ำเสมอตลอดทุก task ✅
