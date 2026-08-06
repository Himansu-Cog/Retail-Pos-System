const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');

loginForm.addEventListener('submit', async event => {
    event.preventDefault();
    if (!RetailValidation.check(loginForm)) return;

    loginError.textContent = '';
    const credentials = new URLSearchParams({
        username: document.getElementById('username').value.trim(),
        password: document.getElementById('password').value
    });

    try {
        await RetailAPI.request('/auth/login', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: credentials.toString()
        });
        location.href = '../dashboard/dashboard.html?v=9';
    } catch (error) {
        loginError.textContent = error.message;
    }
});
