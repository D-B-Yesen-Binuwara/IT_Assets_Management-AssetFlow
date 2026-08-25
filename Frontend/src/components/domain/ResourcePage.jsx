import { useState } from 'react';
import { Button } from '../common/Button';
import { Card } from '../common/Card';
import { Icon } from '../common/Icon';
import { BackendForm, Modal } from '../common/Modal';
import { PageHeader } from '../common/PageHeader';
import { StatCard } from '../common/StatCard';
import { Table } from '../common/Table';
import { useAction, useResource } from '../../hooks/useResource';
import { collectionItems } from '../../utils/collections';

export function ResourcePage({ config }) {
  const resource = useResource(() => config.service.list(), 'list');
  const [modalOpen, setModalOpen] = useState(false);
  const createAction = useAction(config.service.create);
  const rows = collectionItems(resource.data);
  const stats = config.stats.map((stat) => ({
    ...stat,
    value: stat.getValue ? stat.getValue(rows) : '\u2014',
  }));

  const submit = async (payload) => {
    try {
      await createAction.execute(config.toRequest ? config.toRequest(payload) : payload);
      setModalOpen(false);
      resource.reload();
    } catch {
      // The form state surfaces backend errors inside the modal.
    }
  };

  return (
    <div>
      <PageHeader
        title={config.title}
        description={config.description}
        breadcrumbs={[{ label: config.title }]}
        actions={
          <>
            <Button variant="outline" icon="download">
              Export
            </Button>
            <Button icon="plus" onClick={() => setModalOpen(true)}>
              {config.actionLabel || `Add ${config.singular}`}
            </Button>
          </>
        }
      />

      <div className="stats-grid">
        {stats.map((stat) => (
          <StatCard key={stat.label} {...stat} />
        ))}
      </div>

      {config.chart && (
        <div className="content-grid one">
          <Card>
            <div className="section-intro">
              <div>
                <h2>{config.chart.title}</h2>
                <p>{config.chart.description}</p>
              </div>
              <span className="data-source">
                <Icon name="info" size={14} /> API data
              </span>
            </div>

            <div className="chart-empty mini">
              <p>No backend data available</p>
              <small>Trend data will render here when connected.</small>
            </div>
          </Card>
        </div>
      )}

      <Card className="table-wrap">
        <Table
          columns={config.columns}
          rows={rows}
          status={resource.status}
          error={resource.error}
          onRetry={resource.reload}
          emptyTitle={`No ${config.title.toLowerCase()} yet`}
          emptyDescription="This workspace is ready for live API data. No records are being simulated."
          searchPlaceholder={`Search ${config.title.toLowerCase()}...`}
        />
      </Card>

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={config.actionLabel || `Add ${config.singular}`}
        description="This request will be validated and sent to the backend API."
      >
        <BackendForm
          fields={config.fields}
          onClose={() => setModalOpen(false)}
          onSubmit={submit}
          submitting={createAction.status === 'loading'}
        />

        {createAction.status === 'error' && (
          <div className="inline-error">
            <Icon name="warning" size={16} />
            {createAction.error?.message}
          </div>
        )}
      </Modal>
    </div>
  );
}
