function showError(msg) {
  const el = document.getElementById("err");
  el.textContent = msg;
  el.style.display = "block";
}

let mode = "login";
const tabLogin = document.getElementById("tabLogin");
const tabRegister = document.getElementById("tabRegister");
const submitBtn = document.getElementById("submitBtn");

function setMode(m) {
  mode = m;
  document.getElementById("err").style.display = "none";
  tabLogin.className = m === "login" ? "active" : "ghost";
  tabRegister.className = m === "register" ? "active" : "ghost";
  submitBtn.textContent = m === "login" ? "Login" : "Register";
}

tabLogin.onclick = () => setMode("login");
tabRegister.onclick = () => setMode("register");

if (currentUser()) window.location.href = "dashboard.html";

submitBtn.onclick = async () => {
  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;
  if (!username || !password) return showError("Please enter username and password.");
  submitBtn.disabled = true;
  try {
    const data = await api("/api/auth/" + mode, {
      method: "POST",
      body: JSON.stringify({ username, password }),
    });
    localStorage.setItem("hisab_user", JSON.stringify(data));
    window.location.href = "dashboard.html";
  } catch (e) {
    showError(e.message);
  } finally {
    submitBtn.disabled = false;
  }
};
