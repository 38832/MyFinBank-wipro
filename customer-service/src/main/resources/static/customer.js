const registerForm = document.getElementById('registerForm');
const loginForm = document.getElementById('loginForm');
const logoutButton = document.getElementById('logoutButton');
const accountActions = document.getElementById('accountActions');
const loginMessage = document.getElementById('loginMessage');
const registerMessage = document.getElementById('registerMessage');
const notificationContainer = document.getElementById('notificationContainer');
const tokenStorageKey = 'customerJwtToken';

if (localStorage.getItem(tokenStorageKey)) {
    accountActions.classList.remove('hidden');
    loginMessage.textContent = 'Session token found. You can continue using customer actions.';
}

function showToast(message, duration = 4000) {
    const toast = document.createElement('div');
    const isError = duration > 4000; // Long duration = error message
    toast.className = isError ? 'toast-message error' : 'toast-message success';
    toast.textContent = message;
    notificationContainer.appendChild(toast);
    
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

registerForm.addEventListener('submit', async event => {
    event.preventDefault();
    const payload = {
        name: document.getElementById('name').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value
    };
    const response = await fetch('/api/customers/register', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });
    const data = await readResponse(response);
    if (response.ok) {
        registerMessage.textContent = 'Registration successful';
        showToast('Account created successfully');
        document.getElementById('name').value = '';
        document.getElementById('email').value = '';
        document.getElementById('password').value = '';
    } else {
        registerMessage.textContent = data.message || 'Registration failed';
        showToast(data.message || 'Registration failed', 5000);
    }
});

loginForm.addEventListener('submit', async event => {
    event.preventDefault();
    const response = await fetch('/api/customers/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            email: document.getElementById('loginEmail').value,
            password: document.getElementById('loginPassword').value
        })
    });
    const data = await readResponse(response);
    if (response.ok && data.token) {
        localStorage.setItem(tokenStorageKey, data.token);
        loginMessage.textContent = 'Logged in successfully';
        showToast('Login successful');
        accountActions.classList.remove('hidden');
    } else {
        loginMessage.textContent = data.message || 'Login failed';
        showToast(data.message || 'Login failed', 5000);
    }
});

logoutButton.addEventListener('click', () => {
    fetch('/api/customers/logout', {method: 'POST'}).catch(() => {});
    localStorage.removeItem(tokenStorageKey);
    accountActions.classList.add('hidden');
    loginMessage.textContent = 'Logged out';
    showToast('Logged out');
});

async function sendAccountAction(path) {
    const token = localStorage.getItem(tokenStorageKey);
    if (!token) {
        loginMessage.textContent = 'Please login first';
        showToast('Please login first', 5000);
        return;
    }
    const response = await fetch(`/api/customers/${path}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({
            customerId: Number(document.getElementById('customerId').value),
            amount: Number(document.getElementById('amount').value)
        })
    });
    const data = await readResponse(response);
    if (response.ok) {
        showToast(data.message || `${path} successful`);
    } else {
        showToast(data.message || `${path} failed`, 5000);
    }
}

document.getElementById('depositBtn').addEventListener('click', () => sendAccountAction('deposit'));
document.getElementById('withdrawBtn').addEventListener('click', () => sendAccountAction('withdraw'));
document.getElementById('transferBtn').addEventListener('click', async () => {
    const token = localStorage.getItem(tokenStorageKey);
    if (!token) {
        showToast('Please login first', 5000);
        return;
    }
    const targetEmail = document.getElementById('targetEmail').value;
    if (!targetEmail) {
        showToast('Enter target customer email before transfer', 5000);
        return;
    }
    const response = await fetch('/api/customers/transfer', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({
            sourceCustomerId: Number(document.getElementById('customerId').value),
            targetEmail,
            amount: Number(document.getElementById('amount').value)
        })
    });
    const data = await readResponse(response);
    if (response.ok) {
        showToast(data.message || 'Transfer successful');
    } else {
        showToast(data.message || 'Transfer failed', 5000);
    }
});

async function authedFetch(path, options = {}) {
    const token = localStorage.getItem(tokenStorageKey);
    if (!token) {
        loginMessage.textContent = 'Please login first';
        showToast('Please login first', 5000);
        return null;
    }
    const response = await fetch(path, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
            ...(options.headers || {})
        }
    });
    return {response, data: await readResponse(response)};
}

document.getElementById('applyLoanBtn').addEventListener('click', async () => {
    const result = await authedFetch('/api/customers/apply-loan', {
        method: 'POST',
        body: JSON.stringify({
            customerId: Number(document.getElementById('customerId').value),
            amount: Number(document.getElementById('loanAmount').value),
            termMonths: Number(document.getElementById('loanMonths').value),
            rate: Number(document.getElementById('loanRate').value)
        })
    });
    if (result) {
        if (result.response.ok) {
            showToast('Loan application submitted');
        } else {
            showToast(result.data.message || 'Loan application failed', 5000);
        }
    }
});

document.getElementById('emiBtn').addEventListener('click', async () => {
    const principal = Number(document.getElementById('loanAmount').value);
    const months = Number(document.getElementById('loanMonths').value);
    const rate = Number(document.getElementById('loanRate').value);
    const result = await authedFetch(`/api/customers/calculate-emi?principal=${principal}&rate=${rate}&months=${months}`, {method: 'GET'});
    if (result && result.response.ok) {
        const data = result.data;
        if (typeof data === 'number') {
            showToast(`Monthly EMI: ${data.toFixed(2)}`);
        } else if (data && data.emi !== undefined) {
            showToast(`Monthly EMI: ${Number(data.emi).toFixed(2)}`);
        } else {
            showToast(`EMI calculated`);
        }
    } else if (result) {
        showToast(result.data.message || 'EMI calculation failed', 5000);
    }
});

document.getElementById('chatBtn').addEventListener('click', async () => {
    const result = await authedFetch('/api/customers/chat', {
        method: 'POST',
        body: JSON.stringify({
            customerId: Number(document.getElementById('customerId').value),
            sender: 'CUSTOMER',
            message: document.getElementById('chatMessage').value
        })
    });
    if (result) {
        if (result.response.ok) {
            showToast('Message sent');
            document.getElementById('chatMessage').value = '';
        } else {
            showToast(result.data.message || 'Failed to send message', 5000);
        }
    }
});

document.getElementById('transactionsBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const result = await authedFetch(`/api/customers/${id}/transactions`, {method: 'GET'});
    if (result) {
        if (result.response.ok) {
            const count = Array.isArray(result.data) ? result.data.length : 1;
            showToast(`Loaded ${count} transaction(s)`);
        } else {
            showToast(result.data.message || 'Failed to load transactions', 5000);
        }
    }
});

document.getElementById('loansBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const result = await authedFetch(`/api/customers/${id}/loans`, {method: 'GET'});
    if (result) {
        if (result.response.ok) {
            const count = Array.isArray(result.data) ? result.data.length : 1;
            showToast(`Loaded ${count} loan(s)`);
        } else {
            showToast(result.data.message || 'Failed to load loans', 5000);
        }
    }
});
