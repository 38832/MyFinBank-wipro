const loginForm = document.getElementById('loginForm');
const adminActions = document.getElementById('adminActions');
const loginMessage = document.getElementById('loginMessage');
const customerResult = document.getElementById('customerResult');
const logoutButton = document.getElementById('logoutButton');
let adminToken = null;

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
        adminActions.classList.remove('hidden');
    } else {
        loginMessage.textContent = data.message || 'Login failed';
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
    return await readResponse(response);
}

document.getElementById('createCustomerBtn').addEventListener('click', async () => {
    const data = await sendAdminRequest('customer', 'POST', customerPayload());
    customerResult.textContent = data.message || JSON.stringify(data, null, 2);
});

logoutButton.addEventListener('click', () => {
    fetch('/api/admin/logout', {method: 'POST'}).catch(() => {});
    adminToken = null;
    adminActions.classList.add('hidden');
    loginMessage.textContent = 'Admin logged out';
});

document.getElementById('searchCustomerBtn').addEventListener('click', async () => {
    const query = encodeURIComponent(document.getElementById('searchQuery').value);
    const response = await fetch(`/api/admin/customers?query=${query}`, {
        headers: {Authorization: `Bearer ${adminToken}`}
    });
    const data = await readResponse(response);
    customerResult.textContent = JSON.stringify(data, null, 2);
});

document.getElementById('fetchCustomerBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const response = await fetch(`/api/admin/customer/${id}`, {
        headers: {Authorization: `Bearer ${adminToken}`}
    });
    const data = await readResponse(response);
    customerResult.textContent = JSON.stringify(data, null, 2);
});

document.getElementById('updateCustomerBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const data = await sendAdminRequest(`customer/${id}`, 'PUT', customerPayload());
    customerResult.textContent = data.message || JSON.stringify(data, null, 2);
});

document.getElementById('deleteCustomerBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const data = await sendAdminRequest(`customer/${id}`, 'DELETE');
    customerResult.textContent = data.message || JSON.stringify(data, null, 2);
});

document.getElementById('deactivateBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const data = await sendAdminRequest(`customer/${id}/deactivate`, 'PUT');
    customerResult.textContent = data.message || JSON.stringify(data);
});

document.getElementById('activateBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const data = await sendAdminRequest(`customer/${id}/activate`, 'PUT');
    customerResult.textContent = data.message || JSON.stringify(data);
});

document.getElementById('approveLoanBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const data = await sendAdminRequest(`loan/${id}/approve`, 'PUT');
    customerResult.textContent = data.message || JSON.stringify(data);
});

document.getElementById('denyLoanBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('customerId').value);
    const data = await sendAdminRequest(`loan/${id}/deny`, 'PUT');
    customerResult.textContent = data.message || JSON.stringify(data);
});
