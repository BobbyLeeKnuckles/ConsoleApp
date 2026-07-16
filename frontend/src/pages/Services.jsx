import { ShieldCheck } from "lucide-react";

// Static services page listing the major actions supported by the backend API.
export default function Services({ onNavigate = () => {} }) {
  return (
    <main className="page-shell">
      <section className="page-panel">
        <p className="eyebrow">Services</p>
        <h2>Banking Services</h2>
        <p>
          These services match the REST API features built into the Spring Boot backend.
        </p>
        <div className="service-grid">
          <article>
            <h3>Create Accounts</h3>
            <p>Open checking or savings accounts from the Dashboard page.</p>
          </article>
          <article>
            <h3>Move Money</h3>
            <p>Deposit, withdraw, and transfer funds while the backend enforces business rules.</p>
          </article>
          <article>
            <h3>View History</h3>
            <p>Review paginated transaction history for each selected account.</p>
          </article>
          <article>
            <h3>Admin User Review</h3>
            <p>Open the admin service for a protected fetch of registered users.</p>
            <button className="icon-button service-action" type="button" onClick={() => onNavigate("Admin")}>
              <ShieldCheck size={17} aria-hidden="true" />
              Open Admin Services
            </button>
          </article>
        </div>
      </section>
    </main>
  );
}
