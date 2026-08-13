import { createElement } from 'react';
import { StatusBadge } from '../components/common/StatusBadge';
import {
  assetService,
  assignmentService,
  employeeService,
  licenseService,
  locationService,
  maintenanceService,
  procurementService,
  vendorService,
  warrantyService,
} from '../services/resources';

const text = (key, label, placeholder, required = true) => ({
  name: key,
  label,
  placeholder,
  required,
});

const value = (key, label, render) => ({
  key,
  label,
  value: (row) => row?.[key] ?? '-',
  render,
});

const status = (key = 'status') => (row) => createElement(StatusBadge, { status: row[key] });

// The configs describe UI structure only; record values still come from backend services.
export const resourceConfigs = {
  assets: {
    title: 'Assets',
    singular: 'asset',
    icon: 'assets',
    service: assetService,
    transferHistory: true,
    description: 'Manage the complete asset inventory across every category and lifecycle stage.',
    stats: [
      { label: 'Total assets', icon: 'assets', tone: 'indigo' },
      { label: 'Available', icon: 'assets', tone: 'green' },
      { label: 'Assigned', icon: 'transfer', tone: 'blue' },
      { label: 'Under maintenance', icon: 'wrench', tone: 'amber' },
    ],
    columns: [
      value('assetTag', 'Asset tag'),
      value('name', 'Name'),
      value('category', 'Category'),
      value('status', 'Status', status()),
      value('assignedTo', 'Assigned to'),
    ],
    fields: [
      text('assetTag', 'Asset tag', 'e.g. AST-0001'),
      text('name', 'Asset name', 'e.g. Industrial laptop'),
      text('category', 'Category', 'e.g. Laptop'),
      text('serialNumber', 'Serial number', 'Enter serial number'),
    ],
  },

  employees: {
    title: 'Employees',
    singular: 'employee',
    icon: 'users',
    service: employeeService,
    description: 'Manage workforce records, departments, and asset assignments.',
    stats: [
      { label: 'Total employees', icon: 'users', tone: 'indigo' },
      { label: 'Active', icon: 'users', tone: 'green' },
      { label: 'Departments', icon: 'building', tone: 'blue' },
    ],
    columns: [
      value('employeeNumber', 'Employee ID'),
      value('name', 'Name'),
      value('department', 'Department'),
      value('email', 'Email'),
      value('status', 'Status', status()),
    ],
    fields: [
      text('employeeNumber', 'Employee ID', 'Enter employee ID'),
      text('name', 'Full name', 'Enter full name'),
      text('email', 'Work email', 'name@company.com'),
      text('department', 'Department', 'Enter department'),
    ],
  },

  assignments: {
    title: 'Asset Assignments',
    singular: 'assignment',
    icon: 'transfer',
    service: assignmentService,
    description: 'Assign, return, and transfer assets across the organization.',
    actionLabel: 'New assignment',
    stats: [
      { label: 'Active assignments', icon: 'transfer', tone: 'blue' },
      { label: 'Returned', icon: 'refresh', tone: 'slate' },
      { label: 'Transferred', icon: 'transfer', tone: 'violet' },
      { label: 'Total records', icon: 'assets', tone: 'indigo' },
    ],
    columns: [
      value('assetTag', 'Asset tag'),
      value('assetName', 'Asset'),
      value('employeeName', 'Employee'),
      value('assignedDate', 'Assigned'),
      value('status', 'Status', status()),
    ],
    fields: [
      text('assetId', 'Asset ID', 'Enter asset ID'),
      text('employeeId', 'Employee ID', 'Enter employee ID'),
      text('assignedDate', 'Assignment date', 'YYYY-MM-DD'),
    ],
  },

  maintenance: {
    title: 'Maintenance',
    singular: 'maintenance ticket',
    icon: 'wrench',
    service: maintenanceService,
    description: 'Track repair tickets, preventive maintenance, and service costs.',
    chart: {
      title: 'Maintenance cost trend',
      description: 'Monthly repair and preventive maintenance spend.',
    },
    stats: [
      { label: 'Open tickets', icon: 'wrench', tone: 'amber' },
      { label: 'In progress', icon: 'wrench', tone: 'blue' },
      { label: 'Completed', icon: 'shield', tone: 'green' },
      { label: 'Total cost', icon: 'chart', tone: 'rose' },
    ],
    columns: [
      value('ticketNumber', 'Ticket'),
      value('assetName', 'Asset'),
      value('issue', 'Issue'),
      value('priority', 'Priority', status('priority')),
      value('status', 'Status', status()),
      value('dueDate', 'Due date'),
    ],
    fields: [
      text('assetId', 'Asset ID', 'Enter asset ID'),
      text('issue', 'Issue summary', 'Describe the issue'),
      text('priority', 'Priority', 'e.g. High'),
      text('dueDate', 'Due date', 'YYYY-MM-DD'),
    ],
  },

  warranty: {
    title: 'Warranty Management',
    singular: 'warranty record',
    icon: 'shield',
    service: warrantyService,
    description: 'Monitor warranty status, expirations, and claims across assets.',
    chart: {
      title: 'Warranty expiry timeline',
      description: 'Assets grouped by remaining warranty period.',
    },
    stats: [
      { label: 'Active warranties', icon: 'shield', tone: 'green' },
      { label: 'Expiring soon', icon: 'warning', tone: 'amber' },
      { label: 'Expired', icon: 'shield', tone: 'rose' },
      { label: 'Claims filed', icon: 'shield', tone: 'indigo' },
    ],
    columns: [
      value('assetTag', 'Asset tag'),
      value('assetName', 'Asset'),
      value('vendor', 'Vendor'),
      value('warrantyEnd', 'Warranty end'),
      value('status', 'Status', status()),
    ],
    fields: [
      text('assetId', 'Asset ID', 'Enter asset ID'),
      text('provider', 'Warranty provider', 'Enter provider'),
      text('startDate', 'Start date', 'YYYY-MM-DD'),
      text('endDate', 'End date', 'YYYY-MM-DD'),
    ],
  },

  vendors: {
    title: 'Vendors',
    singular: 'vendor',
    icon: 'building',
    service: vendorService,
    description: 'Manage suppliers, contracts, and procurement relationships.',
    stats: [
      { label: 'Total vendors', icon: 'building', tone: 'indigo' },
      { label: 'Active', icon: 'shield', tone: 'green' },
      { label: 'Total spend', icon: 'chart', tone: 'blue' },
    ],
    columns: [
      value('name', 'Vendor'),
      value('contact', 'Contact'),
      value('category', 'Category'),
      value('totalSpend', 'Total spend'),
      value('status', 'Status', status()),
    ],
    fields: [
      text('name', 'Vendor name', 'Enter vendor name'),
      text('contact', 'Primary contact', 'Enter contact name'),
      text('email', 'Email', 'contact@vendor.com'),
      text('category', 'Category', 'Enter category'),
    ],
  },

  licenses: {
    title: 'Software Licenses',
    singular: 'license',
    icon: 'server',
    service: licenseService,
    description: 'Manage enterprise software, subscriptions, and seat utilization.',
    chart: {
      title: 'License utilization',
      description: 'Used seats compared with licensed capacity.',
    },
    stats: [
      { label: 'Total licenses', icon: 'server', tone: 'indigo' },
      { label: 'Active', icon: 'server', tone: 'green' },
      { label: 'Expiring', icon: 'warning', tone: 'amber' },
      { label: 'Total seats', icon: 'users', tone: 'blue' },
    ],
    columns: [
      value('software', 'Software'),
      value('vendor', 'Vendor'),
      value('type', 'Type'),
      value('seats', 'Seats'),
      value('endDate', 'Renewal date'),
      value('status', 'Status', status()),
    ],
    fields: [
      text('software', 'Software name', 'Enter software name'),
      text('vendor', 'Vendor', 'Enter vendor'),
      text('licenseType', 'License type', 'Subscription or perpetual'),
      text('endDate', 'Renewal date', 'YYYY-MM-DD'),
    ],
  },

  procurement: {
    title: 'Procurement',
    singular: 'purchase order',
    icon: 'cart',
    service: procurementService,
    description: 'Manage purchase orders, invoices, and received assets.',
    stats: [
      { label: 'Total POs', icon: 'cart', tone: 'indigo' },
      { label: 'Received', icon: 'assets', tone: 'green' },
      { label: 'Pending', icon: 'cart', tone: 'amber' },
      { label: 'Total spend', icon: 'chart', tone: 'blue' },
    ],
    columns: [
      value('poNumber', 'PO number'),
      value('vendor', 'Vendor'),
      value('orderDate', 'Order date'),
      value('expectedDate', 'Expected'),
      value('total', 'Total'),
      value('status', 'Status', status()),
    ],
    fields: [
      text('vendorId', 'Vendor ID', 'Enter vendor ID'),
      text('requestedBy', 'Requested by', 'Enter employee ID'),
      text('expectedDate', 'Expected date', 'YYYY-MM-DD'),
      text('notes', 'Notes', 'Optional notes', false),
    ],
  },

  locations: {
    title: 'Inventory Locations',
    singular: 'location',
    icon: 'pin',
    service: locationService,
    description: 'Organize assets across branches, buildings, floors, and rooms.',
    stats: [
      { label: 'Total locations', icon: 'pin', tone: 'indigo' },
      { label: 'Branches', icon: 'building', tone: 'blue' },
      { label: 'Assets tracked', icon: 'assets', tone: 'green' },
    ],
    columns: [
      value('name', 'Location'),
      value('type', 'Type'),
      value('branch', 'Branch'),
      value('address', 'Address'),
      value('assetCount', 'Assets'),
    ],
    fields: [
      text('name', 'Location name', 'e.g. Main warehouse'),
      text('type', 'Location type', 'Warehouse, office, etc.'),
      text('branch', 'Branch', 'Enter branch'),
      text('address', 'Address', 'Enter address'),
    ],
  },
};
