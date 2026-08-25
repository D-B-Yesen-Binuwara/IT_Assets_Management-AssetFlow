import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth/useAuth';
import { AppLayout } from './components/layout/AppLayout';
import { LoadingState } from './components/common/State';
import { DashboardPage } from './pages/DashboardPage';
import { AuthPage } from './pages/AuthPage';
import {
  AssetDetailPage,
  AssetsPage,
  AssignmentsPage,
  EmployeeDetailPage,
  EmployeesPage,
  LicensesPage,
  LocationsPage,
  MaintenancePage,
  ProcurementPage,
  VendorsPage,
  WarrantyPage,
} from './pages/ResourcePages';
import { ReportsPage } from './pages/ReportsPage';
import { NotificationsPage } from './pages/NotificationsPage';
import { SettingsPage } from './pages/SettingsPage';

function AuthenticatedRoutes() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/assets" element={<AssetsPage />} />
        <Route path="/assets/:id" element={<AssetDetailPage />} />
        <Route path="/employees" element={<EmployeesPage />} />
        <Route path="/employees/:id" element={<EmployeeDetailPage />} />
        <Route path="/assignments" element={<AssignmentsPage />} />
        <Route path="/maintenance" element={<MaintenancePage />} />
        <Route path="/warranty" element={<WarrantyPage />} />
        <Route path="/vendors" element={<VendorsPage />} />
        <Route path="/licenses" element={<LicensesPage />} />
        <Route path="/procurement" element={<ProcurementPage />} />
        <Route path="/locations" element={<LocationsPage />} />
        <Route path="/reports" element={<ReportsPage />} />
        <Route path="/notifications" element={<NotificationsPage />} />
        <Route path="/settings" element={<SettingsPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}

export default function App() {
  const auth = useAuth();

  if (auth.status === 'loading') return <LoadingState label="Connecting to AssetFlow" />;
  if (!auth.isAuthenticated) return <AuthPage />;

  return <AuthenticatedRoutes />;
}
