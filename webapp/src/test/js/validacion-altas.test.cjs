const { test } = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const codigo = fs.readFileSync(path.join(__dirname, '../../main/webapp/js/validacion-altas.js'), 'utf8');

function escenario(tipo, valores = {}) {
    const iniciales = tipo === 'Institucion'
        ? { nombre: 'UTEC', descripcion: 'Universidad', sitioWeb: 'https://utec.edu.uy', imagen: '' }
        : { edicion: 'Edición propia', nombre: 'General', descripcion: 'Entrada', costo: '0', cupo: '1' };
    const ayudas = {}, campos = {};
    for (const [nombre, valor] of Object.entries({ ...iniciales, ...valores })) {
        const clases = new Set(), atributos = {};
        const id = tipo === 'Institucion' ? nombre : 'tipo' + nombre[0].toUpperCase() + nombre.slice(1);
        campos[nombre] = {
            id, value: valor, files: [], eventos: {}, mensaje: '', foco: false,
            erroresNativos: {},
            get validity() {
                return { ...this.erroresNativos, valid: !this.mensaje && !Object.values(this.erroresNativos).some(Boolean) };
            },
            setCustomValidity(mensaje) { this.mensaje = mensaje; },
            addEventListener(evento, accion) { this.eventos[evento] = accion; },
            setAttribute(n, v) { atributos[n] = v; },
            removeAttribute(n) { delete atributos[n]; },
            focus() { this.foco = true; },
            atributos, clases,
            classList: {
                toggle(n, activo) { if (activo) clases.add(n); else clases.delete(n); },
                remove(n) { clases.delete(n); }
            }
        };
        ayudas[id + 'Error'] = { textContent: '' };
    }
    const form = { elements: campos, eventos: {}, noValidate: false,
        addEventListener(n, accion) { this.eventos[n] = accion; } };
    vm.runInNewContext(codigo, { document: { getElementById(id) {
        if (id === 'formAlta' + tipo) return form;
        return ayudas[id] || null;
    } } });
    return { form, campos, ayudas, enviar() {
        let impedido = false;
        form.eventos.submit({ preventDefault() { impedido = true; } });
        return !impedido;
    } };
}

test('institución válida permite envío sin alterar valores', () => {
    for (const sitioWeb of ['https://utec.edu.uy', 'HTTP://ejemplo.com/ruta?q=1', ' https://ejemplo.com ']) {
        const e = escenario('Institucion', { sitioWeb });
        assert.equal(e.enviar(), true);
        assert.equal(e.campos.sitioWeb.value, sitioWeb);
        assert.equal(e.form.noValidate, true);
    }
});
test('institución rechaza campos obligatorios vacíos o solo blancos con mensaje junto al campo', () => {
    for (const campo of ['nombre', 'descripcion', 'sitioWeb']) for (const valor of ['', '  \t ']) {
        const e = escenario('Institucion', { [campo]: valor });
        assert.equal(e.enviar(), false);
        const c = e.campos[campo];
        assert.match(c.mensaje, /obligatori/);
        assert.equal(e.ayudas[c.id + 'Error'].textContent, c.mensaje);
        assert.equal(c.atributos['aria-invalid'], 'true');
        assert.equal(c.clases.has('is-invalid'), true);
        assert.equal(c.foco, true);
    }
});
test('institución rechaza URL sin esquema HTTP/HTTPS, host sin punto o espacios internos', () => {
    for (const sitioWeb of ['ejemplo.com', 'ftp://ejemplo.com', 'javascript:alert(1)', 'https://localhost', 'https://ejemplo .com']) {
        const e = escenario('Institucion', { sitioWeb });
        assert.equal(e.enviar(), false);
        assert.match(e.campos.sitioWeb.mensaje, /URL HTTP o HTTPS/);
    }
});
test('imagen opcional admite ausencia y límite exacto; rechaza más de 2 MiB', () => {
    const e = escenario('Institucion');
    assert.equal(e.enviar(), true);
    e.campos.imagen.files = [{ size: 2 * 1024 * 1024 }];
    assert.equal(e.enviar(), true);
    e.campos.imagen.files = [{ size: 2 * 1024 * 1024 + 1 }];
    assert.equal(e.enviar(), false);
    assert.match(e.campos.imagen.mensaje, /2 MB/);
});
test('tipo válido admite costo cero, fracciones y cupo int máximo', () => {
    for (const costo of ['0', '-0', '0.01', '1234.50', '1e2']) {
        const e = escenario('TipoRegistro', { costo, cupo: '2147483647' });
        assert.equal(e.enviar(), true);
    }
});
test('tipo exige edición, nombre y descripción sin aceptar solo blancos', () => {
    for (const campo of ['edicion', 'nombre', 'descripcion']) {
        const e = escenario('TipoRegistro', { [campo]: '   ' });
        assert.equal(e.enviar(), false);
        assert.notEqual(e.campos[campo].mensaje, '');
    }
});
test('tipo rechaza costos negativos, no finitos, fuera de float y sintaxis inválida', () => {
    for (const costo of ['', '  ', '-0.01', 'NaN', 'Infinity', '1e309', '3.5e38', '0x10', '10abc', '1,50']) {
        const e = escenario('TipoRegistro', { costo });
        assert.equal(e.enviar(), false, costo);
        assert.notEqual(e.campos.costo.mensaje, '');
    }
});
test('tipo rechaza cupo cero, negativo, decimal, exponencial y fuera del int', () => {
    for (const cupo of ['', '  ', '0', '-1', '1.5', '1.0', '1e2', '2147483648', 'Infinity', '0x10']) {
        const e = escenario('TipoRegistro', { cupo });
        assert.equal(e.enviar(), false, cupo);
        assert.notEqual(e.campos.cupo.mensaje, '');
    }
});
test('errores nativos badInput y stepMismatch se muestran aunque el script maneje submit', () => {
    const e = escenario('TipoRegistro', { costo: '0.001' });
    e.campos.costo.erroresNativos.stepMismatch = true;
    assert.equal(e.enviar(), false);
    assert.match(e.campos.costo.mensaje, /0,01/);
    e.campos.costo.erroresNativos = { badInput: true };
    assert.equal(e.enviar(), false);
    assert.match(e.campos.costo.mensaje, /número válido/);
    e.campos.costo.erroresNativos = {};
    e.campos.cupo.erroresNativos.badInput = true;
    assert.equal(e.enviar(), false);
    assert.match(e.campos.cupo.mensaje, /entero válido/);
});
test('otras restricciones HTML5 no son ignoradas', () => {
    const e = escenario('Institucion');
    e.campos.sitioWeb.erroresNativos.typeMismatch = true;
    assert.equal(e.enviar(), false);
    assert.match(e.campos.sitioWeb.mensaje, /formato/);
});
test('input y change limpian mensaje, estilo y accesibilidad al corregir', () => {
    for (const evento of ['input', 'change']) {
        const e = escenario('Institucion', { nombre: '' });
        assert.equal(e.enviar(), false);
        const c = e.campos.nombre;
        c.value = 'Corregido'; c.eventos[evento]();
        assert.equal(c.mensaje, '');
        assert.equal(e.ayudas.nombreError.textContent, '');
        assert.equal(c.clases.has('is-invalid'), false);
        assert.equal(c.atributos['aria-invalid'], undefined);
        assert.equal(e.enviar(), true);
    }
});
test('submit vuelve a validar valores cambiados sin emitir eventos y muestra todos los errores', () => {
    const e = escenario('TipoRegistro');
    e.campos.nombre.value = ''; e.campos.costo.value = '-1';
    assert.equal(e.enviar(), false);
    assert.notEqual(e.campos.nombre.mensaje, '');
    assert.notEqual(e.campos.costo.mensaje, '');
    assert.equal(e.campos.nombre.foco, true);
    assert.equal(e.campos.costo.foco, false);
});
test('reset elimina mensajes y estilos sin cambiar manualmente los valores', () => {
    const e = escenario('TipoRegistro', { nombre: '' });
    assert.equal(e.enviar(), false);
    e.form.eventos.reset();
    assert.equal(e.campos.nombre.mensaje, '');
    assert.equal(e.campos.nombre.value, '');
    assert.equal(e.ayudas.tipoNombreError.textContent, '');
    assert.equal(e.campos.nombre.clases.has('is-invalid'), false);
});
test('el script puede cargarse en páginas sin ninguno de los formularios', () => {
    assert.doesNotThrow(() => vm.runInNewContext(codigo, { document: { getElementById() { return null; } } }));
});
