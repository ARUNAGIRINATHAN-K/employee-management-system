import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Card, CardContent, TextField, Button, Typography,
  Alert, InputAdornment, IconButton, CircularProgress, Divider,
} from '@mui/material';
import PersonOutlineRoundedIcon   from '@mui/icons-material/PersonOutlineRounded';
import LockOutlineRoundedIcon     from '@mui/icons-material/LockOutlineRounded';
import VisibilityRounded          from '@mui/icons-material/VisibilityRounded';
import VisibilityOffRounded       from '@mui/icons-material/VisibilityOffRounded';
import { useAuth } from '../../context/AuthContext';
import type { AxiosError } from 'axios';
import type { ApiError } from '../../types';

const Login = () => {
  const { login, isAdmin, isHR, isManager } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPwd, setShowPwd]   = useState(false);
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !password) { setError('All fields are required'); return; }
    setError(null);
    setLoading(true);
    try {
      await login({ username, password });
      if (isAdmin() || isHR() || isManager()) {
        navigate('/dashboard', { replace: true });
      } else {
        navigate('/profile', { replace: true });
      }
    } catch (err) {
      const axiosErr = err as AxiosError<ApiError>;
      setError(axiosErr.response?.data?.message ?? 'Invalid username or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: '#F8F9FA',
        p: 2,
      }}
    >
      <Card
        elevation={0}
        sx={{
          width: '100%',
          maxWidth: 400,
          border: '1px solid',
          borderColor: 'divider',
          borderRadius: '6px',
          bgcolor: 'background.paper',
        }}
      >
        <CardContent sx={{ p: { xs: 3, sm: 4 } }}>
          {/* Header */}
          <Box sx={{ mb: 3 }}>
            <Typography
              variant="h5"
              sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', color: 'text.primary', letterSpacing: '-0.5px' }}
            >
              Sign in
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              Employee Management System
            </Typography>
          </Box>

          <Divider sx={{ mb: 3 }} />

          {error && (
            <Alert
              severity="error"
              variant="outlined"
              sx={{ mb: 2.5, borderRadius: '6px', fontSize: '0.82rem' }}
            >
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit} noValidate>
            <TextField
              id="login-username"
              label="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
              sx={{ mb: 2 }}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <PersonOutlineRoundedIcon fontSize="small" sx={{ color: 'text.disabled' }} />
                    </InputAdornment>
                  ),
                },
              }}
            />
            <TextField
              id="login-password"
              label="Password"
              type={showPwd ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
              sx={{ mb: 3 }}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <LockOutlineRoundedIcon fontSize="small" sx={{ color: 'text.disabled' }} />
                    </InputAdornment>
                  ),
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        onClick={() => setShowPwd((p) => !p)}
                        edge="end"
                        size="small"
                        sx={{ color: 'text.disabled' }}
                      >
                        {showPwd
                          ? <VisibilityOffRounded fontSize="small" />
                          : <VisibilityRounded fontSize="small" />}
                      </IconButton>
                    </InputAdornment>
                  ),
                },
              }}
            />
            <Button
              id="login-submit"
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={loading}
              disableElevation
              sx={{
                py: 1.4,
                fontSize: '0.95rem',
                fontWeight: 600,
                letterSpacing: '0.01em',
                borderRadius: '6px',
              }}
            >
              {loading ? <CircularProgress size={20} color="inherit" /> : 'Sign In'}
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default Login;
