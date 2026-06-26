import { useEffect, useState, useCallback } from 'react';
import {
  Box, Typography, Button, TextField, MenuItem, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, TablePagination,
  TableSortLabel, Paper, Chip, IconButton, InputAdornment, Stack,
  Snackbar, Alert, Grid
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import AddIcon from '@mui/icons-material/Add';
import SearchIcon from '@mui/icons-material/Search';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import VisibilityIcon from '@mui/icons-material/Visibility';
import FilterListIcon from '@mui/icons-material/FilterList';
import { employeeService } from '../../services/employeeService';
import { departmentService } from '../../services/departmentService';
import { useAuth } from '../../context/AuthContext';
import { useSnackbar } from '../../hooks/useSnackbar';
import { useDebounce } from '../../hooks/useDebounce';
import { ConfirmDialog } from '../../components/common/CommonComponents';
import type { Employee, Department } from '../../types';

interface HeadCell {
  id: keyof Employee | 'departmentName';
  label: string;
  sortable: boolean;
}

const HEAD_CELLS: HeadCell[] = [
  { id: 'firstName', label: 'Name', sortable: true },
  { id: 'email', label: 'Email', sortable: true },
  { id: 'jobTitle', label: 'Job Title', sortable: true },
  { id: 'departmentName', label: 'Department', sortable: false },
  { id: 'salary', label: 'Salary', sortable: true },
  { id: 'hireDate', label: 'Hire Date', sortable: true },
  { id: 'status', label: 'Status', sortable: true },
];

/**
 * Paginated, sortable Employee list component.
 */
const EmployeeList = () => {
  const navigate = useNavigate();
  const { hasAnyRole, isAdmin } = useAuth();
  const { snackbarProps, showSnackbar, closeSnackbar } = useSnackbar();

  // Search/Filter State
  const [search, setSearch] = useState('');
  const debouncedSearch = useDebounce(search, 500);
  const [selectedDept, setSelectedDept] = useState<number | ''>('');
  const [selectedStatus, setSelectedStatus] = useState<string | ''>('');

  // Pagination & Sorting State
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [orderBy, setOrderBy] = useState<string>('id');
  const [order, setOrder] = useState<'asc' | 'desc'>('desc');

  // Data State
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);

  // Delete State
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  // Privileges
  const canModify = hasAnyRole(['ROLE_ADMIN', 'ROLE_HR']);
  const canDelete = isAdmin();

  // Fetch Departments
  useEffect(() => {
    departmentService.getList()
      .then(setDepartments)
      .catch((err) => console.error('Error fetching departments:', err));
  }, []);

  // Fetch Employees
  const fetchEmployees = useCallback(async () => {
    setLoading(true);
    try {
      const sortParam = `${orderBy},${order}`;
      const data = await employeeService.getAll({
        search: debouncedSearch || undefined,
        departmentId: selectedDept ? Number(selectedDept) : undefined,
        status: selectedStatus || undefined,
        page,
        size: rowsPerPage,
        sort: sortParam
      });
      setEmployees(data.content);
      setTotalElements(data.totalElements);
    } catch (err) {
      console.error('Error fetching employees:', err);
      showSnackbar('Failed to fetch employee list.', 'error');
    } finally {
      setLoading(false);
    }
  }, [debouncedSearch, selectedDept, selectedStatus, page, rowsPerPage, orderBy, order, showSnackbar]);

  useEffect(() => {
    fetchEmployees();
  }, [fetchEmployees]);

  // Handle Sort Request
  const handleSort = (property: string) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
    setPage(0);
  };

  // Handle Delete Confirmation
  const handleDeleteConfirm = async () => {
    if (deleteId === null) return;
    setDeleteLoading(true);
    try {
      await employeeService.delete(deleteId);
      showSnackbar('Employee deleted successfully.', 'success');
      setDeleteId(null);
      fetchEmployees();
    } catch (err: any) {
      console.error('Error deleting employee:', err);
      const errMsg = err.response?.data?.message || 'Failed to delete employee.';
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
            Employees
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Manage employee directories, roles, and department assignments
          </Typography>
        </Box>
        {canModify && (
          <Button
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
            onClick={() => navigate('/employees/new')}
            size="large"
          >
            Add Employee
          </Button>
        )}
      </Box>

      {/* Filters Card */}
      <Paper sx={{ p: 2.5, mb: 3, borderRadius: 3, bgcolor: 'background.paper' }} elevation={0}>
        <Grid container spacing={2} sx={{ alignItems: 'center' }}>
          <Grid size={{ xs: 12, md: 5 }}>
            <TextField
              placeholder="Search by name or email..."
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
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3.5 }}>
            <TextField
              select
              label="Department"
              value={selectedDept}
              onChange={(e) => { setSelectedDept(e.target.value as number | ''); setPage(0); }}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <FilterListIcon fontSize="small" color="action" />
                    </InputAdornment>
                  ),
                },
              }}
              fullWidth
            >
              <MenuItem value="">All Departments</MenuItem>
              {departments.map((dept) => (
                <MenuItem key={dept.id} value={dept.id}>
                  {dept.name}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3.5 }}>
            <TextField
              select
              label="Status"
              value={selectedStatus}
              onChange={(e) => { setSelectedStatus(e.target.value); setPage(0); }}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <FilterListIcon fontSize="small" color="action" />
                    </InputAdornment>
                  ),
                },
              }}
              fullWidth
            >
              <MenuItem value="">All Statuses</MenuItem>
              <MenuItem value="ACTIVE">Active</MenuItem>
              <MenuItem value="INACTIVE">Inactive</MenuItem>
            </TextField>
          </Grid>
        </Grid>
      </Paper>

      {/* Table */}
      <TableContainer component={Paper} sx={{ borderRadius: 3, bgcolor: 'background.paper' }} elevation={0}>
        <Table>
          <TableHead>
            <TableRow>
              {HEAD_CELLS.map((cell) => (
                <TableCell key={cell.id}>
                  {cell.sortable ? (
                    <TableSortLabel
                      active={orderBy === cell.id}
                      direction={orderBy === cell.id ? order : 'asc'}
                      onClick={() => handleSort(cell.id)}
                    >
                      {cell.label}
                    </TableSortLabel>
                  ) : (
                    cell.label
                  )}
                </TableCell>
              ))}
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={8} align="center" sx={{ py: 8 }}>
                  <Typography variant="body2" color="text.secondary">
                    Loading employees...
                  </Typography>
                </TableCell>
              </TableRow>
            ) : employees.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center" sx={{ py: 8 }}>
                  <Typography variant="body2" color="text.secondary">
                    No employees found matching filter criteria.
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              employees.map((emp) => (
                <TableRow key={emp.id}>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 600 }}>
                      {emp.firstName} {emp.lastName}
                    </Typography>
                  </TableCell>
                  <TableCell>{emp.email}</TableCell>
                  <TableCell>{emp.jobTitle}</TableCell>
                  <TableCell>{emp.departmentName || 'N/A'}</TableCell>
                  <TableCell>
                    ₹{emp.salary.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                  </TableCell>
                  <TableCell>{emp.hireDate}</TableCell>
                  <TableCell>
                    <Chip
                      label={emp.status}
                      color={emp.status === 'ACTIVE' ? 'success' : 'default'}
                      size="small"
                      sx={{ height: 24 }}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Stack direction="row" spacing={0.5} sx={{ justifyContent: 'flex-end' }}>
                      <IconButton
                        size="small"
                        onClick={() => navigate(`/employees/${emp.id}`)}
                        color="info"
                      >
                        <VisibilityIcon fontSize="small" />
                      </IconButton>
                      {canModify && (
                        <IconButton
                          size="small"
                          onClick={() => navigate(`/employees/${emp.id}/edit`)}
                          color="primary"
                        >
                          <EditIcon fontSize="small" />
                        </IconButton>
                      )}
                      {canDelete && (
                        <IconButton
                          size="small"
                          onClick={() => setDeleteId(emp.id!)}
                          color="error"
                        >
                          <DeleteIcon fontSize="small" />
                        </IconButton>
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
          rowsPerPageOptions={[5, 10, 25, 50]}
        />
      </TableContainer>

      {/* Confirm Deletion */}
      <ConfirmDialog
        open={deleteId !== null}
        title="Delete Employee"
        message="Are you sure you want to delete this employee? This action is permanent and will delete the associated user account if one exists."
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

export default EmployeeList;
