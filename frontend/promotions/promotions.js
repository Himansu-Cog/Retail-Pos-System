let promotions = [];
const promotionForm = document.getElementById('promotion-form');
window.onRetailReady(loadPromotions);
async function loadPromotions() {
    try {
        promotions = await RetailAPI.request('/promotions');
        renderPromotions();
    } catch (error) {
        RetailAPI.toast(error.message, 'error');
    }
}
function renderPromotions() {
    const text = document.getElementById('promotion-search').value.toLowerCase();
    const rows = promotions.filter(item =>
        item.code.toLowerCase().includes(text) || item.name.toLowerCase().includes(text)
    );
    document.getElementById('promotion-body').innerHTML = rows.length
        ? rows.map(item => `<tr><td>PRM-${item.id}</td><td>${RetailAPI.escape(item.name)}</td>
            <td>${item.discountPercentage}%</td><td>${RetailAPI.date(item.startDate)} - ${RetailAPI.date(item.endDate)}</td>
            <td>${RetailAPI.status(item.status)}</td><td><button class="row-btn" onclick="editPromotion(${item.id})">Edit</button>
            <button class="row-btn remove-btn" onclick="deletePromotion(${item.id})">Delete</button></td></tr>`).join('')
        : RetailAPI.empty(6);
}
promotionForm.addEventListener('submit', async event => {
    event.preventDefault();
    if (!RetailValidation.check(promotionForm)) return;
    const id = field('promotion-id');
    const body = {
        code: field('code'), name: field('promotion-name'),
        discountPercentage: Number(field('discount')),
        minimumPurchase: Number(field('minimum')),
        startDate: `${field('start-date')}T00:00:00`,
        endDate: `${field('end-date')}T23:59:59`,
        status: field('promotion-status'), description: '',
        product: null, applicableCategory: null
    };
    try {
        await RetailAPI.request(id ? `/promotions/${id}` : '/promotions', {
            method: id ? 'PUT' : 'POST', body
        });
        clearPromotion();
        RetailAPI.toast('Promotion saved');
        await loadPromotions();
    } catch (error) { RetailAPI.toast(error.message, 'error'); }
});
window.editPromotion = id => {
    const item = promotions.find(row => row.id === id);
    const values = [item.id, item.code, item.name, item.discountPercentage,
        item.minimumPurchase, item.status];
    ['promotion-id', 'code', 'promotion-name', 'discount', 'minimum',
        'promotion-status'].forEach((name, index) => field(name, values[index]));
    field('start-date', item.startDate.slice(0, 10));
    field('end-date', item.endDate.slice(0, 10));
};
window.deletePromotion = async id => {
    try {
        await RetailAPI.request(`/promotions/${id}`, {method: 'DELETE'});
        await loadPromotions();
    } catch (error) { RetailAPI.toast(error.message, 'error'); }
};
function clearPromotion() { promotionForm.reset(); field('promotion-id', ''); }
function field(id, set) { const input = document.getElementById(id); if (set !== undefined) input.value = set; return input.value.trim(); }
function label(value) { return value.replaceAll('_', ' '); }
document.getElementById('promotion-clear').onclick = clearPromotion;
document.getElementById('promotion-search').oninput = renderPromotions;
document.getElementById('start-date').onchange = event =>
    document.getElementById('end-date').min = event.target.value;
