import { useEffect, useState, useCallback } from 'react';
import {
  Box, Typography, Button, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, IconButton, Tooltip, TextField,
  InputAdornment, Dialog, DialogTitle, DialogContent, DialogContentText,
  DialogActions, CircularProgress, Divider, Alert,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import AddRoundedIcon           from '@mui/icons-material/AddRounded';
import DeleteOutlineRoundedIcon from '@mui/icons-material/DeleteOutlineRounded';
import KeyRoundedIcon           from '@mui/icons-material/KeyRounded';
import SearchRoundedIcon        from '@mui/icons-material/SearchRounded';
import PersonOffOutlinedIcon    from '@mui/icons-material/PersonOffOutlined';
import LinkRoundedIcon          from '@mui/icons-material/LinkRounded';
import { userService }          from '../../services/userService';
import { useAuth }              from '../../context/AuthContext';
import type { UserRecord }      from '../../types';

/* ───────── helpers ───────── */
const ROLE_LABELS: Record<string, string> = {
  ROLE_ADMIN:    'Admin',
  ROLE_HR:       'HR',
  ROLE_MANAGER:  'Manager',
  ROLE_EMPLOYEE: 'Employee',
};

const ROLE_COLORS: Record<string, 'error' | 'warning' | 'info' | 'default'> = {
  ROLE_ADMIN:    'error',
  ROLE_HR:       'warning',
  ROLE_MANAGER:  'info',
  ROLE_EMPLOYEE: 'default',
};

/* ───────── Reset Password Dialog ───────── */
interface ResetDialogProps {
  open: boolean;
  user: UserRecord | null;
  onClose: () => void;
  onConfirm: (newPassword: string) => Promise<void>;
}

const ResetPasswordDialog = ({ open, user, onClose, onConfirm }: ResetDialogProps) => {
  const [pwd, setPwd]       = useState('');
  const [saving, setSaving] = useState(false);
  const [err, setErr]       = useState('');

  const handleConfirm = async () => {
    if (pwd.length < 6) { setErr('Password must be at least 6 characters'); return; }
    setSaving(true);
    try {
      await onConfirm(pwd);
      setPwd(''); setErr(''); onClose();
    } catch {
      setErr('Failed to reset password. Try again.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle sx={{ fontWeight: 600, fontFamily: 'Outfit, sans-serif' }}>
        Reset Password
      </DialogTitle>
      <Divider />
      <DialogContent sx={{ pt: 2.5 }}>
        <DialogContentText sx={{ mb: 2, fontSize: '0.875rem' }}>
          Set a new password for <strong>{user?.username}</strong>.
        </DialogContentText>
        {err && <Alert severity="error" sx={{ mb: 2, borderRadius: '6px' }}>{err}</Alert>}
        <TextField
          label="New Password"
          type="password"
          value={pwd}
          onChange={(e) => { setPwd(e.target.value); setErr(''); }}
          autoFocus
          fullWidth
        />
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2.5 }}>
        <Button onClick={onClose} disabled={saving} sx={{ color: 'text.secondary' }}>
          Cancel
        </Button>
        <Button
          variant="contained"
          disableElevation
          onClick={handleConfirm}
          disabled={saving}
          sx={{ borderRadius: '6px' }}
        >
          {saving ? <CircularProgress size={18} color="inherit" /> : 'Update Password'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

/* ───────── Delete Confirm Dialog ───────── */
interface DeleteDialogProps {
  open: boolean;
  user: UserRecord | null;
  onClose: () => void;
  onConfirm: () => Promise<void>;
}

const DeleteDialog = ({ open, user, onClose, onConfirm }: DeleteDialogProps) => {
  const [deleting, setDeleting] = useState(false);
  const [err, setErr]           = useState('');

  const handleConfirm = async () => {
    setDeleting(true);
    try {
      await onConfirm();
      setErr(''); onClose();
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? 'Failed to delete user.');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle sx={{ fontWeight: 600, fontFamily: 'Outfit, sans-serif' }}>
        Delete User Account
      </DialogTitle>
      <Divider />
      <DialogContent sx={{ pt: 2.5 }}>
        {err && <Alert severity="error" sx={{ mb: 2, borderRadius: '6px' }}>{err}</Alert>}
        <DialogContentText sx={{ fontSize: '0.875rem' }}>
          Are you sure you want to delete <strong>{user?.username}</strong>? This action
          cannot be undone. The linked employee profile (if any) will remain intact.
        </DialogContentText>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2.5 }}>
        <Button onClick={onClose} disabled={deleting} sx={{ color: 'text.secondary' }}>
          Cancel
        </Button>
        <Button
          variant="contained"
          color="error"
          disableElevation
          onClick={handleConfirm}
          disabled={deleting}
          sx={{ borderRadius: '6px' }}
        >
          {deleting ? <CircularProgress size={18} color="inherit" /> : 'Delete'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

/* ─────────────────────────────────────────
   Main UserList Page
───────────────────────────────────────── */
const UserList = () => {
  const navigate = useNavigate();
  const { user: currentUser } = useAuth();

  const [users, setUsers]             = useState<UserRecord[]>([]);
  const [search, setSearch]           = useState('');
  const [loading, setLoading]         = useState(true);
  const [feedback, setFeedback]       = useState<{ msg: string; type: 'success' | 'error' } | null>(null);

  // dialogs
  const [resetTarget, setResetTarget] = useState<UserRecord | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<UserRecord | null>(null);

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    try {
      const page = await userService.getAll({ search: search || undefined, size: 50 });
      setUsers(page.content);
    } catch {
      setFeedback({ msg: 'Failed to load users.', type: 'error' });
    } finally {
      setLoading(false);
    }
  }, [search]);

  useEffect(() => {
    const t = setTimeout(fetchUsers, 300);
    return () => clearTimeout(t);
  }, [fetchUsers]);

  const handleResetPassword = async (newPassword: string) => {
    await userService.resetPassword(resetTarget!.id, newPassword);
    setFeedback({ msg: `Password for "${resetTarget!.username}" has been updated.`, type: 'success' });
  };

  const handleDelete = async () => {
    await userService.delete(deleteTarget!.id);
    setFeedback({ msg: `User "${deleteTarget!.username}" deleted.`, type: 'success' });
    fetchUsers();
  };

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 4 }}>
        <Box>
          <Typography
            variant="h5"
            sx={{ fontWeight: 700, fontFamily: 'Outfit, sans-serif', letterSpacing: '-0.5px' }}
          >
            User Accounts
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            Manage login credentials and role assignments
          </Typography>
        </Box>
        <Button
          id="create-user-btn"
          variant="contained"
          disableElevation
          startIcon={<AddRoundedIcon />}
          onClick={() => navigate('/users/new')}
          sx={{ borderRadius: '6px', fontWeight: 600 }}
        >
          Create User
        </Button>
      </Box>

      {/* Feedback banner */}
      {feedback && (
        <Alert
          severity={feedback.type}
          variant="outlined"
          onClose={() => setFeedback(null)}
          sx={{ mb: 2.5, borderRadius: '6px' }}
        >
          {feedback.msg}
        </Alert>
      )}

      {/* Search */}
      <TextField
        id="user-search"
        placeholder="Search by username or email…"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        sx={{ mb: 2.5, maxWidth: 380 }}
        slotProps={{
          input: {
            startAdornment: (
              <InputAdornment position="start">
                <SearchRoundedIcon fontSize="small" sx={{ color: 'text.disabled' }} />
              </InputAdornment>
            ),
          },
        }}
      />

      {/* Table */}
      <Paper
        elevation={0}
        sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '6px', overflow: 'hidden' }}
      >
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow sx={{ bgcolor: 'action.hover' }}>
                {['Username', 'Email', 'Role', 'Linked Employee', 'Created', 'Actions'].map((h, i) => (
                  <TableCell
                    key={h}
                    align={i === 5 ? 'right' : 'left'}
                    sx={{
                      fontSize: '0.72rem',
                      fontWeight: 600,
                      color: 'text.disabled',
                      textTransform: 'uppercase',
                      letterSpacing: '0.05em',
                      borderColor: 'divider',
                      py: 1.25,
                    }}
                  >
                    {h}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 6, borderBottom: 'none' }}>
                    <CircularProgress size={28} />
                  </TableCell>
                </TableRow>
              ) : users.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 6, borderBottom: 'none' }}>
                    <PersonOffOutlinedIcon sx={{ fontSize: 36, color: 'text.disabled', mb: 1, display: 'block', mx: 'auto' }} />
                    <Typography variant="body2" color="text.secondary">
                      No user accounts found.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                users.map((u) => {
                  const isSelf = u.username === currentUser?.username;
                  return (
                    <TableRow
                      key={u.id}
                      sx={{
                        '&:last-child td': { border: 0 },
                        '&:hover': { bgcolor: 'action.hover' },
                        opacity: isSelf ? 0.85 : 1,
                      }}
                    >
                      {/* Username */}
                      <TableCell sx={{ py: 1.5, borderColor: 'divider' }}>
                        <Typography variant="body2" sx={{ fontWeight: 600, fontSize: '0.85rem' }}>
                          {u.username}
                          {isSelf && (
                            <Chip
                              label="You"
                              size="small"
                              sx={{ ml: 1, height: 16, fontSize: '0.62rem', borderRadius: '4px' }}
                            />
                          )}
                        </Typography>
                      </TableCell>

                      {/* Email */}
                      <TableCell sx={{ py: 1.5, borderColor: 'divider', color: 'text.secondary', fontSize: '0.82rem' }}>
                        {u.email}
                      </TableCell>

                      {/* Roles */}
                      <TableCell sx={{ py: 1.5, borderColor: 'divider' }}>
                        <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                          {u.roles.map((r) => (
                            <Chip
                              key={r}
                              label={ROLE_LABELS[r] ?? r}
                              size="small"
                              color={ROLE_COLORS[r] ?? 'default'}
                              variant="outlined"
                              sx={{ height: 20, fontSize: '0.68rem', borderRadius: '4px', '& .MuiChip-label': { px: 0.75 } }}
                            />
                          ))}
                        </Box>
                      </TableCell>

                      {/* Linked Employee */}
                      <TableCell sx={{ py: 1.5, borderColor: 'divider', fontSize: '0.82rem' }}>
                        {u.linkedEmployeeName ? (
                          <Box
                            component="span"
                            sx={{
                              display: 'inline-flex', alignItems: 'center', gap: 0.5,
                              color: 'text.primary', cursor: 'pointer',
                              '&:hover': { textDecoration: 'underline' },
                            }}
                            onClick={() => navigate(`/employees/${u.linkedEmployeeId}`)}
                          >
                            <LinkRoundedIcon sx={{ fontSize: 14 }} />
                            {u.linkedEmployeeName}
                          </Box>
                        ) : (
                          <Typography variant="caption" color="text.disabled">—</Typography>
                        )}
                      </TableCell>

                      {/* Created */}
                      <TableCell sx={{ py: 1.5, borderColor: 'divider', color: 'text.secondary', fontSize: '0.82rem' }}>
                        {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}
                      </TableCell>

                      {/* Actions */}
                      <TableCell align="right" sx={{ py: 1.5, borderColor: 'divider' }}>
                        <Tooltip title="Reset Password">
                          <IconButton
                            size="small"
                            onClick={() => setResetTarget(u)}
                            sx={{ color: 'text.disabled', '&:hover': { color: 'primary.main' } }}
                          >
                            <KeyRoundedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title={isSelf ? 'Cannot delete your own account' : 'Delete User'}>
                          <span>
                            <IconButton
                              size="small"
                              disabled={isSelf}
                              onClick={() => setDeleteTarget(u)}
                              sx={{ color: 'text.disabled', '&:hover': { color: 'error.main' } }}
                            >
                              <DeleteOutlineRoundedIcon fontSize="small" />
                            </IconButton>
                          </span>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      {/* Dialogs */}
      <ResetPasswordDialog
        open={!!resetTarget}
        user={resetTarget}
        onClose={() => setResetTarget(null)}
        onConfirm={handleResetPassword}
      />
      <DeleteDialog
        open={!!deleteTarget}
        user={deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
      />
    </Box>
  );
};

export default UserList;
