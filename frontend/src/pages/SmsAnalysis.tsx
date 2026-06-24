import React, { useState } from 'react';
import { useNavigate } from 'react-router';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { 
  MessageSquareWarning, 
  Loader2, 
  AlertCircle, 
  HelpCircle,
  TrendingUp,
  History
} from 'lucide-react';
import { aiService } from '@/features/ai';

export const SmsAnalysis: React.FC = () => {
  const [smsText, setSmsText] = useState('');
  const [loading, setLoading] = useState(false);
  const [loadingPhase, setLoadingPhase] = useState('');
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleAnalyze = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!smsText.trim()) return;

    setLoading(true);
    setError(null);

    // Multi-phase loading text simulation
    const phases = [
      'Sanitizing message text payload...',
      'Matching keywords in local pattern registers...',
      'Evaluating threat links with blocklist datasets...',
      'Dispatched content tokens to NLP Classification Engine...',
      'Finalizing threat score and explanations...'
    ];

    let phaseIndex = 0;
    setLoadingPhase(phases[0] || '');
    const interval = setInterval(() => {
      phaseIndex++;
      if (phaseIndex < phases.length) {
        setLoadingPhase(phases[phaseIndex] || '');
      }
    }, 800);

    try {
      const response = await aiService.analyzeSms({ smsText });
      clearInterval(interval);
      
      // Navigate to detailed view page
      navigate(`/sms/analysis/${response.id}`);
    } catch (err: any) {
      clearInterval(interval);
      setError(err.message || 'SMS threat evaluation failed. Please check connection and try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">AI SMS Intelligence</h2>
          <p className="text-muted-foreground">
            Paste any SMS text payload to verify its intent, extract scam vectors, and outline actions.
          </p>
        </div>
        <div className="flex gap-2">
          <Button onClick={() => navigate('/sms/history')} variant="outline" size="sm" className="gap-2">
            <History className="h-4 w-4" />
            History Log
          </Button>
          <Button onClick={() => navigate('/sms/insights')} variant="outline" size="sm" className="gap-2">
            <TrendingUp className="h-4 w-4" />
            Insights
          </Button>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        {/* Input area */}
        <Card className="md:col-span-2 shadow-md border-primary/10">
          <CardHeader>
            <CardTitle>SMS Scam Scanner</CardTitle>
            <CardDescription>
              Analyze language, suspicious phone URLs, OTP codes, and urgent threats.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleAnalyze} className="space-y-4">
              {error && (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertTitle>Analysis Error</AlertTitle>
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}

              <div className="space-y-2">
                <Label htmlFor="sms-textarea" className="text-sm font-semibold">
                  Paste SMS Content
                </Label>
                <textarea
                  id="sms-textarea"
                  rows={6}
                  placeholder="Paste message contents here... e.g., 'Dear customer, your bank account has been disabled. Login immediately at http://scam-bank.com to restore access.'"
                  className="w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50 resize-y"
                  value={smsText}
                  onChange={(e) => setSmsText(e.target.value)}
                  disabled={loading}
                />
              </div>

              <Button type="submit" disabled={loading || !smsText.trim()} className="w-full">
                {loading ? (
                  <div className="flex items-center justify-center gap-2">
                    <Loader2 className="h-4 w-4 animate-spin" />
                    <span>{loadingPhase}</span>
                  </div>
                ) : (
                  'Analyze SMS'
                )}
              </Button>
            </form>
          </CardContent>
        </Card>

        {/* Informational Cards */}
        <Card className="md:col-span-1 bg-muted/40">
          <CardHeader>
            <CardTitle className="text-sm font-bold flex items-center gap-1.5">
              <MessageSquareWarning className="h-4 w-4 text-primary" />
              Scan Parameters
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-xs text-muted-foreground">
            <div className="space-y-1">
              <h5 className="font-semibold text-foreground">Semantic Intent</h5>
              <p>Detects phishing patterns, pretexts, impersonation targets, and social engineering tricks.</p>
            </div>
            <div className="space-y-1">
              <h5 className="font-semibold text-foreground">Rule Matches</h5>
              <p>Evaluates known keywords corresponding to OTP codes, PAN numbers, KYC deadlines, and bank details.</p>
            </div>
            <div className="space-y-1">
              <h5 className="font-semibold text-foreground">Link Verification</h5>
              <p>Extracts URLs and verifies them against the central threat indicators registry to trigger immediate high risk alerts.</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Quick FAQ / Guide */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-1.5">
            <HelpCircle className="h-4 w-4 text-muted-foreground" />
            Scam Detection Hints
          </CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4 sm:grid-cols-3 text-xs">
          <div className="p-3 border rounded-lg bg-card shadow-sm">
            <h6 className="font-bold mb-1 text-red-500">Urgency and Suspensions</h6>
            <p className="text-muted-foreground">Messages demanding immediate action, KYC updates, or warning of account freezes are typical banking scam cues.</p>
          </div>
          <div className="p-3 border rounded-lg bg-card shadow-sm">
            <h6 className="font-bold mb-1 text-amber-500">OTP and PIN Requests</h6>
            <p className="text-muted-foreground">Legitimate organizations will never request a one-time password (OTP). Masking codes or request templates is a critical hazard.</p>
          </div>
          <div className="p-3 border rounded-lg bg-card shadow-sm">
            <h6 className="font-bold mb-1 text-blue-500">Redirect Links</h6>
            <p className="text-muted-foreground">Phishing URLs often mimic popular services (e.g. pay-gov-india.com). Checking and auditing redirect domains is important.</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default SmsAnalysis;
