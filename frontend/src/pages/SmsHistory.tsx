import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { Card, CardContent } from '@/components/ui/card';
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
import { Input } from '@/components/ui/input';
import { 
  Search, 
  History, 
  Trash2, 
  ArrowUpRight, 
  ChevronLeft, 
  ChevronRight, 
  Loader2
} from 'lucide-react';
import { aiService, SmsAnalysisResponse } from '@/features/ai';

export const SmsHistory: React.FC = () => {
  const [historyList, setHistoryList] = useState<SmsAnalysisResponse[]>([]);
  const [search, setSearch] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const loadHistory = async () => {
    setLoading(true);
    try {
      const response = await aiService.getHistory(search, currentPage, 10);
      setHistoryList(response.content || []);
      setTotalPages(response.totalPages || 0);
    } catch (err) {
      console.error('Failed to load history list', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHistory();
  }, [currentPage]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(0);
    loadHistory();
  };

  const handleDelete = async (id: number, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!window.confirm('Are you sure you want to delete this analysis report?')) return;
    try {
      await aiService.deleteAnalysis(id);
      loadHistory();
    } catch (err) {
      console.error('Delete failed', err);
    }
  };

  const getRiskLevelBadge = (level: string) => {
    const colors: Record<string, string> = {
      LOW: 'bg-green-500/10 text-green-600 border-green-200',
      MEDIUM: 'bg-amber-500/10 text-amber-600 border-amber-200',
      HIGH: 'bg-red-500/10 text-red-600 border-red-200',
      CRITICAL: 'bg-red-500/10 text-red-600 border-red-200 border-2 font-bold animate-pulse',
    };
    return (
      <Badge variant="outline" className={colors[level] || ''}>
        {level}
      </Badge>
    );
  };

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Analysis History</h2>
          <p className="text-muted-foreground">
            Logs of all completed SMS scam intelligence scans and threat assessments.
          </p>
        </div>
        <Button onClick={() => navigate('/sms')} variant="outline" size="sm">
          New Scan
        </Button>
      </div>

      <Card>
        <CardContent className="pt-6 space-y-4">
          {/* Search bar */}
          <form onSubmit={handleSearch} className="flex gap-2">
            <div className="relative flex-grow">
              <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search SMS logs by preview contents or category..."
                className="pl-9"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <Button type="submit" variant="default" disabled={loading}>
              Search
            </Button>
          </form>

          {/* Table */}
          {loading ? (
            <div className="flex justify-center py-12">
              <Loader2 className="h-8 w-8 text-primary animate-spin" />
            </div>
          ) : historyList.length === 0 ? (
            <div className="text-center py-12 text-muted-foreground">
              <History className="h-12 w-12 mx-auto mb-4 stroke-1" />
              <p className="font-medium text-sm">No analysis history found</p>
              <p className="text-xs">Submit SMS logs under the scanner page to check for scams.</p>
            </div>
          ) : (
            <div className="border rounded-md overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="min-w-[200px]">SMS Content Preview</TableHead>
                    <TableHead>Category</TableHead>
                    <TableHead>Risk Level</TableHead>
                    <TableHead>Threat Score</TableHead>
                    <TableHead>Date Analyzed</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {historyList.map((item) => (
                    <TableRow key={item.id} className="hover:bg-muted/10">
                      <TableCell className="max-w-[320px] truncate font-medium text-sm">
                        {item.smsText}
                      </TableCell>
                      <TableCell>
                        <Badge variant="secondary">{item.category}</Badge>
                      </TableCell>
                      <TableCell>{getRiskLevelBadge(item.riskLevel)}</TableCell>
                      <TableCell className="font-bold">{item.riskScore}/100</TableCell>
                      <TableCell className="text-xs text-muted-foreground">
                        {new Date(item.createdAt).toLocaleString()}
                      </TableCell>
                      <TableCell className="text-right flex items-center justify-end gap-1.5">
                        <Button
                          onClick={() => navigate(`/sms/analysis/${item.id}`)}
                          variant="ghost"
                          size="sm"
                          className="gap-1"
                        >
                          View
                          <ArrowUpRight className="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          onClick={(e) => handleDelete(item.id, e)}
                          variant="ghost"
                          size="icon"
                          className="text-destructive hover:bg-destructive/10 hover:text-destructive h-8 w-8"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between border-t pt-4">
              <span className="text-xs text-muted-foreground">
                Page {currentPage + 1} of {totalPages}
              </span>
              <div className="flex gap-2">
                <Button
                  onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                  disabled={currentPage === 0 || loading}
                  variant="outline"
                  size="icon"
                  className="h-8 w-8"
                >
                  <ChevronLeft className="h-4 w-4" />
                </Button>
                <Button
                  onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
                  disabled={currentPage === totalPages - 1 || loading}
                  variant="outline"
                  size="icon"
                  className="h-8 w-8"
                >
                  <ChevronRight className="h-4 w-4" />
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default SmsHistory;
