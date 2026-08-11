import { Button } from '../components/common/Button';
import { Card, CardHeader } from '../components/common/Card';
import { ChartPlaceholder } from '../components/common/ChartPlaceholder';
import { PageHeader } from '../components/common/PageHeader';
import { EmptyState } from '../components/common/State';
import { StatCard } from '../components/common/StatCard';
import { useResource } from '../hooks/useResource';
import { dashboardService } from '../services/resources';

const stats = [
  ['Total assets', 'assets', 'indigo', 'Across all categories'],
  ['Available', 'assets', 'green', 'Ready to deploy'],
  ['Assigned', 'transfer', 'blue', 'In active use'],
  ['Under maintenance', 'wrench', 'amber', 'Being serviced'],
  ['Retired / disposed', 'assets', 'slate', 'End of lifecycle'],
  ['Warranty expiring', 'shield', 'rose', 'Within 60 days'],
  ['Total asset value', 'chart', 'violet', 'Current valuation'],
  ['Departments', 'building', 'cyan', 'Organization structure'],
];

export function DashboardPage() {
  const resource = useResource(() => dashboardService.summary(), 'summary');

  // The dashboard exposes metric labels while values remain owned by the backend.
  return (
    <div>
      <PageHeader
        title="Dashboard"
        description="A live operational view of your industrial asset estate."
        actions={
          <>
            <Button variant="outline" icon="refresh" onClick={resource.reload}>
              Refresh
            </Button>
            <Button icon="download">Export report</Button>
          </>
        }
      />

      <div className="stats-grid">
        {stats.map(([label, icon, tone, helper]) => (
          <StatCard key={label} label={label} icon={icon} tone={tone} helper={helper} />
        ))}
      </div>

      <div className="dashboard-grid">
        <ChartPlaceholder
          title="Monthly procurement trend"
          description="Assets procured and spend over the last 12 months."
          className="wide"
        />
        <ChartPlaceholder title="Asset status" description="Distribution by lifecycle state." />
        <ChartPlaceholder title="Assets by category" description="Inventory breakdown." />
        <ChartPlaceholder title="Assets by department" description="Allocation across teams." />
        <ChartPlaceholder title="Maintenance cost trend" description="Monthly repair spend." />
        <ChartPlaceholder
          title="Warranty expiry timeline"
          description="Assets grouped by remaining warranty period."
        />
      </div>

      <div className="dashboard-lower">
        <Card>
          <CardHeader title="Recent activity" description="Latest lifecycle events from the backend." />
          <EmptyState
            icon="refresh"
            title={resource.status === 'error' ? 'Activity is unavailable' : 'No activity yet'}
            description="Activity will appear when the dashboard API is connected."
          />
        </Card>

        <Card>
          <CardHeader title="Open maintenance" description="Upcoming and overdue work." />
          <EmptyState
            icon="wrench"
            title="No maintenance data"
            description="Work orders will appear here when available."
          />
        </Card>
      </div>
    </div>
  );
}
