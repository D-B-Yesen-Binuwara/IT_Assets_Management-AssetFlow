import { useState } from 'react';
import { Button } from '../components/common/Button';
import { Card, CardHeader } from '../components/common/Card';
import { Icon } from '../components/common/Icon';
import { PageHeader } from '../components/common/PageHeader';
import { ErrorState, LoadingState } from '../components/common/State';
import { Table } from '../components/common/Table';
import { useAction, useResource } from '../hooks/useResource';
import { categoryService, locationService, notificationService, settingsService } from '../services/resources';
import { collectionItems } from '../utils/collections';

const tabs = [
  { id: 'organization', label: 'Organization', icon: 'building' },
  { id: 'appearance', label: 'Branding & theme', icon: 'settings' },
  { id: 'categories', label: 'Categories', icon: 'assets' },
  { id: 'locations', label: 'Locations', icon: 'pin' },
  { id: 'notifications', label: 'Notifications', icon: 'bell' },
  { id: 'email', label: 'Email templates', icon: 'mail' },
];

const emptyForm = {
  organizationName: '',
  industry: '',
  primaryContact: '',
  currency: '',
  timezone: 'UTC',
  branding: '{}',
  notificationSettings: '{}',
};

export function SettingsPage() {
  const [active, setActive] = useState('organization');
  const [theme, setTheme] = useState('light');
  const [formEdits, setFormEdits] = useState({});
  const resource = useResource(() => settingsService.get(), 'settings');
  const categories = useResource(() => categoryService.list(), 'categories');
  const locations = useResource(() => locationService.list(), 'locations');
  const preferences = useResource(() => notificationService.preferences(), 'preferences');
  const templates = useResource(() => settingsService.templates(), 'templates');
  const action = useAction(settingsService.update);

  // The form combines backend settings with unsaved browser-session edits.
  const form = {
    ...emptyForm,
    ...(resource.data && typeof resource.data === 'object' ? resource.data : {}),
    ...formEdits,
  };

  const updateField = (key, value) => {
    setFormEdits((current) => ({ ...current, [key]: value }));
  };

  const save = async (event) => {
    event.preventDefault();

    try {
      await action.execute({
        organizationName: form.organizationName,
        industry: form.industry,
        primaryContact: form.primaryContact,
        currency: form.currency,
        timezone: form.timezone,
        branding: form.branding,
        notificationSettings: form.notificationSettings,
      });
      setFormEdits({});
      resource.reload();
    } catch {
      // The action state renders the backend error below the form.
    }
  };

  return (
    <div>
      <PageHeader
        title="Settings"
        description="Configure organization, branding, operational preferences, and notifications."
        actions={
          <Button icon="settings" onClick={save}>
            Save changes
          </Button>
        }
      />

      {resource.status === 'loading' && <LoadingState label="Loading settings" />}
      {resource.status === 'error' && (
        <div className="settings-notice">
          <ErrorState error={resource.error} onRetry={resource.reload} />
        </div>
      )}

      <div className="settings-layout">
        <nav className="settings-tabs">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              className={active === tab.id ? 'active' : ''}
              onClick={() => setActive(tab.id)}
            >
              <Icon name={tab.icon} size={17} />
              {tab.label}
            </button>
          ))}
        </nav>

        <section className="settings-content">
          {active === 'organization' && (
            <form className="settings-form" onSubmit={save}>
              <Card>
                <CardHeader
                  title="Organization details"
                  description="Organization and workspace configuration."
                />

                <div className="form-fields two">
                  <label>
                    Organization name
                    <input
                      value={form.organizationName}
                      onChange={(event) => updateField('organizationName', event.target.value)}
                      placeholder="Organization name"
                    />
                  </label>

                  <label>
                    Industry
                    <input
                      value={form.industry}
                      onChange={(event) => updateField('industry', event.target.value)}
                      placeholder="Industry"
                    />
                  </label>

                  <label>
                    Primary contact
                    <input
                      value={form.primaryContact}
                      onChange={(event) => updateField('primaryContact', event.target.value)}
                      placeholder="Contact name"
                    />
                  </label>

                  <label>
                    Currency
                    <input
                      value={form.currency}
                      onChange={(event) => updateField('currency', event.target.value)}
                      placeholder="Currency code"
                    />
                  </label>
                </div>
              </Card>

              {action.status === 'error' && (
                <div className="inline-error">
                  <Icon name="warning" size={16} />
                  {action.error?.message}
                </div>
              )}
            </form>
          )}

          {active === 'appearance' && (
            <Card>
              <CardHeader title="Appearance" description="Choose the presentation for this browser session." />
              <div className="theme-options">
                {['light', 'dark'].map((option) => (
                  <button
                    key={option}
                    className={theme === option ? 'selected' : ''}
                    onClick={() => setTheme(option)}
                  >
                    <Icon name={option === 'light' ? 'sun' : 'moon'} size={20} />
                    <span>{option} mode</span>
                    {theme === option && <Icon name="shield" size={14} />}
                  </button>
                ))}
              </div>
            </Card>
          )}

          {active === 'categories' && (
            <Card>
              <CardHeader title="Asset categories" description="Manage the categories used across assets." />
              <Table
                columns={[
                  { key: 'name', label: 'Category', value: (row) => row.name },
                  { key: 'active', label: 'Active', value: (row) => row.active ? 'Yes' : 'No' },
                  { key: 'assetCount', label: 'Assets', value: (row) => row.assetCount ?? 0 },
                ]}
                rows={collectionItems(categories.data)}
                status={categories.status}
                error={categories.error}
                onRetry={categories.reload}
                emptyTitle="No categories loaded"
              />
            </Card>
          )}

          {active === 'locations' && (
            <Card>
              <CardHeader
                title="Inventory locations"
                description="Manage the locations used across your inventory."
              />
              <Table
                columns={[
                  { key: 'name', label: 'Location', value: (row) => row.name },
                  { key: 'type', label: 'Type', value: (row) => row.type },
                  { key: 'assetCount', label: 'Assets', value: (row) => row.assetCount ?? 0 },
                ]}
                rows={collectionItems(locations.data)}
                status={locations.status}
                error={locations.error}
                onRetry={locations.reload}
                emptyTitle="No locations loaded"
              />
            </Card>
          )}

          {active === 'notifications' && (
            <Card>
              <CardHeader
                title="Notification preferences"
                description="Choose which events should generate notifications."
              />
              <Table
                columns={[
                  { key: 'notificationType', label: 'Notification', value: (row) => row.notificationType },
                  { key: 'inAppEnabled', label: 'In-app', value: (row) => row.inAppEnabled ? 'Enabled' : 'Disabled' },
                  { key: 'emailEnabled', label: 'Email', value: (row) => row.emailEnabled ? 'Enabled' : 'Disabled' },
                ]}
                rows={collectionItems(preferences.data)}
                status={preferences.status}
                error={preferences.error}
                onRetry={preferences.reload}
                emptyTitle="No preferences configured"
              />
            </Card>
          )}

          {active === 'email' && (
            <Card>
              <CardHeader
                title="Email templates"
                description="Manage notification templates and their content."
              />
              <Table
                columns={[
                  { key: 'templateKey', label: 'Template', value: (row) => row.templateKey },
                  { key: 'subjectTemplate', label: 'Subject', value: (row) => row.subjectTemplate },
                  { key: 'active', label: 'Active', value: (row) => row.active ? 'Yes' : 'No' },
                ]}
                rows={collectionItems(templates.data)}
                status={templates.status}
                error={templates.error}
                onRetry={templates.reload}
                emptyTitle="No templates loaded"
              />
            </Card>
          )}
        </section>
      </div>
    </div>
  );
}
