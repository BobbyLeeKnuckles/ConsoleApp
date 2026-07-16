import React from "react";
import { createRoot } from "react-dom/client";
import App from "./App.jsx";
import "./index.css";

// React mounts the whole frontend inside the <div id="root"> from index.html.
createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    {/* StrictMode helps catch React mistakes during development. */}
    <App />
  </React.StrictMode>
);
