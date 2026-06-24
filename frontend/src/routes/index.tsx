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
import { QrScanner } from '@/pages/QrScanner';
import { UpiVerify } from '@/pages/UpiVerify';
import { VerifyResult } from '@/pages/VerifyResult';
import { VerifyHistory } from '@/pages/VerifyHistory';
import { SmsAnalysis } from '@/pages/SmsAnalysis';
import { SmsHistory } from '@/pages/SmsHistory';
import { SmsDetails } from '@/pages/SmsDetails';
import { FraudInsights } from '@/pages/FraudInsights';

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
        
        {/* Phase 5 Routes */}
        <Route path="qr" element={<QrScanner />} />
        <Route path="upi" element={<UpiVerify />} />
        <Route path="verify/result" element={<VerifyResult />} />
        <Route path="verify/history" element={<VerifyHistory />} />
        
        {/* Phase 6 Routes */}
        <Route path="sms" element={<SmsAnalysis />} />
        <Route path="sms/history" element={<SmsHistory />} />
        <Route path="sms/analysis/:id" element={<SmsDetails />} />
        <Route path="sms/insights" element={<FraudInsights />} />
        
        {/* Placeholders */}
        <Route path="notifications" element={<div className="p-6">Notifications (WIP)</div>} />
      </Route>

      <Route path="/404" element={<NotFound />} />
      <Route path="*" element={<Navigate to="/404" replace />} />
    </Routes>
  );
};

export default AppRoutes;
