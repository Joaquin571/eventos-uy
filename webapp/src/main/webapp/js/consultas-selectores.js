"use strict";
// Mejora opcional: los botones GET permiten realizar el mismo recorrido sin JavaScript.
document.querySelectorAll("[data-selector-consulta]").forEach(formulario => {
    formulario.querySelector("select").addEventListener("change", () => formulario.requestSubmit());
});
