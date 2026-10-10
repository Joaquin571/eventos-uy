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

    const loginUser = inputUser ? inputUser.value.trim().toLowerCase() : "";
    const loginPass = inputPass ? inputPass.value.trim() : "";

    if (!loginUser) {
        mostrarErrorLogin(inputUser, "Ingrese su nickname o correo.");
        return;
    }

    if (!loginPass) {
        mostrarErrorLogin(inputPass, "Ingrese su contraseña.");
        return;
    }

    const registrados = JSON.parse(localStorage.getItem("usuariosRegistrados")) || [];
    let usuarioEncontrado = registrados.find(u =>
        (u.nickname.toLowerCase() === loginUser || u.correo.toLowerCase() === loginUser) &&
        u.password === loginPass
    );

    if (!usuarioEncontrado && typeof Datos !== "undefined" && Array.isArray(Datos.asistentes)) {
        const asistentePrueba = Datos.asistentes.find(a =>
            a.nickname.toLowerCase() === loginUser ||
            `${a.nickname}@eventos.uy`.toLowerCase() === loginUser
        );

        if (asistentePrueba) {
            usuarioEncontrado = {
                id: asistentePrueba.id,
                nickname: asistentePrueba.nickname,
                nombre: asistentePrueba.nombre,
                correo: `${asistentePrueba.nickname}@eventos.uy`,
                rol: "asistente",
                institucionId: asistentePrueba.institucionId
            };
        }
    }

    if (usuarioEncontrado) {
        sessionStorage.setItem("usuarioLogueado", JSON.stringify(usuarioEncontrado));
        alert("¡Bienvenido/a " + usuarioEncontrado.nombre + "!");
        window.location.href = "../index.html";
    } else {
        mostrarErrorLogin(inputPass, "Credenciales incorrectas.");
    }
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