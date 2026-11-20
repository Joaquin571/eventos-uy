const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const path=require('node:path');
const codigo=fs.readFileSync(path.join(__dirname,'../../main/webapp/js/disponibilidad-usuarios.js'),'utf8');
function escenario(){
    const mensajes={},campos={},temporizadores=new Map(),peticiones=[];let secuencia=0;
    for(const nombre of ['nickname','correo']){
        mensajes[`disponibilidad-${nombre}`]={textContent:'',className:''};
        campos[nombre]={value:'',validity:{valid:true},mensaje:'',eventos:{},setCustomValidity(v){this.mensaje=v;},addEventListener(n,f){this.eventos[n]=f;}};
    }
    const form={dataset:{disponibilidadUrl:'/web/disponibilidad-usuario'}};
    vm.runInNewContext(codigo,{
        document:{getElementById:n=>n==='formAltaUsuario'?form:campos[n]||mensajes[n]},
        window:{location:{origin:'https://eventos.test'}},URL,AbortController,
        setTimeout(f,ms){assert.equal(ms,350);temporizadores.set(++secuencia,f);return secuencia;},
        clearTimeout(id){temporizadores.delete(id);},
        fetch(url,opciones){return new Promise((resolve,reject)=>peticiones.push({url,opciones,resolve,reject}));}
    });
    return {campos,mensajes,peticiones,temporizadores,
        escribir(nombre,valor){campos[nombre].value=valor;campos[nombre].eventos.input();},
        async consultar(){const pendientes=[...temporizadores.values()];temporizadores.clear();for(const f of pendientes)f();await Promise.resolve();},
        async responder(i,disponible){peticiones[i].resolve({ok:true,json:async()=>({disponible})});await new Promise(resolve=>setImmediate(resolve));}
    };
}
// Incluye los dos escenarios de Emmanuel (2911ab1), adaptados al contrato vigente.
test('espera y envía solamente campo y valor codificados',async()=>{
    const e=escenario();e.escribir('nickname','nombre & ñ');assert.equal(e.peticiones.length,0);await e.consultar();
    const url=e.peticiones[0].url;assert.equal(url.pathname,'/web/disponibilidad-usuario');assert.equal(url.searchParams.get('valor'),'nombre & ñ');
    assert.equal(e.peticiones[0].opciones.cache,'no-store');
    assert.deepEqual([...url.searchParams.keys()],['campo','valor']);await e.responder(0,true);assert.equal(e.campos.nickname.mensaje,'');
});
test('no disponible impide enviar y editar limpia el error anterior',async()=>{
    const e=escenario();e.escribir('nickname','ocupado');await e.consultar();await e.responder(0,false);
    assert.notEqual(e.campos.nickname.mensaje,'');e.escribir('nickname','nuevo');assert.equal(e.campos.nickname.mensaje,'');
});
test('una respuesta vieja no reemplaza la disponibilidad del valor actual',async()=>{
    const e=escenario();e.escribir('correo','viejo@test.uy');await e.consultar();
    e.escribir('correo','nuevo@test.uy');assert.equal(e.peticiones[0].opciones.signal.aborted,true);await e.consultar();
    await e.responder(1,true);await e.responder(0,false);assert.equal(e.campos.correo.mensaje,'');assert.equal(e.mensajes['disponibilidad-correo'].textContent,'Disponible.');
});
test('un fallo de red permite que Central valide al confirmar',async()=>{
    const e=escenario();e.escribir('nickname','libre');await e.consultar();e.peticiones[0].reject(new Error('Red'));await new Promise(resolve=>setImmediate(resolve));
    assert.equal(e.campos.nickname.mensaje,'');assert.match(e.mensajes['disponibilidad-nickname'].textContent,/comprobará al registrar/);
});
test('vacíos y formatos inválidos no originan consultas',async()=>{
    const e=escenario();e.escribir('nickname','  ');e.campos.correo.validity.valid=false;e.escribir('correo','invalido');await e.consultar();assert.equal(e.peticiones.length,0);
});
test('cambiar el valor sin evento también descarta una respuesta obsoleta',async()=>{
    const e=escenario();e.escribir('nickname','anterior');await e.consultar();e.campos.nickname.value='actual';
    await e.responder(0,false);assert.equal(e.campos.nickname.mensaje,'');
});
