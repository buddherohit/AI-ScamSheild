import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export const SecurityCenter: React.FC = () => {
  return (
    <div className="space-y-6 max-w-5xl">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Security Center</h2>
        <p className="text-muted-foreground">
          Advanced threat configuration and fraud analysis rules.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Fraud Analysis Rules</CardTitle>
          <CardDescription>Configure telemetry and scan heuristics.</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground">Detailed rules configuration goes here...</p>
        </CardContent>
      </Card>
    </div>
  );
};
