import { useState } from 'react';
import {
  Box, Typography, Button, TextField, MenuItem, Paper, Grid,
  IconButton, InputAdornment, Divider, Alert, CircularProgress,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import ArrowBackIcon            from '@mui/icons-material/ArrowBack';
import VisibilityRounded        from '@mui/icons-material/VisibilityRounded';
import VisibilityOffRounded     from '@mui/icons-material/VisibilityOffRounded';
import { userService }          from '../../services/userService';
import type { CreateUserRequest } from '../../types';

const ROLE_OPTIONS = [
  { value: 'ROLE_EMPLOYEE', label: 'Employee' },
  { value: 'ROLE_MANAGER',  label: 'Manager'  },
  { value: 'ROLE_HR',       label: 'HR'        },
  { value: 'ROLE_ADMIN',    label: 'Admin'     },
];

interface FormErrors {
  username?: string;
  email?: string;
  password?: string;
  role?: string;
}

const CreateUserForm = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState<CreateUserRequest>({
    username: '',
    email: '',
    password: '',
    roles: ['ROLE_EMPLOYEE'],
  });

  const [showPwd, setShowPwd]     = useState(false);
  const [loading, setLoading]     = useState(false);
  const [errors, setErrors]       = useState<FormErrors>({});
  const [apiError, setApiError]   = useState<string | null>(null);
  const [success, setSuccess]     = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    if (name === 'role') {
      setFormData((prev) => ({ ...prev, roles: [value] }));
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
    setErrors((prev) => ({ ...prev, [name]: undefined }));
    setApiError(null);
  };

  const validate = (): boolean => {
    const errs: FormErrors = {};
    if (!formData.username.trim()) {
      errs.username = 'Username is required';
    } else if (formData.username.length < 3) {
      errs.username = 'Username must be at least 3 characters';
    }
    if (!formData.email.trim()) {
      errs.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      errs.email = 'Invalid email format';
    }
    if (!formData.password) {
      errs.password = 'Password is required';
    } else if (formData.password.length < 6) {
      errs.password = 'Password must be at least 6 characters';
    }
    if (!formData.roles.length) {
      errs.role = 'A role is required';
    }
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    setApiError(null);
    try {
      await userService.create(formData);
      setSuccess(true);
      setTimeout(() => navigate('/users'), 1500);
    } catch (err: any) {
      const msg =
        err?.response?.data?.message ??
        'Failed to create user. Username or email may already be taken.';
      setApiError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ maxWidth: 600, mx: 'auto' }}>
      {/* Header */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 4 }}>
        <Button
          variant="text"
          startIcon={<ArrowBackIcon fontSize="small" />}
          onClick={() => navigate('/users')}
          sx={{ color: 'text.secondary', fontWeight: 500 }}
        >
          Back
        </Button>
        <Box>
          <Typography
            variant="h5"
            sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', letterSpacing: '-0.5px' }}
          >
            Create User Account
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Set login credentials and assign a system role
          </Typography>
        </Box>
      </Box>

      <Paper
        elevation={0}
        sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '6px', p: { xs: 3, sm: 4 } }}
      >
        {success && (
          <Alert severity="success" variant="outlined" sx={{ mb: 3, borderRadius: '6px' }}>
            User created successfully! Redirecting…
          </Alert>
        )}
        {apiError && (
          <Alert severity="error" variant="outlined" sx={{ mb: 3, borderRadius: '6px' }}>
            {apiError}
          </Alert>
        )}

        <form onSubmit={handleSubmit} noValidate>
          <Grid container spacing={2.5}>
            {/* Username */}
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                id="new-user-username"
                label="Username"
                name="username"
                value={formData.username}
                onChange={handleChange}
                error={!!errors.username}
                helperText={errors.username}
                disabled={loading || success}
                required
              />
            </Grid>

            {/* Email */}
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                id="new-user-email"
                label="Email Address"
                name="email"
                type="email"
                value={formData.email}
                onChange={handleChange}
                error={!!errors.email}
                helperText={errors.email}
                disabled={loading || success}
                required
              />
            </Grid>

            {/* Password */}
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                id="new-user-password"
                label="Password"
                name="password"
                type={showPwd ? 'text' : 'password'}
                value={formData.password}
                onChange={handleChange}
                error={!!errors.password}
                helperText={errors.password ?? 'Minimum 6 characters'}
                disabled={loading || success}
                required
                slotProps={{
                  input: {
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          size="small"
                          onClick={() => setShowPwd((p) => !p)}
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
            </Grid>

            {/* Role */}
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                id="new-user-role"
                select
                label="Role"
                name="role"
                value={formData.roles[0] ?? ''}
                onChange={handleChange}
                error={!!errors.role}
                helperText={errors.role ?? 'Determines what the user can access'}
                disabled={loading || success}
                required
              >
                {ROLE_OPTIONS.map((opt) => (
                  <MenuItem key={opt.value} value={opt.value}>
                    {opt.label}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>

            {/* Divider + Actions */}
            <Grid size={{ xs: 12 }}>
              <Divider sx={{ my: 1 }} />
              <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1.5, mt: 1.5 }}>
                <Button
                  onClick={() => navigate('/users')}
                  disabled={loading}
                  sx={{ color: 'text.secondary' }}
                >
                  Cancel
                </Button>
                <Button
                  id="create-user-submit"
                  type="submit"
                  variant="contained"
                  disableElevation
                  disabled={loading || success}
                  sx={{ borderRadius: '6px', fontWeight: 600, minWidth: 130 }}
                >
                  {loading
                    ? <CircularProgress size={18} color="inherit" />
                    : 'Create Account'}
                </Button>
              </Box>
            </Grid>
          </Grid>
        </form>
      </Paper>
    </Box>
  );
};

export default CreateUserForm;
