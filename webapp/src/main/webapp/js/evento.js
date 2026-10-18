const formAltaEvento = document.getElementById("formAltaEvento");

if (formAltaEvento) {

    // Cargar las categorías como checkboxes
    const contenedorCategorias = document.getElementById("categorias");

    Datos.categorias.forEach(cat => {
        contenedorCategorias.insertAdjacentHTML("beforeend", `
            <div class="form-check form-check-inline">
                <input class="form-check-input" type="checkbox"
                       name="categoria" id="cat${cat.id}" value="${cat.id}">
                <label class="form-check-label" for="cat${cat.id}">${cat.nombre}</label>
            </div>
        `);
    });

    formAltaEvento.addEventListener("submit", function (event) {
        event.preventDefault();

        const nombre = document.getElementById("nombre").value.trim();
        const descripcion = document.getElementById("descripcion").value.trim();
        const sigla = document.getElementById("sigla").value.trim();
        const inputImagen = document.getElementById("imagen");

        const categoriasElegidas = Array.from(
            formAltaEvento.querySelectorAll('input[name="categoria"]:checked')
        ).map(c => Number(c.value));

        let imagen = null;
        if (inputImagen.files.length > 0) {
            imagen = inputImagen.files[0].name;
        }

        // Validaciones
        if (!nombre || !descripcion || !sigla) {
            alert("Complete todos los campos obligatorios.");
            return;
        }

        if (categoriasElegidas.length === 0) {
            alert("Seleccione al menos una categoría.");
            return;
        }

        const nombreYaExiste = Datos.eventos.some(
            evento => evento.nombre.toLowerCase() === nombre.toLowerCase()
        );

        if (nombreYaExiste) {
            alert("Ya existe un evento con ese nombre.");
            return;
        }

        // Guardar
        const nuevoEvento = {
            id: Datos.eventos.length + 1,
            nombre: nombre,
            descripcion: descripcion,
            sigla: sigla,
            categorias: categoriasElegidas,
            fechaAlta: new Date().toISOString().split("T")[0],
            imagen: imagen
        };

        Datos.eventos.push(nuevoEvento);

        alert("Evento registrado correctamente.");
        formAltaEvento.reset();
    });
}

// ===== LISTADO DE EVENTOS (index.html) =====
const listaEventos = document.getElementById("event-list");

if (listaEventos) {
    const categoriaParam = new URLSearchParams(location.search).get("categoria");

    let eventos = Datos.eventos;

    if (categoriaParam) {
        const categoria = Datos.categorias.find(c => c.nombre === categoriaParam);
        eventos = categoria
            ? Datos.eventos.filter(e => e.categorias.includes(categoria.id))
            : [];
    }

    if (eventos.length === 0) {
        listaEventos.innerHTML =
            `<p class="text-secondary">No hay eventos en esta categoría.</p>`;
    } else {
        listaEventos.innerHTML = eventos.map(e => {
            const badges = e.categorias.map(id => {
                const cat = Datos.categorias.find(c => c.id === id);
                return `<span class="badge text-bg-primary me-1">${cat.nombre}</span>`;
            }).join("");

            return `
                <div class="col-12 col-md-6 col-lg-4">
                    <div class="card h-100 shadow-sm">
                        <div class="card-body">
                            <div class="mb-2">${badges}</div>
                            <h5 class="card-title">${e.nombre}</h5>
                            <p class="card-text text-secondary">${e.descripcion}</p>
                        </div>
                        <div class="card-footer bg-white border-0 pb-3">
                            <a href="pages/evento.html?id=${e.id}" class="btn btn-primary w-100">Ver evento</a>
                        </div>
                    </div>
                </div>`;
        }).join("");
    }
}

// ===== DETALLE DE EVENTO (pages/evento.html) =====
const detalleEvento = document.getElementById("detalleEvento");

if (detalleEvento) {
    const id = Number(new URLSearchParams(location.search).get("id"));
    const evento = Datos.eventos.find(e => e.id === id);

    if (!evento) {
        detalleEvento.innerHTML =
            `<div class="alert alert-warning">El evento no existe.</div>`;
    } else {
        const categorias = evento.categorias.map(cid => {
            const cat = Datos.categorias.find(c => c.id === cid);
            return `<a href="../index.html?categoria=${encodeURIComponent(cat.nombre)}"
                       class="badge text-bg-primary text-decoration-none me-1">${cat.nombre}</a>`;
        }).join("");

        const edicionesAceptadas = Datos.ediciones.filter(
            ed => ed.eventoId === evento.id && ed.estado === "Aceptada"
        );

        const listaEdiciones = edicionesAceptadas.length === 0
            ? `<p class="text-secondary">Este evento no tiene ediciones aceptadas.</p>`
            : `<div class="list-group">` + edicionesAceptadas.map(ed =>
            `<a class="list-group-item list-group-item-action"
                    href="edicion.html?id=${ed.id}">${ed.nombre}</a>`
        ).join("") + `</div>`;

        const imagen = evento.imagen
            ? `<img src="../img/${evento.imagen}" class="img-fluid rounded mb-3" alt="${evento.nombre}">`
            : "";

        detalleEvento.innerHTML = `
            <div class="card shadow-sm">
                <div class="card-body p-4">
                    ${imagen}
                    <h1 class="h3">${evento.nombre}</h1>
                    <p class="text-secondary">${evento.descripcion}</p>
                    <p><strong>Sigla:</strong> ${evento.sigla}</p>
                    <p><strong>Alta:</strong> ${evento.fechaAlta}</p>
                    <div class="mb-4">${categorias}</div>
                    <h2 class="h5">Ediciones</h2>
                    ${listaEdiciones}
                </div>
            </div>`;
    }
}