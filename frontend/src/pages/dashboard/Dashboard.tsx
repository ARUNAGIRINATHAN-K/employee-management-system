import { useEffect, useState } from 'react';
import {
  Grid, Card, CardContent, Typography, Box, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Paper, Button, Chip,
  LinearProgress, Divider, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import PeopleOutlineRoundedIcon    from '@mui/icons-material/PeopleOutlineRounded';
import BusinessCenterOutlinedIcon  from '@mui/icons-material/BusinessCenterOutlined';
import CheckCircleOutlineRoundedIcon from '@mui/icons-material/CheckCircleOutlineRounded';
import MonetizationOnOutlinedIcon  from '@mui/icons-material/MonetizationOnOutlined';
import ArrowForwardRoundedIcon     from '@mui/icons-material/ArrowForwardRounded';
import PersonAddRoundedIcon        from '@mui/icons-material/PersonAddRounded';
import BusinessRoundedIcon         from '@mui/icons-material/BusinessRounded';
import ManageAccountsRoundedIcon   from '@mui/icons-material/ManageAccountsRounded';
import AssessmentRoundedIcon       from '@mui/icons-material/AssessmentRounded';
import SearchRoundedIcon           from '@mui/icons-material/SearchRounded';
import { dashboardService } from '../../services/dashboardService';
import { employeeService } from '../../services/employeeService';
import type { DashboardStats, Employee } from '../../types';
import { LoadingSpinner } from '../../components/common/CommonComponents';
import { useAuth } from '../../context/AuthContext';

const Dashboard = () => {
  const navigate = useNavigate();
  const { isManager, isAdmin, isHR } = useAuth();
  const managerMode = isManager() && !isAdmin() && !isHR();

  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [recentEmployees, setRecentEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reportOpen, setReportOpen] = useState(false);

  // Search Team State
  const [searchTeamOpen, setSearchTeamOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<Employee[]>([]);
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        const statsData = await dashboardService.getStats();
        setStats(statsData);

        let recentData;
        if (managerMode && statsData.departmentId) {
          recentData = await employeeService.getAll({
            size: 10,
            sort: 'id,desc',
            departmentId: statsData.departmentId,
          });
        } else {
          recentData = await employeeService.getAll({ size: 10, sort: 'id,desc' });
        }
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
  }, [managerMode]);

  useEffect(() => {
    if (!searchTeamOpen) {
      setSearchQuery('');
      setSearchResults([]);
      return;
    }

    const delayDebounce = setTimeout(async () => {
      if (!searchQuery.trim()) {
        setSearchResults([]);
        return;
      }
      setSearching(true);
      try {
        const response = await employeeService.getAll({
          search: searchQuery,
          departmentId: stats?.departmentId,
          size: 10,
        });
        setSearchResults(response.content);
      } catch (err) {
        console.error('Error searching team:', err);
      } finally {
        setSearching(false);
      }
    }, 400);

    return () => clearTimeout(delayDebounce);
  }, [searchQuery, searchTeamOpen, stats?.departmentId]);

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

  const statCards = managerMode
    ? [
        {
          title: 'Team Members',
          value: stats.totalEmployees,
          icon: <PeopleOutlineRoundedIcon sx={{ fontSize: 22 }} />,
        },
        {
          title: 'Active Team Members',
          value: stats.activeEmployees,
          icon: <CheckCircleOutlineRoundedIcon sx={{ fontSize: 22 }} />,
        },
        {
          title: 'Department',
          value: stats.departmentName || '—',
          icon: <BusinessCenterOutlinedIcon sx={{ fontSize: 22 }} />,
        },
        {
          title: 'Department Average Salary',
          value: `₹${stats.averageSalary.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 })}`,
          icon: <MonetizationOnOutlinedIcon sx={{ fontSize: 22 }} />,
        },
      ]
    : [
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
          title: 'Average Salary',
          value: `₹${stats.averageSalary.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 })}`,
          icon: <MonetizationOnOutlinedIcon sx={{ fontSize: 22 }} />,
        },
      ];

  const chartTitle = managerMode ? 'Team Distribution' : 'Employees by Department';
  const chartSub = managerMode ? 'Active team members by job title' : 'Active workforce distribution';

  const distributionData = managerMode
    ? (stats.teamDistribution || {})
    : (stats.employeesPerDepartment || {});

  const distCounts = Object.values(distributionData);
  const maxCount = distCounts.length > 0 ? Math.max(...distCounts) : 1;
  const activeCount = stats.activeEmployees;

  const quickActions = managerMode
    ? [
        { label: 'View Team', icon: <PeopleOutlineRoundedIcon fontSize="small" />, action: () => navigate('/employees') },
        { label: 'Search Team', icon: <SearchRoundedIcon fontSize="small" />, action: () => setSearchTeamOpen(true) },
      ]
    : [
        { label: 'Add Employee', icon: <PersonAddRoundedIcon fontSize="small" />, action: () => navigate('/employees/new') },
        { label: 'Add Department', icon: <BusinessRoundedIcon fontSize="small" />, action: () => navigate('/departments/new') },
        { label: 'Manage Users', icon: <ManageAccountsRoundedIcon fontSize="small" />, action: () => navigate('/users') },
        { label: 'Reports', icon: <AssessmentRoundedIcon fontSize="small" />, action: () => setReportOpen(true) },
      ];

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
          {managerMode ? 'Manager Dashboard' : 'Admin Dashboard'}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
          {managerMode
            ? 'Department statistics, team breakdown, and directory navigation'
            : 'Organization metrics, department distribution, and quick actions'}
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

      <Grid container spacing={2} sx={{ mb: 3 }}>
        {/* Quick Actions Panel */}
        <Grid size={{ xs: 12, md: 4 }}>
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
                Quick Actions
              </Typography>
              <Typography variant="caption" color="text.disabled" sx={{ display: 'block', mb: 2.5 }}>
                {managerMode ? 'Team management actions' : 'Direct administrative shortcuts'}
              </Typography>
              <Divider sx={{ mb: 2.5 }} />
              <Grid container spacing={1.5}>
                {quickActions.map((qa, i) => (
                  <Grid size={{ xs: managerMode ? 12 : 6, sm: managerMode ? 12 : 6 }} key={i}>
                    <Button
                      variant="outlined"
                      fullWidth
                      startIcon={qa.icon}
                      onClick={qa.action}
                      sx={{
                        py: 1.5,
                        px: 1,
                        border: '1px solid',
                        borderColor: 'divider',
                        borderRadius: '6px',
                        color: 'text.primary',
                        fontSize: '0.75rem',
                        fontWeight: 600,
                        textTransform: 'none',
                        justifyContent: 'flex-start',
                        '&:hover': {
                          bgcolor: 'action.hover',
                          borderColor: 'text.primary',
                        },
                      }}
                    >
                      {qa.label}
                    </Button>
                  </Grid>
                ))}
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        {/* Chart: Distribution */}
        <Grid size={{ xs: 12, md: 8 }}>
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
                {chartTitle}
              </Typography>
              <Typography variant="caption" color="text.disabled" sx={{ display: 'block', mb: 2.5 }}>
                {chartSub}
              </Typography>
              <Divider sx={{ mb: 2.5 }} />
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                {Object.keys(distributionData).length === 0 ? (
                  <Typography variant="body2" color="text.secondary">
                    {managerMode ? 'No active team members registered with job titles yet.' : 'No active employees mapped to departments yet.'}
                  </Typography>
                ) : (
                  Object.entries(distributionData).map(([key, count]) => {
                    const percentage = (count / maxCount) * 100;
                    return (
                      <Box key={key}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.75 }}>
                          <Typography variant="body2" sx={{ fontWeight: 500 }} color="text.primary">
                            {key}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {count} ({Math.round((count / (activeCount || 1)) * 100)}%)
                          </Typography>
                        </Box>
                        <LinearProgress
                          variant="determinate"
                          value={percentage}
                          sx={{
                            height: 6,
                            borderRadius: 3,
                            bgcolor: 'action.hover',
                            '& .MuiLinearProgress-bar': {
                              borderRadius: 3,
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
      </Grid>

      {/* Table: Recent Members */}
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
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 0.5 }}>
            <Box>
              <Typography
                variant="subtitle1"
                sx={{ fontWeight: 600, fontFamily: 'Outfit, sans-serif' }}
              >
                {managerMode ? 'Recent Team Members' : 'Recent Hires (All)'}
              </Typography>
              <Typography variant="caption" color="text.disabled">
                {managerMode ? 'Latest employee additions in your department' : 'Latest employee additions across the organization'}
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
              {managerMode ? 'View Team' : 'Employee Directory'}
            </Button>
          </Box>

          <Divider sx={{ my: 2 }} />

          <TableContainer component={Paper} elevation={0} sx={{ bgcolor: 'transparent' }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  {(managerMode
                    ? ['Employee', 'Job Title', 'Hire Date', 'Status']
                    : ['Employee', 'Department', 'Job Title', 'Hire Date', 'Status']
                  ).map((h, i) => (
                    <TableCell
                      key={h}
                      align={(managerMode ? i === 3 : i === 4) ? 'right' : 'left'}
                      sx={{
                        fontSize: '0.72rem',
                        fontWeight: 600,
                        color: 'text.disabled',
                        textTransform: 'uppercase',
                        letterSpacing: '0.05em',
                        borderColor: 'divider',
                        py: 1,
                        px: 1.5,
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
                    <TableCell colSpan={managerMode ? 4 : 5} align="center" sx={{ py: 4, borderBottom: 'none' }}>
                      <Typography variant="body2" color="text.secondary">
                        {managerMode ? 'No team members registered in your department yet.' : 'No employees registered yet. Click Add Employee to get started.'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  recentEmployees.map((emp) => (
                    <TableRow
                      key={emp.id}
                      sx={{
                        '&:last-child td': { border: 0 },
                        '&:hover': { bgcolor: 'action.hover', cursor: 'pointer' },
                      }}
                      onClick={() => navigate(`/employees/${emp.id}`)}
                    >
                      <TableCell sx={{ py: 1.5, px: 1.5, borderColor: 'divider' }}>
                        <Typography variant="body2" sx={{ fontWeight: 600, fontSize: '0.82rem' }}>
                          {emp.firstName} {emp.lastName}
                        </Typography>
                        <Typography variant="caption" color="text.disabled">
                          {emp.email}
                        </Typography>
                      </TableCell>
                      {!managerMode && (
                        <TableCell sx={{ py: 1.5, px: 1.5, fontSize: '0.82rem', borderColor: 'divider', color: 'text.secondary' }}>
                          {emp.departmentName || '—'}
                        </TableCell>
                      )}
                      <TableCell sx={{ py: 1.5, px: 1.5, fontSize: '0.82rem', borderColor: 'divider', color: 'text.secondary' }}>
                        {emp.jobTitle}
                      </TableCell>
                      <TableCell sx={{ py: 1.5, px: 1.5, fontSize: '0.82rem', borderColor: 'divider', color: 'text.disabled' }}>
                        {emp.hireDate ? new Date(emp.hireDate).toLocaleDateString() : '—'}
                      </TableCell>
                      <TableCell align="right" sx={{ py: 1.5, px: 1.5, borderColor: 'divider' }}>
                        <Chip
                          label={emp.status}
                          size="small"
                          variant="outlined"
                          sx={{
                            height: 22,
                            fontSize: '0.68rem',
                            borderRadius: '4px',
                            borderColor: emp.status === 'ACTIVE' ? 'success.main' : 'divider',
                            color: emp.status === 'ACTIVE' ? 'success.dark' : 'text.disabled',
                            '& .MuiChip-label': { px: 1 },
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

      {/* Reports Dialog Modal (Admin/HR) */}
      <Dialog open={reportOpen} onClose={() => setReportOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontFamily: 'Outfit, sans-serif', fontWeight: 600 }}>
          Generate Administrative Reports
        </DialogTitle>
        <DialogContent dividers sx={{ borderColor: 'divider' }}>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Select a system report to compile and export:
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            {['Workforce Compensation & Salary Analysis', 'Departmental Headcount & Resource Allocation', 'System User Audit & Role Assignment Log'].map((rep, idx) => (
              <Paper
                key={idx}
                elevation={0}
                sx={{
                  p: 1.5,
                  border: '1px solid',
                  borderColor: 'divider',
                  borderRadius: '6px',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <Typography variant="body2" sx={{ fontWeight: 500 }}>
                  {rep}
                </Typography>
                <Button size="small" variant="outlined" sx={{ textTransform: 'none', borderRadius: '4px' }} onClick={() => setReportOpen(false)}>
                  Export CSV
                </Button>
              </Paper>
            ))}
          </Box>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setReportOpen(false)} sx={{ color: 'text.secondary' }}>
            Close
          </Button>
        </DialogActions>
      </Dialog>

      {/* Search Team Dialog Modal (Manager) */}
      <Dialog
        open={searchTeamOpen}
        onClose={() => setSearchTeamOpen(false)}
        maxWidth="sm"
        fullWidth
        slotProps={{
          paper: {
            sx: {
              borderRadius: '6px',
              border: '1px solid',
              borderColor: 'divider',
              boxShadow: 'none',
            }
          }
        }}
      >
        <DialogTitle sx={{ fontFamily: 'Outfit, sans-serif', fontWeight: 600, pb: 1 }}>
          Search Team Members
        </DialogTitle>
        <DialogContent sx={{ p: 3, pt: 1 }}>
          <TextField
            autoFocus
            placeholder="Type name or email to search..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            slotProps={{
              input: {
                startAdornment: (
                  <SearchRoundedIcon sx={{ color: 'text.secondary', mr: 1, fontSize: 20 }} />
                ),
              }
            }}
            sx={{ mb: 2 }}
          />


          <Divider sx={{ mb: 2 }} />

          {searching ? (
            <Box sx={{ py: 3, display: 'flex', justifyContent: 'center' }}>
              <LinearProgress sx={{ width: '50%' }} />
            </Box>
          ) : searchQuery.trim() === '' ? (
            <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 2 }}>
              Start typing to search department team members.
            </Typography>
          ) : searchResults.length === 0 ? (
            <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 2 }}>
              No team members match your query.
            </Typography>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, maxHeight: 300, overflowY: 'auto' }}>
              {searchResults.map((emp) => (
                <Paper
                  key={emp.id}
                  elevation={0}
                  onClick={() => {
                    setSearchTeamOpen(false);
                    navigate(`/employees/${emp.id}`);
                  }}
                  sx={{
                    p: 1.5,
                    border: '1px solid',
                    borderColor: 'divider',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    transition: 'all 0.2s',
                    '&:hover': {
                      bgcolor: 'action.hover',
                      borderColor: 'text.primary',
                    },
                  }}
                >
                  <Box>
                    <Typography variant="body2" sx={{ fontWeight: 600 }}>
                      {emp.firstName} {emp.lastName}
                    </Typography>
                    <Typography variant="caption" color="text.disabled">
                      {emp.jobTitle} • {emp.email}
                    </Typography>
                  </Box>
                  <Chip
                    label={emp.status}
                    size="small"
                    variant="outlined"
                    sx={{
                      height: 20,
                      fontSize: '0.62rem',
                      borderColor: emp.status === 'ACTIVE' ? 'success.main' : 'divider',
                      color: emp.status === 'ACTIVE' ? 'success.dark' : 'text.disabled',
                    }}
                  />
                </Paper>
              ))}
            </Box>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2, borderTop: '1px solid', borderColor: 'divider' }}>
          <Button onClick={() => setSearchTeamOpen(false)} sx={{ color: 'text.secondary' }}>
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Dashboard;

