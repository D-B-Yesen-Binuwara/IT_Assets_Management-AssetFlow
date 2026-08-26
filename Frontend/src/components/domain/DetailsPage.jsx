import { Link, useParams } from 'react-router-dom';
import { Button } from '../common/Button';
import { Card, CardHeader } from '../common/Card';
import { Icon } from '../common/Icon';
import { PageHeader } from '../common/PageHeader';
import { EmptyState, ErrorState, LoadingState } from '../common/State';
import { Table } from '../common/Table';
import { AssetTransferHistory } from './AssetTransferHistory';
import { useResource } from '../../hooks/useResource';
import { collectionItems } from '../../utils/collections';

const visibleDetailFields = (item) =>
  Object.entries(item)
    .filter(
      ([key, value]) =>
        ['id', 'description'].includes(key) === false &&
        ['object', 'function'].includes(typeof value) === false,
    )
    .slice(0, 12);

const formatLabel = (key) =>
  key.replace(/[A-Z]/g, (letter) => ` ${letter}`).replace(/^./, (letter) => letter.toUpperCase());

export function DetailsPage({ config }) {
  const { id } = useParams();
  const resource = useResource(() => config.service.get(id), id);
  const activity = useResource(
    () => config.service.lifecycle ? config.service.lifecycle(id) : Promise.resolve([]),
    id,
  );

  if (resource.status === 'loading') {
    return <LoadingState label={`Loading ${config.singular}`} />;
  }

  if (resource.status === 'error') {
    return (
      <>
        <PageHeader
          title={`${config.singular} details`}
          breadcrumbs={[{ label: config.title, to: config.listPath }]}
        />
        <ErrorState error={resource.error} onRetry={resource.reload} />
      </>
    );
  }

  if (!resource.data) {
    return (
      <>
        <PageHeader
          title={`${config.singular} details`}
          breadcrumbs={[{ label: config.title, to: config.listPath }]}
        />
        <EmptyState
          icon={config.icon}
          title={`${config.singular} not found`}
          description="No record was returned for this identifier."
        />
      </>
    );
  }

  const item = resource.data;

  // The detail page renders only primitive fields supplied by the backend record.
  return (
    <>
      <PageHeader
        title={item.name || item.title || `${config.singular} ${id}`}
        description={item.description || `Reference ${id}`}
        breadcrumbs={[{ label: config.title, to: config.listPath }, { label: id }]}
        actions={
          <>
            <Button variant="outline" icon="arrowLeft">
              Back
            </Button>
            <Button icon="more">More actions</Button>
          </>
        }
      />

      <div className="detail-grid">
        <Card>
          <CardHeader title="Overview" description="Current record details." />
          <div className="detail-fields">
            {visibleDetailFields(item).map(([key, value]) => (
              <div key={key}>
                <span>{formatLabel(key)}</span>
                <strong>{String(value ?? '-')}</strong>
              </div>
            ))}
          </div>
        </Card>

        <Card>
          <CardHeader title="Activity" description="Lifecycle events for this record." />
          <Table
            columns={[
              { key: 'eventType', label: 'Event', value: (row) => row.eventType || '\u2014' },
              { key: 'eventAt', label: 'Time', value: (row) => row.eventAt ? new Date(row.eventAt).toLocaleString() : '\u2014' },
              { key: 'fromStatus', label: 'From', value: (row) => row.fromStatus || '\u2014' },
              { key: 'toStatus', label: 'To', value: (row) => row.toStatus || '\u2014' },
            ]}
            rows={collectionItems(activity.data)}
            status={activity.status}
            error={activity.error}
            onRetry={activity.reload}
            emptyTitle="No lifecycle events"
            emptyDescription="Lifecycle events will appear when this asset changes."
          />
        </Card>
      </div>

      {config.transferHistory && <AssetTransferHistory assetId={id} />}

      <Link className="back-link" to={config.listPath}>
        <Icon name="arrowLeft" size={16} /> Return to {config.title}
      </Link>
    </>
  );
}
