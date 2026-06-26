import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  Box, Drawer, AppBar, Toolbar, List, ListItem, ListItemButton,
  ListItemIcon, ListItemText, Typography, IconButton, Avatar,
  Tooltip, Divider, Chip, useTheme,
} from '@mui/material';
import DashboardRoundedIcon      from '@mui/icons-material/DashboardRounded';
import PeopleRoundedIcon         from '@mui/icons-material/PeopleRounded';
import BusinessRoundedIcon       from '@mui/icons-material/BusinessRounded';
import LogoutRoundedIcon         from '@mui/icons-material/LogoutRounded';
import MenuRoundedIcon           from '@mui/icons-material/MenuRounded';
import PersonRoundedIcon         from '@mui/icons-material/PersonRounded';
import ManageAccountsRoundedIcon from '@mui/icons-material/ManageAccountsRounded';
import { useAuth } from '../context/AuthContext';

const DRAWER_WIDTH = 248;

interface NavItem {
  label: string;
  icon: React.ReactNode;
  path: string;
  roles: string[];
}

const NAV_ITEMS: NavItem[] = [
  {
    label: 'Dashboard',
    icon: <DashboardRoundedIcon fontSize="small" />,
    path: '/dashboard',
    roles: ['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'],
  },
  {
    label: 'Employees',
    icon: <PeopleRoundedIcon fontSize="small" />,
    path: '/employees',
    roles: ['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'],
  },
  {
    label: 'Departments',
    icon: <BusinessRoundedIcon fontSize="small" />,
    path: '/departments',
    roles: ['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'],
  },
  {
    label: 'Users',
    icon: <ManageAccountsRoundedIcon fontSize="small" />,
    path: '/users',
    roles: ['ROLE_ADMIN'],
  },
  {
    label: 'My Profile',
    icon: <PersonRoundedIcon fontSize="small" />,
    path: '/profile',
    roles: ['ROLE_EMPLOYEE'],
  },
];

const DashboardLayout = () => {
  const theme = useTheme();
  const { user, logout, hasAnyRole } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);

  const visibleItems = NAV_ITEMS.filter((item) => hasAnyRole(item.roles));

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const drawerContent = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      {/* Branding */}
      <Box sx={{ px: 3, py: 2.5 }}>
        <Typography
          variant="h6"
          sx={{
            fontWeight: 800,
            fontFamily: 'Outfit, sans-serif',
            color: 'text.primary',
            letterSpacing: '-0.5px',
            fontSize: '1.1rem',
          }}
        >
          EMS
        </Typography>
        <Typography variant="caption" sx={{ color: 'text.disabled', fontSize: '0.72rem' }}>
          Employee Management
        </Typography>
      </Box>

      <Divider />

      {/* Navigation */}
      <List sx={{ flex: 1, px: 1.5, pt: 1.5, pb: 1 }}>
        {visibleItems.map((item) => {
          const active = location.pathname.startsWith(item.path);
          return (
            <ListItem key={item.path} disablePadding sx={{ mb: 0.25 }}>
              <ListItemButton
                onClick={() => { navigate(item.path); setMobileOpen(false); }}
                sx={{
                  borderRadius: '6px',
                  py: 0.9,
                  px: 1.5,
                  bgcolor: active ? 'action.selected' : 'transparent',
                  color: active ? 'text.primary' : 'text.secondary',
                  '&:hover': {
                    bgcolor: 'action.hover',
                    color: 'text.primary',
                  },
                  transition: 'background-color 0.15s, color 0.15s',
                }}
              >
                <ListItemIcon
                  sx={{
                    minWidth: 34,
                    color: active ? 'text.primary' : 'text.disabled',
                  }}
                >
                  {item.icon}
                </ListItemIcon>
                <ListItemText
                  primary={item.label}
                  slotProps={{
                    primary: {
                      sx: {
                        fontWeight: active ? 600 : 400,
                        fontSize: '0.875rem',
                        fontFamily: 'Inter, sans-serif',
                      },
                    },
                  }}
                />
                {active && (
                  <Box
                    sx={{
                      width: 3,
                      height: 18,
                      borderRadius: '2px',
                      bgcolor: 'text.primary',
                    }}
                  />
                )}
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>

      <Divider />

      {/* User Info + Logout */}
      <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
        <Avatar
          sx={{
            width: 32,
            height: 32,
            bgcolor: 'text.primary',
            color: 'background.paper',
            fontSize: '0.8rem',
            fontWeight: 700,
          }}
        >
          {user?.username?.[0]?.toUpperCase()}
        </Avatar>
        <Box sx={{ flex: 1, minWidth: 0 }}>
          <Typography variant="body2" sx={{ fontWeight: 600, fontSize: '0.82rem' }} noWrap>
            {user?.username}
          </Typography>
          <Chip
            label={user?.roles?.[0]?.replace('ROLE_', '') ?? 'USER'}
            size="small"
            variant="outlined"
            sx={{
              height: 16,
              fontSize: '0.62rem',
              borderRadius: '4px',
              mt: 0.25,
              borderColor: 'divider',
              color: 'text.secondary',
              '& .MuiChip-label': { px: 0.75 },
            }}
          />
        </Box>
        <Tooltip title="Logout">
          <IconButton
            size="small"
            onClick={handleLogout}
            sx={{
              color: 'text.disabled',
              '&:hover': { color: 'error.main', bgcolor: 'transparent' },
            }}
          >
            <LogoutRoundedIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      </Box>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      {/* Mobile AppBar */}
      <AppBar
        position="fixed"
        elevation={0}
        sx={{
          display: { md: 'none' },
          bgcolor: 'background.paper',
          borderBottom: '1px solid',
          borderColor: 'divider',
          color: 'text.primary',
          zIndex: theme.zIndex.drawer + 1,
        }}
      >
        <Toolbar sx={{ minHeight: '56px !important' }}>
          <IconButton onClick={() => setMobileOpen(true)} edge="start" sx={{ color: 'text.secondary' }}>
            <MenuRoundedIcon />
          </IconButton>
          <Typography
            variant="h6"
            sx={{
              fontWeight: 800,
              fontFamily: 'Outfit, sans-serif',
              color: 'text.primary',
              ml: 1,
              fontSize: '1rem',
            }}
          >
            EMS
          </Typography>
        </Toolbar>
      </AppBar>

      {/* Desktop Drawer */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          width: DRAWER_WIDTH,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            boxSizing: 'border-box',
            bgcolor: 'background.paper',
            borderRight: '1px solid',
            borderColor: 'divider',
          },
        }}
      >
        {drawerContent}
      </Drawer>

      {/* Mobile Drawer */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={() => setMobileOpen(false)}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            borderRight: '1px solid',
            borderColor: 'divider',
          },
        }}
      >
        {drawerContent}
      </Drawer>

      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: { xs: 2.5, md: 3.5 },
          mt: { xs: 7, md: 0 },
          minHeight: '100vh',
          bgcolor: '#F8F9FA',
          overflow: 'auto',
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
};

export default DashboardLayout;
