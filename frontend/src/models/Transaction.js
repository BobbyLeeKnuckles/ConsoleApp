// Normalizes transaction data and transaction pages returned by Spring Boot.
export default class Transaction {
  constructor({
    transactionId = "",
    type = "",
    amount = 0,
    date = ""
  } = {}) {
    this.transactionId = transactionId;
    this.type = type;
    this.amount = Number(amount || 0);
    this.date = date;
  }

  static from(transaction = {}) {
    // Some APIs call this id/createdAt, while this app displays transactionId/date.
    return new Transaction({
      transactionId: transaction.transactionId || transaction.txnId || transaction.id || "",
      type: transaction.type || transaction.txnType || "",
      amount: transaction.amount || 0,
      date: transaction.date || transaction.createdAt || ""
    });
  }

  static pageFrom(page = {}) {
    // Spring returns a Page object; the UI only needs content, current page, and total pages.
    return {
      content: Array.isArray(page.content) ? page.content.map(Transaction.from) : [],
      number: page.number || 0,
      totalPages: page.totalPages || 1
    };
  }
}
