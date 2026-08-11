import { NavLink, useLocation } from 'react-router-dom';
import { navGroups } from '../../constants/navigation';
import { Icon } from '../common/Icon';

export function Sidebar({ collapsed, setCollapsed, mobileOpen, setMobileOpen }) {
  const location = useLocation();

  // The same navigation content is reused for desktop and mobile shells.
  const content = (
    <div className="sidebar-content">
      <div className="brand">
        <div className="brand-mark">
          <Icon name="assets" size={20} />
        </div>

        {!collapsed && (
          <div>
            <strong>AssetFlow</strong>
            <span>Lifecycle Management</span>
          </div>
        )}
      </div>

      <nav>
        {navGroups.map((group) => (
          <div className="nav-group" key={group.label}>
            {!collapsed && <p>{group.label}</p>}

            {group.items.map((item) => {
              const active =
                item.to === '/' ? location.pathname === '/' : location.pathname.startsWith(item.to);

              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  onClick={() => setMobileOpen(false)}
                  className={active ? 'active' : ''}
                  title={collapsed ? item.label : undefined}
                >
                  <Icon name={item.icon} size={18} />
                  <span>{!collapsed && item.label}</span>
                </NavLink>
              );
            })}
          </div>
        ))}
      </nav>

      <div className="sidebar-footer">
        <button onClick={() => setCollapsed(!collapsed)}>
          <Icon name={collapsed ? 'chevronRight' : 'chevronLeft'} size={16} />
          {!collapsed && 'Collapse'}
        </button>
      </div>
    </div>
  );

  return (
    <>
      <aside className={`sidebar ${collapsed ? 'collapsed' : ''}`}>{content}</aside>

      {mobileOpen && (
        <>
          <div className="mobile-overlay" onClick={() => setMobileOpen(false)} />
          <aside className="mobile-sidebar">{content}</aside>
        </>
      )}
    </>
  );
}
