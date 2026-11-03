(function () {
    const form = document.getElementById('formAltaEdicion');
    if (!form) return;

    const inicio = form.elements.fechaInicio;
    const fin = form.elements.fechaFin;

    function validarFechas() {
        const invertidas = Number.isFinite(inicio.valueAsNumber)
            && Number.isFinite(fin.valueAsNumber)
            && fin.valueAsNumber < inicio.valueAsNumber;
        fin.setCustomValidity(invertidas ? 'La fecha de fin no puede ser anterior al inicio.' : '');
    }

    for (const campo of [inicio, fin]) {
        campo.addEventListener('input', validarFechas);
        campo.addEventListener('change', validarFechas);
    }
    form.addEventListener('submit', function (evento) {
        validarFechas();
        if (!form.checkValidity()) {
            evento.preventDefault();
            form.reportValidity();
        }
    });
    form.addEventListener('reset', function () { fin.setCustomValidity(''); });
    validarFechas();
}());
