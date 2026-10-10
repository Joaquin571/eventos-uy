document.addEventListener("DOMContentLoaded", () => {
    if (document.getElementById("listadoUsuarios")) {
        cargarListadoUsuarios();
    }

    if (document.getElementById("infoDetalleUsuario")) {
        cargarDetallePerfil();
    }
});

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
                correo: `${a.nickname}@eventos.uy`,
                rol: "asistente",
                institucionId: a.institucionId || null,
                fechaAlta: "2026-08-01",
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
                correo: o.correo || `${o.nickname}@eventos.uy`,
                rol: "organizador",
                descripcion: o.descripcion || "",
                sitioWeb: o.sitioWeb || "",
                fechaAlta: "2026-01-15",
                imagen: o.imagen || "../img/default-avatar.png"
            }));
        }
    } catch (e) {
        console.warn("No se encontraron organizadores en datos.js:", e);
    }

    return [...registrados, ...asistentesPrecargados, ...organizadoresPrecargados];
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
        let ediciones = (typeof Datos !== "undefined" && Array.isArray(Datos.ediciones)) ? Datos.ediciones : [
            { id: 1, nombre: "Montevideo Comics 2026", estado: "Aceptada", ciudad: "Montevideo" },
            { id: 2, nombre: "Montevideo Comics 2027", estado: "Ingresada", ciudad: "Montevideo" },
            { id: 3, nombre: "Maratón de Montevideo 2026", estado: "Aceptada", ciudad: "Montevideo" },
            { id: 4, nombre: "Montevideo Comics 2025", estado: "Rechazada", ciudad: "Montevideo" }
        ];

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
        let registros = [
            { id: 1, edicionId: 3, edicionNombre: "Maratón de Montevideo 2026", fecha: "2026-08-20", costo: "$ 1000" },
            { id: 2, edicionId: 1, edicionNombre: "Montevideo Comics 2026", fecha: "2026-10-02", costo: "$ 800" }
        ];

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
                        <a href="registro.html?id=${r.id}" class="fw-bold text-decoration-none fs-6 text-primary">
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