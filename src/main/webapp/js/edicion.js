document.addEventListener("DOMContentLoaded", function () {

        const form = document.getElementById("formAltaEdicion");
        const evento = document.getElementById("evento");
        const nombre = document.getElementById("nombre");
        const fechaInicio = document.getElementById("fechaInicio");
        const fechaFin = document.getElementById("fechaFin");
        const imagen = document.getElementById("imagen");
        const vistaImagen = document.getElementById("vistaImagen");
        const mensaje = document.getElementById("mensaje");

        const datos = App.datos;

        // Cargar eventos existentes.
        datos.eventos.forEach(function (e) {
            const opcion = document.createElement("option");
            opcion.value = e.id;
            opcion.textContent = e.nombre;
            evento.appendChild(opcion);
        });

        // Verificar que el nombre no esté repetido.
        function validarNombre() {
            const nombreIngresado = nombre.value.trim().toLowerCase();

            const existe = datos.ediciones.some(function (edicion) {
                return edicion.nombre.trim().toLowerCase() === nombreIngresado;
            });

            if (existe) {
                nombre.setCustomValidity(
                    "Ya existe una edición con ese nombre."
                );
            } else {
                nombre.setCustomValidity("");
            }
        }

        // Verificar las fechas.
        function validarFechas() {
            if (fechaInicio.value && fechaFin.value &&
                fechaFin.value < fechaInicio.value) {

                fechaFin.setCustomValidity(
                    "La fecha de finalización no puede ser anterior al inicio."
                );
            } else {
                fechaFin.setCustomValidity("");
            }
        }

        nombre.addEventListener("input", validarNombre);
        fechaInicio.addEventListener("change", validarFechas);
        fechaFin.addEventListener("change", validarFechas);

        // Vista previa de imagen.
        let urlImagen = null;

        imagen.addEventListener("change", function () {
            if (urlImagen) {
                URL.revokeObjectURL(urlImagen);
                urlImagen = null;
            }

            vistaImagen.classList.add("d-none");

            if (imagen.files.length > 0 &&
                imagen.files[0].type.startsWith("image/")) {

                urlImagen = URL.createObjectURL(imagen.files[0]);
                vistaImagen.src = urlImagen;
                vistaImagen.classList.remove("d-none");
            }
        });

        // Simulación del alta.
        form.addEventListener("submit", function (e) {
            e.preventDefault();

            validarNombre();
            validarFechas();

            if (!form.checkValidity()) {
                form.reportValidity();
                return;
            }

            const nuevaEdicion = {
                id: Math.max(
                    0,
                    ...datos.ediciones.map(e => Number(e.id) || 0)
                ) + 1,
                eventoId: Number(evento.value),
                nombre: nombre.value.trim(),
                sigla: document.getElementById("sigla").value.trim(),
                ciudad: document.getElementById("ciudad").value.trim(),
                pais: document.getElementById("pais").value.trim(),
                fechaInicio: fechaInicio.value,
                fechaFin: fechaFin.value,
                fechaAlta: new Date().toLocaleDateString("en-CA"),
                estado: "Ingresada",
                organizadorId: App.obtenerUsuarioActual() ? Number(App.obtenerUsuarioActual().id) : null,
                nombreOrganizador: App.obtenerUsuarioActual() ? App.obtenerUsuarioActual().nombre : ""
            };

            datos.ediciones.push(nuevaEdicion);

            mensaje.className = "alert alert-success";
            mensaje.textContent =
                "Edición ingresada correctamente (simulación). Estado: Ingresada.";

            form.reset();
        });

        // Limpiar validaciones.
        form.addEventListener("reset", function () {
            nombre.setCustomValidity("");
            fechaFin.setCustomValidity("");

            if (urlImagen) {
                URL.revokeObjectURL(urlImagen);
                urlImagen = null;
            }

            vistaImagen.removeAttribute("src");
            vistaImagen.classList.add("d-none");
        });

});