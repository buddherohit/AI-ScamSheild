import React from 'react';
import { useLocation, useNavigate } from 'react-router';
import { motion } from 'framer-motion';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { 
  ShieldAlert, 
  ShieldCheck, 
  AlertTriangle, 
  CheckCircle, 
  ArrowLeft, 
  AlertOctagon,
  Info
} from 'lucide-react';

interface UpiResult {
  upiId: string;
  score: number;
  status: 'TRUSTED' | 'SAFE' | 'SUSPICIOUS' | 'DANGEROUS';
  reasons: string[];
}

interface QrResult {
  merchant: string;
  upi: string;
  riskScore: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  recommendation: string;
}

export const VerifyResult: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  // Extract navigation state
  const state = location.state as { type: 'upi' | 'qr'; data: UpiResult | QrResult } | null;

  if (!state || !state.data) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] text-center space-y-4">
        <AlertOctagon className="h-16 w-16 text-destructive animate-pulse" />
        <h3 className="text-xl font-semibold">No Verification Data</h3>
        <p className="text-muted-foreground">Please submit a UPI ID or QR scan to assess risk.</p>
        <Button onClick={() => navigate('/dashboard')} variant="default" className="text-xs font-semibold h-10 px-4">
          Go to Dashboard
        </Button>
      </div>
    );
  }

  const { type, data } = state;

  // Compute unified parameters
  let title = '';
  let entityValue = '';
  let score = 0; // Reputation score (0-100, higher is better)
  let status: 'TRUSTED' | 'SAFE' | 'SUSPICIOUS' | 'DANGEROUS' = 'SAFE';
  let riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' = 'LOW';
  let recommendation = '';
  let reasons: string[] = [];
  let merchant = '';

  if (type === 'upi') {
    const upiData = data as UpiResult;
    title = 'UPI Reputation Audit Report';
    entityValue = upiData.upiId;
    score = upiData.score;
    status = upiData.status;
    reasons = upiData.reasons;
    
    // Map status to risk levels and recommendations
    if (status === 'TRUSTED') {
      riskLevel = 'LOW';
      recommendation = 'Verified Safe. Fully Trusted Destination.';
    } else if (status === 'SAFE') {
      riskLevel = 'LOW';
      recommendation = 'Clear Parameters. No Active Threat Flags.';
    } else if (status === 'SUSPICIOUS') {
      riskLevel = 'MEDIUM';
      recommendation = 'Proceed with Caution. Double-Check Receiver.';
    } else {
      riskLevel = 'HIGH';
      recommendation = 'CRITICAL WARNING: Avoid Funds Transfer!';
    }
  } else {
    const qrData = data as QrResult;
    title = 'QR Code Intelligent Assessment';
    entityValue = qrData.upi;
    merchant = qrData.merchant;
    score = 100 - qrData.riskScore; // Translate risk to reputation score
    riskLevel = qrData.riskLevel;
    recommendation = qrData.recommendation;
    reasons = [
      `Destination Merchant: ${qrData.merchant}`,
      `Risk Level Assessed: ${qrData.riskLevel}`,
    ];

    // Determine status from riskScore
    if (score >= 90) status = 'TRUSTED';
    else if (score >= 70) status = 'SAFE';
    else if (score >= 40) status = 'SUSPICIOUS';
    else status = 'DANGEROUS';
  }

  // Visual configuration based on status
  const statusConfigs = {
    TRUSTED: {
      color: 'text-green-500 border-green-500 bg-green-500/10',
      icon: ShieldCheck,
      gaugeColor: '#10b981',
      description: 'This payment destination is fully verified and matches a reputable merchant.',
    },
    SAFE: {
      color: 'text-emerald-500 border-emerald-500 bg-emerald-500/10',
      icon: CheckCircle,
      gaugeColor: '#34d399',
      description: 'No threat history detected. Normal risk parameters.',
    },
    SUSPICIOUS: {
      color: 'text-amber-500 border-amber-500 bg-amber-500/10',
      icon: AlertTriangle,
      gaugeColor: '#f59e0b',
      description: 'Detections flag suspicious patterns or unverified merchant profile.',
    },
    DANGEROUS: {
      color: 'text-red-500 border-red-500 bg-red-500/10',
      icon: ShieldAlert,
      gaugeColor: '#ef4444',
      description: 'CRITICAL WARNING: This target is linked to active scams or fraud reports.',
    },
  };

  const currentConfig = statusConfigs[status];
  const StatusIcon = currentConfig.icon;

  // Semi-circle gauge rotation calculation
  // 0% -> -90deg, 100% -> 90deg
  const rotationDegrees = -90 + (score / 100) * 180;

  return (
    <div className="space-y-8 max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between">
        <Button onClick={() => navigate(-1)} variant="ghost" size="sm" className="gap-2 hover:bg-muted font-semibold text-xs">
          <ArrowLeft className="h-4 w-4" />
          Back
        </Button>
        <Badge variant="outline" className={`font-bold text-xs px-3.5 py-1 rounded-full ${currentConfig.color}`}>
          Status: {status}
        </Badge>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        {/* Left Side: Score Visualization */}
        <Card className="md:col-span-1 flex flex-col justify-between overflow-hidden relative border border-border/40 shadow-sm transition-all duration-300 hover:shadow-md">
          {/* Vertical Color Indicator */}
          <div className="absolute top-0 left-0 w-1.5 h-full" style={{ backgroundColor: currentConfig.gaugeColor }} />
          <CardHeader className="pb-2">
            <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider text-center mt-1">
              Reputation Rating
            </CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col items-center justify-center p-6 flex-grow">
            {/* Semicircular Risk Gauge */}
            <div className="relative w-48 h-24 flex items-end justify-center overflow-hidden">
              {/* Outer Arc */}
              <div className="absolute top-0 left-0 w-48 h-48 rounded-full border-8 border-muted/50" />
              {/* Value Arc (Drawn as colored indicator) */}
              <div 
                className="absolute top-0 left-0 w-48 h-48 rounded-full border-8 transition-transform duration-1000 ease-out origin-center"
                style={{
                  borderColor: `${currentConfig.gaugeColor} transparent transparent ${currentConfig.gaugeColor}`,
                  transform: `rotate(${rotationDegrees}deg)`,
                }}
              />
              <div className="absolute inset-2 bg-card rounded-full flex flex-col items-center justify-end pb-1.5 z-10">
                <span className="text-4xl font-extrabold tracking-tight">{score}</span>
                <span className="text-[10px] text-muted-foreground uppercase font-bold tracking-wider">
                  Out of 100
                </span>
              </div>
            </div>

            <div className="text-center mt-6">
              <span className={`text-xl font-extrabold tracking-tight`} style={{ color: currentConfig.gaugeColor }}>
                {status}
              </span>
              <p className="text-xs text-muted-foreground mt-1.5 font-medium">
                Risk Level Vector: <span className="font-bold">{riskLevel}</span>
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Right Side: Information Panels */}
        <div className="md:col-span-2 space-y-6">
          <Card className="border border-border/40 shadow-sm transition-all duration-300 hover:shadow-md">
            <CardHeader className="border-b bg-muted/10 pb-4">
              <CardTitle className="text-base font-bold">{title}</CardTitle>
              <CardDescription className="text-xs">
                Real-time security analytics generated by ScamShield reputation nodes.
              </CardDescription>
            </CardHeader>
            <CardContent className="pt-6 space-y-4">
              {merchant && (
                <div className="flex justify-between items-center border-b pb-2">
                  <span className="text-sm text-muted-foreground font-medium">Merchant Entity Name</span>
                  <span className="font-bold text-sm text-foreground">{merchant}</span>
                </div>
              )}
              <div className="flex justify-between items-center border-b pb-2">
                <span className="text-sm text-muted-foreground font-medium">UPI ID Address</span>
                <span className="font-mono font-bold text-sm text-foreground select-all">{entityValue}</span>
              </div>

              {/* Recommendation Panel */}
              <div className={`p-4 rounded-lg border flex items-start gap-3 mt-6 ${
                status === 'DANGEROUS' 
                  ? 'bg-destructive/10 border-destructive/20 text-destructive' 
                  : 'bg-muted/40 border-muted'
              }`}>
                <div className="p-2.5 bg-background rounded-lg shadow-sm shrink-0">
                  <StatusIcon className="h-5 w-5" style={{ color: currentConfig.gaugeColor }} />
                </div>
                <div className="space-y-1">
                  <h4 className="font-bold text-sm text-foreground">Action Recommendation</h4>
                  <p className="text-xs font-extrabold uppercase tracking-wider" style={{ color: currentConfig.gaugeColor }}>
                    {recommendation}
                  </p>
                  <p className="text-xs text-muted-foreground leading-relaxed mt-1">
                    {currentConfig.description}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Reasons List */}
          <Card className="border border-border/40 shadow-sm">
            <CardHeader className="border-b bg-muted/10 pb-4">
              <CardTitle className="text-base font-bold flex items-center gap-2">
                <Info className="h-4.5 w-4.5 text-muted-foreground" />
                Security Risk Vectors Mapped
              </CardTitle>
              <CardDescription className="text-xs">
                Matches flagged by regular expressions, active reports, and intelligence nodes.
              </CardDescription>
            </CardHeader>
            <CardContent className="pt-6">
              <ul className="space-y-3.5">
                {reasons.map((reason, idx) => (
                  <motion.li 
                    key={idx}
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: idx * 0.1 }}
                    className="flex items-start gap-2.5 text-xs text-muted-foreground font-medium leading-relaxed"
                  >
                    <span className="h-2 w-2 rounded-full bg-primary mt-1.5 shrink-0" />
                    <span>{reason}</span>
                  </motion.li>
                ))}
              </ul>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default VerifyResult;
