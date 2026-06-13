async function fetchUsers() {
    const res = await API.get('/api/admin/users');
    if (!res) return; // API helper may redirect on 401
    const users = await res.json();
    const grid = document.getElementById('users-grid');
    grid.innerHTML = '';
    users.forEach(u => {
        const card = document.createElement('div');
        card.className = 'user-card';
        card.innerHTML = `<b>${u.username}</b> (${u.role}) - Active: ${u.active}`;
        const toggle = document.createElement('button');
        toggle.textContent = u.active ? 'Deactivate' : 'Reactivate';
        toggle.onclick = async () => {
            const path = u.active ? `/api/admin/users/${u.id}/deactivate` : `/api/admin/users/${u.id}/reactivate`;
            const resp = await API.post(path);
            if (resp && resp.ok) fetchUsers();
        };
        const roleSelect = document.createElement('select');
        ['ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE','ROLE_ADMIN'].forEach(r => {
            const op = document.createElement('option'); op.value = r; op.text = r; if (u.role===r) op.selected=true; roleSelect.appendChild(op);
        });
        roleSelect.onchange = async () => {
            const resp = await API.put(`/api/admin/users/${u.id}/role`, { role: roleSelect.value });
            if (resp && resp.ok) fetchUsers();
        };
        card.appendChild(toggle);
        card.appendChild(roleSelect);
        grid.appendChild(card);
    });
}

async function downloadFile(url, filename) {
    const res = await API.get(url);
    if (!res) return;
    if (!res.ok) {
        alert('Export failed: ' + (await res.text()));
        return;
    }
    const blob = await res.blob();
    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
}

async function exportAudits() {
    await downloadFile('/api/admin/export/audit-logs', 'audit-logs.xlsx');
}
async function exportDirectory() {
    await downloadFile('/api/admin/export/company-directory', 'company-directory.xlsx');
}

document.getElementById('export-audits').onclick = exportAudits;
document.getElementById('export-directory').onclick = exportDirectory;

fetchUsers();
