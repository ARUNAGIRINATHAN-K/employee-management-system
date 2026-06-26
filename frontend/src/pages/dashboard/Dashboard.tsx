import { useEffect, useState } from 'react';
import {
  Grid, Card, CardContent, Typography, Box, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Paper, Button, Chip,
  useTheme, alpha, LinearProgress
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import PeopleRoundedIcon from '@mui/icons-material/PeopleRounded';
import BusinessRoundedIcon from '@mui/icons-material/BusinessRounded';
import CheckCircleRoundedIcon from '@mui/icons-material/CheckCircleRounded';
import AttachMoneyRoundedIcon from '@mui/icons-material/AttachMoneyRounded';
import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import { dashboardService } from '../../services/dashboardService';
import { employeeService } from '../../services/employeeService';
import type { DashboardStats, Employee } from '../../types';
import { LoadingSpinner } from '../../components/common/CommonComponents';

/**
 * Premium Admin/HR/Manager Dashboard Page.
 */
const Dashboard = () => {
  const theme = useTheme();
  const navigate = useNavigate();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [recentEmployees, setRecentEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        const [statsData, recentData] = await Promise.all([
          dashboardService.getStats(),
          employeeService.getAll({ size: 5, sort: 'id,desc' })
        ]);
        setStats(statsData);
        setRecentEmployees(recentData.content);
        setError(null);
      } catch (err) {
        console.error('Error fetching dashboard data:', err);
        setError('Failed to load dashboard data.');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return <LoadingSpinner />;
  }

  if (error || !stats) {
    return (
      <Box sx={{ p: 3, textAlign: 'center' }}>
        <Typography color="error" variant="h6">
          {error || 'Error loading dashboard.'}
        </Typography>
      </Box>
    );
  }

  const statCards = [
    {
      title: 'Total Employees',
      value: stats.totalEmployees,
      icon: <PeopleRoundedIcon sx={{ fontSize: 28 }} />,
      color: theme.palette.primary.main,
      bg: `linear-gradient(135deg, ${alpha(theme.palette.primary.main, 0.16)} 0%, ${alpha(theme.palette.primary.main, 0.04)} 100%)`,
    },
    {
      title: 'Active Employees',
      value: stats.activeEmployees,
      icon: <CheckCircleRoundedIcon sx={{ fontSize: 28 }} />,
      color: theme.palette.success.main,
      bg: `linear-gradient(135deg, ${alpha(theme.palette.success.main, 0.16)} 0%, ${alpha(theme.palette.success.main, 0.04)} 100%)`,
    },
    {
      title: 'Total Departments',
      value: stats.totalDepartments,
      icon: <BusinessRoundedIcon sx={{ fontSize: 28 }} />,
      color: theme.palette.info.main,
      bg: `linear-gradient(135deg, ${alpha(theme.palette.info.main, 0.16)} 0%, ${alpha(theme.palette.info.main, 0.04)} 100%)`,
    },
    {
      title: 'Average Salary',
      value: `$${stats.averageSalary.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
      icon: <AttachMoneyRoundedIcon sx={{ fontSize: 28 }} />,
      color: theme.palette.secondary.main,
      bg: `linear-gradient(135deg, ${alpha(theme.palette.secondary.main, 0.16)} 0%, ${alpha(theme.palette.secondary.main, 0.04)} 100%)`,
    },
  ];

  // Calculate max department count for visual scaling
  const deptCounts = Object.values(stats.employeesPerDepartment);
  const maxCount = deptCounts.length > 0 ? Math.max(...deptCounts) : 1;

  return (
    <Box sx={{ animation: 'fadeIn 0.5s ease-out' }}>
      {/* Page Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" sx={{ fontWeight: 800 }} color="text.primary" gutterBottom>
          Dashboard Overview
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Real-time metrics and organization analytics
        </Typography>
      </Box>

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {statCards.map((card, i) => (
          <Grid size={{ xs: 12, sm: 6, md: 3 }} key={i}>
            <Card
              sx={{
                background: card.bg,
                border: '1px solid rgba(255,255,255,0.06)',
                borderRadius: 4,
                boxShadow: '0 8px 32px 0 rgba(0,0,0,0.2)',
                position: 'relative',
                overflow: 'hidden',
                '&::before': {
                  content: '""',
                  position: 'absolute',
                  top: -20,
                  right: -20,
                  width: 100,
                  height: 100,
                  borderRadius: '50%',
                  background: card.color,
                  opacity: 0.08,
                },
              }}
            >
              <CardContent sx={{ p: 3 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                  <Box
                    sx={{
                      p: 1.5,
                      borderRadius: 3,
                      bgcolor: alpha(card.color, 0.12),
                      color: card.color,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    {card.icon}
                  </Box>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }} gutterBottom>
                  {card.title}
                </Typography>
                <Typography variant="h4" sx={{ fontWeight: 800 }} color="text.primary">
                  {card.value}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={3}>
        {/* Department Breakdown */}
        <Grid size={{ xs: 12, md: 5 }}>
          <Card sx={{ p: 3, height: '100%', borderRadius: 4, bgcolor: 'background.paper' }}>
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 3 }}>
              Active Employees Per Department
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3.5 }}>
              {Object.keys(stats.employeesPerDepartment).length === 0 ? (
                <Typography variant="body2" color="text.secondary">
                  No active employees mapped to departments yet.
                </Typography>
              ) : (
                Object.entries(stats.employeesPerDepartment).map(([dept, count]) => {
                  const percentage = (count / maxCount) * 100;
                  return (
                    <Box key={dept}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                        <Typography variant="body2" sx={{ fontWeight: 600 }} color="text.primary">
                          {dept}
                        </Typography>
                        <Typography variant="body2" sx={{ fontWeight: 700 }} color="primary.main">
                          {count} {count === 1 ? 'Employee' : 'Employees'}
                        </Typography>
                      </Box>
                      <LinearProgress
                        variant="determinate"
                        value={percentage}
                        sx={{
                          height: 8,
                          borderRadius: 4,
                          bgcolor: alpha(theme.palette.primary.main, 0.1),
                          '& .MuiLinearProgress-bar': {
                            borderRadius: 4,
                            background: `linear-gradient(90deg, ${theme.palette.primary.main} 0%, ${theme.palette.primary.light} 100%)`,
                          },
                        }}
                      />
                    </Box>
                  );
                })
              )}
            </Box>
          </Card>
        </Grid>

        {/* Recent Employees Table */}
        <Grid size={{ xs: 12, md: 7 }}>
          <Card sx={{ p: 3, height: '100%', borderRadius: 4, bgcolor: 'background.paper' }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5 }}>
              <Typography variant="h6" sx={{ fontWeight: 700 }}>
                Recent Hires
              </Typography>
              <Button
                variant="outlined"
                color="primary"
                endIcon={<ArrowForwardRoundedIcon />}
                onClick={() => navigate('/employees')}
                size="small"
              >
                View All
              </Button>
            </Box>
            <TableContainer component={Paper} elevation={0} sx={{ bgcolor: 'transparent' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell>Department</TableCell>
                    <TableCell>Job Title</TableCell>
                    <TableCell align="right">Status</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {recentEmployees.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={4} align="center" sx={{ py: 3 }}>
                        <Typography variant="body2" color="text.secondary">
                          No employees registered yet.
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    recentEmployees.map((emp) => (
                      <TableRow key={emp.id} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                        <TableCell sx={{ py: 1.5 }}>
                          <Typography variant="body2" sx={{ fontWeight: 600 }}>
                            {emp.firstName} {emp.lastName}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {emp.email}
                          </Typography>
                        </TableCell>
                        <TableCell sx={{ py: 1.5 }}>
                          {emp.departmentName || 'N/A'}
                        </TableCell>
                        <TableCell sx={{ py: 1.5 }}>
                          {emp.jobTitle}
                        </TableCell>
                        <TableCell align="right" sx={{ py: 1.5 }}>
                          <Chip
                            label={emp.status}
                            color={emp.status === 'ACTIVE' ? 'success' : 'default'}
                            size="small"
                            sx={{ height: 22, fontSize: '0.7rem' }}
                          />
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
