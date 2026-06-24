import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface UserSession {
  id: number;
  deviceName: string;
  ipAddress: string;
  userAgent: string;
  lastActivity: string;
  createdAt: string;
  isCurrentSession: boolean;
}

interface SessionState {
  sessions: UserSession[];
  loading: boolean;
  error: string | null;
}

const initialState: SessionState = {
  sessions: [],
  loading: false,
  error: null,
};

const sessionSlice = createSlice({
  name: 'session',
  initialState,
  reducers: {
    setSessionsStart(state) {
      state.loading = true;
      state.error = null;
    },
    setSessionsSuccess(state, action: PayloadAction<UserSession[]>) {
      state.sessions = action.payload;
      state.loading = false;
    },
    setSessionsFailure(state, action: PayloadAction<string>) {
      state.loading = false;
      state.error = action.payload;
    },
    removeSession(state, action: PayloadAction<number>) {
      state.sessions = state.sessions.filter(s => s.id !== action.payload);
    }
  },
});

export const {
  setSessionsStart,
  setSessionsSuccess,
  setSessionsFailure,
  removeSession
} = sessionSlice.actions;

export default sessionSlice.reducer;
