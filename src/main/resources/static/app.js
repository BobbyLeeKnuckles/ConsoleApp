const API_BASE = "/api/accounts";
const AUTH_BASE = "/api/auth";

let selectedAccount = null;
let authToken = sessionStorage.getItem("authToken");
let authUser = JSON.parse(sessionStorage.getItem("authUser") || "null");
let transactionPage = 0;
let totalTransactionPages = 1;

const elements = {
	loginForm: document.getElementById("loginForm"),
	createAccountForm: document.getElementById("createAccountForm"),
	lookupForm: document.getElementById("lookupForm"),
	depositForm: document.getElementById("depositForm"),
	withdrawForm: document.getElementById("withdrawForm"),
	renameForm: document.getElementById("renameForm"),
	transferForm: document.getElementById("transferForm"),
	refreshAccounts: document.getElementById("refreshAccounts"),
	loginStatus: document.getElementById("loginStatus"),
	loginMessage: document.getElementById("loginMessage"),
	createMessage: document.getElementById("createMessage"),
	accountId: document.getElementById("accountId"),
	accountDetails: document.getElementById("accountDetails"),
	accountsList: document.getElementById("accountsList"),
	transactionsBody: document.getElementById("transactionsBody"),
	transactionPageLabel: document.getElementById("transactionPageLabel"),
	prevTransactions: document.getElementById("prevTransactions"),
	nextTransactions: document.getElementById("nextTransactions"),
	toast: document.getElementById("toast")
};

async function request(base, path, options = {}) {
	const headers = {
		"Content-Type": "application/json",
		...(authToken ? { "X-Auth-Token": authToken } : {}),
		...(options.headers || {})
	};
	const response = await fetch(`${base}${path}`, {
		...options,
		headers
	});

	if (!response.ok) {
		let message = `Request failed with status ${response.status}.`;
		try {
			const errorBody = await response.json();
			message = errorBody.message || errorBody.error || message;
		} catch {
			// Keep the generic message when the response is not JSON.
		}
		throw new Error(message);
	}

	return response.status === 204 ? null : response.json();
}

async function accountRequest(path, options = {}) {
	return request(API_BASE, path, options);
}

async function authRequest(path, options = {}) {
	const response = await fetch(`${AUTH_BASE}${path}`, {
		headers: {
			"Content-Type": "application/json",
			...(options.headers || {})
		},
		...options
	});

	if (!response.ok) {
		let message = `Request failed with status ${response.status}.`;
		try {
			const errorBody = await response.json();
			message = errorBody.message || errorBody.error || message;
		} catch {
			// Keep the generic message when the response is not JSON.
		}
		throw new Error(message);
	}

	return response.status === 204 ? null : response.json();
}

function money(value) {
	return Number(value || 0).toLocaleString(undefined, {
		style: "currency",
		currency: "USD"
	});
}

function dateTime(value) {
	return value ? new Date(value).toLocaleString() : "Not available";
}

function showToast(message, isError = false) {
	elements.toast.textContent = message;
	elements.toast.classList.toggle("error", isError);
	elements.toast.classList.add("visible");
	window.setTimeout(() => elements.toast.classList.remove("visible"), 2800);
}

function setMessage(element, message, type = "") {
	element.textContent = message;
	element.className = `form-message ${type}`.trim();
}

function updateLoginStatus() {
	if (authUser) {
		elements.loginStatus.textContent = `Logged in: ${authUser.name}`;
		return;
	}
	elements.loginStatus.textContent = "Not logged in";
}

function renderAccount(account) {
	selectedAccount = account;
	elements.accountId.value = account.accountId;
	elements.accountDetails.classList.remove("empty-state");
	elements.accountDetails.innerHTML = `
		<div class="detail-item">
			<span class="detail-label">Account ID</span>
			<span class="detail-value">${account.accountId}</span>
		</div>
		<div class="detail-item">
			<span class="detail-label">User Name</span>
			<span class="detail-value">${account.userName}</span>
		</div>
		<div class="detail-item">
			<span class="detail-label">Email</span>
			<span class="detail-value">${account.email}</span>
		</div>
		<div class="detail-item">
			<span class="detail-label">Type</span>
			<span class="detail-value">${account.accountType}</span>
		</div>
		<div class="detail-item">
			<span class="detail-label">Balance</span>
			<span class="detail-value">${money(account.balance)}</span>
		</div>
		<div class="detail-item">
			<span class="detail-label">Created</span>
			<span class="detail-value">${dateTime(account.createdAt)}</span>
		</div>
	`;
}

function renderAccounts(accounts) {
	if (!accounts.length) {
		elements.accountsList.className = "accounts-list empty-state";
		elements.accountsList.textContent = "No accounts found.";
		return;
	}

	elements.accountsList.className = "accounts-list";
	elements.accountsList.innerHTML = accounts.map(account => `
		<button class="account-row" type="button" data-id="${account.accountId}">
			<strong>${account.userName}</strong>
			<span>${account.accountType} - ${money(account.balance)}</span>
			<span>${account.accountId}</span>
		</button>
	`).join("");
}

function renderTransactions(transactions) {
	if (!transactions.length) {
		elements.transactionsBody.innerHTML = `
			<tr>
				<td colspan="4" class="empty-cell">No transactions yet.</td>
			</tr>
		`;
		return;
	}

	elements.transactionsBody.innerHTML = transactions.map(transaction => `
		<tr>
			<td>${transaction.transactionId}</td>
			<td>${transaction.type}</td>
			<td>${money(transaction.amount)}</td>
			<td>${dateTime(transaction.date)}</td>
		</tr>
	`).join("");
}

function renderTransactionPage(page) {
	totalTransactionPages = page.totalPages || 1;
	transactionPage = page.number || 0;
	renderTransactions(page.content || []);
	elements.transactionPageLabel.textContent = `Page ${transactionPage + 1} of ${totalTransactionPages}`;
	elements.prevTransactions.disabled = transactionPage <= 0;
	elements.nextTransactions.disabled = transactionPage + 1 >= totalTransactionPages;
}

async function loadAccounts() {
	const accounts = await accountRequest("");
	renderAccounts(accounts);
}

async function loadAccount(accountId) {
	const account = await accountRequest(`/${encodeURIComponent(accountId)}`);
	renderAccount(account);
	await loadTransactions(0);
	return account;
}

async function loadTransactions(page) {
	if (!selectedAccount) {
		return;
	}
	const result = await accountRequest(`/${encodeURIComponent(selectedAccount.accountId)}/transactions?page=${page}&size=5`);
	renderTransactionPage(result);
}

function currentAccountId() {
	if (!selectedAccount) {
		throw new Error("Select or create an account first.");
	}
	return selectedAccount.accountId;
}

elements.loginForm.addEventListener("submit", async event => {
	event.preventDefault();
	setMessage(elements.loginMessage, "");
	try {
		const result = await authRequest("/login", {
			method: "POST",
			body: JSON.stringify({
				email: document.getElementById("loginEmail").value,
				password: document.getElementById("loginPassword").value
			})
		});
		authToken = result.token;
		authUser = result;
		sessionStorage.setItem("authToken", authToken);
		sessionStorage.setItem("authUser", JSON.stringify(authUser));
		updateLoginStatus();
		setMessage(elements.loginMessage, "Login successful.", "success");
		await loadAccounts();
		showToast("Logged in.");
	} catch (error) {
		setMessage(elements.loginMessage, error.message, "error");
		showToast(error.message, true);
	}
});

elements.createAccountForm.addEventListener("submit", async event => {
	event.preventDefault();
	setMessage(elements.createMessage, "");
	const body = {
		name: document.getElementById("name").value,
		email: document.getElementById("email").value,
		password: document.getElementById("password").value,
		accountType: document.getElementById("accountType").value
	};

	try {
		const account = await accountRequest("", {
			method: "POST",
			body: JSON.stringify(body)
		});
		const login = await authRequest("/login", {
			method: "POST",
			body: JSON.stringify({ email: body.email, password: body.password })
		});
		authToken = login.token;
		authUser = login;
		sessionStorage.setItem("authToken", authToken);
		sessionStorage.setItem("authUser", JSON.stringify(authUser));
		updateLoginStatus();
		renderAccount(account);
		renderTransactionPage({ content: [], number: 0, totalPages: 1 });
		await loadAccounts();
		elements.createAccountForm.reset();
		setMessage(elements.createMessage, "Account created and logged in.", "success");
		showToast("Account created.");
	} catch (error) {
		setMessage(elements.createMessage, error.message, "error");
		showToast(error.message, true);
	}
});

elements.lookupForm.addEventListener("submit", async event => {
	event.preventDefault();
	try {
		await loadAccount(elements.accountId.value.trim());
		showToast("Account loaded.");
	} catch (error) {
		showToast(error.message, true);
	}
});

elements.depositForm.addEventListener("submit", async event => {
	event.preventDefault();
	try {
		const accountId = currentAccountId();
		const account = await accountRequest(`/${encodeURIComponent(accountId)}/deposit`, {
			method: "POST",
			body: JSON.stringify({ amount: document.getElementById("depositAmount").value })
		});
		renderAccount(account);
		await loadTransactions(0);
		elements.depositForm.reset();
		await loadAccounts();
		showToast("Deposit complete.");
	} catch (error) {
		showToast(error.message, true);
	}
});

elements.withdrawForm.addEventListener("submit", async event => {
	event.preventDefault();
	try {
		const accountId = currentAccountId();
		const account = await accountRequest(`/${encodeURIComponent(accountId)}/withdraw`, {
			method: "POST",
			body: JSON.stringify({ amount: document.getElementById("withdrawAmount").value })
		});
		renderAccount(account);
		await loadTransactions(0);
		elements.withdrawForm.reset();
		await loadAccounts();
		showToast("Withdrawal complete.");
	} catch (error) {
		showToast(error.message, true);
	}
});

elements.renameForm.addEventListener("submit", async event => {
	event.preventDefault();
	try {
		const accountId = currentAccountId();
		const account = await accountRequest(`/${encodeURIComponent(accountId)}/user`, {
			method: "PUT",
			body: JSON.stringify({ name: document.getElementById("newName").value })
		});
		renderAccount(account);
		elements.renameForm.reset();
		await loadAccounts();
		showToast("Name updated.");
	} catch (error) {
		showToast(error.message, true);
	}
});

elements.transferForm.addEventListener("submit", async event => {
	event.preventDefault();
	try {
		const accountId = currentAccountId();
		const account = await accountRequest(`/${encodeURIComponent(accountId)}/transfer`, {
			method: "POST",
			body: JSON.stringify({
				receiverAccountId: document.getElementById("receiverAccountId").value,
				amount: document.getElementById("transferAmount").value
			})
		});
		renderAccount(account);
		await loadTransactions(0);
		elements.transferForm.reset();
		await loadAccounts();
		showToast("Transfer complete.");
	} catch (error) {
		showToast(error.message, true);
	}
});

elements.accountsList.addEventListener("click", async event => {
	const button = event.target.closest("[data-id]");
	if (!button) {
		return;
	}
	try {
		await loadAccount(button.dataset.id);
		showToast("Account loaded.");
	} catch (error) {
		showToast(error.message, true);
	}
});

elements.refreshAccounts.addEventListener("click", async () => {
	try {
		await loadAccounts();
		showToast("Accounts refreshed.");
	} catch (error) {
		showToast(error.message, true);
	}
});

elements.prevTransactions.addEventListener("click", () => {
	loadTransactions(transactionPage - 1).catch(error => showToast(error.message, true));
});

elements.nextTransactions.addEventListener("click", () => {
	loadTransactions(transactionPage + 1).catch(error => showToast(error.message, true));
});

updateLoginStatus();
if (authToken) {
	loadAccounts().catch(error => showToast(error.message, true));
}
