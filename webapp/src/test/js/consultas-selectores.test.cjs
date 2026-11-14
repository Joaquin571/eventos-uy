const {test} = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const script = fs.readFileSync(path.join(__dirname, '../../main/webapp/js/consultas-selectores.js'), 'utf8');
test('cambia cada selección enviando únicamente su formulario GET', () => {
    const formularios = Array.from({length: 3}, () => ({ envios: 0, accion: null,
        querySelector() { return { addEventListener: (nombre, accion) => {
            assert.equal(nombre, 'change'); this.accion = accion;
        }}; }, requestSubmit() { this.envios++; }
    }));
    vm.runInNewContext(script, {document: { querySelectorAll() { return formularios; } }});
    formularios[1].accion();
    assert.deepEqual(formularios.map(f => f.envios), [0, 1, 0]);
});
test('una página sin selectores no provoca errores', () => {
    assert.doesNotThrow(() => vm.runInNewContext(script, {document: {querySelectorAll() {return [];}}}));
});
