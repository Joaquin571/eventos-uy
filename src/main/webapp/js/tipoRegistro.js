const detalleTipoRegistro = document.getElementById("detalleTipoRegistro");

if (detalleTipoRegistro) {
    const selectEvento = document.getElementById("evento");
    const selectEdicion = document.getElementById("edicion");
    const selectTipoRegistro = document.getElementById("tipoRegistro");

    cargarSelect(selectEvento, Datos.eventos, "Seleccione un evento");

    selectEvento.addEventListener("change", function () {
        const eventoId = Number(selectEvento.value);

        cargarSelect(selectTipoRegistro, [], "Seleccione un tipo de registro");
        selectTipoRegistro.disabled = true;
        detalleTipoRegistro.classList.add("d-none");

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

        detalleTipoRegistro.classList.add("d-none");

        if (!edicionId) {
            cargarSelect(selectTipoRegistro, [], "Seleccione un tipo de registro");
            selectTipoRegistro.disabled = true;
            return;
        }

        const tiposDeLaEdicion = Datos.tiposRegistro.filter(
            tipo => tipo.edicionId === edicionId
        );

        cargarSelect(selectTipoRegistro, tiposDeLaEdicion, "Seleccione un tipo de registro");
        selectTipoRegistro.disabled = false;
    });

    selectTipoRegistro.addEventListener("change", function () {
        const tipoRegistroId = Number(selectTipoRegistro.value);

        if (!tipoRegistroId) {
            detalleTipoRegistro.classList.add("d-none");
            return;
        }

        const tipoRegistro = Datos.tiposRegistro.find(
            tipo => tipo.id === tipoRegistroId
        );

        document.getElementById("nombreTipo").textContent =
            tipoRegistro.nombre;

        document.getElementById("descripcionTipo").textContent =
            tipoRegistro.descripcion;

        document.getElementById("costoTipo").textContent =
            "$" + tipoRegistro.costo;

        document.getElementById("cupoTipo").textContent =
            tipoRegistro.cupo;

        detalleTipoRegistro.classList.remove("d-none");
    });

    // Si se llega desde la consulta de edición (tipo-registro.html?id=3)
    // se eligen solos el evento, la edición y el tipo de registro.
    const tipoIdParam = Number(new URLSearchParams(location.search).get("id"));

    if (tipoIdParam) {
        const tipoInicial = Datos.tiposRegistro.find(
            tipo => tipo.id === tipoIdParam
        );

        const edicionInicial = tipoInicial
            ? Datos.ediciones.find(
                edicion => edicion.id === tipoInicial.edicionId &&
                           edicion.estado === "Aceptada")
            : null;

        if (edicionInicial) {
            selectEvento.value = edicionInicial.eventoId;
            selectEvento.dispatchEvent(new Event("change"));

            selectEdicion.value = edicionInicial.id;
            selectEdicion.dispatchEvent(new Event("change"));

            selectTipoRegistro.value = tipoInicial.id;
            selectTipoRegistro.dispatchEvent(new Event("change"));
        }
    }
}