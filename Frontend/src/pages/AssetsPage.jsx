import { useState } from 'react';
import { Button } from '../components/common/Button';
import { Card } from '../components/common/Card';
import { Icon } from '../components/common/Icon';
import { IconActionButton } from '../components/common/IconActionButton';
import { Modal } from '../components/common/Modal';
import { PageHeader } from '../components/common/PageHeader';
import { StatCard } from '../components/common/StatCard';
import { Table } from '../components/common/Table';
import { AssetDetailsModal } from '../components/domain/AssetDetailsModal';
import { AssetForm } from '../components/domain/AssetForm';
import { resourceConfigs } from '../constants/resourceConfigs';
import { useResource } from '../hooks/useResource';
import { assetService, categoryService, departmentService, locationService, vendorService } from '../services/resources';
import { collectionItems, recordId } from '../utils/collections';

const config = resourceConfigs.assets;
const idleLookup = { status: 'idle', categories: [], departments: [], vendors: [], locations: [], assets: [], error: null };

export function AssetsPage() {
  const resource = useResource(() => assetService.list(), 'assets');
  const [formState, setFormState] = useState({ open: false, asset: null });
  const [lookup, setLookup] = useState(idleLookup);
  const [detailsAsset, setDetailsAsset] = useState(null);
  const [detailsVendor, setDetailsVendor] = useState(null);
  const [deleteAsset, setDeleteAsset] = useState(null);
  const [mutation, setMutation] = useState({ status: 'idle', error: null });
  const rows = collectionItems(resource.data);
  const stats = config.stats.map((stat) => ({
    ...stat,
    value: resource.status === 'success' ? stat.getValue(rows) : '—',
  }));
  const openDetails = async (asset) => {
    setDetailsAsset(asset);
    const vendorId = recordId(asset, 'vendorId');
    const cachedVendor = lookup.vendors.find((vendor) => recordId(vendor, 'id') === vendorId);
    setDetailsVendor(cachedVendor || null);
    if (!vendorId || cachedVendor) return;
    try {
      const vendor = await vendorService.get(vendorId);
      setDetailsVendor(vendor);
    } catch {
      // The asset detail view still contains the vendor name if its contact record cannot be loaded.
    }
  };

  const columns = config.columns.map((column) => {
    if (column.key !== 'assetTag') return column;
    return {
      ...column,
      render: (row) => (
        <span className="asset-tag-cell">
          <span>{row.assetTag || '—'}</span>
          <IconActionButton icon="info" label={`View details for ${row.assetTag || row.name || 'asset'}`} onClick={() => openDetails(row)} />
        </span>
      ),
    };
  });

  const loadLookups = async () => {
    setLookup({ ...idleLookup, status: 'loading' });
    try {
      const [categoryData, departmentData, vendorData, locationData, assetData] = await Promise.all([
        categoryService.list(), departmentService.list(), vendorService.list(), locationService.list(), assetService.list(),
      ]);
      setLookup({
        status: 'success',
        categories: collectionItems(categoryData),
        departments: collectionItems(departmentData),
        vendors: collectionItems(vendorData),
        locations: collectionItems(locationData),
        assets: collectionItems(assetData),
        error: null,
      });
    } catch (error) {
      setLookup({ ...idleLookup, status: 'error', error });
    }
  };

  const openForm = async (asset = null) => {
    setMutation({ status: 'idle', error: null });
    setFormState({ open: true, asset });
    await loadLookups();
  };

  const closeForm = () => {
    if (mutation.status !== 'loading') setFormState({ open: false, asset: null });
  };

  const submitAsset = async (form) => {
    setMutation({ status: 'loading', error: null });
    try {
      let categoryId = form.categoryId;
      let departmentId = form.departmentId;
      if (form.newCategoryName?.trim()) {
        const category = await categoryService.create({ name: form.newCategoryName.trim(), active: true });
        categoryId = recordId(category, 'id');
      }
      if (form.newDepartmentName?.trim() || form.newDepartmentCode?.trim()) {
        const department = await departmentService.create({
          name: form.newDepartmentName.trim(),
          code: form.newDepartmentCode.trim(),
        });
        departmentId = recordId(department, 'id');
      }
      const request = {
        ...Object.fromEntries(
          Object.entries(form).filter(([key]) => !['newCategoryName', 'newDepartmentName', 'newDepartmentCode', 'newBrand'].includes(key)),
        ),
        categoryId,
        departmentId: departmentId || null,
      };
      if (formState.asset) await assetService.update(recordId(formState.asset, 'id'), request);
      else await assetService.create(request);
      setMutation({ status: 'success', error: null });
      setFormState({ open: false, asset: null });
      resource.reload();
    } catch (error) {
      setMutation({ status: 'error', error });
    }
  };

  const removeAsset = async () => {
    if (!deleteAsset) return;
    setMutation({ status: 'loading', error: null });
    try {
      await assetService.remove(recordId(deleteAsset, 'id'));
      setMutation({ status: 'success', error: null });
      setDeleteAsset(null);
      resource.reload();
    } catch (error) {
      setMutation({ status: 'error', error });
    }
  };

  return (
    <div>
      <PageHeader
        title={config.title}
        description={config.description}
        breadcrumbs={[{ label: config.title }]}
        actions={<><Button variant="outline" icon="download">Export</Button><Button icon="plus" onClick={() => openForm()}>Add asset</Button></>}
      />

      <div className="stats-grid">{stats.map((stat) => <StatCard key={stat.label} {...stat} />)}</div>

      <Card className="table-wrap">
        <Table
          columns={columns}
          rows={rows}
          status={resource.status}
          error={resource.error}
          onRetry={resource.reload}
          emptyTitle="No assets yet"
          emptyDescription="Add an asset to start tracking your inventory."
          searchPlaceholder="Search assets..."
          rowActionsLabel="Action"
          rowActions={(row) => (
            <div className="table-action-group">
              <IconActionButton icon="edit" label={`Edit ${row.assetTag || row.name || 'asset'}`} tone="edit" onClick={() => openForm(row)} />
              <IconActionButton icon="trash" label={`Delete ${row.assetTag || row.name || 'asset'}`} tone="danger" onClick={() => { setMutation({ status: 'idle', error: null }); setDeleteAsset(row); }} />
            </div>
          )}
        />
      </Card>

      <Modal open={formState.open} onClose={closeForm} className="asset-modal" title={formState.asset ? 'Edit asset' : 'Add asset'} description={formState.asset ? 'Update the asset record, including its current status.' : 'Use existing catalog values or add a new category, brand, or department.'}>
        {lookup.status === 'loading' && <div className="assignment-form-loading"><div className="spinner" aria-hidden="true" /><p>Loading catalog values and vendor details...</p></div>}
        {lookup.status === 'error' && <div className="inline-error"><Icon name="warning" size={16} />{lookup.error?.message}</div>}
        {lookup.status === 'success' && (
          <AssetForm
            key={recordId(formState.asset, 'id') || 'new'}
            asset={formState.asset}
            categories={lookup.categories}
            departments={lookup.departments}
            vendors={lookup.vendors}
            locations={lookup.locations}
            assets={lookup.assets}
            submitting={mutation.status === 'loading'}
            actionError={mutation.status === 'error' ? mutation.error : null}
            onClose={closeForm}
            onSubmit={submitAsset}
          />
        )}
      </Modal>

      <AssetDetailsModal asset={detailsAsset} vendor={detailsVendor} onClose={() => { setDetailsAsset(null); setDetailsVendor(null); }} />

      <Modal open={Boolean(deleteAsset)} onClose={() => mutation.status !== 'loading' && setDeleteAsset(null)} title="Retire asset" description="This removes the asset from active inventory by marking it retired. Its history stays available for audit.">
        <div className="confirm-copy">Are you sure you want to retire <strong>{deleteAsset?.assetTag || deleteAsset?.name}</strong>?</div>
        {mutation.status === 'error' && <div className="inline-error"><Icon name="warning" size={16} />{mutation.error?.message}</div>}
        <div className="modal-actions">
          <Button variant="outline" onClick={() => setDeleteAsset(null)} disabled={mutation.status === 'loading'}>Cancel</Button>
          <Button variant="danger" onClick={removeAsset} disabled={mutation.status === 'loading'}>{mutation.status === 'loading' ? 'Retiring...' : 'Retire asset'}</Button>
        </div>
      </Modal>
    </div>
  );
}
