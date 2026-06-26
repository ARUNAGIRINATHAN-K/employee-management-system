import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box, Typography, Button, Paper, Grid, Avatar, Divider, Chip
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import EditIcon from '@mui/icons-material/Edit';
import EmailIcon from '@mui/icons-material/Email';
import PhoneIcon from '@mui/icons-material/Phone';
import WorkIcon from '@mui/icons-material/Work';
import BusinessIcon from '@mui/icons-material/Business';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import PersonIcon from '@mui/icons-material/Person';
import { employeeService } from '../../services/employeeService';
import { useAuth } from '../../context/AuthContext';
import type { Employee } from '../../types';
import { LoadingSpinner } from '../../components/common/CommonComponents';

/**
 * Employee Profile Detail View Component.
 */
const EmployeeDetails = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const { hasAnyRole } = useAuth();
  const [employee, setEmployee] = useState<Employee | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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
        <Button variant="outlined" color="primary" onClick={() => navigate('/employees')} sx={{ mt: 2 }}>
          Back to list
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ animation: 'fadeIn 0.5s ease-out', maxWidth: 900, mx: 'auto' }}>
      {/* Header Actions */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Button
          variant="outlined"
          color="primary"
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/employees')}
          size="small"
        >
          Back
        </Button>
        {canModify && (
          <Button
            variant="contained"
            color="primary"
            startIcon={<EditIcon />}
            onClick={() => navigate(`/employees/${employee.id}/edit`)}
          >
            Edit Profile
          </Button>
        )}
      </Box>

      {/* Main Info Card */}
      <Grid container spacing={3}>
        {/* Sidebar Profile Summary */}
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

export default EmployeeDetails;
