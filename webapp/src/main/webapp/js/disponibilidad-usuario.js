(function () {
    'use strict';
    const form = document.getElementById('formAltaUsuario');
    if (!form) return;
    const url = form.action.replace(/\/alta-usuario(?:\?.*)?$/, '/disponibilidad-usuario');

    for (const nombre of ['nickname', 'correo']) {
        const campo = form.elements[nombre];
        const estado = document.getElementById(nombre + 'Disponibilidad');
        let secuencia = 0;
        let temporizador;
        let controlador;

        campo.addEventListener('input', () => {
            clearTimeout(temporizador);
            if (controlador) controlador.abort();
            const actual = ++secuencia;
            const valor = campo.value.trim();
            campo.setCustomValidity('');
            estado.textContent = '';
            estado.classList.remove('text-success', 'text-danger');
            if (!valor || !campo.checkValidity()) return;
            estado.textContent = 'Comprobando disponibilidad…';
            temporizador = setTimeout(async () => {
                controlador = new AbortController();
                try {
                    const parametros = new URLSearchParams({tipo: nombre, valor});
                    const respuesta = await fetch(url + '?' + parametros, {
                        signal: controlador.signal, cache: 'no-store', headers: {'Accept': 'application/json'}
                    });
                    if (!respuesta.ok) throw new Error('Servicio no disponible');
                    const datos = await respuesta.json();
                    if (actual !== secuencia || campo.value.trim() !== valor) return;
                    const mensaje = datos.disponible ? '' : 'Este ' + (nombre === 'correo' ? 'correo' : 'nickname') + ' ya está registrado.';
                    campo.setCustomValidity(mensaje);
                    estado.textContent = mensaje || 'Disponible';
                    estado.classList.toggle('text-danger', Boolean(mensaje));
                    estado.classList.toggle('text-success', !mensaje);
                } catch (error) {
                    if (error.name === 'AbortError' || actual !== secuencia) return;
                    estado.textContent = 'No se pudo comprobar. Se validará al registrarte.';
                    estado.classList.remove('text-success', 'text-danger');
                }
            }, 350);
        });
    }
}());
