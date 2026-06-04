const express = require('express');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const fs = require('fs');
const path = require('path');
const http = require('http');
const { WebSocketServer } = require('ws');
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

// ── WebSocket broadcast ───────────────────────────────────────
function broadcast(data) {
  if (!global.wss) return;
  const msg = JSON.stringify(data);
  global.wss.clients.forEach(client => { if (client.readyState === 1) client.send(msg); });
}

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
  broadcast({ type: 'todo_updated', data: todo });
  res.status(201).json(todo);
});

app.patch('/api/todos/:id', (req, res) => {
  const db = readDb();
  const idx = db.todos.findIndex(t => t.id === Number(req.params.id));
  if (idx === -1) return res.status(404).json({ error: 'Todo not found' });
  Object.assign(db.todos[idx], req.body);
  writeDb(db);
  broadcast({ type: 'todo_updated', data: db.todos[idx] });
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
  const product = readDb().products.find(p => p.id === Number(req.params.id));
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

// ── Utility (public) ─────────────────────────────────────────
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

// ── HTTP + WebSocket Server ───────────────────────────────────
const server = http.createServer(app);
const wss = new WebSocketServer({ server });
global.wss = wss;

wss.on('connection', ws => {
  ws.send(JSON.stringify({ type: 'connected', message: 'Welcome to Playwright Course App WebSocket' }));
});

setInterval(() => broadcast({ type: 'ping', timestamp: new Date().toISOString() }), 3000);

server.listen(PORT, () => console.log(`Playwright Course App running at http://localhost:${PORT}`));
