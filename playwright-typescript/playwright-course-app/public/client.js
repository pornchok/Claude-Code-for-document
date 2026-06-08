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

// ── Cart Helpers ──────────────────────────────────────────
const Cart = {
  get() { try { return JSON.parse(localStorage.getItem('cart') || '[]'); } catch { return []; } },
  set(items) { localStorage.setItem('cart', JSON.stringify(items)); },
  count() { return Cart.get().reduce((sum, i) => sum + (i.qty || 1), 0); }
};

// ── Session Badge ─────────────────────────────────────────
function updateSessionBadge() {
  const badge = document.getElementById('session-badge');
  if (!badge) return;
  const user = Auth.getUser();
  badge.textContent = user ? `Logged in as: ${user.username}` : 'Not logged in';
  badge.setAttribute('data-testid', 'session-badge');
}

// ── Cart Count Badge ──────────────────────────────────────
function updateCartCount() {
  const badge = document.getElementById('cart-count');
  if (!badge) return;
  const count = Cart.count();
  badge.textContent = count > 0 ? String(count) : '';
  badge.style.display = count > 0 ? 'inline-flex' : 'none';
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
function buildNav() {
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
  const cartCount = Cart.count();
  return `<nav role="navigation" aria-label="main navigation">
    <span class="nav-brand">🎭 Playwright Course App</span>
    ${pages.map(p => {
      if (p.label === 'Cart') {
        return `<a href="${p.href}" data-testid="nav-cart" style="position:relative">Cart<span id="cart-count" data-testid="cart-count" style="display:${cartCount > 0 ? 'inline-flex' : 'none'};position:absolute;top:-6px;right:-10px;background:#e53e3e;color:#fff;border-radius:50%;width:18px;height:18px;font-size:11px;align-items:center;justify-content:center;font-weight:700">${cartCount > 0 ? cartCount : ''}</span></a>`;
      }
      return `<a href="${p.href}" data-testid="nav-${p.label.toLowerCase()}">${p.label}</a>`;
    }).join('')}
    <span id="session-badge" data-testid="session-badge"></span>
    ${Auth.getToken() ? `<a href="#" onclick="Auth.removeToken();location.href='/'" data-testid="nav-logout">Logout</a>` : `<a href="/login" data-testid="nav-login">Login</a>`}
  </nav>`;
}

document.addEventListener('DOMContentLoaded', () => {
  const navEl = document.getElementById('main-nav');
  if (navEl) { navEl.innerHTML = buildNav(); updateSessionBadge(); updateCartCount(); }
  updateSessionBadge();
});
