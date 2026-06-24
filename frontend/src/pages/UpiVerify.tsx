import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
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
  Clock, 
  ShieldCheck, 
  ShieldAlert,
  Info,
  ArrowRight
} from 'lucide-react';
import { reputationService, ReputationHistoryItem } from '@/features/reputation';

export const UpiVerify: React.FC = () => {
  const [upiInput, setUpiInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
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

    try {
      const response = await reputationService.verifyUpi({ upiId: upiInput });
      loadHistory();
      
      // Brief transition simulation
      setTimeout(() => {
        navigate('/verify/result', { state: { type: 'upi', data: response } });
      }, 500);

    } catch (err: any) {
      setError(err.message || 'Verification failed. Please check the UPI ID format.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-8 max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h2 className="text-3xl font-extrabold tracking-tight bg-gradient-to-r from-foreground to-foreground/75 bg-clip-text text-transparent">
            Verify UPI Reputation
          </h2>
          <p className="text-sm text-muted-foreground mt-1">
            Analyze Virtual Payment Addresses (VPAs) against active blacklists and fraud history registers.
          </p>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        {/* Verification Card */}
        <Card className="md:col-span-2 border border-border/40 shadow-sm transition-all duration-300 hover:shadow-md">
          <CardHeader className="border-b bg-muted/10 pb-4">
            <CardTitle className="text-base font-bold flex items-center gap-2">
              <Search className="h-4.5 w-4.5 text-primary" />
              Reputation Engine Scanner
            </CardTitle>
            <CardDescription className="text-xs">
              Verify recipient address status, credentials, and compliance warnings.
            </CardDescription>
          </CardHeader>
          <CardContent className="pt-6">
            <form onSubmit={handleVerify} className="space-y-6">
              {error && (
                <Alert variant="destructive" className="border-destructive/20 bg-destructive/5">
                  <AlertCircle className="h-4 w-4" />
                  <AlertTitle>Validation Error</AlertTitle>
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}

              <div className="space-y-2.5">
                <Label htmlFor="upi-input" className="text-sm font-semibold text-foreground">
                  Virtual Payment Address (VPA) / UPI ID
                </Label>
                <div className="flex gap-2">
                  <div className="relative flex-grow">
                    <Search className="absolute left-3 top-3 h-4.5 w-4.5 text-muted-foreground" />
                    <Input
                      id="upi-input"
                      type="text"
                      placeholder="Enter UPI ID (e.g. merchant@okaxis, scammer@upi)"
                      className="pl-10 h-11 bg-muted/20 border-input placeholder:text-muted-foreground/60 focus:bg-transparent transition-all"
                      value={upiInput}
                      onChange={(e) => setUpiInput(e.target.value)}
                      disabled={loading}
                    />
                  </div>
                  <Button type="submit" disabled={loading || !upiInput.trim()} className="h-11 px-5 gap-2 font-semibold text-xs">
                    {loading ? (
                      <>
                        <Loader2 className="h-4 w-4 animate-spin" />
                        <span>Verifying...</span>
                      </>
                    ) : (
                      <>
                        <span>Scan ID</span>
                        <ArrowRight className="h-4 w-4" />
                      </>
                    )}
                  </Button>
                </div>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* Info panel */}
        <Card className="md:col-span-1 border border-border/40 shadow-sm bg-muted/40">
          <CardHeader className="border-b bg-muted/5 pb-4">
            <CardTitle className="text-sm font-bold flex items-center gap-2">
              <Info className="h-4.5 w-4.5 text-primary" />
              Safety Protocols
            </CardTitle>
          </CardHeader>
          <CardContent className="pt-6 space-y-4 text-xs text-muted-foreground leading-relaxed">
            <div className="space-y-1">
              <h5 className="font-bold text-foreground">VPA Structure Validation</h5>
              <p>Ensures correct address strings matching merchant or standard PSP suffix formats.</p>
            </div>
            <div className="space-y-1">
              <h5 className="font-bold text-foreground">Scam DB Corroboration</h5>
              <p>Cross-references target ID lists with user reports and cybersecurity databases.</p>
            </div>
            <div className="space-y-1">
              <h5 className="font-bold text-foreground">Risk Level Scoring</h5>
              <p>Assigns a score from 0 (Critical Fraud) to 100 (Trusted/Safe Entity) based on behavior.</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Verification History Logs */}
      <Card className="border border-border/40 shadow-sm">
        <CardHeader className="border-b bg-muted/10 pb-4 flex flex-row items-center justify-between space-y-0">
          <div>
            <CardTitle className="text-base font-bold flex items-center gap-2">
              <Clock className="h-4.5 w-4.5 text-muted-foreground" />
              Recent VPA Audits
            </CardTitle>
            <CardDescription className="text-xs mt-0.5 font-medium">Logs of evaluated Virtual Payment Addresses.</CardDescription>
          </div>
          {history.length > 0 && (
            <Badge variant="outline" className="text-xs bg-muted/50 text-muted-foreground font-semibold">
              {history.length} audits logged
            </Badge>
          )}
        </CardHeader>
        <CardContent className="pt-6">
          {history.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-10 text-center space-y-2">
              <Clock className="h-8 w-8 text-muted-foreground/45" />
              <p className="text-sm text-muted-foreground font-semibold">No recent searches.</p>
              <p className="text-xs text-muted-foreground/75">Searches will appear here once checks are completed.</p>
            </div>
          ) : (
            <div className="space-y-3">
              {history.slice(0, 5).map((item, idx) => {
                const isTrusted = item.status === 'TRUSTED' || item.status === 'SAFE';
                const isSuspicious = item.status === 'SUSPICIOUS';
                
                return (
                  <div 
                    key={idx}
                    onClick={() => navigate('/verify/result', { state: { type: 'upi', data: { upiId: item.verifiedEntity, score: item.riskScore, status: item.status, reasons: ['Historical reputation check log'] } } })}
                    className="flex justify-between items-center p-3 rounded-lg border hover:bg-muted/30 hover:border-primary/10 transition-colors cursor-pointer"
                  >
                    <div className="flex items-center gap-3">
                      {isTrusted ? (
                        <ShieldCheck className="h-5 w-5 text-emerald-500 shrink-0" />
                      ) : (
                        <ShieldAlert className="h-5 w-5 text-destructive shrink-0" />
                      )}
                      <div>
                        <span className="font-semibold text-sm font-mono block text-foreground">{item.verifiedEntity}</span>
                        <span className="text-xs text-muted-foreground">Type: {item.entityType} • {new Date(item.createdAt).toLocaleDateString()}</span>
                      </div>
                    </div>
                    <Badge 
                      variant="outline" 
                      className={`font-semibold text-xs py-0.5 px-2.5 rounded-full ${
                        isTrusted
                          ? 'bg-emerald-500/10 text-emerald-600 border-emerald-200/50'
                          : isSuspicious
                          ? 'bg-amber-500/10 text-amber-600 border-amber-200/50'
                          : 'bg-red-500/10 text-red-600 border-red-200/50'
                      }`}
                    >
                      Score: {item.riskScore} ({item.status})
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
