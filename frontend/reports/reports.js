const settlementModal = document.getElementById('settlement-modal');
let cashiers = [];

window.onRetailReady(async () => {
    const today = new Date().toISOString().slice(0, 10);
    document.getElementById('start-date').value = today;
    document.getElementById('end-date').value = today;
    document.getElementById('settlement-date').value = today;
    try {
        cashiers = await RetailAPI.request('/staff/cashiers');
        document.getElementById('settlement-cashier').innerHTML = cashiers.map(user =>
            `<option value="${user.id}">${RetailAPI.escape(user.fullName)}</option>`
        ).join('');
        await Promise.all([runReport(), loadSettlements()]);
    } catch (error) { RetailAPI.toast(error.message, 'error'); }
});

async function runReport() {
    const query = RetailAPI.query({
        startDate: document.getElementById('start-date').value,
        endDate: document.getElementById('end-date').value
    });
    try {
        const report = await RetailAPI.request(`/reports/sales?${query}`);
        document.getElementById('report-summary').innerHTML = `
            <article class="frost-panel metric-card"><span>Transactions</span><strong>${report.transactionCount}</strong></article>
            <article class="frost-panel metric-card"><span>Total sales</span><strong>${RetailAPI.money(report.totalSales)}</strong></article>`;
        document.getElementById('transaction-count').textContent = `${report.transactions.length} records`;
        document.getElementById('report-body').innerHTML = report.transactions.length
            ? report.transactions.map(sale => `<tr><td>${sale.receiptNumber}</td>
                <td>${RetailAPI.date(sale.transactionDate, true)}</td>
                <td>${RetailAPI.escape(sale.cashier.fullName)}</td><td>${sale.paymentMode}</td>
                <td>${RetailAPI.status(sale.status)}</td><td>${RetailAPI.money(sale.totalAmount)}</td></tr>`).join('')
            : RetailAPI.empty(6);
    } catch (error) { RetailAPI.toast(error.message, 'error'); }
}

async function loadSettlements() {
    const rows = await RetailAPI.request('/reports/settlements');
    document.getElementById('settlement-body').innerHTML = rows.length
        ? rows.map(item => `<tr><td>${RetailAPI.date(item.settlementDate)}</td>
            <td>${RetailAPI.escape(item.cashier.fullName)}</td><td>${item.transactionCount}</td>
            <td>${RetailAPI.money(item.cashAmount)}</td><td>${RetailAPI.money(item.cardAmount)}</td>
            <td>${RetailAPI.money(item.upiAmount)}</td><td>${RetailAPI.money(item.totalAmount)}</td></tr>`).join('')
        : RetailAPI.empty(7);
}

document.getElementById('run-report').onclick = runReport;
document.getElementById('new-settlement').onclick = () => settlementModal.showModal();
document.getElementById('settlement-close').onclick = () => settlementModal.close();
document.getElementById('settlement-cancel').onclick = () => settlementModal.close();
document.getElementById('settlement-form').onsubmit = async event => {
    event.preventDefault();
    const query = RetailAPI.query({
        date: document.getElementById('settlement-date').value,
        cashierId: document.getElementById('settlement-cashier').value,
        notes: document.getElementById('settlement-notes').value.trim()
    });
    try {
        await RetailAPI.request(`/reports/settlements?${query}`, {method: 'POST'});
        settlementModal.close();
        RetailAPI.toast('Settlement created');
        await loadSettlements();
    } catch (error) { RetailAPI.toast(error.message, 'error'); }
};
