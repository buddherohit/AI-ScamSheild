import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { 
  ArrowLeft, 
  ShieldAlert, 
  ShieldCheck, 
  AlertTriangle, 
  Terminal, 
  Clock, 
  Info 
} from 'lucide-react';
import { aiService, SmsAnalysisResponse } from '@/features/ai';

export const SmsDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [data, setData] = useState<SmsAnalysisResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const loadDetails = async () => {
      if (!id) return;
      try {
        const details = await aiService.getAnalysisDetails(parseInt(id));
        setData(details);
      } catch (err: any) {
        setError(err.message || 'Failed to load details for this SMS analysis.');
      } finally {
        setLoading(false);
      }
    };
    loadDetails();
  }, [id]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px]">
        <svg className="animate-spin h-8 w-8 text-primary" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <span className="text-sm font-medium mt-4">Loading threat telemetry...</span>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] text-center space-y-4">
        <AlertTriangle className="h-16 w-16 text-destructive" />
        <h3 className="text-xl font-semibold">Report Not Found</h3>
        <p className="text-muted-foreground">{error || 'Requested SMS report details are not available.'}</p>
        <Button onClick={() => navigate('/sms')} variant="default">
          Back to Scanner
        </Button>
      </div>
    );
  }

  // Configurations based on Risk Level
  const riskConfigs = {
    LOW: {
      color: 'text-green-500 border-green-500 bg-green-500/10',
      icon: ShieldCheck,
      gaugeColor: '#10b981',
      alertVariant: 'default' as const,
      description: 'SMS is classified as low risk. Contains standard informational context.',
    },
    MEDIUM: {
      color: 'text-amber-500 border-amber-500 bg-amber-500/10',
      icon: AlertTriangle,
      gaugeColor: '#f59e0b',
      alertVariant: 'warning' as const,
      description: 'SMS is classified as suspicious. Pre-analysis indicates social engineering flags.',
    },
    HIGH: {
      color: 'text-red-500 border-red-500 bg-red-500/10',
      icon: ShieldAlert,
      gaugeColor: '#ef4444',
      alertVariant: 'destructive' as const,
      description: 'SMS is classified as high threat. Urgency or OTP interception vectors identified.',
    },
    CRITICAL: {
      color: 'text-red-600 border-red-600 bg-red-600/15',
      icon: ShieldAlert,
      gaugeColor: '#dc2626',
      alertVariant: 'destructive' as const,
      description: 'CRITICAL THREAT: Message contains active malicious URLs or severe scam signatures.',
    },
  };

  const currentConfig = riskConfigs[data.riskLevel];
  const StatusIcon = currentConfig.icon;

  // Semi-circle gauge rotation calculation
  const rotationDegrees = -90 + (data.riskScore / 100) * 180;

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <div className="flex items-center justify-between">
        <Button onClick={() => navigate('/sms/history')} variant="ghost" className="gap-2">
          <ArrowLeft className="h-4 w-4" />
          Back to History
        </Button>
        <Badge variant="outline" className={`font-semibold px-3 py-1 ${currentConfig.color}`}>
          RISK LEVEL: {data.riskLevel}
        </Badge>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        {/* Left column: Confidence Meter & SMS contents */}
        <div className="md:col-span-1 space-y-6">
          <Card className="flex flex-col items-center p-6 text-center shadow-sm">
            <CardHeader className="p-0 mb-4">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                Threat Score Gauge
              </CardTitle>
            </CardHeader>
            <CardContent className="p-0 flex flex-col items-center">
              {/* Semicircle Gauge */}
              <div className="relative w-44 h-22 flex items-end justify-center overflow-hidden">
                <div className="absolute top-0 left-0 w-44 h-44 rounded-full border-8 border-muted" />
                <div 
                  className="absolute top-0 left-0 w-44 h-44 rounded-full border-8 origin-center transition-transform duration-1000 ease-out"
                  style={{
                    borderColor: `${currentConfig.gaugeColor} transparent transparent ${currentConfig.gaugeColor}`,
                    transform: `rotate(${rotationDegrees}deg)`,
                  }}
                />
                <div className="absolute inset-2 bg-card rounded-full flex flex-col items-center justify-end pb-1 z-10">
                  <span className="text-3xl font-extrabold">{data.riskScore}</span>
                  <span className="text-[10px] text-muted-foreground uppercase font-bold tracking-widest">
                    Score
                  </span>
                </div>
              </div>
              
              <div className="mt-4">
                <span className="text-base font-bold" style={{ color: currentConfig.gaugeColor }}>
                  {data.riskLevel}
                </span>
                <p className="text-[10px] text-muted-foreground mt-0.5 uppercase">
                  Category: <span className="font-bold text-foreground">{data.category}</span>
                </p>
              </div>
            </CardContent>
          </Card>

          {/* SMS Raw Text Preview */}
          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-semibold">Raw SMS Message</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="p-3 bg-muted/40 rounded-lg text-xs font-mono border leading-relaxed select-all">
                {data.smsText}
              </div>
              <p className="text-[10px] text-muted-foreground mt-2 flex items-center gap-1">
                <Clock className="h-3 w-3" />
                Analyzed at: {new Date(data.createdAt).toLocaleString()}
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Right columns: Explanation & Recommendations */}
        <div className="md:col-span-2 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-xl font-bold flex items-center gap-2">
                <StatusIcon className="h-5 w-5" style={{ color: currentConfig.gaugeColor }} />
                Analysis Explanations
              </CardTitle>
              <CardDescription>
                Detailed breakdown generated by the AI Semantic engine.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Layperson summary */}
              <div className="space-y-2">
                <h4 className="text-sm font-bold flex items-center gap-1.5 text-foreground">
                  <Info className="h-4 w-4 text-muted-foreground" />
                  Simple Rationale
                </h4>
                <p className="text-xs text-muted-foreground leading-relaxed pl-5">
                  {data.simpleExplanation}
                </p>
              </div>

              {/* Technical description */}
              <div className="space-y-2">
                <h4 className="text-sm font-bold flex items-center gap-1.5 text-foreground">
                  <Terminal className="h-4 w-4 text-muted-foreground" />
                  Technical Analysis
                </h4>
                <p className="text-xs text-muted-foreground font-mono bg-muted/20 p-3 rounded border leading-relaxed pl-5">
                  {data.technicalExplanation}
                </p>
              </div>

              {/* Action recommendation */}
              <div className="p-4 bg-muted/50 rounded-lg border">
                <h4 className="text-sm font-bold text-foreground">Recommended Actions</h4>
                <p className="text-xs text-muted-foreground mt-1.5">
                  {data.recommendation}
                </p>
              </div>
            </CardContent>
          </Card>

          {/* Indicators list */}
          <Card>
            <CardHeader>
              <CardTitle className="text-base font-semibold">Threat Vectors Detected</CardTitle>
              <CardDescription>Rules-based and semantic markers matched inside payload.</CardDescription>
            </CardHeader>
            <CardContent>
              {data.indicators.length === 0 ? (
                <p className="text-xs text-muted-foreground">No specific indicators matched.</p>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {data.indicators.map((ind, idx) => (
                    <Badge key={idx} variant="outline" className="text-xs py-1 px-2.5 font-medium border-muted-foreground/30 bg-muted/10">
                      {ind}
                    </Badge>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default SmsDetails;
