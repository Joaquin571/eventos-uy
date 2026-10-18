const formInstitucion = document.getElementById("formAltaInstitucion");

formInstitucion.addEventListener("submit", function(event){
    event.preventDefault();

    const nombre = document.getElementById("nombre").value.trim();
    const descripcion = document.getElementById("descripcion").value.trim();
    const sitioWeb = document.getElementById("sitioWeb").value.trim();
    const inputImagen = document.getElementById("imagen");

    let imagen = null;

    if (inputImagen.files.length > 0) {
        imagen = inputImagen.files[0].name;
    }

    //validaciones
    const nombreYaExiste = Datos.instituciones.some(
        institucion => institucion.nombre.toLowerCase() === nombre.toLowerCase()
    );

    if (nombreYaExiste) {
        alert("Ya existe una institución con ese nombre.");
        return;
    }

    const nuevaInstitucion = {
        id: Datos.instituciones.length + 1,
        nombre: nombre,
        descripcion: descripcion,
        sitioWeb: sitioWeb,
        imagen: imagen
    };

    Datos.instituciones.push(nuevaInstitucion);

    alert("Institución registrada correctamente.");
    formInstitucion.reset();
});

