import { useEffect, useState } from 'react';
import {
  Box, Typography, Paper, Grid, Avatar, Divider, Chip
} from '@mui/material';
import EmailIcon from '@mui/icons-material/Email';
import PhoneIcon from '@mui/icons-material/Phone';
import WorkIcon from '@mui/icons-material/Work';
import BusinessIcon from '@mui/icons-material/Business';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import PersonIcon from '@mui/icons-material/Person';
import { useAuth } from '../../context/AuthContext';
import { employeeService } from '../../services/employeeService';
import type { Employee } from '../../types';
import { LoadingSpinner } from '../../components/common/CommonComponents';

/**
 * Employee Profile Page. Shows own profile information.
 */
const Profile = () => {
  const { user } = useAuth();
  const [employee, setEmployee] = useState<Employee | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProfile = async () => {
      if (!user || !user.employeeId) {
        setLoading(false);
        return;
      }
      try {
        const data = await employeeService.getById(user.employeeId);
        setEmployee(data);
      } catch (err) {
        console.error('Error fetching employee profile:', err);
        setError('Failed to load profile details.');
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [user]);

  if (loading) return <LoadingSpinner />;

  if (!user?.employeeId) {
    return (
      <Box sx={{ animation: 'fadeIn 0.5s ease-out', maxWidth: 600, mx: 'auto', mt: 4 }}>
        <Paper sx={{ p: 4, borderRadius: 4, bgcolor: 'background.paper', textAlign: 'center' }} elevation={0}>
          <PersonIcon sx={{ fontSize: 64, color: 'primary.main', mb: 2 }} />
          <Typography variant="h5" sx={{ fontWeight: 800 }} gutterBottom>
            System Account
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
            You are logged in as <strong>{user?.username}</strong> ({user?.roles.join(', ')}). This account does not have a linked Employee profile.
          </Typography>
          <Chip label="Administrative Account" color="primary" />
        </Paper>
      </Box>
    );
  }

  if (error || !employee) {
    return (
      <Box sx={{ p: 3, textAlign: 'center' }}>
        <Typography color="error" variant="h6">{error || 'Profile details not found.'}</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ animation: 'fadeIn 0.5s ease-out', maxWidth: 900, mx: 'auto' }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" sx={{ fontWeight: 800 }} color="text.primary" gutterBottom>
          My Profile
        </Typography>
        <Typography variant="body2" color="text.secondary">
          View your job profile, assignment details, and active directory settings
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Sidebar Info Summary */}
        <Grid size={{ xs: 12, md: 4 }}>
          <Paper sx={{ p: 4, textAlign: 'center', height: '100%', borderRadius: 4, bgcolor: 'background.paper' }} elevation={0}>
            <Avatar
              sx={{
                width: 100,
                height: 100,
                mx: 'auto',
                mb: 2.5,
                bgcolor: 'primary.main',
                fontSize: '2rem',
                fontWeight: 700
              }}
            >
              {employee.firstName[0]?.toUpperCase()}{employee.lastName[0]?.toUpperCase()}
            </Avatar>
            <Typography variant="h5" sx={{ fontWeight: 800 }} gutterBottom>
              {employee.firstName} {employee.lastName}
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              {employee.jobTitle}
            </Typography>

            <Chip
              label={employee.status}
              color={employee.status === 'ACTIVE' ? 'success' : 'default'}
              sx={{ px: 2, py: 0.5, fontWeight: 700 }}
            />

            <Divider sx={{ my: 3.5, opacity: 0.1 }} />

            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, alignItems: 'flex-start' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <EmailIcon color="action" fontSize="small" />
                <Typography variant="body2" noWrap sx={{ maxWidth: 200 }} title={employee.email}>
                  {employee.email}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <PhoneIcon color="action" fontSize="small" />
                <Typography variant="body2">
                  {employee.phone || 'No phone record'}
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>

        {/* Content Profile Details */}
        <Grid size={{ xs: 12, md: 8 }}>
          <Paper sx={{ p: 4, height: '100%', borderRadius: 4, bgcolor: 'background.paper' }} elevation={0}>
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 3 }}>
              Profile Details
            </Typography>
            <Grid container spacing={3.5}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
                  <BusinessIcon color="primary" sx={{ mt: 0.25 }} />
                  <Box>
                    <Typography variant="caption" color="text.secondary">Department</Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600 }}>{employee.departmentName || 'N/A'}</Typography>
                  </Box>
                </Box>
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
                  <WorkIcon color="primary" sx={{ mt: 0.25 }} />
                  <Box>
                    <Typography variant="caption" color="text.secondary">Designation</Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600 }}>{employee.jobTitle}</Typography>
                  </Box>
                </Box>
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
                  <AttachMoneyIcon color="primary" sx={{ mt: 0.25 }} />
                  <Box>
                    <Typography variant="caption" color="text.secondary">Salary (Annual)</Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600 }}>
                      ${employee.salary.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </Typography>
                  </Box>
                </Box>
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
                  <CalendarTodayIcon color="primary" sx={{ mt: 0.25 }} />
                  <Box>
                    <Typography variant="caption" color="text.secondary">Date of Joining</Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600 }}>{employee.hireDate}</Typography>
                  </Box>
                </Box>
              </Grid>
            </Grid>

            <Divider sx={{ my: 4, opacity: 0.1 }} />

            <Typography variant="h6" sx={{ fontWeight: 700, mb: 3 }}>
              Account Link Details
            </Typography>
            <Grid container spacing={3.5}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
                  <PersonIcon color="secondary" sx={{ mt: 0.25 }} />
                  <Box>
                    <Typography variant="caption" color="text.secondary">Linked Username</Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600 }}>{employee.username || 'No linked credential account'}</Typography>
                  </Box>
                </Box>
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
                  <PersonIcon color="secondary" sx={{ mt: 0.25 }} />
                  <Box>
                    <Typography variant="caption" color="text.secondary">Linked User ID</Typography>
                    <Typography variant="body1" sx={{ fontWeight: 600 }}>{employee.userId || 'N/A'}</Typography>
                  </Box>
                </Box>
              </Grid>
            </Grid>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Profile;
