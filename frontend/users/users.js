let users = [];
const userRoles = ['ADMIN', 'STORE_MANAGER', 'CASHIER', 'INVENTORY_ASSOCIATE'];
const userModal = document.getElementById('user-modal');
const userForm = document.getElementById('user-form');
window.onRetailReady(loadUsers);
async function loadUsers() {
    try {
        const result = await RetailAPI.request('/users?size=1000');
        users = result.content;
        renderUsers();
    } catch (error) { RetailAPI.toast(error.message, 'error'); }
}
function renderUsers() {
    const text = document.getElementById('user-search').value.toLowerCase();
    const rows = users.filter(user => {
        const matches = user.fullName.toLowerCase().includes(text)
            || user.username.toLowerCase().includes(text);
        return matches;
    });
    document.getElementById('user-count').textContent = `${rows.length} accounts`;
    document.getElementById('user-body').innerHTML = rows.map(user => `<tr>
        <td>${RetailAPI.escape(user.fullName)}</td><td>${user.username}</td>
        <td>${user.email}</td><td>${label(user.role)}</td>
        <td>${RetailAPI.status(user.active ? 'ACTIVE' : 'INACTIVE')}</td>
        <td>${RetailAPI.date(user.createdAt)}</td><td>
        <button class="row-btn" onclick="openUser(${user.id})">Edit</button></td></tr>`).join('')
        || RetailAPI.empty(7);
}
window.openUser = id => {
    const user = users.find(item => item.id === id) || {};
    document.getElementById('user-modal-title').textContent = id ? 'Edit user' : 'Add user';
    const values = [user.id || '', user.fullName || '', user.username || '',
        user.email || '', user.role || userRoles[0], String(user.active ?? true), ''];
    ['user-id', 'full-name', 'user-name', 'email', 'user-role',
        'user-active', 'user-password'].forEach((field, index) => setField(field, values[index]));
    document.getElementById('user-password').required = !id;
    userModal.showModal();
};
userForm.onsubmit = async event => {
    event.preventDefault();
    if (!RetailValidation.check(userForm)) return;
    const id = getField('user-id');
    const body = {
        fullName: getField('full-name'), username: getField('user-name'),
        email: getField('email'), password: getField('user-password'),
        role: getField('user-role'), active: getField('user-active') === 'true'
    };
    try {
        await RetailAPI.request(id ? `/users/${id}` : '/users', {
            method: id ? 'PUT' : 'POST', body
        });
        userModal.close();
        RetailAPI.toast('User saved');
        await loadUsers();
    } catch (error) { RetailAPI.toast(error.message, 'error'); }
};
function setField(id, value) { document.getElementById(id).value = value; }
function getField(id) { return document.getElementById(id).value.trim(); }
function label(value) { return value.replaceAll('_', ' '); }
document.getElementById('add-user').onclick = () => openUser();
document.getElementById('user-close').onclick = () => userModal.close();
document.getElementById('user-cancel').onclick = () => userModal.close();
document.getElementById('user-search').oninput = renderUsers;
