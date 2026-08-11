import { useState } from 'react';
import { PageHeader } from '../components/common/PageHeader';
import { Button } from '../components/common/Button';
import { Card } from '../components/common/Card';
import { Table } from '../components/common/Table';
import { useResource } from '../hooks/useResource';
import { notificationService } from '../services/resources';

const filters = [
  { id: 'all', label: 'All notifications' },
  { id: 'unread', label: 'Unread' },
];

const columns = [
  { key: 'title', label: 'Notification', value: (row) => row.title },
  { key: 'type', label: 'Type', value: (row) => row.type },
  { key: 'priority', label: 'Priority', value: (row) => row.priority },
  { key: 'date', label: 'Date', value: (row) => row.date },
  { key: 'read', label: 'Read', value: (row) => (row.read ? 'Yes' : 'No') },
];

export function NotificationsPage() {
  const [filter, setFilter] = useState('all');
  const resource = useResource(() => notificationService.list({ filter }), filter);

  // The notification table accepts common API collection shapes without creating fallback rows.
  const rows = Array.isArray(resource.data?.items) ? resource.data.items : resource.data;

  return (
    <div>
      <PageHeader
        title="Notification Center"
        description="Stay on top of maintenance, warranty, procurement, and system alerts."
        actions={
          <Button variant="outline" icon="refresh" onClick={resource.reload}>
            Refresh
          </Button>
        }
      />

      <div className="filter-tabs" role="group" aria-label="Notification filters">
        {filters.map((item) => (
          <button
            key={item.id}
            type="button"
            className={`filter-tab ${filter === item.id ? 'active' : ''}`}
            aria-pressed={filter === item.id}
            onClick={() => setFilter(item.id)}
          >
            {item.label}
          </button>
        ))}
      </div>

      <Card>
        <Table
          columns={columns}
          rows={rows}
          status={resource.status}
          error={resource.error}
          onRetry={resource.reload}
          emptyTitle="You are all caught up"
          emptyDescription="New alerts will appear here when the notification API is connected."
        />
      </Card>
    </div>
  );
}
