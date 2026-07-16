// Static page used by the navigation menu to explain the project.
export default function About() {
  return (
    <main className="page-shell">
      <section className="page-panel">
        <p className="eyebrow">About</p>
        <h2>Simple Bank App</h2>
        <p>
          This React page explains the project purpose: a beginner-friendly banking app that connects a
          Vite frontend to a Spring Boot REST API and MongoDB database.
        </p>
        <div className="info-grid">
          <div>
            <h3>Architecture</h3>
            <p>React handles the screens, Spring Boot handles the REST API, and MongoDB stores account data.</p>
          </div>
          <div>
            <h3>Goal</h3>
            <p>The app demonstrates account creation, login, deposits, withdrawals, transfers, and history.</p>
          </div>
        </div>
      </section>
    </main>
  );
}
