"use strict";
// Los formularios GET solo contienen la selección; el código nunca va a la URL.
document.querySelectorAll("[data-seleccion-patrocinio]").forEach(form => {
    form.querySelector("select").addEventListener("change", () => form.requestSubmit());
});
const formulario = document.getElementById("formAltaPatrocinio");
if (formulario) {
    const aporte = formulario.elements.aporte;
    const gratuitos = formulario.elements.registrograt;
    const codigo = formulario.elements.codigo;
    const tipo = formulario.elements.tipoRegistro;
    function validar() {
        const monto = Number(aporte.value.trim());
        const cantidad = Number(gratuitos.value.trim());
        aporte.setCustomValidity(Number.isFinite(monto) && monto > 0 ? "" : "Ingresá un aporte mayor que cero.");
        gratuitos.setCustomValidity(/^\d+$/.test(gratuitos.value.trim()) && Number.isSafeInteger(cantidad)
            && cantidad <= 2147483647 ? "" : "Ingresá una cantidad entera no negativa.");
        codigo.setCustomValidity(codigo.value.trim() ? "" : "Ingresá el código de patrocinio.");
        const costo = Number(tipo.selectedOptions[0]?.dataset.costo);
        const total = costo * cantidad;
        const limite = monto * 0.20;
        if (aporte.validity.valid && gratuitos.validity.valid && Number.isFinite(costo)
                && total - limite > Math.max(1, Math.abs(total), Math.abs(limite)) * 1e-7) {
            gratuitos.setCustomValidity("El costo de los registros gratuitos supera el 20 % del aporte.");
        }
    }
    [aporte, gratuitos, codigo, tipo].forEach(campo => {
        campo.addEventListener("input", validar);
        campo.addEventListener("change", validar);
    });
    formulario.addEventListener("submit", evento => {
        validar();
        if (!formulario.reportValidity()) evento.preventDefault();
    });
}
