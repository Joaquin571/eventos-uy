const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');

const codigo = fs.readFileSync(path.join(__dirname, '../../main/webapp/js/disponibilidad-usuario.js'), 'utf8');

function entorno() {
    const pendientes = [];
    const campos = {};
    const estados = {};
    for (const nombre of ['nickname', 'correo']) {
        campos[nombre] = {
            value: '', mensaje: '', listeners: {},
            addEventListener(tipo, fn) { this.listeners[tipo] = fn; },
            checkValidity() { return Boolean(this.value) && !this.mensaje; },
            setCustomValidity(mensaje) { this.mensaje = mensaje; }
        };
        estados[nombre] = {
            textContent: '', clases: new Set(),
            classList: {
                add() {}, remove() {}, toggle() {}
            }
        };
    }
    const formulario = { action: 'http://localhost:8080/eventos/alta-usuario', elements: campos };
    const llamadas = [];
    const contexto = {
        document: { getElementById(id) {
            if (id === 'formAltaUsuario') return formulario;
            return estados[id.replace('Disponibilidad', '')];
        } },
        setTimeout(fn) { pendientes.push(fn); return pendientes.length; },
        clearTimeout() {},
        AbortController,
        URLSearchParams,
        fetch: async (url) => {
            llamadas.push(url);
            return { ok: true, json: async () => ({ disponible: !url.includes('ocupado') }) };
        }
    };
    vm.runInNewContext(codigo, contexto);
    return { campos, estados, pendientes, llamadas };
}

test('consulta nickname mediante servlet, con ruta de contexto y parámetros codificados', async () => {
    const e = entorno();
    e.campos.nickname.value = 'nuevo usuario';
    e.campos.nickname.listeners.input();
    await e.pendientes.shift()();
    await new Promise(resolve => setImmediate(resolve));
    assert.equal(e.llamadas.length, 1);
    assert.match(e.llamadas[0], /\/eventos\/disponibilidad-usuario\?tipo=nickname&valor=nuevo\+usuario/);
    assert.equal(e.estados.nickname.textContent, 'Disponible');
});

test('identificador ocupado se informa y bloquea la validez del campo', async () => {
    const e = entorno();
    e.campos.nickname.value = 'ocupado';
    e.campos.nickname.listeners.input();
    await e.pendientes.shift()();
    await new Promise(resolve => setImmediate(resolve));
    assert.match(e.campos.nickname.mensaje, /ya está registrado/);
});
