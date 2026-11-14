(function () {
    'use strict';
    // Los formularios de institución y tipo tienen sus validadores específicos.
    const ids = ['formLogin', 'formAltaUsuario', 'formModificarUsuario', 'formAltaEvento',
        'formAltaEdicion', 'formAltaPatrocinio', 'formRegistro'];

    for (const id of ids) {
        const form = document.getElementById(id);
        if (!form) continue;
        const tocados = new Set();
        let enviado = false;
        const elementos = () => Array.from(form.elements).filter(c => c.name
            && !['hidden', 'submit', 'button', 'reset'].includes(c.type));
        const activos = () => elementos().filter(c => !c.disabled && !c.readOnly);
        const ayudas = new Map();

        function ayuda(campo) {
            const clave = ['radio', 'checkbox'].includes(campo.type) ? campo.name : campo;
            if (!ayudas.has(clave)) {
                const nodo = document.createElement('div');
                nodo.id = id + 'Error' + ayudas.size;
                nodo.className = 'invalid-feedback d-block';
                nodo.setAttribute('aria-live', 'polite');
                const grupo = form.querySelector('[data-error-grupo="' + campo.name + '"]');
                if (grupo) grupo.appendChild(nodo);
                else (campo.closest('label') || campo).insertAdjacentElement('afterend', nodo);
                ayudas.set(clave, nodo);
            }
            const nodo = ayudas.get(clave);
            // Conserva posibles instrucciones ya asociadas al campo.
            const referencias = new Set((campo.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean));
            referencias.add(nodo.id);
            campo.setAttribute('aria-describedby', Array.from(referencias).join(' '));
            return nodo;
        }

        function mensaje(campo) {
            if (campo.disabled || campo.readOnly) return '';
            const valor = campo.value;
            if (campo.name === 'categorias' && id === 'formAltaEvento') {
                return activos().some(c => c.name === 'categorias' && c.checked)
                    ? '' : 'Elegí al menos una categoría.';
            }
            if (campo.type === 'radio') {
                return campo.required && !activos().some(c => c.name === campo.name && c.checked)
                    ? 'Seleccioná una opción.' : '';
            }
            const vacio = campo.type === 'password' ? valor.length === 0 : valor.trim().length === 0;
            if (campo.required && vacio) return 'Este campo es obligatorio.';
            if (campo.type === 'select-one' && !vacio && Array.from(campo.selectedOptions).some(o => o.disabled))
                return 'Seleccioná una opción disponible.';
            if (campo.name === 'confirmacion') {
                const nueva = form.elements.contrasenia.value;
                if (nueva !== valor) return 'Las contraseñas no coinciden.';
            }
            if (campo.type === 'email' && !vacio && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor.trim()))
                return 'Ingresá un correo electrónico válido.';
            if (campo.type === 'file' && campo.files[0] && campo.files[0].size > 2 * 1024 * 1024)
                return 'La imagen supera el tamaño máximo de 2 MB.';
            const estado = campo.validity;
            // Los validadores de fecha y patrocinio mantienen la autoridad de sus reglas existentes.
            if (estado.customError) return campo.validationMessage;
            if (estado.badInput) return 'Ingresá un valor válido para este campo.';
            if (estado.typeMismatch) return campo.type === 'url'
                ? 'Ingresá una URL válida.' : 'Revisá el formato de este campo.';
            if (estado.rangeOverflow && campo.type === 'date') return 'La fecha de nacimiento no puede ser futura.';
            if (estado.rangeUnderflow || estado.rangeOverflow) return 'El valor está fuera del rango permitido.';
            if (estado.stepMismatch) return 'El valor no respeta el incremento permitido.';
            if (!estado.valid) return 'Revisá el formato de este campo.';
            return '';
        }

        function mostrar(campo) {
            const texto = mensaje(campo);
            ayuda(campo).textContent = texto;
            campo.classList.toggle('is-invalid', Boolean(texto));
            if (texto) campo.setAttribute('aria-invalid', 'true');
            else campo.removeAttribute('aria-invalid');
            return !texto;
        }

        for (const campo of elementos()) ayuda(campo);
        form.addEventListener('invalid', evento => mostrar(evento.target), true);
        for (const tipo of ['input', 'change']) form.addEventListener(tipo, evento => {
            tocados.add(evento.target);
            const relacionados = ['contrasenia', 'confirmacion'].includes(evento.target.name);
            for (const campo of elementos()) {
                if (enviado || tocados.has(campo) || campo.name === evento.target.name
                    || (relacionados && campo.name === 'confirmacion' && campo.value)) mostrar(campo);
            }
        });
        form.addEventListener('submit', evento => {
            enviado = true;
            let primero = null;
            for (const campo of activos()) if (!mostrar(campo) && !primero) primero = campo;
            // También funciona cuando todavía no hay categorías cargadas para elegir.
            if (id === 'formAltaEvento' && !activos().some(c => c.name === 'categorias')) {
                const grupo = form.querySelector('[data-error-grupo="categorias"]');
                grupo.textContent = 'No hay categorías disponibles. No se puede crear el evento.';
                evento.preventDefault();
            }
            if (primero) { evento.preventDefault(); primero.focus(); }
        });
        form.addEventListener('reset', () => {
            enviado = false; tocados.clear();
            for (const nodo of ayudas.values()) nodo.textContent = '';
            for (const campo of elementos()) {
                campo.setCustomValidity('');
                campo.classList.remove('is-invalid'); campo.removeAttribute('aria-invalid');
            }
        });
        // Si JS no carga, required/type/min/step conservan la validación HTML5.
        // Con JS activo los mensajes se muestran junto a cada campo.
        form.noValidate = true;
    }
}());
