import { useEffect, useState } from 'react';
import {
  Grid, Card, CardContent, Typography, Box, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Paper, Button, Chip,
  LinearProgress, Divider,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import PeopleOutlineRoundedIcon    from '@mui/icons-material/PeopleOutlineRounded';
import BusinessCenterOutlinedIcon  from '@mui/icons-material/BusinessCenterOutlined';
import CheckCircleOutlineRoundedIcon from '@mui/icons-material/CheckCircleOutlineRounded';
import MonetizationOnOutlinedIcon  from '@mui/icons-material/MonetizationOnOutlined';
import ArrowForwardRoundedIcon     from '@mui/icons-material/ArrowForwardRounded';
import { dashboardService } from '../../services/dashboardService';
import { employeeService } from '../../services/employeeService';
import type { DashboardStats, Employee } from '../../types';
import { LoadingSpinner } from '../../components/common/CommonComponents';

const Dashboard = () => {
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
          employeeService.getAll({ size: 5, sort: 'id,desc' }),
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

  if (loading) return <LoadingSpinner />;

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
      icon: <PeopleOutlineRoundedIcon sx={{ fontSize: 22 }} />,
    },
    {
      title: 'Active Employees',
      value: stats.activeEmployees,
      icon: <CheckCircleOutlineRoundedIcon sx={{ fontSize: 22 }} />,
    },
    {
      title: 'Departments',
      value: stats.totalDepartments,
      icon: <BusinessCenterOutlinedIcon sx={{ fontSize: 22 }} />,
    },
    {
      title: 'Avg. Salary',
      value: `$${stats.averageSalary.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 })}`,
      icon: <MonetizationOnOutlinedIcon sx={{ fontSize: 22 }} />,
    },
  ];

  const deptCounts = Object.values(stats.employeesPerDepartment);
  const maxCount = deptCounts.length > 0 ? Math.max(...deptCounts) : 1;

  return (
    <Box>
      {/* Page Header */}
      <Box sx={{ mb: 4 }}>
        <Typography
          variant="h5"
          sx={{
            fontWeight: 700,
            fontFamily: 'Outfit, sans-serif',
            letterSpacing: '-0.5px',
            color: 'text.primary',
          }}
        >
          Dashboard
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
          Organization overview and recent activity
        </Typography>
      </Box>

      {/* Stats Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        {statCards.map((card, i) => (
          <Grid size={{ xs: 12, sm: 6, md: 3 }} key={i}>
            <Card
              elevation={0}
              sx={{
                border: '1px solid',
                borderColor: 'divider',
                borderRadius: '6px',
                bgcolor: 'background.paper',
              }}
            >
              <CardContent sx={{ p: 2.5, '&:last-child': { pb: 2.5 } }}>
                <Box
                  sx={{
                    display: 'inline-flex',
                    p: 1,
                    mb: 2,
                    borderRadius: '6px',
                    bgcolor: 'action.hover',
                    color: 'text.secondary',
                  }}
                >
                  {card.icon}
                </Box>
                <Typography
                  variant="body2"
                  color="text.secondary"
                  sx={{ fontSize: '0.78rem', mb: 0.5, fontWeight: 500 }}
                >
                  {card.title}
                </Typography>
                <Typography
                  variant="h5"
                  sx={{
                    fontWeight: 700,
                    fontFamily: 'Outfit, sans-serif',
                    color: 'text.primary',
                    letterSpacing: '-0.5px',
                  }}
                >
                  {card.value}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={2}>
        {/* Department Breakdown */}
        <Grid size={{ xs: 12, md: 5 }}>
          <Card
            elevation={0}
            sx={{
              border: '1px solid',
              borderColor: 'divider',
              borderRadius: '6px',
              bgcolor: 'background.paper',
              height: '100%',
            }}
          >
            <CardContent sx={{ p: 2.5, '&:last-child': { pb: 2.5 } }}>
              <Typography
                variant="subtitle1"
                sx={{ fontWeight: 600, fontFamily: 'Outfit, sans-serif', mb: 0.5 }}
              >
                By Department
              </Typography>
              <Typography variant="caption" color="text.disabled" sx={{ display: 'block', mb: 2.5 }}>
                Active employees per department
              </Typography>
              <Divider sx={{ mb: 2.5 }} />
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                {Object.keys(stats.employeesPerDepartment).length === 0 ? (
                  <Typography variant="body2" color="text.secondary">
                    No active employees mapped to departments yet.
                  </Typography>
                ) : (
                  Object.entries(stats.employeesPerDepartment).map(([dept, count]) => {
                    const percentage = (count / maxCount) * 100;
                    return (
                      <Box key={dept}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.75 }}>
                          <Typography variant="body2" sx={{ fontWeight: 500 }} color="text.primary">
                            {dept}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {count}
                          </Typography>
                        </Box>
                        <LinearProgress
                          variant="determinate"
                          value={percentage}
                          sx={{
                            height: 4,
                            borderRadius: 2,
                            bgcolor: 'action.hover',
                            '& .MuiLinearProgress-bar': {
                              borderRadius: 2,
                              bgcolor: 'text.primary',
                            },
                          }}
                        />
                      </Box>
                    );
                  })
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Recent Employees Table */}
        <Grid size={{ xs: 12, md: 7 }}>
          <Card
            elevation={0}
            sx={{
              border: '1px solid',
              borderColor: 'divider',
              borderRadius: '6px',
              bgcolor: 'background.paper',
              height: '100%',
            }}
          >
            <CardContent sx={{ p: 2.5, '&:last-child': { pb: 2.5 } }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 0.5 }}>
                <Box>
                  <Typography
                    variant="subtitle1"
                    sx={{ fontWeight: 600, fontFamily: 'Outfit, sans-serif' }}
                  >
                    Recent Hires
                  </Typography>
                  <Typography variant="caption" color="text.disabled">
                    Last 5 employees added
                  </Typography>
                </Box>
                <Button
                  variant="text"
                  endIcon={<ArrowForwardRoundedIcon fontSize="small" />}
                  onClick={() => navigate('/employees')}
                  size="small"
                  sx={{
                    color: 'text.secondary',
                    fontSize: '0.78rem',
                    fontWeight: 500,
                    '&:hover': { color: 'text.primary', bgcolor: 'transparent' },
                  }}
                >
                  View all
                </Button>
              </Box>

              <Divider sx={{ my: 2 }} />

              <TableContainer component={Paper} elevation={0} sx={{ bgcolor: 'transparent' }}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      {['Name', 'Department', 'Job Title', 'Status'].map((h, i) => (
                        <TableCell
                          key={h}
                          align={i === 3 ? 'right' : 'left'}
                          sx={{
                            fontSize: '0.72rem',
                            fontWeight: 600,
                            color: 'text.disabled',
                            textTransform: 'uppercase',
                            letterSpacing: '0.05em',
                            borderColor: 'divider',
                            py: 1,
                            px: 1,
                          }}
                        >
                          {h}
                        </TableCell>
                      ))}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {recentEmployees.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={4} align="center" sx={{ py: 4, borderBottom: 'none' }}>
                          <Typography variant="body2" color="text.secondary">
                            No employees registered yet.
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ) : (
                      recentEmployees.map((emp) => (
                        <TableRow
                          key={emp.id}
                          sx={{
                            '&:last-child td': { border: 0 },
                            '&:hover': { bgcolor: 'action.hover' },
                          }}
                        >
                          <TableCell sx={{ py: 1.25, px: 1, borderColor: 'divider' }}>
                            <Typography variant="body2" sx={{ fontWeight: 600, fontSize: '0.82rem' }}>
                              {emp.firstName} {emp.lastName}
                            </Typography>
                            <Typography variant="caption" color="text.disabled">
                              {emp.email}
                            </Typography>
                          </TableCell>
                          <TableCell sx={{ py: 1.25, px: 1, fontSize: '0.82rem', borderColor: 'divider', color: 'text.secondary' }}>
                            {emp.departmentName || '—'}
                          </TableCell>
                          <TableCell sx={{ py: 1.25, px: 1, fontSize: '0.82rem', borderColor: 'divider', color: 'text.secondary' }}>
                            {emp.jobTitle}
                          </TableCell>
                          <TableCell align="right" sx={{ py: 1.25, px: 1, borderColor: 'divider' }}>
                            <Chip
                              label={emp.status}
                              size="small"
                              variant="outlined"
                              sx={{
                                height: 20,
                                fontSize: '0.68rem',
                                borderRadius: '4px',
                                borderColor: emp.status === 'ACTIVE' ? 'success.main' : 'divider',
                                color: emp.status === 'ACTIVE' ? 'success.dark' : 'text.disabled',
                                '& .MuiChip-label': { px: 0.75 },
                              }}
                            />
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
