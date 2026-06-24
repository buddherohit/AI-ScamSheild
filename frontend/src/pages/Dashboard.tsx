import React, { useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Shield, ShieldAlert, Activity, ArrowUpRight } from 'lucide-react';
import {
  Area,
  AreaChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
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
  const { metrics, recentActivity, threatTrends } = useAppSelector((state) => state.dashboard);

  useEffect(() => {
    const loadDashboardData = async () => {
      dispatch(setDashboardDataStart());
      try {
        const response = await dashboardService.getDashboardData();
        // The API returns the response wrapped in ApiResponse structure
        const apiResponse = response as any;
        if (apiResponse && apiResponse.success && apiResponse.data) {
          dispatch(setDashboardDataSuccess(apiResponse.data));
        } else if (response && (response as any).metrics) {
          // Fallback if data is returned directly without envelope
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

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Dashboard</h2>
        <p className="text-muted-foreground">
          Overview of your account security and recent scam prevention metrics.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Security Score</CardTitle>
            <Shield className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{metrics.securityScore}/100</div>
            <p className="text-xs text-muted-foreground">
              Optimal system health
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Threats Blocked</CardTitle>
            <ShieldAlert className="h-4 w-4 text-destructive" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{metrics.totalThreatsBlocked}</div>
            <p className="text-xs text-muted-foreground">
              Total threats flagged in platform
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Alerts</CardTitle>
            <Activity className="h-4 w-4 text-amber-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{metrics.activeAlerts}</div>
            <p className="text-xs text-muted-foreground">
              Critical or High risk entities
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Scanned Transactions</CardTitle>
            <ArrowUpRight className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {metrics.scannedTransactions.toLocaleString()}
            </div>
            <p className="text-xs text-muted-foreground">
              Since system deployment
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
        <Card className="lg:col-span-4">
          <CardHeader>
            <CardTitle>Threat Overview</CardTitle>
            <CardDescription>
              Volume of blocked scams over the last 7 days.
            </CardDescription>
          </CardHeader>
          <CardContent className="pl-2">
            <div className="h-[300px] w-full mt-4">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData}>
                  <defs>
                    <linearGradient id="colorThreats" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="hsl(var(--destructive))" stopOpacity={0.3}/>
                      <stop offset="95%" stopColor="hsl(var(--destructive))" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <XAxis dataKey="name" stroke="#888888" fontSize={12} tickLine={false} axisLine={false} />
                  <YAxis stroke="#888888" fontSize={12} tickLine={false} axisLine={false} />
                  <Tooltip contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }} />
                  <Area type="monotone" dataKey="threats" stroke="hsl(var(--destructive))" fillOpacity={1} fill="url(#colorThreats)" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>

        <Card className="lg:col-span-3">
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
            <CardDescription>
              Latest security events on the network.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-8">
              {activitiesList.map((activity, i) => (
                <div key={activity.id || i} className="flex items-center">
                  <div className={`mt-1 h-2 w-2 rounded-full mr-4 ${activity.severity === 'critical' || activity.severity === 'high' ? 'bg-destructive' : 'bg-primary'}`} />
                  <div className="space-y-1">
                    <p className="text-sm font-medium leading-none">{activity.description}</p>
                    <p className="text-xs text-muted-foreground">{activity.timestamp}</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
