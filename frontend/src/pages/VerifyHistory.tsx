import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { Card, CardContent } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableHead, 
  TableHeader, 
  TableRow 
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { 
  History, 
  Search, 
  QrCode, 
  ArrowUpRight 
} from 'lucide-react';
import { reputationService, ReputationHistoryItem } from '@/features/reputation';
import { qrService, QrScanResponse } from '@/features/qr';

export const VerifyHistory: React.FC = () => {
  const [upiHistory, setUpiHistory] = useState<ReputationHistoryItem[]>([]);
  const [qrHistory, setQrHistory] = useState<QrScanResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const loadData = async () => {
    setLoading(true);
    try {
      const upis = await reputationService.getHistory();
      const qrs = await qrService.getHistory();
      setUpiHistory(upis);
      setQrHistory(qrs);
    } catch (err) {
      console.error('Failed to load history lists', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const getStatusBadge = (status: string) => {
    const statusColors: Record<string, string> = {
      TRUSTED: 'bg-green-500/10 text-green-600 border-green-200',
      SAFE: 'bg-emerald-500/10 text-emerald-600 border-emerald-200',
      SUSPICIOUS: 'bg-amber-500/10 text-amber-600 border-amber-200',
      DANGEROUS: 'bg-red-500/10 text-red-600 border-red-200',
      LOW: 'bg-emerald-500/10 text-emerald-600 border-emerald-200',
      MEDIUM: 'bg-amber-500/10 text-amber-600 border-amber-200',
      HIGH: 'bg-red-500/10 text-red-600 border-red-200',
      CRITICAL: 'bg-red-500/10 text-red-600 border-red-200',
    };
    return (
      <Badge variant="outline" className={statusColors[status] || ''}>
        {status}
      </Badge>
    );
  };

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Verification History</h2>
        <p className="text-muted-foreground">
          View past reputation assessments, scanned merchant profiles, and calculated threat ratings.
        </p>
      </div>

      <Card>
        <CardContent className="pt-6">
          <Tabs defaultValue="upi" className="w-full">
            <div className="flex justify-between items-center mb-6">
              <TabsList>
                <TabsTrigger value="upi" className="gap-2">
                  <Search className="h-4 w-4" />
                  UPI Verifications
                </TabsTrigger>
                <TabsTrigger value="qr" className="gap-2">
                  <QrCode className="h-4 w-4" />
                  QR Code Scans
                </TabsTrigger>
              </TabsList>
              
              <Button onClick={loadData} variant="outline" size="sm" disabled={loading}>
                Refresh Logs
              </Button>
            </div>

            {/* UPI Tab */}
            <TabsContent value="upi" className="border-none p-0">
              {upiHistory.length === 0 ? (
                <div className="text-center py-12 text-muted-foreground">
                  <History className="h-12 w-12 mx-auto mb-4 stroke-1" />
                  <p className="font-medium text-sm">No UPI check records found</p>
                  <p className="text-xs">Your UPI checks will appear here once verified.</p>
                </div>
              ) : (
                <div className="border rounded-md">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>UPI VPA Address</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead>Risk Score</TableHead>
                        <TableHead>Verified At</TableHead>
                        <TableHead className="text-right">Action</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {upiHistory.map((item) => (
                        <TableRow key={item.id}>
                          <TableCell className="font-mono font-medium select-all">
                            {item.verifiedEntity}
                          </TableCell>
                          <TableCell>{getStatusBadge(item.status)}</TableCell>
                          <TableCell className="font-bold">{item.riskScore}/100</TableCell>
                          <TableCell>
                            {new Date(item.createdAt).toLocaleString()}
                          </TableCell>
                          <TableCell className="text-right">
                            <Button 
                              onClick={() => navigate('/verify/result', { state: { type: 'upi', data: { upiId: item.verifiedEntity, score: 100 - item.riskScore, status: item.status, reasons: [] } } })}
                              variant="ghost" 
                              size="sm"
                              className="gap-1.5"
                            >
                              Details
                              <ArrowUpRight className="h-3.5 w-3.5" />
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              )}
            </TabsContent>

            {/* QR Tab */}
            <TabsContent value="qr" className="border-none p-0">
              {qrHistory.length === 0 ? (
                <div className="text-center py-12 text-muted-foreground">
                  <QrCode className="h-12 w-12 mx-auto mb-4 stroke-1" />
                  <p className="font-medium text-sm">No QR code scan records found</p>
                  <p className="text-xs">Your scanned QR codes will appear here.</p>
                </div>
              ) : (
                <div className="border rounded-md">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Merchant Name</TableHead>
                        <TableHead>Extracted VPA</TableHead>
                        <TableHead>Risk Level</TableHead>
                        <TableHead>Risk Score</TableHead>
                        <TableHead>Recommendation</TableHead>
                        <TableHead className="text-right">Action</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {qrHistory.map((item, idx) => (
                        <TableRow key={idx}>
                          <TableCell className="font-semibold">{item.merchant}</TableCell>
                          <TableCell className="font-mono text-xs select-all">{item.upi}</TableCell>
                          <TableCell>{getStatusBadge(item.riskLevel)}</TableCell>
                          <TableCell className="font-bold">{item.riskScore}/100</TableCell>
                          <TableCell className="text-sm">{item.recommendation}</TableCell>
                          <TableCell className="text-right">
                            <Button 
                              onClick={() => navigate('/verify/result', { state: { type: 'qr', data: item } })}
                              variant="ghost" 
                              size="sm"
                              className="gap-1.5"
                            >
                              Details
                              <ArrowUpRight className="h-3.5 w-3.5" />
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              )}
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
};

export default VerifyHistory;
