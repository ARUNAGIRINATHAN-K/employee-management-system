import { useEffect, useState, useRef } from 'react';
import {
  Box, Typography, Grid, Card, CardContent, Button, Tabs, Tab,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, Chip, TextField, Divider, Alert, CircularProgress,
} from '@mui/material';
import PlayArrowRoundedIcon from '@mui/icons-material/PlayArrowRounded';
import StopRoundedIcon from '@mui/icons-material/StopRounded';
import HistoryRoundedIcon from '@mui/icons-material/HistoryRounded';
import AdminPanelSettingsRoundedIcon from '@mui/icons-material/AdminPanelSettingsRounded';
import DownloadRoundedIcon from '@mui/icons-material/DownloadRounded';
import AccessTimeRoundedIcon from '@mui/icons-material/AccessTimeRounded';
import RefreshRoundedIcon from '@mui/icons-material/RefreshRounded';

import { useAuth } from '../../context/AuthContext';
import { attendanceService } from '../../services/attendanceService';
import type { Attendance, AttendanceSummary, AttendancePolicy } from '../../types';

// Helper to format duration in seconds to HH:mm:ss
const formatDuration = (seconds: number) => {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = Math.floor(seconds % 60);
  return [
    h.toString().padStart(2, '0'),
    m.toString().padStart(2, '0'),
    s.toString().padStart(2, '0')
  ].join(':');
};

const getStatusColor = (status: string) => {
  switch (status) {
    case 'PRESENT': return { bg: '#E2FBE9', text: '#0F7336', label: 'Present' };
    case 'LATE': return { bg: '#FFF4E5', text: '#B76E00', label: 'Late' };
    case 'ABSENT': return { bg: '#FEEBEE', text: '#C62828', label: 'Absent' };
    case 'ON_LEAVE': return { bg: '#E3F2FD', text: '#1565C0', label: 'On Leave' };
    case 'WFH': return { bg: '#EDE7F6', text: '#673AB7', label: 'WFH' };
    case 'WEEKEND': return { bg: '#ECEFF1', text: '#455A64', label: 'Weekend' };
    case 'HOLIDAY': return { bg: '#E0F7FA', text: '#00838F', label: 'Holiday' };
    default: return { bg: '#F8FAFC', text: '#64748B', label: status };
  }
};

const AttendanceDashboard = () => {
  const { user, isManager, isAdmin, isHR } = useAuth();
  const showManagerTab = isManager() || isAdmin() || isHR();
  const showAdminTab = isAdmin() || isHR();

  // Active Tab: 0 = My Attendance, 1 = Team Presence, 2 = Shift Config
  const [activeTab, setActiveTab] = useState(0);

  // ─── My Attendance State ────────────────────────────────────────────────────
  const [todayRecord, setTodayRecord] = useState<Attendance | null>(null);
  const [summary, setSummary] = useState<AttendanceSummary | null>(null);
  const [history, setHistory] = useState<Attendance[]>([]);
  const [timerVal, setTimerVal] = useState('00:00:00');
  const timerIntervalRef = useRef<any>(null);

  // History Range Form
  const getFirstDayOfMonth = () => {
    const d = new Date();
    return new Date(d.getFullYear(), d.getMonth(), 1).toISOString().split('T')[0];
  };
  const getTodayStr = () => new Date().toISOString().split('T')[0];

  const [historyStart, setHistoryStart] = useState(getFirstDayOfMonth());
  const [historyEnd, setHistoryEnd] = useState(getTodayStr());

  // ─── Team Presence State ────────────────────────────────────────────────────
  const [teamToday, setTeamToday] = useState<Attendance[]>([]);
  const [teamCounts, setTeamCounts] = useState<Record<string, number>>({});
  const [teamHistoryStart, setTeamHistoryStart] = useState(getFirstDayOfMonth());
  const [teamHistoryEnd, setTeamHistoryEnd] = useState(getTodayStr());

  // ─── Policy Settings State ──────────────────────────────────────────────────
  const [policy, setPolicy] = useState<AttendancePolicy>({
    shiftStartTime: '09:00:00',
    shiftEndTime: '17:00:00',
    gracePeriodMinutes: 15,
    overtimeThresholdMinutes: 60,
  });

  // Global UI
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [msg, setMsg] = useState<{ type: 'success' | 'error', text: string } | null>(null);

  // ─── Fetch My Attendance (Tab 0) ────────────────────────────────────────────
  const fetchMyData = async () => {
    if (!user?.employeeId) {
      setTodayRecord(null);
      setSummary(null);
      setHistory([]);
      return;
    }

    try {
      setLoading(true);
      const todayData = await attendanceService.getTodayStatus();
      setTodayRecord(todayData);

      const summaryData = await attendanceService.getPersonalSummary();
      setSummary(summaryData);

      const historyData = await attendanceService.getPersonalHistory(historyStart, historyEnd);
      setHistory(historyData);
    } catch (err: any) {
      console.error(err);
      setMsg({ type: 'error', text: err.response?.data?.message || 'Failed to load personal attendance details.' });
    } finally {
      setLoading(false);
    }
  };

  // ─── Fetch Team Attendance (Tab 1) ──────────────────────────────────────────
  const fetchTeamData = async () => {
    try {
      setLoading(true);
      const teamTodayData = await attendanceService.getTeamTodayStatus();
      setTeamToday(teamTodayData);

      const counts = await attendanceService.getTeamSummaryCounts();
      setTeamCounts(counts);
    } catch (err: any) {
      console.error(err);
      setMsg({ type: 'error', text: err.response?.data?.message || 'Failed to load team attendance.' });
    } finally {
      setLoading(false);
    }
  };

  // ─── Fetch Policy Settings (Tab 2) ──────────────────────────────────────────
  const fetchPolicyData = async () => {
    try {
      setLoading(true);
      const policyData = await attendanceService.getPolicy();
      setPolicy(policyData);
    } catch (err: any) {
      console.error(err);
      setMsg({ type: 'error', text: err.response?.data?.message || 'Failed to load policy configurations.' });
    } finally {
      setLoading(false);
    }
  };

  // Trigger loading based on active tab
  useEffect(() => {
    setMsg(null);
    if (activeTab === 0) {
      fetchMyData();
    } else if (activeTab === 1 && showManagerTab) {
      fetchTeamData();
    } else if (activeTab === 2 && showAdminTab) {
      fetchPolicyData();
    }
  }, [activeTab, user?.employeeId]);

  // Clock timer calculator
  useEffect(() => {
    if (todayRecord && todayRecord.clockIn && !todayRecord.clockOut) {
      const clockInTime = new Date(todayRecord.clockIn).getTime();
      
      const updateTimer = () => {
        const diffMs = Date.now() - clockInTime;
        if (diffMs > 0) {
          setTimerVal(formatDuration(diffMs / 1000));
        } else {
          setTimerVal('00:00:00');
        }
      };

      updateTimer();
      timerIntervalRef.current = setInterval(updateTimer, 1000);
    } else {
      setTimerVal('00:00:00');
      if (timerIntervalRef.current) {
        clearInterval(timerIntervalRef.current);
      }
    }

    return () => {
      if (timerIntervalRef.current) {
        clearInterval(timerIntervalRef.current);
      }
    };
  }, [todayRecord]);

  // Clock Actions
  const handleClockIn = async () => {
    try {
      setActionLoading(true);
      setMsg(null);
      const res = await attendanceService.clockIn();
      setTodayRecord(res);
      setMsg({ type: 'success', text: 'Clock-in recorded successfully!' });
      // Refresh summary & history
      const summaryData = await attendanceService.getPersonalSummary();
      setSummary(summaryData);
      const historyData = await attendanceService.getPersonalHistory(historyStart, historyEnd);
      setHistory(historyData);
    } catch (err: any) {
      setMsg({ type: 'error', text: err.response?.data?.message || 'Failed to clock in.' });
    } finally {
      setActionLoading(false);
    }
  };

  const handleClockOut = async () => {
    try {
      setActionLoading(true);
      setMsg(null);
      const res = await attendanceService.clockOut();
      setTodayRecord(res);
      setMsg({ type: 'success', text: 'Clock-out recorded successfully!' });
      // Refresh summary & history
      const summaryData = await attendanceService.getPersonalSummary();
      setSummary(summaryData);
      const historyData = await attendanceService.getPersonalHistory(historyStart, historyEnd);
      setHistory(historyData);
    } catch (err: any) {
      setMsg({ type: 'error', text: err.response?.data?.message || 'Failed to clock out.' });
    } finally {
      setActionLoading(false);
    }
  };

  // Submit configuration changes
  const handleSavePolicy = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setActionLoading(true);
      setMsg(null);
      const updated = await attendanceService.updatePolicy(policy);
      setPolicy(updated);
      setMsg({ type: 'success', text: 'Attendance shift policy updated successfully.' });
    } catch (err: any) {
      setMsg({ type: 'error', text: err.response?.data?.message || 'Failed to update shift policy.' });
    } finally {
      setActionLoading(false);
    }
  };

  // Manager CSV Report Export
  const handleExportCSV = async () => {
    try {
      setActionLoading(true);
      const data = await attendanceService.getTeamHistory(teamHistoryStart, teamHistoryEnd);
      
      if (!data || data.length === 0) {
        setMsg({ type: 'error', text: 'No logs found in the selected date range to export.' });
        return;
      }

      // Generate CSV file content
      const headers = ['Date', 'Employee Name', 'Clock In', 'Clock Out', 'Status', 'Work Mode', 'Late (mins)', 'Overtime (mins)'];
      const csvRows = [
        headers.join(','),
        ...data.map(row => [
          row.date,
          `"${row.employeeName || ''}"`,
          row.clockIn ? row.clockIn.replace('T', ' ') : '',
          row.clockOut ? row.clockOut.replace('T', ' ') : '',
          row.status,
          row.workMode,
          row.lateMinutes,
          row.overtimeMinutes
        ].join(','))
      ];

      const csvContent = 'data:text/csv;charset=utf-8,' + csvRows.join('\n');
      const encodedUri = encodeURI(csvContent);
      const link = document.createElement('a');
      link.setAttribute('href', encodedUri);
      link.setAttribute('download', `Team_Attendance_Report_${teamHistoryStart}_to_${teamHistoryEnd}.csv`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      setMsg({ type: 'success', text: 'Team report exported successfully!' });
    } catch (err: any) {
      setMsg({ type: 'error', text: 'Failed to generate report export.' });
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <Box sx={{ maxWidth: '1200px', mx: 'auto', py: 1 }}>
      {/* Page Header */}
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography
            variant="h4"
            sx={{
              fontWeight: 800,
              fontFamily: 'Outfit, sans-serif',
              letterSpacing: '-0.5px',
              color: 'text.primary',
              fontSize: '1.75rem',
            }}
          >
            Attendance & Shifts
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5, fontFamily: 'Inter, sans-serif' }}>
            Log hours, monitor team presence, and define attendance scheduling configurations.
          </Typography>
        </Box>
      </Box>

      {/* Tabs */}
      <Paper
        elevation={0}
        sx={{
          border: '1px solid',
          borderColor: 'divider',
          borderRadius: '8px',
          bgcolor: 'background.paper',
          mb: 4,
          overflow: 'hidden',
        }}
      >
        <Tabs
          value={activeTab}
          onChange={(_, val) => setActiveTab(val)}
          sx={{
            px: 2,
            borderBottom: '1px solid',
            borderColor: 'divider',
            '& .MuiTab-root': {
              fontFamily: 'Outfit, sans-serif',
              textTransform: 'none',
              fontWeight: 600,
              fontSize: '0.9rem',
              py: 2,
              minWidth: 120,
            },
          }}
        >
          <Tab label="My Attendance" id="tab-my-attendance" />
          {showManagerTab && <Tab label="Team Presence" id="tab-team-presence" />}
          {showAdminTab && <Tab label="Shift policies" id="tab-shift-config" />}
        </Tabs>

        {/* Global Notifications */}
        {msg && (
          <Box sx={{ px: 3, pt: 3 }}>
            <Alert
              severity={msg.type}
              onClose={() => setMsg(null)}
              sx={{ borderRadius: '6px', fontFamily: 'Inter, sans-serif', fontSize: '0.85rem' }}
            >
              {msg.text}
            </Alert>
          </Box>
        )}

        <Box sx={{ p: 3 }}>
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
              <CircularProgress size={36} thickness={4} />
            </Box>
          ) : (
            <>
              {/* TAB 0: MY ATTENDANCE */}
              {activeTab === 0 && (
                !user?.employeeId ? (
                  <Box sx={{ p: 4, textAlign: 'center', border: '1px solid', borderColor: 'divider', borderRadius: '8px', bgcolor: '#FAFBFD' }}>
                    <AccessTimeRoundedIcon sx={{ fontSize: '3rem', color: 'text.disabled', mb: 2 }} />
                    <Typography variant="h6" sx={{ fontWeight: 800, fontFamily: 'Outfit, sans-serif', mb: 1 }}>
                      No Employee Profile Linked
                    </Typography>
                    <Typography variant="body2" sx={{ color: 'text.secondary', fontFamily: 'Inter, sans-serif', maxWidth: '480px', mx: 'auto' }}>
                      This administrator/user account is not linked to an employee profile. Personal attendance logging and history logs are not applicable.
                    </Typography>
                  </Box>
                ) : (
                  <Grid container spacing={3.5}>
                    {/* Punch Card Column */}
                    <Grid size={{ xs: 12, md: 4.5 }}>
                      <Card
                        elevation={0}
                        sx={{
                          border: '1px solid',
                          borderColor: 'divider',
                          borderRadius: '8px',
                          height: '100%',
                          display: 'flex',
                          flexDirection: 'column',
                          justifyContent: 'center',
                          bgcolor: '#FAFBFD',
                        }}
                      >
                        <CardContent sx={{ p: 3.5, textAlign: 'center' }}>
                          <Typography variant="subtitle2" sx={{ color: 'text.secondary', fontWeight: 700, fontFamily: 'Outfit, sans-serif', letterSpacing: '0.5px', textTransform: 'uppercase', mb: 2 }}>
                            Daily Punch Clock
                          </Typography>

                          {/* Status Badge */}
                          <Box sx={{ display: 'flex', justifyContent: 'center', mb: 3 }}>
                            {todayRecord ? (
                              (() => {
                                const col = getStatusColor(todayRecord.status);
                                return (
                                  <Chip
                                    label={col.label}
                                    sx={{
                                      bgcolor: col.bg,
                                      color: col.text,
                                      fontWeight: 700,
                                      fontSize: '0.8rem',
                                      borderRadius: '6px',
                                      fontFamily: 'Outfit, sans-serif',
                                    }}
                                  />
                                );
                              })()
                            ) : (
                              <Chip
                                label="Not Clocked In"
                                sx={{
                                  bgcolor: '#F1F5F9',
                                  color: '#475569',
                                  fontWeight: 700,
                                  fontSize: '0.8rem',
                                  borderRadius: '6px',
                                  fontFamily: 'Outfit, sans-serif',
                                }}
                              />
                            )}
                          </Box>

                          {/* Live Timer / Ticking Indicator */}
                          <Box sx={{ mb: 4 }}>
                            <Typography
                              variant="h3"
                              sx={{
                                fontWeight: 800,
                                fontFamily: 'Outfit, sans-serif',
                                color: todayRecord && !todayRecord.clockOut ? 'primary.main' : 'text.primary',
                                fontSize: '2.5rem',
                                letterSpacing: '1px',
                              }}
                            >
                              {timerVal}
                            </Typography>
                            <Typography variant="caption" sx={{ color: 'text.disabled', fontFamily: 'Inter, sans-serif', fontSize: '0.75rem', mt: 0.5 }}>
                              {todayRecord && !todayRecord.clockOut ? 'Active Time Card Session' : 'No Clock In Session'}
                            </Typography>
                          </Box>

                          {/* Action Buttons */}
                          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, maxWidth: '280px', mx: 'auto' }}>
                            {!todayRecord ? (
                              <Button
                                id="btn-clock-in"
                                variant="contained"
                                color="primary"
                                size="large"
                                onClick={handleClockIn}
                                disabled={actionLoading}
                                startIcon={<PlayArrowRoundedIcon />}
                                sx={{
                                  textTransform: 'none',
                                  fontWeight: 700,
                                  fontFamily: 'Outfit, sans-serif',
                                  borderRadius: '6px',
                                  py: 1.2,
                                  boxShadow: 'none',
                                  '&:hover': { boxShadow: 'none' },
                                }}
                              >
                                Punch In
                              </Button>
                            ) : !todayRecord.clockOut ? (
                              <Button
                                id="btn-clock-out"
                                variant="contained"
                                color="error"
                                size="large"
                                onClick={handleClockOut}
                                disabled={actionLoading}
                                startIcon={<StopRoundedIcon />}
                                sx={{
                                  textTransform: 'none',
                                  fontWeight: 700,
                                  fontFamily: 'Outfit, sans-serif',
                                  borderRadius: '6px',
                                  py: 1.2,
                                  boxShadow: 'none',
                                  '&:hover': { boxShadow: 'none' },
                                }}
                              >
                                Punch Out
                              </Button>
                            ) : (
                              <Button
                                variant="outlined"
                                size="large"
                                disabled
                                sx={{
                                  textTransform: 'none',
                                  fontWeight: 700,
                                  fontFamily: 'Outfit, sans-serif',
                                  borderRadius: '6px',
                                  py: 1.2,
                                }}
                              >
                                Completed Today
                              </Button>
                            )}
                          </Box>

                          {/* Clock In / Out times info list */}
                          {todayRecord && (
                            <Box sx={{ mt: 4, display: 'flex', flexDirection: 'column', gap: 1.5, textAlign: 'left', px: 2 }}>
                              <Divider />
                              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <Typography variant="body2" sx={{ color: 'text.secondary', fontFamily: 'Inter, sans-serif' }}>
                                  Clocked In Time
                                </Typography>
                                <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.primary', fontFamily: 'Outfit, sans-serif' }}>
                                  {new Date(todayRecord.clockIn!).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                </Typography>
                              </Box>
                              {todayRecord.clockOut && (
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                  <Typography variant="body2" sx={{ color: 'text.secondary', fontFamily: 'Inter, sans-serif' }}>
                                    Clocked Out Time
                                  </Typography>
                                  <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.primary', fontFamily: 'Outfit, sans-serif' }}>
                                    {new Date(todayRecord.clockOut).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                  </Typography>
                                </Box>
                              )}
                              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <Typography variant="body2" sx={{ color: 'text.secondary', fontFamily: 'Inter, sans-serif' }}>
                                  Today Work Mode
                                </Typography>
                                <Chip
                                  label={todayRecord.workMode}
                                  size="small"
                                  variant="outlined"
                                  sx={{ height: 20, fontSize: '0.65rem', borderRadius: '4px', textTransform: 'uppercase' }}
                                />
                              </Box>
                            </Box>
                          )}
                        </CardContent>
                      </Card>
                    </Grid>

                      {/* Summary & History Column */}
                      <Grid size={{ xs: 12, md: 7.5 }}>
                        {/* Month Summary Stats Grid */}
                        {summary && (
                          <Grid container spacing={2} sx={{ mb: 4 }}>
                            {[
                              { val: summary.presentDays, label: 'Days Present', color: 'success.main' },
                              { val: summary.wfhDays, label: 'Remote/WFH', color: 'purple' },
                              { val: summary.absentDays, label: 'Days Absent', color: 'error.main' },
                              { val: summary.leaveDays, label: 'Leaves taken', color: 'primary.main' },
                              { val: summary.lateDays, label: 'Late Entries', color: 'warning.main' },
                            ].map((stat, i) => (
                              <Grid size={{ xs: 4, sm: 2.4 }} key={i}>
                                <Paper
                                  elevation={0}
                                  sx={{
                                    border: '1px solid',
                                    borderColor: 'divider',
                                    borderRadius: '8px',
                                    p: 1.5,
                                    textAlign: 'center',
                                  }}
                                >
                                  <Typography
                                    variant="h5"
                                    sx={{
                                      fontWeight: 800,
                                      fontFamily: 'Outfit, sans-serif',
                                      color: stat.color,
                                      fontSize: '1.25rem',
                                    }}
                                  >
                                    {stat.val}
                                  </Typography>
                                  <Typography variant="caption" sx={{ color: 'text.disabled', fontWeight: 500, fontFamily: 'Inter, sans-serif', display: 'block', mt: 0.25 }}>
                                    {stat.label}
                                  </Typography>
                                </Paper>
                              </Grid>
                            ))}
                          </Grid>
                        )}

                        {/* History Section */}
                        <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '8px' }}>
                          <CardContent sx={{ p: 3 }}>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
                              <Typography variant="h6" sx={{ fontWeight: 800, fontFamily: 'Outfit, sans-serif', fontSize: '1rem', display: 'flex', alignItems: 'center', gap: 1 }}>
                                <HistoryRoundedIcon fontSize="small" sx={{ color: 'text.secondary' }} />
                                Attendance Logs
                              </Typography>

                              {/* Range Filter */}
                              <Box sx={{ display: 'flex', flexDirection: 'row', gap: 1.5, alignItems: 'center' }}>
                                <TextField
                                  type="date"
                                  size="small"
                                  value={historyStart}
                                  onChange={(e) => setHistoryStart(e.target.value)}
                                  sx={{ '& .MuiInputBase-input': { fontSize: '0.8rem', py: 0.7 } }}
                                />
                                <Typography variant="caption" sx={{ color: 'text.disabled' }}>to</Typography>
                                <TextField
                                  type="date"
                                  size="small"
                                  value={historyEnd}
                                  onChange={(e) => setHistoryEnd(e.target.value)}
                                  sx={{ '& .MuiInputBase-input': { fontSize: '0.8rem', py: 0.7 } }}
                                />
                                <Button
                                  variant="outlined"
                                  size="small"
                                  onClick={fetchMyData}
                                  sx={{ minWidth: 40, py: 0.7, px: 1, textTransform: 'none', borderRadius: '6px' }}
                                >
                                  <RefreshRoundedIcon fontSize="small" />
                                </Button>
                              </Box>
                            </Box>

                            {/* Logs Table */}
                            <TableContainer sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '6px', overflow: 'hidden' }}>
                              <Table size="small">
                                <TableHead sx={{ bgcolor: '#F8FAFC' }}>
                                  <TableRow>
                                    <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Date</TableCell>
                                    <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Punch In</TableCell>
                                    <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Punch Out</TableCell>
                                    <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Status</TableCell>
                                    <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Overtime</TableCell>
                                  </TableRow>
                                </TableHead>
                                <TableBody>
                                  {history.length === 0 ? (
                                    <TableRow>
                                      <TableCell colSpan={5} align="center" sx={{ py: 4, color: 'text.secondary', fontFamily: 'Inter, sans-serif', fontSize: '0.85rem' }}>
                                        No attendance records found for selected range.
                                      </TableCell>
                                    </TableRow>
                                  ) : (
                                    history.map((row) => (
                                      <TableRow key={row.id || row.date} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                                        <TableCell sx={{ fontFamily: 'Outfit, sans-serif', fontSize: '0.82rem', fontWeight: 600 }}>{row.date}</TableCell>
                                        <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem', color: 'text.secondary' }}>
                                          {row.clockIn ? new Date(row.clockIn).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '—'}
                                        </TableCell>
                                        <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem', color: 'text.secondary' }}>
                                          {row.clockOut ? new Date(row.clockOut).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '—'}
                                        </TableCell>
                                        <TableCell sx={{ py: 1 }}>
                                          {(() => {
                                            const col = getStatusColor(row.status);
                                            return (
                                              <Chip
                                                label={col.label}
                                                size="small"
                                                sx={{
                                                  bgcolor: col.bg,
                                                  color: col.text,
                                                  fontWeight: 700,
                                                  fontSize: '0.68rem',
                                                  height: 20,
                                                  borderRadius: '4px',
                                                  fontFamily: 'Outfit, sans-serif',
                                                }}
                                              />
                                            );
                                          })()}
                                        </TableCell>
                                        <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem', color: row.overtimeMinutes > 0 ? 'success.main' : 'text.disabled', fontWeight: row.overtimeMinutes > 0 ? 600 : 400 }}>
                                          {row.overtimeMinutes > 0 ? `${row.overtimeMinutes}m` : '—'}
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
                  )
              )}

              {/* TAB 1: TEAM PRESENCE (MANAGER / PRIVILEGED ONLY) */}
              {activeTab === 1 && showManagerTab && (
                <Box>
                  {/* Today Counts Summary row */}
                  <Grid container spacing={3.5} sx={{ mb: 4 }}>
                    {[
                      { val: teamCounts['PRESENT'] || 0, label: 'Present Today', color: 'success.main' },
                      { val: teamCounts['LATE'] || 0, label: 'Late Entries Today', color: 'warning.main' },
                      { val: teamCounts['WFH'] || 0, label: 'Remote Today', color: 'purple' },
                      { val: teamCounts['ON_LEAVE'] || 0, label: 'On Leave Today', color: 'primary.main' },
                      { val: teamCounts['ABSENT'] || 0, label: 'Absent Today', color: 'error.main' },
                    ].map((stat, i) => (
                      <Grid size={{ xs: 6, sm: 2.4 }} key={i}>
                        <Paper
                          elevation={0}
                          sx={{
                            border: '1px solid',
                            borderColor: 'divider',
                            borderRadius: '8px',
                            p: 2,
                            textAlign: 'center',
                          }}
                        >
                          <Typography
                            variant="h4"
                            sx={{
                              fontWeight: 800,
                              fontFamily: 'Outfit, sans-serif',
                              color: stat.color,
                              fontSize: '1.5rem',
                            }}
                          >
                            {stat.val}
                          </Typography>
                          <Typography variant="body2" sx={{ color: 'text.secondary', fontWeight: 500, fontFamily: 'Inter, sans-serif', mt: 0.5 }}>
                            {stat.label}
                          </Typography>
                        </Paper>
                      </Grid>
                    ))}
                  </Grid>

                  {/* Presence Table & Report download card */}
                  <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '8px' }}>
                    <CardContent sx={{ p: 3 }}>
                      {/* Section Title with Date filters and Export CSV button */}
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3.5, flexWrap: 'wrap', gap: 2 }}>
                        <Typography variant="h6" sx={{ fontWeight: 800, fontFamily: 'Outfit, sans-serif', fontSize: '1rem', display: 'flex', alignItems: 'center', gap: 1 }}>
                          <AccessTimeRoundedIcon fontSize="small" sx={{ color: 'text.secondary' }} />
                          Team Presence (Today)
                        </Typography>

                        {/* Export Form */}
                        <Box sx={{ display: 'flex', flexDirection: 'row', gap: 1.5, alignItems: 'center' }}>
                          <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Export Logs:</Typography>
                          <TextField
                            type="date"
                            size="small"
                            value={teamHistoryStart}
                            onChange={(e) => setTeamHistoryStart(e.target.value)}
                            sx={{ '& .MuiInputBase-input': { fontSize: '0.8rem', py: 0.7 } }}
                          />
                          <Typography variant="caption" sx={{ color: 'text.disabled' }}>to</Typography>
                          <TextField
                            type="date"
                            size="small"
                            value={teamHistoryEnd}
                            onChange={(e) => setTeamHistoryEnd(e.target.value)}
                            sx={{ '& .MuiInputBase-input': { fontSize: '0.8rem', py: 0.7 } }}
                          />
                          <Button
                            id="btn-export-csv"
                            variant="contained"
                            color="primary"
                            size="small"
                            onClick={handleExportCSV}
                            disabled={actionLoading}
                            startIcon={<DownloadRoundedIcon />}
                            sx={{
                              textTransform: 'none',
                              fontWeight: 700,
                              fontFamily: 'Outfit, sans-serif',
                              borderRadius: '6px',
                              py: 0.7,
                              boxShadow: 'none',
                              '&:hover': { boxShadow: 'none' },
                            }}
                          >
                            CSV Report
                          </Button>
                        </Box>
                      </Box>

                      {/* Today Logged Presence list */}
                      <TableContainer sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '6px', overflow: 'hidden' }}>
                        <Table size="small">
                          <TableHead sx={{ bgcolor: '#F8FAFC' }}>
                            <TableRow>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Employee Name</TableCell>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Clock In</TableCell>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Clock Out</TableCell>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Status</TableCell>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Work Mode</TableCell>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Late Mins</TableCell>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Overtime Mins</TableCell>
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            {teamToday.length === 0 ? (
                              <TableRow>
                                <TableCell colSpan={7} align="center" sx={{ py: 4, color: 'text.secondary', fontFamily: 'Inter, sans-serif', fontSize: '0.85rem' }}>
                                  No team clock ins logged today.
                                </TableCell>
                              </TableRow>
                            ) : (
                              teamToday.map((row) => (
                                <TableRow key={row.id} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                                  <TableCell sx={{ fontFamily: 'Outfit, sans-serif', fontSize: '0.82rem', fontWeight: 600 }}>{row.employeeName}</TableCell>
                                  <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem', color: 'text.secondary' }}>
                                    {row.clockIn ? new Date(row.clockIn).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '—'}
                                  </TableCell>
                                  <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem', color: 'text.secondary' }}>
                                    {row.clockOut ? new Date(row.clockOut).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '—'}
                                  </TableCell>
                                  <TableCell sx={{ py: 1 }}>
                                    {(() => {
                                      const col = getStatusColor(row.status);
                                      return (
                                        <Chip
                                          label={col.label}
                                          size="small"
                                          sx={{
                                            bgcolor: col.bg,
                                            color: col.text,
                                            fontWeight: 700,
                                            fontSize: '0.68rem',
                                            height: 20,
                                            borderRadius: '4px',
                                            fontFamily: 'Outfit, sans-serif',
                                          }}
                                        />
                                      );
                                    })()}
                                  </TableCell>
                                  <TableCell sx={{ py: 1 }}>
                                    <Chip
                                      label={row.workMode}
                                      variant="outlined"
                                      size="small"
                                      sx={{
                                        fontSize: '0.65rem',
                                        height: 18,
                                        borderRadius: '4px',
                                        color: row.workMode === 'REMOTE' ? 'purple' : 'text.secondary',
                                        borderColor: row.workMode === 'REMOTE' ? '#E1D5F5' : 'divider',
                                      }}
                                    />
                                  </TableCell>
                                  <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem', color: row.lateMinutes > 0 ? 'warning.main' : 'text.disabled', fontWeight: row.lateMinutes > 0 ? 600 : 400 }}>
                                    {row.lateMinutes > 0 ? `${row.lateMinutes}m` : '—'}
                                  </TableCell>
                                  <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem', color: row.overtimeMinutes > 0 ? 'success.main' : 'text.disabled', fontWeight: row.overtimeMinutes > 0 ? 600 : 400 }}>
                                    {row.overtimeMinutes > 0 ? `${row.overtimeMinutes}m` : '—'}
                                  </TableCell>
                                </TableRow>
                              ))
                            )}
                          </TableBody>
                        </Table>
                      </TableContainer>
                    </CardContent>
                  </Card>
                </Box>
              )}

              {/* TAB 2: ATTENDANCE RULES & POLICIES (ADMIN ONLY) */}
              {activeTab === 2 && showAdminTab && (
                <Grid container spacing={3.5}>
                  <Grid size={12}>
                    <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '8px' }}>
                      <CardContent sx={{ p: 3.5 }}>
                        <Typography variant="h6" sx={{ fontWeight: 800, fontFamily: 'Outfit, sans-serif', fontSize: '1rem', display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
                          <AdminPanelSettingsRoundedIcon fontSize="small" sx={{ color: 'text.secondary' }} />
                          Shift Timing Policies Configuration
                        </Typography>

                        <form onSubmit={handleSavePolicy}>
                          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3.5 }}>
                            <Grid container spacing={2}>
                              <Grid size={{ xs: 12, sm: 6 }}>
                                <TextField
                                  label="Shift Start Time"
                                  type="time"
                                  fullWidth
                                  required
                                  value={policy.shiftStartTime}
                                  onChange={(e) => setPolicy({ ...policy, shiftStartTime: e.target.value.length === 5 ? `${e.target.value}:00` : e.target.value })}
                                  slotProps={{
                                    inputLabel: { shrink: true }
                                  }}
                                  helperText="Default time after which late grace period checks occur."
                                  sx={{ '& label': { fontFamily: 'Outfit, sans-serif' }, '& input': { fontFamily: 'Inter, sans-serif' } }}
                                />
                              </Grid>
                              <Grid size={{ xs: 12, sm: 6 }}>
                                <TextField
                                  label="Shift End Time"
                                  type="time"
                                  fullWidth
                                  required
                                  value={policy.shiftEndTime}
                                  onChange={(e) => setPolicy({ ...policy, shiftEndTime: e.target.value.length === 5 ? `${e.target.value}:00` : e.target.value })}
                                  slotProps={{
                                    inputLabel: { shrink: true }
                                  }}
                                  helperText="Default check-out target shift close timing limit."
                                  sx={{ '& label': { fontFamily: 'Outfit, sans-serif' }, '& input': { fontFamily: 'Inter, sans-serif' } }}
                                />
                              </Grid>
                            </Grid>

                            <Grid container spacing={2}>
                              <Grid size={{ xs: 12, sm: 6 }}>
                                <TextField
                                  label="Grace Period (Minutes)"
                                  type="number"
                                  fullWidth
                                  required
                                  value={policy.gracePeriodMinutes}
                                  onChange={(e) => setPolicy({ ...policy, gracePeriodMinutes: parseInt(e.target.value) || 0 })}
                                  slotProps={{
                                    input: { inputProps: { min: 0 } }
                                  }}
                                  helperText="Grace delay allowed before punches are classified as LATE."
                                  sx={{ '& label': { fontFamily: 'Outfit, sans-serif' }, '& input': { fontFamily: 'Inter, sans-serif' } }}
                                />
                              </Grid>
                              <Grid size={{ xs: 12, sm: 6 }}>
                                <TextField
                                  label="Overtime Grace Threshold (Minutes)"
                                  type="number"
                                  fullWidth
                                  required
                                  value={policy.overtimeThresholdMinutes}
                                  onChange={(e) => setPolicy({ ...policy, overtimeThresholdMinutes: parseInt(e.target.value) || 0 })}
                                  slotProps={{
                                    input: { inputProps: { min: 0 } }
                                  }}
                                  helperText="Minutes worked beyond shift close to start accruing overtime."
                                  sx={{ '& label': { fontFamily: 'Outfit, sans-serif' }, '& input': { fontFamily: 'Inter, sans-serif' } }}
                                />
                              </Grid>
                            </Grid>

                            <Box sx={{ display: 'flex', justifyContent: 'flex-end', pt: 1 }}>
                              <Button
                                id="btn-save-policy"
                                type="submit"
                                variant="contained"
                                color="primary"
                                disabled={actionLoading}
                                sx={{
                                  textTransform: 'none',
                                  fontWeight: 700,
                                  fontFamily: 'Outfit, sans-serif',
                                  borderRadius: '6px',
                                  px: 4,
                                  py: 1,
                                  boxShadow: 'none',
                                  '&:hover': { boxShadow: 'none' },
                                }}
                              >
                                Save Settings
                              </Button>
                            </Box>
                          </Box>
                        </form>
                      </CardContent>
                    </Card>
                  </Grid>
                </Grid>
              )}
            </>
          )}
        </Box>
      </Paper>
    </Box>
  );
};

export default AttendanceDashboard;
