const { test } = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const codigo = fs.readFileSync(path.join(__dirname, '../../main/webapp/js/alta-patrocinio.js'), 'utf8');

function escenario() {
    const campo = value => ({ value, validity: { valid: true }, eventos: {},
        setCustomValidity(mensaje) { this.mensaje = mensaje; this.validity.valid = !mensaje; },
        addEventListener(nombre, accion) { this.eventos[nombre] = accion; }
    });
    const elementos = { aporte: campo('1000'), registrograt: campo('2'), codigo: campo('COD'), tipoRegistro: campo('General') };
    elementos.tipoRegistro.selectedOptions = [{ dataset: { costo: '100' } }];
    const form = { elements: elementos, eventos: {},
        addEventListener(nombre, accion) { this.eventos[nombre] = accion; },
        reportValidity() { return Object.values(elementos).every(c => c.validity.valid); }
    };
    const seleccion = { envios: 0, campo: campo('Evento'),
        querySelector() { return this.campo; }, requestSubmit() { this.envios++; }
    };
    vm.runInNewContext(codigo, { document: {
        querySelectorAll() { return [seleccion]; }, getElementById() { return form; }
    }});
    return { elementos, seleccion, enviar() {
        let impedido = false;
        form.eventos.submit({ preventDefault() { impedido = true; } });
        return !impedido;
    }};
}

test('acepta el límite exacto y cero gratuitos', () => {
    for (const cantidad of ['2', '0']) {
        const e = escenario(); e.elementos.registrograt.value = cantidad;
        assert.equal(e.enviar(), true);
    }
});
test('rechaza el exceso del 20 %', () => {
    const e = escenario(); e.elementos.registrograt.value = '3';
    assert.equal(e.enviar(), false); assert.match(e.elementos.registrograt.mensaje, /20/);
});
test('rechaza aportes inválidos y no finitos', () => {
    for (const valor of ['', 'NaN', 'Infinity', '-Infinity', '0', '-1', '1e500', 'texto']) {
        const e = escenario(); e.elementos.aporte.value = valor;
        assert.equal(e.enviar(), false, valor);
    }
});
test('rechaza cantidades negativas, decimales y desbordamientos', () => {
    for (const valor of ['', '-1', '1.5', '2147483648', 'texto']) {
        const e = escenario(); e.elementos.registrograt.value = valor;
        assert.equal(e.enviar(), false, valor);
    }
});
test('rechaza códigos compuestos solo por espacios', () => {
    const e = escenario(); e.elementos.codigo.value = '   ';
    assert.equal(e.enviar(), false);
});
test('corrige la validez al editar un campo previamente inválido', () => {
    const e = escenario(); e.elementos.aporte.value = '0'; assert.equal(e.enviar(), false);
    e.elementos.aporte.value = '1000'; e.elementos.aporte.eventos.input();
    assert.equal(e.enviar(), true);
});
test('el cambio de selección envía solo el formulario GET de selección', () => {
    const e = escenario(); e.seleccion.campo.eventos.change();
    assert.equal(e.seleccion.envios, 1);
});
