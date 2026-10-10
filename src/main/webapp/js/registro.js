/* =====================================================
   FUNCIONES COMPARTIDAS
   ===================================================== */

// Usuario que inició sesión (lo guarda login.js en sessionStorage).
// Devuelve null si es un visitante.
function obtenerUsuarioSesion() {
    try {
        return JSON.parse(sessionStorage.getItem("usuarioLogueado"));
    } catch (error) {
        return null;
    }
}

function formatearFecha(fechaIso) {
    const partes = fechaIso.split("-");
    return partes[2] + "/" + partes[1] + "/" + partes[0];
}

// Busca un asistente en datos.js y, si no está, entre los usuarios
// que se dieron de alta desde el sitio (localStorage).
function obtenerAsistente(asistenteId) {
    const precargado = Datos.asistentes.find(
        asistente => asistente.id === asistenteId
    );

    if (precargado) {
        return precargado;
    }

    let registrados = [];

    try {
        registrados = JSON.parse(localStorage.getItem("usuariosRegistrados")) || [];
    } catch (error) {
        registrados = [];
    }

    return registrados.find(usuario => usuario.id === asistenteId) || null;
}

// Los registros nuevos se guardan en localStorage para que se vean
// al pasar de "Registro a edición" a "Consulta de registro".
function cargarRegistrosGuardados() {
    let guardados = [];

    try {
        guardados = JSON.parse(localStorage.getItem("registrosNuevos")) || [];
    } catch (error) {
        guardados = [];
    }

    guardados.forEach(registro => {
        const yaCargado = Datos.registros.some(
            existente => existente.id === registro.id
        );

        if (!yaCargado) {
            Datos.registros.push(registro);
        }
    });
}

function guardarRegistroNuevo(registro) {
    let guardados = [];

    try {
        guardados = JSON.parse(localStorage.getItem("registrosNuevos")) || [];
    } catch (error) {
        guardados = [];
    }

    guardados.push(registro);
    localStorage.setItem("registrosNuevos", JSON.stringify(guardados));
}

// Muestra un aviso (alert de Bootstrap) en la página.
function mostrarAviso(tipo, mensaje, conLogin) {
    const aviso = document.getElementById("mensajeAcceso");

    aviso.className = "alert alert-" + tipo;
    aviso.textContent = mensaje;

    if (conLogin) {
        const enlace = document.createElement("a");
        enlace.href = "login.html";
        enlace.className = "alert-link ms-1";
        enlace.textContent = "Iniciar sesión";
        aviso.appendChild(enlace);
    }
}

cargarRegistrosGuardados();

/* =====================================================
   REGISTRO A EDICIÓN DE EVENTO
   ===================================================== */
const formRegistroEdicion = document.getElementById("formRegistroEdicion");

if (formRegistroEdicion) {
    const usuario = obtenerUsuarioSesion();

    if (!usuario) {
        mostrarAviso("warning", "Para registrarte a una edición tenés que iniciar sesión como asistente.", true);
    } else if (usuario.rol !== "asistente") {
        mostrarAviso("warning", "Solo los asistentes pueden registrarse a una edición.", false);
    } else {
        iniciarRegistroEdicion(usuario);
    }
}

function iniciarRegistroEdicion(usuario) {
    const selectEvento = document.getElementById("evento");
    const selectEdicion = document.getElementById("edicion");
    const selectTipoRegistro = document.getElementById("tipoRegistro");
    const detalleEdicion = document.getElementById("detalleEdicion");
    const radioGeneral = document.getElementById("modalidadGeneral");
    const radioCodigo = document.getElementById("modalidadCodigo");
    const contenedorCodigo = document.getElementById("contenedorCodigo");
    const inputCodigo = document.getElementById("codigo");

    formRegistroEdicion.classList.remove("d-none");

    cargarSelect(selectEvento, Datos.eventos, "Seleccione un evento");

    function reiniciarTipos() {
        cargarSelect(selectTipoRegistro, [], "Seleccione un tipo de registro");
        selectTipoRegistro.disabled = true;
    }

    function actualizarModalidad() {
        const conCodigo = radioCodigo.checked;

        contenedorCodigo.classList.toggle("d-none", !conCodigo);
        inputCodigo.required = conCodigo;

        if (!conCodigo) {
            inputCodigo.value = "";
        }
    }

    radioGeneral.addEventListener("change", actualizarModalidad);
    radioCodigo.addEventListener("change", actualizarModalidad);

    selectEvento.addEventListener("change", function () {
        const eventoId = Number(selectEvento.value);

        reiniciarTipos();
        detalleEdicion.classList.add("d-none");

        if (!eventoId) {
            cargarSelect(selectEdicion, [], "Seleccione una edición");
            selectEdicion.disabled = true;
            return;
        }

        // solo ediciones Aceptadas
        const edicionesAceptadas = Datos.ediciones.filter(
            edicion => edicion.eventoId === eventoId &&
                       edicion.estado === "Aceptada"
        );

        cargarSelect(selectEdicion, edicionesAceptadas, "Seleccione una edición");
        selectEdicion.disabled = false;
    });

    selectEdicion.addEventListener("change", function () {
        const edicionId = Number(selectEdicion.value);

        reiniciarTipos();

        if (!edicionId) {
            detalleEdicion.classList.add("d-none");
            return;
        }

        const edicion = Datos.ediciones.find(
            edicion => edicion.id === edicionId
        );

        document.getElementById("siglaEdicion").textContent = edicion.sigla;
        document.getElementById("ciudadEdicion").textContent = edicion.ciudad;
        document.getElementById("paisEdicion").textContent = edicion.pais;
        document.getElementById("inicioEdicion").textContent = formatearFecha(edicion.fechaInicio);
        document.getElementById("finEdicion").textContent = formatearFecha(edicion.fechaFin);

        detalleEdicion.classList.remove("d-none");

        // se muestra el costo junto al nombre del tipo
        const tiposDeLaEdicion = Datos.tiposRegistro
            .filter(tipo => tipo.edicionId === edicionId)
            .map(tipo => ({
                id: tipo.id,
                nombre: tipo.nombre + " - $" + tipo.costo
            }));

        cargarSelect(selectTipoRegistro, tiposDeLaEdicion, "Seleccione un tipo de registro");
        selectTipoRegistro.disabled = false;
    });


    formRegistroEdicion.addEventListener("submit", function (event) {
        event.preventDefault();

        const edicionId = Number(selectEdicion.value);
        const tipoRegistroId = Number(selectTipoRegistro.value);
        const conCodigo = radioCodigo.checked;
        const codigo = inputCodigo.value.trim();
        const institucionId = usuario.institucionId || null;

        const tipoRegistro = Datos.tiposRegistro.find(
            tipo => tipo.id === tipoRegistroId
        );

        //validaciones
        const yaRegistrado = Datos.registros.some(
            registro => registro.edicionId === edicionId &&
                        registro.asistenteId === usuario.id
        );

        if (yaRegistrado) {
            alert("Ya estás registrado en esta edición.");
            return;
        }

        const registrosDelTipo = Datos.registros.filter(
            registro => registro.tipoRegistroId === tipoRegistroId
        ).length;

        if (registrosDelTipo >= tipoRegistro.cupo) {
            alert("Se alcanzó el cupo de este tipo de registro.");
            return;
        }

        let costo = tipoRegistro.costo;
        let patrocinioId = null;

        if (conCodigo) {
            if (institucionId === null) {
                alert("Un asistente sin institución solo puede registrarse de forma general.");
                return;
            }

            const patrocinio = Datos.patrocinios.find(
                patrocinio => patrocinio.codigo.toLowerCase() === codigo.toLowerCase()
            );

            if (!patrocinio) {
                alert("El código de patrocinio ingresado no existe.");
                return;
            }

            if (patrocinio.edicionId !== edicionId) {
                alert("El código de patrocinio no corresponde a esta edición.");
                return;
            }

            if (patrocinio.tipoRegistroId !== tipoRegistroId) {
                alert("El código de patrocinio no corresponde al tipo de registro elegido.");
                return;
            }

            if (patrocinio.institucionId !== institucionId) {
                alert("El código de patrocinio no corresponde a tu institución.");
                return;
            }

            const usosRealizados = Datos.registros.filter(
                registro => registro.patrocinioId === patrocinio.id
            ).length;

            if (usosRealizados >= patrocinio.cantidadGratuitos) {
                alert("El código de patrocinio ya no tiene usos disponibles.");
                return;
            }

            costo = 0;
            patrocinioId = patrocinio.id;
        }

        //guardar
        const nuevoRegistro = {
            id: Date.now(),
            edicionId: edicionId,
            asistenteId: usuario.id,
            tipoRegistroId: tipoRegistroId,
            fechaAlta: new Date().toISOString().split("T")[0],
            costo: costo,
            patrocinioId: patrocinioId
        };

        Datos.registros.push(nuevoRegistro);
        guardarRegistroNuevo(nuevoRegistro);

        alert("Registro realizado correctamente. Costo: $" + costo);

        formRegistroEdicion.reset();

        cargarSelect(selectEdicion, [], "Seleccione una edición");
        selectEdicion.disabled = true;
        reiniciarTipos();
        detalleEdicion.classList.add("d-none");
        actualizarModalidad();
    });

    // Si se llega desde la consulta de edición (registro-edicion.html?edicion=1)
    // se eligen solos el evento y la edición.
    const edicionIdParam = Number(new URLSearchParams(location.search).get("edicion"));

    if (edicionIdParam) {
        const edicionInicial = Datos.ediciones.find(
            edicion => edicion.id === edicionIdParam &&
                       edicion.estado === "Aceptada"
        );

        if (edicionInicial) {
            selectEvento.value = edicionInicial.eventoId;
            selectEvento.dispatchEvent(new Event("change"));

            selectEdicion.value = edicionInicial.id;
            selectEdicion.dispatchEvent(new Event("change"));
        }
    }
}

/* =====================================================
   CONSULTA DE REGISTRO
   ===================================================== */
const detalleRegistro = document.getElementById("detalleRegistro");

if (detalleRegistro) {
    const usuario = obtenerUsuarioSesion();

    if (!usuario) {
        mostrarAviso("warning", "Para consultar registros tenés que iniciar sesión como asistente u organizador.", true);
    } else if (usuario.rol !== "asistente" && usuario.rol !== "organizador") {
        mostrarAviso("warning", "Solo los asistentes y los organizadores pueden consultar registros.", false);
    } else {
        iniciarConsultaRegistro(usuario);
    }
}

function iniciarConsultaRegistro(usuario) {
    const esOrganizador = usuario.rol === "organizador";

    const selectEdicion = document.getElementById("edicion");
    const selectAsistente = document.getElementById("asistente");
    const contenedorAsistente = document.getElementById("contenedorAsistente");
    const filaAsistente = document.getElementById("filaAsistente");

    // El organizador ve las ediciones que organiza;
    // el asistente, las ediciones en las que se registró.
    let edicionesDisponibles;

    if (esOrganizador) {
        edicionesDisponibles = Datos.ediciones.filter(
            edicion => edicion.organizadorId === usuario.id
        );
        contenedorAsistente.classList.remove("d-none");
    } else {
        edicionesDisponibles = Datos.ediciones.filter(
            edicion => Datos.registros.some(
                registro => registro.edicionId === edicion.id &&
                            registro.asistenteId === usuario.id
            )
        );
        filaAsistente.classList.add("d-none");
    }

    if (edicionesDisponibles.length === 0) {
        mostrarAviso(
            "info",
            esOrganizador
                ? "Todavía no tenés ediciones a tu cargo."
                : "Todavía no te registraste a ninguna edición.",
            false
        );
        return;
    }

    document.getElementById("contenidoConsulta").classList.remove("d-none");

    cargarSelect(selectEdicion, edicionesDisponibles, "Seleccione una edición");

    function mostrarRegistro(registroId) {
        const registro = Datos.registros.find(registro => registro.id === registroId);

        const asistente = obtenerAsistente(registro.asistenteId);

        const edicion = Datos.ediciones.find(
            edicion => edicion.id === registro.edicionId
        );

        const tipoRegistro = Datos.tiposRegistro.find(
            tipo => tipo.id === registro.tipoRegistroId
        );

        document.getElementById("nombreAsistente").textContent =
            asistente ? asistente.nickname : "Desconocido";
        document.getElementById("nombreEdicion").textContent = edicion.nombre;
        document.getElementById("tipoRegistroDetalle").textContent = tipoRegistro.nombre;
        document.getElementById("fechaRegistro").textContent = formatearFecha(registro.fechaAlta);
        document.getElementById("costoRegistro").textContent = "$" + registro.costo;
        document.getElementById("usoCodigo").textContent =
            registro.patrocinioId !== null ? "Sí" : "No";

        detalleRegistro.classList.remove("d-none");
    }

    selectEdicion.addEventListener("change", function () {
        const edicionId = Number(selectEdicion.value);

        detalleRegistro.classList.add("d-none");

        if (!edicionId) {
            cargarSelect(selectAsistente, [], "Seleccione un asistente");
            selectAsistente.disabled = true;
            return;
        }

        if (esOrganizador) {
            const asistentesRegistrados = Datos.registros
                .filter(registro => registro.edicionId === edicionId)
                .map(registro => {
                    const asistente = obtenerAsistente(registro.asistenteId);

                    return {
                        id: registro.id,
                        nombre: asistente ? asistente.nickname : "Desconocido"
                    };
                });

            cargarSelect(selectAsistente, asistentesRegistrados, "Seleccione un asistente");
            selectAsistente.disabled = false;

            if (asistentesRegistrados.length === 0) {
                alert("Esta edición todavía no tiene asistentes registrados.");
            }
        } else {
            // el asistente ve directamente su registro
            const miRegistro = Datos.registros.find(
                registro => registro.edicionId === edicionId &&
                            registro.asistenteId === usuario.id
            );

            if (miRegistro) {
                mostrarRegistro(miRegistro.id);
            }
        }
    });

    selectAsistente.addEventListener("change", function () {
        const registroId = Number(selectAsistente.value);

        if (!registroId) {
            detalleRegistro.classList.add("d-none");
            return;
        }

        mostrarRegistro(registroId);
    });

    // Si se llega con consulta-registro.html?edicion=1 o ?id=<registro>
    // se elige sola la edición (y el asistente, si es organizador).
    const parametros = new URLSearchParams(location.search);
    const registroIdParam = Number(parametros.get("id"));
    let edicionIdParam = Number(parametros.get("edicion"));

    const registroInicial = registroIdParam
        ? Datos.registros.find(registro => registro.id === registroIdParam)
        : null;

    if (registroInicial) {
        edicionIdParam = registroInicial.edicionId;
    }

    const edicionPermitida = edicionesDisponibles.some(
        edicion => edicion.id === edicionIdParam
    );

    if (edicionPermitida) {
        selectEdicion.value = edicionIdParam;
        selectEdicion.dispatchEvent(new Event("change"));

        if (esOrganizador && registroInicial) {
            selectAsistente.value = registroInicial.id;
            selectAsistente.dispatchEvent(new Event("change"));
        }
    }
}