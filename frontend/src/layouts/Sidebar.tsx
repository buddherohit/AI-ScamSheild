import React from 'react';
import { NavLink, useLocation } from 'react-router';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Shield, 
  LayoutDashboard, 
  ShieldAlert, 
  Bell, 
  Settings, 
  ChevronLeft,
  ChevronRight,
  LogOut,
  User,
  Activity,
  QrCode,
  Search,
  History,
  MessageSquare,
  TrendingUp
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useAppDispatch, useAppSelector } from '@/store';
import { toggleSidebar } from '@/store/slices/uiSlice';
import { logout } from '@/store/slices/authSlice';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

interface SidebarProps {
  isMobile: boolean;
}

const navItems = [
  { icon: LayoutDashboard, label: 'Dashboard', path: '/dashboard' },
  { icon: QrCode, label: 'QR Scanner', path: '/qr' },
  { icon: Search, label: 'Verify UPI', path: '/upi' },
  { icon: MessageSquare, label: 'SMS Scanner', path: '/sms' },
  { icon: History, label: 'UPI History', path: '/verify/history' },
  { icon: History, label: 'SMS History', path: '/sms/history' },
  { icon: TrendingUp, label: 'Fraud Insights', path: '/sms/insights' },
  { icon: ShieldAlert, label: 'Security Center', path: '/security' },
  { icon: Activity, label: 'Sessions', path: '/sessions' },
  { icon: Bell, label: 'Notifications', path: '/notifications' },
  { icon: Settings, label: 'Settings', path: '/settings' },
];

export const Sidebar: React.FC<SidebarProps> = ({ isMobile }) => {
  const { sidebarOpen } = useAppSelector((state) => state.ui);
  const { user } = useAppSelector((state) => state.auth);
  const dispatch = useAppDispatch();
  const location = useLocation();

  const handleToggle = () => dispatch(toggleSidebar());
  const handleLogout = () => dispatch(logout());

  const sidebarContent = (
    <div className="flex h-full flex-col bg-card border-r shadow-sm">
      {/* Logo Area */}
      <div className="flex h-16 items-center px-4 justify-between border-b">
        <div className="flex items-center gap-3 overflow-hidden">
          <Shield className="h-8 w-8 shrink-0 text-primary" />
          <AnimatePresence>
            {sidebarOpen && (
              <motion.span 
                initial={{ opacity: 0, width: 0 }}
                animate={{ opacity: 1, width: 'auto' }}
                exit={{ opacity: 0, width: 0 }}
                className="font-bold text-lg whitespace-nowrap"
              >
                ScamShield
              </motion.span>
            )}
          </AnimatePresence>
        </div>
        {!isMobile && (
          <Button 
            variant="ghost" 
            size="icon" 
            className="hidden lg:flex" 
            onClick={handleToggle}
          >
            {sidebarOpen ? <ChevronLeft className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
          </Button>
        )}
      </div>

      {/* Navigation Links */}
      <div className="flex-1 overflow-y-auto py-6 px-3 space-y-1">
        {navItems.map((item) => {
          const isActive = location.pathname.startsWith(item.path);
          return (
            <NavLink 
              key={item.path} 
              to={item.path}
              title={!sidebarOpen ? item.label : undefined}
            >
              <div className={cn(
                "flex items-center gap-3 rounded-md px-3 py-2.5 transition-colors group",
                isActive 
                  ? "bg-primary/10 text-primary font-medium" 
                  : "text-muted-foreground hover:bg-muted hover:text-foreground"
              )}>
                <item.icon className={cn("h-5 w-5 shrink-0", isActive ? "text-primary" : "text-muted-foreground group-hover:text-foreground")} />
                <AnimatePresence>
                  {sidebarOpen && (
                    <motion.span
                      initial={{ opacity: 0, width: 0 }}
                      animate={{ opacity: 1, width: 'auto' }}
                      exit={{ opacity: 0, width: 0 }}
                      className="whitespace-nowrap"
                    >
                      {item.label}
                    </motion.span>
                  )}
                </AnimatePresence>
              </div>
            </NavLink>
          );
        })}
      </div>

      {/* User Section */}
      <div className="border-t p-3">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button 
              variant="ghost" 
              className={cn("w-full justify-start h-auto py-2", !sidebarOpen && "px-0 justify-center")}
            >
              <Avatar className="h-8 w-8 shrink-0">
                <AvatarFallback className="bg-primary/10 text-primary">
                  {user?.name?.charAt(0) || 'U'}
                </AvatarFallback>
              </Avatar>
              <AnimatePresence>
                {sidebarOpen && (
                  <motion.div
                    initial={{ opacity: 0, width: 0 }}
                    animate={{ opacity: 1, width: 'auto' }}
                    exit={{ opacity: 0, width: 0 }}
                    className="flex flex-col items-start ml-3 overflow-hidden text-sm"
                  >
                    <span className="font-medium truncate max-w-[150px]">{user?.name || 'User'}</span>
                    <span className="text-xs text-muted-foreground truncate max-w-[150px]">{user?.email || 'user@example.com'}</span>
                  </motion.div>
                )}
              </AnimatePresence>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" side="right" className="w-56">
            <DropdownMenuLabel>My Account</DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={() => {}}>
              <User className="mr-2 h-4 w-4" />
              <span>Profile</span>
            </DropdownMenuItem>
            <DropdownMenuItem onClick={handleLogout} className="text-destructive focus:text-destructive">
              <LogOut className="mr-2 h-4 w-4" />
              <span>Log out</span>
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  );

  if (isMobile) {
    return (
      <>
        {/* Mobile Backdrop */}
        <AnimatePresence>
          {sidebarOpen && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={handleToggle}
              className="fixed inset-0 z-40 bg-background/80 backdrop-blur-sm lg:hidden"
            />
          )}
        </AnimatePresence>

        {/* Mobile Drawer */}
        <motion.aside
          initial={{ x: '-100%' }}
          animate={{ x: sidebarOpen ? 0 : '-100%' }}
          transition={{ type: 'spring', bounce: 0, duration: 0.3 }}
          className="fixed inset-y-0 left-0 z-50 w-[280px] lg:hidden"
        >
          {sidebarContent}
        </motion.aside>
      </>
    );
  }

  return (
    <motion.aside
      initial={false}
      animate={{ width: sidebarOpen ? 280 : 72 }}
      transition={{ type: 'spring', bounce: 0, duration: 0.3 }}
      className="fixed inset-y-0 left-0 z-20 hidden lg:block"
    >
      {sidebarContent}
    </motion.aside>
  );
};
