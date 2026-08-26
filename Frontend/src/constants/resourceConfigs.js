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

const choice = (key, label, placeholder, options, required = true) => ({
  name: key,
  label,
  placeholder,
  required,
  type: 'select',
  options: options.map((value) => ({ value, label: value.replace(/_/g, ' ') })),
});

const value = (key, label, render) => ({
  key,
  label,
  value: (row) => row?.[key] ?? '-',
  render,
});

const status = (key = 'status') => (row) => createElement(StatusBadge, { status: row[key] });

const assignmentStatus = (row) => String(row?.status || '').trim().toUpperCase();
const assignmentCount = (status) => (rows) => rows.filter((row) => assignmentStatus(row) === status).length;

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
      { label: 'Total assets', icon: 'assets', tone: 'indigo', getValue: (rows) => rows.length },
      { label: 'Available', icon: 'assets', tone: 'green', getValue: (rows) => rows.filter((row) => row.status === 'AVAILABLE').length },
      { label: 'Assigned', icon: 'transfer', tone: 'blue', getValue: (rows) => rows.filter((row) => row.status === 'ASSIGNED').length },
      { label: 'Under maintenance', icon: 'wrench', tone: 'amber', getValue: (rows) => rows.filter((row) => row.status === 'UNDER_MAINTENANCE').length },
    ],
    columns: [
      value('assetTag', 'Asset tag'),
      value('name', 'Name'),
      value('category', 'Category'),
      value('brand', 'Brand'),
      value('modelNo', 'Model No'),
      value('status', 'Status', status()),
      value('assignedTo', 'Assigned to'),
    ],
    fields: [
      text('assetTag', 'Asset tag', 'e.g. AST-0001'),
      text('name', 'Asset name', 'e.g. Industrial laptop'),
      text('category', 'Category', 'e.g. Laptop'),
      text('serialNumber', 'Serial number', 'Enter serial number', false),
      text('brand', 'Brand', 'Optional brand', false),
      text('modelNo', 'Model No', 'Enter model number'),
      text('location', 'Location name or code', 'Optional location', false),
      text('department', 'Department name or code', 'Optional department', false),
    ],
  },

  employees: {
    title: 'Employees',
    singular: 'employee',
    icon: 'users',
    service: employeeService,
    description: 'Manage workforce records, departments, and asset assignments.',
    stats: [
      { label: 'Total employees', icon: 'users', tone: 'indigo', getValue: (rows) => rows.length },
      { label: 'Active', icon: 'users', tone: 'green', getValue: (rows) => rows.filter((row) => row.status === 'ACTIVE').length },
      { label: 'Departments', icon: 'building', tone: 'blue', getValue: (rows) => new Set(rows.map((row) => row.department).filter(Boolean)).size },
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
      text('department', 'Department name or code', 'Optional department', false),
      text('phone', 'Phone', 'Optional phone number', false),
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
      { label: 'Active assignments', icon: 'transfer', tone: 'blue', getValue: assignmentCount('ACTIVE') },
      { label: 'Returned', icon: 'refresh', tone: 'slate', getValue: assignmentCount('RETURNED') },
      { label: 'Transferred', icon: 'transfer', tone: 'violet', getValue: assignmentCount('TRANSFERRED') },
      { label: 'Total records', icon: 'assets', tone: 'indigo', getValue: (rows) => rows.length },
    ],
    columns: [
      value('assetTag', 'Asset tag'),
      value('assetName', 'Assets Name'),
      value('category', 'Category'),
      value('employeeName', 'Employee'),
      value('assignedDate', 'Assigned'),
      value('closingDate', 'Closing date'),
      value('status', 'Status', status()),
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
      { label: 'Open tickets', icon: 'wrench', tone: 'amber', getValue: (rows) => rows.filter((row) => row.status === 'OPEN').length },
      { label: 'In progress', icon: 'wrench', tone: 'blue', getValue: (rows) => rows.filter((row) => row.status === 'IN_PROGRESS').length },
      { label: 'Completed', icon: 'shield', tone: 'green', getValue: (rows) => rows.filter((row) => row.status === 'COMPLETED').length },
      { label: 'Total cost', icon: 'chart', tone: 'rose', getValue: (rows) => rows.reduce((sum, row) => sum + Number(row.cost || 0), 0).toFixed(2) },
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
      choice('priority', 'Priority', 'Select priority', ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']),
      text('dueDate', 'Due date', 'YYYY-MM-DD', false),
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
      { label: 'Active warranties', icon: 'shield', tone: 'green', getValue: (rows) => rows.filter((row) => row.status === 'ACTIVE').length },
      { label: 'Expiring soon', icon: 'warning', tone: 'amber', getValue: (rows) => rows.filter((row) => row.status === 'EXPIRING').length },
      { label: 'Expired', icon: 'shield', tone: 'rose', getValue: (rows) => rows.filter((row) => row.status === 'EXPIRED').length },
      { label: 'Claims filed', icon: 'shield', tone: 'indigo', getValue: (rows) => rows.reduce((sum, row) => sum + Number(row.claimCount || 0), 0) },
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
      text('provider', 'Warranty provider name', 'Must match an existing vendor'),
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
      { label: 'Total vendors', icon: 'building', tone: 'indigo', getValue: (rows) => rows.length },
      { label: 'Active', icon: 'shield', tone: 'green', getValue: (rows) => rows.filter((row) => row.status === 'ACTIVE').length },
      { label: 'Purchase orders', icon: 'chart', tone: 'blue', getValue: (rows) => rows.reduce((sum, row) => sum + Number(row.purchaseOrderCount || 0), 0) },
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
      text('contact', 'Primary contact', 'Enter contact name', false),
      text('email', 'Email', 'contact@vendor.com', false),
      text('category', 'Category', 'Enter category', false),
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
      { label: 'Total licenses', icon: 'server', tone: 'indigo', getValue: (rows) => rows.length },
      { label: 'Active', icon: 'server', tone: 'green', getValue: (rows) => rows.filter((row) => row.status === 'ACTIVE').length },
      { label: 'Expiring', icon: 'warning', tone: 'amber', getValue: (rows) => rows.filter((row) => row.status === 'EXPIRING').length },
      { label: 'Total seats', icon: 'users', tone: 'blue', getValue: (rows) => rows.reduce((sum, row) => sum + Number(row.seats || row.seatCount || 0), 0) },
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
      text('vendor', 'Vendor name', 'Optional existing vendor', false),
      choice('licenseType', 'License type', 'Select license type', ['SUBSCRIPTION', 'PERPETUAL', 'OPEN_SOURCE', 'TRIAL']),
      text('endDate', 'Renewal date', 'YYYY-MM-DD', false),
    ],
  },

  procurement: {
    title: 'Procurement',
    singular: 'purchase order',
    icon: 'cart',
    service: procurementService,
    description: 'Manage purchase orders, invoices, and received assets.',
    stats: [
      { label: 'Total POs', icon: 'cart', tone: 'indigo', getValue: (rows) => rows.length },
      { label: 'Received', icon: 'assets', tone: 'green', getValue: (rows) => rows.filter((row) => row.status === 'RECEIVED').length },
      { label: 'Pending', icon: 'cart', tone: 'amber', getValue: (rows) => rows.filter((row) => !['RECEIVED', 'CANCELLED'].includes(row.status)).length },
      { label: 'Total spend', icon: 'chart', tone: 'blue', getValue: (rows) => rows.reduce((sum, row) => sum + Number(row.totalAmount || 0), 0).toFixed(2) },
    ],
    columns: [
      value('poNumber', 'PO number'),
      value('vendor', 'Vendor'),
      value('orderDate', 'Order date'),
      value('expectedDate', 'Expected'),
      value('totalAmount', 'Total'),
      value('status', 'Status', status()),
    ],
    fields: [
      text('poNumber', 'PO number', 'e.g. PO-0001'),
      text('vendorId', 'Vendor ID', 'Enter vendor UUID'),
      text('requestedByEmployeeId', 'Requested by employee ID', 'Optional employee UUID', false),
      text('expectedDate', 'Expected date', 'YYYY-MM-DD', false),
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
      { label: 'Total locations', icon: 'pin', tone: 'indigo', getValue: (rows) => rows.length },
      { label: 'Branches', icon: 'building', tone: 'blue', getValue: (rows) => rows.filter((row) => row.type === 'BRANCH').length },
      { label: 'Assets tracked', icon: 'assets', tone: 'green', getValue: (rows) => rows.reduce((sum, row) => sum + Number(row.assetCount || 0), 0) },
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
      choice('type', 'Location type', 'Select location type', ['BRANCH', 'BUILDING', 'FLOOR', 'ROOM', 'WAREHOUSE', 'OTHER']),
      text('branch', 'Parent location name', 'Optional parent location', false),
      text('address', 'Address', 'Optional address', false),
    ],
  },
};
