document.addEventListener("DOMContentLoaded", () => {



    const formLogin = document.getElementById("formLogin");
    if (formLogin) {
        formLogin.addEventListener("submit", iniciarSesion);
    }
});


function actualizarHeaderGlobal() {
    const usuarioLogueado = JSON.parse(
        sessionStorage.getItem("usuarioLogueado")
    );

    const contenedor = document.getElementById("accionesUsuario") ||
                       document.getElementById("contenedorBotonSesion");

    if (!contenedor) return;

    const enSubcarpeta = window.location.pathname.includes("/pages/");
    const ruta = enSubcarpeta ? "" : "pages/";

    if (!usuarioLogueado) {
        contenedor.innerHTML = `
            <a class="btn btn-outline-light" href="${ruta}login.html">
                Iniciar sesión
            </a>
            <a class="btn btn-light text-primary fw-bold" href="${ruta}alta-usuario.html">
                Registrarse
            </a>
        `;
        return;
    }

    const rutaPerfil = `${ruta}usuario.html?nick=${encodeURIComponent(usuarioLogueado.nickname)}`;
    const rol = usuarioLogueado.rol.toLowerCase();

    let opcionesRol = "";

    if (rol === "organizador") {
        opcionesRol = `
            <li><h6 class="dropdown-header">Gestión de eventos</h6></li>
            <li><a class="dropdown-item" href="${ruta}alta-evento.html">Alta Evento</a></li>
            <li><a class="dropdown-item" href="${ruta}alta-edicion.html">Alta Edición</a></li>
            <li><a class="dropdown-item" href="${ruta}alta-tipo-registro.html">Alta Tipo Registro</a></li>
            <li><a class="dropdown-item" href="${ruta}alta-institucion.html">Alta Institución</a></li>
            <li><a class="dropdown-item" href="${ruta}alta-patrocinio.html">Alta Patrocinio</a></li>
            <li><a class="dropdown-item" href="${ruta}consulta-registro.html">Consulta Registro</a></li>
        `;
    }

    if (rol === "asistente") {
        opcionesRol = `
            <li><h6 class="dropdown-header">Mis actividades</h6></li>
            <li><a class="dropdown-item" href="${ruta}registro-edicion.html">Registrarme a una edición</a></li>
            <li><a class="dropdown-item" href="${ruta}consulta-registro.html">Consultar mis registros</a></li>
        `;
    }

    contenedor.innerHTML = `
        <div class="dropdown">
            <button class="btn btn-light dropdown-toggle fw-bold text-primary"
                    type="button"
                    data-bs-toggle="dropdown"
                    aria-expanded="false">
                ${usuarioLogueado.nombre}
            </button>

            <ul class="dropdown-menu dropdown-menu-end shadow">
                <li>
                    <a class="dropdown-item" href="${rutaPerfil}">
                        Mi perfil
                    </a>
                </li>
                <li>
                    <a class="dropdown-item" href="${ruta}modificar-usuario.html">
                        Modificar mis datos
                    </a>
                </li>

                <li><hr class="dropdown-divider"></li>

                ${opcionesRol}

                <li><hr class="dropdown-divider"></li>

                <li>
                    <button class="dropdown-item text-danger"
                            type="button"
                            onclick="ejecutarCerrarSesion()">
                        Cerrar sesión
                    </button>
                </li>
            </ul>
        </div>
    `;
}

function limpiarErroresLogin() {
    const inputs = document.querySelectorAll("#formLogin .form-control");
    inputs.forEach(input => input.classList.remove("is-invalid"));

    const mensajes = document.querySelectorAll(".invalid-feedback-custom");
    mensajes.forEach(msg => msg.remove());
}

function mostrarErrorLogin(inputElement, textoMensaje) {
    limpiarErroresLogin();
    if (!inputElement) return;
    inputElement.classList.add("is-invalid");

    const errorDiv = document.createElement("div");
    errorDiv.className = "invalid-feedback-custom";
    errorDiv.textContent = textoMensaje;

    inputElement.parentNode.appendChild(errorDiv);
    inputElement.focus();
}


function iniciarSesion(e) {
    e.preventDefault();
    limpiarErroresLogin();

    const inputUser = document.getElementById("loginUser");
    const inputPass = document.getElementById("loginPass");

    const loginUser = inputUser.value.trim().toLowerCase();
    const loginPass = inputPass.value;

    if (!loginUser) {
        mostrarErrorLogin(inputUser, "Ingrese su nickname o correo.");
        return;
    }

    if (!loginPass) {
        mostrarErrorLogin(inputPass, "Ingrese su contraseña.");
        return;
    }

    // Usuarios registrados desde la web
    const registrados = JSON.parse(localStorage.getItem("usuariosRegistrados")) || [];

    // Usuarios de prueba definidos en datos.js
    const organizadores = Datos.organizadores.map(usuario => ({
        ...usuario,
        rol: "organizador"
    }));

    const asistentes = Datos.asistentes.map(usuario => ({
        ...usuario,
        rol: "asistente"
    }));

    // Unificamos los usuarios para buscar en una sola lista
    const usuarios = [
        ...registrados,
        ...organizadores,
        ...asistentes
    ];

    const usuarioEncontrado = usuarios.find(usuario =>
        (
            usuario.nickname.toLowerCase() === loginUser ||
            usuario.correo.toLowerCase() === loginUser
        ) &&
        usuario.password === loginPass
    );

    if (!usuarioEncontrado) {
        mostrarErrorLogin(inputPass, "Credenciales incorrectas.");
        return;
    }

    // Guardamos únicamente los datos necesarios de la sesión
    const sesion = {
        id: usuarioEncontrado.id,
        nickname: usuarioEncontrado.nickname,
        nombre: usuarioEncontrado.nombre,
        rol: usuarioEncontrado.rol.toLowerCase()
    };

    sessionStorage.setItem("usuarioLogueado", JSON.stringify(sesion));

    alert("¡Bienvenido/a " + sesion.nombre + "!");

    window.location.href = "../index.html";
}


function ejecutarCerrarSesion() {
    sessionStorage.removeItem("usuarioLogueado");
    alert("Sesión cerrada correctamente.");
    if (window.location.pathname.includes("/pages/")) {
        window.location.href = "../index.html";
    } else {
        window.location.href = "index.html";
    }
}