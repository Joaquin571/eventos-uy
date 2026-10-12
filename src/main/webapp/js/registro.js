
const usuarioActual = JSON.parse(
    sessionStorage.getItem("usuarioLogueado")
);

const rolActual = usuarioActual?.rol?.toLowerCase() || "visitante";

const asistenteActualId = rolActual === "asistente"
    ? Number(usuarioActual.id)
    : null;

const organizadorActualId = rolActual === "organizador"
    ? Number(usuarioActual.id)
    : null;


// =====================================================
// FUNCIONES GENERALES
// =====================================================

function formatearFecha(fechaIso) {
    if (!fechaIso) return "-";

    const partes = fechaIso.split("-");

    if (partes.length !== 3) return fechaIso;

    return partes[2] + "/" + partes[1] + "/" + partes[0];
}

function obtenerRegistros() {
    const guardados = localStorage.getItem("registrosEventos");

    if (guardados !== null) {
        try {
            const registros = JSON.parse(guardados);

            if (Array.isArray(registros)) {
                return registros;
            }
        } catch (error) {
            console.error("Error al cargar registros:", error);
        }
    }

    return Datos.registros;
}

function guardarRegistros(registros) {
    localStorage.setItem(
        "registrosEventos",
        JSON.stringify(registros)
    );

    Datos.registros = registros;
}

function obtenerAsistente(id) {
    const asistente = Datos.asistentes.find(
        asistente => Number(asistente.id) === Number(id)
    );

    if (asistente) return asistente;

    const registrados = JSON.parse(
        localStorage.getItem("usuariosRegistrados") || "[]"
    );

    return registrados.find(
        usuario => usuario.rol?.toLowerCase() === "asistente" &&
                   Number(usuario.id) === Number(id)
    );
}

function obtenerIdNuevo(registros) {
    return registros.reduce(
        (maximo, registro) => Math.max(maximo, Number(registro.id) || 0),
        0
    ) + 1;
}


// =====================================================
// INICIALIZACIÓN
// =====================================================

document.addEventListener("DOMContentLoaded", function () {

    Datos.registros = obtenerRegistros();

    inicializarRegistroEdicion();
    inicializarConsultaRegistro();

});


// =====================================================
// REGISTRO A EDICIÓN DE EVENTO
// =====================================================

function inicializarRegistroEdicion() {

    const formRegistroEdicion = document.getElementById("formRegistroEdicion");

    if (!formRegistroEdicion) return;

    if (rolActual !== "asistente") {
        alert("Debés iniciar sesión como asistente para registrarte.");
        window.location.replace("../index.html");
        return;
    }

    const selectEvento = document.getElementById("evento");
    const selectEdicion = document.getElementById("edicion");
    const selectTipoRegistro = document.getElementById("tipoRegistro");
    const detalleEdicion = document.getElementById("detalleEdicion");
    const radioGeneral = document.getElementById("modalidadGeneral");
    const radioCodigo = document.getElementById("modalidadCodigo");
    const contenedorCodigo = document.getElementById("contenedorCodigo");
    const inputCodigo = document.getElementById("codigo");

    cargarSelect(
        selectEvento,
        Datos.eventos,
        "Seleccione un evento"
    );

    cargarSelect(
        selectEdicion,
        [],
        "Seleccione una edición"
    );

    selectEdicion.disabled = true;

    function reiniciarTipos() {
        cargarSelect(
            selectTipoRegistro,
            [],
            "Seleccione un tipo de registro"
        );

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

    reiniciarTipos();
    actualizarModalidad();

    radioGeneral.addEventListener("change", actualizarModalidad);
    radioCodigo.addEventListener("change", actualizarModalidad);


    // Selección de evento

    selectEvento.addEventListener("change", function () {

        const eventoId = Number(selectEvento.value);

        reiniciarTipos();
        detalleEdicion.classList.add("d-none");

        if (!eventoId) {
            cargarSelect(
                selectEdicion,
                [],
                "Seleccione una edición"
            );

            selectEdicion.disabled = true;
            return;
        }

        const edicionesAceptadas = Datos.ediciones.filter(
            edicion => Number(edicion.eventoId) === eventoId &&
                       edicion.estado === "Aceptada"
        );

        cargarSelect(
            selectEdicion,
            edicionesAceptadas,
            "Seleccione una edición"
        );

        selectEdicion.disabled = false;
    });


    // Selección de edición

    selectEdicion.addEventListener("change", function () {

        const edicionId = Number(selectEdicion.value);

        reiniciarTipos();

        if (!edicionId) {
            detalleEdicion.classList.add("d-none");
            return;
        }

        const edicion = Datos.ediciones.find(
            edicion => Number(edicion.id) === edicionId
        );

        if (!edicion) {
            detalleEdicion.classList.add("d-none");
            return;
        }

        document.getElementById("siglaEdicion").textContent =
            edicion.sigla;

        document.getElementById("ciudadEdicion").textContent =
            edicion.ciudad;

        document.getElementById("paisEdicion").textContent =
            edicion.pais;

        document.getElementById("inicioEdicion").textContent =
            formatearFecha(edicion.fechaInicio);

        document.getElementById("finEdicion").textContent =
            formatearFecha(edicion.fechaFin);

        detalleEdicion.classList.remove("d-none");

        const tiposDeLaEdicion = Datos.tiposRegistro
            .filter(tipo => Number(tipo.edicionId) === edicionId)
            .map(tipo => ({
                id: tipo.id,
                nombre: tipo.nombre + " - $" + tipo.costo
            }));

        cargarSelect(
            selectTipoRegistro,
            tiposDeLaEdicion,
            "Seleccione un tipo de registro"
        );

        selectTipoRegistro.disabled = false;
    });

    // Preselección desde enlaces (?edicion=<id>)
    const edicionParam = Number(new URLSearchParams(location.search).get("edicion"));
    const edicionPre = Datos.ediciones.find(
        e => Number(e.id) === edicionParam && e.estado === "Aceptada"
    );

    if (edicionPre) {
        selectEvento.value = edicionPre.eventoId;
        selectEvento.dispatchEvent(new Event("change"));
        selectEdicion.value = edicionPre.id;
        selectEdicion.dispatchEvent(new Event("change"));
    }

    // Confirmar registro

    formRegistroEdicion.addEventListener("submit", function (event) {

        event.preventDefault();

        const edicionId = Number(selectEdicion.value);
        const tipoRegistroId = Number(selectTipoRegistro.value);
        const conCodigo = radioCodigo.checked;
        const codigo = inputCodigo.value.trim();

        if (!edicionId || !tipoRegistroId) {
            alert("Seleccioná una edición y un tipo de registro.");
            return;
        }

        const asistente = obtenerAsistente(asistenteActualId);

        if (!asistente) {
            alert("No se encontró el asistente en los datos del sistema.");
            return;
        }

        const tipoRegistro = Datos.tiposRegistro.find(
            tipo => Number(tipo.id) === tipoRegistroId &&
                    Number(tipo.edicionId) === edicionId
        );

        if (!tipoRegistro) {
            alert("El tipo de registro seleccionado no es válido.");
            return;
        }

        const registros = obtenerRegistros();

        // Verificar que el asistente no esté registrado

        const yaRegistrado = registros.some(
            registro => Number(registro.edicionId) === edicionId &&
                        Number(registro.asistenteId) === asistenteActualId
        );

        if (yaRegistrado) {
            alert("Ya estás registrado en esta edición.");
            return;
        }

        // Verificar cupo

        const registrosDelTipo = registros.filter(
            registro => Number(registro.tipoRegistroId) === tipoRegistroId
        ).length;

        if (registrosDelTipo >= Number(tipoRegistro.cupo)) {
            alert("Se alcanzó el cupo de este tipo de registro.");
            return;
        }

        let costo = Number(tipoRegistro.costo);
        let patrocinioId = null;

        // Registro mediante código de patrocinio

        if (conCodigo) {

            if (asistente.institucionId == null) {
                alert(
                    "Un asistente sin institución solo puede registrarse de forma general."
                );
                return;
            }

            const patrocinio = Datos.patrocinios.find(
                patrocinio =>
                    patrocinio.codigo.toLowerCase() === codigo.toLowerCase()
            );

            if (!patrocinio) {
                alert("El código de patrocinio ingresado no existe.");
                return;
            }

            if (Number(patrocinio.edicionId) !== edicionId) {
                alert(
                    "El código de patrocinio no corresponde a esta edición."
                );
                return;
            }

            if (Number(patrocinio.tipoRegistroId) !== tipoRegistroId) {
                alert(
                    "El código de patrocinio no corresponde al tipo de registro elegido."
                );
                return;
            }

            if (
                Number(patrocinio.institucionId) !==
                Number(asistente.institucionId)
            ) {
                alert(
                    "El código de patrocinio no corresponde a tu institución."
                );
                return;
            }

            const usosRealizados = registros.filter(
                registro => Number(registro.patrocinioId) ===
                            Number(patrocinio.id)
            ).length;

            if (usosRealizados >= Number(patrocinio.cantidadGratuitos)) {
                alert(
                    "El código de patrocinio ya no tiene usos disponibles."
                );
                return;
            }

            costo = 0;
            patrocinioId = patrocinio.id;
        }

        // Guardar registro

        const nuevoRegistro = {
            id: obtenerIdNuevo(registros),
            edicionId: edicionId,
            asistenteId: asistenteActualId,
            tipoRegistroId: tipoRegistroId,
            fechaAlta: new Date().toLocaleDateString("en-CA"),
            costo: costo,
            patrocinioId: patrocinioId
        };

        registros.push(nuevoRegistro);

        guardarRegistros(registros);

        alert("Registro realizado correctamente. Costo: $" + costo);

        formRegistroEdicion.reset();

        cargarSelect(
            selectEdicion,
            [],
            "Seleccione una edición"
        );

        selectEdicion.disabled = true;

        reiniciarTipos();
        detalleEdicion.classList.add("d-none");
        actualizarModalidad();
    });
}


// =====================================================
// CONSULTA DE REGISTRO
// =====================================================

function inicializarConsultaRegistro() {

    const detalleRegistro = document.getElementById("detalleRegistro");

    if (!detalleRegistro) return;

    if (rolActual !== "asistente" && rolActual !== "organizador") {
        alert("Debés iniciar sesión para consultar registros.");
        window.location.replace("../index.html");
        return;
    }

    const esOrganizador = rolActual === "organizador";

    const selectEdicion = document.getElementById("edicion");
    const selectAsistente = document.getElementById("asistente");
    const contenedorAsistente = document.getElementById("contenedorAsistente");
    const filaAsistente = document.getElementById("filaAsistente");

    let edicionesDisponibles;

    if (esOrganizador) {

        edicionesDisponibles = Datos.ediciones.filter(
            edicion => Number(edicion.organizadorId) === organizadorActualId
        );

        contenedorAsistente.classList.remove("d-none");

    } else {

        edicionesDisponibles = Datos.ediciones.filter(
            edicion => Datos.registros.some(
                registro => Number(registro.edicionId) === Number(edicion.id) &&
                            Number(registro.asistenteId) === asistenteActualId
            )
        );

        filaAsistente.classList.add("d-none");
    }

    cargarSelect(
        selectEdicion,
        edicionesDisponibles,
        "Seleccione una edición"
    );


    // Mostrar detalle del registro

    function mostrarRegistro(registroId) {

        const registro = Datos.registros.find(
            registro => Number(registro.id) === Number(registroId)
        );

        if (!registro) {
            alert("No se encontró el registro.");
            return;
        }

        const asistente = obtenerAsistente(registro.asistenteId);

        const edicion = Datos.ediciones.find(
            edicion => Number(edicion.id) === Number(registro.edicionId)
        );

        const tipoRegistro = Datos.tiposRegistro.find(
            tipo => Number(tipo.id) === Number(registro.tipoRegistroId)
        );

        if (!asistente || !edicion || !tipoRegistro) {
            alert("No se pudieron obtener todos los datos del registro.");
            return;
        }

        document.getElementById("nombreAsistente").textContent =
            asistente.nickname;

        document.getElementById("nombreEdicion").textContent =
            edicion.nombre;

        document.getElementById("tipoRegistroDetalle").textContent =
            tipoRegistro.nombre;

        document.getElementById("fechaRegistro").textContent =
            formatearFecha(registro.fechaAlta);

        document.getElementById("costoRegistro").textContent =
            "$" + registro.costo;

        document.getElementById("usoCodigo").textContent =
            registro.patrocinioId != null ? "Sí" : "No";

        detalleRegistro.classList.remove("d-none");
    }


    // Selección de edición

    selectEdicion.addEventListener("change", function () {

        const edicionId = Number(selectEdicion.value);

        detalleRegistro.classList.add("d-none");

        if (!edicionId) {

            cargarSelect(
                selectAsistente,
                [],
                "Seleccione un asistente"
            );

            selectAsistente.disabled = true;
            return;
        }

        if (esOrganizador) {

            const asistentesRegistrados = Datos.registros
                .filter(registro => Number(registro.edicionId) === edicionId)
                .map(registro => {

                    const asistente = obtenerAsistente(registro.asistenteId);

                    if (!asistente) return null;

                    return {
                        id: registro.id,
                        nombre: asistente.nickname
                    };
                })
                .filter(asistente => asistente !== null);

            cargarSelect(
                selectAsistente,
                asistentesRegistrados,
                "Seleccione un asistente"
            );

            selectAsistente.disabled = asistentesRegistrados.length === 0;

            if (asistentesRegistrados.length === 0) {
                alert(
                    "Esta edición todavía no tiene asistentes registrados."
                );
            }

        } else {

            const miRegistro = Datos.registros.find(
                registro => Number(registro.edicionId) === edicionId &&
                            Number(registro.asistenteId) === asistenteActualId
            );

            if (miRegistro) {
                mostrarRegistro(miRegistro.id);
            } else {
                detalleRegistro.classList.add("d-none");
                alert("No se encontró tu registro para esta edición.");
            }
        }
    });


    // Selección de asistente

    selectAsistente.addEventListener("change", function () {

        const registroId = Number(selectAsistente.value);

        if (!registroId) {
            detalleRegistro.classList.add("d-none");
            return;
        }

        mostrarRegistro(registroId);
    });
}
