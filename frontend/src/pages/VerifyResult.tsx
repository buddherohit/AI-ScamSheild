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
  AlertOctagon
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
        <Button onClick={() => navigate('/dashboard')} variant="default">
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
    title = 'UPI Reputation Check';
    entityValue = upiData.upiId;
    score = upiData.score;
    status = upiData.status;
    reasons = upiData.reasons;
    
    // Map status to risk levels and recommendations
    if (status === 'TRUSTED') {
      riskLevel = 'LOW';
      recommendation = 'Safe to proceed';
    } else if (status === 'SAFE') {
      riskLevel = 'LOW';
      recommendation = 'Safe to proceed';
    } else if (status === 'SUSPICIOUS') {
      riskLevel = 'MEDIUM';
      recommendation = 'Proceed with caution';
    } else {
      riskLevel = 'HIGH';
      recommendation = 'Do not proceed';
    }
  } else {
    const qrData = data as QrResult;
    title = 'QR Code Intelligence Assessment';
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
      alertVariant: 'default' as const,
    },
    SAFE: {
      color: 'text-emerald-500 border-emerald-500 bg-emerald-500/10',
      icon: CheckCircle,
      gaugeColor: '#34d399',
      description: 'No threat history detected. Normal risk parameters.',
      alertVariant: 'default' as const,
    },
    SUSPICIOUS: {
      color: 'text-amber-500 border-amber-500 bg-amber-500/10',
      icon: AlertTriangle,
      gaugeColor: '#f59e0b',
      description: 'Detections flag suspicious patterns or unverified merchant profile.',
      alertVariant: 'warning' as const,
    },
    DANGEROUS: {
      color: 'text-red-500 border-red-500 bg-red-500/10',
      icon: ShieldAlert,
      gaugeColor: '#ef4444',
      description: 'CRITICAL WARNING: This target is linked to active scams or fraud reports.',
      alertVariant: 'destructive' as const,
    },
  };

  const currentConfig = statusConfigs[status];
  const StatusIcon = currentConfig.icon;

  // Semi-circle gauge rotation calculation
  // 0% -> -90deg, 100% -> 90deg
  const rotationDegrees = -90 + (score / 100) * 180;

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <div className="flex items-center justify-between">
        <Button onClick={() => navigate(-1)} variant="ghost" className="gap-2">
          <ArrowLeft className="h-4 w-4" />
          Back
        </Button>
        <Badge variant="outline" className={`font-semibold px-3 py-1 ${currentConfig.color}`}>
          {status}
        </Badge>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        {/* Left Side: Score Visualization */}
        <Card className="md:col-span-1 flex flex-col justify-between overflow-hidden relative">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground text-center">
              Reputation Score
            </CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col items-center justify-center p-6 flex-grow">
            {/* Semicircular Risk Gauge */}
            <div className="relative w-48 h-24 flex items-end justify-center overflow-hidden">
              {/* Outer Arc */}
              <div className="absolute top-0 left-0 w-48 h-48 rounded-full border-8 border-muted" />
              {/* Value Arc (Drawn as colored indicator) */}
              <div 
                className="absolute top-0 left-0 w-48 h-48 rounded-full border-8 transition-transform duration-1000 ease-out origin-center"
                style={{
                  borderColor: `${currentConfig.gaugeColor} transparent transparent ${currentConfig.gaugeColor}`,
                  transform: `rotate(${rotationDegrees}deg)`,
                }}
              />
              <div className="absolute inset-2 bg-card rounded-full flex flex-col items-center justify-end pb-1 z-10">
                <span className="text-4xl font-extrabold tracking-tight">{score}</span>
                <span className="text-xs text-muted-foreground uppercase font-bold tracking-wider">
                  Out of 100
                </span>
              </div>
            </div>

            <div className="text-center mt-4">
              <span className={`text-lg font-bold tracking-tight`} style={{ color: currentConfig.gaugeColor }}>
                {status}
              </span>
              <p className="text-xs text-muted-foreground mt-1">
                Risk Level: <span className="font-semibold">{riskLevel}</span>
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Right Side: Information Panels */}
        <div className="md:col-span-2 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-2xl font-bold">{title}</CardTitle>
              <CardDescription>
                Real-time reputation results for this payment destination.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {merchant && (
                <div className="flex justify-between items-center border-b pb-2">
                  <span className="text-sm text-muted-foreground">Merchant Entity</span>
                  <span className="font-semibold text-foreground">{merchant}</span>
                </div>
              )}
              <div className="flex justify-between items-center border-b pb-2">
                <span className="text-sm text-muted-foreground">UPI ID</span>
                <span className="font-semibold text-foreground select-all">{entityValue}</span>
              </div>

              {/* Recommendation Panel */}
              <div className={`p-4 rounded-lg border flex items-start gap-3 mt-4 ${status === 'DANGEROUS' ? 'bg-destructive/10 border-destructive/20 text-destructive' : 'bg-muted/50 border-muted'}`}>
                <div className="p-2 bg-background rounded-md shadow-sm">
                  <StatusIcon className="h-5 w-5" style={{ color: currentConfig.gaugeColor }} />
                </div>
                <div>
                  <h4 className="font-bold text-sm">Action Recommendation</h4>
                  <p className="text-xs font-semibold uppercase mt-0.5" style={{ color: currentConfig.gaugeColor }}>
                    {recommendation}
                  </p>
                  <p className="text-xs text-muted-foreground mt-1">
                    {currentConfig.description}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Reasons List */}
          <Card>
            <CardHeader>
              <CardTitle className="text-base font-semibold">Fraud Risk Indicators</CardTitle>
              <CardDescription>
                System findings and security details matching our rules.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="space-y-3">
                {reasons.map((reason, idx) => (
                  <motion.li 
                    key={idx}
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: idx * 0.1 }}
                    className="flex items-start gap-2.5 text-sm text-muted-foreground"
                  >
                    <span className="h-1.5 w-1.5 rounded-full bg-primary mt-2 shrink-0" />
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
