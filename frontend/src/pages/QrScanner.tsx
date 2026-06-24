import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { 
  QrCode, 
  Upload, 
  Camera, 
  Loader2, 
  AlertCircle,
  Clock,
  History
} from 'lucide-react';
import { qrService, QrScanResponse } from '@/features/qr';

export const QrScanner: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [dragActive, setDragActive] = useState(false);
  const [cameraActive, setCameraActive] = useState(false);
  const [simulatedQRType, setSimulatedQRType] = useState<string | null>(null);
  const [history, setHistory] = useState<QrScanResponse[]>([]);
  const navigate = useNavigate();

  const loadHistory = async () => {
    try {
      const data = await qrService.getHistory();
      setHistory(data);
    } catch (err: any) {
      console.error('Failed to load scan history', err);
    }
  };

  useEffect(() => {
    loadHistory();
  }, []);

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = async (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      await processImageFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      await processImageFile(e.target.files[0]);
    }
  };

  const processImageFile = async (file: File) => {
    setLoading(true);
    setError(null);

    try {
      const result = await qrService.scanQrImage(file);
      loadHistory();
      navigate('/verify/result', { state: { type: 'qr', data: result } });
    } catch (err: any) {
      setError(err.message || 'Failed to scan QR code image. Please check that the image contains a readable payment QR.');
    } finally {
      setLoading(false);
    }
  };

  const triggerCameraScan = async (presetType: 'safe' | 'dangerous') => {
    setLoading(true);
    setError(null);
    setSimulatedQRType(presetType);

    setTimeout(async () => {
      let rawText = '';
      if (presetType === 'safe') {
        rawText = 'upi://pay?pa=trustedmerchant@okaxis&pn=Trusted%20Store%20Ltd&mc=5411';
      } else {
        rawText = 'upi://pay?pa=scammer@upi&pn=Malicious%20Entity&mc=0000';
      }

      try {
        const result = await qrService.verifyQrText({ rawText });
        loadHistory();
        setCameraActive(false);
        setSimulatedQRType(null);
        navigate('/verify/result', { state: { type: 'qr', data: result } });
      } catch (err: any) {
        setError(err.message || 'Camera scanning simulation failed.');
      } finally {
        setLoading(false);
      }
    }, 1500);
  };

  return (
    <div className="space-y-8 max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h2 className="text-3xl font-extrabold tracking-tight bg-gradient-to-r from-foreground to-foreground/75 bg-clip-text text-transparent">
            QR Code Intelligence
          </h2>
          <p className="text-sm text-muted-foreground mt-1">
            Scan or upload UPI QR codes to inspect merchant records and fraud risk vectors before paying.
          </p>
        </div>
      </div>

      {error && (
        <Alert variant="destructive" className="border-destructive/20 bg-destructive/5">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>Scan Error</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Main actions grid */}
      <div className="grid gap-6 md:grid-cols-2">
        {/* Upload QR File Card */}
        <Card className={`relative overflow-hidden transition-all duration-300 border-2 border-dashed ${
          dragActive 
            ? 'border-primary bg-primary/5 shadow-md scale-[1.01]' 
            : 'border-muted hover:border-primary/45 hover:shadow-sm'
        }`}
          onDragEnter={handleDrag}
          onDragOver={handleDrag}
          onDragLeave={handleDrag}
          onDrop={handleDrop}
        >
          <CardHeader className="border-b bg-muted/5 pb-4">
            <CardTitle className="text-base font-bold flex items-center gap-2">
              <Upload className="h-4.5 w-4.5 text-primary" />
              Image File Upload
            </CardTitle>
            <CardDescription className="text-xs">Drag and drop a QR image file or click to select.</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col items-center justify-center p-8 min-h-[260px] text-center">
            {loading && !cameraActive ? (
              <div className="space-y-3">
                <Loader2 className="h-10 w-10 text-primary animate-spin mx-auto" />
                <p className="text-sm font-medium">Extracting and assessing QR data...</p>
              </div>
            ) : (
              <div className="space-y-4">
                <div className="p-4 bg-muted/60 rounded-full w-fit mx-auto shadow-sm">
                  <Upload className="h-8 w-8 text-muted-foreground" />
                </div>
                <div className="space-y-1">
                  <p className="text-sm font-semibold text-foreground">
                    Drag and drop file here, or click to upload
                  </p>
                  <p className="text-xs text-muted-foreground">
                    Supports PNG, JPG, JPEG files up to 5MB
                  </p>
                </div>
                <Label htmlFor="qr-file-upload" className="cursor-pointer inline-block">
                  <Button variant="outline" size="sm" asChild className="hover:bg-muted font-medium text-xs">
                    <span>Choose File</span>
                  </Button>
                  <Input
                    id="qr-file-upload"
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={handleFileChange}
                    disabled={loading}
                  />
                </Label>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Camera Simulation Card */}
        <Card className="flex flex-col justify-between border border-border/40 shadow-sm transition-all duration-300 hover:shadow-md">
          <CardHeader className="border-b bg-muted/10 pb-4">
            <CardTitle className="text-base font-bold flex items-center gap-2">
              <Camera className="h-4.5 w-4.5 text-primary" />
              Camera Scan Simulation
            </CardTitle>
            <CardDescription className="text-xs">Simulate real-time camera scan frame overlay.</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col items-center justify-center flex-grow p-6 text-center">
            {cameraActive ? (
              <div className="relative w-full aspect-video rounded-lg overflow-hidden bg-black flex flex-col items-center justify-center text-white border border-primary/50 shadow-inner">
                {/* Simulated Camera scan lines */}
                <div className="absolute inset-0 bg-gradient-to-b from-transparent via-primary/10 to-transparent animate-pulse pointer-events-none" />
                <div className="absolute left-[10%] right-[10%] top-[10%] bottom-[10%] border border-dashed border-primary/40 flex items-center justify-center">
                  <QrCode className="h-16 w-16 text-primary/60 animate-pulse" />
                </div>

                {loading && simulatedQRType ? (
                  <div className="z-10 bg-black/85 px-4 py-2.5 rounded-lg border border-border/10 flex items-center gap-2 shadow-xl">
                    <Loader2 className="h-4 w-4 animate-spin text-primary" />
                    <span className="text-xs font-bold">Scanning Preset...</span>
                  </div>
                ) : (
                  <div className="absolute bottom-4 flex gap-3 z-10">
                    <Button onClick={() => triggerCameraScan('safe')} size="sm" className="bg-green-600 hover:bg-green-700 text-xs font-semibold">
                      Scan Safe QR
                    </Button>
                    <Button onClick={() => triggerCameraScan('dangerous')} size="sm" variant="destructive" className="text-xs font-semibold">
                      Scan Dangerous QR
                    </Button>
                  </div>
                )}
              </div>
            ) : (
              <div className="space-y-4 py-6">
                <div className="p-4 bg-muted/60 rounded-full w-fit mx-auto shadow-sm">
                  <Camera className="h-8 w-8 text-muted-foreground" />
                </div>
                <div className="space-y-1">
                  <p className="text-sm font-semibold text-foreground">Camera feed is currently offline</p>
                  <p className="text-xs text-muted-foreground">Initiate simulation to scan mock payment QR codes.</p>
                </div>
                <Button onClick={() => setCameraActive(true)} variant="outline" size="sm" className="hover:bg-muted text-xs font-medium">
                  Start Camera Scan
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Recent QR Scans History */}
      <Card className="border border-border/40 shadow-sm">
        <CardHeader className="border-b bg-muted/10 pb-4 flex flex-row items-center justify-between space-y-0">
          <div>
            <CardTitle className="text-base font-bold flex items-center gap-2">
              <Clock className="h-4.5 w-4.5 text-muted-foreground" />
              Recent QR Investigations
            </CardTitle>
            <CardDescription className="text-xs mt-0.5">Assessed payment destinations via QR uploads.</CardDescription>
          </div>
          {history.length > 0 && (
            <Badge variant="outline" className="text-xs bg-muted/50 text-muted-foreground font-semibold">
              {history.length} scans total
            </Badge>
          )}
        </CardHeader>
        <CardContent className="pt-6">
          {history.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-10 text-center space-y-2">
              <History className="h-8 w-8 text-muted-foreground/45" />
              <p className="text-sm text-muted-foreground font-semibold">No recent QR scans found.</p>
              <p className="text-xs text-muted-foreground/75">Scanned QR targets will be registered in this table.</p>
            </div>
          ) : (
            <div className="space-y-3">
              {history.slice(0, 5).map((item, idx) => {
                const isCritical = item.riskLevel === 'CRITICAL' || item.riskLevel === 'HIGH';
                const isMedium = item.riskLevel === 'MEDIUM';
                return (
                  <div 
                    key={idx}
                    onClick={() => navigate('/verify/result', { state: { type: 'qr', data: { upi: item.upi, merchant: item.merchant, riskScore: item.riskScore, riskLevel: item.riskLevel, recommendation: item.recommendation } } })}
                    className="flex justify-between items-center p-3 rounded-lg border hover:bg-muted/30 hover:border-primary/10 transition-colors cursor-pointer"
                  >
                    <div className="flex items-center gap-3">
                      <QrCode className="h-5 w-5 text-muted-foreground shrink-0" />
                      <div>
                        <span className="font-semibold text-sm block text-foreground">{item.merchant}</span>
                        <span className="text-xs text-muted-foreground font-mono">{item.upi}</span>
                      </div>
                    </div>
                    <Badge 
                      variant="outline" 
                      className={`font-semibold text-xs py-0.5 px-2.5 rounded-full ${
                        isCritical
                          ? 'bg-red-500/10 text-red-600 border-red-200/50'
                          : isMedium
                          ? 'bg-amber-500/10 text-amber-600 border-amber-200/50'
                          : 'bg-emerald-500/10 text-emerald-600 border-emerald-200/50'
                      }`}
                    >
                      Risk: {item.riskLevel} ({item.riskScore})
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

export default QrScanner;
