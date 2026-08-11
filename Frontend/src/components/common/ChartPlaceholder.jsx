import { Card, CardHeader } from './Card';

export function ChartPlaceholder({ title, description, className = '' }) {
  return (
    <Card className={`chart-card ${className}`}>
      <CardHeader title={title} description={description} />

      <div className="chart-empty">
        <div className="chart-grid">
          <span />
          <span />
          <span />
          <span />
        </div>

        <div>
          <p>Awaiting backend data</p>
          <small>Connect the reporting API to populate this visualization.</small>
        </div>
      </div>
    </Card>
  );
}
