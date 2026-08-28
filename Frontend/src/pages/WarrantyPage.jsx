import { useState } from 'react';
import { Button } from '../components/common/Button';
import { Card } from '../components/common/Card';
import { Icon } from '../components/common/Icon';
import { IconActionButton } from '../components/common/IconActionButton';
import { Modal } from '../components/common/Modal';
import { PageHeader } from '../components/common/PageHeader';
import { StatCard } from '../components/common/StatCard';
import { Table } from '../components/common/Table';
import { WarrantyForm } from '../components/domain/WarrantyForm';
import { RecordDetailsModal } from '../components/domain/RecordDetailsModal';
import { resourceConfigs } from '../constants/resourceConfigs';
import { useResource } from '../hooks/useResource';
import { assetService, categoryService, vendorService, warrantyService } from '../services/resources';
import { collectionItems } from '../utils/collections';

const config = resourceConfigs.warranty;

export function WarrantyPage() {
  const resource = useResource(() => warrantyService.list(), 'warranty');
  const [modalOpen, setModalOpen] = useState(false);
  const [lookup, setLookup] = useState({ status: 'idle', categories: [], assets: [], vendors: [], error: null });
  const [action, setAction] = useState({ status: 'idle', error: null });
  const [detailsRecord, setDetailsRecord] = useState(null);
  const rows = collectionItems(resource.data);
  const stats = config.stats.map((stat) => ({ ...stat, value: resource.status === 'success' ? stat.getValue(rows) : '—' }));
  const columns = config.columns.map((column) => column.key === config.infoColumn ? {
    ...column,
    render: (row) => <span className="asset-tag-cell"><IconActionButton icon="info" className="asset-info-action" label={`View warranty for ${row.assetTag}`} onClick={() => setDetailsRecord(row)} /><span>{column.value(row)}</span></span>,
  } : column);

  const openModal = async () => {
    setModalOpen(true);
    setLookup({ status: 'loading', categories: [], assets: [], vendors: [], error: null });
    try {
      const [categoryData, assetData, vendorData] = await Promise.all([categoryService.list(), assetService.list(), vendorService.list()]);
      setLookup({ status: 'success', categories: collectionItems(categoryData), assets: collectionItems(assetData), vendors: collectionItems(vendorData), error: null });
    } catch (error) {
      setLookup({ status: 'error', categories: [], assets: [], vendors: [], error });
    }
  };
  const closeModal = () => { if (action.status !== 'loading') setModalOpen(false); };
  const submit = async (payload) => {
    setAction({ status: 'loading', error: null });
    try {
      await warrantyService.create(payload);
      setAction({ status: 'success', error: null });
      setModalOpen(false);
      resource.reload();
    } catch (error) {
      setAction({ status: 'error', error });
    }
  };

  return (
    <div>
      <PageHeader title={config.title} description={config.description} breadcrumbs={[{ label: config.title }]} actions={<><Button variant="outline" icon="download">Export</Button><Button icon="plus" onClick={openModal}>Add warranty</Button></>} />
      <div className="stats-grid">{stats.map((stat) => <StatCard key={stat.label} {...stat} />)}</div>
      <Card className="table-wrap"><Table columns={columns} rows={rows} status={resource.status} error={resource.error} onRetry={resource.reload} emptyTitle="No warranties yet" emptyDescription="Add a warranty policy after an asset has been registered." searchPlaceholder="Search warranties..." /></Card>
      <Modal open={modalOpen} onClose={closeModal} title="Add warranty" description="Choose the asset category first, then select the verified asset by tag or name.">
        {lookup.status === 'loading' && <div className="assignment-form-loading"><div className="spinner" aria-hidden="true" /><p>Loading categories, assets, and providers...</p></div>}
        {lookup.status === 'error' && <div className="inline-error"><Icon name="warning" size={16} />{lookup.error?.message}</div>}
        {lookup.status === 'success' && <WarrantyForm categories={lookup.categories} assets={lookup.assets} vendors={lookup.vendors} submitting={action.status === 'loading'} actionError={action.status === 'error' ? action.error : null} onClose={closeModal} onSubmit={submit} />}
      </Modal>
      <RecordDetailsModal record={detailsRecord} title={detailsRecord?.assetTag} description={`Warranty details for ${detailsRecord?.assetName || 'this asset'}.`} fields={config.detailFields} onClose={() => setDetailsRecord(null)} />
    </div>
  );
}
