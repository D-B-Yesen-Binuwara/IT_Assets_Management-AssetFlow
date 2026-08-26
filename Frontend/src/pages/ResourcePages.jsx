import { ResourcePage } from '../components/domain/ResourcePage';
import { DetailsPage } from '../components/domain/DetailsPage';
import { resourceConfigs } from '../constants/resourceConfigs';
import { AssetsPage } from './AssetsPage';
import { EmployeesPage } from './EmployeesPage';
import { WarrantyPage } from './WarrantyPage';
export { AssignmentsPage } from './AssignmentsPage';

export { AssetsPage };
export { EmployeesPage };
export const MaintenancePage = () => <ResourcePage config={resourceConfigs.maintenance} />;
export { WarrantyPage };
export const VendorsPage = () => <ResourcePage config={resourceConfigs.vendors} />;
export const LicensesPage = () => <ResourcePage config={resourceConfigs.licenses} />;
export const ProcurementPage = () => <ResourcePage config={resourceConfigs.procurement} />;
export const LocationsPage = () => <ResourcePage config={resourceConfigs.locations} />;
export const AssetDetailPage = () => <DetailsPage config={{ ...resourceConfigs.assets, listPath: '/assets' }} />;
export const EmployeeDetailPage = () => <DetailsPage config={{ ...resourceConfigs.employees, listPath: '/employees' }} />;
