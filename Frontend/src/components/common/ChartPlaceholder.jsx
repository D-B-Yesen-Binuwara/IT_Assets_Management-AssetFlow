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
          <p>No trend data yet</p>
          <small>Trend data will appear when records are available.</small>
        </div>
      </div>
    </Card>
  );
}
