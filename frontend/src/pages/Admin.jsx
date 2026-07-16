import { CheckCircle2, KeyRound, LogIn, RefreshCw, ShieldCheck, Users } from "lucide-react";
import { useState } from "react";
import BankService from "../api/BankService";

// Reads saved admin login data so the admin screen survives a browser refresh.
function readSessionJson(key) {
  try {
    return JSON.parse(sessionStorage.getItem(key) || "null");
  } catch {
    // Bad old browser data should not crash the admin page.
    return null;
  }
}

function formatDate(value) {
  return value ? new Date(value).toLocaleString() : "Not available";
}

export default function Admin() {
  // This demo admin screen uses the same JWT login endpoint as the user dashboard.
  const [adminToken, setAdminToken] = useState(() => sessionStorage.getItem("adminAuthToken") || "");
  const [adminUser, setAdminUser] = useState(() => readSessionJson("adminAuthUser"));
  const [users, setUsers] = useState([]);
  const [accessProof, setAccessProof] = useState(null);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  function storeAdminSession(login) {
    setAdminToken(login.token);
    setAdminUser(login);
    sessionStorage.setItem("adminAuthToken", login.token);
    sessionStorage.setItem("adminAuthUser", JSON.stringify(login));
  }

  async function handleAdminLogin(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setMessage("");
    setLoading(true);

    try {
      const login = await BankService.login({
        email: form.get("username"),
        password: form.get("password")
      });
      storeAdminSession(login);
      setAccessProof(null);
      setMessage("Admin login successful.");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleVerifyAdmin() {
    setMessage("");
    setLoading(true);

    try {
      if (!adminToken) {
        throw new Error("Admin login is required before testing the protected endpoint.");
      }
      const result = await BankService.verifyAdmin(adminToken);
      setAccessProof(result);
      setMessage("JWT accepted. ROLE_ADMIN access verified.");
    } catch (error) {
      setAccessProof(null);
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleFetchUsers() {
    setMessage("");
    setLoading(true);

    try {
      if (!adminToken) {
        throw new Error("Admin login is required before fetching users.");
      }
      const result = await BankService.getUsers(adminToken);
      setUsers(Array.isArray(result) ? result : []);
      setMessage("Users loaded from UserController.");
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  function handleAdminLogout() {
    setAdminToken("");
    setAdminUser(null);
    setUsers([]);
    setAccessProof(null);
    sessionStorage.removeItem("adminAuthToken");
    sessionStorage.removeItem("adminAuthUser");
    setMessage("Admin session cleared.");
  }

  return (
    <main className="page-shell">
      <section className="page-panel admin-panel">
        <div className="admin-heading">
          <div>
            <p className="eyebrow">JWT Security Lab</p>
            <h2>Admin Command Center</h2>
          </div>
          <span className={`status-chip ${adminUser ? "authenticated" : ""}`.trim()}>
            {adminUser ? "JWT session active" : "Authentication required"}
          </span>
        </div>
        <p>
          Sign in, prove ROLE_ADMIN access against the protected endpoint, then fetch safe user records.
        </p>

        <div className="security-flow" aria-label="JWT authorization flow">
          <div><span>1</span><strong>Login</strong><small>POST /api/auth/login</small></div>
          <div><span>2</span><strong>Bearer JWT</strong><small>Signed and time-limited</small></div>
          <div><span>3</span><strong>ROLE_ADMIN</strong><small>POST /admin</small></div>
        </div>

        <div className="admin-layout">
          <form className="admin-login-card" onSubmit={handleAdminLogin}>
            <div className="panel-header">
              <span className="panel-icon" aria-hidden="true">
                <ShieldCheck size={18} />
              </span>
              <div>
                <h3>Admin Access</h3>
                <p>Demo credentials: admin / admin123</p>
              </div>
            </div>

            <label>
              <span>Username</span>
              <input name="username" type="text" defaultValue="admin" required />
            </label>
            <label>
              <span>Password</span>
              <input name="password" type="password" minLength="6" defaultValue="admin123" required />
            </label>

            <button className="icon-button" type="submit" disabled={loading}>
              <LogIn size={17} aria-hidden="true" />
              Admin Login
            </button>
          </form>

          <section className="admin-service-card admin-proof-card">
            <div className="panel-header">
              <span className="panel-icon" aria-hidden="true">
                <KeyRound size={18} />
              </span>
              <div>
                <h3>Protected Endpoint Check</h3>
                <p>Sends your Bearer token to POST /admin and verifies ROLE_ADMIN.</p>
              </div>
            </div>

            <div className="admin-status">
              <span className="status-chip">
                {adminUser ? `Signed in: ${adminUser.name}` : "Not signed in"}
              </span>
              {adminUser ? (
                <button className="secondary-button" type="button" onClick={handleAdminLogout}>
                  Clear Admin Session
                </button>
              ) : null}
            </div>

            <button className="icon-button" type="button" onClick={handleVerifyAdmin} disabled={loading || !adminToken}>
              <ShieldCheck size={17} aria-hidden="true" />
              Verify JWT Access
            </button>

            {accessProof ? (
              <div className="access-proof" role="status">
                <CheckCircle2 size={20} aria-hidden="true" />
                <div>
                  <strong>{accessProof.message}</strong>
                  <span>{accessProof.role}</span>
                </div>
              </div>
            ) : null}

            <p className={`form-message ${message.includes("successful") || message.includes("loaded") || message.includes("verified") ? "success" : "error"}`.trim()}>
              {message}
            </p>
          </section>
        </div>

        <section className="admin-users-section">
          <div className="admin-users-heading">
            <div className="panel-header">
              <span className="panel-icon" aria-hidden="true"><Users size={18} /></span>
              <div>
                <h3>UserController Service</h3>
                <p>Calls GET /admin/users and displays safe DTOs without password hashes.</p>
              </div>
            </div>
            <button className="icon-button" type="button" onClick={handleFetchUsers} disabled={loading || !adminToken}>
              <RefreshCw size={17} aria-hidden="true" />
              Fetch All Users
            </button>
          </div>

          <div className="table-wrap admin-users-table">
            <table>
              <thead>
                <tr>
                  <th>User ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {users.length ? (
                  users.map(user => (
                    <tr key={user.userId}>
                      <td>{user.userId}</td>
                      <td>{user.name}</td>
                      <td>{user.email}</td>
                      <td>{formatDate(user.createdAt)}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td className="empty-cell" colSpan="4">
                      Sign in, then fetch the registered users.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      </section>
    </main>
  );
}
