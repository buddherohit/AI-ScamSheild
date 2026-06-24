import React from 'react';
import { Card, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Laptop, Smartphone } from 'lucide-react';

export const SessionManagement: React.FC = () => {
  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Active Sessions</h2>
        <p className="text-muted-foreground">
          View and manage all active sessions associated with your account.
        </p>
      </div>

      <div className="space-y-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <div className="flex items-center gap-4">
              <Laptop className="h-8 w-8 text-primary" />
              <div>
                <CardTitle className="text-lg">Windows PC - Chrome</CardTitle>
                <CardDescription>New York, USA • IP: 192.168.1.1</CardDescription>
              </div>
            </div>
            <div className="flex flex-col items-end">
              <span className="text-sm font-medium text-green-500">Current Session</span>
              <span className="text-xs text-muted-foreground">Active now</span>
            </div>
          </CardHeader>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <div className="flex items-center gap-4">
              <Smartphone className="h-8 w-8 text-muted-foreground" />
              <div>
                <CardTitle className="text-lg">iPhone 13 - Safari</CardTitle>
                <CardDescription>Boston, USA • IP: 10.0.0.1</CardDescription>
              </div>
            </div>
            <div className="flex flex-col items-end">
              <Button variant="outline" size="sm" className="text-destructive border-destructive hover:bg-destructive hover:text-white">
                Revoke
              </Button>
              <span className="text-xs text-muted-foreground mt-1">Last active 2 hours ago</span>
            </div>
          </CardHeader>
        </Card>
      </div>
    </div>
  );
};
