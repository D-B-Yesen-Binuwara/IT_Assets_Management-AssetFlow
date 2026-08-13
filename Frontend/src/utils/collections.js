export function collectionItems(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.items)) return data.items;
  if (Array.isArray(data?.content)) return data.content;
  return [];
}

export function recordId(record, key) {
  return record?.[key] ?? record?.[key.replace(/Id$/, '')]?.id ?? '';
}

export function employeeLabel(employee) {
  if (!employee) return '-';
  if (employee.name) return employee.name;

  const fullName = [employee.firstName, employee.lastName].filter(Boolean).join(' ');
  return fullName || employee.employeeNumber || employee.id || '-';
}

export function locationLabel(location) {
  if (!location) return '-';
  return location.name || location.code || location.id || '-';
}
