import { useEffect, useState } from 'react';
import {
  Box, Typography, Button, TextField, MenuItem, Paper, Grid,
  Snackbar, Alert
} from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import SaveIcon from '@mui/icons-material/Save';
import { employeeService } from '../../services/employeeService';
import { departmentService } from '../../services/departmentService';
import { useSnackbar } from '../../hooks/useSnackbar';
import type { Employee, Department } from '../../types';

interface FormErrors {
  firstName?: string;
  lastName?: string;
  email?: string;
  jobTitle?: string;
  salary?: string;
  hireDate?: string;
  departmentId?: string;
}

/**
 * Add / Edit Employee Form Component.
 */
const EmployeeForm = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditMode = !!id;

  const { snackbarProps, showSnackbar, closeSnackbar } = useSnackbar();

  // Form State
  const [formData, setFormData] = useState<Employee>({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    jobTitle: '',
    salary: 0,
    hireDate: new Date().toISOString().split('T')[0],
    status: 'ACTIVE',
    departmentId: 0,
  });

  const [departments, setDepartments] = useState<Department[]>([]);
  const [formErrors, setFormErrors] = useState<FormErrors>({});
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(isEditMode);

  // Fetch departments list & employee details if edit mode
  useEffect(() => {
    const initData = async () => {
      try {
        const depts = await departmentService.getList();
        setDepartments(depts);

        if (isEditMode) {
          const emp = await employeeService.getById(Number(id));
          setFormData({
            ...emp,
            // Ensure date is formatted correctly for input tag
            hireDate: emp.hireDate ? emp.hireDate.substring(0, 10) : '',
          });
        }
      } catch (err) {
        console.error('Error initializing form data:', err);
        showSnackbar('Failed to load form dependencies.', 'error');
      } finally {
        setFetching(false);
      }
    };

    initData();
  }, [id, isEditMode, showSnackbar]);

  // Handle Input Changes
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'departmentId' || name === 'salary' ? Number(value) : value,
    }));
    // Clear error
    if (formErrors[name as keyof FormErrors]) {
      setFormErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  // Validate form locally before submitting
  const validateForm = (): boolean => {
    const errors: FormErrors = {};
    if (!formData.firstName.trim()) errors.firstName = 'First name is required';
    if (!formData.lastName.trim()) errors.lastName = 'Last name is required';
    if (!formData.email.trim()) {
      errors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      errors.email = 'Invalid email format';
    }
    if (!formData.jobTitle.trim()) errors.jobTitle = 'Job title is required';
    if (formData.salary === undefined || formData.salary < 0) {
      errors.salary = 'Salary must be a positive number';
    }
    if (!formData.hireDate) errors.hireDate = 'Hire date is required';
    if (!formData.departmentId) errors.departmentId = 'Department is required';

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Handle Submit
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setLoading(true);
    try {
      if (isEditMode) {
        await employeeService.update(Number(id), formData);
        showSnackbar('Employee updated successfully.', 'success');
      } else {
        await employeeService.create(formData);
        showSnackbar('Employee created successfully.', 'success');
      }
      setTimeout(() => navigate('/employees'), 1500);
    } catch (err: any) {
      console.error('Error saving employee:', err);
      if (err.response?.data?.validationErrors) {
        // Map backend validation errors
        setFormErrors(err.response.data.validationErrors);
        showSnackbar('Please fix the validation errors.', 'error');
      } else {
        const errMsg = err.response?.data?.message || 'An error occurred while saving.';
        showSnackbar(errMsg, 'error');
      }
    } finally {
      setLoading(false);
    }
  };

  if (fetching) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
        <Typography>Loading details...</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ animation: 'fadeIn 0.5s ease-out', maxWidth: 800, mx: 'auto' }}>
      {/* Header & Back Button */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 4 }}>
        <Button
          variant="outlined"
          color="primary"
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/employees')}
          size="small"
        >
          Back
        </Button>
        <Typography variant="h5" sx={{ fontWeight: 800 }}>
          {isEditMode ? 'Edit Employee Profile' : 'Register New Employee'}
        </Typography>
      </Box>

      {/* Form Card */}
      <Paper sx={{ p: 4, borderRadius: 4, bgcolor: 'background.paper' }} elevation={0}>
        <form onSubmit={handleSubmit}>
          <Grid container spacing={3}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="First Name"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                error={!!formErrors.firstName}
                helperText={formErrors.firstName}
                required
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Last Name"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                error={!!formErrors.lastName}
                helperText={formErrors.lastName}
                required
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Email Address"
                name="email"
                type="email"
                value={formData.email}
                onChange={handleChange}
                error={!!formErrors.email}
                helperText={formErrors.email}
                required
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Phone Number"
                name="phone"
                value={formData.phone || ''}
                onChange={handleChange}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Job Title"
                name="jobTitle"
                value={formData.jobTitle}
                onChange={handleChange}
                error={!!formErrors.jobTitle}
                helperText={formErrors.jobTitle}
                required
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Salary (USD)"
                name="salary"
                type="number"
                value={formData.salary}
                onChange={handleChange}
                error={!!formErrors.salary}
                helperText={formErrors.salary}
                required
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                select
                label="Department"
                name="departmentId"
                value={formData.departmentId || ''}
                onChange={handleChange}
                error={!!formErrors.departmentId}
                helperText={formErrors.departmentId}
                required
              >
                {departments.map((dept) => (
                  <MenuItem key={dept.id} value={dept.id}>
                    {dept.name}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Hire Date"
                name="hireDate"
                type="date"
                value={formData.hireDate}
                onChange={handleChange}
                error={!!formErrors.hireDate}
                helperText={formErrors.hireDate}
                slotProps={{ inputLabel: { shrink: true } }}
                required
              />
            </Grid>
            {isEditMode && (
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  select
                  label="Employee Status"
                  name="status"
                  value={formData.status}
                  onChange={handleChange}
                  required
                >
                  <MenuItem value="ACTIVE">Active</MenuItem>
                  <MenuItem value="INACTIVE">Inactive</MenuItem>
                </TextField>
              </Grid>
            )}

            <Grid size={{ xs: 12 }}>
              <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 2 }}>
                <Button
                  variant="outlined"
                  onClick={() => navigate('/employees')}
                  disabled={loading}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  startIcon={<SaveIcon />}
                  disabled={loading}
                >
                  {loading ? 'Saving...' : 'Save Profile'}
                </Button>
              </Box>
            </Grid>
          </Grid>
        </form>
      </Paper>

      {/* Snackbar Alert */}
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

export default EmployeeForm;
