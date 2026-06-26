import { useEffect, useState } from 'react';
import {
  Box, Typography, Button, TextField, Paper, Snackbar, Alert, Grid
} from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import SaveIcon from '@mui/icons-material/Save';
import { departmentService } from '../../services/departmentService';
import { useSnackbar } from '../../hooks/useSnackbar';
import type { Department } from '../../types';

interface FormErrors {
  name?: string;
  description?: string;
}

/**
 * Add / Edit Department Form.
 */
const DepartmentForm = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditMode = !!id;

  const { snackbarProps, showSnackbar, closeSnackbar } = useSnackbar();

  // Form State
  const [formData, setFormData] = useState<Department>({
    name: '',
    description: '',
  });

  const [formErrors, setFormErrors] = useState<FormErrors>({});
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(isEditMode);

  // Fetch department details if edit mode
  useEffect(() => {
    if (!isEditMode) return;
    const fetchDept = async () => {
      try {
        const dept = await departmentService.getById(Number(id));
        setFormData(dept);
      } catch (err) {
        console.error('Error fetching department:', err);
        showSnackbar('Failed to load department details.', 'error');
      } finally {
        setFetching(false);
      }
    };
    fetchDept();
  }, [id, isEditMode, showSnackbar]);

  // Handle Input Changes
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear error
    if (formErrors[name as keyof FormErrors]) {
      setFormErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  // Validate form locally before submitting
  const validateForm = (): boolean => {
    const errors: FormErrors = {};
    if (!formData.name.trim()) errors.name = 'Department name is required';
    if (formData.name.length > 100) errors.name = 'Name cannot exceed 100 characters';
    if (formData.description && formData.description.length > 255) {
      errors.description = 'Description cannot exceed 255 characters';
    }

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
        await departmentService.update(Number(id), formData);
        showSnackbar('Department updated successfully.', 'success');
      } else {
        await departmentService.create(formData);
        showSnackbar('Department created successfully.', 'success');
      }
      setTimeout(() => navigate('/departments'), 1500);
    } catch (err: any) {
      console.error('Error saving department:', err);
      if (err.response?.data?.validationErrors) {
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
    <Box sx={{ animation: 'fadeIn 0.5s ease-out', maxWidth: 600, mx: 'auto' }}>
      {/* Header */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 4 }}>
        <Button
          variant="outlined"
          color="primary"
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/departments')}
          size="small"
        >
          Back
        </Button>
        <Typography variant="h5" sx={{ fontWeight: 800 }}>
          {isEditMode ? 'Edit Department' : 'Create Department'}
        </Typography>
      </Box>

      {/* Form Card */}
      <Paper sx={{ p: 4, borderRadius: 4, bgcolor: 'background.paper' }} elevation={0}>
        <form onSubmit={handleSubmit}>
          <Grid container spacing={3}>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Department Name"
                name="name"
                value={formData.name}
                onChange={handleChange}
                error={!!formErrors.name}
                helperText={formErrors.name}
                required
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Description"
                name="description"
                value={formData.description || ''}
                onChange={handleChange}
                error={!!formErrors.description}
                helperText={formErrors.description}
                multiline
                rows={4}
                fullWidth
              />
            </Grid>

            <Grid size={{ xs: 12 }}>
              <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 2 }}>
                <Button
                  variant="outlined"
                  onClick={() => navigate('/departments')}
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
                  {loading ? 'Saving...' : 'Save Department'}
                </Button>
              </Box>
            </Grid>
          </Grid>
        </form>
      </Paper>

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

export default DepartmentForm;
