import { useEffect, useRef, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { routeLabels } from '../../constants/navigation';
import { Icon } from '../common/Icon';

export function Topbar({ onMenuClick, theme, onToggleTheme }) {
  const location = useLocation();
  const [searchOpen, setSearchOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [query, setQuery] = useState('');
  const searchRef = useRef(null);
  const label =
    routeLabels[location.pathname] || routeLabels[`/${location.pathname.split('/')[1]}`] || 'Workspace';

  useEffect(() => {
    const handler = (event) => {
      if (searchRef.current && !searchRef.current.contains(event.target)) {
        setSearchOpen(false);
      }
    };

    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  useEffect(() => {
    // The shortcut opens the search surface before a backend search endpoint exists.
    const handler = (event) => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault();
        setSearchOpen(true);
      }
    };

    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, []);

  return (
    <header className="topbar">
      <button className="icon-button mobile-menu" onClick={onMenuClick} aria-label="Open navigation">
        <Icon name="menu" />
      </button>

      <div className="crumb">
        <span>AssetFlow</span>
        <b>/</b>
        <strong>{label}</strong>
      </div>

      <div className="topbar-actions">
        <div className="global-search" ref={searchRef}>
          <button className="search-trigger" onClick={() => setSearchOpen((value) => !value)}>
            <Icon name="search" size={16} />
            <span>Search assets, people...</span>
            <kbd>Ctrl K</kbd>
          </button>

          {searchOpen && (
            <div className="search-popover">
              <div className="table-search">
                <Icon name="search" size={16} />
                <input
                  autoFocus
                  value={query}
                  onChange={(event) => setQuery(event.target.value)}
                  placeholder="Search across the workspace..."
                />
              </div>

              <div className="search-empty">
                <Icon name={query ? 'info' : 'search'} size={18} />
                <p>
                  {query
                    ? 'Search results will appear when the backend search endpoint is connected.'
                    : 'Start typing to search assets and people.'}
                </p>
              </div>
            </div>
          )}
        </div>

        <button className="icon-button" aria-label="Toggle theme" onClick={onToggleTheme}>
          <Icon name={theme === 'light' ? 'moon' : 'sun'} />
        </button>

        <Link className="icon-button notification-link" to="/notifications" aria-label="Notifications">
          <Icon name="bell" />
          <span />
        </Link>

        <div className="profile-menu">
          <button className="profile-trigger" onClick={() => setProfileOpen((value) => !value)}>
            <span className="avatar">AM</span>
            <span className="profile-name">Account</span>
            <Icon name="chevronDown" size={14} />
          </button>

          {profileOpen && (
            <div className="profile-popover">
              <p className="popover-label">Signed-in user</p>
              <div className="profile-row">
                <span className="avatar">AM</span>
                <div>
                  <strong>Account profile</strong>
                  <small>Connect identity provider</small>
                </div>
              </div>

              <Link to="/settings" onClick={() => setProfileOpen(false)}>
                <Icon name="settings" size={16} /> Settings
              </Link>
              <button>
                <Icon name="logout" size={16} /> Sign out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
