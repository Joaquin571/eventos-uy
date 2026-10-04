const organizadorActualId = 1;

const formAltaPatrocinio = document.getElementById("formAltaPatrocinio");
const detallePatrocinio = document.getElementById("detallePatrocinio");

if(formAltaPatrocinio){
    const selectEvento = document.getElementById("evento");
    const selectEdicion = document.getElementById("edicion");
    const selectInstitucion = document.getElementById("institucion");
    const selectTipoRegistro = document.getElementById("tipoRegistro");

    cargarSelect(selectEvento, Datos.eventos, "Seleccione un evento");
    cargarSelect(selectInstitucion, Datos.instituciones, "Seleccione una institución");

    selectEvento.addEventListener("change", function () {
        const eventoId = Number(selectEvento.value);

        const edicionesOrganizador = Datos.ediciones.filter(
            edicion => edicion.eventoId === eventoId &&
                       edicion.organizadorId === organizadorActualId
        );

        cargarSelect(selectEdicion, edicionesOrganizador, "Seleccione una edición");
        cargarSelect(selectTipoRegistro, [], "Seleccione un tipo de registro");
    });

    selectEdicion.addEventListener("change", function () {
        const edicionId = Number(selectEdicion.value);

        const tiposDeLaEdicion = Datos.tiposRegistro.filter(
            tipo => tipo.edicionId === edicionId
        );

        cargarSelect(selectTipoRegistro, tiposDeLaEdicion, "Seleccione un tipo de registro");
    });


    formAltaPatrocinio.addEventListener("submit", function (event) {
        event.preventDefault();

        const edicionId = Number(selectEdicion.value);
        const institucionId = Number(selectInstitucion.value);
        const tipoRegistroId = Number(selectTipoRegistro.value);

        //saca valores
        const nivel = document.getElementById("nivel").value;
        const aporte = Number(document.getElementById("aporte").value);
        const cantidadGratuitos = Number(document.getElementById("registrograt").value);
        const codigo = document.getElementById("codigo").value.trim();

        //validaciones
        const institucionYaPatrocina = Datos.patrocinios.some(
            patrocinio => patrocinio.edicionId === edicionId &&
                          patrocinio.institucionId === institucionId
        );

        if (institucionYaPatrocina) {
            alert("La institución seleccionada ya patrocina esta edición.");
            return;
        }

        const codigoYaExiste = Datos.patrocinios.some(
            patrocinio => patrocinio.codigo.toLowerCase() === codigo.toLowerCase()
        );

        if (codigoYaExiste) {
            alert("El código de patrocinio ingresado ya existe.");
            return;
        }

        const tipoRegistro = Datos.tiposRegistro.find(
            tipo => tipo.id === tipoRegistroId
        );

        const costoRegistrosGratuitos = tipoRegistro.costo * cantidadGratuitos;
        const limitePermitido = aporte * 0.20;

        if (costoRegistrosGratuitos > limitePermitido) {
            alert("El costo de los registros gratuitos supera el 20% del aporte.");
            return;
        }

        //guardar
        const nuevoPatrocinio = {
            id: Datos.patrocinios.length + 1,
            edicionId: edicionId,
            institucionId: institucionId,
            nivel: nivel,
            aporte: aporte,
            tipoRegistroId: tipoRegistroId,
            cantidadGratuitos: cantidadGratuitos,
            codigo: codigo,
            fechaAlta: new Date().toISOString().split("T")[0]
        };

        Datos.patrocinios.push(nuevoPatrocinio);

        alert("Patrocinio registrado correctamente.");

        formAltaPatrocinio.reset();

        cargarSelect(selectEdicion, [], "Seleccione una edición");
        cargarSelect(selectTipoRegistro, [], "Seleccione un tipo de registro");

    });
}

if (detallePatrocinio) {
    const selectEvento = document.getElementById("evento");
    const selectEdicion = document.getElementById("edicion");
    const selectPatrocinio = document.getElementById("patrocinio");

    cargarSelect(selectEvento, Datos.eventos, "Seleccione un evento");

    selectEvento.addEventListener("change", function () {
        const eventoId = Number(selectEvento.value);

        if (!eventoId) {
            cargarSelect(selectEdicion, [], "Seleccione una edición");
            cargarSelect(selectPatrocinio, [], "Seleccione un patrocinio");

            selectEdicion.disabled = true;
            selectPatrocinio.disabled = true;

            detallePatrocinio.classList.add("d-none");
            return;
        }

        const edicionesAceptadas = Datos.ediciones.filter(
            edicion => edicion.eventoId === eventoId &&
                       edicion.estado === "Aceptada"
        );

        cargarSelect(
            selectEdicion,
            edicionesAceptadas,
            "Seleccione una edición"
        );

        selectEdicion.disabled = false;
        selectPatrocinio.disabled = true;

        cargarSelect(
            selectPatrocinio,
            [],
            "Seleccione un patrocinio"
        );

        detallePatrocinio.classList.add("d-none");
    });

    selectEdicion.addEventListener("change", function () {
        const edicionId = Number(selectEdicion.value);

        if (!edicionId) {
            cargarSelect(
                selectPatrocinio,
                [],
                "Seleccione un patrocinio"
            );

            selectPatrocinio.disabled = true;
            detallePatrocinio.classList.add("d-none");
            return;
        }

        const patrociniosEdicion = Datos.patrocinios
            .filter(patrocinio => patrocinio.edicionId === edicionId)
            .map(patrocinio => {
                const institucion = Datos.instituciones.find(
                    institucion => institucion.id === patrocinio.institucionId
                );

                return {
                    id: patrocinio.id,
                    nombre: institucion.nombre
                };
            });

        cargarSelect(
            selectPatrocinio,
            patrociniosEdicion,
            "Seleccione un patrocinio"
        );

        selectPatrocinio.disabled = false;
        detallePatrocinio.classList.add("d-none");
    });

    selectPatrocinio.addEventListener("change", function () {
        const patrocinioId = Number(selectPatrocinio.value);

        if (!patrocinioId) {
            detallePatrocinio.classList.add("d-none");
            return;
        }

        const patrocinio = Datos.patrocinios.find(
            patrocinio => patrocinio.id === patrocinioId
        );

        const institucion = Datos.instituciones.find(
            institucion => institucion.id === patrocinio.institucionId
        );

        const tipoRegistro = Datos.tiposRegistro.find(
            tipo => tipo.id === patrocinio.tipoRegistroId
        );

        const edicion = Datos.ediciones.find(
            edicion => edicion.id === patrocinio.edicionId
        );


        document.getElementById("institucion").textContent =
            institucion.nombre;

        document.getElementById("nivel").textContent =
            patrocinio.nivel;

        document.getElementById("aporte").textContent =
            "$" + patrocinio.aporte;

        document.getElementById("tipoRegistro").textContent =
            tipoRegistro.nombre;

        document.getElementById("registrograt").textContent =
            patrocinio.cantidadGratuitos;

        document.getElementById("fechaalta").textContent =
            patrocinio.fechaAlta;


        // Código visible solamente para el organizador de la edición
        const contenedorCodigo =
            document.getElementById("contenedorCodigo");

        if (edicion.organizadorId === organizadorActualId) {
            document.getElementById("codigo").textContent =
                patrocinio.codigo;

            contenedorCodigo.classList.remove("d-none");
        } else {
            contenedorCodigo.classList.add("d-none");
        }


        detallePatrocinio.classList.remove("d-none");
    });
}