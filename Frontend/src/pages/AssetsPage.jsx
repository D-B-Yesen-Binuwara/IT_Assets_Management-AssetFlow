import { useState } from 'react';
import { Button } from '../components/common/Button';
import { Card } from '../components/common/Card';
import { Icon } from '../components/common/Icon';
import { IconActionButton } from '../components/common/IconActionButton';
import { Modal } from '../components/common/Modal';
import { PageHeader } from '../components/common/PageHeader';
import { StatCard } from '../components/common/StatCard';
import { StatusBadge } from '../components/common/StatusBadge';
import { StatusPicker } from '../components/common/StatusPicker';
import { Table } from '../components/common/Table';
import { AssetDetailsModal } from '../components/domain/AssetDetailsModal';
import { AssetForm } from '../components/domain/AssetForm';
import { AssetStatusChangeModal } from '../components/domain/AssetStatusChangeModal';
import { resourceConfigs } from '../constants/resourceConfigs';
import { useResource } from '../hooks/useResource';
import { assetService, categoryService, departmentService, employeeService, locationService, vendorService, warrantyService } from '../services/resources';
import { collectionItems, recordId } from '../utils/collections';

const config = resourceConfigs.assets;
const idleLookup = { status: 'idle', categories: [], departments: [], vendors: [], locations: [], assets: [], employees: [], error: null };
const editableStatuses = [
  { value: 'AVAILABLE', label: 'Available' },
  { value: 'UNDER_MAINTENANCE', label: 'Maintenance' },
  { value: 'IN_TRANSIT', label: 'In Transit' },
  { value: 'DISPOSED', label: 'Disposed' },
  { value: 'LOST', label: 'Lost' },
];

export function AssetsPage() {
  const resource = useResource(() => assetService.list(), 'assets');
  const [formState, setFormState] = useState({ open: false, asset: null });
  const [lookup, setLookup] = useState(idleLookup);
  const [detailsAsset, setDetailsAsset] = useState(null);
  const [detailsVendor, setDetailsVendor] = useState(null);
  const [deleteAsset, setDeleteAsset] = useState(null);
  const [statusModal, setStatusModal] = useState({ asset: null, status: null });
  const [mutation, setMutation] = useState({ status: 'idle', error: null });
  const [statusAction, setStatusAction] = useState({ status: 'idle', error: null });
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
          <IconActionButton icon="info" className="asset-info-action" label={`View details for ${row.assetTag || row.name || 'asset'}`} onClick={() => openDetails(row)} />
          <span>{row.assetTag || '—'}</span>
        </span>
      ),
    };
  });

  const tableColumns = columns.map((column) => {
    if (column.key !== 'status') return column;
    return {
      ...column,
      render: (row) => {
        const value = String(row.status || '').toUpperCase();
        if (!editableStatuses.some((status) => status.value === value)) return <StatusBadge status={row.status} />;
        return <StatusPicker value={value} options={editableStatuses} label={`Change status for ${row.assetTag || row.name || 'asset'}`} disabled={statusAction.status === 'loading'} onChange={(status) => openStatusChange(row, status)} />;
      },
    };
  });

  const loadLookups = async () => {
    setLookup({ ...idleLookup, status: 'loading' });
    try {
      const [categoryData, departmentData, vendorData, locationData, assetData, employeeData] = await Promise.all([
        categoryService.list(), departmentService.list(), vendorService.list(), locationService.list(), assetService.list(), employeeService.list(),
      ]);
      setLookup({
        status: 'success',
        categories: collectionItems(categoryData),
        departments: collectionItems(departmentData),
        vendors: collectionItems(vendorData),
        locations: collectionItems(locationData),
        assets: collectionItems(assetData),
        employees: collectionItems(employeeData),
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
      const assetOnlyKeys = new Set([
        'newCategoryName', 'newDepartmentName', 'newDepartmentCode', 'newBrand',
        'warrantyId', 'warrantyVendorId', 'warrantyPolicyNumber', 'warrantyStartDate', 'warrantyEndDate', 'warrantyCoverage',
        'statusReason', 'statusDescription', 'statusEffectiveDate', 'maintenancePriority', 'maintenanceStartDate',
        'maintenanceDueDate', 'maintenanceVendorId', 'maintenanceAssignedToEmployeeId', 'disposalDate',
        'disposalMethod', 'disposalProceeds', 'statusNotes', 'status',
      ]);
      const request = {
        ...Object.fromEntries(
          Object.entries(form).filter(([key]) => !assetOnlyKeys.has(key)),
        ),
        categoryId,
        departmentId: departmentId || null,
        currency: 'LKR',
      };
      const savedAsset = formState.asset
        ? await assetService.update(recordId(formState.asset, 'id'), request)
        : await assetService.create({ ...request, status: 'AVAILABLE' });
      const assetId = recordId(savedAsset, 'id');
      const hasWarranty = [form.warrantyVendorId, form.warrantyPolicyNumber, form.warrantyStartDate, form.warrantyEndDate, form.warrantyCoverage]
        .some((entry) => entry !== null && entry !== undefined && String(entry).trim() !== '');
      if (hasWarranty) {
        const warrantyRequest = {
          assetId,
          vendorId: form.warrantyVendorId || null,
          policyNumber: form.warrantyPolicyNumber || null,
          startDate: form.warrantyStartDate || null,
          endDate: form.warrantyEndDate || null,
          coverage: form.warrantyCoverage || null,
          current: true,
        };
        if (form.warrantyId) await warrantyService.update(form.warrantyId, warrantyRequest);
        else await warrantyService.create(warrantyRequest);
      }
      const previousStatus = formState.asset?.status || 'AVAILABLE';
      if (form.status !== previousStatus) {
        await assetService.changeStatus(assetId, statusRequest(form.status, form));
      }
      setMutation({ status: 'success', error: null });
      setFormState({ open: false, asset: null });
      resource.reload();
    } catch (error) {
      setMutation({ status: 'error', error });
    }
  };

  const openStatusChange = (asset, status) => {
    setStatusAction({ status: 'idle', error: null });
    setStatusModal({ asset, status });
    if (lookup.status !== 'success') loadLookups();
  };

  const changeStatus = async (details) => {
    const { asset, status } = statusModal;
    if (!asset || !status) return;
    setStatusAction({ status: 'loading', error: null });
    try {
      await assetService.changeStatus(recordId(asset, 'id'), statusRequest(status, details));
      setStatusAction({ status: 'success', error: null });
      setStatusModal({ asset: null, status: null });
      resource.reload();
    } catch (error) {
      setStatusAction({ status: 'error', error });
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
          columns={tableColumns}
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
        {statusAction.status === 'error' && <div className="inline-error table-inline-error"><Icon name="warning" size={16} />{statusAction.error?.message}</div>}
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
            employees={lookup.employees}
            submitting={mutation.status === 'loading'}
            actionError={mutation.status === 'error' ? mutation.error : null}
            onClose={closeForm}
            onSubmit={submitAsset}
          />
        )}
      </Modal>

      <AssetDetailsModal asset={detailsAsset} vendor={detailsVendor} onClose={() => { setDetailsAsset(null); setDetailsVendor(null); }} />

      <AssetStatusChangeModal
        asset={statusModal.asset}
        status={statusModal.status}
        vendors={lookup.vendors}
        employees={lookup.employees}
        submitting={statusAction.status === 'loading'}
        error={statusAction.status === 'error' ? statusAction.error : null}
        onClose={() => statusAction.status !== 'loading' && setStatusModal({ asset: null, status: null })}
        onSubmit={changeStatus}
      />

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

function statusRequest(status, form) {
  return {
    status,
    reason: form.statusReason || null,
    description: form.statusDescription || null,
    effectiveDate: form.statusEffectiveDate || null,
    priority: form.maintenancePriority || null,
    startDate: form.maintenanceStartDate || null,
    dueDate: form.maintenanceDueDate || null,
    vendorId: form.maintenanceVendorId || null,
    assignedToEmployeeId: form.maintenanceAssignedToEmployeeId || null,
    disposalDate: form.disposalDate || null,
    disposalMethod: form.disposalMethod || null,
    proceeds: form.disposalProceeds === '' ? null : Number(form.disposalProceeds),
    notes: form.statusNotes || null,
  };
}
