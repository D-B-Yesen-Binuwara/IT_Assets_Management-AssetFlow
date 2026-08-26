import { Button } from '../components/common/Button';
import { Card, CardHeader } from '../components/common/Card';
import { PageHeader } from '../components/common/PageHeader';
import { StatCard } from '../components/common/StatCard';
import { Table } from '../components/common/Table';
import { useResource } from '../hooks/useResource';
import { dashboardService } from '../services/resources';
import { collectionItems } from '../utils/collections';

const stats = [
  ['Total assets', 'totalAssets', 'assets', 'indigo', 'Across all categories'],
  ['Available', 'availableAssets', 'assets', 'green', 'Ready to deploy'],
  ['Assigned', 'assignedAssets', 'transfer', 'blue', 'In active use'],
  ['Under maintenance', 'assetsUnderMaintenance', 'wrench', 'amber', 'Being serviced'],
  ['Retired / disposed', 'retiredOrDisposedAssets', 'assets', 'slate', 'End of lifecycle'],
  ['Warranty expiring', 'warrantiesExpiring', 'shield', 'rose', 'Within 60 days'],
  ['Total asset value', 'totalAssetValue', 'chart', 'violet', 'Current book value'],
  ['Departments', 'departments', 'building', 'cyan', 'Organization structure'],
];

const formatNumber = (value) => (value === undefined || value === null ? '\u2014' : Number(value).toLocaleString());
const formatMoney = (value) => (value === undefined || value === null ? '\u2014' : Number(value).toLocaleString(undefined, { style: 'currency', currency: 'USD' }));

function DistributionCard({ title, values = {} }) {
  const entries = Object.entries(values);
  const max = Math.max(...entries.map(([, value]) => Number(value)), 1);

  return (
    <Card>
      <CardHeader title={title} description="Current distribution across your assets." />
      {entries.length === 0 ? (
        <div className="summary-empty"><p>No data returned.</p></div>
      ) : (
        <div className="metric-list">
          {entries.map(([label, value]) => (
            <div className="metric-row" key={label}>
              <span>{label.replace(/_/g, ' ')}</span>
              <div className="metric-bar"><i style={{ width: `${(Number(value) / max) * 100}%` }} /></div>
              <strong>{formatNumber(value)}</strong>
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}

const activityColumns = [
  { key: 'eventType', label: 'Event', value: (row) => row.eventType || '\u2014' },
  { key: 'eventAt', label: 'Time', value: (row) => row.eventAt ? new Date(row.eventAt).toLocaleString() : '\u2014' },
  { key: 'fromStatus', label: 'From', value: (row) => row.fromStatus || '\u2014' },
  { key: 'toStatus', label: 'To', value: (row) => row.toStatus || '\u2014' },
  { key: 'notes', label: 'Notes', value: (row) => row.notes || '\u2014' },
];

export function DashboardPage() {
  const summaryResource = useResource(() => dashboardService.summary(), 'summary');
  const activityResource = useResource(() => dashboardService.activity(), 'activity');
  const summary = summaryResource.data || {};

  return (
    <div>
      <PageHeader
        title="Dashboard"
        description="A live operational view of your industrial asset estate."
        actions={
          <Button variant="outline" icon="refresh" onClick={() => {
            summaryResource.reload();
            activityResource.reload();
          }}>
            Refresh
          </Button>
        }
      />

      <div className="stats-grid">
        {stats.map(([label, key, icon, tone, helper]) => (
          <StatCard
            key={label}
            label={label}
            icon={icon}
            tone={tone}
            helper={helper}
            value={key === 'totalAssetValue' ? formatMoney(summary[key]) : formatNumber(summary[key])}
          />
        ))}
      </div>

      <div className="dashboard-grid">
        <DistributionCard title="Assets by status" values={summary.assetsByStatus} />
        <DistributionCard title="Assets by category" values={summary.assetsByCategory} />
        <DistributionCard title="Assets by department" values={summary.assetsByDepartment} />
        <Card>
          <CardHeader title="Operational alerts" description="Current operational counts." />
          <div className="metric-list">
            <div className="metric-row"><span>Open maintenance tickets</span><strong>{formatNumber(summary.openMaintenanceTickets)}</strong></div>
            <div className="metric-row"><span>Unread notifications</span><strong>{formatNumber(summary.unreadNotifications)}</strong></div>
          </div>
        </Card>
      </div>

      <Card className="table-wrap">
        <CardHeader title="Recent activity" description="Latest lifecycle events." />
        <Table
          columns={activityColumns}
          rows={collectionItems(activityResource.data)}
          status={activityResource.status}
          error={activityResource.error}
          onRetry={activityResource.reload}
          emptyTitle="No lifecycle activity"
          emptyDescription="Lifecycle events will appear here as assets change."
          searchPlaceholder="Search activity..."
        />
      </Card>
    </div>
  );
}
