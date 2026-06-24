import React from 'react';
import { Routes, Route, Navigate } from 'react-router';
import { useAppSelector } from '@/store';

// Layout Wrappers
import AuthLayout from '@/layouts/AuthLayout';
import MainLayout from '@/layouts/MainLayout';

// Page Views
import Login from '@/pages/Login';
import Dashboard from '@/pages/Dashboard';
import NotFound from '@/pages/NotFound';

// ==============================================================================
// Route Guards
// ==============================================================================
interface GuardProps {
  children: React.ReactElement;
}

export const ProtectedRoute: React.FC<GuardProps> = ({ children }) => {
  const { isAuthenticated } = useAppSelector((state) => state.auth);

  if (!isAuthenticated) {
    // Redirect to login if user not authenticated
    return <Navigate to="/login" replace />;
  }

  return children;
};

export const PublicRoute: React.FC<GuardProps> = ({ children }) => {
  const { isAuthenticated } = useAppSelector((state) => state.auth);

  if (isAuthenticated) {
    // Redirect to dashboard if user is already authenticated
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

// ==============================================================================
// Routes Orchestrator
// ==============================================================================
export const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {/* Root Route Redirect */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />

      {/* Public Guest Routes */}
      <Route
        path="/login"
        element={
          <PublicRoute>
            <AuthLayout>
              <Login />
            </AuthLayout>
          </PublicRoute>
        }
      />

      {/* Protected Enterprise Routes */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <MainLayout>
              <Dashboard />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Feature Modules Placeholder Routes */}
      <Route
        path="/fraud"
        element={
          <ProtectedRoute>
            <MainLayout>
              <div style={{ padding: '24px' }}>
                <h2>Scam & Fraud Analysis Control Panel</h2>
                <p>Telemetry, scans, and heuristics configuration endpoints [Phase 2 Module].</p>
              </div>
            </MainLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/reports"
        element={
          <ProtectedRoute>
            <MainLayout>
              <div style={{ padding: '24px' }}>
                <h2>Analytics & Compliance Reports</h2>
                <p>Audits, exports, and verification logs templates [Phase 2 Module].</p>
              </div>
            </MainLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/notifications"
        element={
          <ProtectedRoute>
            <MainLayout>
              <div style={{ padding: '24px' }}>
                <h2>Enterprise Alerts Dispatcher</h2>
                <p>Webhook channels, email, and SMS alerts dispatch matrices [Phase 2 Module].</p>
              </div>
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Catch-all 404 Fallback Route */}
      <Route path="/404" element={<NotFound />} />
      <Route path="*" element={<Navigate to="/404" replace />} />
    </Routes>
  );
};

export default AppRoutes;
