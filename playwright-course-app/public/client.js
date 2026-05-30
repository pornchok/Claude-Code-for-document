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
