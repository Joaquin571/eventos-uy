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
}