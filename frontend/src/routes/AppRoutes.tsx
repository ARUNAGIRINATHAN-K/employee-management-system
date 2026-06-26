import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// Layouts
import DashboardLayout from '../layouts/DashboardLayout';

// Guard
import ProtectedRoute from './ProtectedRoute';

// Pages
import Login from '../pages/auth/Login';
import Unauthorized from '../pages/Unauthorized';
import Dashboard from '../pages/dashboard/Dashboard';
import EmployeeList from '../pages/employees/EmployeeList';
import EmployeeForm from '../pages/employees/EmployeeForm';
import EmployeeDetails from '../pages/employees/EmployeeDetails';
import DepartmentList from '../pages/departments/DepartmentList';
import DepartmentForm from '../pages/departments/DepartmentForm';
import DepartmentDetails from '../pages/departments/DepartmentDetails';
import Profile from '../pages/profile/Profile';
import UserList from '../pages/users/UserList';
import CreateUserForm from '../pages/users/CreateUserForm';

/**
 * Root Route tree controller. Handles redirection mapping based on session auth & roles.
 */
const AppRoutes = () => {
  const { isAuthenticated, hasRole } = useAuth();

  // Root Redirection Logic
  const getRootRedirect = () => {
    if (!isAuthenticated) {
      return <Navigate to="/login" replace />;
    }
    if (hasRole('ROLE_EMPLOYEE')) {
      return <Navigate to="/profile" replace />;
    }
    return <Navigate to="/dashboard" replace />;
  };

  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={!isAuthenticated ? <Login /> : getRootRedirect()} />
      <Route path="/unauthorized" element={<Unauthorized />} />

      {/* Main Protected Routes Layout Container */}
      <Route
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        {/* Dashboard */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER']}>
              <Dashboard />
            </ProtectedRoute>
          }
        />

        {/* Employee Module */}
        <Route
          path="/employees"
          element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER']}>
              <EmployeeList />
            </ProtectedRoute>
          }
        />
        <Route
          path="/employees/new"
          element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_HR']}>
              <EmployeeForm />
            </ProtectedRoute>
          }
        />
        <Route
          path="/employees/:id"
          element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER']}>
              <EmployeeDetails />
            </ProtectedRoute>
          }
        />
        <Route
          path="/employees/:id/edit"
          element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_HR']}>
              <EmployeeForm />
            </ProtectedRoute>
          }
        />

        {/* Department Module */}
        <Route
          path="/departments"
          element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER']}>
              <DepartmentList />
            </ProtectedRoute>
          }
        />
        <Route
          path="/departments/new"
          element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
              <DepartmentForm />
            </ProtectedRoute>
          }
        />
        <Route
          path="/departments/:id"
          element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER']}>
              <DepartmentDetails />
            </ProtectedRoute>
          }
        />
        <Route
          path="/departments/:id/edit"
          element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
              <DepartmentForm />
            </ProtectedRoute>
          }
        />

        {/* User Management — ADMIN only */}
        <Route
          path="/users"
          element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
              <UserList />
            </ProtectedRoute>
          }
        />
        <Route
          path="/users/new"
          element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
              <CreateUserForm />
            </ProtectedRoute>
          }
        />

        {/* Employee self-profile view */}
        <Route
          path="/profile"
          element={
            <ProtectedRoute allowedRoles={['ROLE_EMPLOYEE', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER']}>
              <Profile />
            </ProtectedRoute>
          }
        />
      </Route>

      {/* Fallback routes */}
      <Route path="/" element={getRootRedirect()} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default AppRoutes;
