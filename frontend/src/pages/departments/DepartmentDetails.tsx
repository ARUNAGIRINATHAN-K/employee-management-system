import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box, Typography, Button, Paper, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Chip
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import EditIcon from '@mui/icons-material/Edit';
import { departmentService } from '../../services/departmentService';
import { employeeService } from '../../services/employeeService';
import { useAuth } from '../../context/AuthContext';
import type { Department, Employee } from '../../types';
import { LoadingSpinner } from '../../components/common/CommonComponents';

/**
 * Detailed Department view with nested Employee list.
 */
const DepartmentDetails = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const { isAdmin } = useAuth();
  const [department, setDepartment] = useState<Department | null>(null);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [deptData, empData] = await Promise.all([
          departmentService.getById(Number(id)),
          employeeService.getAll({ departmentId: Number(id), size: 100 })
        ]);
        setDepartment(deptData);
        setEmployees(empData.content);
        setError(null);
      } catch (err) {
        console.error('Error fetching department details:', err);
        setError('Failed to load department details.');
      } finally {
        setLoading(false);
      }
    };
    if (id) fetchData();
  }, [id]);

  if (loading) return <LoadingSpinner />;
  if (error || !department) {
    return (
      <Box sx={{ p: 3, textAlign: 'center' }}>
        <Typography color="error" variant="h6">{error || 'Department not found.'}</Typography>
        <Button variant="outlined" color="primary" onClick={() => navigate('/departments')} sx={{ mt: 2 }}>
          Back to list
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ animation: 'fadeIn 0.5s ease-out', maxWidth: 900, mx: 'auto' }}>
      {/* Header Actions */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Button
          variant="outlined"
          color="primary"
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/departments')}
          size="small"
        >
          Back
        </Button>
        {isAdmin() && (
          <Button
            variant="contained"
            color="primary"
            startIcon={<EditIcon />}
            onClick={() => navigate(`/departments/${department.id}/edit`)}
          >
            Edit Department
          </Button>
        )}
      </Box>

      {/* Info Card */}
      <Paper sx={{ p: 4, mb: 4, borderRadius: 4, bgcolor: 'background.paper' }} elevation={0}>
        <Typography variant="h5" sx={{ fontWeight: 800 }} color="primary" gutterBottom>
          {department.name}
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mt: 1.5, whiteSpace: 'pre-wrap' }}>
          {department.description || 'No description available for this department.'}
        </Typography>
      </Paper>

      {/* Employees in Department */}
      <Paper sx={{ p: 4, borderRadius: 4, bgcolor: 'background.paper' }} elevation={0}>
        <Typography variant="h6" sx={{ fontWeight: 700, mb: 3 }}>
          Department Roster ({employees.length} {employees.length === 1 ? 'member' : 'members'})
        </Typography>
        <TableContainer component={Box}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Employee Name</TableCell>
                <TableCell>Job Title</TableCell>
                <TableCell>Email</TableCell>
                <TableCell align="right">Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {employees.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={4} align="center" sx={{ py: 4 }}>
                    <Typography variant="body2" color="text.secondary">
                      No employees are currently mapped to this department.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                employees.map((emp) => (
                  <TableRow
                    key={emp.id}
                    sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}
                    onClick={() => navigate(`/employees/${emp.id}`)}
                  >
                    <TableCell sx={{ py: 1.5, fontWeight: 600 }}>
                      {emp.firstName} {emp.lastName}
                    </TableCell>
                    <TableCell sx={{ py: 1.5 }}>{emp.jobTitle}</TableCell>
                    <TableCell sx={{ py: 1.5 }}>{emp.email}</TableCell>
                    <TableCell align="right" sx={{ py: 1.5 }}>
                      <Chip
                        label={emp.status}
                        color={emp.status === 'ACTIVE' ? 'success' : 'default'}
                        size="small"
                        sx={{ height: 22 }}
                      />
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>
    </Box>
  );
};

export default DepartmentDetails;
