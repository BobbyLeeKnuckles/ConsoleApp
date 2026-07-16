// Normalizes account data so the UI can handle backend and demo records the same way.
export default class Account {
  constructor({
    accountId = "",
    userName = "",
    email = "",
    accountType = "",
    balance = 0,
    createdAt = ""
  } = {}) {
    this.accountId = accountId;
    this.userName = userName;
    this.email = email;
    this.accountType = accountType;
    this.balance = Number(balance || 0);
    this.createdAt = createdAt;
  }

  static from(account = {}) {
    // Accept either backend names or fallback names so demo data and API data use one class.
    return new Account({
      accountId: account.accountId || account.id || "",
      userName: account.userName || account.name || "",
      email: account.email || "",
      accountType: account.accountType || "",
      balance: account.balance || 0,
      createdAt: account.createdAt || ""
    });
  }
}
