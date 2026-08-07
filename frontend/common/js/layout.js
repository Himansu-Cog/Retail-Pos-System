(async function loadLayout() {
    const target = document.getElementById('app-header');
    if (!target) return;
    try {
        const user = await RetailAPI.request('/auth/me');
        const role = user.role;
        const manager = ['ADMIN', 'STORE_MANAGER'].includes(role);
        const cashier = ['ADMIN', 'STORE_MANAGER', 'CASHIER'].includes(role);
        const links = [
            ['dashboard', 'Dashboard', '../dashboard/dashboard.html', true],
            ['products', 'Products', '../products/products.html', manager],
            ['inventory', 'Inventory', '../inventory/inventory.html', manager],
            ['billing', 'Billing', '../billing/billing.html', cashier],
            ['promotions', 'Promotions', '../promotions/promotions.html', manager],
            ['reports', 'Reports', '../reports/reports.html', manager],
            ['users', 'Users', '../users/users.html', role === 'ADMIN']
        ];
        const page = document.body.dataset.page;
        const navigation = links.filter(link => link[3]).map(link =>
            `<a href="${link[2]}" class="${page === link[0] ? 'active' : ''}">${link[1]}</a>`
        ).join('');
        target.innerHTML = `
            <header class="frost-header"><div class="header-inner">
                <a class="brand" href="../dashboard/dashboard.html"><span class="brand-symbol">RS</span>
                    <span><strong>Retail System</strong><small>Store Operations</small></span></a>
                <button class="mobile-menu-button" id="menu-button">&#9776;</button>
                <nav class="header-nav" id="header-nav">${navigation}</nav>
                <div class="header-tools"><span class="role-chip">${role.replaceAll('_', ' ')}</span>
                    <button class="header-signout" id="signout-button">Sign out</button></div>
            </div></header>`;
        document.getElementById('menu-button').onclick = () =>
            document.getElementById('header-nav').classList.toggle('open');
        document.getElementById('signout-button').onclick = async () => {
            await RetailAPI.request('/auth/logout', {method: 'POST'});
            location.href = '../login/login.html';
        };
        window.retailReadyDetail = {user};
        document.dispatchEvent(new CustomEvent('retail:ready', {detail: {user}}));
    } catch (error) {
        RetailAPI.toast(error.message, 'error');
    }
})();
