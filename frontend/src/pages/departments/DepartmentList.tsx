import { useEffect, useState, useCallback } from 'react';
import {
  Box, Typography, Button, TextField, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, TablePagination,
  Paper, IconButton, InputAdornment, Stack, Snackbar, Alert
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import AddIcon from '@mui/icons-material/Add';
import SearchIcon from '@mui/icons-material/Search';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import VisibilityIcon from '@mui/icons-material/Visibility';
import { departmentService } from '../../services/departmentService';
import { useAuth } from '../../context/AuthContext';
import { useSnackbar } from '../../hooks/useSnackbar';
import { useDebounce } from '../../hooks/useDebounce';
import { ConfirmDialog } from '../../components/common/CommonComponents';
import type { Department } from '../../types';

/**
 * List of Departments with pagination and search.
 */
const DepartmentList = () => {
  const navigate = useNavigate();
  const { isAdmin } = useAuth();
  const { snackbarProps, showSnackbar, closeSnackbar } = useSnackbar();

  // Search state
  const [search, setSearch] = useState('');
  const debouncedSearch = useDebounce(search, 500);

  // Pagination state
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  // Data state
  const [departments, setDepartments] = useState<Department[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);

  // Delete state
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  // Fetch Departments
  const fetchDepartments = useCallback(async () => {
    setLoading(true);
    try {
      const data = await departmentService.getAll({
        search: debouncedSearch || undefined,
        page,
        size: rowsPerPage,
        sort: 'id,desc'
      });
      setDepartments(data.content);
      setTotalElements(data.totalElements);
    } catch (err) {
      console.error('Error fetching departments:', err);
      showSnackbar('Failed to fetch departments.', 'error');
    } finally {
      setLoading(false);
    }
  }, [debouncedSearch, page, rowsPerPage, showSnackbar]);

  useEffect(() => {
    fetchDepartments();
  }, [fetchDepartments]);

  // Handle Delete Confirmation
  const handleDeleteConfirm = async () => {
    if (deleteId === null) return;
    setDeleteLoading(true);
    try {
      await departmentService.delete(deleteId);
      showSnackbar('Department deleted successfully.', 'success');
      setDeleteId(null);
      fetchDepartments();
    } catch (err: any) {
      console.error('Error deleting department:', err);
      const errMsg = err.response?.data?.message || 'Failed to delete department.';
      showSnackbar(errMsg, 'error');
    } finally {
      setDeleteLoading(false);
    }
  };

  return (
    <Box sx={{ animation: 'fadeIn 0.5s ease-out' }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 800 }} color="text.primary" gutterBottom>
            Departments
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Manage corporate departments, definitions, and active employee distributions
          </Typography>
        </Box>
        {isAdmin() && (
          <Button
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
            onClick={() => navigate('/departments/new')}
            size="large"
          >
            Add Department
          </Button>
        )}
      </Box>

      {/* Search Filter */}
      <Paper sx={{ p: 2.5, mb: 3, borderRadius: 3, bgcolor: 'background.paper' }} elevation={0}>
        <TextField
          placeholder="Search by department name..."
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }}
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon color="action" />
                </InputAdornment>
              ),
            },
          }}
          fullWidth
        />
      </Paper>

      {/* Table */}
      <TableContainer component={Paper} sx={{ borderRadius: 3, bgcolor: 'background.paper' }} elevation={0}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Department Name</TableCell>
              <TableCell>Description</TableCell>
              <TableCell align="center">Active Employees</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={4} align="center" sx={{ py: 6 }}>
                  <Typography variant="body2" color="text.secondary">
                    Loading departments...
                  </Typography>
                </TableCell>
              </TableRow>
            ) : departments.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} align="center" sx={{ py: 6 }}>
                  <Typography variant="body2" color="text.secondary">
                    No departments found.
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              departments.map((dept) => (
                <TableRow key={dept.id}>
                  <TableCell sx={{ fontWeight: 600 }}>{dept.name}</TableCell>
                  <TableCell sx={{ color: 'text.secondary' }}>
                    {dept.description || 'No description provided'}
                  </TableCell>
                  <TableCell align="center" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    {dept.employeeCount ?? 0}
                  </TableCell>
                  <TableCell align="right">
                    <Stack direction="row" spacing={0.5} sx={{ justifyContent: 'flex-end' }}>
                      <IconButton
                        size="small"
                        onClick={() => navigate(`/departments/${dept.id}`)}
                        color="info"
                      >
                        <VisibilityIcon fontSize="small" />
                      </IconButton>
                      {isAdmin() && (
                        <>
                          <IconButton
                            size="small"
                            onClick={() => navigate(`/departments/${dept.id}/edit`)}
                            color="primary"
                          >
                            <EditIcon fontSize="small" />
                          </IconButton>
                          <IconButton
                            size="small"
                            onClick={() => setDeleteId(dept.id!)}
                            color="error"
                          >
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </>
                      )}
                    </Stack>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={totalElements}
          page={page}
          onPageChange={(_, newPage) => setPage(newPage)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => { setRowsPerPage(parseInt(e.target.value, 10)); setPage(0); }}
          rowsPerPageOptions={[5, 10, 25]}
        />
      </TableContainer>

      {/* Confirm deletion modal */}
      <ConfirmDialog
        open={deleteId !== null}
        title="Delete Department"
        message="Are you sure you want to delete this department? This operation will fail if the department contains any active employees."
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteId(null)}
        loading={deleteLoading}
      />

      {/* Snackbar alerts */}
      <Snackbar
        open={snackbarProps.open}
        autoHideDuration={6000}
        onClose={closeSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert onClose={closeSnackbar} severity={snackbarProps.severity} variant="filled">
          {snackbarProps.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default DepartmentList;
