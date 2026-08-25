import { useState } from 'react';
import { Button } from '../components/common/Button';
import { Card, CardHeader } from '../components/common/Card';
import { Icon } from '../components/common/Icon';
import { PageHeader } from '../components/common/PageHeader';
import { ErrorState, LoadingState } from '../components/common/State';
import { Table } from '../components/common/Table';
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

const value = (row, key) => row?.[key] === undefined || row?.[key] === null ? '\u2014' : String(row[key]);

function ReportData({ data }) {
  const metrics = Object.entries(data?.metrics || {});
  const rows = data?.breakdowns || [];
  const keys = [...new Set(rows.flatMap((row) => Object.keys(row)))];
  const columns = keys.map((key) => ({
    key,
    label: key.replace(/([A-Z])/g, ' $1').replace(/^./, (letter) => letter.toUpperCase()),
    value: (row) => value(row, key),
  }));

  return (
    <>
      <Card>
        <CardHeader title="Metrics" description="Summary values returned by the reporting API." />
        <div className="metric-list">
          {metrics.length === 0 ? (
            <div className="summary-empty"><p>No metrics returned.</p></div>
          ) : metrics.map(([key, metric]) => (
            <div className="metric-row" key={key}>
              <span>{key.replace(/([A-Z])/g, ' $1')}</span>
              <strong>{typeof metric === 'object' ? JSON.stringify(metric) : String(metric)}</strong>
            </div>
          ))}
        </div>
      </Card>

      {rows.length > 0 && (
        <Card className="table-wrap">
          <CardHeader title="Breakdown" description="Grouped records returned by the selected report." />
          <Table columns={columns} rows={rows} status="success" emptyTitle="No breakdown data" />
        </Card>
      )}

      {data?.series?.length > 0 && (
        <Card className="table-wrap">
          <CardHeader title="Time series" description="Date-based values returned by the selected report." />
          <Table
            columns={[
              { key: 'date', label: 'Date', value: (row) => value(row, 'date') },
              { key: 'seriesValue', label: 'Value', value: (row) => value(row, 'value') },
            ]}
            rows={data.series}
            status="success"
          />
        </Card>
      )}
    </>
  );
}

export function ReportsPage() {
  const [active, setActive] = useState(reportTypes[0].id);
  const [range, setRange] = useState({ from: '', to: '' });
  const activeReport = reportTypes.find((report) => report.id === active);
  const resource = useResource(
    () => reportService.summary({ report: active, ...range }),
    `${active}-${range.from}-${range.to}`,
  );

  const updateDate = (key, value) => setRange((current) => ({ ...current, [key]: value }));

  return (
    <div>
      <PageHeader
        title="Reports & Analytics"
        description="Visual reports covering the full asset lifecycle."
        actions={
          <Button variant="outline" icon="refresh" onClick={resource.reload}>
            Refresh
          </Button>
        }
      />

      <div className="report-layout">
        <aside className="report-menu">
          <p className="eyebrow">Report library</p>

          {reportTypes.map((report) => (
            <button
              key={report.id}
              type="button"
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
                <input type="date" value={range.from} onChange={(event) => updateDate('from', event.target.value)} />
              </label>
              <label>
                To
                <input type="date" value={range.to} onChange={(event) => updateDate('to', event.target.value)} />
              </label>
            </div>
          </Card>

          {resource.status === 'loading' && <LoadingState label="Loading report" />}
          {resource.status === 'error' && <ErrorState error={resource.error} onRetry={resource.reload} />}
          {resource.status === 'success' && <ReportData data={resource.data} />}
        </section>
      </div>
    </div>
  );
}
