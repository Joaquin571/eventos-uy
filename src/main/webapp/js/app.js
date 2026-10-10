window.App = window.App || {};
App.rol = "visitante"; // visitante | asistente | organizador

App.renderHeader = function () {
    const h = document.querySelector("#site-header");
    if (!h) return;
    const r = document.body.dataset.root || ".";

    let acciones = App.rol === "visitante"
        ? `<a class="btn" href="${r}/pages/login.html">Iniciar sesión</a><a class="btn" href="${r}/pages/alta-usuario.html">Registrarse</a>`
        : `<a class="btn" href="${r}/pages/perfil.html">Mi perfil</a><a class="btn" href="${r}/index.html">Salir</a>`;

    let rol = "";
    if (App.rol === "organizador") {
        rol = `<nav class="container categories"><a href="${r}/pages/alta-evento.html">Alta evento</a><a href="${r}/pages/alta-edicion.html">Alta edición</a><a href="${r}/pages/alta-institucion.html">Alta institución</a><a href="${r}/pages/alta-tipo-registro.html">Alta tipo registro</a><a href="${r}/pages/alta-patrocinio.html">Alta patrocinio</a></nav>`;
    }

    if (App.rol === "asistente") {
        rol = `<nav class="container categories"><a href="${r}/pages/perfil.html">Mi perfil</a><a href="${r}/pages/mis-registros.html">Mis registros</a></nav>`;
    }

    h.innerHTML = `<header class="site-header"><div class="container topbar"><a class="brand" href="${r}/index.html">eventos.uy</a><nav class="nav">${acciones}</nav></div></header>${rol}<nav class="container categories">${App.datos.categorias.map(c => `<a href="${r}/index.html?categoria=${encodeURIComponent(c)}">${c}</a>`).join("")}<a href="${r}/pages/usuarios.html">Usuarios</a></nav>`;
};

document.addEventListener("DOMContentLoaded", () => {
    App.renderHeader();
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