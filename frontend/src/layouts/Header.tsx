import React from 'react';
import { Menu, Search, Moon, Sun, Bell } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { useAppDispatch, useAppSelector } from '@/store';
import { toggleSidebar, toggleThemeMode } from '@/store/slices/uiSlice';
import { toggleNotificationDrawer } from '@/store/slices/notificationSlice';

interface HeaderProps {
  isMobile: boolean;
}

export const Header: React.FC<HeaderProps> = ({ isMobile }) => {
  const dispatch = useAppDispatch();
  const { themeMode } = useAppSelector((state) => state.ui);
  const { unreadCount } = useAppSelector((state) => state.notifications);

  const handleToggleTheme = () => {
    dispatch(toggleThemeMode());
  };

  return (
    <header className="sticky top-0 z-10 flex h-16 items-center justify-between border-b bg-background/95 px-4 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="flex items-center gap-4">
        {isMobile && (
          <Button variant="ghost" size="icon" onClick={() => dispatch(toggleSidebar())}>
            <Menu className="h-5 w-5" />
          </Button>
        )}
        <div className="hidden md:flex items-center text-sm text-muted-foreground">
          <span className="font-medium text-foreground">ScamShield</span>
          <span className="mx-2">/</span>
          <span>Dashboard</span>
        </div>
      </div>

        <button 
          onClick={() => window.dispatchEvent(new CustomEvent('open-command-palette'))}
          className="relative hidden sm:flex items-center w-full max-w-xs h-9 px-3 bg-muted/30 border border-input rounded-md text-sm text-muted-foreground hover:bg-muted/60 transition-all focus:outline-none focus:ring-1 focus:ring-ring"
        >
          <Search className="h-4 w-4 mr-2 text-muted-foreground" />
          <span className="text-xs font-normal">Search (Ctrl+K)...</span>
          <span className="absolute right-2 top-1.5 pointer-events-none inline-flex h-6 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium text-muted-foreground shadow-sm">
            <span>Ctrl</span>K
          </span>
        </button>

        <Button variant="ghost" size="icon" className="relative" onClick={() => dispatch(toggleNotificationDrawer())}>
          <Bell className="h-5 w-5" />
          {unreadCount > 0 && (
            <Badge 
              variant="destructive" 
              className="absolute -right-1 -top-1 h-5 w-5 rounded-full p-0 flex items-center justify-center text-[10px]"
            >
              {unreadCount}
            </Badge>
          )}
        </Button>

        <Button variant="ghost" size="icon" onClick={handleToggleTheme}>
          {themeMode === 'light' ? <Moon className="h-5 w-5" /> : <Sun className="h-5 w-5" />}
        </Button>
      </div>
    </header>
  );
};
