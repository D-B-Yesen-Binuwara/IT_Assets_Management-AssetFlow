import { useState } from 'react';
import { Button } from '../components/common/Button';
import { Card, CardHeader } from '../components/common/Card';
import { ChartPlaceholder } from '../components/common/ChartPlaceholder';
import { Icon } from '../components/common/Icon';
import { PageHeader } from '../components/common/PageHeader';
import { ErrorState, LoadingState } from '../components/common/State';
import { useResource } from '../hooks/useResource';
import { reportService } from '../services/resources';

const reportTypes = [
  { id: 'overview', label: 'Inventory overview', icon: 'assets' },
  { id: 'department', label: 'By department', icon: 'building' },
  { id: 'maintenance', label: 'Maintenance costs', icon: 'wrench' },
  { id: 'procurement', label: 'Procurement history', icon: 'cart' },
  { id: 'warranty', label: 'Warranty status', icon: 'shield' },
  { id: 'lifecycle', label: 'Lifecycle events', icon: 'refresh' },
];

export function ReportsPage() {
  const [active, setActive] = useState(reportTypes[0].id);
  const activeReport = reportTypes.find((report) => report.id === active);
  const resource = useResource(() => reportService.summary({ report: active }), active);

  // The report page requests a backend summary for the selected report type.
  return (
    <div>
      <PageHeader
        title="Reports & Analytics"
        description="Visual reports covering the full asset lifecycle."
        actions={
          <>
            <Button variant="outline" icon="filter">
              Filters
            </Button>
            <Button icon="download">Export report</Button>
          </>
        }
      />

      <div className="report-layout">
        <aside className="report-menu">
          <p className="eyebrow">Report library</p>

          {reportTypes.map((report) => (
            <button
              key={report.id}
              className={active === report.id ? 'active' : ''}
              onClick={() => setActive(report.id)}
            >
              <Icon name={report.icon} size={17} />
              <span>{report.label}</span>
              <Icon name="chevronRight" size={14} />
            </button>
          ))}
        </aside>

        <section className="report-content">
          <Card className="report-filter">
            <div>
              <p className="eyebrow">Selected report</p>
              <h2>{activeReport?.label}</h2>
              <p>Data and comparisons are populated by the reporting API.</p>
            </div>

            <div className="date-range">
              <label>
                From
                <input type="date" />
              </label>
              <label>
                To
                <input type="date" />
              </label>
            </div>
          </Card>

          {resource.status === 'loading' && <LoadingState label="Loading report" />}
          {resource.status === 'error' && <ErrorState error={resource.error} onRetry={resource.reload} />}
          {resource.status === 'success' && (
            <div className="dashboard-grid">
              <ChartPlaceholder
                title="Primary visualization"
                description="Report data returned by the backend."
              />
              <ChartPlaceholder title="Comparison" description="Breakdown for the selected report." />

              <Card className="wide">
                <CardHeader
                  title="Report summary"
                  description="Key metrics returned by the reporting endpoint."
                />
                <div className="summary-empty">
                  <Icon name="chart" size={22} />
                  <p>No report values were returned.</p>
                </div>
              </Card>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
