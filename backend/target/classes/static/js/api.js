// Shared API helper — same origin (Spring Boot serves frontend + backend together)
async function api(path, options = {}) {
  const res = await fetch(path, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || ("Request failed: " + res.status));
  return data;
}

function currentUser() {
  try { return JSON.parse(localStorage.getItem("hisab_user")); }
  catch { return null; }
}

function requireAuth() {
  const u = currentUser();
  if (!u) { window.location.href = "index.html"; return null; }
  return u;
}

function currentMonth() {
  const d = new Date();
  return d.toISOString().slice(0, 7); // YYYY-MM
}

function rs(n) {
  return "₹" + Number(n || 0).toLocaleString("en-IN", { maximumFractionDigits: 2 });
}
