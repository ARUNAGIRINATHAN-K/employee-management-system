import { Box, Typography, Button, Container } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import BlockRoundedIcon from '@mui/icons-material/BlockRounded';

/**
 * Unauthorized Page (403 Access Denied).
 */
const Unauthorized = () => {
  const navigate = useNavigate();

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
          textAlign: 'center',
        }}
      >
        <BlockRoundedIcon sx={{ fontSize: 80, color: 'error.main', mb: 3 }} />
        <Typography variant="h4" sx={{ fontWeight: 800 }} gutterBottom>
          403 - Access Denied
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
          You do not have permissions to access this page. Please contact your administrator if you believe this is an error.
        </Typography>
        <Button
          variant="contained"
          color="primary"
          onClick={() => navigate('/')}
          size="large"
        >
          Go Back Home
        </Button>
      </Box>
    </Container>
  );
};

export default Unauthorized;
