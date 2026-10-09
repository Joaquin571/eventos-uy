const Datos = {
    categorias: [
        { id: 1, nombre: "Tecnología" },
        { id: 2, nombre: "Cultura" },
        { id: 3, nombre: "Deporte" },
        { id: 4, nombre: "Música" },
        { id: 5, nombre: "Negocios" }
    ],

    eventos: [
        {
            id: 1,
            nombre: "Montevideo Comics",
            descripcion: "Convención de historietas y cultura geek",
            sigla: "MVDCOMICS",
            categorias: [2],
            fechaAlta: "2026-03-12",
            imagen: null
        },
        {
            id: 2,
            nombre: "Maratón de Montevideo",
            descripcion: "Evento deportivo",
            sigla: "MARATON",
            categorias: [1, 3],
            fechaAlta: "2026-03-12",
            imagen: null
        }
    ],

    ediciones: [
        {
            id: 1,
            eventoId: 1,
            nombre: "Montevideo Comics 2026",
            estado: "Aceptada",
            organizadorId: 1,
            sigla: "MC26",
            ciudad: "Montevideo",
            pais: "Uruguay",
            fechaInicio: "2026-11-14",
            fechaFin: "2026-11-16"
        },
        {
            id: 2,
            eventoId: 1,
            nombre: "Montevideo Comics 2027",
            estado: "Ingresada",
            organizadorId: 1,
            sigla: "MC27",
            ciudad: "Montevideo",
            pais: "Uruguay",
            fechaInicio: "2027-11-13",
            fechaFin: "2027-11-15"
        },
        {
            id: 3,
            eventoId: 2,
            nombre: "Maratón de Montevideo 2026",
            estado: "Aceptada",
            organizadorId: 2,
            sigla: "MARATON26",
            ciudad: "Montevideo",
            pais: "Uruguay",
            fechaInicio: "2026-09-14",
            fechaFin: "2026-09-16"
        }
    ],
    instituciones: [
        {
            id: 1,
            nombre: "UTEC",
            descripcion: "Universidad Tecnológica",
            sitioWeb: "https://www.utec.edu.uy",
            imagen: "../img/utec.png"
        },
        {
            id: 2,
            nombre: "ANTEL",
            descripcion: "Empresa de telecomunicaciones",
            sitioWeb: "https://www.antel.com.uy",
            imagen: "../img/antel.png"
        }
    ],

    tiposRegistro: [
        {
            id: 1,
            edicionId: 1,
            nombre: "General",
            descripcion: "Acceso general a los tres días de la convención",
            costo: 1500,
            cupo: 100
        },
        {
            id: 2,
            edicionId: 1,
            nombre: "Estudiante",
            descripcion: "Acceso con descuento para estudiantes",
            costo: 800,
            cupo: 50
        },
        {
            id: 3,
            edicionId: 3,
            nombre: "Corredor 10K",
            descripcion: "Participación en la carrera de 10 kilómetros",
            costo: 1000,
            cupo: 2
        }
    ],

    patrocinios: [
        {
            id: 1,
            edicionId: 1,
            institucionId: 1,
            nivel: "Oro",
            aporte: 50000,
            tipoRegistroId: 1,
            cantidadGratuitos: 5,
            codigo: "UTEC2026",
            fechaAlta: "2026-10-01"
        }
    ],

    asistentes: [
        { id: 1, nickname: "juan23", nombre: "Juan Pérez", institucionId: 1 },
        { id: 2, nickname: "maria88", nombre: "María Gómez", institucionId: null },
        { id: 3, nickname: "ana_utec", nombre: "Ana Silva", institucionId: 1 }
    ],

    registros: [
        {
            id: 1,
            edicionId: 3,
            asistenteId: 1,
            tipoRegistroId: 3,
            fechaAlta: "2026-08-20",
            costo: 1000,
            patrocinioId: null
        },
        {
            id: 2,
            edicionId: 1,
            asistenteId: 2,
            tipoRegistroId: 2,
            fechaAlta: "2026-10-02",
            costo: 800,
            patrocinioId: null
        },
        {
            id: 3,
            edicionId: 1,
            asistenteId: 3,
            tipoRegistroId: 1,
            fechaAlta: "2026-10-03",
            costo: 0,
            patrocinioId: 1
        }
    ]
};