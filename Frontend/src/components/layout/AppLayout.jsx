import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Topbar } from './Topbar';

export function AppLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [theme, setTheme] = useState('light');
  const location = useLocation();

  // The shell keeps theme state in memory so the frontend does not act as a fake backend.
  return (
    <div className={`app-shell theme-${theme}`}>
      <Sidebar
        collapsed={collapsed}
        setCollapsed={setCollapsed}
        mobileOpen={mobileOpen}
        setMobileOpen={setMobileOpen}
      />

      <div className="main-column">
        <Topbar
          onMenuClick={() => setMobileOpen(true)}
          theme={theme}
          onToggleTheme={() => setTheme((value) => (value === 'light' ? 'dark' : 'light'))}
        />

        <main key={location.pathname}>
          <div className="page-container">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
