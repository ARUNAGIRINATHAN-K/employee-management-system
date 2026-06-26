import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box, Typography, Button, Paper, Grid, Avatar, Divider, Chip,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField,
  MenuItem, IconButton, InputAdornment, Alert, CircularProgress,
} from '@mui/material';
import ArrowBackIcon                from '@mui/icons-material/ArrowBack';
import EditIcon                     from '@mui/icons-material/Edit';
import EmailIcon                    from '@mui/icons-material/Email';
import PhoneIcon                    from '@mui/icons-material/Phone';
import WorkIcon                     from '@mui/icons-material/Work';
import BusinessIcon                 from '@mui/icons-material/Business';
import CalendarTodayIcon            from '@mui/icons-material/CalendarToday';
import AttachMoneyIcon              from '@mui/icons-material/AttachMoney';
import PersonOutlineRoundedIcon     from '@mui/icons-material/PersonOutlineRounded';
import LockOutlineRoundedIcon       from '@mui/icons-material/LockOutlineRounded';
import VisibilityRounded            from '@mui/icons-material/VisibilityRounded';
import VisibilityOffRounded         from '@mui/icons-material/VisibilityOffRounded';
import CheckCircleOutlineRoundedIcon from '@mui/icons-material/CheckCircleOutlineRounded';
import { employeeService }          from '../../services/employeeService';
import { useAuth }                  from '../../context/AuthContext';
import type { Employee }            from '../../types';
import { LoadingSpinner }           from '../../components/common/CommonComponents';

const ROLE_OPTIONS = [
  { value: 'ROLE_EMPLOYEE', label: 'Employee' },
  { value: 'ROLE_MANAGER',  label: 'Manager'  },
  { value: 'ROLE_HR',       label: 'HR'        },
  { value: 'ROLE_ADMIN',    label: 'Admin'     },
];


/* ───────── Assign Account Dialog ───────── */
interface AssignDialogProps {
  open: boolean;
  employeeName: string;
  employeeId: number;
  onClose: () => void;
  onSuccess: (updated: Employee) => void;
}

const AssignAccountDialog = ({ open, employeeName, employeeId, onClose, onSuccess }: AssignDialogProps) => {
  const [form, setForm]       = useState({ username: '', password: '', role: 'ROLE_EMPLOYEE' });
  const [showPwd, setShowPwd] = useState(false);
  const [saving, setSaving]   = useState(false);
  const [err, setErr]         = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((p) => ({ ...p, [e.target.name]: e.target.value }));
    setErr('');
  };

  const validate = () => {
    if (!form.username.trim() || form.username.length < 3) return 'Username must be at least 3 characters';
    if (!form.password || form.password.length < 6)        return 'Password must be at least 6 characters';
    if (!form.role)                                        return 'A role is required';
    return '';
  };

  const handleSubmit = async () => {
    const validErr = validate();
    if (validErr) { setErr(validErr); return; }
    setSaving(true);
    try {
      const updated = await employeeService.assignAccount(employeeId, form);
      onSuccess(updated);
      setForm({ username: '', password: '', role: 'ROLE_EMPLOYEE' });
      onClose();
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? 'Failed to create account. Try again.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', pb: 1 }}>
        Set Login Account
      </DialogTitle>
      <Divider />
      <DialogContent sx={{ pt: 2.5 }}>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2.5 }}>
          Creating a login account for <strong>{employeeName}</strong>.
          The account email will be set to their registered employee email.
        </Typography>

        {err && (
          <Alert severity="error" variant="outlined" sx={{ mb: 2.5, borderRadius: '6px' }}>
            {err}
          </Alert>
        )}

        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              label="Username"
              name="username"
              value={form.username}
              onChange={handleChange}
              disabled={saving}
              autoFocus
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
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              label="Password"
              name="password"
              type={showPwd ? 'text' : 'password'}
              value={form.password}
              onChange={handleChange}
              disabled={saving}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <LockOutlineRoundedIcon fontSize="small" sx={{ color: 'text.disabled' }} />
                    </InputAdornment>
                  ),
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton size="small" onClick={() => setShowPwd((p) => !p)} sx={{ color: 'text.disabled' }}>
                        {showPwd ? <VisibilityOffRounded fontSize="small" /> : <VisibilityRounded fontSize="small" />}
                      </IconButton>
                    </InputAdornment>
                  ),
                },
              }}
            />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField
              select
              label="Role"
              name="role"
              value={form.role}
              onChange={handleChange}
              disabled={saving}
              helperText="Determines what this person can access after signing in"
            >
              {ROLE_OPTIONS.map((opt) => (
                <MenuItem key={opt.value} value={opt.value}>
                  {opt.label}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2.5, gap: 1 }}>
        <Button onClick={onClose} disabled={saving} sx={{ color: 'text.secondary' }}>
          Cancel
        </Button>
        <Button
          variant="contained"
          disableElevation
          onClick={handleSubmit}
          disabled={saving}
          sx={{ borderRadius: '6px', fontWeight: 600, minWidth: 140 }}
        >
          {saving ? <CircularProgress size={18} color="inherit" /> : 'Create Account'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

/* ─────────────────────────────────────────
   Main Employee Details Page
───────────────────────────────────────── */
const EmployeeDetails = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const { hasAnyRole, isAdmin } = useAuth();
  const [employee, setEmployee] = useState<Employee | null>(null);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState<string | null>(null);
  const [assignOpen, setAssignOpen] = useState(false);

  const canModify = hasAnyRole(['ROLE_ADMIN', 'ROLE_HR']);

  useEffect(() => {
    const fetchDetails = async () => {
      try {
        const data = await employeeService.getById(Number(id));
        setEmployee(data);
        setError(null);
      } catch (err) {
        console.error('Error fetching employee details:', err);
        setError('Failed to load employee profile details.');
      } finally {
        setLoading(false);
      }
    };
    if (id) fetchDetails();
  }, [id]);

  if (loading) return <LoadingSpinner />;
  if (error || !employee) {
    return (
      <Box sx={{ p: 3, textAlign: 'center' }}>
        <Typography color="error" variant="h6">{error || 'Employee not found.'}</Typography>
        <Button variant="outlined" onClick={() => navigate('/employees')} sx={{ mt: 2 }}>
          Back to list
        </Button>
      </Box>
    );
  }

  const hasAccount = !!employee.userId;

  return (
    <Box sx={{ maxWidth: 900, mx: 'auto' }}>
      {/* Header Actions */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Button
          variant="text"
          startIcon={<ArrowBackIcon fontSize="small" />}
          onClick={() => navigate('/employees')}
          sx={{ color: 'text.secondary', fontWeight: 500 }}
        >
          Back
        </Button>
        {canModify && (
          <Button
            variant="contained"
            disableElevation
            startIcon={<EditIcon fontSize="small" />}
            onClick={() => navigate(`/employees/${employee.id}/edit`)}
            sx={{ borderRadius: '6px', fontWeight: 600 }}
          >
            Edit Profile
          </Button>
        )}
      </Box>

      <Grid container spacing={2.5}>
        {/* Sidebar Profile Summary */}
        <Grid size={{ xs: 12, md: 4 }}>
          <Paper
            elevation={0}
            sx={{
              p: 3.5,
              textAlign: 'center',
              height: '100%',
              border: '1px solid',
              borderColor: 'divider',
              borderRadius: '6px',
            }}
          >
            <Avatar
              sx={{
                width: 80,
                height: 80,
                mx: 'auto',
                mb: 2,
                bgcolor: 'text.primary',
                color: 'background.paper',
                fontSize: '1.6rem',
                fontWeight: 700,
              }}
            >
              {employee.firstName[0]?.toUpperCase()}{employee.lastName[0]?.toUpperCase()}
            </Avatar>
            <Typography
              variant="h6"
              sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', letterSpacing: '-0.3px' }}
              gutterBottom
            >
              {employee.firstName} {employee.lastName}
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2.5 }}>
              {employee.jobTitle}
            </Typography>
            <Chip
              label={employee.status}
              size="small"
              variant="outlined"
              sx={{
                borderRadius: '4px',
                borderColor: employee.status === 'ACTIVE' ? 'success.main' : 'divider',
                color: employee.status === 'ACTIVE' ? 'success.dark' : 'text.disabled',
                fontWeight: 600,
                fontSize: '0.75rem',
              }}
            />

            <Divider sx={{ my: 3 }} />

            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, alignItems: 'flex-start', textAlign: 'left' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <EmailIcon sx={{ fontSize: 18, color: 'text.disabled' }} />
                <Typography variant="body2" noWrap sx={{ maxWidth: 180 }} title={employee.email}>
                  {employee.email}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <PhoneIcon sx={{ fontSize: 18, color: 'text.disabled' }} />
                <Typography variant="body2" color={employee.phone ? 'text.primary' : 'text.disabled'}>
                  {employee.phone || 'No phone on record'}
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>

        {/* Right Panel */}
        <Grid size={{ xs: 12, md: 8 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>

            {/* Profile Details */}
            <Paper
              elevation={0}
              sx={{ p: 3.5, border: '1px solid', borderColor: 'divider', borderRadius: '6px' }}
            >
              <Typography
                variant="subtitle1"
                sx={{ fontWeight: 600, fontFamily: 'Outfit, sans-serif', mb: 2.5 }}
              >
                Profile Details
              </Typography>
              <Grid container spacing={3}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5 }}>
                    <BusinessIcon sx={{ fontSize: 18, color: 'text.disabled', mt: 0.2 }} />
                    <Box>
                      <Typography variant="caption" color="text.disabled">Department</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>{employee.departmentName || 'N/A'}</Typography>
                    </Box>
                  </Box>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5 }}>
                    <WorkIcon sx={{ fontSize: 18, color: 'text.disabled', mt: 0.2 }} />
                    <Box>
                      <Typography variant="caption" color="text.disabled">Designation</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>{employee.jobTitle}</Typography>
                    </Box>
                  </Box>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5 }}>
                    <AttachMoneyIcon sx={{ fontSize: 18, color: 'text.disabled', mt: 0.2 }} />
                    <Box>
                      <Typography variant="caption" color="text.disabled">Salary (Annual)</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        ₹{employee.salary.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                      </Typography>
                    </Box>
                  </Box>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5 }}>
                    <CalendarTodayIcon sx={{ fontSize: 18, color: 'text.disabled', mt: 0.2 }} />
                    <Box>
                      <Typography variant="caption" color="text.disabled">Date of Joining</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>{employee.hireDate}</Typography>
                    </Box>
                  </Box>
                </Grid>
              </Grid>
            </Paper>

            {/* Login Account Section */}
            <Paper
              elevation={0}
              sx={{ p: 3.5, border: '1px solid', borderColor: 'divider', borderRadius: '6px' }}
            >
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography
                  variant="subtitle1"
                  sx={{ fontWeight: 600, fontFamily: 'Outfit, sans-serif' }}
                >
                  Login Account
                </Typography>
                {!hasAccount && isAdmin() && (
                  <Button
                    id="assign-account-btn"
                    variant="contained"
                    disableElevation
                    size="small"
                    onClick={() => setAssignOpen(true)}
                    sx={{ borderRadius: '6px', fontWeight: 600, fontSize: '0.78rem' }}
                  >
                    Set Login Account
                  </Button>
                )}
              </Box>
              <Divider sx={{ mb: 2.5 }} />

              {hasAccount ? (
                /* Account exists — show details */
                <Grid container spacing={2.5}>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                      <CheckCircleOutlineRoundedIcon sx={{ fontSize: 18, color: 'success.main' }} />
                      <Box>
                        <Typography variant="caption" color="text.disabled">Username</Typography>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>
                          {employee.username}
                        </Typography>
                      </Box>
                    </Box>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <Box>
                      <Typography variant="caption" color="text.disabled">Account Status</Typography>
                      <Box sx={{ mt: 0.25 }}>
                        <Chip
                          label="Active"
                          size="small"
                          variant="outlined"
                          sx={{
                            height: 20,
                            fontSize: '0.68rem',
                            borderRadius: '4px',
                            borderColor: 'success.main',
                            color: 'success.dark',
                            '& .MuiChip-label': { px: 0.75 },
                          }}
                        />
                      </Box>
                    </Box>
                  </Grid>
                  {isAdmin() && (
                    <Grid size={{ xs: 12 }}>
                      <Typography variant="caption" color="text.disabled">
                        To reset this user's password, go to{' '}
                        <Box
                          component="span"
                          sx={{ color: 'text.primary', cursor: 'pointer', textDecoration: 'underline' }}
                          onClick={() => navigate('/users')}
                        >
                          User Management
                        </Box>
                        {' '}and use the key icon next to <strong>{employee.username}</strong>.
                      </Typography>
                    </Grid>
                  )}
                </Grid>
              ) : (
                /* No account — explain what to do */
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
                  <LockOutlineRoundedIcon sx={{ fontSize: 20, color: 'text.disabled', mt: 0.2 }} />
                  <Box>
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>
                      No login account assigned
                    </Typography>
                    <Typography variant="caption" color="text.disabled">
                      {isAdmin()
                        ? 'Click "Set Login Account" to create credentials so this person can sign in.'
                        : 'Contact your administrator to set up login credentials for this employee.'}
                    </Typography>
                  </Box>
                </Box>
              )}
            </Paper>

          </Box>
        </Grid>
      </Grid>

      {/* Assign Account Dialog */}
      <AssignAccountDialog
        open={assignOpen}
        employeeName={`${employee.firstName} ${employee.lastName}`}
        employeeId={employee.id!}
        onClose={() => setAssignOpen(false)}
        onSuccess={(updated) => setEmployee(updated)}
      />
    </Box>
  );
};

export default EmployeeDetails;
