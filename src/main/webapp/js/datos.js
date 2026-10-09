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
            organizadorId: 1
        },
        {
            id: 2,
            eventoId: 1,
            nombre: "Montevideo Comics 2027",
            estado: "Ingresada",
            organizadorId: 1
        },
        {
            id: 3,
            eventoId: 2,
            nombre: "Maratón de Montevideo 2026",
            estado: "Aceptada",
            organizadorId: 2
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
            costo: 1500,
            cupo: 100
        },
        {
            id: 2,
            edicionId: 1,
            nombre: "Estudiante",
            costo: 800,
            cupo: 50
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
    ]
};