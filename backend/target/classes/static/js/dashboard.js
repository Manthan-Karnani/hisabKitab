const user = requireAuth();
document.getElementById("userPill").textContent = "👤 " + user.username;
document.getElementById("logoutBtn").onclick = () => {
  localStorage.removeItem("hisab_user");
  window.location.href = "index.html";
};

const monthPicker = document.getElementById("monthPicker");
monthPicker.value = currentMonth();
document.getElementById("expDate").value = new Date().toISOString().slice(0, 10);

let chart = null;
const COLORS = ["#166b45", "#b98a1d", "#a31621", "#23905f", "#d9ab2e", "#0a2e22", "#7a5c0e", "#4c8c6c", "#c96a2b", "#6f6a52"];

async function loadAll() {
  const month = monthPicker.value || currentMonth();
  await Promise.all([loadCategories(), loadBudget(month), loadExpenses(month), loadSummary(month)]);
}

async function loadCategories() {
  const cats = await api(`/api/categories?userId=${user.id}`);
  const sel = document.getElementById("expCategory");
  sel.innerHTML = cats.map((c) => `<option value="${c.id}">${c.name}</option>`).join("");
}

async function loadBudget(month) {
  const b = await api(`/api/budgets?userId=${user.id}&month=${month}`);
  document.getElementById("budgetInput").value = Number(b.amount) > 0 ? b.amount : "";
  return Number(b.amount) || 0;
}

async function loadExpenses(month) {
  const list = await api(`/api/expenses?userId=${user.id}&month=${month}`);
  const wrap = document.getElementById("expTableWrap");
  if (!list.length) {
    wrap.innerHTML = `<div class="empty">No expenses yet — add your first one!</div>`;
    return;
  }
  wrap.innerHTML =
    `<table><thead><tr><th>Date</th><th>Category</th><th>Note</th><th>Amount</th><th></th></tr></thead><tbody>` +
    list.map((e) => `<tr><td>${e.date}</td><td>${e.category}</td><td>${e.note || "—"}</td><td><b>${rs(e.amount)}</b></td>
      <td><button class="danger small" onclick="deleteExpense(${e.id})">Delete</button></td></tr>`).join("") +
    `</tbody></table>`;
}

async function loadSummary(month) {
  const s = await api(`/api/summary?userId=${user.id}&month=${month}`);
  document.getElementById("statBudget").textContent = rs(s.totalBudget);
  document.getElementById("statSpent").textContent = rs(s.totalSpent);
  document.getElementById("statLeft").textContent = rs(s.remaining);

  const pct = Number(s.totalBudget) > 0 ? Math.min(100, (Number(s.totalSpent) / Number(s.totalBudget)) * 100) : 0;
  document.getElementById("budgetBar").style.width = pct + "%";
  document.getElementById("budgetMsg").textContent =
    Number(s.totalBudget) > 0
      ? `Spent ${rs(s.totalSpent)} of ${rs(s.totalBudget)} budget — ${rs(s.remaining)} remaining.`
      : "No budget set for this month — set one above to track deductions.";

  const labels = s.byCategory.map((x) => x.category);
  const values = s.byCategory.map((x) => Number(x.total));
  const ctx = document.getElementById("catChart");
  if (chart) chart.destroy();
  chart = new Chart(ctx, {
    type: "doughnut",
    data: {
      labels,
      datasets: [{ data: values, backgroundColor: labels.map((_, i) => COLORS[i % COLORS.length]), borderWidth: 2 }],
    },
    options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: "bottom" } } },
  });
  document.getElementById("catLegend").innerHTML = labels.length
    ? labels.map((l, i) => `<div>● <b style="color:${COLORS[i % COLORS.length]}">${l}</b> — ${rs(values[i])}</div>`).join("")
    : `<div class="empty">No spending data for chart yet.</div>`;
}

async function deleteExpense(id) {
  if (!confirm("Delete this expense?")) return;
  await api(`/api/expenses/${id}?userId=${user.id}`, { method: "DELETE" });
  loadAll();
}

document.getElementById("saveBudgetBtn").onclick = async () => {
  const amount = document.getElementById("budgetInput").value;
  if (!amount || Number(amount) <= 0) return alert("Enter a valid budget amount.");
  await api("/api/budgets", {
    method: "POST",
    body: JSON.stringify({ userId: user.id, month: monthPicker.value, amount }),
  });
  loadAll();
};

document.getElementById("addCatBtn").onclick = async () => {
  const name = document.getElementById("newCat").value.trim();
  if (!name) return;
  try {
    await api(`/api/categories?userId=${user.id}`, { method: "POST", body: JSON.stringify({ name }) });
    document.getElementById("newCat").value = "";
    await loadCategories();
  } catch (e) { alert(e.message); }
};

document.getElementById("addExpBtn").onclick = async () => {
  const categoryId = document.getElementById("expCategory").value;
  const amount = document.getElementById("expAmount").value;
  const date = document.getElementById("expDate").value;
  const note = document.getElementById("expNote").value.trim();
  if (!categoryId || !amount || !date) return alert("Category, amount and date are required.");
  try {
    await api("/api/expenses", {
      method: "POST",
      body: JSON.stringify({ userId: user.id, categoryId, amount, date, note }),
    });
    document.getElementById("expAmount").value = "";
    document.getElementById("expNote").value = "";
    loadAll();
  } catch (e) { alert(e.message); }
};

monthPicker.onchange = loadAll;
loadAll();
