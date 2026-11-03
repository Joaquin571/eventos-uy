const { test } = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const codigo = fs.readFileSync(path.join(__dirname, '../../main/webapp/js/validacion-formularios.js'), 'utf8');

function escenario(id, definiciones) {
    const nodos = [], grupos = {}, campos = [];
    if (id === 'formAltaEvento') grupos.categorias = { textContent: '', appendChild(n) { this.ayuda = n; nodos.push(n); } };
    function nodo() {
        const atributos = {};
        return { textContent: '', id: '', className: '', atributos,
            setAttribute(n, v) { atributos[n] = v; } };
    }
    for (const d of definiciones) {
        const atributos = {}, clases = new Set();
        const campo = { type: 'text', value: '', name: '', required: false, disabled: false,
            readOnly: false, checked: false, files: [], selectedOptions: [{ disabled: false }], errores: {}, validationMessage: '', foco: false, ...d,
            get validity() { return { ...this.errores, valid: !Object.values(this.errores).some(Boolean) }; },
            setCustomValidity(mensaje) { this.validationMessage = mensaje; this.errores.customError = Boolean(mensaje); },
            getAttribute(n) { return atributos[n] || null; },
            setAttribute(n, v) { atributos[n] = v; }, removeAttribute(n) { delete atributos[n]; },
            insertAdjacentElement(pos, n) { this.ayuda = n; nodos.push(n); },
            closest() { return this.etiqueta || null; },
            focus() { this.foco = true; }, atributos, clases,
            classList: { toggle(n, si) { if (si) clases.add(n); else clases.delete(n); }, remove(n) { clases.delete(n); } }
        };
        campos.push(campo);
        if (!campos[d.name]) campos[d.name] = campo;
        if (['radio', 'checkbox'].includes(campo.type)) grupos[d.name] ||= { appendChild(n) { this.ayuda = n; nodos.push(n); } };
    }
    const form = { elements: campos, eventos: {}, noValidate: false,
        addEventListener(n, f) { (this.eventos[n] ||= []).push(f); },
        querySelector(selector) { return grupos[selector.match(/"([^"]+)"/)[1]] || null; } };
    vm.runInNewContext(codigo, { document: {
        getElementById(nombre) { return nombre === id ? form : null; }, createElement() { return nodo(); }
    } });
    function disparar(nombre, target) { for (const f of form.eventos[nombre] || []) f({ target }); }
    return { form, campos, nodos, grupos, disparar, enviar() {
        let impedido = false;
        for (const f of form.eventos.submit) f({ preventDefault() { impedido = true; } });
        return !impedido;
    } };
}
const campo = (name, value, extra = {}) => ({ name, value, required: true, ...extra });
const login = (identificador = 'nick', clave = 'clave') => escenario('formLogin', [
    campo('identificador', identificador), campo('contrasenia', clave, { type: 'password' })]);

test('login acepta nickname o correo sin imponer formato de correo al nickname', () => {
    for (const id of ['nick', 'usuario@correo.uy', 'nick@sin.politica', 'nombre con espacios']) assert.equal(login(id).enviar(), true);
});
test('login exige identificador y contraseña, sin recortar contraseñas con espacios', () => {
    for (const id of ['', '  ']) assert.equal(login(id).enviar(), false);
    assert.equal(login('nick', '').enviar(), false);
    assert.equal(login('nick', '  ').enviar(), true);
});
test('mensajes junto al campo, foco y accesibilidad sin cambiar los valores', () => {
    const e = login('  ', '');
    e.campos.identificador.setAttribute('aria-describedby', 'instrucciones');
    assert.equal(e.enviar(), false);
    assert.equal(e.campos.identificador.value, '  ');
    assert.equal(e.campos.identificador.foco, true);
    assert.equal(e.campos.contrasenia.foco, false);
    assert.equal(e.campos.identificador.atributos['aria-invalid'], 'true');
    assert.match(e.campos.identificador.atributos['aria-describedby'], /^instrucciones /);
    assert.match(e.campos.identificador.ayuda.textContent, /obligatorio/);
    assert.equal(e.campos.identificador.ayuda.atributos['aria-live'], 'polite');
});
test('en campos envueltos por label el mensaje queda fuera de la etiqueta', () => {
    const etiqueta = { insertAdjacentElement(pos, nodo) { this.nodo = nodo; } };
    const e = escenario('formAltaEdicion', [campo('nombre', '', { etiqueta })]);
    assert.equal(e.enviar(), false);
    assert.match(etiqueta.nodo.textContent, /obligatorio/);
    assert.equal(e.campos.nombre.atributos['aria-describedby'], etiqueta.nodo.id);
});
test('input y change limpian errores después de corregir sin cambiar el contenido', () => {
    for (const evento of ['input', 'change']) {
        const e = login(''); assert.equal(e.enviar(), false);
        e.campos.identificador.value = 'nuevo'; e.disparar(evento, e.campos.identificador);
        assert.equal(e.campos.identificador.ayuda.textContent, '');
        assert.equal(e.campos.identificador.clases.has('is-invalid'), false);
        assert.equal(e.campos.identificador.atributos['aria-invalid'], undefined);
        assert.equal(e.enviar(), true);
    }
});
test('alta usuario valida correo con el formato existente de Sistema', () => {
    for (const correo of ['sin-arroba', 'a@b', 'a b@c.uy', 'a@@b.uy']) {
        const e = escenario('formAltaUsuario', [campo('correo', correo, { type: 'email' })]);
        assert.equal(e.enviar(), false); assert.match(e.campos.correo.ayuda.textContent, /correo/);
    }
    assert.equal(escenario('formAltaUsuario', [campo('correo', 'a@b.uy', { type: 'email' })]).enviar(), true);
});
test('alta usuario exige solamente los campos activos del subtipo', () => {
    const e = escenario('formAltaUsuario', [campo('nombre', 'Nombre'), campo('apellido', ''),
        campo('fechaNac', '', { type: 'date' }), campo('descripcion', '', { disabled: true })]);
    assert.equal(e.enviar(), false);
    e.campos.apellido.value = 'Apellido'; e.campos.fechaNac.value = '2000-01-01';
    assert.equal(e.enviar(), true);
    e.campos.apellido.disabled = true; e.campos.fechaNac.disabled = true;
    e.campos.descripcion.disabled = false; assert.equal(e.enviar(), false);
    e.campos.descripcion.value = 'Organizador'; assert.equal(e.enviar(), true);
});
test('alta y modificación comprueban confirmación sin imponer longitud mínima nueva', () => {
    for (const id of ['formAltaUsuario', 'formModificarUsuario']) {
        const e = escenario(id, [campo('contrasenia', 'x', { type: 'password', required: id === 'formAltaUsuario' }),
            campo('confirmacion', 'y', { type: 'password', required: id === 'formAltaUsuario' })]);
        assert.equal(e.enviar(), false); assert.match(e.campos.confirmacion.ayuda.textContent, /no coinciden/);
        e.campos.confirmacion.value = 'x'; assert.equal(e.enviar(), true);
    }
});
test('modificación permite dejar ambas contraseñas vacías y bloquea confirmación aislada como antes', () => {
    const e = escenario('formModificarUsuario', [campo('contrasenia', '', { type: 'password', required: false }),
        campo('confirmacion', '', { type: 'password', required: false })]);
    assert.equal(e.enviar(), true);
    e.campos.confirmacion.value = 'otra'; assert.equal(e.enviar(), false);
});
test('modificación no valida nickname/correo bloqueados; sitio web e institución son opcionales', () => {
    const e = escenario('formModificarUsuario', [campo('nickname', '', { disabled: true }), campo('correo', '', { readOnly: true }),
        campo('sitioWeb', '', { type: 'url', required: false }), campo('institucion', '', { type: 'select-one', required: false })]);
    assert.equal(e.enviar(), true);
});
test('fechas válidas pasan y fechas futuras o de formato inválido se informan junto al campo', () => {
    for (const id of ['formAltaUsuario', 'formModificarUsuario']) {
        const e = escenario(id, [campo('fechaNac', '2000-01-01', { type: 'date' })]);
        assert.equal(e.enviar(), true);
        e.campos.fechaNac.errores.rangeOverflow = true;
        assert.equal(e.enviar(), false); assert.match(e.campos.fechaNac.ayuda.textContent, /futura/);
        e.campos.fechaNac.errores = { badInput: true };
        assert.equal(e.enviar(), false); assert.match(e.campos.fechaNac.ayuda.textContent, /válido/);
    }
});
test('edición exige evento, sigla, ciudad, país y fechas sin aceptar espacios', () => {
    for (const nombre of ['evento', 'nombre', 'sigla', 'ciudad', 'pais', 'fechaInicio', 'fechaFin']) {
        const e = escenario('formAltaEdicion', [campo(nombre, '  ')]);
        assert.equal(e.enviar(), false);
    }
});
test('edición muestra el error de orden del validador existente sin borrarlo', () => {
    const e = escenario('formAltaEdicion', [campo('fechaFin', '2026-01-01', { type: 'date' })]);
    e.campos.fechaFin.setCustomValidity('La fecha de fin no puede ser anterior al inicio.');
    assert.equal(e.enviar(), false);
    assert.match(e.campos.fechaFin.ayuda.textContent, /anterior/);
    assert.equal(e.campos.fechaFin.validity.customError, true);
    e.campos.fechaFin.setCustomValidity(''); assert.equal(e.enviar(), true);
});
test('evento exige una categoría por grupo y limpia el mensaje al seleccionar', () => {
    const e = escenario('formAltaEvento', [campo('nombre', 'Evento'), campo('categorias', 'Cat1', { type: 'checkbox', required: false }),
        campo('categorias', 'Cat2', { type: 'checkbox', required: false })]);
    assert.equal(e.enviar(), false); assert.match(e.grupos.categorias.ayuda.textContent, /categoría/);
    e.campos[2].checked = true; e.disparar('change', e.campos[2]);
    assert.equal(e.enviar(), true); assert.equal(e.grupos.categorias.ayuda.textContent, '');
});
test('patrocinio exige selección de institución/nivel/tipo y conserva sus validaciones económicas', () => {
    for (const nombre of ['institucion', 'nivel', 'tipoRegistro', 'aporte', 'registrograt', 'codigo'])
        assert.equal(escenario('formAltaPatrocinio', [campo(nombre, '  ')]).enviar(), false);
    const e = escenario('formAltaPatrocinio', [campo('registrograt', '3')]);
    e.campos.registrograt.setCustomValidity('El costo supera el 20 % del aporte.');
    assert.equal(e.enviar(), false); assert.match(e.campos.registrograt.ayuda.textContent, /20/);
});
test('una opción no disponible conservada tras error se muestra pero no permite confirmar', () => {
    const e = escenario('formAltaPatrocinio', [campo('nivel', 'INVALIDO', { type: 'select-one' })]);
    e.campos.nivel.selectedOptions = [{ disabled: true }];
    assert.equal(e.enviar(), false); assert.match(e.campos.nivel.ayuda.textContent, /disponible/);
    assert.equal(e.campos.nivel.value, 'INVALIDO');
    e.campos.nivel.selectedOptions = [{ disabled: false }];e.campos.nivel.value = 'ORO';
    assert.equal(e.enviar(), true);
});
test('registro exige un tipo y código solo cuando la modalidad lo exige', () => {
    const e = escenario('formRegistro', [campo('tipo', 'General', { type: 'radio' }),
        campo('tipo', 'VIP', { type: 'radio' }), campo('codigo', '', { required: false })]);
    assert.equal(e.enviar(), false); assert.match(e.grupos.tipo.ayuda.textContent, /opción/);
    e.campos[0].checked = true; assert.equal(e.enviar(), true);
    e.campos.codigo.required = true; assert.equal(e.enviar(), false);
    e.campos.codigo.value = '  '; assert.equal(e.enviar(), false);
    e.campos.codigo.value = 'COD'; assert.equal(e.enviar(), true);
    e.campos.codigo.required = false; e.campos.codigo.value = ''; assert.equal(e.enviar(), true);
});
test('imagen opcional con tamaño exacto y rechazo de exceso en cada alta/modificación', () => {
    for (const id of ['formAltaUsuario', 'formModificarUsuario', 'formAltaEvento', 'formAltaEdicion']) {
        const lista = [campo('imagen', '', { type: 'file', required: false })];
        if (id === 'formAltaEvento') lista.push(campo('categorias', 'Cat', { type: 'checkbox', checked: true, required: false }));
        const e = escenario(id, lista);
        assert.equal(e.enviar(), true);
        e.campos.imagen.files = [{ size: 2 * 1024 * 1024 }]; assert.equal(e.enviar(), true);
        e.campos.imagen.files = [{ size: 2 * 1024 * 1024 + 1 }]; assert.equal(e.enviar(), false);
        assert.match(e.campos.imagen.ayuda.textContent, /2 MB/);
    }
});
test('evento sin categorías disponibles impide el alta con un mensaje junto al grupo', () => {
    const e = escenario('formAltaEvento', [campo('nombre', 'Evento')]);
    assert.equal(e.enviar(), false);
    assert.match(e.grupos.categorias.textContent, /No hay categorías/);
});
test('URL opcional mal formada y otras restricciones nativas no se ignoran', () => {
    for (const flag of ['typeMismatch', 'stepMismatch', 'rangeUnderflow']) {
        const e = escenario('formModificarUsuario', [campo('sitioWeb', 'valor', { type: 'url', required: false })]);
        e.campos.sitioWeb.errores[flag] = true; assert.equal(e.enviar(), false);
    }
});
test('reset limpia errores, incluidos los personalizados, sin reescribir datos', () => {
    const e = login(''); assert.equal(e.enviar(), false);
    e.campos.identificador.setCustomValidity('error anterior'); e.disparar('reset');
    assert.equal(e.campos.identificador.ayuda.textContent, '');
    assert.equal(e.campos.identificador.validationMessage, '');
    assert.equal(e.campos.identificador.value, '');
});
test('no marca errores al cargar, ignora token CSRF y no genera nodos repetidos', () => {
    const e = escenario('formLogin', [campo('identificador', ''), campo('_csrf', 'TOKEN', { type: 'hidden' })]);
    assert.equal(e.campos.identificador.ayuda.textContent, '');
    assert.equal(e.campos._csrf.ayuda, undefined);
    assert.equal(e.form.noValidate, true); assert.equal(e.nodos.length, 1);
    e.enviar(); e.enviar(); assert.equal(e.nodos.length, 1);
});
test('no afecta páginas de consulta ni logout sin campos de negocio', () => {
    assert.doesNotThrow(() => vm.runInNewContext(codigo, { document: { getElementById() { return null; } } }));
});

function usuarioInline(archivo) {
    const jsp = fs.readFileSync(path.join(__dirname, '../../main/webapp/WEB-INF/views', archivo), 'utf8');
    const script = jsp.match(/<script>\s*([\s\S]*?)<\/script>/)[1];
    const elementos = {};
    for (const id of ['tipo', 'apellido', 'fechaNac', 'institucion', 'descripcion', 'sitioWeb', 'contrasenia', 'confirmacion']) {
        elementos[id] = { value: '', eventos: {}, disabled: false, required: false, mensaje: '',
            addEventListener(n, f) { this.eventos[n] = f; },
            setCustomValidity(m) { this.mensaje = m; }, reportValidity() {} };
    }
    elementos.tipo.value = 'asistente';
    for (const [id, nombres] of [['camposAsistente', ['apellido', 'fechaNac', 'institucion']],
        ['camposOrganizador', ['descripcion', 'sitioWeb']]]) {
        elementos[id] = { oculto: false, classList: { toggle(n, valor) { elementos[id].oculto = valor; } },
            querySelectorAll() { return nombres.map(n => elementos[n]); } };
    }
    const form = { eventos: {}, addEventListener(n, f) { this.eventos[n] = f; } };
    elementos.formAltaUsuario = form; elementos.formModificarUsuario = form;
    class FechaLocal {
        getFullYear() { return 2026; } getMonth() { return 10; } getDate() { return 2; }
        toISOString() { return '2026-11-03T01:00:00Z'; }
    }
    vm.runInNewContext(script, { Date: FechaLocal, document: { getElementById(id) { return elementos[id]; } } });
    return { elementos, form, enviar() {
        let impedido = false; form.eventos.submit({ preventDefault() { impedido = true; } }); return !impedido;
    } };
}
test('script real de alta alterna subtipos sin perder valores ni validar campos ocultos', () => {
    const { elementos: e } = usuarioInline('alta-usuario.jsp');
    e.apellido.value = 'Anterior';
    assert.equal(e.apellido.required, true); assert.equal(e.descripcion.disabled, true);
    e.tipo.value = 'organizador'; e.tipo.eventos.change();
    assert.equal(e.apellido.disabled, true); assert.equal(e.institucion.disabled, true);
    assert.equal(e.descripcion.required, true); assert.equal(e.descripcion.disabled, false);
    e.tipo.value = 'asistente'; e.tipo.eventos.change();
    assert.equal(e.apellido.value, 'Anterior'); assert.equal(e.apellido.disabled, false);
});
test('scripts reales de usuario usan fecha local para el límite, no el día UTC', () => {
    for (const archivo of ['alta-usuario.jsp', 'modificar-usuario.jsp'])
        assert.equal(usuarioInline(archivo).elementos.fechaNac.max, '2026-11-02');
});
test('scripts reales limpian confirmación obsoleta al corregir cualquiera de las contraseñas', () => {
    for (const archivo of ['alta-usuario.jsp', 'modificar-usuario.jsp']) for (const corregir of ['contrasenia', 'confirmacion']) {
        const e = usuarioInline(archivo);
        e.elementos.contrasenia.value = 'a'; e.elementos.confirmacion.value = 'b';
        assert.equal(e.enviar(), false);
        e.elementos[corregir].value = corregir === 'contrasenia' ? 'b' : 'a';
        e.elementos[corregir].eventos.input();
        assert.equal(e.elementos.confirmacion.mensaje, ''); assert.equal(e.enviar(), true);
    }
});
test('scripts reales vuelven a comprobar confirmación al enviar valores corregidos sin evento input', () => {
    for (const archivo of ['alta-usuario.jsp', 'modificar-usuario.jsp']) {
        const e = usuarioInline(archivo);
        e.elementos.contrasenia.value = 'a'; e.elementos.confirmacion.value = 'b';
        assert.equal(e.enviar(), false);
        e.elementos.confirmacion.value = 'a';
        assert.equal(e.enviar(), true);
        assert.equal(e.elementos.confirmacion.mensaje, '');
    }
});
