const tokenStorageKey = 'adminJwtToken';
const adminNameStorageKey = 'adminName';
const adminEmailStorageKey = 'adminEmail';
const notificationContainer = document.getElementById('notificationContainer');
const logoutButton = document.getElementById('logoutButton');
const customerTableBody = document.getElementById('customerTableBody');
const loanTableBody = document.getElementById('loanTableBody');
const selectedCustomerLabel = document.getElementById('selectedCustomerLabel');
const adminChatThread = document.getElementById('adminChatThread');
const sidebarUsername = document.getElementById('sidebarUsername');
let currentCustomers = [];

// Check authentication on page load
function checkAuth() {
    const token = localStorage.getItem(tokenStorageKey);
    if (!token) {
        window.location.href = 'login.html';
    }
}

checkAuth();

const storedAdminName = localStorage.getItem(adminNameStorageKey) || localStorage.getItem(adminEmailStorageKey);
if (storedAdminName) {
    sidebarUsername.textContent = storedAdminName;
}

function showToast(message, duration = 4000) {
    const toast = document.createElement('div');
    const isError = duration > 4000;
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

function statusPill(value) {
    const status = String(value || 'PENDING').toLowerCase();
    return `<span class="status-pill ${status}">${status}</span>`;
}

function formatDate(value) {
    if (!value) {
        return '-';
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString();
}

function formatCurrency(value) {
    const amount = Number(value || 0);
    return amount.toLocaleString(undefined, {
        style: 'currency',
        currency: 'INR',
        maximumFractionDigits: 2
    });
}

function setTableMessage(tableBody, colspan, message) {
    tableBody.innerHTML = `<tr><td colspan="${colspan}">${message}</td></tr>`;
}

function clearCustomerForm() {
    document.getElementById('customerId').value = '';
    document.getElementById('customerName').value = '';
    document.getElementById('customerEmail').value = '';
    document.getElementById('customerPassword').value = '';
    selectedCustomerLabel.textContent = 'None';
    adminChatThread.innerHTML = '<div class="empty-chat">Select a customer to view the conversation.</div>';
}

function selectCustomer(customer) {
    document.getElementById('customerId').value = customer.id || '';
    document.getElementById('customerName').value = customer.name || '';
    document.getElementById('customerEmail').value = customer.email || '';
    document.getElementById('customerPassword').value = '';
    selectedCustomerLabel.textContent = customer.id ? `${customer.name || 'Customer'} (#${customer.id})` : 'None';
    loadAdminChat(customer.id);
}

function renderAdminChat(messages) {
    if (!messages.length) {
        adminChatThread.innerHTML = '<div class="empty-chat">No messages yet for this customer.</div>';
        return;
    }

    adminChatThread.innerHTML = messages.map(message => {
        const mine = String(message.sender || '').toUpperCase() === 'ADMIN';
        return `
            <div class="chat-message ${mine ? 'mine' : 'customer'}">
                <div class="chat-bubble">
                    <div class="chat-meta">
                        <strong>${mine ? 'Admin' : 'Customer'}</strong>
                        <span>${formatDate(message.sentAt)}</span>
                    </div>
                    <p>${message.message || ''}</p>
                </div>
            </div>
        `;
    }).join('');
    adminChatThread.scrollTop = adminChatThread.scrollHeight;
}

async function loadAdminChat(customerId) {
    if (!customerId) {
        adminChatThread.innerHTML = '<div class="empty-chat">Select a customer to view the conversation.</div>';
        return;
    }
    adminChatThread.innerHTML = '<div class="empty-chat">Loading conversation...</div>';
    const {response, data} = await sendAdminRequest(`customer/${customerId}/chat`, 'GET');
    if (response.ok) {
        const messages = Array.isArray(data) ? data : [data].filter(Boolean);
        renderAdminChat(messages);
    } else {
        adminChatThread.innerHTML = '<div class="empty-chat">Unable to load conversation.</div>';
        showToast(data.message || 'Failed to load chat messages', 5000);
    }
}

// Logout handler
logoutButton.addEventListener('click', () => {
    localStorage.removeItem(tokenStorageKey);
    localStorage.removeItem(adminNameStorageKey);
    localStorage.removeItem(adminEmailStorageKey);
    showToast('Logged out');
    setTimeout(() => {
        window.location.href = 'index.html';
    }, 500);
});

async function sendAdminRequest(path, method = 'POST', body = null) {
    const token = localStorage.getItem(tokenStorageKey);
    const response = await fetch(`/api/admin/${path}`, {
        method,
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`
        },
        body: body ? JSON.stringify(body) : undefined
    });
    return {response, data: await readResponse(response)};
}

async function loadCustomers(query = '') {
    setTableMessage(customerTableBody, 6, 'Loading customers...');
    const path = `customers?query=${encodeURIComponent(query)}`;
    const {response, data} = await sendAdminRequest(path, 'GET');
    if (!response.ok) {
        setTableMessage(customerTableBody, 6, data.message || 'Failed to load customers');
        showToast(data.message || 'Failed to load customers', 5000);
        return;
    }

    const customers = Array.isArray(data) ? data : [data].filter(Boolean);
    currentCustomers = customers;
    if (customers.length === 0) {
        setTableMessage(customerTableBody, 6, 'No customers found.');
        return;
    }

    customerTableBody.innerHTML = customers.map(customer => `
        <tr>
            <td>${customer.id ?? '-'}</td>
            <td>${customer.name || '-'}</td>
            <td>${customer.email || '-'}</td>
            <td>${statusPill(customer.active ? 'active' : 'inactive')}</td>
            <td>${formatDate(customer.registeredAt)}</td>
            <td>
                <div class="row-actions">
                    <button class="neutral" type="button" data-customer-action="select" data-customer-id="${customer.id}">Select</button>
                    <button type="button" data-customer-action="${customer.active ? 'deactivate' : 'activate'}" data-customer-id="${customer.id}">
                        ${customer.active ? 'Deactivate' : 'Activate'}
                    </button>
                    <button class="danger" type="button" data-customer-action="delete" data-customer-id="${customer.id}">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function loadLoans() {
    setTableMessage(loanTableBody, 9, 'Loading loan approvals...');
    const {response, data} = await sendAdminRequest('loans', 'GET');
    if (!response.ok) {
        setTableMessage(loanTableBody, 9, data.message || 'Failed to load loan approvals');
        showToast(data.message || 'Failed to load loan approvals', 5000);
        return;
    }

    const loans = Array.isArray(data) ? data : [data].filter(Boolean);
    if (loans.length === 0) {
        setTableMessage(loanTableBody, 9, 'No loan applications found.');
        return;
    }

    loanTableBody.innerHTML = loans.map(loan => {
        const pending = String(loan.status || '').toUpperCase() === 'PENDING';
        return `
            <tr>
                <td>${loan.id ?? '-'}</td>
                <td>${loan.customerId ?? '-'}</td>
                <td>${loan.loanType || '-'}</td>
                <td>${formatCurrency(loan.amount)}</td>
                <td>${loan.termMonths ?? '-'} months</td>
                <td>${loan.rate ?? '-'}%</td>
                <td>${statusPill(loan.status)}</td>
                <td>${formatDate(loan.appliedAt)}</td>
                <td>
                    <div class="row-actions">
                        <button type="button" data-loan-action="approve" data-customer-id="${loan.customerId}" ${pending ? '' : 'disabled'}>Approve</button>
                        <button class="warning" type="button" data-loan-action="deny" data-customer-id="${loan.customerId}" ${pending ? '' : 'disabled'}>Deny</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

document.getElementById('createCustomerBtn').addEventListener('click', async () => {
    const {response, data} = await sendAdminRequest('customer', 'POST', customerPayload());
    if (response.ok) {
        showToast('Customer created successfully');
        clearCustomerForm();
        await loadCustomers();
    } else {
        showToast(data.message || 'Failed to create customer', 5000);
    }
});

document.getElementById('searchCustomerBtn').addEventListener('click', async () => {
    const query = document.getElementById('searchQuery').value;
    await loadCustomers(query);
});

document.getElementById('updateCustomerBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    if (!id) {
        showToast('Select a customer from the table before updating', 5000);
        return;
    }
    const {response, data} = await sendAdminRequest(`customer/${id}`, 'PUT', customerPayload());
    if (response.ok) {
        showToast('Customer updated successfully');
        await loadCustomers();
    } else {
        showToast(data.message || 'Failed to update customer', 5000);
    }
});

document.getElementById('adminChatBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    if (!id) {
        showToast('Select a customer from the table before sending chat', 5000);
        return;
    }
    const message = document.getElementById('adminChatMessage').value.trim();
    if (!message) {
        showToast('Type a reply before sending', 5000);
        return;
    }
    const {response, data} = await sendAdminRequest(`customer/${id}/chat`, 'POST', {message});
    if (response.ok) {
        showToast('Reply sent');
        document.getElementById('adminChatMessage').value = '';
        await loadAdminChat(id);
    } else {
        showToast(data.message || 'Failed to send message', 5000);
    }
});

document.getElementById('refreshCustomersBtn').addEventListener('click', () => {
    document.getElementById('searchQuery').value = '';
    loadCustomers();
});

document.getElementById('refreshLoansBtn').addEventListener('click', loadLoans);
document.getElementById('clearCustomerBtn').addEventListener('click', clearCustomerForm);

customerTableBody.addEventListener('click', async event => {
    const button = event.target.closest('button[data-customer-action]');
    if (!button) {
        return;
    }

    const action = button.dataset.customerAction;
    const id = Number(button.dataset.customerId);
    const customer = currentCustomers.find(item => Number(item.id) === id);

    if (action === 'select') {
        if (customer) {
            selectCustomer(customer);
        }
        return;
    }

    if (action === 'delete') {
        const {response, data} = await sendAdminRequest(`customer/${id}`, 'DELETE');
        if (response.ok) {
            showToast('Customer deleted successfully');
            clearCustomerForm();
            await loadCustomers();
        } else {
            showToast(data.message || 'Failed to delete customer', 5000);
        }
        return;
    }

    const {response, data} = await sendAdminRequest(`customer/${id}/${action}`, 'PUT');
    if (response.ok) {
        showToast(action === 'activate' ? 'Customer activated' : 'Customer deactivated');
        await loadCustomers();
    } else {
        showToast(data.message || `Failed to ${action} customer`, 5000);
    }
});

loanTableBody.addEventListener('click', async event => {
    const button = event.target.closest('button[data-loan-action]');
    if (!button) {
        return;
    }

    const action = button.dataset.loanAction;
    const customerId = button.dataset.customerId;
    const {response, data} = await sendAdminRequest(`loan/${customerId}/${action}`, 'PUT');
    if (response.ok) {
        showToast(`Loan ${action === 'approve' ? 'approved' : 'denied'}`);
        await loadLoans();
    } else {
        showToast(data.message || `Failed to ${action} loan`, 5000);
    }
});

loadCustomers();
loadLoans();
