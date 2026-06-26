import {
  Dialog, DialogTitle, DialogContent, DialogContentText,
  DialogActions, Button, CircularProgress, Box,
} from '@mui/material';
import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';

// ─── Confirm Dialog ───────────────────────────────────────────────────────────
interface ConfirmDialogProps {
  open: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  onConfirm: () => void;
  onCancel: () => void;
  loading?: boolean;
}

export const ConfirmDialog = ({
  open, title, message, confirmLabel = 'Delete',
  onConfirm, onCancel, loading = false,
}: ConfirmDialogProps) => (
  <Dialog open={open} onClose={onCancel} maxWidth="xs" fullWidth>
    <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <WarningAmberRoundedIcon color="warning" />
      {title}
    </DialogTitle>
    <DialogContent>
      <DialogContentText>{message}</DialogContentText>
    </DialogContent>
    <DialogActions sx={{ px: 3, pb: 2 }}>
      <Button onClick={onCancel} disabled={loading}>Cancel</Button>
      <Button
        onClick={onConfirm}
        variant="contained"
        color="error"
        disabled={loading}
        startIcon={loading ? <CircularProgress size={16} /> : undefined}
      >
        {loading ? 'Deleting…' : confirmLabel}
      </Button>
    </DialogActions>
  </Dialog>
);

// ─── Loading Spinner ──────────────────────────────────────────────────────────
export const LoadingSpinner = ({ minHeight = '60vh' }: { minHeight?: string }) => (
  <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight }}>
    <CircularProgress size={48} thickness={4} />
  </Box>
);
