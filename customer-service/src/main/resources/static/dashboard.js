const tokenStorageKey = 'customerJwtToken';
const customerIdStorageKey = 'customerId';
const customerNameStorageKey = 'customerName';
const customerEmailStorageKey = 'customerEmail';
const notificationContainer = document.getElementById('notificationContainer');
const logoutButton = document.getElementById('logoutButton');
const customerIdInput = document.getElementById('customerId');
const heroCustomerId = document.getElementById('heroCustomerId');
const sidebarUsername = document.getElementById('sidebarUsername');
const emiResult = document.getElementById('emiResult');
const transactionTableBody = document.getElementById('transactionTableBody');
const loanTableBody = document.getElementById('loanTableBody');
const chatThread = document.getElementById('chatThread');
const accountBalance = document.getElementById('accountBalance');
const accountBalanceMeta = document.getElementById('accountBalanceMeta');

// Check authentication on page load
function checkAuth() {
    const token = localStorage.getItem(tokenStorageKey);
    if (!token) {
        window.location.href = 'login.html';
    }
}

checkAuth();

function currentCustomerId() {
    const storedId = localStorage.getItem(customerIdStorageKey);
    const typedId = customerIdInput.value;
    return Number(storedId || typedId);
}

if (localStorage.getItem(customerIdStorageKey)) {
    customerIdInput.value = localStorage.getItem(customerIdStorageKey);
    heroCustomerId.textContent = localStorage.getItem(customerIdStorageKey);
}

const storedCustomerName = localStorage.getItem(customerNameStorageKey) || localStorage.getItem(customerEmailStorageKey);
if (storedCustomerName) {
    sidebarUsername.textContent = storedCustomerName;
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

function formatCurrency(value) {
    return Number(value || 0).toLocaleString(undefined, {
        style: 'currency',
        currency: 'INR',
        maximumFractionDigits: 2
    });
}

function formatDate(value) {
    if (!value) {
        return '-';
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString();
}

function statusPill(value) {
    const status = String(value || 'PENDING').toLowerCase();
    return `<span class="status-pill ${status}">${status}</span>`;
}

function setTableMessage(tableBody, colspan, message) {
    tableBody.innerHTML = `<tr><td colspan="${colspan}">${message}</td></tr>`;
}

function renderChat(messages) {
    if (!messages.length) {
        chatThread.innerHTML = '<div class="empty-chat">No messages yet. Start a conversation with bank support.</div>';
        return;
    }

    chatThread.innerHTML = messages.map(message => {
        const mine = String(message.sender || '').toUpperCase() === 'CUSTOMER';
        return `
            <div class="chat-message ${mine ? 'mine' : 'bank'}">
                <div class="chat-bubble">
                    <div class="chat-meta">
                        <strong>${mine ? 'You' : 'Bank Support'}</strong>
                        <span>${formatDate(message.sentAt)}</span>
                    </div>
                    <p>${message.message || ''}</p>
                </div>
            </div>
        `;
    }).join('');
    chatThread.scrollTop = chatThread.scrollHeight;
}

// Logout handler
logoutButton.addEventListener('click', () => {
    const token = localStorage.getItem(tokenStorageKey);
    if (token) {
        fetch('/api/customers/logout', {
            method: 'POST',
            headers: {'Authorization': `Bearer ${token}`}
        }).catch(() => {});
    }
    localStorage.removeItem(tokenStorageKey);
    localStorage.removeItem(customerIdStorageKey);
    localStorage.removeItem(customerNameStorageKey);
    localStorage.removeItem(customerEmailStorageKey);
    showToast('Logged out');
    setTimeout(() => {
        window.location.href = 'index.html';
    }, 500);
});

async function sendAccountAction(path) {
    const token = localStorage.getItem(tokenStorageKey);
    if (!token) {
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
            customerId: currentCustomerId(),
            amount: Number(document.getElementById('amount').value)
        })
    });
    const data = await readResponse(response);
    if (response.ok) {
        showToast(data.message || `${path} successful`);
        document.getElementById('amount').value = '';
        await loadBalance();
        await loadTransactions();
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
            sourceCustomerId: currentCustomerId(),
            targetEmail,
            amount: Number(document.getElementById('amount').value)
        })
    });
    const data = await readResponse(response);
    if (response.ok) {
        showToast(data.message || 'Transfer successful');
        document.getElementById('amount').value = '';
        document.getElementById('targetEmail').value = '';
        await loadBalance();
        await loadTransactions();
    } else {
        showToast(data.message || 'Transfer failed', 5000);
    }
});

async function authedFetch(path, options = {}) {
    const token = localStorage.getItem(tokenStorageKey);
    if (!token) {
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
            customerId: currentCustomerId(),
            loanType: document.getElementById('loanType').value,
            amount: Number(document.getElementById('loanAmount').value),
            termMonths: Number(document.getElementById('loanMonths').value),
            rate: Number(document.getElementById('loanRate').value)
        })
    });
    if (result) {
        if (result.response.ok) {
            showToast('Loan application submitted');
            await loadLoans();
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
        let emi = null;
        if (typeof data === 'number') {
            emi = data;
        } else if (data && data.emi !== undefined) {
            emi = Number(data.emi);
        }
        emiResult.innerHTML = `
            <span>Monthly EMI</span>
            <strong>${emi === null ? 'Calculated' : formatCurrency(emi)}</strong>
        `;
    } else if (result) {
        showToast(result.data.message || 'EMI calculation failed', 5000);
    }
});

document.getElementById('chatBtn').addEventListener('click', async () => {
    const message = document.getElementById('chatMessage').value.trim();
    if (!message) {
        showToast('Type a message before sending', 5000);
        return;
    }
    const result = await authedFetch('/api/customers/chat', {
        method: 'POST',
        body: JSON.stringify({
            customerId: currentCustomerId(),
            sender: 'CUSTOMER',
            message
        })
    });
    if (result) {
        if (result.response.ok) {
            showToast('Message sent');
            document.getElementById('chatMessage').value = '';
            await loadChat();
        } else {
            showToast(result.data.message || 'Failed to send message', 5000);
        }
    }
});

async function loadChat() {
    chatThread.innerHTML = '<div class="empty-chat">Loading messages...</div>';
    const result = await authedFetch('/api/customers/chat', {method: 'GET'});
    if (!result) {
        return;
    }
    if (result.response.ok) {
        const messages = Array.isArray(result.data) ? result.data : [result.data].filter(Boolean);
        renderChat(messages);
    } else {
        chatThread.innerHTML = '<div class="empty-chat">Unable to load messages.</div>';
        showToast(result.data.message || 'Failed to load chat messages', 5000);
    }
}

async function loadBalance(showSuccess = false) {
    accountBalance.textContent = 'Loading...';
    accountBalanceMeta.textContent = 'Checking account balance';
    const result = await authedFetch('/api/customers/balance', {method: 'GET'});
    if (!result) {
        return;
    }
    if (result.response.ok) {
        accountBalance.textContent = formatCurrency(result.data.balance);
        const accountType = result.data.accountType || 'account';
        const accountId = result.data.accountId ?? '-';
        accountBalanceMeta.textContent = `${accountType} account #${accountId}`;
        if (showSuccess) {
            showToast('Balance refreshed');
        }
    } else {
        accountBalance.textContent = 'Unavailable';
        accountBalanceMeta.textContent = result.data.message || 'Could not load balance';
        showToast(result.data.message || 'Failed to load balance', 5000);
    }
}

async function loadTransactions() {
    const id = currentCustomerId();
    setTableMessage(transactionTableBody, 5, 'Loading transactions...');
    const result = await authedFetch(`/api/customers/${id}/transactions`, {method: 'GET'});
    if (result) {
        if (result.response.ok) {
            const transactions = Array.isArray(result.data) ? result.data : [result.data].filter(Boolean);
            if (transactions.length === 0) {
                setTableMessage(transactionTableBody, 5, 'No transactions found.');
                return;
            }
            transactionTableBody.innerHTML = transactions.map(transaction => `
                <tr>
                    <td>${transaction.transactionId || '-'}</td>
                    <td>${transaction.type || '-'}</td>
                    <td>${formatCurrency(transaction.amount)}</td>
                    <td>${transaction.description || '-'}</td>
                    <td>${formatDate(transaction.timestamp)}</td>
                </tr>
            `).join('');
        } else {
            setTableMessage(transactionTableBody, 5, result.data.message || 'Failed to load transactions');
            showToast(result.data.message || 'Failed to load transactions', 5000);
        }
    }
}

async function loadLoans() {
    const id = currentCustomerId();
    setTableMessage(loanTableBody, 7, 'Loading loan applications...');
    const result = await authedFetch(`/api/customers/${id}/loans`, {method: 'GET'});
    if (result) {
        if (result.response.ok) {
            const loans = Array.isArray(result.data) ? result.data : [result.data].filter(Boolean);
            if (loans.length === 0) {
                setTableMessage(loanTableBody, 7, 'No loan applications found.');
                return;
            }
            loanTableBody.innerHTML = loans.map(loan => `
                <tr>
                    <td>${loan.id ?? '-'}</td>
                    <td>${loan.loanType || '-'}</td>
                    <td>${formatCurrency(loan.amount)}</td>
                    <td>${loan.termMonths ?? '-'} months</td>
                    <td>${loan.rate ?? '-'}%</td>
                    <td>${statusPill(loan.status)}</td>
                    <td>${formatDate(loan.appliedAt)}</td>
                </tr>
            `).join('');
        } else {
            setTableMessage(loanTableBody, 7, result.data.message || 'Failed to load loans');
            showToast(result.data.message || 'Failed to load loans', 5000);
        }
    }
}

document.getElementById('transactionsBtn').addEventListener('click', loadTransactions);
document.getElementById('loansBtn').addEventListener('click', loadLoans);
document.getElementById('balanceBtn').addEventListener('click', () => loadBalance(true));
document.getElementById('checkBalanceBtn').addEventListener('click', () => loadBalance(true));

loadBalance();
loadTransactions();
loadLoans();
loadChat();
