/* =========================================
   EMS - API Helper Module
   Centralized Fetch API wrapper with JWT
   ========================================= */

const API = {
    BASE_URL: '',

    getToken() {
        return localStorage.getItem('ems_token');
    },

    getHeaders(isJson = true) {
        const headers = {};
        if (isJson) {
            headers['Content-Type'] = 'application/json';
        }
        const token = this.getToken();
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }
        return headers;
    },

    async request(method, url, body = null, isJson = true) {
        const options = {
            method,
            headers: this.getHeaders(isJson),
        };
        if (body) {
            if (isJson) {
                options.body = JSON.stringify(body);
            } else {
                // FormData - remove Content-Type to let browser set boundary
                delete options.headers['Content-Type'];
                options.body = body;
            }
        }
        const response = await fetch(this.BASE_URL + url, options);
        if (response.status === 401) {
            // Token expired or invalid
            localStorage.removeItem('ems_token');
            localStorage.removeItem('ems_user');
            window.location.href = '/index.html';
            return null;
        }
        return response;
    },

    async get(url) {
        return this.request('GET', url);
    },

    async post(url, body, isJson = true) {
        return this.request('POST', url, body, isJson);
    },

    async put(url, body) {
        return this.request('PUT', url, body);
    },

    async delete(url) {
        return this.request('DELETE', url);
    },

    // ---------- AUTH ----------
    async login(username, password) {
        const res = await fetch(this.BASE_URL + '/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        return res;
    },

    async forgotPassword(email) {
        const res = await fetch(this.BASE_URL + '/api/auth/forgot-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });
        return res;
    },

    async resetPassword(email, token, newPassword) {
        const res = await fetch(this.BASE_URL + '/api/auth/reset-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, token, newPassword })
        });
        return res;
    },

    // ---------- EMPLOYEES ----------
    async getEmployees(page = 0, size = 10, search = '', sortBy = 'id', sortDir = 'asc') {
        let url = `/api/employees?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`;
        if (search) url += `&search=${encodeURIComponent(search)}`;
        return this.get(url);
    },

    async getEmployee(id) { return this.get(`/api/employees/${id}`); },
    async createEmployee(data) { return this.post('/api/employees', data); },
    async updateEmployee(id, data) { return this.put(`/api/employees/${id}`, data); },
    async deleteEmployee(id) { return this.delete(`/api/employees/${id}`); },
    async uploadPhoto(id, formData) { return this.post(`/api/employees/${id}/photo`, formData, false); },
    async exportExcel() { return this.get('/api/employees/export/excel'); },
    async exportPdf() { return this.get('/api/employees/export/pdf'); },

    // ---------- DEPARTMENTS ----------
    async getDepartments() { return this.get('/api/departments'); },
    async getDepartment(id) { return this.get(`/api/departments/${id}`); },
    async getDepartmentEmployees(id) { return this.get(`/api/departments/${id}/employees`); },
    async createDepartment(data) { return this.post('/api/departments', data); },
    async updateDepartment(id, data) { return this.put(`/api/departments/${id}`, data); },
    async deleteDepartment(id) { return this.delete(`/api/departments/${id}`); },

    // ---------- LEAVES ----------
    async getAllLeaves() { return this.get('/api/leaves'); },
    async getEmployeeLeaves(empId) { return this.get(`/api/leaves/employee/${empId}`); },
    async getManagerLeaves(mgrId) { return this.get(`/api/leaves/manager/${mgrId}`); },
    async getLeaveBalances(empId) { return this.get(`/api/leaves/balances/employee/${empId}`); },
    async applyLeave(data) { return this.post('/api/leaves/apply', data); },
    async processLeave(leaveId, status, comments) {
        return this.post(`/api/leaves/${leaveId}/approve?status=${status}&comments=${encodeURIComponent(comments)}`);
    },
    async accrueLeaves() { return this.post('/api/leaves/accrue'); },
    async getLeavePolicies() { return this.get('/api/leaves/policies'); },
    async updateLeavePolicy(id, data) { return this.put(`/api/leaves/policies/${id}`, data); },

    // ---------- SHIFTS ----------
    async getShifts() { return this.get('/api/shifts'); },
    async createShift(data) { return this.post('/api/shifts', data); },

    // ---------- PROFILE CHANGES ----------
    async getMyProfileRequests() { return this.get('/api/profile-changes/my-requests'); },
    async getPendingProfileRequests() { return this.get('/api/profile-changes/pending'); },
    async submitProfileRequest(data) { return this.post('/api/profile-changes', data); },
    async approveProfileRequest(id) { return this.post(`/api/profile-changes/${id}/approve`); },
    async rejectProfileRequest(id, data) { return this.post(`/api/profile-changes/${id}/reject`, data); },

    // ---------- EXPENSES ----------
    async getAllExpenses() { return this.get('/api/expenses'); },
    async getEmployeeExpenses(empId) { return this.get(`/api/expenses/employee/${empId}`); },
    async getManagerExpenses(mgrId) { return this.get(`/api/expenses/manager/${mgrId}`); },
    async submitExpenseClaim(data) { return this.post('/api/expenses', data); },
    async approveExpenseClaim(id) { return this.post(`/api/expenses/${id}/approve`); },
    async rejectExpenseClaim(id, data) { return this.post(`/api/expenses/${id}/reject`, data); },

    // ---------- ATTENDANCE ----------
    async checkIn(empId) { return this.post(`/api/attendance/check-in/${empId}`); },
    async checkOut(empId) { return this.post(`/api/attendance/check-out/${empId}`); },
    async getTodayAttendance(empId) { return this.get(`/api/attendance/today/${empId}`); },
    async getAttendanceHistory(empId) { return this.get(`/api/attendance/history/${empId}`); },

    // ---------- PAYROLL ----------
    async getPayrollHistory(empId) { return this.get(`/api/payroll/history/${empId}`); },
    async getPayrollByPeriod(period) { return this.get(`/api/payroll/period?period=${period}`); },
    async generatePayroll(empId, payPeriod) {
        return this.post(`/api/payroll/generate?employeeId=${empId}&payPeriod=${payPeriod}`);
    },
    async markPayrollPaid(id) { return this.post(`/api/payroll/${id}/pay`); },
    async downloadPayslip(id) { return this.get(`/api/payroll/${id}/payslip`); },

    // ---------- PERFORMANCE ----------
    async getEmployeeReviews(empId) { return this.get(`/api/performance/employee/${empId}`); },
    async addReview(data) { return this.post('/api/performance', data); },

    // ---------- DASHBOARD ----------
    async getDashboardStats() { return this.get('/api/dashboard/stats'); },
};
