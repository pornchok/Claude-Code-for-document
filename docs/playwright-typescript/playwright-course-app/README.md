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
| `/checkout` | Multi-step checkout wizard (3 steps) |
| `/components` | UI kitchen sink (all element types) |
| `/advanced` | Popup, iframe, file upload, dialogs, Shadow DOM |
| `/visual` | Theme switcher for visual regression testing |
| `/admin` | Admin-only dashboard (requires admin JWT) |

## Public API Endpoints (no auth required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/todos` | List todos |
| POST | `/api/todos` | Create todo `{"text":"..."}` |
| PATCH | `/api/todos/:id` | Update todo |
| DELETE | `/api/todos/:id` | Delete todo |
| POST | `/api/reset` | Reset todos to empty state |
| GET | `/api/products` | Products `?search=&category=&page=&limit=` |
| GET | `/api/products/:id` | Single product |
| GET | `/api/slow?delay=ms` | Simulated slow response (default 2000ms) |
| GET | `/api/error` | Returns 500 error |
| GET | `/api/flaky` | Random 50% failure |
| GET | `/api/export` | Download todos as CSV |
| WS | `ws://localhost:3000/ws` | WebSocket (ping every 3s + todo_updated events) |

## Protected API Endpoints (require `Authorization: Bearer <token>`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login → `{"token":"..."}` |
| GET | `/api/me` | Current user info |
| GET | `/api/admin` | Admin stats (admin role only, returns 403 for users) |
| POST | `/api/orders` | Create order |

## WebSocket Events

Connect to `ws://localhost:3000/ws` to receive:
- `{ type: "connected", message: "..." }` — on connect
- `{ type: "ping", timestamp: "..." }` — every 3 seconds
- `{ type: "todo_updated", data: {...} }` — when a todo is created or updated
