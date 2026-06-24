import React from 'react';
import { BrowserRouter } from 'react-router';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { useAppSelector } from '@/store';
import { lightTheme, darkTheme } from '@/theme/theme';
import AppRoutes from '@/routes';

export const App: React.FC = () => {
  const { themeMode } = useAppSelector((state) => state.ui);
  const activeTheme = themeMode === 'light' ? lightTheme : darkTheme;

  return (
    <ThemeProvider theme={activeTheme}>
      <CssBaseline />
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </ThemeProvider>
  );
};

export default App;
