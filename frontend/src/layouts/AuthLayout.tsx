import React from 'react';
import { Container, Box, Paper, Typography, useTheme } from '@mui/material';
import ShieldIcon from '@mui/icons-material/Shield';

interface AuthLayoutProps {
  children: React.ReactNode;
}

export const AuthLayout: React.FC<AuthLayoutProps> = ({ children }) => {
  const theme = useTheme();

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background:
          theme.palette.mode === 'light'
            ? 'radial-gradient(circle at 50% 50%, #F1F5F9 0%, #E2E8F0 100%)'
            : 'radial-gradient(circle at 50% 50%, #0F172A 0%, #020617 100%)',
        py: 4,
      }}
    >
      <Container maxWidth="xs">
        <Paper
          elevation={theme.palette.mode === 'light' ? 2 : 0}
          sx={{
            p: 4,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            borderRadius: 4,
            backdropFilter: 'blur(8px)',
            background:
              theme.palette.mode === 'light'
                ? 'rgba(255, 255, 255, 0.9)'
                : 'rgba(11, 19, 41, 0.8)',
            border: `1px solid ${
              theme.palette.mode === 'light' ? 'rgba(226, 232, 240, 0.8)' : 'rgba(30, 41, 59, 0.8)'
            }`,
          }}
        >
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              mb: 3,
              gap: 1,
            }}
          >
            <ShieldIcon sx={{ fontSize: 32, color: theme.palette.secondary.main }} />
            <Typography
              variant="h5"
              component="h1"
              sx={{
                fontWeight: 800,
                letterSpacing: '-0.02em',
                background: `linear-gradient(45deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`,
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
              }}
            >
              ScamShield
            </Typography>
          </Box>
          {children}
        </Paper>
      </Container>
    </Box>
  );
};

export default AuthLayout;
