let products = [];
let stock = [];
const productForm = document.getElementById('product-form');
window.onRetailReady(async event => {
    if (!['ADMIN', 'STORE_MANAGER'].includes(event.detail.user.role)) {
        document.querySelector('.create-panel').classList.add('hidden');
    }
    await loadProducts();
});
async function loadProducts() {
    try {
        const result = await Promise.all([
            RetailAPI.request('/products?size=1000'),
            RetailAPI.request('/inventory')
        ]);
        products = result[0].content;
        stock = result[1];
        renderProducts();
    } catch (error) {
        RetailAPI.toast(error.message, 'error');
    }
}
function renderProducts() {
    const text = document.getElementById('product-search').value.toLowerCase();
    const quantities = Object.fromEntries(stock.map(row => [row.product.id, row.quantity]));
    const rows = products.filter(product => product.name.toLowerCase().includes(text) ||
        product.supplierMaster.toLowerCase().includes(text));
    document.getElementById('product-body').innerHTML = rows.length
        ? rows.map(product => `<tr><td>PROD-${product.id}</td>
            <td>${RetailAPI.escape(product.name)}</td><td>${label(product.category)}</td>
            <td>${RetailAPI.money(product.price)}</td><td>${quantities[product.id] || 0}</td>
            <td>${product.barcode}</td><td>${RetailAPI.status(product.productStatus)}</td></tr>`).join('')
        : RetailAPI.empty(7);
}
productForm.onsubmit = async event => {
    event.preventDefault();
    if (!RetailValidation.check(productForm)) return;
    const body = {
        name: field('name'),
        supplierMaster: field('supplier'),
        category: field('category'),
        price: Number(field('price')),
        productStatus: field('status')
    };
    const query = RetailAPI.query({
        quantity: field('quantity'),
        threshold: field('threshold')
    });
    try {
        await RetailAPI.request(`/products?${query}`, {method: 'POST', body});
        productForm.reset();
        RetailAPI.toast('Product saved');
        await loadProducts();
    } catch (error) {
        RetailAPI.toast(error.message, 'error');
    }
};
document.getElementById('product-search').oninput = renderProducts;
function field(id) { return document.getElementById(id).value.trim(); }
function label(value) { return value.replaceAll('_', ' '); }
