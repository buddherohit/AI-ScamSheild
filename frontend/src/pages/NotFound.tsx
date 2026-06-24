import React from 'react';
import { Box, Button, Typography, Container } from '@mui/material';
import { Link } from 'react-router';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';

export const NotFound: React.FC = () => {
  return (
    <Container maxWidth="md">
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '70vh',
          textAlign: 'center',
          gap: 2,
        }}
      >
        <ErrorOutlineIcon sx={{ fontSize: 80, color: 'text.secondary' }} />
        <Typography variant="h3" sx={{ fontWeight: 800 }}>
          404 - Page Not Found
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
          The security resource or dashboard screen you requested does not exist or has been relocated.
        </Typography>
        <Button variant="contained" component={Link} to="/" sx={{ height: 44 }}>
          Return to Safe Zone
        </Button>
      </Box>
    </Container>
  );
};

export default NotFound;
