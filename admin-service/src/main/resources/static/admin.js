const loginForm = document.getElementById('loginForm');
const adminActions = document.getElementById('adminActions');
const loginMessage = document.getElementById('loginMessage');
const notificationContainer = document.getElementById('notificationContainer');
const logoutButton = document.getElementById('logoutButton');
const customerTableBody = document.getElementById('customerTableBody');
const loanTableBody = document.getElementById('loanTableBody');
let adminToken = null;

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

function customerPayload() {
    return {
        name: document.getElementById('customerName').value,
        email: document.getElementById('customerEmail').value,
        password: document.getElementById('customerPassword').value
    };
}

function statusPill(status) {
    const normalized = String(status || 'pending').toLowerCase();
    return `<span class="status-pill ${normalized}">${normalized}</span>`;
}

function money(value) {
    const amount = Number(value || 0);
    return amount.toLocaleString(undefined, {style: 'currency', currency: 'USD'});
}

function renderCustomers(customers) {
    const rows = Array.isArray(customers) ? customers : [customers].filter(Boolean);
    if (rows.length === 0) {
        customerTableBody.innerHTML = '<tr><td colspan="4">No customers found.</td></tr>';
        return;
    }
    customerTableBody.innerHTML = rows.map(customer => `
        <tr>
            <td>${customer.id ?? '-'}</td>
            <td>${customer.name ?? '-'}</td>
            <td>${customer.email ?? '-'}</td>
            <td>${statusPill(customer.active ? 'active' : 'inactive')}</td>
        </tr>
    `).join('');
}

function renderLoans(loans) {
    const rows = Array.isArray(loans) ? loans : [loans].filter(Boolean);
    if (rows.length === 0) {
        loanTableBody.innerHTML = '<tr><td colspan="6">No loan applications found.</td></tr>';
        return;
    }
    loanTableBody.innerHTML = rows.map(loan => `
        <tr>
            <td>${loan.id ?? '-'}</td>
            <td>${loan.customerId ?? '-'}</td>
            <td>${money(loan.amount)}</td>
            <td>${loan.termMonths ?? '-'} months</td>
            <td>${loan.rate ?? '-'}%</td>
            <td>${statusPill(loan.status)}</td>
        </tr>
    `).join('');
}

async function loadCustomers(query = '') {
    const response = await fetch(`/api/admin/customers?query=${encodeURIComponent(query)}`, {
        headers: {Authorization: `Bearer ${adminToken}`}
    });
    const data = await readResponse(response);
    if (response.ok) {
        renderCustomers(data);
        return data;
    }
    showToast(data.message || 'Failed to load customers', 5000);
    return [];
}

async function loadLoans() {
    const response = await fetch('/api/admin/loans', {
        headers: {Authorization: `Bearer ${adminToken}`}
    });
    const data = await readResponse(response);
    if (response.ok) {
        renderLoans(data);
        return data;
    }
    showToast(data.message || 'Failed to load loan approvals', 5000);
    return [];
}

async function refreshAdminLists() {
    await loadCustomers(document.getElementById('searchQuery').value);
    await loadLoans();
}

loginForm.addEventListener('submit', async event => {
    event.preventDefault();
    const response = await fetch('/api/admin/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            email: document.getElementById('adminEmail').value,
            password: document.getElementById('adminPassword').value
        })
    });
    const data = await readResponse(response);
    if (response.ok && data.token) {
        adminToken = data.token;
        loginMessage.textContent = 'Admin logged in';
        showToast('Login successful');
        adminActions.classList.remove('hidden');
        await refreshAdminLists();
    } else {
        loginMessage.textContent = data.message || 'Login failed';
        showToast(data.message || 'Login failed', 5000);
    }
});

async function sendAdminRequest(path, method = 'POST', body = null) {
    const response = await fetch(`/api/admin/${path}`, {
        method,
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${adminToken}`
        },
        body: body ? JSON.stringify(body) : undefined
    });
    return {response, data: await readResponse(response)};
}

document.getElementById('createCustomerBtn').addEventListener('click', async () => {
    const {response, data} = await sendAdminRequest('customer', 'POST', customerPayload());
    if (response.ok) {
        showToast('Customer created successfully');
        document.getElementById('customerName').value = '';
        document.getElementById('customerEmail').value = '';
        document.getElementById('customerPassword').value = '';
        await refreshAdminLists();
    } else {
        showToast(data.message || 'Failed to create customer', 5000);
    }
});

logoutButton.addEventListener('click', () => {
    fetch('/api/admin/logout', {method: 'POST'}).catch(() => {});
    adminToken = null;
    adminActions.classList.add('hidden');
    loginMessage.textContent = 'Admin logged out';
    customerTableBody.innerHTML = '<tr><td colspan="4">Login to load customers.</td></tr>';
    loanTableBody.innerHTML = '<tr><td colspan="6">Login to load loan approvals.</td></tr>';
    showToast('Logged out');
});

document.getElementById('searchCustomerBtn').addEventListener('click', async () => {
    const data = await loadCustomers(document.getElementById('searchQuery').value);
    showToast(`Found ${Array.isArray(data) ? data.length : 1} customer(s)`);
});

document.getElementById('refreshCustomersBtn').addEventListener('click', async () => {
    document.getElementById('searchQuery').value = '';
    const data = await loadCustomers();
    showToast(`Loaded ${Array.isArray(data) ? data.length : 1} customer(s)`);
});

document.getElementById('fetchCustomerBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const response = await fetch(`/api/admin/customer/${id}`, {
        headers: {Authorization: `Bearer ${adminToken}`}
    });
    const data = await readResponse(response);
    if (response.ok) {
        renderCustomers(data);
        showToast(`Loaded customer: ${data.name || 'Customer ' + id}`);
    } else {
        showToast(data.message || 'Failed to load customer', 5000);
    }
});

document.getElementById('updateCustomerBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const {response, data} = await sendAdminRequest(`customer/${id}`, 'PUT', customerPayload());
    if (response.ok) {
        showToast('Customer updated successfully');
        await refreshAdminLists();
    } else {
        showToast(data.message || 'Failed to update customer', 5000);
    }
});

document.getElementById('deleteCustomerBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const {response, data} = await sendAdminRequest(`customer/${id}`, 'DELETE');
    if (response.ok) {
        showToast('Customer deleted successfully');
        await refreshAdminLists();
    } else {
        showToast(data.message || 'Failed to delete customer', 5000);
    }
});

document.getElementById('deactivateBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const {response, data} = await sendAdminRequest(`customer/${id}/deactivate`, 'PUT');
    if (response.ok) {
        showToast('Customer deactivated');
        await refreshAdminLists();
    } else {
        showToast(data.message || 'Failed to deactivate customer', 5000);
    }
});

document.getElementById('activateBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const {response, data} = await sendAdminRequest(`customer/${id}/activate`, 'PUT');
    if (response.ok) {
        showToast('Customer activated');
        await refreshAdminLists();
    } else {
        showToast(data.message || 'Failed to activate customer', 5000);
    }
});

document.getElementById('approveLoanBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const {response, data} = await sendAdminRequest(`loan/${id}/approve`, 'PUT');
    if (response.ok) {
        showToast('Loan approved');
        await loadLoans();
    } else {
        showToast(data.message || 'Failed to approve loan', 5000);
    }
});

document.getElementById('denyLoanBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const {response, data} = await sendAdminRequest(`loan/${id}/deny`, 'PUT');
    if (response.ok) {
        showToast('Loan denied');
        await loadLoans();
    } else {
        showToast(data.message || 'Failed to deny loan', 5000);
    }
});

document.getElementById('refreshLoansBtn').addEventListener('click', async () => {
    const data = await loadLoans();
    showToast(`Loaded ${Array.isArray(data) ? data.length : 1} loan application(s)`);
});

document.getElementById('adminChatBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const message = document.getElementById('adminChatMessage').value;
    const {response, data} = await sendAdminRequest(`customer/${id}/chat`, 'POST', message);
    if (response.ok) {
        showToast('Message sent');
        document.getElementById('adminChatMessage').value = '';
    } else {
        showToast(data.message || 'Failed to send message', 5000);
    }
});
