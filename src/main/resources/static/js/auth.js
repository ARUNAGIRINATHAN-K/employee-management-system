/* =========================================
   EMS - Authentication Module
   Login / Forgot Password / Reset Password
   ========================================= */

document.addEventListener('DOMContentLoaded', () => {
    // If already logged in, redirect
    const token = localStorage.getItem('ems_token');
    if (token && window.location.pathname.includes('index.html') || token && window.location.pathname === '/') {
        window.location.href = '/dashboard.html';
        return;
    }

    // ---------- Form Toggle ----------
    const toggleBtns = document.querySelectorAll('#formToggle button');
    const forms = {
        login: document.getElementById('loginForm'),
        forgot: document.getElementById('forgotForm'),
        reset: document.getElementById('resetForm')
    };

    toggleBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetForm = btn.dataset.form;
            toggleBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            Object.keys(forms).forEach(key => {
                forms[key].style.display = key === targetForm ? 'block' : 'none';
            });
            hideMessages();
        });
    });

    // ---------- Login Form ----------
    document.getElementById('loginForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        hideMessages();
        const username = document.getElementById('loginUsername').value.trim();
        const password = document.getElementById('loginPassword').value.trim();

        if (!username || !password) {
            showError('Please enter both username and password.');
            return;
        }

        const loginBtn = document.getElementById('loginBtn');
        loginBtn.textContent = 'Signing in...';
        loginBtn.disabled = true;

        try {
            const res = await API.login(username, password);
            const data = await res.json();

            if (res.ok) {
                localStorage.setItem('ems_token', data.token);
                localStorage.setItem('ems_user', JSON.stringify({
                    username: data.username,
                    role: data.role,
                    userId: data.userId,
                    employeeId: data.employeeId
                }));
                window.location.href = '/dashboard.html';
            } else {
                showError(data.message || 'Invalid credentials. Please try again.');
            }
        } catch (err) {
            showError('Network error. Please check your connection.');
        } finally {
            loginBtn.textContent = 'Sign In';
            loginBtn.disabled = false;
        }
    });

    // ---------- Forgot Password Form ----------
    document.getElementById('forgotForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        hideMessages();
        const email = document.getElementById('forgotEmail').value.trim();

        if (!email) {
            showError('Please enter your username/email.');
            return;
        }

        try {
            const res = await API.forgotPassword(email);
            const data = await res.json();

            if (res.ok) {
                showSuccess('Reset code generated! Token: ' + data.token + '. Switch to "Reset Password" tab to use it.');
            } else {
                showError(data.message || 'Failed to generate reset token.');
            }
        } catch (err) {
            showError('Network error.');
        }
    });

    // ---------- Reset Password Form ----------
    document.getElementById('resetForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        hideMessages();
        const email = document.getElementById('resetEmail').value.trim();
        const token = document.getElementById('resetToken').value.trim();
        const newPassword = document.getElementById('resetNewPassword').value.trim();

        if (!email || !token || !newPassword) {
            showError('Please fill in all fields.');
            return;
        }

        try {
            const res = await API.resetPassword(email, token, newPassword);
            const data = await res.json();

            if (res.ok) {
                showSuccess('Password reset successfully! You can now sign in.');
                document.getElementById('resetForm').reset();
            } else {
                showError(data.message || 'Reset failed. Please check your code.');
            }
        } catch (err) {
            showError('Network error.');
        }
    });

    // ---------- Helper Functions ----------
    function showError(msg) {
        const el = document.getElementById('authError');
        el.textContent = msg;
        el.style.display = 'block';
    }

    function showSuccess(msg) {
        const el = document.getElementById('authSuccess');
        el.textContent = msg;
        el.style.display = 'block';
    }

    function hideMessages() {
        document.getElementById('authError').style.display = 'none';
        document.getElementById('authSuccess').style.display = 'none';
    }
});
