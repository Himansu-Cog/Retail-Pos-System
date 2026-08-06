window.RetailValidation = {
    namePattern: /^[A-Za-z][A-Za-z0-9 .&'()-]*$/,
    personPattern: /^[A-Za-z][A-Za-z .'-]*$/,

    setMessage(input, message) {
        input.setCustomValidity(message);
        if (!input.dataset.validationResetBound) {
            const clear = () => input.setCustomValidity('');
            input.addEventListener('input', clear);
            input.addEventListener('change', clear);
            input.dataset.validationResetBound = 'true';
        }
    },

    validateText(input, label, personOnly = false) {
        const value = input.value.trim();
        const pattern = personOnly ? this.personPattern : this.namePattern;
        this.setMessage(input, value && !pattern.test(value)
            ? `${label}: start with a letter; use valid characters only.`
            : '');
    },

    validateNonNegative(input, label, allowZero = true) {
        const value = Number(input.value);
        const invalid = !Number.isFinite(value) || value < 0 || (!allowZero && value === 0);
        this.setMessage(input, invalid
            ? `${label}: enter ${allowZero ? '0 or more' : 'a value above 0'}.`
            : '');
    },

    validateDates(start, end) {
        this.setMessage(end, start.value && end.value && end.value <= start.value
            ? 'End date must be after start date'
            : '');
    },

    check(form) {
        if (!form.checkValidity()) {
            form.reportValidity();
            return false;
        }
        return true;
    }
};
