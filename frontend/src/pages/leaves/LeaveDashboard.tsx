import { useEffect, useState } from 'react';
import {
  Box, Typography, Grid, Card, CardContent, Button, Tabs, Tab,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, Chip, TextField, MenuItem, Alert, CircularProgress,
} from '@mui/material';
import SendRoundedIcon from '@mui/icons-material/SendRounded';
import HourglassEmptyRoundedIcon from '@mui/icons-material/HourglassEmptyRounded';
import CheckCircleRoundedIcon from '@mui/icons-material/CheckCircleRounded';
import CancelRoundedIcon from '@mui/icons-material/CancelRounded';
import AddCircleOutlineRoundedIcon from '@mui/icons-material/AddCircleOutlineRounded';
import HistoryRoundedIcon from '@mui/icons-material/HistoryRounded';

import { useAuth } from '../../context/AuthContext';
import { leaveService } from '../../services/leaveService';
import type { LeaveRequest, LeaveType } from '../../types';

const LEAVE_TYPES: { value: LeaveType; label: string }[] = [
  { value: 'CASUAL', label: 'Casual Leave' },
  { value: 'SICK', label: 'Sick Leave' },
  { value: 'WFH', label: 'Work From Home (WFH)' },
  { value: 'PERMISSION', label: 'Short Permission' },
];

const getStatusColor = (status: string) => {
  switch (status) {
    case 'APPROVED': return { bg: '#E2FBE9', text: '#0F7336', label: 'Approved' };
    case 'PENDING': return { bg: '#FFF4E5', text: '#B76E00', label: 'Pending' };
    case 'REJECTED': return { bg: '#FEEBEE', text: '#C62828', label: 'Rejected' };
    default: return { bg: '#F8FAFC', text: '#64748B', label: status };
  }
};

const LeaveDashboard = () => {
  const { user, isManager, isAdmin, isHR } = useAuth();
  const showApprovalsTab = isManager() || isAdmin() || isHR();

  // Tab State: 0 = My Leaves, 1 = Pending Reviews, 2 = Leave History
  const [activeTab, setActiveTab] = useState(0);

  // ─── My Leaves State ────────────────────────────────────────────────────────
  const [myHistory, setMyHistory] = useState<LeaveRequest[]>([]);
  const [formOpen, setFormOpen] = useState(false);
  const [newRequest, setNewRequest] = useState<Partial<LeaveRequest>>({
    leaveType: 'CASUAL',
    startDate: '',
    endDate: '',
    reason: '',
  });

  // ─── Pending Approvals State ────────────────────────────────────────────────
  const [pendingRequests, setPendingRequests] = useState<LeaveRequest[]>([]);

  // ─── History State (Manager / Admin Only) ───────────────────────────────────
  const [teamHistory, setTeamHistory] = useState<LeaveRequest[]>([]);

  // UI
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [msg, setMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const fetchMyLeaves = async () => {
    if (!user?.employeeId) {
      setMyHistory([]);
      return;
    }
    try {
      setLoading(true);
      const data = await leaveService.getPersonalLeaveHistory();
      setMyHistory(data);
    } catch (err: any) {
      console.error(err);
      setMsg({ type: 'error', text: err.response?.data?.message || 'Failed to load leave history.' });
    } finally {
      setLoading(false);
    }
  };

  const fetchPendingRequests = async () => {
    try {
      setLoading(true);
      const data = await leaveService.getPendingLeaveRequests();
      setPendingRequests(data);
    } catch (err: any) {
      console.error(err);
      setMsg({ type: 'error', text: err.response?.data?.message || 'Failed to load pending requests.' });
    } finally {
      setLoading(false);
    }
  };

  const fetchTeamHistory = async () => {
    try {
      setLoading(true);
      const data = await leaveService.getTeamLeaveHistory();
      setTeamHistory(data);
    } catch (err: any) {
      console.error(err);
      setMsg({ type: 'error', text: err.response?.data?.message || 'Failed to load team leave history.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setMsg(null);
    if (activeTab === 0) {
      fetchMyLeaves();
    } else if (activeTab === 1 && showApprovalsTab) {
      fetchPendingRequests();
    } else if (activeTab === 2 && showApprovalsTab) {
      fetchTeamHistory();
    }
  }, [activeTab, user?.employeeId]);

  // Submit Leave Request
  const handleSubmitLeave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setActionLoading(true);
      setMsg(null);

      if (newRequest.startDate && newRequest.endDate && newRequest.startDate > newRequest.endDate) {
        setMsg({ type: 'error', text: 'Start date cannot be after end date.' });
        return;
      }

      await leaveService.applyLeave(newRequest as LeaveRequest);
      setMsg({ type: 'success', text: 'Request submitted successfully!' });
      
      // Reset form and close
      setNewRequest({
        leaveType: 'CASUAL',
        startDate: '',
        endDate: '',
        reason: '',
      });
      setFormOpen(false);

      // Refresh list
      const data = await leaveService.getPersonalLeaveHistory();
      setMyHistory(data);
    } catch (err: any) {
      setMsg({ type: 'error', text: err.response?.data?.message || 'Failed to submit leave request.' });
    } finally {
      setActionLoading(false);
    }
  };

  // Actions: Approve
  const handleApprove = async (id: number) => {
    try {
      setActionLoading(true);
      setMsg(null);
      await leaveService.approveLeave(id);
      setMsg({ type: 'success', text: 'Request approved successfully.' });
      
      // Refresh list
      const data = await leaveService.getPendingLeaveRequests();
      setPendingRequests(data);
    } catch (err: any) {
      setMsg({ type: 'error', text: err.response?.data?.message || 'Failed to approve request.' });
    } finally {
      setActionLoading(false);
    }
  };

  // Actions: Reject
  const handleReject = async (id: number) => {
    try {
      setActionLoading(true);
      setMsg(null);
      await leaveService.rejectLeave(id);
      setMsg({ type: 'success', text: 'Request rejected successfully.' });
      
      // Refresh list
      const data = await leaveService.getPendingLeaveRequests();
      setPendingRequests(data);
    } catch (err: any) {
      setMsg({ type: 'error', text: err.response?.data?.message || 'Failed to reject request.' });
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
            Leaves & Outages
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5, fontFamily: 'Inter, sans-serif' }}>
            Submit time off, plan WFH schedules, and manage team approvals.
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
          <Tab label="My Requests" id="tab-my-leaves" />
          {showApprovalsTab && <Tab label="Pending Approvals" id="tab-leave-approvals" />}
          {showApprovalsTab && <Tab label="Leave History" id="tab-leave-history" />}
        </Tabs>

        {/* Alerts */}
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
              {/* TAB 0: MY REQUESTS */}
              {activeTab === 0 && (
                !user?.employeeId ? (
                  <Box sx={{ p: 4, textAlign: 'center', border: '1px solid', borderColor: 'divider', borderRadius: '8px', bgcolor: '#FAFBFD' }}>
                    <HourglassEmptyRoundedIcon sx={{ fontSize: '3rem', color: 'text.disabled', mb: 2 }} />
                    <Typography variant="h6" sx={{ fontWeight: 800, fontFamily: 'Outfit, sans-serif', mb: 1 }}>
                      No Employee Profile Linked
                    </Typography>
                    <Typography variant="body2" sx={{ color: 'text.secondary', fontFamily: 'Inter, sans-serif', maxWidth: '480px', mx: 'auto' }}>
                      This administrator/user account is not linked to an employee profile. Personal leave applications and history records are not applicable.
                    </Typography>
                  </Box>
                ) : (
                  <Grid container spacing={3.5}>
                    {/* Left Form Trigger or Form Block */}
                    <Grid size={{ xs: 12, md: formOpen ? 4.5 : 12 }}>
                      {!formOpen ? (
                        <Paper
                          elevation={0}
                          sx={{
                            border: '1px dashed',
                            borderColor: 'divider',
                            borderRadius: '8px',
                            p: 4,
                            textAlign: 'center',
                            cursor: 'pointer',
                            '&:hover': { bgcolor: '#F8FAFC', borderColor: 'primary.main' },
                            transition: 'all 0.15s',
                          }}
                          onClick={() => setFormOpen(true)}
                        >
                          <AddCircleOutlineRoundedIcon sx={{ fontSize: '2.5rem', color: 'text.disabled', mb: 1.5 }} />
                          <Typography variant="subtitle1" sx={{ fontWeight: 800, fontFamily: 'Outfit, sans-serif', mb: 0.5 }}>
                            Apply Leave or WFH
                          </Typography>
                          <Typography variant="body2" sx={{ color: 'text.secondary', fontFamily: 'Inter, sans-serif', fontSize: '0.82rem' }}>
                            Submit a sick leave, vacation request, short permission, or remote work schedule.
                          </Typography>
                        </Paper>
                      ) : (
                        <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '8px', bgcolor: '#FAFAFB' }}>
                          <CardContent sx={{ p: 3.5 }}>
                            <Typography variant="subtitle1" sx={{ fontWeight: 800, fontFamily: 'Outfit, sans-serif', mb: 3 }}>
                              Submit Request
                            </Typography>

                            <form onSubmit={handleSubmitLeave}>
                              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                                <TextField
                                  select
                                  label="Leave Type"
                                  fullWidth
                                  required
                                  value={newRequest.leaveType}
                                  onChange={(e) => setNewRequest({ ...newRequest, leaveType: e.target.value as LeaveType })}
                                  sx={{ '& label': { fontFamily: 'Outfit, sans-serif' }, '& .MuiSelect-select': { fontFamily: 'Inter, sans-serif' } }}
                                >
                                  {LEAVE_TYPES.map((opt) => (
                                    <MenuItem key={opt.value} value={opt.value} sx={{ fontFamily: 'Inter, sans-serif' }}>
                                      {opt.label}
                                    </MenuItem>
                                  ))}
                                </TextField>

                                <Grid container spacing={2}>
                                  <Grid size={{ xs: 12, sm: 6 }}>
                                    <TextField
                                      label="Start Date"
                                      type="date"
                                      fullWidth
                                      required
                                      value={newRequest.startDate}
                                      onChange={(e) => setNewRequest({ ...newRequest, startDate: e.target.value })}
                                      slotProps={{
                                        inputLabel: { shrink: true }
                                      }}
                                      sx={{ '& label': { fontFamily: 'Outfit, sans-serif' }, '& input': { fontFamily: 'Inter, sans-serif' } }}
                                    />
                                  </Grid>
                                  <Grid size={{ xs: 12, sm: 6 }}>
                                    <TextField
                                      label="End Date"
                                      type="date"
                                      fullWidth
                                      required
                                      value={newRequest.endDate}
                                      onChange={(e) => setNewRequest({ ...newRequest, endDate: e.target.value })}
                                      slotProps={{
                                        inputLabel: { shrink: true }
                                      }}
                                      sx={{ '& label': { fontFamily: 'Outfit, sans-serif' }, '& input': { fontFamily: 'Inter, sans-serif' } }}
                                    />
                                  </Grid>
                                </Grid>

                                <TextField
                                  label="Reason / Notes"
                                  multiline
                                  rows={3}
                                  fullWidth
                                  required
                                  value={newRequest.reason}
                                  onChange={(e) => setNewRequest({ ...newRequest, reason: e.target.value })}
                                  placeholder="State reason or brief project handover details..."
                                  sx={{ '& label': { fontFamily: 'Outfit, sans-serif' }, '& .MuiInputBase-input': { fontFamily: 'Inter, sans-serif' } }}
                                />

                                <Box sx={{ display: 'flex', flexDirection: 'row', gap: 2, justifyContent: 'flex-end' }}>
                                  <Button
                                    variant="outlined"
                                    onClick={() => setFormOpen(false)}
                                    disabled={actionLoading}
                                    sx={{ textTransform: 'none', fontWeight: 700, fontFamily: 'Outfit, sans-serif', borderRadius: '6px' }}
                                  >
                                    Cancel
                                  </Button>
                                  <Button
                                    id="btn-submit-leave"
                                    type="submit"
                                    variant="contained"
                                    color="primary"
                                    disabled={actionLoading}
                                    startIcon={<SendRoundedIcon fontSize="small" />}
                                    sx={{
                                      textTransform: 'none',
                                      fontWeight: 700,
                                      fontFamily: 'Outfit, sans-serif',
                                      borderRadius: '6px',
                                      boxShadow: 'none',
                                      '&:hover': { boxShadow: 'none' },
                                    }}
                                  >
                                    Submit
                                  </Button>
                                </Box>
                              </Box>
                            </form>
                          </CardContent>
                        </Card>
                      )}
                    </Grid>

                    {/* History List */}
                    <Grid size={{ xs: 12, md: formOpen ? 7.5 : 12 }}>
                      <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '8px' }}>
                        <CardContent sx={{ p: 3 }}>
                          <Typography variant="h6" sx={{ fontWeight: 800, fontFamily: 'Outfit, sans-serif', fontSize: '1rem', mb: 3 }}>
                            My Request History
                          </Typography>

                          <TableContainer sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '6px', overflow: 'hidden' }}>
                            <Table size="small">
                              <TableHead sx={{ bgcolor: '#F8FAFC' }}>
                                <TableRow>
                                  <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Leave Type</TableCell>
                                  <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Duration</TableCell>
                                  <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Reason</TableCell>
                                  <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Status</TableCell>
                                  <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Approved By</TableCell>
                                </TableRow>
                              </TableHead>
                              <TableBody>
                                {myHistory.length === 0 ? (
                                  <TableRow>
                                    <TableCell colSpan={5} align="center" sx={{ py: 4, color: 'text.secondary', fontFamily: 'Inter, sans-serif', fontSize: '0.85rem' }}>
                                      No requests applied yet.
                                    </TableCell>
                                  </TableRow>
                                ) : (
                                  myHistory.map((row) => (
                                    <TableRow key={row.id} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                                      <TableCell sx={{ fontFamily: 'Outfit, sans-serif', fontSize: '0.82rem', fontWeight: 600 }}>
                                        {row.leaveType}
                                      </TableCell>
                                      <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem', color: 'text.secondary' }}>
                                        {row.startDate} to {row.endDate}
                                      </TableCell>
                                      <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem', maxWidth: '180px', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                                        {row.reason}
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
                                      <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem', color: 'text.secondary' }}>
                                        {row.approvedByUsername || '—'}
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

              {/* TAB 1: PENDING APPROVALS */}
              {activeTab === 1 && showApprovalsTab && (
                <Box>
                  <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '8px' }}>
                    <CardContent sx={{ p: 3 }}>
                      <Typography variant="h6" sx={{ fontWeight: 800, fontFamily: 'Outfit, sans-serif', fontSize: '1rem', mb: 3, display: 'flex', alignItems: 'center', gap: 1 }}>
                        <HourglassEmptyRoundedIcon fontSize="small" sx={{ color: 'text.secondary' }} />
                        Pending Leave Requests Review
                      </Typography>

                      <TableContainer sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '6px', overflow: 'hidden' }}>
                        <Table size="small">
                          <TableHead sx={{ bgcolor: '#F8FAFC' }}>
                            <TableRow>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Employee Name</TableCell>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Type</TableCell>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Duration</TableCell>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Reason</TableCell>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2, width: '180px' }} align="right">Actions</TableCell>
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            {pendingRequests.length === 0 ? (
                              <TableRow>
                                <TableCell colSpan={5} align="center" sx={{ py: 4, color: 'text.secondary', fontFamily: 'Inter, sans-serif', fontSize: '0.85rem' }}>
                                  No pending requests currently require review.
                                </TableCell>
                              </TableRow>
                            ) : (
                              pendingRequests.map((row) => (
                                <TableRow key={row.id} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                                  <TableCell sx={{ fontFamily: 'Outfit, sans-serif', fontSize: '0.82rem', fontWeight: 600 }}>{row.employeeName}</TableCell>
                                  <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem' }}>
                                    <Chip
                                      label={row.leaveType}
                                      variant="outlined"
                                      size="small"
                                      sx={{ fontSize: '0.65rem', height: 18, borderRadius: '4px', textTransform: 'uppercase' }}
                                    />
                                  </TableCell>
                                  <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem', color: 'text.secondary' }}>
                                    {row.startDate} to {row.endDate}
                                  </TableCell>
                                  <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem', maxWidth: '250px', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                                    {row.reason}
                                  </TableCell>
                                  <TableCell align="right" sx={{ py: 1 }}>
                                    <Box sx={{ display: 'flex', flexDirection: 'row', gap: 1, justifyContent: 'flex-end' }}>
                                      <Button
                                        variant="outlined"
                                        color="success"
                                        size="small"
                                        onClick={() => row.id && handleApprove(row.id)}
                                        disabled={actionLoading}
                                        startIcon={<CheckCircleRoundedIcon fontSize="small" />}
                                        sx={{ textTransform: 'none', fontWeight: 700, fontFamily: 'Outfit, sans-serif', borderRadius: '4px', fontSize: '0.72rem', py: 0.4 }}
                                      >
                                        Approve
                                      </Button>
                                      <Button
                                        variant="outlined"
                                        color="error"
                                        size="small"
                                        onClick={() => row.id && handleReject(row.id)}
                                        disabled={actionLoading}
                                        startIcon={<CancelRoundedIcon fontSize="small" />}
                                        sx={{ textTransform: 'none', fontWeight: 700, fontFamily: 'Outfit, sans-serif', borderRadius: '4px', fontSize: '0.72rem', py: 0.4 }}
                                      >
                                        Reject
                                      </Button>
                                    </Box>
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

              {/* TAB 2: LEAVE HISTORY (MANAGER / PRIVILEGED ONLY) */}
              {activeTab === 2 && showApprovalsTab && (
                <Box>
                  <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '8px' }}>
                    <CardContent sx={{ p: 3 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                        <Typography variant="h6" sx={{ fontWeight: 800, fontFamily: 'Outfit, sans-serif', fontSize: '1rem', display: 'flex', alignItems: 'center', gap: 1 }}>
                          <HistoryRoundedIcon fontSize="small" sx={{ color: 'text.secondary' }} />
                          {isAdmin() ? 'Global Leaves & WFH History' : 'Team Leaves & WFH History'}
                        </Typography>
                        <Button
                          variant="outlined"
                          size="small"
                          onClick={fetchTeamHistory}
                          sx={{ textTransform: 'none', fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', borderRadius: '6px', py: 0.5 }}
                        >
                          Refresh Logs
                        </Button>
                      </Box>

                      <TableContainer sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '6px', overflow: 'hidden' }}>
                        <Table size="small">
                          <TableHead sx={{ bgcolor: '#F8FAFC' }}>
                            <TableRow>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Employee Name</TableCell>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Type</TableCell>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Duration</TableCell>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Reason</TableCell>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Status</TableCell>
                              <TableCell sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', fontSize: '0.8rem', py: 1.2 }}>Approved By</TableCell>
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            {teamHistory.length === 0 ? (
                              <TableRow>
                                <TableCell colSpan={6} align="center" sx={{ py: 4, color: 'text.secondary', fontFamily: 'Inter, sans-serif', fontSize: '0.85rem' }}>
                                  No leave history logs found.
                                </TableCell>
                              </TableRow>
                            ) : (
                              teamHistory.map((row) => (
                                <TableRow key={row.id} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                                  <TableCell sx={{ fontFamily: 'Outfit, sans-serif', fontSize: '0.82rem', fontWeight: 600 }}>{row.employeeName}</TableCell>
                                  <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem' }}>
                                    <Chip
                                      label={row.leaveType}
                                      variant="outlined"
                                      size="small"
                                      sx={{ fontSize: '0.65rem', height: 18, borderRadius: '4px', textTransform: 'uppercase' }}
                                    />
                                  </TableCell>
                                  <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem', color: 'text.secondary' }}>
                                    {row.startDate} to {row.endDate}
                                  </TableCell>
                                  <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem', maxWidth: '200px', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                                    {row.reason}
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
                                  <TableCell sx={{ fontFamily: 'Inter, sans-serif', fontSize: '0.8rem', color: 'text.secondary' }}>
                                    {row.approvedByUsername || '—'}
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
            </>
          )}
        </Box>
      </Paper>
    </Box>
  );
};

export default LeaveDashboard;
