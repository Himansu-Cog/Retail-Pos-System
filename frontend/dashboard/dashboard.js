window.onRetailReady(async () => {
    try {
        const data = await RetailAPI.request('/dashboard');
        showMetrics(data);
        showLowStock(data.lowStockProducts);
        showTransactions(data.recentTransactions);
    } catch (error) {
        RetailAPI.toast(error.message, 'error');
    }
});

function showMetrics(data) {
    const metrics = [
        ['Total products', data.totalProducts],
        ['Inventory units', data.totalInventory],
        ["Today's sales", RetailAPI.money(data.todaySales)],
        ['Weekly sales', RetailAPI.money(data.weeklySales)],
        ['Monthly sales', RetailAPI.money(data.monthlySales)],
        ['Active promotions', data.activePromotions],
        ['Low stock items', data.lowStockProducts.length],
        ['Recent sales', data.recentTransactions.length]
    ];
    document.getElementById('metric-grid').innerHTML = metrics.map(item => `
        <article class="frost-panel metric-card">
            <div class="metric-label"><span>${item[0]}</span></div>
            <strong class="metric-value">${item[1]}</strong>
        </article>`
    ).join('');
}

function showLowStock(items) {
    document.getElementById('low-stock-body').innerHTML = items.length
        ? items.map(item => `
            <tr><td>${RetailAPI.escape(item.product.name)}</td>
            <td>${item.quantity}</td><td>${item.reorderLevel}</td></tr>`).join('')
        : RetailAPI.empty(3, 'Stock levels are healthy');
}

function showTransactions(items) {
    document.getElementById('recent-body').innerHTML = items.length
        ? items.map(sale => `
            <tr><td>${sale.receiptNumber}</td>
            <td>${RetailAPI.escape(sale.cashier.fullName)}</td>
            <td>${RetailAPI.money(sale.totalAmount)}</td></tr>`).join('')
        : RetailAPI.empty(3);
}
