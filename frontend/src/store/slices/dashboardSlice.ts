import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface ThreatActivity {
  id: string;
  timestamp: string;
  type: string;
  severity: 'low' | 'medium' | 'high' | 'critical';
  description: string;
  status: 'blocked' | 'flagged' | 'investigating';
}

export interface DashboardMetrics {
  securityScore: number;
  totalThreatsBlocked: number;
  activeAlerts: number;
  scannedTransactions: number;
}

interface DashboardState {
  metrics: DashboardMetrics;
  recentActivity: ThreatActivity[];
  threatTrends: Array<{ name: string; threats: number }>;
  loading: boolean;
  error: string | null;
}

const initialState: DashboardState = {
  metrics: {
    securityScore: 100,
    totalThreatsBlocked: 0,
    activeAlerts: 0,
    scannedTransactions: 0,
  },
  recentActivity: [],
  threatTrends: [],
  loading: false,
  error: null,
};

const dashboardSlice = createSlice({
  name: 'dashboard',
  initialState,
  reducers: {
    setDashboardDataStart(state) {
      state.loading = true;
      state.error = null;
    },
    setDashboardDataSuccess(
      state,
      action: PayloadAction<{
        metrics: DashboardMetrics;
        activity: ThreatActivity[];
        threatTrends?: Array<{ name: string; threats: number }>;
      }>
    ) {
      state.metrics = action.payload.metrics;
      state.recentActivity = action.payload.activity;
      if (action.payload.threatTrends) {
        state.threatTrends = action.payload.threatTrends;
      } else {
        state.threatTrends = [];
      }
      state.loading = false;
    },
    setDashboardDataFailure(state, action: PayloadAction<string>) {
      state.loading = false;
      state.error = action.payload;
    }
  },
});

export const {
  setDashboardDataStart,
  setDashboardDataSuccess,
  setDashboardDataFailure
} = dashboardSlice.actions;

export default dashboardSlice.reducer;
