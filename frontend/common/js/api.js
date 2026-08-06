const API_PORT = sessionStorage.apiPort = new URLSearchParams(location.search).get('apiPort') || sessionStorage.apiPort || '8081';
const API_URL = `http://127.0.0.1:${API_PORT}/api`;
window.onRetailReady = callback => {
    if (window.retailReadyDetail) {
        callback({detail: window.retailReadyDetail});
        return;
    }
    document.addEventListener('retail:ready', callback, {once: true});
};
window.RetailAPI = {
    async request(path, options = {}) {
        const loader = document.getElementById('global-loader');
        loader?.classList.add('show');
        try {
            const response = await axios({
                url: `${API_URL}${path}`,
                method: options.method || 'GET',
                data: options.body,
                headers: options.headers,
                withCredentials: true
            });
            return response.data;
        } catch (error) {
            if (error.response?.status === 401 && !path.includes('/auth/login')) {
                location.href = '../login/login.html';
            }
            const data = error.response?.data;
            const fields = data?.validationErrors
                ? Object.values(data.validationErrors).join(', ') : '';
            throw new Error(fields || data?.message || 'Unable to contact the server');
        } finally {
            loader?.classList.remove('show');
        }
    },
    money(value) {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency', currency: 'INR'
        }).format(Number(value || 0));
    },
    date(value, time = false) {
        if (!value) return '-';
        const format = time
            ? {dateStyle: 'medium', timeStyle: 'short'}
            : {dateStyle: 'medium'};
        return new Intl.DateTimeFormat('en-IN', format).format(new Date(value));
    },
    escape(value) {
        const element = document.createElement('span');
        element.textContent = value ?? '';
        return element.innerHTML;
    },
    status(value) {
        const text = String(value || '').replaceAll('_', ' ');
        const css = text.toLowerCase().replaceAll(' ', '-');
        return `<span class="status-pill status-${css}">${this.escape(text)}</span>`;
    },
    empty(columns, message = 'No records found') {
        return `<tr><td class="empty-state" colspan="${columns}">${message}</td></tr>`;
    },
    toast(message, type = 'success') {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.textContent = message;
        document.body.appendChild(toast);
        setTimeout(() => toast.remove(), 3000);
    },
    query(values) {
        return new URLSearchParams(values).toString();
    }
};
