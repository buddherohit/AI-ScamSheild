import React from 'react';
import { Routes, Route, Navigate } from 'react-router';
import { useAppSelector } from '@/store';

import { AuthLayout } from '@/layouts/AuthLayout';
import { MainLayout } from '@/layouts/MainLayout';

import { Login } from '@/pages/Login';
import { Dashboard } from '@/pages/Dashboard';
import { Profile } from '@/pages/Profile';
import { SessionManagement } from '@/pages/SessionManagement';
import { SecurityCenter } from '@/pages/SecurityCenter';
import { Settings } from '@/pages/Settings';
import { NotFound } from '@/pages/NotFound';

interface GuardProps {
  children: React.ReactElement;
}

export const ProtectedRoute: React.FC<GuardProps> = ({ children }) => {
  const { isAuthenticated } = useAppSelector((state) => state.auth);
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return children;
};

export const PublicRoute: React.FC<GuardProps> = ({ children }) => {
  const { isAuthenticated } = useAppSelector((state) => state.auth);
  if (isAuthenticated) return <Navigate to="/dashboard" replace />;
  return children;
};

export const AppRoutes: React.FC = () => {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />

      {/* Public Routes */}
      <Route path="/" element={<PublicRoute><AuthLayout /></PublicRoute>}>
        <Route path="login" element={<Login />} />
      </Route>

      {/* Protected Routes */}
      <Route path="/" element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="profile" element={<Profile />} />
        <Route path="sessions" element={<SessionManagement />} />
        <Route path="security" element={<SecurityCenter />} />
        <Route path="settings" element={<Settings />} />
        
        {/* Placeholders */}
        <Route path="notifications" element={<div className="p-6">Notifications (WIP)</div>} />
      </Route>

      <Route path="/404" element={<NotFound />} />
      <Route path="*" element={<Navigate to="/404" replace />} />
    </Routes>
  );
};

export default AppRoutes;
