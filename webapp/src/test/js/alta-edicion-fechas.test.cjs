const { test } = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const codigo = fs.readFileSync(path.join(__dirname, '../../main/webapp/js/alta-edicion-fechas.js'), 'utf8');

function escenario(inicio = '', fin = '') {
    function campo(value) {
        return { value, mensaje: '', eventos: {},
            get valueAsNumber() { return this.value ? Date.parse(this.value + 'T00:00:00Z') : NaN; },
            setCustomValidity(mensaje) { this.mensaje = mensaje; },
            addEventListener(nombre, accion) { this.eventos[nombre] = accion; }
        };
    }
    const campos = { fechaInicio: campo(inicio), fechaFin: campo(fin) };
    const form = { elements: campos, eventos: {}, avisos: 0,
        addEventListener(nombre, accion) { this.eventos[nombre] = accion; },
        checkValidity() { return Object.values(campos).every(c => c.value && !c.mensaje); },
        reportValidity() { this.avisos++; }
    };
    vm.runInNewContext(codigo, { document: { getElementById(id) {
        assert.equal(id, 'formAltaEdicion'); return form;
    } } });
    return { campos, form, enviar() {
        let impedido = false;
        form.eventos.submit({ preventDefault() { impedido = true; } });
        return !impedido;
    } };
}

test('admite fechas iguales y un intervalo que cruza de año', () => {
    for (const [inicio, fin] of [['2026-11-02', '2026-11-02'], ['2026-12-31', '2027-01-01']]) {
        const e = escenario(inicio, fin);
        assert.equal(e.enviar(), true);
        assert.equal(e.campos.fechaFin.mensaje, '');
        assert.equal(e.form.avisos, 0);
    }
});

test('bloquea fin anterior, incluso si los valores se asignaron sin eventos', () => {
    const e = escenario();
    e.campos.fechaInicio.value = '2026-11-03'; e.campos.fechaFin.value = '2026-11-02';
    assert.equal(e.enviar(), false);
    assert.equal(e.campos.fechaFin.mensaje, 'La fecha de fin no puede ser anterior al inicio.');
    assert.equal(e.form.avisos, 1);
});

test('limpia el error al corregir fin o inicio mediante input o change', () => {
    for (const evento of ['input', 'change']) {
        for (const corregir of ['fechaInicio', 'fechaFin']) {
            const e = escenario('2026-11-03', '2026-11-02');
            assert.notEqual(e.campos.fechaFin.mensaje, '');
            e.campos[corregir].value = corregir === 'fechaInicio' ? '2026-11-01' : '2026-11-04';
            e.campos[corregir].eventos[evento]();
            assert.equal(e.campos.fechaFin.mensaje, '');
            assert.equal(e.enviar(), true);
        }
    }
});

test('al vaciar fechas elimina el error de orden y respeta los required del formulario', () => {
    const e = escenario('2026-11-03', '2026-11-02');
    e.campos.fechaFin.value = ''; e.campos.fechaFin.eventos.input();
    assert.equal(e.campos.fechaFin.mensaje, '');
    assert.equal(e.enviar(), false);
    e.campos.fechaInicio.value = ''; e.campos.fechaFin.value = '2026-11-02';
    e.campos.fechaInicio.eventos.input();
    assert.equal(e.enviar(), false);
});

test('reset no deja el mensaje personalizado de una selección anterior', () => {
    const e = escenario('2026-11-03', '2026-11-02');
    e.form.eventos.reset();
    assert.equal(e.campos.fechaFin.mensaje, '');
});

test('el script no falla si se carga en una página sin ese formulario', () => {
    assert.doesNotThrow(() => vm.runInNewContext(codigo, { document: { getElementById() { return null; } } }));
});
