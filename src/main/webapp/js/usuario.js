document.addEventListener("DOMContentLoaded", () => {
    cargarInstitucionesEnSelect();

    const inputFecha = document.getElementById("fechaNac");
    if (inputFecha) {
        const hoy = new Date().toISOString().split("T")[0];
        inputFecha.max = hoy;
    }

    const tipoRolSelect = document.getElementById("tipoRol");
    if (tipoRolSelect) {
        tipoRolSelect.addEventListener("change", alternarCamposRol);
    }

    const formAlta = document.getElementById("formAltaUsuario");
    if (formAlta) {
        formAlta.addEventListener("submit", guardarUsuario);
    }
});

function togglePassVisibility(idInput, btn) {
    const input = document.getElementById(idInput);
    if (!input) return;

    const icon = btn.querySelector("i");
    if (input.type === "password") {
        input.type = "text";
        if (icon) icon.className = "bi bi-eye-slash";
    } else {
        input.type = "password";
        if (icon) icon.className = "bi bi-eye";
    }
}

function cargarInstitucionesEnSelect() {
    const selectInst = document.getElementById("institucion");
    if (!selectInst) return;

    if (typeof Datos !== "undefined" && Array.isArray(Datos.instituciones)) {
        selectInst.innerHTML = '<option value="">Ninguna</option>';
        Datos.instituciones.forEach(inst => {
            const option = document.createElement("option");
            option.value = inst.id;
            option.textContent = inst.nombre;
            selectInst.appendChild(option);
        });
    }
}

function alternarCamposRol() {
    const rol = document.getElementById("tipoRol").value;
    const cAsistente = document.getElementById("camposAsistente");
    const cOrganizador = document.getElementById("camposOrganizador");
    const bloqueApellido = document.getElementById("bloqueApellido");

    if (rol === "organizador") {
        if (cAsistente) cAsistente.classList.add("d-none");
        if (bloqueApellido) bloqueApellido.classList.add("d-none");
        if (cOrganizador) cOrganizador.classList.remove("d-none");
    } else {
        if (cOrganizador) cOrganizador.classList.add("d-none");
        if (cAsistente) cAsistente.classList.remove("d-none");
        if (bloqueApellido) bloqueApellido.classList.remove("d-none");
    }
    limpiarErrores();
}

function limpiarErrores() {
    const inputs = document.querySelectorAll("#formAltaUsuario .form-control, #formAltaUsuario .form-select");
    inputs.forEach(input => input.classList.remove("is-invalid"));

    const mensajes = document.querySelectorAll(".invalid-feedback-custom");
    mensajes.forEach(msg => msg.remove());
}

function mostrarError(inputElement, textoMensaje) {
    limpiarErrores();
    if (!inputElement) return;
    inputElement.classList.add("is-invalid");

    const errorDiv = document.createElement("div");
    errorDiv.className = "invalid-feedback-custom";
    errorDiv.textContent = textoMensaje;

    if (inputElement.parentNode.classList.contains("input-group")) {
        inputElement.parentNode.parentNode.appendChild(errorDiv);
    } else {
        inputElement.parentNode.appendChild(errorDiv);
    }
    inputElement.focus();
}

function guardarUsuario(e) {
    e.preventDefault();
    limpiarErrores();

    try {
        const rol = document.getElementById("tipoRol").value;
        const inputNick = document.getElementById("nickname");
        const inputCorreo = document.getElementById("correo");
        const inputNombre = document.getElementById("nombre");
        const inputP1 = document.getElementById("p1");
        const inputP2 = document.getElementById("p2");
        const inputImagen = document.getElementById("imagenPerfil");

        const nickname = inputNick ? inputNick.value.trim() : "";
        const correo = inputCorreo ? inputCorreo.value.trim() : "";
        const nombre = inputNombre ? inputNombre.value.trim() : "";
        const p1 = inputP1 ? inputP1.value.trim() : "";
        const p2 = inputP2 ? inputP2.value.trim() : "";

        if (!nickname) { mostrarError(inputNick, "Campo obligatorio."); return; }
        if (!correo) { mostrarError(inputCorreo, "Campo obligatorio."); return; }

        const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!regexEmail.test(correo)) {
            mostrarError(inputCorreo, "Ingrese un correo electrónico válido.");
            return;
        }

        const registrados = JSON.parse(localStorage.getItem("usuariosRegistrados")) || [];
        let asistentesDatos = (typeof Datos !== "undefined" && Array.isArray(Datos.asistentes)) ? Datos.asistentes : [];

        if (registrados.some(u => u.nickname.toLowerCase() === nickname.toLowerCase()) ||
            asistentesDatos.some(a => a.nickname.toLowerCase() === nickname.toLowerCase())) {
            mostrarError(inputNick, "El nickname ya se encuentra registrado.");
            return;
        }

        if (registrados.some(u => u.correo.toLowerCase() === correo.toLowerCase())) {
            mostrarError(inputCorreo, "El correo electrónico ya se encuentra registrado.");
            return;
        }

        if (!nombre) { mostrarError(inputNombre, "Campo obligatorio."); return; }

        let apellido = "";
        let fechaNac = "";
        let institucionId = null;
        let descripcion = "";
        let sitioWeb = "";

        if (rol === "asistente") {
            const inputApellido = document.getElementById("apellido");
            const inputFecha = document.getElementById("fechaNac");
            const selectInst = document.getElementById("institucion");

            apellido = inputApellido ? inputApellido.value.trim() : "";
            fechaNac = inputFecha ? inputFecha.value : "";
            institucionId = selectInst && selectInst.value ? parseInt(selectInst.value) : null;

            if (!apellido) { mostrarError(inputApellido, "Campo obligatorio."); return; }
            if (!fechaNac) { mostrarError(inputFecha, "Campo obligatorio."); return; }

            const hoy = new Date().toISOString().split("T")[0];
            if (fechaNac > hoy) {
                mostrarError(inputFecha, "La fecha no puede ser futura.");
                return;
            }
        } else {
            const inputDesc = document.getElementById("descripcion");
            const inputWeb = document.getElementById("sitioWeb");
            descripcion = inputDesc ? inputDesc.value.trim() : "";
            sitioWeb = inputWeb ? inputWeb.value.trim() : "";

            if (!descripcion) { mostrarError(inputDesc, "Campo obligatorio."); return; }
        }

        if (!p1) { mostrarError(inputP1, "Campo obligatorio."); return; }
        if (!p2) { mostrarError(inputP2, "Campo obligatorio."); return; }

        if (p1 !== p2) {
            mostrarError(inputP2, "Las contraseñas no coinciden.");
            return;
        }

        // Asignar fecha actual en la que fue creado el perfil
        const fechaAlta = new Date().toISOString().split("T")[0];

        // Imagen del perfil
        const procesarRegistro = (imagenUrl) => {
            const nuevoUsuario = {
                id: Date.now(),
                nickname: nickname,
                correo: correo,
                nombre: nombre,
                password: p1,
                rol: rol,
                fechaAlta: fechaAlta,
                imagen: imagenUrl || "../img/default-avatar.png"
            };

            if (rol === "asistente") {
                nuevoUsuario.apellido = apellido;
                nuevoUsuario.fechaNac = fechaNac;
                nuevoUsuario.institucionId = institucionId;
            } else {
                nuevoUsuario.descripcion = descripcion;
                nuevoUsuario.sitioWeb = sitioWeb;
            }

            registrados.unshift(nuevoUsuario);
            localStorage.setItem("usuariosRegistrados", JSON.stringify(registrados));

            alert("¡Usuario registrado con éxito!");
            window.location.href = "login.html";
        };

        if (inputImagen && inputImagen.files && inputImagen.files[0]) {
            const reader = new FileReader();
            reader.onload = (e) => procesarRegistro(e.target.result);
            reader.readAsDataURL(inputImagen.files[0]);
        } else {
            procesarRegistro(null);
        }

    } catch (err) {
        console.error("Error al registrar usuario:", err);
        alert("Ocurrió un error inesperado: " + err.message);
    }
}