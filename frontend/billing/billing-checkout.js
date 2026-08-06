const receiptModal = document.getElementById('receipt-modal');
document.getElementById('checkout').onclick = async () => {
    if (!cart.length) return RetailAPI.toast('Add products to the cart', 'error');
    const total = calculateTotals();
    if (couponId && !total.discount) {
        return RetailAPI.toast('Coupon is not eligible for this cart', 'error');
    }
    const paymentMode = document.querySelector('input[name="payment"]:checked').value;
    const items = cart.map(item => ({product: {id: item.id}, quantity: item.quantity}));
    const query = couponId ? `?promotionId=${couponId}` : '';
    try {
        const sale = await RetailAPI.request(`/transactions${query}`, {
            method: 'POST', body: {paymentMode, items}
        });
        showReceipt(sale);
        cart = [];
        couponId = '';
        document.getElementById('coupon-message').textContent = 'No coupon applied';
        document.getElementById('remove-coupon').disabled = true;
        await loadCatalogue();
    } catch (error) {
        RetailAPI.toast(error.message, 'error');
    }
};
function showReceipt(sale) {
    document.getElementById('receipt-content').innerHTML = `
        <div class="receipt-brand"><strong>Retail System</strong><small>Payment receipt</small></div>
        <div class="receipt-meta"><span>${sale.receiptNumber}</span>
        <span>${RetailAPI.date(sale.transactionDate, true)}</span></div>
        <p>Payment: ${sale.paymentMode}</p><div class="receipt-totals">
        <span>Subtotal <b>${RetailAPI.money(sale.subtotal)}</b></span>
        <span>Discount <b>-${RetailAPI.money(sale.discountAmount)}</b></span>
        <span>Tax <b>${RetailAPI.money(sale.taxAmount)}</b></span>
        <strong>Total <b>${RetailAPI.money(sale.totalAmount)}</b></strong></div>`;
    receiptModal.showModal();
}
document.getElementById('receipt-confirm').onclick = () => {
    receiptModal.close();
    RetailAPI.toast('Sale confirmed');
};
document.getElementById('receipt-cancel').onclick = () => receiptModal.close();
document.getElementById('print-receipt').onclick = () => window.print();
