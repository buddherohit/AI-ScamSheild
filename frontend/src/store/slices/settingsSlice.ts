import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface SettingsState {
  language: string;
  emailNotifications: boolean;
  smsNotifications: boolean;
  twoFactorEnabled: boolean;
  dataSharing: boolean;
}

const initialState: SettingsState = {
  language: 'en',
  emailNotifications: true,
  smsNotifications: false,
  twoFactorEnabled: false,
  dataSharing: false,
};

const settingsSlice = createSlice({
  name: 'settings',
  initialState,
  reducers: {
    updateSettings(state, action: PayloadAction<Partial<SettingsState>>) {
      return { ...state, ...action.payload };
    },
    resetSettings() {
      return initialState;
    },
  },
});

export const { updateSettings, resetSettings } = settingsSlice.actions;
export default settingsSlice.reducer;
