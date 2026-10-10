document.addEventListener("DOMContentLoaded", () => {
    actualizarHeaderGlobal();

    const formLogin = document.getElementById("formLogin");
    if (formLogin) {
        formLogin.addEventListener("submit", iniciarSesion);
    }
});

function actualizarHeaderGlobal() {
    const usuarioLogueado = JSON.parse(sessionStorage.getItem("usuarioLogueado"));
    const contenedor = document.getElementById("accionesUsuario") || document.getElementById("contenedorBotonSesion");

    if (usuarioLogueado && contenedor) {
        const enSubcarpeta = window.location.pathname.includes("/pages/");
        const rutaPerfil = enSubcarpeta
            ? `usuario.html?nick=${encodeURIComponent(usuarioLogueado.nickname)}`
            : `pages/usuario.html?nick=${encodeURIComponent(usuarioLogueado.nickname)}`;

        contenedor.innerHTML = `
            <div class="dropdown">
                <button class="btn btn-light dropdown-toggle fw-bold text-primary px-3 py-1 shadow-sm" 
                        type="button" 
                        id="dropdownMenuUser" 
                        data-bs-toggle="dropdown" 
                        aria-expanded="false">
                    <i class="bi bi-person-circle me-1"></i>${usuarioLogueado.nombre}
                </button>
                <ul class="dropdown-menu dropdown-menu-end shadow border-0 mt-2" aria-labelledby="dropdownMenuUser">
                    <li>
                        <a class="dropdown-item py-2 fw-semibold" href="${rutaPerfil}">
                            <i class="bi bi-person me-2"></i>Mi perfil
                        </a>
                    </li>
                    <li><hr class="dropdown-divider my-1"></li>
                    <li>
                        <button class="dropdown-item py-2 fw-semibold text-danger" type="button" onclick="ejecutarCerrarSesion()">
                            <i class="bi bi-box-arrow-right me-2"></i>Cerrar sesión
                        </button>
                    </li>
                </ul>
            </div>`;
    }
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