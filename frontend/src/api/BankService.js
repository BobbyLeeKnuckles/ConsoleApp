// Central place for REST calls to the Spring Boot backend.
const ACCOUNT_BASE = "/api/accounts";
const AUTH_BASE = "/api/auth";
const USER_BASE = "/admin/users";

function buildHeaders(token) {
  // All JSON API calls use the same content type, and logged-in calls include the JWT.
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  };
}

async function parseResponse(response) {
  if (response.status === 204) {
    return null;
  }

  // Backend errors are also JSON, so the frontend can show the server's message directly.
  const body = await response.json();
  if (!response.ok) {
    throw new Error(body.message || body.error || `Request failed with status ${response.status}.`);
  }

  return body;
}

async function request(url, options = {}, token) {
  // This wrapper keeps fetch setup in one place instead of repeating it in every component.
  const response = await fetch(url, {
    ...options,
    headers: {
      ...buildHeaders(token),
      ...(options.headers || {})
    }
  });

  return parseResponse(response);
}

const BankService = {
  login(payload) {
    return request(`${AUTH_BASE}/login`, {
      method: "POST",
      body: JSON.stringify(payload)
    });
  },

  getAccounts(token) {
    // GET /api/accounts returns the account picker list for the dashboard.
    return request(ACCOUNT_BASE, {}, token);
  },

  getAccount(accountId, token) {
    // encodeURIComponent keeps special characters in IDs from breaking the URL.
    return request(`${ACCOUNT_BASE}/${encodeURIComponent(accountId)}`, {}, token);
  },

  createAccount(payload) {
    return request(ACCOUNT_BASE, {
      method: "POST",
      body: JSON.stringify(payload)
    });
  },

  updateAccountUser(accountId, payload, token) {
    return request(`${ACCOUNT_BASE}/${encodeURIComponent(accountId)}/user`, {
      method: "PUT",
      body: JSON.stringify(payload)
    }, token);
  },

  deposit(accountId, amount, token) {
    return request(`${ACCOUNT_BASE}/${encodeURIComponent(accountId)}/deposit`, {
      method: "POST",
      body: JSON.stringify({ amount })
    }, token);
  },

  withdraw(accountId, amount, token) {
    return request(`${ACCOUNT_BASE}/${encodeURIComponent(accountId)}/withdraw`, {
      method: "POST",
      body: JSON.stringify({ amount })
    }, token);
  },

  transfer(accountId, payload, token) {
    return request(`${ACCOUNT_BASE}/${encodeURIComponent(accountId)}/transfer`, {
      method: "POST",
      body: JSON.stringify(payload)
    }, token);
  },

  getTransactions(accountId, page = 0, size = 5, token) {
    return request(
      `${ACCOUNT_BASE}/${encodeURIComponent(accountId)}/transactions?page=${page}&size=${size}`,
      {},
      token
    );
  },

  getUsers(token) {
    // Admin page uses this to call UserController without exposing password hashes.
    return request(USER_BASE, {}, token);
  }
};

export default BankService;
