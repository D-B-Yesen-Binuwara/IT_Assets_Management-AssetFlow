import { useState } from 'react';
import { PageHeader } from '../components/common/PageHeader';
import { Button } from '../components/common/Button';
import { Card } from '../components/common/Card';
import { Table } from '../components/common/Table';
import { useAction, useResource } from '../hooks/useResource';
import { notificationService } from '../services/resources';
import { collectionItems } from '../utils/collections';

const filters = [
  { id: 'all', label: 'All notifications' },
  { id: 'unread', label: 'Unread' },
];

const columns = [
  { key: 'title', label: 'Notification', value: (row) => row.title },
  { key: 'type', label: 'Type', value: (row) => row.type },
  { key: 'priority', label: 'Priority', value: (row) => row.priority },
  { key: 'date', label: 'Date', value: (row) => row.date ? new Date(row.date).toLocaleString() : '\u2014' },
  { key: 'read', label: 'Read', value: (row) => (row.read ? 'Yes' : 'No') },
];

export function NotificationsPage() {
  const [filter, setFilter] = useState('all');
  const resource = useResource(() => notificationService.list({ filter }), filter);
  const readAction = useAction(notificationService.markRead);
  const deleteAction = useAction(notificationService.remove);

  const rows = collectionItems(resource.data);

  const markRead = async (id) => {
    await readAction.execute(id);
    resource.reload();
  };

  const remove = async (id) => {
    await deleteAction.execute(id);
    resource.reload();
  };

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
          emptyDescription="New alerts will appear here when events occur."
          rowActions={(row) => (
            <div className="table-action-group">
              {!row.read && (
                <Button variant="outline" size="sm" onClick={() => markRead(row.id)} disabled={readAction.status === 'loading'}>
                  Mark read
                </Button>
              )}
              <Button variant="outline" size="sm" onClick={() => remove(row.id)} disabled={deleteAction.status === 'loading'}>
                Delete
              </Button>
            </div>
          )}
        />
      </Card>
    </div>
  );
}
