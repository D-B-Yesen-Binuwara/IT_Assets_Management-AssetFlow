import { useMemo, useState } from 'react';
import { Button } from '../common/Button';
import { Icon } from '../common/Icon';
import { recordId } from '../../utils/collections';

const today = () => new Date().toISOString().slice(0, 10);
const normalize = (value) => String(value || '').trim().toLocaleLowerCase();
const assetModelNo = (asset) => asset?.modelNo || asset?.model || '';
const assetBrand = (asset) => asset?.brand || asset?.manufacturer || '';

const initialForm = {
  categoryId: '',
  assetTag: '',
  assetName: '',
  vendorId: '',
  policyNumber: '',
  startDate: today(),
  endDate: '',
  coverage: '',
};

export function WarrantyForm({ categories = [], assets = [], vendors = [], submitting = false, actionError, onClose, onSubmit }) {
  const [form, setForm] = useState(initialForm);
  const [selectedAsset, setSelectedAsset] = useState(null);
  const [assetMessage, setAssetMessage] = useState('');
  const activeCategories = useMemo(() => categories.filter((category) => category.active !== false), [categories]);
  const categoryAssets = useMemo(
    () => assets.filter((asset) => recordId(asset, 'categoryId') === form.categoryId),
    [assets, form.categoryId],
  );
  const sameNameAssets = useMemo(
    () => categoryAssets.filter((asset) => normalize(asset.name) === normalize(form.assetName)),
    [categoryAssets, form.assetName],
  );

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }));
  const selectAsset = (asset) => {
    setSelectedAsset(asset);
    setForm((current) => ({ ...current, assetTag: asset.assetTag || '', assetName: asset.name || '' }));
    setAssetMessage('');
  };
  const changeCategory = (categoryId) => {
    setSelectedAsset(null);
    setAssetMessage('');
    setForm((current) => ({ ...current, categoryId, assetTag: '', assetName: '' }));
  };
  const changeAssetTag = (assetTag) => {
    const match = categoryAssets.find((asset) => normalize(asset.assetTag) === normalize(assetTag));
    if (match) return selectAsset(match);
    setSelectedAsset(null);
    setForm((current) => ({ ...current, assetTag, assetName: '' }));
    setAssetMessage(assetTag ? 'Choose an asset tag from the selected category.' : '');
  };
  const changeAssetName = (assetName) => {
    const matches = categoryAssets.filter((asset) => normalize(asset.name) === normalize(assetName));
    if (matches.length === 1) return selectAsset(matches[0]);
    setSelectedAsset(null);
    setForm((current) => ({ ...current, assetName, assetTag: '' }));
    setAssetMessage(matches.length > 1 ? 'More than one asset has this name. Choose its asset tag below.' : assetName ? 'Choose an asset name from the selected category.' : '');
  };
  const submit = (event) => {
    event.preventDefault();
    if (!selectedAsset) {
      setAssetMessage('Select a verified asset by asset tag or asset name before adding a warranty.');
      return;
    }
    onSubmit({
      assetId: recordId(selectedAsset, 'id'),
      vendorId: form.vendorId || null,
      policyNumber: form.policyNumber || null,
      startDate: form.startDate,
      endDate: form.endDate,
      coverage: form.coverage || null,
      current: true,
    });
  };
  const enabled = Boolean(form.categoryId);

  return (
    <form className="form-grid" onSubmit={submit}>
      <div className="form-fields">
        <label>
          Category
          <select value={form.categoryId} onChange={(event) => changeCategory(event.target.value)} required disabled={submitting || activeCategories.length === 0}>
            <option value="">{activeCategories.length ? 'Select category' : 'No active categories available'}</option>
            {activeCategories.map((category) => <option key={recordId(category, 'id')} value={recordId(category, 'id')}>{category.name}</option>)}
          </select>
        </label>
        {!enabled && <p className="field-hint">Select a category to search its registered assets.</p>}

        <fieldset className="assignment-fields" disabled={!enabled || submitting}>
          <label>
            Asset tag
            <input value={form.assetTag} onChange={(event) => changeAssetTag(event.target.value)} list="warranty-asset-tags" placeholder="Search or select an asset tag" required />
            <datalist id="warranty-asset-tags">
              {categoryAssets.map((asset) => <option key={recordId(asset, 'id')} value={asset.assetTag}>{[asset.name, assetModelNo(asset)].filter(Boolean).join(' · ')}</option>)}
            </datalist>
          </label>
          <label>
            Asset Name
            <input value={form.assetName} onChange={(event) => changeAssetName(event.target.value)} list="warranty-asset-names" placeholder="Search or select an asset name" />
            <datalist id="warranty-asset-names">
              {categoryAssets.map((asset) => <option key={recordId(asset, 'id')} value={asset.name}>{[asset.assetTag, assetModelNo(asset)].filter(Boolean).join(' · ')}</option>)}
            </datalist>
          </label>
          {sameNameAssets.length > 1 && !selectedAsset && (
            <div className="asset-match-list" role="list" aria-label="Assets matching the selected name">
              {sameNameAssets.map((asset) => <button key={recordId(asset, 'id')} type="button" onClick={() => selectAsset(asset)}><strong>{asset.assetTag}</strong><span>{assetModelNo(asset)}</span></button>)}
            </div>
          )}
          <div className="form-fields two asset-details" aria-live="polite">
            <label>Model No<input value={assetModelNo(selectedAsset)} readOnly /></label>
            <label>Brand<input value={assetBrand(selectedAsset)} readOnly /></label>
          </div>
          <label>
            Warranty provider <span className="optional-label">(optional)</span>
            <select value={form.vendorId} onChange={(event) => update('vendorId', event.target.value)}>
              <option value="">No provider selected</option>
              {vendors.filter((vendor) => String(vendor.status || 'ACTIVE').toUpperCase() === 'ACTIVE').map((vendor) => <option key={recordId(vendor, 'id')} value={recordId(vendor, 'id')}>{vendor.name}</option>)}
            </select>
          </label>
          <label>Policy number <span className="optional-label">(optional)</span><input value={form.policyNumber} onChange={(event) => update('policyNumber', event.target.value)} placeholder="Enter policy number" /></label>
          <div className="form-fields two">
            <label>Warranty start date<input type="date" value={form.startDate} onChange={(event) => update('startDate', event.target.value)} required /></label>
            <label>Warranty end date<input type="date" value={form.endDate} onChange={(event) => update('endDate', event.target.value)} min={form.startDate || undefined} required /></label>
          </div>
          <label>Coverage <span className="optional-label">(optional)</span><textarea value={form.coverage} onChange={(event) => update('coverage', event.target.value)} placeholder="Describe warranty coverage" rows="3" /></label>
        </fieldset>
      </div>
      {assetMessage && <div className="inline-error"><Icon name="warning" size={16} />{assetMessage}</div>}
      {actionError && <div className="inline-error"><Icon name="warning" size={16} />{actionError.message}</div>}
      <div className="modal-actions"><Button variant="outline" onClick={onClose} disabled={submitting}>Cancel</Button><Button type="submit" disabled={submitting || !selectedAsset}>{submitting ? 'Saving...' : 'Add warranty'}</Button></div>
    </form>
  );
}
