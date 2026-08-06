let catalogue = [];
let cart = [];
let promotions = [];
let couponId = '';
window.onRetailReady(loadCatalogue);
async function loadCatalogue() {
    try {
        const result = await Promise.all([
            RetailAPI.request('/products?size=1000'),
            RetailAPI.request('/inventory'),
            RetailAPI.request('/promotions/active')
        ]);
        const quantities = Object.fromEntries(result[1].map(item => [item.product.id, item.quantity]));
        catalogue = result[0].content
            .filter(product => product.productStatus === 'ACTIVE')
            .map(product => ({...product, stock: quantities[product.id] || 0}));
        promotions = result[2];
        document.getElementById('coupon-select').innerHTML = '<option value="">Select coupon</option>' +
            result[2].map(item => `<option value="${item.id}">${RetailAPI.escape(item.code)} - ${item.discountPercentage}%</option>`).join('');
        showProducts();
        showCart();
    } catch (error) { RetailAPI.toast(error.message, 'error'); }
}
function showProducts() {
    const text = document.getElementById('billing-search').value.toLowerCase();
    if (!text.trim()) return document.getElementById('product-results').innerHTML = '';
    const products = catalogue.filter(item => item.name.toLowerCase().includes(text)).slice(0, 6);
    document.getElementById('product-results').innerHTML = products.map(item => `
        <article class="result-card"><h3>${RetailAPI.escape(item.name)}</h3>
            <p>${item.stock} units available</p>
            <footer><strong>${RetailAPI.money(item.price)}</strong>
            <button class="btn btn-primary" onclick="addItem(${item.id})"
                ${item.stock === 0 ? 'disabled' : ''}>Add</button></footer>
        </article>`).join('') || '<p class="empty-state">No products found</p>';
}
window.addItem = id => {
    const product = catalogue.find(item => item.id === id);
    const existing = cart.find(item => item.id === id);
    if (!product || (existing && existing.quantity >= product.stock)) {
        RetailAPI.toast('No more stock available', 'error');
        return;
    }
    if (existing) existing.quantity += 1;
    else cart.push({...product, quantity: 1});
    showCart();
};
window.changeItem = (id, change) => {
    const item = cart.find(product => product.id === id);
    if (!item) return;
    item.quantity = Math.min(item.stock, item.quantity + change);
    if (item.quantity <= 0) cart = cart.filter(product => product.id !== id);
    showCart();
};
function calculateTotals() {
    const subtotal = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
    const promotion = promotions.find(item => item.id == couponId);
    const discount = cart.reduce((sum, item) => sum + couponDiscount(item, promotion), 0);
    const tax = cart.reduce((sum, item) => {
        const base = item.price * item.quantity;
        const taxable = base - couponDiscount(item, promotion);
        const rate = Number(item.taxPercentage || 0) / 100;
        return sum + taxable * rate;
    }, 0);
    return {subtotal, discount, tax, total: subtotal - discount + tax};
}
function couponDiscount(item, promotion) {
    if (!promotion) return 0;
    const base = item.price * item.quantity;
    if (base < Number(promotion.minimumPurchase || 0)) return 0;
    const product = promotion.product?.id === item.id;
    const category = promotion.applicableCategory === item.category;
    const store = !promotion.product && !promotion.applicableCategory;
    return product || category || store ? base * promotion.discountPercentage / 100 : 0;
}
function showCart() {
    document.getElementById('cart-items').innerHTML = cart.length
        ? cart.map(item => `<tr><td>${RetailAPI.escape(item.name)}</td>
            <td>${RetailAPI.money(item.price)}</td><td><div class="quantity">
            <button onclick="changeItem(${item.id},-1)">-</button><span>${item.quantity}</span>
            <button onclick="changeItem(${item.id},1)">+</button></div></td>
            <td>${RetailAPI.money(item.price * item.quantity)}</td>
            <td><button class="row-btn" onclick="changeItem(${item.id},-${item.quantity})">Remove</button></td></tr>`).join('')
        : RetailAPI.empty(5, 'Cart is empty');
    const total = calculateTotals();
    document.getElementById('subtotal').textContent = RetailAPI.money(total.subtotal);
    document.getElementById('discount-total').textContent = `-${RetailAPI.money(total.discount)}`;
    document.getElementById('tax-total').textContent = RetailAPI.money(total.tax);
    document.getElementById('grand-total').textContent = RetailAPI.money(total.total);
    if (couponId) document.getElementById('coupon-message').textContent =
        total.discount ? `Coupon applied · Save ${RetailAPI.money(total.discount)}` : 'Coupon applied · No eligible item yet';
}
document.getElementById('billing-search').oninput = showProducts;
document.getElementById('clear-cart').onclick = () => { cart = []; showCart(); };
document.getElementById('apply-coupon').onclick = () => {
    couponId = document.getElementById('coupon-select').value;
    if (!couponId) return RetailAPI.toast('Select a coupon', 'error');
    document.getElementById('remove-coupon').disabled = false;
    showCart();
};
document.getElementById('remove-coupon').onclick = () => {
    couponId = '';
    document.getElementById('coupon-select').value = '';
    document.getElementById('coupon-message').textContent = 'No coupon applied';
    document.getElementById('remove-coupon').disabled = true;
    showCart();
};
