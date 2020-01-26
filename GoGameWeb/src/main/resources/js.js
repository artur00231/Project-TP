function submit(a) {
    var form = document.createElement('form');
    form.method = 'post';
    form.action = '/index';

    var hiddenField = document.createElement('input');
    hiddenField.type = 'hidden';
    hiddenField.name = 'move';
    hiddenField.value = a;

    form.appendChild(hiddenField);

    document.body.appendChild(form);
    form.submit();
}