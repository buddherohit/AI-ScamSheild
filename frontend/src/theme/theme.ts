import { createTheme, ThemeOptions } from '@mui/material/styles';

// ==============================================================================
// Global Design Tokens
// ==============================================================================
const typographyOptions = {
  fontFamily: '"Outfit", "Inter", "Roboto", "Helvetica", "Arial", sans-serif',
  h1: {
    fontSize: '2.5rem',
    fontWeight: 700,
    lineHeight: 1.2,
    letterSpacing: '-0.02em',
  },
  h2: {
    fontSize: '2rem',
    fontWeight: 700,
    lineHeight: 1.3,
    letterSpacing: '-0.018em',
  },
  h3: {
    fontSize: '1.75rem',
    fontWeight: 600,
    lineHeight: 1.3,
    letterSpacing: '-0.01em',
  },
  h4: {
    fontSize: '1.5rem',
    fontWeight: 600,
    lineHeight: 1.4,
  },
  h5: {
    fontSize: '1.25rem',
    fontWeight: 600,
    lineHeight: 1.4,
  },
  h6: {
    fontSize: '1rem',
    fontWeight: 600,
    lineHeight: 1.4,
  },
  body1: {
    fontSize: '1rem',
    lineHeight: 1.5,
  },
  body2: {
    fontSize: '0.875rem',
    lineHeight: 1.5,
  },
  button: {
    textTransform: 'none' as const,
    fontWeight: 600,
  },
};

// ==============================================================================
// Light Theme Palette
// ==============================================================================
const lightThemeOptions: ThemeOptions = {
  palette: {
    mode: 'light',
    primary: {
      main: '#0F172A', // Slate 900
      light: '#334155', // Slate 700
      dark: '#020617', // Slate 950
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: '#6366F1', // Indigo 500
      light: '#818CF8', // Indigo 400
      dark: '#4F46E5', // Indigo 600
      contrastText: '#FFFFFF',
    },
    error: {
      main: '#EF4444', // Red 500
      light: '#F87171',
      dark: '#DC2626',
    },
    warning: {
      main: '#F59E0B', // Amber 500
      light: '#FBBF24',
      dark: '#D97706',
    },
    info: {
      main: '#3B82F6', // Blue 500
      light: '#60A5FA',
      dark: '#2563EB',
    },
    success: {
      main: '#10B981', // Emerald 500
      light: '#34D399',
      dark: '#059669',
    },
    background: {
      default: '#F8FAFC', // Slate 50
      paper: '#FFFFFF',
    },
    text: {
      primary: '#0F172A', // Slate 900
      secondary: '#475569', // Slate 600
      disabled: '#94A3B8',
    },
    divider: '#E2E8F0', // Slate 200
  },
  typography: typographyOptions,
  shape: {
    borderRadius: 12,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          boxShadow: 'none',
          '&:hover': {
            boxShadow: 'none',
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
      },
    },
  },
};

// ==============================================================================
// Dark Theme Palette (Sleek Glassmorphic Vibes)
// ==============================================================================
const darkThemeOptions: ThemeOptions = {
  palette: {
    mode: 'dark',
    primary: {
      main: '#F8FAFC', // Slate 50
      light: '#FFFFFF',
      dark: '#CBD5E1', // Slate 300
      contrastText: '#020617',
    },
    secondary: {
      main: '#818CF8', // Indigo 400
      light: '#A5B4FC',
      dark: '#6366F1',
      contrastText: '#020617',
    },
    error: {
      main: '#F87171', // Red 400
      light: '#FCA5A5',
      dark: '#EF4444',
    },
    warning: {
      main: '#FBBF24', // Amber 400
      light: '#FDE047',
      dark: '#F59E0B',
    },
    info: {
      main: '#60A5FA', // Blue 400
      light: '#93C5FD',
      dark: '#3B82F6',
    },
    success: {
      main: '#34D399', // Emerald 400
      light: '#6EE7B7',
      dark: '#10B981',
    },
    background: {
      default: '#020617', // Slate 950
      paper: '#0B1329', // Deep Slate
    },
    text: {
      primary: '#F8FAFC', // Slate 50
      secondary: '#94A3B8', // Slate 400
      disabled: '#64748B',
    },
    divider: '#1E293B', // Slate 800
  },
  typography: typographyOptions,
  shape: {
    borderRadius: 12,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          boxShadow: 'none',
          '&:hover': {
            boxShadow: 'none',
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          border: '1px solid #1E293B',
        },
      },
    },
  },
};

export const lightTheme = createTheme(lightThemeOptions);
export const darkTheme = createTheme(darkThemeOptions);
