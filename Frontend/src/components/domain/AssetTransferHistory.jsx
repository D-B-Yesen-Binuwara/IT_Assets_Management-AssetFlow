import { Card, CardHeader } from '../common/Card';
import { StatusBadge } from '../common/StatusBadge';
import { Table } from '../common/Table';
import { useResource } from '../../hooks/useResource';
import { assetService } from '../../services/resources';
import { collectionItems } from '../../utils/collections';

const field = (...keys) => (row) =>
  keys.map((key) => row?.[key]).find((value) => value !== undefined && value !== null) || '-';

const columns = [
  {
    key: 'previousEmployee',
    label: 'Previous employee',
    value: field('previousEmployeeName', 'previousEmployee'),
  },
  { key: 'newEmployee', label: 'New employee', value: field('newEmployeeName', 'newEmployee') },
  { key: 'reason', label: 'Reason', value: field('reason') },
  { key: 'transferredAt', label: 'Transferred', value: field('transferredAt', 'requestedAt') },
  {
    key: 'status',
    label: 'Status',
    value: field('status'),
    render: (row) => <StatusBadge status={row.status} />,
  },
];

export function AssetTransferHistory({ assetId }) {
  const resource = useResource(() => assetService.transferHistory(assetId), assetId);
  const rows = collectionItems(resource.data);

  return (
    <Card className="transfer-history-card">
      <CardHeader
        title="Transfer history"
        description="Previous and current custodians recorded by the transfer workflow."
      />
      <Table
        columns={columns}
        rows={rows}
        status={resource.status}
        error={resource.error}
        onRetry={resource.reload}
        emptyTitle="No transfers recorded"
        emptyDescription="Asset transfers will appear here after the transfer API is connected."
        searchPlaceholder="Search transfer history..."
      />
    </Card>
  );
}
