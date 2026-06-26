import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary:   { main: '#0F172A', light: '#334155', dark: '#020617' }, // Slate 900
    secondary: { main: '#64748B', light: '#94A3B8', dark: '#475569' }, // Slate 500
    success:   { main: '#10B981', light: '#34D399', dark: '#059669' }, // Emerald 500
    warning:   { main: '#F59E0B', light: '#FBBF24', dark: '#D97706' }, // Amber 500
    error:     { main: '#EF4444', light: '#F87171', dark: '#DC2626' }, // Red 500
    info:      { main: '#3B82F6', light: '#60A5FA', dark: '#2563EB' }, // Blue 500
    background: { default: '#F8FAFC', paper: '#FFFFFF' },              // Soft slate background, white paper
    text:       { primary: '#0F172A', secondary: '#475569' },          // Ink and dark slate text
    divider:    '#E2E8F0',                                              // Crisp slate border lines
  },
  typography: {
    fontFamily: '"Outfit", "Inter", "Roboto", sans-serif',
    h4: { fontWeight: 700, letterSpacing: '-0.02em' },
    h5: { fontWeight: 600, letterSpacing: '-0.01em' },
    h6: { fontWeight: 600 },
    button: { fontWeight: 600, letterSpacing: '0.02em' },
  },
  shape: { borderRadius: 6 }, // Sharp, clean corners
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          borderRadius: 6,
          boxShadow: 'none',
          padding: '8px 16px',
          '&:hover': {
            boxShadow: 'none',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          border: '1px solid #E2E8F0',
          boxShadow: 'none',
          borderRadius: 6,
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-root': {
            backgroundColor: '#F8FAFC',
            color: '#475569',
            fontWeight: 700,
            fontSize: '0.75rem',
            letterSpacing: '0.05em',
            textTransform: 'uppercase',
            borderBottom: '1px solid #E2E8F0',
          },
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:hover': { backgroundColor: '#F1F5F9' },
          transition: 'background-color 0.15s ease',
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          borderBottom: '1px solid #E2E8F0',
          padding: '12px 16px',
        },
      },
    },
    MuiTextField: {
      defaultProps: { variant: 'outlined', size: 'small', fullWidth: true },
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 6,
            '& fieldset': {
              borderColor: '#E2E8F0',
            },
            '&:hover fieldset': {
              borderColor: '#CBD5E1',
            },
            '&.Mui-focused fieldset': {
              borderColor: '#0F172A',
            },
          },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          fontWeight: 600,
          borderRadius: 4,
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          borderBottom: '1px solid #E2E8F0',
          backgroundColor: '#FFFFFF',
          color: '#0F172A',
          boxShadow: 'none',
        },
      },
    },
  },
});

export default theme;
