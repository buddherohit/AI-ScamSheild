import React, { useEffect, useState } from 'react';
import { Outlet } from 'react-router';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { useAppSelector, useAppDispatch } from '@/store';
import { setSidebarOpen } from '@/store/slices/uiSlice';
import { CommandPalette } from '@/components/CommandPalette';
import { cn } from '@/lib/utils';
import { AnimatePresence } from 'framer-motion';

export const MainLayout: React.FC = () => {
  const { sidebarOpen, themeMode } = useAppSelector((state) => state.ui);
  const dispatch = useAppDispatch();
  const [isMobile, setIsMobile] = useState(false);

  // Handle responsive sidebar behavior
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth < 1024) {
        setIsMobile(true);
        dispatch(setSidebarOpen(false));
      } else {
        setIsMobile(false);
        dispatch(setSidebarOpen(true));
      }
    };
    
    // Initial check
    handleResize();
    
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [dispatch]);

  // Apply theme to document root
  useEffect(() => {
    const root = window.document.documentElement;
    root.classList.remove('light', 'dark');
    root.classList.add(themeMode);
  }, [themeMode]);

  return (
    <div className="flex h-screen overflow-hidden bg-background">
      <CommandPalette />
      
      {/* Sidebar - Desktop and Mobile Drawer handled within the component */}
      <Sidebar isMobile={isMobile} />

      {/* Main Content Area */}
      <div 
        className={cn(
          "flex flex-col flex-1 w-full transition-all duration-300 ease-in-out",
          sidebarOpen && !isMobile ? "lg:ml-[280px]" : "lg:ml-[72px]"
        )}
      >
        <Header isMobile={isMobile} />
        
        <main className="flex-1 overflow-y-auto overflow-x-hidden p-4 md:p-6 lg:p-8 bg-muted/20">
          <AnimatePresence mode="wait">
            <Outlet />
          </AnimatePresence>
        </main>
      </div>
    </div>
  );
};
