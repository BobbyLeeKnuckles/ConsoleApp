import { useEffect, useState } from "react";
import "./App.css";
import Header from "./components/Header";
import About from "./pages/About.jsx";
import Accounts from "./pages/Accounts.jsx";
import Admin from "./pages/Admin.jsx";
import Contact from "./pages/Contact.jsx";
import Home from "./pages/Home.jsx";
import Services from "./pages/Services.jsx";

// Theme starts from localStorage, then falls back to the computer/browser preference.
function getInitialTheme() {
  const savedTheme = localStorage.getItem("simpleBankTheme");
  if (savedTheme === "dark" || savedTheme === "light") {
    return savedTheme;
  }

  return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}

// App follows the instructor pattern: it stores the active page and renders that page.
export default function App() {
  const [page, setPage] = useState("Dashboard");
  const [theme, setTheme] = useState(getInitialTheme);
  // The keys here must match the menu labels in Header.jsx.
  const pages = {
    Dashboard: <Home />,
    Accounts: <Accounts />,
    Services: <Services onNavigate={setPage} />,
    Admin: <Admin />,
    About: <About />,
    Contact: <Contact />
  };

  useEffect(() => {
    // The data attribute lets CSS switch every page without passing theme props everywhere.
    document.documentElement.dataset.theme = theme;
    localStorage.setItem("simpleBankTheme", theme);
  }, [theme]);

  function toggleTheme() {
    setTheme(currentTheme => currentTheme === "dark" ? "light" : "dark");
  }

  return (
    <div className="app">
      {/* Header gets the current page and a callback so it can tell App when the user clicks a menu item. */}
      <Header
        activePage={page}
        onNavigate={setPage}
        theme={theme}
        onToggleTheme={toggleTheme}
      />
      {pages[page] || pages.Dashboard}
    </div>
  );
}
