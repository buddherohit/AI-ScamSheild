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

      <div className="flex items-center gap-2 md:gap-4 flex-1 justify-end">
        <div className="relative hidden sm:block w-full max-w-xs">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            type="search"
            placeholder="Search (Ctrl+K)..."
            className="w-full bg-muted/50 pl-9 md:w-[300px]"
          />
        </div>

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
