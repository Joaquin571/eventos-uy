// Simulación de sesión (Parte 1).
// Cambiá esta línea para probar el otro rol:
//   { rol: "organizador", id: 1 }
const sesion = { rol: "organizador", id: 1 };

const formModificarUsuario = document.getElementById("formModificarUsuario");

if (formModificarUsuario) {
    const esAsistente = sesion.rol === "asistente";

    const usuario = esAsistente
        ? Datos.asistentes.find(a => a.id === sesion.id)
        : Datos.organizadores.find(o => o.id === sesion.id);

    const camposAsistente = document.getElementById("camposAsistente");
    const camposOrganizador = document.getElementById("camposOrganizador");
    const inputApellido = document.getElementById("apellido");
    const inputFechaNac = document.getElementById("fechaNacimiento");
    const selectInstitucion = document.getElementById("institucion");
    const inputDescripcion = document.getElementById("descripcion");
    const inputSitioWeb = document.getElementById("sitioWeb");

    // Mostrar solo los campos del rol y marcar como obligatorios los visibles
    camposAsistente.classList.toggle("d-none", !esAsistente);
    camposOrganizador.classList.toggle("d-none", esAsistente);
    inputApellido.required = esAsistente;
    inputFechaNac.required = esAsistente;
    inputDescripcion.required = !esAsistente;

    const rolActual = document.getElementById("rolActual");
    if (rolActual) {
        rolActual.textContent = esAsistente ? "Asistente" : "Organizador";
    }

    cargarSelect(selectInstitucion, Datos.instituciones, "Sin institución");

    // Precargar los datos actuales
    document.getElementById("nickname").value = usuario.nickname;
    document.getElementById("correo").value = usuario.correo;
    document.getElementById("nombre").value = usuario.nombre;

    if (esAsistente) {
        inputApellido.value = usuario.apellido;
        inputFechaNac.value = usuario.fechaNacimiento;
        selectInstitucion.value = usuario.institucionId || "";
    } else {
        inputDescripcion.value = usuario.descripcion;
        inputSitioWeb.value = usuario.sitioWeb;
    }

    document.getElementById("imagenActual").textContent = usuario.imagen
        ? "Imagen actual: " + usuario.imagen
        : "Todavía no tiene imagen. Opcional.";

    formModificarUsuario.addEventListener("submit", function (event) {
        event.preventDefault();

        const nombre = document.getElementById("nombre").value.trim();
        const password = document.getElementById("password").value;
        const confirmacion = document.getElementById("confirmacion").value;
        const inputImagen = document.getElementById("imagen");

        // Validaciones
        if (!nombre) {
            alert("El nombre es obligatorio.");
            return;
        }

        if (esAsistente && !inputApellido.value.trim()) {
            alert("El apellido es obligatorio.");
            return;
        }

        if (!esAsistente && !inputDescripcion.value.trim()) {
            alert("La descripción es obligatoria.");
            return;
        }

        // Cubre los dos casos: una llena y la otra vacía, o distintas
        if (password !== confirmacion) {
            alert("La contraseña y su confirmación no coinciden.");
            return;
        }

        // Guardar
        usuario.nombre = nombre;

        if (esAsistente) {
            usuario.apellido = inputApellido.value.trim();
            usuario.fechaNacimiento = inputFechaNac.value;
            usuario.institucionId = selectInstitucion.value
                ? Number(selectInstitucion.value)
                : null;
        } else {
            usuario.descripcion = inputDescripcion.value.trim();
            usuario.sitioWeb = inputSitioWeb.value.trim();
        }

        if (password) {
            usuario.password = password;
        }

        if (inputImagen.files.length > 0) {
            usuario.imagen = inputImagen.files[0].name;
            document.getElementById("imagenActual").textContent =
                "Imagen actual: " + usuario.imagen;
        }

        // La contraseña no queda visible en la pantalla
        document.getElementById("password").value = "";
        document.getElementById("confirmacion").value = "";
        inputImagen.value = "";

        alert("Datos actualizados correctamente.");
    });
}