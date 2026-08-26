import { useMemo, useState } from 'react';
import { Button } from '../common/Button';
import { Icon } from '../common/Icon';
import { recordId } from '../../utils/collections';

const ADD_NEW = '__add_new__';
const assetStatuses = [
  { value: 'AVAILABLE', label: 'Available' },
  { value: 'UNDER_MAINTENANCE', label: 'Maintenance' },
  { value: 'IN_TRANSIT', label: 'In Transit' },
  { value: 'DISPOSED', label: 'Disposed' },
  { value: 'LOST', label: 'Lost' },
];
const assetConditions = ['NEW', 'GOOD', 'FAIR', 'POOR', 'DAMAGED'];

const initialValues = (asset) => ({
  assetTag: asset?.assetTag || '',
  name: asset?.name || '',
  categoryId: recordId(asset, 'categoryId'),
  newCategoryName: '',
  serialNumber: asset?.serialNumber || '',
  brand: asset?.brand || '',
  newBrand: '',
  modelNo: asset?.modelNo || '',
  departmentId: recordId(asset, 'departmentId'),
  newDepartmentName: '',
  newDepartmentCode: '',
  locationId: recordId(asset, 'locationId'),
  vendorId: recordId(asset, 'vendorId'),
  purchaseDate: asset?.purchaseDate || '',
  purchaseCost: asset?.purchaseCost ?? '',
  warrantyPeriodMonths: asset?.warrantyPeriodMonths ?? '',
  currency: asset?.currency || 'USD',
  condition: asset?.condition || asset?.assetCondition || 'GOOD',
  status: asset?.status || 'AVAILABLE',
  notes: asset?.notes || '',
});

const uniqueBrands = (assets) => Array.from(new Set(
  assets.map((asset) => String(asset?.brand || asset?.manufacturer || '').trim()).filter(Boolean),
)).sort((first, second) => first.localeCompare(second));

export function AssetForm({
  asset,
  categories = [],
  departments = [],
  vendors = [],
  locations = [],
  assets = [],
  submitting = false,
  actionError,
  onClose,
  onSubmit,
}) {
  const [form, setForm] = useState(() => initialValues(asset));
  const isEditing = Boolean(recordId(asset, 'id'));
  const brands = useMemo(() => uniqueBrands(assets), [assets]);
  const activeCategories = useMemo(
    () => categories.filter((category) => category.active !== false || recordId(category, 'id') === form.categoryId),
    [categories, form.categoryId],
  );
  const activeVendors = useMemo(
    () => vendors.filter((vendor) => String(vendor.status || 'ACTIVE').toUpperCase() === 'ACTIVE' || recordId(vendor, 'id') === form.vendorId),
    [vendors, form.vendorId],
  );
  const selectedVendor = vendors.find((vendor) => recordId(vendor, 'id') === form.vendorId);

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }));

  const changeCategory = (value) => {
    setForm((current) => ({ ...current, categoryId: value, newCategoryName: value === ADD_NEW ? current.newCategoryName : '' }));
  };

  const changeDepartment = (value) => {
    setForm((current) => ({
      ...current,
      departmentId: value,
      newDepartmentName: value === ADD_NEW ? current.newDepartmentName : '',
      newDepartmentCode: value === ADD_NEW ? current.newDepartmentCode : '',
    }));
  };

  const changeBrand = (value) => {
    setForm((current) => ({ ...current, brand: value, newBrand: value === ADD_NEW ? current.newBrand : '' }));
  };

  const submit = (event) => {
    event.preventDefault();
    onSubmit({
      ...form,
      categoryId: form.categoryId === ADD_NEW ? '' : form.categoryId,
      departmentId: form.departmentId === ADD_NEW ? '' : form.departmentId,
      brand: form.brand === ADD_NEW ? form.newBrand : form.brand,
      vendorId: form.vendorId || null,
      locationId: form.locationId || null,
      purchaseDate: form.purchaseDate || null,
      purchaseCost: form.purchaseCost === '' ? null : Number(form.purchaseCost),
      warrantyPeriodMonths: form.warrantyPeriodMonths === '' ? null : Number(form.warrantyPeriodMonths),
      notes: form.notes || null,
    });
  };

  return (
    <form className="form-grid asset-form" onSubmit={submit}>
      <div className="asset-form-section">
        <h3>Asset identity</h3>
        <div className="form-fields two">
          <label>
            Asset tag
            <input value={form.assetTag} onChange={(event) => update('assetTag', event.target.value)} placeholder="e.g. AST-0001" required disabled={submitting} />
          </label>
          <label>
            Asset Name
            <input value={form.name} onChange={(event) => update('name', event.target.value)} placeholder="e.g. Industrial laptop" required disabled={submitting} />
          </label>
          <label>
            Category
            <select value={form.categoryId} onChange={(event) => changeCategory(event.target.value)} required disabled={submitting}>
              <option value="">Select category</option>
              {activeCategories.map((category) => <option key={recordId(category, 'id')} value={recordId(category, 'id')}>{category.name}</option>)}
              <option value={ADD_NEW}>+ Add New</option>
            </select>
          </label>
          <label>
            Model No
            <input value={form.modelNo} onChange={(event) => update('modelNo', event.target.value)} placeholder="Enter model number" required disabled={submitting} />
          </label>
          {form.categoryId === ADD_NEW && (
            <label className="form-field-wide">
              New category name
              <input value={form.newCategoryName} onChange={(event) => update('newCategoryName', event.target.value)} placeholder="Enter a new category" required disabled={submitting} />
            </label>
          )}
          <label>
            <span className="field-label">Brand <span className="optional-label">(optional)</span></span>
            <select value={form.brand} onChange={(event) => changeBrand(event.target.value)} disabled={submitting}>
              <option value="">No brand selected</option>
              {brands.map((brand) => <option key={brand} value={brand}>{brand}</option>)}
              <option value={ADD_NEW}>+ Add New</option>
            </select>
          </label>
          <label>
            <span className="field-label">Serial number <span className="optional-label">(optional)</span></span>
            <input value={form.serialNumber} onChange={(event) => update('serialNumber', event.target.value)} placeholder="Enter serial number" disabled={submitting} />
          </label>
          {form.brand === ADD_NEW && (
            <label className="form-field-wide">
              New brand
              <input value={form.newBrand} onChange={(event) => update('newBrand', event.target.value)} placeholder="Enter a new brand" required disabled={submitting} />
            </label>
          )}
        </div>
      </div>

      <div className="asset-form-section">
        <h3>Ownership and lifecycle</h3>
        <div className="form-fields two">
          <label>
            <span className="field-label">Department <span className="optional-label">(optional)</span></span>
            <select value={form.departmentId} onChange={(event) => changeDepartment(event.target.value)} disabled={submitting}>
              <option value="">No department selected</option>
              {departments.map((department) => <option key={recordId(department, 'id')} value={recordId(department, 'id')}>{department.name}{department.code ? ` (${department.code})` : ''}</option>)}
              <option value={ADD_NEW}>+ Add New</option>
            </select>
          </label>
          <label>
            <span className="field-label">Branch <span className="optional-label">(optional)</span></span>
            <select value={form.locationId} onChange={(event) => update('locationId', event.target.value)} disabled={submitting}>
              <option value="">No location selected</option>
              {locations.map((location) => <option key={recordId(location, 'id')} value={recordId(location, 'id')}>{location.name}{location.code ? ` (${location.code})` : ''}</option>)}
            </select>
          </label>
          {form.departmentId === ADD_NEW && (
            <div className="form-fields two form-field-wide catalog-add-fields">
              <label>
                New department name
                <input value={form.newDepartmentName} onChange={(event) => update('newDepartmentName', event.target.value)} placeholder="e.g. Operations" required disabled={submitting} />
              </label>
              <label>
                New department code
                <input value={form.newDepartmentCode} onChange={(event) => update('newDepartmentCode', event.target.value)} placeholder="e.g. OPS" required disabled={submitting} />
              </label>
            </div>
          )}
          {isEditing && form.status === 'ASSIGNED' && (
            <label>
              Status
              <input value="Assigned automatically from the active assignment" readOnly />
            </label>
          )}
          {isEditing && form.status !== 'ASSIGNED' && (
            <label>
              Status
              <select value={form.status} onChange={(event) => update('status', event.target.value)} disabled={submitting}>
              {assetStatuses.map((status) => <option key={status.value} value={status.value}>{status.label}</option>)}
              </select>
            </label>
          )}
          <label>
            Condition
            <select value={form.condition} onChange={(event) => update('condition', event.target.value)} disabled={submitting}>
              {assetConditions.map((condition) => <option key={condition} value={condition}>{condition}</option>)}
            </select>
          </label>
        </div>
      </div>

      <div className="asset-form-section">
        <h3>Vendor and purchase details</h3>
        <div className="form-fields two">
          <label className="form-field-wide">
            <span className="field-label">Vendor <span className="optional-label">(optional)</span></span>
            <select value={form.vendorId} onChange={(event) => update('vendorId', event.target.value)} disabled={submitting}>
              <option value="">No vendor selected</option>
              {activeVendors.map((vendor) => <option key={recordId(vendor, 'id')} value={recordId(vendor, 'id')}>{vendor.name}{vendor.vendorCode ? ` (${vendor.vendorCode})` : ''}</option>)}
            </select>
          </label>
          <label>
            Vendor Name
            <input value={selectedVendor?.name || ''} readOnly />
          </label>
          <label>
            Vendor Location
            <input value={selectedVendor?.address || ''} readOnly />
          </label>
          <label>
            Vendor No (Contact no)
            <input value={selectedVendor?.phone || ''} readOnly />
          </label>
          <label>
            Vendor email
            <input value={selectedVendor?.email || ''} readOnly />
          </label>
          <label>
            <span className="field-label">Purchase date <span className="optional-label">(optional)</span></span>
            <input type="date" value={form.purchaseDate} onChange={(event) => update('purchaseDate', event.target.value)} disabled={submitting} />
          </label>
          <label>
            <span className="field-label">Price <span className="optional-label">(optional)</span></span>
            <input type="number" min="0" step="0.01" value={form.purchaseCost} onChange={(event) => update('purchaseCost', event.target.value)} placeholder="0.00" disabled={submitting} />
          </label>
          <label>
            <span className="field-label">Warranty period <span className="optional-label">(optional)</span></span>
            <input type="number" min="0" step="1" value={form.warrantyPeriodMonths} onChange={(event) => update('warrantyPeriodMonths', event.target.value)} placeholder="e.g. 24" disabled={submitting} />
          </label>
          <label>
            <span className="field-label">Currency <span className="optional-label">(optional)</span></span>
            <input value={form.currency} onChange={(event) => update('currency', event.target.value.toUpperCase())} maxLength="3" placeholder="USD" disabled={submitting} />
          </label>
          <label className="form-field-wide">
            <span className="field-label">Notes <span className="optional-label">(optional)</span></span>
            <textarea value={form.notes} onChange={(event) => update('notes', event.target.value)} placeholder="Add any useful asset notes" rows="3" disabled={submitting} />
          </label>
        </div>
      </div>

      {actionError && <div className="inline-error"><Icon name="warning" size={16} />{actionError.message}</div>}

      <div className="modal-actions">
        <Button variant="outline" onClick={onClose} disabled={submitting}>Cancel</Button>
        <Button type="submit" disabled={submitting}>{submitting ? 'Saving...' : isEditing ? 'Save changes' : 'Add asset'}</Button>
      </div>
    </form>
  );
}
