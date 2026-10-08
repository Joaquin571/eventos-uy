// Simulación de la sesión (como organizadorActualId en patrocinio.js).
// Para probar la consulta como organizador: consulta-registro.html?rol=organizador
const asistenteActualId = 1;
const organizadorActualId = 1;
const rolActual = new URLSearchParams(location.search).get("rol") || "asistente";

function formatearFecha(fechaIso) {
    const partes = fechaIso.split("-");
    return partes[2] + "/" + partes[1] + "/" + partes[0];
}

/* =====================================================
   REGISTRO A EDICIÓN DE EVENTO
   ===================================================== */
const formRegistroEdicion = document.getElementById("formRegistroEdicion");

if (formRegistroEdicion) {
    const selectEvento = document.getElementById("evento");
    const selectEdicion = document.getElementById("edicion");
    const selectTipoRegistro = document.getElementById("tipoRegistro");
    const detalleEdicion = document.getElementById("detalleEdicion");
    const radioGeneral = document.getElementById("modalidadGeneral");
    const radioCodigo = document.getElementById("modalidadCodigo");
    const contenedorCodigo = document.getElementById("contenedorCodigo");
    const inputCodigo = document.getElementById("codigo");

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

        const asistente = Datos.asistentes.find(
            asistente => asistente.id === asistenteActualId
        );

        const tipoRegistro = Datos.tiposRegistro.find(
            tipo => tipo.id === tipoRegistroId
        );

        //validaciones
        const yaRegistrado = Datos.registros.some(
            registro => registro.edicionId === edicionId &&
                        registro.asistenteId === asistenteActualId
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
            if (asistente.institucionId === null) {
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

            if (patrocinio.institucionId !== asistente.institucionId) {
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
            id: Datos.registros.length + 1,
            edicionId: edicionId,
            asistenteId: asistenteActualId,
            tipoRegistroId: tipoRegistroId,
            fechaAlta: new Date().toISOString().split("T")[0],
            costo: costo,
            patrocinioId: patrocinioId
        };

        Datos.registros.push(nuevoRegistro);

        alert("Registro realizado correctamente. Costo: $" + costo);

        formRegistroEdicion.reset();

        cargarSelect(selectEdicion, [], "Seleccione una edición");
        selectEdicion.disabled = true;
        reiniciarTipos();
        detalleEdicion.classList.add("d-none");
        actualizarModalidad();
    });
}

/* =====================================================
   CONSULTA DE REGISTRO
   ===================================================== */
const detalleRegistro = document.getElementById("detalleRegistro");

if (detalleRegistro) {
    const esOrganizador = rolActual === "organizador";

    const selectEdicion = document.getElementById("edicion");
    const selectAsistente = document.getElementById("asistente");
    const contenedorAsistente = document.getElementById("contenedorAsistente");
    const filaAsistente = document.getElementById("filaAsistente");

    // El organizador ve las ediciones que organiza;
    // el asistente, las ediciones en las que se registró.
    let edicionesDisponibles;

    if (esOrganizador) {
        edicionesDisponibles = Datos.ediciones.filter(
            edicion => edicion.organizadorId === organizadorActualId
        );
        contenedorAsistente.classList.remove("d-none");
    } else {
        edicionesDisponibles = Datos.ediciones.filter(
            edicion => Datos.registros.some(
                registro => registro.edicionId === edicion.id &&
                            registro.asistenteId === asistenteActualId
            )
        );
        filaAsistente.classList.add("d-none");
    }

    cargarSelect(selectEdicion, edicionesDisponibles, "Seleccione una edición");

    function mostrarRegistro(registroId) {
        const registro = Datos.registros.find(registro => registro.id === registroId);

        const asistente = Datos.asistentes.find(
            asistente => asistente.id === registro.asistenteId
        );

        const edicion = Datos.ediciones.find(
            edicion => edicion.id === registro.edicionId
        );

        const tipoRegistro = Datos.tiposRegistro.find(
            tipo => tipo.id === registro.tipoRegistroId
        );

        document.getElementById("nombreAsistente").textContent = asistente.nickname;
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
                    const asistente = Datos.asistentes.find(
                        asistente => asistente.id === registro.asistenteId
                    );

                    return {
                        id: registro.id,
                        nombre: asistente.nickname
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
                            registro.asistenteId === asistenteActualId
            );

            mostrarRegistro(miRegistro.id);
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
}