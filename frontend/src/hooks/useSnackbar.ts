import { useState, useCallback } from 'react';
import type { AlertColor } from '@mui/material';

export interface SnackbarState {
  open: boolean;
  message: string;
  severity: AlertColor;
}

/**
 * Custom hook to manage MUI Snackbar states.
 */
export const useSnackbar = () => {
  const [state, setState] = useState<SnackbarState>({
    open: false,
    message: '',
    severity: 'success',
  });

  const showSnackbar = useCallback((message: string, severity: AlertColor = 'success') => {
    setState({
      open: true,
      message,
      severity,
    });
  }, []);

  const closeSnackbar = useCallback(() => {
    setState((prev) => ({ ...prev, open: false }));
  }, []);

  return {
    snackbarProps: state,
    showSnackbar,
    closeSnackbar,
  };
};
