import {
  ArrowDownCircle,
  ArrowUpCircle,
  CreditCard,
  History,
  LogIn,
  Pencil,
  RefreshCw,
  Search,
  Send,
  ShieldCheck,
  UserPlus,
  Wallet
} from "lucide-react";
import { useEffect, useState } from "react";
import BankService from "../api/BankService";
import Account from "../models/Account";
import Transaction from "../models/Transaction";

// Starter frontend data. This lets the React UI show banking information before the backend is running.
const DEMO_ACCOUNTS = [
  Account.from({
    accountId: "DEMO-1001",
    userName: "Billy Huynh",
    email: "billy@example.com",
    accountType: "CHECKING",
    balance: 1240,
    createdAt: "2026-07-15T09:00:00"
  }),
  Account.from({
    accountId: "DEMO-1002",
    userName: "Simple Bank Savings",
    email: "savings@example.com",
    accountType: "SAVINGS",
    balance: 2750.5,
    createdAt: "2026-07-15T09:15:00"
  })
];

const DEMO_TRANSACTION_PAGE = Transaction.pageFrom({
  content: [
    {
      transactionId: "TXN-9001",
      type: "DEPOSIT",
      amount: 500,
      date: "2026-07-15T09:30:00"
    },
    {
      transactionId: "TXN-9002",
      type: "WITHDRAW",
      amount: 75,
      date: "2026-07-15T10:00:00"
    },
    {
      transactionId: "TXN-9003",
      type: "TRANSFER",
      amount: 150,
      date: "2026-07-15T10:30:00"
    }
  ],
  number: 0,
  totalPages: 1
});

// Reads saved login data from sessionStorage so a page refresh keeps the user signed in.
function readSessionJson(key) {
  try {
    return JSON.parse(sessionStorage.getItem(key) || "null");
  } catch {
    // If old browser data is malformed, fail safely and treat the user as logged out.
    return null;
  }
}

// Small formatting helpers keep the JSX cleaner and make money/date display consistent.
function formatMoney(value) {
  return Number(value || 0).toLocaleString(undefined, {
    style: "currency",
    currency: "USD"
  });
}

function formatDate(value) {
  return value ? new Date(value).toLocaleString() : "Not available";
}

// Reusable panel component. Each major screen area uses the same header structure.
function Panel({ icon: Icon, title, description, children, className = "" }) {
  return (
    <section className={`panel ${className}`.trim()}>
      <div className="panel-header">
        <span className="panel-icon" aria-hidden="true">
          <Icon size={18} />
        </span>
        <div>
          <h2>{title}</h2>
          <p>{description}</p>
        </div>
      </div>
      {children}
    </section>
  );
}

// Field wraps a label around inputs so forms stay consistent across the app.
function Field({ label, children }) {
  return (
    <label>
      <span>{label}</span>
      {children}
    </label>
  );
}

// Toast is the small message that appears after API actions succeed or fail.
function Toast({ toast }) {
  if (!toast.message) {
    return null;
  }
  return (
    <div className={`toast ${toast.error ? "error" : ""}`.trim()} role="status" aria-live="polite">
      {toast.message}
    </div>
  );
}

export default function Home() {
  // Authentication state is stored in React and mirrored in sessionStorage for refreshes.
  const [authToken, setAuthToken] = useState(() => sessionStorage.getItem("authToken"));
  const [authUser, setAuthUser] = useState(() => readSessionJson("authUser"));

  // Account state drives the dashboard, selected account card, and transaction table.
  const [selectedAccount, setSelectedAccount] = useState(DEMO_ACCOUNTS[0]);
  const [accounts, setAccounts] = useState(DEMO_ACCOUNTS);
  const [transactionPage, setTransactionPage] = useState(DEMO_TRANSACTION_PAGE);

  // Form message state is separate from toast state because some messages live inside forms.
  const [accountIdInput, setAccountIdInput] = useState(DEMO_ACCOUNTS[0].accountId);
  const [loginMessage, setLoginMessage] = useState("");
  const [createMessage, setCreateMessage] = useState("");
  const [toast, setToast] = useState({ message: "", error: false });

  // Shows a short-lived message in the bottom corner of the screen.
  function showToast(message, error = false) {
    setToast({ message, error });
    window.setTimeout(() => setToast({ message: "", error: false }), 2800);
  }

  // Saves login info in both React state and browser sessionStorage.
  function storeLogin(login) {
    setAuthToken(login.token);
    setAuthUser(login);
    sessionStorage.setItem("authToken", login.token);
    sessionStorage.setItem("authUser", JSON.stringify(login));
  }

  // Loads the account list used by the Accounts panel.
  async function loadAccounts(token = authToken) {
    const result = await BankService.getAccounts(token);
    setAccounts(result.map(Account.from));
  }

  // Loads one page of transactions for the selected account.
  async function loadTransactions(accountId, page = 0) {
    if (!authToken && DEMO_ACCOUNTS.some(account => account.accountId === accountId)) {
      setTransactionPage(DEMO_TRANSACTION_PAGE);
      return;
    }

    const result = await BankService.getTransactions(accountId, page, 5, authToken);
    setTransactionPage(Transaction.pageFrom(result));
  }

  // Loads account details, then refreshes the matching transaction history.
  async function loadAccount(accountId) {
    const demoAccount = DEMO_ACCOUNTS.find(account => account.accountId === accountId);
    if (!authToken && demoAccount) {
      setSelectedAccount(demoAccount);
      setAccountIdInput(demoAccount.accountId);
      setTransactionPage(DEMO_TRANSACTION_PAGE);
      return;
    }

    const account = Account.from(await BankService.getAccount(accountId, authToken));
    setSelectedAccount(account);
    setAccountIdInput(account.accountId);
    await loadTransactions(account.accountId, 0);
  }

  // Prevents money actions from running before an account is selected.
  function requireSelectedAccount() {
    if (!selectedAccount) {
      throw new Error("Select or create an account first.");
    }
    return selectedAccount.accountId;
  }

  function updateDemoAccount(updatedAccount) {
    // Demo mode updates React state only; it does not save anything to MongoDB.
    setSelectedAccount(updatedAccount);
    setAccounts(currentAccounts =>
      currentAccounts.map(account =>
        account.accountId === updatedAccount.accountId ? updatedAccount : account
      )
    );
  }

  function addDemoTransaction(type, amount) {
    // Newest demo transactions appear at the top, matching the backend sort order.
    const transaction = Transaction.from({
      transactionId: `DEMO-${Date.now()}`,
      type,
      amount,
      date: new Date().toISOString()
    });

    setTransactionPage(currentPage => ({
      ...currentPage,
      content: [transaction, ...(currentPage.content || [])],
      number: 0,
      totalPages: 1
    }));
  }

  // Handles the login form and stores the returned token for future requests.
  async function handleLogin(event) {
    event.preventDefault();
    setLoginMessage("");
    const formElement = event.currentTarget;
    const form = new FormData(formElement);

    try {
      const login = await BankService.login({
        email: form.get("email"),
        password: form.get("password")
      });
      storeLogin(login);
      setLoginMessage("Login successful.");
      await loadAccounts(login.token);
      showToast("Logged in.");
    } catch (error) {
      setLoginMessage(error.message);
      showToast(error.message, true);
    }
  }

  // Creates a user/account pair, then logs the new user in automatically.
  async function handleCreateAccount(event) {
    event.preventDefault();
    setCreateMessage("");
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    const body = {
      name: form.get("name"),
      email: form.get("email"),
      password: form.get("password"),
      accountType: form.get("accountType")
    };

    try {
      const account = Account.from(await BankService.createAccount(body));
      const login = await BankService.login({ email: body.email, password: body.password });
      storeLogin(login);
      setSelectedAccount(account);
      setAccountIdInput(account.accountId);
      setTransactionPage({ content: [], number: 0, totalPages: 1 });
      await loadAccounts(login.token);
      formElement.reset();
      setCreateMessage("Account created and logged in.");
      showToast("Account created.");
    } catch (error) {
      setCreateMessage(error.message);
      showToast(error.message, true);
    }
  }

  // Lets the user paste or type an account ID and load it directly.
  async function handleLookup(event) {
    event.preventDefault();
    try {
      await loadAccount(accountIdInput.trim());
      showToast("Account loaded.");
    } catch (error) {
      showToast(error.message, true);
    }
  }

  // Deposit and withdraw share the same flow; only the endpoint name changes.
  async function handleMoneyAction(event, action) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);

    try {
      const accountId = requireSelectedAccount();
      const amount = form.get("amount");

      if (!authToken) {
        // Without a login token, keep the app usable with hard-coded frontend data.
        const numericAmount = Number(amount);
        if (numericAmount <= 0) {
          throw new Error("Amount must be positive.");
        }
        if (action === "withdraw" && selectedAccount.balance < numericAmount) {
          throw new Error("Cannot withdraw more than the current balance.");
        }

        const nextBalance = action === "deposit"
          ? selectedAccount.balance + numericAmount
          : selectedAccount.balance - numericAmount;
        const updatedAccount = Account.from({ ...selectedAccount, balance: nextBalance });
        updateDemoAccount(updatedAccount);
        addDemoTransaction(action === "deposit" ? "DEPOSIT" : "WITHDRAW", numericAmount);
        formElement.reset();
        showToast(action === "deposit" ? "Demo deposit complete." : "Demo withdrawal complete.");
        return;
      }

      const account = Account.from(
        action === "deposit"
          ? await BankService.deposit(accountId, amount, authToken)
          : await BankService.withdraw(accountId, amount, authToken)
      );
      setSelectedAccount(account);
      await loadTransactions(account.accountId, 0);
      await loadAccounts();
      formElement.reset();
      showToast(action === "deposit" ? "Deposit complete." : "Withdrawal complete.");
    } catch (error) {
      showToast(error.message, true);
    }
  }

  // Updates the display name for the user tied to the selected account.
  async function handleRename(event) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);

    try {
      const accountId = requireSelectedAccount();

      if (!authToken) {
        const updatedAccount = Account.from({ ...selectedAccount, userName: form.get("name") });
        updateDemoAccount(updatedAccount);
        formElement.reset();
        showToast("Demo name updated.");
        return;
      }

      const account = Account.from(await BankService.updateAccountUser(accountId, { name: form.get("name") }, authToken));
      setSelectedAccount(account);
      await loadAccounts();
      formElement.reset();
      showToast("Name updated.");
    } catch (error) {
      showToast(error.message, true);
    }
  }

  // Transfers money from the selected account to another account.
  async function handleTransfer(event) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);

    try {
      const accountId = requireSelectedAccount();
      const receiverAccountId = form.get("receiverAccountId");
      const amount = form.get("amount");

      if (!authToken) {
        // Demo transfers update both accounts in local React state.
        const numericAmount = Number(amount);
        const receiver = accounts.find(account => account.accountId === receiverAccountId);
        if (!receiver) {
          throw new Error("Receiver account not found in demo data.");
        }
        if (numericAmount <= 0) {
          throw new Error("Amount must be positive.");
        }
        if (selectedAccount.balance < numericAmount) {
          throw new Error("Cannot transfer more than the current balance.");
        }

        const sender = Account.from({ ...selectedAccount, balance: selectedAccount.balance - numericAmount });
        const updatedReceiver = Account.from({ ...receiver, balance: receiver.balance + numericAmount });
        setSelectedAccount(sender);
        setAccounts(currentAccounts =>
          currentAccounts.map(account => {
            if (account.accountId === sender.accountId) {
              return sender;
            }
            if (account.accountId === updatedReceiver.accountId) {
              return updatedReceiver;
            }
            return account;
          })
        );
        addDemoTransaction("TRANSFER", numericAmount);
        formElement.reset();
        showToast("Demo transfer complete.");
        return;
      }

      const account = Account.from(await BankService.transfer(accountId, {
        receiverAccountId,
        amount
      }, authToken));
      setSelectedAccount(account);
      await loadTransactions(account.accountId, 0);
      await loadAccounts();
      formElement.reset();
      showToast("Transfer complete.");
    } catch (error) {
      showToast(error.message, true);
    }
  }

  // Manual refresh for the account list in case another request changed balances.
  async function handleRefresh() {
    try {
      if (!authToken) {
        // Refresh resets the local demo data back to the starter values.
        setAccounts(DEMO_ACCOUNTS);
        setSelectedAccount(DEMO_ACCOUNTS[0]);
        setAccountIdInput(DEMO_ACCOUNTS[0].accountId);
        setTransactionPage(DEMO_TRANSACTION_PAGE);
        showToast("Hard-coded demo data refreshed.");
        return;
      }

      await loadAccounts();
      showToast("Accounts refreshed.");
    } catch (error) {
      showToast(error.message, true);
    }
  }

  // When a saved token exists, load accounts as soon as the app opens.
  useEffect(() => {
    if (authToken) {
      loadAccounts().catch(error => showToast(error.message, true));
    }
    // loadAccounts already reads the current authToken through BankService.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [authToken]);

  return (
    <>
      <section className="dashboard-toolbar">
        <div>
          <p className="eyebrow">Account workspace</p>
          <h1>Bank Command Center</h1>
        </div>
        <div className="topbar-actions">
          <span className="login-status">
            <ShieldCheck size={16} aria-hidden="true" />
            {authUser ? `Signed in: ${authUser.name}` : "Guest session"}
          </span>
          <button className="secondary-button icon-button" type="button" onClick={handleRefresh}>
            <RefreshCw size={17} aria-hidden="true" />
            Refresh
          </button>
        </div>
      </section>

      <main className="app-shell">
        {/* Login and create account are the entry points into the authenticated banking flow. */}
        <Panel icon={LogIn} title="Login" description="Sign in to view and manage accounts.">
          <form className="form-grid" onSubmit={handleLogin}>
            <Field label="Email">
              <input name="email" type="email" autoComplete="email" required />
            </Field>
            <Field label="Password">
              <input name="password" type="password" autoComplete="current-password" required />
            </Field>
            <p className={`form-message ${loginMessage === "Login successful." ? "success" : "error"}`.trim()}>
              {loginMessage}
            </p>
            <button className="icon-button" type="submit">
              <LogIn size={17} aria-hidden="true" />
              Login
            </button>
          </form>
        </Panel>

        <Panel icon={UserPlus} title="Create Account" description="Open a checking or savings account.">
          <form className="form-grid" onSubmit={handleCreateAccount}>
            <Field label="Name">
              <input name="name" autoComplete="name" required />
            </Field>
            <Field label="Email">
              <input name="email" type="email" autoComplete="email" required />
            </Field>
            <Field label="Password">
              <input name="password" type="password" autoComplete="new-password" minLength="6" required />
            </Field>
            <Field label="Account Type">
              <select name="accountType" required>
                <option value="SAVINGS">Savings</option>
                <option value="CHECKING">Checking</option>
              </select>
            </Field>
            <p className={`form-message ${createMessage.includes("created") ? "success" : "error"}`.trim()}>
              {createMessage}
            </p>
            <button className="icon-button" type="submit">
              <UserPlus size={17} aria-hidden="true" />
              Create Account
            </button>
          </form>
        </Panel>

        {/* Account lookup keeps the required project flow: view an account by ID. */}
        <Panel icon={Search} title="View Account" description="Load an account by ID or select one from the list.">
          <form className="lookup-row" onSubmit={handleLookup}>
            <input
              value={accountIdInput}
              onChange={event => setAccountIdInput(event.target.value)}
              placeholder="Account ID"
              required
            />
            <button className="icon-button" type="submit">
              <Search size={17} aria-hidden="true" />
              Load
            </button>
          </form>

          {selectedAccount ? (
            <div className="account-details">
              <Detail label="Account ID" value={selectedAccount.accountId} />
              <Detail label="User Name" value={selectedAccount.userName} />
              <Detail label="Email" value={selectedAccount.email} />
              <Detail label="Type" value={selectedAccount.accountType} />
              <Detail label="Balance" value={formatMoney(selectedAccount.balance)} />
              <Detail label="Created" value={formatDate(selectedAccount.createdAt)} />
            </div>
          ) : (
            <div className="empty-state padded">Select or create an account.</div>
          )}
        </Panel>

        {/* These forms call the business-rule endpoints: deposit, withdraw, rename, and transfer. */}
        <Panel icon={Wallet} title="Actions" description="Deposit, withdraw, rename, or transfer money." className="actions-panel">
          <div className="action-grid">
            <form onSubmit={event => handleMoneyAction(event, "deposit")}>
              <Field label="Deposit Amount">
                <input name="amount" type="number" min="0.01" step="0.01" required />
              </Field>
              <button className="icon-button" type="submit">
                <ArrowDownCircle size={17} aria-hidden="true" />
                Deposit
              </button>
            </form>
            <form onSubmit={event => handleMoneyAction(event, "withdraw")}>
              <Field label="Withdraw Amount">
                <input name="amount" type="number" min="0.01" step="0.01" required />
              </Field>
              <button className="icon-button" type="submit">
                <ArrowUpCircle size={17} aria-hidden="true" />
                Withdraw
              </button>
            </form>
            <form onSubmit={handleRename}>
              <Field label="New Name">
                <input name="name" required />
              </Field>
              <button className="icon-button" type="submit">
                <Pencil size={17} aria-hidden="true" />
                Update Name
              </button>
            </form>
            <form onSubmit={handleTransfer}>
              <Field label="Receiver Account ID">
                <input name="receiverAccountId" required />
              </Field>
              <Field label="Transfer Amount">
                <input name="amount" type="number" min="0.01" step="0.01" required />
              </Field>
              <button className="icon-button" type="submit">
                <Send size={17} aria-hidden="true" />
                Transfer
              </button>
            </form>
          </div>
        </Panel>

        {/* The account list acts like a quick picker after login or account creation. */}
        <Panel icon={CreditCard} title="Accounts" description="Choose an account to inspect." className="accounts-panel">
          {accounts.length ? (
            <div className="accounts-list">
              {accounts.map(account => (
                <button
                  className="account-row"
                  type="button"
                  key={account.accountId}
                  onClick={() => loadAccount(account.accountId).then(() => showToast("Account loaded.")).catch(error => showToast(error.message, true))}
                >
                  <strong>{account.userName}</strong>
                  <span>{account.accountType} - {formatMoney(account.balance)}</span>
                  <small>{account.accountId}</small>
                </button>
              ))}
            </div>
          ) : (
            <div className="empty-state padded">No accounts loaded.</div>
          )}
        </Panel>

        {/* The transaction table shows the paginated history returned by Spring Boot. */}
        <Panel icon={History} title="Transaction History" description="Latest deposits, withdrawals, and transfers." className="history-panel">
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Transaction ID</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {transactionPage.content?.length ? (
                  transactionPage.content.map(transaction => (
                    <tr key={transaction.transactionId}>
                      <td>{transaction.transactionId}</td>
                      <td>{transaction.type}</td>
                      <td>{formatMoney(transaction.amount)}</td>
                      <td>{formatDate(transaction.date)}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="4" className="empty-cell">Select an account to view transactions.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
          <div className="pagination-row">
            <button
              className="secondary-button icon-button"
              type="button"
              disabled={!selectedAccount || transactionPage.number <= 0}
              onClick={() => loadTransactions(selectedAccount.accountId, transactionPage.number - 1)}
            >
              Previous
            </button>
            <span>Page {(transactionPage.number || 0) + 1} of {transactionPage.totalPages || 1}</span>
            <button
              className="secondary-button icon-button"
              type="button"
              disabled={!selectedAccount || transactionPage.number + 1 >= (transactionPage.totalPages || 1)}
              onClick={() => loadTransactions(selectedAccount.accountId, transactionPage.number + 1)}
            >
              Next
            </button>
          </div>
        </Panel>
      </main>

      <Toast toast={toast} />
    </>
  );
}

// Small read-only label/value pair used by the account details section.
function Detail({ label, value }) {
  return (
    <div className="detail-item">
      <span className="detail-label">{label}</span>
      <span className="detail-value">{value}</span>
    </div>
  );
}
