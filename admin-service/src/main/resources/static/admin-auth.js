const tokenStorageKey = 'adminJwtToken';
const adminNameStorageKey = 'adminName';
const adminEmailStorageKey = 'adminEmail';

function showToast(message, duration = 4000) {
    const container = document.getElementById('notificationContainer');
    const toast = document.createElement('div');
    const isError = duration > 4000;
    toast.className = isError ? 'toast-message error' : 'toast-message success';
    toast.textContent = message;
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

async function readResponse(response) {
    const text = await response.text();
    try {
        return text ? JSON.parse(text) : {};
    } catch (error) {
        return {message: text};
    }
}

// Login form handler
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async event => {
        event.preventDefault();
        const response = await fetch('/api/admin/login', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                email: document.getElementById('email').value,
                password: document.getElementById('password').value
            })
        });
        const data = await readResponse(response);
        if (response.ok && data.token) {
            localStorage.setItem(tokenStorageKey, data.token);
            localStorage.setItem(adminNameStorageKey, data.name || document.getElementById('email').value);
            localStorage.setItem(adminEmailStorageKey, data.email || document.getElementById('email').value);
            showToast('Login successful');
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 500);
        } else {
            showToast(data.message || 'Login failed', 5000);
        }
    });
}

// Register form handler
const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', async event => {
        event.preventDefault();
        const response = await fetch('/api/admin/register', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                name: document.getElementById('name').value,
                email: document.getElementById('email').value,
                password: document.getElementById('password').value
            })
        });
        const data = await readResponse(response);
        if (response.ok) {
            showToast('Admin account created successfully');
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 500);
        } else {
            showToast(data.message || 'Registration failed', 5000);
        }
    });
}
