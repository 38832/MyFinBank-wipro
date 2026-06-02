const registerForm = document.getElementById('registerForm');
const loginForm = document.getElementById('loginForm');
const logoutButton = document.getElementById('logoutButton');
const accountActions = document.getElementById('accountActions');
const loginMessage = document.getElementById('loginMessage');
const registerMessage = document.getElementById('registerMessage');
const output = document.getElementById('output');
const tokenStorageKey = 'customerJwtToken';

async function readResponse(response) {
    const text = await response.text();
    try {
        return text ? JSON.parse(text) : {};
    } catch (error) {
        return {message: text};
    }
}

function showResult(data) {
    output.textContent = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
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
    registerMessage.textContent = data.message || JSON.stringify(data);
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
        accountActions.classList.remove('hidden');
    } else {
        loginMessage.textContent = data.message || 'Login failed';
    }
});

logoutButton.addEventListener('click', () => {
    fetch('/api/customers/logout', {method: 'POST'}).catch(() => {});
    localStorage.removeItem(tokenStorageKey);
    accountActions.classList.add('hidden');
    loginMessage.textContent = 'Logged out';
});

async function sendAccountAction(path) {
    const token = localStorage.getItem(tokenStorageKey);
    if (!token) {
        loginMessage.textContent = 'Please login first';
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
    showResult(data.message || data);
}

document.getElementById('depositBtn').addEventListener('click', () => sendAccountAction('deposit'));
document.getElementById('withdrawBtn').addEventListener('click', () => sendAccountAction('withdraw'));
document.getElementById('transferBtn').addEventListener('click', async () => {
    const token = localStorage.getItem(tokenStorageKey);
    if (!token) return;
    const targetEmail = prompt('Enter beneficiary email');
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
    showResult(data.message || data);
});

async function authedFetch(path, options = {}) {
    const token = localStorage.getItem(tokenStorageKey);
    if (!token) {
        loginMessage.textContent = 'Please login first';
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
    return readResponse(response);
}

document.getElementById('applyLoanBtn').addEventListener('click', async () => {
    const data = await authedFetch('/api/customers/apply-loan', {
        method: 'POST',
        body: JSON.stringify({
            customerId: Number(document.getElementById('customerId').value),
            amount: Number(document.getElementById('loanAmount').value),
            termMonths: Number(document.getElementById('loanMonths').value),
            rate: Number(document.getElementById('loanRate').value)
        })
    });
    if (data) showResult(data.message || data);
});

document.getElementById('emiBtn').addEventListener('click', async () => {
    const principal = Number(document.getElementById('loanAmount').value);
    const months = Number(document.getElementById('loanMonths').value);
    const rate = Number(document.getElementById('loanRate').value);
    const data = await authedFetch(`/api/customers/calculate-emi?principal=${principal}&rate=${rate}&months=${months}`, {method: 'GET'});
    if (data) showResult({emi: data});
});

document.getElementById('chatBtn').addEventListener('click', async () => {
    const data = await authedFetch('/api/customers/chat', {
        method: 'POST',
        body: JSON.stringify({
            customerId: Number(document.getElementById('customerId').value),
            sender: 'CUSTOMER',
            message: document.getElementById('chatMessage').value
        })
    });
    if (data) showResult(data.message || data);
});

document.getElementById('transactionsBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const data = await authedFetch(`/api/customers/${id}/transactions`, {method: 'GET'});
    if (data) showResult(data);
});

document.getElementById('loansBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const data = await authedFetch(`/api/customers/${id}/loans`, {method: 'GET'});
    if (data) showResult(data);
});
