import React from 'react';
import { Grid, Paper, Typography, Box, useTheme, Card, CardContent } from '@mui/material';
import ShieldIcon from '@mui/icons-material/Shield';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import PeopleIcon from '@mui/icons-material/People';
import SpeedIcon from '@mui/icons-material/Speed';

export const Dashboard: React.FC = () => {
  const theme = useTheme();

  // Mock dashboard cards telemetry data
  const telemetryData = [
    {
      title: 'Scams Intercepted',
      value: '2,845',
      change: '+14% from last week',
      icon: <ShieldIcon fontSize="large" color="error" />,
      color: theme.palette.error.main,
    },
    {
      title: 'Accounts Protected',
      value: '14,208',
      change: '+8.2% monthly growth',
      icon: <PeopleIcon fontSize="large" color="secondary" />,
      color: theme.palette.secondary.main,
    },
    {
      title: 'Transaction Scan Speed',
      value: '18ms',
      change: 'Ultra-low latency',
      icon: <SpeedIcon fontSize="large" color="success" />,
      color: theme.palette.success.main,
    },
    {
      title: 'Detection Accuracy',
      value: '99.4%',
      change: 'AI Model v1.2 Active',
      icon: <TrendingUpIcon fontSize="large" color="info" />,
      color: theme.palette.info.main,
    },
  ];

  return (
    <Box sx={{ width: '100%' }}>
      <Typography variant="h4" gutterBottom sx={{ fontWeight: 800, mb: 1 }}>
        Enterprise Security Telemetry
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Real-time transaction interception metrics and fraud detection platform status.
      </Typography>

      <Grid container spacing={3} sx={{ mb: 4 }}>
        {telemetryData.map((card) => (
          <Grid item xs={12} sm={6} md={3} key={card.title}>
            <Card
              sx={{
                height: '100%',
                background:
                  theme.palette.mode === 'light'
                    ? '#FFFFFF'
                    : 'linear-gradient(135deg, #0B1329 0%, #152238 100%)',
                borderLeft: `5px solid ${card.color}`,
              }}
            >
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                  <Typography variant="h6" color="text.secondary" sx={{ fontSize: '0.875rem', fontWeight: 700 }}>
                    {card.title}
                  </Typography>
                  {card.icon}
                </Box>
                <Typography variant="h4" sx={{ fontWeight: 800, mb: 1 }}>
                  {card.value}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {card.change}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3, height: 350, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
            <Typography variant="h6" color="text.secondary" gutterBottom sx={{ fontWeight: 700 }}>
              Live Threat Interception Flow
            </Typography>
            <Typography variant="body2" color="text.secondary">
              [Phase 2: Interactive Threat Map and Detection Feed Component]
            </Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: 350, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
            <Typography variant="h6" color="text.secondary" gutterBottom sx={{ fontWeight: 700 }}>
              Channel Flag Distribution
            </Typography>
            <Typography variant="body2" color="text.secondary">
              [Phase 2: UPI, SMS, QR Code, and Web-Link detection ratios charts]
            </Typography>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
