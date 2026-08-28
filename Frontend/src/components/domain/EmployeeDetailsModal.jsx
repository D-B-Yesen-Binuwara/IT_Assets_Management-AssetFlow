import { Modal } from '../common/Modal';
import { StatusBadge } from '../common/StatusBadge';

const value = (item) => item === null || item === undefined || item === '' ? '—' : item;
const date = (item) => item ? new Date(`${item}T00:00:00`).toLocaleDateString() : '—';

function DetailSection({ title, children }) {
  return (
    <section className="asset-detail-section">
      <h3>{title}</h3>
      <div className="asset-detail-grid">{children}</div>
    </section>
  );
}

function Detail({ label, children }) {
  return <div><span>{label}</span><strong>{children}</strong></div>;
}

export function EmployeeDetailsModal({ employee, onClose }) {
  if (!employee) return null;

  return (
    <Modal
      open
      onClose={onClose}
      className="employee-detail-modal"
      title={employee.name || employee.employeeNumber || 'Employee details'}
      description={`Full record for ${employee.employeeNumber || 'this employee'}.`}
    >
      <div className="asset-detail-summary">
        <div>
          <span>Employee ID</span>
          <strong>{value(employee.employeeNumber)}</strong>
        </div>
        <StatusBadge status={employee.status} />
      </div>

      <div className="asset-detail-sections">
        <DetailSection title="Employee identity">
          <Detail label="Full name">{value(employee.name)}</Detail>
          <Detail label="First name">{value(employee.firstName)}</Detail>
          <Detail label="Last name">{value(employee.lastName)}</Detail>
          <Detail label="Job title">{value(employee.jobTitle)}</Detail>
        </DetailSection>

        <DetailSection title="Organization">
          <Detail label="Status"><StatusBadge status={employee.status} /></Detail>
          <Detail label="Branch">{value(employee.branch)}</Detail>
          <Detail label="Department">{value(employee.department)}</Detail>
          <Detail label="Hire date">{date(employee.hireDate)}</Detail>
          <Detail label="Left date">{date(employee.terminationDate)}</Detail>
          <Detail label="Account access">{employee.hasAccount ? 'Enabled' : 'Not set up'}</Detail>
        </DetailSection>

        <DetailSection title="Contact details">
          <Detail label="Email">{value(employee.email)}</Detail>
          <Detail label="Phone">{value(employee.phone)}</Detail>
          <Detail label="Address">{value(employee.address)}</Detail>
        </DetailSection>
      </div>
    </Modal>
  );
}
