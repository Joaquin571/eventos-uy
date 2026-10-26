
document.addEventListener("DOMContentLoaded", function () {
    construirHeader();
});

function construirHeader() {
    const header = document.getElementById("headerPrincipal");
    if (!header) return;

    const usuario = JSON.parse(sessionStorage.getItem("usuarioLogueado"));
    const rol = usuario ? usuario.rol.toLowerCase() : "visitante";

    const enPages = window.location.pathname.includes("/pages/");
    const inicio = enPages ? "../index.html" : "index.html";
    const ruta = enPages ? "" : "pages/";

    let menuGestion = "";
    let menuConsultas = "";
    let menuUsuario = "";

    if (rol === "organizador") {
        menuGestion = `
            <li class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" href="#"
                   role="button" data-bs-toggle="dropdown">
                    Gestión
                </a>
                <ul class="dropdown-menu">
                    <li><a class="dropdown-item" href="${ruta}alta-evento.html">Alta Evento</a></li>
                    <li><a class="dropdown-item" href="${ruta}alta-edicion.html">Alta Edición</a></li>
                    <li><a class="dropdown-item" href="${ruta}alta-tipo-registro.html">Alta Tipo Registro</a></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item" href="${ruta}alta-institucion.html">Alta Institución</a></li>
                    <li><a class="dropdown-item" href="${ruta}alta-patrocinio.html">Alta Patrocinio</a></li>
                </ul>
            </li>
        `;

        menuConsultas = `
            <li class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" href="#"
                   role="button" data-bs-toggle="dropdown">
                    Consultas
                </a>
                <ul class="dropdown-menu">
                    <li><a class="dropdown-item" href="${ruta}consulta-registro.html">Consulta Registro</a></li>
                    <li><a class="dropdown-item" href="${ruta}consulta-patrocinio.html">Consulta Patrocinio</a></li>
                </ul>
            </li>
        `;
    }

    if (rol === "asistente") {
        menuGestion = `
            <li class="nav-item">
                <a class="nav-link" href="${ruta}registro-edicion.html">
                    Registrarme a edición
                </a>
            </li>
        `;

        menuConsultas = `
            <li class="nav-item">
                <a class="nav-link" href="${ruta}consulta-registro.html">
                    Mis registros
                </a>
            </li>
        `;
    }

    if (usuario) {
        const perfil = `${ruta}usuario.html?nick=${encodeURIComponent(usuario.nickname)}`;

        menuUsuario = `
            <div class="dropdown">
                <button class="btn btn-light dropdown-toggle"
                        type="button"
                        data-bs-toggle="dropdown">
                    ${usuario.nombre}
                </button>
                <ul class="dropdown-menu dropdown-menu-end">
                    <li><a class="dropdown-item" href="${perfil}">Mi perfil</a></li>
                    <li><a class="dropdown-item" href="${ruta}modificar-usuario.html">Modificar mis datos</a></li>
                    <li><hr class="dropdown-divider"></li>
                    <li>
                        <button class="dropdown-item text-danger"
                                type="button"
                                id="botonCerrarSesion">
                            Cerrar sesión
                        </button>
                    </li>
                </ul>
            </div>
        `;
    } else {
        menuUsuario = `
            <div class="d-flex gap-2">
                <a class="btn btn-outline-light" href="${ruta}login.html">Iniciar sesión</a>
                <a class="btn btn-light text-primary" href="${ruta}alta-usuario.html">Registrarse</a>
            </div>
        `;
    }

    header.innerHTML = `
        <nav class="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm">
            <div class="container">
                <a class="navbar-brand fw-bold" href="${inicio}">eventos.uy</a>

                <button class="navbar-toggler"
                        type="button"
                        data-bs-toggle="collapse"
                        data-bs-target="#navbarPrincipal"
                        aria-controls="navbarPrincipal"
                        aria-expanded="false"
                        aria-label="Abrir menú">
                    <span class="navbar-toggler-icon"></span>
                </button>

                <div class="collapse navbar-collapse" id="navbarPrincipal">
                    <ul class="navbar-nav me-auto">
                        <li class="nav-item">
                            <a class="nav-link" href="${inicio}">Eventos</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${ruta}usuarios.html">Usuarios</a>
                        </li>
                        ${menuGestion}
                        ${menuConsultas}
                    </ul>

                    ${menuUsuario}
                </div>
            </div>
        </nav>
    `;

    const botonCerrar = document.getElementById("botonCerrarSesion");

    if (botonCerrar) {
        botonCerrar.addEventListener("click", ejecutarCerrarSesion);
    }
}
