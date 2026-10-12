window.App = window.App || {};

App.obtenerUsuarioActual = function () {
    const usuarioGuardado = sessionStorage.getItem("usuarioLogueado");

    if (!usuarioGuardado) {
        return null;
    }

    try {
        return JSON.parse(usuarioGuardado);
    } catch (error) {
        return null;
    }
};

App.obtenerRolActual = function () {
    const usuario = App.obtenerUsuarioActual();

    return usuario ? usuario.rol.toLowerCase() : "visitante";
};

App.rol = App.obtenerRolActual();


document.addEventListener("DOMContentLoaded", () => {

    const list = document.querySelector("#event-list");

    if (list) {
        const cat = new URLSearchParams(location.search).get("categoria");
        const data = cat ? App.datos.eventos.filter(e => e.categorias.includes(cat)) : App.datos.eventos;

        list.innerHTML = data.map(e => `<article class="card"><h2>${e.nombre}</h2><p>${e.descripcion}</p><p class="muted">${e.categorias.join(" · ")}</p><a class="btn btn-primary" href="pages/evento.html?id=${e.id}">Ver evento</a></article>`).join("");
    }

    document.querySelectorAll("form[data-demo]").forEach(f => f.addEventListener("submit", e => {
        e.preventDefault();
        if (!f.checkValidity()) {
            f.reportValidity();
            return;
        }

        const p = f.querySelector('[name=password]'), c = f.querySelector('[name=confirmacion]');
        if (p && c && p.value !== c.value) {
            c.setCustomValidity("Las contraseñas no coinciden");
            c.reportValidity();
            return;
        }

        const m = f.querySelector(".message");
        if (m) {
            m.textContent = "Operación simulada correctamente.";
            m.style.display = "block";
        }
    }));
});