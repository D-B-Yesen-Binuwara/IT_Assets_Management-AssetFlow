import { ResourcePage } from '../components/domain/ResourcePage';
import { DetailsPage } from '../components/domain/DetailsPage';
import { resourceConfigs } from '../constants/resourceConfigs';
export { AssignmentsPage } from './AssignmentsPage';

export const AssetsPage = () => <ResourcePage config={resourceConfigs.assets} />;
export const EmployeesPage = () => <ResourcePage config={resourceConfigs.employees} />;
export const MaintenancePage = () => <ResourcePage config={resourceConfigs.maintenance} />;
export const WarrantyPage = () => <ResourcePage config={resourceConfigs.warranty} />;
export const VendorsPage = () => <ResourcePage config={resourceConfigs.vendors} />;
export const LicensesPage = () => <ResourcePage config={resourceConfigs.licenses} />;
export const ProcurementPage = () => <ResourcePage config={resourceConfigs.procurement} />;
export const LocationsPage = () => <ResourcePage config={resourceConfigs.locations} />;
export const AssetDetailPage = () => <DetailsPage config={{ ...resourceConfigs.assets, listPath: '/assets' }} />;
export const EmployeeDetailPage = () => <DetailsPage config={{ ...resourceConfigs.employees, listPath: '/employees' }} />;
