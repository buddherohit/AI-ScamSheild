import React from 'react';
import { Outlet } from 'react-router';
import { Shield } from 'lucide-react';
import { useAppSelector } from '@/store';
import { useEffect } from 'react';

export const AuthLayout: React.FC = () => {
  const { themeMode } = useAppSelector((state) => state.ui);

  useEffect(() => {
    const root = window.document.documentElement;
    root.classList.remove('light', 'dark');
    root.classList.add(themeMode);
  }, [themeMode]);

  return (
    <div className="flex min-h-screen bg-background">
      {/* Left side: branding/marketing (hidden on mobile) */}
      <div className="hidden lg:flex flex-col flex-1 bg-primary text-primary-foreground p-12 justify-between">
        <div className="flex items-center gap-3">
          <Shield className="w-10 h-10" />
          <span className="text-2xl font-bold tracking-tight">AI ScamShield</span>
        </div>
        
        <div className="space-y-6 max-w-lg">
          <h1 className="text-5xl font-extrabold tracking-tight">
            Detect digital fraud before money leaves your account.
          </h1>
          <p className="text-lg opacity-90 leading-relaxed">
            Enterprise-grade protection analyzing millions of patterns to secure your digital footprint against the most advanced scams.
          </p>
        </div>
        
        <div className="flex items-center gap-4 text-sm opacity-70">
          <span>© 2026 AI ScamShield. All rights reserved.</span>
        </div>
      </div>
      
      {/* Right side: auth forms */}
      <div className="flex flex-1 flex-col justify-center items-center p-8 sm:p-12">
        <div className="w-full max-w-md space-y-8">
          <div className="flex lg:hidden items-center justify-center gap-3 mb-8 text-primary">
            <Shield className="w-12 h-12" />
            <span className="text-3xl font-bold tracking-tight">AI ScamShield</span>
          </div>
          
          <Outlet />
        </div>
      </div>
    </div>
  );
};
