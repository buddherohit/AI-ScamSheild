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
  AlertCircle
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

  // Handle file drop
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

  // Handle file select
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
      // Navigate to results
      navigate('/verify/result', { state: { type: 'qr', data: result } });
    } catch (err: any) {
      setError(err.message || 'Failed to scan QR code image. Please check that the image contains a readable payment QR.');
    } finally {
      setLoading(false);
    }
  };

  // Simulate scanning a camera QR code (Presets)
  const triggerCameraScan = async (presetType: 'safe' | 'dangerous') => {
    setLoading(true);
    setError(null);
    setSimulatedQRType(presetType);

    // Simulated network delay
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
    <div className="space-y-6 max-w-4xl mx-auto">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">QR Code Scanner</h2>
        <p className="text-muted-foreground">
          Upload or scan any UPI QR code payment invoice to verify recipient credentials and risk profiles.
        </p>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>Scan Error</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div className="grid gap-6 md:grid-cols-2">
        {/* Upload QR File Card */}
        <Card className={`relative overflow-hidden transition-all duration-300 border-2 border-dashed ${dragActive ? 'border-primary bg-primary/5' : 'border-muted'}`}
          onDragEnter={handleDrag}
          onDragOver={handleDrag}
          onDragLeave={handleDrag}
          onDrop={handleDrop}
        >
          <CardHeader className="pb-2">
            <CardTitle>Image Upload</CardTitle>
            <CardDescription>Drag and drop a QR image file or click to select.</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col items-center justify-center p-8 min-h-[260px] text-center">
            {loading && !cameraActive ? (
              <div className="space-y-3">
                <Loader2 className="h-10 w-10 text-primary animate-spin mx-auto" />
                <p className="text-sm font-medium">Extracting and assessing QR data...</p>
              </div>
            ) : (
              <div className="space-y-4">
                <div className="p-4 bg-muted rounded-full w-fit mx-auto shadow-sm">
                  <Upload className="h-8 w-8 text-muted-foreground" />
                </div>
                <div className="space-y-1">
                  <p className="text-sm font-medium text-foreground">
                    Drag and drop file here, or click to upload
                  </p>
                  <p className="text-xs text-muted-foreground">
                    Supports PNG, JPG, JPEG up to 5MB
                  </p>
                </div>
                <Label htmlFor="qr-file-upload" className="cursor-pointer">
                  <Button variant="outline" size="sm" asChild>
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
        <Card className="flex flex-col justify-between">
          <CardHeader>
            <CardTitle>Camera Scanning</CardTitle>
            <CardDescription>Simulate real-time camera scan frame overlay.</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col items-center justify-center flex-grow p-6 text-center">
            {cameraActive ? (
              <div className="relative w-full aspect-video rounded-lg overflow-hidden bg-black flex flex-col items-center justify-center text-white border-2 border-primary">
                {/* Simulated Camera scan lines */}
                <div className="absolute inset-0 bg-gradient-to-b from-transparent via-primary/20 to-transparent animate-pulse pointer-events-none" />
                <div className="absolute left-[10%] right-[10%] top-[10%] bottom-[10%] border border-dashed border-primary/60 flex items-center justify-center">
                  <QrCode className="h-16 w-16 text-primary/80 animate-pulse" />
                </div>

                {loading && simulatedQRType ? (
                  <div className="z-10 bg-black/80 px-4 py-2 rounded-md flex items-center gap-2">
                    <Loader2 className="h-4 w-4 animate-spin text-primary" />
                    <span className="text-xs font-semibold">Scanning Preset...</span>
                  </div>
                ) : (
                  <div className="absolute bottom-4 flex gap-3 z-10">
                    <Button onClick={() => triggerCameraScan('safe')} size="sm" className="bg-green-600 hover:bg-green-700">
                      Scan Safe QR
                    </Button>
                    <Button onClick={() => triggerCameraScan('dangerous')} size="sm" variant="destructive">
                      Scan Dangerous QR
                    </Button>
                  </div>
                )}
              </div>
            ) : (
              <div className="space-y-4">
                <div className="p-4 bg-muted rounded-full w-fit mx-auto shadow-sm">
                  <Camera className="h-8 w-8 text-muted-foreground" />
                </div>
                <p className="text-sm font-medium">Camera scanning mode offline</p>
                <Button onClick={() => setCameraActive(true)} variant="outline" size="sm">
                  Start Camera Scan
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Recent QR Scans History */}
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Recent QR Scans</CardTitle>
          <CardDescription>Payment destinations assessed via QR scans.</CardDescription>
        </CardHeader>
        <CardContent>
          {history.length === 0 ? (
            <p className="text-sm text-muted-foreground text-center py-6">
              No recent QR scans found.
            </p>
          ) : (
            <div className="space-y-3">
              {history.slice(0, 5).map((item, idx) => {
                const statusColors = {
                  LOW: 'bg-green-500/10 text-green-600 border-green-200',
                  MEDIUM: 'bg-amber-500/10 text-amber-600 border-amber-200',
                  HIGH: 'bg-red-500/10 text-red-600 border-red-200',
                  CRITICAL: 'bg-red-500/10 text-red-600 border-red-200',
                };
                return (
                  <div 
                    key={idx}
                    onClick={() => navigate('/verify/result', { state: { type: 'qr', data: { upi: item.upi, merchant: item.merchant, riskScore: item.riskScore, riskLevel: item.riskLevel, recommendation: item.recommendation } } })}
                    className="flex justify-between items-center p-3 rounded-lg border hover:bg-muted/30 transition-colors cursor-pointer"
                  >
                    <div className="flex items-center gap-3">
                      <QrCode className="h-5 w-5 text-muted-foreground shrink-0" />
                      <div>
                        <span className="font-semibold text-sm block">{item.merchant}</span>
                        <span className="text-xs text-muted-foreground font-mono">{item.upi}</span>
                      </div>
                    </div>
                    <Badge variant="outline" className={statusColors[item.riskLevel]}>
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
