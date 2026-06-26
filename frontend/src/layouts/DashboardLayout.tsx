import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  Box, Drawer, AppBar, Toolbar, List, ListItem, ListItemButton,
  ListItemIcon, ListItemText, Typography, IconButton, Avatar,
  Tooltip, Divider, Chip, useTheme, alpha,
} from '@mui/material';
import DashboardRoundedIcon from '@mui/icons-material/DashboardRounded';
import PeopleRoundedIcon    from '@mui/icons-material/PeopleRounded';
import BusinessRoundedIcon  from '@mui/icons-material/BusinessRounded';
import LogoutRoundedIcon    from '@mui/icons-material/LogoutRounded';
import MenuRoundedIcon      from '@mui/icons-material/MenuRounded';
import PersonRoundedIcon    from '@mui/icons-material/PersonRounded';
import { useAuth } from '../context/AuthContext';

const DRAWER_WIDTH = 260;

interface NavItem {
  label: string;
  icon: React.ReactNode;
  path: string;
  roles: string[];
}

const NAV_ITEMS: NavItem[] = [
  {
    label: 'Dashboard',
    icon: <DashboardRoundedIcon />,
    path: '/dashboard',
    roles: ['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'],
  },
  {
    label: 'Employees',
    icon: <PeopleRoundedIcon />,
    path: '/employees',
    roles: ['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'],
  },
  {
    label: 'Departments',
    icon: <BusinessRoundedIcon />,
    path: '/departments',
    roles: ['ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'],
  },
  {
    label: 'My Profile',
    icon: <PersonRoundedIcon />,
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

  const visibleItems = NAV_ITEMS.filter((item) =>
    hasAnyRole(item.roles)
  );

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const drawerContent = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      {/* Branding */}
      <Box sx={{ px: 3, py: 2.5 }}>
        <Typography variant="h6" sx={{ fontWeight: 800, color: 'primary.main' }}>
          EMS
        </Typography>
        <Typography variant="caption" color="text.secondary">
          Employee Management
        </Typography>
      </Box>
      <Divider sx={{ opacity: 0.12 }} />

      {/* Navigation */}
      <List sx={{ flex: 1, px: 1.5, pt: 1 }}>
        {visibleItems.map((item) => {
          const active = location.pathname.startsWith(item.path);
          return (
            <ListItem key={item.path} disablePadding sx={{ mb: 0.5 }}>
              <ListItemButton
                onClick={() => { navigate(item.path); setMobileOpen(false); }}
                sx={{
                  borderRadius: 2,
                  bgcolor: active ? alpha(theme.palette.primary.main, 0.16) : 'transparent',
                  color: active ? 'primary.main' : 'text.secondary',
                  '&:hover': { bgcolor: alpha(theme.palette.primary.main, 0.1) },
                  transition: 'all 0.2s',
                }}
              >
                <ListItemIcon sx={{ minWidth: 38, color: 'inherit' }}>
                  {item.icon}
                </ListItemIcon>
                <ListItemText
                  primary={item.label}
                  slotProps={{ primary: { sx: { fontWeight: active ? 700 : 500, fontSize: '0.9rem' } } }}
                />
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>

      <Divider sx={{ opacity: 0.12 }} />

      {/* User Info + Logout */}
      <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
        <Avatar sx={{ width: 36, height: 36, bgcolor: 'primary.main', fontSize: '0.85rem' }}>
          {user?.username?.[0]?.toUpperCase()}
        </Avatar>
        <Box sx={{ flex: 1, minWidth: 0 }}>
          <Typography variant="body2" sx={{ fontWeight: 600 }} noWrap>
            {user?.username}
          </Typography>
          <Chip
            label={user?.roles?.[0]?.replace('ROLE_', '') ?? 'USER'}
            size="small"
            color="primary"
            sx={{ height: 18, fontSize: '0.65rem', mt: 0.25 }}
          />
        </Box>
        <Tooltip title="Logout">
          <IconButton size="small" onClick={handleLogout} color="error">
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
          borderBottom: '1px solid rgba(255,255,255,0.06)',
          zIndex: theme.zIndex.drawer + 1,
        }}
      >
        <Toolbar>
          <IconButton onClick={() => setMobileOpen(true)} edge="start">
            <MenuRoundedIcon />
          </IconButton>
          <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main', ml: 1 }}>
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
            borderRight: '1px solid rgba(255,255,255,0.06)',
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
          '& .MuiDrawer-paper': { width: DRAWER_WIDTH },
        }}
      >
        {drawerContent}
      </Drawer>

      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: { xs: 2, md: 3 },
          mt: { xs: 8, md: 0 },
          minHeight: '100vh',
          bgcolor: 'background.default',
          overflow: 'auto',
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
};

export default DashboardLayout;
