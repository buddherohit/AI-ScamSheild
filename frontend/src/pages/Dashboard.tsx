import React, { useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { 
  Shield, 
  ShieldAlert, 
  Activity, 
  ArrowUpRight, 
  QrCode, 
  Search, 
  MessageSquare, 
  TrendingUp, 
  AlertTriangle, 
  CheckCircle2, 
  Info
} from 'lucide-react';
import {
  Area,
  AreaChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
  CartesianGrid
} from 'recharts';
import { useNavigate } from 'react-router';
import { useAppDispatch, useAppSelector } from '@/store';
import {
  setDashboardDataStart,
  setDashboardDataSuccess,
  setDashboardDataFailure
} from '@/store/slices/dashboardSlice';
import { dashboardService } from '@/services/dashboardService';

const defaultThreatData = [
  { name: 'Mon', threats: 12 },
  { name: 'Tue', threats: 19 },
  { name: 'Wed', threats: 15 },
  { name: 'Thu', threats: 28 },
  { name: 'Fri', threats: 22 },
  { name: 'Sat', threats: 40 },
  { name: 'Sun', threats: 32 },
];

const defaultActivities = [
  { id: 'act-1', timestamp: '10 mins ago', type: 'alert', severity: 'high', description: 'Suspicious payment link blocked.', status: 'blocked' },
  { id: 'act-2', timestamp: '2 hours ago', type: 'info', severity: 'low', description: 'New login from Chrome on Windows.', status: 'flagged' },
  { id: 'act-3', timestamp: 'Yesterday', type: 'alert', severity: 'high', description: 'Phishing SMS detected and flagged.', status: 'flagged' },
  { id: 'act-4', timestamp: 'Yesterday', type: 'success', severity: 'low', description: 'Password changed successfully.', status: 'investigating' },
];

export const Dashboard: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { metrics, recentActivity, threatTrends } = useAppSelector((state) => state.dashboard);

  useEffect(() => {
    const loadDashboardData = async () => {
      dispatch(setDashboardDataStart());
      try {
        const response = await dashboardService.getDashboardData();
        const apiResponse = response as any;
        if (apiResponse && apiResponse.success && apiResponse.data) {
          dispatch(setDashboardDataSuccess(apiResponse.data));
        } else if (response && (response as any).metrics) {
          dispatch(setDashboardDataSuccess(response as any));
        }
      } catch (err: any) {
        dispatch(setDashboardDataFailure(err.message || 'Failed to load dashboard metrics'));
      }
    };

    loadDashboardData();
  }, [dispatch]);

  const chartData = threatTrends && threatTrends.length > 0 ? threatTrends : defaultThreatData;
  const activitiesList = recentActivity && recentActivity.length > 0 ? recentActivity : defaultActivities;

  const getActivityIcon = (type: string, severity: string) => {
    if (severity === 'critical' || severity === 'high') {
      return <ShieldAlert className="h-4 w-4 text-destructive" />;
    }
    switch (type) {
      case 'alert':
        return <AlertTriangle className="h-4 w-4 text-amber-500" />;
      case 'success':
        return <CheckCircle2 className="h-4 w-4 text-emerald-500" />;
      case 'info':
      default:
        return <Info className="h-4 w-4 text-blue-500" />;
    }
  };

  return (
    <div className="space-y-8">
      {/* Header section */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h2 className="text-3xl font-extrabold tracking-tight bg-gradient-to-r from-foreground to-foreground/75 bg-clip-text text-transparent">
            Security Overview
          </h2>
          <p className="text-sm text-muted-foreground mt-1">
            Real-time digital asset protection and transaction safety insights.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button onClick={() => navigate('/sms/insights')} variant="outline" size="sm" className="gap-2 hover:bg-muted transition-colors">
            <TrendingUp className="h-4 w-4 text-primary" />
            Threat Insights
          </Button>
        </div>
      </div>

      {/* Metrics Grid */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {/* Metric 1 */}
        <Card className="relative overflow-hidden transition-all duration-300 hover:-translate-y-1 hover:shadow-md hover:border-emerald-500/20 group">
          <div className="absolute top-0 left-0 w-1.5 h-full bg-emerald-500" />
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
              Security Health
            </CardTitle>
            <Shield className="h-5 w-5 text-emerald-500 group-hover:scale-110 transition-transform" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-extrabold text-emerald-500">{metrics.securityScore}%</div>
            <p className="text-xs text-muted-foreground mt-1.5 font-medium">
              Optimal system configuration
            </p>
          </CardContent>
        </Card>

        {/* Metric 2 */}
        <Card className="relative overflow-hidden transition-all duration-300 hover:-translate-y-1 hover:shadow-md hover:border-destructive/20 group">
          <div className="absolute top-0 left-0 w-1.5 h-full bg-destructive" />
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
              Threats Blocked
            </CardTitle>
            <ShieldAlert className="h-5 w-5 text-destructive group-hover:scale-110 transition-transform" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-extrabold text-destructive">{metrics.totalThreatsBlocked}</div>
            <p className="text-xs text-muted-foreground mt-1.5 font-medium">
              Flagged scam vector pretexts
            </p>
          </CardContent>
        </Card>

        {/* Metric 3 */}
        <Card className="relative overflow-hidden transition-all duration-300 hover:-translate-y-1 hover:shadow-md hover:border-amber-500/20 group">
          <div className="absolute top-0 left-0 w-1.5 h-full bg-amber-500" />
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
              Active Alerts
            </CardTitle>
            <Activity className="h-5 w-5 text-amber-500 group-hover:scale-110 transition-transform" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-extrabold text-amber-500">{metrics.activeAlerts}</div>
            <p className="text-xs text-muted-foreground mt-1.5 font-medium">
              High risk nodes identified
            </p>
          </CardContent>
        </Card>

        {/* Metric 4 */}
        <Card className="relative overflow-hidden transition-all duration-300 hover:-translate-y-1 hover:shadow-md hover:border-primary/20 group">
          <div className="absolute top-0 left-0 w-1.5 h-full bg-primary" />
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
              Scanned Items
            </CardTitle>
            <ArrowUpRight className="h-5 w-5 text-primary group-hover:scale-110 transition-transform" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-extrabold text-primary">
              {metrics.scannedTransactions.toLocaleString()}
            </div>
            <p className="text-xs text-muted-foreground mt-1.5 font-medium">
              Checked links, scans & VPAs
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Main Grid: Charts & Actions */}
      <div className="grid gap-6 lg:grid-cols-7">
        {/* Threat Overview chart */}
        <Card className="lg:col-span-4 border border-border/40 shadow-sm">
          <CardHeader className="border-b bg-muted/10 pb-4">
            <CardTitle className="text-base font-bold">Threat Mitigation Trends</CardTitle>
            <CardDescription className="text-xs">
              Daily frequency of blocked fraud vectors over the last week.
            </CardDescription>
          </CardHeader>
          <CardContent className="pt-6 pl-0">
            <div className="h-[300px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData} margin={{ left: 10, right: 10, top: 0, bottom: 0 }}>
                  <defs>
                    <linearGradient id="threatGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="hsl(var(--primary))" stopOpacity={0.25}/>
                      <stop offset="95%" stopColor="hsl(var(--primary))" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(128,128,128,0.15)" />
                  <XAxis 
                    dataKey="name" 
                    stroke="currentColor" 
                    className="text-muted-foreground text-[10px]"
                    tickLine={false} 
                    axisLine={false} 
                    dy={10}
                  />
                  <YAxis 
                    stroke="currentColor" 
                    className="text-muted-foreground text-[10px]"
                    tickLine={false} 
                    axisLine={false} 
                    dx={-5}
                  />
                  <Tooltip 
                    contentStyle={{ 
                      borderRadius: '8px', 
                      backgroundColor: 'hsl(var(--card))', 
                      border: '1px solid hsl(var(--border))', 
                      boxShadow: '0 4px 12px rgba(0,0,0,0.08)' 
                    }} 
                  />
                  <Area 
                    type="monotone" 
                    dataKey="threats" 
                    stroke="hsl(var(--primary))" 
                    strokeWidth={2}
                    fillOpacity={1} 
                    fill="url(#threatGrad)" 
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>

        {/* Quick Launch & Activities Panel */}
        <div className="lg:col-span-3 space-y-6">
          {/* Quick Actions Card */}
          <Card className="border border-border/40 shadow-sm">
            <CardHeader className="border-b bg-muted/10 pb-4">
              <CardTitle className="text-base font-bold">Quick Actions Launchpad</CardTitle>
            </CardHeader>
            <CardContent className="pt-4 grid grid-cols-2 gap-3">
              <Button 
                onClick={() => navigate('/qr')} 
                variant="outline" 
                className="flex flex-col items-center justify-center h-24 gap-2.5 rounded-lg border-muted/70 hover:border-primary/30 hover:bg-primary/5 transition-all text-xs font-semibold"
              >
                <QrCode className="h-5 w-5 text-primary" />
                Scan QR Code
              </Button>
              <Button 
                onClick={() => navigate('/upi')} 
                variant="outline" 
                className="flex flex-col items-center justify-center h-24 gap-2.5 rounded-lg border-muted/70 hover:border-primary/30 hover:bg-primary/5 transition-all text-xs font-semibold"
              >
                <Search className="h-5 w-5 text-primary" />
                Verify UPI ID
              </Button>
              <Button 
                onClick={() => navigate('/sms')} 
                variant="outline" 
                className="flex flex-col items-center justify-center h-24 gap-2.5 rounded-lg border-muted/70 hover:border-primary/30 hover:bg-primary/5 transition-all text-xs font-semibold"
              >
                <MessageSquare className="h-5 w-5 text-primary" />
                Scan SMS Text
              </Button>
              <Button 
                onClick={() => navigate('/verify/history')} 
                variant="outline" 
                className="flex flex-col items-center justify-center h-24 gap-2.5 rounded-lg border-muted/70 hover:border-primary/30 hover:bg-primary/5 transition-all text-xs font-semibold"
              >
                <Activity className="h-5 w-5 text-primary" />
                Audit History
              </Button>
            </CardContent>
          </Card>

          {/* Recent Activity Card */}
          <Card className="border border-border/40 shadow-sm">
            <CardHeader className="border-b bg-muted/10 pb-4">
              <CardTitle className="text-base font-bold">Activity Feed</CardTitle>
            </CardHeader>
            <CardContent className="pt-4">
              <div className="relative pl-6 border-l border-muted space-y-5">
                {activitiesList.map((activity, i) => (
                  <div key={activity.id || i} className="relative group">
                    {/* Activity Bullet Icon */}
                    <div className="absolute -left-[35px] top-1 bg-background border rounded-full p-1.5 flex items-center justify-center shadow-sm group-hover:scale-110 transition-transform">
                      {getActivityIcon(activity.type, activity.severity)}
                    </div>
                    
                    <div className="space-y-0.5">
                      <div className="flex items-center justify-between">
                        <p className="text-xs font-bold text-foreground truncate max-w-[160px] md:max-w-[200px]">
                          {activity.description}
                        </p>
                        <span className="text-[10px] text-muted-foreground whitespace-nowrap">
                          {activity.timestamp}
                        </span>
                      </div>
                      
                      {/* Sub-label severity */}
                      <span className={`inline-flex items-center text-[9px] font-bold px-1.5 py-0.5 rounded-full uppercase tracking-wider ${
                        activity.severity === 'critical' || activity.severity === 'high'
                          ? 'bg-destructive/10 text-destructive'
                          : 'bg-muted text-muted-foreground'
                      }`}>
                        {activity.severity}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
