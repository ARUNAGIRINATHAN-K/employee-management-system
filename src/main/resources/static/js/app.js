/* =========================================
   EMS - Main Application Module
   Dashboard navigation, data binding, charts
   ========================================= */

// ---------- State ----------
let currentUser = null;
let employeePage = 0;
let employeeSize = 10;
let employeeSortBy = 'id';
let employeeSortDir = 'asc';
let deptChartInstance = null;
let leaveChartInstance = null;

// ---------- Init ----------
document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('ems_token');
    const userData = localStorage.getItem('ems_user');
    if (!token || !userData) {
        window.location.href = '/index.html';
        return;
    }
    currentUser = JSON.parse(userData);
    initApp();
});

function initApp() {
    setupTheme();
    setupUserProfile();
    setupNavigation();
    setupEventListeners();
    applyRolePermissions();
    loadDashboard();
    setupResponsiveMenu();
}

// ---------- Theme ----------
function setupTheme() {
    const theme = localStorage.getItem('ems_theme') || 'light';
    document.documentElement.setAttribute('data-theme', theme);
    document.getElementById('themeToggle').addEventListener('click', () => {
        const current = document.documentElement.getAttribute('data-theme');
        const next = current === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', next);
        localStorage.setItem('ems_theme', next);
    });
}

// ---------- User Profile ----------
function setupUserProfile() {
    const name = currentUser.username || 'User';
    const initials = name.substring(0, 2).toUpperCase();
    document.getElementById('userAvatar').textContent = initials;
    document.getElementById('userNameDisplay').textContent = name;
    const roleMap = { ROLE_HR: 'HR Admin', ROLE_MANAGER: 'Manager', ROLE_EMPLOYEE: 'Employee' };
    document.getElementById('userRoleDisplay').textContent = roleMap[currentUser.role] || currentUser.role;
}

// ---------- RBAC ----------
function applyRolePermissions() {
    const role = currentUser.role;
    if (role === 'ROLE_EMPLOYEE') {
        // Employees see limited sidebar items
        const hrOnly = ['navDashboard'];
        hrOnly.forEach(id => {
            const el = document.getElementById(id);
            if (el) el.style.display = 'none';
        });
        document.getElementById('addEmployeeBtn').style.display = 'none';
        document.getElementById('exportExcelBtn').style.display = 'none';
        document.getElementById('exportPdfBtn').style.display = 'none';
        document.getElementById('generatePayrollBtn').style.display = 'none';
        document.getElementById('addReviewBtn').style.display = 'none';
        // Navigate to employees by default
        navigateTo('employees');
    }
    if (role === 'ROLE_MANAGER') {
        document.getElementById('generatePayrollBtn').style.display = 'none';
    }
}

// ---------- Navigation ----------
function setupNavigation() {
    document.querySelectorAll('.nav-item[data-section]').forEach(item => {
        item.addEventListener('click', () => {
            navigateTo(item.dataset.section);
        });
    });
    document.getElementById('logoutBtn').addEventListener('click', () => {
        localStorage.removeItem('ems_token');
        localStorage.removeItem('ems_user');
        window.location.href = '/index.html';
    });
}

function navigateTo(section) {
    // Update sidebar
    document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
    const navItem = document.querySelector(`.nav-item[data-section="${section}"]`);
    if (navItem) navItem.classList.add('active');

    // Update content
    document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
    const sectionEl = document.getElementById('section' + capitalize(section));
    if (sectionEl) sectionEl.classList.add('active');

    // Update title
    const titles = {
        dashboard: 'Dashboard', employees: 'Employee Directory', departments: 'Departments',
        leaves: 'Leave Management', attendance: 'Attendance', payroll: 'Payroll Center',
        performance: 'Performance Reviews'
    };
    document.getElementById('pageTitle').textContent = titles[section] || section;

    // Load section data
    const loaders = {
        dashboard: loadDashboard, employees: loadEmployees, departments: loadDepartments,
        leaves: loadLeaves, attendance: loadAttendance, payroll: loadPayroll,
        performance: loadPerformance
    };
    if (loaders[section]) loaders[section]();

    // Close sidebar on mobile
    document.getElementById('sidebar').classList.remove('open');
}

// ---------- Responsive Menu ----------
function setupResponsiveMenu() {
    const toggle = document.getElementById('menuToggle');
    const mq = window.matchMedia('(max-width: 768px)');
    function check(e) {
        toggle.style.display = e.matches ? 'flex' : 'none';
    }
    mq.addEventListener('change', check);
    check(mq);
    toggle.addEventListener('click', () => {
        document.getElementById('sidebar').classList.toggle('open');
    });
}

// ---------- Event Listeners ----------
function setupEventListeners() {
    // Employee search
    let searchTimeout;
    document.getElementById('empSearch').addEventListener('input', (e) => {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => { employeePage = 0; loadEmployees(); }, 400);
    });

    // Table sorting
    document.querySelectorAll('#employeeTable thead th[data-sort]').forEach(th => {
        th.addEventListener('click', () => {
            const field = th.dataset.sort;
            if (employeeSortBy === field) {
                employeeSortDir = employeeSortDir === 'asc' ? 'desc' : 'asc';
            } else {
                employeeSortBy = field;
                employeeSortDir = 'asc';
            }
            loadEmployees();
        });
    });

    // Employee CRUD
    document.getElementById('addEmployeeBtn').addEventListener('click', () => openEmployeeModal());
    document.getElementById('saveEmployeeBtn').addEventListener('click', saveEmployee);
    document.getElementById('exportExcelBtn').addEventListener('click', exportExcel);
    document.getElementById('exportPdfBtn').addEventListener('click', exportPdf);

    // Department CRUD
    document.getElementById('addDeptBtn').addEventListener('click', () => openDeptModal());
    document.getElementById('saveDeptBtn').addEventListener('click', saveDepartment);

    // Leave
    document.getElementById('applyLeaveBtn').addEventListener('click', () => openModal('leaveModal'));
    document.getElementById('submitLeaveBtn').addEventListener('click', submitLeave);

    // Attendance
    document.getElementById('checkInBtn').addEventListener('click', doCheckIn);
    document.getElementById('checkOutBtn').addEventListener('click', doCheckOut);

    // Payroll
    document.getElementById('generatePayrollBtn').addEventListener('click', () => openPayrollModal());
    document.getElementById('submitPayrollBtn').addEventListener('click', submitPayroll);

    // Performance
    document.getElementById('addReviewBtn').addEventListener('click', () => openPerfModal());
    document.getElementById('submitPerfBtn').addEventListener('click', submitPerformanceReview);
}

// =============================================
//  DASHBOARD
// =============================================
async function loadDashboard() {
    if (currentUser.role === 'ROLE_EMPLOYEE') return;
    try {
        const res = await API.getDashboardStats();
        if (!res || !res.ok) return;
        const data = await res.json();

        // Stats Cards
        const statsHtml = `
            <div class="stat-card purple">
                <div class="stat-icon">👥</div>
                <div class="stat-value">${data.totalEmployees || 0}</div>
                <div class="stat-label">Total Employees</div>
            </div>
            <div class="stat-card green">
                <div class="stat-icon">✅</div>
                <div class="stat-value">${data.activeEmployees || 0}</div>
                <div class="stat-label">Active Employees</div>
            </div>
            <div class="stat-card blue">
                <div class="stat-icon">🏢</div>
                <div class="stat-value">${data.totalDepartments || 0}</div>
                <div class="stat-label">Departments</div>
            </div>
            <div class="stat-card orange">
                <div class="stat-icon">📋</div>
                <div class="stat-value">${data.pendingLeaves || 0}</div>
                <div class="stat-label">Pending Leaves</div>
            </div>
        `;
        document.getElementById('statsGrid').innerHTML = statsHtml;

        // Update pending leaves badge
        const badge = document.getElementById('pendingLeavesBadge');
        if (data.pendingLeaves > 0) {
            badge.textContent = data.pendingLeaves;
            badge.classList.remove('hidden');
        }

        // Department Chart
        renderDeptChart(data.departmentWiseCounts || {});

        // Leave Chart
        renderLeaveChart(data.leaveStats || {});

        // Activity Feed
        renderActivityFeed(data.recentActivity || []);
    } catch (e) {
        console.error('Dashboard load error:', e);
    }
}

function renderDeptChart(deptData) {
    const ctx = document.getElementById('deptChart').getContext('2d');
    if (deptChartInstance) deptChartInstance.destroy();

    const labels = Object.keys(deptData);
    const values = Object.values(deptData);
    const colors = ['#6E59DE', '#00BCD4', '#4CAF50', '#FF9800', '#E91E63', '#9C27B0', '#3F51B5', '#009688'];

    deptChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels,
            datasets: [{ data: values, backgroundColor: colors.slice(0, labels.length), borderWidth: 0, spacing: 3 }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'bottom', labels: { padding: 16, usePointStyle: true, pointStyle: 'circle', font: { family: 'Inter', size: 12 } } }
            },
            cutout: '65%'
        }
    });
}

function renderLeaveChart(leaveData) {
    const ctx = document.getElementById('leaveChart').getContext('2d');
    if (leaveChartInstance) leaveChartInstance.destroy();

    const labels = Object.keys(leaveData);
    const values = Object.values(leaveData);
    const colors = ['#6E59DE', '#00BCD4', '#4CAF50'];

    leaveChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels,
            datasets: [{ label: 'Leave Requests', data: values, backgroundColor: colors.slice(0, labels.length), borderRadius: 8, maxBarThickness: 50 }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, ticks: { stepSize: 1, font: { family: 'Inter' } }, grid: { color: 'rgba(0,0,0,0.05)' } },
                x: { ticks: { font: { family: 'Inter' } }, grid: { display: false } }
            }
        }
    });
}

function renderActivityFeed(activities) {
    const feed = document.getElementById('activityFeed');
    if (activities.length === 0) {
        feed.innerHTML = '<li class="empty-state"><p>No recent activity</p></li>';
        return;
    }
    feed.innerHTML = activities.map(a => `
        <li>
            <div class="activity-dot"></div>
            <div>
                <div class="activity-text"><strong>${a.action}</strong> by ${a.username}</div>
                <div class="activity-text">${a.details || ''}</div>
                <div class="activity-time">${formatDateTime(a.timestamp)}</div>
            </div>
        </li>
    `).join('');
}

// =============================================
//  EMPLOYEES
// =============================================
async function loadEmployees() {
    const search = document.getElementById('empSearch').value.trim();
    try {
        const res = await API.getEmployees(employeePage, employeeSize, search, employeeSortBy, employeeSortDir);
        if (!res || !res.ok) return;
        const data = await res.json();
        renderEmployeeTable(data.content || []);
        renderPagination(data, 'employeePagination', (p) => { employeePage = p; loadEmployees(); });
    } catch (e) {
        console.error('Load employees error:', e);
    }
}

function renderEmployeeTable(employees) {
    const tbody = document.getElementById('employeeTableBody');
    if (employees.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9"><div class="empty-state"><div class="empty-icon">👥</div><p>No employees found</p></div></td></tr>';
        return;
    }

    const isPrivileged = currentUser.role === 'ROLE_HR' || currentUser.role === 'ROLE_MANAGER';

    tbody.innerHTML = employees.map(e => {
        const initials = (e.firstName?.[0] || '') + (e.lastName?.[0] || '');
        const statusClass = e.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive';
        const avatarHtml = e.photoPath
            ? `<div class="employee-avatar"><img src="${e.photoPath}" alt=""></div>`
            : `<div class="employee-avatar">${initials}</div>`;

        return `<tr>
            <td>${e.id}</td>
            <td>
                <div class="employee-cell">
                    ${avatarHtml}
                    <div>
                        <div class="employee-name">${e.firstName} ${e.lastName}</div>
                    </div>
                </div>
            </td>
            <td><span class="employee-email">${e.email}</span></td>
            <td>${e.phone || '-'}</td>
            <td>${e.jobTitle || '-'}</td>
            <td>${e.department ? e.department.name : '-'}</td>
            <td>$${(e.salary || 0).toLocaleString()}</td>
            <td><span class="badge ${statusClass}">${e.status}</span></td>
            <td>
                ${isPrivileged ? `
                    <button class="btn btn-outline btn-sm btn-icon" onclick="editEmployee(${e.id})" title="Edit">✏️</button>
                    <button class="btn btn-outline btn-sm btn-icon" onclick="uploadEmployeePhoto(${e.id})" title="Upload Photo">📷</button>
                ` : ''}
                ${currentUser.role === 'ROLE_HR' ? `
                    <button class="btn btn-outline btn-sm btn-icon" onclick="deleteEmployee(${e.id})" title="Delete" style="color:var(--danger)">🗑️</button>
                ` : ''}
            </td>
        </tr>`;
    }).join('');
}

function openEmployeeModal(emp = null) {
    document.getElementById('empModalTitle').textContent = emp ? 'Edit Employee' : 'Add Employee';
    document.getElementById('empId').value = emp ? emp.id : '';
    document.getElementById('empFirstName').value = emp ? emp.firstName : '';
    document.getElementById('empLastName').value = emp ? emp.lastName : '';
    document.getElementById('empEmail').value = emp ? emp.email : '';
    document.getElementById('empPhone').value = emp ? (emp.phone || '') : '';
    document.getElementById('empJobTitle').value = emp ? (emp.jobTitle || '') : '';
    document.getElementById('empSalary').value = emp ? (emp.salary || '') : '';
    document.getElementById('empHireDate').value = emp ? (emp.hireDate || '') : '';

    // Load departments into dropdown
    loadDepartmentDropdown('empDepartment', emp ? (emp.department ? emp.department.id : '') : '');

    // Disable email editing on update
    document.getElementById('empEmail').readOnly = !!emp;

    openModal('employeeModal');
}

async function loadDepartmentDropdown(selectId, selectedId) {
    try {
        const res = await API.getDepartments();
        if (!res || !res.ok) return;
        const depts = await res.json();
        const select = document.getElementById(selectId);
        select.innerHTML = '<option value="">-- Select --</option>' +
            depts.map(d => `<option value="${d.id}" ${d.id == selectedId ? 'selected' : ''}>${d.name}</option>`).join('');
    } catch (e) { console.error(e); }
}

async function saveEmployee() {
    const id = document.getElementById('empId').value;
    const data = {
        firstName: document.getElementById('empFirstName').value,
        lastName: document.getElementById('empLastName').value,
        email: document.getElementById('empEmail').value,
        phone: document.getElementById('empPhone').value,
        jobTitle: document.getElementById('empJobTitle').value,
        salary: parseFloat(document.getElementById('empSalary').value) || 0,
        hireDate: document.getElementById('empHireDate').value || null,
        department: document.getElementById('empDepartment').value ? { id: parseInt(document.getElementById('empDepartment').value) } : null
    };

    try {
        let res;
        if (id) {
            res = await API.updateEmployee(id, data);
        } else {
            res = await API.createEmployee(data);
        }
        if (res && res.ok) {
            showToast(id ? 'Employee updated successfully' : 'Employee created successfully', 'success');
            closeModal('employeeModal');
            loadEmployees();
        } else {
            const err = await res.json();
            showToast(err.message || 'Failed to save employee', 'error');
        }
    } catch (e) {
        showToast('Network error', 'error');
    }
}

async function editEmployee(id) {
    try {
        const res = await API.getEmployee(id);
        if (res && res.ok) {
            const emp = await res.json();
            openEmployeeModal(emp);
        }
    } catch (e) { showToast('Error loading employee', 'error'); }
}

async function deleteEmployee(id) {
    if (!confirm('Are you sure you want to delete this employee? This action is a soft delete.')) return;
    try {
        const res = await API.deleteEmployee(id);
        if (res && res.ok) {
            showToast('Employee deleted successfully', 'success');
            loadEmployees();
        } else {
            const err = await res.json();
            showToast(err.message || 'Failed to delete', 'error');
        }
    } catch (e) { showToast('Network error', 'error'); }
}

function uploadEmployeePhoto(id) {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = async () => {
        const file = input.files[0];
        if (!file) return;
        const formData = new FormData();
        formData.append('file', file);
        try {
            const res = await API.uploadPhoto(id, formData);
            if (res && res.ok) {
                showToast('Photo uploaded successfully', 'success');
                loadEmployees();
            } else {
                showToast('Upload failed', 'error');
            }
        } catch (e) { showToast('Network error', 'error'); }
    };
    input.click();
}

async function exportExcel() {
    try {
        const res = await API.exportExcel();
        if (res && res.ok) {
            const blob = await res.blob();
            downloadBlob(blob, 'employees.xlsx');
            showToast('Excel exported successfully', 'success');
        }
    } catch (e) { showToast('Export failed', 'error'); }
}

async function exportPdf() {
    try {
        const res = await API.exportPdf();
        if (res && res.ok) {
            const blob = await res.blob();
            downloadBlob(blob, 'employees.pdf');
            showToast('PDF exported successfully', 'success');
        }
    } catch (e) { showToast('Export failed', 'error'); }
}

// =============================================
//  DEPARTMENTS
// =============================================
async function loadDepartments() {
    try {
        const res = await API.getDepartments();
        if (!res || !res.ok) return;
        const depts = await res.json();
        const grid = document.getElementById('deptGrid');

        if (depts.length === 0) {
            grid.innerHTML = '<div class="empty-state"><div class="empty-icon">🏢</div><p>No departments found</p></div>';
            return;
        }

        grid.innerHTML = depts.map((d, i) => {
            const colors = ['purple', 'green', 'blue', 'orange', 'red'];
            const color = colors[i % colors.length];
            return `
                <div class="stat-card ${color}" style="cursor:pointer;">
                    <div class="stat-icon">🏢</div>
                    <div class="stat-value">${d.name}</div>
                    <div class="stat-label">${d.description || 'No description'}</div>
                    <div style="margin-top:12px;display:flex;gap:6px;">
                        ${currentUser.role === 'ROLE_HR' ? `
                            <button class="btn btn-outline btn-sm" onclick="editDept(${d.id})">Edit</button>
                            <button class="btn btn-outline btn-sm" onclick="deleteDept(${d.id})" style="color:var(--danger)">Delete</button>
                        ` : ''}
                    </div>
                </div>`;
        }).join('');
    } catch (e) { console.error(e); }
}

function openDeptModal(dept = null) {
    document.getElementById('deptModalTitle').textContent = dept ? 'Edit Department' : 'Add Department';
    document.getElementById('deptId').value = dept ? dept.id : '';
    document.getElementById('deptName').value = dept ? dept.name : '';
    document.getElementById('deptDesc').value = dept ? (dept.description || '') : '';
    openModal('departmentModal');
}

async function editDept(id) {
    try {
        const res = await API.getDepartment(id);
        if (res && res.ok) {
            const dept = await res.json();
            openDeptModal(dept);
        }
    } catch (e) { showToast('Error', 'error'); }
}

async function saveDepartment() {
    const id = document.getElementById('deptId').value;
    const data = {
        name: document.getElementById('deptName').value,
        description: document.getElementById('deptDesc').value
    };

    try {
        let res;
        if (id) {
            res = await API.updateDepartment(id, data);
        } else {
            res = await API.createDepartment(data);
        }
        if (res && res.ok) {
            showToast(id ? 'Department updated' : 'Department created', 'success');
            closeModal('departmentModal');
            loadDepartments();
        } else {
            const err = await res.json();
            showToast(err.message || 'Error', 'error');
        }
    } catch (e) { showToast('Network error', 'error'); }
}

async function deleteDept(id) {
    if (!confirm('Delete this department? Employees will be unassigned.')) return;
    try {
        const res = await API.deleteDepartment(id);
        if (res && res.ok) {
            showToast('Department deleted', 'success');
            loadDepartments();
        } else {
            const err = await res.json();
            showToast(err.message || 'Error', 'error');
        }
    } catch (e) { showToast('Network error', 'error'); }
}

// =============================================
//  LEAVES
// =============================================
async function loadLeaves() {
    const empId = currentUser.employeeId;

    // Load balances
    if (empId) {
        try {
            const res = await API.getLeaveBalances(empId);
            if (res && res.ok) {
                const balances = await res.json();
                const grid = document.getElementById('leaveBalanceGrid');
                grid.innerHTML = balances.map(b => `
                    <div class="balance-card">
                        <div class="balance-type">${b.leaveType}</div>
                        <div class="balance-days">${b.balance}</div>
                        <div class="balance-label">days remaining</div>
                    </div>
                `).join('');
            }
        } catch (e) { console.error(e); }
    }

    // Load leave list
    try {
        let res;
        if (currentUser.role === 'ROLE_HR') {
            res = await API.getAllLeaves();
        } else if (empId) {
            res = await API.getEmployeeLeaves(empId);
        }
        if (!res || !res.ok) return;
        const leaves = await res.json();
        renderLeaveTable(leaves);
    } catch (e) { console.error(e); }
}

function renderLeaveTable(leaves) {
    const tbody = document.getElementById('leaveTableBody');
    if (leaves.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8"><div class="empty-state"><p>No leave records</p></div></td></tr>';
        return;
    }

    const canApprove = currentUser.role === 'ROLE_HR' || currentUser.role === 'ROLE_MANAGER';

    tbody.innerHTML = leaves.map(l => {
        const statusClass = l.status === 'APPROVED' ? 'badge-approved' :
                           l.status === 'REJECTED' ? 'badge-rejected' : 'badge-pending';
        const empName = l.employee ? `${l.employee.firstName} ${l.employee.lastName}` : '-';
        return `<tr>
            <td>${l.id}</td>
            <td>${empName}</td>
            <td><span class="badge badge-role-manager">${l.leaveType}</span></td>
            <td>${l.startDate}</td>
            <td>${l.endDate}</td>
            <td>${l.reason || '-'}</td>
            <td><span class="badge ${statusClass}">${l.status}</span></td>
            <td>
                ${canApprove && l.status === 'PENDING' ? `
                    <button class="btn btn-success btn-sm" onclick="processLeave(${l.id},'APPROVED')">✓</button>
                    <button class="btn btn-danger btn-sm" onclick="processLeave(${l.id},'REJECTED')">✕</button>
                ` : '-'}
            </td>
        </tr>`;
    }).join('');
}

async function submitLeave() {
    if (!currentUser.employeeId) {
        showToast('No employee profile linked to this account', 'error');
        return;
    }
    const data = {
        employee: { id: currentUser.employeeId },
        leaveType: document.getElementById('leaveType').value,
        startDate: document.getElementById('leaveStart').value,
        endDate: document.getElementById('leaveEnd').value,
        reason: document.getElementById('leaveReason').value
    };
    try {
        const res = await API.applyLeave(data);
        if (res && res.ok) {
            showToast('Leave application submitted', 'success');
            closeModal('leaveModal');
            loadLeaves();
        } else {
            const err = await res.json();
            showToast(err.message || 'Failed to apply', 'error');
        }
    } catch (e) { showToast('Network error', 'error'); }
}

async function processLeave(leaveId, status) {
    const comments = prompt(`Comments for ${status.toLowerCase()}:`) || '';
    try {
        const res = await API.processLeave(leaveId, status, comments);
        if (res && res.ok) {
            showToast(`Leave ${status.toLowerCase()} successfully`, 'success');
            loadLeaves();
        } else {
            const err = await res.json();
            showToast(err.message || 'Error', 'error');
        }
    } catch (e) { showToast('Network error', 'error'); }
}

// =============================================
//  ATTENDANCE
// =============================================
async function loadAttendance() {
    const empId = currentUser.employeeId;
    if (!empId) {
        document.getElementById('attendanceStatusText').textContent = 'No employee profile linked';
        return;
    }

    // Today's status
    try {
        const res = await API.getTodayAttendance(empId);
        if (res && res.ok) {
            const data = await res.json();
            const statusEl = document.getElementById('attendanceStatusText');
            const timeEl = document.getElementById('attendanceTimeText');
            if (data.status === 'NOT_CHECKED_IN') {
                statusEl.textContent = 'You have not checked in today';
                timeEl.textContent = '';
                document.getElementById('checkInBtn').disabled = false;
                document.getElementById('checkOutBtn').disabled = true;
            } else {
                statusEl.textContent = `Today: ${data.status}`;
                const cin = data.checkIn ? new Date(data.checkIn).toLocaleTimeString() : '-';
                const cout = data.checkOut ? new Date(data.checkOut).toLocaleTimeString() : 'Not yet';
                timeEl.textContent = `Check-in: ${cin} | Check-out: ${cout}`;
                document.getElementById('checkInBtn').disabled = true;
                document.getElementById('checkOutBtn').disabled = !!data.checkOut;
            }
        }
    } catch (e) { console.error(e); }

    // History
    try {
        const res = await API.getAttendanceHistory(empId);
        if (res && res.ok) {
            const records = await res.json();
            const tbody = document.getElementById('attendanceTableBody');
            if (records.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5"><div class="empty-state"><p>No attendance records</p></div></td></tr>';
                return;
            }
            tbody.innerHTML = records.reverse().map(r => {
                const statusClass = r.status === 'PRESENT' ? 'badge-present' :
                                   r.status === 'LATE' ? 'badge-late' :
                                   r.status === 'EARLY_DEPARTURE' ? 'badge-early-departure' : 'badge-absent';
                return `<tr>
                    <td>${r.date}</td>
                    <td>${r.checkIn ? new Date(r.checkIn).toLocaleTimeString() : '-'}</td>
                    <td>${r.checkOut ? new Date(r.checkOut).toLocaleTimeString() : '-'}</td>
                    <td><span class="badge ${statusClass}">${r.status}</span></td>
                    <td>${r.notes || '-'}</td>
                </tr>`;
            }).join('');
        }
    } catch (e) { console.error(e); }
}

async function doCheckIn() {
    const empId = currentUser.employeeId;
    if (!empId) return;
    try {
        const res = await API.checkIn(empId);
        if (res && res.ok) {
            showToast('Checked in successfully!', 'success');
            loadAttendance();
        } else {
            const err = await res.json();
            showToast(err.message || 'Check-in failed', 'error');
        }
    } catch (e) { showToast('Network error', 'error'); }
}

async function doCheckOut() {
    const empId = currentUser.employeeId;
    if (!empId) return;
    try {
        const res = await API.checkOut(empId);
        if (res && res.ok) {
            showToast('Checked out successfully!', 'success');
            loadAttendance();
        } else {
            const err = await res.json();
            showToast(err.message || 'Check-out failed', 'error');
        }
    } catch (e) { showToast('Network error', 'error'); }
}

// =============================================
//  PAYROLL
// =============================================
async function loadPayroll() {
    try {
        let records = [];
        if (currentUser.role === 'ROLE_HR') {
            // Load all employees' payroll (get current month)
            const now = new Date();
            const period = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0');
            const res = await API.getPayrollByPeriod(period);
            if (res && res.ok) records = await res.json();
        } else if (currentUser.employeeId) {
            const res = await API.getPayrollHistory(currentUser.employeeId);
            if (res && res.ok) records = await res.json();
        }
        renderPayrollTable(records);
    } catch (e) { console.error(e); }
}

function renderPayrollTable(records) {
    const tbody = document.getElementById('payrollTableBody');
    if (records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9"><div class="empty-state"><p>No payroll records found</p></div></td></tr>';
        return;
    }
    tbody.innerHTML = records.map(p => {
        const empName = p.employee ? `${p.employee.firstName} ${p.employee.lastName}` : '-';
        const statusClass = p.status === 'PAID' ? 'badge-paid' : 'badge-pending';
        return `<tr>
            <td>${p.id}</td>
            <td>${empName}</td>
            <td>${p.payPeriod}</td>
            <td>$${(p.basicSalary || 0).toLocaleString()}</td>
            <td>$${(p.allowances || 0).toLocaleString()}</td>
            <td>$${(p.deductions || 0).toLocaleString()}</td>
            <td><strong>$${(p.netSalary || 0).toLocaleString()}</strong></td>
            <td><span class="badge ${statusClass}">${p.status}</span></td>
            <td>
                <button class="btn btn-outline btn-sm" onclick="downloadPayslip(${p.id})">📄 Payslip</button>
                ${currentUser.role === 'ROLE_HR' && p.status === 'PENDING' ? `
                    <button class="btn btn-success btn-sm" onclick="markPaid(${p.id})">💸 Pay</button>
                ` : ''}
            </td>
        </tr>`;
    }).join('');
}

async function openPayrollModal() {
    // Load employee list
    try {
        const res = await API.getEmployees(0, 100, '', 'firstName', 'asc');
        if (res && res.ok) {
            const data = await res.json();
            const select = document.getElementById('payrollEmployee');
            select.innerHTML = data.content.map(e => `<option value="${e.id}">${e.firstName} ${e.lastName} (${e.email})</option>`).join('');
        }
    } catch (e) { console.error(e); }

    // Default to current month
    const now = new Date();
    document.getElementById('payrollPeriod').value = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0');
    openModal('payrollModal');
}

async function submitPayroll() {
    const empId = document.getElementById('payrollEmployee').value;
    const period = document.getElementById('payrollPeriod').value;
    try {
        const res = await API.generatePayroll(empId, period);
        if (res && res.ok) {
            showToast('Payroll generated', 'success');
            closeModal('payrollModal');
            loadPayroll();
        } else {
            const err = await res.json();
            showToast(err.message || 'Error', 'error');
        }
    } catch (e) { showToast('Network error', 'error'); }
}

async function markPaid(id) {
    try {
        const res = await API.markPayrollPaid(id);
        if (res && res.ok) {
            showToast('Marked as paid', 'success');
            loadPayroll();
        } else {
            const err = await res.json();
            showToast(err.message || 'Error', 'error');
        }
    } catch (e) { showToast('Network error', 'error'); }
}

async function downloadPayslip(id) {
    try {
        const res = await API.downloadPayslip(id);
        if (res && res.ok) {
            const blob = await res.blob();
            downloadBlob(blob, `payslip_${id}.pdf`);
            showToast('Payslip downloaded', 'success');
        }
    } catch (e) { showToast('Download failed', 'error'); }
}

// =============================================
//  PERFORMANCE
// =============================================
async function loadPerformance() {
    try {
        let reviews = [];
        if (currentUser.employeeId) {
            const res = await API.getEmployeeReviews(currentUser.employeeId);
            if (res && res.ok) reviews = await res.json();
        }
        renderPerfTable(reviews);
    } catch (e) { console.error(e); }
}

function renderPerfTable(reviews) {
    const tbody = document.getElementById('perfTableBody');
    if (reviews.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7"><div class="empty-state"><p>No performance reviews yet</p></div></td></tr>';
        return;
    }
    tbody.innerHTML = reviews.map(r => {
        const empName = r.employee ? `${r.employee.firstName} ${r.employee.lastName}` : '-';
        const revName = r.reviewer ? `${r.reviewer.firstName} ${r.reviewer.lastName}` : '-';
        const stars = Array.from({ length: 5 }, (_, i) =>
            `<span class="star ${i < r.rating ? 'filled' : ''}">★</span>`
        ).join('');
        return `<tr>
            <td>${empName}</td>
            <td>${revName}</td>
            <td>${r.reviewDate}</td>
            <td><div class="stars">${stars}</div></td>
            <td>${r.kpiGoals || '-'}</td>
            <td>${r.promotionRecommendation ? '<span class="badge badge-active">Yes</span>' : '<span class="badge badge-inactive">No</span>'}</td>
            <td>${r.comments || '-'}</td>
        </tr>`;
    }).join('');
}

async function openPerfModal() {
    try {
        const res = await API.getEmployees(0, 100, '', 'firstName', 'asc');
        if (res && res.ok) {
            const data = await res.json();
            const select = document.getElementById('perfEmployee');
            select.innerHTML = data.content.map(e => `<option value="${e.id}">${e.firstName} ${e.lastName}</option>`).join('');
        }
    } catch (e) { console.error(e); }
    openModal('perfModal');
}

async function submitPerformanceReview() {
    if (!currentUser.employeeId) {
        showToast('No employee profile linked to your account', 'error');
        return;
    }
    const data = {
        employee: { id: parseInt(document.getElementById('perfEmployee').value) },
        reviewer: { id: currentUser.employeeId },
        rating: parseInt(document.getElementById('perfRating').value),
        kpiGoals: document.getElementById('perfKpi').value,
        comments: document.getElementById('perfComments').value,
        promotionRecommendation: document.getElementById('perfPromotion').checked
    };
    try {
        const res = await API.addReview(data);
        if (res && res.ok) {
            showToast('Review submitted', 'success');
            closeModal('perfModal');
            loadPerformance();
        } else {
            const err = await res.json();
            showToast(err.message || 'Error', 'error');
        }
    } catch (e) { showToast('Network error', 'error'); }
}

// =============================================
//  UTILITY FUNCTIONS
// =============================================
function openModal(id) {
    document.getElementById(id).classList.add('show');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('show');
}

function capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

function formatDateTime(dt) {
    if (!dt) return '';
    const d = new Date(dt);
    return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function downloadBlob(blob, filename) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
}

function renderPagination(pageData, containerId, callback) {
    const container = document.getElementById(containerId);
    const totalPages = pageData.totalPages || 0;
    const currentPage = pageData.number || 0;
    const totalElements = pageData.totalElements || 0;

    if (totalPages <= 1) {
        container.innerHTML = `<div class="pagination-info">Showing ${totalElements} records</div>`;
        return;
    }

    const start = currentPage * pageData.size + 1;
    const end = Math.min((currentPage + 1) * pageData.size, totalElements);

    let html = `<div class="pagination-info">Showing ${start}-${end} of ${totalElements}</div>`;
    html += '<div class="pagination-controls">';
    html += `<button ${currentPage === 0 ? 'disabled' : ''} onclick="(${callback.toString()})(${currentPage - 1})">‹ Prev</button>`;

    for (let i = 0; i < totalPages && i < 7; i++) {
        html += `<button class="${i === currentPage ? 'active' : ''}" onclick="(${callback.toString()})(${i})">${i + 1}</button>`;
    }

    html += `<button ${currentPage >= totalPages - 1 ? 'disabled' : ''} onclick="(${callback.toString()})(${currentPage + 1})">Next ›</button>`;
    html += '</div>';

    container.innerHTML = html;
}

// ---------- Toast Notifications ----------
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const icons = { success: '✅', error: '❌', info: 'ℹ️', warning: '⚠️' };
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <span class="toast-icon">${icons[type]}</span>
        <span class="toast-text">${message}</span>
        <button class="toast-close" onclick="this.parentElement.remove()">✕</button>
    `;
    container.appendChild(toast);
    setTimeout(() => { if (toast.parentElement) toast.remove(); }, 5000);
}
