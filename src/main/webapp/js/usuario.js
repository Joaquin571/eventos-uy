document.addEventListener("DOMContentLoaded", () => {
    cargarInstitucionesEnSelect();
    if (document.getElementById("listadoUsuarios")) cargarListadoUsuarios();
    if (document.getElementById("infoDetalleUsuario")) cargarDetallePerfil();
    if (document.getElementById("formModificarUsuario")) inicializarModificarUsuario();

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
        // El alta es solo para visitantes
        if (sessionStorage.getItem("usuarioLogueado")) {
            window.location.replace("../index.html");
            return;
        }
        formAlta.addEventListener("submit", guardarUsuario);
    }
});

function togglePassVisibility(idInput, btn) {
    const input = document.getElementById(idInput);
    if (!input) return;

    if (input.type === "password") {
        input.type = "text";
        btn.textContent = "Ocultar";
    } else {
        input.type = "password";
        btn.textContent = "Ver";
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
        const p1 = inputP1 ? inputP1.value : "";
        const p2 = inputP2 ? inputP2.value : "";

        if (!nickname) { mostrarError(inputNick, "Campo obligatorio."); return; }
        if (!correo) { mostrarError(inputCorreo, "Campo obligatorio."); return; }

        const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!regexEmail.test(correo)) {
            mostrarError(inputCorreo, "Ingrese un correo electrónico válido.");
            return;
        }

        const registrados = JSON.parse(localStorage.getItem("usuariosRegistrados")) || [];
        // Unicidad contra TODOS los usuarios: registrados, asistentes y organizadores precargados
        const existentes = obtenerTodosLosUsuarios();

        if (existentes.some(u => u.nickname && u.nickname.toLowerCase() === nickname.toLowerCase())) {
            mostrarError(inputNick, "El nickname ya se encuentra registrado.");
            return;
        }

        if (existentes.some(u => u.correo && u.correo.toLowerCase() === correo.toLowerCase())) {
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
            if (sitioWeb && !/^https?:\/\/[^\s.]+\.[^\s]+$/i.test(sitioWeb)) {
                mostrarError(inputWeb, "Ingrese una URL válida (ej: https://ejemplo.com).");
                return;
            }
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

// CONSULTA: listado y detalle de usuarios
function obtenerTodosLosUsuarios() {
    // 1. Usuarios registrados a manopla
    const registrados = JSON.parse(localStorage.getItem("usuariosRegistrados")) || [];

    // 2. Asistentes precargados desde datos.js
    let asistentesPrecargados = [];
    try {
        if (typeof Datos !== "undefined" && Array.isArray(Datos.asistentes)) {
            asistentesPrecargados = Datos.asistentes.map(a => ({
                id: a.id,
                nickname: a.nickname,
                nombre: a.nombre,
                apellido: a.apellido || "",
                fechaNac: a.fechaNacimiento || a.fechaNac || "",
                correo: a.correo || "",
                rol: "asistente",
                institucionId: a.institucionId || null,
                fechaAlta: a.fechaAlta || "",
                imagen: a.imagen || "../img/default-avatar.png"
            }));
        }
    } catch (e) {
        console.warn("No se encontraron asistentes en datos.js:", e);
    }

    // 3. Organizadores precargados desde datos.js
    let organizadoresPrecargados = [];
    try {
        if (typeof Datos !== "undefined" && Array.isArray(Datos.organizadores)) {
            organizadoresPrecargados = Datos.organizadores.map(o => ({
                id: o.id,
                nickname: o.nickname,
                nombre: o.nombre,
                correo: o.correo || "",
                rol: "organizador",
                descripcion: o.descripcion || "",
                sitioWeb: o.sitioWeb || "",
                fechaAlta: o.fechaAlta || "",
                imagen: o.imagen || "../img/default-avatar.png"
            }));
        }
    } catch (e) {
        console.warn("No se encontraron organizadores en datos.js:", e);
    }

    const precargados = [...asistentesPrecargados, ...organizadoresPrecargados];
    const cambios = JSON.parse(localStorage.getItem("usuariosModificados") || "{}");
    const todos = [...registrados, ...precargados.map(u => ({...u, ...(cambios[u.nickname] || {})}))];
    return todos.filter((u, i) => u.nickname && todos.findIndex(x => x.nickname.toLowerCase() === u.nickname.toLowerCase()) === i);
}

function cargarListadoUsuarios() {
    const contenedorListado = document.getElementById("listadoUsuarios");
    if (!contenedorListado) return;

    const todos = obtenerTodosLosUsuarios();

    if (!todos || todos.length === 0) {
        contenedorListado.innerHTML = `<div class="alert alert-info">No hay usuarios registrados.</div>`;
        return;
    }

    let html = "";
    todos.forEach((u) => {
        const nick = u.nickname;
        const nombreCompleto = u.apellido ? `${u.nombre} ${u.apellido}` : u.nombre;
        const rolFormateado = u.rol ? (u.rol.charAt(0).toUpperCase() + u.rol.slice(1)) : "Asistente";
        const imgAvatar = u.imagen || "../img/default-avatar.png";

        html += `
            <div class="card border-0 shadow-sm rounded-3">
                <div class="card-body p-4 d-flex align-items-center gap-3">
                    <img src="${imgAvatar}" alt="Foto ${nick}" class="rounded-circle object-fit-cover" width="60" height="60" style="background-color: #e9ecef;">
                    <div>
                        <h3 class="h5 fw-bold text-dark mb-1">${nombreCompleto}</h3>
                        <p class="text-muted mb-1">${rolFormateado} · ${nick}</p>
                        <a href="usuario.html?nick=${encodeURIComponent(nick)}" class="text-decoration-none small fw-bold">Ver perfil</a>
                    </div>
                </div>
            </div>`;
    });

    contenedorListado.innerHTML = html;
}

function cargarDetallePerfil() {
    const contenedorDetalle = document.getElementById("infoDetalleUsuario");
    if (!contenedorDetalle) return;

    const urlParams = new URLSearchParams(window.location.search);
    const nickParam = urlParams.get("nick");
    const usuarioLogueado = JSON.parse(sessionStorage.getItem("usuarioLogueado"));

    const todos = obtenerTodosLosUsuarios();
    let u = null;

    if (nickParam) {
        u = todos.find(item => item.nickname.toLowerCase() === nickParam.toLowerCase());
    } else if (usuarioLogueado) {
        u = usuarioLogueado;
    }

    if (!u) {
        contenedorDetalle.innerHTML = `
            <div class="alert alert-warning border-0 shadow-sm">
                No se encontró el perfil del usuario. <a href="usuarios.html" class="fw-bold">Volver al listado</a>.
            </div>`;
        return;
    }

    const esMiPropioPerfil = usuarioLogueado && (usuarioLogueado.nickname.toLowerCase() === u.nickname.toLowerCase());

    let nombreInstitucion = "Ninguna";
    if (u.institucionId && typeof Datos !== "undefined" && Array.isArray(Datos.instituciones)) {
        const inst = Datos.instituciones.find(i => i.id === u.institucionId);
        if (inst) nombreInstitucion = inst.nombre;
    }

    let datosRolHTML = "";
    if (u.rol === "asistente") {
        datosRolHTML = `
            <p class="mb-2"><strong>Apellido:</strong> ${u.apellido || '-'}</p>
            <p class="mb-2"><strong>Fecha de Nacimiento:</strong> ${u.fechaNac || '-'}</p>
            <p class="mb-2"><strong>Institución:</strong> ${nombreInstitucion}</p>`;
    } else {
        datosRolHTML = `
            <p class="mb-2"><strong>Descripción:</strong> ${u.descripcion || '-'}</p>
            <p class="mb-2"><strong>Sitio Web:</strong> ${u.sitioWeb ? `<a href="${u.sitioWeb}" target="_blank">${u.sitioWeb}</a>` : '-'}</p>`;
    }

    const imgAvatar = u.imagen || "../img/default-avatar.png";

    // DATOS DEL PERFIL
    let htmlPerfil = `
        <div class="d-flex align-items-center gap-4 mb-4 pb-3 border-bottom">
            <img src="${imgAvatar}" class="rounded-circle object-fit-cover shadow-sm" width="90" height="90" style="background-color: #e9ecef;">
            <div>
                <h3 class="fw-bold mb-1">${u.nombre} ${u.apellido || ''}</h3>
                <span class="badge bg-primary text-uppercase mb-2">${u.rol}</span>
                <p class="text-muted small mb-0">Miembro desde: ${u.fechaAlta || 'Fecha no registrada'}</p>
            </div>
        </div>

        <div class="row g-3 fs-6 mb-4">
            <div class="col-md-6">
                <p class="mb-2"><strong>Nickname:</strong> ${u.nickname}</p>
                <p class="mb-2"><strong>Correo:</strong> ${u.correo}</p>
            </div>
            <div class="col-md-6">
                ${datosRolHTML}
            </div>
        </div>`;

    // EDICIONES (Si es Organizador) ---
    let htmlEdiciones = "";
    if (u.rol === "organizador") {
        let ediciones = (typeof Datos !== "undefined" && Array.isArray(Datos.ediciones))
            ? Datos.ediciones.filter(e => String(e.organizadorId) === String(u.id))
            : [];

        // Si NO es su propio perfil, solo ve las ediciones Aceptadas
        if (!esMiPropioPerfil) {
            ediciones = ediciones.filter(e => e.estado === "Aceptada");
        }

        htmlEdiciones = `
            <div class="mt-4 pt-3 border-top">
                <h4 class="h5 fw-bold mb-3">Ediciones Organizadas</h4>
                ${ediciones.length === 0 ? '<p class="text-muted small">No hay ediciones para mostrar.</p>' : '<div class="list-group shadow-sm">'}
        `;

        ediciones.forEach(ed => {
            let badgeClass = "bg-success";
            if (ed.estado === "Ingresada") badgeClass = "bg-warning text-dark";
            if (ed.estado === "Rechazada") badgeClass = "bg-danger";

            htmlEdiciones += `
                <div class="list-group-item d-flex justify-content-between align-items-center py-3">
                    <div>
                        <!-- Enlace cliqueable hacia la Consulta de Edición -->
                        <a href="edicion.html?id=${ed.id}" class="fw-bold text-decoration-none fs-6 text-primary">
                            ${ed.nombre}
                        </a>
                        <small class="text-muted d-block">${ed.ciudad || 'Uruguay'}</small>
                    </div>
                    <span class="badge ${badgeClass}">${ed.estado}</span>
                </div>`;
        });

        if (ediciones.length > 0) htmlEdiciones += `</div>`;
        htmlEdiciones += `</div>`;
    }

    // REGISTROS (Si es Asistente y es SU propio perfil) ---
    let htmlRegistros = "";
    if (u.rol === "asistente" && esMiPropioPerfil) {
        const registrosGuardados = JSON.parse(localStorage.getItem("registrosEventos") || "null");
        const registros = (registrosGuardados || Datos.registros || [])
            .filter(r => String(r.asistenteId) === String(u.id))
            .map(r => ({
                ...r,
                edicionNombre: Datos.ediciones.find(e => String(e.id) === String(r.edicionId))?.nombre || "Edición",
                fecha: r.fechaAlta || r.fechaRegistro || r.fecha || "",
                costo: "$" + (r.costo ?? 0)
            }));

        htmlRegistros = `
            <div class="mt-4 pt-3 border-top">
                <h4 class="h5 fw-bold mb-3">Mis Registros a Ediciones</h4>
                <div class="list-group shadow-sm">
        `;

        registros.forEach(r => {
            htmlRegistros += `
                <div class="list-group-item d-flex justify-content-between align-items-center py-3">
                    <div>
                        <!-- Enlace cliqueable hacia la Consulta de Registro -->
                        <a href="consulta-registro.html" class="fw-bold text-decoration-none fs-6 text-primary">
                            ${r.edicionNombre}
                        </a>
                        <small class="text-muted d-block">Inscripto el: ${r.fecha}</small>
                    </div>
                    <span class="badge bg-secondary fs-6">${r.costo}</span>
                </div>`;
        });

        htmlRegistros += `</div></div>`;
    }

    contenedorDetalle.innerHTML = htmlPerfil + htmlEdiciones + htmlRegistros;
}

// MODIFICACIÓN DE USUARIO: usa la sesión real, nunca un usuario fijo.
function inicializarModificarUsuario() {
    const form = document.getElementById("formModificarUsuario");
    if (!form) return;
    const sesion = JSON.parse(sessionStorage.getItem("usuarioLogueado") || "null");
    if (!sesion) { window.location.href = "login.html"; return; }
    // Cancelar vuelve al perfil del usuario que inició sesión
    const btnCancelar = document.getElementById("btnCancelar");
    if (btnCancelar) {
        btnCancelar.href = "usuario.html?nick=" + encodeURIComponent(sesion.nickname);
    }
    const usuario = obtenerTodosLosUsuarios().find(u => u.nickname?.toLowerCase() === sesion.nickname?.toLowerCase());
    if (!usuario) { alert("No se encontró el usuario de la sesión."); return; }
    const esAsistente = usuario.rol === "asistente";
    const $ = id => document.getElementById(id);
    $("camposAsistente")?.classList.toggle("d-none", !esAsistente);
    $("camposOrganizador")?.classList.toggle("d-none", esAsistente);
    if ($("apellido")) $("apellido").required = esAsistente;
    if ($("fechaNacimiento")) $("fechaNacimiento").required = esAsistente;
    if ($("descripcion")) $("descripcion").required = !esAsistente;
    if ($("rolActual")) $("rolActual").textContent = esAsistente ? "Asistente" : "Organizador";
    if ($("institucion")) {
        $("institucion").innerHTML = '<option value="">Sin institución</option>';
        (Datos.instituciones || []).forEach(i => $("institucion").add(new Option(i.nombre, i.id)));
        $("institucion").value = usuario.institucionId || "";
    }
    for (const [id, valor] of Object.entries({nickname: usuario.nickname, correo: usuario.correo, nombre: usuario.nombre,
        apellido: usuario.apellido, fechaNacimiento: usuario.fechaNacimiento || usuario.fechaNac,
        descripcion: usuario.descripcion, sitioWeb: usuario.sitioWeb})) {
        if ($(id)) $(id).value = valor || "";
    }
    if ($("imagenActual")) $("imagenActual").textContent = usuario.imagen ? "Imagen actual configurada" : "Todavía no tiene imagen. Opcional.";
    form.addEventListener("submit", e => {
        e.preventDefault();
        const nombre = $("nombre").value.trim();
        const pass = $("password")?.value || "";
        const confirmacion = $("confirmacion")?.value || "";
        if (!nombre || (esAsistente && !$("apellido").value.trim()) || (!esAsistente && !$("descripcion").value.trim())) {
            alert("Completá los campos obligatorios."); return;
        }
        if (pass !== confirmacion) { alert("Las contraseñas no coinciden."); return; }
        const cambios = { nombre };
        if (esAsistente) Object.assign(cambios, {apellido: $("apellido").value.trim(),
            fechaNac: $("fechaNacimiento").value, institucionId: $("institucion").value ? Number($("institucion").value) : null});
        else Object.assign(cambios, {descripcion: $("descripcion").value.trim(), sitioWeb: $("sitioWeb").value.trim()});
        if (pass) cambios.password = pass;
        const guardar = imagen => {
            if (imagen) cambios.imagen = imagen;
            const registrados = JSON.parse(localStorage.getItem("usuariosRegistrados") || "[]");
            const indice = registrados.findIndex(u => u.nickname.toLowerCase() === usuario.nickname.toLowerCase());
            if (indice >= 0) {
                registrados[indice] = {...registrados[indice], ...cambios};
                localStorage.setItem("usuariosRegistrados", JSON.stringify(registrados));
            } else {
                const modificaciones = JSON.parse(localStorage.getItem("usuariosModificados") || "{}");
                modificaciones[usuario.nickname] = {...(modificaciones[usuario.nickname] || {}), ...cambios};
                localStorage.setItem("usuariosModificados", JSON.stringify(modificaciones));
            }
            sessionStorage.setItem("usuarioLogueado", JSON.stringify({...sesion, nombre}));
            if ($("password")) $("password").value = "";
            if ($("confirmacion")) $("confirmacion").value = "";
            alert("Datos actualizados correctamente.");
        };
        const imagen = $("imagen")?.files?.[0];
        if (imagen) { const lector = new FileReader(); lector.onload = () => guardar(lector.result); lector.readAsDataURL(imagen); }
        else guardar(null);
    });
}
