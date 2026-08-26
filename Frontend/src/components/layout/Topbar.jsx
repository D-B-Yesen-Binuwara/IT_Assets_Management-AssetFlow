import { useEffect, useRef, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { routeLabels } from '../../constants/navigation';
import { Icon } from '../common/Icon';
import { useAuth } from '../../auth/useAuth';
import { useResource } from '../../hooks/useResource';
import { notificationService } from '../../services/resources';
import { collectionItems } from '../../utils/collections';

export function Topbar({ onMenuClick, theme, onToggleTheme }) {
  const { user, logout } = useAuth();
  const unreadNotifications = useResource(() => notificationService.list({ filter: 'unread' }), 'unread');
  const location = useLocation();
  const [searchOpen, setSearchOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [query, setQuery] = useState('');
  const searchRef = useRef(null);
  const label =
    routeLabels[location.pathname] || routeLabels[`/${location.pathname.split('/')[1]}`] || 'Workspace';
  const displayName = [user?.firstName, user?.lastName].filter(Boolean).join(' ') || user?.username || 'Account';
  const initials = [user?.firstName?.[0], user?.lastName?.[0]].filter(Boolean).join('').toUpperCase() || 'AF';
  const unreadCount = collectionItems(unreadNotifications.data).length;

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
    // The shortcut opens the global search surface.
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
                    ? 'No matching results yet.'
                    : 'Start typing to search assets and people.'}
                </p>
              </div>
            </div>
          )}
        </div>

        <button className="icon-button" aria-label="Toggle theme" onClick={onToggleTheme}>
          <Icon name={theme === 'light' ? 'moon' : 'sun'} />
        </button>

        <Link className="icon-button notification-link" to="/notifications" aria-label={`Notifications${unreadCount ? `, ${unreadCount} unread` : ''}`}>
          <Icon name="bell" />
          {unreadCount > 0 && <span />}
        </Link>

        <div className="profile-menu">
          <button className="profile-trigger" onClick={() => setProfileOpen((value) => !value)}>
            <span className="avatar">{initials}</span>
            <span className="profile-name">{displayName}</span>
            <Icon name="chevronDown" size={14} />
          </button>

          {profileOpen && (
            <div className="profile-popover">
              <p className="popover-label">Signed-in user</p>
              <div className="profile-row">
                <span className="avatar">{initials}</span>
                <div>
                  <strong>{displayName}</strong>
                  <small>{user?.email || user?.username || 'AssetFlow user'}</small>
                </div>
              </div>

              <Link to="/settings" onClick={() => setProfileOpen(false)}>
                <Icon name="settings" size={16} /> Settings
              </Link>
              <button type="button" onClick={logout}>
                <Icon name="logout" size={16} /> Sign out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
