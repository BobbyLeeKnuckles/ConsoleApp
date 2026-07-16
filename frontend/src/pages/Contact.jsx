// Static contact page. This keeps non-banking content separate from the Dashboard.
export default function Contact() {
  return (
    <main className="page-shell">
      <section className="page-panel">
        <p className="eyebrow">Contact</p>
        <h2>Contact Simple Bank</h2>
        <p>
          This page is a frontend placeholder for support information. In a larger app, this would connect to a
          backend support form or customer service workflow.
        </p>
        <div className="contact-list">
          <span>Support Email: support@simplebank.local</span>
          <span><span role="img" aria-label="United States flag">🇺🇸</span> Phone: 359-315-3951</span>
          <span>Hours: Monday to Friday, 9 AM to 5 PM</span>
        </div>
      </section>
    </main>
  );
}
