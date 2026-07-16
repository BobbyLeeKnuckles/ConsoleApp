import { Moon, Sun } from "lucide-react";
import "./Header.css";

// Keeping menu items in one array makes it easy to add/remove pages later.
const menuItems = ["Dashboard", "Accounts", "Services", "Admin", "About", "Contact"];

// Header follows the instructor pattern: App owns the active page and Header reports clicks back up.
export default function Header({
  activePage = "Dashboard",
  onNavigate = () => {},
  theme = "light",
  onToggleTheme = () => {}
}) {
  const isDarkMode = theme === "dark";

  return (
    <header className="site-header">
      <div className="brand">
        <span className="brand-mark" aria-hidden="true">SB</span>
        <div>
          <p>Simple Bank App</p>
          <h1>Bank Command Center</h1>
        </div>
      </div>

      <div className="header-actions">
        <nav className="header-nav" aria-label="Main navigation">
          <ul>
            {menuItems.map(item => (
              <li key={item}>
                {/* The active class lets CSS highlight the page the user is currently viewing. */}
                <a
                  className={activePage === item ? "active" : ""}
                  href={`#${item.toLowerCase()}`}
                  onClick={(event) => {
                    event.preventDefault();
                    onNavigate(item);
                  }}
                >
                  {item}
                </a>
              </li>
            ))}
          </ul>
        </nav>

        <button
          className="theme-toggle"
          type="button"
          aria-label={isDarkMode ? "Switch to light mode" : "Switch to dark mode"}
          title={isDarkMode ? "Switch to light mode" : "Switch to dark mode"}
          onClick={onToggleTheme}
        >
          {isDarkMode ? <Sun size={18} aria-hidden="true" /> : <Moon size={18} aria-hidden="true" />}
        </button>
      </div>
    </header>
  );
}
