(function () {
    'use strict';
    const obligatorio = mensaje => campo => campo.value.trim() ? '' : mensaje;
    const numero = /^[+-]?(?:\d+(?:\.\d*)?|\.\d+)(?:[eE][+-]?\d+)?$/;
    const reglas = {
        formAltaInstitucion: {
            nombre: obligatorio('El nombre es obligatorio.'),
            descripcion: obligatorio('La descripción es obligatoria.'),
            sitioWeb(campo) {
                const valor = campo.value.trim();
                if (!valor) return 'El sitio web es obligatorio.';
                // Mismo formato del Servlet; la URL no se consulta ni se usa como autorización.
                return /^https?:\/\/[^\s.]+\.[^\s]+$/i.test(valor) ? ''
                    : 'Ingresá una URL HTTP o HTTPS válida (por ejemplo, https://ejemplo.com).';
            },
            imagen(campo) {
                return campo.files && campo.files[0] && campo.files[0].size > 2 * 1024 * 1024
                    ? 'La imagen supera el tamaño máximo de 2 MB.' : '';
            }
        },
        formAltaTipoRegistro: {
            edicion: obligatorio('Seleccioná una edición.'),
            nombre: obligatorio('El nombre es obligatorio.'),
            descripcion: obligatorio('La descripción es obligatoria.'),
            costo(campo) {
                const valor = campo.value.trim();
                if (campo.validity.badInput) return 'El costo debe ser un número válido.';
                if (!valor) return 'El costo es obligatorio.';
                const costo = Number(valor);
                if (!numero.test(valor) || !Number.isFinite(Math.fround(costo)) || costo < 0)
                    return 'El costo debe ser un número finito mayor o igual a cero.';
                // Conserva step=0.01 del HTML; no introduce una precisión nueva.
                return campo.validity.stepMismatch ? 'Usá incrementos de 0,01 para el costo.' : '';
            },
            cupo(campo) {
                const valor = campo.value.trim();
                if (campo.validity.badInput) return 'El cupo debe ser un número entero válido.';
                if (!valor) return 'El cupo es obligatorio.';
                // El backend recibe un int, no acepta decimales ni notación exponencial.
                return /^[+]?\d+$/.test(valor) && Number(valor) > 0 && Number(valor) <= 2147483647
                    ? '' : 'El cupo debe ser un entero entre 1 y 2147483647.';
            }
        }
    };

    for (const [id, validadores] of Object.entries(reglas)) {
        const form = document.getElementById(id);
        if (!form) continue;

        function validar(nombre) {
            const campo = form.elements[nombre];
            campo.setCustomValidity('');
            let mensaje = validadores[nombre](campo);
            if (!mensaje && !campo.validity.valid) mensaje = 'Revisá el formato de este campo.';
            campo.setCustomValidity(mensaje);
            campo.classList.toggle('is-invalid', Boolean(mensaje));
            if (mensaje) campo.setAttribute('aria-invalid', 'true');
            else campo.removeAttribute('aria-invalid');
            const ayuda = document.getElementById(campo.id + 'Error');
            if (ayuda) ayuda.textContent = mensaje;
            return !mensaje;
        }

        for (const nombre of Object.keys(validadores)) {
            for (const evento of ['input', 'change'])
                form.elements[nombre].addEventListener(evento, () => validar(nombre));
        }
        form.addEventListener('submit', function (evento) {
            let primero = null;
            for (const nombre of Object.keys(validadores)) {
                if (!validar(nombre) && !primero) primero = form.elements[nombre];
            }
            if (primero) {
                evento.preventDefault();
                primero.focus();
            }
        });
        form.addEventListener('reset', function () {
            for (const nombre of Object.keys(validadores)) {
                const campo = form.elements[nombre];
                campo.setCustomValidity('');
                campo.classList.remove('is-invalid');
                campo.removeAttribute('aria-invalid');
                const ayuda = document.getElementById(campo.id + 'Error');
                if (ayuda) ayuda.textContent = '';
            }
        });
        // Solo con JS activo: el submit valida todos los campos y muestra errores junto a ellos.
        // Sin el script se mantiene la validación HTML5 nativa.
        form.noValidate = true;
    }
}());
