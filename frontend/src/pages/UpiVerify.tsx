import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
import { motion, AnimatePresence } from 'framer-motion';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { 
  Search, 
  Loader2, 
  AlertCircle, 
  History, 
  ShieldCheck, 
  ShieldAlert
} from 'lucide-react';
import { reputationService, UpiVerifyResponse, ReputationHistoryItem } from '@/features/reputation';

export const UpiVerify: React.FC = () => {
  const [upiInput, setUpiInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<UpiVerifyResponse | null>(null);
  const [history, setHistory] = useState<ReputationHistoryItem[]>([]);
  const navigate = useNavigate();

  const loadHistory = async () => {
    try {
      const data = await reputationService.getHistory();
      setHistory(data);
    } catch (err: any) {
      console.error('Failed to load history', err);
    }
  };

  useEffect(() => {
    loadHistory();
  }, []);

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!upiInput.trim()) return;

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await reputationService.verifyUpi({ upiId: upiInput });
      setResult(response);
      loadHistory(); // Refresh history log
      
      // Optionally navigate to dedicated result page after a brief animation delay,
      // or display results directly on screen.
      // Let's navigate to /verify/result to keep pages clean and responsive.
      setTimeout(() => {
        navigate('/verify/result', { state: { type: 'upi', data: response } });
      }, 800);

    } catch (err: any) {
      setError(err.message || 'Verification failed. Please check the UPI ID format.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Verify UPI ID</h2>
        <p className="text-muted-foreground">
          Perform a real-time risk assessment on any Virtual Payment Address (VPA) before transferring funds.
        </p>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        {/* Verification Card */}
        <Card className="md:col-span-2 shadow-lg border-primary/10">
          <CardHeader>
            <CardTitle>Reputation Scanner</CardTitle>
            <CardDescription>
              Check reported entities, threat indicators, and compliance warnings.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleVerify} className="space-y-4">
              {error && (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertTitle>Validation Error</AlertTitle>
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}

              <div className="space-y-2">
                <Label htmlFor="upi">Enter UPI ID</Label>
                <div className="flex gap-2">
                  <div className="relative flex-grow">
                    <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                    <Input
                      id="upi"
                      placeholder="e.g. abc@okaxis, merchant@upi"
                      className="pl-9"
                      value={upiInput}
                      onChange={(e) => setUpiInput(e.target.value)}
                      disabled={loading}
                    />
                  </div>
                  <Button type="submit" disabled={loading} className="px-6">
                    {loading ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Verifying...
                      </>
                    ) : (
                      'Verify'
                    )}
                  </Button>
                </div>
              </div>
            </form>

            <AnimatePresence>
              {result && (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: 10 }}
                  className="mt-6 p-4 border rounded-lg bg-primary/5 flex items-center justify-between"
                >
                  <div>
                    <h4 className="font-semibold text-sm">Scan Successful!</h4>
                    <p className="text-xs text-muted-foreground">Redirecting to assessment details...</p>
                  </div>
                  <Badge className="text-xs font-semibold py-1 px-3">
                    SCORE: {result.score}
                  </Badge>
                </motion.div>
              )}
            </AnimatePresence>
          </CardContent>
        </Card>

        {/* Info Card */}
        <Card className="md:col-span-1 bg-muted/35">
          <CardHeader>
            <CardTitle className="text-sm font-semibold">Reputation Scale</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-xs">
            <div className="flex justify-between items-center border-b pb-1">
              <span className="font-medium text-green-500">Trusted</span>
              <span className="text-muted-foreground">90 - 100</span>
            </div>
            <div className="flex justify-between items-center border-b pb-1">
              <span className="font-medium text-emerald-500">Safe</span>
              <span className="text-muted-foreground">70 - 89</span>
            </div>
            <div className="flex justify-between items-center border-b pb-1">
              <span className="font-medium text-amber-500">Suspicious</span>
              <span className="text-muted-foreground">40 - 69</span>
            </div>
            <div className="flex justify-between items-center pb-1">
              <span className="font-medium text-red-500">Dangerous</span>
              <span className="text-muted-foreground">0 - 39</span>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* History Section */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <div>
            <CardTitle className="text-lg">Recent Verifications</CardTitle>
            <CardDescription>Your log of recently verified payment entities.</CardDescription>
          </div>
          <History className="h-5 w-5 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          {history.length === 0 ? (
            <p className="text-sm text-muted-foreground text-center py-6">
              No recent verifications found.
            </p>
          ) : (
            <div className="space-y-3">
              {history.slice(0, 5).map((item) => {
                const isThreat = item.status === 'DANGEROUS' || item.status === 'SUSPICIOUS';
                const statusColors = {
                  TRUSTED: 'bg-green-500/10 text-green-600 border-green-200',
                  SAFE: 'bg-emerald-500/10 text-emerald-600 border-emerald-200',
                  SUSPICIOUS: 'bg-amber-500/10 text-amber-600 border-amber-200',
                  DANGEROUS: 'bg-red-500/10 text-red-600 border-red-200',
                };
                return (
                  <div 
                    key={item.id}
                    onClick={() => navigate('/verify/result', { state: { type: 'upi', data: { upiId: item.verifiedEntity, score: 100 - item.riskScore, status: item.status, reasons: [] } } })}
                    className="flex justify-between items-center p-3 rounded-lg border hover:bg-muted/30 transition-colors cursor-pointer"
                  >
                    <div className="flex items-center gap-3">
                      {isThreat ? (
                        <ShieldAlert className="h-5 w-5 text-destructive shrink-0" />
                      ) : (
                        <ShieldCheck className="h-5 w-5 text-green-500 shrink-0" />
                      )}
                      <div>
                        <span className="font-semibold text-sm select-all">{item.verifiedEntity}</span>
                        <span className="text-xs text-muted-foreground block">
                          Verified: {new Date(item.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                    </div>
                    <Badge variant="outline" className={statusColors[item.status]}>
                      {item.status} ({100 - item.riskScore})
                    </Badge>
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default UpiVerify;
