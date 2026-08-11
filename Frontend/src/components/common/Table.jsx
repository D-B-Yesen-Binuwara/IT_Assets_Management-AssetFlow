import { useMemo, useState } from 'react';
import { Icon } from './Icon';
import { EmptyState, LoadingState, ErrorState } from './State';

export function Table({
  columns,
  rows = [],
  status = 'success',
  error,
  onRetry,
  emptyTitle = 'No records found',
  emptyDescription = 'Records will appear here once the backend returns them.',
  searchPlaceholder = 'Search records...',
}) {
  const [query, setQuery] = useState('');
  const [sort, setSort] = useState({ key: null, direction: 'asc' });

  const filtered = useMemo(() => {
    const source = Array.isArray(rows) ? rows : [];
    const normalizedQuery = query.toLowerCase();

    // The table only searches and sorts rows that were returned by the backend.
    const searched = normalizedQuery
      ? source.filter((row) =>
          columns.some((column) =>
            String(column.value(row) ?? '').toLowerCase().includes(normalizedQuery),
          ),
        )
      : source;

    if (!sort.key) return searched;

    const column = columns.find((item) => item.key === sort.key);

    return [...searched].sort(
      (a, b) =>
        String(column.value(a) ?? '').localeCompare(String(column.value(b) ?? ''), undefined, {
          numeric: true,
        }) * (sort.direction === 'asc' ? 1 : -1),
    );
  }, [rows, query, sort, columns]);

  const toggleSort = (key) =>
    setSort((current) => ({
      key,
      direction: current.key === key && current.direction === 'asc' ? 'desc' : 'asc',
    }));

  if (status === 'loading') return <LoadingState label="Loading records" />;
  if (status === 'error') return <ErrorState error={error} onRetry={onRetry} />;

  return (
    <div className="table-card">
      <div className="table-toolbar">
        <div className="table-search">
          <Icon name="search" size={16} />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder={searchPlaceholder}
          />
        </div>
        <span className="table-count">
          {query ? `${filtered.length} matching` : 'Ready for backend data'}
        </span>
      </div>

      {filtered.length === 0 ? (
        <EmptyState
          icon="assets"
          title={query ? 'No matching records' : emptyTitle}
          description={query ? 'Try a different search term.' : emptyDescription}
        />
      ) : (
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                {columns.map((column) => (
                  <th key={column.key}>
                    <button onClick={() => toggleSort(column.key)}>
                      {column.label}
                      {column.sortable !== false && <Icon name="chevronDown" size={13} />}
                    </button>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {filtered.map((row, index) => (
                <tr key={row.id ?? index}>
                  {columns.map((column) => (
                    <td key={column.key}>
                      {column.render ? column.render(row) : column.value(row)}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
