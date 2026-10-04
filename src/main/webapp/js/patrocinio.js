const organizadorActualId = 1;

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

const formPatrocinio = document.getElementById("formAltaPatrocinio");

formPatrocinio.addEventListener("submit", function (event) {
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

    formPatrocinio.reset();

    cargarSelect(selectEdicion, [], "Seleccione una edición");
    cargarSelect(selectTipoRegistro, [], "Seleccione un tipo de registro");

});