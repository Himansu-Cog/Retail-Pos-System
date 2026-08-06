let inventory = [];
let action = 'ADD';
let canEdit = false;
const form = document.getElementById('stock-form');
const value = id => document.getElementById(id).value;
window.onRetailReady(async event => {
    canEdit = ['ADMIN', 'STORE_MANAGER', 'INVENTORY_ASSOCIATE'].includes(event.detail.user.role);
    if (!canEdit) document.querySelector('.edit-panel').classList.add('hidden');
    await loadInventory();
});
async function loadInventory() {
    try {
        inventory = await RetailAPI.request('/inventory');
        render();
        summary();
        if (canEdit && inventory.length) selectStock(inventory[0].id);
    } catch (error) { RetailAPI.toast(error.message, 'error'); }
}
function render() {
    const text = value('inventory-search').toLowerCase();
    const filter = value('stock-filter');
    const rows = inventory.filter(item => (item.product.name.toLowerCase().includes(text) ||
        item.product.supplierMaster.toLowerCase().includes(text)) && (!filter || state(item) === filter));
    document.getElementById('inventory-count').textContent = `${rows.length} records`;
    document.getElementById('inventory-body').innerHTML = rows.length ? rows.map(item => `<tr>
        <td>${RetailAPI.escape(item.product.name)}</td><td>${RetailAPI.escape(item.product.supplierMaster)}</td>
        <td>${RetailAPI.money(item.product.price)}</td><td>${item.quantity}</td><td>${item.reorderLevel}</td>
        <td>${RetailAPI.status(label(state(item)))}</td><td>${canEdit ? `<button class="row-btn" onclick="selectStock(${item.id})">Select</button>` : '-'}</td></tr>`).join('') : RetailAPI.empty(7);
}
function summary() {
    document.getElementById('total-products').textContent = inventory.length;
    const ids = ['in-stock', 'low-stock', 'out-stock'];
    ['healthy', 'low', 'out'].forEach((key, i) =>
        document.getElementById(ids[i]).textContent = inventory.filter(row => state(row) === key).length);
}
window.selectStock = id => {
    const item = inventory.find(row => row.id === id);
    if (!item) return;
    document.getElementById('selected-inventory-id').value = item.id;
    document.getElementById('selected-product-id').value = item.product.id;
    document.getElementById('selected-product').value = item.product.name;
    document.getElementById('stock-threshold').value = item.reorderLevel;
    document.getElementById('stock-change').value = '';
    document.getElementById('stock-remarks').value = '';
};
document.querySelector('.stock-action').onclick = event => {
    if (!event.target.dataset.action) return;
    action = event.target.dataset.action;
    document.querySelectorAll('.stock-action button').forEach(button =>
        button.classList.toggle('active', button === event.target));
};
form.onsubmit = async event => {
    event.preventDefault();
    if (!RetailValidation.check(form)) return;
    const item = inventory.find(row => row.id == value('selected-inventory-id'));
    const change = Number(value('stock-change')) * (action === 'ADD' ? 1 : -1);
    if (item.quantity + change < 0) return RetailAPI.toast('Remove quantity exceeds current stock', 'error');
    const product = {...item.product, name: value('selected-product').trim()};
    const query = RetailAPI.query({quantity: item.quantity + change, threshold: value('stock-threshold'),
        reason: value('stock-reason'), remarks: value('stock-remarks').trim()});
    try {
        await RetailAPI.request(`/inventory/${item.id}?${query}`, {method: 'PUT', body: product});
        RetailAPI.toast('Product and stock updated');
        await loadInventory();
    } catch (error) { RetailAPI.toast(error.message, 'error'); }
};
document.getElementById('inventory-search').oninput = render;
document.getElementById('stock-filter').onchange = render;
function state(item) { return item.quantity === 0 ? 'out' : item.quantity <= item.reorderLevel ? 'low' : 'healthy'; }
function label(text) { return text.replaceAll('_', ' '); }
