// Static accounts page for the navigation menu.
export default function Accounts() {
  return (
    <main className="page-shell">
      <section className="page-panel">
        <p className="eyebrow">Accounts</p>
        <h2>Account Management</h2>
        <p>
          Use the Dashboard page to create accounts, select an account, deposit, withdraw, transfer money,
          and review transaction history.
        </p>
        <div className="info-grid">
          <div>
            <h3>Checking</h3>
            <p>Designed for daily money movement, deposits, withdrawals, and transfers.</p>
          </div>
          <div>
            <h3>Savings</h3>
            <p>Designed for holding money while still keeping account history visible in the app.</p>
          </div>
        </div>
      </section>
    </main>
  );
}
