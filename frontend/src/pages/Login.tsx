import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Box, Button, TextField, Typography, CircularProgress, Link } from '@mui/material';
import { useAppDispatch, useAppSelector } from '@/store';
import { loginStart, loginSuccess, loginFailure } from '@/store/slices/authSlice';
import { showNotification } from '@/store/slices/uiSlice';

// Zod Login Validation Schema
const loginSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters long'),
});

type LoginFormInput = z.infer<typeof loginSchema>;

export const Login: React.FC = () => {
  const dispatch = useAppDispatch();
  const { loading } = useAppSelector((state) => state.auth);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormInput>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const onSubmit = async (data: LoginFormInput) => {
    dispatch(loginStart());

    // Simulate login API call response
    setTimeout(() => {
      if (data.email === 'admin@scamshield.com' && data.password === 'password123') {
        dispatch(
          loginSuccess({
            token: 'mock-jwt-access-token-xyz-123',
            refreshToken: 'mock-jwt-refresh-token-xyz-987',
            user: {
              id: 'usr_01HGPX4D',
              email: data.email,
              name: 'Enterprise Admin',
              roles: ['ROLE_ADMIN'],
            },
          })
        );
        dispatch(
          showNotification({
            message: 'Signed in successfully! Welcome to ScamShield.',
            severity: 'success',
          })
        );
      } else {
        dispatch(loginFailure('Invalid email or password.'));
        dispatch(
          showNotification({
            message: 'Authentication failed. Please verify credentials.',
            severity: 'error',
          })
        );
      }
    }, 1000);
  };

  return (
    <Box sx={{ width: '100%' }}>
      <Typography variant="h5" align="center" gutterBottom sx={{ fontWeight: 700 }}>
        Sign In
      </Typography>
      <Typography variant="body2" color="text.secondary" align="center" sx={{ mb: 3 }}>
        Access your ScamShield dashboard configuration
      </Typography>

      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <TextField
          margin="normal"
          fullWidth
          id="email"
          label="Email Address"
          autoComplete="email"
          autoFocus
          error={!!errors.email}
          helperText={errors.email?.message}
          disabled={loading}
          {...register('email')}
        />

        <TextField
          margin="normal"
          fullWidth
          label="Password"
          type="password"
          id="password"
          autoComplete="current-password"
          error={!!errors.password}
          helperText={errors.password?.message}
          disabled={loading}
          {...register('password')}
        />

        <Button
          type="submit"
          fullWidth
          variant="contained"
          color="primary"
          disabled={loading}
          sx={{ mt: 3, mb: 2, height: 44 }}
        >
          {loading ? <CircularProgress size={24} color="inherit" /> : 'Sign In'}
        </Button>

        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 1 }}>
          <Typography variant="body2" color="text.secondary">
            Demo credentials: <code style={{ fontWeight: 'bold' }}>admin@scamshield.com / password123</code>
          </Typography>
        </Box>
      </form>
    </Box>
  );
};

export default Login;
