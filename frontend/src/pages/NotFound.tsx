import React from 'react';
import { Button } from '@/components/ui/button';
import { useNavigate } from 'react-router';

export const NotFound: React.FC = () => {
  const navigate = useNavigate();
  return (
    <div className="flex h-screen w-full flex-col items-center justify-center bg-background text-center px-4">
      <h1 className="text-9xl font-black text-primary/20">404</h1>
      <h2 className="text-3xl font-bold tracking-tight mt-4">Page Not Found</h2>
      <p className="text-muted-foreground mt-2 max-w-md">
        The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.
      </p>
      <Button onClick={() => navigate('/dashboard')} className="mt-8">
        Return to Dashboard
      </Button>
    </div>
  );
};
