import { useMemo, useState } from 'react';
import { Button } from '../common/Button';
import { Icon } from '../common/Icon';
import { employeeLabel, recordId } from '../../utils/collections';

const today = () => new Date().toISOString().slice(0, 10);
const normalize = (value) => String(value || '').trim().toLocaleLowerCase();
const assetModelNo = (asset) => asset?.modelNo || asset?.model || '';
const assetBrand = (asset) => asset?.brand || asset?.manufacturer || '';
const assetIsAvailable = (asset) => String(asset?.status || '').toUpperCase() === 'AVAILABLE';

const initialForm = {
  categoryId: '',
  assetTag: '',
  modelNo: '',
  employeeId: '',
  assignedDate: today(),
  expectedReturnDate: '',
};

export function AssignmentForm({
  categories = [],
  assets = [],
  employees = [],
  submitting = false,
  actionError,
  onClose,
  onSubmit,
}) {
  const [form, setForm] = useState(initialForm);
  const [selectedAsset, setSelectedAsset] = useState(null);
  const [assetMessage, setAssetMessage] = useState('');

  const activeCategories = useMemo(
    () => categories.filter((category) => category.active !== false),
    [categories],
  );
  const categoryAssets = useMemo(
    () => assets.filter(
      (asset) => recordId(asset, 'categoryId') === form.categoryId && assetIsAvailable(asset),
    ),
    [assets, form.categoryId],
  );
  const sameModelAssets = useMemo(
    () => categoryAssets.filter((asset) => normalize(assetModelNo(asset)) === normalize(form.modelNo)),
    [categoryAssets, form.modelNo],
  );
  const selectedEmployee = employees.find((employee) => recordId(employee, 'id') === form.employeeId);

  const selectAsset = (asset) => {
    setSelectedAsset(asset);
    setForm((current) => ({
      ...current,
      assetTag: asset.assetTag || '',
      modelNo: assetModelNo(asset),
    }));
    setAssetMessage('');
  };

  const changeCategory = (categoryId) => {
    setSelectedAsset(null);
    setAssetMessage('');
    setForm((current) => ({
      ...current,
      categoryId,
      assetTag: '',
      modelNo: '',
    }));
  };

  const changeAssetTag = (assetTag) => {
    const matchingAsset = categoryAssets.find(
      (asset) => normalize(asset.assetTag) === normalize(assetTag),
    );

    if (matchingAsset) {
      selectAsset(matchingAsset);
      return;
    }

    setSelectedAsset(null);
    setForm((current) => ({ ...current, assetTag, modelNo: '' }));
    setAssetMessage(assetTag ? 'Choose an available asset tag from this category.' : '');
  };

  const changeModelNo = (modelNo) => {
    const matchingAssets = categoryAssets.filter(
      (asset) => normalize(assetModelNo(asset)) === normalize(modelNo),
    );

    if (matchingAssets.length === 1) {
      selectAsset(matchingAssets[0]);
      return;
    }

    setSelectedAsset(null);
    setForm((current) => ({ ...current, assetTag: '', modelNo }));
    setAssetMessage(
      matchingAssets.length > 1
        ? 'More than one available asset has this model number. Choose its asset tag below.'
        : modelNo
          ? 'Choose an available model number from this category.'
          : '',
    );
  };

  const updateField = (key, value) => setForm((current) => ({ ...current, [key]: value }));

  const submit = (event) => {
    event.preventDefault();

    if (!selectedAsset) {
      setAssetMessage('Select an available asset by its asset tag or model number before submitting.');
      return;
    }

    onSubmit({
      assetId: recordId(selectedAsset, 'id'),
      categoryId: form.categoryId,
      employeeId: form.employeeId,
      assignedDate: form.assignedDate || null,
      expectedReturnDate: form.expectedReturnDate || null,
    });
  };

  const inputsEnabled = Boolean(form.categoryId);
  const noAssetsInCategory = inputsEnabled && categoryAssets.length === 0;

  return (
    <form className="form-grid" onSubmit={submit}>
      <div className="form-fields">
        <label>
          Category
          <select
            value={form.categoryId}
            onChange={(event) => changeCategory(event.target.value)}
            required
            disabled={submitting || activeCategories.length === 0}
          >
            <option value="">{activeCategories.length === 0 ? 'No active categories available' : 'Select category'}</option>
            {activeCategories.map((category) => {
              const id = recordId(category, 'id');
              return <option key={id} value={id}>{category.name}</option>;
            })}
          </select>
        </label>

        {!inputsEnabled && (
          <p className="field-hint">Select a category to enable the assignment fields.</p>
        )}

        <fieldset className="assignment-fields" disabled={!inputsEnabled || submitting}>
          <label>
            Asset tag
            <input
              value={form.assetTag}
              onChange={(event) => changeAssetTag(event.target.value)}
              list="assignment-asset-tags"
              placeholder="Search or select an asset tag"
              required
            />
            <datalist id="assignment-asset-tags">
              {categoryAssets.map((asset) => (
                <option key={recordId(asset, 'id')} value={asset.assetTag}>
                  {[asset.name, assetModelNo(asset)].filter(Boolean).join(' · ')}
                </option>
              ))}
            </datalist>
          </label>

          <label>
            Model No
            <input
              value={form.modelNo}
              onChange={(event) => changeModelNo(event.target.value)}
              list="assignment-model-numbers"
              placeholder="Search or select a model number"
            />
            <datalist id="assignment-model-numbers">
              {categoryAssets.filter((asset) => assetModelNo(asset)).map((asset) => (
                <option key={recordId(asset, 'id')} value={assetModelNo(asset)}>
                  {[asset.assetTag, asset.name].filter(Boolean).join(' · ')}
                </option>
              ))}
            </datalist>
          </label>

          {sameModelAssets.length > 1 && !selectedAsset && (
            <div className="asset-match-list" role="list" aria-label="Assets matching the selected model number">
              {sameModelAssets.map((asset) => (
                <button key={recordId(asset, 'id')} type="button" onClick={() => selectAsset(asset)}>
                  <strong>{asset.assetTag}</strong>
                  <span>{asset.name}</span>
                </button>
              ))}
            </div>
          )}

          {noAssetsInCategory && (
            <p className="field-hint">No available assets are recorded for this category.</p>
          )}

          <div className="form-fields two asset-details" aria-live="polite">
            <label>
              Assets Name
              <input value={selectedAsset?.name || ''} readOnly />
            </label>
            <label>
              Brand
              <input value={assetBrand(selectedAsset)} readOnly />
            </label>
          </div>

          <label>
            Employee
            <select
              value={form.employeeId}
              onChange={(event) => updateField('employeeId', event.target.value)}
              required
              disabled={noAssetsInCategory}
            >
              <option value="">Select employee</option>
              {employees.map((employee) => {
                const id = recordId(employee, 'id');
                return <option key={id} value={id}>{employeeLabel(employee)}</option>;
              })}
            </select>
          </label>

          <div className="form-fields two employee-details" aria-live="polite">
            <label>Department<input value={selectedEmployee?.department || ''} readOnly /></label>
            <label>Branch<input value={selectedEmployee?.branch || ''} readOnly /></label>
            <label>Email<input value={selectedEmployee?.email || ''} readOnly /></label>
            <label>Contact No<input value={selectedEmployee?.phone || ''} readOnly /></label>
          </div>

          <div className="form-fields two">
            <label>
              Assignment date
              <input
                type="date"
                value={form.assignedDate}
                onChange={(event) => updateField('assignedDate', event.target.value)}
                required
              />
            </label>
            <label>
              <span className="field-label">Closing date <span className="optional-label">(optional)</span></span>
              <input
                type="date"
                value={form.expectedReturnDate}
                onChange={(event) => updateField('expectedReturnDate', event.target.value)}
                min={form.assignedDate || undefined}
              />
            </label>
          </div>
        </fieldset>
      </div>

      {assetMessage && (
        <div className="inline-error">
          <Icon name="warning" size={16} />
          {assetMessage}
        </div>
      )}

      {actionError && (
        <div className="inline-error">
          <Icon name="warning" size={16} />
          {actionError.message}
        </div>
      )}

      <div className="modal-actions">
        <Button variant="outline" onClick={onClose} disabled={submitting}>Cancel</Button>
        <Button type="submit" disabled={submitting || !selectedAsset || noAssetsInCategory}>
          {submitting ? 'Submitting...' : 'Create assignment'}
        </Button>
      </div>
    </form>
  );
}
