(() => {
    const form = document.getElementById('formAltaUsuario');
    if (!form) return;
    for (const campo of ['nickname', 'correo']) {
        const input = document.getElementById(campo);
        const mensaje = document.getElementById(`disponibilidad-${campo}`);
        let temporizador;
        let peticion;
        let version = 0;
        input.addEventListener('input', () => {
            const actual = ++version;
            clearTimeout(temporizador);
            if (peticion) peticion.abort();
            input.setCustomValidity('');
            mensaje.textContent = '';
            mensaje.className = 'form-text';
            const valor = input.value.trim();
            if (!valor || !input.validity.valid) return;
            mensaje.textContent = 'Consultando disponibilidad…';
            temporizador = setTimeout(async () => {
                peticion = new AbortController();
                const url = new URL(form.dataset.disponibilidadUrl, window.location.origin);
                url.searchParams.set('campo', campo);
                url.searchParams.set('valor', valor);
                try {
                    const respuesta = await fetch(url, {signal: peticion.signal, headers: {'Accept': 'application/json'}});
                    if (!respuesta.ok) throw new Error('Sin respuesta');
                    const datos = await respuesta.json();
                    if (actual !== version) return;
                    if (typeof datos.disponible !== 'boolean') throw new Error('Respuesta inválida');
                    mensaje.textContent = datos.disponible ? 'Disponible.' : 'No disponible o no válido.';
                    mensaje.className = datos.disponible ? 'form-text text-success' : 'form-text text-danger';
                    input.setCustomValidity(datos.disponible ? '' : 'Este valor no está disponible o no es válido.');
                } catch (error) {
                    if (error.name === 'AbortError' || actual !== version) return;
                    mensaje.textContent = 'No pudimos verificarlo ahora. Se comprobará al registrar el usuario.';
                    mensaje.className = 'form-text text-muted';
                    input.setCustomValidity('');
                }
            }, 350);
        });
        if (input.value) input.dispatchEvent(new Event('input'));
    }
})();