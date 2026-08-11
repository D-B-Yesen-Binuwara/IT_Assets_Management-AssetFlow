import { iconNames } from './iconNames';

export const navGroups = [
  { label: 'Overview', items: [{ label: 'Dashboard', to: '/', icon: iconNames.dashboard }] },
  {
    label: 'Core Modules',
    items: [
      { label: 'Assets', to: '/assets', icon: iconNames.assets },
      { label: 'Employees', to: '/employees', icon: iconNames.users },
      { label: 'Assignments', to: '/assignments', icon: iconNames.transfer },
    ],
  },
  {
    label: 'Operations',
    items: [
      { label: 'Maintenance', to: '/maintenance', icon: iconNames.wrench },
      { label: 'Warranty', to: '/warranty', icon: iconNames.shield },
      { label: 'Software Licenses', to: '/licenses', icon: iconNames.server },
      { label: 'Vendors', to: '/vendors', icon: iconNames.building },
      { label: 'Procurement', to: '/procurement', icon: iconNames.cart },
      { label: 'Inventory Locations', to: '/locations', icon: iconNames.pin },
    ],
  },
  {
    label: 'Insights',
    items: [
      { label: 'Reports', to: '/reports', icon: iconNames.chart },
      { label: 'Notifications', to: '/notifications', icon: iconNames.bell },
    ],
  },
  { label: 'System', items: [{ label: 'Settings', to: '/settings', icon: iconNames.settings }] },
];

export const routeLabels = Object.fromEntries(
  navGroups.flatMap((group) => group.items.map((item) => [item.to, item.label])),
);
